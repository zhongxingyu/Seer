 /**
  * This file is part of InfoGuide.
  *
  * Copyright Dockter 2012 <mcsnetworks.com> InfoGuide is licensed under the GNU
  * General Public License.
  *
  * InfoGuide is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * As an exception, all classes which do not reference GPL licensed code are
  * hereby licensed under the GNU Lesser Public License, as described in GNU
  * General Public License.
  *
  * InfoGuide is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License, the GNU
  * Lesser Public License (for classes that fulfill the exception) and the GNU
  * General Public License along with this program. If not, see
  * <http://www.gnu.org/licenses/> for the GNU General Public License and the GNU
  * Lesser Public License.
  */
 package net.dockter.infoguide.guide;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Guide {
 
 	public final static Logger log = Logger.getLogger("Minecraft");
 
 	public static Guide load(File file) {
 		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
 		List<String> pgs = new ArrayList<String>();
 		System.out.println("Loading guide: " + file.getName());
 		if (config.isConfigurationSection("Pages")) {
 			System.out.println("Pages is a configuration section!");
 			ConfigurationSection cs = config.getConfigurationSection("Pages");
 			for (String key : cs.getKeys(false)) {
 				System.out.println("Adding " + cs.getString(key) + " to the pages list!");
 				pgs.add(cs.getString(key));
 			}
 		}
 		return new Guide(config.getString("Name"), config.getString("Date"), config.getString("Text"), pgs);
 	}
 	private String name, date;
 	private List<String> pages;
 
 	public Guide(String name, String date, String text, List<String> pages) {
 		this.name = name;
 		this.date = date;
 
 		this.pages = pages;
		
		pages.add("PageZero");
 
 		if (this.pages == null) {
 			this.pages = new ArrayList<String>();
 			log.info("Upgrading Guide..");
 		}
 		prepareForLoad();
 	}
 
 	public void prepareForLoad() {
 		for (ChatColor possibleColor : ChatColor.values()) {
 			for (int i = 1; i < pages.size(); i++) {
 				String what = pages.get(i);
 				what = what.replaceAll("#" + possibleColor.getChar(), "" + possibleColor);
 				pages.set(i, what);
 			}
 		}
 		for (int i = 1; i < pages.size(); i++) {
 			String what = pages.get(i);
 			what = what.replaceAll("#k", ChatColor.COLOR_CHAR + "k");
 			what = what.replaceAll("#l", ChatColor.COLOR_CHAR + "l");
 			what = what.replaceAll("#m", ChatColor.COLOR_CHAR + "m");
 			what = what.replaceAll("#n", ChatColor.COLOR_CHAR + "n");
 			what = what.replaceAll("#o", ChatColor.COLOR_CHAR + "o");
 			what = what.replaceAll("#r", ChatColor.COLOR_CHAR + "r");
 			pages.set(i, what);
 		}
 
 
 	}
 
 	public void prepareForSave() {
 		for (ChatColor possibleColor : ChatColor.values()) {
 			for (int i = 1; i < pages.size(); i++) {
 				String what = pages.get(i);
 				what = what.replaceAll("" + possibleColor, "#" + possibleColor.getChar());
 				pages.set(i, what);
 			}
 		}
 		for (int i = 1; i < pages.size(); i++) {
 			String what = pages.get(i);
 			what = what.replaceAll(ChatColor.COLOR_CHAR + "k", "#k");
 			what = what.replaceAll(ChatColor.COLOR_CHAR + "l", "#l");
 			what = what.replaceAll(ChatColor.COLOR_CHAR + "m", "#m");
 			what = what.replaceAll(ChatColor.COLOR_CHAR + "n", "#n");
 			what = what.replaceAll(ChatColor.COLOR_CHAR + "o", "#o");
 			what = what.replaceAll(ChatColor.COLOR_CHAR + "r", "#r");
 			pages.set(i, what);
 		}
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getDate() {
 		return date;
 	}
 
 	public String getPage(int i) {
 		if (i == pages.size()) {
 			pages.add("");
 		}
 		System.out.println("Returning text: " + pages.get(i));
 		return pages.get(i);
 	}
 
 	public void save() {
 		prepareForSave();
 		File toSave = new File("plugins" + File.separator + "InfoGuide" + File.separator + "guides" + File.separator + name + ".yml");
 		if (!toSave.exists()) {
 			try {
 				toSave.createNewFile();
 			} catch (IOException ex) {
 				Logger.getLogger(Guide.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 		YamlConfiguration config = YamlConfiguration.loadConfiguration(toSave);
 		config.set("Name", name);
 		config.set("Date", date);
 		config.set("Pages", null);
 		config.createSection("Pages");
 		for (Integer curPage = 1; curPage < pages.size(); curPage++) {
 			config.set("Pages.Nr" + curPage, pages.get(curPage));
 		}
 		try {
 			config.save(toSave);
 		} catch (IOException ex) {
 			Logger.getLogger(Guide.class.getName()).log(Level.SEVERE, null, ex);
 		}
 
 	}
 
 	public void delete() {
 		File toSave = new File("plugins" + File.separator + "InfoGuide" + File.separator + "guides" + File.separator + name + ".yml");
 		toSave.delete();
 	}
 
 	public void setPage(int i, String page) {
 		pages.set(i, page);
 	}
 
 	public void setDate(String format) {
 		this.date = format;
 	}
 
 	public void setName(String text) {
 		this.name = text;
 	}
 
 	public int getPages() {
 		return pages.size() - 1;
 	}
 
 	public void addPage() {
 		pages.add("");
 	}
 }
