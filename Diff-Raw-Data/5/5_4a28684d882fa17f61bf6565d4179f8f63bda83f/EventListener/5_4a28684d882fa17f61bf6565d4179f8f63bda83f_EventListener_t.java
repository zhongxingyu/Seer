 /*
  * EventListener.java
  * 
  * Copyright (c) 2012 Lolmewn <info@lolmewn.nl>. 
  * 
  * Sortal is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Sortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Sortal.  If not, see <http ://www.gnu.org/licenses/>.
  */
 package nl.lolmewn.sortal;
 
 import java.util.logging.Level;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 /**
  *
  * @author Lolmewn <info@lolmewn.nl>
  */
 public class EventListener implements Listener {
 
     private Main plugin;
 
     private Main getPlugin() {
         return this.plugin;
     }
 
     private Localisation getLocalisation() {
         return this.getPlugin().getSettings().getLocalisation();
     }
 
     public EventListener(Main main) {
         this.plugin = main;
     }
 
     @EventHandler
     public void onSignChange(SignChangeEvent event) {
         for (int i = 0; i < event.getLines().length; i++) {
             if (this.getPlugin().getSettings().isDebug()) {
                 this.getPlugin().getLogger().log(Level.INFO, String.format("[Debug] Checking line "
                         + "%s of the sign", i));
             }
             if (event.getLine(i).toLowerCase().contains("[sortal]")
                     || event.getLine(i).toLowerCase().contains(this.getPlugin().getSettings().getSignContains())) {
                 if (!event.getPlayer().hasPermission("sortal.placesign")) {
                     event.getPlayer().sendMessage(this.getLocalisation().getNoPerms());
                     event.setCancelled(true);
                     return;
                 }
             }
         }
     }
 
     @EventHandler
     public void onPlayerHitSign(PlayerInteractEvent event) {
         if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
             Block b = event.getClickedBlock();
             Player p = event.getPlayer();
             if (b.getType().equals(Material.SIGN) || b.getType().equals(Material.SIGN_POST)
                     || b.getType().equals(Material.WALL_SIGN)) {
                 this.getPlugin().debug("Sign has been clicked");
                 //It's a sign
                 Sign s = (Sign) b.getState();
                 if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                     if (sortalSign(s, p)) {
                         event.setCancelled(true);
                     }
                     return;
                 }
                 int found = -1;
                 for (int i = 0; i < s.getLines().length; i++) {
                     if (s.getLine(i).toLowerCase().contains("[sortal]")
                             || s.getLine(i).toLowerCase().contains(this.getPlugin().getSettings().getSignContains())) {
                         //It's a sortal sign
                         found = i;
                         break;
                     }
                 }
                 if (found == -1) {
                     if (!this.getPlugin().getWarpManager().hasSignInfo(s.getLocation())) {
                         return; //nvm it's not a sortal sign of any kind.
                     }
                     if (!p.hasPermission("sortal.warp")) {
                         p.sendMessage(this.getLocalisation().getNoPerms());
                         event.setCancelled(true);
                         return;
                     }
                     SignInfo sign = this.getPlugin().getWarpManager().getSign(s.getLocation());
                     if (sign.hasWarp()) {
                         this.getPlugin().debug(String.format("[Debug] Sign clicked has warp: %s", sign.getWarp()));
                         if (this.getPlugin().getSettings().isPerWarpPerm()) {
                             this.getPlugin().debug("[Debug] WarpPerPerm = true");
                             if (!p.hasPermission("sortal.warp." + sign.getWarp())) {
                                 this.getPlugin().debug(String.format("[Debug] No perms! Needed: sortal.warp.%s", sign.getWarp()));
                                 p.sendMessage(this.getLocalisation().getNoPerms());
                                 event.setCancelled(true);
                                 return;
                             }
                         }
                         if (!isPrivateUser(sign, p)) {
                             p.sendMessage(this.getLocalisation().getIsPrivateSign());
                             event.setCancelled(true);
                             return;
                         }
                         Warp w = this.getPlugin().getWarpManager().getWarp(sign.getWarp());
                         if (w == null) {
                             this.getPlugin().debug("[Debug] Warp w == null, cancelling");
                             p.sendMessage(this.getLocalisation().getErrorInSign());
                             event.setCancelled(true);
                             return;
                         }
                         if (!canPay(w, sign, p)) {
                             this.getPlugin().debug(String.format("[Debug] Can't pay: %s", getPrice(w, sign)));
                             p.sendMessage(this.getLocalisation().getNoMoney(Integer.toString(getPrice(w, sign))));
                             event.setCancelled(true);
                             return;
                         }
                         if (!usesCheck(w, sign, p)) {
                             p.sendMessage(this.getLocalisation().getMaxUsesReached());
                             event.setCancelled(true);
                             return;
                         }
                         this.getPlugin().debug(String.format("[Debug] Player is paying.."));
                         this.getPlugin().pay(p, this.getPrice(w, sign));
                         Location loc = w.getLocation();
                        if(!loc.getChunk().isLoaded()){loc.getChunk().load();}
                         if (loc.getYaw() == 0 && loc.getPitch() == 0) {
                             loc.setYaw(p.getLocation().getYaw());
                             loc.setPitch(p.getLocation().getPitch());
                         }
                         p.teleport(w.getLocation(), TeleportCause.PLUGIN);
                         this.getPlugin().debug(String.format("[Debug] Teleported to: %s", w.getName()));
                         p.sendMessage(this.getLocalisation().getPlayerTeleported(w.getName()));
                         event.setCancelled(true); //Cancel, don't place block.
                         return;
                     }
                     this.getPlugin().debug(String.format("[Debug] Error in sign!"));
                     p.sendMessage(this.getLocalisation().getErrorInSign()); //Sign does have something but no warp -> weird.
                     event.setCancelled(true);
                     return; //have to return, otherwise it'll check the next lines
                 }
                 if (!p.hasPermission("sortal.warp")) {
                     this.getPlugin().debug("No perms - need sortal.warp");
                     p.sendMessage(this.getLocalisation().getNoPerms());
                     event.setCancelled(true);
                     return;
                 }
                 String nextLine = s.getLine(found + 1);
                 if (nextLine == null || nextLine.equals("")) {
                     //Well, that didn't really work out well..
                     this.getPlugin().debug("nextLine == null || \"\" ");
                     p.sendMessage(this.getLocalisation().getErrorInSign());
                     event.setCancelled(true);
                     return;
                 }
                 if (nextLine.contains("w:")) {
                     //It's a warp
                     String warp = nextLine.split(":")[1];
                     if (this.getPlugin().getSettings().isPerWarpPerm()) {
                         if (!p.hasPermission("sortal.warp." + warp)) {
                             this.getPlugin().debug("No perms - need sortal.warp." + warp);
                             p.sendMessage(this.getLocalisation().getNoPerms());
                             event.setCancelled(true);
                             return;
                         }
                     }
                     if (!this.getPlugin().getWarpManager().hasWarp(warp)) {
                         this.getPlugin().debug("Warp not found!");
                         p.sendMessage(this.getLocalisation().getWarpNotFound(warp));
                         event.setCancelled(true);
                         return;
                     }
                     Warp w = this.getPlugin().getWarpManager().getWarp(warp);
                     SignInfo sign = this.getPlugin().getWarpManager().getSign(s.getLocation());
                     if (sign != null) {
                         if (!isPrivateUser(sign, p)) {
                             p.sendMessage(this.getLocalisation().getIsPrivateSign());
                             event.setCancelled(true);
                             return;
                         }
                     }
                     if (!canPay(w, sign, p)) {
                         p.sendMessage(this.getLocalisation().getNoMoney(Integer.toString(getPrice(w, sign))));
                         event.setCancelled(true);
                         return;
                     }
                     if (!usesCheck(w, sign, p)) {
                         p.sendMessage(this.getLocalisation().getMaxUsesReached());
                         event.setCancelled(true);
                         return;
                     }
                    if(!w.getLocation().getChunk().isLoaded()){w.getLocation().getChunk().load();}
                     this.getPlugin().pay(p, this.getPrice(w, sign));
                     p.teleport(w.getLocation(), TeleportCause.PLUGIN);
                     p.sendMessage(this.getLocalisation().getPlayerTeleported(warp));
                     event.setCancelled(true);
                     return;
                 }
                 if (nextLine.contains(",")) {
                     String[] split = nextLine.split(",");
                     World w;
                     int add = 0;
                     if (split.length == 3) {
                         w = p.getWorld();
                     } else {
                         w = this.getPlugin().getServer().getWorld(split[0]);
                         if (w == null) {
                             p.sendMessage(this.getLocalisation().getErrorInSign());
                             event.setCancelled(true);
                             return;
                         }
                         add = 1;
                     }
                     SignInfo sign = this.getPlugin().getWarpManager().getSign(s.getLocation());
                     if (sign != null) {
                         if (!isPrivateUser(sign, p)) {
                             p.sendMessage(this.getLocalisation().getIsPrivateSign());
                             event.setCancelled(true);
                             return;
                         }
                     }
                     if (!canPay(null, sign, p)) {
                         p.sendMessage(this.getLocalisation().getNoMoney(Integer.toString(getPrice(null, sign))));
                         event.setCancelled(true);
                         return;
                     }
                     if (!usesCheck(null, sign, p)) {
                         p.sendMessage(this.getLocalisation().getMaxUsesReached());
                         event.setCancelled(true);
                         return;
                     }
                     this.getPlugin().pay(p, this.getPrice(null, sign));
                     int x = Integer.parseInt(split[0 + add]), y = Integer.parseInt(split[1 + add]),
                             z = Integer.parseInt(split[2 + add]);
                     Location dest = new Location(w, x, y, z, p.getLocation().getYaw(), p.getLocation().getPitch());
                    if(!dest.getChunk().isLoaded()){dest.getChunk().load();}
                     p.teleport(dest, TeleportCause.PLUGIN);
                     p.sendMessage(this.getLocalisation().getPlayerTeleported(
                             dest.getBlockX() + ", " + dest.getBlockY() + ", " + dest.getBlockZ()));
                     event.setCancelled(true);
                     return;
                 }
                 p.sendMessage(this.getLocalisation().getErrorInSign());
                 event.setCancelled(true);
             }
         }
     }
 
     private boolean sortalSign(Sign s, Player player) {
         //checks whether
         if (this.getPlugin().setcost.containsKey(player.getName())) {
             if (this.getPlugin().getWarpManager().hasSignInfo(s.getLocation())) {
                 //It's a registered sign
                 SignInfo sign = this.getPlugin().getWarpManager().getSign(s.getLocation());
                 sign.setPrice(this.getPlugin().setcost.remove(player.getName()));
                 player.sendMessage("Price set to " + sign.getPrice() + " for this sign!");
                 return true;
             }
             for (String line : s.getLines()) {
                 if (line.toLowerCase().contains("[sortal]") || line.contains(this.getPlugin().getSettings().getSignContains())) {
                     SignInfo sign = this.getPlugin().getWarpManager().addSign(s.getLocation());
                     sign.setPrice(this.getPlugin().setcost.remove(player.getName()));
                     sign.setOwner(player.getName());
                     player.sendMessage("Price set to " + this.getPlugin().getWarpManager().getSign(s.getLocation()).getPrice() + " for this sign!");
                     return true;
                 }
             }
             player.sendMessage("This is not a valid sortal sign!");
             return true;
         }
         if (this.getPlugin().register.containsKey(player.getName())) {
             if (this.getPlugin().getWarpManager().hasSignInfo(s.getLocation())) {
                 //It's a registered sign
                 SignInfo sign = this.getPlugin().getWarpManager().getSign(s.getLocation());
                 sign.setWarp(this.getPlugin().register.remove(player.getName()));
                 player.sendMessage("Sign is now pointing to " + sign.getWarp());
                 return true;
             }
             SignInfo sign = this.getPlugin().getWarpManager().addSign(s.getLocation());
             String warp = this.getPlugin().register.remove(player.getName());
             sign.setWarp(warp);
             sign.setOwner(player.getName());
             player.sendMessage("Sign is now pointing to " + warp);
             return true;
         }
         if (this.getPlugin().unregister.contains(player.getName())) {
             if (!this.getPlugin().getWarpManager().hasSignInfo(s.getLocation())) {
                 player.sendMessage("This sign isn't registered, please hit a registered sign to unregister!");
                 return true;
             }
             SignInfo sign = this.getPlugin().getWarpManager().getSign(s.getLocation());
             if (!sign.hasWarp()) {
                 player.sendMessage("This sign isn't pointing to a warp!");
                 return true;
             }
             if (!sign.hasPrice()) {
                 //Sign doesn't have a price and warp gets removed, remove whole sign info
                 if (this.getPlugin().getSettings().useMySQL()) {
                     sign.delete(this.getPlugin().getMySQL(), this.getPlugin().getSignTable());
                 } else {
                     sign.delete(this.getPlugin().getWarpManager().signFile);
                 }
                 player.sendMessage("Unregistered sign! No data left for sign, removing..");
                 this.getPlugin().unregister.remove(player.getName());
                 return true;
             }
             sign.setWarp(null);
             this.getPlugin().unregister.remove(player.getName());
             player.sendMessage("Sign unregistered!");
             return true;
         }
         if (this.getPlugin().setuses.containsKey(player.getName())) {
             SignInfo info;
             if (this.getPlugin().getWarpManager().hasSignInfo(s.getLocation())) {
                 info = this.getPlugin().getWarpManager().getSign(s.getLocation());
             } else {
                 info = this.getPlugin().getWarpManager().addSign(s.getLocation());
             }
             String uses = this.getPlugin().setuses.remove(player.getName());
             String[] split = uses.split(",");
             info.setUses(Integer.parseInt(split[1]));
             info.setUsedTotalBased(split[0].equals("total") ? true : false);
             player.sendMessage("Uses set to " + info.getUses() + " for this sign, " + (info.isUsedTotalBased() ? "total based" : "player based") + "!");
             return true;
         }
         if (this.getPlugin().setPrivate.contains(player.getName())) {
             SignInfo sign;
             if (this.getPlugin().getWarpManager().hasSignInfo(s.getLocation())) {
                 sign = this.getPlugin().getWarpManager().getSign(s.getLocation());
             } else {
                 sign = this.getPlugin().getWarpManager().addSign(s.getLocation());
             }
             sign.setIsPrivate(!sign.isPrivate());
             player.sendMessage("This sign is now " + (sign.isPrivate() ? "private" : "public"));
             this.getPlugin().setPrivate.remove(player.getName());
             if (this.getPlugin().setPrivateUsers.containsKey(player.getName())) {
                 for (String name : this.getPlugin().setPrivateUsers.get(player.getName())) {
                     sign.addPrivateUser(name);
                 }
                 player.sendMessage(this.getPlugin().setPrivateUsers.remove(player.getName()).size() + " players added!");
                 return true;
             }
             return true;
         }
         if (this.getPlugin().setPrivateUsers.containsKey(player.getName())) {
             if (!this.getPlugin().getWarpManager().hasSignInfo(s.getLocation())) {
                 player.sendMessage("Please hit a private sign!");
                 return true;
             }
             SignInfo sign = this.getPlugin().getWarpManager().getSign(s.getLocation());
             if (!sign.isPrivate()) {
                 player.sendMessage("Please hit a private sign!");
                 return true;
             }
             for (String name : this.getPlugin().setPrivateUsers.get(player.getName())) {
                 sign.addPrivateUser(name);
             }
             player.sendMessage(this.getPlugin().setPrivateUsers.remove(player.getName()).size() + " players added!");
             return true;
         }
         return false;
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         Block b = event.getBlock();
         if (b.getType().equals(Material.SIGN) || b.getType().equals(Material.SIGN_POST)
                 || b.getType().equals(Material.WALL_SIGN)) {
             Sign s = (Sign) b.getState();
             for (String line : s.getLines()) {
                 if (line.toLowerCase().contains("[sortal]") || line.toLowerCase().contains(this.getPlugin().getSettings().getSignContains())) {
                     if (!event.getPlayer().hasPermission("sortal.breaksign")) {
                         event.getPlayer().sendMessage(this.getLocalisation().getNoPerms());
                         event.setCancelled(true);
                         return;
                     }
                 }
             }
             //no [Sortal] or whatever on sign, maybe registered?
             if (this.getPlugin().getWarpManager().hasSignInfo(b.getLocation())) {
                 if (!event.getPlayer().hasPermission("sortal.breaksign")) {
                     event.getPlayer().sendMessage(this.getLocalisation().getNoPerms());
                     event.setCancelled(true);
                 }
                 SignInfo i = this.getPlugin().getWarpManager().getSign(b.getLocation());
                 if (this.getPlugin().getSettings().useMySQL()) {
                     i.delete(this.getPlugin().getMySQL(), this.getPlugin().getSignTable());
                 } else {
                     i.delete(this.getPlugin().getWarpManager().signFile);
                 }
             }
         }
     }
 
     /*
      * @return returns true if the check was completed without denial
      */
     private boolean usesCheck(Warp w, SignInfo sign, Player p) {
         UserInfo f = this.getPlugin().getWarpManager().getUserInfo(p.getName());
         if (w == null && sign == null) {
             this.getPlugin().debug("[Debug] Uses end code 1");
             return true;
         }
         if (w == null) {
             if (sign.getUses() == -1) {
                 this.getPlugin().debug("[Debug] Uses end code 2.1");
                 return true;
             }
         }
         if (sign == null) {
             if (w.getUses() == -1) {
                 this.getPlugin().debug("[Debug] Uses end code 2.2");
                 return true;
             }
         }
         if (w == null) {
             //warp is unlimited then
             if (sign.isUsedTotalBased()) {
                 if (sign.getUsed() >= sign.getUses()) {
                     this.getPlugin().debug("[Debug] Uses end code 3");
                     //Used more often than allowed
                     return false;
                 }
                 sign.setUsed(sign.getUsed() + 1);
                 this.getPlugin().debug("[Debug] Uses end code 4");
                 return true;
             } else {
                 if (sign.getUses() > f.getUsedLocation(sign.getLocation())) {
                     //Not used as many times as allowed
                     f.addtoUsedLocation(sign.getLocation(), 1);
                     this.getPlugin().debug("[Debug] Uses end code 5");
                     return true;
                 }
                 this.getPlugin().debug("[Debug] Uses end code 6");
                 return false;
             }
         }
         if (sign == null) {
             //warp is unlimited then
             if (w.isUsedTotalBased()) {
                 if (w.getUsed() >= w.getUses()) {
                     this.getPlugin().debug("[Debug] Uses end code 7");
                     //Used more often than allowed
                     return false;
                 }
                 w.setUsed(w.getUsed() + 1);
                 this.getPlugin().debug("[Debug] Uses end code 8");
                 return true;
             } else {
                 if (w.getUses() > f.getUsedWarp(w.getName())) {
                     //Not used as many times as allowed
                     f.addtoUsedWarp(w.getName(), 1);
                     this.getPlugin().debug("[Debug] Uses end code 9," + w.getUses() + "," + f.getUsedWarp(w.getName()));
                     return true;
                 }
                 this.getPlugin().debug("[Debug] Uses end code 10");
                 return false;
             }
         }
         if (sign.getUses() == -1 && w.getUses() == -1) {
             //both are unlimited
             this.getPlugin().debug("[Debug] Uses end code 11");
             return true;
         }
         if (w.getUses() == -1) {
             //warp is unlimited, sign isn't or it'd already returned true
             if (sign.isUsedTotalBased()) {
                 if (sign.getUsed() >= sign.getUses()) {
                     //Used more often than allowed
                     this.getPlugin().debug("[Debug] Uses end code 12");
                     return false;
                 } else {
                     sign.setUsed(sign.getUsed() + 1);
                     this.getPlugin().debug("[Debug] Uses end code 13");
                     return true;
                 }
             } else {
                 if (sign.getUses() > f.getUsedLocation(sign.getLocation())) {
                     //Not used as many times as allowed
                     f.addtoUsedLocation(sign.getLocation(), 1);
                     this.getPlugin().debug("[Debug] Uses end code 14");
                     return true;
                 } else {
                     this.getPlugin().debug("[Debug] Uses end code 15");
                     return false;
                 }
             }
         }
         if (sign.getUses() == -1) {
             if (w.isUsedTotalBased()) {
                 if (w.getUsed() >= w.getUses()) {
                     //Used more often than allowed
                     this.getPlugin().debug("[Debug] Uses end code 16");
                     return false;
                 } else {
                     w.setUsed(w.getUsed() + 1);
                     this.getPlugin().debug("[Debug] Uses end code 17");
                     return true;
                 }
             } else {
                 if (w.getUses() > f.getUsedWarp(w.getName())) {
                     //Not used as many times as allowed
                     f.addtoUsedWarp(w.getName(), 1);
                     this.getPlugin().debug("[Debug] Uses end code 18");
                     return true;
                 } else {
                     this.getPlugin().debug("[Debug] Uses end code 19");
                     return false;
                 }
             }
         }
         //They both aren't -1, doing epic face check
         if (w.isUsedTotalBased()) {
             if (w.getUsed() >= w.getUses()) {
                 this.getPlugin().debug("[Debug] Uses end code 20");
                 //Used more often than allowed
                 return false;
             }
             if (sign.isUsedTotalBased()) {
                 if (sign.getUsed() >= sign.getUses()) {
                     this.getPlugin().debug("[Debug] Uses end code 21");
                     //Used more often than allowed
                     return false;
                 }
                 this.getPlugin().debug("[Debug] Uses end code 22");
                 sign.setUsed(sign.getUsed() + 1);
                 w.setUsed(w.getUsed() + 1);
                 return true;
             }
             if (sign.getUses() > f.getUsedLocation(sign.getLocation())) {
                 this.getPlugin().debug("[Debug] Uses end code 23");
                 w.setUsed(w.getUsed() + 1);
                 f.addtoUsedLocation(sign.getLocation(), 1);
                 return true;
             }
             this.getPlugin().debug("[Debug] Uses end code 24");
             return false;
         }
         if (sign.isUsedTotalBased()) {
             if (sign.getUsed() >= sign.getUses()) {
                 this.getPlugin().debug("[Debug] Uses end code 25");
                 return false;
             }
             //w cant be usedTotalBased
             if (w.getUses() > f.getUsedWarp(w.getName())) {
                 this.getPlugin().debug("[Debug] Uses end code 26");
                 sign.setUsed(sign.getUsed() + 1);
                 f.addtoUsedWarp(w.getName(), 1);
                 return true;
             }
             this.getPlugin().debug("[Debug] Uses end code 27");
             return false;
         } else {
             if (w.getUses() <= f.getUsedWarp(w.getName())) {
                 this.getPlugin().debug("[Debug] Uses end code 28");
                 return false;
             }
             if (sign.getUses() <= f.getUsedLocation(sign.getLocation())) {
                 this.getPlugin().debug("[Debug] Uses end code 29");
                 return false;
             }
             f.addtoUsedLocation(sign.getLocation(), 1);
             f.addtoUsedWarp(w.getName(), 1);
             this.getPlugin().debug("[Debug] Uses end code 30");
             return true;
         }
         //both player based
     }
 
     private boolean canPay(Warp w, SignInfo sign, Player p) {
         if (this.getPlugin().canPay(p, this.getPrice(w, sign))) {
             return true;
         }
         return false;
     }
 
     private int getPrice(Warp w, SignInfo sign) {
         if (sign != null && sign.hasPrice()) {
             return sign.getPrice();
         } else if (w != null && w.hasPrice()) {
             return w.getPrice();
         } else {
             return this.getPlugin().getSettings().getWarpUsePrice();
         }
     }
 
     private boolean isPrivateUser(SignInfo sign, Player p) {
         if(!sign.isPrivate()){
             return true;
         }
         this.getPlugin().debug("[Debug] Sign is private");
         if (!sign.isPrivateUser(p.getName())) {
             this.getPlugin().debug("[Debug] Not a PrivateUser");
             //not a private user, maybe setting overrides
             if (this.getPlugin().getSettings().isSignCreatorIsPrivateUser()) {
                 this.getPlugin().debug("[Debug] Setting is true, checking owner");
                 if (sign.hasOwner()) {
                     this.getPlugin().debug("[Debug] Sign has owner!");
                     if(sign.getOwner().equals(p.getName())){
                         this.getPlugin().debug("[Debug] Wow! They're the same!");
                         return true;
                     }
                     this.getPlugin().debug("[Debug] Aww.. Another owner for this sign!");
                     return false;
                 }
                 this.getPlugin().debug("[Debug] Sign has no owner");
                 return false;
             }
             this.getPlugin().debug("[Debug] Setting is false -> false");
             return false;
         }
         this.getPlugin().debug("[Debug] Player is a private user.");
         return true;
     }
 }
