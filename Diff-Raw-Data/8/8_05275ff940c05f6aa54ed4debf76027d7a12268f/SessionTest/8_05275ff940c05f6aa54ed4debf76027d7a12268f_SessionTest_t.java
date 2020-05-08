 package com.dobby.tests.core;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import com.dobby.core.Request;
 import com.dobby.core.Session;
 import com.dobby.core.StateVector;
 import com.dobby.core.requests.DeleteRequest;
 import com.dobby.core.requests.InsertRequest;
 
 public class SessionTest {
 
 	@Test
 	public void testLog() {
 		Session testSession = new Session("Test", "TestDoc");
 		Request testRequest = new InsertRequest("Test", new StateVector(), 0,
 				0, 'a');
 		testSession.receiveRequest(testRequest);
 		testSession.getRequest("Test", 0);
 	}
 
 	@Test
 	public void testReceiveRequest() {
 		Session testSession = new Session("Test", "TestDoc");
 		Request testRequesta = new InsertRequest("Test", new StateVector(), 0,
 				0, 'a');
 		testSession.receiveRequest(testRequesta);
 		assertTrue(!testSession.isRequestQueueEmpty());
 		assertTrue(testSession.getRequest("Test", 0).equals(testRequesta));
 	}
 
 	@Test
 	public void testExecuteRequest() {
 		Session testSession = new Session("User1", "TestDoc");
 		Request testRequestb = new InsertRequest("Test", new StateVector(), 0,
 				0, 'a');
 		testSession.receiveRequest(testRequestb);
 		testSession.executeRequest();
 		assertTrue(testSession.getCurrentText().equals("a"));
 	}
 
 	@Test
 	public void testTranslateRequest() {
 		Session testSessionx = new Session("User1", "TestDoc");
 		StateVector testVectors = new StateVector();
 		testSessionx.setCurrentState(testVectors);
 		Request testRequest = new InsertRequest("User1", testVectors, 0, 0, 'x');
 		testSessionx.receiveRequest(testRequest);
 		Request testRequest2 = new InsertRequest("User1",
 				testVectors.incrementedUser("User1"), 1, 1, 'b');
 		Request testRequest3 = new InsertRequest("User1", testRequest2
 				.getStateVector().incrementedUser("User1"), 2, 2, 'y');
 		testSessionx.receiveRequest(testRequest2);
 		testSessionx.receiveRequest(testRequest3);
 		testSessionx.executeRequest();
 		// stores a state vector after this request
 		// will be used to simulate User2 that gets left behind
 		StateVector laggyTestVector = testVectors.clone();
 		testSessionx.executeRequest();
 		assertTrue(testSessionx.getCurrentText().equals("xb"));
 
 		Request testRequest4 = new InsertRequest("User2", laggyTestVector, 3,
 				0, 'w');
 		testSessionx.receiveRequest(testRequest4);
 		testSessionx.executeRequest();
 		System.out.println(testSessionx.getCurrentText());
 		testSessionx.executeRequest();
 		System.out.println(testSessionx.getCurrentText());
 		assertTrue(testSessionx.getCurrentText().equals("wxby"));
 
 		Request testRequest5 = new InsertRequest("User3", testSessionx
 				.getCurrentState().clone(), 5, 1, 'r');
 		testSessionx.receiveRequest(testRequest5);
 		testSessionx.executeRequest();
 		System.out.println(testSessionx.getCurrentText());
 		assertTrue(testSessionx.getCurrentText().equals("wrxby"));
 
 		Request testRequest6 = new InsertRequest("User6",
 				testRequest3.getStateVector(), 5, 1, 'a');
 		testSessionx.receiveRequest(testRequest6);
 		testSessionx.executeRequest();
 		System.out.println(testSessionx.getCurrentText());
 		assertTrue(testSessionx.getCurrentText().equals("wrxaby"));
 		
 		Request testRequest7 = new DeleteRequest("User2", 
 				testSessionx.getCurrentState().clone(), 6, 1, 'r');
 		testSessionx.receiveRequest(testRequest7);
 		testSessionx.executeRequest();
 		System.out.println(testSessionx.getCurrentText());
 		assertTrue(testSessionx.getCurrentText().equals("wxaby"));
 		
 		Request testRequest8 = new DeleteRequest("User2",
 				testRequest6.getStateVector().incrementedUser("User2"), 7, 1, 'r');
 		testSessionx.receiveRequest(testRequest8);
 		testSessionx.executeRequest();
 		System.out.println(testSessionx.getCurrentText());
 	}
 
 	@Test
 	public void testGetRequest() {
 		Session testSession = new Session("Test", "TestDoc");
 		Request testRequestc = new InsertRequest("Test", new StateVector(), 0,
 				0, 'a');
 		testSession.receiveRequest(testRequestc);
 		assertTrue(testSession.getRequest("Test", 0).equals(testRequestc));
 	}
 
 	@Test
 	public void testReachable() {
 		Session testSession = new Session("User1", "TestDoc");
 		StateVector testVector = new StateVector();
 		assertTrue(testSession.reachable(testVector));
 		Request testRequestd = new InsertRequest("Test", testVector, 0, 0, 'a');
 		testSession.receiveRequest(testRequestd);
 		testSession.executeRequest();
 		assertTrue(testSession.reachable(new StateVector()));
 		Request e = new InsertRequest("Test2",
 				testVector.incrementedUser("Test"), 0, 1, 'a');
 		testSession.receiveRequest(e);
 		testSession.executeRequest();
 		assertTrue(testSession.reachable(testVector.incrementedUser("Test")));
 	}
 }
