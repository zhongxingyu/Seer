 package controllers;
 
 import org.apache.commons.codec.binary.BinaryCodec;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import play.mvc.Controller;
 
 public class Binary extends Controller {
 	// Workaround for Codec API Bug
 	private static final BinaryCodec BINARY_CODEC = new BinaryCodec();
 
 	public static void index() {
 		render();
 	}
 
 	public static void binaryToText(Byte endian, String binary) {
 		if (binary != null && binary.length() > 0) {
			// Remove all whitespace without asking; the operation is fast
			// enough that we don't need to waste time checking.
			binary = binary.replaceAll("\\s", "");
 
 			byte[] result = BINARY_CODEC.toByteArray(binary);
 
 			if (result != null) {
 				// If the result is big endian, we have to reverse the byte order first
 				if(endian == 0) {
 					ArrayUtils.reverse(result);
 				}
 
 				binary = new String(result);
 			} else
 				binary = "[ Unable to Base64 decode text ]";
 		}
 
 		renderText(binary);
 	}
 
 	public static void textToBinary(Boolean spaces, Byte endian, String text) {
 		if (text != null && text.length() > 0) {
 			byte[] bytes = text.getBytes();
 
 			// If the result is big endian, we have to reverse the byte order first
 			if(endian == 0) {
 				ArrayUtils.reverse(bytes);
 			}
 
 			text = new String(BinaryCodec.toAsciiChars(bytes));
 
 			if(spaces) {
 				StringBuilder builder = new StringBuilder(text.length() * 2);
 
 				for(int i = 0, size = text.length(); i < size; i++) {
 					if(i != 0 && i % 8 == 0) {
 						builder.append(' ');
 					}
 
 					builder.append(text.charAt(i));
 				}
 
 				text = builder.toString();
 			}
 		}
 
 		renderText(text);
 	}
 }
