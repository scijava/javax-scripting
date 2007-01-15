package com.sun.svg.script;

public class Main extends org.apache.batik.apps.svgbrowser.Main {
   public static void main(String[] args) {
       new Main(args);
   }

   public Main(String[] args) {
       super(args);
   }

   public boolean canLoadScriptType(String scriptType){
       return true;
   }
}