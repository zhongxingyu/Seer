 package no.niths.services.auth;
 
 import no.niths.security.RequestHolderDetails;
 import no.niths.services.auth.interfaces.AuthenticationService;
 import no.niths.services.auth.interfaces.UserDetailService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Component;
 /**
  * Class responsible for feching the user holding the request  
  * 
  */
 @Component
 public class UserDetailServiceImpl implements UserDetailService {
 
 	@Autowired
 	private AuthenticationService authService;
 	
 	/**
 	 * Implement your own version of this method to return UserDetails
 	 * 
 	 * See the other methods for a how to
 	 * 
 	 */
 	@Override
 	public UserDetails loadUserByUsername(String sessionToken)
 			throws UsernameNotFoundException {
 		
 		return null;
 	}
 	
 
 	/**
 	 * Calls on authentication to authenticate the session token
 	 * 
 	 * @return UserDetails the details of the student with belonging session token
 	 * @throws UsernameNotFoundException when no student is found
 	 */
 	@Override
 	public UserDetails loadStudentBySessionToken(String sessionToken) throws UsernameNotFoundException{
 		//TEST MODE:
 //		RequestHolderDetails testUser = new RequestHolderDetails("rosen09@nith.no");
 //		testUser.addRoleName("ROLE_STUDENT");
 //		testUser.addRoleName("ROLE_SR");
 //		testUser.setStudentId(new Long(1));
 //		return testUser;
 		//END TESTMODE
 		RequestHolderDetails user = authService.authenticateSessionToken(sessionToken);
 		if(user == null){
 			throw new UsernameNotFoundException("Could not find user with that sessiontoken");
 		}
 		
 		return user;
 	}
 	
 	/**
 	 * Calls on authentication service to authenticate the developer
 	 * 
 	 * @param developerToken the string to verify
 	 * @return id of the developer
 	 * @throws UsernameNotFoundException if no developer is found
 	 * 
 	 */
 	@Override
 	public Long loadDeveloperIdFromDeveloperToken(String developerToken) throws UsernameNotFoundException{
 		//TEST MODE:
 //		return new Long(1);
 		//END TESTMODE
 		Long id = authService.authenticateDeveloperToken(developerToken);
 		if(id == null){
 			throw new UsernameNotFoundException("Could not find a developer with that developer token");
 		}
 		return id;
 	}
 	
 	/**
 	 * Calls on authenticationservice to authenticate the application
 	 * 
 	 * @param applicationToken string to verify
 	 * @return id of the belonging app
 	 * @throws UsernameNotFoundException of no app is found
 	 */
 	@Override
 	public Long loadApplicationIdFromApplicationToken(String applicationToken) throws UsernameNotFoundException{
 		//TEST MODE:
 //		return new Long(1);
 		//END TESTMODE
		Long id = authService.authenticateApplicationToken(applicationToken);
 		if(id == null){
 			throw new UsernameNotFoundException("Could not find a application with that token");
 		}
 		return id;
 	}
 	
 
 }
