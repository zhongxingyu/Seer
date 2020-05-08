 package me.Sk8r2K10.sGift;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class sGiftCommandExecutor implements CommandExecutor {
 // =)
     private sGift plugin;
     ArrayList<Gift> gifts = new ArrayList<Gift>();
     ArrayList<Trade> trades = new ArrayList<Trade>();
 
     public sGiftCommandExecutor(sGift instance) {
         plugin = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
         Player player = null;
         String prefix = ChatColor.WHITE + "[" + ChatColor.GREEN + "sGift" + ChatColor.WHITE + "] ";
         String prefix2 = ChatColor.WHITE + "[" + ChatColor.GOLD + "sGift" + ChatColor.WHITE + "] ";
         Logger log = Logger.getLogger("Minecraft");
 
         if (sender instanceof Player) {
 
             player = (Player) sender;
         }
         if (commandLabel.equalsIgnoreCase("gift") && sender.hasPermission("sgift.gift")) {
             if (player == null) {
 
                 log.warning(prefix + ChatColor.RED + "Don't send sGift commands through console!");
 
             } else if (args.length == 1) {
                 if (args[0].equalsIgnoreCase("help") && player.hasPermission("sgift.gift.help")) {
 
                     player.sendMessage(ChatColor.DARK_GRAY + "----------------[" + ChatColor.GREEN + "sGift - Gift Help Menu" + ChatColor.DARK_GRAY + "]-----------------");
                     player.sendMessage(ChatColor.GREEN + "/gift <PlayerName> <ItemID:#> <Amount>" + ChatColor.GRAY + " - Gifts a player a block");
                     player.sendMessage(ChatColor.GRAY + "example: /gift Bob log:redwood 1337");
                     player.sendMessage(ChatColor.GREEN + "/gift accept" + ChatColor.GRAY + " - Accepts a Pending Gift.");
                     player.sendMessage(ChatColor.GREEN + "/gift deny" + ChatColor.GRAY + " - Denies a pending Gift.");
                     player.sendMessage(ChatColor.GREEN + "/gift cancel" + ChatColor.GRAY + " - Cancels a gift in progress");
                     player.sendMessage(ChatColor.GREEN + "/gift help" + ChatColor.GRAY + " - Brings up this Menu.");
 
                     if (player.hasPermission("sgift.admin")) {
 
                         player.sendMessage(ChatColor.GREEN + "/gift stop" + ChatColor.GRAY + " - Stops all gifts in progress");
                     }
 
                 } else if (args[0].equalsIgnoreCase("accept")) {
 
                     Gift gift = null;
 
                     for (Gift g : gifts) {
 
                         if (g.Victim == player) {
 
                             gift = g;
                         }
                     }
 
                     if (gift == null) {
 
                         player.sendMessage(prefix + ChatColor.RED + "No Gifts to accept!");
                     } else {
                         
                     Player playerSendingItems = gift.playerSender;
                     Player Victim = gift.Victim;
                     ItemStack items = gift.itemStack;
                     
                     if (player.getInventory().firstEmpty() == -1) {
                             Location playerloc = player.getLocation();
                             player.getWorld().dropItemNaturally(playerloc, items);
                             player.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                         }
 
                     Victim.getInventory().addItem(items);
                     playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.WHITE + " Delivered to " + ChatColor.YELLOW + Victim.getName() + ChatColor.WHITE + "!");
                     Victim.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerSendingItems.getDisplayName() + ChatColor.WHITE + "!");
                     log.info(prefix + Victim.getDisplayName() + " recieved " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName());
 
                     gifts.remove(gift);
                     }
 
 
 
                 } else if (args[0].equalsIgnoreCase("deny")) {
 
                     Gift gift = null;
 
                     for (Gift g : gifts) {
 
                         if (g.Victim == player) {
 
                             gift = g;
                         }
                     }
 
                     if (gift == null) {
 
                         player.sendMessage(prefix + ChatColor.RED + "No Gifts to deny!");
                     } else {
                         
                     Player playerSendingItems = gift.playerSender;
                     Player Victim = gift.Victim;
                     ItemStack items = gift.itemStack;
                     
                     if (playerSendingItems.getInventory().firstEmpty() == -1) {
                             Location playerloc = playerSendingItems.getLocation();
                             playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                             playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                         }
 
                     playerSendingItems.getInventory().addItem(items);
                     playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + Victim.getDisplayName() + ChatColor.RED + " has Denied your Gift request!");
                     playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " Has been returned to you.");
                     Victim.sendMessage(prefix + ChatColor.RED + "You denied " + playerSendingItems.getName() + "'s Gift!");
                     log.info(prefix + Victim.getDisplayName() + " denied " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName());
 
                     gifts.remove(gift);
                     }
 
 
 
                 } else if (args[0].equalsIgnoreCase("stop") && player.hasPermission("sgift.admin")) {
 
                     Gift gift = null;
 
                     for (Gift g : gifts) {
 
                         if (g.itemStack != null) {
                             gift = g;
                         }
 
                     }
                     if (gift == null) {
 
                         player.sendMessage(prefix + ChatColor.RED + "No Gifts to stop!");
                     } else {
                         
                     Player playerSendingItems = gift.playerSender;
                     Player Victim = gift.Victim;
                     ItemStack items = gift.itemStack;
                     
                     if (playerSendingItems.getInventory().firstEmpty() == -1) {
                             Location playerloc = playerSendingItems.getLocation();
                             playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                             playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                         }
 
                     playerSendingItems.getInventory().addItem(items);
                     playerSendingItems.sendMessage(prefix + ChatColor.RED + "Your Gift has been cancelled by an Admin!");
                     playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " has been returned to you.");
                     Victim.sendMessage(prefix + ChatColor.RED + "Admin cancelled your Gift.");
                     log.info(prefix + "stopped a gift of " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName());
 
                     gifts.remove(gift);
                     }
 
 
 
 
                 } else if (args[0].equalsIgnoreCase("cancel")) {
 
                     Gift gift = null;
 
                     for (Gift g : gifts) {
 
                         if (g.playerSender == player) {
 
                             gift = g;
                         }
                     }
 
                     if (gift == null) {
 
                         player.sendMessage(prefix + ChatColor.RED + "No Gifts to cancel!");
                     } else {
                         
                     Player playerSendingItems = gift.playerSender;
                     Player Victim = gift.Victim;
                     ItemStack items = gift.itemStack;
                     
                     if (playerSendingItems.getInventory().firstEmpty() == -1) {
                             Location playerloc = playerSendingItems.getLocation();
                             playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                             playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                         }
 
                     playerSendingItems.getInventory().addItem(items);
                     playerSendingItems.sendMessage(prefix + ChatColor.RED + "Cancelled gift!");
                     playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " Has been returned to you.");
                     Victim.sendMessage(prefix + ChatColor.YELLOW + playerSendingItems.getName() + ChatColor.RED + " Cancelled the Gift!");
 
                     gifts.remove(gift); 
                     }
 
                     
 
                 } else if (player.getServer().getPlayer(args[0]) == null) {
 
                     player.sendMessage(prefix + ChatColor.RED + "Player not Online.");
 
                 } else if (player.getServer().getPlayer(args[0]) == player) {
 
                     player.sendMessage(prefix + ChatColor.RED + "Don't gift Items to yourself!");
 
                 } else {
 
                     player.sendMessage(prefix + ChatColor.RED + "Too few arguments!");
                     player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /gift <Player> <Item> <Amount>");
 
                 }
             } else if (args.length == 2) {
 
                 player.sendMessage(prefix + ChatColor.RED + "Too Few arguments!");
                 player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /gift <Player> <Item> <Amount>");
 
             } else if (args.length == 3) {
                 if (player.getServer().getPlayer(args[0]) != player) {
                     if (player.getServer().getPlayer(args[0]) != null) {
 
                         Player Victim = player.getServer().getPlayer(args[0]);
                         int amount = plugin.getInt(args[2]);
 
                         if (Items.parse(args[1], amount) != null) {
 
                             ItemStack Item = new ItemStack(Items.parse(args[1], amount));
 
                             if (amount != 0) {
                                 if (plugin.inventoryContains(player.getInventory(), Item)) {
                                     if (Item.getEnchantments().isEmpty()) {
 
                                         gifts.add(new Gift(Victim, player, Item));
 
                                         player.getInventory().removeItem(Item);
 
                                         player.sendMessage(prefix + ChatColor.WHITE + "Now Gifting " + ChatColor.YELLOW + Item.getAmount() + " " + Items.name(Item) + ChatColor.WHITE + " with " + ChatColor.YELLOW + Victim.getName());
                                         player.sendMessage(prefix + ChatColor.YELLOW + "Waiting for " + Victim.getName() + " to accept...");
                                         Victim.sendMessage(prefix + ChatColor.WHITE + "New Gift from " + ChatColor.YELLOW + player.getDisplayName() + ChatColor.WHITE + " of " + ChatColor.YELLOW + Item.getAmount() + " " + Items.name(Item));
                                         Victim.sendMessage(prefix + ChatColor.WHITE + "Do " + ChatColor.YELLOW + "/Gift accept" + ChatColor.WHITE + " to accept this Gift or " + ChatColor.YELLOW + "/Gift deny" + ChatColor.WHITE + " to deny this Gift!");
 
                                     } else {
 
                                         player.sendMessage(prefix + ChatColor.RED + "You can't Gift enchanted Items! (Yet)");
                                     }
 
                                 } else {
 
                                     player.sendMessage(prefix + ChatColor.RED + "You don't have enough " + Items.name(Item) + ", or Item is partially Used!");
                                 }
 
                             } else {
 
                                 player.sendMessage(prefix + ChatColor.RED + "Amount provided is Invalid!");
                             }
                         } else {
                             player.sendMessage(prefix + ChatColor.RED + "Material provided is Invalid!");
                         }
 
                     } else {
 
                         player.sendMessage(prefix + ChatColor.RED + "Player not Online!");
                     }
 
                 } else {
 
                     player.sendMessage(prefix + ChatColor.RED + "You can't Gift yourself!");
                 }
 
             } else if (args.length == 0) {
 
                 player.sendMessage(prefix + ChatColor.RED + "/gift help for more info.");
 
             } else if (args.length >= 4) {
 
                 player.sendMessage(prefix + ChatColor.RED + "Too many arguments!");
                 player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /gift <Player> <Item> <Amount>");
             }
         } else if (!sender.hasPermission("sgift.gift")) {
 
             player.sendMessage(prefix + ChatColor.RED + "You don't have Permission for that Command!");
 
         } else if (commandLabel.equalsIgnoreCase("trade") && sender.hasPermission("sgift.trade")) {
             if (player == null) {
 
                 log.warning(prefix2 + ChatColor.RED + "Don't send sGift commands through console!");
 
             } else if (args.length == 1) {
                 if (args[0].equalsIgnoreCase("help") && player.hasPermission("sgift.trade.help")) {
 
                     player.sendMessage(ChatColor.DARK_GRAY + "---------------[" + ChatColor.GOLD + "sGift - Trade Help Menu" + ChatColor.DARK_GRAY + "]----------------");
                     player.sendMessage(ChatColor.GOLD + "/trade <PlayerName> <ItemID:#> <Amount> <Price>" + ChatColor.GRAY + " - Trades with a player");
                     player.sendMessage(ChatColor.GRAY + "example: /trade Bob log:redwood 1337 9001");
                     player.sendMessage(ChatColor.GOLD + "/trade accept" + ChatColor.GRAY + " - Accepts a Pending Trade.");
                     player.sendMessage(ChatColor.GOLD + "/trade deny" + ChatColor.GRAY + " - Denies a pending Trade.");
                     player.sendMessage(ChatColor.GOLD + "/trade cancel" + ChatColor.GRAY + " - Cancels a trade in progress");
                     player.sendMessage(ChatColor.GOLD + "/trade help" + ChatColor.GRAY + " - Brings up this Menu.");
 
                 } else if (args[0].equalsIgnoreCase("accept")) {
 
                     Trade trade = null;
 
                     for (Trade t : trades) {
 
                         if (t.Victim == player) {
 
                             trade = t;
                         }
                     }
 
                     if (trade == null) {
 
                         player.sendMessage(prefix2 + ChatColor.RED + "No Trades to accept!");
                     } else {
 
                         Player playerSendingItems = trade.playerSender;
                         Player Victim = trade.Victim;
                         ItemStack items = trade.itemStack;
                         int price = trade.price;
 
                         if (player.getInventory().firstEmpty() == -1) {
                             Location playerloc = player.getLocation();
                             player.getWorld().dropItemNaturally(playerloc, items);
                             player.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                         }
 
                         player.getInventory().addItem(items);
                         plugin.getEcon().withdrawPlayer(Victim.getName(), price);
                         plugin.getEcon().depositPlayer(playerSendingItems.getName(), price);
 
                        playerSendingItems.sendMessage(prefix2 + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.WHITE + " Delivered to " + ChatColor.YELLOW + Victim.getName() + ChatColor.WHITE + " for " + ChatColor.GOLD + price);
                        Victim.sendMessage(prefix2 + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerSendingItems.getDisplayName() + ChatColor.WHITE + " for " + ChatColor.GOLD + price);
                        log.info(prefix2 + Victim.getDisplayName() + " recieved " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName() + " for " + price + plugin.getEcon().currencyNameSingular());
 
                         trades.remove(trade);
                     }
 
 
                 } else if (args[0].equalsIgnoreCase("deny")) {
 
                     Trade trade = null;
 
                     for (Trade t : trades) {
 
                         if (t.Victim == player) {
 
                             trade = t;
                         }
                     }
 
                     if (trade == null) {
 
                         player.sendMessage(prefix2 + ChatColor.RED + "No Trades to deny!");
                     } else {
 
                         Player playerSendingItems = trade.playerSender;
                         Player Victim = trade.Victim;
                         ItemStack items = trade.itemStack;
                         int price = trade.price;
                         
                         if (playerSendingItems.getInventory().firstEmpty() == -1) {
                             Location playerloc = playerSendingItems.getLocation();
                             playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                             playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                         }
 
                         playerSendingItems.getInventory().addItem(items);
                         playerSendingItems.sendMessage(prefix2 + ChatColor.YELLOW + Victim.getDisplayName() + ChatColor.RED + " has Denied your Trade request!");
                         playerSendingItems.sendMessage(prefix2 + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " Has been returned to you.");
                         Victim.sendMessage(prefix2 + ChatColor.RED + "You denied " + playerSendingItems.getName() + "'s Trade!");
                         log.info(prefix2 + Victim.getDisplayName() + " denied " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName() + " for " + price + " " + plugin.getEcon().currencyNameSingular() + "(s)");
 
                         trades.remove(trade);
                     }
 
 
                 } else if (args[0].equalsIgnoreCase("stop") && player.hasPermission("sgift.admin")) {
 
                     Trade trade = null;
 
                     for (Trade t : trades) {
 
                         if (t.itemStack != null) {
                             trade = t;
                         }
 
                     }
                     if (trade == null) {
 
                         player.sendMessage(prefix2 + ChatColor.RED + "No Trades to stop!");
                     } else {
 
                         Player playerSendingItems = trade.playerSender;
                         Player Victim = trade.Victim;
                         ItemStack items = trade.itemStack;
                         int price = trade.price;
                         
                         if (playerSendingItems.getInventory().firstEmpty() == -1) {
                             Location playerloc = playerSendingItems.getLocation();
                             playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                             playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                         }
 
                         playerSendingItems.getInventory().addItem(items);
                         playerSendingItems.sendMessage(prefix2 + ChatColor.RED + "Your Trade has been cancelled by an Admin!");
                         playerSendingItems.sendMessage(prefix2 + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " has been returned to you.");
                         Victim.sendMessage(prefix2 + ChatColor.RED + "Admin cancelled your Trade.");
                         log.info(prefix2 + "stopped a trade of " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName() + " for " + price + " " + plugin.getEcon().currencyNameSingular() + "(s)");
                         player.sendMessage(prefix2 + ChatColor.GREEN + "Cancelled all Trades safely.");
 
                         trades.remove(trade);
                     }
 
                 } else if (args[0].equalsIgnoreCase("cancel")) {
 
                     Trade trade = null;
 
                     for (Trade t : trades) {
 
                         if (t.playerSender == player) {
 
                             trade = t;
                         }
                     }
 
                     if (trade == null) {
 
                         player.sendMessage(prefix2 + ChatColor.RED + "No Trades to cancel!");
                     } else {
 
                         Player playerSendingItems = trade.playerSender;
                         Player Victim = trade.Victim;
                         ItemStack items = trade.itemStack;
                         int price = trade.price;
                         
                         if (playerSendingItems.getInventory().firstEmpty() == -1) {
                             Location playerloc = playerSendingItems.getLocation();
                             playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                             playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                         }
 
                         playerSendingItems.getInventory().addItem(items);
                         playerSendingItems.sendMessage(prefix2 + ChatColor.RED + "Cancelled trade!");
                         playerSendingItems.sendMessage(prefix2 + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " Has been returned to you.");
                         Victim.sendMessage(prefix2 + ChatColor.YELLOW + playerSendingItems.getName() + ChatColor.RED + " Cancelled the Trade!");
 
                         trades.remove(trade);
                     }
 
 
                 } else if (player.getServer().getPlayer(args[0]) == null) {
 
                     player.sendMessage(prefix2 + ChatColor.RED + "Player not Online.");
 
                 } else if (player.getServer().getPlayer(args[0]) == player) {
 
                     player.sendMessage(prefix2 + ChatColor.RED + "Don't trade Items with yourself!");
 
                 } else {
 
                     player.sendMessage(prefix2 + ChatColor.RED + "Too few arguments!");
                     player.sendMessage(prefix2 + ChatColor.GRAY + "Correct usage: /trade <Player> <Item> <Amount> <Price>");
 
                 }
             } else if (args.length == 2) {
 
                 player.sendMessage(prefix2 + ChatColor.RED + "Too Few arguments!");
                 player.sendMessage(prefix2 + ChatColor.GRAY + "Correct usage: /trade <Player> <Item> <Amount> <Price>");
 
             } else if (args.length == 3) {
 
                 player.sendMessage(prefix2 + ChatColor.RED + "Too Few arguments!");
                 player.sendMessage(prefix2 + ChatColor.GRAY + "Correct usage: /trade <Player> <Item> <Amount> <Price>");
 
             } else if (args.length == 4) {
                 if (player.getServer().getPlayer(args[0]) != player) {
                     if (player.getServer().getPlayer(args[0]) != null) {
 
                         int price = plugin.getInt(args[3]);
                         Player Victim = player.getServer().getPlayer(args[0]);
                         int amount = plugin.getInt(args[2]);
 
                         if (Items.parse(args[1], amount) != null) {
 
                             ItemStack Item = new ItemStack(Items.parse(args[1], amount));
 
                             if (amount != 0) {
                                 if (price != 0) {
                                     if (plugin.inventoryContains(player.getInventory(), Item)) {
                                         if (Item.getEnchantments().isEmpty()) {
                                             if (plugin.getEcon().getBalance(Victim.getName()) >= price) {
 
                                                 trades.add(new Trade(Victim, player, Item, price));
 
                                                 player.getInventory().removeItem(Item);
 
                                                 player.sendMessage(prefix2 + ChatColor.WHITE + "Now Trading " + ChatColor.YELLOW + Item.getAmount() + " " + Items.name(Item) + ChatColor.WHITE + " with " + ChatColor.YELLOW + Victim.getName() + ChatColor.WHITE + " for " + ChatColor.GOLD + price + " " + plugin.getEcon().currencyNameSingular() + "(s)");
                                                 player.sendMessage(prefix2 + ChatColor.YELLOW + "Waiting for " + Victim.getName() + " to accept...");
                                                 Victim.sendMessage(prefix2 + ChatColor.WHITE + "New Trade from " + ChatColor.YELLOW + player.getDisplayName() + ChatColor.WHITE + " of " + ChatColor.YELLOW + Item.getAmount() + " " + Items.name(Item) + ChatColor.WHITE + " for " + ChatColor.GOLD + price + " " + plugin.getEcon().currencyNameSingular() + "(s)");
                                                 Victim.sendMessage(prefix2 + ChatColor.WHITE + "Do " + ChatColor.YELLOW + "/trade accept" + ChatColor.WHITE + " to accept this Trade or " + ChatColor.YELLOW + "/trade deny" + ChatColor.WHITE + " to deny this trade!");
                                             } else {
 
                                                 player.sendMessage(prefix2 + ChatColor.RED + "That player doesn't have enough money!");
                                             }
 
                                         } else {
 
                                             player.sendMessage(prefix2 + ChatColor.RED + "You can't trade enchanted Items! (Yet)");
                                         }
 
                                     } else {
 
                                         player.sendMessage(prefix2 + ChatColor.RED + "You don't have enough " + Items.name(Item) + ", or Item is partially Used!");
                                     }
 
 
                                 } else {
                                     player.sendMessage(prefix2 + ChatColor.RED + "Price provided is Invalid!");
                                 }
 
                             } else {
 
                                 player.sendMessage(prefix2 + ChatColor.RED + "Amount provided is Invalid!");
                             }
                         } else {
                             player.sendMessage(prefix2 + ChatColor.RED + "Material provided is Invalid!");
                         }
 
                     } else {
 
                         player.sendMessage(prefix2 + ChatColor.RED + "Player not Online!");
                     }
 
                 } else {
 
                     player.sendMessage(prefix2 + ChatColor.RED + "You can't Trade with yourself!");
                 }
 
             } else if (args.length == 0) {
 
                 player.sendMessage(prefix2 + ChatColor.RED + "/trade help for more info.");
 
             } else if (args.length >= 5) {
 
                 player.sendMessage(prefix2 + ChatColor.RED + "Too many arguments!");
                 player.sendMessage(prefix2 + ChatColor.GRAY + "Correct usage: /trade <Player> <Item> <Amount> <Price>");
             }
         } else if (!sender.hasPermission("sgift.trade") || !sender.hasPermission("sgift.gift") || !sender.hasPermission("sgift.gift.help") || !sender.hasPermission("sgift.admin")) {
 
             player.sendMessage(prefix2 + ChatColor.RED + "You don't have permission for that command!");
 
         }
         return false;
     }
 }
