 package com.codisimus.plugins.pvpreward;
 
 import java.util.LinkedList;
 import java.util.List;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 /**
  * A Record contains a Player's name, number of kills/deaths and karma level
  * A Record also keeps track of who the Player is in combat with and where they died
  * 
  * @author Codisimus
  */
 public class Record implements Comparable {
     public static int combatTimeOut;
     public static int graveTimeOut;
     public static String graveRob;
     public String name;
     public int kills = 0;
     public int deaths = 0;
     public int karma = 0;
     public double kdr = 0;
     public boolean inCombat = false;
     public String inCombatWith;
     public Location signLocation;
     public Sign tombstone;
     public LinkedList<ItemStack> grave = new LinkedList<ItemStack>();
     private int instance = 0;
 
     /**
      * Constructs a new Record for the given Player
      *
      * @param name The name of the Player on Record
      * @return The newly created Record
      */
     public Record(String name) {
         this.name = name;
     }
 
     /**
      * Constructs a new Record and sets the kdr/karma
      * 
      * @param name The name of the player on Record
      * @param kills The amount of other Players the Player killed
      * @param deaths The amount of times this Player was killed
      * @param karma The current karma level of the Player
      * @return The newly created Record
      */
     public Record(String name, int kills, int deaths, int karma) {
         this.name = name;
         this.kills = kills;
         this.deaths = deaths;
         this.karma = karma;
         
         if (deaths == 0)
             kdr = kills;
         else
             kdr = (double)kills / deaths;
         
         long temp = (long)(kdr * 100);
         kdr = (double)temp / 100;
     }
     
     /**
      * Adds a kill to the Record and 2 karma points
      * 
      * @return The new kdr
      */
     public double addKill() {
         kills++;
         karma = karma + 2;
         
         if (deaths == 0)
             kdr = kills;
         else
             kdr = (double)kills / deaths;
         
         long temp = (long)(kdr * 100);
         kdr = (double)temp / 100;
         
         PvPReward.save();
         return kdr;
     }
 
     /**
      * Adds a death to the Record and subtracts a karma point
      * 
      * @return The new kdr
      */
     public double addDeath() {
         deaths++;
         karma--;
         
         if (karma < 0)
             karma = 0;
         
         if (kills == 0)
             kdr = 1 / deaths;
         else
             kdr = (double)kills / deaths;
         
         long temp = (long)(kdr * 100);
         kdr = (double)temp / 100;
         
         PvPReward.save();
         return kdr;
     }
 
     /**
      * Sets the Record as in combat with the given Player
      * The combat values reset if a new instance is not made after the combatTimeOut
      * 
      * @return The new kdr
      */
     public void startCombat(String player) {
         instance++;
         final int THIS_INSTANCE = instance;
         inCombat = true;
         inCombatWith = player;
         
         //Start a new thread
         Thread combat = new Thread() {
             @Override
             public void run() {
                 try {
                     //Leave Record in combat for the given time
                     Thread.currentThread().sleep(combatTimeOut);
                     
                     //Check if combat was started again
                     if (instance == THIS_INSTANCE)
                         resetCombat();
                 }
                 catch (Exception e) {
                 }
             }
         };
         combat.start();
     }
 
     /**
      * Resets the combat values of the Record
      *
      */
     public void resetCombat() {
         inCombat = false;
         inCombatWith = null;
     }
 
     /**
      * Creates a tombstone for time interval graveTimeOut
      * 
      * @param dropped A List of items that were dropped
      */
     public void digGrave(List<ItemStack> dropped, Block lastKnown) {
         grave.clear();
         
         for (int i = 0; i < dropped.size(); i++)
             if (dropped.get(i) != null) {
                 grave.add(dropped.get(i));
                 dropped.remove(i);
             }
         
         if (grave.isEmpty())
             return;
         
         if (signLocation != null)
             signLocation.getBlock().setTypeId(0);
         
         while (lastKnown.getTypeId() != 0)
             lastKnown = lastKnown.getRelative(BlockFace.UP);
         
         lastKnown.setType(Material.SIGN_POST);
         signLocation = lastKnown.getLocation();
         tombstone = (Sign)lastKnown.getState();
         tombstone.setLine(1, "Here Lies");
         tombstone.setLine(2, name);
         
         //Start a new thread
         Thread dig = new Thread() {
             @Override
             public void run() {
                 try {
                     Thread.currentThread().sleep(graveTimeOut);
                     signLocation.getBlock().setTypeId(0);
                     signLocation = null;
                 }
                 catch (Exception e) {
                 }
             }
         };
         dig.start();
     }
     
     /**
      * Adds items from grave into Player's Inventory
      * Also removes tombstone
      * 
      * @param graveRobber The Player taking the items
      */
     public void robGrave(Player graveRobber) {
         PlayerInventory sack = graveRobber.getInventory();
         
         for (ItemStack item: grave)
             sack.addItem(item);
         
         graveRobber.sendMessage(graveRob);
         signLocation.getBlock().setTypeId(0);
         signLocation = null;
     }
 
     /**
      * Compares the KDRs of this Record and the given Record
      * 
      * @param obj The Record being compared to this one
     * @return 1, 0, or -1 if this Record's KDR is less, equal or greater respectively
      */
     @Override
     public int compareTo(Object obj) {
         //Records are compared by their kdr value
         double objKDR = ((Record)obj).kdr;
         
         if (kdr < objKDR)
             return 1;
        else if (kdr > objKDR)
            return -1;
         else
             return 0;
     }
 }
