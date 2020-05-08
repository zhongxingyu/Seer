 package controllers;
 
 import org.codehaus.jackson.node.ObjectNode;
 import play.libs.Json;
 import play.libs.WS.Response;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Http.Cookie;
 import play.mvc.Result;
 import views.html.login;
 import play.data.DynamicForm;
 import play.data.Form;
 import plugins.RestedCrowdPlugin;
 
 public class Application extends Controller {
     // Parameters required to send a user along
     // * for <= Referring URL
     // * refId <= Requesting Host Secret (so we aren't abused)
     
     public static Result index() {
         Form<Login> loginForm = new Form<>(Login.class);
         String forUrl = DynamicForm.form().bindFromRequest().get("for");
         Cookie ssoToken = request().cookie(RestedCrowdPlugin.getCookieSettings().getName());
         if (ssoToken != null) {
             // We need to validate this cookie
             if (RestedCrowdPlugin.validCrowdSession(ssoToken.value())) {
                 if (forUrl != null) {
                     return redirect(forUrl);
                 } else {
                     return ok("DISPLAY LIST OF AGMIP SITES");
                 }
             } else {
                response().discardCookie(RestedCrowdPlugin.getCookieSettings().getName());
                 // Add an indicator for the user to know that their session has
                 // expired here.
                 return ok(login.render(loginForm, forUrl));
             }
         } else {
             return ok(login.render(loginForm, forUrl));
         }
     }
 
     public static Result authenticate() {
         Form<Login> loginForm = new Form<>(Login.class).bindFromRequest();
         String forUrl = DynamicForm.form().bindFromRequest().get("for");
         if (loginForm.hasErrors()) {
             return badRequest(login.render(loginForm, forUrl));
         } else {
             return redirect(forUrl);
         }
     }
 
     public static Result logout() {
         Cookie ssoToken = request().cookie(RestedCrowdPlugin.getCookieSettings().getName());
         String forUrl = DynamicForm.form().bindFromRequest().get("for");
         if (ssoToken != null) {
             RestedCrowdPlugin.crowdRequest("session/"+ssoToken.value()).delete().get();
            response().discardCookie(RestedCrowdPlugin.getCookieSettings().getName());
         }
         if (forUrl != null) {
             return redirect(forUrl);
         } else {
             return ok("DISPLAY LIST OF AGMIP SITES");
         }
     }
 
     // REST Endpoints (JSON)
 
     /**
      * Lookup the email address associated with a given SSO token
      * @param token
      * @return email address of current user
      */
     @BodyParser.Of(BodyParser.Json.class)
     public static Result lookupEmail(String token) {
         ObjectNode result = Json.newObject();
         if (token == null || ! RestedCrowdPlugin.validCrowdSession(token)) {
             return forbidden(result);
         }
         String email = RestedCrowdPlugin.getCrowdEmail(token);
         if (email == null) {
             email = "";
         }
         result.put("email", email);
         return ok(result);
     }
 
     /**
      * Lookup the full profile associated with a given SSO token
      *
      * @param token
      * @return Crowd profile of the current user
      */
     @BodyParser.Of(BodyParser.Json.class)
     public static Result lookupProfile(String token) {
         if (token == null || ! RestedCrowdPlugin.validCrowdSession(token)) {
             return forbidden(Json.newObject());
         }
         return ok(RestedCrowdPlugin.crowdRequest("session/"+token).get().get().asJson());
     }
 
     /**
      * Basic class for verifying username and password. Simple for
      * modeling purposes. Based on Zentasks from Play Framework
      * tutorial.
      * 
      * @author Christopher Villalobos
      *
      */
     public static class Login {
         public String username;
         public String password;
         public String token;
         
         public String validate() {
             String authString = "{\"username\":\""+username+"\", "+
             		"\"password\":\""+password+"\", "+
                     "\"validation-factors\":"+RestedCrowdPlugin.VALIDATION_FACTORS+
                     "}"; 
             Response res = RestedCrowdPlugin.crowdRequest("session")
                     .post(authString).get();
             int statusCode = res.getStatus();
             if(statusCode == 201) {
                 // Set the token here, since no other place has knowledge of the
                 // token
                 response().setCookie(RestedCrowdPlugin.getCookieSettings().getName(), 
                         res.asJson().findPath("token").asText(),
                         null, "/", ".agmip.org");
                 return null;
             } else if(statusCode == 400 || statusCode == 403) {
                 return "Invalid username or password:";
             } else {
                 return "We've encountered a server error.";
             }
         }
     }
 }
