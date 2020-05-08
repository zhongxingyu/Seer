 /************************************************************************
  * This file is part of WarpSign.									
  *																		
  * WarpSign is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by	
  * the Free Software Foundation, either version 3 of the License, or		
  * (at your option) any later version.									
  *																		
  * WarpSign is distributed in the hope that it will be useful,	
  * but WITHOUT ANY WARRANTY; without even the implied warranty of		
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			
  * GNU General Public License for more details.							
  *																		
  * You should have received a copy of the GNU General Public License
  * along with WarpSign.  If not, see <http://www.gnu.org/licenses/>.
  ************************************************************************/
 package be.Balor.WarpSign.Listeners;
 
 import static be.Balor.Tools.Utils.colorParser;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import be.Balor.Manager.Exceptions.WorldNotLoaded;
 import be.Balor.Manager.Permissions.PermissionManager;
 import be.Balor.Tools.Utils;
 import be.Balor.Tools.Warp;
 import be.Balor.WarpSign.ConfigEnum;
 import be.Balor.WarpSign.WarpSign;
 import be.Balor.WarpSign.Utils.WarpSignContainer;
 import be.Balor.World.ACWorld;
 
 /**
  * @author Balor (aka Antoine Aflalo)
  * 
  */
 public class SignListener implements Listener {
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockBreak(final BlockBreakEvent event) {
 		if (event.isCancelled()) {
 			return;
 		}
 		final Block block = event.getBlock();
 		if (!(block.getState() instanceof Sign)) {
 			return;
 		}
 		final Sign sign = (Sign) block.getState();
 		if (sign.getLine(0).indexOf(ConfigEnum.KEYWORD.getString()) != 0) {
 			return;
 		}
 		if (!PermissionManager.hasPerm(event.getPlayer(),
 				"admincmd.warpsign.edit")) {
 			event.setCancelled(true);
 			return;
 		}
 
 		try {
 			final PreparedStatement stmt = WarpSign
 					.getSqlLite()
 					.prepareStatement(
 							"DELETE FROM `signs` WHERE `signs`.`x` = ? AND `signs`.`y` = ? AND `signs`.`z` = ?");
 			stmt.setInt(1, block.getX());
 			stmt.setInt(2, block.getY());
 			stmt.setInt(3, block.getZ());
 			stmt.execute();
 		} catch (final SQLException e) {
 			WarpSign.logSqliteException(e);
 		}
 
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public WarpSignContainer onPlayerInteract(final PlayerInteractEvent event) {
 		if (event.isCancelled()) {
 			return null;
 		}
 		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			return null;
 		}
 		final Block block = event.getClickedBlock();
 		if (!(block.getState() instanceof Sign)) {
 			return null;
 		}
 		final Sign sign = (Sign) block.getState();
 		if (sign.getLine(0).indexOf(ConfigEnum.KEYWORD.getString()) != 0) {
 			return null;
 		}
 		if (!PermissionManager.hasPerm(event.getPlayer(),
 				"admincmd.warpsign.use")) {
 			return null;
 		}
 		ACWorld world;
 		final Player p = event.getPlayer();
 		WarpSignContainer container = WarpSign.getSign(sign);
 		Warp warpPoint;
 		if (container == null) {
 			try {
 				world = ACWorld.getWorld(ChatColor.stripColor(sign.getLine(1)));
 			} catch (final WorldNotLoaded e) {
 				p.sendMessage(ConfigEnum.WORLDNF.getString() + sign.getLine(1));
 				return null;
 			}
 			warpPoint = world.getWarp(ChatColor.stripColor(sign.getLine(2)));
 			if (warpPoint == null) {
 				p.sendMessage(ConfigEnum.WARPNF.getString() + sign.getLine(2));
 				return null;
 			}
 			container = new WarpSignContainer(warpPoint.name, world.getName(),
 					sign);
 		} else {
 			container.sign = sign;
 			try {
 				world = ACWorld.getWorld(container.worldName);
 			} catch (final WorldNotLoaded e) {
				p.sendMessage(ConfigEnum.WORLDNF.getString() + sign.getLine(1));
 				return null;
 			}
 			warpPoint = world.getWarp(container.warpName);
 			if (warpPoint == null) {
				p.sendMessage(ConfigEnum.WARPNF.getString() + sign.getLine(2));
 				return null;
 			}
 		}
 		Utils.teleportWithChunkCheck(p, warpPoint.loc);
 		p.sendMessage(colorParser(ConfigEnum.TP_MSG.getString())
 				+ warpPoint.name);
 		return container;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onSignChange(final SignChangeEvent event) {
 		if (event.isCancelled()) {
 			return;
 		}
 		final String line0 = event.getLine(0);
 		final Player p = event.getPlayer();
 		if (line0.indexOf(ConfigEnum.KEYWORD.getString()) != 0) {
 			return;
 		}
 		if (!PermissionManager.hasPerm(p, "admincmd.warpsign.edit")) {
 			event.setCancelled(true);
 			return;
 		}
 
 		final String world = event.getLine(1);
 		final String warp = event.getLine(2);
 		ACWorld acWorld;
 		try {
 			acWorld = ACWorld.getWorld(world);
 			event.setLine(1, acWorld.getName());
 		} catch (final WorldNotLoaded e) {
 			event.setLine(1, ConfigEnum.WORLDNF.getString() + world);
 			return;
 		}
 
 		final Warp warpPoint = acWorld.getWarp(warp);
 		if (warpPoint == null) {
 			event.setLine(2, ConfigEnum.WARPNF.getString() + warp);
 			return;
 		}
 		event.setLine(2, warpPoint.name);
 
 		if (ConfigEnum.COLOR.getBoolean()) {
 			event.setLine(
 					1,
 					colorParser(ConfigEnum.WORDC.getString())
 							+ event.getLine(1));
 			event.setLine(
 					2,
 					colorParser(ConfigEnum.WARPC.getString())
 							+ event.getLine(2));
 		}
 		WarpSign.insertSign(acWorld.getName(), warpPoint.name, event.getBlock());
 	}
 }
