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
        boolean foundStart = false;
         for ( int i=0; i<lines.length; i++) {
             String line = lines[i].trim().toLowerCase();
            if ( line.contains(startWord) && foundStart == false) { 
                skip = !skip;
                foundStart = true;
            } else if ( line.contains(stopWord) ) {
                 skip = !skip;
             } else {
                 if ( skip == false ) {
                    System.out.println("KEEP: "+line);
                     finalContent.append(lines[i]).append("\n");
                 }
             }
         }
         
         return finalContent.toString().trim();
     }
 }
