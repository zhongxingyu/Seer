 package org.oneandone.qxwebdriver.widget;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.oneandone.qxwebdriver.QxWebDriver;
 import org.oneandone.qxwebdriver.resources.javascript.JavaScript;
 import org.openqa.selenium.By;
 import org.openqa.selenium.Dimension;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.Point;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 /**
  * Represents a qooxdoo Desktop widget. Suppports all {@link org.openqa.selenium.WebElement}
  * methods, although not all of them will be useful in a qooxdoo context.
  * 
  * For more advanced interactions, see the interfaces in this namespace.
  * @see Scrollable
  * @see Selectable
  *
  */
 public class Widget implements WebElement {
 
 	public Widget(WebElement element, QxWebDriver webDriver) {
 		driver = webDriver;
 		
 		jsExecutor = (JavascriptExecutor) driver.driver;
 		
 		contentElement = (WebElement) jsExecutor.executeScript(JavaScript.INSTANCE.getValue("getContentElement"),
 				element);
 		
 		qxHash = (String) jsExecutor.executeScript(JavaScript.INSTANCE.getValue("getObjectHash"), 
 				element);
 		
 		classname = (String) jsExecutor.executeScript("return qx.ui.core.Widget.getWidgetByElement(arguments[0]).classname", 
 				contentElement);
 	}
 	
 	/**
 	 * This widget's qooxdoo object registry ID
 	 */
 	public String qxHash;
 	/**
 	 * This widget's qooxdoo class name
 	 */
 	public String classname;
 	/**
 	 * The WebElement representing this widget's content element
 	 */
 	public WebElement contentElement;
 
 	protected QxWebDriver driver;
 
 	protected JavascriptExecutor jsExecutor;
 	
 	public void click() {
 		contentElement.click();
 	}
 	
 	public void sendKeys(CharSequence keysToSend) {
 		contentElement.sendKeys(keysToSend);
 	}
 	
 	/**
 	 * Waits until a child control has been rendered.
 	 */
 	public Widget waitForChildControl(String childControlId, Integer timeout) {
 		WebDriverWait wait = new WebDriverWait(driver, timeout, 250);
 		return wait.until(childControlIsVisible(childControlId));
 	}
 	
 	/**
 	 * A conditon that waits until a child control has been rendered, then 
 	 * returns it.
 	 */
 	public ExpectedCondition<Widget> childControlIsVisible(final String childControlId) {
 		return new ExpectedCondition<Widget>() {
 			@Override
 			public Widget apply(WebDriver webDriver) {
 				return getChildControl(childControlId);
 			}
 
 			@Override
 			public String toString() {
 				return "Child control is visible.";
 			}
 		};
 	}
 	
 	/**
 	 * Returns a {@link Widget} representing a child control of this widget.
 	 */
 	public Widget getChildControl(String childControlId) {
 		Object result = jsExecutor.executeScript(JavaScript.INSTANCE.getValue("getChildControl"),
 				contentElement, childControlId);
 		WebElement element = (WebElement) result;
 		return driver.getWidgetForElement(element);
 	}
 	
 	/**
 	 * Returns the value of a qooxdoo property on this widget, serialized in JSON
 	 * format.
 	 * <strong>NOTE:</strong> Never use this for property values that are instances
 	 * of qx.core.Object. Circular references in qooxoo's OO system will lead to
 	 * JavaScript errors.
 	 */
 	public String getPropertyValueAsJson(String propertyName) {
 		Object result = jsExecutor.executeScript(JavaScript.INSTANCE.getValue("getPropertyValueAsJson"),
 				contentElement, propertyName);
 		return (String) result;
 	}
 	
 	/**
 	 * Returns the value of a qooxdoo property on this widget. See the {@link org.openqa.selenium.JavascriptExecutor}
 	 * documentation for details on how JavaScript types are represented.
 	 * <strong>NOTE:</strong> Never use this for property values that are instances
 	 * of qx.core.Object. Circular references in qooxoo's OO system will lead to
 	 * JavaScript errors.
 	 */
 	public Object getPropertyValue(String propertyName) {
 		Object result = jsExecutor.executeScript(JavaScript.INSTANCE.getValue("getPropertyValue"),
 				contentElement, propertyName);
 		return result;
 	}
 	
 	private WebElement getElementFromProperty(String propertyName) {
 		Object result = jsExecutor.executeScript(JavaScript.INSTANCE.getValue("getElementFromProperty"),
 				contentElement, propertyName);
 		return (WebElement) result;
 	}
 	
 	/**
 	 * Returns a {@link Widget} representing the value of a widget property,
 	 * e.g. <a href="http://demo.qooxdoo.org/current/apiviewer/#qx.ui.form.MenuButton~menu!property">the 
 	 * MenuButton's menu property</a>
 	 */
 	public Widget getWidgetFromProperty(String propertyName) {
 		return driver.getWidgetForElement(getElementFromProperty(propertyName));
 	}
 	
 	private List<WebElement> getChildrenElements() {
 		Object result = jsExecutor.executeScript(JavaScript.INSTANCE.getValue("getChildrenElements"), 
 				contentElement);
 		List<WebElement> children = (List<WebElement>) result;
 		return children;
 	}
 	
 	/**
 	 * Returns a list of {@link Widget} objects representing this widget's children
 	 * as defined using <a href="http://demo.qooxdoo.org/current/apiviewer/#qx.ui.core.MChildrenHandling~add!method_public">parent.add(child);</a> in the application code.
 	 */
 	public List<Widget> getChildren() {
 		List<WebElement> childrenElements = getChildrenElements();
 		Iterator<WebElement> iter = childrenElements.iterator();
 		List<Widget> children = new ArrayList<Widget>();
 		
 		while(iter.hasNext()) {
 			WebElement child = iter.next();
 			children.add(driver.getWidgetForElement(child));
 		}
 		
 		return children;
 	}
 	
 	/**
 	 * A condition that checks if an element is rendered.
 	 */
 	public ExpectedCondition<WebElement> isRendered(final WebElement contentElement, final By by) {
 		return new ExpectedCondition<WebElement>() {
 			@Override
 			public WebElement apply(WebDriver driver) {
 				return contentElement.findElement(by);
 			}
 
 			@Override
 			public String toString() {
 				return "element is rendered.";
 			}
 		};
 	}
 	
 	/**
 	 * Finds an element that is a child of this widget's content element.
 	 */
 	public WebElement findElement(org.openqa.selenium.By by) {
 		WebDriverWait wait = new WebDriverWait(driver, 5);
 		return wait.until(isRendered(contentElement, by));
 	}
 	
 	/**
 	 * Finds a widget relative to the current one by traversing the qooxdoo
 	 * widget hierarchy.
 	 */
 	public Widget findWidget(org.openqa.selenium.By by) {
 		WebElement element = findElement(by);
 		return driver.getWidgetForElement(element);
 	}
 	
 	public String toString() {
 		return "QxWidget " + classname +  "[" + qxHash + "]";
 	}
 
 	/**
 	 * Not implemented for qooxdoo widgets.
 	 */
 	public void submit() {
 		throw new RuntimeException("Not implemented for qooxdoo widgets.");
 	}
 
 	@Override
 	public void sendKeys(CharSequence... keysToSend) {
 		contentElement.sendKeys(keysToSend);
 		
 	}
 
 	@Override
 	public void clear() {
 		contentElement.clear();
 	}
 
 	@Override
 	public String getTagName() {
 		return contentElement.getTagName();
 	}
 
 	@Override
 	public String getAttribute(String name) {
 		return contentElement.getAttribute(name);
 	}
 
 	@Override
 	public boolean isSelected() {
 		try {
 			return (Boolean) getPropertyValue("selected");
 		} catch(org.openqa.selenium.WebDriverException e) {
 			// No such property: selected exception thrown by the qx property
 			// system
 			return false;
 		}
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return contentElement.isEnabled();
 	}
 
 	@Override
 	public String getText() {
 		return contentElement.getText();
 	}
 
 	@Override
 	public List<WebElement> findElements(By by) {
 		return contentElement.findElements(by);
 	}
 
 	/**
 	 * Determines if the widget is visible by querying the qooxdoo property 
 	 * <a href="http://demo.qooxdoo.org/current/apiviewer/#qx.ui.core.Widget~isSeeable!method_public">seeable</a>.
 	 */
 	public boolean isDisplayed() {
		return (Boolean) getPropertyValue("seeable");
 	}
 
 	@Override
 	public Point getLocation() {
 		return contentElement.getLocation();
 	}
 
 	@Override
 	public Dimension getSize() {
 		return contentElement.getSize();
 	}
 
 	@Override
 	public String getCssValue(String propertyName) {
 		return contentElement.getCssValue(propertyName);
 	}
 
 }
