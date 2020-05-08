 package com.scholastic.sbam.server.fastSearch;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import com.scholastic.sbam.server.database.util.HibernateUtil;
 import com.scholastic.sbam.server.util.AppServerConstants;
 import com.scholastic.sbam.shared.objects.InstitutionInstance;
 
 /**
  * This class acts as a cache, to speed institution searches and sorting.
  * 
  * @author Bob Lacatena
  *
  */
 public class InstitutionCache implements Runnable {
 	protected static final String INSTITUTION_SQL = "SELECT ucn, institution_name, address1, address2, address3, city, state, zip, country, phone, fax FROM institution WHERE status = 'A'";
 	protected static final int	LOAD_WATCH	= 5000;
 	protected static final int	LOAD_GC 	= 100000;
 	
 	public static class InstitutionCacheNotReady extends Exception {
 		private static final long serialVersionUID = -8657762616708856381L;
 	}
 	
 	public static class InstitutionCacheConflict extends Exception {
 		private static final long serialVersionUID = -5649410161902019748L;
 	}
 	
 	public static class InstitutionCacheConfig {
 		/**
 		 * Whether or not inner strings should be used, or only the strings that begin words.
 		 */
 		protected boolean	useInnerStrings		= false;
 		/**
 		 * Whether or not pairs of strings should be used, or only the strings alone.
 		 */
 		protected boolean	useStringPairs		= true;
 		/**
 		 * The minimum length to bother even considering for a search string.
 		 */
 		protected int	 	minStringLength	= 2;
 		/**
 		 * The minimum length to bother with in creating inner strings.
 		 */
 		protected int	 	minInnerStringLength	= 4;
 		/**
 		 * The maximum length to bother with in creating string pairs.
 		 */
 		protected int	 	maxStringPairLength	= 5;
 		/**
 		 * The maximum useful length of a list.  Anything over this is stored only as a count in the countMap.
 		 */
 		protected int		maxListLength = 500;
 		/**
 		 * The maximum number of words to list for a prefix.  Anything over this is removed from the list.
 		 */
 		protected int		maxWordListLength	= 500;
 		
 		public String toString() {
 			return "Institution Cache Config : [Inner = " + useInnerStrings + ", Pairs = " + useStringPairs + ", min len = " + minStringLength + ", min inner = " + minInnerStringLength + 
 					", max pair = " + maxStringPairLength + ", max ucn list = " + maxListLength + ", max word list = " + maxWordListLength + "]";
 		}
 		
 		public boolean isUseInnerStrings() {
 			return useInnerStrings;
 		}
 		public void setUseInnerStrings(boolean useInnerStrings) {
 			this.useInnerStrings = useInnerStrings;
 		}
 		public boolean isUseStringPairs() {
 			return useStringPairs;
 		}
 		public void setUseStringPairs(boolean useStringPairs) {
 			this.useStringPairs = useStringPairs;
 		}
 		public int getMinStringLength() {
 			return minStringLength;
 		}
 		public void setMinStringLength(int minStringLength) {
 			this.minStringLength = minStringLength;
 		}
 		public int getMinInnerStringLength() {
 			return minInnerStringLength;
 		}
 		public void setMinInnerStringLength(int minInnerStringLength) {
 			this.minInnerStringLength = minInnerStringLength;
 		}
 		public int getMaxStringPairLength() {
 			return maxStringPairLength;
 		}
 		public void setMaxStringPairLength(int maxStringPairLength) {
 			this.maxStringPairLength = maxStringPairLength;
 		}
 		public int getMaxListLength() {
 			return maxListLength;
 		}
 		public void setMaxListLength(int maxListLength) {
 			this.maxListLength = maxListLength;
 		}
 		public int getMaxWordListLength() {
 			return maxWordListLength;
 		}
 		public void setMaxWordListLength(int maxWordListLength) {
 			this.maxWordListLength = maxWordListLength;
 		}
 		
 	}
 	
 	protected static InstitutionCache singleton;	//	 = new InstitutionCache();
 	
 	/**
 	 * The cache configuration
 	 */
 	protected InstitutionCacheConfig config;
 	
 	/**
 	 * Whether or not the init thread is running.
 	 */
 	protected boolean initRunning		= false;
 	/**
 	 * Whether or not a reload is in progress and data is ready
 	 */
 	protected boolean mapsReady			= false;
 	
 	protected HashMap<String, SortedSet<String>>	wordMap	  = new HashMap<String, SortedSet<String>>();
 	protected HashMap<String, List<Integer>>		searchMap = new HashMap<String, List<Integer>>();
 	protected HashMap<String, Integer>				countMap  = new HashMap<String, Integer>();
 	
 	private InstitutionCache() {
 		config = new InstitutionCacheConfig();
 		init();
 	}
 	
 	private InstitutionCache(InstitutionCacheConfig config) {
 		if (config != null)
 			this.config = config;
 		init(this.config);
 	}
 	
 	/**
 	 * This class returns the singleton with new, specific run parameters.
 	 * 
 	 * This method can fail!  If a previous call to initialize the singleton is running, then a new one cannot be created (unless there were unlimited memory,
 	 * or we took the trouble to kill the old thread and clean it up first).  Instead, this method returns null, signaling the calling process that it
 	 * needs to worry about canceling its own request, or waiting and requesting the singleton again.
 	 * 
 	 * This is no different from the fact that any request to use the singleton must also check that it is ready.
 	 * 
 	 * @param useInnerStrings
 	 * @param minStringLength
 	 * @param maxListLength
 	 * @param maxWordListLength
 	 * @return
 	 */
 	public static synchronized InstitutionCache getSingleton(InstitutionCacheConfig config) throws InstitutionCacheConflict {
 		if (singleton == null) {
 			singleton = new InstitutionCache(config);
 		} else {
 			if (config != null && singleton.config != config && !configsAreEqual(singleton.config, config)) {
 			//	Config has changed, so we need to re-initialize the singleton	
 				if (!singleton.init(config)) {
 					//	If it wouldn't reinitialize because another init was running, then this particular request failed
 					System.out.println("Running   config -- " + singleton.config);
 					System.out.println("Requested config -- " + config);
 					throw new InstitutionCacheConflict();
 				}
 			}
 		}
 		//	In any event, return the singleton we have here
 		return singleton;
 	}
 	
 	private boolean init() {
 		return init(config);
 	}
 	
 	private static boolean configsAreEqual(InstitutionCacheConfig config1, InstitutionCacheConfig config2) {
 		if (config1.useInnerStrings != config2.useInnerStrings)
 			return false;
 		if (config1.useStringPairs != config2.useStringPairs)
 			return false;
 		if (config1.minStringLength != config2.minStringLength)
 			return false;
 		if (config1.minInnerStringLength != config2.minInnerStringLength)
 			return false;
 		if (config1.maxStringPairLength != config2.maxStringPairLength)
 			return false;
 		if (config1.maxListLength != config2.maxListLength)
 			return false;
 		if (config1.maxWordListLength != config2.maxWordListLength)
 			return false;
 		return true;
 	}
 	
 	/**
 	 * Initialize the map from the institutions in the database.
 	 */
 	private synchronized boolean init(InstitutionCacheConfig config) {
 		if (initRunning)
 			return false;
 		
 		initRunning = true;
 		
 		this.config = config;
 		
 		System.out.println(this.config);
 		System.out.println("Institution cache thread starting...");
		Thread initThread = new Thread(this);
		initThread.setDaemon(true);
		initThread.start();
 		System.out.println("Institution cache thread running.");
 		return true;
 	}
 
 	
 	/**
 	 * Threaded code to initialize the map from the institutions in the database.
 	 */
 	public synchronized void run() {
 		initRunning = true;
 		mapsReady = false;
 		
 		searchMap = new HashMap<String, List<Integer>>();
 		countMap  = new HashMap<String, Integer>();
 		wordMap	  = new HashMap<String, SortedSet<String>>();
 		
 		System.gc();
 		
 		HibernateUtil.openSession();
 		HibernateUtil.startTransaction();
 
 		try {
 			int loadLimit = AppServerConstants.getCustomerLoadLimit();
 
 			System.out.println(new Date());
 			System.out.println("Loading institutions...");
 			Statement sqlStmt = HibernateUtil.getConnection().createStatement();
 			ResultSet results = sqlStmt.executeQuery(INSTITUTION_SQL);
 			
 			int count = 0;
 			System.out.println(new Date());
 			System.out.println("Parsing institutions...");
 			while (results.next()) {
 				
 				if (loadLimit > -1 && count >= loadLimit) {
 					break;
 				}
 
 				count++;
 				parse(getInstance(results));
 				
 				if (LOAD_WATCH > 0 && count % LOAD_WATCH == 0) {
 					System.out.print(count + " | ");
 					System.out.println(new Date() + "   |   " + searchMap.size() + "   |   "  + countMap.size() + "   |   "+ (Runtime.getRuntime().freeMemory() / 1000000000d) + "Gb  |  " + (Runtime.getRuntime().totalMemory() / 1000000000d) + "Gb");
 				}
 				if (LOAD_GC > 0 && count % LOAD_GC == 0)
 					Runtime.getRuntime().gc();
 				
 				Thread.yield();
 			}
 			System.out.println();
 			
 			results.close();
 			sqlStmt.close();
 			
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
 
 			System.out.println("Loaded.");
 			Runtime.getRuntime().gc();
 			System.out.println("Cleaned.");
 			
 			//	This method, using Hibernate, is going to fail because it wants to load all objects into memory
 //			int count = 0;
 //			System.out.println("Loading institutions...");
 //			List<Institution> institutions = DbInstitution.findAllActive();
 //			System.out.println("Parsing institutions...");
 //			for (Institution institution : institutions) {
 //				parse(DbInstitution.getInstance(institution), tempMap);
 //				
 //				if(count++ > 10)
 //					break;
 //			}
 
 			System.out.println(new Date());
 			System.out.println(count + " institutions parsed and loaded into a map of size " + searchMap.size() + ".");
 			System.out.println(wordMap.size() + " word prefixes with " + wordCount + " words stored.");
 			
 //			for (String string : searchMap.keySet()) {
 //				System.out.println(string + " : " + searchMap.get(string).length);
 //			}
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
 	
 	private InstitutionInstance getInstance(ResultSet results) throws SQLException {
 		InstitutionInstance instance = new InstitutionInstance();
 		
 		instance.setUcn(results.getInt("ucn"));
 		instance.setInstitutionName(results.getString("institution_name"));
 		instance.setAddress1(results.getString("address1"));
 		instance.setAddress2(results.getString("address2"));
 		instance.setAddress3(results.getString("address3"));
 		instance.setCity(results.getString("city"));
 		instance.setState(results.getString("state"));
 		instance.setZip(results.getString("zip"));
 		instance.setCountry(results.getString("country"));
 		instance.setPhone(results.getString("phone"));
 		instance.setFax(results.getString("fax"));
 		
 		return instance;
 	}
 	
 	/**
 	 * Parse one institution and add it to the supplied, temporary (list based) map of strings.
 	 * 
 	 * @param institution
 	 * @param tempMap
 	 */
 	private void parse(InstitutionInstance institution) {
 		HashSet<String> strings = new HashSet<String>();
 		
 		//  Parse all components of the institution address
		parseAdd(institution.getUcn() + "", strings);
 		parseAdd(institution.getInstitutionName(), strings);
 		parseAdd(institution.getAddress1(), strings);
 		parseAdd(institution.getAddress2(), strings);
 		parseAdd(institution.getAddress3(), strings);
 		parseAdd(institution.getCity(), strings);
 		parseAdd(institution.getState(), strings);
 		parseAdd(institution.getZip(), strings);
 		parseAdd(institution.getCountry(), strings);
 		parseAddContinuous(institution.getPhone(), strings);
 		parseAddContinuous(institution.getFax(), strings);
 		
 		//	Add more substrings for inner strings, if the option is activated
 		if (config.useInnerStrings) {
 			HashSet<String> newStrings = new HashSet<String>();
 			for (String string : strings) {
 				newStrings.add(string);
 				for (int i = 1; i < string.length(); i++) {
 					for (int j = i + config.minInnerStringLength; j <= string.length(); j++) {
 						if (!newStrings.contains(string.substring(i, j)))
 							newStrings.add(string.substring(i,j));
 					}
 				}
 			}
 			strings = newStrings;
 		}
 		
 		//	Add pairs of substrings, if the option is activated
 		if (config.useStringPairs) {
 			HashSet<String> stringPairs = new HashSet<String>();
 			for (String first : strings) {
 				for (String second : strings) {
 					if (!first.equals(second))
 						stringPairs.add(getFilterPair(first, second));
 				}
 			}
 		//	strings.addAll(stringPairs);
 		}
 		
 		//  Add them all to the search maps
 		Integer key = new Integer(institution.getUcn());
 		for (String string : strings) {
 			if (string.length() >= config.minStringLength) {
 				if (countMap.containsKey(string)) {
 					countMap.put(string, countMap.get(string) + 1);
 				} else if (searchMap.containsKey(string)) {
 					if (searchMap.get(string).size() + 1 > config.maxListLength) {
 						countMap.put(string, searchMap.get(string).size() + 1);
 						searchMap.remove(string);
 					//	Runtime.getRuntime().gc();
 					} else {
 						searchMap.get(string).add(key);
 					}
 				} else {
 					List<Integer> list = new ArrayList<Integer>();
 					list.add(key);
 					searchMap.put(string, list);
 				}
 			} else {
 				if (countMap.containsKey(string)) {
 					countMap.put(string, countMap.get(string) + 1);
 				} else {
 					countMap.put(string, 1);
 				}
 			}
 		}
 		
 //		System.out.println(institution.getInstitutionName());
 //		System.out.println(institution.getAddress1());
 //		System.out.println(institution.getAddress2());
 //		System.out.println(institution.getAddress3());
 //		System.out.println(institution.getCity());
 //		System.out.println(institution.getState());
 //		System.out.println(institution.getZip());
 //		System.out.println(institution.getCountry());
 //		System.out.println(institution.getPhone());
 //		System.out.println(institution.getFax());
 //		System.out.println();
 //		System.out.println(strings);
 //		System.out.println("---------------------------------------------------------------------------------------------------");
 	}
 	
 	/**
 	 * Parse a string into word strings, broken (and ignoring) by any non-alphanumeric characters into a hash set of such strings.
 	 * 
 	 * Using a hash set guarantees that any string will exist at most one time in the complete list, even if duplicated in the address.
 	 * @param string
 	 * @param strings
 	 */
 	private void parseAdd(String string, HashSet<String> strings) {
 		StringBuffer word = new StringBuffer();
 		for (int i = 0; i < string.length(); i++) {
 			if ( (string.charAt(i) >= 'a' && string.charAt(i) <= 'z')
 			|| 	 (string.charAt(i) >= 'A' && string.charAt(i) <= 'Z')
 			||	 (string.charAt(i) >= '0' && string.charAt(i) <= '9')) {
 				word.append(string.charAt(i));
 				if (!strings.contains(word.toString())) {
 					strings.add(word.toString());
 				}
 			} else if (word.length() > 0) {
 				mapWord(word);
 				word = new StringBuffer();
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
 					if (words.size() < config.maxWordListLength)
 						words.add(mapWord);
 					else
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
 	 * Parse a string into character sequences without word breaks into a hashset of such strings.
 	 * 
 	 * Using a hash set guarantees that any string will exist at most one time in the complete list, even if duplicated in the address.
 	 * @param string
 	 * @param strings
 	 */
 	private void parseAddContinuous(String string, HashSet<String> strings) {
 		string = string.toUpperCase();
 		StringBuffer word = new StringBuffer();
 		for (int i = 0; i < string.length(); i++) {
 			if ( (string.charAt(i) >= 'a' && string.charAt(i) <= 'z')
 			|| 	 (string.charAt(i) >= 'A' && string.charAt(i) <= 'Z')
 			||	 (string.charAt(i) >= '0' && string.charAt(i) <= '9')) {
 				word.append(string.charAt(i));
 				if (!strings.contains(word.toString()))
 					strings.add(word.toString());
 			}
 		}
 		
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
 	
 	public String getFilterPair(String first, String second) {
 		if (first.length() > config.maxStringPairLength)
 			first = first.substring(0, config.maxStringPairLength);
 		if (second.length() > config.maxStringPairLength)
 			second = second.substring(0, config.maxStringPairLength);
 		
 		if (first.compareToIgnoreCase(second) <= 0)
 			return (first + " " + second);
 		else
 			return (second + " " + first);
 	}
 	
 	public List<Integer> getFilteredUcns(String filter) throws InstitutionCacheNotReady {
 		String filters [] = parseFilter(filter);
 		return getFilteredUcns(filters);
 	}
 	
 	public List<Integer> getFilteredUcns(String [] filters) throws InstitutionCacheNotReady {
 		
 		List<Integer> ucns = null;
 
 		if (config.useStringPairs && filters.length > 1) {
 			for (int i = 0; i < filters.length - 1; i++)
 				for (int j = i + 1; j < filters.length; j++)
 					if (!filters.equals(filters[j])) {
 						String filterPair = getFilterPair(filters [i], filters [j]);
 						if (filterPair != null) {
 							List<Integer> unionUcns = getInstitutionUcns(filterPair);
 							if (unionUcns != null) {
 								if (ucns == null) {
 									ucns = new ArrayList<Integer>();
 									ucns.addAll(unionUcns);
 								} else {
 									ucns.retainAll(unionUcns);
 								}
 							}
 						}
 					}
 		}
 		
 		for (int i = 0; i < filters.length; i++) {
 			List<Integer> unionUcns = getInstitutionUcns(filters [i]);
 			if (unionUcns != null) {
 				if (ucns == null) {
 					ucns = new ArrayList<Integer>();
 					ucns.addAll(unionUcns);
 				} else {
 					ucns.retainAll(unionUcns);
 				}
 			}
 		}
 			
 		return ucns;
 	}
 	
 	public int getFilteredUcnCount(String filter) throws InstitutionCacheNotReady {
 		String filters [] = parseFilter(filter);
 		return getFilteredUcnCount(filters);
 	}
 
 	
 	public int getFilteredUcnCount(String [] filters) throws InstitutionCacheNotReady {
 		int bestCount = -1;
 		
 		if (config.useStringPairs && filters.length > 1) {
 			for (int i = 0; i < filters.length - 1; i++) {
 				for (int j = i; j < filters.length; j++) {
 					String filterPair = getFilterPair(filters [i], filters [j]);
 					if (filterPair != null) {
 						int thisCount = getInstitutionCount(filterPair);
 						if(thisCount > 0 && (bestCount < 0 || thisCount < bestCount))
 							bestCount = thisCount;
 					}
 				}
 			}
 		}
 		
 		for (int i = 0; i < filters.length; i++) {
 			int thisCount = getInstitutionCount(filters [i]);
 			if(thisCount > 0 && (bestCount < 0 || thisCount < bestCount))
 				bestCount = thisCount;
 		}
 		
 		if (bestCount < 0)
 			bestCount = 0;
 		
 		return bestCount;
 	}
 	
 	public void refresh() {
 		init();
 	}
 
 	public boolean isMapsReady() {
 		return mapsReady;
 	}
 
 	public HashMap<String, SortedSet<String>> getWordMap() throws InstitutionCacheNotReady {
 		if (!mapsReady)
 			throw new InstitutionCacheNotReady();
 		return wordMap;
 	}
 
 	public InstitutionCacheConfig getConfig() {
 		return config;
 	}
 
 	public static InstitutionCache getSingleton() throws InstitutionCacheConflict {
 		return getSingleton(null);
 	}
 
 	public HashMap<String, List<Integer>> getSearchMap() throws InstitutionCacheNotReady {
 		if (!mapsReady)
 			throw new InstitutionCacheNotReady();
 		return searchMap;
 	}
 
 	public HashMap<String, Integer> getCountMap() throws InstitutionCacheNotReady {
 		if (!mapsReady)
 			throw new InstitutionCacheNotReady();
 		return countMap;
 	}
 	
 	public List<Integer> getInstitutionUcns(String string) throws InstitutionCacheNotReady {
 		if (!mapsReady)
 			throw new InstitutionCacheNotReady();
 		return searchMap.get(string);
 	}
 	
 	public int getInstitutionCount(String string) throws InstitutionCacheNotReady {
 		if (!mapsReady)
 			throw new InstitutionCacheNotReady();
 		if (countMap.containsKey(string))
 			return countMap.get(string);
 		else
 			return -1;
 	}
 	
 	public String [] getWords(String prefix) throws InstitutionCacheNotReady {
 		if (!mapsReady)
 			throw new InstitutionCacheNotReady();
 		if (prefix == null)
 			return new String [] {};
 		prefix = prefix.toUpperCase();
 		if (wordMap.containsKey(prefix))
 			return wordMap.get(prefix).toArray(new String [] {});
 		else
 			return new String [] {};
 	}
 	
 }
