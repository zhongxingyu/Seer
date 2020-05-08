 package com.freakz.hokan_ng.common.service;
 
 import com.freakz.hokan_ng.common.entity.Channel;
 import com.freakz.hokan_ng.common.entity.User;
 import com.freakz.hokan_ng.common.exception.HokanServiceException;
 import com.freakz.hokan_ng.common.rest.IrcEvent;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: petria
  * Date: 11/18/13
  * Time: 8:52 AM
  *
  * @author Petri Airio <petri.j.airio@gmail.com>
  */
 @Service
 @Slf4j
 public class AccessControlServiceServiceImpl implements AccessControlService {
 
   @Autowired
   private UserService userService;
 
   @Override
   public List<User> getMasterUsers() throws HokanServiceException {
     List<User> masterUsers = new ArrayList<>();
     masterUsers.add(userService.findUser("_Pete_"));
     masterUsers.add(userService.findUser("petria"));
    masterUsers.add(userService.findUser("petria2"));
     return masterUsers;
   }
 
   @Override
   public List<User> getChannelOps(Channel channel) throws HokanServiceException {
     List<User> channelOps = new ArrayList<>();
     channelOps.add(userService.findUser("_Pete_"));
     return channelOps;
   }
 
   @Override
   public boolean isChannelOp(IrcEvent ircEvent) {
     return false;  //ToDO
   }
 
   @Override
   public boolean isMasterUser(IrcEvent ircEvent) {
     try {
       for (User user : getMasterUsers()) {
         if (user.getNick().equalsIgnoreCase(ircEvent.getSender())) {
           return true;
         }
       }
     } catch (HokanServiceException e) {
       e.printStackTrace();  //ToDO
     }
     return false;
   }
 }
