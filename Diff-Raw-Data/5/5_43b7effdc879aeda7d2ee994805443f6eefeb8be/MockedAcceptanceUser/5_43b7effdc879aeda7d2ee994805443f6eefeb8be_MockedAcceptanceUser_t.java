 package org.exoplatform.acceptance.security;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import lombok.Data;
 import lombok.NoArgsConstructor;
 import lombok.NonNull;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 
 @Data
 @NoArgsConstructor
 public class MockedAcceptanceUser implements AcceptanceUser {
 
   public final static MockedAcceptanceUser USER = new User();
 
   public final static MockedAcceptanceUser ADMIN = new Administrator();
 
   @NonNull
   private String username;
 
   @NonNull
   private String password;
 
   @NonNull
   private String firstName;
 
   @NonNull
   private String lastName;
 
   private boolean accountNonExpired = true;
 
   private boolean accountNonLocked = true;
 
   private boolean credentialsNonExpired = true;
 
   private boolean enabled = true;
 
   @NonNull
   private Collection<GrantedAuthority> authorities;
 
   public String getFullName() {
     return getFirstName() + " " + getLastName();
   }
 
   public static class Administrator extends MockedAcceptanceUser {
    public final static String USERNAME = "admin";
 
     private Administrator() {
       setPassword(USERNAME);
       setUsername(USERNAME);
       setFirstName("Super");
       setLastName("Admin");
       List<GrantedAuthority> grantedAuths = new ArrayList<>();
       grantedAuths.add(new SimpleGrantedAuthority("ROLE_acceptance-administrators"));
       grantedAuths.add(new SimpleGrantedAuthority("ROLE_acceptance-users"));
       setAuthorities(grantedAuths);
     }
   }
 
   public static class User extends MockedAcceptanceUser {
    public final static String USERNAME = "user";
 
     private User() {
       setPassword(USERNAME);
       setUsername(USERNAME);
       setFirstName("Famous");
       setLastName("User");
       List<GrantedAuthority> grantedAuths = new ArrayList<>();
       grantedAuths.add(new SimpleGrantedAuthority("ROLE_acceptance-users"));
       setAuthorities(grantedAuths);
     }
   }
 
 }
