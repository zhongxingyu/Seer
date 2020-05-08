 /*
  * This file is part of Plugin.
  *
  * Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
 * along with SaveStopper.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Author: Robert 'Bobby' Zenz
  * Website: http://www.bonsaimind.org
  * GitHub: https://github.com/RobertZenz/org.bonsaimind.bukkitplugins/tree/master/Plugin
  * E-Mail: bobby@bonsaimind.org
  */
 package org.bonsaimind.bukkitplugins.SaveStopper;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import org.bukkit.Server;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * 
  * @author Robert 'Bobby' Zenz
  */
 public class Plugin extends JavaPlugin {
 
 	private static final String CONFIG_FILE = "./plugins/SaveStopper/config.yml";
 	private Server server = null;
 	private boolean isSaving = true;
 	private Timer timer = new Timer(true);
 	private PlayerListener listener = new PlayerListener(this);
 	private Settings settings;
 
 	public void onDisable() {
 		settings.save(CONFIG_FILE);
 
 		timer.cancel();
 		timer = null;
 
 		listener = null;
 
 		server = null;
 	}
 
 	public void onEnable() {
 		server = getServer();
 
 		PluginManager pm = server.getPluginManager();
 		pm.registerEvent(Type.PLAYER_LOGIN, listener, Priority.Monitor, this);
 		pm.registerEvent(Type.PLAYER_QUIT, listener, Priority.Monitor, this);
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled.");
 
 		settings = new Settings();
 		settings.load(CONFIG_FILE);
 
 		if (settings.getDisableOnStart()) {
 			internalDisable();
 		}
 	}
 
 	/**
 	 * Enable saving.
 	 */
 	protected void enable() {
 		if (server.getOnlinePlayers().length == 0 && isSaving) {
 			System.out.println("SaveStopper: Canceling scheduled disabling...");
 			timer.purge();
 		}
 
 		if (!isSaving) {
 			System.out.println("SaveStopper: Enabling saving...");
 			CommandHelper.queueConsoleCommand(server, "save-on");
 			isSaving = true;
 		}
 	}
 
 	/**
 	 * Disable saving, check if we should use the timer or not.
 	 */
 	protected void disable() {
 		if (isSaving && server.getOnlinePlayers().length <= 1) {
 			if (settings.getWait() > 0) {
 				System.out.println("SaveStopper: Scheduling disabling in " + Long.toString(settings.getWait()) + " seconds...");
 
 				timer.schedule(new TimerTask() {
 
 					@Override
 					public void run() {
 						internalDisable();
 					}
 				}, settings.getWait() * 1000);
 			} else {
 				internalDisable();
 			}
 		}
 	}
 
 	/**
 	 * Disable saving.
 	 */
 	private void internalDisable() {
 		if (isSaving && server.getOnlinePlayers().length == 0) {
 			System.out.println("SaveStopper: Disabling saving...");
 
 			if (settings.getSaveAll()) {
 				CommandHelper.queueConsoleCommand(server, "save-all");
 			}
 
 			CommandHelper.queueConsoleCommand(server, "save-off");
 
 			isSaving = false;
 		}
 	}
 }
