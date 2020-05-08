 package controllers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import models.AppProps;
 import models.Bundle;
 import models.CmdArgs;
 import models.Field;
 import models.OutResult;
 import models.Repo;
 import models.Service;
 import models.Status;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.blackcoffee.commons.utils.CmdLineUtils;
 
 import play.Logger;
 import play.data.validation.Validation;
 import play.libs.IO;
 import play.libs.MimeTypes;
 import play.mvc.Before;
 import play.mvc.Finally;
 import play.mvc.Http.Cookie;
 import play.mvc.Router;
 import play.mvc.Util;
 import query.History;
 import util.Utils;
 import bundle.BundleRegistry;
 
 /**
  * The main application controller 
  * 
  * @author Paolo Di Tommaso
  *
  */
 
 public class Application extends CommonController {
 	
 	static List<String> PAGES = Arrays.asList("index", "history", "references", "help", "contacts");
 
 	static private ThreadLocal<Bundle> bundle = new ThreadLocal<Bundle>();
 	
 	@Before
 	static void before(String bundle) { 
 		assertNotEmpty(bundle, "Missing bundle argument");
 		
 		Bundle _bundle = BundleRegistry.instance().get(bundle);
 		if( _bundle == null ) { 
 			error("Cannot find bundle named: " + bundle);
 		}
 		
 		/* 
 		 * 1) save the bundle instance in the current context 
 		 * 2) save as route argument
 		 * 3) inject implict variables 
 		 */
 		Application.bundle.set(_bundle);
 		routeArgs.put("bundle", bundle);
 		injectImplicitVars(bundle);
 	}
 	
 	@Finally
 	static void release() {
 		Service.release();
 		Application.bundle.remove();
 	}
 	
 	/** 
 	 * Renders the main application index page
 	 */
     public static void index() {
     	redirect("Application.html", "index.html");
     }
     
     /**
      * Handle request to display the <i>result</i> page
      * 
      * @param rid the unique request identifier 
      */
     public static void result(String rid, Boolean ... cached ) {		
     	showResultFor(rid,"result.html",cached);
     }
     
     @Util
     static void showResultFor(String rid, String template, Boolean ... cached ) {
 		assertNotEmpty(rid, "Missing 'rid' argument on #result action");
     	
     	final Repo ctx = new Repo(rid,false);
     	final Status status = ctx.getStatus();
 
     	if( status.isDone()) {
     		// touch it to update the last accessed time 
     		ctx.touch(); 
 		
     		// if the file exists load the result object and show it
 			OutResult result = ctx.getResult();
 			renderArgs.put("rid", rid);
 			renderArgs.put("ctx", ctx);
 			renderArgs.put("result", result);
 			renderArgs.put("cached", cached);
 	
     		renderBundlePage(template);
 		}
 		else if( status.isFailed() ) {
 			OutResult result = ctx.getResult();
 	    	render("Application/failed.html", rid, ctx, result, cached);
 		}
 		else if( status.isRunning() ) {
 			responseNoCache();
 			render("Application/wait.html", rid );
 		}
 		else {
 			int maxDays = AppProps.instance().getDataCacheDuration() / 60 / 60 / 24;
 	    	render("Application/oops.html", rid, maxDays);
 		}
    	
     } 
     
     /**
      * Embed the Jalview applet 
      * 
      * @param rid the request ID to show
      */
     public static void jalview(String rid) {
     	renderArgs.put("_nowrap", true);
     	showResultFor(rid, "jalview.html", false);
     } 
 	
     /**
      * Renders the user requests 'history' page 
      */
     public static void history() {
     	render();
     } 
     
 	/**
 	 * Renders the history html table  
 	 */
 	public static void historyTable() {	
 		String sid = session.getId();
     	String userid = session.get("trusted_user");
 		
 		Date since = new Date( System.currentTimeMillis() - AppProps.instance().getDataCacheDuration() *1000 ); 
 		List<History> recent = History.findBySessionAndEmailAndSinceDate(sid, userid, since);
 		
 		responseNoCache();
     	renderArgs.put("trusted_user", userid);
     	renderArgs.put("recent", recent);
 		render();
 	}
 	
 	/**
 	 * Delete an entry from the user history log.
 	 * <p>
 	 * This method is meant to be used as a ajax request
 	 * 
 	 * @param rid the unique request identifier 
 	 */
 	public static void historyDel( String rid ) {
 		Boolean result = History.deleteByRequestId(rid);
 		renderText(result);
 	}
 	
 	public static void historyDelAll() {
 		String sid = session.getId();
 		Cookie email = request.cookies.get("email");
 	
 		Boolean result = History.deleteBySessionAndEmail(sid, email != null ? email.value : null );
 		renderText(result);
 	}
 
 
 	/**
 	 * Check the current status for the alignment request specified by <code>rid</code>
 	 * 
 	 * @param rid the request unique identifier
 	 */
 	public static void status(String rid) {
 		assertNotEmpty(rid, "Missing 'rid' argument on #status action");
 
 		Repo ctx = new Repo(rid,false);
 		renderText(ctx.getStatus().toString());
 	}
 	
 	public static void replay( String rid ) {
 		assertNotEmpty(rid, "Missing 'rid' argument on #replay action");
 	
 		/* 
 		 * 1. check if a result exists 
 		 */
 		Repo repo = new Repo(rid);
 		if( !repo.hasResult() ) {
 			notFound(String.format("The specified request ID does not exist (%s)", rid));
 		}
 
 		
 		/* 
 		 * 2. create the service and bind the stored values 
 		 */
 		String serviceName = repo.getResult().service;
 		String bundleName = bundle.get().name;
 		Service service = service(bundleName,serviceName);
 
 		Map<String,Object> args = new HashMap<String, Object>(2);
     	args.put("bundle", bundleName );
     	args.put("name", service.name);
     	args.put("replay", rid);
     	redirect( Router.reverse(service.action, args).url );
 
 	}
 
 	public static void submit( String rid ) {
 		assertNotEmpty(rid, "Missing 'rid' argument on #submit action");
 	
 		/*
 		 * 1. check and load the repo context object 
 		 */
 		Repo repo = new Repo(rid);
 		if( !repo.hasResult() ) {
 			notFound(String.format("The specified request ID does not exist (%s)", rid));
 		}
 
 		/* 
 		 * 2. create and bind the stored input values 
 		 */
 		OutResult result = repo.getResult(); 
 		Service service = service(bundle.get().name,result.service).copy();
 		Service.current(service);
 		service.input = repo.getInput();
 		
 		/* 
 		 * 4. re-execute with caching feature disabled
 		 */
 		exec(bundle.get().name,service,false);
 	}
 	
 	/**
 	 * Gateway is meant to pass parameters to the input forms. All URL paramerts 
 	 * matching a input field in the form will be filled with that value
 	 * 
 	 * @param name the service name for which render the input form 
 	 */
 	public static void gateway( String name ) { 
 		/* 
 		 * if the name is missing use the first as default 
 		 */
 		if( Utils.isEmpty(name) && bundle.get().services!=null && bundle.get().services.size()>0 ) { 
 			name = bundle.get().services.get(0).name;
 		}
 		
 		if( Utils.isEmpty(name) ) { 
 			error("Missing service for name for bundle: " + bundle);
 		}
 		
 		Service service = service(bundle.get().name,name).copy();
 		
 		/* 
 		 * bind any input parameters to make it possible to enters params 
 		 * trought the URL 
 		 */
 		List<Field> fields = service.input.fields();
 		for( Field field : fields ) { 
 			String val;
 			if( Utils.isNotEmpty(val = params.get(field.name))) {
 				field.value = val;
 			} 
 		}
 		
 		/* render the page */
 		render("Application/main.html", service);		
 	}
 	
 	/**
 	 * Renders a generic t-coffee 'service' i.e. a specific configuration defined in the main application file 
 	 * 
 	 * @param name the <i>service</i> name for which render the input form
 	 */
 	public static void main(String name) {
 		
 		if( isGET() ) {
 
 			/*
 			 * when the 'replay' params is provided the page is reloaded 
 			 * with the previous run data and options
 			 */
 			if( params._contains("replay") ) { 
 				String rid = params.get("replay");
 				
 				Service service = getServiceByRid(rid);
 				render(service);
 				return;
 			}
 			
 			/* 
 			 * if the name is missing use the first as default 
 			 */
 			if( Utils.isEmpty(name) && bundle.get().services!=null && bundle.get().services.size()>0 ) { 
 				name = bundle.get().services.get(0).name;
 			}
 			
 			if( Utils.isEmpty(name) ) { 
 				error("Missing service for name for bundle: " + bundle);
 			}
 			
 			Service service = service(bundle.get().name,name);
 			
 			/* retrieve the 'email' cookie */
 			String email = request.cookies.get("email") != null ? request.cookies.get("email").value : null;
 			Field field = service.input.field("email");
 			if( field != null && field.value == null && StringUtils.isNotBlank(email) ) {
 				// create a copy of this service to avoid that the email will be saved 
 				// in the object 'prototype'
 				service = service.copy();
 				service.input.field("email").value = email;
 			}
 			
 			/* render the page */
 			render(service);
 			return;
 		}
 
 		/*
 		 * process the submitted data
 		 */
 		Service service = service(bundle.get().name,name).copy();
 		Service.current(service);
 
 		if( !service.validate(params) ) {
 			/* if the validation FAIL go back to the service page */
 			renderArgs.put("service", service);
 			render();
 			return;
 		} 
 
 		exec(bundle.get().name,service, true);
 	}
 	
 	static void exec( String bundle, Service service, boolean enableCaching ) {
 		
 		/*
 		 * 1. prepare for the execution
 		 */
 		service.init(enableCaching);
 		
 		
 		/*
 		 * 2. check if this request has already been processed in some way 
 		 */
 		Status status = service.repo().getStatus();
 		if( !status.isReady() ) {
 	    	Logger.debug("Current request status: '%s'. Forward to result page with rid: %s", status, service.rid());
 	    	result(service.rid(), service.repo().cached);
 	    	return;
 		}
 
 		/*
 		 * 3. fire the job 
 		 */
 		if( service.start() ) {
 	    	// store an cookie containing the user email 
 	    	storeEmailCookie(params.get("email"));
 		}
 		
 
     	/*
     	 * 5. forwards to the result page 
     	 */
     	Logger.debug("Forward to result page with rid: %s", service.rid());
     	result(service.rid());			
 	}
 
 
 	public static void servePublic( String path ) { 
 		assertNotEmpty(path, "Missing 'path' argument on #servePublic action");
 		response.contentType = MimeTypes.getMimeType(path);
 		renderStaticResponse();
 		renderFile(bundle.get().publicPath, path);
 	}
 	
 	/**
 	 * Renders a generic page provided the by bundle 
 	 * 
 	 * @param bundle 
 	 * @param path
 	 */
 	public static void html( String page ) { 
 		assertNotEmpty(page, "Missing 'path' argument on #html action");
 		renderBundlePage(page);
 	}
 	
 	/**
 	 * Try to load the specied page template in the bundle context 
 	 * if does not exists fallback on the application scope 
 	 * 
 	 * @param page
 	 * @param args
 	 */
 	static void renderBundlePage( String page, Object... args) { 
 		Bundle bundle = Application.bundle.get();
 		if( bundle != null && bundle.pagesPath != null && bundle.pagesPath.child(page) .exists()) { 
 			renderArgs.put("_page", page);
 			render("Application/_wrapper.html", args);
 		}
 		else { 
 			render("Application/" + page, args);
 		}
 
 	}
 
 
 	/** 
 	 * Render the bundle css content
 	 */
 	public static void css() { 
 		File file = Application.bundle.get().cssPath;
 		if( file == null || !file.exists() ) { 
 			notFound("Cannot render CSS file for bundle: ", Application.bundle.get().name );
 		}
 		
 		response.contentType = "text/css";
 		renderText( IO.readContentAsString(file) );
 	}
 	
 	/**
 	 * T-Coffee advanced mode
 	 */
 	public static void advanced() { 
 
 		Service service = service(bundle.get().name,"adv-cmdline").copy();
 
 		if( isGET() ) { 
 			
 			/*
 			 * Check if has the replay parameter 
 			 */
 			String rid;
 			Repo repo;
 			String uploadedFiles = null;
 			
 			if( (rid=params.get("replay")) != null && (repo=new Repo(rid)).hasResult() ) { 
 				
 				
 				/* reuse the previous command line */
 				String cmdLine = IO.readContentAsString( repo.getResult().getCommandLine().file );
 				cmdLine = normalizeCmdLine(cmdLine);
 				service.input.field("cmdline").value = cmdLine;
 				
 				/* load the previously used input file as uploaded files */
 				uploadedFiles = repo.getInput().getValue("uploadedFiles", "");  // <-- return empty string by default 
 				List<File> files;
 				if( StringUtils.isEmpty(uploadedFiles) && (files=repo.getResult().getInputFiles()) != null && files.size()>0 ) { 
 					if( uploadedFiles.length()>0 ) {
 						uploadedFiles += ",";
 					}
 					for( File it : files ) {
 						uploadedFiles += it.getName();
 						// copy the file to the 
 						File target=null;
 						try {
 							target = Data.getUserFile(it.getName());
 							FileUtils.copyFile(it, target);
 						} catch (IOException e) {
 							Logger.warn("Cannot copy: '%s' to '%s'", it, target);
 						}
 					}
 				}
 			}
 			
 			/* retrieve the 'email' cookie */
 			String email = request.cookies.get("email") != null ? request.cookies.get("email").value : null;
 			Field field = service.input.field("email");
 			if( field != null && field.value == null && StringUtils.isNotBlank(email) ) {
 				field.value = email;
 			}		
 			
 			render(service, uploadedFiles);
 		}
 
 	
 		/* 
 		 * When is posted process the request
 		 */
 		Service.current(service);
 
 		
 		String uploadedFiles = params.get("uploadedFiles");
 		Logger.debug("Advanced - uploadedFiles: '%s'", uploadedFiles);
 		renderArgs.put("uploadedFiles", uploadedFiles);
 		
 		/* 
 		 * 0. bind and validate
 		 */
 		if( !service.validate(params) ) {
 			/* if the validation FAIL go back to the service page */
 			render(service);
 		} 
 
 		/* 
 		 * handle the uploaded files 
 		 */
 		
 		List<File> uploadedFilesList = new ArrayList<File>();
 		service.input.field("uploadedFiles").value = uploadedFiles; 
 		
 		if( StringUtils.isNotEmpty(uploadedFiles) ) {
 			String[] all = uploadedFiles.split(",");
 			for( String item : all ) {
 				if( StringUtils.isBlank(item) ) continue;
 				if( item.startsWith("file://") ) { item = item.substring(7); }
 				File file = Data.getUserFile(item);
 
 				// check if the file is valid otherwise return an error message
 				if( !file.exists() ) { 
 					String err = String.format("The file '%s' is no more available. Please upload it again or choose another file", file.getName());
 					Validation.addError("uploadedFiles", err, (String)null);
 					render(service);
 				} 
 				uploadedFilesList.add(file);
 			}
 
 		}   
 		// check if at least one file has been uploaded 
 		if( uploadedFiles.length() == 0 ) {
 			Validation.addError("uploadedFiles", "You should provided at lease one input file", (String)null);
 			render(service);
 		} 
 		
 
 		/* 
 		 * steup the command line 
 		 */
 		final String cmdline = normalizeCmdLine(service.input.field("cmdline").value);
 		
 		// verify that the entered command line 
 		if( cmdline != null && cmdline.length() > 900 ) {
 			String msg = 
 					"The command line you entered in not valid. " +
 					"Please note: in the above field you can enter only T-Coffee program arguments (not the input sequences). " +
 					"Input data have to be uploaded using the 'Upload' button in the box above this one." ;
 			Validation.addError("cmdline", msg, (String)null);
 			render(service, uploadedFilesList);
 		}
 
 		service.input.field("cmdline").value = cmdline;
 	
 		/* 
 		 * The command line cannot contains some 'special' character 
 		 * to avoid malicious commands entered 
 		 */
 		final String cmdLine = service.input.field("cmdline").value;
 		for( char ch : Data.COMMANDLINE_INVALID_CHARS ) { 
 			if( cmdLine.indexOf(ch) != -1 ) { 
 				String msg =  String.format("Program options cannot contains character '%s'",  ch);
 				Validation.addError("cmdline", msg, (String)null);
 				render(service, uploadedFilesList);
 			}
 		}
 		
 		
 		/* 
 		 * also avoid the use of some T-coffee options
 		 */
 		CmdArgs args = new CmdArgs(cmdLine);
 		String other_pg = args.get("other_pg");
 		List<String> valid = Arrays.asList(new String[] { "aln_compare", "seq_reformat", "trmsd", "extract_from_pdb" }); 
 		if( Utils.isNotEmpty(other_pg) && !valid.contains(other_pg)) { 
 			String msg = String.format("Option '-other_pg=%s' is not supported by the server", other_pg);
 			Validation.addError("cmdline", msg, (String)null);
 			render(service, uploadedFilesList);
 		}
 
 
 		
 		/*
 		 * 1. prepare for the execution
 		 */
 		service.init(false);
 		
 		/* 
 		 * 2. copy the files to the target folder 
 		 */
 		for( File upload: uploadedFilesList) { 
 			service.repo().store( upload.getAbsoluteFile() );
 		}
 		
 		/*
 		 * 3. check if this request has already been processed in some way 
 		 */
 		Status status = service.repo().getStatus();
 		if( !status.isReady() ) {
 	    	Logger.debug("Current request status: '%s'. Forward to result page with rid: %s", status, service.rid());
 	    	result(service.rid(), service.repo().cached);
 	    	return;
 		}
 
 		/*
 		 * 4. fire the job 
 		 */
 		if( service.start() ) {
 	    	// store an cookie containing the user email 
 	    	storeEmailCookie(params.get("email"));
 		}
 		
 
     	/*
     	 * 6. forwards to the result page 
     	 */
     	Logger.debug("Forward to result page with rid: %s", service.rid());
     	result(service.rid());		
 
 	}
 	
 	/*
 	 * Store the provided email address in a cookie  
 	 * 
 	 * @param email the address to store, if the value is empty 
 	 * will force the cookie to be removed 
 	 */
 	private static void storeEmailCookie(String email) {
     	if( StringUtils.isNotBlank(email) ) {
     		response.setCookie("email", email, "30d");
     	} 	
     	else { // remove the cookie 
     		response.removeCookie("email");
     	}
 	}
 
 	@Util
 	static String normalizeCmdLine(String cmdLine) {
 		
 		cmdLine = cmdLine.trim();
 
 		/* remove any 't_coffee' at the beginning og the string */ 
 		while( cmdLine.startsWith("t_coffee ") ) { 
 			cmdLine = cmdLine.substring("t_coffee ".length());
 		}
 
 		while( cmdLine.endsWith(" -quiet=stdout") || cmdLine.endsWith(" -quiet stdout")) {
 			cmdLine = cmdLine.substring(0, cmdLine.length()-" -quiet stdout".length());
 		}
 		
 		cmdLine = CmdLineUtils.normalize(cmdLine);
 		
 		return cmdLine;
 	}
 
 	@Util
 	static Service getServiceByRid( String rid ) { 
 		Repo repo = new Repo(rid);
 		if( !repo.hasResult() ) {
 			notFound(String.format("The specified request ID does not exist (%s)", rid));
 		}
 		
 		/*
 		 * copy the input file to the user area 
 		 */
 		for( File it: repo.getResult().getInputFiles()) {
 			File target=null;
 			try {
 				target = Data.getUserFile(it.getName());
 				FileUtils.copyFile(it, target);
 			} catch (IOException e) {
 				Logger.warn("Cannot copy file: '%s' to '%s'", it, target);
 			}
 		}
 		
 		
 		/* 
 		 * create the service and bind the stored values 
 		 */
 		String serviceName = repo.getResult().service;
 		String bundleName = repo.getResult().bundle;
 		Service service = service(bundleName,serviceName);
 		service = service.copy();
 		service.input = repo.getInput();
 
 		return service;
 	}
 	
 	
 	/**
 	 * Redirect the legacy 'play' action
 	 */
 	public static void legacyFwd() {
 		main( params.get("name") );
 	} 
 }
