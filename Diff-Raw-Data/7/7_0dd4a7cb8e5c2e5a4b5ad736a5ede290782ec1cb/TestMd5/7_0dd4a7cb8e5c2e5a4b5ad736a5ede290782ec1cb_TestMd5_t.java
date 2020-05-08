 package ch.hsr.waktu.services;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 //import java.io.UnsupportedEncodingException;
 import ch.hsr.waktu.TestSuite;
 
 public class TestMd5 extends TestSuite {
 	@Test
 	public void hash_NullInput() {
 		assertEquals(null, Md5.hash(null));
 	}
 	
 	//MD5 referecnce strings from: http://www.miraclesalad.com/webtools/md5.php
 	//(c) 2010, Sunny Walker
 	@Test
 	public void hash_LowerCaseInput() {
 		assertEquals("61a60170273e74a5be90355ffe8e86ad", Md5.hash("aabbcc"));
 	}
 	
 	@Test
 	public void hash_UppercaseLowerCaseInput() {
 		assertEquals("cce4f4bcf14f047ff3e20efe283a0bcc", Md5.hash("aAbBcC"));
 	}
 	
 	@Test
 	public void hash_EmptyStringInput() {
 		assertEquals("d41d8cd98f00b204e9800998ecf8427e", Md5.hash(""));
 	}
 	 
 	@Test
 	public void hash_SpecialCharactersInput() {
 		assertEquals("fa7837e812ed19536055330e59300d9d", Md5.hash("+\"*ç%&/()=?`!£,.-öä$<§|\\"));
 	}
 	
 }
