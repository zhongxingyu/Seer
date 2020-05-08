 /**
  * Copyright (C) 2011 Adriano Monteiro Marques
  *
  * Author:  Zubair Nabi <zn.zubairnabi@gmail.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  */
 
 package org.umit.icm.mobile.test;
 
 
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.spec.RSAPrivateKeySpec;
 import java.security.spec.RSAPublicKeySpec;
 
 import org.umit.icm.mobile.utils.RSACrypto;
 
 import junit.framework.Assert;
 import android.test.AndroidTestCase;
 
 
 public class RSACryptoTests extends AndroidTestCase {
 	private KeyPair keyPair;
 	
     public void testPublicEncryptDecrypt() throws Throwable {
     	keyPair = RSACrypto.generateKey();
     	String cipherText = RSACrypto.encryptPublic(keyPair.getPublic(), "This is a test string");
         Assert.assertEquals("This is a test string", RSACrypto.decryptPrivate(keyPair.getPrivate(), cipherText));
     }
     
     public void testPrivateEncryptDecrypt() throws Throwable {
     	keyPair = RSACrypto.generateKey();
     	String cipherText = RSACrypto.encryptPrivate(keyPair.getPrivate(), "This is a test string");
         Assert.assertEquals("This is a test string", RSACrypto.decryptPublic(keyPair.getPublic(), cipherText));
     }
      
     public void testPublicReadWrite() throws Throwable {
     	keyPair = RSACrypto.generateKey();
     	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
     	RSAPublicKeySpec publicKeySpec = keyFactory.getKeySpec(keyPair.getPublic()
     			, RSAPublicKeySpec.class);
     	RSACrypto.saveKey("rsaKey.pub", publicKeySpec.getModulus()
     			, publicKeySpec.getPublicExponent());
        Assert.assertEquals(RSACrypto.readPublicKey("rsaKey.pub") 
        		,keyPair.getPublic());
     }
     
     public void testPrivateReadWrite() throws Throwable {
     	keyPair = RSACrypto.generateKey();
     	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
     	RSAPrivateKeySpec privateKeySpec = keyFactory.getKeySpec(keyPair.getPrivate()
     			, RSAPrivateKeySpec.class);
     	RSACrypto.saveKey("rsaKey.priv", privateKeySpec.getModulus()
     			, privateKeySpec.getPrivateExponent());
        Assert.assertEquals(RSACrypto.readPrivateKey("rsaKey.priv")
        		, keyPair.getPrivate());
     }
 
 }
