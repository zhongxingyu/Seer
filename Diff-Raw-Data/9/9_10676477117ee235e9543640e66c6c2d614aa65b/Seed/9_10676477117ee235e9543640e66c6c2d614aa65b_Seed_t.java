 package com.edify.db;
 
 import com.edify.config.ApplicationConfig;
 import com.edify.model.Role;
 import com.edify.model.User;
 import com.edify.repositories.RoleRepository;
 import com.edify.repositories.UserRepository;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 
 /**
 * @author <a href="https://github.com/jarias">jarias</a>
  */
 public class Seed {
     public static void main(String[] args) {
         ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
 
         UserRepository userRepository = context.getBean(UserRepository.class);
         RoleRepository roleRepository = context.getBean(RoleRepository.class);
 
         String[] roleAuthorities = {"ROLE_USER", "ROLE_ADMIN"};
 
         for (String authority : roleAuthorities) {
             if (roleRepository.findByAuthority(authority) == null) {
                 Role r = new Role();
                 r.setAuthority(authority);
                 roleRepository.save(r);
             }
         }
 
         if (userRepository.findByUsername("root@admin.local") == null) {
             User user = new User();
             user.setFirstName("Root");
             user.setLastName("Admin");
             user.setUsername("root@admin.local");
             user.setPassword("123");
             user.getRoles().add(roleRepository.findByAuthority("ROLE_USER"));
             user.getRoles().add(roleRepository.findByAuthority("ROLE_ADMIN"));
 
             userRepository.save(user);
         }
     }
 }
