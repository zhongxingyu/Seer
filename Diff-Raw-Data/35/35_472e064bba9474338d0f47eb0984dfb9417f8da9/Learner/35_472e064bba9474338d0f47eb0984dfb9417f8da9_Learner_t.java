 package semanticMarkup.ling.learn;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 
 
 import semanticMarkup.core.Treatment;
 import semanticMarkup.knowledge.lib.WordNetAPI;
 
 public class Learner {	
 	/**************************************************
 	 * Debug variables used by developer
 	 */
 	private boolean populateSentence_debug = false;
 	private boolean addHeuristicsNouns_debug = false;
 	private boolean getHeuristicNouns_debug = false;
 	private boolean characterHeuristics_debug = false;
 	/**************************************************
 	 */
 
 	private Configuration myConfiguration;
 	private Utility myUtility;
 	
 	// Data holder
 	private DataHolder myDataHolder;
 	
 	// Utilities
 	private WordFormUtility myWordFormUtility;
 	private PopulateSentenceUtility myPopulateSentenceUtility;
 	
 	// Class variables
 	private int NUM_LEAD_WORDS; // Number of leading words
 	
 	// others
 	
 	// leading three words of sentences 
 	private Set<String> checkedWordSet;
 	
 	public Learner(Configuration configuration, Utility utility) {
 		this.myConfiguration = configuration;
 		this.myUtility = utility;
 		
 		// Data holder
 		myDataHolder = new DataHolder(myConfiguration, myUtility);
 		
 		// Utilities
 		this.myWordFormUtility = new WordFormUtility(this.myUtility.getWordNet());
 		this.myPopulateSentenceUtility = new PopulateSentenceUtility(this.myUtility.getSentenceDetector(), this.myUtility.getTokenizer());
 		
 		// Class variables
 		NUM_LEAD_WORDS = 3; // Set the number of leading words be 3
 		
 		checkedWordSet = new HashSet<String>();
 		
 	}
 
 	public DataHolder Learn(List<Treatment> treatments) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("Learn");
 		myLogger.trace("Enter Learn");
 		myLogger.info(String.format("Learning Mode: %s", this.myConfiguration.getLearningMode()));
 
 		this.populateSentence(treatments);
 		this.populateUnknownWordsTable(this.myDataHolder.allWords);
 
 		/*
 		Map<String, String> mygetHeuristicNounHolder() = myDataHolder.getgetHeuristicNounHolder()();
 		myHeuristicNounTable.put("word1", "type1");
 		
 		List<Sentence> mygetSentenceHolder() = myDataHolder.getgetSentenceHolder()();
 		mygetSentenceHolder().add(new Sentence("source1", "sentence1", "originalSentence", "lead1", "status1", "tag1", "modifier1", "type1"));
 		*/
 		
 		// List<String> fileNameList = fileLoader.getFileNameList();
 		// List<Integer> typeList = fileLoader.getTypeList();
 
 		// List<String> textList = fileLoader.getTextList();
 
 		// process treatments
 		//this.populateSentences(treatments);
 		
 
 		// pre load words
 		this.addHeuristicsNouns();
 		this.addPredefinedWords();
 
 
 		// ???
 		this.posBySuffix();
 		this.markupByPattern();
 		this.markupIgnore();
 
 		// learning rules with high certainty
 		// At the every beginning, only those sentence whose first word is a p,
 		// could have a tag of "start", see populateSentece - getFirstNWords section -Dongye
 		myLogger.info("Learning rules with high certainty:");
 		this.discover("start");
 		
 		// bootstrapping rules
 //		myLogger.info("Bootstrapping rules");
 //		this.discover("normal");
 
 		//System.out.println("Method: learn - Done!\n");
 
 		myLogger.trace("Quite Learn");
 		return myDataHolder;
 	}
 	
 	private void addPredefinedWords() {
 		this.addStopWords();
 		this.addCharacters();
 		this.addNumbers();
 		this.addClusterstrings();
 		this.addProperNouns();		
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public DataHolder getDataHolder(){
 		return this.myDataHolder;
 	}
 
 	/**
 	 * 
 	 * @param treatments
 	 * @return number of sentences
 	 */
 	public int populateSentence(List<Treatment> treatments) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.populateSentence");
 		myLogger.info("Reading sentences...");
 
 		String fileName;
 		int type;
 		String text;
 		int SENTID = 0;
 
 		for (int i = 0; i < treatments.size(); i++) {
 			Treatment tm = treatments.get(i);
 			fileName = tm.getFileName();
 			text = tm.getDescription();
 			type = this.myPopulateSentenceUtility.getType(fileName);
 
 			if (text != null) {
 				// process this text
 				text = this.handleText(text);
 				myLogger.debug("Text: " + text);
 
 				// use Apache OpenNLP to do sentence segmentation
 				String sentences[] = {};
 				sentences = this.myPopulateSentenceUtility.segmentSentence(text);
 
 				List<String> sentCopy = new LinkedList<String>();
 				List<Integer> validIndex = new LinkedList<Integer>();
 				
 				// for each sentence, do some operations
 				for (int j = 0; j < sentences.length; j++) {
 					myLogger.debug("Sentence " + j + ": " + sentences[j]);
 					
 					// if(!/\w+/){next;}
 					if (!sentences[j].matches("^.*\\w+.*$")) {
 						continue;
 					}
 
 					// This is a valid sentence, save the index
 					validIndex.add(j);
 
 					// restore marks in brackets
 					sentences[j] = this.myPopulateSentenceUtility.restoreMarksInBrackets(sentences[j]);
 					// Make a copy of the sentence
 					sentCopy.add(sentences[j]);
 
 					// process the sentence
 					sentences[j] = this.handleSentence(sentences[j]);
 
 					// store all words
 					this.myDataHolder.allWords = this.myPopulateSentenceUtility.getAllWords(sentences[j], this.myDataHolder.allWords);
 				}
 
 				for (int j = 0; j < validIndex.size(); j++) {
 					String line = sentences[validIndex.get(j)];
 					String oline = sentCopy.get(j);
 
 					// handle line first
 					// remove all ' to avoid escape problems
 					// $line =~ s#'# #g;
 					line.replaceAll("\'", " ");
 
 					// then handle oline
 					Matcher matcher = Pattern.compile(
 							"(\\d)\\s*\\[\\s*DOT\\s*\\]\\s*(\\d)").matcher(
 							oline);
 					if (matcher.lookingAt()) {
 						oline = oline.replaceAll(
 								"(\\d)\\s*\\[\\s*DOT\\s*\\]\\s*(\\d)",
 								matcher.group(1) + matcher.group(2));
 					}
 
 					// restore ".", "?", ";", ":", "."
 					oline = this.myPopulateSentenceUtility.restoreMarksInBrackets(oline);
 					oline = oline.replaceAll("\'", " ");
 
 					List<String> nWords = this.myPopulateSentenceUtility.getFirstNWords(line,
 							this.NUM_LEAD_WORDS);
 					String lead = "";
 					Iterator<String> iter = nWords.iterator();
 					while (iter.hasNext()) {
 						String w = iter.next();
 						lead = lead + w + " ";
 					}
 					lead = lead.replaceAll("\\s$", "");
 
 					String status = "";
 					if (myWordFormUtility.getNumber(nWords.get(0)).equals("p")) {
 						status = "start";
 					} else {
 						status = "normal";
 					}
 
 					lead = StringUtility.removeAll(lead, "\\s+$");
 					lead = StringUtility.removeAll(lead, "^\\s*");
 					lead = lead.replaceAll("\\s+", " ");
 
 					String source = fileName + "-" + Integer.toString(j);
 					if (oline.length() >= 2000) { // EOL
 						oline = line;
 					}
 					String typeStr = null;
 					switch (type) {
 					case 1:
 						typeStr = "character";
 						break;
 					case 2:
 						typeStr = "description";
 						break;
 					}
 
 					Sentence newSent = new Sentence(source, line, oline, lead,
 							status, null, null, typeStr);
 					this.myDataHolder.getSentenceHolder().add(newSent);
 
 					SENTID++;
 				}
 			}
 		}
 
 		myLogger.info("Total sentences = " + SENTID);
 
 		return SENTID;
 	}
 	
 	/**
 	 * A helper of method pupulateSentence to handle text process
 	 * 
 	 * @param t
 	 * @return text after process
 	 */
 	public String handleText(String t) {
 
 		if (t == null || t == "") {
 			return t;
 		}
 
 		String text = t;
 
 		//
 		text = text.replaceAll("[\"']", "");
 
 		// plano - to
 		text = text.replaceAll("\\s*-\\s*to\\s+", " to ");
 
 		//
 		text = text.replaceAll("[-_]+shaped", "-shaped");
 
 		// unhide <i>
 		text = text.replaceAll("&lt;i&gt;", "<i>");
 
 		// unhide </i>, these will be used by characterHeuristics to
 		// collect taxon names
 		text = text.replaceAll("&lt;/i&gt;", "</i>");
 
 		// remove 2a. (key marks)
 		text = text.replaceAll("^\\s*\\d+[a-z].\\s*", "");
 
 		// this is not used any more, see perl code - Dongye
 		// store text at this point in original
 		// String original = text;
 
 		// remove HTML entities
 		text = text.replaceAll("&[;#\\w\\d]+;", " ");
 
 		//
 		text = text.replaceAll(" & ", " and ");
 
 		// replace '.', '?', ';', ':', '!' within brackets by some
 		// special markers, to avoid split within brackets during
 		// sentence segmentation
 		// System.out.println("Before Hide: "+text);
 		
 		text = this.myPopulateSentenceUtility.hideMarksInBrackets(text);
 		// System.out.println("After Hide: "+text+"\n");
 
 		text = text.replaceAll("_", "-"); // _ to -
 		text = text.replaceAll("", ""); //
 
 		// absent ; => absent;
 		while (true) {
 			Matcher matcher1 = Pattern.compile("(^.*?)\\s+([:;\\.].*$)")
 					.matcher(text);
 			if (matcher1.lookingAt()) {
 				text = matcher1.group(1) + matcher1.group(2);
 			} else {
 				break;
 			}
 		}
 
 		// absent;blade => absent; blade
 		while (true) {
 			Matcher matcher2 = Pattern.compile("(^.*?\\w)([:;\\.])(\\w.*$)")
 					.matcher(text);
 			if (matcher2.lookingAt()) {
 				// text = text.replaceAll("^.*\\w[:;\\.]\\w.*",
 				// matcher2.group(1)
 				// + matcher2.group(2) + " " + matcher2.group(3));
 				text = matcher2.group(1) + matcher2.group(2) + " "
 						+ matcher2.group(3);
 			} else {
 				break;
 			}
 		}
 
 		// 1 . 5 => 1.5
 		while (true) {
 			Matcher matcher3 = Pattern.compile("(^.*?\\d\\s*\\.)\\s+(\\d.*$)")
 					.matcher(text);
 			if (matcher3.lookingAt()) {
 				text = matcher3.group(1) + matcher3.group(2);
 			} else {
 				break;
 			}
 		}
 
 		// ###NOT necessary at all, done before in "absent ; => absent;"###
 		// diam . =>diam.
 		// Matcher matcher4 =
 		// Pattern.compile("(\\sdiam)\\s+(\\.)").matcher(text);
 		// if (matcher4.lookingAt()) {
 		// text = text.replaceAll("\\sdiam\\s+\\.", matcher4.group(1)
 		// + matcher4.group(2));
 		// }
 
 		// ca . =>ca.
 		// Matcher matcher5 = Pattern.compile("(\\sca)\\s+(\\.)").matcher(text);
 		// if (matcher5.lookingAt()) {
 		// text = text.replaceAll("\\sca\\s+\\.",
 		// matcher5.group(1) + matcher5.group(2));
 		// }
 
 		//
 		while (true) {
 			Matcher matcher6 = Pattern.compile(
 					"(^.*\\d\\s+(cm|mm|dm|m)\\s*)\\.(\\s+[^A-Z].*$)").matcher(
 					text);
 			if (matcher6.lookingAt()) {
 				text = matcher6.group(1) + "[DOT]" + matcher6.group(3);
 			} else {
 				break;
 			}
 		}
 
 		return text;
 	}
 
 	/**
 	 * remove bracketed text from sentence (keep those in originalsent). Tthis
 	 * step will not be able to remove nested brackets, such as (petioles
 	 * (2-)4-8 cm). Nested brackets will be removed after threedsent step in
 	 * POSTagger4StanfordParser.java
 	 * 
 	 * @param s
 	 *            sentence to be handled
 	 * @return sentence after being processed
 	 */
 	public String handleSentence(String s) {
 		if (s == null || s == "") {
 			return s;
 		}
 
 		String sentence = s;
 
 		// remove (.a.)
 		sentence = sentence.replaceAll("\\([^()]*?[a-zA-Z][^()]*?\\)", " ");
 
 		// remove [.a.]
 		sentence = sentence.replaceAll("\\[[^\\]\\[]*?[a-zA-Z][^\\]\\[]*?\\]",
 				" ");
 
 		// remove {.a.}
 		sentence = sentence.replaceAll("\\{[^{}]*?[a-zA-Z][^{}]*?\\}", " ");
 
 		// to fix basi- and hypobranchial
 		while (true) {
 			Matcher matcher = Pattern.compile("(^.*?)\\s*[-]+\\s*([a-z].*$)")
 					.matcher(sentence);
 			if (matcher.lookingAt()) {
 				sentence = matcher.group(1) + "_ " + matcher.group(2);
 			} else {
 				break;
 			}
 		}
 
 		// add space around nonword char
 		sentence = this.myPopulateSentenceUtility.addSpace(sentence, "\\W");
 
 		// multiple spaces => 1 space
 		sentence = sentence.replaceAll("\\s+", " ");
 
 		// trim: remove leading and ending spaces
 		sentence = sentence.replaceAll("^\\s*", "");
 		sentence = sentence.replaceAll("\\s*$", "");
 
 		// all to lower case
 		sentence = sentence.toLowerCase();
 
 		return sentence;
 	}
 	
 	/**
 	 * Insert all words in WORDS into getUnknownWordHolder(). Insert those formed by
 	 * non words characters into getWordPOSHolder()
 	 * 
 	 * @param WORDS
 	 * @return
 	 */
 	public int populateUnknownWordsTable(Map<String, Integer> WORDS) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.populateSentence");
 
 		int count = 0;
 
 		Iterator<String> iter = WORDS.keySet().iterator();
 
 		while (iter.hasNext()) {
 			String word = iter.next();
 			if ((!word.matches("^.*\\w.*$")) || (word.matches("^.*ous$"))) {
 				this.myDataHolder.addUnknown(word, word);
 				this.myDataHolder.updateTable(word, "b", "", "wordpos", 1);
 			} else {
 				this.myDataHolder.addUnknown(word, "unknown");
 			}
 			count++;
 		}
 
 		myLogger.info("Total words = " + count);
 		
 		return count;
 	}
 	
 	/**
 	 * 
 	 */
 	public void addHeuristicsNouns() {
 		if (this.addHeuristicsNouns_debug)
 			System.out.println("Enter addHeuristicsNouns:\n");
 
 		// part 1
 		List<Set<String>> results = this.characterHeuristics();
 		Set<String> rnouns = results.get(0);
 		Set<String> descriptors = results.get(1);
 		
 		addDescriptors(descriptors);
 		addNouns(rnouns);
 		
 		// part 2
 		Set<String> nouns = this.learnHeuristicsNouns();
 		Iterator<String> iter = nouns.iterator();
 		while (iter.hasNext()) {
 			String e = iter.next();
 			if ((e.matches("^.*\\w.*$"))
 					&& (!StringUtility.isMatchedWords(e, "NUM|" + Constant.NUMBER
 							+ "|" + Constant.CLUSTERSTRING + "|"
 							+ Constant.CHARACTER + "|" + Constant.PROPERNOUN))) {
 				// same word may have two different pos tags
 				String[] nounArray = e.split("\\|");
 				for (int i = 0; i < nounArray.length; i++) {
 					String nounAndPOS = nounArray[i];
 					Pattern p = Pattern.compile("(\\w+)\\[([spn])\\]");
 					Matcher m = p.matcher(nounAndPOS);
 					if (m.lookingAt()) {
 						String word = m.group(1);
 						String pos = m.group(2);
 						this.myDataHolder.updateTable(word, pos, "*", "wordpos", 0);
 
 						if (pos.equals("p")) {
 							String plural = word;
 							String singular = this.myWordFormUtility
 									.getSingular(plural);
 							if (singular != null) {
 								if (!singular.equals("")) {
 									this.myDataHolder.addSingularPluralPair(singular, plural);
 								}
 							}
 						}
 
 						if (pos.equals("s")) {
 							String singular = word;
 							List<String> pluralList = this.myWordFormUtility
 									.getPlural(singular);
 							iter = pluralList.iterator();
 							while (iter.hasNext()) {
 								String plural = iter.next();
 								if (plural != null) {
 									if (!plural.equals("")) {
 										this.myDataHolder.addSingularPluralPair(singular, plural);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 
 	
 	/**
 	 * 
 	 * @param descriptors
 	 */
 	public void addDescriptors(Set<String> descriptors) {
 		Iterator<String> iter = descriptors.iterator();
 		while (iter.hasNext()) {
 			String descriptor = iter.next();
 			
 			if (!StringUtility.isMatchedWords(descriptor, Constant.FORBIDDEN)) {
 				this.myDataHolder.updateTable(descriptor, "b", "", "wordpos", 1);
 			}
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @param rnouns
 	 */
 	public void addNouns(Set<String> rnouns) {
 		// TODO Auto-generated method stub
 		Iterator<String> iter = rnouns.iterator();
 		while (iter.hasNext()) {
 			String noun = iter.next();
 			if (!StringUtility.isMatchedWords(noun, Constant.FORBIDDEN)) {
 				this.myDataHolder.updateTable(noun, "n", "", "wordpos", 1);
 			}
 		}
 	}
 
 
 
 	/**
 	 * 
 	 * @return nouns learned by heuristics
 	 */
 	public Set<String> learnHeuristicsNouns() {
 		// Set of words
 		Set<String> words = new HashSet<String>();
 
 		// Set of nouns
 		Set<String> nouns = new HashSet<String>();
 
 		List<String> sentences = new LinkedList<String>();
 		for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 			String originalSentence = this.myDataHolder.getSentenceHolder().get(i)
 					.getOriginalSentence();
 			if (this.getHeuristicNouns_debug) {
 				 System.out.println(originalSentence+"\n");
 			}
 			sentences.add(StringUtility.strip(originalSentence));
 		}
 
 		// Now we have original sentences in sentences
 		// Method addWords
 		for (int i = 0; i < sentences.size(); i++) {
 			String sentence = sentences.get(i);
 			sentence = sentence.toLowerCase();
 			String noun = this.getPresentAbsentNouns(sentence);
 			if (!noun.equals("")) {
 				nouns.add(noun);
 			}
 
 			// add words
 			String[] tokens = this.myUtility.getTokenizer().tokenize(sentence);
 			for (int j = 0; j < tokens.length; j++) {
 				String token = tokens[j];
 				if (StringUtility.isWord(token)) {
 					// if (token.equals("arch"))
 					// token="arch";
 					words.add(token);
 					if (this.getHeuristicNouns_debug) {
 						System.out.println("Add a word into words:");
 						System.out.println(token);
 					}
 				}
 			}
 		}
 
 		// solve the problem: septa and septum are both s
 		Iterator<String> nounsIterator = nouns.iterator();
 		while (nounsIterator.hasNext()) {
 			String oldNoun = nounsIterator.next();
 			String newNoun = this.getHeuristicsNounsHelper(oldNoun, nouns);
 			if (!newNoun.equals(oldNoun)) {
 				nouns.remove(oldNoun);
 				nouns.add(newNoun);
 			}
 		}
 
 		// sort all words
 		Map<String, Set<String>> wordMap = new HashMap<String, Set<String>>();
 		Iterator<String> wordsIterator = words.iterator();
 		while (wordsIterator.hasNext()) {
 			String word = wordsIterator.next();
 			String root = myWordFormUtility.getRoot(word);
 			if (wordMap.containsKey(root)) {
 				Set<String> wordList = wordMap.get(root);
 				wordList.add(word);
 				//List<String> wordList2 = wordMap.get(root);
 				//System.out.println(wordList2);
 			} else {
 				Set<String> wordList = new HashSet<String>();
 				wordList.add(word);
 				wordMap.put(root, wordList);
 			}
 		}
 
 		// print out the wordMap
 		if (getHeuristicNouns_debug) {
 			Iterator<Map.Entry<String, Set<String>>> iter = wordMap.entrySet().iterator();
 			while (iter.hasNext()){
 				Map.Entry<String, Set<String>> e = iter.next();
 				System.out.println(e.getKey());
 				System.out.println(e.getValue());
 			}
 		}
 		
 		// find nouns
 		Iterator<Map.Entry<String, Set<String>>> iter = wordMap.entrySet().iterator();
 		while (iter.hasNext()){
 			Map.Entry<String, Set<String>> e = iter.next();
 			Set<String> wordSet = e.getValue();
 			Iterator<String> wordIterator = wordSet.iterator();
 			while(wordIterator.hasNext()){
 				String word = wordIterator.next();
 				
 				// getnouns
 				if (word.matches("^.*" + Constant.NENDINGS)) {
 					nouns.add(word + "[s]");
 					if (wordSet.contains(word + "s")) {
 						nouns.add(word + "s" + "[p]");
 						this.myDataHolder.addSingularPluralPair(word, word+"s");						
 					}
 					if (wordSet.contains(word + "es")) {
 						nouns.add(word + "es" + "[p]");
 						this.myDataHolder.addSingularPluralPair(word, word+"es");
 					}
 				}
 			}
 		}
 
 		// Iterator<LinkedList> wordMapIterator = wordMap.i
 		Iterator<Map.Entry<String, Set<String>>> wordMapIterator = wordMap
 				.entrySet().iterator();
 		while (wordMapIterator.hasNext()) {
 			Map.Entry<String, Set<String>> wordMapEntry = wordMapIterator
 					.next();
 			Set<String> wordSet = wordMapEntry.getValue();
 
 			// check if there is a word with Vending
 			boolean hasVending = false;
 			//for (int i1 = 0; i1 < wordList.size(); i1++) {
 			Iterator<String> wordIterator = wordSet.iterator();
 			while(wordIterator.hasNext()){
 				String tempWord =wordIterator.next(); 
 				if (tempWord.matches("^.*" + Constant.VENDINGS)) {
 					hasVending = true;
 					break;
 				}
 			}
 
 			// at least two words without verb endings
 			if ((!hasVending) && (wordSet.size() > 1)) {
 				List<String> wordList = new LinkedList<String>(wordSet);
 				for (int i = 0; i < wordList.size(); i++) {
 					for (int j = i + 1; j < wordList.size(); j++) {
 						String word1 = wordList.get(i);
 						String word2 = wordList.get(j);
 						List<String> pair = myWordFormUtility.getSingularPluralPair(word1, word2);
 						if (pair.size() == 2) {
 							String singular = pair.get(0);
 							String plural = pair.get(1);
 							nouns.add(singular + "[s]");
 							nouns.add(plural + "[p]");
 							this.myDataHolder.addSingularPluralPair(singular, plural);
 						}
 					}
 				}
 			}
 		}
 		
 		//print out nouns
 		if (this.getHeuristicNouns_debug) {
 			System.out.println("Nouns:\n");
 			System.out.println(nouns);
 		}
 				
 		return nouns;
 	}
 	
 	// ---------------addHeuristicsNouns Help Function----
 	// #solve the problem: septa and septum are both s
 	// septum - Singular
 	// septa -Plural
 	// septa[s] => septa[p]
 	public String getHeuristicsNounsHelper(String oldNoun, Set<String> words) {
 		String newNoun = oldNoun;
 
 		if (oldNoun.matches("^.*a\\[s\\]$")) {
 			String noun = oldNoun.replaceAll("\\[s\\]", "");
 			if (words.contains(noun)) {
 				newNoun = noun + "[p]";
 			}
 		}
 
 		return newNoun;
 	}
 
 	/**
 	 * any word preceeding "present"/"absent" would be a n
 	 * 
 	 * @param text
 	 *            the content to learn from
 	 * @return nouns learned
 	 */
 	public String getPresentAbsentNouns(String text) {
 		String pachecked = "and|or|to";
 
 		if (text.matches("(\\w+?)\\s+(present|absent)")) {
 			System.out.println(text);
 		}
 
 		Matcher matcher = Pattern.compile("^.*?(\\w+?)\\s+(present|absent).*$")
 				.matcher(text);
 		if (matcher.lookingAt()) {
 			String word = matcher.group(1);
 			if ((!word.matches("\\b(" + pachecked + ")\\b"))
 					&& (!word.matches("\\b(" + Constant.STOP + ")\\b"))
 					&& (!word
 							.matches("\\b(always|often|seldom|sometimes|[a-z]+ly)\\b"))) {
 				if (this.getHeuristicNouns_debug)
 					System.out.println("present/absent " + word + "\n");
 
 				if (((word.matches("^.*" + Constant.PENDINGS))
 						|| (word.matches("^.*[^s]s$")) || (word
 							.matches("teeth")))
 						&& (!word.matches(Constant.SENDINGS))) {
 					return word + "[p]";
 				} else {
 					return word + "[s]";
 				}
 			}
 		}
 
 		return "";
 	}
 
 	/**
 	 * Discover nouns and descriptors according to a set of rules
 	 * 
 	 * @return a linked list, whose first element is a set of nouns, and second
 	 *         element is a set of descriptors
 	 */
 	public List<Set<String>> characterHeuristics() {
 		
 		Set<String> taxonNames = new HashSet<String>();
 		Set<String> nouns = new HashSet<String>();
 		Set<String> anouns = new HashSet<String>();
 		Set<String> pnouns = new HashSet<String>();
 		Set<String> descriptors = new HashSet<String>();
 		Map<String, Boolean> descriptorMap = new HashMap<String, Boolean>();
 
 		int sent_num = this.myDataHolder.getSentenceHolder().size();
 		for (int i = 0; i < sent_num; i++) {
 
 			// taxon rule
 			Sentence sent = this.myDataHolder.getSentenceHolder().get(i);
 			String source = sent.getSource();
 			String sentence = sent.getSentence();
 			String originalSentence = sent.getOriginalSentence();
 
 			if (this.characterHeuristics_debug) {
 				System.out.println(source);
 				System.out.println(sentence);
 				System.out.println(originalSentence + "\n");
 			}
 
 			originalSentence = StringUtility.trimString(originalSentence);
 
 			// noun rule 0: taxon names
 			taxonNames = this.getTaxonNameNouns(originalSentence);
 			
 			//$sentence =~ s#<\s*/?\s*i\s*>##g;
 			//$originalsent =~ s#<\s*/?\s*i\s*>##g;
 			
 			sentence = sentence.replaceAll("<\\s*/?\\s*i\\s*>", "");
 			originalSentence = originalSentence.replaceAll("<\\s*/?\\s*i\\s*>",
 					"");
 			// Update getSentenceHolder()
 			this.myDataHolder.getSentenceHolder().get(i).setSentence(sentence);
 
 			// noun rule 0.5: Meckle#s cartilage
 
 			Set<String> nouns0 = this
 					.getNounsMecklesCartilage(originalSentence);
 			nouns.addAll(nouns0);
 			sentence = sentence.replaceAll("#", "");
 			// Update getSentenceHolder()
 			this.myDataHolder.getSentenceHolder().get(i).setSentence(sentence);
 
 			// noun rule 2: end of sentence nouns
 			// (a|an|the|some|any|this|that|those|these) noun$
 			Set<String> nouns2 = this.getNounsRule2(originalSentence);
 			nouns.addAll(nouns2);
 
 			// noun rule 3: proper nouns and acronyms
 			String copy = originalSentence;
 			Set<String> nouns_temp = this.getNounsRule3Helper(copy);
 			Iterator<String> iter = nouns_temp.iterator();
 			while (iter.hasNext()) {
 				String token = iter.next();
 				if (token.matches("^.*[A-Z].+$")
 						&& (!token.matches("^.*-\\w+ed$"))) {
 					if (token.matches("^[A-Z0-9]+$")) {
 						token = token.toLowerCase();
 						anouns.add(token);
 					} else {
 						token = token.toLowerCase();
 						pnouns.add(token);
 					}
 					nouns.add(token);
 				}
 			}
 
 			// noun rule 1: sources with 1 _ are character statements, 2 _ are
 			// descriptions
 			Set<String> nouns1 = getNounsRule1(source, originalSentence,
 					descriptorMap);
 			nouns.addAll(nouns1);
 
 			// noun rule 4: non-stop/prep followed by a number: epibranchial 4
 			// descriptor heuristics
 			Set<String> nouns4 = this.getNounsRule4(originalSentence);
 			nouns.addAll(nouns4);
 
 			// remove puncts for descriptor rules
 			originalSentence = StringUtility.removePunctuation(
 					originalSentence, "-");
 //			System.out.println("oSent:");
 //			System.out.println(originalSentence);
 			
 			// Descriptor rule 1: single term descriptions are descriptors
 			descriptors.addAll(this.getDescriptorsRule1(source,
 					originalSentence, nouns));
 
 			// Descriptor rule 2: (is|are) red: isDescriptor
 			descriptors.addAll(this.getDescriptorsRule2(originalSentence,
 					descriptorMap));
 		}
 
 		nouns = this.filterOutDescriptors(nouns, descriptors);
 		anouns = this.filterOutDescriptors(anouns, descriptors);
 		pnouns = this.filterOutDescriptors(pnouns, descriptors);
 
 		this.add2HeuristicNounTable(nouns, "organ");
 		this.add2HeuristicNounTable(anouns, "acronyms");
 		this.add2HeuristicNounTable(pnouns, "propernouns");
 		this.add2HeuristicNounTable(taxonNames, "taxonnames");
 
 		nouns.addAll(anouns);
 		nouns.addAll(pnouns);
 		nouns.addAll(taxonNames);
 
 		List<Set<String>> results = new LinkedList<Set<String>>();
 		results.add(nouns);
 		results.add(descriptors);
 
 		return results;
 	}
 
 	/**
 	 * Add the terms into the heuristicNounTable with the type specified
 	 * 
 	 * @param terms
 	 *            set of terms
 	 * @param type
 	 *            type of the terms
 	 */
 	public int add2HeuristicNounTable(Set<String> terms, String type) {
 		int count = 0;
 
 		Iterator<String> iter = terms.iterator();
 		while (iter.hasNext()) {
 			String term = iter.next();
 			this.myDataHolder.getHeuristicNounHolder().put(term, type);
 			count++;
 		}
 
 		return count;
 	}
 
 	/**
 	 * filter out descriptors from nouns, and return remaining nouns
 	 * 
 	 * @param rNouns
 	 *            set of nouns
 	 * @param rDescriptors
 	 *            set of descriptors
 	 * @return set of nouns that are not descriptors
 	 */
 	Set<String> filterOutDescriptors(Set<String> rNouns,
 			Set<String> rDescriptors) {
 		Set<String> filtedNouns = new HashSet<String>();
 
 		Iterator<String> iter = rNouns.iterator();
 		while (iter.hasNext()) {
 			String noun = iter.next();
 			noun = noun.toLowerCase();
 
 			Pattern p = Pattern.compile("\\b(" + Constant.PREPOSITION + "|"
 					+ Constant.STOP + ")\\b", Pattern.CASE_INSENSITIVE);
 			Matcher m = p.matcher(noun);
 
 			if ((!m.lookingAt()) && (!rDescriptors.contains(noun))) {
 				filtedNouns.add(noun);
 			}
 		}
 		return filtedNouns;
 	}
 
 	/**
 	 * Nouns rule 0: get <i></i> enclosed taxon names
 	 * 
 	 * @param oSent
 	 * @return
 	 */
 	public Set<String> getTaxonNameNouns(String oSent) {
 		Set<String> taxonNames = new HashSet<String>();
 		String regex = "(.*?)<\\s*i\\s*>\\s*([^<]*)\\s*<\\s*\\/\\s*i\\s*>(.*)";
 		String copy = oSent;
 
 		while (true) {
 			Matcher matcher = Pattern.compile(regex).matcher(copy);
 			if (matcher.lookingAt()) {
 				String taxonName = matcher.group(2);
 				if (taxonName.length() > 0) {
 					taxonNames.add(taxonName);
 					String[] taxonNameArray = taxonName.split("\\s+");
 					for (int i = 0; i < taxonNameArray.length; i++) {
 						taxonNames.add(taxonNameArray[i]);
 					}
 					copy = matcher.group(3);
 				} else {
 					break;
 				}
 			} else {
 				break;
 			}
 		}
 
 		return taxonNames;
 	}
 
 	/**
 	 * Nouns rule 0.5: Meckle#s cartilage
 	 * 
 	 * @param oSent
 	 * @return
 	 */
 	public Set<String> getNounsMecklesCartilage(String oSent) {
 		Set<String> nouns = new HashSet<String>();
 		String regex = "^.*\\b(\\w+#s)\\b.*$";
 		Matcher m = Pattern.compile(regex).matcher(oSent);
 		if (m.lookingAt()) {
 			String noun = "";
 			noun = m.group(1);
 
 			noun = noun.toLowerCase();
 			nouns.add(noun);
 
 			noun = noun.replaceAll("#", "");
 			nouns.add(noun);
 
 			noun = noun.replaceAll("s$", "");
 			nouns.add(noun);
 		}
 
 		return nouns;
 	}
 
 	/**
 	 * 
 	 * @param source
 	 * @param originalSentence
 	 * @param descriptorMap
 	 * @return
 	 */
 	public Set<String> getNounsRule1(String source, String originalSentence,
 			Map<String, Boolean> descriptorMap) {
 		Set<String> nouns = new HashSet<String>();
 
 		if ((!(source.matches("^.*\\.xml_\\S+_.*$")))
 				&& (!(originalSentence.matches("^.*\\s.*$")))) {
 			if (!this.isDescriptor(originalSentence, descriptorMap)) {
 				originalSentence = originalSentence.toLowerCase();
 				nouns.add(originalSentence);
 			}
 		}
 
 		return nouns;
 	}
 
 	/**
 	 * 
 	 * @param oSent
 	 * @return
 	 */
 	public Set<String> getNounsRule2(String oSent) {
 		String copy = oSent;
 		String regex = "(.*?)\\b(a|an|the|some|any|this|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth) +(\\w+)\\s*($|\\(|\\[|\\{|\\b"
 				+ Constant.PREPOSITION + "\\b)(.*)";
 		Set<String> nouns = new HashSet<String>();
 
 		while (true) {
 			if (copy == null) {
 				break;
 			}
 			Matcher m = Pattern.compile(regex).matcher(copy);
 			if (m.lookingAt()) {
 				String t = m.group(3);
 				String prep = m.group(4);
 				copy = m.group(5);
 
 				if (prep.matches("^.*\\w.*$")
 						&& t.matches("^.*\\b(length|width|presence|\\w+tion)\\b.*$")) {
 					continue;
 				}
 				t = t.toLowerCase();
 				nouns.add(t);
 			} else {
 				break;
 			}
 		}
 
 		return nouns;
 	}
 
 	/**
 	 * 
 	 * @param sentence
 	 * @return
 	 */
 	public Set<String> getNounsRule3Helper(String sentence) {
 		Set<String> nouns = new HashSet<String>();
 
 		String[] segs = sentence.split("[()\\[\\]\\{\\}]");
 		for (int i1 = 0; i1 < segs.length; i1++) {
 			String seg = segs[i1];
 			seg = StringUtility.removePunctuation(seg, "-");
 			String[] tokens = seg.split("\\s+");
 
 			// #ignore the first word in character statements--this is normally
 			// capitalized
 			for (int j = 1; j < tokens.length; j++) {
 				String token = tokens[j];
 				if (token.matches("^.*[A-Z].+$")
 						&& (!token.matches("^.*-\\w+ed$"))) {
 					nouns.add(token);
 				}
 			}
 		}
 
 		return nouns;
 	}
 
 	/**
 	 * noun rule 4: non-stop/prep followed by a number: epibranchial 4
 	 * descriptor heuristics
 	 * 
 	 * @param oSent
 	 * @return a set of nouns
 	 */
 	public Set<String> getNounsRule4(String oSent) {
 		Set<String> nouns = new HashSet<String>();
 
 		String copy = oSent;
 		String regex = "(.*?)\\s(\\w+)\\s+\\d+(.*)";
 
 		while (true) {
 			if (copy == null) {
 				break;
 			}
 			Matcher m = Pattern.compile(regex).matcher(copy);
 			if (m.lookingAt()) {
 				String t = m.group(2);
 				copy = m.group(3);
 				String regex2 = "\\b(" + Constant.PREPOSITION + "|"
 						+ Constant.STOP + ")\\b";
 				if (!t.matches(regex2)) {
 					t = t.toLowerCase();
 					nouns.add(t);
 				}
 			} else {
 				break;
 			}
 		}
 
 		return nouns;
 	}
 
 	/**
 	 * 
 	 * @param source
 	 * @param sentence
 	 * @param nouns
 	 * @return
 	 */
 	public Set<String> getDescriptorsRule1(String source, String sentence,
 			Set<String> nouns) {
 		Set<String> descriptors = new HashSet<String>();
 		// single word
 		if (source.matches("^.*\\.xml_\\S+_.*$") && (!sentence.matches("^.*\\s.*$"))) {
 			Iterator<String> iter = nouns.iterator();
 			boolean isExist = false;
 			while (iter.hasNext()) {
 				String noun = iter.next();
 				if (noun.equals(sentence)) {
 					isExist = true;
 					break;
 				}
 			}
 			if (isExist == false) {
 				sentence = sentence.toLowerCase();
 				descriptors.add(sentence);
 			}
 		}
 
 		return descriptors;
 	}
 
 	/**
 	 * (is|are) red: isDescriptor
 	 * 
 	 * @param oSent
 	 * @return
 	 */
 	public Set<String> getDescriptorsRule2(String sentence,
 			Map<String, Boolean> descriptorMap) {
 		Set<String> descriptors = new HashSet<String>();
 
 		String[] tokens = sentence.split("\\s+");
 
 		for (int i = 0; i < tokens.length; i++) {
 			String token = tokens[i];
 			token = token.toLowerCase();
 			if (isDescriptor(token, descriptorMap)) {
 				token = token.toLowerCase();
 				descriptors.add(token);
 			}
 		}
 
 		return descriptors;
 	}
 
 	/**
 	 * Check if the term is a descriptor
 	 * 
 	 * @param term
 	 * @param descriptorMap
 	 *            descriptors have already learned
 	 * @return a boolean value indicating whether the term is a descriptor. This
 	 *         result will be stored in the descriptorMap for future use
 	 */
 	public boolean isDescriptor(String term, Map<String, Boolean> descriptorMap) {
 		if (descriptorMap.containsKey(term)) {
 			if (descriptorMap.get(term).booleanValue()) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 				String originalSentence = this.myDataHolder.getSentenceHolder().get(i)
 						.getOriginalSentence();
 				if (isMatched(originalSentence, term, descriptorMap)) {
 					return true;
 				}
 			}
 			term = term.toLowerCase();
 			descriptorMap.put(term, false);
 			return false;
 		}
 
 	}
 
 	/**
 	 * Check if the term matches the sentence
 	 * 
 	 * @param sentence
 	 * @param term
 	 * @param descriptorMap
 	 * @return a boolean value indicating whether the term matches the sentence
 	 */
 	public boolean isMatched(String sentence, String term,
 			Map<String, Boolean> descriptorMap) {
 		if (sentence.matches("^.*" + " (is|are|was|were|be|being) " + term
 				+ ".*$")) {
 			term = term.toLowerCase();
 			descriptorMap.put(term, true);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public void addStopWords() {
 		List<String> stops = new ArrayList<String>();
 		stops.addAll(Arrays.asList(Constant.STOP.split("\\|")));
 		stops.addAll(Arrays.asList(new String[] { "NUM", "(", "[", "{", ")",
 				"]", "}", "\\\\d+" }));
 
 //		System.out.println(stops);
 //		System.out.println(Constant.FORBIDDEN);
 
 		for (int i = 0; i < stops.size(); i++) {
 			String word = stops.get(i);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateTable(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 0, 0, null, null));
 			// System.out.println("Add Stop Word: " + word+"\n");
 		}
 	}
 
 	public void addCharacters() {
 		List<String> chars = new ArrayList<String>();
 		chars.addAll(Arrays.asList(Constant.CHARACTER.split("\\|")));
 //
 //		System.out.println(chars);
 //		System.out.println(Constant.CHARACTER);
 
 		for (int i = 0; i < chars.size(); i++) {
 			String word = chars.get(i);
 			// String reg="\\b("+Constant.FORBIDDEN+")\\b";
 			// boolean f = word.matches(reg);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateTable(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("", 0, 0, null, null));
 			// System.out.println("addCharacter word: " + word);
 		}
 	}
 
 	public void addNumbers() {
 		List<String> nums = new ArrayList<String>();
 		nums.addAll(Arrays.asList(Constant.NUMBER.split("\\|")));
 
 //		System.out.println(nums);
 //		System.out.println(Constant.NUMBER);
 
 		for (int i = 0; i < nums.size(); i++) {
 			String word = nums.get(i);
 			// String reg="\\b("+Constant.FORBIDDEN+")\\b";
 			// boolean f = word.matches(reg);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateTable(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 0, 0, null, null));
 			// System.out.println("add Number: " + word);
 		}
 		this.myDataHolder.updateTable("NUM", "b", "*", "wordpos", 0);
 		// this.getWordPOSHolder().put(new WordPOSKey("NUM", "b"), new
 		// WordPOSValue("*",0, 0, null, null));
 	}
 
 	public void addClusterstrings() {
 		List<String> cltstrs = new ArrayList<String>();
 		cltstrs.addAll(Arrays.asList(Constant.CLUSTERSTRING.split("\\|")));
 
 //		System.out.println(cltstrs);
 //		System.out.println(Constant.CLUSTERSTRING);
 
 		for (int i = 0; i < cltstrs.size(); i++) {
 			String word = cltstrs.get(i);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateTable(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 1, 1, null, null));
 			// System.out.println("addClusterString: " + word);
 		}
 	}
 
 	public void addProperNouns() {
 		List<String> ppnouns = new ArrayList<String>();
 		ppnouns.addAll(Arrays.asList(Constant.PROPERNOUN.split("\\|")));
 
 		for (int i = 0; i < ppnouns.size(); i++) {
 			String word = ppnouns.get(i);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateTable(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "z"), new
 			// WordPOSValue("*", 0, 0, null, null));
 			// System.out.println("Add ProperNoun: " + word);
 		}
 	}
 	
 	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	// suffix: -fid(adj), -form (adj), -ish(adj), -less(adj), -like (adj)),
 	// -merous(adj), -most(adj), -shaped(adj), -ous(adj)
 	// -ly (adv), -er (advj), -est (advj),
 	// foreach unknownword in unknownwords table
 	// seperate root and suffix
 	// if root is a word in WN or in unknownwords table
 	// make the unknowword a "b" boundary
 
 	/**
 	 * for each unknownword in unknownwords table seperate root and suffix if
 	 * root is a word in WN or in unknownwords table make the unknowword a "b"
 	 * boundary
 	 * 
 	 * suffix: -fid(adj), -form (adj), -ish(adj), -less(adj), -like (adj)),
 	 * -merous(adj), -most(adj), -shaped(adj), -ous(adj)
 	 */
 	public void posBySuffix() {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.posBySuffix");		
 		myLogger.trace("Enter posBySuffix");
 		
 		Iterator<Map.Entry<String, String>> iterator = this.myDataHolder.getUnknownWordHolder()
 				.entrySet().iterator();
 
 		while (iterator.hasNext()) {
 			Map.Entry<String, String> unknownWordEntry = iterator.next();
 			String unknownWord = unknownWordEntry.getKey();
 			String unknownWordTag = unknownWordEntry.getValue();
 
 			if (unknownWordTag.equals("unknown")) {
 				boolean flag1 = posBySuffixCase1Helper(unknownWord);				
 				boolean flag2 = posBySuffixCase2Helper(unknownWord);								
 			}
 		}
 		
 		myLogger.trace("Quite posBySuffix");
 	}
 
 	public boolean posBySuffixCase1Helper(String unknownWord) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.posBySuffix");
 		
 		String pattern1 = "^[a-z_]+(" + Constant.SUFFIX + ")$";
 		myLogger.debug("Pattern1: "+pattern1);
 				
 		if (unknownWord.matches(pattern1)) {
 			Matcher matcher = Pattern.compile(
 					"(.*?)(" + Constant.SUFFIX + ")$").matcher(
 					unknownWord);
 			if ((unknownWord.matches("^[a-zA-Z0-9_-]+$"))
 					&& matcher.matches()) {
 				myLogger.debug("posBySuffix - check word: " + unknownWord);
 				String base = matcher.group(1);
 				String suffix = matcher.group(2);
 				if (this.containSuffix(unknownWord, base, suffix)) {
 					myLogger.debug("Pass\n");
 					this.myDataHolder.updateTable(unknownWord, "b", "*", "wordpos", 0);							
 					myLogger.debug("posBySuffix - set word: " + unknownWord);
 					return true;
 				}
 				else {
 					myLogger.debug("Not Pass\n");
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean posBySuffixCase2Helper(String unknownWord) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.posBySuffix");
 		
 		String pattern2 = "^[._.][a-z]+"; // , _nerved
 		myLogger.debug("Pattern2: "+pattern2);
 		
 		if (unknownWord.matches(pattern2)) {
 			this.myDataHolder.getWordPOSHolder().put(new WordPOSKey(unknownWord, "b"),
 					new WordPOSValue("*", 0, 0, null, null));
 			myLogger.debug("posbysuffix set "+unknownWord + " a boundary word\n");
 			return true;
 		}
 		
 		return false;
 	}
 
 	
 	/**
 	 * return false or true depending on if the word contains the suffix as the
 	 * suffix
 	 * 
 	 * @param word
 	 * @param base
 	 * @param suffix
 	 * @return
 	 */
 	public boolean containSuffix(String word, String base, String suffix) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.posBySuffix.containSuffix");		
 		myLogger.trace("Enter containSuffix");
 		
 		boolean flag = false; // return value
 		boolean wordInWN = false; // if this word is in WordNet
 		boolean baseInWN = false;
 		WordNetAPI myWN = this.myUtility.getWordNet();
 
 		// check base
 		if (base.length() == 0) {
 			myLogger.trace("case 0");
 			return true;
 		}
 
 		base.replaceAll("_", ""); // cup_shaped
 
 		if (myWN.contains(word)) {
 			myLogger.trace("case 1.1");
 			wordInWN = true; // word is in WordNet
 		} else {
 			myLogger.trace("case 1.2");
 			wordInWN = false;
 		}
 
 		if (myWN.contains(base)) {
 			myLogger.trace("case 2.1");
 			baseInWN = true;
 		} else {
 			myLogger.trace("case 2.2");
 			baseInWN = false;
 		}
 
 		// if WN pos is adv, return 1: e.g. ly, or if $base is in
 		// unknownwords table
 		if (suffix.equals("ly")) {
 			myLogger.trace("case 3.1");
 			if (wordInWN) {
 				if (myWN.isAdverb(word)) {
 					return true;
 				}
 			}
 			// if the word is in unknown word set, return true
 			if (this.myDataHolder.getUnknownWordHolder().containsKey(base)) {
 				return true;
 			}
 		}
 
 		// if WN recognize superlative, comparative adjs, return 1: e.g. er, est
 		else if (suffix.equals("er") || suffix.equals("est")) {
 			myLogger.trace("case 3.2");
 			if (wordInWN) {
 				boolean case1 =!myWN.isAdjective(word);
 				boolean case2 = myWN.isAdjective(base); 
 				if (case1 && case2) {
 					return true;
 				}
 				else {
 					return false;
 				}
 			}
 		}
 
 		// if $base is in WN or unknownwords table, or if $word has sole pos
 		// adj in WN, return 1: e.g. scalelike
 		else {
 			myLogger.trace("case 3.3");
 			if (myWN.isSoleAdjective(word)) {
 				return true;
 			}
 			if (baseInWN) {
 				return true;
 			}
 			if (this.myDataHolder.getUnknownWordHolder().containsKey(base)) {
 				return true;
 			}
 		}
 
 		return flag;
 	}
 
 	public void markupByPattern() {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.markupByPattern");		
 		myLogger.trace("Enter markupByPattern");
 		
 		int size = this.myDataHolder.getSentenceHolder().size();
 
 		for (int i = 0; i < size; i++) {			
 			boolean flag = markupByPatternHelper(this.myDataHolder.getSentenceHolder().get(i));
 			if (flag) {
 				myLogger.debug("Updated Sentence #"+i);
 			}			
 		}
 		myLogger.trace("Quite markupByPattern");
 	}
 
 	public boolean markupByPatternHelper(Sentence sentence) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("markupByPattern");	
 		// case 1
 		if (sentence.getOriginalSentence().matches("^x=.*")) {
 			myLogger.trace("Case 1");
 			sentence.setTag("chromosome");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 2
 		else if (sentence.getOriginalSentence().matches("^2n=.*")) {
 			myLogger.trace("Case 2");
 			sentence.setTag("chromosome");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 3
 		else if (sentence.getOriginalSentence().matches("^x .*")) {
 			myLogger.trace("Case 3");
 			sentence.setTag("chromosome");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 4
 		else if (sentence.getOriginalSentence().matches("^2n .*")) {
 			myLogger.trace("Case 4");
 			sentence.setTag("chromosome");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 5
 		else if (sentence.getOriginalSentence().matches("^2 n.*")) {
 			myLogger.trace("Case 5");
 			sentence.setTag("chromosome");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 6
 		else if (sentence.getOriginalSentence().matches("^fl.*")) {
 			myLogger.trace("Case 6");
 			sentence.setTag("flowerTime");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 7
 		else if (sentence.getOriginalSentence().matches("^fr.*")) {
 			myLogger.trace("Case 7");
 			sentence.setTag("fruitTime");
 			sentence.setModifier("");
 			return true;
 		}
 		return false;
 	}
 
 	// private String IGNOREPTN ="(IGNOREPTN)"; //disabled
 	public void markupIgnore() {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.markupIgnore");		
 		myLogger.trace("Enter markupIgnore");
 		
 		for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 			boolean flag = markupIgnoreHelper(this.myDataHolder.getSentenceHolder().get(i));
 			if (flag) {
 				myLogger.debug("Updated Sentence #"+i);
 			}
 		}
 		
 		myLogger.trace("Quite markupIgnore");
 	}
 
 	public boolean markupIgnoreHelper(Sentence sentence) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("markupIgnore");		
 		
 		String thisOriginalSentence = sentence.getOriginalSentence();
 		String pattern = "(^|^ )" + Constant.IGNOREPTN + ".*$";
 		if (thisOriginalSentence.matches(pattern)) {
 			sentence.setTag("ignore");
 			sentence.setModifier("");
 			myLogger.trace("Set Tag to \"ignore\", Modifier to \"\"");
 			
 			return true;
 		}
 		
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param status
 	 *            "start" or "normal"
 	 * @return
 	 */
 	public int discover(String status) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.discover");
 		
 		myLogger.trace("Enter Discover - Status: "+status);
 		
 		int newDisc = 0;
 		
 		this.myDataHolder.printHolder(DataHolder.SENTENCE);
 		
 		for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 			Sentence sentEntry = this.myDataHolder.getSentenceHolder().get(i);
 			// sentid
 			String thisSentence = sentEntry.getSentence();
 			String thisLead = sentEntry.getLead();
 			String thisTag = sentEntry.getTag();
 			String thisStatus = sentEntry.getStatus();
 			//if (!(thisTag == null || !thisTag.equals("ignore") 
 			
 			//myLogger.debug("Tag: "+thisTag);
 
 			
 			
 			if ((
 					//!thisTag.equals("ignore") 
 					
 					!StringUtility.equalsWithNull(thisTag, "ignore")
 					
 					|| (thisTag == null)
 					
 					)
 					&& thisStatus.equals(status)) {
 				
 				myLogger.debug("Sentence #: "+i);
 				myLogger.debug("Lead: " + thisLead);
 				
 				myLogger.debug("Tag: "+thisTag);
 				
 				myLogger.debug("Sentence: "+thisSentence);
 				// tag is not null
 				if (isMarked(this.myDataHolder.getSentenceHolder().get(i))) {
 					myLogger.debug("Not Pass");
 					continue;
 				}				
 				// tag is null
 				else {
 					myLogger.debug("Pass");
 				}
 				
 				
 				String[] startWords = thisLead.split("\\s+");
 				myLogger.debug("startWords: "+startWords.toString());
 
 				String pattern = buildPattern(startWords);
 				myLogger.info("Build pattern ["+pattern+"] from starting words ["+thisLead+"]");
 				
 				if (pattern != null) {
 					// IDs of untagged sentences that match the pattern
 					Set<Integer> matched = matchPattern(pattern, status, false);
 					int round = 0;
 					int numNew = 0;
 
 					do {
 						numNew = ruleBasedLearn(matched);
 						newDisc = newDisc + numNew;
 						round++;
 					} while (numNew > 0);
 				}
 			}
 		}
 
 		myLogger.trace("Return " + newDisc);
 		myLogger.trace("Quite discover");
 		return newDisc;
 	}
 
 	/**
 	 * A helper of method discover(). Check if the tag of the i-th sentence is
 	 * NOT null
 	 * 
 	 * @param sentence
 	 *            the sentence to check
 	 * @return if the tag of the i-th sentence is NOT null, returns true;
 	 *         otherwise returns false
 	 */
 	public boolean isMarked(Sentence sentence) {
 		String thisTag = sentence.getTag();
 
 		if (thisTag != null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * build a pattern based on existing checked word set, and the start words
 	 * 
 	 * @param startWords
 	 * @return a pattern. If no pattern is generated, return null
 	 */
 	public String buildPattern(String[] startWords) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.buildPattern");
 		
 		myLogger.trace("Enter buildPattern");
 		myLogger.trace("Start Words: "+startWords);
 				
 		Set<String> newWords = new HashSet<String>();
 		String temp = "";
 		String prefix = "\\w+\\s";
 		String pattern = "";
 		
 		Set<String> checkedWords = this.checkedWordSet;
 		myLogger.trace("checkedWords: " + checkedWords);
 
 		for (int i = 0; i < startWords.length; i++) {
 			String word = startWords[i];
 			// This is not very sure, need to make sure - Dongye
 			if ((!word.matches("[\\p{Punct}0-9]")) 
 					&& (!checkedWords.contains(word))) {
 				temp = temp + word + "|";
 				newWords.add(word);
 			}
 		}
 		myLogger.trace("temp: " + temp);		
 
 		// no new words
 		if (temp.length() == 0) {
 			myLogger.trace("No new words");
 			myLogger.trace("Return null");
 			myLogger.trace("Quite buildPattern");
 			myLogger.trace("\n");
 			return null;
 		} else {
 
 			// remove the last char, which is a '|'
 			temp = temp.substring(0, temp.length() - 1);
 		}
 
 		temp = "\\b(?:" + temp + ")\\b";
 		pattern = "^" + temp + "|";
 
 		for (int j = 0; j < this.NUM_LEAD_WORDS - 1; j++) {
 			temp = prefix + temp;
 			pattern = pattern + "^" + temp + "|";
 		}
 		myLogger.trace("Pattern: "+pattern);
 		
 		pattern = pattern.substring(0, pattern.length() - 1);
 		pattern = "(?:" + pattern + ").*$";
 		checkedWords.addAll(newWords);
 		this.checkedWordSet = checkedWords;
 		
 		myLogger.trace("Return Pattern: "+pattern);
 		myLogger.trace("Quite buildPattern");
 		myLogger.trace("\n");
 		return pattern;
 	}
 	
 	/**
 	 * Find the IDs of the sentences that matches the pattern
 	 * 
 	 * @param pattern
 	 * @param status
 	 * @param hasTag
 	 * @return a set of sentence IDs of the sentences that matches the pattern
 	 */
 	public Set<Integer> matchPattern(String pattern, String status, boolean hasTag) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.matchPattern");
 		
 		myLogger.trace("Enter matchPattern");
 		myLogger.trace("Pattern: "+pattern);
 		myLogger.trace("Status: "+status);
 		myLogger.trace("HasTag: "+hasTag);
 		
 		Set<Integer> matchedIDs = new HashSet<Integer>();
 
 		for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 			Sentence sent = this.myDataHolder.getSentenceHolder().get(i);
 			String thisSentence = sent.getSentence();
 			String thisStatus = sent.getStatus();
 			String thisTag = sent.getTag();
 			
 			boolean a = hasTag;
 			boolean b = (thisTag == null);
 			
 			if ((a ^ b) && (StringUtility.equalsWithNull(status, thisStatus))) {
 				Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
 				Matcher m = p.matcher(thisSentence);
 				if (m.lookingAt()) {
 					myLogger.debug("Push Sentence #"+i);
 					myLogger.debug("Sentence: "+thisSentence);
 					myLogger.debug("Status: "+thisStatus);
 					myLogger.debug("Tag: "+thisTag);
 					myLogger.debug("\n");
 										
 					matchedIDs.add(i);
 				}
 			}
 		}
 
 		myLogger.trace("Return IDs: "+matchedIDs);
 		myLogger.trace("Quite matchPattern");
 		myLogger.trace("\n");
 		return matchedIDs;
 	}
 
 	
 	/**
 	 * return a positive number if anything new is learnt from @source sentences
 	 * by applying rules and clues to grow %NOUNS and %BDRY and to confirm tags
 	 * create and maintain decision tables
 	 * 
 	 * @param matched
 	 * @return
 	 */
 	public int ruleBasedLearn(Set<Integer> matched) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.ruleBasedLearn");
 		
 		myLogger.trace("Enter ruleBasedLearn");
 		myLogger.trace("Matched IDs: "+matched);
 		
 		int sign = 0;
 		int numNew = 0;
 		String tag = "";
 
 		Iterator<Integer> iter = matched.iterator();
 		while (iter.hasNext()) {
 			int sentID = iter.next().intValue();
 			Sentence sentence = this.myDataHolder.getSentenceHolder().get(sentID);
 			if (!isMarked(sentence)) {
 				// ($tag, $new) = doit($sentid);
 				doIt(sentID);
 				// tag($sentid, $tag);
 				tagIt(sentID, tag);
 				sign = sign + numNew;
 			}
 		}
 		
 		myLogger.trace("Return: "+sign);
 		myLogger.trace("Quit ruleBaseLearn");
 		myLogger.trace("\n");
 		
 		return sign;
 	}
 
 	// update wordpos table (on certainty) when a sentence is tagged for the
 	// first time. this update should not be done when a pos is looked up,
 	// because we may lookup a pos for the same example multiple times. if the
 	// tag need to be adjusted (not by doit function), also need to adjust
 	// certainty counts.
 	public void doIt(int sentID) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.ruleBasedLearn.doIt");
 		
 		myLogger.trace("Enter doIt");
 		myLogger.trace("sentence ID: " + sentID);
 		
 		int sign = 0;
 
 		Sentence sentEntry = this.myDataHolder.getSentenceHolder().get(sentID);
 		String thisSentence = sentEntry.getSentence();
 		String thisLead = sentEntry.getLead();
 
 		String[] words = thisLead.split("\\s+");
 		String ptn = this.getPOSptn(words);
 
 		Pattern p;
 		Matcher m;
 
 		// Case 1: single word case
 		if (ptn.matches("^[pns]$")) {
 			String tag = words[0];
 			sign = sign + this.myDataHolder.updateTable(tag, ptn, "-", "wordpos", 1);
 		}
 
 		// Case 2: the POSs are "ps"
 		else {
 			p = Pattern.compile("^.*ps.*$");
 			m = p.matcher(ptn);
 			if (m.find()) {
 				int start = m.start();
 				int end = m.end();
 				String pWord = words[start];
 				String sWord = words[end - 1];
 
 				sign += this.myDataHolder.updateTable(pWord, "p", "-", "wordpos", 1);
 				sign += this.myDataHolder.updateTable(sWord, "s", "", "wordpos", 1);
 
 				// $sign += updatenn(0, $#tws+1, @tws); #up to the "p" inclusive
 
 			} else {
 				p = Pattern.compile("^.*p(\\?).*$");
 				m = p.matcher(ptn);
 				if (m.find()) {
 					int start = m.start();
 				}
 			}
 		}
 		
 		myLogger.trace("Quit doIt");
 	}
 
 	/**
 	 * The length of the ptn must be the same as the number of words in words.
 	 * If certainty is < 50%, replace POS with ?.
 	 * 
 	 * @param words
 	 * @return
 	 */
 	public String getPOSptn(String[] words) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.ruleBasedLearn.doIt.getPOSptn");
 		
 		myLogger.trace("Enter getPOSptn");
 		myLogger.trace("Words: " + StringUtility.stringArray2String(words));
				
 		String ptn = "";
 		for (int i = 0; i < words.length; i++) {
 			String word = words[i];
 			List<POSInfo> POSInfoList = this.myDataHolder.checkPOSInfo(word);
 			if (POSInfoList.size() > 0) {
 				POSInfo p = POSInfoList.get(0);
 				String POS = p.getPOS();
 				String role = p.getRole();
 				double certainty = (double) p.getCertaintyU()
 						/ (double) p.getCertaintyL();
 
				if ((!StringUtility.equalsWithNull(POS, "?")) && (certainty <= 0.5)) {
					myLogger.info("This POS has a certainty less than 0.5. It is ignored.");
 					POS = "?";
 				}
 				ptn = ptn + POS;
 			}
 		}
 
 		
 		myLogger.trace("Return ptn: " + ptn);
 		myLogger.trace("Quite getPOSptn");
 		
 		return ptn;
 	}
 
 
 
 	public void tagIt(int sentID, String tag) {
 		;
 	}
 
 	
 	/**
 	 * Utilities
 	 * @return 
 	 */
 	public Set<String> getCheckedWordSet() {
 		return this.checkedWordSet;
 	}
 	
 	public void setCheckedWordSet(Set<String> wordSet) {
 		this.checkedWordSet = wordSet;
 	}
 	
 }
