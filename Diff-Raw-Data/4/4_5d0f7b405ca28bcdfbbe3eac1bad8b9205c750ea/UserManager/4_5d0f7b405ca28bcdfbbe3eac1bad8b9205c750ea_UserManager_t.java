 package org.zju.electric_factory.service;
 
 import java.util.List;
 
 import org.zju.electric_factory.entity.User;
 
 public interface UserManager {
     
     User getCurrentUser();
 
     void createUser(String username, String email, String password);
 
     List<User> getAllUsers();
 
     User getUser(Long userId);
 
     void deleteUser(Long userId);
 
     void updateUser(User user);
    
    public void createUser(User user);
    
    public User getUserByName(String name);
 }
