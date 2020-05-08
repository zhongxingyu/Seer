 package agents.test;
 import java.util.*;
 
 
 import junit.framework.TestCase;
 import state.transducers.*;
 
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import agents.test.mock.*;
 
import state.FactoryState;
 
 import gui.GUI_GantryRobot;
 import gui.GUI_Lane;
 import gui.GUI_Nest;
 import gui.GUI_Part;
 import gui.GUI_Feeder;
 
 
 /**
  * 
  * Unit testing for Part C - FeederAgent
  *  
  * **/
 
 
 
 
 
 public class FeederAgentTest extends TestCase {
 	
 	/**
 	 * Tests the normative scenario of part passing between gantry and feeder
 	 * -->Requesting, queuing parts, passing when device ready
 	 * **/
 	public void testNormativePartPassing()
 	{
 		/*		Setup 		*/
 		
 		//Create Mock Agents
 		MockPartsMover mockPartsMover = new MockPartsMover("Parts Mover");
 		MockPartsMover top = new MockPartsMover("Top");
 		MockPartsMover bottom = new MockPartsMover("Bottom");
 		MockLane mockLane = new MockLane("Lane");
 		MockGantry mockGantry = new MockGantry("Gantry");
 		MockPart mockPart = new MockPart("Part");	
 		MockNest mockNest = new MockNest("Nest", null);
 		
 		//Create the feeder and set its part source
 		FeederAgent feeder = new FeederAgent("Feeder", top, bottom); 
 		feeder.msgSetSupplier(mockLane); //<----------- check this, should it be mock gantry? 
 		feeder.setGuiFeeder(new GUI_Feeder(feeder));
 		
 		//Check that the feeder has no requested parts
 		assertTrue("No parts should be requested", feeder.requested == 0);
 		assertTrue("No requests should be pending", feeder.pendingRequests.isEmpty());
 
 		//Make request to the feeder
		feeder.msgRequestParts("Dory", 10, 10, mockNest);
 		
 		//Check request is unprocessed but pending
 		assertTrue("Request unprocessed", feeder.requested == 0);
 		assertTrue("Request pending", feeder.pendingRequests.size() == 1);
 		
 		//Mock logs should still be empty up to this point
 		
 		
 		//Call scheduler to process the pending request
 		while (feeder.pickAndExecuteAnAction());
 
 		//Check that the feeder is correctly trying to fulfill the request
 		assertTrue("Original request remembered", feeder.requested == 10);
 		assertTrue("Holding nothing", feeder.holding == 0);
 //		assertTrue("Forwarded request correct", feeder.expecting == 10);
 		assertTrue("Correct part type set", feeder.currentPart.equals("Dory"));
 		
 		//Check that the request was forwarded to the mock feeder 
 		assertTrue("Request forwarded to feeder", mockGantry.log.containsString("Received msgRequestParts"));
 		
 		//Fill request as the feeder would, but only with 1 of the requested 10 parts
 		feeder.msgHereIsPart("Dory");
 		
 		//Check that we are now expecting 9, holding 1 parts
 //		assertTrue("Expecting 9 parts", feeder.expecting == 9);
 //		assertTrue("Holding 1 part", feeder.holding == 1);
 		
 		//Pass on 4 more parts
 		for (int i = 0; i < 4; i++)
 			feeder.msgHereIsPart("Dory");
 
 		//Check that we are now expecting 5, holding 5 parts
 //		assertTrue("Expecting 5 parts", feeder.expecting == 5);
 //		assertTrue("Holding 5 part", feeder.holding == 5);
 		
 		
 	}
 	
 	
 
 	
 	
 	/* Test Case: Non-norm Parts missing - feeder turned off or broken*/
 	public void testDefectiveFeeder()
 	{
 		/*		Setup 		*/
 		
 		//Create Mock Agents
 		MockPartsMover mockPartsMover = new MockPartsMover("Parts Mover");
 		MockPartsMover top = new MockPartsMover("Top");
 		MockPartsMover bottom = new MockPartsMover("Bottom");
 		MockLane mockLane = new MockLane("Lane");
 		MockGantry mockGantry = new MockGantry("Gantry");
 		MockPart mockPart = new MockPart("Part");	
 		MockNest mockNest = new MockNest("Nest", null);
 		
 		//Create the feeder and set its part source
 		FeederAgent feeder = new FeederAgent("Feeder", top, bottom); 
 		feeder.msgSetSupplier(mockLane); //<----------- check this, should it be mock gantry? 
 		feeder.setGuiFeeder(new GUI_Feeder(feeder));
 	
 	}
 	
 
 	
 	
 }
