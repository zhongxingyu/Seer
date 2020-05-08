 package net.loadingchunks.plugins.Leeroy;
 
 import java.util.List;
 import java.util.Map.Entry;
 
 import net.loadingchunks.plugins.Leeroy.Types.BasicNPC;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.MetadataValue;
 
 import com.topcat.npclib.entity.HumanNPC;
 
 public class LeeroyCommands implements CommandExecutor
 {
 	public Leeroy plugin;
 
 	public LeeroyCommands(Leeroy plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{	
 		if(cmd.getName().equalsIgnoreCase("leeroy"))
 		{
 			this.plugin.log.info("[LEEROY] Got Leeroy Command Hook!");
 
 			if(args[0].equalsIgnoreCase("spawn"))
 			{
 				String name;
 				
 				if(args.length == 3)
 					name = args[2];
 				else
 				{
 					return false;
 				}
 
 				this.plugin.log.info("[LEEROY] Got Spawn!");
 
 				if(!(sender instanceof Player))
 				{
 					sender.sendMessage("[LEEROY] This command can only be used in-game.");
 					return false;
 				}
 
 				if(!LeeroyPermissions.canSpawn((Player)sender))
 				{
 					sender.sendMessage("[LEEROY] This command is op-only!");
 					return false;
 				}
 
 				this.plugin.log.info("[LEEROY] Init Handler.");
 				LeeroyNPCHandler npc = new LeeroyNPCHandler(this.plugin);
 				
 				for(Entry<String, Object> n : this.plugin.NPCList.entrySet())
 				{
 					BasicNPC np = (BasicNPC)n.getValue();
 
 					if(np.name.equalsIgnoreCase(name) && np.npc.getBukkitEntity().getWorld().getName().equalsIgnoreCase(((Player)sender).getWorld().getName()) )
 					{
 						sender.sendMessage("[LEEROY] An NPC already exists within this world with that name.");
 						return true;
 					}
 				}
 				
 				this.plugin.log.info("[LEEROY] Spawning...");
 				npc.spawn(args[1], name, ((Player)sender).getLocation(), "", "", "", "", true, ((Player)sender).getWorld().getName(), null);
 
 				return true;
 			} else if(args[0].equalsIgnoreCase("angle") && sender instanceof Player)
 			{
 				sender.sendMessage("You Yaw is " + ((Player)sender).getLocation().getYaw() + " and your pitch is " + ((Player)sender).getLocation().getPitch() + ".");
 			}
 			else if(args[0].equalsIgnoreCase("kill"))
 			{
 				String name;
 				Integer radius = 5;
 				
 				if(!(sender instanceof Player))
 					return false;
 				
 				if(!LeeroyPermissions.canKill((Player)sender))
 				{
 					sender.sendMessage("[LEEROY] This command is op-only!");
 					return false;
 				}
 
 				if(args.length < 2)
 					return false;
 				
 				name = args[1];
 
 				if(args.length == 3)
 				{
 					radius = Integer.parseInt(args[2]);
 				}
 				
 				Player p = (Player)sender;
 				
 				for(Entity e : p.getNearbyEntities(radius, radius, radius))
 				{
 					if(e instanceof HumanEntity)
 					{
 						List<MetadataValue> md = e.getMetadata("leeroy_id");
 						List<MetadataValue> mdtype = e.getMetadata("leeroy_type");
 
 						if(md.size() > 0)
 						{
 							if(((BasicNPC)this.plugin.NPCList.get(md.get(0).asString())).name.equalsIgnoreCase(name) || name.equalsIgnoreCase("*"))
 							{
 								this.plugin.log.info("[LEEROY] Killing NPC with ID: " + md.get(0).asString());
 								this.plugin.npcs.remove(md.get(0).asString());
 							}
 						}
 					}
 				}
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 }
