 package net.ST392.ChangeHorse;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 
 public class CHCommandExecutor implements CommandExecutor {
 	
 	String mainArgs = ChatColor.GREEN + "/ChangeHorse (set/get)";
 	String getArgs = ChatColor.YELLOW + "A valid property to get is required. " + ChatColor.GREEN + " Options are: Type, Color, Style, JumpStrength";
 	String setArgs = ChatColor.YELLOW + "A valid property to set is required. " + ChatColor.GREEN + " Options are: Type, Color, Style, JumpStrength, MaxHealth";
 
 	//Class that handles slash commands from user
 	//=============================================
 	private ChangeHorse plugin;
 	public CHCommandExecutor(ChangeHorse plugin) {
 		this.plugin = plugin;
 	}
 	//=============================================
 	
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 
 		//Make sure command was not sent from console
 		if (sender instanceof Player == false){
 			plugin.getLog().info("/ChangeHorse is only available in game.");
 			return true;
 		}
 
 		//Convert sender to Player
 		Player player = (Player) sender;
 
 		if (!player.isInsideVehicle() || !(player.getVehicle() instanceof Horse)) {
 
 			player.sendMessage(ChatColor.YELLOW + "Must have argument and must be riding a horse.");
			return true; 
 		}
 
 		if(args.length == 0){
 			player.sendMessage(mainArgs);	
 			return true;
 		}		
 
 
 
 		//Get the Horse
 		Horse horse = (Horse) player.getVehicle();
 
 		String property = null;
 		try { property = args[0].toLowerCase(); } catch(NumberFormatException e) {}
 
 		if(property == null){
 			player.sendMessage("Invalid argument");
 			return true;
 		}
 
 		if(args.length > 3){
			args[2] += args[3];
 		}
 
 		if(property.equals("get")){
 			if(args.length < 2){	//ADD NEW PROPERTIES HERE
 				player.sendMessage(getArgs);
 				return true;
 			}
 
 			String option = args[1].toLowerCase();
 
 			if(option.equals("type") || option.equals("t")){
 				if(!player.hasPermission("changehorse.get.type")){
 					player.sendMessage(ChatColor.RED + "You don't have permission!");
					return true;
 				}
 				player.sendMessage(ChatColor.GREEN + "Horse type is: " + horse.getType());
 				return true;
 			}
 			if(option.equals("color") || option.equals("c")){
 				if(!player.hasPermission("changehorse.get.color")){
 					player.sendMessage(ChatColor.RED + "You don't have permission!");
					return true;
 				}
 				player.sendMessage(ChatColor.GREEN + "Horse color is: " + horse.getColor());
 				return true;
 			}
 			if(option.equals("style") || option.equals("st")){
 				if(!player.hasPermission("changehorse.get.style")){
 					player.sendMessage(ChatColor.RED + "You don't have permission!");
					return true;
 				}
 				player.sendMessage(ChatColor.GREEN + "Horse style is: " + horse.getStyle());
 				return true;
 			}
 			if(option.equals("jumpstrength") || option.equals("js")){
 				if(!player.hasPermission("changehorse.get.jumpstrength")){
 					player.sendMessage(ChatColor.RED + "You don't have permission!");
					return true;
 				}
 				player.sendMessage(ChatColor.GREEN + "Horse type is: " + horse.getJumpStrength()*50);
 				return true;
 			}
 			player.sendMessage(getArgs);
 			return true;
 
 		}else if(property.equals("set")){
 			if(args.length < 2){	//ADD NEW PROPERTIES HERE
 				player.sendMessage(ChatColor.YELLOW + setArgs);
 				return true;
 			}
 
 			String option = args[1].toLowerCase();
 
 			if(option.equals("type") || option.equals("t")){
 				if(!player.hasPermission("changehorse.set.type")){
 					player.sendMessage(ChatColor.RED + "You don't have permission!");
					return true;
 				}
 
 				if(args.length == 2){
 					player.sendMessage(ChatColor.GREEN + "Type options are:");
 					for(Horse.Variant variant: Horse.Variant.values()){
 						player.sendMessage(ChatColor.GREEN + "     " + variant.toString().toLowerCase());
 					}
 					return true;
 				}else{
 					for(Horse.Variant variant: Horse.Variant.values()){
 						if(variant.toString().toLowerCase().replaceAll("_", "").replaceAll("horse", "").equals(args[2].toLowerCase().replaceAll("_", "").replaceAll("horse", ""))){
 							if(variant != Horse.Variant.HORSE && horse.getInventory().getArmor() != null){
 								player.sendMessage(ChatColor.RED + "Please remove horse armor before changing horse type.");
 								return true;
 							}
 							horse.setVariant(variant);
 							player.sendMessage(ChatColor.GREEN + "Horse type set to " + variant.toString());
 							return true;
 						}
 					}
 					player.sendMessage(ChatColor.YELLOW + "Invalid arguement.  For list of Types: /ChangeHorse Type");
 					return true;
 				}
 
 			}
 			if(option.equals("color") || option.equals("c")){
 				if(!player.hasPermission("changehorse.set.color")){
 					player.sendMessage(ChatColor.RED + "You don't have permission!");
					return true;
 				}
 
 				if(args.length == 2){
 					player.sendMessage(ChatColor.GREEN + "Color options are:");
 					for(Horse.Color color: Horse.Color.values()){
 						player.sendMessage(ChatColor.GREEN + "     " + color.toString().toLowerCase().replaceAll("_", ""));
 					}
 					return true;
 				}else{
 					if(horse.getVariant() == Horse.Variant.HORSE){
 						for(Horse.Color color: Horse.Color.values()){
 							if(color.toString().toLowerCase().replaceAll("_", "").equals(args[2].toLowerCase())){
 								horse.setColor(color);
 								player.sendMessage(ChatColor.GREEN + "Horse color set to " + color.toString());
 								return true;
 							}
 						}
 						player.sendMessage(ChatColor.YELLOW + "Invalid arguement.  For list of Colors: /ChangeHorse Color");
 						return true;
 					}else{
 						player.sendMessage(ChatColor.YELLOW + "Horse colors only apply to normal horses.  Change horse to type \"Horse\"");
 						return true;
 					}
 				}
 
 			}
 			if(option.equals("style") || option.equals("st")){
 				if(!player.hasPermission("changehorse.set.style")){
 					player.sendMessage(ChatColor.RED + "You don't have permission!");
					return true;
 				}
 				if(args.length == 2){
 					player.sendMessage(ChatColor.GREEN + "Style options are:");
 					for(Horse.Style style: Horse.Style.values()){
 						player.sendMessage(ChatColor.GREEN + "     " + style.toString().toLowerCase());
 					}
 					return true;
 				}else{
 					if(horse.getVariant() == Horse.Variant.HORSE){
 						for(Horse.Style style: Horse.Style.values()){
							if(style.toString().toLowerCase().replaceAll("_", "").equals(args[2].toLowerCase().replaceAll("_", ""))){
 								horse.setStyle(style);
 								player.sendMessage(ChatColor.GREEN + "Horse style set to " + style.toString());
 								return true;
 							}
 						}
 						player.sendMessage(ChatColor.YELLOW + "Invalid arguement.  For list of Styles: /ChangeHorse Style");
 						return true;
 					}else{
 						player.sendMessage(ChatColor.YELLOW + "Horse styles only apply to normal horses.  Change horse to type \"Horse\"");
 						return true;
 					}
 				}
 
 			}
 			if(option.equals("jumpstrength") || option.equals("js")){
 				if(!player.hasPermission("changehorse.set.jumpstrength")){
 					player.sendMessage(ChatColor.RED + "You don't have permission!");
					return true;
 				}
 				if(args.length == 2){
 					player.sendMessage(ChatColor.GREEN + "Valid range for Jump Strength is: 0-100");
 					return true;
 				}else{
 					int num = -1;
 					try{
 						num = Integer.parseInt(args[2]);
 					} catch(NumberFormatException e) {
 						player.sendMessage(ChatColor.YELLOW + "Invalid number given. " + ChatColor.GREEN + "Valid range for Jump Strength is: 0-100");
 						return true;
 					}
 
 					if(num > 0 && num <= 100){
 						double js = (double) num/50;
 						horse.setJumpStrength(js);
 						player.sendMessage(ChatColor.GREEN + "Horse jump strength set to " + num);
 						return true;
 					}else{
 						player.sendMessage(ChatColor.YELLOW + "Invalid number given, go get the correct range use /ChangeHorse set JumpStrength");
 						return true;
 					}
 				}
 			}
 			if(option.equals("maxhealth") || option.equals("mh")){
 				if(!player.hasPermission("changehorse.set.maxhealth")){
 					player.sendMessage(ChatColor.RED + "You don't have permission!");
					return true;
 				}
 				if(args.length == 2){
 					player.sendMessage(ChatColor.GREEN + "Valid range for Max Health is: 0-60");
 					return true;
 				}else{
 					int num = -1;
 					try{
 						num = Integer.parseInt(args[2]);
 					} catch(NumberFormatException e) {
 						player.sendMessage(ChatColor.YELLOW + "Invalid number given." + ChatColor.GREEN +" Valid range for Max Health is: 0-60");
 						return true;
 					}
 
 					if(num > 0 && num <= 100){
 						horse.setMaxHealth(num);
 						player.sendMessage(ChatColor.GREEN + "Horse jump strength set to " + num);
 						return true;
 					}else{
 						player.sendMessage(ChatColor.YELLOW + "Invalid number given, go get the correct range use /ChangeHorse set MaxHealth");
 						return true;						
 					}
 				}
 			}
 
 			player.sendMessage(ChatColor.YELLOW + "Invalid arguement. " + ChatColor.GREEN + setArgs);
 			return true;
 		}
 		player.sendMessage(ChatColor.YELLOW + "Invalid arguments. " + mainArgs);
 		return true;
 		//end new code
 	}
 }
