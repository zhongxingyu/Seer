 package org.cagrid.data.test.upgrades.from1pt5.enumeration;
 
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainer;
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainerFactory;
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainerType;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.DeployServiceStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.DestroyContainerStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.StartContainerStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.StopContainerStep;
 import gov.nih.nci.cagrid.testing.system.deployment.steps.UnpackContainerStep;
 import gov.nih.nci.cagrid.testing.system.haste.Step;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Vector;
 
 import junit.framework.TestResult;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.cagrid.data.test.creation.DataTestCaseInfo;
 import org.cagrid.data.test.creation.DeleteOldServiceStep;
 import org.cagrid.data.test.system.BaseSystemTest;
 import org.cagrid.data.test.system.ResyncAndBuildStep;
 import org.cagrid.data.test.system.VerifyOperationsStep;
 import org.cagrid.data.test.upgrades.UnpackOldServiceStep;
 import org.cagrid.data.test.upgrades.UpgradeIntroduceServiceStep;
 import org.cagrid.data.test.upgrades.UpgradeTestConstants;
 
 /** 
  *  UpgradeEnumerationFrom1pt5Tests
  *  Tests to upgrade an enumeration data service from 1.5 to current
  * 
  * @author <A HREF="MAILTO:ervin@bmi.osu.edu">David W. Ervin</A>  * 
  * @created Feb 20, 2007 
  * @version $Id: UpgradeEnumerationFrom1pt3Tests.java,v 1.1 2008-10-28 15:55:16 dervin Exp $ 
  */
 public class UpgradeEnumerationFrom1pt5Tests extends BaseSystemTest {
     public static final String SERVICE_ZIP_NAME = "DataServiceWithWsEnum_1-5.zip";
     public static final String SERVICE_DIR_NAME = "DataServiceWithWsEnum_1-5";
    public static final String SERVICE_NAME = "DataServiceWithWsEnum";
     public static final String SERVICE_PACKAGE = "org.cagrid.test.data.with.wsenum";
     public static final String SERVICE_NAMESPACE = "http://wsenum.with.data.test.cagrid.org/DataServiceWithEnum";
     
     private DataTestCaseInfo testServiceInfo = null;
     private ServiceContainer container = null;
 	
 	public String getDescription() {
 		return "Tests upgrade of an enumeration data service from version 1.5 to " + UpgradeTestConstants.getCurrentDataVersion();
 	}
     
     
     public String getName() {
         return "Data Service With Enumeration 1_5 to " 
             + UpgradeTestConstants.getCurrentDataVersion().replace(".", "_") 
             + " Upgrade Tests";
     }
     
     
     public boolean storySetUp() {
         this.testServiceInfo = new DataTestCaseInfo() {
             public String getServiceDirName() {
                 return SERVICE_DIR_NAME;
             }
 
 
             public String getName() {
                 return SERVICE_NAME;
             }
 
 
             public String getNamespace() {
                 return SERVICE_NAMESPACE;
             }
 
 
             public String getPackageName() {
                 return SERVICE_PACKAGE;
             }
         };
         try {
             this.container = ServiceContainerFactory.createContainer(ServiceContainerType.TOMCAT_6_CONTAINER);
         } catch (IOException ex) {
             ex.printStackTrace();
             fail("Error creating service container: " + ex.getMessage());
         }
         return true;
     }
 	
 
 	protected Vector<?> steps() {
         Vector<Step> steps = new Vector<Step>();
 		// steps to unpack and upgrade the old service
 		steps.add(new DeleteOldServiceStep(testServiceInfo));
 		steps.add(new UnpackOldServiceStep(SERVICE_ZIP_NAME));
 		steps.add(new UpgradeIntroduceServiceStep(testServiceInfo.getDir()));
 		steps.add(new ResyncAndBuildStep(testServiceInfo, getIntroduceBaseDir()));
 	    // deploy the service, check out the CQL 2 related operations and metadata
         steps.add(new UnpackContainerStep(container));
         steps.add(new DeployServiceStep(container, testServiceInfo.getDir(), 
             Collections.singletonList("-Dno.deployment.validation=true")));
         steps.add(new StartContainerStep(container));
         steps.add(new VerifyOperationsStep(container, testServiceInfo.getName(),
             false, true, false));
 		return steps;
 	}
     
     
     protected void storyTearDown() throws Throwable {
         if (container.isStarted()) {
             Step stopStep = new StopContainerStep(container);
             stopStep.runStep();
         }
         if (container.isUnpacked()) {
             Step deleteStep = new DestroyContainerStep(container);
             deleteStep.runStep();
         }
         Step deleteServiceStep = new DeleteOldServiceStep(testServiceInfo);
         deleteServiceStep.runStep();
     }
 
 
 	public static void main(String[] args) {
 		TestRunner runner = new TestRunner();
 		TestResult result = runner.doRun(new TestSuite(UpgradeEnumerationFrom1pt5Tests.class));
 		System.exit(result.errorCount() + result.failureCount());
 	}
 }
