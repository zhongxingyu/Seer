 package nl.giantit.minecraft.GiantShop.API.GSW.Commands.Chat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import nl.giantit.minecraft.GiantShop.API.GiantShopAPI;
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.perms.Permission;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Giant
  */
 public class Help {
 	
 	private static ArrayList<String[]> entries = new ArrayList<String[]>();
 	private static Permission perms = GiantShop.getPlugin().getPermHandler().getEngine();
 	private static Messages mH = GiantShop.getPlugin().getMsgHandler();
 	
 	private static void init() {
 		entries = new ArrayList<String[]>();
 		entries.add(new String[] {"gsw", "Show GiantShopWeb API help page 1", "null"});
 		entries.add(new String[] {"gsw help|h|? (page)", "Show GiantShopWeb API page x", "null"});
 		entries.add(new String[] {"gsw sendhelp|sh [receiver] (page)", "Send Show GiantShopWeb API help page x to player y", "giantshop.admin.sendhelp"});
 		entries.add(new String[] {"gsw list|l (page)", "Show all trusted web apps", "giantshop.api.web.list"});
		entries.add(new String[] {"gsw register|reg [app]( [app]...)", "Register at given web apps.", "giantshop.api.web.register"});
		entries.add(new String[] {"gsw pickup|p", "Pickup all available transactions", "giantshop.api.web.pickup.all"});
		entries.add(new String[] {"gsw pickup|p [transactionID]", "Pickup transaction with given ID", "giantshop.api.web.pickup.pickup"});
		entries.add(new String[] {"gsw pickup|p list (page)", "Show page x of available transaction list", "giantshop.api.web.pickup.list"});
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
 		
 		String name = GiantShop.getPlugin().getName() + " Web API (v" + GiantShopAPI.Obtain().getGSWAPI().getAPIVersion() + ")";
 		int perPage = config.Obtain().getInt("GiantShop.global.perPage");
 		int curPag = 0;
 		
 		if(args.length >= 1) {
 			try{
 				curPag = Integer.parseInt(args[0]);
 			}catch(Exception e) {
 				curPag = 1;
 			}
 		}else
 			curPag = 1;
 		
 		curPag = (curPag > 0) ? curPag : 1;
 		int pages = ((int)Math.ceil((double)uEntries.size() / (double)perPage) < 1) ? 1 : (int)Math.ceil((double)uEntries.size() / (double)perPage);
 		int start = (curPag * perPage) - perPage;
 		
 		if(uEntries.size() <= 0) {
 			Heraut.say(player, "&e[&3" + name + "&e]" + mH.getMsg(Messages.msgType.ERROR, "noHelpEntries"));
 		}else if(curPag > pages) {
 			HashMap<String, String> d = new HashMap<String, String>();
 			d.put("list", "help");
 			d.put("pages", String.valueOf(pages));
 			Heraut.say(player, "&e[&3" + name + "&e]" + mH.getMsg(Messages.msgType.ERROR, "pageOverMax", d));
 		}else{
 			HashMap<String, String> d = new HashMap<String, String>();
 			d.put("page", String.valueOf(curPag));
			d.put("maxPages", String.valueOf(pages));
 			Heraut.say(player, "&e[&3" + name + "&e]" + mH.getMsg(Messages.msgType.MAIN, "helpPageHead", d));
 
 			for(int i = start; i < (((start + perPage) > uEntries.size()) ? uEntries.size() : (start + perPage)); i++) {
 				String[] data = uEntries.get(i);
 
 				String helpEntry = data[0];
 				String description = data[1];
 				Messages msg = GiantShop.getPlugin().getMsgHandler();
 				HashMap<String, String> params = new HashMap<String, String>();
 				params.put("command", helpEntry);
 				params.put("description", description);
 				
 				Heraut.say(player, msg.getMsg(Messages.msgType.MAIN, "helpCommand", params));
 			}
 		}
 	}
 }
