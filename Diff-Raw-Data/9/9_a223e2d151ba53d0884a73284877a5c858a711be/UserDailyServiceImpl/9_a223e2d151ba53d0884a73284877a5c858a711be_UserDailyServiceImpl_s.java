 package com.server.cx.service.cx.impl;
 
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 import com.google.common.collect.Lists;
 import com.server.cx.dao.cx.CXCoinConsumeRecordDao;
 import com.server.cx.dao.cx.ContactsDao;
 import com.server.cx.dao.cx.UserInfoDao;
 import com.server.cx.dao.cx.UserStatusMGraphicDao;
 import com.server.cx.entity.cx.Contacts;
 import com.server.cx.entity.cx.UserInfo;
 import com.server.cx.exception.SystemException;
 import com.server.cx.service.cx.UserDailyService;
 
 @Component
 @Transactional(readOnly = true)
 public class UserDailyServiceImpl implements UserDailyService {
     private final static Logger LOGGER = LoggerFactory.getLogger(UserDailyServiceImpl.class);
     @Autowired
     private UserStatusMGraphicDao userStatusMGraphicDao;
     
     @Autowired
     private CXCoinConsumeRecordDao cxCoinConsumeRecordDao;
     
     @Autowired
     private UserInfoDao userInfoDao;
     
     @Autowired
     private ContactsDao contactsDao;

     @Override
     public void doDailyTask() throws SystemException {
         clearUserStatusMGraphic();
         clearCXCoinConsumeRecord();
         updateContactSelfUserInfo();
     }
     
    @Transactional(readOnly = false)
     private void updateContactSelfUserInfo() {
         List<Contacts> foundContacts = Lists.newArrayList();
         List<Contacts> contacts = contactsDao.findBySelfUserInfo(null);
         for(Contacts c : contacts) {
             UserInfo userInfo = userInfoDao.findByPhoneNo(c.getPhoneNo());
             if(userInfo != null) {
                 c.setSelfUserInfo(userInfo);
                 foundContacts.add(c);
             }
         }
         contactsDao.batchUpdateContactsPhoneNo(foundContacts);
     }
 
    @Transactional(readOnly = false)
     private void clearCXCoinConsumeRecord() {
         LOGGER.info("Into clearCXCoinConsumeRecord");
         cxCoinConsumeRecordDao.deleteAll();
     }
     
    @Transactional(readOnly = false)
     private void clearUserStatusMGraphic() {
         LOGGER.info("Into clearUserStatusMGraphic");
         userStatusMGraphicDao.deleteAll();
     }
 
 }
