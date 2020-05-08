 package com.bot.main;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Random;
 
 import twitter4j.Status;
 
 import com.bot.twitter.TwitterClient;
 import com.bot.utils.StringUtils;
 
 /**
  * @author tchap
  * This class covers the bot, and all the actions the bot can take. This includes responding to tweets
  * and tweeting at all of its followers. When it responds to tweets it uses the corpus of the author
  * and generates a tweets using markov chaining. It selects one word from the incoming tweet and begins
  * the chain with that word. When the bot is sending tweets to its followers it collects a list of 
  * followers and responds with a markov chain that begins with a random selection of a list of possible 
  * starting words. 
  *
  */
 public class Bot {
 	private TwitterClient twitClient;
 	private String botName;
 	private static Date searchTime = new Date();
 
 	public Bot(String botName) {
 		this.botName = botName;
 		this.twitClient = new TwitterClient(botName);
 	}
 	/**
 	 * Method to acquire mentions of bot and respond to the users who mentioned. It checks to see if
 	 * tweets came in since the last time it was checked to avoid responding to same tweet more than
 	 * once.
 	 * @throws IOException
 	 */
 	public void createAndPublish(int checkTime) throws IOException {
 		ArrayList<Response> comments = parseMentions();
 		comments = getMentionsInTime(comments, checkTime);
 		if(comments.size()>0){
 			Corpus botCorp = new Corpus(botName);
 			makeTweets(botCorp.getwordProbablityMap(), comments);
 			publishResponseTweet(comments);
 		}else{
 			MainBotResponder.logger.info("No valid comments in time period");
 			System.exit(0);
 		}
 
 	}
 	public void createAndPublishCont(int timePause) throws IOException, InterruptedException {
 		Corpus botCorp = new Corpus(botName);
 		Date lastDate = twitClient.getLastTweet();
 		while(true){
 			ArrayList<Response> comments = parseMentions(lastDate);
 			if(comments.size()>0){
 				makeTweets(botCorp.getwordProbablityMap(), comments);
 				publishResponseTweet(comments);
 				lastDate = comments.get(0).getDate();
 			}
 			Thread.sleep(timePause*1000*60);
 		}
 
 	}
 	private ArrayList<Response> parseMentions(Date lastDate) {
 		return twitClient.parseMentions(lastDate);
 	}
 	/**
 	 * Method to get a list of all the followers and publish a tweet at each of them
 	 * @throws IOException
 	 */
 	public void getFollowersAndPublish() throws IOException {
 		ArrayList<Response> comments = getFollowers();
 		Corpus botCorp = new Corpus(botName);
 		makeTweets(botCorp.getwordProbablityMap(), comments);
 		twitClient.publishFriendTweet(comments);
 	}
 
 	public ArrayList<Response> getFollowers() {
 		return twitClient.getFollowers(botName);
 	}
 
 	public ArrayList<Response> parseMentions() {
 		return twitClient.parseMentions();
 	}
 	/**
 	 * Method to set response tweets from a commenter
 	 * @param wordProbablityMap
 	 * @param comments
 	 */
 	public void makeTweets(HashMap<String, HashMap<String, Float>> wordProbablityMap, ArrayList<Response> comments) {
 		for(int i=0; i<comments.size(); i++){
 			String responseTweet = "";
 			String commentator = comments.get(i).getCommenter();
 			String searchWord = comments.get(i).getSearchWord();
 			while(!StringUtils.hasPunctuation(responseTweet)){
 				responseTweet = getTweet(wordProbablityMap, commentator, searchWord);
 			}
 			comments.get(i).setResponse(responseTweet);
 			MainBotResponder.logger.info(i+1 + ".   " + comments.get(i).getResponse());
 		}
 	}
 	/**
 	 * Method to create tweet with a markov chain and commenter. If the start word isn't in the corpus a
 	 * generic starting word is selected. If the generated tweet has punctuation and is greater than 70
 	 * characters it will be returned immediately otherwise it will keep generating words until it is about
 	 * to go over the 140 character limit. 
 	 * @param wordProbablityMap
 	 * @param commenter
 	 * @param srchWrd
 	 * @return generated tweet
 	 */
 	public String getTweet(HashMap<String, HashMap<String, Float>> wordProbablityMap, String commenter, String srchWrd){
 		StringBuilder sb = new StringBuilder("@" + commenter);
 		String nextWord = getStartSentence(wordProbablityMap, srchWrd);
 		while((sb.length() + nextWord.length()) < 140){
 			sb.append(" " + nextWord);
 			if(StringUtils.hasPunctuation(nextWord) && sb.length()>70){
 				return sb.toString();
 			}else if(!wordProbablityMap.containsKey(nextWord)) {
 				nextWord = getStartSentence(wordProbablityMap, nextWord);
 			}else {
 				nextWord = getNext(wordProbablityMap, nextWord);
 			}
 		}
 		return sb.toString();
 	}
 	/**
 	 * Method to get the word that starts a sentence. This is complex for markov chaining because some words in the corpus
 	 * will not have words following them if they are at the end of paragraphs or chapters. Instead of compensating for 
 	 * this at the point of ingestion it is done here by looking for different word.
 	 * @param wordProbablityMap
 	 * @param srchWrd
 	 * @return
 	 */
 	private String getStartSentence(HashMap<String, HashMap<String, Float>> wordProbablityMap, String srchWrd) {
 		if(wordProbablityMap.containsKey(srchWrd)){
 			return srchWrd;
 		} else if(wordProbablityMap.containsKey(srchWrd.toLowerCase())){
 			return srchWrd.toLowerCase();
 		} else if(wordProbablityMap.containsKey(StringUtils.stripPunctuation(srchWrd))){
 			return StringUtils.stripPunctuation(srchWrd);
 		} else {
 			return Response.genRandomStart(srchWrd);
 		}
 	}
 	/**
 	 * Method to get next word in the markov chain following firstWord. It generates a float that is between
 	 * 0-100 then finds the word with the closest number without going over. This is a property of how the
 	 * wordProbabilityMap was created. This allows for an O(n) search for the next word since they are not ordered
 	 * within the HashMap.
 	 * @param wordProbablityMap
 	 * @param firstWord
 	 * @return following word in the markov chain
 	 */
 	public String getNext(HashMap<String, HashMap<String, Float>> wordProbablityMap, String firstWord) {
 		String closestWord = null;
 		Random generator = new Random();
 		float randomIndex = generator.nextFloat()*100;
 		if (!wordProbablityMap.containsKey(firstWord)){
 			return Response.genRandomStart(firstWord);
 		}
 		float nearDist = Float.MAX_VALUE;
 		HashMap<String, Float> wordProb = wordProbablityMap.get(firstWord);
 		for(String keyWord : wordProb.keySet()){
 			float wordDist = wordProb.get(keyWord);
 			if(wordDist>randomIndex && (wordDist-randomIndex<nearDist)){
 				nearDist = wordDist-randomIndex;
 				closestWord = keyWord;
 			}
 		}
 		return closestWord;
 	}
 	/**
 	 * Method to eliminate comments that have already been responded to. It check if the comment was
 	 * created in the time since the bot has last responded to tweets.
 	 * @param comments
 	 * @param checkTime
 	 * @return all comments that haven't been responded to
 	 */
 	private ArrayList<Response> getMentionsInTime(ArrayList<Response> comments, int checkTime) {
 		ArrayList<Response> validComments = new ArrayList<Response>();
 		Date cutoffTime = getCutoffTime(checkTime);
 		for(Response comment : comments){
 			Date twtDate = comment.getDate();
 			if(twtDate.after(cutoffTime)){
 				validComments.add(comment);
 			}
 		}
 		return validComments;
 	}
 
 	public long publishResponseTweet(ArrayList<Response> comments) {
 		long tweetId = 0;
 		try{
 			for(Response comment : comments){
 				Status st = twitClient.publishResponse(comment.getResponse());
 				tweetId = st.getId();
 				MainBotResponder.logger.info("Tweet is within the last search");
 				MainBotResponder.logger.info("Tweet time: " + comment.getDate().toString());
 			}
 		}catch (Exception e){
 			MainBotResponder.logger.error(e);
 		}
 		return tweetId;
 	}
 
 	private Date getCutoffTime(int checkTime) {
 		Calendar cd = new GregorianCalendar();
 		cd.setTime(searchTime);
 		cd.add(Calendar.MINUTE,	-checkTime);
 		return new Date(cd.getTimeInMillis());
 	}
 
 	public TwitterClient getTwitClient() {
 		return twitClient;
 	}
 
 	public void setTwitClient(TwitterClient twitClient) {
 		this.twitClient = twitClient;
 	}
 
 }
