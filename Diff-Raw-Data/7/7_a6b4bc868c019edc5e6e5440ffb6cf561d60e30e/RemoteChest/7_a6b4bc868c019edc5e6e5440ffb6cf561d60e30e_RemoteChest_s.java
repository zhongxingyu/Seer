 package me.corriekay.pppopp3.remotechest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import mc.alk.arena.util.ExpUtil;
 import me.corriekay.pppopp3.modules.Equestria;
 import me.corriekay.pppopp3.ponyville.Pony;
 import me.corriekay.pppopp3.ponyville.Ponyville;
 import me.corriekay.pppopp3.utils.PSCmdExe;
 import me.corriekay.pppopp3.utils.Utils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.meta.SkullMeta;
 
 public class RemoteChest extends PSCmdExe{
 
 	private HashMap<String,Inventory> viewingInvs = new HashMap<String,Inventory>();
 
 	public RemoteChest(){
 		super("RemoteChest", "c", "w", "transferchest");
 	}
 
 	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
 		String cmdn = cmd.getName();
 		if(!(sender instanceof Player)) {
 			sendMessage(sender, notPlayer);
 			return true;
 		}
 		Player player = (Player)sender;
 		if(cmdn.equals("c")) {
 			Pony pony = Ponyville.getPony(player);
 			Inventory inv = pony.getRemoteChest(Equestria.get().getParentWorld(player.getWorld()));
 			if(inv == null) {
 				sendMessage(player, "There is no remote chest for this world!");
 				return true;
 			}
 			player.openInventory(inv);
 			viewingInvs.put(player.getName(), inv);
 			return true;
 		}
 		if(cmdn.equals("w")) {
 			player.openWorkbench(null, true);
 			return true;
 		}
 
 		if(cmd.getName().equals("transferchest")) {
 			World world = Equestria.get().getParentWorld(player.getWorld());
 			Pony pony = Ponyville.getPony(player);
 			Inventory inv = pony.getRemoteChest(world);
 			if(inv == null) {
 				sendMessage(player, "There is no remote chest for this world!");
 				return true;
 			}
 			if(args.length == 0) {
 				transferChest(player, player.getInventory(), inv, "null", false);
 				sendMessage(player, "Items transferred!");
 				return true;
 			}
 			if(args[0].equals("help")) {
 				sendMessage(player, "transferchest is a powerful item transferral tool for the on-the-go miner! if you just want to dump your inventory into your chest, just type /transferchest. Simple as that! If you want to use a \"smart\" mode, type either /transferchest ore (to transfer ores and ingots) or /transferchest material (to transfer materials, such as dirt, gravel, sand, cobblestone) to your chest. If youre feeling adventurous, type /transferchest <material type (or) material id> to transfer a specific type of item to your inventory!");
 				return true;
 			}
 			if(args[0].equals("ore") || args[0].equals("material")) {
 				transferChest(player, player.getInventory(), inv, args[0], false);
 				sendMessage(player, "Items Transferred!");
 				return true;
 			}
 			boolean correct = true;
 			Material mat = null;
 			int matId;
			try {
				mat = Material.matchMaterial(args[0]);
				correct = true;
			} catch(IllegalArgumentException e) {
 				try {
 					matId = Integer.parseInt(args[0]);
 					mat = Material.getMaterial(matId);
 					correct = true;
 				} catch(NumberFormatException e2) {
 					correct = false;
 				}
 			}
 			if(correct) {
 				transferChest(player, player.getInventory(), inv, mat.name(), true);
 				sendMessage(player, "Items transferred!");
 				return true;
 			} else {
 				sendMessage(player, "Sorry... I couldnt find that item type!");
 				return true;
 			}
 		}
 		return true;
 	}
 
 	@EventHandler
 	public void invclick(InventoryClickEvent event){
 		if(event.getWhoClicked() instanceof Player) {
 			Player player = (Player)event.getWhoClicked();
 			if(viewingInvs.containsKey(player.getName())) {
 				Pony pony = Ponyville.getPony(player);
 				String invWorldname = pony.getRCWorld(viewingInvs.get(player.getName())).getName();
 				String actualWorldName = player.getWorld().getName();
 				if(invWorldname != actualWorldName) {
 					event.setCancelled(true);
 					player.closeInventory();
 					viewingInvs.remove(player.getName());
 					return;
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void invclose(InventoryCloseEvent event){
 		if(event.getPlayer() instanceof Player) {
 			Player p = (Player)event.getPlayer();
 			if(viewingInvs.containsKey(p.getName())) {
 				Inventory inv2 = viewingInvs.get(p.getName());
 				viewingInvs.remove(p.getName());
 				Pony pony = Ponyville.getPony(p);
 				pony.saveRemoteChest(pony.getRCWorld(inv2));
 				pony.save();
 				System.out.println("saving inventory");
 				return;
 			}
 		}
 	}
 
 	protected static void transferChest(Player player, PlayerInventory playerInv, Inventory chestInv, String param, boolean isMat){
 		HashSet<Material> typesToTransfer = new HashSet<Material>();
 		if(isMat) {
 			Material mat = Material.getMaterial(param);
 			typesToTransfer.add(mat);
 		} else {
 			if(param.equals("ore")) {
 				typesToTransfer.add(Material.COAL);
 				typesToTransfer.add(Material.COAL_ORE);
 				typesToTransfer.add(Material.DIAMOND);
 				typesToTransfer.add(Material.DIAMOND_ORE);
 				typesToTransfer.add(Material.DIAMOND_BLOCK);
 				typesToTransfer.add(Material.IRON_INGOT);
 				typesToTransfer.add(Material.IRON_ORE);
 				typesToTransfer.add(Material.IRON_BLOCK);
 				typesToTransfer.add(Material.GOLD_INGOT);
 				typesToTransfer.add(Material.GOLD_ORE);
 				typesToTransfer.add(Material.GOLD_BLOCK);
 				typesToTransfer.add(Material.LAPIS_ORE);
 				typesToTransfer.add(Material.LAPIS_BLOCK);
 				typesToTransfer.add(Material.REDSTONE_ORE);
 				typesToTransfer.add(Material.REDSTONE);
 			}
 			if(param.equals("material")) {
 				typesToTransfer.add(Material.DIRT);
 				typesToTransfer.add(Material.STONE);
 				typesToTransfer.add(Material.COBBLESTONE);
 				typesToTransfer.add(Material.SAND);
 				typesToTransfer.add(Material.GRAVEL);
 				typesToTransfer.add(Material.CLAY_BALL);
 				typesToTransfer.add(Material.WOOD);
 				typesToTransfer.add(Material.LOG);
 				typesToTransfer.add(Material.SNOW_BALL);
 				typesToTransfer.add(Material.SNOW_BLOCK);
 			}
 			if(param.equals("null")) {//dumb transfer/alltransfer
 				for(Material material : Material.values()) {
 					typesToTransfer.add(material);
 				}
 			}
 		}
 		for(int i = 9; i <= 35; i++) {
 			ItemStack playerIs = playerInv.getContents()[i];
 			if(playerIs == null) {
 				playerIs = new ItemStack(Material.AIR);
 			}
 			if(typesToTransfer.contains(playerIs.getType())) {
 				try {
 					HashMap<Integer,ItemStack> returnedItems = chestInv.addItem(playerInv.getItem(i));
 					playerInv.setItem(i, returnedItems.get(0));
 				} catch(NullPointerException e) {}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onDeath(PlayerDeathEvent event){
 		if(Equestria.get().getParentWorld(event.getEntity().getWorld()).getName().equals("badlands")) {
 			event.setKeepLevel(false);
 			if(!(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
 				event.setDroppedExp(0);
 				event.setNewTotalExp(0);
 				return;
 			} else {
 				Player player = event.getEntity();
 				EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent)player.getLastDamageCause();
 				if(!(edbee.getDamager() instanceof Player)) {
 					event.setDroppedExp(0);
 					event.setNewTotalExp(0);
 					return;
 				}
 				Player killer = (Player)edbee.getDamager();
 				Pony pony = Ponyville.getPony(player);
 				Inventory inv = pony.getRemoteChest(Bukkit.getWorld("badlands"));
 				for(ItemStack is : inv.getContents()) {
 					event.getDrops().add(is);
 				}
 				inv.clear();
 				pony.saveRemoteChest(Bukkit.getWorld("badlands"));
 				pony.save();
 				ExpUtil.giveExperience(killer, event.getDroppedExp() + event.getNewExp());
 				event.setDroppedExp(0);
 				event.setNewTotalExp(0);
 				ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
 				SkullMeta sm = (SkullMeta)is.getItemMeta();
 				sm.setOwner(player.getName());
 				List<String> lore = new ArrayList<String>();
 				String weapon = "fists";
 				if(killer.getItemInHand() != null) {
 					weapon = killer.getItemInHand().getType().name();
 				}
 				lore.add("Killed by " + killer.getName() + " with " + weapon + "!");
 				lore.add("Time of death: " + Utils.getDate(System.currentTimeMillis()));
 				sm.setLore(lore);
 				is.setItemMeta(sm);
 				event.getDrops().add(is);
 			}
 		}
 	}
 }
