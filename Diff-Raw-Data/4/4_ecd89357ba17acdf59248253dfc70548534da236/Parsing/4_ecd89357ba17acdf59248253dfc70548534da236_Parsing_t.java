 package org.softlang.company.features;
 
 import org.softlang.company.features.parsing.Recognizer;
 
 import java.io.IOException;
 
 public class Parsing {
 
     public static Recognizer recognizeCompany(String in) throws IOException {
         Recognizer recognizer = new Recognizer(in);
         return recognizer;
     }
 
 }
