 package org.nines.cleaner;
 
 import org.nines.ICustomCleaner;
 
 public class LocEphemeraCleaner implements ICustomCleaner {
 
     public String clean(String archiveName, String content) {
         
         if ( archiveName.equals("locEphemera")) {
             return stripJunk(content, "<hr>", "Information about SGML version of this document.");
         }
         return content;
     }
     
     private String stripJunk(String content, String startWord, String stopWord) {
         String[] lines = content.split("\n");
         StringBuffer finalContent = new StringBuffer();
         boolean skip = true;
         for ( int i=0; i<lines.length; i++) {
             String line = lines[i].trim().toLowerCase();
            if ( line.contains(startWord) || line.contains(stopWord) ) {
                 skip = !skip;
             } else {
                 if ( skip == false ) {
                     finalContent.append(lines[i]).append("\n");
                 }
             }
         }
         
         return finalContent.toString().trim();
     }
 }
