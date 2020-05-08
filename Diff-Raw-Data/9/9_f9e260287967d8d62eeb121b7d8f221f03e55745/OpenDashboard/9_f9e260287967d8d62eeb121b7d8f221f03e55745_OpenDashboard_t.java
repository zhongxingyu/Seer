 package com.operativus.senacrs.audit.graph.edges.webdriver;
 
 import org.openqa.selenium.WebDriver;
 
import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNode;
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNodeTypeEnum;
 
 public final class OpenDashboard
 		extends AbstractWebDriverEdge {
 
 	private final String baseUrl;
 
 	public OpenDashboard(final WebDriver driver, final String baseUrl) {
 
 		super(driver, WebDriverNodeTypeEnum.START);
 		this.baseUrl = baseUrl;
		if (baseUrl == null) {
			throw RuntimeExceptionFactory.getNullArgumentException("baseUrl");
		}
		if (baseUrl.trim().isEmpty()) {
			throw RuntimeExceptionFactory.getEmpyStringArgumentException("baseUrl");
		}
 	}
 
 	@Override
 	protected void internTraverse(final WebDriverNode source) {
 
 		this.getWebDriver().get(this.baseUrl);
 	}
 }
