 package org.jvnet.hudson.crypto;
 
 import junit.framework.TestCase;
 
 import java.math.BigInteger;
 import java.security.interfaces.RSAPublicKey;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class RSAPublicKeyUtilTest extends TestCase {
     public void testReadPublicKey() throws Exception {
         RSAPublicKey p = (RSAPublicKey) RSAPublicKeyUtil.readPublicKey("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCzBy1GEihAxSgrsEANgCxYwxS8Yy0U7cKq/1MMtr4/IrW2m2rzDcr4a7ZG/p/XrchCMn5eIekq1dYHsB0hY81iJr7jMZi7XbQx/LohF833YhIRctALpNzPunqBxZvOUVDib/dfX6LuoZTOojI/W5UPYrzAjyrjKMQvF5Mo0LaZ6eN1LElVaGzWExqO7mNkOrJY3IVurPu81mK4E+59FHTuB/oIawHUlxjMgBFPGKZBmb0cyVyViEmY6E78bNcN+frdSxZ72gcK/J7l1gfGz6YNQX6hKA+3v2O+/6pHf282W2hy0u4nw2DTs5NrsTnG8koiivilXC3VbhgVmQnUFKx5 kohsuke@griffon.2013");
         System.out.println(p);
 
        assertEquals(p.getModulus(), new BigInteger("65537"));
         // this hex output I got from "openssl rsa -modulus -noout -in ~/.ssh/id_rsa"
        assertEquals(p.getPublicExponent(), new BigInteger("B3072D46122840C5282BB0400D802C58C314BC632D14EDC2AAFF530CB6BE3F22B5B69B6AF30DCAF86BB646FE9FD7ADC842327E5E21E92AD5D607B01D2163CD6226BEE33198BB5DB431FCBA2117CDF762121172D00BA4DCCFBA7A81C59BCE5150E26FF75F5FA2EEA194CEA2323F5B950F62BCC08F2AE328C42F179328D0B699E9E3752C4955686CD6131A8EEE63643AB258DC856EACFBBCD662B813EE7D1474EE07FA086B01D49718CC80114F18A64199BD1CC95C95884998E84EFC6CD70DF9FADD4B167BDA070AFC9EE5D607C6CFA60D417EA1280FB7BF63BEFFAA477F6F365B6872D2EE27C360D3B3936BB139C6F24A228AF8A55C2DD56E18159909D414AC79",16));
     }
 
     public void testGetFingerPrint() throws Exception {
         RSAPublicKey p = (RSAPublicKey) RSAPublicKeyUtil.readPublicKey("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCzBy1GEihAxSgrsEANgCxYwxS8Yy0U7cKq/1MMtr4/IrW2m2rzDcr4a7ZG/p/XrchCMn5eIekq1dYHsB0hY81iJr7jMZi7XbQx/LohF833YhIRctALpNzPunqBxZvOUVDib/dfX6LuoZTOojI/W5UPYrzAjyrjKMQvF5Mo0LaZ6eN1LElVaGzWExqO7mNkOrJY3IVurPu81mK4E+59FHTuB/oIawHUlxjMgBFPGKZBmb0cyVyViEmY6E78bNcN+frdSxZ72gcK/J7l1gfGz6YNQX6hKA+3v2O+/6pHf282W2hy0u4nw2DTs5NrsTnG8koiivilXC3VbhgVmQnUFKx5 kohsuke@griffon.2013");
         assertEquals("f7:7a:42:76:79:e8:8a:1a:4a:32:0c:b3:f9:3b:53:d4",RSAPublicKeyUtil.getFingerPrint(p));
     }
 }
