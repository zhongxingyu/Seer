 /**
 This file is part of Like.
 
 Like is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
VoteforDay is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Like.  If not, see http://www.gnu.org/licenses/.
 **/
 
 package me.endorphin8er.Rater;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class Rate extends JavaPlugin implements Listener {
 	
 	public static Rate Plugin;
 	public final Logger logger = Logger.getLogger("Minecraft");
 	
 	// Define the GUI object
 	GUI gui;
 	
 	// Set up likes
 	HashMap<Player, Integer> likes = new HashMap<Player, Integer>();
 	// Set up dislikes
 	HashMap<Player, Integer> dislikes = new HashMap<Player, Integer>();
 	// Set up agrees
 	HashMap<Player, Integer> agrees = new HashMap<Player, Integer>();
 	// Set up disagrees
 	HashMap<Player, Integer> disagrees = new HashMap<Player, Integer>();
 	// Set up facepalms
 	HashMap<Player, Integer> facepalms = new HashMap<Player, Integer>();
 	
 	// Set up GUI object
 	HashMap<Player, GUI> player_gui = new HashMap<Player, GUI>();
 	
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " Has been Disabled!");
 	}
 	 
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() +  " Has been Enabled!");
 		
 		// Register event listeners
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	// This in theory frees up memory
 	@EventHandler
 	public void onDisconnect(PlayerQuitEvent event) {
 		if (player_gui.containsKey(event.getPlayer())) {
 			player_gui.remove(event.getPlayer());
 		}
 	}
 	
 	@EventHandler
 	public void buttonPress(ButtonClickEvent event){
 		
 		Player targetPlayer = null;
 		SpoutPlayer sp_target = null;
 		
 		if (player_gui.containsKey(event.getPlayer())) {
 			targetPlayer = player_gui.get(event.getPlayer()).isTargetPlayer();
 			sp_target = (SpoutPlayer) targetPlayer;
 		
 			// Like
 			if (player_gui.get(event.getPlayer()).isLikeButton(event.getButton())) {
 				event.getPlayer().getMainScreen().getActivePopup().close();
 				
 				// safety check
 				if (!likes.containsKey(targetPlayer)) {
 					likes.put(targetPlayer, 0);
 				}
 				
 				int likes_count = likes.get(targetPlayer) + 1;
 				
 				likes.put(targetPlayer, likes_count);
 				
 				if (sp_target.isSpoutCraftEnabled()) {
 					sp_target.sendNotification("Like", "You recieved a Like!", Material.GOLDEN_APPLE);
 				}
 			}
 			// DisLike
 			else if (player_gui.get(event.getPlayer()).isDisLikeButton(event.getButton())) {
 				event.getPlayer().getMainScreen().getActivePopup().close();
 			
 				// safety check
 				if (!dislikes.containsKey(targetPlayer)) {
 					dislikes.put(targetPlayer, 0);
 				}
 				
 				int dislikes_count = dislikes.get(targetPlayer) + 1;
 				
 				dislikes.put(targetPlayer, dislikes_count);
 				
 				if (sp_target.isSpoutCraftEnabled()) {
 					sp_target.sendNotification("Like", "You recieved a dis-Like!", Material.GOLDEN_APPLE);
 				}
 			}
 			// Agree
 			else if (player_gui.get(event.getPlayer()).isAgree(event.getButton())) {
 				event.getPlayer().getMainScreen().getActivePopup().close();
 				
 				// safety check
 				if (!agrees.containsKey(targetPlayer)) {
 					agrees.put(targetPlayer, 0);
 				}
 				
 				int agrees_count = agrees.get(targetPlayer) + 1;
 				
 				agrees.put(targetPlayer, agrees_count);
 				
 				if (sp_target.isSpoutCraftEnabled()) {
 					sp_target.sendNotification("Like", "Someone agrees with you!", Material.GOLDEN_APPLE);
 				}
 			}
 			// DisAgree
 			else if (player_gui.get(event.getPlayer()).isDisAgree(event.getButton())) {
 				event.getPlayer().getMainScreen().getActivePopup().close();
 				
 				// safety check
 				if (!disagrees.containsKey(targetPlayer)) {
 					disagrees.put(targetPlayer, 0);
 				}
 				
 				int disagrees_count = disagrees.get(targetPlayer) + 1;
 				
 				disagrees.put(targetPlayer, disagrees_count);
 				
 				if (sp_target.isSpoutCraftEnabled()) {
 					sp_target.sendNotification("Like", "Someone disagrees with you!", Material.GOLDEN_APPLE);
 				}
 			}
 			// Facepalm
 			else if (player_gui.get(event.getPlayer()).isFacePalm(event.getButton())) {
 				event.getPlayer().getMainScreen().getActivePopup().close();
 			
 				// safety check
 				if (!facepalms.containsKey(targetPlayer)) {
 					facepalms.put(targetPlayer, 0);
 				}
 				
 				int facepalms_count = facepalms.get(targetPlayer) + 1;
 				
 				facepalms.put(targetPlayer, facepalms_count);
 				
 				if (sp_target.isSpoutCraftEnabled()) {
 					sp_target.sendNotification("Like", "You recieved a FacePalm!", Material.GOLDEN_APPLE);
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void playerInteract(PlayerInteractEntityEvent event) {
 		if (event.getRightClicked() instanceof Player) {
 			gui = new GUI((SpoutPlayer) event.getPlayer(), (Player) event.getRightClicked(), this, this);
 			player_gui.put(event.getPlayer(), gui);
 		}
 	}
 			
 	// Public like return
 	public int Likes(Player player) {
 		// Safety check
 		if (!likes.containsKey(player)) {
 			likes.put(player, 0);
 		}
 		return likes.get(player);
 	}
 	
 	// Public dislike return
 	public int DisLikes(Player player) {
 		// Safety check
 		if (!dislikes.containsKey(player)) {
 			dislikes.put(player, 0);
 		}
 		return dislikes.get(player);
 	}
 	
 	// Public agree return
 	public int Agrees(Player player) {
 		// Safety check
 		if (!agrees.containsKey(player)) {
 			agrees.put(player, 0);
 		}
 		return agrees.get(player);
 	}
 	
 	// Public disagrees return
 	public int DisAgrees(Player player) {
 		// Safety check
 		if (!disagrees.containsKey(player)) {
 			disagrees.put(player, 0);
 		}
 		return disagrees.get(player);
 	}
 	
 	// Public facepalm return
 	public int Facepalms(Player player) {
 		// Safety check
 		if (!facepalms.containsKey(player)) {
 			facepalms.put(player, 0);
 		}
 		return facepalms.get(player);
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		Player player = (Player) sender;
 		
 		/*
 		 * On Like Command
 		 */
 		
 		if(commandLabel.equalsIgnoreCase("like")){
 			if(args.length == 0){
 				
 				player.sendMessage("----" + ChatColor.GREEN + "Like Help" + ChatColor.WHITE + "----");
 				player.sendMessage(ChatColor.AQUA + "/like" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows This Screen!");
 				player.sendMessage(ChatColor.AQUA + "/like <player's name>" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Sends A Message To The Player That You Like Them");
 				player.sendMessage(ChatColor.AQUA + "/like amount" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Amount Of Likes You Have!");
 				player.sendMessage(ChatColor.AQUA + "/facepalm" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows Facepalm Commands!");
 				player.sendMessage(ChatColor.AQUA + "/dislike" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows Dislike Commands!");
 				player.sendMessage(ChatColor.AQUA + "/agree" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows 'Agree' Commands!");
 				player.sendMessage(ChatColor.AQUA + "/disagree" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows 'Disagree' Commands!");
 			}
 			if(args.length == 1){
 				if(player.getServer().getPlayer(args[0]) !=null){
 					Player targetPlayer = player.getServer().getPlayer(args[0]);
 					if(targetPlayer.isOp()){
 						// Safety check for null pointers
 						if (!likes.containsKey(targetPlayer)) {
 							likes.put(targetPlayer, 0);
 						}
 						
 						int likes_count = likes.get(targetPlayer) + 1;
 						
 						// Increment the target player's like by 1
 						likes.put(targetPlayer, likes_count);
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Like From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + likes.get(targetPlayer) + " Likes!");
 					}
 					else{
 						// Safety check for null pointers
 						if (!likes.containsKey(player)) {
 							likes.put(player, 0);
 						}
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Like From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + likes.get(targetPlayer) + " Likes!");
 					}
 				}
 				if(player.isOp()){
 					// Amount parameter (Shows the amount of likes you have)
 					if(args[0].equalsIgnoreCase("amount")){
 						
 						// Safety check for null pointers
 						if (!likes.containsKey(player)) {
 							likes.put(player, 0);
 						}
 						// If likes are 0
 						if (likes.get(player) == 0) {
 							player.sendMessage(ChatColor.GREEN + "You have no Likes At The Moment");
 						}
 						// If likes are more than 0
 						else {
 							player.sendMessage(ChatColor.AQUA + "You Have" + likes.get(player) + "Likes!");
 						}
 						
 					}
 					// Undo a like
 					else if(args[0].equalsIgnoreCase("done")){
 						// Safety check
 						if (!likes.containsKey(player)) {
 							likes.put(player, 0);
 						}
 						
 						if (likes.get(player) == 0) {
 							player.sendMessage(ChatColor.DARK_RED + "You have No Likes!");
 						}
 						else {
 							int likes_count = likes.get(player) -1;
 							
 							likes.put(player, likes_count);
 							player.sendMessage(ChatColor.BLUE + "You Have " + likes.get(player) + " Likes Left!");
 						}
 					}
 					// Show the amount of likes you have
 					else if (args[0].equalsIgnoreCase("amount")) {
 						// Safety check
 						if (!likes.containsKey(player)) {
 							likes.put(player, 0);
 						}
 						
 						if (likes.get(player) == 0) {
 							player.sendMessage(ChatColor.DARK_RED + "You have No Likes!");
 						}
 						else {
 							player.sendMessage(ChatColor.BLUE + "You Have " + likes.get(player) + " Likes Left!");
 						}
 					}
 					// If no arguments pass
 					else {
 						return false;
 					}
 				}
 			}
 		}
 		/*
 		 * on Facepalm command		
 		 */
 		if(commandLabel.equalsIgnoreCase("facepalm")){
 			if(args.length == 0){
 					player.sendMessage("----" + ChatColor.GREEN + "Facepalm Help" + ChatColor.WHITE + "----");
 					player.sendMessage(ChatColor.AQUA + "/facepalm" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows This Screen!");
 					player.sendMessage(ChatColor.AQUA + "/facepalm <player's name>" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Sends A Message To The Player That You facepalmed Them");
 					player.sendMessage(ChatColor.AQUA + "/facepalm amount" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Amount Of Facepalms You Have!");
 			}
 			if(args.length == 1){
 				if(player.getServer().getPlayer(args[0]) !=null){
 					Player targetPlayer = player.getServer().getPlayer(args[0]);
 					if(targetPlayer.isOp()){
 						
 						// Safety check
 						if (!facepalms.containsKey(targetPlayer)) {
 							facepalms.put(targetPlayer, 0);
 						}
 						
 						int facepalms_count = facepalms.get(targetPlayer) + 1;
 						
 						facepalms.put(targetPlayer, facepalms_count);
 									
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Facepalm From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + facepalms.get(targetPlayer) + " Facepalms");
 					}
 					else {
 						// Safety check
 						if (!facepalms.containsKey(targetPlayer)) {
 							facepalms.put(targetPlayer, 0);
 						}
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Facepalm From " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN + "He is not an OP");
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + facepalms.get(targetPlayer) + " Facepalms!");
 					}
 				}
 				if(player.isOp()){
 					if(args[0].equalsIgnoreCase("done")){
 						
 						// Safety check
 						if (!facepalms.containsKey(player)) {
 							facepalms.put(player, 0);
 						}
 						
 						if (facepalms.get(player) == 0) {
 							player.sendMessage(ChatColor.DARK_RED + "You have no Facepalms!");
 						}
 						else {
 							
 							int facepalms_count = facepalms.get(player) -1;
 							
 							facepalms.put(player, facepalms_count);
 							player.sendMessage(ChatColor.BLUE + "You have " + facepalms.get(player) + " Facepalms!");
 						}
 									
 					}
 					// Amount
 					else if (args[0].equalsIgnoreCase("amount")){
 						// Safety check
 						if (!facepalms.containsKey(player)) {
 							facepalms.put(player, 0);
 						}
 						
 						if (facepalms.get(player) == 0) {
 							player.sendMessage(ChatColor.BLUE + "You have no Facepalms!");
 						}
 						else {
 							player.sendMessage(ChatColor.BLUE + "You have " + facepalms.get(player) + " Facepalms!");
 						}
 					}
 					// If all parameters fail
 					else {
 						return false;
 					}
 				}
 			}
 		}
 		/*
 		 * On Dislike Command
 		 */
 		if(commandLabel.equalsIgnoreCase("dislike")) {
 			if(args.length == 0){
 				player.sendMessage("----" + ChatColor.GREEN + "Dislike Help" + ChatColor.WHITE + "----");
 				player.sendMessage(ChatColor.AQUA + "/dislike" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows This Screen!");
 				player.sendMessage(ChatColor.AQUA + "/dislike amount" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Amount Of Dislikes You Have");
 				player.sendMessage(ChatColor.AQUA + "/dislike <player's name>" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Sends A Message To The Player That You Disliked Them");
 			}
 			if(args.length == 1){
 				if(player.getServer().getPlayer(args[0]) !=null){
 					// Set the target player to the argument
 					Player targetPlayer = player.getServer().getPlayer(args[0]);
 					if(targetPlayer.isOp()){
 						
 						if (!dislikes.containsKey(targetPlayer)) {
 							dislikes.put(targetPlayer, 0);
 						}
 						
 						int dislikes_count = dislikes.get(targetPlayer) +1;
 						
 						dislikes.put(targetPlayer, dislikes_count);
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Dislike From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + dislikes.get(targetPlayer) + " Dislikes");
 					}
 					else {
 						// Safety check
 						if (!dislikes.containsKey(targetPlayer)) {
 							dislikes.put(targetPlayer, 0);
 						}
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A Dislike From " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN + " He is not an OP");
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + dislikes.get(targetPlayer) + " Dislikes!");
 					}
 				}
 				if(player.isOp()){
 					if(args[0].equalsIgnoreCase("amount")){
 						
 						// Safety check
 						if (!dislikes.containsKey(player)) {
 							dislikes.put(player, 0);
 						}
 						
 						if (dislikes.get(player) == 0) {
 							player.sendMessage(ChatColor.GREEN + "You have No Dislikes At The Moment");
 						}
 						else {
 							player.sendMessage(ChatColor.AQUA + "You Have " + dislikes.get(player) + " Dislikes!");
 						}
 					}
 					else if(args[0].equalsIgnoreCase("done")){
 						
 						// Safety check
 						if (!dislikes.containsKey(player)) {
 							dislikes.put(player, 0);
 						}
 						
 						if (dislikes.get(player) == 0) {
 							player.sendMessage(ChatColor.GREEN + "You have No Dislikes At The Moment");
 						}
 						else {
 							// Subtract a dislike
 							int dilikes_count = dislikes.get(player) -1;
 							
 							dislikes.put(player, dilikes_count);
 							player.sendMessage(ChatColor.AQUA + "You Have " + dislikes.get(player) + " Dislikes!");
 						}
 
 					}
 					else {
 						return false;
 					}
 				}
 			}
 		}
 		/*
 		 * On agree command
 		 */
 		if(commandLabel.equalsIgnoreCase("agree")){
 			if(args.length == 0){
 				player.sendMessage("----" + ChatColor.GREEN + "Agree Help" + ChatColor.WHITE + "----");
 				player.sendMessage(ChatColor.AQUA + "/agree" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows This Screen!");
 				player.sendMessage(ChatColor.AQUA + "/agree amount" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Amount Of Agrees You Have");
 				player.sendMessage(ChatColor.AQUA + "/agree <player's name>" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Sends A Message To The Player That You Agree With Them");
 			}
 			if(args.length == 1){
 				if(player.getServer().getPlayer(args[0]) !=null){
 					Player targetPlayer = player.getServer().getPlayer(args[0]);
 					if(targetPlayer.isOp()){
 						
 						if (!agrees.containsKey(targetPlayer)) {
 							agrees.put(targetPlayer, 0);
 						}
 						
 						int agrees_count = agrees.get(player) +1;
 						
 						agrees.put(targetPlayer, agrees_count);
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A 'Agree' From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + agrees.get(targetPlayer) + " People That Agree With You");
 					}
 					else{
 						
 						// Safety check
 						if (!agrees.containsKey(targetPlayer)) {
 							agrees.put(targetPlayer, 0);
 						}
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A 'Agree' From " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN + " He is not an OP");
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + agrees.get(targetPlayer) + " People That Agree With You!");
 					}
 				}
 				if(player.isOp()){
 					if(args[0].equalsIgnoreCase("amount")){
 						
 						// Safety check
 						if (!agrees.containsKey(player)) {
 							agrees.put(player, 0);
 						}
 						
 						if(agrees.get(player) == 0){
 							player.sendMessage(ChatColor.GREEN + "No One Agrees With You At The Moment");
 						}
 						else{
 							player.sendMessage(ChatColor.AQUA + "You Have " + agrees.get(player) + " People That Agree!");
 						}
 					}	
 					else if(args[0].equalsIgnoreCase("done")){
 						
 						// Safety check
 						if (!agrees.containsKey(player)) {
 							agrees.put(player, 0);
 						}
 						
 						if(agrees.get(player) == 0){
 							player.sendMessage(ChatColor.DARK_RED + "You have no Agrees!");
 						}
 						else {
 							// Subtract 1 from your agrees
 							int agrees_count = agrees.get(player) -1;
 							
 							agrees.put(player, agrees_count);
 							player.sendMessage(ChatColor.AQUA + "You Have " + agrees.get(player) + " People That Agree!");
 						}
 					}
 					else {
 						return false;
 					}
 				}
 			}
 		}
 		/*
 		 * On disagree command
 		 */
 		if(commandLabel.equalsIgnoreCase("disagree")){
 			if(args.length == 0){
 				player.sendMessage("----" + ChatColor.GREEN + "Disagree Help" + ChatColor.WHITE + "----");
 				player.sendMessage(ChatColor.AQUA + "/disagree" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows This Screen!");
 				player.sendMessage(ChatColor.AQUA + "/disagree amount" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Shows The Amount Of Disagrees You Have");
 				player.sendMessage(ChatColor.AQUA + "/disagree <player's name>" + ChatColor.WHITE + " | " + ChatColor.GREEN + "Sends A Message To The Player That You Disagree With Them");
 			}
 			if(args.length == 1) {
 				if(player.getServer().getPlayer(args[0]) !=null){
 					Player targetPlayer = player.getServer().getPlayer(args[0]);
 					if(targetPlayer.isOp()){
 						
 						// Safety check
 						if (!disagrees.containsKey(targetPlayer)) {
 							disagrees.put(targetPlayer, 0);
 						}
 						
 						int disagrees_count = disagrees.get(targetPlayer) +1;
 						
 						disagrees.put(targetPlayer, disagrees_count);
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A 'Disagree' From " + ChatColor.DARK_AQUA + player.getName());
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + disagrees.get(targetPlayer) + " People That Disagree With You");
 					}
 					else{
 						// Safety check
 						if (!disagrees.containsKey(targetPlayer)) {
 							disagrees.put(targetPlayer, 0);
 						}
 						
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Have A 'Disagree' From " + ChatColor.DARK_AQUA + player.getName() + " He is not an OP");
 						targetPlayer.sendMessage(ChatColor.GREEN + "You Now Have " + disagrees.get(targetPlayer) + " People That Disagree With You!");
 					}
 				}
 				if(player.isOp()){
 					if(args[0].equalsIgnoreCase("amount")){
 						
 						// Safety check
 						if (!disagrees.containsKey(player)) {
 							disagrees.put(player, 0);
 						}
 						
 						if (disagrees.get(player) == 0) {
 							player.sendMessage(ChatColor.GREEN + "There Are No People That Disagree With You At The Moment");
 						}
 						else {
 							player.sendMessage(ChatColor.AQUA + "You Have " + disagrees.get(player) + " People That Disagree!");
 						}
 						
 					}
 					else if(args[0].equalsIgnoreCase("done")){
 						
 						// Safety check
 						if (!disagrees.containsKey(player)) {
 							disagrees.put(player, 0);
 						}
 						
 						if (disagrees.get(player) == 0) {
 							player.sendMessage(ChatColor.BLUE + "There Are No People That Disagree With You left!");
 						}
 						else {
 							int disagrees_count = disagrees.get(player) -1;
 							
 							disagrees.put(player, disagrees_count);
 							player.sendMessage(ChatColor.BLUE + "You Have " + disagrees.get(player) + " People That Disagree!");
 						}
 
 					}
 					else {
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 }
