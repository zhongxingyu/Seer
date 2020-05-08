 package com.cqlybest.common.dao;
 
import org.springframework.stereotype.Repository;

 import com.belerweb.weixin.mp.WeixinUser;
 
@Repository
 public class WeixinUserDao extends AbstractDao<WeixinUser, String> {
 
   protected WeixinUserDao() {
     super(WeixinUser.class);
   }
 
 }
