 package Ne0nx3r0.SignShop;
 
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import java.util.Map;
 import java.util.HashMap;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.block.Chest;
 import org.bukkit.inventory.ItemStack;
 import java.util.List;
 import java.util.ArrayList;
 import org.bukkit.Location;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import org.bukkit.Bukkit;
 import org.bukkit.event.block.Action;
 import java.util.Random;
 
public class SignShopPlayerListener extends PlayerListener {
     private final SignShop plugin;
     private static Map<String, Location> mClicks  = new HashMap<String,Location>();
     private static Map<String,Location> mConfirms = new HashMap<String,Location>();
 
     private int takePlayerMoney = 1;
     private int givePlayerMoney = 2;
     private int takePlayerItems = 3;
     private int givePlayerItems = 4;
     private int takeOwnerMoney = 5;
     private int giveOwnerMoney = 6;
     private int takeShopItems = 7;
     private int giveShopItems = 8;
     private int givePlayerRandomItem = 10;
     private int playerIsOp = 11;
     private int setDayTime = 12;
     private int setNightTime = 13;
     private int setRaining = 14;
     private int setClearSkies = 16;
     private int setRedstoneOn = 17;
     private int setRedstoneOff = 18;
     private int setRedStoneOnTemp = 19;
     private int toggleRedstone = 20;
     private int usesChest = 21;
     private int usesLever = 22;
     private int healPlayer = 23;
     private int repairPlayerHeldItem = 24;
 
 
     public SignShopPlayerListener(SignShop instance){
         this.plugin = instance;
     }
 
     private String getOperation(String sSignOperation){
         if(sSignOperation.length() < 3){
             return "";
         }
         return sSignOperation.substring(1,sSignOperation.length()-1);
     }
 
     private String getMessage(String sType,String sOperation,String sItems,float fPrice,String sCustomer,String sOwner){
         if(!plugin.Messages.get(sType).containsKey(sOperation)){
             return "";
         }
         return plugin.Messages.get(sType).get(sOperation)
             .replace("\\!","!")
             .replace("!price", plugin.iConomy.format(fPrice))
             .replace("!items", sItems)
             .replace("!customer", sCustomer)
             .replace("!owner", sOwner);
     }
 
     //msg a player object
     private void msg(Player player,String msg){
         if(msg == null || msg.equals("")){
             return;
         }
         player.sendMessage(ChatColor.GOLD+"[SignShop] "+ChatColor.WHITE+msg);
     }
 
     private void msg(List<Player> players,String msg){
         for(Player player : players){
             msg(player,msg);
         }
     }
 
     //look up a player by player.getName()
     private boolean msg(String sPlayer,String msg){
         Player[] players = Bukkit.getServer().getOnlinePlayers();
 
         for(Player player : players){
             if(player.getName().equals(sPlayer)){
                 msg(player,msg);
                 return true;
             }
         }
         return false;
     }
 
     private static String stringFormat(Material material){
         String sMaterial = material.name().replace("_"," ");
         Pattern p = Pattern.compile("(^|\\W)([a-z])");
         Matcher m = p.matcher(sMaterial.toLowerCase());
         StringBuffer sb = new StringBuffer(sMaterial.length());
 
         while(m.find()){
             m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
         }
 
         m.appendTail(sb);
 
         return sb.toString();
     }
 
