 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 //
 // Unexpected character error.
 
 package me.pavlina.alco.compiler.errors;
 
 import me.pavlina.alco.compiler.ErrorAnnotator;
 
 /**
  * Unexpected-character error. This is used when the lexer cannot figure out
  * what a character represents (no regular expressions match).
  */
 public class UnexpectedChar extends CError
 {
 
     /**
      * Initialise an UnexpectedChar error.
      * @param ch Character which caused the error
      * @param line Line of code which triggered the error (zero-based)
      * @param col Column where the character was found
      * @param annotator ErrorAnnotator to format the code line
      */
     public UnexpectedChar (char ch, int line, int col, ErrorAnnotator annotator)
     {
         super ();
 
         message = "unexpected '" + Character.toString (ch) + "'";
         this.line = line;
         this.col = col;
         start = 0;
         stop = 0;
         this.annotator = annotator;
     }
 
     /**
      * Initialise an UnexpectedChar with a reason.
      * @param ch Character which caused the error
      * @param line Line of code which triggered the error (zero-based)
      * @param col Column where the character was found
      * @param annotator ErrorAnnotator to format the code line
      * @param msg Message suffix */
     public UnexpectedChar (char ch, int line, int col, ErrorAnnotator annotator,
                            String msg) {
         super ();
        message = "unexpected '" + Character.toString (ch) + "' " + msg;
         this.line = line;
         this.col = col;
         start = 0;
         stop = 0;
         this.annotator = annotator;
     }
 }
