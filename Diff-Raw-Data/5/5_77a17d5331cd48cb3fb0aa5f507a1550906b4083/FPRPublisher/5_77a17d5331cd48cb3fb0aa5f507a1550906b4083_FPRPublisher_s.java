 package org.jvnet.hudson.plugins.fortify360;
 
 import java.io.*;
 import java.lang.reflect.InvocationTargetException;
 import java.util.*;
 import hudson.*;
 import hudson.scm.ChangeLogSet;
 import hudson.scm.ChangeLogSet.AffectedFile;
 import hudson.scm.ChangeLogSet.Entry;
 import hudson.tasks.*;
 import hudson.util.FormValidation;
 import hudson.model.*;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.apache.commons.beanutils.MethodUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.StringUtils;
 
 /** Fortify 360 Publisher Hudson Plugin
  * 
  * <p>This plugin will mainly do three things
  * <ul>
  *   <li>Calculate NVS from the FPR and plot graph in the project main page</li>
  *   <li>Upload FPR to Fortify 360 Server</li>    
  *   <li>Make a build to be UNSTABLE if some critical vulnerabilities are found</li>
  *   <li>Run the job assignment script as a Javascript</li>
  * </ul>
  * <p>
  * When Fortify360 plugin runs, it will load the correct version of fortifyclient.jar, wsclient.jar 
  * and wsobjects.jar dynamically. And Fortify360 plugin will call FortifyClient via reflection, hence 
  * removing all the dependency between Fortify360 hudson plugin with F360 server version. Therefore, 
  * Fortify360 plugin can work with any versions of F360server.
  * <p>
  * 
  * @author sng
  *
  */
 @SuppressWarnings("unchecked")
 public class FPRPublisher extends Recorder {
 	
 	private String fpr;
 	private String filterSet;
 	private String searchCondition;
 	private Long f360projId;
 	private String auditToken;
 	private Integer uploadWaitTime;
 	private String auditScript;
 	
 	@DataBoundConstructor
 	public FPRPublisher(String fpr, String filterSet, String searchCondition, Long f360projId, String auditToken, Integer uploadWaitTime, String auditScript) {
 		
 		this.fpr = fpr;
 		this.filterSet = filterSet;
 		this.searchCondition = searchCondition;
 		this.f360projId = f360projId;
 		
 		this.auditToken = auditToken;
 		this.uploadWaitTime = uploadWaitTime;
 		this.auditScript = auditScript;
 
 		/*
 		System.out.println("###########################################");
 		System.out.printf("FPR=%s, filterSet=%s, searchCondition=%s, UploadToF360=%s\n", this.fpr, this.filterSet, this.searchCondition, this.f360projId);
 		System.out.println("###########################################");
 		*/
 	}
 	
 	public String getFpr() {
 		return fpr;
 	}
 	
 	public String getFilterSet() {
 		return filterSet;
 	}
 	
 	public String getSearchCondition() {
 		return searchCondition;
 	}
 	
 	public Long getF360projId() {
 		return f360projId;
 	}
 	
 	/** The AnuditToken
 	 * <p>
 	 * This is per project because different project may have different access right
 	 * </p>
 	 * 
 	 * @return
 	 */
 	public String getAuditToken() {
 		return auditToken;
 	}
 	
 	/** Minute(s) to wait between upload and run the Javascrip AutoAssignment job
 	 * <p>
 	 * F360Srv FPR upload is an async job, meaning I call the WS-API, the API will return once the file is uploaded. 
 	 * But at the server side, F360Srv is still processing the FPR, therefore, if I run the JobAssignment script immediately
 	 * I will run it against the old (last) FPR instead because the newly uploaded FPR is still being inserted into the DB
 	 * and not seen by the WS-client.
 	 * <p>
 	 * Fortify development team will add new API in the future, but before we can check the upload status, we can only sleep
 	 * for a while before we run the JobAssignment script.
 	 * <p>
 	 * Please be noted, in some cases, the newly uploaded FPR will need to be approved, that suitation is not handled currently
 	 * 
 	 * @return sleep time in minute(s), 0-60
 	 */
 	public Integer getUploadWaitTime() {
 		return uploadWaitTime;
 	}
 	
 	/** The Javascript (as a string) to run after uploaded the FPR to F360 Server
 	 * <ul>
 	 *   <li>Auto assign issues to users</li>
 	 *   <li>Auto audit/comment</li>
 	 * </ul>
 	 * 
 	 */
 	public String getAuditScript() {
 		return auditScript;
 	}
 	
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.NONE;
 	}
 	
 	@Override
     public Action getProjectAction(AbstractProject<?,?> project) {
         return new ChartAction(project);
     }
 
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
 		
 		PrintStream log = launcher.getListener().getLogger();		
 		log.println("Publishing Fortify 360 FPR Data");
 		
 		// calling the remote slave to retrieve the NVS
 		// build.getActions().add(new ChartAction(build.getProject()));
 		String jarsPath = DESCRIPTOR.getJarsPath();
 		String suggestedFortifyHome = null;
 		if ( !StringUtils.isBlank(jarsPath) ) {
 			// jarsPath should be <SCA_Install_Path>/Core/lib
 			File f = new File(jarsPath);
 			suggestedFortifyHome = f.getParentFile().getParentFile().toString();			
 		}
 		RemoteService service = new RemoteService(fpr, filterSet, searchCondition, suggestedFortifyHome);
 		FPRSummary summary = build.getWorkspace().act(service);
 		String logMsg = summary.getLogMessage();
 		if ( !StringUtils.isBlank(logMsg) ) log.println(logMsg);
 		
 		// if FPR is a remote FilePath, copy to local
 		File localFPR = null;
 		if ( summary.getFprFile().isRemote() ) {
 			localFPR = copyToLocalTmp(summary.getFprFile());
 		} else {
 			localFPR = new File(summary.getFprFile().toURI());
 		}
 		log.printf("Using FPR: %s\n", summary.getFprFile().toURI());
 		//if ( summary.getFprFile().isRemote() ) 
 		log.printf("Local FPR: %s\n", localFPR.getCanonicalFile());
 		log.printf("Calculated NVS = %f\n", summary.getNvs());
 		
 		// save data under the builds directory, this is always in Hudson master node
 		log.println("Saving FPR summary");
 		summary.save(new File(build.getRootDir(), FPRSummary.FILE_BASENAME));
 		
 		// if the project ID is not null, then we need to upload the FPR to 360 server
		if ( null != f360projId && DESCRIPTOR.canUploadToF360() ) {
 			// the FPR may be in remote slave, we need to call launcher to do this for me
 			log.printf("Uploading FPR to Fortify 360 Server at %s\n", DESCRIPTOR.getUrl());
 			try {
 				Object[] args = new Object[] { localFPR, f360projId};
 				invokeFortifyClient(DESCRIPTOR.getToken(), "uploadFPR", args, log);
 				log.println("FPR uploaded successfully");
 			} catch ( Throwable t ) {
 				log.println("Error uploading to F360 Server: " + DESCRIPTOR.getUrl());
 				t.printStackTrace(log);
 			} finally {
 				// if this is a remote FPR, I need to delete the local temp FPR after use
 				if ( summary.getFprFile().isRemote() ) {
 					if ( null != localFPR && localFPR.exists() ) {
 						try { 
 							boolean deleted = localFPR.delete();
 							if ( !deleted ) log.printf("Can't delete local FPR file: %s\n", localFPR.getCanonicalFile());
 						} catch ( Exception e ) {
 							e.printStackTrace(log);
 						}
 					}
 				}
 			}
 		}
 		
 		// now check if the fail count
 		if ( !StringUtils.isBlank(searchCondition) ) {
 			Integer failedCount = summary.getFailedCount();
 			if ( null != failedCount && failedCount > 0 ) {
 				log.printf("Fortify 360 Plugin: this build is unstable because there are %d critical vulnerabilities\n", failedCount);
 				build.setResult(Result.UNSTABLE);
 			}
 		}
 		
 		// now do job assignment
		if ( !StringUtils.isBlank(auditScript) ) {
 			int sleep = (uploadWaitTime != null) ? uploadWaitTime : 1;
 			log.printf("Sleep for %d minute(s)\n", sleep);
 			sleep = sleep * 60 * 1000; // wait time is in minute(s)
 			long sleepUntil = System.currentTimeMillis() + sleep;
 			while(true) {
 				long diff = sleepUntil - System.currentTimeMillis();
 				if ( diff > 0 ) {
 					try {
 						Thread.sleep(diff);
 					} catch ( InterruptedException e ) { }
 				} else {
 					break;
 				}
 			}
 			log.printf("Auto JobAssignment, AuditToken = %s\n", auditToken);
 			try {
 				jobAssignment(build, log);
 			} catch ( Throwable t ) {
 				log.println("Error auditing FPR");
 				t.printStackTrace(log);
 			}
 		}
 		
 		return true;
 	}
 		
 	private void jobAssignment(AbstractBuild<?, ?> build, PrintStream log) throws Exception {
 		
 		ChangeLogSet<? extends Entry> set = build.getChangeSet();
 		Map<String, String> changeLog = new HashMap<String, String>();
 		// noted: ChangeLogSet<> is iterable, and is always in sequential order
 		// older changes come iterated first, latest changes come later
 		// so we don't need to do any checking when put it in the map
 		for(Entry entry : set) {
 			for(AffectedFile file : entry.getAffectedFiles()) {
 				changeLog.put(file.getPath(), entry.getAuthor().toString());
 			}
 		}
 		
 		//void auditFPR(Long projectVersionId, String auditScript, String filterSet, Map<String, String> changeLog, PrintWriter log)
 		Object[] args = new Object[5];
 		args[0] = f360projId;
 		args[1] = auditScript;
 		args[2] = filterSet;
 		args[3] = changeLog;
 		// auditFPR will call Javascript, better use PrintWriter instead of PrintSteam 
 		// PrintStream will convert strings to bytes
 		args[4] = new PrintWriter(log); 
 		// the last log is mainly for getInstance() as the third argument, that's is PrintStream
 		invokeFortifyClient(auditToken, "auditFPR", args, log);
 	}
 	
 	private static Object invokeFortifyClient(String token, String methodName, Object[] args, PrintStream log) throws Exception {
 		String url = DESCRIPTOR.getUrl() + "/fm-ws/services";
 		FortifyClientClassLoader loader = FortifyClientClassLoader.getInstance(DESCRIPTOR.getJarsPath(), DESCRIPTOR.getVersion(), log);
 		loader.bindCurrentThread();
 		try {
 			Object fortifyclient = loader.loadClass("org.jvnet.hudson.plugins.fortify360.fortifyclient.FortifyClient").newInstance();
 			MethodUtils.invokeMethod(fortifyclient, "init", new String[] {url, token});
 			Object out = MethodUtils.invokeMethod(fortifyclient, methodName, args);
 			return out;
 		} catch (InvocationTargetException e ) {
 			System.out.println("Catch exception " + e.getClass().toString());
 			Throwable t = e.getCause();
 			// rethrow the root cause, not the one wrapped by InvocationTargetException
 			if ( t instanceof Exception ) throw (Exception)t;
 			else throw e;
 		} finally {
 			loader.unbindCurrentThread();
 		}
 	}
 	
 	private File copyToLocalTmp(FilePath file) throws IOException, InterruptedException {
 		UUID uuid = UUID.randomUUID();
 		String tmp = System.getProperty("java.io.tmpdir");
 		String s = System.getProperty("file.separator");
 		File tmpFile = new File(tmp + s + uuid + s + file.getName());
 		FilePath tmpFP = new FilePath(tmpFile);
 		file.copyTo(tmpFP);
 		return tmpFile;
 	}
 	
 	/** Use fortifyclient to upload FPR to F360 Server
 	 * 
 	 * @deprecated
 	 * @param fprFullPath
 	 * @param launcher
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unused")
 	private void uploadToF360_(String fprFullPath, Launcher launcher) throws IOException {
 		// fortifyclient uploadFPR projectID <proj_ID> -file XXXX.fpr -url http://fortify.ca.com:8080/f360 -authtoken XXXX
 		String array[] = {"fortifyclient", "uploadFPR", "-projectID", "unknown", "-file", "unknown", "-url", "unknown", "-authtoken", "unknown"};
 		
 		String os = System.getProperty("os.name");
 		String image = os.matches("Win.*|.*win.*") ? "fortifyclient.bat" : "fortifyclient";
 		array[0] = image;
 		
 		array[3] = f360projId.toString();   // project Id
 		array[5] = fprFullPath;             // fpr
 		array[7] = DESCRIPTOR.getUrl();     // F360 server url
 		array[9] = DESCRIPTOR.getToken();   // authentication token
 		
 		Launcher.ProcStarter proc = launcher.launch();
 		proc.cmds(array);
 		proc.start();		
 	}
 
 	@Extension
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 	
     public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
     	
     	/** The Fortify 360 Server URL, e.g. http://localhost:8080/f360 */
     	private String url;
     	
     	/** The Fortify 360 Server AnalysisUploadToken */
     	private String token;
     	
     	/** The Path that contains wsclient.jar and wsobject.jar, will auto-detect if not defined */
     	private String jarsPath;
     	
     	/** F360 version, e.g. 2.5/2.6, will auto detect by checking the md5 of wsclient.jar if not defined */
     	private String version;
     	
     	private List<ProjectVersionBean> f360projList;
     	
     	public DescriptorImpl() {
     		super(FPRPublisher.class);
     		load();
     	}
 
     	@Override
     	public boolean isApplicable(Class<? extends AbstractProject> jobType) {
     		return true; // applicable to all project type
     	}
 
     	@Override
     	public String getDisplayName() {
     		return "Fortify FPR Publisher";
     	}
     	
     	public String getUrl() {
     		return url;
     	}
     	
     	public String getToken() {
     		return token;
     	}
     	
     	public boolean canUploadToF360() {
     		return ( null != url && null != token );
     	}
     	
     	public String getJarsPath() {
     		return jarsPath;
     	}
     	
     	public String getVersion() {
     		return version;
     	}
     	
     	public FormValidation doCheckJarsPath(@QueryParameter String value) {
     		if ( StringUtils.isBlank(value) ) return FormValidation.ok();
     		
 			File wsclient = new File(value, "wsclient.jar");
 			File wsobjects = new File(value, "wsobjects.jar");
 			if ( wsclient.exists() && wsobjects.exists() ) {
 				return FormValidation.ok();
 			} else {
 				return FormValidation.error("Can't locate wsclient.jar and wsobjects.jar inside the path");
 			}
     	}    	
 
     	public FormValidation doCheckVersion(@QueryParameter String value) {
     		if ( StringUtils.isBlank(value) ) return FormValidation.ok();
 
     		String ver = value.trim();
 			Set<String> supportedVersions = FortifyClientClassLoader.getSupportedVersions();
     		if ( supportedVersions.contains(ver) ) {
     			return FormValidation.ok();
 			} else if ( supportedVersions.contains(ver + "0") ) {
 				return FormValidation.error("Please change version number to " + ver + "0");
     		} else {
     			return FormValidation.error("Not a valid version number");
     		}
     	}    
     	    	
     	public FormValidation doTestConnection(@QueryParameter String url, @QueryParameter String token,
     			                               @QueryParameter String jarsPath, @QueryParameter String version) {
     		
     		if ( StringUtils.isBlank(url) ) return  FormValidation.error("URL can't be empty");
     		if ( StringUtils.isBlank(token) ) return FormValidation.error("Token can't be empty");
     		
     		// backup original values
     		String orig_url = this.url;
     		String orig_jarsPath = this.jarsPath;
     		String orig_token = this.token;
     		String orig_version = this.version;
     		this.url = url;
     		this.token = token;
     		this.jarsPath = jarsPath;
     		this.version = version;
 			try {
 				// as long as no exception, that's ok
 				FPRPublisher.invokeFortifyClient(token, "getProjectList", null, System.out);
 	    		return FormValidation.ok("Connection Successful");
 			} catch (Throwable t ) {
 				return FormValidation.error(t.getMessage());
 			} finally {
 				this.url = orig_url;
 	    		this.token = orig_token;				
 	    		this.jarsPath = orig_jarsPath;
 	    		this.version = orig_version;
 			}
     	}
     	    	
     	public void doRefreshProjects(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value) throws Exception {
     		try {
     			// alwasy retrieve data from f360 server
     			f360projList = getF360projListNoCache();
     			// and then convert it to JSON
     			StringBuilder buf = new StringBuilder();
     			buf.append("[\n");
     			for(int i=0; i<f360projList.size(); i++) {
     				ProjectVersionBean b = f360projList.get(i);
     				buf.append("{ \"name\": \"" + b.getName() + "\", \"id\": \"" + b.getId() + "\" }");
     				if ( i != f360projList.size()-1 ) buf.append(",\n");
     				else buf.append("\n");
     			}
     			buf.append("]");
     			// send HTML data directly
     			rsp.setContentType("text/html;charset=UTF-8");
     			rsp.getWriter().print(buf.toString());
     		} catch ( Exception e ) {
     			e.printStackTrace();
     			throw e;
     		}
     	}  
     	
     	public FormValidation doCheckFpr(@QueryParameter String value) {
     		if ( StringUtils.isBlank(value) ) {
     			return FormValidation.ok();
     		} else if ( !value.contains("/") && !value.contains("\\") 
     		&& FilenameUtils.isExtension(value.toLowerCase(), new String[] {"fpr", "fvdl"}) )  {
     			return FormValidation.ok();
     		} else {
     			return FormValidation.error("The FPR filename should be in basename *ONLY*, with extension FPR or FVDL");
     		}
     	}    	
     	
     	public FormValidation doCheckUploadWaitTime(@QueryParameter String value) {
     		if ( StringUtils.isBlank(value) ) {
     			return FormValidation.ok();
     		} else {
     			int x = -1;
     			try {
     				x = Integer.parseInt(value);
         			if ( x >= 0 && x <= 60 ) return FormValidation.ok(); 
     			} catch ( NumberFormatException e )  {}
     			return FormValidation.error("The unit is in minutes, and in the range of 0 to 60");
     		}
     	}    	
     	
     	public FormValidation doCheckAuditScript(@QueryParameter String value) {
     		if ( StringUtils.isBlank(value) ) {
     			return FormValidation.ok();
     		} else {
     			Object[] args = new Object[1];
     			args[0] = value;
     			try {
 					FPRPublisher.invokeFortifyClient("", "checkAuditScript", args, System.out);
 				} catch (Exception e) {
 	    			return FormValidation.error(e.getMessage());
 				}
 				return FormValidation.ok();
     		}
     	}    
     	
     	@Override
         public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
             // to persist global configuration information,
             // set that to properties and call save().
             url = o.getString("url");
             token = o.getString("token");
             jarsPath = o.getString("jarsPath");
             version = o.getString("version");
             save();
             
             // we have to reset FortifyClientClassLoader, since it is a singleton
             FortifyClientClassLoader.reset();
             
             return super.configure(req,o);
         }
         
     	/** Get Project Name ID list from F360 Server via WS
     	 * <br/>Basically only for config.jelly pull down menu
     	 * 
     	 * @return A list of Project Name and ID
     	 * @throws FortifyWebServiceException
     	 */
     	public List<ProjectVersionBean> getF360projList() {
     		if ( null == f360projList ) {
     			f360projList = getF360projListNoCache(); 
     		}
 			return f360projList;
     	}
     	
     	private List<ProjectVersionBean> getF360projListNoCache() {
     		
     		if ( null != DESCRIPTOR.getUrl() && null != DESCRIPTOR.getToken() ) {
     			try {
     				Map<String, Long> map = (Map<String, Long>)invokeFortifyClient(DESCRIPTOR.getToken(), "getProjectList", null, System.out);
     				List<ProjectVersionBean> list = new ArrayList<ProjectVersionBean>();
     				for(String name : map.keySet()) {
     					ProjectVersionBean proj = new ProjectVersionBean(name, map.get(name));
     					list.add(proj);
     				}
 					Collections.sort(list);
     				return list;
     				
     			// many strange thing can happen.... need to catch throwable
     			} catch ( Throwable e ) {
     				e.printStackTrace();
     				return null;
     			}
     		} else {
     			return null;
     		}
     	}
     }
 }
