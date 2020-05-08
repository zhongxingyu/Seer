 package org.agmip.ui.quadui;
 
 import java.io.File;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ExecutorService;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 import org.agmip.core.types.TranslatorOutput;
 import org.apache.pivot.util.concurrent.Task;
 import org.apache.pivot.util.concurrent.TaskExecutionException;
 
 import org.agmip.translators.apsim.ApsimOutput;
 import org.agmip.translators.dssat.DssatControllerOutput;
 import org.agmip.translators.dssat.DssatWeatherOutput;
 import org.agmip.acmo.util.AcmoUtil;
 import org.agmip.translators.stics.SticsOutput;
 
 // import com.google.common.io.Files;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.agmip.util.JSONAdapter.toJSON;
 
 public class TranslateToTask extends Task<String> {
 
     private HashMap data;
     private ArrayList<String> translateList;
     private ArrayList<String> weatherList, soilList;
     private String destDirectory;
     private boolean compress;
     private static Logger LOG = LoggerFactory.getLogger(TranslateToTask.class);
 
     public TranslateToTask(ArrayList<String> translateList, HashMap data, String destDirectory, boolean compress) {
         this.data = data;
         this.destDirectory = destDirectory;
         this.translateList = new ArrayList<String>();
         this.weatherList = new ArrayList<String>();
         this.soilList = new ArrayList<String>();
         this.compress = compress;
         for (String trType : translateList) {
             if (!trType.equals("JSON")) {
                 this.translateList.add(trType);
             }
         }
         if (data.containsKey("weathers")) {
             for (HashMap<String, Object> stations : (ArrayList<HashMap>) data.get("weathers")) {
                 weatherList.add((String) stations.get("wst_id"));
             }
         }
 
         if (data.containsKey("soils")) {
             for (HashMap<String, Object> soils : (ArrayList<HashMap>) data.get("soils")) {
                 soilList.add((String) soils.get("soil_id"));
             }
         }
     }
 
     @Override
         public String execute() throws TaskExecutionException {
             ExecutorService executor = Executors.newFixedThreadPool(64);
             try {
                 for (String tr : translateList) {
                     // Generate the ACMO here (pre-generation) so we know what
                     // we should get out of everything.
                     File destDir = createModelDestDirectory(destDirectory, tr);
                     AcmoUtil.writeAcmo(destDir.toString(), data, tr.toLowerCase());
                     if (data.size() == 1 && data.containsKey("weather")) {
                         LOG.info("Running in weather only mode");
                         submitTask(executor, tr, data, destDir, true, compress);
                     } else {
                         submitTask(executor, tr, data, destDir, false, compress);
                     }
                 }
                 executor.shutdown();
                 while (!executor.isTerminated()) {
                 }
                 executor = null;
                 //this.data = null;
             } catch (Exception ex) {
                 throw new TaskExecutionException(ex);
             }
             return null;
         }
 
     /**
      * Submit a task to an executor to start translation.
      * 
      * @param executor The <code>ExecutorService</code> to execute this thread on.
      * @param trType The model name to translate to (used to instantiate the
      *                proper <code>TranslatorOutput</code> 
      * @param data The data to translate
      */
     private void submitTask(ExecutorService executor, String trType, HashMap<String, Object> data, File path, boolean wthOnly, boolean compress) {
         TranslatorOutput translator = null;
         if (trType.equals("DSSAT")) {
             if (wthOnly) {
                 LOG.info("DSSAT Weather Translator Started");
                 translator = new DssatWeatherOutput();
             } else { 
                 LOG.info("DSSAT Translator Started");
                 translator = new DssatControllerOutput();
             }
         } else if (trType.equals("APSIM")) {
             LOG.info("APSIM Translator Started");
             translator = new ApsimOutput();
         } else if (trType.equals("STICS")) {
             LOG.info("STICS Translator Started");
             translator = new SticsOutput();
         }
         LOG.debug("Translating with :"+translator.getClass().getName());
         Runnable thread = new TranslateRunner(translator, data, path.toString(), trType, compress);
         executor.execute(thread);
     }
 
     private static File createModelDestDirectory(String basePath, String model) {
         model = model.toUpperCase();
         File originalDestDir = new File(basePath+File.separator+model);
         File destDirectory = originalDestDir;
         int i=0;
         while (destDirectory.exists()) {
             i++;
             destDirectory = new File(originalDestDir.toString()+"-"+i);
         }
         destDirectory.mkdirs();
         return destDirectory;
     }
 }
