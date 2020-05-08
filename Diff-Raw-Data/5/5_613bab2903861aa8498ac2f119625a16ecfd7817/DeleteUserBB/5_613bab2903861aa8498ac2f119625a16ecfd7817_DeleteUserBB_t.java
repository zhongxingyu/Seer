 package it.chalmers.fannysangles.friendzone.bb;
 
 
 import it.chalmers.fannysangles.friendzone.model.FriendzoneUser;
 import it.chalmers.fannysangles.friendzone.model.Tag;
 import it.chalmers.fannysangles.friendzone.model.managers.UserManager;
 import java.io.Serializable;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.event.ActionEvent;
 import javax.inject.Named;
 
 /**
  *
  * @author CaptainTec
  */
 @Named("deleteuser")
 @RequestScoped
 public class DeleteUserBB implements Serializable {
 
     @EJB
     private UserManager userManager;
     private FriendzoneUser user;
 
     
     public void actionListenerDelete(ActionEvent e){
         userManager.remove(this.getUser().getId());
     }
     public String actionDelete(){
        return "success";
     }
     
     public String actionCancel(){
        return "success";
     }
     
     public FriendzoneUser getUser(){
         if (user==null){
             user = userManager.getAnonymousUser();
         }
         return user;
     }
     
     public void setUser(FriendzoneUser user){
         this.user=user;
     }
 }
