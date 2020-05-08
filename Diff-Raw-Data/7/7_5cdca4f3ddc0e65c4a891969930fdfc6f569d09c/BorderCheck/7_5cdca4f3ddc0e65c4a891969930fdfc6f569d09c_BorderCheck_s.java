 package com.vartala.soulofw0lf.rpgapi.borderapi;
 
 import com.vartala.soulofw0lf.rpgapi.RpgAPI;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * Created with IntelliJ IDEA.
  * User: links
  * Date: 6/18/13
  * Time: 5:57 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BorderCheck {
 
 
     public static void cycleCheck(RpgAPI rpgAPI) {
         final JavaPlugin plugin = rpgAPI;
 
         new BukkitRunnable(){
 
             @Override
             public void run() {
                 for (Player player : Bukkit.getOnlinePlayers())
                 {
                     if (player.getLocation().getWorld().getName().equalsIgnoreCase("Kardegah"))
                     {
                         Location loc = player.getLocation();
                         loc.setY(0);
                         Location Center = new Location(Bukkit.getWorld("Kardegah"),0,0,0);
                        if (loc.distance(Center) >= 5090){
                            loc.setX(loc.getX() * -0.985);
                             loc.setY(player.getLocation().getY()+1);
                            loc.setZ(loc.getZ() * -0.985);
                             loc.setPitch(loc.getPitch());
                             loc.setYaw(loc.getYaw());
                             player.teleport(loc);
                         }
                     }
                 }
             }
         }.runTaskTimer(plugin, 60, 60);
     }
 }
