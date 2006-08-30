
/*
 * This function returns a convenient wrapper for browser's top-level Window 
 * object. This uses Java-to-JavaScript Liveconnect feature (i.e., browser's)
 * JavaScript engine. This function is called just before initialization to 
 * initialize a global variable by the name "window".  Note that to use this
 * feature, applet tag should use mayscript="true" attribute or else window is
 * initialized to null (due to security restrictions).
 *
 * With this convenient object, scriptlet JavaScript code can access browser's
 * JavaScript objects and HTML DOM objects. User can use familiar object.field
 * syntax to access fields and write fields. For example,
 *
 *    window.location.href
 *
 * returns the current web page's URL value as a string.
 *
 * Also calling a  function (or method) is straight forward. For example, to call
 * the browser's alert function, scriptlet code has to use
 *
 *    window.alert("hello, world");
 */
function wrapJSWindow(applet) {
    var JSObject;
    try {
        JSObject = Packages.netscape.javascript.JSObject;
    } catch(e) {
        return null;
    }

    var wrappedJSObject = "wrapped-jsobject";

    function JSObjectWrapper(jsobj) {

        function wrapResult(res) {
            if (res instanceof JSObject) {
                return new JSAdapter(new JSObjectWrapper(res));
            } else {
                return res;
            }
        }

        this.__has__ = function(name) {
            return jsobj.eval("typeof(" + name + ")") != 'undefined';
        };

        this.__get__ = function(name) {
            if (name == wrappedJSObject) {
                return jsobj;
            }

            var type = jsobj.eval("typeof(" + name + ")");
            if (type == 'function') {
                return function() {
                    var args = new Array();
                    for (var i = 0; i < arguments.length; i++) {
                        args[i] = arguments[i];
                    }                
                    return wrapResult(jsobj.call(name, args));
                }
            }

            var res;
            if (typeof(name) == 'number') {
                res = jsobj.getSlot(name);
            } else {
                res = jsobj.getMember(name);
            }           
            return wrapResult(res);
        };

        this.__put__ = function (name, value) {
            if (name == wrappedJSObject) {                
                return;
            }

            if (value[wrappedJSObject] != undefined) {
                value = value[wrappedJSObject];
            }

            if (typeof(name) == 'number') {
                jsobj.setSlot(name, value);
            } else {
                jsobj.setMember(name, value);
            }
        };

        this.__delete__ = function (name) {
            jsobj.removeMember(name);
        };
    }

    try {
        var win = JSObject.getWindow(applet);    
        return new JSAdapter(new JSObjectWrapper(win));
    } catch (e) {
        return null;
    }
}

// utilities for animations

/**
 * Causes current thread to sleep for specified
 * number of milliseconds
 *
 * @param interval in milliseconds
 */
function sleep(interval) {
    java.lang.Thread.sleep(interval);
}

/**
 * Schedules a task to be executed once in
 * every N milliseconds specified. 
 *
 * @param callback function or expression to evaluate
 * @param interval in milliseconds to sleep
 * @return timeout ID (which is nothing but Thread instance)
 */
function setTimeout(callback, interval) {
    if (! (callback instanceof Function)) {
        callback = new Function(callback);
    }

    // start a new thread that sleeps given time
    // and calls callback in an infinite loop
    var th = new java.lang.Thread(function () {
        while (true) {
            try {
                sleep(interval);
            } catch (e) {
                return;
            }
            callback();
        }
    });
    th.start();
    return th;
}

/** 
 * Cancels a timeout set earlier.
 * @param tid timeout ID returned from setTimeout
 */
function clearTimeout(tid) {
    // we just interrupt the timer thread
    tid.interrupt();
}

// few applet utilities

/** 
 * Returns an audio clip 
 *
 * @param applet the applet whose audio clip is returned
 * @param url of the audio clip
 */
function audio(applet, url) {
    url = String(url);
    return applet.getAudioClip(applet.getDocumentBase(),url);
}

/** 
 * Returns an image
 *
 * @param applet the applet whose image is returned
 * @param url of the image
 */
function image(applet, url) {
    url = String(url);
    return applet.getImage(applet.getDocumentBase(), url);
}

