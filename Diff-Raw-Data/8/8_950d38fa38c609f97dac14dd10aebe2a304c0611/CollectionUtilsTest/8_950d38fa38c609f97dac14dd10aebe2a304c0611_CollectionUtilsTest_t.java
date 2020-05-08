 package com.alexrnl.commons.utils;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 
 import org.junit.Test;
 
 /**
  * Test suite for the {@link CollectionUtils} class.
  * @author Alex
  */
 public class CollectionUtilsTest {
 
 	/**
 	 * Test method for {@link com.alexrnl.commons.utils.CollectionUtils#isSorted(java.lang.Iterable)}.
 	 */
 	@Test
 	public void testIsSorted () {
 		assertTrue(CollectionUtils.isSorted(new LinkedList<Integer>()));
 		assertTrue(CollectionUtils.isSorted(Arrays.asList(new Integer[] {1, 1, 2, 3, 8, 20, 28})));
 		assertFalse(CollectionUtils.isSorted(Arrays.asList(new Integer[] {1, 0, 2, 3, 8, 20})));
 		assertFalse(CollectionUtils.isSorted(Arrays.asList(new Integer[] {0, 2, 1, 3, 8, 20})));
		assertFalse(CollectionUtils.isSorted(Arrays.asList(new Integer[] {0, 2, 1})));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.utils.CollectionUtils#isSorted(java.lang.Iterable)}.
 	 */
 	@Test(expected = NullPointerException.class)
 	public void testIsSortedNullPointer () {
 		CollectionUtils.isSorted(null);
 	}
 }
