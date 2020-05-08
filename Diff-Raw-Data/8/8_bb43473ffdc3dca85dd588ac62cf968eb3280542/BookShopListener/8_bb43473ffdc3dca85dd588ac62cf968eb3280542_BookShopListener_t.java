 package me.ibhh.BookShop;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 
 public class BookShopListener
         implements Listener {
     
     private final BookShop plugin;
     private InteractHandler interactHandler;
     private CreateHandler createHandler;
     public ChestHandler chestHandler;
     public HashMap<Player, Chest> ChestViewers = new HashMap<Player, Chest>();
     public HashMap<Player, Boolean> NewspapersViewers = new HashMap<Player, Boolean>();
     private static final BlockFace[] shopFaces = {BlockFace.SELF, BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};
     
     public BookShopListener(BookShop BookShop) {
         this.plugin = BookShop;
         try {
             this.interactHandler = new InteractHandler(this.plugin);
             this.createHandler = new CreateHandler(this.plugin);
             this.chestHandler = new ChestHandler(this.plugin);
             BookShop.getServer().getPluginManager().registerEvents(this, BookShop);
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("[BookShop] Error: Uncatch Exeption!");
             this.plugin.getReportHandler().report(3318, "Logger doesnt work", e.getMessage(), "BookShopListener", e);
             try {
                 MetricsHandler.Error += 1;
             } catch (Exception e1) {
             }
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH)
     public void invClose(InventoryCloseEvent event) {
         final InventoryCloseEvent ev = event;
         if (this.ChestViewers.containsKey((Player) event.getPlayer())) {
             this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, new Runnable() {
                 @Override
                 public void run() {
                     try {
                         Chest chest = ((Chest) BookShopListener.this.ChestViewers.get((Player) ev.getPlayer()));
                         if (chest == null) {
                             BookShopListener.this.plugin.Logger("Chest == null", "Debug");
                             return;
                         }
                         if (chest.getBlockInventory() == null) {
                             BookShopListener.this.plugin.Logger("Inv == null", "Debug");
                             return;
                         }
                         if (chest.getBlockInventory().contains(Material.WRITTEN_BOOK)) {
                             BookShopListener.this.plugin.Logger("Chest contains Written_Book", "Debug");
                             Block chestblock = ((Chest) BookShopListener.this.ChestViewers.get((Player) ev.getPlayer())).getBlock();
                             if ((chestblock.getRelative(BlockFace.UP).getState() instanceof org.bukkit.block.Sign)) {
                                 BookShopListener.this.plugin.Logger("Block Relative UP = Sign", "Debug");
                                 org.bukkit.block.Sign sign = (org.bukkit.block.Sign) chestblock.getRelative(BlockFace.UP).getState();
                                 if (sign.getLine(0).equalsIgnoreCase("[BookShop]")) {
                                     BookShopListener.this.plugin.Logger("Sign is BookShop", "Debug");
                                     int slot = ((Chest) BookShopListener.this.ChestViewers.get((Player) ev.getPlayer())).getBlockInventory().first(Material.WRITTEN_BOOK);
                                     ItemStack item = ((Chest) BookShopListener.this.ChestViewers.get((Player) ev.getPlayer())).getBlockInventory().getItem(slot);
                                     if (item != null) {
                                         BookShopListener.this.plugin.Logger("Book != null", "Debug");
                                         BookMeta bm = (BookMeta) item.getItemMeta();
                                         ItemStack loadedBook = BookLoader.load(BookShopListener.this.plugin, bm.getAuthor(), bm.getTitle());
                                         if (loadedBook != null) {
                                             BookMeta bmLoaded = (BookMeta) loadedBook.getItemMeta();
                                             if ((!bmLoaded.getAuthor().equals(bm.getAuthor())) || (!bmLoaded.getTitle().equals(bm.getTitle()))) {
                                                 BookLoader.save(BookShopListener.this.plugin, item);
                                             }
                                         } else {
                                             BookLoader.save(BookShopListener.this.plugin, item);
                                         }
                                         sign.setLine(2, bm.getTitle());
                                         sign.update();
                                     }
                                 }
                             }
                         }
                         BookShopListener.this.ChestViewers.remove((Player) ev.getPlayer());
                         BookShopListener.this.NewspapersViewers.remove((Player) ev.getPlayer());
                     } catch (Exception e) {
                         e.printStackTrace();
                         BookShopListener.this.plugin.getReportHandler().report(3319, "Error on closing chest", e.getMessage(), "BookShopListener", e);
                         MetricsHandler.Error += 1;
                         BookShopListener.this.plugin.Logger("uncatched Exception!", "Error");
                     }
                 }
             }, 2L);
             this.plugin.Logger("Player from ChestViewers removed: " + event.getPlayer().getName(), "Debug");
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH)
     public void invClick(InventoryClickEvent event) {
         try {
             Player player = (Player) event.getWhoClicked();
             if (this.ChestViewers.containsKey(player)) {
                 if (this.NewspapersViewers.containsKey(player)) {
                     if (this.NewspapersViewers.get(player)) {
                         this.plugin.Logger("Slot: " + event.getSlot(), "Debug");
                         this.plugin.Logger("Player " + player.getName() + " clicked on " + event.getInventory().getType().name() + "!", "Debug");
                         if (event.getInventory().getType().equals(InventoryType.CHEST)) {
                             if (event.getCurrentItem() == null) {
                                 return;
                             }
                             ItemStack bookItem = event.getCurrentItem();
                             if (bookItem != null) {
                                 if (bookItem.getType().equals(Material.WRITTEN_BOOK)) {
                                     BookMeta bm = (BookMeta) bookItem.getItemMeta();
                                     if (!bm.getAuthor().equalsIgnoreCase(player.getName())
                                             && (!this.plugin.PermissionsHandler.checkpermissionssilent(player, "BookShop.sell.other"))) {
                                         this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.onlyyourbooks." + this.plugin.getConfig().getString("language")), "Error");
                                         event.setCancelled(true);
                                         return;
                                     }
                                 }
                             }
                             
                             if ((!event.getCurrentItem().getType().equals(Material.WRITTEN_BOOK)) && (!event.getCurrentItem().getType().equals(Material.AIR))) {
                                 if (this.plugin.getConfig().getBoolean("useBookandQuill")) {
                                     if (!event.getCurrentItem().getType().equals(Material.BOOK_AND_QUILL)) {
                                         this.plugin.Logger("Item is " + event.getCurrentItem().getType().name(), "Debug");
                                         this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.wrongItem." + this.plugin.config.language), "Error");
                                         event.setCancelled(true);
                                     }
                                 } else {
                                     this.plugin.Logger("Item is " + event.getCurrentItem().getType().name(), "Debug");
                                     this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.wrongItem." + this.plugin.config.language), "Error");
                                     event.setCancelled(true);
                                 }
                             }
                         } else {
                             this.plugin.Logger("Player " + player.getName() + " clicked on own Inventory!", "Debug");
                         }
                         return;
                     } else {
                         this.plugin.Logger("Slot: " + event.getSlot(), "Debug");
                         this.plugin.Logger("Player " + player.getName() + " clicked on " + event.getInventory().getType().name() + "!", "Debug");
                         if (event.getInventory().getType().equals(InventoryType.CHEST)) {
                             if (event.getCurrentItem() == null) {
                                 return;
                             }
                             ItemStack bookItem = event.getCurrentItem();
                             
                             if (bookItem != null) {
                                 if (bookItem.getType().equals(Material.WRITTEN_BOOK)) {
                                     BookMeta bm = (BookMeta) bookItem.getItemMeta();
                                     if (!bm.getAuthor().equalsIgnoreCase(player.getName())
                                             && (!this.plugin.PermissionsHandler.checkpermissionssilent(player, "BookShop.sell.other"))) {
                                         this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.onlyyourbooks." + this.plugin.getConfig().getString("language")), "Error");
                                         event.setCancelled(true);
                                         return;
                                     }
                                 }
                             }
                             
                             if ((!event.getCurrentItem().getType().equals(Material.WRITTEN_BOOK)) && (!event.getCurrentItem().getType().equals(Material.AIR))) {
                                 if (this.plugin.getConfig().getBoolean("useBookandQuill")) {
                                     if (!event.getCurrentItem().getType().equals(Material.BOOK_AND_QUILL)) {
                                         this.plugin.Logger("Item is " + event.getCurrentItem().getType().name(), "Debug");
                                         this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.wrongItem." + this.plugin.config.language), "Error");
                                         event.setCancelled(true);
                                     }
                                 } else {
                                     this.plugin.Logger("Item is " + event.getCurrentItem().getType().name(), "Debug");
                                     this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.wrongItem." + this.plugin.config.language), "Error");
                                     event.setCancelled(true);
                                 }
                             } else if (((countWrittenBooks(((Chest) this.ChestViewers.get(player)).getInventory()) <= 0) || (!((Chest) this.ChestViewers.get(player)).getInventory().contains(event.getCurrentItem()))) && (countWrittenBooks(((Chest) this.ChestViewers.get(player)).getInventory()) > 0)) {
                                 if (this.plugin.getConfig().getBoolean("useBookandQuill")) {
                                     this.plugin.Logger("UseBooksandQuill = true", "Debug");
                                     if ((!event.getCursor().getType().equals(Material.BOOK_AND_QUILL)) && (!event.getCurrentItem().getType().equals(Material.BOOK_AND_QUILL))) {
                                         this.plugin.Logger("!event.getCursor().getType().equals(Material.BOOK) || !event.getCurrentItem().getType().equals(Material.BOOK)", "Debug");
                                         this.plugin.Logger("Item is " + event.getCurrentItem().getType().name(), "Debug");
                                         this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.wrongItem." + this.plugin.config.language), "Error");
                                         event.setCancelled(true);
                                     }
                                 } else {
                                     this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.onebook." + this.plugin.config.language), "Error");
                                     event.setCancelled(true);
                                 }
                             } else if ((event.isShiftClick()) && (countWrittenBooks(((Chest) this.ChestViewers.get(player)).getInventory()) > 0)) {
                                 this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.onebook." + this.plugin.config.language), "Error");
                                 event.setCancelled(true);
                             }
                         } else {
                             this.plugin.Logger("Player " + player.getName() + " clicked on own Inventory!", "Debug");
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             this.plugin.getReportHandler().report(3320, "invClick doesnt work", e.getMessage(), "BookShopListener", e);
             System.out.println("[BookShop] Error: Uncatch Exeption!");
             try {
                 MetricsHandler.Error += 1;
             } catch (Exception e1) {
             }
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH)
     public void join(PlayerJoinEvent event) {
         try {
             if (!this.plugin.toggle) {
                 if ((this.plugin.PermissionsHandler.checkpermissionssilent(event.getPlayer(), "BookShop.admin"))
                         && plugin.getUpdate().isUpdateaviable()) {
                     this.plugin.PlayerLogger(event.getPlayer(), "installed BookShop version: " + plugin.getVersion() + ", latest version: " + plugin.getUpdate().getNewversion(), "Warning");
                     this.plugin.PlayerLogger(event.getPlayer(), "New BookShop update aviable: type \"/BookShop update\" to install!", "Warning");
                     if (!this.plugin.getConfig().getBoolean("installondownload")) {
                         this.plugin.PlayerLogger(event.getPlayer(), "Please edit the config.yml if you wish that the plugin updates itself atomatically!", "Warning");
                     }
                 }
                 
                 if ((!this.plugin.getServer().getOfflinePlayer(event.getPlayer().getName()).hasPlayedBefore())
                         && (this.plugin.getConfig().getBoolean("GiveBookToNewPlayers"))) {
                     ItemStack book = BookLoader.load(this.plugin, this.plugin.getConfig().getString("Book"));
                     if (event.getPlayer().getInventory().firstEmpty() != -1) {
                         if (book != null) {
                             event.getPlayer().getInventory().addItem(book);
                         } else {
                             this.plugin.Logger("Book wasnt found, so the new player gets no book!", "Error");
                             this.plugin.Logger("Please check your config.yml or type with a book in the hand '/bookshop setwelcomebook'!", "Error");
                         }
                     }
                 }
                 if (plugin.getGivebook_list().containsKey(event.getPlayer().getName())) {
                    for (BookFile file : plugin.getGivebook_list().get(event.getPlayer().getName())) {
                         if (event.getPlayer().getInventory().firstEmpty() != -1) {
                             event.getPlayer().getInventory().addItem(BookLoader.BookFileToBookHandler(file));
                             plugin.PlayerLogger(event.getPlayer(), "You were given a book by an admin!", "");
                            plugin.getGivebook_list().get(event.getPlayer().getName()).remove(file);
                            if (plugin.getGivebook_list().get(event.getPlayer().getName()).isEmpty()) {
                                 plugin.getGivebook_list().remove(event.getPlayer().getName());
                             }
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("[BookShop] Error: Uncatched Exeption!");
             this.plugin.getReportHandler().report(3321, "Player join throws error", e.getMessage(), "BookShopListener", e);
             try {
                 MetricsHandler.Error += 1;
             } catch (Exception e1) {
             }
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH)
     public void precommand(PlayerCommandPreprocessEvent event) {
         if ((!this.plugin.toggle)
                 && (event.getMessage().toLowerCase().startsWith("/BookShop".toLowerCase()))
                 && (this.plugin.config.debugfile)) {
             this.plugin.getLoggerUtility().log("Player: " + event.getPlayer().getName() + " command: " + event.getMessage());
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH)
     public void aendern(SignChangeEvent event) {
         if (!this.plugin.toggle) {
             if (this.plugin.config.debug) {
                 this.plugin.Logger("First Line " + event.getLine(0), "Debug");
             }
             if (event.getLine(0).equalsIgnoreCase(this.plugin.SHOP_configuration.getString("FirstLineOfEveryShop"))) {
                 this.createHandler.CreateBookShop(event);
             }
         }
     }
     
     public static Block getAttachedFace(org.bukkit.block.Sign sign) {
         return sign.getBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
     }
     
     private static boolean isCorrectSign(org.bukkit.block.Sign sign, Block block) {
         return (sign != null) && ((sign.getBlock().equals(block)) || (getAttachedFace(sign).equals(block)));
     }
     
     public static boolean isSign(Block block) {
         return block.getState() instanceof org.bukkit.block.Sign;
     }
     
     public org.bukkit.block.Sign findSignBook(Block block) {
         for (BlockFace bf : shopFaces) {
             Block faceBlock = block.getRelative(bf);
             if (isSign(faceBlock)) {
                 org.bukkit.block.Sign sign = (org.bukkit.block.Sign) faceBlock.getState();
                 if ((blockIsValid(sign)) && ((faceBlock.equals(block)) || (getAttachedFace(sign).equals(block)))) {
                     return sign;
                 }
             }
         }
         return null;
     }
     
     @EventHandler(priority = EventPriority.HIGH)
     public void onBreak(BlockBreakEvent event) {
         try {
             if (!this.plugin.toggle) {
                 Player p = event.getPlayer();
                 if (isSign(event.getBlock())) {
                     org.bukkit.block.Sign s = (org.bukkit.block.Sign) event.getBlock().getState();
                     String[] line = s.getLines();
                     this.plugin.Logger("Line 0: " + line[0], "Debug");
                     this.plugin.Logger("Sign dedected", "Debug");
                     if ((line[0].equalsIgnoreCase(this.plugin.SHOP_configuration.getString("FirstLineOfEveryShop")))
                             && (blockIsValid(line, "break", p))) {
                         if ((s.getLine(1).equalsIgnoreCase(plugin.getNameShortener().getShortName(p.getName()))) && (this.plugin.PermissionsHandler.checkpermissions(p, "BookShop.create"))) {
                             this.plugin.PlayerLogger(p, "Destroying BookShop!", "");
                             MTLocation loc = MTLocation.getMTLocationFromLocation(s.getLocation());
                             if (MetricsHandler.Shop.containsKey(loc)) {
                                 MetricsHandler.Shop.remove(loc);
                                 this.plugin.Logger("Removed Shop from list!", "Debug");
                             }
                             s.getBlock().getRelative(BlockFace.DOWN).breakNaturally();
                         } else if (this.plugin.PermissionsHandler.checkpermissions(p, "BookShop.create.admin")) {
                             this.plugin.PlayerLogger(p, "Destroying BookShop (Admin)!", "");
                             MTLocation loc = MTLocation.getMTLocationFromLocation(s.getLocation());
                             if ((line[1].equalsIgnoreCase("AdminShop"))
                                     && (MetricsHandler.AdminShop.containsKey(loc))) {
                                 MetricsHandler.AdminShop.remove(loc);
                                 this.plugin.Logger("Removed AdminShop from list!", "Debug");
                             }
                             
                             if (MetricsHandler.Shop.containsKey(loc)) {
                                 MetricsHandler.Shop.remove(loc);
                                 this.plugin.Logger("Removed Shop from list!", "Debug");
                             }
                             s.getBlock().getRelative(BlockFace.DOWN).breakNaturally();
                         } else {
                             event.setCancelled(true);
                             this.plugin.Logger("Event canceled! 3", "Debug");
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             this.plugin.getReportHandler().report(3322, "Breaking BookShop throws error", e.getMessage(), "BookShopListener", e);
             System.out.println("[BookShop] Error: Uncatch Exeption!");
             try {
                 MetricsHandler.Error += 1;
             } catch (Exception e1) {
             }
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH)
     public void onInteract(PlayerInteractEvent event) {
         try {
             this.interactHandler.InteracteventHandler(event);
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("[BookShop] Error: Uncatch Exeption!");
             this.plugin.getReportHandler().report(3323, "BookShop interact throws error", e.getMessage(), "BookShopListener", e);
             try {
                 MetricsHandler.Error += 1;
             } catch (Exception e1) {
             }
         }
     }
     
     public double getPrice(org.bukkit.block.Sign s, Player p, boolean BookandQuill) {
         double doubleline3 = 0.0D;
         try {
             doubleline3 = Double.parseDouble(s.getLine(3));
         } catch (Exception e) {
             if (this.plugin.getConfig().getBoolean("debug")) {
                 e.printStackTrace();
             }
             try {
                 String[] a = s.getLine(3).split(":");
                 if (this.plugin.getConfig().getBoolean("debug")) {
                     this.plugin.Logger("getPrice: a1: " + a[0] + " a2: " + a[1], "Debug");
                 }
                 double[] b = new double[2];
                 b[0] = Double.parseDouble(a[0]);
                 b[1] = Double.parseDouble(a[1]);
                 if (BookandQuill) {
                     doubleline3 = b[1];
                 } else {
                     doubleline3 = b[0];
                 }
             } catch (Exception e1) {
                 if (this.plugin.getConfig().getBoolean("debug")) {
                     e.printStackTrace();
                 }
             }
         }
         return doubleline3;
     }
     
     public double getPrice(String s, Player p, boolean BookandQuill) {
         double doubleline3 = 0.0D;
         try {
             doubleline3 = Double.parseDouble(s);
         } catch (Exception e) {
             if (this.plugin.getConfig().getBoolean("debug")) {
                 e.printStackTrace();
             }
             try {
                 String[] a = s.split(":");
                 if (this.plugin.getConfig().getBoolean("debug")) {
                     this.plugin.Logger("getPrice: a1: " + a[0] + " a2: " + a[1], "Debug");
                 }
                 double[] b = new double[2];
                 b[0] = Double.parseDouble(a[0]);
                 b[1] = Double.parseDouble(a[1]);
                 if (BookandQuill) {
                     doubleline3 = b[1];
                 } else {
                     doubleline3 = b[0];
                 }
             } catch (Exception e1) {
                 if (this.plugin.getConfig().getBoolean("debug")) {
                     e.printStackTrace();
                 }
             }
         }
         return doubleline3;
     }
     
     public boolean blockIsValid(String[] lines, String von, Player p) {
         boolean a = false;
         this.plugin.Logger("Checking if block is valid!", "Debug");
         double temp = 0.0D;
         double[] b = new double[2];
         try {
             temp = Double.parseDouble(lines[3]);
         } catch (Exception e) {
             try {
                 String[] a1 = lines[3].split(":");
                 b[0] = Double.parseDouble(a1[0]);
                 b[1] = Double.parseDouble(a1[1]);
             } catch (Exception e1) {
                 return false;
             }
         }
         if ((temp >= 0.0D) || ((b[0] >= 0.0D) && (b[1] >= 0.0D) && (b[0] >= b[1]))) {
             this.plugin.Logger("amount greater than 0", "Debug");
             a = true;
         } else {
             this.plugin.Logger("amount is smaller than 0! ", "Debug");
         }
         return a;
     }
     
     public boolean blockIsValid(org.bukkit.block.Sign sign) {
         boolean a = false;
         this.plugin.Logger("Checking if block is valid!", "Debug");
         double temp = 0.0D;
         double[] b = new double[2];
         try {
             temp = Double.parseDouble(sign.getLine(3));
         } catch (Exception e) {
             try {
                 String[] a1 = sign.getLine(3).split(":");
                 b[0] = Double.parseDouble(a1[0]);
                 b[1] = Double.parseDouble(a1[1]);
             } catch (Exception e1) {
                 return false;
             }
         }
         if ((temp >= 0.0D) || ((b[0] >= 0.0D) && (b[1] >= 0.0D) && (b[0] >= b[1]))) {
             this.plugin.Logger("amount greater than 0", "Debug");
             a = true;
         } else {
             this.plugin.Logger("amount is smaller than 0! ", "Debug");
         }
         return a;
     }
     
     public int countWrittenBooks(Inventory inv) {
         int a = 0;
         for (ItemStack i : inv.getContents()) {
             if ((i == null)
                     || (!i.getType().equals(Material.WRITTEN_BOOK))) {
                 continue;
             }
             this.plugin.Logger("Item " + i.getType().name() + " Slotid: " + inv.contains(i), "Debug");
             a++;
         }
         
         return a;
     }
 }
