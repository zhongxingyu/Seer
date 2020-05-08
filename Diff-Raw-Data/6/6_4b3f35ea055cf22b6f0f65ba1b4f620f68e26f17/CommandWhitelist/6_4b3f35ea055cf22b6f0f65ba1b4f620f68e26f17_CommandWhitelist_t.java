 package dev.xyzcraft.net.bkbw;
 
 import java.util.HashSet;
 import net.md_5.bungee.ChatColor;
import net.md_5.bungee.Permission;
 import net.md_5.bungee.command.CommandSender;
 import net.md_5.bungee.plugin.JavaPlugin;
 import org.json.JSONException;
 
 public class CommandWhitelist extends MacCommand{
 
 	public CommandWhitelist(JavaPlugin pl) {
 		super(pl);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void execute(CommandSender arg0, String[] arg1) {
             // TODO Auto-generated method stub
            if (getPermission(arg0) != Permission.ADMIN && getPermission(arg0) != Permission.MODERATOR) {
                arg0.sendMessage(StringFormatter.error("Permission denied."));
                return;
            }
             if (arg1.length < 1) {
                 arg0.sendMessage(StringFormatter.error("Unknow command (Valid commands: add,remove,list,on,off)"));
                 return;
             }
             if (arg1[0].equals("add")) {
                 if (arg1.length < 2) {
                     arg0.sendMessage(StringFormatter.error("Specify someone to add from the whitelist"));
                     return;
                 }
                 String[] returnVal = this.add(arg1[1]);
                 for (String val : returnVal) {
                     arg0.sendMessage(val);
                 }
             }
             else if (arg1[0].equals("remove")) {
                 if (arg1.length < 2) {
                     arg0.sendMessage(StringFormatter.error("Specify someone to remove from the whitelist"));
                     return;
                 }
                 String[] returnVal = this.remove(arg1[1]);
                 for (String val : returnVal) {
                     arg0.sendMessage(val);
                 }
             }
             else if (arg1[0].equals("list")) {
                 String[] returnVal = this.list();
                 for (String val : returnVal) {
                     arg0.sendMessage(val);
                 }
             }
             else if (arg1[0].equals("on")) {
                 String[] returnVal = this.on();
                 for (String val : returnVal) {
                     arg0.sendMessage(val);
                 }
             }
             else if (arg1[0].equals("off")) {
                 String[] returnVal = this.off();
                 for (String val : returnVal) {
                     arg0.sendMessage(val);
                 }
             }
             else {
                 arg0.sendMessage(StringFormatter.error("Unknow command (Valid commands: add,remove,list,on,off)"));
             }
             
         }
         private String[] add(String name) {
             ((WBKMain)this.plugin).whitelistb.whitelist(name);
             String[] re = {ChatColor.GREEN + "You have whitelisted " + ChatColor.YELLOW + name};
             return re;
         }
         private String[] remove(String name) {
             ((WBKMain)this.plugin).whitelistb.unWhitelist(name);
             String[] re = {ChatColor.RED + "You have removed " + ChatColor.YELLOW + name + ChatColor.RED + " from the whitelist"};
             return re;
         }
         private String[] list() {
             HashSet<String> list;
             try {
               list = ((WBKMain)this.plugin).whitelistb.list();
             } catch (JSONException ex) {
                 String[] re = {ChatColor.DARK_RED + "COMMAND ERROR. INTERNAL"};
                 return re;            
             }
             String formattedList = "";
             for (String user : list) {
                 formattedList = formattedList + ChatColor.YELLOW + user + ChatColor.RED + ", ";
             }
             formattedList = formattedList.substring(0, formattedList.length()-2);
             String[] re = {ChatColor.RED + "The whitelist is currently " + ((((WBKMain)this.plugin).whitelistb.whitelistOn()) ? (ChatColor.GREEN + "on") : (ChatColor.DARK_RED + "off")),ChatColor.RED + "There are currently " + ChatColor.YELLOW + list.size() + ChatColor.RED + " people whitelisted :" ,formattedList};
             return re;
             
         }
         private String[] on() {
         try {
             ((WBKMain)this.plugin).whitelistb.turnOnWhitelist();
         } catch (JSONException ex) {
             String[] re = {ChatColor.DARK_RED + "COMMAND ERROR. INTERNAL"};
             return re;
         }
             String[] re = {ChatColor.RED + "Whitelist turned on"};
             return re;
         }
         private String[] off() {
         try {
             ((WBKMain)this.plugin).whitelistb.turnOffWhitelist();
         } catch (JSONException ex) {
             String[] re = {ChatColor.DARK_RED + "COMMAND ERROR. INTERNAL"};
             return re;
         }            String[] re = {ChatColor.RED + "Whitelist turned off"};
             return re;
         }
 }
