 package com.seitenbau.common;
 
import static org.junit.Assert.*;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class FileHelperTest {
 
 	private static String filePath;
 	private static String failurePath;
 	private static String failureDirectory;
 	
 	private static String failureNoXmi;
 	
 	private static String jUnitTestFile;
 	private static File testFile;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() {
 		filePath = "src/test/resources/import/export.xmi";
 		failurePath = "testFailure";
 		failureDirectory = "src/test/resources/import";
 		
 		failureNoXmi = "src/test/resources/import/test.eap";
 		
 		jUnitTestFile = "src/test/resources/import/jUnitTest.xmi";
 		testFile = new File(jUnitTestFile);
 	}
 	
 	@Test
 	public void testGetFileSuccess() throws FileNotFoundException {
 		File returnFile = FileHelper.getFile(filePath);
 		
 		assertNotNull(returnFile);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testGetFileFailureNull() throws FileNotFoundException {
 		
 		FileHelper.getFile(null);
 	}
 	
 	@Test(expected = FileNotFoundException.class)
 	public void testGetFileNotFound() throws FileNotFoundException {
 		
 		FileHelper.getFile(failurePath);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testGetFileFailureDirectory() throws FileNotFoundException {
 		
 		FileHelper.getFile(failureDirectory);
 	}
 
 	@Test
 	public void testGetXmiFile() throws FileNotFoundException {
 		File returnFile = FileHelper.getXmiFile(filePath);
 		
 		assertNotNull(returnFile);
 	}
 	
 	@Test(expected = FileNotFoundException.class)
 	public void testGetXmiFileFailure() throws FileNotFoundException {
 		
 		FileHelper.getXmiFile(failureNoXmi);
 	}
 
 	@Test
 	public void testGetContents() {
 
 		try{
 			FileWriter fstream = new FileWriter(testFile);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write("Test");
 			out.close();
 		}catch (Exception e){
 			System.err.println("Error: " + e.getMessage());
 		}
 		
 		// \n is caused by out.write
		String expected = "Test\n";
 		String content = FileHelper.getContents(testFile);
 
 		assertEquals(expected, content);
 	}
 	
 	@AfterClass
 	public static void tearDownAfterClass() {
 		testFile.delete();
 	}
 
 }
