 package ru.dimafeng.springresttest.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import ru.dimafeng.springresttest.model.User;
 import ru.dimafeng.springresttest.repository.UserRepository;
 
 import java.util.*;
 
 @Service
 public class UserServiceImpl implements UserService {
 
     @Autowired
     private UserRepository userRepository;
 
     @Override
     public List<User> getUsers() {
         Iterator<User> iterator = userRepository.findAll().iterator();
         List<User> result = new ArrayList<User>();
         while (iterator.hasNext()) {
             result.add(iterator.next());
         }
 
         return result;
     }
 
     @Override
     public User addUser(User newUser) {
         return userRepository.save(newUser);
     }
 
    @Override
    public void removeUser(long id) {
        userRepository.delete(id);
    }

 }
