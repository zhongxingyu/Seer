 package controllers;
 
 import controllers.compositions.UserCheck;
 import models.Lead;
 import models.User;
 import org.codehaus.jackson.JsonNode;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.With;
 import tyrex.services.UUID;
import utils.CollectionUtils;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: guym
  * Date: 8/20/13
  * Time: 12:12 PM
  */
 public class LeadsController extends Controller {
 
     @With( UserCheck.class )
     public static Result postLead( String userId , String authToken ){
         User user = ( User) ctx().args.get("user");
         JsonNode postLeadBody = request().body().asJson();
         String email = (String) postLeadBody.get("email").asText();
 
 
         Lead lead = new Lead();
         lead.email = email;
         lead.owner = user;
         lead.uuid = UUID.create();
         lead.validated = false;
         lead.extra = postLeadBody.toString();
         lead.save();
 
          return ok(Json.toJson(lead));
     }
 
 
     @With( UserCheck.class )
     public static Result getLead(  String userId, String authToken, String email  ){
         User user =  (User) ctx().args.get("user");
        Lead lead = CollectionUtils.first(Lead.find.where().eq("email", email).eq("owner", user).findList());
         return ok( Json.toJson(lead));
     }
 
     @With( UserCheck.class )
     public static Result getLeads( String userId, String authToken ){
         User user = ( User ) ctx().args.get("user");
         List<Lead> leads = Lead.find.where().eq("owner", user).findList();
         return ok( Json.toJson( leads ) );
     }
 }
