 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.dom4j.DocumentException;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.fgpt.sampletab.utils.PRIDEutils;
 
 public class PRIDEcronBulk {
 
     @Option(name = "-h", aliases={"--help"}, usage = "display help")
     private boolean help;
 
     //TODO make required
    @Option(name = "-i", aliases={"--input"}, usage = "input directory")
     private String inputFilename;
 
     //TODO make required
     @Option(name = "-s", aliases={"--scripts"}, usage = "script directory")
     private String scriptDirname;
 
     //TODO make required
    @Option(name = "-j", aliases={"--projects"}, usage = "projects filename")
     private String projectsFilename;
     
     @Option(name = "--threaded", usage = "use multiple threads?")
     private boolean threaded = false;
 
     @Option(name = "-n", aliases={"--hostname"}, usage = "server hostname")
     private String hostname = "mysql-ae-autosubs-test.ebi.ac.uk";
 
     @Option(name = "-t", aliases={"--port"}, usage = "server port")
     private int port = 4340;
 
     @Option(name = "-d", aliases={"--database"}, usage = "server database")
     private String database = "autosubs_test";
 
     @Option(name = "-u", aliases={"--username"}, usage = "server username")
     private String username = "admin";
 
     @Option(name = "-p", aliases={"--password"}, usage = "server password")
     private String password = "edsK6BV6";
     
     private Logger log = LoggerFactory.getLogger(getClass());
 
     private class DoProcessFile implements Runnable {
         private final File subdir;
         private final File scriptdir;
         private final File projectsFile;
         
         public DoProcessFile(File subdir, File scriptdir, File projectsFile){
             this.subdir = subdir;
             this.scriptdir = scriptdir;
             this.projectsFile = projectsFile; 
         }
 
         public void run() {
             File xml = new File(subdir, "trimmed.xml");
             File sampletabpre = new File(subdir, "sampletab.pre.txt");
             
             if (!xml.exists()) {
                 log.warn("xml does not exist ("+xml+")");
                 return;
             }
             
             File target;
 
             // convert xml to sampletab.pre.txt
             target = sampletabpre;
             if (!target.exists()
                     || target.lastModified() < xml.lastModified()) {
                 log.info("Processing " + target);
                 
                 PRIDEXMLToSampleTab c;
                 try {
                     c = new PRIDEXMLToSampleTab(projectsFile.getAbsolutePath());
                     log.info("Converting "+xml.getPath()+" to "+sampletabpre.getPath());
                     c.convert(xml.getPath(), sampletabpre.getPath());
                 } catch (IOException e) {
                     log.error("Problem processing "+sampletabpre);
                     e.printStackTrace();
                     return;
                 } catch (DocumentException e) {
                     log.error("Problem processing "+sampletabpre);
                     e.printStackTrace();
                     return;
                 } 
             }
             
             new SampleTabcronBulk(hostname, port, database, username, password).process(subdir, scriptdir);
         }
         
     }
     
     public static void main(String[] args) {
         new PRIDEcronBulk().doMain(args);
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
             parser.printUsage(System.err);
             System.err.println();
             System.exit(1);
             return;
         }
         
         File outdir = new File(inputFilename);
         
         if (outdir.exists() && !outdir.isDirectory()) {
             log.error("Target is not a directory");
             System.exit(1);
             return;
         }
 
         if (!outdir.exists())
             outdir.mkdirs();
 
         File scriptdir = new File(scriptDirname);
         
         if (!outdir.exists() && !outdir.isDirectory()) {
             log.error("Script directory missing or is not a directory");
             System.exit(1);
             return;
         }
         
         log.info("Parsing projects file");
         
         File projectsFile = new File(projectsFilename);
 
         //read all the projects
         Map<String, Set<String>> projects;
         try {
             projects = PRIDEutils.loadProjects(projectsFile);
         } catch (IOException e) {
             log.error("Unable to read projects file "+projectsFilename);
             e.printStackTrace();
             System.exit(1);
             return;
         }
         
         
         int nothreads = Runtime.getRuntime().availableProcessors();
         ExecutorService pool = Executors.newFixedThreadPool(nothreads);
 
         for (String name: projects.keySet()){
             File subdir = new File(outdir, Collections.min(projects.get(name)));
             Runnable t = new DoProcessFile(subdir, scriptdir, projectsFile);
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
                 log.error("Interuppted awaiting thread pool termination");
                 e.printStackTrace();
             }
         }
     }
 }
