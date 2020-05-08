 package com.globant.katari.shindig.auth;
 
 import static org.apache.shindig.auth.SecurityTokenDecoder.SECURITY_TOKEN_NAME;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.shindig.auth.AnonymousSecurityToken;
 import org.apache.shindig.auth.BlobCrypterSecurityToken;
 import org.apache.shindig.auth.KatariBlobCrypterSecurityTokenDecoder;
 import org.apache.shindig.auth.SecurityToken;
 import org.apache.shindig.auth.SecurityTokenException;
 import org.apache.shindig.common.crypto.BasicBlobCrypter;
 import org.apache.shindig.common.crypto.BlobCrypter;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableMap;
 
 /**
  * Test for the token decoder.
  * 
  * @author waabox (emiliano[dot]arango[at]globant[dot]com)
  * 
  */
 public class KatariBlobCrypterSecurityTokenDecoderTest {
 
   private KatariBlobCrypterSecurityTokenDecoder decoder;
   private BlobCrypter crypter;
 
   // Token Data
   String appUrl = "http://katari.globant.com/gadget.xml";
   long moduleId = 12345L;
   String ownerId = "owner";
   String viewerId = "viewer";
   String trustedJson = "trusted";
 
   @Before
   public void setUp() throws Exception {
     crypter = new BasicBlobCrypter("0123456789012618".getBytes("UTF-8"));
   }
 
   @Test
   public void testCreateToken() throws Exception {
 
     // Token Data
     String appUrl = "http://katari.globant.com/gadget.xml";
     long moduleId = 12345L;
     String ownerId = "jonh.doe";
     String viewerId = "jonh.doe";
     String trustedJson = "trusted";
     String container = "default";
     // End token Data.
     
     BlobCrypterSecurityToken token = new BlobCrypterSecurityToken(crypter,
         container, null);
 
     token.setAppUrl(appUrl);
     token.setModuleId(moduleId);
     token.setOwnerId(ownerId);
     token.setViewerId(viewerId);
     token.setTrustedJson(trustedJson);
 
     decoder = new KatariBlobCrypterSecurityTokenDecoder(container, crypter);
 
     String cryptedToken = token.encrypt();
 
    cryptedToken = URLEncoder.encode(cryptedToken, "UTF-8");

     Map<String, String> tokenMap;
     tokenMap = ImmutableMap.of(SECURITY_TOKEN_NAME, cryptedToken);
 
     SecurityToken st = decoder.createToken(tokenMap);
 
     assertEquals(st.getAppUrl(), appUrl);
     assertTrue(st.getModuleId() == moduleId);
     assertEquals(st.getOwnerId(), ownerId);
     assertEquals(st.getViewerId(), viewerId);
     assertEquals(st.getTrustedJson(), trustedJson);
   }
 
   @Test
   public void testCreateToken_Annonimous() throws Exception {
     decoder = new KatariBlobCrypterSecurityTokenDecoder("default", crypter);
     SecurityToken st = decoder.createToken(new HashMap<String, String>());
     assertTrue(st instanceof AnonymousSecurityToken);
   }
 
   @Test
   public void testCreateToken_InvalidToken() throws Exception {
     decoder = new KatariBlobCrypterSecurityTokenDecoder("default", crypter);
     try {
       Map<String, String> tokenMap;
       tokenMap = ImmutableMap.of(SECURITY_TOKEN_NAME, "invalidToken");
       decoder.createToken(tokenMap);
       fail("should fail because it is not a valid token!");
     } catch (SecurityTokenException e) {
       // nothing here
     }
   }
 
   @Test
   public void testContructor_failDomain() throws Exception {
     try {
       new KatariBlobCrypterSecurityTokenDecoder("", crypter);
       fail("should fail because the domain can not be empty");
     } catch (Exception e) {
       // nothing here
     }
     try {
       new KatariBlobCrypterSecurityTokenDecoder(null, crypter);
       fail("should fail because the domain can not be null");
     } catch (Exception e) {
       // nothing here
     }
   }
 
   @Test
   public void testContructor_failCrypter() throws Exception {
     try {
       new KatariBlobCrypterSecurityTokenDecoder("a", null);
       fail("should fail because the crypter can not be null");
     } catch (Exception e) {
       // nothing here
     }
   }
 }
