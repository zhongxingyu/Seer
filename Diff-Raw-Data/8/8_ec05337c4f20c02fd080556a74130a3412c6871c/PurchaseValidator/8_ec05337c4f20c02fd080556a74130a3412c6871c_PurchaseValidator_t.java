 package no.runsafe.ItemControl.trading;
 
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.Sound;
 import no.runsafe.framework.minecraft.enchantment.RunsafeEnchantment;
 import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
 
 @SuppressWarnings("unchecked")
 public class PurchaseValidator
 {
 	public void addPurchaseItem(RunsafeMeta item)
 	{
 		purchaseItems.add(item);
 	}
 
 	public void addRequiredItem(RunsafeMeta item)
 	{
 		for (Map.Entry<RunsafeMeta, Integer> itemNode : requiredItems.entrySet())
 		{
 			RunsafeMeta requiredItem = itemNode.getKey();
 			if (strictItemMatch(requiredItem, item))
 			{
 				requiredItems.put(requiredItem, itemNode.getValue() + item.getAmount());
 				return;
 			}
 		}
 		requiredItems.put(item, item.getAmount());
 	}
 
 	private boolean playerCanPurchase(IPlayer player)
 	{
		ConcurrentHashMap<RunsafeMeta, Integer> checklist = new ConcurrentHashMap<RunsafeMeta, Integer>(requiredItems.size());
		checklist.putAll(requiredItems);
 
 		for (RunsafeMeta item : player.getInventory().getContents())
 		{
 			for (Map.Entry<RunsafeMeta, Integer> checkNode : checklist.entrySet())
 			{
 				int checkItemAmount = checkNode.getValue();
 				RunsafeMeta checkItem = checkNode.getKey();
 				if (strictItemMatch(item, checkItem))
 				{
 					if (item.getAmount() >= checkItemAmount)
 						checklist.remove(checkItem);
 					else
 						checklist.put(checkItem, checkItemAmount - item.getAmount());
 				}
 			}
 		}
 		return checklist.isEmpty();
 	}
 
 	public void purchase(IPlayer player)
 	{
 		if (playerCanPurchase(player))
 		{
 			RunsafeInventory playerInventory = player.getInventory();
 
 			for (Map.Entry<RunsafeMeta, Integer> items : requiredItems.entrySet())
 				playerInventory.removeExact(items.getKey(), items.getValue());
 
 			for (RunsafeMeta item : purchaseItems)
 				player.give(item);
 
 			ILocation location = player.getLocation();
 
 			if (location != null)
 				location.playSound(Sound.Item.PickUp, 2F, 0F);
 
 			player.sendColouredMessage("&aPurchase complete!");
 		}
 		else
 		{
 			player.sendColouredMessage("&cYou don't have enough to buy that!");
 		}
 	}
 
 	private boolean strictItemMatch(RunsafeMeta item, RunsafeMeta check)
 	{
 		// Check the item is the same.
 		if (!item.is(check.getItemType()))
 			return false;
 
 		// Check the durability matches
 		if (item.getDurability() != check.getDurability())
 			return false;
 
 		String displayName = item.getDisplayName();
 		String checkName = check.getDisplayName();
 
 		// Check the names are either both null, or both not null.
 		if ((displayName == null && checkName != null) || (displayName != null && checkName == null))
 			return false;
 
 		// Check the names match.
 		if (displayName != null && !checkName.equals(displayName))
 			return false;
 
 		// Check the lore.
 		List<String> itemLore = item.getLore();
 		List<String> checkLore = item.getLore();
 
 		// Check the lore lists are either both null, or both not null.
 		if ((itemLore == null && checkLore != null) || (itemLore != null && checkLore == null))
 			return false;
 
 		// Make sure both the lore lists are the same (order-sensitive).
 		if (itemLore != null && !itemLore.equals(checkLore))
 			return false;
 
 		// Enchant checking
 		Map<RunsafeEnchantment, Integer> enchants = item.getEnchantments();
 
 		// Check both enchantment maps are the same size, otherwise they don't match.
 		if (enchants.size() != check.getEnchantments().size())
 			return false;
 
 		// Check all the enchants match.
 		for (Map.Entry<RunsafeEnchantment, Integer> enchantNode : enchants.entrySet())
 		{
 			RunsafeEnchantment enchant = enchantNode.getKey();
 
 			// Make sure the enchant exists on the item and has the same level.
 			if (!check.hasEnchant(enchant) || check.getEnchantLevel(enchant) != enchantNode.getValue())
 				return false;
 		}
 		return true;
 	}
 
 	private List<RunsafeMeta> purchaseItems = new ArrayList<RunsafeMeta>(0);
	private ConcurrentHashMap<RunsafeMeta, Integer> requiredItems = new ConcurrentHashMap<RunsafeMeta, Integer>(0);
 }
