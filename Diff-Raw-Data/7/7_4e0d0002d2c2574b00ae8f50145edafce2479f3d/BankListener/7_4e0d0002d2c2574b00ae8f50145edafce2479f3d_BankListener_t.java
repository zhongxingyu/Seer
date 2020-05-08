 package cc.thedudeguy.xpinthejar.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import cc.thedudeguy.xpinthejar.XPInTheJar;
 import cc.thedudeguy.xpinthejar.databases.Bank;
 import cc.thedudeguy.xpinthejar.util.Debug;
 
 public class BankListener implements Listener {
 
     /**
      * Handle returning the xp in the bank block to the user who broke the block, so it is not lost forever
      *
      * @param event
      */
     @EventHandler
     public void onBlockDestoy(BlockBreakEvent event) {
         if (!event.getBlock().getType().equals(Material.DIAMOND_BLOCK)) {
             return;
         }
 
         Block bankBlock = event.getBlock();
         Player player = event.getPlayer();
 
         if (!Bank.hasBank(bankBlock)) {
             // no bank do nothing.
             return;
         }
 
         if(!player.hasPermission("xpjar.bank.destroy")) {
             if(XPInTheJar.instance.spoutEnabled && ((SpoutPlayer)event.getPlayer()).isSpoutCraftEnabled()) {
                 ((SpoutPlayer)player).sendNotification("Permission", "You don't have the permission to destroy a bank", Material.GLASS_BOTTLE);
             } else {
                 player.sendMessage(ChatColor.RED + "You don't have the permission to destroy a bank");
             }
             event.setCancelled(true);
             return;
         }
 
         int balance = Bank.getBankBalance(bankBlock);
 
         if (balance <= 0) {
             player.sendMessage("The bank was empty.");
         } else {
             player.giveExp(balance);
             if(XPInTheJar.instance.spoutEnabled && ((SpoutPlayer)event.getPlayer()).isSpoutCraftEnabled()) {
                 ((SpoutPlayer)player).sendNotification("Exp Bank Destroyed", "Retrieved " + String.valueOf(balance) + "xp", Material.GLASS_BOTTLE);
             } else {
                 player.sendMessage("You recovered " + String.valueOf(balance) + " Exp from the destroyed bank");
             }
         }
         updateSigns(bankBlock, "Destroyed");
         Bank.destroyBank(bankBlock);
     }
 
     /**
      * Handle Block interaction with a Bank Block or a Deposit Block.
      * @param event
      */
     @EventHandler
     public void onBlockInteract(PlayerInteractEvent event) {
         if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
             return;
         }
         // If we are interacting with a cauldron, we want to deposit xp
         if (event.getClickedBlock().getType().equals(Material.CAULDRON)) {
             processCauldronInteraction(event);
         } else if (event.getClickedBlock().getType().equals(Material.DIAMOND_BLOCK)) {
             //If we are interacting with a diamond block, we want to withdraw xp
             processDiamondBlockInteraction(event);
         }
     }
 
     private void processCauldronInteraction(PlayerInteractEvent event) {
         Player player = event.getPlayer();
 
         if(!player.hasPermission("xpjar.bank.deposit")) {
             return;
         }
 
         //get a connected bank.
         Block bankBlock = getConnectedBankBlock(event.getClickedBlock());
         if (bankBlock == null) {
             return;
         }
 
         //are we depositing from a bottle?
         if(XPInTheJar.isItemXPBottle(player.getItemInHand())
                 && XPInTheJar.getXpStored(player.getItemInHand()) > 0
                 && player.hasPermission("xpjar.bank.depositeBottle")) {
 
             ItemStack bottle = player.getItemInHand();
             int toDeposit = XPInTheJar.getXpStored(bottle);
 
             this.depositExp(player, bankBlock, toDeposit);
 
             if (XPInTheJar.instance.getConfig().getBoolean("consumeBottleOnDeposit")) {
                 player.setItemInHand(new ItemStack(Material.AIR));
             } else {
                 player.setItemInHand(new ItemStack(Material.GLASS_BOTTLE));
             }
 
             return;
         }
 
         Debug.debug(player, "################################");
         Debug.debug(player, "Exp (% into level): ", player.getExp());
         Debug.debug(player, "Exp to level: ", player.getExpToLevel());
         Debug.debug(player, "Level: ", player.getLevel());
         Debug.debug(player, "Total Exp: ", player.getTotalExperience());
 
         // nothing to deposit if nothing left
         if (player.getTotalExperience() <= 0) {
             return;
         }
         // nothing to deposit (also in case)
         if (player.getLevel() <= 0 && player.getExp() <= 0) {
             return;
         }
 
        // Player don't user / place their block if they are holding one while depositing xp
        event.setCancelled(true);

         //partial level deposit
         if (player.getExp() > 0) {
             //calculate xp into level.
             int intoLevel = (int) (player.getExpToLevel() * player.getExp());
             Debug.debug(player, "XP Into Level: ", intoLevel);
 
             //deposit into level.
             player.setTotalExperience(player.getTotalExperience() - intoLevel);
             player.setLevel(player.getLevel());
             player.setExp(0);
 
             depositExp(player, bankBlock, intoLevel);
 
             return;
         }
 
         // full level deposit
         player.setLevel(player.getLevel() - 1);
         player.setExp(0);
         // failsafe -> fixes issues when going into the negative (in case this happens)
         if (player.getLevel() < 0) {
             player.setLevel(0);
             player.setExp(0);
             player.setTotalExperience(0);
             return;
         }
 
         int levelXP = player.getExpToLevel();
         player.setTotalExperience(player.getTotalExperience() - levelXP);
 
         //deposit levelXP
         depositExp(player, bankBlock, levelXP);
 
     }
 
     private void processDiamondBlockInteraction(PlayerInteractEvent event) {
         Player player = event.getPlayer();
 
         if(!player.hasPermission("xpjar.bank.withdraw")) {
             return;
         }
 
         Block bankBlock = event.getClickedBlock();
         int balance = Bank.getBankBalance(bankBlock);
 
         if (balance < 1) {
             player.sendMessage("This bank is empty");
             return;
         }
 
        // Player don't user / place their block if they are holding one while depositing xp
        event.setCancelled(true);

         // we will set the withdrawel to be what is required for the player to level up.
         int withdrawel;
         if (player.getExp() > 0) {
             withdrawel = player.getExpToLevel() - ((int) (player.getExpToLevel() * player.getExp()));
         } else {
             withdrawel = player.getExpToLevel();
         }
 
         // bank might not have enough for that
         if (balance < withdrawel) {
             withdrawel = balance;
         }
 
         player.giveExp(withdrawel);
 
         withdrawExp(player, bankBlock, withdrawel);
     }
 
 
     public void updateSigns(Block bankBlock, Object balance) {
         BlockFace[] blockFaces = {BlockFace.SELF, BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};
         for(BlockFace bf : blockFaces) {
             Block relBlock = bankBlock.getRelative(bf);
             if (relBlock.getType().equals(Material.SIGN) || relBlock.getType().equals(Material.SIGN_POST) || relBlock.getType().equals(Material.WALL_SIGN)) {
                 Sign sign = (Sign)relBlock.getState();
                 sign.setLine(0, "");
                 sign.setLine(1, "XP Bank");
                 sign.setLine(2, balance.toString());
                 sign.setLine(3, "");
                 sign.update(true);
             }
         }
     }
 
 
     public Block getConnectedBankBlock(Block bankInputBlock) {
         if (bankInputBlock.getRelative(BlockFace.UP).getType().equals(Material.DIAMOND_BLOCK)) {
             return bankInputBlock.getRelative(BlockFace.UP);
         } else if (bankInputBlock.getRelative(BlockFace.DOWN).getType().equals(Material.DIAMOND_BLOCK)) {
             return bankInputBlock.getRelative(BlockFace.DOWN);
         } else if (bankInputBlock.getRelative(BlockFace.EAST).getType().equals(Material.DIAMOND_BLOCK)) {
             return bankInputBlock.getRelative(BlockFace.EAST);
         } else if (bankInputBlock.getRelative(BlockFace.WEST).getType().equals(Material.DIAMOND_BLOCK)) {
             return bankInputBlock.getRelative(BlockFace.WEST);
         } else if (bankInputBlock.getRelative(BlockFace.NORTH).getType().equals(Material.DIAMOND_BLOCK)) {
             return bankInputBlock.getRelative(BlockFace.NORTH);
         } else if (bankInputBlock.getRelative(BlockFace.SOUTH).getType().equals(Material.DIAMOND_BLOCK)) {
             return bankInputBlock.getRelative(BlockFace.SOUTH);
         }
         return null;
     }
 
     public void depositExp(Player player, Block bankBlock, int amount) {
 
         depositExp(bankBlock, amount);
 
         if(XPInTheJar.instance.spoutEnabled && ((SpoutPlayer)player).isSpoutCraftEnabled()) {
             ((SpoutPlayer)player).sendNotification( "Exp Deposited", String.valueOf(amount) + "xp", Material.GLASS_BOTTLE);
         } else {
             player.sendMessage("Exp Deposited: " + String.valueOf(amount) + " xp");
         }
 
         //TODO: Play cool sound
     }
 
     public void depositExp(Block bankBlock, int amount) {
         int balance = Bank.addToBankBlock(bankBlock, amount);
         Debug.debug("Added ", amount, " to bank to make a new balance of ", balance);
         updateSigns(bankBlock, balance);
     }
 
     public void withdrawExp(Player player, Block bankBlock, int amount) {
         withdrawExp(bankBlock, amount);
         if(XPInTheJar.instance.spoutEnabled && ((SpoutPlayer)player).isSpoutCraftEnabled()) {
             ((SpoutPlayer)player).sendNotification( "Exp Withdrawn", amount + "xp", Material.GLASS_BOTTLE);
         } else {
             player.sendMessage("Exp Withdrawn: " + amount + " xp");
         }
     }
 
     public void withdrawExp(Block bankBlock, int amount) {
         int balance = Bank.deductFromBankBlock(bankBlock, amount);
         Debug.debug("Withdrew ", amount, " to bank to make a new balance of ", balance);
         updateSigns(bankBlock, balance);
     }
 }
