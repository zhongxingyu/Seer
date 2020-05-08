 package logic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import twitter4j.Status;
 import database.sentimental.BoostWords;
 import database.sentimental.Emoticons;
 import database.sentimental.SentiWordNet;
 import database.sentimental.Weight;
 
 public class TweetAnalyzer {
 
 	Logger log = LoggerFactory.getLogger(TweetAnalyzer.class);
	private static final double UPPERCASE_BONUS = 1.1;
	private static final double EXCLAMATION_BONUS = 1.1;
 	private SentiWordNet sentiWordNet;
 	private Emoticons emoticons;
 	private BoostWords boostWords;
 	
 	public static int numberOfUnknownWords = 0;
 
 	public TweetAnalyzer(SentiWordNet sentiWordNet, Emoticons emoticons, BoostWords boostWords) {
 		this.sentiWordNet = sentiWordNet;
 		this.emoticons = emoticons;
 		this.boostWords = boostWords; // TODO: Not working yet, because the database isn't working yet.
 	}
 
 	public Tweet getAnalyzedTweet(Status rawTweet) {
 		String text = rawTweet.getText();
 
 		log.info("Analyzing the following tweet: {}",text);
 
 		Tweet result = analyzeTweet(text);
 		return result;
 	}
 
 	private Tweet analyzeTweet(String text) {
 		Tweet result = new Tweet(text);
 		List<Word> words = filterTweet(text);
 		
 		logCleanTweet(words);
 		
 		List<Word> noValueFound = new ArrayList<>();
 		for (Word word : words) {
 			setWeightOfWord(word,noValueFound);
 		}
 		
 		logNoValueWords(noValueFound);
 		
 		result.setWords(words);
 		return result;
 	}
 
 	private void logNoValueWords(List<Word> noValueFound) {
 		for (Word word : noValueFound) {
 			log.warn("Couldn't find any value for word: '" + word.toString()+"'");
 		}
 	}
 
 	private void logCleanTweet(List<Word> words) {
 		Tweet tweet = new Tweet("");
 		tweet.setWords(words);
 		log.info("filtered tweet: {}", tweet.toString());
 	}
 
 	private void setWeightOfWord(/*in*/ Word word, /*out*/ List<Word> noValueFound) {
 		Weight weight = new Weight(0.0,0.0);
 		if (!important(word)) {
 			/* don't do anything */;
 		} else if(sentiWordNet.containsWord(word)){
 			weight = sentiWordNet.getWeight(word);
 		} else if (emoticons.containsWord(word)) {
 			weight = emoticons.getWeight(word);
 		} else {
 			numberOfUnknownWords++;
 			noValueFound.add(word);
 		}
 		word.setPositiveWeight(Utils.getAverage(weight.positive));
 		word.setNegativeWeight(Utils.getAverage(weight.negative));
 		
 		postprocessBonuses(word);
 		
 		log.trace(word + " : ( +{}; -{})",word.getPositiveWeight(),word.getNegativeWeight());
 	}
 
 	private void postprocessBonuses(Word word) {
 		if(isUpperCase(word)) {
 			word.setPositiveWeight(word.getPositiveWeight()*UPPERCASE_BONUS);
 			word.setNegativeWeight(word.getNegativeWeight()*UPPERCASE_BONUS);
 		}
 		
 		if(haveExclamationMark(word)) {
 			word.setPositiveWeight(word.getPositiveWeight()*EXCLAMATION_BONUS);
 			word.setNegativeWeight(word.getNegativeWeight()*EXCLAMATION_BONUS);
 		}
 	}
 	
 	private boolean haveExclamationMark(Word word) {
 		return word.toString().endsWith("!");
 	}
 
 	private boolean important(Word word) {
 		if(word.toString().equals("I") || word.toString().equals("\"")) {
 			return false;
 		}
 		return true;
 	}
 
 	private boolean isUpperCase(Word word) {
 		return word.toString().matches("[A-Z]");
 	}
 
 	private List<Word> filterTweet(String tweet) {
 		List<Word> filteredTweet = new ArrayList<>();
 		String links = "(http://.*[.].*/.*[ ]|http://.*[.].*/.*$)";
 		String names = "@[A-Za-z0-9_]*";
 		String multipleSpaces = "[ ]+";
 		
  		tweet = tweet.replaceAll(names,"");
  		tweet = tweet.replaceAll("#","");
  		tweet = tweet.replaceAll(links,"");
  		tweet = tweet.replaceAll(multipleSpaces," ");
  		
  		String[] wordArray = tweet.split(" ");
  		for (int i = 0; i < wordArray.length; i++) {
  			filteredTweet.add(new Word(wordArray[i]));
 		}
  		return filteredTweet;
 	}
 	
 }
