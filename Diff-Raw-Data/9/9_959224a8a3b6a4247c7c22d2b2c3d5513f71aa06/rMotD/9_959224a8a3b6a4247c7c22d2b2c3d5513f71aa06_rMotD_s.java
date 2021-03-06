 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class rMotD extends Plugin {
 	public rPropertiesFile Messages;
 	PluginListener listener = new rMotDListener(this);
 	Logger log = Logger.getLogger("Minecraft");
 	Server MCServer =etc.getServer();
 	Timer scheduler;
 	String defaultGroup;
	String versionNumber = "1.8"; 
 	public iData data;
 	
 	public rMotD () {
 		Messages = new rPropertiesFile("rMotD.properties");
 	}
 	
 	public void initialize(){
 		etc.getLoader().addListener(PluginLoader.Hook.LOGIN  , listener, this, PluginListener.Priority.MEDIUM);
 		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
 		etc.getLoader().addListener(PluginLoader.Hook.SERVERCOMMAND, listener, this, PluginListener.Priority.MEDIUM);
 		if (etc.getDataSource().getDefaultGroup() != null)
 			defaultGroup = etc.getDataSource().getDefaultGroup().Name;
 		if (iData.iExist()){
 			data = new iData();
 		}
 	} 
 	public void enable(){
 		try {
 			Messages.load();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "[rMotD]: Exception while loading properties file.", e);
 		}
 		
 		/* Go through all timer messages, create rMotDTimers for each unique list */
 		if (Messages.keyExists("<<timer>>")){
 			Hashtable<String, ArrayList<String>> timerLists = new Hashtable <String, ArrayList<String>>();
 			scheduler = new Timer();
 			// Sort all the timer messages into lists 
 			for (String sortMe : Messages.getStrings("<<timer>>")){
 				String [] split =  sortMe.split(":");
 				String [] options =  split[1].split(",");
 				String sortMeList = options[0];
 				if(!timerLists.containsKey(sortMeList)){
 					timerLists.put(sortMeList, new ArrayList<String>());
 				}
 				timerLists.get(sortMeList).add(sortMe);
 			}
 			// Make an rMotDTimer for each list!
 			for (String key : timerLists.keySet().toArray(new String[timerLists.keySet().size()])){
 				// rMotDTimer(rMotD rMotD, Timer timer, String [] Messages)
 				ArrayList<String> sendTheseAList = timerLists.get(key);
 				String [] sendThese = sendTheseAList.toArray(new String[sendTheseAList.size()]);
 				rMotDTimer scheduleMe = new rMotDTimer(this, scheduler, sendThese); 
 				scheduler.schedule(scheduleMe, scheduleMe.delay);
 			}
 		}
 
 		/* TODO (Efficiency): Go through each message, see if any messages actually need these listeners. */
 		// Regex: ^([A-Za-z0-9,]+):([A-Za-z0-9,]*:([A-Za-z0-9,]*disconnect([A-Za-z0-9,]*)
 		etc.getLoader().addListener(PluginLoader.Hook.DISCONNECT   , listener, this, PluginListener.Priority.MEDIUM);
 		etc.getLoader().addListener(PluginLoader.Hook.BAN          , listener, this, PluginListener.Priority.MEDIUM);
 		etc.getLoader().addListener(PluginLoader.Hook.HEALTH_CHANGE, listener, this, PluginListener.Priority.MEDIUM);
 		etc.getInstance().addCommand("/grouptell", "Tell members of a group something.");
 		etc.getInstance().addCommand("/rmotd", "Displays your Message of the Day");
 		log.info("[rMotD] Loaded: Version " + versionNumber);
 	}
 	public void disable(){
 		Messages.save();
 		etc.getInstance().removeCommand("/grouptell");
 		etc.getInstance().removeCommand("rmotd");
 		scheduler.cancel();
 		scheduler.purge();
 		log.info("[rMotD] Disabled!");
 	} 
 	
 	public void sendToGroup(String sendToGroup, String message) {
 		String [] arrayOfOne = new String[1];
 		arrayOfOne[0] = sendToGroup;
 		sendToGroups(arrayOfOne, message);
 		return;
 	}
 	
 	/* Looks through all of the messages,
 	 * Sends the messages triggered by groups which 'triggerMessage' is a member of,
 	 * But only if that message has the contents of 'option' as one of its options */
 	public void triggerMessagesWithOption(Player triggerMessage, String option){
 		String[] eventToReplace = new String[0];
 		String[] eventReplaceWith = new String[0];
 		triggerMessagesWithOption(triggerMessage, option, eventToReplace, eventReplaceWith);
 	}
 	
 	public void triggerMessagesWithOption(Player triggerMessage, String option, String[] eventToReplace, String[] eventReplaceWith){
 		ArrayList<String>groupArray = new ArrayList<String>();
 		/* Obtain triggerer's group list */
 		if (triggerMessage.hasNoGroups()){
 			groupArray.add(defaultGroup);
 		} else {
 			groupArray.addAll(Arrays.asList(triggerMessage.getGroups()));
 		}
 		groupArray.add("<<player|" + triggerMessage.getName() + ">>");
 		groupArray.add("<<everyone>>");
 		
 		/* Obtain list of online players */
 		String playerList = new String();
 		List<Player> players = MCServer.getPlayerList();
 		if (players.size() == 1)
 			playerList = players.get(0).getName();
 		else {
 			for (Player getName : players){
 				playerList = getName.getName() + ", " + playerList;
 			}
 		}
 		
 		/* Check for messages triggered by each group the player is a member of. */
 		for (String groupName : groupArray){
 			if (Messages.keyExists(groupName)){
 				for (String sendToGroups_Message : Messages.getStrings(groupName)){
 					String [] split =  sendToGroups_Message.split(":");
 					String [] options =  split[1].split(",");
 					boolean hookValid = false;
 					
 					if (split[1].isEmpty() && option.equalsIgnoreCase("onlogin")){
 						hookValid = true;
 					} else for (int i = 0; i <options.length && hookValid == false; i++){
 						if(options[i].equalsIgnoreCase(option)) hookValid = true;
 					}
 					
 					if (hookValid) {
 						String message = etc.combineSplit(2, split, ":");
 						
 						/* Tag replacement: First round (triggerer) go! */
 						int balance = 0;
 						if (data != null){
 							balance = data.getBalance(triggerMessage.getName());
 						}
 						String [] replace = {"@"	, "<<triggerer>>"          , "<<triggerer-ip>>"    , "<<triggerer-color>>"   , "<<triggerer-balance>>"  , "<<player-list>>"};
 						String [] with    = {"\n"	, triggerMessage.getName() , triggerMessage.getIP(),triggerMessage.getColor(), Integer.toString(balance), playerList};					
 						message = MessageParser.parseMessage(message, replace, with);
 						if (eventToReplace.length > 0)
 							message = MessageParser.parseMessage(message, eventToReplace, eventReplaceWith);
 						/* Tag replacement end! */
 						
 						sendMessage(message, triggerMessage, split[0]);
 					}
 				}
 			}
 		}
 	}
 	
 	
 	public void sendMessage(String message, Player triggerMessage, String Groups){
 		/* Default: Send to player unless groups are specified.
 		 * If so, send to those instead. */
 		if (Groups.isEmpty()) {
 			sendToPlayer(message, triggerMessage);
 		}
 		else {
 			String [] sendToGroups = Groups.split(",");
 			sendToGroups(sendToGroups, message, triggerMessage);
 		}
 	}
 
 	/* Takes care of 'psuedo-groups' like <<triggerer>>, <<server>>, and <<everyone>>,
 	 * then sends to the rest as normal */
 	public void sendToGroups (String [] sendToGroups, String message, Player triggerer) {
 		ArrayList <String> sendToGroupsFiltered = new ArrayList<String>();
 		Hashtable <Player, Player> sendToUs = new Hashtable<Player, Player>();
 		for (String group : sendToGroups){
 			if (group.equalsIgnoreCase("<<triggerer>>")) {
 				if (triggerer != null){
 					sendToUs.put(triggerer, triggerer);
 				}
 			} else if (group.equalsIgnoreCase("<<everyone>>")){
 				sendToUs.clear();
 				for (Player putMe : MCServer.getPlayerList()) {
 					sendToUs.put(putMe, putMe);
 				}
 			} else if (group.equalsIgnoreCase("<<server>>")) {
 				String [] replace = {"<<recipient>>", "<<recipient-ip>>", "<<recipient-color>>", "<<recipient-balance>>"};
 				String [] with    = {"server", "", "", ""};
 				String serverMessage = "[rMotD] " + MessageParser.parseMessage(message, replace, with);
 				for(String send : serverMessage.split("\n"))
 					log.info(send);
 			} else if (group.equalsIgnoreCase("<<twitter>>")){
 				String [] replace = {"<<recipient>>", "<<recipient-ip>>", "<<recipient-color>>", "<<recipient-balance>>"};
 				String [] with    = {"Twitter", "", "", ""};
 				String twitterMessage = MessageParser.parseMessage(message, replace, with);
 				etc.getLoader().callCustomHook("tweet", new Object[] {twitterMessage});
 			} else if (group.equalsIgnoreCase("<<command>>")) {
 				if (triggerer != null) {
 					String command = message.substring(message.indexOf('/'));
 					triggerer.command(command);
 				}
 			} else if (group.substring(0,9).equalsIgnoreCase("<<player|")){
 				String playerName = group.substring(9, group.length()-2);
 				log.info(playerName);
 				Player putMe = MCServer.getPlayer(playerName);
 				if (putMe != null)
 					sendToUs.put(putMe, putMe);
 			} else {
 				sendToGroupsFiltered.add(group);
 			}
 		}
 		for (Player sendToMe : constructPlayerList(sendToGroupsFiltered.toArray(new String[sendToGroupsFiltered.size()]), sendToUs).values()){
 			sendToPlayer(message, sendToMe);
 		}
 	}
 
 	/* Sends the message string to each group named in sendToGroups */
 	public void sendToGroups (String [] sendToGroups, String message) {
 		for (Player sendToMe :  constructPlayerList(sendToGroups, new Hashtable<Player,Player>()).values()){
 			sendToPlayer(message, sendToMe);
 		}
 		return;
 	}
 	
 	public Hashtable<Player, Player> constructPlayerList(String [] inTheseGroups, Hashtable<Player,Player> List){
 		for (Player addMe: MCServer.getPlayerList()){
 			if (!List.contains(addMe)){
 				if (addMe.hasNoGroups()) {
 					search:
 					for (String isDefault : inTheseGroups) {
 						if (isDefault.equalsIgnoreCase(defaultGroup)) {
 							List.put(addMe,addMe);
 						}
 						break search;
 					}
 				} else {
 					search:
 					for(String memberGroup : addMe.getGroups()) {
 						for(String amIHere : inTheseGroups){
 							if (memberGroup.equalsIgnoreCase(amIHere)){
 								List.put(addMe, addMe);
 								break search;
 							}
 						}
 					}
 				}
 			}
 		}
 		return List;
 	}
 	
 	public void sendToPlayer(String message, Player recipient) {
 		int balance = 0;
 		if (data != null){
 			balance = data.getBalance(recipient.getName());
 		}
 		String [] replace = {"<<recipient>>"    , "<<recipient-ip>>" , "<<recipient-color>>", "<<recipient-balance>>"};
 		String [] with    = {recipient.getName(), recipient.getIP()  , recipient.getColor() , Integer.toString(balance)};
 		message = MessageParser.parseMessage(message, replace, with);
 		/* Tag replacement end. */
 		for(String send : message.split("\n"))
 			recipient.sendMessage(send);
 	}
 }
