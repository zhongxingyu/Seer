 package org.wyona.security.core.api;
 
 import java.util.Date;
 
 import org.wyona.security.core.UserHistory;
 import org.wyona.security.core.ExpiredIdentityException;
 
 /**
  * A user.
  */
 public interface User extends Item {

     /**
      * Gets the email address of this user.
      *
      * @return email address
      * @throws AccessManagementException
      */
     String getEmail() throws AccessManagementException;
 
     /**
      * Sets the email address of this user. The user is not saved automatically.
      *
      * @param email
      * @throws AccessManagementException
      */
     void setEmail(String email) throws AccessManagementException;
 
     /**
      * Gets the description of this user.
      *
      * @return description
      * @throws AccessManagementException
      */
     String getDescription() throws AccessManagementException;
 
     /**
      * Sets the description of this user. The user is not saved automatically.
      *
      * @param description
      * @throws AccessManagementException
      */
     void setDescription(String description) throws AccessManagementException;
 
     /**
      * Sets the password. The parameter may be passed in cleartext, the
      * implementation is reponsible for encrypting the password.
      *
      * @param plainTextPassword
      *            as cleartext
      * @throws AccessManagementException
      */
     void setPassword(String plainTextPassword) throws AccessManagementException;
 
     /**
      * Authenticates this user by verifying the password.
      *
      * @param plainTextPassword
      *            as cleartext
      * @return true if authentication was successful, false otherwise.
      * @throws AccessManagementException
      * @throws ExpiredIdentityException when the identity expiration date is earlier than today
      */
     boolean authenticate(String plainTextPassword) throws ExpiredIdentityException, AccessManagementException;
 
     /**
      * Gets all groups this user is a member of.
      *
      * @return array of groups, empty array if this user is not a member of any group.
      * @throws AccessManagementException
      */
     Group[] getGroups() throws AccessManagementException;
 
     /**
      * Gets all groups this user is a member of and the parents of these groups (and their parents, etc).
      *
      * @param parents If true then also return all parent groups
      * @return array of groups, empty array if this user is not a member of any group.
      * @throws AccessManagementException
      */
     Group[] getGroups(boolean parents) throws AccessManagementException;
 
     /**
      * Get expiration date of this user
      *
      * @return expiration date
      */
     public Date getExpirationDate();
 
     /**
      * Set expiration date of this user
      */
     public void setExpirationDate(Date date);
 
     /**
      * Get history (login/logout etc) of this user
      *
      * @return history
      */
     public UserHistory getHistory();
 
     /**
      * Set history of this user
      */
     public void setHistory(UserHistory history);
 
     /**
      * Get Language of this user
      *
      * @return String language
      */
    public String getLanguage() throws AccessManagementException;
 
     /**
      * Set Language of this user
      * @param String language
      */
    public void setLanguage(String language) throws AccessManagementException;
 }
