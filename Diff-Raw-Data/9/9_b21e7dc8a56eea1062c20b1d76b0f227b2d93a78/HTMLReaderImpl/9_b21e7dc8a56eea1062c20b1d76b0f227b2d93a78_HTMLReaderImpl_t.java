 package webcrawler;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * Implementation of the HMTLReader Interface
  * 
  * @author Peter Hayes
  * @author Iain Ritchie
  */
 
 public class HTMLReaderImpl implements HTMLReader {
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see webcrawler.HTMLReader#readUntil(java.io.InputStream, char, char)
 	 */
 	public boolean readUntil(InputStream in, char ch1, char ch2)
 			throws IOException {
 		
 		if (in == null) {
 			return false;
 		}
 		
 		char ch1LowerCase = Character.toLowerCase(ch1);
 		char ch2LowerCase = Character.toLowerCase(ch2);
 		
 		int b;
 		char charToCompare;
 		
 		do {
 			b = in.read();
 			charToCompare = (char) Character.toLowerCase(b);
 			
 		} while (b >= 0 && charToCompare != ch1LowerCase
 				&& charToCompare != ch2LowerCase);
 		return (charToCompare == ch1LowerCase);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see webcrawler.HTMLReader#skipSpace(java.io.InputStream, char)
 	 */
 	public char skipSpace(InputStream in, char ch) throws IOException {
 		
 		int b;
 		
 		do {
 			b = in.read();
		} while (b >= 0 && Character.isWhitespace(b));
 		
 		return (b == ch ? Character.MIN_VALUE : (char) b);
 		
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see webcrawler.HTMLReader#readString(java.io.InputStream, char, char)
 	 */
 	public String readString(InputStream in, char ch1, char ch2)
 			throws IOException {
 		
 		int b;
 		StringBuilder sb = new StringBuilder();
 		
 		do {
 			b = in.read();
 			sb.append((char) b);
 		} while (b >= 0 && b != ch1 && b != ch2);
 		
 		return ((b == ch1 || b == -1) ? sb.toString() : null);
 		
 	}
 	
 }
