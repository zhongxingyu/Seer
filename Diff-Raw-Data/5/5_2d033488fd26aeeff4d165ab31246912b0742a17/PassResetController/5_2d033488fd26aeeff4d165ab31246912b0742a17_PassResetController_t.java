 package pl.agh.enrollme.controller;
 
import java.io.Serializable;

 /**
  * Author: Piotr Turek
  */
public class PassResetController implements Serializable {
 
     private String oldPass;
     private String newPass;
     private String newPassConfirm;
 
     public String getOldPass() {
         return oldPass;
     }
 
     public void setOldPass(String oldPass) {
         this.oldPass = oldPass;
     }
 
     public String getNewPass() {
         return newPass;
     }
 
     public void setNewPass(String newPass) {
         this.newPass = newPass;
     }
 
     public String getNewPassConfirm() {
         return newPassConfirm;
     }
 
     public void setNewPassConfirm(String newPassConfirm) {
         this.newPassConfirm = newPassConfirm;
     }
 }
