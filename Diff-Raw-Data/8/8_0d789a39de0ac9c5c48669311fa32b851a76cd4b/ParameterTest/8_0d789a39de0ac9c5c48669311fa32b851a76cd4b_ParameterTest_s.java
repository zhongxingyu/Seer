 package com.alexrnl.commons.arguments;
 
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.lessThan;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test suite for the {@link Parameter} class.
  * @author Alex
  */
 public class ParameterTest {
 	/** The parameter to test */
 	private Parameter parameter;
 	/** The parameter loaded reflectively */
 	private Parameter reflectParam;
 	/** The parameter to load reflectively */
 	@Param(names = {"-name"}, required = false, description = "the name", order = 1)
 	private String myParam;
 	
 	/**
 	 * Set up test attributes.
 	 * @throws SecurityException
 	 *         if the field to test cannot be retrieved.
 	 * @throws NoSuchFieldException
 	 *         if the field to test cannot be found.
 	 */
 	@Before
 	public void setUp () throws NoSuchFieldException, SecurityException {
 		parameter = new Parameter(null, Arrays.asList(new String[] {"-n", "--number"}), true, "my description", 0);
 		final Field field = ParameterTest.class.getDeclaredField("myParam");
 		reflectParam = new Parameter(field, field.getAnnotation(Param.class));
 		
 	}
 	
 	/**
 	 * Test that a parameter cannot be created with no name.
 	 */
 	@SuppressWarnings("unused")
 	@Test(expected = IllegalArgumentException.class)
 	public void testParamWithNoName () {
 		new Parameter(null, new ArrayList<String>(), false, "does not matter", 0);
 	}
 	
 	/**
 	 * Test method for {@link Parameter#getField()}.
 	 * @throws SecurityException
 	 *         if the field to test cannot be retrieved.
 	 * @throws NoSuchFieldException
 	 *         if the field to test cannot be found.
 	 */
 	@Test
 	public void testGetField () throws NoSuchFieldException, SecurityException {
 		assertNull(parameter.getField());
 		assertEquals(ParameterTest.class.getDeclaredField("myParam"), reflectParam.getField());
 	}
 	
 	/**
 	 * Test method for {@link Parameter#getNames()}.
 	 */
 	@Test
 	public void testGetNames () {
 		assertEquals(new HashSet<>(Arrays.asList("-n", "--number")), parameter.getNames());
 		assertEquals(new HashSet<>(Arrays.asList("-name")), reflectParam.getNames());
 	}
 	
 	/**
 	 * Test method for {@link Parameter#isRequired()}.
 	 */
 	@Test
 	public void testIsRequired () {
 		assertTrue(parameter.isRequired());
 		assertFalse(reflectParam.isRequired());
 	}
 	
 	/**
 	 * Test method for {@link Parameter#getDescription()}.
 	 */
 	@Test
 	public void testGetDescription () {
 		assertEquals("my description", parameter.getDescription());
 		assertEquals("the name", reflectParam.getDescription());
 	}
 	
 	/**
 	 * Test method for {@link Parameter#getOrder()}.
 	 */
 	@Test
 	public void testGetOrder () {
 		assertEquals(0, parameter.getOrder());
 		assertEquals(1, reflectParam.getOrder());
 	}
 	
 	/**
 	 * Test method for {@link Parameter#hashCode()}.
 	 */
 	@Test
 	public void testHashCode () {
 		assertEquals(parameter.hashCode(), parameter.hashCode());
 		assertEquals(reflectParam.hashCode(), reflectParam.hashCode());
 		assertNotEquals(parameter.hashCode(), reflectParam.hashCode());
 	}
 
 	/**
 	 * Test method for {@link Parameter#equals(Object)}.
 	 */
 	@Test
 	public void testEqualsObject () {
 		assertEquals(parameter, parameter);
 		assertEquals(reflectParam, reflectParam);
 		assertNotEquals(parameter, reflectParam);
 		assertNotEquals(parameter, null);
 	}
 	
 	/**
 	 * Test method for {@link Parameter#compareTo(Parameter)}.
 	 */
 	@Test
 	public void testCompareTo () {
 		final List<String> emptyStrings = Arrays.asList("man", "test");
 		// Order is enough:
 		assertThat(new Parameter(null, emptyStrings , true, "", 0).compareTo(new Parameter(null, emptyStrings, true, "", 1)), lessThan(0));
 		assertThat(new Parameter(null, emptyStrings, true, "", 2).compareTo(new Parameter(null, emptyStrings, true, "", 1)), greaterThan(0));
 		
 		// With required attribute:
 		//0-0 & f-t
 		assertThat(new Parameter(null, emptyStrings , false, "", 0).compareTo(new Parameter(null, emptyStrings, true, "", 0)), greaterThan(0));
 		//0-0 & t-f
 		assertThat(new Parameter(null, emptyStrings, true, "", 0).compareTo(new Parameter(null, emptyStrings, false, "", 0)), lessThan(0));
 
 		// With names:
 		assertThat(new Parameter(null, emptyStrings , true, "", 0).compareTo(new Parameter(null, Arrays.asList("a"), true, "", 0)), greaterThan(0));
 		assertThat(new Parameter(null, emptyStrings , true, "", 0).compareTo(new Parameter(null, Arrays.asList("xal"), true, "", 0)), lessThan(0));
 		assertThat(new Parameter(null, emptyStrings , true, "", 0).compareTo(new Parameter(null, Arrays.asList("xal", "a"), true, "", 0)), greaterThan(0));
 		assertEquals(0, new Parameter(null, emptyStrings , true, "", 0).compareTo(new Parameter(null, Arrays.asList("man"), true, "", 0)));
 	}
 }
