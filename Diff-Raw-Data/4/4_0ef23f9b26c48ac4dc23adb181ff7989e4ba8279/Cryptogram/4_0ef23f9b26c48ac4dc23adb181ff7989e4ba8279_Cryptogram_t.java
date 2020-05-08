 package pg13.models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 /*
  *  @author Lauren Slusky
  *  @date May 26 2013
  *  @title Cryptogram
  *  @description Class defining cryptograms
  */
 public class Cryptogram extends Puzzle
 {
 	private final int ALPHABET_SIZE = 26;	// Letters of the alphabet
 	private String ciphertext;				// Coded string
 	private String plaintext;				// the original user string
 	private CryptogramPair[] solutionMapping;	// Mapping for plaintext to ciphertext characters used for encryption and decryption
 	private CryptogramPair[] userMapping;	// User Entered mapping of characters when solving a cryptogram ordered by ciphertext
 	
 	
 	public Cryptogram()
 	{
 		super();
 		this.solutionMapping = setMappingKeys(false);
 		generateMappingKeys();
 		this.userMapping = setMappingKeys(true);
 		this.ciphertext = "";
 		this.plaintext = "";
 	}
 	
 	public Cryptogram(String author, String title, Date dateCreated, String plaintext)
 	{
 		super(author, title, dateCreated);
 		this.solutionMapping = setMappingKeys(false);
 		generateMappingKeys();
 		this.userMapping = setMappingKeys(true);
 		this.plaintext = plaintext;	
 		this.ciphertext = encrypt();
 	}
 
 	public String getCiphertext() {
 		return ciphertext;
 	}
 
 	public String getPlaintext() {
 		return plaintext;
 	}
 
 	public void setPlaintext(String plaintext) {
 		this.plaintext = plaintext;
		generateMappingKeys();
 		this.ciphertext = encrypt();
 	}
 
 	public CryptogramPair[] getSolutionMapping() {
 		return solutionMapping;
 	}
 
 	public CryptogramPair[] getUserMapping() {
 		return userMapping;
 	}
 
 
 	/*
 	 *  @author Lauren Slusky
 	 *  @date May 26 2013
 	 *  @title getUserPlaintextFromCiphertext
 	 *  @return plaintext char that the user thinks is mapped to a given ciphertext
 	 */
 	public char getUserPlaintextFromCiphertext(char ciphertextc)
 	{
 		int index = Character.toUpperCase(ciphertextc) - 'A';
 		return Character.toUpperCase(this.userMapping[index].getPlainc());
 	}
 
 	/*
 	 *  @author Lauren Slusky
 	 *  @date May 26 2013
 	 *  @title setPlainText
 	 *  @return maps the users choice of plaintext char for a given ciphertext char
 	 */
 	public void setUserPlaintextForCiphertext(char plaintextc, char ciphertextc)
 	{
 		int index = Character.toUpperCase(ciphertextc) - 'A';	// spot the in the array of the plaintext ciphertext pairing
 		this.userMapping[index].setPlainc(Character.toUpperCase(plaintextc));	//map the ciphertext to the given plaintext
 	}
 	
 
 
 	/*
 	 *  @author Lauren Slusky
 	 *  @date May 26 2013
 	 *  @title isCompleted
 	 *  @paeam String userString
 	 *  @return checks if userString which is the user's plaintext, matches the original plaintext (ignorescase)
 	 */	
 	public boolean isCompleted()
 	{		
 		String userString = decrypt(this.userMapping);
 		if(userString == null)
 		{
 			throw new IllegalArgumentException();
 		}
 		return userString.equalsIgnoreCase(this.plaintext);
 	}
 	
 	/*
 	 *  @author Lauren Slusky
 	 *  @date May 26 2013
 	 *  @title setMappingKeys
 	 *  @return an array of type Cryptogram pair with the plain text A - Z mapped
 	 */	
 	private CryptogramPair[] setMappingKeys(boolean orderByCipherText)
 	{
 		char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
 		CryptogramPair[] map = new CryptogramPair[ALPHABET_SIZE];
 		for(int i = 0; i < alphabet.length; i ++)
 		{
 			char letter = alphabet[i];
 			if(orderByCipherText)
 			{
 				map[i] = new CryptogramPair('\0', letter);	//no ciphertext char to map yet
 			}
 			else
 			{
 				map[i] = new CryptogramPair(letter, '\0');	//no ciphertext char to map yet
 			}
 		}
 		return map;
 	}
 	
 	/*
 	 *  @author Lauren Slusky
 	 *  @date May 26 2013
 	 *  @title generateMappingKeys
 	 *  @desription Takes a randomly shuffled array of the alphabet and assigns the each char to the charMapping array[i]'s ciphertext
 	 */	
 	private void generateMappingKeys() 
 	{	
 		char[] alphabet = shuffleAlphabet();
 		
 		for(int i = 0; i < alphabet.length; i ++)
 		{
 			char letter = alphabet[i];
 			this.solutionMapping[i].setCipherc(letter);
 		}
 	}
 
 	/*
 	 *  @author Lauren Slusky
 	 *  @date May 26 2013
 	 *  @title shuffleAlphabet
 	 *  @return a char array of the alphabet where no letter is in the same place
 	 *  This Method Uses Sattolo's Algorithm URL(http://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#Sattolo.27s_algorithm)
 	 */	
 	private char[] shuffleAlphabet() 
 	{
 		char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
 		int posn = ALPHABET_SIZE;
 		int newPosn = 0;
 		char temp;
 		
 		while(posn > 1)
 		{
 			posn = posn - 1;
 			// we use posn - 1 so that way there is no chance of the random element being posn and the element swapping places with itself
			newPosn = (int)(Math.random() * (posn - 1));
 			
 			// regular old swapskie
 			temp = alphabet[posn];
 			alphabet[posn] = alphabet[newPosn];
 			alphabet[newPosn] = temp;
 		}
 		return alphabet;
 	}
 
 	/*
 	 *  @author Lauren Slusky
 	 *  @date May 26 2013
 	 *  @title encrypt
 	 *  @return generates random ciphertext based on plain text and charMapping array
 	 */	
 	private String encrypt()
 	{
 		String ciphertext = "";
 		String plain = this.plaintext;
 		int index = 0;
 		
 		if(this.plaintext != null && this.plaintext.length () > 0)
 		{
 			plain = plain.toUpperCase();
 			
 			for(int i = 0; i < plain.length(); i++)
 			{
 				char temp = plain.charAt(i);
 				
 				//alphabet character
 				if(temp >= 'A' && temp <= 'Z')
 				{
 					index = temp - 'A';			// gives me index in charMapping for the cipher text letter that matches this plaintext char
 					ciphertext += this.solutionMapping[index].getCipherc();
 				}
 				//punctuation, spaces, etc
 				else
 				{
 					ciphertext += temp;
 				}
 			}
 		}
 		
 		return ciphertext;
 	}
 
 	/*
 	 *  @author Lauren Slusky
 	 *  @date May 26 2013
 	 *  @title decrypt
 	 *  @param mapping - mapping used to decrypt the cipher text
 	 *  @return decodes messages based on charMapping and cipher text
 	 */	
 	public String decrypt(CryptogramPair[] mapping) 
 	{
 		String plaintext = "";
 		char[] decryptKey = reOrderMappingByDecrypt(mapping);	// an array of plaintext characters ordered by ciphertext character index
 		String ciphertext = this.ciphertext;
 		int index = 0;
 		
 		if(this.ciphertext != null && this.ciphertext.length () > 0)
 		{
 			for(int i = 0; i < ciphertext.length(); i++)
 			{
 				char temp = ciphertext.charAt(i);
 				
 				//alphabet character
 				if(temp >= 'A' && temp <= 'Z')
 				{
 					index = temp - 'A';				// gives me index associated with the ciphertext character (A = 0, B = 1) in charMapping 
 					plaintext += decryptKey[index];
 				}
 				//punctuation, spaces, etc
 				else
 				{
 					plaintext += temp;
 				}
 			}
 		}
 		
 		return plaintext;
 	}
 
 	/*
 	 *  @author Lauren Slusky
 	 *  @date May 26 2013
 	 *  @title reOrderMappingByDecrypt
 	 *  @return an array of plaintext characters that are in the position of it's paired cipher text
 	 *  	i.e if A plaintext maps to Z ciphertext then A is in spot 25 of this array
 	 */	
 	private char[] reOrderMappingByDecrypt(CryptogramPair[] tofix) 
 	{
 		char[] reorder = new char[ALPHABET_SIZE];
 		int index = 0;
 		
 		for(int i = 0; i < tofix.length; i++)
 		{
 			index = tofix[i].getCipherc() - 'A';		// get ciphertext character index
 			reorder[index] = tofix[i].getPlainc(); // put plaintext character in that spot
 		}
 		return reorder;
 	}
 }
