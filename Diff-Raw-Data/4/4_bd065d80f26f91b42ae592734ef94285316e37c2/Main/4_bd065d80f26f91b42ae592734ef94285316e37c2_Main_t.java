 package com.Jessy1237.RTest;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.Random;
 import java.util.Timer;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin{
 	
	public static String vernum = "1.2.3";
 	private Commands Exe;
 	static int b = 0;
 	private final RTestPlayerListener playerlistener = new RTestPlayerListener(this);
 	static Logger log = Logger.getLogger("Minecraft");
 	static String message = "";
 	static String rNum() throws IOException{
 		return randomString(charAmount());
 	}
 	static String dir = "plugins/RTest";
 	static File conf = new File(dir, "/properties.yml");
 	static Properties prop = new Properties();
 	static String Currency;
 	static String Currency(){
 		if(!conf.exists()){
 			Currency = "Dollars";
 		}else{
 			try{
 				FileInputStream in = new FileInputStream(conf);
 				prop.load(in);
 				Currency = prop.getProperty("Currency");
				in.close();
 			}catch(IOException c){
 				c.printStackTrace();
 			}
 		}return Currency;	
 	}
 	static int charAmount;
 	static int charAmount() throws IOException{
 		if(!conf.exists()){
 		    charAmount = 10;
 		}else{
 			FileInputStream in = new FileInputStream(conf);
 			prop.load(in);
 			charAmount = Integer.parseInt(prop.getProperty("charAmount"));
 			in.close();
 		}return charAmount;
 	}
 	static int delay;
 	static int delay() throws IOException{
 		if(!conf.exists()){
 			delay = 180000;
 		}else{
 			FileInputStream in = new FileInputStream(conf);
 			prop.load(in);
 			delay = Integer.parseInt(prop.getProperty("delay"));
 			in.close();
 		}return delay;
 	}
 	static int reward;
 	static int reward(){
 		if(!conf.exists()){
 			reward = 500;
 		}else{
 			try{
 			FileInputStream in = new FileInputStream(conf);
 			prop.load(in);
 			reward = Integer.parseInt(prop.getProperty("reward"));
 			in.close();
 			}catch(IOException b){
 				b.printStackTrace();
 			}
 		}return reward;
 	}
 	Timer timer;
 	static javax.swing.Timer t;
 	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 	static Random rnd = new Random();
 
 	static String randomString( int len ) 
 	{
 	   StringBuilder sb = new StringBuilder( len );
 	   for( int i = 0; i < len; i++ ) 
 	      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
 	   return sb.toString();
 	}
 
 	public void onEnable() {
 	Exe = new Commands(this);
 	getCommand("RTest").setExecutor(Exe);
     PluginManager pm = getServer().getPluginManager();
     log.info("[RTest] RTest " + vernum + " Enabled");
     loadProp();
     loadRnum.loadprops1();
     t.start();
     Hooks();
     Plugin iConomyPlugin = pm.getPlugin("iConomy");
     if(iConomyPlugin == null){
     	log.info("[RTest] Unable to find iConomy, disabling...");
     	pm.disablePlugin(this);
     	return;
     	}else{
     		log.info("[RTest] Successfully hooked into iConomy.");
     	}
 	}
 
 
 	public void onDisable() {
 	log.info("[RTest]Disabling RTest " + vernum + " ....");
 	t.stop();
 	}
 	
 	public Main() throws IOException{
 			timer = new Timer();
 			t = new javax.swing.Timer(delay(), new ActionListener(){				
 			public void actionPerformed(ActionEvent e){
 					try {
 						RTestPlayerListener.enable();
 						loadRnum.dNum();
 						loadRnum.dNum1();
 						sendMessage(message);
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 			}
 		});
 	}
 			
 	
 	public void sendMessage(String message) throws IOException{
 			Player players[] = getServer().getOnlinePlayers();
 			for(int i = 0; i <players.length; i++)
 			players[i].sendMessage((new StringBuilder()).append(ChatColor.RED).append("[RTest] First Player to Type: " + loadRnum.dNum + " Wins " + reward() + " " + Currency()).append(message).toString());
 		    log.info("[RTest] Displaying Reaction Test to Online Players...");
 	}
 	
 	public void loadProp(){
 		(new File(dir)).mkdir();
 		if(!conf.exists()){
 			log.info("[RTest] Property File not found!");
 			log.info("[RTest] Creating default properties...");
 			try{
 				conf.createNewFile();
 				FileOutputStream out = new FileOutputStream(conf);
 				prop.put("charAmount", Integer.toString(10));
 				prop.put("reward", Integer.toString(500));
 				prop.put("delay", Integer.toString(180000));
 				prop.put("Currency", "Dollars");
 				prop.store(out, "delay is in milliseconds, Reward is the amount of money the winner is payed, CharAmount is the amount of characters the player has to type and Currency is the Name of the Currency of your server.");
 				out.flush();
 				out.close();
 				log.info("[RTest] Property file created.");
 			}catch(IOException ex){
 				ex.printStackTrace();
 			}
 		}else{
 		log.info("[RTest] Loading Properties...");
 		}
 	}
 	
 	public void Hooks(){
 	    PluginManager pm = getServer().getPluginManager();
 	    pm.registerEvent(Event.Type.PLAYER_CHAT, playerlistener, Priority.Normal, this);
 	}
 }
