 package net.maizegenetics.gbs.pipeline;
 
 import java.awt.Frame;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import net.maizegenetics.util.MultiMemberGZIPInputStream;
 import javax.swing.ImageIcon;
 import net.maizegenetics.gbs.homology.ParseBarcodeRead;
 import net.maizegenetics.gbs.homology.ReadBarcodeResult;
 import net.maizegenetics.gbs.tagdist.TagCountMutable;
 import net.maizegenetics.gbs.tagdist.TagsByTaxa.FilePacking;
 import net.maizegenetics.gbs.util.ArgsEngine;
 import net.maizegenetics.plugindef.AbstractPlugin;
 import net.maizegenetics.plugindef.DataSet;
 import net.maizegenetics.util.DirectoryCrawler;
 import org.apache.log4j.Logger;
 
 /* TODO:
  * - Pass some of countTags() initial code to other methods
  *   - Grabbing/parsing key files
  *   - Directory crawling for the fastq files
  * - make keyFileList a private variable
  * - Uncomment null case
  * - Parse r1xyz
  */
 
 /** 
  * Derives a tagCount list for each fastq file in the input directory.
  *
  * Keeps only good reads having a barcode and a cut site and no N's in the
  * useful part of the sequence.  Trims off the barcodes and truncates sequences
  * that (1) have a second cut site, or (2) read into the common adapter.
  * 
  */
 public class FastqPairedEndToTagCountPlugin extends AbstractPlugin {  
     static long timePoint1;
     private ArgsEngine engine = null;
     private Logger logger = Logger.getLogger(FastqPairedEndToTagCountPlugin.class);
 // DEPENDING ON APPROACH, MAY NEED TO ADD ADDITIONAL DIRECOTORYnAME, OR
 // LOOP THROUGH THIS ONE TWICE
     String directoryName=null;
     String keyfile=null;
     String enzyme = null;
     int maxGoodReads = 200000000;
     int minCount =1;
     String outputDir=null;
 
     public FastqPairedEndToTagCountPlugin() {
         super(null, false);
     }
 
     public FastqPairedEndToTagCountPlugin(Frame parentFrame) {
         super(parentFrame, false);
     }
 
     private void printUsage(){
         logger.info(
              "\n\nUsage is as follows:\n"
             + " -i  Input directory containing FASTQ files in text or gzipped text.\n"
             + "     NOTE: Directory will be searched recursively and should\n"
             + "     be written WITHOUT a slash after its name.\n\n"
             + " -k  Key file listing barcodes distinguishing the samples\n"
             + " -e  Enzyme used to create the GBS library, if it differs from the one listed in the key file.\n"
             + " -s  Max good reads per lane. (Optional. Default is 200,000,000).\n"
             + " -c  Minimum tag count (default is 1).\n"
             + " -o  Output directory to contain .cnt files (one per FASTQ file, defaults to input directory).\n\n"
         );
     }
 
     public DataSet performFunction(DataSet input){
         File qseqDirectory = new File(directoryName);
         if (!qseqDirectory.isDirectory()) {
             printUsage();
             throw new IllegalStateException("The input name you supplied is not a directory.");
         }
         countTags(keyfile, enzyme, directoryName, outputDir, maxGoodReads, minCount);  
         return null;
     }
 
     @Override
     public void setParameters(String[] args) {
         if(args.length==0) {
             printUsage();
             throw new IllegalArgumentException("\n\nPlease use the above arguments/options.\n\n");
         }
 //        try{
             if(engine == null){
                 engine = new ArgsEngine();
 //NEED TO MODIFY TO SET TO TWO FILE LOCATIONS
                 engine.add("-i", "--input-directory", true);
 //MAY NEED TO MODIFY TO TAKE IN TWO KEY FILES
                 engine.add("-k", "--key-file", true);
 //NEED TO MODIFY TO SET TWO ENZYMES
                 engine.add("-e", "--enzyme", true);
                 engine.add("-s", "--max-reads", true);
                 engine.add("-c", "--min-count", true);
                 engine.add("-o", "--output-file", true);
                 engine.parse(args);
             }
 //CREATE A NESTING LOOP THAT RUNS THROUGH THE TWO DIRECTORIES AND CHECKS
 // FOR THE TWO ENZYMES
             if (engine.getBoolean("-i")) { directoryName = engine.getString("-i");}
             else{ printUsage(); throw new IllegalArgumentException("Please specify the location of your FASTQ files."); }
 
             if(engine.getBoolean("-k")){ keyfile = engine.getString("-k");}
             else{ printUsage(); throw new IllegalArgumentException("Please specify a barcode key file.");}
 
             if(engine.getBoolean("-e")){ enzyme = engine.getString("-e"); }
             else{ 
                 System.out.println("No enzyme specified.  Using enzyme listed in key file.");
 //                printUsage(); throw new IllegalArgumentException("Please specify the enzyme used to create the GBS library.");
             }
         
 //END THE NEST LOOP HERE
             if(engine.getBoolean("-s")){ maxGoodReads = Integer.parseInt(engine.getString("-s"));}
 
             if (engine.getBoolean("-c")) { minCount = Integer.parseInt(engine.getString("-c"));}
 
             if(engine.getBoolean("-o")){ outputDir = engine.getString("-o");}
             else{outputDir = directoryName;}
 //        }catch (Exception e){
 //            System.out.println("Caught exception while setting parameters of "+this.getClass()+": "+e);
 //        }
     }
 
     
     /**
      * Derives a tagCount list for each fastq file in the fastqDirectory.
      *
      * @param keyFileS        A key file (a sample key by barcode, with a plate map included).
      * @param enzyme          The enzyme used to create the library (currently ApeKI or PstI).
      * @param fastqDirectory  Directory containing the fastq files (will be recursively searched).
      * @param outputDir       Directory to which the tagCounts files (one per fastq file) will be written.
      * @param maxGoodReads    The maximum number of barcoded reads expected in a fastq file
      * @param minCount        The minimum number of occurrences of a tag in a fastq file for it to be included in the output tagCounts file
      */
     public static void countTags(String keyFileS, String enzyme, String fastqDirectory, String outputDir, int maxGoodReads, int minCount) {
         BufferedReader br;
 //COUNTER VARIABLE
         String[] countFileNames = null;
 
         
         /* Grab ':' delimited key files */
         String[] tempFileList = keyFileS.split(":");
         String[] keyFileList = new String[2];
         
         if (tempFileList.length == 0){
         	System.out.println("No key file given");
        	keyFileList[0] = null;
        	keyFileList[1] = null;
         	
         } else if (tempFileList.length == 1){
         	System.out.println("Only one key file given");
         	keyFileList[0] = tempFileList[0];
         	keyFileList[1] = tempFileList[0];
         	
         } else {
         	keyFileList[0] = tempFileList[0];
         	keyFileList[1] = tempFileList[1];
         }
 
  
 // THIS IS WHERE FILE INPUT NEEDS TO BE ADJUSTED, TRY TO
 // CONTINUE TO USE THE DIRECTORY CRAWLER AND PARSE THE OUTPUT TO READ1
 // AND READ2
  
 
         File inputDirectory = new File(fastqDirectory);
         File[] rawFastqFiles = DirectoryCrawler.listFiles("(?i).*\\.fq$|.*\\.fq\\.gz$|.*\\.fastq$|.*_fastq\\.txt$|.*_fastq\\.gz$|.*_fastq\\.txt\\.gz$|.*_sequence\\.txt$|.*_sequence\\.txt\\.gz$", inputDirectory.getAbsolutePath());
 //                                                      (?i) denotes case insensitive;                 \\. denotes escape . so it doesn't mean 'any char' & escape the backslash
         
         
         /* ----- Get only r1smp files ----- */
         ArrayList<File> fastqFilesArray = new ArrayList<File>();
         if (rawFastqFiles.length == 0){
         	System.out.println("Couldn't find any files that end with \".fq\", \".fq.gz\", \".fastq\", \"_fastq.txt\", \"_fastq.gz\", \"_fastq.txt.gz\", \"_sequence.txt\", or \"_sequence.txt.gz\" in the supplied directory.");
         	return;
         }
         
         // Get array list of files with r1smp in them
         for (int i = 0; i < rawFastqFiles.length; i++){
         	String fileName = rawFastqFiles[i].getName();
         	
         	if (fileName.indexOf("r1smp") > -1){
         		fastqFilesArray.add(rawFastqFiles[i]);
         	}
         }
         // Cast to file array as following code relies on it
         File[] fastqFiles = fastqFilesArray.toArray(new File[fastqFilesArray.size()]);
         
         
         
         
         if(fastqFiles.length !=0 ){
             System.out.println("Using the following FASTQ files:");
             
             
 //COUNTS HOW MANY FILES THERE ARE IN THE INPUT            
             countFileNames = new String[fastqFiles.length];
             for (int i=0; i<fastqFiles.length; i++) {
                 countFileNames[i] = fastqFiles[i].getName().replaceAll
                     ("(?i)\\.fq$|\\.fq\\.gz$|\\.fastq$|_fastq\\.txt$|_fastq\\.gz$|_fastq\\.txt\\.gz$|_sequence\\.txt$|_sequence\\.txt\\.gz$", ".cnt");
 //                        \\. escape . so it doesn't mean 'any char' & escape the backslash                
                 System.out.println(fastqFiles[i].getAbsolutePath());
             }
         }
         
         int allReads=0, goodBarcodedReads=0;
         
         /* Loop through all of the fastqFiles */
         for(int laneNum=0; laneNum<fastqFiles.length; laneNum++) {
         	
         	/* Get second read file by name */
         	File read1 = fastqFiles[laneNum];
         	String read1Name = read1.getAbsolutePath();
         	int index = read1Name.indexOf("r1smp")+1;
         	
         	String read2Name = read1Name.substring(0, index) + "2" + read1Name.substring(index+1);
         	File read2 = new File(read2Name);
         	
         	/* Open output file, don't do work on input if corresponding output exists */
             File outputFile = new File(outputDir+File.separator+countFileNames[laneNum]);
             if(outputFile.isFile()){
                 System.out.println(
                         "An output file "+countFileNames[laneNum]+"\n"+ 
                         " already exists in the output directory for file "+fastqFiles[laneNum]+".  Skipping.");
                 continue;
             }
 
             TagCountMutable theTC=null;
             System.out.println("Reading FASTQ files: "+fastqFiles[laneNum]+", "+read2Name);
             String[] filenameField=fastqFiles[laneNum].getName().split("_");
             ParseBarcodeRead thePBR;  // this reads the key file and store the expected barcodes for this lane
  
             
 //handle keyfiles and enzymes
 //2 arrays for manually inputing multiple enzymes and keys for testing
 System.out.println("OLD Key file is:"+ keyFileS);            
 System.out.println("OLD enzyme is:"+ enzyme);
 //String[] hcEnzyme={"PstI","MspI"};
 String[] hcEnzyme={"PstI-MspI","MspI-PstI"};
 String[] hcKeyFiles={"GBS.key","GBS2.key"};
 
 if(filenameField[0].contains("1")){
 	keyFileS=hcKeyFiles[0];
 	enzyme=hcEnzyme[0];
 	System.out.println("NEW Key file is:" + keyFileS);
 	System.out.println("NEW enzyme is:"+ enzyme);}
 else{
 	keyFileS=hcKeyFiles[1];
 	enzyme=hcEnzyme[1];
 	System.out.println("NEW Key file is:"+ keyFileS);
 	System.out.println("NEW enzyme is:"+ enzyme);}
 //Debug           
 System.out.println("NEW Key file is:"+ keyFileS);
 System.out.println("NEW enzyme is:"+ enzyme);
 
             if(filenameField.length==3) {thePBR=new ParseBarcodeRead(keyFileS, enzyme, filenameField[0], filenameField[1]);}
             else if(filenameField.length==4) {thePBR=new ParseBarcodeRead(keyFileS, enzyme, filenameField[0], filenameField[2]);}  // B08AAABXX_s_1_sequence.txt.gz
             else if(filenameField.length==5) {thePBR=new ParseBarcodeRead(keyFileS, enzyme, filenameField[1], filenameField[3]);}
             else {
                 System.out.println("Error in parsing file name:");
                 System.out.println("   The filename does not contain either 3, 4, or 5 underscore-delimited values.");
                 System.out.println("   Expect: flowcell_lane_fastq.txt.gz OR flowcell_s_lane_fastq.txt.gz OR code_flowcell_s_lane_fastq.txt.gz");
                 System.out.println("   Filename: "+fastqFiles[laneNum]);
                 continue;
             }
             System.out.println("Total barcodes found in lane:"+thePBR.getBarCodeCount());
             if(thePBR.getBarCodeCount() == 0){
                 System.out.println("No barcodes found.  Skipping this flowcell lane."); continue;
             }
             String[] taxaNames=new String[thePBR.getBarCodeCount()];
             for (int i = 0; i < taxaNames.length; i++) {
                 taxaNames[i]=thePBR.getTheBarcodes(i).getTaxaName();
             }
 
             try{
                 //Read in qseq file as a gzipped text stream if its name ends in ".gz", otherwise read as text
                 if(fastqFiles[laneNum].getName().endsWith(".gz")){
                     br = new BufferedReader(new InputStreamReader(new MultiMemberGZIPInputStream(new FileInputStream(fastqFiles[laneNum]))));
                 }else{
                     br=new BufferedReader(new FileReader(fastqFiles[laneNum]),65536);
                 }
                 String sequence="", qualityScore="";
                 String temp;
 
                 try{
                     theTC = new TagCountMutable(2, maxGoodReads);
                 }catch(OutOfMemoryError e){
                     System.out.println(
                         "Your system doesn't have enough memory to store the number of sequences"+
                         "you specified.  Try using a smaller value for the minimum number of reads."
                     );
                 }
                 int currLine=0;
                 allReads = 0;
                 goodBarcodedReads = 0;
                 while (((temp = br.readLine()) != null) && goodBarcodedReads < maxGoodReads) {
                     currLine++;
                     try{
                         //The quality score is every 4th line; the sequence is every 4th line starting from the 2nd.
                         if((currLine+2)%4==0){
                             sequence = temp;
                         }else if(currLine%4==0){
                             qualityScore = temp;
                             allReads++;
                             //After quality score is read, decode barcode using the current sequence & quality  score
                             ReadBarcodeResult rr = thePBR.parseReadIntoTagAndTaxa(sequence, qualityScore, true, 0);
                             if (rr != null){
                                 goodBarcodedReads++;
                                 theTC.addReadCount(rr.getRead(), rr.getLength(), 1);
                             }
                             if (allReads % 1000000 == 0) {
                                 System.out.println("Total Reads:" + allReads + " Reads with barcode and cut site overhang:" + goodBarcodedReads);
                             }
                         }
                     }catch(NullPointerException e){
                         System.out.println("Unable to correctly parse the sequence and "
                         + "quality score from fastq file.  Your fastq file may have been corrupted.");
                         System.exit(0);
                     }
                 }
                 System.out.println("Total number of reads in lane=" + allReads);
                 System.out.println("Total number of good barcoded reads=" + goodBarcodedReads);
                 System.out.println("Timing process (sorting, collapsing, and writing TagCount to file).");
                 timePoint1 = System.currentTimeMillis();
                 theTC.collapseCounts();
                 theTC.writeTagCountFile(outputDir+File.separator+countFileNames[laneNum], FilePacking.Bit, minCount);
                 System.out.println("Process took " + (System.currentTimeMillis() - timePoint1) + " milliseconds.");
                 br.close();
             
         } catch(Exception e) {
             System.out.println("Catch testBasicPipeline c="+goodBarcodedReads+" e="+e);
             e.printStackTrace();
             System.out.println("Finished reading "+(laneNum+1)+" of "+fastqFiles.length+" sequence files.");
         }
     }
     }
     @Override
     public ImageIcon getIcon(){
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String getButtonName() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String getToolTipText() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
 }
