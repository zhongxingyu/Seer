 
 package models.user;
 
 import java.util.Set;
 
 import models.dbentities.UserModel;
 
 
 /**
  * @author Sander Demeester
  */
 
 public class Organizer extends SuperUser{
 
     protected Organizer(UserModel data, UserType type) {
         super(data, type);
         ROLES.add(Role.MANAGEQUESTIONS);
         ROLES.add(Role.MANAGESERVERS);
         ROLES.add(Role.MANAGECONTESTS);
         ROLES.add(Role.MANAGEUSERS);
         ROLES.add(Role.MIMIC);
     }
 
     public Organizer(UserModel data) {
         this(data, UserType.ORGANIZER);
     }
 
     /**
      * Block user.
      * @param user
      */
     public void blockPupil(User user){
        // TODO So empty, oh so empty - Felix
     }
 
     /**
      * Mimick user.
      * @param user
      */
     public void mimicPupil(Independent user){
        // TODO So empty, oh so empty - Felix
     }
 
     /**
      *
      * @return return a list of all teachers.
      */
     public Set<Teacher> listTeachers(){
        // TODO So empty, oh so empty - Felix
         return null;
     }
 
 }
