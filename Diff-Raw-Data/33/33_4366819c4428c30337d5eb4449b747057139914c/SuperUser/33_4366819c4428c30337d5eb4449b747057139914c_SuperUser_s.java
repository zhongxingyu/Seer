 
 package models.user;
 
 import models.dbentities.UserModel;
 
 /**
  * Abstract superUser class
  * @author Sander Demeester
 **/
 
 public abstract class SuperUser extends Authenticated{
 
     public SuperUser(UserModel data, UserType type) {
         super(data, type);
         ROLES.add(Role.QUESTIONEDITOR);
     }

    /**
     * reset password for user.
     * @param user
     */
    public void resetPassword(User user){
        // TODO I suppose this is not to remain empty? - Felix
    }
 }
