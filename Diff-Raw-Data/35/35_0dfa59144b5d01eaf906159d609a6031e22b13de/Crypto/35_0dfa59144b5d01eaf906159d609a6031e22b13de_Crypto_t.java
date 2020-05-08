 package com.voracious.dragons.server;
 
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 
import com.voracious.dragons.common.Turn;

 public class Crypto {
     // The higher the number of iterations the more 
     // expensive computing the hash is for us
     // and also for a brute force attack.
     private static final int iterations = 10*1024;
     private static final int saltLen = 32;
     private static final int desiredKeyLen = 256;
     
     private static Logger logger = Logger.getLogger(Crypto.class);
 
     public static String getSessionId(){
     	try {
			return Base64.encodeBase64String(SecureRandom.getInstance("SHA1PRNG").generateSeed(Turn.sessionLength));
 		} catch (NoSuchAlgorithmException e) {
 			logger.error(e);
 			return null;
 		}
     }
     
     /** Computes a salted PBKDF2 hash of given plaintext password
         suitable for storing in a database. 
         Empty passwords are not supported. */
     public static String getSaltedHash(String password) {
         byte[] salt;
 		try {
 			salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen);
 			// store the salt with the password
 			return Base64.encodeBase64String(salt) + "$" + hash(password, salt);
 		} catch (NoSuchAlgorithmException e) {
 			logger.error(e);
 			return null;
 		}
     }
 
     /** Checks whether given plaintext password corresponds 
         to a stored salted hash of the password. 
      * @throws Exception */
     public static boolean check(String password, String stored){
         String[] saltAndPass = stored.split("\\$");
         if (saltAndPass.length != 2)
             return false;
         String hashOfInput = hash(password, Base64.decodeBase64(saltAndPass[0]));
         return hashOfInput.equals(saltAndPass[1]);
     }
 
     // using PBKDF2 from Sun, an alternative is https://github.com/wg/scrypt
     // cf. http://www.unlimitednovelty.com/2012/03/dont-use-bcrypt.html
     private static String hash(String password, byte[] salt) {
         if (password == null || password.length() == 0)
             throw new IllegalArgumentException("Empty passwords are not supported.");
 		try {
 			SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
 			try {
 				SecretKey key = f.generateSecret(new PBEKeySpec(
 				    password.toCharArray(), salt, iterations, desiredKeyLen)
 				);
 				return Base64.encodeBase64String(key.getEncoded());
 			} catch (InvalidKeySpecException e) {
 				logger.error(e);
 				return null;
 			}
 		} catch (NoSuchAlgorithmException e) {
 			logger.error(e);
 			return null;
 		}
     }
 }
