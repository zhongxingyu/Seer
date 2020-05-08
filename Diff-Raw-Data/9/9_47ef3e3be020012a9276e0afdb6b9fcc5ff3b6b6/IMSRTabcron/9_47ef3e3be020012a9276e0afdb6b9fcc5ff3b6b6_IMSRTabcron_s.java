 package uk.ac.ebi.fgpt.sampletab.imsr;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.fgpt.sampletab.utils.ConanUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.FTPUtils;
 
 public class IMSRTabcron {
 
     @Option(name = "-h", aliases={"--help"}, usage = "display help")
     private boolean help;
 
     @Option(name = "-o", aliases={"--output"}, usage = "output directory")
     private String outputDirName;
 
     @Option(name = "--no-conan", usage = "do not trigger conan loads?")
     private boolean noconan = false;
         
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	private IMSRTabcron() {
 	}
 	
 
 	public static void main(String[] args) {
         new IMSRTabcron().doMain(args);
     }
 	
 	private void submitConan(String submissionIdentifier){
         try {
             ConanUtils.submit(submissionIdentifier, "BioSamples (IMSR)");
         } catch (IOException e) {
             log.error("Problem submitting "+submissionIdentifier, e);
         }
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
         
 		File outdir = new File(this.outputDirName);
 
 		if (outdir.exists() && !outdir.isDirectory()) {
 			System.err.println("Target is not a directory");
 			System.exit(1);
 			return;
 		}
 
 		if (!outdir.exists())
 			outdir.mkdirs();
 
 		IMSRTabWebSummary summary = IMSRTabWebSummary.getInstance();
 		try {
             summary.get();
         } catch (NumberFormatException e) {
             log.error("Unable to download summary", e);
             System.exit(1);
             return;
         } catch (ParseException e) {
             log.error("Unable to download summary", e);
             System.exit(1);
             return;
         } catch (IOException e) {
             log.error("Unable to download summary", e);
             System.exit(1);
             return;
         }
 
         IMSRTabWebDownload downloader = new IMSRTabWebDownload();
         for(int i = 0; i < summary.sites.size(); i++){
             String site = summary.sites.get(i);
             String subID = "GMS-"+site;
             File raw = new File(new File(outdir, subID), "raw.tab.txt");
             if (!raw.exists()){
                 downloader.download(subID, raw);
                submitConan(subID);
             } else {
                 Date fileDate = new Date(raw.lastModified());
                 if (summary.updates.get(i).after(fileDate)){
                     downloader.download(subID, raw);
                    if (!noconan)
                         submitConan(subID);
                 }
             }
         }
         
 	}
 }
