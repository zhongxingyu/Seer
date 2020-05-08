 package enderdom.eddie.tasks.bio;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.Properties;
 
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.log4j.Logger;
 
 import enderdom.eddie.bio.blast.MultiblastParser;
 import enderdom.eddie.bio.blast.UniVecBlastObject;
 import enderdom.eddie.bio.blast.UniVecRegion;
 import enderdom.eddie.bio.factories.SequenceListFactory;
 import enderdom.eddie.bio.interfaces.BioFileType;
 import enderdom.eddie.bio.interfaces.SequenceList;
 import enderdom.eddie.bio.interfaces.SequenceObject;
 import enderdom.eddie.tasks.TaskXTwIO;
 import enderdom.eddie.tools.Tools_File;
 import enderdom.eddie.tools.Tools_String;
 import enderdom.eddie.tools.Tools_System;
 import enderdom.eddie.tools.Tools_Task;
 import enderdom.eddie.tools.Tools_Web;
 import enderdom.eddie.tools.bio.Tools_Bio_File;
 import enderdom.eddie.tools.bio.Tools_Blast;
 import enderdom.eddie.ui.UI;
 
 public class Task_UniVec extends TaskXTwIO{
 
 	private String uni_db;
 	private String blast_bin;
 	private String workspace;
 	private String xml;
 	private String qual;
 	private boolean create;
 	private static String univecsite = "ftp://ftp.ncbi.nih.gov/pub/UniVec/UniVec";
 	private static String univeccom = "makeblastdb -title UniVec -dbtype nucl ";
 	private static String strategyfolder = "resources";	
 	private static String strategyfile = "univec_strategy";
 	private static String key = "UNI_VEC_DB";
 	private SequenceList fout;
 	private int filter = 50;
 	private boolean saveasfastq;
 	
 	public Task_UniVec(){
 	}
 	
 	public void run(){
 		setComplete(started);
 		logger.debug("Started running task @ "+Tools_System.getDateNow());
 		/*
 		* Check IO
 		*/
 		File dir = checkOutput();
 		File file = checkInput();
 		if(file == null) return;
 		
 
 		if(xml == null){
 			if(!checkUniDB()){
 				logger.error("Failed to establish UniVec database");
 				return;
 			}
 			File out = Tools_File.getOutFileName(dir, file, ".xml");
 			
 			/*
 			* Build univec strategy file
 			*/
 			String strat = this.workspace + Tools_System.getFilepathSeparator()
 					+strategyfolder+Tools_System.getFilepathSeparator()+ strategyfile +".asn";
 			if(!new File(strat).exists()){
 				if(!generateStrategyFile(strat))return;
 			}
 	
 			/*
 			* Actually run the blast program
 			* See http://www.ncbi.nlm.nih.gov/VecScreen/VecScreen_docs.html for specs on vecscreen
 			*/
 			StringBuffer[] arr = Tools_Blast.runLocalBlast(file, "blastn", blast_bin, uni_db, "-import_search_strategy "+strat+" -outfmt 5 ", out);
 			if(arr[0].length() > 0){
 				logger.info("blastn output:"+Tools_System.getNewline()+arr[0].toString().trim());
 			}
 			if(arr[1].length() > 0){
 				logger.info("blastn output:"+Tools_System.getNewline()+arr[0].toString().trim());
 			}
 			if(out.isFile()){
 				xml = out.getPath();
 			}
 			else{
 				logger.error("Search ran, but no outfile found at " + out.getPath());
 				return;
 			}
 		}
 		//
 		//
 		//Trim fasta based on univec output
 		String[] outs = null;
 		if(xml != null){
 			File xm = new File(xml);
 			if(xm.isFile()){
 				outs = parseBlastAndTrim(xm, fout, output, this.filetype, filter,this.saveasfastq);
 			}
 			else if(xm.isDirectory()){
 				File[] files = xm.listFiles();
 				int i =0;
 				for(File f : files){
 					if(Tools_Bio_File.detectFileType(f.getPath())==BioFileType.BLAST_XML){
 						outs = parseBlastAndTrim(f, fout, output, this.filetype, filter, this.saveasfastq);
 						i++;
 					}
 				}
 				if(i==0){
 					logger.warn("No blast files detected in folder, attempting to parse any old XMLs");
 					for(File f: files){
 						if(Tools_Bio_File.detectFileType(f.getPath())==BioFileType.XML){
 							outs = parseBlastAndTrim(f, fout, output, this.filetype, filter,this.saveasfastq);
 							i++;
 						}
 					}
 				}
 			}
 			else{
 				logger.error("Error loading XML file");
 			}
 		}
 		for(int i =0; i < outs.length ; i++){
 			logger.info("Saved file to " + outs[i]);
 		}
 		logger.debug("Finished running task @ "+Tools_System.getDateNow());
 		setComplete(finished);
 	}
 
 	public void buildOptions(){
 		super.buildOptions();
 		options.getOption("i").setDescription("Input sequence file Fast(a/q)");
 		options.getOption("o").setDescription("Output folder");
 		options.addOption(new Option("u", "uni_db", true, "Set UniVec database location"));
 		options.addOption(new Option("c", "create_db", false, "Downloads and creates the UniVec database with the makeblastdb"));
 		options.addOption(new Option("bbb", "blast_bin", true, "Specify blast bin directory"));
 		options.addOption(new Option("filetype", true, "Specify filetype (rather then guessing from ext)"));
 		options.addOption(new Option("x","xml", true, "Skip running univec search and import previous blast xml"));
 		options.addOption(new Option("q","qual", true, "Include quality file, this will also be trimmed"));
 		options.addOption(new Option("r", "trim", true, "Remove sequences smaller than this (After trimming) "));
 		options.getOption("w").setDescription("Force Save file as fastq format");
 		
 	}
 	
 	public void parseOpts(Properties props){
 		if(blast_bin == null){
 			blast_bin = props.getProperty("BLAST_BIN_DIR");
 		}
 		workspace = props.getProperty("WORKSPACE");
 		logger.trace("Parse Options From props");
 	}
 	
 	public void parseArgsSub(CommandLine cmd){
 		super.parseArgsSub(cmd);
 		if(cmd.hasOption("u"))uni_db=cmd.getOptionValue("u");
 		if(cmd.hasOption("bbb"))blast_bin=cmd.getOptionValue("bbb");
 		if(cmd.hasOption("c"))create=true;
 		if(cmd.hasOption("i"))input=cmd.getOptionValue("i");
 		if(cmd.hasOption("x"))xml=cmd.getOptionValue("x");
 		if(cmd.hasOption("q"))qual=cmd.getOptionValue("q");
 		if(cmd.hasOption("r")){
 			Integer trimlen = Tools_String.parseString2Int(cmd.getOptionValue("r"));
 			if(trimlen != null){
 				this.filter = trimlen;
 			}
 			else logger.warn("Trim length suggested is not a number, defaulted to " + filter);	
 		}
 		if(cmd.hasOption("w")){
 			this.saveasfastq = true;
 		}
 	}
 	
 	
 	public static String[] parseBlastAndTrim(File xml, SequenceList seql, String outputfolder, BioFileType filetype, int trimlength, boolean saveasfastq){
 		try{
 			return parseBlastAndTrim(new MultiblastParser(MultiblastParser.UNIVEC, xml), seql, outputfolder, filetype, trimlength, saveasfastq);
 		}
 		catch(Exception e){
 			Logger.getRootLogger().error("Failed to parse XML and trim sequences", e);
 			return null;
 		}
 	}
 	
 	/**
 	 * Checks input is okay, sets fout sequenceList
 	 * @return the Input File as a file object or null
 	 * if a problem occurs
 	 */
 	public File checkInput(){
 		if(input == null){
 			logger.error("No input file specified");
 			return null;
 		}
 		File file = new File(input);
 		if(!file.isFile()){
 			logger.error("Input file is not a file");
 			return null;
 		}
 		else{
 			try {
 				if(qual != null &&  new File(qual).isFile()){
 					fout = SequenceListFactory.getSequenceList(input, qual);
 				}
 				else{
 					fout = SequenceListFactory.getSequenceList(input);
 				}
 				this.filetype = fout.getFileType();
 				return file;
 			} catch (Exception e) {
 				logger.error("Failed to parse input file",e);
 				return null;
 			}
 		}
 	}
 	
 	public File checkOutput(){
 		if(output == null){
 			output = workspace + Tools_System.getFilepathSeparator()+
 					"out" + Tools_System.getFilepathSeparator();
 		}
 		File dir = new File(output);
 		if(dir.isFile()){
 			logger.warn("File named out present in folder ...ugh...");
 			Tools_File.justMoveFileSomewhere(dir);
 			dir = new File(output);
 		}
 		dir.mkdirs();
 		return dir;
 	}
 	
 	
 	/**
 	 * Parses a blast xml file and trims a SequenceList according
 	 * to the hit locations based on UniVec rules. Obviously this 
 	 * is only really designed for univec
 	 * 
 	 * @param parser MultiBlastParser object
 	 * 
 	 * @param seql Sequence list, make sure quality data is there
 	 * if it is included
 	 * 
 	 * @param outputfolder where to save to
 	 * 
 	 * @param filetype type of file to save to, obviously this
 	 * will only work if the SequenceList is amenable to it
 	 * 
 	 * @param filter sequences shorter than this will be removed entirely
 	 * (set to <1 for no filtering)
 	 * 
 	 * @return the return value for SequenceList.saveFile, the path of 
 	 * all files saved
 	 * 
 	 * @throws Exception
 	 */
 	public static String[] parseBlastAndTrim(MultiblastParser parser, SequenceList seql, String outputfolder, BioFileType filetype, int trimlength, boolean saveasfastq) throws Exception{
 		int startsize =  seql.getNoOfSequences();
 		int startmonmers = seql.getQuickMonomers();
 		int lefttrims = 0;
 		int righttrims = 0;
 		int midtrims = 0;
 		int removed =0;
 		int added =0;
 		int regions =0;
 		UniVecBlastObject obj = null;
 		SequenceObject o =null;
 		HashSet<String> alreadytrimmed = new HashSet<String>();
 		String s = new String();
 		try{
 			while(parser.hasNext()){
 				obj = (UniVecBlastObject) parser.next();
 				obj.reverseOrder();
 				s = obj.getBlastTagContents("Iteration_query-def");
 				for(UniVecRegion r : obj.getRegions()){
 					if(!alreadytrimmed.contains(s)){
 						alreadytrimmed.add(s);
 						o = seql.getSequence(s);
 						if(r.isLeftTerminal()){
 							o.leftTrim(r.getStop(0));
 							if(o.getLength() >= trimlength){
 								seql.addSequenceObject(o);
 								lefttrims++;
 							}
 							else{
 								seql.removeSequenceObject(o.getName());
 								removed++;
 							}
 						}
 						else if(r.isRightTerminal()){
 							o.rightTrim(o.getLength()-r.getStart(0));
 							if(o.getLength() >= trimlength){
 								seql.addSequenceObject(o);
 								righttrims++;
 							}
 							else{
 								seql.removeSequenceObject(o.getName());
 								removed++;
 							}
 						}
 						else{
 							SequenceObject[] splits =  o.removeSection(r.getStart(0), r.getStop(0));
 							if(splits.length > 1){
 								//Remove, as when added, added with diff name
 								seql.removeSequenceObject(o.getName());
 								if(splits[0].getLength() > trimlength){
 									seql.addSequenceObject(splits[0]);
 									if(splits[1].getLength() > trimlength){
 										seql.addSequenceObject(splits[1]) ;
 										added++;
 									}
 									midtrims++;
 								}
 								else if(splits[1].getLength() > trimlength){
 									seql.addSequenceObject(splits[1]) ;
 									midtrims++;
 								}
 								else{
 									removed++;
 								}
 							}
 							else if(splits.length > 0){
 								seql.removeSequenceObject(o.getName());
 								if(splits[0].getLength() > trimlength){
 									seql.addSequenceObject(splits[0]);
 									midtrims++;
 								}
 								else{
 									removed++;
 								}
 							}
 							else Logger.getRootLogger().debug("All sequence trimmed");
 						}
 						regions++;
 					}
 				}
 			}
 		}
 		catch (Exception e) {
 			Logger.getRootLogger().error("Exception in UniVec ",e);
 			if(obj != null){
 				Logger.getRootLogger().error("Error trimming file at " + obj.getIterationNumber() + 
 						" XML iteration, with " +obj.getBlastTagContents("Iteration_query-def"));
 			}
 			if(o != null){
 				Logger.getRootLogger().error(" Last object was " + o.getName() + " of length " + o.getLength());
 			}
 			return null;
 		}
 		
 		int endsize =  seql.getNoOfSequences();
 		int endmonmers = seql.getQuickMonomers();
 		System.out.println("");
 		System.out.println("##########__REPORT__###########");
 		System.out.println("A total of " + regions + " univec hits were found the across the " + startsize + " sequences, containing " + startmonmers + "bps");
 		System.out.println("After trimming "+ endsize + " remain (" +(startsize-endsize)+ " removed), a change of " +(startmonmers-endmonmers)+ " bps");
 		System.out.println(lefttrims+" sequences trimmed left and " + righttrims + " trimmed right");
 		System.out.println(midtrims + " sequences were trimmed inside the sequence");
 		System.out.println("A total of "+ removed +" sequences were removed (" + added +" Added due to internal trims)" );
 		System.out.println("###############################");
 		System.out.println("");
 		String name = outputfolder+Tools_System.getFilepathSeparator();
		name += seql.getFileName() !=null ? seql.getFileName()+"_trimmed" : "out_trimmed";  
 		if(!saveasfastq){
 			return seql.saveFile(new File(name), filetype);
 		}
 		else{
 			return seql.saveFile(new File(name), BioFileType.FASTQ);
 		}
 	}
 	
 	/**
 	 * Admittedly a bit of a mess, but hopefully checks
 	 * all eventualities.
 	 * 
 	 * @return true if uni_db is set and exists
 	 */
 	private boolean checkUniDB(){
 		if(uni_db != null && create){
 			return createUniVecDb(uni_db);
 		}
 		else if(create){
 			if(createUniVecDb(this.workspace+Tools_System.getFilepathSeparator()+"data"+Tools_System.getFilepathSeparator()+"UniVec")){
 				uni_db = this.workspace+Tools_System.getFilepathSeparator()+"data"+Tools_System.getFilepathSeparator()+"UniVec";
 				return true;
 			}
 			else{
 				logger.error("Failed to create univec database, create manually and add location to props");
 				return false;
 			}
 		}
 		else if(uni_db != null){
 			File f = new File(uni_db);
 			if(f.isFile()){
 				ui.getPropertyLoader().setValue("key", uni_db);
 				return true;
 			}
 			else{
 				logger.error("UniVec database set as " + uni_db + " is not a file, closing");
 				return false;
 			}
 		}
 		else if(ui.getPropertyLoader().getValue(key) != null && ui.getPropertyLoader().getValue(key).length() > 0){
 			uni_db = ui.getPropertyLoader().getValue(key);
 			if(new File(uni_db).isFile() || new File(uni_db + ".nin").exists() || new File(uni_db+".nhr").exists()) return true;
 			else return false;
 		}
 		else{
 			logger.warn("No uni-vec set, nor create is set.");
 			int value = ui.requiresUserYNI("Do you want to automatically create UniVec data?", "Create Univec Database?");
 			if(value == UI.YES){
 				if(createUniVecDb(this.workspace+Tools_System.getFilepathSeparator()+"data"+Tools_System.getFilepathSeparator()+"UniVec")){
 					uni_db = this.workspace+Tools_System.getFilepathSeparator()+"data"+Tools_System.getFilepathSeparator()+"UniVec";
 					return true;
 				}
 				else{
 					logger.error("Failed to create univec database, create manually and add location to props");
 					return false;
 				}
 			}
 			else{
 				logger.info("User chose not to create UniVec database");
 				return false;
 			}
 		}
 	}
 	
 	/**
 	 * Method requires external execution of 'makeblastdb'
 	 * program which should be available at blast_bin
 	 * 
 	 * @param filepath path which data is downloaded to. 
 	 * Note: file may not actually appear at this location,
 	 * data is downloaded to filepath + ".fasta" and 
 	 * database generated should be ".nhr"/".nin" though
 	 * this may change depending on size. 
 	 * @return true if database is both downloaded and makeblastdb is
 	 * run and the resulting files output are detected.
 	 */
 	public boolean createUniVecDb(String filepath){
 		logger.debug("About to create UniVec database at " + filepath);
 		File file = new File(filepath);
 		file.getParentFile().mkdirs();
 		if(file.exists())logger.warn("Database already exists, overwriting...");
 		if(Tools_Web.basicFTP2File(ui.getPropertyLoader().getValueOrSet("UNIVEC_URL", univecsite), filepath+".fasta")){
 			StringBuffer univec = new StringBuffer();
 			univec.append(blast_bin);
 			if(!blast_bin.endsWith(Tools_System.getFilepathSeparator()))univec.append(Tools_System.getFilepathSeparator());
 			univec.append("");
 			univec.append(univeccom +"-in "+ filepath+ ".fasta -out "+ filepath+ " ");
 			StringBuffer[] arr = Tools_Task.runProcess(univec.toString(), true);
 			if(arr[0].length() > 0){
 				logger.info("makeblastdb output:"+Tools_System.getNewline()+arr[0].toString().trim());
 			}
 			if(new File(file.getPath() + ".nin").exists() || new File(file.getPath()+".nhr").exists() || file.exists()){
 				ui.getPropertyLoader().setValue(key, file.getPath());
 				ui.getPropertyLoader().savePropertyFile(ui.getPropertyLoader().getPropertyFilePath(), 
 						ui.getPropertyLoader().getPropertyObject());
 				return true;
 			}
 			else return false;
 		}
 		else return false;
 	}
 	
 	public boolean generateStrategyFile(String strat){
 		String resource = this.getClass().getPackage().getName();
 		resource=resource.replaceAll("\\.", "/");
 		resource = "/"+resource+"/"+strategyfolder+"/"+strategyfile;
 		logger.debug("Creating resource from internal file at "+resource);
 		//Create inputstream from resource
 		InputStream str = this.getClass().getResourceAsStream(resource);
 		//Check if it is null
 		if(str == null){
 			logger.error("Failed to create strategy file resource, please send bug to maintainer");
 			return false;
 		}
 		//Generate folders for the strategy file
 		File tmpfolder = new File(this.workspace + Tools_System.getFilepathSeparator()+strategyfolder);
 		if(!tmpfolder.exists())tmpfolder.mkdirs();
 		//Write to file
 		if(Tools_File.stream2File(str, strat)){
 			logger.error("Failed to create search strategy file at " + strat);
 			return false;
 		}
 		else{
 			return true;
 		}
 	}
 }
 
