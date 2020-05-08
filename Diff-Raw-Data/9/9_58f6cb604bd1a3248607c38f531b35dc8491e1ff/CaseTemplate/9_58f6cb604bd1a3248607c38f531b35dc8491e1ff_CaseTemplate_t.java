 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.ajax;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  *
  * @author Sam
  */
 public class CaseTemplate {
     
     private EnvConfigData envCfgData = new EnvConfigData();
     private HashMap envConfigOptions = envCfgData.getEnvConfigOptions();
 
     private String templateOriginal;
     private String templateFileLocation = "template";
     private String templatePopulated; // This is what is changed and returned
     private String caseRoot = "scripts/CASENAME";
     private String envConf = ""; // env_conf.xml changes, intially none
 
     public String getTemplateOriginal() {
         return templateOriginal;
     }
 
     public String getTemplatePopulated() {
         return templatePopulated;
     }
 
     public CaseTemplate() {
         try {
             templateFileLocation = getClass().getResource(templateFileLocation).getPath();
             templateOriginal = readFile(templateFileLocation);
 
         } catch (Exception ex) {
             System.err.println("Error: Couldn't read/find template file " + templateFileLocation + "\n> " + ex.getMessage());
             templateOriginal = "Error finding/loading template file: " + ex.getMessage();
         }
         templatePopulated = templateOriginal;
     }
 
     /**
      * Resets current populated template
      */
     public void resetPopulatedTemplate() {
         templatePopulated = templateOriginal;
         caseRoot = "scripts/CASENAME";
     }
 
     public String get() {
         // Important note, replaceAll fails miserably with many special characters like $
         // Also, since this return is used by XML parser wonky '<>' stuff will mess it up
         return templatePopulated.replaceAll("ENVCONF", envConf);
     }
 
     /**
      * Populated template
      * @param casename
      * @param resolution
      * @param compset
      * @param machine
      * @return template populated w/ parameters
      */
     public String fillTemplate(String casename,
             String resolution,
             String compset,
             String machine) {
 
         // SQL DB standin
         String[] fillers = {"CASEROOT", "CASENAME", "RESOLUTION", "COMPSET", "MACHINE"};
         String[] replacements = {caseRoot, casename, resolution, compset, machine};
 
         // Main create case & directory replaces, must run after all XML configs
         for (int i = 0; i < fillers.length; i++) {
             if (!replacements[i].isEmpty()) {
                 templatePopulated = templatePopulated.replaceAll(fillers[i], replacements[i].trim()); // Added trim
             }
         }
 
         caseRoot = "scripts/" + casename;
 
         // Have to do this or XML parser goes wonky
         //templatePopulated = templatePopulated.replaceAll("<", "").replaceAll(">", "");
 
         return templatePopulated;
     }
 
     public void fillEnvConf(String[] params) {
         // Order of params determined by HashMap keyset iterator, very bad
         // But we'll be moving to SQL soon so it'll do for now.
 
         // ENV_CONF.XML Variables
         envConf = ""; // Reset
         // This is a standin for eventual SQL database
         
         String[] names = new String[envConfigOptions.size()];
         String[] values = new String[envConfigOptions.size()];
         String[] defaults = new String[envConfigOptions.size()];
         
         Iterator it = envConfigOptions.keySet().iterator();
         int i = 0;
         while (it.hasNext()) {
             String id = (String) it.next();
             EnvConfigOption option = (EnvConfigOption) envConfigOptions.get(id);
             names[i] = option.getName();
             values[i] = params[i]; // New values
             defaults[i] = option.getDefaultValue();
            //System.err.println("Now we're here #" + i + ": (id: " + id + ") " + params[i] + " vs default " + defaults[i]);
             i++;
         }
         
 
         for (i = 0; i < names.length; i++) {
             if (values[i] != null && !values[i].isEmpty() && !values[i].equalsIgnoreCase(defaults[i])) {
                 envConf += xmlChange("env_conf.xml", names[i], values[i]);
             }
         }
 
         // Add header comment if there are edits
         if (!envConf.isEmpty()) {
             envConf = "# Edits to file 'env_conf.xml'" + "\n" + envConf;
         }
 
     }
 
     /**
      * Syntax for invoking CESM's xmlchange script
      * @param file The xml file to be edited.
      * @param id The xml variable name to be changed.
      * @param value The intended value of the variable associated with the -id argument.
      * @return String for invoking xmlchange w/ passed parameters
      */
     public static String xmlChange(String file, String id, String value) {
         return "xmlchange -file " + file
                 + " -id " + id
                 + " -val " + value + "\n";
     }
 
     /** Read a File to a string
      * Modified from http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
      *
      * @param path
      * @return
      * @throws IOException
      */
     private static String readFile(String path) throws IOException {
         FileInputStream stream = new FileInputStream(new File(path));
         try {
             FileChannel fc = stream.getChannel();
             MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             /* Instead of using default, pass in a decoder. */
             return Charset.defaultCharset().decode(bb).toString();
         } finally {
             stream.close();
         }
     }
 }
