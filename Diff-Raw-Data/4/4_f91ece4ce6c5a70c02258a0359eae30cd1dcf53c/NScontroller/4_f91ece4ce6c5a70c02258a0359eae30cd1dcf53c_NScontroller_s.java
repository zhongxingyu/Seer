 package controllers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import models.ComboboxOpts;
 import models.Constants;
 import models.IAjob;
 import models.NSjob;
 
 import org.codehaus.jackson.JsonNode;
 
 import play.Logger;
 import play.data.Form;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Result;
 import tools.Utils;
 import views.html.errorpage;
 import views.html.ns.nsform;
 import views.html.ns.nssummary;
 
 public class NScontroller extends Controller {
     final static Form<NSjob> nsForm = form(NSjob.class);
 
     public static Result initNSform() {
         Form<NSjob> newform = nsForm.fill(new NSjob());
         return ok(nsform.render(newform, "", ""));
     }
 
     public static Result redirectPreloadedNSform(String jobid) {
         IAjob iajob = IAcontroller.getJobFromJsonFile(jobid);
         Form<NSjob> newform = nsForm.fill(new NSjob());
         return ok(nsform.render(newform, iajob.jobid, iajob.jsonSelectedForNS));
     }
 
     public static Result initPreloadedNSform() {
         // Get form
         Form<NSjob> filledForm = nsForm.bindFromRequest();
         NSjob job = filledForm.get();
 
         // Logger.info("sJOB ID:"+job.jobid);
         // Logger.info("selected for NS JSON:"+job.jsonSelectedForNS);
 
         IAjob iajob = IAcontroller.getJobFromJsonFile(job.jobid);
         iajob.jsonSelectedForNS = job.jsonSelectedForNS;
         IAcontroller.writeJsonJobToFile(iajob, job.jobid);
 
         return redirect("/preloaded-normalizationscoring/" + job.jobid);
     }
 
     public static Result submit() {
         // Get form
         Form<NSjob> filledForm = nsForm.bindFromRequest();
 
         // Logger.info(filledForm.data().toString());
 
         // Process submission
         long submissionStartTime = new Date().getTime(); // start time
 
         // Get job
         NSjob job = filledForm.get();
 
         job.linkageGenes = job.linkageGenes.replaceAll("\n|\r", ",");
         job.linkageGenes = job.linkageGenes.replaceAll("\\s", "");
 
         Logger.debug("LINKAGE GENES = " + job.linkageGenes);
 
         Map<String, File> plateFilesMap = new HashMap<String, File>();
 
         // Get file(s) uploaded
         MultipartFormData body = request().body().asMultipartFormData();
 
         // Check if we have an array definition file
         FilePart arrayDefCustomFile = body.getFile("arrayDefCustomFile");
 
         // Logger.error(body.toString());
         // Logger.info("JOBid = "+job.jobid);
 
         if (job.jobid == null | job.jobid.isEmpty()) {
             // Logger.info("Files uploaded, populating plate file map:");
             List<FilePart> plateFiles = body.getFiles();
             for (FilePart fp : plateFiles) {
                 // File f = fp.getFile();
                 // Logger.info("--> "+f.getPath() + "\t"+f.exists());
                 plateFilesMap.put(fp.getFilename(), fp.getFile());
             }
         } else {
             // Logger.info("Files preloaded from IA, populating plate file map:");
             IAjob iajob = IAcontroller.getJobFromJsonFile(job.jobid);
             // Logger.info("selectedForNS = "+iajob.jsonSelectedForNS);
             String[] preloadedList = Json.fromJson(Json.parse(iajob.jsonSelectedForNS), String[].class);
             for (String fileName : preloadedList) {
                 File file = new File(
                         Utils.joinPath(Constants.JOB_OUTPUT_DIR, job.jobid, "ia", "output_files", fileName));
                 // Logger.info("\t"+fileName+" -> " + file.getPath());
                 plateFilesMap.put(fileName, file);
             }
         }
 
         // Generate
         List<String> adList = ComboboxOpts.arrayDef();
         StringBuilder adfiles = new StringBuilder();
         String summaryAD = "Not applied";
 
         if (job.doArrayDef) {
             // Custom file -ERROR HERE FILE NOT UPLOADING
             if (adList.indexOf(job.arrayDefPredefined) == adList.size() - 1) {
                 // Logger.info("Found array definition file");
                 // Remove extra non plate files
                 plateFilesMap.remove(arrayDefCustomFile.getFilename());
                 adfiles.append(arrayDefCustomFile.getFile().getPath());
                 summaryAD = "Custom upload: " + arrayDefCustomFile.getFilename();
             }
             // Predefined array def
             else if (job.selectedArrayDefPlate != null) {
                 String arrayDefDir = Constants.ARRAY_DEF_PATH + "/" + job.arrayDefPredefined;
                 summaryAD = job.arrayDefPredefined + " (";
                 List<String> adPlatesList = ComboboxOpts.arrayDefPlates(job.arrayDefPredefined);
                 if (adPlatesList.indexOf(job.selectedArrayDefPlate) == 0) {
                     // All plates
                     for (int i = 1; i < adPlatesList.size(); i++) {
                         adfiles.append(Utils.joinPath(arrayDefDir, adPlatesList.get(i)));
                         if (i < adPlatesList.size() - 1) {
                             adfiles.append(':');
                         }
                     }
                     summaryAD += "All plates";
                 } else {
                     // One plate
                     adfiles.append(Utils.joinPath(arrayDefDir, job.selectedArrayDefPlate));
                     summaryAD += job.selectedArrayDefPlate;
                 }
                 summaryAD += ")";
             }
         }
 
         // Make directories for output
         if (job.jobid.isEmpty() || job.jobid == null) {
             job.jobid = UUID.randomUUID().toString();
             // Logger.info("Generated new UUID: "+job.jobid);
         }
 
         File outputDir = new File(Utils.joinPath(Constants.JOB_OUTPUT_DIR, job.jobid));
         File nsDir = new File(Utils.joinPath(outputDir.getPath(), "ns"));
         File outputFilesDir = new File(Utils.joinPath(nsDir.getPath(), "output_files"));
         File inputFilesDir = new File(Utils.joinPath(nsDir.getPath(), "input_files"));
 
         // Remove any ns directory if it exists
         if (nsDir.exists()) {
             nsDir.delete();
         }
 
         outputDir.mkdir();
         nsDir.mkdir();
         outputFilesDir.mkdir();
         inputFilesDir.mkdir();
 
         StringBuilder inputfiles = new StringBuilder();
         StringBuilder savenames = new StringBuilder();
         List<String> savenamesList = new ArrayList<String>();
         for (Entry<String, File> e : plateFilesMap.entrySet()) {
             inputfiles.append(e.getValue().getPath() + ":");
             savenames.append(e.getKey() + ":");
             savenamesList.add(e.getKey());
         }
         String ninputfiles = inputfiles.substring(0, inputfiles.lastIndexOf(":"));
         String nsavenames = savenames.substring(0, savenames.lastIndexOf(":"));
 
         if (!job.doLinkage)
             job.linkageCutoff = "-1";
 
         // Prepare arguments
         List<String> c = new ArrayList<String>();
         c.add("Rscript");
         c.add(Constants.RSCRIPT_PATH);
         c.add("--inputfiles");
         c.add(ninputfiles);
         c.add("--savenames");
         c.add(nsavenames);
         c.add("--outputdir");
         c.add(outputFilesDir.getPath());
         c.add("--replicates");
         c.add(job.replicates.toString());
         c.add("--linkagecutoff");
         c.add(job.linkageCutoff.toString());
         c.add("--linkagegenes");
         c.add(job.linkageGenes.toString());
         c.add("--wd");
         c.add(Constants.RSCRIPT_DIR);
 
         if (!adfiles.toString().isEmpty()) {
             c.add("--adfiles");
             c.add(adfiles.toString());
             c.add("--adname");
             c.add(summaryAD);
         }
         if (job.doScoring) {
             c.add("--score");
             // c.add("--sfunction"); c.add(job.scoringFunction);
             c.add("--sfunction");
             c.add("1");
         }
         // Logger.info("#####"+c );
 
         String zipFilePath = nsDir.getPath() + "normalizationscoring-sgatools-" + job.jobid + ".zip";
         StringBuilder shell_output = new StringBuilder();
         StringBuilder shell_output_error = new StringBuilder();
         try {
             // Try execute and read
             Process p = Runtime.getRuntime().exec(c.toArray(new String[0]));
 
             // Read in output returned from shell
             // #########FOR DEBUG ONLY######
             BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
             String line;
             while ((line = in.readLine()) != null) {
                 shell_output.append(line + "\n");
             }
 
             // Read in error input stream (if we have some error)
             BufferedReader in_error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
             String line_error;
             while ((line_error = in_error.readLine()) != null) {
                 shell_output_error.append(line_error + "\n");
             }
             in_error.close();
             // #############################
             Logger.debug(shell_output.toString());
            Logger.error(shell_output_error.toString());
             // Zip files
             Zipper.zipDir(zipFilePath, outputFilesDir.getPath());
         } catch (Exception e) {
             Logger.error(shell_output.toString());
             Logger.error("===============================");
             Logger.error(shell_output_error.toString());
             // Fatal error
             filledForm
                     .reject("plateFiles",
                             "Fatal error, no output files were produced. If this problem persists, please contact the developers with your input files");
             return badRequest(nsform.render(filledForm, "", ""));
         }
 
         // Record time elapsed
         long submissionEndTime = new Date().getTime(); // end time
         long milliseconds = submissionEndTime - submissionStartTime; // check
                                                                      // different
         int seconds = (int) (milliseconds / 1000) % 60;
         int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
 
         Map<String, String> outputFilesMap = new HashMap<String, String>();
         for (File of : outputFilesDir.listFiles()) {
             if (!of.getName().startsWith("combined") && !of.getName().startsWith("README")
                     && !of.getName().startsWith("scores"))
                 outputFilesMap.put(of.getName(), of.getPath());
         }
 
         // Save some objects
         job.timeElapsed = minutes + " mins " + seconds + " secs";
         job.outputFilesMap = outputFilesMap;
         job.summaryAD = summaryAD;
 
         // Save bound data to file
         job.plateFilesMap = plateFilesMap;
         job.downloadZipPath = zipFilePath.replace(Constants.BASE_PUBLIC_DIR, "");
         writeJsonJobToFile(job, job.jobid);
 
         // Direct to summary page
         return redirect("/normalizationscoring/" + job.jobid);
     }
 
     public static Result showJob(String jobid) {
         NSjob nsJob = NScontroller.getJobFromJsonFile(jobid);
 
         if (nsJob == null) {
             return ok(errorpage.render(
                     "The job you have requested was not found, please check to make your job id is correct", "404"));
         }
         return ok(nssummary.render(nsJob));
     }
 
     public static NSjob getJobFromJsonFile(String jobid) {
         try {
             String jsonString = Help.readFile(Utils.joinPath(Constants.JOB_OUTPUT_DIR, jobid, "ns", jobid + ".json"));
             NSjob nsJob = Json.fromJson(Json.parse(jsonString), NSjob.class);
 
             return nsJob;
         } catch (Exception e) {
             Logger.error(e.getMessage());
             return null;
         }
     }
 
     public static boolean writeJsonJobToFile(NSjob job, String jobid) {
         try {
             JsonNode jn = Json.toJson(job);
             Help.writeFile(jn.toString(), Utils.joinPath(Constants.JOB_OUTPUT_DIR, jobid, "ns", jobid + ".json"));
             return true;
         } catch (Exception e) {
             return false;
         }
     }
 
 }
