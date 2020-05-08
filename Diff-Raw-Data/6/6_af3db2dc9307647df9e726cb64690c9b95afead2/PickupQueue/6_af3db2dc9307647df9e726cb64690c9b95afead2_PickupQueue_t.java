 package nl.giantit.minecraft.GiantShop.API.GSW;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import nl.giantit.minecraft.GiantShop.API.conf;
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 import nl.giantit.minecraft.GiantShop.core.Database.drivers.iDriver;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 /**
  *
  * @author Giant
  */
 public class PickupQueue {
 	
 	private GiantShop p;
 	private Messages mH;
 	
 	private HashMap<String, ArrayList<Queued>> queue = new HashMap<String, ArrayList<Queued>>();
 	
 	public PickupQueue(GiantShop p) {
 		this.p = p;
 		this.mH = p.getMsgHandler();
 	}
 	
 	public void addToQueue(String transactionID, String player, int amount, int id, int type) {
 		// Add purchase to database and take money
 		iDriver db = p.getDB().getEngine();
 		ArrayList<String> fields = new ArrayList<String>() {
 			{
 				add("transactionID");
 				add("player");
 				add("amount");
 				add("itemID");
 				add("itemType");
 			}
 		};
 		
 		HashMap<Integer, HashMap<String, String>> values = new HashMap<Integer, HashMap<String, String>>();
 		int i = 0;
 		
 		for(String field : fields) {
 			HashMap<String, String> temp = new HashMap<String, String>();
 			if(field.equals("transactionID")) {
				temp.put("data", transactionID);
 				values.put(i, temp);
 			}else if(field.equals("player")) {
 				temp.put("data", player);
 				values.put(i, temp);
 			}else if(field.equals("amount")) {
				temp.put("kind", "INT");
 				temp.put("data", "" + amount);
 				values.put(i, temp);
 			}else if(field.equals("itemID")) {
				temp.put("kind", "INT");
 				temp.put("data", "" + id);
 				values.put(i, temp);
 			}else if(field.equals("itemType")) {
 				temp.put("kind", "INT");
 				temp.put("data", "" + type);
 				values.put(i, temp);
 			}
 			
 			i++;
 		}
 		
 		// Insert transaction into database as ready pickup!
 		db.insert("#__api_gsw_pickups", fields, values).updateQuery();
 		
 		ArrayList<Queued> q;
 		if(this.queue.containsKey(player)) {
 			q = this.queue.remove(player);
 		}else{
 			q = new ArrayList<Queued>();
 		}
 		
 		Queued Q = new Queued(id, type, amount, transactionID);
 		q.add(Q);
 		this.queue.put(player, q);
 		
 		this.stalkUser(player);
 	}
 	
 	public boolean inQueue(String player) {
 		
 		return false;
 	}
 	
 	public void stalkUser(String player) {
 		Player pl = p.getSrvr().getPlayerExact(player);
 		if(null != pl) {
 			// Player is online! Bug him about his new purchase now!
 			conf c = GSWAPI.getInstance().getConfig();
 			if(c.getBoolean("GiantShopWeb.Items.PickupsRequireCommand")) {
 				Heraut.say(pl, mH.getMsg(Messages.msgType.MAIN, "itemWaitingForPickup"));
 				Heraut.say(pl, mH.getMsg(Messages.msgType.MAIN, "itemPickupHelp"));
 			}else{
 				this.deliver(pl);
 			}
 		}
 	}
 	
 	public void deliver(Player player) {
 		Inventory inv = player.getInventory();
 		ArrayList<Queued> qList = this.queue.get(player.getName());
 		for(Queued q : qList) {
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("transactionID", q.getTransactionID());
 			data.put("itemID", String.valueOf(q.getItemID()));
 			data.put("itemType", String.valueOf(q.getItemType()));
 			data.put("itemName", q.getItemName());
 			data.put("amount", String.valueOf(q.getAmount()));
 			
 			Heraut.say(player, mH.getMsg(Messages.msgType.MAIN, "itemDelivery", data));
 			
 			ItemStack iStack;
 			if(q.getItemType() != -1 && q.getItemType() != 0) {
 				if(q.getItemID() != 373)
 					iStack = new MaterialData(q.getItemID(), (byte) ((int) q.getItemType())).toItemStack(q.getAmount());
 				else
 					iStack = new ItemStack(q.getItemID(), q.getAmount(), (short) ((int) q.getItemType()));
 			}else{
 				iStack = new ItemStack(q.getItemID(), q.getAmount());
 			}
 			
 			HashMap<Integer, ItemStack> left = inv.addItem(iStack);
 			if(!left.isEmpty()) {
 				Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "infFull"));
 				for(Map.Entry<Integer, ItemStack> stack : left.entrySet()) {
 					player.getWorld().dropItem(player.getLocation(), stack.getValue());
 				}
 			}
 		}
 		
 		Heraut.say(player, mH.getMsg(Messages.msgType.MAIN, "itemsDelivered"));
 	}
 }
