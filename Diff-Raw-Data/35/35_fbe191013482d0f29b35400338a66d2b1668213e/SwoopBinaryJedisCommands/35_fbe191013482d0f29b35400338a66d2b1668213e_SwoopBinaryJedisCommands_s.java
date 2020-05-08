 package com.swoop.data.redis;
 
 import redis.clients.jedis.BinaryJedisCommands;
 import redis.clients.jedis.Pipeline;
 import java.util.List;
 
 public interface SwoopBinaryJedisCommands extends BinaryJedisCommands
 {
     /**
      * Get the values of all the specified keys. If one or more keys dont exist
      * or is not of type String, a 'nil' value is returned instead of the value
      * of the specified key, but the operation never fails.
      * <p>
      * Time complexity: O(1) for every key
      * 
      * @param keys
      * @return Multi bulk reply
      */
     List<byte[]> mget(final byte[]... keys);
 
     /**
      * Set the the respective keys to the respective values. MSET will replace
     * old values with new values, while {@link #msetnx(String...) MSETNX} will
      * not perform any operation at all even if just a single key already
      * exists.
      * <p>
      * Because of this semantic MSETNX can be used in order to set different
      * keys representing different fields of an unique logic object in a way
      * that ensures that either all the fields or none at all are set.
      * <p>
      * Both MSET and MSETNX are atomic operations. This means that for instance
      * if the keys A and B are modified, another client talking to Redis can
      * either see the changes to both A and B at once, or no modification at
      * all.
      * 
     * @see #msetnx(String...)
      * 
      * @param keysvalues
      * @return Status code reply Basically +OK as MSET can't fail
      */
     String mset(final byte[]... keysvalues);
     
     /**
      * Starts a pipeline, which is a very efficient way to send lots of command
      * and read all the responses when you finish sending them.
      */
     Pipeline pipelined();
 }
