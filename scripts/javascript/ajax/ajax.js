/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met: Redistributions of source code
 * must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution. Neither the name of the Sun Microsystems nor the names of
 * is contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */


/*
 * This file contains AJAX implementation for Mustang's Rhino based
 * JavaScript engine. Also, we add few hacks to simulate browser-like
 * environment "fool" the existing "AJAX" using scripts.
 */

// Few concurrency, asynchronous utilities
var AsyncUtils = new Object();

/**
 * Creates a java.lang.Runnable from a given script
 * function.
 *
 * @param func function that accepts no argument
 * @return a java.lang.Runnable that wraps the function
 */
AsyncUtils.newRunnable = function(func) {
    return new java.lang.Runnable() {
        run: func
    }
};

/**
 * Creates a java.util.concurrent.Callable from a given script
 * function.
 *
 * @param func function that accepts no argument
 * @return a java.util.concurrent.Callable that wraps the function
 */
AsyncUtils.newCallable = function(func) {
    return new java.util.concurrent.Callable() {
          call: func
    }
};


/**
 * Registers the script function so that it will be called exit.
 *
 * @param func function that accepts no argument
 */
AsyncUtils.callAtExit = function (func) {
    java.lang.Runtime.getRuntime().addShutdownHook(
         new java.lang.Thread(AsyncUtils.newRunnable(func)));
};

// The default executor for futures

/* private */ 
AsyncUtils._theExecutor = java.util.concurrent.Executors.newCachedThreadPool(
    new java.util.concurrent.ThreadFactory() {
        newThread: function(r) {
            var th = new java.lang.Thread(r);
            // create daemon threads for futures
            th.setDaemon(true);
            return th;
        }
    });

// clean-up the default executor at exit
AsyncUtils.callAtExit(function() { AsyncUtils._theExecutor.shutdown(); });


/**
 * Executes the function asynchronously with a future task.
 *
 * @param func function that accepts no argument
 * @return a java.util.concurrent.FutureTask
 */
AsyncUtils.submitFuture = function(func) {
    return AsyncUtils._theExecutor.submit(AsyncUtils.newCallable(func));   
};


/**
 * Creates a new Thread that will execute the given
 * function when started.
 *
 * @param func function that accepts no argument
 * @param daemon whether to create a daemon or not [optional]
 *               default value is false
 * @return a java.lang.Thread object
 */
AsyncUtils.newThread = function(func, daemon) {
    if (daemon == undefined) {
        daemon = false;
    }
    var th = new java.lang.Thread(AsyncUtils.newRunnable(func));
    th.setDaemon(daemon);
    return th;
};


// support for locks, condition variables

/**
 * Creates a java.util.concurrent reentrant lock
 *
 * @param fair whether we want a fair lock or not [optional]
 *             default is false
 */
AsyncUtils.newLock = function(fair) {
    if (fair == undefined) {
        fair = false;
    }
    return java.util.concurrent.locks.ReentrantLock(fair);
}

/**
 * Creates a java.util.concurrent reentrant read-write lock
 *
 * @param fair whether we want a fair lock or not [optional]
 *             default is false
 */
AsyncUtils.newRWLock = function(fair) {
    if (fair == undefined) {
        fair = false;
    }
    return java.util.concurrent.locks.ReentrantReadWriteLock(fair);
}



// XML, DOM utilities

// minimal XMLSerializer object -- just serialize XML to String

if (this.XMLSerializer == undefined) {
    this.XMLSerializer = function() {}


    /*
     * Spec: http://www.xulplanet.com/references/objref/XMLSerializer.html
     *
     * Note that this is a partial implementation of XMLSerializer.
     * In particular, no serialize to a stream - only to a string.
     */  

    XMLSerializer.prototype.serializeToString = function(node) {
        if (node == null) {
            return "";
        }

        var xmlAPI = new JavaImporter(javax.xml.parsers,
                 javax.xml.transform,
                 javax.xml.transform.dom,
                 javax.xml.transform.stream,
                 java.io);

        with (xmlAPI) {
            var source = new DOMSource(node);
            var sw = new StringWriter();
            var result = new StreamResult(sw);
            var xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
            return String(sw.toString());
        } 
    };
}