     @Override
     public void onPlayerInteract(PlayerInteractEvent event){
 // Respect protection plugins
         if(event.getClickedBlock() == null
         || event.isCancelled()){
             return;
         }
 
         Block bClicked = event.getClickedBlock();
 // Clicked a sign with redstone
         if(event.getItem() != null && event.getItem().getType() == Material.REDSTONE){
             if(bClicked.getType() == Material.SIGN_POST
             || bClicked.getType() == Material.WALL_SIGN){
 // Verify this isn't a shop already
                 if(plugin.Storage.getSeller(event.getClickedBlock().getLocation()) != null){
                     return;
                 }
                
//TODO: auto set operation names to title case to avoid case issues
//TODO: save operation with signs to avoid sign changing issues
                 String[] sLines = ((Sign) bClicked.getState()).getLines();
                 String sOperation = getOperation(sLines[0]);
 
 // Not a valid operation
                 if(!plugin.Operations.containsKey(sOperation)){
                     return;
                 }
 
                 List operation = plugin.Operations.get(sOperation);
 
 // OP / permissions check - prosaic, but it works and it's tidy.
                 if(operation.contains(playerIsOp)){
                     if(plugin.USE_PERMISSIONS){
                         if(!plugin.permissionHandler.has(event.getPlayer(),"SignShop.Admin."+sOperation)){
                             msg(event.getPlayer(),plugin.Errors.get("no_permission"));
                             return;
                         }
                     }else{
                         if(!event.getPlayer().isOp()){
                             msg(event.getPlayer(),plugin.Errors.get("no_permission"));
                             return;
                         }
                     }
                 }else{
                     if(plugin.USE_PERMISSIONS && !plugin.permissionHandler.has(event.getPlayer(),"SignShop.Signs."+sOperation)){
                         msg(event.getPlayer(),plugin.Errors.get("no_permission"));
                         return;
                     }
                 }
 
                 float fPrice = 0.0f;
                 try{
                     fPrice = Float.parseFloat(sLines[3]);
                 }
                 catch(NumberFormatException nFE){}
                 if(fPrice < 0.0f){
                     fPrice = 0.0f;
                 }
 
 // Does this sign have a chest/lever counterpart?
                 if(operation.contains(usesChest) || operation.contains(usesLever)){
                     mClicks.put(event.getPlayer().getName(),event.getClickedBlock().getLocation());
 
                     msg(event.getPlayer(),plugin.Errors.get("sign_location_stored"));
 
                     return;
 // Standalone operation
                 }else{
                     plugin.Storage.addSeller(event.getPlayer().getName(),event.getClickedBlock(),event.getClickedBlock(),new ItemStack[]{new ItemStack(Material.DIRT,1)});
                     
                     msg(event.getPlayer(),getMessage("setup",sOperation,"",fPrice,"",event.getPlayer().getName()));
 
                     return;
                 }
 // Left clicked a chest and has already clicked a sign
             }else if(event.getAction() == Action.LEFT_CLICK_BLOCK
             && (event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.LEVER)
             && mClicks.containsKey(event.getPlayer().getName())){
 
                 Block bSign = mClicks.get(event.getPlayer().getName()).getBlock();
 
                 String sOperation = getOperation(((Sign) bSign.getState()).getLine(0));
                 List operation = plugin.Operations.get(sOperation);
 
                 String sPrice = ((Sign) bSign.getState()).getLine(3);
                 
 // Verify the operation
                 if(!plugin.Operations.containsKey(sOperation)){
                     msg(event.getPlayer(),plugin.Errors.get("invalid_operation"));
                     return;
                 }
 
 // OP/Permissions check - prosaic, but it works and it's tidy.
                 if(operation.contains(playerIsOp)){
                     if(plugin.USE_PERMISSIONS){
                         if(!plugin.permissionHandler.has(event.getPlayer(),"SignShop.Admin."+sOperation)){
                             msg(event.getPlayer(),plugin.Errors.get("no_permission"));
                             return;
                         }
                     }else{
                         if(!event.getPlayer().isOp()){
                             msg(event.getPlayer(),plugin.Errors.get("no_permission"));
                             return;
                         }
                     }
                 }else{
                     if(plugin.USE_PERMISSIONS && !plugin.permissionHandler.has(event.getPlayer(),"SignShop.Signs."+sOperation)){
                         msg(event.getPlayer(),plugin.Errors.get("no_permission"));
                         return;
                     }
                 }
 
 // Verify the price
                 float fPrice = 0.0f;
                 try{
                     fPrice = Float.parseFloat(sPrice);
                 }
                 catch(NumberFormatException nFE){}
                 if(fPrice < 0.0f){
                     fPrice = 0.0f;
                 }
 
 // Redstone operation
                 if(operation.contains(usesLever) && event.getClickedBlock().getType() == Material.LEVER){
                     msg(event.getPlayer(),getMessage("setup",sOperation,"",fPrice,"",""));
 
                     plugin.Storage.addSeller(event.getPlayer().getName(),bSign,event.getClickedBlock(),new ItemStack[]{new ItemStack(Material.DIRT,1)});
 
                     mClicks.remove(event.getPlayer().getName());
 // Chest operation
                 }else if(operation.contains(usesChest) && event.getClickedBlock().getType() == Material.CHEST){
     // Chest items
                     Chest cbChest = (Chest) event.getClickedBlock().getState();
                     ItemStack[] isChestItems = cbChest.getInventory().getContents();
 
                     //remove extra values
                     List<ItemStack> tempItems = new ArrayList<ItemStack>();
                     for(ItemStack item : isChestItems) {
                         if(item != null && item.getAmount() > 0) {
                             tempItems.add(item);
                         }
                     }
                     isChestItems = tempItems.toArray(new ItemStack[tempItems.size()]);
 
     // Make sure the chest wasn't empty, if dealing with an operation that uses items
                     if(operation.contains(usesChest)){
                         if(isChestItems.length == 0){
                             msg(event.getPlayer(),plugin.Errors.get("chest_empty"));
                             return;
                         }
                     }
 
     // Send setup msg, and setup seller
                     String sItems = "";
                     for(ItemStack item : isChestItems){
                         sItems += item.getAmount()+" "+stringFormat(item.getType())+", ";
                     }
                     sItems = sItems.substring(0,sItems.length()-2);
 
                     msg(event.getPlayer(),getMessage("setup",sOperation,sItems,fPrice,"",event.getPlayer().getName()));
 
                     plugin.Storage.addSeller(event.getPlayer().getName(),bSign,event.getClickedBlock(),isChestItems);
 
                     mClicks.remove(event.getPlayer().getName());
                 }
 
                 return;
             }
         }
 // Clicked on a sign, might be a signshop.
         else if(bClicked.getType() == Material.SIGN_POST || bClicked.getType() == Material.WALL_SIGN){
             Sign sbSign = (Sign) bClicked.getState();
             String sOperation = getOperation(sbSign.getLine(0));
 
 // It wasn't a sign shop
             if(!plugin.Operations.containsKey(sOperation)){
                 return;
             }
 
             List operation = plugin.Operations.get(sOperation);
 
             Seller seller = plugin.Storage.getSeller(bClicked.getLocation());
 
 // Verify seller at this location
             if(seller == null){
                 return;
             }
 
 // Verify price
             float fPrice = 0.0f;
 
             try{
                 fPrice = Float.parseFloat(((Sign) bClicked.getState()).getLine(3));
             }catch(NumberFormatException nfe){}
 
             if(fPrice < 0.0f){
                 fPrice = 0.0f;
             }
 
 //setup items
             ItemStack[] isItems = seller.getItems();
 
             String sItems = "";
             for(ItemStack item: isItems){
                 sItems += item.getAmount()+" "+stringFormat(item.getType())+", ";
             }
             sItems = sItems.substring(0,sItems.length()-2);
 
 //Make sure the money is there
             if(operation.contains(takePlayerMoney)){
                 if(!plugin.iConomy.getAccount(event.getPlayer().getName()).getHoldings().hasEnough(fPrice)){
                     msg(event.getPlayer(),plugin.Errors.get("no_player_money").replace("!price",plugin.iConomy.format(fPrice)));
 
                     return;
                 }
             }
 
             if(operation.contains(takeOwnerMoney)){
                 if(!plugin.iConomy.getAccount(seller.owner).getHoldings().hasEnough(fPrice)){
                     msg(event.getPlayer(),plugin.Errors.get("no_shop_money").replace("!price",plugin.iConomy.format(fPrice)));
 
                     return;
                 }
             }
 
 //Make sure the items are there
             // Due to the standalone nature of the statements these need to be defined here
             Chest cbChest = null;
             ItemStack[] isChestItems = null;
             ItemStack[] isChestItemsBackup = null;
 
             ItemStack[] isPlayerItems = event.getPlayer().getInventory().getContents();
             ItemStack[] isPlayerItemsBackup = new ItemStack[isPlayerItems.length];
 
             HashMap<Integer,ItemStack> iiItemsLeftover;
 
             if(operation.contains(usesChest)){
                 if(seller.getChest().getType() != Material.CHEST){
                     msg(event.getPlayer(),plugin.Errors.get("out_of_business"));
 
                     Location lClicked = bClicked.getLocation();
 
                     return;
                 }
 
                 cbChest = (Chest) seller.getChest().getState();
                 isChestItems = cbChest.getInventory().getContents();
                 isChestItemsBackup = new ItemStack[isChestItems.length];
                 for(int i=0;i<isChestItems.length;i++){
                     if(isChestItems[i] != null){
                         isChestItemsBackup[i] = new ItemStack(
                             isChestItems[i].getType(),
                             isChestItems[i].getAmount(),
                             isChestItems[i].getDurability()
                         );
 
                         if(isChestItems[i].getData() != null){
                             isChestItemsBackup[i].setData(isChestItems[i].getData());
                         }
                     }
                 }
 
                 for(int i=0;i<isPlayerItems.length;i++){
                     if(isPlayerItems[i] != null){
                         isPlayerItemsBackup[i] = new ItemStack(
                             isPlayerItems[i].getType(),
                             isPlayerItems[i].getAmount(),
                             isPlayerItems[i].getDurability()
                         );
 
                         if(isPlayerItems[i].getData() != null){
                             isPlayerItemsBackup[i].setData(isPlayerItems[i].getData());
                         }
                     }
                 }
 
                 if(operation.contains(takePlayerItems)){
                     iiItemsLeftover = event.getPlayer().getInventory().removeItem(isItems);
 
                     if(!iiItemsLeftover.isEmpty()){
                         //reset chest inventory
 
                         event.getPlayer().getInventory().setContents(isPlayerItemsBackup);
 
                         msg(event.getPlayer(),plugin.Errors.get("player_doesnt_have_items").replace("!items", sItems));
 
                         event.setCancelled(true);
 
                         return;
                     }
                     //every operation step needs to be self cleaning
                     event.getPlayer().getInventory().setContents(isPlayerItemsBackup);
 
                 }
 
                 if(operation.contains(takeShopItems)){
                     iiItemsLeftover = cbChest.getInventory().removeItem(isItems);
 
                     if(!iiItemsLeftover.isEmpty()){
                         //reset chest inventory
                         cbChest.getInventory().setContents(isChestItemsBackup);
 
                         msg(event.getPlayer(),plugin.Errors.get("out_of_stock"));
 
                         return;
                     }
                     //every operation step needs to be self cleaning
                     cbChest.getInventory().setContents(isChestItemsBackup);
                 }
 
     //Make sure the shop has room
                 if(operation.contains(giveShopItems)){
                     iiItemsLeftover = cbChest.getInventory().addItem(isItems);
 
                     if(!(iiItemsLeftover).isEmpty()){
                         //reset chest inventory
                         cbChest.getInventory().setContents(isChestItemsBackup);
 
                         msg(event.getPlayer(),plugin.Errors.get("overstocked"));
 
                         return;
                     }
                     //every operation step needs to be self cleaning
                     cbChest.getInventory().setContents(isChestItemsBackup);
                 }
             }
 
 //Make sure the item can be repaired
      if(operation.contains(repairPlayerHeldItem)){
         if(event.getItem() == null){
             msg(event.getPlayer(),plugin.Errors.get("no_item_to_repair"));
 
             return;
         }else if(event.getItem().getType().getMaxDurability() < 30){
             msg(event.getPlayer(),plugin.Errors.get("invalid_item_to_repair"));
 
             return;
         }
      }
 
 //have they seen the confirm message? (right click skips)
             if(event.getAction() == Action.LEFT_CLICK_BLOCK
             &&(!mConfirms.containsKey(event.getPlayer().getName())
                 || mConfirms.get(event.getPlayer().getName()).getBlock() != bClicked)
             ){
                 msg(event.getPlayer(),getMessage("confirm",sOperation,sItems,fPrice,event.getPlayer().getName(),seller.owner));
 
                 mConfirms.put(event.getPlayer().getName(),bClicked.getLocation());
 
                 return;
             }
             
             mConfirms.remove(event.getPlayer().getName());
 
 // Money giving/taking
             if(operation.contains(givePlayerMoney))
                 plugin.iConomy.getAccount(event.getPlayer().getName()).getHoldings().add(fPrice);
             if(operation.contains(takePlayerMoney))
                 plugin.iConomy.getAccount(event.getPlayer().getName()).getHoldings().subtract(fPrice);
 
             if(operation.contains(giveOwnerMoney)) 
                 plugin.iConomy.getAccount(seller.owner).getHoldings().add(fPrice);
             if(operation.contains(takeOwnerMoney)) 
                 plugin.iConomy.getAccount(seller.owner).getHoldings().subtract(fPrice);
 
 // Item giving/taking
             if(operation.contains(givePlayerItems)){
                 for(ItemStack item : isItems){
                    event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(),item);
                 }
             }
             if(operation.contains(takePlayerItems)){
                 event.getPlayer().getInventory().removeItem(isItems);
             }
 
             if(operation.contains(giveShopItems)){
                 cbChest.getInventory().addItem(isItems);
             }
             if(operation.contains(takeShopItems)){
                 cbChest.getInventory().removeItem(isItems);
             }
 
 // Health
             if(operation.contains(healPlayer)){
                 event.getPlayer().setHealth(20);
             }
 
 // Item Repair
             if(operation.contains(repairPlayerHeldItem)){
                 event.getPlayer().getItemInHand().setDurability((short) 0);
             }
 
 
 // Weather Operations
             if(operation.contains(setDayTime)){
                 event.getPlayer().getWorld().setTime(0);
 
                 msg(event.getPlayer().getWorld().getPlayers(),plugin.Errors.get("made_day").replace("!player",event.getPlayer().getDisplayName()));
 
             }else if(operation.contains(setNightTime)){
                 event.getPlayer().getWorld().setTime(13000);
 
                 msg(event.getPlayer().getWorld().getPlayers(),plugin.Errors.get("made_night").replace("!player",event.getPlayer().getDisplayName()));
             }
             
             if(operation.contains(setRaining)){
                 event.getPlayer().getWorld().setStorm(true);
                 event.getPlayer().getWorld().setThundering(true);
 
                 msg(event.getPlayer().getWorld().getPlayers(),plugin.Errors.get("made_rain").replace("!player",event.getPlayer().getDisplayName()));
             }else if(operation.contains(setClearSkies)){
                 event.getPlayer().getWorld().setStorm(false);
                 event.getPlayer().getWorld().setThundering(false);
 
                 msg(event.getPlayer().getWorld().getPlayers(),plugin.Errors.get("made_clear_skies").replace("!player",event.getPlayer().getDisplayName()));
             }
 
 // Redstone operations
             if(operation.contains(setRedstoneOn)){
                 Block bLever = seller.getChest();
 
                 if(bLever.getType() == Material.LEVER){
                     int iData = (int) bLever.getData();
 
                     if((iData&0x08) != 0x08){
                         iData|=0x08;//send power on
                         bLever.setData((byte) iData);
                     }
                 }
             }else if(operation.contains(setRedstoneOff)){
                 Block bLever = seller.getChest();
 
                 if(bLever.getType() == Material.LEVER){
                     int iData = (int) bLever.getData();
 
                     if((iData&0x08) != 0x08){
                         iData^=0x08;//send power off
                         bLever.setData((byte) iData);
                     }
                 }
             }else if(operation.contains(setRedStoneOnTemp)){
                 Block bLever = seller.getChest();
 
                 if(bLever.getType() == Material.LEVER){
                     int iData = (int) bLever.getData();
 
                     if((iData&0x08) != 0x08){
                         iData|=0x08;//send power on
                         bLever.setData((byte) iData);
                     }
 
                     plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,new lagSetter(bLever),10*20);
                 }
             }else if(operation.contains(toggleRedstone)){
                 Block bLever = seller.getChest();
 
                 if(bLever.getType() == Material.LEVER){
                     int iData = (int) bLever.getData();
 
                     if((iData&0x08) != 0x08){
                         iData|=0x08;//send power on
                         bLever.setData((byte) iData);
                     }else if((iData&0x08) == 0x08){
                         iData^=0x08;//send power off
                         bLever.setData((byte) iData);
                     }
                 }
             }
 
