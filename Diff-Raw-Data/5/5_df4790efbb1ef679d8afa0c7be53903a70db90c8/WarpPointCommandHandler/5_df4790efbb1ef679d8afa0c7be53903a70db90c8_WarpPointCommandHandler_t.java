 package de.felixbruns.minecraft.handlers.commands;
 
 import java.util.Map.Entry;
 
 import de.felixbruns.minecraft.SpMcPlayer;
 import de.felixbruns.minecraft.SpMcStorage;
 import de.felixbruns.minecraft.handlers.CommandHandler;
 import de.felixbruns.minecraft.handlers.commands.annotations.CommandProvider;
 import de.felixbruns.minecraft.protocol.Colors;
 import de.felixbruns.minecraft.protocol.Position;
 import de.felixbruns.minecraft.protocol.packets.Packet;
 import de.felixbruns.minecraft.protocol.packets.PacketPlayerPositionAndLook;
 
 @CommandProvider(commands = {"setwarp", "setglobalwarp", "deletewarp", "deleteglobalwarp", "listwarps", "warp", "warpto"})
 public class WarpPointCommandHandler extends CommandHandler implements Colors {
     /**
      * Handle a warp command sent by a player.
      * 
      * @param player   The associated player.
      * @param command  The command that was sent.
      * @param args     The arguments to the command.
      */
     public Packet handleCommand(SpMcPlayer player, Packet packet, String command, String... args){
     	if(command.equals("setwarp")){
     		if(args.length != 1){
     			player.sendMessage(COLOR_LIGHT_RED + "Usage: !add-warp <name>");
     			
     			return null;
     		}
     		
     		player.getWarpPoints().put(args[0], player.getPosition());
     		
     		SpMcStorage.saveWarpPoints(player.getName(), player.getWarpPoints());
     		
     		return null;
     	}
     	else if(command.equals("setglobalwarp")){
     		if(args.length != 1){
     			player.sendMessage(COLOR_LIGHT_RED + "Usage: !add-global-warp <name>");
     			
     			return null;
     		}
     		
     		player.getWrapper().getWarpPoints().put(args[0], player.getPosition());
     		
     		SpMcStorage.saveWarpPoints(player.getWrapper().getWarpPoints());
     		
     		return null;
     	}
     	else if(command.equals("deletewarp")){
     		if(args.length != 1){
     			player.sendMessage(COLOR_LIGHT_RED + "Usage: !delete-warp <name>");
     			
     			return null;
     		}
     		
     		player.getWarpPoints().remove(args[0]);
     		
     		SpMcStorage.saveWarpPoints(player.getName(), player.getWarpPoints());
     		
     		return null;
     	}
     	else if(command.equals("deleteglobalwarp")){
     		if(args.length != 1){
     			player.sendMessage(COLOR_LIGHT_RED + "Usage: !delete-global-warp <name>");
     			
     			return null;
     		}
     		
     		player.getWrapper().getWarpPoints().remove(args[0]);
     		
     		SpMcStorage.saveWarpPoints(player.getWrapper().getWarpPoints());
     		
     		return null;
     	}
     	else if(command.equals("listwarps")){
     		int n;
 
 			player.sendMessage("");
 			player.sendMessage(COLOR_LIGHT_YELLOW + "Global warp points:");
 			
 			if(player.getWrapper().getWarpPoints().isEmpty()){
 				player.sendMessage(COLOR_LIGHT_YELLOW + "  None");
 			}
 			
 			n = 1;
 			
     		for(Entry<String, Position> entry : player.getWrapper().getWarpPoints().entrySet()){
     			String   name     = entry.getKey();
     			Position position = entry.getValue();
     			
     			player.sendMessage(COLOR_LIGHT_YELLOW + String.format(
     				" %2d. %s (%.2fm)", n++,
     				name, position.distance(player.getPosition())
     			));
     		}
     		
 			player.sendMessage("");
 			player.sendMessage(COLOR_LIGHT_YELLOW + "Personal warp points:");
 			
 			if(player.getWarpPoints().isEmpty()){
 				player.sendMessage(COLOR_LIGHT_YELLOW + "  None");
 			}
 			
 			n = 1;
 			
     		for(Entry<String, Position> entry : player.getWarpPoints().entrySet()){
     			String   name     = entry.getKey();
     			Position position = entry.getValue();
     			
     			player.sendMessage(COLOR_LIGHT_YELLOW + String.format(
         			" %2d. %s (%.2fm)", n++,
         			name, position.distance(player.getPosition())
         		));
     		}
     		
 			player.sendMessage("");
     		
     		return null;
     	}
     	else if(command.equals("warp")){
     		if(args.length != 1){
     			player.sendMessage(COLOR_LIGHT_RED + "Usage: !warp <name>");
     			
     			return null;
     		}
     		
     		Position target = player.getWarpPoints().get(args[0]);
     		
     		if(target == null){
     			target = player.getWrapper().getWarpPoints().get(args[0]);
     		}
     		
     		if(target != null){
     			this.warp(player, target);
     		}
     		
     		return null;
     	}
     	else if(command.equals("warpto")){
     		if(args.length != 1){
     			player.sendMessage(COLOR_LIGHT_RED + "Usage: !warp <name>");
     			
     			return null;
     		}
     		
     		SpMcPlayer target = player.getWrapper().getPlayers().get(args[0]);
     		
     		if(target != null){
     			this.warp(player, target);
     		}
     		
     		return null;
     	}
     	
     	return packet;
     }
     
 	/**
 	 * Warp a player to another player.
 	 * 
 	 * @param player The player to warp.
 	 * @param target The target player to warp to.
 	 */
 	private void warp(SpMcPlayer player, SpMcPlayer target){
 		warp(player, target.getPosition());
 	}
 	
 	/**
 	 * Warp a player to a target position.
 	 * 
 	 * @param player The player to warp.
 	 * @param target The target position.
 	 */
 	private void warp(SpMcPlayer player, Position target){
 		PacketPlayerPositionAndLook packet = new PacketPlayerPositionAndLook();
 		
 		/* Fill the packet with position information. */
 		packet.x        = player.getPosition().x;
 		packet.z        = player.getPosition().z;
 		packet.yaw      = 0.0f;
 		packet.pitch    = 0.0f;
 		packet.onGround = false;
 		
 		/* First move up to level 128. */
 		packet.y      = 128.0;
 		packet.stance = 128.0 + 1.62;
 		
 		player.sendToServer(packet);
 		
 		/* Then move sideways to the target location. */
 		packet.x = target.x;
 		packet.z = target.z;
 		
 		player.sendToServer(packet);
 		
 		/* Now move down to the target level. */
		packet.y        = target.y;
		packet.stance   = target.y + 1.62;
		packet.onGround = true;
 		
 		player.sendToServer(packet);
 		
 		/*
 		 * Also send that packet to the client, but swap
 		 * Y and stance here... (see protocol specification).
 		 */
 		packet.y      = target.y + 1.62;
 		packet.stance = target.y;
 		
 		player.sendToClient(packet);
 	}
 }
