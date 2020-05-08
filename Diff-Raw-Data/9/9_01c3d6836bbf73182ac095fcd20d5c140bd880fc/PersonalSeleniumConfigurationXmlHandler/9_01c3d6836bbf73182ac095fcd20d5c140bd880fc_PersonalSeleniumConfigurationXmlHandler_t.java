 package net.sf.testium.configuration;
 
import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import net.sf.testium.configuration.SeleniumConfiguration.BROWSER_TYPE;
 import net.sf.testium.configuration.SeleniumInterfaceConfiguration.SAVE_SOURCE;
 
 import org.testtoolinterfaces.testsuite.TestSuiteException;
 import org.testtoolinterfaces.utils.GenericTagAndStringXmlHandler;
 import org.testtoolinterfaces.utils.Trace;
 import org.testtoolinterfaces.utils.XmlHandler;
 import org.xml.sax.Attributes;
 import org.xml.sax.XMLReader;
 
 /**
  * @author Arjan Kranenburg 
  * 
  *  <SeleniumConfiguration>
  *    <DefaultBrowser>...</DefaultBrowser>
  *    <SavePageSource>...</SavePageSource>
  *    <SaveScreenshot>...</SaveScreenshot>
  *    <SeleniumInterfaces>...</SeleniumInterfaces>
  *    <SeleniumGridUrl>...</SeleniumGridUrl>
  *  ...
  *  </SeleniumConfiguration>
  * 
  */
 
 public class PersonalSeleniumConfigurationXmlHandler extends XmlHandler
 {
 	private static final String START_ELEMENT = "SeleniumConfiguration";
 
 	private static final String DEF_BROWSER_ELEMENT = "DefaultBrowser";
 	private static final String SAVE_PAGESOURCE = "SavePageSource"; // NEVER, ONFAIL, ALWAYS
 	private static final String SAVE_SCREENSHOT = "SaveScreenshot"; // NEVER, ONFAIL, ALWAYS
 	private static final String SELENIUM_GRID_URL_ELEMENT = "SeleniumGridUrl";
 
 	private GenericTagAndStringXmlHandler myDefaultBrowserXmlHandler;
 	private GenericTagAndStringXmlHandler mySavePageSourceXmlHandler;
 	private GenericTagAndStringXmlHandler mySaveScreenShotXmlHandler;
 	private GenericTagAndStringXmlHandler mySeleniumGridUrlXmlHandler;
 	private SeleniumInterfacesXmlHandler myInterfacesXmlHandler;
 	
 	private BROWSER_TYPE myDefaultBrowser;
	private final File mySeleniumLibsDir;
 	private URL mySeleniumGridUrl = null;
 	private ArrayList<String> myInterfaceNames;
 	private SAVE_SOURCE mySavePageSource;
 	private SAVE_SOURCE mySaveScreenShot;
 
 	public PersonalSeleniumConfigurationXmlHandler( XMLReader anXmlReader,
 			SeleniumConfiguration globalConfig)
 	{
 	    super(anXmlReader, START_ELEMENT);
 	    Trace.println(Trace.CONSTRUCTOR);
 
 	    myDefaultBrowser = globalConfig.getBrowserType();
	    mySeleniumLibsDir = globalConfig.getSeleniumLibsDir();
 		mySeleniumGridUrl = globalConfig.getSeleniumGridUrl();
 		myInterfaceNames = globalConfig.getInterfaceNames();
 		mySavePageSource = globalConfig.getSavePageSource();
 		mySaveScreenShot = globalConfig.getSaveScreenShot();
 
 	    myDefaultBrowserXmlHandler = new GenericTagAndStringXmlHandler(anXmlReader, DEF_BROWSER_ELEMENT);
 		this.addElementHandler(myDefaultBrowserXmlHandler);
 
 		mySeleniumGridUrlXmlHandler = new GenericTagAndStringXmlHandler(anXmlReader, SELENIUM_GRID_URL_ELEMENT);
 		this.addElementHandler(mySeleniumGridUrlXmlHandler);
 
 		mySavePageSourceXmlHandler = new GenericTagAndStringXmlHandler(anXmlReader, SAVE_PAGESOURCE);
 		this.addElementHandler(mySavePageSourceXmlHandler);
 
 		mySaveScreenShotXmlHandler = new GenericTagAndStringXmlHandler(anXmlReader, SAVE_SCREENSHOT);
 		this.addElementHandler(mySaveScreenShotXmlHandler);
 
 		myInterfacesXmlHandler = new SeleniumInterfacesXmlHandler( anXmlReader );
 		this.addElementHandler(myInterfacesXmlHandler);
 	}
 
 	@Override
 	public void handleStartElement(String aQualifiedName)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleCharacters(String aValue)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleEndElement(String aQualifiedName)
 	{
 		// nop
 	}
 
 	@Override
 	public void processElementAttributes(String aQualifiedName, Attributes att)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleGoToChildElement(String aQualifiedName)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleReturnFromChildElement(String aQualifiedName, XmlHandler aChildXmlHandler)
 			throws TestSuiteException
 	{
 	    Trace.println(Trace.UTIL, "handleReturnFromChildElement( " + 
 		    	      aQualifiedName + " )", true);
 		    
 		if (aQualifiedName.equalsIgnoreCase( DEF_BROWSER_ELEMENT ))
     	{
 			BROWSER_TYPE browserType = BROWSER_TYPE.enumOf( myDefaultBrowserXmlHandler.getValue() );
 			myDefaultBrowser = browserType;
 			myDefaultBrowserXmlHandler.reset();	
     	}
 		else if (aQualifiedName.equalsIgnoreCase( SELENIUM_GRID_URL_ELEMENT ))
     	{
 			String seleniumGridUrl = mySeleniumGridUrlXmlHandler.getValue();
 //			seleniumGridUrl = myRtData.substituteVars(seleniumGridUrl);
 			try {
 				mySeleniumGridUrl = new URL( seleniumGridUrl );
 			} catch (MalformedURLException e) {
 				throw new TestSuiteException( "\"" + SELENIUM_GRID_URL_ELEMENT + "\" is malformed: " + e.getMessage(), e );
 			}
 
 			mySeleniumGridUrlXmlHandler.reset();	
     	}
 		else if (aQualifiedName.equalsIgnoreCase( SAVE_PAGESOURCE ))
     	{
 			String savePageSource = mySavePageSourceXmlHandler.getValue();
 			mySavePageSource = SAVE_SOURCE.enumOf(savePageSource);
 			mySavePageSourceXmlHandler.reset();	
     	}
 		else if (aQualifiedName.equalsIgnoreCase( SAVE_SCREENSHOT ))
     	{
 			String saveScreenshots = mySaveScreenShotXmlHandler.getValue();
 			mySaveScreenShot = SAVE_SOURCE.enumOf(saveScreenshots);
 			mySaveScreenShotXmlHandler.reset();	
     	}
 		else if (aQualifiedName.equalsIgnoreCase( myInterfacesXmlHandler.getStartElement() ))
     	{
 			myInterfaceNames = myInterfacesXmlHandler.getInterfaceNames();
 			myInterfacesXmlHandler.reset();
     	}
 		else
     	{ // Programming fault
 			throw new Error( "Child XML Handler returned, but not recognized. The handler was probably defined " +
 			                 "in the Constructor but not handled in handleReturnFromChildElement()");
 		}
 	}
 	
 	public SeleniumConfiguration getConfiguration()
 	{
		return new SeleniumConfiguration( myInterfaceNames, myDefaultBrowser,
				mySeleniumLibsDir, mySeleniumGridUrl, mySavePageSource, mySaveScreenShot );
 	}
 
 }
