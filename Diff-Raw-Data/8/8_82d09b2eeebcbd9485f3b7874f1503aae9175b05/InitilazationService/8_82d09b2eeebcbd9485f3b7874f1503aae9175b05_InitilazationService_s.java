 package cz.cvut.fel.bupro.service;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.crypto.password.PasswordEncoder;
 import org.springframework.stereotype.Component;
 
 import cz.cvut.fel.bupro.dao.RoleRepository;
 import cz.cvut.fel.bupro.dao.UserRepository;
 import cz.cvut.fel.bupro.model.Role;
 import cz.cvut.fel.bupro.model.User;
 
 @Component
 public class InitilazationService {
 	private final Log log = LogFactory.getLog(getClass());
 
 	@Autowired
 	private InitDevelService initDevelService;
 	@Autowired
 	private UserRepository userRepository;
 	@Autowired
 	private RoleRepository roleRepository;
 	@Autowired
 	private PasswordEncoder passwordEncoder;
 
 	private void createIfNotPresent(String authority) {
 		Role role = roleRepository.findByAuthority(authority);
 		if (role == null) {
 			log.info("Initializing role " + authority);
 			roleRepository.save(new Role(authority));
 		}
 	}
 
 	private void initializeRoles() {
 		createIfNotPresent(Role.ADMIN);
 		createIfNotPresent(Role.TEACHER);
 		createIfNotPresent(Role.STUDENT);
 	}
 
 	private void initializeAdminAccount() {
 		Role adminRole = roleRepository.findByAuthority(Role.ADMIN);
 		if (adminRole.getUsers().isEmpty()) {
 			log.info("Initializing admin account");
 			User user = new User();
 			user.setUsername("admin");
 			user.setFirstName("Admin");
 			user.setLastName("");
 			user.setPassword(passwordEncoder.encode("admin"));
 			user.setEmail("admin@bupro.org");
 			adminRole.grantTo(user);
 			userRepository.save(user);
 		}
 	}
 
 	@PostConstruct
 	public void initializeApplication() {
 		log.info("Web application bootstrap Initilzation Service - initialize application");
		initializeRoles();
		initializeAdminAccount();
 		//initDevelService.initDevelData();
 	}
 }
