 package semanticMarkup.ling.learn.utility;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import semanticMarkup.know.lib.WordNetPOSKnowledgeBase;
 import semanticMarkup.ling.Token;
 import semanticMarkup.ling.learn.auxiliary.KnownTagCollection;
 import semanticMarkup.ling.learn.auxiliary.StringAndInt;
 import semanticMarkup.ling.learn.dataholder.DataHolder;
 import semanticMarkup.ling.learn.dataholder.ModifierTableValue;
 import semanticMarkup.ling.learn.dataholder.SentenceStructure;
 import semanticMarkup.ling.learn.dataholder.WordPOSKey;
 import semanticMarkup.ling.learn.dataholder.WordPOSValue;
 import semanticMarkup.ling.learn.knowledge.Constant;
 import semanticMarkup.ling.transform.ITokenizer;
 
 public class LearnerUtility {
 
 	private ITokenizer mySentenceDetector;
 	private ITokenizer mytokenizer;
 	private WordFormUtility myWordFormUtility;
 	private WordNetPOSKnowledgeBase myWordNetPOS;
 	private Constant myConstant;
 	
 	public LearnerUtility(ITokenizer sentenceDetector, ITokenizer tokenizer, WordNetPOSKnowledgeBase wordNetPOS) {
 		this.myConstant = new Constant();
 		this.mySentenceDetector = sentenceDetector;
 		this.mytokenizer = tokenizer;
 		this.myWordFormUtility = new WordFormUtility(wordNetPOS);
 		this.myWordNetPOS = wordNetPOS;
 	}
 	
 	public Constant getConstant(){
 		return this.myConstant;
 	}
 	
 	public ITokenizer getTokenizer(){
 		return this.mytokenizer;
 	}
 	
 	public ITokenizer getSentenceDetector(){
 		return this.mySentenceDetector;
 	}
 	
 	public WordFormUtility getWordFormUtility(){
 		return this.myWordFormUtility;
 	}
 	
 	public WordNetPOSKnowledgeBase getWordNetPOSKnowledgeBase(){
 		return this.myWordNetPOS;
 	}
 	
 	// populate sentence utilities
 	/**
 	 * Given a file name, return its type
 	 * 
 	 * @param fileName
 	 * @return return 1 if it is a file of character file, or 2 if it is a
 	 *         description file, otherwise return 0
 	 */
 	public int getType(String fileName) {
 		// remove pdf.xml
 		fileName = fileName.replaceAll(".*\\.xml_", "");
 		// remove all non_ charaters
 		fileName = fileName.replaceAll("[^_]", "");
 
 		// a character file
 		if (fileName.length() == 0) {
 			return 1;
 		}
 
 		// a description file
 		if (fileName.length() == 1) {
 			return 2;
 		}
 
 		return 0;
 	}
 	
 	
 
 
 	/**
 	 * replace '.', '?', ';', ':', '!' within brackets by some special markers,
 	 * to avoid split within brackets during sentence segmentation
 	 * 
 	 * @param text
 	 * @return
 	 */
 	public String hideMarksInBrackets(String text) {
 
 		if (text == null || text == "") {
 			return text;
 		}
 
 		String hide = "";
 		int lRound = 0;
 		int lSquare = 0;
 		int lCurly = 0;
 
 		for (int i = 0; i < text.length(); i++) {
 			char c = text.charAt(i);
 			switch (c) {
 			case '(':
 				lRound++;
 				hide = hide + c;
 				break;
 			case ')':
 				lRound--;
 				hide = hide + c;
 				break;
 			case '[':
 				lSquare++;
 				hide = hide + c;
 				break;
 			case ']':
 				lSquare--;
 				hide = hide + c;
 				break;
 			case '{':
 				lCurly++;
 				hide = hide + c;
 				break;
 			case '}':
 				lCurly--;
 				hide = hide + c;
 				break;
 			default:
 				if (lRound + lSquare + lCurly > 0) {
 					if (c == '.') {
 						hide = hide + "[DOT] ";
 					} else if (c == '?') {
 						hide = hide + "[QST] ";
 					} else if (c == ';') {
 						hide = hide + "[SQL] ";
 					} else if (c == ':') {
 						hide = hide + "[QLN] ";
 					} else if (c == '!') {
 						hide = hide + "[EXM] ";
 					} else {
 						hide = hide + c;
 					}
 				} else {
 					hide = hide + c;
 				}
 			}
 		}
 		return hide;
 
 	}
 	
 	
 	/**
 	 * Put all words in this sentence into the words map
 	 * 
 	 * @param sent
 	 * @param words
 	 *            a map mapping all words already known to their counts
 	 * @return a new map of all words, including words in sent
 	 */
 	public Map<String, Integer> getAllWords(String sentence,
 			Map<String, Integer> words) {
 		List<String> tokens = this.tokenizeText(sentence, "all");
 
 		for (String token: tokens) {
 			if (words.containsKey(token)) {
 				int count = words.get(token);
 				count = count + 1;
 				words.put(token, count);
 			} else {
 				words.put(token, 1);
 			}
 		}
 
 		return words;
 	}
 	
 	/**
 	 * returns the first n words of the sentence
 	 * 
 	 * @param sent
 	 *            the sentence
 	 * @param n
 	 *            number of words to be returned
 	 * @return the first n words of the sentence. If the number of words in the
 	 *         sentence is less than n, return all of them.
 	 */
 	public List<String> getFirstNWords(String sentence, int n) {
 		List<String> nWords = new ArrayList<String>();
 
 		if (sentence == null || sentence == "") {
 			return nWords;
 		}
 		
 		List<String> tokens = this.tokenizeText(sentence, "firstseg");
 		
 		
 		int minL = tokens.size() > n ? n : tokens.size();
 		for (int i = 0; i < minL; i++) {
 			nWords.add(tokens.get(i));
 		}
 
 		return nWords;
 	}
 	
 
 	/**
 	 * Restore '.', '?', ';', ':', '!' within brackets
 	 * 
 	 * @param text
 	 * @return the restored string
 	 */
 	public String restoreMarksInBrackets(String text) {
 
 		if (text == null || text == "") {
 			return text;
 		}
 
 		// restore "." from "[DOT]"
 		text = text.replaceAll("\\[\\s*DOT\\s*\\]", ".");
 		// restore "?" from "[QST]"
 		text = text.replaceAll("\\[\\s*QST\\s*\\]", "?");
 		// restore ";" from "[SQL]"
 		text = text.replaceAll("\\[\\s*SQL\\s*\\]", ";");
 		// restore ":" from "[QLN]"
 		text = text.replaceAll("\\[\\s*QLN\\s*\\]", ":");
 		// restore "." from "[DOT]"
 		text = text.replaceAll("\\[\\s*EXM\\s*\\]", "!");
 
 		return text;
 	}
 
 	/**
 	 * Add space before and after all occurence of the regex in the string str
 	 * 
 	 * @param str
 	 * @param regex
 	 * @return
 	 */
 	public String addSpace(String str, String regex) {
 
 		if (str == null || str == "" || regex == null || regex == "") {
 			return str;
 		}
 
 		Matcher matcher = Pattern.compile("(^.*)(" + regex + ")(.*$)").matcher(
 				str);
 		if (matcher.lookingAt()) {
 			str = addSpace(matcher.group(1), regex) + " " + matcher.group(2)
 					+ " " + addSpace(matcher.group(3), regex);
 			return str;
 		} else {
 			return str;
 		}
 	}
 	
 	public List<String> tokenizeText(String sentence, String mode) {
 		if (StringUtils.equals(mode, "firstseg")) {
 			sentence = getSentenceHead(sentence);
 		}
 		else {
 			;
 		}
 		
 		String[] tempWords = sentence.split("\\s+");
 		List<String> words = new ArrayList<String>();
 		words.addAll(Arrays.asList(tempWords));
 		
 		return words;
 	}
 	
 	/**
 	 * Get the portion in the input sentence before any of ,:;.[(, or any
 	 * preposition word, if any
 	 * 
 	 * @param sentence
 	 *            the input sentence
 	 * @return the portion in the head
 	 */
 	public String getSentenceHead(String sentence) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger
 				.getLogger("learn.populateSentence.getFirstNWords.getHead");
 
 		if (sentence == null) {
 			return sentence;
 		}
 		else if (sentence.equals("")) {
 			return sentence;
 		} 
 		else {
 			String head = "";
 			int end = sentence.length();
 
 			String pattern1 = " [,:;.\\[(]";
 			String pattern2 = "\\b" + "(" + Constant.PREPOSITION + ")" + "\\s";
 
 			myLogger.trace("Pattern1: " + pattern1);
 			myLogger.trace("Pattern2: " + pattern2);
 
 			Pattern p1 = Pattern.compile(pattern1);
 			Pattern p2 = Pattern.compile(pattern2);
 
 			Matcher m1 = p1.matcher(sentence);
 			Matcher m2 = p2.matcher(sentence);
 
 			boolean case1 = m1.find();
 			boolean case2 = m2.find();
 			
 			if (case1 || case2) {
 				// case 1
 				if (case1) {
 					int temp1 = m1.end();
 					end = temp1 < end ? temp1 : end;
 					end = end - 1;
 				}
 				// case 2
 				else {
 					int temp2 = m2.end();
 					end = temp2 < end ? temp2 : end;
 				}
 
 				head = sentence.substring(0, end - 1);
 			}
 			else {
 				head = sentence;
 			}
 
 			myLogger.trace("Return: " + head);
 			return head;
 		}
 	}
 	
 	/**
 	 * Segment a text into sentences using the OpenNLP sentence detector. Note
 	 * how dot after any abbreviations is handled: to avoid segmenting at
 	 * abbreviations, the dots of abbreviations are first replaced by a special
 	 * mark before the text is segmented. Then after the segmentation, they are
 	 * restored back.
 	 * 
 	 * @param text
 	 * @return List of Sentence
 	 */
 	public List<Token> segmentSentence(String text) {
 		List<Token> sentences;
 		
 		//hide abbreviations
 		text = this.hideAbbreviations(text);
 		
 		// do sentence segmentation
 		
 		sentences = this.mySentenceDetector.tokenize(text);
 		
 		// restore Abbreviations
 		
 		for (Token sentence: sentences){
 			String contentHideAbbreviations = sentence.getContent();
 			String contentRestoreAbbreviations = this.restoreAbbreviations(contentHideAbbreviations);
 			sentence.setContent(contentRestoreAbbreviations); 
 		}
 		
 		return sentences;
 	}
 	
 	/**
 	 * replace the dot (.) mark of abbreviations in the text by a special mark
 	 * ([DOT])
 	 * 
 	 * @param text
 	 * @return the text after replacement
 	 */
 	public String hideAbbreviations(String text) {
 		String pattern = "(^.*)("
 				+Constant.PEOPLE_ABBR
 				+"|"+Constant.ARMY_ABBR
 				+"|"+Constant.INSTITUTES_ABBR
 				+"|"+Constant.COMPANIES_ABBR
 				+"|"+Constant.PLACES_ABBR
 				+"|"+Constant.MONTHS_ABBR
 				+"|"+Constant.MISC_ABBR
 				+"|"+Constant.BOT1_ABBR
 				+"|"+Constant.BOT2_ABBR
 				+"|"+Constant.LATIN_ABBR
 				+")(\\.)(.*$)";
 		//pattern = "(^.*)(jr|abc)(\\.)(.*$)";
 		
 		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
 		Matcher m;
 		m= p.matcher(text);
 		while (m.matches()){
 			String head = m.group(1);
 			String abbr = m.group(2);
 			String dot = m.group(3);
 			String remaining = m.group(4);
 			dot = "[DOT]";
 			text= head+abbr+dot+remaining;
 			m=p.matcher(text);
 		}
 		
 		return text;
 	}
 	
 	/**
 	 * restore the dot (.) mark of abbreviations in the text from special mark
 	 * ([DOT])
 	 * 
 	 * @param text
 	 * @return the text after replacement
 	 */
 	public String restoreAbbreviations(String text) {
 		String pattern = "(^.*)("
 				+Constant.PEOPLE_ABBR
 				+"|"+Constant.ARMY_ABBR
 				+"|"+Constant.INSTITUTES_ABBR
 				+"|"+Constant.COMPANIES_ABBR
 				+"|"+Constant.PLACES_ABBR
 				+"|"+Constant.MONTHS_ABBR
 				+"|"+Constant.MISC_ABBR
 				+"|"+Constant.BOT1_ABBR
 				+"|"+Constant.BOT2_ABBR
 				+"|"+Constant.LATIN_ABBR
 				+")(\\[DOT\\])(.*$)";
 		//pattern = "(^.*)(jr|abc)(\\.)(.*$)";
 		
 		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
 		Matcher m;
 		m= p.matcher(text);
 		while (m.matches()){
 			String head = m.group(1);
 			String abbr = m.group(2);
 			String dot = m.group(3);
 			String remaining = m.group(4);
 			dot = ".";
 			text= head+abbr+dot+remaining;
 			m=p.matcher(text);
 		}
 		
 		return text;
 	}
 
 	/**
 	 * Convert a collection of words to a string of those words separated by "|"
 	 * 
 	 * @param c
 	 *            collection of words
 	 * @return string of pattern. If the collection is null or empty, return an
 	 *         empty string
 	 */
 	public String Iterable2Pattern(Iterable<String> words) {
 		if (words == null) {
 			return "";
 		}
 
 		List<String> wordList = new LinkedList<String>();
 		for (String word: words) {
 			word = this.addDoubleBackslash(word);
 			wordList.add(word);
 		}
 		String pattern = StringUtils.join(wordList, "|");
 //		pattern = this.addDoubleBackslash(pattern);
 		
 		
 		
 		
 //		testRunner("\\\\", "abc\\abc");
 //		testRunner("\\(", "abc(abc");
 //		testRunner("\\)", "abc)abc");
 //		testRunner("\\[", "abc[abc");
 //		testRunner("\\]", "abc]abc");
 //		testRunner("\\{", "abc{abc");
 //		testRunner("\\}", "abc}abc");
 //		testRunner("\\.", "abc.abc");
 //		testRunner("\\|", "abc|abc");
 //		testRunner("\\+", "abc+abc");
 //		testRunner("\\*", "abc*abc");
 //		testRunner("\\?", "abc?abc");
 //		testRunner("\\d+", "01138");
 		
 //		[-\\\\\\(\\)\\[\\]\\{\\}\\.\\|\\+\\*\\?]
 //				
 //				stops.addAll(Arrays.asList(new String[] { "NUM", "(", "[", "{",
 //						")", "]", "}", "d+" }));
 
 		return pattern;
 	}
 
 	/**
 	 * Convert a pattern with words separated by "|" to a set
 	 * 
 	 * @param pattern
 	 *            the pattern
 	 * @return a set. If the input is null or empty string, return a empty set
 	 */
 	public static Set<String> Pattern2Set(String pattern) {
 		Set<String> set = new HashSet<String>();
 
 		if (StringUtils.equals(pattern, null)
 				|| StringUtils.equals(pattern, "")) {
 			return (set);
 		}
 
 		set.addAll(Arrays.asList(pattern.split("|")));
 
 		return set;
 	}
 	
 	/**
 	 * tag words with all o n m b tags that are applicable to the words
 	 * 
 	 * @param mode
 	 *            "singletag" or "multitags"
 	 * @param type
 	 *            "sentence" or "orginal"
 	 */
 	public void tagAllSentences (DataHolder dataholderHandler, String mode, String type) {
 		List<StringAndInt> idAndSentenceList = new LinkedList<StringAndInt>();
 		
 		Iterator<SentenceStructure> sentenceIter = 
 				dataholderHandler.getSentenceHolder().iterator();
 		
 		if (StringUtils.equals(mode, "original")) {
 			while (sentenceIter.hasNext()) {
 				SentenceStructure sentence = sentenceIter.next();
 				int thisID = sentence.getID();
 				String thisOriginalSentence = sentence.getOriginalSentence();
 				idAndSentenceList.add(new StringAndInt(thisOriginalSentence, thisID));
 			}
 		}
 		else {
 			while (sentenceIter.hasNext()) {
 				SentenceStructure sentence = sentenceIter.next();
 				int thisID = sentence.getID();
 				String thisSentence = sentence.getSentence();
 				idAndSentenceList.add(new StringAndInt(thisSentence, thisID));
 			}
 		}
 		
 		KnownTagCollection myKnownTags = this.getKnownTags(dataholderHandler, mode);
 	
 		Iterator<StringAndInt> idAndSentenceListIter = idAndSentenceList.iterator();
 		while (idAndSentenceListIter.hasNext()) {
 			StringAndInt idAndSentence = idAndSentenceListIter.next();
 			int thisID = idAndSentence.getInt();
 			if (thisID == 127) {
 				System.out.println();
 			}
 			String thisSentence = idAndSentence.getString();
 			
 			thisSentence = tagAllSentencesHelper(thisSentence);
 			thisSentence = annotateSentence(thisSentence, myKnownTags, dataholderHandler.getBMSWords());
 			
 			SentenceStructure targetSentence = dataholderHandler.getSentence(thisID);
 			
 			if (StringUtils.equals(mode, "original")) {
 				targetSentence.setOriginalSentence(thisSentence);
 			}
 			else {
 			targetSentence.setSentence(thisSentence);
 			}
 		}
 		
 	}
     
 	/**
 	 * Helper of tagAllSentencesHelper method
 	 * @param text
 	 * @return text after processing
 	 */
 	public String tagAllSentencesHelper(String text) {
 		text = text.replaceAll("<\\S+?>", "");
 		text = text.toLowerCase();
 		
 		// cup_shaped, 3_nerved, 3-5 (-7)_nerved
 //		Matcher m2 = StringUtility.createMatcher("\\s*-\\s*([a-z])", text);
 //		while (m2.find()) {
 //			String group1 = m2.group(1);
 //			text = m2.replaceFirst("_"+group1);
 //			m2 = StringUtility.createMatcher("\\s*-\\s*([a-z])", text);
 //		}
 		
 		//$b =~ s#\b(_[a-z]+)\b#(?\:\\b\\d+)$1#g; #_nerved => (?:\b\d+)_nerved
 //		$sent =~ s#\s*-\s*([a-z])#_$1#g; 
 		text = StringUtility.replaceAllBackreference(text, "\\s*-\\s*([a-z])", "_$1");
 		
 		// add space around nonword char
 		text = StringUtility.replaceAllBackreference(text, "(\\W)", " $1 ");
 		
 		// multiple spaces => 1 space
 		text = text.replaceAll("\\s+", " ");	
 		// trim
 		text = text.replaceAll("^\\s*", "");	
 		text = text.replaceAll("\\s*$", "");	
 		
 		return text;
 	}
 	
 	
 	
 	public String annotateSentence(String sentence,
 			KnownTagCollection knownTags, Set<String> NONS) {
 		// get known tags
 		Set<String> boundaryMarks;
 		Set<String> boundaryWords;
 		Set<String> modifiers;
 		Set<String> nouns;
 		Set<String> organs;
 		Set<String> properNouns;
 		
 		if (knownTags.boundaryMarks == null) {
 			boundaryMarks = new HashSet<String>();
 		} else {
 			boundaryMarks = knownTags.boundaryMarks;
 		}
 		
 		if (knownTags.boundaryWords == null) {
 			boundaryWords = new HashSet<String>();
 		} else {
 			boundaryWords = knownTags.boundaryWords;
 		}
 		
 		if (knownTags.modifiers == null) {
 			modifiers = new HashSet<String>();
 		} else {
 			modifiers = knownTags.modifiers;
 		}
 		
 		if (knownTags.nouns== null) {
 			nouns = new HashSet<String>();
 		} else {
 			nouns = knownTags.nouns;
 		}
 		
 		if (knownTags.organs == null) {
 			organs = new HashSet<String>();
 		} else {
 			organs = knownTags.organs;
 		}
 		
 		if (knownTags.properNouns == null) {
 			properNouns = new HashSet<String>();
 		} else {
 			properNouns = knownTags.properNouns;
 		}
 		
 		// preprocessing 1
 		List<String> bDeleteList = new LinkedList<String>();
 		List<String> bAddList = new LinkedList<String>();
 		Iterator<String> bIter = boundaryWords.iterator();
 		while(bIter.hasNext()) {
 			String oldWord = bIter.next();
 			
 			if (oldWord.charAt(0)=='_') {
 				String newWord = "(?\\:\\b\\d+)"+oldWord;
 				bDeleteList.add(oldWord);
 				bAddList.add(newWord);
 			}
 		}
 		boundaryWords.removeAll(bDeleteList);
 		boundaryWords.addAll(bAddList);
 		
 		nouns = StringUtility.setSubtraction(nouns, NONS);
 		organs = StringUtility.setSubtraction(organs, NONS);
 		
 		// preprocessing 2
 		Set<String> tagSet = new HashSet<String>();
 		tagSet.addAll(Arrays.asList("Z O N M B".split(" ")));
 		properNouns = StringUtility.setSubtraction(properNouns, tagSet);
 		organs = StringUtility.setSubtraction(organs, tagSet);
 		nouns = StringUtility.setSubtraction(nouns, tagSet);
 		modifiers = StringUtility.setSubtraction(modifiers, tagSet);
 		boundaryWords = StringUtility.setSubtraction(boundaryWords, tagSet);
 		boundaryMarks = StringUtility.setSubtraction(boundaryMarks, tagSet);
 		
 		// insert tags
 		sentence = annotateSentenceHelper(sentence, properNouns, "Z", true);
 //		System.out.println(sentence);
 		sentence = annotateSentenceHelper(sentence, organs, "O", true);
 //		System.out.println(sentence);
 //		if (sentence.equals("<O>extent</O> of dermal cranial covering")) {
 //			System.out.println();
 //		}
 		sentence = annotateSentenceHelper(sentence, nouns, "N", true);
 //		System.out.println(sentence);
 		sentence = annotateSentenceHelper(sentence, modifiers, "M", true);
 		sentence = annotateSentenceHelper(sentence, boundaryWords, "B", true);
 		sentence = annotateSentenceHelper(sentence, boundaryMarks, "B", false);
 		
 		sentence = annotateSentenceHelper2(sentence);
 		
 		return sentence;
 	}
 	
 	
 	public String annotateSentenceHelper(String sentence, Set<String> words,
 			String tag, boolean isWithBoundaryWord) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.annotateSentence");
 		
 		if (words.size() != 0) {
 			if (isWithBoundaryWord) {
 				sentence = StringUtility.replaceAllBackreference(
 						sentence,
 						String.format("\\b(%s)\\b",
 								this.Iterable2Pattern(words)),
 						String.format("<%s>$1</%s>", tag, tag));
 			} else {
 //				String pattern = String.format("(%s)", LearnerUtility.Collection2Pattern(words));
 //				Matcher m1 = StringUtility.createMatcher("(\\]|\\}|\\(|\\)|\\{|\\[)", "word ]abc");
 //				boolean b1 = m1.find();
 ////				Matcher m2 = StringUtility.createMatcher("(]|}|(|)|{|[)", "word (abc)");
 ////				boolean b2 = m2.find();
 				
 				String regex = String.format("(%s)",
 						this.Iterable2Pattern(words));
 				String replacement = String.format("<%s>$1</%s>", tag, tag);
 				
 				myLogger.trace("Sentence: "+sentence);
 				myLogger.trace("Words: "+words);
 				myLogger.trace("Regex: "+regex);
 				myLogger.trace("Replacement: "+replacement);
 
 				sentence = StringUtility.replaceAllBackreference(sentence,
 						regex, replacement);
 			}
 		}
 
 		return sentence;
 	}
 	
 	public String annotateSentenceHelper2(String sentence){
 		if (StringUtility.createMatcher(sentence, "").find()) {
 			sentence = StringUtility.replaceAllBackreference(sentence, "<(\\w)>\\s*</$1>", "");
 		}
 		
 		Matcher m = StringUtility
 				.createMatcher(sentence, "<(\\w)>\\s*</(\\1)>");
 		while (m.find()) {
 			sentence = m.replaceFirst("");
 			m = StringUtility.createMatcher(sentence, "<(\\w)>\\s*</(\\1)>");
 		}
 		
 		sentence = StringUtility.replaceAllBackreference(sentence, 
 				"(?:<[^<]+>)+("+Constant.FORBIDDEN+")(?:</[^<]+>)+", "$1");
 		
 		return sentence;
 	}
 	
 	/**
 	 * 
 	 * @param mode
 	 *            can be either "singletag" or "multitags"
 	 */
     public KnownTagCollection getKnownTags(DataHolder dataholderHandler, String mode) {
     	PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.getKnownTags");
 		myLogger.trace("Enter (mode: "+mode+")");
 		
 		KnownTagCollection knownTags = null;
 		Set<String> nouns = new HashSet<String>(); // nouns
 		Set<String> organs = new HashSet<String>(); // organs
 		Set<String> modifiers = new HashSet<String>(); // modifiers
 		Set<String> boundaryWords = new HashSet<String>(); // boundary words
 		Set<String> boundaryMarks = new HashSet<String>(); // boundary marks
 		Set<String> properNouns = new HashSet<String>(); // proper nouns
 		
 		// get nouns
 		Set<String> nounSet = new HashSet<String>();
 		Set<String> psWordSet = new HashSet<String>(); // set of nouns
 		psWordSet = this.getPSWords(dataholderHandler);
 		nounSet.addAll(psWordSet);
 		// if the mode is "singletag", then get additional nouns from tags
 		if (StringUtils.equalsIgnoreCase(mode, "singletag")) {
 			nounSet.addAll(this.getOrgans(dataholderHandler));
 		} else {
 			// do nothing
 		}
 		nouns.addAll(nounSet);
 		myLogger.trace("Get nouns: "+nouns.toString());
 		
 		// get organs
 		if(StringUtils.equals(mode, "multitags")){
 			Set<String> organSet = this.getOrgans(dataholderHandler);
 			organs.addAll(organSet);
 			myLogger.trace("Get organs: "+organs.toString());
 		}
 		
 		// get modifiers
 		Set<String> modifierSet = new HashSet<String>();
 		modifierSet = this.getModifiers(dataholderHandler);
 		if(StringUtils.equals(mode, "singletag")){
 			Iterator<String> mIter = modifierSet.iterator();
 			while (mIter.hasNext()) {
 				String m = mIter.next();
 				if (!psWordSet.contains(m)) {
 					modifiers.add(m);
 				}
 			}
 		}else{
 			modifiers.addAll(modifierSet);
 		}
 		
 		// get boundary words and marks
 		List<Set<String>> result = this.getBoundaries(dataholderHandler);
 		boundaryWords = result.get(0);
 		boundaryMarks = result.get(1);
 		
 		// get proper nouns
 		properNouns = this.getProperNouns(dataholderHandler);
 		
 		// put all known tags into one KnownTagCollection object
 		knownTags = new KnownTagCollection(nouns, organs, modifiers, boundaryWords, boundaryMarks, properNouns);
 		
 		return knownTags;
 	}
     
 	/**
 	 * A helper of method getKnownTags(). Get a set of all nouns from the
 	 * word-POS collection.
 	 * 
 	 * @return a set of nouns
 	 */
 	public Set<String> getPSWords(DataHolder dataholderHandler) {
 		Set<String> psSet = new HashSet<String>(); // set of p and s
 		// get a set of all nouns from the word-POS collection
 		Iterator<Entry<WordPOSKey, WordPOSValue>> iterWordPOS = dataholderHandler
 				.getWordPOSHolder().entrySet().iterator();
 		while (iterWordPOS.hasNext()) {
 			Entry<WordPOSKey, WordPOSValue> entry = iterWordPOS.next();
 			String POS = entry.getKey().getPOS();
 			if ((StringUtils.equals(POS, "s"))
 					|| (StringUtils.equals(POS, "p"))) {
 				String word = entry.getKey().getWord();
 				if (word != null) {
 					if (StringUtility.createMatcher(word, "^[a-zA-Z0-9_-]+$")
 							.find()) {
 						psSet.add(word);
 					}
 				}
 			}
 		}
 
 		return psSet;
 	}
 	
 	/**
 	 * A helper of method getKnownTags(). Get a set of o from tags in sentence
 	 * collections
 	 * 
 	 * @return a set of o
 	 */
 	public Set<String> getOrgans(DataHolder dataholderHandler) {
 		Set<String> oSet = new HashSet<String>(); // set of organs
 		
 		Iterator<SentenceStructure> iterSentence = dataholderHandler
 				.getSentenceHolder().iterator();
 		while (iterSentence.hasNext()) {
 			SentenceStructure sentence = iterSentence.next();
 			String tag = sentence.getTag();
 
 			if (tag != null) {
 				if ((!StringUtils.equals(tag, "ignore"))
 						&& (!StringUtility.createMatcher(tag, ".* .*").find()) 
 						&& (!StringUtility.createMatcher(tag, ".*\\[.*").find())) {
 					if (StringUtility.createMatcher(tag, "^[a-zA-Z0-9_-]+$").find()) {
 						oSet.add(tag);
 					}
 				}
 			}
 		}
 		
 		return oSet;
 	}
 	
 	/**
 	 * Get modifier words from modifier collection.
 	 * 
 	 * @return a set fo modifer words
 	 */
 	public Set<String> getModifiers(DataHolder dataholderHandler) {
 		Set<String> mSet = new HashSet<String>(); // set of o
 		
 		Iterator<Entry<String, ModifierTableValue>> iter = dataholderHandler
 				.getModifierHolder().entrySet().iterator();
 		while (iter.hasNext()) {
 			Entry<String, ModifierTableValue> entry = iter.next();
 			String word = entry.getKey();
 			if (word != null) {
 				if (StringUtility.createMatcher(word, "^[a-zA-Z0-9_-]+$")
 						.find()) {
 					mSet.add(word);
 				}
 			}
 		}
 		
 		return mSet;
 	}
 	
 	/**
 	 * Get boundary words and marks.
 	 * 
 	 * @return a list of two elements. The first element is a set of boundary
 	 *         words, and second element is a set of boundary marks.
 	 */
     public List<Set<String>> getBoundaries (DataHolder dataholderHandler){
     	Set<String> bWords = new HashSet<String>();
     	Set<String> bMarks = new HashSet<String>();
     	List<Set<String>> result = new LinkedList<Set<String>>();
     	
 		Iterator<Entry<WordPOSKey, WordPOSValue>> iter = dataholderHandler
 				.getWordPOSHolderIterator();
     	while (iter.hasNext()) {
     		Entry<WordPOSKey, WordPOSValue> entry = iter.next();
     		String word = entry.getKey().getWord();
     		String POS = entry.getKey().getPOS();
 
 			if (word != null && POS != null) {
 				if (StringUtils.equals(POS, "b")) {
 //					String pattern = "^[-\\\\\\(\\)\\[\\]\\{\\}\\.\\|\\+\\*\\?]$";
 					String pattern = "^(-|\\\\|\\(|\\)|\\[|\\]|\\{|\\}|\\.|\\||\\+|\\*|\\?)$";
 					if (StringUtility.isMatchedNullSafe(word, pattern)) {
 						bMarks.add(word);
 					} else if ((!(StringUtility.isMatchedNullSafe(word, "\\w"))) && (!StringUtils.equals(word, "/"))) {
 						if (StringUtility.createMatcher(word, "^[a-zA-Z0-9_-]+$").find()) {
 							bMarks.add(word);
 						}
 					} else {
 						if (StringUtility.isMatchedNullSafe(word, "^[a-zA-Z0-9_-]+$")) {
 							bWords.add(word);
 						}
 					}
 				}
 			}
 		}
 
     	result.add(bWords);
     	result.add(bMarks);
     	
     	return result;
     }
     
 	/**
 	 * Get the proper nouns from the word-POS collection
 	 * 
 	 * @return a set of the porper nouns
 	 */
 	public Set<String> getProperNouns(DataHolder dataholderHandler) {
 		Set<String> pNouns = new HashSet<String>();
 		
 		Iterator<Entry<WordPOSKey, WordPOSValue>> iter = dataholderHandler.getWordPOSHolder().entrySet().iterator();
 		
 		while (iter.hasNext()) {
 			Entry<WordPOSKey, WordPOSValue> entry = iter.next();
 			String word = entry.getKey().getWord();
 			String POS = entry.getKey().getPOS();
 			
 			if (StringUtils.equals(POS, "z")) {
 				if (StringUtility.createMatcher(word, "^[a-zA-Z0-9_-]+$").find()) {
 					pNouns.add(word);
 				}
 			}
 		}
 		
 		return pNouns;
 	}
 
 //	/**
 //	 * @param args
 //	 */
 //	public static void main(String[] args) {
 ////		[-\\\\\\(\\)\\[\\]\\{\\}\\.\\|\\+\\*\\?]
 ////		
 ////		stops.addAll(Arrays.asList(new String[] { "NUM", "(", "[", "{",
 ////				")", "]", "}", "d+" }));
 //		testRunner("z", "abczabc");
 //		testRunner("/", "abc/abc");
 //		testRunner("-", "abc-abc");
 //		testRunner("_", "abc_abc");
 //		testRunner(addDoubleBackslash("\\"), "abc\\abc");
 //		testRunner(addDoubleBackslash("("), "abc(abc");
 //		testRunner(addDoubleBackslash(")"), "abc)abc");
 //		testRunner(addDoubleBackslash("["), "abc[abc");
 //		testRunner(addDoubleBackslash("]"), "abc]abc");
 //		testRunner(addDoubleBackslash("{"), "abc{abc");
 //		testRunner(addDoubleBackslash("}"), "abc}abc");
 //		testRunner(addDoubleBackslash("."), "abc.abc");
 //		testRunner(addDoubleBackslash("|"), "abc|abc");
 //		testRunner(addDoubleBackslash("+"), "abc+abc");
 //		testRunner(addDoubleBackslash("*"), "abc*abc");
 //		testRunner(addDoubleBackslash("?"), "abc?abc");
 //		testRunner(addDoubleBackslash("d+"), "01138");
 ////		testRunner("\\(", "abc(abc");
 ////		testRunner("\\(", "abc(abc");
 ////		testRunner("\\(", "abc(abc");
 //		
 //		String str = "(";
 //		str = str.replaceAll("(\\()", "\\\\$1");
 //		System.out.println(str);
 //		
 //		str = addDoubleBackslash(str);
 //
 //
 //	}
 	private String addDoubleBackslash(String word) {
 		word = word.replaceAll("^(\\\\|\\(|\\)|\\[|\\]|\\{|\\}|\\.|\\||\\+|\\*|\\?|d\\+)$", "\\\\$1");
 //		word = word.replaceAll("^(d\\+)$", "\\\\$1");
 		
 		return word;
 	}
 	
 //	private static String addDoubleBackslash(String word) {
 //		word = word.replaceAll("^(\\\\|\\(|\\)|\\[|\\]|\\{|\\}|\\.|\\||\\+|\\*|\\?|d\\+)$", "\\\\$1");
 ////		word = word.replaceAll("^(d\\+)$", "\\\\$1");
 //		
 //		return word;
 //	}
 
 	private static boolean testRunner(String regex, String str) {
 		boolean isMatched = false;
 		
 		Pattern p = Pattern.compile(regex);
 		Matcher m = p.matcher(str);
 		
 		isMatched = m.find();
 		
 		System.out.println(isMatched);
 		
 		return isMatched;
 	}
 	
 	public String getSentencePtn(DataHolder dataholderHandler, Set<String> token, int limit, List<String> words) {
 		Set<String> typeModifierPtns = dataholderHandler.getTypeModifierPattern();
 		String ptn = "";
 		
 		int counter = 0;
 		String regex = String.format("\\b(%s)\\b",StringUtils.join(token, "|"));
 		
 		Iterator<String> wordIter = words.iterator();
 		while (wordIter.hasNext()) {
 			if (counter > limit - 1) {
 				break;
 			}
 			counter++;
 			String word = wordIter.next();
//			if (token.contains(word)) {
 			
 			if (StringUtility.isEntireMatchedNullSafe(word, regex))	{
 				ptn = ptn + "&";
 			}
 			else {
 				if (word == null) {
 					ptn = ptn + "q";
 				}
 				else {
 					Matcher m1 = StringUtility.createMatcher(word, "([,:;\\.])");
 					Matcher m2 = StringUtility.createMatcher(word, "<(\\w)>");
 					if (m1.find()) {
 						String g1 = m1.group(1);
 						ptn = ptn + g1;
 					}
 					else if (m2.find()){
 						String g1 = m2.group(1);
 						String tag = g1;
 						if (StringUtils.equals(tag, "M") && typeModifierPtns.contains(word)) {
 							ptn = ptn + "t";
 						}
 						else {
 							ptn = ptn + tag.toLowerCase();
 						}
 					}
 					else if (StringUtils.equals(this.getWordFormUtility().getNumber(word), "p")) {
 						ptn = ptn + "p";
 					}
 					else {
 						ptn = ptn + "q";
 					}
 				}
 			}
 		}
 		
 		return ptn;
 	}
 	
 }
