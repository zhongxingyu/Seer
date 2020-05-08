 /* *****************************************************************************
  * Main.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2006-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.js2doc;
 import org.w3c.dom.Document;
 import org.apache.log4j.*;
 import org.apache.log4j.spi.*;
 import org.apache.log4j.varia.NullAppender;
 import org.openlaszlo.utils.FileUtils;
 import java.io.*;
 import java.util.*;
 
 public class Main {
     
     private static Logger logger;
     
     private static final String[] USAGE = {
         "Usage: js2doc [OPTION]... FILE...",
         "",
         "Options:",
         "-v",
         "  Write progress information to standard output.",
         "--test schema",
         "  Test mode: validate result against schema given",
         "--reprocess",
         "  Reprocess comments",
         "--help",
         "  Prints this message.",
         "",
         "Output options:",
         "--out outputfile",
         "  Output file",
         "--dir outputdir",
         "  Output directory (deprecated).",
     };
 
     /** Extracts reference documentation from the file(s) included.
      *
      * <p>See the usage string or execute <code>js2doc --help</code>
      * to see a list of options.
      *
      * @param args the command line arguments
      */
     public static void main(String args[])
         throws IOException
     {
         js2doc(args, null, null, null);
     }
 
     static final String[] runtimeOptionStrings = { "swf7", "swf8", "swf9", "dhtml", "svg", "j2me" };
     static final Set runtimeOptions = new HashSet(Arrays.asList(runtimeOptionStrings));
     // first element is the alias, subsequent elements are valid runtimes
     // e.g. js1 === dhtml || j2me || svg
     static final String[][] runtimeAliasStrings = { { "as2", "swf7", "swf8", "swf9" },
                                                     { "as3", "swf9" },
                                                     { "js1", "dhtml" /*, "j2me", "svg" */ } };
     static final List runtimeAliases = Arrays.asList(runtimeAliasStrings);                                                 
     static final String[] buildOptionStrings = { "debug", "profile" };
     static final List buildOptions = Arrays.asList(buildOptionStrings);
     
     /** This method implements the behavior described in main
      * but also returns an integer error code.
      */
     public static int js2doc(String args[], String logFile, 
                              String outFileName, String outDir)
         throws IOException
     {
         logger = Logger.getRootLogger();
         // Configure logging
         logger.setLevel(Level.ERROR);
         PatternLayout layout = new PatternLayout("%m%n");
         logger.removeAllAppenders();
         if (logFile == null) {
             logger.addAppender(new ConsoleAppender(layout));
         } else {
             logger.addAppender(new FileAppender(layout, logFile, false));
         }
     
         List files = new Vector();
         boolean testMode = false;
         boolean reprocessOnly = false;
         String libraryID = null;
         String schemaFilename = null; // used for --test mode
         
         for (int i = 0; i < args.length; i++) {
             String arg = args[i].intern();
             if (arg.startsWith("-")) {
                 boolean badform = false;
                 if (arg.equals("-v")) {
                     logger.setLevel(Level.ALL);
                 } else if (arg.equals("--test")) {
                     testMode = true;
                     if (i < args.length) {
                         i++;
                         schemaFilename = args[i];
                     } else
                         badform = true;
                 } else if (arg.equals("--reprocess")) {
                     reprocessOnly = true;
                 } else if (arg.equals("--out")) {
                     if (i < args.length) {
                         i++;
                         outFileName = args[i];
                     } else
                         badform = true;
                 } else if (arg.equals("--dir")) {
                     if (i < args.length) {
                         i++;
                         outDir = args[i];
                     } else
                         badform = true;
                 } else if (arg.equals("--libraryid")) {
                     if (i < args.length) {
                         i++;
                         libraryID = args[i];
                     } else
                         badform = true;
                 } else if (arg.equals("--help")) {
                     for (int j = 0; j < USAGE.length; j++) {
                         System.err.println(USAGE[j]);
                     }
                     return 0;
                 } else {
                     badform = true;
                 }
                 
                 if (badform == true) {
                     System.err.println("Bad arg: " + arg);
                     System.err.println("Usage: js2doc [OPTION]... file...");
                     System.err.println("Try `js2doc --help' for more information.");
                     return 1;
                 }
             } else {
                 files.add(arg);
             }
         }
 
         if (testMode == true && outDir != null) {
             System.err.println("Both --test and --dir given; ignoring --dir.");
         }
         
         for (Iterator iter = files.iterator(); iter.hasNext(); ) {
             String sourceName = (String) iter.next();
             if (testMode) {
                  System.err.println("extracting docs and validating from " + sourceName);
                  boolean result = validateAndCompare(sourceName, schemaFilename, libraryID, runtimeOptions, runtimeAliases, buildOptions);
                  System.err.println("result: " + (result ? "success" : "failure"));
                  if (result == false)
                     return 1;
             } else if (reprocessOnly) {
                 if (files.size() > 1)
                     System.err.println("Reprocessing documentation within " + sourceName);
                 reprocess(sourceName, outFileName, outDir, libraryID, runtimeOptions, runtimeAliases, buildOptions);
             } else {
                 if (files.size() > 1)
                     System.err.println("Extracting documentation from " + sourceName);
                 process(sourceName, outFileName, outDir, libraryID, runtimeOptions, runtimeAliases, buildOptions);
             }
         }
         return 0;
     }
 
     static public void process(String sourceName,
                                String outFileName,
                                String outDir,
                                String libraryID,
                                Set runtimeOptions,
                                List runtimeAliases,
                                List buildOptions)
     {
         File sourceFile = new File(sourceName);
         if (outFileName == null) {
             if (outDir == null) {
                 outFileName = FileUtils.getBase(sourceName) + ".xml";
             } else {
                 outFileName = outDir + File.separator + 
                 FileUtils.getBase(sourceFile.getName()) + ".xml";
             }
         } 
         File scriptFile = new File(outFileName);
         try {
             String scriptContents = FileUtils.readFileString(sourceFile);
             String script = "#file " + sourceName + "\n" + "#line 1\n" + scriptContents;
             // TODO [jgrandy 2007/01/11] pass in or retrieve JS2DOC_HOME
             String sourceRoot = System.getProperty("JS2DOC_LIBROOT");
             Document descr = JS2Doc.toXML(script, sourceFile, sourceRoot, libraryID, runtimeOptions, runtimeAliases, buildOptions);
             
             JS2DocUtils.xmlToFile(descr, outFileName);
             
         } catch (IOException e) {
             e.printStackTrace();
             System.err.println("Couldn't read script file");
         }
     }
     
     static public void reprocess(String sourceName,
                                String outFileName,
                                String outDir,
                                String libraryID,
                                Set runtimeOptions,
                                List runtimeAliases,
                                List buildOptions)
     {
         File sourceFile = new File(sourceName);
         if (outFileName == null) {
             if (outDir == null) {
                 outFileName = FileUtils.getBase(sourceName) + ".xml";
             } else {
                 outFileName = outDir + File.separator + 
                 FileUtils.getBase(sourceFile.getName()) + ".xml";
             }
         } 
         File scriptFile = new File(outFileName);
         try {
             String scriptContents = FileUtils.readFileString(sourceFile);
 
             Document descr = ReprocessComments.reprocess(scriptContents);
             
             JS2DocUtils.xmlToFile(descr, outFileName);
             
         } catch (IOException e) {
             e.printStackTrace();
             System.err.println("Couldn't read script file");
         }
     }
     
     static public boolean validateAndCompare(String sourceName, 
                                              String schemaName,
                                              String libraryID,
                                              Set runtimeOptions,
                                              List runtimeAliases,
                                              List buildOptions) {
        boolean result = true;
         try {
             File sourceFile = new File(sourceName);
             String sourceContents = FileUtils.readFileString(sourceFile);
             String source = "#file " + sourceName + "\n" + "#line 1\n" + sourceContents;
             String sourceRoot = System.getProperty("JS2DOC_LIBROOT");
             Document test = JS2Doc.toXML(source, sourceFile, sourceRoot, libraryID, runtimeOptions, runtimeAliases, buildOptions);
             
             String expectName = FileUtils.getBase(sourceName) + ".xml";
             File expectFile = new File(expectName);
             String expect = FileUtils.readFileString(expectFile);
             
         } catch (java.io.IOException exc) {
            result = false;
             exc.printStackTrace();
         }
 
         return result;
     }
 }
