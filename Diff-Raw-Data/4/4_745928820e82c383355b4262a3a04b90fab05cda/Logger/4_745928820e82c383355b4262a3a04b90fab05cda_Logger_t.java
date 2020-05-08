 package App.logging;
 
 import App.factory.PlanetFactory;
 import App.model.*;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: marky
  * Date: 10/18/12
  * Time: 3:44 PM
  */
 public class Logger {
 
     /**
      * Systematically dumps all game information to the console
      */
     public static void printGameToConsole(){
         List<Player> players = Game.getPlayers();
         Collection<Planet> planets = PlanetFactory.getPlanets().values();
 
         System.out.println("########################################");
         System.out.println("########################################");
         System.out.println("### THROWING GAME INFO INTO CONSOLE  ###");
         System.out.println("########################################");
         System.out.println("########################################");
         System.out.println("########################################");
 
         System.out.println("Number of Players: " + players.size());
         int playerCounter = 0;
         for (Player player : players){
             playerCounter++;
             System.out.println("\n");
             System.out.println("Player " + playerCounter + ":");
             printPlayerInfo(player);
             System.out.print("\n");
         }
 
         System.out.println("Current Player: " + Game.getCurrentPlayer().getName());
         System.out.print("\n");
         System.out.println("## Planets ##");
         System.out.print("Number of Planets: " + planets.size());
         System.out.print("\n");
         for (Object planet : planets){
            printPlanetInfo((Planet) planet);
         }
     }
 
     /*
      * Helper Methods for the console dump
      */
 
     public static void printPlayerInfo(Player player){
         System.out.println("Name: " + player.getName());
         System.out.println("Money: " + player.getMoney());
         System.out.println("Skills: ");
         printPlayerSkills(player.getSkillLevels());
         printShipInfo(player.getShip());
     }
 
     public static void printPlayerSkills(Map<SkillType, Integer> skills){
         for (SkillType skillType : SkillType.values()){
             System.out.println("\t" + skillType.toString() + " = " + skills.get(skillType));
         }
     }
 
     public static void printShipInfo(Ship ship){
         System.out.println("Ship: " + ship.getName().toString());
     }
 
     public static void printSettingsInfo(){
         // we'll worry about this later
     }
 
     public static void printPlanetInfo(Planet planet){
         System.out.println("Name: " + planet.getName() + "; xDimension: " + planet.getX() + "; yDimension: " + planet.getY());
         System.out.println("Tech level: " + planet.getTechnologyLevel());
        System.out.println("Resource level: " + planet.getResourceType());
        System.out.println("Political level: " + planet.getPoliticalSystem());
     }
 }
