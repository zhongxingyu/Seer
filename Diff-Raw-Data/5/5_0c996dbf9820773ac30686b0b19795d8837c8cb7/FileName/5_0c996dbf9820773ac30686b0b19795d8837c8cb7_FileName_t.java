 /**
  * 
  */
 package org.guanxi.common.filters;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * <p>This class features an encoder and decoder that will take an
  * arbitrary String and produce a String that can be used as a
  * file name on both Unix and Windows.</p>
  * 
  * @author matthew
  *
  */
 public class FileName {
 	/**
 	 * This is a cached version of the file name compliant
 	 * encoder.
 	 */
 	private static Encoder encoder;
 	/**
 	 * This is the destination of the encoder and will
 	 * contain the encoded string.
 	 */
 	private static StringWriter encoderBuffer;
 	
 	/**
 	 * This is a cached version of the file name compliant
 	 * decoder.
 	 */
 	private static GenericDecoder decoder;
 	/**
 	 * This is the destination of the decoder and will
 	 * contain the decoded string.
 	 */
 	private static StringWriter decoderBuffer;
 	
 	/**
 	 * This initialises the cached encoder and decoder.
 	 */
 	static {
 		encoderBuffer = new StringWriter();
 		encoder = new Encoder(encoderBuffer);
 		
 		decoderBuffer = new StringWriter();
 		decoder = new GenericDecoder(decoderBuffer, '%');
 	}
 	
 	/**
 	 * This will encode the String provided in an file name safe way.
 	 * 
 	 * @param string
 	 * @return
 	 */
 	public static String encode(String string) {
 		encoderBuffer.getBuffer().setLength(0);
 		
 		try {
 			encoder.write(string);
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e); 
 			// Its better to throw than eat.
 			// The exception becomes unchecked because encoding should never error.
 		}
 		return encoderBuffer.toString();
 	}
 	
 	/**
 	 * This will decode a String encoded in by the {@link #encode(String)} method.
 	 *  
 	 * @param string
 	 * @return
 	 */
 	public static String decode(String string) {
 		decoderBuffer.getBuffer().setLength(0);
 		
 		try {
 			decoder.write(string);
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e); 
 			// Its better to throw than eat.
 			// Decoding can fail however it cannot (really) fail with an IOException.
 			// Decoding can throw NumberFormatExceptions if the String is badly formed.
 		}
 		return decoderBuffer.toString();
 	}
 	
 	/**
 	 * <p>This encoder implements an encoding format that produces file name
 	 * safe strings from arbitrary strings.</p>
 	 * 
 	 * <p>The rules can be summarised as follows:
 	 * <ul>
 	 * 	<li>The following characters must be encoded: ; : \ / %</li>
 	 *  <li>The encoding is done by converting the character into
 	 *  	a two digit hex code. This is then preceded by a percent (%).</li>
 	 * </ul></p>
 	 * 
 	 * @author matthew
 	 */
 	private static class Encoder extends GenericEncoder {
 		
 		/**
 		 * This contains all of the characters that must be escaped.
 		 * The value of this map is the escaped form, in a format that
 		 * can be written directly to the OutputStream.
 		 */
 		private static Map<Character, String> escapeCharacters;
 		
 		static {
 			escapeCharacters = new TreeMap<Character, String>();
 			
 			for ( char character : new char[]{ ':', ';', '\\', '/' } ) {
				escapeCharacters.put(character, String.format("%%%02X", (int)character) );
 			}
 		}
 		
 		/**
 		 * This creates a new file name encoder.
 		 * 
 		 * @param out
 		 */
 		public Encoder(Writer out) {
 			super(out);
 		}
 
 		/**
 		 * This will escape the provided character.
 		 * 
 		 * @param c
 		 */
 		protected String escape(char c) {
 			if ( !escapeCharacters.containsKey(c) ) {
 				return String.format("%%%02X", c);
 			}
 			
			return escapeCharacters.get(c);
 		}
 
 		/**
 		 * This will test the provided character to determine if
 		 * it should be escaped.
 		 * 
 		 * @param c
 		 */
 		protected boolean requiresEscaping(char c) {
 			return escapeCharacters.containsKey(c) || Character.isISOControl(c);
 		}
 	}
 }
