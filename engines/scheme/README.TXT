This is JSR-223 script engine for SISC - Java implementation of Scheme language.
SISC is available for download at http://sisc-scheme.org/. We have
built and tested with SISC version 1.16.6.

Note on accessing jsr-223 Binding variables from Scheme:

SISC implementation has support for pluggable top level environments. But, 
SISC engine expects to map symbol to ints and use that when looking those ints up
later. But, jsr-223 supports variables though a Map and map entries may be 
changed/deleted/added freely -- both from Java and the underlying scripting 
language. So, the way jsr-223 Bindings works is as follows:

Scheme user has to use "var" procedure to access/update the variables exposed by 
jsr-223 bindings. To access variable "foo" from Scheme, you use (var 'foo). To 
update variable "foo", you need to use (var 'foo <value>). 

For example, you can use the following expression to print the current file name.

    (display (var 'javax.script.filename))

Similarly, the following expression

    (var 'context) 

can be used to access the current script context from scheme.

