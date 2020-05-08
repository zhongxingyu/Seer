 package com.jshnd.casrom.config;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Collection;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.jshnd.casrom.config.dummyclasses.mapped.SomeClass;
 import com.jshnd.casrom.config.dummyclasses.mapped.SomeOtherClass;
 
 public class ReflectionsRowMapperSourceTest {
 
 	private ReflectionsRowMapperSource testObj;
 	
 	@Before
 	public void setup() {
 		testObj = new ReflectionsRowMapperSource();
 	}
 	
 	@Test
 	public void testGetRowMapperClasses() {
 		testObj.setBasePackage("com.jshnd.casrom.config.dummyclasses.mapped");
 		Collection<Class<?>> classes = testObj.getRowMapperClasses();
		assertEquals(2, classes.size());
 		assertTrue(classes.contains(SomeClass.class));
 		assertTrue(classes.contains(SomeOtherClass.class));
 	}
 
 }
