 /**
  * 
  */
 package org.smslib.util;
 
 /**
  * @author Alex Anderson
  */
 public enum GsmAlphabetBaseTable implements GsmCharacterTable {
 	DEFAULT(TpduUtils.TP_UDH_IEI_NLI_DEFAULT,
			'@', '', '$', '', '', '', '', '', '', '', '\n', '', '', '\r', '', '', '?', '_',
 			'\u03A6', // GREEK CAPITAL LETTER PHI
 			'\u0393', // GREEK CAPITAL LETTER GAMMA
 			'\u039B', // GREEK CAPITAL LETTER LAMDA
 			'\u03A9', // GREEK CAPITAL LETTER OMEGA
 			'\u03A0', // GREEK CAPITAL LETTER PI
 			'\u03A8', // GREEK CAPITAL LETTER PSI
 			'\u03A3', // GREEK CAPITAL LETTER SIGMA
 			'\u0398', // GREEK CAPITAL LETTER THETA
 			'\u039E', // GREEK CAPITAL LETTER XI
 			'\u00A0', // Escape to alphabet extension or national language single shift table.  "A receiving entity which does not understand the meaning of this escape mechanism shall display it as a space character."
 			                                                            '', '', '', '',
			' ', '!', '"', '#', '', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', 
 			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
 			'', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',  
 			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '', '', '', '', '',
 			'', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
 			'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '', '', '', '', ''
 		);
 	
 //> PROPERTIES
 	/** The National Language Identifier for the language encoded in this table. */
 	private final int nli;
 	/** The characters available in this alphabet.  The position in this array indicates the byte value
 	 * of the character when encoded in the alphabet. */
 	private char[] characters;
 	
 //> CONSTRUCTORS
 	private GsmAlphabetBaseTable(int nli, char... characters) {
 		assert(characters.length == 128) : "Character array was incorrect size.  Expected 128, but was: " + characters.length;
 		
 		this.nli = nli;
 		this.characters = characters;
 	}
 	
 //> ACCESSORS
 	public int getNli() {
 		return nli;
 	}
 	
 	public char getCharacter(int byteValue) {
 		return characters[byteValue];
 	}
 	
 	public int getByteValue(char character) {
 		for (int i = 0; i < characters.length; i++) {
 			if(characters[i] == character) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 //> STATIC HELPERS
 	public static final GsmAlphabetBaseTable getFromNli(int nli) {
 		for(GsmAlphabetBaseTable table : values()) {
 			if(table.getNli() == nli) {
 				return table;
 			}
 		}
 		return null;
 	}
 
 	/** @return <code>true</code> iff the indicated language is supported */
 	public static boolean isNliSupported(int nli) {
 		for(GsmAlphabetBaseTable table : values()) {
 			if(table.getNli() == nli) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
