 package org.dentleisen.appening2;
 
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.log4j.Logger;
 
 import twitter4j.GeoLocation;
 import twitter4j.StatusUpdate;
 import twitter4j.Twitter;
 import twitter4j.TwitterFactory;
 import twitter4j.conf.ConfigurationBuilder;
 
 public class TwitterUpdateGenerator {
 
 	private static Logger log = Logger.getLogger(TwitterUpdateGenerator.class);
 
 	private static final long minMentions = Utils
 			.getCfgInt("appening.twitter.minMentions");
 	private static final long minMentionsDays = Utils
 			.getCfgInt("appening.twitter.minMentionDays");
 
 	private static final String urlPrefix = Utils
 			.getCfgStr("appening.twitter.urlPrefix");
 
 	private static final String accessToken = Utils
 			.getCfgStr("appening.twitter.accessToken");
 	private static final String accessSecret = Utils
 			.getCfgStr("appening.twitter.accessSecret");
 
 	private static final String consumerKey = Utils
 			.getCfgStr("appening.twitter.consumerKey");
 	private static final String consumerSecret = Utils
 			.getCfgStr("appening.twitter.consumerSecret");
 
 	private static final long interval = Utils
 			.getCfgInt("appening.twitter.intervalSeconds") * 1000;
 
 	private static final double minRank = Utils
 			.getCfgDbl("appening.twitter.minRank");
 
 	private static final double lastMentionedMins = Utils
 			.getCfgDbl("appening.twitter.lastMentionedMins");
 
 	public static void main(String[] args) {
 
 		ConfigurationBuilder cb = new ConfigurationBuilder();
 		cb.setDebugEnabled(false).setOAuthConsumerKey(consumerKey)
 				.setOAuthConsumerSecret(consumerSecret)
 				.setOAuthAccessToken(accessToken)
 				.setOAuthAccessTokenSecret(accessSecret);
 
 		TwitterFactory factory = new TwitterFactory(cb.build());
 		final Twitter twitter = factory.getInstance();
 
 		Timer t = new Timer();
 		TimerTask tt = new TimerTask() {
 			@Override
 			public void run() {
 				try {
 					log.info("Checking for places to push to feed...");
 					List<PopularPlace> places = Place.loadPopularPlaces(
 							minMentions, minMentionsDays);
 					for (PopularPlace p : places) {
 						if (p.rank < minRank) {
 							log.debug("Ignoring " + p.name + " (" + p.id
 									+ ") - rank " + p.rank + " < " + minRank);
 							continue;
 						}
 						if (p.wasMentioned(lastMentionedMins)) {
 							log.debug("Ignoring " + p.name + " (" + p.id
 									+ ") - already mentioned in last "
 									+ lastMentionedMins + " minutes");
 							continue;
 						}
 
						StatusUpdate su = new StatusUpdate(
								"'" + p.name + "' - "
										+ urlPrefix + "#-" + p.id + " (" + p.rank+")");
 						su.setLocation(new GeoLocation(p.lat, p.lng));
 						log.info(su.getStatus());
 						twitter.updateStatus(su);
 						p.setMentioned();
 					}
 				} catch (Exception e) {
 					log.warn("Unable to update twitter feed", e);
 				}
 			}
 		};
 		t.scheduleAtFixedRate(tt, 0, interval);
 
 	}
 }
