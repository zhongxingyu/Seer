 package net.praqma.drmemory;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import net.praqma.drmemory.exceptions.InvalidInputException;
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.Logger.LogLevel;
 import net.praqma.util.debug.appenders.ConsoleAppender;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class TestDrMemoryResult {
 
 	private static Logger logger = Logger.getLogger();
 	
 	@BeforeClass
 	public static void start() {
 		ConsoleAppender app = new ConsoleAppender();
 		app.setMinimumLevel( LogLevel.DEBUG );
 		Logger.addAppender( app );
 	}
 	
 	@Test
 	public void testTopParser() throws IOException, InvalidInputException {
 		URL s = TestError.class.getClassLoader().getResource( "top.txt" );
 		System.out.println( "URL: " + s );
 		DrMemoryResult result = DrMemoryResult.parse( new File( s.getFile() ) );
 		
		assertEquals( "1.4.5 build 2", result.version );
		assertEquals( "Nov 16 2011 22:44:01", result.date );
		assertEquals( "\"C:\\praqma\\workspace2\\alm\\Win32\\Debug\\ALMsin.exe\" \"praqma\"", result.cmd );
 	}
 	
 	@Test
 	public void testOutput1() throws IOException, InvalidInputException {
 		URL s = TestError.class.getClassLoader().getResource( "output2.txt" );
 		System.out.println( "URL: " + s );
 		DrMemoryResult result = DrMemoryResult.parse( new File( s.getFile() ) );
 		
 		assertTrue( 2 == result.getElements().size() );
 	}
 	
 	@Test
 	public void testOutput2() throws IOException, InvalidInputException {
 		URL s = TestError.class.getClassLoader().getResource( "drmemory-light.txt" );
 		System.out.println( "URL: " + s );
 		DrMemoryResult result = DrMemoryResult.parse( new File( s.getFile() ) );
 		
 		assertTrue( 2 == result.getErrors().size() );
 		assertTrue( result.getErrors().get( 2 ).getDuplicates() == 115 );
 	}
 	
 	@Test
 	public void testErrorSummary() throws IOException, InvalidInputException {
 		URL s = TestError.class.getClassLoader().getResource( "drmemory-light.txt" );
 		System.out.println( "URL11: " + s );
 		DrMemoryResult result = DrMemoryResult.parse( new File( s.getFile() ) );
 		
 		
 		assertTrue( result.getUnaddressableAccesses().unique == 0 );
 		assertTrue( result.getUnaddressableAccesses().total == 0 );
 		
 		assertTrue( result.getUninitializedAccess().unique == 28 );
 		assertTrue( result.getUninitializedAccess().total == 4014 );
 		
 		assertTrue( result.getInvalidHeapArguments().unique == 1 );
 		assertTrue( result.getInvalidHeapArguments().total == 1 );
 		
 		assertTrue( result.getWarnings().unique == 0 );
 		assertTrue( result.getWarnings().total == 0 );
 		
 		assertTrue( result.getBytesOfLeaks().unique == 52 );
 		assertTrue( result.getBytesOfLeaks().total == 69 );
 		assertTrue( result.getBytesOfLeaks().info == 620936 );
 		
 		assertTrue( result.getBytesOfPossibleLeaks().unique == 1 );
 		assertTrue( result.getBytesOfPossibleLeaks().total == 1 );
 		assertTrue( result.getBytesOfPossibleLeaks().info == 64 );
 		
 		assertTrue( result.getStillReachableAllocations() == 305 );
 		
 	}
 }
