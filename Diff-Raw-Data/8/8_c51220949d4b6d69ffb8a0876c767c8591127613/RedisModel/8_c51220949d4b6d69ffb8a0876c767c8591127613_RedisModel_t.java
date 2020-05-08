 package com.wk.rivers;
 
 import java.net.URI;
 import java.util.Set;
 
 import redis.clients.jedis.Jedis;
 
 public class RedisModel {
 	private int mRedisDB;
 	private String mRedisURI;
 	private String mRiver;
 	private String mKeySpace;
 	private String mType;
 	private Jedis mJedis;
 	
 	public RedisModel(String river, String type, String redisURI) {
 		mRiver = river;
 		mType = type;
 		mRedisURI = redisURI;
 		mKeySpace = String.format("%s:%s", mRiver, mType);
 	}
 	
 	public Set<String> all() {
 		return getClient().zrevrange(mKeySpace, 0, -1);
 	}
 	
 	public boolean contains(String id) {		
 			return getClient().sismember(mKeySpace+":ids", id);	
 	}
 	
 	public void add(Double rank, String member, String id){
 		System.out.println("Adding: "+member);
 		if (getClient().sadd(mKeySpace+":ids", id) != 0) {
 			getClient().zadd(mKeySpace, rank, member);
 		}
 	}
 	
 	public Long len() {
 		return getClient().zcard(mKeySpace);		
 	}
 	
 	/**
 	 * Get a Redis client
 	 * @return
 	 */
 	private Jedis getClient() {
 		if (mJedis == null || !mJedis.isConnected()) {		
 			URI uri = URI.create(mRedisURI);
 			if (uri.getScheme() != null && uri.getScheme().equals("redis")) {
 				String host = uri.getHost();
 				System.out.println(host);
 				int port = uri.getPort();
 				System.out.println(port);
 				String userInfo = uri.getUserInfo();				
 				mRedisDB = Integer.parseInt(uri.getPath().split("/", 2)[1]);
 				System.out.println(mRedisDB);
				System.out.println("userInfo: "+userInfo);
 				mJedis = new Jedis(host, port);
 				if (userInfo != null){
					System.out.println("userinfo: "+userInfo);
 					String password = userInfo.split(":", 2)[1];
 					System.out.println(password);
 					mJedis.auth(password);
 				}
				mJedis.select(mRedisDB);
 			}
 		}
 		return mJedis;
 	}
 }
