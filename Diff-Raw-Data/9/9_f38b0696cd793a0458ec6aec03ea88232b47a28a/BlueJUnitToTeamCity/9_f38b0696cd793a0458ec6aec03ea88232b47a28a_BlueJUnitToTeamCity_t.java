 import java.io.*;
 //import java.util.*;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.CharacterData;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 public class BlueJUnitToTeamCity {
 	public int numTestsRun;
 	public int numFailuresOcc;
 	
 	public BlueJUnitToTeamCity () {
 		this.numTestsRun = 0;
 		this.numFailuresOcc = 0;
 	}
 	
 	public String replaceTeamCityEscape(String message){
 		return message.replace("|", "||")
                 .replace("'", "|'")
                 .replace("\n", "|n")
                 .replace("\r", "|r")
                 //.replace(/\u0085/g, "|x")
                 //.replace(/\u2028/g, "|l")
                 //.replace(/\u2029/g, "|p")
                 .replace("[", "|[")
                 .replace("]", "|]");
 
 	}
 	
 	public void printTeamCityTestReport(String blueJUnitTestResult){
 		try {
 	        DocumentBuilderFactory dbf =
 	            DocumentBuilderFactory.newInstance();
 	        DocumentBuilder db = dbf.newDocumentBuilder();
 	        InputSource is = new InputSource();
 	        is.setCharacterStream(new StringReader(blueJUnitTestResult));
 
 	        Document doc = db.parse(is);
 	        NodeList testSuitesList = doc.getElementsByTagName("testsuites");
 
 	        // iterate the employees
 	        for (int i = 0; i < testSuitesList.getLength(); i++) {
 	           Element testSuites = (Element) testSuitesList.item(i);
 	           NodeList testSuiteList = testSuites.getElementsByTagName("testsuite");
 	           for(int k = 0; k< testSuiteList.getLength(); k++){
 	        	   Element testSuite = (Element) testSuiteList.item(k);
 		           //System.out.println(testSuite.toString());
 		           int numTests = Integer.parseInt(testSuite.getAttribute("tests"));
 		           this.numTestsRun += numTests;
 		           if(numTests <= 0) {continue;}
 		           else {
 		        	   int numFailures = Integer.parseInt(testSuite.getAttribute("failures"));
 		        	   this.numFailuresOcc += numFailures;
 		        	   double second = Double.parseDouble(testSuite.getAttribute("time"));
 		        	   String testSuiteName = this.replaceTeamCityEscape(testSuite.getAttribute("name"));
 		        	   System.out.println("##teamcity[testSuiteStarted name='"+ testSuiteName +"']");
 		        	   //process testcases
 		        	   NodeList testCases = testSuite.getElementsByTagName("testcase");
 		        	   for(int j = 0; j < testCases.getLength(); j++){
 		        		   Element testCase = (Element) testCases.item(j);
 		        		   this.printTeamCityTestCase(testCase);
 		        	   }
 		        	   
		        	   System.out.println("##teamcity[testSuiteFinished name='"+ testSuiteName +"' duration='"+ second*1000 +"']");
 		        
 	           		}
 	           }
 	        }
 	    }
 	    catch (Exception e) {
 	        e.printStackTrace();
 	    }
 
 	}
 	
 	private void printTeamCityTestCase(Element testCase){
 		String testCaseName = this.replaceTeamCityEscape(testCase.getAttribute("name"));
 		System.out.println("##teamcity[testStarted name='"+testCaseName+"' captureStandardOutput='true']");
 		NodeList failure = testCase.getElementsByTagName("failure");
 		if(failure.getLength()>0){
 			String failureDetail = this.getCharacterDataFromElement((Element)failure.item(0));
 			System.out.println("##teamcity[testFailed name='" + testCaseName + "' message='|[FAILED|]' details='" + failureDetail + "']");
 		}
 		System.out.println("##teamcity[testFinished name='"+testCaseName+"' captureStandardOutput='true']");
 
 	}
 	
 	public String getCharacterDataFromElement(Element e) {
 	    Node child = e.getFirstChild();
 	    if (child instanceof CharacterData) {
 	       CharacterData cd = (CharacterData) child;
 	       return cd.getData();
 	    }
 	    return "?";
 	  }
 	
 }
