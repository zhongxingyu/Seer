 /*
  * Copyright (C) 2011  Andrew E. Bruno <aeb@qnot.org>
  *
  * This file is part of passtab.
  *
  * passtab is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * passtab is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with passtab.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.qnot.passtab;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.google.gson.Gson;
 
 /**
  * Main class for the PassTab application
  * 
  * @author Andrew E. Bruno <aeb@qnot.org>
  */
 public class PassTab {
     private static Log logger = LogFactory.getLog(PassTab.class);
     private Options options;
     protected PropertiesConfiguration properties;
     
     public PassTab() {
         properties = new PropertiesConfiguration();
         try {
             properties.load(".passtab");
         } catch(ConfigurationException ignored) {
         }
     }
     
     public static void main(String[] args) {
         PassTab passtab = new PassTab();
         
         try {
             passtab.run(args);
         } catch(Exception e) {
             logger.fatal("Something really bad happened: "+e.getMessage());
             e.printStackTrace();
         }   
     }
 
     public void run(String[] args) throws IOException {
         buildOptions();
 
         CommandLineParser parser = new PosixParser();
         CommandLine cmd = null;
         try {
             cmd = parser.parse(options, args);
         } catch (ParseException e) {
             printHelpAndExit(options, e.getMessage());
         }
 
         if (cmd.hasOption("h")) {
             printHelpAndExit(options);
         }
 
         if (cmd.hasOption("p")) {
             print(cmd);
         } else if(cmd.hasOption("e")) {
             export(cmd);
         } else if (cmd.hasOption("g") || cmd.hasOption("t")) {
             printPassword(cmd);
         } else {
             generate(cmd);
         }
 
         System.exit(0);
     }
     
     
     public void print(CommandLine cmd) throws IOException {
         TabulaRecta tabulaRecta = getDatabase(cmd);
         getOutputFormat(cmd).output(getOutputStream(cmd), tabulaRecta);
     }
     
     @SuppressWarnings("unchecked")
     public void export(CommandLine cmd) throws IOException {
         Iterator<String> kit = properties.getKeys("tag");
         if(kit.hasNext()) {
             Map<String, Boolean> tagMap = new HashMap<String, Boolean>();
             while(kit.hasNext()) {
                 String key = kit.next();
                 String[] parts = key.split("\\.");
                 if(parts == null || parts.length < 2) {
                     printHelpAndExit(options, "Malformed configuration file. This property is invalid: "+key);
                 }
                 tagMap.put(parts[1], true);
             }
             
             if(cmd.hasOption("v")) {
                 System.out.println("tag\tcoords\tpassword\tlogin\twebsite");
             }
             
             for(String tag : tagMap.keySet()) {
                 String key = "tag."+tag;
                 String coords = properties.getString(key+".coords");
                 String login = properties.getString(key+".login", "");
                 String website = properties.getString(key+".website", "");
                 String password = fetchPassword(coords, cmd);
                 
                 if(cmd.hasOption("v")) {
                     System.out.println(tag+"\t"+coords+"\t"+password+"\t"+login+"\t"+website);
                 } else {
                     System.out.println(tag+"\t"+password);
                 }
             }
         } else {
             printHelpAndExit(options, "No tags defined in user config file: "+properties.getFile().getAbsolutePath());
         }
     }
 
     @SuppressWarnings("unchecked")
     public void printPassword(CommandLine cmd) throws IOException {
         String coords = null;
         String login = null;
         String website = null;
         String tag = null;
         if(cmd.hasOption("t")) {
             tag = cmd.getOptionValue("t");
             if(tag == null || tag.length() == 0) {
                 printHelpAndExit(options, "Please provide a non empty tag");
             }
             
             Iterator<String> kit = properties.getKeys("tag");
             if(kit.hasNext()) {
                 while(kit.hasNext()) {
                     String key = kit.next();
                     if(key.contains(tag+".coords")) {
                         String prefix = key.replaceAll("\\.coords", "");
                         coords = properties.getString(key);
                         login = properties.getString(prefix+".login", "");
                         website = properties.getString(prefix+".website", "");
                         break;
                     }
                 }
             }
             
             if(coords == null) {
                 die("Tag not found in configuration file: "+tag);
             }
         } else {
             coords = cmd.getOptionValue("g");
         }
         
         if (coords == null || coords.length() == 0) {
             printHelpAndExit(options,
                 "Please provide a row:column (ex. C:F)");
         }
         
         String password = fetchPassword(coords, cmd);
         
         if(cmd.hasOption("v") && tag != null) {
             System.out.println(tag+"\t"+coords+"\t"+password+"\t"+login+"\t"+website);
         } else {
             if(cmd.hasOption("m")) {
                 System.out.print(password);
             } else {
                 System.out.println(password);
             }
         }
     }
     
     private String fetchPassword(String coords, CommandLine cmd) throws IOException {
         String[] parts = coords.split(":");
         if (parts == null || parts.length != 2) {
             printHelpAndExit(options,
                 "Invalid value. Please provide a row:column (ex. C:F)");
         }
         
         String row = parts[0];
         String col = parts[1];
         
         String seq = cmd.getOptionValue("s");
         if (seq == null || seq.length() == 0) {
            String[] seqItems = properties.getStringArray("sequence");
            seq = StringUtils.join(seqItems, ",");
         }
         
         if(seq == null) {
             seq = Sequence.DEFAULT_SEQUENCE;
         }
         
         Sequence sequence = null;
         try {
             sequence = Sequence.fromString(seq);
         } catch(SequenceParseException e) {
             printHelpAndExit(options, "Invalid sequence string: "+e.getMessage());
         }
         
         Direction[] directionPriority = null;
         if(cmd.hasOption("c")) {
             String collision = cmd.getOptionValue("c");
             if("cc".equalsIgnoreCase(collision) || 
                "clockwise_compass".equalsIgnoreCase(collision)) {
                 directionPriority = Direction.clockwiseCompass();
             } else if("ccc".equalsIgnoreCase(collision) || 
                       "counterclockwise_compass".equalsIgnoreCase(collision)) {
                 directionPriority = Direction.counterclockwiseCompass();
             } else {
             String[] cparts = collision.split(",");
                 List<Direction> list = new ArrayList<Direction>();
                 for(String c : cparts) {
                     try {
                         list.add(Direction.fromString(c));
                     } catch(SequenceParseException e) {
                         printHelpAndExit(options, "Direction parse error: "+e.getMessage());
                     }
                 }
                 directionPriority = list.toArray(new Direction[list.size()]);
             }
         }
         
         boolean skipStart = false;
         if(cmd.hasOption("k") || properties.getBoolean("skipstart", false)) {
             skipStart = true;
         } 
         
         int skipInterval = properties.getInt("skip.interval", 0);
         if(cmd.hasOption("x")) {
             String skipStr = cmd.getOptionValue("x");
             try {
                 skipInterval = Integer.valueOf(skipStr);
             } catch(NumberFormatException e) {
                 printHelpAndExit(options, "Please provide a positive integer for the skip value");
             }
         }
         
         if(skipInterval < 0) {
             printHelpAndExit(options, "Please provide a positive integer for the skip value");
         }
 
         TabulaRecta tabulaRecta = getDatabase(cmd);
         
         int rowIndex = tabulaRecta.getHeader().getIndex(row);
         int colIndex = tabulaRecta.getHeader().getIndex(col);
         if (rowIndex == -1 || colIndex == -1) {
             die("Invalid row/column.");
         }
 
         String password = tabulaRecta.getPassword(rowIndex, colIndex, sequence, skipStart, skipInterval, directionPriority);
         return password;   
     }
     
     public void generate(CommandLine cmd) throws IOException {
         Alphabet headerAlphabet = Alphabet.ALPHA_UPPER_NUM;
         Alphabet dataAlphabet = Alphabet.ALPHA_NUM_SYMBOL;
         
         String[] dataAlphaPropArray = properties.getStringArray("alphabet");
         if(cmd.hasOption("a")) {
             try {
                 dataAlphabet = Alphabet.fromString(cmd.getOptionValue("a"));
             } catch(AlphabetParseException e) {
                 printHelpAndExit(options, "Alphabet parsing error: "+e.getMessage());
             }
         } else if(dataAlphaPropArray != null && dataAlphaPropArray.length > 0) {
             try {
                 dataAlphabet = Alphabet.fromStringArray(dataAlphaPropArray);
             } catch(AlphabetParseException e) {
                 printHelpAndExit(options, "Alphabet parsing error: "+e.getMessage());
             }
         }
         
         String[] headerAlphaPropArray = properties.getStringArray("alphabet.header");
         if(cmd.hasOption("b")) {
             try {
                 headerAlphabet = Alphabet.fromString(cmd.getOptionValue("b"));
             } catch(AlphabetParseException e) {
                 printHelpAndExit(options, "Header Alphabet parsing error: "+e.getMessage());
             }
         } else if(headerAlphaPropArray != null && headerAlphaPropArray.length > 0) {
             try {
                 headerAlphabet = Alphabet.fromStringArray(headerAlphaPropArray);
             } catch(AlphabetParseException e) {
                 printHelpAndExit(options, "Header Alphabet parsing error: "+e.getMessage());
             }
         }
         
         if(headerAlphabet.size() > 36) {
             printHelpAndExit(options, "Header alphabets with more than 36 symbols are not supported yet :)");
         }
         
         TabulaRecta tabulaRecta = new TabulaRecta(headerAlphabet, dataAlphabet);
         
         logger.info("Generating a random Tabula Recta (might take a while)...");
         tabulaRecta.generate();
 
         if (cmd.hasOption("d")) {
             OutputStream jsonOut = null;
             OutputStream pdfOut = null;
 
             String name = cmd.getOptionValue("n");
             if (name != null && name.length() > 0) {
                 jsonOut = new FileOutputStream(name + ".json");
                 pdfOut = new FileOutputStream(name + ".pdf");
             } else {
                 jsonOut = new FileOutputStream(new File(System
                         .getProperty("user.home"), ".passtab_db"));
                 pdfOut = new FileOutputStream("passtab.pdf");
             }
 
             OutputFormat jsonFormat = new JSONOutput();
             OutputFormat pdfFormat = new PDFOutput();
             jsonFormat.output(jsonOut, tabulaRecta);
             pdfFormat.output(pdfOut, tabulaRecta);
         } else {
             getOutputFormat(cmd).output(getOutputStream(cmd), tabulaRecta);
         }
     }
     
     private OutputFormat getOutputFormat(CommandLine cmd) {
         String format = cmd.getOptionValue("f");
         OutputFormat outputFormat = null;
 
         if ("json".equals(format)) {
             outputFormat = new JSONOutput();
         } else if ("pdf".equals(format)) {
             if(cmd.hasOption("w")) {
                 outputFormat = new PDFOutput(false);
             } else {
                 outputFormat = new PDFOutput();
             }
         } else {
             outputFormat = new AsciiOutput();
         }
         
         return outputFormat;
     }
     
     private TabulaRecta getDatabase(CommandLine cmd) throws IOException {
         File jsonFile = null;
 
         String inFile = cmd.getOptionValue("i");
         String inFileProps = properties.getString("passtab.path");
         if (inFile != null && inFile.length() > 0) {
             jsonFile = new File(inFile);
         } else if(inFileProps != null && inFileProps.length() > 0){
             jsonFile = new File(inFileProps);
         } else {
             jsonFile = new File(System.getProperty("user.home"), ".passtab_db");
         }
         
         if(!jsonFile.exists() || !jsonFile.canRead()) {
             printHelpAndExit(options, "Failed to read db file: "+jsonFile.getAbsolutePath());
         }
 
         String json = FileUtils.readFileToString(jsonFile);
         Gson gson = new Gson();
         TabulaRecta tabulaRecta = gson.fromJson(json, TabulaRecta.class);
         
         // Check to make sure we loaded a valid JSON file
         if(tabulaRecta == null ||
            tabulaRecta.getHeader() == null || tabulaRecta.getHeader().size() == 0 ||
            tabulaRecta.getDataAlphabet() == null || tabulaRecta.getDataAlphabet().size() == 0 ||
            tabulaRecta.getRawData() == null || tabulaRecta.rows() == 0 || tabulaRecta.cols() == 0
            ) {
             printHelpAndExit(options, "Not a valid passtab JSON file: "+jsonFile.getAbsolutePath());
         }
         
         return tabulaRecta;
     }
     
     private OutputStream getOutputStream(CommandLine cmd) throws IOException {
         String outFile = cmd.getOptionValue("o");
         OutputStream outputStream = System.out;
 
         if (outFile != null && outFile.length() > 0) {
             outputStream = new FileOutputStream(outFile);
         }
         
         return outputStream;
     }
   
     @SuppressWarnings("static-access")
     private void buildOptions() {
         options = new Options();
         options.addOption(
             OptionBuilder.withLongOpt("output")
                          .withDescription("output file")
                          .hasArg()
                          .create("o")
         );
         options.addOption(
             OptionBuilder.withLongOpt("help")
                          .withDescription("print usage info")
                          .create("h")
         );
         options.addOption(
             OptionBuilder.withLongOpt("dbsave")
                          .withDescription("save passtab db to JSON and write out PDF file")
                          .create("d")
         );
         options.addOption(
             OptionBuilder.withLongOpt("name")
                          .withDescription("name of database, creates [name].pdf and [name].json files")
                          .hasArg()
                          .create("n")
         );
         options.addOption(
             OptionBuilder.withLongOpt("format")
                          .withDescription("Output format [pdf|json|ascii]")
                          .hasArg()
                          .create("f")
         );
         options.addOption(
             OptionBuilder.withLongOpt("getpass")
                          .withDescription("[row:column] - get the password at row:column (ex. B:W)")
                          .hasArg()
                          .create("g")
         );
         options.addOption(
                 OptionBuilder.withLongOpt("print")
                              .withDescription("print existing passtab database")
                              .create("p")
         );
         options.addOption(
             OptionBuilder.withLongOpt("input")
                          .withDescription("input file for retrieving passwords")
                          .hasArg()
                          .create("i")
         );
         options.addOption(
                 OptionBuilder.withLongOpt("sequence")
                              .withDescription("sequence for fetching password")
                              .hasArg()
                              .create("s")
             );
         options.addOption(
                 OptionBuilder.withLongOpt("alphabet")
                              .withDescription("alphabet to use for Password Recta")
                              .hasArg()
                              .create("a")
             );
         options.addOption(
                 OptionBuilder.withLongOpt("header")
                              .withDescription("header alphabet to use for row/column headings")
                              .hasArg()
                              .create("b")
             );
         options.addOption(
                 OptionBuilder.withLongOpt("collision")
                              .withDescription("direction precedence should a collision occur")
                              .hasArg()
                              .create("c")
             );
         options.addOption(
                 OptionBuilder.withLongOpt("tag")
                              .withDescription("get password for tag")
                              .hasArg()
                              .create("t")
             );
         options.addOption(
                 OptionBuilder.withLongOpt("skipstart")
                              .withDescription("Don't include the starting cell in the password")
                              .create("k")
             );
         options.addOption(
                 OptionBuilder.withLongOpt("skip")
                              .withDescription("Skip interval. Skip every X symbol when generating password")
                              .hasArg()
                              .create("x")
             );
         options.addOption(
                 OptionBuilder.withLongOpt("export")
                              .withDescription("Export password for all tags defined in user config file")
                              .create("e")
             );
         options.addOption(
                 OptionBuilder.withLongOpt("verbose")
                              .withDescription("print out all fields from config file when fetching a password")
                              .create("v")
             );
         options.addOption(
                 OptionBuilder.withLongOpt("nocolor")
                              .withDescription("Print Password Recta with no color")
                              .create("w")
             );
         options.addOption(
                 OptionBuilder.withLongOpt("chomp")
                              .withDescription("Don't print newline when fetching password")
                              .create("m")
             );
     }
 
     public void die(String message) {
         if (message != null) logger.fatal("Fatal error: " + message);
 
         System.exit(1);
     }
 
     public void printHelpAndExit(Options options, String message) {
         if (message != null)
             logger.fatal("Usage error: " + message + "\n");
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("passtab", options);
         if (message != null) {
             System.exit(1);
         } else {
             System.exit(0);
         }
     }
 
     public void printHelpAndExit(Options options) {
         printHelpAndExit(options, null);
     }
 
 }
