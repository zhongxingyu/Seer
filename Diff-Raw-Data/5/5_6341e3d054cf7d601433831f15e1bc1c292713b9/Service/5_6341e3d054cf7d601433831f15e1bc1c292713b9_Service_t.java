 package models;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 
 import play.Logger;
 import play.data.validation.Validation;
 import play.jobs.Job;
 import play.mvc.Http.Request;
 import play.mvc.Router;
 import play.mvc.Scope;
 import play.mvc.Scope.Params;
 import play.mvc.Scope.Session;
 import util.Utils;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 import exception.QuickException;
  
 @XStreamAlias("service")
 public class Service implements Serializable {
 
 	static ThreadLocal<Service> CURRENT = new ThreadLocal<Service>();
 	
 	/** the parent configuration object */
 	@XStreamOmitField
 	public Bundle bundle; 
 	
 	@XStreamOmitField Map<String,Object> fCtx;
 	@XStreamOmitField String fRid;
 	@XStreamOmitField Repo fRepo;
 	@XStreamOmitField OutResult fOutResult;
 	@XStreamOmitField Date fStartTime;
 	@XStreamOmitField String fRemoteAddress;
 	
 	/**
 	 * The unique service name
 	 */
 	@XStreamAsAttribute
 	public String name; 
 
 	/** A label used to describe the group to which this service belongs to */
 	public String group;
 	
 	/**
 	 * The service main <i>title</i>. It will be displayed in the index page and on top of the service form input data
 	 */
 	public String title;
 
 	/**
 	 * The service <i>description</i>. It will be displayed in the index page and on top of the service form input data
 	 */
 	public String description;
 
 	/**
 	 * The reference to the related ncbi articles related to the selected tcoffee service
 	 */
 	public String cite; 
 	
 	
 	/**
 	 * The input data model
 	 */
 	public Input input;
 	
 
 	/**
 	 * Define the main job to be executed in this service
 	 */
 	public ProcessCommand process;
 	
 	
 	/**
 	 * Defines the output of this service execution 
 	 */
 	public Output output;
 	
 	/**
 	 * The defualt constructor. Initialize the class to empty 
 	 */
 	public Service() {
 		
 	}
 	
 	public Service( String name ) {
 		this.name = name;
 	}
 	
 	public Service( final Bundle bundle ) {
 		this.bundle = bundle;
 	}
 	
 	
 	/** 
 	 * Module cony constructor. Creates a copy of <code>that</code> instance
 	 */
 	public Service( Service that ) {
 		this.bundle = that.bundle; // <-- be aware the - parent - configuration must NOT be copied 
 		this.name = Utils.copy(that.name); 
 		this.group = Utils.copy(that.group);
 		this.title = Utils.copy(that.title); 
 		this.description = Utils.copy(that.description); 
 		this.cite = Utils.copy(that.cite);
 		this.input = Utils.copy(that.input);
 		this.process = Utils.copy(that.process);
 		this.output = Utils.copy(that.output);
 	}
 	
 	/**
 	 * @return a cloned instance of the current service
 	 */
 	public Service copy() {
 		return new Service(this);
 	}
 	
 	public String getTitle() {
 		return Utils.isNotEmpty(title) ? title : name;
 	}
 	
 	public String rid() {
 		if( fRid != null ) {
 			return fRid;
 		}
 
 		return fRid = getRid();
 	}
 	
 	private String getRid() {
 		return getRid(true);
 	}
 	
 	private String getRid( boolean enableCaching ) {
 		if( input == null ) { return null; };
 		
 		int hash = input.hashFields();
 		hash = Utils.hash(hash, this.name);
 		hash = Utils.hash(hash, Session.current().getId());
 		hash = Utils.hash(hash, this.bundle.getLastModified());
 
 		/* 
 		 * Avoid clash on existing folder with unknown status, 
 		 * so basically check if for the current hash (rid) already exists 
 		 * a folder, if so loop until a non existing hash(<--> folder) is found 
 		 */
 		String result = Integer.toHexString(hash);
 		Repo check = new Repo(result,false);
 		while( check.getFile().exists() ) {
 			Status status = check.getStatus();
 			if( !enableCaching || check.isExpired() || status .isUnknown() ) {
 				// force a new hash id 
 				hash = Utils.hash(hash,result);
 				result = Integer.toHexString(hash);
 				check = new Repo(result,false);
 			}
 			else {
 				break;
 			}
 		}
 		
 		return fRid = result;
 		
 	} 
 
 	@Deprecated
 	public File folder() {
 		return fRepo != null ? fRepo.getFile() : null;
 	}
 	
 	public Repo repo() {
 		return fRepo;
 	}
 	
 	public static Service current() {
 		return CURRENT.get();
 	}
 	
 	public static Service current(Service service) {
 		CURRENT.set(service);
 		return service;
 	}
 	
 	public static void release() {
 		CURRENT.set(null);
 	}
 
 	/**
 	 * Validate all input fields against the specified http parameters 
 	 * @param params the parameters on the current http request  
 	 * @return <code>true</code> if validation is OK <code>false</code> otherwise 
 	 */
 	public boolean validate(Params params) {
 		fRid = null; // <-- invalidate the current 'request-id' if exists 
 		input.bind(params);
 		input.validate();
 		return !Validation.hasErrors();
 	} 
 	
 	/*
 	 * set a variable onto the binding context
 	 */
 	void setVariable( Field field ) {
 		Object value = null;
 		
 		/* just skip empty fields */
 		if( Utils.isEmpty(field.value) ) {
 			return;
 		}
 		
 		/*
 		 * memo fields are store as temporary files and in the context is passed that file 
 		 */
 		if( "memo".equals(field.type) ) {
 			File file=null;
 			try {
 				file = File.createTempFile("input-", ".txt", folder());
 				FileUtils.writeStringToFile(file, field.value, "utf-8");
 				value = file;
 			} catch (IOException e) {
 				throw new QuickException(e, "Unable to save memo field: '%s' to temp file: '%s'", field.name, file);
 			}
 		}
 		/* 
 		 * file are are managed in a similar way that 'memo' field
 		 */
 		else if( "file".equals(field.type) ) {
 			byte[] data = field.getFileContent();
 			if( data == null || data.length == 0 ) {
 				/* empty file - do not put this variable on the context */
 				return;
 			}
 			
 			File file=null;
 			try {
 				file = File.createTempFile("input-",null, folder());
 				FileUtils.writeByteArrayToFile(file, data);
 				value = file;
 			} catch (IOException e) {
 				throw new QuickException(e, "Unable to save memo field: '%s' to temp file: '%s'", field.name, file);
 			}
 			
 			
 		}
 		else {
 			value = field.value;
 		}
 
 		setVariable(field.name, value);
 	}
 
 	void setVariable( String key, Object value ) {
 		
 		if( fCtx.containsKey(key) ) {
 			/* if an entry already exist with this key, repack it as a list */
 			Object item = fCtx.get(key);
 			if( item instanceof List ) {
 				((List)item) .add(value);
 				return;
 			}
 			else {
 				List<Object> list = new ArrayList<Object>();
 				list.add(item);
 				list.add(value);
 				value = list;
 			} 
 		}
 		
 		/* 
 		 * put on the context 
 		 */
 		fCtx.put(key, value);
 		
 	}
 	
 
 
 	public Map<String,Object> getCtx() {
 		return fCtx;
 	} 
 	
 	/**
 	 * Evaluate the string replacing variables in the form ${varname}
 	 * 
 	 * @param raw the string containing variables to replace
 	 * @return the string wioth resolved variables 
 	 */
 	public String eval(String raw) { 
 		Eval evaluator = new Eval(raw);
 		return evaluator.eval( fCtx );
 	}
 	
 	public void init() {
 		init(true);
 	}
 	
 	/**
 	 * Prepare the <i>service</i> to be executed 
 	 */
 	public void init( boolean enableCaching ) {
 		
 		/*
 		 * 0. generic initialization 
 		 */
 		fStartTime = new Date();
 		fRemoteAddress = Request.current().remoteAddress;
 		
 		/*
 		 * 1. create the context repository folder for this execution 
 		 */
 		fRid = getRid(enableCaching);
 		fRepo = new Repo(fRid,true);
 	
 		/*
 		 * 2. initialize the context for the expression evaluation 
 		 */
 		fCtx = new HashMap<String,Object>();
 		
 		/* add the bundle properties content */
 		for( Object key : bundle.properties.keySet() ) {
 			setVariable( key.toString(), bundle.properties.getProperty(key.toString()));
 		}
 
 		/* the private folder for this service */
 		setVariable( "data.path", Utils.getCanonicalPath(fRepo.getFile()) );
 
 		/* some 'special' variables */
 		setVariable("_rid", rid());
 		setVariable("_result_url", getResultURL());
 		
 		
 		/* 
 		 * 3. add all fields value as context variables 
 		 */
 		for( Field field : input.fields() ) {
 			setVariable(field);
 		}
 		
 		
 		/*
 		 * 4. store the input so that can be used to re-submit job execution
 		 */
 		input.save( fRepo.getInputFile() );
 	}
 
 	
 	String getResultURL() {
 		String bundle = Scope.Params.current().get("bundle");
 		String rid = rid();
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("bundle", bundle);
 		params.put("rid", rid);
 		
 		return Request.current().getBase() + Router.reverse("Application.result", params).toString();
 	}
 	
 	public boolean start() {
 
 		/* check if a main process is defined otherwise skip it */
 		if( process == null ) { 
 			Logger.warn("Nothing to process");
 			return false;
 		};
 
 		
 		/* try to lock this context for execution */
 		if( !fRepo.lock() ) {
 			// if cannot be lock it mean that the job is still running 
 			return false;
 		}
 		
 		
 		/* create an aysnc execution context */
 		Job<?> job = new Job() {
     		@Override
     		public void doJob() throws Exception {
     			/*
     			 * run the alignment job
     			 */
     			Service.current(Service.this);
     			try {
     				/* run the job */
     				Service.this.run();
     			}
     			finally  {
     				try { Service.this.fRepo.unlock(); } catch( Exception e ) { Logger.error(e, "Failure on context unlock"); }
     				try { Service.this.trace(); } catch( Exception e ) { Logger.error(e, "Failure on request logging"); }
     				Service.release();
     			}
     		}
     	}; 
     	
     	/* 
     	 * fire the job asynchronously 
     	 */
     	job.now();
     	return true;
 	}
 
 	/**
 	 * Append a line in the server requests log with the following format 
 	 * 
 	 * <start time>, <user ip>, <bundle name>, <service name>, <request id>, <elapsed time>, <status> 
 	 */
 	void trace() {
 		try {
 			DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			StringBuilder line = new StringBuilder();
 			line.append( fmt.format(fStartTime) ).append(",")
 				.append( fRemoteAddress ).append(",")
 				.append( this.bundle.name ).append(",")
 				.append( this.name ).append(",")
 				.append( this.fRid ).append(",")
 				.append( fOutResult!=null ? String.valueOf(fOutResult.elapsedTime) : "-") .append(",")
 				.append( fOutResult!=null && fOutResult.status!=null ? fOutResult.status.name() : "-");
 
			PrintWriter out = new PrintWriter(new FileWriter(AppProps.SERVER_USAGE_FILE, true), true);
 			out.println(line.toString());
 			out.close();
 		} 
 		catch (IOException e) {
			throw new QuickException(e, "Unable to trace request in log file: %s", AppProps.SERVER_USAGE_FILE );
 		}
 	}
 
 	void run() {
 		
 		/* 
 		 * initialize the process 
 		 */
 		process.init(new CommandCtx( fCtx )); // <-- pass to the command context the save variables
 		
 		/* 
 		 * the main execution 
 		 */
 		fOutResult = null;
 		boolean success = false; 
 		
 		
 		try {
 			/* run the main job */
 			try {
 				success = process.execute();
 			}
 			finally {
 				
 				/*
 				 * if result is OK handle the commands for valid case  
 				 */
 				OutSection branch = getOutSection(success);
 				fOutResult = branch.result;
 				
 				if( process.hasResult() ) {
 					fOutResult.addAll(process.getResult());
 					fOutResult.elapsedTime = process.elapsedTime;
 					fOutResult.status = success ? Status.DONE : Status.FAILED;
 					fOutResult.bundle = bundle.name;
 					fOutResult.service = this.name;
 					fOutResult.title = this.title;
 					fOutResult.cite = this.cite;
 				}
 				
 				/*
 				 * execute the result events 
 				 */
 				if( branch.hasEvents() ) {
 					branch.events.execute();
 					if( branch.events.getResult() != null ) {
 						branch.result.addAll( branch.events.getResult() );
 					}
 				}	
 				
 				/*
 				 * normalize path on result items 
 				 */
 				resolveOutFilesPath();
 			}
 
 		}
 		catch( Exception e ) {
 			/* trace the error in the log file */
 			Logger.error(e, "Error processing request # %s", fRid);
 			if( fOutResult == null ) fOutResult = defaultOutSection(false).result;
 			fOutResult.status = Status.FAILED;
 			fOutResult.addError( e.getMessage() );
 			
 		}
 		finally {
 			/* garantee to save the result object in any case */
 			fRepo.saveResult(fOutResult);
 		}
 
 
 	}
 	
 	/**
 	 * Resolve file system and web paths for files in {@link OutItem} instances 
 	 */
 	void resolveOutFilesPath() { 
 		for( OutItem item : fOutResult.getItems() ) { 
 			if( item.file == null && item.name != null) { 
 				item.file = this.repo().getFile(item.name);
 			}
 			
 			if( item.webpath == null && item.file != null ) { 
 				item.webpath = webPathFor(item.file);
 			}
 			
 		}
 	}
 
 
 	
 	private static String webPathFor( File file ) {
 		if( file == null ) { 
 			return null;
 		}
 		/*
 		 * the file path have to be published under the framework root, 
 		 * being so the 'framework path' is the prefix of the file full path
 		 */
 		String context = AppProps.instance().getContextPath();
 		String path = Utils.getCanonicalPath(file);
 		String root = AppProps.instance().getDataPath();
 		
 		String result = null;
 		int p = path.indexOf(root);
 		if( p==0 ) {
 			result = path.substring(root.length());
 			if( result.charAt(0) != '/' ) {
 				result = "/" + result;
 			}
 			result = "/data" + result;
 			
 			if( Utils.isNotEmpty(context)) {
 				result = context + result;
 			}
 			
 		}
 		
 		return result;
 	}	
 	
 	OutSection getOutSection(boolean status) {
 		/* initialize the standard output if it has not been specified */
 		OutSection section = defaultOutSection(status); 
 		
 		if( output != null ) {
 			section.addAll( status ? output.valid : output.fail );
 		}
 
 		/* garantee a result object */
 		if( section.result == null ) {
 			section.result = new OutResult();
 		}
 
 		return section;
 	} 
 	
 	OutSection defaultOutSection(boolean success) {
 		OutSection out = null;
 		
 		if( bundle != null && bundle.def != null ) {
 			if( success && bundle.def.validResult != null ) {
 				out = new OutSection(bundle.def.validResult);
 			}
 			else if( !success && bundle.def.failResult != null ){
 				out = new OutSection(bundle.def.failResult);
 			}
 		}
 		
 		if( out == null ) {
 			out = new OutSection();
 			out.result = new OutResult();
 		}
 		
 		return out;
 	}
 	
 	
 	/**
 	 * Replace all variables in the environment with the specified context and return it
 	 * 
 	 * @param fCtx
 	 * @return
 	 */
 	public Map<String,String> defaultEnvironment() {
 
 		if( bundle == null || bundle.environment == null ) { 
 			return null;
 		}
 	
 		
 		Map<String,String> result = new HashMap<String,String>();
 		
         // Resolve ${..}
         Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
         for (Object key : bundle.environment.keySet()) {
             String value = bundle.environment.getProperty(key.toString());
             Matcher matcher = pattern.matcher(value);
             StringBuffer newValue = new StringBuffer();
             while (matcher.find()) {
                 String var = matcher.group(1);
                 String r = null;
                 if( var != null && var.startsWith("env.")) { 
                 	r = System.getenv(var.substring(4));
                 }
                 else if( fCtx != null ) { 
                 	r = fCtx.get(var) != null ? fCtx.get(var).toString() : null; 
                 }
                 
                 if (r == null) {
                     Logger.warn("Cannot replace %s in configuration (%s=%s)", var, key, value);
                     continue;
                 }
                 matcher.appendReplacement(newValue, r.replaceAll("\\\\", "\\\\\\\\"));
             }
             matcher.appendTail(newValue);
             result.put(key.toString(), newValue.toString());
         }		
         
         return result;
 	}		
 
 }
