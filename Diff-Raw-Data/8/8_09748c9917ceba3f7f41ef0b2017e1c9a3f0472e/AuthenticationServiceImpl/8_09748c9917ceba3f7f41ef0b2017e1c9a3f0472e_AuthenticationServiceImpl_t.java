 package no.niths.services.auth;
 
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import no.niths.application.rest.auth.SessionParcel;
 import no.niths.application.rest.exception.DuplicateEntryCollectionException;
 import no.niths.application.rest.exception.ExpiredTokenException;
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.exception.UnvalidEmailException;
 import no.niths.application.rest.exception.UnvalidTokenException;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.domain.Application;
 import no.niths.domain.Developer;
 import no.niths.domain.Student;
 import no.niths.domain.security.Role;
 import no.niths.security.ApplicationToken;
 import no.niths.security.DeveloperToken;
 import no.niths.security.RequestHolderDetails;
 import no.niths.security.SessionToken;
 import no.niths.services.auth.interfaces.AuthenticationService;
 import no.niths.services.auth.interfaces.GoogleAuthenticationService;
 import no.niths.services.auth.interfaces.KeyGeneratorService;
 import no.niths.services.auth.interfaces.TokenGeneratorService;
 import no.niths.services.interfaces.ApplicationService;
 import no.niths.services.interfaces.DeveloperService;
 import no.niths.services.interfaces.MailSenderService;
 import no.niths.services.interfaces.StudentService;
 
 import org.apache.commons.validator.routines.EmailValidator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.stereotype.Service;
 
 /**
  * Authenticates user trying to request a resource
  * 
  */
 @Service
 public class AuthenticationServiceImpl implements AuthenticationService {
 
     private static final Logger logger = LoggerFactory
             .getLogger(AuthenticationServiceImpl.class);
 
     @Autowired
     private StudentService studentService;
     
     @Autowired
     private DeveloperService developerService;
 
     @Autowired
     private GoogleAuthenticationService googleService;
     
     @Autowired
     private TokenGeneratorService tokenService;
     
     @Autowired
     private ApplicationService appService;
     
     @Autowired
     private MailSenderService mailService;
     
     @Autowired
     private KeyGeneratorService keyService;
 
     /**
      * Authenticates a student via Google. If authentication succeeds, student
      * is either fetched from DB or if the student is a first time user, he/she
      * gets persisted.
      * <p>
      * Returns a session token valid for {@value AppConstants.SESSION_VALID_TIME}
      * minutes.
      * <p> 
      * Use this session token for future requests against the API
      * <p>
      * How to use:
      * <pre>
      * {@code
      * Place in header:
      * Session-token: ojejcndiu23io2hjUILHDSDW21.wqi8h2
      * Accept: Application/xml
      * }
      * </pre>
      * @param googleToken    the string to authenticate. If null, or not correct
      *                         a 401 will be in the response.
      * 
      * @return SessionToken the string to use in future requests against the 
      *                         API. It is valid for {@value SecurityConstants.SESSION_VALID_TIME} ms.
      *                         Max concurrent session is {@value SecurityConstants.MAX_SESSION_VALID_TIME} ms.
      *                         
      */
     @Override
     public SessionParcel authenticateAtGoogle(String googleToken)
             throws UnvalidEmailException{
 
         // Authenticate user from Google, 
         // and then check to see if the email is valid
         String userEmail = googleService.authenticateAndGetEmail(googleToken);
         isUserValid(userEmail); //Verify email
         
         //Get the matching student
         //If no student exists, we persist
         Student authenticatedStudent = getStudent(userEmail);
         // Generate "session token" that the app will use from now on
         String generatedToken = tokenService.generateToken(authenticatedStudent.getId());
         // Add the generated token to the student,
         // and update last login time
        Student temp = new Student();
        temp.setId(authenticatedStudent.getId());
        temp.setSessionToken(generatedToken);
        temp.setLastLogon(getCurrentTime());
        studentService.mergeUpdate(temp);
         
         // Create a wrapper to give to the request holder
         SessionToken sessionToken = new SessionToken(); 
         sessionToken.setToken(generatedToken);
         sessionToken.setStudentId(authenticatedStudent.getId());
 
         SessionParcel sessionParcel =
                 new SessionParcel(authenticatedStudent, sessionToken);
 
         return sessionParcel;
     }
 
     /**
      * Authenticates the session token from a request. 
      * <p>
      * Uses a TokenGeneratorService to verify the format of the token and any
      * errors will throw an AuthenticationException with an "Error" header.
      * If format is verified we then fetches belonging student from DB.
      * <p>
      * We then create a User wrapper object with roles copied from the student and
      * return that user to the class responsible for doing the actual authentication.
      * 
      * @param sessionToken     the string to verify. If not correct, an Authentication
      *                         exception will occur with an "Error header" explaining
      *                         the issue.
      * @return a user object with roles from student belonging to the session
      *         token
      */
     @Override
     public RequestHolderDetails authenticateSessionToken(String sessionToken)
             throws AuthenticationException {
         logger.debug("Will authenticate session-token: " + sessionToken);
 
         //TEST MODE:
 //        RequestHolderDetails testUser = new RequestHolderDetails("rosen09@nith.no");
 //        testUser.addRoleName("ROLE_STUDENT");
 //        testUser.addRoleName("ROLE_SR");
 //        testUser.setStudentId(new Long(1));
 //        return testUser;
         //END TESTMODE
         
         // First check the format of the token        
         tokenService.verifyTokenFormat(sessionToken, true);
 
         // Fetch student owning the session token
         Student wantAccess = studentService
                 .getStudentBySessionToken(sessionToken);
         // Then we verify the last login time of the student
         if (wantAccess == null) {
             logger.debug("No student has that session-token");
             throw new UnvalidTokenException(
                     "Token does not belong to a student");
         }
         if(wantAccess.getLastLogon() == null){
             throw new UnvalidTokenException("Can not find last login");
         }
         verifyLastLogonTime(wantAccess.getLastLogon());
 
         // The information added here is used in the @Security annotations
         RequestHolderDetails authenticatedUser = new RequestHolderDetails(); // ROLE_ANONYMOUS --> Wrapper
         authenticatedUser.setUserName(wantAccess.getEmail());
         authenticatedUser.setStudentId(wantAccess.getId());
 
         // Checking roles of student and adding them to User wrapper
         List<Role> roles = wantAccess.getRoles();
         if (!(roles.isEmpty())) {
             String loggerText = "Student logging in has role(s): ";
             for (Role role : roles) {
                 loggerText += role.getRoleName() + " ";
                 authenticatedUser.addRoleName(role.getRoleName());
             }
             logger.debug(loggerText);
         }
         // Update last login time
         wantAccess.setLastLogon(getCurrentTime());
         studentService.update(wantAccess);
 
         return authenticatedUser;
     }
     
     /**
      * Register a developer and generates a developer token that the
      * developer uses in future requests
      * <p>
      * Sends an email to the developer with confirmation and instructions
      * on how to enable the account
      * <p>
      * @param developer the developer to persist
      * @return DeveloperToken the developer key and a confirmation message 
      */
     @Override
     public DeveloperToken registerDeveloper(Developer dev) {
         //Verify developer email
         isEmailValid(dev.getEmail());
         
         //Passed checks! Generate a key and persist the developer
         String developerKey = keyService.generateDeveloperKey();
         dev.setDeveloperKey(developerKey);
         developerService.create(dev);
 
         logger.debug("Developer[" + dev.getId() + "] has been created and given key: " + dev.getDeveloperKey());
         
         //Create response to the request holder
         DeveloperToken devToken = new DeveloperToken();
         devToken.setKey(developerKey);
         
         logger.debug("Developer[" + dev.getId() + "] has been given key: " + devToken.getKey());
         
         mailService.sendDeveloperRegistratedConfirmation(dev);
         
 //        devToken.setMessage("Failed to send an email, but now worries! \n"
 //                    + "To enable your new developer account paste this into a browser\n" +
 //                    AppConstants.NITHS_BASE_DOMAIN + "register/enable/" + dev.getDeveloperKey());
         
         
         return devToken;
     }
     
     /**
      * Registers an application to the matching developer
      * <p>
      * Sends an email to the developer with confirmation and 
      * information on how to proceed
      * <p>
      * 
      * @param app the application to add
      * @param devId id of the dev to add application to
      * @return an application token to use in future requests
      * 
      */
     @Override
     public ApplicationToken registerApplication(Application app, String developerKey) 
             throws ObjectNotFoundException, DuplicateEntryCollectionException {
         
         Developer dev = developerService.getDeveloperByDeveloperKey(developerKey);
         if(dev == null){
             throw new ObjectNotFoundException("No developer found");
         }
         ApplicationToken appToken = new ApplicationToken("No token");
 
         String appKey = keyService.generateApplicationKey();
         
         if(dev.getApps().contains(app)){
             throw new DuplicateEntryCollectionException("App already added to developer");
         }
         
         appToken.setAppKey(appKey);
         app.setApplicationKey(appKey);
         dev.getApps().add(app);
         developerService.update(dev);
         mailService.sendDeveloperAddedAppConfirmation(dev, app);
         return appToken;
     }
     
     /**
      * Authenticates the developer token. Verifies the format of the token and 
      * and fetches matching student from DB based on the key. Then checks if 
      * developer token is correct
      * 
      * @param devToken the developer token
      * @param devKey the developer key
      * @throws AuthenticationException if no matching student is found
      * 
      */
     @Override
     public Long authenticateDeveloperToken(String devToken, String devKey) throws AuthenticationException{
         //TEST MODE
 //        return new Long(1);
         //TEST MODE
 
         tokenService.verifyTokenFormat(devToken, false);
         Developer dev = developerService.getDeveloperByDeveloperKey(devKey);
         
         if(dev == null){
             throw new UnvalidTokenException("No developer found for token/key");
         }else if(dev.getEnabled() == null){
             throw new UnvalidTokenException("Developer is not enabled");
         }else if (dev.getDeveloperToken() == null){
             throw new UnvalidTokenException("Developer does not have a developer token");
         }else if(dev.getEnabled() == false || !dev.getDeveloperToken().equals(devToken)){
             throw new UnvalidTokenException("Not a correct token or dev is not enabled");
         }
             
         return dev.getId();
         
     }
     
 
     /**
      * Authenticates the application token.
      * <p>
      * Verifies the token format and, based on the key,
      * fetches the matching application from DB,
      * if it is enabled.
      * <p>
      * @param applicationKey the application key
      * @param applicationToken the application token
      * @return id of the application
      * @throws AuthenticationException if no matching app is found
      */
     @Override
     public Long authenticateApplicationToken(String applicationKey, String applicationToken)
                         throws AuthenticationException {
         //TEST MODE
 //        return new Long(1);
         //TEST MODE
         
         tokenService.verifyTokenFormat(applicationToken, true);
         Application app = appService.getByApplicationKey(applicationKey, true);
         if(app == null){
             throw new UnvalidTokenException("No app found or app is not enabled");
         }else if(app.getApplicationToken() == null){
             throw new UnvalidTokenException("Application does not have a token");
         }else if(!app.getApplicationToken().equals(applicationToken)){
             throw new UnvalidTokenException("Application token is not correct");
         }
         return app.getId();
     }
     
     
     /**
      * Enables a developer, needed to be able to do requests towards the API
      * <p>
      * Developer must exist in the DB, or else enabling will fail...
      * <p>
      * Sends the developer a confirmation email with instructions
      * <p>
      * @param developerToken string return from registerDeveloper(Dev)
      * @return the developer object, null if not found
      */
     @Override
     public Developer enableDeveloper(String developerKey) throws AuthenticationException{
         logger.debug("Trying to enable developer with token: " + developerKey);
         Developer dev = developerService.getDeveloperByDeveloperKey(developerKey);
         if(dev == null){
             throw new UnvalidTokenException("No developer found with that key");
         }
         
         //Generate a personal token and set developer to enabled
         dev.setDeveloperToken(tokenService.generateToken(dev.getId()));
         dev.setEnabled(true);
         developerService.update(dev);
         
         //Send confirmation email
         mailService.sendDeveloperEnabledConfirmation(dev);
         
         return dev;
     }
     
     /**
      * 
      * Enables an application
      * <p>
      * Sends the developer a confirmation email with instructions
      * <p>
      * 
      * @param applicationKey 
      * @return the Application
      * @throws AuthenticationException
      */
     @Override
     public Application enableApplication(String applicationKey) throws AuthenticationException{
         logger.debug("Trying to enable application with key: " + applicationKey);
         Application app = appService.getByApplicationKey(applicationKey, false);
         if(app == null){
             throw new UnvalidTokenException("No application found with that key");
         }
         //Generate a personal token and set app to enabled
         app.setApplicationToken(tokenService.generateToken(app.getId()));
         app.setEnabled(true);
         appService.update(app);
         
         //Send confirmation email
         if(app.getDeveloper() != null){
             mailService.sendApplicationEnabledConfirmation(app.getDeveloper(), app);            
         }
         
         return app;
     }
 
     /**
      * Verifies the last login time against the {@value SecurityConstants.SESSION_VALID_TIME} 
      * 
      * @param lastLogon long time student had last login in ms
      * @throws AuthenticationException when session token has expired
      */
     private void verifyLastLogonTime(long lastLogon)
             throws AuthenticationException {
         logger.debug("Verifying last login time...");
         if (!(System.currentTimeMillis() - lastLogon <= SecurityConstants.SESSION_VALID_TIME)) {
             logger.debug("Token expired");
             throw new ExpiredTokenException("Session-token has expired");
         }
         logger.debug("Verified");
     }
 
     /**
      * Fetches student from DB. If no student matches the email, a new student
      * will be created and persisted
      * 
      * @param userEmail
      *            the email to the student
      * @return Student with the email, existing or newly created
      */
     private Student getStudent(String userEmail) {
         // Fetches the student from DB, if first time user, he/she gets
         // persisted
         Student student = studentService.getStudentByEmail(userEmail);
         if (student == null) { // First time user, persist!
             logger.debug("Student is a first time user, persisting.");
             student = new Student(userEmail);
             Long id = studentService.create(student);
             student.setId(id);
         }
         return student;
     }
 
     /**
      * Check if the email of the user is valid(nith.no) and passes bean validation
      * @param email the string to check
      * @throws UnvalidEmailException
      */
     private void isUserValid(String email) throws UnvalidEmailException{
         isEmailValid(email);
         if (!email.endsWith(AppConstants.VALID_EMAIL_DOMAIN)) {
             logger.debug("email is unvalid: " + email);
             throw new UnvalidEmailException("Unvalid email, must end with " + AppConstants.VALID_EMAIL_DOMAIN);
         }
         logger.debug("Email valid: " + email);
     }
     
     /**
      * Return true if the email is valid
      * @param email string to check
      */
     private void isEmailValid(String email){
         EmailValidator validator = EmailValidator.getInstance();
         if(!validator.isValid(email)){
             throw new UnvalidEmailException("Unvalid email, did you forget @?");
         }
     }
 
     //Private helper
     private long getCurrentTime() {
         return new GregorianCalendar().getTimeInMillis();
     }
 }
