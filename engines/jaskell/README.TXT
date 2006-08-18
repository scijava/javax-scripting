This is JSR-223 script engine for the Jaskell programming language.
Jaskell is a functional scripting programming language that runs on the
JVM. The name "Jaskell" stands for Java-Haskell, Haskell being the 
famous pure functional programming language. Jaskell is available for
download at http://jaskell.codehaus.org. We have built and tested using
Jaskell version 1.0.


NOTE: There is binary as well as source distributions of Jaskell. But, 
the jparsec.jar and jfunutil.jar that come from the binary distribution
does *not* work (for example, Jaskell interactive shell class does *not*
work with these jar files [jfun.jaskell.shell.Shell]). But, the 
corresponding jar files from the source distribution work as expected. 
So, we need to use

* jaskell-1.0.jar from the binary distribution
* jparsec.jar and jfunutil.jar from the source distribution

