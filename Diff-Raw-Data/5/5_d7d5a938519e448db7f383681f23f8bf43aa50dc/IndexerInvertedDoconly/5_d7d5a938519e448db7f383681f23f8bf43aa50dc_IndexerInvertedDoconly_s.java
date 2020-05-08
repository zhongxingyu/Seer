 package edu.nyu.cs.cs2580;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import edu.nyu.cs.cs2580.SearchEngine.Options;
 
 /**
  * @CS2580: Implement this class for HW2.
  */
 public class IndexerInvertedDoconly extends Indexer {
 	final int BULK_DOC_PROCESSING_SIZE = 1000;
 	final int BULK_DOC_WRITE_SIZE = 300;
 	final String METADATA_FILE = "index.dat";
 	private Vector<Document> _documents = new Vector<Document>();
 	private Map<Character, Map<String, Map<Integer, Integer>>> _characterMap;
 	private Map<String, Long> _docMap;
 	private Map<String, Integer> _corpusTermFrequency;
 	private Map<String, Integer> _corpusTermFrequencyCache;
 	private Map<String, List<Integer>> _wordToDocListMap;
 	private Map<Integer, DocumentIndexed> _docIdMap;
 	private Map<Integer, Document> _docIdToDocMap;
 	private Map<String, Map<Integer, Integer>> _TermFrequencyInDocMap;
 	private List<Document> _documentCache;
 
 	public IndexerInvertedDoconly(Options options) {
 		super(options);
 		_docIdToDocMap = new HashMap<Integer, Document>();
 		_documentCache = new ArrayList<Document>();
 		_TermFrequencyInDocMap = new HashMap<String, Map<Integer, Integer>>();
 		_docIdMap = new HashMap<Integer, DocumentIndexed>();
 		_wordToDocListMap = new HashMap<String, List<Integer>>();
 		_corpusTermFrequency = new HashMap<String, Integer>();
 		_corpusTermFrequencyCache = new HashMap<String, Integer>();
 		_characterMap = new HashMap<Character, Map<String, Map<Integer, Integer>>>();
 		_docMap = new HashMap<String, Long>();
 	}
 
 	@Override
 	public void constructIndex() throws IOException {
 		List<String> documents = Utility
 				.getFilesInDirectory(_options._corpusPrefix);
 
 		for (String filename : documents) {
 			System.out.println("Processing ... " + _numDocs);
 			processDocument(filename);
 			if (_numDocs % BULK_DOC_PROCESSING_SIZE == 0) {
 				writeFile(_characterMap);
 				_characterMap.clear();
 				writeFrequency(_corpusTermFrequency);
 				_corpusTermFrequency.clear();
 			}
 		}
 		if (!_characterMap.isEmpty()) {
 			writeFile(_characterMap);
 			_characterMap.clear();
 		}
 		if (!_documents.isEmpty()) {
 			_persistentStore.saveDoc(
 					_options._indexPrefix + "/" + String.valueOf("Doc")
 							+ ".dat", _documents);
 			_documents.clear();
 		}
 		if (!_corpusTermFrequency.isEmpty()) {
 			writeFrequency(_corpusTermFrequency);
 			_corpusTermFrequency.clear();
 		}
 		mergeAll();
 		_documents.clear();
 		saveIndexMetadata();
 	}
 
 	private void writeFrequency(Map<String, Integer> frequency)
 			throws IOException {
 		String path = _options._indexPrefix + "/" + _numDocs + ".freq";
 		File file = new File(path);
 		OutputStream out = new FileOutputStream(file, true);
 		for (Map.Entry<String, Integer> entry1 : frequency.entrySet()) {
 			out.write(entry1.getKey().getBytes());
 			out.write(" ".getBytes());
 			out.write(entry1.getValue().toString().getBytes());
 			out.write("\n".getBytes());
 		}
 		out.close();
 	}
 
 	private void saveIndexMetadata() throws IOException {
 		Map<String, Long> dataMap = new HashMap<String, Long>();
 		dataMap.put("numDocs", new Long(_numDocs));
 		dataMap.put("totalTermFrequency", _totalTermFrequency);
 		dataMap.putAll(_docMap);
 		String metaDataFile = _options._indexPrefix + "/" + METADATA_FILE;
 		_persistentStore.saveIndexMetadata(metaDataFile, dataMap);
 	}
 
 	private void processDocument(String filename) throws MalformedURLException,
 			IOException {
 		String corpusFile = _options._corpusPrefix + "/" + filename;
 		int docId = _numDocs;
 		String document = Utility.extractText(corpusFile);
 		List<String> stemmedTokens = Utility.tokenize(document);
 		buildMapFromTokens(docId, filename, stemmedTokens);
 		_numDocs++;
 	}
 
 	private void buildMapFromTokens(int docId, String docName,
 			List<String> stemmedTokens) {
 		DocumentIndexed doc = new DocumentIndexed(docId);
 		Map<String, Integer> termFrequency = new HashMap<String, Integer>();
 		for (String stemmedToken : stemmedTokens) {
 			if (_corpusTermFrequency.containsKey(stemmedToken)) {
 				int value = _corpusTermFrequency.get(stemmedToken);
 				value++;
 				_corpusTermFrequency.put(stemmedToken, value);
 			} else {
 				_corpusTermFrequency.put(stemmedToken, 1);
 			}
 			if (termFrequency.containsKey(stemmedToken)) {
 				int value = termFrequency.get(stemmedToken);
 				value++;
 				termFrequency.put(stemmedToken, value);
 			} else {
 				termFrequency.put(stemmedToken, 1);
 			}
 			char start = stemmedToken.charAt(0);
 			if (_characterMap.containsKey(start)) {
 				Map<String, Map<Integer, Integer>> wordMap = _characterMap
 						.get(start);
 				if (wordMap.containsKey(stemmedToken)) {
 					Map<Integer, Integer> docList = wordMap.get(stemmedToken);
 					if (!docList.containsKey(docId)) {
 						docList.put(docId, 1);
 					} else {
 						int tempCount = docList.get(docId) + 1;
 						docList.put(docId, tempCount);
 					}
 					wordMap.put(stemmedToken, docList);
 				} else {
 					Map<Integer, Integer> tempMap = new TreeMap<Integer, Integer>();
 					tempMap.put(docId, 1);
 					wordMap.put(stemmedToken, tempMap);
 				}
 			} else {
 				Map<String, Map<Integer, Integer>> tempMap = new HashMap<String, Map<Integer, Integer>>();
 				Map<Integer, Integer> tempInnerMap = new TreeMap<Integer, Integer>();
 				tempInnerMap.put(docId, 1);
 				tempMap.put(stemmedToken, tempInnerMap);
 				_characterMap.put(start, tempMap);
 			}
 		}
 		_totalTermFrequency = _totalTermFrequency + stemmedTokens.size();
 		doc.setUrl(docName);
 		doc.setTotalWordsInDoc(stemmedTokens.size());
 		_docMap.put(docName, new Long(docId));
 		_documents.add(doc);
 	}
 
 	private void mergeAll() throws IOException {
 		List<String> files = Utility.getFilesInDirectory(_options._indexPrefix);
 		for (String file : files) {
 			if (file.endsWith(".idx")) {
 				System.out.println("Merging ... " + file);
 				Map<Character, Map<String, Map<Integer, Integer>>> characterMap = readAll(file);
 				String fileName = _options._indexPrefix + "/" + file;
 				File charFile = new File(fileName);
 				charFile.delete();
 				writeFile(characterMap);
 			}
 		}
 	}
 
 	private Map<Character, Map<String, Map<Integer, Integer>>> readAll(
 			String fileName) throws FileNotFoundException {
 		String file = _options._indexPrefix + "/" + fileName;
 		Scanner scan = new Scanner(new File(file));
 		Map<Character, Map<String, Map<Integer, Integer>>> CharacterMap = new HashMap<Character, Map<String, Map<Integer, Integer>>>();
 		Map<String, Map<Integer, Integer>> tempMap = new HashMap<String, Map<Integer, Integer>>();
 		while (scan.hasNextLine()) {
 			String line = scan.nextLine();
 			String lineArray[] = line.split(";;");
 			String word = lineArray[0];
 			Map<Integer, Integer> tempList = new TreeMap<Integer, Integer>();
 			for (int i = 1; i < lineArray.length; i++) {
 				String[] tempSt = lineArray[i].split(",");
 				tempList.put(Integer.parseInt(tempSt[0]),
 						Integer.parseInt(tempSt[1]));
 			}
 			if (tempMap.containsKey(word)) {
 				Map<Integer, Integer> temp = tempMap.get(word);
 				temp.putAll(tempList);
 				tempMap.put(word, temp);
 			} else {
 				tempMap.put(word, tempList);
 			}
 		}
 		CharacterMap.put(fileName.charAt(0), tempMap);
 		return CharacterMap;
 	}
 
 	private void writeFile(
 			Map<Character, Map<String, Map<Integer, Integer>>> characterMap)
 			throws IOException {
 		for (Map.Entry<Character, Map<String, Map<Integer, Integer>>> entry : characterMap
 				.entrySet()) {
 			String path = _options._indexPrefix + "/" + entry.getKey() + ".idx";
 			File file = new File(path);
 			BufferedWriter write = new BufferedWriter(
 					new FileWriter(file, true));
 			Map<String, Map<Integer, Integer>> tempMap = entry.getValue();
 			for (Map.Entry<String, Map<Integer, Integer>> entry1 : tempMap
 					.entrySet()) {
 				String wordName = entry1.getKey();
 				Map<Integer, Integer> innerMostMap = entry1.getValue();
 				write.write(wordName);
 				StringBuffer sb = new StringBuffer();
 				for (Map.Entry<Integer, Integer> innerEntry : innerMostMap
 						.entrySet()) {
 					sb.append(";;").append(innerEntry.getKey()).append(",")
 							.append(innerEntry.getValue());
 				}
 				write.write(sb.toString());
 				write.write("\n");
 			}
 			write.close();
 		}
 	}
 
 	@Override
 	public void loadIndex() throws IOException, ClassNotFoundException {
 		loadIndexMetadata();
 	}
 
 	private void loadIndexMetadata() throws IOException {
 		String metaDataFile = _options._indexPrefix + "/" + METADATA_FILE;
 		_docMap = _persistentStore.loadIndexMetadata(metaDataFile);
 		_totalTermFrequency = _docMap.get("totalTermFrequency");
 		_docMap.remove("totalTermFrequency");
 		_numDocs = _docMap.get("numDocs").intValue();
 		_docMap.remove("numDocs");
 		_documentCache = _persistentStore.loadDocForDocOnly(_options._indexPrefix + "/"
 				+ "Doc.dat");
 		for (Document doc : _documentCache) {
 			_docIdToDocMap.put(doc._docid, doc);
 		}
 	}
 
 	@Override
 	public Document getDoc(int docid) {
 		return _docIdToDocMap.get(docid);
 	}
 
 	/**
 	 * In HW2, you should be using {@link DocumentIndexed}
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public Document nextDoc(Query query, int docid) {
 		try {
 			query.processQuery();
 			List<String> queryVector = query._tokens;
 			List<List<Integer>> list = new ArrayList<List<Integer>>();
 			for (String search : queryVector) {
 				if (_wordToDocListMap.containsKey(search)) {
 					list.add(_wordToDocListMap.get(search));
 				} else {
 					String fileName = _options._indexPrefix + "/"
 							+ search.charAt(0) + ".idx";
 					System.out.println("Search " + fileName);
 					Map<Integer, Integer> tempIntegerMap = grepFile(search,
 							fileName);
 					_TermFrequencyInDocMap.put(search, tempIntegerMap);
 					List<Integer> tempList = new ArrayList<Integer>();
 					for(Map.Entry<Integer, Integer> entry : tempIntegerMap.entrySet()){
 						tempList.add(entry.getKey());
 					}
 					list.add(tempList);
 					System.out.println(list);
 					_wordToDocListMap.put(search, tempList);
 				}
 			}
 			if (list.size() == 1) {
 				int index = Collections.binarySearch(list.get(0), docid);
 				if (index + 1 <= list.get(0).size() - 1) {
 					return getDoc(list.get(0).get(index + 1));
 				} else {
 					return null;
 				}
 			}
 			int min = Integer.MAX_VALUE;
 			int index = Integer.MAX_VALUE;
 			for (int i = 0; i < list.size(); i++) {
 				if (list.get(i).size() < min) {
 					min = list.get(i).size();
 					index = i;
 				}
 			}
 			List<Integer> tempInteger = list.get(index);
 			list.remove(index);
 			int index1 = tempInteger.indexOf(docid);
 			for (int i = index1 + 1; i < tempInteger.size(); i++) {
 				boolean flag = false;
 				for (List<Integer> tempList1 : list) {
 					int tempIndex = Collections.binarySearch(tempList1,
 							tempInteger.get(i));
 					flag = tempIndex < 0 ? false : true;
 					if (!flag) {
 						break;
 					}
 				}
 				if (flag) {
 					return getDoc(tempInteger.get(i));
 				}
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private Map<Integer, Integer> grepFile(String search, String fileName)
 			throws IOException {
 		String cmd = "grep '\\<" + search + "\\>' " + fileName;
 		// System.out.println(cmd);
 		List<String> commands = new ArrayList<String>();
 		commands.add("/bin/bash");
 		commands.add("-c");
 		commands.add(cmd);
 		ProcessBuilder pb = new ProcessBuilder(commands);
 		Process p;
 		p = pb.start();
 		InputStreamReader isr = new InputStreamReader(p.getInputStream());
 		BufferedReader br = new BufferedReader(isr);
 		String s[];
 		String line = br.readLine();
 		// System.out.println(line);
 		s = line.split(";;");
 		Map<Integer, Integer> tempList = new TreeMap<Integer, Integer>();
 		for (int i = 1; i < s.length; i++) {
 			String[] tempSt = s[i].split(",");
 			tempList.put(Integer.parseInt(tempSt[0]),
 					Integer.parseInt(tempSt[1]));
 		}
 		System.out.println(tempList);
 		return tempList;
 	}
 
 	@Override
 	public int corpusDocFrequencyByTerm(String term) {
 		return 0;
 	}
 
 	@Override
 	public int corpusTermFrequency(String term) {
 		try {
 			if (_corpusTermFrequencyCache.containsKey(term)) {
 				return _corpusTermFrequencyCache.get(term);
 			}
 			int total = 0;
 			List<String> files = Utility.getFileInDirectory(
 					_options._indexPrefix, "", ".freq");
 			for (String file : files) {
 				String fileName = _options._indexPrefix + "/" + file;
 				String cmd = "grep '\\<" + term + "\\>' " + fileName;
 				List<String> commands = new ArrayList<String>();
 				commands.add("/bin/bash");
 				commands.add("-c");
 				commands.add(cmd);
 				ProcessBuilder pb = new ProcessBuilder(commands);
 				Process p;
 				p = pb.start();
 				InputStreamReader isr = new InputStreamReader(p.getInputStream());
 				BufferedReader br = new BufferedReader(isr);
 				String s[];
 				String line = br.readLine();
 				// System.out.println(line);
 				s = line.split(";;");
 				List<Integer> tempList = new ArrayList<Integer>();
 				for (int i = 1; i < s.length; i++) {
 					tempList.add(Integer.parseInt(s[i]));
 				}
				int value = tempList.get(0);
 				total += value;
 			}
 			if (_corpusTermFrequencyCache.size() < 50) {
 				_corpusTermFrequencyCache.put(term, total);
 			} else {
 				_corpusTermFrequencyCache.clear();
 				_corpusTermFrequencyCache.put(term, total);
 			}
 			return total;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	@Override
 	public int documentTermFrequency(String term, String url) {
 		int docid = _docMap.get(url).intValue();
 		return _TermFrequencyInDocMap.get(term).get(docid);
 	}
 
 	public static void main(String[] args) throws IOException, ClassNotFoundException {
 		Options option = new Options("conf/engine.conf");
 		IndexerInvertedDoconly in = new IndexerInvertedDoconly(option);
 		Date d = new Date();
 		int docid = -1;
 		in.loadIndex();
 		Document doc; 
 		Query query = new Query("web");
 		while((doc = in.nextDoc(query, docid)) != null){
 			System.out.println(doc._docid + doc.getUrl());
 			docid = doc._docid;
 		}
 		//in.constructIndex();
 		
 		/*  Query query = new Query("eddie 6 strings"); Long start =
 		 // System.currentTimeMillis(); Document doc = in.nextDoc(query, 2);
 		 * Document doc2 = in.nextDoc(query, doc._docid); Long end =
 		 * System.currentTimeMillis(); System.out.println(doc._docid);
 		 * System.out.println(doc2._docid);
 		 */
 		Date d1 = new Date();
 		System.out.println(d);
 		System.out.println(d1);
 		/*
 		 * // DocumentIndexed inh = new DocumentIndexed(45);
 		 * System.out.println(in.corpusTermFrequency("web"));
 		 * System.out.println(in.corpusTermFrequency("web"));
 		 */
 
 	}
 }
