 package service;
 
 import exceptions.CoreException;
 import models.school.Course;
 import models.school.YearCourse;
 import models.user.User;
 import org.apache.commons.lang3.RandomStringUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.jasypt.util.password.StrongPasswordEncryptor;
 import play.db.jpa.Model;
 
 import javax.persistence.Query;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class UserService extends AbstractService<User> {
 
     public User save(User user) throws CoreException {
 
         if (null != getByIdBooster(user.idBooster)) {
             throw new CoreException().type(CoreException.Type.UNIQUE_CONSTRAINT_VIOLATION);
         }
 
         user.password = RandomStringUtils.randomAlphanumeric(9);
         user.save();
 
         return user;
     }
 
     public User getById(long id) {
         User user = User.findById(id);
         return user;
     }
 
     public User getByIdBooster(String idBooser) {
         checkNotNull(idBooser, "ID booster is required");
         return User.find("byIdBooster", idBooser).first();
     }
 
     public List<User> getActiveUsers() {
         return User.em().createQuery("select u from User u where u.active = true").getResultList();
     }
 
     public User getFromLogin(String idBooster, String password) {
 
         checkNotNull(idBooster, "ID booster is required");
         checkNotNull(password, "Password is required");
 
         User user = User.find("byIdBooster", idBooster).first();
 
         if (null == user) {
             return null;
         }
 
         boolean isPasswordValid;
         if (user.active) {
             isPasswordValid = new StrongPasswordEncryptor().checkPassword(password, user.password);
         } else {
             isPasswordValid = password.equals(user.password);
         }
 
         if (!isPasswordValid) {
             return null;
         }
 
         return user;
     }
 
     public void updateFromFirstLogin(User newUser, Set<Course> courses, User existingUser) {
 
         existingUser.active = true;
         existingUser.skills = courses;
         existingUser.password = new StrongPasswordEncryptor().encryptPassword(newUser.password);
 
         updateFromPersonalInfo(newUser, existingUser);
     }
 
     public void updateFromPersonalInfo(User newUser, User existingUser) {
 
         existingUser.firstName = newUser.firstName;
         existingUser.lastName = newUser.lastName;
         existingUser.street = newUser.street;
         existingUser.postalCode = newUser.postalCode;
         existingUser.city = newUser.city;
         existingUser.siret = newUser.siret;
 
         existingUser.save();
     }
 
     public void updateFromPassword(String newPassword, User user) {
         user.password = new StrongPasswordEncryptor().encryptPassword(newPassword);
         user.save();
     }
 
     public void updateFromSkills(Set<Course> courses, User user) {
         user.skills = courses;
         user.save();
     }
 
     public User findUserWithContratAndOrders(long id) {
 
         Query query = User.em().createQuery("select u from User u " +
                 "left join u.contract c left join u.orders o " +
                 "where u.id = :id");
        
         query.setParameter("id", id);
 
         try {
             return (User) query.getSingleResult();
         } catch (Exception e) {
             return null;
         }
 
     }
 }
