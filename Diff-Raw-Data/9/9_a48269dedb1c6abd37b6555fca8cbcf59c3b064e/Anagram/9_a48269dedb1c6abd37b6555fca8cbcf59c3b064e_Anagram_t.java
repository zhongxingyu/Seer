 package com.github.kpacha.jkata.anagram;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class Anagram {
 
     public static Set<String> generate(String source) {
 	Set<String> result = new HashSet<String>();
 	List<Character> chars = getAsCharacterList(source);
	if (chars.size() == 4) {
	    for (int currentChar = 0; currentChar < 4; currentChar++) {
		Character character = chars.get(currentChar);
		for (String part : Anagram.generate(new String(getCharsToMix(
			chars, character)))) {
		    result.add(character + part);
		}
	    }
	}
 	if (chars.size() == 3) {
 	    for (int currentChar = 0; currentChar < 3; currentChar++) {
 		Character character = chars.get(currentChar);
 		for (String part : Anagram.generate(new String(getCharsToMix(
 			chars, character)))) {
 		    result.add(character + part);
 		}
 	    }
 	}
 	if (chars.size() == 2) {
 	    for (int currentChar = 0; currentChar < 2; currentChar++) {
 		Character character = chars.get(currentChar);
 		for (String part : Anagram.generate(new String(getCharsToMix(
 			chars, character)))) {
 		    result.add(character + part);
 		}
 	    }
 	}
 	if (chars.size() == 1) {
 	    result.add(source);
 	}
 	return result;
     }
 
     private static char[] getCharsToMix(List<Character> chars,
 	    Character character) {
 	List<Character> characters = new ArrayList<Character>(chars);
 	characters.remove(character);
 	char[] charArrayToMix = new char[characters.size()];
 	for (int currentChar = 0; currentChar < characters.size(); currentChar++) {
 	    charArrayToMix[currentChar] = characters.get(currentChar);
 	}
 	return charArrayToMix;
     }
 
     private static List<Character> getAsCharacterList(String source) {
 	List<Character> chars = new ArrayList<Character>();
 	for (char c : source.toCharArray()) {
 	    chars.add(c);
 	}
 	return chars;
     }
 
 }
