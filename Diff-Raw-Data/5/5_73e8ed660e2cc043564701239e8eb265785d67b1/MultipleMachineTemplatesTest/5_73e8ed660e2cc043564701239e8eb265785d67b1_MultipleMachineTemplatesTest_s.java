 package test.cli.cloudify.cloud;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.dsl.cloud.Cloud;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.openspaces.admin.AdminFactory;
 import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.IOUtils;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 
 /**
  * This test installs petclinic with 3 different templates on a byon cloud.
  * It checks that each service was assigned to the correct template, according to byon-cloud.groovy.
  * After the installation completes, the test checks the uninstall and teardown operations.
  * 
  * Note: this test uses 5 fixed machines - 192.168.9.115,192.168.9.116,192.168.9.120,192.168.9.125,192.168.9.126.
  */
 public class MultipleMachineTemplatesTest extends AbstractCloudTest{
 
 	private ByonCloudService service;
 	private String cloudName = "byon";
 	private static final String LOOKUPGROUP = "byon_group";
 	private File byonUploadDir = new File(ScriptUtils.getBuildPath() , "tools/cli/plugins/esc/byon/upload");
 	private File backupStartManagementFile = new File(byonUploadDir, "bootstrap-management.sh.backup");
 	private File originialBootstrapManagement = new File(byonUploadDir, "bootstrap-management.sh");
 	private File byonCloudConfDir = new File(ScriptUtils.getBuildPath() , "tools/cli/plugins/esc/byon");
 	private File backupCloudConfFile = new File(byonCloudConfDir, "byon-cloud.groovy.backup");
 	private File originialCloudConf = new File(byonCloudConfDir, "byon-cloud.groovy");
 	private File mongodbDir = new File(ScriptUtils.getBuildPath() , "recipes/services/mongodb");
 	private File tomcatDir = new File(ScriptUtils.getBuildPath() , "recipes/services");
 	private Cloud readCloud;
 	private static final String TEMPLATE_1_IPs = "192.168.9.115,192.168.9.116";
 	private static final String TEMPLATE_2_IPs = "192.168.9.120,192.168.9.125";
 	private static final String TEMPLATE_3_IPs = "192.168.9.126";
 	private static final String DEFAULT_MACHINE_TEMPLATE = "SMALL_LINUX";
 	private static final int MONGOD_DEFAULT_INSTANCES_NUM = 2;
 	private static final int MONGOD_INSTANCES_NUM = 1;
 	public final static long MY_OPERATION_TIMEOUT = 1 * 60 * 1000;
 	
 	@BeforeClass(enabled = true)
 	public void before() throws IOException, InterruptedException, DSLException {
 		
 		// get the default service
 		service = (ByonCloudService) getDefaultService(cloudName);
 		
 		if ((service != null) && service.isBootstrapped()) {
 			service.teardownCloud(); // tear down the existing byon cloud since we need a new bootstrap			
 		}
 		
 		service = new ByonCloudService();
 		service.setMachinePrefix(this.getClass().getName());
 
		backupAndReplaceOriginalFile(originialBootstrapManagement,SGTestHelper.getSGTestRootDir() + "\\apps\\cloudify\\cloud\\byon\\bootstrap-management-multicast-and-byon-java-home.sh");
 		Map<String,String> replaceMap = new HashMap<String,String>();
 		
 		readCloud = ServiceReader.readCloud(originialCloudConf);
 		String cloudifyUrl = readCloud.getProvider().getCloudifyUrl();
		File newCloudConfFile = backupAndReplaceOriginalFile(originialCloudConf,SGTestHelper.getSGTestRootDir() + "\\apps\\cloudify\\cloud\\byon\\byon-cloud.groovy");
 
 		replaceMap.put("cloudify.zip", cloudifyUrl);
 		replaceMap.put("1.1.1.1", TEMPLATE_1_IPs);
 		replaceMap.put("2.2.2.2", TEMPLATE_2_IPs);
 		replaceMap.put("3.3.3.3", TEMPLATE_3_IPs);
 
 		IOUtils.replaceTextInFile(newCloudConfFile.getAbsolutePath(), replaceMap);
 
 		backupAndReplaceMachineTemplateInService("mongos", "TEMPLATE_1");
 		backupAndReplaceMachineTemplateInService("mongod", "TEMPLATE_2");
 		backupAndReplaceMachineTemplateInService("mongoConfig", "TEMPLATE_1");
 		backupAndReplaceMachineTemplateInService("tomcat", "TEMPLATE_2");
 		IOUtils.replaceTextInFile(mongodbDir.getAbsolutePath() + "/mongod/mongod-service.groovy", "numInstances " + MONGOD_DEFAULT_INSTANCES_NUM, "numInstances " + MONGOD_INSTANCES_NUM);
 		
 		service.bootstrapCloud();
 
 		if (service.getRestUrls() == null) {
 			Assert.fail("Test failed becuase the cloud was not bootstrapped properly");
 		}
 
 		setService(service);
 
 		LogUtils.log("creating admin");
 		AdminFactory factory = new AdminFactory();
 		factory.addGroup(LOOKUPGROUP);
 		admin = factory.createAdmin();
 
 	}
 
 	private File backupAndReplaceOriginalFile(File originalFile, String replacementFilePath) throws IOException {
 		// replace the default originalFile with a different one
 		// first make a backup of the original file
 		File backupFile = new File(originalFile.getAbsolutePath() + ".backup");
 		FileUtils.copyFile(originalFile, backupFile);
 
 		// copy replacement file to upload dir as the original file's name
 		File replacementFile = new File(replacementFilePath);
 
 		FileUtils.deleteQuietly(originalFile);
 		File newOriginalFile = new File(originalFile.getParent(), originalFile.getName());
 		FileUtils.copyFile(replacementFile, newOriginalFile);
 		
 		return newOriginalFile;
 	}
 
 	private void backupAndReplaceMachineTemplateInService(String serviceName, String newTemplate) throws IOException {
 		
 		File parentDir = mongodbDir;
 		if(serviceName.equalsIgnoreCase("tomcat"))
 			parentDir = tomcatDir;
 		
 		File originalService = new File(parentDir, serviceName + "/" + serviceName + "-service.groovy");
 		FileUtils.copyFile(originalService, new File(originalService.getAbsolutePath() + ".backup"));
 		IOUtils.replaceTextInFile(originalService.getAbsolutePath(), DEFAULT_MACHINE_TEMPLATE , newTemplate);
 	}
 	
 	/**
 	 * check that each service was assigned to the correct template, according to byon-cloud.groovy.
 	 * @throws Exception
 	 */
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true, priority = 1)
 	public void testPetclinic() throws Exception {
 		LogUtils.log("installing application petclinic on " + cloudName);
 		installApplicationAndWait(ScriptUtils.getBuildPath() + "/recipes/apps/petclinic", "petclinic");
 
 		String template1IPsArray[] = TEMPLATE_1_IPs.split(",");
 		String template2IPsArray[] = TEMPLATE_2_IPs.split(",");
 		String template3IPsArray[] = TEMPLATE_3_IPs.split(",");
 		
 		String hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("petclinic.mongod")
 				.getInstances()[0].getMachine().getHostAddress();
 		Assert.assertTrue(Arrays.asList(template2IPsArray).contains(hostAddressToCompare));
 		
 		hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("petclinic.mongos")
 				.getInstances()[0].getMachine().getHostAddress();
 		Assert.assertTrue(Arrays.asList(template1IPsArray).contains(hostAddressToCompare));
 		
 		hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("petclinic.mongoConfig")
 				.getInstances()[0].getMachine().getHostAddress();
 		Assert.assertTrue(Arrays.asList(template1IPsArray).contains(hostAddressToCompare));
 		
 		hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("petclinic.tomcat")
 				.getInstances()[0].getMachine().getHostAddress();
 		Assert.assertTrue(Arrays.asList(template2IPsArray).contains(hostAddressToCompare));	
 		
 		hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("webui")
 				.getInstances()[0].getMachine().getHostAddress();
 		Assert.assertTrue(Arrays.asList(template3IPsArray).contains(hostAddressToCompare));	
 		
 		hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("rest")
 				.getInstances()[0].getMachine().getHostAddress();
 		Assert.assertTrue(Arrays.asList(template3IPsArray).contains(hostAddressToCompare));		
 		
 	}
 	
 	/**
 	 * tests the uninstall operation - uninstalls and checks that each application service is down.
 	 */
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true, priority = 2)
 	public void testPetclinicUninstall(){
 
 		try {
 			uninstallApplicationAndWait("petclinic");
 		} catch (Throwable e) {
 			LogUtils.log("caught an exception while uninstalling petclinic", e);
 			sendTeardownCloudFailedMail(cloudName, e);
 		}
 		
 		AssertUtils.repetitiveAssertTrue("petclinic.mongod is not down", new RepetitiveConditionProvider() {
             public boolean getCondition() {
                 try {
                     return (admin.getProcessingUnits().getProcessingUnit("petclinic.mongod") == null);
                 } catch (Exception e) {
                     return false;
                 }
             }
         }, MY_OPERATION_TIMEOUT);
 		AssertUtils.repetitiveAssertTrue("petclinic.mongos is not down", new RepetitiveConditionProvider() {
 			public boolean getCondition() {
 				try {
 					return (admin.getProcessingUnits().getProcessingUnit("petclinic.mongos") == null);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, MY_OPERATION_TIMEOUT);
 		AssertUtils.repetitiveAssertTrue("petclinic.mongoConfig is not down", new RepetitiveConditionProvider() {
 			public boolean getCondition() {
 				try {
 					return (admin.getProcessingUnits().getProcessingUnit("petclinic.mongoConfig") == null);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, MY_OPERATION_TIMEOUT);
 		AssertUtils.repetitiveAssertTrue("petclinic.tomcat is not down", new RepetitiveConditionProvider() {
 			public boolean getCondition() {
 				try {
 					return (admin.getProcessingUnits().getProcessingUnit("petclinic.tomcat") == null);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, MY_OPERATION_TIMEOUT);
 		AssertUtils.repetitiveAssertTrue("webui is down", new RepetitiveConditionProvider() {
 			public boolean getCondition() {
 				try {
 					return (admin.getProcessingUnits().getProcessingUnit("webui") != null);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, MY_OPERATION_TIMEOUT);
 		AssertUtils.repetitiveAssertTrue("rest is down", new RepetitiveConditionProvider() {
 			public boolean getCondition() {
 				try {
 					return (admin.getProcessingUnits().getProcessingUnit("rest") != null);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, MY_OPERATION_TIMEOUT);
 		AssertUtils.repetitiveAssertTrue("cloudifyManagementSpace is down", new RepetitiveConditionProvider() {
 			public boolean getCondition() {
 				try {
 					return (admin.getProcessingUnits().getProcessingUnit("cloudifyManagementSpace") != null);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, MY_OPERATION_TIMEOUT);
 	
 	}
 	
 	/**
 	 * tests the teardown operation - tearsdown the byon cloud and checks that each management service is down.
 	 */
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true, priority = 3)
 	public void testPetclinicTeardownByon(){
 		try {
 			service.teardownCloud();
 		}
 		catch (Throwable e) {
 			LogUtils.log("caught an exception while tearing down " + service.getCloudName(), e);
 			sendTeardownCloudFailedMail(cloudName, e);
 		}
 		
 		AssertUtils.repetitiveAssertTrue("rest is not down", new RepetitiveConditionProvider() {
 			public boolean getCondition() {
 				try {
 					return (admin.getProcessingUnits().getProcessingUnit("rest") == null);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, MY_OPERATION_TIMEOUT);
 		AssertUtils.repetitiveAssertTrue("webui is not down", new RepetitiveConditionProvider() {
 			public boolean getCondition() {
 				try {
 					return (admin.getProcessingUnits().getProcessingUnit("webui") == null);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, MY_OPERATION_TIMEOUT);
 		AssertUtils.repetitiveAssertTrue("cloudifyManagementSpace is down", new RepetitiveConditionProvider() {
 			public boolean getCondition() {
 				try {
 					return (admin.getProcessingUnits().getProcessingUnit("cloudifyManagementSpace") == null);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, MY_OPERATION_TIMEOUT);
 	}
 
 	@AfterClass(alwaysRun = true)
 	public void teardown() throws IOException {
 
 		putService(new ByonCloudService());
 		restoreOriginalBootstrapManagementFile();
 		restoreOriginalByonCloudFile();
 		restoreOriginalServiceFile("mongos");
 		restoreOriginalServiceFile("mongod");
 		restoreOriginalServiceFile("tomcat");
 		restoreOriginalServiceFile("mongoConfig");
 	}
 
 
 
 	private void restoreOriginalBootstrapManagementFile() throws IOException {
 		originialBootstrapManagement.delete();
 		FileUtils.moveFile(backupStartManagementFile, originialBootstrapManagement);
 
 	}
 
 	private void restoreOriginalByonCloudFile() throws IOException {
 		originialCloudConf.delete();
 		FileUtils.moveFile(backupCloudConfFile, originialCloudConf);
 
 	}
 	
 	private void restoreOriginalServiceFile(String serviceName) throws IOException {
 		
 		File parentDir = mongodbDir;
 		if(serviceName.equalsIgnoreCase("tomcat"))
 			parentDir = tomcatDir;
 		
 		File originalService = new File(parentDir, serviceName + "/" + serviceName + "-service.groovy");
 		originalService.delete();
 		FileUtils.moveFile(new File(originalService + ".backup"), originalService);
 	}
 
 }
 
 
 
