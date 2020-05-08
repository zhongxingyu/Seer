 
 /*
  *  Copyright 2011 richard.hierlmeier@web.de.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 import java.io.*;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 /**
  * Utility for searching class files in a java classpath
  */
 public class JWhich {
 
     private final static Logger log = Logger.getLogger(JWhich.class.getName());
     private final static Formatter CLI_FORMATTER = new Formatter() {
 
         @Override
         public String format(LogRecord record) {
             return this.formatMessage(record) + System.getProperty("line.separator");
         }
     };
 
     public static void main(String[] args) {
 
         ConsoleHandler h = new ConsoleHandler();
         h.setFormatter(CLI_FORMATTER);
         h.setLevel(Level.INFO);
 
         log.addHandler(h);
         log.setUseParentHandlers(false);
 
         int regExpFlags = 0;
 
         Queue<String> argQueue = new LinkedList<String>(Arrays.asList(args));
         String arg;
 
         while (!argQueue.isEmpty() && argQueue.peek().startsWith("-")) {
             arg = argQueue.poll();
             if ("-help".equals(arg) || "-h".equals(arg)) {
                 printUsage();
             } else if ("-v".equals(arg) || "--verbose".equals(arg)) {
                 log.setLevel(Level.FINE);
             } else if ("-i".equals(arg) || "--ignore-case".equals(arg)) {
                 log.setLevel(Level.FINE);
                 regExpFlags |= Pattern.CASE_INSENSITIVE;
             } else {
                 System.err.println("Error: Unknown option " + arg);
             }
         }
 
         if (argQueue.isEmpty()) {
             System.err.println("Error: Invalid number of argument (try -help)");
             System.exit(1);
         }
 
         String className = argQueue.poll();
 
         StringBuilder buf = new StringBuilder();
         buf.append(".*");
         for (char c : className.toCharArray()) {
             switch (c) {
                 case '*':
                     buf.append(".*");
                     break;
                 case '?':
                     buf.append(".");
                     break;
                 case '.':
                     buf.append("[/\\.]");
                     break;
                 case '/':
                     buf.append("[/\\.]");
                     break;
                 default:
                     buf.append(c);
             }
         }
         buf.append(".*");
 
         Pattern pattern = Pattern.compile(buf.toString(), regExpFlags);
 
         if (argQueue.isEmpty()) {
             log.log(Level.FINE, "No files specified, searching in working directory");
             search(pattern, new File("."));
         } else {
             while (!argQueue.isEmpty()) {
                 File f = new File(argQueue.poll());
                 search(pattern, f);
             }
         }
     }
 
     private static void search(Pattern pattern, File file) {
         if (!file.exists()) {
             log.log(Level.WARNING, "Ingoring non existent file {0}", file);
         }
         if (file.isDirectory()) {
             searchInDir(pattern, file, file);
         } else if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
             log.log(Level.FINE, "Searching ''{0}'' in {1}", new Object[]{pattern, file});
             searchInArchive(pattern, file);
         }
     }
 
     private static void searchInArchive(Pattern pattern, File file) {
         try {
             ZipFile zipFile = new ZipFile(file);
             Enumeration<? extends ZipEntry> en = zipFile.entries();
             while (en.hasMoreElements()) {
                 ZipEntry ze = en.nextElement();
                 if (ze.isDirectory()) {
                     continue;
                 }
                 log.log(Level.FINE, "Entry: {0}", ze.getName());
                 if (pattern.matcher(ze.getName()).matches()) {
                     System.out.print(file);
                     System.out.print('\t');
                     System.out.println(ze.getName());
                 }
             }
         } catch (Exception ex) {
             log.log(Level.WARNING, "I/O error " + file + ": " + ex.getLocalizedMessage(), ex);
         }
     }
 
     private static void searchInDir(Pattern pattern, File basedir, File dir) {
         for (File f : dir.listFiles()) {
 
             if (f.getName().endsWith(".jar") || f.getName().endsWith(".zip")) {
                 searchInArchive(pattern, f);
             } else if (f.isDirectory()) {
                 searchInDir(pattern, basedir, f);
             } else {
                 String relPath = f.getAbsolutePath().substring(basedir.getAbsolutePath().length());
                 if (pattern.matcher(relPath).matches()) {
                     System.out.print(basedir);
                     System.out.print('\t');
                     System.out.println(relPath);
                 }
             }
         }
     }
 
     private static void printUsage() {
 
        System.out.println();
        System.out.println("jwhich [options] <class_name_pattern> [<search_path>]");
         System.out.println();
         System.out.println(" Search a class in directories, zip or jar files");
         System.out.println();
         System.out.println("   <class_name_pattern>  pattern for a class name in glob style (* and ? wild cards)");
         System.out.println("   <search_path>          One or more directories, zip or jar files");
         System.out.println("                          if no search path is specified the working directory is used");
         System.out.println();
         System.out.println("Options:");
         System.out.println("");
         System.out.println("   --verbose|-v            verbose mode.");
         System.out.println("   --ignore-case|-i        case insensitive matching of classnames");
         System.out.println("   --help|-h               print this usage");
         System.out.println();
         System.out.println("Examples:");
        System.out.println("   jwhich 'java.lang.*' $JAVA_HOME/lib/rt.jar");
         System.out.println();
         System.exit(0);
     }
 }
