 /*
  * File: 		SimpleProgramCounterImplTest.java
  * Date: 		Oct 15, 2013
  *
  * Copyright 2013 Constantin Lazari. All rights reserved.
  *
  * Unless required by applicable law or agreed to in writing, this software
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.
  */
 package ch.zhaw.lazari.cpu.impl.program_counter;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import ch.zhaw.lazari.cpu.api.ProgramCounter;
 
 /**
  * Responsibility:
  */
 public class SimpleProgramCounterImplTest {
 
	/**
	 * Test method for {@link ch.zhaw.lazari.cpu.impl.program_counter.SimpleProgramCounterImpl#SimpleProgramCounterImpl()}.
	 */
	@Test
	public void testSimpleProgramCounterImpl() {
		final ProgramCounter counter = new SimpleProgramCounterImpl();
		assertEquals(0, counter.get());
	}

 	@Test
 	public void testSet() {
 		final ProgramCounter counter = new SimpleProgramCounterImpl(20, 3);
 		assertEquals(20, counter.get());
 		counter.set(32);
 		assertEquals(52, counter.get());
 	}
 	
 	@Test
 	public void testGetNext() {
		final ProgramCounter counter = new SimpleProgramCounterImpl();
 		final int last = counter.get();
 		counter.next();
 		assertTrue(last < counter.get());
 	}
 
 }
