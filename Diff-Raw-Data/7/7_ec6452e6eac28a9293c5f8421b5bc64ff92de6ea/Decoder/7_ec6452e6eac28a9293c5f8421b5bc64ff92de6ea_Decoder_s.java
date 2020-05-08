 import java.util.LinkedList;
 
 class Decoder {
 	private char[] zero = { '!', '"', '£', '$', '%', '&', '/', '(', ')', '=', '?', '^' };
 	private char[] unos = { 'd', 'D', 'o', 'O', '0', 'g', 'G', 'e', 'E', '1' };
 	private char[] dels = { '|', '_', 'L', 'Q', 'F', '+', '-', ':' };
 	
 	private int salt;
 
 	public String decode(String txt) {
 		LinkedList buffs = new LinkedList();
 		String ret = "";
 
 		if ( txt.length() < 3 )
 			return "";
 
		this.salt = Integer.parseInt(txt.substring(txt.length()-2,txt.length())); 
 		String buff = "";
 		
 		for ( int i = 0 ; i < txt.length() - 2 ; i++ )
 			buff+= this.getChar(txt.charAt(i));
 		
 		String[] buff1 = buff.split(" ");
 		int output[] = new int[buff1.length];
 
 	       	for ( int i = 0 ; i < buff1.length ; i++ ) {
 			if ( ! buff1[i].equals("") )
 				output[i] = Integer.parseInt(buff1[i], 2) ^ this.salt;
 			else
 				output[i] = 0;
 		}
 		buff = "";
 		for ( int i = 0 ; i < output.length ; i++ )
 			if ( output[i] != 0 ) 
 				buff += ((char) (output[i]));		
 
 		return buff;
 	}
 	
 	private String getChar(char ch) {
 		char ret = ' ';
 		
 		for ( int i = 0 ; i < this.zero.length ; i++ )
 			if ( ch == this.zero[i] )
 				ret = '1';
 		for ( int i = 0 ; i < this.unos.length ; i++ )
 			if ( ch == this.unos[i] )
 				ret = '0';
 		for ( int i = 0 ; i < this.dels.length ; i++ )
 			if ( ch == this.dels[i] )
 				ret = ' ';
 		
 		return ""+ret;
 	}
 
 }
 		
 		
