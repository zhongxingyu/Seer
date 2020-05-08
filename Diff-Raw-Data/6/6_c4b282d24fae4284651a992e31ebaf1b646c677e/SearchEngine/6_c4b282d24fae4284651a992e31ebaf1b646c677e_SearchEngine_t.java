 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.PriorityQueue;
 import java.util.Scanner;
 
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.util.Version;
 
 
 public class SearchEngine {
 	File docsDir;
 	File indexDir;
 	File queryFile;
 	File answerFile;
 	
 	public SearchEngine(String docsDir, String indexDir, String queryFile, String answerFile) {
 		this.docsDir = new File(docsDir);
 		this.indexDir = new File(indexDir);
 		this.queryFile = new File(queryFile);
 		this.answerFile = new File(answerFile);
 	}
 	
 	/**
 	 * a)
 	 * Creates a list of (number of words, frequency) pairs
 	 * Prints the 5 most frequent and the 5 least frequent words
 	 */
 	private void verifyZipf(int numResults) {
 		HashMap<String, Integer> occurences = new HashMap<String, Integer>();
 		StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_44);
 		TokenStream stream;
 		
 		try {
 			File[] children = docsDir.listFiles();
 			Scanner scanner;
 			for (File child : children) {
 				scanner = new Scanner(new FileReader(child));
 				while ( scanner.hasNextLine() ){
 					String line = scanner.nextLine();
 
 					stream = sa.tokenStream(null, new StringReader(line)); //content
 					CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
 					stream.reset();
 			        while (stream.incrementToken())
 			        {
 			        	String token = cattr.toString();
 			            if (occurences.containsKey(token)) {
 			            	occurences.put(token, occurences.get(token) + 1);
 			            } else {
 			            	occurences.put(token, 1);
 			            }
 			        }
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		PriorityQueue<Tuple> highQ = new PriorityQueue<Tuple>();
 		PriorityQueue<Tuple> lowQ = new PriorityQueue<Tuple>();
 		
 		for (String token : occurences.keySet()) {
 			int value = occurences.get(token);
 			//maintains a a queue with the numResults greatest values
 			if (highQ.size() >= numResults) {
 				if (highQ.peek().value < value) {
 					highQ.poll();
 					highQ.add(new Tuple(token, value));
 				}
 			} else {
 				highQ.add(new Tuple(token, value));
 			}
 			
 			//maintains a a queue with the numResults lowest values
 			if (lowQ.size() >= numResults) {
 				if (lowQ.peek().value < -value) {
 					lowQ.poll();
 					lowQ.add(new Tuple(token, -value));
 				}
 			} else {
 				lowQ.add(new Tuple(token, -value));
 			}
 		}
 		
 		int numWords = occurences.size();
 		Tuple[] maxArr = new Tuple[highQ.size()];
 		for (int i = 0; i < numResults; i++) {
 			maxArr[i] = highQ.poll();
 		}
 		Arrays.sort(maxArr);
 		String max = "";
 		for (int i = 0; i < maxArr.length; i++) {
 			Tuple t = maxArr[i];
 			int r = (maxArr.length - i);
 			double prob = ((double) t.value / (double) numWords);
 			max = "Word: " + t.object 
 					+ ", Freq: " + t.value 
 					+ ", r: " + r 
 					+ ", prob: " + prob
 					+ ", c: " + (r*prob)
 					+ "\n" + max;
 		}
 		System.out.println(max);
 		
 		Tuple[] minArr = new Tuple[lowQ.size()];
 		for (int i = 0; i < numResults; i++) {
 			minArr[i] = lowQ.poll();
 		}
 		Arrays.sort(minArr);
 		String min = "";
 		for (int i = 0; i < minArr.length; i++) {
 			Tuple t = minArr[i];
 			int r = (minArr.length - i);
 			double prob = ((double) -t.value / (double) numWords);
 			min = "Word: " + t.object 
 					+ ", Freq: " + -t.value 
 					+ ", r: " + r 
 					+ ", prob: " + prob
 					+ ", c: " + (r*prob)
 					+ "\n" + min;
 		}
 		System.out.println(min);
 		System.out.println("Number of unique words: " + numWords);
 	}
 	
 	/**
 	 * b)
 	 * Creates an inverted index
 	 * Prints the 5 most frequent and the 5 least frequent words
 	 * Returns the inverted index
 	 */
 	private Object invertedIndexFrequencies() {
		HashMap<String, HashMap<String, Double>> invIndex = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, Double> docFreqI = new HashMap<String, Double>();
		docFreqI.put("why hello there tre", 0.0);
		invIndex.put("the_tre_word",docFreqI);
 		return invIndex;
 	}
 
 	/**
 	 * c)
 	 * tf.idf(i) = termFrequency(i) * log(total#documents / documentFrequency(i))
 	 * Prints Precision@5 on the queries
 	 */
 	private void tf_idf(Object invertedIndex) {
 		
 	}
 	
 	/**
 	 * d)
 	 * Figure out what formula is being used by IndexSearcher's DefaultSimilarity.
 	 */
 	private void examineDefaultSimilarity() {
 		
 	}
 	
 	/**
 	 * e)
 	 * Implement two versions of your own tf.idf similarity measures
 	 * Compare their performance
 	 */
 	private void compareTfidfs() {
 		
 	}
 	
 	/**
 	 * f)
 	 * Extend your program with a BM25 similarity measure. Experiment with varying the levels of b and k1
 	 */
 	private void bm25() {
 		
 	}
 	
 	/**
 	 * g)
 	 * Print the 3 most relevant documents in the CACM collection based on term frequency 
 	 * and the 3 most relevant documents using tf-idf weighting
 	 */
 	private void getMostRevelantDocuments() {
 		String query = "proposal or survey , binary variable , Fibonaccian";
 		
 	}
 
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		SearchEngine engine = new SearchEngine("data/txt/", "data/index/", "data/cacm_processed.query", "data/cacm_processed.rel");
 		
 		System.out.println("Part A:");
 		engine.verifyZipf(5);
 		
 		System.out.println();
 		System.out.println("Part B:");
 		Object invertedIndex = engine.invertedIndexFrequencies();
 		
 		System.out.println();
 		System.out.println("Part C:");
 		engine.tf_idf(invertedIndex);
 
 		System.out.println();
 		System.out.println("Part D:");
 		engine.examineDefaultSimilarity();
 
 		System.out.println();
 		System.out.println("Part E:");
 		engine.compareTfidfs();
 
 		System.out.println();
 		System.out.println("Part F:");
 		engine.bm25();
 	}
 
 }
