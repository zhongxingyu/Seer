 package roark.utilities.keywords;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.Keys;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.interactions.Actions;
 import org.openqa.selenium.support.ui.Select;
 
 import roark.jelenium.TestSuite;
 import roark.jelenium.TestcaseStep;
 import roark.utilities.data.EnvironmentVariables;
 
 public class WebUI {
 	static Logger logger = Logger.getLogger(WebUI.class);
 	private WebDriver webDriver;
 	public WebUI(){
 	} 
 	
 	
 	public int checkElement(TestcaseStep testcaseStep) {
 		int exitCode = 0;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("ON")){
 					if(targetElement.isSelected()==true){	
 						try{
 							targetElement.click();
 							exitCode=0;
 						}catch(Exception e){
 							exitCode=1;
 							logger.error("Exception in performing the Check operation - "+ e.getMessage());
 						}
 					}else{
 						logger.info("Already Checked");
 						exitCode=0;
 					}
 				}else if(testcaseStep.getTestDataValue().equalsIgnoreCase("OFF")){
 					if(targetElement.isSelected()==false){	
 						try{
 							targetElement.click();
 							exitCode=0;
 						}catch(Exception e){
 							exitCode=1;
 						}
 					}else{
 						logger.info("Already UnChecked");
 						exitCode=0;
 					}
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in checkElement -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 
 	public int clickElement(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					targetElement.click();
 					exitCode=0;
 				}catch(Exception e){
 					exitCode=1;
 					logger.error("Exception in performing the Click operation - "+ e.getMessage());
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.error("Unknown Exception in ClickElement -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 
 	public int enterText(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				targetElement.clear();
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						targetElement.sendKeys(testcaseStep.getTestDataValue());
 						exitCode=0;
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the Enter operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 					logger.info("Testdata not found @step#"+testcaseStep.getStepID());
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in EnterText -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	
 	private WebElement findElementByXpath(String xpath){
 		WebElement targetObject;
 		try{
 			if(this.isElementPresent( xpath)){
 				targetObject= this.getWebDriver().findElement(By.xpath(xpath));
 				logger.info("targetObject is set to ::"+targetObject );
 			}else{
 				targetObject= null;
 				logger.error("targetObject is set to null , with xpath::"+xpath );
 			}
 
 		}catch(Exception e){
 			logger.info("exception in findElementByXpath \n"+e.getMessage());
 			targetObject=null;
 			//e.printStackTrace();
 		}
 		return targetObject;
 		
 	}
 	private boolean isElementPresent(String fieldDefinition){
 		boolean isElementPresent;
 		try{
 			if(fieldDefinition.equalsIgnoreCase("LOCATOR_NOT_FOUND")==false){
 				if(this.getWebDriver().findElements(By.xpath(fieldDefinition)).size()>=1){
 					isElementPresent=true;
 					logger.info("Element  is found on the page , fieldDefinition::"+fieldDefinition);
 				}else{
 					isElementPresent=false;
 					logger.error("Element  is NOT found on the page , fieldDefinition::"+fieldDefinition);
 				}
 			}else{
 				logger.error("Element  is NOT found on the page , fieldDefinition::"+fieldDefinition);
 				isElementPresent=false;
 			}
 		}catch(Exception e){
 			isElementPresent=false;
 			logger.error("Exception in isElementPresent - \n"+ e.getMessage());
 		}
 		return isElementPresent;
 		
 	}
 
 	public int clickElementAndWait(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					targetElement.click();
 					int pageLoadtimeout =this.getPageLoadtimeoutValue(testcaseStep);
 					logger.info("clicked on the element - " + testcaseStep.getFieldName());
 					int c=0;
 					logger.info("Waiting for PageLoad to complete,..after clicking on element - " + testcaseStep.getFieldName());
 					while( ((JavascriptExecutor) this.getWebDriver()).executeScript("return document.readyState;").toString().equalsIgnoreCase("complete")==false){
 						Thread.sleep(1000);
 						c=c+1;
 						if(c== pageLoadtimeout){
 							break;
 						}
 					}
 					if(((JavascriptExecutor) this.getWebDriver()).executeScript("return document.readyState;").toString().equalsIgnoreCase("complete")==true){
 						exitCode=0;
 						logger.info("Pageload is complete within "+c+ " seconds");
 					}else{
 						exitCode=13; // pageLoad not complete errorCode
 						logger.error("PageLoad not complete after "+pageLoadtimeout +" seconds");
 					}
 				}catch(Exception e){
 					exitCode=1;
 					logger.error("Exception in performing the ClickAndWait operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in ClickAndWait -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 		
 	}
 	private int getPageLoadtimeoutValue(TestcaseStep testcaseStep) {
 		int pageLoadtimeout;
 		try{
 			EnvironmentVariables ev = EnvironmentVariables.getInstance();
 			String evValue =ev.getTestParameterValue(testcaseStep.getApplicationID(), "PageloadTimeout");
 			if(evValue.equals("NOT_FOUND")==false){
 				pageLoadtimeout= Integer.parseInt(evValue);
 				logger.info("pageLoadtimeout is set to - "+evValue);
 			}else{
 				pageLoadtimeout=30;
 				logger.info("pageLoadtimeout is not found in envVariables , set to default value- "+pageLoadtimeout);
 			}
 		}catch(Exception e){
 			logger.error("Exception in fetching global pageLoadtimeout value  - " + e.getMessage());
 			pageLoadtimeout=30;
 			logger.info("pageLoadtimeout is not found in envVariables , set to default value- "+pageLoadtimeout);
 
 		}
 		return pageLoadtimeout;
 	}
 	public int clickElementUsingJavaScript(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					((JavascriptExecutor) this.getWebDriver()).executeScript("arguments[0].click();",targetElement);				
 					 WebDriver mywd = this.getWebDriver();
 					 JavascriptExecutor js = (JavascriptExecutor) mywd; 
 					 js.executeScript("document.getElementsByName('listPrice1')[0].value='200';");
 					// mywd.findElement(By.xpath("")).sendKeys("");
 					 
 					exitCode=0;
 				}catch(Exception e){
 					exitCode=1;
 					logger.error("Exception in performing the ClickElementUsingJavaScript operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in ClickElementUsingJavaScript -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 		
 	}
 	public int mouseOver(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					Actions builder = new Actions(this.getWebDriver());
 					builder.moveToElement(targetElement).perform();
 					exitCode=0;
 				}catch(Exception e){
 					exitCode=1;
 					logger.error("Exception in performing the MouseOver operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in MouseOver -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int mouseOverUsingJavaScript(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					((JavascriptExecutor) this.getWebDriver()).executeScript("arguments[0].mouseover();", targetElement);
 					exitCode=0;
 				}catch(Exception e){
 					exitCode=1;
 					logger.error("Exception in performing the MouseOverUsingJavaScript operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in MouseOverUsingJavaScript -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int checkRadioByValue(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition()+"[@value='"+testcaseStep.getTestDataValue()+"']");
 			if(targetElement!=null){
 				try{
 					targetElement.click();
 					exitCode=0;
 				}catch(Exception e){
 					exitCode=1;
 					logger.error("Exception in performing the CheckRadioByValue operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in CheckRadioByValue -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int checkRadioByPreSibling(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition()+"//preceding-sibling::input");
 			if(targetElement!=null){
 				try{
 					targetElement.click();
 					exitCode=0;
 				}catch(Exception e){
 					exitCode=1;
 					logger.error("Exception in performing the CheckRadioByPreSibling operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in CheckRadioByPreSibling -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 		
 	}
 	public int checkRadioByFolSibling(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition()+"//following-sibling::input");
 			if(targetElement!=null){
 				try{
 					targetElement.click();
 					exitCode=0;
 				}catch(Exception e){
 					exitCode=1;
 					logger.error("Exception in performing the CheckRadioByFolSibling operation - "+ e.getMessage());
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in CheckRadioByFolSibling -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int selectListByIndex(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						Select targetSelector = new Select (targetElement);
 						targetSelector.selectByIndex(Integer.parseInt(testcaseStep.getTestDataValue().trim()));
 						exitCode=0;
 					}catch(Exception e){
 						exitCode=1;
 						logger.error("Exception in performing the SelectListByIndex operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode = 2;
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in SelectListByIndex -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;	
 	}
 	public int selectListByLabel(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						Select targetSelector = new Select (targetElement);
 						targetSelector.selectByVisibleText(testcaseStep.getTestDataValue().trim());
 						exitCode=0;
 					}catch(Exception e){
 						exitCode=1;
 						logger.error("Exception in performing the SelectListByLabel operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode = 2;
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in SelectListByLabel -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int selectListByValue(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						Select targetSelector = new Select (targetElement);
 						targetSelector.selectByValue(testcaseStep.getTestDataValue().trim());
 						exitCode=0;
 					}catch(Exception e){
 						exitCode=1;
 						logger.error("Exception in performing the SelectListByValue operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode = 2;
 				}
 			}else{
 				exitCode = 3;
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in SelectListByValue -" +e.getMessage());
 			exitCode =4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;	
 	}
 	public int sendEnterKey(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				targetElement.clear();
 				try{
 					targetElement.sendKeys(Keys.ENTER);
 					exitCode=0;
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the SendEnterKey operation - "+ e.getMessage());
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in SendEnterKey -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int sendTabKey(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				targetElement.clear();
 				try{
 					targetElement.sendKeys(Keys.TAB);
 					exitCode=0;
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the SendTabKey operation - "+ e.getMessage());
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in SendTabKey -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int appendTextAtStart(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				targetElement.clear();
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						targetElement.sendKeys(Keys.HOME);
 						targetElement.sendKeys(testcaseStep.getTestDataValue());
 						exitCode=0;
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the AppendTextAtStart operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in AppendTextAtStart -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int appendText(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				targetElement.clear();
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						targetElement.sendKeys(testcaseStep.getTestDataValue());
 						exitCode=0;
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the AppendText operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in AppendText -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int clear(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					targetElement.clear();
 					exitCode=0;
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the Clear operation - "+ e.getMessage());
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in Clear -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	private int verifyAttribute(TestcaseStep testcaseStep, String string) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(targetElement.getAttribute(string).equals(testcaseStep.getTestDataValue())){
 							exitCode=0;
 						}
 						else{
 							exitCode=5;
 							testcaseStep.setErrDescription("Verification Mismatch. Actual: "+targetElement.getAttribute(string)+" Expected: "+testcaseStep.getTestDataValue());
 						}
 						
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyAttribute operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyAttribute"+string+" -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 		
 	}
 	private int verifyAttributeStartsWith(TestcaseStep testcaseStep,String string) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(targetElement.getAttribute(string).startsWith(testcaseStep.getTestDataValue())){
 							exitCode=0;
 						}
 						else{
 							exitCode=5;
 							testcaseStep.setErrDescription("Verification Mismatch. Actual: "+targetElement.getAttribute(string)+" Expected: "+testcaseStep.getTestDataValue());
 						}
 						
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the verifyAttributeStartsWith operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in verify"+string+"StartsWith -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	private  int verifyAttributeEndsWith(TestcaseStep testcaseStep, String string) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(targetElement.getAttribute(string).endsWith(testcaseStep.getTestDataValue())){
 							exitCode=0;
 						}
 						else{
 							exitCode=5;
 							testcaseStep.setErrDescription("Verification Mismatch. Actual: "+targetElement.getAttribute(string)+" Expected: "+testcaseStep.getTestDataValue());
 						}
 						
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyAttributeEndsWith operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in Verify"+string+"EndsWith -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 
 	public int verifyTextStartsWith(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(targetElement.getText().startsWith(testcaseStep.getTestDataValue())){
 							exitCode=0;
 						}
 						else{
 							exitCode=5;
 							testcaseStep.setErrDescription("Verification Mismatch. Actual: "+targetElement.getText()+" Expected: "+testcaseStep.getTestDataValue());
 						}
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyTextStartsWith operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyTextStartsWith -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyTextEndsWith(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(targetElement.getText().endsWith(testcaseStep.getTestDataValue())){
 							exitCode=0;
 						}
 						else{
 							exitCode=5;
 							testcaseStep.setErrDescription("Verification Mismatch. Actual: "+targetElement.getText()+" Expected: "+testcaseStep.getTestDataValue());
 						}
 						
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyTextEndsWith operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyTextEndsWith -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int enterByJavaScript(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 				((JavascriptExecutor) this.getWebDriver()).executeScript("arguments[0].value='"+testcaseStep.getTestDataValue()+"';",targetElement);	
 				exitCode=0;
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the EnterByJavaScript operation - "+ e.getMessage());
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in EnterByJavaScript -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyValueByJavaScript(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 				String val=(String)((JavascriptExecutor) this.getWebDriver()).executeScript("return arguments[0].value;",targetElement);	
 					if(val.equals(testcaseStep.getTestDataValue())){
 						exitCode=0;
 					}else{
 						exitCode=5;
 						testcaseStep.setErrDescription("Verification Mismatch. Actual: "+val+" Expected: "+testcaseStep.getTestDataValue());
 					}
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the VerifyValueByJavaScript operation - "+ e.getMessage());
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyValueByJavaScript -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyValueByJavaScriptStartsWith(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 				String val=(String)((JavascriptExecutor) this.getWebDriver()).executeScript("return arguments[0].value;",targetElement);	
 					if(val.startsWith(testcaseStep.getTestDataValue())){
 						exitCode=0;
 					}else{
 						exitCode=5;
 						testcaseStep.setErrDescription("Verification Mismatch. Actual: "+val+" Expected: "+testcaseStep.getTestDataValue());
 					}
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the VerifyValueByJavaScriptStartsWith operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyValueByJavaScriptStartsWith -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyValueByJavaScriptEndsWith(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 				String val=(String)((JavascriptExecutor) this.getWebDriver()).executeScript("return arguments[0].value;",targetElement);	
 					if(val.endsWith(testcaseStep.getTestDataValue())){
 						exitCode=0;
 					}else{
 						exitCode=5;
 						testcaseStep.setErrDescription("Verification Mismatch. Actual: "+val+" Expected: "+testcaseStep.getTestDataValue());
 					}
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the VerifyValueByJavaScriptEndsWith operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyValueByJavaScriptEndsWith -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int setInnerHtml(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 				((JavascriptExecutor) this.getWebDriver()).executeScript("document.body.innerHTML = '"+testcaseStep.getTestDataValue()+"'",targetElement);
 				exitCode=0;
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the SetInnerHtml operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in SetInnerHtml -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyInnerHtml(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 				String val=(String)((JavascriptExecutor) this.getWebDriver()).executeScript("return document.body.innerHTML");	
 					if(val.equals(testcaseStep.getTestDataValue())){
 						exitCode=0;
 					}else{
 						exitCode=5;
 						testcaseStep.setErrDescription("Verification Mismatch. Actual: "+val+" Expected: "+testcaseStep.getTestDataValue());
 					}
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the VerifyInnerHtml operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyInnerHtml -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyInnerHtmlStartsWith(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 				String val=(String)((JavascriptExecutor) this.getWebDriver()).executeScript("return document.body.innerHTML");	
 					if(val.startsWith(testcaseStep.getTestDataValue())){
 						exitCode=0;
 					}else{
 						exitCode=5;
 						testcaseStep.setErrDescription("Verification Mismatch. Actual: "+val+" Expected: "+testcaseStep.getTestDataValue());
 					}
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the VerifyInnerHtmlStartsWith operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyInnerHtmlStartsWith -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyInnerHtmlEndsWith(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 				String val=(String)((JavascriptExecutor) this.getWebDriver()).executeScript("return document.body.innerHTML");	
 					if(val.endsWith(testcaseStep.getTestDataValue())){
 						exitCode=0;
 					}else{
 						exitCode=5;
 						testcaseStep.setErrDescription("Verification Mismatch. Actual: "+val+" Expected: "+testcaseStep.getTestDataValue());
 					}
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the VerifyInnerHtmlEndsWith operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyInnerHtmlEndsWith -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyEditable(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					if(targetElement.getAttribute("readonly")==null){
 						exitCode=0;
 					}else{
 						exitCode=5;
 						testcaseStep.setErrDescription("The field is Non-Editable");
 					}
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the VerifyEditable operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyEditable -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyNonEditable(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					if(targetElement.getAttribute("readonly")!=null){
 						exitCode=0;
 					}else{
 						exitCode=5;
 						testcaseStep.setErrDescription("The field is Editable");
 					}
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the VerifyNonEditable operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyNonEditable -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int copyToClipBoard(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 				targetElement.sendKeys(Keys.LEFT_CONTROL + "a");
 				targetElement.sendKeys(Keys.LEFT_CONTROL + "c");
 				targetElement.sendKeys(Keys.ESCAPE);
 				exitCode=0;
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the CopyToClipBoard operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in CopyToClipBoard -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int pasteFromClipBoard(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 				targetElement.sendKeys(Keys.LEFT_CONTROL + "v");
 				exitCode=0;
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the PasteFromClipBoard operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in PasteFromClipBoards -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyEnabled(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					if(targetElement.isEnabled()==true){
 						exitCode=0;
 					}else{
 						exitCode=5;
 						testcaseStep.setErrDescription("The Element is Disabled");
 					}
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the VerifyEnabled operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyEnabled -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyDisabled(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					if(targetElement.isEnabled()==false){
 						exitCode=0;
 					}else{
 						exitCode=5;
 						testcaseStep.setErrDescription("The Element is Disabled");
 					}
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the VerifyDisabled operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyDisabled -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyTextNotEqual(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(targetElement.getText().trim().equals(testcaseStep.getTestDataValue())){
 							exitCode=5;
 							testcaseStep.setErrDescription("Text is Equal");
 						}
 						else{
 							exitCode=0;
 						}
 						
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyTextNotEqual operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyTextNotEqual -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyValueNotEqual(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().trim().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(targetElement.getAttribute("value").trim().equals(testcaseStep.getTestDataValue())){
 							exitCode=5;
 							testcaseStep.setErrDescription("Value is Equal");
 						}
 						else{
 							exitCode=0;
 						}
 						
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyValueNotEqual operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyValueNotEqual -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyOptionTextPresent(TestcaseStep testcaseStep) {
 		int exitCode = 0;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						List<WebElement> options = new Select(targetElement).getOptions();
 						for(WebElement we:options)
 						{
 							if((we.getText()).equals(testcaseStep.getTestDataValue()))
 							{
 								exitCode=0;
 								break;
 							}
 							else{
 								exitCode=5;
 								testcaseStep.setErrDescription("The Option: "+testcaseStep.getTestDataValue()+" is not present in the Selectbox");
 							}
 						}
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyOptionTextPresent operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyOptionTextPresent -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyOptionValuePresent(TestcaseStep testcaseStep) {
 		int exitCode = 0;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						List<WebElement> options = new Select(targetElement).getOptions();
 						for(WebElement we:options)
 						{
 							if((we.getAttribute("value")).equals(testcaseStep.getTestDataValue()))
 							{
 								exitCode=0;
 								break;
 							}
 							else{
 								exitCode=5;
 								testcaseStep.setErrDescription("The Option: "+testcaseStep.getTestDataValue()+" is not present in the Selectbox");
 							}
 						}
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyOptionValuePresent operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyOptionValuePresent -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyOptionsCount(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						int actOptions = this.getWebDriver().findElements(By.xpath(testcaseStep.getFieldDefinition())).size();
 						int expOptions = Integer.parseInt(testcaseStep.getTestDataValue());
 						if(actOptions==expOptions)
 							{
 								exitCode=0;
 							}
 							else{
 								exitCode=5;
 								testcaseStep.setErrDescription("Verification Mismatch. Actual: "+actOptions+" Expected: "+expOptions);
 							}
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyOptionsCount operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyOptionsCount -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyOptionTextNotPresent(TestcaseStep testcaseStep) {
 		int exitCode = 0;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						List<WebElement> options = new Select(targetElement).getOptions();
 						for(WebElement we:options)
 						{
 							if((we.getText()).equals(testcaseStep.getTestDataValue()))
 							{
 								exitCode=5;
 								testcaseStep.setErrDescription("The Option: "+testcaseStep.getTestDataValue()+" is present in the Selectbox");
 								break;
 							}
 							else{
 								exitCode=0;
 							}
 						}
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyOptionTextNotPresent operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyOptionTextNotPresent -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyOptionValueNotPresent(TestcaseStep testcaseStep) {
 		int exitCode = 0;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						List<WebElement> options = new Select(targetElement).getOptions();
 						for(WebElement we:options)
 						{
 							if((we.getAttribute("value")).equals(testcaseStep.getTestDataValue()))
 							{
 								exitCode=5;
 								testcaseStep.setErrDescription("The Option: "+testcaseStep.getTestDataValue()+" is not present in the Selectbox");
 								break;
 							}
 							else{
 								exitCode=0;
 							}
 						}
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyOptionValueNotPresent operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyOptionValueNotPresent -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifySelectedOption(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(new Select(targetElement).getFirstSelectedOption().getText().equals(testcaseStep.getTestDataValue())){
 							exitCode=0;
 						}
 						else{
 							exitCode=5;
 							testcaseStep.setErrDescription("Verification Mismatch. Actual: "+new Select(targetElement).getFirstSelectedOption().getText()+" Expected: "+testcaseStep.getTestDataValue());
 						}
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifySelectedOption operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifySelectedOption -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyMultiple(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(new Select(targetElement).isMultiple()==true){
 							exitCode=0;
 						}
 						else{
 							exitCode=5;
 							testcaseStep.setErrDescription("Not a Multiple Select box");
 						}
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyMultiple operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyMultiple -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyNotMultiple(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(new Select(targetElement).isMultiple()==false){
 							exitCode=0;
 						}
 						else{
 							exitCode=5;
 							testcaseStep.setErrDescription("It is a Multiple Select box");
 						}
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyNotMultiple operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyNotMultiple -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyElementPresent(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					if(targetElement.isDisplayed()==true){
 						exitCode=0;
 					}else{
 						exitCode=5;// verification failure
 						testcaseStep.setErrDescription("The Element is not Visible/present");
 					}
 				}catch(Exception e){
 					exitCode=1;
 					logger.error("Exception in performing the VerifyElementPresent operation - "+ e.getMessage());
 
 				}
 			}else{
 				exitCode = 3; // null object error
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyElementPresent -" +e.getMessage());
 			exitCode =4; // unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;		
 	}
 	public int verifyTextOnElement(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(targetElement.getText().trim().equals(testcaseStep.getTestDataValue())){
 							exitCode=0;
 						}
 						else{
 							exitCode=5;
 							testcaseStep.setErrDescription("Verification Mismatch. Actual: "+targetElement.getText()+" Expected: "+testcaseStep.getTestDataValue());
 						}
 						
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyTextOnElement operation - "+ e.getMessage());
 
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyTextOnElement -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;		
 	}
 	public int verifyPartialTextOnElement(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						if(targetElement.getText().contains(testcaseStep.getTestDataValue())){
 							exitCode=0;
 						}else{
 							exitCode=5;
 							testcaseStep.setErrDescription("Verification Mismatch. Actual: "+targetElement.getText()+" Expected: "+testcaseStep.getTestDataValue());
 						}
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyPartialTextOnElement operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyPartialTextOnElement -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;
 	}
 	public int verifyValue(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				if(testcaseStep.getTestDataValue().trim().equalsIgnoreCase("TESTDATA_NOTFOUND")==false){
 					try{
 						String actualValue =targetElement.getAttribute("value");
 						if(actualValue.trim().equals(testcaseStep.getTestDataValue())){
 							exitCode=0;
 							logger.info("Actual and Expected values are matched.  \n Actual Value::"+actualValue +
 									"\n Expected value::"+testcaseStep.getTestDataValue());
 						}
 						else{
 							exitCode=5;
 							logger.error("Actual and Expected values do not match. \n Actual Value::"+actualValue +
 									"\n Expected value::"+testcaseStep.getTestDataValue());
 							testcaseStep.setErrDescription("Actual and Expected values do not match. \n Actual Value::"+actualValue +
 									"\n Expected value::"+testcaseStep.getTestDataValue());
 						}
 						
 					}catch(Exception e){
 						exitCode=1; //Cannot perform operation
 						logger.error("Exception in performing the VerifyValue operation - "+ e.getMessage());
 					}
 				}else{
 					exitCode=2; //Testdata not found
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in VerifyValue -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;		
 	}
 	public int storeTextFromElement(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				String textFromElement = targetElement.getText().trim();
 				try{
 					if(testcaseStep.getRunTimeData().get(testcaseStep.getTestcaseID())==null){
 						Map<String, String>runtimeDatarecord = new HashMap<String, String>();
 						runtimeDatarecord.put(testcaseStep.getTestDataName(), textFromElement);
 						testcaseStep.getRunTimeData().put(testcaseStep.getTestcaseID(), runtimeDatarecord);
 						exitCode = 0;
 					}else{
						testcaseStep.getRunTimeData().get(testcaseStep.getTestcaseID()).put(testcaseStep.getTestcaseID(), textFromElement);
 						exitCode = 0;
 					}
 				}catch(Exception e){
 					exitCode=1;
 					logger.error("Exception in performing the StoreTextFromElement operation - "+ e.getMessage());
 					//e.printStackTrace();
 				}
 			}else{
 				exitCode=3;
 			}
 			
 		}catch(Exception e){
 			logger.info("Unknown Exception in StoreTextFromElement -" +e.getMessage());
 			//e.printStackTrace();
 			exitCode = 4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}		
 		return exitCode;
 	}
 	public int clearDataInElement(TestcaseStep testcaseStep) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 						targetElement.clear();
 						exitCode=0;
 				}catch(Exception e){
 					exitCode=1; //Cannot perform operation
 					logger.error("Exception in performing the ClearDataInElement operation - "+ e.getMessage());
 				}
 			}else{
 				exitCode = 3; //Element not found 
 			}
 		}catch(Exception e){
 			logger.info("Unknown Exception in ClearDataInElement -" +e.getMessage());
 			exitCode =4; //Unknown exception
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}
 		return exitCode;				
 	}
 
 
 
 	public WebDriver getWebDriver() {
 		return webDriver;
 	}
 	public void setWebDriver(WebDriver webDriver) {
 		this.webDriver = webDriver;
 	}
 	public void sendKeyboardInput(TestcaseStep testcaseStep) {
 		// TODO Auto-generated method stub
 		
 	}
 	public int storeAttributeValueFromElement(TestcaseStep testcaseStep,  String attributeName) {
 		int exitCode;
 		try{
 			WebElement targetElement = this.findElementByXpath(testcaseStep.getFieldDefinition());
 			if(targetElement!=null){
 				try{
 					String attValueFromElement = targetElement.getAttribute(attributeName).trim();
 					if(testcaseStep.getRunTimeData().get(testcaseStep.getTestcaseID().trim())==null){
 						Map<String, String>runtimeDatarecord = new HashMap<String, String>();
 						runtimeDatarecord.put(testcaseStep.getTestDataName(), attValueFromElement);
 						testcaseStep.getRunTimeData().put(testcaseStep.getTestcaseID(), runtimeDatarecord);
 						exitCode = 0;
 					}else{
 						testcaseStep.getRunTimeData().get(testcaseStep.getTestcaseID()).put(testcaseStep.getTestcaseID(), attValueFromElement);
 						exitCode = 0;
 					}
 					logger.info("Attribute value for " +attributeName + " for target element is stored in to runtimedata - "+attValueFromElement );
 				}catch(Exception e){
 					exitCode=1;
 					logger.error("Exception in performing the storeAttributeValueFromElement operation - "+ e.getMessage());
 					//e.printStackTrace();
 				}
 			}else{
 				exitCode=3;
 			}
 			
 		}catch(Exception e){
 			logger.error("Unknown Exception in storeAttributeValueFromElement -" +e.getMessage());
 			//e.printStackTrace();
 			exitCode = 4;
 		}
 		if(exitCode==0){
 			testcaseStep.setStepResult("PASS");
 		}else{
 			testcaseStep.setStepResult("FAIL");
 			testcaseStep.setErrorCode(exitCode);
 		}		
 		return exitCode;		
 	}
 
 		
 	
 	
 }
