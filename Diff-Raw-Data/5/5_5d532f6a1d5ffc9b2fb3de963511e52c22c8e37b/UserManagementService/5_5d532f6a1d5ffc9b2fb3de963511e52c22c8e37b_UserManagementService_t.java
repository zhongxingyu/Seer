 package org.homebudget.services;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.annotation.Resource;
 import org.apache.log4j.Logger;
 import org.homebudget.dao.UserRepository;
 import org.homebudget.dao.UserRoleRepository;
 import org.homebudget.model.UserDetails;
 import org.homebudget.model.UserRole;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 public class UserManagementService {
 
     private static final Logger gLogger = Logger.getLogger(UserManagementService.class);
     @Resource
     private UserRepository userRepositoryDao;
     
     @Resource
     private UserRoleRepository userRoleRepository;
     
     @Resource
     private MailConfirmationService mailConfirmationService;
 
     @Transactional
     public void saveUserDetails(UserDetails userDetails) {
         userRepositoryDao.save(userDetails);
     }
     
     @Transactional
     public void saveUserRole(UserRole userRole) {
         userRoleRepository.save(userRole);
     }
     
      public void registerUser(UserDetails aUserDetails) {
         String dateString = aUserDetails.getDateString();
         Date birthday = getBirthdayFromString(dateString);
         aUserDetails.setUserBirthday(birthday);
         final String userPassword = aUserDetails.getPassword();
         try {
             String passwordHash = PasswordService.getHash(aUserDetails
                     .getPassword());
             aUserDetails.setPassword(passwordHash);
         } catch (Exception e) {// with is a hack. Must be removed. If hashing
             // fails, user must be notified.
             aUserDetails.setPassword(userPassword);
         }
         aUserDetails.addUserRole(UserRole.Role.USER_ROLE);
         //TODO: set to 0, when email confirmation is implemented
         aUserDetails.setEnabled(1);
        mailConfirmationService.sendConfirmation(aUserDetails);
         saveUserDetails(aUserDetails);
     }
      
        private Date getBirthdayFromString(String dateString) {
         // password is replaced with hash after validation of the form.
         DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
         System.out.println("DateString read: " + dateString);
         gLogger.info("DateString read: " + dateString);
         Date birthday = null;
         try {
             birthday = format.parse(dateString);
         } catch (ParseException ex) {
            gLogger.error("Datestring could not be parsed " + dateString);
         }
         return birthday;
     }
      
 //    public UserRole getRole(Role role){
 //        UserRole result = userRoleRepository.findByUserRole(role);
 //        return  result;
 //     }
 }
