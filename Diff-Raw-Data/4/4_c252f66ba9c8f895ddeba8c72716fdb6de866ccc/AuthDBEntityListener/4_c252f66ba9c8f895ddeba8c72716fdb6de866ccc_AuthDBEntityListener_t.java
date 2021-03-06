 /**
 (C) Copyright 2011 CraftFire <dev@craftfire.com>
 Contex <contex@craftfire.com>, Wulfspider <wulfspider@craftfire.com>
 
 This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/
 or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/
 
 package com.authdb.listeners;
 
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityTargetEvent;
 
 import com.authdb.AuthDB;
 import com.authdb.util.Config;
 import com.authdb.util.Messages;
 import com.authdb.util.Util;
 import com.authdb.util.Messages.Message;
 
 public class AuthDBEntityListener extends EntityListener {
 private final AuthDB plugin;
 
 public AuthDBEntityListener(AuthDB instance) {
    this.plugin = instance;
 }
 
 public void onEntityTarget(EntityTargetEvent event) {
   if ((event.getTarget() instanceof Player)) {
       Player player = (Player)event.getTarget();
       if (((event.getEntity() instanceof Monster)) && (event.getTarget() instanceof Player) && plugin.isAuthorized(player) == false) {
           Player p = (Player)event.getTarget();
             if (!checkGuest(p, Config.guests_mobtargeting)) {
                 event.setCancelled(true);
             }
       }
   }
 }
 
 public void onEntityDamage(EntityDamageEvent event) {
         if (event.getEntity() instanceof Player) {
            Player p = (Player)event.getEntity();
            if (this.plugin.AuthDB_AuthTime.containsKey(p.getName())) {
                long timestamp = System.currentTimeMillis()/1000;
                long difference = timestamp - this.plugin.AuthDB_AuthTime.get(p.getName());
                if (difference < 5) {
                    Util.logging.Debug("Time difference: " + difference + ", canceling damage.");
                    event.setCancelled(true);
                }
            }
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;
                if ((e.getDamager() instanceof Animals) || (e.getDamager() instanceof Monster)) {
                    if (event.getEntity() instanceof Player && !checkGuest(p, Config.guests_health)) {
                         event.setCancelled(true);
                    }
                } else if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
                   Player t = (Player)e.getDamager();
                  Util.logging.Debug("I'm here 2");
                  if ((this.plugin.isRegistered("health", p.getName()) == true && plugin.isAuthorized(p) == false) || (!checkGuest(t, Config.guests_pvp) && !checkGuest(p,Config.guests_health))) {
                      Util.logging.Debug("I'm here 3");
                         event.setCancelled(true);
                   }
                } else {
                    if (!checkGuest(p, Config.guests_health)) {
                        event.setCancelled(true);
                      }
                    else if (this.plugin.isRegistered("health", p.getName()) == true && plugin.isAuthorized(p) == false) {
                        event.setCancelled(true);
                    }
                }
            } else {
                if (this.plugin.isRegistered("health", p.getName()) == true && plugin.isAuthorized(p) == false) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if ((event.getEntity() instanceof Animals) || (event.getEntity() instanceof Monster)) {
             if (!(event instanceof EntityDamageByEntityEvent)) { return; }
             EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;
                 if ((e.getDamager() instanceof Player)) {
                 Player t = (Player)e.getDamager();
                   if (!checkGuest(t, Config.guests_mobdamage)) { event.setCancelled(true); }
                 }
         }
     }
 
 boolean checkGuest(Player player,boolean what) {
     if (what) {
         if (this.plugin.isRegistered("checkguest",player.getName()) == false || this.plugin.isRegistered("checkguest",Util.checkOtherName(player.getName())) == false) {
             return true;
         }
     } else if (Config.protection_notify && !AuthDB.isAuthorized(player)) {
         if (!this.plugin.AuthDB_RemindLogin.containsKey(player.getName())) {
              this.plugin.AuthDB_RemindLogin.put(player.getName(), Util.timeStamp() + Config.protection_notify_delay);
              Messages.sendMessage(Message.protection_notauthorized, player, null);
          } else {
              if (this.plugin.AuthDB_RemindLogin.get(player.getName()) < Util.timeStamp()) {
                  Messages.sendMessage(Message.protection_notauthorized, player, null);
                  this.plugin.AuthDB_RemindLogin.put(player.getName(), Util.timeStamp() + Config.protection_notify_delay);
              }
          }
      } else if (this.plugin.isRegistered("checkguest", player.getName()) == true && plugin.isAuthorized(player) == true) {
          if (Config.protection_notify && this.plugin.AuthDB_RemindLogin.containsKey(player.getName())) {  
              this.plugin.AuthDB_RemindLogin.remove(player.getName());
          }
         return true;
      } else {
          if (Config.protection_notify && this.plugin.AuthDB_RemindLogin.containsKey(player.getName())) {  
              this.plugin.AuthDB_RemindLogin.remove(player.getName());
          }
      }
      return false;
     }
 }
