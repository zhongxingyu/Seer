 package com.thesmartpuzzle.ircbot;
 
 import java.text.Normalizer;
 import java.text.Normalizer.Form;
 
 /**
  * @author Tommaso Soru <mommi84 at gmail dot com>
  *
  */
 public class StringUtilities {
 
 	/**
 	 * This method and filters out all the characters that are different from:
 	 * digits (ASCII code 48-57), upper case (65-90), lower-case letters (97-122) and space (32).
 	 * It keeps the accents.
 	 * @param in
 	 * @return
 	 */
     public static String normalize(String in) {
     	in = in.trim();
         String out = "";
         for(int i=0; i<in.length(); i++) {
             char c = Normalizer.normalize(in.charAt(i)+"", Form.NFD).charAt(0);
             if((48 <= c && c <= 57) || (65 <= c && c <= 90) || (97 <= c && c <= 122) || c == 32)
                 out += in.charAt(i);
         }
         return out;
     }
 
 }
