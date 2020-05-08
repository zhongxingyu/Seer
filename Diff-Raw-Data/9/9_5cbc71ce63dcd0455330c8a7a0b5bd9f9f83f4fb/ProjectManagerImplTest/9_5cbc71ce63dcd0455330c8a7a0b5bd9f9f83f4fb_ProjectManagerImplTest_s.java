 /**
  * Licensed to the Austrian Association for Software Tool Integration (AASTI)
  * under one or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information regarding copyright
  * ownership. The AASTI licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.opencit.core.projectmanager.internal;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.hasItem;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyLong;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.doAnswer;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Semaphore;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.openengsb.core.api.ConnectorValidationFailedException;
 import org.openengsb.core.api.OsgiUtilsService;
 import org.openengsb.core.api.WiringService;
 import org.openengsb.core.api.context.Context;
 import org.openengsb.core.api.context.ContextCurrentService;
 import org.openengsb.core.api.persistence.PersistenceException;
 import org.openengsb.core.api.persistence.PersistenceManager;
 import org.openengsb.core.api.workflow.WorkflowService;
 import org.openengsb.core.common.util.DefaultOsgiUtilsService;
 import org.openengsb.core.security.BundleAuthenticationToken;
 import org.openengsb.core.test.AbstractOsgiMockServiceTest;
 import org.openengsb.core.test.DummyPersistence;
 import org.openengsb.domain.report.ReportDomain;
 import org.openengsb.domain.scm.CommitRef;
 import org.openengsb.domain.scm.ScmDomain;
 import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
 import org.openengsb.opencit.core.projectmanager.ProjectAlreadyExistsException;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 import org.openengsb.opencit.core.projectmanager.model.Project.State;
 import org.openengsb.opencit.core.projectmanager.util.ConnectorUtil;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 
 public class ProjectManagerImplTest extends AbstractOsgiMockServiceTest {
 
     private ProjectManagerImpl projectManager;
     private ContextCurrentService contextMock;
     private DummyPersistence persistence;
     private SchedulingServiceImpl scheduler;
     private WorkflowService workflowService;
     private ScmDomain scmMock;
     private BundleContext bundleContext;
     private ReportDomain reportMock;
     private DefaultOsgiUtilsService serviceUtils;
 
     @Before
     public void setUp() throws Exception {
 
         scheduler = new SchedulingServiceImpl();
         scheduler.setOsgiUtilsService(serviceUtils);
         projectManager = new ProjectManagerImpl();
         projectManager.setOsgiUtilsService(serviceUtils);
         scheduler.setProjectManager(projectManager);
         projectManager.setScheduler(scheduler);
         projectManager.setBundleContext(bundleContext);
 
         AuthenticationManager authenticationManager = makeAuthenticationManager();
         scheduler.setAuthenticationManager(authenticationManager);
         projectManager.setOsgiUtilsService(serviceUtils);
 
         workflowService = mock(WorkflowService.class);
         scheduler.setWorkflowService(workflowService);
         scheduler.setOsgiUtilsService(serviceUtils);
 
         contextMock = Mockito.mock(ContextCurrentService.class);
 
         Context mockContext = Mockito.mock(Context.class);
         when(mockContext.getId()).thenReturn("test");
         Mockito.when(contextMock.getContext()).thenReturn(mockContext);
         projectManager.setContextService(contextMock);
 
         WiringService wiringService = Mockito.mock(WiringService.class);
 
         reportMock = Mockito.mock(ReportDomain.class);
         when(wiringService.getDomainEndpoint(ReportDomain.class, "report")).thenReturn(reportMock);
 
         scmMock = Mockito.mock(ScmDomain.class);
         when(wiringService.getDomainEndpoint(eq(ScmDomain.class), eq("scm"), anyString())).thenReturn(scmMock);
 
         registerServiceViaId(wiringService, "wiring", WiringService.class);
 
         PersistenceManager persistenceManagerMock = Mockito.mock(PersistenceManager.class);
         persistence = new DummyPersistence();
         Mockito.when(persistenceManagerMock.getPersistenceForBundle(Mockito.any(Bundle.class))).thenReturn(
             persistence);
         projectManager.setPersistenceManager(persistenceManagerMock);
         ConnectorUtil connectorUtilMock = Mockito.mock(ConnectorUtil.class);
         projectManager.setConnectorUtil(connectorUtilMock);
         projectManager.init();
     }
 
     private AuthenticationManager makeAuthenticationManager() {
         AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
         BundleAuthenticationToken bundleAuthenticationToken =
             new BundleAuthenticationToken("", "", new ArrayList<GrantedAuthority>());
         when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(bundleAuthenticationToken);
         return authenticationManager;
     }
 
     private void addTestData() throws Exception {
         Project project = new Project("test");
         project.setState(State.OK);
         projectManager.createProject(project);
     }
 
     @Test
     public void createProject_shouldWork() throws Exception {
         Project project = new Project("foo");
         projectManager.createProject(project);
         List<Project> allProjects = projectManager.getAllProjects();
         assertThat(allProjects, hasItem(project));
     }
 
     @Test(expected = ProjectAlreadyExistsException.class)
     public void createProjectTwice_shouldFail() throws Exception {
         addTestData();
         Project project = new Project("test");
         projectManager.createProject(project);
     }
 
     @Test
     public void getAllProjects_shouldWork() throws Exception {
         addTestData();
         List<Project> allProjects = projectManager.getAllProjects();
         assertThat(allProjects.size(), is(1));
         assertThat(allProjects.get(0).getId(), is("test"));
     }
 
     @Test
     public void getProject_souldWork() throws Exception {
         addTestData();
         Project project = projectManager.getProject("test");
         assertThat(project.getId(), is("test"));
         assertThat(project.getState(), is(State.OK));
     }
 
     @Test(expected = NoSuchProjectException.class)
     public void getProject_souldFail() throws NoSuchProjectException {
         projectManager.getProject("test");
     }
 
     @Test
     public void updateProject_shouldWork() throws Exception {
         addTestData();
         Project project = new Project("test");
         project.setState(State.OK);
         projectManager.updateProject(project);
         List<Project> allProjects = projectManager.getAllProjects();
         assertThat(allProjects, hasItem(project));
     }
 
     @Test(expected = NoSuchProjectException.class)
     public void updateProject_souldFail() throws NoSuchProjectException {
         projectManager.updateProject(new Project("test"));
     }
 
     @Test
     public void deleteProject_souldWork() throws Exception {
         addTestData();
         projectManager.deleteProject("test");
         List<Project> allProjects = projectManager.getAllProjects();
         assertThat(allProjects.isEmpty(), is(true));
     }
 
     @Test(expected = NoSuchProjectException.class)
     public void deleteProject_souldFail() throws NoSuchProjectException {
         projectManager.deleteProject("test");
     }
 
     @Test
     public void updateCurrentContextProjectState_shouldWork() throws Exception {
         addTestData();
         projectManager.updateCurrentContextProjectState(State.FAILURE);
         Project project = projectManager.getProject("test");
         assertThat(project.getState(), is(State.FAILURE));
     }
 
     @Test(expected = NoSuchProjectException.class)
     public void updateCurrentContextProjectState_shouldFail() throws NoSuchProjectException {
         projectManager.updateCurrentContextProjectState(State.FAILURE);
     }
 
     @Test
     public void createProjectShouldStartPoller() throws Exception {
         when(scmMock.update()).thenReturn(null);
         Project project = new Project("test2");
         project.setNotificationRecipient("test@test.com");
 
         projectManager.createProject(project);
         Thread.sleep(200);
         assertThat(scheduler.isProjectBuilding("test2"), is(false));
         assertThat(scheduler.isProjectPolling("test2"), is(true));
         verify(scmMock).update();
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testPollerShouldTriggerBuild() throws Exception {
         List<CommitRef> fakeCommits = new LinkedList<CommitRef>();
         fakeCommits.add(Mockito.mock(CommitRef.class));
 
         Project project = new Project("test2");
         project.setNotificationRecipient("test@test.com");
         when(scmMock.update()).thenReturn(fakeCommits);
         when(workflowService.startFlow(eq("ci"), any(Map.class))).thenReturn(1L);
         Answer<?> answer = new Answer<Void>() {
             @Override
             public Void answer(InvocationOnMock invocation) throws Throwable {
                 synchronized (this) {
                     this.notify();
                 }
                Thread.sleep(200);
                 return null;
             }
         };
         doAnswer(answer).when(workflowService).waitForFlowToFinish(eq(1L), anyLong());
         projectManager.createProject(project);
         synchronized (answer) {
             answer.wait();
         }
         assertThat(scheduler.isProjectBuilding("test2"), is(true));
         assertThat(scheduler.isProjectPolling("test2"), is(false));
         verify(scmMock).update();

     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void build_shouldSuspendPoller() throws Exception {
         List<CommitRef> fakeCommits = new LinkedList<CommitRef>();
         fakeCommits.add(Mockito.mock(CommitRef.class));
 
         final Semaphore eventSync = new Semaphore(0);
         when(workflowService.startFlow(eq("ci"), any(Map.class))).thenReturn(1L);
         doAnswer(new Answer<Void>() {
             @Override
             public Void answer(InvocationOnMock invocation) throws Throwable {
                 eventSync.acquire();
                 return null;
             }
         }).when(workflowService).waitForFlowToFinish(eq(1L), anyLong());
         when(scmMock.update()).thenReturn(fakeCommits, (List<CommitRef>[]) null);
 
         scheduler.setPollInterval(100L);
 
         Project project = new Project("test");
         project.setState(State.OK);
         projectManager.createProject(project);
         Thread.sleep(200);
         assertThat(scheduler.isProjectBuilding("test"), is(true));
         Thread.sleep(200);
 
         verify(scmMock).update();
 
         eventSync.release();
 
         Thread.sleep(200);
 
         assertThat(scheduler.isProjectBuilding("test"), is(false));
         assertThat(scheduler.isProjectPolling("test"), is(true));
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void buildManually_shouldSuspendPoller() throws Exception {
         final Semaphore eventSync = new Semaphore(0);
         when(workflowService.startFlow(eq("ci"), any(Map.class))).thenReturn(1L);
         doAnswer(new Answer<Void>() {
             @Override
             public Void answer(InvocationOnMock invocation) throws Throwable {
                 eventSync.acquire();
                 return null;
             }
         }).when(workflowService).waitForFlowToFinish(eq(1L), anyLong());
         when(scmMock.update()).thenReturn(null);
 
         Project project = new Project("test");
         project.setState(State.OK);
         projectManager.createProject(project);
         Thread.sleep(200);
         scheduler.scheduleProjectForBuild("test", new TestBuild());
         assertThat(scheduler.isProjectBuilding("test"), is(true));
         assertThat(scheduler.isProjectPolling("test"), is(false));
         Thread.sleep(200);
         verify(scmMock).update();
 
         eventSync.release();
         Thread.sleep(200);
 
         assertThat(scheduler.isProjectBuilding("test"), is(false));
         assertThat(scheduler.isProjectPolling("test"), is(true));
     }
 
     @Override
     protected void setBundleContext(BundleContext bundleContext) {
         serviceUtils = new DefaultOsgiUtilsService();
         serviceUtils.setBundleContext(bundleContext);
         registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
         this.bundleContext = bundleContext;
     }
 }
