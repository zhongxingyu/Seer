 package org.softlang.company.features;
 
 import org.softlang.company.features.parsing.Recognizer;
 
 import java.io.IOException;
 
/**
 * For clarification, this is precise copy and
 * only shows the idea of Unparsing (noop copy).
 */
 public class Parsing {
 
     public static Recognizer recognizeCompany(String in) throws IOException {
         Recognizer recognizer = new Recognizer(in);
         return recognizer;
     }
 
 }
