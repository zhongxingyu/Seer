 package controllers;
 
 import controllers.Utils.Constantes;
 import controllers.Utils.JsonUtils;
 import models.Personne;
 import models.Utilisateur;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 import play.db.ebean.Model;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 import services.UtilisateurService;
 import views.html.admin.utilisateurs;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Benjamin
  * Date: 01/03/13
  * Time: 14:45
  * To change this template use File | Settings | File Templates.
  */
 public class Admin extends Controller {
 
     @Security.Authenticated(Securite.class)
     static public Result utilisateurs() {
         if( Securite.utilisateur().getRole() != Utilisateur.Role.ADMIN ) {
             return unauthorized();
         }
         return ok(utilisateurs.render());
     }
 
     @Security.Authenticated(SecuriteAPI.class)
     static public Result labelsUtilisateurs() {
         if( Securite.utilisateur().getRole() != Utilisateur.Role.ADMIN ) {
             return unauthorized();
         }
 
         ObjectNode json = JsonUtils.genererReponseJson(JsonUtils.JsonStatut.OK, "Récupération des labels effectuée");
 
         // TODO service?
         ArrayNode auth = new ArrayNode(JsonNodeFactory.instance);
         for( Utilisateur.TYPE_AUTH t: Utilisateur.TYPE_AUTH.values() ) {
             auth.add( t.getIntitule() );
         }
 
         ArrayNode roles = new ArrayNode(JsonNodeFactory.instance);
         for(Personne.Role r: Personne.Role.values()) {
             roles.add( r.getIntitule() );
         }
 
         json.put("services", auth);
         json.put("roles", roles);
 
         // attendu: msg.statut, msg.services, msg.roles
         return ok(json);
     }
 
     @Security.Authenticated(SecuriteAPI.class)
     static public Result listeUtilisateurs() {
         if( SecuriteAPI.utilisateur().getRole() != Utilisateur.Role.ADMIN ) {
             return unauthorized();
         }
 
         // TODO service
         Model.Finder<Long, Utilisateur> finder = new Model.Finder<Long, Utilisateur>(Long.class, Utilisateur.class);
         List<Utilisateur> utilisateurs = finder.all();
 
         ObjectNode json = JsonUtils.genererReponseJson(JsonUtils.JsonStatut.OK, utilisateurs.size() + " utilisateur(s) trouvés.");
         ArrayNode jsonUtilisateurs = new ArrayNode(JsonNodeFactory.instance);
         for( Utilisateur u: utilisateurs ) {
             jsonUtilisateurs.add( u.toJsonMinimal() );
         }
         json.put(Constantes.JSON_UTILISATEURS, jsonUtilisateurs);
 
         // attendu: msg.statut, msg.utilisateurs = [{id, login, service, type, nom, prenom}]
         return ok(json);
     }
 
     @Security.Authenticated(SecuriteAPI.class)
     static public Result infoUtilisateur(Long id) {
         if( SecuriteAPI.utilisateur().getRole() != Utilisateur.Role.ADMIN ) {
             return unauthorized();
         }
 
         // TODO service
         Model.Finder<Long, Utilisateur> finder = new Model.Finder<Long, Utilisateur>(Long.class, Utilisateur.class);
         Utilisateur utilisateur = finder.byId(id);
         if( utilisateur == null ) {
 
             return ok(JsonUtils.genererReponseJson(JsonUtils.JsonStatut.ERREUR, "Aucun utilisateur avec cet id trouvé."));
         }
 
         ObjectNode json = JsonUtils.genererReponseJson(JsonUtils.JsonStatut.OK, "Utilisateur trouvé.");
         json.put(Constantes.JSON_UTILISATEUR, utilisateur.toJsonFull() );
 
         // attendu: msg.statut, msg.utilisateur.{login, nom, prenom, role, mails: [{libellé, email}], telephones: [{libellé, email}]
         return ok(json);
     }
 
     @Security.Authenticated(SecuriteAPI.class)
     static public Result majUtilisateur() {
         if( SecuriteAPI.utilisateur().getRole() != Utilisateur.Role.ADMIN ) {
             return unauthorized();
         }
 
         // TODO service
         Utilisateur nouveau = StaticPages.parseUtilisateur(true);
         if( nouveau == null ) {
             return ok(JsonUtils.genererReponseJson(JsonUtils.JsonStatut.ERREUR, "Arguments manquants."));
         }
 
         Model.Finder<Long, Utilisateur> finder = new Model.Finder<Long, Utilisateur>(Long.class, Utilisateur.class);
         Utilisateur ancien = finder.byId(nouveau.getId());
 
         if( ancien == null ) {
             if( nouveau.getId() == Constantes.JSON_ID_UTILISATEUR_INEXISTANT )
             {
                 // Les nouveaux utilisateurs doivent s'authentifier par login / password
                 ancien = new Utilisateur();
                 ancien.setAuth_service(Utilisateur.TYPE_AUTH.REGULIERE);
             } else {
                 return ok(JsonUtils.genererReponseJson(JsonUtils.JsonStatut.ERREUR, "Utilisateur non trouvé."));
             }
         }
 
         ancien.setRole( nouveau.getRole() );
         ancien.setLogin( nouveau.getLogin() );
 
         if( UtilisateurService.majUtilisateur(ancien, nouveau) ) {
             return ok(JsonUtils.genererReponseJson(JsonUtils.JsonStatut.OK, "Modification effectuée"));
         } else {
             return ok(JsonUtils.genererReponseJson(JsonUtils.JsonStatut.ERREUR, "Erreur interne. Les administrateurs ont été prévenus."));
         }
         // attendu: msg.statut
     }
 
     @Security.Authenticated(SecuriteAPI.class)
     static public Result supprimerUtilisateur(Long id, Boolean supprPersonne) {
         if( SecuriteAPI.utilisateur().getRole() != Utilisateur.Role.ADMIN ) {
             return unauthorized();
         }
 
         // TODO what if... l'admin se supprime lui-même?
 
         // TODO service
         Model.Finder<Long, Utilisateur> finder = new Model.Finder<Long, Utilisateur>(Long.class, Utilisateur.class);
         Utilisateur utilisateur = finder.byId(id);
         if( utilisateur == null ) {
             return ok(JsonUtils.genererReponseJson(JsonUtils.JsonStatut.ERREUR, "Utilisateur non trouvé."));
         } else {
             if(!supprPersonne) {
                 Personne p = utilisateur;
                 p.save(); // TODO vérifier ça!!!!
             }
             utilisateur.delete();
             return ok(JsonUtils.genererReponseJson(JsonUtils.JsonStatut.OK, "Utilisateur supprimé."));
         }
 
         // attendu: msg.statut
     }
 }
