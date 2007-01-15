Batik (http://xml.apache.org/batik) is an SVG (Scalable Vector Graphics)
toolkit. This SVG toolkit supports SVG 1.1 (http://www.w3.org/TR/SVG)
specification with support for script tag. With script tag, SVG browsers
can support interactivity, animation etc. Batik's script tag support is 
available for JavaScript (using Mozilla Rhino). This application adds
support for any jsr-223 scripting language to be used with script tag.

How to build?

* download Batik 1.6 from http://xmlgraphics.apache.org/batik/download.cgi
* install it under say $BATIK_HOME dir and also define a shell environment
variable BATIK_HOME to point to that.
* to build svg-jsr223.jar, use the following instructions: 
    cd ./make
    ant -Dbatik.dir=$BATIK_HOME

How to run?

To use scripting languages with Batik, you need to relax certain
security restrictions on script engine jar and scripting implementation
jar files. There is an unsafe.policy (do *not* use this in production)
in ./bin directory and there are shell scripts and batch files for
Groovy and JRuby. It is possible to use any other jsr-223 compatible
language as well. You may want to write similar shell scripts or batch files.

To use any jsr-223 scripting language, use type="x-script/language"
as attribute in script tag. For eg. for Groovy, use type="x-script/groovy"
as MIME type. For JRuby, use type="x-script/ruby" in script tag.
