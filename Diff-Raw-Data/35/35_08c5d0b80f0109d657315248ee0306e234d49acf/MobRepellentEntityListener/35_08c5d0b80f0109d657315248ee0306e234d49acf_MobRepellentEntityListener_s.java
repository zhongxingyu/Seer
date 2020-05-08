 package mobrepellent;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Animals;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityListener;
 
 public class MobRepellentEntityListener extends EntityListener
 {
 	private MobRepellent plugin;
 	
 	public MobRepellentEntityListener( MobRepellent plugin )
 	{
 		this.plugin = plugin;
 	}
 	
 	public void onCreatureSpawn( CreatureSpawnEvent event )
 	{
 		if( !plugin.getConfig().shouldRepelNeutralMobs() &&
			( event.getEntity() instanceof Animals ) )
 			return;
 
 		Location loc = event.getLocation();
 		
 		if( plugin.getRepellerList().isRepelled( loc.getX(), loc.getY(), loc.getZ(), loc.getWorld() ) )
 		{
 			//this.plugin.getLogger().info(
 			//		"[MobRepellent] A mob has been repelled at " + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getWorld().getName()  );
 			event.setCancelled( true );
 		}
 	}
 }
