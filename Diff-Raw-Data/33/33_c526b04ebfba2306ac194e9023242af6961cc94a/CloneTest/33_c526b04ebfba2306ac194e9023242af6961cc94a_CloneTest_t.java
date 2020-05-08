 package uk.ac.ucl.cs.clonedetector.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.security.NoSuchAlgorithmException;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.ac.ucl.cs.clonedetector.Clone;
 import uk.ac.ucl.cs.clonedetector.Reference;
 
 public class CloneTest extends TestCase {
 
 	public Clone c;
 	public Clone c2;
 	public Clone c3;
 	public Reference r;
 	public Reference r2;
 	public Reference r3;
 	public Reference r4;
 	
 	@Before
 	public void setUp() throws Exception {
 		r = new Reference("filename", 14);
 		r2 = new Reference("filename", 15);
 		r3 = new Reference("filename", 16);
 		r4 = new Reference("filename", 17);
 		c = new Clone(r,r2,r3,r4);
 		c2 = new Clone(r3,r4,r,r2);
 		c3 = new Clone(r4,r2,r3,r);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	
 	@Test
 	public void test_toString() {
 		String output = "(filename)14-15:(filename)16-17";
 		assertEquals(c.toString(), output);
 		
 		String output2 = "(filename)14-15:(filename)16-17";
 		assertEquals(c2.toString(), output2);
 	}
 	
 	@Test
 	public void test_getIStart() {
 		
 		assertEquals(c.getIStart(), r);
 	}
 	
 	@Test
	public void test_getLength() {
		
		assertEquals(c.getLength(), 1);
 	}
 	
	@Test
	public void test_encompasses() {
		Reference r = new Reference("filename", 26);
		Reference r2 =new Reference("filename", 28);
		Reference r3 =new Reference("filename", 29);
		Reference r4 =new Reference("filename", 20);
		Clone c5 = new Clone(r,r2,r3,r4);
		assertEquals(c.encompasses(c5), false);
		
		assertEquals(c.encompasses(c), true);
		
		//Test different file names for iStart and jSTart
		Reference r5 =new Reference("different", 20);
		c5 = new Clone(r,r2,r5,r4);
		assertEquals(c.encompasses(c5), false);
		
	}
 	
 	
	@Test
	public void test_compareTo() {
		assertTrue(c.compareTo(c3) < 0);
		assertTrue(c3.compareTo(c) > 0);
	}

 	public static TestSuite suite() {
 		TestSuite suite = new TestSuite(CloneTest.class);
 		return  suite;
 	}
 
 	
 }
