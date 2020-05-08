 package com.censoredsoftware.demigods.greek.item.armor;
 
 import com.censoredsoftware.censoredlib.util.Items;
 import com.censoredsoftware.demigods.engine.item.DivineItem;
 import com.censoredsoftware.demigods.engine.util.Zones;
 import com.censoredsoftware.demigods.greek.item.GreekItem;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerExpChangeEvent;
 import org.bukkit.event.player.PlayerVelocityEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.util.Vector;
 
 import java.util.ArrayList;
 
 public class FaultyBootsOfHermes extends GreekItem
 {
 	public final static String name = "Faulty Boots of Hermes";
 	public final static String description = "Walk as fast as Hermes!";
 	public final static DivineItem.Category category = DivineItem.Category.ARMOR;
 	public final static ItemStack item = Items.create(Material.LEATHER_BOOTS, ChatColor.DARK_GREEN + name, new ArrayList<String>()
 	{
 		{
 			add(ChatColor.BLUE + "" + ChatColor.ITALIC + description);
 		}
 	}, null);
 	public final static Recipe recipe = new ShapedRecipe(item)
 	{
 		{
 			shape("AAA", "ABA", "AAA");
 			setIngredient('A', Material.FEATHER);
 			setIngredient('B', Material.LEATHER_BOOTS);
 		}
 	};
 	public final static Listener listener = new Listener()
 	{
 		@EventHandler
 		public void onPlayerExpChange(PlayerExpChangeEvent event)
 		{
			if(Zones.inNoDemigodsZone(event.getPlayer().getLocation()) || Zones.inNoBuildZone(event.getPlayer(), event.getPlayer().getLocation())) return;
 
 			// Define variables
 			Player player = event.getPlayer();
 
 			if(player.getInventory().getBoots() != null && Items.areEqualIgnoreEnchantments(item, player.getInventory().getBoots()))
 			{
 				event.getPlayer().setVelocity(new Vector(0.1F, 3, 0));
 			}
 		}
 
 		@EventHandler(priority = EventPriority.NORMAL)
 		public void onPlayerVelocity(PlayerVelocityEvent event)
 		{
 			if(Zones.inNoDemigodsZone(event.getPlayer().getLocation()) || Zones.inNoBuildZone(event.getPlayer(), event.getPlayer().getLocation())) return;
 			// Define variables
 			Player player = event.getPlayer();
 
 			if(player.getInventory().getBoots() != null && Items.areEqualIgnoreEnchantments(item, player.getInventory().getBoots()))
 			{
 				Vector victor = event.getVelocity();
 
 				event.setVelocity(new Vector(victor.getX() + 1, victor.getX() + 1, victor.getX() + 1));
 
 				player.setVelocity(new Vector(7, 7, 7));
 			}
 		}
 	};
 
 	public FaultyBootsOfHermes()
 	{
 		super(name, description, category, item, recipe, listener);
 	}
 }
