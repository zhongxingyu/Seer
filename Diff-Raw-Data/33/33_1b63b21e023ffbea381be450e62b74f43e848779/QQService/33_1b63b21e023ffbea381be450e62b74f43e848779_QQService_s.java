 package com.mmtzj.service;
 
import com.google.common.base.Joiner;
import com.mmtzj.domain.Category;
import com.mmtzj.domain.Eval;
import com.mmtzj.domain.Item;
import com.mmtzj.mapper.BaseMapper;
 import org.springframework.data.redis.core.RedisTemplate;
 import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Resource;
 import java.util.List;
import java.util.Map;
import java.util.Set;
 
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
        redisTemplate.opsForSet().add(openid, itemId);
     }
 
    public Set getCollect(String openid){
        return redisTemplate.opsForSet().members(openid);
     }
 
     public void delCollect(String openid, String itemId) {
        redisTemplate.opsForSet().remove(openid, itemId);
     }
 }
