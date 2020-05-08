 package org.oneandone.qxwebdriver;
 
 import java.util.List;
 
 import org.oneandone.qxwebdriver.resources.javascript.JavaScript;
 import org.openqa.selenium.InvalidSelectorException;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.SearchContext;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.remote.RemoteWebElement;
 
 public class By extends org.openqa.selenium.By {
 	
 	public WebElement findElement(SearchContext context) {
 		return null;
 	}
 	
 	public List<WebElement> findElements(SearchContext context) {
 		return null;
 	}
 
 	public static By qxh(final String locator) {
 		if (locator == null) {
 			throw new IllegalArgumentException(
 					"Can't find elements without a locator string.");
 		}
 		
 		return new ByQxh(locator, true);
 	}
 	
 	public static By qxh(final String locator, final Boolean onlyVisible) {
 		if (locator == null) {
 			throw new IllegalArgumentException(
 					"Can't find elements without a locator string.");
 		}
 		return new ByQxh(locator, onlyVisible);
 	}
 	
 	public static class ByQxh extends By {
 		
 		private final String locator;
 		private Boolean onlyVisible;
 
 		public ByQxh(String locator, Boolean onlyVisible) {
 			this.locator = locator;
 			this.onlyVisible = onlyVisible;
 		}
 
 		public List<WebElement> findElements(SearchContext context) {
 			//TODO
 			
 			return null;
 		}
 		
 		public WebElement findElement(SearchContext context) {
 			JavascriptExecutor jsExecutor;
 			
 			RemoteWebElement contextElement = null;
 			
 			if (context instanceof RemoteWebElement) {
 				contextElement = (RemoteWebElement) context;
 				jsExecutor = (JavascriptExecutor) contextElement.getWrappedDriver();
 			}
 			else {
 				 jsExecutor = (JavascriptExecutor) context;
 			}
 			
 			String script  = JavaScript.INSTANCE.getValue("qxh");
 			
 			try {
 				Object result;
 				if (contextElement == null) {
 					// OperaDriver.executeScript won't accept null as an argument
 					result = jsExecutor.executeScript(script, locator, onlyVisible);
 				} else {
 					try {
 						result = jsExecutor.executeScript(script, locator, onlyVisible, (WebElement) contextElement);
 					} catch(com.opera.core.systems.scope.exceptions.ScopeException e) {
 						// OperaDriver will sometimes throw a ScopeException if executeScript is called
 						// with an OperaWebElement as argument
 						return null;
 					}
 					
 				}
 				return (WebElement) result;
 				
 			} catch(org.openqa.selenium.WebDriverException e) {
 				String msg = e.getMessage();
				if (msg.contains("Error resolving qxh path")) {
 					return null;
 				}
 				else if (msg.contains("Illegal path step")) {
 					String reason = "Invalid qxh selector " + locator.toString();
 					throw new InvalidSelectorException(reason, e);
 				}
 				else {
 					String reason = "Error while processing selector " + locator.toString();
 					throw new org.openqa.selenium.WebDriverException(reason, e);
 				}
 			}
 		}
 
 		public String toString() {
 			return "By.qxh: " + locator;
 		}
 	}
 }
