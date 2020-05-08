 package enderdom.eddie.tasks.database;
 
 import java.io.File;
 import java.sql.SQLException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.commons.io.FilenameUtils;
 
 
 import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
 import enderdom.eddie.databases.manager.DatabaseManager;
 import enderdom.eddie.exceptions.EddieDBException;
 import enderdom.eddie.tasks.TaskState;
 import enderdom.eddie.tasks.TaskXT;
 import enderdom.eddie.tools.Tools_System;
 
 public class Task_BlastAnalysis2 extends TaskXT{
 
 	private int blastRunID;
 	
 	private String output;
 	private double evalue;
 	private int hit_no;
 	private DatabaseManager manager;
 	private boolean unique;
 	private boolean OP_contig;
 	private boolean OP_read;
 	private boolean cumulcount;
 	private boolean header;
 	private boolean dbcoverage;
 	private boolean meta;
 	
 	public Task_BlastAnalysis2(){
 		setHelpHeader("--Task outputs blast analysis using database and assembly files--");
 	}
 	
 	public void parseArgsSub(CommandLine cmd){
 		super.parseArgsSub(cmd);
 		blastRunID = getOption(cmd, "rb", -1);
 		evalue = getOption(cmd, "evalue", 0.001);
 		hit_no = getOption(cmd, "numbhits", 1);
 		output = getOption(cmd, "o", null);
 		unique = cmd.hasOption("unique");
 		OP_contig = cmd.hasOption("OP_contig");
 		OP_read = cmd.hasOption("OP_read");
 		cumulcount=cmd.hasOption("OP_cumulCount");
 		header = !cmd.hasOption("noheader");
 		dbcoverage = cmd.hasOption("OP_dbCoverage");
 		meta = cmd.hasOption("OP_dbCoverageMETA");
 		if(meta)dbcoverage=true;
 	}
 	
 	public void buildOptions(){
 		super.buildOptions();
 		options.addOption(new Option("o", "output", true,  "Output file location"));
 		options.addOption(new Option("rb", "runBlast", true, "Set blast run id if needed (or for meta assemblies use meta assembly run id)"));
 		options.addOption(new Option("e", "evalue", true, "Evalue filter"));
 		options.addOption(new Option("numbhits", true, "Number of hits to limit to."));
 		options.addOption(new Option("unique", false, "Number of unique hits, ie number of hits matching "));
 		options.addOption(new Option("OP_contig", false, "Number of contig hits"));
 		options.addOption(new Option("OP_read", false, "Number of read hits extrapolated from contigs"));
 		options.addOption(new Option("OP_cumulCount", false, "Outputs a cumulative score for this blast run"));
 		options.addOption(new Option("noheader", false, "Do not include header in output file"));
 		options.addOption(new Option("OP_dbCoverage", false, "Output the total coverage of a blast run "));
 	}
 
 	public void run(){
 		setCompleteState(TaskState.STARTED);
 		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
 		boolean task = false;
 		if(OP_contig || OP_read){
 			task=true;
 			try{
 				manager = ui.getDatabaseManager(this.password);
 				if(manager.open()){
 					if(blastRunID != -1){
 						if(OP_read)printHitReadCount(blastRunID);
 						else printHitCount(blastRunID);
 					}
 					else{
 						int[] runs = manager.getBioSQLXT().getRunId(manager, null, Run.RUNTYPE_blast);
 						for(int i =0 ; i < runs.length ; i++){
 							if(OP_read)printHitReadCount(runs[i]);
 							else printHitCount(runs[i]);
 						}
 					}
 				}
 				else logger.error("Failed to open manager");
 			}
 			catch(Exception e){
 				logger.error("Failed to run database",e);
 				this.setCompleteState(TaskState.ERROR);
 			}
 		}
 		if(cumulcount){
 			task=true;
 			if(output!=null)CumulativeCount();
 			else logger.error("output not set for OP_c");
 		}
 		if(dbcoverage){
 			task=true;
 			logger.debug("Database coverage analysis begun");
 			if(this.blastRunID >0){
 				manager = ui.getDatabaseManager(this.password);
 				try{
 					if(manager.open()){
 						Run r = manager.getBioSQLXT().getRun(manager, blastRunID);
 						meta = r.getRuntype().equals(Run.RUNTYPE_ASSEMBLY_META);
 						if(meta)logger.info("Analysis detected as running on meta assembly");
 						int[] total = manager.getBioSQLXT().getDbCoverageAssembly(manager, this.hit_no,
 							1, evalue, blastRunID, meta);
 						logger.info("Coverage: " + total[0]);
 						logger.info("Overrepresentation: " + total[1]);
 						
 						System.out.println("-----------------------------------------------------------------");
 						System.out.println("COVERAGE:");
 						System.out.println(total[0]+" residue/bp (Depending on database)");
 						System.out.println();
 						System.out.println("OVERREPRESENTATION:");
 						System.out.println(total[1]+" residue/bp (Depending on database)");
 						System.out.println("-----------------------------------------------------------------");
 					}
 				}
 				catch (InstantiationException e) {
 					logger.error("Failed to open database", e);
 				} catch (IllegalAccessException e) {
 					logger.error("Failed to open database", e);
 				} catch (ClassNotFoundException e) {
 					logger.error("Failed to open database", e);
 				} catch (EddieDBException e) {
 					logger.error("Failed to open database", e);
 				} catch (InterruptedException e) {
 					logger.error("Failed to open database", e);
 				}catch (SQLException e) {
 					logger.error("Failed to open database", e);
 				}
 			}
			else{
 				logger.error("Blast run id needs to be set");
 			}
 		}
		if(!task){
 			logger.warn("No specific option set, pick OP_* for task to run");
 		}
 		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
 	    setCompleteState(TaskState.FINISHED);
 	}
 	
 	private void CumulativeCount(){
 			manager = ui.getDatabaseManager(password);
 			try{
 				if(manager.open()){
 					if(blastRunID > 0){
 						manager.getBioSQLXT().cumulativeCountQuery(manager, new File(output),this.blastRunID, this.hit_no, header);
 					}
 					else{
 						logger.info("You did not set blast run, running for each blast avaialble");
 						int[] blasts = manager.getBioSQLXT().getRunId(manager, null, Run.RUNTYPE_blast);
 						File f;
 						for(int i=0;i < blasts.length; i++){
 							Run run = manager.getBioSQLXT().getRun(manager, blasts[i]);
 							Run parent = manager.getBioSQLXT().getRun(manager, run.getParent_id());
 							String filename = parent.getProgram().replaceAll(" ", "_") +"_"; 
 							filename += (parent.getSource().indexOf(" ") != 1) ? parent.getSource().split(" ")[0] : parent.getSource();
 							f = new File(FilenameUtils.getFullPath(output)+filename+".dat");
 							if(manager.getBioSQLXT().cumulativeCountQuery(manager, f,blasts[i], this.hit_no, header)){
 								logger.info("File generated for run with blast ID "+blasts[i]+", output at " +f.getPath());
 							}
 							else logger.error("Error returned, please check logs");
 						}
 					}			
 				}
 				else throw new Exception("Failed to open database");
 			}
 			catch(Exception e){
 				logger.error("Failed database or something",e);
 				setCompleteState(TaskState.ERROR);
 				return;
 			}
 	}
 	
 	private void printHitReadCount(int run_id){
 		int hit_count = manager.getBioSQLXT().getHitCountwReadCount(manager, run_id, evalue, hit_no, 1, -1);
 		if(hit_count == 0){
 			logger.warn("Hit count at 0 ");
 			Run r = manager.getBioSQLXT().getRun(manager, run_id);
 			if(r !=null && !r.getRuntype().equalsIgnoreCase(Run.RUNTYPE_blast)){
 				logger.warn("Expected run type is "+Run.RUNTYPE_blast+" but this is "+ r.getRuntype()+ " check you are using the correct ");
 			}
 			else{
				logger.warn("Runtype is correct but no hits, have you mapped contigs to reads?");
 			}
 		}
 		
 		System.out.println("----INFO----");
 		printRunStuff(run_id);
 		System.out.println("Blast Hits mapped to reads where hit no <" 
 				+ hit_no + " & evalue <" +evalue+ ":");
 		System.out.println(hit_count);
 		System.out.println("------------");
 	}
 	
 	private void printHitCount(int run_id){
 		int hit_count = manager.getBioSQLXT().getHitCount(manager, run_id, evalue, hit_no, 1, -1, unique);
 		System.out.println("----INFO----");
 		printRunStuff(run_id);
 		System.out.println("Blast Hits where hit no <" 
 				+ hit_no + " & evalue <" +evalue+ ":");
 		System.out.println(hit_count);
 		System.out.println("------------");
 	}
 	
 	private void printRunStuff(int run_id){
 		Run run = manager.getBioSQLXT().getRun(manager, run_id);
 		Run parent = manager.getBioSQLXT().getRun(manager, run.getParent_id());
 		String p = (run!=null) ?"Data for blast run (RunID:"
 				+run_id+") of assembly by " + parent.getProgram() + " on " + parent.getSource()
 				: "This Data blast run attached to " + run_id;
 		System.out.println(p);
 	}
 }
