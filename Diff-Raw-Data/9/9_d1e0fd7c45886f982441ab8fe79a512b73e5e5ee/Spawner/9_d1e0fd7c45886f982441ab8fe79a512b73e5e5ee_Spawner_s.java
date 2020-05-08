 /*
 * Copyright (c) 2012 Sean Porter <glitchkey@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 
 package org.enderspawn;
 
 import java.lang.Runnable;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.List;
 
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.EntityType;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 
 public class Spawner implements Runnable
 {
 	private EnderSpawn plugin;
 	private int taskID;
 	
 	public Spawner(EnderSpawn plugin)
 	{
 		this.plugin = plugin;
 		taskID = -1;
 	}
 	
 	public void start()
 	{
 		if(taskID >= 0)
 			return;
 		
 		Timestamp currentTime 	= new Timestamp(new Date().getTime());
 		Timestamp lastDeath		= this.plugin.config.lastDeath;
 		
 		long spawnMinutes		= this.plugin.config.spawnMinutes;
 		
 		if(currentTime.getTime() >= (lastDeath.getTime() + (spawnMinutes * 60000)))
 		{
			run();
 			return;
 		}
 		
 		long timeRemaining = ((lastDeath.getTime() + (spawnMinutes * 60000)) - currentTime.getTime());
 		long ticksRemaining = (timeRemaining / 50);
 		
 		taskID = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, this, ticksRemaining);
 	}
 	
 	public void stop()
 	{
 		if(taskID < 0)
 			return;
 		
 		plugin.getServer().getScheduler().cancelTask(taskID);
 		taskID = -1;
 	}
 	
 	public void run()
 	{
 		List<World> worlds = plugin.getServer().getWorlds();
 		
 		for (World world : worlds)
 		{
 			if(world.getEnvironment() != World.Environment.valueOf("THE_END"))
 				continue;
 			
 			if(world.getEntitiesByClass(EnderDragon.class).size() >= plugin.config.maxDragons)
 				continue;
 			
 			Location location = new Location(world, 0, 128, 0);
 			world.spawnCreature(location, EntityType.ENDER_DRAGON);
 		}
 		
 		stop();
 	}
 }
