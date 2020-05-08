 package agents.test;
 import java.util.*;
 
 
 import junit.framework.TestCase;
 import state.transducers.*;
 
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import agents.test.mock.*;
 
import state.FactoryState;
 
 import gui.GUI_Lane;
 import gui.GUI_Nest;
 import gui.GUI_Part;
 import gui.GUI_GantryRobot;
 
 
 /**
  * 
  * Unit testing for Part C - GantryAgent
  *  
  * **/
 
 public class GantryAgentTest extends TestCase{
 
 	
 	/**
 	 * Tests the normative scenario of the gantry getting multiple requests from feeders and filling them
 	 * -->Requesting, queuing parts, passing when device ready
 	 * **/
 	public void testNormativeQueuingAndBinDumping()
 	{
 		/*		Setup 		*/
 		
 		//Create Mock Agents
 		MockFeeder mockFeeder1 = new MockFeeder("Feeder1");
 		MockFeeder mockFeeder2 = new MockFeeder("Feeder2");
 		
 		//Create the gantry and set its part source
 		GantryAgent gantry = new GantryAgent("Gantry");
 		gantry.setGuiGantry(new GUI_GantryRobot(gantry));
 		
 		/*		Test passing all	*/
 		
 		//Check that the gantry has no requested parts
 		assertTrue("No parts should be requested", gantry.requested == 0);
 		assertTrue("No requests should be pending", gantry.pendingRequests.isEmpty());
 		
 		//Make two requests to the gantry
		gantry.msgRequestParts("PartA", 10, 10, mockFeeder1);
		gantry.msgRequestParts("PartB", 10, 10, mockFeeder2);
 		
 		//Check requests are unprocessed but pending
 		assertTrue("Requests unprocessed", gantry.requested == 0);
 		assertTrue("Requests pending", gantry.pendingRequests.size() == 2);
 		
 		//Mock logs should still be empty up to this point
 		assertTrue("Feeder1 log still empty", mockFeeder1.log.size() == 0);
 		assertTrue("Feeder2 log still empty", mockFeeder2.log.size() == 0);
 		
 		//Call scheduler to process the first pending request
 		while(gantry.pickAndExecuteAnAction());
 
 		//Check that the gantry is correctly trying to fulfill the request to feeder 1
 		assertTrue("Original request remembered", gantry.requested == 10);
 		assertTrue("Holding bin", gantry.holding == 10);
 		assertTrue("Correct part type set", gantry.currentPart.equals("PartA"));
 		
 		//Check that the gantry is waiting for the gui
 		assertTrue("Ready to pass one part", !gantry.readyToMove);
 		
 		//Pass on all items as the bin is dumped
 		for (int i = 0; i < 10; i++)
 		{
 			gantry.msgReadyToMove();
 			while (gantry.pickAndExecuteAnAction());
 			
 			//Check that feeder receives each part
 	//		assertTrue((i + 0) + " parts received messages in nest", mockFeeder1.log.size() == i+0);
 	//		assertTrue("Latest message is of received part", mockFeeder1.log.getLastLoggedEvent().toString().contains("msgHereIsPart"));
 		}
 		
 		//Request completely filled, nothing requested, holding, or expected
 		assertTrue("No more requested", gantry.requested == 0);
 		assertTrue("Passed everything", gantry.holding == 0);
 		assertTrue("No more expected", gantry.expecting == 0);
 		
 	}
 	
 }
