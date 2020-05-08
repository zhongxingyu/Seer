 package is2.service;
 
 import java.util.Collections;
 
 import javax.annotation.PostConstruct;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 import is2.domain.Account;
 import is2.repository.AccountDao;
 
 public class UserService implements UserDetailsService {
 	
 	@Autowired
 	AccountDao accountDao;
 	
 	@PostConstruct	
 	protected void initialize() {
		accountDao.persist(new Account("user", "demo", "ROLE_USER"));
		accountDao.persist(new Account("admin", "admin", "ROLE_ADMIN"));
 	}
 	
 	@Override
 	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
 		Account account = accountDao.findByEmail(username);
 		if(account == null) {
 			throw new UsernameNotFoundException("user not found");
 		}
 		return createUser(account);
 	}
 	
 	public void signin(Account account) {
 		SecurityContextHolder.getContext().setAuthentication(authenticate(account));
 	}
 	
 	private Authentication authenticate(Account account) {
 		return new UsernamePasswordAuthenticationToken(createUser(account), null, Collections.singleton(createAuthority(account)));		
 	}
 	
 	private User createUser(Account account) {
 		return new User(account.getEmail(), account.getPassword(), Collections.singleton(createAuthority(account)));
 	}
 
 	private GrantedAuthority createAuthority(Account account) {
 		return new SimpleGrantedAuthority(account.getRole());
 	}
 
 }
