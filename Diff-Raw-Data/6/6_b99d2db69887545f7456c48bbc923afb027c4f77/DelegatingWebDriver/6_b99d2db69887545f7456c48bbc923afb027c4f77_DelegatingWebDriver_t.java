 package ch.vorburger.webdriver.runner.core.providers;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.internal.WrapsDriver;
 import org.openqa.selenium.internal.WrapsElement;
 import org.openqa.selenium.support.events.WebDriverEventListener;
 
 /**
  * WebDriver Delegate helper.
  * 
  * Implementation for the correct handling for the various WebDriver extension
  * interfaces (JavascriptExecutor, TakesScreenshot, HasInputDevices, HasTouchScreen; 
  * Rotatable, BrowserConnection, WebStorage, LocationContext, LocationListener,
  * ApplicationCache; FindsBy* etc.) is "strongly inspired" (erm, mostly 
  * copy/pasted) from the EventFiringWebDriver.
  * 
 * Shame such a helper/util class is not already included in WebDriver core.
 * @see https://code.google.com/p/selenium/issues/detail?id=2512
 * 
  * @author Michael Vorburger
  */
 public abstract class DelegatingWebDriver implements WebDriver, WrapsDriver {
 	
 	// intentionally private and not protected, because all code should go through getWrappedDriver() 
 	private final WebDriver delegate;
 
 	protected DelegatingWebDriver(final WebDriver delegate) {
 		// copy/pasted from EventFiringWebDriver
 		Class<?>[] allInterfaces = extractInterfaces(delegate);
 		this.delegate = (WebDriver) Proxy.newProxyInstance(
 				WebDriverEventListener.class.getClassLoader(), allInterfaces,
 				new InvocationHandler() {
 					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 						if ("getWrappedDriver".equals(method.getName())) {
 							return delegate;
 						}
 
 						try {
 							return method.invoke(delegate, args);
 						} catch (InvocationTargetException e) {
 							onExceptionFromAdditionalInterfaceProxyInvocation(e);
 							throw e.getTargetException();
 						}
 					}
 				});
 	}
 
 	// copy/pasted from EventFiringWebDriver
 	protected Class<?>[] extractInterfaces(Object object) {
 		Set<Class<?>> allInterfaces = new HashSet<Class<?>>();
 		allInterfaces.add(WrapsDriver.class);
 		if (object instanceof WebElement) {
 			allInterfaces.add(WrapsElement.class);
 		}
 		extractInterfaces(allInterfaces, object.getClass());
 
 		return allInterfaces.toArray(new Class<?>[allInterfaces.size()]);
 	}
 
 	// copy/pasted from EventFiringWebDriver
 	protected void extractInterfaces(Set<Class<?>> addTo, Class<?> clazz) {
 		if (Object.class.equals(clazz)) {
 			return; // Done
 		}
 
 		Class<?>[] classes = clazz.getInterfaces();
 		addTo.addAll(Arrays.asList(classes));
 		extractInterfaces(addTo, clazz.getSuperclass());
 	}
 	
 	protected void onExceptionFromAdditionalInterfaceProxyInvocation(InvocationTargetException e) {
         // dispatcher.onException(e.getTargetException(), driver);
 	}
 	
 	@Override public WebDriver getWrappedDriver() {
 		return delegate;
 	}
 
 	public void get(String url) {
 		getWrappedDriver().get(url);
 	}
 
 	public String getCurrentUrl() {
 		return getWrappedDriver().getCurrentUrl();
 	}
 
 	public String getTitle() {
 		return getWrappedDriver().getTitle();
 	}
 
 	public List<WebElement> findElements(By by) {
 		return getWrappedDriver().findElements(by);
 	}
 
 	public WebElement findElement(By by) {
 		return getWrappedDriver().findElement(by);
 	}
 
 	public String getPageSource() {
 		return getWrappedDriver().getPageSource();
 	}
 
 	public void close() {
 		getWrappedDriver().close();
 	}
 
 	public void quit() {
 		getWrappedDriver().quit();
 	}
 
 	public Set<String> getWindowHandles() {
 		return getWrappedDriver().getWindowHandles();
 	}
 
 	public String getWindowHandle() {
 		return getWrappedDriver().getWindowHandle();
 	}
 
 	public TargetLocator switchTo() {
 		return getWrappedDriver().switchTo();
 	}
 
 	public Navigation navigate() {
 		return getWrappedDriver().navigate();
 	}
 
 	public Options manage() {
 		return getWrappedDriver().manage();
 	}
 	
 }
