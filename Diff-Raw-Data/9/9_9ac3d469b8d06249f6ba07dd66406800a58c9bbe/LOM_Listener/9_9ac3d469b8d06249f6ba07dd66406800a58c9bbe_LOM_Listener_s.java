 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package io.github.cowang4.leagueofmine;
 
 import java.util.HashMap;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.world.WorldUnloadEvent;
 
 /**
  *
  * @author Greg
  */
 public class LOM_Listener implements Listener{
     
     LeagueOfMine lomineMainClass;
     
     @EventHandler(priority = EventPriority.HIGH)
     public void onTakeDamage(EntityDamageEvent event)
     {
                 
         if(event.getEntity() instanceof Player)
         {
             Player player = (Player)event.getEntity();
             
             if(lomineMainClass.activePlayers.containsKey(player.getName()))
             {
 
                 if(event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))
                 {
                     lomineMainClass.getLogger().info("Someone got Shot!");
 
                 }
                 lomineMainClass.getLogger().info("Found Damage on Entity: " + event.getEntity().toString());
                 lomineMainClass.getLogger().info("Coming from: " + event.getCause().name());
                 lomineMainClass.getLogger().info("Coming from: " + event.getCause().toString());
             }
         }
         
         //i give up for now
     }
     
     @EventHandler(priority = EventPriority.HIGH)
     public void onShotEvent(ProjectileHitEvent event)
     {
         Projectile proj = event.getEntity();
         if (proj.getShooter() instanceof Player){
             Player player = (Player)proj.getShooter();}
         lomineMainClass.getLogger().info("Someone was shot!");
     }
     
     
     @EventHandler(priority = EventPriority.HIGH)
     public void onShootBow(EntityShootBowEvent event)
     {
         if (event.getEntityType().equals(EntityType.PLAYER))
         {
             Player player = (Player)event.getEntity();
             if(lomineMainClass.activePlayers.containsKey(player.getName()))
             {
                 //event.setProjectile();
                 lomineMainClass.getLogger().info(player.getName() + " shot a bow.");//idk what to od for this know
             }
         }
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerDeath(PlayerDeathEvent event)
     {
         Player player = event.getEntity();
         lomineMainClass.getLogger().info(player.getName());
         
         if (lomineMainClass.isLOMEnabled && lomineMainClass.activePlayers.containsKey(event.getEntity().getName()) && lomineMainClass.activePlayers.get(player.getName()).isInGameArena && lomineMainClass.activePlayers.containsKey(event.getEntity().getKiller().getName()))
         {
             //lomineMainClass.getLogger().info("GOOD TO GO!");
             if(lomineMainClass.activePlayers.get(player.getName()).isOnBlueTeam)
             {
                 //lomineMainClass.getLogger().info("GOOD TO GO! BLUEEEEE");
                 Bukkit.getServer().getWorld("world").setSpawnLocation(lomineMainClass.locations.LOM_BlueTeamSpawnPoint.getBlockX(), lomineMainClass.locations.LOM_BlueTeamSpawnPoint.getBlockY(), lomineMainClass.locations.LOM_BlueTeamSpawnPoint.getBlockZ()); 
                 handlePlayerDeath(true, player);
             }
             else if (!lomineMainClass.activePlayers.get(player.getName()).isOnBlueTeam)
             {
                 //lomineMainClass.getLogger().info("GOOD TO GO! REDDDDDD");
                 Bukkit.getServer().getWorld("world").setSpawnLocation(lomineMainClass.locations.LOM_RedTeamSpawnPoint.getBlockX(), lomineMainClass.locations.LOM_RedTeamSpawnPoint.getBlockY(), lomineMainClass.locations.LOM_RedTeamSpawnPoint.getBlockZ()); 
                 handlePlayerDeath(false, player);
             }
         }
         else
         {
             Bukkit.getServer().getWorld("world").setSpawnLocation(lomineMainClass.xspawn, lomineMainClass.yspawn, lomineMainClass.zspawn);
         }
     }
     
     public void setLOM(LeagueOfMine lom)
     {
         lomineMainClass = lom;
     }
     
     public void handlePlayerDeath(boolean BlueIsTrue, Player player)
     {
         Player killer = player.getKiller();
         if (lomineMainClass.activePlayers.get(player.getName()).isOnBlueTeam != lomineMainClass.activePlayers.get(killer.getName()).isOnBlueTeam)
         {
             int playerBounty = lomineMainClass.activePlayers.get(player.getName()).bounty;
             ItemStack iss = new ItemStack(Material.GOLD_NUGGET, playerBounty);
             killer.getInventory().addItem(iss);
             killer.sendMessage("[" + ChatColor.DARK_GREEN + "LOM" + ChatColor.RESET + "]" + ChatColor.GOLD + "Nice job killing " + player.getName() + " you have received " + playerBounty + " gold.");
             
             if(lomineMainClass.activePlayers.get(killer.getName()).bounty < 20){
                 lomineMainClass.activePlayers.get(killer.getName()).bounty = lomineMainClass.activePlayers.get(killer.getName()).bounty + 2;
             }
             
             if(lomineMainClass.activePlayers.get(player.getName()).bounty > 3){
                 lomineMainClass.activePlayers.get(player.getName()).bounty = lomineMainClass.activePlayers.get(player.getName()).bounty - 2;
             }
         }
     }
     
     @EventHandler
     public void onWorldUnload(WorldUnloadEvent ev)
     {
         ev.getWorld().setSpawnLocation(lomineMainClass.xspawn, lomineMainClass.yspawn, lomineMainClass.zspawn);
     }
 }
