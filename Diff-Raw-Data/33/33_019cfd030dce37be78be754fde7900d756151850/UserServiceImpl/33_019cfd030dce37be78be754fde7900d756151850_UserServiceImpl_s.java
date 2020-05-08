 package com.lucho.service.impl;
 
 import com.lucho.dao.UserDao;
 import com.lucho.domain.User;
 import com.lucho.service.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service("userService")
 public class UserServiceImpl implements UserDetailsService, UserService {
 
     private UserDao userDao;
 
     public UserDao getUserDao() {
         return userDao;
     }
 
     @Autowired
     public void setUserDao(final UserDao userDao) {
         this.userDao = userDao;
     }
 
     @Override
     @Transactional(readOnly = true)
     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
         User user = this.getUser(username);
         if (user == null) {
             throw new UsernameNotFoundException("User " + username + " not found.");
         }
         return user;
     }
 
     @Override
     @Transactional(readOnly = true)
     public User getUser(final Integer id) {
         return this.getUserDao().getUser(id);
     }
 
     @Override
     @Transactional(readOnly = true)
     public User getUser(String username) {
         return this.getUserDao().getUser(username);
     }
 
     @Override
     @Transactional(readOnly = true)
     public boolean userExists(final String username) {
         return this.getUserDao().userExists(username);
     }
 
     @Override
     @Transactional
     public User addUser(final String username, final String password) {
         return this.getUserDao().addUser(username, password);
     }
 
     @Override
     @Transactional
     public void followUser(final User user, final User userToFollow) {
         this.getUserDao().followUser(user, userToFollow);
     }
 
 }
