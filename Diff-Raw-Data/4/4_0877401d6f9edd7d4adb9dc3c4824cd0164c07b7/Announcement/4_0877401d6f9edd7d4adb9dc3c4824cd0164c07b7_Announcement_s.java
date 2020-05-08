 package me.thelunarfrog.frogannounce;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Announcement{
 	private String[] text;
 	private final List<String> groups;
 	private final List<String> worlds;
 	private final List<String> commands;
 
 	public void execute(){
 		final List<Player> players = new ArrayList<Player>();
 		final List<String> ignored = FrogAnnounce.getInstance().getIgnoredPlayers();
 		if(!this.worlds.isEmpty()){
 			for(final String world : this.worlds){
 				World w;
 				if((w = Bukkit.getServer().getWorld(world)) != null){
 					for(final Player p : w.getPlayers())
 						if(!ignored.contains(p.getName()))
 							if(this.groups.isEmpty()){
 								players.add(p);
 							}else if(!players.contains(p)){
 								for(final String group : FrogAnnounce.getInstance().getVaultPerms().getPlayerGroups(p))
 									if(this.groups.contains(group)){
 										players.add(p);
 										break;
 									}
 							}
 				}else{
 					System.err.println("World \"" + world + "\" isn't a valid world, so it couldn't be used in restriction for announcing...");
 				}
 			}
 		}else if(!this.groups.isEmpty()){
 			for(final Player p : Bukkit.getServer().getOnlinePlayers())
 				if(!ignored.contains(p.getName())){
 					for(final String group : FrogAnnounce.getInstance().getVaultPerms().getPlayerGroups(p))
 						if(this.groups.contains(group)){
 							players.add(p);
 							break;
 						}
 				}
 		}else{
 			for(final Player p : Bukkit.getServer().getOnlinePlayers())
 				if(!ignored.contains(p.getName())){
 					players.add(p);
 				}
 		}
 		final String tag = FrogAnnounce.getInstance().getTag();
 		for(final Player p : players){
 			for(final String s : this.text){
 				p.sendMessage(tag + (tag.isEmpty() ? "" : " ") + s);
 			}
 		}
 		if(FrogAnnounce.getInstance().isShowingConsoleAnnouncements()){
 			for(final String s : this.text){
 				Bukkit.getConsoleSender().sendMessage(tag + (tag.isEmpty() ? "" : " ") + s + ChatColor.DARK_GREEN + " [Announced to " + players.size() + " player(s)]");
 			}
 		}
 		if(!this.commands.isEmpty()){
 			for(final String command : this.commands){
 				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replaceFirst("/", ""));
 				if(FrogAnnounce.getInstance().isShowingConsoleAnnouncements()){
 					Bukkit.getConsoleSender().sendMessage("Running console command as part of announcement: " + command);
 				}
 			}
 		}
 	}
 
 	public String[] getText(){
 		return this.text.clone();
 	}
 
 	public Announcement(final String text, final List<String> groups, final List<String> worlds, final List<String> commands){
 		this.text = new String[text.split("&NEW_LINE;").length];
 		if(this.text.length < 2){
			this.text = new String[1];
 			this.text[0] = FrogAnnounce.colourizeText(text);
 		}else{
			for(int i = 0; i < text.length(); i++){
 				this.text[i] = FrogAnnounce.colourizeText(text.split("&NEW_LINE;")[i]);
 			}
 		}
 		if(groups != null){
 			this.groups = groups;
 		}else{
 			this.groups = new ArrayList<String>();
 		}
 		if(worlds != null){
 			this.worlds = worlds;
 		}else{
 			this.worlds = new ArrayList<String>();
 		}
 		if(commands != null){
 			this.commands = commands;
 		}else{
 			this.commands = new ArrayList<String>();
 		}
 	}
 }
