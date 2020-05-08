 package example.jsf;
 
 import example.dao.Clinic;
 import example.entities.Owner;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  * @Author Paul Bakker - paul.bakker@luminis.eu
  */
 @Named
 @RequestScoped
 public class NewOwnerController {
     @Inject
     private Clinic clinic;
 
     private Owner owner = new Owner();
 
     public Owner getOwner() {
         return owner;
     }
 
     public void setOwner(Owner owner) {
         this.owner = owner;
     }
    
     public String save() {
         clinic.storeOwner(owner);
 
        return "showowner.xhtml?faces-redirect=true&ownerId=" + owner.getId();
     }
 }