/** 
 * Shows the given document in browser.
 *
 * @param applet the applet whose document is shown
 * @param url of the page to show
 * @param where which browser window option to use [optional]
 */
function showDocument(applet, url, where) {
    var ctxt = applet.getAppletContext();
    url = String(url);
    if (where) {
        ctxt.showDocument(applet.getDocumentBase(), url, where);
    } else {
        ctxt.showDocument(applet.getDocumentBase(), url);
    }
}

/*
 * Few user interface utilities. 
 */
 
/** 
 * Swing invokeLater - invokes given function in AWT event thread
 */
Function.prototype.invokeLater = function() {
    var SwingUtilities = javax.swing.SwingUtilities;
    var func = this;
    var args = arguments;
    SwingUtilities.invokeLater(new java.lang.Runnable() {
                        run: function() { 
                            func.apply(func, args);
                        }
                   });
}
 
/** 
 * Swing invokeAndWait - invokes given function in AWT event thread
 * and waits for it's completion
 */
Function.prototype.invokeAndWait = function() {
     var SwingUtilities = javax.swing.SwingUtilities;
     var func = this;
     var args = arguments;
     SwingUtilities.invokeAndWait(new java.lang.Runnable() {
                        run: function() { 
                            func.apply(func, args);
                        }
                   });
}

 /**
  * Am I running in AWT event dispatcher thread?
  */
 function isEventThread() {
     var SwingUtilities = javax.swing.SwingUtilities;
     return SwingUtilities.isEventDispatchThread();
 }
  
/**
 * Shows a message box
 *
 * @param msg message to be shown
 * @param title title of message box [optional]
 * @param msgType type of message box [constants in JOptionPane]
 */
function msgBox(msg, title, msgType) {
    
    function _msgBox() { 
        var JOptionPane = javax.swing.JOptionPane;
        if (msg === undefined) msg = "undefined";
        if (msg === null) msg = "null";
        if (title == undefined) title = msg;
        if (msgType == undefined) type = JOptionPane.INFORMATION_MESSAGE;
        JOptionPane.showMessageDialog(null, msg, title, msgType);
    }
    if (isEventThread()) {
        _msgBox();
    } else {
        _msgBox.invokeAndWait();
    }
}

/**
 * Shows an information alert box
 *
 * @param msg message to be shown
 * @param title title of message box [optional]
 */   
function alert(msg, title) {
    var JOptionPane = javax.swing.JOptionPane;
    msgBox(msg, title, JOptionPane.INFORMATION_MESSAGE);
}

/**
 * Shows an error alert box
 *
 * @param msg message to be shown
 * @param title title of message box [optional]
 */
function error(msg, title) {
    var JOptionPane = javax.swing.JOptionPane;
    msgBox(msg, title, JOptionPane.ERROR_MESSAGE);
}

/**
 * Shows a warning alert box
 *
 * @param msg message to be shown
 * @param title title of message box [optional]
 */
function warn(msg, title) {
    var JOptionPane = javax.swing.JOptionPane;
    msgBox(msg, title, JOptionPane.WARNING_MESSAGE);
}

/**
 * Shows a prompt dialog box
 *
 * @param question question to be asked
 * @param answer default answer suggested [optional]
 * @return answer given by user
 */
function prompt(question, answer) {
    var result;
    function _prompt() {
        var JOptionPane = javax.swing.JOptionPane;
        if (answer == undefined) answer = "";
        result = JOptionPane.showInputDialog(null, question, answer);
    }
    if (isEventThread()) {
        _prompt();
    } else {
        _prompt.invokeAndWait();
    }
    return result;
}

/**
 * Shows a confirmation dialog box
 *
 * @param msg message to be shown
 * @param title title of message box [optional]
 * @return boolean (yes->true, no->false)
 */
function confirm(msg, title) {
    var result;
    var JOptionPane = javax.swing.JOptionPane;
    function _confirm() {
        if (title == undefined) title = msg;
        var optionType = JOptionPane.YES_NO_OPTION;
        result = JOptionPane.showConfirmDialog(null, msg, title, optionType);
    }
    if (isEventThread()) {
        _confirm();
    } else {
        _confirm.invokeAndWait();
    }     
    return result == JOptionPane.YES_OPTION;
}