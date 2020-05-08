 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.*;
 
 /**
  * Give a description of the players surroundings.
  * @author UndeadScythes
  */
 public class WhereCmd extends PlayerCommandExecutor {
     /**
      * @inheritDocs
      */
     @Override
     public void playerExecute(SaveablePlayer player, String[] args) {
        int distance = (int)(Math.sqrt(Math.pow(player.getLocation().getBlockX(), 2) + Math.pow(player.getLocation().getBlockZ(), 2)));
        String message = "You are " + distance + " blocks from spawn";
         if(player.getLocation().getBlockY() < 64) {
             message = message.concat(" " + (64 - player.getLocation().getBlockY()) + " blocks below sea level");
         } else if(player.getLocation().getBlockY() > 64) {
             message = message.concat(" " + (player.getLocation().getBlockY() - 64) + " blocks above sea level");
         } else {
             message = message.concat(" at sea level");
         }
         message = message.concat(" in a " + player.getLocation().getBlock().getBiome().toString().toLowerCase().replace("_", " ") + " biome");
         player.sendMessage(Color.MESSAGE + message);
     }
 }
