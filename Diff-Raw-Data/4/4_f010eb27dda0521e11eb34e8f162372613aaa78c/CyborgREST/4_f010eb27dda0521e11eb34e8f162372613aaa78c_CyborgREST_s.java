 /*
  * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
  *
  * This file is part of CyborgREST
  *
  * CyborgREST is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CyborgREST is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alta189.cyborg.rest;
 
 import com.alta189.cyborg.api.plugin.CommonPlugin;
 import com.alta189.cyborg.api.util.yaml.YAMLFormat;
 import com.alta189.cyborg.api.util.yaml.YAMLProcessor;
 import com.alta189.cyborg.rest.core.Command;
 import com.alta189.cyborg.rest.core.CommandProvider;
 import com.alta189.cyborg.rest.core.CyborgInfo;
 import com.alta189.cyborg.rest.core.CyborgProvider;
 import com.alta189.cyborg.rest.core.StatusProvider;
 import com.alta189.cyborg.rest.factoids.Factoid;
 import com.alta189.cyborg.rest.factoids.FactoidProvider;
 import org.glassfish.grizzly.http.server.HttpServer;
 import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.json.JsonJaxbModule;
 import org.glassfish.jersey.server.ResourceConfig;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.logging.Level;
 
 public class CyborgREST extends CommonPlugin {
 	private static CyborgREST instance;
 	private YAMLProcessor config;
 	private HttpServer server;
 	private URI baseUri;
 	private GarbageManThread garbageMan;
 	private boolean debug;
 
 	@Override
 	public void onEnable() {
 		instance = this;
 		getLogger().log(Level.INFO, "Enabling...");
 
 		debug = getConfig().getBoolean("debug", false);
 
 		baseUri = URI.create(getConfig().getString("base-url", "http://localhost:8080/rest/"));
 
 		server = GrizzlyHttpServerFactory.createHttpServer(baseUri, buildResourceConfig());
 
 		if (getConfig().getBoolean("gc-thread", true)) {
 			Object obj = getConfig().getProperty("gc-wait");
 			long wait = obj instanceof Number ? ((Number) obj).longValue() : 600000;
 			if (debug) {
 				System.out.println("Starting the GarbageManThread with a wait of " + wait);
 			}
 			garbageMan = new GarbageManThread(wait);
 			garbageMan.start();
 		}
 
 		try {
 			server.start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		getLogger().log(Level.INFO, "Successfully Enabled!");
 	}
 
 	@Override
 	public void onDisable() {
 		getLogger().log(Level.INFO, "Disabling...");
 
 		if (garbageMan != null) {
 			try {
 				garbageMan.interrupt();
 			} catch (Exception ignored){
 			}
 		}
 
 		if (server != null) {
 			server.stop();
 			server = null;
 		}
 
 		getConfig().save();
 
 		getLogger().log(Level.INFO, "Successfully Disabled!");
 		instance = null;
 	}
 
 	public ResourceConfig buildResourceConfig() {
		ResourceConfig resourceConfig = new ResourceConfig().addModules(new JsonJaxbModule());
 		resourceConfig.addClasses(StatusProvider.class, CyborgProvider.class, CyborgInfo.class, Command.class, CommandProvider.class);
 
 		if (getCyborg().getPluginManager().getPlugin("CyborgFactoids") != null) {
 			resourceConfig.addClasses(Factoid.class, FactoidProvider.class);
 		}
 
 		return resourceConfig;
 	}
 
 	public static CyborgREST getInstance() {
 		return instance;
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
 
 	public static synchronized boolean isDebug() {
 		return instance.debug;
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
