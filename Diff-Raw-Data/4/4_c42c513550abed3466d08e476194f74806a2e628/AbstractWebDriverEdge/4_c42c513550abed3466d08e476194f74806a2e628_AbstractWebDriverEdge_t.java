 package com.operativus.senacrs.audit.graph.edges.webdriver;
 
 import java.util.Arrays;
 
 import org.openqa.selenium.WebDriver;
 
 import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;
 import com.operativus.senacrs.audit.graph.nodes.Node;
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNode;
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNodeType;
 
 public abstract class AbstractWebDriverEdge
 		implements WebDriverEdge {
 
 	private final WebDriver driver;
 	private final WebDriverNodeType[] acceptedSources;
 
 	protected AbstractWebDriverEdge(final WebDriver driver, final WebDriverNodeType... acceptedSourceNodes) {
 
 		super();
 
 		if (driver == null) {
 			throw RuntimeExceptionFactory.getNullArgumentException("driver");
 		}
 		this.driver = driver;
 		if (acceptedSourceNodes != null) {
 			this.acceptedSources = Arrays.copyOf(acceptedSourceNodes, acceptedSourceNodes.length);
 		} else {
 			this.acceptedSources = new WebDriverNodeType[0];
 		}
 	}
 
 	@Override
 	public WebDriver getWebDriver() {
 
 		return this.driver;
 	}
 
 	@Override
 	public void traverse(final Node source) {
 
 		WebDriverNode castedNode = null;
 
 		if (source == null) {
 			throw RuntimeExceptionFactory.getNullArgumentException("source");
 		}
 		if (!(source instanceof WebDriverNode)) {
 			throw new IllegalSourceNodeClassException(source);
 		}
 		castedNode = (WebDriverNode) source;
 		this.checkSourceType(castedNode);
 		this.internTraverse(castedNode);
 	}
 
 	private void checkSourceType(final WebDriverNode source) {
 
 		boolean found = false;
 		WebDriverNodeType type = null;
 
 		type = source.getType();
 		for (WebDriverNodeType t : this.acceptedSources) {
 			if (t.equals(type)) {
 				found = true;
 				break;
 			}
 		}
 		if (!found) {
			throw new IllegalSourceNodeTypeException(type, this.acceptedSources);
 		}
 	}
 
 	protected abstract void internTraverse(final WebDriverNode source);
 }
