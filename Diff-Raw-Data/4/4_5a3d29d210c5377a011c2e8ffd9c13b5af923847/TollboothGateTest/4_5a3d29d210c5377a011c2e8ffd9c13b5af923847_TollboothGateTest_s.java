 /*******************************************************************************
  * This files was developed for CS4233: Object-Oriented Analysis & Design. The course was
  * taken at Worcester Polytechnic Institute. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License
  * v1.0 which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package wpi.parking;
 
 import static org.junit.Assert.*;
 
 import java.util.concurrent.TimeUnit;
 
 import org.junit.*;
 
import wpi.parking.TollboothGate.TollboothGateState;
 import wpi.parking.hw.*;
 
 /**
  * Test cases for the TollboothGate class.
  *
  * @author gpollice (Initial implementation)
  * @author ndemarinis (Extensions for US4 and US5)
  * @version Jan 16, 2013
  */
 public class TollboothGateTest
 {
 	private TestGateController controller;
 	private TollboothGate gate;
 	private TollboothGate gateDelay2s;
 	
 	/**
 	 * Create the gate controller that we will use in the tests.
 	 */
 	@Before
 	public void setup() throws WPIPSException
 	{
 		controller = new TestGateController();
 		gate = new TollboothGate("gate", controller);
 		gateDelay2s = new TollboothGate("Delayed gate", controller, 2);
 	}
 	/**
 	 * Ensure that an initialized tollbooth gate is closed.
 	 * @throws WPIPSException
 	 */
 	@Test
 	public void initializedTollboothGateIsClosed() throws WPIPSException
 	{
 		assertNotNull(gate);
 		assertEquals(TollboothGate.TollboothGateState.CLOSED, gate.getState());
 	}
 
 	/**
 	 * A blank ID should cause an exception.
 	 * @throws WPIPSException
 	 */
 	@Test(expected=WPIPSException.class)
 	public void blankIDIsInvalid() throws WPIPSException
 	{
 		new TollboothGate("", controller);
 	}
 	
 	/**
 	 * A null ID should cause an exception.
 	 * @throws WPIPSException
 	 */
 	@Test(expected=WPIPSException.class)
 	public void nullIDIsInvalid() throws WPIPSException
 	{
 		new TollboothGate(null, controller);
 	}
 	
 	/**
 	 * Open a closed gate. This should make the gate's state OPEN.
 	 * @throws WPIPSException
 	 */
 	@Test
 	public void openAClosedGateShouldGiveAnOpenState() throws WPIPSException
 	{
 		assertEquals(TollboothGate.TollboothGateState.OPEN, gate.open());
 		assertEquals(TollboothGate.TollboothGateState.OPEN, gate.getState());
 	}
 	
 	/**
 	 * Close an open gate. This should make the gate's state CLOSED.
 	 * @throws WPIPSException
 	 */
 	@Test
 	public void closeAnOpenGateShouldGiveAClosedState() throws WPIPSException
 	{
 		gate.open();
 		assertEquals(TollboothGate.TollboothGateState.CLOSED, gate.close());
 		assertEquals(TollboothGate.TollboothGateState.CLOSED, gate.getState());
 	}
 	
 	/**
 	 * If there is an error in the gate controller hardware, then the gate should be
 	 * in an UNKNOWN state after closing.
 	 */
 	@Test
 	public void errorOnClosingCausesExceptionAndUnknownState()
 	{
 		controller.setCloseResults(new boolean[] {false});
 		TollboothGate nullGate = null;
 		try {
 			nullGate = new TollboothGate("gate1", controller);
 			nullGate.close();		// in case close is not called on initialization
 			fail("Expected gate controller exception");
 		} catch (WPIPSException e) {
 			assertEquals(TollboothGate.TollboothGateState.UNKNOWN, nullGate.getState());
 		}
 	}
 	
 	/**
 	 * If there is an error in the gate controller hardware, then the gate should be
 	 * in an UNKNOWN state after opening.
 	 */
 	@Test
 	public void errorOnOpeningCausesExceptionAndUnknownState()
 	{
 		controller.setOpenResults(new boolean[] {false});
 		TollboothGate failGate = null;
 		try {
 			failGate = new TollboothGate("gate1", controller);
 			failGate.open();
 			fail("Expected gate controller exception");
 		} catch (WPIPSException e) {
 			assertEquals(TollboothGate.TollboothGateState.UNKNOWN, failGate.getState());
 		}
 	}
 	
 /* More basic tests */
 	@Test
 	public void closingAGateIsIdempotent() throws WPIPSException 
 	{
 		// Fun fact:  This test taught me a new word!  WOOT!  
 		gate.open();
 		gate.close();
 		gate.close();
 		
 		assertEquals(TollboothGate.TollboothGateState.CLOSED, gate.getState());
 	}
 	
 /* US4:  Deactivating and reactivating a gate */
 	
 	@Test
 	public void initializedGateIsActivated() throws WPIPSException
 	{
 		assertTrue(TollboothGate.TollboothGateState.DEACTIVATED != gate.getState());
 	}
 	
 	@Test
 	public void deactivatingAGateShouldGiveADeactivatedState() throws WPIPSException
 	{
 		assertEquals(gate.deactivate(), TollboothGate.TollboothGateState.DEACTIVATED);
 		assertEquals(gate.getState(), TollboothGate.TollboothGateState.DEACTIVATED);
 	}
 	
 	@Test
 	public void activatingADectivatedGateShouldBeInClosedState() throws WPIPSException
 	{
 		gate.deactivate();
 		
 		assertEquals(gate.activate(), TollboothGate.TollboothGateState.CLOSED);
 		assertEquals(gate.getState(), TollboothGate.TollboothGateState.CLOSED);
 	}
 	
 	@Test(expected=WPIPSException.class)
 	public void activatingAnActivatedGateShouldThrowError() throws WPIPSException
 	{
 		gate.activate();
 	}
 	
 	@Test(expected=WPIPSException.class)
 	public void deactivatingADeactivatedGateShouldThrowError() throws WPIPSException
 	{
 		gate.deactivate();
 		
 		gate.deactivate();
 	}
 	
 	@Test(expected=WPIPSException.class)
 	public void ClosingADeactivatedGateThrowsError() throws WPIPSException
 	{
 		gate.deactivate();
 		
 		gate.close();
 	}
 	
 	@Test(expected=WPIPSException.class)
 	public void OpeningADeactivatedGateThrowsError() throws WPIPSException
 	{
 		gate.deactivate();
 		
 		gate.open();
 	}
 	
 /* US5:  Delayed close (this line does not contain code) */
 	
 	@Test
 	public void ICanInitializeAGateWithADelay() throws WPIPSException
 	{
 		final TollboothGate delayGate = new TollboothGate("gate", controller, 5);
		assertNotNull(gate);
 		
 	}
 	
 	@Test
 	public void AGateInitializedWithADelayClosesNSecondsAfterOpening() 
 			throws WPIPSException, InterruptedException
 	{
 		gateDelay2s.open();
 		assertEquals(TollboothGate.TollboothGateState.OPEN, gateDelay2s.getState());
 		
 		Thread.sleep(TimeUnit.SECONDS.toMillis(3)); // Add a second to avoid the race condition
 		assertEquals(TollboothGate.TollboothGateState.CLOSED, gateDelay2s.getState());
 	}
 	
 	@Test
 	public void DeactivatingAGateWithDelayCancelsTimer() 
 			throws WPIPSException, InterruptedException
 	{
 		gateDelay2s.open();
 		// Gate will close in two seconds
 		gateDelay2s.deactivate();
 		
 		assertEquals(TollboothGate.TollboothGateState.DEACTIVATED, gateDelay2s.getState());
 		
 		// If we wait out the delay (that shouldn't exist), the state shouldn't have changed
 		Thread.sleep(TimeUnit.SECONDS.toMillis(3));
 		assertEquals(TollboothGate.TollboothGateState.DEACTIVATED, gateDelay2s.getState());
 	}
 	
 	@Test
 	public void OpeningAnAlreadyOpenGateWithDelayExtendsTimer() 
 			throws WPIPSException, InterruptedException
 	{
 		final TollboothGate gateDelay4s = new TollboothGate("4s Delayed Gate", 
 				controller, 4);
 		
 		gateDelay4s.open(); // Open gate for three seconds 
 		
 		// Wait a bit, but continue when the previous timer is running
 		Thread.sleep(TimeUnit.SECONDS.toMillis(2));
 		gateDelay4s.open();  // Open gate for another two seconds
 		
 		// Check the state after the first timer completes, but not the second.  
 		// It should still be open because the new timer cancelled the old one
 		Thread.sleep(TimeUnit.SECONDS.toMillis(2));  
 		assertEquals(TollboothGate.TollboothGateState.OPEN, gateDelay4s.getState());
 		
 		// The gate should be closed after the second timer has completed
 		Thread.sleep(TimeUnit.SECONDS.toMillis(3)); 
 		assertEquals(TollboothGate.TollboothGateState.CLOSED, gateDelay4s.getState());
 	}
 	
 	@Test
 	public void weCanReactivateADeactivatedDelayedGateAndUseIt() 
 			throws WPIPSException, InterruptedException
 	{
 		gateDelay2s.open();
 		gateDelay2s.deactivate();
 		gateDelay2s.activate();
 		
 		gateDelay2s.open();
 		assertEquals(TollboothGate.TollboothGateState.OPEN, gateDelay2s.getState());
 		
 		Thread.sleep(TimeUnit.SECONDS.toMillis(3)); 
 		assertEquals(TollboothGate.TollboothGateState.CLOSED, gateDelay2s.getState());
 	}
 }
