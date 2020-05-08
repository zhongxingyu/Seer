 package org.agmip.ui.quadui;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 
 import org.agmip.core.types.TranslatorInput;
 import org.agmip.dome.DomeUtil;
 import org.agmip.dome.Engine;
 import org.agmip.translators.csv.DomeInput;
 import org.agmip.util.MapUtil;
 import org.apache.pivot.util.concurrent.Task;
 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ApplyDomeTask extends Task<HashMap> {
     private static Logger log = LoggerFactory.getLogger(ApplyDomeTask.class);
     private HashMap<String, HashMap<String, Object>> domes = new HashMap<String, HashMap<String, Object>>();
     private HashMap source;
     private DomeInput translator = new DomeInput();
 
 
     public ApplyDomeTask(String fileName, HashMap m) {
         this.source = m;
         // Setup the domes here.
         String fileNameTest = fileName.toUpperCase();
 
         log.debug("Loading DOME file: {}", fileName);
 
         if (fileNameTest.endsWith(".ZIP") && ! fileNameTest.startsWith(".")) {
             log.debug("Entering Zip file handling");
             ZipFile z = null;
             try {
                 z = new ZipFile(fileName);
                 Enumeration  entries = z.entries();
                 while (entries.hasMoreElements()) {
                     // Do we handle nested zips? Not yet.
                     ZipEntry entry = (ZipEntry) entries.nextElement();
                     log.debug("Processing file: {}", entry.getName());
                     if (entry.getName().toLowerCase().endsWith(".csv")) {
                         translator.readCSV(z.getInputStream(entry));
                         HashMap<String, Object> dome = translator.getDome();
                         String domeName = DomeUtil.generateDomeName(dome);
                         if (! domeName.equals("----")) {
                             domes.put(domeName, new HashMap<String, Object>(dome));
                         }
                     }
                 }
                 z.close();
             } catch (Exception ex) {
                 log.error("Error processing DOME file: {}", ex.getMessage());
                 HashMap<String, Object> d = new HashMap<String, Object>();
                 d.put("errors", ex.getMessage());
             }
         } else if (fileNameTest.endsWith(".CSV")) {
             log.debug("Entering single file DOME handling");
             try {
                 HashMap<String, Object> dome = (HashMap<String, Object>) translator.readFile(fileName);
                 String domeName = DomeUtil.generateDomeName(dome);
                 domes.put(domeName, dome);
             } catch (Exception ex) {
                 log.error("Error processing DOME file: {}", ex.getMessage());
                 HashMap<String, Object> d = new HashMap<String, Object>();
                 d.put("errors", ex.getMessage());
             }
         }
     }
 
     @Override
     public HashMap<String, Object> execute() {
         // First extract all the domes and put them in a HashMap by DOME_NAME
         // The read the DOME_NAME field of the CSV file
         // Split the DOME_NAME, and then apply sequentially to the HashMap.
 
         // PLEASE NOTE: This can be a massive undertaking if the source map
         // is really large. Need to find optimization points.
 
         HashMap<String, Object> output = new HashMap<String, Object>();
         //HashMap<String, ArrayList<HashMap<String, String>>> dome;
         // Load the dome
 
         if (domes.isEmpty()) {
             log.error("No DOME to apply.");
             HashMap<String, Object> d = new HashMap<String, Object>();
             //d.put("domeinfo", new HashMap<String, String>());
             d.put("domeoutput", source);
             return d;
         }
         
         // Flatten the data and apply the dome.
 
         ArrayList<HashMap<String, Object>> flattenedData = MapUtil.flatPack(source);
         for (HashMap<String, Object> entry : flattenedData) {
             Engine domeEngine;
 
             String domeName = MapUtil.getValueOr(entry, "dome_name", "");
             if (! domeName.equals("")) {
                 String tmp[] = domeName.split("[|]");
                 int tmpLength = tmp.length;
                 for (int i=0; i < tmpLength; i++) {
                    if (domes.containsKey(domeName)) {
                        domeEngine = new Engine(domes.get(domeName));
                         domeEngine.apply(entry);
                     }
                 }
             }
         }
 
         output.put("domeoutput", MapUtil.bundle(flattenedData));
         return output;
     }
 }
