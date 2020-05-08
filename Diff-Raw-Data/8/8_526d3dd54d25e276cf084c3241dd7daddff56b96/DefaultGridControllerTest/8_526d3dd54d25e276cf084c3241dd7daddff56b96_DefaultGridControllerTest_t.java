 /**
  *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  The software in this package is published under the terms of the AGPL license
  *  a copy of which has been included with this distribution in the license.txt file.
  */
 package org.fusesource.cloudmix.controller.provisioning;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.fusesource.cloudmix.common.ControllerDataProvider;
 import org.fusesource.cloudmix.common.controller.AgentController;
 import org.fusesource.cloudmix.common.controller.ProfileController;
 import org.fusesource.cloudmix.common.dto.AgentDetails;
 import org.fusesource.cloudmix.common.dto.ProfileDetails;
 import org.easymock.EasyMock;
 import org.easymock.IAnswer;
 import org.fusesource.cloudmix.controller.provisioning.SimpleControllerDataProvider;
 import org.fusesource.cloudmix.controller.provisioning.DefaultGridController;
 
 public class DefaultGridControllerTest extends TestCase {
     public void testSetDataProviderAlsoSetsReferenceToController() {
         SimpleControllerDataProvider dp = new SimpleControllerDataProvider();
         DefaultGridController gc = new DefaultGridController();
         
         assertNull(dp.getGrid());
         gc.setDataProvider(dp);
         assertSame(gc, dp.getGrid());
     }
     
     public void testAddProfile() throws Exception {
         ControllerDataProvider dp = EasyMock.createMock(ControllerDataProvider.class);
         DefaultGridController gc = new DefaultGridController();
         final ProfileDetails pd = new ProfileDetails("testing");
         
         EasyMock.expect(dp.addProfile((String) EasyMock.anyObject(),
                                       (ProfileController) EasyMock.anyObject())).
             andAnswer(new IAnswer<ProfileController>() {
                 public ProfileController answer() throws Throwable {
                     String id = (String) EasyMock.getCurrentArguments()[0];
                     assertEquals("testing", id);
                     
                     ProfileController pc = (ProfileController) EasyMock.getCurrentArguments()[1];
                     assertEquals(pd, pc.getDetails());
                     return pc;
                 }            
             });
         dp.setGrid(gc);
         EasyMock.replay(dp);
         
         gc.setDataProvider(dp);
 
         gc.addProfile(pd);
         EasyMock.verify(dp);
     }
     
     public void testRemoveProfile() throws Exception {
         ControllerDataProvider dp = EasyMock.createMock(ControllerDataProvider.class);
         DefaultGridController gc = new DefaultGridController();

         EasyMock.expect(dp.removeProfile("production")).andReturn(
                 new ProfileController(gc, new ProfileDetails("production")));

         dp.setGrid(gc);

        // TODO bit of a hack but the default implementation invokes getFeatures by default
        EasyMock.expect(dp.getFeatures()).andReturn(Collections.EMPTY_LIST);

         EasyMock.replay(dp);
         
         gc.setDataProvider(dp);
         
         gc.removeProfile("production");
         EasyMock.verify(dp);
     }
     
     public void testAgentTrackers() throws Exception {        
         ControllerDataProvider dp = EasyMock.createMock(ControllerDataProvider.class);
         DefaultGridController gc = new DefaultGridController();
         AgentController ac1 = new AgentController(gc, new AgentDetails());
         ac1.markActive();
         AgentController ac2 = new AgentController(gc, new AgentDetails());
         AgentController ac3 = new AgentController(gc, new AgentDetails());
         ac3.markActive();
         Set<AgentController> agents = new HashSet<AgentController>();
         agents.addAll(Arrays.asList(ac1, ac2, ac3));
         
         dp.setGrid(gc);
         EasyMock.expect(dp.getAgents()).andReturn(agents);
         EasyMock.replay(dp);
         
         gc.setDataProvider(dp);
         
         Set<AgentController> expectedAgents = new HashSet<AgentController>();
         expectedAgents.addAll(Arrays.asList(ac1, ac3));
         assertEquals(expectedAgents, new HashSet<AgentController>(gc.agentTrackers()));
         
         EasyMock.verify(dp);        
     }
     
     public void testAgentTrackers2() {
         ControllerDataProvider dp = EasyMock.createMock(ControllerDataProvider.class);
         DefaultGridController gc = new DefaultGridController();
         
         AgentDetails ad1 = new AgentDetails();
         ad1.setProfile("testing");
         AgentController ac1 = new AgentController(gc, ad1);
         ac1.markActive();
         AgentDetails ad2 = new AgentDetails();
         ad2.setProfile("production");
         AgentController ac2 = new AgentController(gc, ad2);
         ac2.markActive();
         AgentDetails ad3 = new AgentDetails();
         ad3.setProfile("production");
         AgentController ac3 = new AgentController(gc, ad3);
         ac3.markActive();
         
         Set<AgentController> agents = new HashSet<AgentController>();
         agents.addAll(Arrays.asList(ac1, ac2, ac3));
         
         dp.setGrid(gc);
         EasyMock.expect(dp.getAgents()).andReturn(agents).anyTimes();
         EasyMock.replay(dp);
         
         gc.setDataProvider(dp);
         
         assertEquals(Collections.singletonList(ac1), gc.agentTrackers("testing"));
         assertEquals(new HashSet<AgentController>(Arrays.asList(ac2, ac3)), 
                      new HashSet<AgentController>(gc.agentTrackers("production")));
         assertEquals(Collections.emptyList(), gc.agentTrackers("default"));
         
         EasyMock.verify(dp);        
     }
 
     public void testAddAgentDetails() {
         DefaultGridController gc = new DefaultGridController();
         AgentDetails ad = new AgentDetails();
         ad.setProfile("testing");
         ad.setHostname("somehost.somedomain");
         ad.setPid(12345);
         assertNull("Precondition failed", ad.getId());
         gc.addAgentDetails(ad);        
         String id = ad.getId();
         assertTrue(id.startsWith("testing_somehost"));
         
         AgentController ac = gc.getDataProvider().getAgent(id);
         assertSame(ad, ac.getDetails());
         assertNotNull(ac.getHistory());
     }
     
     public void testUpdateAgentDetails() {
         DefaultGridController gc = new DefaultGridController();
         AgentDetails ad = new AgentDetails();
         gc.updateAgentDetails("a1", ad); // should not bomb
         
         AgentController ac = new AgentController(gc, null);
         gc.getDataProvider().addAgent("a1", ac);
 
         assertNull("Precondition failed", gc.getAgentDetails("a1"));        
         gc.updateAgentDetails("a1", ad);
         assertSame(ad, gc.getAgentDetails("a1"));
         assertEquals("a1", ad.getId());
     }
     
     public void testGetAgentsAssignedToFeature() {
         DefaultGridController gc = new DefaultGridController();
         ControllerDataProvider dp = gc.getDataProvider();
 
         assertEquals(0, gc.getAgentsAssignedToFeature("f1").size());
         assertEquals(0, gc.getAgentsAssignedToFeature("f1", "production", false).size());
                 
         AgentDetails ad1 = new AgentDetails();
         ad1.setId("a1");
         ad1.setProfile("production");
         AgentController a1 = new AgentController(gc, ad1);
         a1.getFeatures().add("f2");
         dp.addAgent("a1", a1);
         
         AgentDetails ad2 = new AgentDetails();
         ad2.setId("a2");
         ad2.setProfile("default");
         AgentController a2 = new AgentController(gc, ad2);
         a2.getFeatures().add("f1");
         dp.addAgent("a2", a2);
         
         AgentDetails ad3 = new AgentDetails();
         ad3.setId("a3");
         AgentController a3 = new AgentController(gc, ad3);
         a3.getFeatures().add("f1");
         dp.addAgent("a3", a3);
         
         AgentDetails ad4 = new AgentDetails();
         ad4.setId("a4");
         ad4.setProfile("production");
         AgentController a4 = new AgentController(gc, ad4);
         a4.getFeatures().add("f1");        
         a4.getFeatures().add("f2");
         dp.addAgent("a4", a4);
 
         assertEquals("No agents should be reported since none are active",
                      0,
                      gc.getAgentsAssignedToFeature("f1", "default", false).size());
         
         // activate all agents
         a1.markActive();
         a2.markActive();
         a3.markActive();
         a4.markActive();
         
         assertEquals(new HashSet<String>(Arrays.asList("a2", "a3")),
                      new HashSet<String>(gc.getAgentsAssignedToFeature("f1", "default", false)));
         assertEquals(new HashSet<String>(Arrays.asList("a4")), 
                      new HashSet<String>(gc.getAgentsAssignedToFeature("f1", "production", false)));
         assertEquals(new HashSet<String>(Arrays.asList("a1", "a4")), 
                      new HashSet<String>(gc.getAgentsAssignedToFeature("f2", "production", false)));
         assertEquals(new HashSet<String>(Arrays.asList("a2", "a3", "a4")), 
                      new HashSet<String>(gc.getAgentsAssignedToFeature("f1")));
     }
 }
