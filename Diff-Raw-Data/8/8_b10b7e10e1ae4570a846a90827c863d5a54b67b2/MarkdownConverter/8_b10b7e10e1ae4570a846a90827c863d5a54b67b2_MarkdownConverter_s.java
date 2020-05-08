 /*
  * MarkdownConverter.java
  *
  * Copyright (c) 2008-2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package quill.converter;
 
 import java.io.FileNotFoundException;
 import java.util.HashSet;
 
 import org.gnome.gdk.Pixbuf;
 import org.gnome.gtk.Gtk;
 import org.gnome.gtk.IconSize;
 import org.gnome.gtk.Label;
 import org.gnome.gtk.Stock;
 import org.gnome.gtk.TextBuffer;
 import org.gnome.gtk.TextIter;
 import org.gnome.gtk.TextTag;
 
 import static org.gnome.gtk.TextBuffer.OBJECT_REPLACEMENT_CHARACTER;
 
 public class MarkdownConverter
 {
     /*
      * These are placeholders to allow the class to compiler. As it stands,
      * this is all legacy, broken, and to be replaced by operating over the
      * TextChain.
      */
     private static final TextTag italics = null;
 
     private static final TextTag bold = null;
 
     private static final TextTag mono = null;
 
     private static final TextTag hidden = null;
 
     public static String extractToFile(TextBuffer buffer) {
         StringBuilder str;
         TextIter pointer;
        char ch;
 
         str = new StringBuilder();
 
         pointer = buffer.getIterStart();
 
         while (true) {
             /*
              * Close markup for formats that are now ending
              */
 
             if (pointer.endsTag(mono)) {
                 str.append('`');
             }
             if (pointer.endsTag(bold)) {
                 str.append("**");
             }
             if (pointer.endsTag(italics)) {
                 str.append('_');
             }
 
             if (pointer.isEnd()) {
                 str.append('\n');
                 break;
             }
 
             /*
              * While a single newline terminates a paragraph in Markdown, for
              * aesthetic purposes we'll double tap it to create a blank line
              * in the output textfile.
              */
 
             if (pointer.endsLine()) {
                 str.append('\n');
             }
 
             /*
              * Open markup that represents formats that are now beginning.
              */
 
             if (pointer.beginsTag(italics)) {
                 str.append('_');
             }
             if (pointer.beginsTag(bold)) {
                 str.append("**");
             }
             if (pointer.beginsTag(mono)) {
                 str.append('`');
             }
 
             /*
              * Finally, add the TextBuffer's content at this position, and
              * move to the next character... unless it's something special
              */
 
             ch = pointer.getChar();
 
             if (ch == OBJECT_REPLACEMENT_CHARACTER) {
                 str.append("![");
                 str.append("text");
                 str.append("](");
 
                 while (pointer.forwardChar()) {
                     if (pointer.hasTag(hidden)) {
                         ch = pointer.getChar();
                        str.append(ch);
                     } else {
                         break;
                     }
                 }
 
                 str.append(')');
                 continue;
             } else {
                str.append(ch);
             }
 
             pointer.forwardChar();
         }
 
         return str.toString();
     }
 
     private static HashSet<TextTag> tags;
 
     static TextBuffer loadFile(String contents) {
         final TextBuffer buffer;
         int i, j;
         TextIter pointer;
         char ch;
         String alt, src;
         Pixbuf graphic;
 
         buffer = new TextBuffer();
 
         if (contents.length() == 0) {
             return buffer;
         }
 
         i = 0;
         tags = new HashSet<TextTag>(4);
         pointer = buffer.getIterStart();
 
         while (i < contents.length()) {
             ch = contents.charAt(i);
             i++;
 
             if (ch == '_') {
                 toggleFormat(italics);
                 continue;
             }
             if (ch == '*') {
                 if (contents.charAt(i) == '*') {
                     toggleFormat(bold);
                 }
                 continue;
             }
             if (ch == '`') {
                 toggleFormat(mono);
                 continue;
             }
             if (ch == '\n') {
                 tags.clear();
 
                 if (i == contents.length()) {
                     break;
                 }
             }
             if (ch == '!') {
                 ch = contents.charAt(i);
                 if (ch != '[') {
                     continue;
                 }
                 i++;
 
                 j = contents.indexOf(']', i);
                 alt = contents.substring(i, j);
                 i += alt.length() + 1;
 
                 ch = contents.charAt(i);
                 if (ch != '(') {
                     continue;
                 }
                 i++;
 
                 j = contents.indexOf(')', i);
                 src = contents.substring(i, j);
                 i += src.length() + 1;
 
                 try {
                     graphic = new Pixbuf(src);
                 } catch (FileNotFoundException fnfe) {
                     graphic = Gtk.renderIcon(new Label(""), Stock.MISSING_IMAGE, IconSize.BUTTON);
                 }
 
                 buffer.insert(pointer, graphic);
                 buffer.insert(pointer, src, hidden);
                 continue;
             }
 
             buffer.insert(pointer, String.valueOf(ch), tags);
 
             if (ch == '\n') {
                 if (contents.charAt(i) == '\n') {
                     i++;
                 }
             }
         }
 
         return buffer;
     }
 
     private static void toggleFormat(TextTag format) {
         if (tags.contains(format)) {
             tags.remove(format);
         } else {
             tags.add(format);
         }
     }
 }
