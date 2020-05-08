 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ejb;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Stateful;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import model.User;
 
 /**
  *
  * @author mist
  */
 @Stateful
 public class LoginBean implements LoginBeanLocal {
 
     @PersistenceContext(unitName = "WheelGo-ejbPU")
     private EntityManager em;
     private User user;
 
     public LoginBean() {
     }
 
     @PostConstruct
     private void init() {
        user = (User)em.createNamedQuery("getDefaultUser").getSingleResult();
         if(user == null) {
             throw new IllegalStateException("Neni udany zadny uzivatel!!!");
         }
     }
 
     @Override
     public void setUser(User user) {
         this.user = user;
     }
 
     @Override
     public User getUser() {
         return user;
     }
 
     @Override
     public void login(String username, String password) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 }
