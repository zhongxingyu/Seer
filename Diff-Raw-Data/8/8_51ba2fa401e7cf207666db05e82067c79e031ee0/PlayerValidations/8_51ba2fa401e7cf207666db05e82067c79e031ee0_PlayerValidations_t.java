 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package validations;
 
 import models.Player;
 
 /**
  *
  * @author Santonas
  */
 public class PlayerValidations {
 
     public static Player trimAtRegistr(Player player) {
 
         player.username = player.username.trim();
         player.password = player.password.trim();
         return player;
     }
 
     public static Player trim(Player player) {
 
 
         player.username = player.username.trim();
         if (player.bio != null) {
             player.bio = player.bio.trim();
         }
         if (player.email != null) {
             player.email = player.email.trim();
         }
         if (player.image != null) {
             player.image = player.image.trim();
         }
 
         return player;
     }
 
     public static String trim(String trim) {
         return trim.trim();
     }
 
     public static String sqlInjection(String toRemove) {
 
         //TODO
 
 
 
         return toRemove;
     }
     
    // Capitalize the first letter and after a whitespace
     public static String capitalize(String s){
        s.trim();
         if (s != null){
             char [] chars = s.toLowerCase().toCharArray();
             chars[0] = Character.toUpperCase(chars[0]);
            for (int i = 0; i < chars.length-1; i++) {
                if(Character.isWhitespace(chars[i]))
                    chars[i+1] = Character.toUpperCase(chars[i+1]);
            }
             return String.valueOf(chars);
         }
         else 
             return null;
     }
 }
