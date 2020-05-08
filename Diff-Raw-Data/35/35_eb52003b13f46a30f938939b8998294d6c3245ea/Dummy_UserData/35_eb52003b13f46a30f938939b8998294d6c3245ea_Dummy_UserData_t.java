 package EMS_Database.impl;
 
 import EMS_Database.DoesNotExistException;
 import EMS_Database.DuplicateInsertionException;
 import EMS_Database.Interface_UserData;
 import EMS_Database.User;
 import java.util.ArrayList;
 
 /**
  *
  * @author Mike Meding
  */
 public class Dummy_UserData implements Interface_UserData {
 
    public ArrayList<User> userList = new ArrayList();
 
     public Dummy_UserData() {
         // temp data to be inserted
         User user1 = new User(1, 1, "Gilmore", "password", "insane@email.com");
         User user2 = new User(2, 1, "Thad", "password", "insaneCrazy@email.com");
         User user3 = new User(3, 1, "Haxzor", "password1", "bestEmail@email.com");
         userList.add(user1);
         userList.add(user2);
         userList.add(user3);
     }
 
     @Override
     public String getNameByUID(int uid) throws DoesNotExistException {
         /**
          * Returns the a User based on a given UID.
          * <p>         
          * @param uid Is the unique id of the user in question.
          */
         
         for (User user : userList) {
             if (user.getUid() == uid) {
                 return user.getName();
             }
         }
         throw new DoesNotExistException("UserData");
     }    
 
     @Override
     public int getUIDByName(String uname) throws DoesNotExistException {
         /**
          * Returns the UID of the specified user if it exists.
          * <p>         
          * @param uname The username of the user being searched for.
          */
         
         for (User user : userList) {
             if (user.getName().equals(uname)) {
                 return user.getUid();
             }
         }
         throw new DoesNotExistException("UserData");
     }
 
     @Override
     public boolean createUser(User user) throws DuplicateInsertionException {
         /**
          * Returns true upon successful creation and addition of a user to dummy
          * database.
          * <p>
          * @param user is of type User which holds all the parameters needed for
          * creation         
          */
         
         // Checks if user already exists in userList
         for(User currUser : userList){
             if(currUser.getUid() == user.getUid())
                 throw new DuplicateInsertionException("UserData");
         }
         
         userList.add(user);
         return true;
     }
 
     @Override
     public String getEmailByUID(int uid) throws DoesNotExistException {
         /**
          * Returns a String of the email of the user with the UID specified
          * <p>         
          * @param uid Is the unique id of the user in question.
          */
         
         for (User user : userList) {
             if (user.getUid() == uid) {
                 return user.getEmail();
             }
         }
         throw new DoesNotExistException("UserData");
     }
 
     @Override
     public String getPwdByUID(int uid) throws DoesNotExistException {
         /**
          * Returns a String of the password of the user with the UID specified
          * <p>         
          * @param uid Is the unique id of the user in question.
          */
         
         for (User user : userList) {
             if (user.getUid() == uid) {
                 return user.getPwd();
             }
         }
         throw new DoesNotExistException("UserData");
     }
 
     @Override
     public int getLevelByUID(int uid) throws DoesNotExistException {
         /**
          * Returns a String of the user privilege level of the user with the UID specified
          * <p>         
          * @param uid Is the unique id of the user in question.
          */
         
         for (User user : userList) {
             if (user.getUid() == uid) {
                 return user.getLevel();
             }
         }
         throw new DoesNotExistException("UserData");
     }
     
 }
