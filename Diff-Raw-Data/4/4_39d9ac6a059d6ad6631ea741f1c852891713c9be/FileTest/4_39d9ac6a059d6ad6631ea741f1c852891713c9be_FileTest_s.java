 package org.xomios.internal;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.xomios.IOException;
 
 public class FileTest {
 
 	private File file;
 
 	@Before
 	public void setUp ( ) throws Exception {
 		file = new File( "test" );
 	}
 
 	@After
	public void tearDown ( ) {

 	}
 
 	@Test
 	public void testIsOpen ( ) throws IOException {
 		assertFalse( file.isOpen() );
 		file.open( File.O_CREAT | File.O_EXCL );
 		assertTrue( file.isOpen() );
 	}
 
 	@Test
 	public void testOpen ( ) {
 		fail( "Not yet implemented" );
 	}
 
 	@Test
 	public void testReadInt ( ) {
 		fail( "Not yet implemented" );
 	}
 
 	@Test
 	public void testRead ( ) {
 		fail( "Not yet implemented" );
 	}
 
 	@Test
 	public void testWrite ( ) {
 		fail( "Not yet implemented" );
 	}
 
 	@Test
 	public void testSetOffsetLongSeek ( ) {
 		fail( "Not yet implemented" );
 	}
 
 	@Test
 	public void testGetOffset ( ) {
 		fail( "Not yet implemented" );
 	}
 
 	@Test
 	public void testClose ( ) {
 		fail( "Not yet implemented" );
 	}
 
 }
