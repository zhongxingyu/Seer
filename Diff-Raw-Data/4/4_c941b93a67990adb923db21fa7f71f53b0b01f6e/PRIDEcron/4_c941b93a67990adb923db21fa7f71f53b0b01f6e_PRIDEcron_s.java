 package uk.ac.ebi.fgpt.sampletab.pride;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 import org.dom4j.DocumentException;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.fgpt.sampletab.utils.ConanUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.FTPUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.FileUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.PRIDEutils;
 
 public class PRIDEcron {
 
     @Option(name = "-h", aliases={"--help"}, usage = "display help")
     private boolean help;
 
     @Argument(required=true, index=0, metaVar="OUTPUT", usage = "output filename")
     private String outputDirName;
     private File outputDir;
    
    @Option(name = "--threaded", usage = "use multiple threads?")
     private boolean threaded = false;
 
     @Option(name = "--no-conan", usage = "do not trigger conan loads?")
     private boolean noconan = false;
     
     private Logger log = LoggerFactory.getLogger(getClass());
     
     private FTPClient ftp = null;
 
     private Map<String, Set<String>> subs = Collections.synchronizedMap(new HashMap<String, Set<String>>());
     
     private Set<String> updated = new HashSet<String>();
     
     private Set<String> deleted = new HashSet<String>();
     
 
     private PRIDEcron() {
         
     }
 
     private void close() {
         try {
             ftp.logout();
         } catch (IOException e) {
             if (ftp.isConnected()) {
                 try {
                     ftp.disconnect();
                 } catch (IOException ioe) {
                     // do nothing
                 }
             }
         }
 
     }
 
     private File getTrimFile(File outdir, String accesssion) {
         File subdir = new File(outdir, "GPR-" + accesssion);
         File trim = new File(subdir, "trimmed.xml");
         return trim.getAbsoluteFile();
     }
     
     private class XMLProjectRunnable implements Runnable {
 
         private File subdir = null;
         private Map<String, Set<String>> subs = null;
         
         public XMLProjectRunnable(File subdir, Map<String, Set<String>> subs){
             this.subdir = subdir;
             this.subs = subs;
         }
         
         public void run() {
             if (subdir.isDirectory() && subdir.getName().matches("GPR-[0-9]+")) {
                 // get the xml file in this subdirectory
                 File xmlfile = new File(subdir, "trimmed.xml");
                 if (xmlfile.exists()) {
                     Set<String> projects;
                     try {
 						projects = PRIDEutils.getProjects(xmlfile);
 					} catch (FileNotFoundException e) {
 						log.error("Error reading file "+xmlfile, e);
 						return;
 					} catch (DocumentException e) {
 						log.error("Error parsing file "+xmlfile, e);
 						return;
 					}
 					if (projects.size() == 0){
 					    log.warn("Unable to find any projects for "+subdir.getName());
 					    projects.add(subdir.getName());
 					}
                     for (String project : projects) {
                         // add it if it does not exist
                         synchronized (this.subs) {
                             if (!subs.containsKey(project)) {
                                 subs.put(project, Collections.synchronizedSet(new HashSet<String>()));
                             }
                         }
                         // now put it in the mapping
                         subs.get(project).add(subdir.getName());
                     }
                 }
             }
             return;
         }
         
     }
 
     private void downloads(File outdir) {
 
         FTPFile[] files;
         try {
             ftp = FTPUtils.connect("ftp.ebi.ac.uk");
             log.info("Getting file listing...");
             files = ftp.listFiles("/pub/databases/pride/");
             log.info("Got file listing");
         } catch (IOException e) {
             log.error("Unable to connect to FTP", e);
             System.exit(1);
             return;
         }
         
         Pattern regex = Pattern.compile("PRIDE_Exp_Complete_Ac_([0-9]+)\\.xml\\.gz");
         //Pattern regex = Pattern.compile("PRIDE_Exp_mzData_Ac_([0-9]+)\\.xml\\.gz");
 
         int nothreads = Runtime.getRuntime().availableProcessors();
         ExecutorService pool = Executors.newFixedThreadPool(nothreads);
 
         for (FTPFile file : files) {
             String filename = file.getName();
             //do a regular expression to match and pull out accession
             Matcher matcher = regex.matcher(filename);
             if (matcher.matches()) {
                 String accession = matcher.group(1);
                 File outfile = getTrimFile(outdir, accession);
                 // do not overwrite existing files unless newer
                 Calendar ftptime = file.getTimestamp();
                 Calendar outfiletime = new GregorianCalendar();
                 outfiletime.setTimeInMillis(outfile.lastModified());
                 if (!outfile.exists() 
                         || ftptime.after(outfiletime)) {
                     
                     Runnable t = new PRIDEXMLFTPDownload(accession, outfile, false);
                     
                     if (threaded){
                         pool.execute(t);
                     } else {
                         t.run();
                     }
                     
                     updated.add(accession);
                 }
             }
         }
         
         //see which ones have been deleted
         for (File subdir : outdir.listFiles()){
             File trimmed = new File(subdir, "trimmed.xml");
             if (trimmed.exists()){
                 String prideAccession = subdir.getName().substring(4);
                 String ftpFilename = "PRIDE_Exp_Complete_Ac_"+prideAccession+".xml.gz";
                 //String ftpFilename = "PRIDE_Exp_mzData_Ac_"+prideAccession+".xml.gz";
                 
                 boolean exists = false;
                 for (FTPFile file : files) {
                     String filename = file.getName();
                     if (filename.equals(ftpFilename)){
                         exists = true;
                         break;
                     }
                 }
                 
                 if (!exists){
                     //this has been deleted
                     log.info("Detected deleted "+prideAccession);
                     deleted.add(prideAccession);
                     updated.add(prideAccession);
                 }
                 
             }
         }
 
         // run the pool and then close it afterwards
         // must synchronize on the pool object
         synchronized (pool) {
             pool.shutdown();
             try {
                 // allow 24h to execute. Rather too much, but meh
                 pool.awaitTermination(1, TimeUnit.DAYS);
             } catch (InterruptedException e) {
                 log.error("Interuppted awaiting thread pool termination", e);
             }
         }
     }
     
     private void findSubs(File outdir) {
 
         // now that all the files have been updated, parse them to extract the relevant data
         
         int nothreads = Runtime.getRuntime().availableProcessors();
         ExecutorService pool = Executors.newFixedThreadPool(nothreads);
         
         for (File subdir : outdir.listFiles()) {
             Runnable t = new XMLProjectRunnable(subdir, subs);
             if (threaded) {
                 pool.execute(t);
             } else {
                 t.run();
             }
         }
 
         // run the pool and then close it afterwards
         // must synchronize on the pool object
         synchronized (pool) {
             pool.shutdown();
             try {
                 // allow 24h to execute. Rather too much, but meh
                 pool.awaitTermination(1, TimeUnit.DAYS);
             } catch (InterruptedException e) {
                 log.error("Interuppted awaiting thread pool termination", e);
             }
         }
     }
     
     private void writeSubs(Writer writer) throws IOException {
 
         // at this point, subs is a mapping from the project name to a set of BioSample accessions
         // output them to a file
         synchronized (subs) {
             // sort them to put them in a sensible order
             List<String> projects = new ArrayList<String>(subs.keySet());
             Collections.sort(projects);
             for (String project : projects) {
 
                 writer.write(project);
                 writer.write("\t");
                 List<String> accessions = new ArrayList<String>(subs.get(project));
                 Collections.sort(accessions);
                 for (String accession : accessions) {
 
                     writer.write(accession);
                     writer.write("\t");
                 }
 
                 writer.write("\n");
             }
         }
     }
     
     private void submitToConan() {
         if (noconan){
             return;
         }
         Set<String> updatedProjects = new HashSet<String>();
         for (String updatedAccession : updated){
             for (String project : subs.keySet()){
                 if (subs.get(project).contains(updatedAccession)){
                     updatedProjects.add(project);
                 }
             }
         }
         
         for (String project : updatedProjects){
             try {
                 if (!noconan){
                     ConanUtils.submit("GPR-"+project, "BioSamples (PRIDE)");
                 }
             } catch (IOException e) {
                 log.error("Unable to submit to conan GPR-"+project, e);
             }
         }
     }
 
     public static void main(String[] args) {
         new PRIDEcron().doMain(args);
     }
 
     public void doMain(String[] args) {
         CmdLineParser parser = new CmdLineParser(this);
         try {
             // parse the arguments.
             parser.parseArgument(args);
             // TODO check for extra arguments?
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             help = true;
         }
 
         if (help) {
             // print the list of available options
             parser.printSingleLineUsage(System.err);
             System.err.println();
             parser.printUsage(System.err);
             System.err.println();
             System.exit(1);
             return;
         }
         
         outputDir = new File(this.outputDirName);
 
         if (outputDir.exists() && !outputDir.isDirectory()) {
             System.err.println("Target is not a directory");
             System.exit(1);
             return;
         }
 
         if (!outputDir.exists())
             outputDir.mkdirs();
 
         try {
             downloads(outputDir);
             log.info("Completed downloading, starting parsing...");
             //TODO hide files that have disappeared from the FTP site.            
         } finally {
             // tidy up ftp connection
             this.close();
         }
 
         //look through xmls to determine projects
         findSubs(outputDir);
 
         //write out the project summary
         //this is needed so conan jobs can read this 
         BufferedWriter projoutwrite = null; 
         //create it in a tempory location
         File projout = new File(outputDir, "projects.tab.txt.tmp");
         try {
             projoutwrite = new BufferedWriter(new FileWriter(projout));
             writeSubs(projoutwrite);
             projoutwrite.close();
         } catch (IOException e) {
             log.error("Unable to write to " + projout, e);
             System.exit(1);
             return;
         } finally {
             if (projoutwrite != null) {
                 try {
                     projoutwrite.close();
                 } catch (IOException e) {
                     //failed within a fail so give up                    
                 }
             }
         }
         //then move it when it is complete
         File projoutFinal = new File(outputDir, "projects.tab.txt");
         try {
             FileUtils.move(projout, projoutFinal);
         } catch (IOException e) {
             log.error("Unable to move "+projout+" to "+projoutFinal, e);
         }
         
         //trigger conan for any project that have been extended or updated
         submitToConan();
         
         
         //TODO handle removed projects
         //get which files exist
         //see which ones are not in FTP
     }
 }
