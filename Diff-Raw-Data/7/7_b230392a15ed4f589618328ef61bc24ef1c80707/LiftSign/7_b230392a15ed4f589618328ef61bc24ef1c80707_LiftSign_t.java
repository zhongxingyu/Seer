 /*
  * SignLift Bukkit plugin for Minecraft
  * Copyright (C) 2011 Shannon Wynter (http://fremnet.net/)
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.minecraftserver.improvedsignlifts;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.List;
 import java.util.Vector;
 
 public class LiftSign {
     static private ImprovedSignLift plugin;
 
     public enum Direction {
         UP, DOWN, NONE
     };
 
     private Block        block;
     private Sign         sign;
     private String       label;
     private Direction    direction;
     private String       owner     = "";
     private List<String> members   = new Vector<>();
     private Boolean      isPrivate = false;
 
     static public void init(ImprovedSignLift parent) {
         plugin = parent;
     }
 
     public LiftSign(Sign sign, ImprovedSignLift plugin) {
         this.plugin = plugin;
         this.sign = sign;
         Bukkit.broadcastMessage(this.sign.getLine(1));
         String lineDirection = this.sign.getLine(1);
 
         if (lineDirection.startsWith(plugin.getNormalOpen())
                 && lineDirection.endsWith(plugin.getNormalClose())) {
             this.isPrivate = false;
         } else if (lineDirection.startsWith(plugin.getPrivateOpen())
                 && lineDirection.endsWith(plugin.getPrivateClose())) {
             this.owner = this.sign.getLine(3);
            this.isPrivate = true;
         }
 
         // Remove the prefix and suffix
         lineDirection = lineDirection.substring(1, lineDirection.length() - 1);
 
         if (lineDirection.equalsIgnoreCase(plugin.getLiftString())) {
             this.direction = Direction.NONE;
             this.sign.setLine(
                     1,
                     (isPrivate ? plugin.getPrivateOpen() : plugin.getNormalOpen())
                             + plugin.getLiftString()
                             + (isPrivate ? plugin.getPrivateClose() : plugin.getNormalClose()));
         } else if (lineDirection.equalsIgnoreCase(plugin.getLiftUpString())) {
             this.direction = Direction.UP;
             this.sign.setLine(
                     1,
                     (isPrivate ? plugin.getPrivateOpen() : plugin.getNormalOpen())
                             + plugin.getLiftUpString()
                             + (isPrivate ? plugin.getPrivateClose() : plugin.getNormalClose()));
         } else if (lineDirection.equalsIgnoreCase(plugin.getLiftDownString())) {
             this.direction = Direction.DOWN;
             this.sign.setLine(
                     1,
                     (isPrivate ? plugin.getPrivateOpen() : plugin.getNormalOpen())
                             + plugin.getLiftDownString()
                             + (isPrivate ? plugin.getPrivateClose() : plugin.getNormalClose()));
         }
 
         this.label = this.sign.getLine(0);
     }
 
     public String getLabel() {
         return label;
     }
 
     public boolean checkAllowed(Player player) {
         Bukkit.broadcastMessage("Perm check");
         Location signLocation = this.sign.getLocation();
        // TODO check if lift is private is broken
         Bukkit.broadcastMessage("Private " + (isPrivate) + " Perm (use.normal) "
                 + player.hasPermission("signlift.use.normal"));
         if ((!isPrivate && player.hasPermission("signlift.use.normal")) || player.isOp())
             return true;
         String playerName = player.getName();
         Bukkit.broadcastMessage("Name " + player.getName() + " Perm (use.own) "
                 + player.hasPermission("signlift.use.own") + " Perm (use.own) "
                 + player.hasPermission("signlift.use.other"));
        if (owner.equalsIgnoreCase(plugin.shortPlayerName(playerName))) {
 
             Bukkit.broadcastMessage(player.getName() + " is owner of lift");
             return player.hasPermission("signlift.use.private.own");
         } else if (LiftDataManager.isMemberOfLift(signLocation, owner, player.getName())) {
             Bukkit.broadcastMessage(player.getName() + " is member of lift");
             return player.hasPermission("signlift.use.private.other");
         }
         return false;
     }
 
     public boolean activate(Player player) {
         if (this.direction == Direction.NONE) {
             return false;
         }
 
         LiftSign target = this.findSign();
 
         if (target == null) return false;
 
         if (!(this.checkAllowed(player) && target.checkAllowed(player))) {
             player.sendMessage(plugin.getDeniedLift());
             return false;
         }
         Location loc = player.getLocation();
         String destination = target.getLabel();
         String message;
         if (destination.equals("")) {
             message = this.direction == Direction.UP ? plugin.getDefaultGoingUpString() : plugin
                     .getDefaultGoingDownString();
         } else {
             message = String.format(
                     this.direction == Direction.UP ? plugin.getGoingUpStringFormat() : plugin
                             .getGoingDownStringFormat(), destination);
         }
 
         Block block0 = target.getTargetBlock(loc, 0);
         boolean safe = false;
         if (block0.getY() < 128) {
             Block block1 = target.getTargetBlock(loc, 1);
             loc.setY(block0.getY());
             safe = this.safeBlock(block0) && this.safeBlock(block1);
         }
 
         if (block0.getY() > 0 && !safe) {
             Block block1 = target.getTargetBlock(loc, -1);
             loc.setY(block0.getY() - 1);
             safe = this.safeBlock(block0) && this.safeBlock(block1);
         }
 
         if (safe) {
             player.teleport(loc);
             player.sendMessage(message);
             return true;
         }
 
         return false;
     }
 
     private Block getTargetBlock(Location loc, int offset) {
         boolean sanity = plugin.getSanityCheck();
         int x = sanity ? (int) Math.round(loc.getX()) : loc.getBlockX();
         int y = this.sign.getY() + offset;
         int z = sanity ? (int) Math.round(loc.getZ()) : loc.getBlockZ();
 
         return this.sign.getWorld().getBlockAt(x, y, z);
     }
 
     private Boolean safeBlock(Block block) {
         switch (block.getType()) {
         case AIR:
         case BROWN_MUSHROOM:
         case CROPS:
         case DEAD_BUSH:
         case DIODE_BLOCK_OFF:
         case DIODE_BLOCK_ON:
         case GLOWING_REDSTONE_ORE:
         case LADDER:
         case LEVER:
         case LONG_GRASS:
         case RED_MUSHROOM:
         case RED_ROSE:
         case REDSTONE_ORE:
         case SIGN:
         case SIGN_POST:
         case STATIONARY_WATER:
         case STONE_BUTTON:
         case STONE_PLATE:
         case SUGAR_CANE_BLOCK:
         case TORCH:
         case WALL_SIGN:
         case WATER:
         case WOOD_PLATE:
         case YELLOW_FLOWER:
             return true;
         default:
             return false;
         }
     }
 
     private LiftSign findSign() {
         World world = this.sign.getWorld();
         int x = this.sign.getX(), y = this.sign.getY(), z = this.sign.getZ();
         int d;
         switch (this.direction) {
         case UP:
             d = 1;
             break;
         case DOWN:
             d = -1;
             break;
         default:
             return null;
         }
 
         for (int h = world.getMaxHeight(), y1 = y + d; y1 < h && y1 > 0; y1 += d) {
             Block block = world.getBlockAt(x, y1, z);
             BlockState blockState = block.getState();
             if (blockState instanceof Sign) {
                 String line = ((Sign) blockState).getLine(1);
                 if (line.length() > 2) {
                     line = line.substring(1, line.length() - 1);
                     if (line.equalsIgnoreCase(plugin.getLiftString())
                             || line.equalsIgnoreCase(plugin.getLiftUpString())
                             || line.equalsIgnoreCase(plugin.getLiftDownString())) {
                         return new LiftSign((Sign) blockState, plugin);
                     }
                 }
             }
         }
 
         return null;
     }
 
     public String getOwner() {
         return this.owner;
     }
 
 }
