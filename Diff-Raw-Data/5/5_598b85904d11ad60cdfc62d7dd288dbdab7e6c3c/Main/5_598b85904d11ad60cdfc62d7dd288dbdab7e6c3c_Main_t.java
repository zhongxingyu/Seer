 package com.silentmatt.dss;
 
 import com.martiansoftware.jsap.FlaggedOption;
 import com.martiansoftware.jsap.JSAP;
 import com.martiansoftware.jsap.JSAPException;
 import com.martiansoftware.jsap.JSAPResult;
 import com.martiansoftware.jsap.ParseException;
 import com.martiansoftware.jsap.Switch;
 import com.martiansoftware.jsap.UnflaggedOption;
 import com.martiansoftware.jsap.stringparsers.FileStringParser;
 import com.silentmatt.dss.css.CssDocument;
 import com.silentmatt.dss.parser.DSSParser;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.RandomAccessFile;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Arrays;
 
 public final class Main {
     private Main() {
     }
 
     private static void printUsage(JSAP jsap) {
         System.err.println("usage: java -jar dss.jar");
         System.err.println("       " + jsap.getUsage());
         System.err.println();
         System.err.println(jsap.getHelp());
     }
 
     private static void printVersion() {
         System.out.println("DSS 0.2");
     }
 
     @SuppressWarnings("deprecation")
     private static class FileOrURLStringParser extends com.martiansoftware.jsap.stringparsers.URLStringParser {
         private static FileStringParser fileParser = FileStringParser.getParser();
 
         private FileOrURLStringParser() {
             fileParser.setMustBeFile(false).setMustExist(true);
         }
 
         public static FileOrURLStringParser getParser() {
             return new FileOrURLStringParser();
         }
 
         @Override
         public Object parse(String arg) throws ParseException {
             try {
                 return super.parse(arg);
             } catch (ParseException ex) {
                 fileParser.setUp();
                 File file = (File) fileParser.parse(arg);
                 fileParser.tearDown();
                 try {
                     return file.toURI().toURL();
                 } catch (MalformedURLException ex1) {
                     throw new ParseException(ex1);
                 }
             }
         }
     }
 
     private static void setupArguments(JSAP jsap) {
         FlaggedOption outOpt = new FlaggedOption("out")
                 .setStringParser(FileStringParser.getParser().setMustBeFile(true).setMustExist(false))
                 .setRequired(false)
                 .setAllowMultipleDeclarations(false)
                 .setShortFlag('o');
         outOpt.setHelp("File to save outout to");
 
         Switch debugFlag = new Switch("debug")
                 .setLongFlag("debug");
         debugFlag.setHelp("Don't remove DSS directives from output");
 
         Switch versionFlag = new Switch("version")
                 .setShortFlag('v')
                 .setLongFlag("version");
         versionFlag.setHelp("Show version information and exit");
 
         Switch testFlag = new Switch("test")
                 .setShortFlag('t')
                 .setLongFlag("test");
         testFlag.setHelp("Run tests in the specified directory");
 
         Switch colorFlag = new Switch("color")
                 .setLongFlag("color");
         colorFlag.setHelp("Colorize test output");
 
         Switch compressFlag = new Switch("compress")
                 .setShortFlag('c')
                 .setLongFlag("compress");
         compressFlag.setHelp("Compress the CSS output.");
 
         FlaggedOption defineOpt = new FlaggedOption("define")
                 .setAllowMultipleDeclarations(true)
                 .setRequired(false)
                 .setShortFlag('d')
                 .setLongFlag("define")
                 .setUsageName("name:value")
                 .setStringParser(JSAP.STRING_PARSER);
         defineOpt.setHelp("Pre-define a constant in the global namespace");
 
         UnflaggedOption urlOpt = new UnflaggedOption("url")
                 .setStringParser(FileOrURLStringParser.getParser())
                 .setRequired(true);
         urlOpt.setHelp("The filename or URL of the DSS file.");
 
         try {
             jsap.registerParameter(versionFlag);
             jsap.registerParameter(testFlag);
             jsap.registerParameter(colorFlag);
             jsap.registerParameter(debugFlag);
             jsap.registerParameter(compressFlag);
             jsap.registerParameter(defineOpt);
             jsap.registerParameter(outOpt);
 
             jsap.registerParameter(urlOpt);
         } catch (JSAPException j) {
             System.err.println("Unexpected Error: Illegal JSAP parameter.");
             System.exit(2);
         }
     }
 
     public static void main(String[] args) {
         JSAP jsap = new JSAP();
         setupArguments(jsap);
 
         JSAPResult config = jsap.parse(args);
         if (config.getBoolean("version")) {
             printVersion();
             System.exit(0);
         }
 
         if (!config.success()) {
             printUsage(jsap);
             System.exit(1);
         }
 
         if (config.getBoolean("test")) {
             System.exit(runTests(config.getURL("url"), config.getBoolean("color")));
         }
 
         URL url = config.getURL("url");
         File out = config.getFile("out", null);
 
         if (out != null) {
             try {
                 if (new File(url.toURI()).isDirectory()) {
                     throw new MalformedURLException();
                 }
                 if (url.sameFile(out.toURI().toURL())) {
                     System.err.println("Input and output are the same file.");
                     System.exit(1);
                 }
             } catch (MalformedURLException ex) {
                 System.err.println("Invalid file: " + out.getPath());
                 System.exit(1);
             } catch (URISyntaxException ex) {
                 System.err.println("Invalid file: " + out.getPath());
                 System.exit(1);
             }
         }
 
         DSSEvaluator.Options opts = new DSSEvaluator.Options(url);
         ErrorReporter errors = new PrintStreamErrorReporter();
         opts.setErrors(errors);
 
         String[] defines = config.getStringArray("define");
         for (String define : defines) {
             Declaration declaration = DSSParser.parseDeclaration(define, errors);
             opts.getVariables().declare(declaration.getName(), declaration.getExpression());
         }
 
         if (url != null) {
             try {
                 DSSDocument css = DSSDocument.parse(url, errors);
                 if (css != null) {
 
                     CssDocument outputDocument = new DSSEvaluator(opts).evaluate(css);
 
                     String cssString;
                     if (config.getBoolean("debug")) {
                         cssString = css.toString();
                     }
                     else {
                         cssString = outputDocument.toString(config.getBoolean("compress"));
                     }
 
                     if (out == null) {
                         System.out.print(cssString);
                     }
                     else {
                         PrintStream pout = new PrintStream(out);
                         pout.print(cssString);
                         pout.close();
                     }
                 }
             } catch (MalformedURLException ex) {
                 errors.SemErr("DSS: Invalid URL");
             } catch (IOException ex) {
                 errors.SemErr("DSS: I/O error: " + ex.getMessage());
             }
         }
         else {
             System.err.println("Missing url parameter.");
             printUsage(jsap);
             System.exit(1);
         }
 
         if (errors.getErrorCount() > 0) {
             System.exit(1);
         }
     }
 
     private static File getTestDirectory(URL directory) {
         if (directory == null) {
             try {
                 directory = new URL(".");
             } catch (MalformedURLException ex) {
                 System.out.println("Fatal error in runTests.");
                 return null;
             }
         }
 
         File dir;
         try {
             dir = new File(directory.toURI());
         } catch (URISyntaxException ex) {
             dir = null;
         }
 
         if (dir != null && !dir.isDirectory()) {
             dir = null;
         }
 
         return dir;
     }
 
     private static class DssFilenameFilter implements FilenameFilter {
         public boolean accept(File directory, String filename) {
             return filename.toLowerCase().endsWith(".dss");
         }
     }
 
     private static String readFile(File cssFile) {
         try {
             RandomAccessFile raf = new RandomAccessFile(cssFile, "r");
             byte[] contents = new byte[(int)raf.length()];
             raf.readFully(contents);
             return new String(contents);
         }
         catch (IOException ex) {
             return null;
         }
     }
 
     private static String compile(URL url, String dssString, boolean compact) {
         try {
             ErrorReporter errors = new ExceptionErrorReporter(new NullErrorReporter());
             DSSDocument dss = DSSDocument.parse(new ByteArrayInputStream(dssString.getBytes()), errors);
             DSSEvaluator.Options opts = new DSSEvaluator.Options(url);
             opts.setErrors(errors);
             return new DSSEvaluator(opts).evaluate(dss).toString(compact);
         }
         catch (Exception ex) {
             return ex.getMessage();
         }
     }
 
     private static int testString(URL url, String dssString, String correct) {
         String normalCSS = compile(url, dssString, false);
         if (normalCSS == null) {
             return 1;
         }
         String compressed = compile(url, dssString, true);
         if (compressed == null) {
             return 2;
         }
         String decompressed = compile(url, compressed, false);
         if (decompressed == null) {
             return 2;
         }
 
         if (!normalCSS.equals(correct)) {
             return 1;
         }
         if (!decompressed.equals(correct)) {
             return 2;
         }
 
         return 0;
     }
 
     private static String testDssFile(URL url) {
         File cssFile;
         File dssFile;
         try {
             dssFile = new File(url.toURI());
             cssFile = new File(new File(url.toURI()).getAbsolutePath().replace(".dss", ".css"));
             if (!cssFile.exists()) {
                 // HACK: This is an ugly way to do this...
                 throw new URISyntaxException("", "");
             }
         } catch (URISyntaxException ex) {
             return "Could not find css file";
         }
 
         switch (testString(url, readFile(dssFile), readFile(cssFile))) {
         case 0:
             return "PASS";
         case 1:
             return "FAIL";
         case 2:
             return "FAIL (compressed)";
         default:
             return "FAIL";
         }
     }
 
     private static void showResult(String result, boolean color) {
         if (!color) {
             System.out.println(result);
         }
         else if (result.equals("PASS")) {
             System.out.println("\033[32mPASS\033[0m");
         }
         else {
             System.out.println("\033[31m" + result + "\033[0m");
         }
     }
     private static int runTests(URL directory, boolean color) {
         File dir = getTestDirectory(directory);
         if (dir == null) {
             System.err.println("Invalid test directory.");
             return 1;
         }
 
         int errors = 0;
         String[] dirList = dir.list(new DssFilenameFilter());
         Arrays.sort(dirList);
         for (String dssFileName : dirList) {
             try {
                 System.out.print(dssFileName.replace(".dss", "") + ": ");
                 String result = testDssFile(new URL(directory, dssFileName));
                 showResult(result, color);
                 if (!result.equals("PASS")) {
                     ++errors;
                 }
             } catch (MalformedURLException ex) {
                 System.err.println("Invalid DSS file: " + dssFileName);
             }
         }
 
         if (errors == 0) {
             if (color) {
                 System.out.println("\033[32mAll tests passed.\033[0m");
             }
             else {
                 System.out.println("All tests passed.");
             }
         }
         else {
             if (color) {
                System.out.println("\033[31m" + errors + " test" + (errors != 1 ? "s" : "") + " failed.\033[0m");
             }
             else {
                System.out.println(errors + " test" + (errors != 1 ? "s" : "") + " failed.");
             }
         }
         return errors;
     }
 }
