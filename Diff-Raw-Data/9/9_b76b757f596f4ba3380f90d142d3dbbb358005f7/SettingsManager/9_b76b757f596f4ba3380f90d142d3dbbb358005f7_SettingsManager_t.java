 package org.monstercraft.support.plugin.managers;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.monstercraft.support.MonsterTickets;
 import org.monstercraft.support.plugin.Configuration;
 import org.monstercraft.support.plugin.Configuration.Variables;
 import org.monstercraft.support.plugin.command.commands.Close;
 import org.monstercraft.support.plugin.util.Status;
 import org.monstercraft.support.plugin.wrappers.HelpTicket;
 
 /**
  * This class contains all of the plugins settings.
  * 
  * @author fletch_to_99 <fletchto99@hotmail.com>
  * 
  */
 public class SettingsManager {
 	private MonsterTickets plugin = null;
 
 	private String SETTINGS_PATH = null;
 
 	private String SETTINGS_FILE = "Config.yml";
 
 	/**
 	 * Creates an instance of the Settings class.
 	 * 
 	 * @param plugin
 	 *            The parent plugin.
 	 * @throws InvalidConfigurationException
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 */
 	public SettingsManager(MonsterTickets plugin) {
 		this.plugin = plugin;
 		this.SETTINGS_PATH = plugin.getDataFolder().getAbsolutePath();
 		load();
 		loadTicketsConfig();
 	}
 
 	private void save(final FileConfiguration config, final File file) {
 		try {
 			config.save(file);
 		} catch (IOException e) {
 			Configuration.debug(e);
 		}
 	}
 
 	/**
 	 * This method loads the plugins configuration file.
 	 */
 	public void save() {
 		final FileConfiguration config = this.plugin.getConfig();
 		final File CONFIGURATION_FILE = new File(SETTINGS_PATH + File.separator
 				+ SETTINGS_FILE);
 		try {
 			config.set("MONSTERTICKETS.OPTIONS.OVERRIDE_HELP_COMMAND",
 					Variables.overridehelp);
 			save(config, CONFIGURATION_FILE);
 		} catch (Exception e) {
 			Configuration.debug(e);
 		}
 	}
 
 	/**
 	 * This method loads the plugins configuration file.
 	 */
 	public void load() {
 		final FileConfiguration config = this.plugin.getConfig();
 		final File CONFIGURATION_FILE = new File(SETTINGS_PATH, SETTINGS_FILE);
 		boolean exists = CONFIGURATION_FILE.exists();
 		if (exists) {
 			try {
 				Configuration.log("Loading settings!");
 				config.options()
 						.header("MonsterTickets configuration file, refer to our DBO page for help.");
 				config.load(CONFIGURATION_FILE);
 			} catch (Exception e) {
 				Configuration.debug(e);
 			}
 		} else {
 			Configuration.log("Loading default settings!");
 			config.options()
 					.header("MonsterTickets configuration file, refer to our DBO page for help.");
 			config.options().copyDefaults(true);
 		}
 		try {
 			Variables.overridehelp = config.getBoolean(
 					"MONSTERTICKETS.OPTIONS.OVERRIDE_HELP_COMMAND",
 					Variables.overridehelp);
 			save(config, CONFIGURATION_FILE);
 		} catch (Exception e) {
 			Configuration.debug(e);
 		}
 	}
 
 	private void loadTicketsConfig() {
 		File TICKETS_FILE = new File(SETTINGS_PATH, "Tickets.dat");
 		if (!TICKETS_FILE.exists()) {
 			return;
 		}
 		List<String> tickets = new ArrayList<String>();
 		FileConfiguration config = new YamlConfiguration();
 		try {
 			config.load(TICKETS_FILE);
 		} catch (Exception e) {
 			Configuration.log("Error loading cached tickets!");
 			File back = new File(SETTINGS_PATH, "OLD_Tickets.dat");
 			if (back.exists()) {
 				back.delete();// prevent errors when saving later
 			}
 			TICKETS_FILE.renameTo(back); // prevent errors when saving
 			return;
 		}
 		config.options().header("DO NOT MODIFY");
 		tickets = config.getStringList("TICKETS");
 		if (!tickets.isEmpty()) {
 			for (String str : tickets) {
 				if (str.contains("|")) {
 					int count = 0;
 					for (char c : str.toCharArray()) {
 						if (c == '|') {
 							count++;
 						}
 					}
 					if (count > 0) {
 						int idx1 = str.indexOf("|");
						int idx2 = str.indexOf("|", idx1 + 1);
						if (count == 2) {
 							int id = Integer.parseInt(str.substring(0, idx1));
 							String player = str.substring(idx1 + 1, idx2);
 							String description = str.substring(idx2 + 1);
 							Variables.tickets.add(new HelpTicket(id,
 									description, player));
						} else if (count == 3) {
							int idx3 = str.indexOf("|", idx2 + 1);
 							int id = Integer.parseInt(str.substring(0, idx1));
 							String player = str.substring(idx1 + 1, idx2);
 							String description = str.substring(idx2 + 1, idx3);
 							String modname = str.substring(idx3 + 1);
 							HelpTicket t = new HelpTicket(id, description,
 									player);
 							Variables.tickets.add(t);
 							Variables.tickets.getLast().Claim(modname);
 							Variables.tickets.getLast().close();
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void saveTicketsConfig() {
 		File TICKETS_FILE = new File(SETTINGS_PATH + File.separator
 				+ "Tickets.dat");
 		ArrayList<String> tickets = new ArrayList<String>();
 		FileConfiguration config = new YamlConfiguration();
 		config.options().header("DO NOT MODIFY");
 		for (HelpTicket t : Variables.tickets) {
 			if (t.getStatus().equals(Status.OPEN)) {
 				tickets.add(t.getID() + "|" + t.getNoobName() + "|"
 						+ t.getDescription());
 			} else if (t.getStatus().equals(Status.CLAIMED)) {
 				Close.close(t.getMod());
 			}
 			if (t.getStatus().equals(Status.CLOSED)) {
 				tickets.add(t.getID() + "|" + t.getNoobName() + "|"
 						+ t.getDescription() + "|" + t.getModName());
 			}
 
 		}
 		if (!tickets.isEmpty()) {
 			config.set("TICKETS", tickets);
 		}
 		save(config, TICKETS_FILE);
 	}
 }
