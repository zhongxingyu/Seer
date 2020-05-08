 package com.github.yukinoraru.ToggleInventory;
 
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class UpdateChecker {
 
 	private ToggleInventory plugin;
 	private URL filesFeed;
 
 	private String version, link;
 
 	public String getVersion() {
 		return version;
 	}
 
 	public String getLink() {
 		return link;
 	}
 
 	public UpdateChecker(ToggleInventory plugin, String url){
 		this.plugin = plugin;
 
 		try {
 			this.filesFeed = new URL(url);
 		} catch (MalformedURLException e) {
 			plugin.getLogger().warning("Update check failed.");
 		}
 	}
 
 	public boolean updateNeeded(){
 		try {
			InputStream input = this.filesFeed.openConnection().getInputStream();
 			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
 
 			Node latestFile = document.getElementsByTagName("item").item(0);
 			NodeList children = latestFile.getChildNodes();
 
 			this.version = children.item(1).getTextContent().replaceAll("[a-zA-Z -]", "").replaceAll("\\(.*\\)", "");
 			this.link = children.item(3).getTextContent();
 
 			//plugin.getLogger().info("Version=" + this.version + ", DL link=" + this.link);
 
 			// compare version
 			String runningVersion = plugin.getDescription().getVersion().replaceAll("[-a-zA-Z ]", "");
 
 			int cmp = runningVersion.compareTo(this.version);
 			if(cmp < 0){
 				return true;
 			}
 			else if(cmp > 0){
 				// maybe developing
 				plugin.getLogger().info("This version is newer than dev.bukkit.org one.");
 			}else{
 				// latest
 				plugin.getLogger().info("This is the latest version :)");
 			}
 
 		} catch (Exception e) {
 			plugin.getLogger().warning(String.format("Update check failed. (%s)", e.getMessage()));
 		}
 		return false;
 	}
 
 }
