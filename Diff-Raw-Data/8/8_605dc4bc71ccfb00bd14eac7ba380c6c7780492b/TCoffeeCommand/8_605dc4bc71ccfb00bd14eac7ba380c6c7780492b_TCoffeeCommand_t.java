 package models;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.blackcoffee.commons.format.TCoffeeErrorLog;
 import org.blackcoffee.commons.format.TCoffeeResultLog;
 import org.blackcoffee.commons.format.TCoffeeResultLog.FileItem;
 import org.blackcoffee.commons.utils.FileIterator;
 
 import play.Logger;
 import play.Play;
 import util.Utils;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 import exception.QuickException;
 
 @XStreamAlias("tcoffee")
 public class TCoffeeCommand extends AbstractShellCommand {
 
 	/** The t-coffee arguments */
 	public CmdArgs args;
 
 	private String fInputFileName;
 	
 	@XStreamOmitField
 	List<String> _warnings;
 	
	List<String> warnings() { return _warnings != null ? _warnings : (_warnings = new ArrayList<String>()); } 
	
 	/** The default constructor */
 	public TCoffeeCommand() {
 	}
 
 	/** The copy constrcutor */
 	public TCoffeeCommand(TCoffeeCommand that) {
 		super(that);
 		this.args = Utils.copy(that.args);
 	}
 	
 	@Override
 	public void init(CommandCtx ctx) {
 
 		/* an outfile is mandatory required */
 		if( Utils.isEmpty(logfile) ) logfile = "_tcoffee.out.log";
 		if( Utils.isEmpty(errfile) ) errfile = "_tcoffee.err.log";
 		if( Utils.isEmpty(cmdfile) ) cmdfile = "_tcoffee.cmd.txt";
 		if( Utils.isEmpty(envfile) ) envfile = "_tcoffee.env.txt";
 		
 		super.init(ctx);
 
 	}
 	
 	@Override
 	protected void onInitEnv(Map<String, String> map) {
 		super.onInitEnv(map);
 		
 		makePath( map.get("DIR_4_TCOFFEE") );
 		makePath( map.get("TMP_4_TCOFFEE") );
 		makePath( map.get("LOCKDIR_4_TCOFFEE") );
 		makePath( map.get("CACHE_4_TCOFFEE") );
 
 		/* add the user email in the environment */
 		if( Utils.isNotEmpty(Service.current().userEmail)) { 
 			map.put("EMAIL_4_TCOFFEE", Service.current().userEmail);
 		}
 		
 		/* 
 		 * check if exists the msa_max_len variable
 		 */
 		String maxlen = ctx.get("msa_max_len");
 		if( Utils.isNotEmpty(maxlen) ) { 
 			map.put("ALN_LINE_LENGTH", maxlen);
 		}
 	}
 
 	
 	protected void makePath( String path ) { 
 		if( Utils.isEmpty(path) ) { 
 			return;
 		}
 		
 		File file = new File(path);
 		if( !file.exists() && !file.mkdirs() ) {
 			Logger.warn("Unable to create T-coffee path: '%s' ", file);
 		}
 		
 	}
 	
 	@Override
 	protected String onInitCommandLine(String cmdLine) {
 		/*
 		 * get t_coffee absolute path 
 		 */
 		String result = "t_coffee";
 		
 		/* 
 		 * add the specified arguments for t-coffee 
 		 */
 		if (args == null) {
 			return result;
 		}
 		
 		/* 
 		 * detect input file name 
 		 */
 		String inFile = detectedInputFile(args);
 		
 		if( Utils.isNotEmpty(inFile)) {
 			int p = inFile.indexOf(' ');
 			fInputFileName = (p==-1) ? inFile : inFile.substring(0,p);
 			Logger.debug("t-coffee detected input file name: %s", fInputFileName);
 		}
 		
 		/*
 		 * check if database has been specified for local blast 
 		 */
 		if( "LOCAL".equals(args.get("blast")) ) {
 			String pdb = args.get("pdb_db");
 			String prot = args.get("protein_db");
 			
 			if( Utils.isEmpty(pdb) && Utils.isEmpty(prot)) {
 				Logger.info("BLAST server is local but any database is specified. Adding default configuration.");
 				args.put("pdb_db", "${settings.PDB_DB}");
 				args.put("protein_db", "${settings.PROTEIN_DB}");
 			} 
 		}
 		
 		/* 
 		 * check any method has been specified otheriwse remove the parameter 
 		 */
 		String method = args.get("method");
 		if( Utils.isEmpty(method)) {
 			args.remove("method");
 		}
 		else if( method.contains("clustalw")) {
 			Logger.warn("Note: multicore is not supported using 'clustal_xxx' method(s)");
 			args.put("multi_core","no");
 		}
 		
 		
 		if( args.get("other_pg") == null ) { 
 			/* 
 			 * override 'quit' attribute. This is required becase it influence as the stdout stderr are generated 
 			 * (only for classic i.e. other that 'other_pg' command)
 			 */
 			String quiet;
 			if( (quiet=args.get("quiet")) != null && !quiet.equals("stdout") ) {
 				Logger.warn("T-Coffee -quiet attribute is not supported. Setting to 'stdout'");
 			}
 			args.put("quiet", "stdout");
 		}
 		
 		/* return the command line */
 		return result += " " + args.toCmdLine();
 	}
 
 	static String detectedInputFile(CmdArgs args) {
 
 		String result;
 		
 		if( Utils.isNotEmpty(result=args.get("infile")) ) { 
 			return result;
 		}
 
 		if( Utils.isNotEmpty(result=args.get("in")) ) { 
 			return result;
 		}
 	
 		if( Utils.isNotEmpty(result=args.get("seq")) ) { 
 			return result;
 		}
 
 		if( Utils.isNotEmpty(result=args.get("aln")) ) { 
 			return result;
 		}
 	
 		/* fallbakc to the first argument with no value and any prefix, just the name */
 		for( Arg arg : args.getItems() ) { 
 			if( Utils.isEmpty(arg.prefix) && Utils.isEmpty(arg.value) && Utils.isNotEmpty(arg.name) ) { 
 				return arg.name;
 			}
 		}
 		
 		return null;
 		
 
 	}
 
 	/*
 	 * Some NFS can return false even the file exists, maybe for latency problem, try more than one time before return false  
 	 */
 	boolean safeLogFileCheck() { 
 		long begin = System.currentTimeMillis(); 
 		boolean result;
 		
 		
 		try { 
 			long timeout = Long.parseLong(Play.configuration.getProperty("settings.file.exists.fix.timeout", "-1"));
 			
 			while( !(result = hasLogFile() )) { // <-- until it return false, just wait 
 				
 				if( System.currentTimeMillis() - begin > timeout ) { 
 					break;
 				}
 				
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					Logger.warn(e.getMessage());
 				}
 			}
 			
 			return result;
 			
 		}
 		catch( Exception e ) { 
 			Logger.error(e, "Unable to check if log file exists");
 			return false;
 		}
 		
 	}
 	/**
 	 * on job termination parse the result file to discover tcoffee output
 	 */
 	@Override
 	protected boolean done(boolean success) {
 
 		boolean hasLogFile = safeLogFileCheck();
 		
 		/* the result file MUST exists */
 		/* check the result file does not contain an error */
 		if( success && hasLogFile) {
 			String firstLine = new FileIterator(getLogFile()).iterator().next();
 			success = firstLine != null && !firstLine.contains("ERROR:");
 		}
 		
 		/* add the input file to the result object */
 		if( fInputFileName != null ) {		
 
 			OutItem out = new OutItem(fInputFileName,"input_file");
 			//TODO tis label should be parametrized
 			out.label = "Input sequences";
 			result.add(out);
 		}
 		
 		/* add the command line file to the result object */
 		if( getCmdFile() != null ) {
 			OutItem out = new OutItem(getCmdFile(),"system_file");
 			//TODO this label should be parametrized
 			out.label = "Command line";
 			out.format = "cmdline";
 			result.add(out);
 		}
 		
 		/* Check the std output */
 		if( success || hasLogFile) { // <-- note: it is OR condition to force an exception if the job has been processed BUT the out file does not exists
 			
 			/* add at least the tcoffee log file */
 			OutItem out = new OutItem(logfile, "system_file");
 			//TODO tis label should be parametrized
 			out.label = "Log file";
 			result.add(out); 
 			
 			/* add all t-coffee result */
 			result.addAll(parseResultFile(getLogFile()));
 			
 			/* add warnings */
 			result.addWarnings( _warnings );
 		}
 
 		/* check the error output */
 		if( hasErrFile()) { 
 			if( !success ) { 
 				/* add at least the tcoffee log file */
 				OutItem out = new OutItem(getErrFile(), "error_file");
 				//TODO tis label should be parametrized
 				out.label = "Error file";
 				result.add(out); 
 			}
 
 			// parse for warning in any case 
 			parseErrorFile(getErrFile());
 		}
 		
 		/* add the t_coffee.ErrorReport file */
 		File report;
 		if( !success && (report=new File(ctxfolder, "t_coffee.ErrorReport")).exists() ) { 
 			OutItem out = new OutItem(report, "error_file");
 			out.label = "Error Report";
 			result.add(out); 
 			
 		}
 		
 		/*
 		 * check if the html exists if it has been specified on the cmd line
 		 */
 		List<String> output = args.getAsList("output");
 		if( output != null && (output.contains("score_html") || output.contains("html")) ) { 
 			OutItem html = result.getAlignmentHtml();
 			success = success && html != null && html.exists();
 		}
 
 		return success;
 	}
 	
 	
 	/** 
 	 * Parse the error file for warning . . 
 	 * 
 	 * @param file
 	 */
 	void parseErrorFile( File file ) { 
 		try { 
 			TCoffeeErrorLog log = TCoffeeErrorLog.parse(file);
			warnings().addAll( log.getWarnings() );
 		}
 		catch( Exception e ) { 
 			Logger.error(e, "Failing parsing T-coffee error file: %s", file);
 		}
 	}
 
 	List<OutItem> parseResultFile(File file) {
 	
 		List<OutItem> result = new ArrayList<OutItem>();
 		
 		try { 
 			/* 
 			 * Parse the T-Coffee output and append the file references returned,
 			 * each of them have should exist on the file system  
 			 */
 			TCoffeeResultLog log = TCoffeeResultLog.parse(file);
 
 			/*
 			 * add the warnings 
 			 */
			warnings().addAll( log.getWarnings() );
 			
 			/* retrieve the file in the context path */
 			final String root = ctx.get("data.path");
 		
 			for( FileItem fileItem : log.getFileItems() ) { 
 				
 				
 				OutItem item = new OutItem(new File( root, fileItem.name ), fileItem. type);
 				item. format = fileItem.format;
 				result.add(item);
 			}
 			
 			return result;
 			
 		} catch (IOException e) {
 			throw new QuickException(e, "Unable to parse result file: %s", file);
 		}		
 	}
 
 	
 	
 
 	
 }
