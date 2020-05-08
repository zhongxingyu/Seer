 package no.tjnome.phfms;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 /**
 *
* GoldBank
* Copyright (C) 2011 tjnome
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 *  @author tjnome
 */
 
 public class main extends JavaPlugin {
 
 	public PluginManager pm;
 
 	public main() {
 	}
 
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " disabled");
 		System.out.println("Plugin by tjnome!");
 
 	}
 
 	@Override
 	public void onEnable() {
 		getDataFolder().mkdirs();
 		registerEvents();
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
 		System.out.println("Plugin by tjnome!");
 
 	}
 
 	public void registerEvents() {
 		this.pm = getServer().getPluginManager();
 		getServer().getPluginManager().registerEvents(new mainListner(this), this);
 	}
 }
