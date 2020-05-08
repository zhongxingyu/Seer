package edu.ucsb.cs56.projects.utilities.Cryptography;
 
 /**
    A class to implement the Vigenere Cipher.
    @author Callum Steele and Miranda Aperghis
    @version Project cs56, S13
  */
 
 public class VigenereCipher {
     private String cipherKey = null;
 
     /**
        Default constructor.
     */
     public VigenereCipher() {}
 
     /**
        Constructor that takes the cipher key as an argument.
        @param cipherKey The String to use when encrypting plaintext.
      */
     public VigenereCipher(String cipherKey) {
 	this.cipherKey = cipherKey.toLowerCase();
     }
 
     /**
        Sets the cipher key to the given String.
        @param cipherKey The String to use when encrypting plaintext.
      */
     public void setCipherKey(String cipherKey) {
 	this.cipherKey = cipherKey.toLowerCase();
     }
 
     /**
        Gets the cipher key.
      */
     public String getCipherKey() {
 	return this.cipherKey;
     }
 
     /**
        Encrypts the given plaintext using the Vigenere Cipher. This is done by matching up the cipher key with the plaintext and applying the Shift Cipher to each letter in the plaintext, using the corresponding cipher key letter as the shift.
      */
     public String encrypt(String plaintext) {
 	String cipherText = null;
 	//ShiftCipher s = new ShiftCipher();
 	
 	for (int i = 0; i < plaintext.length(); i++) {
 	    // sets the cipher key for the Shift Cipher
 	    //s.setCipherKey(cipherKey.charAt(i % cipherKey.length()));
 	    // applies the Shift Cipher to the next character
 	    //cipherText = cipherText + s.encrypt(plaintext.charAt(i));
 	}
 	
 	return cipherText;
     }
 }
