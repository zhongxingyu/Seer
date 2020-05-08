 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xsqconvertergit;
 
 import xsqconvertergit.interfaces.CSFastQEntryInterface;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.io.FileUtils;
 import xsqconvertergit.interfaces.FastaQualEntryInterface;
 
 /**
  * Writes FastQentries to an output file. 
  * Chunks the output file if the amount of entries is larger than the chunksize. 
  * Also sets the seqName of the entry just before writing it to the output file. 
  * @author Wim Spee
  */
 public class FastQWriter {
     
     private long readCounter = new Long(0);
     private Long chunkCounter = new Long(0);
     private BufferedWriter fastqOut = null;   
     private BufferedWriter qualOut = null;
     
     private long chunkSize;   
     
     
     private File outputDirReads;
     
     private String writerId;
     
     private List<File> writtenFastQFiles;
 
     /**
      * Writes FastQentries to an output file.
      * @param libraryName name of the library for which reads are being converted. 
      * @param outputDir output directory where the fastq file should be written
      * @param chunkSize amount or reads after which a new fastq chunk file should be opened.  
      * @param dateString dateString that should be used in the name of the fastq files
      * @param laneNr laneNr that should be used in the name of the fastq files
      * @param tagName tag name that should be used in the name of the fastq files
      */
     public FastQWriter(String writerId, File baseOutputDir, long chunkSize) {
         
         this.chunkSize = chunkSize;
         this.writerId = writerId;
         
         writtenFastQFiles = new ArrayList<File>();
         
        
         createOutputDir(baseOutputDir, writerId);   
         
     } 
     
     
     /**
      * Creates a output directory for the library being processed
      * @param outputDir the base output directory
      * @param libraryName the library name being processed
      */
     private void createOutputDir(File baseOutputDir, String writerId ) {
        
         File outputDir = new File(baseOutputDir,writerId );
         //create the outputDir if it doesn't exist 
         if(!outputDir.exists())
         {
             outputDir.mkdir();
         }
         
         outputDirReads = new File(outputDir, "reads" );        
         if(!outputDirReads.exists())
         {
             outputDirReads.mkdir();
         } 
         
         
         
     }
     
     public boolean checkForExistingOutput() {
         
         //check if there are already fastq files in the outputDir and it's subdirectories
         String[] extensions = new String[]{"fastq"};
         List<File> fastqFiles = (List<File>) FileUtils.listFiles(outputDirReads, extensions, true);
         
         return !fastqFiles.isEmpty();  
     }
     
     
     
     /**
      * Set the output buffer to write to a next file (chunk)
      * @param fastQEntry 
      */
     private void setWriterToNextChunk(FastQDialect fq){
         chunkCounter++;
         if(fq == FastQDialect.csfasta){
         	StringBuilder chunkFastaFileName = getChunkFileName(".csfasta");
         	openNextChunk(chunkFastaFileName, true);
         	StringBuilder chunkQualFileName = getChunkFileName(".qual");
         	openNextChunk(chunkQualFileName, false);
         }else{
         	StringBuilder chunkFastQFileName = getChunkFileName(".fastq");
         	openNextChunk(chunkFastQFileName, true);
         }  
     }
 
     
     private void openNextChunk(StringBuilder chunkFastQFileName, boolean containsSequence) {
     	File outPutChunk = new File(outputDirReads, chunkFastQFileName.toString());
         writtenFastQFiles.add(outPutChunk);
         FileWriter fstream;
         try {
             fstream = new FileWriter(outPutChunk);
             if(containsSequence){
             	fastqOut = new BufferedWriter(fstream);        
             }else{
             	qualOut = new BufferedWriter(fstream);     
             }
         } catch (IOException ex) {
             Logger.getLogger(XSQConverterGit.class.getName()).log(Level.SEVERE, null, ex);
         }
 		
 	}
 
 	private StringBuilder getChunkFileName(String suffix) {
     	StringBuilder chunkFileName = new StringBuilder();
         chunkFileName.append("p");
         chunkFileName.append(chunkCounter);
         chunkFileName.append('.');
         chunkFileName.append(writerId);
         chunkFileName.append(suffix); 
 		return chunkFileName;
 	}
 
     
    	/**
      * Write a fastq entry to the output buffer. Adds seqName to the fastq entry. Calls setWriterToNextChunk if read counter % chunksize = 0 .
      * @param fastQEntry the fastq entry to write
      */
     public void writeFastQEntry(CSFastQEntryInterface fastQEntry, FastQDialect dialect) {
         try {   
             
             fastQEntry.setSeqName(fastQEntry.getSeqName()+"_"+readCounter );            
 
             if(readCounter % chunkSize == 0 && readCounter != 0)
             {
                 closeWriter();
                 setWriterToNextChunk(dialect);
             }            
             fastqOut.write(fastQEntry.toString());
             if(fastQEntry instanceof FastaQualEntryInterface){
             	qualOut.write(((FastaQualEntryInterface) fastQEntry).toQualString());
             }
             readCounter++;           
            
             
                         
         } catch (IOException ex) {
             Logger.getLogger(XSQConverterGit.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     /**
      * Close and flush the current output buffer to file
      */
     public void closeWriter()
     {
         try {
             if(fastqOut != null)
             {
                 fastqOut.close();
                
             }
            if(qualOut != null )
            {
                qualOut.close();
            }
            
            
             
         } catch (IOException ex) {
             Logger.getLogger(XSQConverterGit.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public void printNrReadsWritten()
     {
         System.out.println(writerId+"\t"+readCounter);           
     }
 
     public String getWriterId() {
         return writerId;
     }
 
     public long getReadCounter() {
         return readCounter;
     }
 
    public void openFastQFileForWriting(FastQDialect dialect){
 		setWriterToNextChunk(dialect);
     }
 
     public List<File> getWrittenFiles() {
         return writtenFastQFiles;
     }
     
     
     
     
 
     
     
     
     
     
     
     
     
     
 
     
     
     
     
     
     
 
 
     
     
 
     
     
     
 
     
     
     
     
     
     
     
     
 }
