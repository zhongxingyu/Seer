 package org.netling.ssh.util;
 
 import java.io.IOException;
 
 import org.junit.Test;
 import org.netling.util.Base64;
 
 
 public class Base64Test {
 	
 	@Test
 	public void testEncodeDecode() throws IOException {
 		String s = "Hello world";
 		String encoded = Base64.encodeBytes(s.getBytes());
		System.out.println(encoded);
 		String decoded = new String(Base64.decode(encoded));
		System.out.println(decoded);
 	}
 
 }
