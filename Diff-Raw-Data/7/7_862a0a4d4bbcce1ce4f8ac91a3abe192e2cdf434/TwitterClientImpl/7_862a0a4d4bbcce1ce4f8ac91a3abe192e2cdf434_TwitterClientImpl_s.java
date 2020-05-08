 package net.ex337.scriptus.transport.twitter;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 
 import net.ex337.scriptus.config.ScriptusConfig;
 import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
 import twitter4j.Status;
 import twitter4j.StatusUpdate;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.conf.ConfigurationBuilder;
 
 public class TwitterClientImpl implements TwitterClient {
 
     private Twitter twitter;
     
     @Resource
     private ScriptusConfig config;
 
     @PostConstruct
     public void init() {
 		ConfigurationBuilder cb = new ConfigurationBuilder();
 		cb.setDebugEnabled(true)
 		.setOAuthConsumerKey(config.getTwitterConsumerKey())
 		.setOAuthConsumerSecret(config.getTwitterConsumerSecret())
 		.setOAuthAccessToken(config.getTwitterAccessToken())
 		.setOAuthAccessTokenSecret(config.getTwitterAccessTokenSecret())
 		.setIncludeEntitiesEnabled(true);
 
 		twitter = new TwitterFactory(cb.build()).getInstance();
     }
     
 	@Override
 	public List<Tweet> getMentions() {
 		
 		/*
 		 * to avoid duplicates (we have to search and get mentions,
 		 * because mentions by themselves are broken  / sometimes
 		 * don't update on a timely basis.
 		 */
 		
 		Set<Tweet> result = new TreeSet<Tweet>();
 		//FIXME or 200 mentions = DOS
 		try {
 			List<Status> mentions = twitter.getMentions();
 			
 			for(Status s : mentions) {
 			    Tweet t = new Tweet(s.getId(), s.getText(), s.getUser().getScreenName());
 			    t.setInReplyToId(s.getInReplyToStatusId());
 				result.add(t);
 			}
 			//mentions should be fixed now?
 //            Query q = new Query("@"+screenName);
 //            
 //            QueryResult r = twitter.search(q);
 //            
 //			for(twitter4j.Tweet t : r.getTweets()) {
 //				//compute hashcodes like in mockimpl
 //			    Tweet tt = new Tweet(t.getId(), t.getText(), t.getFromUser());
 //			    if(t.get){
 //			        tt.setInReplyToId(t.getInReplyToStatusId()))
 //			    }
 //			    t.
 //				result.add(tt);
 //			}
 			
 		} catch (TwitterException e) {
 			throw new ScriptusRuntimeException(e);
 		}
 		
 		return new ArrayList<Tweet>(result);
 	}
 
 	@Override
 	public long tweet(String txt) {
 		
 		if(txt.length() > 140) {
 			throw new ScriptusRuntimeException("tweet > 140 characters: "+txt);
 		}
 		
 		Status s;
 		try {
 			s = twitter.updateStatus(new StatusUpdate(txt));
 		} catch (TwitterException e) {
 			throw new ScriptusRuntimeException(e);
 		}
 		return s.getId();
 	}
 
     @Override
     public String getScreenName() {
         try {
             return twitter.getScreenName();
         } catch (IllegalStateException e) {
             throw new ScriptusRuntimeException(e);
         } catch (TwitterException e) {
             throw new ScriptusRuntimeException(e);
         }
     }
 
 }
