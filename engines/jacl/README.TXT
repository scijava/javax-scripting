This is JSR-223 script engine for the Jacl language. Jacl is Java 
implementation of Tcl (Tool Command Language). This is available for 
download at http://tcljava.sourceforge.net/. We have built and tested with
Jacl version 1.3.3.

TCL (Jacl) user has to use "var" procedure to access/update the variables exposed 
by jsr-223 bindings. To access variable "foo" from Tcl, you use [var "foo"]. To 
update variable "foo", you need to use 

    var "foo" <value>. 

For example, you can use the following expression to print the current file name.

    echo [var "javax.script.filename"]

Similarly, the following expression

    var "context"

can be used to access the current script context from Tcl.


