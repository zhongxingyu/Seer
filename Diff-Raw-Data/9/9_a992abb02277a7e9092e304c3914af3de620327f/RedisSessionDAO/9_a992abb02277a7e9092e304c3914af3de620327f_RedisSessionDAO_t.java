 package com.mmtzj.service;
 
 import org.apache.shiro.session.Session;
 import org.apache.shiro.session.UnknownSessionException;
 import org.apache.shiro.session.mgt.SimpleSession;
 import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.Resource;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Date;
 
 @Service("redisSessionDao")
 public class RedisSessionDao extends AbstractSessionDAO {
 
     private static final Logger log = LoggerFactory.getLogger(RedisSessionDao.class);
 
     @Resource
     private JedisService jedisService;
 
     private final static String REDIS_SESS_PREFIX = "mmtzj:shiro_sess:";
 
     private final static int REDIS_DATA_TIMEOUT = 24 * 3600 * 365 * 2; // redis的session数据保留半个月
 
     public RedisSessionDao() {
     }
 
     @Override
     public void delete(Session session) {
         if (session == null) {
             log.error("detete session error, session argument cannot be null.");
             return;
         }
         Serializable id = session.getId();
         if (id != null) {
             jedisService.del(buildRedisKey(id.toString()));
         }
     }
 
     /**
      * 这个方法不应该被调用
     *
      * @return
      */
     @Override
     public Collection<Session> getActiveSessions() {
         log.error("what? get all active sessions?");
         return null;
     }
 
     @Override
     public void update(Session session) throws UnknownSessionException {
         storeSession(session.getId(), session);
     }
 
     private String buildRedisKey(String sessId) {
         return REDIS_SESS_PREFIX + sessId;
     }
 
     @Override
     protected Serializable doCreate(Session session) {
         Serializable sessionId = generateSessionId(session);
         assignSessionId(session, sessionId); // 声称一个唯一id，可以是uuid，可以是随机long，但是必须是Serializable
         storeSession(sessionId, session); // 放在sessions里面
         return sessionId;
     }
 
     @Override
     protected Session doReadSession(Serializable sessionId) {
         if (sessionId == null) {
             throw new NullPointerException("id argument cannot be null.");
         }
         if (sessionId.equals("mmtzj:invalid:user-agent")) {
             SimpleSession sess = new SimpleSession();
             sess.setId(sessionId);
             return sess;
         }
 
         Object sessionObj = jedisService.get(buildRedisKey(sessionId.toString()));
         if (sessionObj == null) {
             return null;
         }
 
         if (sessionObj instanceof Session) {
             return (Session) sessionObj;
         } else {
             log.error("can not unserialize session from redis:" + sessionId);
             return null;
         }
 
     }
 
     protected Session storeSession(Serializable id, Session session) {
         if (id == null) {
             throw new NullPointerException("id argument cannot be null.");
         }
 
         if (session == null) {
             throw new NullPointerException("session argument cannot be null.");
         }
 
         SimpleSession simpleSession = (SimpleSession) session;
         simpleSession.setLastAccessTime(new Date());
 
         if (!id.equals("mmtzj:invalid:user-agent")) {
             jedisService.set(buildRedisKey(id.toString()), simpleSession, REDIS_DATA_TIMEOUT);
         }
 
         return simpleSession;
     }
 }
