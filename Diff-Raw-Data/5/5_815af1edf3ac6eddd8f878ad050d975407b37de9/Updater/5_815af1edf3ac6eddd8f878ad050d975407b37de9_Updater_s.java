 package com.spaceemotion.updater;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class Updater {
 	private final JavaPlugin plugin;
 	private final boolean do_update;
 
 	private final String plugin_name;
 	private final String update_url;
 	private final double current_version;
 	private final String current_version_str;
 
 	private double new_version;
 	private String new_version_str;
 
 	private BukkitTask updateTask;
 	private boolean update_available = false;
 	private String update_message;
 
 	public Updater( JavaPlugin plugin, boolean doUpdate ) {
 		this.plugin = plugin;
 		this.do_update = doUpdate;
 
 		this.plugin_name = plugin.getName();
		this.current_version_str = plugin.getDescription().getVersion();
 		this.current_version = Double.parseDouble( current_version_str.replaceFirst( "\\.", "" ).replace( "/", "" ) );
 
 		this.update_url = "http://dev.bukkit.org/server-mods/" + plugin_name + "/files.rss";
 
 		if (this.do_update) {
 			initialize();
 			checkForUpdate();
 		}
 	}
 
 	private void initialize() {
 		updateTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously( plugin, new Runnable() {
 			public void run() {
 				checkForUpdate();
 			}
 		}, 30 * 1200, 30 * 1200 );
 	}
 
 	private void checkForUpdate() {
 		new_version = getNewVersion( this.current_version );
 
 		if (new_version > current_version) {
 			update_available = true;
 
 			displayUpdateMsg();
 		} else
 			plugin.getLogger().info( "Running latest version (v" + current_version + "), no new updates available!" );
 	}
 
 	private double getNewVersion( double currentVersion ) {
 		try {
 			URL url = new URL( update_url );
 			InputStream in = url.openConnection().getInputStream();
 
 			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( in );
 			doc.getDocumentElement().normalize();
 
 			NodeList nodes = doc.getElementsByTagName( "item" );
 			Node first = nodes.item( 0 );
 
 			if (first.getNodeType() == 1) {
 				NodeList titleNode = ((Element) first).getElementsByTagName( "title" );
 				NodeList firstNodes = titleNode.item( 0 ).getChildNodes();
 
 				new_version_str = firstNodes.item( 0 ).getNodeValue().replace( plugin_name + " v", "" ).trim();
 				return Double.parseDouble( new_version_str.replaceFirst( "\\.", "" ).replace( "/", "" ) );
 			}
 		} catch (Exception e) {
 			displayUpdateMsg( false );
 		}
 
 		return current_version;
 	}
 
 	private void displayUpdateMsg() {
 		displayUpdateMsg( true );
 	}
 
 	private void displayUpdateMsg( boolean success ) {
 		String out;
 
 		if (success)
 			out = "New version available: v" + new_version_str;
 		else
 			out = "Updater failed to get new version!";
 
 		out += " (running v" + current_version_str + ")";
 
 		update_message = out;
 		plugin.getLogger().info( out );
 	}
 
 	public BukkitTask getUpdaterTask() {
 		return updateTask;
 	}
 
 	public boolean updateIsAvailable() {
 		return update_available;
 	}
 
 	public String getUpdateMessage() {
 		return update_message;
 	}
 }
