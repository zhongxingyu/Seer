 package com.mewin.perkShop.shop;
 
 import java.util.Date;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class Perk {
     
     public boolean activated, expired, hasOptions;
     public int id, expiry_length;
     public String name, description, commandA, commandE;
     public Date expireDate;
     
     public Perk(int id, String name, String description, boolean activated, boolean expired, boolean hasOptions, int expiry_length, Date expireDate)
     {
         this.id = id;
         this.name = name;
         this.description = description;
         this.activated = activated;
         this.expired = expired;
         this.hasOptions = hasOptions;
         this.expiry_length = expiry_length;
         this.expireDate = expireDate;
     }
     
     public void activate(Player player)
     {
         Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), replaceParams(commandA, player));
         if (this.canExpire() && this.hasOptions)
         {
             this.expireDate = new Date(System.currentTimeMillis() + (this.expiry_length * 24 * 60 * 60 * 1000));
         }
        this.activated = true;
     }
     
     public void expire(Player player)
     {
         if (commandE != null)
         {
             Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), replaceParams(commandE, player));
         }
         
         expired = true;
     }
     
     private String replaceParams(String command, Player player)
     {
         String cmd = command;
         
         cmd = cmd.replace("{name}", player.getName());
         
         return cmd;
     }
 
     public boolean checkExpired() {
        if (expiry_length <= 0 || expireDate == null)
         {
             return false;
         }
         else
         {
             return expireDate.compareTo(new Date()) <= 0;
         }
     }
     
     public boolean canExpire()
     {
         return hasOptions;
     }
 }
