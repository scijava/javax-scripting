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
 * This is a AJAX test client for "DWR" (Direct Web Remoting)
 * http://dwr.dev.java.net. After installing DWR in your web container 
 * such as Tomcat, you can run the following script with jrunscript 
 * using the the command line:
 *
 *     jrunscript -f test.js -f -
 *
 * Note that the above command evaluates test.js and then evaluates 
 * standard input (-) so that the script waits for async. call to finish.
 */

// load ajax.js to get XMLHttpRequest and other related functions
load("ajax.js");

// need to initialize AJAX to pass "base URL" of the remote site
ajaxInit("http://localhost:8080");

// load DWR's engine.js and particular remote interface script
load("http://localhost:8080/dwr/dwr/engine.js");
load("http://localhost:8080/dwr/dwr/interface/Test.js");

// call a remote method using DWR, we just print the output
// from the callback function.
Test.stringStringParam("hello", "world", 
   function(data) {
       print(data);   
   });


