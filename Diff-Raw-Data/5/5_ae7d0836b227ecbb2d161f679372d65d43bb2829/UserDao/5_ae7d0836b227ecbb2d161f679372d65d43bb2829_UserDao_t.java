 
 package edu.chl.dat076.foodfeed.model.dao;
 
 import edu.chl.dat076.foodfeed.model.entity.User;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.crypto.password.StandardPasswordEncoder;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 @Repository("userDao")
 public class UserDao extends AbstractDao<User, String> {
     
     @Autowired
     private StandardPasswordEncoder encoder;
     
     public UserDao(){
         super(User.class);
     }
     
     @Override
     public void create(User u){
         u.setPassword(encoder.encode(u.getPassword()));        
        super.create(u);  
     }
 }