// minimal DOMParser - just parsing from String
if (this.DOMParser == undefined) {
    this.DOMParser = function() {}

    /*
     * Spec: http://www.xulplanet.com/references/objref/DOMParser.html
     *
     * Note that this is a partial implementation of XMLSerializer.
     * We support just parsing from a string source.
     */

    DOMParser.prototype.parseFromString = function(str, contentType) {
        // ignore contentType for now...

        var xmlAPI = new JavaImporter(javax.xml.parsers,
                 javax.xml.transform,
                 javax.xml.transform.dom,
                 javax.xml.transform.stream,
                 javax.xml.transform.stream,
                 java.io,
                 org.xml.sax);
        with (xmlAPI) {
            var fac = DocumentBuilderFactory.newInstance();
            var docBuilder = fac.newDocumentBuilder();
            var r = new StringReader(str);
            return docBuilder.parse(new InputSource(r));
        }
    };
}


// function to create an empty DOM Document
if (this.DOMDocument == undefined) {

    this.DOMDocument = function() {
        var xmlAPI = new JavaImporter(javax.xml.parsers);
        with (xmlAPI) {     
            var fac = DocumentBuilderFactory.newInstance();
            return fac.newDocumentBuilder().newDocument();
        }
    }
}

/**
 * The "AJAX fame" XMLHttpRequest constructor.
 *
 * Spec: http://www.w3.org/TR/XMLHttpRequest/
 */
