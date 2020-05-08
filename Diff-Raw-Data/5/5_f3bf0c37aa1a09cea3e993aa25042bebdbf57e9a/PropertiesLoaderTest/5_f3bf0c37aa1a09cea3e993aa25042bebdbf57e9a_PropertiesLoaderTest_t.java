 package com.ideas.producerconsumer.util;
 
 import java.io.FileNotFoundException;
 import java.util.Properties;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class PropertiesLoaderTest
 {
 	@Test
 	public void testGetPropertyFile()
 	{
 		Properties propertyFile;
 		try
 		{
			propertyFile = PropertiesLoader.getPropertyFile("queue.properties");
 			Assert.assertNotNull(propertyFile);
 			System.out.println(propertyFile.get("corePoolSize"));
 			Assert.assertNotNull(propertyFile.get("corePoolSize"));
 		}
 		catch(FileNotFoundException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Test(expected=FileNotFoundException.class)
 	public void testGetPropertyFileWithIncorrectFile() throws FileNotFoundException
 	{
		Properties propertyFile = PropertiesLoader.getPropertyFile("settings.properties");
 		Assert.assertNotNull(propertyFile);
 		System.out.println(propertyFile.get("corePoolSize"));
 		Assert.assertNotNull(propertyFile.get("corePoolSize"));
 	}
 	
 }
