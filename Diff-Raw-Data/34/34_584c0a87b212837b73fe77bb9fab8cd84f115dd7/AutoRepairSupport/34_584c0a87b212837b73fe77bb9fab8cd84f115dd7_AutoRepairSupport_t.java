 package com.mrockey28.bukkit.ItemRepair;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;

 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.mrockey28.bukkit.ItemRepair.AutoRepairPlugin.operationType;
 
 /**
  * 
  * @author lostaris, mrockey28
  */
 public class AutoRepairSupport {
 	private final AutoRepairPlugin plugin;
 	protected static Player player;
 	public AutoRepairSupport(AutoRepairPlugin instance, Player player) {
 		plugin = instance;
 		AutoRepairSupport.player = player;
 	}
 
 	private boolean warning = false;
 	private boolean lastWarning = false;
 	
 	public void deduct(ArrayList<ItemStack> req) {
 		PlayerInventory inven = player.getInventory();
 		for (int i =0; i < req.size(); i++) {
 			ItemStack currItem = new ItemStack(req.get(i).getTypeId(), req.get(i).getAmount());
 			int neededAmount = req.get(i).getAmount();
 			int smallestSlot = findSmallest(currItem);
 			if (smallestSlot != -1) {
 				while (neededAmount > 0) {									
 					smallestSlot = findSmallest(currItem);
 					ItemStack smallestItem = inven.getItem(smallestSlot);
 					if (neededAmount < smallestItem.getAmount()) {
 						// got enough in smallest stack deal and done
 						ItemStack newSize = new ItemStack(currItem.getType(), smallestItem.getAmount() - neededAmount);
 						inven.setItem(smallestSlot, newSize);
 						neededAmount = 0;										
 					} else {
 						// need to remove from more than one stack, deal and continue
 						neededAmount -= smallestItem.getAmount();
 						inven.clear(smallestSlot);
 					}
 				}
 			}
 		}
 	}
 	
 	public void doQueryOperation(ItemStackPlus tool)
 	{
 		//Prevent query access if object is not repairable or access not allowed by permissions
 		if (!AutoRepairPlugin.isOpAllowed(getPlayer(), operationType.QUERY, tool.isEnchanted(), tool.getPermGroup()) ||
 			 !tool.isRepairable) {
 			return;
 		}
 		tool.setAdjustedCosts(AutoRepairPlugin.config);
 		
 		if (!AutoRepairPlugin.config.isAnyCost() || tool.freeRepairs()) {
 			getPlayer().sendMessage("3No materials needed to repair.");
 			return;
 		}
 		
 		String queryResponse = "6It costs";
 		if (AutoRepairPlugin.config.econCostUse && tool.getRepairCosts().getEconCost() != 0)	
 			queryResponse = queryResponse + " " + AutoRepairPlugin.econ.format(tool.getRepairCosts().getEconCost()) + ",";
 
 		if (AutoRepairPlugin.config.xpCostUse && tool.getRepairCosts().getXpCost() != 0)
 			queryResponse = queryResponse + " " +  tool.getRepairCosts().getXpCost() + " xp,";
 		if (AutoRepairPlugin.config.itemCostUse)
 			queryResponse = queryResponse +  printFormatReqs(tool.getRepairCosts().getItemCost());
 				
 		queryResponse = queryResponse.substring(0, queryResponse.length() -1) + " to repair " + tool.getName();
 		getPlayer().sendMessage(queryResponse);
 	}
 	
 	public void doWarnOperation(ItemStackPlus tool, boolean alreadyAdjustedCosts)
 	{
 		if (!AutoRepairPlugin.isOpAllowed(getPlayer(), operationType.WARN, tool.isEnchanted(), 0) ||
 				!tool.isRepairable) {
 			return;
 		}
 		
 		//If we've already warned once, don't warn again; otherwise, this is your first warning.
 		if (warning)
 			return;
 		else warning = true;
 		
 		if (!AutoRepairPlugin.config.automaticRepair_allow) {
 			if (!AutoRepairPlugin.config.automaticRepair_noWarnings) 
 				player.sendMessage("6WARNING: " + tool.getName() + " will break soon; auto repairing disabled.");
 			return;
 		}
 		
 		if (!alreadyAdjustedCosts)
 			tool.setAdjustedCosts(AutoRepairPlugin.config);
 		
 		//If costs are nil, then just return, no need to warn.
 		if (!AutoRepairPlugin.config.isAnyCost() && tool.freeRepairs()) {
 			return;
 		}
 		
 		
 		String warnResponse = "";
 		if (AutoRepairPlugin.config.econCostUse) {
 
 			double balance = AutoRepairPlugin.econ.getBalance(player.getName());
 			if (tool.getRepairCosts().getEconCost() > balance) {
 				warnResponse = warnResponse + " " + AutoRepairPlugin.econ.format(tool.getRepairCosts().getEconCost()) + ",";
 			}	
 		}
 		
 		if (AutoRepairPlugin.config.xpCostUse) {
 			
 			int xpBalance = player.getTotalExperience();
 			if (tool.getRepairCosts().getXpCost() > xpBalance) {
 				warnResponse = warnResponse + " " + tool.getRepairCosts().getXpCost() + " xp,";
 			}	
 		}
 		if (AutoRepairPlugin.config.itemCostUse) {
 			ArrayList<ItemStack> neededItems = new ArrayList<ItemStack>(0);
 			if (!isEnoughItems(tool.getRepairCosts().getItemCost(), neededItems)) {
 				warnResponse = warnResponse + printFormatReqs(neededItems);
 			}
 		}
 		if (warnResponse.length() != 0)
 		{
 			if (!alreadyAdjustedCosts)
 				player.sendMessage("6WARNING: " + tool.getName() + " will break soon.");
 			
 			warnResponse = warnResponse.substring(0, warnResponse.length() -1);
 			warnResponse = "cYou still need the following to be able to repair:" + warnResponse;
 			player.sendMessage(warnResponse);
 		}
 	}
 	
 	public void doLastWarnOperation(operationType op, ItemStackPlus tool, boolean alreadyAdjustedCost)
 	{
 		if (op != operationType.FULL_REPAIR){
 			if (op == operationType.MANUAL_REPAIR || op == operationType.SIGN_REPAIR || !getLastWarning()) {
 				setWarning(false);
 				doWarnOperation(tool, alreadyAdjustedCost);
 				if (op == operationType.AUTO_REPAIR) setLastWarning(true);	
 				return;
 			}
 		}
 	}
 	
 	public void doRepairOperation(ItemStackPlus tool, AutoRepairPlugin.operationType op)
 	{
 		//Check for necessary permissions
 		if (!AutoRepairPlugin.isOpAllowed(getPlayer(), op, tool.isEnchanted(), tool.getPermGroup())) {
 			return;
 		}
 		
 		//Prevent repairs on things that don't appear in the repair file (i.e. dyes)
 		if (!tool.isRepairable)
 		{
 			return;
 		}
 		
 		//Prevent repairs on fully repaired tools
 		if (tool.item.getDurability() == 0)
 		{
 			if (op == operationType.MANUAL_REPAIR || op == operationType.SIGN_REPAIR)
 				player.sendMessage("3" + tool.getName() + " is already fully repaired.");
 			return;
 		}
 
 		tool.setAdjustedCosts(AutoRepairPlugin.config);
 		
 		//No repair costs
 		if (!AutoRepairPlugin.config.isAnyCost() && tool.freeRepairs()) {
			repItem(tool);
			if (op != operationType.FULL_REPAIR && !(op == operationType.AUTO_REPAIR && AutoRepairPlugin.config.automaticRepair_noNotifications))
 				getPlayer().sendMessage("3Repaired " + tool.getName());
 			return;
 		}
 		
 		String repairResponse = "3Using";
 		if (AutoRepairPlugin.config.econCostUse && tool.getRepairCosts().getEconCost() != 0)
 		{
 			double balance = AutoRepairPlugin.econ.getBalance(player.getName());
 			if (tool.getRepairCosts().getEconCost() <= balance) 
 			{
 				repairResponse = repairResponse + " " + AutoRepairPlugin.econ.format(tool.getRepairCosts().getEconCost()) + ",";	
 			} 
 			else 
 			{
 				doLastWarnOperation(op, tool, true);
 				return;	
 			}
 		} 
 		
 		if (AutoRepairPlugin.config.xpCostUse && tool.getRepairCosts().getXpCost() != 0) 
 		{	
 			int xpBalance = player.getTotalExperience();
 			if (tool.getRepairCosts().getXpCost() <= xpBalance) 
 			{
 				repairResponse = repairResponse + " " + tool.getRepairCosts().getXpCost() + " xp,";	
 			} 
 			else 
 			{
 				doLastWarnOperation(op, tool, true);
 				return;	
 			}
 		} 
 
 		if (AutoRepairPlugin.config.itemCostUse && !tool.getRepairCosts().getItemCost().isEmpty()) 
 		{
 			ArrayList<ItemStack> neededItems = new ArrayList<ItemStack>(0);
 			if (isEnoughItems(tool.getRepairCosts().getItemCost(), neededItems)) 
 			{
 				repairResponse = repairResponse + printFormatReqs(tool.getRepairCosts().getItemCost());
 			}
 			else 
 			{
 				doLastWarnOperation(op, tool, true);
 				return;	
 			}
 		}
 
 		if (AutoRepairPlugin.config.econCostUse && tool.getRepairCosts().getEconCost() != 0) 
 			AutoRepairPlugin.econ.withdrawPlayer(player.getName(), tool.getRepairCosts().getEconCost());
 		if (AutoRepairPlugin.config.xpCostUse && tool.getRepairCosts().getXpCost() != 0)
 		{
 			int totalExp = player.getTotalExperience();
 			player.setTotalExperience(0);
 			player.setLevel(0);
 			player.setExp(0);
 			player.giveExp(totalExp - tool.getRepairCosts().getXpCost() );
 		}
 		if (AutoRepairPlugin.config.itemCostUse && !tool.getRepairCosts().getItemCost().isEmpty())
 			deduct(tool.getRepairCosts().getItemCost());
 		
 		repairResponse = repairResponse.substring(0, repairResponse.length() -1);
 		repairResponse = repairResponse + " to repair " + tool.getName();
 		if (op != operationType.FULL_REPAIR && !(op == operationType.AUTO_REPAIR && AutoRepairPlugin.config.automaticRepair_noNotifications)) {
 			player.sendMessage(repairResponse);
 		}
 		repItem(tool);		
 	}
 
 	public void repArmourInfo(String query) {
 
 		HashMap<String, Integer> itemTotal = new HashMap<String, Integer> (0);
 		double econTotal = 0;
 		int xpTotal = 0;
 		
 		for (ItemStack armor : player.getInventory().getArmorContents()) {
 			ItemStackPlus armorPlus = new ItemStackPlus(armor);
 			if (armorPlus.isRepairable)
 			{
 				armorPlus.setAdjustedCosts(AutoRepairPlugin.config);
 				econTotal += armorPlus.getRepairCosts().getEconCost();
 				xpTotal += armorPlus.getRepairCosts().getXpCost();
 				for (ItemStack item : armorPlus.getRepairCosts().getItemCost())
 				{
 					if (itemTotal.containsKey(item.getType().toString()))
 					{
 						int currentVal = itemTotal.get(item.getType().toString());
 						itemTotal.remove(item.getType().toString());
 						itemTotal.put(item.getType().toString(), currentVal + item.getAmount());
 					}
 					else
 					{
 						itemTotal.put(item.getType().toString(), item.getAmount());
 					}
 				}
 			}
 		}
 		ArrayList<ItemStack> repCost = new ArrayList<ItemStack>(0);
 		for (String i : itemTotal.keySet())
 		{
 			ItemStack newitem = new ItemStack(Material.getMaterial(i), itemTotal.get(i));
 			repCost.add(newitem.clone());
 		}
 		
 		if (!AutoRepairPlugin.config.isAnyCost() || (econTotal == 0 && xpTotal == 0 && repCost.isEmpty())) 
 				{
 			getPlayer().sendMessage("3No materials needed to repair.");
 			return;
 		}
 		
 		String queryResponse = "6It costs";
 		if (AutoRepairPlugin.config.econCostUse && econTotal != 0)	
 			queryResponse = queryResponse + " " + AutoRepairPlugin.econ.format(econTotal) + ",";
 	
 		if (AutoRepairPlugin.config.xpCostUse && xpTotal != 0)
 			queryResponse = queryResponse + " " +  xpTotal + " xp,";
 		
 		if (AutoRepairPlugin.config.itemCostUse)
 			queryResponse = queryResponse +  printFormatReqs(repCost);
 				
 		queryResponse = queryResponse.substring(0, queryResponse.length() -1) + " to repair all your armour.";
 		getPlayer().sendMessage(queryResponse);
 	}
 
 	public void repItem(ItemStackPlus item) {
 		item.repair();
 		if (AutoRepairPlugin.config.repairOfEnchantedItems_loseEnchantment)
 		{
 			item.deleteAllEnchantments();
 		}
 	}
 
 	//prints the durability left of the current tool to the player
 	public void durabilityLeft(ItemStackPlus tool) {
 		if (AutoRepairPlugin.isAllowed(player, "info")) {
 			int usesLeft = tool.getUsesLeft();
 			if (usesLeft != -1) {
 				player.sendMessage("3" + usesLeft + " uses left untill this tool breaks." );
 			} else {
 				player.sendMessage("6Can't get uses left for this item.");
 			}
 		} else {
 			player.sendMessage("cYou dont have permission to do the ? or dmg commands.");
 		}
 
 	}
 
 	@SuppressWarnings("rawtypes")
 	public int findSmallest(ItemStack item) {
 		PlayerInventory inven = player.getInventory();
 		HashMap<Integer, ? extends ItemStack> items = inven.all(item.getTypeId());
 		int slot = -1;
 		int smallest = 64;
 		//iterator for the hashmap
 		Set<?> set = items.entrySet();
 		Iterator<?> i = set.iterator();
 		//ItemStack highest = new ItemStack(repairItem.getType(), 0);
 		while(i.hasNext()){
 			Map.Entry me = (Map.Entry)i.next();
 			ItemStack item1 = (ItemStack) me.getValue();
 			//if the player has doesn't not have enough of the item used to repair
 			if (item1.getAmount() <= smallest) {
 				smallest = item1.getAmount();
 				slot = (Integer)me.getKey();
 			}
 		}		
 		return slot;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public int getTotalItems(ItemStack item) {
 		int total = 0;
 		PlayerInventory inven = player.getInventory();
 		HashMap<Integer, ? extends ItemStack> items = inven.all(item.getTypeId());
 		//iterator for the hashmap
 		Set<?> set = items.entrySet();
 		Iterator<?> i = set.iterator();
 		//ItemStack highest = new ItemStack(repairItem.getType(), 0);
 		while(i.hasNext()){
 			Map.Entry me = (Map.Entry)i.next();
 			ItemStack item1 = (ItemStack) me.getValue();
 			//if the player has doesn't not have enough of the item used to repair
 			total += item1.getAmount();					
 		}
 		return total;
 	}
 
 	public boolean isEnoughItems (ArrayList<ItemStack> req, ArrayList<ItemStack> neededItems) {
 		boolean enough = true;
 		for (int i =0; i<req.size(); i++) {
 			ItemStack currItem = new ItemStack(req.get(i).getTypeId(), req.get(i).getAmount());
 			int neededAmount = req.get(i).getAmount();
 			int currTotal = getTotalItems(currItem);
 			if (neededAmount > currTotal) {
 				neededItems.add(req.get(i).clone());
 				enough = false;
 			}
 		}
 		return enough;
 	}
 
 	public String printFormatReqs(ArrayList<ItemStack> items) {
 		StringBuffer string = new StringBuffer();
 		for (int i = 0; i < items.size(); i++) {
 			if (items.get(i).getAmount() != 0)
 					string.append(" " + items.get(i).getAmount() + " " + items.get(i).getType() + ",");
 		}
 		String returnString = string.toString();
 		return returnString;
 	}
 	
 	public void checkForAnvilRepair(PlayerInteractEvent event)
 	{
 		if (!AutoRepairPlugin.config.anvilUse_allow)
 		{
 			return;
 		}
 		
 		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
 		{
 			Block block = event.getClickedBlock();
 			if (block.getType() == AutoRepairPlugin.config.anvilUse_anvilBlockType)
 			{
 				World world = block.getWorld();
 				ArrayList<Block> maybeSign = new ArrayList<Block> (0);
 				maybeSign.add(world.getBlockAt(block.getX() + 1, block.getY(), block.getZ()));
 				maybeSign.add(world.getBlockAt(block.getX() - 1, block.getY(), block.getZ()));
 				maybeSign.add(world.getBlockAt(block.getX(), block.getY(), block.getZ() + 1));
 				maybeSign.add(world.getBlockAt(block.getX(), block.getY(), block.getZ() - 1));
 				maybeSign.add(world.getBlockAt(block.getX(), block.getY() + 1, block.getZ()));
 				maybeSign.add(world.getBlockAt(block.getX(), block.getY() - 1, block.getZ()));
 
 				for (Block mSign : maybeSign) 
 				{
 					if (mSign.getType() == Material.SIGN || mSign.getType() == Material.SIGN_POST)
 					{
 						if (((Sign)mSign.getState()).getLine(0).equalsIgnoreCase("Anvil"))
 						{
 							setPlayer(event.getPlayer());
 							plugin.repair.anvilRepair(ItemStackPlus.convert(event.getPlayer().getItemInHand()));
 							return;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public boolean getWarning() {
 		return warning;
 	}
 
 	public boolean getLastWarning() {
 		return lastWarning;
 	}
 
 	public void setWarning(boolean newValue) {
 		this.warning = newValue;
 	}
 
 	public void setLastWarning(boolean newValue) {
 		this.lastWarning = newValue;
 	}
 
 	public AutoRepairPlugin getPlugin() {
 		return plugin;
 	}
 
 	public static Player getPlayer() {
 		return player;
 	}
 	public void setPlayer(Player player) {
 		AutoRepairSupport.player = player;
 	}
 	
 	
 }
 
