 package com.ne0nx3r0.rareitemhunter.property;
 
 import com.ne0nx3r0.rareitemhunter.RareItemHunter;
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 public class ItemPropertyRepeatingEffect extends ItemProperty
 {
     protected final Map<String,Integer> activePlayers = new HashMap<String,Integer>();
     
     public ItemPropertyRepeatingEffect(ItemPropertyTypes type,String name,String description,int maxLevel)
     {
         super(type, name, description, maxLevel, 0);
     }
     
     public ItemPropertyRepeatingEffect(ItemPropertyTypes type,String name,String description,int maxLevel,int cost)
     {
         super(type, name, description, maxLevel, cost);
     }
     
     @Override
     public void onEquip(Player p,int level)
     {
         activePlayers.put(p.getName(), level);
     }
     
     @Override
     public void onUnequip(Player p, int level)
     {
         activePlayers.remove(p.getName());
     }
     
     public void applyEffectToPlayer(Player p,int level){}
 
     public void createRepeatingAppliedEffect(final ItemPropertyRepeatingEffect property, int duration) 
     {
         Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(RareItemHunter.self, new Runnable()
         {
             @Override
             public void run()
             {
                 for(String sPlayer : activePlayers.keySet())
                 {
                     Player p = Bukkit.getPlayer(sPlayer);
                     
                     if(p != null)
                     {
                         property.applyEffectToPlayer(p,activePlayers.get(p.getName()));
                     }
                     else
                     {
                        activePlayers.remove(p.getName());
                     }
                 }
             }
         }, duration, duration);
     }
     
     public Map<String, Integer> getActivePlayers()
     {
         return this.activePlayers;
     }
 }
