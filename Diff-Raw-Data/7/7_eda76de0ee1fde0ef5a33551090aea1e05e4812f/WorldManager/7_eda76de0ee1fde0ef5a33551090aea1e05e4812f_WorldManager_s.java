 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.tehbeard.BeardStat;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import me.tehbeard.utils.syringe.configInjector.InjectConfig;
 import me.tehbeard.utils.syringe.configInjector.YamlConfigInjector;
 import org.bukkit.GameMode;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author James
  */
 public class WorldManager {
    private WorldData defaultWorld = new WorldData();
     Map<String,WorldData> worlds = new HashMap<String, WorldData>();
 
     public WorldManager() {
     }
 
     private static class WorldData {
         
         @InjectConfig("survival")
         private boolean survival;
         @InjectConfig("creative")
         private boolean creative;
         @InjectConfig("adventure")
         private boolean adventure;
 
         public WorldData() {
             survival  = true;
             creative  = false;
             adventure = false;
         }
 
         private WorldData(boolean s, boolean c, boolean a) {
             survival  = s;
             creative  = c;
             adventure = a;
         }
         
         public boolean shouldTrack(Player player){
             GameMode gm = player.getGameMode();
             return 
                     (gm == GameMode.SURVIVAL  && survival) ||
                     (gm == GameMode.CREATIVE  && creative) ||
                     (gm == GameMode.ADVENTURE && adventure);
                     
         }
         
         public boolean isBlackListed(){
             return !(survival || creative || adventure);
         }
     }
     
     
     public WorldManager(ConfigurationSection section){
         Set<String> keys = section.getKeys(false);
         
         for(String key : keys){
             WorldData d = new WorldData();
             new YamlConfigInjector(section.getConfigurationSection(key)).inject(d);
             worlds.put(key,d);
             if(key.equalsIgnoreCase("default")){
                 defaultWorld = d;
             }
         }
     }
     
     public boolean shouldTrack(Player player){
         if(worlds.containsKey(player.getWorld().getName())){
             return worlds.get(player.getWorld().getName()).shouldTrack(player);
         }
             return defaultWorld.shouldTrack(player);
             
     }
     
     public boolean isBlackListed(World world){
         return false;
         /*if(worlds.containsKey(world.getName())){
             return worlds.get(world.getName()).isBlackListed();
         }
             return defaultWorld.isBlackListed();*/
     }
     
     public void addWorld(String name,boolean s,boolean c,boolean a){
         worlds.put(name,new WorldData(s,c,a));
     }
 }
