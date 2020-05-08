package gitFolder;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /*
  * Jonathan Sumrall, Ryan Lengel
  * 1DV200
  * LNU - Spring 2012
  * Practical Assignment 1
  * 
  * 
  *This is a small program that have various encryption algorithms implemented.
  *Users should be presented with the option to specificy the name of a text file to 
  *encrypt and then choose an encryption method, and to then have a new file generated
  *that is an encrpyted version of the origional. 
  *I guess it would also be able to apply the decryption of a file using 
  *a specified algorithm.  
  */
 
 public class encryptor{
 	public static void main(String [] args){
 		//Ryan this is going to be awesome
 		BufferedReader kb = new BufferedReader(new InputStreamReader(System.in));
 		//kb, as in "keyboard"
 		//We're in sweden....
 		System.out.println("Hej Hej");
 		System.out.println("Choose an encryption method:\n (1) Caesar Cipher \n(2)NULL \n(3)NUll");
 		String option = null;
 		try {
 			option = kb.readLine();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(option);
 	}
 	
 	
 }
 
 
 
