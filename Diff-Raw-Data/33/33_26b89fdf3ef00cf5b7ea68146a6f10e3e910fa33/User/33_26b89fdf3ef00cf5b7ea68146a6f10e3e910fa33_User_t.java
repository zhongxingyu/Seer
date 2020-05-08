 
 package models.user;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import models.dbentities.UserModel;
 
 /**
  * The default abstract class for User.
  * @author Sander Demeester, Ruben Taelman
 **/
 
 public abstract class User{
 
 	public UserModel data;
	// The roles this user has or can have
 	protected static Set<Role> ROLES = new HashSet<Role>();// Can be made non-static if roles have to be altered on runtime
 	private UserType type;
 	
     /**
 	 * @param data
 	 */
 	public User(UserModel data, UserType type) {
 		this.data = data;
 		this.type = type;
 	}
 
 	/**
      * Returns info about this user as a String.
      * @return Userinfo.
      */
 	
 	
 	
     public String getUserInfo() {
         return null;
     }
 
     public UserModel getData() {
 		return data;
 	}
 
 	public void setData(UserModel data) {
 		this.data = data;
 	}
 
 	/**
      * Reset the password of this user. Is delegated to the
      * AuthenticationManager.
      */
     public void resetPassword(){
 
     }
 
     /**
      * Logs out the user or pops a mimic.
      */
     public void logout(){
         AuthenticationManager.getInstance().logout();
     }
     
     /*
      * Returns the landing page. Is to be implemented by the child classes
      *@return Landing Page
      */
     //public abstract Content getLandingPage();
     
     /*
      * Returns the userID
      * @return userID
      */
     public String getID(){
     	return data.id;
     }
     
    /**
     * Check if a user has a certain role
     * @param role
     * @return
     */
     public boolean hasRole(Role role) {
         return ROLES.contains(role);
     }
     
    /**
     * Get the type of this user
     * @return
     */
     public UserType getType() {
         return this.type;
     }
     
    /**
     * Check if this user is able to mimic the given user
     * @param user
     * @return
     */
     public boolean canMimic(User user) {
         return this.getType().ordinal() > user.getType().ordinal()
                 && this.hasRole(Role.MIMIC);
     }
 
 }
