 package controllers;
 
 import java.util.Date;
 import java.util.List;
 
 import models.Act;
 import models.Actor;
 import models.Author;
 import models.Location;
 import models.LogFile;
 import models.Revision;
 import play.mvc.Controller;
 import play.mvc.With;
 import tools.Utility;
 
 @With(Secure.class)
 public class ActController extends Controller {
 
 	public static void editAct(long actId, String name, long legislatorId,
			long locationId, long actsId) {
 		Act act = Act.findById(actId);
 		// Store old information for log:
 		String original = act.toString();
 
 		act.name = name;
 		if (legislatorId != 0) {
 			Actor newLegislator = Actor.findById(legislatorId);
 			act.legislator = newLegislator;
 		}
 		if (locationId != 0) {
 			Location newLocation = Location.findById(locationId);
 			act.location = newLocation;
 		}
 		act.save();
 
 		LogFile.logActChange(act, original, act.toString(), session
 				.get("username"), new Date());
 
 		flash.success("Text wurde geändert.");
 		Application.index();
 	}
 
 	public static void formAddAct() {
 		render();
 	}
 
 	public static void addAct(String name, long legislatorId, long locationId,
 			String orDate, String comDate) {
 		Act act = new Act(name);
 		act.legislator = Actor.findById(legislatorId);
 		act.location = Location.findById(locationId);
 		List<Author> aut = Author.find("byName", session.get("username"))
 				.fetch();
 		act.author = aut.get(0);
 		act.save();
 
 		Revision rev = new Revision(act, 0, Utility.stringToDate(orDate),
 				Utility.stringToDate(comDate), aut.get(0));
 		rev.save();
 
 		flash.success("Text wurde gespeichert.");
 		Application.index();
 	}
 
 	public static void formEditAct(long actId) {
 		Act act = Act.findById(actId);
 		Actor legislator = act.legislator;
 		long editLegislatorId = 0;
 		if (legislator != null)
 			editLegislatorId = legislator.id;
 		Location location = act.location;
 		long editLocationId = 0;
 		if (location != null)
 			editLocationId = location.id;
 		render(act, editLegislatorId, editLocationId);
 	}
 
 	public static void actList() {
 		List<Act> acts = Act.findAll();
 		render(acts);
 	}
 }
