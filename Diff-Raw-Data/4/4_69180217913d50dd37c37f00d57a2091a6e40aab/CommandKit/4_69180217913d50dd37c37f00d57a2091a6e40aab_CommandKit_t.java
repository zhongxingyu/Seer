 /*
  * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
  *
  * This file is part of CommandKit
  *
  * CommandKit is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CommandKit is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alta189.cyborg.commandkit;
 
 import com.alta189.cyborg.api.command.annotation.EmptyConstructorInjector;
 import com.alta189.cyborg.api.command.annotation.SimpleInjector;
 import com.alta189.cyborg.api.plugin.CommonPlugin;
 import com.alta189.cyborg.api.util.yaml.YAMLFormat;
 import com.alta189.cyborg.api.util.yaml.YAMLProcessor;
 import com.alta189.cyborg.commandkit.google.GoogleCommands;
 import com.alta189.cyborg.commandkit.hastebin.HastebinCommands;
 import com.alta189.cyborg.commandkit.seen.SeenCommands;
 import com.alta189.cyborg.commandkit.seen.SeenEntry;
 import com.alta189.cyborg.commandkit.seen.SeenListener;
 import com.alta189.cyborg.commandkit.tell.TellCommands;
 import com.alta189.cyborg.commandkit.tell.TellEntry;
 import com.alta189.cyborg.commandkit.tell.TellListener;
 import com.alta189.cyborg.commandkit.twitter.TwitterCommands;
 import com.alta189.simplesave.Database;
 import com.alta189.simplesave.DatabaseFactory;
 import com.alta189.simplesave.exceptions.ConnectionException;
 import com.alta189.simplesave.exceptions.TableRegistrationException;
 import com.alta189.simplesave.mysql.MySQLConfiguration;
 import com.alta189.simplesave.mysql.MySQLConstants;
 import twitter4j.Twitter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 
 public class CommandKit extends CommonPlugin {
 
 	private static CommandKit instance;
 	private YAMLProcessor config;
 	private Database db;
 	
 	@Override
 	public void onEnable() {
 		getLogger().log(Level.INFO, "Enabling...");
 		instance = this;
 
 		MySQLConfiguration dbConfig = new MySQLConfiguration();
 		dbConfig.setHost(getConfig().getString("database.mysql.host", "127.0.0.1"));
 		dbConfig.setPort(getConfig().getInt("database.mysql.port", MySQLConstants.DefaultPort));
 		dbConfig.setDatabase(getConfig().getString("database.mysql.database"));
 		dbConfig.setUser(getConfig().getString("database.mysql.user", MySQLConstants.DefaultUser));
 		dbConfig.setPassword(getConfig().getString("database.mysql.password", MySQLConstants.DefaultPass));
 
 		db = DatabaseFactory.createNewDatabase(dbConfig);
 		
 		try {
 			db.registerTable(SeenEntry.class);
 			db.registerTable(TellEntry.class);
 		} catch (TableRegistrationException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			db.connect();
 		} catch (ConnectionException e) {
 			e.printStackTrace();
 		}
 
 		// Register Listeners
 		getCyborg().getEventManager().registerEvents(new SeenListener(), this);
 		getCyborg().getEventManager().registerEvents(new TellListener(), this);
 
 		// Register Commands
 		getCyborg().getCommandManager().registerCommands(this, GoogleCommands.class, EmptyConstructorInjector.getInstance());
 		getCyborg().getCommandManager().registerCommands(this, SeenCommands.class, EmptyConstructorInjector.getInstance());
 		getCyborg().getCommandManager().registerCommands(this, TellCommands.class, EmptyConstructorInjector.getInstance());
 		getCyborg().getCommandManager().registerCommands(this, HastebinCommands.class, EmptyConstructorInjector.getInstance());
		getCyborg().getCommandManager().registerCommands(this, TwitterCommands.class, new SimpleInjector(getConfig().getString("twitter.consumer-key")));
		getCyborg().getCommandManager().registerCommands(this, TwitterCommands.class, new SimpleInjector(getConfig().getString("twitter.consumer-secret")));
 
 		getLogger().log(Level.INFO, "Successfully Enabled!");
 	}
 
 	@Override
 	public void onDisable() {
 		getLogger().log(Level.INFO, "Disabling...");
 
 		if (db != null) {
 			try {
 				db.close();
 			} catch (ConnectionException e) {
 				e.printStackTrace();
 			}
 		}
 
 		getConfig().save();
 		
 		instance = null;
 		getLogger().log(Level.INFO, "Successfully Disabled!");
 	}
 	
 	public static YAMLProcessor getConfig() {
 		if (instance.config == null) {
 			instance.config = instance.setupConfig(new File(instance.getDataFolder(), "config.yml"));
 			try {
 				instance.config.load();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return instance.config;
 	}
 
 	public static Database getDatabase() {
 		return instance.db;
 	}
 
 	private YAMLProcessor setupConfig(File file) {
 		if (!file.exists()) {
 			try {
 				InputStream input = getClass().getResource("config.yml").openStream();
 				if (input != null) {
 					FileOutputStream output = null;
 					try {
 						if (file.getParentFile() != null) {
 							file.getParentFile().mkdirs();
 						}
 						output = new FileOutputStream(file);
 						byte[] buf = new byte[8192];
 						int length;
 
 						while ((length = input.read(buf)) > 0) {
 							output.write(buf, 0, length);
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					} finally {
 						try {
 							input.close();
 						} catch (Exception ignored) {
 						}
 						try {
 							if (output != null) {
 								output.close();
 							}
 						} catch (Exception e) {
 						}
 					}
 				}
 			} catch (Exception e) {
 			}
 		}
 
 		return new YAMLProcessor(file, false, YAMLFormat.EXTENDED);
 	}
 }
