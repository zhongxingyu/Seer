 package org.jrecruiter.web.actions;
 
 import org.apache.struts2.convention.annotation.Result;
 import org.jrecruiter.model.User;
 
 import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
 import com.opensymphony.xwork2.validator.annotations.Validations;
 import com.opensymphony.xwork2.validator.annotations.ValidatorType;
 
 /**
  * Resets the users passwords and emails it back to the user.
  *
  * @author Gunnar Hillert
  * @version $Id:UserService.java 128 2007-07-27 03:55:54Z ghillert $
  */
 @Result(name="success", location="login", type="redirectAction")
 public class GetPasswordAction extends BaseAction {
 
     private User user;
 
     /** serialVersionUID. */
     private static final long serialVersionUID = -3422780336408883930L;
 
     public String execute() {
         return INPUT;
     }
 
     @Validations(
             requiredStrings = {
                         @RequiredStringValidator(type = ValidatorType.SIMPLE, fieldName = "user.username", trim=true, key = "class.get-password.password.required")
                      }
             )
     public String process() {
 
         this.user = userService.getUser(this.user.getUsername());
 
         if (this.user == null) {
             super.addActionError(super.getText("class.get-password.user.not.found"));
             return INPUT;
         }
 
        if (!this.user.isEnabled()) {
            super.addActionError(super.getText("class.get-password.user.account.not.enabled"));
            return INPUT;
        }

         userService.resetPassword(this.user);
 
         super.addActionMessage(super.getText("class.get-password.success", new String[] {user.getEmail()}));
         return SUCCESS;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
 }
