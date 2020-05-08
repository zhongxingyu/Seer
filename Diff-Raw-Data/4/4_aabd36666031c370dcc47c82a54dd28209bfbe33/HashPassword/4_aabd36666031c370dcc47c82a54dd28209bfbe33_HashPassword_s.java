 package com.aes;
 
 import org.apache.commons.codec.DecoderException;
 import org.apache.commons.codec.binary.Hex;
 
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.util.Arrays;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sergey.vyatkin
  * Date: 12/4/13
  * Time: 9:37 AM
  */
 public class HashPassword {
 
     /**
      * Password hash.
      *
      * @param password      the password to hash.
      * @param salt          the salt
      * @param iterations    the iteration count (slowness factor)
      * @param lengthInBytes the length of the hash to compute in bytes
      * @param algorithm     the algorithm
      * @return the PBDKF2 hash of the password
      */
     private static byte[] getHashPassword(final char[] password,
                                           final byte[] salt,
                                           final int iterations,
                                           final int lengthInBytes,
                                           final String algorithm)
             throws NoSuchAlgorithmException, InvalidKeySpecException {
         /**
          * PBEKeySpec(char[] password, byte[] salt, int iterationCount, int keyLength)
          * Constructor that takes a password, salt, iteration count,
          * and to-be-derived key length for generating PBEKey of variable-key-size PBE ciphers.
          */
 
         PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, lengthInBytes);
         SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
         return skf.generateSecret(spec).getEncoded();
     }
 
     /**
      * Simple test to hash password.
      *
      * @param args
      */
     public static void main(final String[] args) throws DecoderException, UnsupportedEncodingException {
 
         String[] algorithms = {"PBKDF2WithHmacSHA1"};
 
         // Generate a random salt
        SecureRandom random = new SecureRandom();
         byte[] salt = new byte[24];  // SALT_LENGTH = 24
        random.nextBytes(salt);
 
         for (String algorithm : algorithms) {
             // Hash the password
             try {
                 byte[] hash = getHashPassword("password".toCharArray(), salt, 100, 24 * 8, algorithm);
                 // encode hash to hexadecimal string
                 String hexStr = Hex.encodeHexString(hash);
                 System.out.println(algorithm + ":salt:" + salt + ":hash:"
                         + Arrays.toString(hash) + ":hex:" + hexStr);
             } catch (NoSuchAlgorithmException e) {
                 e.printStackTrace();
             } catch (InvalidKeySpecException e) {
                 e.printStackTrace();
             }
         }
     }
 }
