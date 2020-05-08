 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.chalmers.fannysangles.friendzone.bb;
 
 import javax.enterprise.context.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  *
  * @author Simon
  */
 @Named("urls")
 @RequestScoped
 public class UrlsBB {
         
     public String getBaseUrl() {
 //        HttpServletRequest origRequest = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 //        origRequest.get
         return "http://localhost:8080/";
     }
     
     public String getProfileUrl(Long id) {
         return this.getBaseUrl() + "user/" + id;
     }
     
    public String getEditProfileUrl(Long id) {
        return this.getBaseUrl() + "user/edit/" + id;
     }
     
     public String getEventPostUrl(Long id){
         return this.getBaseUrl() + "post/" + id;
     }
     
     public String getAddEventPostUrl(){
         return this.getBaseUrl() + "secured/post/add";
     }
     
     public String getIndexUrl(){
         return this.getBaseUrl();
     }
     
     public String getAddUserUrl(){
         return this.getBaseUrl() + "user/add";
     }
     /**
      * Redirect to info page.
      * @return Info page name.
      */
     public String redirectToIndex() {
         return "/index.xhtml?faces-redirect=true";
     }
     
     /**
      * Redirect to info page.
      * @return Info page name.
      */
     public String redirectToEventPost(Long id) {
         return "/view_post.xhtml?id=" + id + "&faces-redirect=true";
     }
     
     public String getLoginUrl(){
         return this.getBaseUrl() + "login";
     }    
     
     public String getRegisterUrl(){
         return this.getBaseUrl() + "register";
     }
     
     public String redirectToLogin(){
         return "/login?faces-redirect=true";
     }
     
 }
