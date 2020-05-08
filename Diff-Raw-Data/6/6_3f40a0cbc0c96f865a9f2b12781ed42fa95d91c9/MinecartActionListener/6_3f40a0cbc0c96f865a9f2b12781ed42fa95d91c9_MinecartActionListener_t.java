 package net.sradonia.bukkit.minecartmania.teleport;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.util.Vector;
 
 import com.afforess.minecartmaniacore.MinecartManiaMinecart;
 import com.afforess.minecartmaniacore.event.MinecartActionEvent;
 import com.afforess.minecartmaniacore.event.MinecartManiaListener;
 import com.afforess.minecartmaniacore.utils.MinecartUtils;
 
 public class MinecartActionListener extends MinecartManiaListener {
 	private final TeleporterList teleporters;
 
 	public MinecartActionListener(TeleporterList teleporters) {
 		this.teleporters = teleporters;
 	}
 
 	@Override
 	public void onMinecartActionEvent(MinecartActionEvent event) {
		if (event.isActionTaken())
			return;
		Block blockAhead = event.getMinecart().getBlockTypeAhead();
		if (blockAhead != null) {
 			Material type = blockAhead.getType();
 			if (type.equals(Material.SIGN_POST) || type.equals(Material.WALL_SIGN)) {
 				// Minecart is going to crash into a sign...
 
 				Location signLocation = blockAhead.getLocation();
 				Teleporter teleporter = teleporters.search(signLocation);
 				if (teleporter != null) {
 					// ... which is a teleporter!
 
 					event.setActionTaken(true);
 
 					MinecartManiaMinecart minecart = event.getMinecart();
 					Location targetLocation = teleporter.getOther(signLocation);
 					if (targetLocation == null) {
 						// but we're missing the second waypoint...
 						if (minecart.hasPlayerPassenger()) {
 							minecart.getPlayerPassenger().sendMessage("You just crashed into an unconnected teleporter sign ;-)");
 						}
 					} else {
 						// search for minecart tracks around the target waypoint
 						Location trackLocation = findTrackAround(targetLocation);
 						if (trackLocation == null) {
 							if (minecart.hasPlayerPassenger()) {
 								minecart.getPlayerPassenger().sendMessage("Couldn't find tracks at target sign.");
 							}
 						} else {
 							// teleport minecart...
 							minecart.minecart.teleportTo(trackLocation);
 
 							// ...and set it's moving direction
 							double speed = minecart.minecart.getVelocity().length();
 							if (targetLocation.getX() > trackLocation.getX())
 								minecart.minecart.setVelocity(new Vector(-speed, 0, 0));
 							else if (targetLocation.getX() < trackLocation.getX())
 								minecart.minecart.setVelocity(new Vector(speed, 0, 0));
 							else if (targetLocation.getZ() > trackLocation.getZ())
 								minecart.minecart.setVelocity(new Vector(0, 0, -speed));
 							else if (targetLocation.getZ() < trackLocation.getZ())
 								minecart.minecart.setVelocity(new Vector(0, 0, speed));
 						}
 					}
 
 				}
 			}
 		}
 	}
 
 	public static Location findTrackAround(Location center) {
 		Block centerBlock = center.getBlock();
 
 		Block block;
 		block = centerBlock.getRelative(BlockFace.NORTH);
 		if (MinecartUtils.isMinecartTrack(block))
 			return block.getLocation();
 		block = centerBlock.getRelative(BlockFace.SOUTH);
 		if (MinecartUtils.isMinecartTrack(block))
 			return block.getLocation();
 		block = centerBlock.getRelative(BlockFace.EAST);
 		if (MinecartUtils.isMinecartTrack(block))
 			return block.getLocation();
 		block = centerBlock.getRelative(BlockFace.WEST);
 		if (MinecartUtils.isMinecartTrack(block))
 			return block.getLocation();
 
 		return null;
 	}
 }
