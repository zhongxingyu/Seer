 /*
  * This file is part of SimpleCronClone.
  *
  * Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SimpleCronClone is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SimpleCronClone.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonsaimind.bukkitplugins.simplecronclone;
 
 import java.io.File;
 import java.io.IOException;
 
import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * The main Plugin class.
  */
 public class Plugin extends JavaPlugin {
 
 	private Server server;
 	private Engine engine;
 
 	@Override
 	public void onDisable() {
 		engine.stop();
 		engine = null;
 	}
 
 	@Override
 	public void onEnable() {
 		server = getServer();
 
 		PluginDescriptionFile pdfFile = this.getDescription();
		getLogger().log(Level.INFO, "{0} {1} is enabled.", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
 
 		engine = new Engine(server, new File("plugins/SimpleCronClone/"), getLogger());
 		engine.start();
 
 		setCommands();
 
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException ex) {
 			//we don't want to complain loudly if we can't submit stats, just the one line is enough.
 			getLogger().info("SimpleCronClone: Failed to submit the stats. :-(");
 		}
 	}
 
 	private void setCommands() {
 		getCommand("simplecronclone").setExecutor(new CommandExecutor() {
 
 			@Override
 			public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
 				if (!sender.isOp()) {
 					sender.sendMessage("I'm sorry, Dave. I'm afraid I can't do that.");
 					return true;
 				}
 
 				if (args.length == 0) {
 					return false;
 				}
 
 				for (int idx = 0; idx < args.length; idx++) {
 					String arg = args[idx];
 
 					if (arg.equalsIgnoreCase("exec")) {
 						for (int scriptIdx = idx + 1; scriptIdx < args.length; scriptIdx++) {
 							String script = args[scriptIdx];
 
 							if (!script.endsWith(".scc")) {
 								script += ".scc";
 							}
 
 							// Create threaded execution environment so that we don't block the main thread.
 							// This basically replicates how it would work from cron4j.
 
 							final String finalScript = script;
 							Thread t = new Thread(new Runnable() {
 
 								public void run() {
 									if (engine.executeScript(new File("plugins/SimpleCronClone/" + finalScript))) {
 										Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
 												Bukkit.getServer().getPluginManager().getPlugin("SimpleCronClone"), new Runnable() {
 
 											public void run() {
 												sender.sendMessage("SimpleCronClone: Executed \"plugins/SimpleCronClone/" + finalScript + "\".");
 											}
 										});
 
 									} else {
 										Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
 												Bukkit.getServer().getPluginManager().getPlugin("SimpleCronClone"), new Runnable() {
 
 											public void run() {
 												sender.sendMessage("SimpleCronClone: Error while executing \"plugins/SimpleCronClone/" + finalScript + "\".");
 											}
 										});
 
 									}
 								}
 							});
 							t.start();
 						}
 					} else if (arg.equalsIgnoreCase("restart")) {
 						engine.stop();
 						engine.start();
 						sender.sendMessage("SimpleCronClone: Restarted.");
 					} else if (arg.equalsIgnoreCase("stop")) {
 						engine.stop();
 						sender.sendMessage("SimpleCronClone: HALTED!");
 						sender.sendMessage("SimpleCronClone: Use \"/simplecronclone restart\" to restart it.");
 					}
 				}
 
 				return true;
 			}
 		});
 	}
 }
