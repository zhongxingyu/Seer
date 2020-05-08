 /*
  * DESEncrypter.java
  *
  * Copyright (c) 2009 FooBrew, Inc.
  */
 package org.j2free.security;
 
 
 import javax.crypto.Cipher;
 
 import javax.crypto.spec.SecretKeySpec;
 import net.jcip.annotations.Immutable;
 
 import org.apache.commons.codec.DecoderException;
 import org.apache.commons.codec.binary.Hex;
 import org.j2free.util.LaunderThrowable;
 
 /**
  * Utility class for encrypting via a passphrase
  *
  * @author Arjun Lall
  */
 @Immutable
 public class AESCipherFactory {
 
     public static Cipher getCipher(byte[] passPhrase, int cipherMode) {
         
         Cipher cipher = null;
         
         try {
             // Create the key based on the passPhrase
             SecretKeySpec keySpec = new SecretKeySpec(passPhrase,"AES");
             cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
 
             // Create the ciphers
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
 
         } catch (Exception e) {
             throw LaunderThrowable.launderThrowable(e);
         }
         
         return cipher;
     }
     
     public static Cipher getCipherFromHexString(String key, int cipherMode) {
         try {
            return getCipher(Hex.decodeHex(key.toCharArray()), Cipher.ENCRYPT_MODE);
         } catch (DecoderException ex) {
             throw LaunderThrowable.launderThrowable(ex);
         }
     }
 }
