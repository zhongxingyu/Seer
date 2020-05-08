 package de.felixbruns.minecraft.handlers.commands;
 
 import java.util.Map.Entry;
 
 import de.felixbruns.minecraft.SpMcGroup;
 import de.felixbruns.minecraft.SpMcPlayer;
 import de.felixbruns.minecraft.SpMcStorage;
 import de.felixbruns.minecraft.handlers.CommandHandler;
 import de.felixbruns.minecraft.handlers.commands.annotations.CommandProvider;
 import de.felixbruns.minecraft.protocol.Colors;
 import de.felixbruns.minecraft.protocol.packets.Packet;
 import de.felixbruns.minecraft.protocol.packets.PacketDisconnect;
 
 @CommandProvider(commands = {"setgroup", "quit", "restart"})
 public class AdminCommandHandler extends CommandHandler implements Colors {
     public Packet handleCommand(SpMcPlayer player, Packet packet, String command, String... args){
     	/*
     	 * The 'quit' command stops and waits for the minecraft
     	 * server to shut down, then it also terminates the wrapper.
     	 */
     	if(command.equals("setgroup")){
     		if(args.length != 2){
     			player.sendMessage(COLOR_LIGHT_RED, "Usage: !setgroup <player> <group>");
     			
     			return null;
     		}
     		
     		SpMcPlayer p = player.getWrapper().getPlayers().get(args[0]);
     		SpMcGroup  g = player.getWrapper().getGroups().get(args[1]);
     		
     		if(p == null || g != null){
    			if(p.getGroup() != null){
    				p.getGroup().removePlayer(args[0]);
    			}
    			
     			g.addPlayer(args[0]);
     			p.setGroup(g);
     			
     			player.sendMessage(COLOR_LIGHT_GREEN, "%s's group is now '%s'!", args[0], args[1]);
     		}
     		else{
     			player.sendMessage(COLOR_LIGHT_RED, "That player or group doesn't exist!");
     		}
     		
     		SpMcStorage.saveGroups(player.getWrapper().getGroups());
     		
     		return null;
     	}
     	/*
     	 * The 'quit' command stops and waits for the minecraft
     	 * server to shut down, then it also terminates the wrapper.
     	 */
     	else if(command.equals("quit")){
     	   	if(!player.getWrapper().isConsoleAvailable()){
         		player.sendMessage(COLOR_LIGHT_RED + "Sorry, the server console is not available!");
     	   		
     	   		return null;
         	}
     		
     		for(Entry<String, SpMcPlayer> entry : player.getWrapper().getPlayers().entrySet()){
     			SpMcPlayer p = entry.getValue();
     			
     			p.sendToClient(new PacketDisconnect("Server is stopping..."));
     			p.disconnect();
     		}
     	   	
     		player.getWrapper().stopServer();
     		
     		System.exit(0);
     		
     		return null;
     	}
     	/* The 'restart' command restarts the minecraft server. */
     	else if(command.equals("restart")){
     	   	if(!player.getWrapper().isConsoleAvailable()){
         		player.sendMessage(COLOR_LIGHT_RED + "Sorry, the server console is not available!");
     	   		
     	   		return null;
         	}
     		
     		for(Entry<String, SpMcPlayer> entry : player.getWrapper().getPlayers().entrySet()){
     			SpMcPlayer p = entry.getValue();
     			
     			p.sendToClient(new PacketDisconnect("Server is restarting..."));
     			p.disconnect();
     		}
     		
     		player.getWrapper().restartServer();
     		
     		return null;
     	}
     	
     	return packet;
     }
 }
