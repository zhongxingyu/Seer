 package services;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.commons.collections.ListUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.lang.Validate;
 
 import models.PasswordPolicy;
 
 public class RandomPasswordUtils {
 
 	public static String generateRandomPassword(PasswordPolicy policy) {
 		Validate.notNull(policy);
 		Integer[] countDistribution = charsCountDistribution(policy);
 		
 		int upperCaseCount = countDistribution[0];
 		int lowerCaseCount = countDistribution[1];
 		int numericCount = countDistribution[2];
 		int specialCount = countDistribution[3];
 		
 		List<Character> chars = new ArrayList<Character>();
 		
 		addAllChars(chars, randomUpperCase(upperCaseCount));
 		addAllChars(chars, randomLowerCase(lowerCaseCount));
 		addAllChars(chars, randomNumeric(numericCount));
 		addAllChars(chars, randomSpecial(specialCount));
 
 		Collections.shuffle(chars);
 		
 		char[] charArray = ArrayUtils.toPrimitive(chars.toArray(new Character[chars.size()]));
 		
 		return new String(charArray);
 		
 	}
 
 	/** genera un string con N mayúsculas */
 	private static String randomUpperCase(int count) {
 		return RandomStringUtils.randomAlphabetic(count).toUpperCase();
 	}
 	
 	/** genera un string con N minúsculas */
 	private static String randomLowerCase(int count) {
 		return RandomStringUtils.randomAlphabetic(count).toLowerCase();
 	}
 	
 	/** genera un string con N dígitos */
 	private static String randomNumeric(int count) {
 		return RandomStringUtils.randomNumeric(count);
 	}
 	
 	/** genera un string con N mayúsculas */
 	private static String randomSpecial(int count) {
 		return RandomStringUtils.random(count, 0, PasswordPolicy.SPECIAL_CHARS.length - 1,
 										false, false, PasswordPolicy.SPECIAL_CHARS);
 	}
 	
 	/** 1 o 0 */
 	private static int minimum(final Boolean b) {
 		return b ? 1 : 0;
 	}
 
 	/** entero menor o mayor a N. el mínimo depende del valor booleano (0 o 1)  */
 	private static int randomInt(int max) {
 		Random rand = new Random(new Date().getTime());
 		return rand.nextInt(max + 1);
 	}
 
 	/**
 	 * Devuelve un arreglo con:
 	 * 0. cantidad de mayúsculas
 	 * 1. cantidad de minúsculas
 	 * 2. cantidad de números
 	 * 3. cantidad de caracateres especiales 
 	 */
 	private static Integer[] charsCountDistribution(PasswordPolicy policy) {
 		int upperCaseCount = minimum(policy.useUpperCaseLetters);
 		int lowerCaseCount = minimum(policy.useLowerCaseLetters);
 		int numericCount = minimum(policy.useNumbers);
 		int specialCount = minimum(policy.useSpecialCharsLetters);
 		
 		// genero claves 5 characteres más largas que el mínimo
 		int missing = policy.passwordLength - upperCaseCount - lowerCaseCount - numericCount - specialCount + 5;
 		
		if (missing < 0) {
			throw new IllegalStateException("la política es inconsistente.");
 		}
 		
		int moreUpper = randomInt(missing);
		upperCaseCount += moreUpper;
		missing -= moreUpper;
		
		int moreLower = randomInt(missing);
		lowerCaseCount += moreLower;
		missing -= moreLower;
		
		int moreNumeric = randomInt(missing);
		numericCount += moreNumeric;
		missing -= moreNumeric;

		numericCount += missing;
		
 		return new Integer[] {upperCaseCount, lowerCaseCount, numericCount, specialCount};
 	}
 
 	private static void addAllChars(List<Character> chars, String st) {
//		char[] charArray = randomUpperCase(upperCaseCount).toCharArray();
//		List<Character> asList = Arrays.asList(ArrayUtils.toObject(charArray));
//		chars.addAll(asList);
 		chars.addAll(Arrays.asList(ArrayUtils.toObject(st.toCharArray())));
 	}
 }
