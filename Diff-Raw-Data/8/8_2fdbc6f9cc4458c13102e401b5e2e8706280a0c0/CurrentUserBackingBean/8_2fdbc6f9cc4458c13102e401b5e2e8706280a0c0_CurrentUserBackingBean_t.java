 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop_app.model;
 
 import dit126.group4.group4shop.core.Address;
 import dit126.group4.group4shop.core.Users;
 import java.io.Serializable;
 import java.util.List;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  *
  * @author emilbogren
  */
 @Named("currentUserBean")
 @SessionScoped
 public class CurrentUserBackingBean implements Serializable{
     
     @Inject
     private Group4Shop shop;
     
     private String username;  
     
     /*private String firstname;
     private String lastname;
     
     private String street;
     private String streetnr;
     private String postalcode;
     private String region;
     private String country;*/
     private Users currentuser;
     private List<Address> currentAddress;
 
     public void initUser(){
         this.username = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
         this.currentuser = shop.getUserRegister().find(username);
         this.currentAddress = shop.getAddressCatalogue().find(username);
         
         /*this.firstname = currentuser.getFirstName();
         this.lastname = currentuser.getLastName();
         
         this.street = */
     }
     
     public Users getCurrentUser(){
         return this.currentuser;
     }     
     
     public List<Address> getCurrentAddress(){
         return this.currentAddress;
     } 
 }
