 package tzer0.Money2XP;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.coelho.iConomy.iConomy;
 import com.nijiko.coelho.iConomy.system.Account;
 import com.nijiko.permissions.PermissionHandler;
 
 import cosine.boseconomy.BOSEconomy;
 
 // TODO: Auto-generated Javadoc
 /**
  * The is a listener interface for receiving PlayerCommandPreprocessEvent events.
  * 
  */
 public class Money2XPPlayerListener extends PlayerListener  {
     Configuration conf;
     Money2XP parent;
     public PermissionHandler permissions;
 
     /**
      * Sets the pointers so that they can be referenced later in the code
      *
      * @param config Plugin-config
      * @param parent Money2XP
      * @param permissions Permissions-handler (if available)
      */
     public void setPointers(Configuration config, Money2XP parent, PermissionHandler permissions) {
         conf = config;
         this.parent = parent;
         this.permissions = permissions;
     }
 
     /** 
      * Checks if the command is valid and wether the player attempting to do the command has permissions to do so
      * 
      * @see org.bukkit.event.player.PlayerListener#onPlayerCommandPreprocess(org.bukkit.event.player.PlayerCommandPreprocessEvent)
      */
     public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
         Player player = event.getPlayer();
         String[] command = event.getMessage().split(" ");
         boolean help = false;
         if (command[0].equalsIgnoreCase("/m2x")) {
 
             // user-accessible commands
             event.setCancelled(true);
             if (!(permissions == null || (permissions != null && permissions.has(player, "money2xp.user")))) {
                 player.sendMessage(ChatColor.RED + "You do not have access to this command.");
                 return;
             }
             if (command.length >= 2 && command[1].equalsIgnoreCase("list")) {
                 // lists available skills
                 parent.showSkills((CommandSender)player);
             } else if (command.length == 4 && command[1].equalsIgnoreCase("check")) {
                 xpMod(command[2], command[3], player, true);
             } else if (command.length == 3) {
                 // checks if the skill exists and whether the player has enough money to purchase the required
                 // amount of xp.
                 xpMod(command[1], command[2], player, false);
             } else {
                 help = true;
             }
         } else if (command[0].equalsIgnoreCase("/m2xset")) {
             event.setCancelled(true);
             if (!((permissions == null && player.isOp()) || (permissions != null && permissions.has(player, "money2xp.admin")))) {
                 player.sendMessage(ChatColor.RED + "You do not have access to this command.");
                 return;
             }
             if (command.length == 3) {
                 if (!parent.modValue(command[1], command[2], player)) {
                     help = true;
                 }
             } else {
                 help = true;
             }
         }
         if (help) {
             player.sendMessage(ChatColor.GREEN + "Money2XP by TZer0 (TZer0.jan@gmail.com)");
             player.sendMessage(ChatColor.YELLOW + "Commands:");
             player.sendMessage(ChatColor.YELLOW + "/m2x list - list skills and prices");
             player.sendMessage(ChatColor.YELLOW + "/m2x [skillname] [amount]");
             player.sendMessage(ChatColor.YELLOW + "/m2x check [skillname] [amount]");
             if ((permissions == null && player.isOp()) || permissions.has(player, "money2xp.admin")) {
                 player.sendMessage(ChatColor.YELLOW + "Admin commands:");
                 player.sendMessage(ChatColor.YELLOW + "/m2xset [skillname] [price_per_xp] - sets xp-cost for a skill");
             }
         }
     }
     /**
      * Modifies a skill if test is false, if test is true, displays cost of modification
      * 
      * @param skill The skill to be modified
      * @param xpstring How much xp should be added
      * @param player Player receving the xp and taking the costs
      * @param test If this is a test or not.
      */
     public void xpMod(String skill, String xpstring, Player player, boolean test) {
        Plugin iCTest = parent.getServer().getPluginManager().getPlugin("iconomy");
         Plugin BOS = parent.getServer().getPluginManager().getPlugin("BOSEconomy");
         skill = skill.toLowerCase();
         if (parent.checkInt(xpstring)) {
             if (!parent.skillnames.contains(skill)) {
                 player.sendMessage(ChatColor.RED+"This skill does not exist!");
                 return;
             }
             int xp = Integer.parseInt(xpstring);
             int xpcost = conf.getInt(skill.toLowerCase(), conf.getInt("default", 100));
             
             if (xpcost <= 0) {
                 player.sendMessage(String.format(ChatColor.RED + "Training %s using money has been disabled.", skill));
                 return;
             } else if (Integer.MAX_VALUE/xpcost < xp || xp <= 0) {
                 // Prevents overflows and underflows and adds a disapproving comment.
                 player.sendMessage(ChatColor.RED+String.format("Nice try."));
                 return;
             }
             int bal = 0;
            if (iCTest != null) {
                 Account acc = iConomy.getBank().getAccount(player.getName());
                 bal = (int)acc.getBalance();
             } else if (BOS != null) {
                 bal = ((BOSEconomy) BOS).getPlayerMoney(player.getName());
             } else {
                 player.sendMessage(ChatColor.RED + "No economy system disabled, cancelled.");
                 return;
             }
             
             if (!test && bal < xp*xpcost) {
                 player.sendMessage(ChatColor.RED+String.format("You cannot afford %d %s xp (@%d) since ", 
                         xp, skill, xpcost));
                 player.sendMessage(ChatColor.RED+String.format("your current balance is %d, and this costs %d!", 
                         bal, xpcost*xp));
             } else if (test) {
                 player.sendMessage(ChatColor.YELLOW+String.format("%d %s-xp (@%d) would cost you %d,", 
                         xp, skill, xpcost, xp*xpcost));
                 player.sendMessage(ChatColor.YELLOW+String.format("leaving you with %d money.",
                         ((int) bal)-xp*xpcost));
             } else {
                if (iCTest != null) {
                     iConomy.getBank().getAccount(player.getName()).subtract(xp*xpcost);
                 } else {
                     ((BOSEconomy) BOS).addPlayerMoney(player.getName(), -xp*xpcost, true);
                 }
                 bal -= xp*xpcost;
                 player.sendMessage(ChatColor.GREEN+String.format("You received %d %s-xp(@%d) for %d money!", 
                         xp, skill, xpcost, xp*xpcost));
                 player.sendMessage(ChatColor.GREEN+String.format("You have %d money left", 
                         (int) bal));
                 parent.mcmmo.addXp(player, skill, xp);
             }
         } else {
             player.sendMessage(ChatColor.RED + "Value must be a number.");
         }
     }
 }
