 package net.dthg.taric;
 
 import java.util.Formatter;
 
 public class ChapterFetch extends Fetcher {
     
     private String date;
     private int chapter;
     
     public ChapterFetch( String date, int chapter ) {
         this.date = date;
         this.chapter = chapter;
     }
 
     protected String buildUrl() {
         Formatter format = new Formatter();
         StringBuilder sb = new StringBuilder();
         sb.append( BASE_URL )
           .append( '_' ).append( BASE_LANG )
           .append( '_' ).append( date )
           .append( '_' ).append( format.format( "%02d", chapter ) )
          .append( '_' ).append( BASE_LANG )
           .append( ".js" );
         return sb.toString();
     }
 }
