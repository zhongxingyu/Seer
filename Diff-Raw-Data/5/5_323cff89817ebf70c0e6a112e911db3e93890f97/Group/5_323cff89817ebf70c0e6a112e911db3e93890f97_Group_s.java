 package edu.berkeley.cs.cs162;
 
 import java.util.Calendar;
 import java.util.Set;
 import java.util.HashMap;
 
 public class Group{
     private HashMap<String, BaseUser> users;
     private String name;
 
     public Group(String groupName) {
         this.name = groupName;
         this.users = new HashMap<String, BaseUser>();
     }
 
     public Group(String groupName, int maxNumUsers) {}
 
     /** Add a user object to the group. */	
     public boolean addUser(BaseUser user){
        if (this.isFull() || this.hasUser(user.getName())){
             return false;
         }
        users.put(user.getName(), user);
         TestChatServer.logUserJoinGroup(name, user.getUsername(), Calendar.getInstance().getTime());
         return true;
     }
 	
     /** Remove user with given name from the group. */
     public void removeUser(String userName){
         if (!users.containsKey(userName)){
             return;
         }
         TestChatServer.logUserLeaveGroup(name, userName, Calendar.getInstance().getTime());
         users.remove(userName);
     }
     
     /** Returns Set of all userNames in the group. */
     public Set<String> listUsers(){
         return users.keySet();
     }
 
     /** Returns number of users in the group. */
     public int numUsers(){
         return users.size();
     }
 	
     /** Returns true if group contains user with given userName. */
     public boolean hasUser(String userName) {
         return users.containsKey(userName);
     }
 	
     /** Returns true if group is at maximum capacity. */	
     public boolean isFull(){
         return this.numUsers() >= ChatServer.MAX_GROUP_USERS;
     }
 
     /** Sends the given message to all users in the group. */
     public void messageUsers(Message message) {
         for (BaseUser user: users.values()) {
             user.msgReceived(message.toString());
         }
     }
 }
