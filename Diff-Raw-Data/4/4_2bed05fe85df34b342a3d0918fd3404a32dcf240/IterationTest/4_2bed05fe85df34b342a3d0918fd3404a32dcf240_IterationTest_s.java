 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
import com.google.gson.Gson;

 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.MockNetwork;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.network.Network;
 import edu.wpi.cs.wpisuitetng.network.configuration.NetworkConfiguration;
 
 /**
  * Tests methods in iteration.java and tests the creation of variants of an iteration
  * @author Rolling Thunder
  *
  */
 public class IterationTest {
 	
 	@Test
 	public void createNonNullIteration() {
 		assertNotNull(new Iteration(1, "Test"));
 		assertNotNull(new Iteration());
 
 	}
 
 	@Test
 	public void iterationGettersTest() {
 		Iteration itr = new Iteration(3, "Rolling Thunder");
 		assertEquals(3, itr.getId());
 		assertEquals("Rolling Thunder", itr.getName());
 		assertEquals(0, itr.getEstimate());
 	}
 	
 	@Test
 	public void iterationSettersTest() {
 		// Mock network
 		Network.initNetwork(new MockNetwork());
 		Network.getInstance().setDefaultNetworkConfiguration(
 				new NetworkConfiguration("http://wpisuitetng"));
 		
 		Iteration itr = new Iteration(3, "Rolling Thunder");
 		itr.setId(2);
 		itr.setName("Changed");
 		itr.setEstimate(5);
 		
 		assertEquals(2, itr.getId());
 		assertEquals("Changed", itr.getName());
 		assertEquals(5, itr.getEstimate());
 		
 		itr.setName("");
 		assertEquals("Backlog", itr.getName());
 	}
 
 	@Test
 	public void createNewIterationWithNoName() {
 		assertEquals(new Iteration(0,"").getName(), "Backlog");
 		assertEquals(new Iteration(0,"").toString(), new Iteration(0,"").getName());
 	}
 	
 	@Test
 	public void testRenameIteration() {
 		Iteration i = new Iteration(0,"Iteration 1");
 		i.setName("Iteration 2");
 		assertEquals("Iteration 2", i.getName());
 	}
 
 	@Test
 	public void testEqualityWithEqualIterations() {
 		assertTrue(new Iteration(0,"Iteration 1").equals(new Iteration(0,"Iteration 1")));
 		assertTrue(new Iteration(0,"").equals(new Iteration(0,"")));
 	}
 	
 	@Test
 	public void testEqualityWithDifferentIterations() {
 		assertFalse(new Iteration(0,"Iteration 1").equals(new Iteration(0,"Iteration 2")));
 	}
 	
 	@Test
 	public void testCopyFrom() {
 		Iteration itr = new Iteration(3, "Rolling Thunder");
 		Iteration itr2 = new Iteration(5, "Original");
 		itr2.copyFrom(itr);
 		assertEquals("Rolling Thunder", itr2.getName());
 	}
 	
 	@Test
 	public void testToFromJson() {
 		Iteration itr = new Iteration(3, "Rolling Thunder");
 		String jsonitr = itr.toJSON();
 		Iteration returned = Iteration.fromJson(jsonitr);
 		assertEquals(returned.getId(), itr.getId());
 		assertEquals(returned.getName(), itr.getName());
 	}
 }
