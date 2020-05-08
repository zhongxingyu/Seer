 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.TreeMap;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 
 
 public class WordMarkovModel extends AbstractModel {
 	
 	private String myString;
 	private String[] myWords;
     private Random myRandom;
     private TreeMap<WordNgram, ArrayList<WordNgram>> myMap; 
     public static final int DEFAULT_COUNT = 100; // default # random letters generated
 
     public WordMarkovModel() {
         myRandom = new Random(1234);
         myMap = new TreeMap<WordNgram, ArrayList<WordNgram>>();
     }
 	
     /**
      * Read in the text for the Markov model
      */
 	public void initialize(Scanner s) {
         double start = System.currentTimeMillis();
         int countChar = readChars(s);
         myWords = myString.split("\\s+");
         int countWord = myWords.length;
         double end = System.currentTimeMillis();
         double time = (end - start) / 1000.0;
         super.messageViews("#read: " + countWord + " words in: " + time + " secs");
     }
 
     protected int readChars(Scanner s) {
         myString = s.useDelimiter("\\Z").next();
         s.close();      
         return myString.length();
     }
     
     /**
      * Generate N words using an order-K markov process where
      * the parameter is a String containing K and N separated by
      * whitespace with K first. If N is missing it defaults to some
      * value.
      */
     public void process(Object o) {
         String temp = (String) o;
         String[] nums = temp.split("\\s+");
         int k = Integer.parseInt(nums[0]);
         int numWords = DEFAULT_COUNT;
         if (nums.length > 1) {
             numWords = Integer.parseInt(nums[1]);
         }
         smart(k, numWords);
     }
     
     /**
      * build the output string of length numWords using a Markovian map of WordNgrams
      * @param k = n in the WordNgrams
      * @param numWords is the number of words in the output 
      */
     public void smart(int k, int numWords) {
     	
     	if (myMap.size()==0){
     		myMap = buildMap(k);
     	}
     	else{
     		List<WordNgram> keys = new ArrayList<WordNgram>(myMap.keySet());
         	WordNgram first = keys.get(0);
        	if(first.numWords() !=k){
         		myMap = buildMap(k);
         	}
     	}
        System.out.println("Number of Keys: "+ myMap.size());
     	
     	// use a map to generate the markov string
     	int start = myRandom.nextInt(myWords.length - k + 1);
     	WordNgram str = new WordNgram(myWords, start, k);
     	StringBuilder build = new StringBuilder();
     	
     	double stime = System.currentTimeMillis();
     	for(int i=0; i<numWords; i++){
     		ArrayList<WordNgram> nextWords = myMap.get(str);    		
     		int pick = myRandom.nextInt(nextWords.size());
         	WordNgram next = nextWords.get(pick);
        		String nextString = next.getLast();
        		build.append(nextString);
        		build.append(" ");
        		str = next; 
     	}
     	double etime = System.currentTimeMillis();
         double time = (etime - stime) / 1000.0;
         this.messageViews("Time to generate: " + time);
     	this.notifyViews(build.toString());
     } 
     
     /**
      * Build the map for smart()
      * @param k is the length of the WordNgrams
      * @return a map of WordNgrams to a WordNgram array
      */
     public TreeMap<WordNgram, ArrayList<WordNgram>> buildMap(int k){
     	TreeMap<WordNgram, ArrayList<WordNgram>> map = new TreeMap<WordNgram, ArrayList<WordNgram>>();
     	String[] wrapAroundWords = new String[myWords.length+k];
     	for(int i = 0; i<myWords.length; i++){
     		wrapAroundWords[i] = myWords[i];
     	}
     	for(int j = 0; j < k; j++){
     		wrapAroundWords[myWords.length+j] = myWords[j];
     	}
     	
     	ArrayList<WordNgram> list = new ArrayList<WordNgram>();
     	
     	for (int i = 0; i < myWords.length; i++) {
     		WordNgram kWords = new WordNgram(wrapAroundWords, i, k);
     		if(map.containsKey(kWords)){ list = map.get(kWords); }
     		else{ list = new ArrayList<WordNgram>(); }
     		WordNgram next = new WordNgram(wrapAroundWords, i+1, k);
     		list.add(next);
     		map.put(kWords, list);
     	}
     		
     	return map; 
     }
 
 }
