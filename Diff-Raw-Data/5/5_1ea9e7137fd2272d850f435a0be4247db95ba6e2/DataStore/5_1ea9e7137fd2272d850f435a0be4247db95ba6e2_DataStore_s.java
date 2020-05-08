 package com.vaguehope.senkyou;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.bind.JAXBException;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisPool;
 import redis.clients.jedis.JedisPoolConfig;
 import redis.clients.jedis.Protocol;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 
 import com.vaguehope.senkyou.model.UserData;
 import com.vaguehope.senkyou.twitter.TwitterConfigHelper;
 
 public class DataStore {
 
 	private static final Logger LOG = Logger.getLogger(DataStore.class.getName());
 
 	private final AtomicReference<JedisPool> pool = new AtomicReference<JedisPool>();
 
 	public void start () {
 		try {
 			URI redisURI = new URI(System.getenv("REDISTOGO_URL")); // Heroko pattern.
 			JedisPool jp = new JedisPool(new JedisPoolConfig(),
 					redisURI.getHost(),
 					redisURI.getPort(),
 					Protocol.DEFAULT_TIMEOUT,
 					redisURI.getUserInfo().split(":", 2)[1]);
 			this.pool.set(jp);
 		}
 		catch (URISyntaxException e) {
 			throw new IllegalStateException();
 		}
 	}
 
 	public void report (StringBuilder r) {
 		JedisPool jedisPool = this.pool.get();
 		Jedis jedis = jedisPool.getResource();
 		try {
 			r.append(jedis.dbSize()).append(" Redis entries.");
 		}
 		finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	public void putUserData (String sessionId, Twitter t) throws TwitterException {
 		JedisPool jedisPool = this.pool.get();
 		Jedis jedis = jedisPool.getResource();
 		try {
			PrintWriter data = new PrintWriter(new ByteArrayOutputStream());
			new UserData(t.getOAuthAccessToken()).toXml(data);
 			jedis.set(sessionId, data.toString());
 		}
 		catch (JAXBException e) {
 			throw new IllegalStateException(e);
 		}
 		finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	public Twitter getUser (String sessionId) {
 		if (sessionId == null) return null;
 		JedisPool jedisPool = this.pool.get();
 		Jedis jedis = jedisPool.getResource();
 		try {
 			String data = jedis.get(sessionId);
 			if (data == null) return null;
 			try {
 				UserData user = UserData.fromXml(data);
 				Twitter twitter = TwitterConfigHelper.getTwitter();
 				twitter.setOAuthAccessToken(user.getAccessToken());
 				try {
 					if (twitter.verifyCredentials() != null) return twitter;
 				}
 				catch (TwitterException e) {
 					// Do not care.
 				}
 				jedis.del(sessionId);
 				return null;
 			}
 			catch (JAXBException e) {
 				LOG.log(Level.WARNING, "Failed to parse data: '" + data + "'.", e);
 				jedis.del(sessionId);
 				return null;
 			}
 		}
 		finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 }
