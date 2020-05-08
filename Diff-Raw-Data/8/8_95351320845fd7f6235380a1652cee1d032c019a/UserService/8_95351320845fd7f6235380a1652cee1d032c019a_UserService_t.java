 package service;
 
 import exceptions.CoreException;
 import models.school.Course;
 import models.user.User;
 import org.apache.commons.lang3.RandomStringUtils;
 import org.jasypt.util.password.StrongPasswordEncryptor;
 import org.joda.time.LocalDate;
 
 import javax.persistence.Query;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class UserService extends AbstractService<User> {
 
     public User save(User user) throws CoreException {
 
         if (null != getByIdBooster(user.idBooster)) {
             throw new CoreException().type(CoreException.Type.UNIQUE_CONSTRAINT_VIOLATION);
         }
 
         User last = User.find("order by staNumber desc limit 1").first();
 
         if (null == last) {
             user.staNumber = 1;
         } else {
             user.staNumber = last.staNumber + 1;
         }
 
         user.password = RandomStringUtils.randomAlphanumeric(9);
         user.save();
 
         return user;
     }
 
     public User getByIdBooster(String idBooser) {
         checkNotNull(idBooser, "ID booster is required");
         return User.find("byIdBooster", idBooser).first();
     }
 
     public List<User> getActiveUsers() {
         return User.em().createQuery("select u from User u " +
                 "where u.active = true " +
                 "order by u.idBooster desc")
                 .getResultList();
     }
 
     public List<User> getActiveUsersByIds(List<Long> ids) {
 
         if (null == ids || ids.isEmpty()) {
             return new ArrayList<User>();
         }
 
         return User.em().createQuery("select u from User u " +
                 "where u.active = true and u.id in (:ids)" +
                 "order by u.idBooster desc")
                 .setParameter("ids", ids)
                 .getResultList();
     }
 
     public User getFromLogin(String idBooster, String password) {
 
         checkNotNull(idBooster, "ID booster is required");
         checkNotNull(password, "Password is required");
 
         User user = User.find("byIdBooster", idBooster).first();
 
         if (null == user) {
             return null;
         }
 
        boolean isPasswordValid = new StrongPasswordEncryptor().checkPassword(password, user.password);

        // if user change the password and is not yet active, ugly fix
        if (!user.active && !isPasswordValid) {
             isPasswordValid = password.equals(user.password);
         }
 
         if (!isPasswordValid) {
             return null;
         }
 
         return user;
     }
 
     public void updateFromFirstLogin(User newUser, List<Course> courses, User existingUser) {
 
         existingUser.active = true;
         existingUser.skills = courses;
         existingUser.password = new StrongPasswordEncryptor().encryptPassword(newUser.password);
 
         updateFromPersonalInfo(newUser, existingUser);
 
         new ContractService().createForUser(existingUser);
     }
 
     public void updateFromPersonalInfo(User newUser, User existingUser) {
 
         existingUser.firstName = newUser.firstName;
         existingUser.lastName = newUser.lastName;
         existingUser.street = newUser.street;
         existingUser.postalCode = newUser.postalCode;
         existingUser.city = newUser.city;
         existingUser.siret = newUser.siret;
         existingUser.rcs = newUser.rcs;
 
         existingUser.save();
     }
 
     public void updateFromPassword(String newPassword, User user) {
         user.password = new StrongPasswordEncryptor().encryptPassword(newPassword);
         user.save();
     }
 
     public void updateFromSkills(List<Course> courses, User user) {
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
 
     public void remove(User user) {
 
         try {
             if (user.hasContract() && user.contract.pdf.exists()) {
                 user.contract.pdf.delete();
             }
 
             user.delete();
         } catch (Exception e) {
             throw new CoreException().type(CoreException.Type.REJECTED);
         }
     }
 
     public void changeActivationState(User user) {
         user.desactivated = !user.desactivated;
         user.save();
     }
 
     public User forgotPasswordRequest(User user) {
 
         user.passwordResetKey = new BigInteger(130, new SecureRandom()).toString(32);
         user.passwordResetDate = new LocalDate();
         user.save();
 
         return user;
     }
 
     public User resetPassword(User user, String password) {
 
         user.password = new StrongPasswordEncryptor().encryptPassword(password);
         user.passwordResetDate = null;
         user.passwordResetKey = null;
         user.save();
 
         return user;
     }
 }
