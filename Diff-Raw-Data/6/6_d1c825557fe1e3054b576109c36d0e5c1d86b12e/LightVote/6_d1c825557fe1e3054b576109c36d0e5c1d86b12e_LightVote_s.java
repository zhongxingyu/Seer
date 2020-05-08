 package nl.xupwup.LightVote;
 
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.event.Event;
 
 
 /**
  * LightVote for Bukkit
  *
  * @author XUPWUP
  */
 public class LightVote extends JavaPlugin {
     private LVTPlayerListener playerListener;
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 	private Logger log;
     private String name;
     private String version;
     File voteStartersFile;
     HashSet<String> voters;
     
     // default configuration
     private double reqYesVotes = 0.05, minAgree = 0.5;
 	private int permaOffset = 4000; 
 	private int voteTime = 30000, voteFailDelay = 30000, votePassDelay = 50000, voteRemindCount = 2;
 	private boolean bedVote = false;
 	private boolean perma = false;
 	private boolean debugMessages;
 	private static final String defaultConfig = "# At least 'required-yes-percentage'*peopleOnServer people must vote yes, and there must be more people that voted yes than no" + '\n' + 
 		"required-yes-percentage 5" + '\n' +
 	 	"minimum-agree-percentage 50" + '\n' +
 		"vote-fail-delay 30" + '\n' +
 		"vote-pass-delay 50" + '\n' +
 		"vote-time 30" + '\n' +
 		"reminders 2" + '\n' +
 		"bedvote no" + '\n' +
 		"permanent no";
     
     private void parseSettings(Scanner sc){ 
 		while(sc.hasNext()){
 			String thisline = sc.nextLine();
 			String[] contents = thisline.split(" ");
 			if (contents.length > 1){
 				if (contents[0].equals("minimum-agree-percentage")){
 						minAgree = Integer.parseInt(contents[1]);
 						minAgree /= 100;
 				}else if (contents[0].equals("required-yes-percentage")){
 						reqYesVotes = Integer.parseInt(contents[1]);
 						reqYesVotes /= 100;
 				}else if (contents[0].equals("vote-fail-delay")){
 						voteFailDelay = Integer.parseInt(contents[1]) * 1000;
 				}else if (contents[0].equals("vote-pass-delay")){
 						votePassDelay = Integer.parseInt(contents[1]) * 1000;
 				}else if (contents[0].equals("vote-time")){
 						voteTime = Integer.parseInt(contents[1]) * 1000;
 				}else if (contents[0].equals("reminders")){
 						voteRemindCount = Integer.parseInt(contents[1]);
 				}else if (contents[0].equals("permanent")){
 						perma = contents[1].equals("yes");
 				}else if (contents[0].equals("bedvote")){
 						bedVote = contents[1].equals("yes");
 				}else if (contents[0].equals("debug-messages")){
 					debugMessages = contents[1].equals("yes");
 				}
 			}
 		}
 	}
 
    
 
     public void onEnable() {        
        playerListener = new LVTPlayerListener(this, log);
         sM("Initialised");
     	
     	// Register event for beds
 		PluginManager pm = this.getServer().getPluginManager();
     	pm.registerEvent(Event.Type.PLAYER_BED_ENTER, playerListener, Event.Priority.Normal, this);
 		
         
         File folder = new File("plugins" + File.separator + "LightVote");
         if (!folder.exists()) {
             folder.mkdir();
         }
         
         File configFile = new File(folder.getAbsolutePath() + File.separator +"LightVote.conf");
         if (configFile.exists()){
         	sM("Scanning properties file.");
         	Scanner sc = null;
         	try {
 				sc = new Scanner(configFile);
 				parseSettings(sc);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
         }else{
         	sM("Creating properties file.");
         	BufferedWriter out = null;
         	try {
 				configFile.createNewFile();
 				out = new BufferedWriter(new FileWriter(configFile));
 				out.write(defaultConfig);
 				out.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
         }
         
         voteStartersFile = new File(folder.getAbsolutePath() + File.separator +"voteStarters.conf");
         if(voteStartersFile.exists()){
         	updateVoters(voteStartersFile);
         }else{
         	BufferedWriter out = null;
         	try {
 				voteStartersFile.createNewFile();
 				out = new BufferedWriter(new FileWriter(voteStartersFile));
 				out.write("*");
 				out.close();
 				voters = null;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
         }
         
         playerListener.config(reqYesVotes, minAgree, permaOffset, voteTime, voteFailDelay, votePassDelay, voteRemindCount, perma, voters, bedVote);
 
         if(perma){
         	playerListener.setReset();
         }
     }
     
     private void updateVoters(File f){
     	try {
 			Scanner sc = new Scanner(f);
 			voters = new HashSet<String>();
 			while(sc.hasNext()){
 				String name = sc.next();
 				if (name.equals("*")){
 					voters = null;
 					return;
 				}
 				voters.add(name.toLowerCase());
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command,
     		String label, String[] args) {
 
     	return playerListener.onPlayerCommand(sender, command, label, args);
     }
     public void onDisable() {
     	if (playerListener.tReset != null) playerListener.tReset.cancel();
 
         // NOTE: All registered events are automatically unregistered when a plugin is disabled
 
         sM("Disabled");
     }
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
 
     public void sM(String message) {
    	log = Logger.getLogger("Minecraft");
     	PluginDescriptionFile pdfFile = getDescription();
     	log.info("[" + pdfFile.getName() + ":" + pdfFile.getVersion() + "] " + message);
     }
 
     public void sMdebug(String message) {
     	if (debugMessages) {
     		sM(message);
     	}
     }
 }
