 /**
  * 
  */
 package model;
 
 import java.net.URL;
 
 /**
  * @author Derek Carr
  *
  */
 public class SocketString {
 
 	private String handlerKey = "KEY";
 	private String antCommand;
 	private String os;
 	private String browser;
 	private String browserVersion;
 	private URL url;
 	private String lmpUser;
 	private String lmpPass;
 	private String sfUser;
 	private String sfPass;
 	private String email;
 	private int queueNumber;
 	private double time;
 	private String testPackage;
 	private String testClass;
 	
 	/**
 	 * Class Constructor
 	 */
 	public SocketString() {
 		this.antCommand = null;
 		this.os = null;
 		this.browser = null;
 		this.browserVersion = null;
 		this.url = null;
 		this.lmpUser = null;
 		this.lmpPass = null;
 		this.sfUser = null;
 		this.sfPass = null;
 		this.email = null;
 		this.queueNumber = 0;
 		this.time = 0.00;
 		this.testPackage = null;
 		this.testClass = null;
 	}
 	
 	
 	/**
 	 * @param handlerKey
 	 * @param antCommand
 	 * @param os
 	 * @param browser
 	 * @param browserVersion
 	 * @param url
 	 * @param lmpUser
 	 * @param lmpPass
 	 * @param sfUser
 	 * @param sfPass
 	 * @param email
 	 * @param queueNumber
 	 * @param time
 	 * @param testPackage
 	 * @param testClass
 	 */
 	public SocketString(String os, String browser, String browserVersion, URL url, 
 			String lmpUser, String lmpPass, String sfUser, String sfPass, String email,
 			int queueNumber, double time, String testPackage, String testClass) {
 		super();
 		this.antCommand = buildAntCommand(testPackage, testClass);
 		this.os = os;
 		this.browser = browser;
 		this.browserVersion = browserVersion;
 		this.url = url;
 		this.lmpUser = lmpUser;
 		this.lmpPass = lmpPass;
 		this.sfUser = sfUser;
 		this.sfPass = sfPass;
 		this.email = email;
 		this.queueNumber = queueNumber;
 		this.time = time;
 		this.testPackage = testPackage;
 		this.testClass = testClass;
 	}
 
 	/**
 	 * @return the handlerKey
 	 */
 	public String getHandlerKey() {
 		return handlerKey;
 	}
 
 	/**
 	 * @param handlerKey the handlerKey to set
 	 */
 	public void setHandlerKey(String handlerKey) {
 		this.handlerKey = handlerKey;
 	}
 
 	/**
 	 * @return the antCommand
 	 */
 	public String getAntCommand() {
 		return antCommand;
 	}
 	
 	/**
 	 * 
 	 * @param antCommand
 	 */
 	public void setAntCommand(String antCommand) {
 		this.antCommand = antCommand;
 	}
 
 	/**
 	 * @param antCommand the antCommand to set
 	 */
 	public String buildAntCommand(String testPackage, String testClass) {
 		assert this.browser != null;
		if (this.browser.toLowerCase().contains("explore")) {
 			this.browser = "iexplore";
		}
 		return "ant -DBROWSER="+this.browser.toLowerCase()+" -f build"+testPackage+".xml "+testPackage.toLowerCase()+"."+testClass;
 	}
 
 	/**
 	 * @return the os
 	 */
 	public String getOs() {
 		return os;
 	}
 
 	/**
 	 * @param os the os to set
 	 */
 	public void setOs(String os) {
 		this.os = os;
 	}
 
 	/**
 	 * @return the browser
 	 */
 	public String getBrowser() {
 		return browser;
 	}
 
 	/**
 	 * @param browser the browser to set
 	 */
 	public void setBrowser(String browser) {
 		this.browser = browser;
 	}
 
 	/**
 	 * @return the browserVersion
 	 */
 	public String getBrowserVersion() {
 		return browserVersion;
 	}
 
 	/**
 	 * @param browserVersion the browserVersion to set
 	 */
 	public void setBrowserVersion(String browserVersion) {
 		this.browserVersion = browserVersion;
 	}
 
 	/**
 	 * @return the url
 	 */
 	public URL getUrl() {
 		return url;
 	}
 
 	/**
 	 * @param url the url to set
 	 */
 	public void setUrl(URL url) {
 		this.url = url;
 	}
 
 	/**
 	 * @return the lmpUser
 	 */
 	public String getLmpUser() {
 		return lmpUser;
 	}
 
 	/**
 	 * @param lmpUser the lmpUser to set
 	 */
 	public void setLmpUser(String lmpUser) {
 		this.lmpUser = lmpUser;
 	}
 
 	/**
 	 * @return the lmpPass
 	 */
 	public String getLmpPass() {
 		return lmpPass;
 	}
 
 	/**
 	 * @param lmpPass the lmpPass to set
 	 */
 	public void setLmpPass(String lmpPass) {
 		this.lmpPass = lmpPass;
 	}
 
 	/**
 	 * @return the sfUser
 	 */
 	public String getSfUser() {
 		return sfUser;
 	}
 
 	/**
 	 * @param sfUser the sfUser to set
 	 */
 	public void setSfUser(String sfUser) {
 		this.sfUser = sfUser;
 	}
 
 	/**
 	 * @return the sfPass
 	 */
 	public String getSfPass() {
 		return sfPass;
 	}
 
 	/**
 	 * @param sfPass the sfPass to set
 	 */
 	public void setSfPass(String sfPass) {
 		this.sfPass = sfPass;
 	}
 
 	/**
 	 * @return the email
 	 */
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * @param email the email to set
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	/**
 	 * @return the queueNumber
 	 */
 	public int getQueueNumber() {
 		return queueNumber;
 	}
 
 	/**
 	 * @param queueNumber the queueNumber to set
 	 */
 	public void setQueueNumber(int queueNumber) {
 		this.queueNumber = queueNumber;
 	}
 
 	/**
 	 * @return the time
 	 */
 	public double getTime() {
 		return time;
 	}
 
 	/**
 	 * @param time the time to set
 	 */
 	public void setTime(double time) {
 		this.time = time;
 	}
 
 	/**
 	 * @return the testPackage
 	 */
 	public String getTestPackage() {
 		return testPackage;
 	}
 
 	/**
 	 * @param testPackage the testPackage to set
 	 */
 	public void setTestPackage(String testPackage) {
 		this.testPackage = testPackage;
 	}
 
 	/**
 	 * @return the testClass
 	 */
 	public String getTestClass() {
 		return testClass;
 	}
 
 	/**
 	 * @param testClass the testClass to set
 	 */
 	public void setTestClass(String testClass) {
 		this.testClass = testClass;
 	}
 
 	/**
 	 * Overrides to String - this is the string that gets serialized to send across the network
 	 */
 	@Override
 	public String toString() {
 		assert handlerKey != null;
 		assert antCommand != null;
 		assert os != null;
 		assert browser != null;
 		assert browserVersion != null;
 		assert url != null;
 		assert lmpUser != null;
 		assert lmpPass != null;
 		assert sfUser != null;
 		assert sfPass != null;
 		assert email != null;
 		assert queueNumber > 0;
 		assert testPackage != null;
 		assert testClass != null;
 	
 		String delim = ";";
 		return handlerKey + delim + antCommand + delim + os + delim + browser + delim + browserVersion +
 				delim + url.toString() + delim + lmpUser + delim + lmpPass + delim + sfUser + delim + sfPass +
 				delim + email + delim + queueNumber + delim + time + delim + testPackage + delim + testClass + "\n";
 	}
 }
