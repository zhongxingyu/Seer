 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms are permitted provided
   that: (1) source distributions retain this entire copyright notice and
   comment; and (2) modifications made to the software are prominently
   mentioned, and a copy of the original software (or a pointer to its
   location) are included. The name of the author may not be used to endorse
   or promote products derived from this software without specific prior
   written permission.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 
   Effectively, this means you can do what you want with the software except
   remove this notice or take advantage of the author's name. If you modify
   the software and redistribute your modified version, you must indicate that
   your version is a modification of the original, and you must provide either
   a pointer to or a copy of the original.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.parser;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Stack;
 import java.util.SimpleTimeZone;
 import java.util.TimeZone;
 
 import java.text.DateFormat;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 
 import org.apache.oro.text.regex.PatternCompiler;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.PatternMatcher;
 import org.apache.oro.text.regex.Perl5Matcher;
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.Pattern;
 
 /**
  * Common utility routines that can be used by all parser implementations.
  *
  * @version <tt>$Revision$</tt>
  */
 public final class ParserUtil
 {
     /*----------------------------------------------------------------------*\
 			     Private Constants
     \*----------------------------------------------------------------------*/
 
     /**
      * A date format for parsing RFC 822-style dates.
      */
     private static DateFormat[] RFC822_DATE_FORMATS;
 
     /**
      * Patterns for the more common W3C date/time formats.
      */
     private static DateFormat[] W3C_DATE_FORMATS;
 
     static
     {
         W3C_DATE_FORMATS = new SimpleDateFormat[]
         {
             new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss"),
             new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm"),
             new SimpleDateFormat ("yyyy-MM-dd"),
             new SimpleDateFormat ("yyyy-MM"),
             new SimpleDateFormat ("yyyy")
         };
 
         RFC822_DATE_FORMATS = new SimpleDateFormat[]
         {
             new SimpleDateFormat ("EEE, d MMM yyyy HH:mm:ss z"),
             new SimpleDateFormat ("EEE, d MMM yyyy HH:mm:ss")
         };
     };
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Constructor.
      */
     private ParserUtil()
     {
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Try to parse a date using as many methods as possible, until one
      * works.
      *
      * @param sDate  the date string
      *
      * @return the corresponding date, or null if not parseable
      */
     public static Date parseDate (String sDate)
     {
         Date result = null;
 
         if ((result = parseRFC822Date (sDate)) == null)
             result = parseW3CDate (sDate);
 
         return result;
     }
 
     /**
      * Parse an RFC 822-style date string.
      *
      * @param sDate  the date string
      *
      * @return the corresponding date, or null if not parseable
      */
     public static Date parseRFC822Date (String sDate)
     {
         return parseDate (sDate, RFC822_DATE_FORMATS, TimeZone.getDefault());
     }
 
     /**
      * Parse a W3C date string. Not comprehensive.
      *
      * @param sDate  the date string
      *
      * @return the corresponding date, or null if not parseable
      */
     public static Date parseW3CDate (String sDate)
     {
         TimeZone       timeZone = TimeZone.getDefault();
         int            tzIndex;
 
         // First, extract the time zone, if present.
 
         if (((tzIndex = sDate.lastIndexOf ('Z')) != -1) ||
             ((tzIndex = sDate.lastIndexOf ('-')) != -1) ||
             ((tzIndex = sDate.lastIndexOf ('+')) != -1))
         {
             timeZone = parseW3CTimeZone (sDate.substring (tzIndex));
         }
 
         // Now, parse the date.
 
         return parseDate (sDate, W3C_DATE_FORMATS, timeZone);
     }
 
     /**
      * Used to process the characters found between two XML elements, this
      * method removes any leading and trailing white space, including
      * newlines. Embedded newlines are mapped to spaces.
      *
      * @param ch      the array of characters to process
      * @param start   the start position in the array
      * @param length  the number of characters to read from the array
      *
      * @return the resulting string
      *
      * @see #normalizeCharacterData(char[],int,int,StringBuffer)
      * @see #normalizeCharacterData(String)
      */
     public static String normalizeCharacterData (char[] ch,
                                                  int    start,
                                                  int    length)
     {
         StringBuffer buf = new StringBuffer();
 
         normalizeCharacterData (ch, start, length, buf);
 
         return buf.toString();
     }
 
     /**
      * Used to process the characters found between two XML elements, this
      * method removes any leading and trailing white space, including
      * newlines. Embedded newlines are mapped to spaces.
      *
      * @param s   the string to process
      *
      * @return the resulting string
      *
      * @see #normalizeCharacterData(char[],int,int,StringBuffer)
      * @see #normalizeCharacterData(String)
      */
     public static String normalizeCharacterData (String s)
     {
         return normalizeCharacterData (s.toCharArray(), 0, s.length());
     }
 
     /**
      * Used to process the characters found between two XML elements, this
      * method removes any leading and trailing white space, including
      * newlines. Embedded newlines are mapped to spaces.
      *
      * @param ch      the array of characters to process
      * @param start   the start position in the array
      * @param length  the number of characters to read from the array
      * @param buf     where to append the resulting characters
      *
      * @see #normalizeCharacterData(char[],int,int)
      * @see #normalizeCharacterData(String)
      */
     public static void normalizeCharacterData (char[]       ch,
                                                int          start,
                                                int          length,
                                                StringBuffer buf)
     {
        boolean lastWasNewline = false;
 
         int end = start + length;
         while (start < end)
         {
             char c = ch[start++];
 
             // Substitute a space for each newline sequence. Be sure to
             // handle all combinations of newline sequences (e.g., \n, \r,
             // \r\n, \n\r). We want just one space to replace a given
             // newline sequence. The easiest solution is to replace all
             // consecutive newline (\n or \r) characters with one space.
             // This has the effect of nuking blank lines, but blank lines
             // are meaningless within character data anyway, at least in
             // this context.
 
            if ((c == '\n') || (c == '\r') || (start == 0))
             {
                 if (! lastWasNewline)
                     buf.append (' ');
                 lastWasNewline = true;
             }
 
             else
             {
                 lastWasNewline = false;
                 if (! Character.isISOControl (c))
                     buf.append (c);
             }
         }
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Parse a date/time using an array of DateFormat objects
      *
      * @param sDate    the date string
      * @param formats  the array of DateFormat objects to try, one by one
      * @param timeZone time zone to use
      *
      * @return the parsed date, or null if not parseable
      */
     private static Date parseDate (String        sDate,
                                    DateFormat[]  formats,
                                    TimeZone      timeZone)
     {
         Date result = null;
 
         for (int i = 0; i < formats.length; i++)
         {
             formats[i].setTimeZone (timeZone);
             try
             {
                 result = formats[i].parse (sDate, new ParsePosition (0));
             }
 
             catch (NumberFormatException ex)
             {
                 result = null;
             }
 
             if (result != null)
                 break;
         }
 
         return result;
     }
 
     /**
      * Parse a string into a W3C time zone. Returns the default time zone
      * if the string doesn't parse.
      *
      * @param tz  the alleged time zone string
      *
      * @return a TimeZone
      */
     private static TimeZone parseW3CTimeZone (String tz)
     {
         TimeZone timeZone = null;
 
         // Do we have a time zone?
 
         switch (tz.charAt (0))
         {
             case 'Z':
                 timeZone = TimeZone.getTimeZone ("GMT");
                 break;
 
             case '-':
             case '+':
                 // +hh:mm -hh:mm
 
                 Perl5Compiler reCompiler = new Perl5Compiler();
                 Perl5Matcher reMatcher = new Perl5Matcher();
                 Pattern re = null;
 
                 try
                 {
                     re = reCompiler.compile ("^[+-][0-9][0-9]:[0-9][0-9]$");
                 }
 
                 catch (MalformedPatternException ex)
                 {
                     // Shouldn't happen.
 
                     throw new IllegalStateException (ex.toString());
                 }
 
                 if (reMatcher.contains (tz, re))
                     timeZone = TimeZone.getTimeZone ("GMT" + tz);
 
                 if (timeZone == null)
                     timeZone = TimeZone.getDefault();
 
                 break;
 
             default:
                 // Whatever it is, it's not a time zone. Assume
                 // local time zone.
 
                 timeZone = TimeZone.getDefault();
                 break;
                         
         }
 
         return timeZone;
     }
 }
