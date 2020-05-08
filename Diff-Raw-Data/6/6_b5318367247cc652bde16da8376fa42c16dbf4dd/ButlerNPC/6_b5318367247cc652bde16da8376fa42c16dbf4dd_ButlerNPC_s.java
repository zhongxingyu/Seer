 package net.loadingchunks.plugins.Leeroy.Types;
 
 import net.loadingchunks.plugins.Leeroy.Leeroy;
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import com.topcat.npclib.entity.HumanNPC;
 import com.topcat.npclib.nms.NpcEntityTargetEvent;
 
 /* When adding new NPC:
  *  
  */
 
 public class ButlerNPC extends BasicNPC
 {	
 	public String type = "leeroy_npcbutler";
 	public String hrtype = "butler";
 	public long bcastInitial = 20L;
 	public long bcastBetween = 500L;
 
 	public ButlerNPC(Leeroy plugin, String name, Location l, String id, String msg1, String msg2, String msg3, String msg4, boolean isnew, String world)
 	{
 		super(plugin, name, l, id, msg1, msg2, msg3, msg4, isnew, world, "butler", "leeroy_npcbutler");
 	}
 
 	@Override
 	public void SetBroadcast(final String msg)
 	{
 		final HumanNPC tmp = this.npc;
 		this.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(this.plugin, new Runnable() {
 			public void run() {
 				for(Entity e : tmp.getBukkitEntity().getNearbyEntities(50,50,50))
 				{
 					if(e instanceof Player)
 					{
 						Player p = (Player)e;
 						
 						p.sendMessage("<" + npc.getName() + "> Welcome to your homeworld, " + p.getDisplayName() + "!");
						p.sendMessage("<" + npc.getName() + "> If you would like to go to the lobby area from where you can");
						p.sendMessage("teleport to places across the mainland, right click me!");
 					}
 				}
 			}
 		}, 40L, 10000L);
 	}
 
 	// Player bounces (doesn't seem to work)
 	@Override
 	public void onBounce(Player player, EntityTargetEvent event)
 	{
 		return;
 	}
 
 	// Player near
 	@Override
 	public void onNear(Player player)
 	{
 		this.npc.lookAtPoint(player.getEyeLocation());
 	}
 
 	// Player right clicks
 	@Override
 	public void onRightClick(final Player player, NpcEntityTargetEvent event)
 	{
		player.sendMessage("<" + this.name + "> You'll be transported to the lobby in 5 seconds");
 		player.sendMessage("<" + this.name + "> Thank you for using the Chunky Transport System!");
 
 		this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new Runnable() {
 			public void run() {
 				player.teleport(plugin.mvcore.getMVWorldManager().getMVWorld(plugin.getConfig().getString("general.lobby")).getSpawnLocation());
 			}
 		},100L);
 	}
 
 	// When a player attacks
 	@Override
 	public void onPlayer(Player assailant, EntityDamageByEntityEvent event)
 	{
 		return;
 	}
 
 	// What's that coming over the hill
 	// Is it a monster?
 	// When a monster attacks
 	@Override
 	public void onMonster(Monster monster, EntityDamageByEntityEvent event)
 	{
 		return;
 	}
 
 }
