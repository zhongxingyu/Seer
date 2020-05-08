 package nl.giantit.minecraft.GiantShop.core.Commands;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.perm;
 import nl.giantit.minecraft.GiantShop.core.Database.db;
 import nl.giantit.minecraft.GiantShop.core.Items.Items;
 import nl.giantit.minecraft.GiantShop.core.Items.ItemID;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 /**
  *
  * @author Giant
  */
 public class check {
 	
 	public static void check(Player player, String[] args) {
 		Messages msgs = GiantShop.getPlugin().getMsgHandler();
 		Items iH = GiantShop.getPlugin().getItemHandler();
 		perm perms = perm.Obtain();
 		config conf = config.Obtain();
 		if(perms.has(player, "giantshop.shop.check")) {
 			db DB = db.Obtain();
 			int itemID;
 			Integer itemType = -1;
 			
 			if(!args[1].matches("[0-9]+:[0-9]+")) {
 				try {
 					itemID = Integer.parseInt(args[1]);
 					itemType = -1;
 				}catch(NumberFormatException e) {
 					ItemID key = iH.getItemIDByName(args[1]);
 					if(key != null) {
 						itemID = key.getId();
 						itemType = key.getType();
 					}else{
 						Heraut.say(player, msgs.getMsg(Messages.msgType.ERROR, "itemNotFound"));
 						return;
 					}
 				}catch(Exception e) {
 					if(conf.getBoolean("GiantShop.global.debug") == true) {
 						GiantShop.log.log(Level.SEVERE, "GiantShop Error: " + e.getMessage());
 						GiantShop.log.log(Level.INFO, "Stacktrace: " + e.getStackTrace());
 					}
 
 					Heraut.say(player, msgs.getMsg(Messages.msgType.ERROR, "unknown"));
 					return;
 				}
 			}else{
 				try {
 					String[] data = args[1].split(":");
 					itemID = Integer.parseInt(data[0]);
 					itemType = Integer.parseInt(data[1]);
 				}catch(NumberFormatException e) {
 					HashMap<String, String> data = new HashMap<String, String>();
 					data.put("command", "check");
 
 					Heraut.say(player, msgs.getMsg(Messages.msgType.ERROR, "syntaxError", data));
 					return;
 				}catch(Exception e) {
 					if(conf.getBoolean("GiantShop.global.debug") == true) {
 						GiantShop.log.log(Level.SEVERE, "GiantShop Error: " + e.getMessage());
 						GiantShop.log.log(Level.INFO, "Stacktrace: " + e.getStackTrace());
 					}
 
 					Heraut.say(player, msgs.getMsg(Messages.msgType.ERROR, "unknown"));
 					return;
 				}
 			}
 			itemType = (itemType == null || itemType == 0) ? -1 : itemType;
 			
 			ArrayList<String> fields = new ArrayList<String>();
 			fields.add("perStack");
 			fields.add("sellFor");
 			fields.add("buyFor");
 			fields.add("stock");
			fields.add("maxStock");
 			fields.add("shops");
 			
 			HashMap<String, String> where = new HashMap<String, String>();
 			where.put("itemID", String.valueOf(itemID));
 			where.put("type", String.valueOf(itemType));
 			
 			ArrayList<HashMap<String, String>> resSet = DB.select(fields).from("#__items").where(where).execQuery();
 			if(resSet.size() == 1) {
 				//Wait didn't we just do this the other way round?!
 				//Yea we did! Why? Because we can!
 				itemType = (itemType == -1) ? 0 : itemType;
 				
 				String name = iH.getItemNameByID(itemID, itemType);
 				HashMap<String, String> res = resSet.get(0);
 				
 				int stock = Integer.parseInt(res.get("stock"));
				int maxStock = Integer.parseInt(res.get("maxStock"));
 				double sellFor = Double.parseDouble(res.get("sellFor"));
 				double buyFor = Double.parseDouble(res.get("buyFor"));
 				
 				if(conf.getBoolean("GiantShop.stock.useStock") && conf.getBoolean("GiantShop.stock.stockDefinesCost") && maxStock != -1 && stock != -1) {
 					double maxInfl = conf.getDouble("GiantShop.stock.maxInflation");
 					double maxDefl = conf.getDouble("GiantShop.stock.maxDeflation");
 					int atmi = conf.getInt("GiantShop.stock.amountTillMaxInflation");
 					int atmd = conf.getInt("GiantShop.stock.amountTillMaxDeflation");
 					double split = Math.round((atmi + atmd) / 2);
 					if(maxStock <= atmi + atmd); {
 						split = maxStock / 2;
 						atmi = 0;
 						atmd = maxStock;
 					}
 					
 					if(stock >= atmd) {
 						if(buyFor != -1)
 							buyFor = buyFor * (1.0 - maxDefl / 100.0);
 						
 						if(sellFor != -1)
 							sellFor = sellFor * (1.0 - maxDefl / 100.0); 
 					}else if(stock <= atmi) {
 						if(buyFor != -1)
 							buyFor = buyFor * (1.0 + maxDefl / 100.0);
 						
 						if(sellFor != -1)
 							sellFor = sellFor * (1.0 + maxDefl / 100.0);
 					}else{
 						if(stock < split) {
 							if(buyFor != -1)
 								buyFor = (double)Math.round((buyFor * (1.0 + (maxInfl / stock) / 100)) * 100.0) / 100.0;
 
 							if(sellFor != -1)
 								sellFor = (double)Math.round((sellFor * (1.0 + (maxInfl / stock) / 100)) * 100.0) / 100.0;
 						}else if(stock > split) {
 							if(buyFor != -1)
 								buyFor = 2.0 + (double)Math.round((buyFor / (maxDefl * stock / 100)) * 100.0) / 100.0;
 
 							if(sellFor != -1)
 								sellFor = 2.0 + (double)Math.round((sellFor / (maxDefl * stock / 100)) * 100.0) / 100.0;
 							
 						}
 					}
 				}
 				
 				String sf = String.valueOf(sellFor);
 				String bf = String.valueOf(buyFor);
 				
 				Heraut.say(player, "Here's the result for " + name + "!");
 				Heraut.say(player, "ID: " + itemID);
 				Heraut.say(player, "Type: " + itemType);
 				Heraut.say(player, "Quantity per amount: " + res.get("perStack"));
 				Heraut.say(player, "Leaves shop for: " + (!sf.equals("-1.0") ? sf : "Not for sale!"));
 				Heraut.say(player, "Returns to shop for: " + (!bf.equals("-1.0") ? bf : "No returns!"));
 				Heraut.say(player, "Amount of items in the shop: " + (!res.get("stock").equals("-1") ? res.get("stock") : "unlimited"));
				Heraut.say(player, "Maximum amount of items in the shop: " + (!res.get("maxStock").equals("-1") ? res.get("maxStock") : "unlimited"));
 				//More future stuff
 				/*if(conf.getBoolean("GiantShop.Location.useGiantShopLocation") == true) {
 				 *		ArrayList<Indaface> shops = GiantShop.getPlugin().getLocationHandler().parseShops(res.get("shops"));
 				 *		for(Indaface shop : shops) {
 				 *			if(shop.inShop(player.getLocation())) {
 				 *				Heraut.say(player, "Something about what shops these items are in or something like that!");
 				 *				break;
 				 *			}
 				 *		}
 				 * } 
 				 */
 			}else{
 				Heraut.say(player, msgs.getMsg(Messages.msgType.ERROR, "noneOrMoreResults"));
 			}
 		}else{
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("command", "check");
 
 			Heraut.say(player, msgs.getMsg(Messages.msgType.ERROR, "noPermissions", data));
 		}
 	}
 	
 	public static void check(CommandSender sender, String[] args) {
 		Messages msgs = GiantShop.getPlugin().getMsgHandler();
 		Items iH = GiantShop.getPlugin().getItemHandler();
 		config conf = config.Obtain();
 		
 		db DB = db.Obtain();
 		int itemID;
 		Integer itemType = -1;
 
 		if(!args[1].matches("[0-9]+:[0-9]+")) {
 			try {
 				itemID = Integer.parseInt(args[1]);
 				itemType = -1;
 			}catch(NumberFormatException e) {
 				ItemID key = iH.getItemIDByName(args[1]);
 				if(key != null) {
 					itemID = key.getId();
 					itemType = key.getType();
 				}else{
 					Heraut.say(sender, msgs.getConsoleMsg(Messages.msgType.ERROR, "itemNotFound"));
 					return;
 				}
 			}catch(Exception e) {
 				if(conf.getBoolean("GiantShop.global.debug") == true) {
 					GiantShop.log.log(Level.SEVERE, "GiantShop Error: " + e.getMessage());
 					GiantShop.log.log(Level.INFO, "Stacktrace: " + e.getStackTrace());
 				}
 
 				Heraut.say(sender, msgs.getConsoleMsg(Messages.msgType.ERROR, "unknown"));
 				return;
 			}
 		}else{
 			try {
 				String[] data = args[1].split(":");
 				itemID = Integer.parseInt(data[0]);
 				itemType = Integer.parseInt(data[1]);
 			}catch(NumberFormatException e) {
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("command", "check");
 
 				Heraut.say(sender, msgs.getConsoleMsg(Messages.msgType.ERROR, "syntaxError", data));
 				return;
 			}catch(Exception e) {
 				if(conf.getBoolean("GiantShop.global.debug") == true) {
 					GiantShop.log.log(Level.SEVERE, "GiantShop Error: " + e.getMessage());
 					GiantShop.log.log(Level.INFO, "Stacktrace: " + e.getStackTrace());
 				}
 
 				Heraut.say(sender, msgs.getConsoleMsg(Messages.msgType.ERROR, "unknown"));
 				return;
 			}
 		}
 		itemType = (itemType == null || itemType == 0) ? -1 : itemType;
 
 		ArrayList<String> fields = new ArrayList<String>();
 		fields.add("perStack");
 		fields.add("sellFor");
 		fields.add("buyFor");
 		fields.add("stock");
 		fields.add("shops");
 
 		HashMap<String, String> where = new HashMap<String, String>();
 		where.put("itemID", String.valueOf(itemID));
 		where.put("type", String.valueOf(itemType));
 
 		ArrayList<HashMap<String, String>> resSet = DB.select(fields).from("#__items").where(where).execQuery();
 		if(resSet.size() == 1) {
 			//Wait didn't we just do this the other way round?!
 			//Yea we did! Why? Because we can!
 			itemType = (itemType == -1) ? 0 : itemType;
 
 			String name = iH.getItemNameByID(itemID, itemType);
 			HashMap<String, String> res = resSet.get(0);
 			Heraut.say(sender, "Here's the result for " + name + "!");
 			Heraut.say(sender, "ID: " + itemID);
 			Heraut.say(sender, "Type: " + itemType);
 			Heraut.say(sender, "Quantity per amount: " + res.get("perStack"));
 			Heraut.say(sender, "Leaves shop for: " + (!res.get("sellFor").equals("-1.0") ? res.get("sellFor") : "Doesn't leave the shop!"));
 			Heraut.say(sender, "Returns to shop for: " + (!res.get("buyFor").equals("-1.0") ? res.get("buyFor") : "No returns!"));
 			Heraut.say(sender, "Amount of items in the shop: " + (!res.get("stock").equals("-1") ? res.get("stock") : "unlimited"));
 			//More future stuff
 			/*if(conf.getBoolean("GiantShop.Location.useGiantShopLocation") == true) {
 			 *		ArrayList<Indaface> shops = GiantShop.getPlugin().getLocationHandler().parseShops(res.get("shops"));
 			 *		for(Indaface shop : shops) {
 			 *			if(shop.inShop(player.getLocation())) {
 			 *				Heraut.say(player, "Something about what shops these items are in or something like that!");
 			 *				break;
 			 *			}
 			 *		}
 			 * } 
 			 */
 		}else{
 			Heraut.say(sender, msgs.getConsoleMsg(Messages.msgType.ERROR, "noneOrMoreResults"));
 		}
 	}
 }
