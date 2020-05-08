 package org.denevell.briefwebserver;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class BriefWebServerTests {
 
 	@Test
 	public void serverStartsOkay() {
 		try {
 			//Arrange
 			BriefWebServer bws = new BriefWebServer("localhost", 8880);
 			
 			//Act
 			bws.stop();
 			Assert.assertTrue(true);
 		} catch(Exception e) {
 			Assert.assertTrue("Server didn't start and stop without error", false);
 		}
 		//Assert
 	}
 	
 	@Test
 	public void exceptionOnNoContexts() throws IOException {
 		//Arrange
 		BriefWebServer bws = null;
 		try {
 			bws = new BriefWebServer("localhost", 8882);
 		
 			//Act
 			URL url = new URL("http://localhost:8882/");
 			url.openStream();
 			//Assert
 			Assert.assertFalse("Excepted exception when no contexts given", true);
 		} catch(FileNotFoundException e) {
 			//Good
 		} finally {
 			if(bws!=null) bws.stop();
 		}
 	}
 	
 	@Test
 	public void getSimpleString() throws IOException {
 		//Arrange
 		BriefWebServer bws = new BriefWebServer("localhost", 8882);
 		bws.addStub("/", "hiya");
 		
 		//Act
 		URL url = new URL("http://localhost:8882/");
 		try {
 			BufferedReader result = new BufferedReader(new InputStreamReader(url.openStream()));
 			//Assert
 			Assert.assertNotNull(result);
 			Assert.assertEquals("hiya", result.readLine());
 		} catch(Exception e) {
 			Assert.assertFalse("Unexpected exception", true);
 		} finally {
 			bws.stop();
 		}
 	}
 	
 	@Test
 	public void getFromDifferentURLs() throws IOException {
 		//Arrange
 		BriefWebServer bws = new BriefWebServer("localhost", 8883);
 		bws.addStub("/one", "hiya");
 		bws.addStub("/two", "there");
 		
 		//Act
 		try {
 			URL url = new URL("http://localhost:8883/one");
 			BufferedReader result = new BufferedReader(new InputStreamReader(url.openStream()));
 			//Assert
 			Assert.assertNotNull(result);
 			Assert.assertEquals("hiya", result.readLine());
 			
 			url = new URL("http://localhost:8883/two");
 			result = new BufferedReader(new InputStreamReader(url.openStream()));
 			//Assert
 			Assert.assertNotNull(result);
 			Assert.assertEquals("there", result.readLine());
 		} catch(Exception e) {
 			Assert.assertFalse("Unexpected exception", true);
 		} finally {
 			bws.stop();
 		}
 	}
 	
 	@Test
 	public void enterIncorrectURLThrowsExpection() throws IOException {
 		//Arrange
 		BriefWebServer bws = new BriefWebServer("localhost", 8884);
 		bws.addStub("/thing", "hiya");
 		
 		//Act
 		URL url = new URL("http://localhost:8884/WRONG");
 		try {
 			new BufferedReader(new InputStreamReader(url.openStream()));
 			Assert.assertTrue("Exception expected", false);
 		} catch(FileNotFoundException e) {
 			//Good
 		} finally {
 			bws.stop();
 		}
 	} 
 	
 	@Test
 	public void getMultilineString() throws IOException {
 		//Arrange
 		BriefWebServer bws = new BriefWebServer("localhost", 8882);
 		bws.addStub("/", "one\ntwo");
 		
 		//Act
 		URL url = new URL("http://localhost:8882/");
 		try {
 			BufferedReader result = new BufferedReader(new InputStreamReader(url.openStream()));
 			//Assert
 			Assert.assertNotNull(result);
 			Assert.assertEquals("one", result.readLine());
 			Assert.assertEquals("two", result.readLine());
 			Assert.assertEquals(null, result.readLine());
 		} catch(Exception e) {
 			Assert.assertFalse("Unexpected exception", true);
 		} finally {
 			bws.stop();
 		}
 	}
 	
 	@Test
 	public void getMultilineStringViaHelper() throws IOException {
 		//Arrange
 		BriefWebServer bws = new BriefWebServer("localhost", 8882);
 		bws.addStub("/thing", "one\ntwo");
 		
 		//Act
 		try {
 			String result = bws.getStringFromContext("thing");
 			//Assert
 			Assert.assertNotNull(result);
 			Assert.assertEquals("one\ntwo", result);
 		} catch(Exception e) {
 			Assert.assertFalse("Unexpected exception: " + e, true);
 		} finally {
 			bws.stop();
 		}
 	}
 	
 	@Test
 	public void getExceptionFromViaGetInputHelperWhenCompleteBadPath() throws IOException {
 		//Arrange
 		BriefWebServer bws = new BriefWebServer("localhost", 8882);
 		bws.addStub("/thing", "one\ntwo");
 		
 		//Act
 		try {
 			String s  = bws.getStringFromContext("WRONG");
 			Assert.assertFalse("Expected exception: " + s, true);
 		} catch(Exception e) {
 			//Assert
 			//Good
 		} finally {
 			bws.stop();
 		}
 	}
 	
 	@Test
 	public void getExceptionFromViaGetInputHelperWhenPartiallyBadPath() throws IOException {
 		//Arrange
 		BriefWebServer bws = new BriefWebServer("localhost", 8882);
 		bws.addStub("/thing", "one\ntwo");
 		
 		//Act
 		try {
 			String s  = bws.getStringFromContext("thingx");
 			Assert.assertFalse("Expected exception: " + s, true);
 		} catch(Exception e) {
 			//Assert
 			//Good
 		} finally {
 			bws.stop();
 		}
 	}
 	
 }
