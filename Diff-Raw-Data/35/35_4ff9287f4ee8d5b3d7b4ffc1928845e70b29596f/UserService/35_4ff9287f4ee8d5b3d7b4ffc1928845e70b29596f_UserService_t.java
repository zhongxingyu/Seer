 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.northwestern.bioinformatics.studycalendar.domain.User;
 import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import gov.nih.nci.security.UserProvisioningManager;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.*;
 
 @Transactional
 public class UserService {
 
     private UserDao userDao;
     private UserProvisioningManager userProvisioningManager;
     public static final String STUDY_CALENDAR_APPLICATION_ID = "2";
 
     public User saveUser(User user, String password) throws Exception {
         if(user == null)
             return null;
 
         if(user.getCsmUserId() == null) {
             gov.nih.nci.security.authorization.domainobjects.User csmUser = createCsmUser(user, password);
             user.setCsmUserId(csmUser.getUserId());
             if(csmUser.getUserId() == null)
                 throw new StudyCalendarSystemException("Csm User Id is null");
         } else {
             gov.nih.nci.security.authorization.domainobjects.User csmUser = userProvisioningManager.getUserById(user.getCsmUserId().toString());
             csmUser.setPassword(password);
             userProvisioningManager.modifyUser(csmUser);
         }
 
         return saveUser(user);
     }
 
     public User saveUser(User user) throws Exception {
         if (user == null) return null;
 
         userDao.save(user);
         return user;
     }
 
     private gov.nih.nci.security.authorization.domainobjects.User createCsmUser(User user, String password) throws Exception {
         gov.nih.nci.security.authorization.domainobjects.User csmUser =
                 new gov.nih.nci.security.authorization.domainobjects.User();
         csmUser.setLoginName(user.getName());
         csmUser.setPassword(password);
         csmUser.setFirstName("");   // Attribute can't be null
         csmUser.setLastName("");    // Attribute can't be null
         userProvisioningManager.createUser(csmUser);
         return csmUser;
     }
 
     @SuppressWarnings("unchecked")
     public User getUserByName(String userName) {
         User usersResults = userDao.getByName(userName);
         return usersResults;
     }
 
     public List<User> getAllUsers() throws Exception {
         return userDao.getAll();
     }
 
     public User getUserById(int id)  throws Exception {
         return userDao.getById(id);
     }
 
     public List<User> getParticipantCoordinatorsForSites(List<Site> sites) {
         List<User> users = userDao.getAllParticipantCoordinators();
         List<User> associatedUsers = new ArrayList<User>();
         for (User user : users) {
             for (Site site : sites) {
                 UserRole userRole = UserRole.findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
                 if (userRole != null && userRole.getSites().contains(site)) {
                     associatedUsers.add(user);
                     break;
                 }
             }
         }
         return associatedUsers;
     }
 
     ////// CONFIGURATION
     @Required
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     @Required
     public void setUserProvisioningManager(UserProvisioningManager userProvisioningManager) {
         this.userProvisioningManager = userProvisioningManager;
     }
 }
