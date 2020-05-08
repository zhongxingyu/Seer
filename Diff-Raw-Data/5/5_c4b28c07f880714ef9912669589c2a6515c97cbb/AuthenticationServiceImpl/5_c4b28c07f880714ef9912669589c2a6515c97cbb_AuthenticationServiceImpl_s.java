 package no.niths.services.auth;
 
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import no.niths.application.rest.auth.SessionParcel;
 import no.niths.application.rest.exception.ExpiredTokenException;
 import no.niths.application.rest.exception.ObjectInCollectionException;
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.exception.UnvalidEmailException;
 import no.niths.application.rest.exception.UnvalidTokenException;
 import no.niths.common.constants.MiscConstants;
 import no.niths.common.constants.SecurityConstants;
 import no.niths.common.helpers.ValidationHelper;
 import no.niths.domain.development.Application;
 import no.niths.domain.development.Developer;
 import no.niths.domain.school.Role;
 import no.niths.domain.school.Student;
 import no.niths.security.ApplicationToken;
 import no.niths.security.DeveloperToken;
 import no.niths.security.RequestHolderDetails;
 import no.niths.security.SessionToken;
 import no.niths.services.auth.interfaces.AuthenticationService;
 import no.niths.services.auth.interfaces.GoogleAuthenticationService;
 import no.niths.services.auth.interfaces.KeyGeneratorService;
 import no.niths.services.auth.interfaces.TokenGeneratorService;
 import no.niths.services.development.interfaces.ApplicationService;
 import no.niths.services.development.interfaces.DeveloperService;
 import no.niths.services.interfaces.MailSenderService;
 import no.niths.services.school.interfaces.StudentService;
 
 import org.apache.commons.validator.routines.EmailValidator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.stereotype.Service;
 
 /**
  * Authenticates user trying to request a resource
  * <p>
  * This class delegates the request to the classes responsible for
  * verifying tokens and fetching the belonging apps, developers and students
  * </p>
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
      * {@inheritDoc}
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
      * {@inheritDoc}
      */
     @Override
     public RequestHolderDetails authenticateSessionToken(String sessionToken)
             throws AuthenticationException {
         logger.debug("Will authenticate session-token: " + sessionToken);
         
         // First check the format of the token        
         Long id = tokenService.verifyTokenFormat(sessionToken, true);
 
         // Fetch student owning the session token
         Student wantAccess = studentService.getById(id);
         // Then we verify the last login time of the student
         if (wantAccess == null || wantAccess.getSessionToken() == null) {
             logger.debug("No student has that session-token");
             throw new UnvalidTokenException(
                     "Token does not belong to a student");
         }
         if(!(wantAccess.getSessionToken().equals(sessionToken)) || wantAccess.getLastLogon() == null){
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
         Student temp = new Student();
         temp.setId(wantAccess.getId());
         temp.setLastLogon(wantAccess.getLastLogon());
         temp.setSessionToken(wantAccess.getSessionToken());
         studentService.mergeUpdate(temp);
 //        studentService.update(wantAccess);
 
         return authenticatedUser;
     }
 
     /**
      * {@inheritDoc}
      */
 	@Override
 	public void logout(Long studentId) {
 		Student wantToLogout = studentService.getById(studentId);
 		ValidationHelper.isObjectNull(wantToLogout, Student.class);
 		wantToLogout.setSessionToken(null);
 		studentService.update(wantToLogout);
 	}
 
     /**
      * {@inheritDoc}
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
         
         return devToken;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ApplicationToken registerApplication(Application app, String developerKey) 
             throws ObjectNotFoundException, ObjectInCollectionException {
         
         Developer dev = developerService.getDeveloperByDeveloperKey(developerKey);
         if(dev == null){
             throw new ObjectNotFoundException("No developer found");
         }
         ApplicationToken appToken = new ApplicationToken("No token");
 
         String appKey = keyService.generateApplicationKey();
         
         if(dev.getApps().contains(app)){
             throw new ObjectInCollectionException("App already added to developer");
         }
         appToken.setAppKey(appKey);
         app.setApplicationKey(appKey);
         dev.getApps().add(app);
         developerService.update(dev);
         mailService.sendDeveloperAddedAppConfirmation(dev, app);
         return appToken;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Long authenticateDeveloperToken(String devToken, String devKey) throws AuthenticationException{
         Long id = tokenService.verifyTokenFormat(devToken, false);
         
         Developer dev = developerService.getById(id);
         
 //        Developer dev = developerService.getDeveloperByDeveloperKey(devKey);
         
         if(dev == null){
             throw new UnvalidTokenException("No developer found for token/key");
         }else if(dev.getEnabled() == null || dev.getEnabled() == false){
             throw new UnvalidTokenException("Developer is not enabled");
         }else if (dev.getDeveloperToken() == null || !(dev.getDeveloperToken().equals(devToken))){
             throw new UnvalidTokenException("NOMt a correct token");
         }else if(dev.getDeveloperKey() == null || !dev.getDeveloperKey().equals(devKey)){
             throw new UnvalidTokenException("Not a correct key");
         }
             
         return dev.getId();
         
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Long authenticateApplicationToken(String applicationKey, String applicationToken)
                         throws AuthenticationException {
 
         Long id = tokenService.verifyTokenFormat(applicationToken, false);
         Application app = appService.getById(id);
 //        Application app = appService.getByApplicationKey(applicationKey, true);
         if(app == null ){
             throw new UnvalidTokenException("No app found or app is not enabled");
         }else if(app.getApplicationToken() == null || app.getEnabled() == null){
             throw new UnvalidTokenException("Application does not have a token");
         }else if(!(app.getApplicationToken().equals(applicationToken))){
             throw new UnvalidTokenException("Application token is not correct");
         }else if(app.getEnabled() == false){
         	throw new UnvalidTokenException("Application not enabled");
         }
 
         //Up the application request counter!
         if(app.getRequests() != null){
         	app.setRequests(app.getRequests() + 1);        	
         } else {
         	app.setRequests(new Long(1));
         }
         appService.update(app);
         
         return app.getId();
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Developer enableDeveloper(String developerKey) throws AuthenticationException{
         logger.debug("Trying to enable developer with token: " + developerKey);
         Developer dev = developerService.getDeveloperByDeveloperKey(developerKey);
         if(dev == null){
             throw new UnvalidTokenException("No developer found with that key");
         }
        if(dev.getEnabled() != null && dev.getEnabled() == true){
         	throw new UnvalidTokenException("Developer is already enabled");
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
      * {@inheritDoc}
      */
     @Override
     public Application enableApplication(String applicationKey) throws AuthenticationException{
         logger.debug("Trying to enable application with key: " + applicationKey);
         Application app = appService.getByApplicationKey(applicationKey, false);
         if(app == null){
             throw new UnvalidTokenException("No application found with that key");
         }
        if(app.getEnabled() != null && app.getEnabled() == true){
         	throw new UnvalidTokenException("Application is already enabled");
         }
         //Generate a personal token and set app to enabled
         app.setApplicationToken(tokenService.generateToken(app.getId()));
         app.setEnabled(true);
         appService.update(app);
         logger.debug("Application enabled " + applicationKey);
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
         if (!email.endsWith(MiscConstants.VALID_EMAIL_DOMAIN)) {
             logger.debug("email is unvalid: " + email);
             throw new UnvalidEmailException("Unvalid email, must end with " + MiscConstants.VALID_EMAIL_DOMAIN);
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
