 /**
  * 
  */
 package org.jtalks.poulpe.service.transactional;
 
 import java.util.Collection;
 
 import org.joda.time.DateTime;
 import org.jtalks.common.model.dao.UserDao;
 import org.jtalks.common.model.entity.User;
import org.jtalks.common.service.transactional.AbstractTransactionalEntityService;
 import org.jtalks.poulpe.service.UserService;
 
 /**
  * User service class, contains methods needed to manipulate with {@code User}
  * persistent entity.
  * 
  * @author Guram Savinov
  */
 public class TransactionalUserService extends
        AbstractTransactionalEntityService<User, UserDao> implements
        UserService {
 
     /**
      * Create an instance of user entity based service.
      * 
      * @param dao
      *            the user DAO
      * @param securityService
      *            the security service
      */
    public TransactionalUserService(UserDao userDao) {
        this.dao = userDao;
     }
 
     @Override
     public void setPermanentBanStatus(Collection<User> users,
             boolean permanentBan, String banReason) {
         if (users == null) {
             throw new NullPointerException();
         }
 
         for (User user : users) {
             user.setPermanentBan(permanentBan);
             user.setBanExpirationDate(null);
             user.setBanReason(banReason);
 
             dao.saveOrUpdate(user);
         }
     }
 
     @Override
     public void setTemporaryBanStatus(Collection<User> users, int days,
             String banReason) {
         if (users == null) {
             throw new NullPointerException();
         }
         if (days <= 0) {
             throw new IllegalArgumentException();
         }
 
         DateTime banExpirationDate = DateTime.now().plusDays(days);
 
         for (User user : users) {
             user.setPermanentBan(false);
             user.setBanExpirationDate(banExpirationDate);
             user.setBanReason(banReason);
 
             dao.saveOrUpdate(user);
         }
     }
 
 }
