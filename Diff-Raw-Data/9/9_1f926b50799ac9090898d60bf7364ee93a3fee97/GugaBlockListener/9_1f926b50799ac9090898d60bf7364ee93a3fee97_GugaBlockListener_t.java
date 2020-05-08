 package me.Guga.Guga_SERVER_MOD.Listeners;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import me.Guga.Guga_SERVER_MOD.BasicWorld;
 import me.Guga.Guga_SERVER_MOD.GugaBonusDrop;
 import me.Guga.Guga_SERVER_MOD.GugaEventWorld;
 import me.Guga.Guga_SERVER_MOD.GugaProfession2;
 import me.Guga.Guga_SERVER_MOD.GugaRegion;
 import me.Guga.Guga_SERVER_MOD.Guga_SERVER_MOD;
 import me.Guga.Guga_SERVER_MOD.Locker;
 import me.Guga.Guga_SERVER_MOD.GameMaster.Rank;
 import me.Guga.Guga_SERVER_MOD.MinecraftPlayer;
 import me.Guga.Guga_SERVER_MOD.Handlers.ChatHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GameMasterHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaRegionHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.HomesHandler;
 
 import org.bukkit.ChatColor;
import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class GugaBlockListener implements Listener
 {
 	public GugaBlockListener(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockBreak(BlockBreakEvent e)
 	{
 		MinecraftPlayer player = plugin.userManager.getUser(e.getPlayer().getName());
 		if (!player.isAuthenticated())
 		{
 			e.setCancelled(true);
 			return;
 		}
 		
 		if(player.getProfession() != null && player.getProfession().GetLevel() < 10 && !BasicWorld.IsBasicWorld(e.getBlock().getLocation()))
 		{
 			ChatHandler.FailMsg(player.getPlayerInstance(), "Jste novacek. Novacci smi stavet jenom ve svete pro novacky. Dostanete se tam /pp bw.");
 			e.setCancelled(true);
 			return;
 		}
 		
 		if (plugin.debug == true)
 		{
 			plugin.log.info("BLOCK_BREAK_EVENT: playerName="+e.getPlayer().getName()+",typeID="+e.getBlock().getTypeId());
 		}
 		Player p = e.getPlayer();
 		long timeStart = System.nanoTime();
 		if (plugin.arena.IsArena(p.getLocation()))
 		{
 			if (GameMasterHandler.IsAtleastRank(p.getName(), Rank.BUILDER))
 			{
 				return;
 			}
 			ChatHandler.FailMsg(p,"V arene nemuzete kopat.");
 			e.setCancelled(true);
 			return;
 		}
 		if(plugin.EventWorld.IsEventWorld(p.getLocation()) && GugaEventWorld.regionStatus())
 		{
 			if (GameMasterHandler.IsAtleastRank(p.getName(), Rank.EVENTER))
 			{
 				return;
 			}
 			ChatHandler.FailMsg(p, "V EventWorldu nemuzete kopat!");
 			e.setCancelled(true);
 			return;
 		}
 		
 		if (!GugaRegionHandler.CanInteract(p, e.getBlock().getX(), e.getBlock().getZ()))
 		{
 			if (!GameMasterHandler.IsAtleastRank(p.getName(), Rank.BUILDER))
 			{
 				e.setCancelled(true);
 				GugaRegion region = GugaRegionHandler.GetRegionByCoords(e.getBlock().getX(), e.getBlock().getZ(), p.getWorld().getName());
 				ChatHandler.FailMsg(p, "Tady nemuzete kopat! Nazev pozemku: " + ChatColor.YELLOW + region.GetName());
 		        if(region.GetWorld().equals("world_basic"))
 		        {
 		        	if(player.getProfession()!=null && player.getProfession().GetLevel() < 10)
 		        	{
 		        		BasicWorld.basicWorldRegionBlockBreak(region,e.getPlayer(),e.getBlock());
 		        	}
 		        }
 				return;
 			}
 		}
 		
 		GugaProfession2 prof = player.getProfession();
 		int level = prof.GetLevel();
 		//*************************GRIEFING PROTECTION*************************
 		Block targetBlock;
 		targetBlock = e.getBlock();
 		if(level >= 10 && BasicWorld.IsBasicWorld(e.getPlayer().getLocation()))
 		{
 			if(!GameMasterHandler.IsAtleastGM(e.getPlayer().getName())||!HomesHandler.getHomeByPlayer(e.getPlayer().getName()).getWorld().equalsIgnoreCase("world_basic"))
 			{
 				ChatHandler.InfoMsg(p, "Pro opusteni zakladniho sveta napiste /pp spawn");
 			}
 		}
 		//*************************************************************************
 		int blockType = targetBlock.getTypeId();
 		if (Locker.LockableBlocks.isLockableBlock(blockType))
 		{
 			if(plugin.blockLocker.IsLocked(targetBlock))
 			{
 				e.setCancelled(true);
 				String owner = plugin.blockLocker.GetBlockOwner(targetBlock);
 				if (owner.equalsIgnoreCase(p.getName()))
 				{
 					ChatHandler.InfoMsg(p, "Nemuzete rozbit svoji zamcenou truhlu. Nejdrive ji odemknete.");
 				}
 				else
 				{
 					ChatHandler.FailMsg(p, "Nemuzete rozbit cizi zamcenou truhlu! " + ChatColor.YELLOW + owner + ChatColor.RED + " je vlastnikem teto truhly.");
 				}
 				return;
 			}
 		}
 		
 		int typeId = targetBlock.getTypeId();
 		prof.onBlockBreak(targetBlock);
 		if (typeId == 1)
 		{
 			GugaBonusDrop bonus = prof.CobbleStoneDrop();
 			if (bonus == GugaBonusDrop.DIAMOND)
 			{
 				p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(264,1));
 				prof.GainExperience(50);
 				ChatHandler.InfoMsg(p, "Nasel jste " + ChatColor.AQUA + "diamant" + ChatColor.YELLOW + "!");
 			}
 			else if (bonus == GugaBonusDrop.GOLD)
 			{
 				p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(14,1));
 				prof.GainExperience(40);
 				ChatHandler.InfoMsg(p, "Nasel jste " + ChatColor.GOLD + "zlato" + ChatColor.YELLOW + "!");
 			}
 			else if (bonus == GugaBonusDrop.IRON)
 			{
 				p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(15,1));
 				prof.GainExperience(30);
 				ChatHandler.InfoMsg(p, "Nasel jste " + ChatColor.GRAY + "zelezo" + ChatColor.YELLOW + "!");
 			}
 			else if (bonus == GugaBonusDrop.EMERALD)
 			{
 				p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(388,1));
 				prof.GainExperience(60);
 				ChatHandler.InfoMsg(p, "Nasel jste " + ChatColor.GREEN + "emerald" + ChatColor.YELLOW + "!");
 			}
 		}
 		
 		if (plugin.debug == true)
 		{
 			plugin.log.info("BLOCK_BREAK_EVENT: Time=" + ((System.nanoTime() - timeStart)/1000));
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockPlace(BlockPlaceEvent e)
 	{
 		MinecraftPlayer player = plugin.userManager.getUser(e.getPlayer().getName());
 		if (!player.isAuthenticated())
 		{
 			e.setCancelled(true);
 			return;
 		}
 		
 		if(player.getProfession() == null || player.getProfession().GetLevel() < 10 && !BasicWorld.IsBasicWorld(e.getBlock().getLocation()))
 		{
 			ChatHandler.FailMsg(player.getPlayerInstance(), "Jste novacek. Novacci smi stavet jenom ve svete pro novacky. Dostanete se tam /pp bw.");
 			e.setCancelled(true);
 			return;
 		}
 		
		if(e.getBlock().getType() == Material.TNT && (player.getProfession() == null || player.getProfession().GetLevel() < 50))
 		{
 			ChatHandler.FailMsg(player.getPlayerInstance(), "Nemate lvl 50, nemuzete pouzit TNT.");
 			e.setCancelled(true);
 			return;
 		}
 		
 		if (plugin.debug)
 		{
 			plugin.log.info("BLOCK_PLACE_EVENT: typeID="+e.getBlock().getTypeId()+",PlayerName="+e.getPlayer().getName());
 		}
 		//plugin.logger.LogBlockPlace(e);
 		if (plugin.arena.IsArena(e.getBlock().getLocation()))
 		{
 			if (!GameMasterHandler.IsAtleastRank(e.getPlayer().getName(), Rank.BUILDER))
 			{
 				e.setCancelled(true);
 				return;
 			}
 		}
 		Player p = e.getPlayer(); 
 		if(plugin.EventWorld.IsEventWorld(p.getLocation()) && GugaEventWorld.regionStatus())
 		{
 			if (GameMasterHandler.IsAtleastRank(p.getName(), Rank.EVENTER))
 			{
 				return;
 			}
 			ChatHandler.FailMsg(p, "V EventWorldu nemuzete pokladat blocky!");
 			e.setCancelled(true);
 			return;
 		}
 		
 		if (!GugaRegionHandler.CanInteract(e.getPlayer(), e.getBlock().getX(), e.getBlock().getZ()))
 		{
 			if (!GameMasterHandler.IsAtleastRank(e.getPlayer().getName(), Rank.BUILDER))
 			{
 				e.setCancelled(true);
 				GugaRegion region = GugaRegionHandler.GetRegionByCoords(e.getBlock().getX(), e.getBlock().getZ(), p.getWorld().getName());
 				ChatHandler.FailMsg(p, "Tady nemuzete stavet! Nazev pozemku: " + ChatColor.YELLOW  + region.GetName());
 				return;
 			}
 		}
 		
 		//54
 		Block block = e.getBlockPlaced();
 		if(Locker.LockableBlocks.isLockableBlock(block.getTypeId()))
 		{
 			if(block.getTypeId() == Locker.LockableBlocks.CHEST.getID() && !plugin.blockLocker.IsLocked(block))
 			{
 				Block W=block.getRelative(BlockFace.WEST);
 				Block E=block.getRelative(BlockFace.EAST);
 				Block N=block.getRelative(BlockFace.NORTH);
 				Block S=block.getRelative(BlockFace.SOUTH);
 				
 				if(W.getTypeId() == 54 || E.getTypeId() == 54 || N.getTypeId() == 54 || S.getTypeId() == 54)
 				{
 					String username = e.getPlayer().getName();
 					if((plugin.blockLocker.IsLocked(W) && !plugin.blockLocker.IsOwner(W,username)) ||
 						(plugin.blockLocker.IsLocked(E) && !plugin.blockLocker.IsOwner(E,username)) ||
 						(plugin.blockLocker.IsLocked(N) && !plugin.blockLocker.IsOwner(N,username)) ||
 						(plugin.blockLocker.IsLocked(S) && !plugin.blockLocker.IsOwner(S,username)))
 					{
 						ChatHandler.FailMsg(p, "Nemuzete postavit truhlu vedle cizi zamcene!");
 						e.setCancelled(true);
 						return;
 					}
 					else
 					{
 						plugin.blockLocker.LockBlock(block,e.getPlayer().getName());
 						ChatHandler.SuccessMsg(p, "Vase dvojita truhla byla zamcena.");
 						return;
 					}
 				}
 				else
 				{
 					plugin.blockLocker.LockBlock(block, e.getPlayer().getName());
 					ChatHandler.SuccessMsg(p, "Vase truhla byla zamcena.");
 					return;
 				}			
 			}
 			else if(block.getTypeId() == Locker.LockableBlocks.FURNANCE.getID() && !plugin.blockLocker.IsLocked(block))
 			{
 				plugin.blockLocker.LockBlock(block,e.getPlayer().getName());
 				ChatHandler.SuccessMsg(p, "Vase pec byla zamcena.");
 			}
 			else if(block.getTypeId() == Locker.LockableBlocks.BURNING_FURNANCE.getID() && !plugin.blockLocker.IsLocked(block))
 			{
 				plugin.blockLocker.LockBlock(block,e.getPlayer().getName());
 				ChatHandler.SuccessMsg(p, "Vase pec byla zamcena.");
 			}
 			else if(block.getTypeId() == Locker.LockableBlocks.DISPENSER.getID() && !plugin.blockLocker.IsLocked(block))
 			{
 				plugin.blockLocker.LockBlock(block,e.getPlayer().getName());
 				ChatHandler.SuccessMsg(p, "Vas davkovac byl zamcen.");
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onBlockIgnite(BlockIgniteEvent e)
 	{
		BlockIgniteEvent.IgniteCause cause = e.getCause();
 
 		if (cause == BlockIgniteEvent.IgniteCause.LIGHTNING)
 		{
 			e.setCancelled(true);
 			return;
 		}
 
 		if (cause == BlockIgniteEvent.IgniteCause.LAVA)
 		{
 			e.setCancelled(true);
 			return;
 		}
 		
 		if (cause == BlockIgniteEvent.IgniteCause.SPREAD)
 		{
 			e.setCancelled(true);
 			return;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onBlockBurn(BlockBurnEvent e)
 	{
 		e.setCancelled(true);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
  	public void onBlockRedstoneChange(BlockRedstoneEvent e)
 	{
 		Block block = e.getBlock();
 		int blockID = block.getTypeId();
 		if (plugin.debug)
 		{
 			plugin.log.info("REDSTONE_CHANGE_EVENT: typeID=" + blockID + ",blockData=" + block.getData() + ",x=" + block.getX() + ",y=" + block.getY() + ",z=" + block.getZ());
 		}
 		if (!redStoneDebug.isEmpty())
 		{
 			Iterator<Player> it = redStoneDebug.iterator();
 			while(it.hasNext())
 			{
 				it.next().sendMessage("RS_EVENT: ID=" + blockID + ",x=" + block.getX() + ",y=" + block.getY() + ",z=" + block.getZ());
 			}
 		}
 	}
 
 	public ArrayList<Player> redStoneDebug = new ArrayList<Player>();
 	public static Guga_SERVER_MOD plugin;
 }
