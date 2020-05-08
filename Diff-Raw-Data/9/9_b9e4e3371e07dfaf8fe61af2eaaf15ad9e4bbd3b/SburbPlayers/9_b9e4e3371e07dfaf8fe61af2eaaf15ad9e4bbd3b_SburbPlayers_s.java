 package com.benzrf.sblock.sburbplayers;
 
 import com.benzrf.sblock.sburbplayers.commandparser.ArgumentType;
 import com.benzrf.sblock.sburbplayers.commandparser.CommandNode;
 import com.benzrf.sblock.sburbplayers.commandparser.CommandParser;
 import com.benzrf.sblock.sburbplayers.commandparser.ExecutableCommandNode;
 
 import com.google.gson.Gson;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.SkullType;
 import org.bukkit.block.Skull;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerBedLeaveEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import org.yaml.snakeyaml.Yaml;
 
 public class SburbPlayers extends JavaPlugin implements Listener
 {
 	@Override
 	public void onDisable()
 	{
 		try
 		{
 			new Yaml().dump(this.towers, new FileWriter("plugins/SburbPlayers/towers.yml"));
 			new Yaml().dump(this.tpacks, new FileWriter("plugins/SburbPlayers/tpacks.yml"));
 			for (Player p : this.getServer().getOnlinePlayers())
 			{
 				writePlayer(p);
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onEnable()
 	{
 		getServer().getPluginManager().registerEvents(this, this);
 		try
 		{
 			this.towers = ((HashMap<String, String>) new Yaml().loadAs(new FileReader("plugins/SburbPlayers/towers.yml"), HashMap.class));
 			this.tpacks = ((HashMap<String, String>) new Yaml().loadAs(new FileReader("plugins/SburbPlayers/tpacks.yml"), HashMap.class));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		for (Player p : this.getServer().getOnlinePlayers())
 		{
 			try
 			{
 				readPlayer(p);
 				if (p.getWorld().getName().equals("InnerCircle") || p.getWorld().getName().equals("OuterCircle"))
 				{
 					p.setAllowFlight(true);
 				}
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		this.root = new CommandNode("sp");
 		new ExecutableCommandNode("i", this.root, SburbPlayer.class, "getInfo", ArgumentType.PLAYER);
 		new ExecutableCommandNode("info", this.root, SburbPlayer.class, "getInfo", ArgumentType.PLAYER);
 		instance = this;
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getPlayer().getItemInHand() != null)
 		{
 			this.used.put(event.getPlayer(), event.getPlayer().getItemInHand().clone());
 		}
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		if (!event.isCancelled() && event.getBlock().getState() instanceof Skull && ((Skull) event.getBlock().getState()).getSkullType().equals(SkullType.WITHER))
 		{
 			Item i = event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SKULL_ITEM));
 			i.getItemStack().setDurability((short) 1);
 			ItemMeta im = i.getItemStack().getItemMeta();
 			im.setDisplayName(((Skull) event.getBlock().getState()).getOwner());
 			i.getItemStack().setItemMeta(im);
 			event.setCancelled(true);
 			event.getBlock().setTypeId(0);
 		}
 	}
 	
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		if (event.getBlock().getState() instanceof Skull && ((Skull) event.getBlock().getState()).getSkullType().equals(SkullType.ZOMBIE))
 		{
 			event.setCancelled(true);
 		}
 		else if (event.getBlock().getState() instanceof Skull && ((Skull) event.getBlock().getState()).getSkullType().equals(SkullType.WITHER) && this.used.get(event.getPlayer()) != null)
 		{
 			((Skull) event.getBlock().getState()).setOwner(this.used.get(event.getPlayer()).getItemMeta().getDisplayName());
 		}
 	}
 	
 	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) throws IOException, ClassNotFoundException
 	{
 		readPlayer(event.getPlayer());
 		if (tpacks.containsKey(event.getPlayer().getWorld().getName()))
 		{
 			((CraftPlayer) event.getPlayer()).getHandle().a(this.tpacks.get(event.getPlayer().getWorld().getName()), 16);
 		}
 		if (event.getPlayer().getWorld().getName().equals("InnerCircle") || event.getPlayer().getWorld().getName().equals("OuterCircle"))
 		{
 			event.getPlayer().setAllowFlight(true);
 		}
 		BufferedWriter w = new BufferedWriter(new FileWriter("plugins/SburbPlayers/ips/" + event.getPlayer().getAddress().getAddress().getHostAddress()));
 		w.write(event.getPlayer().getName());
 		w.flush();
 		w.close();
 		com.benzrf.services.Services.statement.executeUpdate("INSERT INTO players (name, ip) VALUES ('" + event.getPlayer().getName() + "', '" + event.getPlayer().getAddress().getAddress().getHostAddress() + "');");
 	}
 	private void readPlayer(Player p) throws IOException, ClassNotFoundException
 	{
 		if (!this.players.containsKey(p.getName()))
 		{
 			if (new File("plugins/SburbPlayers/u_" + p.getName() + ".spd").exists())
 			{
 				SburbPlayer sp = this.gson.fromJson(this.readFile("plugins/SburbPlayers/u_" + p.getName() + ".spd"), SburbPlayer.class);
 				sp.player = p;
 				this.players.put(p.getName(), sp);
 			}
 			else
 			{
 				this.players.put(p.getName(), new SburbPlayer(p, SClass.Heir, Aspect.Breath, MPlanet.LOWAS, CPlanet.Prospit, Integer.toString(new Random().nextInt(3))));
 			}
 		}
 	}
 	
 	private String readFile(String path) throws IOException
 	{
 		String file;
 		FileInputStream stream = new FileInputStream(new File(path));
 		try
 		{
 			FileChannel fc = stream.getChannel();
 			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 			file = Charset.defaultCharset().decode(bb).toString();
 		}
 		finally
 		{
 			stream.close();
 		}
 		return file;
 	}
 
 	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) throws IOException
 	{
 		writePlayer(event.getPlayer());
 		new File("plugins/SburbPlayers/ips/" + event.getPlayer().getAddress().getAddress().getHostAddress()).delete();
 		com.benzrf.services.Services.statement.executeUpdate("DELETE FROM players WHERE name = '" + event.getPlayer().getName() + "';");
 	}
 	private void writePlayer(Player p) throws IOException
 	{
 		if (this.players.containsKey(p.getName()))
 		{
 			BufferedWriter w = new BufferedWriter(new FileWriter("plugins/SburbPlayers/u_" + p.getName() + ".spd"));
 			w.write(this.gson.toJson(this.players.get(p.getName())));
 			w.flush();
 			w.close();
 			this.players.remove(p.getName());
 		}
 	}
 
 	@EventHandler
 	public void onPlayerBedEnter(PlayerBedEnterEvent event)
 	{
 		if (this.players.containsKey(event.getPlayer().getName()))
 		{
 			((SburbPlayer)this.players.get(event.getPlayer().getName())).inBed = true;
 			final String name = event.getPlayer().getName();
 			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
 				public void run()
 				{
 						if (players.containsKey(name))
 						{
 							SburbPlayer sp = (SburbPlayer) players.get(name);
 							if (sp.inBed)
 							{
 								sp.inBed = false;
 								((CraftPlayer) sp.player).getHandle().a(false, true, false);
 								if (sp.player.getWorld().getName().equals("InnerCircle") || sp.player.getWorld().getName().equals("OuterCircle"))
 								{
 									sp.dreamingloc = SburbPlayers.lts(sp.player.getLocation());
 									sp.player.teleport(SburbPlayers.stl(sp.sleepingloc));
 									sp.player.setAllowFlight(false);
 									sp.player.setFlying(false);
 								}
 								else
 								{
 									sp.sleepingloc = SburbPlayers.lts(sp.player.getLocation());
 									sp.player.teleport(sp.dreamingloc.equals("") ? SburbPlayers.stl((String)SburbPlayers.instance.towers.get(sp.cplanet.toString() + sp.bed)) : SburbPlayers.stl(sp.dreamingloc));
 									sp.player.setAllowFlight(true);
 								}
 							}
 						}
 				}
 			}, 80L);
 		}
 	}
 
 	@EventHandler
 	public void onPlayerBedLeave(PlayerBedLeaveEvent event)
 	{
 		if (this.players.containsKey(event.getPlayer().getName()))
 		{
 			((SburbPlayer)this.players.get(event.getPlayer().getName())).inBed = false;
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
 	{
 		if (tpacks.containsKey(event.getPlayer().getWorld().getName()))
 		{
 			((CraftPlayer) event.getPlayer()).getHandle().a(this.tpacks.get(event.getPlayer().getWorld().getName()), 16);
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		if (commandLabel.equals("setsp"))
 		{
 			if (!sender.isOp())
 			{
 				return true;
 			}
 			Player p = getServer().getPlayer(args[0]);
 			if (p == null)
 			{
 				return true;
 			}
 
 			if (args[1].equals("class"))
 			{
 				((SburbPlayer)this.players.get(p.getName())).sclass = SClass.valueOf(args[2]);
 			}
 			else if (args[1].equals("aspect"))
 			{
 				((SburbPlayer)this.players.get(p.getName())).aspect = Aspect.valueOf(args[2]);
 			}
 			else if (args[1].equals("mplanet"))
 			{
 				((SburbPlayer)this.players.get(p.getName())).mplanet = MPlanet.valueOf(args[2]);
 			}
 			else if (args[1].equals("cplanet"))
 			{
 				((SburbPlayer)this.players.get(p.getName())).cplanet = CPlanet.valueOf(args[2]);
 			}
 			else if (args[1].equals("bed"))
 			{
 				((SburbPlayer)this.players.get(p.getName())).bed = args[2];
 			}
 
 		}
 		else if (commandLabel.equals("getsp"))
 		{
 			if (!sender.isOp())
 			{
 				return true;
 			}
 			Player p = getServer().getPlayer(args[0]);
 			if (p == null)
 			{
 				return true;
 			}
 
 			SburbPlayer sp = (SburbPlayer)this.players.get(p.getName());
 			sender.sendMessage(sp.sclass + ", " + sp.aspect + ", " + sp.mplanet + ", " + sp.cplanet + ", " + sp.bed);
 		}
 		else if (commandLabel.equals("addtower"))
 		{
 			if (!sender.isOp())
 			{
 				return true;
 			}
 			this.towers.put(args[0], lts(((Player)sender).getLocation()));
 		}
 		else if (commandLabel.equals("addtpack"))
 		{
 			if (!sender.isOp())
 			{
 				return true;
 			}
 			this.tpacks.put(((Player)sender).getLocation().getWorld().getName(), args[0]);
 		}
 		else if (commandLabel.equals("readcaptcha"))
 		{
 			if (!sender.isOp())
 			{
 				return true;
 			}
 			sender.sendMessage(CaptchaCoder.encode(((Player) sender).getItemInHand()));
 		}
 		else if (commandLabel.equals("makecaptcha"))
 		{
 			if (!sender.isOp())
 			{
 				return true;
 			}
 			((Player) sender).getWorld().dropItem(((Player) sender).getLocation(), CaptchaCoder.decode(args[0]));
 		}
 		else
 		{
 			if (this.players.containsKey(sender.getName())) CommandParser.runCommand(args, this.root, this.players.get(sender.getName()));
 		}
 		return true;
 	}
 	
 	public String getCaptcha(String p)
 	{
 		return CaptchaCoder.encode(this.getServer().getPlayer(p).getItemInHand());
 	}
 	
 	public static String lts(Location l)
 	{
 		String out = "";
 		out = out + l.getWorld().getName() + ",";
 		out = out + l.getBlockX() + ",";
 		out = out + l.getBlockY() + ",";
 		out = out + l.getBlockZ();
 		return out;
 	}
 	
 	public static Location stl(String s)
 	{
 		String[] parts = s.split(",");
 		try
 		{
 			return new Location(instance.getServer().getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
 		}
 		catch (ArrayIndexOutOfBoundsException e)
 		{
 		}
 		return null;
 	}
 	
 	public SburbPlayer getPlayer(String name)
 	{
 		return this.players.get(name);
 	}
 	
 	public String prefix()
 	{
 		return prefix;
 	}
 	
 	public static SburbPlayers instance;
 	private Map<String, SburbPlayer> players = new HashMap<String, SburbPlayer>();
 	private Map<Player, ItemStack> used = new HashMap<Player, ItemStack>();
 	public Map<String, String> towers = new HashMap<String, String>();
 	public Map<String, String> tpacks = new HashMap<String, String>();
 	private CommandNode root;
 	private String prefix = ChatColor.WHITE + "[" + ChatColor.GREEN + "Sburb" + ChatColor.RED + "Players" + ChatColor.WHITE + "] ";
 	private Gson gson = new Gson();
 }
 
