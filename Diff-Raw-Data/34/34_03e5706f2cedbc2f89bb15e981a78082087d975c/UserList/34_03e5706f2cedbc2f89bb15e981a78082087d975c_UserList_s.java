 package com.lastcrusade.fanclub.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
import com.lastcrusade.fanclub.util.BluetoothUtils;


/*
 * TODO: determine if we want to keep track of an actual
 * username for users as well as the bluetooth id. Right now,
 * username is synonymous with bluetooth id - once we decide
 * if we actually want username, we can add that in to user and 
 * then swtich references to username here to the bluetooth id
 */
 public class UserList {
 
     private List<User> connectedUsers;    
     private UserColors userColors;
 
     public UserList(){
         userColors = new UserColors();
         connectedUsers = new ArrayList<User>();
     }
 
     public UserList(String bluetoothID, String macAddress){
         this();
         connectedUsers.add(new User(bluetoothID, macAddress, userColors.getNextAvailableColor()));
     }
 
     public void addUser(String bluetoothID, String macAddress){
         //check to make sure that the user isn't already in the list before adding
         if(getUserByMACAddress(macAddress)==null){
             connectedUsers.add(new User(bluetoothID, macAddress, userColors.getNextAvailableColor()));
         }
     }
 
     //untested
     public void removeUser(String macAddress){
         int removeIndex = -1;
         for(int i=0; i<connectedUsers.size(); i++){
             if(connectedUsers.get(i).getMacAddress().equals(macAddress)){
                 removeIndex = i;
             }
         }
         if(removeIndex >-1){
             int color = connectedUsers.get(removeIndex).getColor();
             userColors.returnColor(color);
             connectedUsers.remove(removeIndex);
         }
     }
 
     public List<User> getUsers(){
         return connectedUsers;
     }
 
     //get a list of bluetoothIDs of the connected users
     public List<String> getBluetoothIDs(){
         ArrayList<String> bluetoothIDs = new ArrayList<String>();
 
         for(User u:connectedUsers){
             bluetoothIDs.add(u.getBluetoothID());
         }
         
         return bluetoothIDs;
     }
     
     //get a list of macAddresses of the connected users
     public List<String> getMacAddresses(){
         ArrayList<String> macAddresses = new ArrayList<String>();
 
         for(User u:connectedUsers){
             macAddresses.add(u.getMacAddress());
         }
         
         return macAddresses;
     }
     
     //using macAddress instead of bluetooth id to make sure that
     //it is unique
     public User getUserByMACAddress(String macAddress){
         User user = null;
         
         for(User u:connectedUsers){
             if(u.getMacAddress().equals(macAddress)){
                 user = u;
             }
         }
         
         return user;
     }
 }
