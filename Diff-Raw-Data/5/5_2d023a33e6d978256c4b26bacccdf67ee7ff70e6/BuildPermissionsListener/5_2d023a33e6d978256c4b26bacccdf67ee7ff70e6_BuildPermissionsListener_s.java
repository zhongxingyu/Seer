 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package at.co.hohl.myresidence.bukkit.listener;
 
 import at.co.hohl.mcutils.chat.Chat;
 import at.co.hohl.myresidence.PermissionsResolver;
 import at.co.hohl.myresidence.translations.Translate;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 /**
  * Listens to changes on blocks done by players and checks if they are allowed to do so.
  *
  * @author Michael Hohl
  */
 public class BuildPermissionsListener extends BlockListener {
   private final PermissionsResolver permissionsResolver;
 
   /**
    * Creates a new BuildPermissionsListener.
    *
    * @param permissionsResolver resolver used to check if player is allowed to do that.
    */
   public BuildPermissionsListener(PermissionsResolver permissionsResolver) {
     this.permissionsResolver = permissionsResolver;
   }
 
   @Override
   public void onBlockPlace(BlockPlaceEvent event) {
     if (event.isCancelled() || !event.canBuild()) {
       return;
     }
 
    if (permissionsResolver.isAllowedToPlaceBlockAt(event.getPlayer(), event.getBlockPlaced())) {
       Chat.sendMessage(event.getPlayer(), Translate.get("not_allowed_to_build"));
       event.setBuild(false);
     }
   }
 
   @Override
   public void onBlockBreak(BlockBreakEvent event) {
     if (event.isCancelled()) {
       return;
     }
 
    if (permissionsResolver.isAllowedToDestroyBlockAt(event.getPlayer(), event.getBlock())) {
       Chat.sendMessage(event.getPlayer(), Translate.get("not_allowed_to_destroy"));
       event.setCancelled(true);
     }
   }
 
 }
