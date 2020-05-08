 package test.webui.recipes.applications;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.webui.objects.LoginPage;
 import test.webui.objects.MainNavigation;
 import test.webui.objects.dashboard.DashboardTab;
 import test.webui.objects.dashboard.ServicesGrid;
 import test.webui.objects.dashboard.ServicesGrid.ApplicationServicesGrid;
 import test.webui.objects.dashboard.ServicesGrid.ApplicationsMenuPanel;
 import test.webui.objects.dashboard.ServicesGrid.Icon;
 import test.webui.objects.dashboard.ServicesGrid.InfrastructureServicesGrid;
 import test.webui.objects.services.PuTreeGrid;
 import test.webui.objects.services.ServicesTab;
 import test.webui.objects.topology.TopologyTab;
 import test.webui.objects.topology.applicationmap.ApplicationMap;
 import test.webui.objects.topology.applicationmap.ApplicationNode;
 import test.webui.objects.topology.applicationmap.Connector;
 import test.webui.objects.topology.healthpanel.HealthPanel;
 import test.webui.recipes.services.AbstractSeleniumServiceRecipeTest;
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 public class TravelTest extends AbstractSeleniumApplicationRecipeTest {
 
 	@Override
 	@BeforeMethod
 	public void install() throws IOException, InterruptedException {
 		setCurrentApplication("travel");
 		super.install();
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2)
 	public void travelApplicationTest() throws InterruptedException {
 
 		// get new login page
 		LoginPage loginPage = getLoginPage();
 
 		MainNavigation mainNav = loginPage.login();
 
 		DashboardTab dashboardTab = mainNav.switchToDashboard();
 		
 		final InfrastructureServicesGrid infrastructureServicesGrid = dashboardTab.getServicesGrid().getInfrastructureGrid();
 		
 		RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				return ((infrastructureServicesGrid.getESMInst().getCount() == 1) 
 						&& (infrastructureServicesGrid.getESMInst().getIcon().equals(Icon.OK)));
 			}
 		};
 		AssertUtils.repetitiveAssertTrue("No esm in showing in the dashboard", condition, waitingTime);
 
 
 		ServicesGrid servicesGrid = dashboardTab.getServicesGrid();
 
 		ApplicationsMenuPanel appMenu = servicesGrid.getApplicationsMenuPanel();
 
 		appMenu.selectApplication(AbstractSeleniumServiceRecipeTest.MANAGEMENT);
 
 		final ApplicationServicesGrid applicationServicesGrid = servicesGrid.getApplicationServicesGrid();
 
 		condition = new RepetitiveConditionProvider() {		
 			@Override
 			public boolean getCondition() {
 				return applicationServicesGrid.getWebModule().getCount() == 2;
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 
 		condition = new RepetitiveConditionProvider() {		
 			@Override
 			public boolean getCondition() {
 				return applicationServicesGrid.getWebModule().getIcon().equals(Icon.OK);
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 
 		appMenu.selectApplication("travel");
 
 		condition = new RepetitiveConditionProvider() {		
 			@Override
 			public boolean getCondition() {
 				return applicationServicesGrid.getWebServerModule().getCount() == 1;
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 
 		condition = new RepetitiveConditionProvider() {		
 			@Override
 			public boolean getCondition() {
 				return applicationServicesGrid.getWebServerModule().getIcon().equals(Icon.OK);
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 
 		condition = new RepetitiveConditionProvider() {		
 			@Override
 			public boolean getCondition() {
 				return applicationServicesGrid.getNoSqlDbModule().getCount() == 1;
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 
 		condition = new RepetitiveConditionProvider() {		
 			@Override
 			public boolean getCondition() {
 				return applicationServicesGrid.getNoSqlDbModule().getIcon().equals(Icon.OK);
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 
 		TopologyTab topologyTab = mainNav.switchToTopology();
 
 		final ApplicationMap appMap = topologyTab.getApplicationMap();
 
 		appMap.selectApplication(AbstractSeleniumServiceRecipeTest.MANAGEMENT);
 
 		ApplicationNode restful = appMap.getApplicationNode("rest");
 
 		assertTrue(restful != null);
 		assertTrue(restful.getStatus().equals(DeploymentStatus.INTACT));
 
 		ApplicationNode webui = appMap.getApplicationNode("webui");
 
 		assertTrue(webui != null);
 		assertTrue(webui.getStatus().equals(DeploymentStatus.INTACT));
 
 		appMap.selectApplication("travel");
 
 		ApplicationNode cassandra = appMap.getApplicationNode("travel.cassandra");
 
 		assertTrue(cassandra != null);
 		assertTrue(cassandra.getStatus().equals(DeploymentStatus.INTACT));	
 
 		ApplicationNode tomcat = appMap.getApplicationNode("travel.tomcat");
 
 		assertTrue(tomcat != null);
 		assertTrue(tomcat.getStatus().equals(DeploymentStatus.INTACT));		
 
 		List<Connector> connectors = tomcat.getConnectors();
		assertTrue(connectors.size() == 2);
 		List<Connector> targets = tomcat.getTargets();
 		assertTrue(targets.size() == 1);
		assertTrue(targets.get(0).getTarget().equals(cassandra));
 
 		cassandra.select();
 
 		HealthPanel healthPanel = topologyTab.getTopologySubPanel().switchToHealthPanel();
 
 		assertTrue(healthPanel.getMetric("Process Cpu Usage") != null);
 		assertTrue(healthPanel.getMetric("Total Process Virtual Memory") != null);
 		assertTrue(healthPanel.getMetric("Compaction Manager Completed Tasks") != null);
 		assertTrue(healthPanel.getMetric("Compaction Manager Pending Tasks") != null);
 		assertTrue(healthPanel.getMetric("Commit Log Active Tasks") != null);
 
 		tomcat.select();
 
 		assertTrue(healthPanel.getMetric("Process Cpu Usage") != null);
 		assertTrue(healthPanel.getMetric("Total Process Virtual Memory") != null);
 		assertTrue(healthPanel.getMetric("Num Of Active Threads") != null);
 		assertTrue(healthPanel.getMetric("Current Http Threads Busy") != null);
 		assertTrue(healthPanel.getMetric("Backlog") != null);
 		assertTrue(healthPanel.getMetric("Active Sessions") != null);
 
 		ServicesTab servicesTab = mainNav.switchToServices();
 
 		PuTreeGrid puTreeGrid = servicesTab.getPuTreeGrid();
 
 		assertTrue(puTreeGrid.getProcessingUnit("webui") != null);
 		assertTrue(puTreeGrid.getProcessingUnit("rest") != null);
 		assertTrue(puTreeGrid.getProcessingUnit("travel.cassandra") != null);
 		assertTrue(puTreeGrid.getProcessingUnit("travel.tomcat") != null);
 
 	}
 
 }
