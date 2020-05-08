 package no.niths.services.auth;
 
 import java.util.List;
 
 import no.niths.common.AppConstants;
 import no.niths.domain.Student;
 import no.niths.domain.security.Role;
 import no.niths.infrastructure.interfaces.StudentRepository;
 import no.niths.security.User;
 import no.niths.services.auth.interfaces.RequestAuthenticationService;
 
 import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Authenticates user trying to request a resource
  */
 @Service
 @Transactional
 public class RequestAuthenticationServiceImpl implements
 		RequestAuthenticationService {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(RequestAuthenticationServiceImpl.class);
 
 	@Autowired
 	private StudentRepository studentRepo;
 
 	@Value("${jasypt.password}")
 	private String decryptionPassword;
 
 	/**
 	 * Fetch student that belongs to session token and returns a user object
 	 * with roles of the student
 	 * 
 	 * @param sessionToken
 	 *            access token
 	 * @return a user object with roles
 	 */
 	@Override
 	public User authenticate(String sessionToken) {
 		logger.debug("User trying to log in with session token: "
 				+ sessionToken);
 		User authenticatedUser = new User(); // ROLE_USER
 
		if (verifySessionToken(sessionToken)) {
 			Student wantAccess = studentRepo
 					.getStudentBySessionToken(sessionToken);
 
 			if (wantAccess != null) { // User with session token found
 				// For access to authenticated users email if any future
 				// implementations needs this.
 				authenticatedUser.setUserName(wantAccess.getEmail());
 				// Checking roles of student and adding them to User wrapper
 				// object
 				List<Role> roles = wantAccess.getRoles();
 				if (!(roles.isEmpty())) {
 					String loggerText = "Student logging in has role(s): ";
 					for (Role role : roles) {
 						loggerText += role.getRoleName() + " ";
 						authenticatedUser.addRoleName(role.getRoleName());
 					}
 					logger.debug(loggerText);
 				}
 			}
 		}
 		return authenticatedUser;
 	}
 
 	// Verifies the session token from the HTTP request
 	private boolean verifySessionToken(String token) {
 		logger.info("Verifying format of token: " + token);
 		StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
 		jasypt.setPassword(decryptionPassword);
 		String decryptedToken = jasypt.decrypt(token);
 
 		logger.debug("Token after decryption: " + decryptedToken);
 		String[] splittet = decryptedToken.split("[|]");
 		if (splittet.length == 3) {
 			try {
 				long issuedAt = Long.parseLong(splittet[2]);
 				if (System.currentTimeMillis() - issuedAt <= AppConstants.SESSION_VALID_TIME) {
 					return true;
 				}
 			} catch (NumberFormatException e) {
 				// Do nothing...
 			}
 		}
 
 		return false; // Not valid token
 	}
 
 	public String getDecryptionPassword() {
 		return decryptionPassword;
 	}
 
 	public void setDecryptionPassword(String decryptionPassword) {
 		this.decryptionPassword = decryptionPassword;
 	}
 
 }
