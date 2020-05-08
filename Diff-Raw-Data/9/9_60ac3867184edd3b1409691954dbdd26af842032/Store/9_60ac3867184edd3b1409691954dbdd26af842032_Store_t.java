 package nl.giantit.minecraft.GiantBanks.Commands.Chat;
 
 import nl.giantit.minecraft.GiantBanks.GiantBanks;
 import nl.giantit.minecraft.GiantBanks.Bank.UserAccount;
 import nl.giantit.minecraft.GiantBanks.core.Items.ItemID;
 import nl.giantit.minecraft.GiantBanks.core.Items.Items;
 import nl.giantit.minecraft.GiantBanks.core.Misc.Heraut;
 import nl.giantit.minecraft.GiantBanks.core.Misc.Messages;
 import nl.giantit.minecraft.GiantBanks.core.Misc.Messages.msgType;
 
 import org.bukkit.entity.Player;
 
 import java.util.HashMap;
 
 public class Store {
 	
 	private static Items iH = GiantBanks.getPlugin().getItemHandler();
 	private static Messages mH = GiantBanks.getPlugin().getMsgHandler();
 
 	public static void exec(Player p, String[] args) {
 		if(args.length > 2) {
 			String item = null;
 			int id = 0;
 			Integer type = null;
 			int amount = 0;
 			
 			for(int i = 1; i < args.length; i++) {
 				if(args[i].equalsIgnoreCase("-i")) {
 					i++;
 					if(args.length - 1 >= i) {
 						item = args[i];
 						continue;
 					}else{
 						break;
 					}
 				}else if(args[i].startsWith("-i:")) {
 					item = args[i].replaceFirst("-i:", "");
 					continue;
 				}else if(args[i].equalsIgnoreCase("-id")) {
 					i++;
 					if(args.length - 1 >= i) {
 						try{
 							id = Integer.parseInt(args[i].replaceFirst("-a:", ""));
 						}catch(NumberFormatException e) {
 							//ignore
 						}
 						continue;
 					}else{
 						break;
 					}
 				}else if(args[i].startsWith("-id:")) {
 					try{
 						id = Integer.parseInt(args[i].replaceFirst("-id:", ""));
 					}catch(NumberFormatException e) {
 						//ignore
 					}
 					continue;
 				}else if(args[i].equalsIgnoreCase("-t")) {
 					i++;
 					if(args.length - 1 >= i) {
 						try{
 							type = Integer.parseInt(args[i].replaceFirst("-t:", ""));
 						}catch(NumberFormatException e) {
 							//ignore
 						}
 						continue;
 					}else{
 						break;
 					}
 				}else if(args[i].startsWith("-t:")) {
 					try{
 						type = Integer.parseInt(args[i].replaceFirst("-t:", ""));
 					}catch(NumberFormatException e) {
 						//ignore
 					}
 					continue;
 				}else if(args[i].equalsIgnoreCase("-a")) {
 					i++;
 					if(args.length - 1 >= i) {
 						try{
 							amount = Integer.parseInt(args[i].replaceFirst("-a:", ""));
 						}catch(NumberFormatException e) {
 							//ignore
 						}
 						continue;
 					}else{
 						break;
 					}
 				}else if(args[i].startsWith("-a:")) {
 					try{
 						amount = Integer.parseInt(args[i].replaceFirst("-a:", ""));
 					}catch(NumberFormatException e) {
 						//ignore
 					}
 					continue;
 				}
 			}
 			
 			if(item == null) {
 				if(id > 0) {
 					if(null != type && 0 == type)
 						type = null;
 					if(iH.isValidItem(id, type)) {
 						item = iH.getItemNameByID(id, type);
 					}else{
 						Heraut.say(p, mH.getMsg(msgType.ERROR, "itemInvalid"));
 						return;
 					}
 				}else{
 					Heraut.say(p, mH.getMsg(msgType.ERROR, "itemIDInvalid"));
 					return;
 				}
 			}
 			
 			
 			UserAccount uA = UserAccount.getUserAccount(p.getName());
 			if(null == uA)
 				uA = UserAccount.createUserAccount(p.getName());
 			
 			ItemID iID = iH.getItemIDByName(item);
 			
 			if(iID != null) {
 				if(amount > 0) {
 					int status = uA.add(iID, amount);
 					if(0 == status) {
 						HashMap<String, String> data = new HashMap<String, String>();
 						data.put("amount", String.valueOf(amount));
 						data.put("item", item);
 						Heraut.say(p, mH.getMsg(msgType.MAIN, "itemStored", data));
 						return;
 					}else{
 						HashMap<String, String> data = new HashMap<String, String>();
						data.put("amount", String.valueOf(amount - status));
 						data.put("item", item);
 						
 						switch(status) {
 							case -2:
 								Heraut.say(p, mH.getMsg(msgType.ERROR, "ItemInvalid"));
 								break;
 							case -1:
 								Heraut.say(p, mH.getMsg(msgType.ERROR, "noAvailableSlots", data));
 								break;
 							default:
 								Heraut.say(p, mH.getMsg(msgType.ERROR, "notEnoughSpace", data));
 								Heraut.say(p, mH.getMsg(msgType.MAIN, "itemStored", data));
 								//Give user his [status] of item [item]
 								break;
 						}
 						
 					}
 				}else{
 					Heraut.say(p, mH.getMsg(msgType.ERROR, "zeroAmount"));
 					return;
 				}
 			}else{
 				Heraut.say(p, mH.getMsg(msgType.ERROR, "itemInvalid"));
 				return;
 			}
 		}
 	}
 }
