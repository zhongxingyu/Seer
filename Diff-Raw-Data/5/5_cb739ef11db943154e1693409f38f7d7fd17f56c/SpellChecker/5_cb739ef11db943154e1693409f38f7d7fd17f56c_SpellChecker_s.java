 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2010 Operational Dynamics Consulting, Pty Ltd
  *
  * The code in this file, and the program it is a part of, is made available
  * to you by its authors as open source software: you can redistribute it
  * and/or modify it under the terms of the GNU General Public License version
  * 2 ("GPL") as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
  *
  * You should have received a copy of the GPL along with this program. If not,
  * see http://www.gnu.org/licenses/. The authors of this program may be
  * contacted through http://research.operationaldynamics.com/projects/quill/.
  */
 package quill.ui;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.TreeSet;
 
 import org.freedesktop.bindings.Environment;
 import org.freedesktop.enchant.Dictionary;
 import org.freedesktop.enchant.Enchant;
 
 import parchment.manuscript.Manuscript;
 
 import static java.lang.System.arraycopy;
 
 /**
  * Check the spelling of a word against <i>two</i> Dictionaries: the spell
  * checking dictionary specified by the Enchant language tag in the Manuscript
  * Metadata, and the document's personal word list.
  * 
  * @author Andrew Cowie
  */
 /*
  * Effectively, this implements Dictionary
  */
 class SpellChecker
 {
     private final Dictionary dict;
 
     private Dictionary list;
 
     /**
      * The path of the document word list.
      */
     private final String filename;
 
     private File target;
 
     private File tmp;
 
     SpellChecker(final Manuscript manuscript, final String lang) {
         if (Enchant.existsDictionary(lang)) {
             dict = Enchant.requestDictionary(lang);
         } else {
             dict = null;
         }
 
         filename = manuscript.getDirectory() + "/" + manuscript.getBasename() + ".dic";
 
         target = new File(filename);
         createTemporaryList();
     }
 
     protected void finalize() {
         tmp.delete();
     }
 
     private static int counter;
 
     static {
         counter = 0;
     }
 
     private void createTemporaryList() {
         final int pid;
         FileReader reader;
         FileWriter writer;
         BufferedReader in;
         BufferedWriter out;
         String line;
        boolean first;
 
         pid = Environment.getProcessID();
         counter++;
 
         tmp = new File("/tmp/quill-" + pid + "-" + counter + ".tmp");
         tmp.deleteOnExit();
 
         if (!target.exists()) {
             return;
         }
 
         try {
             reader = new FileReader(target);
             in = new BufferedReader(reader);
 
             writer = new FileWriter(tmp, false);
             out = new BufferedWriter(writer);
 
             /*
              * Note that readLine() trims, and, we output a leading (not
              * trailing) \n to observe Enchant's conventions.
              */
 
             first = false;
 
             while (true) {
                 line = in.readLine();
 
                 if (line == null) {
                     break;
                 }
                 if (line.equals("")) {
                     continue;
                 }
                 if (line.charAt(0) == '#') {
                     continue;
                 }
 
                if (!first) {
                     out.write('\n');
                 } else {
                     first = true;
                 }
 
                 out.write(line);
             }
 
             in.close();
             out.close();
 
             list = Enchant.requestPersonalWordList(tmp.getAbsolutePath());
         } catch (IOException ioe) {
             throw new AssertionError("Failed to prepare word list");
         }
     }
 
     boolean check(String word) {
         boolean result;
 
         result = false;
 
         if (dict != null) {
             result = dict.check(word);
         }
 
         if (result) {
             return true;
         }
 
         if (list != null) {
             result = list.check(word);
         }
 
         return result;
     }
 
     /**
      * Add the given word to the user's word list that goes along with the
      * system dictionary. Turns out we're not using this because we don't want
      * people adding words to their local system only personal word lists.
      * 
      * @deprecated
      */
     void addToSystem(final String word) {
         if (dict != null) {
             dict.add(word);
         }
     }
 
     /**
      * Add the given word to the document's private word list. If there is no
      * such list, create it, putting a comment (sic) at the beginning.
      */
     /*
      * This must be staggeringly expensive, so TODO any better ideas? We want
      * a newline terminated file, and no bloody blanks.
      */
     void addToDocument(final String word) {
 
         if (list == null) {
             try {
                 tmp.createNewFile();
                 list = Enchant.requestPersonalWordList(tmp.getAbsolutePath());
             } catch (IOException fnfe) {
                 throw new AssertionError("Can't open document word list");
             }
         }
 
         list.add(word);
     }
 
     boolean isSystemValid() {
         return (dict != null);
     }
 
     boolean isDocumentValid() {
         return (list != null);
     }
 
     /**
      * Enchant is extraordinarily annoying in that it appends "\nword" instead
      * of doing "word\n" which means there is never a newline at end of file.
      * It also doesn't bother to sort the list.
      */
     /*
      * We therefore read in the entire list, remove the leading newline, sort,
      * then append a newline.
      */
     void saveDocumentList() {
         FileReader reader;
         BufferedReader in;
         FileWriter writer;
         BufferedWriter out;
         String line;
         final TreeSet<String> sorted;
 
         if (!tmp.exists()) {
             return;
         }
         try {
             sorted = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
 
             reader = new FileReader(tmp);
             in = new BufferedReader(reader);
 
             line = in.readLine();
             while (line != null) {
                 sorted.add(line);
                 line = in.readLine();
             }
 
             in.close();
 
             writer = new FileWriter(target, false);
             out = new BufferedWriter(writer);
 
             out.write("# Document word list\n");
 
             for (String word : sorted) {
                 if (word.equals("")) {
                     continue;
                 }
                 out.write(word);
                 out.write('\n');
             }
 
             out.close();
         } catch (IOException ioe) {
             throw new AssertionError("Can't save document word list");
         }
     }
 
     /**
      * Puts words from the document's local list before dictionary suggests.
      * Is that best?
      * 
      * A String[] with suggestions, on null if there aren't any.
      */
     String[] suggest(final String word) {
         String[] one, two, result;
         int a, b;
 
         one = null;
         two = null;
 
         if (list != null) {
             one = list.suggest(word);
         }
 
         if (dict != null) {
             two = dict.suggest(word);
         }
 
         if (one != null) {
             a = one.length;
         } else {
             a = 0;
         }
         if (two != null) {
             b = two.length;
         } else {
             b = 0;
         }
 
         if ((a == 0) && (b == 0)) {
             return null;
         }
 
         result = new String[a + b];
 
         if (a > 0) {
             arraycopy(one, 0, result, 0, a);
         }
         if (b > 0) {
             arraycopy(two, 0, result, a, b);
         }
 
         return result;
     }
 }
