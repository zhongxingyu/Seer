 package org.nuxeo.ooo;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.artofsolving.jodconverter.OfficeDocumentConverter;
 import org.artofsolving.jodconverter.document.DocumentFamily;
 import org.artofsolving.jodconverter.document.DocumentFormat;
 import org.nuxeo.common.utils.FileUtils;
 import org.nuxeo.ecm.platform.convert.ooomanager.OOoManagerComponent;
 import org.nuxeo.runtime.RuntimeService;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.util.SimpleRuntime;
 
 public class Main {
 
     protected static String[] allprops = { "jod.connection.protocol",
             "jod.max.tasks.per.process", "jod.office.home",
             "jod.task.execution.timeout", "jod.task.queue.timeout",
             "jod.template.profile.dir", "jod.office.pipes", "jod.office.ports" };
 
     protected static void initRuntimeIfNeeded() throws Exception {
         if (!Framework.isInitialized()) {
             RuntimeService runtime = new SimpleRuntime();
             System.setProperty("nuxeo.home",
                     System.getProperty("java.io.tmpdir"));
             Framework.initialize(runtime);
         }
     }
 
     protected static void out(String msg) {
         System.out.println(msg);
     }
 
     protected static OOoManagerComponent manager;
 
     protected static OfficeDocumentConverter converter;
 
     public static void main(String args[]) throws IOException {
 
         try {
             initRuntimeIfNeeded();
         } catch (Exception e) {
             throw new IOException("Unable to init Framework", e);
         }
         out("Create OOManagerComponent");
         manager = new OOoManagerComponent();
 
         initProperties(null);
 
         start();
 
         out("Entering interactive mode (type help)");
         System.out.print("OOo> ");
         String line = System.console().readLine();
         while (line != null) {
             line = line.trim();
             String[] parts = line.split(" ");
             String cmd = parts[0];
             if ("exit".equals(cmd)) {
                 break;
             } else if ("help".equals(cmd)) {
                 help();
             } else if ("start".equals(cmd)) {
                 start();
             } else if ("stop".equals(cmd)) {
                 stop();
             } else if ("status".equals(cmd)) {
                 status();
             } else if ("convert".equals(cmd)) {
                 convert();
             } else if ("config".equals(cmd)) {
                 config();
             } else if ("initProp".equals(cmd)) {
                 if (parts.length > 1) {
                     File propFile = new File(parts[1]);
                     if (propFile.exists()) {
                         initProperties(propFile);
                     } else {
                         out("Unable to load file " + parts[1]);
                     }
                 } else {
                     initProperties(null);
                 }
             } else if ("setProp".equals(cmd)) {
                 if (parts.length > 2) {
                     String propName = parts[1];
                     String propValue = parts[2];
                     setProp(propName, propValue);
                 } else {
                     out("Not enough arguments ...");
                     help();
                 }
             } else {
                 out("Command " + cmd + " (" + line + ") not found");
                 help();
             }
 
             System.out.print("OOo> ");
             line = System.console().readLine();
         }
 
         stop();
     }
 
     protected static void help() {
         out(" start : start OOo Manager");
         out(" stop : stop OOo Manager");
         out(" status : display status of OOo Manager");
         out(" initProp <file> : init properties");
         out(" config : dump current config");
         out(" setProp <name> <value> : set a property");
         out(" convert : runs a .doc to PDF conversion on a sample doc");
     }
 
     protected static void stop() {
         if (manager.isOOoManagerStarted()) {
             out("Stoping OOo");
             manager.stopOOoManager();
         } else {
             out("OOo Manager not started");
         }
     }
 
     protected static void start() throws IOException {
         if (manager.isOOoManagerStarted()) {
             out("OOo Manager already started");
         } else {
             out("Starting ooo manager");
             manager.startOOoManager();
             out("manager started : " + manager.isOOoManagerStarted());
            if (manager.getOfficeManager() != null) {
                out(" OfficeManager class is "
                        + manager.getOfficeManager().getClass().getSimpleName());
                converter = manager.getDocumentConverter();
            }
         }
     }
 
     protected static void status() throws IOException {
         if (!manager.isOOoManagerStarted()) {
             out("OOo Manager not started");
         } else {
             out("OOo Manager is running");
         }
         out(" OfficeManager class is "
                 + manager.getOfficeManager().getClass().getSimpleName());
         out(" OfficeManager info : " + manager.getOfficeManager().toString());
 
     }
 
     protected static void initProperties(File file) throws IOException {
         InputStream propStream = null;
         if (file == null) {
             propStream = Main.class.getClassLoader().getResourceAsStream(
                     "jodSocket.properties");
         } else {
             propStream = new FileInputStream(file);
         }
         Framework.getProperties().load(propStream);
     }
 
     protected static void config() throws IOException {
         Properties props = Framework.getProperties();
         List<String> propNames = new ArrayList<String>();
         for (Object key : props.keySet()) {
             if (key.toString().startsWith("jod.")) {
                 propNames.add(key.toString());
                 out(key.toString() + " => " + props.get(key));
             }
         }
         for (String propName : allprops) {
             if (!propNames.contains(propName)) {
                 out(propName + " => UNSET");
             }
         }
     }
 
     protected static void setProp(String name, String value) throws IOException {
         Framework.getProperties().setProperty(name, value);
         config();
     }
 
     protected static void convert() throws IOException {
         if (!manager.isOOoManagerStarted()) {
             start();
         }
         InputStream in = Main.class.getClassLoader().getResourceAsStream(
                 "hello.doc");
         File inputFile = File.createTempFile("OOo", ".doc");
         File outFile = File.createTempFile("OOo", ".pdf");
         out("Created input file in " + inputFile.getAbsolutePath());
         out("Created output file in " + outFile.getAbsolutePath());
         FileUtils.copyToFile(in, inputFile);
 
         DocumentFormat format = new DocumentFormat("PDF", "pdf",
                 "application/pdf");
         String filterName = "writer_pdf_Export";
         Map<String, Object> storeProperties = new HashMap<String, Object>();
         storeProperties.put("FilterName", filterName);
         format.setStoreProperties(DocumentFamily.TEXT, storeProperties);
 
         converter.convert(inputFile, outFile, format,
                 new HashMap<String, Serializable>());
 
         outFile = new File(outFile.getAbsolutePath());
 
         out("Wrote PDF file in " + outFile.getAbsoluteFile() + " ("
                 + outFile.length() + " Bytes)");
 
     }
 }
