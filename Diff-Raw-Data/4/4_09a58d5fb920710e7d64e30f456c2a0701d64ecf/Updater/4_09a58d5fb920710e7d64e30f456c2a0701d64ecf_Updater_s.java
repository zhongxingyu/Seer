 package nl.giantit.minecraft.GiantShop.core.Updater;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.Updater.Config.confUpdate;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.net.URL;
 import java.util.logging.Level;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 public class Updater {
 	
 	private GiantShop plugin;
 	private int tID;
 	private config conf = config.Obtain();
 	
 	private boolean outOfDate = false;
 	private String newVersion = "";
 	
 	private void start() {
 		tID = this.plugin.scheduleAsyncRepeatingTask(new Runnable() {
 			@Override
 			public void run() {
 				newVersion = updateCheck(plugin.getDescription().getVersion());
 				if(isNewer(newVersion, plugin.getDescription().getVersion())) {
 					outOfDate = true;
 					GiantShop.log.log(Level.WARNING, "[" + plugin.getName() + "] " + newVersion + " has been released! You are currently running: " + plugin.getDescription().getVersion());
 					if(conf.getBoolean("GiantShop.Updater.broadcast"))
 						Heraut.broadcast("&cA new version of GiantShop has just ben released! You are currently running: " + plugin.getDescription().getVersion() + " while the latest version is: " + newVersion, true);
 				}
 			}
 		}, 0L, 432000L);
 	}
 	
 	public Updater(GiantShop plugin) {
 		this.plugin = plugin;
 		if(conf.getBoolean("GiantShop.Updater.checkForUpdates", false)) {
 			this.start();
 		}
 	}
 	
 	public void stop() {
 		if(!Double.isNaN(tID)) {
 			plugin.getServer().getScheduler().cancelTask(tID);
 		}
 	}
 	
 	public String updateCheck(String version) {
 		String uri = "http://dev.bukkit.org/server-mods/giantshop/files.rss";
 		try {
 			URL url = new URL(uri);
 			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
 			doc.getDocumentElement().normalize();
 			Node firstNode = doc.getElementsByTagName("item").item(0);
 			if(firstNode.getNodeType() == 1) {
 				NodeList firstElementTagName = ((Element)firstNode).getElementsByTagName("title");
 				NodeList firstNodes = ((Element)firstElementTagName.item(0)).getChildNodes();
 				return firstNodes.item(0).getNodeValue().replace("GiantShop 2.0", "").replaceAll(" \\(([a-zA-Z ]+)\\)", "").trim();
 			}
 		}catch (Exception e) {	
 		}
 		
 		return version;
 	}
 	
 	public iUpdater getUpdater(UpdateType t) {
 		switch(t) {
 			case CONFIG:
 				return new confUpdate();
 			default:
 				break;
 		}
 		
 		return null;
 	}
 	
 	public boolean isNewer(String newVersion, String version) {
		String[] nv = newVersion.replaceAll("\\.[a-zA-Z]+", "").split("\\.");
		String[] v = version.replaceAll("\\.[a-zA-Z]+", "").split("\\.");
 		Boolean isNew = false;
 		Boolean prevIsEqual = false; 
 		
 		for(int i = 0; i < nv.length; i++) {
 			int tn = Integer.parseInt(nv[i]);
 			int tv = 0;
 			if(v.length - 1 >= i)
 				tv = Integer.parseInt(v[i]);
 			
 			if(tn > tv) {
 				if(i == 0 || prevIsEqual == true) {
 					isNew = true;
 					break;
 				}
 			}else if(tn == tv) {
 				prevIsEqual = true;
 			}else{
 				break;
 			}
 			
 		}
 
 		return isNew;
 	}
 	
 	public Boolean isOutOfDate() {
 		return this.outOfDate;
 	}
 	
 	public String getNewVersion() {
 		return this.newVersion;
 	}
 }
