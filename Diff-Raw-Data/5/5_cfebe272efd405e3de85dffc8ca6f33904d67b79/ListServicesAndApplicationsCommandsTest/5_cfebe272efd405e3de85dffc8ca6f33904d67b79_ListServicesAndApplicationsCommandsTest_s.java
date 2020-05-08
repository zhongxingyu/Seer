 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import iTests.framework.utils.ScriptUtils;
 
 import java.io.IOException;
 
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.internal.CloudifyConstants.DeploymentState;
 import org.testng.annotations.Test;
 
 public class ListServicesAndApplicationsCommandsTest extends AbstractLocalCloudTest{
 
     private static final int ONE_SEC_IN_MILLI = 1000;
     
    private static final String LIST_TWO_SERVICES_TEXT = "\ndefault  STARTED\tAuthorization Groups: \n\tdefault.tomcat  STARTED (1/1)\n\tdefault.solr  STARTED (1/1)\n>>> \n>>>";
 	private static final String LIST_TWO_APPLICATIONS_TEXT = "list-applications \nhelloworld  STARTED\tAuthorization Groups: \n\thelloworld.tomcat  STARTED (1/1)\nsimple  STARTED\tAuthorization Groups: \n\tsimple.simple  STARTED (1/1)\n\n>>>";
 	private static final String LIST_PETCLINIC_SERVICES_TEXT = "\npetclinic  STARTED\tAuthorization Groups: \n\tpetclinic.mongod  STARTED (1/1)\n\tpetclinic.tomcat  STARTED (1/1)\n>>>";
 	private static final String LIST_ZERO_SERVICES_OUTPUT = ">>> list-services \n\n>>> ";
	private static final String LIST_ZERO_APPLICATIONS_OUTPUT = ">>> list-applications \n\n>>> \n>>>";
 	
 	
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void listServicesWithMultipleServices() throws Exception {
     	CommandTestUtils.runCommandAndWait("connect " + restUrl + ";install-service tomcat");
         CommandTestUtils.runCommandAndWait("connect " + restUrl + ";install-service solr");
         String listServicesOutput = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";list-services");
         assertTrue("List services yielded a wrong output: " + listServicesOutput
         		+ CloudifyConstants.NEW_LINE + "Expected output: " + LIST_TWO_SERVICES_TEXT, 
         listServicesOutput.contains(LIST_TWO_SERVICES_TEXT));
         
         CommandTestUtils.runCommandAndWait("connect " + restUrl + ";uninstall-service tomcat");
         CommandTestUtils.runCommandAndWait("connect " + restUrl + ";uninstall-service solr");
     }
     
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void listServicesWithinAnApplication() throws Exception {
     	CommandTestUtils.runCommandAndWait("connect " + restUrl + ";install-application petclinic-simple");
         String listServicesOutput = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";use-application petclinic;list-services");
         assertTrue("List services yielded a wrong output: " + listServicesOutput
         		+ CloudifyConstants.NEW_LINE + "Expected output: " + LIST_PETCLINIC_SERVICES_TEXT, 
         listServicesOutput.contains(LIST_PETCLINIC_SERVICES_TEXT));
         
         CommandTestUtils.runCommandAndWait("connect " + restUrl + ";uninstall-application petclinic");
     }
     
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void listServicesWithZeroServices() throws Exception {
         String listServicesOutput = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";list-services");
         assertTrue("List services yielded a wrong output: " + listServicesOutput 
         		+ CloudifyConstants.NEW_LINE + "Expected output: " + LIST_ZERO_SERVICES_OUTPUT, 
         		listServicesOutput.contains(LIST_ZERO_SERVICES_OUTPUT));        
     }
     
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void listApplicationsWithMultipleApplications() throws Exception {
     	CommandTestUtils.runCommandAndWait("connect " + restUrl + ";install-application helloworld");
         String simpleApplicationPath = CommandTestUtils.getPath("src/main/resources/apps/USM/usm/applications/simple");
         CommandTestUtils.runCommandAndWait("connect " + restUrl + ";install-application " + simpleApplicationPath);
         String listApplicationsOutput = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";list-applications");
         assertTrue("List applications yielded a wrong output: " + listApplicationsOutput
         		+ CloudifyConstants.NEW_LINE + "Expected output: " + LIST_TWO_APPLICATIONS_TEXT, 
         		listApplicationsOutput.contains(LIST_TWO_APPLICATIONS_TEXT));
         
         CommandTestUtils.runCommandAndWait("connect " + restUrl + ";uninstall-application helloworld");
         CommandTestUtils.runCommandAndWait("connect " + restUrl + ";uninstall-application simple");
     }
     
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void listApplicationsWithZeroApplications() throws Exception {
         String listApplicationsOutput = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";list-applications");
         assertTrue("List applications yielded a wrong output: " + listApplicationsOutput 
         		+ CloudifyConstants.NEW_LINE + "Expected output: " + LIST_ZERO_APPLICATIONS_OUTPUT, 
         		listApplicationsOutput.contains(LIST_ZERO_APPLICATIONS_OUTPUT));        
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void checkListApplicationsCommandOnSuccessfulInstallation() throws Exception{
         installApplicationAsync();
         assertPetclinicServicesGoThroughAllStagesOfDeployment();
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void checkListApplicationsCommandOnFailedInstallation() throws Exception{
         installBadApplicationAsync();
         assertInstallationEndedWithFailedStatus();
     }
 
     private void assertInstallationEndedWithFailedStatus()
             throws IOException, InterruptedException {
         boolean applicationDeployedWithFailure = false;
         boolean applicationInstallStateOccurred = false;
         boolean badGroovyServiceInstallStateOccurred = false;
         boolean badGroovyServiceFailedStateOccurred = false;
         boolean groovyServiceInstallStateOccurred = false;
         boolean groovyServiceStartStateOccurred = false;
 
         while (!applicationDeployedWithFailure) {
             String listApplicationsOutput = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";list-applications");
             if (listApplicationsOutput.contains("groovyApp.badGroovyService  " + DeploymentState.IN_PROGRESS.toString())) {
                 badGroovyServiceInstallStateOccurred = true;
             }
             if (listApplicationsOutput.contains("groovyApp.badGroovyService  " + DeploymentState.FAILED.toString())) {
                 badGroovyServiceFailedStateOccurred = true;
             }
             if (listApplicationsOutput.contains("groovyApp.groovyService  " + DeploymentState.IN_PROGRESS.toString())) {
                 groovyServiceInstallStateOccurred = true;
             }
             if (listApplicationsOutput.contains("groovyApp.groovyService  " + DeploymentState.STARTED.toString())) {
                 groovyServiceStartStateOccurred = true;
             }
             if (listApplicationsOutput.contains("groovyApp  " + DeploymentState.IN_PROGRESS.toString())) {
                 applicationInstallStateOccurred = true;
             }
             if (listApplicationsOutput.contains("groovyApp  " + DeploymentState.FAILED.toString())) {
                 applicationDeployedWithFailure = true;
             }
             Thread.sleep(ONE_SEC_IN_MILLI);
         }
 
         assertTrue("Service did not go through install state", badGroovyServiceInstallStateOccurred);
         assertTrue("Service installation did not fail as expected", badGroovyServiceFailedStateOccurred);
         assertTrue("Service did not go through install state", groovyServiceInstallStateOccurred);
         assertTrue("Service did not go get to start state", groovyServiceStartStateOccurred);
         assertTrue("Application did not go through install state", applicationInstallStateOccurred);
         assertTrue("Application final deployment state should be failed", applicationDeployedWithFailure);
 
     }
 
 
     private void assertPetclinicServicesGoThroughAllStagesOfDeployment()
             throws IOException, InterruptedException {
 
         boolean applicationDeployed = false;
         boolean applicationInstallStateOccurred = false;
         boolean mongodInstallStateOccurred = false;
         boolean mongodStartedStateOccurred = false;
         boolean tomcatInstallStateOccurred = false;
         boolean tomcatStartedStateOccurred = false;
 
         while (!applicationDeployed) {
             String listApplicationsOutput = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";list-applications");
             if (listApplicationsOutput.contains("petclinic.mongod  " + DeploymentState.IN_PROGRESS.toString())) {
                 mongodInstallStateOccurred = true;
             }
             if (listApplicationsOutput.contains("petclinic.mongod  " + DeploymentState.STARTED.toString())) {
                 mongodStartedStateOccurred = true;
             }
             if (listApplicationsOutput.contains("petclinic.tomcat  " + DeploymentState.IN_PROGRESS.toString())) {
                 tomcatInstallStateOccurred = true;
             }
             if (listApplicationsOutput.contains("petclinic.tomcat  " + DeploymentState.STARTED.toString())) {
                 tomcatStartedStateOccurred = true;
             }
             if (listApplicationsOutput.contains("petclinic  " + DeploymentState.IN_PROGRESS.toString())) {
                 applicationInstallStateOccurred = true;
             }
             if (listApplicationsOutput.contains("petclinic  " + DeploymentState.STARTED.toString())) {
                 applicationDeployed = true;
             }
             Thread.sleep(ONE_SEC_IN_MILLI);
         }
 
         assertTrue("mongod did not go through the install state", mongodInstallStateOccurred);
         assertTrue("mongod did not go through the start state", mongodStartedStateOccurred);
         assertTrue("tomcat did not go through the install state", tomcatInstallStateOccurred);
         assertTrue("tomcat did not go through the start state", tomcatStartedStateOccurred);
         assertTrue("application did not go through the install state", applicationInstallStateOccurred);
 
     }
 
     private void installApplicationAsync() throws IOException, InterruptedException {
         String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/petclinic-simple";
         CommandTestUtils.runCommand("connect " + restUrl + ";install-application " + applicationPath);
     }
 
     private void installBadApplicationAsync() throws IOException, InterruptedException {
         String applicationPath = CommandTestUtils.getPath("src/main/resources/apps/USM/usm/applications/badGroovyApp");
         CommandTestUtils.runCommand("connect " + restUrl + ";install-application -disableSelfHealing " + applicationPath);
     }
 
 }
