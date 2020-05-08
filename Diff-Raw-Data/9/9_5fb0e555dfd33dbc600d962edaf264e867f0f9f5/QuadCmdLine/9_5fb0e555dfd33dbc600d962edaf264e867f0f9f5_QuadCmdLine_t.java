 package org.agmip.ui.quadui;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 import static org.agmip.util.JSONAdapter.*;
 import org.apache.pivot.util.concurrent.Task;
 import org.apache.pivot.util.concurrent.TaskListener;
 import org.apache.pivot.wtk.TaskAdapter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Meng Zhang
  */
 public class QuadCmdLine {
 
     public enum DomeMode {
 
         NONE, FIELD, STRATEGY
     }
 
     public enum Model {
 
         DSSAT, APSIM, JSON
     }
     private static Logger LOG = LoggerFactory.getLogger(QuadCmdLine.class);
     private DomeMode mode = DomeMode.NONE;
     private String convertPath = null;
     private String fieldPath = null;
     private String strategyPath = null;
     private String outputPath = null;
     private ArrayList<String> models = new ArrayList();
     private boolean helpFlg = false;
 
     public void run(String[] args) {
 
         readCommand(args);
         if (!validate()) {
             printHelp();
             return;
         } else {
             argsInfo();
         }
 
         LOG.info("Starting translation job");
         try {
             startTranslation();
         } catch (Exception ex) {
             LOG.error(getStackTrace(ex));
         }
 
     }
 
     private void readCommand(String[] args) {
         int i = 0;
         int pathNum = 2;
         while (i < args.length && args[i].startsWith("-")) {
             if (args[i].equalsIgnoreCase("-n") || args[i].equalsIgnoreCase("-none")) {
                 mode = DomeMode.NONE;
                 pathNum = 2;
             } else if (args[i].equalsIgnoreCase("-f") || args[i].equalsIgnoreCase("-field")) {
                 mode = DomeMode.FIELD;
                 pathNum = 3;
             } else if (args[i].equalsIgnoreCase("-s") || args[i].equalsIgnoreCase("-strategy")) {
                 mode = DomeMode.STRATEGY;
                 pathNum = 4;
             } else if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("-help")) {
                 helpFlg = true;
             } else if (args[i].equalsIgnoreCase("-dssat")) {
                 addModel(Model.DSSAT.toString());
             } else if (args[i].equalsIgnoreCase("-apsim")) {
                 addModel(Model.APSIM.toString());
             } else if (args[i].equalsIgnoreCase("-json")) {
                 addModel(Model.JSON.toString());
             } else {
                 if (args[i].contains("D")) {
                     addModel(Model.DSSAT.toString());
                 }
                 if (args[i].contains("A")) {
                     addModel(Model.APSIM.toString());
                 }
                 if (args[i].contains("J")) {
                     addModel(Model.JSON.toString());
                 }
             }
             i++;
         }
         try {
             if (pathNum > 1) {
                 convertPath = args[i++];
             }
             if (pathNum > 2) {
                 fieldPath = args[i++];
             }
             if (pathNum > 3) {
                 strategyPath = args[i++];
             }
             if (i < args.length) {
                 outputPath = args[i];
             } else {
                 try {
                     outputPath = new File(convertPath).getCanonicalFile().getParent();
                 } catch (IOException ex) {
                     outputPath = null;
                     LOG.error(getStackTrace(ex));
                 }
             }
         } catch (ArrayIndexOutOfBoundsException e) {
             LOG.error("Path arguments are not enough for selected dome mode");
         }
     }
     
     private void addModel(String model) {
         if (models.indexOf(model) < 0) {
             models.add(model);
         }
     }
 
     private boolean validate() {
 
         if (!isValidPath(convertPath, true)) {
             LOG.warn("convert_path is invalid : " + convertPath);
             return false;
         } else if (!isValidPath(outputPath, false)) {
             LOG.warn("output_path is invalid : " + outputPath);
             return false;
         }
 
         if (mode.equals(DomeMode.NONE)) {
         } else if (mode.equals(DomeMode.FIELD)) {
             if (!isValidPath(fieldPath, true)) {
                 LOG.warn("field_path is invalid : " + fieldPath);
                 return false;
             }
         } else if (mode.equals(DomeMode.STRATEGY)) {
             if (!isValidPath(fieldPath, true)) {
                 LOG.warn("field_path is invalid : " + fieldPath);
                 return false;
             } else if (!isValidPath(strategyPath, true)) {
                 LOG.warn("strategy_path is invalid : " + strategyPath);
                 return false;
             }
         } else {
             LOG.warn("Unsupported mode option : " + mode);
             return false;
         }
 
         if (models.isEmpty()) {
             LOG.warn("<model_option> is required for running translation");
             return false;
         }
 
         return true;
     }
 
     private boolean isValidPath(String path, boolean isFile) {
         if (path == null) {
             return false;
         } else {
             File f = new File(path);
             if (isFile) {
                 return f.isFile();
             } else {
                return !path.matches(".*\\.\\w+$");
             }
         }
     }
 
     private void startTranslation() throws Exception {
         LOG.info("Importing data...");
         if (convertPath.endsWith(".json")) {
             try {
                 // Load the JSON representation into memory and send it down the line.
                 String json = new Scanner(new File(convertPath), "UTF-8").useDelimiter("\\A").next();
                 HashMap data = fromJSON(json);
 
                 if (mode.equals(DomeMode.NONE)) {
                     toOutput(data);
                 } else {
                     LOG.debug("Attempting to apply a new DOME");
                     applyDome(data, mode.toString().toLowerCase());
                 }
             } catch (Exception ex) {
                 LOG.error(getStackTrace(ex));
             }
         } else {
             TranslateFromTask task = new TranslateFromTask(convertPath);
             TaskListener<HashMap> listener = new TaskListener<HashMap>() {
                 @Override
                 public void taskExecuted(Task<HashMap> t) {
                     HashMap data = t.getResult();
                     if (!data.containsKey("errors")) {
                         if (mode.equals(DomeMode.NONE)) {
                             toOutput(data);
                         } else {
                             applyDome(data, mode.toString().toLowerCase());
                         }
                     } else {
                         LOG.error((String) data.get("errors"));
                     }
                     t.abort();
                 }
 
                 @Override
                 public void executeFailed(Task<HashMap> arg0) {
                     LOG.error(getStackTrace(arg0.getFault()));
                     arg0.abort();
                 }
             };
             task.execute(new TaskAdapter<HashMap>(listener));
         }
     }
 
     private void applyDome(HashMap map, String mode) {
         LOG.info("Applying DOME...");
         ApplyDomeTask task = new ApplyDomeTask(fieldPath, strategyPath, mode, map);
         TaskListener<HashMap> listener = new TaskListener<HashMap>() {
             @Override
             public void taskExecuted(Task<HashMap> t) {
                 HashMap data = t.getResult();
                 if (!data.containsKey("errors")) {
                     //LOG.error("Domeoutput: {}", data.get("domeoutput"));
                     toOutput((HashMap) data.get("domeoutput"));
                 } else {
                     LOG.error((String) data.get("errors"));
                 }
                 t.abort();
             }
 
             @Override
             public void executeFailed(Task<HashMap> arg0) {
                 LOG.error(getStackTrace(arg0.getFault()));
                 arg0.abort();
             }
         };
         task.execute(new TaskAdapter<HashMap>(listener));
     }
 
     private void toOutput(HashMap map) {
         LOG.info("Generating model input files...");
 
         if (models.size() == 1 && models.get(0).equals("JSON")) {
             DumpToJson task = new DumpToJson(convertPath, outputPath, map);
             TaskListener<String> listener = new TaskListener<String>() {
                 @Override
                 public void taskExecuted(Task<String> t) {
                     LOG.info("Translation completed");
                     t.abort();
                 }
 
                 @Override
                 public void executeFailed(Task<String> arg0) {
                     LOG.error(getStackTrace(arg0.getFault()));
                     arg0.abort();
                 }
             };
             task.execute(new TaskAdapter<String>(listener));
         } else {
             if (models.indexOf("JSON") != -1) {
                 DumpToJson task = new DumpToJson(convertPath, outputPath, map);
                 TaskListener<String> listener = new TaskListener<String>() {
                     @Override
                     public void taskExecuted(Task<String> t) {
                         t.abort();
                     }
 
                     @Override
                     public void executeFailed(Task<String> arg0) {
                         LOG.error(getStackTrace(arg0.getFault()));
                         arg0.abort();
                     }
                 };
                 task.execute(new TaskAdapter<String>(listener));
             }
             TranslateToTask task = new TranslateToTask(models, map, outputPath);
             TaskListener<String> listener = new TaskListener<String>() {
                 @Override
                 public void executeFailed(Task<String> arg0) {
                     LOG.error(getStackTrace(arg0.getFault()));
                     arg0.abort();
                 }
 
                 @Override
                 public void taskExecuted(Task<String> arg0) {
                     LOG.info("=== Completed translation job ===");
                     arg0.abort();
                 }
             };
             task.execute(new TaskAdapter<String>(listener));
         }
     }
 
     private void printHelp() {
         if (helpFlg) {
             System.out.println("***********************************************************************************************************************");
             System.out.println(" The arguments format : <dome_mode_option><model_option><convert_path><field_path><strategy_path><output_path>");
             System.out.println("\t<dome_mode_option>");
             System.out.println("\t\t-n | -none\tRaw Data Only, Default");
             System.out.println("\t\t-f | -filed\tField Overlay, will require Field Overlay File");
             System.out.println("\t\t-s | -strategy\tSeasonal Strategy, will require both Field Overlay and Strategy File");
             System.out.println("\t<model_option>");
             System.out.println("\t\t-D | -dssat\tDSSAT");
             System.out.println("\t\t-A | -apsim\tAPSIM");
             System.out.println("\t\t-J | -json\tJSON");
             System.out.println("\t\t* Could be combined input like -DAJ or -DJ");
             System.out.println("\t<convert_path>");
             System.out.println("\t\tThe path for file to be converted");
             System.out.println("\t<field_path>");
             System.out.println("\t\tThe path for file to be used for field overlay");
             System.out.println("\t<strategy_path>");
             System.out.println("\t\tThe path for file to be used for strategy");
             System.out.println("\t<output_path>");
             System.out.println("\t\tThe path for output.");
             System.out.println("\t\t* If not provided, will use convert_path");
             System.out.println("***********************************************************************************************************************");
         } else {
             LOG.info("Type -h or -help for arguments info");
         }
     }
 
     private void argsInfo() {
        LOG.info("Dome mode: \t" + mode);
         LOG.info("convertPath:\t" + convertPath);
         LOG.info("fieldPath: \t" + fieldPath);
         LOG.info("strategyPath:\t" + strategyPath);
         LOG.info("outputPath:\t" + outputPath);
         LOG.info("Models:\t\t" + models);
     }
 
     private static String getStackTrace(Throwable aThrowable) {
         final Writer result = new StringWriter();
         final PrintWriter printWriter = new PrintWriter(result);
         aThrowable.printStackTrace(printWriter);
         return result.toString();
     }
 }
