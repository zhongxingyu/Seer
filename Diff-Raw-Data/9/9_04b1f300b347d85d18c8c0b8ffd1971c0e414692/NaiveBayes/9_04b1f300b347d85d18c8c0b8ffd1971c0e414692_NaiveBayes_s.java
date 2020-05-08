 package danger_zone;
 import java.util.HashMap;
 import java.io.*;
 
 /**
 *@author Ethan Eldridge <ejayeldridge @ gmail.com>
 *@version 0.2
 *@since 2012-10-28
 *
 * Naive Bayes classifier to classify a tweet as a danger or not. 
 */
 public class NaiveBayes{
 	/**
 	*Constant for the category danger
 	*/
 	public static final int CAT_DANGER = 1;
 	/**
 	*Constant for the category safe
 	*/
 	public static final int CAT_SAFE = 0;
 	/**
 	*Default for a tweet that's probability falls beneath the threshold.
 	*/
 	public static final int DEFAULT_CATEGORY = CAT_DANGER;
 	/**
 	*Lemmatizer instance to strip and parse tweets into a managable form.
 	*/
 	private Lemmatizer tweetStripper = new Lemmatizer();
 	/**
 	*Helper array to make the classifying function easier
 	*/
	public static  int [] categories = {CAT_SAFE,CAT_DANGER};
 
 	/**
 	*Holds The two categories of the data being classified, and number of times a given String appears
 	*/
 	private HashMap<Integer, HashMap<String,Integer>> category_count = new HashMap<Integer, HashMap<String,Integer>>();
 
 	/**
 	*Totals of all the counts during training.
 	*/
 	private HashMap<String, Integer> prior_totals = new HashMap<String, Integer>();
 
 	/**
 	*Total count of all things trained on
 	*/
 	private int total_training_size = 0;
 
 	/**
 	*Threshold value to allow false positives
 	*/
 	private float threshold = (float)0.000000005;
 
 	public NaiveBayes(){
 		HashMap<String,Integer> danger = new HashMap<String,Integer>();
 		HashMap<String,Integer> safe = new HashMap<String,Integer>();
 		category_count.put(CAT_DANGER, danger);
 		category_count.put(CAT_SAFE, safe);
 	}
 
 	public NaiveBayes(int [] categorys){
 		HashMap<String,Integer> catMap; 
 		this.categories = categorys;
 		for(int cat : categories){ 
 			catMap = new HashMap<String,Integer>();
 			category_count.put(cat,catMap);
 		}
 		
 
 	}
 
 	/**
 	*Trains the algorithm on the dataset, each category is specified by a constant declared in this class, CAT_SAFE, CAT_DANGER and such.
 	*@param category The category to place this tweet into, should be selected from the constants declared in this class.
 	*@param tweet The text string of the tweet to be classified
 	*/
 	public void train(int category, String tweet){
 
 
 		//Get the words out of the tweet
 		String [] parsedTweet = tweetStripper.parseTweet(tweet).split(" ");
 
 		//Associate these words with the category
 		for(String pt : parsedTweet){
 			if(!category_count.get(category).containsKey(pt)){
 				category_count.get(category).put(pt,1);
 			}
 			int numW = category_count.get(category).get(pt);
 			category_count.get(category).put(pt,numW+1);
 			if(prior_totals.containsKey(pt)){
 				prior_totals.put(pt, prior_totals.get(pt) + 1);
 			}else{
 				prior_totals.put(pt,1);
 			}
 			
 			//Increment the total size of the training set.
 			total_training_size++;
 		}
 		//count of pt in cat divided by total count of pt in all categories = probability
 	}
 
 	/**
 	*Converts true into CAT_DANGER and false into CAT_SAFE, this is the only function that would need to change if we changed the values of those constants.
 	*/
 	static public int convertBoolToInt(boolean cat){
 		if(cat){
 			return CAT_DANGER;
 		}else{
 			return CAT_SAFE;
 		}
 	}
 
 	public void calculateProbabilities(){
 		//Remove all keys from each hash map and do some counting and then put in the scores for everything?
 	}
 
 	/**
 	*Classifies a given tweet into a category class defined as a constant in this class.
 	*@param tweet The text string of the sweet to be classified
 	*/
 	public int classify(String tweet){
 		//Each word is classified indenpendtly, whichever one has the most wins. 
 		String [] parsedTweet = tweetStripper.parseTweet(tweet).split(" ");
 
 		//Compute: Prob(C) * Prob(Ti|Ci)
 		//Prob(C) = fraction of documents in training set that are of category C
 		//Prob(Ti|C) = # occurences of Ti in C / total # words in C
 
 		//Find Prob C, the leading term of the expression
 		HashMap<Integer, Double> probC = new HashMap<Integer,Double>();
 		for(int cat : categories){
 			System.out.println("CAT" + cat);
 			
 			probC.put(cat, category_count.get(cat).size() / (double) total_training_size );
 		}
 
 		//Find the running product of the words. 
 		for(int cat : categories){
 			for(String pt : parsedTweet){
 				if(category_count.get(cat).containsKey(pt)){
 					double prob = probC.get(cat);
 					prob *= category_count.get(cat).get(pt)/(double)category_count.get(cat).size();
 					probC.put(cat,prob);
 				}	
 			}
 		}
 
 		//Compute the best 
 		int bestCat = DEFAULT_CATEGORY;
 		double bestFit = -1;
 		for(int cat : categories){
 			if(probC.get(cat) > bestFit){
 				bestFit = cat;
 			}
 		}
 		if(threshold > bestFit){
 			bestFit = DEFAULT_CATEGORY;
 		}
 
 		return bestCat;
 
 	}
 
 	public static void main(String[] args) {
 		NaiveBayes nb = new NaiveBayes();
 
 		nb.train(NaiveBayes.CAT_DANGER,"Syria is under attack");
 		nb.train(NaiveBayes.CAT_DANGER,"Bombs in Syria");
 		nb.train(NaiveBayes.CAT_SAFE,"Syria officials attend a ball");
 		nb.train(NaiveBayes.CAT_SAFE,"Peacetime in Syria");
 
 		switch(nb.classify("Attack in Syria")){
 			case NaiveBayes.CAT_DANGER:
 				System.out.println("danger");
 				break;
 			case NaiveBayes.CAT_SAFE:
 				System.out.println("safe");
 				break;
 		}
 
 	}
 
 }
