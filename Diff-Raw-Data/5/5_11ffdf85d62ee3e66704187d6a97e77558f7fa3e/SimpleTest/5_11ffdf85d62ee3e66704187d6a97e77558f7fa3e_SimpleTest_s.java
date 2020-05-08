 package com.sumilux.idme.sdk;
 
import static org.junit.Assert.*;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import com.sumilux.ssi.client.Idme;
 import com.sumilux.ssi.client.IdmeException;
 import com.sumilux.ssi.client.json.JSONObject;
 
 
 public class SimpleTest {
 	
 	private String testToken = "86336695cd994af1b54149741fcb7417";
	private String ssiHost = "https://ss2.social-sign-in.com/smx/api";
 	
 //	private String testToken = "5f5e3ff82403401f936bcdbf08c4f800";
 //	private String ssiHost = "http://hf.sumilux.com/smx/api";
 	
 	
 	
 	@Test
 	public void testIsValidToken() {
 		try {
 			Idme idme = new Idme(testToken, ssiHost);
 			assertTrue(idme.isValidToken());
 		} catch (IdmeException e) {
 			e.printStackTrace();
 			assertTrue(false);
 		}
 	}
 	
 	@Test
 	public void testGetUserprofile() {
 		try {
 			Idme idme = new Idme(testToken, ssiHost);
 			JSONObject jo = idme.getUserProfile();
 			assertNotNull(jo);
 			System.out.println(jo);
 		} catch (IdmeException e) {
 			e.printStackTrace();
 			assertTrue(false);
 		}
 	}
 	
 //	@Test
 //	public void TestSSICache() {
 //		try {
 //			String token = Idme.allocateTestUser();
 //			Idme idme = new Idme(token);
 //			JSONObject jo1 = idme.getUserProfile();
 //			JSONObject jo2 = idme.getUserProfile();
 //			assertEquals(jo1.toString(), jo2.toString());
 //		} catch (IdmeException e) {
 //			e.printStackTrace();
 //			assertTrue(false);
 //		}
 //	}
 }
