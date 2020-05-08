 package controllers;
 
 import controllers.securesocial.SecureSocial;
 import models.Horse;
 import models.Player;
 import models.Race;
 import play.data.validation.Required;
 import play.db.jpa.JPABase;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import java.util.List;
 
 import static controllers.PlayerUtil.getCurrentPlayer;
 
 @With(SecureSocial.class)
 public class Races extends Controller {
 
     public static void showAllUpcoming() {
         List<Race> races = Race.findUpcomingRaces();
         Player player = getCurrentPlayer(renderArgs);
         render(races, player);
     }
 
     public static void subscribeHorse(@Required Long raceId, @Required Long horseId) {
         Race race = Race.findById(raceId);
         Horse byId = Horse.findById(horseId);
         race.enter(byId);
         showAllUpcoming();
     }
 }
