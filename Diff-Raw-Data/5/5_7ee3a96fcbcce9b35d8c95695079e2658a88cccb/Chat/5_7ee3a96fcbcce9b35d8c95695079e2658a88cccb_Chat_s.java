 package ru.ufatos.chat;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.configuration.file.FileConfiguration;
 
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 
 
 @SuppressWarnings("deprecation")
 public class Chat implements Listener {
 	
 	private double RangeMain;
 	private double RangeWhispering;
 	private double RangeShout;
 	private double RangeAction;
 	private boolean DeathMessage;
 	private double ConfigChance;
 	protected static Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-F])");
 	protected static Pattern chatMagicPattern = Pattern.compile("(?i)&([K])");
 	protected static Pattern chatBoldPattern = Pattern.compile("(?i)&([L])");
 	protected static Pattern chatStrikethroughPattern = Pattern.compile("(?i)&([M])");
 	protected static Pattern chatUnderlinePattern = Pattern.compile("(?i)&([N])");
 	protected static Pattern chatItalicPattern = Pattern.compile("(?i)&([O])");
 	protected static Pattern chatResetPattern = Pattern.compile("(?i)&([R])");
 	protected static Pattern spaceF = Pattern.compile("^\\s+");
 	
 	public Chat(FileConfiguration config) {
 		this.RangeMain = config.getDouble("Range.main", this.RangeMain);
 		this.RangeWhispering = config.getDouble("Range.whispering", this.RangeWhispering);
 		this.RangeShout = config.getDouble("Range.shout", this.RangeShout);
 		this.RangeAction = config.getDouble("Range.action", this.RangeAction);
 		this.DeathMessage = config.getBoolean("disableDeathMessage", this.DeathMessage);
 		this.ConfigChance = config.getDouble("ConfigChance", this.ConfigChance);
 
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		updateDisplayName(event.getPlayer());
 		event.setJoinMessage (null);
 	}
 	
 	@EventHandler
 	public void onPlayerChat(PlayerChatEvent event) {
 	  	Player player = event.getPlayer();
 		String message = "%1$s: %2$s";		
 		String chatMessage = event.getMessage();	
 		boolean ranged = true; // я не знаю как это назвать, поэтому ranged (органичен ли чат)
 		double range = RangeMain; 
 		
 		if (chatMessage.startsWith("#") && chatMessage.length() > 1 ) {			
 			if (player.hasPermission("rpchat.global")){
 				ranged = false;
 				message = "%1$s [GlobalChat]: %2$s";
 				chatMessage = ChatColor.GOLD+delSpace(chatMessage.substring(1));
 			}
 			else {
 				player.sendMessage("У вас нет прав писать в глобальный чат");
 				event.setCancelled(true);
 			}			
 		}			
 		
 		
 			if (chatMessage.startsWith("!") && chatMessage.length() > 1 ) {
 				range = RangeShout;
 				message = "%1$s кричит: %2$s";
 				//chatMessage = ChatColor.BOLD+chatMessage.substring(1); жирный смотрится плохо
 				chatMessage = ChatColor.RED+delSpace(chatMessage.substring(1));
 			}
 			
 			if (chatMessage.startsWith("@") && chatMessage.length() > 1 ) {
 				range = RangeWhispering;
 				message = "%1$s шепчет: %2$s";
 				chatMessage = ChatColor.ITALIC+delSpace(chatMessage.substring(1));
 				chatMessage = ChatColor.GRAY+chatMessage;			
 			}
 			
			if (chatMessage.startsWith("***") {
 				if (chatMessage.length() > 3) {
 					range = RangeAction;
 					chatMessage = ChatColor.LIGHT_PURPLE+delSpace(chatMessage.substring(3));
 					double chance = Math.random()*100;
 					//Не уверен в работоспособности тернарного оператора в формирование строки
 					String luck=ChatColor.RED+((chance<ConfigChance)?"(не удачно)":"(удачно)")+ChatColor.LIGHT_PURPLE;
 					message = ChatColor.LIGHT_PURPLE+"**%1$s %2$s "+luck+" **";
 				}else{
 					double chance = Math.random()*100;
 					chatMessage = ChatColor.LIGHT_PURPLE+delSpace(chatMessage.substring(3));
 					message = ChatColor.LIGHT_PURPLE+"**%1$s выбрасывает "+Double.toString(chance)+" **";	
 				}
 			}
 			
 			if (chatMessage.startsWith("**") && chatMessage.length() > 2) {
 			range = RangeAction;
 			message = ChatColor.LIGHT_PURPLE+"**%1$s %2$s**";
 			chatMessage = delSpace(chatMessage.substring(2));	
 			}
 			
 		if (ranged)
 		{	
 			event.getRecipients().clear();
 			event.getRecipients().addAll(this.getLocalRecipients(player, message, range));
 		}
 		event.setFormat(message);
 		event.setMessage(chatMessage);	
 	    }
 	/*убираем сообщение о смерти*/
 	 @EventHandler(priority = EventPriority.NORMAL)
 	 public void onEntityDeath(EntityDeathEvent event) {
 		 if (DeathMessage){
 			 if (event instanceof PlayerDeathEvent) {
 				 PlayerDeathEvent deathEvent = (PlayerDeathEvent) event;
 				 deathEvent.setDeathMessage(null);
          	}
 		 }
 	 }
 		
 	 //Тут немного взял у ChatManager
 	protected List<Player> getLocalRecipients(Player sender, String message, double range) {
 		Location playerLocation = sender.getLocation();
 		List<Player> recipients = new LinkedList<Player>();
 		double squaredDistance = Math.pow(range, 2);
 		for (Player recipient : Bukkit.getServer().getOnlinePlayers()) {
 		  if (recipient.getWorld().equals(sender.getWorld())  && playerLocation.distanceSquared(recipient.getLocation()) < squaredDistance) {
            	     recipients.add(recipient);
          	  } else if (main.hasPermission(recipient, "rpchat.spy")) {
             	     recipients.add(recipient);
           	  } else {
            	    continue;
           	  }
 		}
 		return recipients;		
 	}
 	
 	protected static String translateColorCodes(String string) {
 		if (string == null) {
 			return "";
 		}
 
 		String newstring = string;
 		newstring = chatColorPattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatMagicPattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatBoldPattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatStrikethroughPattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatUnderlinePattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatItalicPattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatResetPattern.matcher(newstring).replaceAll("\u00A7$1");
 		return newstring;
 	}
 	static void updateDisplayNames() {
 		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 			updateDisplayName(player);
 		}
 	}
 	public String delSpace(String chatMessage) {
 		Matcher space = spaceF.matcher(chatMessage);
 		chatMessage = space.replaceFirst("");
 		return chatMessage;
 	}
 
 	static void updateDisplayName(Player player) {
 		PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
 		if (user == null) {
 			return;
 		}
 		player.setDisplayName(Chat.translateColorCodes(user.getPrefix()+player.getName()));
 		}
 }
