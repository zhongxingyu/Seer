 /*******************************************************************************
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright (c) 2012 Mark Morgan.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer
  * in the documentation and/or other materials provided with the
  * distribution.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * Contributors:
  *     Mark Morgan - initial API and implementation
  ******************************************************************************/
 /**
  * 
  */
 package org.morganm.liftsign.listener;
 
 import javax.inject.Inject;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.morganm.liftsign.PermissionCheck;
 import org.morganm.liftsign.SignCache;
 import org.morganm.liftsign.SignDetail;
 import org.morganm.liftsign.SignFactory;
 import org.morganm.liftsign.Util;
 import org.morganm.mBukkitLib.Logger;
 import org.morganm.mBukkitLib.Teleport;
 
 /**
  * @author morganm
  *
  */
 public class PlayerListener implements Listener {
 	private final SignCache cache;
 	private final PermissionCheck perm;
 	private final Teleport teleport;
 	private final Logger log;
 	private final Util util;
 	private final SignFactory factory;
 
 	@Inject
 	public PlayerListener(SignCache cache, SignFactory factory, PermissionCheck perm,
 			Teleport teleport, Logger log, Util util) {
 		this.cache = cache;
 		this.factory = factory;
 		this.perm = perm;
 		this.teleport = teleport;
 		this.log = log;
 		this.util = util;
 	}
 	
 	@EventHandler(ignoreCancelled=true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if( event.getAction() != Action.RIGHT_CLICK_BLOCK )
 			return;
 
 		Sign sign = util.getSignState(event.getClickedBlock());
 		if( sign != null ) {
 			final Player p = event.getPlayer();
 			
 			log.debug("Sign right clicked");
 			SignDetail signDetail = cache.getCachedSignDetail(sign.getLocation());
 			if( signDetail == null ) {
 				log.debug("Instantiating SignDetail object");
 				signDetail = factory.create(sign, null);
 			}
 			
 			SignDetail targetLift = null;
 			if( signDetail != null )
 				targetLift = signDetail.getTargetLift();
 			
 			Sign targetSign = null;
 			World world = null;
 			if( targetLift != null ) {
 				world = targetLift.getWorld();
 				Block signBlock = world.getBlockAt(targetLift.getLocation());
 				targetSign = util.getSignState(signBlock);
 			}
 			
 			if( targetLift != null && targetSign != null ) {
 				// abort if player doesn't have permission
 				if( !perm.canUseNormalLift(p) ) {
 					util.getMessageUtil().sendLocalizedMessage(p, Util.MSG_NO_PERMISSION);
 					return;
 				}
 				log.debug("has permission");
 				
 				// check to make sure targetblock is safe
 				Location playerLocation = event.getPlayer().getLocation();
 				Location finalLocation = null;
 				for(int y=targetSign.getY(); y >= targetSign.getY()-1; y--) {
 					Location newLocation = new Location(playerLocation.getWorld(), playerLocation.getX(),
 							y, playerLocation.getZ(), playerLocation.getYaw(), 
 							playerLocation.getPitch());
 					Block testBlock = world.getBlockAt(newLocation);
 					if( teleport.isSafeBlock(testBlock, 0) )
 						finalLocation = newLocation;
 				}
 				
 				if( finalLocation != null ) {
 					event.getPlayer().teleport(finalLocation);
 					if( finalLocation.getBlockY() > playerLocation.getBlockY() )
 						util.getMessageUtil().sendLocalizedMessage(p, Util.MSG_UP_ONE_FLOOR);
 					else
 						util.getMessageUtil().sendLocalizedMessage(p, Util.MSG_DOWN_ONE_FLOOR);
 				}
 				else {
 					util.getMessageUtil().sendLocalizedMessage(p, Util.MSG_DESTINATION_NOT_SAFE);
 				}
 			}
 			else {
 				log.debug("no target");
 				util.getMessageUtil().sendLocalizedMessage(p, Util.MSG_NO_VALID_LIFT_TARGET);
 			}
 		}
 	}
 }
