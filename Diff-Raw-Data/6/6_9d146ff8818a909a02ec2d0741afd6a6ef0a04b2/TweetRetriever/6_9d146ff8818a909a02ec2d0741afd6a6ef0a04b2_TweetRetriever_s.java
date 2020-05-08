 package toctep.skynet.backend.bll;
 
 import toctep.skynet.backend.Skynet;
 import twitter4j.FilterQuery;
 import twitter4j.Status;
 import twitter4j.StatusDeletionNotice;
 import twitter4j.StatusListener;
 import twitter4j.TwitterStream;
 import twitter4j.TwitterStreamFactory;
 
 public class TweetRetriever implements Runnable {
 
 	private TweetParser tweetParser;
 	private TwitterStream twitterStream;
 	
 	private static final double[][] NETHERLANDS_COORDS = new double[][] { {3.39, 51.17}, {7.29, 53.51} };
 	//private final static double[][] GRONINGEN_PROVINCE_COORDS = { {6.19, 53.09}, {7.22, 53.51} };
 	//private final static double[][] GRONINGEN_CITY_COORDS = new double[][] { {6.45, 53.16}, {6.65, 53.26} };
 	//private final static double[][] GRONINGEN_ZERNIKE_COORDS = new double[][] { {6.52, 53.23}, {6.55, 53.25} };
 	
 	public TweetRetriever() {
 	    initialize();
 	}
 	
 	private void initialize() {
<<<<<<< HEAD
		// Disable twitter4j logging
		//System.setProperty ("twitter4j.loggerFactory", "twitter4j.internal.logging.NullLoggerFactory"); 
		
=======
>>>>>>> c5c2bfb99cb3b6f949f1b906e9b4156d62133e67
 		tweetParser = TweetParser.getInstance();
 		
 		StatusListener statusListener = new StatusListener() {
 	        public void onStatus(Status status) {
 	            tweetParser.parse(status);
 	        }
 	
 	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) { }
 	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) { }
 	        public void onScrubGeo(long userId, long upToStatusId) { }
 	
 	        public void onException(Exception e) {
 	            Skynet.LOG.error(e.getMessage(), e);
 	        }
 	    };
 	    
 	    twitterStream = new TwitterStreamFactory().getInstance();
 	    twitterStream.addListener(statusListener);
 	}
 
 	@Override
 	public void run() {
 	    twitterStream.filter(new FilterQuery(0, null, null, NETHERLANDS_COORDS));
 	}
 	
 }
