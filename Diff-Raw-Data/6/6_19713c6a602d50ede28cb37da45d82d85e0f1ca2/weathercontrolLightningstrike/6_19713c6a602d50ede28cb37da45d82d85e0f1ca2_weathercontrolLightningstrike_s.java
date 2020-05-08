 package net.craftrepo.WeatherControl;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.event.weather.LightningStrikeEvent;
 import org.bukkit.event.weather.WeatherListener;
 
 public class weathercontrolLightningstrike extends WeatherListener {
 	private final weathercontrol plugin;
 	private Integer area = 20;
 	public weathercontrolLightningstrike(weathercontrol plugin) {
 		super();
 		this.plugin = plugin;
 	}
 	@Override
 	public void onLightningStrike(LightningStrikeEvent event) {
 		Location strike = event.getLightning().getLocation();
 		if(!plugin.lightning.contains(strike))
 		{
 			int highestWeight=0;
 			List<Location> highesLocations=new LinkedList<Location>();
 			World w= strike.getWorld();
 			for(int x=strike.getBlockX()-(area/2);x<=strike.getBlockX()+(area/2);x++)
 			{
 				for(int z=strike.getBlockZ()-(area/2);z<=strike.getBlockZ()+(area/2);z++)
 				{
 					int height = w.getHighestBlockYAt(x, z);
 					int weight = height;
 					if(w.getBlockTypeIdAt(x, height-1, z)==42)
 					{
 						weight+=2;
 					}
 					//TODO Make it scale so that if it's further away from the original source it has less weight
 					//TODO Add more "Targets" to the lightning
 					if(weight>highestWeight)
 					{
 						highestWeight=weight;
 						highesLocations.clear();
 					}
 					if(weight==highestWeight)
 					{
 						highesLocations.add(new Location(w, x, height, z));
 					}
 				}
 			}
 			//if(strike.getBlockY()<highestLocation.getBlockY())
 			{
 				Random nr= new Random();
 				int index = nr.nextInt(highesLocations.size());
 				//TODO Power redstone if close to lightning strike area
 				event.getLightning().teleport(highesLocations.get(index));
				plugin.lightning.remove(event.getLightning());
 			}
 		}
 		super.onLightningStrike(event);
 	}
 
 }
