 /**
  * 
  */
 package main;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 /**
  * @author tomAndSimon
  *
  */
 public class HTMLreadImplTest {
 
 	@Test
 	public void readUntilTest() {
 		InputStream stream1 = null;
 		InputStream stream2 = null;
 		HTMLread myReader = new HTMLreadImpl(); 
 		try {
 			URL mySite1 = new URL("http://www.google.co.uk");
 			URL mySite2 = new URL("http://www.guardian.co.uk/");
 			stream1 = mySite1.openStream();
 			stream2 = mySite2.openStream();
 		} catch (MalformedURLException ex){
 			System.out.println("Malformed...");			
 		} catch (IOException ex) {
 			System.out.println("IO problem...");		
 		}
 		boolean result1 = myReader.readUntil(stream1, 's', 'f');
 		boolean result2 = myReader.readUntil(stream2, 'l', 's');
 		assertTrue(result1);
 		assertTrue(result2);
 	}
 
 	@Test
 	public void skipSpaceTest() {
		String str = "        x";
 		InputStream stream = new ByteArrayInputStream(str.getBytes());
 		HTMLread myReader = new HTMLreadImpl();
 		
 		assertEquals('x', myReader.skipSpace(stream, 'y'));
 		assertEquals(Character.MIN_VALUE, myReader.skipSpace(stream, 'x'));
 	}
 }
