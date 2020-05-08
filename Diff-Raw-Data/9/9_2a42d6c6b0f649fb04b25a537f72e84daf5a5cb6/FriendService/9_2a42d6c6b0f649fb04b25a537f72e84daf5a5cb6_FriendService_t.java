 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.uhsarp.billrive.services;
 
 import com.uhsarp.billrive.dao.GenericDao;
 import com.uhsarp.billrive.domain.Friend;
 import java.util.List;
 import javax.annotation.Resource;
 import org.springframework.stereotype.Service;
 
 
 /**
  *
  * @author uhsarp
  */
 @Service("friendService")
 public class FriendService {
//   @Resource(name= "genericDao")
     GenericDao genericDao;
 
     public List<Friend> getFriends() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     public Friend getFriendById(String friendId_p) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     public Friend createFriend(Friend friend_p) {
         Friend friendObj = null;
 //       friendObj = genericDao.createFriend(friend_p);
        return friendObj;
     }
 
     public Friend updateFriend(Friend friend_p) {
         Friend friendObj=null ;//=genericDao.updateFriendInfo(friend_p);
 //        Friend friendObj = genericDao.updateFriendInfo(friend_p);
         return friendObj;
     }
     
     public void activateFriends(List<Integer> friendIds) {
 //      genericDao.activateFriends(friendIds);
     }
     public void deActivateFriends(List<Integer> friendIds) {
 //      genericDao.deActivateFriends(friendIds);
     }
     public void deleteFriend(String friendId_p) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
 }
