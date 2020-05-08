 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import models.*;
 
 import views.html.*;
 import com.force.api.*;
 import java.util.Map;
 import java.util.List;
 
 import static play.libs.Json.toJson;
 
 public class TeamManagerController extends Controller {
 
 	//player search form
 	static Form<Player> playerForm = form(Player.class);
 
 	//TODO this includes search/form fields?
 	@With(Authenticated.class)
 		public static Result index() {
 			return ok(subFinderForm.render(playerForm));
 		}
 
 	//TODO does this result in a "search results" page?
 	@With(Authenticated.class)
 		public static Result getSubs() {
 			Form<Player> filledForm = playerForm.bindFromRequest();
 			List<Player> players = null;	
 			if(filledForm.hasErrors()) {
 				return badRequest(
 						subFinderForm.render(filledForm)
 						);
 			} else {
 				Player searchPlayer = filledForm.get();
 				String[] positions = {searchPlayer.getPositions()};
 				String[] nightsAvailable = {searchPlayer.getNightsAvailable()};
 				players = new PlayerManager((ForceApi)ctx().args.get("api"))
 					.findPlayers(positions, nightsAvailable);
 				//render the response
 				return ok(showPlayerResults.render(players));
 			}
 		}
 
 	//TODO get rid of this after the regular seearch is properly implemented
 	@With(Authenticated.class)
 		public static Result managerTest() {
 			return ok(toJson(PlayerManager.getPitchersOnWednesday()));
 		}
 
 
 
 }
