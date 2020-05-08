 /*******************************************************************************
  *
  * Copyright (c) 2011 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *    Nikita Levyankov
  *
  *******************************************************************************/
 package hudson.model;
 
 import hudson.tasks.Mailer;
 import hudson.tasks.Maven;
 import hudson.tasks.junit.JUnitResultArchiver;
 import java.io.File;
 import java.net.URISyntaxException;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import static junit.framework.Assert.*;
 import static org.easymock.EasyMock.expect;
 import static org.junit.Assert.assertFalse;
 import static org.powermock.api.easymock.PowerMock.*;
 
 /**
  * Test for legacy
  * <p/>
  * Date: 9/23/11
  *
  * @author Nikita Levyankov
  */
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({Hudson.class})
 public class LegacyProjectTest {
 
     private File config;
 
     @Before
     public void setUp() throws URISyntaxException {
         config = new File(FreeStyleProject.class.getResource("/hudson/model/freestyle").toURI());
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether CustomWorkspace is configured based
      * on legacy value.
      *
      * @throws Exception if any.
      */
     @Test
    public void testСonvertLegacyCustomWorkspaceProperty() throws Exception {
         FreeStyleProject project = (FreeStyleProject) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         assertNull(project.getProperty(FreeStyleProject.CUSTOM_WORKSPACE_PROPERTY_NAME));
         project.convertCustomWorkspaceProperty();
         assertNotNull(project.getProperty(FreeStyleProject.CUSTOM_WORKSPACE_PROPERTY_NAME));
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether Builders
      * from Project are configured
      *
      * @throws Exception if any.
      */
     @Test
    public void testСonvertLegacyBuildersProperty() throws Exception {
         Project project = (Project) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         //Property should be null, because of legacy implementation. Version < 2.2.0
         assertNull(project.getProperty(Project.BUILDERS_PROPERTY_NAME));
         project.convertBuildersProjectProperty();
         //Verify builders
         assertNotNull(project.getProperty(Project.BUILDERS_PROPERTY_NAME));
         assertFalse(project.getBuildersList().isEmpty());
         assertEquals(1, project.getBuildersList().size());
         assertTrue(project.getBuildersList().get(0) instanceof Maven);
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether BuildWrappers
      * from Project are configured
      *
      * @throws Exception if any.
      */
     @Test
    public void testСonvertLegacyBuildWrappersProperty() throws Exception {
         Project project = (Project) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         //Property should be null, because of legacy implementation. Version < 2.2.0
         assertNull(project.getProperty(Project.BUILD_WRAPPERS_PROPERTY_NAME));
         project.convertBuildWrappersProjectProperties();
         //Verify buildWrappers
         assertNotNull(project.getProperty(Project.BUILD_WRAPPERS_PROPERTY_NAME));
         assertTrue(project.getBuildWrappersList().isEmpty());
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether publishers
      * from Project are configured
      *
      * @throws Exception if any.
      */
     @Test
     @Ignore
     //TODO investigate how to mock descriptor loading and creation.
    public void testСonvertPublishersProperty() throws Exception {
         Hudson hudson = createMock(Hudson.class);
         expect(hudson.getDescriptorOrDie(Mailer.class)).andReturn(new Mailer.DescriptorImpl());
         expect(hudson.getDescriptorOrDie(JUnitResultArchiver.class)).andReturn(
             new JUnitResultArchiver.DescriptorImpl());
         mockStatic(Hudson.class);
         expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
         replayAll();
         Project project = (Project) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         String mailerKey = "hudson-tasks-Mailer";
         //Publishers should be null, because of legacy implementation. Version < 2.2.0
         assertNull(project.getProperty(mailerKey));
         project.convertPublishersProperties();
         //Verify publishers
         assertFalse(project.getPublishersList().isEmpty());
         assertEquals(2, project.getPublishersList().size());
         assertNotNull(project.getProperty(mailerKey).getValue());
         verifyAll();
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
      * from AbstractProject is configured
      *
      * @throws Exception if any.
      */
     @Test
    public void testСonvertLegacyBlockBuildWhenDownstreamBuildingProperty() throws Exception {
         AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         assertNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_DOWNSTREAM_BUILDING_PROPERTY_NAME));
         project.convertBlockBuildWhenDownstreamBuildingProperty();
         assertNotNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_DOWNSTREAM_BUILDING_PROPERTY_NAME));
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
      * from AbstractProject is configured
      *
      * @throws Exception if any.
      */
     @Test
    public void testСonvertLegacyBlockBuildWhenUpstreamBuildingProperty() throws Exception {
         AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         assertNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_UPSTREAM_BUILDING_PROPERTY_NAME));
         project.convertBlockBuildWhenUpstreamBuildingProperty();
         assertNotNull(project.getProperty(AbstractProject.BLOCK_BUILD_WHEN_UPSTREAM_BUILDING_PROPERTY_NAME));
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
      * from AbstractProject is configured
      *
      * @throws Exception if any.
      */
     @Test
    public void testСonvertLegacyConcurrentBuildProperty() throws Exception {
         AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         assertNull(project.getProperty(AbstractProject.CONCURRENT_BUILD_PROPERTY_NAME));
         project.convertConcurrentBuildProperty();
         assertNotNull(project.getProperty(AbstractProject.CONCURRENT_BUILD_PROPERTY_NAME));
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
      * from AbstractProject is configured
      *
      * @throws Exception if any.
      */
     @Test
    public void testСonvertLegacyCleanWorkspaceRequiredProperty() throws Exception {
         AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         assertNull(project.getProperty(AbstractProject.CLEAN_WORKSPACE_REQUIRED_PROPERTY_NAME));
         project.convertCleanWorkspaceRequiredProperty();
         assertNotNull(project.getProperty(AbstractProject.CLEAN_WORKSPACE_REQUIRED_PROPERTY_NAME));
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
      * from AbstractProject is configured
      *
      * @throws Exception if any.
      */
     @Test
    public void testСonvertLegacyQuietPeriodProperty() throws Exception {
         AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         assertNull(project.getProperty(AbstractProject.QUIET_PERIOD_PROPERTY_NAME));
         project.convertQuietPeriodProperty();
         assertNotNull(project.getProperty(AbstractProject.QUIET_PERIOD_PROPERTY_NAME));
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
      * from AbstractProject is configured
      *
      * @throws Exception if any.
      */
     @Test
    public void testСonvertLegacyScmCheckoutRetryCountProperty() throws Exception {
         AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         assertNull(project.getProperty(AbstractProject.SCM_CHECKOUT_RETRY_COUNT_PROPERTY_NAME));
         project.convertScmCheckoutRetryCountProperty();
         assertNotNull(project.getProperty(AbstractProject.SCM_CHECKOUT_RETRY_COUNT_PROPERTY_NAME));
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether expected property
      * from AbstractProject is configured
      *
      * @throws Exception if any.
      */
     @Test
    public void testСonvertLegacyJDKProperty() throws Exception {
         AbstractProject project = (AbstractProject) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         assertNull(project.getProperty(AbstractProject.JDK_PROPERTY_NAME));
         project.convertJDKProperty();
         assertNotNull(project.getProperty(AbstractProject.JDK_PROPERTY_NAME));
     }
 
     /**
      * Tests unmarshalls FreeStyleProject configuration and checks whether LogRotator
      * from Job are configured
      *
      * @throws Exception if any.
      */
     @Test
    public void testСonvertLegacyLogRotatorProperty() throws Exception {
         Job project = (Job) Items.getConfigFile(config).read();
         project.setAllowSave(false);
         project.initProjectProperties();
         assertNull(project.getProperty(Job.LOG_ROTATOR_PROPERTY_NAME));
         project.convertLogRotatorProperty();
         assertNotNull(project.getProperty(Job.LOG_ROTATOR_PROPERTY_NAME));
     }
 }
