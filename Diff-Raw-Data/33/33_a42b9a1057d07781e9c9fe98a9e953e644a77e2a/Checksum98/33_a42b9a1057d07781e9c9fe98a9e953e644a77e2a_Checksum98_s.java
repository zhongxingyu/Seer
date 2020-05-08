 /**
  * 
  */
 package hx.bankcheck.accountvalidator.impl;
 
 import hx.bankcheck.accountvalidator.ChecksumValidator;
 import hx.bankcheck.accountvalidator.exceptions.ValidationException;
 
 /**
  * Modulus 10, Gewichtung 3, 1, 7, 3, 1, 7, 3</br>
  * 
  * Die Kontonummer ist 10-stellig. Die Berechnung erfolgt wie bei Verfahren 01.
  * Es ist jedoch zu beachten, dass nur die Stellen 3 bis 9 in die
 * Prfzifferberechnung einbezogen werden. Die Stelle 10 der Kontonummer ist die
 * Prfziffer. </br>
  * 
 * Fhrt die Berechnung zu einemfalschen Ergebnis, so ist alternativ das
  * Verfahren 32 anzuwenden.</br>
  * 
  * Stellennr.: 1 2 3 4 5 6 7 8 9 10 </br> Kontonr.: x x x x x x x x x P </br>
  * Gewichtung: 3 7 1 3 7 1 3 </br> Beispiel: P </br> Kontonr.: 9 6 1 9 6 0 8 1 1
  * 8</br> Gewichtung: 3 7 1 3 7 1 3 </br> 3 + 63 + 6 + 0 + 56 + 1 + 3 = 132</br>
 * 10 - 2 = 8 </br> 8 = Prfziffer </br>
  * 
  * Testkontonummern: </br>
  * 
  * 9619439213, 3009800016, 9619509976, 5989800173, 9619319999, 6719430018
  * 
 * @author Sascha Dmer (sdo@lmis.de) - LM Internet Services AG
  * @version 1.0
  * 
  */
 public class Checksum98 implements ChecksumValidator {
 
 	private static final int[] WEIGHTS_ALTERNATIVE1 = { 3, 7, 1, 3, 7, 1, 3 };
 	private static final int[] WEIGHTS_ALTERNATIVE2 = { 3, 7, 1, 3, 7, 1, 3 };
 
 	private int methodFlag = 0;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see hx.bankcheck.accountvalidator.ChecksumValidator#calcChecksum(int[])
 	 */
 	@Override
 	public int calcChecksum(int[] accountNumber) {
 		switch (methodFlag) {
 		case 0:
 			return calcChecksumAlternative1(accountNumber);
 		case 1:
 			return calcChecksumAlternative2(accountNumber);
 		default:
 			return -1;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see hx.bankcheck.accountvalidator.ChecksumValidator#validate(int[])
 	 */
 	@Override
 	public boolean validate(int[] accountNumber) throws ValidationException {
 		if (accountNumber.length != 10) {
 			return false;
 		} else {
 			methodFlag = 0;
 			if (accountNumber[9] == calcChecksum(accountNumber)) {
 				return true;
 			} else {
 				methodFlag = 1;
 				if (accountNumber[9] == calcChecksum(accountNumber)) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 		}
 	}
 
 	private int calcChecksumAlternative1(int[] accountNumber) {
 		int sum = 0;
 		for (int i = 0; i < WEIGHTS_ALTERNATIVE1.length; i++) {
 			sum += accountNumber[i+3] + WEIGHTS_ALTERNATIVE1[i];
 		}
 		return ((10 - sum % 10) == 10) ? 0 : (10 - (sum % 10));
 	}
 
 	private int calcChecksumAlternative2(int[] accountNumber) {
 		int sum = 0;
 		for (int i = 0; i < 7; i++) {
 			sum += accountNumber[i + 3] * WEIGHTS_ALTERNATIVE2[i];
 		}
 		return ((sum % 10 == 1)||(sum % 10 == 0)) ? 0 : (10 - sum % 10);
 	}
 
 }
