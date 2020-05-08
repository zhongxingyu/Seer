 package uk.ac.ebi.fgpt.sampletab.pride;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PRIDEXMLFTPDownload implements Runnable {
 
     // logging
     private Logger log = LoggerFactory.getLogger(getClass());
 
     private String accession = null;
     private File outfile = null;
     private boolean replace = false;
 
     public PRIDEXMLFTPDownload() {
 
     }
 
     public PRIDEXMLFTPDownload(String accession, String outfilename, boolean replace) {
         this(accession, outfilename);
         this.replace = replace;
     }
 
     public PRIDEXMLFTPDownload(String accession, File outfile, boolean replace) {
         this(accession, outfile);
         this.replace = replace;
     }
 
     public PRIDEXMLFTPDownload(String accession, String outfilename) {
         this(accession, new File(outfilename));
     }
 
     public PRIDEXMLFTPDownload(String accession, File outfile) {
         this.accession = accession;
         this.outfile = outfile;
     }
 
     public static PRIDEXMLFTPDownload getInstance() {
         return new PRIDEXMLFTPDownload();
     }
 
     public void download(String accession, String outfilename) {
         // replace by default
         this.download(accession, outfilename, true);
     }
 
     public void download(String accession, String outfilename, boolean replace) {
         this.download(accession, new File(outfilename), replace);
     }
 
     public void download(String accession, File outfile, boolean replace) {
 
         // make sure the path is valid
         // construct directories if required
         outfile = outfile.getAbsoluteFile();
         File outdir = outfile.getParentFile();
         if (!outdir.exists()) {
             outdir.mkdirs();
         }
 
         if (!replace && outfile.exists()) {
             // we are not supposed to overwrite, so dont
             log.debug("Skipping " + accession + " to " + outfile);
             return;
         }
 
         // for some reason that escapes me, the standard java ftp /gunzip tools do not work reliably for all PRIDE data
         // therefore we sacrifice multiplatformness to run well
         List<String> command = new ArrayList<String>();
         ProcessBuilder pb = new ProcessBuilder();
         Process p;
 
         // now we need to download, extract & filter the file
         try {
             //curl needs to redirect stderr to stdout
             String bashcom = "curl -o - -s ftp://ftp.ebi.ac.uk/pub/databases/pride/PRIDE_Exp_Complete_Ac_" + accession + ".xml.gz 2>&1" 
                 + " | gunzip -c -d"
                 + " | sed '/<GelFreeIdentification>/,/<\\/GelFreeIdentification>/d' "
                 + " | sed '/<TwoDimensionalIdentification>/,/<\\/TwoDimensionalIdentification>/d' "
                + " | sed '/<spectrumList/,/<\\/spectrumList>/d' " 
                 + " > " + outfile;
             log.debug(bashcom);
 
             command = new ArrayList<String>();
             command.add("/bin/bash");
             command.add("-c");
             command.add(bashcom);
             pb.command(command);
 
             log.debug("Starting bash process for "+accession);
             
             p = pb.start();
             synchronized (p) {
                 p.waitFor();
             }
 
             log.debug("Processed " + outfile);
 
         } catch (IOException e) {
             log.error("Unable to run bash", e);
             return;
         } catch (RuntimeException e) {
             log.error("Unable to run bash", e);
             return;
         } catch (InterruptedException e) {
             log.error("Unable to run bash", e);
             return;
         }
     }
 
     public void run() {
         download(this.accession, this.outfile, this.replace);
     }
 
     public static void main(String[] args) {
         if (args.length < 2) {
             System.out.println("Must provide an PRIDE identifier and an output filename.");
             return;
         }
         String accession = args[0];
         String outdir = args[1];
 
         PRIDEXMLFTPDownload prideftpdownload = new PRIDEXMLFTPDownload();
         prideftpdownload.download(accession, outdir);
     }
 
 }