             if(operation.contains(givePlayerRandomItem)){
                 ItemStack isRandom = isItems[(new Random()).nextInt(isItems.length)];
 
                 iiItemsLeftover = cbChest.getInventory().removeItem(isRandom);
 
                 if(!iiItemsLeftover.isEmpty()){
                     //reset chest inventory
                     cbChest.getInventory().setContents(isChestItemsBackup);
 
                     msg(event.getPlayer(),plugin.Errors.get("out_of_stock"));
 
                     return;
                 }
 
                 event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(),isRandom);
                 
                 sItems = isRandom.getType().name().toLowerCase().replace("_"," ");
             }
 
             if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
                 if(event.getItem() != null){
                     event.setCancelled(true);
                 }
                 //kludge
                 event.getPlayer().updateInventory();
             }
 
             msg(event.getPlayer(),getMessage("transaction",sOperation,sItems,fPrice,event.getPlayer().getName(),seller.owner));
             msg(seller.owner,ChatColor.GREEN+getMessage("transaction_owner",sOperation,sItems,fPrice,event.getPlayer().getName(),seller.owner));
         }
     }
 
     private static class lagSetter implements Runnable{
         private final Block blockToChange;
 
         lagSetter(Block blockToChange){
             this.blockToChange = blockToChange;
         }
 
         public void run(){
             if(blockToChange.getType() == Material.LEVER){
                 int iData = (int) blockToChange.getData();
 
                 if((iData&0x08) == 0x08){
                     iData^=0x08;//send power off
                     blockToChange.setData((byte) iData);
                 }
             }
         }
     }
 }
