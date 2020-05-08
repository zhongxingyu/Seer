 package com.ssm.llp.biz.manager.impl;
 
 import com.ssm.llp.biz.event.EmailEvent;
 import com.ssm.llp.biz.manager.UserRegistrationManager;
 import com.ssm.llp.core.dao.SsmActorDao;
 import com.ssm.llp.core.dao.SsmPrincipalRoleDao;
 import com.ssm.llp.core.dao.SsmUserDao;
 import com.ssm.llp.core.model.SsmOfficer;
import com.ssm.llp.core.model.SsmPrincipalType;
 import com.ssm.llp.core.model.SsmRoleType;
 import com.ssm.llp.core.model.SsmUser;
 import com.ssm.llp.core.model.impl.SsmOfficerImpl;
 import com.ssm.llp.core.model.impl.SsmUserImpl;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * @author rafizan.baharum
  * @since 9/15/13
  */
 @Service("userRegistrationManager")
 @Transactional
 public class UserRegistrationManagerImpl implements UserRegistrationManager {
 
     public static final String ADMIN = "root";
     @Autowired
     private SsmUserDao userDao;
 
     @Autowired
     private SsmPrincipalRoleDao roleDao;
 
     @Autowired
     private SsmActorDao actorDao;
 
     @Autowired
     private SessionFactory sessionFactory;
 
     @Autowired
     private ApplicationContext applicationContext;
 
     @Override
     public void register(String username, String password, String name, String nricNo,
                          String email, String fax, String phone,
                          String address1, String address2, String address3) {
 
         // user root to save
         SsmUser root = userDao.findByUsername(ADMIN);
 
         // add user
         SsmUser user = new SsmUserImpl();
         user.setEmail(email);
        user.setUsername(username);
         user.setPassword(password);
         user.setRealname(name);
        user.setLocked(true);//apsal kene true?
        user.setPrincipalType(SsmPrincipalType.USER);
         userDao.save(user, root);
         sessionFactory.getCurrentSession().flush();
         sessionFactory.getCurrentSession().refresh(user);
 
         // add roles
         roleDao.grant(user, SsmRoleType.ROLE_USER, root);
         sessionFactory.getCurrentSession().flush();
         sessionFactory.getCurrentSession().refresh(user);
 
         // add actor
         SsmOfficer officer = new SsmOfficerImpl();
         officer.setName(name);
         officer.setNricNo(nricNo);
         officer.setAddress1(address1);
         officer.setAddress2(address2);
         officer.setAddress3(address3);
         officer.setEmail(email);
         officer.setPhone(phone);
         officer.setFax(fax);
         actorDao.save(officer, root);
         sessionFactory.getCurrentSession().flush();
         sessionFactory.getCurrentSession().refresh(officer);
 
         // update user
         user.setActor(officer);
         userDao.save(user, root);
         sessionFactory.getCurrentSession().flush();
         sessionFactory.getCurrentSession().refresh(officer);
 
         // send email with links
         applicationContext.publishEvent(new EmailEvent(email, "ssm.llp.poc@gmail.com", "Portal Registration", "Thank you for your registration."));
     }
 
     @Override
     public boolean isExists(String username) {
         SsmUser user = userDao.findByUsername(username);
         return (user != null);
     }
 }
