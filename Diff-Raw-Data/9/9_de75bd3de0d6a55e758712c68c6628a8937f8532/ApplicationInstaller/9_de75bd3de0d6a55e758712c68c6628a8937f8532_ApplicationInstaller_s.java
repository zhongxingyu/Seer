 package framework.utils;
 
 import org.apache.commons.lang.StringUtils;
 
 import junit.framework.Assert;
 import test.cli.cloudify.CommandTestUtils;
 
 public class ApplicationInstaller extends RecipeInstaller {
 
 	private static final int DEFAULT_INSTALL_APPLICATION_TIMEOUT = 30;
 	
 	private String applicationName; 
 	
 	public ApplicationInstaller(String restUrl, String applicationName) {
 		super(restUrl, DEFAULT_INSTALL_APPLICATION_TIMEOUT);
 		this.applicationName = applicationName;
 	}
 
 	public ApplicationInstaller setApplicationName(String applicationName) {
 		this.applicationName = applicationName;
 		return this;
 	}
 	
 	public void uninstallIfFound() {	
 		if (getRestUrl() != null) {
 			String command = connectCommand() + ";list-applications";
 			String output;
 			try {
 				output = CommandTestUtils.runCommandAndWait(command);
				if (output.contains(applicationName)) {
 					uninstall();
 				}
 			} catch (final Exception e) {
 				LogUtils.log(e.getMessage(), e);
 				Assert.fail(e.getMessage());
 			}
 		}
 	}
 	
 	protected String connectCommand(){
 
 		String user = getCloudifyUsername();
 		String password = getCloudifyPassword();
 		
 		if(StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)){
 			return("connect -user " + user + " -password " + password + " " + getRestUrl());
 		}
 
 		return("connect " + getRestUrl());
 	}
 	
 	public String getApplicationName() {
 		return applicationName;
 	}
 }
