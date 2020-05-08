 package com.coverity.scan.hudson;
 
 import java.net.URL;
 import java.net.HttpURLConnection;
 import java.net.URLEncoder;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import hudson.Launcher;
 import hudson.Extension;
 import hudson.XmlFile;
 import hudson.util.FormValidation;
 import org.hudsonci.maven.plugin.builder.MavenBuilder;
 import org.hudsonci.maven.plugin.builder.MavenBuilderDescriptor;
 //import org.hudsonci.utils.plugin.ui.RenderableEnum;
 import org.eclipse.jgit.transport.RemoteConfig;
 import org.eclipse.jgit.transport.URIish;
 import hudson.plugins.git.GitSCM;
 import hudson.model.AbstractBuild;
 import hudson.model.Action;
 import hudson.model.BuildListener;
 import hudson.model.JobProperty;
 import hudson.model.AbstractProject;
 import hudson.model.Result;
 import hudson.model.Cause;
 import hudson.model.Descriptor.FormException;
 import hudson.model.FreeStyleProject;
 import hudson.model.Job;
 import hudson.scm.SCMDescriptor;
 import hudson.scm.SCM;
 import hudson.scm.CVSSCM;
 import hudson.scm.ModuleLocation;
 import hudson.scm.SubversionSCM;
 import java.util.Arrays;
 import hudson.tasks.Builder;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Ant;
 import hudson.tasks.BatchFile;
 import hudson.tasks.Shell;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.QueryParameter;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl\#newInstance(StaplerRequest)} is invoked
  * and a new {@link ScanPluginBuilder} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link \#name})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link \#perform(AbstractBuild, Launcher, BuildListener)} method
  * will be invoked.
  */
 public class ScanPluginBuilder extends Builder {
 
     private String name;
 	private String password;
 	private String email;
 	private String project;
 	private String build_number;
 	private String proj_scm;
 	private String scm_command;
 	private String[] scm_commands;
 	private String proj_builder;
 	private String build_command;
 	private String build_comments;
 
     // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     @DataBoundConstructor
     public ScanPluginBuilder(String name, String password, String email, String project) {
         this.name = name;
         this.password = password;
         this.email = email;
         this.project = project;
         this.proj_builder="null";
         this.build_command="null";
         this.scm_command="null";
         this.proj_scm="null";
         this.scm_commands = new String[10];
     }
 
     /**
      * We'll use this from the <tt>config.jelly</tt>.
      */
     public String getName() {
         return name;
     }
     
     public String getPassword() {
     // this has to stay public if not the user will need to re-enter the password everytime the config page is opened
         return password;
     }
 
     public String getEmail() {
         return email;
     }
 
     public String getProject() {
         return project;
     }
     
     public String getBuildNumber() {
         return build_number;
     }
     
 	public String getProjSCM() {
         return proj_scm;
     }
      
 	public String getBuildComments() {
         return build_comments;
     }
   
 	public String getSCMCommand() {
         return scm_command;
     }
     
 	public String[] getSCMCommands() {
         return scm_commands;
     }
         
     public String getProjBuilder() {
         return proj_builder;
     }
     
     public String getBuildCommand() {
         return build_command;
     }
 /*
     private String encodeUTF8(String str){
     	try {
     		return URLEncoder.encode(str, "UTF-8");
     	} catch (UnsupportedEncodingException ex){
     		Logger.getLogger(ScanPluginBuilder.class.getName()).log(Level.SEVERE, null, ex);
     		return "Failed to encode";
     	}    		
     }*/
     
     private boolean submitToCoverity(BuildListener listener){
         URL submitURL;
    		HttpURLConnection connection = null;  
    		
    		String urlParameters = "username="+ScanPluginConfiguration.encodeUTF8(getName());
    		urlParameters += "&password="+ScanPluginConfiguration.encodeUTF8(getPassword());
    		urlParameters += "&project="+ScanPluginConfiguration.encodeUTF8(getProject());
    		urlParameters += "&email="+ScanPluginConfiguration.encodeUTF8(getEmail());
    		urlParameters += "&build_number="+ScanPluginConfiguration.encodeUTF8(getBuildNumber());
    		urlParameters += "&proj_scm="+ScanPluginConfiguration.encodeUTF8(getProjSCM());
    		urlParameters += "&proj_builder="+ScanPluginConfiguration.encodeUTF8(getProjBuilder());
    		urlParameters += "&build_command="+ScanPluginConfiguration.encodeUTF8(getBuildCommand());
    		urlParameters += "&scm_command="+ScanPluginConfiguration.encodeUTF8(getSCMCommand());
    		urlParameters += "&build_comments="+ScanPluginConfiguration.encodeUTF8(getBuildComments());
     	try {
       	    //Create connection
       		submitURL = new URL(ScanPluginConfiguration.SUBMIT_URL);
             connection = (HttpURLConnection)submitURL.openConnection();
             connection.setRequestMethod("POST");
             connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
             connection.setRequestProperty("Content-Language", "en-US");  
       		connection.setUseCaches (false);
       		connection.setDoInput(true);
       		connection.setDoOutput(true);
             listener.getLogger().println("Connecting to Coverity at: " + ScanPluginConfiguration.SUBMIT_URL);
 			listener.getLogger().println("Options sent to Coverity are: "+urlParameters);
 			
       		//Send request
       		DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
      		wr.writeBytes (urlParameters);
       		wr.flush ();
       		wr.close ();
 
       		//Get Response	
       		InputStream is = connection.getInputStream();
       		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
       		String line;
       		StringBuffer response = new StringBuffer(); 
       		while((line = rd.readLine()) != null) {
         	response.append(line);
         	response.append('\r');
       		}
       		rd.close();
       		listener.getLogger().println("Coverity's response was");
       		listener.getLogger().println(response.toString());
     	} catch (Exception e) {
 			listener.getLogger().println("Failed to submit build to Coverity");
       		e.printStackTrace();
       		return false;
     	} finally {
       		if(connection != null) connection.disconnect(); 
     	}
    	 	return true;
     }
 
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
 		List<Cause> buildStepCause = new ArrayList();
 		 buildStepCause.add(new Cause() {
 		   public String getShortDescription() {
 			 return "Build Step started by Coverity Scan plugin Builder";
 		   }
 		 });
         listener.started(buildStepCause);
 
         // this also shows how you can consult the global configuration of the builder
         if(getDescriptor().useFrench())
             listener.getLogger().println("Bonjour, "+name+" " + password + " " + email + " " + project + "!");
         else
             listener.getLogger().println("Hello, "+name+" " + password + " " + email + " " + project + "!");
 
         // now showing the build object
         AbstractProject<?,?> buildProj = build.getProject();
         listener.getLogger().println("This project is called: "+buildProj.getName());
         listener.getLogger().println("The build number is: "+ build.getNumber());
         build_number=Integer.toString(build.getNumber());
         build_comments= project+ " build #"+ build_number;
 
         XmlFile projXml = buildProj.getConfigFile();
         listener.getLogger().println("Project config file location:"+projXml.toString());
         listener.getLogger().println("");
 
         FreeStyleProject realProj = (FreeStyleProject) build.getProject();
     	List projBuilders = realProj.getBuilders();
     	Iterator<Builder> iteratorBuilder = projBuilders.iterator();
     	
     	int i=0;
     	while (iteratorBuilder.hasNext()) {
     		i++;
     		Builder iBuilder = iteratorBuilder.next();
 
     		listener.getLogger().println("builder "+ i + " : " + iBuilder.getClass().toString());
     		if ("org.hudsonci.maven.plugin.builder.MavenBuilder".equals(iBuilder.getClass().getName())) {
     			MavenBuilder mvnBuilder = (MavenBuilder) iBuilder;
     			MavenBuilderDescriptor mvnBuilderDesc=mvnBuilder.getDescriptor();
     			listener.getLogger().println("The Maven command was : mvn " + mvnBuilder.getConfig().getGoals());
     			proj_builder="mvn";
     			build_command=mvnBuilder.getConfig().getGoals();
     			if (build_command.isEmpty()) {
     				build_command= "NO_TARGETS";
     			}
     		} else if ("hudson.tasks.BatchFile".equals(iBuilder.getClass().getName())) {
     			BatchFile batchBuilder = (BatchFile) iBuilder;
     			listener.getLogger().println("The Windows batch command was:" + batchBuilder.getCommand());
     			proj_builder="batch";
     			build_command=batchBuilder.getCommand();
     			if (build_command.isEmpty()) {
     				listener.getLogger().println("Fatal error, the batch command is empty");
     				return false;
     			}
     		} else if ("hudson.tasks.Ant".equals(iBuilder.getClass().getName())) {
     			Ant antBuilder = (Ant) iBuilder;
     			listener.getLogger().println("The Ant command was : ant " + antBuilder.getTargets());
     			proj_builder="ant";
     			build_command=antBuilder.getTargets();
     			if (build_command.isEmpty()) {
     				build_command= "NO_TARGETS";
     			}
     		} else if ("hudson.tasks.Shell".equals(iBuilder.getClass().getName())) {
     			Shell shellBuilder = (Shell) iBuilder;
     			listener.getLogger().println("The shell command was : " + shellBuilder.getCommand());
     			proj_builder="shell";
     			build_command=shellBuilder.getCommand();
     			if (build_command.isEmpty()) {
     				listener.getLogger().println("Fatal error, the shell command is empty");
     				return false;
     			}    			
     		} else if ("com.coverity.scan.hudson.ScanPluginBuilder".equals(iBuilder.getClass().getName())) {
     			// This is us, nothing to do
     		} else {
     			listener.getLogger().println("Unfortunately we do not support the builder " + iBuilder.getClass().getName() +". Please let us know you would like us to!");
     			build_comments+=" unsupported builder: " + iBuilder.getClass().getName();
     		}
     	}
 
         listener.getLogger().println("");
         listener.getLogger().println("Extracting the SCM");
 
         SCM projSCM = buildProj.getScm();
 
         listener.getLogger().println("scm.type="+projSCM.getType());
 		SCMDescriptor<?> scmDesc = projSCM.getDescriptor();
 		//listener.getLogger().println("the SCM descriptor is: " + scmDesc.getClass().getName());
  		if ("hudson.plugins.git.GitSCM".equals(projSCM.getType())) {
 			proj_scm="git";
             GitSCM theSCM;
         
         	theSCM = (GitSCM) projSCM;
            // listener.getLogger().println("the git command was : getGitConfigName " + theSCM.getGitConfigName());
         	//listener.getLogger().println("the git command was : getGitConfigEmail " + theSCM.getGitConfigEmail());
         	//listener.getLogger().println("the git command was : getGitTool " + theSCM.getGitTool());
         	//<? extends RemoteConfig>
             Iterator  repositoryIterator = theSCM.getRepositories().iterator();
          
 			int j=0;
 			while (repositoryIterator.hasNext()) {
 				j++;
 				//listener.getLogger().println("Git Scm  repository " + j);
 				RemoteConfig jConfig = (RemoteConfig) repositoryIterator.next();
 				//listener.getLogger().println("Git Scm  repository  " + j + " content:");
 				//listener.getLogger().println("Git Scm  repository  " + j + " : " + jConfig.getName());
 				Iterator  URIIterator = jConfig.getURIs().iterator();
 				int k=0;
 				while (URIIterator.hasNext()) {
 					k++;
 					//listener.getLogger().println("Git Scm " + j + " URI " +k);
 					URIish kURI = (URIish) URIIterator.next();
 					//listener.getLogger().println("Git Scm  repository  " + j + " URI " +k+ " content:");
 					listener.getLogger().println("Git Scm  repository  " + j + " URI " +k+ " toPrivateString : " + kURI.toPrivateString());
 					scm_command=kURI.toPrivateString();
 					//listener.getLogger().println("Git Scm  repository  " + j + " URI " +k+ " getHost : " + kURI.getHost());
 				}
 			}
         } else if ("hudson.scm.CVSSCM".equals(projSCM.getType())) {
         	proj_scm="cvs";
         	listener.getLogger().println("this SCM is CVS");
         	CVSSCM theSCM;
             theSCM = (CVSSCM) projSCM;
             i=0;
     		for (ModuleLocation moduleLocation : theSCM.getModuleLocations()) {
     		    listener.getLogger().println("loop");
     		    scm_command=" -Q ";
     			//scm_commands[i]=" -Q ";    //Cause CVS to be really quiet.
             	//scm_commands[i]+= " -z3 "; //Causes CVS to use network compression (this is not a local checkout)
             	scm_command+= " -z3 ";
            		if (theSCM.isPreventLineEndingConversion()) {
             		//scm_commands[i]+= " --lf ";  //Causes CVS to not convert unix to windows line ending
             		scm_command+= " --lf ";
             	}
             	//scm_commands[i]+= " -d " + moduleLocation.getCvsroot() +" co -P ";
             	scm_command+= " -d " + moduleLocation.getCvsroot() +" co -P ";
             	if (moduleLocation.getBranch() != null) {
       				//scm_commands[i]+= " -r " + moduleLocation.getBranch();
       				scm_command+=" -r " + moduleLocation.getBranch();
     			}
     			scm_command+=" -d " + project +" "; 
     			//scm_commands[i]+= " -d WORKSPACE ";
     			scm_command+=moduleLocation.getModule();  
     			//scm_commands[i]+= moduleLocation.getModule(); 
     		    listener.getLogger().println("the CVS root is "+moduleLocation.getCvsroot());
     		    //listener.getLogger().println("the CVS branch to build is "+moduleLocation.getBranch());
     		    listener.getLogger().println("the CVS module is "+moduleLocation.getModule());
     		    //listener.getLogger().println("the CVS location is "+Arrays.toString(moduleLocation.getNormalizedModules()));
     		    //listener.getLogger().println("the CVS command["+i+"] should be : cvs " +scm_commands[i]);
     		    listener.getLogger().println("the CVS command["+i+"] should be : cvs " +scm_command);
     		}
         } else if ("hudson.scm.SubversionSCM".equals(projSCM.getType())) {
             proj_scm="svn";
         	listener.getLogger().println("this SCM is SVN");
         	SubversionSCM theSCM;
             theSCM = (SubversionSCM) projSCM;
             scm_command="";
             for (SubversionSCM.ModuleLocation moduleLocation : theSCM.getLocations()) {
     		    listener.getLogger().println("the svn URL is "+moduleLocation.getURL());
     		    listener.getLogger().println("the svn full remote is "+moduleLocation.getOriginRemote());
     		    listener.getLogger().println("the svn depth option is "+moduleLocation.getDepthOption());
     		    scm_command=moduleLocation.getOriginRemote();
     		}     
         } else {
             proj_scm=projSCM.getType();
             scm_command="unknown";
         	listener.getLogger().println("Unfortunately we do not support the builder " + proj_scm +". Please let us know you would like us to!");
         	build_comments+=" unsupported SCM: " + proj_scm;
         }
 			 
  		// Submitting the build to Coverity
 		submitToCoverity(listener);
 
         // Creating link to the report
        ScanPluginReport report = new ScanPluginReport(project,getBuildNumber(), getName(), getPassword());
         build.addAction(report);
 
         listener.finished(Result.SUCCESS);
         return true;
     }
 
     // overrided for better type safety.
     // if your plugin doesn't really define any property on Descriptor,
     // you don't have to do this.
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl)super.getDescriptor();
     }
 
     /**
      * Descriptor for {@link ScanPluginBuilder}. Used as a singleton.
      * The class is marked as public so that it can be accessed from views.
      *
      * <p>
      * See <tt>views/hudson/plugins/scan-plugin/ScanPluginBuilder/*.jelly</tt>
      * for the actual HTML fragment for the configuration screen.
      */
     @Extension // this marker indicates Hudson that this is an implementation of an extension point.
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
         /**
          * To persist global configuration information,
          * simply store it in a field and call save().
          *
          * <p>
          * If you don't want fields to be persisted, use <tt>transient</tt>.
          */
         private boolean useFrench;
 
         /**
          * Performs on-the-fly validation of the form field 'name'.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
             if(value.length()==0)
                 return FormValidation.error("Please set a name");
             if(value.length()<4)
                 return FormValidation.warning("Isn't the name too short?");
             return FormValidation.ok();
         }
 
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             // indicates that this builder can be used with all kinds of project types
             return true;
         }
 
         /**
          * This human readable name is used in the configuration screen.
          */
         public String getDisplayName() {
             return "Coverity Scan";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             // To persist global configuration information,
             // set that to properties and call save().
             useFrench = formData.getBoolean("useFrench");
             // ^Can also use req.bindJSON(this, formData);
             //  (easier when there are many fields; need set* methods for this, like setUseFrench)
             save();
             return super.configure(req,formData);
         }
 
         /**
          * This method returns true if the global configuration says we should speak French.
          */
         public boolean useFrench() {
             return useFrench;
         }
     }
 }
 
