 package net.maizegenetics.gbs.pipeline;
 
 import java.awt.Frame;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.TreeMap;
 import java.text.DecimalFormat;
 import java.io.*;
 import java.util.*;
 
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
 
 /** 
  * Derives a tagCount list for each fastq file in the input directory.
  *
  * Keeps only good reads having a barcode and a cut site and no N's in the
  * useful part of the sequence.  Trims off the barcodes and truncates sequences
  * that (1) have a second cut site, or (2) read into the common adapter.
  * 
  */
 public class FastqPairedEndToTagCountPlugin extends AbstractPlugin {  
     static long startTime;		// reporter variable holding system time in milliseconds
     private ArgsEngine engine = null;
     private Logger logger = Logger.getLogger(FastqPairedEndToTagCountPlugin.class);
     String directoryName=null;
     String keyfile=null;
     String enzyme = null;
     int maxGoodReads = 0;	// can be set by user args, left at 0 means process entire file
     int minCount =1;	// can be set by user args, left at 1 means count everything
     String outputDir=null;
     protected static TreeMap <String, Integer> barcodePairs = new TreeMap<String, Integer>();
     protected static ArrayList<String> summaryOutputs = new ArrayList<String>();
    
 
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
             + " -s  Max good reads per lane. (Optional. Default will try to process entire file).\n"
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
                 engine.add("-i", "--input-directory", true);
                 engine.add("-k", "--key-file", true);
                 engine.add("-e", "--enzyme", true);
                 engine.add("-s", "--max-reads", true);
                 engine.add("-c", "--min-count", true);
                 engine.add("-o", "--output-file", true);
                 engine.parse(args);
             }
 
             if (engine.getBoolean("-i")) { directoryName = engine.getString("-i");}
             else{ printUsage(); throw new IllegalArgumentException("Please specify the location of your FASTQ files."); }
 
             if(engine.getBoolean("-k")){ keyfile = engine.getString("-k");}
             else{ printUsage(); throw new IllegalArgumentException("Please specify a barcode key file.");}
 
             if(engine.getBoolean("-e")){ enzyme = engine.getString("-e"); }
             else{ 
                 System.out.println("No enzyme specified.  Using enzyme listed in key file.");
             }
         
             if(engine.getBoolean("-s")){ maxGoodReads = Integer.parseInt(engine.getString("-s"));}
 
             if (engine.getBoolean("-c")) { minCount = Integer.parseInt(engine.getString("-c"));}
 
             if(engine.getBoolean("-o")){ outputDir = engine.getString("-o");}
             else{outputDir = directoryName;}
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
         
     	BufferedReader br1;
         BufferedReader br2;;
 
         String[] countFileNames = null;  // counter variable
         TreeMap <String, Integer> tagCount = new TreeMap<String, Integer>();  // stores and counts tags, master list
         
         // stores paired sequences to write to file, value links to key in lanetrack
         TreeMap <String, Integer> pairCount = new TreeMap<String, Integer>(); 
         
         // holds lane and count information linked to key in pairCount
         TreeMap <String, Integer> laneTrack = new TreeMap<String,Integer>();	
         
         ArrayList <String> hashFileNames = new ArrayList<String>(); // stores names of files resulting from HashMap output
         ArrayList <String> flowcellLane = new ArrayList<String>(); //list of all flowcell and lane combinations
         
         System.out.println(enzyme);
         /* Grab ':' delimited key files */
         String[] keyFileList = parseFlagArgs(keyFileS, ":", "Key File");
         String[] enzymeList = parseFlagArgs(enzyme, ":", "enzymes");
         
         summaryOutputs.add("Lane \t Total Reads \t Forward Only \t Reverse Only \t Both");
 
         File inputDirectory = new File(fastqDirectory);
         File[] fastqFiles = DirectoryCrawler.listFiles("(?i).*\\.fq$|.*\\.fq\\.gz$|.*\\.fastq$|.*_fastq\\.txt$|.*_fastq\\.gz$|.*_fastq\\.txt\\.gz$|.*_sequence\\.txt$|.*_sequence\\.txt\\.gz$", inputDirectory.getAbsolutePath());
         int allReads=0, goodBarcodedReads=0, goodBarcodedForwardReads=0, goodBarcodedReverseReads=0;
         int numFastqFiles = fastqFiles.length;  //number of files   
         int indexStartOfRead2 = numFastqFiles/2;  // index of where paired file should start, also number of pairs
         
         if(fastqFiles.length !=0 ){
         	Arrays.sort(fastqFiles);
             System.out.println("Using the following "+numFastqFiles+" FASTQ files:");
                       
             //Counts number of input files            
             countFileNames = new String[fastqFiles.length];
             for (int i=0; i<fastqFiles.length; i++) {
                 countFileNames[i] = fastqFiles[i].getName().replaceAll
                     ("(?i)\\.fq$|\\.fq\\.gz$|\\.fastq$|_fastq\\.txt$|_fastq\\.gz$|_fastq\\.txt\\.gz$|_sequence\\.txt$|_sequence\\.txt\\.gz$", ".cnt");
 //                        \\. escape . so it doesn't mean 'any char' & escape the backslash    
                 System.out.println(fastqFiles[i].getAbsolutePath());
             }
             
             // Sets unique flowcell Lane combinations
             for (int i=0; i<fastqFiles.length; i++) {
             	String [] fl = fastqFiles[i].getName().split("_");	// split filename by underscore
             	String tempFL = fl[1]+"-"+fl[3];
             	
             	if(flowcellLane.indexOf(tempFL)==-1){
             		flowcellLane.add(tempFL);
             	}
             }  
         }
         
         
         
         
         // check for even number of files
         checkForPairs(indexStartOfRead2);
                          
 		/* sets mutildimensional array for 
 		 * [x][][] 2 bays for forward and reverse reads
 		 * [][x][] number of files expected for each directional read
 		 * [][][y] will hold the parsed elements of the file name
 		 */
 		String [][][] fileReadInfo=new String[2][indexStartOfRead2][5]; 
 		
 		String[][][] filenameField= new String[2][numFastqFiles][];
 
 		int fileNum=0;
 		/* parses file name into array elements that correspond to reads and expected pairing
 		 * where the first file would be paired with the file that is half of the total number 
 		 * of files.
 		 * 
 		 * The outter loop controller is set to 2 because there should not be more than two 
 		 * directional reads, forward and reverse.
 		 */
 		for(int read=0; read<2; read++) {
 			int loopController, setStart; //control loops and arrays
 			int fileController=0; // resets to 0 so files are copied in correct array
 			
 			//set conditions for the loop
 			if(read==0){
 				setStart=0;
 				loopController=indexStartOfRead2;
 			}
 			else{
 				setStart=indexStartOfRead2;
 				loopController=numFastqFiles;
 			}
 				
 				for(fileNum=setStart; fileNum<loopController; fileNum++) {			
 					//following block could be set as separate private method
 					File outputFile = new File(outputDir+File.separator+countFileNames[fileNum]);
 						if(outputFile.isFile()){
 				            System.out.println(
 				                    "An output file "+countFileNames[fileNum]+"\n"+ 
 				                    " already exists in the output directory for file "+fastqFiles[fileNum]+".  Skipping.");
 							fileController++;
 				            continue;
 				        }
 					System.out.println("Reading FASTQ file: "+fastqFiles[fileNum]);//print
 					filenameField[read][fileNum]=fastqFiles[fileNum].getName().split("_");
 					System.arraycopy(filenameField[read][fileNum], 0, fileReadInfo[read][fileController], 0, filenameField[read][fileNum].length);
 					fileController++;
 					}
 		}
 		
 			fileNum=0;
 
 			keyFileList[1]=modifyKey2File(keyFileList[1]);
 
  			/* 
  			 * Reads the key file and store the expected barcodes for a lane.
  			 * Set to a length of 2 to hold up to two key files' worth of information.
  			 * The convention will be that the forward read is [0] and the reverse
  			 * read is[1]
  			 */
             ParseBarcodeRead [] thePBR = new ParseBarcodeRead [2];  
             int uniqueID = 0;	// acts as a unique values across different lane data files for assignment of uniqueness to tag map
             ArrayList <String> laneNumAL = new ArrayList<String>(); //track lane numbers
             ArrayList <String> pairedFileNames = new ArrayList<String>(); //track lane names
             String laneID=null;
             String flowcellAndLane=null;
             String tagsByTaxaFile = "Tags_by_Taxa.txt";
 
             for(int b=0;b<indexStartOfRead2;b++){
 
 				for(int i=0;i<thePBR.length;i++){
 					int which = 1;	// set controller to detect 2nd set of arguments
 					
 					// if true, set controller to detect 1st set of arguements
 					if(i==0 || i%2==0){
 						which = 0;
 					}		
 					
 					if(fileReadInfo[0][b][0]!=null && fileReadInfo[i][b].length==5) {
 						thePBR[i]=new ParseBarcodeRead(
 								keyFileList[which], enzymeList[which], fileReadInfo[i][b][1], fileReadInfo[i][b][3]);
 					}
 					else {
 					 printParsingError();
 					 continue;
 					}
 				}
 		
 				for(int i=0;i<thePBR.length;i++){
 					System.out.println("\nTotal barcodes found in lane:"+thePBR[i].getBarCodeCount());
 					if(thePBR[i].getBarCodeCount() == 0){
 		                System.out.println("No barcodes found.  Skipping this flowcell lane " +fileReadInfo[i][b][3]+"."); continue;
 		            }
 				}
 	
 				try{
 	                //Read in fastq file as a gzipped text stream if its name ends in ".gz", otherwise read as text
 	                if(fastqFiles[b].getName().endsWith(".gz")){
 	                    br1 = new BufferedReader(new InputStreamReader(
 	                    						new MultiMemberGZIPInputStream(
 	                    						new FileInputStream(fastqFiles[b]))));
 	                    br2 = new BufferedReader(new InputStreamReader(
         										new MultiMemberGZIPInputStream(
         										new FileInputStream(fastqFiles[b+indexStartOfRead2]))));
 	                    
 	              System.out.println("\nIntermediate file: "+fastqFiles[b].getName() + "-" + fastqFiles[b+indexStartOfRead2].getName());      
 	                }else{
 	                    br1=new BufferedReader(new FileReader(fastqFiles[b]),65536);
 	                    br2=new BufferedReader(new FileReader(fastqFiles[b+indexStartOfRead2]),65536);
 	                }
 	                                            
 	                // clear all counters and controllers for the upcoming section
 	                ReadBarcodeResult [] rr = new ReadBarcodeResult [2];
 	                allReads = 0;
 	                goodBarcodedReads = 0;	// used if a maximum number of total good reads is set
 	                goodBarcodedForwardReads = 0;
 	                goodBarcodedReverseReads = 0;
 	                
 	                String sequenceF="", sequenceR="", qualityScoreF="", qualityScoreR="";
 	                String tempF, tempR;
 	                String tempSeqF,tempSeqR ,tempIdF , tempIdR=null;
 	                String concatenation=null;
 	                String hiseqID=null; // captures reverse ID from raw sequence file
 	                String outputLaneFilename = fastqFiles[b].getName() + "-" + fastqFiles[b+indexStartOfRead2].getName()+".txt";
 	                
 	                int bothGood=0;	// keeps count of good pairs within a lane of data
 	                int currLine=0;
 	                int idLine = 1;  // designates lines ID information should be found starting with the first line
 	                
 	                pairedFileNames.add(outputLaneFilename);	// logs filename for processing later
 
 	                System.out.println("Begin reading raw sequence files");
 	                resetTime();	//reporter
 	                
 	                while (((tempF = br1.readLine()) != null && (tempR = br2.readLine()) != null) && 
 	                		(maxGoodReads==0 || bothGood<maxGoodReads)) {
 	                	currLine++;
 	                	
 	                    try{
 	                        //The quality score is every 4th line; the sequence is every 4th line starting from the 2nd.
 	                        if(currLine==idLine){
 	                        	hiseqID = tempR;
 	                        	idLine+=4;
 	                        }else if((currLine+2)%4==0){
 	                            sequenceF = tempF;
 	                            sequenceR = tempR;
 	                        }else if(currLine%4==0){
 	                            qualityScoreF = tempF;
 	                            qualityScoreR = tempR;
 	                            allReads += 2;
 	                            /*
 	                             * Decode barcode using the current sequence & quality score.  The forward and reverse 
 	                             * sequences are processed with similar but different methods.  The forward is more stringent
 	                             * while the reverse is lenient.  To run the reverse with the same criteria as the forward, 
 	                             * comment-out the line commented as lenient and un-comment the line marked stringent for rr[1]
 	                             * in the next few lines (directly below and lines 5+6 in the first if-statement.
 	                             */
 	                            rr[0] = thePBR[0].parseReadIntoTagAndTaxa(sequenceF, qualityScoreF, true, 0,64);	// stringent
 	                            rr[1] = thePBR[1].forceParseReadIntoTagAndTaxa(sequenceR, qualityScoreR, false, 0,64);	// lenient,accepts N bases
 	                            //rr[1] = thePBR[1].parseReadIntoTagAndTaxa(sequenceR, qualityScoreR, true, 0,64);	// stringent
 	                            
 	                            if (rr[0] != null && rr[1] !=null){
 	                                goodBarcodedReads+=2;	
 	                                bothGood++;	// increment the lane counter
 	                                uniqueID++;	//increment the unique couter
 	                                tempSeqF=rr[0].toString().substring(0,64);
 	                                tempIdF = rr[0].toString().substring(65);
 	                                tempSeqR=rr[1].paddedSequence;  // lenient, correctly handles Ns present in sequence
 	                                //tempSeqR=rr[1].toString().substring(65);  // uncomment if stringent is being used
 
 	                                tempIdR = rr[1].toString().substring(65);
 	                                
 	                                concatenation=stitch(tempSeqF, tempSeqR, tempIdF, tempIdR);
 	                                
 	                                String idF1[]=tempIdF.split(":");
 	                            	String idR2[]=tempIdR.split(":");
 	                            	laneID = idF1[3];	// sets the flowcell lane identification
 	                            	flowcellAndLane= idF1[2]+"-"+idF1[3];
 	                            	
 	                            	// check for the lane ID and add if not present
 	                            	if(laneNumAL.isEmpty()){
 	                            		laneNumAL.add(laneID);
 	                            	}
 	                            	else if(laneNumAL.contains(laneID)){}
 	                            	else{
 	                            		laneNumAL.add(laneID);
 	                            	}
 	                                
 	                            	// check for and increment paired tag sequence to the master tag map, otherwise add it as new
 	                                if(tagCount.containsKey(tempSeqF+tempSeqR)){
 	                                	tagCount.put(tempSeqF+tempSeqR, tagCount.get(tempSeqF+tempSeqR)+1);
 	                                }else{
 	                                	tagCount.put(tempSeqF+tempSeqR, 1);
 	                                }
 	                                
 	                                /*
 	                                 * check for and increment paired tag sequence, otherwise add it as new.  The tag
 	                                 * gets paired with a unique ID that is used to help key to the laneTrack Map
 	                                 */
 	                                if(pairCount.containsKey(concatenation)){
 	                                	if(laneTrack.containsKey(String.valueOf(pairCount.get(concatenation))+":"+flowcellAndLane)){
 	                                		laneTrack.put(String.valueOf(pairCount.get(concatenation))+":"+flowcellAndLane, laneTrack.get(String.valueOf(pairCount.get(concatenation))+":"+flowcellAndLane)+1);
 	                                	}else{
 	                                		laneTrack.put(String.valueOf(pairCount.get(concatenation))+":"+flowcellAndLane, 1);
 	                                	}
 	                                }else{
 	                                	pairCount.put(concatenation, uniqueID);
 	                                	laneTrack.put(String.valueOf(uniqueID)+":"+flowcellAndLane, 1);
 	                                }
 	                            }
 	                            /*
 	                             * The following else-ifs only increment the reporter variables for
 	                             * the user's benefit to being able to see the breakdown of pairs vs
 	                             * single (forward or reverse) tags that were detected. 
 	                             */
 	                            else if (rr[0] != null){
 	                                goodBarcodedForwardReads++;
 	                                    }
 	                            else if (rr[1] != null){
 	                                goodBarcodedReverseReads++;
 	                            }
 	                            // print to console so user gets a snapshot of what's happening and where in the file
 	                            if (allReads % 10000000 == 0) {
 	                            	reportStats(laneID,bothGood, goodBarcodedForwardReads, goodBarcodedReverseReads, 
 	                            			goodBarcodedReads, allReads);
 	                            }	
 	                        }
 	                    }catch(NullPointerException e){
 	                        System.out.println("Unable to correctly parse the sequence and "
 	                        + "quality score from fastq file.  Your fastq file may have been corrupted.");
 	                        System.exit(0);
 	                    }
 	                    
 	                } 
 	            reportStats(laneID,bothGood, goodBarcodedForwardReads, goodBarcodedReverseReads, 
 	            			goodBarcodedReads, allReads);
 	            reportTime();	//reporter
 	 
                 br1.close();
                 br2.close();
               
                 // following prints what should be considered an intermediate output file for each lane
                 printTagsAndTaxaByLane(pairCount, laneTrack, flowcellLane, outputDir, outputLaneFilename);
                 
                 pairCount.clear();
                 laneTrack.clear();
                 
                 fileNum++;
                 
 		        } catch(Exception e) {
 		            System.out.println("Catch testBasicPipeline c="+goodBarcodedReads+" e="+e);
 		            e.printStackTrace();
 		            System.out.println("Finished reading "+(fileNum+1)+" of "+fastqFiles.length+" sequence files.");
         			}
 			}
             /*
              * If the minCount variable was not set by the user or set to an invalid value, default it back to 1
              * to collect all detected tags and taxa.  If minCount is valid, use it to further refine output
              */
             if(minCount<1){
             	System.out.println("minCount flag is set to 0 or negative value.  These are invalid.  Resetting to 1 and continuing...");
             	minCount=1;
             }
             else if(minCount>1){
             System.out.println("minCount is "+minCount);
             excludeTagCount(tagCount, outputDir, minCount);
             }
 
             printTagCountMap(tagCount, outputDir);	// print master tag list to file
 
         	pairedFileNames.trimToSize();
         	
         	// while there are still files to process, keep combining them to the final output
         	while(pairedFileNames.size()>0){
         		combineTBT(tagsByTaxaFile, outputDir, pairedFileNames, tagCount);
         	}
         	
         	addEmptyLanesToTBT(tagsByTaxaFile, outputDir, flowcellLane);
 
         	tagCount.clear();  
         	
         	printIds(outputDir);	// print barcode ID summary
 
         	printListToFile(outputDir+File.separator+"Summary_Stats.txt",summaryOutputs);	// print final summary info to file
     }
     
     /**
      * Removes tags from a master map based on a minimum observation cut-off value.  Anything 
      * below the value the user sets from the commandline is removed.  If no minum value is 
      * set, this method is not called as it is assumed the user wants to collect everything
      * @param tcm Master map of tags observed
      * @param directoryInfo System directory information
      * @param min The minimum value of observations required for a tag to continue past this point
      */
     private static void excludeTagCount(TreeMap <String, Integer> tcm, String directoryInfo, int min){
     	resetTime();	//reporter
     	System.out.println("\nexcludeTagCount: Removing tags less than set minimum value");
 
     	ArrayList <String> toRemove = new ArrayList<String>();	//a temporary list of values to delete
     	int size = 0;
     	
     	/*
     	 * Look at all the values in the map. If the value fall short of the minimim cut-off
     	 * add the key to the remove list
     	 */
     	for(String t : tcm.keySet()){
     		String key = t.toString();
     		int value = tcm.get(key);
     		
     		if(value<min){
     			toRemove.add(key);
     		}
     	}
     	
     	toRemove.trimToSize();
     	size=toRemove.size();
     	
     	//  Remove tags from the master map that don't meet the minimum count requirement
     	for(int i=0; i<size;i++){
     		tcm.remove(toRemove.get(i));
     	}
     	reportTime();	//reporter
     }
     
     /**
      * Combines tags and barcode information with lane:count values from across multiple intermediate files.
      * Tags are checked against a master tag list before being counted as valid, and a total barcode ID summary
      * is generated while the tags and taxa information is being compiled
      * @param fname Name of file to write to
      * @param directoryInfo System directory information
      * @param names A list of file names that are the source of data to compile
      * @param tcm A master list of valid tags that should be written to final outputs
      */
     private static void combineTBT(String fname, String directoryInfo, ArrayList<String> names, TreeMap<String, Integer> tcm){
     	resetTime();	//reporter
     	System.out.println("\ncombineTBT: Combining tags by taxa and lane");
     	
     	names.trimToSize();
     	File tbtName = new File(directoryInfo+File.separator+fname);	
     	//File tbtName = new File(fname);
     	BufferedReader b1;
     	BufferedReader b2;
     	String line;
     	int lastTab;
     	TreeMap<String, String> t1=new TreeMap<String, String>();
     	int alsize=names.size();
     	boolean removeOne=false;
     	
     	
     	try{
 	    	// check to see if a TBT file has been started
 	    	if(tbtName.exists()){
 	    		// process one more file
 	    		b1= new BufferedReader(new InputStreamReader(new FileInputStream(tbtName)));
 	    		b2= new BufferedReader(new InputStreamReader(new FileInputStream(directoryInfo+File.separator+names.get(alsize-1))));
 	    		removeOne=true;
 	    	}else if(alsize==1){
 	    		// set reader to process only one file
 	    		b1= new BufferedReader(new InputStreamReader(new FileInputStream(directoryInfo+File.separator+names.get(alsize-1))));
 	    		b2= null;
 	    	}else{
 	    		// process last 2 files, should only come here once, first if more than 2 files
 	    		b1= new BufferedReader(new InputStreamReader(new FileInputStream(directoryInfo+File.separator+names.get(alsize-2))));
 	    		b2= new BufferedReader(new InputStreamReader(new FileInputStream(directoryInfo+File.separator+names.get(alsize-1))));
 	    	}
 	    	
 	    	while((line=b1.readLine())!=null){
 	    		lastTab=line.lastIndexOf("\t");
 	    		
 	    		// check against master list of tags before adding to base compare map
 	    		if(tcm.containsKey(line.substring(0, line.indexOf("\t")))){
 	    			//t1.put(line.substring(0,lastTab), line.substring(lastTab+1));
 	    			t1.put(line.substring(0,lastTab), line.substring(lastTab+1));
 	    		}
 	    	}
 	    	b1.close();
 	    	line=null;
 	    	
 	    	// adds content from 2nd file to Map with contents of first file
 	    	while((line=b2.readLine())!=null){
 	    		lastTab=line.lastIndexOf("\t");
 	    		ArrayList <String> together = new ArrayList<String>();
 	    		
 	    		/*
 	    		 * If tag and barcode exist, get the lane(s) and value(s) associated with them, add new
 	    		 * lane(s) and value(s) from line being processed.  Concatenate the lane(s) and value(s)
 	    		 * with formatting, append to tag and barcode, then overwrite the previously exsiting
 	    		 * tag by taxa by count information with newly processed string.
 	    		 */
 	    		if(t1.containsKey(line.substring(0,lastTab))){
 	    			String [] existing= t1.get(line.substring(0,lastTab)).toString().split("\t");  // get exisitng lane:count
 	 				// retrieves lane:value string skipping formatting "\t"
 	    			for(int i=0;i<existing.length;i++){
 	 					if(i%2==0){
 	 						together.add(existing[i]);
 	 					}
 	 				}
 	 	
 	    			String [] additions = line.substring(lastTab+1).split("\t"); // get new lane:count info to add
 	    			
 	    			// retrieves lane:value string skipping formatting "\t"
 	    			for(int j=0;j<additions.length;j++){
 	 					if(j%2==0){
 	 						together.add(additions[j]);
 	 					}
 	 				}
 	    			
 	    			together.trimToSize();
 	    			String [] togetherArray = together.toArray(new String[together.size()]); 	//populate array with lane:count values
 	    			Arrays.sort(togetherArray);
 	    			String togetherLine = togetherArray[0];	//assign first set of values, there should always be at least one
 	    			
 	    			// concatenate with formatting the rest of the lane:count values
 	    			for(int k=1;k<togetherArray.length;k++){
 	    				togetherLine = togetherLine+"\t"+togetherArray[k];
 	    			}
 	    			t1.put(line.substring(0,lastTab), togetherLine);	// overwrite previously exisintg information with new values
 	    		}else{
 	    			// check against master list of tags before adding to base compare map
 		    		if(tcm.containsKey(line.substring(0, line.indexOf("\t")))){
 		    			t1.put(line.substring(0,lastTab), line.substring(lastTab+1));
 		    		}    		
 	    		}
 	    	}
 	    	b2.close();   	
     	}catch (IOException e) {
        	 System.out.println(e.getMessage());
     	}
     	
     	try{
     	PrintWriter tbtOut = new PrintWriter(new BufferedWriter(new FileWriter(tbtName, false)));
     	
     	int counter=0;
     	// print map values to file and log barcode information
     	for(String tbt:t1.keySet()){
     		String key = tbt.toString();
     		String value = t1.get(key);
     		tbtOut.println(key+"\t"+value);
     		logIds(key, value);	//	logs the barcode ids
     		counter++;
     		// send update to console so user gets status update
         	if(counter%10000000 == 0){
         		System.out.println(counter+" lines written to file");
         	}
     	}
     	tbtOut.close();
     	t1.clear();
         System.out.println(counter+" lines written to file");
 
     	}catch (IOException e) {
        	 System.out.println(e.getMessage());
         }
     	
     	// Adjusts ArrayList size to impact calling loop
     	if(names.size()==1){
     		names.clear();
     	}
     	else if(removeOne){
     		names.remove(alsize-1);    
     	}else{
     		names.remove(alsize-1);
     		names.trimToSize();
     		if(names.size()==1){
     			names.clear();
     		}else{
         		names.remove(alsize-1); 
         		names.trimToSize();
     		}
     	}
     	reportTime();	//reporter
     }
     
     
     private static void addEmptyLanesToTBT(String fname, String directoryInfo, ArrayList<String> fcL){
     	resetTime();	//reporter
     	System.out.println("\nAdding empty lanes to tags (formatting)");
     	
     	File name = new File(directoryInfo+File.separator+fname);
     	
     	BufferedReader br;
     	String line;
     	String tKey;
     	String tValue;
     	int sum=0;
     	
     	fcL.trimToSize();
     	int fcLSize= fcL.size();
     	ArrayList <String> tempFCL = new ArrayList<String>();
     	ArrayList <String> tempValue = new ArrayList<String>();
     	TreeMap <String, String> tbtFormatted = new TreeMap <String, String>();
     	
     	try{
     		// check to see if file exists
         	//if(name.exists()){
         		br= new BufferedReader(new InputStreamReader(new FileInputStream(name)));
         	//}
         	
         	while((line=br.readLine())!=null){
         		tempFCL=new ArrayList<String>(fcL);
         		String [] breakdown = line.split("\t");
         		tKey = breakdown[0]+"\t"+breakdown[1];
         		int fclLength = breakdown.length;
         		
         		if((fclLength-2)==fcLSize){
         			for(int i=2;i<fclLength;i++){
             			sum = sum +Integer.parseInt(breakdown[i].substring(breakdown[i].indexOf(":")+1)); 
             		}
             		
             		tValue=String.valueOf(sum);
             		for(int i=2;i<fclLength;i++){
             			tValue = tValue+"\t"+breakdown[i].substring(breakdown[i].indexOf(":")+1); 
             		}
 
             		tbtFormatted.put(tKey, tValue);
             		tKey=null;
             		tValue=null;
             		sum=0;
         		}else{
         	//System.out.println(fclLength);
         	//System.out.println(fcLSize);
         			for(int i=2; i<(fclLength);i++){
         				String existingFCL = breakdown[i].substring(0, breakdown[i].indexOf(":"));
         	//System.out.println(existingFCL);
         	//System.out.println(tempFCL);
         				if(tempFCL.contains(existingFCL)){
         	//System.out.println(tempFCL.indexOf(existingFCL));
         					tempFCL.remove(tempFCL.indexOf(existingFCL));
         					tempFCL.trimToSize();
         				}
         			}
         			
         		//	if(tempFCL.isEmpty()){
             	//		continue;
             	//	}else{
             			for(int i=2; i<(fclLength);i++){
             				tempValue.add(breakdown[i]);
             			}
             			
             			for(int i=0; i<tempFCL.size();i++){
             				String temp = tempFCL.get(i).toString();
             				temp = temp + ":0";
             				tempValue.add(temp);
             			}
             	//	}
         		tempFCL.clear();
 
         		tempValue.trimToSize();
         		String [] tv = tempValue.toArray(new String[tempValue.size()]);
         		tempValue.clear();
         		Arrays.sort(tv);
         		int size = tv.length;
         		
         		for(int i=0;i<size;i++){
         			sum = sum +Integer.parseInt(tv[i].substring(tv[i].indexOf(":")+1)); 
         		}
         		
         		tValue=String.valueOf(sum);
         		for(int i=0;i<size;i++){
         			tValue = tValue+"\t"+tv[i].substring(tv[i].indexOf(":")+1); 
         		}
         		
         		tbtFormatted.put(tKey, tValue);
         		tKey=null;
         		tValue=null;
         		sum=0;
         		}
         	}
         }catch (IOException e) {
           	 System.out.println(e.getMessage());
        	}
     	
     	try{
         	PrintWriter tbtOut = new PrintWriter(new BufferedWriter(new FileWriter(name, false)));
         	
         	String forPrinting = "Tag Sequence"+"\t"+"Barcode Taxa"+"\t"+"Tag by Taxa Sum";
         	fcL.trimToSize();
         	for(int i=0;i<fcL.size();i++){
         		forPrinting = forPrinting + "\t" + fcL.get(i).toString();
         	}
         	tbtOut.println(forPrinting);
         	
         	int counter=0;
         	// print map values to file and log barcode information
         	for(String tbt:tbtFormatted.keySet()){
         		String key = tbt.toString();
         		String value = tbtFormatted.get(key);
         		tbtOut.println(key+"\t"+value);
         		counter++;
         		// send update to console so user gets status update
             	if(counter%10000000 == 0){
             		System.out.println(counter+" lines written to file");
             	}
         	}
         	tbtOut.close();
         	tbtFormatted.clear();
             System.out.println(counter+" lines written to file");
 
         	}catch (IOException e) {
            	 System.out.println(e.getMessage());
             }
     	
     	reportTime();	//reporter
     }    
     
     
     /**
      * Parses within element of the input to sum and return a value.
      * @param v String array containing lane and count information in the format "lane:count".
      * @return Returns an int of the total number of observations from all lanes fed into method via string array.
      */
     private static int sumIDs(String [] v){
 		int numElements=v.length;
 		int count=0;
 		
 		// parse lane and tally information for each lane
 		for(int i=0;i<numElements;i++){
 			String [] laneNumSplit = v[i].split(":"); 
 			count+=Integer.parseInt(laneNumSplit[1]);	// add count to itself plus the value from the lane to count
 		}
 		
 		return count;
 	}
 
 	/**
      * Logs the barcode / taxa information to a storage variable.  
      * @param k A string that contains the barcode information in the 2nd element
      * when split by a "\t" delimeter.
      * @param v The lane and count inforamtion stored as a string.  It can contain
      * single or multiple lane and count information in the format lane:count"\t"lane:count...
      */
     private static void logIds(String k, String v){
 		//String [] kSplit = k.split("\t");
 		String [] vSplit = v.split("\t");
 		
 		//checks for the barcode, if it does not exist, adds it for the first time
 		// if it does, then adjusts the associated value
 	//if(barcodePairs.containsKey(kSplit[1])){
 		if(barcodePairs.containsKey(k)){
 			barcodePairs.put(k, barcodePairs.get(k)+sumIDs(vSplit));
 		}else{
 			barcodePairs.put(k, sumIDs(vSplit));
 		}
 	}
 
     /**
      * Prints the TreeMap of totaled tag counts to "Tags_Totaled.txt" file.  The totals from 
      * here are tallied separately from the "Tags by taxa" output for error checking.  
      * Also prints an update to the console so user can track progress.
      * @param tcm TreeMap of the tag sequence and number of times each was observered.
      * @param directoryInfo System file directory information.
      */
 	private static void printTagCountMap(TreeMap <String, Integer> tcm, String directoryInfo){
 		resetTime();	//reporter
     	System.out.println("\nprintTagCountMap: Printing tag counts to file");
 
     	int tempCount=0;
     	
     	try {
         	PrintWriter out = new PrintWriter(
             		new BufferedWriter(
             				new FileWriter(
             						directoryInfo+File.separator+"Tags_Totaled.txt", true)));
         tempCount=0;
         	
         	for(String t: tcm.keySet()){
         		String key = t.toString();
         		int value = tcm.get(t);
         		out.println(key+"\t"+value);
         		tempCount++;
             	// send update to console so user gets status update
             	if(tempCount%2000000 == 0){
             		System.out.println(tempCount+" lines written to file");
             	}
         	}
             out.close();		// close PrintWriter
         }catch (IOException e) {
         	 System.out.println(e.getMessage());
         }
     	reportTime();	//reporter
     }
    
 	/**
 	 * Prints the tags and taxa information from Maps to a file.
 	 * @param pc Stored tag information for tags by taxa association purposes. Key is tag plus barcode
 	 * information, value is a unique number. 
 	 * @param lt Stored lane and count information.  Key is a string representation of the unique number
 	 *  that is associated with a tag, a ":" delimeter, and which lane the value information comes from.
 	 *  The value is a count of the number of observations a particular tags ocurred in a lane.
 	 * @param al A list of all combinations of flowcell and lanes for all files being processed
 	 * @param directoryInfo System directory information
 	 * @param name Name of the new file
 	 */
     private static void printTagsAndTaxaByLane(TreeMap <String, Integer> pc, 
     		TreeMap<String, Integer> lt, ArrayList <String> al, String directoryInfo, String name){
     	resetTime();	//reporter
     	System.out.println("\nprintTagsAndTaxaByLane: Printing tag and taxa information to file");
 
     	int tempCount=0;
     	al.trimToSize();
     	String arrayOfFlowcellLanes[] =al.toArray(new String[al.size()]);	//convert ArrayList to String of arrays
     	int aolSize = arrayOfFlowcellLanes.length;	// loop controller
     	String filename = name;
     	
     	try {
         	PrintWriter out = new PrintWriter(
             		new BufferedWriter(
             				new FileWriter(
             						directoryInfo+File.separator+filename, true)));
         tempCount=0;
         String tbtOutput=null;
         	//  iterate through the pc map
         	for(String tag: pc.keySet()){
         		String key1 = tag.toString();
         		String value1 = pc.get(tag).toString();       
         		
         		tbtOutput=key1;	//assign the tag and barcode (key1) information to output string
         		
         		// Go through all possible lanes looking for values for the key
         		for(int i=0;i<aolSize;i++){
         			/*
         			 * Should always be true as no lt value should have been set without a pc being set
         			 * first.  
         			 */
         			if(lt.containsKey(value1+":"+arrayOfFlowcellLanes[i])){
         				// concatenate and format the tag information and respective lane and count values
         				tbtOutput=tbtOutput+"\t"+arrayOfFlowcellLanes[i]+":"+lt.get(value1+":"+arrayOfFlowcellLanes[i]).toString();
         			}
         		}
         		
         		out.println(tbtOutput);	// print new tag by taxa information to file
           		tempCount++;
             	// send update to console so user gets status update
             	if(tempCount%1000000 == 0){
             		System.out.println(tempCount+"lines written to file");
             	}
         	}
             out.close();		// close PrintWriter
         }catch (IOException e) {
         	 System.out.println(e.getMessage());
         }
     	reportTime();	// reporter
     }
 
     /**
      * This is a helper method that adds a line of summary 
      * information to a list that will be printed to a
      * summary file for user analysis.  
      * @param line A string of summary information.
      */
 	private static void summarizeCounts(String line){
     	summaryOutputs.add(line);
     }
     
 	/**
 	 * Modifies an set of barcodes from a file and places them in a new
 	 * file.  Modifications occur in other methods 
 	 * but are called upon here.  The oringal set of barcodes is also copied
 	 * over to the new file.
 	 * @param name Name of the file containing the original barcodes
 	 * @return The name of the new file containing orignial and modified barcodes
 	 */
     private static String modifyKey2File(String name){
     	String newName=name+"_mod";
     	String keyContents;
     	ArrayList <String> modified = new ArrayList<String>();
     	int currentLine = 0;
     	int barcodeLength = 0;
     	
     	copyOriginalKeyFile(name, newName);	// copies oringal barcodes to new file
     	
     	try{
     		BufferedReader inputKey = new BufferedReader(new InputStreamReader(
     				new FileInputStream(name)));
     	
 	    	while((keyContents = inputKey.readLine()) != null){
 	    		currentLine++;
 	    		if(currentLine==1){
 	    			System.out.println("Begin modifications to key file");
	    		}else if(keyContents.trim().length()==0){
	    			// check if the line is empty and skip so no out of bounds execption is thrown
	    			System.out.println("\n\tBE AWARE YOU HAVE AN EMPTY LINE IN YOUR READ 2 KEY FILE. " +
	    					"SKIPPING EMPTY LINE AND CONTINUING.\n");
	    			break;
 	    		}else{
 	    			// Expectation is that the barcode information is the 3rd element
 	    			String splitKeyLine[] = keyContents.split("\t");
 	    			barcodeLength=(splitKeyLine[2].length());
 	    			
 	    			/* modifier methods that delete and subsitute bases and write
 	    			 * modifications to new file 
 	    			 */
 	    			deletion(splitKeyLine, barcodeLength, modified);
 	    			substitution(splitKeyLine, barcodeLength, modified);		
 	    		}
 	    	}		
 	    	inputKey.close();
 	    	}catch(Exception e) {
 	            System.out.println("modifyKey2File: e="+e);
 	            e.printStackTrace();
 			}
     	
     	printListToFile(newName, modified);
     	
     	return newName;
     }
     
     /**
      * Copies the original entries from a key file over to a new file.  The new file has
      * a new file name and will be appended with modified barcodes after the orignial ones.
      * @param name Name of the key file with the original barcodes
      * @param newName Name of the new modified key file that will hold the original and modified
      * barcodes
      */
     private static void copyOriginalKeyFile(String name, String newName){
     	String temp;
     	ArrayList <String> hold = new ArrayList<String>();	// temporary list to hold original barcodes
     	int size=0;
     	
     	try{
     		// read the lines from the original file and store them in the temporary list
     		BufferedReader source = new BufferedReader(new InputStreamReader(
     				new FileInputStream(name)));
     		while((temp=source.readLine())!=null){
     			hold.add(temp);
     		}
     		source.close();
     		}catch(Exception e) {
             System.out.println("copyOriginalKeyFile: e="+e);
             e.printStackTrace();
 			}
     	
     	// print the original barcodes to the new file
     	printListToFile(newName, hold);
     }
     
     /**
      * Used in creating two alternative key file barcode configurations.  Creates a new barcode sequence
      * by deleting one and two nucleotides for each postion of the original.  
      * @param lineElement Array of a parsed line from the key file
      * @param length Length of the original barcode
      * @param list ArrayList holding the original and modified barocdes
      */
     private static void deletion(String [] lineElement, int length, ArrayList<String>list){
     	String tempBarcode;
     	String barcode = lineElement[2];	// Expectation is that the orginal barcode is the 3rd element
     	
     	// The first round of deletion
     	for(int i=0; i<length; i++){
     		// on the first pass, remove the first nucleotides
 			if(i==0){
 				list.add(lineElement[0]+"\t"+lineElement[1]+"\t"
 						+barcode.substring(1,length)+"\t"+lineElement[3]+"_mod\t"
 						+lineElement[4]+"\t"+lineElement[5]+"\t"+lineElement[6]+"\t"
 						+lineElement[7]);
 			}
 			else{
 				// on remaining passes, remove other nucleotides
 				tempBarcode = barcode.substring(0,i)+barcode.substring(i+1,length);
 				/* if barcode exists, continue (will happen often if barcode sequence
 				 * has the same nucleotide in a sequence of 2 or more), otherwise add
 				 * the new barcode to the list
 				 */
 				if(list.contains(tempBarcode)){
 					continue;
 				}else{
 					list.add(lineElement[0]+"\t"+lineElement[1]+"\t"
 							+tempBarcode+"\t"+lineElement[3]+"_mod\t"
 							+lineElement[4]+"\t"+lineElement[5]+"\t"+lineElement[6]+"\t"
 							+lineElement[7]);
 				}
 			}
     	}
     	
     	for(int i=0; i<length-1; i++){
 			if(i==0){
 	    		// on the first pass, remove the first 2 nucleotides
 				list.add(lineElement[0]+"\t"+lineElement[1]+"\t"
 						+barcode.substring(2,length)+"\t"+lineElement[3]+"_mod\t"
 						+lineElement[4]+"\t"+lineElement[5]+"\t"+lineElement[6]+"\t"
 						+lineElement[7]);
 			}
 			else{
 				// on remaining passes, remove other nucleotides
 				tempBarcode = barcode.substring(0,i)+barcode.substring(i+2,length);
 				/* if barcode exists, continue (will happen often if barcode sequence
 				 * has the same nucleotide in a sequence of 2 or more), otherwise add
 				 * the new barcode to the list
 				 */
 				if(list.contains(tempBarcode)){
 					continue;
 				}else{
 					list.add(lineElement[0]+"\t"+lineElement[1]+"\t"
 							+tempBarcode+"\t"+lineElement[3]+"_mod\t"
 							+lineElement[4]+"\t"+lineElement[5]+"\t"+lineElement[6]+"\t"
 							+lineElement[7]);
 				}
 			}
     	}
     }
     
     /**
      * Used in creating alternative key file barcode configurations.  Creates a new barcode sequence
      * by substituting every alternate base and missing base "N" at each postion of the original
      * barcode.
      * @param lineElement Array of a parsed line from the key file
      * @param length Length of the original barcode
      * @param list ArrayList holding the original and modified barocdes
      */
     private static void substitution(String [] lineElement, int length, ArrayList<String>list){
     	String originalBarcode = lineElement[2];	// Expectation is that the orginal barcode is the 3rd element in the array
     	char [] nucleotides = {'A','C','G','T','N'};	//  All of the possible letter swaps
     	char [] barcodeAsArray = originalBarcode.toCharArray();	// Convert barcode from String to Char array
     	String moddedBarcode;	// newly modified barcode
     	String newLine;	// the new line with modified barcode
     	
     	// Outter loop cycles through original barcode length
     	for(int i=0;i<length;i++){
     		// Nested loop cycles through all of the nucleotide possibilities
     		for(int k=0;k<nucleotides.length;k++){
     			barcodeAsArray = originalBarcode.toCharArray();
     			barcodeAsArray[i]=nucleotides[k];
     			moddedBarcode=new String(barcodeAsArray);
     			newLine = lineElement[0]+"\t"+lineElement[1]+"\t"
 						+moddedBarcode+"\t"+lineElement[3]+"_mod\t"
 						+lineElement[4]+"\t"+lineElement[5]+"\t"+lineElement[6]+"\t"
 						+lineElement[7];
     			/* The if conditional ignores the new line if it was previously generated or the same as
     			 * an orignial barcode, otherwise add it to the list */
     			if(list.contains(newLine) || moddedBarcode.equals(originalBarcode)){
     				continue;
     			}else{
     				list.add(lineElement[0]+"\t"+lineElement[1]+"\t"
 						+moddedBarcode+"\t"+lineElement[3]+"_mod\t"
 						+lineElement[4]+"\t"+lineElement[5]+"\t"+lineElement[6]+"\t"
 						+lineElement[7]);
     			}
     			
     		}
     	}
     
     }
     
     /**
      * Generic method for printing an string ArrayList to a file.  Appends to file
      * if it exists rather than overwrite it.
      * @param name Name of the file to be created.
      * @param list String ArrayList to be printed.
      */
     private static void printListToFile(String name, ArrayList<String> list){
     	int size=0;
     	try{
     		PrintWriter out = new PrintWriter(new BufferedWriter(
     				new FileWriter(name, true)));
 
     		list.trimToSize();
     		size=list.size();
     		for(int i=0;i<size;i++){
     			out.println(list.get(i).toString());
     		}
     		out.close();
     	}catch (IOException e) {
             System.out.println(e.getMessage());
             }
     }
     
     /**
      * Writes out the contents of a TreeMap to a unique file.  The TreeMap contains sequences
      * and identification information.  Due to the large amounts of data that is 
      * being process, at the end of the method, the TreeMap is cleared
      * to free system resources.
      * @param fileName1 Name of the first source data file
      * @param fileName2 Name of the second source data file
      * @param dir The output directory
      * @param order Integer that is counting the number of files that have been written and is used
      * to help add a uniqueness to the filename
      * @param stored TreeMap that contains the sequences and identification information
      * @param nameLog List containing file names processed by this file
      */
     private static void processPairsCounted(String fileName1, String fileName2,String dir,
     		int order, TreeMap<String, Integer> stored, ArrayList <String> nameLog){
     	
     	try {
         	long timeTemp = System.currentTimeMillis();
         	String hashOutName = Integer.toString(order)+"-"+fileName1+"-"+fileName2+".txt";
         	
             PrintWriter out = new PrintWriter(
             		new BufferedWriter(
             				new FileWriter(
             						dir+File.separator+hashOutName, true)));
         
         	nameLog.add(hashOutName);
         	
             for(String h: stored.keySet()){
             	String key = h.toString();
             	String value = stored.get(h).toString();
             	out.println(key+ "\t" + value);
             }
             out.close();		// close PrintWriter
             
             System.out.println("The number of lines added to " + hashOutName +" is " +stored.size());
             
             stored.clear();  // clear before continuing
             reportTime();	//reporter
         }catch (IOException e) {
         	System.out.println(e.getMessage());
         }
     }
     
     /**
      * Checks for an even number of files
      * @param numberOfPairs Number of file pairs
      */
     private static void checkForPairs(int numberOfPairs){
     	if (numberOfPairs % 2 !=0){
     		System.out.println("There are an odd number of files so there won't be correct pairing"); 
     		System.out.println("The number of files detected was "+numberOfPairs); 
         }
     }
     
     /**
      * Reporter method that prints stats to the console
      * @param lane - flowcell lane identifier
      * @param both - a counter that keeps track of the number of times both sequences register as good reads
      * @param forward - a counter that keeps track of the number of times only the forward sequence registers as a good read
      * @param reverse -  a counter that keeps track of the number of times only the reverse sequence registers as a good read
      * @param allGood - a counter that keeps the current total value of all lines read so far
      * @param totalReads - the total number of lines, good or bad, that the program has read so far
      */
     private static void reportStats(String lane, int both, int forward, int reverse, int allGood, int totalReads){
     	
     	String forSummary = lane+"\t"+Integer.toString(totalReads)+"\t"+Integer.toString(forward)+"\t"+
     			Integer.toString(reverse)+"\t"+Integer.toString(both);
     	
     	System.out.println("\nTotal Reads in this lane:" + totalReads);
     	System.out.println("The number of good paired reads in this lane is: "+both);
     	System.out.println("The number of good forward only reads in this lane is: "+forward);
     	System.out.println("The number of good reverse only reads in this lane is: "+reverse);
     	
     	summarizeCounts(forSummary);	// add to variable holding summary information for later output to file
     }
     
     /**
      * Prints a message to the console indicating there is a problem with the file name structure
      */
     private static void printParsingError(){
     	System.out.println("Error in parsing file name:");
 		System.out.println("   The filename does not contain a 5 underscore-delimited value.");
 		System.out.println("   Expected: code_flowcell_s_lane_fastq.txt.gz");
 		System.out.println("OR There is already a file in the ouput folder of the same name");
     }
     
     /**
      * Concatenate both forward and reverse sequence tags and identification information
      * @param forward Forward sequence
      * @param reverse Reverse sequence
      * @param idF Identification and summary information from the forward sequence
      * @param idR Identification and summary information from the reverse sequence
      * @return Concatenation of the sequences and id information with formatting elements
      */
     private static String stitch(String forward, String reverse, String idF, String idR){
     	String result;
     	String tag = forward+reverse;
     	String id1[]=idF.split(":");
     	String id2[]=idR.split(":");
     	//String flowcell = id1[2];
     	String barcodeIDs = id1[1]+":"+id2[1];
     	String lane = id1[3];
     	String tab="\t";
     	
     	//result = tag+tab+barcodeIDs+tab+flowcell;
     	result = tag+tab+barcodeIDs;
  
     	return result;
     }
     
     /**
      * Sets a static class variable to current system time.
      */
     private static void resetTime(){
     	startTime = System.currentTimeMillis();
     }
     
     /**
      * Reporter method that returns user friendlier hour, minute, sec output.  Minutes and seconds 
      * are rounded up to the nearest integer.  Method should only be called in code once a 
      * process is completed since it calls on the current system time to act as the end of a process.
      */
     private static void reportTime(){
     	long end = System.currentTimeMillis();
     	long time = end-startTime; //milliseconds
     	int sec = 0;
     	int mins = 0;
     	int hours = 0;
     	
     	if(time>=1000){
     		if(time>=60000){
     			if(time>=3600000){
     				hours = (int)time/3600000;
     				mins = ((int)time / 60000)-(hours*60);
     				System.out.println("Process completed in " +hours + " hours and "+ mins + " minutes\n");
     			}else{
     				mins = (int)time / 60000;
     				System.out.println("Process completed in " +mins + " minutes\n");
     			}
     		}else{
     			sec = Math.round((time / 1000));
 				System.out.println("Process completed in " +sec + " seconds\n");
     		}
     	}else{
     		System.out.println("Process completed in " +time + " milliseconds\n");
     	}
     }
    
     /**
      * Prints barcode IDs and counts to a file named "Barcode_ID_Info.txt"
      * @param directoryInfo System directory information
      */
     private static void printIds(String directoryInfo){
     	resetTime();	//reporter
     	System.out.println("\nprintIds: Start writing to output file");
     	System.out.println("The number of lines to be sent to the output file is " + barcodePairs.size());
     	
     	try {
     		ArrayList value = new ArrayList();	
         	PrintWriter out = new PrintWriter(
             		new BufferedWriter(
             				new FileWriter(
             						directoryInfo+File.separator+"Barcode_ID_Info.txt", true)));
         int tempCount=0;
         	// Send each key-value pair to output file
         	for(String h: barcodePairs.keySet()){
             	String key = h.toString();
             	String val = barcodePairs.get(h).toString();
             	out.println(key+ "\t" + val);
             	tempCount++;
             	// send update to console so user gets status update
             	if(tempCount%1000000 == 0){
             	System.out.println(tempCount+"lines written to file");
             	}
             }
             out.close();		// close PrintWriter
         }catch (IOException e) {
         	 System.out.println(e.getMessage());
         }
     	reportTime();	//reporter
     }
     
     /**
      * Takes an arguement set by the user via a flag and parses it based on a delimeter
      * within the arguement.
      * @param flagArg arguement from the user
      * @param delim delimeter used to parse the arguement
      * @param process reports the name of the flag being parsed so that a meaningful error
      * message can be returned.
      * @return An array where each element contains a string from the original arguement
      * @author Designed by Nicolas Kiely, modified by Saul Garcia
      */
     private static String [] parseFlagArgs(String flagArg, String delim, String process){
     	String [] results = flagArg.split(delim);
     	
     	/* Expect exactly two results */
     	if(results == null || results.length !=2){
     		String err = "Error, exiting program.  Expected 2 files for parsing: "+ process;
     		System.out.println(err);
     		/* Will force program to terminate */
     		System.exit(0);
     	}
     	
     	return results;
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
