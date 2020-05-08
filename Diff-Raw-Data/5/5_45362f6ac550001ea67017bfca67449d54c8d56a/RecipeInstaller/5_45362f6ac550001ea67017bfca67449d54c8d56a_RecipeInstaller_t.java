 package framework.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Properties;
 
 import test.cli.cloudify.CommandTestUtils;
 
 public abstract class RecipeInstaller {
 	
 	private String restUrl;
 	private String recipePath;
 	private Map<String, Object> overrideProperties;
 	private Map<String, Object> cloudOverrideProperties;
 	private long timeoutInMinutes;
 	private boolean waitForFinish = true;
 	private boolean expectToFail = false;
 
 	public RecipeInstaller(final String restUrl, int timeout) {
 		this.restUrl = restUrl;
 		this.timeoutInMinutes = timeout;
 	}
 	
 	public String getRestUrl() {
 		return restUrl;
 	}
 
 	public void setRestUrl(String restUrl) {
 		this.restUrl = restUrl;
 	}
 
 	public String getRecipePath() {
 		return recipePath;
 	}
 
 	public void setRecipePath(String recipePath) {
 		this.recipePath = recipePath;
 	}
 
 	public Map<String, Object> getOverrides() {
 		return overrideProperties;
 	}
 
 	public void setOverrides(Map<String, Object> overridesFilePath) {
 		this.overrideProperties = overridesFilePath;
 	}
 
 	public Map<String, Object> getCloudOverrides() {
 		return cloudOverrideProperties;
 	}
 
 	public void setCloudOverrides(Map<String, Object> cloudOverridesFilePath) {
 		this.cloudOverrideProperties = cloudOverridesFilePath;
 	}
 
 	public long getTimeoutInMinutes() {
 		return timeoutInMinutes;
 	}
 
 	public void setTimeoutInMinutes(long timeoutInMinutes) {
 		this.timeoutInMinutes = timeoutInMinutes;
 	}
 
 	public boolean isWaitForFinish() {
 		return waitForFinish;
 	}
 
 	public void setWaitForFinish(boolean waitForFinish) {
 		this.waitForFinish = waitForFinish;
 	}
 
 	public boolean isExpectToFail() {
 		return expectToFail;
 	}
 
 	public void setExpectToFail(boolean expectToFail) {
 		this.expectToFail = expectToFail;
 	}
 	
 	public abstract String getInstallCommand();
 	
 	public abstract String getUninstallCommand();
 	
 	public abstract String getRecipeName();
 	
 	public abstract void assertInstall(String output);
 	
 	public abstract void assertUninstall(String output);
 	
 	public void install() throws IOException, InterruptedException {
 		
 		if (recipePath == null) {
 			throw new IllegalStateException("recipe path cannot be null. please use setRecipePath before calling install");
 		}
 		
 		final String connectCommand = "connect " + restUrl + ";";
 		StringBuilder commandBuilder = new StringBuilder()
 				.append(getInstallCommand()).append(" ")
 				.append("--verbose").append(" ")
 				.append("-timeout").append(" ")
 				.append(timeoutInMinutes).append(" ");
 		
 		if (cloudOverrideProperties != null && !cloudOverrideProperties.isEmpty()) {	
 			File cloudOverridesFile = createTempOverridesFile(cloudOverrideProperties);
			commandBuilder.append("-cloud-overrides ").append(cloudOverridesFile.getAbsolutePath().replace("\\", "/")).append(" ");
 		}
 		if (overrideProperties != null && !overrideProperties.isEmpty()) {
 			File serviceOverridesFile = createTempOverridesFile(overrideProperties);
			commandBuilder.append("-overrides ").append(serviceOverridesFile.getAbsolutePath().replace("\\", "/")).append(" ");
 		}
 		
 		commandBuilder.append(recipePath.replace('\\', '/'));
 		final String installCommand = commandBuilder.toString();
 		if (expectToFail) {
 			CommandTestUtils.runCommandExpectedFail(connectCommand + installCommand);
 			return;
 		}
 		if (waitForFinish) {
 			CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 			return;			
 		}
 		String output = CommandTestUtils.runCommand(connectCommand + installCommand);
 		assertInstall(output);
 	}
 	
 	public void uninstall() throws IOException, InterruptedException {
 		String url = null;
 		try {
 			url = restUrl + "/service/dump/machines/?fileSizeLimit=50000000";
 			DumpUtils.dumpMachines(restUrl);
 		} catch (final Exception e) {
 			LogUtils.log("Failed to create dump for this url - " + url, e);
 		}
 
 		final String connectCommand = "connect " + restUrl + ";";
 		final String installCommand = new StringBuilder()
 				.append(getUninstallCommand()).append(" ")
 				.append("--verbose").append(" ")
 				.append("-timeout").append(" ")
 				.append(timeoutInMinutes).append(" ")
 				.append(getRecipeName())
 				.toString();
 		String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 		assertUninstall(output);
 	}
 	
 	private File createTempOverridesFile(Map<String, Object> overrides) throws IOException {
 		
 		File createTempFile = File.createTempFile("__sgtest_cloudify", ".overrides");	
 		
 		Properties props = new Properties();
 		for (Map.Entry<String, Object> entry : overrides.entrySet()) {
 			Object value = entry.getValue();
 			String key = entry.getKey();
 			String actualValue = null;
 			if (value instanceof String) {
 				actualValue = '"' + value.toString() + '"';
 			} else {
 				actualValue = value.toString();
 			}
 			props.setProperty(key, actualValue);
 		}
 		
 		File overridePropsFile = IOUtils.writePropertiesToFile(props, createTempFile);
 		return overridePropsFile;
 
 	}
 }
