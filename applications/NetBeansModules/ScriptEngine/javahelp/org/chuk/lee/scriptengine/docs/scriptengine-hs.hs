<?xml version="1.0" encoding="ISO-8859-1"?>
<helpset>
  <title>ScriptEngine Help</title>
  <maps>
    <homeID>org.chuk.lee.scriptengine.about</homeID>
    <mapref location="scriptengine-map.xml"/>
  </maps>
  <view mergetype="javax.help.AppendMerge">
    <name>TOC</name>
    <label>Table of Contents</label>
    <type>javax.help.TOCView</type>
    <data>scriptengine-toc.xml</data>
  </view>
  <view mergetype="javax.help.AppendMerge">
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>scriptengine-idx.xml</data>
  </view>
  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch</data>
  </view>
</helpset>
