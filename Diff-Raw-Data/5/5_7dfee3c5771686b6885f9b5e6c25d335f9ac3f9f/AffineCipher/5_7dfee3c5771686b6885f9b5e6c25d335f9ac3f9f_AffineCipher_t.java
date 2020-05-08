 package edu.ucsb.cs56.projects.utilities.cryptography;
 
 /**
    A class to implement the Affine Cipher.
    @author Miranda Aperghis
    @author Callum Steele
    @version Project CS56, S13, 05/29/2013
  */
 public class AffineCipher{
     private int keyA;
     private int keyB;
 
     /**
        Default no-arg constructor.
     */
     public AffineCipher(){}
 
     /**
        Two-arg constructor.
        @param keyA an integer that is used to encrypt the plaintext 
        @param keyB an integer that is used to encrypt the plaintext
     */
     public AffineCipher(int keyA, int keyB){
 	this.keyA = keyA;
 	this.keyB = keyB;
     }
 
     /**
        Getter for the cipher key A
        @return cipher key integer
     */
     public int getKeyA(){
	return this.keyA;
     }
 
     /**
        Getter for the cipher key B
        @return cipher key integer
     */
     public int getKeyB(){
	return this.keyB;
     }
 
     /** 
 	Setter for the cipher key A
 	@param keyA an integer is used to encrypt the plaintext
     */
     public void setKeyA(int keyA){
 	this.keyA = keyA;
     }
 
     /** 
 	Setter for the cipher key B
 	@param keyB an integer is used to encrypt the plaintext
     */
     public void setKeyB(int keyB){
 	this.keyB = keyB;
     }
 
     /**
        Encryption algorithm for the affine cipher.
        Each character is multiplied by keyA and then keyB is added, mod 26 to get the new encrypted character.
        @param word a plaintext word that is to be encrypted
        @return the ciphertext (the encrypted plaintext)
     */
     public String encrypt(String word){
 	if(word == null) throw new IllegalArgumentException();
 	String result = "STUB";//STUB
 	/*
 	for(int i=0; i<word.length(); i++){
 	    if(word.charAt(i)<97 || word.charAt(i)>122)
 		throw new IllegalArgumentException();
 	    int k = (((word.charAt(i)-97)+this.cipherKey)%26)+97;
 	    result += Character.toString((char)k);
 	    }*/
 	return result;
     }
 
 }
