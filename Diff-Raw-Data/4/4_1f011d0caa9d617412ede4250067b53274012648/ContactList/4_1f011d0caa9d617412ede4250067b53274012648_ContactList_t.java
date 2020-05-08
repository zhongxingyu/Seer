 package com.model;
 
 import java.util.ArrayList;
 
 public class ContactList extends ArrayList<User> {
     public ContactList() {
 
     }
 
     /**
      * Adds a user to this ContactList and returns if
      * it was successfully or not
      *
      * @param user
      * @return an error code CONTACT_EXISTS
      */
     public int addContact(User user) {
         // if found the user with the same properties
         //  return user_already_exists
         // else
         //  add user to this list
         //  add user to the db
        //  return successful
         add(user);
 

         return 0;
     }
 
     /**
      * Remove the given contact
      *
      * @param user
      * @return
      */
     public int removeContact(User user) {
         return 0;
     }
 }
