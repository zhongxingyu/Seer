 package test.webui.recipes.services;
 
 import java.io.IOException;
 
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.gigaspaces.webuitf.LoginPage;
 import com.gigaspaces.webuitf.MainNavigation;
 import com.gigaspaces.webuitf.dashboard.DashboardTab;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.ApplicationServicesGrid;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.ApplicationsMenuPanel;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.Icon;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.InfrastructureServicesGrid;
 import com.gigaspaces.webuitf.services.PuTreeGrid;
 import com.gigaspaces.webuitf.services.ServicesTab;
 import com.gigaspaces.webuitf.topology.TopologyTab;
 import com.gigaspaces.webuitf.topology.applicationmap.ApplicationMap;
 import com.gigaspaces.webuitf.topology.applicationmap.ApplicationNode;
 import com.gigaspaces.webuitf.topology.healthpanel.HealthPanel;
 
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 public class CassandraServiceTest extends AbstractSeleniumServiceRecipeTest  {
 		
 	@Override
 	@BeforeMethod
 	public void install() throws IOException, InterruptedException {
 		setCurrentRecipe("cassandra");
 		super.install();
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT)
 	public void cassandraRecipeTest() throws InterruptedException, IOException {
 
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
 
 		appMenu.selectApplication(MANAGEMENT_APPLICATION_NAME);
 
 		final ApplicationServicesGrid applicationServicesGrid = servicesGrid.getApplicationServicesGrid();
 
 		condition = new RepetitiveConditionProvider() {		
 			@Override
 			public boolean getCondition() {
 				return applicationServicesGrid.getWebModule().getCount() == 2;
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 
 		appMenu.selectApplication(DEFAULT_APPLICATION_NAME);
 
 		condition = new RepetitiveConditionProvider() {		
 			@Override
 			public boolean getCondition() {
 				return applicationServicesGrid.getNoSqlDbModule().getCount() == 1;
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 
 		TopologyTab topologyTab = mainNav.switchToTopology();
 
 		final ApplicationMap appMap = topologyTab.getApplicationMap();
 
 		topologyTab.selectApplication(MANAGEMENT_APPLICATION_NAME);
 		
 
 		ApplicationNode restful = appMap.getApplicationNode("rest");
 
 		assertTrue(restful != null);
 		assertTrue(restful.getStatus().equals(DeploymentStatus.INTACT));
 
 		ApplicationNode webui = appMap.getApplicationNode("webui");
 
 		assertTrue(webui != null);
 		assertTrue(webui.getStatus().equals(DeploymentStatus.INTACT));
 
 		topologyTab.selectApplication(DEFAULT_APPLICATION_NAME);
 
 		takeScreenShot(this.getClass(),"cassandraRecipeTest", "topology");
 
 		final ApplicationNode simple = appMap.getApplicationNode(DEFAULT_CASSANDRA_FULL_SERVICE_NAME);
 
 		assertTrue(simple != null);
 		condition = new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				return simple.getStatus().equals(DeploymentStatus.INTACT);
 			}
 		};
 		repetitiveAssertTrueWithScreenshot(
 				"cassandra service is displayed as " + simple.getStatus() + 
 					"even though it is installed", condition, this.getClass(), "cassandraRecipeTest", "cassandra-service");
 
 		HealthPanel healthPanel = topologyTab.getTopologySubPanel().switchToHealthPanel();
 
 		takeScreenShot(this.getClass(),"cassandraRecipeTest", "topology-healthpanel");
 		
		assertTrue("Process Cpu Usage " + METRICS_ASSERTION_SUFFIX, healthPanel.getMetric("Process Cpu Usage") != null);
		assertTrue("Total Process Virtual Memory " + METRICS_ASSERTION_SUFFIX, healthPanel.getMetric("Total Process Virtual Memory") != null);
		assertTrue("Compaction Manager Completed Tasks " + METRICS_ASSERTION_SUFFIX , healthPanel.getMetric("Compaction Manager Completed Tasks") != null);
		assertTrue("Compaction Manager Pending Tasks " + METRICS_ASSERTION_SUFFIX, healthPanel.getMetric("Compaction Manager Pending Tasks") != null);
		assertTrue("Commit Log Active Tasks" + METRICS_ASSERTION_SUFFIX , healthPanel.getMetric("Commit Log Active Tasks") != null);
 
 		ServicesTab servicesTab = mainNav.switchToServices();
 
 		PuTreeGrid puTreeGrid = servicesTab.getPuTreeGrid();
 
 		assertTrue(puTreeGrid.getProcessingUnit("webui") != null);
 		assertTrue(puTreeGrid.getProcessingUnit("rest") != null);
 		assertTrue(puTreeGrid.getProcessingUnit(DEFAULT_CASSANDRA_FULL_SERVICE_NAME) != null);
 		uninstallService("cassandra", true);
 
 	}
 
 }
