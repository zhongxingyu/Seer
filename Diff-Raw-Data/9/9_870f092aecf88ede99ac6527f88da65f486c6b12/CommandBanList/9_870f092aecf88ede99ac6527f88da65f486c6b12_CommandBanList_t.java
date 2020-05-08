 package dev.xyzcraft.net.bkbw;
 
 import java.util.HashSet;
 import java.util.Set;
 import net.md_5.bungee.ChatColor;
 import net.md_5.bungee.Permission;
 import net.md_5.bungee.command.CommandSender;
 import net.md_5.bungee.plugin.JavaPlugin;
 
 public class CommandBanList extends MacCommand {
 
 	public CommandBanList(JavaPlugin pl) {
 		super(pl);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void execute(CommandSender arg0, String[] arg1) {
 		// TODO Auto-generated method stub
             if (getPermission(arg0) != Permission.ADMIN && getPermission(arg0) != Permission.MODERATOR) {
                 arg0.sendMessage(StringFormatter.error("No Permission"));
                 return;
             }
             HashSet<String> msgs = new HashSet();
             Set<String> bannedUsers = ((WBKMain)this.plugin).bandb.bannedUsers();
            arg0.sendMessage(ChatColor.RED + "There are currently " + ChatColor.AQUA + bannedUsers.size() + ChatColor.RED + " players banned");
             Integer count = 1;
             for (String banned : bannedUsers) {
                 msgs.add(ChatColor.RED + count.toString() + ". " + ChatColor.YELLOW + banned + ChatColor.RED + " - " + ChatColor.DARK_RED + ((WBKMain)this.plugin).bandb.banReason(banned));
                 count++;
             }
             for (String msg : msgs) {
                 arg0.sendMessage(msg);
             }
 	}
 
 }
