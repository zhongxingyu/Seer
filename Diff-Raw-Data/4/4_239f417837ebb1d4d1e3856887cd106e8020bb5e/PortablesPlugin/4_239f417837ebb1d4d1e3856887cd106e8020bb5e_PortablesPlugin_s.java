 /*
  * This file is part of Portables.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Portables is licensed under the Almura Development License.
  *
  * Portables is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * As an exception, all classes which do not reference GPL licensed code
  * are hereby licensed under the GNU Lesser Public License, as described
  * in Almura Development License.
  *
  * Portables is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License,
  * the GNU Lesser Public License (for classes that fulfill the exception)
  * and the Almura Development License along with this program. If not, see
  * <http://www.gnu.org/licenses/> for the GNU General Public License and
  * the GNU Lesser Public License.
  */
 package com.almuramc.portables.bukkit;
 
 import com.almuramc.portables.bukkit.command.PortablesCommands;
 import com.almuramc.portables.bukkit.configuration.PortablesConfiguration;
 import com.almuramc.portables.bukkit.util.Dependency;
 import com.almuramc.portables.bukkit.util.SpoutSafeBindings;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PortablesPlugin extends JavaPlugin {
 	private static Dependency hooks;
 	private static PortablesConfiguration cached;
 	private static PortablesPlugin instance;
 
 	@Override
 	public void onLoad() {
 		cached = new PortablesConfiguration(this);
 		cached.load();
 		hooks = new Dependency(this);
 	}
 
 	@Override
 	public void onEnable() {
 		instance = this;
		if (cached.useEconomy()) {
			hooks.setupVaultEconomy();
		}
 		hooks.setupVaultPermissions();
 		getCommand("portables").setExecutor(new PortablesCommands(this));
 		//Classloader has SpoutPlugin, Admin wants to use Spout features, and SpoutPlugin has been enabled. Overkill but trying to nail out the issue
 		if (cached.useSpout() && hooks.isSpoutPluginEnabled()) {
 			SpoutSafeBindings.registerSpoutBindings();
 		}
 	}
 
 	public static PortablesPlugin getInstance() {
 		return instance;
 	}
 
 	public static Dependency getHooks() {
 		return hooks;
 	}
 
 	public static PortablesConfiguration getCached() {
 		return cached;
 	}
 }
