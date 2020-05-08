 package edu.unca.szhang.GameSpout;
 
 import net.minecraft.server.Material;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.material.*;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.getspout.spout.player.SpoutCraftPlayer;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.sound.SoundManager;
 
 import com.google.common.base.Joiner;
 
 public class GameSpoutCommandExecutor implements CommandExecutor {
 	private final GameSpout plugin;
 
 	/*
 	 * This command executor needs to know about its plugin from which it came
 	 * from
 	 */
 	public GameSpoutCommandExecutor(GameSpout plugin) {
 		this.plugin = plugin;
 	}
 
 	/*
 	 * On command set the sample message
 	 */
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 
 		if (args.length == 0) {
 			return false;
 		}     	
 		
 		else if (!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED
 					+ "you must be logged on to use these commands");
 			return false;
 
 		} 
 		
 		else if (args[0].equalsIgnoreCase("description")) {
 			Player player = (Player) sender;
 			player.sendMessage("Commands available: ");
 			player.sendMessage("/game camoflauge: blend into your surroundings!");
 			player.sendMessage("/game dispel: cancels camoflauge");
 			
 			return true;
 		}
 		
 		else if (args[0].equalsIgnoreCase("camoflauge")) {
 			Player player = (Player) sender;
 			SpoutCraftPlayer sp = (SpoutCraftPlayer) player;
 			
 			Location loc = sp.getLocation();
 			World world = loc.getWorld();
 			loc.setY(loc.getY() - 1);
 			org.bukkit.Material ground = world.getBlockAt(loc).getType();
 			
 			if (ground == org.bukkit.Material.GRASS) {
 				sp.setSkin("http://i46.tinypic.com/rlhr4n.png");
 			}
 			else if (ground == org.bukkit.Material.BEDROCK) {
 				sp.setSkin("http://i48.tinypic.com/33xyp1l.png");
 			}			
 			else if (ground == org.bukkit.Material.CLAY) {
 				sp.setSkin("http://i50.tinypic.com/33usbcx.png");
 			}
 			else if (ground == org.bukkit.Material.COBBLESTONE) {
 				sp.setSkin("http://i48.tinypic.com/rr5clv.png");
 			}
 			else if (ground == org.bukkit.Material.DIRT) {
 				sp.setSkin("http://i49.tinypic.com/347gnea.png");
 			}	
 			else if (ground == org.bukkit.Material.GRAVEL) {
 				sp.setSkin("http://i47.tinypic.com/5x8rvk.png");
 			}	
 			else if (ground == org.bukkit.Material.ICE) {
 				sp.setSkin("http://i50.tinypic.com/2co22xk.png");
 			}	
 			else if (ground == org.bukkit.Material.LAVA) {
 				sp.setSkin("http://i49.tinypic.com/2zt91c4.png");
 			}	
 			else if (ground == org.bukkit.Material.OBSIDIAN) {
 				sp.setSkin("http://i47.tinypic.com/2uhl0u0.png");
 			}	
 			else if (ground == org.bukkit.Material.PUMPKIN) {
 				sp.setSkin("http://i47.tinypic.com/r2l3co.png");
 			}	
 			else if (ground == org.bukkit.Material.SAND) {
 				sp.setSkin("http://i48.tinypic.com/317jjv4.png");
 			}	
 			else if (ground == org.bukkit.Material.SANDSTONE) {
 				sp.setSkin("http://i50.tinypic.com/16jmp9e.png");
 			}
 			else if (ground == org.bukkit.Material.SNOW) {
 				sp.setSkin("http://i47.tinypic.com/3brly.png");
 			}
 			else if (ground == org.bukkit.Material.STONE) {
 				sp.setSkin("http://i47.tinypic.com/1tvt4i.png");
 			}
 			else if (ground == org.bukkit.Material.WATER) {
 				sp.setSkin("http://i50.tinypic.com/51he9.png");
 			}			
 			else {
 				sp.setSkin("http://i47.tinypic.com/2gv4uc6.png");				
 			}
 			
 			return true;
 
 		} 
 		else if (args[0].equalsIgnoreCase("dispel")) {
 			Player player = (Player) sender;
 			SpoutCraftPlayer sp = (SpoutCraftPlayer) player;
 			sp.resetSkinFor(sp);
 			
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 }
