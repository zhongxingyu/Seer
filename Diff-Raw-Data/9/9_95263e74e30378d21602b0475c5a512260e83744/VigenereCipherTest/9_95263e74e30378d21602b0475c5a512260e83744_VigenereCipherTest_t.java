package edu.ucsb.cs56.projects.utilities.cryptography;
 
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 /**
    A class to implement the Vigenere Cipher.
    @author Callum Steele and Miranda Aperghis
    @version Project cs56, S13
    @see VigenereCipher
  */
 
 public class VigenereCipherTest {
 
     /**
        Test case for VigenereCipher.getCipherKey()
      */
     @Test
     public void test_getCipherKey() {
 	VigenereCipher v = new VigenereCipher("test");
 	assertEquals("test", v.getCipherKey());
     }
     
     /**
        Test case for VigenereCipher.setCipherKey()
      */
     @Test
     public void test_setCipherKey() {
 	VigenereCipher v = new VigenereCipher("test");
 	v.setCipherKey("word");
 	assertEquals("word", v.getCipherKey());
     }
 
     /**
        Test case for VigenereCipher.encrypt()
      */
     @Test
     public void test_encrypt1() {
 	VigenereCipher v = new VigenereCipher("test");
 	assertEquals("random", v.encrypt("random"));
     }
     
     /**
        Test case for VigenereCipher.encrypt()
      */
     @Test
     public void test_encrypt2() {
 	VigenereCipher v = new VigenereCipher("word");
 	assertEquals("random", v.encrypt("random"));
     }
 }