function XMLHttpRequest() {

    // readyState constants
    var INITIALIZED = 0;
    var OPEN        = 1;
    var SENT        = 2;
    var RECEIVING   = 3;
    var LOADED      = 4;

    var javax = Packages.javax;

    // HTTP methods
    var POST = "POST";
    var GET = "GET";
    var HEAD = "HEAD";

    var UTF8 = "UTF-8";

    function stringToDOM(str) {
        try {
            var parser = new DOMParser();
            return parser.parseFromString(str);
        } catch (e) {
            alert(e);
            return null;
        }
    }

    function domToString(doc) {
        try {
            var serializer = new XMLSerializer();
            return serializer.serializeToString(doc);
        } catch (e) {
            alert(e);
            return "";
        }
    }

    function listToString(list) {
        if (list == null) 
            return "";

        var i = list.iterator();
        if (! i.hasNext())
            return "";

        var sb = new java.lang.StringBuilder();
        for (;;) {
            var e = i.next();
            sb.append(e);
            if (! i.hasNext())
                return sb.toString();
            sb.append(", ");
        }
    }

    function throwInvalidState(rs, method) {
        throw "invalid state: " + rs + " in " + method;
    }    

    function XMLHttpRequestImpl() {
        this._readyState = INITIALIZED;

        this._responseText = "";
        this._status = 0;
        this._statusText = null;

        this._futureTask = null;
        this._connection = null;
        this._requestHeaders = null;

        this._method = "GET";
        this._async = true;
    }

    XMLHttpRequestImpl.prototype.setOnreadystatechange = sync(function(func) {           
            this._onreadystatechange = func;           
        });


    XMLHttpRequestImpl.prototype.getOnreadystatechange = sync(function() {
            return this._onreadystatechange;
        });

    XMLHttpRequestImpl.prototype.setReadyState = sync(
        function setReadyState(state) {
            this._readyState = state;
            if (this["_onreadystatechange"]) {
                this._onreadystatechange.apply(this);
            }
        });

    XMLHttpRequestImpl.prototype.getReadyState = sync(
        function getReadyState() {            
            return this._readyState;
        });

    XMLHttpRequestImpl.prototype.open = sync(function(method, url, 
                                                async, user, password) {
            var rs = this.getReadyState();
            if (rs != INITIALIZED && rs != LOADED) {
                throwInvalidState("XMLHttpRequest.open");
            }
            this._method = method.toUpperCase();
            this._url = url;
            if (async == undefined) {
                async = true;
            }
            this._async = async;

            this._responseText = "";
            this._status = 0;
            this._statusText = null;

            this._futureTask = null;
            this._connection = null;
            this._requestHeaders = null;

            this.setReadyState(OPEN);          
        });

    XMLHttpRequestImpl.prototype.setRequestHeader = sync(function (header, value) {
            var rs = this.getReadyState();
            if (rs != OPEN) {
                throwInvalidState(rs, "XMLHttpRequest.setRequestHeader");
            }

            if (! this["requestHeaders"]) {
                this._requestHeaders = new Object();
            }
            this._requestHeaders[header] = value;
        });


    XMLHttpRequestImpl.prototype.send = sync(function(content) {
            var rs = this.getReadyState();
            if (rs != OPEN) {
                throwInvalidState(rs, "XMLHttpRequest.send");
            }
            if (! content) {
                content = "";
            }
            if (content instanceof org.w3c.dom.Node) {
                content = domToString(content);
            } else {
                content = String(content);
            }

            this.connect(content);
            this.setRequestHeaders();
            this.sendRequest(content);
            this.setReadyState(SENT);

            var obj = this;
            if (this._async) {
                function func() {
                    obj.readResponse();                        
                }
                this._futureTask = AsyncUtils.submitFuture(func);
            } else {
                this.readResponse();
            }
        });
    
    XMLHttpRequestImpl.prototype.abort = sync(function() {
            if (this["futureTask"]) {
                this._futureTask.cancel();
            }
        });


    XMLHttpRequestImpl.prototype.getAllResponseHeaders = sync(function() {
            var rs = this.getReadyState();
            if (rs != RECEIVING && rs != LOADED) {
                return "";
            }
        
            var headers = this._connection.getHeaderFields();
        
            var sb = new java.lang.StringBuilder();
            var itr = headers.keySet().iterator();
            while (itr.hasNext()) {
                var key = itr.next();
                if (key != null) {
                    sb.append(key);
                    sb.append(": ");
                    sb.append(listToString(headers.get(key)));
                    sb.append("\n");
                }
            }
            return sb.toString();
        });

    XMLHttpRequestImpl.prototype.getResponseHeader = sync(function(header) {
            var rs = this.getReadyState();
            if (rs != RECEIVING && rs != LOADED) {
                return "";
            }
        
            var headers = this._connection.getHeaderFields();
            if (headers == null) {
                return "";
            }
            var values = headers.get(header);
            return listToString(values);
        });

    XMLHttpRequestImpl.prototype.getResponseText = sync(function() {
            var rs = this.getReadyState();
            if (rs != RECEIVING && rs != LOADED) {
                return "";
            }
            return this._responseText;
        });

    XMLHttpRequestImpl.prototype.getResponseXML = sync(function() {
            var rs = this.getReadyState();
            if (rs != LOADED) {
                return null;
            } else {                
                return this._responseXML;
            }
        });

    XMLHttpRequestImpl.prototype.getStatus = sync(function() {
            var rs = this.getReadyState();
            if (rs != RECEIVING && rs != LOADED) {
                throwInvalidState(rs, "XMLHttpRequest.status");
            }
            return this._status;
        });

    XMLHttpRequest.prototype.getStatusText = sync(function() {
            var rs = this.getReadyState();
            if (rs != RECEIVING && rs != LOADED) {
                throwInvalidState(rs, "XMLHttpRequest.statusText");
            }
            return this._statusText;
        });

    XMLHttpRequestImpl.prototype.connect = function connect(content) {
            var u;

            /*
             * We do not have the notion of base URL of the current page.
             * Client scripts need to call "ajaxInit" to initialize 
             * window.location.pathname. If available, we use that as
             * "context" URL or else we expect an absolute URL.
             */

            if (typeof(window) == 'object' && 
                typeof(window.location) == 'object' &&
                window.location.pathname != undefined) {
                u = new java.net.URL(new java.net.URL(window.location.pathname), this._url);
            } else {
                u = new java.net.URL(this._url);
            }
            this._connection = u.openConnection();
            this._connection.setInstanceFollowRedirects(true);
            this._connection.setRequestMethod(this._method);
            if (this._method.equals(POST) || content.length > 0) {
                this._connection.setDoOutput(true);
            }
            if (this._method.equals(POST) || this._method.equals(GET)) {
                this._connection.setDoInput(true);
            }
        };

    XMLHttpRequestImpl.prototype.setRequestHeaders = function() {
            if (this["requestHeaders"]) {
                for (var header in requestHeaders) {
                    var value = this._requestHeaders[header];
                    this._connection.setRequestProperty(header, value);
                }
            }
        };


    XMLHttpRequestImpl.prototype.sendRequest = function(content) {
        if (this._method.equals(POST) || content.length > 0) {
            var out = new java.io.OutputStreamWriter(this._connection
                        .getOutputStream(), UTF8);
            out.write(content);
            out.flush();
            out.close();
        }

        this._status = this._connection.responseCode;
        this._statusText = this._connection.responseMessage;
    }

    XMLHttpRequestImpl.prototype.setResponseChunk = sync(function (str) {
            this._responseText = str;
            this.setReadyState(RECEIVING);           
        });

    XMLHttpRequestImpl.prototype.readResponse = function() {
        var encoding = this._connection.contentEncoding;
        if (encoding == null) {
            encoding = UTF8;
        }
        var r = new java.io.InputStreamReader(this._connection.getInputStream(),
                                     encoding);
        var sb = new java.lang.StringBuffer();           
        var arr = java.lang.reflect.Array.newInstance(java.lang.Character.TYPE,
                        512); // 0.5K at a time
        var numChars;            
        while ((numChars = r.read(arr, 0, arr.length)) > 0) {            
            sb.append(arr, 0, numChars);
            this.setResponseChunk(sb.toString());
            if (java.lang.Thread.interrupted()) { 
                break;
            }
        }
        r.close();               
        this.convertResponseToDOM();
    }

    XMLHttpRequestImpl.prototype.convertResponseToDOM = sync(function() {
            var contentType = this._connection.getContentType();
            var idx = contentType.indexOf(";");
            if (idx != -1) {
                contentType = contentType.substring(0, idx);
                contentType = contentType.trim();
            }
            if (contentType.equals("application/xml") || 
                contentType.equals("text/xml") ||
                contentType.endsWith("+xml")) {
                this._responseXML = stringToDOM(this._responseText);
            } else {
                this._responseXML = null;
            }
            this.setReadyState(LOADED);            
        });

    var impl = new XMLHttpRequestImpl();
    var res = new JSAdapter() {
        __has__: function (name) {
            switch (name) {
            case 'onreadystatechange':
            case 'readyState':
            case 'open':
            case 'setRequestHeader':
            case 'send':
            case 'abort':
            case 'getAllResponseHeaders':
            case 'getResponseHeader':
            case 'responseText':
            case 'responseXML':
            case 'status':
            case 'statusText':
                return true;
            default:
                return false;
            }
        },

        __get__: function(name) {
            switch (name) {
            case 'onreadystatechange': {
                var wrapper = impl.getOnreadystatechange();
                return (wrapper)? wrapper.realFunc : wrapper;
            }
            case 'readyState':
                return impl.getReadyState();
            case 'open':
                return function() {
                    return impl.open.apply(impl, arguments);
                }
            case 'setRequestHeader':
                return function() {
                    return impl.setRequestHeader.apply(impl, arguments);
                }
            case 'send':
                return function() {
                    return impl.send.apply(impl, arguments);
                }
            case 'abort':
                return function() {
                    return impl.abort.apply(impl, arguments);
                }
            case 'getAllResponseHeaders':
                return function() {
                    return String(impl.getAllResponseHeaders.apply(impl, arguments));
                }
            case 'getResponseHeader':
                return function() {
                    return String(impl.getResponseHeader.apply(impl, arguments));
                }
            case 'responseTex':
                return String(impl.getResponseText());
            case 'responseXML':
                return impl.getResponseXML();
            case 'status':
                return impl.getStatus();
            case 'statusText':
                return String(impl.getStatusText());
            default:
                return undefined;
            }
        },

        __put__: function(name, value) {
            switch (name) {
            case 'onreadystatechange': {                
                function wrapper() { value.apply(res); }
                wrapper.realFunc = value;
                return impl.setOnreadystatechange(wrapper);
            }
            default:
                return;
            }
        }
    };
    return res;
}

