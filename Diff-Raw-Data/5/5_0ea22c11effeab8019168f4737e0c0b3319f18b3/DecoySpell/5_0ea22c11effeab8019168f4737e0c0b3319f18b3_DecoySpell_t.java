 package aor.SimplePlugin.Spells;
 
 import java.util.List;
 
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftItem;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.Material;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Creature;
 
 import aor.SimplePlugin.SimplePlugin;
 import aor.SimplePlugin.Spell;
 
 
 public class DecoySpell extends Spell {
 	
 	
 	public static SimplePlugin plugin;
 	int MAXDISTANCE = 50;
 	public DecoySpell(SimplePlugin instance) // Constructor.
 	{
 		plugin = instance;
 		spellName = "Decoy";
 		spellDescription = "Spawns a cow, and causes all mobs within a 5 block radius to attack it.";
 		
 		setRequiredItems(new ItemStack(Material.LEATHER, 4), new ItemStack(Material.PORK, 1)); // We need 4 leather and 1 porkchop.
 	}
 	
 	
 	public void castSpell (Player player)
 	{
 		PlayerInventory inventory = player.getInventory();
 
 		if (checkInventoryRequirements(inventory))
 		{
 
 			Block targetBlock = player.getTargetBlock(null, MAXDISTANCE); // Select the target block.
 			
 			if (targetBlock.getType() != Material.AIR) // No cows midair!
 			{
				
				removeRequiredItemsFromInventory(inventory); // Remove the items.

 				LivingEntity cow = player.getWorld().spawnCreature(targetBlock.getLocation(), CreatureType.COW);
 				List<Entity> nearCow = cow.getNearbyEntities(10, 10, 10);
 				for (int i = 0; i < nearCow.size();i++){
 					Entity currentCreature = nearCow.get(i);
 					try {
 						if (currentCreature.getClass() != CraftPlayer.class && currentCreature.getClass() != CraftItem.class) //You can't make no player or item target nothin'!
 						{
 							((Creature) currentCreature).setTarget(cow);
 						}
 					} catch (Exception e){
 						e.printStackTrace();	//If there is an error, we want reports!
 					}
 				}
 			}
 
 			
 			
 		} else {
 			player.sendMessage("Could not cast! Spell requires 4 leather and 1 raw porkchop.");
 			} // They don't have the proper items.
 		
 	}
 }
