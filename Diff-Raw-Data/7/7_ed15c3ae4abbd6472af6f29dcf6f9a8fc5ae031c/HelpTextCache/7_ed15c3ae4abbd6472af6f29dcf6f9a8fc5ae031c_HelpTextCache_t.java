 package com.scholastic.sbam.server.fastSearch;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import com.scholastic.sbam.server.database.codegen.HelpText;
 import com.scholastic.sbam.server.database.objects.DbHelpText;
 import com.scholastic.sbam.server.database.util.HibernateUtil;
 import com.scholastic.sbam.shared.objects.CacheStatusInstance;
 import com.scholastic.sbam.shared.objects.HelpTextIndexInstance;
 
 /**
  * This class acts as a cache, to speed institution searches and sorting.
  * 
  * @author Bob Lacatena
  *
  */
 public class HelpTextCache extends AppCacheBase implements Runnable {
 	
 	public static final int MIN_WORD_LENGTH = 3;
 	public static final int MAX_WORD_LIST_LENGTH = 50;
 	// These are the standard MySQL stop words, which shouldn't be loaded into the word map
 	public static final HashSet<String> stopWordMap = new HashSet<String>();
 	
 	public static class HelpTextCacheNotReady extends Exception {
 		private static final long serialVersionUID = -8657762616708856381L;
 	}
 	
 	public static class HelpTextIndexFailure extends Exception {
 		private static final long serialVersionUID = -6804142059622204049L;
 		public HelpTextIndexFailure(String message) {
 			super(message);
 		}
 	}
 	
 	protected static HelpTextCache singleton;	//	 = new HelpTextCache();
 	
 	/**
 	 * Whether or not the init thread is running.
 	 */
 	protected boolean initRunning		= false;
 	/**
 	 * Whether or not a reload is in progress and data is ready
 	 */
 	protected boolean mapsReady			= false;
 	
 	protected HashMap<String, SortedSet<String>>	wordMap	  = new HashMap<String, SortedSet<String>>();
 	
 	protected HashSet<String> stopWords = new HashSet<String>();
 	
 	protected List<HelpTextIndexInstance> index = new ArrayList<HelpTextIndexInstance>();
 	
 	protected String indexError = null;
 	
 	protected int	helpPages;
 	
 	private HelpTextCache() {
 		init();
 	}
 	
 	/**
 	 * Initialize the map from the help text in the database.
 	 */
 	private synchronized boolean init() {
 		if (initRunning)
 			return false;
 
		helpPages = 0;
 		mapsReady = false;
 		initRunning = true;
 		System.out.println("HelpText cache thread starting...");
 		Thread initThread = new Thread(this);
 		initThread.setDaemon(true);
 		initThread.start();
 		System.out.println("HelpText cache thread running.");
 		return true;
 	}
 
 	
 	/**
 	 * Threaded code to initialize the map from the help text in the database.
 	 */
 	public synchronized void run() {
		helpPages = 0;
 		initRunning = true;
 		mapsReady = false;
 		
		index		= new ArrayList<HelpTextIndexInstance>();
		wordMap	  	= new HashMap<String, SortedSet<String>>();
 		
 		System.gc();
 		
 		HibernateUtil.openSession();
 		HibernateUtil.startTransaction();
 
 		try {
 
 			System.out.println(new Date());
 			
 			populateStopWords();
 
 			int count = 0;
 			System.out.println("Loading help text...");
 			List<HelpText> dbHelpTextList = DbHelpText.findAll();
 			String rootStart = null;
 			HashMap<String, HelpText> map = new HashMap<String, HelpText>();
 			System.out.println("Parsing help text...");
 			for (HelpText dbHelpText : dbHelpTextList) {
 				count++;
 				helpPages++;
 				parse(dbHelpText);
 				map.put(dbHelpText.getId(), dbHelpText);
 				if (dbHelpText.getParentId() == null || dbHelpText.getParentId().length() == 0)
 					if (dbHelpText.getPrevSiblingId() == null || dbHelpText.getPrevSiblingId().length() == 0)
 						rootStart = dbHelpText.getId();
 				
 				Thread.yield();
 			}
 
 			//	If no root, we have a problem
 			if (rootStart == null) {
 				setIndexError("Help text has no valid root element.");
 			} else {
 				try {
 					buildFromMap(rootStart, map, index);
 				} catch (HelpTextIndexFailure exc) {
 					setIndexError(exc.getMessage());
 				}
 			}
 			
 			//	Clean up the map words (remove anything that had too many entries)
 			int wordCount = 0;
 			HashSet<String> nullWords = new HashSet<String>();
 			for (String string : wordMap.keySet()) {
 				if (wordMap.get(string) == null) {
 					nullWords.add(string);
 				} else {
 					wordCount += wordMap.get(string).size();
 //					System.out.println(string + " : " + wordMap.get(string));
 				}
 			}
 			System.out.println("Null words count is " + nullWords.size());
 			for (String string : nullWords)
 				wordMap.remove(string);
 			
 			// We don't need the stop words map anymore
 			stopWords = null;
 
 			System.out.println("Loaded " + index.size() + " help pages.");
 			Runtime.getRuntime().gc();
 			System.out.println("Cleaned.");
 			
 			//	This method, using Hibernate, is going to fail because it wants to load all objects into memory
 
 			System.out.println(new Date());
 			System.out.println(wordMap.size() + " word prefixes with " + wordCount + " words stored.");
 		} catch (Exception exc) {
 			exc.printStackTrace();
 		}
 		
 		HibernateUtil.endTransaction();
 		HibernateUtil.closeSession();
 		
 		mapsReady = true;
 
 		Runtime.getRuntime().gc();
 		System.out.println("Total memory: " + Runtime.getRuntime().totalMemory());
 		System.out.println("Free  memory: " + Runtime.getRuntime().freeMemory());
 		System.out.println("Max   memory: " + Runtime.getRuntime().maxMemory());
 		
 		initRunning = false;
 		
 		dumpWordStats();
 	}
 	
 	public void populateStopWords() {
 		String [] stopWordList = {"as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"};
 		
 		stopWords = new HashSet<String>();
 		
 		for (String word : stopWordList)
 			stopWords.add(word);
 	}
 	
 	/**
 	 * Output the word map statistics for debugging or performance analysis
 	 */
 	public void dumpWordStats() {
 		
 		HashMap<Integer, Integer> wordCountMap = new HashMap<Integer, Integer>();
 		HashMap<Integer, Integer> prefixCountMap = new HashMap<Integer, Integer>();
 		for (String string : wordMap.keySet()) {
 			if (wordCountMap.containsKey(string.length())) {
 				wordCountMap.put(string.length(), wordCountMap.get(string.length()) + wordMap.get(string).size());
 				prefixCountMap.put(string.length(), prefixCountMap.get(string.length()) + 1);
 			} else {
 				wordCountMap.put(string.length(), wordMap.get(string).size());
 				prefixCountMap.put(string.length(), 1);
 			}
 		}
 		for (Integer length : wordCountMap.keySet()) {
 			System.out.println("length " + length + "  : " + prefixCountMap.get(length) + " prefixes, " + wordCountMap.get(length) + " words");
 		}
 		
 	}
 	
 	/**
 	 * Parse one institution and add it to the supplied, temporary (list based) map of strings.
 	 * 
 	 * @param institution
 	 * @param tempMap
 	 */
 	private void parse(HelpText dbHelpText) {
 		//  Parse all components of the help text
 		parseAdd(dbHelpText.getTitle());
 		parseAdd(dbHelpText.getText());
 	}
 	
 	/**
 	 * Parse a string into word strings, broken (and ignoring) by any non-alphanumeric characters into a hash set of such strings.
 	 * 
 	 * Using a hash set guarantees that any string will exist at most one time in the complete list, even if duplicated in the address.
 	 * @param string
 	 * @param strings
 	 */
 	private void parseAdd(String string) {
 		boolean skipping = false;
 		StringBuffer word = new StringBuffer();
 		for (int i = 0; i < string.length(); i++) {
 			//	Skip HTML tags
 			if (string.charAt(i) == '<')
 				skipping = true;
 			else if (string.charAt(i) == '>')
 				skipping = false;
 			else if (!skipping) {
 				if ( (string.charAt(i) >= 'a' && string.charAt(i) <= 'z')
 				|| 	 (string.charAt(i) >= 'A' && string.charAt(i) <= 'Z')
 				||	 (string.charAt(i) >= '0' && string.charAt(i) <= '9')) {
 					word.append(string.charAt(i));
 				} else if (word.length() > 0) {
 					mapWord(word);
 					word = new StringBuffer();
 				}
 			}
 		}
 		if (word.length() > 0)
 			mapWord(word);
 	}
 	
 	/**
 	 * Add a word to the word map
 	 * @param word
 	 */
 	private void mapWord(StringBuffer word) {
 		if (word == null || word.length() == 0)
 			return;
 		if (word.length() < MIN_WORD_LENGTH)
 			return;
 		if (stopWords.contains(word))
 			return;
 		String mapWord;
 		if (word.length() == 1)
 			mapWord = word.toString().toUpperCase();
 		else 
 			mapWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
 		for (int i = 1; i <= word.length(); i++) {
 			String string = word.substring(0, i).toUpperCase();
 			if (wordMap.containsKey(string)) {
 				SortedSet<String> words = wordMap.get(string);
 				if (words != null) {
 					if (words.size() < MAX_WORD_LIST_LENGTH)
 						words.add(mapWord);
 					else	// We have too many now, to remember this by having a null entry in the map
 						wordMap.put(string, null);
 				}
 			} else {
 				SortedSet<String> words = new TreeSet<String>();
 				words.add(mapWord);
 				wordMap.put(string, words);
 			}
 		}
 	}
 	
 	/**
 	 * Build help text index instances, adding siblings to the root list
 	 * @param id
 	 * @param map
 	 * @param list
 	 * @throws IllegalArgumentException
 	 */
 	public void buildFromMap(String id, HashMap<String, HelpText> map, List<HelpTextIndexInstance> list) throws HelpTextIndexFailure {
 		
 		//	Process all siblings, and add them in order to the list
 		while (id != null && id.length() > 0) {
 			if (!map.containsKey(id))
 				throw new HelpTextIndexFailure("Referenced Help Text ID '" + id + "' not found.");
 			
 			HelpText dbInstance = map.get(id);
 			HelpTextIndexInstance instance = getIndexInstance(dbInstance);
 			buildFromMap(dbInstance.getFirstChildId(), map, instance);
 			
 			list.add(instance);
 			id = dbInstance.getNextSiblingId();
 		}
 		
 	}
 	
 	/**
 	 * Build help text index instances, adding siblings to the declared parent
 	 * @param id
 	 * @param map
 	 * @param parent
 	 * @throws IllegalArgumentException
 	 */
 	public void buildFromMap(String id, HashMap<String, HelpText> map, HelpTextIndexInstance parent) throws HelpTextIndexFailure {
 		
 		//	Process all siblings, and add them in order to the list
 		while (id != null && id.length() > 0) {
 			if (!map.containsKey(id))
 				throw new HelpTextIndexFailure("Referenced Help Text ID '" + id + "' not found.");
 			
 			HelpText dbInstance = map.get(id);
 			HelpTextIndexInstance instance = getIndexInstance(dbInstance);
 			buildFromMap(dbInstance.getFirstChildId(), map, instance);
 			
 			parent.add(instance);
 			id = dbInstance.getNextSiblingId();
 		}
 		
 	}
 	
 	public HelpTextIndexInstance getIndexInstance(HelpText dbInstance) {
 		HelpTextIndexInstance instance = new HelpTextIndexInstance();
 		instance.setId(dbInstance.getId());
 		instance.setTitle(dbInstance.getTitle());
 		instance.setIconName(dbInstance.getIconName());
 		
 		return instance;
 	}
 	
 	public String [] parseFilter(String filter) {
 		HashSet<String> terms = new HashSet<String>();
 		if (filter != null) {
 			StringBuffer word = new StringBuffer();
 			for (int i = 0; i < filter.length(); i++) {
 				if ( (filter.charAt(i) >= 'a' && filter.charAt(i) <= 'z')
 				|| 	 (filter.charAt(i) >= 'A' && filter.charAt(i) <= 'Z')
 				||	 (filter.charAt(i) >= '0' && filter.charAt(i) <= '9')) {
 					word.append(filter.charAt(i));
 				} else if (word.length() > 0) {
 					if (!terms.contains(word)) {
 						terms.add(word.toString());
 					}
 					word = new StringBuffer();
 				}
 			}
 			if (word.length() > 0)
 				if (!terms.contains(word.toString())) {
 					terms.add(word.toString());
 				}
 		}
 		return terms.toArray(new String [] {});
 	}
 	
 	public void refresh() {
 		init();
 	}
 
 	public boolean isMapsReady() {
 		return mapsReady;
 	}
 
 	public HashMap<String, SortedSet<String>> getWordMap() throws HelpTextCacheNotReady {
 		if (!mapsReady)
 			throw new HelpTextCacheNotReady();
 		return wordMap;
 	}
 
 	public static HelpTextCache getSingleton() {
 		if (singleton == null)
 			singleton = new HelpTextCache();
 		return singleton;
 	}
 	
 	public String [] getWords(String prefix) throws HelpTextCacheNotReady {
 		if (!mapsReady)
 			throw new HelpTextCacheNotReady();
 		if (prefix == null)
 			return new String [] {};
 		prefix = prefix.toUpperCase();
 		if (wordMap.containsKey(prefix))
 			return wordMap.get(prefix).toArray(new String [] {});
 		else
 			return new String [] {};
 	}
 
 	public List<HelpTextIndexInstance> getIndex() {
 		return index;
 	}
 
 	public String getIndexError() {
 		return indexError;
 	}
 
 	protected void setIndexError(String indexError) {
 		this.indexError = indexError;
 		this.index = null;
 		System.out.println("Error generating help index: " + indexError);
 	}
 
 	
 	@Override
 	public String getTableName() {
 		return "help_text";
 	}
 	
 	@Override
 	public String [] getCountHeadings() {
 		return new String [] {"Pages", "Words"};
 	}
 
 	@Override
 	public int [] getProcessed() {
 		return new int [] {helpPages, wordMap.size()};
 	}
 	
 	@Override
 	public int [] getExpectedCounts() throws Exception {
 		return new int [] {getExpectedCount()};
 	}
 
 	@Override
 	public boolean getReady() {
 		return mapsReady;
 	}
 
 	@Override
 	public boolean getLoading() {
 		return initRunning;
 	}
 
 	@Override
 	public String getName() {
 		return "Help Text Cache";
 	}
 
 	@Override
 	public int getSeq() {
 		return 3;
 	}
 
 	@Override
 	public String getKey() {
 		return CacheStatusInstance.HELP_TEXT_CACHE_KEY;
 	}
 }
