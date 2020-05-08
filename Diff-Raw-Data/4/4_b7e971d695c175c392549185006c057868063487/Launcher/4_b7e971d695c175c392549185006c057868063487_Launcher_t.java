 package com.alexkasko.izpack.builder;
 
 import com.izforge.izpack.compiler.CompilerConfig;
 import org.apache.commons.cli.*;
 
 import java.io.*;
 
 import static java.lang.System.out;
 
 /**
  * User: alexkasko
  * Date: 11/18/12
  */
 public class Launcher {
     private static final String VERSION = "IzPack Installer Builder Utility 1.0";
     private static final String HELP_OPTION = "help";
     private static final String VERSION_OPTION = "version";
     private static final String IZPACK_COPMPRESS_OPTION = "izpack-compress";
 
 
     public static void main(String[] args) throws Exception {
         Options options = new Options();
         try {
             options.addOption("h", HELP_OPTION, false, "show this page");
             options.addOption("v", VERSION_OPTION, false, "show version");
             options.addOption("c", IZPACK_COPMPRESS_OPTION, true, "installer compress option: 'raw' (default), 'deflate' and 'bzip2'");
             CommandLine cline = new GnuParser().parse(options, args);
             if (cline.hasOption(VERSION_OPTION)) {
                 out.println(VERSION);
             } else if (cline.hasOption(HELP_OPTION)) {
                 throw new ParseException("Printing help page:");
             } else if (2 == cline.getArgs().length) {
                 File inputFile = new File(cline.getArgs()[0]);
                 if(!(inputFile.exists() && inputFile.isDirectory())) throw new IOException("Invalid input dir: '" + inputFile.getAbsolutePath() + "'");
                 File outputFile = new File(cline.getArgs()[1]);
                 if(outputFile.exists()) throw new IOException("Output file exists: '" + outputFile.getAbsolutePath() + "'");
                 String compress = cline.hasOption(IZPACK_COPMPRESS_OPTION) ? cline.getOptionValue(IZPACK_COPMPRESS_OPTION) : "raw";
                 buildInstaller(inputFile, outputFile, compress);
             } else {
                 throw new ParseException("Incorrect arguments received!");
             }
         } catch (ParseException e) {
             HelpFormatter formatter = new HelpFormatter();
             out.println(e.getMessage());
             out.println(VERSION);
             formatter.printHelp("java -jar izpack-builder.jar [-c (deflate|bzip2)] input.dir output.jar", options);
         }
     }
 
     private static void buildInstaller(File input, File output, String compress) throws Exception {
         File izpackXml = new File(input, "izpack.xml");
         if(!(izpackXml.exists() && izpackXml.isFile())) throw new IOException(
                 "'izpack.xml' file not found in input directory: '" + input.getAbsolutePath() + "'");
        output.mkdirs();
        if(!output.exists()) throw new IOException(
                        "Cannot create directory for output file: '" + output.getAbsolutePath() + "'");
        output.delete();
         // run compiler
         System.out.println("Starting IzPack ...");
         CompilerConfig compilerConfig = new CompilerConfig(izpackXml.getAbsolutePath(), input.getAbsolutePath(),
                 "standard", output.getAbsolutePath(), compress, null);
         CompilerConfig.setIzpackHome(input.getAbsolutePath());
         compilerConfig.executeCompiler();
         System.out.println("Installer built successfully: '" + output + "'");
     }
 }
