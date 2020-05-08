 package biz.thaicom.security.services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 import biz.thaicom.eBudgeting.repositories.UserRepository;
 import biz.thaicom.security.models.Group;
 import biz.thaicom.security.models.ThaicomUserDetail;
 import biz.thaicom.security.models.User;
 
 public class ThaicomUserDetailService implements UserDetailsService {
 	private static final Logger logger = LoggerFactory.getLogger(ThaicomUserDetailService.class);
 	
 	
 	@Autowired
 	private UserRepository userRepository;
 	
 	@Override
 	public UserDetails loadUserByUsername(String userName)
 			throws UsernameNotFoundException {
 		logger.debug("loading information userName: " + userName);
 		
 		User user = userRepository.findByUsername(userName);
 		
 		if(user != null) {
 			
 			List<Group> groups = userRepository.findGroupsByUser(user);
 			logger.debug("user.groups size: " + groups.size());
 			
 			user.setGroups(groups);
 			
 			List<GrantedAuthority> AUTHORITIES = new ArrayList<GrantedAuthority>();
 			
 			logger.debug("user.groups size: " + user.getGroups().size());
 			for(Group g : user.getGroups()) {
 				logger.debug(g.getName());
 				AUTHORITIES.add(new SimpleGrantedAuthority(g.getName()));	
 			}
 			
 			
	        AUTHORITIES.add(new SimpleGrantedAuthority("ROLE_USER"));
	        AUTHORITIES.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
	        
 			ThaicomUserDetail userDetail = new ThaicomUserDetail(
 					user.getUsername(), user.getPassword(), AUTHORITIES);
 			
 			userDetail.setWorkAt(user.getPerson().getWorkAt());
 			userDetail.setPerson(user.getPerson());
			if(user.getPerson().getWorkAt().getId() == 7) {
				AUTHORITIES.add(new SimpleGrantedAuthority("ROLE_USER_PLAN"));
			}
 			
 			return userDetail;
 		} else {
 			throw new UsernameNotFoundException(userName);
 		}
 	}
 
 }
