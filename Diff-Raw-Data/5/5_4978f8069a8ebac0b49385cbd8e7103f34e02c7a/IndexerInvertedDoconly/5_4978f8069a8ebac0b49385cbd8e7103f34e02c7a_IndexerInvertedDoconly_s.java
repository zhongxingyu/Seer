 package edu.nyu.cs.cs2580;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.comparator.SizeFileComparator;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.en.EnglishAnalyzer;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.util.Version;
 
 import edu.nyu.cs.cs2580.SearchEngine.Options;
 
 /**
  * @CS2580: Implement this class for HW2.
  */
 public class IndexerInvertedDoconly extends Indexer {
 	private Map<String, HashMap<String, WordAttribute>> mapOfMaps;
 	private Map<Integer, DocumentIndexed> docMap = new HashMap<Integer, DocumentIndexed>();
 	private Map<String, WordAttribute> wordMap = new HashMap<String, WordAttribute>();
 	private int totalNoOfFiles = 0;
 
 	public IndexerInvertedDoconly(Options options) {
 		super(options);
 		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
 	}
 
 	private List<String> tokenize(TokenStream stream) throws IOException {
 		List<String> words = new ArrayList<String>();
 
 		CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);
 		while (stream.incrementToken()) {
 			words.add(attr.toString());
 		}
 
 		stream.end();
 		stream.close();
 		return words;
 	}
 
 	@Override
 	public void constructIndex() throws IOException {
 		File corpusDir = new File(_options._corpusPrefix);
 		File[] listOfFiles = corpusDir.listFiles();
 		totalNoOfFiles = listOfFiles.length;
 
 		int i = 0;
 		int index = 1;
 		initializeMap();
 		for (File eachFile : listOfFiles) {
 			if (i >= totalNoOfFiles / 5) {
 				serialize();
 				mapOfMaps = null;
 				i = 0;
 				initializeMap();
 			}
 			analyse(eachFile, index);
 			index++;
 			i++;
 		}
 
 		// if (noOfFiles <= 5 && i <= noOfFiles) {
 		serialize();
 		mapOfMaps = null;
 		// }
 
 		try {
 			merge();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void merge() throws FileNotFoundException, IOException, ClassNotFoundException {
 		File indexDir = new File(_options._indexPrefix);
 		File[] indexedFiles = indexDir.listFiles();
 
 		indexedFiles = indexDir.listFiles();
 		for (File file : indexedFiles) {
 			if (file.getName().equals(".DS_Store") || file.getName().equals("doc_map.csv"))
 				continue;
 			BufferedReader ois = new BufferedReader(new FileReader(file.getAbsoluteFile()));
 			String o;
 			while (((o = ois.readLine()) != null)) {
 				String[] eachLine = o.split("\t");
 				String key = eachLine[0];
 				WordAttribute wa = new WordAttribute();
 				if (wordMap.containsKey(key)) {
 					WordAttribute wa1 = wordMap.get(key);
 					wa1.setFreq(Integer.parseInt(eachLine[eachLine.length - 1]) + wa1.getFreq());
 					List<Integer> tmpList = new ArrayList<Integer>();
 					for (int i = 1; i < eachLine.length - 1; i++) {
 						tmpList.add(Integer.parseInt(eachLine[i]));
 					}
 					wa1.getList().addAll(tmpList);
 					tmpList = null;
 				} else {
 					wa.setFreq(Integer.parseInt(eachLine[eachLine.length - 1]));
 					List<Integer> tmpList = new ArrayList<Integer>();
 					for (int i = 1; i < eachLine.length - 1; i++) {
 						tmpList.add(Integer.parseInt(eachLine[i]));
 					}
 					wa.setList(tmpList);
 					tmpList = null;
 					wordMap.put(key, wa);
 				}
 			}
 
 			String s = file.getName().split("_")[0];
 
 			BufferedWriter oos = new BufferedWriter(new FileWriter(_options._indexPrefix + "/" + s + ".csv", true));
 
 			for (String si : wordMap.keySet()) {
 				oos.write(si + "\t");
 				WordAttribute wa = wordMap.get(si);
 
 				for (int i : wa.getList()) {
 					oos.write(i + "\t");
 				}
 				oos.write(wa.getFreq() + "");
 				oos.newLine();
 			}
 			wordMap.clear();
 			oos.close();
 			ois.close();
 			file.delete();
 		}
 	}
 
 	private void serialize() throws IOException {
 
 		StringBuilder builder = new StringBuilder(_options._indexPrefix).append("/").append("doc_map.csv");
 		BufferedWriter aoos = new BufferedWriter(new FileWriter(builder.toString(), true));
 		for (int doc : docMap.keySet()) {
 			aoos.write(doc + "\t");
 			DocumentIndexed docIndexed = docMap.get(doc);
 			aoos.write(docIndexed.getTitle() + "\t" + docIndexed.getUrl() + "\t");
 			HashMap<String, Integer> wordFreq = docIndexed.getWordFrequency();
 			for (String s : wordFreq.keySet()) {
 				aoos.write(s + "\t" + wordFreq.get(s) + "\t");
 			}
 			aoos.write(docIndexed.getTotalWords() + "");
 		}
 		aoos.close();
 		docMap.clear();
 
 		for (String firstLetter : mapOfMaps.keySet()) {
 			StringBuilder file = new StringBuilder(_options._indexPrefix).append("/").append(firstLetter)
 					.append("_tmp.csv");
 			BufferedWriter oos = new BufferedWriter(new FileWriter(file.toString(), true));
 
 			HashMap<String, WordAttribute> attr = mapOfMaps.get(firstLetter);
 			for (String s : attr.keySet()) {
 				oos.write(s + "\t");
 				WordAttribute wa = attr.get(s);
 
 				for (int i : wa.getList()) {
 					oos.write(i + "\t");
 				}
 				oos.write(wa.getFreq() + "");
 				oos.newLine();
 			}
 			oos.close();
 		}
 	}
 
 	private void analyse(File eachFile, int index) throws IOException {
 		DocumentIndexed docIndexed = new DocumentIndexed(index);
 		docIndexed.setTitle(eachFile.getName());
 		docIndexed.setUrl(eachFile.getPath());
 
 		String newFile = Parser.parse(eachFile);
 		Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_30, new HashSet<String>());
 		List<String> words = tokenize(analyzer.tokenStream("", new StringReader(newFile)));
 		for (String word : words) {
 			String stemmed = Stemmer.stemAWord(word);
 			if (stemmed.matches("[A-Za-z0-9\\p{Punct}\\s]+")) {
 				String first = stemmed.substring(0, 1);
 				HashMap<String, WordAttribute> currCharMap = mapOfMaps.get(first);
 				if (currCharMap.containsKey(stemmed)) {
 					WordAttribute tmpAttr = currCharMap.get(stemmed);
 					List<Integer> tmpList = tmpAttr.getList();
 					if (!tmpList.contains(index)) {
 						tmpList.add(index);
 					}
 					int tmpFreq = tmpAttr.getFreq();
 					tmpFreq++;
 					tmpAttr.setFreq(tmpFreq);
 					currCharMap.put(stemmed, tmpAttr);
 				} else {
 					WordAttribute tmpAttr = new WordAttribute();
 					List<Integer> tmpList = new ArrayList<Integer>();
 					tmpList.add(index);
 					tmpAttr.setFreq(1);
 					tmpAttr.getList().addAll(tmpList);
 					currCharMap.put(stemmed, tmpAttr);
 				}
 
 				docIndexed.incrementWordFrequency(stemmed);
 			}
 		}
 		analyzer.close();
 		docIndexed.setTotalWords(words.size());
 
 		docMap.put(index, docIndexed);
 	}
 
 	private void initializeMap() {
 
 		mapOfMaps = new HashMap<String, HashMap<String, WordAttribute>>();
 
 		for (int j = 0; j < 26; j++) {
 			HashMap<String, WordAttribute> tmp = new HashMap<String, WordAttribute>();
 			String name = Character.toString((char) (j + 'a'));
 			mapOfMaps.put(name, tmp);
 		}
 
 		for (int j = 0; j < 10; j++) {
 			HashMap<String, WordAttribute> tmp = new HashMap<String, WordAttribute>();
 			String name = Character.toString((char) (j + '0'));
 			mapOfMaps.put(name, tmp);
 		}
 
 	}
 
 	@Override
 	public void loadIndex() throws IOException, ClassNotFoundException {
 		wordMap = new HashMap<String, WordAttribute>();
 		
 		File indexDir = new File(_options._indexPrefix);
 		File[] indexedFiles = indexDir.listFiles();
 
 		for (File file : indexedFiles) {
 			if (file.getName().equals(".DS_Store"))
 				continue;
 			
			if(file.getName().equals("doc_map.csv"))
 				loadDocMap(file);
 			
 			if(file.getName().equals("f.csv"))
 				break;
 			
 			BufferedReader ois = new BufferedReader(new FileReader(file.getAbsoluteFile()));
 			String o;
 			while (((o = ois.readLine()) != null)) {
 				String[] eachLine = o.split("\t");
 				String key = eachLine[0];
 				WordAttribute wa = new WordAttribute();
 				// get the frequency for the words
 				wa.setFreq(Integer.parseInt(eachLine[eachLine.length - 1]));
 				List<Integer> tmpList = new ArrayList<Integer>();
 				for (int i = 1; i < eachLine.length - 1; i++) {
 					tmpList.add(Integer.parseInt(eachLine[i]));
 				}
 				wa.setList(tmpList);
 				tmpList = null;
 				wordMap.put(key, wa);
 			}
 			ois.close();
 		}
 	}
 
 	private void loadDocMap(File file) throws NumberFormatException, IOException {
 		docMap = new HashMap<Integer, DocumentIndexed>();
 		
 		BufferedReader ois = new BufferedReader(new FileReader(file.getAbsoluteFile()));
 		String o;
 		while (((o = ois.readLine()) != null)) {
 			String[] eachLine = o.split("\t");
 			int did = Integer.parseInt(eachLine[0]);
 			if(did > totalNoOfFiles/5)
 				break;
 			DocumentIndexed wa = new DocumentIndexed(did);
 			String title = eachLine[1];
 			String url = eachLine[2];
 			
 			int i = 3;
 			HashMap<String, Integer> wordFreq = new HashMap<String, Integer>();
 			
 			while(i < eachLine.length-1) {
 				String word = eachLine[i];
 				i++;
 				int freq = Integer.parseInt(eachLine[i]);
 				wordFreq.put(word, freq);
 			}
 			int totalWords = Integer.parseInt(eachLine[eachLine.length-1]);
 			
 			wa.setTitle(title);
 			wa.setUrl(url);
 			wa.setWordFrequency(wordFreq);
 			wa.setTotalWords(totalWords);
 			
 			docMap.put(did, wa);
 		}
 		ois.close();
 	}
 
 	@Override
 	public Document getDoc(int docid) {
 		if(!checkInCache(docid)) {
 			
 		}
 		return null;
 	}
 
 	private boolean checkInCache(int docid) {
 		return docMap.containsKey(docid);
 	}
 	
 	private void loadDocInCache(int did) {
 		if(docMap.containsKey(did))
 			return;
 		
 		
 	}
 
 	/**
 	 * In HW2, you should be using {@link DocumentIndexed}
 	 */
 	@Override
 	public Document nextDoc(Query query, int docid) {
 		query.processQuery();
 
 		// if docid is -1 then make docid=0
 		if (docid == -1) {
 			docid = 0;
 		}		
 		//Pick each word in the query and extract the list of the nextDoc List
 		//Now pick a docid from a word and run a binary search in all the other
 		// docID list of other words.
 
 		// First find out the smallest list among the list of all the words
 		String smallestListWord = null;
 		try {
 			smallestListWord = findWordWithSmallestList(query);
 			if (smallestListWord == null)
 				return null;
 
 			// Now take a next docId form the list of the smallestListWord
 			WordAttribute smallestWordAttribute = wordMap.get(smallestListWord);
 			List<Integer> smallestList = smallestWordAttribute.getList();
 
 			// Find the position of docid in the smallestListWord
 			int position = searchForIdInWordList(smallestListWord, docid);
 
 			// Now iterate the list linearly and apply binary search in all the
 			// other words
 			for (int i = position + 1; i < smallestList.size(); i++) {
 				int currentDocId = smallestList.get(i);
 				// Now loop through the other words and apply binary search on
 				// all
 				boolean value = isPresentInAll(currentDocId, smallestListWord, query);
 				if (value == true) {
 					return docMap.get(i);
 				}
 			}
 
 		} catch (IOException e) {
 			// TODO
 		}
 		return null;
 	}
 
 	private boolean isPresentInAll(int docid, String originalWord, Query query) throws IOException {
 		ArrayList<String> tokens = new ArrayList<String>();
 		for (String s : query._tokens) {
 			boolean flag = false;
 			if (!isPresentInCache(s)) {
 				flag = loadInCache(s);
 				if (flag)
 					tokens.add(s);
 			}
 		}
 		for (String str : tokens) {
 			if (str == originalWord) {
 				continue;
 			} else if (searchForIdInWordList(str, docid) != -1) {
 				continue;
 			} else {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * @param smallestList
 	 * @param docid
 	 * @return the index where the docid is present in the list
 	 */
 	private int searchForIdInWordList(String word, int docid) {
 		// Applying bInary Search to search for the docid in the list of the
 		// word
 		WordAttribute currentWordAttribute = wordMap.get(word);
 		List<Integer> smallestList = currentWordAttribute.getList();
 		int low = 0;
 		int high = smallestList.size();
 
 		while (low <= high) {
 			// Calculate the middle of the array via the bitwise operator for
 			// unsigned right-shift. This the same as writing ( low + high ) /
 			// 2^1
 			// only faster. In addition, it shifts a zero into the leftmost
 			// position.
 			int middle = (low + high) >> 1;
 
 			if (smallestList.get(middle) > docid) {
 				// Continue searching the lower part of the array
 				high = middle - 1;
 			} else if (smallestList.get(middle) < docid) {
 				// Continue searching the upper part of the array
 				low = middle + 1;
 			} else {
 				// We've found the array index of the value
 				return middle;
 			}
 		}
 		return -1;
 	}
 
 	private String findWordWithSmallestList(Query query) throws IOException {
 		int minListLength = 0;
 		String smallestListWord = null;
 		for (String strTemp : query._tokens) {
 			boolean flag = false;
 			if (!isPresentInCache(strTemp)) {
 				flag = loadInCache(strTemp, query);
 				if (flag == false)
 					continue;
 			}
 			WordAttribute currWordAttribute = wordMap.get(strTemp);
 			int listSize = currWordAttribute.getList().size();
 			if (minListLength > listSize) {
 				minListLength = listSize;
 				smallestListWord = strTemp;
 			}
 		}
 		return smallestListWord;
 	}
 
 	private boolean isPresentInCache(String word) {
 		return wordMap.containsKey(word);
 	}
 
 	private boolean loadInCache(String word, Query query) throws IOException {
 		if(wordMap.containsKey(word))
 			return false;
 		
 		List<String> to_be_removed = new ArrayList<String>();
 		int k = 0;
 		for(String s : wordMap.keySet()) {
 			if(k >= query._tokens.size())
 				break;
 			if(!query._tokens.contains(s))
 				to_be_removed.add(s);
 			k++;
 		}
 		
 		for(String s : to_be_removed)
 			wordMap.remove(s);
 		
 		boolean flag = false;
 		String firstLetter = word.substring(0, 1);
 		List<String> commands = new ArrayList<String>();
 		commands.add("/bin/bash");
 		commands.add("-c");
 		commands.add("grep $'^" + word + "\t' " + _options._indexPrefix + "/" + firstLetter + ".csv");
 		ProcessBuilder pb = new ProcessBuilder(commands);
 		Process p = pb.start();
 		BufferedReader ois = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
 		String o;
 		while (((o = ois.readLine()) != null)) {
 			String[] eachLine = o.split("\t");
 			String key = eachLine[0];
 			WordAttribute wa = new WordAttribute();
 
 			wa.setFreq(Integer.parseInt(eachLine[eachLine.length - 1]));
 			List<Integer> tmpList = new ArrayList<Integer>();
 			for (int i = 1; i < eachLine.length - 1; i++) {
 				tmpList.add(Integer.parseInt(eachLine[i]));
 			}
 			wa.setList(tmpList);
 			tmpList = null;
 			wordMap.put(key, wa);
 			flag = true;
 		}
 		ois.close();
 		
 		return flag;
 	}
 	
 	private boolean loadInCache(String word) throws IOException {
 		if(wordMap.containsKey(word))
 			return false;
 		
 		
 		boolean flag = false;
 		String firstLetter = word.substring(0, 1);
 		List<String> commands = new ArrayList<String>();
 		commands.add("/bin/bash");
 		commands.add("-c");
 		commands.add("grep $'^" + word + "\t' " + _options._indexPrefix + "/" + firstLetter + ".csv");
 		ProcessBuilder pb = new ProcessBuilder(commands);
 		Process p = pb.start();
 		BufferedReader ois = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
 		String o;
 		while (((o = ois.readLine()) != null)) {
 			String[] eachLine = o.split("\t");
 			String key = eachLine[0];
 			WordAttribute wa = new WordAttribute();
 
 			wa.setFreq(Integer.parseInt(eachLine[eachLine.length - 1]));
 			List<Integer> tmpList = new ArrayList<Integer>();
 			for (int i = 1; i < eachLine.length - 1; i++) {
 				tmpList.add(Integer.parseInt(eachLine[i]));
 			}
 			wa.setList(tmpList);
 			tmpList = null;
 			wordMap.put(key, wa);
 			flag = true;
 		}
 		ois.close();
 		
 		return flag;
 	}
 
 	@Override
 	public int corpusDocFrequencyByTerm(String term) {
 		boolean flag = false;
 		if (!isPresentInCache(term)) {
 			try {
 				flag = loadInCache(term);
 				if (flag == false)
 					return 0;
 			} catch (IOException e) {
 				// TODO
 			}
 		}
 		return wordMap.get(term).getList().size();
 	}
 
 	@Override
 	public int corpusTermFrequency(String term) {
 		boolean flag = false;
 		if (!isPresentInCache(term)) {
 			try {
 				flag = loadInCache(term);
 				if (flag == false)
 					return 0;
 			} catch (IOException e) {
 				// TODO
 			}
 		}
 		return (int) wordMap.get(term).getFreq();
 	}
 
 	@Override
 	public int documentTermFrequency(String term, String url) {
 		SearchEngine.Check(false, "Not implemented!");
 		return 0;
 	}
 }
