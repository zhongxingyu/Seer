 /*
  * Copyright 2013 Dr Daniel R Naylor.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package uk.co.drnaylor.moneysigns;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class PlayerEventHandler implements Listener {
     
     // Player Join - open MoneyUser class for him/her
     @EventHandler
     public void OnPlayerJoin(PlayerJoinEvent event) {
         MoneySigns.addMoneyUser(event.getPlayer());
     }
     
     // Player Quit - close MoneyUser class and save file
     @EventHandler
     public void OnPlayerQuit(PlayerQuitEvent event) {
         MoneyUser a = MoneySigns.getMoneyUser(event.getPlayer());
         a.saveConfig();
         MoneySigns.removeMoneyUser(event.getPlayer());
     }
     
     // PlayerInteractEvent - signs!
     @EventHandler
     public void OnPlayerInteract(PlayerInteractEvent event) {
         
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN) {
                 
                 Sign sign = (Sign)event.getClickedBlock().getState();
                 if (sign.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "[MoneyPrize]")) {
                    event.setCancelled(true); // Cancel block place (hopefully!)
                     MoneyUser mu = MoneySigns.getMoneyUser(event.getPlayer());
                     if (!mu.canGetPrizes()) {
                         event.getPlayer().sendMessage(ChatColor.RED + "You cannot use this sign!");
                         return;
                     }
                     
                     String identifier = sign.getLine(2);
                     if (!MoneySigns.plugin.checkIdentifier(identifier)) {
                         event.getPlayer().sendMessage(ChatColor.RED + "Identifier not found!");
                         return;
                     }
                                       
                     if (mu.canGetPrize(identifier)) {
                         try {
                             mu.claimPrize(identifier, Integer.valueOf(sign.getLine(1)));
                         }
                         catch (Exception e) {
                             e.printStackTrace();
                             event.getPlayer().sendMessage(ChatColor.RED + "An error occured - maybe the sign is malformed?");
                         }
                     } 
                     else {
                         try {
                             long ttl = mu.getTimeToWait(identifier);
                             event.getPlayer().sendMessage(ChatColor.RED + "You must wait for " + Util.toDuration(ttl) + " before you may use that sign again.");
                         }
                         catch (IdentifierException e) {
                             e.printStackTrace();
                             event.getPlayer().sendMessage(ChatColor.RED + "An error occured - maybe the sign is malformed?");
                         }
                     }
                 }
             }
         }
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void OnBlockBreak(BlockBreakEvent event) {
             if (event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN) {
                 MoneyUser mu = MoneySigns.getMoneyUser(event.getPlayer());
                 Sign sign = (Sign)event.getBlock().getState();
                 if (sign.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "[MoneyPrize]")) {
                     if (!mu.canRemoveSign()) {
                         event.setCancelled(true);
                         event.getPlayer().sendMessage(ChatColor.RED + "You cannot remove this sign!");
                     }
                     else {
                         event.getPlayer().sendMessage(ChatColor.GREEN + "You removed a MoneyPrize sign!");
                     }
                 }
             }
     }
     
     @EventHandler(priority = EventPriority.LOW)
     public void onSignChange(SignChangeEvent event){
         if (event.getLine(0).toLowerCase().contains("[moneyprize]")) {
                MoneyUser mu = MoneySigns.getMoneyUser(event.getPlayer());
                if (!mu.canCreateSign()) {
                     event.setCancelled(true);
                     event.getBlock().breakNaturally();
                     event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to create this sign!");
                     return;
                 }
                 
                 String line1 = event.getLine(1);
                 int amount;
                 try {
                     amount = Integer.valueOf(line1);
                 }
                 catch (NumberFormatException e) {
                     event.setCancelled(true);
                     event.getBlock().breakNaturally();
                     event.getPlayer().sendMessage(ChatColor.RED + "The second line must be a positive integer!");
                     return;
                 }
                 
                 if (amount <= 0) {
                     event.setCancelled(true);
                     event.getBlock().breakNaturally();
                     event.getPlayer().sendMessage(ChatColor.RED + "The second line must be a positive integer!");
                     return;
                 }
                 
                 String identifier = event.getLine(2);
                 if (!MoneySigns.plugin.checkIdentifier(identifier.toLowerCase())) {
                     event.setCancelled(true);
                     event.getBlock().breakNaturally();
                     event.getPlayer().sendMessage(ChatColor.RED + "The third line must be a identifier! (add it with /msid set <id> <timeout>)");                    
                     return;
                 }
 
                 event.setLine(0, ChatColor.GREEN + "[MoneyPrize]");
                 event.setLine(2, identifier.toLowerCase());
                 event.getPlayer().sendMessage(ChatColor.RED + "Sign created succesfully!");                    
             }
     }
 }
