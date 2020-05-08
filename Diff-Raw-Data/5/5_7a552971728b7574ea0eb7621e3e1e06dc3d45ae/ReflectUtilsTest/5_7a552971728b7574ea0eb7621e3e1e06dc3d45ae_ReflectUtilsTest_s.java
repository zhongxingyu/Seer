 package com.alexrnl.commons.utils.object;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.alexrnl.commons.utils.object.AutoCompareTest.ComparedClass;
 
 /**
  * Test suite for the {@link ReflectUtils} class.
  * @author Alex
  */
 public class ReflectUtilsTest {
 	/** The first compared class */
 	private ComparedClass one;
 	/** The second compared class. */
 	private ComparedClass two;
 	
 	/**
 	 * Set up attributes.
 	 */
 	@Before
 	public void setUp () {
 		one = new ComparedClass("test", new Integer[] { 1, 2 }, false, new double[] { 2.8 });
 		two = new ComparedClass("track", new Integer[] { 4, 8 }, true, new double[] { -8.8 });
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.utils.object.ReflectUtils#retrieveMethods(java.lang.Class, java.lang.Class)}.
 	 */
 	@Test
 	public void testRetrieveMethods () {
 		assertEquals(3, ReflectUtils.retrieveMethods(ComparedClass.class, Field.class).size());
 		Logger.getLogger(ReflectUtils.class.getName()).setLevel(Level.FINE);
 		assertEquals(4 + Object.class.getMethods().length, ReflectUtils.retrieveMethods(ComparedClass.class, null).size());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.utils.object.ReflectUtils#invokeMethods(java.lang.Object, java.util.List)}.
 	 */
 	@Test
 	public void testInvokeMethods () {
 		final List<Method> comparedClassMethods = new ArrayList<>(ReflectUtils.retrieveMethods(ComparedClass.class, Field.class));
 		Collections.sort(comparedClassMethods, new Comparator<Method>() {
 			@Override
 			public int compare (final Method m1, final Method m2) {
 				return m1.getName().compareTo(m2.getName());
 			}
 		});
 		try {
 			final List<Object> ones = ReflectUtils.invokeMethods(one, comparedClassMethods);
 			final List<Object> twos = ReflectUtils.invokeMethods(two, comparedClassMethods);
 			assertArrayEquals(new double[] { 2.8 }, (double[]) ones.get(0), 0.01);
 			assertArrayEquals(new double[] { -8.8 }, (double[])  twos.get(0), 0.01);
 			assertArrayEquals(new Integer[] { 1, 2 }, (Integer[]) ones.get(1));
 			assertArrayEquals(new Integer[] { 4, 8 }, (Integer[]) twos.get(1));
 			assertEquals("test", ones.get(2));
 			assertEquals("track", twos.get(2));
 		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			fail("TODO (" + e.getMessage() + ")");
			e.printStackTrace();
 		}
 		
 	}
 }
