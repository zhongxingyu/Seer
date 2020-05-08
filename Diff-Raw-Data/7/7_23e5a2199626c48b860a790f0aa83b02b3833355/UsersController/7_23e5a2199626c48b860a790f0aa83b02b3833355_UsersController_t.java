 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package controllers.modules.admin;
 
 import conf.JSP;
 import controllers.Controller;
 import controllers.modules.ModuleController;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import libs.form.Form;
 import libs.form.fields.EmailField;
 import libs.form.fields.PasswordField;
 import libs.form.fields.SubmitButton;
 import libs.form.fields.TextField;
 import metier.User;
 import models.ArticlesModel;
 import models.UsersModel;
 
 
 public class UsersController extends ModuleController {
     
     public UsersController(Controller parent) {
         super(parent);
     }
 
     @Override
     public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String act = request.getParameter("act");
         
         if(act == null || act.isEmpty()) {
             error("Page inconnue", request, response);
             return;
         }
         
         if(act.equals("index"))
             doIndex(request, response);
         else if(act.equals("edit"))
             doEdit(request, response);
         else if(act.equals("delete"))
             doDelete(request, response);
         else
             error("Page inconnue", request, response);
     }
     
     
     /**
      * Page d'accueil du site. On récupère la liste des derniers articles et on
      * l'affiche.
      *
      * @param request
      * @param response
      * @throws ServletException
      * @throws IOException
      */
     private void doIndex(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         List<User> elems;
         UsersModel mdl = new UsersModel();
 
         try {
             elems = mdl.getAll();
         } catch(SQLException e) {
             error(e.getMessage(), request, response);
             return;
         }
 
         request.setAttribute("elems", elems);
         request.setAttribute("PAGE_TITLE", "Gestion des utilisateurs");
         
         forward(JSP.ADMIN_LIST_USERS, request, response);
     }
 
     private void doEdit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         User u = new User();
         UsersModel mdl = new UsersModel();
 
         // création du formulaire
         Form form = new Form();
         form.add(new TextField("login").setLabel("Identifiant"));
         form.add(new TextField("name").setLabel("Nom").required(false));
         form.add(new TextField("firstName").setLabel("Prénom").required(false));
         form.add(new EmailField("mail").setLabel("Mail").required(false));
         form.add(new PasswordField("pass").setLabel("Mot de passe").required(true));
         form.add(new PasswordField("confirm").setLabel("Confirmation").required(true));
         form.add(new SubmitButton("Envoyer"));
 
         // si on a demandé l'édition d'un utilisateur, on le charge
         if(request.getParameter("id") != null) {
             int id = Integer.parseInt(request.getParameter("id"));
             
             try {
                 u = mdl.get(id);
 
                 if(u == null)
                     throw new Exception("L'utilisateur n'existe pas");
                 
                 form.field("login").setValue(u.getLogin()).readOnly(true);
                 form.field("name").setValue(u.getLastName());
                 form.field("firstName").setValue(u.getFirstName());
                 form.field("mail").setValue(u.getMail());
 
                 form.field("pass").required(false);
                 form.field("confirm").required(false);
             } catch (Exception ex) {
                 redirect("./admin/users/", "Impossible de charger l'utilisateur demandé : "+ex.getMessage(), request, response);
                 return;
             }
         }
 
         // traitement du formulaire
         if(request.getAttribute("HTTP_METHOD").equals("POST")) {
             form.bind(request);
 
             if(form.isValid() && verifPass(form, request)) {
                 boolean wasNew = u.isNew();
 
                 if(wasNew)
                     u.setLogin(request.getParameter("login"));
                 
                 u.setFirstName(request.getParameter("firstName"));
                 u.setLastName(request.getParameter("name"));
                 u.setMail(request.getParameter("mail"));
                 
                 if(request.getParameter("pass") != null && !request.getParameter("pass").isEmpty())
                     u.setPass(request.getParameter("pass"));
                 
                 try {
                     mdl.save(u);
                 } catch (Exception ex) {
                     error("Erreur à la création de l'utilisateur : "+ex.getMessage(), request, response);
                     return;
                 }
 
                 String msg = wasNew ? "Utilisateur enregistré !" : "Utilisateur modifié !";
                 redirect("./admin/users/edit/"+u.getId()+"/", msg, request, response);
             }
         }
 
         // affichage de la page
         request.setAttribute("form", form);
         request.setAttribute("PAGE_TITLE", u.isNew() ? "Nouvel utilisateur" : "Edition d'un utilisateur");
 
         forward(JSP.FORM, request, response);
     }
 
     private void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         int id = -1;
         ArticlesModel mdl = new ArticlesModel();
         
         try {
             id = Integer.parseInt(request.getParameter("id"));
         } catch (NumberFormatException e) {
             id  = -1;
         }
         
         if(id == -1) {
             redirect("./admin/users/", "Requête incorrecte", request, response);
             return;
         }
         
         try {
             mdl.deleteArticle(id);
             redirect("./admin/users/", "Utilisateur supprimé.", request, response);
         } catch (SQLException ex) {
             redirect("./admin/users/", "Erreur à la suppression de l'utilisateur : "+ex.getMessage(), request, response);
         }
     }
 
     private boolean verifPass(Form form, HttpServletRequest request) {
         String pass = request.getParameter("pass");
         String confirm = request.getParameter("confirm");
         
         if(pass != null && !pass.isEmpty())
         {
             if(confirm == null || confirm.isEmpty()) {
                 form.triggerError(form.field("confirm"), "Les mots de passe doivent correspondre");
                 return false;
             }
            
            if(!pass.equals(confirm)) {
                form.triggerError(form.field("confirm"), "Les mots de passe doivent correspondre");
                return false;
            }
         }
 
         return true;
     }
 }
