This is JSR-223 script engine for the Java Math Expression Parser (JEP)
language. The Java Math Expression Parser (JEP) is available for download 
http://www.singularsys.com/jep/. We have built and tested with JEP 
version 2.4.0.

This script engine is an expression-only script engine. This engine
supports various "modes". The mode determines the data-type used for
arithmetic.

1. complex  - supports complex number arithmetic
              [components are doubles]
2. integer  - supports BigInteger arithmetic
3. real     - supports double arithmeticc
4. bigreal  - supports BigDecimal arithmetic
5. rational - supports rational number arithmetic
              [components are BigIntegers]
6. quaternion - supports quaternion arithmetic
              [components are doubles]
7. vector - vectors, matices and tensors
            [components are doubles]
8. Zn (where n is a BigInteger)
      - supports integers mod n group

If no mode is set, then double arithmetic is supported. There are 
two different ways to set the mode of JEP script engine:

1. Set "jep.mode" attribute of the ScriptContext
to one of the above mode values.

2. Call "mode" built-in function in a JEP expression.
The argument passed should be one of the above modes.
