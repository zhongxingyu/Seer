 /************************************************************************
  * This file is part of FunCommands.
  *
  * FunCommands is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ExamplePlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with FunCommands.  If not, see <http://www.gnu.org/licenses/>.
  ************************************************************************/
 
 package de.Lathanael.FC.FunCommands;
 
 import java.util.HashMap;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 
 import de.Lathanael.FC.Commands.ACVoid;
 import de.Lathanael.FC.Commands.Attaint;
 import de.Lathanael.FC.Commands.Entomb;
 import de.Lathanael.FC.Commands.Rocket;
 import de.Lathanael.FC.Commands.Slap;
 import de.Lathanael.FC.Listeners.FCPlayerListener;
 import de.Lathanael.FC.Tools.BlocksOld;
 import be.Balor.Manager.LocaleManager;
 import be.Balor.Manager.Permissions.PermParent;
 import be.Balor.Tools.ACLogger;
 import be.Balor.Tools.Utils;
 import be.Balor.bukkit.AdminCmd.ACPluginManager;
 import be.Balor.bukkit.AdminCmd.AbstractAdminCmdPlugin;
 
 
 /**
  * @author Lathanael (aka Philippe Leipold)
  *
  */
 public class FunCommands extends AbstractAdminCmdPlugin {
 
 	public static HashMap<Player, BlocksOld> blockStates;
 	public static HashMap<String, Player> players;
 	private Configuration config;
 	private static FCPlayerListener fcPL = new FCPlayerListener();
 	private static PluginManager pm;
 
 	/**
 	 * @param name
 	 */
 	public FunCommands() {
 		super("FunCommands");
 	}
 
 	@Override
 	public void registerCmds() {
 		ACPluginManager.registerCommand(Slap.class);
 		ACPluginManager.registerCommand(Rocket.class);
 		ACPluginManager.registerCommand(Entomb.class);
 		ACPluginManager.registerCommand(ACVoid.class);
 		ACPluginManager.registerCommand(Attaint.class);
 	}
 
 	@Override
 	protected void registerPermParents() {
 		permissionLinker.addPermParent(new PermParent("admincmd.fun.*"));
 		permissionLinker.setMajorPerm(new PermParent("admincmd.*"));
 	}
 
 	@Override
 	protected void setDefaultLocale() {
 		Utils.addLocale("slapSender", ChatColor.DARK_AQUA + "You have slapped " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("slapTarget", ChatColor.DARK_AQUA + "You have been slapped by " + ChatColor.GOLD
 				+ "%sender" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("slapYourself", ChatColor.DARK_AQUA + "You have slapped " + ChatColor.GOLD
 				+ "yourself" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("attaintYourself", ChatColor.DARK_AQUA + "You changed your Displayname to:" + ChatColor.DARK_RED
 				+ " %name");
 		Utils.addLocale("attaintTarget", ChatColor.DARK_AQUA+  "Your Displayname has been changed by" + ChatColor.GOLD
 				+ " %sender" + ChatColor.DARK_AQUA + " to:" + ChatColor.DARK_RED + "%name");
 		Utils.addLocale("attaintSender", ChatColor.DARK_AQUA + "You have changed the Displayname of" + ChatColor.GOLD
 				+ " %target" + ChatColor.DARK_AQUA + " to:" + ChatColor.DARK_RED +" %name");
 		Utils.addLocale("attaintShowName", ChatColor.DARK_AQUA + "The Displayname of " + ChatColor.GOLD + "%name"
 				+ ChatColor.DARK_AQUA + " is:" + ChatColor.DARK_RED	+ " %dname");
 		Utils.addLocale("entombSender", ChatColor.DARK_AQUA + "You have entombed " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("entombTarget", ChatColor.DARK_AQUA + "You have been entombed by " + ChatColor.GOLD
 				+ "%sender" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("entombYourself", ChatColor.DARK_AQUA + "You have entombed yourself!");
 		Utils.addLocale("voidSender", ChatColor.DARK_AQUA + "You have dropped " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + " into the " + ChatColor.RED +"VOID" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("voidTarget", ChatColor.DARK_AQUA + "You have been dropped into the " + ChatColor.RED + "VOID "
 				+ ChatColor.DARK_AQUA + "by " + ChatColor.GOLD + "%sender" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("voidYourself", ChatColor.DARK_AQUA + "You have dropped yourself into the " + ChatColor.RED + "VOID"
 				+ ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("rocketTarget", ChatColor.DARK_AQUA + "You have shot " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + " high into the air!");
 		Utils.addLocale("rocketSender", ChatColor.DARK_AQUA + "You have been shot high into the air by " + ChatColor.GOLD
 				+ "%sender" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("rocketYourself", ChatColor.DARK_AQUA + "You have shot yourself into the air!");
 		LocaleManager.getInstance().save();
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		config = Configuration.getInstance();
 		config.setInstance(this);
 		players = new HashMap<String, Player>();
 		blockStates = new HashMap<Player, BlocksOld>();
 		pm = getServer().getPluginManager();
 		pm.registerEvent(Type.PLAYER_KICK, fcPL, Priority.Monitor, this);
 		pm.registerEvent(Type.PLAYER_QUIT, fcPL, Priority.Monitor, this);
 		PluginDescriptionFile pdfFile = this.getDescription();
 		permissionLinker.registerAllPermParent();
		ACLogger.info("[" + pdfFile.getName() +"] Enabled. (Version " + pdfFile.getVersion() + ")");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.bukkit.plugin.Plugin#onDisable()
 	 */
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
		ACLogger.info("[" + pdfFile.getName() +"] Disabled. (Version " + pdfFile.getVersion() + ")");
 	}
 }
