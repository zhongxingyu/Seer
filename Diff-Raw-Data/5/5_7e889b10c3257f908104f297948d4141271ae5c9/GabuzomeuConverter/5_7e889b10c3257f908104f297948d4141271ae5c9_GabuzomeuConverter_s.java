 package eu.ttbox.gabuzomeu.service;
 
 import java.math.BigInteger;
 import java.util.HashMap;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.util.SparseArray;
 import eu.ttbox.gabuzomeu.R;
 
 public class GabuzomeuConverter {
 
 	private static final String TAG = "GabuzomeuConverter";
 
 	private static boolean isInitShadokDigit = false;
 
 	private Context mContext;
 	private static SparseArray<Character> BASE10_TO_SHADOK_DIGIT;
	private   SparseArray<Character> SHADOK_DIGIT_TO_BASE10;
 	private static SparseArray<String> SHADOK_DIGIT_NAME;
 
 	private static String IS_SHADOK_DIGIT;
 
 	public final static boolean isNumberPartKey(char c) {
 		return (c >= '0' && c <= '9');// || c == '.';
 	}
 
 	public GabuzomeuConverter(Context mContext) {
 		super();
 		this.mContext = mContext;
 		intitShadokDigit(mContext);
 	}
 
 	private void intitShadokDigit(Context mContext) {
 		if (!isInitShadokDigit) {
 			Resources res = mContext.getResources();
 			// Digit
 //			String digitGa = res.getString(R.string.digitGa);
 //			String digitBu = res.getString(R.string.digitBu);
 //			String digitZo = res.getString(R.string.digitZo);
 //			String digitMeu = res.getString(R.string.digitMeu);
 			
 			char digitGa = res.getString(R.string.digitGa).charAt(0);
 			char digitBu = res.getString(R.string.digitBu).charAt(0);
 			char digitZo = res.getString(R.string.digitZo).charAt(0);
 			char digitMeu = res.getString(R.string.digitMeu).charAt(0);
 			// is Shadow digit
 			StringBuilder isDigitBuilder = new StringBuilder();
 			isDigitBuilder.append(digitGa).append(digitBu).append(digitZo).append(digitMeu);
 			IS_SHADOK_DIGIT = isDigitBuilder.toString();
 			// Digit Map
 			SparseArray<Character> shadokDigit = new SparseArray<Character>(4);
 			shadokDigit.put('0', digitGa);
 			shadokDigit.put('1', digitBu);
 			shadokDigit.put('2', digitZo);
 			shadokDigit.put('3', digitMeu);
 			BASE10_TO_SHADOK_DIGIT = shadokDigit;
 
 			SparseArray<Character> nbToshadokDigit = new SparseArray<Character>(4);
 			nbToshadokDigit.put(digitGa, '0');
 			nbToshadokDigit.put(digitBu, '1');
 			nbToshadokDigit.put(digitZo, '2');
 			nbToshadokDigit.put(digitMeu, '3');
 			SHADOK_DIGIT_TO_BASE10 = nbToshadokDigit;
 			
 			// Digit Name Map
 			SparseArray<String> shadokDigitName = new SparseArray<String>(4);
 			shadokDigitName.put('0', res.getString(R.string.digitNameGa));
 			shadokDigitName.put('1', res.getString(R.string.digitNameBu));
 			shadokDigitName.put('2', res.getString(R.string.digitNameZo));
 			shadokDigitName.put('3', res.getString(R.string.digitNameMeu));
 			SHADOK_DIGIT_NAME = shadokDigitName;
 			isInitShadokDigit = true;
 		}
 	}
 
 	public void convertBase10NumberToShadokDigit(String base10String, StringBuilder shadokDigit, StringBuilder shadokDigitName) {
 		BigInteger base10 = new BigInteger(base10String);
 		String base4 = base10.toString(4);
 		convertBase4NumberToShadokDigit(base4, shadokDigit, shadokDigitName);
 	}
 
 	public void convertShadokDigitToBase10Digit(String shadokString, StringBuilder base10DigitDest) {
 		int shadokStringSize = shadokString.length();
 		// Convert To 0123 format
 		StringBuilder sb = new StringBuilder(shadokStringSize);
 		for (char c : shadokString.toCharArray()) {
//			Log.d(TAG, String.format("Want convert shadok [%s] with map %s", c, SHADOK_DIGIT_TO_BASE10 ));
 			char base10Digit = SHADOK_DIGIT_TO_BASE10.get( c);
 //			Log.d(TAG, String.format("Want convert shadok [%s] ==> [%s]", c, base10Digit));
 			sb.append(base10Digit);
 		}
 		// Chnaging to Base4
 		
 //		Integer	base10 = Integer.valueOf(sb.toString(), 4); 
 		BigInteger base10 = new BigInteger(sb.toString(), 4);
 		Log.d(TAG, String.format("Convert Shadok %s ==>  %s", sb.toString(), base10));
 		base10DigitDest.append(base10.toString());
 	}
 
 	public void convertBase4NumberToShadokDigit(String text, StringBuilder shadokDigitDest, StringBuilder shadokDigitNameDest) {
 		for (char c : text.toCharArray()) {
 			if (shadokDigitDest != null) {
 				char shadDigit = BASE10_TO_SHADOK_DIGIT.get(c);
 				shadokDigitDest.append(shadDigit);
 			}
 			if (shadokDigitNameDest != null) {
 				String shadName = SHADOK_DIGIT_NAME.get(c);
 				shadokDigitNameDest.append(shadName);
 			}
 		}
 	}
 
 	public void encodeEquationToShadokCode(CharSequence base10, StringBuilder shadokDigit, StringBuilder shadokDigitName) {
 		int baseSize = base10.length();
 		StringBuilder current = new StringBuilder(baseSize + baseSize);
 		boolean isShadokDigit = shadokDigit != null;
 		boolean isShadokDigitName = shadokDigitName != null;
 		for (int i = 0; i < baseSize; i++) {
 			char c = base10.charAt(i);
 			if (isNumberPartKey(c)) {
 				current.append(c);
 			} else {
 				int currentSize = current.length();
 				if (currentSize > 0) {
 					convertBase10NumberToShadokDigit(current.toString(), shadokDigit, shadokDigitName);
 					current.delete(0, currentSize);
 				}
 				if (isShadokDigit) {
 					shadokDigit.append(c);
 				}
 				if (isShadokDigitName) {
 					shadokDigitName.append(c);
 				}
 			}
 		}
 		// Clear Cache
 		if (current.length() > 0) {
 			convertBase10NumberToShadokDigit(current.toString(), shadokDigit, shadokDigitName);
 		}
 	}
 
 	public void decodeShadokDigitEquationToBase10Code(CharSequence base4, StringBuilder base10Digit) {
 		int baseSize = base4.length();
 		StringBuilder current = new StringBuilder(baseSize);
 		for (int i = 0; i < baseSize; i++) {
 			char c = base4.charAt(i);
 			if (isNumberShadokKey(c)) {
 				current.append(c); 
 			} else {
 				int currentSize = current.length();
 				if (currentSize > 0) {
 					convertShadokDigitToBase10Digit(current.toString(), base10Digit);
 					current.delete(0, currentSize);
 				}  
 				base10Digit.append(c); 
 			} 
 		}
 		// Clear Cache
 		if (current.length() > 0) {
 			convertShadokDigitToBase10Digit(current.toString(), base10Digit);
 		} 
 	}
 
 	public final boolean isNumberShadokKey(char c) {
 		return IS_SHADOK_DIGIT.indexOf(c) > -1;
 	}
 
 	public final char getShadokKey(char c) {
 		return BASE10_TO_SHADOK_DIGIT.get(c);
 	}
 
 	public static Typeface getSymbolFont(Context context) {
 		Typeface font = Typeface.createFromAsset(context.getAssets(), "dejavu_serif.ttf");
 		return font;
 	}
 
 	// @Deprecated
 	// public String convertBase10NumberToShadokDigit(String base10String,
 	// SparseArray<String> shadokCode) {
 	// BigInteger base10 = new BigInteger(base10String);
 	// return convertBase10NumberToShadokDigit(base10, shadokCode);
 	// }
 	//
 	// @Deprecated
 	// public String convertBase10NumberToShadokDigit(BigInteger base10,
 	// SparseArray<String> shadokCode) {
 	// String base4 = base10.toString(4);
 	// return convertBase4NumberToShadokDigit(base4, shadokCode);
 	// }
 	//
 	// @Deprecated
 	// public String convertBase4NumberToShadokDigit(String text,
 	// SparseArray<String> shadokCode) {
 	// int textSize = text.length();
 	// StringBuilder sb = new StringBuilder(textSize);
 	// for (char c : text.toCharArray()) {
 	// String shad = shadokCode.get(c);
 	// sb.append(shad);
 	// }
 	// return sb.toString();
 	// }
 
 	// @Deprecated
 	// public String encodeEquationToShadokCode(CharSequence base10,
 	// SparseArray<String> shadokCode) {
 	// int baseSize = base10.length();
 	// StringBuilder sb = new StringBuilder(baseSize + baseSize);
 	// StringBuilder current = new StringBuilder(baseSize + baseSize);
 	// for (int i = 0; i < baseSize; i++) {
 	// char c = base10.charAt(i);
 	// if (isNumberPartKey(c)) {
 	// current.append(c);
 	// } else {
 	// int currentSize = current.length();
 	// if (currentSize > 0) {
 	// String shadokCodes = convertBase10NumberToShadokDigit(current.toString(),
 	// shadokCode);
 	// sb.append(shadokCodes);
 	// current.delete(0, currentSize);
 	// }
 	// sb.append(c);
 	// }
 	// }
 	// // Clear Cache
 	// if (current.length() > 0) {
 	// String shadokCodes = convertBase10NumberToShadokDigit(current.toString(),
 	// shadokCode);
 	// sb.append(shadokCodes);
 	// }
 	// return sb.toString();
 	// }
 
 	public static String encodeTobase4(int base10) {
 		String converted = Integer.toString(base10, 4);
 		// String shadok = converted.replaceAll("0", "G").replaceAll("1",
 		// "B").replaceAll("2", "Z").replaceAll("3", "M");
 		String shadok = converted.replaceAll("0", "GA ").replaceAll("1", "BU ").replaceAll("2", "ZO ").replaceAll("3", "MEU ");
 		Log.d(TAG, String.format("Nombre %s  =>  %s  => %s", base10, converted, shadok));
 		return shadok;
 	}
 
 	public static String encodeNumberTobase4(Integer base10) {
 		return Integer.toString(base10, 4);
 	}
 
 	public static Integer decodeTobase10(String shadok) {
 		String base4 = shadok.replaceAll("GA", "0").replaceAll("BU", "1").replaceAll("ZO", "2").replaceAll("MEU", "3").replaceAll(" ", "");
 		Integer base10 = Integer.valueOf(base4, 4);
 		// String shadok = converted.replaceAll("0", "G").replaceAll("1",
 		// "B").replaceAll("2", "Z").replaceAll("3", "M");
 		Log.d(TAG, String.format("Shadok %s  =>  %s  => %s", shadok, base4, base10));
 		return base10;
 	}
 
 }
