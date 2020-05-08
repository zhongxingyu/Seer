 package enderdom.eddie.tasks.bio;
 
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.Stack;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 
 import enderdom.eddie.bio.factories.SequenceListFactory;
 import enderdom.eddie.bio.sequence.SequenceList;
 import enderdom.eddie.bio.sequence.SequenceObject;
 
 import enderdom.eddie.tasks.Checklist;
 import enderdom.eddie.tasks.TaskState;
 import enderdom.eddie.tasks.TaskXTwIO;
 import enderdom.eddie.tools.Tools_File;
 import enderdom.eddie.tools.Tools_String;
 import enderdom.eddie.tools.Tools_System;
 import enderdom.eddie.tools.Tools_Task;
 import enderdom.eddie.tools.bio.Tools_Fasta;
 import enderdom.eddie.ui.UI;
 
 
 /**
  * 
  * @author Dominic Wood
  * 
  * I wasn't originally going to make this,
  * but for some reason nobody ever makes programs
  * which let you stop-start, As such this just 
  * auto-splits file and keeps track of the progress 
  * with checklist, thus allowing you to close and 
  * restart at will. Though iprscan doesn't drop resources
  * immediately when you close the process, it least
  * with python so I assume the same is true for java. 
  *
  */
 public class Task_IprscanLocal extends TaskXTwIO{
 	
 	private String iprscanbin;
 	private String params;
 	SequenceList sequences;
 	private int split;
 	private long timestart;
 	
 	public Task_IprscanLocal(){
 		/*
 		 */
 		setCore(true);
 		split = 1;
 		params = "-cli -iprlookup -goterms -nocrc -format xml -altjobs";
 		this.setHelpHeader("Task to help automate local iprscan-ing");
 	}
 	
 
 	public void parseArgsSub(CommandLine cmd){
 		super.parseArgsSub(cmd);
 		if(cmd.hasOption("bin"))iprscanbin=cmd.getOptionValue("iprscanbin");
 		if(cmd.hasOption("params"))params = cmd.getOptionValue("params");
 		if(cmd.hasOption("pf")){
 			File fie = new File(cmd.getOptionValue("pf"));
 			if(fie.isFile()){
 				params = Tools_File.quickRead(fie, false);
 			}
 			else{
 				logger.error("Iprscan parameter file does not exist");
 				params = "";
 			}
 		}
 		if(cmd.hasOption("split")){
 			Integer i = Tools_String.parseString2Int(cmd.getOptionValue("split"));
 			if(i !=null)split=i;
 			else logger.error("Integer set with -split is not an integer");
 		}
 	}
 	
 	public void parseOpts(Properties props){
 		if(iprscanbin == null){
 			iprscanbin = props.getProperty("IPRSCAN_BIN");
 		}
 		logger.trace("Parse Options From props");
 	}
 	
 	public void buildOptions(){
 		super.buildOptions();
 		options.addOption(new Option("b", "bin", true, "Set executable for local iprscan, else will use property file location"));
 		options.addOption(new Option("pa", "params", true, "Parameters if not using default"));
 		options.addOption(new Option("pf", "paramsfile", true, "File containing the parameters, !will overwrite anything in -params"));
 		options.addOption(new Option("s", "split", true, "Successively split file into this many sequences"));
 		options.getOption("i").setDescription("Input sequence file fasta");
 		options.getOption("o").setDescription("Output directory");
 		options.removeOption("p");
 		options.removeOption("w");
 		options.removeOption("filetype");
 	}
 
 	public void run(){
 		setCompleteState(TaskState.STARTED);
 		logger.debug("Started running task @ "+Tools_System.getDateNow());
 		this.checklist = openChecklist(ui);
 		if(input != null && output != null){
 			File in = new File(input);
 			File out = new File(output);
 			int total = 0;
 			timestart = System.currentTimeMillis();
 			if(in.isFile() && out.isDirectory() && this.iprscanbin !=null){
 				try{
 					this.sequences = SequenceListFactory.getSequenceList(input);
					int size = this.sequences.getNoOfSequences();
 					if(checklist.inRecovery()){
 						trimRecovered(checklist.getData());
 					}
 					logger.debug("About to start running iprscans");
 					while(this.sequences.getNoOfSequences() != 0){
 						SequenceObject[] id = sequences.getNoOfSequences() > split ?
 								new SequenceObject[split] : new SequenceObject[sequences.getNoOfSequences()];
 						int c=0;
 						Stack<String> removes = new Stack<String>();
 						for(String s : sequences.keySet()){
 							if(c == split)break;
 							else{
 								id[c]=sequences.getSequence(s);
 							}
 							c++;
 						}
 						while(removes.size()!=0)sequences.removeSequenceObject(removes.pop());
 						runIPRScan(out, checklist, id);
 						total+=c;
 						long timecurrent = System.currentTimeMillis();
 						long rate = (timecurrent-timestart)/total;
 						long s = rate*sequences.getNoOfSequences();
						int perc = (int) Math.round(((double)size-sequences.getNoOfSequences() / (double)size)*100);
						if(perc==100)perc=99;
						System.out.println(total + " sequences run, "+perc+"% complete. Estimated Time Left "+ Tools_System.long2DayHourMin(s));
 					}
 				}
 				catch(Exception io){
 					logger.error("Failed to run iprscan",io);
 				}
 				System.out.println();
 				System.out.println(" "+total + " sequences run, 100% complete");
 			}
 			else{
 				logger.error("Check that in is file, out is directory and blast_bin/db/prg is set");
 			}
 		}
 		else{
 			logger.error("Null input/output");
 		}
 		logger.debug("Finished running task @ "+Tools_System.getDateNow());
 	    setCompleteState(TaskState.FINISHED);
 	}
 	
 	public boolean isKeepArgs(){
 		return true;
 	}
 	
 	private void trimRecovered(String[] data){
 		int j=0;
 		for(int i =0;i < data.length; i++){
 			if(sequences.getSequence(data[i]) != null){
 				sequences.removeSequenceObject(data[i]);
 				j++;
 			}
 		}
 		logger.debug("Removed "+j+" of "+ data.length + " from list, as previously run");
 	}
 	
 	public void runIPRScan(File output, Checklist list, SequenceObject[] id){
 		
 		FileWriter fstream = null;
 		BufferedWriter out = null;
 		File temp = null;
 		try{
 			temp = File.createTempFile("Tempscan", ".fasta");
 			fstream = new FileWriter(temp, false);
 			out = new BufferedWriter(fstream);
 			for(int i =0; i < id.length; i++){
 				Tools_Fasta.saveFasta(id[i].getIdentifier(),id[i].getSequence(),out);
 			}
 			fstream.close();
 			
 			logger.trace("Saved to " + temp.getPath() + " stream closed");
 			String exec = iprscanbin +" " + params+ " -i " + temp.getPath() + " -o " 
 			+ output.getPath() + Tools_System.getFilepathSeparator()+ id[0].getIdentifier() +".xml";
 			logger.trace("About to execute output: " + exec);
 			StringBuffer[] buffer = Tools_Task.runProcess(exec, true);
 			logger.trace("Output:"+buffer[0].toString());
 			temp.delete();
 		}
 		catch(IOException io){
 			logger.error(io);
 		}
 	}
 	
 	public boolean wantsUI(){
 		return true;
 	}
 	
 	public void addUI(UI ui){
 		this.ui = ui;
 	}
 	
 }
