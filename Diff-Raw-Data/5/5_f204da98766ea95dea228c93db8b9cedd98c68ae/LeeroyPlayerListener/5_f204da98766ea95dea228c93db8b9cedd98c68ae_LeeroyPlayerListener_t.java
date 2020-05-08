 package net.loadingchunks.plugins.Leeroy;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.util.Vector;
 
 import net.loadingchunks.plugins.Leeroy.Types.*;
 
 public class LeeroyPlayerListener implements Listener {
 	
 	Leeroy plugin;
 	
 	public LeeroyPlayerListener(Leeroy plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event)
 	{			
 		if(!(event.getPlayer().getWorld().getName().startsWith("homeworld_")) || event.getPlayer().getGameMode() == GameMode.CREATIVE)
 		{
 			return;
 		}
 
 		Location pl = event.getPlayer().getLocation();
 
 		// Don't let players fall through the void.
 		if(pl.getY() < -10)
 		{
 			event.getPlayer().setFallDistance(0);
 			event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		if(event.getPlayer().getWorld().getName().startsWith("homeworld_") && event.getPlayer().getWorld().getName().equalsIgnoreCase("homeworld_" + event.getPlayer().getName()))
 		{
 			final Player p = event.getPlayer();
 			this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new Runnable() {
 				public void run() {
					p.teleport(plugin.mvcore.getMVWorldManager().getMVWorld("mainworld").getSpawnLocation());
 				}
 			},3L);
 		}
 
 		if(event.getPlayer().getWorld().getName().startsWith("homeworld_") && !(LeeroyUtils.hasNPC(this.plugin, event.getPlayer().getWorld().getName())))
 		{
 			Location nl = new Location(event.getPlayer().getWorld(), this.plugin.getConfig().getDouble("home.butler.x"), this.plugin.getConfig().getDouble("home.butler.y"), this.plugin.getConfig().getDouble("home.butler.z"));
 			this.plugin.npcs.spawn("butler",this.plugin.getConfig().getString("home.butler.name"), nl, "", "", "", "", false, event.getPlayer().getWorld().getName(), event.getPlayer().getWorld().getName() + "_butler");
 		}	
 	}
 	
 	@EventHandler
 	public void onPlayerChangeWorld(PlayerChangedWorldEvent event)
 	{
 		if(event.getFrom().getName().startsWith("homeworld_"))
 		{
 			if(LeeroyUtils.hasNPC(this.plugin, event.getFrom().getName()) && this.plugin.NPCList.containsKey(event.getPlayer().getWorld().getName() + "_butler") && event.getFrom().getPlayers().isEmpty())
 			{
 				((ButlerNPC)this.plugin.NPCList.get(event.getFrom().getName() + "_butler")).npc.removeFromWorld();
 				this.plugin.NPCList.remove(event.getFrom().getName() + "_butler");
 			}
 		}
 		
 		if(event.getPlayer().getWorld().getName().startsWith("homeworld_"))
 		{
 			if(!LeeroyUtils.hasNPC(this.plugin, event.getPlayer().getWorld().getName()))
 			{
 				Location nl = new Location(event.getPlayer().getWorld(), this.plugin.getConfig().getDouble("home.butler.x"), this.plugin.getConfig().getDouble("home.butler.y"), this.plugin.getConfig().getDouble("home.butler.z"));
 				this.plugin.npcs.spawn("butler",this.plugin.getConfig().getString("home.butler.name"), nl, "", "", "", "", false, event.getPlayer().getWorld().getName(), event.getPlayer().getWorld().getName() + "_butler");
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerTeleport(PlayerTeleportEvent event)
 	{
 		if(event.getTo().getWorld().getName().startsWith("homeworld_"))
 		{
 			if(!LeeroyUtils.hasNPC(this.plugin, event.getTo().getWorld().getName()))
 			{
 				Location nl = new Location(event.getTo().getWorld(), this.plugin.getConfig().getDouble("home.butler.x"), this.plugin.getConfig().getDouble("home.butler.y"), this.plugin.getConfig().getDouble("home.butler.z"));
 				this.plugin.npcs.spawn("butler",this.plugin.getConfig().getString("home.butler.name"), nl, "", "", "", "", false, event.getTo().getWorld().getName(), event.getTo().getWorld().getName() + "_butler");
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		if(event.getPlayer().getWorld() != null && this.plugin.NPCList.containsKey(event.getPlayer().getWorld().getName() + "_butler") && event.getPlayer().getWorld().getPlayers().isEmpty())
 		{
 			((ButlerNPC)this.plugin.NPCList.get(event.getPlayer().getWorld().getName() + "_butler")).npc.removeFromWorld();
 			this.plugin.NPCList.remove(event.getPlayer().getWorld().getName() + "_butler");
 		}
 	}
 	
 	// Stop players doing stuff
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		
 	}
 }
