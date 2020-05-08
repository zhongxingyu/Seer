 /**
  * 
  */
 package net.sf.testium.executor.webdriver.commands;
 
 import java.util.ArrayList;
 
 import org.openqa.selenium.WebElement;
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.utils.RunTimeData;
 
 import net.sf.testium.executor.general.SpecifiedParameter;
 import net.sf.testium.executor.webdriver.WebInterface;
 
 /**
  * Command for checking the text of a WebElement
  * 
  * @author Arjan Kranenburg
  *
  */
 public class CheckText extends GenericSeleniumCommandExecutor {
 	private static final SpecifiedParameter PARSPEC_ELEMENT = new SpecifiedParameter( 
 			"element", WebElement.class, false, false, true, false );
 
 	private static final SpecifiedParameter PARSPEC_EXPECTED = new SpecifiedParameter( 
 			"expected", String.class, false, true, true, true );
 
 	private static final SpecifiedParameter PARSPEC_MATCH = new SpecifiedParameter( 
			"match", String.class, true, true, true, false )
 			.setDefaultValue("exact");
 
 	private static final SpecifiedParameter PARSPEC_CASE = new SpecifiedParameter( 
			"case", Boolean.class, true, true, true, false )
 			.setDefaultValue( true );
 
 	private static final String COMMAND = "checkText";
 
 	public CheckText( WebInterface aWebInterface ) {
 		super( COMMAND, aWebInterface, new ArrayList<SpecifiedParameter>() );
 
 		this.addParamSpec( PARSPEC_ELEMENT );
 		this.addParamSpec( PARSPEC_EXPECTED );
 		this.addParamSpec( PARSPEC_MATCH );
 		this.addParamSpec( PARSPEC_CASE );
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepResult result)
 			throws Exception {
 
 
 		WebElement element = obtainElement( aVariables, parameters, PARSPEC_ELEMENT );
 //		if ( element instanceof SmartWebElement ) {
 //			SmartWebElement smElm = (SmartWebElement) element;
 //			element = smElm.getElement();
 //			
 //			if ( element == null ) {
 //				element = this.getDriver().findElement( smElm.getBy() );
 //			}
 //		}
 //Object obj = obtainValue( aVariables, parameters, PARSPEC_ELEMENT );
 ////SmartWebElement smElm = null;
 //WebElement elm = null;
 //if ( obj instanceof WebElement || Proxy.isProxyClass(obj.getClass()) ) {
 //	elm = (WebElement) obj;
 //	if (elm instanceof SmartWebElement) {
 //		SmartWebElement smElm = (SmartWebElement) elm;
 //		elm = smElm.getElement();
 //		
 //		if ( elm == null ) {
 //			elm = this.getDriver().findElement( smElm.getBy() );
 //		}
 //	}
 //}
 		String expectedText = (String) obtainValue( aVariables, parameters, PARSPEC_EXPECTED );
 		
 //		if ( elm == null ) {
 //		if ( element == null ) {
 //			throw new Exception( "Element is not defined" );
 //		}
 
 //		String actualText = getActualText(elm);
 		String actualText = getActualText(element);
 
 		String match = (String) this.obtainOptionalValue(aVariables, parameters, PARSPEC_MATCH);
 		boolean caseSensitive = (Boolean) this.obtainOptionalValue(aVariables, parameters, PARSPEC_CASE);
 		if ( match.equalsIgnoreCase( "exact" ) )
 		{
 			checkExact(expectedText, actualText, caseSensitive,
 					"Actual Text: \"" + actualText + "\" is not equal to: \"" + expectedText + "\"");
 			return;
 		}
 		else if ( match.equalsIgnoreCase( "contains" ) )
 		{
 			checkContains(expectedText, actualText, caseSensitive,
 					"Actual Text: \"" + actualText + "\" does not contain: \"" + expectedText + "\"");
 			return;
 		}
 		else if ( match.equalsIgnoreCase( "startsWith" ) )
 		{
 			checkStartsWith(expectedText, actualText, caseSensitive,
 					"Actual Text: \"" + actualText + "\" does not start with: \"" + expectedText + "\"" );
 			return;
 		}
 		else if ( match.equalsIgnoreCase( "endsWith" ) )
 		{
 			checkEndsWith(expectedText, actualText, caseSensitive,
 					"Actual Text: \"" + actualText + "\" does not end with: \"" + expectedText + "\"" );
 			return;
 		}
 		else
 		{
 			throw new Exception( "match criteria \"" + match + "\" is not supported. Only exact, contains, startsWith, or endsWith" );
 		}
 	}
 
 	/**
 	 * @param expectedText
 	 * @param actualText
 	 * @param caseSensitive
 	 * @param message
 	 * @throws Exception
 	 */
 	private void checkExact(String expectedText, String actualText,
 			boolean caseSensitive, String message) throws Exception {
 		if ( caseSensitive ) {
 			if ( ! actualText.equals(expectedText) ) {
 				throw new Exception( message );
 			}
 		} else {
 			if ( ! actualText.equalsIgnoreCase(expectedText) ) {
 				throw new Exception( message + " (ignoring case)" );
 			}
 		}
 	}
 	
 	/**
 	 * @param expectedText
 	 * @param actualText
 	 * @param caseSensitive
 	 * @param message
 	 * @throws Exception
 	 */
 	private void checkContains(String expectedText, String actualText,
 			boolean caseSensitive, String message) throws Exception {
 		if ( caseSensitive ) {
 			if ( ! actualText.contains(expectedText) ) {
 				throw new Exception( message );
 			}
 		} else {
 			String expectedText_lowerCase = expectedText.toLowerCase();
 			String actualText_lowerCase = actualText.toLowerCase();
 			if ( ! actualText_lowerCase.contains(expectedText_lowerCase) ) {
 				throw new Exception( message + " (ignoring case)" );
 			}
 		}
 	}
 
 	/**
 	 * @param expectedText
 	 * @param actualText
 	 * @param caseSensitive
 	 * @param message
 	 * @throws Exception
 	 */
 	private void checkStartsWith(String expectedText, String actualText,
 			boolean caseSensitive, String message) throws Exception {
 		if ( caseSensitive ) {
 			if ( ! actualText.startsWith(expectedText) ) {
 				throw new Exception( message );
 			}
 		} else {
 			String expectedText_lowerCase = expectedText.toLowerCase();
 			String actualText_lowerCase = actualText.toLowerCase();
 			if ( ! actualText_lowerCase.startsWith(expectedText_lowerCase) ) {
 				throw new Exception( message + " (ignoring case)" );
 			}
 		}
 	}
 
 	/**
 	 * @param expectedText
 	 * @param actualText
 	 * @param caseSensitive
 	 * @param message
 	 * @throws Exception
 	 */
 	private void checkEndsWith(String expectedText, String actualText,
 			boolean caseSensitive, String message) throws Exception {
 		if ( caseSensitive ) {
 			if ( ! actualText.endsWith(expectedText) ) {
 				throw new Exception( message );
 			}
 		} else {
 			String expectedText_lowerCase = expectedText.toLowerCase();
 			String actualText_lowerCase = actualText.toLowerCase();
 			if ( ! actualText_lowerCase.endsWith(expectedText_lowerCase) ) {
 				throw new Exception( message + " (ignoring case)" );
 			}
 		}
 	}
 
 	protected static String getActualText( WebElement elm )
 	{
 		if ( elm == null )
 		{ 
 			return "";
 		} //else
 		
 		if ( elm.getTagName().equalsIgnoreCase("input") || 
 			 elm.getTagName().equalsIgnoreCase("textarea")	)
 		{
 			String text = elm.getAttribute("value");
 			return text == null ? "" : text;
 		} //else
 		
 		return elm.getText();
 	}
 }
