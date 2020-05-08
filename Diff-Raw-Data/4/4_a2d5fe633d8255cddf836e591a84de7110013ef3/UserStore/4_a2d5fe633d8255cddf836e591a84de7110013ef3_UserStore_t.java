 package com.directi.train.tweetapp.services;
 
 import com.directi.train.tweetapp.model.FeedItem;
 import com.directi.train.tweetapp.model.UserProfileItem;
 import com.directi.train.tweetapp.services.Auxillary.ShardStore;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Service;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: elricl
  * Date: 20/7/12
  * Time: 12:44 AM
  * To change this template use File | Settings | File Templates.
  */
 
 @Service
 public class UserStore {
     @Autowired private ShardStore shardStore;
 
     public long getUserId(String userName) {
         return shardStore.getShardByUserName(userName).queryForInt("select id from users where username= ?", userName);
     }
 
     public UserProfileItem getUserProfileItem(String userName) {
         long userId = getUserId(userName);
         return shardStore.getShardByUserName(userName).queryForObject("select username, id, email from users where id = ?", UserProfileItem.rowMapper, userId);
     }
 
     public List<UserProfileItem> followingList(String userName) {
         long userId = getUserId(userName);
         List<Long> followingIds = shardStore.getShardByUserId(userId).query("select following_id from following where user_id = ?", new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("following_id");
             }
         }, userId);
         List<UserProfileItem> users = new ArrayList<UserProfileItem>();
         for (Long followingId : followingIds) {
             users.add((UserProfileItem) shardStore.getShardByUserId(followingId).queryForObject("select username, id, email from users where id = ?", UserProfileItem.rowMapper, followingId));
         }
         return applyFollowing(userId, users);
     }
 
     public List<UserProfileItem> followerList(String userName) {
         long userId = getUserId(userName);
         List<Long> followerIds = shardStore.getShardByUserId(userId).query("select follower_id from followers where user_id = ?", new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("follower_id");
             }
         }, userId);
         List<UserProfileItem> users = new ArrayList<UserProfileItem>();
         for (Long followerId : followerIds) {
             UserProfileItem u = shardStore.getShardByUserId(followerId).queryForObject("select username, id, email from users where id = ?", UserProfileItem.rowMapper, followerId);
             users.add(u);
         }
         return applyFollowing(userId, users);
     }
 
     public Integer checkFollowingStatus(String currentUser,String otherUser) {
        return shardStore.getShardByUserName(currentUser).queryForInt("select count(*) from following where user_id = ? and following_id = ?",
                getUserId(currentUser), getUserId(otherUser));
     }
 
     public List<UserProfileItem> applyFollowing(long userId, List<UserProfileItem> users) {
         for (UserProfileItem user : users) {
             user.setFollowing(shardStore.getShardByUserId(userId).queryForInt("select count(*) from following where user_id = ? and following_id = ?", userId, user.getId())> 0);
         }
         return users;
     }
 
     public int followUser(String userName, Long loggedUserId) {
         try {
             long otherUserId = getUserId(userName);
             if (loggedUserId.equals(otherUserId)) {
                 return 1;
             }
             if (shardStore.getShardByUserId(loggedUserId).queryForInt("select count (*) from following where user_id = ? and following_id = ?", loggedUserId, otherUserId) > 0) {
                 return 1;
             }
             shardStore.getShardByUserId(loggedUserId).update("insert into following (user_id, following_id) values (? ,?)", loggedUserId, otherUserId);
             shardStore.getShardByUserId(otherUserId).update("insert into followers (user_id, follower_id) values  (?, ?)", otherUserId, loggedUserId);
             return 0;
         } catch (IndexOutOfBoundsException E) {
             return 1;
         } catch (Exception E) {
             E.printStackTrace();
             return 1;
         }
     }
 
     public int unFollowUser(String userName, Long loggedUserId) {
         try {
             long otherUserId = getUserId(userName);
             if (loggedUserId.equals(otherUserId)) {
                 return 1;
             }
 
             shardStore.getShardByUserId(loggedUserId).update("delete from following where user_id = ? and following_id = ?", loggedUserId, otherUserId);
             shardStore.getShardByUserId(otherUserId).update("delete from followers where user_id = ? and follower_id = ?", otherUserId, loggedUserId);
             return 0;
         } catch (IndexOutOfBoundsException E) {
             return 1;
         } catch (Exception E) {
             E.printStackTrace();
             return 1;
         }
     }
 
     public List<FeedItem> tweetList(String userName, Long loggedUserId) {
         String conditionalSQL = "user_id = ? and user_id = receiver_id";
         String orderingSQL = "desc limit ?";
         String otherCondition = "id > ?";
         return feedQueryAndFavoriteStatus(getUserId(userName), loggedUserId, conditionalSQL, otherCondition, orderingSQL, getMinFeedId(), getFeedLimit());
     }
 
     public int noOfTweets(String userName) {
         return shardStore.getShardByUserName(userName).queryForInt("select count(*) from feeds where user_id = receiver_id and user_id = ?",getUserId(userName));
     }
 
     public Integer noOfFollowers(String userName) {
         return shardStore.getShardByUserName(userName).queryForInt("select count(*) from followers where user_id = ?", getUserId(userName));
     }
 
     public Integer noOfFollowing(String userName) {
         return shardStore.getShardByUserName(userName).queryForInt("select count(*) from following where user_id = ?", getUserId(userName));
     }
 
     public List<Long> getFavoriteTweetsOfAUser(String userName) {
         long userId = getUserId(userName);
         return shardStore.getShardByUserId(userId).query("select tweet_id from favorites where user_id = ?", new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("tweet_id");
             }
         }, userId);
     }
 
     public List<Long> getReTweetsOfAUser(String userName) {
         long userId = getUserId(userName);
         return shardStore.getShardByUserName(userName).query("select tweet_id from retweets where user_id = ?", new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("tweet_id");
             }
         }, userId);
     }
 
     public List<FeedItem> feedQueryAndFavoriteStatus(Long userId, Long loggedUserId, String conditionalSQL, String otherCondition, String orderingSQL, Long feedId, Long feedLimit) {
         List<FeedItem> feedItems = shardStore.getShardByUserId(userId).query(getPreSQL() + conditionalSQL + getPostSQL() + otherCondition + getPreOrderSQL() + orderingSQL,
                 FeedItem.rowMapper, userId, feedId, feedLimit);
 
         for (FeedItem feedItem : feedItems) {
             feedItem.setFavorite(isFavorited(feedItem.getCreatorId(), feedItem.getTweetId(), loggedUserId));
             feedItem.setFavoriteCount(favoriteCount(feedItem.getCreatorId(), feedItem.getTweetId()));
             feedItem.setRetweetCount(reTweetCount(feedItem.getCreatorId(), feedItem.getTweetId()));
         }
         return feedItems;
     }
 
     private Long reTweetCount(Long creatorId, Long tweetId) {
         return shardStore.getShardByUserId(creatorId).queryForLong("select count(*) from retweets where creator_id = ? and tweet_id = ?", creatorId, tweetId);
     }
 
     private Long favoriteCount(Long creatorId, Long tweetId) {
         return shardStore.getShardByUserId(creatorId).queryForLong("select count(*) from favorites where creator_id = ? and tweet_id = ?", creatorId, tweetId);
     }
 
     public boolean isFavorited(Long creatorId, Long tweetId, Long userId) {
         return shardStore.getShardByUserId(creatorId).queryForInt("select count(*) from favorites where tweet_id = ? and user_id = ? and creator_id = ?", tweetId, userId, creatorId) > 0;
     }
 
     public boolean isRetweeted(Long creatorId, Long tweetId, long userId, FeedStore feedStore) {
         return shardStore.getShardByUserId(creatorId).queryForInt("select count(*) from retweets where tweet_id = ? and user_id = ?  and creator_id = ?", tweetId, userId, creatorId) > 0;
     }
 
     public String getPreSQL() {
         return " select * from (select distinct on (tweet_id, creator_id) * from feeds where ";
     }
 
     public String getPostSQL() {
         return " )temp where ";
     }
 
     public String getPreOrderSQL() {
         return " order by id ";
     }
 
     public Long getMaxFeedLimit() {
         return 10000L;
     }
 
     public Long getFeedLimit() {
         return 20L;
     }
 
     public Long getMinFeedId() {
         return 0L;
     }
 
 }
