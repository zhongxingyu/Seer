 package org.exoplatform.acceptance.security;
 
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 public class MockedCrowdUserDetailsService implements UserDetailsService {
   @Override
   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
     switch (username) {
       case MockedAcceptanceUser.User.USERNAME:
         return MockedAcceptanceUser.USER;
      case MockedAcceptanceUser.Administrator.USERNAME:
        return MockedAcceptanceUser.ADMIN;
       default:
         throw new UsernameNotFoundException("Unknown user : " + username);
     }
   }
 }
