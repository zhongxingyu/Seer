 package de.cubeisland.AuctionHouse.Commands;
 
 
 import de.cubeisland.AuctionHouse.AbstractCommand;
 import de.cubeisland.AuctionHouse.CommandArgs;
 import de.cubeisland.AuctionHouse.Auction.Auction;
 import de.cubeisland.AuctionHouse.Auction.Bidder;
 import de.cubeisland.AuctionHouse.Auction.ServerBidder;
 import de.cubeisland.AuctionHouse.AuctionHouse;
 import static de.cubeisland.AuctionHouse.AuctionHouse.t;
 import de.cubeisland.AuctionHouse.AuctionHouseConfiguration;
 import de.cubeisland.AuctionHouse.BaseCommand;
 import de.cubeisland.AuctionHouse.Perm;
 import de.cubeisland.AuctionHouse.Util;
 import net.milkbowl.vault.economy.Economy;
 import org.apache.commons.lang.time.DateFormatUtils;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author Faithcaio
  */
 public class AddCommand extends AbstractCommand
 {
     private static final AuctionHouse plugin = AuctionHouse.getInstance();
     private static final AuctionHouseConfiguration config = plugin.getConfiguration();
     Economy econ = AuctionHouse.getInstance().getEconomy();
 
     public AddCommand(BaseCommand base)
     {
         super(base, "add");
     }
 
     public boolean execute(CommandSender sender, CommandArgs args)
     {
         ItemStack newItem = null;
         Material newMaterial;
         Integer amount;
         Double startBid = 0.0;
         long auctionEnd = 1;
         Integer multiAuction = 1;
         if (!Perm.get().check(sender, "auctionhouse.command.add")) return true;
         if (args.isEmpty())
         {
             sender.sendMessage(t("add_title1"));
             sender.sendMessage(t("add_title2"));
             sender.sendMessage(t("add_title3"));
             sender.sendMessage(t("add_use"));
             sender.sendMessage("");
             return true;
         }
 
         if (args.getString("m") != null)
         {
             multiAuction = args.getInt("m");
             if (multiAuction == null)
             {
                 sender.sendMessage(t("i")+" "+t("add_multi_number"));
                 return true;
             }
             if (!Perm.get().check(sender, "auctionhouse.command.add.multi")) return true;
         }
         if (args.getString(0)==null)
         {
             sender.sendMessage(t("e")+" "+t("invalid_com"));
             return true;
         }
         if (args.getString(0).equalsIgnoreCase("hand"))
         {
             if (!(sender instanceof ConsoleCommandSender))
             {
                 newItem = ((Player) sender).getItemInHand();
                 if (newItem.getType() == Material.AIR)
                 {
                     sender.sendMessage(t("pro")+" "+t("add_sell_hand"));
                     return true;
                 }
                 if (args.getString(1) != null)
                 {
                     Integer length = Util.convertTimeToMillis(args.getString(1));
                     if (length == null)
                     {
                         sender.sendMessage(t("e")+" "+t("add_invalid_length"));
                         return true;
                     }
                     if (length <= config.auction_maxLength)
                     {
                         auctionEnd = (System.currentTimeMillis() + length);
                     }
                     else
                     {
                         sender.sendMessage(t("i")+" "+t("add_max_length",
                                 DateFormatUtils.format(config.auction_maxLength, "dd:hh:mm:ss")));
                         return true;
                     }
                 }
                 else
                 {
                     auctionEnd = (System.currentTimeMillis() + config.auction_standardLength);
                 }
                 if (args.getString(2) != null)
                 {
                     startBid = args.getDouble(2);
                     if (startBid == null)
                     {
                         sender.sendMessage(t("i")+" "+t("add_invalid_time"));
                         return true;
                     }
                 }
                 else
                 {
                     startBid = 0.0;
                 }
             }
         }
         else
         {
             if (args.getItem(0)==null)
             {
                 sender.sendMessage(t("e")+" "+t("add_invalid_item",args.getString(0)));
                 return true;
             }
            
             newMaterial = args.getItem(0).getType();
             if (newMaterial == null)
             {
                 sender.sendMessage(t("i") + " " +t("add_invalid_item",args.getString(0)));
                 return true;
             }
             if (newMaterial.equals(Material.AIR))
             {
                 sender.sendMessage(t("i") +t("add_invalid_item","AIR"));
                 return true;
             }
             amount = args.getInt(1);
             if (amount == null)
             {
                 sender.sendMessage(t("i") + " " + t("add_no_amount"));
                 return true;
             }
             
             newItem = new ItemStack(newMaterial, amount);
             newItem.setDurability(args.getItem(0).getDurability());
             if (args.getString(2) != null)
             {
                 Integer length = Util.convertTimeToMillis(args.getString(2));
                 if (length == -1)
                 {
                     sender.sendMessage(t("e") + " " + t("add_invalid_length"));
                     return true;
                 }
                 if (length <= config.auction_maxLength)
                 {
                     auctionEnd = (System.currentTimeMillis() + length);
                 }
                 else
                 {
                     sender.sendMessage(t("i")+" "+t("add_max_length",Util.convertTime(config.auction_maxLength)));
                     return true;
                 }
             }
             else
             {
                 auctionEnd = (System.currentTimeMillis() + config.auction_standardLength);
             }
             if (args.getString(3) != null)
             {
                 startBid = args.getDouble(3);
                 if (startBid == null)
                 {
                     sender.sendMessage(t("i") + " "+t("add_invalid_startbid"));
                     return true;
                 }
             }
             else
             {
                 startBid = 0.0;
             }
         }
 
         if (sender instanceof ConsoleCommandSender)
         {
             sender.sendMessage(t("i")+" "+t("add_server_create"));
         }
 
         if (newItem == null)
         {
             sender.sendMessage(t("pro")+" "+t("add_server_nohand"));
             return true;
         }
         ItemStack removeItem = newItem.clone();
         removeItem.setAmount(removeItem.getAmount() * multiAuction);
 
         if (!(sender instanceof ConsoleCommandSender))
         {
             if (!((Player) sender).getInventory().contains(removeItem.getType(), removeItem.getAmount()))
             {
                 if (Perm.get().check(sender, "auctionhouse.command.add.cheatItems"))
                 {
                     sender.sendMessage(t("i")+" "+t("add_enough_item")+" "+t("add_cheat"));
                 }
                 else
                 {
                     sender.sendMessage(t("i")+" "+t("add_enough_item"));
                     return true;
                 }
             }
         }
 
         for (ItemStack item : config.auction_blacklist)
         {
             if (item.getType().equals(newItem.getType()))
             {
                 sender.sendMessage(t("e")+" "+t("add_blacklist"));
                 return true;
             }
         }
         Auction newAuction;
         for (int i = 0; i < multiAuction; ++i)
         {
             if (sender instanceof ConsoleCommandSender)
             {
                 newAuction = new Auction(newItem, ServerBidder.getInstance(), auctionEnd, startBid);
             }
             else
             {
                 newAuction = new Auction(newItem, Bidder.getInstance((Player) sender), auctionEnd, startBid);//Created Auction
             }
             if (args.getString("s") != null)
             {
                 if (args.getString("s").equalsIgnoreCase("Server"))
                     if (sender.hasPermission("auctionhouse.command.add.server"))
                     {
                         newAuction.giveServer();
                         if (!(Util.registerAuction(newAuction, ServerBidder.getInstance())))
                         {
                             sender.sendMessage(t("i")+" "+t("add_all_stop"));
                             sender.sendMessage(t("i")+" "+t("add_max_auction",config.auction_maxAuctions_overall));
                             return true;
                         }
                     }
             }
             else
             {
                 if (!(Util.registerAuction(newAuction, sender)))
                 {
                     sender.sendMessage(t("i")+" "+t("add_all_stop"));
                     sender.sendMessage(t("i")+" "+t("add_max_auction",config.auction_maxAuctions_overall));
                     return true;
                 }
                 if (!(sender instanceof ConsoleCommandSender))
                 {
 
                     ((Player) sender).getInventory().removeItem(removeItem);
                 }
                 else
                 {
                     AuctionHouse.log("ServerAuction(s) added succesfully!");
                 }
             }
         }
 
         sender.sendMessage(t("i")+" "+t("add_start",
                                         multiAuction,
                                         newItem.getType().toString()+"x"+newItem.getAmount(),
                                         econ.format(startBid),
                                         DateFormatUtils.format(auctionEnd, config.auction_timeFormat)));                     
         return true;
     }
 
     @Override
     public String getUsage()
     {
         return super.getUsage() + " <Item> <Amount>";
     }
 
     public String getDescription()
     {
         return t("command_add");
     }
 }
