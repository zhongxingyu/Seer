 package enderdom.eddie.tasks.bio;
 
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 
 import enderdom.eddie.bio.assembly.ACEFileParser;
 import enderdom.eddie.bio.assembly.ACERecord;
 
 import enderdom.eddie.tasks.TaskState;
 import enderdom.eddie.tasks.TaskXTwIO;
 import enderdom.eddie.tools.Tools_Array;
 import enderdom.eddie.tools.Tools_System;
 import enderdom.eddie.tools.bio.Tools_Sequences;
 
 public class Task_Assembly extends TaskXTwIO{
 	
 	private boolean stats;
 	private int filter;
 	private boolean lens2file;
 	
 	public Task_Assembly(){
 		filter = 0;
 	}
 
 	public void parseArgsSub(CommandLine cmd){
 		super.parseArgsSub(cmd);
 		stats = cmd.hasOption("stats");
 		lens2file = cmd.hasOption("l");
		filter = getOption(cmd, "statlenfilter", -1);
 	}
 	
 	public void parseOpts(Properties props){
 	
 	}
 	
 	public void buildOptions(){
 		super.buildOptions();
 		options.addOption(new Option("s","stats", false, "Get Statistics regarding file"));
		options.addOption(new Option("c","contig", true, "Contig Name to analyse"));
 		options.addOption(new Option("f", "filterlen", true, "Filter out contigs smaller than arg bp in length"));
 		options.addOption(new Option("l", "lengths2file", false, "Get list of contigs length and save to file"));
 		options.removeOption("p");
 		options.removeOption("w");
 		options.removeOption("filetype");
 	}
 	
 	public void run(){
 		setCompleteState(TaskState.STARTED);
 		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
 		if(testmode)runTest();
 		else{
 			if(stats){
 				try {
 					ACEFileParser parse = new ACEFileParser(new FileInputStream(this.input));
 					int count=0;
 					int totalread=0;
 					long totalbp = 0;
 					int[] lengths = new int[parse.getContigSize()];
 					while(parse.hasNext()){
 						ACERecord record = (ACERecord) parse.next();
 						System.out.print("\r(No."+count+") : " + record.getContigName() + "        ");
 						lengths[count] = record.getConsensus().getActualLength();
 						if(lengths[count] < filter){
 							lengths[count]=-1;
 						}
 						else{
 							totalread+=record.getNoOfSequences();
 							totalbp+=record.getNoOfMonomers();
 						}
 						count++;
 					}
 					lengths = Tools_Array.IntArrayTrimAll(lengths, -1);
 					System.out.println();
 					System.out.println();
 					System.out.println(count-lengths.length+" contigs removed " +
 							"as they fell below the "+filter+"bp length");
 					
 					long[] stats = Tools_Sequences.SequenceStats(lengths);
 					System.out.println();
 					System.out.println("No. of Contigs: " + lengths.length);
 					System.out.println("Total Contig Length: " + stats[0]+ "bp");
 					System.out.println("Min-Max Lengths: " + stats[1] +"-"+stats[2] + "bp");
 					System.out.println("n50: " + stats[3]);
 					System.out.println("n90: " + stats[4]);
 					System.out.println("Contigs >500bp: " + stats[5]);
 					System.out.println("Contigs >1Kbp: " + stats[6]);
 					System.out.println("Total No. of Reads: " + totalread);
 					System.out.println("Total No. of Bp: " + totalbp);
 					
 				}
 				catch (FileNotFoundException e) {
 					logger.error("No file called " + this.input,e);
 					setCompleteState(TaskState.ERROR);
 					return;
 				} catch (IOException e) {
 					logger.error("Could not parse " + this.input,e);
 					setCompleteState(TaskState.ERROR);
 					return;
 				}
 			}
 			else if(lens2file){
 				ACEFileParser parse;
 				try {
 					parse = new ACEFileParser(new FileInputStream(this.input));
 					int count=0;
 					logger.info("Parsing ACE file ...");
 					ArrayList<Integer> arrs = new ArrayList<Integer>(parse.getContigSize());
 					while(parse.hasNext()){
 						ACERecord record = (ACERecord) parse.next();
 						System.out.print("\r(No."+count+") : " + record.getContigName() + "        ");
 						int l = record.getConsensus().getActualLength();
 						if(l >= this.filter){
 							arrs.add(l);
 						}
 						count++;
 					}
 					System.out.println();
 					logger.info("Writing to file " + output);
 					FileWriter fstream = new FileWriter(output);
 					BufferedWriter out = new BufferedWriter(fstream);
 					String newline = Tools_System.getNewline();
 					for(Integer i : arrs){
 						out.write(i.toString());
 						out.write(newline);
 					} 
 					out.close();
 				}
 				catch (FileNotFoundException e) {
 					logger.error("No file called " + this.input,e);
 					setCompleteState(TaskState.ERROR);
 					return;
 				} catch (IOException e) {
 					logger.error("Could not parse " + this.input + " and/or write to "+this.output,e);
 					setCompleteState(TaskState.ERROR);
 					return;
 				}
 			}
 			else{
 				logger.info("Not Task set");
 			}
 		}
 		
 		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
 	    setCompleteState(TaskState.FINISHED);
 	}
 	
 }
 
