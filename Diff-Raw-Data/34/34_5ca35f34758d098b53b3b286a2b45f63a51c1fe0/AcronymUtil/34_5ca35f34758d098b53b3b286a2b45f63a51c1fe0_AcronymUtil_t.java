 package org.cujau.utils;
 
 import java.math.BigDecimal;
 
 public class AcronymUtil {
 
     public static String formatLongAcronym( Object val ) {
         if ( val.getClass() != Long.class && val.getClass() != Integer.class ) {
             return "NA";
         }
         Long mc;
         if ( val.getClass() == Integer.class ) {
             mc = ((Integer)val).longValue();
         } else {
             mc = (Long) val;
         }
         if ( mc == Long.MIN_VALUE ) {
             return "NA";
         } else if ( mc >= 1000000000000l ) {
             return String.format( "%.2fT", ( mc / 1000000000000.0 ) );
         } else if ( mc >= 1000000000 ) {
             return String.format( "%.2fB", ( mc / 1000000000.0 ) );
         } else if ( mc >= 1000000 ) {
             return String.format( "%.2fM", ( mc / 1000000.0 ) );
         } else {
             return String.valueOf( mc );
         }
     }
 
     public static long parseLongAcronym( Object val ) {
         if ( val.getClass() == Long.class ) {
             return (Long) val;
         }
         if ( val.getClass() == Integer.class ) {
             return ( (Integer) val ).longValue();
         }
         if ( val.getClass() != String.class ) {
             return 0;
         }
         String str = (String) val;
         if ( str.equals( "NA" ) ) {
             return Long.MIN_VALUE;
         }
         str = str.replace( ",", "" );
        if ( str.length() == 0 ) {
            return Long.MIN_VALUE;
        }
         char end = str.charAt( str.length() - 1 );
         long factor = 1;
         switch ( end ) {
         case 'M':
             factor = 1000000;
             str = str.substring( 0, str.length() - 1 );
             break;
         case 'B':
             factor = 1000000000;
             str = str.substring( 0, str.length() - 1 );
             break;
         case 'T':
             factor = 1000000000000l;
             str = str.substring( 0, str.length() - 1 );
             break;
         }
        try {
            BigDecimal ret = new BigDecimal( str );
            return ret.multiply( new BigDecimal( factor ) ).longValue();
        } catch ( NumberFormatException e ) {
            return Long.MIN_VALUE;
        }
     }
 }
