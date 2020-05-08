 package com.vartala.soulofw0lf.rpgapi.vectorapi;
 
 import com.vartala.soulofw0lf.rpgapi.RpgAPI;
 import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
 import org.bukkit.Location;
import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
 
 /**
  * Created by: soulofw0lf
  * Date: 7/25/13
  * Time: 4:05 PM
  * <p/>
  * This file is part of the Rpg Suite Created by Soulofw0lf and Linksy.
  * <p/>
  * The Rpg Suite is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * The Rpg Suite is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with The Rpg Suite Plugin you have downloaded.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class VecBlockListener implements Listener {
     RpgAPI Rpg;
     public VecBlockListener(RpgAPI rpga){
         this.Rpg = rpga;
         Bukkit.getPluginManager().registerEvents(this, this.Rpg);
     }
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player p = event.getPlayer();
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR) && p.getItemInHand().getType().equals(Material.BRICK)){
            p.sendMessage(ChatColor.GREEN + "Eating Granola Bar!");
            event.setCancelled(true);
            ItemStack iS = p.getItemInHand();
            if (iS.getAmount() <= 1){
                p.getInventory().remove(iS);
                p.updateInventory();
            } else {
                iS.setAmount(iS.getAmount() -1);
                p.updateInventory();
            }
        }
    }
     @EventHandler
     public void vecMove(PlayerMoveEvent e){
         Player p = e.getPlayer();
         Block b = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
         if (RpgAPI.vecBlocks.contains(b)){
             RpgVectorBlocks rV = RpgAPI.vecBlockMap.get(b);
             Location loc = p.getLocation();
             loc.setY(loc.getY()+1.5);
             p.teleport(loc);
             p.setNoDamageTicks(rV.getImmune());
             p.setVelocity(rV.getVec());
         }
     }
 }
