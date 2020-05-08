 package enderdom.eddie.tasks.database;
 
 import java.io.File;
 import java.util.Properties;
 import java.util.Stack;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;
 
 import enderdom.eddie.databases.manager.DatabaseManager;
 
 import enderdom.eddie.bio.homology.blast.BlastObject;
 import enderdom.eddie.bio.homology.blast.BlastxHelper;
 import enderdom.eddie.bio.homology.blast.MultiblastParser;
 
 import enderdom.eddie.tasks.TaskState;
 import enderdom.eddie.tasks.TaskXT;
 import enderdom.eddie.tools.Tools_CLI;
 import enderdom.eddie.tools.Tools_File;
 import enderdom.eddie.tools.Tools_System;
 
 /**
  * 
  * This will be put on the stack of
  * horrible tasks. FFS I can't code for toffee.
  * 
  * @author dominic
  *
  */
 public class Task_Blast extends TaskXT{
 
 	private boolean fuzzynames;
 	private String input;
 	private String[] files;
 	private boolean[] ignore;
 	DatabaseManager manager;
 	private String dbname;
 	private int run_id;
 	//filecount, fileerror, fileskip, hspcount-up, hspcount-skip, hspcount-error
 	private int[] counts;
 	private boolean force;
 	Stack<String> errfiles;
 	Stack<String> errfilesmin;
 	private boolean ignoreErrors;
 	
 	public Task_Blast(){
 		setHelpHeader("--This is the Help Message for the the blast Task--");
 		run_id =-1;
 		counts = new int[]{0,0,0,0,0,0,0};
 		errfiles = new Stack<String>();
 		errfilesmin = new Stack<String>();
 	}
 	
 	public void printHelpMessage(){
 		Tools_CLI.printHelpMessage(getHelpHeader(), Tools_System.getNewline()+"*Blasts within a 3 week date range that have identical parameters will be grouped together" +
 				"within the database. Alternatively you can force a run id to group blasts together." +
 				" This is needed so you can bulk retrieve statistics for a 'blast run' on say, an assembly."
 				+Tools_System.getNewline()+"-- Share And Enjoy! --", this.options);
 	}
 	
 	public void parseArgsSub(CommandLine cmd){
 		dbname= getOption(cmd, "dbname", null);
 		fuzzynames = cmd.hasOption("fuzzy");
 		force = cmd.hasOption("force");
 		run_id = getOption(cmd, "run_id", -1);
 		input = getOption(cmd, "i", null);
 		//date = getOption(cmd, "date", null);
 	}
 	
 	public void buildOptions(){
 		super.buildOptions();
 		options.addOption(new Option("r","run_id", true, "Force set run id"));
 		//options.addOption(new Option("date", true, "Set date when blast was run*, use format "+Tools_System.SQL_DATE_FORMAT+", [RECOMMENDED]"));
 		options.addOption(new Option("i","input", true, "Input folder or file"));
 		options.addOption(new Option("force", false, "Force blast records to be updated"));
 		options.addOption(new Option("db","dbname", true, "Default database name, such as nr, swiss-prot etc. If not set, will attempt to get from blast file"));
 		options.addOption(new Option("fuzzy", false, "Check for fuzzy names before failing, " +
 				"may be help if blast query-id is different from database id. May lead to incorrect uploads though "));
 		options.addOption(new Option("g","ignore", true, "Ignore errors and continue updating"));
 	}
 	
 	public void parseOpts(Properties props){
 
 	}
 	
 	public Options getOptions(){
 		return this.options;
 	}
 
 	public void run(){
 		setCompleteState(TaskState.STARTED);
 		logger.debug("Started running Blast Upload Task @ "+Tools_System.getDateNow());
 		File in=null;
 		if(input !=null)in = new File(input);
 		this.checklist = openChecklist(ui);
 		if(in == null || !in.exists()){
 			logger.error("File "+input+" does not exists");
 			return;
 		}
 		else{
 			manager = ui.getDatabaseManager(password);
 			try{
 				manager.open();
 				if(in.isDirectory()){
 					files = in.list();
					String p = Tools_System.getFilepathSeparator();
					for(int i =0; i < files.length;i++)files[i]=in.getPath()+p+files[i];
 					ignore = new boolean[files.length];
 					if(checklist.inRecovery()){
 						trimRecovered(checklist.getData());
 					}
 					int i=0;
 					for(;i < files.length; i++){
 						if(!ignore[i]){
 							try{
 								uploadBlastFile(files[i]);
 								counts[0]++;
 							}
 							catch(Exception e){
 								logger.error("Failed to parse file " + files[i],e);
 								counts[1]++;
 								errfiles.push(files[i]);
 							}
 							checklist.update(files[i]);
 							if(counts[1] > 1 && !ignoreErrors){
 								throw new Exception("Errors in blast upload, quitting, run with -ignore to skip errors");
 							}
 						}
 						else{
 							counts[2]++;
 						}
 						System.out.print("\rFile No.: "+i+" 		");
 					}
 					dealErrors();
 
 					String s = Tools_System.getNewline();
 					System.out.println(s+"#####################################################");
 					System.out.println("--Blast Parsing--");
 					System.out.println("Parsed:"+counts[0]+" Skipped:"+counts[2]+" Errored:"+counts[1]+s);
 					System.out.println("--Blast Matches Upload--");
 					System.out.println("Hits Uploaded:"+counts[3]+" Updated:"+counts[6]+" Skipped:"+counts[4]+" Errored:"+counts[5]);
 					System.out.println("#####################################################"+s+s);
 					//Also log this information, for nohup and whatnot
 					logger.info("Blast Parsing: " + "Parsed:"+counts[0]+" Skipped:"+counts[2]+" Errored:"+counts[1]);
 					logger.info("Uploaded:"+counts[3]+" Updated:"+counts[6]+" Skipped:"+counts[4]+" Errored:"+counts[5]);
 
 				}
 				else{
 					try{
 						uploadBlastFile(input);
 					}
 					catch(Exception e){
 						logger.error("Failed to parse file " + in.getName(),e);
 						setCompleteState(TaskState.ERROR);
 						return;
 					}
 				}
 				checklist.complete();
 			}
 			catch(Exception e){
 				logger.error("Failed database or somethinG",e);
 				setCompleteState(TaskState.ERROR);
 				return;
 			}
 		}
 		logger.debug("Finished running Blast Upload Task @ "+Tools_System.getDateNow());
 	    setCompleteState(TaskState.FINISHED);
 	}
 	
 	public void uploadBlastFile(String filename) throws Exception{
 		MultiblastParser parse = new MultiblastParser(MultiblastParser.BASICBLAST, new File(filename));
 		manager.getBioSQL().largeInsert(manager.getCon(), true);
 		int parsecount=0;
 		while(parse.hasNext()){
 			BlastObject o = parse.next();
 			if(o != null){
 				BlastxHelper helper = new BlastxHelper(o);
 				if(dbname == null){
 					dbname = helper.getBlastDatabase();
 				}
 				if(run_id > 0){
 					helper.setRun_id(run_id);
 				}
 				else{
 					helper.setDate(Tools_System.getDateNow(Tools_System.SQL_DATE_FORMAT));
 				}
 				
 				int[] j = helper.upload2BioSQL(manager, fuzzynames, dbname, force);
 				if(j[0] == -1){
 					counts[5]++;
 					errfilesmin.push(filename);
 				}
 				else{
 					counts[3]+=j[0];
 					counts[4]+=j[1];
 					counts[6]+=j[2];
 				}
 				if(run_id <0 && helper.getRun_id() > 0){
 					this.run_id = helper.getRun_id();
 					logger.debug("Run id was not available so was created and set as " + run_id);
 				}
 				else if(run_id < 0) throw new Exception("No run ID attributed to blast");
 				
 				parsecount++;
 				if(parsecount%1000==0){
 					manager.getBioSQL().largeInsert(manager.getCon(), false);
 					manager.getBioSQL().largeInsert(manager.getCon(), true);
 				}
 			}
 			else{
 				counts[1]++;
 				errfiles.push(filename);
 			}
 		}
 		parse.close();
 		parse = null;
 		manager.getBioSQL().largeInsert(manager.getCon(), false);
 	}
 	
 	private void trimRecovered(String[] data){
 		for(int i =0;i < data.length; i++){
 			for(int j =0; j < files.length; j++){
 				if(data[i].equals(files[i])){
 					this.ignore[i] = true;
 					break;
 				}
 			}
 		}
 	}
 	
 	private void dealErrors(){
 		if(errfiles.size() == 0  && errfilesmin.size() == 0){
 			logger.info("No errors to deal wtih");
 		}
 		else{
 			File f = new File(ui.getPropertyLoader().getValue("WORKSPACE")+Tools_System.getFilepathSeparator()+"err.dump");
 			logger.warn("Errors in files, list dumped in the following location:"+f.getPath());
 			Tools_File.quickWrite(Tools_System.getNewline()+"Errors for Blast on " + Tools_System.getDateNow()+Tools_System.getNewline(), f, true);
 			Tools_File.quickWrite("Major File Errors:"+Tools_System.getNewline(),f, true);
 			while(errfiles.size() !=0)Tools_File.quickWrite(errfiles.pop()+Tools_System.getNewline(), f, true);
 			Tools_File.quickWrite("Minor Blast Errors:"+Tools_System.getNewline(),f, true);
 			while(errfilesmin.size() !=0)Tools_File.quickWrite(errfilesmin.pop()+Tools_System.getNewline(), f, true);
 		}
 	}
 }
