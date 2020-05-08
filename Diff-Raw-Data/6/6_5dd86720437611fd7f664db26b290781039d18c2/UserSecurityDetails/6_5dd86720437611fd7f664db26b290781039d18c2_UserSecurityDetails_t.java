 package cours.ulaval.glo4003.controller.security;
 
 import java.util.*;
 
 import org.springframework.security.core.*;
 import org.springframework.security.core.authority.*;
 import org.springframework.security.core.userdetails.*;
 
import cours.ulaval.glo4003.domain.*;
 import cours.ulaval.glo4003.domain.User;
 
 public class UserSecurityDetails implements UserDetails {
 
 	private User user;
 
 	private static final long serialVersionUID = 1L;
 
 	public UserSecurityDetails(User user) {
 		this.user = user;
 	}
 
 	@Override
 	public Collection<GrantedAuthority> getAuthorities() {
 		Collection<GrantedAuthority> roleList = new ArrayList<GrantedAuthority>();
		for (Role role : user.getRoles()) {
			roleList.add(new GrantedAuthorityImpl(role.toString()));
		}
 		return roleList;
 	}
 
 	@Override
 	public String getPassword() {
 		if (user == null) {
 			throw new UsernameNotFoundException("Utilisateur non trouvé dans le répertoire.");
 		}
 		return user.getPassword();
 	}
 
 	@Override
 	public String getUsername() {
 		return user.getIdul();
 	}
 
 	@Override
 	public boolean isAccountNonExpired() {
 		return true;
 	}
 
 	@Override
 	public boolean isAccountNonLocked() {
 		return true;
 	}
 
 	@Override
 	public boolean isCredentialsNonExpired() {
 		return true;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return true;
 	}
 
 }
