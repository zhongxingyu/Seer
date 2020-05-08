 /**
  *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  The software in this package is published under the terms of the AGPL license
  *  a copy of which has been included with this distribution in the license.txt file.
  */
 package org.fusesource.cloudmix.agent.karaf;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.*;
 
 import junit.framework.TestCase;
 
 import org.apache.felix.karaf.admin.AdminService;
 import org.apache.felix.karaf.admin.Instance;
 import org.apache.felix.karaf.admin.InstanceSettings;
 import org.easymock.EasyMock;
 import org.fusesource.cloudmix.agent.Feature;
 import org.fusesource.cloudmix.common.GridClient;
 import org.fusesource.cloudmix.common.dto.ProvisioningAction;
 import org.fusesource.cloudmix.common.util.FileUtils;
 
 /**
  * Test cases for using the Karaf agent to create new Karaf instances on the fly
  */
 public class KarafAgentInstancesTest extends TestCase {
     private static final String ORIGINAL_REPOSITORY 
         = "mvn:org.apache.felix.karaf/features/RELEASE/xml/features";
     private static final String REPOSITORY = "mvn:org.fusesource.cloudmix/features/1.3-SNAPSHOT/xml/features";
     private static final String ORIGINAL_FEATURE = "webconsole";
     private static final String FEATURE = "cloudmix.agent";
 
     private GridClient cl;
     private AdminService service;
     private KarafAgent agent;
     private List<Feature> agentFeatures = new LinkedList<Feature>();
 
     private File workdir;
 
 
     @Override
     protected void setUp() throws Exception {
         cl = EasyMock.createNiceMock(GridClient.class);
         EasyMock.replay(cl);
 
         agent = new KarafAgent() {
             @Override
             public GridClient getClient() {
                 return cl;
             }
 
             @Override
             protected void addAgentFeature(Feature feature) {
                 super.addAgentFeature(feature);
                 agentFeatures.add(feature);
             }
         };
         workdir = new File("testworkdir");
         FileUtils.createDirectory(workdir);
 
         service = new MockAdminService();
 
         agent.setAdminService(service);
         agent.setWorkDirectory(workdir);
         agent.setDetailsPropertyFilePath(workdir.getAbsolutePath() + "/agent.properties");
     }
 
     @Override
     protected void tearDown() throws Exception {
         if (workdir != null) {
             FileUtils.deleteDirectory(workdir);
         }
     }
 
     /*
      * Test creating a new Karaf instance through the AdminService
      */
     public void testCreateNewInstance() throws Exception {
         ProvisioningAction action = new ProvisioningAction();
         action.setId("my-action-id");
         action.setFeature("my-feature-name");
 
         agent.installFeatures(action, null, "karaf:instance");
         assertEquals("One instance should have been created", 1, service.getInstances().length);
         assertEquals("One agent feature should have registered", 1, agentFeatures.size());
 
         Instance instance = service.getInstances()[0];
         assertEquals("Instance should have been started", Instance.STARTED, instance.getState());
 
         // now, let's uninstall the agent feature to destroy the instance
         agent.uninstallFeature(agentFeatures.get(0));
         assertEquals("Instance should have been stopped", Instance.STOPPED, instance.getState());
         assertEquals("Instance should have been destroyed", 0, service.getInstances().length);
     }
 
     /*
      * Test creating a new Karaf instance through the AdminService, specifying the initial list of feature
      * repositories and features to be installed from them
      */
     public void testCreateNewInstanceWithFeature() throws Exception {
         ProvisioningAction action = new ProvisioningAction();
         action.setId("my-action-id");
         action.setFeature("my-feature-name");
 
         agent.installFeatures(action, null, "karaf:instance " + REPOSITORY + " " + FEATURE);
         assertEquals("One instance should have been created", 1, service.getInstances().length);
         assertEquals("One agent feature should have registered", 1, agentFeatures.size());
 
         Instance instance = service.getInstances()[0];
         assertEquals("Instance should have been started", Instance.STARTED, instance.getState());
         File etc = new File(instance.getLocation(), "etc");
         Properties config = new Properties();
         config.load(new FileInputStream(new File(etc, "org.apache.felix.karaf.features.cfg")));
 
         assertTrue("Features repository url should have been added", config
             .getProperty(KarafAgent.FEATURES_REPOSITORIES).contains(REPOSITORY));
         assertEquals("There should be two repositories listed in total", 2, config
             .getProperty(KarafAgent.FEATURES_REPOSITORIES).split(",").length);
         assertTrue("Boot feature should have been added", config.getProperty(KarafAgent.FEATURES_BOOT)
             .contains(FEATURE));
         assertEquals("There should be two boot features listed in total", 2, config
             .getProperty(KarafAgent.FEATURES_BOOT).split(",").length);
 
         // now, let's uninstall the agent feature to destroy the instance
         agent.uninstallFeature(agentFeatures.get(0));
         assertEquals("Instance should have been stopped", Instance.STOPPED, instance.getState());
         assertEquals("Instance should have been destroyed", 0, service.getInstances().length);
     }
 
     /*
      * Test the conversion of a feature name into a valid Karaf instance name
      */
     public void testGetInstanceName() throws Exception {
         assertEquals("Replace : by _ when determining the instance name",
                      "ceffbab3-bc3d-4c72-a89c-154ab2052971_my-feature", agent
                          .getInstanceName("ceffbab3-bc3d-4c72-a89c-154ab2052971:my-feature"));
     }
 
     /*
      * Mock AdminService implementation
      */
     private class MockAdminService implements AdminService {
 
         private final class MockAdminInstance implements Instance {
             private final String name;
             private String state;
             private File location;
 
             private MockAdminInstance(String name) {
                 this.name = name;
                 location = new File("target/instances/" + name);
             }
 
             public void stop() throws Exception {
                 state = STOPPED;
             }
 
             public void start(String arg0) throws Exception {
                 state = STARTED;
             }
 
             public String getState() throws Exception {
                 return state;
             }
 
             public int getPort() {
                 return 0;
             }
 
             public int getPid() {
                 return 0;
             }
 
             public String getName() {
                 return name;
             }
 
             public String getLocation() {
                 return location.getAbsolutePath();
             }
 
             public void destroy() throws Exception {
                 instances.remove(getName());
             }
 
             public void changePort(int arg0) throws Exception {
                 throw new UnsupportedOperationException("Not implemented");
             }

            public boolean isRoot() {
                return false;
            }           
         }
 
         private Map<String, Instance> instances = new HashMap<String, Instance>();
 
 
         public Instance createInstance(String name, InstanceSettings settings) throws Exception {
             assertEquals("The agent should not assign a port number", 0, settings.getPort());
             assertNull("The agent should not assign a location", settings.getLocation());
 
             Instance instance = new MockAdminInstance(name);
             createInstanceLocation(instance);
             instances.put(name, instance);
             return instance;
 
         }
 
         private void createInstanceLocation(Instance instance) throws IOException {
             File etc = new File(instance.getLocation(), "etc");
             etc.mkdirs();
 
             Properties properties = new Properties();
             properties.setProperty(KarafAgent.FEATURES_REPOSITORIES, ORIGINAL_REPOSITORY);
             properties.setProperty(KarafAgent.FEATURES_BOOT, ORIGINAL_FEATURE);
             properties.store(new FileOutputStream(new File(etc, "org.apache.felix.karaf.features.cfg")),
                              "Default repository and feature created from mock AdminService");
         }
 
         public Instance getInstance(String name) {
             return instances.get(name);
         }
 
         public Instance[] getInstances() {
             return instances.values().toArray(new Instance[] {});
         }
     }
 }
