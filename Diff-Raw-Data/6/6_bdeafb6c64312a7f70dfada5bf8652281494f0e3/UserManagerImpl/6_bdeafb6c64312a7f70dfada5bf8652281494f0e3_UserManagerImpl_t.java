 package org.patientview.radar.service.impl;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.patientview.model.Patient;
 import org.patientview.radar.dao.DemographicsDao;
 import org.patientview.radar.dao.UserDao;
 import org.patientview.radar.model.exception.DaoException;
 import org.patientview.radar.model.exception.DecryptionException;
 import org.patientview.radar.model.exception.EmailAddressNotFoundException;
 import org.patientview.radar.model.exception.InvalidSecurityQuestionAnswer;
 import org.patientview.radar.model.exception.RegistrationException;
 import org.patientview.radar.model.exception.UserEmailAlreadyExists;
 import org.patientview.radar.model.filter.PatientUserFilter;
 import org.patientview.radar.model.filter.ProfessionalUserFilter;
 import org.patientview.radar.model.user.AdminUser;
 import org.patientview.radar.model.user.PatientUser;
 import org.patientview.radar.model.user.ProfessionalUser;
 import org.patientview.radar.model.user.User;
 import org.patientview.radar.service.EmailManager;
 import org.patientview.radar.service.UserManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.authentication.ProviderManager;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 import java.util.Date;
 import java.util.List;
 
 public class UserManagerImpl implements UserManager, UserDetailsService {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(UserManagerImpl.class);
 
     private EmailManager emailManager;
     private ProviderManager authenticationManager;
 
     private DemographicsDao demographicsDao;
     private UserDao userDao;
 
     public AdminUser getAdminUser(String email) {
         return userDao.getAdminUser(email);
     }
 
     public AdminUser getAdminUserWithUsername(String username) {
         return userDao.getAdminUserWithUsername(username);
     }
 
     public PatientUser getPatientUser(Long id) {
         return userDao.getPatientUser(id);
     }
 
     public PatientUser getPatientUser(String email) {
         return userDao.getPatientUser(email);
     }
 
     public PatientUser getPatientUserWithUsername(String username) {
         return userDao.getPatientUserWithUsername(username);
     }
 
     public PatientUser getPatientUser(String email, Date dateOfBirth) {
         PatientUser user = userDao.getPatientUser(email);
         if (user != null) {
             return user.getDateOfBirth().equals(dateOfBirth) ? user : null;
         }
         return null;
     }
 
     public PatientUser getPatientUserWithUsername(String username, Date dateOfBirth) {
         PatientUser user = userDao.getPatientUserWithUsername(username);
         if (user != null) {
             return user.getDateOfBirth().equals(dateOfBirth) ? user : null;
         }
         return null;
     }
 
     public List<PatientUser> getPatientUsers() {
         return getPatientUsers(new PatientUserFilter(), -1, -1);
     }
 
     public List<PatientUser> getPatientUsers(PatientUserFilter filter) {
         return getPatientUsers(filter, -1, -1);
     }
 
     public List<PatientUser> getPatientUsers(PatientUserFilter filter, int page, int numberPerPage) {
         return userDao.getPatientUsers(filter, page, numberPerPage);
     }
 
     public void savePatientUser(PatientUser patientUser) throws Exception {
 
         userDao.savePatientUser(patientUser);
     }
 
     public void deletePatientUser(PatientUser patientUser) throws Exception {
         userDao.deletePatientUser(patientUser);
     }
 
     public void registerPatient(Patient patient) throws Exception {
         // Check we have a valid radar number, email address and date of birth
         if (patient == null || patient.getId() < 1) {
             throw new IllegalArgumentException("Invalid demographics supplied to registerPatient");
         }
 
         if (patient.getDob() == null) {
             throw new IllegalArgumentException("Missing required parameter to registerPatient: " +
                     "demographics.getDateOfBirth()");
         }
 
         if (patient.getNhsno() == null) {
             throw new IllegalArgumentException("Missing required parameter to registerPatient: " +
                     "demographics.getNhsNumber()");
         }
 
         // Register the patient with patient view table with mapping
         // userDao.createRawUser(patient., "TestUser", "Test User", nhsNo + "@aptientview.com", patient.getUnitcode(),
         // patient.getNhsno());
 
         PatientUser patientUser = createPatientViewUser(patient);
 
 
         // if this demographic is already an existing patient, just skip the registration
         if (userDao.getPatientUser(patientUser.getUserId()) == null) {
 
             // now fill in the radar patient stuff
             patientUser.setRadarNumber(patient.getId());
             patientUser.setDateOfBirth(patient.getDob());
 
             // Update the user record created by patient view and create radar patient row and user mapping row
             userDao.savePatientUser(patientUser);
         }
 
 
         // however lets make sure the mappings exist.
         try {
             // renal_unit
             if (!userDao.userExistsInPatientView(patient.getNhsno(), patient.getRenalUnit().getUnitCode())) {
                 userDao.createUserMappingInPatientView(patientUser.getUsername(),
                         patient.getNhsno(), patient.getRenalUnit().getUnitCode());
             }
             // unitcode
             if (!userDao.userExistsInPatientView(patient.getNhsno(), patient.getDiseaseGroup().getId())) {
                 userDao.createUserMappingInPatientView(patientUser.getUsername(),
                         patient.getNhsno(), patient.getDiseaseGroup().getId());
             }
         } catch (Exception e) {
             LOGGER.error("Unable to create usermapping using {}", patient.getNhsno());
         }
     }
 
     private PatientUser createPatientViewUser(Patient patient) {
 
         PatientUser patientUser = userDao.getExternallyCreatedPatientUser(patient.getNhsno());

         String username = generateUsername(patient);
         String name = patient.getForename() + " " + patient.getSurname();
 
         // Not registered on the system so create a username for them and a mapping to the patients unit
        if (patientUser == null) {
             userDao.createRawUser(username, null, name, patient.getEmailAddress(), patient.getUnitcode(),
                     patient.getNhsno());
            patientUser = userDao.getExternallyCreatedPatientUser(patient.getNhsno());
         }
 
         return patientUser;
     }
 
     public void registerProfessional(ProfessionalUser professionalUser) throws UserEmailAlreadyExists,
             InvalidSecurityQuestionAnswer, RegistrationException {
         User user = userDao.getProfessionalUser(professionalUser.getEmail());
         if (user != null) {
             throw new UserEmailAlreadyExists("Email address already exists");
         }
 
         if (!professionalUser.getSecurityQuestion().equals(professionalUser.getSecurityQuestionAnsw())) {
             throw new InvalidSecurityQuestionAnswer("Security question answer is incorrect");
         }
 
         try {
             saveProfessionalUser(professionalUser);
         } catch (Exception e) {
             LOGGER.error("Could not register professional", e);
             throw new RegistrationException("Could not register professional", e);
         }
         emailManager.sendProfessionalRegistrationAdminNotificationEmail(professionalUser);
     }
 
     public ProfessionalUser getProfessionalUser(Long id) {
         return userDao.getProfessionalUser(id);
     }
 
     public ProfessionalUser getProfessionalUser(String email) {
         return userDao.getProfessionalUser(email);
     }
 
     public ProfessionalUser getProfessionalUserWithUsername(String username) {
         return userDao.getProfessionalUserWithUsername(username);
     }
 
     public User getSuperUserWithUsername(String username) {
         return userDao.getSuperUserWithUsername(username);
     }
 
     public void saveProfessionalUser(ProfessionalUser professionalUser) throws Exception {
         // if its a new user generate a password
         if (!professionalUser.hasValidId()) {
             String password = generateRandomPassword();
             professionalUser.setPassword(ProfessionalUser.getPasswordHash(password));
             professionalUser.setUsername(professionalUser.getEmail());
         }
 
         userDao.saveProfessionalUser(professionalUser);
     }
 
     public void deleteProfessionalUser(ProfessionalUser professionalUser) throws Exception {
         userDao.deleteProfessionalUser(professionalUser);
     }
 
     public List<ProfessionalUser> getProfessionalUsers() {
         return getProfessionalUsers(new ProfessionalUserFilter(), -1, -1);
     }
 
     public List<ProfessionalUser> getProfessionalUsers(ProfessionalUserFilter filter) {
         return getProfessionalUsers(filter, -1, -1);
     }
 
     public List<ProfessionalUser> getProfessionalUsers(ProfessionalUserFilter filter, int page, int numberPerPage) {
         return userDao.getProfessionalUsers(filter, page, numberPerPage);
     }
 
     public boolean authenticateProfessionalUser(String username, String password) throws AuthenticationException {
         ProfessionalUser professionalUser = userDao.getProfessionalUser(username);
         if (professionalUser != null) {
             try {
                 Authentication authentication = authenticationManager.
                         authenticate(new UsernamePasswordAuthenticationToken(username, password));
                 return authentication.isAuthenticated();
             } catch (AuthenticationException e) {
                 LOGGER.warn("Authentication failed for user {} and password {}", username, e.getMessage());
                 throw e;
             }
         }
         return false;
     }
 
     public void changeUserPassword(String username, String password) throws DecryptionException, DaoException {
         ProfessionalUser professionalUser = getProfessionalUser(username);
 
         try {
             userDao.saveProfessionalUser(professionalUser);
         } catch (Exception e) {
             LOGGER.error("could not save professional user", e);
             throw new DaoException("Could not save professional user");
         }
     }
 
     public boolean userExistsInPatientView(String nhsno) {
         return userDao.userExistsInPatientView(nhsno);
     }
 
     public void sendForgottenPasswordToPatient(String username) throws EmailAddressNotFoundException,
             DecryptionException {
         // In theory this could just go in the email manager but we need to query for user first
         PatientUser patientUser = userDao.getPatientUser(username);
         if (patientUser != null) {
             try {
                 String password = generateRandomPassword();
                 patientUser.setPassword(ProfessionalUser.getPasswordHash(password));
 
                 userDao.savePatientUser(patientUser);
 
                 emailManager.sendForgottenPassword(patientUser, password);
             } catch (Exception e) {
                 LOGGER.error("Could not decrypt password for forgotten password email for {}", username, e);
                 throw new DecryptionException("Could not decrypt password for forgotten password email", e);
             }
         } else {
             LOGGER.error("Could not find user with email {}", username);
             throw new EmailAddressNotFoundException("Email Address not found");
         }
     }
 
     public void sendForgottenPasswordToProfessional(String username) throws EmailAddressNotFoundException,
             DecryptionException {
         // In theory this could just go in the email manager but we need to query for user first
         ProfessionalUser professionalUser = userDao.getProfessionalUser(username);
         if (professionalUser != null) {
             try {
                 String password = generateRandomPassword();
                 professionalUser.setPassword(ProfessionalUser.getPasswordHash(password));
 
                 userDao.saveProfessionalUser(professionalUser);
 
                 emailManager.sendForgottenPassword(professionalUser, password);
             } catch (Exception e) {
                 LOGGER.error("Could not decrypt");
                 throw new DecryptionException("Could not decrypt", e);
             }
         } else {
             LOGGER.error("Could not find user with email {}", username);
             throw new EmailAddressNotFoundException("Email Address not found");
         }
     }
 
     private String generateRandomPassword() {
         // I love you Apache commons
         return RandomStringUtils.randomAlphanumeric(8);
     }
 
     public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException, DataAccessException {
         // Pull the user from the DAO
         User user = userDao.getProfessionalUser(email);
         if (user != null) {
             return user;
         }
         throw new UsernameNotFoundException("User not found with email address " + email);
     }
 
     public void setEmailManager(EmailManager emailManager) {
         this.emailManager = emailManager;
     }
 
     public void setDemographicsDao(DemographicsDao demographicsDao) {
         this.demographicsDao = demographicsDao;
     }
 
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     public void setAuthenticationManager(ProviderManager authenticationManager) {
         this.authenticationManager = authenticationManager;
     }
 
     private String generateUsername(Patient patient) {
 
         String username = patient.getForename() + "." + patient.getSurname();
         //Strip non alpha numeric characters
         username = username.replaceAll("\\P{Alnum}", "");
 
         int i = 1;
         while (userDao.getPatientUserWithUsername(username) != null) {
             username = username + ++i;
 
         }
         return username;
     }
 
 
 
 }
