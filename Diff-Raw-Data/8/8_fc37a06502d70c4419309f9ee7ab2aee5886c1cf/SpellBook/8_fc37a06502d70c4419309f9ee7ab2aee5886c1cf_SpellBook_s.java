 package aor.SimplePlugin;
 
 import java.util.ArrayList;
 import aor.SimplePlugin.Spell;
 import org.bukkit.entity.Player;
 import aor.SimplePlugin.Spells.*;
 import org.bukkit.ChatColor;
 import java.lang.String;
 
 public class SpellBook {
 
 	public static SimplePlugin plugin;
 
 	public SpellBook(Player player, SimplePlugin instance) // We take player for possible permissions/op spell disable/enable.
 	{
 		plugin = instance;
 		registerSpell(new RapidfireSpell(plugin)); // Register the rapidfire spell.
 		registerSpell(new ExplosionSpell(plugin)); // Register explosion spell.
 		registerSpell(new SpikeSpell(plugin));
 	}
 
 	ArrayList<Spell> spellRegistry = new ArrayList<Spell>();
 
 	public int index = 0;
 
 	public Spell getSpell(String searchingShortName)
 	{
 		for (int i = 0; i < spellRegistry.size(); i++)
 		{
 			if (spellRegistry.get(i).getShortName().equalsIgnoreCase(searchingShortName))
 			{
 				return spellRegistry.get(i);
 			}
 			else { }
 		}
 		return null;
 	}
 	
 	public int getSpellIndex(String searchingShortName)
 	{
 		for (int i = 0; i < spellRegistry.size(); i++)
 		{
 			if (spellRegistry.get(i).getShortName().equalsIgnoreCase(searchingShortName))
 			{
 				return i;
 			}
 			else { }
 		}
 		return spellRegistry.size() + 1;
 	}
 
 	public void registerSpell(Spell sp){
 		spellRegistry.add(sp);
 	}
 
 	public Spell returnFirstEntry()
 	{
 		return spellRegistry.get(0);
 	}
 
 	public Spell getCurrentSpell()
 	{
 		return spellRegistry.get(index);
 	}
 	
 	public void setSpell(int setIndex)
 	{
 		index = setIndex;
 	}
 	
 	public ChatColor spellFormat(Spell spell, Player player)
 	{
 		if (spell.checkInventoryRequirements(player.getInventory()))
 		{
 			return ChatColor.DARK_GREEN;
 		}
 		else
 		{
 			return ChatColor.DARK_RED;
 		}
 	}
 
 
 	public Spell nextSpell(Player player)
 	{
 		if (index != spellRegistry.size() - 1) { index++; } // Remember indexes start at 0.
 		else { index = 0; }
 		
 		if (getCurrentSpell().checkInventoryRequirements(player.getInventory())) // If they have the right items...
 		{
 			player.sendMessage("Selected spell: " + ChatColor.DARK_GREEN + getCurrentSpell().getName()); // Print spell with green.
 		}
 		else // If they don't.
 		{
 			player.sendMessage("Selected spell: " + ChatColor.DARK_RED + getCurrentSpell().getName()); // Print spell with red.
 		}
 		
 		
 		return getCurrentSpell();
 	}
 
 }
