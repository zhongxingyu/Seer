 package com.directi.train.tweetapp.services;
 
 import com.directi.train.tweetapp.model.FeedItem;
 import com.directi.train.tweetapp.model.TweetItem;
 import org.hsqldb.Row;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Service;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sitesh
  * Date: 24/7/12
  * Time: 2:33 PM
  * To change this template use File | Settings | File Templates.
  */
 
 @Service
 public class FeedStore {
     private final ThreadLocal<Long> userID;
     public SimpleJdbcTemplate db;
     private UserStore userStore;
 
     @Autowired
     public FeedStore(@Qualifier("userID") ThreadLocal<Long> userID, SimpleJdbcTemplate simpleJdbcTemplate, UserStore userStore) {
         this.userID = userID;
         this.db = simpleJdbcTemplate;
         this.userStore = userStore;
     }
 
     public FeedItem add(FeedItem feedItem,Long userID) {
         System.out.println("userID: " + userID);
         System.out.println("tweet: " + feedItem.getTweet());
 
         String userName = (String)db.query(String.format("select username from users where id = %d", userID),new RowMapper<Object>() {
             @Override
             public String mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getString("username");
             }
         }).get(0);
         System.out.println("userName: " + userName);
 
         Long nextUniqueTweetId = feedItem.getTweetId();
         if (nextUniqueTweetId == null)
             nextUniqueTweetId = 1 + db.queryForLong(String.format("SELECT MAX(tweet_id) from (SELECT tweet_id from feeds where creator_id = %d) tweetidtable",
                                                     userID));
         System.out.println("nextUniqueTweetId: " + nextUniqueTweetId);
 
         Long creatorId = feedItem.getCreatorId();
         if (creatorId == null)
             creatorId = userID;
         System.out.println("creatorId: " + creatorId);
 
         List<Long> followerIdList = userStore.followerList(userName);
         for (Long i : followerIdList) {
             System.out.println("followerId: " + i);
             db.update("insert into feeds (user_id, receiver_id, tweet, tweet_id, creator_id, timestamp) values(?, ?, ?, ?, ?, now())",
                     userID, i, feedItem.getTweet(), nextUniqueTweetId, creatorId);
         }
         db.update("insert into feeds (user_id, receiver_id, tweet, tweet_id, creator_id, timestamp) values(?, ?, ?, ?, ?, now())",
                 userID, userID, feedItem.getTweet(), nextUniqueTweetId, creatorId);
         int id = db.queryForInt(String.format("select id from feeds where user_id =%d order by id desc limit 1", userID));
         return db.queryForObject(String.format("select something.id, user_id, something.username, tweet_id, tweet, creator_id, users.username as creatorname " +
                 "from (select feeds.id, feeds.user_id , users.username, feeds.tweet_id, feeds.tweet, feeds.creator_id " +
                 "from feeds inner join users " +
                 "on users.id = feeds.user_id " +
                 "where feeds.id = %d order by feeds.id desc) something inner join users " +
                 "on something.creator_id = users.id " +
                 "order by something.id desc", id), FeedItem.rowMapper);
     }
 
     public List<FeedItem> feed() {
         List<FeedItem> feedItems = db.query(String.format("select something.id, user_id, something.username, tweet_id, tweet, something.creator_id, users.username as creatorname " +
                 "from (select feeds.id, feeds.user_id , users.username, feeds.tweet_id, feeds.tweet, feeds.creator_id " +
                 "from feeds inner join users " +
                 "on users.id = feeds.user_id " +
                 "where feeds.receiver_id = %d " +
                 "order by feeds.id desc) something inner join users " +
                 "on something.creator_id = users.id " +
                 "order by something.id desc", userID.get()), FeedItem.rowMapper);
         for (FeedItem feedItem : feedItems) {
                 feedItem.setFavorite(db.queryForInt(String.format("select count(*) from favorites where tweet_id = %d and user_id =  %d", feedItem.getTweetId(), feedItem.getUserId())) > 0);
         }
         return feedItems;
     }
 
     public boolean favoriteTweet(Long creatorId, Long tweetId, Long userId) {
         return db.update(String.format("insert into favorites (tweet_id, user_id) values (%d, %d)", tweetId, userId)) > 0;
     }
 
     public boolean unFavoriteTweet(Long creatorId, Long tweetId, Long userID) {
         return db.update(String.format("delete from favorites where tweet_id = %d and user_id = %d", tweetId, userID)) > 0;
     }
 
     public FeedItem reTweet(Long creatorId, Long tweetId, Long userId) {
        if (creatorId == userId) {
             System.out.println("User #" + userId + " can't retweet its own!");
             return null;
         }
 
         db.update("insert into retweets (tweet_id, user_id) values (?, ?)", tweetId, userId);
 
         FeedItem feedItem = new FeedItem();
         feedItem.setTweetId(tweetId);
         String tweet = db.queryForObject(String.format("select tweet from feeds where tweet_id = %d and user_id = creator_id and user_id = receiver_id", tweetId), new RowMapper<String>() {
             @Override
             public String mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getString("tweet");
             }
         });
         feedItem.setTweet(tweet);
         feedItem.setCreatorId(creatorId);
         return add(feedItem,userId);
     }
 
     public void unReTweet(Long creatorId, Long tweetId, Long userId) {
        if (creatorId == userId) {
             System.out.println("User #" + userId + " can't unretweet its own!");
             return;
         }
         db.update(String.format("delete from retweets where tweet_id = %d and user_id = %d", tweetId, userId));
     }
 
     public List<Long> favoritedUsers(Long tweetId) {
         return db.query(String.format("select user_id from favorites where tweet_id = %d", tweetId), new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("user_id");
             }
         });
     }
 
     public List<Long> reTweetedUsers(Long tweetId) {
         return db.query(String.format("select user_id from retweets where tweet_id = %d", tweetId), new RowMapper<Long>() {
             @Override
             public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                 return resultSet.getLong("user_id");
             }
         });
     }
 }
