 package org.vanillaworld.CustomMusicDiscs;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Jukebox;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.Location;
 
 public class Main extends JavaPlugin implements Listener {
 	
 	public static JavaPlugin plugin;
 	public static List<Disc> discs;
 	Map<Location, Disc> playing = new HashMap<Location, Disc>();
 	
 	public void onEnable()
 	{
 		plugin = this;
 		Setup.setupFolders(); // Setup the folder structure
 		discs = Setup.getDiscs(); // Load discs
 		this.getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	public void onDisable()
 	{
 		discs = null;
 		plugin = null;
 	}
 	
 	@SuppressWarnings("deprecation")
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if(cmd.getName().equalsIgnoreCase("music"))
 		{
 			if(args.length == 3)
 			{
 				if(args[0].equalsIgnoreCase("give"))
 				{
 					Player p = Bukkit.getPlayerExact(args[1]);
 					if(p == null)
 					{
 						sender.sendMessage(ChatColor.RED + "The player must be online to give a music disc!");
 					}
 					else
 					{
 						if(discs.size() > 0)
 						{
 							for(Disc disc : discs)
 							{
 								if(disc.name.equalsIgnoreCase(args[2]))
 								{
 									p.getInventory().addItem(disc.disc);
 									p.updateInventory();
 									return true;
 								}
 							}
 						}
 						sender.sendMessage(ChatColor.RED + "That disc does not exist!");
 					}
 				}
 				else
 				{
 					sender.sendMessage(ChatColor.RED + "Invaild command. Correct usage: /music give <player> <disc>");
 				}
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + "To many/few args. Correct usage: /music give <player> <disc>");
 			}
 			return true;
 		}
 		return false; 
 	}
 	
 	@EventHandler (priority=EventPriority.HIGHEST)
 	private void PlayerInteract(PlayerInteractEvent event)
 	{
 		if(event.getClickedBlock() != null)
 		{
 			if(event.getClickedBlock().getType().equals(Material.JUKEBOX) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
 			{
 				final Jukebox jukebox = (Jukebox) event.getClickedBlock().getState();
 				if(playing.containsKey(jukebox.getLocation()))
 				{
 					jukebox.getLocation().getWorld().dropItem(jukebox.getLocation(), playing.get(jukebox.getLocation()).disc);
 					playing.get(jukebox.getLocation()).stop(jukebox.getLocation());
 					playing.remove(jukebox.getLocation());
 					return;
 				}
 				ItemStack item = event.getPlayer().getItemInHand();
 				if(item != null)
 				{
 					ItemMeta meta = item.getItemMeta();
 					if(meta.getLore() != null)
 					{
 						if(meta.getLore().contains("*Custom Disc*"))
 						{
 							String name = meta.getLore().get(1);
 							if(discs.size() > 0)
 							{
 								for(Disc disc : discs)
 								{
 									if((ChatColor.RESET + "" + ChatColor.GRAY + disc.name).equalsIgnoreCase(name))
 									{
 										this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 											  public void run() {
 											      jukebox.setPlaying(null);
 											  }
 											}, 2);
 										disc.play(jukebox.getLocation());
 										playing.put(jukebox.getLocation(), disc);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	private void BlockBreak(BlockBreakEvent event)
 	{
 		if(event.getBlock().getType().equals(Material.JUKEBOX))
 		{
 			Jukebox jukebox = (Jukebox) event.getBlock().getState();
 			if(playing.containsKey(jukebox.getLocation()))
 			{
 				jukebox.getLocation().getWorld().dropItem(jukebox.getLocation(), playing.get(jukebox.getLocation()).disc);
 				playing.get(jukebox.getLocation()).stop(jukebox.getLocation());
 				playing.remove(jukebox.getLocation());
 				return;
 			}
 		}
 	}
 
 }
