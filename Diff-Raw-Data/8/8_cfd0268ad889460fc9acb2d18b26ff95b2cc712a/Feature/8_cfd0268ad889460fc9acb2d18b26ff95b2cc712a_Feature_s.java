  
 import java.text.BreakIterator;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.StringTokenizer;
 
 import edu.stanford.nlp.trees.*;
 
 /**
  * Define feature object that describe
  * features of a relation, which will be further used 
  * to compose feature vector for training 
  * Since we're using low-level signals/features to build 
  * relation-independent model, we will not use parser here.
  * And we'll only use POS and phase chunker to build features
  */
 
 public class Feature {
 	
 	private Tree e1;
 	private Tree e2;
 	private String sentence;
 	private ArrayList<Tree> e1leaves;
 	private ArrayList<Tree> e2leaves;
 	
 	/* edge words */
 	private String leftWordE1;
 	private String rightWordE1;
 	private String leftWordE2;
 	private String rightWordE2;
 	
 	/* words and its frequency between(include) two NP */
 	private HashMap<Integer, Integer> words;
 	
 	/* pos dictionary, key: word value: POS tag */
 	private HashMap<String, String> posDic;
 	
 	/* number of words between two NP, including
 	 * last word of e1 and first word of e2 */
 	private int numWordsBtw;
 	
 	/* number of stop words in between */
 	private int numStopWords;
 	
 	/* number of Capitalization words in between 
 	 * here, capitalization means the first char is capitalized*/
 	private int numCapWords;
 	
 	/* number of punctuations in between */
 	private int numPuncsBtw;
 		
 	/* presence of POS sequence */
 	private int POSSequence;
 	
 	/* number of phrases in between */
 	private int numPhrasesBts;
 
 	/* POS to the left of e1 */
 	private Integer POSlefte1;
 	
 	/* POS to the right of e2 */
 	private Integer POSrighte2;
 	
 	/* entity type of e1 */
 	private int entityType1;
 	
 	/* entity type of e2 */
 	private int entityType2;
 	
 	public Feature(Tree e1, Tree e2, String sentence) {
 		this.e1 = e1;
 		this.e2 = e2;
 		this.sentence = sentence;
 		e1leaves = (ArrayList<Tree>) e1.getLeaves();
 		e2leaves = (ArrayList<Tree>) e2.getLeaves();
 		words = new HashMap<Integer, Integer>();
 		posDic = new HashMap<String, String>();
		// TODO: delete
 		this.sentence = "Jaguar, the luxury auto maker Sold 1,214 cars in the U.S.A.";
 		buildFeatures();
 	}
 
 	/**
 	 * Populate each feature
 	 */
 	private void buildFeatures() {
 		setEdgeWords();
 		setPOSFeatures();
 		setWords();
 		setNumPhraseBtw();
 		setEntityTypes();
 	}
 	
 	/**
 	 * Set edge words of E1 and E2
 	 * namely, leftWordE1, rightWordE1
 	 * and leftWordE2, rightWordE2
 	 */
 	private void setEdgeWords() {
 		leftWordE1 = e1leaves.get(0).label().value();
 		rightWordE1 = e1leaves.get(e1leaves.size() - 1).label().value();
 		leftWordE2 = e2leaves.get(0).label().value();
 		rightWordE2 = e2leaves.get(e2leaves.size() - 1).label().value();
 	}
 	
 	/**
 	 * This function tag the POS of each word in the sentence
 	 * and generate feature of POS sequence between e1 and e2
 	 * and also POS of the word before e1 and POS of the word after e2
 	 * We have normal POS tags pre-stored in the dictionary. 
 	 * While for the feature of POS sequence, we will build the dictionary along with each
 	 * new sequence we have encountered
 	 */
 	private void setPOSFeatures() {
 		// build POS arrayList during which process generate POS features
 		String taggedSentence = Processor.tagger.tagSentence(sentence);
 
 		// get the left word of e1 and right word of e2
 		leftWordE1 = e1leaves.get(0).label().value();
 		rightWordE2 = e2leaves.get(e2leaves.size() - 1).label().value();
 		
		// debug use
 		leftWordE1 = "the";
 		rightWordE2 = "U.S.A.";
 		rightWordE1 = "maker";
 		leftWordE2 = "the";
 		
 		/* Though we can get the pos features in one pass of the tagged sentence,
 		 * we avoid doing that for simplicity and readability of the code 
 		 */
 		setPOSEdgeWords(taggedSentence);
 		setPOSSequence(taggedSentence);
 	}
 
 	/**
 	 * Set the POS of word to left of E1 and POS of word to the right of E2
 	 * @param taggedSentence
 	 */
 	private void setPOSEdgeWords(String taggedSentence) {
 		// store left word of e1 and right word of e2 
 		String leftToE1 = "", rightToE2 = "";
 		// flags avoid repeated assignment to lefttoe1 or righttoe2
 		boolean flagLeftToE1 = false, flagRightToE2 = false;
 		String previouWord = "";
 		
 		// walk through taggedSentence, build posDic and also find leftToE1 and rightToE2
 		StringTokenizer st = new StringTokenizer(taggedSentence);
 		while (st.hasMoreTokens()) {
 			String currWord = st.nextToken();
 			// further tokenize each tagged words into original word and its tag
 			StringTokenizer subSt = new StringTokenizer(currWord, "_");
 			String word = subSt.nextToken();
 			String tag = subSt.nextToken();
 			
 			// we do not store punctuation in the posDic
 			if (word.matches("[^A-Za-z0-9]")) 
 				continue;
 			
 			// if current words is leftword of e1, then lefttoe1 is previousWord
 			if (word.equals(leftWordE1) && !flagLeftToE1) { 
 				leftToE1 = previouWord;
 				flagLeftToE1 = true;
 			}
 			// if previousWord is rightword of e2, then righttoe2 is word
 			if (previouWord.equals(rightWordE2) && !flagRightToE2) { 
 				rightToE2 = word;
 				flagRightToE2 = true;
 			}
 			previouWord = word;
 			posDic.put(word, tag);
 		}
 		
 		// set POS of lefttoE1 and righttoE2
 		if ((POSlefte1 = Processor.POSDictionary.get(posDic.get(leftToE1))) == null) 
 			POSlefte1 = 0;
 		if ((POSrighte2 = Processor.POSDictionary.get(posDic.get(rightToE2))) == null) 
 			POSrighte2 = 0;
 	}
 	
 	/**
 	 * Set pos sequene between e1 and e2
 	 * we build pos sequence along the way of constructing the feature
 	 * this is different from other features where we have dictionary in advance
 	 * because there're too much permutations of pos sequence and we do not want to
 	 * generate them all at once. The tradeoff is that we may loose the effect of features
 	 * to some extend
 	 * @param taggedSentence
 	 */
 	private void setPOSSequence(String taggedSentence) {
 		System.out.println(taggedSentence);
 		StringTokenizer st = new StringTokenizer(taggedSentence);
 		// control whether to start/stop generating the sequence
 		boolean recordFlag = false;
 		String tempSequence = "";
 		while (st.hasMoreTokens()) {
 			String currWord = st.nextToken();
 			StringTokenizer subSt = new StringTokenizer(currWord, "_");
 			String word = subSt.nextToken();
 			
 			if (word.equals(leftWordE2))
 				recordFlag = false;
 			
 			if (recordFlag) {
 				String tag = posDic.get(word);
 				if (tag != null) {
 					tempSequence += tag + "-";
 				}
 			}
 			
 			if (word.equals(rightWordE1)) 
 				recordFlag = true;
 		}
 		// we build POS sequence dictionary
 		Integer lastIndex = Processor.POSSequenceDictonary.size();
 		if (!Processor.POSSequenceDictonary.containsKey(tempSequence)) {
 			Processor.POSSequenceDictonary.put(tempSequence, lastIndex + 1);
 		}
		System.out.println(Processor.POSSequenceDictonary);
 	}
 
 	/**
 	 * Set word list between two NPs e1 and e2, 
 	 * count number of words, stop words, punctuation, capital words in between
 	 * TODO: should not consider verb or nouns as in the thesis? currently, we include those
 	 * using the Java default breakIterator
 	 */
 	private void setWords() {
 		Integer wordIndex;
 		boolean addFlag = false; // control whether to add flag to words
 		
 		// set last word in e1 as start word, and first word in e2 as begin word
 		rightWordE1 = removeLastPoint(rightWordE1);
 		leftWordE2 = removeLastPoint(leftWordE2);
 		
 		// debug usage
 		rightWordE1 = "maker";
 		leftWordE2 = "U.S.A";
 		
 		BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
 		iterator.setText(sentence);
 		int start = iterator.first();
 		for (int end = iterator.next();
 				end != BreakIterator.DONE;
 				start = end, end = iterator.next()) {
 			
 			// get word and convert from plural form if it is
 			String word = sentence.substring(start, end);
 			String tag = posDic.get(word);
 			if (tag != null && tag.matches("NN.*"))
 				word = fromPlural(word);
 			
 			String canonicalWord = word.toLowerCase();
 			// if encounter startWord, start adding words
 			if (word.equals(rightWordE1)) {
 				addFlag = true;
 			}
 			// eliminate empty word
 			if (word.isEmpty() || word.equals(" ") || word.equals("") || !addFlag) 
 				continue;
 			
 			// detect whether current word is punctuation
 			if (word.matches("[^A-Za-z0-9]")) {
 				this.numPuncsBtw++;
 				continue;
 			}
 			
 			// detect whether it's stop word
 			if (Processor.stopWordDictionary.get(canonicalWord) != null) {
 				this.numStopWords++;
 			}
 			
 			// only add word if it's a valid word in dictionary
 			// we also consider stop words here
 			if ((wordIndex = Processor.dictionary.get(canonicalWord)) != null) {
 				// if it's valid word, detect whether it's capitalized word
 				if (Character.isUpperCase(word.charAt(0))) {
 					this.numCapWords++;
 				}
 				if (words.get(wordIndex) != null) {
 					words.put(wordIndex, words.get(wordIndex) + 1);
 				} else {
 					words.put(wordIndex, 1);
 				}
 				this.numWordsBtw++;
 			}
 			
 			// if read end word, end adding words
 			if (word.equals(leftWordE2)){
 				addFlag = false;
 			}
 			
 		}
 		System.out.println(this);
 	}
 	
 	private void setNumPhraseBtw() {
 		
 	}
 	
 	private void setEntityTypes() {
 		
 	}
 	
 	public HashMap<Integer, Integer> getWords() {
 		return words;
 	}
 	
 	public int getNumWordsBtw() {
 		return numWordsBtw;
 	}
 	
 	public int getNumStopWords() {
 		return numStopWords;
 	}
 	
 	public int getNumPuncsBtw() {
 		return numPuncsBtw;
 	}
 	
 	public int getNumCapBtw() {
 		return numCapWords;
 	}
 	
 	public int getNumPhrasesBtw() {
 		return numPhrasesBts;
 	}
 	
 	public int getPOSSequence() {
 		return POSSequence;
 	}
 
 	public Integer getPOSlefte1() {
 		return POSlefte1;
 	}
 
 	public Integer getPOSrighte2() {
 		return POSrighte2;
 	}
 	
 	public int getEntityType1() {
 		return entityType1;
 	}
 	
 	public int getEntityType2() {
 		return entityType2;
 	}
 	
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("original sentence: " + sentence + "\n");
 		sb.append("e1: " + e1.toString() + "\n");
 		sb.append("e2: " + e2.toString() + "\n");
 		sb.append("words between: " + words.toString() + "\n");
 		sb.append("number of words: " + numWordsBtw + "\n");
 		sb.append("number of stop words: " + numStopWords + "\n");
 		sb.append("number of punctuations: " + numPuncsBtw + "\n");
 		sb.append("number of cap words: " + numCapWords + "\n");
 		return sb.toString();
 	}
 	
 	
 	
 	/* Helper functions */
 	/** 
 	 * Returns the non-plural form of a plural noun like: cars -> car, children -> child, people -> person, etc. 
 	 * @param str 
 	 * @return the non-plural form of a plural noun like: cars -> car, children -> child, people -> person, etc. 
 	 */  
 	private String fromPlural(String str)  
 	{  
 		if(str.toLowerCase().endsWith("es") && ! shouldEndWithE(str)) 
 			return str.substring(0, str.toLowerCase().lastIndexOf("es"));  
 		else if(str.toLowerCase().endsWith("s")) 
 			return str.substring(0, str.toLowerCase().lastIndexOf('s'));  
 		else if(str.toLowerCase().endsWith("children")) 
 			return str.substring(0, str.toLowerCase().lastIndexOf("ren"));  
 		else if(str.toLowerCase().endsWith("people")) 
 			return str.substring(0, str.toLowerCase().lastIndexOf("ople")) + "rson"; 
 		else return str;  
 	}  
 	
 	/**
 	 * This function is used to remove last point of the words in case 
 	 * Java built in BreakIterator can match with the parser
 	 * Say, in BreakIterator, it will return the word "U.S.A"
 	 * while in parse, it will return the word "U.S.A". We need to close the gap.
 	 * @param str
 	 * @return string after eliminating last point if it contains
 	 */
 	private String removeLastPoint(String str) {
 		if(str.toLowerCase().endsWith(".")) 
 			return str.substring(0, str.toLowerCase().lastIndexOf("."));
 		else return str;
 	}
 
 	/** 
 	 * Determine whether it's plural noun
 	 * @param str 
 	 * @return true is the singular form of a word should end with the letter "e" 
 	 */  
 	private boolean shouldEndWithE(String str)  
 	{  
 		return str.toLowerCase().endsWith("iece")  
 				|| str.toLowerCase().endsWith("ice")  
 				|| str.toLowerCase().endsWith("ace")  
 				|| str.toLowerCase().endsWith("ise");  
 	}  
 	
 	
 }
