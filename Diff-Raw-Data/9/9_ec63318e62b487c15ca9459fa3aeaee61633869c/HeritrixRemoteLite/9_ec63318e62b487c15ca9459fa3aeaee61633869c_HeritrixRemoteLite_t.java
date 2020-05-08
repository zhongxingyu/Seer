 package hu.juranyi.zsolt.heritrixremote;
 
 import hu.juranyi.zsolt.heritrixremote.model.Heritrix;
 import hu.juranyi.zsolt.heritrixremote.model.HeritrixCall;
 import hu.juranyi.zsolt.heritrixremote.model.Job;
 import hu.juranyi.zsolt.heritrixremote.model.JobState;
 import hu.juranyi.zsolt.heritrixremote.model.TextFile;
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import org.apache.commons.io.FileUtils;
 
 /**
  * Lite version of HeritrixRemote - created as a workaround because
  * HeritrixRemote seemed to fail (freeze) fetching Heritrix index page when
  * there are more than 338 jobs. So this version does not fetch Heritrix index
  * page, so has no job filters, and no error handling, just pure command
  * sending. Also, this version contains only functions and output needed at my
  * workplace. Later I found the solution for the index page fetching, so
  * HeritrixRemote is fully functional.
  *
  * @author Zsolt Jurányi
  */
 public class HeritrixRemoteLite {
 
     public static Heritrix heritrix;
     private static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
 
     public static void main(String[] args) {
         if (args.length < 4) {
             printUsage();
         } else {
             heritrix = new Heritrix(args[0], args[1]);
             if (args[2].equals("create")) { // 0:host 1:auth 2:create 3:urls 4:use 5:cxml
                 if (args.length < 6) {
                     printUsage();
                 } else {
                     create(args);
                 }
             } else if (args[2].equalsIgnoreCase("status")) { // 0:host 1:auth 2:status 3:ulrs/jobdir
                 status(args[3]);
             } else if (args[2].equals("build")) {
                 basicAction(args[3], "build", new JobState[]{JobState.UNBUILT});
             } else if (args[2].equals("launch")) {
                 basicAction(args[3], "launch", new JobState[]{JobState.READY});
             } else if (args[2].equals("unpause")) {
                 basicAction(args[3], "unpause", new JobState[]{JobState.PAUSED});
             } else if (args[2].equals("pause")) {
                 basicAction(args[3], "pause", new JobState[]{JobState.RUNNING});
             } else if (args[2].equals("teardown")) {
                 basicAction(args[3], "teardown", new JobState[]{JobState.READY, JobState.PAUSED, JobState.FINISHED});
             } else if (args[2].equals("terminate")) {
                 basicAction(args[3], "terminate", new JobState[]{JobState.PAUSED, JobState.RUNNING});
             } else if (args[2].equals("store")) { // 0:host 1:auth 2:status 3:ulrs/jobdir 4:heritrixdir 5:archivedir
                 if (args.length < 6) {
                     printUsage();
                 } else {
                     store(args);
                 }
             }
         }
     }
 
     private static void printUsage() {
         System.out.println("HeritrixRemote Lite\n");
         System.out.println("Arguments:");
         System.out.println("<host> <auth> create <urls> use <cxml>");
         System.out.println("<host> <auth> build|launch|pause|unpause|terminate|teardown <jobdir OR urls>");
         System.out.println("<host> <auth> store <jobdir OR urls> <heritrix directory> <archive directory>");
     }
 
     private static void create(String[] args) {
         String cxml = args[5];
         List<String> URLs = new ArrayList<String>();
         for (String URL : args[3].split(",")) {
             if (URL.matches("(https?|ftp)://.+\\..+")) {
                 URLs.add(URL);
             }
         }
         if (URLs.isEmpty()) {
             return;
         }
 
         String jobdir = Job.URLtoDirectoryName(URLs.get(0));
 
         try {
             // Old method fails after 338 job:
             //new HeritrixCall(heritrix).data("action=create&createpath=" + jobdir).getResponse();
 
             // We must avoid fetching the response (Heritrix index page):
            String curl = "curl DATA -k -u AUTH --anyauth --location URL -3";
             curl = curl.replace("AUTH", heritrix.getUserPass()).replace("URL", "https://" + heritrix.getHostPort() + "/engine");
             curl = curl.replace("DATA", "-d " + "action=create&createpath=" + jobdir);
             Process proc = Runtime.getRuntime().exec(curl);
             //proc.waitFor();
             Thread.sleep(1000);
         } catch (Exception ex) {
             System.out.println("Creation failed at Heritrix call.");
             return;
         }
 
         TextFile oldCXML = new TextFile(cxml);
         if (!oldCXML.load()) {
             System.out.println("Creation failed at CXML loading.");
             return;
         }
 
         TextFile newCXML = new TextFile("temp.cxml");
         boolean inProp = false;
         for (String line : oldCXML.getLines()) {
             if (inProp) {
                 if (line.matches(".*</prop>.*")) {
                     inProp = false;
                 } else {
                     continue; // skipping old seed URL lines
                 }
             }
 
             newCXML.getLines().add(line);
 
             // inserting seed URLs
             if (line.matches(".*<prop[^>]+key=\"?seeds.textSource.value\"?.*")) {
                 inProp = true;
                 for (String URL : URLs) {
                     newCXML.getLines().add(URL.replaceAll("&", "&amp;"));
                 }
             }
         }
         newCXML.save();
 
         // push it
         try {
             String response = new HeritrixCall(heritrix).path("job/" + jobdir + "/jobdir/crawler-beans.cxml").file(new File("temp.cxml")).getResponse();
             if (null != response && !response.isEmpty()) {
                 System.out.println("Creation failed at CXML push - Heritrix output not empty.");
                 return;
             }
         } catch (Exception ex) {
             System.out.println("Creation failed at CXML push - exception: " + ex.getMessage());
             return;
         }
 
         // <timestamp> "created job" <jobdir> <urls>
         System.out.println(df.format(new Date()) + "\tcreated\t" + jobdir + "\t" + args[3]);
     }
 
     private static Job recognizeJob(String jobdirORurls) {
         String jobdir = jobdirORurls; // jobdir
         if (jobdir.matches("(https?|ftp)://.+\\..+")) { // urls
             jobdir = jobdir.split(",")[0]; // first url
             jobdir = Job.URLtoDirectoryName(jobdir); // jobdir
         }
 
         return new Job(heritrix, jobdir);
     }
 
     private static void status(String jobdirORurls) {
         Job job = recognizeJob(jobdirORurls);
 
         String startDateStr = (null == job.getStartDate()) ? "N/A" : new SimpleDateFormat("yyyyMMdd HHmmss").format(job.getStartDate());
 
         // <timestamp> "status" <jobdir> <state> <startdate>
 
         System.out.println(
                 df.format(new Date())
                 + "\tstatus\t"
                 + job.getDir() + "\t"
                 + job.getState().toString() + "\t"
                 + startDateStr);
     }
 
     private static void basicAction(String jobdirORurls, String action, JobState[] allowedJobStates) {
         Job job = recognizeJob(jobdirORurls);
         List<JobState> allowed = Arrays.asList(allowedJobStates);
         if (allowed.contains(job.getState())) {
             try {
                 new HeritrixCall(heritrix).path("job/" + job.getDir()).data("action=" + action).getResponse();
                 // TODO should I check something here?
 
                 // <timestamp> <action> <jobdir>
                 System.out.println(df.format(new Date()) + "\t" + action + "\t" + job.getDir());
             } catch (Exception ex) {
                 System.out.println("Action failed - exception: " + ex.getMessage());
             }
         }
     }
 
     private static void store(String[] args) {
         /*String heritrixMirrorDir = heritrix.getJobsDirectory().replaceAll("job.?$", "mirror/");
          if (!(new File(heritrixMirrorDir).exists())) {
          System.out.println("No mirror dir.");
          return;
          }*/
         String heritrixMirrorDir = args[4] + "/mirror/";
 
         Job job = recognizeJob(args[3]);
 
         if (job.getState().equals(JobState.FINISHED)) {
             String startDateString = new SimpleDateFormat("yyyyMMdd").format(job.getStartDate());
             String archiveDir = args[5].replaceAll("/$", "") + "/" + startDateString + "/";
             boolean success = false;
             for (String seedURL : job.getSeedURLs()) {
                 String host = seedURL.replaceAll(".*://", "").replaceAll("/.*$", "");
                 File sourceDir = new File(heritrixMirrorDir + host);
                 File destDir = new File(archiveDir + host);
                 System.out.println("Moving " + sourceDir + " to " + destDir);
                 try {
                     FileUtils.moveDirectory(sourceDir, destDir);
                     success = true; // one move means success
                 } catch (IOException ex) {
                     System.out.println("Failed to move directory: " + sourceDir);
                     // Not always an error: can come up when a job contains
                     // more than one seed URLs with the same host.
                 }
             }
             if (success) {
                 System.out.println(df.format(new Date()) + "\tstored\t" + job.getDir() + "\t" + archiveDir);
             }
         }
     }
 }
