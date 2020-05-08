 package org.oneandone.qxwebdriver.ui.core.scroll;
 
 import java.util.concurrent.TimeUnit;
 
 import org.oneandone.qxwebdriver.QxWebDriver;
 import org.oneandone.qxwebdriver.By;
 import org.oneandone.qxwebdriver.resources.JavaScript;
 import org.oneandone.qxwebdriver.ui.Scrollable;
 import org.oneandone.qxwebdriver.ui.Widget;
 import org.openqa.selenium.TimeoutException;
 import org.openqa.selenium.WebElement;
 
 /**
  * Represents a <a href="http://demo.qooxdoo.org/current/apiviewer/#qx.ui.core.scroll.AbstractScrollArea">ScrollArea</a>
  * widget
  */
 public class AbstractScrollArea extends org.oneandone.qxwebdriver.ui.core.WidgetImpl implements Scrollable {
 
 	public AbstractScrollArea(WebElement element, QxWebDriver webDriver) {
 		super(element, webDriver);
 	}
 	
 	protected Widget getScrollbar(String direction) {
 		String childControlId = "scrollbar-" + direction;
 		try {
 			org.oneandone.qxwebdriver.ui.Widget scrollBar = waitForChildControl(childControlId, 2);
 			return scrollBar;
 		} catch(TimeoutException e) {
 			return null;
 		}
 	}
 	
 	public void scrollTo(String direction, Integer position) {
 		Widget scrollBar = getScrollbar(direction);
 		if (scrollBar == null) {
 			return;
 		}
 		jsExecutor.executeScript(JavaScript.INSTANCE.getValue("scrollTo"),
 				scrollBar.getContentElement(), position);
 	}
 	
 	public Long getScrollPosition(String direction) {
 		Widget scrollBar = getScrollbar(direction);
 		if (scrollBar == null) {
 			return new Long(0);
 		}
 		return getScrollPosition(scrollBar);
 	}
 	
 	protected Long getScrollPosition(Widget scrollBar) {
 		try {
 			String result = scrollBar.getPropertyValueAsJson("position");
 			return Long.parseLong(result);
 		} catch(com.opera.core.systems.scope.exceptions.ScopeException e) {
 			return null;
 		}
 	}
 	
 	protected Long getScrollStep(Widget scrollBar) {
 		String result = scrollBar.getPropertyValueAsJson("singleStep");
 		return Long.parseLong(result);
 	}
 	
 	public Long getScrollStep(String direction) {
 		Widget scrollBar = getScrollbar(direction);
 		if (scrollBar == null) {
 			return new Long(0);
 		}
 		return getScrollStep(scrollBar);
 	}
 	
 	public Long getMaximum(String direction) {
 		Widget scrollBar = getScrollbar(direction);
 		if (scrollBar == null) {
 			return new Long(0);
 		}
 		return getMaximum(scrollBar);
 	}
 	
 	protected Long getMaximum(Widget scrollBar) {
 		String result = scrollBar.getPropertyValueAsJson("maximum");
 		return Long.parseLong(result);
 	}
 	
 	public Widget scrollToChild(String direction, By locator) {
 		WebElement target = contentElement.findElement(locator);
 		if (target != null) {
 			return driver.getWidgetForElement(target);
 		}
 		
 		driver.manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
 		
 		Long singleStep = getScrollStep(direction);
 		Long maximum = getMaximum(direction);
 		Long scrollPosition = getScrollPosition(direction);
 		
 		while (scrollPosition < maximum) {
 			target = contentElement.findElement(locator);
 			if (target != null) {
				// FirefoxDriver will return the correct child but calling click()
				// on it will cause the previous item to be selected, e.g. in a 
				// VirtualSelectBox list. Scrolling another half step to make sure
				// the child widget is in view fixes this.
				Long halfStep = singleStep / 2;
				int to = (int) (scrollPosition + singleStep);
				scrollTo(direction, to);
 				driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);
 				return driver.getWidgetForElement(target);
 			}
 			
 			int to = (int) (scrollPosition + singleStep);
 			scrollTo(direction, to);
 			scrollPosition = getScrollPosition(direction);
 		}
 		
 		//TODO: Find out the original timeout and re-apply it 
 		driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);
 		return null;
 	}
 
 }
