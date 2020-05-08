 package k2b6s9j.OreEncyclopedia.command;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import net.minecraft.command.CommandBase;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.command.WrongUsageException;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraftforge.oredict.OreDictionary;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.ModContainer;
 
 public class OECommand extends CommandBase
 {
   private List aliases;
   
   public OECommand()
   {
     this.aliases = new ArrayList();
     this.aliases.add("oreencyclopedia");
     this.aliases.add("oe");
   }
 
   @Override
   public String getCommandName()
   {
     return "oreencyclopedia";
   }
 
   @Override
 	public String getCommandUsage(ICommandSender sender) {
 		return "/" + this.getCommandName() + " <help|list|exchange>";
 	}
 
   @Override
   public List getCommandAliases()
   {
     return this.aliases;
   }
 
   @Override
   public void processCommand(ICommandSender sender, String[] arguments)
   {
 	  if (arguments.length <= 0)
 			throw new WrongUsageException("Type '" + this.getCommandUsage(sender) + "' for help.");
 
 		if (arguments[0].matches("help")) {
 			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Format: '" + this.getCommandName() + " <command> <arguments>'"));
 			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Available commands:"));
 			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("- list : Lists all of the items belonging to an entry."));
 			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("- exchange : Exchange the item being held for another in the same entry."));
 			return;
 		} else if (arguments[0].matches("list")) {
 			list(sender, arguments);
 			return;
 		} else if (arguments[0].matches("exchange")) {
 			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Not yet implemented"));
 			return;
 		}
 
 		throw new WrongUsageException(this.getCommandUsage(sender));
   }
   
   private void list(ICommandSender sender, String[] arguments) {
 	  if (arguments.length <= 1) {
 		  sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("You must provide either an OreDictionary entry name or the term 'all'"));
 		  return;
 	  }else if (arguments[1].matches("all")) {
 		  List<String> entries = sortList(Arrays.asList(OreDictionary.getOreNames()));
 		  int size = entries.size();
 		  int perPage = 7;
 		  int pages = (int) Math.ceil(size / (float) perPage);
 			
		  int page = arguments.length == 2 ? 2 : parseIntBounded(sender, arguments[2], 1, pages);
 		  int min = Math.min(page * perPage, size);
 
		  sender.sendChatToPlayer(ChatMessageComponent.func_111082_b("commands.help.header", new Object[] {Integer.valueOf(page), Integer.valueOf(pages)}).func_111059_a(EnumChatFormatting.DARK_GREEN));
 
 		  for (int i = page * perPage; i < min + perPage; i++)
 		  {
 			  if (i >= size)
 			  {
 				  break;
 			  }
 			  String entry = entries.get(i);
 			  sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(entry));
 		  }
 		  return;
 	  } else {
 		  List<String> entries = Arrays.asList(OreDictionary.getOreNames());
 		  List<ItemStack> definitions = OreDictionary.getOres(arguments[1]);
 		  if (entries.contains(arguments[1])) {
 			  sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(arguments[1]));
 			  for (ItemStack definition : definitions) {
 				  sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("- " + definition.getDisplayName() + " (" + definition.toString() + ")"));
 			  }
 			  return;
 		  } else {
 			  sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("The term " + arguments[1] + " is not in the OreDictionary"));
 		  }
 	  }
   }
   
   private List sortList(List list) {
       Collections.sort(list);
       return list;
   }
 
   @Override
   public boolean canCommandSenderUseCommand(ICommandSender icommandsender)
   {
     return true;
   }
 
   @Override
   public List addTabCompletionOptions(ICommandSender icommandsender,
       String[] astring)
   {
     return null;
   }
 
   @Override
   public boolean isUsernameIndex(String[] astring, int i)
   {
     return false;
   }
 
   @Override
   public int compareTo(Object o)
   {
     return 0;
   }
 }
