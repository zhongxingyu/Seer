 package net.daboross.bukkitdev.commandexecutorbase;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabExecutor;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author daboross
  */
 public abstract class CommandExecutorBase implements TabExecutor {
 
     private final Map<String, String> aliasMap = new HashMap<String, String>();
     private final Map<String, Boolean> isConsoleMap = new HashMap<String, Boolean>();
     private final Map<String, String> helpList = new HashMap<String, String>();
     private final Map<String, String[]> helpAliasMap = new HashMap<String, String[]>();
     private final Map<String, String[]> argsMap = new HashMap<String, String[]>();
     private final Map<String, String> permMap = new HashMap<String, String>();
 
     /**
      * Initialize a sub command on this executor.
      */
     protected void initCommand(String cmd, String[] aliases, boolean isConsole, String permission, String[] arguments, String helpString) {
         String lowerCaseCmd = cmd.toLowerCase(Locale.ENGLISH);
         aliasMap.put(lowerCaseCmd, cmd.toLowerCase(Locale.ENGLISH));
         isConsoleMap.put(lowerCaseCmd, isConsole);
         permMap.put(lowerCaseCmd, permission.toLowerCase());
         helpList.put(lowerCaseCmd, helpString);
         if (arguments != null) {
             argsMap.put(lowerCaseCmd, arguments);
         } else {
             argsMap.put(lowerCaseCmd, new String[0]);
         }
         if (aliases != null) {
             for (String alias : aliases) {
                 aliasMap.put(alias.toLowerCase(), lowerCaseCmd);
             }
             helpAliasMap.put(lowerCaseCmd, aliases);
         }
     }
 
     protected void initCommand(String cmd, String[] aliases, boolean isConsole, String permission, String helpString) {
         initCommand(cmd, aliases, isConsole, permission, null, helpString);
     }
 
     protected void initCommand(String cmd, boolean isConsole, String permission, String[] arguments, String helpString) {
         initCommand(cmd, null, isConsole, permission, arguments, helpString);
     }
 
     protected void initCommand(String cmd, boolean isConsole, String permission, String helpString) {
         initCommand(cmd, null, isConsole, permission, null, helpString);
     }
 
     private void invalidSubCommandMessage(CommandSender sender, String label, String[] args) {
         sender.sendMessage(ColorList.MAIN + "The SubCommand: " + ColorList.CMD + args[0] + ColorList.MAIN + " Does not exist for the command " + ColorList.CMD + "/" + getCommandName());
         sender.sendMessage(ColorList.MAIN + "To see all possible sub commands, type " + ColorList.CMD + "/" + label + ColorList.SUBCMD + " ?");
     }
 
     private void noSubCommandMessage(CommandSender sender, Command cmd, String label, String[] args) {
         sender.sendMessage(ColorList.MAIN + "This is a base command, Please Use a sub command after it.");
         sender.sendMessage(ColorList.MAIN + "To see all possible sub commands, type " + ColorList.CMD + "/" + label + ColorList.SUBCMD + " ?");
     }
 
     private void noPermissionMessage(CommandSender sender, Command cmd, String label, String[] args) {
         if (args.length < 1) {
             sender.sendMessage(ColorList.NOPERM + "You don't have permission to run " + ColorList.CMD + "/" + label);
         } else {
             sender.sendMessage(ColorList.NOPERM + "You don't have permission to run " + ColorList.CMD + "/" + label + " " + ColorList.SUBCMD + args[0]);
         }
     }
 
     private void noConsoleMessage(CommandSender sender, Command cmd, String label, String[] args) {
         sender.sendMessage(ColorList.NOPERM + "This command must be run by a player");
     }
 
     /**
      * This will check if the command given is a valid sub command. It will
      * display the correct messages to the player IF the command is not valid in
      * any way. This will check if the command exists, and return null if it
      * doesn't. If the command must be run by a player and the sender isn't a
      * player then this will return null. This will check if the player has
      * permission to access the command, and if they don't, this will tell them
      * they don't and return null. If none of the above, then this will return
      * the command given, aliases turned into the base command. This will run
      * the help message and return null if the sub command is "help".
      */
     protected String isCommandValid(CommandSender sender, Command cmd, String label, String[] args) {
         if (args.length < 1) {
             noSubCommandMessage(sender, cmd, label, args);
             return null;
         }
         String commandName;
         if (aliasMap.containsKey(args[0].toLowerCase(Locale.ENGLISH))) {
             commandName = aliasMap.get(args[0].toLowerCase());
         } else {
             invalidSubCommandMessage(sender, label, args);
             return null;
         }
         if (sender instanceof Player) {
             if (!sender.hasPermission(permMap.get(commandName))) {
                 noPermissionMessage(sender, cmd, label, args);
                 return null;
             }
         }
         boolean isConsole;
         if (isConsoleMap.containsKey(commandName)) {
             isConsole = isConsoleMap.get(commandName);
         } else {
             isConsole = false;
         }
         if (!(sender instanceof Player)) {
             if (!isConsole) {
                 noConsoleMessage(sender, cmd, label, args);
                 return null;
             }
         }
         if (commandName.equalsIgnoreCase("help")) {
             runHelpCommand(sender, cmd, label, getSubArray(args));
             return null;
         }
         return commandName;
     }
 
     protected String[] getArgs(String alias) {
         return argsMap.get(aliasMap.get(alias));
     }
 
     /**
      * This returns an array that is the given array without the first value.
      */
     protected String[] getSubArray(String[] array) {
         if (array.length > 1) {
             List<String> list = Arrays.asList(array).subList(1, array.length);
             return list.toArray(new String[list.size()]);
         } else {
             return new String[0];
         }
     }
 
     protected void runHelpCommand(CommandSender sender, Command mainCommand, String mainCommandLabel, String[] subCommandArgs) {
         sender.sendMessage(ColorList.MAIN + "List Of Possible Sub Commands:");
         for (String str : aliasMap.keySet()) {
             if (str.equalsIgnoreCase(aliasMap.get(str))) {
                if (sender.hasPermission(permMap.get(aliasMap.get(str)))) {
                     sender.sendMessage(getMultipleAliasHelpMessage(str, mainCommandLabel));
                 }
             }
         }
     }
 
     protected String getHelpMessage(String alias, String baseCommand) {
         String subCmd = aliasMap.get(alias);
         StringBuilder resultBuilder = new StringBuilder();
         appendArgsString(subCmd, resultBuilder.append(ColorList.CMD).append("/").append(baseCommand).append(ColorList.SUBCMD).append(" ").append(alias).append(" ")).append(ColorList.HELP).append(helpList.get(subCmd));
         return resultBuilder.toString();
     }
 
     protected String getMultipleAliasHelpMessage(String alias, String baseCommand) {
         StringBuilder resultStringBuilder = new StringBuilder(ColorList.CMD);
         String subcmd = aliasMap.get(alias);
         String[] aliasList = helpAliasMap.get(subcmd);
        resultStringBuilder.append("/").append(baseCommand).append(ColorList.SUBCMD).append(" ").append(subcmd);
         if (aliasList != null) {
             for (String str : aliasList) {
                 resultStringBuilder.append(ColorList.DIVIDER).append("/").append(ColorList.SUBCMD).append(str);
             }
         }
         appendArgsString(subcmd, resultStringBuilder.append(" ")).append(ColorList.HELP).append(helpList.get(subcmd));
         return resultStringBuilder.toString();
     }
 
     protected String getArgsString(String cmd) {
         String[] args = argsMap.get(cmd);
         if (args == null) {
             return "";
         }
         StringBuilder resultBuilder = new StringBuilder(ColorList.ARGS_SURROUNDER);
         for (String arg : args) {
             resultBuilder.append("<").append(ColorList.ARGS).append(arg).append(ColorList.ARGS_SURROUNDER).append("> ");
         }
         return resultBuilder.toString();
     }
 
     /**
      * @return The original StringBuildger.
      */
     protected StringBuilder appendArgsString(String cmd, StringBuilder builder) {
         String[] args = argsMap.get(cmd);
         if (args == null) {
             return builder;
         }
         builder.append(ColorList.ARGS_SURROUNDER);
         for (String arg : args) {
             builder.append("<").append(ColorList.ARGS).append(arg).append(ColorList.ARGS_SURROUNDER).append("> ");
         }
         return builder;
     }
 
     @Override
     public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
         ArrayList<String> returnList = new ArrayList<String>();
         if (cmd.getName().equalsIgnoreCase(getCommandName())) {
             if (args.length == 0) {
                 for (String alias : aliasMap.keySet()) {
                     returnList.add(alias);
                 }
             } else if (args.length == 1) {
                 for (String alias : aliasMap.keySet()) {
                     if (alias.startsWith(args[0])) {
                         if (sender.hasPermission(permMap.get(aliasMap.get(alias)))) {
                             returnList.add(alias);
                         }
                     }
                 }
             } else if (aliasMap.containsKey(args[1])) {
 //                returnList.addAll(Arrays.asList(getArgs(args[1])));
             }
         }
         return returnList;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (cmd.getName().equalsIgnoreCase(getCommandName())) {
             String commandName = isCommandValid(sender, cmd, label, args);
             if (commandName == null) {
                 return true;
             }
             runCommand(sender, cmd, label, commandName, args[0], getSubArray(args));
             return true;
         }
         return false;
     }
 
     public abstract String getCommandName();
 
     public abstract void runCommand(CommandSender sender, Command mainCommand, String mainCommandLabel, String subCommand, String subCommandLabel, String[] subCommandArgs);
 }
