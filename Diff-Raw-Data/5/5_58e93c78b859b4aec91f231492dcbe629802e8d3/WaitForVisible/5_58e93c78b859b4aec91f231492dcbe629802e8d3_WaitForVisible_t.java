 package net.sf.testium.executor.webdriver.commands;
 /**
  * 
  */
 import java.util.ArrayList;
 
 import net.sf.testium.executor.general.SpecifiedParameter;
 import net.sf.testium.executor.webdriver.WebInterface;
 import net.sf.testium.selenium.SmartWebElement;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.utils.RunTimeData;
 
 /**
  * Command for waiting until the element is visible
  * 
  * @author Arjan Kranenburg
  *
  */
 public class WaitForVisible extends GenericSeleniumCommandExecutor
 {
 	private class isNotVisible implements ExpectedCondition<Boolean>
 	{
 		WebElement element;
 		
 		public isNotVisible( WebElement element ) {
 			this.element = element;
 		}
 
 		public Boolean apply(WebDriver input) {
 
 			try {
 				if ( ! element.isDisplayed() ) {
 					return true;
 				}
 			} catch ( WebDriverException ignored ) {
 				return true;
 			}
 			return false;
 		}
 		
 	}
 
 	private static final String COMMAND = "waitForVisible";
 
 	private static final SpecifiedParameter PARSPEC_ELEMENT = new SpecifiedParameter( 
 			"element", SmartWebElement.class, false, false, true, false );
 
 	private static final SpecifiedParameter PARSPEC_PRESENT = new SpecifiedParameter( 
 			"present", Boolean.class, true, true, false, false )
 			.setDefaultValue( true );
 
 	private static final SpecifiedParameter PARSPEC_TIMEOUT = new SpecifiedParameter( 
			"timeout", Long.class, true, true, true, false )
 			.setDefaultValue( 5L ); //seconds
 
 	private static final SpecifiedParameter PARSPEC_SLEEPTIME = new SpecifiedParameter( 
			"sleeptime", Long.class, true, true, true, false )
 			.setDefaultValue( 500L ); // milli-seconds
 
 	public WaitForVisible( WebInterface aWebInterface ) {
 		super( COMMAND, aWebInterface, new ArrayList<SpecifiedParameter>() );
 
 		this.addParamSpec( PARSPEC_ELEMENT );
 		this.addParamSpec( PARSPEC_PRESENT );
 		this.addParamSpec( PARSPEC_TIMEOUT );
 		this.addParamSpec( PARSPEC_SLEEPTIME );
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepResult result)
 			throws Exception {
 
 		WebElement element = obtainElement(aVariables, parameters, PARSPEC_ELEMENT);
 		if( ! (element instanceof SmartWebElement) )
 		{
 			throw new Exception( "Mandatory element is not a SmartWebElement" );
 		}
 		SmartWebElement smartElement = (SmartWebElement) element;
 		
 		Boolean presentFlag = obtainOptionalValue(aVariables, parameters, PARSPEC_PRESENT);
 		Long timeout = obtainOptionalValue(aVariables, parameters, PARSPEC_TIMEOUT);
 		Long sleeptime = obtainOptionalValue(aVariables, parameters, PARSPEC_SLEEPTIME);
 		
 		if ( presentFlag ) {
 			new WebDriverWait( getDriver(), timeout, sleeptime )
 			.until( ExpectedConditions.visibilityOf( smartElement ) );
 		} else {
 			new WebDriverWait( getDriver(), timeout, sleeptime )
 			.until( new isNotVisible( smartElement ) );
 		}
 	}
 }
