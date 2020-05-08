 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
 import java.io.Serializable;
 
 import javax.inject.Named; 
    // or import javax.faces.bean.ManagedBean;
 import javax.enterprise.context.SessionScoped; 
 import netcracker.dao.DAOFactory;
    // or import javax.faces.bean.SessionScoped;
 
 @Named("Login")
 @SessionScoped
 public class Login implements Serializable {
 
     private String login;
     private String password;
     
     //для смены пароля
     private String oldPassword;    
     private String newPassword;
     
     //для проверки
     String newPass;
 
     public String getNewPass() {
         return newPass;
     }
 
     public void setNewPass(String newPass) {
         this.newPass = newPass;
     }
 
     public String getNewPassword() {
         return newPassword;
     }
 
     public void setNewPassword(String newPassword) {
         this.newPassword = newPassword;
     }
 
     public String getOldPassword() {
         return oldPassword;
     }
 
     public void setOldPassword(String oldPassword) {
         this.oldPassword = oldPassword;
     }
 
     public String getLogin() {
         return login;
     }
 
     public void setLogin(String login) {
         this.login = login;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
     
     public String checkLoginRole(int role) {
         
         if(DAOFactory.getEmployeeDAO().checkPassword(login, password))
         {        
             return "index.xhtml";
         }
         else{
             return "login.xhtml";
         }
     }
     
     public String saveNewPassword() {
         
       int employeeId = DAOFactory.getEmployeeDAO().getIdEmployeeByLogin(login);
         
       if(DAOFactory.getEmployeeDAO().changePassword(employeeId, oldPassword, newPassword))
        {        
         newPass="изменен";
         return "../check.xhtml";
        }
        else
            return "doPassword";
          
     }
 }
