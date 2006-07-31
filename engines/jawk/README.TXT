This is JSR-223 script engine for the awk language. This is based on the
jawk implementation of the awk language. jawk is available for download at 
http://sourceforge.net/projects/jawk. We have built and tested jawk 0.14.

Pre-defined variable names in ScriptContext:

stdin -> InputStream to be used as input stream. If not defined, System.in
         is used.

stdout -> PrintStream to be used as output stream. If not defined, System.out
         is used.

stderr -> PrintStream to be used as error stream. If not defined, System.err
         is used.

FS -> Field separator char (same as awk's -F option)

arguments -> Array of strings passed as argument to awk script. If not defined,
            empty String array is used.

