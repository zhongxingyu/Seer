 package me.MnC.MnC_SERVER_MOD.Listeners;
 
 import me.MnC.MnC_SERVER_MOD.GameMaster.Rank;
 import me.MnC.MnC_SERVER_MOD.GugaEventWorld;
 import me.MnC.MnC_SERVER_MOD.MinecraftPlayer;
 import me.MnC.MnC_SERVER_MOD.MnC_SERVER_MOD;
 import me.MnC.MnC_SERVER_MOD.ServerRegion;
 import me.MnC.MnC_SERVER_MOD.Estates.EstateHandler;
 import me.MnC.MnC_SERVER_MOD.Handlers.GameMasterHandler;
 import me.MnC.MnC_SERVER_MOD.Handlers.ServerRegionHandler;
 import me.MnC.MnC_SERVER_MOD.basicworld.BasicWorld;
 import me.MnC.MnC_SERVER_MOD.chat.ChatHandler;
 import me.MnC.MnC_SERVER_MOD.manor.Manor;
 import me.MnC.MnC_SERVER_MOD.manor.ManorManager;
 import me.MnC.MnC_SERVER_MOD.rpg.BonusDrop;
 import me.MnC.MnC_SERVER_MOD.rpg.PlayerProfession;
 import me.MnC.MnC_SERVER_MOD.util.SpongeUtil;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class BlockListener implements Listener
 {
 	public BlockListener(MnC_SERVER_MOD plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		Block block = event.getBlock();
 		Player p = event.getPlayer();
 		
 		long timeStart = System.nanoTime();
 		
 		MinecraftPlayer player = plugin.userManager.getUser(p.getName());
 		if (!player.isAuthenticated())
 		{
 			event.setCancelled(true);
 			return;
 		}
 		
 		//*************************GRIEFING PROTECTION*************************
 		if(player.getProfession() != null && player.getProfession().getLevel() < 10 && !BasicWorld.IsBasicWorld(event.getBlock().getLocation()))
 		{
 			ChatHandler.FailMsg(player.getPlayerInstance(), "Jste novacek. Novacci smi stavet jenom ve svete pro novacky. Dostanete se tam /pp bw.");
 			event.setCancelled(true);
 			return;
 		}		
 		
 		if (plugin.arena.IsArena(p.getLocation()))
 		{
 			if (GameMasterHandler.IsAtleastRank(p.getName(), Rank.BUILDER))
 			{
 				return;
 			}
 			ChatHandler.FailMsg(p,"V arene nemuzete kopat.");
 			event.setCancelled(true);
 			return;
 		}
 		if(plugin.EventWorld.IsEventWorld(p.getLocation()) && GugaEventWorld.regionStatus())
 		{
 			if (GameMasterHandler.IsAtleastRank(p.getName(), Rank.EVENTER))
 			{
 				return;
 			}
 			ChatHandler.FailMsg(p, "V EventWorldu nemuzete kopat!");
 			event.setCancelled(true);
 			return;
 		}
 		
 		if (!ServerRegionHandler.CanInteract(p, block.getX(), block.getZ()))
 		{
 			if (!GameMasterHandler.IsAtleastRank(p.getName(), Rank.BUILDER))
 			{
 				event.setCancelled(true);
 				ServerRegion region = ServerRegionHandler.GetRegionByCoords(block.getX(), block.getZ(), block.getWorld().getName());
 				ChatHandler.FailMsg(p, "Tady nemuzete kopat! Nazev pozemku: " + ChatColor.YELLOW + region.GetName());
 		        if(region.GetWorld().equals("world_basic"))
 		        {
 		        	if(player.getProfession()!=null && player.getProfession().getLevel() < 10)
 		        	{
 		        		BasicWorld.basicWorldRegionBlockBreak(region,event.getPlayer(),event.getBlock());
 		        	}
 		        }
 				return;
 			}
 		}
 		
 		//UserRegions
 		if(!EstateHandler.canInteract(player.getName(), event.getBlock()) && !GameMasterHandler.IsAtleastRank(p.getName(), Rank.BUILDER))
 		{
 			event.setCancelled(true);
 			ChatHandler.FailMsg(p, "Nemuzete kopat blocky na pozemku jineho hrace.");
 		}
 		
 		// Manors
 		Manor manor = ManorManager.getInstance().getManorByLocation(event.getBlock().getLocation());
 		if(manor != null && !manor.canBlockPlaceBreak(player))
 		{
 			event.setCancelled(true);
 			player.getPlayerInstance().sendMessage("This is manor "+manor.getName()+". You are not allowed to break blocks here.");
 			return;
 		}
 		
 		
 		PlayerProfession prof = player.getProfession();
 		int level = prof.getLevel();	
 		if(level >= 10 && BasicWorld.IsBasicWorld(event.getPlayer().getLocation()))
 		{
 			if(!GameMasterHandler.IsAtleastGM(event.getPlayer().getName()) && !(level > 20))
 			{
 				ChatHandler.InfoMsg(p, "Pro opusteni zakladniho sveta napiste /pp spawn");
 			}
 		}
 		
 		//TODO debug message
 		if (plugin.debug)
 		{
 			plugin.log.info("BLOCK_BREAK_EVENT: playerName="+p.getName()+",typeID="+block.getTypeId()+",Time=" + ((System.nanoTime() - timeStart)/1000000f));
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onBlockBreakMonitor(BlockBreakEvent event)
 	{
 		Block block = event.getBlock();
 		MinecraftPlayer player = plugin.userManager.getUser(event.getPlayer().getName());
 		
 		if(!(block.getTypeId() == 50 || block.getTypeId() == 78 || block.getTypeId() == 6 || block.getTypeId() == 55 || block.getTypeId() == 76))
 		{
 			player.getProfession().gainExperience(4);
 		}
 		
 		ItemStack itemInHand = event.getPlayer().getItemInHand();
 		if(!(itemInHand != null && itemInHand.getItemMeta() !=null && itemInHand.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)))
 		{
 			BonusDrop.dropBonusDrops(player.getProfession(), event.getBlock());
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		//TODO debug message
 		
 		MinecraftPlayer player = plugin.userManager.getUser(event.getPlayer().getName());
 		if (!player.isAuthenticated())
 		{
 			event.setCancelled(true);
 			return;
 		}
 		
 		if(player.getProfession() == null || player.getProfession().getLevel() < 10 && !BasicWorld.IsBasicWorld(event.getBlock().getLocation()))
 		{
 			ChatHandler.FailMsg(player.getPlayerInstance(), "Jste novacek. Novacci smi stavet jenom ve svete pro novacky. Dostanete se tam /pp bw.");
 			event.setCancelled(true);
 			return;
 		}
 		
 	
 		if(event.getBlock().getType() == Material.TNT && (player.getProfession() == null || player.getProfession().getLevel() < 50))
 		{
 			ChatHandler.FailMsg(player.getPlayerInstance(), "Nemate lvl 50, nemuzete pouzit TNT.");
 			event.setCancelled(true);
 			return;
 		}
 		
 		if (plugin.arena.IsArena(event.getBlock().getLocation()))
 		{
 			if (!GameMasterHandler.IsAtleastRank(event.getPlayer().getName(), Rank.BUILDER))
 			{
 				event.setCancelled(true);
 				return;
 			}
 		}
 		Player p = event.getPlayer(); 
 		if(plugin.EventWorld.IsEventWorld(p.getLocation()) && GugaEventWorld.regionStatus())
 		{
 			if (GameMasterHandler.IsAtleastRank(p.getName(), Rank.EVENTER))
 			{
 				return;
 			}
 			ChatHandler.FailMsg(p, "V EventWorldu nemuzete pokladat blocky!");
 			event.setCancelled(true);
 			return;
 		}
 		
 		if (!ServerRegionHandler.CanInteract(event.getPlayer(), event.getBlock().getX(), event.getBlock().getZ()))
 		{
 			if (!GameMasterHandler.IsAtleastRank(event.getPlayer().getName(), Rank.BUILDER))
 			{
 				event.setCancelled(true);
 				ServerRegion region = ServerRegionHandler.GetRegionByCoords(event.getBlock().getX(), event.getBlock().getZ(), p.getWorld().getName());
 				ChatHandler.FailMsg(p, "Tady nemuzete stavet! Nazev pozemku: " + ChatColor.YELLOW  + region.GetName());
 				return;
 			}
 		}
 		
 		//UserRegions
 		if(!EstateHandler.canInteract(player.getName(), event.getBlock()) && !GameMasterHandler.IsAtleastRank(p.getName(), Rank.BUILDER))
 		{
 			event.setCancelled(true);
 			ChatHandler.FailMsg(p, "Nemuzete pokladat blocky na pozemku jineho hrace.");
 		}
 		
 		// Manors
 		Manor manor = ManorManager.getInstance().getManorByLocation(event.getBlock().getLocation());
 		if(manor != null && !manor.canBlockPlaceBreak(player))
 		{
 			event.setCancelled(true);
 			player.getPlayerInstance().sendMessage("This is manor "+manor.getName()+". You are not allowed to place blocks here.");
 			return;
 		}
 		
 		Block block = event.getBlockPlaced();
 		
 		if(block.getTypeId() == 19)
 		{
 			World world = block.getWorld();
 			int x = block.getX();
 			int y = block.getY();
 			int z = block.getZ();
 			SpongeUtil.clearWater(world, x, y, z);
 			SpongeUtil.setUsedAsSponge(block);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onBlockIgnite(BlockIgniteEvent event)
 	{
 		//TODO debug message
 		BlockIgniteEvent.IgniteCause cause = event.getCause();
 
 		if (cause == BlockIgniteEvent.IgniteCause.LIGHTNING)
 		{
 			event.setCancelled(true);
 			return;
 		}
 
 		if (cause == BlockIgniteEvent.IgniteCause.LAVA)
 		{
 			event.setCancelled(true);
 			return;
 		}
 		
 		if (cause == BlockIgniteEvent.IgniteCause.SPREAD)
 		{
 			event.setCancelled(true);
 			return;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onBlockBurn(BlockBurnEvent event)
 	{
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
  	public void onBlockRedstoneChange(BlockRedstoneEvent event)
 	{
 		//TODO debug message
 		Block block = event.getBlock();
 		int blockID = block.getTypeId();
 		if (plugin.debug)
 		{
 			plugin.log.info("REDSTONE_CHANGE_EVENT: typeID=" + blockID + ",blockData=" + block.getData() + ",x=" + block.getX() + ",y=" + block.getY() + ",z=" + block.getZ());
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onBlockFromTo(BlockFromToEvent event)
 	{
         World world = event.getBlock().getWorld();
         Block blockTo = event.getToBlock();
         
         int ox = blockTo.getX();
         int oy = blockTo.getY();
         int oz = blockTo.getZ();

        for (int cx = -4; cx <= 4; cx++)
         {
        	for (int cy = -4; cy <= 4; cy++) 
         	{
        		for (int cz = -4; cz <= 4; cz++) 
         		{
         			Block sponge = world.getBlockAt(ox + cx, oy + cy, oz + cz);
         			if (sponge.getTypeId() == 19 || SpongeUtil.wasUsedAsSponge(sponge)) 
         			{
                             event.setCancelled(true);
                             return;
         			}
         		}
         	}
         }
 	}
 	public MnC_SERVER_MOD plugin;
 }
