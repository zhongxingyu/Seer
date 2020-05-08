 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.persistence;
 
 import com.j_spaces.kernel.PlatformVersion;
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.quality.iTests.framework.tools.SGTestHelper;
 import org.cloudifysource.quality.iTests.framework.utils.*;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.AbstractByonCloudTest;
 import org.cloudifysource.restclient.GSRestClient;
 import org.cloudifysource.restclient.RestException;
 import org.jclouds.compute.domain.NodeMetadata;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * User: nirb
  * Date: 06/03/13
  */
 public abstract class AbstractByonManagementPersistencyTest extends AbstractByonCloudTest {
 
     private static final String PATH_TO_SERVICE = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/custom-tomcat");;
 
     private static final String BOOTSTRAP_SUCCEEDED_STRING = "Successfully created Cloudify Manager";
 
     private static final String APPLICATION_NAME = "default";
     private static final String EC2_USER = "ec2-user";
 
     private int numOfManagementMachines = 2;
 
     protected String backupFilePath = SGTestHelper.getBuildDir() + "/backup-details.txt";
 
     private Map<String, Integer> installedServices = new HashMap<String, Integer>();
 
     private List<String> attributesList = new LinkedList<String>();
 
     protected void installTomcatService(final int numberOfInstances, final String overrideName) throws IOException, InterruptedException {
 
         copyCustomTomcatToBuild();
 
         try {
 
             // replace number of instances
             File customTomcatGroovy = new File(ScriptUtils.getBuildRecipesServicesPath() + "/custom-tomcat", "tomcat-service.groovy");
             IOUtils.replaceTextInFile(customTomcatGroovy.getAbsolutePath(), "ENTER_NUMBER_OF_INSTANCES", "" + numberOfInstances + "");
 
             // TODO - Once CLOUDIFY-1591 is fixed, use -name option to override a service installation name.
             // replace name if needed
             String actualServiceName;
             if (overrideName != null) {
                 actualServiceName = overrideName;
             } else {
                 actualServiceName = "tomcat";
             }
             IOUtils.replaceTextInFile(customTomcatGroovy.getAbsolutePath(), "ENTER_NAME", actualServiceName);
 
             // install the custom tomcat
             ServiceInstaller tomcatInstaller = new ServiceInstaller(getRestUrl(), actualServiceName);
             tomcatInstaller.recipePath("custom-tomcat");
             tomcatInstaller.timeoutInMinutes(5 * numberOfInstances);
             tomcatInstaller.install();
 
             installedServices.put(actualServiceName, numberOfInstances);
             CloudBootstrapper bootstrapper = getService().getBootstrapper();
             String attributes = bootstrapper.listServiceInstanceAttributes(APPLICATION_NAME, actualServiceName, 1, false);
             attributesList.add(attributes.substring(attributes.indexOf("home")));
 
         } finally  {
             deleteCustomTomcatFromBuild();
         }
 
     }
 
     private void copyCustomTomcatToBuild() throws IOException {
         deleteCustomTomcatFromBuild();
         FileUtils.copyDirectoryToDirectory(new File(PATH_TO_SERVICE), new File(ScriptUtils.getBuildRecipesServicesPath()));
     }
 
     private void deleteCustomTomcatFromBuild() throws IOException {
         File customTomcat = new File(ScriptUtils.getBuildRecipesServicesPath(), "custom-tomcat");
         if (customTomcat.exists()) {
             FileUtils.deleteDirectory(customTomcat);
         }
     }
 
 
     /**
      * 1. Shutdown management machines.
      * 2. Bootstrap using the persistence file.
      * 3. Retrieve attributes from space and compare with the ones before the shutdown.
      * 4. Shutdown an instance agent and wait for recovery.
      * @throws Exception
      */
     public void testManagementPersistency() throws Exception {
 
         shutdownManagement();
 
         CloudBootstrapper bootstrapper = new CloudBootstrapper();
         bootstrapper.provider(getService().getBootstrapper().getProvider());
         bootstrapper.scanForLeakedNodes(false);
         bootstrapper.useExistingFilePath(backupFilePath);
         bootstrapper.bootstrap();
         bootstrapper.setRestUrl(getRestUrl());
 
         List<String> newAttributesList = new LinkedList<String>();
 
         for (String serviceName : installedServices.keySet()) {
             String attributes = bootstrapper.listServiceInstanceAttributes(APPLICATION_NAME, serviceName, 1, false);
             newAttributesList.add(attributes.substring(attributes.indexOf("home")));
         }
 
         List<String> differenceAttributesList = new LinkedList<String>(attributesList);
         differenceAttributesList.removeAll(newAttributesList);
 
         AssertUtils.assertTrue("the service attributes post management restart are not the same as the attributes pre restart", differenceAttributesList.isEmpty());
 
         String serviceToShutdown = installedServices.keySet().iterator().next();
         LogUtils.log("Shutting down GSA that belonges to " + serviceToShutdown);
         admin.getProcessingUnits().getProcessingUnit(ServiceUtils.getAbsolutePUName(APPLICATION_NAME, serviceToShutdown))
                 .getInstances()[0].getGridServiceContainer().getGridServiceAgent().shutdown();
 
         LogUtils.log("Waiting for service to restart on a new machine");
         final GSRestClient client = new GSRestClient("", "", new URL(getRestUrl()), PlatformVersion.getVersionNumber());
 
         final AtomicReference<String> brokenService = new AtomicReference<String>();
 
         AssertUtils.repetitiveAssertTrue("Service didn't break", new AssertUtils.RepetitiveConditionProvider() {
             @Override
             public boolean getCondition() {
                 try {
 
                     // we don't know which service the agent we shutdown belonged to.
                     // query all installed services to find out.
                     for (String serviceName : installedServices.keySet()) {
                         String serviceRestUrl = "ProcessingUnits/Names/" + APPLICATION_NAME + "." + serviceName;
                         int numberOfInstances = Integer.parseInt((String) client.getAdminData(serviceRestUrl).get("NumberOfInstances"));
                         LogUtils.log("Number of " + serviceName + " instances is " + numberOfInstances);
                         if (numberOfInstances < installedServices.get(serviceName)) {
                             LogUtils.log(serviceName + " service broke. it now has only " + numberOfInstances + " instances");
                             brokenService.set(serviceName);
                         }
                     }
                    return (brokenService != null);
                 } catch (RestException e) {
                     throw new RuntimeException(e);
                 }
 
             }
         } , OPERATION_TIMEOUT * 4);
 
         // now we already know the service that broke.
         // so we wait for it to recover.
         AssertUtils.repetitiveAssertTrue(brokenService.get() + " service did not recover", new AssertUtils.RepetitiveConditionProvider() {
             @Override
             public boolean getCondition() {
                 final String brokenServiceRestUrl = "ProcessingUnits/Names/" + APPLICATION_NAME + "." + brokenService.get();
                 try {
                     int numOfInst = Integer.parseInt((String) client.getAdminData(brokenServiceRestUrl).get("NumberOfInstances"));
                     return (1 == numOfInst);
 
 /* CLOUDIFY-1602
                     int numOfPlannedInstances = Integer.parseInt((String) client.getAdminData(brokenServiceRestUrl).get("PlannedNumberOfInstances"));
                     return (1 == numOfPlannedInstances);
 */
 
                 } catch (RestException e) {
                     throw new RuntimeException("caught a RestException", e);
                 }
             }
         } , OPERATION_TIMEOUT * 3);
     }
 
     /**
      * 1. Shutdown management machines.
      * 2. Corrupt the persistence file.
      * 3. Bootstrap with bad file.
      * @throws Exception
      */
     protected void testBadPersistencyFile() throws Exception {
 
         shutdownManagement();
 
         IOUtils.replaceTextInFile(backupFilePath, "instanceId", "instnceId");
 
         CloudBootstrapper bootstrapper = getService().getBootstrapper();
         bootstrapper.useExistingFilePath(backupFilePath);
         bootstrapper.setBootstrapExpectedToFail(true);
         bootstrapper.bootstrap();
 
         String output = bootstrapper.getLastActionOutput();
 
         AssertUtils.assertTrue("bootstrap succeeded with a defective persistence file", !output.contains(BOOTSTRAP_SUCCEEDED_STRING));
     }
 
     /**
      * 1. Shutdown management machines.
      * 2. Bootstrap without persistence file. (Only for DefaultProvisioningDriver)
      * 3. Check management machines are the same.
      * 4. repeat 1-3, 4 times.
      * @throws Exception
      */
     protected void testRepetitiveShutdownManagersBootstrap() throws Exception {
 
         // retrieve the rest url's before we start the chaos.
         final Set<String> originalRestUrls = toSet(getService().getRestUrls());
 
         int repetitions = 4;
 
         for(int i=0; i < repetitions; i++){
 
             shutdownManagement();
 
             CloudBootstrapper bootstrapper = new CloudBootstrapper();
             bootstrapper.provider(getService().getBootstrapper().getProvider());
             bootstrapper.scanForLeakedNodes(false);
             bootstrapper.useExistingFilePath(backupFilePath);
             bootstrapper.bootstrap();
 
             String output = bootstrapper.getLastActionOutput();
 
             AssertUtils.assertTrue("bootstrap failed", output.contains("Successfully created Cloudify Manager"));
 
             // check the rest urls are the same;
             final Set<String> newRestUrls = new HashSet<String>();
             for (URL url : getService().getBootstrapper().getRestAdminUrls()) {
                 newRestUrls.add(url.toString());
             }
             AssertUtils.assertEquals("Expected rest url's not to change after re-bootstrapping", originalRestUrls, newRestUrls);
         }
     }
 
 
     protected void shutdownManagement() throws Exception{
 
         CloudBootstrapper bootstrapper = getService().getBootstrapper();
         bootstrapper.setRestUrl(getRestUrl());
 
         LogUtils.log("shutting down managers");
         File persistenceFile = new File(backupFilePath);
         if (persistenceFile.exists()) {
             FileUtils.deleteQuietly(persistenceFile);
         }
         bootstrapper.shutdownManagers("default", backupFilePath, false);
     }
 
     private Set<String> toSet(final String[] array) {
         final Set<String> set = new HashSet<String>();
         for (String s : array) {
             set.add(s);
         }
         return set;
     }
 
     public void testCorruptedPersistencyDirectory() throws Exception {
 
         String persistencyFolderPath = getService().getCloud().getConfiguration().getPersistentStoragePath();
         String fileToDeletePath = persistencyFolderPath + "/deploy/management-space";
         JCloudsUtils.createContext(getService());
         Set<? extends NodeMetadata> managementMachines = JCloudsUtils.getServersByName(getService().getMachinePrefix() + "cloudify-manager");
         JCloudsUtils.closeContext();
 
         Iterator<? extends NodeMetadata> managementNodesIterator = managementMachines.iterator();
         String machineIp1 = managementNodesIterator.next().getPrivateAddresses().iterator().next();
         String machineIp2 = managementNodesIterator.next().getPrivateAddresses().iterator().next();
 
         SSHUtils.runCommand(machineIp1, OPERATION_TIMEOUT, "rm -rf " + fileToDeletePath, EC2_USER, getPemFile());
         SSHUtils.runCommand(machineIp2, OPERATION_TIMEOUT, "rm -rf " + fileToDeletePath, EC2_USER, getPemFile());
 
         shutdownManagement();
 
         CloudBootstrapper bootstrapper = new CloudBootstrapper();
         bootstrapper.provider(getService().getBootstrapper().getProvider());
         bootstrapper.setBootstrapExpectedToFail(true);
         bootstrapper.timeoutInMinutes(15);
         bootstrapper.bootstrap();
 
         String output = bootstrapper.getLastActionOutput();
         AssertUtils.assertTrue("bootstrap succeeded with a corrupted persistency folder", !output.contains(BOOTSTRAP_SUCCEEDED_STRING));
 
     }
 
     @Override
     protected void customizeCloud() throws Exception {
         super.customizeCloud();
         getService().setNumberOfManagementMachines(numOfManagementMachines);
         getService().getProperties().put("persistencePath", "/home/ec2-user/persistence");
     }
 }
