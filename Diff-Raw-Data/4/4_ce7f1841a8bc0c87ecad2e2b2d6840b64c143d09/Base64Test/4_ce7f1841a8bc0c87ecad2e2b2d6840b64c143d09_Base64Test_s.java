 package com.github.seratch.signedrequest4j;
 
 import com.github.seratch.signedrequest4j.Base64.*;
 import static org.mockito.BDDMockito.*;
 
 import static org.junit.Assert.*;
 import com.github.seratch.signedrequest4j.Base64;
 import org.junit.Test;
 
 public class Base64Test {
 
 	@Test
 	public void type() throws Exception {
 		assertNotNull(Base64.class);
 	}
 
 	@Test
 	public void instantiation() throws Exception {
 		Base64 target = new Base64();
 		assertNotNull(target);
 	}
 
 	@Test
 	public void encode_A$byteArray() throws Exception {
 		// given
 		byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
 		// when
 		String actual = Base64.encode(bytes);
 		// then
 		String expected = "AQIDBAU=";
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void decode_A$String() throws Exception {
 		// given
 		String str = "AQIDBAU=";
 		// when
 		byte[] actual = Base64.decode(str);
 		// then
 		byte[] expected = new byte[] { 1, 2, 3, 4, 5 };
		assertEquals(expected, actual);
 	}
 
 }
