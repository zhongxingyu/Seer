 //Copyright 2011 Lohika .  This file is part of ALP.
 //
 //    ALP is free software: you can redistribute it and/or modify
 //    it under the terms of the GNU General Public License as published by
 //    the Free Software Foundation, either version 3 of the License, or
 //    (at your option) any later version.
 //
 //    ALP is distributed in the hope that it will be useful,
 //    but WITHOUT ANY WARRANTY; without even the implied warranty of
 //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //    GNU General Public License for more details.
 //
 //    You should have received a copy of the GNU General Public License
 //    along with ALP.  If not, see <http://www.gnu.org/licenses/>.
 package com.lohika.alp.selenium.log;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.events.WebDriverEventListener;
 
 import com.lohika.alp.selenium.configurator.Configuration;
 import com.lohika.alp.selenium.jscatcher.FirefoxJsErrorCathcer;
 import com.lohika.alp.selenium.jscatcher.JSErrorCatcher;
 
 public class LoggingWebDriverListener implements WebDriverEventListener {
 
 	private final Logger logger = Logger.getLogger(getClass());
 	private final LogElementsSeleniumFactory elementsFactory;
 
 	public LoggingWebDriverListener(LogElementsSeleniumFactory logElements) {
 		this.elementsFactory = logElements;
 	}
 
 	@Override
 	public void afterChangeValueOf(WebElement arg0, WebDriver arg1) {
 	}
 
 	@Override
 	public void afterClickOn(WebElement arg0, WebDriver driver) {
 	}
 
 	@Override
 	public void afterFindBy(By arg0, WebElement arg1, WebDriver arg2) {
 	}
 
 	@Override
 	public void afterNavigateBack(WebDriver arg0) {
 	}
 
 	@Override
 	public void afterNavigateForward(WebDriver arg0) {
 	}
 
 	@Override
 	public void afterNavigateTo(String arg0, WebDriver driver) {
 		if (!Configuration.getInstance().getJsErrorAutolog())
 			return;
 		JSErrorCatcher catcher = new FirefoxJsErrorCathcer(driver);
 		ArrayList<String> errors = catcher.getJsErrors();
 		if (errors!=null && errors.size()>0)
			logger.error(errors.toString());
 
 	}
 
 	@Override
 	public void afterScript(String arg0, WebDriver arg1) {
 	}
 
 	@Override
 	public void beforeChangeValueOf(WebElement arg0, WebDriver arg1) {
 	}
 
 	@Override
 	public void beforeClickOn(WebElement arg0, WebDriver driver) {
 		if (!Configuration.getInstance().getJsErrorAutolog())
 			return;
 		JSErrorCatcher catcher = new FirefoxJsErrorCathcer(driver);
 		ArrayList<String> errors = catcher.getJsErrors();
 		if (errors!=null && errors.size()>0)
			logger.error(errors.toString());
 
 	}
 
 	@Override
 	public void beforeFindBy(By arg0, WebElement arg1, WebDriver arg2) {
 	}
 
 	@Override
 	public void beforeNavigateBack(WebDriver arg0) {
 	}
 
 	@Override
 	public void beforeNavigateForward(WebDriver arg0) {
 	}
 
 	@Override
 	public void beforeNavigateTo(String arg0, WebDriver driver) {
 	}
 
 	@Override
 	public void beforeScript(String arg0, WebDriver arg1) {
 	}
 
 	@Override
 	public void onException(Throwable tr, WebDriver driver) {
 		logger.error(tr);
 
 		try {
 			Object screenshot = elementsFactory.screenshot(driver, tr
 					.getClass().getName());
 			if (screenshot != null)
 				logger.error(screenshot);
 
 			// EventFiringWebDriver uses Proxy mechanism to listen WebDriver
 			// exceptions. If WebDriverEventListener.onException implementation
 			// throws exception, Proxy will throw UndeclaredThrowableException
 			// into test
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 	}
 
 }
