 package nl.giantit.minecraft.GiantShop.core.Commands.chat;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.perms.Permission;
 
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  *
  * @author Giant
  */
 public class help {
 	
 	private static ArrayList<String[]> entries = new ArrayList<String[]>();
 	private static config conf = config.Obtain();
 	private static Permission perms = GiantShop.getPlugin().getPermHandler().getEngine();
 	
 	private static void init() {
 		entries = new ArrayList<String[]>();
 		entries.add(new String[] {"shop", "Show GiantShop help page 1", "null"});
 		entries.add(new String[] {"shop help|h|? (page)", "Show GiantShop help page x", "null"});
 		entries.add(new String[] {"shop sendhelp|sh [receiver] (page)", "Send GiantShop help page x to player y", "giantshop.admin.sendhelp"});
 		entries.add(new String[] {"shop list|l (page)", "Show all items in the shop", "giantshop.shop.list"});
 		entries.add(new String[] {"shop check|c [item](:[type])", "Show all available item info for item x", "giantshop.shop.check"});
 	//	entries.add(new String[] {"shop search (part)", "Show all items matching (part)", "giantshop.shop.search"}); //Future plans!
 		entries.add(new String[] {"shop buy|b [item](:[type]) (amount)", "Buy (amount) of item (item)", "giantshop.shop.buy"});
 		entries.add(new String[] {"shop gift|g [player[ [item](:[type]) (amount)", "Gift (amount) of item (item) to player (player)", "giantshop.shop.gift"});
 		entries.add(new String[] {"shop sell|s [item](:[type]) (amount)", "Sell (amount) of item (item)", "giantshop.shop.sell"});
 		entries.add(new String[] {"shop discount|d (page)", "Show your available discounts", "giantshop.shop.discount.list"});
 		entries.add(new String[] {"shop add|a [item](:[type]) [amount] [sellFor] (buyFor) (stock)", "Add an item to the shop", "giantshop.admin.add"});
 		entries.add(new String[] {"shop update|u select [item](:[type])", "Select an item for updating", "giantshop.admin.update"});
 		entries.add(new String[] {"shop update|u show", "Show current details for the selected item", "giantshop.admin.update"});
 		entries.add(new String[] {"shop update|u set sellFor [new value]", "Update the amount of money needed for buying", "giantshop.admin.update"});
 		entries.add(new String[] {"shop update|u set buyFor [new value]", "Update the amount of money a player receives on selling", "giantshop.admin.update"});
 		entries.add(new String[] {"shop update|u set stock [new value]", "Update the quantity of items in the shop", "giantshop.admin.update"});
 		entries.add(new String[] {"shop update|u set perStack [new value]", "Update the quantity of items per amount", "giantshop.admin.update"});
 		entries.add(new String[] {"shop update|u save", "Saves the changes that you made to the item", "giantshop.admin.update"});
 		entries.add(new String[] {"shop remove|r [item](:[type])", "Remove an item from the shop", "giantshop.admin.remove"});
 		entries.add(new String[] {"shop discount|d list|l all|a (page)", "Show all discounts page x", "giantshop.admin.discount.list"});
 		entries.add(new String[] {"shop discount|d list|l all|a (-p:[page]) (-u:[user]) (-g:[group])", "Show all discounts page x for user u or group g", "giantshop.admin.discount.list"});
 		entries.add(new String[] {"shop discount|d add|a (-i:[itemID]) (-t:[type]) (-u:[user]) (-g:[group]) -d:[discount]", "Add a discount to the shop", "giantshop.admin.discount.add"});
 		entries.add(new String[] {"shop discount|d update|u -id:[discountID] -d:[discount]", "Update discount x to y", "giantshop.admin.discount.update"});
 		entries.add(new String[] {"shop discount|d remove|r -id:[discountID]", "Remove discount from the shop", "giantshop.admin.discount.remove"});
 		entries.add(new String[] {"shop reload|rel", "Reload the config file.", "giantshop.admin.reload"});
 	}
 	
 	public static void showHelp(Player player, String[] args) {
 		if(entries.isEmpty())
 			init();
 		
 		ArrayList<String[]> uEntries = new ArrayList<String[]>();
 		for(int i = 0; i < entries.size(); i++) {
 			String[] data = entries.get(i);
 
 			String permission = data[2];
 
 			if(permission.equalsIgnoreCase("null") || perms.has((Player)player, (String)permission)) {
 				uEntries.add(data);				
 			}else{
 				continue;
 			}
 		}
 		
 		String name = GiantShop.getPlugin().getPubName();
 		int perPage = conf.getInt("GiantShop.global.perPage");
 		int curPag = 0;
 		
 		if(args.length >= 2) {
 			try{
 				curPag = Integer.parseInt(args[1]);
 			}catch(Exception e) {
 				curPag = 1;
 			}
 		}else
 			curPag = 1;
 		
 		curPag = (curPag > 0) ? curPag : 1;
 		
 		int pages = ((int)Math.ceil((double)uEntries.size() / (double)perPage) < 1) ? 1 : (int)Math.ceil((double)uEntries.size() / (double)perPage);
 		int start = (curPag * perPage) - perPage;
 		
		Heraut.savePlayer(player);
		
 		if(uEntries.size() <= 0) {
			Heraut.say("&e[&3" + name + "&e]&c Sorry no help entries yet :(");
 		}else if(curPag > pages) {
			Heraut.say("&e[&3" + name + "&e]&c My help list only has &e" + pages + " &cpages!!");
 		}else{
			Heraut.say("&e[&3" + name + "&e]&f Help. Page: &e" + curPag + "&f/&e" + pages);
 
 			for(int i = start; i < (((start + perPage) > uEntries.size()) ? uEntries.size() : (start + perPage)); i++) {
 				String[] data = uEntries.get(i);
 
 				String helpEntry = data[0];
 				String description = data[1];
 				Messages msg = GiantShop.getPlugin().getMsgHandler();
 				HashMap<String, String> params = new HashMap<String, String>();
 				params.put("command", helpEntry);
 				params.put("description", description);
 				
				Heraut.say(msg.getMsg(Messages.msgType.MAIN, "helpCommand", params));
 			}
 		}
 	}
 	
 	public static void sendHelp(Player player, String[] args) {
 		if(entries.isEmpty())
 			init();
 		
 		String name = conf.getString("GiantShop.global.name");
 		int perPage = conf.getInt("GiantShop.global.perPage");
 		int curPag = 0;
 		
 		String usr;
 		
 		if(args.length >= 2) {
 			usr = args[1];
 			if(args.length >= 3) {
 				try{
 					curPag = Integer.parseInt(args[2]);
 				}catch(Exception e) {
 					curPag = 1;
 				}
 			}else
 				curPag = 1;
 		}else{
 			curPag = 1;
 			usr = null;
 		}
 		
 		if(usr != null) {
 			Player receiver = GiantShop.getPlugin().getServer().getPlayer(usr);
 			if(receiver != null && receiver.isOnline()) {
 				
 				ArrayList<String[]> uEntries = new ArrayList<String[]>();
 				for(int i = 0; i < entries.size(); i++) {
 					String[] data = entries.get(i);
 
 					String permission = data[2];
 
 					if(permission.equalsIgnoreCase("null") || perms.has(receiver, (String)permission)) {
 						uEntries.add(data);				
 					}else{
 						continue;
 					}
 				}
 				curPag = (curPag > 0) ? curPag : 1;
 				
 				int pages = ((int)Math.ceil((double)uEntries.size() / (double)perPage) < 1) ? 1 : (int)Math.ceil((double)uEntries.size() / (double)perPage);
 				int start = (curPag * perPage) - perPage;
 
 				if(uEntries.size() <= 0) {
 					Heraut.say(player, "&e[&3" + name + "&e]&c Sorry no help entries yet :(");
 					return;
 				}else if(curPag > pages) {
 					Heraut.say(player, "&e[&3" + name + "&e]&c My help list for player " + usr + " only has &e" + pages + " &cpages!!");
 					return;
 				}else{
 					if(!perms.has(player, "giantshop.admin.sendhelp")) {
 						Heraut.say(player, "&cYou have no access to that command!");
 						return;
 					}
 					Heraut.say(player, "&e[&3" + name + "&e]&f Sending help page &e" + curPag + "&f to player &e" + usr);
 					Heraut.say(receiver, "&e[&3" + name + "&e]&f You were sent help by " + player.getDisplayName() + "!");
 					Heraut.say(receiver, "&e[&3" + name + "&e]&f Help. Page: &e" + curPag + "&f/&e" + pages);
 
 					for(int i = start; i < (((start + perPage) > uEntries.size()) ? uEntries.size() : (start + perPage)); i++) {
 						String[] data = uEntries.get(i);
 
 						String helpEntry = data[0];
 						String description = data[1];
 
 						Heraut.say(receiver, "&c/" + helpEntry + " &e-&f " + description);
 					}
 				}
 			}else{
 				Heraut.say(player, "&e[&3" + name + "&e]&c The requested player does not to be offline or even not existing! :(");
 			}
 		}else{
 			help.showHelp(player, args);
 		}
 	}
 }
