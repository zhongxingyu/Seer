 package test.webui.objects.topology;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Assert;
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.remote.RemoteWebElement;
 import org.openspaces.admin.pu.DeploymentStatus;
 
 import test.webui.interfaces.RenderedWebUIElement;
 import test.webui.resources.WebConstants;
 
 import com.thoughtworks.selenium.Selenium;
 
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 public class ApplicationMap {
 	
 	Selenium selenium;
 	WebDriver driver;
 
 	public static final String CONN_STATUS_OK = "conn-status-ok";
 	public static final String CONN_STATUS_WARN = "conn-status-warn";
 	public static final String CONN_STATUS_CRITICAL = "conn-status-critical";
 	public static final String CONN_STATUS_EMPTY = "conn-status-empty";
 
 	public ApplicationMap(Selenium selenium, WebDriver driver) {
 		this.selenium = selenium;
 		this.driver = driver;
 	}
 
 	public static ApplicationMap getInstance(Selenium selenium, WebDriver driver) {
 		return new ApplicationMap(selenium, driver);
 	}
 	
 	public enum DumpType {
 		JVM_THREAD,NETWORK,LOG,PU,JVM_HEAP,
 	}
 	
 	public enum ServiceTypes {
 		UNDEFINED,
 		LOAD_BALANCER,
 		WEB_SERVER,
 		SECURITY_SERVER,
 		APP_SERVER,
 		ESB_SERVER,
 		MESSAGE_BUS,
 		DATABASE,
 		NOSQL_DB;
 	}
 	
 	public class ApplicationNode implements RenderedWebUIElement {
 		
 		private String name;
 		
 		public ApplicationNode(String name) {
 			
 			this.name = getNameFromUI(name);
 		}
 		
 		public String getName() {
 			return name;
 		}
 		
 		private String getNameFromUI(String name) {
 
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			try {
 				String label = (String)js.executeScript("return this.dGraphAppMap.nodes[" + '"' 
 						+ name + '"' + "].id");
 				return label;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 		}
 		
 		
 		public List<Connector> getTargets() {
 			
 			List<Connector> connectors = new ArrayList<Connector>();
 			
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			
 			try {
 				Long length = (Long)js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges.length");
 				for (int i = 0 ; i < length ; i++) {
 					String sourceNodeName = (String) js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges[" + i + "].source.id");
 					if (sourceNodeName.equals(name)) {
 						String targetNodeName = (String) js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges[" + i + "].target.id");
 						String status = (String) js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges[" + i + "].style.status");
 						connectors.add(new Connector(new ApplicationNode(sourceNodeName), new ApplicationNode(targetNodeName), status));
 					}
 				}
 				return connectors;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 		}
 		
 		public List<Connector> getTargeted() {
 			
 			List<Connector> connectors = new ArrayList<Connector>();
 			
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			
 			try {
 				Long length = (Long)js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges.length");
 				for (int i = 0 ; i < length ; i++) {
 					String sourceNodeName = (String) js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges[" + i + "].source.id");
 					String targetNodeName = (String) js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges[" + i + "].target.id");
 					if (targetNodeName.equals(name)) {
 						String status = (String) js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges[" + i + "].style.status");
 						connectors.add(new Connector(new ApplicationNode(sourceNodeName), new ApplicationNode(targetNodeName), status));
 					}
 				}
 				return connectors;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 		}
 		
 		public List<Connector> getConnectors() {
 			
 			List<Connector> connectors = new ArrayList<Connector>();
 			
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			
 			try {
 				Long length = (Long)js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges.length");
 				for (int i = 0 ; i < length ; i++) {
 					String sourceNodeName = (String) js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges[" + i + "].source.id");
 					String targetNodeName = (String) js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges[" + i + "].target.id");
 					String status = (String) js.executeScript("return this.dGraphAppMap.nodes[" + '"' + name + '"' + "].edges[" + i + "].style.status");
 					connectors.add(new Connector(new ApplicationNode(sourceNodeName), new ApplicationNode(targetNodeName), status));	
 				}
 				return connectors;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 		}
 
 		@SuppressWarnings("unchecked")
 		public List<String> getComponents() {
 			
 			List<String> comps = new ArrayList<String>();
 			
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			try {
 				comps = (List<String>)js.executeScript("return this.dGraphAppMap.nodes[" + '"' 
 						+ name + '"' + "].components");
 				return comps;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 			
 		}
 
 		public Long getxPosition() {
 			
 			Long xPosition = null;
 
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			try {
 				xPosition = (Long)js.executeScript("return this.dGraphAppMap.nodes[" + '"' 
 						+ name + '"' + "].layoutPosX");
 				return xPosition;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 		}
 		
 		public Long getyPosition() {
 			
 			Long xPosition = null;
 
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			try {
 				xPosition = (Long)js.executeScript("return this.dGraphAppMap.nodes[" + '"' 
 						+ name + '"' + "].layoutPosY");
 				return xPosition;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 		}
 
 		public String getNodeColor() {
 			
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			try {
 				String color = (String)js.executeScript("return this.dGraphAppMap.nodes[" + '"' 
 						+ name + '"' + "].nodeColor");
 				return color;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 		}
 		
 		public String getNodeType() {
 			
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			try {
 				String type = (String)js.executeScript("return this.dGraphAppMap.nodes[" + '"' 
 						+ name + '"' + "].nodeType");
 				return type;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 		}
 		
 		public String getPuType() {
 			
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			try {
 				String type = (String)js.executeScript("return this.dGraphAppMap.nodes[" + '"' 
 						+ name + '"' + "].puType");
 				return type;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 		}
 
 		public DeploymentStatus getStatus() {
 
 			String stat;
 			
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			try {
 				stat = (String)js.executeScript("return this.dGraphAppMap.nodes[" + '"' 
 						+ name + '"' + "].status");
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 			if (stat.equals(WebConstants.ID.nodeStatusOk)) return DeploymentStatus.INTACT;
 			if (stat.equals(WebConstants.ID.nodeStatusWarning)) return DeploymentStatus.COMPROMISED;
 			if (stat.equals(WebConstants.ID.nodeStatusBroken)) return DeploymentStatus.BROKEN;
 			else return DeploymentStatus.SCHEDULED;
 		}
 
 		public Long getPlannedInstances() {
 
 			Long actualInstances = null;
 
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			try {
 				actualInstances = (Long)js.executeScript("return this.dGraphAppMap.nodes[" + '"' 
 						+ name + '"' + "].plannedInstances");
 				return actualInstances;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 		}
 
 		public Long getActualInstances() {
 
 			Long actualInstances = null;
 
 			JavascriptExecutor js = (JavascriptExecutor) driver;
 			try {
 				actualInstances = (Long)js.executeScript("return this.dGraphAppMap.nodes[" + '"' 
 						+ name + '"' + "].actualInstances");
 				return actualInstances;
 			}
 			catch (NoSuchElementException e) {
 				return null;
 			}
 			catch (WebDriverException e) {
 				return null;
 			}
 		}
 		
 		public boolean verifyNodeVisible() {
 			return selenium.isTextPresent(this.getName());
 		}
 		
 		public void select() {
 			selenium.click(WebConstants.ID.nodePath + this.name);
 		}
 		
 		public void clickOnActions() {
 			WebElement actionsButton = driver.findElement(By.id(WebConstants.ID.getActionToolBoxId(this.name)));
 			actionsButton.click();
 		}
 		
 		public void clickOnInfo() {
 			WebElement infoButton = driver.findElement(By.id(WebConstants.ID.getInfoToolBoxId(this.name)));
 			infoButton.click();
 		}
 		
 		public void assertActionMenuVisible() {
 			WebElement menu = driver.findElement(By.className("x-menu-list"));
 			AssertUtils.assertTrue(menu.isDisplayed());
 			List<WebElement> items = menu.findElements(By.className("x-menu-list-item"));
 			AssertUtils.assertTrue(items.size() == 2);
 		}
 		
 		public void assertActionMenuNotVisible() {
 			try {
 				@SuppressWarnings("unused")
 				WebElement menu = driver.findElement(By.className("x-menu-list"));
 				Assert.fail("Menu list item is still visible");
 			}
 			catch (WebDriverException e) {
 				return;
 			}
 		}
 		
 		public void undeploy() {
 			clickOnActions();
 			assertActionMenuVisible();
 			selenium.click(WebConstants.Xpath.pathToUndeployNode);
 			selenium.click(WebConstants.Xpath.acceptAlert);
 			assertActionMenuNotVisible();
 		}
 		
 		/**
 		 * generates dump of a processing unit from a pu node in the application map
 		 * this currently can only be executed fully on chrome browser
 		 * since it downloads files without opening a file dialog
 		 * @param reason - reason for requesting dump
 		 * @throws InterruptedException
 		 */
 		public void generateDump(String reason) throws InterruptedException {
 			clickOnActions();
 			assertActionMenuVisible();
 			selenium.click(WebConstants.Xpath.pathToGenerateNodeDump);
 			WebElement dumpWindow = driver.findElement(By.className("servicesDumpWindow"));
 			WebElement reasonInput = dumpWindow.findElement(By.tagName("input"));
 			reasonInput.sendKeys(reason);
 			selenium.click(WebConstants.Xpath.generateDumpButton);
 			Thread.sleep(5000);
 			selenium.click(WebConstants.Xpath.closeWindow);
 			assertActionMenuNotVisible();
 		}
 
 		public void showInfo() {
 			clickOnInfo();
 		}
 
 		public boolean isDisplayed() {
 			
 			RemoteWebElement node = (RemoteWebElement) driver.findElement(By.id(WebConstants.ID.nodePath + this.name));
 			return node.isDisplayed();
 		}
 		
 		public void restart() {
 			//TODO implement
 		}
 		
 		public class Connector {
 			
 			private ApplicationNode source;
 			private ApplicationNode target;
 			private String status;
 			
 			public Connector(ApplicationNode source, ApplicationNode target, String status) {
 				this.source = source;
 				this.target = target;
 				this.status = status;
 			}
 			
 			public ApplicationNode getSource() {
 				return source;
 			}
 			public ApplicationNode getTarget() {
 				return target;
 			}
 			
 			public String getStatus() {
 				return status;
 			}
 			
 		}		
 	}
 	
 	public void selectApplication(final String applicationName) {
 		
 		RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {			
 			public boolean getCondition() {
 				WebElement arrowDown = driver.findElement(By.id(WebConstants.ID.topologyCombobox)).findElement(By.className("icon"));
 				arrowDown.click();
				List<WebElement> visibleApps = driver.findElement(By.id(WebConstants.ID.topologyCombobox)).findElements(By.xpath("//li[@class='visible']"));
				WebElement activeApp = driver.findElement(By.id(WebConstants.ID.topologyCombobox)).findElement(By.xpath("//li[@class='visible active']"));
				List<WebElement> allApps = visibleApps;
				allApps.add(activeApp);
 				WebElement app = null;
 				for (WebElement e : allApps) {
 					if (e.getText().equals(applicationName)) app = e;
 				}
 				if ((app != null) && app.isDisplayed()) {
 					app.click();
 					return true;
 				}
 				else {
 					return false;
 				}
 			}
 		};
 
 		AssertUtils.repetitiveAssertTrue("Application is not present in the applications menu panel", condition,10000);
 	}
 
 	public ApplicationNode getApplicationNode(String name) {
 		ApplicationNode appNode = new ApplicationNode(name);
 		if (appNode.getName() != null) {
 			return appNode;
 		}
 		return null;
 	}
 }
