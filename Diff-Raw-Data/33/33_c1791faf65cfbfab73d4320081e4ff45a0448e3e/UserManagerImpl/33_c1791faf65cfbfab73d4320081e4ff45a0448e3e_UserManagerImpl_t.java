 package war.webapp.service.impl;
 
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 
 import javax.jws.WebService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.orm.jpa.JpaSystemException;
 import org.springframework.security.providers.encoding.PasswordEncoder;
 import org.springframework.security.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Service;
 
 import war.webapp.dao.UserDao;
 import war.webapp.model.User;
 import war.webapp.service.UserExistsException;
 import war.webapp.service.UserManager;
 import war.webapp.service.UserService;
 
 /**
  * Implementation of UserManager interface.
  * 
  */
 @Service("userManager")
 @WebService(serviceName = "UserService", endpointInterface = "war.webapp.service.UserService")
 public class UserManagerImpl extends GenericManagerImpl<User, Long> implements UserManager, UserService {
     private PasswordEncoder passwordEncoder;
     private UserDao userDao;
    private Map<String, List<User>> usersByFloor = new HashMap<String, List<User>>();
 
     @Autowired
     public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
         this.passwordEncoder = passwordEncoder;
     }
 
     @Autowired
     public void setUserDao(UserDao userDao) {
         this.dao = userDao;
         this.userDao = userDao;
     }
 
     /**
      * {@inheritDoc}
      */
     public User getUser(String userId) {
         return userDao.get(new Long(userId));
     }
 
     /**
      * {@inheritDoc}
      */
     public List<User> getUsers() {
         return userDao.getAllDistinct();
     }
 
     /**
      * {@inheritDoc}
      */
     public User saveUser(User user) throws UserExistsException {
 
         if (user.getVersion() == null) {
             // if new user, lowercase userId
             user.setUsername(user.getUsername().toLowerCase());
         }
 
         // Get and prepare password management-related artifacts
         boolean passwordChanged = false;
         if (passwordEncoder != null) {
             // Check whether we have to encrypt (or re-encrypt) the password
             if (user.getVersion() == null) {
                 // New user, always encrypt
                 passwordChanged = true;
             } else {
                 // Existing user, check password in DB
                 String currentPassword = userDao.getUserPasswordById(user.getId());
                 if (currentPassword == null) {
                     passwordChanged = true;
                 } else {
                     if (!currentPassword.equals(user.getPassword())) {
                         passwordChanged = true;
                     }
                 }
             }
 
             // If password was changed (or new user), encrypt it
             if (passwordChanged) {
                 user.setPassword(passwordEncoder.encodePassword(user.getPassword(), null));
             }
         } else {
             log.warn("PasswordEncoder not set, skipping password encryption...");
         }
 
         try {
            String floor = user.getAddress().getHostelFloor();
            if (usersByFloor.containsKey(floor)) {
                usersByFloor.remove(floor);
            }
             return userDao.saveUser(user);
         } catch (DataIntegrityViolationException e) {
             // e.printStackTrace();
             log.warn(e.getMessage());
             throw new UserExistsException("User '" + user.getUsername() + "' already exists!");
         } catch (JpaSystemException e) { // needed for JPA
             // e.printStackTrace();
             log.warn(e.getMessage());
             throw new UserExistsException("User '" + user.getUsername() + "' already exists!");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeUser(String userId) {
         log.debug("removing user: " + userId);
         userDao.remove(new Long(userId));
     }
 
     /**
      * {@inheritDoc}
      * 
      * @param username the login name of the human
      * @return User the populated user object
      * @throws UsernameNotFoundException thrown when username not found
      */
     public User getUserByUsername(String username) throws UsernameNotFoundException {
         return (User) userDao.loadUserByUsername(username);
     }
 
     public List<User> getUsersByFloor(String floor) {
        if (usersByFloor.containsKey(floor)) {
            return usersByFloor.get(floor);
        }
        List<User> result = userDao.loadUsersByFloor(floor);
        usersByFloor.put(floor, result);
        return result;
     }
 
     public User getUserByRoomAndFullName(String room, String fullName) {
         return userDao.loadUserByRoomAndFullName(room, fullName);
     }
 }