/*
 * The following is not strictly part of AJAX JavaScript API. But,
 * unfortunately, most AJAX scripts assume browser environment and
 * access browser DOM (and other) built-in objects.
 */

/*
 * XMLHttpRequest in checked in "window" object -- which is same 
 * as "global" scope object in browser's embedded JavaScript engine. 
 * This is a hack to simulate the same here.
 */
if (this.window == undefined) {
    this.window = this;
}

/*
 * Hack to simulate "navigator" variable.
 */
if (this.navigator == undefined) {
    this.navigator = { userAgent: "Java" };
}

/*
 * AJAX scripts access "document" object -- sometimes to update current
 * HTML page's DOM but sometimes for generating new DOM Nodes. While we 
 * can't support the former, we can support Document creation. We create 
 * an empty DOM Document object and store it in a global variable.
 */
if (this.document == undefined) {
    this.document = new DOMDocument();
}

/*
 * DOMImplementation singleton object
 */
if (this.DOMImplementation == undefined) {
    this.DOMImplementation = (function() {
        var xmlAPI = new JavaImporter(javax.xml.parsers);
        with (xmlAPI) {
            var fac = DocumentBuilderFactory.newInstance();
            return fac.newDocumentBuilder().getDOMImplementation();
        }
    })();
}

/*
 * 'alert' function - we just println the alert message
 */
if (this.alert == undefined) {
    this.alert = println;
}

/*
 * We don't have the notion of current webpage. User needs to
 * all this to supply the base url of the "current" page.
 * This base URL (if defined) is used by XMLHttpRequest.
 */
function ajaxInit(url) {
   window.location = { pathname: url };
}

