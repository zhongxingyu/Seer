 /*
  * File: 		SimpleMemoryImplTest.java
  * Date: 		Oct 15, 2013
  *
  * Copyright 2013 Constantin Lazari. All rights reserved.
  *
  * Unless required by applicable law or agreed to in writing, this software
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.
  */
 package ch.zhaw.lazari.cpu.impl.memory;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import ch.zhaw.lazari.cpu.api.Memory;
 
 /**
  * Responsibility:
  */
 public class SimpleMemoryImplTest {
 	
	private final static byte STORE = (byte) 42;
 	
 	private final static int ADDRESS = Memory.DEFAULT_SIZE - 1;
 	
 	@Test
 	public void testConstructor() {
 		final int testSize = 10;
 		final Memory memory = new SimpleMemoryImpl(testSize);
 		int nonZeroBytes = 0;
 		for(int index = 0; index < testSize; ++index) {
 			nonZeroBytes = (memory.load(index) == 0) ? nonZeroBytes : nonZeroBytes + 1; 
 		}
 		assertTrue(nonZeroBytes > 0);
 	}
 	
 	/**
 	 * Test method for {@link ch.zhaw.lazari.cpu.impl.memory.SimpleMemoryImpl#store(int, byte)}.
 	 */
 	@Test
 	public void testStoreLoadValid() {
 		final Memory memory = new SimpleMemoryImpl();
		memory.store(ADDRESS, STORE);
 		assertEquals(STORE, memory.load(ADDRESS));
 	}
 
 	/**
 	 * Test method for {@link ch.zhaw.lazari.cpu.impl.memory.SimpleMemoryImpl#store(int, byte)}.
 	 */
 	@Test(expected = InvalidMemoryAddressException.class)
 	public void testStoreInvalidNegative() {
 		final Memory memory = new SimpleMemoryImpl();
		memory.store(-1, STORE);
 	}
 
 	@Test(expected = InvalidMemoryAddressException.class)
 	public void testStoreInvalidMax() {
 		final Memory memory = new SimpleMemoryImpl();
		memory.store(Memory.DEFAULT_SIZE, STORE);
 	}
 
 	/**
 	 * Test method for {@link ch.zhaw.lazari.cpu.impl.memory.SimpleMemoryImpl#load(int)}.
 	 */
 	@Test(expected = InvalidMemoryAddressException.class)
 	public void testLoadInvalidNegative() {
 		final Memory memory = new SimpleMemoryImpl();
		memory.store(ADDRESS, STORE);
 		memory.load(-1);
 	}
 
 	/**
 	 * Test method for {@link ch.zhaw.lazari.cpu.impl.memory.SimpleMemoryImpl#load(int)}.
 	 */
 	@Test(expected = InvalidMemoryAddressException.class)
 	public void testLoadInvalidMax() {
 		final Memory memory = new SimpleMemoryImpl();
		memory.store(ADDRESS, STORE);
 		memory.load(Memory.DEFAULT_SIZE);
 	}
 
 }
