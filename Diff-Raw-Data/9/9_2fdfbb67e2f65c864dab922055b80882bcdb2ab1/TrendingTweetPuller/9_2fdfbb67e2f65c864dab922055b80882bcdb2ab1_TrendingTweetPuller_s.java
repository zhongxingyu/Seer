 /************************************************************************
  * Program to add Trending Tweets to a SuperTweet Symbol Table every Hour
  * Written by Antonio Juliano and Brendan Chou
  ***********************************************************************
  */
 
 import java.util.*;
 import java.io.*;
 import twitter4j.*;
 import twitter4j.auth.AccessToken;
 import twitter4j.conf.ConfigurationBuilder;
 
 public class TrendingTweetPuller {
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) throws TwitterException {
 		// Files to Load From
 		final String tweetTableFile = "superTweets.data";
 		final String wordsFile = "words.tst";
 		final String tweetTableBackup = "superTweetsBackup.data";
 		
 		// Number of tweets from each subject to pull
 		final int tweetsToPull = 100;
 		
 		System.out.print("Authenticating... ");
 		// Authenticate
 		final String username = "turtleman755";
 		final String password = "accelerando";
 		final String consumerKey = "bceUVDFbpUh2pcu6gvpp9w";
 		final String consumerSecret = "pvei5cAMJgy5qXQPjuyyki508ZxHPM6ypRJt94OW9sY";
 		final String token = "17186983-6cc4E3GPsn1aFNrMsr5qJKpm8a0mxFl8ozsmUP43t";
 		final String tokenSecret = "p4Sc5WrSdSL2R4cUxqal86QRosbW5txbFQYF5ItOow";
 		ConfigurationBuilder cb = new ConfigurationBuilder();
         cb.setDebugEnabled(true);
         cb.setUser(username);
         cb.setPassword(password);
         cb.setOAuthConsumerKey(consumerKey);
         cb.setOAuthConsumerSecret(consumerSecret);
         cb.setOAuthAccessToken(token);
         cb.setOAuthAccessTokenSecret(tokenSecret);
         TwitterFactory tf = new TwitterFactory(cb.build());
         AccessToken accessToken = new AccessToken(token, tokenSecret);
         Twitter twitter = tf.getInstance(accessToken);
 		System.out.println("Done");
         
 		// Instance Variables
 		TweetTable tweetTable = null;
 		Date nextUpdate;
         ResponseList<Trends> trendsList;
         Query query;
         List<Tweet> tweets = null;
         List<User> usersToBeAdded = null;
     	int trendsListSize, hourlyTrendArraySize, resultsSize, numTweets, totalNumTweets;
     	TST<PolarityValue> wordPolarities = null;
     	TweetEvaluator tweetEvaluator;
     	Object obj = null;
     	Queue<TweetHolder> toBeAdded = new Queue<TweetHolder>();
     	SuperTweet superTweetToBeAdded;
     	String text;
     	String[] words;
     	final int lookupSize = 100;
    	String[] usersToLookup = new String[lookupSize];
     	TweetHolder[] tweetsToBeAdded = new TweetHolder[lookupSize];
     	long[] foundUsers;
     	ArrayList<User> finalUsers;
     	ArrayList<Tweet> finalTweets;
     	ArrayList<String> finalSubjects;
     	long[] userIDs = new long[lookupSize];
     	
     	// Load From File
     	System.out.print("Loading... ");
         try {
 			obj = ObjectLoader.load(tweetTableFile);
 		} catch (FileNotFoundException e3) {
 			System.err.println("FileNotFoundException: Failed to load TweetTable from " + tweetTableFile);
 			System.exit(0);
 		} catch (IOException e3) {
 			System.err.println("IOException: Failed to load TweetTable from " + tweetTableFile);
 			System.exit(0);
 		} catch (ClassNotFoundException e3) {
 			System.err.println("ClassNotFoundException: Failed to load TweetTable from " + tweetTableFile);
 			System.exit(0);
 		} 
         if (obj instanceof TweetTable)
         	tweetTable = (TweetTable) obj;
         else
         {
         	System.err.println("Class Mismatch: Failed to load TweetTable from " + tweetTableFile);
         	System.exit(0);
         }
         
         try {
 			obj = ObjectLoader.load(wordsFile);
 		} catch (FileNotFoundException e3) {
 			System.err.println("FileNotFoundException: Failed to load Word Polarizations from " + wordsFile);
 			System.exit(0);
 		} catch (IOException e3) {
 			System.err.println("IOException: Failed to load Word Polarizations from " + wordsFile);
 			System.exit(0);
 		} catch (ClassNotFoundException e3) {
 			System.err.println("ClassNotFoundException: Failed to load Word Polarizations from " + wordsFile);
 			System.exit(0);
 		} 
         if (obj instanceof TST<?>)
         	wordPolarities = (TST<PolarityValue>) obj;
         else
         {
         	System.err.println("Class Mismatch: Failed to load Word Polarizations from " + wordsFile);
         	System.exit(0);
         }
         System.out.println("Done.");
     	tweetEvaluator = new TweetEvaluator(wordPolarities);
     	
     	
     	
     	// Process Loop
     	for(;;)
     	{
     		numTweets = 0;
     		System.out.println(tweetTable.getSize() + " SuperTweets currently in TweetTable.");
     		
     		// Create Backup
     		System.out.print("Backing Up... ");
     		try {
 				ObjectLoader.backupFile(tweetTableFile, tweetTableBackup);
 			} catch (FileNotFoundException e3) {
 				System.err.println("FileNotFoundException: Unable to Create Backup of TweetTable");
 			} catch (NullPointerException e3) {
 				System.err.println("NullPointerException: Unable to Create Backup of TweetTable");
 			} catch (IOException e3) {
 				System.err.println("IOException: Unable to Create Backup of TweetTable");
 			}
     		System.out.println("Done.");
     		
     		
     		// Gather Tweets
     		try
     		{
 	    		trendsList = twitter.getDailyTrends();
 		        trendsListSize = trendsList.size();
 		        System.out.print("Gathering Tweets... ");
 		        for(int i = 0; i < trendsListSize; i++)
 		        {
 		        	Trends hourlyTrends = trendsList.get(i);
 					Trend[] hourlyTrendArray = hourlyTrends.getTrends();
 					hourlyTrendArraySize = hourlyTrendArray.length;
 					for(int j = 0; j < hourlyTrendArraySize; j++)
 					{
 						String trendName = hourlyTrendArray[j].getName();
 						query = new Query(trendName);
 				        query.setRpp(tweetsToPull);
 				        query.setLang("en");
 				        try
 				        {
 				        	tweets = twitter.search(query).getTweets();
 				        }
 				        catch (TwitterException e)
 				        {
 				        	continue;
 				        }
 						resultsSize = tweets.size();
 						for(int k = 0; k < resultsSize; k++)
 						{
 							numTweets++;
 							toBeAdded.enqueue(new TweetHolder(tweets.get(k), trendName));
 						}
 					}
 		        }
     		}
     		// TODO check infinite errors
     		catch (TwitterException e)
     		{
     			if (e.exceededRateLimitation())
         		{
         			System.out.println("Hourly Limit of " + e.getRateLimitStatus().getHourlyLimit() + " requests has been Reached.\n" +
         								"Process will Resume: " + e.getRateLimitStatus().getResetTime());
         			nextUpdate = e.getRateLimitStatus().getResetTime();
         			
         			// Create Backups
         			System.out.print("Backing Up... ");
             		try {
 						ObjectLoader.backupFile(tweetTableFile, tweetTableBackup);
 					} catch (FileNotFoundException e3) {
 						System.err.println("FileNotFoundException: Unable to Create Backup of TweetTable");
 					} catch (NullPointerException e3) {
 						System.err.println("NullPointerException: Unable to Create Backup of TweetTable");
 					} catch (IOException e3) {
 						System.err.println("IOException: Unable to Create Backup of TweetTable");
 					}
             		System.out.println("Done.");
             		
             		// Save
         			System.out.print("Saving... ");
         			try {
         				ObjectLoader.save(tweetTable, tweetTableFile);
         			} catch (FileNotFoundException e2) {
         				System.err.println("FileNotFoundException: Could not save TweetTable to " + tweetTableFile);
         			} catch (IOException e2) {
         				System.err.println("IOException: Could not save TweetTable to " + tweetTableFile);
         			}
         			System.out.println("Done.");
         			
         			
         			numTweets = 0;
         			
         			// Wait Until Rate Limit Resets
         			while(new Date().before(nextUpdate))
         	        {
         	        	try {
         					Thread.sleep(10000);
         				} catch (InterruptedException e2) {
         					System.err.println("Interrupted");
         				}
         	        }
         			continue;
         		}
         		else
         		{
         			if (e.isErrorMessageAvailable())
         				System.err.println(e.getErrorMessage());
         			else
         				System.err.println("TwitterException: Continuing");
         			continue;
         		}
     		}
 	        System.out.println("Done.\n" + numTweets + " Tweets Collected.\nAdding Tweets to TweetTable... ");
 	        
 	        
 	        //Add Tweets to TweetTable Based Around Rate Limit
 	        numTweets = 0;
 	        totalNumTweets = 0;
 	        while (!toBeAdded.isEmpty())
 	        {
 	        	try
 	        	{
 	        		// Ignore last tweets in Queue, possibly change later
 	        		if (toBeAdded.size() < lookupSize)
 	        			break;
 	        		
 	        		for (int i = 0; i < lookupSize; i++)
 	        		{
 	        			tweetsToBeAdded[i] = toBeAdded.dequeue();
 	        			userIDs[i] = tweetsToBeAdded[i].tweet.getFromUserId();
 	        		}
 	        		// TODO make sure IDs are actually the same between Search and REST APIs
 	        		usersToBeAdded = twitter.lookupUsers(userIDs);
 	        		
 	        		// Match up users and tweets
 	        		foundUsers = new long[usersToBeAdded.size()];
 	        		finalTweets = new ArrayList<Tweet>();
 	        		finalUsers = new ArrayList<User>();
 	        		finalSubjects = new ArrayList<String>();
 	        		int index = 0;
 	        		for (User u : usersToBeAdded)
 	        		{
 	        			foundUsers[index] = u.getId();
 	        			index++;
 	        		}
 	        		Arrays.sort(foundUsers);
 	        		for (int i = 0; i < lookupSize; i++)
 	        		{
 	        			index = Arrays.binarySearch(foundUsers, tweetsToBeAdded[i].tweet.getFromUserId());
 	        			if (index >= 0)
 	        			{
 	        				finalTweets.add(tweetsToBeAdded[i].tweet);
 	        				finalUsers.add(usersToBeAdded.get(index));
 	        				finalSubjects.add(tweetsToBeAdded[i].subject);
 	        			}
 	        		}
 	        		
 	        		Tweet currentTweet;
 	        		for (int i = 0; i < finalTweets.size(); i++)
 	        		{
 	        			currentTweet = finalTweets.get(i);
 	        			text = currentTweet.getText();
 		        		text = text.replaceAll("http:\\/\\/t.co\\/........", " ");
 		        		text = text.toLowerCase();
 		        		words = text.split("(\\W)+");
 	        			superTweetToBeAdded = new SuperTweet(currentTweet, finalUsers.get(i), words,
 	        					tweetEvaluator, twitter);
 	        			tweetTable.add(superTweetToBeAdded, finalSubjects.get(i));
 		        		for (int j = 0; j < words.length; j++)
 		        		{
 		        			if (words[j].length() == 0 || words[j] == null)
 		    					continue;
 		        			//TODO Check if this actually stores a reference
 		        			tweetTable.add(superTweetToBeAdded, words[j]);
 		        		}
 		        		numTweets++;
 		        		totalNumTweets++;
 	        		}
 	        		
 	        	}
 	        	catch (TwitterException e)
 	        	{
 	        		if (e.exceededRateLimitation())
 	        		{
 	        			System.out.println("Hourly Limit of " + e.getRateLimitStatus().getHourlyLimit() + " requests has been Reached.\n" +
 	        								numTweets + " Tweets have been added to the TweetTable in the Past Hour.\nProcess will Resume" +
 	        										": " + e.getRateLimitStatus().getResetTime());
 	        			nextUpdate = e.getRateLimitStatus().getResetTime();
 	        			
 	        			// Create Backups
 	        			System.out.print("Backing Up... ");
 	            		try {
 							ObjectLoader.backupFile(tweetTableFile, tweetTableBackup);
 						} catch (FileNotFoundException e3) {
 							System.err.println("FileNotFoundException: Unable to Create Backup of TweetTable");
 						} catch (NullPointerException e3) {
 							System.err.println("NullPointerException: Unable to Create Backup of TweetTable");
 						} catch (IOException e3) {
 							System.err.println("IOException: Unable to Create Backup of TweetTable");
 						}
 	            		System.out.println("Done.");
 	            		
 	            		// Save
 	        			System.out.print("Saving... ");
 	        			try {
 	        				ObjectLoader.save(tweetTable, tweetTableFile);
 	        			} catch (FileNotFoundException e1) {
 	        				System.err.println("FileNotFoundException: Could not save TweetTable to " + tweetTableFile);
 	        			} catch (IOException e1) {
 	        				System.err.println("IOException: Could not save TweetTable to " + tweetTableFile);
 	        			}
 	        			System.out.println("Done.");
 	        			
 	        			numTweets = 0;
 	        			
 	        			// Wait Until Rate Limit Resets
 	        			while(new Date().before(nextUpdate))
 	        	        {
 	        	        	try {
 	        					Thread.sleep(10000);
 	        				} catch (InterruptedException e2) {
 	        					System.err.println("Interrupted");
 	        				}
 	        	        }
 	        		}
 	        		else
 	        		{
 	        			if (e.isErrorMessageAvailable())
 	        				System.err.println(e.getErrorMessage());
 	        		}
 	        	}
 	        }
 	        
 	        
 	        System.out.print("Finished Adding Tweets to TweetTable.\n" + totalNumTweets + " Tweets Added.\nSaving... ");
 	        try {
 				ObjectLoader.save(tweetTable, tweetTableFile);
 			} catch (FileNotFoundException e1) {
 				System.err.println("FileNotFoundException: Could not save TweetTable to " + tweetTableFile);
 			} catch (IOException e1) {
 				System.err.println("IOException: Could not save TweetTable to " + tweetTableFile);
 			}
 	        System.out.println("Done.");
     	}
 	}
 	private static class TweetHolder
 	{
 		private String subject;
 		private Tweet tweet;
 		public TweetHolder(Tweet tweet, String subject)
 		{
 			this.subject = subject;
 			this.tweet = tweet;
 		}
 	}
 }
