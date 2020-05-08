 package me.Guga.Guga_SERVER_MOD.Listeners;
 
 import java.util.ArrayList;
 
 
 
 import me.Guga.Guga_SERVER_MOD.GugaBonusDrop;
 import me.Guga.Guga_SERVER_MOD.GugaEventWorld;
 import me.Guga.Guga_SERVER_MOD.GugaProfession;
 import me.Guga.Guga_SERVER_MOD.GugaRegion;
 import me.Guga.Guga_SERVER_MOD.Guga_SERVER_MOD;
 import me.Guga.Guga_SERVER_MOD.GameMaster.Rank;
 import me.Guga.Guga_SERVER_MOD.Handlers.GameMasterHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaRegionHandler;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
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
 		if (plugin.debug == true)
 		{
 			plugin.log.info("BLOCK_BREAK_EVENT: playerName="+e.getPlayer().getName()+",typeID="+e.getBlock().getTypeId());
 		}
 		plugin.logger.LogBlockBreak(e, e.getBlock().getTypeId());
 		long timeStart = System.nanoTime();
 		Player p = e.getPlayer();
 		if (plugin.arena.IsArena(p.getLocation()))
 		{
 			if (GameMasterHandler.IsAtleastRank(p.getName(), Rank.BUILDER))
 			{
 				return;
 			}
 			p.sendMessage(ChatColor.BLUE+"[ARENA]: "+ChatColor.WHITE+"V arene nemuzes kopat!");
 			e.setCancelled(true);
 			return;
 		}
 		if(plugin.EventWorld.IsEventWorld(p.getLocation())&& GugaEventWorld.regionStatus())
 		{
 			if (GameMasterHandler.IsAtleastRank(p.getName(), Rank.EVENTER))
 			{
 				return;
 			}
 			p.sendMessage(ChatColor.BLUE+"[EVENTWORLD]: "+ChatColor.WHITE+"V EventWorldu nemuzes kopat!");
 			e.setCancelled(true);
 			return;
 		}
 		if (!GugaRegionHandler.CanInteract(p, e.getBlock().getX(), e.getBlock().getZ()))
 		{
 			if (!GameMasterHandler.IsAtleastRank(p.getName(), Rank.BUILDER))
 			{
 				e.setCancelled(true);
 				GugaRegion region = GugaRegionHandler.GetRegionByCoords(e.getBlock().getX(), e.getBlock().getZ());
 				p.sendMessage(ChatColor.BLUE+"[REGIONS]: "+"Tady nemuzes kopat! Nazev pozemku: " + region.GetName());
 				return;
 			}
 		}
 		if (!plugin.acc.UserIsLogged(p) && plugin.config.accountsModule)
 		{
 			e.setCancelled(true);
 			p.sendMessage("******************************");
 			if (plugin.acc.UserIsRegistered(p))
 			{
 				p.sendMessage("NEJSTE PRIHLASEN! Prihlaste se pomoci /login heslo");
 			}
 			else
 			{
 				p.sendMessage("NEJSTE ZAREGISTROVAN! Prosim zaregistrujte se /register heslo");
 			}
 			p.sendMessage("******************************");
 			return;
 		}
 		GugaProfession prof = plugin.professions.get(p.getName());
 		boolean canBreak = false;
 		//*************************GRIEFING PROTECTION*************************
 		Block targetBlock;
 		targetBlock = e.getBlock();
 		if (prof == null)
 		{
 			int i = 0;
 			while (i < allowedBlocksTier1.length)
 			{
 				if (e.getBlock().getTypeId() == allowedBlocksTier1[i])
 				{
 					canBreak = true;
 					break;
 				}
 				i++;
 			}
 			if (canBreak)
 			{
 				Block blockAbove = targetBlock.getRelative(BlockFace.UP);
 				if (blockAbove.getTypeId() == 55)
 				{
 					canBreak = false;
 				}
 			}
 			if (GameMasterHandler.IsAtleastGM(p.getName()))
 			{
 				if(plugin.acc.UserIsLogged(p))
 				{
 					canBreak = true;
 				}
 			}
 			if (!canBreak)
 			{
 				p.sendMessage(ChatColor.BLUE+"[RPG]: "+ChatColor.WHITE+"Musite byt alespon level 5, aby jste mohl kopat tento druh bloku!");
 				e.setCancelled(true);
 				return;
 			}
 		}
 		else
 		{
 			int level = prof.GetLevel();
 			if (level < 5)
 			{
 				int i = 0;
 				while (i < allowedBlocksTier1.length)
 				{
 					if (e.getBlock().getTypeId() == allowedBlocksTier1[i])
 					{
 						canBreak = true;
 						break;
 					}
 					i++;
 				}
 				if (canBreak)
 				{
 					Block blockAbove = targetBlock.getRelative(BlockFace.UP);
 					if (blockAbove.getTypeId() == 55)
 					{
 						canBreak = false;
 					}
 				}
 				if (GameMasterHandler.IsAtleastGM(p.getName()))
 				{
 					if(plugin.acc.UserIsLogged(p))
 					{
 						canBreak = true;
 					}
 				}
 				if (!canBreak)
 				{
 					p.sendMessage(ChatColor.BLUE+"[RPG]: "+ChatColor.WHITE+"Musite byt alespon level 5, aby jste mohl kopat tento druh bloku!");
 					e.setCancelled(true);
 					return;
 				}
 			}
 		}
 		//*************************************************************************
 		String chestOwner;
 		int blockType = targetBlock.getTypeId();
 		if (blockType == ID_CHEST)
 		{
 			chestOwner = plugin.chests.GetBlockOwner(targetBlock);
 			if(chestOwner.matches("notFound"))
 			{
 				
 			}
 			else
 			{
 				e.setCancelled(true);
 				if (chestOwner.matches(p.getName()))
 				{
 					p.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Nemuzete rozbit zamcenou truhlu! Nejdrive ji odemknete.");
 				}
 				else
 				{
 					p.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Nemuzete rozbit zamcenou truhlu! " + chestOwner + " je vlastnikem teto truhly.");
 				}
 			}
 		}
 		else if(blockType==ID_DISPENSER)
 		{
 			chestOwner = plugin.dispensers.GetBlockOwner(targetBlock);
 			if(chestOwner.matches("notFound"))
 			{
 				
 			}
 			else
 			{
 				e.setCancelled(true);
 				if (chestOwner.matches(p.getName()))
 				{
 					p.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Nemuzete rozbit zamceny davkovac! Nejdrive jej odemknete.");
 				}
 				else
 				{
 					p.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Nemuzete rozbit zamceny davkovac! " + chestOwner + " je vlastnikem tohoto davkovace.");
 				}
 			}
 		}
 		else if(blockType==ID_FURNANCE || blockType==ID_FURNANCE_BURNING)
 		{
 			chestOwner = plugin.furnances.GetBlockOwner(targetBlock);
 			if(chestOwner.matches("notFound"))
 			{
 				
 			}
 			else
 			{
 				e.setCancelled(true);
 				if (chestOwner.matches(p.getName()))
 				{
 					p.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Nemuzete rozbit zamcenou pec! Nejdrive ji odemknete.");
 				}
 				else
 				{
 					p.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Nemuzete rozbit zamcenou pec! " + chestOwner + " je vlastnikem teto pece.");
 					}
 				}
 			}
 			int typeId = targetBlock.getTypeId();
 			if (!IsInstaBreakBlock(typeId))
 			{
 				prof.GainExperience(3);
 			}
 			if (typeId == 1)
 			{
 				GugaBonusDrop bonus = prof.CobbleStoneDrop();
 				if (bonus == GugaBonusDrop.DIAMOND)
 				{
 					p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(264,1));
 					prof.GainExperience(50);
 					p.sendMessage(ChatColor.BLUE+"[RPG]: "+ChatColor.WHITE+"Nasel jste diamant!");
 				}
 				else if (bonus == GugaBonusDrop.GOLD)
 				{
 					p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(14,1));
 					prof.GainExperience(40);
 					p.sendMessage(ChatColor.BLUE+"[RPG]: "+ChatColor.WHITE+"Nasel jste zlato!");
 				}
 				else if (bonus == GugaBonusDrop.IRON)
 				{
 					p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(15,1));
 					prof.GainExperience(30);
 					p.sendMessage(ChatColor.BLUE+"[RPG]: "+ChatColor.WHITE+"Nasel jste zelezo!");
 				}
 			}
 			/*else if ((typeId == 15) || (typeId == 14) || (typeId == 56))
 			{
 				int drops;
 				if (dropsCache.contains(targetBlock))
 				{
 					dropsCache.remove(dropsCache.indexOf(targetBlock));
 					return;
 				}
 				boolean bonusExp = false;
 				drops = ((GugaMiner)prof).BonusDrop(targetBlock);
 				int i = 0;
 				while (i< drops)
 				{
 					if (typeId ==56)
 					{
 						p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(264,1));
 					}
 					else
 					{
 						p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(targetBlock.getTypeId(),1));
 					}
 					bonusExp = true;
 					if (i == 1)
 					{
 						i++;
 					}//**************ON BLOCK PLACE**************
 					i++;
 				}
 				if (bonusExp)
 				{
 					prof.GainExperience(25);
 				}
 			}
 		}
 		else if (prof instanceof GugaHunter)
 		{
 			int typeId = targetBlock.getTypeId();
 			if (!IsInstaBreakBlock(typeId))
 			{*/
 				prof.GainExperience(1);
 			//}
 		//}
 		if (plugin.debug == true)
 		{
 			plugin.log.info("BLOCK_BREAK_EVENT: Time=" + ((System.nanoTime() - timeStart)/1000));
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockPlace(BlockPlaceEvent e)
 	{
 		if (plugin.debug)
 		{
 			plugin.log.info("BLOCK_PLACE_EVENT: typeID="+e.getBlock().getTypeId()+",PlayerName="+e.getPlayer().getName());
 		}
 		plugin.logger.LogBlockPlace(e);
 		if (plugin.arena.IsArena(e.getBlock().getLocation()))
 		{
 			if (!GameMasterHandler.IsAtleastRank(e.getPlayer().getName(), Rank.BUILDER))
 			{
 				e.setCancelled(true);
 				return;
 			}
 		}
 		if (plugin.EventWorld.IsEventWorld(e.getBlock().getLocation()))
 		{
 			if (!GameMasterHandler.IsAtleastRank(e.getPlayer().getName(), Rank.EVENTER))
 			{
 				e.setCancelled(true);
 				return;
 			}
 		}
 		if (!GugaRegionHandler.CanInteract(e.getPlayer(), e.getBlock().getX(), e.getBlock().getZ()))
 		{
 			if (!GameMasterHandler.IsAtleastRank(e.getPlayer().getName(), Rank.BUILDER))
 			{
 				e.setCancelled(true);
 				GugaRegion region = GugaRegionHandler.GetRegionByCoords(e.getBlock().getX(), e.getBlock().getZ());
 				e.getPlayer().sendMessage(ChatColor.BLUE+"[REGIONS]: "+ChatColor.WHITE+"Tady nemuzete stavet! Nazev pozemku: " + region.GetName());
 			}
 		}
 		//ID_CHEST
 		Player p=e.getPlayer();
 		Block block = e.getBlockPlaced();
 		if (block.getTypeId() == ID_CHEST)
 		{
 			if(plugin.chests.GetBlockOwner(block).equalsIgnoreCase("notFound"))
 			{
 				Block W=block.getRelative(BlockFace.WEST);
 				Block E=block.getRelative(BlockFace.EAST);
 				Block N=block.getRelative(BlockFace.NORTH);
 				Block S=block.getRelative(BlockFace.SOUTH);
 				if(E.getTypeId()==ID_CHEST || N.getTypeId()==ID_CHEST || S.getTypeId()==ID_CHEST || W.getTypeId()==ID_CHEST)
 				{
 					if((plugin.chests.GetBlockOwner(W).equalsIgnoreCase("notFound"))||plugin.chests.GetBlockOwner(W).equalsIgnoreCase(p.getName()))
 					{
 						plugin.chests.LockBlock(block,e.getPlayer().getName());
 						p.sendMessage(ChatColor.BLUE + "[AUTOLOCKER]: " + ChatColor.WHITE+"Vase dvojtruhla byla zamcena. Vice: /locker");
 						return;
 					}
 					else
 					{
 						p.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Nemuzete postavit truhlu vedle zamcene truhly!");
 						e.setCancelled(true);
 						return;
 					}
 				}
 				else
 				{
 					plugin.chests.LockBlock(block,e.getPlayer().getName());
 					p.sendMessage(ChatColor.BLUE + "[AUTOLOCKER]: " + ChatColor.WHITE+"Vase truhla byla zamcena. Vice: /locker");
 					return;
 				}
 			}
 		}
 		else if(block.getTypeId()==ID_DISPENSER)
 		{
 			if(plugin.dispensers.GetBlockOwner(block).equalsIgnoreCase("notFound"))
 			{
 				plugin.dispensers.LockBlock(block,e.getPlayer().getName());
 				p.sendMessage(ChatColor.BLUE + "[AUTOLOCKER]: " + ChatColor.WHITE+"Vas davkovac byl zamcen. Vice: /locker");
 			}
 		}
 		else if(block.getTypeId()==ID_FURNANCE || block.getTypeId()==ID_FURNANCE_BURNING)
 		{
 			if(plugin.furnances.GetBlockOwner(block).equalsIgnoreCase("notFound"))
 			{
 				plugin.furnances.LockBlock(block,e.getPlayer().getName());
 				p.sendMessage(ChatColor.BLUE + "[AUTOLOCKER]: " + ChatColor.WHITE+"Vase pec byla zamcena. Vice: /locker");
 			}
 		}
 			//***********************************************************************************************
 			/*Block relBlock = block.getRelative(BlockFace.WEST);
 			if (relBlock.getTypeId() == ID_CHEST)
 			{
 				if (!plugin.chests.GetBlockOwner(relBlock).equalsIgnoreCase("notFound"))
 				{
 					if(plugin.chests.GetBlockOwner(relBlock).equalsIgnoreCase(p.getName()))
 					{
 						plugin.chests.LockBlock(block,e.getPlayer().getName());
 						e.getPlayer().sendMessage("Vase truhla byla zamcena.");
 						return;
 					}
 					else
 					{
 						e.getPlayer().sendMessage("Nemuzete postavit truhlu vedle zamcene truhly!");
 						e.setCancelled(true);
 						return;
 					}
 				}
 			}
 			relBlock = block.getRelative(BlockFace.EAST);
 			if (relBlock.getTypeId() == ID_CHEST)
 			{
 				if (!plugin.chests.GetBlockOwner(relBlock).equalsIgnoreCase("notFound"))
 				{
 					if(plugin.chests.GetBlockOwner(relBlock).equalsIgnoreCase(p.getName()))
 					{
 						plugin.chests.LockBlock(block,e.getPlayer().getName());
 						e.getPlayer().sendMessage("Vase truhla byla zamcena.");
 						return;
 					}
 					else
 					{
 						e.getPlayer().sendMessage("Nemuzete postavit truhlu vedle zamcene truhly!");
 						e.setCancelled(true);
 						return;
 					}
 				}
 			}
 			relBlock = block.getRelative(BlockFace.SOUTH);
 			if (relBlock.getTypeId() == ID_CHEST)
 			{
 				if (!plugin.chests.GetBlockOwner(relBlock).equalsIgnoreCase("notFound"))
 				{
 					if(plugin.chests.GetBlockOwner(relBlock).equalsIgnoreCase(p.getName()))
 					{
 						plugin.chests.LockBlock(block,e.getPlayer().getName());
 						e.getPlayer().sendMessage("Vase truhla byla zamcena.");
 						return;
 					}
 					else
 					{
 						e.getPlayer().sendMessage("Nemuzete postavit truhlu vedle zamcene truhly!");
 						e.setCancelled(true);
 						return;
 					}
 				}
 			}
 			relBlock = block.getRelative(BlockFace.NORTH);
 			if (relBlock.getTypeId() == ID_CHEST)
 			{
 				if (!plugin.chests.GetBlockOwner(relBlock).equalsIgnoreCase("notFound"))
 				{
 					if(plugin.chests.GetBlockOwner(relBlock).equalsIgnoreCase(p.getName()))
 					{
 						plugin.chests.LockBlock(block,e.getPlayer().getName());
 						e.getPlayer().sendMessage("Vase truhla byla zamcena.");
 						return;
 					}
 					else
 					{
 						e.getPlayer().sendMessage("Nemuzete postavit truhlu vedle zamcene truhly!");
 						e.setCancelled(true);
 						return;
 					}
 				}
 			}
 		}*/
 		/*int typeId = e.getBlock().getTypeId();
 		if ((typeId == 14) || (typeId == 15) || (typeId == 56))
 		{
 			dropsCache.add(e.getBlock());
 		}*/
 	}
 	public boolean IsInstaBreakBlock(int blockId)
 	{
 		int i = 0;
 		while (i < instaBreakBlocks.length)
 		{
 			if (instaBreakBlocks[i] == blockId)
 			{
 				return true;
 			}
 			i++;
 		}
 		return false;
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockIgnite(BlockIgniteEvent e)
 	{
 		if(e.getCause() == IgniteCause.FLINT_AND_STEEL)
 		{
 			plugin.logger.LogBlockIgnite(e);
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockBurn(BlockBurnEvent e)
 	{
 		Block block = e.getBlock();
 		if (block.getTypeId() == 5)
 		{
 			Block relBlock;
 			BlockFace faceVals[] = BlockFace.values();
 			int i = 0;
 			while (i < faceVals.length)
 			{
 				relBlock = block.getRelative(faceVals[i]);
 				if (relBlock.getTypeId() == 51)
 				{
 					relBlock.setTypeId(0);
 				}
 				i++;
 			}
 			
 			e.setCancelled(true);
 			return;
 		}
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
 	}
 	private int ID_CHEST=54;
 	private int ID_DISPENSER=23;
 	private int ID_FURNANCE=61;
 	private int ID_FURNANCE_BURNING=62;
 	public int[] allowedBlocksTier1 = {1, 2, 3, 12, 13, 14, 15, 16, 17, 18, 24, 31, 32, 37, 38, 39, 40, 56, 78, 79, 81, 82};
 	public int[] instaBreakBlocks = {6, 18, 30, 31, 32, 37, 38, 39, 40, 50, 55, 59, 75, 76, 83};
 	public ArrayList<Block> dropsCache = new ArrayList<Block>();
 	public static Guga_SERVER_MOD plugin;
 }
