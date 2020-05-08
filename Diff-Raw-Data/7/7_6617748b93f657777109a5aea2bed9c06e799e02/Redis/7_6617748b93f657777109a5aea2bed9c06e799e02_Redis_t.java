 /** 
  * @(#)Redis.java
  */
 package framework.cache;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisPoolConfig;
 import redis.clients.jedis.JedisShardInfo;
 import redis.clients.jedis.ShardedJedis;
 import redis.clients.jedis.ShardedJedisPool;
 import redis.clients.jedis.exceptions.JedisConnectionException;
 import framework.config.Config;
 
 /**
  * Redis ĳ ü (http://redis.io/)
  */
 public class Redis extends AbstractCache {
 	/**
 	 * ̱ ü
 	 */
 	private static Redis _uniqueInstance;
 
 	/** 
 	 * ŸӾƿ  (ms)
 	 */
 	private static final int _TIMEOUT = 500;
 
 	/**
 	 * ĳ Ŭ̾Ʈ Pool
 	 */
 	private ShardedJedisPool _pool;
 
 	/**
 	 * , ܺο ü νϽȭ    
 	 */
 	private Redis() {
 		List<JedisShardInfo> shards;
 		if (_getConfig().containsKey("redis.host")) {
 			shards = _getAddresses(_getConfig().getString("redis.host"));
 		} else if (_getConfig().containsKey("redis.1.host")) {
 			int count = 1;
 			StringBuilder buffer = new StringBuilder();
 			while (_getConfig().containsKey("redis." + count + ".host")) {
 				buffer.append(_getConfig().getString("redis." + count + ".host") + " ");
 				count++;
 			}
 			shards = _getAddresses(buffer.toString());
 		} else {
 			throw new RuntimeException("redis ȣƮ Ǿϴ.");
 		}
 		_pool = new ShardedJedisPool(new JedisPoolConfig(), shards);
 	}
 
 	/** 
 	 * ü νϽ ش.
 	 * 
 	 * @return Redis ü νϽ
 	 */
 	public synchronized static Redis getInstance() {
 		if (_uniqueInstance == null) {
 			_uniqueInstance = new Redis();
 		}
 		return _uniqueInstance;
 	}
 
 	@Override
 	public void set(String key, Object value, int seconds) {
 		set(_serialize(key), _serialize(value), seconds);
 	}
 
 	public void set(byte[] key, byte[] value, int seconds) {
 		ShardedJedis jedis = null;
 		try {
 			jedis = _pool.getResource();
 			jedis.set(key, value);
 			jedis.expire(key, seconds);
 		} catch (JedisConnectionException e) {
 			if (jedis != null) {
 				_pool.returnBrokenResource(jedis);
 			}
 		} finally {
 			if (jedis != null) {
 				_pool.returnResource(jedis);
 			}
 		}
 	}
 
 	@Override
 	public Object get(String key) {
 		return get(_serialize(key));
 	}
 
 	public Object get(byte[] key) {
 		ShardedJedis jedis = null;
 		Object value = null;
 		try {
 			jedis = _pool.getResource();
 			value = _deserialize(jedis.get(key));
 		} catch (JedisConnectionException e) {
 			if (jedis != null) {
 				_pool.returnBrokenResource(jedis);
 			}
 		} finally {
 			if (jedis != null) {
 				_pool.returnResource(jedis);
 			}
 		}
 		return value;
 	}
 
 	@Override
 	public Map<String, Object> get(String[] keys) {
 		Map<String, Object> resultMap = new HashMap<String, Object>();
 		for (String key : keys) {
 			resultMap.put(key, get(key));
 		}
 		return resultMap;
 	}
 
 	@Override
 	public long incr(String key, int by) {
 		return incr(_serialize(key), by);
 	}
 
 	public long incr(byte[] key, int by) {
 		ShardedJedis jedis = null;
 		Long value = null;
 		try {
 			jedis = _pool.getResource();
 			value = jedis.incrBy(key, by);
 		} catch (JedisConnectionException e) {
 			if (jedis != null) {
 				_pool.returnBrokenResource(jedis);
 			}
 		} finally {
 			if (jedis != null) {
 				_pool.returnResource(jedis);
 			}
 		}
		if (value == null) {
			value = Long.valueOf(-1);
		}
 		return value;
 	}
 
 	@Override
 	public long decr(String key, int by) {
 		return decr(_serialize(key), by);
 	}
 
 	public long decr(byte[] key, int by) {
 		ShardedJedis jedis = null;
 		Long value = null;
 		try {
 			jedis = _pool.getResource();
 			value = jedis.decrBy(key, by);
 		} catch (JedisConnectionException e) {
 			if (jedis != null) {
 				_pool.returnBrokenResource(jedis);
 			}
 		} finally {
 			if (jedis != null) {
 				_pool.returnResource(jedis);
 			}
 		}
		if (value == null) {
			value = Long.valueOf(-1);
		}
 		return value;
 	}
 
 	@Override
 	public void delete(String key) {
 		ShardedJedis jedis = null;
 		try {
 			jedis = _pool.getResource();
 			jedis.del(key);
 		} catch (JedisConnectionException e) {
 			if (jedis != null) {
 				_pool.returnBrokenResource(jedis);
 			}
 		} finally {
 			if (jedis != null) {
 				_pool.returnResource(jedis);
 			}
 		}
 	}
 
 	@Override
 	public void clear() {
 		ShardedJedis jedis = null;
 		try {
 			jedis = _pool.getResource();
 			for (Jedis j : jedis.getAllShards()) {
 				j.flushAll();
 			}
 		} catch (JedisConnectionException e) {
 			if (jedis != null) {
 				_pool.returnBrokenResource(jedis);
 			}
 		} finally {
 			if (jedis != null) {
 				_pool.returnResource(jedis);
 			}
 		}
 	}
 
 	////////////////////////////////////////////////////////////////////////////////////////Private ޼ҵ
 
 	/**
 	* (config.properties)  о Ŭ Ѵ.
 	* @return ü
 	*/
 	private Config _getConfig() {
 		return Config.getInstance();
 	}
 
 	/**
 	 * ڿ redis ȣƮ ּҸ ĽϿ Ѵ.
 	 * @param str ̽ е ּҹڿ
 	 * @return ּҰü
 	 */
 	private List<JedisShardInfo> _getAddresses(String str) {
 		if (str == null || "".equals(str.trim())) {
 			throw new IllegalArgumentException("redis ȣƮ Ǿϴ.");
 		}
 		ArrayList<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
 		for (String addr : str.split("(?:\\s|,)+")) {
 			if ("".equals(addr)) {
 				continue;
 			}
 			int sep = addr.lastIndexOf(':');
 			if (sep < 1) {
 				throw new IllegalArgumentException(" ߸Ǿϴ. =>ȣƮ:Ʈ");
 			}
 			shards.add(new JedisShardInfo(addr.substring(0, sep), Integer.valueOf(addr.substring(sep + 1)), _TIMEOUT));
 		}
 		assert !shards.isEmpty() : "redis ȣƮ Ǿϴ.";
 		return shards;
 	}
 
 	/**
 	 * ü Ʈ迭 ȭ Ѵ.
 	 * @param obj ȭ ü
 	 * @return Ʈ迭
 	 */
 	public byte[] _serialize(Object obj) {
 		ObjectOutputStream oos = null;
 		ByteArrayOutputStream baos = null;
 		try {
 			baos = new ByteArrayOutputStream();
 			oos = new ObjectOutputStream(baos);
 			oos.writeObject(obj);
 			byte[] bytes = baos.toByteArray();
 			return bytes;
 		} catch (Exception e) {
 		}
 		return null;
 	}
 
 	/**
 	 * Ʈ迭 ü ȭ Ѵ.
 	 * @param bytes Ʈ迭
 	 * @return ȭ ü
 	 */
 	public Object _deserialize(byte[] bytes) {
 		ByteArrayInputStream bais = null;
 		try {
 			bais = new ByteArrayInputStream(bytes);
 			ObjectInputStream ois = new ObjectInputStream(bais);
 			return ois.readObject();
 		} catch (Exception e) {
 		}
 		return null;
 	}
 }
