 package com.netspective.commons.io;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import junit.framework.TestCase;
 
 /**
 * $Id: ResourceTest.java,v 1.2 2003-11-08 18:30:45 shahid.shah Exp $
  */
 public class ResourceTest extends TestCase
 {
 	final private String fileOne = "FindFileTestDirectory";
 	final private String fileTwo = "FindFileTestDirectory/file11.c";
 
 	public void testResource() throws IOException
 	{
 		Resource rsrcOne = new Resource(ResourceTest.class, fileOne);
 		String fqrn = "resource://" + this.getClass().getName() + "/" + fileOne;
 
 		assertEquals(fqrn, rsrcOne.getSystemId());
 		assertEquals(fileOne, rsrcOne.getResourceName());
 		assertEquals(this.getClass(), rsrcOne.getSourceClass());
 		assertNull(rsrcOne.getSourceLoader());
 		assertTrue(rsrcOne.isPhysicalFile());
 
 		File rsrcFileOne = rsrcOne.getFile();
 		assertTrue(rsrcFileOne.isDirectory());
 
 		// Build the resource name by changing all \ to / (if needed) and prepending a 'file:/' to the result...
 		String fileOneFQN = rsrcFileOne.getCanonicalPath();
         if(File.separatorChar == '\\')
 		    fileOneFQN = "/" + fileOneFQN.replace('\\', '/');
 		assertEquals("file:" + fileOneFQN, rsrcOne.getResource().toString());
 
 		Resource rsrcTwo = new Resource(this.getClass().getClassLoader(), fileTwo);
 		assertFalse(rsrcTwo.isPhysicalFile());
 		assertNotNull(rsrcTwo.getSourceLoader());
 
 		rsrcTwo = new Resource(this.getClass(), fileTwo);
 		String fqrnTwo = "resource://" + this.getClass().getName() + "/" + fileTwo;
 		assertEquals(fqrnTwo, rsrcTwo.getSystemId());
 		assertEquals(fileTwo, rsrcTwo.getResourceName());
 		assertEquals(this.getClass(), rsrcTwo.getSourceClass());
 		assertNull(rsrcTwo.getSourceLoader());
 		assertTrue(rsrcTwo.isPhysicalFile());
 
 		File rsrcFileTwo = rsrcTwo.getFile();
 		assertFalse(rsrcFileTwo.isDirectory());
 		assertTrue(rsrcFileTwo.isFile());
 
 		// Build the resource name by changing all \ to / (if needed) and prepending a 'file:/' to the result...
 		String fileTwoFQN = rsrcFileTwo.getCanonicalPath();
         if(File.separatorChar == '\\')
		    fileTwoFQN = "/" + fileOneFQN.replace('\\', '/');
 		assertEquals("file:" + fileTwoFQN, rsrcTwo.getResource().toString());
 
 		InputStream is = rsrcTwo.getResourceAsStream();
 		// 0 byte file...
 		assertEquals(0, is.available());
 		is.close();
 	}
 }
