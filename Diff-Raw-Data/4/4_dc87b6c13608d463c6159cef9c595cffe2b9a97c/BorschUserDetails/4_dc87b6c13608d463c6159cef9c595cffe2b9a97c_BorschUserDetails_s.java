 package com.exadel.borsch.web.users;
 
 import com.exadel.borsch.dao.User;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collection;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 
 /**
  * @author zubr
  */
 public class BorschUserDetails implements UserDetails {
     private static final long serialVersionUID = 42L;
     private transient User user;
 
     public BorschUserDetails(User user) {
         this.user = user;
     }
 
     @Override
     public Collection<? extends GrantedAuthority> getAuthorities() {
         List<GrantedAuthority> authorities = new ArrayList<>();
         GrantedAuthority authority = new GrantedAuthority() {
             @Override
             public String getAuthority() {
                 return "ROLE_USER";
             }
         };
         authorities.add(authority);
         return authorities;
     }
 
     @Override
     public String getPassword() {
         return "";
     }
 
     @Override
     public String getUsername() {
         return user.getName();
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
