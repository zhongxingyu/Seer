 /* Copyright (c) 2013, Regents of the University of California.  See License.txt for details */
 
 package org.grayleaves.utility;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.grayleaves.utility.ModelProperties;
 import org.grayleaves.utility.ModelPropertiesException;
 import org.junit.Test;
 
 
 public class ModelPropertiesTest
 {
 	
 	@Test
 	public void verifyNonExistentPropertiesFileThrowsError() throws Exception
 	{
 		try
 		{
 			ModelProperties.load("nonexistent.properties"); 
 		}
 		catch (ModelPropertiesException e)
 		{
 			assertTrue(e.getMessage().startsWith("Could not find properties nonexistent.properties:  ")); 
 		}
 	}
 //	@Test
 //	public void verifySeparatorBehaviorAcrossOperatingSystems() throws Exception
 //	{
 //		
 //	}
 	@Test
 	public void verifyCanLoadProperties() throws Exception
 	{		
		//TODO rework to avoid hard-coding file locations 
 		String fs = System.getProperty("file.separator"); 
		File file = new File("src"+fs+"test"+fs+"java"+fs+"org"+fs+"grayleaves"+fs+"utility"+fs+"testing.properties");
 		assertTrue(file.exists());
 		ModelProperties.load("org/grayleaves/utility/testing.properties");
 		assertEquals("2", ModelProperties.getProperties().getProperty("integer.property"));
		File backup = new File("src\\test\\java\\org\\grayleaves\\utility\\testing.properties.bak");
 		file.renameTo(backup);
 		assertTrue(!file.exists());
 		assertTrue(backup.exists());
 		ModelProperties.getProperties().setProperty("integer.property", "3");
 		assertEquals("update memory copy","3", ModelProperties.getProperties().getProperty("integer.property"));
 		ModelProperties.load("org/grayleaves/utility/testing.properties");
 		assertEquals("load does not execute 2nd time; memory copy should not be overlaid","3", ModelProperties.getProperties().getProperty("integer.property"));
 		ModelProperties.forceload("org/grayleaves/utility/testing.properties");
 		assertEquals("refreshed from the classloader, but not from disk!","2", ModelProperties.getProperties().getProperty("integer.property"));
 		backup.renameTo(file);
 		assertTrue(file.exists());
 		assertTrue(!backup.exists());
 	}
 	@Test
 	public void verifyCanReadAndUpdateProperty() throws Exception
 	{	
 		Properties properties = new Properties(); 
 		properties.setProperty("some.property", "1");
 		ModelProperties.setPropertiesForTesting(properties);
 		assertEquals("1", ModelProperties.getProperties().getProperty("some.property"));
 		ModelProperties.getProperties().setProperty("some.property", "2");
 		assertEquals("2", ModelProperties.getProperties().getProperty("some.property"));
 	}
 }
