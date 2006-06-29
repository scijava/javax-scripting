Requirements:

To build script engines, you need 

1. Mustang (Java SE 6) JDK - available at https://mustang.dev.java.net
2. Apache ant build tool - http://ant.apache.org (we have used ant 1.5.4)

Build instructions:

1. To build all script engines, use build.xml in the current directory.
You may run

  ant

to build all script engines.

2. To build an individual script engine, use the following instructions:

  cd <script-engine-dir>/make
  ant

To run:

1. cd <script-engine-dir>/bin
2. There is a shell script and batch file to run the engine.

These are wrapper scripts over jrunscript command line tool that is shipped
with Mustang JDK.
