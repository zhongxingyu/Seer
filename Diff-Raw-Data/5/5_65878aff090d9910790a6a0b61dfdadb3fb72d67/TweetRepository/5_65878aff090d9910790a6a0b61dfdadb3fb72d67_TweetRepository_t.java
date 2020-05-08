 package com.springapp.mvc.data;
 
 import com.google.code.ssm.api.ParameterValueKeyProvider;
 import com.google.code.ssm.api.ReadThroughSingleCache;
 import com.springapp.mvc.model.Tweet;
 import org.apache.log4j.Logger;
 import org.aspectj.lang.annotation.Aspect;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 import org.springframework.web.context.request.async.DeferredResult;
 import org.springframework.web.util.HtmlUtils;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 @Aspect
 @Repository
 public class TweetRepository {
     static Logger log = Logger.getLogger(TweetRepository.class);
     private final JdbcTemplate jdbcTemplate;
 
     @Autowired
     public TweetRepository(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
     public List<Tweet> fetchTweets(String username){
         return jdbcTemplate.query("((select id,username,tweet,timestamp,null as originalId,location, latitude, longitude from tweets where username=?) " +
                 "UNION " +
                 "(select id,retweets.username as username,tweets.tweet,retweets.timestamp,tweets.username as originalId,location, latitude, longitude from tweets inner join retweets on retweets.retweetId=tweets.id where retweets.username=?)) " +
                 "order by timestamp desc",
                 new Object[]{username,username}, new BeanPropertyRowMapper<>(Tweet.class));
 
     }
 
     public Tweet fetchTweet(Long tweetId) {
         return jdbcTemplate.queryForObject("select id,username,tweet,timestamp,location, latitude, longitude from tweets where id=?",
                 new Object[]{tweetId}, new BeanPropertyRowMapper<>(Tweet.class));
     }
 
     public List<Tweet> fetchUserTimeline(String username,long offset) {
         return jdbcTemplate.query("((select id,username,tweet,timestamp,null as originalId, location, latitude, longitude  from tweets where username=?) " +
                 "UNION " +
                 "(select id,retweets.username as username,tweets.tweet,retweets.timestamp ,tweets.username as originalId,location, latitude, longitude  from tweets inner join retweets on retweets.retweetId=tweets.id where retweets.username=?)) " +
                 "ORDER by timestamp DESC LIMIT 10 OFFSET ?",
                 new Object[]{username,username,offset*10}, new BeanPropertyRowMapper<>(Tweet.class));
     }
 
     public List<Tweet> fetchHomeTimeline(String username,long offset) {
         return jdbcTemplate.query("((select DISTINCT id,username,tweet,tweet_timestamp as timestamp,originalId,location, latitude, longitude from " +
                 "(select username,follower,tweet,id,test.timestamp as tweet_timestamp,location, latitude, longitude, following.timestamp as following_timestamp,test.originalId as originalId from " +
                 "((((select id,username,tweet,timestamp,null as originalId,location, latitude, longitude from tweets) UNION (select id,retweets.username as username,tweets.tweet,retweets.timestamp,tweets.username as originalId,location, latitude, longitude from tweets inner join retweets on retweets.retweetId=tweets.id)) as test inner join following on test.username = following.following))) as mergedTable where " +
                 "((mergedTable.following_timestamp is not NULL and mergedTable.tweet_timestamp<mergedTable.following_timestamp) or mergedTable.following_timestamp is NULL) and " +
                 "mergedTable.follower=?) UNION ((select id,username,tweet,timestamp,null as originalId, location, latitude, longitude  from tweets where username=?) " +
                 "UNION " +
                 "(select id,retweets.username as username,tweets.tweet,retweets.timestamp ,tweets.username as originalId,location, latitude, longitude  from tweets inner join retweets on retweets.retweetId=tweets.id where retweets.username=?) " +
                 ")) ORDER by timestamp DESC LIMIT 10 OFFSET ?",
                 new Object[]{username,username,username,offset*10}, new BeanPropertyRowMapper<>(Tweet.class));
     }
 
     public long addTweet(String username, String status, String location, Double latitude, Double longitude) {
         Timestamp timestamp = new Timestamp(new Date().getTime());
         log.info("Inserting new tweet " + status + " by user " + username);
         long id;
         if(!location.equals("-1"))
            id = jdbcTemplate.queryForInt("INSERT into tweets (username, tweet, timestamp, location, latitude, longitude ) VALUES (?,?,?,?,?,?) RETURNING id", new Object[]{username, status, timestamp, location, latitude, longitude});
         else
            id = jdbcTemplate.queryForInt("INSERT into tweets (username, tweet, timestamp) VALUES (?,?,?) RETURNING id", new Object[]{username, status, timestamp});
         detectAndInsertHashTags(id, status);
         return id;
     }
 
     public List<Tweet> searchTweets(String searchQuery) {
         log.info("SELECT * from tweets where to_tsvector(\'english\',tweet) @@ to_tsquery(\'english\','\""+ searchQuery +"\"')");
         return jdbcTemplate.query("SELECT * from tweets where to_tsvector(\'english\',tweet) @@ plainto_tsquery(\'english\','\""+ searchQuery +"\"')",
                 new Object[]{}, new BeanPropertyRowMapper<>(Tweet.class));
     }
 
     public List<Tweet> fetchRandomTweets() {
         return jdbcTemplate.query("SELECT * FROM tweets ORDER BY RANDOM() LIMIT 20",
                 new Object[]{} , new BeanPropertyRowMapper<>(Tweet.class));
     }
 
     public void detectAndInsertHashTags(long id,String status) {
         Pattern p = Pattern.compile("#[a-zA-Z][a-zA-Z0-9_]+");
         Matcher m = p.matcher(status);
         while(m.find()){
             String hashtag = m.group().substring(1);
             jdbcTemplate.update("insert into hashtags values(?,?)", new Object[]{id,hashtag});
         }
     }
 
     public List<Tweet> searchHashTags(String searchTag,long offset) {
         return jdbcTemplate.query("SELECT * from tweets where id in (select hashtags.id from hashtags where hashtag=?) ORDER by timestamp DESC LIMIT 10 OFFSET ?",
                 new Object[]{searchTag,offset*10}, new BeanPropertyRowMapper<>(Tweet.class));
     }
 
     public void retweet(long id,String username) {
         try {
             Timestamp timestamp = new Timestamp(new Date().getTime());
             jdbcTemplate.update("insert into retweets values (?,?,?)", new Object[]{username,id,timestamp});
         }
         catch (Exception e) {
             log.error(e.toString());
         }
     }
 
     public long getLatestTweetCount(Timestamp timestamp,String username) {
         return jdbcTemplate.queryForInt("select count(*) from (((select DISTINCT id,username,tweet,tweet_timestamp as timestamp,originalId,location, latitude, longitude from " +
                 "(select username,follower,tweet,id,test.timestamp as tweet_timestamp,location, latitude, longitude, following.timestamp as following_timestamp,test.originalId as originalId from " +
                 "((((select id,username,tweet,timestamp,null as originalId,location, latitude, longitude from tweets) UNION (select id,retweets.username as username,tweets.tweet,retweets.timestamp,tweets.username as originalId,location, latitude, longitude from tweets inner join retweets on retweets.retweetId=tweets.id)) as test inner join following on test.username = following.following))) as mergedTable where " +
                 "((mergedTable.following_timestamp is not NULL and mergedTable.tweet_timestamp<mergedTable.following_timestamp) or mergedTable.following_timestamp is NULL) and " +
                 "mergedTable.follower=?) UNION ((select id,username,tweet,timestamp,null as originalId, location, latitude, longitude  from tweets where username=?) " +
                 "UNION " +
                 "(select id,retweets.username as username,tweets.tweet,retweets.timestamp ,tweets.username as originalId,location, latitude, longitude  from tweets inner join retweets on retweets.retweetId=tweets.id where retweets.username=?) " +
                 ")) ORDER by timestamp DESC) as finalResult where finalResult.timestamp>?",
                 new Object[]{username,username,username,timestamp});
     }
 
     public List<Tweet> getLatestTweets(Timestamp timestamp, String username) {
         return jdbcTemplate.query("select * from (((select DISTINCT id,username,tweet,tweet_timestamp as timestamp,originalId,location, latitude, longitude from " +
                 "(select username,follower,tweet,id,test.timestamp as tweet_timestamp,location, latitude, longitude, following.timestamp as following_timestamp,test.originalId as originalId from " +
                 "((((select id,username,tweet,timestamp,null as originalId,location, latitude, longitude from tweets) UNION (select id,retweets.username as username,tweets.tweet,retweets.timestamp,tweets.username as originalId,location, latitude, longitude from tweets inner join retweets on retweets.retweetId=tweets.id)) as test inner join following on test.username = following.following))) as mergedTable where " +
                 "((mergedTable.following_timestamp is not NULL and mergedTable.tweet_timestamp<mergedTable.following_timestamp) or mergedTable.following_timestamp is NULL) and " +
                 "mergedTable.follower=?) UNION ((select id,username,tweet,timestamp,null as originalId, location, latitude, longitude  from tweets where username=?) " +
                 "UNION " +
                 "(select id,retweets.username as username,tweets.tweet,retweets.timestamp ,tweets.username as originalId,location, latitude, longitude  from tweets inner join retweets on retweets.retweetId=tweets.id where retweets.username=?) " +
                 ")) ORDER by timestamp DESC) as finalResult where finalResult.timestamp>? order by finalResult.timestamp;",
                 new Object[]{username, username, username, timestamp}, new BeanPropertyRowMapper<>(Tweet.class));
     }
 
     public List<String> getCurrentTrends() {
         Timestamp timestamp = new Timestamp(new Date().getTime()-(10*24*60*60*1000));
         log.info("Fetching trending items after "+timestamp.toString());
         return jdbcTemplate.queryForList("select result.hashtag from (select count(*) as mycount,hashtag from hashtags where hashtags.id in (select tweets.id from tweets where tweets.timestamp>?) group by hashtag order by mycount desc) as result LIMIT 10",
                 new Object[]{timestamp}, String.class);
     }
 
     public List<String> getHashTags(String searchTag) {
         return jdbcTemplate.queryForList("SELECT distinct hashtag from hashtags where hashtag LIKE '%"+searchTag+"%' LIMIT 10",
                 new Object[]{}, String.class);
 
     }
 }
 
