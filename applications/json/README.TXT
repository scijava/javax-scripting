This is JSON (JavaScript Object Notation) handling library for Java.
JSON is a lightweight data interchange format. It is easy to read, write,
parse text format that can be used as a replacement for XML.

This Java library is based on JSON library at http://www.json.org/java.
The org.json package is licensed under the license info found in the
"THIRDPARTYLICENSES.TXT" file.

Unlike other JSON libraries, this library handles circular references.
This is done by emitting "$id" and "$idref" properties (like XML/HTML's
ID and IDREF attributes). Also, optionally, "$class" property may
be emitted to map JSON object and array nodes specific Java types.
The deserializer optionally can make use "$class" properties, if available.
Or else, the deserializer map JSON objects to java.util.Map instances
and JSON arrays to java.util.ArrayList instances. Note that JSON syntax is
*not* extended. Only that few properties such as $id, $idref and $class
are treated specially (to resolve cyclical references and to map to objects
of specific system (for example Java classes)).

This is JSON extension in the sense that all properties that start with "$"
in the name are reserved and used for various purposes. Currently used
special properites:

* $id

String valued property that identifies an object uniquely within a JSON
"file"/"stream".

* $idref

String valued property that refers to another object within the same JSON
"file"/"stream".

Example:

{

  "$id" : "thisobj",
  "name": "Simple object",
  "date": { "day" : 16, "month": 10, "year": 2006},
  "mycontainer" : { "$idref": "thisobj" }
}

In the above example, "mycontainer" property refers back to the JSON object
that contains "name", "date" and "mycontainer" properties.

* $href

String valyed property that refers to another JSON "file" or "stream" - like
HTML Anchor tag. [Note: this is *not* yet implemented]. Also, "fragment"
identifiers can be mapped to "$id" within the referred JSON URI.

* $class property

"$class" property helps while mapping JSON objects back to language specific
objects - for example, objects of specific Java bean classes. "$class" 
property may be used differently by other language bindings. For example, 
with JavaScript, the value of this property may be treated as a code string 
that will be evaluated to get "prototype" of the object being parsed.

* id for JSON arrays.

Since, we cannot add "$xxx" properties to JSON arrays, we need a slighly different mechanism for hanlding array references. We map an array that will be
referred elsewhere as follows:

 * wrap it with an object. Add "$id" to the object.
 * add a property with a name "$" to the object - the value of this property
   is the array (that will be referred).

Example:

{
  "x": { "$id" : "myarray" , "$": [2, 3, 54654] },
  "y": { "$idref": "myarray" }
}

After deserialization, the object will have two array valued properties
"x" and "y" both pointing to the same array.

