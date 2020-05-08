 package com.ba.languagechecker.wordchecker;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 import org.apache.log4j.Logger;
 
 public class WordSearcher {
 	private Logger _log = Logger
 			.getLogger(TextChecker.class.getCanonicalName());
 	private static final int MAX = 300000; 
	private final Set<String> dictionary  = new HashSet(MAX);
 
 	public void loadDictionaryFile(final String dictionaryFileName)
 			throws FileNotFoundException, IOException {
 		dictionary.clear();
 		final File file = new File(dictionaryFileName);
 		try (BufferedReader in = new BufferedReader(new InputStreamReader(
 				new FileInputStream(file)))) {
 			String word;
 			while ((word = in.readLine()) != null) {
				dictionary.add(word);
 			}
 		}
 		_log.info("dictionary " + dictionaryFileName + " size is " + dictionary.size()); 
 	}
 
 
 	public boolean isWordInTheDictionary(final String word) {
 		return dictionary.contains(word);
 	}
 
 }
