 /**
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Wiki.
  *
  * The Initial Developer of the Original Code is Technology Concepts
  * and Design, Inc.
  * Copyright (C) 2000 Technology Concepts and Design, Inc.  All
  * Rights Reserved.
  *
  * Contributor(s): Lane Sharman (OpenDoors Software)
  *                 Justin Wells (Semiotek Inc.)
  *                 Eric B. Ridge (Technology Concepts and Design, Inc.)
  *
  * Alternatively, the contents of this file may be used under the
  * terms of the GNU General Public License Version 2 or later (the
  * "GPL"), in which case the provisions of the GPL are applicable
  * instead of those above.  If you wish to allow use of your
  * version of this file only under the terms of the GPL and not to
  * allow others to use your version of this file under the MPL,
  * indicate your decision by deleting the provisions above and
  * replace them with the notice and other provisions required by
  * the GPL.  If you do not delete the provisions above, a recipient
  * may use your version of this file under either the MPL or the
  * GPL.
  *
  *
  * This product includes sofware developed by OpenDoors Software.
  *
  * This product includes software developed by Justin Wells and Semiotek Inc.
  * for use in the WebMacro ServletFramework (http://www.webmacro.org).
  */
 package org.tcdi.opensource.wiki;
 
 import java.security.*;
 import java.util.*;
import java.io.UnsupportedEncodingException;
 
 /**
  * A simple singleton to provide easy access to some
  * common, and mundane, WikiTasks.
  *
  * @author Eric B. Ridge
  */
 public class WikiUtil {
     public static final int MAX_RANDOM_PAGES = 5;
     private static WikiUtil _instance = new WikiUtil ();
     
     /** return the only instance of the WikiUtil class */
     public static WikiUtil getInstance() {
         return _instance;
     }
 
     /** we're a singleton.  can't create us! */
     private WikiUtil () {
         
     }
     
     /**
      * Using the provided WikiSystem, guessWikiTitle() will find
      * the fist page that most closely matches a wiki-fied version of
      * the provided phrase by linearly searching pages in the WikiSystem,
      * if the phrase isn't already an existing page.
      *
      * @param phrase the phrase to be guessed as a WikiTitle
      * @param wiki the WikiSystem to use for linear searching
      */
     public static String guessWikiTitle(String phrase, WikiSystem wiki) {
         if (wiki.containsPage(phrase))
             return phrase;  // instant match!
         
         String title = formatAsWikiTitle(phrase).toLowerCase();
         Enumeration enum = wiki.getPageNames();
         while (enum.hasMoreElements()) {  // find first page that starts with title
             String pageName = (String) enum.nextElement();
             if (pageName.toLowerCase().startsWith(title) && pageName.indexOf('.') == -1)
                 return pageName;
         }
         
         return phrase;
     }
     
     /**
      * takes provided phrase and turns it into a WikiTerm by
      * .toUpperCase() first letter, and first letter of each word.
      * Strips all non-alpha characters
      *
      * @param phrase a phrase to convert to a WikiTerm
      * @return a wiki-fied version of phrase
      */
     public static String formatAsWikiTitle(String phrase) {
         if (phrase == null)
             return null;
         
         StringBuffer sb = new StringBuffer();
         
         char[] chars = phrase.toCharArray();
         for (int x=0; x<chars.length; x++) {
             if (x == 0 || chars[x-1] == ' ')
                 chars[x] = Character.toUpperCase(chars[x]);
             
             if (Character.isLetter(chars[x]))
                 sb.append(chars[x]);
         }
         
         return sb.toString();
     }
     
     /**
      * takes a WikiTerm and puts spaces between each term.
      * example:  WikiTerm --> Wiki Term
      *
      * @param word a propertly formed WikiTerm
      * @return a spaced out version of word
      */
     public static String unformatWikiTitle(String word) {
         if (word == null)
             return "";
         char[] chars = word.toCharArray();
         StringBuffer sb = new StringBuffer(chars.length);
         int size = word.length();
         for (int x=0; x<size; x++) {
             char ch = chars[x];
             if (x>0 && !Character.isLowerCase(ch))
                 sb.append (" ");
             sb.append (ch);
         }
         
         
         return sb.toString();
     }
     
     
     /**
      * returns a String array of 5 random wiki page names
      */
     public static String[] getRandomPages(WikiSystem wiki) {
         
         java.util.Random r = new Random();
         String[] pageNames = wiki.getCurrentPageNames();
         String[] randomPages = new String[MAX_RANDOM_PAGES];
         for (int x=0; x<5; x++)
             randomPages[x] = pageNames[r.nextInt(pageNames.length)];
 
         return randomPages;
     }
     
     
     
     /**
      * @param str String to generate an MD5 with
      * @return a cookie-safe MD5 of provided string
      */
     public static String getMD5(String str) {
         try {
             MessageDigest md = MessageDigest.getInstance("MD5");
             md.update(str.getBytes());
             byte[] bytes = md.digest();
             return new String(makeCookieSafe(bytes));
         } catch (Exception e) {
             throw new RuntimeException (e.toString());
         }
     }
     
     /**
      * takes a byte[] and converts it to a char[]
      * that is safe to store as a cookie.
      */
    private static char[] makeCookieSafe(byte[] bytes) throws UnsupportedEncodingException {
        char[] chars = new String(bytes, "ISO-8859-1").toCharArray();
         int BOT = 48;
         int TOP = 125;
         
         // I'm sure there's a better way to make sure
         // all characters are between BOT and TOP
         // but I'm too tired to think of it.
         for (int x=0; x<chars.length; x++) {
             while (chars[x] < BOT || chars[x] > TOP) {
                 if (chars[x] < BOT)
                     chars[x] += BOT;
                 else if (chars[x] > TOP)
                     chars[x] -= TOP;
             }
             
             if (chars[x] == ';') {  // never allow a semi-colon as a value: change to BOT character
                 // this is invalid inside a cookie
                 chars[x] = (char) BOT;
             }
         }
         return chars;
     }
 }
