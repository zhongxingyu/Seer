 package no.runsafe.ItemControl.enchant_container;
 
 import no.runsafe.framework.api.block.IBlock;
 import no.runsafe.framework.api.event.player.IPlayerRightClick;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 
 import java.util.List;
 
 public class EnchantContainerHandler implements IPlayerRightClick
 {
 	@Override
 	public boolean OnPlayerRightClick(IPlayer player, RunsafeMeta usingItem, IBlock targetBlock)
 	{
		if (usingItem != null)
 		{
			if (targetBlock != null && usingItem.is(Item.Brewing.GlassBottle))
 			{
 				if (targetBlock.is(Item.Decoration.EnchantmentTable))
 				{
 					int playerLevel = player.getLevel();
 					if (playerLevel > 0)
 					{
 						player.setLevel(0);
 						RunsafeInventory inventory = player.getInventory();
 						inventory.removeExact(usingItem, 1);
 						RunsafeMeta item = Item.Miscellaneous.ExperienceBottle.getItem();
 						item.setAmount(1);
 						item.addLore("§3Contains: §f" + playerLevel + " levels");
 						inventory.addItems(item);
 						player.updateInventory();
 					}
 					else
 					{
 						player.sendColouredMessage("&cYou do not have any levels to put in there.");
 					}
 					return false;
 				}
 			}
 			else if (usingItem.is(Item.Miscellaneous.ExperienceBottle))
 			{
 				List<String> lore = usingItem.getLore();
 				if (lore != null)
 				{
 					String levelString = null;
 					for (String loreString : lore)
						if (loreString.startsWith("§3Contains: ") && loreString.endsWith(" levels"))
 							levelString = loreString;
 
 					if (levelString != null)
 					{
 						String[] stringSplit = levelString.split(" ");
 						player.setLevel(Integer.parseInt(stringSplit[1]));
 						RunsafeInventory inventory = player.getInventory();
 						inventory.removeExact(usingItem, 1);
 						RunsafeMeta item = Item.Brewing.GlassBottle.getItem();
 						item.setAmount(1);
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 }
