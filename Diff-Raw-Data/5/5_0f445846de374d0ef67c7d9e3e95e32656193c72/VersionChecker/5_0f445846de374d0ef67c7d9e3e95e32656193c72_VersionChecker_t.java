 package team.ApiPlus.Util;
 
 import java.net.URL;
 import java.util.logging.Level;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.Plugin;
 import org.w3c.dom.CharacterData;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /* 
  * @author SirTyler (Tyler Martin) <sirtyler132@gmail.com>
  *
  * Copyright 2011 Tyler Martin. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of
  * conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list
  * of conditions and the following disclaimer in the documentation and/or other materials
  * provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and contributors and should not be interpreted as representing official policies,
  * either expressed or implied, of anybody else.
  */
 
 public class VersionChecker implements Listener{
 	private Plugin plugin;
 	private URL url;
 	private String title;
 	private String link;
 	private String pubdate;
 	public String version;
 	private String thisversion;
 	private boolean warn = false;
 	
 	public VersionChecker(Plugin _plugin, String u) {
 		plugin = _plugin;
 		try {
 			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			url = new URL(u);
 			Document doc = builder.parse(url.openStream());
 			Element element = (Element) doc.getElementsByTagName("item").item(0);
 			title = (getElementValue(element,"title"));
 			link = (getElementValue(element,"link"));
 			String[] pubdates = (getElementValue(element,"pubDate")).split(" ");
 			pubdate = pubdates[0] + " " + pubdates[1] + " " + pubdates[2] + " " + pubdates[3];
			version = title.substring(title.indexOf('[')).replaceAll("\\[", "").replaceAll("\\]", "").replace(" ", "");
 			thisversion = plugin.getDescription().getVersion().replace("-DEV", "");
 			Bukkit.getPluginManager().registerEvents(this, plugin);
 			check();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}

 	public void check() {
 		if(!isCurrent()) {
 			plugin.getLogger().log(Level.INFO, "New Version " + version + " Out!");
 			plugin.getLogger().log(Level.INFO, title + " published on: " + pubdate);
 			plugin.getLogger().log(Level.INFO, "Download at " + link);
 			warn = true;
 		}
 	}
 	
 	public boolean isCurrent() {
 		if(version.equalsIgnoreCase(thisversion)){
 			return true;
 		} else {
 			double a = 0;
 			try {
 				a = Double.parseDouble(version);
 			} catch (Exception e) {
 				a = Double.parseDouble(version.split("\\.")[0] + "." + version.split("\\.")[1]);
 			}
 			double b;
 			try {
 				b = Double.parseDouble(thisversion);
 			} catch (Exception e) {
 				b = Double.parseDouble(thisversion.split("\\.")[0] + "." + thisversion.split("\\.")[1]);
 			}
 			if(b >= a) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 	}
 	
 	private String getCharacterDataFromElement(Element e) {
 		try {
 			Node child = e.getFirstChild();
 			if(child instanceof CharacterData) {
 				CharacterData cd = (CharacterData) child;
 				return cd.getData();
 			}
 		}catch(Exception ex) {
 			ex.printStackTrace();
 		}
 		return "";
 	}
 		
 	protected String getElementValue(Element parent,String label) {
 		return getCharacterDataFromElement((Element)parent.getElementsByTagName(label).item(0));
 	}
 	
 	@EventHandler
 	public void command(PlayerCommandPreprocessEvent event) {
 		Player p = event.getPlayer();
 		String command = event.getMessage().replaceFirst("/", "");
 		if(command.equalsIgnoreCase("versionchecker")) {
 			p.sendMessage("Running VersionChecker in " + plugin.getDescription().getFullName());
 		}
 	}
 	
 	@EventHandler
 	public void join(PlayerJoinEvent event) {
 		Player p = event.getPlayer();
 		if(warn == true && (p.hasPermission("version.notify") || p.isOp())) {
 			p.sendMessage(ChatColor.RED + "Your Version of " + plugin.getDescription().getName() + " is Outdated.");
 			p.sendMessage(ChatColor.RED + "Current Version is " + title);
 		}
 	}
 }
