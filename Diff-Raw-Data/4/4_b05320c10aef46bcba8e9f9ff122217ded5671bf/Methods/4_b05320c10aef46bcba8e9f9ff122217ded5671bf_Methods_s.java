 package com.miners.ironminer.utils;
 
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.util.Filter;
 import org.powerbot.game.api.wrappers.node.SceneObject;
 
 import java.awt.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: coldasice
  * Date: 28/04/13
  * Time: 15:30
  * To change this template use File | Settings | File Templates.
  */
 public class Methods {
 
     public static boolean atMine_Area() {
         return (Vars.MINE_AREA.contains(Players.getLocal().getLocation()));
     }
 
     public static boolean atLodestone_Area() {
         return (Vars.LODESTONE_AREA.contains(Players.getLocal().getLocation()));
     }
 
     public static boolean nextRock(SceneObject Ore) {
         SceneObject nextRock = getSecoundNearest(Ore);
         if (nextRock != null && (Vars.MINE_AREA.contains(nextRock.getLocation())) && rockAlive()
                 && Players.getLocal().getAnimation() == Vars.MINING_ANIMATION
                && (Mouse.getX() != nextRock.getLocation().getX() && Mouse.getY() != nextRock.getLocation().getY())) {
 
             Vars.PAINT_NEXT_ROCK = nextRock;
            Point p = nextRock.getCentralPoint();
             Mouse.move((int) nextRock.getCentralPoint().getX(), (int) nextRock.getCentralPoint().getY());
             return true;
         }
         return false;
     }
 
     public static boolean rockAlive() {
         if (Vars.LAST != null) {
             SceneObject rock = SceneEntities.getAt(Vars.LAST);
             if (rock != null) {
                 for (int aIron_ID : Vars.Iron_ID) {
                     if (rock.getId() == aIron_ID)
                         return true;
                 }
             }
         }
         Vars.LAST = null;
         return false;
     }
 
     public static SceneObject getSecoundNearest(final SceneObject currock) {
 
         return SceneEntities.getNearest(new Filter<SceneObject>() {
             //     @Override
             public boolean accept(SceneObject o) {
                 if (currock != null) {
                     if (currock.equals(o)) {
                         return false;
                     }
                 }
                 for (int id : Vars.Iron_ID)
                     if (o.getId() == id)
                         return true;
                 return false;
             }
         });
     }
 }
 
