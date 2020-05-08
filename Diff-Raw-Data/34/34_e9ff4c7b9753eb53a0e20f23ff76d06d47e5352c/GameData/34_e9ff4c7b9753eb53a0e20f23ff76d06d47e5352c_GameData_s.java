 package model;
 
import java.util.Locale;
 import java.util.logging.Logger;
 
 public class GameData {
 
     private static Logger log = Logger.getLogger(GameData.class.getName());
     private String enemy = null;
 
     private GameData(Builder builder) {
         this.enemy = builder.enemy.toLowerCase();
         log.fine(enemy);
     }
 
     private GameData() {
     }
 
     public String getEnemy() {
         return enemy;
     }
 
     @Override
     public String toString() {
         if (enemy != null) {
             return enemy;
         } else {
             return "null enemy";
         }
     }
 
     public static class Builder {
 
         private String enemy = null;
 
         public Builder enemy(String enemy) {
             this.enemy = enemy;
             return this;
         }
 
         public GameData build() {
             return new GameData(this);
         }
     }
 }
