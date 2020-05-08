 package fr.iut.javaee.appshop.web.controller;
 
 import fr.iut.javaee.appshop.commons.Users;
 import fr.iut.javaee.appshop.service.local.UserServiceLocal;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.SessionScoped;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  *
  * @author Alexis
  */
 @ManagedBean(name = "userController")
 @SessionScoped
 public class UserController implements Serializable 
 {
     @EJB
     private UserServiceLocal service;
        
     private Users user;
     private Users managedUser;
     
     @PostConstruct
     public void init()
     {
         user = new Users();  
         managedUser = new Users();       
     }
     
     public List<Users> findAll()
     {
        return service.findAll();
     }
     
     public String editOneById(int id)
     {
         if(user.getUserGroupName().equals("Admin")){
             managedUser = service.findOneById(id);
            return "/admin/users/user.xhtml?faces-redirect=true";
         }
         else if (user.getUserGroupName().equals("Member")) {
             user = service.findOneById(id);
            return "/protected/account.xhtml?faces-redirect=true";            
         }
         
         return null;
     }
     
     public String doLogin() 
     {
         try { 
             ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext(); 
             ((HttpServletRequest) ctx.getRequest()).login(user.getUserUsername(), user.getUserPassword());
             user = service.findOneByUsername(user.getUserUsername());
             if(user.getUserGroupName().contains("Admin")) {
                 ctx.redirect("/AppShopWeb-war/admin/index.xhtml");  
             }
             else {
                 ctx.redirect("/AppShopWeb-war/protected/index.xhtml");
             }  
         } 
         catch (ServletException e) {  
             user.setUserUsername(null);
             user.setUserPassword(null);
         } 
         catch (IOException ex) {
             Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex); 
         }
         return null;  
     }
     
     public void doLogout()
     { 
         try {
             ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();   
             ((HttpServletRequest) ctx.getRequest()).logout();
             user = new Users();
             ctx.redirect("/AppShopWeb-war/index.xhtml?face-redirect=true");
         } catch (IOException ex) {
             Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ServletException ex) {
             Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public String doRegister() 
     {
         service.persist(managedUser);
         managedUser = new Users();
         
         return "/AppShopWeb-war/index.xhtml?faces-redirect=true";
     }
     
     public String add()
     {
         service.persist(managedUser);
         managedUser = new Users();
         
         return "/admin/users/list.xhtml?faces-redirect=true";
     }
     
     public String update(Users u)
     {
         service.persist(u);
         
         if(user.getUserGroupName() != null &&
                 user.getUserGroupName().equals("Admin")) {
             return "/admin/users/list.xhtml?faces-redirect=true";
         }
         return "/protected/index.xhtml?face-redirect=true";
     }
     
     public String remove(Users u)
     {
         service.remove(u);
         managedUser = new Users();
         
         return "/admin/users/list.xhtml?faces-redirect=true";
     }
     
     public Users getUser()
     {
         return user;
     }
     
     public Users getManagedUser()
     {
         return managedUser;
     }
     
     public void setUser(Users user)
     {
         this.user = user;
     }
 }
