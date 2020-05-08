 package io.github.alshain01.flags;
 
 import io.github.alshain01.flags.api.Flag;
 import io.github.alshain01.flags.api.FlagsAPI;
 import io.github.alshain01.flags.api.Registrar;
 import io.github.alshain01.flags.api.area.Area;
 import io.github.alshain01.flags.api.area.Subdividable;
 import io.github.alshain01.flags.api.economy.EconomyPurchaseType;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.permissions.Permission;
 
 import java.util.*;
 
 final class CommandFlag extends Command implements CommandExecutor, Listener {
     private final Material tool;
 
     public CommandFlag(Material tool) {
         this.tool = tool;
     }
 
     private enum FlagCommandType {
         SET('s', 3, 1, true, true, "Set <area|wilderness|default> <flag> [true|false]"),
         GET('g', 2, 1, true, false, "Get <area|wilderness|default> [flag]"),
         REMOVE ('r', 2, 1, true, false, "Remove <area|wilderness|default> [flag]"),
         TRUST('t', 4, -1, true, true, "Trust <area|wilderness|default> <flag> <player> [player]..."),
         DISTRUST('d', 3, -1, true, true, "Distrust <area|wilderness|default> <flag> [player] [player]..."),
         VIEWTRUST('v', 3, 0, true, true, "ViewTrust <area|wilderness|default> <flag>"),
         MESSAGE('m', 4, -1, true, true, "Message <area|wilderness|default> <flag> <message>"),
         PRESENTMESSAGE('p', 3, 0, true, true, "PresentMessage <area|wilderness|default> <flag>"),
         ERASEMESSAGE('e', 3, 0, true, true, "EraseMessage <area|wilderness|default> <flag>"),
         CHARGE('c', 3, 1, false, true, "Charge <flag|message> <flag> [price]"),
         HELP ('h', 1, 2, false, null, "Help [group] [page]"),
         INHERIT('i', 1, 1, false, null, "Inherit [true|false]");
 
         private final char alias;
         final int requiredArgs;
         final int optionalArgs; //-1 for infinite
         final boolean requiresLocation;
         final Boolean requiresFlag; // null if flag isn't even an optional arg.
         private final String help;
 
         //Note: requiredArgs INCLUDES the command action
         FlagCommandType(char alias, int requiredArgs, int optionalArgs, boolean hasLocation, Boolean requiresFlag, String help) {
             this.alias = alias;
             this.requiredArgs = requiredArgs;
             this.optionalArgs = optionalArgs;
             this.help = help;
             this.requiresLocation = hasLocation;
             this.requiresFlag = requiresFlag;
         }
 
         static FlagCommandType get(String name) {
             for(FlagCommandType c : FlagCommandType.values()) {
                 if(name.toLowerCase().equals(c.toString().toLowerCase()) || name.toLowerCase().equals(String.valueOf(c.alias))) {
                     return c;
                 }
             }
             return null;
         }
 
         String getHelp() {
             return "/flag " + this.help;
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     private void onPlayerInteract(PlayerInteractEvent e) {
         if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
             if(tool != null && e.getPlayer().getItemInHand().getType() == tool) {
                 if (e.getPlayer().hasPermission("flags.command.flag")) {
                     CommandLocation loc = CommandLocation.WILDERNESS;
                     if(FlagsAPI.hasArea(e.getClickedBlock().getLocation())) {
                         loc = CommandLocation.AREA;
                     }
                     get(e.getPlayer(), e.getClickedBlock().getLocation(), loc, null);
                 }
             }
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
         //if(!cmd.toString().equalsIgnoreCase("flag")) { return false; }
 
         if (args.length < 1) {
             if(sender instanceof Player) {
                 sender.sendMessage(getFlagUsage((Player)sender, FlagsAPI.getAreaAt(((Player)sender).getLocation())));
                 return true;
             }
             return false;
         }
 
         final FlagCommandType command = FlagCommandType.get(args[0]);
         if(command == null) {
             if(sender instanceof Player) {
                 sender.sendMessage(getFlagUsage((Player)sender, FlagsAPI.getAreaAt(((Player)sender).getLocation())));
                 return true;
             }
             return false;
         }
 
         CommandLocation location = null;
         boolean success = false;
         Flag flag = null;
         Set<String> players = new HashSet<String>();
 
         // Check argument length (-1 means infinite optional args)
         if(args.length < command.requiredArgs
                 || (command.optionalArgs > 0 && args.length > command.requiredArgs + command.optionalArgs)) {
             sender.sendMessage(command.getHelp());
             return true;
         }
 
         // Check the command location for those that apply
         if(command.requiresLocation) {
             location = CommandLocation.get(args[1]);
             if(location == null) {
                 sender.sendMessage(command.getHelp());
                 return true;
             }
         }
 
         // Location based commands require the player to be in the world
         // Inherit is a special case, doesn't require a location but assumes one exists
         if((location != null || command == FlagCommandType.INHERIT) && !(sender instanceof Player)) {
             sender.sendMessage(Message.NoConsoleError.get());
             return true;
         }
 
         // Get the flag if required.
         if(command.requiresFlag != null) {
             if(command.requiresFlag || args.length >= 3) {
                 flag = FlagsAPI.getRegistrar().getFlagIgnoreCase(args[2]);
                 if (Validate.notFlag(sender, flag, args[2])) { return true; }
             }
         }
 
         // Process the command
         switch(command) {
             case HELP:
                 help(sender, getPage(args), getGroup(args), getFlag(args));
                 success = true;
                 break;
             case INHERIT:
                 inherit((Player) sender, getValue(args, 1));
                 success = true;
                 break;
             case GET:
                 get((Player)sender, location, flag);
                 success = true;
                 break;
             case SET:
                 set((Player) sender, location, flag, getValue(args, 3));
                 success = true;
                 break;
             case REMOVE:
                 remove((Player) sender, location, flag);
                 success = true;
                 break;
             case VIEWTRUST:
                 viewTrust((Player) sender, location, flag);
                 success = true;
                 break;
             case TRUST:
                 players = getPlayers(args, command.requiredArgs - 1);
                 success = trust((Player)sender, location, flag, players);
                 break;
             case DISTRUST:
                 if(args.length > command.requiredArgs) {
                     players = getPlayers(args, command.requiredArgs); } // Players can be omitted to distrust all
                 distrust((Player) sender, location, flag, players);
                 success = true;
                 break;
             case PRESENTMESSAGE:
                 success = presentMessage((Player)sender, location, flag);
                 break;
             case MESSAGE:
                 // Build the message from the remaining arguments
                 StringBuilder message = new StringBuilder();
                 for (int x = 3; x < args.length; x++) {
                     message.append(args[x]);
                     if (x < args.length - 1) {	message.append(" "); }
                 }
 
                 message((Player) sender, location, flag, message.toString());
                 success = true;
                 break;
             case ERASEMESSAGE:
                 erase((Player) sender, location, flag);
                 success = true;
                 break;
             case CHARGE:
                 final EconomyPurchaseType t = EconomyPurchaseType.get(args[1]);
                 if (t != null && args.length > 3) {
                     success = setPrice(sender, t, flag, args[3]);
                 } else {
                     getPrice(sender, t, flag);
                     success = true;
                 }
                 break;
         }
 
         if(!success) {
             sender.sendMessage(command.getHelp());
         }
         return true;
     }
 
     private static String getFlagUsage(Player player, Area area) {
         //usage: /flag <get|set|remove|trust|distrust|viewtrust|message|presentmessage|erasemessage|charge|inherit|help>
         // We can assume if we get this far, the player has read access to the command.
         StringBuilder usage = new StringBuilder("/flag <get");
         if(player.hasPermission("flags.command.flag.set") && area.hasPermission(player)) {
             usage.append("|set|remove|trust|distrust");
         }
         usage.append("|viewtrust"); //read access
 
         if(player.hasPermission("flags.command.flag.set") && area.hasPermission(player)) {
             usage.append("|message|erasemessage");
         }
         usage.append("|presentmessage");
 
         if(Flags.getEconomy() != null && player.hasPermission("flags.command.flag.charge")) {
             usage.append("|charge");
         }
 
         if(player.hasPermission("flags.command.flag.set") && area.hasPermission(player) && FlagsAPI.getCuboidPlugin().hasSubdivisions()) {
             usage.append("|inherit");
         }
 
         usage.append("|help>");
 
         return usage.toString();
     }
 
     private static String getGroup(String[] args) {
         if(args.length < 2) { return null; }
         if(args.length >= 3) { return args[1]; }
 
         // Either group or page was omitted, which one?
         try {
             //noinspection ResultOfMethodCallIgnored
             Integer.parseInt(args[1]);
         } catch (Exception e) {
             // It was a string.
             return args[1];
         }
         // It was an integer
         return null;
     }
 
     private static Flag getFlag(String[] args) {
         Registrar registrar = FlagsAPI.getRegistrar();
         if(args.length == 2 && registrar.isFlag(args[1]))  {
             return registrar.getFlag(args[1]);
         }
         return null;
     }
 
     /*
  * Value Command Handlers
  */
     private void get(Player player, CommandLocation locType, Flag flag) {
         get(player, player.getLocation(), locType, flag);
     }
 
     private void get(Player player, Location location, CommandLocation locType, Flag flag) {
         // Acquire the area
         final Area area = getArea(location, locType);
 
         if(Validate.notArea(player, area)) { return; }
 
         if (flag != null) {
             // Return the single flag requested
             player.sendMessage(Message.GetFlag.get()
                     .replace("{AreaType}", area.getCuboidPlugin().getCuboidName().toLowerCase())
                     .replace("{Flag}", flag.getName())
                     .replace("{Value}", getFormattedValue(area.getValue(flag, false)).toLowerCase()));
             return;
         }
 
         // No flag provided, list all set flags for the area
         StringBuilder message = new StringBuilder(Message.GetAllFlags.get()
                 .replace("{AreaType}", area.getCuboidPlugin().getCuboidName().toLowerCase()));
         boolean first = true; // Governs whether we insert a comma or not (true means no)
         Boolean value;
         Area defaultArea = new AreaDefault(player.getWorld());
 
         for(Flag f : FlagsAPI.getRegistrar().getFlags()) {
             value = area.getValue(f, true);
 
             // Output the flag name
             if (value != null) {
                 if ((area instanceof AreaDefault && value != f.getDefault())
                         || (!(area instanceof AreaDefault) && value != defaultArea.getValue(f, false))){
                     if (!first) { message.append(", ");	}
                     else { first = false; }
                     message.append(f.getName());
                 }
             }
         }
         message.append(".");
         player.sendMessage(message.toString());
     }
 
     private static void set(Player player, CommandLocation location, Flag flag, Boolean value) {
         // Acquire the area
         Area area = getArea(player, location);
         if(Validate.notPermittedFlag(player, area, flag, flag.getName())) { return; }
 
         // Acquire the value (maybe)
         if(value == null) {	value = !area.getValue(flag, false); }
 
         // Set the flag
         if(area.setValue(flag, value, player)) {
             player.sendMessage(Message.SetFlag.get()
                     .replace("{AreaType}", area.getCuboidPlugin().getCuboidName().toLowerCase())
                     .replace("{Flag}", flag.getName())
                     .replace("{Value}", getFormattedValue(value).toLowerCase()));
         }
     }
 
     private static void remove(Player player, CommandLocation location, Flag flag) {
         // Acquire the area
         Area area = getArea(player, location);
         if(Validate.notArea(player, area) || Validate.notPermittedFlag(player, area)) { return; }
 
         // Removing single flag type
         if (flag != null) {
             if (Validate.notPermittedFlag(player, flag)) { return; }
 
             if(area.setValue(flag, null, player)) {
                 player.sendMessage(Message.RemoveFlag.get()
                         .replace("{AreaType}", area.getCuboidPlugin().getCuboidName().toLowerCase())
                         .replace("{Flag}", flag.getName()));
             }
             return;
         }
 
         // Removing all flags if the player has permission
         boolean success = true;
         for(Flag f : FlagsAPI.getRegistrar().getFlags()) {
             if(area.getValue(f, true) != null) {
                 if (!player.hasPermission(f.getPermission()) || !area.setValue(f, null, player)) {
                     success = false;
                 }
             }
         }
 
         player.sendMessage((success ? Message.RemoveAllFlags.get() : Message.RemoveAllFlagsError.get())
                 .replace("{AreaType}", area.getCuboidPlugin().getCuboidName().toLowerCase()));
     }
 
     /*
      * Trust Command Handlers
      */
     private static void viewTrust(Player player, CommandLocation location, Flag flag) {
         boolean first = true;
         StringBuilder message;
         Area area = getArea(player, location);
 
         Set<String> trustList = new HashSet<String>();
         if(player.hasPermission("flags.view.permtrust")) {
             Set<Permission> perms = area.getPermissionTrustList(flag);
             for(Permission p : perms) {
                 trustList.add(p.getName());
             }
         }
         trustList.addAll(area.getPlayerTrustList(flag).values());
 
         if(Validate.notPlayerFlag(player, flag)
                 || Validate.notArea(player, area)
                 || Validate.notTrustList(player, trustList, area.getCuboidPlugin().getCuboidName(), flag.getName())) { return; }
 
         // List all set flags
         message = new StringBuilder(Message.GetTrust.get()
                 .replace("{AreaType}", area.getCuboidPlugin().getCuboidName().toLowerCase())
                 .replace("{Flag}", flag.getName()));
 
         for (String p : trustList) {
             if (!first) { message.append(", ");	}
             else { first = false; }
             message.append(p);
         }
 
         message.append(".");
         player.sendMessage(message.toString());
     }
 
     private static boolean trust(Player player, CommandLocation location, Flag flag, Set<String> trustees) {
         if(trustees.size() == 0) { return false; }
 
         Area area = getArea(player, location);
         if(Validate.notPlayerFlag(player, flag)
                 || Validate.notPermittedFlag(player, area, flag, flag.getName())) { return true; }
 
         Set<String> permissions = new HashSet<String>();
         Set<String> playerList = new HashSet<String>();
         for(String t : trustees) {
             if(t.contains(".")) {
                 permissions.add(t);
             }
             playerList.add(t);
         }
 
         Set<Player> players = getPlayerList(player, playerList);
 
         boolean success = true;
         for(Player p : players) {
             if(!area.setPlayerTrust(flag, p, player)) { success = false; }
         }
 
         for(String p : permissions) {
             if(!area.setPermissionTrust(flag, p, player)) { success = false; }
         }
 
         player.sendMessage((success ? Message.SetTrust.get() : Message.SetTrustError.get())
                 .replace("{AreaType}", area.getCuboidPlugin().getCuboidName().toLowerCase())
                 .replace("{Flag}", flag.getName()));
         return true;
     }
 
     private static void distrust(Player player, CommandLocation location, Flag flag, Set<String> trustees) {
         boolean success = true;
         Area area = getArea(player, location);
 
         if(Validate.notPlayerFlag(player, flag)
                 || Validate.notPermittedFlag(player, area, flag, flag.getName())) { return; }
 
         if(Validate.notTrustList(player, trustees, area.getCuboidPlugin().getCuboidName(), flag.getName())) {
             return;
         }
 
         Set<String> permissions = new HashSet<String>();
         Set<String> playerList = new HashSet<String>();
         for(String t : trustees) {
             if(t.contains(".")) {
                 permissions.add(t);
             }
             playerList.add(t);
         }
 
         Map<UUID, String> trustList = area.getPlayerTrustList(flag);
 
         //If playerList is empty, remove everyone
         for(String p : playerList.isEmpty() && permissions.isEmpty() ? trustList.values() : playerList) {
             for(UUID u : trustList.keySet()) {
                 if(trustList.get(u).equals(p)) {
                     if (!area.removePlayerTrust(flag, u, player)) { success = false; }
                 }
             }
         }
 
         for(String p : permissions) {
             if(!area.removePermissionTrust(flag, p, player)) { success = false; }
         }
 
         player.sendMessage((success ? Message.RemoveTrust.get() : Message.RemoveTrustError.get())
                 .replace("{AreaType}", area.getCuboidPlugin().getCuboidName().toLowerCase())
                 .replace("{Flag}", flag.getName()));
     }
 
     /*
      * Message Command Handlers
      */
     private static boolean presentMessage(Player player, CommandLocation location, Flag flag) {
         // Acquire the flag
         if(!flag.isPlayerFlag()) {
             player.sendMessage(Message.PlayerFlagError.get()
                     .replace("{Flag}", flag.getName()));
             return true;
         }
 
         // Acquire the area
         Area area = getArea(player, location);
         if(area == null) { return false; }
 
         // Send the message
        player.sendMessage(area.getMessage(flag, player.getName()));
         return true;
     }
 
     private static void message(Player player, CommandLocation location, Flag flag, String message) {
         Area area = getArea(player, location);
 
         if(Validate.notPlayerFlag(player, flag)
                 || Validate.notPermittedFlag(player, area, flag, flag.getName())) { return; }
 
         if(area.setMessage(flag, message, player)) {
            player.sendMessage(area.getMessage(flag, player.getName()));
         }
     }
 
     private static void erase(Player player, CommandLocation location, Flag flag) {
         Area area = getArea(player, location);
 
         if(Validate.notPlayerFlag(player, flag)
                 || Validate.notPermittedFlag(player, area, flag, flag.getName())) { return; }
 
         if (area.setMessage(flag, null, player)) {
            player.sendMessage(area.getMessage(flag, player.getName()));
         }
     }
 
     /*
      * Inheritance Command Handlers
      */
     private static void inherit(Player player, Boolean value) {
         Area area = getArea(player, CommandLocation.AREA);
         if(Validate.notSubdividable(player)
                 || Validate.notArea(player, area)
                 || Validate.notSubdivision(player, area)) {
             return;
         }
 
         if(value == null) {
             value = !((Subdividable)area).isInherited();
         }
 
         ((Subdividable)area).setInherited(value);
         player.sendMessage(Message.SetInherited.get()
                 .replace("{Value}", getFormattedValue(((Subdividable) area).isInherited()).toLowerCase()));
     }
 
     /*
      * Price Command Handlers
      */
     private static void getPrice(CommandSender sender, EconomyPurchaseType type, Flag flag) {
         if(Validate.noEconomyInstalled(sender)) { return; }
 
         sender.sendMessage(Message.GetPrice.get()
                 .replace("{PurchaseType}", type.getLocal().toLowerCase())
                 .replace("{Flag}", flag.getName())
                 .replace("{Price}", Flags.getEconomy().format(flag.getPrice(type))));
     }
 
     private static boolean setPrice(CommandSender sender, EconomyPurchaseType type, Flag flag, String price) {
         if(Validate.noEconomyInstalled(sender)) { return true; }
         if((sender instanceof Player) && Validate.notPermittedEditPrice(sender)) { return true; }
 
         double p;
         try { p = Double.valueOf(price); }
         catch (NumberFormatException ex) { return false; }
 
         flag.setPrice(type, p);
         sender.sendMessage(Message.SetPrice.get()
                 .replace("{PurchaseType}", type.getLocal().toLowerCase())
                 .replace("{Flag}", flag.getName())
                 .replace("{Price}", price));
         return true;
     }
 
     /*
      * Help Command Handlers
      */
     private static void help (CommandSender sender, int page, String group, Flag flag) {
         if(flag != null) {
             sendFlagHelp(sender, flag);
             return;
         }
 
         Registrar registrar = FlagsAPI.getRegistrar();
 
         //Build the list of help topics
         Set<String> groupNames = new HashSet<String>();
 
         //Get all flags or a group of flags
         Set<Flag> flags;
         if(group == null) {
             flags = registrar.getPermittedFlags(sender);
             groupNames = registrar.getPermittedFlagGroups(sender);
         } else {
             flags = registrar.getPermittedGroup(sender, group);
         }
 
         // Sort the flags
         List<String> flagNames= new ArrayList<String>();
         for(Flag f : flags) { flagNames.add(f.getName()); }
         Collections.sort(flagNames);
 
         // Combine the lists
         List<String> topics = new ArrayList<String>(groupNames);
         Collections.sort(topics);
         topics.addAll(flagNames);
 
         // No flags were found, there should always be flags.
         if(Validate.isNullOrEmpty(sender, topics, Message.Flag)) { return; }
 
         //Get total pages
         //1 header per page
         //9 flags per page, except on the first which has a usage line and 8 flags
         int total = ((topics.size() + 1) / 9);
 
 
         // Add the last page, if the last page is not full (less than 9 flags)
         if ((topics.size() + 1) % 9 != 0) { total++; }
 
         //Check the page number requested
         if (page < 1 || page > total) { page = 1; }
 
         String indexType = Message.Index.get();
         if(group != null) {
             indexType = registrar.getFlag(topics.get(0)).getGroup();
         }
 
         sender.sendMessage(Message.HelpHeader.get()
                 .replace("{Group}", indexType)
                 .replace("{Page}", String.valueOf(page))
                 .replace("{TotalPages}", String.valueOf(total))
                 .replace("{Type}", Message.Flag.get()));
 
         // Setup for only displaying 10 lines at a time (including the header)
         int lineCount = 0;
 
         // Usage line.
         if (page == 1) {
             if(group == null) {
                 sender.sendMessage(Message.HelpInfo.get()
                         .replace("{Type}", Message.Flag.get().toLowerCase()));
             } else {
                 sender.sendMessage(Message.GroupHelpInfo.get()
                         .replace("{Type}", registrar.getFlag(flagNames.get(0)).getGroup()));
             }
             lineCount++;
         }
 
         // Find the start position in the array of names
         int position = ((page - 1) * 9) - 1;
         if(position < 0) { position = 0; }
 
 
 
         // Output the results
         while (position < topics.size()) {
             String message;
             if(groupNames.contains(topics.get(position))) {
                 message = Message.HelpTopic.get()
                         .replace("{Topic}", topics.get(position))
                         .replace("{Description}", Message.GroupHelpDescription.get().replace("{Group}", topics.get(position)));
 
             } else {
                 message = Message.HelpTopic.get()
                         .replace("{Topic}", topics.get(position))
                         .replace("{Description}", registrar.getFlag(topics.get(position)).getDescription());
             }
 
             if(message.length() > 51) {
                 message = message.substring(0, 47) + "...";
             }
             sender.sendMessage(message);
 
             if(++lineCount == 9) {
                 return;
             }
             position++;
         }
     }
 
     private static void sendFlagHelp(CommandSender sender, Flag flag) {
         sender.sendMessage(Message.FlagHelpHeader.get()
                 .replace("{Flag}", flag.getName()));
         sender.sendMessage(Message.FlagDescription.get()
                .replace("{Description}", flag.getDescription()));
     }
 }
