 /*
  * Copyright (c) 2012, Justin Wilcox
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
  * DAMAGE.
  */
 package nl.nitori.Dismantle;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.Material;
 
 public class DismantleMain extends JavaPlugin {
     /*
      * Stores a map between what to break down, and a set of items that it gets
      * broken down into.
      */
     HashMap<Material, MaterialSet> breakDowns;
 
     public void onEnable() {
         populateBreakDowns("plugins/dismantle.cfg");
         getLogger().info("Loaded " + breakDowns.size() + " dismantle recipies");
     }
 
     public void onDisable() {
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String label,
             String[] args) {
         if (cmd.getName().equalsIgnoreCase("dismantle")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("Error: Dismantle must be run by a player");
             }
             Player player = (Player) sender;
             breakDownHand(player);
             return true;
         }
         return false;
     }
 
     private void breakDownHand(Player player) {
         ItemStack hand = player.getItemInHand();
         if (!breakDowns.containsKey(hand.getType())) {
             player.sendMessage("That item can't be dismantled");
             return;
         }
 
         // Some info in case people are trying to abuse a hole in the
         // recipes (like a DSword giving back 3 diamond instead of 2)
         getLogger().info(
                player.getName() + " is dismantling a " + hand.getType()
                         + " with durability " + hand.getDurability());
 
         // What percent of the items should be given. Defaults to 1
         // if the material either can't have a durability, or if the
         // durability is full
         float factor = 1.0f;
         if (hand.getDurability() != 0) {
             short max = hand.getType().getMaxDurability();
             factor = (float) (max - hand.getDurability()) / max;
         }
 
        ItemStack[] broken = breakDowns.get(hand.getType()).getItems(factor);
         player.getInventory().clear(player.getInventory().getHeldItemSlot());
 
         for (ItemStack s : broken) {
             // Give the item stack to the player's inventory
             HashMap<Integer, ItemStack> ret = player.getInventory().addItem(s);
 
             // If it didn't fit, drop it on the ground by the player
             for (Integer i : ret.keySet()) {
                 player.getLocation().getWorld()
                         .dropItem(player.getLocation(), ret.get(i));
             }
         }
     }
 
     private void populateBreakDowns(String fileName) {
         breakDowns = new HashMap<Material, MaterialSet>();
 
         try {
             FileReader reader = new FileReader(fileName);
             BufferedReader buf = new BufferedReader(reader);
 
             String line = buf.readLine();
             int lineCount = 1;
 
             // What is being broken down
             Material base = null;
             // What it breaks into
             MaterialSet set = null;
             while (line != null) {
                 line = line.trim();
 
                 if (line.equals("")) {
                     // Might be ending a recipe
                     if (set != null) {
                         // Commit the current recipe if we have one
                         breakDowns.put(base, set);
                     }
 
                     set = null;
                     base = null;
                 } else if (line.charAt(0) == '#') {
                     // A comment line
                 } else if (set == null) {
                     // Start a new recipe
                     try {
                         base = Material.valueOf(line);
                         set = new MaterialSet();
                     } catch (IllegalArgumentException e) {
                         getLogger().severe(
                                 "Malformed dismantle data file on line "
                                         + lineCount
                                         + ". Was expecting a Material. Got: "
                                         + line);
                     }
                 } else {
                     // Recipe ingredient
                     String[] parts = line.split(" ");
                     if (parts.length != 2) {
                         getLogger().severe(
                                 "Malformed dismantle data file on line "
                                         + lineCount);
                     } else {
                         try {
                             set.addItem(Material.valueOf(parts[0]),
                                     Integer.parseInt(parts[1]));
                         } catch (NumberFormatException e) {
                             getLogger().severe(
                                     "Malformed dismantle data file on line "
                                             + lineCount
                                             + ". Was expecting a number. Got: "
                                             + parts[1]);
                         } catch (IllegalArgumentException e) {
                             getLogger()
                                     .severe("Malformed dismantle data file on line "
                                             + lineCount
                                             + ". Was expecting a Material. Got: "
                                             + parts[0]);
                         }
                     }
                 }
                 lineCount++;
                 line = buf.readLine();
             }
             if (set != null) {
                 // Commit the recipe if we haven't yet
                 breakDowns.put(base, set);
             }
             buf.close();
 
         } catch (FileNotFoundException e1) {
             getLogger().severe(
                     "Could not find dismantle data file: " + fileName);
         } catch (IOException e1) {
             getLogger().severe("Error reading dismantle data file " + fileName);
         }
     }
 
 }
