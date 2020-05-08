 package arithmea.shared.gematria;
 
 import java.util.HashMap;
 
 
 public class GematriaUtil {
 	boolean skipNext = false;
 	boolean skipAfterNext = false;
 	
 	public HashMap<GematriaMethod, Integer> getAllValues(final String id) {
 		HashMap<GematriaMethod, Integer> result = new HashMap<GematriaMethod, Integer>(LatinMethod.values().length + HebrewMethod.values().length);
 
 		final char[] latinChars = id.toUpperCase().toCharArray();
 		int chaldeanResult = 0;
 		int pythagoreanResult = 0;
 		int iaResult = 0;
 		int naeqResult = 0;
 		int tqResult = 0;
 		int germanResult = 0;		
 		for(int i = 0; i < latinChars.length; i++) {
 			try {
 				LatinLetter letter = LatinLetter.valueOf(String.valueOf(latinChars[i]));
 				chaldeanResult += letter.chaldeanValue;					
 				pythagoreanResult += letter.pythagoreanValue;
 				iaResult += letter.iaValue;
 				naeqResult += letter.naeqValue;
 				tqResult += letter.tqValue;
 				germanResult += letter.germanValue;
 			} catch(IllegalArgumentException iae) {
 				//ignore everything that isn't a latin letter
 			}
 		}
 		result.put(LatinMethod.Chaldean, chaldeanResult);
 		result.put(LatinMethod.Pythagorean, pythagoreanResult);
 		result.put(LatinMethod.IA, iaResult);
 		result.put(LatinMethod.NAEQ, naeqResult);
 		result.put(LatinMethod.TQ, tqResult);
 		result.put(LatinMethod.German, germanResult);
 		
 		String hebrew = getHebrew(id);
 		final char[] hebrewChars = hebrew.toCharArray();
 		int fullResult = 0;
 		int ordinalResult = 0;
 		int katanResult = 0;
 		for(int i = 0; i < hebrewChars.length; i++) {
 			for (HebrewLetter letter : HebrewLetter.values()) {
 				if (letter.hebrew == hebrewChars[i]) {
 					fullResult += letter.fullValue;
 					ordinalResult += letter.ordinalValue;
 					katanResult += letter.katanValue;
 					break;
 				}
 			}
 		}
 		result.put(HebrewMethod.Full, fullResult);
 		result.put(HebrewMethod.Ordinal, ordinalResult);
 		result.put(HebrewMethod.Katan, katanResult);
 		return result;
 	}
 
 	public String getHebrew(final String id) {
 		final char[] chars = id.toCharArray();
 		StringBuilder result = new StringBuilder();
 		for(int i = 0; i < chars.length; i++) {
 			if (skipNext) {
 				if (skipAfterNext) {
 					skipAfterNext = false;
 				} else {
 					skipNext = false;
 				}
 			} else { 
 				final char current = chars[i];
 				char next = '\u0000';
 				if (i < chars.length - 1) {
 					next = chars[i+1];
 				}
 				char afterNext = '\u0000';
 				if (i < chars.length - 2) {
 					afterNext = chars[i+2];
 				}
 
 				char resultCharacter = getHebrewCharacter(i == 0, current, next, afterNext);
 			
 				if (resultCharacter != '\u0000') {
 					result.append(resultCharacter);				
 				}
 			}
 		}
 		return result.toString();
 	}
 
 	private char getHebrewCharacter(boolean isFirst, final char current, final char next, final char afterNext) {
 		if (current == 'A') { return HebrewLetter.Aleph.hebrew; } 
 		else if (current == 'B') {return HebrewLetter.Beth.hebrew; }
 		else if (current == 'C') {
 			if (next == 'H') {
 				skipNext = true;
 				return HebrewLetter.Cheth.hebrew; 
 			}
 			if (next == 'C' || next == 'K') { //CC and CK
 				skipNext = true;
 			}
 			if (afterNext == ' ' || next == '-' || afterNext == '\u0000') {
 				return HebrewLetter.Kaph_Final.hebrew; 					
 			} else {
 				return HebrewLetter.Kaph.hebrew; 
 			}
 		}
 		else if (current == 'D') { return HebrewLetter.Daleth.hebrew; }
 		else if (current == 'E') { 
 			if (next == 'E') { //EE
 				skipNext = true;				
 				return HebrewLetter.Heh.hebrew;
 			}
 			return '\u0000'; 
 		}
 		else if (current == 'F') { return HebrewLetter.Peh.hebrew; }
 		else if (current == 'G') { return HebrewLetter.Gimel.hebrew; }
 		else if (current == 'H') { return HebrewLetter.Heh.hebrew; }
 		else if (current == 'I') { return HebrewLetter.Yud.hebrew; }
 		else if (current == 'J') { return HebrewLetter.Gimel.hebrew; }
 		else if (current == 'K') {
			if (next == 'H') {
				skipNext = true;
 				if (afterNext == ' ' || next == '-' || afterNext == '\u0000') {
 					return HebrewLetter.Kaph_Final.hebrew; 					
 				} else {
 					return HebrewLetter.Kaph.hebrew; 
 				}
 			} else if (next == ' ' || next == '-' || (next == '\u0000' && afterNext == '\u0000')) {
 				return HebrewLetter.Kaph_Final.hebrew; 
 			} else {
 				return HebrewLetter.Kaph.hebrew; 
 			}
 		}
 		else if (current == 'L') { return HebrewLetter.Lamed.hebrew; }
 		else if (current == 'M') { 
 			if (next == ' ' || next == '-' || (next == '\u0000' && afterNext == '\u0000')) {
 				return HebrewLetter.Mem_Final.hebrew; 
 			} else {
 				return HebrewLetter.Mem.hebrew; 
 			}
 		}
 		else if (current == 'N') { 
 			if (next == ' ' || next == '-' || (next == '\u0000' && afterNext == '\u0000')) {
 				return HebrewLetter.Nun_Final.hebrew; 
 			} else {
 				return HebrewLetter.Nun.hebrew; 
 			}
 		}
 		else if (current == 'O') { 
 			if (next == 'O' || next == 'U') { // double O and ou
 				skipNext = true;
 				return HebrewLetter.Ayin.hebrew; 
 			} else if (isFirst) { // O at start of word
 				return HebrewLetter.Ayin.hebrew; 				
 			} else { // other O's
 				return HebrewLetter.Vav.hebrew; 
 			}
 
 		}
 		else if (current == 'P') { 
 			if (next == 'H') {
 				skipNext = true;
 				if (afterNext == ' ' || next == '-' || afterNext == '\u0000') {
 					return HebrewLetter.Peh_Final.hebrew; 					
 				} else {
 					return HebrewLetter.Peh.hebrew; 
 				}
 			} else if (next == ' ' || next == '-' || (next == '\u0000' && afterNext == '\u0000')) {
 				return HebrewLetter.Peh_Final.hebrew; 
 			} else {
 				return HebrewLetter.Peh.hebrew; 
 			}
 		}
 		else if (current == 'Q') { return HebrewLetter.Qoph.hebrew; }
 		else if (current == 'R') { return HebrewLetter.Resh.hebrew; }
 		else if (current == 'S') { 			
 			if (next == 'C' && afterNext == 'H') { //Sch
 				skipNext = true;
 				skipAfterNext = true;
 				return HebrewLetter.Shin.hebrew; 				
 			} else if (next == 'H') { // Sh
 				skipNext = true;
 				return HebrewLetter.Shin.hebrew;  
 			} else if (next == 'S') { // SS
 				skipNext = true;
 				return HebrewLetter.Zain.hebrew;  
 			} else {
 				return HebrewLetter.Samekh.hebrew; 
 			}
 		}
 		else if (current == 'T') { 
 			if (next == 'Z' || next == 'X') { //Tz, Tx
 				skipNext = true;
 				if (afterNext == ' ' || next == '-' || afterNext == '\u0000') {
 					return HebrewLetter.Tzaddi_Final.hebrew; 
 				} else {
 					return HebrewLetter.Tzaddi.hebrew; 
 				}
 			} else if (next == 'H') { //Th
 				skipNext = true;
 				return HebrewLetter.Tav.hebrew; 
 			} else if (next == 'S') { //Ts
 				skipNext = true;
 				return HebrewLetter.Zain.hebrew; 
 			} else {
 				return HebrewLetter.Teth.hebrew; 	
 			}
 		}
 		else if (current == 'U') { return HebrewLetter.Vav.hebrew; }
 		else if (current == 'V') { return HebrewLetter.Vav.hebrew; }
 		else if (current == 'W') { return HebrewLetter.Vav.hebrew; }
 		else if (current == 'X') { 
 			if (next == ' ' || next == '-' || (next == '\u0000' && afterNext == '\u0000')) {
 				return HebrewLetter.Tzaddi_Final.hebrew; 
 			} else {
 				return HebrewLetter.Tzaddi.hebrew; 
 			}
 		}
 		else if (current == 'Y') { return HebrewLetter.Yud.hebrew; }
 		else if (current == 'Z') { return HebrewLetter.Zain.hebrew; }
 		
 		return current;
 	}
 }
