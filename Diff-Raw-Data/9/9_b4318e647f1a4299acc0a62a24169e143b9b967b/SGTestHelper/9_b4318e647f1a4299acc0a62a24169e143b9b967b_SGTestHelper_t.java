 package org.cloudifysource.quality.iTests.framework.tools;
 
 import java.io.File;
 
 import org.apache.commons.lang.StringUtils;
 
import org.cloudifysource.quality.iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.framework.utils.ScriptUtils;
 
 public class SGTestHelper {
 
 	/**
 	 * This is a very cool method which returns jar or directory from where the supplied class has loaded.
 	 * 
 	 * @param claz the class to get location.
 	 * @return jar/path location of supplied class.
 	 */
 	public static String getClassLocation(Class claz)
 	{
 		return claz.getProtectionDomain().getCodeSource().getLocation().toString().substring(5);
 	}
 
 	public static boolean isDevMode() {
 		boolean isDevMode;
 		if (System.getenv().containsKey("DEV_ENV")) {
             return Boolean.valueOf(System.getenv("DEV_ENV"));
 		} else if (System.getProperties().containsKey("DEV_ENV")) {
 			return Boolean.getBoolean("DEV_ENV");
 		} else if (System.getProperties().containsKey("iTests.cloud.enabled")) {
            if   (Boolean.getBoolean("iTests.cloud.enabled")){
                return false;
            }
         }
 
        LogUtils.log(System.getProperties().toString());
 		if (ScriptUtils.isWindows()) {
 			isDevMode = !System.getenv("USERNAME").equals("ca");
 		}
 		else {
 			isDevMode = !System.getProperty("user.name").equals("tgrid");
 		}
 		return isDevMode;
 	}
 
 	public static String getSGTestSrcDir() {
 		String sgtestSrcDir;
 		if (isDevMode()) {
 			sgtestSrcDir = getSGTestRootDir() + "/src";
 		}
 		else {
 			sgtestSrcDir = getSGTestRootDir() + "/tmp";
 		}
 		return sgtestSrcDir;
 
 	}
 	
 	
 	public static String getSuiteName(){
 		return System.getProperty("iTests.suiteName", "");
 	}
 	
 	public static String getSuiteId() {
 		return System.getProperty("iTests.suiteId", "");
 	}
 	
 	//each suite has it's own work dir.
 	public static String getWorkDirName() {
 		String suiteDir = getSuiteName();
 		String suiteId = getSuiteId();
 		Integer numberOfSuite = Integer.getInteger("iTests.numOfSuites",1);
 
 		if (StringUtils.isEmpty(suiteDir) || StringUtils.isEmpty(suiteId) || numberOfSuite == 1) {
 			return "work";
 		} else {
 			return suiteDir + suiteId + "_work";
 		}
 	}
 
 	/** @return SGTest root directory */
 	public static String getSGTestRootDir(){
 		return new File(".").getAbsolutePath();
 	}
 
 	public static String getBuildDir() {
 		return ScriptUtils.getBuildPath();
 	}
 
 	public static String getCustomCloudConfigDir(String cloudName) {
 		return getSGTestRootDir() + "/src/main/resources/custom-cloud-configs/" + cloudName;
 	}
 	
 }
