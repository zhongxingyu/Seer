 package test.cli.cloudify.cloud.byon;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.exec.util.StringUtils;
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.Assert;
 import org.testng.ITestContext;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.IOUtils;
 import framework.utils.LogUtils;
 
 /**
  * This test installs petclinic with 3 different templates on a byon cloud. It checks that each service was
  * assigned to the correct template, according to byon-cloud.groovy. After the installation completes, the
  * test checks the uninstall and teardown operations.
  * 
  * Note: this test uses 5 fixed machines -
  * 192.168.9.115,192.168.9.116,192.168.9.120,192.168.9.125,192.168.9.126.
  */
 public class MultipleMachineTemplatesTest extends AbstractByonCloudTest {
 	
 	/* holds a list of hosts assigned to a certain template */
 	protected Map<String, List<String>> hostsPerTemplate = new HashMap<String, List<String>>();
 	
 	/* holds which template is used by which service */
 	protected Map<String, String> templatePerService = new HashMap<String, String>();
 		
 	private static final int NUM_HOSTS_PER_TEMPLATE = 2;
 	
 	/* template names */
 	protected static final String SMALL_LINUX = "SMALL_LINUX";
 	protected static final String TEMPLATE_1 = "TEMPLATE_1";
 	protected static final String TEMPLATE_2 = "TEMPLATE_2";
 	protected static final String TEMPLATE_3 = "TEMPLATE_3";
 
 	private static final long MY_OPERATION_TIMEOUT = 1 * 60 * 1000;
 
 	protected static final String PETCLINIC_MULTIPLE_TEMPLATES_SG_PATH = SGTestHelper.getSGTestRootDir() + "/apps/cloudify/recipes/petclinic-multiple-templates";
 	protected static final String PETCLINIC_MULTIPLE_TEMPLATES_BUILD_PATH = SGTestHelper.getBuildDir() + "/recipes/apps/petclinic-multiple-templates";
 
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap(final ITestContext testContext) {
 		super.bootstrap(testContext);
 	}
 
 	@BeforeMethod(alwaysRun = true)
 	public void perpareApplication() throws IOException {					
 		LogUtils.log("copying application " + PETCLINIC_MULTIPLE_TEMPLATES_SG_PATH + " to " + PETCLINIC_MULTIPLE_TEMPLATES_BUILD_PATH);
 		FileUtils.copyDirectory(new File(PETCLINIC_MULTIPLE_TEMPLATES_SG_PATH), new File(PETCLINIC_MULTIPLE_TEMPLATES_BUILD_PATH));
 	}
 
 	/**
 	 * check that each service was assigned to the correct template, according to byon-cloud.groovy.
 	 * 
 	 * @throws Exception
 	 */
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true, priority = 1)
 	public void testPetclinic() throws Exception {
 		
 		injectTemplateInService(TEMPLATE_1, "mongos");
 		injectTemplateInService(TEMPLATE_1, "mongoConfig");
 		injectTemplateInService(TEMPLATE_2, "tomcat");
 		injectTemplateInService(TEMPLATE_2, "apacheLB");
 		injectTemplateInService(TEMPLATE_3, "mongod");
 		
 		installApplicationAndWait(PETCLINIC_MULTIPLE_TEMPLATES_BUILD_PATH, "petclinic");
 
 		Assert.assertTrue(hostsPerTemplate.get(templatePerService.get("petclinic.mongod")).contains(getPuHost("petclinic.mongod")));
 		Assert.assertTrue(hostsPerTemplate.get(templatePerService.get("petclinic.mongos")).contains(getPuHost("petclinic.mongos")));
 		Assert.assertTrue(hostsPerTemplate.get(templatePerService.get("petclinic.mongoConfig")).contains(getPuHost("petclinic.mongoConfig")));
 		Assert.assertTrue(hostsPerTemplate.get(templatePerService.get("petclinic.tomcat")).contains(getPuHost("petclinic.tomcat")));
 		Assert.assertTrue(hostsPerTemplate.get(templatePerService.get("petclinic.apacheLB")).contains(getPuHost("petclinic.apacheLB")));
 		
 		uninstallApplicationAndWait("petclinic");
 
 		assertServiceIsDown("petclinic.mongod");
 		assertServiceIsDown("petclinic.mongos");
 		assertServiceIsDown("petclinic.mongoConfig");
 		assertServiceIsDown("petclinic.tomcat");
 	}
 	
 	private void assertManagementIsDown() {		
 		assertServiceIsDown("webui");
 		assertServiceIsDown("rest");
 		assertServiceIsDown("cloudifyManagementSpace");
 	}
 
 	protected void injectTemplateInService(final String template, String serviceName) throws IOException {
 		
 		String serviceGroovyPath = PETCLINIC_MULTIPLE_TEMPLATES_BUILD_PATH + "/" + serviceName + "/" + serviceName + "-service.groovy";
 		
 		// "ENTER_TEMPLATE" is the hook that resides in the service file for purposes of replacing it
 		LogUtils.log("Service " + serviceName + " will use template " + template);
 		IOUtils.replaceTextInFile(serviceGroovyPath, "ENTER_TEMPLATE", template);
 		templatePerService.put(ServiceUtils.getAbsolutePUName("petclinic", serviceName), template);
 		
 	}
 
 	@AfterMethod
 	public void cleanUp() {
 		super.scanAgentNodesLeak();
 	}
 
 	@AfterClass(alwaysRun = true)
 	protected void teardown() {
 		super.teardown();
 	}
 
 
 	@Override
 	protected void customizeCloud() throws Exception {
 		super.customizeCloud();
 
 		List<String> assignableHosts = new ArrayList<String>(Arrays.asList(getService().getMachines()));
 		
 		File multiTemplatesGroovy = new File(SGTestHelper.getSGTestRootDir()
 				+ "/apps/cloudify/cloud/byon/byon-cloud.groovy");
 
 		// replace the cloud groovy file with a customized one
 		File fileToBeReplaced = new File(getService().getPathToCloudFolder(), "byon-cloud.groovy");
 		Map<File, File> filesToReplace = new HashMap<File, File>();
 		filesToReplace.put(fileToBeReplaced, multiTemplatesGroovy);
 		getService().addFilesToReplace(filesToReplace);
 		
 		hostsPerTemplate.put(TEMPLATE_1, new ArrayList<String>());
 		hostsPerTemplate.put(TEMPLATE_2, new ArrayList<String>());
 		hostsPerTemplate.put(TEMPLATE_3, new ArrayList<String>());
 				
 		LogUtils.log("Assigning hosts for templates");
 		for (String templateName : hostsPerTemplate.keySet()) {
 			
 			List<String> hostsForTemplate = new ArrayList<String>();
 			for (int i = 0 ; i < NUM_HOSTS_PER_TEMPLATE ; i++) {
 				String host = assignableHosts.iterator().next();
 				if (host != null) {
 					LogUtils.log("Host " + host + " was assigned to template " + templateName);
 					hostsForTemplate.add(host);
 					assignableHosts.remove(host);
 				}
 				
 			}
 			hostsPerTemplate.put(templateName, hostsForTemplate);
 		}
 		
 		/* from the remaining hosts, construct the template for the management machine */
 		List<String> managementTemplateHosts = new ArrayList<String>(); 
 		for (String host : assignableHosts) {
 			LogUtils.log("Host " + host + " was assigned to template " + SMALL_LINUX);
 			managementTemplateHosts.add(host);
 		}
 		hostsPerTemplate.put(SMALL_LINUX, managementTemplateHosts);
 		
 		Map<String, String> props = new HashMap<String,String>();
 		for (String template : hostsPerTemplate.keySet()) {
 			props.put(template + "_HOSTS", StringUtils.toString(hostsPerTemplate.get(template).toArray(new String[] {}), ","));			
 		}
 		getService().getAdditionalPropsToReplace().putAll(props);
 	}
 
 	/**
 	 * Gets the address of the machine on which the given processing unit is deployed. 
 	 * @param puName The name of the processing unit to look for
 	 * @return The address of the machine on which the processing unit is deployed.
 	 */
 	protected String getPuHost(final String puName) {
 		ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
 		Assert.assertNotNull(pu.getInstances()[0], puName + " processing unit is not found");
 		return pu.getInstances()[0].getMachine().getHostAddress();		
 	}
 
 	@Override
	protected void beforeTeardown() {
		// override so as to not close admin. we will use it after the teardown
	}
	@Override
 	protected void afterTeardown() throws Exception {		
 		assertManagementIsDown();
 	}
 
 	private void assertServiceIsDown(final String serviceFullName) {
 		AssertUtils.repetitiveAssertTrue(serviceFullName + " is not down", new RepetitiveConditionProvider() {
 			public boolean getCondition() {
 				try {
 					return (admin.getProcessingUnits().getProcessingUnit(serviceFullName) == null);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}, MY_OPERATION_TIMEOUT);
 	}
 }
