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
 import java.util.Arrays;
 import java.util.Random;
 
 public class SignShopPlayerListener extends PlayerListener {
     private final SignShop plugin;
     private static Map<String, Location> mClicks  = new HashMap<String, Location>();
     private static Map<String,Location> mConfirms = new HashMap<String,Location>();
 
     private static Map<String,List> operations = new HashMap<String,List>();
 
     private byte takePlayerMoney = 1;
     private byte givePlayerMoney = 2;
     private byte takePlayerItems = 3;
     private byte givePlayerItems = 4;
     private byte takeOwnerMoney = 5;
     private byte giveOwnerMoney = 6;
     private byte takeShopItems = 7;
     private byte giveShopItems = 8;
     private byte givePlayerRandomItem = 10;
     private byte playerIsOp = 11;
     private byte setDayTime = 12;
     private byte setNightTime = 13;
     private byte setRaining = 14;
     private byte setClearSkies = 16;
     private byte setRedstoneOn = 17;
     private byte setRedstoneOff = 18;
     private byte setRedStoneOnTemp = 19;
     private byte toggleRedstone = 20;
     private byte usesChest = 21;
     private byte usesLever = 22;
 
     public SignShopPlayerListener(SignShop instance){
         this.plugin = instance;
 
         operations.put("Buy",Arrays.asList(takePlayerMoney,takeShopItems,giveOwnerMoney,givePlayerItems,usesChest));
         operations.put("Sell",Arrays.asList(takePlayerItems,takeOwnerMoney,giveShopItems,givePlayerMoney,usesChest));
         operations.put("Donate",Arrays.asList(takePlayerItems,giveShopItems,usesChest));
         operations.put("Slot",Arrays.asList(takePlayerMoney,giveOwnerMoney,givePlayerRandomItem,usesChest));
        operations.put("iSell",Arrays.asList(givePlayerMoney,takePlayerItems,playerIsOp,usesChest));
        operations.put("iBuy",Arrays.asList(takePlayerMoney,givePlayerItems,playerIsOp,usesChest));
         operations.put("gBuy",Arrays.asList(takePlayerMoney,givePlayerItems,takeShopItems,playerIsOp,usesChest));
         operations.put("gSell",Arrays.asList(givePlayerMoney,takePlayerItems,giveShopItems,playerIsOp,usesChest));
         operations.put("Day",Arrays.asList(takePlayerMoney,setDayTime,playerIsOp));
         operations.put("Night",Arrays.asList(takePlayerMoney,setNightTime,playerIsOp));
         operations.put("Rain",Arrays.asList(takePlayerMoney,setRaining,playerIsOp));
         operations.put("ClearSkies",Arrays.asList(takePlayerMoney,setClearSkies,playerIsOp));
         operations.put("DeviceOn",Arrays.asList(takePlayerMoney,setRedstoneOn,usesLever));
         operations.put("DeviceOff",Arrays.asList(takePlayerMoney,setRedstoneOff,usesLever));
         operations.put("DeviceToggle",Arrays.asList(takePlayerMoney,toggleRedstone,usesLever));
         operations.put("Device",Arrays.asList(takePlayerMoney,setRedStoneOnTemp,usesLever));
     }
 
     private String getOperation(String sSignOperation){
         if(sSignOperation.length() < 3){
             return "";
         }
         return sSignOperation.substring(1,sSignOperation.length()-1);
     }
 
     private String getMessage(String sType,String sOperation,String sItems,float fPrice,String sCustomer,String sOwner){
         return plugin.Messages.get(sType).get(sOperation)
             .replace("\\!","!")
             .replace("!price", plugin.iConomy.format(fPrice))
             .replace("!items", sItems)
             .replace("!customer", sCustomer)
             .replace("!owner", sOwner);
     }
 
     //msg a player object
     private void msg(Player player,String msg){
         player.sendMessage(ChatColor.GOLD+"[SignShop] "+ChatColor.WHITE+msg);
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
         if(event.getClickedBlock() == null
         || event.isCancelled()){
             return;
         }
 
         Block bClicked = event.getClickedBlock();
 //clicked a sign with redstone
         if(event.getItem() != null && event.getItem().getType() == Material.REDSTONE){
             if(bClicked.getType() == Material.SIGN_POST
             || bClicked.getType() == Material.WALL_SIGN){
                 // verify this isn't a shop already
                 if(plugin.Storage.getSeller(event.getClickedBlock().getLocation()) != null){
                     return;
                 }
                 
                 String[] sLines = ((Sign) bClicked.getState()).getLines();
 
                 String sOperation = getOperation(sLines[0]);
 
                 if(!operations.containsKey(sOperation)){
                     return;
                 }
 
                 List operation = operations.get(sOperation);
 
 //op operation - prosaic, but it works and it's tidy.
                 if(operation.contains(playerIsOp)){
                     if(plugin.USE_PERMISSIONS){
                         if(!plugin.permissionHandler.has(event.getPlayer(),"SignShop.Admin."+sOperation)){
                             msg(event.getPlayer(),"You don't have permission to create this sign!");
                             return;
                         }
                     }else{
                         if(!event.getPlayer().isOp()){
                             msg(event.getPlayer(),"You don't have permission to create this sign!");
                             return;
                         }
                     }
                 }else{
                     if(plugin.USE_PERMISSIONS && !plugin.permissionHandler.has(event.getPlayer(),"SignShop.Signs."+sOperation)){
                         msg(event.getPlayer(),"You don't have permission to create this sign!");
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
 
                 //does this sign have a chest/lever counterpart?
                 if(operation.contains(usesChest) || operation.contains(usesLever)){
                     mClicks.put(event.getPlayer().getName(),event.getClickedBlock().getLocation());
 
                     msg(event.getPlayer(),"Sign location stored!");
                 }else{
                     plugin.Storage.addSeller(event.getPlayer().getName(),event.getClickedBlock(),event.getClickedBlock(),new ItemStack[]{new ItemStack(Material.AIR,1)});
                     
                     msg(event.getPlayer(),getMessage("setup",sOperation,"",fPrice,"",event.getPlayer().getName()));
                 }
 
                 return;
 //left clicked a chest and has already clicked a sign
             }else if(event.getAction() == Action.LEFT_CLICK_BLOCK
             && (event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.LEVER)
             && mClicks.containsKey(event.getPlayer().getName())){
 
                 Block bSign = mClicks.get(event.getPlayer().getName()).getBlock();
 
                 String sOperation = getOperation(((Sign) bSign.getState()).getLine(0));
                 List operation = operations.get(sOperation);
 
                 String sPrice = ((Sign) bSign.getState()).getLine(3);
                 
 //verify the operation
                 if(!operations.containsKey(sOperation)){
                     msg(event.getPlayer(),"The sign you clicked doesnt have a valid operation!");
                     return;
                 }
 
 //op operation - prosaic, but it works and it's tidy.
                 if(operation.contains(playerIsOp)){
                     if(plugin.USE_PERMISSIONS){
                         if(!plugin.permissionHandler.has(event.getPlayer(),"SignShop.Admin."+sOperation)){
                             msg(event.getPlayer(),"You don't have permission to create this sign!");
                             return;
                         }
                     }else{
                         if(!event.getPlayer().isOp()){
                             msg(event.getPlayer(),"You don't have permission to create this sign!");
                             return;
                         }
                     }
                 }else{
                     if(plugin.USE_PERMISSIONS && !plugin.permissionHandler.has(event.getPlayer(),"SignShop.Signs."+sOperation)){
                         msg(event.getPlayer(),"You don't have permission to create this sign!");
                         return;
                     }
                 }
 
 //verify the price
                 float fPrice = 0.0f;
                 try{
                     fPrice = Float.parseFloat(sPrice);
                 }
                 catch(NumberFormatException nFE){}
                 if(fPrice < 0.0f){
                     fPrice = 0.0f;
                 }
 
 //take a different route for redstone events
                 if(operation.contains(usesLever) && event.getClickedBlock().getType() == Material.LEVER){
                     msg(event.getPlayer(),getMessage("setup",sOperation,"",fPrice,"",""));
                     
                     plugin.Storage.addSeller(event.getPlayer().getName(),bSign,event.getClickedBlock(),new ItemStack[]{new ItemStack(Material.AIR,1)});
 
                     mClicks.remove(event.getPlayer().getName());
 
                     return;
                 }
 
 //chest items
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
 
 //make sure the chest wasn't empty, if dealing with an operation that uses items
                 if(operation.contains(usesChest)){
                     if(isChestItems.length == 0){
                         msg(event.getPlayer(),"Shop is empty!");
                         return;
                     }
                 }
 
 //send setup msg, and setup seller
                 String sItems = "";
                 for(ItemStack item : isChestItems){
                     sItems += item.getAmount()+" "+stringFormat(item.getType())+", ";
                 }
                 sItems = sItems.substring(0,sItems.length()-2);
 
                 msg(event.getPlayer(),getMessage("setup",sOperation,sItems,fPrice,"",event.getPlayer().getName()));
 
                 plugin.Storage.addSeller(event.getPlayer().getName(),bSign,event.getClickedBlock(),isChestItems);
 
                 mClicks.remove(event.getPlayer().getName());
 
                 return;
             }
         }
         else if(bClicked.getType() == Material.SIGN_POST || bClicked.getType() == Material.WALL_SIGN){
             Sign sbSign = (Sign) bClicked.getState();
             String sOperation = getOperation(sbSign.getLine(0));
 
             if(!operations.containsKey(sOperation)){
                 return;
             }
 
             List operation = operations.get(sOperation);
 
 
             Seller seller = plugin.Storage.getSeller(bClicked.getLocation());
 
 //verify seller at this location
             if(seller == null){
                 return;
             }
 
 //setup price
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
                     msg(event.getPlayer(),"You don't have "+plugin.iConomy.format(fPrice)+" to pay!");
 
                     return;
                 }
             }
 
             if(operation.contains(takeOwnerMoney)){
                 if(!plugin.iConomy.getAccount(seller.owner).getHoldings().hasEnough(fPrice)){
                     msg(event.getPlayer(),"The shop doesn't have "+plugin.iConomy.format(fPrice)+" to pay you!");
 
                     return;
                 }
             }
 
 //Make sure the items are there
             Chest cbChest = null;
             ItemStack[] isChestItems = null;
             ItemStack[] isChestItemsBackup = null;
 
             if(operation.contains(usesChest)){
                 if(seller.getChest().getType() != Material.CHEST){
                     msg(event.getPlayer(),"This shop appears to have gone out of business!");
 
                     Location lClicked = bClicked.getLocation();
 
                     msg(seller.owner,ChatColor.RED+"Your shop at (X:"+lClicked.getBlockX()+",Y:"+lClicked.getBlockY()+",Z:"+lClicked.getBlockZ()+") is missing it's chest!");
                     
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
             }
 
             ItemStack[] isPlayerItems = event.getPlayer().getInventory().getContents();
             ItemStack[] isPlayerItemsBackup = new ItemStack[isPlayerItems.length];
             
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
             HashMap<Integer,ItemStack> iiItemsLeftover;
 
             if(operation.contains(takePlayerItems)){
                 iiItemsLeftover = event.getPlayer().getInventory().removeItem(isItems);
 
                 if(!iiItemsLeftover.isEmpty()){
                     //reset chest inventory
 
                     event.getPlayer().getInventory().setContents(isPlayerItemsBackup);
 
                     msg(event.getPlayer(),"You don't have the items! ("+sItems+")");
 
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
 
                     msg(event.getPlayer(),"This shop is out of stock!");
 
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
 
                     msg(event.getPlayer(),"This shop is overstocked!");
 
                     return;
                 }
                 //every operation step needs to be self cleaning
                 cbChest.getInventory().setContents(isChestItemsBackup);
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
             
             if(operation.contains(givePlayerMoney))
                 plugin.iConomy.getAccount(event.getPlayer().getName()).getHoldings().add(fPrice);
             if(operation.contains(takePlayerMoney))
                 plugin.iConomy.getAccount(event.getPlayer().getName()).getHoldings().subtract(fPrice);
 
             if(operation.contains(giveOwnerMoney)) 
                 plugin.iConomy.getAccount(seller.owner).getHoldings().add(fPrice);
             if(operation.contains(takeOwnerMoney)) 
                 plugin.iConomy.getAccount(seller.owner).getHoldings().subtract(fPrice);
 
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
             
             if(operation.contains(setDayTime)){
                 event.getPlayer().getWorld().setTime(0);
             }else if(operation.contains(setNightTime)){
                 event.getPlayer().getWorld().setTime(13000);
             }
             
             if(operation.contains(setRaining)){
                 event.getPlayer().getWorld().setStorm(true);
                 event.getPlayer().getWorld().setThundering(true);
             }else if(operation.contains(setClearSkies)){
                 event.getPlayer().getWorld().setStorm(false);
                 event.getPlayer().getWorld().setThundering(false);
             }
             
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
 
                     msg(event.getPlayer(),"This shop is out of stock!");
 
                     return;
                 }
 
                 event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(),isRandom);
 
                 msg(event.getPlayer(),"You got "+isRandom.getAmount()+" "+stringFormat(isRandom.getType())+"!");
             }
 
             if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
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
 
                 if((iData&0x08) != 0x08){
                     iData^=0x08;//send power off
                     blockToChange.setData((byte) iData);
                 }
             }
         }
     }
 }
