 package semanticMarkup.ling.learn;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import semanticMarkup.know.lib.WordNetPOSKnowledgeBase;
 import semanticMarkup.ling.Token;
 import semanticMarkup.ling.learn.dataholder.DataHolder;
 import semanticMarkup.ling.learn.utility.LearnerUtility;
 import semanticMarkup.ling.transform.ITokenizer;
 import semanticMarkup.ling.transform.lib.OpenNLPSentencesTokenizer;
 import semanticMarkup.ling.transform.lib.OpenNLPTokenizer;
 
 
 public class LearnerUtilityTest {
 	
 	private LearnerUtility tester;
 
 	@Before
 	public void initialize() {
 		Configuration myConfiguration = new Configuration();
 		ITokenizer sentenceDetector = new OpenNLPSentencesTokenizer(
 				myConfiguration.getOpenNLPSentenceDetectorDir());
 		ITokenizer tokenizer = new OpenNLPTokenizer(myConfiguration.getOpenNLPTokenizerDir());
 		
 		WordNetPOSKnowledgeBase wordNetPOSKnowledgeBase = null;
 		try {
 			wordNetPOSKnowledgeBase = new WordNetPOSKnowledgeBase(myConfiguration.getWordNetDictDir(), false);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		
 		this.tester = new LearnerUtility(sentenceDetector, tokenizer, wordNetPOSKnowledgeBase);
 	}
 	
 	// populate sentence utilities
 	@Test
 	public void testGetType() {
 		assertEquals("PopulateSent Helper - getType: character", 1,
 				tester.getType("Brazeau_2009.xml_states737.txt"));
 		assertEquals("PopulateSent Helper - getType: description", 2,
 				tester.getType("Brazeau_2009.xml_states737_state739.txt"));
 		assertEquals("PopulateSent Helper - getType: otherwise", 0,
 				tester.getType("saf_saiflkds)dsljf_fls.txt"));
 	}
 
 	@Test
 	public void testHideMarksInBrackets() {
 		assertEquals("Result", null, tester.hideMarksInBrackets(null));
 		assertEquals("Result", "", tester.hideMarksInBrackets(""));
 		assertEquals("Result", "before (word[DOT]  word) after",
 				tester.hideMarksInBrackets("before (word. word) after"));
 		assertEquals("Result", "before (word[QST]  word) after",
 				tester.hideMarksInBrackets("before (word? word) after"));
 		assertEquals("Result", "before (word[SQL]  word) after",
 				tester.hideMarksInBrackets("before (word; word) after"));
 		assertEquals("Result", "before (word[QLN]  word) after",
 				tester.hideMarksInBrackets("before (word: word) after"));
 		assertEquals("Result", "before (word[EXM]  word) after",
 				tester.hideMarksInBrackets("before (word! word) after"));
 	}
 
 	@Test
 	public void testRestoreMarksInBrackets() {
 		assertEquals("Result", null, tester.restoreMarksInBrackets(null));
 		assertEquals("Result", "", tester.restoreMarksInBrackets(""));
 		assertEquals("Result", "before (word.  word) after",
 				tester.restoreMarksInBrackets("before (word[DOT]  word) after"));
 		assertEquals("Result", "before (word?  word) after",
 				tester.restoreMarksInBrackets("before (word[QST]  word) after"));
 		assertEquals("Result", "before (word;  word) after",
 				tester.restoreMarksInBrackets("before (word[SQL]  word) after"));
 		assertEquals("Result", "before (word:  word) after",
 				tester.restoreMarksInBrackets("before (word[QLN]  word) after"));
 		assertEquals("Result", "before (word!  word) after",
 				tester.restoreMarksInBrackets("before (word[EXM]  word) after"));
 	}
 	
     
 	@Test
 	public void testGetFirstNWords() {
 		List<String> nWords = new ArrayList<String>();
 		assertEquals("PopulateSent Helper - getFirstNWords: none", nWords,
 				tester.getFirstNWords(null, -1));
 		assertEquals("PopulateSent Helper - getFirstNWords: none", nWords,
 				tester.getFirstNWords("", -1));
 		assertEquals("PopulateSent Helper - getFirstNWords: none", nWords,
 				tester.getFirstNWords(null, 1));
 		assertEquals("PopulateSent Helper - getFirstNWords: none", nWords,
 				tester.getFirstNWords("", 1));
 		nWords.add("word1");
 		nWords.add("word2");
 		assertEquals("PopulateSent Helper - getFirstNWords: none", nWords,
 				tester.getFirstNWords("word1 word2 word3 word4", 2));
 		assertEquals("PopulateSent Helper - getFirstNWords: none", nWords,
 				tester.getFirstNWords("word1 word2", 3));
 	}
 	
 	@Test
 	public void testGetAllWords() {
 		Map<String, Integer> wordsBefore = new HashMap<String, Integer>();
 		wordsBefore.put("word1", 1);
 		wordsBefore.put("word2", 2);
 		Map<String, Integer> wordsAfter = new HashMap<String, Integer>();
 		wordsAfter.put("word1", 2);
 		wordsAfter.put("word2", 4);
 		wordsAfter.put("word3", 2);
 		wordsAfter.put("word4", 1);
 		wordsAfter.put("word5", 1);
 		assertEquals("PopulateSent Helper - getAllWords", wordsAfter,
 				tester.getAllWords("word1 word2 word3 word2 word3 word4 word5",
 						wordsBefore));
 	}
 	
 	@Test
 	public void testGetSentencePtns(){
		Learner myTester = learnerFactory();		
 		Set<String> token = new HashSet<String>();
 		token.addAll(Arrays.asList("and or nor".split(" ")));
 		token.add("/");
 		token.add("and / or");
 		
 		List<String> words = new ArrayList<String>();
 		words.addAll(Arrays
 				.asList("distinct crown and <N>base</N> demarcated <B>by</B> <B>a</B> <N>constriction</N> <B>(</B> neck"
 						.split(" ")));
 		String target = "qq&nqbbnbq";
 		
 		assertEquals("getSentencePtns", target, myTester.getLearnerUtility().getSentencePtn(myTester.getDataHolder(), token, 80, words));
 	}
 
 	@Test
 	public void testAddSpace() {
 		// null
 		assertEquals("Result", null, tester.addSpace(null, null));
 		// ""
 		assertEquals("Result", "", tester.addSpace("", ""));
 		assertEquals("Result", "word , word ; word : word ! word ? word . ",
 				tester.addSpace("word,word;word:word!word?word.", "\\W"));
 	}
 	
 	@Test
 	public void testGetSentenceHead() {
 		assertEquals("getSentenceHead - case 0.1 - null input", null, tester.getSentenceHead(null));
 		assertEquals("getSentenceHead - case 0.2 - empty string input", "", tester.getSentenceHead(""));
 		
 		assertEquals("getSentenceHead - case 1", "word1 word2", tester.getSentenceHead("word1 word2 , word3"));
 		assertEquals("getSentenceHead - case 1", "word1 word2", tester.getSentenceHead("word1 word2 : word3"));
 		assertEquals("getSentenceHead - case 1", "word1 word2", tester.getSentenceHead("word1 word2 ; word3"));
 		assertEquals("getSentenceHead - case 1", "word1 word2", tester.getSentenceHead("word1 word2 . word3"));
 		assertEquals("getSentenceHead - case 1", "word1 word2", tester.getSentenceHead("word1 word2 [ word3"));
 		assertEquals("getSentenceHead - case 1", "word1 word2", tester.getSentenceHead("word1 word2 ( word3"));
 		
 		assertEquals("getSentenceHead - case 2", "lepidotrichia", tester.getSentenceHead("lepidotrichia , of fin webs"));
 		assertEquals("getSentenceHead - case 2", "bases of", tester.getSentenceHead("bases of tooth whorls"));
 		
 		assertEquals("getSentenceHead - case n", "word1 word2 word3", tester.getSentenceHead("word1 word2 word3"));
 	}
 
 	@Test
 	public void testHideAbbreviations(){
 		assertEquals("hideAbbreviations", "Word1 jr[DOT] name word2.", tester.hideAbbreviations("Word1 jr. name word2."));
 		assertEquals("hideAbbreviations", "Word1 Gen[DOT] name word2.", tester.hideAbbreviations("Word1 Gen. name word2."));
 		assertEquals("hideAbbreviations", "Word1 uNiV[DOT] name word2.", tester.hideAbbreviations("Word1 uNiV. name word2."));
 		assertEquals("hideAbbreviations", "Word1 blvd[DOT] name coRp[DOT] word 3 name word2.", tester.hideAbbreviations("Word1 blvd. name coRp. word 3 name word2."));
 		assertEquals("hideAbbreviations", "Word1 bld[DOT] name coRp[DOT] word 3 name word2.", tester.hideAbbreviations("Word1 bld. name coRp. word 3 name word2."));		
 		assertEquals("hideAbbreviations", "Word1 uNiV[DOT] name coRp[DOT] word 3 name word2.", tester.hideAbbreviations("Word1 uNiV. name coRp. word 3 name word2."));	
 	}
 	
 	@Test
 	public void testRestoreAbbreviations(){
 		assertEquals("hideAbbreviations", "Word1 jr. name word2.", tester.restoreAbbreviations("Word1 jr[DOT] name word2."));
 		assertEquals("hideAbbreviations", "Word1 Gen. name word2.", tester.restoreAbbreviations("Word1 Gen[DOT] name word2."));
 		assertEquals("hideAbbreviations", "Word1 uNiV. name word2.", tester.restoreAbbreviations("Word1 uNiV[DOT] name word2."));
 		assertEquals("hideAbbreviations", "Word1 uNiV. name coRp. word 3 name word2.", tester.restoreAbbreviations("Word1 uNiV[DOT] name coRp[DOT] word 3 name word2."));
 		assertEquals("hideAbbreviations", "Word1 uNiV. name pde. word 3 name word2.", tester.restoreAbbreviations("Word1 uNiV[DOT] name pde[DOT] word 3 name word2."));
 		assertEquals("hideAbbreviations", "Word1 uNiV. name pd. word 3 name word2.", tester.restoreAbbreviations("Word1 uNiV[DOT] name pd[DOT] word 3 name word2."));
 	}
 	
 	@Test
 	public void testSegmentSentence(){
 		assertEquals("segmentSentence - handle abbreviations", new Token("This is jr. Gates."), tester.segmentSentence("This is jr. Gates. This is second sentence.").get(0));
 		assertEquals("segmentSentence - handle abbreviations", new Token("This is second sentence."), tester.segmentSentence("This is jr. Gates. This is second sentence.").get(1));
 		assertEquals("segmentSentence - handle abbreviations", new Token("The Energy DEPT. is holding a work shop now."), tester.segmentSentence("The Energy DEPT. is holding a work shop now. This is second sentence. ").get(0));
 		assertEquals("segmentSentence - handle abbreviations", new Token("This is second sentence."), tester.segmentSentence("The Energy DEPT. is holding a work shop now. This is second sentence. ").get(1));
 		assertEquals("segmentSentence - handle abbreviations", new Token("Mr. Gates from the Energy DEPT. is holding a work shop now."), tester.segmentSentence("Mr. Gates from the Energy DEPT. is holding a work shop now. This is second sentence. ").get(0));
 		
 	}
 	
 	@Test
 	public void testIterable2Pattern(){
 		assertEquals("Iterable2Pattern - null", "", tester.Iterable2Pattern(null));
 		assertEquals("Iterable2Pattern - empty input", "", tester.Iterable2Pattern(new LinkedList<String>()));
 		
 		Set<String> input = new HashSet<String>();
 		input.add("word1");
 		input.add("word2");
 		input.add("word3");
 		input.add("(");
 		assertEquals("Iterable2Pattern - set input", "word1|word2|word3|\\(", tester.Iterable2Pattern(input));
 	}
 	
 	@Test
 	public void testPattern2Set(){
 		assertEquals("pattern2Set - null input", new HashSet<String>(),
 				LearnerUtility.Pattern2Set(null));
 		assertEquals("pattern2Set - empty input", new HashSet<String>(),
 				LearnerUtility.Pattern2Set(""));
 		
 		Set<String> result = new HashSet<String>();
 		result.addAll(Arrays.asList("word1|word2|word3".split("|")));
 		assertEquals("pattern2Set - normal input", result,
 				LearnerUtility.Pattern2Set("word2|word3|word1"));
 		
 	}
 	
 	 @Test
 	    public void testGetPSWord(){
 			Learner myTester = learnerFactory();
 			
 	    	myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"acrodin", "s", "role", "0", "0", null, null}));
 	    	myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"areas", "p", "role", "0", "0", null, null}));
 	    	myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"(", "p", "role", "0", "0", null, null}));
 	    	
 	    	
 	    	Set<String> target = new HashSet<String>();
 	    	target.add("acrodin");
 	    	target.add("areas");
 	    	
 	    	assertEquals("getPSWords", target, myTester.getLearnerUtility().getPSWords(myTester.getDataHolder()));
 	    }
 	    
 	    @Test
 	    public void testGetO() {
 			Learner myTester = learnerFactory();
 
 	    	myTester.getDataHolder().add2Holder(DataHolder.SENTENCE, Arrays.asList(new String[] {"src", "sent", "osent","lead","status","ignore","m","type"}));
 	    	myTester.getDataHolder().add2Holder(DataHolder.SENTENCE, Arrays.asList(new String[] {"src", "sent", "osent","lead","status",null,"m","type"}));
 	    	myTester.getDataHolder().add2Holder(DataHolder.SENTENCE, Arrays.asList(new String[] {"src", "sent", "osent","lead","status","taga tagb","m","type"}));
 	    	myTester.getDataHolder().add2Holder(DataHolder.SENTENCE, Arrays.asList(new String[] {"src", "sent", "osent","lead","status","taga[tagb]","m","type"}));
 	    	myTester.getDataHolder().add2Holder(DataHolder.SENTENCE, Arrays.asList(new String[] {"src", "sent", "osent","lead","status","tag1","m","type"}));
 	    	myTester.getDataHolder().add2Holder(DataHolder.SENTENCE, Arrays.asList(new String[] {"src", "sent", "osent","lead","status","tag2","m","type"}));
 	    	
 	    	Set<String> target = new HashSet<String>();
 	    	target.add("tag1");
 	    	target.add("tag2");
 	    	
 	    	assertEquals("getOs", target, myTester.getLearnerUtility().getOrgans(myTester.getDataHolder()));
 	    }
 	    
 	    @Test
 	    public void testGetModifiers(){
 			Learner myTester = learnerFactory();
 			
 			myTester.getDataHolder().add2Holder(DataHolder.MODIFIER, Arrays.asList(new String[] {"basal", "1", "false"}));
 			myTester.getDataHolder().add2Holder(DataHolder.MODIFIER, Arrays.asList(new String[] {"endoskeletal", "1", "false"}));
 			myTester.getDataHolder().add2Holder(DataHolder.MODIFIER, Arrays.asList(new String[] {"\\", "1", "false"}));
 			myTester.getDataHolder().add2Holder(DataHolder.MODIFIER, Arrays.asList(new String[] {null, "1", "false"}));
 	    	
 	    	Set<String> target = new HashSet<String>();
 	    	target.add("basal");
 	    	target.add("endoskeletal");
 	    	
 	    	assertEquals("getModifiers", target, myTester.getLearnerUtility().getModifiers(myTester.getDataHolder()));
 	    }
 	    
 	    @Test
 	    public void testGetBoundaries(){
 			Learner myTester = learnerFactory();
 
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"\\", "b", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {")", "b", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"[", "b", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"}", "b", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {".", "b", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"|", "b", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"+", "b", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"*", "b", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"?", "b", "role", "0", "0", null, null}));
 			
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {",", "b", "role", "0", "0", null, null}));
 			
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"about", "b", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"along", "b", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"acrodin", "s", "role", "0", "0", null, null}));
 			
 			Set<String> targetWords = new HashSet<String>();
 			targetWords.addAll(Arrays.asList("about along".split(" ")));
 			
 			Set<String> targetMarks = new HashSet<String>();
 			targetMarks.addAll(Arrays.asList(") \\ [ } . | * + ?".split(" ")));
 			
 			List<Set<String>> target = new LinkedList<Set<String>>();
 			target.add(targetWords);
 			target.add(targetMarks);
 			
 			assertEquals("getBoundaries", target, myTester.getLearnerUtility().getBoundaries(myTester.getDataHolder()));
 	    }
 	    
 	    @Test
 	    public void testGetProperNouns(){
 			Learner myTester = learnerFactory();
 			
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"propernoun1", "z", "*", "0", "0", "", null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"acrodin", "s", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"propernoun2", "z", "role", "0", "0", null, null}));
 			myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"(", "z", "role", "0", "0", null, null}));
 			
 			
 			Set<String> target = new HashSet<String>();
 			target.add("propernoun1");
 			target.add("propernoun2");
 			
 			assertEquals("getProperNouns", target, myTester.getLearnerUtility().getProperNouns(myTester.getDataHolder()));
 	    }
 	    
 	private Learner learnerFactory(){
 		Learner myTester;
 
 		Configuration myConfiguration = new Configuration();
 		ITokenizer tokenizer = new OpenNLPTokenizer(
 				myConfiguration.getOpenNLPTokenizerDir());
 		ITokenizer sentenceDetector = new OpenNLPSentencesTokenizer(
 				myConfiguration.getOpenNLPSentenceDetectorDir());
 		WordNetPOSKnowledgeBase wordNetPOSKnowledgeBase = null;
 		try {
 			wordNetPOSKnowledgeBase = new WordNetPOSKnowledgeBase(myConfiguration.getWordNetDictDir(), false);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		LearnerUtility myLearnerUtility = new LearnerUtility(sentenceDetector,
 				tokenizer, wordNetPOSKnowledgeBase);
 		myTester = new Learner(myConfiguration, tokenizer, myLearnerUtility);
 
 		return myTester;
 	}
 	
 	@Test
 	public void testTagAllSentence(){
 		assertEquals("tagAllSentenceHelper", "word1 word2", tester.tagAllSentencesHelper("word1 <tag> word2"));
 		assertEquals("tagAllSentenceHelper", "3_nerved , cup_shaped , 3 - 5 ( - 7 ) _nerved", tester.tagAllSentencesHelper(" 	 3  - nerved, cup- shaped, 3-5 (-7) -nerved		 "));
 	}
 	
 
 	@Test
 	public void testAnnotateSentence(){
 		// Test Case 1: See testUnknownWordBootstrapping - Postprocessing
 		
 		// Test Case 2:
 		String input = "stems usually erect , sometimes prostrate to ascending ( underground stems sometimes woody caudices or rhizomes , sometimes fleshy ) .";
 		String expected1 = 
 				"stems usually erect , sometimes prostrate to ascending <B>(</B> underground stems sometimes woody caudices or rhizomes , sometimes fleshy <B>)</B> .";
 		String expected2 = 
 				"stems <B>usually</B> <B>erect</B> , sometimes prostrate to ascending <B>(</B> underground stems sometimes woody caudices or rhizomes , sometimes fleshy <B>)</B> .";
 		Set<String> boundaryWords = new HashSet<String>();
 		Set<String> boundaryMarks = new HashSet<String>();
 		boundaryMarks.addAll(Arrays.asList("( ) [ ] { }".split(" ")));
 		boundaryWords.addAll(Arrays.asList("under up upward usually erect villous was weakly".split(" ")));
 		
 		assertEquals("annotateSentenceHelper1", expected1, tester.annotateSentenceHelper(input, boundaryMarks, "B", false));
 		assertEquals("annotateSentenceHelper1", expected2, tester.annotateSentenceHelper(expected1, boundaryWords, "B", true));
 		
 		assertEquals("annotateSentenceHelper2", " word ", tester.annotateSentenceHelper2("<B> 	 </B> word <N> 	 </N>"));
 		assertEquals("annotateSentenceHelper2", "<B> 	 </C> word ", tester.annotateSentenceHelper2("<B> 	 </C> word <B> 	 </B>"));
 		assertEquals("annotateSentenceHelper2", "and", tester.annotateSentenceHelper2("<B>and</B>"));
 		assertEquals("annotateSentenceHelper2", "and</B>", tester.annotateSentenceHelper2("and</B>"));
 	}
 	
 }
