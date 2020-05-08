 package me.sinnoh.MasterPromote;
 
 
 
 import me.sinnoh.MasterPromote.Events.PlayerPromoteEvent;
 import me.sinnoh.MasterPromote.Events.PlayerPromoteEvent.PROMOTIONTYPE;
 
 import org.anjocaido.groupmanager.GroupManager;
 import org.anjocaido.groupmanager.data.Group;
 import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
 import org.bukkit.Bukkit;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 
 import ru.tehkode.permissions.PermissionGroup;
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class MasterPromotePermissions 
 {
 	
 	public static MasterPromote plugin = MasterPromote.instance;
 	public static String activePermissions;
 	public static Plugin pex = Bukkit.getPluginManager().getPlugin("PermissionsEx");
 	public static Plugin gm = Bukkit.getPluginManager().getPlugin("GroupManager");
 	public static Plugin pb = Bukkit.getPluginManager().getPlugin("PermissionsBukkit");
 	public static Plugin bp = Bukkit.getPluginManager().getPlugin("bPermissions");
 	public static Plugin pr = Bukkit.getPluginManager().getPlugin("Privileges");
 	public static Plugin yp = Bukkit.getPluginManager().getPlugin("YAPP");
 	public static PlayerPromoteEvent event;
 	
 	public static void loadPermission()
 	{
 		if(Bukkit.getPluginManager().isPluginEnabled(pex))
 		{
 			MasterPromotePermissions.activePermissions = "PermissionsEx";
 		}
 		else if(Bukkit.getPluginManager().isPluginEnabled(gm))
 		{
 			MasterPromotePermissions.activePermissions = "GroupManager";
 		}
 		else if(Bukkit.getPluginManager().isPluginEnabled(pb))
 		{
 			MasterPromotePermissions.activePermissions = "PermissionsBukkit";
 		}
 		else if(Bukkit.getPluginManager().isPluginEnabled(bp))
 		{
 			MasterPromotePermissions.activePermissions = "bPermissions";
 		}
 		else if(Bukkit.getPluginManager().isPluginEnabled(pr))
 		{
 			MasterPromotePermissions.activePermissions = "Privileges";
 		}
 		else if(Bukkit.getPluginManager().isPluginEnabled(yp))
 		{
 			MasterPromotePermissions.activePermissions = "YAPP";
 		}
 		else
 		{
 			MasterPromotePermissions.activePermissions = "none";
 		}
 	}
 	
 	public static void promote(Player player, String group, PROMOTIONTYPE type)
 	{
 		event = new PlayerPromoteEvent(player, group, type, activePermissions);
 		Bukkit.getPluginManager().callEvent(event);
 		if(event.isCanceled())
 		{
 			sUtil.log("PlayerPromotionEvent has been canceled");
 			return;
 		}
 		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
 		if(!plugin.config.getString("PromoteSyntax").equalsIgnoreCase("none"))
 		{
 			String command = plugin.config.getString("PromoteSyntax");
 			command = command.replace("<player>", player.getName());
 			command = command.replace("<group>", group);
 			Bukkit.dispatchCommand(console, command);
 		}
 		else if(activePermissions.equals("PermissionsEx"))
 		{
 			//Bukkit.dispatchCommand(console, "pex user " + player.getName() + " group set " + group);
 			PermissionUser pexuser = PermissionsEx.getUser(player);
 			for(PermissionGroup g : pexuser.getGroups())
 			{
 				pexuser.removeGroup(g);
 			}
 			pexuser.addGroup(group);
 		}
 		else if(activePermissions.equals("GroupManager"))
 		{
 			//Bukkit.dispatchCommand(console, "manuadd " + player.getName() + " " + group);
 			GroupManager gm = (GroupManager) plugin.getServer().getPluginManager().getPlugin("GroupManager");
 			OverloadedWorldHolder perm = gm.getWorldsHolder().getWorldData(player);
 			Group g = perm.getGroup(group);
			perm.getUser(player.getName()).setGroup(g);
 			perm.reload();
 		}
 		else if(activePermissions.equals("PermissionsBukkit"))
 		{
 			Bukkit.dispatchCommand(console, "permissions player setgroup " + player.getName() + " " + group);
 		}
 		else if(activePermissions.equals("bPermissions"))
 		{
 			Bukkit.dispatchCommand(console, "world " + player.getWorld().getName());
 			Bukkit.dispatchCommand(console, "user " + player.getName());
 			Bukkit.dispatchCommand(console, "user setgroup " + group);
 		}
 		else if(activePermissions.equals("Privileges"))
 		{
 			Bukkit.dispatchCommand(console, "priv group set " + player.getName() + " " + group);
 		}
 		else if(activePermissions.equalsIgnoreCase("YAPP"))
 		{
 			Bukkit.dispatchCommand(console, "yapp o:" + player.getName());
 			Bukkit.dispatchCommand(console, "yapp +g "  + group);
 			Bukkit.dispatchCommand(console, "yapp @");
 		}
 	}
 
 
 }
