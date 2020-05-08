 package com.suhorukov.analyzer;
 
 public class WordAnalyzer {
     public static void main(String[] args){
         Parser p = new Parser();
         p.parse(args[0]);
         p.sortList();
         p.dumpToFile();
     }
 }
