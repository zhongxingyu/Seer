 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.*;
 
 import org.w3c.dom.*;
 
 public class Listener extends PluginListener {
 	
 	List<Criminal> wantedCriminals = new ArrayList<Criminal>();
 	Logger pluginLogger = Logger.getLogger("Minecraft");
 	File pluginSettingsFile = new File("Wanted.xml");
 
 	public boolean onCommand(Player player, java.lang.String[] split) {
		// TODO Implement a parseCommand() method
 		return false;
 	}
 
 	public boolean onConsoleCommand(java.lang.String[] split) {
 		// TODO Implement a parseConsoleCommand() method
 		return false;
 	}
 	
 	public boolean parseCommand(Player player, java.lang.String[] chatCommand) {		
 		if ((chatCommand.length > 0) && (chatCommand.length < 2) && (chatCommand[1].equalsIgnoreCase("/wanted")) && (player.canUseCommand(chatCommand[1]))) {
 			player.sendMessage("c[Wanted!] fInvalid parameters!");
 		} else if ((chatCommand.length > 0) && (chatCommand[1].equalsIgnoreCase("/wanted")) && (player.canUseCommand(chatCommand[1]))){
 			// TODO Parse the wanted command
 		}
 		return false;
 	}
 
 	public boolean parseConsoleCommand(java.lang.String[] consoleCommand) {
 		return false;
 	}
 	
 	public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount) {
 		Player playerAttacker = attacker.getPlayer(), playerDefender = defender.getPlayer();
 		if ((playerAttacker.isPlayer()) && (playerDefender.isPlayer())) {
 			// TODO Detect if the player was killed by the attack
 		}
 		return false;
 	}
 	
 	public void onLogin(Player player) {
 		if (wantedCriminals.size() > 0) {
 			for (Criminal wantedPlayer : wantedCriminals) {
 				if (wantedPlayer.getUsername().equalsIgnoreCase(player.getName())) {
 					alertPlayers(player);
 				}
 			}
 		}
 	}
 	
 	public void alertPlayers(final Player player) {
 		etc.getServer().addToServerQueue(new Runnable(){
 			public void run() {
 				etc.getServer().messageAll("c[Wanted!] f\"" + player.getName() + "\" has a bounty!");
 			}
 		});
 	}
 	
 	@SuppressWarnings("unused")
 	public void loadSettings() {
 		if (!pluginSettingsFile.exists()) {
 			// TODO Load default settings
 		} else {
 			try {
 				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
 				DocumentBuilder builder;
 				builder = builderFactory.newDocumentBuilder();
 				Document settingsDocument = builder.parse(pluginSettingsFile);
 				NodeList bountyNodes, temporaryNodes;
 				NamedNodeMap nodeAttributes;
 				Node temporaryNode;
 				
 				settingsDocument.getDocumentElement().normalize();
 				bountyNodes = settingsDocument.getElementsByTagName("Bounty");
 				
 				if (bountyNodes.getLength() > 0) {
 					for (Integer i = 0; i < bountyNodes.getLength(); i++) {
 						temporaryNodes = bountyNodes.item(i).getChildNodes();
 						if (temporaryNodes.getLength() > 0) {
 							String temporaryUsername;
 							List<Reward> temporaryRewards = new ArrayList<Reward>();
 							
 							for (Integer j = 0; j < temporaryNodes.getLength(); j++) {
 								if (temporaryNodes.item(j).getNodeName().equalsIgnoreCase("Username")) {
 									if (temporaryNodes.item(j).getNodeValue().length() > 0) {
 										temporaryUsername = temporaryNodes.item(j).getNodeValue();
 									} else {
 										pluginLogger.warning("[Wanted!] Unable to load bounty for user \"" + temporaryNodes.item(j).getNodeValue() + "\"");
 										break;
 									}
 								} else if (temporaryNodes.item(j).getNodeName().equalsIgnoreCase("Reward")) {
 									nodeAttributes = temporaryNodes.item(j).getAttributes();
 									
 									// FIXME Parse the reward tags
 								}
 							}
 						} else {
 							pluginLogger.warning("[Wanted!] A bounty was improperly saved!");
 						}
 					}
 				} else {
 					pluginLogger.warning("[Wanted!] No bounties were found!");
 				}
 			} catch (Exception e) {
 				pluginLogger.severe("[Wanted!] An exception has been thrown : " + e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void saveSettings() {
 		// FIXME Save the settings to the file
 	}
 	
 }
