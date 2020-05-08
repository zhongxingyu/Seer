 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
import  play.libs.Json.toJson;
 import models.*;
 import views.html.*;
 import com.force.api.*;
 import java.util.Map;
 import java.util.List;
 
 public class TeamManagerController extends Controller {
 
 	//player search form
 	static Form<Player> playerForm = form(Player.class);
 
 	//Present the Form page
 	@With(Authenticated.class)
 	public static Result index() {
 		return ok(subFinderForm.render(playerForm));
 	}
 
 	//Perform the search, present the results
 	@With(Authenticated.class)
 	public static Result getSubs() {
 		Form<Player> filledForm = playerForm.bindFromRequest();
 		List<Player> players = null;	
 		if(filledForm.hasErrors()) {
 			return badRequest(subFinderForm.render(filledForm));
 		} else {
 			Player searchPlayer = filledForm.get();
 			String[] positions = null;
 			String[] nightsAvailable = null;
 			if (searchPlayer.getPositions() != null && !searchPlayer.getPositions().isEmpty()){
 				positions = new String[]{searchPlayer.getPositions()};
 			}
 			if (searchPlayer.getNightsAvailable() != null && !searchPlayer.getNightsAvailable().isEmpty()){
 				nightsAvailable = new String[]{searchPlayer.getNightsAvailable()};
 			}
 			//get the Force API from the context
 			//execute a search
 			players = new PlayerManager((ForceApi)ctx().args.get("api"))
 				.findPlayers(positions, nightsAvailable);
 			//render the response
 			return ok(showPlayerResults.render(players));
 		}
 	}
 
 }
