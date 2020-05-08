 /* Copyright 2011 Anthony Cassidy
  * Usage of the works is permitted provided that this instrument is retained with the works, so that any entity that uses the works is notified of this instrument.
  *  DISCLAIMER: THE WORKS ARE WITHOUT WARRANTY.
  */
 package com.github.a2g.core;
 
 
 public class SentenceUnit {
 
     private String displayName;
     private String textualId;
     private int code;
 
     public SentenceUnit(String displayName, String textualId, int code) {
         this.displayName = displayName;
         this.textualId = textualId;
         this.code = code;
     }
 
     public int getLength() {
         return this.displayName.length();
     }
 
     public SentenceUnit getDisplayNameAfterDivider() {
         int i = this.getDisplayName().lastIndexOf(
                 "|");
 
         if (i != -1) {
             return new SentenceUnit(
                     this.displayName.substring(
                            i),
                             this.textualId,
                             this.code);
         }
         return  new SentenceUnit(
                 this.displayName, this.textualId,
                 this.code);
     	
     }
 
     public SentenceUnit getDisplayNameBeforeDivider() {
         int i = this.getDisplayName().lastIndexOf(
                 "|");
 
         if (i != -1) {
             return new SentenceUnit(
                     this.displayName.substring(
                            0, i + 1),
                             this.textualId,
                             this.code);
         }
         return  new SentenceUnit(
                 this.displayName, this.textualId,
                 this.code);
     }
 
     public final String getDisplayName() {
         return this.displayName;
     }
 
     public final String getTextualId() {
         return this.textualId;
     }
 
     public final int getCode() {
         return this.code;
     }
 }
 
 
 ;
 
