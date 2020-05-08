 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 
 
 public class TextMinerFuncs
 {
 	private final static String NUMBER_PATTERN = "-?\\d+?\\.?\\d*?";
 	
 	public static void tokenize(List<Bookmark> bookmarks)
 	{
 		for (Bookmark bookmark : bookmarks) {
 			bookmark.tokens = Arrays.asList(bookmark.body.toLowerCase().split(" "));
 		}
 	}
 	
 	private static String stemToken(String token, Map<String,String> stemTable)
 	{
 		if (token.length() < 4) return token;
 		if (token.matches(NUMBER_PATTERN)) return token;
 //		if (token is acronym) return token
 		if (stemTable.containsKey(token)) return stemTable.get(token);
 //		if (token.endsWith("s'") return token.substring(0, token.length()-2);
 		// ...
 		return token;
 	}
 	
 	public static void stem(List<Bookmark> bookmarks, String stemwordsFilePath)
 	{
 		// get stem table from file
 		Map<String,String> stemTable = new HashMap<String,String>();
		File stemsFile = new File(stemwordsFilePath);
 		Scanner sc = null;
 		try {
			sc = new Scanner(stemsFile);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.exit(1);
 		}
 		while (sc.hasNext()) {
 			String root = sc.next().trim();
 			String token = sc.next().trim();
 			stemTable.put(token, root);
 		}
 		
 		// stem words from stem table
 		for (Bookmark bookmark : bookmarks) {
 			List<String> tokens = bookmark.tokens;
 			for (int i = 0; i < tokens.size(); ++i) {
 				String token = tokens.get(i);
 				tokens.set(i, stemToken(token, stemTable));
 			}
 		}
 		
 		// stem words using algorithm TODO
 		
 	}
 	
 	public static void stop(List<Bookmark> bookmarks, String stopwordsFilePath)
 	{
 		Set<String> stopwords = new HashSet<String>();
 		File stopwordsFile = new File(stopwordsFilePath);
 		Scanner sc = null;
 		try {
 			sc = new Scanner(stopwordsFile);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.exit(1);
 		}
 		while (sc.hasNext()) {
 			stopwords.add(sc.next().trim());
 		}
 		for (Bookmark bookmark : bookmarks) {
 			Iterator<String> tokensIter = bookmark.tokens.iterator();
 			while (tokensIter.hasNext()) {
 				String token = tokensIter.next();
 				if (stopwords.contains(token)) {
 					tokensIter.remove();
 				}
 			}
 		}
 	}
 	
 	public static List<String> getDict(List<Bookmark> bookmarks)
 	{
 		SortedSet<String> dict = new TreeSet<String>();
 		for (Bookmark bookmark : bookmarks) {
 			dict.addAll(bookmark.tokens);
 		}
 		return ImmutableList.copyOf(dict);
 	}
 	
 	public static Map<String,Integer> getTokenLookupTable(List<String> dict)
 	{
 		Map<String,Integer> tokenLookup = new HashMap<String,Integer>(dict.size());
 		for (int i = 0; i < dict.size(); ++i) {
 			tokenLookup.put(dict.get(i), i);
 		}
 		return ImmutableMap.copyOf(tokenLookup);
 	}
 	
 	public static void calcTfs(List<Bookmark> bookmarks)
 	{
 		for (Bookmark bookmark : bookmarks) {
 			Map<Integer,Integer> tf = new HashMap<Integer,Integer>(); 
 			for (int tokenId : bookmark.bodyCompressed) {
 				if (tf.containsKey(tokenId)) {
 					tf.put(tokenId, tf.get(tokenId)+1);
 				} else {
 					tf.put(tokenId, 1);
 				}
 			}
 			bookmark.tf = ImmutableMap.copyOf(tf);
 			List<TokenValue> sortedTf = new ArrayList<TokenValue>(tf.size());
 			for (int tokenId : tf.keySet()) {
 				sortedTf.add(new TokenValue(tokenId, tf.get(tokenId)));
 			}
 			Collections.sort(sortedTf);
 			bookmark.sortedTf = ImmutableList.copyOf(sortedTf);
 		}
 	}
 	
 	public static Map<Integer,Integer> calcTfidfs(List<Bookmark> bookmarks)
 	{
 		// create df for all docs and tf for each doc
 		Map<Integer,Integer> df = new HashMap<Integer,Integer>();
 		for (Bookmark bookmark : bookmarks) {
 			Map<Integer,Integer> tf = new HashMap<Integer,Integer>(); 
 			for (int tokenId : bookmark.bodyCompressed) {
 				if (tf.containsKey(tokenId)) {
 					tf.put(tokenId, tf.get(tokenId)+1);
 				} else {
 					tf.put(tokenId, 1);
 					if (df.containsKey(tokenId)) {
 						df.put(tokenId, df.get(tokenId)+1);
 					} else {
 						df.put(tokenId, 1);
 					}
 				}
 			}
 			bookmark.tf = ImmutableMap.copyOf(tf);
 			List<TokenValue> sortedTf = new ArrayList<TokenValue>(tf.size());
 			for (int tokenId : tf.keySet()) {
 				sortedTf.add(new TokenValue(tokenId, tf.get(tokenId)));
 			}
 			Collections.sort(sortedTf, Collections.reverseOrder());
 			bookmark.sortedTf = ImmutableList.copyOf(sortedTf);
 		}
 		
 		// compute tfidf for each doc
 		for (Bookmark bookmark : bookmarks) {
 			Map<Integer,Double> tfidf = new HashMap<Integer,Double>(bookmark.tf.size());
 			for (int tokenId : bookmark.tf.keySet()) {
 				double idf = Math.log( 1.0 * bookmarks.size() / df.get(tokenId) );
 				tfidf.put(tokenId, bookmark.tf.get(tokenId) * idf);
 			}
 			bookmark.tfidf = ImmutableMap.copyOf(tfidf);
 			List<TokenValue> sortedTfidf = new ArrayList<TokenValue>(tfidf.size());
 			for (int tokenId : tfidf.keySet()) {
 				sortedTfidf.add(new TokenValue(tokenId, tfidf.get(tokenId)));
 			}
 			Collections.sort(sortedTfidf, Collections.reverseOrder());
 			bookmark.sortedTfidf = ImmutableList.copyOf(sortedTfidf);
 		}
 		
 		return ImmutableMap.copyOf(df);
 	}
 }
