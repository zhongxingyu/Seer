 package com.janrain.backplane2.server.dao.redis;
 
 import com.janrain.backplane2.server.BackplaneMessage;
 import com.janrain.backplane2.server.BackplaneServerException;
 import com.janrain.backplane2.server.Scope;
 import com.janrain.backplane2.server.Token;
 import com.janrain.backplane2.server.dao.TokenDAO;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.oauth2.TokenException;
 import com.janrain.redis.Redis;
 import org.apache.commons.lang.SerializationUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import redis.clients.jedis.Jedis;
 
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 /**
  * @author Tom Raney
  */
 public class RedisTokenDAO implements TokenDAO {
 
     public static byte[] getKey(String id) {
         return ("v2_token_" + id).getBytes();
     }
 
     @Override
     public Token get(String id) throws BackplaneServerException {
         byte[] bytes = Redis.getInstance().get(getKey(id));
         if (bytes != null) {
             return (Token) SerializationUtils.deserialize(bytes);
         } else {
             return null;
         }
     }
 
     @Override
     public List<Token> getAll() throws BackplaneServerException {
         List<Token> tokens = new ArrayList<Token>();
         List<byte[]> byteList = Redis.getInstance().lrange(getKey("list"), 0, -1);
         for (byte[] bytes : byteList) {
             if (bytes != null) {
                 tokens.add((Token) SerializationUtils.deserialize(bytes));
             }
         }
         return tokens;
     }
 
     @Override
     public void persist(Token token) throws BackplaneServerException {
         Jedis jedis = null;
         try {
             jedis = Redis.getInstance().getJedis();
             byte[] bytes = SerializationUtils.serialize(token);
             jedis.rpush(getKey("list"), bytes);
             jedis.set(getKey(token.getIdValue()), bytes);
             // set a TTL
             if (token.getExpirationDate() != null) {
                jedis.expireAt(getKey(token.getIdValue()), token.getExpirationDate().getTime() / 1000 +1);
             }
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
     }
 
     @Override
     public void delete(String tokenId) throws BackplaneServerException {
         Jedis jedis = null;
         try {
             jedis = Redis.getInstance().getJedis();
             byte[] bytes = jedis.get(getKey(tokenId));
             if (bytes != null) {
                 logger.info("removing token " + tokenId);
                 jedis.lrem(getKey("list"), 0, bytes);
                 jedis.del(getKey(tokenId));
             }
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
     }
 
     @Override
     public List<Token> retrieveTokensByGrant(String grantId) throws BackplaneServerException {
         List<Token> tokens = getAll();
         List<Token> filtered = new ArrayList<Token>();
         for (Token token: tokens) {
             if (token.getBackingGrants().contains(grantId)) {
                 logger.info("found");
                 filtered.add(token);
             }
         }
         return filtered;
     }
 
     @Override
     public void revokeTokenByGrant(String grantId) throws BackplaneServerException {
         List<Token> tokens = retrieveTokensByGrant(grantId);
         for (Token token : tokens) {
             delete(token.getIdValue());
             logger.info("revoked token " + token.getIdValue());
         }
         if (! tokens.isEmpty()) {
             logger.info("all tokens for grant " + grantId + " have been revoked");
         }
     }
 
     @Override
     public String getBusForChannel(String channel) throws BackplaneServerException, TokenException {
 
         if (StringUtils.isEmpty(channel)) return null;
 
         final Scope singleChannelScope = new Scope(BackplaneMessage.Field.CHANNEL, channel);
         try {
 /*            List<Token> tokens = superSimpleDB.retrieveWhere(bpConfig.getTableName(BP_ACCESS_TOKEN), Token.class,
                                 Token.TokenField.TYPE.getFieldName() + "='" + GrantType.ANONYMOUS + "' AND " +
                                 Token.TokenField.SCOPE.getFieldName() + " LIKE '%" + singleChannelScope.toString() + "%'", true);*/
             //todo: this method is ripe for optimization
             List<Token> tokens = getAll();
 
             List<Token> filtered = new ArrayList<Token>();
             for (Token token : tokens) {
                 if (!token.getType().isPrivileged() && !token.getType().isRefresh() && token.getScopeString().contains(channel)) {
                     filtered.add(token);
                 }
             }
 
             tokens = filtered;
 
             if (tokens == null || tokens.isEmpty()) {
                 logger.error("No anonymous tokens found to bind channel " + channel + " to a bus");
                 throw new TokenException("invalid channel: " + channel);
             }
 
             String bus = null;
             for (Token token : tokens) {
                 if (token.getType().isPrivileged() || token.getType().isRefresh()) continue;
                 Scope tokenScope = token.getScope();
                 if ( tokenScope.containsScope(singleChannelScope)) {
                     LinkedHashSet<String> buses = tokenScope.getScopeMap().get(BackplaneMessage.Field.BUS);
                     if (buses == null || buses.isEmpty()) continue;
                     if ( (bus != null && buses.size() == 1) || (buses.size() > 1) )  {
                         throw new TokenException("invalid channel, bound to more than one bus");
                     }
                     bus = buses.iterator().next();
                 }
             }
             if (bus == null) {
                 throw new TokenException("invalid channel: " + channel);
             } else {
                 return bus;
             }
 
         } catch (TokenException te) {
             throw te;
         } catch (Exception e) {
             throw new BackplaneServerException(e.getMessage());
         }
     }
 
     @Override
     public boolean isValidBinding(String channel, String bus) throws BackplaneServerException {
         try {
             return bus != null && channel != null && bus.equals(getBusForChannel(channel));
         } catch (TokenException e) {
             logger.error(e.getMessage(), e);
             return false;
         }
     }
 
     @Override
     public void deleteExpiredTokens() throws BackplaneServerException {
         // todo: add token cache?
         Jedis jedis = null;
 
         try {
             jedis = Redis.getInstance().getJedis();
             logger.info("Backplane token cleanup task started.");
 
             List<Token> tokens = getAll();
             if (tokens != null) {
                 for (Token token : tokens) {
                     if (Redis.getInstance().get(getKey(token.getIdValue())) == null) {
                         // remove from list
                         jedis.lrem(getKey("list"), 0, SerializationUtils.serialize(token));
                         logger.info("removed expired token " + token.getIdValue());
                     }
                 }
             }
         } catch (Exception e) {
             // catch-all, else cleanup thread stops
             logger.error("Backplane token cleanup task error: " + e.getMessage(), e);
         } finally {
             logger.info("Backplane token cleanup task finished.");
             Redis.getInstance().releaseToPool(jedis);
         }
     }
 
     @Override
     public void cacheRevokedCleanup() throws SimpleDBException {
         // no-op
     }
 
     // PRIVATE
 
     private static final Logger logger = Logger.getLogger(RedisTokenDAO.class);
 }
