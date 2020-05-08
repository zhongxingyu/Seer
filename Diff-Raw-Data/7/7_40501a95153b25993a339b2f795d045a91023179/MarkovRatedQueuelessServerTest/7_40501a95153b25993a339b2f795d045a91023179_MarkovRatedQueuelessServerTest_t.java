 package com.harcourtprogramming.markov;
 
 import java.util.Set;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * Test which models a queueless server, at which arrive at a rate lambda
  * = 0.5, and are completed at a rate mu = 1.0. The server has no queue, so
  * arrivals that occur whilst the system is still processing are ignored - thus
  * this system only have two states - processing and not processing a job, which
  * are being identified with the strings "Working" and "Ready"
  *
  * With the given rates, it should be obvious that the overall analysis will be
  * that the system spend 1/3 of the time in the "Working" state and 2/3 of the
  * time in the "Ready" state.
  * @author Benedict
  */
 public class MarkovRatedQueuelessServerTest
 {
 	private final static float LAMBDA = (float)0.5;
 	private final static float MU = (float)1;
 
 	private final static String STATE0 = "Working";
 	private final static String STATE1 = "Ready";
 
 	public MarkovRatedQueuelessServerTest()
 	{
		// Nothing to see here. Move along, citizen!
 	}
 
 	MarkovBuilder<String> createSemaphor()
 	{
 		MarkovBuilder<String> builder = new MarkovBuilder<String>();
 		assertNotNull("Unable to create a Markov Builder", builder);
 
 		// Add the arrival transition
 		try
 		{
 			builder.addRateTransition(STATE0, STATE1, LAMBDA);
 		}
 		catch (Throwable e)
 		{
			fail("Error whilst attempt to add transition from " + STATE0 + " to " + STATE1 + ": " + e.toString());
 		}
 
 		// Add the completion transition
 		try
 		{
 			builder.addRateTransition(STATE1, STATE0, MU);
 		}
 		catch (Throwable e)
 		{
			fail("Error whilst attempt to add transition from " + STATE1 + " to " + STATE0 + ": " + e.toString());
 		}
 
 		return builder;
 	}
 
 	@Test
 	public void createProcessBuilder()
 	{
 		assertNotNull(new MarkovBuilder<String>());
 	}
 
 	@Test
 	public void confirmStateExistence()
 	{
 		MarkovBuilder<String> builder = createSemaphor();
 
 		Set<String> states = builder.getStates();
 
 		assertNotNull("State list was null", states);
 		assertEquals("Set of states was wrong size", 2, states.size());
 		assertTrue(STATE0 + " was not in the set of states", states.contains(STATE0));
 		assertTrue(STATE1 + " was not in the set of states", states.contains(STATE1));
 	}
 
 	@Test
 	public void confirmTransistionExistence()
 	{
 		MarkovBuilder<String> builder = createSemaphor();
 		assertTrue("Transition is not reported from " + STATE0 + " to " + STATE1, builder.transistionExists(STATE0, STATE1));
 		assertTrue("Transition is not reported from " + STATE1 + " to " + STATE0, builder.transistionExists(STATE1, STATE0));
 	}
 
 }
