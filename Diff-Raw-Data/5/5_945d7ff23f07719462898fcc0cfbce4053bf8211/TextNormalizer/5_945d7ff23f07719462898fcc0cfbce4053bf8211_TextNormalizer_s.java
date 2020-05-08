 package com.bananity.text;
 
 
 // Apache Commons
 import org.apache.commons.lang3.text.translate.UnicodeEscaper;
 import org.apache.commons.lang3.text.translate.UnicodeUnescaper;
 
 // Java Text
 import java.text.Normalizer;
 
 
 /**
  * This class encapsulates text normalization code (manly for UTF8 encoding)
  *
  * @author Andreu Correa Casablanca
  */
 public class TextNormalizer
 {
 	private static UnicodeEscaper 		escaper 	= UnicodeEscaper.above(127);
 	private static UnicodeUnescaper 	unescaper 	= new UnicodeUnescaper();
 
 	/**
 	 * This method normalizes UTF8 strings into its canonical form
 	 *
 	 * @param 	text 	Text to be normalized
 	 * @return 			normalized text
 	 */
 	public static String normalizeText (final String text) {
 		if (!Normalizer.isNormalized(text, Normalizer.Form.NFC)) {
 			return Normalizer.normalize(text, Normalizer.Form.NFC);
 		} else {
 			return text;
 		}
 	}
 
 	/**
	 * This method normalizes UTF8 strings into its canonical form (and, if you want, unescapes \uXXXX substrings)
 	 *
 	 * @param 	text 		Text to be normalized
	 * @param 	unescape 	Indicates if you want to unescape \uXXXX substrings
 	 * @return 				normalized text
 	 */
 	public static String normalizeText (final String text, boolean unescape) {
 		if (unescape) {
 			return normalizeText(unescaper.translate(text));
 		} else {
 			return normalizeText(text);
 		}
 	}
 
 	/**
 	 * This method flattens UTF8 strings (removes accents, diacritical marks and other marks, and also undercases the text)
 	 * 
 	 * @param 	text 	Text to be flattened
 	 * @return 			flattened text
 	 */
 	public static String flattenText (final String text) {
 		String nfdNormalizedText;
 
 		if (!Normalizer.isNormalized(text, Normalizer.Form.NFD)) {
 			nfdNormalizedText = Normalizer.normalize(text, Normalizer.Form.NFD).toLowerCase();
 		} else {
 			nfdNormalizedText = text.toLowerCase();
 		}
 
 		return nfdNormalizedText.replaceAll("\\p{IsM}", "");
 	}
 
 	/**
 	 *
 	 */
 	public static String escapeUnicode (final String text) {
 		return escaper.translate(text);
 	}
 
 	/**
 	 *
 	 */
 	public static String unescapeUnicode (final String text) {
 		return unescaper.translate(text);
 	}
 }
