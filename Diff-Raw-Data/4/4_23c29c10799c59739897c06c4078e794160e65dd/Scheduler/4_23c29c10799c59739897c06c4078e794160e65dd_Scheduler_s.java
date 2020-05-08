 /*************************************************************************
  * Copyright (C) 2012 Philippe Leipold
  *
  * EntityCleaner is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * EntityCleaner is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with EntityCleaner. If not, see <http://www.gnu.org/licenses/>.
  *
  **************************************************************************/
 
 package de.Lathanael.EC.Utils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import be.Balor.Tools.MaterialContainer;
 import be.Balor.Tools.Utils;
 import be.Balor.Tools.Debug.ACLogger;
 import be.Balor.Tools.Exceptions.InvalidInputException;
 import de.Lathanael.EC.Main.EntityCleaner;
 import de.Lathanael.EC.Tasks.AnimalTask;
 import de.Lathanael.EC.Tasks.ArrowTask;
 import de.Lathanael.EC.Tasks.BoatTask;
 import de.Lathanael.EC.Tasks.CartTask;
 import de.Lathanael.EC.Tasks.CompleteTask;
 import de.Lathanael.EC.Tasks.FloatingItemsTask;
 import de.Lathanael.EC.Tasks.MonsterTask;
 import de.Lathanael.EC.Tasks.OrbTask;
 import de.Lathanael.EC.Tasks.VehicleTask;
 import de.Lathanael.EC.Tasks.VillagerTask;
 
 /**
  * @author Lathanael (aka Philippe Leipold)
  *
  */
 public class Scheduler {
 
 	public static HashMap<String, Integer> taskIDs = new HashMap<String, Integer>();
 	public static HashMap<String, TaskContainer> tasks = new HashMap<String, TaskContainer>();
 	public Server server;
 	public List<World> worlds = new ArrayList<World>();
 	public HashMap<String, List<Material>> items = new HashMap<String, List<Material>>();
 	private EntityCleaner instance;
 
 	public Scheduler(Server server, EntityCleaner instance) {
 		this.server = server;
 		this.instance = instance;
 		Set<String> list = ECConfig.getConfig().getKeys(false);
 		list.remove("debugMsg");
 		Tools.debugMsg("Loading worlds...");
 		for (String world : list) {
 			if (EntityCleaner.debug)
 				Tools.debugMsg("Adding world: " + server.getWorld(world).getName());
 			worlds.add(server.getWorld(world));
 			List<Material> item = new ArrayList<Material>();
 			List<String> itemName = ECConfig.getStringList(world + ".item.list");
 			for(String s : itemName) {
 				MaterialContainer m = null;
 				try {
 					m = Utils.checkMaterial(s);
 				} catch (InvalidInputException e) {
 					final HashMap<String, String> replace = new HashMap<String, String>();
 					replace.put("material", s);
 					ACLogger.Log(Utils.I18n("unknownMat", replace));
 				}
 				if (m.isNull()) {
 					continue;
 				}
 				Material mat = m.getMaterial();
 				if (mat != null)
 					item.add(mat);
 			}
 			items.put(world, item);
 		}
 		initTaskList();
 	}
 
 	public void startTasks() {
 		for (Map.Entry<String, TaskContainer> map : tasks.entrySet()) {
 			TaskContainer container = map.getValue();
 			if (container.isEnabled())
 				startTask(container.getTask(), map.getKey(), container.getInitTIme(), container.getTime());
 		}
 	}
 
 	public void startTask(Runnable task, String taskName, long initTime, long time) {
 		taskIDs.put(taskName, server.getScheduler().scheduleSyncRepeatingTask(
 				instance, task, initTime, time));
 	}
 
 	public void startTask(String task) {
 		TaskContainer container = tasks.get(task);
 		taskIDs.put(task, server.getScheduler().scheduleSyncRepeatingTask(
 				instance, container.getTask(), container.getInitTIme(), container.getTime()));
 	}
 
 	public void stopTasks() {
 		for (Map.Entry<String, Integer> entries : taskIDs.entrySet())
 			stopTask(entries.getKey(), entries.getValue());
 	}
 
 	public void stopTask(String taskName, int id) {
 		server.getScheduler().cancelTask(id);
 		taskIDs.remove(taskName);
 	}
 
 	public void stopTask(String className) {
 		int id = taskIDs.get(className);
 		server.getScheduler().cancelTask(id);
 		taskIDs.remove(className);
 	}
 
 	public void restartTask(String task) {
 		int id = taskIDs.get(task);
 		server.getScheduler().cancelTask(id);
 		taskIDs.remove(task);
 		startTask(task);
 	}
 
 	public void reInitTaskList() {
 		tasks.clear();
 		initTaskList();
 	}
 
 	//---------------------------------private functons-----------------------------------------
 
 	private void initTaskList() {
 		for (World world : worlds) {
 			String name = world.getName();
 			tasks.put(name + ".cart", new TaskContainer((Runnable) new CartTask(world), (long) (ECConfig.getDouble(name + ".cart.inittime", 0.5D)*20*60),
 					(long) (ECConfig.getDouble(name + ".cart.time", 5D)*20*60), ECConfig.getBoolean(name + ".cart.enable"),
 					ECConfig.getBoolean(name + ".cart.protect"), ECConfig.getBoolean(name + ".cart.passenger")));
 
 			tasks.put(name + ".boat", new TaskContainer((Runnable) new BoatTask(world), (long) (ECConfig.getDouble(name + ".boat.inittime", 0.5D)*20*60),
 					(long) (ECConfig.getDouble(name + ".boat.time", 5D)*20*60), ECConfig.getBoolean(name + ".boat.enable"),
 					ECConfig.getBoolean(name + ".boat.protect"), ECConfig.getBoolean(name + ".boat.passenger")));
 
 			tasks.put(name + ".arrow", new TaskContainer((Runnable) new ArrowTask(world), (long) (ECConfig.getDouble(name + ".arrow.inittime", 0.5D)*20*60),
 					(long) (ECConfig.getDouble(name + ".arrow.time", 5D)*20*60), ECConfig.getBoolean(name + ".arrow.enable"), false, false));
 
 			tasks.put(name + ".animal", new TaskContainer((Runnable) new AnimalTask(world), (long) (ECConfig.getDouble(name + ".animal.inittime", 0.5D)*20*60),
 					(long) (ECConfig.getDouble(name + ".animal.time", 5D)*20*60), ECConfig.getBoolean(name + "animal.enable"), false, false));
 
 			tasks.put(name + ".orb", new TaskContainer((Runnable) new OrbTask(world), (long) (ECConfig.getDouble(name + ".orb.inittime", 0.5D)*20*60),
 					(long) (ECConfig.getDouble(name + ".orb.time", 5D)*20*60), ECConfig.getBoolean(name + ".orb.enable"), false, false));
 
 			tasks.put(name + ".monster", new TaskContainer((Runnable) new MonsterTask(world), (long) (ECConfig.getDouble(name + ".monster.inittime", 0.5D)*20*60),
 					(long) (ECConfig.getDouble(name + ".monster.time", 5D)*20*60), ECConfig.getBoolean(name + ".monster.enable"), false, false));
 
 			tasks.put(name + ".villager", new TaskContainer((Runnable) new VillagerTask(world), (long) (ECConfig.getDouble(name + ".villager.inittime", 0.5D)*20*60),
 					(long) (ECConfig.getDouble(name + ".villager.time", 5D)*20*60), ECConfig.getBoolean(name + ".villager.enable"), false, false));
 
 			tasks.put(name + ".item", new TaskContainer((Runnable) new FloatingItemsTask(world, items.get(world)), (long) (ECConfig.getDouble(name + ".item.inittime", 0.5D)*20*60),
 					(long) (ECConfig.getDouble(name + ".item.time", 5D)*20*60), ECConfig.getBoolean(name + ".item.enable"), false, false));
 
 			tasks.put(name + ".vehicle", new TaskContainer((Runnable) new VehicleTask(world), (long) (ECConfig.getDouble(name + ".vehicle.inittime", 0.5D)*20*60),
 					(long) (ECConfig.getDouble(name + ".vehicle.time", 5D)*20*60), ECConfig.getBoolean(name + ".vehicle.enable"),
 					ECConfig.getBoolean(name + ".vehicle.protect"), ECConfig.getBoolean(name + ".vehicle.passenger")));
 
 			tasks.put(name + ".all", new TaskContainer((Runnable) new CompleteTask(world), (long) (ECConfig.getDouble(name + ".all.inittime", 0.5D)*20*60),
 					(long) (ECConfig.getDouble(name + ".all.time", 5D)*20*60), ECConfig.getBoolean(name + ".all.enable"),
 					ECConfig.getBoolean(name + ".all.protect"), ECConfig.getBoolean(name + ".all.passenger")));		}
 	}
 }
