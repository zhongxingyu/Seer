 package jfullam.vfabric.jenkins.plugin;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildWrapper;
 import hudson.tasks.BuildWrapperDescriptor;
 import hudson.util.FormValidation;
 import hudson.util.ListBoxModel;
 
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import jfullam.vfabric.jenkins.plugin.rest.ServiceManager;
 import jfullam.vfabric.jenkins.plugin.rest.ServiceResult;
 import jfullam.vfabric.jenkins.plugin.rest.ServiceException;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * BuildWrapper plugin that provides the ability to take post build actions using
  * vFabric Application Director.
  * 
  * Specific actions include the re deployment of a blueprint in Application Director
  * or the updating of an existing deployment.  
  * 
  * @author Jonathan Fullam
  */
 public class ApplicationDirectorPostBuildDeployer extends BuildWrapper {
 	
 	private static final Log log = LogFactory.getLog(ApplicationDirectorPostBuildDeployer.class);
 
 	private static final String PROVISION = "provision"; 
 	
 	private String application;
 	private String deploymentProfile;
 	private String deployment;
 	private String component;
 	private String updateProperty;
 	private String updatePropertyValue;
 	private String provisionOrUpdate;
 
 	@DataBoundConstructor
 	public ApplicationDirectorPostBuildDeployer(String provisionOrUpdate, String application,
 			String deploymentProfile, String deployment, String component, String updateProperty,
 			String updatePropertyValue) {
 		
 		this.application = application;
 		this.deploymentProfile = deploymentProfile;
 		this.deployment = deployment;
 		this.component = component;
 		this.updateProperty = updateProperty;
 		this.updatePropertyValue = updatePropertyValue;
 		this.provisionOrUpdate = provisionOrUpdate;
 	}
 
 	@Override
 	public Environment setUp(AbstractBuild build, Launcher launcher,
 			BuildListener listener) throws IOException, InterruptedException {
 
 		Environment applicationDirectorEnv = new Environment() {
 			public boolean tearDown(final AbstractBuild build,
 					final BuildListener listener) throws IOException,
 					InterruptedException {
 
 			
 				listener.getLogger().println("Application Director base REST uri:  " + getDescriptor().getAppDirBaseURI());
 				listener.getLogger().println("Applicatin Director REST API user:  " + getDescriptor().getUserName());
 
 				try {
 					//Teardown an existing deployment (if it exists) and re-deploy based on user selected data
 					if (provisionOrUpdate.equals(PROVISION)) {
 						listener.getLogger().println("Calling Applicaiton Director to re-deploy an application blueprint.");
 						listener.getLogger().println("Application:  " + application);
 						listener.getLogger().println("Deployment Profile:  " + deploymentProfile);
 						
 						//Need to parse the deployment profile and application into an ID and name due to the need
 						//for the profile and application name when finding a specific deployment.
 						//These fields are formatted as name,id
 						StringTokenizer profileParser = new StringTokenizer(deploymentProfile, ",");
 						StringTokenizer applicationParser = new StringTokenizer(application, ",");
 						
 						listener.getLogger().println("Looking for an existing deployment to teardown.");
 						ServiceResult teardownResult = 
 							ServiceManager.provisioningService().tearDown(applicationParser.nextToken(), profileParser.nextToken());
 						
 						handleResult(teardownResult, listener, build);
 						
 						if (!teardownResult.isSuccess()) {
 							return false;
 						}
 						
 						listener.getLogger().println("Deploying based on the configured deployment profile.");
 						ServiceResult deployResult =
 							ServiceManager.provisioningService().scheduleDeployment(profileParser.nextToken());
 						
 						handleResult(deployResult, listener, build);
 						
 						if (!deployResult.isSuccess()) {
 							return false;
 						}
 					
 					//Update and existing deployment based on user selected data
 					} else {
 						listener.getLogger().println("Calling Application Director to update an existing deployment.");
 						listener.getLogger().println("Existing deployment:  " + deployment);
 						listener.getLogger().println("component:  " + component);
 						listener.getLogger().println("property to update:  " + updateProperty + " value:  " + updatePropertyValue);
 						
 						ServiceResult updateResult =
 							ServiceManager.updateService().updateDeployment(deployment,component,updateProperty,updatePropertyValue);
 						
 						handleResult(updateResult, listener, build);
 						
 						if(!updateResult.isSuccess()) {
 							return false;
 						}
 						
 					}
 				} catch (ServiceException e) {
 					listener.getLogger().println("Error while integrating with Appplication Director");
 					listener.getLogger().println(e.getMessage());
 					build.setResult(Result.FAILURE);
 					return false;
 				}
 
 				return true;
 			}
 		};
 
 		return applicationDirectorEnv;
 	}
 		
 	private void handleResult(ServiceResult result,
 			BuildListener listener, AbstractBuild build) {
 		
 		if (result.isSuccess()) {
 			for (String msg : result.getMessages()) {
 				listener.getLogger().println(msg);
 			}
 		} else {
 			build.setResult(Result.FAILURE);
 			for (String msg : result.getMessages()) {
 				listener.error(msg);
 			}
 		}
 	}
 		
 	
 	public String getApplication() {
 		return application;
 	}
 
 
 
 	public void setApplication(String application) {
 		this.application = application;
 	}
 
 
 
 	public String getProvisionOrUpdate() {
 		return provisionOrUpdate;
 	}
 
 
 
 	public void setProvisionOrUpdate(String provisionOrUpdate) {
 		this.provisionOrUpdate = provisionOrUpdate;
 	}
 
 	public String getDeploymentProfile() {
 		return deploymentProfile;
 	}
 
 	public void setDeploymentProfile(String deploymentProfile) {
 		this.deploymentProfile = deploymentProfile;
 	}
 
 	public String getDeployment() {
 		return deployment;
 	}
 
 	public void setDeployment(String deployment) {
 		this.deployment = deployment;
 	}
 
 	public String getUpdateProperty() {
 		return updateProperty;
 	}
 
 	public void setUpdateProperty(String updateProperty) {
 		this.updateProperty = updateProperty;
 	}
 
 	public String getComponent() {
 		return component;
 	}
 
 	public void setComponent(String component) {
 		this.component = component;
 	}
 
 	public String getUpdatePropertyValue() {
 		return updatePropertyValue;
 	}
 
 	public void setUpdatePropertyValue(String updatePropertyValue) {
 		this.updatePropertyValue = updatePropertyValue;
 	}
 
 	@Override
 	public Descriptor getDescriptor() {
 		return (Descriptor) super.getDescriptor();
 	}
 
 	/**
 	 * BuildWrapper plugin descriptor.  
 	 * 
 	 * @author Jonathan Fullam
 	 */
 	@Extension
 	public static class Descriptor extends BuildWrapperDescriptor {
 
 		private String appDirBaseURI;
 		private String userName;
 		private String password;
 
 		@Override
 		public boolean isApplicable(AbstractProject<?, ?> item) {
 			return true;
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "Application Director Client";
 		}
 		
 		/**
 		 * Called when the connection validation button is clicked on the global connection screen.
 		 * 
 		 * @param appDirBaseURI
 		 * @param userName
 		 * @param password
 		 * @return FormValidation - indicates if the connection data on the global page is valid.
 		 */
 		public FormValidation doValidateConnection(@QueryParameter("appDirBaseURI") String appDirBaseURI, 
 						@QueryParameter("userName") String userName, @QueryParameter("password") String password) {
 			try {
 				ServiceManager.configureApplicationDirectorClient(appDirBaseURI,userName, password);
 			
 				if (ServiceManager.connectionValidationService().connectionValid()) {
 					return FormValidation.ok("Validated Successfully!");
 				} else {
 					return FormValidation.error("Validation Failed!");
 				}
 			} catch (ServiceException e) {
 				return FormValidation.error("Error During Validation");
 			}
 			
 		}
 		
 		/**
 		 * @return ListBoxModel - A list of the current deployments found in
 		 *  Applicaiton Director
 		 */
 		public ListBoxModel doFillDeploymentItems() {
 			try {
 				return ServiceManager.userInterfaceService().getDeployments();
 			} catch (ServiceException e) {
 				log.error("Problem calling REST service for deployments select box.", e);
 			}
 			return new ListBoxModel();
 		}
 		
 		/**
 		 * Get the list of components for a specific deployment.
 		 * 
 		 * @param deployment - a deployment id 
 		 * @return ListBoxModel - list of components
 		 */
 		public ListBoxModel doFillComponentItems(@QueryParameter String deployment) {
 			try {
 				return ServiceManager.userInterfaceService().getComponents(deployment);
 			} catch (ServiceException e) {
 				log.error("Problem calling REST service for components select box.", e);
 			}
 			return new ListBoxModel();
 		}
 		
 		/**
 		 * Gets a list of update properties for a specific component.
 		 * 
 		 * @param component
 		 * @return ListBoxModel - the list of update properties for the component
 		 */
 		public ListBoxModel doFillUpdatePropertyItems(@QueryParameter String component) {
 			return ServiceManager.userInterfaceService().getUpdateProperties(component);
 		}
 
 		/**
 		 * Gets a list of Application that are currently in Application Director
 		 * 
 		 * @return ListBoxModel - List of Applications 
 		 */
 		public ListBoxModel doFillApplicationItems() {
 			try {
 				return ServiceManager.userInterfaceService().getApplications();
 			} catch (ServiceException e) {
 				log.error("Problem calling REST service for applications select box.", e);
 			}
 			return new ListBoxModel();
 		}
 		
 		/**
 		 * Gets a list of deployment profile for a given application currently configured
 		 * in Application Director
 		 * 
 		 * @param application - application name / id combination (ie SpringTravel,325)
 		 * @return ListBoxModel - list of deployment profile
 		 */
 		public ListBoxModel doFillDeploymentProfileItems(@QueryParameter String application) {
 			
 			if (StringUtils.isNotBlank(application)) {
 				//Get the id by parsing past the name (application is formatted as name,id)
 				StringTokenizer applicationParser = new StringTokenizer(application, ",");
 				applicationParser.nextToken();
 				try {
 					return ServiceManager.userInterfaceService().getDeploymentProfiles(applicationParser.nextToken());
 				} catch (ServiceException e) {
 					log.error("Problem calling REST service for deployment profile select box.", e);
 				}
 			}
 			return new ListBoxModel();
 			
 		}
 		
 		/*
 		 * Called when the global configuration screen is saved 
 		 */
 		@Override
 		public boolean configure(StaplerRequest req, JSONObject formData)
 				throws FormException {
 
 			appDirBaseURI = formData.getString("appDirBaseURI");
 			userName = formData.getString("userName");
 			password = formData.getString("password");
 			try {
 				ServiceManager
 						.configureApplicationDirectorClient(appDirBaseURI,
 								userName, password);
 			} catch (ServiceException e) {
 				throw new FormException(e, "Problem configuring the REST service.");
 			}
 
 			save();
 			return super.configure(req, formData);
 		}
 
 		public String getAppDirBaseURI() {
 			return appDirBaseURI;
 		}
 
 		public String getUserName() {
 			return userName;
 		}
 
 		public String getPassword() {
 			return password;
 		}
 
 	}
 
 }
