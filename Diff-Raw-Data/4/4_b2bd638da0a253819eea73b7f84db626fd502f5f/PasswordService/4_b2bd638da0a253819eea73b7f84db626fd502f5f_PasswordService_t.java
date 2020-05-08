 package org.otherobjects.cms.security;
 
 import java.security.MessageDigest;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.codec.binary.Base64;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.otherobjects.cms.dao.UserDao;
 import org.otherobjects.cms.model.PasswordChangeRequest;
 import org.otherobjects.cms.model.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.security.providers.dao.SaltSource;
 import org.springframework.security.providers.encoding.PasswordEncoder;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Assert;
 
 public class PasswordService extends HibernateDaoSupport
 {
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     public static final long MAXIMUM_TOKEN_AGE = 24 * 60 * 60 * 1000;
 
     @Resource
     private UserDao userDao;
 
     @Resource
     private PasswordEncoder passwordEncoder;
 
     @Resource
     private SaltSource saltSource;
 
     public String getPasswordChangeRequestCode(String username) throws Exception
     {
         User user = (User) userDao.loadUserByUsername(username);
         return getPasswordChangeRequestCode(user);
     }
 
     @Transactional
     public String getPasswordChangeRequestCode(User user) throws Exception
     {
         //cleanExpiredPasswordChangeRequests();
         PasswordChangeRequest pcr = new PasswordChangeRequest();
         pcr.setUser(user);
         pcr.setRequestDate(new Date());
         pcr.setToken(generateToken());
         getHibernateTemplate().save(pcr);
         return pcr.getChangeRequestCode();
     }
 
     /**
      * Generates a hopefully sufficiently random token by generating a random number merging it with the current date, 
      * then hashing and base64 encoding it for easy transport via e.g email
      * @return
      * @throws Exception 
      */
     String generateToken() throws Exception
     {
         Random random = new Random();
         Double rdbl = random.nextDouble();
         // create 10 digit int from that random double
         long rlong = (long) (rdbl * 10000000000L);
 
         String rawtoken = "" + rlong + new Date().getTime();
 
         MessageDigest md = MessageDigest.getInstance("SHA");
 
         byte[] digest = md.digest(rawtoken.getBytes("UTF-8"));
 
         return new String(Base64.encodeBase64(digest));
 
     }
 
    @SuppressWarnings("unchecked")
     @Transactional
     public boolean changePassword(PasswordChanger passwordChanger)
     {
         //cleanExpiredPasswordChangeRequests();
         boolean result = false;
         try
         {
             if (!passwordChanger.newPasswordValid())
                 return false;
 
             Object[] identifier = PasswordChangeRequest.splitPasswordChangeRequestIdentifier(passwordChanger.getChangeRequestCode());
 
             List pcrs = getHibernateTemplate().find("from PasswordChangeRequest where id=? and token=?", identifier);
 
             Assert.isTrue(pcrs.size() <= 1);
 
             PasswordChangeRequest pcr = null;
 
             if (pcrs.size() == 0)
                 return false; // no matching pcr
             else
                 pcr = (PasswordChangeRequest) pcrs.get(0);
 
             Date now = new Date();
 
             //if the token is too old we can't change pwd
             if ((now.getTime() - pcr.getRequestDate().getTime()) > MAXIMUM_TOKEN_AGE)
                 return false;
 
             User user = pcr.getUser();
 
             // encode and store new password
             user.setPassword(passwordEncoder.encodePassword(passwordChanger.getNewPassword(), saltSource.getSalt(user)));
             userDao.save(user);
 
             // if we've gotten here we can safely delete the pcr
             getHibernateTemplate().delete(pcr);
             result = true;
         }
         catch (Exception e)
         {
             logger.warn("Couldn't process password change fully", e);
         }
         return result;
     }
 
     //@Transactional
    public void cleanExpiredPasswordChangeRequests()
     {
         try
         {
             final Date maximumValidAge = new Date(new Date().getTime() - MAXIMUM_TOKEN_AGE);
 
             getHibernateTemplate().execute(new HibernateCallback()
             {
 
                 public Object doInHibernate(Session session) throws HibernateException, SQLException
                 {
                     Query q = session.createQuery("delete from PasswordChangeRequest where requestDate < :requestDate");
                     q.setDate("requestDate", maximumValidAge);
                     q.executeUpdate();
                     return null;
                 }
 
             });
         }
         catch (Exception e)
         {
             //noop
         }
     }
 
 }
