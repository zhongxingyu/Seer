 /** 
  Project "com.quui.chat.core" (C) 2006 Fabian Steeg
 
  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package com.quui.chat;
 
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.quui.chat.mind.wn.WNLookup;
 
 /**
  * A simple preprocessor
  * 
  * @author Fabian Steeg (fsteeg)
  */
 public class Preprocessor {
     private Vector<String> stopwords;
 
     private boolean isWordNetEnabled;
 
     /**
      * @param isWordnetEnabled
      *            If true this preprocessor uses wordnet for stemming
      * @param stopwords
      *            The words to ignore, will be filtered while preprocessing
      */
     public Preprocessor(boolean isWordnetEnabled, Vector<String> stopwords) {
         this.isWordNetEnabled = isWordnetEnabled;
         this.stopwords = stopwords;
     }
 
     /**
      * Tokenizes the user input, then every word thats not in the stopwords-list
      * is stemmed and these are returned.
      * 
      * @param s
      *            The input to answer to.
      * @return Returns those words (stemmed) that will be processed
      */
     public Vector<String> preProcess(String s) {
         if (s == null) {
             throw new NullPointerException("Word to process is null.");
         }
         StringTokenizer tokenizer = new StringTokenizer(s,
                 " .!?,;:^\"�$%&/\\()[]#'+*<>|\t-");
         String[] tokens = new String[tokenizer.countTokens()];
         int i = 0;
         while (tokenizer.hasMoreElements()) {
             tokens[i] = tokenizer.nextToken();
             i++;
         }
         Vector<String> result = new Vector<String>();
         for (String element : tokens) {
             String firstStem = element;
             if (this.isWordNetEnabled) {
                 firstStem = WNLookup.getStaticStem(element);
                 if (firstStem.equals(""))
                     firstStem = element;
             }
             if (!this.stopwords.contains(firstStem)
                     && firstStem.trim().length() > 1) {
                 if (firstStem.trim().equals(""))
                     throw new NullPointerException("Empty token!");
                 result.add(firstStem);
             }
         }
         return result;
     }
 
     /**
      * Cleans all occurences and almost-occurences (Levenshtein Distance) of
      * nick in message.
      * 
      * @param message
      *            The message to clean
      * @param nick
      *            The nick to clean from the message
      * @return The cleaned message
      */
     static public String clean(String message, String nick) {
 
        String[] toks = message.toLowerCase().split("[^\\p{L}?!']");
         String[] nickToks = nick.toLowerCase().split("[^\\p{L}]");
         for (int j = 0; j < toks.length; j++) {
             for (int i = 0; i < nickToks.length; i++) {
                 int dist = StringUtils.getLevenshteinDistance(toks[j],
                         nickToks[i]);
                 if (dist < 2 && toks[j].length() > 3
                         && nickToks[i].length() > 3) {// ||
                     Log.logger.debug("Cutting out, L-Dist zw. " + toks[j]
                             + " und " + nickToks[i] + " ist: " + dist);
                     toks[j] = "";
                 }
             }
 
         }
         message = "";
         for (int j = 0; j < toks.length; j++) {
             message = (message + toks[j].trim()).trim() + " ";
         }
        return message;
     }
 
     /**
      * @param message
      *            the string to check for non ascii
      * @return true is string contains non-ascii, else false
      */
     static public boolean containsNonAscii(String message) {
         char[] m = message.toCharArray();
         for (int i = 0; i < m.length; i++) {
             if ((int) m[i] < 0 || (int) m[i] > 127)
                 return true;
         }
         return false;
     }
 }
