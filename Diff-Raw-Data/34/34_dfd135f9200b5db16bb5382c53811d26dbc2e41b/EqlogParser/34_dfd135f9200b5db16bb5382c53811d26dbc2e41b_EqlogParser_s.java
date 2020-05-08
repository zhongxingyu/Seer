 /*
  * EqlogParser.java
  * Copyright (C) 2001 Klaus Rennecke, all rights reserved.
  * Created on 28. April 2001, 05:36
  */
 
 package net.sourceforge.fraglets.yaelp;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.StringTokenizer;
 import java.util.zip.GZIPInputStream;
 
 /** This class implements a simple scanner for log files.
  *
  * <p>This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * <p>This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * <p>You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  * @author  kre
 * @version $Revision: 1.4 $
  */
 public class EqlogParser {
     public static final String PREFIX = "[Sun Apr 01 16:38:57 2001] ";
     
     public static final int TIMESTAMP_START = 1;
     public static final int TIMESTAMP_END = PREFIX.length()-2;
     public static final int LINE_START = PREFIX.length();
     
     protected BufferedReader in;
     
     /** Creates new Parser */
     public EqlogParser(BufferedReader in)
     {
         this.in = in;
     }
     
     public synchronized Line readLine()
         throws IOException
     {
         String line = in.readLine();
         if (line == null) {
             return null;
         } else  try {
             String timestamp = line.substring(TIMESTAMP_START, TIMESTAMP_END);
             char rest[] = line.substring(LINE_START).toCharArray();
             return new Line(timestamp, rest);
         } catch (StringIndexOutOfBoundsException ex) {
             throw new IOException("invalid log file format");
         }
     }
     
     public static int parseFile(String name, Recognizer recognizer)
         throws IOException
     {
         return parseFile(new File(name), recognizer);
     }
     
     public static int parseFile(File file, Recognizer recognizer)
         throws IOException
     {
         BufferedReader in;
         if (file.getName().endsWith(".gz")) {
             System.out.println("reading compressed "+file+"...");
             in = new BufferedReader(new InputStreamReader(new GZIPInputStream
                 (new FileInputStream(file))));
         } else {
             System.out.println("reading text "+file+"...");
             in = new BufferedReader(new FileReader(file));
         }
         EqlogParser parser = new EqlogParser(in);
         Line line;
         int n = 0;
        do {
            line = parser.readLine();
            if (line == null) {
                // end
            } else if (recognizer.isWhoLine(line)) {
                recognizer.parseWhoLine(line);
                n++;
            } else {
                n++;
            }
        } while (line != null);
         return n;
     }
     
     public static void main(String args[])
     {
         try {
             int opt = 0;
             
             // Parse all log files
             Recognizer recognizer = new Recognizer();
             while (opt < args.length) {
                 parseFile(args[opt++], recognizer);
             }
             
             // Print avatars in name order
             Avatar avatars[] = recognizer.getAvatars();
             Comparator nameOrder = new Comparator() {
                 public boolean equals(Object other) {
                     return other != null &&
                     other.getClass() == getClass();
                 }
                 public int compare(Object a, Object b) {
                     return ((Avatar)a).getName().toString()
                     .compareTo(((Avatar)b).getName().toString());
                 }
             };
             Arrays.sort(avatars, nameOrder);
             for (int i = 0; i < avatars.length; i++) {
                 System.out.println(avatars[i]);
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 }
