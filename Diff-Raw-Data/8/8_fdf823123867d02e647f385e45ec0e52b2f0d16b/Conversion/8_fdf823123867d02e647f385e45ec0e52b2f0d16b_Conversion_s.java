 class Conversion {
 	final static int MIN_RADIX = 1;
 	final static int MAX_RADIX = 10;
 	final static int SIG_DIGITS_AFTER_DOT = 10;
 	
 	static String convert(String number, int radix) {
 		if (number == null)			return null;
 		if (radix <= MIN_RADIX)		return null;
 		if (radix >= MAX_RADIX)		return null;
 		
 		String sign = "";		
 		if (number.charAt(0) == '+' || number.charAt(0) == '-') {
 			if (number.length() == 1)		return null;
 			if (number.charAt(0) == '-')	sign = "-";
 			number = number.substring(1);
 		}
 		
 		int dot = -1;
 		for (int i = 0; i < number.length(); ++i) {
 			char c = number.charAt(i);
 			if (c == '.') {
 				if (dot != -1)	return null;
 				else			dot = i;
 			}
 			else if (c < '0' || c > '9')	return null;
 		}
 		
 		int wholeNo;
 		if (dot == -1) {
 			wholeNo = Integer.parseInt(number);
 			return sign + convertWholeNumber(wholeNo, radix);
 		}
 		
 		wholeNo = Integer.parseInt(number.substring(0, dot));
 		String w = convertWholeNumber(wholeNo, radix);
		
 		double fractionalPart = Double.parseDouble(number.substring(dot));
		return sign + w + fractionalPart;
 	}
 	
 	static String convertWholeNumber(int wholeNo, int radix) {
 		if (wholeNo == 0)	return "0";
 		
 		String s = "";
 		while (wholeNo != 0) {
 			int remainder = wholeNo % radix;
 			wholeNo = wholeNo / radix;
 			s = remainder + s;
 		}
 		return s;	
 	}
 	
 	static String convertFractionalPart(double fractionalPart, int radix) {
 		if (fractionalPart == 0)	return ".0";
		
 		String s = ".";
 		for (int i = 0; i < SIG_DIGITS_AFTER_DOT; ++i) {
 			fractionalPart *= radix;
 			int digit = (int) fractionalPart;
 			fractionalPart -= digit;
 			s += digit;
 			
 			if (fractionalPart == 0)	break;
 		}
 		return s;
 	}
 	
 	public static void main(String[] args) {
 		System.out.println("convert(\"-1abc.23\", 2) = " + convert("-1abc.23", 2));
 		System.out.println("convert(\"-1\", 2) = " + convert("-1", 2));
 		System.out.println("convert(\"8.5\", 2) = " + convert("8.5", 2));
 		System.out.println("convert(\"0\", 5) = " + convert("0", 5));
 		System.out.println("convert(\"-16\", 8) = " + convert("-16", 8));
 		System.out.println("convert(\"+234.3452.34\", 4) = " + convert("+234.3452.34", 4));
 		System.out.println("convert(\"3.3\", 2) = " + convert("3.3", 2));
 	}
 }
