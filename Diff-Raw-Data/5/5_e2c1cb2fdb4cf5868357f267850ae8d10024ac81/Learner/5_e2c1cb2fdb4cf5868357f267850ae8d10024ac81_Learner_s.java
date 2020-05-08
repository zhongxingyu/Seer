 package semanticMarkup.ling.learn;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import semanticMarkup.core.Treatment;
 import semanticMarkup.know.IGlossary;
 import semanticMarkup.know.lib.WordNetPOSKnowledgeBase;
 import semanticMarkup.ling.Token;
 import semanticMarkup.ling.learn.auxiliary.GetNounsAfterPtnReturnValue;
 import semanticMarkup.ling.learn.auxiliary.KnownTagCollection;
 import semanticMarkup.ling.learn.auxiliary.POSInfo;
 import semanticMarkup.ling.learn.auxiliary.SentenceLeadLengthComparator;
 import semanticMarkup.ling.learn.auxiliary.StringAndInt;
 import semanticMarkup.ling.learn.dataholder.DataHolder;
 import semanticMarkup.ling.learn.dataholder.ModifierTableValue;
 import semanticMarkup.ling.learn.dataholder.SentenceStructure;
 import semanticMarkup.ling.learn.dataholder.WordPOSKey;
 import semanticMarkup.ling.learn.dataholder.WordPOSValue;
 import semanticMarkup.ling.learn.knowledge.Constant;
 import semanticMarkup.ling.learn.knowledge.Initiation;
 import semanticMarkup.ling.learn.knowledge.MarkupByPOS;
 import semanticMarkup.ling.learn.knowledge.UnknownWordBootstrapping;
 import semanticMarkup.ling.learn.utility.LearnerUtility;
 import semanticMarkup.ling.learn.utility.StringUtility;
 import semanticMarkup.ling.transform.ITokenizer;
 
 public class Learner {	
 	private static final Set<String> NONS = null; //??
 	private Configuration myConfiguration;
 	private ITokenizer myTokenizer;
 	
 	// Data holder
 	private DataHolder myDataHolder;
 	
 	// Learner utility
 	private LearnerUtility myLearnerUtility;
 	
 	// Class variables
 	private int NUM_LEAD_WORDS; // Number of leading words
 	
 	// leading three words of sentences 
 	private Set<String> checkedWordSet;
 	
 	
 	// modules
 	Initiation initiationModule;
 	
 	UnknownWordBootstrapping unknownWordBootstrappingModule; 
 	
 	MarkupByPOS markupByPOS;
 	
 	public Learner(Configuration configuration, ITokenizer tokenizer, LearnerUtility learnerUtility) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("Learner");
 		
 		this.myConfiguration = configuration;
 		this.myTokenizer = tokenizer;
 		
 		// Utilities
 		this.myLearnerUtility = learnerUtility;
 		
 		// Data holder
 		this.myDataHolder = new DataHolder(myConfiguration, myLearnerUtility.getWordFormUtility());
 		
 		// Class variables
 		NUM_LEAD_WORDS = this.myConfiguration.getNumLeadWords(); // Set the number of leading words be 3
 		
 		checkedWordSet = new HashSet<String>();
 		
 		myLogger.info("Created Learner");
 		myLogger.info("\tLearning Mode: "+myConfiguration.getLearningMode());
 		myLogger.info("\tMax Tag Lengthr: "+myConfiguration.getMaxTagLength());
 		myLogger.info("\n");
 		
 		initiationModule = new Initiation(this.myLearnerUtility, this.NUM_LEAD_WORDS);
 		unknownWordBootstrappingModule = new UnknownWordBootstrapping(this.myLearnerUtility);
 		markupByPOS = new MarkupByPOS(this.myLearnerUtility);
 	}
 
 	public DataHolder Learn(List<Treatment> treatments, IGlossary glossary, String markupMode) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("Learn");
 		myLogger.trace("Enter Learn");
 		myLogger.trace(String.format("Learning Mode: %s", this.myConfiguration.getLearningMode()));
 		
 //		this.populateSentence(treatments);
 //		this.populateUnknownWordsTable(this.myDataHolder.allWords);
 		
 		this.initiationModule.loadTreatments(treatments);
 		this.initiationModule.run(myDataHolder);
 
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
 		this.resetCounts(myDataHolder);
 		this.markupByPattern();
 		this.markupIgnore();
 		
 		// learning rules with high certainty
 		// At the every beginning, only those sentence whose first word is a p,
 		// could have a tag of "start", see populateSentece - getFirstNWords section -Dongye
 		myLogger.info("Learning rules with high certainty:");
 		this.discover("start");
 		
 		// bootstrapping rules
 		myLogger.info("Bootstrapping rules:");
 		this.discover("normal");
 //		myDataHolder.write2File("");here!!!
 		myLogger.info("Additional bootstrappings:");
 		this.additionalBootstrapping();
 		
 		myLogger.info("Unknownword bootstrappings:");
 //		this.unknownWordBootstrapping();
 		this.unknownWordBootstrappingModule.run(myDataHolder);
 		
 		myLogger.info("Adjectives Verification:");
 		this.adjectivesVerification(myDataHolder);
 		
 		this.separateModifierTag(myDataHolder);
 		
 		this.resolveNMB(myDataHolder);
 		
 		this.setAndOr(myDataHolder);
 		
 		if (StringUtils.equals(this.myConfiguration.getLearningMode(), "adj")) {
 //			print STDOUT "::::::::::::::::::::::::Bootstrapping on adjective subjects: \n";
 //			adjectivesubjectbootstrapping()
 		}
 		else {
 			int v = 0;
 			do {
 				v = 0;
 				this.handleAndOr(myDataHolder);
 			} while (v > 0);
 		}
 		
 		this.resetAndOrTags(myDataHolder);
 
 		this.getLearnerUtility().tagAllSentences(myDataHolder, "singletag", "sentence");
 		
 		this.markupByPOS.run(myDataHolder);
 		
 		this.phraseClause(myDataHolder);
 		
 		this.ditto(myDataHolder);
 		
 		myDataHolder.write2File("");
 		
 		myLogger.info("Learning done!");
 		
 //		myLogger.info(myDataHolder.toString());
 //		myLogger.info(myDataHolder.getSentenceHolder().toString());
 //		myLogger.info(this.myDataHolder.getHeuristicNounHolder().toString());
 //		myLogger.info(myDataHolder.getSentenceHolder().get(0).toString());
 		
 		return myDataHolder;
 	}
 
 	public void addGlossary(IGlossary glossary) {
 		if (glossary != null) {
 			String category = "struture";
 			Set<String> pWords = glossary.getWords(category);
 			Set<String> categories = new HashSet<String>();
 			categories.add(category);
 			Set<String> bWords = glossary.getWordsNotInCategories(categories);
 			this.getDataHolder().addWords2WordPOSHolder(pWords, "p");
 			this.getDataHolder().addWords2WordPOSHolder(bWords, "b");
 		}
 	}
 
 	private void addPredefinedWords() {
 		this.addStopWords();
 		this.addCharacters();
 		this.addNumbers();
 		this.addClusterStrings();
 		this.addProperNouns();		
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public DataHolder getDataHolder() {
 		return this.myDataHolder;
 	}
 
 	/**
 	 * 
 	 */
 	public void addHeuristicsNouns() {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.addHeuristicsNouns");
 		
 		myLogger.trace("Enter addHeuristicsNouns");
 		
 		Set<String> nouns = this.learnHeuristicsNouns();
 		myLogger.debug("Nouns learned from heuristics:");
 		myLogger.debug("\t"+nouns.toString());
 		myLogger.debug("Total: "+nouns.size());
 		
 		List<Set<String>> results = this.characterHeuristics();
 		Set<String> rnouns = results.get(0);
 		Set<String> descriptors = results.get(1);		
 		addDescriptors(descriptors);
 		addNouns(rnouns);	
 		
 		//this.myDataHolder.printHolder(DataHolder.SINGULAR_PLURAL);
 		
 		myLogger.debug("Total: "+nouns.size());
 		Iterator<String> iter = nouns.iterator();
         myLogger.info("Learn singular-plural pair");
 		while (iter.hasNext()) {
 			String e = iter.next();
 			myLogger.trace("Check Word: "+e);
 			
 			if ((e.matches("^.*\\w.*$"))
 					&& (!StringUtility.isMatchedWords(e, "NUM|" + Constant.NUMBER
 							+ "|" + Constant.CLUSTERSTRING + "|"
 							+ Constant.CHARACTER + "|" + Constant.PROPERNOUN))) {
 				myLogger.trace("Pass");
 				
 				// same word may have two different pos tags
 				String[] nounArray = e.split("\\|");
 				for (int i = 0; i < nounArray.length; i++) {
 					String nounAndPOS = nounArray[i];
 					Pattern p = Pattern.compile("(\\w+)\\[([spn])\\]");
 					Matcher m = p.matcher(nounAndPOS);
 					if (m.lookingAt()) {
 						String word = m.group(1);
 						String pos = m.group(2);
 						this.myDataHolder.updateDataHolder(word, pos, "*", "wordpos", 0);
 
 						if (pos.equals("p")) {
 							String plural = word;
 							String singular = this.myLearnerUtility
 									.getWordFormUtility().getSingular(plural);
 							if (singular != null) {
 								if (!singular.equals("")) {
 									this.myDataHolder.addSingularPluralPair(
 											singular, plural);
 								}
 							}
 						}
 
 						if (pos.equals("s")) {
 							String singular = word;
 							List<String> pluralList = this.myLearnerUtility
 									.getWordFormUtility().getPlural(singular);
 							Iterator<String> pluralIter = pluralList.iterator();
 							while (pluralIter.hasNext()) {
 								String plural = pluralIter.next();
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
 				
 		myLogger.trace("Quite addHeuristicsNouns");
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
 				this.myDataHolder.updateDataHolder(descriptor, "b", "", "wordpos", 1);
 			}
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @param rnouns
 	 */
 	public void addNouns(Set<String> rnouns) {
 		Iterator<String> iter = rnouns.iterator();
 		while (iter.hasNext()) {
 			String noun = iter.next();
 			if (!StringUtility.isMatchedWords(noun, Constant.FORBIDDEN)) {
 				this.myDataHolder.updateDataHolder(noun, "n", "", "wordpos", 1);
 			}
 		}
 	}
 
 
 
 	/**
 	 * 
 	 * @return nouns learned by heuristics
 	 */
 	public Set<String> learnHeuristicsNouns() {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.addHeuristicsNouns.learnHeuristicsNouns");
 		
 		// Set of words
 		Set<String> words = new HashSet<String>();
 
 		// Set of nouns
 		Set<String> nouns = new HashSet<String>();
 
 		List<String> sentences = new LinkedList<String>();
 		for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 			String originalSentence = this.myDataHolder.getSentenceHolder().get(i)
 					.getOriginalSentence();
 			myLogger.trace("Original Sentence: "+originalSentence);
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
 			List<String> tokens = this.myLearnerUtility.tokenizeText(sentence, "all");
 			for (String token: tokens) {
 				if (StringUtility.isWord(token)) {
 					words.add(token);
 					myLogger.trace("Add a word into words: "+token);
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
 			String root = myLearnerUtility.getWordFormUtility().getRoot(word);
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
 		myLogger.trace("WordMap:");
 		Iterator<Map.Entry<String, Set<String>>> wordMapIter = wordMap.entrySet()
 				.iterator();
 		while (wordMapIter.hasNext()) {
 			Map.Entry<String, Set<String>> e = wordMapIter.next();
 			myLogger.trace(e.toString());
 		}
 	
 		
 		// find nouns
         myLogger.info("Learn singular-plural pair");
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
 						List<String> pair = myLearnerUtility.getWordFormUtility().getSingularPluralPair(word1, word2);
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
 		myLogger.debug("Nouns: "+nouns);
 				
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
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.addHeuristicsNouns.learnHeuristicsNouns.getPresentAbsentNouns");
 		
 		String pachecked = "and|or|to";
 
 //		if (text.matches("(\\w+?)\\s+(present|absent)")) {
 //			System.out.println(text);
 //		}
 
 		Matcher matcher = Pattern.compile("^.*?(\\w+?)\\s+(present|absent).*$")
 				.matcher(text);
 		if (matcher.lookingAt()) {
 			String word = matcher.group(1);
 			if ((!word.matches("\\b(" + pachecked + ")\\b"))
 					&& (!word.matches("\\b(" + Constant.STOP + ")\\b"))
 					&& (!word
 							.matches("\\b(always|often|seldom|sometimes|[a-z]+ly)\\b"))) {
 				
 				myLogger.trace("present/absent " + word);
 
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
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.addHeuristicsNouns.characterHeuristics");
 		
 		Set<String> taxonNames = new HashSet<String>();
 		Set<String> nouns = new HashSet<String>();
 		Set<String> anouns = new HashSet<String>();
 		Set<String> pnouns = new HashSet<String>();
 		Set<String> descriptors = new HashSet<String>();
 		Map<String, Boolean> descriptorMap = new HashMap<String, Boolean>();
 
 		int sent_num = this.myDataHolder.getSentenceHolder().size();
 		for (int i = 0; i < sent_num; i++) {
 
 			// taxon rule
 			SentenceStructure sent = this.myDataHolder.getSentenceHolder().get(i);
 			String source = sent.getSource();
 			String sentence = sent.getSentence();
 			String originalSentence = sent.getOriginalSentence();
 
 			myLogger.trace("Source: "+source);
 			myLogger.trace("Sentence: "+sentence);
 			myLogger.trace("Original Sentence: "+originalSentence);
 
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
 
 		this.getDataHolder().add2HeuristicNounTable(nouns, "organ");
 		this.getDataHolder().add2HeuristicNounTable(anouns, "acronyms");
 		this.getDataHolder().add2HeuristicNounTable(pnouns, "propernouns");
 		this.getDataHolder().add2HeuristicNounTable(taxonNames, "taxonnames");
 
 		nouns.addAll(anouns);
 		nouns.addAll(pnouns);
 		nouns.addAll(taxonNames);
 
 		List<Set<String>> results = new LinkedList<Set<String>>();
 		results.add(nouns);
 		results.add(descriptors);
 
 		return results;
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
 	public Set<String> filterOutDescriptors(Set<String> rNouns, Set<String> rDescriptors) {
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
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.addStopWords");
 		myLogger.trace("Add stop words");
 		
 		List<String> stops = new ArrayList<String>();
 		stops.addAll(Arrays.asList(Constant.STOP.split("\\|")));
 		stops.addAll(Arrays.asList(new String[] { "NUM", "(", "[", "{",
 				")", "]", "}", "d+" }));
 
 		myLogger.trace("Stop Words: " + stops);
 		for (int i = 0; i < stops.size(); i++) {
 			String word = stops.get(i);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateDataHolder(word, "b", "*", "wordpos", 0);
 			myLogger.trace(String.format("(\"%s\", \"b\", \"*\", \"wordpos\", 0) added\n", word));
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 0, 0, null, null));
 			// System.out.println("Add Stop Word: " + word+"\n");
 		}
 		myLogger.trace("Quite\n");
 	}
 
 	public void addCharacters() {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.addCharacters");
 		myLogger.trace("Add characters");		
 		
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
 			this.myDataHolder.updateDataHolder(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("", 0, 0, null, null));
 			// System.out.println("addCharacter word: " + word);
 		}
 	}
 
 	public void addNumbers() {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.addNumbers");
 		myLogger.trace("Add numbers");	
 		
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
 			this.myDataHolder.updateDataHolder(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 0, 0, null, null));
 			// System.out.println("add Number: " + word);
 		}
 		this.myDataHolder.updateDataHolder("NUM", "b", "*", "wordpos", 0);
 		// this.getWordPOSHolder().put(new WordPOSKey("NUM", "b"), new
 		// WordPOSValue("*",0, 0, null, null));
 	}
 
 	public void addClusterStrings() {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.addClusterstrings");
 		myLogger.trace("Add clusterstrings");	
 		
 		List<String> cltstrs = new ArrayList<String>();
 		cltstrs.addAll(Arrays.asList(Constant.CLUSTERSTRING.split("\\|")));
 
 //		System.out.println(cltstrs);
 //		System.out.println(Constant.CLUSTERSTRING);
 
 		for (int i = 0; i < cltstrs.size(); i++) {
 			String word = cltstrs.get(i);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateDataHolder(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 1, 1, null, null));
 			// System.out.println("addClusterString: " + word);
 		}
 	}
 
 	public void addProperNouns() {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.addProperNouns");
 		myLogger.trace("Add proper nouns");	
 		
 		List<String> ppnouns = new ArrayList<String>();
 		ppnouns.addAll(Arrays.asList(Constant.PROPERNOUN.split("\\|")));
 
 		for (int i = 0; i < ppnouns.size(); i++) {
 			String word = ppnouns.get(i);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateDataHolder(word, "b", "*", "wordpos", 0);
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
 //				boolean flag1 = 
 				posBySuffixCase1Helper(unknownWord);				
 //				boolean flag2 = 
 				posBySuffixCase2Helper(unknownWord);								
 			}
 		}
 		
 		myLogger.trace("Quite posBySuffix");
 	}
 	
 	/**
 	 * 
 	 * @param dh
 	 *            DataHolder handle to update the dataholder and return the
 	 *            updated dataholder
 	 * @return Number of records that have been changed
 	 */
 	public int resetCounts(DataHolder dh){
 		int count = 0;
 		Iterator<Entry<WordPOSKey, WordPOSValue>> iter = dh.getWordPOSHolderIterator();
 		while (iter.hasNext()) {
 			Entry<WordPOSKey, WordPOSValue> wordPOSObject = iter.next();
 			wordPOSObject.getValue().setCertiantyU(0);
 			wordPOSObject.getValue().setCertiantyL(0);
 			count++;
 		}
 		
 		return count;
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
 					this.myDataHolder.updateDataHolder(unknownWord, "b", "*", "wordpos", 0);							
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
 		WordNetPOSKnowledgeBase myWN = this.myLearnerUtility.getWordNetPOSKnowledgeBase();
 
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
 
 	public boolean markupByPatternHelper(SentenceStructure sentence) {
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
 
 	public boolean markupIgnoreHelper(SentenceStructure sentence) {
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
 		
 //		this.myDataHolder.printHolder(DataHolder.SENTENCE);
 		
 		for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 			SentenceStructure sentEntry = this.myDataHolder.getSentenceHolder().get(i);
 			// sentid
 			String thisSentence = sentEntry.getSentence();
 			String thisLead = sentEntry.getLead();
 			String thisTag = sentEntry.getTag();
 			String thisStatus = sentEntry.getStatus();
 			//if (!(thisTag == null || !thisTag.equals("ignore") 
 			
 			//myLogger.debug("Tag: "+thisTag);
 
 			
 			
 			if (    (!StringUtils.equals(thisTag, "ignore")
 					|| (thisTag == null))
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
 				
 				if (pattern != null) {
                     myLogger.debug("Build pattern ["+pattern+"] from starting words ["+thisLead+"]");
 					// IDs of untagged sentences that match the pattern
 					Set<Integer> matched = matchPattern(pattern, status, false);
 					int round = 0;
 					int numNew = 0;
 
 					do {
 						numNew = ruleBasedLearn(matched);
 						newDisc = newDisc + numNew;
 						myLogger.trace("Round: "+round);
 						round++;
 					} while (numNew > 0);
 				}
                 else {
                     myLogger.debug("Build no pattern from starting words ["+thisLead+"]");
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
 	public boolean isMarked(SentenceStructure sentence) {
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
 		Logger myLogger = Logger.getLogger("learn.discover.buildPattern");
 		
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
 		Logger myLogger = Logger.getLogger("learn.discover.matchPattern");
 		
 		myLogger.trace("Enter matchPattern");
 		myLogger.trace("Pattern: "+pattern);
 		myLogger.trace("Status: "+status);
 		myLogger.trace("HasTag: "+hasTag);
 		
 		Set<Integer> matchedIDs = new HashSet<Integer>();
 
 		for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 			SentenceStructure sent = this.myDataHolder.getSentenceHolder().get(i);
 			String thisSentence = sent.getSentence();
 			String thisStatus = sent.getStatus();
 			String thisTag = sent.getTag();
 			
 			boolean a = hasTag;
 			boolean b = (thisTag == null);
 			
 			if ((a ^ b) && (StringUtils.equals(status, thisStatus))) {
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
 		Logger myLogger = Logger.getLogger("learn.discover.ruleBasedLearn");
 		
 		myLogger.trace("Enter ruleBasedLearn");
 		myLogger.trace("Matched IDs: "+matched);
 		
 		int sign = 0;
 
 		Iterator<Integer> iter = matched.iterator();
 		while (iter.hasNext()) {
 			int sentID = iter.next().intValue();
 			SentenceStructure sentence = this.myDataHolder.getSentenceHolder().get(sentID);
 			if (!isMarked(sentence)) {
 				StringAndInt tagAndNew = null;
 				String tag = null;
 				int numNew = 0;
 				
 				tagAndNew = doIt(sentID); 
 				tag = tagAndNew.getString();
 				numNew = tagAndNew.getInt();
 				
 				this.tagSentence(sentID, tag);				
 				sign = sign + numNew;
 			}
 		}
 		
 		myLogger.trace("Return: "+sign);
 		myLogger.trace("Quit ruleBaseLearn");
 		myLogger.trace("\n");
 		
 		return sign;
 	}
 
 	/**
 	 * update wordpos table (on certainty) when a sentence is tagged for the
 	 * first time. 
 	 * Note: 
 	 * 1) this update should not be done when a pos is looked
 	 * up, because we may lookup a pos for the same example multiple times. 
 	 * 2) if the tag need to be adjusted (not by doit function), also need 
 	 * to adjust certainty counts.
 	 * 
 	 * @param sentID
 	 *            the ID of the sentence
 	 * @return a pair of (tag, sign)
 	 */
 	public StringAndInt doIt(int sentID) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.discover.ruleBasedLearn.doIt");
 		
 		myLogger.trace("Enter doIt");
 		myLogger.trace("sentence ID: " + sentID);
 		
 		SentenceStructure sentEntry = this.myDataHolder.getSentenceHolder().get(sentID);
 		String thisSentence = sentEntry.getSentence();
 		String thisLead = sentEntry.getLead();
 		
 		StringAndInt returnValue = this.doItCaseHandle(thisSentence, thisLead);
 		
 		myLogger.trace("Return Tag: " + returnValue.getString() + ", sign: " + returnValue.getInt());
 		myLogger.trace("Quit doIt");
 		myLogger.trace("\n");
 		
 		return returnValue;		
 	}
 
 	/**
 	 * 
 	 * @param thisSentence
 	 * @param thisLead
 	 * @return
 	 */
 	public StringAndInt doItCaseHandle(String thisSentence, String thisLead) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.discover.ruleBasedLearn.doIt.doItCaseHandle");
 
 		myLogger.trace("Enter doItCaseHandle");
 		myLogger.trace("Sentence: " + thisSentence);
 		myLogger.trace("Lead: " + thisLead);
 
 		if (thisSentence == null || thisLead == null) {
 			return null;
 		}
 
 		int sign = 0;
 		String tag = "";
 		
 		List<String> words = Arrays.asList(thisLead.split("\\s+"));		
 		String ptn = this.getPOSptn(words);		
 		myLogger.trace("ptn: "+ptn);
 		
 		Pattern p2 = Pattern.compile("ps");
 		Matcher m2 = p2.matcher(ptn);
 		
 		Pattern p3 = Pattern.compile("p(\\?)");
 		Matcher m3 = p3.matcher(ptn);
 		
 		Pattern p4 = Pattern.compile("[psn](b)");
 		Matcher m4 = p4.matcher(ptn);
 		
 		Pattern p5 = Pattern.compile("([psn][psn]+)");
 		Matcher m5 = p5.matcher(ptn);
 		
 		Pattern p6A = Pattern.compile("b[?b]([psn])$");
 		Matcher m6A = p6A.matcher(ptn);
 		
 		Pattern p6B = Pattern.compile("[?b]b([psn])$");
 		Matcher m6B = p6B.matcher(ptn);
 		
 		boolean case6A = m6A.find();
 		boolean case6B = m6B.find();
 		
 		Pattern p7 = Pattern.compile("^s(\\?)$");
 		Matcher m7 = p7.matcher(ptn);
 		
 		Pattern p10 = Pattern.compile("^\\?(b)");
 		Matcher m10 = p10.matcher(ptn);
 		
 
 		// Case 1: single word case
 		if (ptn.matches("^[pns]$")) {
 			myLogger.trace("Case 1");
 			tag = words.get(0);
 			sign = sign + this.myDataHolder.updateDataHolder(tag, ptn, "-", "wordpos", 1);
 			myLogger.debug("Directly markup with tag: "+tag+"\n");
 		}
 
 		// Case 2: "ps"
 		else if (m2.find()) {
 			myLogger.trace("Case 2");
 			myLogger.debug("Found [ps] pattern\n");
 			int start = m2.start();
 			int end = m2.end();
 			String pWord = words.get(start);
 			String sWord = words.get(end-1);
 			List<String> tempWords = StringUtility.stringArraySplice(words, 0, start+1);
 			tag = StringUtility.joinList(" ", tempWords);
 
 			myLogger.debug("\tdetermine the tag: "+tag);
 			
 			int returnedSign = 0;
 			returnedSign = this.myDataHolder.updateDataHolder(pWord, "p", "-", "wordpos", 1);
 			sign += returnedSign;
 			myLogger.trace(String.format("updateDataHolder(%s, p, -, wordpos, 1), returned: %d", pWord, returnedSign));
 			
 			returnedSign = this.myDataHolder.updateDataHolderNN(0, tempWords.size(), tempWords);
 			sign += returnedSign;
 			myLogger.trace(String.format("updateDataHolderNN(0, %d, %s), returned: %d", tempWords.size(), tempWords.toString(), returnedSign));
 			
 			returnedSign = this.myDataHolder.updateDataHolder(sWord, "b", "", "wordpos", 1);
 			sign += returnedSign;
 			myLogger.trace(String.format("updateDataHolder(%s, b, , wordpos, 1), returned: %d", sWord, returnedSign));
 		} 
 		
 		// Case 3: "p(\\?)"
 		else if (m3.find()) {
 			myLogger.trace("Case 3");
 			myLogger.debug("Found [p?] pattern");
 			
 //			int start = m3.start(1);
 			int end = m3.end(1);
 			
 			String secondMatchedWord = words.get(end-1);
 			
 			// case 3.1
 			if (StringUtils.equals(this.myLearnerUtility.getWordFormUtility().getNumber(secondMatchedWord), "p")) {
 				myLogger.trace("Case 3.1");
 				tag = secondMatchedWord;
 				sign = sign + this.myDataHolder.updateDataHolder(tag, "p", "-", "wordpos", 1);
 				this.myDataHolder.add2Holder(DataHolder.ISA, Arrays.asList(new String[] {tag, words.get(end-2)}));	
 				myLogger.debug("\t:[p p] pattern: determine the tag: "+tag);
 			}
 			// case 3.2
 			else {
 				myLogger.trace("Case 3.2");
 				
 				List<String> wordsCopy = new ArrayList<String>(words);
 				// $i is just end-1
 				List<String> tempWords = StringUtility.stringArraySplice(words, 0, end-1);
 				tag = StringUtility.joinList(" ", tempWords);
 				
 				myLogger.debug("\t:determine the tag: "+tag);
 				myLogger.debug("\t:updates on POSs");
 				
 				int temp = 0;
 				temp = this.myDataHolder.updateDataHolder(wordsCopy.get(end-1), "b", "", "wordpos", 1);
 				sign += temp;
 				myLogger.debug("\t:updateDataHolder1 returns " + temp);
 				
 				temp = this.myDataHolder.updateDataHolder(wordsCopy.get(end-2), "p", "-", "wordpos", 1);
 				sign += temp;
 				myLogger.debug("\t:updateDataHolder2 returns " + temp);
 				
 				temp = this.myDataHolder.updateDataHolderNN(0, tempWords.size(), tempWords);
 				sign += temp;
 				myLogger.debug("\t:updateDataHolder returns " + temp);
 			}
 		}
 		
 		// case 4: "[psn](b)"
 		else if (m4.find()) {
 			myLogger.trace("Case 4");
 			Pattern p41 = Pattern.compile("^sbp");
 			Matcher m41 = p41.matcher(ptn);
 			
 			if (m41.find()) {
 				myLogger.trace("\tCase 4.1");
 				myLogger.debug("Found [sbp] pattern");
 				List<String> wordsCopy = new ArrayList<String>(words);
 				tag = StringUtility.joinList(" ", StringUtility.stringArraySplice(wordsCopy, 0, 3));
 				myLogger.trace("\t:determine the tag: " + tag);
 			}
 			else {
 				myLogger.trace("\tCase 4.2");
 				myLogger.debug("Found [[psn](b)] pattern");
 
 				int index = m4.start(1);
 
 				// get tag, which is the words prior to the b word (exclusive)				
 				List<String> wordsTemp = StringUtility.stringArraySplice(words, 0, index);
 				tag = StringUtility.joinList(" ", wordsTemp);
 				myLogger.trace("Tag: " + tag);
 
 				// update the b word
 				sign += this.myDataHolder.updateDataHolder(words.get(index), "b", "", "wordpos", 1);
 				myLogger.trace(String.format("updateDataHolder (%s, b, , wordpos, 1)", words.get(index)));
 				
 				sign += this.myDataHolder.updateDataHolder(words.get(index - 1),
 						ptn.substring(index - 1, index), "-", "wordpos", 1);
 
 				myLogger.trace(String.format(
 						"updateDataHolder (%s, %s, -, wordpos, 1)",
 						words.get(index - 1), ptn.substring(index - 1, index)));
 
 				sign += this.myDataHolder.updateDataHolderNN(0, wordsTemp.size(),
 						wordsTemp);
 				myLogger.trace(String.format("updateDataHolderNN (0, %d, %s)",
 						wordsTemp.size(), wordsTemp.toString()));
 
 				myLogger.debug("\t:determine the tag: " + tag);
 				myLogger.debug("\t:updates on POSs");
 			}
 		}
 		
 		// case 5: "pp"
 		else if (m5.find()) {
 			myLogger.debug("Case 5: Found [[psn][psn]+] pattern");
 			int start = m5.start(1);
 			int end = m5.end(1);
 			List<String> copyWords = new ArrayList<String>();
 			copyWords.addAll(words);
 			GetNounsAfterPtnReturnValue returnedValue = this.getNounsAfterPtn(thisSentence, end);
 			List<String> moreNoun = new LinkedList<String>();
 			List<String> morePtn = new LinkedList<String>();
 			String bWord = "";
 			
 			moreNoun.addAll(returnedValue.getNouns());
 			morePtn.addAll(returnedValue.getNounPtn());
 			bWord = returnedValue.getBoundaryWord();
 			List<POSInfo> t;
 			
 			if (StringUtility.createMatcher(ptn, "pp").find()) {
 				myLogger.trace("Case 5.1");
 				
 				String morePtnStr = StringUtility.joinList("", morePtn);
 				Pattern p511 = Pattern.compile("/^p*(s)");
 				Matcher m511 = p511.matcher(morePtnStr);
 				Pattern p512 = Pattern.compile("^(p+)");
 				Matcher m512 = p512.matcher(morePtnStr);
 				
 				
 				if (m511.find()) {
 					myLogger.trace("Case 5.1.1");
 					// find last p word, and reset it to "b"
 					int sAfterPIndex = m511.start(1);
 					int lastPIndex = sAfterPIndex - 1;
 					String sWord = moreNoun.get(sAfterPIndex);
 					String lastPWord = lastPIndex >=0? moreNoun.get(lastPIndex):"";
 					bWord = lastPWord;
 					if (StringUtils.equals(lastPWord, "")) {
 						tag = words.get(ptn.lastIndexOf("p"));
 					}
 					else {
 						tag = lastPWord;
 					}
 					sign += this.getDataHolder().updateDataHolder(sWord, "b", "", "wordpos", 1);
 				}
 				else if (m512.find()) {
 					myLogger.trace("Case 5.1.2");
 					tag = moreNoun.get(m512.end(1)-1);
 				}
 				else {
 					myLogger.trace("Case 5.1.3");
 					int lastPIndex = ptn.lastIndexOf("p");
 					tag = words.get(lastPIndex);
 				}
 				t = this.getDataHolder().checkPOSInfo(tag);
 			}
 			else {
 				myLogger.trace("Case 5.2");
 				List<String> tempWords = new LinkedList<String>();
 				tempWords.addAll(StringUtility.stringArraySplice(words, 0, end));
 				tag = StringUtility.joinList(" ", tempWords);
 				if (moreNoun.size()>0) {
 					tag = tag + " "+ StringUtility.joinList(" ", moreNoun);
 				}
 				
 				t = this.getDataHolder().checkPOSInfo(tag.substring(tag.lastIndexOf(" ")+1, tag.length()));
 			}
 			
 			if (t.size() > 0) {
 				String pos = t.get(0).getPOS();
 //				String role = t.get(0).getRole();
 //				int certiantyU = t.get(0).getCertaintyU();
 //				int certiantyL = t.get(0).getCertaintyL();
 
 				if (StringUtility.createMatcher(pos, "[psn]").find()) {
 					// case 5.x
 					myLogger.debug("Case 5.x: relax this condition");
 					List<String> tWords = new LinkedList<String>();
 					tWords.addAll(Arrays.asList(thisSentence.split(" ")));
 					sign += this.getDataHolder().updateDataHolder(bWord, "b",
 							"", "wordpos", 1);
 					ptn = ptn.substring(start, end);
 					String tempPtn = ptn + StringUtility.joinList("", morePtn);
 					for (int k = start; k < tempPtn.length(); k++) {
 						if (k != tempPtn.length() - 1) {
 							sign += this.getDataHolder().updateDataHolder(
 									tWords.get(k), tempPtn.substring(k, k + 1),
 									"_", "wordpos", 1);
 						} else {
 							sign += this.getDataHolder().updateDataHolder(
 									tWords.get(k), tempPtn.substring(k, k + 1),
 									"-", "wordpos", 1);
 						}
 					}
 					if (tWords.size() > 1) {
 						sign += this.getDataHolder().updateDataHolderNN(0,
 								tempPtn.length(), tWords);
 					}
 				}
 			}
 			myLogger.debug("\t:determine the tag: "+tag);
 			
 		}
 		
 		// case 6: "b[?b]([psn])$" or "[?b]b([psn])$"
 		else if (case6A || case6B) {
 			myLogger.debug("Case 6: Found [b?[psn]$] or [[?b]b([psn])$] pattern");
 			int end = -1;
 			// the index of noun
 			if (case6A) {
 				end = m6A.end(1)-1;
 			}
 			else {
 				end = m6B.end(1)-1;
 			}
 			GetNounsAfterPtnReturnValue tempReturnValue = this
 					.getNounsAfterPtn(thisSentence, end + 1);
 //			List<String> moreNouns = tempReturnValue.getNouns();
 			List<String> morePtn = tempReturnValue.getNounPtn();
 			String bWord = tempReturnValue.getBoundaryWord();
 
 			List<String> sentenceHeadWords = this.getLearnerUtility()
 					.tokenizeText(thisSentence, "firstseg");
 			end += morePtn.size();
 			List<String> tempWords = StringUtility.stringArraySplice(
 					sentenceHeadWords, 0, end + 1);
 			tag = StringUtility.joinList(" ", tempWords);
 			myLogger.debug("\t:updates on POSs");
 			if (StringUtility.createMatcher(bWord, "\\w").find()) {
 				sign += this.getDataHolder().updateDataHolder(bWord, "b", "",
 					"wordpos", 1);
 			}
 			String allPtn = "" + ptn;
 			allPtn = allPtn + StringUtility.joinList("", morePtn);
 			// from the index of noun
 			for (int i = 2; i < allPtn.length(); i++) {
 				// case 6.1: last ptn
 				if (i != allPtn.length() - 1) {
 					myLogger.trace("Case 6.1");
 					sign += this.getDataHolder().updateDataHolder(
 							sentenceHeadWords.get(i),
 							allPtn.substring(i, i + 1), "_", "wordpos", 1);
 				} 
 				// case 6.2: not last ptn
 				else {
 					myLogger.trace("Case 6.2");
 					sign += this.getDataHolder().updateDataHolder(
 							sentenceHeadWords.get(i),
 							allPtn.substring(i, i + 1), "-", "wordpos", 1);
 				}
 			}
 			myLogger.debug("\t:determine the tag: " + tag);
 		}
 		
 		
 		// case 7: "^s(\\?)$"
 		else if (m7.find()) {
 			myLogger.trace("Case 7");
 			String singularWord = words.get(0);
 			String questionedWord = words.get(1);
 			String wnPOS = this.myLearnerUtility.getWordFormUtility().checkWN(questionedWord, "pos");
 			
 			if (StringUtility.createMatcher(wnPOS, "p").find()){
 				myLogger.trace("Case 7.1");
 				tag = singularWord+" "+questionedWord;
 				myLogger.debug("\t:determine the tag: "+tag);
 				myLogger.debug("\t:updates on POSs");
 				String questionedPOS = this.getLearnerUtility().getWordFormUtility().getNumber(singularWord);
 				sign += this.getDataHolder().updateDataHolder(questionedWord, questionedPOS, "-", "wordpos", 1);
 			}
 			else {
 				myLogger.trace("Case 7.2");
 				tag = words.get(0);
 				myLogger.debug("\t:determine the tag: "+tag);
 				myLogger.debug("\t:updates on POSs");
 				sign += this.getDataHolder().updateDataHolder(questionedWord, "b", "", "wordpos", 1);
 				sign += this.getDataHolder().updateDataHolder(singularWord, "s", "-", "wordpos", 1);
 			}
 		}
 		
 		// case 8: "^bs$"
 		else if (StringUtility.createMatcher(ptn, "^bs$").find()) {
 			myLogger.trace("Case 8");
 			tag = StringUtility.joinList(" ", words);
 			sign += this.getDataHolder().updateDataHolder(words.get(0), "b", "", "wordpos", 1);
 			sign += this.getDataHolder().updateDataHolder(words.get(1), "s", "-", "wordpos", 1);
 		}
 		
 		// case 9: ^bp$
 		else if (StringUtility.createMatcher(ptn, "^bp$").find()) {
 			myLogger.trace("Case 9");
 			tag = StringUtility.joinList(" ", words);
 			sign += this.getDataHolder().updateDataHolder(words.get(0), "b", "", "wordpos", 1);
 			sign += this.getDataHolder().updateDataHolder(words.get(1), "p", "-", "wordpos", 1);
 		} 
 		
 		// case 10: "^\\?(b)"
 		else if (m10.find()) {
 			myLogger.trace("Case 10");
 			myLogger.trace("Found [?(b)] pattern");
 			
 			int index = m10.start(1);
 
 			sign += this.myDataHolder.updateDataHolder(words.get(index), "b", "", "wordpos", 1);
 			myLogger.trace(String.format("updateDataHolder (%s, b, , wordpos, 1)", words.get(index)));
 
 			List<String> wordsTemp = StringUtility.stringArraySplice(words, 0, index);
 			tag = StringUtility.joinList(" ", wordsTemp);
 			String word = words.get(index-1); // the "?" word
 			
 			myLogger.trace("Tag: "+tag);
 			myLogger.trace("Word: "+word);
 			
 			if (!isFollowedByNoun(thisSentence, thisLead)) {
 				myLogger.trace("Case 10.1");
 				String wnP1 = this.myLearnerUtility.getWordFormUtility().checkWN(word, "pos");
 				myLogger.trace("wnP1: "+wnP1);
 				String wnP2 = "";
 						
 				if (!StringUtility.createMatcher(wnP1, "\\w").find()) {
 					wnP2 = this.myLearnerUtility.getWordFormUtility().getNumber(word);	
 				}
 				myLogger.trace("wnP2: "+wnP2);
 				
 				if (StringUtility.createMatcher(wnP1, "[ar]").find()) {
 					wnP1 = "";
 				}
 				
 				if ((StringUtility.createMatcher(wnP1, "[psn]").find())
 						|| (StringUtility.createMatcher(wnP2, "[ps]").find())) {
 					myLogger.trace("Case 10.1.1");	
                     myLogger.debug("\t:determine the tag: "+tag);
                     myLogger.debug("\t:updates on POSs");
                     sign += this.myDataHolder.updateDataHolder(word, "n", "-", "wordpos", 1);
                     myLogger.trace(String.format("updateDataHolder(%s, n, -, wordpos, 1)", word));
                     sign += this.myDataHolder.updateDataHolderNN(0, wordsTemp.size()-1, wordsTemp);
                     myLogger.trace(String.format("updateDataHolderNN(%d, %d, %s)", 0, wordsTemp.size()-1, wordsTemp));
 					
 				}
 				else{
 					myLogger.trace("Case 10.1.2");
 					myLogger.debug("\t:"+tag+" is adv/adj or modifier. skip.");
 					tag = "";
 				}	
 			}
 			else {
 				myLogger.trace("Case 10.2");
 				myLogger.debug(String.format("\t:%s is adv/adj or modifier. skip.", tag));
                 tag = "";
 			}
 		}
 		else {
 			myLogger.trace("\tCase 0");
 			myLogger.trace(String.format("Pattern [%s] is not processed", ptn));
 		}
 		
 		StringAndInt returnValue = new StringAndInt(tag,sign);
 		
 		myLogger.trace("Return: "+returnValue.toString());
 		return returnValue;
 	}
 
 	public int doItCase7Helper(String regex, String ptn) {
 		Matcher m = StringUtility.createMatcher(ptn, regex);
 		if (m.find()) {
 			int start = m.start();
 			return start + 1;
 		} else {
 			return -1;
 		}
 	}
 	
 	public GetNounsAfterPtnReturnValue getNounsAfterPtn(String sentence, int startWordIndex){
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.getNounsAfterPattern");
 		myLogger.trace(String.format("enter (%s, %d)", sentence, startWordIndex));
 		
 		String bWord = "";
 		List<String> nouns = new ArrayList<String>();
 		List<String> nounPtn = new ArrayList<String>();
 		
 		List<String> tempWords = new ArrayList<String>();
 		tempWords.addAll(this.getLearnerUtility().tokenizeText(sentence, "firstseg"));
 		List<String> words = StringUtility.stringArraySplice(tempWords, startWordIndex, tempWords.size());
 		myLogger.trace("words: "+words);
 		String ptn = this.getPOSptn(words);
 		myLogger.trace("ptn: " + ptn);
 		
 		if (ptn!=null) {
 			Matcher m1 = StringUtility.createMatcher(ptn, "^([psn]+)");
 			Matcher m2 = StringUtility.createMatcher(ptn, "^(\\?+)");
 			boolean case1 = false;
 			boolean case2 = false;
 			int end = -1;
 			if (m1.find()) {
 				case1 = true;
 				end = m1.end(1);
 			}
 			if (m2.find()) {
 				case2 = true;
 				end = m2.end(1);
 			}
 			if (case1 || case2) {
 				myLogger.trace("end: " + end);
 				if (end<words.size()) {
 					bWord = words.get(end);
 				}
 				List<String> nWords = new ArrayList<String>();
 				nWords.addAll(StringUtility.stringArraySplice(words, 0, end));
 				for (int i=0;i<nWords.size();i++) {
 					String p = ptn.substring(i, i+1);
 					p = StringUtils.equals(p, "?") ? 
 							this.getLearnerUtility().getWordFormUtility().checkWN(nWords.get(i), "pos")
 							: p;
 					if (StringUtility.createMatcher(p, "^[psn]+$").find()) {
 						nouns.add(nWords.get(i));
 						nounPtn.add(p);
 					}
 					else {
 						bWord = nWords.get(i);
 						break;
 					}
 				}
 			}
 		}
 		
 		GetNounsAfterPtnReturnValue returnValue = new GetNounsAfterPtnReturnValue(nouns, nounPtn, bWord);
 		myLogger.trace("return " + returnValue);
 		return (returnValue);
 	}
 
 	/**
 	 * Check if a lead is followed by a noun without any proposition in between
 	 * in the sentence
 	 * 
 	 * @param thisSentence
 	 *            the sentence
 	 * @param thisLead
 	 *            the lead
 	 * @return true if lead is followed by a N without any proposition in
 	 *         between
 	 */
 	public boolean isFollowedByNoun(String sentence, String lead) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.discover.ruleBasedLearn.doIt.isFollowedByNoun");
 		myLogger.trace(String.format("(%s, %s)", sentence, lead));
 		
 		// null case
 		if (sentence == null || lead == null) {
 			myLogger.trace("Return false");
 			return false;
 		}
 		
 		if (StringUtils.equals(sentence, "")) {
 			myLogger.trace("Return false");
 			return false;
 		}
 		
 		// remove lead from sentence
 		sentence = sentence.replaceFirst("^"+lead, "");
 		myLogger.trace("Sentence after remove lead: "+sentence);
 		
 		//List<String> nouns = this.myDataHolder.getWordByPOS("ps");
 		Set<String> POSTags = new HashSet<String>();
 		POSTags.add("p");
 		POSTags.add("s");
 		Set<String> nouns = this.myDataHolder.getWordsFromWordPOSByPOSs(POSTags);
 		
 		if (nouns.size()==0) {
 			myLogger.trace("Return false");
 			return false;
 		}
 		
 //		String pattern1 = StringUtility.joinList("|", nouns);		
 		String pattern1 = StringUtils.join(nouns, "|");
 		
 		
 		pattern1 = "(.*?)\\b("+pattern1+")"+"\\b";
 		myLogger.trace("Pattern: " + pattern1);
 		
 		Pattern p1 = Pattern.compile(pattern1);
 		Matcher m1 = p1.matcher(sentence);
 
 		String inBetweenPart = "";
 		if (m1.find()) {
 			inBetweenPart = m1.group(1);
 
 			String pattern2 = "\\b(" + Constant.PREPOSITION + ")\\b";
 			Pattern p2 = Pattern.compile(pattern2);
 			Matcher m2 = p2.matcher(inBetweenPart);
 			if (!m2.find()) {
 				myLogger.trace("Return true");
 				return true;
 			}
 		}
 		myLogger.trace("Return false");
 		return false;
 	}
 
 	/**
 	 * The length of the ptn must be the same as the number of words in words.
 	 * If certainty is < 50%, replace POS with ?.
 	 * 
 	 * @param words
 	 * @return
 	 */
 	public String getPOSptn(List<String> words) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger
 				.getLogger("learn.discover.ruleBasedLearn.doIt.getPOSptn");
 
 		myLogger.trace("Enter getPOSptn");
 		myLogger.trace("Words: " + words.toString());
 
 		String ptn = "";
 		String POS = "";
 		double certainty;
 		for (int i = 0; i < words.size(); i++) {
 
 			String word = words.get(i);
 			myLogger.trace("\tCheck word: " + word);
 			List<POSInfo> POSInfoList = this.myDataHolder.checkPOSInfo(word);
 			if (POSInfoList.size() >= 0) {
 				if (POSInfoList.size() == 0) {
                     myLogger.trace("\t\tThe word is not in WordPOS holder");
 					POS = "?";
 				} 
 				else {
 					POSInfo p = POSInfoList.get(0);
 					POS = p.getPOS();
 
 					if (p.getCertaintyU() == 0) {
 						certainty = 1.0;
 					} else {
 						double certaintyU = (double) p.getCertaintyU();
 						double certaintyL = (double) p.getCertaintyL();
 						certainty = certaintyU / certaintyL;
 					}
 
 					myLogger.trace(String.format("\t\tCertaintyU: %d",
 							p.getCertaintyU()));
 					myLogger.trace(String.format("\t\tCertaintyL: %d",
 							p.getCertaintyL()));
 					myLogger.trace(String
 							.format("\t\tCertainty: %f", certainty));
 					if ((!StringUtils.equals(POS, "?"))
 							&& (certainty <= 0.5)) {
 						myLogger.info("\t\tThis POS has a certainty less than 0.5. It is ignored.");
 						POS = "?";
 					}
 
 				}
 				ptn = ptn + POS;
 				myLogger.trace("\t\tAdd pos: " + POS);
 			} else {
 				myLogger.error("Error: checkPOSInfo gave invalid return value");
 			}
 		}
 
 		myLogger.trace("Return ptn: " + ptn);
 		myLogger.trace("Quite getPOSptn");
 
 		return ptn;
 	}
 
 	/**
 	 * 
 	 */
 	public void additionalBootstrapping(){
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.additionalBootStrapping");
 		myLogger.trace("[additionalBootStrapping]Start");
 
 //		this.myDataHolder.printHolder(DataHolder.SENTENCE);
 		
 		int flag = 0;
 		
 		do {
 			myLogger.trace(String.format("Enter one do-while loop iteration"));
 			flag = 0;
 
 			// warmup markup
 			int cmReturn = wrapupMarkup();
 			myLogger.trace(String
 					.format("wrapupMarkup() returned %d", cmReturn));
 			flag += cmReturn;
 
 			// one lead word markup
 			Set<String> tags = myDataHolder.getCurrentTags();
 			myLogger.trace(tags.toString());
 			int omReturn = oneLeadWordMarkup(tags);
 			myLogger.trace(String.format("oneLeadWordMarkup() returned %d",
 					omReturn));
 			flag += omReturn;
 
 			// doit markup
 			int dmReturn = this.doItMarkup();
 			myLogger.trace(String.format("doItMarkup() returned %d", dmReturn));
 			flag += dmReturn;
 
 			myLogger.trace(String.format("Quite this iteration with flag = %d",
 					flag));
 		} while (flag > 0);
 		
 		myLogger.trace("[additionalBootStrapping]End");
 	}
 	
 	/**
 	 * In the sentence collections, search for such sentence, whose lead is
 	 * among the tags passed in, and add the lead into word POS collections as a
 	 * noun
 	 * 
 	 * @param tags
 	 *            a set of all tags in the tagged sentences in the sentence
 	 *            collection
 	 * @return the numbet of updates made
 	 */
 	public int oneLeadWordMarkup(Set<String> tags) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger
 				.getLogger("learn.additionalBootStrapping.oneLeadWordMarkup");
 		//String tags = StringUtility.joinList("|", tags);
 		int sign = 0;
 		myLogger.trace(String.format("Enter (%s)", tags));
 
 		Iterator<SentenceStructure> iter = this.myDataHolder.getSentenceHolder().iterator();
 
 		while (iter.hasNext()) {
 			SentenceStructure sentence = iter.next();
 			int ID = sentence.getID();
 			String tag = sentence.getTag();
 			String lead = sentence.getLead();
 
 			if ((tag == null) && (!(StringUtility.createMatcher(lead, ".* .*").find()))) {
 				if (tags.contains(lead)) {
 					this.tagSentence(ID, lead);
 					myLogger.trace(String.format(
 							"updateDataHolder(%s, n, -, wordpos, 1)", lead));
 					sign += myDataHolder.updateDataHolder(lead, "n", "-",
 							"wordpos", 1);
 				}
 			}
 		}
 
 		myLogger.trace("Return: " + sign);
 		return 0;
 	}
 	
 	/**
 	 * for the remaining of sentences that do not have a tag yet, look for lead
 	 * word co-ocurrance, use the most freq. co-occured phrases as tags 
 	 *   e.g. plication induplicate (n times) and plication reduplicate (m times) =>
 	 *     plication is the tag and a noun 
 	 *   e.g. stigmatic scar basal (n times) and stigmatic scar apical (m times) => 
 	 *     stigmatic scar is the tag and scar is
 	 * a noun. what about externally like A; externally like B, functionally
 	 * staminate florets, functionally staminate xyz?
 	 * 
 	 * @return
 	 */
 	public int wrapupMarkup() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.additionalBootStrapping.wrapupMarkup");
 		myLogger.trace("Enter");
 		
 		int sign = 0;
 		Set<Integer> checkedIDs = new HashSet<Integer>();
 		List<SentenceStructure> sentenceList = new LinkedList<SentenceStructure>();
 		
 		for (int id1 = 0; id1 < this.myDataHolder.getSentenceHolder().size(); id1++) {
 			SentenceStructure sentence = this.myDataHolder.getSentenceHolder().get(id1);
 			String tag = sentence.getTag();
 			String lead = sentence.getLead();
 
 			if ((tag == null)
 					&& (StringUtility.createMatcher(lead, ".* .*").find())) {
 				sentenceList.add(sentence);
 			}
 		}
 		
 		SentenceLeadLengthComparator myComparator = new SentenceLeadLengthComparator(
 				false);
 		Collections.sort(sentenceList, myComparator);
 
 		Iterator<SentenceStructure> iter1 = sentenceList.iterator();
 		while (iter1.hasNext()){
 			SentenceStructure sentence = iter1.next();
 			int ID1 = sentence.getID();
 			String lead = sentence.getLead();
 			// if this sentence has been checked, pass
 			if (checkedIDs.contains(ID1)) {
 				continue;
 			}
 			
 			List<String> words = new ArrayList<String>();
 			words.addAll(Arrays.asList(lead.split("\\s+")));
 
 			List<String> sharedHead = new ArrayList<String>();
 			sharedHead.addAll(words.subList(0, words.size()-1));
 			String match = StringUtility.joinList(" ", sharedHead);
 			
 			Set<SentenceStructure> sentenceSet = new HashSet<SentenceStructure>();
 			for (int index=0;index<this.myDataHolder.getSentenceHolder().size();index++) {
 				SentenceStructure thisSentence = this.myDataHolder.getSentenceHolder().get(index);
 				String thisLead = thisSentence.getLead();
 				String tag = thisSentence.getTag();
 				String pTemp = "^"+match + " [\\S]+$";
 				myLogger.trace(thisLead);
 				myLogger.trace(pTemp);
 				
 //				if ((tag==null) && StringUtility.isMatchedNullSafe(pTemp, thisLead)) {
 				if ((tag==null) && StringUtility.isMatchedNullSafe(thisLead, pTemp)) {
 					if (!StringUtils.equals(thisLead, lead)) {
 						sentenceSet.add(thisSentence);
 					}
 				}
 			}
 			
 			if (sentenceSet.size() > 1) {
 				String ptn = this.getPOSptn(sharedHead);
 				String wnPOS = this.myLearnerUtility.getWordFormUtility().checkWN(
 						sharedHead.get(sharedHead.size() - 1), "pos");
 				
 				myLogger.trace("ptn: " + ptn);
 				myLogger.trace("wnPOS: " + wnPOS);
 
 				if ((StringUtility.createMatcher(ptn, "[nsp]$").find())
 						|| ((StringUtility.createMatcher(ptn, "\\?$").find()) && (StringUtility
 								.createMatcher(wnPOS, "n").find()))) {
 
 					Iterator<SentenceStructure> iter2 = sentenceSet.iterator();
 					while (iter2.hasNext()) {
 						SentenceStructure thisSentence = iter2.next();
 						int ID = thisSentence.getID();
 						String thisLead = thisSentence.getLead();
 
 						List<String> words2 = new ArrayList<String>();
 						words2.addAll(Arrays.asList(thisLead.split("\\s+")));
 
 						// case 1
 						boolean case1 = false;
 						boolean case2 = false;
 						case1 = words2.size() > sharedHead.size();
 						if (case1) {
 							List<String> checkWord = new ArrayList<String>();
 							checkWord.add(words2.get(sharedHead.size()));
 							case2 = StringUtility.createMatcher(
 									this.getPOSptn(checkWord), "[psn]").find();
 						}
 
 						if (case1 && case2) {
 							myLogger.trace("Case 1");
 							String nb = words2.size() >= sharedHead.size() + 2 ? 
 									words2.get(sharedHead.size() + 1) : "";
 							words2 = 
 									StringUtility.stringArraySplice(words2, 0, sharedHead.size() + 1);
 							String nmatch = StringUtility.joinList(" ", words2);
 
 							this.tagSentence(ID, nmatch);
 							myLogger.trace(String.format("tag (%d, %s)", ID, nmatch));
 							this.tagSentence(ID1, match);
 							myLogger.trace(String.format("tag (%d, %s)", ID1, match));
 
 							String updatedWord = words2.get(words2.size() - 1); 
 							int update1 = this.myDataHolder.updateDataHolder(
 									updatedWord, "n", "-", "wordpos", 1);
 							sign += update1;
 							myLogger.trace(String.format("update (%s)", updatedWord));
 							
 							if (!StringUtils.equals(nb, "")) {
 								int update2 = 
 										this.myDataHolder.updateDataHolder(nb, "b", "", "wordpos", 1);
 								sign += update2;
 								myLogger.trace(String.format("update (%s)", nb));
 							}
 							
 							updatedWord = words.get(words.size() - 1);
 							int update3 = this.myDataHolder.updateDataHolder(
 									words.get(words.size() - 1), "b", "", "wordpos", 1);
 							sign += update3;
 							myLogger.trace(String.format("update (%s)", updatedWord));
 						}
 						// case 2
 						else {
 							myLogger.trace("Case 2");
 							String b = words2.size() >= sharedHead.size() + 1 ? words2
 									.get(sharedHead.size()) : "";
 
 							this.tagSentence(ID, match);
 							this.tagSentence(ID1, match);
 
 //							if (sharedHead.get(sharedHead.size() - 1).equals("tissue")) {
 //								System.out.println();
 //							}
 							
 							int update1 = this.myDataHolder.updateDataHolder(
 									sharedHead.get(sharedHead.size() - 1), "n", "-", "wordpos", 1);
 							sign += update1;
 							if (!StringUtils.equals(b, "")) {
 								int update2 = this.myDataHolder
 										.updateDataHolder(b, "b", "", "wordpos", 1);
 								sign += update2;
 							}
 							int update3 = this.myDataHolder.updateDataHolder(
 									words.get(words.size() - 1), "b", "", "wordpos", 1);
 							sign += update3;
 
 						}
 						checkedIDs.add(ID);
 					}
 				} else {
 					Iterator<SentenceStructure> iter2 = sentenceSet.iterator();
 					while (iter2.hasNext()) { 
 						SentenceStructure thisSentence = iter2.next();
 						int ID = thisSentence.getID();
 						checkedIDs.add(ID);
 					}
 				}
 			} else {
 				checkedIDs.add(ID1);
 			}
 		}
 
 		myLogger.trace("Return " + sign);
 		return sign;
 	}
 	
 	/**
 	 * check if the lead has the head in the beginning of it
 	 * 
 	 * @param head
 	 * @param lead
 	 * @return true if it has, false if it does not have
 	 */
 	public boolean hasHead(List<String> head, List<String> lead) {
 		
 		// null case
 		if (head == null || lead == null) {
 			return false;
 		}
 		
 		int headSize = head.size();
 		int leadSize = lead.size();
 		if (headSize > leadSize) {
 			return false;
 		}
 		
 		for (int i=0;i<headSize;i++) {
 			if (!StringUtils.equals(head.get(i), lead.get(i))) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * skip and/or cases
 	 * skip leads with $stop words
 	 * @return number of updates
 	 */
 	public int doItMarkup() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.additionalBootStrapping.doItMarkup");
 		myLogger.trace("Enter");
 
 		int sign = 0;		
 //		for (int i=0;i<myDataHolder.getSentenceHolder().size();i++) {			
 		Iterator<SentenceStructure> iter = this.myDataHolder.getSentenceHolder().iterator();
 		while(iter.hasNext()) {
 			SentenceStructure sentenceObject = iter.next();
 			String tag = sentenceObject.getTag();
 			if (doItMarkupHelper(tag)) {
 				int ID = sentenceObject.getID();
 				String lead = sentenceObject.getLead();
 				String sentence = sentenceObject.getSentence();
 				
                 // case 1
 				if (doItMarkupCase1Helper(sentence)) {
                     myLogger.trace(String.format("sent #%d: case 1", ID));
 					continue;
 				}
 				
                 // case 2
 				if (doItMarkupCase2Helper(lead)) {
 					myLogger.trace(String.format("sent #%d: case 2", ID));
 					continue;
 				}
 				
 				StringAndInt tagAndSign = doIt(ID);
 				String doItTag = tagAndSign.getString();
 				int doItSign = tagAndSign.getInt();
 				sign = doItSign;
                 
                 // case 3
 				if (StringUtility.createMatcher(doItTag, "\\w").find()) {
 					myLogger.trace(String.format("sent #%d: case 3", ID));
 					this.tagSentence(ID, doItTag);
 				}
 			}
 		}
 		
 		myLogger.trace("Return: "+sign);
 		return sign;
 	}
 	
 	public boolean doItMarkupHelper(String tag){
 		boolean flag = false;
 		flag = (tag == null) 
 				|| (StringUtils.equals(tag, ""))
 				|| (StringUtils.equals(tag, "unknown"));
 		
 		return flag;
 	}
 	
 	public boolean doItMarkupCase1Helper(String sentence) {
 		boolean flag = false;
 		flag = StringUtility.createMatcher(sentence, "^.{0,40} (nor|or|and|\\/)").find();
 		return flag;
 	}
 	
 	public boolean doItMarkupCase2Helper(String lead) {
 		boolean flag = false;
 		flag = StringUtility.createMatcher(lead, "\\b("+Constant.STOP+")\\b").find();
 		
 		return flag;
 	}
 	
 	public void unknownWordBootstrapping() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.unknownWordBootstrapping");
 		myLogger.trace("[unknownWordBootstrapping]Start");
 		
 		unknownWordBootstrappingPreprocessing();
 		unknownWordBootstrappingMain();
 		unknownWordBootstrappingPostprocessing();
 		
 		myLogger.trace("[unknownWordBootstrapping]End");
 	}
 	
 	public void unknownWordBootstrappingPreprocessing() {
 		this.myLearnerUtility.tagAllSentences(this.myDataHolder, "singletag", "sentence");
 	}
 	
 	public void unknownWordBootstrappingMain() {
 		String plMiddle = "(ee)";
 		
 		int newInt = 0;
 		do {
 //			this.unknownWordBootstrappingGetUnknownWord(plMiddle);
 		} while (newInt > 0);
 	}
 
 	public void unknownWordBootstrappingPostprocessing() {
 		// pistillate_zone
 		// get all nouns from wordPOS holder
 		Set<String> POSTags = new HashSet<String>();
 		POSTags.add("p");
 		POSTags.add("s");
 		Set<String> nouns = this.getDataHolder().getWordsFromWordPOSByPOSs(
 				POSTags);
 		
 		// get boudaries
 		Set<String> boundaries = new HashSet<String>();
 		Set<String> words = this.getDataHolder()
 				.getWordsFromUnknownWord("^.*_.*$", true,
 						"^unknown$", true);
 		Iterator<String> wordIter = words.iterator();
 		String pattern = "_(" + StringUtils.join(nouns, "|") + ")$";
 		while (wordIter.hasNext()) {
 			String word = wordIter.next();
 			Pattern p1 = Pattern.compile("^[a-zA-Z0-9_-]+$");
 			Matcher m1 = p1.matcher(word);
 			Pattern p2 = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
 			Matcher m2 = p2.matcher(word);
 			if (m1.matches() && (!m2.matches())) {
 				if (!StringUtility.createMatcher(word,
 						"\\b(" + Constant.FORBIDDEN + ")\\b").find()) {
 					boundaries.add(word);
 				}
 				this.getDataHolder().updateDataHolder(word, "b", "", "wordpos", 1);
 			}
 		}
 		
 		// if the boundaries is not empty
 		if (boundaries.size() > 0) {
 			Iterator<SentenceStructure> iter = this.getDataHolder()
 					.getSentenceHolderIterator();
 			while (iter.hasNext()) {
 				SentenceStructure sentenceItem = iter.next();
 				String tag = sentenceItem.getTag();
 				String sentence = sentenceItem.getSentence();
 				int sentenceID = sentenceItem.getID();
 
 				if ((!(StringUtils.equals(tag, "ignore")) || (tag == null))
 						&& (StringUtility.createMatcher(sentence, "(^| )("
 								+ StringUtils.join(boundaries, "|") + ") ")
 								.find())) {
 					KnownTagCollection tags = new KnownTagCollection(null,
 							null, null, boundaries, null, null);
 					sentence = this.myLearnerUtility.annotateSentence(sentence, tags, this.myDataHolder.getBMSWords());
 					SentenceStructure updatedSentence = this.getDataHolder()
 							.getSentence(sentenceID);
 					updatedSentence.setSentence(sentence);
 				}
 			}
 		}
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
 	
 
 	
 	public boolean tagSentence(int sentenceID, String tag) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.tagSentence");
 		myLogger.trace(String.format("Enter (%d, %s)", sentenceID, tag));
 		
 		// case 1
 		if (!StringUtility.createMatcher(tag, "\\w+").find()) {
 			myLogger.trace("\t:tag is not a word. Return");
 			return false;
 		} else {
 			// case 2
 			if (StringUtility
 					.createMatcher(tag, "^(" + Constant.STOP + ")\\b").find()) {
 				myLogger.trace(String
 						.format("\t:tag %s starts with a stop word, ignore tagging requrest",
 								tag));
 				return false;
 			} else {
 				// case 3
 				int maxLength = this.myConfiguration.getMaxTagLength();
 				if (tag.length() > maxLength) {
 					maxLength = this.myConfiguration.getMaxTagLength();
 					tag = tag.substring(0, maxLength);
 					myLogger.debug(String.format("\ttag: %s longer than %d)",
 							tag, maxLength));
 				} else {
 					;
 				}
 				SentenceStructure sentence = myDataHolder.getSentence(sentenceID);
 				sentence.setTag(tag);
 				myLogger.debug(String.format(
 						"\t:mark up sentence #%d with tag %s", sentenceID, tag));
 				return true;
 			}
 		}
 	}
 	
 	/**
 	 * correct markups that used an adj as an s, e.g lateral, adult, juvenile
 	 */
 	public void adjectivesVerification(DataHolder dataholderHandler) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.adjectivesVerification");
 		
 		String pattern = "^<N>([a-z]+)</N> ([^N,;.]+ <N>[a-z]+</N>)";
 		Iterator<SentenceStructure> iter = dataholderHandler
 				.getSentenceHolderIterator();
 		while (iter.hasNext()) {
 			SentenceStructure sentenceItem = iter.next();
 			String sentence = sentenceItem.getSentence();
 //			System.out.println(sentence);
 			Set<Integer> ids = new HashSet<Integer>();
 			ids.add(133);
 			ids.add(163);
 			ids.add(236);
 			ids.add(269);
 			
 			if (ids.contains(sentenceItem.getID())) {
 				System.out.println();
 			}
 			
 			
 			if (sentence != null) {
 				Pattern p = Pattern.compile(pattern);
 				Matcher m = p.matcher(sentence);
 				if (m.find()) {
 					String part1 = m.group(1);
 					String part2 = m.group(2);
 					myLogger.trace(String.format("Sentence %s\n" +
 							"\tSentence: %s\n" +
 							"\tPart1: %s\n" +
 							"\tPart2: %s", sentenceItem.getID(), sentenceItem.getSentence(), part1, part2));
 					boolean condition1 = this.isSentenceTag(dataholderHandler,
 							part2);
 					boolean condition2 = StringUtils.equals(this
 							.getLearnerUtility().getWordFormUtility()
 							.getNumber(part1), "p");
 
 					if (condition1 && condition2) {
 						String wrongWord = part1;
 						myLogger.trace("\tWrong: "+ wrongWord);
 //						if (StringUtility.isMatchedNullSafe(wrongWord, "\\w")) {
 						if (StringUtility.isMatchedNullSafe(wrongWord, "\\w")) {
 							this.noun2Modifier(dataholderHandler, wrongWord);
 							Set<String> words = dataholderHandler
 									.getWordsFromUnknownWord(null, false,
 											String.format("^%s$", wrongWord),
 											true);
 							for (String word : words) {
 								this.noun2Modifier(dataholderHandler, word);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Check if a word is (part of) the tag of any sentence
 	 * 
 	 * @param dataholderHandler
 	 *            DataHolder handler
 	 * @param raw
 	 *            word to check
 	 * @return true if it is, false otherwise
 	 */
 	public boolean isSentenceTag(DataHolder dataholderHandler, String raw) {
 		boolean result = false;
 		result = dataholderHandler.isExistSentence(false, String.format("^%s.*$", raw));
 		
 		return result;
 	}
 
 	/**
 	 * change the POS tag of a word from noun to modifier
 	 * 
 	 * @param dataholderHandler
 	 *            dataholder handler
 	 * 
 	 * @param word
 	 *            the word to change
 	 * @return true if any updates has been made, false otherwise
 	 */
 	public boolean noun2Modifier(DataHolder dataholderHandler, String word) {
 		boolean isUpdated = false;
 		
 		ArrayList<String> deletedPOSs = new ArrayList<String>();
 		deletedPOSs.add("s");
 		deletedPOSs.add("p");
 		deletedPOSs.add("n");
 		
 		for (String POS: deletedPOSs) {
 			dataholderHandler.deleteWordPOS(true, word, true, POS);
 		}
 		dataholderHandler.updateDataHolder(word, "m", "", "modifiers", 1);
 		
 		String oldPattern = String.format("(^%s$|^.* %s$)", word, word);
 		dataholderHandler.updateSentenceTag(oldPattern, null);
 		
 		return isUpdated;
 	}
 	
 	
 	public void separateModifierTag(DataHolder dataholderHandler) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.separateModifierTag");
 		
 		List<SentenceStructure> sentences = dataholderHandler.getSentencesByTagPattern("^.* .*$");
 		
 		for (SentenceStructure sentenceItem : sentences) {
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			String tag = sentenceItem.getTag();
 			myLogger.trace("ID: " + sentenceID);
 			myLogger.trace("Sentence: " + sentence);
 			myLogger.trace("Tag: " + tag);
 			
 			// case 1
 			String tagBackup = "" + tag;
 //			if (StringUtility.isMatchedNullSafe("\\w+", tagBackup)) {
 			if (StringUtility.isMatchedNullSafe(tagBackup, "\\w+")) {	
 				myLogger.trace("Case 1");
 				if (!StringUtility.isMatchedNullSafe(tagBackup,
 						String.format("\\b(%s)\\b", Constant.STOP))) {
 	
 					List<String> words = new LinkedList<String>();
 					words.addAll(Arrays.asList(tagBackup.split("\\s+")));
 					tag = words.get(words.size()-1);
 					
 					String modifier = "";
 					if (words.size() > 1) {
 						modifier = StringUtils.join(
 								StringUtility.stringArraySplice(words, 0,
 										words.size() - 1), " ");
 					}
 					
 					if (sentenceID == 22) {
 						System.out.println();
 					}
 					if (StringUtility.isMatchedNullSafe(tag, "\\w")) {
 						// case 1.1
 						myLogger.trace("Case 1.1");
 						dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "separatemodifiertag");
 					}
 					else {
 						// case 1.2
 						myLogger.trace("Case 1.2");
 						myLogger.trace(sentenceID);
 						dataholderHandler.tagSentenceWithMT(sentenceID, sentence, null, tag, "separatemodifiertag");
 					}
 				}
 				
 			}
 			// case 2
 			else {
 				// treat them case by case
 				// case 2: in some species, abaxially with =>NULL
 				myLogger.trace("Case 2");
 				if ((StringUtility.isMatchedNullSafe(tagBackup, "^in"))&&(StringUtility.isMatchedNullSafe(tagBackup, "\\b(with|without)\\b"))) {
 					myLogger.trace("Case 2.1");
 					dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "", null, "separtemodifiertag");
 				}
 				else {
 					myLogger.trace("Case 2.2");
 					String tagWithStopWordsReplaced = ""+tagBackup;
 					if (tagWithStopWordsReplaced != null) {
 						Pattern p = Pattern.compile("@ ([^@]+)$");
 						Matcher m = p.matcher(tagWithStopWordsReplaced);
 						if (m.find()) {
 							String tg = m.group(1);
 							ArrayList<String> tagWords = new ArrayList<String>();
 							tagWords.addAll(Arrays.asList(tg.split("\\s+")));
 							tag = tagWords.get(tagWords.size()-1);
 							String modifier = "";
 							if (tagWords.size()>1) {
 								modifier = StringUtils.join(StringUtility.stringArraySplice(tagWords, 0, tagWords.size()), " ");
 							}
 							
 							if (StringUtility.isMatchedNullSafe(tag, "\\w")) {
 								myLogger.trace("Case 2.2.1");
 								dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "separatemodifiertag");
 							}
 							else {
 								myLogger.trace("Case 2.2.2");
 								dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "", null, "separatemodifiertag");
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void resolveNMB(DataHolder dataholderHandler) {
 		Set<String> tags = dataholderHandler.getSentenceTags();
 		Iterator<Entry<WordPOSKey, WordPOSValue>> wordPOSIter = dataholderHandler.getWordPOSHolderIterator();
 		
 		// get words
 		Set<String> words = new HashSet<String>();
 		while (wordPOSIter.hasNext()) {
 			Entry<WordPOSKey, WordPOSValue> wordPOSEntry = wordPOSIter.next();
 			if (StringUtils.equals(wordPOSEntry.getKey().getPOS(), "b")) {
 				String word = wordPOSEntry.getKey().getWord();
 				boolean case1 = dataholderHandler.getWordPOSHolder().containsKey(new WordPOSKey(word, "s"));
 				boolean case2 = tags.contains(word);
 				if (case1 || case2) {
 					words.add(word);
 				}
 			}
 		}
 		
 		// update wordPOS holder and / or sentence holder
 		Iterator<String> wordIter = words.iterator();
 		while (wordIter.hasNext()) {
 			String word = wordIter.next();
 			
 			if (dataholderHandler.getModifierHolder().containsKey(word)) {
 				// remove N role
 				dataholderHandler.getWordPOSHolder().remove(new WordPOSKey(word, "s"));
 				
 				// reset sentence tags
 				Iterator<SentenceStructure> sentenceIter = dataholderHandler.getSentenceHolderIterator();
 				while (sentenceIter.hasNext()) {
 					SentenceStructure sentenceItem = sentenceIter.next();
 					String tag = sentenceItem.getSentence();
 					boolean case1 = StringUtils.equals(tag, word);
 					boolean case2 = StringUtility.isMatchedNullSafe(tag, " "+word);
 					if (case1 || case2) {
 						sentenceItem.setModifier("");
 						sentenceItem.setTag(null);
 					}
 				}
 				
 				dataholderHandler.getBMSWords().add(word);
 			}
 		}
 		
 		// retag clauses with <N><M><B> tags
 		Iterator<SentenceStructure> sentenceIter = dataholderHandler.getSentenceHolderIterator();
 		while (sentenceIter.hasNext()) {
 			SentenceStructure sentenceItem = sentenceIter.next();
 			String sentence = sentenceItem.getSentence();
 			sentence = sentence.replaceAll("<[ON]><M><B>", "<M><B>");
 			sentence = sentence.replaceAll("</B></M></[ON]>", "</B></M>");
 			sentenceItem.setSentence(sentence);
 		}
 		
 	}
 	
 	public void setAndOr(DataHolder dataholderHandler) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.separateModifierTag");		
 		myLogger.debug("Tag and/or sentences andor");
 		
 		String ptn1="^(?:[mbq,]{0,10}[onp]+(?:,|(?=&)))+&(?:[mbq,]{0,10}[onp]+)"; // n,n,n&n
 		String ptn2="^(?:[mbq,]{0,10}(?:,|(?=&)))+&(?:[mbq,]{0,10})[onp]+"; // m,m,&mn
 		
 		Iterator<SentenceStructure> sentenceIter = dataholderHandler.getSentenceHolderIterator();
 		while (sentenceIter.hasNext()) {
 			SentenceStructure sentenceItem = sentenceIter.next();
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			String lead = sentenceItem.getLead();
 			if (isIsAndOrSentence(sentenceID, sentence, lead, ptn1, ptn2)) {
 				sentenceItem.setTag("andor");
 			}
 		}
 	}
 
 	public boolean isIsAndOrSentence(int sentenceID, String sentence,
 			String lead, String ptn1, String ptn2) {
 		
 		Set<String> token = new HashSet<String>();
 		token.addAll(Arrays.asList("and or nor".split(" ")));
 		token.add("\\");
 		token.add("and / or");
 		
 		int limit = 80;
 		
 		List<String> words = new ArrayList<String>();
 		words.addAll(Arrays.asList(sentence.split(" ")));
 		
 		String sentencePtn = this.getLearnerUtility().getSentencePtn(
 				myDataHolder, token, limit, words);
 		
 		if (sentencePtn == null) {
 			return false;
 		}
 		
 		boolean result = isIsAndOrSentenceHelper(words, sentencePtn, ptn1, ptn2);
 		
 		return result;
 	}
 	
 	public boolean isIsAndOrSentenceHelper(List<String> words, String sentencePtn, String ptn1,
 			String ptn2) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.isIsAndOrSentence");		
 		
 		sentencePtn = sentencePtn.toLowerCase();
 		// ignore the distinction between type modifiers and modifiers
 		sentencePtn = sentencePtn.replaceAll("t", "m");
 		
 		Pattern p1 = Pattern.compile(ptn1);
 		Matcher m1 = p1.matcher(sentencePtn);
 		
 		Pattern p2 = Pattern.compile(ptn2);
 		Matcher m2 = p2.matcher(sentencePtn);
 		
 		int end = -1;
 		boolean case1 = false;
 		boolean case2 = false;
 		
 		if (m1.find()) {
 			end = m1.end();
 			case1 = true;
 		}
 		
 		if (m2.find()) {
 			end = m2.end();
 			case2 = true;
 		}
 		
 		if (case1 || case2) {
 			String matchedWords = StringUtils.join(words.subList(0, end), " ");
 			String regex = String.format("\\b(%s)\\b", Constant.PREPOSITION);
 			if (StringUtility.isMatchedNullSafe(matchedWords, regex)) {
 				myLogger.trace("Case 1");
 				return false;
 			}
 			myLogger.trace("Case 2");
 			return true;
 		}
 		myLogger.trace("Case 3");
 		return false;
 	}
 	
 	public void handleAndOr(DataHolder dataholderHandler) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.handleAndOr");
 		
 		myLogger.info("to match pattern $ANDORPTN");
 		
 		List<SentenceStructure> sentenceItems = dataholderHandler.getSentencesByTagPattern("^andor$");
 		
 		int sign = 0;
 		for (SentenceStructure sentenceItem : sentenceItems) {
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			myLogger.trace(Constant.SEGANDORPTN);
 			myLogger.trace(Constant.ANDORPTN);
 			int result = this.andOrTag(dataholderHandler, sentenceID, sentence, Constant.SEGANDORPTN, Constant.ANDORPTN);
 			sign = sign + result;
 		}
 		
 	}
 
 	public int andOrTag(DataHolder dataholderHandler, int sentenceID, String sentence, String sPattern,
 			String wPattern) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.andOrTag");
 		
 		Set<String> token = new HashSet<String>();
 		token.addAll(Arrays.asList("and or nor".split(" ")));
 		token.add("\\");
 		token.add("and / or");
 		
 		int limit = 80;
 		List<String> words = new ArrayList<String>();
 		words.addAll(Arrays.asList(sentence.split(" ")));
 		String ptn = this.getLearnerUtility().getSentencePtn(dataholderHandler, token, limit, words);
 		ptn = ptn.replaceAll("t", "m");
 		
 		myLogger.info(String.format("Andor pattern %s for %s", ptn, words.toString()));
 		
 		if (ptn == null) {
 			return -1;
 		}
 		
 		Matcher m1 = StringUtility.createMatcher(ptn, wPattern);
 		Matcher m2 = StringUtility.createMatcher(ptn, "^b+&b+[,:;.]");
 		
 		if (m1.find()) {
 			myLogger.trace("Case 1");
 		}
 		else if (m2.find()) {
 			myLogger.trace("Case 2");
 			dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "", "ditto", "andor");
 		}
 		else {
 			myLogger.trace("Case 3");
 			myLogger.trace("[andortag]Andor can not determine a tag or modifier for "+sentenceID+": " + sentence);
 		}
 		
 		
 		return 0;
 	}
 	
 	public void resetAndOrTags(DataHolder dataholderHandler) {
 		dataholderHandler.updateSentenceTag("^andor$", null);
 	}
 	
 	public void ditto (DataHolder dataholderHandler) {
 		String nPhrasePattern = "(?:<[A-Z]*[NO]+[A-Z]*>[^<]+?<\\/[A-Z]*[NO]+[A-Z]*>\\s*)+";
 		String mPhrasePattern = "(?:<[A-Z]*M[A-Z]*>[^<]+?<\\/[A-Z]*M[A-Z]*>\\s*)+";
 		
 		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
 			if (sentenceItem.getTag() == null) {
 				int sentenceID = sentenceItem.getID();
 				String sentence = sentenceItem.getSentence();
 				this.dittoHelper(dataholderHandler, sentenceID, sentence, nPhrasePattern, mPhrasePattern);
 			}
 		}
 	}
 	
 	public int dittoHelper(DataHolder dataholderHandler, int sentenceID,
 			String sentence, String nPhrasePattern, String mPhrasePattern) {
 		int res = 0;
 		String sentenceCopy = "" + sentence;
 		sentenceCopy = sentenceCopy.replaceAll("></?", "");
 		String modifier = "";
 		
 		Matcher m2 = StringUtility.createMatcher(sentenceCopy, "(.*?)"+nPhrasePattern);
 		
 		if (!StringUtility.isMatchedNullSafe(sentence, "<[NO]>")) {
 			String tag = "ditto";
 			dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "", tag, "ditto-no-N");
 			res = 1;
 		}
 		else if (m2.find()) {
 			String head = m2.group(1);
 			String pattern21 = String.format("\\b(%s)\\b", Constant.PREPOSITION);
 			if (StringUtility.isMatchedNullSafe(head, pattern21)) {
 				String tag = "ditto";
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "ditto-proposition");
 				res = 21;
 			}
 			else if (StringUtility.isMatchedNullSafe(head, ",<\\/B>\\s*$")) {
 				String tag = "ditto";
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "ditto-,-N");
 				res = 22;
 			}
 		}	
 		
 		return res;
 	}
 	
 	public void phraseClause(DataHolder dataholderHandler) {
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			if (sentenceItem.getTag() == null) {
 				int sentenceID = sentenceItem.getID();
 				String sentence = sentenceItem.getSentence();
 				List<String> res = this.phraseClauseHelper(sentence);
 				if (res != null && res.size() == 2) {
 					String modifier = res.get(0);
 					String tag = res.get(1);
 					dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 							modifier, tag, "phraseclause");
 				}
 			}
 		}
 	}
 
 	public List<String> phraseClauseHelper(String sentence) {
 		if (sentence == null) {
 			return null;
 		}
 
 		List<String> res = new ArrayList<String>(2);
 		String pattern = "^(.*?)((?:<[A-Z]*M[A-Z]*>[^<]*?<\\/[A-Z]*M[A-Z]*>\\s*)*)((?:<[A-Z]*[NO]+[A-Z]*>[^<]*?<\\/[A-Z]*[NO]+[A-Z]*>\\s*)+)<B>[,:\\.;]<\\/B>\\s*$";
 		String sentenceCopy = "" + sentence;
 		sentenceCopy = sentenceCopy.replaceAll("></?", "");
 		
 		Matcher m = StringUtility.createMatcher(sentenceCopy, pattern);
 		if (m.find()) {
 			String head = m.group(1);
 			String modifier = m.group(2);
 			String tag = m.group(3);
 
 			String prepositionPattern = String.format("\\b(%s)\\b",
 					Constant.PREPOSITION);
 			if (!StringUtility.isMatchedNullSafe(head, prepositionPattern)
 					&& !StringUtility.isMatchedNullSafe(head, "<\\/N>")
 					&& !StringUtility.isMatchedNullSafe(modifier,
 							prepositionPattern)) {
 				if (tag != null) {
 					Matcher m2 = StringUtility.createMatcher(tag,
 							"(.*?)<N>([^<]+)<\\/N>\\s*$");
 					if (m2.find()) {
 						modifier = modifier + m2.group(1);
 						tag = m2.group(2);
 					}
 					tag = tag.replaceAll("<\\S+?>", "");
 					modifier = modifier.replaceAll("<\\S+?>", "");
 					tag = tag.replaceAll("(^\\s*|\\s*$)", "");
 					modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
 					res.add(modifier);
 					res.add(tag);
 
 					return res;
 				}
 			}
 		}
 		return res;
 	}
 	
 	public void pronounCharacterSubject(DataHolder dataholderHandler) {
 		
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			
 			int sentenceID = sentenceItem.getID();
 			String lead = sentenceItem.getLead();
 			String sentence = sentenceItem.getSentence();
 			String modifier = sentenceItem.getModifier();
 			String tag = sentenceItem.getTag();
 			
 			List<String> mt = pronounCharacterSubjectHelper(lead, sentence, modifier, tag);
 			if (mt != null) {
 				dataholderHandler.tagSentenceWithMT(sentenceID,
 					sentence, modifier, tag,
 					"pronouncharactersubject[character subject]");
 			}
 		}
 		
 		// preposition cases
 		String prepositionPattern = String.format("^(%s)", Constant.PREPOSITION);
 		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
 			int sentenceID = sentenceItem.getID();
 			String lead = sentenceItem.getLead();
 			String modifier = sentenceItem.getModifier();
 			String tag = sentenceItem.getTag();
 			String sentence = sentenceItem.getSentence();
 			boolean case1 = (StringUtils.equals(tag, "ignore"));
 			boolean case2 = (tag == null);
 			boolean case3 = StringUtility.isMatchedNullSafe(tag, prepositionPattern + " ");
 			if ((case1 || case2) && case3) {
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "", "", "pronouncharactersubject[proposition subject]");
 			}
 		}
 		
 		// pronoun cases
 		String pronounPattern = String.format("(%s)", Constant.PRONOUN);
 		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
 			int sentenceID = sentenceItem.getID();
 			String lead = sentenceItem.getLead();
 			String modifier = sentenceItem.getModifier();
 			String tag = sentenceItem.getTag();
 			String sentence = sentenceItem.getSentence();
 			
 			boolean case1 = StringUtility.isMatchedNullSafe(tag, String.format("(^| )%s( |\\$)", pronounPattern));
 			boolean case2 = StringUtility.isMatchedNullSafe(modifier, String.format("(^| )%s( |\\$)", pronounPattern));			
 			if (case1 || case2) {				
 				modifier = modifier.replaceAll("\\b("+Constant.PRONOUN+")\\b", "");
 				tag = tag.replaceAll("\\b("+Constant.PRONOUN+")\\b", "");
 				modifier = modifier.replaceAll("\\s+", " ");
 				tag = tag.replaceAll("\\s+", " ");
 				
 				if (!StringUtility.isMatchedNullSafe(tag, "\\w") || StringUtility.isMatchedNullSafe(tag, "ditto")) {
 					tag = dataholderHandler.getParentSentenceTag(sentenceID);
 				}
 				
 				modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
 				tag = tag.replaceAll("(^\\s*|\\s*$)", "");
 				
 				List<String> mt = dataholderHandler.getMTFromParentTag(tag);
 				String m = mt.get(0);
 				tag = mt.get(1);
 				
 				if (StringUtility.isMatchedNullSafe(m, "\\w")) {
 					modifier = modifier + m;
 					dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "pronouncharactersubject[pronoun subject]");
 				}
 			}
 		}
 		
 		// correct to missed N
 		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
 			int sentenceID = sentenceItem.getID();
 			String lead = sentenceItem.getLead();
 			String modifier = sentenceItem.getModifier();
 			String tag = sentenceItem.getTag();
 			String sentence = sentenceItem.getSentence();
 			
 			boolean case1 = (StringUtils.equals(tag, "ignore"));
 			boolean case2 = (tag == null);
 			boolean case3 = !StringUtility.isMatchedNullSafe(tag, " (and|nor|or) ");
			boolean case4 = !StringUtility.isMatchedNullSafe(sentence, "[");
 			boolean case5 = false;
 			if (sentence != null) {
 				Pattern p = Pattern.compile("^[^N]*<N>" + tag);
 				Matcher m = p.matcher(sentence);
 				if (m.find()) {
 					case5 = true;
 				}
 			}
 			
 			if ((case1 || case2) && case3 && case4 && case5) {
 				if (sentence != null) {
 					String sentenceCopy = "" + sentence;
 					sentence = sentence.replaceAll("></?", "");
 					Pattern p = Pattern.compile("^(\\S*) ?<N>([^<]+)<\\/N> <[MB]+>(\\S+)<\\/[MB]+> \\S*\\b"+tag+"\\b\\S*");
 					Matcher m2 = p.matcher(sentence);
 					if (m2.find()) {
 						modifier = m2.group(1);
 						tag = m2.group(2);
 						String g3 = m2.group(3);
 						if (!StringUtility.isMatchedNullSafe(g3, "\\bof\\b")) {
 							modifier = modifier.replaceAll("<\\S+?>", "");
 							tag = tag.replaceAll("<\\S+?>", "");
 							modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
 							tag = tag.replaceAll("(^\\s*|\\s*$)", "");
 							dataholderHandler.tagSentenceWithMT(sentenceID, sentenceCopy, modifier, tag, "pronouncharactersubject[correct to missed N]");
 						}
 					}
 							
 				}
 			}
 		}
 	}
 
 	public List<String> pronounCharacterSubjectHelper(String lead,
 			String sentence, String modifier, String tag) {
 		String t = "(?:<\\/?[A-Z]+>)?";
 		
 		boolean b1 = !StringUtils.equals(tag, "ignore");
 		boolean b2 = (tag == null);
 		boolean b3 = StringUtility.isMatchedNullSafe(lead, "(^| )("+Constant.CHARACTER+")( |$)");
 		boolean b4 = StringUtility.isMatchedNullSafe(tag, "(^| )("+Constant.CHARACTER+")( |$)");
 		if (((b1 || b2) && b3) || b4) {
 			sentence = sentence.replaceAll("></?", "");
 			if (sentence != null) {
 				String pattern1 = String
 						.format("^.*?%s\\b(%s)\\b%s %s(?:of)%s (.*?)(<[NO]>([^<]*?)<\\/[NO]> ?)+ ",
 								t, Constant.CHARACTER, t, t, t);
 				Matcher m1 = StringUtility.createMatcher(sentence, pattern1);
 				
 				String pattern2 = String.format("^(.*?)((?:<\\/?[BM]+>\\w+?<\\/?[BM]+>\\s*)*)%s\\b(%s)\\b%s", t, Constant.CHARACTER, t);
 				Matcher m2 = StringUtility.createMatcher(sentence, pattern2);
 				
 				// case 1.1
 				if (m1.find()) {
 					tag = m1.group(4);
 					modifier = sentence.substring(m1.start(2), m1.start(4));
 					String s2 = m1.group(2);
 					String s3 = m1.group(3);
 					
 					if ((!StringUtility.isMatchedNullSafe(s2, String.format("\\b(%s)\\b", Constant.PREPOSITION)))
 							&& (!StringUtility.isMatchedNullSafe(s3, String.format("\\b(%s|\\d)\\b", Constant.STOP)))) {
 						modifier = modifier.replaceAll("<\\S+?>", "");
 						modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
 						tag = tag.replaceAll("<\\S+?>", "");
 						tag = tag.replaceAll("(^\\s*|\\s*$)", "");
 					}
 					else {
 						modifier = "";
 						tag = "ditto";							
 					}
 				}
 				
 				// case 1.2
 				else if (m2.find()) {
 					String text = m2.group(1);
 					
 					if ((!StringUtility.isMatchedNullSafe(text, "\\b("+Constant.STOP+"|\\d+)\\b")) 
 							&& (StringUtility.isMatchedNullSafe(text, "\\w")) 
 							&& (!StringUtility.isMatchedNullSafe(text, "[,:;.]"))) {
 						text = text.replaceAll("<\\S+?>", "");
 //						$text =~ s#(^\s*|\s*$)##g;
 //						$text =~ s#[[:punct:]]##g;
 						text = text.replaceAll("(^\\s*|\\s*$)", "");
 						text = text.replaceAll("\\p{Punct}", "");
 						
 						String[] textArray = text.split("\\s+");
 //						List<String> textList = new LinkedList<String>();
 //						textList.addAll(Arrays.asList(textArray));
 						if (textArray.length >= 1) {
 							tag = textArray[textArray.length-1];
 							String pattern = "<[NO]>"+tag+"</[NO]>";
 							if (StringUtility.isMatchedNullSafe(sentence, pattern)) {
 								// 1.2.1.1
 								text = text.replaceAll(tag, "");
 								modifier = text;
 							}
 							else {
 								// 1.2.1.2
 								modifier = "";
 								tag = "ditto";
 							}
 						}
 					}
 					else {
 						// 1.2.2
 						modifier = "";
 						tag = "ditto";
 					}
 				}
 				
 				// case 1.3
 				else if (StringUtility.isMatchedNullSafe(sentence, "\\b("+Constant.CHARACTER+")\\b")) {
 					modifier = "";
 					tag = "ditto";
 				}
 				
 				
 			}	
 			List<String> mt = new ArrayList<String>(2);
 			mt.add(modifier);
 			mt.add(tag);
 			return mt;
 		}	
 		else {
 			return null;
 		}
 		
 	}
 
 
 
 	// some unused variables in perl
 	// directory of /descriptions folder
 	private String desDir = "";
 	// directory of /characters folder
 	private String chrDir = "";
 	// prefix for all tables generated by this program
 	private String prefix = "";
 	// default general tag
 	private String defaultGeneralTag = "general";
 	// knowledge base
 	private String knlgBase = "phenoscape";
 
 	private int DECISIONID = 0;
 
 	private Map<String, String> numberRecords = new HashMap<String, String>(); // word->(p|s)
 	private Map<String, String> singularRecords = new HashMap<String, String>();// word->singular
 	private Map<String, String> POSRecords = new HashMap<String, String>(); // word->POSs
 	// private Map<String, String> POSRecordsRECORDS = new HashMap<String,
 	// String>();
 	private String NEWDESCRIPTION = ""; // record the index of sentences that
 										// ends a description
 
 	private Hashtable<String, String> PLURALS = new Hashtable<String, String>();
 
 	private String TAGS = "";
 
 	// grouped #may contain q but not the last m, unless it is followed by a p
 	private String mptn = "((?:[mbq][,&]*)*(?:m|b|q(?=[pon])))";
 	// grouped #must present, no q allowed
 	private String nptn = "((?:[nop][,&]*)*[nop])";
 	// grouped #when following a p, a b could be a q
 	private String bptn = "([,;:\\\\.]*\\$|,*[bm]|(?<=[pon]),*q)";
 	private String SEGANDORPTN = "(?:" + mptn + nptn + ")";
 	private String ANDORPTN = "^(?:" + SEGANDORPTN + "[,&]+)*" + SEGANDORPTN
 			+ bptn;
 	
 	// utility method
 	public LearnerUtility getLearnerUtility() {
 		return this.myLearnerUtility;
 	}
 	
 	public ITokenizer getTokenizer(){
 		return this.myTokenizer;
 	}
 	
 	public Configuration getConfiguration(){
 		return this.myConfiguration;
 	}
 	
 	public static void main(String[] args){
 		assertEquals("tagAllSentenceHelper", 1, 12);
 		
 		
 		
 		
 	}
 	
 }
