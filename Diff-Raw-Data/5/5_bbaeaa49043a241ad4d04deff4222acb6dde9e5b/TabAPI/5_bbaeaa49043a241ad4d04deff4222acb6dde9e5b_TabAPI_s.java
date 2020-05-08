 package org.mcsg.double0negative.tabapi;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.HashSet;
 
 
 import net.minecraft.server.v1_4_R1.Packet201PlayerInfo;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.protocol.Packets;
 import com.comphenix.protocol.ProtocolLibrary;
 import com.comphenix.protocol.ProtocolManager;
 import com.comphenix.protocol.events.ConnectionSide;
 import com.comphenix.protocol.events.ListenerPriority;
 import com.comphenix.protocol.events.PacketAdapter;
 import com.comphenix.protocol.events.PacketContainer;
 import com.comphenix.protocol.events.PacketEvent;
 
 
 /**
  * TabAPI
  * 
  * Provides a simple interface for adding custom text to 
  * display on the minecraft tab menu on a player/plugin 
  * basis
  * 
  * @author Double0negative
  * 
  *
  */
 
 
 public class TabAPI extends JavaPlugin implements Listener, CommandExecutor{
 
 
 	private static HashMap<String, TabObject>playerTab = new HashMap<String, TabObject>();
 	private static HashMap<String, TabHolder>playerTabLast = new HashMap<String, TabHolder>();
 
 	private static int horzTabSize = 3;
 	private static int vertTabSize = 20;
 	private static String[] colors = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "a","b", "c", "d", "f"};
 
 	private static int e = 0;
 	private static int r = 0;
 
 	private static ProtocolManager protocolManager;
 
 	private static boolean shuttingdown = false;
 	
 	public void onEnable(){
 
 		this.getCommand("tabapi").setExecutor(this);
 		
 		try {
 			new Metrics(this).start();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		protocolManager = ProtocolLibrary.getProtocolManager();
 
 		
 		Bukkit.getServer().getPluginManager().registerEvents(this, this);
 		for(Player p: Bukkit.getOnlinePlayers()){
 			Plugin plugin = Bukkit.getPluginManager().getPlugin("TabAPI");
 			setPriority(plugin, p, 2);
 			resetTabList(p);
 			setPriority(plugin, p, -2);
 			
 		}
 
 
 		protocolManager.addPacketListener(new PacketAdapter(this,
 				ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, Packets.Server.PLAYER_INFO) {
 			@Override
 			public void onPacketSending(PacketEvent event) {
 				switch (event.getPacketID()) {
 				case Packets.Server.PLAYER_INFO:
 					PacketContainer p = event.getPacket();
 					String s = p.getStrings().read(0);
 					if(s.startsWith("$")){  // this is a packet sent by TabAPI **Work around until I figure out how to make my own
 						p.getStrings().write(0,s.substring(1));  // packets bypass this block**
 						event.setPacket(p);
 					}
 					else{
 						event.setCancelled(true);
 					}
 					break;
 				}
 			}
 		});
 
 
 	}
 
 	public void onDisable(){
 		shuttingdown = true;
 		for(Player p: Bukkit.getOnlinePlayers()){
 			clearTab(p);
 		}
 		playerTab = null;
 		playerTabLast = null;
 	}
 	
     @Override
     public boolean onCommand(CommandSender sender, Command cmd1, String commandLabel, String[] args){
         PluginDescriptionFile pdfFile = this.getDescription();
 
         Player player = null;
         if (sender instanceof Player) {
             player = (Player) sender;
             if(args.length == 1 && player.hasPermission("tabapi.reload")){
             	updateAll();
             }
             else{
                 player.sendMessage(ChatColor.DARK_RED +""+ ChatColor.BOLD +"TabAPI - Double0negative"+ChatColor.RESET+  ChatColor.RED +" Version: "+ pdfFile.getVersion() );
 
             }
         }
         else{
             sender.sendMessage(ChatColor.DARK_RED +""+ ChatColor.BOLD +"TabAPI - Double0negative"+ChatColor.RESET+  ChatColor.RED +" Version: "+ pdfFile.getVersion() );
             return true;
         }
         
         return true;
         
     }
 
 	private static void sendPacket(Player p, String msg, boolean b, int ping){
 		PacketContainer message = protocolManager.createPacket(Packets.Server.PLAYER_INFO);
 		message.getStrings().write(0, ((!shuttingdown)?"$":"")+msg);
 		message.getBooleans().write(0, b);
 		message.getIntegers().write(0, ping);
 		try {
 			protocolManager.sendServerPacket(p, message);
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 			System.out.println("[TabAPI] Error sending packet to client");
 		}
 	}
 	
 	
 	private static TabObject getTab(Player p){
 		TabObject tabo = playerTab.get(p.getName());
 		if(tabo == null){
 			tabo = new TabObject();
 			playerTab.put(p.getName(), tabo);
 			//tabo.getTab().tab = new String[3][20];
 		}
 		return tabo;
 	}
 	
 
 	/**
 	 * Priorities
 	 * 
 	 * -2 = no longer active, remove
 	 * -1 = background, only show if nothing else is there
 	 *  0 = normal
 	 *  1 = high priority
 	 *  2 = always show, only use if MUST show
 	 * 
 	 */
 	public static void setPriority(Plugin plugin, Player player, int pri){
 		getTab(player).setPriority(plugin, pri);
 	}
 
 	/**
 	 * Returns the tab list to the vanilla tab list for a player. 
 	 * If another plugin holds higher priority, this does notning
 	 * @param p
 	 */
 	public static void disableTabForPlayer(Player p){
 		playerTab.put(p.getName(), null);
 		resetTabList(p);
 	}
 
 	/**
 	 * Resets tab to normal tab. 
 	 * @param p
 	 */
 	public static void resetTabList(Player p){
 		int a = 0; int b = 0;
 
 		for(Player pl : Bukkit.getOnlinePlayers()){
 			setTabString(Bukkit.getPluginManager().getPlugin("TabAPI"), p, a, b, pl.getPlayerListName());
 			b++;
			if(b > horzTabSize){
 				b = 0;
 				a++;
 			}
 		}
 	}
 
 	public static void setTabString(Plugin plugin, Player p, int x, int y, String msg){
 	setTabString(plugin, p, x, y, msg, 0);
 		
 	}
 	
 	/**
 	 * Set the tab for a player. 
 	 * 
 	 * If the plugin the tab is being set from does not have a priority, It will automatically be give a base
 	 * priority of 0
 	 * 
 	 * @param plugin
 	 * @param p
 	 * @param x
 	 * @param y
 	 * @param msg
 	 * @param ping
 	 */
 	public static void setTabString(Plugin plugin, Player p, int x, int y, String msg, int ping){
 		try{
 			TabObject tabo = getTab(p);
 			tabo.setTab(plugin, x, y,msg+":"+ping);
 			playerTab.put(p.getName(), tabo);
 		}catch(Exception e){e.printStackTrace();}
 
 	}
 
 
 	/**
 	 * Updates a players tab
 	 * 
 	 * A tab will be updated with the tab from the highest priority plugin
 	 * 
 	 * @param p
 	 */
 	public static void updatePlayer(Player p){
 		if(!p.isOnline()) return;
 		r = 0; e = 0;
 		TabObject tabo = playerTab.get(p.getName());
 
 		/* need to clear the tab first */
 		clearTab(p);
 		TabHolder tab = tabo.getTab();
 
 		if(tab == null){
 			return;
 		}
 		
 		for(int b = 0; b < tab.maxv; b++){
 			for(int a = 0; a < tab.maxh ; a++){	
 				if(tab.tab[a][b] == null){
 					tab.tab[a][b] = nextNull();
 				}
 
 				String msg2 = tab.tab[a][b];
 				String[] split = msg2.split(":");
 				String msg = split[0];
 				int ping = 0;
 				if(split.length == 2)
 				 ping = Integer.parseInt(split[1]);
 			//System.out.print(a+":"+b+":"+msg);
 				sendPacket(p, (msg == null)? " ": msg.substring(0, Math.min(msg.length(), 16)), true, ping);
 			}
 		}
 
 		TabHolder o = new TabHolder();
 		o.tab = copyArray(tabo.getTab().tab);
 		playerTabLast.put(p.getName(),o);
 	}
 
 	/**
 	 * Clear a players tab menu
 	 * @param p
 	 */
 	public static void clearTab(Player p){
 		if(!p.isOnline())return;
 		//System.out.println("Clearing");
 		TabHolder tabold = playerTabLast.get(p.getName());
 
 		/*for(Player pl: Bukkit.getOnlinePlayers()){
 			sendPacket(p, pl.getPlayerListName().substring(0, Math.min(pl.getPlayerListName().length(), 16)), false, 0);
 		}*/
 
 		if(tabold != null){
 			for(String [] s: tabold.tab){
 				for(String str:s){					
 					String msg = str;
 					if(msg != null){
 						String[] split = msg.split(":");
 						String msg2 = split[0];
 						sendPacket(p, (msg2 == null)? " ": msg2.substring(0, Math.min(msg2.length(), 16)), false, 0);
 					}
 				}
 			}
 		}
 
 	}
 
 	public static void updateAll(){
 		for(Player p:Bukkit.getOnlinePlayers()){
 			updatePlayer(p);
 		
 		}
 	}
 
 
 	/* return the next null filler */
 	public static String nextNull(){
 		String s = "";
 		for(int a = 0; a < r; a++){
 			s = " "+s;
 		}
		s = s + "" + colors[e];
 		e++; 
 		if(e > 14){
 			e = 0;
 			r++;
 		}
 		return s;
 	}
 
 	/* Util method, copy tab array to new array */
 	private static String[][] copyArray(String[][] tab){
 		String[][] temp = new String[horzTabSize][vertTabSize];
 		for(int b = 0; b < vertTabSize; b++){
 			for(int a = 0; a < horzTabSize ; a++){
 				temp[a][b] = tab[a][b];
 			}
 		}
 		return temp;
 	}
 
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void PlayerJoin(PlayerJoinEvent e){
 		//ensure that the join packet has actually been sent, send d/c packet
 	/*	final Player p = e.getPlayer();
 		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
 			public void run(){
 				for(Player p: Bukkit.getOnlinePlayers()){
 					if(playerTab.get(p.getName()) != null){
 						((CraftPlayer)p).getHandle().playerConnection.sendPacket(new Packet201PlayerInfo(p.getName(), false, 0));
 					}
 				}
 			}
 		}, 1);*/
 	}
 
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void PlayerLeave(PlayerQuitEvent e){
 		//cleanup
 		playerTab.remove(e.getPlayer().getName());
 		playerTabLast.remove(e.getPlayer().getName());
 	}
 
 	public static int getVertSize(){
 		return vertTabSize;
 	}
 	
 	public static int getHorizSize(){
 		return horzTabSize;
 	}
 }
