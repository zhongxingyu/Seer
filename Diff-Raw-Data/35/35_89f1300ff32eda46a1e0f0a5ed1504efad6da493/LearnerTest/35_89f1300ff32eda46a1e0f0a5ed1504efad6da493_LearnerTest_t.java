 package semanticMarkup.ling.learn;
 
 import static org.junit.Assert.*;
 
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
 
 import semanticMarkup.core.Treatment;
 
 public class LearnerTest {
 
 	private Learner tester;
 
 	@Before
 	public void initialize() {
 		Configuration myConfiguration = new Configuration();
 		Utility myUtility = new Utility(myConfiguration);
 		this.tester = new Learner(myConfiguration, myUtility);
 	}
 
 	@Test
 	public void testLearn() {
 
 		String str = "C:/Users/Dongye/Dropbox/ATEST/target/descriptions";
 		FileLoader fileLoader = new FileLoader(str);
 		fileLoader.load();
 
 		List<Treatment> tms = fileLoader.getTreatmentList();
 
 		Configuration myConfiguration = new Configuration();
 		Utility myUtility = new Utility(myConfiguration);
 		DataHolder results = new DataHolder(myConfiguration, myUtility);
 
 		Map<String, String> myHeuristicNounTable = results
 				.getHeuristicNounTable();
 		myHeuristicNounTable.put("word1", "type1");
 
 		List<Sentence> mySentenceTable = results.getSentenceHolder();
 		mySentenceTable.add(new Sentence("source1", "sentence1",
 				"originalSentence", "lead1", "status1", "tag1", "modifier1",
 				"type1"));
 
 		// Learner tester = new Learner("plain","res/WordNet/WordNet-3.0/dict");
 
 		// assertEquals ("learner", results, tester.Learn(tms));
 
 		// results = tester.Learn(tms);
 
 		// assertEquals ("learner", results, tester.Learn(tms));
 	}
 
 	@Test
 	public void testHandleText() {
 		// handleTest (Fully finished - Dongye 01/08)
 		// null
 		assertEquals("Result", null, tester.handleText(null));
 		// ""
 		assertEquals("Result", "", tester.handleText(""));
 		// remove " and '
 		assertEquals("Result", "words word", tester.handleText("word's wo\"rd"));
 		// plano - to
 		assertEquals("Result", "word to word",
 				tester.handleText("word -to word"));
 		//
 		assertEquals("Result", "word -shaped",
 				tester.handleText("word ______shaped"));
 		// unhide <i>
 		assertEquals("Result", "word <i> word.",
 				tester.handleText("word &lt;i&gt; word."));
 		// unhide </i>
 		assertEquals("Result", "word </i> word.",
 				tester.handleText("word &lt;/i&gt; word."));
 		// remove 2a. (key marks)
 		assertEquals("Result", "word", tester.handleText("7b. word"));
 		// remove HTML entities
 		assertEquals("Result", "word   word",
 				tester.handleText("word &amp; word"));
 		// " & " => " and "
 		assertEquals("Result", "word and word.",
 				tester.handleText("word & word."));
 		// "_" => "-"
 		assertEquals("Result", "word-word.", tester.handleText("word_word."));
 		// absent ; => absent;
 		assertEquals("Result", "word; word; word.",
 				tester.handleText("word ;word ;word."));
 		// absent;blade => absent; blade
 		assertEquals("Result", "word; word; word.",
 				tester.handleText("word;word;word."));
 		assertEquals("Result", "word: word. word.",
 				tester.handleText("word:word.word."));
 		// 1 . 5 => 1.5
 		assertEquals("Result", "word 1.5 word 384739.84 word.",
 				tester.handleText("word 1 . 5 word 384739 . 84 word."));
 		// #diam . =>diam.
 		assertEquals("Result", "word diam. word diam. word.",
 				tester.handleText("word diam . word diam . word."));
 		// ca . =>ca.
 		assertEquals("Result", "word ca. word ca. word.",
 				tester.handleText("word ca . word ca . word."));
 		// cm|mm|dm|m
 		assertEquals("Result", "word 12 cm[DOT] word 376 mm[DOT] word.",
 				tester.handleText("word 12 cm . word 376 mm. word."));
 
 	}
 
 	@Test
 	public void testHandleSentence() {
 		// handleString
 		// null
 		assertEquals("Result", null, tester.handleSentence(null));
 		// ""
 		assertEquals("Result", "", tester.handleSentence(""));
 		// remove (.a.)
 		assertEquals("Result", "word word word word .",
 				tester.handleSentence("word (.a.) word (a) word ( a ) word."));
 		// remove [.a.]
 		assertEquals("Result", "word word word word .",
 				tester.handleSentence("word [.a.] word [a] word [ a ] word."));
 		// remove {.a.}
 		assertEquals("Result", "word word word word .",
 				tester.handleSentence("word {.a.} word {a} word { a } word."));
 		// to fix basi- and hypobranchial
 		assertEquals(
 				"Result",
 				"word cup_ shaped word cup_ shaped word cup_ shaped word .",
 				tester.handleSentence("word cup --- shaped word cup-shaped word cup ---------        shaped word."));
 
 		// multiple spaces => 1 space
 		assertEquals("Result", "word word word .",
 				tester.handleSentence("word  word	 word."));
 		// remove multipe spaces at the beginning
 		assertEquals("Result", "word word .",
 				tester.handleSentence("  	word word."));
 		// remove multipe spaces at the rear
 		assertEquals("Result", "word word .",
 				tester.handleSentence("word word.    "));
 	}
 
 //	@Test
 //	public void testPopulateUnknownWordsTable() {
 //		fail("Not yet implemented");
 //	}
 
 
 
 
 
 
 //	@Test
 //	public void testDiscount() {
 //		fail("Not yet implemented");
 //	}
 
 //	@Test
 //	public void testGetParentSentenceTag() {
 //		fail("Not yet implemented");
 //	}
 
 //	@Test
 //	public void testTagSentWithMT() {
 //		fail("Not yet implemented");
 //	}
 
 //	@Test
 //	public void testProcessNewWord() {
 //		fail("Not yet implemented");
 //	}
 
 //	@Test
 //	public void testSingularPluralVariations() {
 //		fail("Not yet implemented");
 //	}
 
 //	@Test
 //	public void testUpdateUnknownWords() {
 //		fail("Not yet implemented");
 //	}
 
 //	@Test
 //	public void testAddHeuristicsNouns() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testAddDescriptors() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testAddNouns() {
 //		fail("Not yet implemented");
 //	}
 
 //	@Test
 //	public void testGetHeuristicsNouns() {
 //		fail("Not yet implemented");
 //	}
 
 	@Test
 	public void testGetHeuristicsNounsHelper() {
 		HashSet<String> words = new HashSet<String>();
 		words.add("septa");
 		words.add("word1");
 		words.add("septum");
 		assertEquals("getHeuristicsNouns - handleSpecialCase 1", "septa[p]",
 				tester.getHeuristicsNounsHelper("septa[s]", words));
 	}
 
 	@Test
 	public void testGetPresentAbsentNouns() {
 		// Method getPresentAbsentNouns
 		assertEquals("getPresentAbsentNouns - no present/absent", "",
 				tester.getPresentAbsentNouns("only one pair of abcly presen"));
 		assertEquals("getPresentAbsentNouns - and|or|to", "",
 				tester.getPresentAbsentNouns("only one pair of and present"));
 		assertEquals("getPresentAbsentNouns - STOP words", "",
 				tester.getPresentAbsentNouns("only one pair of without absent"));
 		assertEquals(
 				"getPresentAbsentNoun - always|often|seldom|sometimes|[a-z]+lys",
 				"",
 				tester.getPresentAbsentNouns("only one pair of abcly present"));
 		assertEquals("getPresentAbsentNouns - PENDINGS", "circuli[p]",
 				tester.getPresentAbsentNouns("only one pair of circuli absent"));
 		assertEquals("getPresentAbsentNouns - end with ss", "glass[s]",
 				tester.getPresentAbsentNouns("only one pair of glass absent"));
 		assertEquals(
 				"getPresentAbsentNouns - end with none ss",
 				"computers[p]",
 				tester.getPresentAbsentNouns("only one pair of computers absent"));
 		assertEquals("getPresentAbsentNouns - teeth", "teeth[p]",
 				tester.getPresentAbsentNouns("only one pair of teeth present"));
 		assertEquals("getPresentAbsentNouns - not SENDINGS", "serum[s]",
 				tester.getPresentAbsentNouns("only one pair of serum absent"));
 		assertEquals(
 				"getPresentAbsentNouns - SENDINGS",
 				"computer[s]",
 				tester.getPresentAbsentNouns("only one pair of computer absent"));
 
 	}
 
 //	@Test
 //	public void testCharacterHeuristics() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testAdd2HeuristicNounTable() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testFilterOutDescriptors() {
 //		fail("Not yet implemented");
 //	}
 
 	@Test
 	public void testGetTaxonNameNouns() {
 
 		// Nouns rule 0: Taxon name nouns
 		Set<String> taxonNames = new HashSet<String>();
 		// Method getTaxonNameNouns
 		assertEquals("getTaxonNameNouns - not match", taxonNames,
 				tester.getTaxonNameNouns("word word word"));
 		assertEquals("getTaxonNameNouns - empty taxon name", taxonNames,
 				tester.getTaxonNameNouns("< i >< / i >"));
 		taxonNames.add("word1 word2	word3");
 		taxonNames.add("word1");
 		taxonNames.add("word2");
 		taxonNames.add("word3");
 		taxonNames.add("word4 word5");
 		taxonNames.add("word4");
 		taxonNames.add("word5");
 		assertEquals(
 				"getTaxonNameNouns - match",
 				taxonNames,
 				tester.getTaxonNameNouns("< i	>word1 word2	word3< /	i>, < i >word4 word5<	/i>"));
 	}
 
 	@Test
 	public void testGetNounsMecklesCartilage() {
 		// Nouns rule 0.5: Method getNounsMecklesCartilage
 		Set<String> nouns = new HashSet<String>();
 		assertEquals("getTaxonNameNouns - not match", nouns,
 				tester.getNounsMecklesCartilage("word word word"));
 		nouns.add("meckel#s");
 		nouns.add("meckels");
 		nouns.add("meckel");
 		assertEquals("getTaxonNameNouns - match", nouns,
 				tester.getNounsMecklesCartilage("word Meckel#s word"));
 	}
 
 	@Test
 	public void testGetNounsRule1() {
 		// Method getNounsRule1
 		// Set<String> descriptorMap = new HashSet<String>();
 		Set<String> nouns1 = new HashSet<String>();
 		nouns1.add("term1");
 		assertEquals(
 				"getNounsRule1",
 				nouns1,
 				tester.getNounsRule1(
 						"Chang_2004.xml_ ffa60eb1-4320-4e69-b151-75a2615dca4b_29482156-8083-430c-91f4-e80209b50138.txt-0",
 						"term1", new HashMap<String, Boolean>()));
 	}
 
 	@Test
 	public void testGetNounsRule2() {
 		// Method getNounsRule2
 		Set<String> nouns2 = new HashSet<String>();
 		assertEquals("getNounsRule2 - not match", nouns2,
 				tester.getNounsRule2("word word 	word soe width nea"));
 		nouns2.add("nouna");
 		assertEquals("getNounsRule2 - match 1", nouns2,
 				tester.getNounsRule2("word word 	word some nouna"));
 		nouns2.add("nounb");
 		assertEquals(
 				"getNounsRule2 - match 2",
 				nouns2,
 				tester.getNounsRule2("word some nouna near word some width near word third nounb near end"));
 		assertEquals(
 				"getNounsRule2 - match 2",
 				nouns2,
 				tester.getNounsRule2("word some nouna near word some width near word third nounb near end nounc abction of end"));
 	}
 
 	@Test
 	public void testGetNounsRule3Helper() {
 		// Method getNounsRule3
 		Set<String> nouns3 = new HashSet<String>();
 		nouns3.add("II");
 		nouns3.add("IX");
 		assertEquals(
 				"getNounsRule3",
 				nouns3,
 				tester.getNounsRule3Helper("posterior and dorsal to foramen for nerve II (i.e. a posterior oblique myodome IX)"));
 		nouns3.remove("II");
 		nouns3.remove("IX");
 		nouns3.add("Meckelian");
 		assertEquals(
 				"getNounsRule3",
 				nouns3,
 				tester.getNounsRule3Helper("Pronounced dorsal process on Meckelian element"));
 	}
 
 	@Test
 	public void testGetNounsRule4() {
 		// Method getNounsRule4
 		Set<String> nouns4 = new HashSet<String>();
 		assertEquals("getNounsRule4 - not match", nouns4,
 				tester.getNounsRule4("word word 	word noun one"));
 		nouns4.add("nouna");
 		assertEquals("getNounsRule4 - not match", nouns4,
 				tester.getNounsRule4("word word 	word nouna 1"));
 		nouns4.remove("nouna");
 		nouns4.add("nounb");
 		assertEquals(
 				"getNounsRule4 - not match",
 				nouns4,
 				tester.getNounsRule4("word word 	word page 1 word above 2 word NoUnb 2 end"));
 	}
 
 	@Test
 	public void testGetDescriptorsRule1() {
 		// Method getDescriptorsRule1
 		Set<String> descriptors1 = new HashSet<String>();
 		descriptors1.add("absent");
 		assertEquals("getDescriptorsRule1", descriptors1,
 				tester.getDescriptorsRule1(
 						"Brazeau_2009.xml_states200_state202.txt-0", "absent",
 						new HashSet<String>()));
 		descriptors1.remove("absent");
 		descriptors1.add("present");
 		Set<String> nouns = new HashSet<String>();
 		nouns.add("present");
 		assertEquals("getDescriptorsRule1", new HashSet<String>(),
 				tester.getDescriptorsRule1(
 						"Brazeau_2009.xml_states200_state203.txt-0", "present",
 						nouns));
 		assertEquals("getDescriptorsRule1", descriptors1,
 				tester.getDescriptorsRule1(
 						"Brazeau_2009.xml_states200_state203.txt-0", "present",
 						new HashSet<String>()));
 	}
 
 //	@Test
 //	public void testGetDescriptorsRule2() {
 //		fail("Not yet implemented");
 //	}
 
 	@Test
 	public void testIsDescriptor() {
 		// Method filterOutDescriptors
 		Set<String> rNouns = new HashSet<String>();
 		Set<String> rDescriptors = new HashSet<String>();
 		Set<String> results = new HashSet<String>();
 		rNouns.add("noun1");
 		rNouns.add("descriptor2");
 		rNouns.add("noun2");
 		rDescriptors.add("descriptor1");
 		rDescriptors.add("descriptor2");
 		rDescriptors.add("descriptor3");
 		results.add("noun1");
 		results.add("noun2");
 		assertEquals("filterOutDescriptors", results,
 				tester.filterOutDescriptors(rNouns, rDescriptors));
 	}
 
 	@Test
 	public void testIsMatched() {
 		// Method isMatched
 		Map<String, Boolean> descriptorMap = new HashMap<String, Boolean>();
 		descriptorMap.put("term1", false);
 		assertEquals("isMatched", false, descriptorMap.get("term1"));
 		assertEquals("isMatched", true, tester.isMatched(
 				"begin word word was term1 word word end", "term1",
 				descriptorMap));
 		assertEquals("isMatched", true, descriptorMap.get("term1"));
 	}
 
 //	@Test
 //	public void testAddStopWords() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testAddCharacters() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testAddNumbers() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testAddClusterstrings() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testAddProperNouns() {
 //		fail("Not yet implemented");
 //	}
 //
 	@Test
 	public void testPosBySuffix() {
 		// Pattern 1: ^[a-z_]+(er|est|fid|form|ish|less|like|ly|merous|most|shaped)$
 		// Pattern 2: ^[._.][a-z]+
 		
 		tester.posBySuffix();
 	}
 	
 	@Test
 	public void testPosBySuffixCase1Helper(){
 		assertEquals("posBySuffix Case1 - match", true, tester.posBySuffixCase1Helper("approximately"));
 		assertEquals("posBySuffix Case1 - not match", false, tester.posBySuffixCase1Helper("bigger"));
 		assertEquals("posBySuffix Case1 - match", true, tester.posBySuffixCase1Helper("bifid"));
 		assertEquals("posBySuffix Case1 - not match", false, tester.posBySuffixCase1Helper("per"));
 	}
 	
 	@Test
 	public void testPosBySuffixCase2Helper(){
 		assertEquals("posBySuffix Case2 - match", true, tester.posBySuffixCase2Helper("_nerved"));
 		assertEquals("posBySuffix Case2 - not match", false, tester.posBySuffixCase2Helper("nerved"));
 	}
 
 	@Test
 	public void testContainSuffix() {
 		// test method containSuffix
 		assertEquals("containSuffix less", true,
 				tester.containSuffix("less", "", "less"));
 		assertEquals("containSuffix ly", true,
 				tester.containSuffix("slightly", "slight", "ly"));
 		assertEquals("containSuffix er", false,
 				tester.containSuffix("fewer", "few", "er"));
 		assertEquals("containSuffix est", true,
 				tester.containSuffix("fastest", "fast", "est"));
 		assertEquals("containSuffix base is in WN", true,
 				tester.containSuffix("platform", "plat", "form"));
 		assertEquals("containSuffix sole adj", true,
 				tester.containSuffix("scalelike", "scale", "like"));
 		
 		// case 3.1.2 and case 3.3.3 not tested
 		assertEquals("containSuffix 111", false,
 				tester.containSuffix("anterolaterally", "anterolateral", "ly")); // 111
 		assertEquals("containSuffix 121", false,
 				tester.containSuffix("mesially", "mesial", "ly")); // 121
 		assertEquals("containSuffix 122", false,
 				tester.containSuffix("per", "p", "er")); // 122
 		assertEquals("containSuffix 212", false,
 				tester.containSuffix("border", "bord", "er")); // 212
 		assertEquals("containSuffix 212", false,
 				tester.containSuffix("bigger", "bigg", "er")); // 212
 		assertEquals("containSuffix 221", true,
 				tester.containSuffix("anteriorly", "anterior", "ly")); // 221
 		assertEquals("containSuffix 222", false,
 				tester.containSuffix("corner", "corn", "er")); // 222
 		assertEquals("containSuffix 222", true,
 				tester.containSuffix("lower", "low", "er")); // 222
 		assertEquals("containSuffix 223", true,
 				tester.containSuffix("bifid", "bi", "fid")); // 223
 
 	}
 
 	@Test
 	public void testMarkupByPattern() {
 		Configuration myConfiguration = new Configuration();
 		Utility myUtility = new Utility(myConfiguration);
 		Learner myTester = new Learner(myConfiguration, myUtility);
 
 		myTester.getDataHolder().add2Holder(DataHolder.SENTENCE, 
 				Arrays.asList(new String[] {"source1", "sentence1", "x=word word word", "lead1", "status1", "tag1", "modifier1", "type1"}));
 		myTester.markupByPattern();
 		
 		List<Sentence> targetSentenceHolder = new LinkedList<Sentence>();
 		targetSentenceHolder.add(new Sentence("source1", "sentence1", "x=word word word", "lead1", "status1", "chromosome", "", "type1"));
 		
 		assertEquals("markupByPattern", targetSentenceHolder, myTester.getDataHolder().getSentenceHolder());
 	}
 	
 	@Test
 	public void testMarkupByPatternHelper(){
 		// case 1
 		Sentence mySentence1 = new Sentence("source1", "sentence1", "x=word word word", "lead1", "status1", "tag1", "modifier1", "type1");
 		Sentence target1 = new Sentence("source1", "sentence1", "x=word word word", "lead1", "status1", "chromosome", "", "type1");
 		tester.markupByPatternHelper(mySentence1);
 		assertEquals("markupByPatternHelper - case 1", target1,mySentence1);
 		
 		// case 2
 		Sentence mySentence2 = new Sentence("source2", "sentence2", "2n=abc...", "lead2", "status2", "tag2", "modifier2", null);
 		Sentence target2 = new Sentence("source2", "sentence2", "2n=abc...", "lead2", "status2", "chromosome", "", null);
 		tester.markupByPatternHelper(mySentence2);
 		assertEquals("markupByPatternHelper - case 2", target2,mySentence2);
 		
 		// case 3
 		Sentence mySentence3 = new Sentence("source", "sentence", "x word word", "lead", "status", "tag", "modifier", null);
 		Sentence target3 = new Sentence("source", "sentence", "x word word", "lead", "status", "chromosome", "", null);
 		tester.markupByPatternHelper(mySentence3);
 		assertEquals("markupByPatternHelper - case 3", target3, mySentence3);
 		
 		// case 4
 		Sentence mySentence4 = new Sentence("source", "sentence", "2n word word", "lead",null, "tag", "modifier", null);
 		Sentence target4 = new Sentence("source", "sentence", "2n word word", "lead", null, "chromosome", "", null);
 		tester.markupByPatternHelper(mySentence4);
 		assertEquals("markupByPatternHelper - case 4", target4, mySentence4);
 		
 		// case 5
 		Sentence mySentence5 = new Sentence("source", "sentence", "2 nword word", "lead", "status", "tag", "modifier", "");
 		Sentence target5 = new Sentence("source", "sentence", "2 nword word", "lead", "status", "chromosome", "", "");
 		tester.markupByPatternHelper(mySentence5);
 		assertEquals("markupByPatternHelper - case 5", target5, mySentence5);
 		
 		// case 6
 		Sentence mySentence6 = new Sentence("source", "sentence", "fl. word word", "lead", "status", null, null, "");
 		Sentence target6 = new Sentence("source", "sentence", "fl. word word", "lead", "status", "flowerTime", "", "");
 		tester.markupByPatternHelper(mySentence6);
 		assertEquals("markupByPatternHelper - case 6", target6, mySentence6);
 		
 		// case 7
 		Sentence mySentence7 = new Sentence("source", "sentence", "fr.word word", "lead", "status", null, "", "");
 		Sentence target7 = new Sentence("source", "sentence", "fr.word word", "lead", "status", "fruitTime", "", "");
 		tester.markupByPatternHelper(mySentence7);
 		assertEquals("markupByPatternHelper - case 7", target7, mySentence7);
 	}
 
 	@Test
 	public void testMarkupIgnore() {
 		Configuration myConfiguration = new Configuration();
 		Utility myUtility = new Utility(myConfiguration);
 		Learner myTester = new Learner(myConfiguration, myUtility);
 
 		myTester.getDataHolder().add2Holder(DataHolder.SENTENCE, 
 				Arrays.asList(new String[] {"source1", "sentence1", "IGNOREPTN", "lead1", "status1", "tag1", "modifier1", "type1"}));
 		myTester.markupIgnore();
 		
 		List<Sentence> targetSentenceHolder = new LinkedList<Sentence>();
 		targetSentenceHolder.add(new Sentence("source1", "sentence1", "IGNOREPTN", "lead1", "status1", "ignore", "", "type1"));
 		
 		assertEquals("markupIgnore", targetSentenceHolder, myTester.getDataHolder().getSentenceHolder());
 
 	}
 
 	@Test
 	public void testMarkupIgnoreHelper() {
 		Sentence mySentence1 = new Sentence("source", "sentence", "IGNOREPTN", "lead", "status", null, "", "");
 		Sentence target1 = new Sentence("source", "sentence", "IGNOREPTN", "lead", "status", "ignore", "", "");
 		tester.markupIgnoreHelper(mySentence1);
 		assertEquals("markupIgnoreHelper", target1, mySentence1);
 		
 		Sentence mySentence2 = new Sentence("source", "sentence", " IGNOREPTN", "lead", "status", null, "", "");
 		Sentence target2 = new Sentence("source", "sentence", " IGNOREPTN", "lead", "status", "ignore", "", "");
 		tester.markupIgnoreHelper(mySentence2);
 		assertEquals("markupIgnoreHelper", target2, mySentence2);
 	}
 	
 //	@Test
 //	public void testDiscover() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testRuleBasedLearn() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testDoIt() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testGetPOSptn() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testCheckPOSInfo() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testTagIt() {
 //		fail("Not yet implemented");
 //	}
 //
 //	@Test
 //	public void testMatchPattern() {
 //		fail("Not yet implemented");
 //	}
 
 	@Test
 	public void testBuildPattern() {		
 		Configuration myConfiguration = new Configuration();
 		Utility myUtility = new Utility(myConfiguration);
 		Learner myTester = new Learner(myConfiguration, myUtility);
 
 		// Method buildPattern
 //		assertEquals(
 //				"buildPattern",
 //				"(?:^\\b(?:one|two|three)\\b|^\\w+\\s\\b(?:one|two|three)\\b|^\\w+\\s\\w+\\s\\b(?:one|two|three)\\b)",
 //				tester.buildPattern("one two three".split(" ")));
 		
 		HashSet<String> wordSet= new HashSet<String>();
 		wordSet.add("teeth");
 		wordSet.add("unicuspid");
 		wordSet.add("with");
 		myTester.setCheckedWordSet(wordSet);
 		
 		assertEquals("buildPattern", null,
 				myTester.buildPattern("teeth ; 9".split(" ")));
 		
 		assertEquals("buildPattern", 
 				"(?:^\\b(?:variously|arranged)\\b|^\\w+\\s\\b(?:variously|arranged)\\b|^\\w+\\s\\w+\\s\\b(?:variously|arranged)\\b).*$",
 				myTester.buildPattern("teeth variously arranged".split(" ")));
 		
 		wordSet.add("circuli");
 		wordSet.add("present");
 		wordSet.add("on");
 		wordSet.add("hyohyoidei");
 		wordSet.add("muscle");
 		
 		assertEquals("buildPattern", 
 				"(?:^\\b(?:does|not|cross)\\b|^\\w+\\s\\b(?:does|not|cross)\\b|^\\w+\\s\\w+\\s\\b(?:does|not|cross)\\b).*$",
 				myTester.buildPattern("does not cross".split(" ")));
 		
 		wordSet.addAll(Arrays.asList("lepidotrichia:of:passes:between:bases".split(":")));
 		
 		assertEquals("buildPattern", 
 				"(?:^\\b(?:ankylosed|to)\\b|^\\w+\\s\\b(?:ankylosed|to)\\b|^\\w+\\s\\w+\\s\\b(?:ankylosed|to)\\b).*$",
 				myTester.buildPattern("teeth ankylosed to".split(" ")));		
 		
 	}
 	
 	@Test
 	public void testGetPOSptn(){
 		Configuration myConfiguration = new Configuration();
 		Utility myUtility = new Utility(myConfiguration);
 		Learner myTester = new Learner(myConfiguration, myUtility);
 		
 		myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 				Arrays.asList(new String[] {"teeth", "p", "role", "1", "1", "", ""}));
 		
 		myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 				Arrays.asList(new String[] {"unicuspid", "p", "role", "1", "3", "", ""}));
 		
 		myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 				Arrays.asList(new String[] {"with", "b", "role", "1", "1", "", ""}));
 		
 		assertEquals("getPOSptn", "p?b", myTester.getPOSptn(Arrays.asList("teeth unicuspid with".split(" "))));
 	}
 	
 	@Test
 	public void testDoItCaseHandle(){
 		Configuration myConfiguration = new Configuration();
 		Utility myUtility = new Utility(myConfiguration);
 		
 //		// case x: boundary case
 //		Learner myTesterBoundary = new Learner(myConfiguration, myUtility);
 //		assertEquals("CaseHandle - case 3.2", null, myTesterBoundary.doItCaseHandle(null, null));   
 //		assertEquals("CaseHandle - case 3.2", new StringAndInt("",0), myTesterBoundary.doItCaseHandle("", ""));   
 //		
 //        // case 1
 //		Learner myTester1 = new Learner(myConfiguration, myUtility);				
 //		myTester1.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"submandibulars", "p", "", "0", "0", null, null}));
 //		
 //		assertEquals("CaseHandle - case 1", new StringAndInt("submandibulars",0), myTester1.doItCaseHandle("submandibulars", "submandibulars"));
 //        
 //        // case 3.2
 //		Learner myTester32 = new Learner(myConfiguration, myUtility);
 //		myTester32.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"teeth", "p", "role", "1", "1", "", ""}));
 //		myTester32.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"with", "b", "role", "1", "1", "", ""}));
 //        assertEquals("CaseHandle - case 3.2", new StringAndInt("teeth",1), 
 //            myTester32.doItCaseHandle("teeth unicuspid with crowns posteriorly curved along the main axis of the mandible , organized into a long series of equally_ sized teeth", 
 //                "teeth unicuspid with"));   
 //        
 //        // case 4
 //        // case 4.2	
 //		Learner myTester42 = new Learner(myConfiguration, myUtility);
 //		// test case 1
 //		myTester42.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"teeth", "p", "role", "1", "1", "", ""}));
 //		myTester42.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"variously", "b", "role", "0", "0", "", ""}));
 //
 //        assertEquals("CaseHandle - case 4.2", new StringAndInt("teeth",0), 
 //                myTester42.doItCaseHandle("teeth variously arranged , but never very numerous , equally_ sized and regularly curved posteriorly along main axis of mandible", 
 //                    "teeth variously arranged")); 
 //        
 //        //case 4.2 - test case 2
 //		myTester42.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"muscle", "s", "role", "0", "0", "", ""}));
 //		myTester42.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"with", "b", "role", "0", "0", "", ""}));
 //        assertEquals("CaseHandle - case 4.2", new StringAndInt("hyohyoidei muscle",1), 
 //                myTester42.doItCaseHandle("hyohyoidei muscle with a broad origin across the entire ventral surface and lateral margins of the ventrolateral wings of the urohyal",  
 //                    "hyohyoidei muscle with")); 
 //        
 //        //case 4.2 - test case 2
 //		myTester42.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"bases", "p", "role", "0", "0", "", ""}));
 //		myTester42.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"of", "b", "role", "2", "2", "", ""}));
 //        assertEquals("CaseHandle - case 4.2", new StringAndInt("bases",0), 
 //                myTester42.doItCaseHandle("bases of tooth whorls", "bases of")); 
         
 
         
         // case 10
 		// case 10.1.1
 		Learner myTester10_1_1 = new Learner(myConfiguration, myUtility);
         myTester10_1_1.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 				Arrays.asList(new String[] {"of", "b", "role", "0", "0", "", ""}));
         assertEquals("CaseHandle - case 10.1.1", new StringAndInt("teeth",2), 
                 myTester10_1_1.doItCaseHandle("teeth of dentary", 
                     "teeth of")); 
        
        myTester10_1_1.getDataHolder().add2Holder(DataHolder.WORDPOS, 
				Arrays.asList(new String[] {"on", "b", "role", "4", "4", "", ""}));
        assertEquals("CaseHandle - case 10.1.1", new StringAndInt("foramina",2), 
                myTester10_1_1.doItCaseHandle("foramina on external surface of lower jaw", 
                    "foramina on")); 
 		
 //        // case 10.2
 //        Learner myTester10_2 = new Learner(myConfiguration, myUtility);
 //        myTester10_2.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"between", "b", "role", "0", "0", "", ""}));
 //        
 //        assertEquals("CaseHandle - case 10.2", new StringAndInt("",0), 
 //                myTester10_2.doItCaseHandle("passes between scales", 
 //                    "passes between")); 
 //                    
 //        // case 0
 //        Learner myTester0 = new Learner(myConfiguration, myUtility);
 //        
 //		myTester0.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"does", "b", "role", "0", "0", "", ""}));
 //		myTester0.getDataHolder().add2Holder(DataHolder.WORDPOS, 
 //				Arrays.asList(new String[] {"not", "b", "role", "0", "0", "", ""}));
 //
 //        assertEquals("CaseHandle - case 0", new StringAndInt("",0), 
 //                myTester0.doItCaseHandle("does not cross over the anterodorsal corner of opercular bone", 
 //                    "does not cross"));                     
         
 	}
 	
 	@Test
 	public void testIsFollowedByNoun() {
 		Configuration myConfiguration = new Configuration();
 		Utility myUtility = new Utility(myConfiguration);
 		Learner myTester = new Learner(myConfiguration, myUtility);
 		
 		myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"rhombic", "b", "role", "0", "0", null, null}));
 		myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"bones", "p", "role", "0", "0", null, null}));
 		myTester.getDataHolder().add2Holder(DataHolder.WORDPOS, Arrays.asList(new String[] {"radial", "s", "role", "0", "0", null, null}));
 		
 		assertEquals("isFollowedByNoun - null case", false, myTester.isFollowedByNoun(null, null));
 		assertEquals("isFollowedByNoun - empty case", false, myTester.isFollowedByNoun("", ""));
 		assertEquals("isFollowedByNoun", true, myTester.isFollowedByNoun("foramina on dermal cheek bones", "foramina on"));
 		assertEquals("isFollowedByNoun", true, myTester.isFollowedByNoun("foramina on bones", "foramina on"));
		assertEquals("isFollowedByNoun", false, myTester.isFollowedByNoun("teeth of dentary", "teeth of"));
 	}
 	
 	
 	
 	
 }
