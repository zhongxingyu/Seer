 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package controllers.modules;
 
 import conf.JSP;
 import controllers.Controller;
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import libs.form.Form;
 import libs.form.fields.PasswordField;
 import libs.form.fields.SubmitButton;
 import libs.form.fields.TextField;
 import models.SessionModel;
 
 
 public class ConnectionController extends ModuleController {
     
     public ConnectionController(Controller parent) {
         super(parent);
     }
 
     @Override
     public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String action = request.getParameter("action");
         
         if(action == null || action.equals("login"))
             doLogin(request, response);
         else
             doLogout(request, response);
     }
 
     private void doLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         Form form = new Form();
         SessionModel mdl = (SessionModel) request.getAttribute("session");
         
         if(mdl.isLoggedIn()) {
             redirect("./", "Vous êtes déjà connecté", request, response);
             return;
         }
         
         form.add(new TextField("login").setLabel("Identifiant"));
         form.add(new PasswordField("pass").setLabel("Mot de passe"));
         form.add(new SubmitButton("Connexion"));
         
         request.setAttribute("form", form);
         request.setAttribute("PAGE_TITLE", "Connexion");
         
         if(request.getAttribute("HTTP_METHOD").equals("POST")) {
             form.bind(request);
             
             String login = request.getParameter("login");
             String pass = request.getParameter("pass");
             
             boolean auth = false;
             
             try {
                 auth = mdl.authenticate(login, pass, request, response);
             } catch (Exception ex) {
                 error(ex.getMessage(), request, response);
                 return;
             }
             
             if(auth) {
                 redirect("./", "Connexion réussie !", request, response);
                 return;
             }
             
             form.triggerError(form.field("login"), "Identifiants incorrects.");
         }
         
         forward(JSP.FORM, request, response);
     }
 
     private void doLogout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         request.setAttribute("PAGE_TITLE", "Déconnexion");
         
         SessionModel mdl = (SessionModel) request.getAttribute("session");
         
         if(!mdl.isLoggedIn()) {
             redirect("./", "Vous n'êtes pas connecté !", request, response);
             return;
         }
         
         mdl.logout(request, response);
         
         redirect("./", "Déconnexion réussie.", request, response);
     }
 }
