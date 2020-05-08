 package application;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
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
 	
 	/* constants for the start index of each feature */
 	private final static int WORD_INDEX = 1;
 	private final static int SIZE_WORD_DIC = Processor.dictionary.size();
 	private final static int NUM_WORDS_INDEX = WORD_INDEX + SIZE_WORD_DIC;
 	private final static int NUM_STOP_WORDS_INDEX = NUM_WORDS_INDEX + 1;
 	private final static int NUM_CAP_WORDS_INDEX = NUM_STOP_WORDS_INDEX + 1;
 	private final static int NUM_PUNC_INDEX = NUM_CAP_WORDS_INDEX + 1;
 	private final static int NUM_NPS_BTW = NUM_PUNC_INDEX + 1;
 	private final static int ENTITY_E1_INDEX = NUM_NPS_BTW + 1;
 	private final static int SIZE_NER_DIC = Processor.nerDictionary.size();
 	private final static int ENTITY_E2_INDEX = ENTITY_E1_INDEX + SIZE_NER_DIC;
 	private final static int POS_LEFT_E1_INDEX = ENTITY_E2_INDEX + SIZE_NER_DIC;
 	private final static int SIZE_POS_DIC = Processor.POSDictionary.size();
 	private final static int POS_RIGHT_E2_INDEX = POS_LEFT_E1_INDEX + SIZE_POS_DIC;
 	private final static int POS_SEQUENCE_INDEX = POS_RIGHT_E2_INDEX + SIZE_POS_DIC;
 	
 	
 	private Tree e1;
 	private Tree e2;
 	private Tree headE1;
 	private Tree headE2;
 	private Integer interval;
 	private String sentence;
 	private String taggedSentence;
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
 
 	/* POS to the left of e1 */
 	private Integer POSlefte1;
 	
 	/* POS to the right of e2 */
 	private Integer POSrighte2;
 	
 	/* number of phrases in between */
 	private int numPhrasesBts;
 	
 	/* entity type of e1 */
 	private int entityType1;
 	
 	/* entity type of e2 */
 	private int entityType2;
 	
 	public Feature(Tree e1, Tree e2, Tree headE1, Tree headE2, Integer interval, String sentence) {
 		this.e1 = e1;
 		this.e2 = e2;
 		this.headE1 = headE1;
 		this.headE2 = headE2;
 		this.numPhrasesBts = interval - 1;
 		this.sentence = sentence;
 		e1leaves = (ArrayList<Tree>) e1.getLeaves();
 		e2leaves = (ArrayList<Tree>) e2.getLeaves();
 		words = new HashMap<Integer, Integer>();
 		posDic = new HashMap<String, String>();
 		buildFeatures();
 	}
 
 	/**
 	 * Populate each feature
 	 */
 	private void buildFeatures() {
 		setEdgeWords();
 		setPOSFeatures();
 		setWords();
 		setEntityTypes();
 		System.out.println(this);
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
 		taggedSentence = Processor.tagger.tagSentence(sentence);
 		System.out.println(taggedSentence);
 		
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
 		StringTokenizer st = new StringTokenizer(taggedSentence);
 		// control whether to start/stop generating the sequence
 		boolean recordFlag = false;
 		String tempSequence = "";
 		int startIndex = 0, counter = 0;
 		while (st.hasMoreTokens()) {
 			counter++;
 			String currWord = st.nextToken();
 			StringTokenizer subSt = new StringTokenizer(currWord, "_");
 			String word = subSt.nextToken();
 			
 			if (word.equals(leftWordE2) && (counter - startIndex) >= 2 * numPhrasesBts) {
 				recordFlag = false;
 				startIndex = counter;
 			}
 			
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
 		if (!tempSequence.equals("")) {
 			if (!Processor.POSSequenceDictonary.containsKey(tempSequence)) {
 				Processor.POSSequenceDictonary.put(tempSequence, lastIndex + 1);
 			}
 			POSSequence  = Processor.POSSequenceDictonary.get(tempSequence);
 		}
 		System.out.println("temp sequence: " + tempSequence);
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
 
 		int startIndex = 0, counter = 0;
 		StringTokenizer st = new StringTokenizer(taggedSentence);
 		while (st.hasMoreTokens()) {
 			counter++;
 			String currWord = st.nextToken();
 			StringTokenizer subSt = new StringTokenizer(currWord, "_");
 			String word = subSt.nextToken();
 
 			String tag = posDic.get(word);
 			String canonicalWord = word;
 			if (tag != null && tag.matches("NN.*"))
 				canonicalWord = fromPlural(word).toLowerCase();
 			
 			// if encounter startWord, start adding words
 			if (word.equals(rightWordE1)) {
 				addFlag = true;
 				startIndex =counter;
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
 			if (Processor.dictionary.containsKey(word)) {
 				wordIndex = Processor.dictionary.get(word);
 			} else if (Processor.dictionary.containsKey(canonicalWord)) {
 				wordIndex = Processor.dictionary.get(canonicalWord);
 			} else {
 				wordIndex = null;
 			}
 			
 			if (wordIndex != null) {
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
 			if (word.equals(leftWordE2) && (counter - startIndex) > 2 * numPhrasesBts) {
 				addFlag = false;
 			}			
 		}
 	}
 	
 	/**
 	 * Set entity type features of head of e1 and e2
 	 */
 	private void setEntityTypes() {
 		String nerSentence = Processor.ner.runNER(sentence);
 		System.out.println(nerSentence);
 		StringTokenizer st = new StringTokenizer(nerSentence);
 		// walk through each token(words with ner), and find the name entity of heade1 and heade2
 		while (st.hasMoreTokens()) {
 			String currWord = st.nextToken();
 			StringTokenizer subSt = new StringTokenizer(currWord, "/");
 			String word = subSt.nextToken();
 			// remove last ","
 			String ner = removeLastPunc(subSt.nextToken());
 			
 			if (word.equals(headE1.label().value())) {
 				if (Processor.nerDictionary.containsKey(ner))
 					entityType1 = Processor.nerDictionary.get(ner);
 			}
 			
 			if (word.equals(headE2.label().value())) {
 				if (Processor.nerDictionary.containsKey(ner)) 
 					entityType2 = Processor.nerDictionary.get(ner);
 			}
 		}
 	}
 	
 	/**
 	 * return feature vector of current relation
 	 * the total feature vector consists of
 	 * words | num. of words | num. of stop words | num. of cap words | num. of punc. |
 	 * num. of NPs btw. | entity type of e1 | entity type of e2 | 
 	 * pos of left to e1 | pos of right to e2 | pos sequence
 	 * among these, words, POS sequence, pos of left to e1 and pos of right to e2 and also 
 	 * entity type features consists of multiple columns (high dimension) 
 	 * For sake of saving space, we store the features in format of position->value (where 
 	 * value != 0). For instance, for words features, according to our word dictionary, 
 	 * we may have word "economy" and "at" in format of {1151:1 5875:1}, where key corresponds to
 	 * the line where the word set in dictionary, and value represents frequency of the word
 	 * in the feature vector, we save this feature as "1151 1 5875 1".
 	 * For convenience of matlab processing 
 	 * same logic will apply for pos sequence, pos of left/right and also entity type
 	 * @return String string format of the feature vector
 	 */
 	public String getFeatureVector() {
 		StringBuffer featureVector = new StringBuffer();
 		// declare set of start point of each feature in feature vector
 		// compose words feature
 	    Iterator it = words.entrySet().iterator();
 	    while (it.hasNext()) {
 	        Map.Entry pair = (Map.Entry)it.next();
 	        Integer key = (Integer) pair.getKey();
 	        Integer value = (Integer) pair.getValue();
 	        featureVector.append(key + ":" + value + " ");
 	    }
 	    
 	    // compose num. of words feature
 	    featureVector.append(NUM_WORDS_INDEX + ":" + numWordsBtw + " ");
 	    
 	    // compose num. of stop words feature
 	    featureVector.append(NUM_STOP_WORDS_INDEX + ":" + numStopWords + " ");
 	    
 	    // compose num. of cap words
 	    featureVector.append(NUM_CAP_WORDS_INDEX + ":" + numCapWords + " ");
 	    
 	    // compose num. of punctuation
 	    featureVector.append(NUM_PUNC_INDEX + ":" + numPuncsBtw + " ");
 	    
 	    // compose num. of nps btw
 	    featureVector.append(NUM_NPS_BTW + ":" + numPhrasesBts + " ");
 	    
 	    // compose entity types
 	    if (entityType1 != 0)
 	    	featureVector.append((ENTITY_E1_INDEX + entityType1 - 1) + ":1 ");
 	    if (entityType2 != 0)
 	    	featureVector.append((ENTITY_E2_INDEX + entityType2 - 1) + ":1 ");
 	    
 	    // compose pos tags
 	    if (POSlefte1 != 0)
 	    	featureVector.append((POS_LEFT_E1_INDEX + POSlefte1 - 1) + ":1 ");
 	    if (POSrighte2 != 0)
 	    	featureVector.append((POS_RIGHT_E2_INDEX + POSrighte2 - 1) + ":1 ");
 	    
 	    // compose pos sequence
	    if (POSSequence != 0)
 	    	featureVector.append((POS_SEQUENCE_INDEX + POSSequence - 1) + ":1\n");
 	    
 //	    System.out.println(featureVector);
 	    return featureVector.toString();
 	}
 	
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("original sentence: " + sentence + "\n");
 		sb.append("e1: " + e1.toString() + "\n");
 		sb.append("e2: " + e2.toString() + "\n");
 		sb.append("words between: " + words.toString() + "\n");
 		sb.append("number of words: " + numWordsBtw + "\n");
 		sb.append("number of stop words: " + numStopWords + "\n");
 		sb.append("number of cap words: " + numCapWords + "\n");
 		sb.append("number of punctuations: " + numPuncsBtw + "\n");
 		sb.append("number of phrases: " + numPhrasesBts + "\n");
 		sb.append("Entity type e1: " + entityType1 + "\n");
 		sb.append("Entity type e2: " + entityType2 + "\n");
 		sb.append("POS left e1: " + POSlefte1 + "\n");
 		sb.append("POS right e2: " + POSrighte2 + "\n");
 		sb.append("POS sequence: " + POSSequence + "\n");
 		return sb.toString();
 	}
 	
 	
 	
 	/* Helper functions */
 	/** 
 	 * Returns the non-plural form of a plural noun like: cars -> car, 
 	 * children -> child, people -> person, etc. 
 	 * @param str 
 	 * @return the non-plural form of a plural noun
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
 	private String removeLastPunc(String str) {
 		String lastChar = str.substring(str.length() - 1, str.length());
 		if (lastChar.matches("[^A-Za-z0-9]")) 
 			return str.substring(0, str.toLowerCase().lastIndexOf(lastChar));
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
