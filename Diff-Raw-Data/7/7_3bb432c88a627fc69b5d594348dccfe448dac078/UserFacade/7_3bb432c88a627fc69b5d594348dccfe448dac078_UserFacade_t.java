 package net.sparkmuse.user;
 
 import twitter4j.TwitterFactory;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.http.RequestToken;
 import twitter4j.http.AccessToken;
 
 import play.Logger;
 import com.google.common.base.Preconditions;
 import com.google.inject.name.Named;
 import com.google.inject.Inject;
 import net.sparkmuse.common.Constants;
 import net.sparkmuse.common.Cache;
 import net.sparkmuse.data.UserDao;
 import net.sparkmuse.data.util.AccessLevel;
 import net.sparkmuse.data.entity.UserVO;
 import net.sparkmuse.data.entity.Entity;
 import net.sparkmuse.data.entity.UserVote;
 
 import java.util.Set;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author neteller
  * @created: Jul 3, 2010
  */
 public class UserFacade {
 
   private static final String CONSUMER_KEY = "wZidO63kKxdvpcCgBtQGQ";
   private static final String CONSUMER_SECRET = "HH7POuDB0HPFKdbpabJhvI4QrBS1Zh7JeE10Fvy3Nc";
   @Inject @Named(Constants.TWITTER_CALLBACK_URI) private String CALLBACK_URI;
 
   private final UserDao userDao;
   private final Cache cache;
 
   @Inject
   public UserFacade(UserDao userDao, Cache cache) {
     this.userDao = userDao;
     this.cache = cache;
   }
 
   public String beginAuthentication() {
     TwitterFactory tf = new TwitterFactory();
     Twitter twitter = tf.getInstance();
 
     twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
 
     RequestToken rtoken = null;
     try {
       rtoken = twitter.getOAuthRequestToken(CALLBACK_URI);
     } catch (TwitterException e) {
       throw new RuntimeException(e);
     }
 
     play.cache.Cache.set(rtoken.getToken(), rtoken, "5min");
 
     return rtoken.getAuthorizationURL();
   }
 
   public UserVO registerAuthentication(String oauth_token, String oauth_verifier) throws InvalidOAuthRequestToken {
     Preconditions.checkNotNull(oauth_token);
     Preconditions.checkNotNull(oauth_verifier);
     Logger.info("Twitter authentication received: " + oauth_token + " : " + oauth_verifier);
 
     TwitterFactory tf = new TwitterFactory();
     Twitter twitter = tf.getInstance();
 
     RequestToken requestToken = play.cache.Cache.get(oauth_token, RequestToken.class);
     if (null == requestToken) throw new InvalidOAuthRequestToken();
 
     twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
 
     try {
       AccessToken ac = twitter.getOAuthAccessToken(requestToken, oauth_verifier);
       
 
       UserVO user = userDao.findOrCreateUserBy(Integer.toString(ac.getUserId()), ac.getScreenName());
       cache.put(user);
 
       return user;
     } catch (TwitterException e) {
       throw new RuntimeException(e);
     }
   }
 
   //@todo create token mechanism
   public boolean verifyAuthorizationToken(final UserVO user, final String authToken) {
     boolean validToken = "SM123".equals(authToken);
     if (validToken) {
       user.setAccessLevel(AccessLevel.USER);
       userDao.update(user);
       cache.put(user);
     }
 
     return validToken;
   }
 
   /**
    * Finds a user in the cache.  If not present, the db is queried and the cache is updated.
    *
    * @param id
    * @return
    */
   public UserVO findUserBy(final Long id) {
     return userDao.findUserBy(id);
   }
 
   public void applyForInvitation(final String userName, final String url) {
     userDao.saveApplication(userName, url);
   }
 
   public void recordUpVote(final Votable votable, final Long userId) {
     userDao.vote(votable, findUserBy(userId));
   }
 
   public void recordUpVote(String className, Long id, UserVO voter) {
     try {
       final Class clazz = Class.forName(className);
       if (Entity.class.isAssignableFrom(clazz)) {
         userDao.vote((Class<Entity>) clazz, id, voter);
       }
     } catch (ClassNotFoundException e) {
       return;
     }
   }
 
   public UserVotes findUserVotesFor(Set<Votable> votables, UserVO user) {
     final Set<UserVote> userVotes = userDao.findVotesFor(votables, user);
     return new UserVotes(userVotes);
   }
 }
