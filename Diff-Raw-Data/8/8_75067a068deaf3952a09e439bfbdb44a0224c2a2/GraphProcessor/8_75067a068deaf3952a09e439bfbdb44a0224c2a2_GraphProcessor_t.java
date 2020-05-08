 package controllers;
 
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 import com.google.gdata.util.common.base.Preconditions;
 import com.restfb.Connection;
 import com.restfb.DefaultFacebookClient;
 import com.restfb.FacebookClient;
 import com.restfb.exception.FacebookException;
 import models.User;
 import models.UserSocialGraph;
 import play.mvc.Controller;
 import utils.UserUtils;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * Graph Processor endpoints. To be called by the TaskQueue.
  *
  * @author Clement Pang (clement@glmx.com)
  */
 public class GraphProcessor extends Controller {
 
   private static final Logger logger = Logger.getLogger(GraphProcessor.class.getCanonicalName());
 
   /**
    * Updates the social graph of a user. To be called as a job in the TaskQueue.
    *
    * @param userId User id of user to process.
    */
   public static void updateSocialGraph(long userId, String secret) {
     Preconditions.checkArgument(secret.equals(Secret.APP_SECRET));
     User user = UserUtils.fetchUser(userId);
     if (user == null) return;
     UserSocialGraph socialGraph = loadSocialGraph(user);
     logger.info("Updating UserSocialGraph for user: " + user + ", created=" + socialGraph.getCreationTimeAsJodaDateTime().toString() +
         ", lastUpdated=" + socialGraph.getUpdateTimeAsJodaDateTime().toString());
     Set<Long> oldFriends = Collections.emptySet();
     if (socialGraph.friends != null) {
       oldFriends = Sets.newHashSet(socialGraph.friends);
     }
     socialGraph.friends = Lists.newArrayList();
     Set<Long> activeFriends = Sets.newHashSetWithExpectedSize(socialGraph.activeFriends.size());
     Set<Long> newlyCreatedUsers = Sets.newHashSet();
     try {
       FacebookClient fbClient = new DefaultFacebookClient(user.facebookAccessToken);
       String connectionUrl = null;
       boolean hasMore = true;
       while (hasMore) {
         Connection<com.restfb.types.User> friends;
         if (connectionUrl == null) {
           friends = fbClient.fetchConnection("me/friends", com.restfb.types.User.class);
         } else {
           friends = fbClient.fetchConnectionPage(connectionUrl, com.restfb.types.User.class);
         }
         for (com.restfb.types.User facebookFriend : friends.getData()) {
           User friend = UserUtils.getByFacebookUserId(facebookFriend.getId());
           if (friend != null) {
             if (friend.facebookAccessToken != null) {
               activeFriends.add(friend.id);
             }
           } else {
             friend = new User();
             friend.name = facebookFriend.getName();
             friend.facebookUserId = facebookFriend.getId();
             friend.save();
             newlyCreatedUsers.add(friend.id);
           }
           socialGraph.friends.add(friend.id);
         }
         if (friends.hasNext()) {
           connectionUrl = friends.getNextPageUrl();
         } else {
           hasMore = false;
         }
       }
       Set<Long> currentFriends = Sets.newHashSet(socialGraph.friends);
       Set<Long> defriended = Sets.difference(oldFriends, currentFriends);
       Set<Long> newFriends = Sets.difference(currentFriends, oldFriends);
       for (Long defriendedUserId : defriended) {
         // remove the link by scheduling a fetch.
         Queue queue = QueueFactory.getDefaultQueue();
         queue.add(TaskOptions.Builder.withUrl("/GraphProcessor/adjustLink").
             param("fromUserId", defriendedUserId.toString()).
             param("toUserId", user.id.toString()).
             param("secret", Secret.APP_SECRET).
             param("add", "false"));
         logger.info("Scheduling link removal from userId=" + defriendedUserId + " to userId=" + user.id);
       }
       for (Long newFriendUserId : newFriends) {
         if (newlyCreatedUsers.contains(newFriendUserId)) continue;
         Queue queue = QueueFactory.getDefaultQueue();
         queue.add(TaskOptions.Builder.withUrl("/GraphProcessor/adjustLink").
             param("fromUserId", newFriendUserId.toString()).
             param("toUserId", user.id.toString()).
             param("toUserId", user.id.toString()).
             param("secret", Secret.APP_SECRET).
             param("add", "true"));
         logger.info("Scheduling link add from userId=" + newFriendUserId + " to userId=" + user.id);
       }
       socialGraph.activeFriends = Lists.newArrayList(activeFriends);
       socialGraph.updateTime = new Date();
       socialGraph.save();
       logger.info("Updated social graph for user: " + userId);
     } catch (FacebookException ex) {
       logger.warning("Cannot update social graph for user: " + user.id + ", access token is probably invalid");
     }
   }
 
   public static void adjustLink(long fromUserId, long toUserId, String secret, boolean add) {
     Preconditions.checkArgument(secret.equals(Secret.APP_SECRET));
     User user = UserUtils.fetchUser(fromUserId);
     if (user == null) return;
     if (user.facebookAccessToken == null) {
       logger.info((add ? "Adding" : "Removing") + " userId=" + toUserId + " as a friend to: " + user +
           " is unnecessary since the user is not registered yet");
       return;
     }
     UserSocialGraph socialGraph = loadSocialGraph(user);
    if (socialGraph.id == null) {
      logger.info((add ? "Adding" : "Removing") + " userId=" + toUserId + " as a friend to: " + user +
          " cannot be done now as the user does not have a social graph yet");
      return;
    }
    logger.info((add ? "Adding" : "Removing") + " userId=" + toUserId + " as a friend to: " + user);
     socialGraph.getPersistenceManager().beginTransaction();
     try {
       Set<Long> friends = Sets.newLinkedHashSet(socialGraph.getFriends());
       Set<Long> activeFriends = Sets.newLinkedHashSet(socialGraph.getActiveFriends());
       boolean result = false;
       if (add) result |= friends.add(toUserId); else result |= friends.remove(toUserId);
       if (add) result |= activeFriends.add(toUserId); else result |= activeFriends.remove(toUserId);
       if (result) {
         socialGraph.friends = Lists.newArrayList(friends);
         socialGraph.activeFriends = Lists.newArrayList(activeFriends);
         socialGraph.save();
         socialGraph.getPersistenceManager().commitTransaction();
       } else {
         socialGraph.getPersistenceManager().rollbackTransaction();
       }
     } catch (RuntimeException ex) {
       socialGraph.getPersistenceManager().rollbackTransaction();
       throw ex;
     } finally {
       socialGraph.getPersistenceManager().closeConnection();
     }
   }
 
   private static UserSocialGraph loadSocialGraph(User user) {
     List<UserSocialGraph> results = UserSocialGraph.all(UserSocialGraph.class).filter("user", user).fetch();
     UserSocialGraph socialGraph;
     if (results.size() > 1) {
       logger.warning("Found " + results.size() + " UserSocialGraph objects for user:" + user + ", pruning");
       socialGraph = Ordering.natural().min(results);
       for (UserSocialGraph toDelete : results) {
         if (toDelete != socialGraph) {
           toDelete.delete();
         }
       }
     } else if (results.size() == 1) {
       socialGraph = results.get(0);
     } else {
       socialGraph = new UserSocialGraph();
       socialGraph.user = user;
     }
     return socialGraph;
   }
 }
