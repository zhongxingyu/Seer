 package nl.giantit.minecraft.GiantBanks.Commands.Chat;
 
 import nl.giantit.minecraft.GiantBanks.GiantBanks;
 import nl.giantit.minecraft.GiantBanks.Bank.UserAccount;
 import nl.giantit.minecraft.GiantBanks.core.Items.ItemID;
 import nl.giantit.minecraft.GiantBanks.core.Items.Items;
 import nl.giantit.minecraft.GiantBanks.core.Misc.Heraut;
 import nl.giantit.minecraft.GiantBanks.core.Misc.Messages;
 import nl.giantit.minecraft.GiantBanks.core.Misc.Messages.msgType;
 import nl.giantit.minecraft.GiantBanks.core.Tools.InventoryHandler;
 import nl.giantit.minecraft.GiantBanks.core.perms.Permission;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import java.util.HashMap;
 
 public class GetAll {
 
 	private static Permission pH = GiantBanks.getPlugin().getPermHandler();
 	private static Items iH = GiantBanks.getPlugin().getItemHandler();
 	private static Messages mH = GiantBanks.getPlugin().getMsgHandler();
 
 	public static void exec(Player p, String[] args) {
 		if(args.length > 2) {
 			if(pH.has(p, "giantbanks.bank.getall")) {
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
 								id = Integer.parseInt(args[i]);
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
 								type = Integer.parseInt(args[i]);
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
 					int h = uA.has(iID);
 					if(h >= 0) { 
 						int status = uA.getAll(iID);
 						
 						if(status >= 1) {
 							ItemStack iStack;
 							Inventory inv = p.getInventory();
 	
 							if(iID.getType() != null && iID.getType()  != -1) {
 								if(iID.getId() != 373)
 									iStack = new MaterialData(iID.getId(), (byte) ((int) iID.getType() )).toItemStack(status);
 								else
 									iStack = new ItemStack(iID.getId(), status, (short) ((int) iID.getType() ));
 							}else{
 								iStack = new ItemStack(iID.getId(), status);
 							}
 							
 							InventoryHandler.storeItem(p, inv, iStack);
 							
 							HashMap<String, String> data = new HashMap<String, String>();
 							data.put("amount", String.valueOf(status));
 							data.put("item", item);
 							Heraut.say(p, mH.getMsg(msgType.MAIN, "itemObtained", data));
 							//Give user his [amount] of item [item]
 							return;
 						}else{
 							String m = "";
 							HashMap<String, String> data = new HashMap<String, String>();
 							data.put("amount", String.valueOf(status));
 							data.put("item", item);
 							
 							switch(status) {
 								case -2:
 									Heraut.say(p, mH.getMsg(msgType.ERROR, "ItemInvalid"));
 									break;
 								case -1:
 								case 0:
 									Heraut.say(p, mH.getMsg(msgType.ERROR, "noItemQuantity", data));
 									break;
 								default:
 									ItemStack iStack;
 									Inventory inv = p.getInventory();
 	
 									if(iID.getType() != null && iID.getType()  != -1) {
 										if(iID.getId() != 373)
 											iStack = new MaterialData(iID.getId(), (byte) ((int) iID.getType() )).toItemStack(status);
 										else
 											iStack = new ItemStack(iID.getId(), status, (short) ((int) iID.getType() ));
 									}else{
 										iStack = new ItemStack(iID.getId(), status);
 									}
 									
 									InventoryHandler.storeItem(p, inv, iStack);
 									Heraut.say(p, mH.getMsg(msgType.ERROR, "itemObtained", data));
 									//Give user his [status] of item [item]
 									break;
 							}
 							
 							return;
 						}
 					}else{
 						HashMap<String, String> data = new HashMap<String, String>();
 						data.put("amount", String.valueOf(h));
 						data.put("item", item);
 						if(h > 0) {
 							Heraut.say(p, mH.getMsg(msgType.ERROR, "notEnoughItems", data));
 						}else{
 							Heraut.say(p, mH.getMsg(msgType.ERROR, "noItemQuantity", data));
 						}
 						return;
 					}
 				}else{
 					Heraut.say(p, mH.getMsg(msgType.ERROR, "itemInvalid"));
 					return;
 				}
 			}else{
 				HashMap<String, String> d = new HashMap<String, String>();
 				d.put("command", "getall");
 				
				Heraut.say(p, mH.getMsg(msgType.ERROR, "syntaxError", d));
 			}
 		}
 	}
 
 }
