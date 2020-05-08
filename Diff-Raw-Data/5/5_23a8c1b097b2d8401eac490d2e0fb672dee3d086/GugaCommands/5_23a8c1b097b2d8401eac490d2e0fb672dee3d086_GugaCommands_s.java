 package me.Guga.Guga_SERVER_MOD.Handlers;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import me.Guga.Guga_SERVER_MOD.AutoSaver;
 import me.Guga.Guga_SERVER_MOD.BasicWorld;
 import me.Guga.Guga_SERVER_MOD.GameMaster;
 import me.Guga.Guga_SERVER_MOD.GameMaster.Rank;
 import me.Guga.Guga_SERVER_MOD.Book;
 import me.Guga.Guga_SERVER_MOD.GugaAnnouncement;
 import me.Guga.Guga_SERVER_MOD.GugaAuction;
 import me.Guga.Guga_SERVER_MOD.GugaBan;
 import me.Guga.Guga_SERVER_MOD.GugaDataPager;
 import me.Guga.Guga_SERVER_MOD.GugaEvent;
 import me.Guga.Guga_SERVER_MOD.GugaFile;
 import me.Guga.Guga_SERVER_MOD.GugaHunter;
 import me.Guga.Guga_SERVER_MOD.GugaInvisibility;
 import me.Guga.Guga_SERVER_MOD.GugaMiner;
 import me.Guga.Guga_SERVER_MOD.GugaParty;
 import me.Guga.Guga_SERVER_MOD.GugaProfession;
 import me.Guga.Guga_SERVER_MOD.GugaRegion;
 import me.Guga.Guga_SERVER_MOD.GugaSpectator;
 import me.Guga.Guga_SERVER_MOD.GugaTeams;
 import me.Guga.Guga_SERVER_MOD.GugaVirtualCurrency;
 import me.Guga.Guga_SERVER_MOD.Guga_SERVER_MOD;
 import me.Guga.Guga_SERVER_MOD.Homes;
 import me.Guga.Guga_SERVER_MOD.Places;
 import me.Guga.Guga_SERVER_MOD.Prices;
 import me.Guga.Guga_SERVER_MOD.GugaArena.ArenaSpawn;
 import me.Guga.Guga_SERVER_MOD.GugaArena.ArenaTier;
 import me.Guga.Guga_SERVER_MOD.GugaVirtualCurrency.VipItems;
 import me.Guga.Guga_SERVER_MOD.GugaMute;
 import me.Guga.Guga_SERVER_MOD.Listeners.GugaEntityListener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 public abstract class GugaCommands 
 {
 	public static void SetPlugin(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 	}
 	public static void TestCommand(String[] args)
 	{
 		Player p = plugin.getServer().getPlayer(args[0]);
 		if (p == null)
 			return;
 		String str = "HELLO NIGGER";
 		p.sendPluginMessage(plugin, "Guga", str.getBytes());
 		
 	}
 	public static void CommandWho(Player sender)
 	{
 		Player[] p = plugin.getServer().getOnlinePlayers();
 		String pName;
 		double playerX;
 		double playerZ;
 		double senderX = sender.getLocation().getX();
 		double senderZ = sender.getLocation().getZ();
 		
 		double distX;
 		double distZ;
 		double distance;
 		sender.sendMessage("******************************");
 		sender.sendMessage("HRACI ONLINE:");
 		int i = 0;
 		while (i < p.length)
 		{
 			pName = p[i].getName();
 			if (p[i].getName()!= sender.getName())
 			{
 				playerX = p[i].getLocation().getX();
 				playerZ = p[i].getLocation().getZ();
 				
 				distX = Math.abs(playerX-senderX);
 				distZ = Math.abs(playerZ-senderZ);
 				distance = Math.sqrt((distX*distX)+(distZ*distZ));
 				distance = Math.round(distance*10);
 				distance = distance/10;
 				String msg;
 				if (plugin.professions.get(p[i].getName()) != null)
 				{
 					msg = "- " + pName;
 					msg += /*"  Prof: " +plugin.professions.get(p[i].getName()).GetProfession() +*/ "   Level " + plugin.professions.get(p[i].getName()).GetLevel()+ "  " + distance + " bloku daleko";
 					if (plugin.FindPlayerCurrency(pName).IsVip())
 					{
 						sender.sendMessage(ChatColor.GOLD + msg);
 					}
 					else
 					{
 						sender.sendMessage(msg);
 					}
 				}
 				else
 				{
 					msg = "- " + pName + "  " + distance + " bloku daleko";
 					if (plugin.FindPlayerCurrency(pName).IsVip())
 					{
 						sender.sendMessage(ChatColor.GOLD + msg);
 					}
 					else
 					{
 						sender.sendMessage(msg);
 					}
 				}
 			}
 			else
 			{
 				String msg;
 				if (plugin.professions.get(p[i].getName()) != null)
 				{
 					msg = "- " + pName;
 					msg += /*"  Prof: " +plugin.professions.get(p[i].getName()).GetProfession() +*/ "   Level " + plugin.professions.get(p[i].getName()).GetLevel();
 					if (plugin.FindPlayerCurrency(pName).IsVip())
 					{
 						sender.sendMessage(ChatColor.GOLD + msg);
 					}
 					else
 					{
 						sender.sendMessage(msg);
 					}
 				}
 				else
 				{
 					msg = "- " + pName;
 
 					if (plugin.FindPlayerCurrency(pName).IsVip())
 					{
 						sender.sendMessage(ChatColor.GOLD + msg);
 					}
 					else
 					{
 						sender.sendMessage(msg);
 					}
 				}
 			}
 			
 			i++;
 		}
 		sender.sendMessage("******************************");
 		
 	}	
 	public static void CommandHelp(Player sender)
 	{
 		//log.info("<"+sender.getName() + " has used /help Command>");
 		sender.sendMessage("******************************");
 		sender.sendMessage("GUGA MINECRAFT SERVER MOD "+Guga_SERVER_MOD.version);
 		sender.sendMessage("******************************");
 		sender.sendMessage("Seznam prikazu:");
 		sender.sendMessage(ChatColor.AQUA + " /lock " + ChatColor.WHITE + "- Zamkne block (info v /locker).");
 		sender.sendMessage(ChatColor.AQUA + " /unlock  " + ChatColor.WHITE + "- Odemkne block (info v /locker).");
 		sender.sendMessage(ChatColor.AQUA + " /who  " + ChatColor.WHITE + "-  Seznam online hracu.");
 		sender.sendMessage(ChatColor.AQUA + " /login " + ChatColor.GRAY + "<heslo>  " + ChatColor.WHITE + "-  Prihlasi zaregistrovaneho hrace.");
 		sender.sendMessage(ChatColor.AQUA + " /register " + ChatColor.GRAY + "<pass>  " + ChatColor.WHITE + "-  Zaregistruje noveho hrace.");
 		sender.sendMessage(ChatColor.AQUA + " /password " + ChatColor.GRAY + "<stare_heslo> <nove_heslo>  " + ChatColor.WHITE + "-  Zmeni heslo.");
 		sender.sendMessage(ChatColor.AQUA + " /rpg  " + ChatColor.WHITE + "-  Menu Profesi.");
 		sender.sendMessage(ChatColor.AQUA + " /arena  " + ChatColor.WHITE + "-  Menu areny.");
 		sender.sendMessage(ChatColor.AQUA + " /eventworld  " + ChatColor.WHITE + "-  Menu EventWorldu.");
 		sender.sendMessage(ChatColor.AQUA + " /shop  " + ChatColor.WHITE + "-  Menu Obchodu.");
 		sender.sendMessage(ChatColor.AQUA + " /vip  " + ChatColor.WHITE + "-  VIP menu.");
 		sender.sendMessage(ChatColor.AQUA + " /places " + ChatColor.WHITE + "- Menu mist, kam se da teleportovat.");
 		sender.sendMessage(ChatColor.AQUA + " /party " + ChatColor.WHITE + "- Prikazy pro party");
 		//sender.sendMessage(ChatColor.AQUA + " /ah " + ChatColor.WHITE + "- Menu Aukce.");
 		sender.sendMessage(ChatColor.AQUA + " /r " + ChatColor.GRAY + "<message> " + ChatColor.WHITE + "-  Odpoved na whisper.");
 		sender.sendMessage(ChatColor.AQUA + " /feedback " + ChatColor.GRAY + "<text> " + ChatColor.WHITE + "-  Odesle zpetny odkaz administratorum serveru. Napr. bugy/napady na vylepseni.");
 		if (GameMasterHandler.IsAdmin(sender.getName()))
 		{
 			sender.sendMessage(ChatColor.AQUA + " /gm " + ChatColor.WHITE + "- GameMaster's menu.");
 			sender.sendMessage(ChatColor.AQUA + "/event " + ChatColor.WHITE + "- Event menu.");
 		}
 		sender.sendMessage("******************************");
 		sender.sendMessage("Created by Guga 2011");
 		sender.sendMessage("******************************");
 	}
 	public static void CommandHome(Player p, String args[])
 	{
 		if(!(p.getWorld().getName().matches("world") || p.getWorld().getName().matches("world_basic")))
 		{
 			ChatHandler.FailMsg(p, "Tento prikaz zde nelze pouzit!");
 			return;
 		}
 		if(args.length == 0)
 		{
 			Homes home;
 			if((home = HomesHandler.getHomeByPlayer(p.getName())) != null)
 			{
 				p.teleport(HomesHandler.getLocation(home));
 				ChatHandler.SuccessMsg(p, "Byl jste teleportovan na home!");
 			}
 			else
 			{
 				ChatHandler.FailMsg(p, "Vas home jeste nebyl nastaven!");
 			}
 		}
 		else if(args.length == 1)
 		{
 			if(args[0].equalsIgnoreCase("set"))
 			{
 				if(p.getWorld().getName().matches("world") || p.getWorld().getName().matches("world_basic"))
 				{
 					HomesHandler.addHome(p);
 					ChatHandler.SuccessMsg(p, "Vas home byl nastaven!");
 				}
 				else
 				{
 					ChatHandler.FailMsg(p, "V tomto svete si nemuzete nastavit home!");
 				}
 			}
 			else
 			{
 				ChatHandler.FailMsg(p, "Prikaz home neprebira tento argument!");
 			}
 		}
 		else
 		{
 			ChatHandler.FailMsg(p, "Prikaz home neprebira dalsi argumenty!");
 		}
 	}
 	public static void CommandLocker(Player sender)
 	{
 		sender.sendMessage(ChatColor.BLUE+"***********");
 		sender.sendMessage(ChatColor.BLUE+"LOCKER");
 		sender.sendMessage(ChatColor.BLUE+"***********");
 		sender.sendMessage("Zamcit muzete bednu, davkovac a pec!");
 		sender.sendMessage("Blocky se zamykaji automaticky pri polozeni!");
 		sender.sendMessage("PRIKAZY:");
 		sender.sendMessage(ChatColor.AQUA + "/lock " + ChatColor.WHITE + "- zamce block");
 		sender.sendMessage(ChatColor.AQUA + "/unlock " + ChatColor.WHITE + "- odemce block");
 	}
 	public static void CommandLock(Player sender)
 	{
 		Block chest = sender.getTargetBlock(null, 10);
 		int blockType = chest.getTypeId(); // chest = 54
 		if (blockType == ID_CHEST)
 		{
 			if (plugin.chests.GetBlockOwner(chest).matches("notFound"))
 			{
 				plugin.chests.LockBlock(chest,sender.getName());
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Vase truhla byla zamcena.");
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Truhlu jiz nekdo zamkl!");
 			}
 		}	
 		else if(blockType == ID_DISPENSER)
 		{
 			if (plugin.dispensers.GetBlockOwner(chest).matches("notFound"))
 			{
 				plugin.dispensers.LockBlock(chest,sender.getName());
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Vas davkovac byl zamcen.");
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Davkovac jiz nekdo zamkl!");
 			}
 		}
 		else if(blockType == ID_FURNANCE || blockType == ID_FURNANCE_BURNING)
 		{
 			if (plugin.furnances.GetBlockOwner(chest).matches("notFound"))
 			{
 				plugin.furnances.LockBlock(chest,sender.getName());
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Vase pec byla zamcena.");
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Pec jiz nekdo zamkl!");
 			}
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.BLUE+"[LOCKER]:"+ChatColor.WHITE+" Tento block nelze zamcit!");
 		}
 	}
 	public static void CommandConfirm(Player sender, String args[])
 	{
 		Player p = vipTeleports.get(sender);
 		GugaVirtualCurrency vip = plugin.FindPlayerCurrency(p.getName());
 		if (p != null && vip != null)
 		{
 			if (GugaEvent.ContainsPlayer(p.getName()))
 			{
 				sender.sendMessage(ChatColor.GREEN + "[TELEPORT]: Hrac se nemuze teleportovat v prubehu Eventu!");
 				p.sendMessage(ChatColor.GREEN + "[TELEPORT]: Nemuzete se teleportovat v prubehu Eventu!");
 				return;
 			}
 			if (plugin.arena.IsArena(p.getLocation()))
 			{
 				sender.sendMessage(ChatColor.GREEN + "[TELEPORT]: Hrac nemuze pouzit teleport v Arene!");
 				p.sendMessage(ChatColor.GREEN + "[TELEPORT]: Nemuzete pouzit teleport v Arene!");
 				return;
 			}
 			vip.SetLastTeleportLoc(p.getLocation());
 			p.teleport(sender);
 			vipTeleports.remove(sender);
 			
 			sender.sendMessage(ChatColor.GREEN + "[TELEPORT]: Teleport prijmut!");
 		}
 		else
 			sender.sendMessage("Nemate zadny pozadavek na teleport!");
 	}
 	public static void CommandUnlock(Player sender)
 	{
 		Block chest = sender.getTargetBlock(null, 10);
 		int blockType = chest.getTypeId(); // chest = 54
 
 		if (blockType == ID_CHEST)
 		{
 			if ( (plugin.chests.GetBlockOwner(chest).matches(sender.getName())) || (GameMasterHandler.IsAtleastGM(sender.getName())) )
 			{
 				plugin.chests.UnlockBlock(chest,sender.getName());
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]: "+ChatColor.WHITE+"Vase truhla byla odemcena.");
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]: "+ChatColor.WHITE+"Tuto truhlu nemuzete odemknout!");
 			}
 		}
 		else if(blockType==ID_DISPENSER)
 		{
 			if ( (plugin.dispensers.GetBlockOwner(chest).matches(sender.getName())) || (GameMasterHandler.IsAtleastGM(sender.getName())) )
 			{
 				plugin.dispensers.UnlockBlock(chest,sender.getName());
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]: "+ChatColor.WHITE+"Vas davkovac byl odemcen.");
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]: "+ChatColor.WHITE+"Tento davkovac nemuzete odemknout!");
 			}
 		}
 		else if(blockType==ID_FURNANCE)
 		{
 			if ( (plugin.furnances.GetBlockOwner(chest).matches(sender.getName())) || (GameMasterHandler.IsAtleastGM(sender.getName())) )
 			{
 				plugin.furnances.UnlockBlock(chest,sender.getName());
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]: "+ChatColor.WHITE+"Vase pec byla odemcena.");
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.BLUE+"[LOCKER]: "+ChatColor.WHITE+"Tuto pec nemuzete odemknout!");
 			}
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.BLUE+"[LOCKER]: "+ChatColor.WHITE+"Tento block nelze odemcit!");
 		}
 		
 	}
 	public static void CommandShop(Player sender, String args[])
 	{
 		if (!plugin.acc.UserIsLogged(sender))
 		{
 			sender.sendMessage("Nejdrive se prihlaste!");
 			return;
 		}
 		if (plugin.arena.IsArena(sender.getLocation()))
 		{
 			sender.sendMessage("V arene nemuzete pouzit prikaz /shop!");
 			return;
 		}
 		if (plugin.EventWorld.IsEventWorld(sender.getLocation()))
 		{
 			sender.sendMessage("V EventWorldu nemuzete pouzit prikaz /shop!");
 			return;
 		}
 		GugaVirtualCurrency p = plugin.FindPlayerCurrency(sender.getName());
 		if (args.length == 0)
 		{
 			sender.sendMessage("Shop Menu:");
 			sender.sendMessage("/shop info  -  Info o Obchodu.");
 			sender.sendMessage("/shop buy <nazev>  -  Koupi dany item (1).");
 			sender.sendMessage("/shop balance  -  Zobrazi vase kredity.");
 			sender.sendMessage("/shop items <strana>  -  Seznam itemu, ktere se daji koupit.");
 		}
 		else if (args.length == 1)
 		{
 			String subCommand = args[0];
 			if (subCommand.matches("info"))
 			{
 				sender.sendMessage("not yet");
 			}
 			else if (subCommand.matches("balance"))
 			{
 				sender.sendMessage("Vas ucet:");
 				sender.sendMessage("Kredity: " + p.GetCurrency());
 			}
 		}
 		else if (args.length == 2)
 		{
 			String subCommand = args[0];
 			String arg1 = args[1];
 			
 			if (subCommand.matches("buy"))
 			{
 				p.BuyItem(arg1, 1);
 			}
 			else if(subCommand.matches("items"))
 			{
 				ArrayList<Prices> prices = new ArrayList<Prices>();
 				for (Prices i : Prices.values())
 				{
 					prices.add(i);
 				}
 				GugaDataPager<Prices> pager = new GugaDataPager<Prices>(prices, 15);
 				Iterator<Prices> i = pager.GetPage(Integer.parseInt(args[1])).iterator();
 				sender.sendMessage("SEZNAM ITEMU:");
 				sender.sendMessage("STRANA " + args[1] + "/" + pager.GetPagesCount());
 				while (i.hasNext())
 				{
 					Prices item = i.next();
 					if (item == Prices.KRUMPAC_EFFICIENCY_V)
 						sender.sendMessage(ChatColor.GOLD + item.toString() +" -    cena: "+ item.GetItemPrice() + " Diamantovy krumpac s Efficiency V + Unbreaking III");
 					else
 						sender.sendMessage(item.toString() +" -    cena: "+ item.GetItemPrice() + ChatColor.YELLOW + " po: " + item.GetAmmount());
 				}
 			}
 		}
 		/*else if (args.length == 3)
 		{
 			String subCommand = args[0];
 			String arg1 = args[1]; // item
 			String arg2 = args[2]; // amount
 			if (subCommand.matches("buy"))
 			{
 				p.BuyItem(arg1, Integer.parseInt(arg2));
 			}
 			
 		}*/
 	}
 	public static void CommandFly(Player sender, String args[])
 	{
 		/*if (!plugin.acc.UserIsLogged(sender))
 		{
 			sender.sendMessage("Nejprve se musite prihlasit!");
 			return;
 		}
 		if (GugaEvent.ContainsPlayer(sender.getName()))
 		{
 			sender.sendMessage("Nemuzete pouzivat FLY prikazy v prubehu eventu!");
 			return;
 		}
 		if (plugin.arena.IsArena(sender.getLocation()))
 		{
 			sender.sendMessage("FLY Prikazy nemuzete pouzivat v arene!");
 			return;
 		}
 		if (plugin.EventWorld.IsEventWorld(sender.getLocation()))
 		{
 			sender.sendMessage("FLY Prikazy nemuzete pouzivat v EventWorldu!");
 			return;
 		}
 		if(args.length == 0)
 		{
 			sender.sendMessage(ChatColor.AQUA+" MENU LETANI:");
 			if(!GugaFlyHandler.isFlying(sender.getName()))
 			{
 				sender.sendMessage(ChatColor.GOLD + " CENIK:");
 				sender.sendMessage(" *Letani na 2 hodiny, CENA: 250 kreditu.");
 				sender.sendMessage(" *Letani na 4 hodiny, CENA: 450 kreditu.");
 				sender.sendMessage(" *Letani na 8 hodin, CENA: 650 kreditu.");
 				sender.sendMessage(" *Letani na 24 hodin, CENA: 950 kreditu.");
 				sender.sendMessage(" /fly activate <pocetHodin> - Aktuvuje letani na urcity pocet hodin.");
 			}
 			else
 			{
 				sender.sendMessage( "/fly on - zapne letani.");
 				sender.sendMessage( "/fly off - vypte letani.");
 			}	
 		}
 		else if((args.length == 1) && (GugaFlyHandler.isFlying(sender.getName())))
 		{
 			if(args[0].equalsIgnoreCase("on"))
 			{
 				sender.setAllowFlight(true);
 				sender.setFlying(true);
 				sender.sendMessage("Letani bylo zapnuto!");
 			}
 			else if(args[0].equalsIgnoreCase("off"))
 			{
 				sender.setAllowFlight(false);
 				sender.setFlying(false);
 				sender.sendMessage("Letani bylo vypnuto!");
 			}
 		}
 		else if((args.length == 2) && (!GugaFlyHandler.isFlying(sender.getName())))
 		{
 			GugaVirtualCurrency c = plugin.FindPlayerCurrency(sender.getName());
 			int currency = c.GetCurrency();
 			if(args[0].equalsIgnoreCase("activate"))
 			{
 				if(args[1].equalsIgnoreCase("2"))
 				{
 					if(currency >= 250)
 					{
 						c.RemoveCurrency(250);
 						GugaFlyHandler.AddFlyingPlayer(sender.getName(), System.currentTimeMillis() + (2*60*60*1000));
 						sender.sendMessage("Letani bylo aktivovano");
 						sender.setAllowFlight(true);
 						sender.setFlying(true);
 					}
 				}
 				else if(args[1].equalsIgnoreCase("4"))
 				{
 					if(currency >= 450)
 					{
 						c.RemoveCurrency(450);
 						GugaFlyHandler.AddFlyingPlayer(sender.getName(), System.currentTimeMillis() + (4*60*60*1000));
 						sender.sendMessage("Letani bylo aktivovano");
 						sender.setAllowFlight(true);
 						sender.setFlying(true);
 					}
 				}
 				else if(args[1].equalsIgnoreCase("8"))
 				{
 					if(currency >= 650)
 					{
 						c.RemoveCurrency(650);
 						GugaFlyHandler.AddFlyingPlayer(sender.getName(), System.currentTimeMillis() + (8*60*60*1000));
 						sender.sendMessage("Letani bylo aktivovano");
 						sender.setAllowFlight(true);
 						sender.setFlying(true);
 					}
 				}
 				else if(args[1].equalsIgnoreCase("24"))
 				{
 					if(currency >= 950)
 					{
 						c.RemoveCurrency(950);
 						GugaFlyHandler.AddFlyingPlayer(sender.getName(), System.currentTimeMillis() + (24*60*60*1000));
 						sender.sendMessage("Letani bylo aktivovano");
 						sender.setAllowFlight(true);
 						sender.setFlying(true);
 					}
 				}
 				GugaFlyHandler.SaveFly();
 			}
 		}*/
 	}
 	public static void CommandVIP(Player sender, String args[])
 	{
 		GugaVirtualCurrency vip = plugin.FindPlayerCurrency(sender.getName());
 		if (!plugin.acc.UserIsLogged(sender))
 		{
 			sender.sendMessage("Nejprve se musite prihlasit!");
 			return;
 		}
 		if (!vip.IsVip())
 		{
 			sender.sendMessage("Pouze VIP mohou pouzivat tento prikaz!");
 			return;
 		}
 		if (GugaEvent.ContainsPlayer(sender.getName()))
 		{
 			sender.sendMessage("Nemuzete pouzivat VIP prikazy v prubehu eventu!");
 			return;
 		}
 		if (plugin.arena.IsArena(sender.getLocation()))
 		{
 			sender.sendMessage("VIP Prikazy nemuzete pouzivat v arene!");
 			return;
 		}
 		if (plugin.EventWorld.IsEventWorld(sender.getLocation()))
 		{
 			sender.sendMessage("VIP Prikazy nemuzete pouzivat v EventWorldu!");
 			return;
 		}
 		if (BasicWorld.IsBasicWorld(sender.getLocation()))
 		{
 			sender.sendMessage("VIP Prikazy nemuzete pouzivat ve svete pro novacky!");
 			return;
 		}
 		if (plugin.AdventureWorld.IsAdventureWorld(sender.getLocation()))
 		{
 			sender.sendMessage("VIP Prikazy nemuzete pouzivat v AdventureWorldu!");
 			return;
 		}
 		if (args.length == 0)
 		{
 			sender.sendMessage("VIP MENU:");
 			sender.sendMessage("/vip expiration  -  Zobrazi, kdy vyprsi vas VIP status.");
 			sender.sendMessage("/vip tp  -  Teleport podprikaz.");
 			sender.sendMessage("/vip time  -  Podprikaz zmeny casu.");
 			sender.sendMessage("/vip item  -  Podprikaz itemu.");
 			sender.sendMessage("/vip nohunger - Utisi Vas hlad.");
 			sender.sendMessage("/vip fly - Podprikaz letani.");
 		}
 		else if (args.length == 1)
 		{
 			String subCommand = args[0];
 			if (subCommand.matches("expiration"))
 			{
 				sender.sendMessage("Vase VIP vyprsi: " + new Date(vip.GetExpirationDate()));
 			}
 			else if (subCommand.matches("tp"))
 			{
 				sender.sendMessage("Teleport Menu:");
 				sender.sendMessage("/vip tp player <jmeno>  -  Teleport k danemu hraci.");
 				sender.sendMessage("/vip tp spawn  -  Teleport na spawn.");
 				sender.sendMessage("/vip tp back  -  Teleport zpet na predchozi pozici.");
 				sender.sendMessage("/vip tp bed  -  Teleport k posteli.");
 				sender.sendMessage("/vip tp death  -  Teleportuje vas na posledni misto smrti.");
 			}
 			else if (subCommand.matches("time"))
 			{
 				sender.sendMessage("Time Menu:");
 				sender.sendMessage("/vip time set <hodnota>  -  Nastavi cas na 0-24000.");
 				sender.sendMessage("/vip time reset  -  Zmeni cas zpet na serverovy cas.");
 			}
 			else if (subCommand.matches("item"))
 			{
 				sender.sendMessage("Item Menu:");
 				sender.sendMessage("/vip item add <itemID>  -  Prida stack daneho itemu.");
 				sender.sendMessage("/vip item list - Vypise vsechny dostupne itemy a jejich ID.");
 			}
 			else if(subCommand.matches("nohunger"))
 			{
 				sender.setFoodLevel(20);
 				sender.setSaturation(20);
 				sender.sendMessage("Uspesne jste se najedli");
 			}
 			else if(subCommand.matches("fly"))
 			{
 				sender.sendMessage("VIP FLY MENU:");
 				sender.sendMessage("/vip fly on - Zapne letani.");
 				sender.sendMessage("/vip fly off - Vypne letani.");
 			}
 		}
 		else if (args.length == 2)
 		{
 			String subCommand = args[0];
 			String arg1 = args[1];
 			if (subCommand.matches("tp"))
 			{
 				if (arg1.matches("back"))
 				{
 					Location locCache = sender.getLocation();
 					Location tpLoc = vip.GetLastTeleportLoc();
 					if (tpLoc == null)
 					{
 						sender.sendMessage("Nejdrive se musite nekam teleportovat!");
 						return;
 					}
 					sender.teleport(tpLoc);
 					vip.SetLastTeleportLoc(locCache);
 				}
 				else if (arg1.matches("spawn"))
 				{
 					vip.SetLastTeleportLoc(sender.getLocation());
 					sender.teleport(sender.getWorld().getSpawnLocation());
 				}
 				else if (arg1.matches("bed"))
 				{
 					vip.SetLastTeleportLoc(sender.getLocation());
 					Location loc = sender.getBedSpawnLocation();
 					Location tpLoc = loc;
 					boolean canTeleport = false;
 					int i = loc.getBlockY();
 					while (!canTeleport)
 					{
 						loc = tpLoc;
 						loc.add(0, 1, 0);
 						if (loc.getBlock().getTypeId() == 0)
 						{
 							if (loc.getBlock().getRelative(BlockFace.UP).getTypeId() == 0)
 							{
 								tpLoc = loc;
 								break;
 							}
 						}
 						if (i >= 127)
 						{
 							break;
 						}
 						i++;
 					}
 					sender.teleport(tpLoc);
 				}
 				else if (arg1.matches("death"))
 				{
 					if(GugaEntityListener.playersDeaths.containsKey(sender.getName()))
 					{
 						sender.teleport(GugaEntityListener.playersDeaths.get(sender.getName()));
 						ChatHandler.SuccessMsg(sender, "Byl jsi uspesne teleportovan na misto posledni smrti!");
 					}
 					else
 					{
 						ChatHandler.FailMsg(sender, "Misto smrti zatim neexistuje!");
 					}
 				}
 				else if (arg1.matches("endtrapka"))
 				{
 					sender.chat("/pp endtrapka");
 				}
 			}
 			else if (subCommand.matches("item"))
 			{
 				if (args[1].equalsIgnoreCase("list"))
 				{
 					sender.sendMessage("SEZNAM ITEMU: (Nazev - ID)");
 					for (VipItems i : VipItems.values())
 					{
 						sender.sendMessage(i.toString() + " - " + i.GetID());
 					}
 				}
 			}
 			else if (subCommand.matches("time"))
 			{
 				if (arg1.matches("reset"))
 				{
 					if (!sender.isPlayerTimeRelative())
 					{
 						sender.resetPlayerTime();
 						sender.sendMessage("Cas byl restartovan");
 					}
 					else
 						sender.sendMessage("Vas cas nepotrebuje restartovat!");
 				}
 			}
 			else if (subCommand.matches("fly"))
 			{
 				GugaVirtualCurrency curr = plugin.FindPlayerCurrency(sender.getName()); 
 				if(args[1].matches("on"))
 				{
 					curr.ToggleFly(true);
 					ChatHandler.SuccessMsg(sender, "Letani zapnuto!");
 				}
 				else if(args[1].matches("off"))
 				{
 					curr.ToggleFly(false);
 					ChatHandler.SuccessMsg(sender, "Letani vypnuto!");
 				}
 			}
 		}
 		else if (args.length == 3)
 		{
 			String subCommand = args[0];
 			String arg1 = args[1];
 			String arg2 = args[2];
 			if (subCommand.matches("tp"))
 			{
 				if (arg1.matches("player"))
 				{
 					/*vip.SetLastTeleportLoc(sender.getLocation());
 					Player p = plugin.getServer().getPlayer(arg2);
 					sender.teleport(p);*/
 					Player p = plugin.getServer().getPlayer(arg2);
 					if (p == null)
 					{
 						sender.sendMessage("Tento hrac neni online!");
 						return;
 					}
 					if (p.getLocation().getWorld().getName().matches("world_basic"))
 					{
 						ChatHandler.FailMsg(sender, "Tento hrac je ve svete pro novacky!");
 						return;
 					}
 					if (p.getLocation().getWorld().getName().matches("arena"))
 					{
 						ChatHandler.FailMsg(sender, "Tento hrac je v arene!");
 						return;
 					}
 					if (p.getLocation().getWorld().getName().matches("world_event"))
 					{
 						ChatHandler.FailMsg(sender, "Tento hrac je v EW!");
 						return;
 					}
 					plugin.getServer().getPlayer(arg2).sendMessage(ChatColor.GREEN + "[TELEPORT]: Hrac " + sender.getName() + " se na vas chce teleportovat, pro prijmuti napiste prikaz /y");
 					vipTeleports.put(p, sender);
 					sender.sendMessage(ChatColor.GREEN + "[TELEPORT]: Pozadavek odeslan");
 				}
 			}
 			else if (subCommand.matches("item"))
 			{
 				if (arg1.matches("add"))
 				{
 					int itemID = Integer.parseInt(arg2);
 					if (VipItems.IsVipItem(itemID))
 					{
 						ItemStack item = new ItemStack(itemID, 64);
 						PlayerInventory pInventory = sender.getInventory();
 						pInventory.addItem(item);
 						sender.sendMessage("Item pridan!");
 					}
 					else
 						sender.sendMessage("Tento item nejde pridat!");
 				}
 			}
 			else if (subCommand.matches("time"))
 			{
 				if (arg1.matches("set"))
 				{
 					int time = Integer.parseInt(arg2);
 					if ( (time >= 0) && (time <= 24000) )
 					{
 						sender.setPlayerTime(time, false);
 						sender.sendMessage("Cas byl uspesne zmenen");
 					}
 					else 
 						sender.sendMessage("Tato hodnota nelze nastavit!");
 				}
 			}
 		}
 	}
 	public static void CommandRpg(Player sender, String args[])
 	{
 		if (args.length == 0)
 		{
 			sender.sendMessage("RPG MENU:");
 			sender.sendMessage("/rpg status  -  Zobrazi vas status.");
 			sender.sendMessage("/rpg skills  -  Zobrazi vase bonusy.");
 		//	sender.sendMessage("/rpg select <miner/hunter>  -  Vybere profesi.");
 			sender.sendMessage("/rpg info  -  Info o profesi.");
 		//	sender.sendMessage("/rpg info <miner/hunter>  -  Info o dane profesi.");*/
 		}
 		else if (args.length == 1)
 		{
 			String subCommand = args[0];
 			if (subCommand.matches("status"))
 			{
 				GugaProfession prof;
 				if ((prof = plugin.professions.get(sender.getName())) != null)
 				{
 					int lvl = prof.GetLevel();
 					int xp = prof.GetXp();
 					int xpNeeded = prof.GetXpNeeded();
 					sender.sendMessage("********************");
 					sender.sendMessage("**Level:" + lvl);
 					sender.sendMessage("**XP:" + xp + "/" + xpNeeded);
 					sender.sendMessage("********************");
 					sender.sendMessage("********************");
 				}
 				else 
 				{
 					sender.sendMessage("Nejdrive si musite zvolit profesi!");
 				}
 			}
 			else if (subCommand.matches("skills"))
 			{
 				GugaProfession prof;
 				if ((prof = plugin.professions.get(sender.getName())) == null)
 				{
 					sender.sendMessage("Nejdrive si musite zvolit profesi!");
 				}
 					int chance[] = prof.GetChances();
 					//int bonus[] = miner.GetBonusDrops();
 					int iron = chance[plugin.IRON];
 					int gold = chance[plugin.GOLD];
 					int diamond = chance[plugin.DIAMOND];
 					int emerald = chance[plugin.EMERALD];
 					double chanceIron = ( (double)iron / (double)1000 )  * (double)100;
 					double chanceGold = ( (double)gold / (double)1000 )  * (double)100;
 					double chanceDiamond = ( (double)diamond/ (double)1000 )  * (double)100;
 					double chanceEmerald = ( (double)emerald/ (double)1000 )  * (double)100;
 					sender.sendMessage("********************");
 					sender.sendMessage("**Sance na nalezeni ve stonu:");
 					sender.sendMessage("**Iron: " + chanceIron + "%");
 					sender.sendMessage("**Gold: " + chanceGold + "%");
 					sender.sendMessage("**Diamond: " + chanceDiamond + "%");
 					sender.sendMessage("**Emerald: " + chanceEmerald + "%");
 					sender.sendMessage("********************");
 					
 					/*iron = bonus[plugin.IRON];
 					gold = bonus[plugin.GOLD];
 					diamond = bonus[plugin.DIAMOND];
 					
 					sender.sendMessage("**Bonusove dropy z:");
 					sender.sendMessage("**Iron: +" + iron);
 					sender.sendMessage("**Gold: +" + gold);
 					sender.sendMessage("**Diamond: +" + diamond);
 					
 					sender.sendMessage("********************");
 					sender.sendMessage("********************");*/
 					
 				//}
 				/*else if (prof instanceof GugaHunter)
 				{
 					GugaHunter hunter = (GugaHunter)prof;
 					double regen = ((double)hunter.GetHpRegen())/2;
 					int dmg = hunter.GetDamageIncrease();
 					sender.sendMessage("********************");
 					sender.sendMessage("**HP Regen: " + regen + "hp za minutu");
 					sender.sendMessage("**Zvyseny damage: " + dmg);
 					sender.sendMessage("********************");
 					sender.sendMessage("********************");
 				}*/
 			}
 			else if (subCommand.matches("select"))
 			{
 				sender.sendMessage("Prosim uvedte profesi kterou chcete! Miner nebo Hunter");
 			}
 			else if (subCommand.matches("info"))
 			{
 				/*sender.sendMessage("********************");
 				sender.sendMessage("**Profesi si muzete vybrat tak, ze napisete");
 				sender.sendMessage("** /rpg select <vase_profese>");
 				sender.sendMessage("**Mate na vyber ze dvou profesi:");
 				sender.sendMessage("**      -Hunter a Miner");
 				sender.sendMessage("**Kazda profese ma jine bonusy");*/
 				sender.sendMessage("**XP ziskavate za zabijeni monster a kopani");
 			//	sender.sendMessage("**Kazda profese dostava rozdilny pocet XP.");
 				sender.sendMessage("**Maximalni level: " + new GugaProfession().GetLvlCap());
 			}
 		}
 		else if (args.length == 2)
 		{
 			String subCommand = args[0];
 			String arg1 = args[1];
 			
 			if (subCommand.matches("select"))
 			{
 				if (arg1.matches("hunter"))
 				{
 					if (plugin.professions.get(sender.getName()) == null)
 					{
 						GugaHunter prof = new GugaHunter(sender.getName(),0,plugin);
 						plugin.professions.put(sender.getName(), prof);
 						prof.StartRegenHp();
 						sender.sendMessage("Stal jste se Hunterem!");
 					}
 					else 
 					{
 						sender.sendMessage("Nemuzete si znovu zvolit profesi!");
 					}
 				}
 				else if (arg1.matches("miner"))
 				{
 					if (plugin.professions.get(sender.getName()) == null)
 					{
 						GugaMiner prof = new GugaMiner(sender.getName(),0,plugin);
 						plugin.professions.put(sender.getName(), prof);
 						sender.sendMessage("Stal jste se Minerem!");
 					}
 					else 
 					{
 						sender.sendMessage("Nemuzete si znovu zvolit profesi!");
 					}
 				}
 				else 
 				{
 					sender.sendMessage("Toto neni profese!");
 				}
 			}
 			else if (subCommand.matches("info"))
 			{
 				/*if (arg1.matches("hunter"))
 				{
 					sender.sendMessage("********************");
 					sender.sendMessage("**Hunterovo Bonusy:");
 					sender.sendMessage("** - Hp Regen (+0,5 kazde 2 levely)");
 					sender.sendMessage("** - Bonus Damage (+1 kazde 4 levely)");
 					sender.sendMessage("********************");
 					sender.sendMessage("********************");
 					
 				}*/
 			//	else if (arg1.matches("miner"))
 			//	{
 					/*sender.sendMessage("********************");
 					sender.sendMessage("**Minerovo Bonusy:");
 					sender.sendMessage("** - Zvysene dropy z:");
 					sender.sendMessage("**      -Iron (+1 every 6 levels)");
 					sender.sendMessage("**      -Gold (+1 every 8 levels)");
 					sender.sendMessage("**      -Diamond (+1 every 10 levels)");*/
 					sender.sendMessage("********************");
 					sender.sendMessage("** - Sance vzacneho dropu ze Stone:");
 					sender.sendMessage("**      -Iron (+0.1% kazdych 10 levelu)");
 					sender.sendMessage("**      -Gold (+0.1% kazdych 20 levelu)");
 					sender.sendMessage("**      -Diamond (+0.1% kazdych 50 levelu)");
 					sender.sendMessage("********************");
 			//	}
 			}
 		}
 		else if (args.length == 3)
 		{
 			String subCommand = args[0];
 			String player = args[1];
 			String xp = args[2];
 			if (subCommand.matches("xp") && sender instanceof ConsoleCommandSender)
 			{
 				plugin.professions.get(player).GainExperience(Integer.parseInt(xp));
 			}
 		}
 	}
 	public static void CommandPlaces(Player sender, String args[])
 	{
 		if (BasicWorld.IsBasicWorld(sender.getLocation()))
 		{
 			sender.sendMessage("PP Prikazy nemuzete pouzivat ve svete pro novacky!");
 			return;
 		}
 		if (args.length == 0)
 		{
 			sender.sendMessage("PLACES MENU:");
 			sender.sendMessage("/places list <strana>  -  Seznam vsech moznych mist.");
 			sender.sendMessage("/pp <jmeno>  -  Teleportuje hrace na dane misto.");
 			sender.sendMessage("/places me - Zobrazi vase mista.");
 			sender.sendMessage("/places set - Zobrazi dostupna nastaveni.");
 		}
 		else if (args.length == 1)
 		{
 			String subCommand = args[0];
 			if(subCommand.matches("me"))
 			{
 				if(PlacesHandler.getPlacesByOwner(sender.getName()).isEmpty())
 				{
 					ChatHandler.FailMsg(sender, "Bohuzel nemate zadna mista");
 				}
 				else
 				{
 					sender.sendMessage("VASE MISTA:");
 					Iterator <Places> it = PlacesHandler.getPlacesByOwner(sender.getName()).iterator();
 					while (it.hasNext())
 					{
 						sender.sendMessage("* " + it.next().getPortName());
 					}
 				}
 			}
 			else if (subCommand.matches("set"))
 			{
				sender.sendMessage(" /place set players <jmenoPortu> <player1,player2> - Nastavi uzivatele, kteri se mohou pouzivat port.");
 				sender.sendMessage(ChatColor.YELLOW +"- pro soukromy - zadejte pouze vase jmeno.");
 				sender.sendMessage(ChatColor.YELLOW +"- pro verejny  - zadejte \"all\".");
				sender.sendMessage(" /place set welcome <jmenoPortu> <zprava> - Nastavi zpravu pro navstevniky Vaseho portu");
 			}
 		}
 		else if (args.length == 2)
 		{
 			String subCommand = args[0];
 			if(subCommand.matches("list"))
 			{
 				GugaDataPager<Places> pager = new GugaDataPager<Places>(PlacesHandler.getPlacesByPlayer(sender.getName()), 15);
 				Iterator <Places> i = pager.GetPage(Integer.parseInt(args[1])).iterator();
 				sender.sendMessage("SEZNAM DOSTUPNYCH MIST:");
 				sender.sendMessage("STRANA " + args[1] + "/" + pager.GetPagesCount());
 				while (i.hasNext())
 				{
 					sender.sendMessage("* " + i.next().getPortName());
 				}
 				
 			}
 		}
 		else if (args.length == 4)
 		{
 			String subCommand = args[0];
 			if(subCommand.matches("set"))
 			{
 				if(args[1].matches("players"))
 				{
 					if (PlacesHandler.isOwner(args[2], sender.getName()))
 					{
 						Places place;
 						if ((place = PlacesHandler.getPlaceByName(args[2])) != null)
 						{
 							place.setAllowedPlayers(args[3].split(","));
 							ChatHandler.SuccessMsg(sender, "Nastaveni bylo uspesne!");
 					}
 						else
 						{
 							ChatHandler.FailMsg(sender, "Toto misto neexistuje!");
 						}
 					}
 					else
 					{
 						ChatHandler.FailMsg(sender, "Nejste majitelem mista " + args[2] + "!");
 					}
 				}
 				else if(args[1].matches("welcome"))
 				{
 					if (PlacesHandler.isOwner(args[2], sender.getName()))
 					{
 						Places place;
 						if ((place = PlacesHandler.getPlaceByName(args[2])) != null)
 						{
 							place.setWelcomeMsg(args[3]);
 							ChatHandler.SuccessMsg(sender, "Nastaveni bylo uspesne!");
 						}
 						else
 						{
 							ChatHandler.FailMsg(sender, "Toto misto neexistuje!");
 						}
 					}
 					else
 					{
 						ChatHandler.FailMsg(sender, "Nejste majitelem mista " + args[2] + "!");
 					}
 				}
 			}
 		}
 		else if (args.length > 4 )
 		{
 			if(args[0].matches("set"))
 			{
 				if(args[1].matches("welcome"))
 				{
 					if (PlacesHandler.isOwner(args[2], sender.getName()))
 					{
 						Places place;
 						if ((place = PlacesHandler.getPlaceByName(args[2])) != null)
 						{
 							int i = 3;
 							String msg = "";
 							while(i<args.length)
 							{
 								msg = msg + " " + args[i];
 								i++;
 							}
 							place.setWelcomeMsg(msg);
 							ChatHandler.SuccessMsg(sender, "Nastaveni bylo uspesne!");
 						}
 						else
 						{
 							ChatHandler.FailMsg(sender, "Toto misto neexistuje!");
 						}
 					}
 					else
 					{
 						ChatHandler.FailMsg(sender, "Nejste majitelem mista " + args[2] + "!");
 					}
 				}
 			}
 		}
 	}
 	public static void CommandPP(Player sender, String args[])
 	{
 		if (BasicWorld.IsBasicWorld(sender.getLocation()))
 		{
 			sender.sendMessage("PP Prikazy nemuzete pouzivat ve svete pro novacky!");
 			return;
 		}
 		if(args.length==1)
 		{
 			Teleport(sender,args[0]);
 		}
 	}
 	public static void CommandAH(Player sender, String args[])
 	{
 		if (args.length == 0)
 		{
 			sender.sendMessage("AUCTION HOUSE MENU:");
 			sender.sendMessage("Jako Platidlo slouzi Gold Ingoty!");
 			sender.sendMessage("Prikazy:");
 			sender.sendMessage("/ah show - Zobrazi pocet stran.");
 			sender.sendMessage("/ah show <strana> - Zobrazi danou stranku nabidky aukce.");
 			sender.sendMessage("/ah buy <id> - Koupi aukci podle id z /ah show.");
 			sender.sendMessage("/ah create <itemID> <pocet> <cena> - Vytvori novou aukci.");
 			sender.sendMessage("/ah cancel <id> - Stornuje aukci podle ID z /ah my.");
 			sender.sendMessage("/ah my - Zobrazi vase aukce.");
 			return;
 		}
 		if (args.length >= 1)
 		{
 			String subCmd = args[0];
 			if (subCmd.equalsIgnoreCase("show"))
 			{
 				if (args.length == 1)
 				{
 					sender.sendMessage("Aktualni pocet stran: " + GugaAuctionHandler.GetPagesCount());
 				}
 				else if (args.length == 2)
 				{
 					int page = Integer.parseInt(args[1]);
 					GugaDataPager<GugaAuction> pager = new GugaDataPager<GugaAuction>(GugaAuctionHandler.GetAllAuctions(), 15);
 					sender.sendMessage("ID ; itemID ; pocet ; cena ; vlastnik");
 					Iterator<GugaAuction> i = pager.GetPage(page).iterator();
 					int i2 = 15 * (page - 1);
 					while (i.hasNext())
 					{
 						GugaAuction auction = i.next();
 						int itemID = auction.GetItemID();
 						int amount = auction.GetAmount();
 						int price = auction.GetPrice();
 						String owner = auction.GetOwner();
 						sender.sendMessage(i2 + " ; " + itemID + " ; " + amount + " ; " + price + " ; " + owner);
 						i2++;
 					}
 				}
 			}
 			else if (subCmd.equalsIgnoreCase("create"))
 			{
 				if (args.length == 4)
 				{
 					int itemID = Integer.parseInt(args[1]);
 					int amount = Integer.parseInt(args[2]);
 					int price = Integer.parseInt(args[3]);
 					if (GugaAuctionHandler.CreateAuction(itemID, amount, price, sender))
 						sender.sendMessage("Aukce uspesne vytvorena!");
 					else
 					{
 						sender.sendMessage("Nemate dostatek itemu v inventari!");
 					}
 					
 				}
 			}
 			else if (subCmd.equalsIgnoreCase("buy"))
 			{
 				if (args.length == 2)
 				{
 					int index = Integer.parseInt(args[1]);
 					if (GugaAuctionHandler.GetAllAuctions().get(index).GetOwner().matches(sender.getName()))
 					{
 						sender.sendMessage("Nemuzete koupit vlastni aukci!");
 						return;
 					}
 					if (GugaAuctionHandler.BuyAuction(sender, index))
 						sender.sendMessage("Aukce uspesne koupena!");
 					else
 						sender.sendMessage("Nemate na zaplaceni!");
 				}
 			}
 			else if (subCmd.equalsIgnoreCase("cancel"))
 			{
 				if (args.length == 2)
 				{
 					int index = Integer.parseInt(args[1]);
 					if (GugaAuctionHandler.CancelAuction(index, sender))
 						sender.sendMessage("Aukce uspesne zrusena.");
 					else
 						sender.sendMessage("Aukce s timto ID neexistuje!");
 				}
 			}
 			else if (subCmd.equalsIgnoreCase("my"))
 			{
 				if (args.length == 1)
 				{
 					ArrayList<GugaAuction> list = GugaAuctionHandler.GetPlayerAuctions(sender);
 					if (list.size() == 0)
 					{
 						sender.sendMessage("Nemate zadnou aukci!");
 						return;
 					}
 					Iterator<GugaAuction> i = list.iterator();
 					int i2 = 0;
 					sender.sendMessage("ID ; itemID ; amount ; price");
 					while (i.hasNext())
 					{
 						GugaAuction auction = i.next();
 						int itemID = auction.GetItemID();
 						int amount = auction.GetAmount();
 						int price = auction.GetPrice();
 						sender.sendMessage(i2 + " ; " + itemID + " ; " + amount + " ; " + price);
 						i2++;
 					}
 				}
 			}
 		}
 	}
 	public static void CommandModule(String args[])
 	{
 		if (args.length >= 1)	
 		 {
 			if (args[0].equalsIgnoreCase("ChestsModule"))
 			{
 				plugin.config.chestsModule = !plugin.config.chestsModule;
 				plugin.config.SetConfiguration();
 				plugin.log.info("chestModule = "+plugin.config.chestsModule);
 			}
 			else if (args[0].equalsIgnoreCase("AccountsModule"))
 			{
 				plugin.config.accountsModule = !plugin.config.accountsModule;
 				plugin.config.SetConfiguration();
 				plugin.log.info("accountsModule = "+plugin.config.accountsModule);
 			}
 		 }
 		 else
 		 {
 			 plugin.log.info("Modules:");
 			 plugin.log.info("	AccountsModule	= "+plugin.config.accountsModule);
 			 plugin.log.info("	ChestsModule	= "+plugin.config.chestsModule);
 		 }
 	}
 	/*public static void CommandRegister(Player sender, String args[])
 	{
 		if(!GugaMCClientHandler.HasClient(sender))
 		{
 			sender.sendMessage("Nemate client");
 			return;
 		}
 		if(plugin.acc.UserIsRegistered(sender))
 		{
 			sender.sendMessage("Tento ucet je jiz zaregistrovan!");
 		}
 		else
 		{
 			if (args.length > 0)
 			{
 				String pass = args[0];
 				plugin.acc.RegisterUser(sender, pass);
 				GugaBanHandler.UpdateBanAddr(sender.getName());
 			}
 			else
 			{
 				sender.sendMessage("Prosim zadejte vase heslo!");
 			}
 		}
 	}*/
 	public static void CommandEventWorld(Player sender, String args[])
 	{
 		if (!plugin.acc.UserIsLogged(sender))
 		{
 			sender.sendMessage("K pouziti tohoto prikazu je treba se prihlasit!");
 			return;
 		}
 		if (args.length==0)
 		{
 			sender.sendMessage("EventWorld MENU:");
 			sender.sendMessage("Commands:");
 			sender.sendMessage("/ew join - Teleportuje hrace do eventWorldu");
 			sender.sendMessage("/ew leave - Vrati hrace do normalniho sveta");
 			if(GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				sender.sendMessage("/ew togglemobs - Toggle mobs on/off");
 				sender.sendMessage("/ew togglepvp - Toggle PvP on/off");
 				sender.sendMessage("/ew toggleregion - Toggle region on/off");
 				if(GameMasterHandler.IsAdmin(sender.getName()))
 				{
 					sender.sendMessage("/eventworld setspawn - Sets a EventWorld spawn to GM's position");
 				}
 			}
 			GameMaster gm = GameMasterHandler.GetGMByName(sender.getName());
 			if(gm.GetRank() == Rank.EVENTER)
 			{
 				sender.sendMessage("/ew mode");
 			}
 		}
 		else if (args.length == 1)
 		{
 			String subCommand = args[0];
 			if (plugin.arena.IsArena(sender.getLocation()))
 			{
 				sender.sendMessage("Z areny se nedostanete do EventWordu!");
 				return;
 			}
 			else
 			{
 				if (subCommand.matches("join"))
 				{
 					if (!plugin.EventWorld.IsEventWorld(sender.getLocation()))
 					{
 						plugin.EventWorld.PlayerJoin(sender.getLocation(),sender);
 					}
 					else
 					{
 						sender.sendMessage("V EventWorldu jiz jste!");
 					}
 				}
 				else if(subCommand.matches("leave"))
 				{
 					if (plugin.EventWorld.IsEventWorld(sender.getLocation()))
 					{
 						plugin.EventWorld.PlayerLeave(sender);
 					}
 					else
 					{
 						sender.sendMessage("Nejste v EW!");
 					}
 				}
 				else if(subCommand.matches("mode") && GameMasterHandler.IsAtleastRank(sender.getName(), Rank.EVENTER))
 				{
 					if(plugin.EventWorld.IsEventWorld(sender.getLocation()))
 					{
 						if(sender.getGameMode() == GameMode.SURVIVAL)
 						{
 							sender.setGameMode(GameMode.CREATIVE);
 							ChatHandler.SuccessMsg(sender, "Creative mod nastaven");
 						}
 						else
 						{
 							sender.setGameMode(GameMode.SURVIVAL);
 							ChatHandler.SuccessMsg(sender, "Survival mod nastaven");
 						}
 					}
 					else
 					{
 						ChatHandler.FailMsg(sender,"Tento prikaz funguje jen v EW");
 					}
 				}
 				else if(subCommand.matches("togglepvp") && GameMasterHandler.IsAtleastGM(sender.getName()))
 				{
 					plugin.EventWorld.togglePvP(sender);
 				}
 				else if(subCommand.matches("togglemobs") && GameMasterHandler.IsAtleastGM(sender.getName()))
 				{
 					plugin.EventWorld.toggleMobs(sender);
 				}
 				else if(subCommand.matches("toggleregion")&& GameMasterHandler.IsAtleastGM(sender.getName()))
 				{
 					plugin.EventWorld.toggleRegion(sender);
 				}
 				else if(subCommand.matches("setspawn")&&GameMasterHandler.IsAdmin(sender.getName()))
 				{
 					Location l = sender.getLocation();
 					sender.getWorld().setSpawnLocation((int)l.getX(), (int)l.getY(), (int)l.getZ());
 					sender.sendMessage("New spawn for EventWorld has been set!");
 				}
 			}
 		}
 	}
 	public static void CommandAdventureWorld(Player sender, String args[])
 	{
 		if (args.length==0)
 		{
 			sender.sendMessage("AdventureWorld MENU:");
 			sender.sendMessage("Commands:");
 			sender.sendMessage("/aw join - Teleportuje hrace do AdventureWorldu");
 			sender.sendMessage("/aw leave - Vrati hrace do normalniho sveta");
 			if(GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				sender.sendMessage("/aw togglemobs - Toggle mobs on/off");
 				sender.sendMessage("/aw togglepvp - Toggle PvP on/off");
 				sender.sendMessage("/aw toggleregion - Toggle region on/off");
 				sender.sendMessage("/aw toggle - Toggle AW on/off");
 				if(GameMasterHandler.IsAdmin(sender.getName()))
 				{
 					sender.sendMessage("/aw setspawn - Sets a AdventureWorld spawn to GM's position");
 				}
 			}
 		}
 		else if (args.length == 1)
 		{
 			String subCommand = args[0];
 			if (plugin.arena.IsArena(sender.getLocation()))
 			{
 				sender.sendMessage("Z areny se nedostanete do AdventureWorldu!");
 				return;
 			}
 			else
 			{
 				if (subCommand.matches("join"))
 				{
 					if (!plugin.AdventureWorld.IsAdventureWorld(sender.getLocation()))
 					{
 						plugin.AdventureWorld.PlayerJoin(sender.getLocation(),sender);
 					}
 					else
 					{
 						sender.sendMessage("V AdventureWorldu jiz jste!");
 					}
 				}
 				else if(subCommand.matches("leave"))
 				{
 					plugin.AdventureWorld.PlayerLeave(sender);
 				}
 				else if(subCommand.matches("togglepvp") && GameMasterHandler.IsAtleastGM(sender.getName()))
 				{
 					plugin.AdventureWorld.togglePvP(sender);
 				}
 				else if(subCommand.matches("togglemobs") && GameMasterHandler.IsAtleastGM(sender.getName()))
 				{
 					plugin.AdventureWorld.toggleMobs(sender);
 				}
 				else if(subCommand.matches("toggleregion")&& GameMasterHandler.IsAtleastGM(sender.getName()))
 				{
 					plugin.AdventureWorld.toggleRegion(sender);
 				}
 				else if(subCommand.matches("toggle")&& GameMasterHandler.IsAtleastGM(sender.getName()))
 				{
 					plugin.AdventureWorld.toggleWorld(sender);
 				}
 				else if(subCommand.matches("setspawn")&&GameMasterHandler.IsAdmin(sender.getName()))
 				{
 					Location l = sender.getLocation();
 					sender.getWorld().setSpawnLocation((int)l.getX(), (int)l.getY(), (int)l.getZ());
 					sender.sendMessage("New spawn for AdventureWorld has been set!");
 				}
 			}
 		}
 	}
 	public static void CommandArena(Player sender, String args[])
 	{
 		if (!plugin.acc.UserIsLogged(sender))
 		{
 			sender.sendMessage("K pouziti tohoto prikazu je treba se prihlasit!");
 			return;
 		}
 		if (args.length == 0)
 		{
 			sender.sendMessage("ARENA MENU:");
 			sender.sendMessage("Commands:");
 			sender.sendMessage("/arena join - Teleportuje hrace do areny");
 			sender.sendMessage("/arena leave - Vrati hrace do normalniho sveta");
 			sender.sendMessage("/arena stats - Zobrazi zebricek nejlepsich hracu");
 			sender.sendMessage("/arena info - Info about arena and ranks");
 		}
 		else if (args.length == 1)
 		{
 			String subCommand = args[0];
 			if (subCommand.matches("join"))
 			{
 				if (plugin.EventWorld.IsEventWorld(sender.getLocation()))
 				{
 					sender.sendMessage("Z EventWorldu se nedostanete do areny!");
 					return;
 				}
 				/*if(GugaFlyHandler.isFlying(sender.getName()))
 				{
 					sender.setFlying(false);
 					sender.setAllowFlight(false);
 				}*/
 				if (!plugin.arena.IsArena(sender.getLocation()))
 				{
 					plugin.arena.PlayerJoin(sender);
 				}
 				else
 				{
 					sender.sendMessage("V arene jiz jste!");
 				}
 			
 			}
 			else if (subCommand.matches("leave"))
 			{
 				plugin.arena.PlayerLeave(sender);
 			}
 			else if (subCommand.matches("stats"))
 			{
 				plugin.arena.ShowPvpStats(sender);
 			}
 			else if (subCommand.matches("info"))
 			{
 				Integer kills = plugin.arena.GetPlayerStats(sender);
 				sender.sendMessage("Vas rank: " +ArenaTier.GetTier(kills).toString());
 				sender.sendMessage("Pocet killu: " +kills);
 			}
 		}
 	}
 	public static void CommandParty(Player sender, String args[])
 	{
 		if(args.length==0)
 		{
 			sender.sendMessage(" /party <jmeno> - vytvori a prihlasi Vas do party");
 			sender.sendMessage(" /party q - opustite party");
 			sender.sendMessage(" /invite <jmeno hrace> - pozve hrace do Vasi party");
 			sender.sendMessage(" /invite - prijme pozvani do party");
 			sender.sendMessage(" /p - posle zpravu do party chatu");
 		}
 		else if(args.length==1)
 		{	
 			if(args[0].matches("q"))
 			{
 				GugaParty.removePlayer(sender);
 			}
 			else
 			{
 				GugaParty.addParty(sender, args[0]);
 			}
 		}
 	}
 	public static void CommandInvite(Player sender, String args[])
 	{
 		if(args.length==0)
 		{
 			GugaParty.inviteAccepted(sender);
 		}
 		else if(args.length==1)
 		{
 			GugaParty.invitePlayer(sender, args[0]);
 		}
 	}
 	public static void CommandSendPartyMsg(Player sender, String args[])
 	{
 		if(args.length!=0)
 		{
 			if(args.length!=0)
 			{
 				String text="";
 				for(int i=0;i<args.length;i++)
 				{
 					if(i==0)
 						text=text+args[i];
 					else
 						text=text+" "+args[i];
 				}
 				GugaParty.sendMessage(sender, text);
 			}
 		}
 	}
 	public static void CommandReply(Player sender, String args[])
 	{
 		if (args.length > 0)
 		{
 			Player p;
 			if ( (p = reply.get(sender)) != null)
 			{
 				int i = 0;
 				String msg = "";
 				while (i < args.length)
 				{
 					msg += args[i] + " ";
 					i++;
 				}
 				String cmd = "/tell " + p.getName() + " " + msg;
 				sender.chat(cmd);
 				//sender.sendMessage(ChatColor.GRAY + "To " + p.getName() + ": " + msg);
 				reply.put(p, sender);
 				return;
 			}
 			sender.sendMessage("Nemate komu odpovedet!");
 		}
 	}
 	public static void CommandTeam(Player sender, String args[])
 	{
 		if(!plugin.acc.UserIsLogged(sender))
 		{
 			sender.sendMessage("Nejprve se musite prihlasit!");
 			return;
 		}
 		if(!GameMasterHandler.IsAtleastGM(sender.getName()))
 		{
 			return;
 		}
 		if(args.length == 0)
 		{
 			sender.sendMessage("/team blue:red add <player1,player2> - Adds player to blue or red team");
 			sender.sendMessage("/team blue:red add itemID, ammount - Adds items to certain team");
 			sender.sendMessage("/team blue:red remove <player> - Removes player from blue or red team");
 			sender.sendMessage("/team clear - Clears all teams");
 		}
 		else if(args.length == 1)
 		{
 			if(args[0].equalsIgnoreCase("clear"))
 			{
 				GugaTeams.deleteTeams();
 				sender.sendMessage("Cleared!");
 			}
 		}
 		else if(args.length == 3)
 		{
 			if(args[0].equalsIgnoreCase("blue") || args[0].equalsIgnoreCase("red"))
 			{
 				if(args[1].equalsIgnoreCase("add"))
 				{
 					GugaTeams.addToTeam(args[2].split(","), args[0]);
 					sender.sendMessage("Player was successfuly added to " + args[0] + " team.");
 				}
 				else if(args[1].equalsIgnoreCase("remove"))
 				{
 					GugaTeams.removePlayer(args[2]);
 					sender.sendMessage("Player was successfully removed");
 				}
 			}
 		}
 		else if(args.length == 4)
 		{
 			if(args[0].equalsIgnoreCase("blue") || args[0].equalsIgnoreCase("red"))
 			{
 				if(args[1].equalsIgnoreCase("add"))
 				{
 					int itemID = Integer.parseInt(args[2]);
 					int ammount = Integer.parseInt(args[3]);
 					GugaTeams.AddItemToPlayers(itemID, ammount,args[0]);
 					sender.sendMessage("Items successfully added!");
 				}
 			}
 		}
 	}
 	public static void CommandFeedback(Player sender, String args[])
 	{
 		if (!plugin.acc.UserIsLogged(sender))
 		{
 			sender.sendMessage("Nejprve se musite prihlasit!");
 			return;
 		}
 		if(args.length == 0)
 			return;
 		int i = 0;
 		String feed = "";
 		while (i < args.length)
 		{
 			feed += args[i];
 			i++;
 		}
 		GugaFile file = new GugaFile(FeedbackFile, GugaFile.APPEND_MODE);
 		String line = "Feedback (" + sender.getName() + ") " + feed;
 		file.Open();
 		file.WriteLine(line);
 		file.Close();
 		sender.sendMessage("Zpetna vazba byla odeslana. Dekujeme za Vasi podporu!");
 	}
 	public static void CommandEvent(Player sender, String args[])
 	{
 		if (!plugin.acc.UserIsLogged(sender))
 		{
 			sender.sendMessage("Nejprve se musite prihlasit!");
 			return;
 		}
 		if ((!GameMasterHandler.IsAtleastGM(sender.getName())) && (!GameMasterHandler.IsRank(sender.getName(), Rank.EVENTER)))
 		{
 			if (args.length > 0)
 			{
 				if (args[0].equalsIgnoreCase("join"))
 				{
 					if (GugaEvent.acceptInv)
 					{
 						if (GugaEvent.players.size() < GugaEvent.playersCap)
 						{
 							GugaEvent.AddPlayer(sender.getName().toLowerCase());
 							sender.sendMessage("Byl jste uspesne prihlasen k eventu");
 						}
 						else
 							sender.sendMessage("Neni mozne se pripojit - Event je plny!");
 								
 					}
 					else
 						sender.sendMessage("Nyni se nemuzete prihlasit k zadnemu eventu!");
 				}
 			}
 			return;
 		}
 		if (args.length == 0)
 		{
 			String stateGodMode;
 			if (GugaEvent.godMode)
 				stateGodMode = "[ON]";
 			else
 				stateGodMode = "[OFF]";
 			String stateInv;
 			if (GugaEvent.acceptInv)
 				stateInv = "[ON]";
 			else
 				stateInv = "[OFF]";
 			sender.sendMessage("EVENT MENU:");
 			sender.sendMessage("Commands:");
 			sender.sendMessage("/event players - Shows players submenu.");
 			sender.sendMessage("/event inventory - Shows inventory submenu.");
 			//sender.sendMessage("/event spawners - Shows spawners submenu.");
 			sender.sendMessage("/event teleport - Teleports all tagged players to your location.");
 			sender.sendMessage("/event tpback - Teleports all players back to their original locations.");
 			sender.sendMessage("/event give <itemID> <amount> - Adds specified item to tagged players.");
 			sender.sendMessage("/event godmode " + stateGodMode + " - Toggles immortality for tagged players.");
 			sender.sendMessage("/event stats <itemID> - Prints stats of all tagged players.");
 			sender.sendMessage("/event allowinv " + stateInv + " - Allow players to join your event.");
 			return;
 		}
 		String arg1 = args[0];
 		if (arg1.equalsIgnoreCase("teleport"))
 		{
 			GugaEvent.TeleportPlayersTo(sender.getName());
 			sender.sendMessage("Players teleported.");
 			return;
 		}
 		else if(arg1.equalsIgnoreCase("msg"))
 		{
 			int i = 1;
 			String msg = "";
 			while(i<(args.length))
 			{
 				msg += " " + args[i];
 				i++;
 			}
 			plugin.getServer().broadcastMessage(ChatColor.AQUA + "[EVENT]" + ChatColor.RED + msg);
 		}
 		else if (arg1.equalsIgnoreCase("allowinv"))
 		{
 			GugaEvent.ToggleAcceptInvites();
 			String stateInv;
 			if (GugaEvent.acceptInv)
 				stateInv = "[ON]";
 			else
 				stateInv = "[OFF]";
 			sender.sendMessage("Accept Invites " + stateInv);
 		}
 		else if (arg1.equalsIgnoreCase("godmode"))
 		{
 			GugaEvent.godMode = !GugaEvent.godMode;
 			String state;
 			if (GugaEvent.godMode)
 				state = "[ON]";
 			else
 				state = "[OFF]";
 			sender.sendMessage("GodMode " + state);
 			return;
 		}
 		else if (arg1.equalsIgnoreCase("tpback"))
 		{
 			GugaEvent.TeleportPlayersBack();
 			sender.sendMessage("Players teleported back.");
 			return;
 		}
 		else if (arg1.equalsIgnoreCase("stats"))
 		{
 			sender.sendMessage("PLAYER STATS FOR ID " + args[1] + ":");
 			if (args.length == 2)
 			{
 				Iterator<String> i = GugaEvent.GetItemCountStats(Integer.parseInt(args[1])).iterator();
 				while (i.hasNext())
 				{
 					String[] split = i.next().split(";");
 					sender.sendMessage(split[0] + " - " + split[1]);
 				}
 			}
 			return;
 		}
 		else if (arg1.equalsIgnoreCase("give"))
 		{
 			if (args.length == 3)
 			{
 				GugaEvent.AddItemToPlayers(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
 				sender.sendMessage("Items added.");
 				return;
 			}
 		}
 		/*else if (arg1.equalsIgnoreCase("spawners"))
 		{
 			if (args.length == 1)
 			{
 				sender.sendMessage("/event spawners add <group> <typeID> <interval> - Creates a spawner.");
 				sender.sendMessage("/event spawners remove <group> <index> - Removes spawner from a group.");
 				sender.sendMessage("/event spawners clear <group> - Removes all spawners of a specified group.");
 				sender.sendMessage("/event spawners toggle <group> - Turns spawning ON or OFF.");
 				sender.sendMessage("/event spawners list - Prints all spawner groups.");
 				sender.sendMessage("/event spawners list <group> - Prints all spawners of specified group.");
 				sender.sendMessage("/event spawners ids - Prints list of all mob ids.");
 			}
 			else if (args.length == 2)
 			{
 				if (args[1].equalsIgnoreCase("list"))
 				{
 					Iterator<String> i = GugaEvent.GetGroupNames().iterator();
 					while (i.hasNext())
 					{
 						String group = i.next();
 						String state;
 						if (GugaEvent.GetGroupState(group))
 							state = "[ON]";
 						else
 							state = "[OFF]";
 						sender.sendMessage(group + " " + state);
 					}
 				}
 			}
 			else if (args.length == 3)
 			{
 				if (args[1].equalsIgnoreCase("list"))
 				{
 					Iterator<GugaSpawner> i = GugaEvent.GetSpawnersOfGroup(args[2]).iterator();
 					int index = 0;
 					sender.sendMessage(args[2] + " SPAWNERS:");
 					while (i.hasNext())
 					{
 						GugaSpawner spawner = i.next();
 						Location loc = spawner.GetLocation();
 						String state;
 						if (spawner.GetSpawnState())
 							state = "[ON]";
 						else
 							state = "[OFF]";
 						sender.sendMessage(index + ": " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " " + state);
 						index++;
 					}
 				}
 				if (args[1].equalsIgnoreCase("clear"))
 				{
 					GugaEvent.ClearSpawnersFromGroup(args[2]);
 					sender.sendMessage("All spawners removed.");
 				}
 				else if (args[1].equalsIgnoreCase("toggle"))
 				{
 					String state;
 					GugaEvent.ToggleGroupSpawning(args[2]);
 					if (GugaEvent.GetGroupState(args[2]))
 						state = "[ON]";
 					else
 						state = "[OFF]";
 					sender.sendMessage(args[2] + " " + state);
 				}
 				else if (args[1].equalsIgnoreCase("ids"))
 				{
 					int i = 0;
 					for (EntityType type : EntityType.values())
 					{
 						sender.sendMessage(i + type.getName());
 						i++;
 					}
 				}
 				return;
 			}
 			else if (args.length == 4)
 			{
 				if (args[1].equalsIgnoreCase("remove"))
 				{
 					GugaEvent.RemoveSpawnerFromGroup(args[2], Integer.parseInt(args[3]));
 					sender.sendMessage("Spawner removed.");
 				}
 				return;
 			}
 			else if (args.length == 5)
 			{
 				if (args[1].equalsIgnoreCase("add"))
 				{
 					GugaEvent.AddSpawnerToGroup(args[2], sender.getLocation(), Integer.parseInt(args[4]), Integer.parseInt(args[3]));
 					sender.sendMessage("Spawner has been added.");
 				}
 				return;
 			}
 		}*/
 		else if (arg1.equalsIgnoreCase("inventory"))
 		{
 			if (args.length == 1)
 			{
 				sender.sendMessage("/event inventory clear - Clears inventories of tagged players.");
 				sender.sendMessage("/event inventory return <0 / 1> - Returns old items back to players. 0 or 1 determines, whether items gained in event gonna be deleted. 0 - not deleted, 1 - deleted.");
 				return;
 			}
 			else if (args.length == 2)
 			{
 				if (args[1].equalsIgnoreCase("clear"))
 				{
 					GugaEvent.ClearInventories();
 					sender.sendMessage("Inventories cleared.");
 				}
 				return;
 			}
 			else if (args.length == 3)
 			{
 				if (args[1].equalsIgnoreCase("return"))
 				{
 					GugaEvent.ReturnInventories(args[2].equals("1"));
 					sender.sendMessage("Inventories returned.");
 				}
 				return;
 			}
 		}
 		else if (arg1.equalsIgnoreCase("players"))
 		{
 			if (args.length == 1)
 			{
 				sender.sendMessage("/event players add <name1,name2,name3> - Tags specified players for event.");
 				sender.sendMessage("/event players remove <name> - Removes specified player from the list.");
 				sender.sendMessage("/event players clear - Removes all tags.");
 				sender.sendMessage("/event players list <page> - List of tagged players.");
 				sender.sendMessage("/event players cap <value> [" + GugaEvent.playersCap + "] - Sets a new cap.");
 				return;
 			}
 			else if (args.length == 2) 
 			{
 				if (args[1].equalsIgnoreCase("clear"))
 				{
 					GugaEvent.ClearPlayers();
 					sender.sendMessage("Player list cleared.");
 				}
 				return;
 			}
 			else if (args.length == 3)
 			{
 				if (args[1].equalsIgnoreCase("add"))
 				{
 					String[] names = args[2].split(",");
 					int i = 0;
 					while (i < names.length)
 					{
 						if (GugaEvent.players.size() < GugaEvent.playersCap)
 							GugaEvent.AddPlayer(names[i]);
 						else
 							break;
 						i++;
 					}
 					sender.sendMessage(i + " Player(s) added.");
 				}
 				else if (args[1].equalsIgnoreCase("cap"))
 				{
 					GugaEvent.playersCap = Integer.parseInt(args[2]);
 					sender.sendMessage("New cap has been set.");
 				}
 				else if (args[1].equalsIgnoreCase("list"))
 				{
 					GugaDataPager<String> pager = new GugaDataPager<String>(GugaEvent.GetPlayers(), 15);
 					Iterator<String> i = pager.GetPage(Integer.parseInt(args[2])).iterator();
 					sender.sendMessage("PLAYER LIST:");
 					sender.sendMessage("PAGE " + args[2] + "/" + pager.GetPagesCount());
 					while (i.hasNext())
 					{
 						sender.sendMessage(i.next());
 					}
 				}
 				else if (args[1].equalsIgnoreCase("remove"))
 				{
 					if (GugaEvent.ContainsPlayer(args[2]))
 					{
 						GugaEvent.RemovePlayer(args[2]);
 						sender.sendMessage("Player removed from the list.");
 					}
 					else
 						sender.sendMessage("Player not found.");
 				}
 				return;
 			}
 		}
 	}
 	public static void CommandGM(Player sender, String args[])
 	{
 		if (!(GameMasterHandler.IsAtleastRank(sender.getName(), Rank.BUILDER)))
 			return;
 		Player []players = plugin.getServer().getOnlinePlayers();
 		String command = "/gm ";
 		int r=0;
 		while(r < args.length)
 		{
 			command += args[r] + " ";
 			r++;
 		}
 		if(GameMasterHandler.IsAdmin(sender.getName()))
 		{
 			int i = 0;
 			while(i < players.length)
 			{
 				if(GameMasterHandler.IsAdmin(players[i].getName()) && (sender.getName() != players[i].getName()))
 				{
 					players[i].sendMessage(ChatColor.GRAY+sender.getName() + " used command: " + command);
 				}
 				i++;
 			}
 		}
 		else if(GameMasterHandler.IsAtleastGM(sender.getName()))
 		{
 			int i = 0;
 			while(i < players.length)
 			{
 				if(GameMasterHandler.IsAtleastGM(players[i].getName()) && (sender.getName() != players[i].getName()))
 				{
 					players[i].sendMessage(ChatColor.GRAY+sender.getName() + " used command: " + command);
 				}
 				i++;
 			}
 		}
 		else if(GameMasterHandler.IsAtleastRank(sender.getName(), Rank.BUILDER))
 		{
 			int i = 0;
 			while(i < players.length)
 			{
 				if(GameMasterHandler.IsAtleastRank(players[i].getName(), Rank.BUILDER) && (sender.getName() != players[i].getName()))
 				{
 					players[i].sendMessage(ChatColor.GRAY+sender.getName() + " used command: " + command);
 				}
 				i++;
 			}
 		}
 		if (!plugin.acc.UserIsLogged(sender))
 		{
 			sender.sendMessage("Musite byt prihlaseny, aby jste mohl pouzit tento prikaz!");
 			return;
 		}
 		if (args.length == 0)
 		{
 			sender.sendMessage("GM MENU:");
 			sender.sendMessage("Commands:");
 			if (GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				sender.sendMessage("/gm ip <name> - Shows an IP of a player");
 				sender.sendMessage("/gm setspawn - Sets a world spawn to GM's position");
 				sender.sendMessage("/gm credits - Credits sub-menu.");
 				sender.sendMessage("/gm setvip <name> <months>  -  Set VIP to certain player for (now + months)");
 				sender.sendMessage("/gm getvip <name>  -  Gets VIP expiration date");
 				sender.sendMessage("/gm announce  - Announcements sub-menu.");
 				sender.sendMessage("/gm genblock <typeID> <reltiveX> <relativeY> <relativeZ>  -  Spawns a blocks from block you point at.");
 				sender.sendMessage("/gm replace <typeID> <typeID2> <reltiveX> <relativeY> <relativeZ> - Replaces a blocks from block you point at.");
 				sender.sendMessage("/gm godmode <name>  -  Toggles immortality for a certain player.");
 				sender.sendMessage("/gm spectate  -  Spectation sub-menu.");
 				sender.sendMessage("/gm places - Places sub-menu.");
 				sender.sendMessage("/gm regions - Regions sub-menu.");
 				sender.sendMessage("/gm arena - Arenas sub-menu.");
 				sender.sendMessage("/gm rank - Ranks sub-menu.");
 				sender.sendMessage("/gm fly <name> - Toggles fly mode for certain player.");
 				sender.sendMessage("/gm spawn - Spawns sub-menu.");
 				sender.sendMessage("/gm save-all - Saves all files of plugin and worlds.");
 				sender.sendMessage("/gm book - Books sub-menu");
 			}
 			if(GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				sender.sendMessage("/gm ban - Bans sub-menu.");
 				sender.sendMessage("/gm invis <name>  -  Toggles invisibility for a certain player.");
 				sender.sendMessage("/gm mute - Mute sub-menu.");
 				sender.sendMessage("/gm kill <player> - Kills target player.");
 				sender.sendMessage("/gm on - Turn your GM status to on");
 				sender.sendMessage("/gm off - Turn your GM status to off");
 				sender.sendMessage("/gm bw - BasicWorld sub-menu");
 				sender.sendMessage("/gm home <player> - Teleports you to certain player's home");
 				sender.sendMessage("/gm cmd <cmd> <arg1>... - Perform a bukkit command.");
 				sender.sendMessage("/gm rsdebug - Toggles RedStone debug.");
 				sender.sendMessage("/gm speed - Speed sub-menu");
 			}
 			sender.sendMessage("/gm log - Shows a log records for target block.(+saveall - saves unsaved progress)");
 			sender.sendMessage("/gm tp <x> <y> <z>  -  Teleports gm to specified coords.");
 			sender.sendMessage("/gm gmmode <name> -  Toggles gm mode for a certain player.");
 		}
 		else if (args.length == 1)
 		{
 			String subCommand = args[0];
 			if (subCommand.matches("log"))
 			{
 				plugin.logger.PrintLogData(sender, sender.getTargetBlock(null, 20));
 			}
 			else if(subCommand.matches("rsdebug") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if(plugin.bListener.redStoneDebug.contains(sender))
 				{
 					plugin.bListener.redStoneDebug.remove(sender);
 					ChatHandler.SuccessMsg(sender, "RedStone debug successfully turned off!");
 				}
 				else
 				{
 					plugin.bListener.redStoneDebug.add(sender);
 					ChatHandler.SuccessMsg(sender, "RedStone debug successfully turned on!");
 				}
 			}
 			else if(subCommand.matches("rank") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				sender.sendMessage("/gm rank add <player> <rank> - Adds rank (EVENTER/BUILDER) for a certain player.");
 				sender.sendMessage("/gm rank remove <player> - Removes rank for a certain player");
 			}
 			else if (subCommand.matches("mute") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				sender.sendMessage("/gm mute all - Toggle all chat messages on/off");
 				sender.sendMessage("/gm mute add <name> <time> - Mute players chat messages for certain time");
 				sender.sendMessage("/gm mute list - Shows list of muted players");
 			}
 			else if (subCommand.matches("spawn") && GameMasterHandler.IsAdmin(sender.getName())) 
 			{
 				sender.sendMessage("/gm spawn add <spawnName> - Adds spawn to your position.");
 				sender.sendMessage("/gm spawn remove <spawnName> - Removes certain spawn.");
 			}
 			else if (subCommand.matches("bw") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				sender.sendMessage("/gm bw join - Teleports you to BasicWorld.");
 				sender.sendMessage("/gm bw leave - Teleports you Spawn of main world.");
 			}
 			else if (subCommand.matches("time") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				sender.sendMessage("/gm time <world> <value> - Sets time for certain world.");
 			}
 			else if (subCommand.matches("arena") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				sender.sendMessage("/gm arena add <name> - Adds new arena spawn at your location.");
 				sender.sendMessage("/gm arena remove <name> - Removes specified arena.");
 				sender.sendMessage("/gm arena list - List of all arenas.");
 				sender.sendMessage("/gm arena next - Changes actual arena to next one.");
 			}
 			else if (subCommand.matches("setspawn") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				Location pLoc = sender.getLocation();
 				sender.getWorld().setSpawnLocation((int)pLoc.getX(), (int)pLoc.getY(), (int)pLoc.getZ());
 				sender.sendMessage("New World Spawn has been set!");
 			}
 			else if (subCommand.matches("announce") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				sender.sendMessage("/gm announce print - Prints messages and indexes.");
 				sender.sendMessage("/gm announce remove <index> - Removes a message from the list.");
 				sender.sendMessage("/gm announce add <message> - Adds new message to the list.");
 			}
 			else if (subCommand.matches("ban") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				sender.sendMessage("/gm ban add <player> <hours> - Bans a player for number of hours.");
 				sender.sendMessage("/gm ban remove <player> - Removes a ban.");
 				sender.sendMessage("/gm ban whitelist - Whitelist sub-menu.");
 				sender.sendMessage("/gm ban list <page>  -  Shows all banned players.");
 			}
 			else if (subCommand.matches("speed") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				sender.sendMessage("/gm speed fly <name> <speed> - Sets fly speed of a certain player.");
 				sender.sendMessage("/gm speed walk <name> <speed> - Sets walk speed of a certain player.");
 			}
 			else if (subCommand.matches("credits") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				sender.sendMessage("/gm credits add <player> <amount>  -  Add credits to a player.");
 				sender.sendMessage("/gm credits remove <player> <amount>  -  Remove credits to a player.");
 				sender.sendMessage("/gm credits balance <player>  -  Shows credits of a player.");
 			}
 			
 			else if (subCommand.matches("spectate") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				sender.sendMessage("/gm spectate player <name> - Start spectating certain player.");
 				sender.sendMessage("/gm spectate stop - Stop spectating.");
 			}
 			else if(subCommand.matches("places") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				sender.sendMessage("/gm places list <page>  - Show list of all places.");	
 				sender.sendMessage("/gm places add <name> <owner> - Adds actual position to places (owner all = public).");	
 				sender.sendMessage("/gm places remove <name> - Removes a certain place from the list.");	
 			}
 			else if (subCommand.matches("regions") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				sender.sendMessage("/gm regions list <page>  - Show list of all places.");	
 				sender.sendMessage("/gm regions add <name> <world> <owner1,owner2> <x1> <x2> <z1> <z2> - Adds Region");	
 				sender.sendMessage("/gm regions owners <name> <owners> - Changes owners of certain region.");	
 				sender.sendMessage("/gm regions remove <name> - Removes a certain region from the list.");	
 			}
 			else if (subCommand.matches("save-all") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				AutoSaver.SaveAll();
 				ChatHandler.SuccessMsg(sender, "Successfully saved!");
 			}
 			else if (subCommand.matches("book") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				sender.sendMessage("/gm book copy - Copies book in your hand.");
 			}
 			else if (subCommand.matches("on") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if(disabledGMs.contains(sender.getName()))
 				{
 					disabledGMs.remove(sender.getName());
 					ChatHandler.InitializeDisplayName(sender);
 					sender.setGameMode(GameMode.CREATIVE);
 					sender.sendMessage("GM state succesfully turned on!");
 				}
 			}
 			else if (subCommand.matches("off") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if(!disabledGMs.contains(sender.getName()))
 				{
 					disabledGMs.add(sender.getName());
 					ChatHandler.InitializeDisplayName(sender);
 					sender.setGameMode(GameMode.SURVIVAL);
 					sender.sendMessage("GM state succesfully turned off!");
 				}
 			}
 		}
 		else if (args.length == 2)
 		{
 			String subCommand = args[0];
 			String arg1 = args[1];
 			Player p;
 			if (subCommand.matches("ip") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if ((p = plugin.getServer().getPlayer(arg1)) != null)
 				{
 					sender.sendMessage("Players IP:" + p.getAddress());
 				}
 				else
 				{
 					sender.sendMessage("This player is not online!");
 				}
 			}
 			else if(subCommand.matches("mute") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if(args[1].matches("list"))
 				{
 					GugaMute.printPlayers(((Player)sender));
 				}
 				if(arg1.matches("all"))
 				{
 					boolean status = GugaMute.toggleChatMute();
 					if(status==true)
 						sender.sendMessage("Mute for all players is on.");
 					else
 						sender.sendMessage("Mute for all players is off.");	
 				}
 			}
 			else if (subCommand.matches("fly") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				Player target = plugin.getServer().getPlayer(arg1);
 				if (target == null)
 				{
 					sender.sendMessage("Hrac neni online!");
 					return;
 				}
 				if (target.getAllowFlight())
 				{
 					target.setAllowFlight(false);
 					target.setFlying(false);
 					fly.remove(target.getName().toLowerCase());
 					target.sendMessage("Fly mode byl vypnut!");
 					sender.sendMessage("Fly mode succesfuly turned off.");
 				}
 				else
 				{
 					target.setAllowFlight(true);
 					target.setFlying(true);
 					fly.add(target.getName().toLowerCase());
 					target.sendMessage("Fly mode byl zapnut!");
 					sender.sendMessage("Fly mode succesfuly turned on.");
 				}
 			}
 			else if (subCommand.matches("bw") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if(args[1].matches("join"))
 				{
 					BasicWorld.BasicWorldJoin(sender);
 				}
 				else if(args[1].matches("leave"))					
 				{
 					BasicWorld.BasicWorldLeave(sender);
 				}
 			}
 			else if (subCommand.matches("world") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if(plugin.getServer().getWorld(args[1]) != null)
 				{
 					sender.teleport(plugin.getServer().getWorld(args[1]).getSpawnLocation());
 					ChatHandler.SuccessMsg(sender, "You have been teleported!");
 				}
 				else
 					ChatHandler.FailMsg(sender, "This world doesn't exist!");
 			}
 			else if (subCommand.matches("book") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if(args[1].matches("copy"))
 				{
 					if(sender.getItemInHand().getTypeId() == 387)
 					{
 						Book book = new Book(sender.getItemInHand());
 						sender.getInventory().addItem(book.generateItemStack());
 						ChatHandler.SuccessMsg(sender, "Book has been copied.");
 					}
 					else
 					{
 						ChatHandler.FailMsg(sender, "Item is not writable book.");
 					}
 				}
 			}
 			else if (subCommand.matches("home") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				Homes home;
 				if((home = HomesHandler.getHomeByPlayer(args[1])) != null)
 				{
 					sender.teleport(HomesHandler.getLocation(home));
 					ChatHandler.SuccessMsg(sender, "You have been teleported to " + args[1] + " spawn!");
 				}
 			}
 			else if (subCommand.matches("log"))
 			{
 				if(args[1].matches("saveall"))
 				{
 					sender.sendMessage("Saving breakLog data...");
 					plugin.logger.SaveWrapperBreak();
 					sender.sendMessage("Saving placeLog data...");
 					plugin.logger.SaveWrapperPlace();
 					sender.sendMessage("Save completed");
 				}
 				else
 				{
 				ArrayList<String> data = plugin.logger.blockCache.get(sender);
 				if (data.size() == 0)
 				{
 					sender.sendMessage("You have no data saved! Use /gm log first");
 					return;
 				}
 				GugaDataPager<String> pager = new GugaDataPager<String>(data, 6);
 				Iterator<String> i = pager.GetPage(Integer.parseInt(arg1)).iterator();
 				sender.sendMessage("LIST OF BLOCK DATA:");
 				sender.sendMessage("PAGE " + Integer.parseInt(arg1) + "/" + pager.GetPagesCount());
 				while (i.hasNext())
 					sender.sendMessage(i.next());
 				}
 			}
 			else if (subCommand.matches("kill") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				Player target = plugin.getServer().getPlayer(args[1]);
 				if(target.isOnline())
 				{
 					target.setHealth(0);
 					target.sendMessage("Byl jste zabit adminem/GM!");
 					sender.sendMessage("Hrac "+target.getName()+" byl zabit!");
 				}
 			}
 			else if (subCommand.matches("arena") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (arg1.equalsIgnoreCase("list"))
 				{
 					Iterator<ArenaSpawn> i = plugin.arena.GetArenaList().iterator();
 					sender.sendMessage("LIST OF ARENAS:");
 					while (i.hasNext())
 					{
 						sender.sendMessage(i.next().GetName());
 					}
 				}
 				else if (arg1.equalsIgnoreCase("next"))
 				{
 					plugin.arena.RotateArena();
 					sender.sendMessage("Actual Arena changed.");
 				}
 			}
 			else if(subCommand.matches("announce") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (arg1.matches("print"))
 				{
 					int i = 0;
 					String msg;
 					while ( (msg = GugaAnnouncement.GetAnnouncement(i)) != null)
 					{
 						sender.sendMessage("[" + i + "]  -  " + msg);
 						i++;
 					}
 				}
 			}
 			else if (subCommand.matches("getvip") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				GugaVirtualCurrency vip = plugin.FindPlayerCurrency(arg1);
 				if (vip.IsVip())
 				{
 					sender.sendMessage("VIP Status for " + arg1 + " expires " + new Date(vip.GetExpirationDate()));
 				}
 				else
 				{
 					sender.sendMessage("This player is not a VIP");
 				}
 			}
 			else if (subCommand.matches("gmmode"))
 			{
 				if (!GameMasterHandler.IsAdmin(sender.getName()))
 					if (!arg1.equalsIgnoreCase(sender.getName()))
 					{
 						sender.sendMessage("You can only set gmmode to yourself!");
 						return;
 					}
 				if ((p = plugin.getServer().getPlayer(arg1)) != null)
 				{
 					GameMode mode = p.getGameMode();
 					if (mode == GameMode.CREATIVE)
 					{
 						p.setGameMode(GameMode.SURVIVAL);
 						sender.sendMessage("GM Mode for " + arg1 + " has been turned off");
 					}
 					else
 					{
 						p.setGameMode(GameMode.CREATIVE);
 						sender.sendMessage("GM Mode for " + arg1 + " has been turned on");
 					}
 				}
 			}
 			else if (subCommand.matches("godmode")&&GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if (godMode.contains(arg1.toLowerCase()))
 				{
 					godMode.remove(arg1);
 					sender.sendMessage("Immortality for " + arg1 + " has been turned off");
 				}
 				else
 				{
 					godMode.add(arg1.toLowerCase());
 					sender.sendMessage("Immortality for " + arg1 + " has been turned on");
 				}
 			}
 			else if (subCommand.matches("invis") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				ToggleInvisibility(sender, arg1);
 			}
 			else if (subCommand.matches("spectate") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (arg1.matches("stop"))
 				{
 					if (RemoveSpectation(sender))
 					{
 						sender.sendMessage("Spectation stopped");
 					}
 					else
 					{
 						sender.sendMessage("You are not spectating anyone!");
 					}
 				}
 			}
 			else if(subCommand.matches("ban") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if(args[1].matches("whitelist"))
 				{
 					sender.sendMessage("/gm ban whitelist add <playerName> - Adds player to whitelist.");
 					sender.sendMessage("/gm ban whitelist remove <playerName> - Removes player from whitelist");
 					sender.sendMessage("/gm ban whitelist - Prints acctualy whitelisted players");
 				}
 			}
 		}
 		else if (args.length > 2)
 		{
 			String subCommand = args[0];
 			if (subCommand.matches("spawn") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if(args.length == 3)
 				{
 					if(args[1].matches("add"))
 					{
 						SpawnsHandler.AddSpawn(args[2], sender.getLocation());
 						SpawnsHandler.SaveSpawns();
 						sender.sendMessage("Spawn was successfully added!");
 					}
 					else if(args[1].matches("remove"))
 					{
 						if(SpawnsHandler.GetSpawnByName(args[2]) != null)
 						{
 							SpawnsHandler.RemoveSpawn(args[2]);
 							SpawnsHandler.SaveSpawns();
 							sender.sendMessage("Spawn was successfully removed!");
 						}
 						else
 						{
 							sender.sendMessage("This spawn doesn't exist!");
 						}
 					}
 				}
 			}
 			if (subCommand.matches("announce") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				String arg1 = args[1];
 				if (arg1.matches("remove"))
 				{
 					String arg2 = args[2];
 					if (arg2 != null)
 					{
 						int num = Integer.parseInt(arg2);
 						if (GugaAnnouncement.RemoveAnnouncement(num))
 						{
 							sender.sendMessage("Announcement has been succesfuly removed.");
 						}
 						else
 						{
 							sender.sendMessage("This announcement doesnt exist!");
 						}
 					}
 				}
 				else if (arg1.matches("add"))
 				{
 					String msg = "";
 					int i = 2;
 					while (i < args.length)
 					{
 						msg += args[i];
 						msg += " ";
 						i++;
 					}
 					GugaAnnouncement.AddAnnouncement(msg);
 					sender.sendMessage("Announcement succesfuly added! <" + msg + ">");
 				}
 			}
 			else if (subCommand.matches("time") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if(args.length == 3)
 				{
 					if(plugin.getServer().getWorld(args[1]) != null)
 					{
 						plugin.getServer().getWorld(args[1]).setTime(Integer.parseInt(args[2]));
 						ChatHandler.SuccessMsg(sender, "Cas byl uspesne nastaven");
 					}
 				}
 			}
 			else if (subCommand.matches("mute")&& GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if(args.length==4)
 				{
 					if(args[1].matches("add"))
 					{
 						int i=0;
 						boolean isOnline=false;
 						Player []player=plugin.getServer().getOnlinePlayers();
 						while(i<player.length)
 						{
 							if(player[i].getName().equalsIgnoreCase(args[2]))
 							{
 								GugaMute.addPlayer(args[2],Integer.parseInt(args[3]));
 								player[i].sendMessage(ChatColor.RED+("Byl jste ztlumen na " + args[3]+" minut!"));
 								sender.sendMessage("Player " + player[i].getName() + " was muted!");
 								isOnline=true;
 							}
 							i++;
 						}
 						if(!(isOnline))
 						{
 							sender.sendMessage("This player is not online");
 						}
 					}
 				}
 			}
 			else if (subCommand.matches("speed") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if (args.length == 4)
 				{
 					Player target = plugin.getServer().getPlayer(args[2]);
 					if (target == null)
 					{
 						sender.sendMessage("Player is not online");
 						return;
 					}
 					if(!GameMasterHandler.IsAdmin(sender.getName()))
 					{
 						if(!args[2].equalsIgnoreCase(sender.getName()))
 						{
 							ChatHandler.FailMsg(sender, "You can set your speed only.");
 							return;
 						}
 					}
 					if(args[1].matches("fly"))
 					{
 						target.setFlySpeed(Float.parseFloat(args[3]));
 						ChatHandler.SuccessMsg(sender, "Fly speed has been succesfuly set.");
 					}
 					else if(args[1].matches("walk"))
 					{
 						target.setWalkSpeed(Float.parseFloat(args[3]));
 						ChatHandler.SuccessMsg(sender, "Walk speed has been succesfuly set.");
 					}
 				}
 			}
 			else if (subCommand.matches("log") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (args[1].equalsIgnoreCase("shop"))
 					plugin.logger.PrintShopData(sender, Integer.parseInt(args[2]));
 			}
 			else if (subCommand.matches("cmd") && GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				String cmd = args[1];
 				int i = 2;
 				while (i < args.length)
 				{
 					cmd += " " + args[i++];
 				}
 				BukkitCommandParser.ParseCommand(sender, cmd);
 			}
 			else if (subCommand.matches("arena") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (args.length == 3)
 				{
 					if (args[1].equalsIgnoreCase("add"))
 					{
 						if (plugin.arena.IsArena(sender.getLocation()))
 						{
 							plugin.arena.AddArena(args[2], sender.getLocation());
 							sender.sendMessage("Arena spawn succesfuly added.");
 						}
 						else
 							sender.sendMessage("You must be in arena world!");
 					}
 					else if (args[1].equalsIgnoreCase("remove"))
 					{
 						if (plugin.arena.ContainsArena(args[2]))
 						{
 							plugin.arena.RemoveArena(args[2]);
 							sender.sendMessage("Arena succesfuly removed.");
 						}
 						else
 							sender.sendMessage("This arena doesnt exist!");
 					}
 				}
 			}	
 			else if (subCommand.matches("ban")&&GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				if (args.length == 3)
 				{
 					String arg1 = args[1];
 					String arg2 = args[2];
 					if (arg1.matches("remove"))
 					{
 						GugaBanHandler.RemoveBan(arg2);
 						sender.sendMessage("Player unbanned!");
 					}
 					else if (arg1.equalsIgnoreCase("list"))
 					{
 						GugaDataPager<GugaBan> pager = new GugaDataPager<GugaBan>(GugaBanHandler.GetBannedPlayers(), 15);
 						Iterator<GugaBan> i = pager.GetPage(Integer.parseInt(arg2)).iterator();
 						sender.sendMessage("LIST OF BANNED PLAYERS:");
 						sender.sendMessage("PAGE " + arg2 + "/" + pager.GetPagesCount());
 						while (i.hasNext())
 						{
 							GugaBan ban = i.next();
 							long hours = (ban.GetExpiration() - System.currentTimeMillis()) / (60 * 60 * 1000);
 							sender.sendMessage(ban.GetPlayerName() + "  -  " + hours + " hours");
 						}
 					}
 					else if(arg1.matches("whitelist"))
 					{
 						GugaDataPager<String> pager = new GugaDataPager<String>(GugaBanHandler.GetWhitelistedPlayers(), 15);
 						Iterator <String> i = pager.GetPage(Integer.parseInt(arg2)).iterator();
 						sender.sendMessage("WHITELISTED PLAYERS:");
 						sender.sendMessage("PAGE " + args[1] + "/" + pager.GetPagesCount());
 						while (i.hasNext())
 						{
 							sender.sendMessage(i.next());
 						}
 					}
 				}
 				else if (args.length == 4)
 				{
 					String arg1 = args[1];
 					String arg2 = args[2];
 					String arg3 = args[3];
 					if (arg1.matches("add"))
 					{
 						Calendar c = Calendar.getInstance();
 						c.setTime(new Date());
 						c.add(Calendar.HOUR, Integer.parseInt(arg3));
 						GugaBanHandler.AddBan(arg2, c.getTimeInMillis());
 						Player p = plugin.getServer().getPlayer(arg2);
 						if (p != null)
 							p.kickPlayer("Vas ucet byl zabanovan na " + arg3 + " hodiny.");
 						sender.sendMessage("Player banned!");
 					}
 					else if(arg1.matches("whitelist"))
 					{
 						if(arg2.matches("add"))
 						{
 							if(!GugaBanHandler.IsIpWhitelisted(arg3))
 							{
 								GugaBanHandler.WhiteListPlayer(arg3);
 								GugaBanHandler.SaveIpWhiteList();
 								ChatHandler.SuccessMsg(sender, "Player successully added to ban whitelist!");
 							}
 							else
 							{
 								ChatHandler.FailMsg(sender, "Player is already whitelisted!");
 							}
 								
 						}
 						else if(arg2.matches("remove"))
 						{
 							if(GugaBanHandler.IsIpWhitelisted(arg3))
 							{
 								GugaBanHandler.RemovePlayerFromWhitelist(arg3);
 								GugaBanHandler.SaveIpWhiteList();
 								ChatHandler.SuccessMsg(sender, "Player successully removed from ban whitelist!");
 							}
 							else
 							{
 								ChatHandler.FailMsg(sender, "Player is not whitelisted!");
 							}
 						}
 					}
 				}
 			}
 			else if (subCommand.matches("places") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (args.length == 3)
 				{
 					String arg1 = args[1];
 					String arg2 = args[2];
 					if (arg1.matches("remove"))
 					{
 						Places place;
 						if ( (place = PlacesHandler.getPlaceByName(arg2)) != null)
 						{
 							PlacesHandler.removePlace(place);
 							sender.sendMessage("Place successfully removed");
 						}
 						else
 						{
 							sender.sendMessage("This place doesnt exist!");
 						}
 					}
 					if (arg1.matches("list"))
 					{
 						if (Integer.parseInt(args[2]) < 1)
 							return;
 						GugaDataPager<Places> pager = new GugaDataPager<Places>(PlacesHandler.newPlaces, 15);
 						sender.sendMessage("LIST OF PLACES:");
 						sender.sendMessage("PAGE " + args[2] + "/" + pager.GetPagesCount());
 						Iterator<Places> i = pager.GetPage(Integer.parseInt(args[2])).iterator();
 						while (i.hasNext())
 						{
 							Places e = i.next();
 							sender.sendMessage(" - " + e.getPortName() + "(" + e.getPortOwner() + ")");
 						}
 					}
 				}
 				else if (args.length == 4)
 				{
 					String arg1 = args[1];
 					String arg2 = args[2];
 					String arg3 = args[3];
 					if (arg1.matches("add"))
 					{
 						if (PlacesHandler.getPlaceByName(arg2) != null)
 						{
 							sender.sendMessage("This place already exists!");
 							return;
 						}
 						String[] owner = {arg3};
 						PlacesHandler.addPlace(new Places(arg2, arg3, owner, 
 								(int)sender.getLocation().getX(), (int)sender.getLocation().getY(), (int)sender.getLocation().getZ(), sender.getLocation().getWorld().getName(), "Vitejte na portu hrace " + arg3 + "!"));
 						sender.sendMessage("Place successfully added");
 					}
 				}
 			}
 			else if (subCommand.matches("regions") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (args.length == 3)
 				{
 					String subCmd = args[1];
 					if (subCmd.matches("remove"))
 					{
 						String name = args[2];
 						GugaRegion region = GugaRegionHandler.GetRegionByName(name);
 						if (region == null)
 						{
 							sender.sendMessage("Region not found!");
 							return;
 						}
 						GugaRegionHandler.RemoveRegion(region);
 						sender.sendMessage("Region successfully removed!");
 					}
 					else if (subCmd.equalsIgnoreCase("list"))
 					{
 						GugaDataPager<GugaRegion> pager = new GugaDataPager<GugaRegion>(GugaRegionHandler.GetAllRegions(), 15);
 						sender.sendMessage("LIST OF REGIONS:");
 						sender.sendMessage("PAGE " + args[2] + pager.GetPagesCount());
 						Iterator<GugaRegion> i = pager.GetPage(Integer.parseInt(args[2])).iterator();
 						while (i.hasNext())
 						{
 							GugaRegion region = i.next();
 							String[] owners = region.GetOwners();
 							int[] coords = region.GetCoords();
 							sender.sendMessage(" - " + region.GetName() + " [" + GugaRegionHandler.OwnersToLine(owners) + "]   <" + coords[GugaRegion.X1] + "," + coords[GugaRegion.X2] + "," + coords[GugaRegion.Z1] + "," + coords[GugaRegion.Z2] + ">");
 						}
 					}
 				}
 				else if (args.length == 4)
 				{
 					String subCmd = args[1];
 					if (subCmd.matches("owners"))
 					{
 						String name = args[2];
 						String[] owners = args[3].split(",");
 						if (GugaRegionHandler.SetRegionOwners(name, owners))
 							sender.sendMessage("Owners successfuly set!");
 						else
 							sender.sendMessage("Region not found!");
 					}
 					else if (subCmd.matches("add"))
 					{
 						String name = args[2];
 						String world = sender.getWorld().getName();
 						if (GugaRegionHandler.GetRegionByName(name) != null)
 						{
 							sender.sendMessage("Region with this name already exists!");
 							return;
 						}
 						String[] owners = args[3].split(",");GugaRegionHandler.AddRegion(name, world, owners, GugaCommands.x1,  GugaCommands.x2,  GugaCommands.z1,  GugaCommands.z2);
 						sender.sendMessage("Region successfully added");
 					}
 				}
 				else if (args.length == 9)
 				{
 					String subCmd = args[1];
 					if (subCmd.matches("add"))
 					{
 						String name = args[2];
 						if (GugaRegionHandler.GetRegionByName(name) != null)
 						{
 							sender.sendMessage("Region with this name already exists!");
 							return;
 						}
 						String world = args[3];
 						String[] owners = args[4].split(",");
 						int x1 = Integer.parseInt(args[5]);
 						int x2 = Integer.parseInt(args[6]);
 						int z1 = Integer.parseInt(args[7]);
 						int z2 = Integer.parseInt(args[8]);
 						GugaRegionHandler.AddRegion(name, world, owners, x1, x2, z1, z2);
 						sender.sendMessage("Region successfully added");
 					}
 				}
 			}
 			else if (subCommand.matches("credits") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (args.length == 3)
 				{
 					String arg1 = args[1];
 					String name = args[2];
 					if (arg1.matches("balance"))
 					{
 						GugaVirtualCurrency p = plugin.FindPlayerCurrency(name);
 						if (p == null)
 						{
 							sender.sendMessage("This account doesnt have any credits.");
 							return;
 						}
 						sender.sendMessage("This account has " + p.GetCurrency() + " credits.");
 					}
 				}
 				else if (args.length == 4)
 				{
 					String arg1 = args[1];
 					String name = args[2];
 					int amount = Integer.parseInt(args[3]);
 					if (arg1.matches("add"))
 					{
 						if (amount > 1000)
 						{
 							sender.sendMessage("You cannot add that much!");
 							return;
 						}
 						if (amount <= 0)
 						{
 							sender.sendMessage("Amount has to be > 0!");
 							return;
 						}
 						GugaVirtualCurrency p = plugin.FindPlayerCurrency(name);
 						if (p == null)
 						{
 							sender.sendMessage("Couldnt find player with this name");
 							return;
 						}
 						p.AddCurrency(amount);
 						Player dest = plugin.getServer().getPlayer(name);
 						if (dest != null)
 							dest.sendMessage("You received +" + amount + " credits!");
 						sender.sendMessage("You added +" + amount + " credits to " + name);
 					}
 					else if (arg1.matches("remove"))
 					{
 						if (amount > 1000)
 						{
 							sender.sendMessage("You cannot remove that much!");
 							return;
 						}
 						if (amount <= 0)
 						{
 							sender.sendMessage("Amount has to be > 0!");
 							return;
 						}
 						GugaVirtualCurrency p = plugin.FindPlayerCurrency(name);
 						if (p == null)
 						{
 							sender.sendMessage("Couldnt find player with this name");
 							return;
 						}
 						p.RemoveCurrency(amount);
 						plugin.getServer().getPlayer(name).sendMessage("You lost +" + amount + " credits!");
 						sender.sendMessage("You removed +" + amount + " credits from " + name);
 					}
 				}
 			}
 			else if (subCommand.matches("genblock") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (args.length == 5)
 				{
 					int typeID = Integer.parseInt(args[1]);
 					int x = Integer.parseInt(args[2]);
 					int y = Integer.parseInt(args[3]);
 					int z = Integer.parseInt(args[4]);
 					plugin.GenerateBlockType(sender, typeID, x, y, z);
 				}
 			}
 			else if (subCommand.matches("replace")&& GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (args.length == 6)
 				{
 					int typeID1 = Integer.parseInt(args[1]);
 					int typeID2 = Integer.parseInt(args[2]);
 					int x = Integer.parseInt(args[3]);
 					int y = Integer.parseInt(args[4]);
 					int z = Integer.parseInt(args[5]);
 					plugin.GenerateBlockType2(sender, typeID1, typeID2, x, y, z);
 				}
 			}
 			else if (subCommand.matches("setvip") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (args.length == 3)
 				{
 					String pName = args[1];
 					int months = Integer.parseInt(args[2]);
 					Calendar c = Calendar.getInstance();
 					c.setTime(new Date());
 					//int i = 0;
 					//while (i < months)
 					//{
 						//c.roll(Calendar.MONTH, true);
 					c.add(Calendar.MONTH, months);
 						//i++;
 					//}
 					GugaVirtualCurrency p = plugin.FindPlayerCurrency(pName);
 					p.SetExpirationDate(c.getTime());
 					sender.sendMessage("Vip Status for " + pName + " is active till " + c.getTime());
 				}
 			}
 			else if (subCommand.matches("rank") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if(args.length == 4)
 				{
 					String arg1 = args[1];
 					String arg2 = args[2];
 					String arg3 = args[3];
 					if(arg1.matches("add"))
 					{
 						Player target = plugin.getServer().getPlayer(arg2);
 						if(target != null)
 						{
 							if(arg3.equalsIgnoreCase("eventer")||arg3.equalsIgnoreCase("builder")||arg3.equalsIgnoreCase("admin")||arg3.equalsIgnoreCase("gamemaster"))
 							{
 								if(!GameMasterHandler.gameMasters.contains(GameMasterHandler.GetGMByName(target.getName())))
 								{
 									GameMasterHandler.AddGMIng(target.getName(), arg3.toUpperCase());
 									sender.sendMessage("User was succesfully added to GMs file!");
 								}
 								else
 								{
 									sender.sendMessage("User cannot be removed, because he already exists in GMs file. - Delete him to change rank.");
 								}
 							}
 							else
 							{
 								sender.sendMessage("This rank can't be added.");
 							}
 						}
 						else
 						{
 							sender.sendMessage("This player doesn't exist.");
 						}
 					}
 				}
 				else if(args.length == 3)
 				{
 					String arg1 = args[1];
 					String arg2 = args[2];
 					if(arg1.matches("remove"))
 					{
 						Player target = plugin.getServer().getPlayer(arg2);
 						if(target != null)
 						{
 							GameMasterHandler.RemoveGMIng(arg2);
 							sender.sendMessage("User was succesfully removed from GMs file!");
 						}
 						else
 						{
 							sender.sendMessage("This player doesn't exist.");
 						}
 					}
 				}
 			}
 			else if (subCommand.matches("tp"))
 			{
 				if (args.length == 4)
 				{
 					int x = Integer.parseInt(args[1]);
 					int y = Integer.parseInt(args[2]);
 					int z = Integer.parseInt(args[3]);
 					Location loc = new Location(sender.getWorld(), x, y, z);
 					sender.teleport(loc);
 				}
 			}
 			else if (subCommand.matches("spectate") && GameMasterHandler.IsAdmin(sender.getName()))
 			{
 				if (args.length == 3)
 				{
 					String arg1 = args[1];
 					if (arg1.matches("player"))
 					{
 						String tarName = args[2];
 						Player tarPlayer = plugin.getServer().getPlayer(tarName);
 						spectation.put(tarPlayer.getName(), new GugaSpectator(plugin,tarPlayer,sender));
 						sender.sendMessage("Starting spectation.");
 					}
 				}
 			}
 		}
 	}
 	public static void CommandWorld(Player sender)
 	{
 		if(plugin.professions.get(sender.getName()).GetLevel() >= 10)
 		{
 			if(BasicWorld.IsBasicWorld(sender.getLocation()))
 			{
 				BasicWorld.BasicWorldLeaveToWorld(sender);
 			}
 			else
 			{
 				ChatHandler.FailMsg(sender, "V tomto svete nelze prikaz pouzit!");
 			}
 		}
 		else
 		{
 			ChatHandler.FailMsg(sender, "Jeste nemate level 10!");
 		}
 	}
 	public static void CommandLogin(Player sender, String args[])
 	{
 		 String pass = args[0];
 		 if (plugin.acc.UserIsRegistered(sender))
 		 {
 			 if (!plugin.acc.UserIsLogged(sender))
 			 {
 				 if (plugin.acc.LoginUser(sender, pass))
 				 {
 					plugin.acc.tpTask.remove(sender.getName());
 					sender.teleport(plugin.acc.playerStart.get(sender.getName()));
 					ChatHandler.SuccessMsg(sender, "Byl jste uspesne prihlasen!");
 					if(plugin.professions.get(sender.getName()).GetXp() == 0 && !BasicWorld.IsBasicWorld(sender.getLocation()))
 					{
 						BasicWorld.BasicWorldEnter(sender);
 						ChatHandler.SuccessMsg(sender, "Vitejte ve svete pro novacky!");
 					}
 				 }
 				 else
 				 {
 					 ChatHandler.FailMsg(sender, "Prihlaseni se nezdarilo!");
 					 return;
 				 }
 				 if(!GugaBanHandler.IsIpWhitelisted(sender.getName()))
 						 GugaBanHandler.UpdateBanAddr(sender.getName());
 				/* GugaProfession prof;
 					if ((prof = plugin.professions.get(sender.getName())) != null)
 					{
 						if (prof instanceof GugaHunter)
 						{
 							((GugaHunter)prof).StartRegenHp();
 						}
 					}*/
 			 }
 			 else
 			 {
 				 ChatHandler.FailMsg(sender, "Jiz jste prihlasen!");
 			 }
 		 }
 		 else
 		 {
 			 ChatHandler.FailMsg(sender, "Nejdrive se zaregistrujte!");
 		 }
 	}
 	public static void CommandDebug()
 	{
 		plugin.debug = !plugin.debug;
 		plugin.log.info("DEBUG="+plugin.debug);
 	}
 	/*public static void CommandPassword(Player sender, String args[])
 	{
 		if (plugin.acc.UserIsRegistered(sender))
 		{
 			if (plugin.acc.UserIsLogged(sender))
 			{
 				if (args.length == 2)
 				{
 					plugin.acc.ChangePassword(sender, args[0], args[1]);
 				}
 				else
 				{
 					sender.sendMessage("Prosim vlozte vase stare a nove heslo");
 				}
 			}
 			else
 			{
 				sender.sendMessage("Nejdrive se musite prihlasit!");
 			}
 		}
 		else
 		{
 			sender.sendMessage("Nejdrive se musite zaregistrovat!");
 		}
 	}*/
 	private static void ToggleInvisibility(Player sender, String pName)
 	{
 		Player p = plugin.getServer().getPlayer(pName);
 		GugaInvisibility inv;
 		if (!p.isOnline())
 		{
 			return;
 		}
 		if ( (inv = invis.get(p)) != null)
 		{
 			inv.Stop();
 			invis.remove(p);
 			sender.sendMessage("Invisibility for " + pName + " has been turned off");
 		}
 		else
 		{
 			inv = new GugaInvisibility(p, 50, plugin);
 			inv.Start();
 			invis.put(p, inv);
 			sender.sendMessage("Invisibility for " + pName + " has been turned on");
 		}
 	}
 	private static void Teleport(Player sender,String name)
 	{
 		if (plugin.arena.IsArena(sender.getLocation()))
 		{
 			sender.sendMessage("V arene nemuzete pouzit prikaz /places!");
 			return;
 		}
 		if (plugin.EventWorld.IsEventWorld(sender.getLocation()))
 		{
 			sender.sendMessage("V EventWorldu nemuzete pouzit prikaz /places!");
 			return;
 		}
 		Places place;
 		if ( (place = PlacesHandler.getPlaceByName(name)) != null)
 		{
 			if (PlacesHandler.CanTeleport(place.getPortName(), sender.getName()) || GameMasterHandler.IsAtleastGM(sender.getName()))
 			{
 				PlacesHandler.teleport(sender, place);
 				return;
 			}
 		}
 		sender.sendMessage("Toto misto neexistuje!");
 	}
 	private static boolean RemoveSpectation(Player spectator)
 	{
 		Iterator<Entry<String, GugaSpectator>> i;
 		i = spectation.entrySet().iterator();
 		while (i.hasNext())
 		{
 			Entry<String, GugaSpectator> element = i.next();
 			GugaSpectator spec = element.getValue();
 			if (spec.GetSpectator().getName().matches(spectator.getName()))
 			{
 				spec.SpectateStop();
 				i.remove();
 				return true;
 			}
 		}
 		return false;
 	}
 	private static int ID_CHEST=54;
 	private static int ID_DISPENSER=23;
 	private static int ID_FURNANCE=61;
 	private static int ID_FURNANCE_BURNING=62;
 	public static HashMap<Player, Player> vipTeleports = new HashMap<Player, Player>();
 	public static HashMap<Player, Player> reply = new HashMap<Player, Player>();
 	public static ArrayList<String> godMode = new ArrayList<String>();
 	public static ArrayList<String> fly = new ArrayList<String>();
 	public static int x1 = 0;
 	public static int x2 = 0;
 	public static int z1 = 0;
 	public static int z2 = 0;
 	public static HashMap<Player, GugaInvisibility> invis = new HashMap<Player, GugaInvisibility>();
 	public static HashMap<String, GugaSpectator> spectation = new HashMap<String, GugaSpectator>(); // <target, GugaSpectator>
 	public static ArrayList<String> disabledGMs = new ArrayList<String>();
 	public static String FeedbackFile = "plugins/FeedbackFile.dat";
 	private static Guga_SERVER_MOD plugin;
 
 }
