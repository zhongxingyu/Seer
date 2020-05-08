 package com.xone.service.security;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 import com.xone.model.hibernate.entity.Person;
 import com.xone.model.hibernate.entity.Resources;
 import com.xone.model.hibernate.entity.Roles;
 import com.xone.service.app.PersonService;
 import com.xone.service.app.RolesService;
 import com.xone.service.app.utils.EncryptRef;
 
 public class MyUserDetailServiceImpl implements UserDetailsService {
 	
 	@Autowired
 	private PersonService personService;
 	
 	@Autowired
 	private RolesService rolesService;
 
 	@Override
 	public UserDetails loadUserByUsername(String username)
 			throws UsernameNotFoundException {
//		System.out.println("username is " + username);
         boolean enables = true;  
         boolean accountNonExpired = true;  
         boolean credentialsNonExpired = true;  
         boolean accountNonLocked = true;
         Collection<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();//obtionGrantedAuthorities(person);
 		//超级用户
         if("admin".equals(username)){
             grantedAuths.add(new SimpleGrantedAuthority("CONSOLE_ADMIN"));
 		    User userdetail = new User("admin", EncryptRef.SHA1(String.valueOf("admin")),
 	                enables, accountNonExpired, credentialsNonExpired,
 	                accountNonLocked, grantedAuths);
 		    return userdetail;
 		}
         
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("username", username);
 		Person person = getPersonService().findByMap(params);
 		if (null == person.getId()) {
 			throw new UsernameNotFoundException("用户/密码错误,请重新输入!");
 		}
 		
 
         List<Roles> roles = getRolesService().findAllByUser(person);
         if (null == roles || roles.isEmpty()) {
 			throw new UsernameNotFoundException("权限不足!");
         }
         for (Roles role : roles) {  
         	grantedAuths.add(new SimpleGrantedAuthority(role.getName()));  
         }
 		User userdetail = new User(person.getUsername(), person.getPassword(),
 				enables, accountNonExpired, credentialsNonExpired,
 				accountNonLocked, grantedAuths); 
 		return userdetail;
 	}
 	
 	//取得用户的权限  
     protected Set<GrantedAuthority> obtionGrantedAuthorities(Person person) {  
         Set<GrantedAuthority> authSet = new HashSet<GrantedAuthority>();  
         List<Roles> roles = getRolesService().findAllByUser(person);
         List<Resources> resources = getRolesService().findAllByRoles(roles);
         for(Resources res : resources) {  
 //        	authSet.add(new GrantedAuthorityImpl(res.getName()));  
         	authSet.add(new SimpleGrantedAuthority(res.getName()));  
         }
         return authSet;  
     } 
 
 	public PersonService getPersonService() {
 		return personService;
 	}
 
 	public void setPersonService(PersonService personService) {
 		this.personService = personService;
 	}
 
 	public RolesService getRolesService() {
 		return rolesService;
 	}
 
 	public void setRolesService(RolesService rolesService) {
 		this.rolesService = rolesService;
 	}
 
 }
