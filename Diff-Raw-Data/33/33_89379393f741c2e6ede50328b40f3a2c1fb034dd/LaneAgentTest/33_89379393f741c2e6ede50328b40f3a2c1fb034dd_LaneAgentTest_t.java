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
 
 /**
  * 
  * Unit testing for Part C - LaneAgent
  * Tests the normative scenarios:
  * - Part passing
  * Tests the non-normative scenarios:
  * - Backed up parts purged upon request for a different part type 
  * - Request too large for the lane, request maximum, and continue as space opens
  * **/
 
 
 public class LaneAgentTest extends TestCase{
 
 	/** Test Case: Normative Scenario
 	 * -->Requesting, queuing parts, passing when device ready 
 	 * */
 	public void testNormativePartPassing()
 	{
 		/*		Setup 		*/
 		
 		//Create Mock Agents
 		MockFeeder mockFeeder = new MockFeeder("Feeder");
 		MockNest mockNest = new MockNest("Nest", null);
 		MockPartsMover mockPartsMover = new MockPartsMover("Parts Mover");
 		
 		//Create the lane and set its part source
 		LaneAgent lane = new LaneAgent("Lane");
 		lane.msgSetSupplier(mockFeeder);
 		lane.setguiLane(new GUI_Lane(lane));
 		
 		/*		Test passing all	*/
 		
 		//Check that the lane has no requested parts
 		assertTrue("No parts should be requested", lane.requested == 0);
 		assertTrue("No requests should be pending", lane.pendingRequests.isEmpty());
 		
 		//Make request to the lane
		lane.msgRequestParts("PartA", 10, 10, mockNest);
 		
 		//Check request is unprocessed but pending
 		assertTrue("Request unprocessed", lane.requested == 0);
 		assertTrue("Request pending", lane.pendingRequests.size() == 1);
 		
 		//Mock logs should still be empty up to this point
 		assertTrue("Feeder log still empty", mockFeeder.log.size() == 0);
 		assertTrue("Nest log still empty", mockNest.log.size() == 0);
 		
 		//Call scheduler to process the pending request
 		while(lane.pickAndExecuteAnAction());
 
 		//Check that the lane is correctly trying to fulfill the request
 		assertTrue("Original request remembered", lane.requested == 10);
 		assertTrue("Holding nothing", lane.holding == 0);
 		assertTrue("Forwarding request correct", lane.expecting == 10);
 		assertTrue("Correct part type set", lane.currentPart.equals("PartA"));
 		
 		//Check that the request was forwarded to the mock feeder 
 		assertTrue("Request forwarded to feeder", mockFeeder.log.containsString("Received msgRequestParts"));
 		
 		//Fill request as the feeder would, but only with 1 of the requested 10 parts
 		lane.msgHereIsPart("PartA");
 		
 		//Check that we are now expecting 9, holding 1 parts
 		assertTrue("Expecting 9 parts", lane.expecting == 9);
 		assertTrue("Holding 1 part", lane.holding == 1);
 		
 		//Pass on 4 more parts
 		for (int i = 0; i < 4; i++)
 			lane.msgHereIsPart("PartA");
 
 		//Check that we are now expecting 5, holding 5 parts, these parts are blocked
 		assertTrue("Expecting 5 parts", lane.expecting == 5);
 		assertTrue("Holding 5 part", lane.holding == 5);
 
 		//Check that the lane is waiting for the gui
 		assertTrue("Ready to pass one part", !lane.readyToMove);
 		
 		//Test passing on 1 part on before receiving more parts that fill the original request
 		lane.msgReadyToMove();
 		
 		//Check that the lane is ready to pass one part
 		assertTrue("Ready to pass one part", lane.readyToMove);
 		
 		//Last point at which the log of the receiver is empty
 		assertTrue("Receiver still empty", mockNest.log.size() == 0);
 		
 		//Pick and execute so the part is passed on
 		while (lane.pickAndExecuteAnAction());
 		
 		//Check that the nest received a part
 		assertTrue("Nest received a part", mockNest.log.containsString("Received msgHereIsPart"));
 		
 		//Should now be holding 1 fewer parts (4), now 9 requested, still expecting 5
 		assertTrue("Requested decremented", lane.requested == 9);
 		assertTrue("Holding 1 fewer", lane.holding == 4);
 		assertTrue("Forwarding request correct", lane.expecting == 5);
 		
 		//Finish filling feeder request
 		for (int i = 0; i < 5; i++)
 			lane.msgHereIsPart("PartA");
 		
 		//Process like previous requests
 		while (lane.pickAndExecuteAnAction());
 		
 		//Should now have request filled with request and holding 9, expecting 0
 		assertTrue("Requested decremented", lane.requested == 9);
 		assertTrue("Holding 1 fewer", lane.holding == 9);
 		assertTrue("Forwarding request correct", lane.expecting == 0);
 		
 		//Pass on all items as the device would allow
 		for (int i = 0; i < 9; i++)
 		{
 			lane.msgReadyToMove();
 			while (lane.pickAndExecuteAnAction());
 		}
 		
 		//Nest received all items, 10 messages
 		assertTrue("10 parts received messages in nest", mockNest.log.size() == 10);
 		
 		//Request completely filled, nothing requested, holding, or expected
 		assertTrue("No more requested", lane.requested == 0);
 		assertTrue("Passed everything", lane.holding == 0);
 		assertTrue("No more expected", lane.expecting == 0);
 		
 	}
 	
 	/* Test Case: Backed up parts purged upon request for a different part type */
 	public void testPurge()
 	{
 		/*		Setup 		*/
 		
 		//Create Mock Agents
 		MockFeeder mockFeeder = new MockFeeder("Feeder");
 		MockNest mockNest = new MockNest("Nest", null);
 		MockPartsMover mockPartsMover = new MockPartsMover("Parts Mover");
 		
 		//Create the lane and set its part source
 		LaneAgent lane = new LaneAgent("Lane");
 		lane.msgSetSupplier(mockFeeder);
 		lane.setguiLane(new GUI_Lane(lane));
 		
 		/*		Test passing 3 of 10, then purging and filling req for a different part type 	*/
 		
 		//Check that the lane has no requested parts
 		assertTrue("No parts should be requested", lane.requested == 0);
 		assertTrue("No requests should be pending", lane.pendingRequests.isEmpty());
 		
 		//Make request to the lane
		lane.msgRequestParts("PartA", 10, 10, mockNest);
 		
 		//Check request is unprocessed but pending
 		assertTrue("Request unprocessed", lane.requested == 0);
 		assertTrue("Request pending", lane.pendingRequests.size() == 1);
 		
 		//Mock logs should still be empty up to this point
 		assertTrue("Feeder log still empty", mockFeeder.log.size() == 0);
 		assertTrue("Nest log still empty", mockNest.log.size() == 0);
 		
 		//Call scheduler to process the pending request
 		while(lane.pickAndExecuteAnAction());
 
 		//Check that the lane is correctly trying to fulfill the request
 		assertTrue("Original request remembered", lane.requested == 10);
 		assertTrue("Holding nothing", lane.holding == 0);
 		assertTrue("Forwarding request correct", lane.expecting == 10);
 		assertTrue("Correct part type set", lane.currentPart.equals("PartA"));
 		
 		//Check that the request was forwarded to the mock feeder 
 		assertTrue("Request forwarded to feeder", mockFeeder.log.containsString("Received msgRequestParts"));
 		
 		//Fill request as the feeder would, but only with 1 of the requested 10 parts
 		lane.msgHereIsPart("PartA");
 		
 		//Check that we are now expecting 9, holding 1 parts
 		assertTrue("Expecting 9 parts", lane.expecting == 9);
 		assertTrue("Holding 1 part", lane.holding == 1);
 		
 		//Pass on 2 more parts
 		for (int i = 0; i < 2; i++)
 			lane.msgHereIsPart("PartA");
 
 		//Check that we are now expecting 7, holding 3 parts, these parts are blocked
 		assertTrue("Expecting 5 parts", lane.expecting == 7);
 		assertTrue("Holding 5 part", lane.holding == 3);
 
 		//Check that the lane is waiting for the gui
 		assertTrue("Ready to pass one part", !lane.readyToMove);
 		
 		//Test passing on 1 part on before receiving the request that will cause a purge
 		lane.msgReadyToMove();
 		
 		//Check that the lane is ready to pass one part
 		assertTrue("Ready to pass one part", lane.readyToMove);
 		
 		//Last point at which the log of the receiver is empty
 		assertTrue("Receiver still empty", mockNest.log.size() == 0);
 		
 		//Pick and execute so the part is passed on
 		while (lane.pickAndExecuteAnAction());
 		
 		//Check that the nest received a part
 		assertTrue("Nest received a part", mockNest.log.containsString("Received msgHereIsPart"));
 		
 		//Should now be holding 1 fewer parts (2), now 9 requested, still expecting 7
 		assertTrue("Requested decremented", lane.requested == 9);
 		assertTrue("Holding 1 fewer", lane.holding == 2);
 		assertTrue("Forwarding request correct", lane.expecting == 7);
 		
 		//Now make a new request from downstream for a different part type so we must purge
		lane.msgRequestParts("PartB", 10, 10, mockNest);
 		
 		/*	 PURGE	 */
 		
 		//Run scheduler so it purges and passes on new request
 		while (lane.pickAndExecuteAnAction());
 		
 		//Ensure the feeder got the new request
 		assertTrue("Feeder received revised request", mockFeeder.log.getLastLoggedEvent().toString().contains("msgRequestParts"));
 		
 		//Ensure the lane was purged and the lane is attempting to fulfill the new request
 		assertTrue("Part type changed to B", lane.currentPart.equals("PartB"));
 		assertTrue("Request updated to 10", lane.requested == 10);
 		assertTrue("Holding nothing since we purged", lane.holding == 0);
 		assertTrue("Expecting 10 of the new part type", lane.requested == 10);
 		
 		//Fill the whole request, also testing total backup
 		for (int i = 0; i < 10; i++)
 			lane.msgHereIsPart("PartB");
 		
 		//Process like previous requests
 		while (lane.pickAndExecuteAnAction());
 		
 		//Should now have request filled with request and holding 10, expecting 0
 		assertTrue("Requested value maintained", lane.requested == 10);
 		assertTrue("Holding 10", lane.holding == 10);
 		assertTrue("No more parts expected", lane.expecting == 0);
 		
 		//Pass on all items as the device would allow
 		for (int i = 0; i < 10; i++)
 		{
 			lane.msgReadyToMove();
 			while (lane.pickAndExecuteAnAction());
 		}
 		
 		//Nest received all items 11 messages
 		assertTrue("11 parts received messages in nest", mockNest.log.size() == 11);
 		
 		//Request completely filled, nothing requested, holding, or expected
 		assertTrue("Requested filled", lane.requested == 0);
 		assertTrue("Holding none", lane.holding == 0);
 		assertTrue("No more parts expected", lane.expecting == 0);
 	}
 	
 	
 }
 
