 package me.hgilman.Spells.Executors;
 
 import me.hgilman.Spells.Spell;
 import me.hgilman.Spells.Spells;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SpellCommandExecutor implements CommandExecutor {
 	private Spells plugin;
 	
 	public SpellCommandExecutor(Spells instance)
 	{
 		plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		Player player;
 		if(sender instanceof Player)
 		{
 			player = (Player) sender;
 
 			if(command.getName().equalsIgnoreCase("spellinfo"))
 			{
 				if(player.getItemInHand().getType() == Material.GOLD_HOE)
 				{
 					if(args.length==0)
 					{
 						Spell currentSpell = plugin.playerBooks.get(player.getName()).getCurrentSpell(); // Default to the current spell.
						sender.sendMessage("Current spell " + currentSpell.abilityFormat(true) + ": " + currentSpell.getDescription() + " Requires" + currentSpell.printRequiredItems());
 						return true;
 					}
 					else // They gave an arg.
 					{
 						if(plugin.playerBooks.get(player.getName()).getSpell(args[0]) != null)
 						{
 							Spell spell = plugin.playerBooks.get(player.getName()).getSpell(args[0]);
							sender.sendMessage(spell.abilityFormat() + ": " + spell.getDescription() + " Requires" + spell.printRequiredItems());
 							return true;
 						}
 						else
 						{
 							sender.sendMessage("Invalid input!");
 							return false;
 						}
 					}
 					//SPELLINFO
 
 				}
 				else
 				{
 					sender.sendMessage("You must be wielding a Golden Scepter to use Spells.");
 					return false;
 				}
 			}
 			else
 			{
 				plugin.log.info(this.toString() + " could not process the commmand: " + command.getName());
 				return false;
 			}
 
 	   /*     else if(command.getName().equalsIgnoreCase("listspells"))
 	        {
 	        	//LISTSPELLS
 	        }
 	        
 	        else if(command.getName().equalsIgnoreCase("setspell"))
 	        {
 	        	//SETSPELL
 	        }*/
 		}
 		else
 		{
 			plugin.log.info(sender.getName() + " tried to use an ingame-only command.");
 			return false; // Only ingame players may use the spell related commands.
 		}
 	}
 	
 
 	
 }
