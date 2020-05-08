 package com.mmtzj.service;
 
 import org.springframework.data.redis.core.RedisTemplate;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Resource;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: FANLU
  * Date: 13-1-23
  * Time: 下午9:19
  * To change this template use File | Settings | File Templates.
  */
 @Service
 public class QQService {
 
     @Resource
     private JdbcTemplate jdbcTemplate;
 
     @Resource
     private JedisService jedisService;
 
     @Resource
     private RedisTemplate redisTemplate;
 
     @Transactional
     public void updateItem(int itemId, String key) {
         String sql = String.format("update item set %s = %s +1 where id=?", key, key);
         jdbcTemplate.update(sql, new Object[]{itemId});
     }
 
     public void addCollect(String openid, int itemId) {
        redisTemplate.opsForList().remove(openid, 0, itemId);
        redisTemplate.opsForList().leftPush(openid, itemId);
     }
 
    public List getCollect(String openid){
        return redisTemplate.opsForList().range(openid, 0, 3);
     }
 
     public void delCollect(String openid, String itemId) {
        redisTemplate.opsForList().remove(openid, 0, itemId);
     }
 }
