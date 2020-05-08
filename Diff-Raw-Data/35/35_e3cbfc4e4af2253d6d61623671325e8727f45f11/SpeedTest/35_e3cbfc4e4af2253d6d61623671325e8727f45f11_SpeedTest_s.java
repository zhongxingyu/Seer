 package com.cyanoryx.uni.tests;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.InvalidKeySpecException;
 import java.util.Random;
 import java.util.zip.DataFormatException;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.KeyGenerator;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 
 import com.cyanoryx.uni.crypto.aes.AES;
 import com.cyanoryx.uni.crypto.aes.Key;
 import com.cyanoryx.uni.crypto.aes.KeySize;
 import com.cyanoryx.uni.crypto.aes.Mode;
 import com.cyanoryx.uni.crypto.rsa.PublicKey;
 import com.cyanoryx.uni.crypto.rsa.RSA_OAEP;
 
 public class SpeedTest {
   public static void main(String[] args) throws InternalError,
   												DataFormatException,
   												IOException,
   												InvalidKeyException,
   												NoSuchAlgorithmException,
   												NoSuchPaddingException,
   												IllegalBlockSizeException,
   												BadPaddingException,
  												InvalidKeySpecException {
     System.out.println("=================================\nRSA Algorithms");
     byte[] tmp = new byte[16];
     new Random().nextBytes(tmp);
     
     RSA_OAEP rsa = new RSA_OAEP(new PublicKey(new File("./id_rsa")));
     
     long rsa_duration=0;
     long rsa_dur_sum=0;
     for (int i=0;i<20;i++) {
 	    final long rsa_startTime = System.nanoTime();
 	    final long rsa_endTime;
 	    try {
 	      rsa.encrypt(tmp);
 	    } finally {
 	      rsa_endTime = System.nanoTime();
 	    }
 	    rsa_duration = (rsa_endTime - rsa_startTime);
 	    rsa_dur_sum += rsa_duration;
     }
     
     System.out.println("Custom time: "+(rsa_dur_sum/20));
     
     KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
     kpg.initialize(2048);
     KeyPair kp = kpg.genKeyPair();
     java.security.Key publicKey = kp.getPublic();
     
    Cipher cipher = Cipher.getInstance("RSA");
     cipher.init(Cipher.ENCRYPT_MODE, publicKey);
     
     rsa_dur_sum=0;
     for (int i=0;i<20;i++) {
 	    final long sdk_rsa_startTime = System.nanoTime();
 	    final long sdk_rsa_endTime;
 	    try {
 	      cipher.doFinal(tmp);
 	    } finally {
 	      sdk_rsa_endTime = System.nanoTime();
 	    }
 	    final long sdk_rsa_duration = sdk_rsa_endTime - sdk_rsa_startTime;
 	    rsa_dur_sum += sdk_rsa_duration;
     }
     
     System.out.println("JDK time:  "+(rsa_dur_sum/20)+"ns");
     
     System.out.println("=================================\nAES Algorithms");
     
     AES aes = new AES();
     aes.setKey(new Key(KeySize.K256));
     aes.setMode(Mode.ECB);
     aes.setPlainText(tmp);
     
     long aes_duration=0;
     long aes_dur_sum=0;
     for (int i=0;i<20;i++) {
 	    final long aes_startTime = System.nanoTime();
 	    final long aes_endTime;
 	    try {
 	      aes.encrypt();
 	    } finally {
 	      aes_endTime = System.nanoTime();
 	    }
 	    aes_duration = aes_endTime - aes_startTime;
 	    aes_dur_sum+=aes_duration;
     }
     
     System.out.println("Custom time:  "+(aes_dur_sum/20)+"ns");
     
     KeyGenerator kgen = KeyGenerator.getInstance("AES");
     kgen.init(256);
     
     SecretKey skey = kgen.generateKey();
     byte[] raw = skey.getEncoded();
   
     SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
   
     cipher = Cipher.getInstance("AES");
     cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
     
     aes_dur_sum=0;
     for (int i=0;i<20;i++) {
 	    final long sdk_aes_startTime = System.nanoTime();
 	    final long sdk_aes_endTime;
 	    try {
 	      cipher.doFinal(tmp);
 	    } finally {
 	      sdk_aes_endTime = System.nanoTime();
 	    }
 	    final long sdk_aes_duration = sdk_aes_endTime - sdk_aes_startTime;
 	    aes_dur_sum += sdk_aes_duration;
     }
     
     System.out.println("JDK time:  "+(aes_dur_sum/20)+"ns");
   }
 }
