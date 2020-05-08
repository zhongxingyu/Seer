 package org.ddelizia.vcrud.core.service.user.impl;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang.StringUtils;
 import org.ddelizia.vcrud.core.service.model.ModelService;
 import org.ddelizia.vcrud.core.service.user.UserService;
 import org.ddelizia.vcrud.model.usermanagement.Domain;
 import org.ddelizia.vcrud.model.usermanagement.Role;
 import org.ddelizia.vcrud.model.usermanagement.User;
 import org.ddelizia.vcrud.model.usermanagement.User_;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.servlet.http.HttpServletRequest;
 import java.lang.reflect.InvocationTargetException;
 import java.security.Principal;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: danilo.delizia
  * Date: 25/06/13
  * Time: 15:31
  * To change this template use File | Settings | File Templates.
  */
@Service("org.ddelizia.vcrud.core.service.user.UserService")
 public class UserServiceImpl implements UserService, UserDetailsService {
 
     @Autowired
     private ModelService modelService;
 
     @Override
     public User vcrudLogIn(String username, String password, Domain domain) {
         Map<String,Object> map = new HashMap<String, Object>();
         map.put(User_.username.getName(),username);
         map.put(User_.password.getName(),password);
 
        //if(domainEnabled!=null && domainEnabled.equals(true)){
            //map.put(User_.domain.getName(),domain);
        //}
 
         return modelService.getModel(map,User.class);
     }
 
     @Override
     @Transactional
     public User registerUser(String username, String email, String password, String password2, Domain domain) {
         if (StringUtils.isNotEmpty(password) && StringUtils.equals(password,password2)){
             User u= new User();
 
             PasswordEncoder passwordEncoder =  new Md5PasswordEncoder();
             u.setUsername(username);
             u.setPassword(passwordEncoder.encodePassword(password,null));
             u.setDomain(domain);
             u.setEmail(email);
             modelService.persist(u);
             return u;
         }
         return null;
     }
 
     @Override
     public User getCurrentUser(HttpServletRequest request) {
         return modelService.getModel(User_.username.getName(), request.getUserPrincipal().getName(),User.class);
     }
 
     @Override
     public User getCurrentSessionUser(){
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         if (auth==null){
             return null;
         }else{
             return getUserByUsernameOrEmail(auth.getName());
         }
     }
 
     @Override
     public User getCurrentUser(Principal principal) {
         return modelService.getModel(User_.username.getName(), principal.getName(),User.class);
     }
 
     @Override
     public User getUserByUsernameOrEmail(String usernameOrEmail) {
         User user = modelService.getModel(User_.username.getName(), usernameOrEmail, User.class);
         if (user==null){
             user = modelService.getModel(User_.email.getName(), usernameOrEmail, User.class);
         }
         return user;
     }
 
     @Override
     @Transactional
     public boolean updateUser(User user) {
         modelService.persist(user);
         return true;
     }
 
     @Override
     public boolean updateUser(User vcrudModel, String key, Object value) {
         try {
             PropertyUtils.setProperty(vcrudModel, key, value);
         } catch (IllegalAccessException e) {
             e.printStackTrace();
             return false;
         } catch (InvocationTargetException e) {
             e.printStackTrace();
             return false;
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     @Override
     public boolean updateUser(User vcrudModel, Map<String, Object> keyValue) {
         try {
             for (String key: keyValue.keySet()){
                 PropertyUtils.setProperty(vcrudModel, key, keyValue.get(key));
             }
         } catch (IllegalAccessException e) {
             e.printStackTrace();
             return false;
         } catch (InvocationTargetException e) {
             e.printStackTrace();
             return false;
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     @Override
     public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
         org.ddelizia.vcrud.model.usermanagement.User vcrudUser = getUserByUsernameOrEmail(s);
         if (vcrudUser == null){
             throw new UsernameNotFoundException("User not found");
         }
         return createUserDetail(vcrudUser);  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private org.springframework.security.core.userdetails.User createUserDetail(org.ddelizia.vcrud.model.usermanagement.User user){
         Role role = user.getRole();
         List<GrantedAuthority> grantedAuthority = new LinkedList<GrantedAuthority>();
         grantedAuthority.add(new SimpleGrantedAuthority(role.getCode()));
 
         Date date = new Date();
         Boolean accountNonExpired=false;
         if (user.getExpriteDateAccount()==null || user.getExpriteDateAccount().before(date)){
             accountNonExpired=true;
         }
         Boolean credentialsNonExpired=false;
         if (user.getExpriteDateCredentials()==null || user.getExpriteDateCredentials().before(date)){
             accountNonExpired=true;
         }
 
         return new org.springframework.security.core.userdetails.User(
                 user.getUsername(),
                 user.getPassword(),
                 user.getEnabled(),
                 accountNonExpired,
                 credentialsNonExpired,
                 user.getAccountNonLocked(),
                 grantedAuthority);
     }
 }
