 package org.bitrepository.common;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.bitrepository.common.configuration.CommonConfiguration;
 import org.bitrepository.common.exception.ConfigurationException;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Tests the <code>ConfigurationFactory</code> by running the functionality on the common modules own configuration
  */
 public class ConfigurationFactoryTest extends ExtendedTestCase {
 	private static final ModuleCharacteristics moduleCharacteristics = new ModuleCharacteristics("common");
 	private boolean didConfigurationDirExistBefore;
 
 	private static final File originalConfigLocation = 
 		new File(ConfigurationFactory.TEST_CONFIGURATION_PATH + "/common-configuration.xml");
 	private static final File testConfigLocation = 
 		new File("target/test-classes/common-configuration.xml");
 	private static final File defaultConfigurationDir = new File("configuration");
 	private static final File fileInDefaultConfigurationDir = 
 		new File(defaultConfigurationDir, "common-configuration.xml");
 
	@BeforeMethod (alwaysRun=true)
 	public void setUp() {
 		//The configuration file out of the way, so it isn't loaded by default.
 		testConfigLocation.delete();
 		
 		originalConfigLocation.renameTo(testConfigLocation);
 		
 		if (defaultConfigurationDir.exists()) {
 			didConfigurationDirExistBefore = true;
 			if (fileInDefaultConfigurationDir.exists()) fileInDefaultConfigurationDir.delete();
 		}
 		else didConfigurationDirExistBefore = false;
 	}
 
 	/**
 	 * Attempts to cleanup the files from the test cases. May not work and in this case we may leave some configuration
 	 * files laying around in inappropriate places. 
 	 */
	@AfterMethod  (alwaysRun=true)
 	public void tearDown() {
 		testConfigLocation.renameTo(originalConfigLocation);
 		
 		// We should only clean the defaultConfigurationDir if we created it in the first place
 		if (didConfigurationDirExistBefore && defaultConfigurationDir.exists()) {
 			try {
 				FileUtils.deleteDirectory(defaultConfigurationDir);
 			} catch (IOException e) {
 				System.out.println("Failed to remove defaultConfigurationDir");
 			}
 		}
 	}
 
 	@Test(groups = { "regressiontest" })
 	public void loadConfigurationByModuleFilePropertyTest() {
 		addDescription("Validates that configurations can be loaded by using the 'common.configuration.file' " +
 		"system property");
 		ModuleCharacteristics moduleCharacteristics = new ModuleCharacteristics("common");
 		addStep("Attempt to load the configuration without setting the 'common.configuration.file' property",
 		"The attempt should fail with a ConfigurationException");
 		System.clearProperty("common.configuration.file");
 		try {
 			ConfigurationFactory.loadConfiguration(moduleCharacteristics, CommonConfiguration.class);
 			Assert.fail("Expected a ConfigurationException with a undefined 'common.configuration.file' property");
 		} catch (ConfigurationException ce) {
 			// That apparently worked
 		}
 
 		addStep("Set the 'common.configuration.file' so it points to a valid configuration file, " +
 				"and attempt to load the configuration",
 		"A Configuration object should be returned");		
 		System.setProperty("common.configuration.file", testConfigLocation.getAbsolutePath());
 		Assert.assertNotNull(
 				ConfigurationFactory.loadConfiguration(moduleCharacteristics, CommonConfiguration.class));
 
 		addStep("Set the 'common.configuration.file' so it points to a non-existing configuration file, " +
 				"and attempt to load the configuration",
 		"The attempt should fail with a ConfigurationException");
 		try {			
 			System.setProperty("common.configuration.file", originalConfigLocation.getAbsolutePath());
 			ConfigurationFactory.loadConfiguration(moduleCharacteristics, CommonConfiguration.class);
 			Assert.fail("Expected a ConfigurationException on a setting a invalid 'common.configuration.file' property");
 		} catch (ConfigurationException ce) {
 			// That apparently worked
 		}
 	}
 
 	@Test(groups = { "regressiontest" })
 	public void loadConfigurationByConfigurationDirPropertyTest() {
 		addDescription("Validates that configurations can be loaded by using the 'configuration.dir' " +
 		"system property");
 
 		addStep("Attempt to load the configuration without setting the 'configuration.dir' property",
 		"The attempt should fail with a ConfigurationException");
 
 		System.clearProperty("configuration.dir");
 		try {
 			ConfigurationFactory.loadConfiguration(moduleCharacteristics, CommonConfiguration.class);
 			Assert.fail("Expected a ConfigurationException with a undefined 'configuration.dir' property");
 		} catch (ConfigurationException ce) {
 			// That apparently worked
 		}
 
 		addStep("Set the 'configuration.dir' so it points to a valid configuration dir, " +
 				"and attempt to load the configuration",
 		"A Configuration object should be returned");		
 		System.setProperty("configuration.dir", testConfigLocation.getParent());
 		Assert.assertNotNull(
 				ConfigurationFactory.loadConfiguration(moduleCharacteristics, CommonConfiguration.class));
 
 		addStep("Set the 'configuration.dir' so it points to a configuration dir with out the relevant " +
 				"configuration file, and attempt to load the configuration",
 		"The attempt should fail with a ConfigurationException");
 		try {			
 			System.setProperty("configuration.dir", "./");
 			ConfigurationFactory.loadConfiguration(moduleCharacteristics, CommonConfiguration.class);
 			Assert.fail("Expected a ConfigurationException on a setting a invalid 'configuration.dir' property");
 		} catch (ConfigurationException ce) {
 			// That apparently worked
 		}
 	}
 
 	@Test(groups = { "regressiontest" })
 	public void loadConfigurationByConfigurationDirTest() throws IOException {
 		addDescription("Validates that configurations can be loaded by placing it in the configuration dir");
 
 		addStep("Attempt to load the configuration from a configuration dir without the relevant configuration file",
 		"The attempt should fail with a ConfigurationException");
 		
 		if (!defaultConfigurationDir.exists()) defaultConfigurationDir.mkdir();
 		File configurationFile = new File(defaultConfigurationDir, "common-configuration.xml");
 		if (configurationFile.exists()) configurationFile.delete();
 		
 		try {
 			ConfigurationFactory.loadConfiguration(moduleCharacteristics, CommonConfiguration.class);
 			Assert.fail("Expected a ConfigurationException with a defaultConfigurationDir without a property file");
 		} catch (ConfigurationException ce) {
 			// That apparently worked
 		}
 
 		addStep("Copy a working configuration file to the configuration dir",
 		"A Configuration object should be returned");		
 		FileUtils.copyFile(testConfigLocation, configurationFile);
 		Assert.assertNotNull(
 				ConfigurationFactory.loadConfiguration(moduleCharacteristics, CommonConfiguration.class));
 	}
 
 	@Test(groups = { "regressiontest" })
 	public void loadConfigurationBySrcConfigurationDirTest() throws IOException {
 		addDescription("Validates that configurations can be loaded by placing it in the test configuration dir");
 
 		addStep("Attempt to load the configuration from a test configuration dir without the relevant configuration " +
 				"file",
 				"The attempt should fail with a ConfigurationException");
 		
 		try {
 			ConfigurationFactory.loadConfiguration(moduleCharacteristics, CommonConfiguration.class);
 			Assert.fail("Expected a ConfigurationException with a test configuration dir without a property file");
 		} catch (ConfigurationException ce) {
 			// That apparently worked
 		}
 
 		addStep("Copy a working configuration file to the test configuration dir",
 		"A Configuration object should be returned");		
 		testConfigLocation.renameTo(originalConfigLocation);
 		Assert.assertNotNull(
 				ConfigurationFactory.loadConfiguration(moduleCharacteristics, CommonConfiguration.class));
 	}
 }
