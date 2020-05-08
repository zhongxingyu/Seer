 package me.corriekay.pppopp3.modules;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import me.corriekay.pppopp3.Mane;
 import me.corriekay.pppopp3.utils.PSCmdExe;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import com.sk89q.worldedit.regions.Region;
 
 public class WorldEditHookModule extends PSCmdExe {
 
 	private final ArrayList<String> commands = new ArrayList<String>();
 	private final HashMap<String, Selection> playerAreas = new HashMap<String, Selection>();
 	private final FileConfiguration config;
 	private final WorldEditPlugin wep = ((WorldEditPlugin)Bukkit.getPluginManager().getPlugin("WorldEdit"));
 	public WorldEditHookModule(){
 		super("WorldEditHookModule", "claimplot", "releaseplot");
 		config = getNamedConfig("wehook.yml");
 		commands.add("//set");
 		commands.add("//replace");
 		commands.add("//overlay");
 		commands.add("//walls");
 		commands.add("//outline");
 		commands.add("//faces");
 		commands.add("//sel");
 		commands.add("/;");
 		loadAreas();
 	}
 	private void loadAreas(){
 		playerAreas.clear();
 		for(String player : config.getKeys(false)){
 			Location l;
 			double x,z;
 			x = config.getInt(player+".x");
 			z = config.getInt(player+".z");
 			World w = Bukkit.getWorld("equestria");
 			l = new Location(w,x,1,z);
 			Location max,min;
 			max = new Location(l.getWorld(),l.getX()-50,2,l.getZ()-50);
 			min = new Location(l.getWorld(),l.getX()+50,l.getWorld().getMaxHeight(),l.getZ()+50);
 			Selection s = new CuboidSelection(w, min, max);
 			playerAreas.put(player, s);
 		}
 	}
 	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(!(sender instanceof Player)){
 			sendMessage(sender,notPlayer);
 			return true;
 		}
 		final Player player = (Player)sender;
 		final BukkitScheduler s = Bukkit.getScheduler();
 		if(cmd.getName().equals("claimplot")){
 			s.scheduleAsyncDelayedTask(Mane.getInstance(), new Runnable(){
 				public void run(){
 					Selection sel = playerAreas.get(player.getName());
 					if(sel != null){
 						String answer = questioner.ask(player, pinkieSays+"You already have a creative plot! Are you sure you wish to abandon it for a new one?", "yes","no");
 						if(answer.equals("no")){
 							sendSyncMessage(player,"Okay! Aborting!");
 							return;
 						} else if(answer.equals("yes")){
 							String answer2 = questioner.ask(player, pinkieSays+"Are you ABSOLUTELY sure? This will REMOVE your protection in the area, allowing others to claim it!", "yes","no");
 							if(answer2.equals("no")){
 								sendSyncMessage(player,"Okay! Aborting!");
 								return;
 							} else if(answer2.equals("yes")){
 								sendSyncMessage(player,"Alright! Removing old plot claim and setting the new one!");
 								s.scheduleSyncDelayedTask(Mane.getInstance(),new Runnable(){
 									public void run(){
 										try {
 											removeProtection(player.getName());
 											addProtection(player);
 										} catch (Exception e) {
 											// TODO Auto-generated catch block
 											e.printStackTrace();
 										}
 									}
 								});
 							}
 						}
 					} else {
 						s.scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
 							public void run(){
 								sendMessage(player,"Claiming plot!");
 								try {
 									addProtection(player);
 								} catch (Exception e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 								return;
 							}
 						});
 					}
 				}
 			});
 		}
 		if(cmd.getName().equals("releaseplot")){
 			Selection sel = playerAreas.get(player.getName());
 			if(sel == null){
 				sendMessage(player,"Silly, you dont have a creative plot!");
 				return true;
 			}
 			s.scheduleAsyncDelayedTask(Mane.getInstance(), new Runnable(){
 				public void run(){
 					String answer = questioner.ask(player, pinkieSays+"Are you sure you want to remove your plot? Players will be able to edit what you've created there!", "yes", "no");
 					if(answer.equals("no")){
 						sendSyncMessage(player,"Okay! Aborting!");
 						return;
 					} else {
 						answer = questioner.ask(player, pinkieSays+"Are you ABSOLUTELY sure? This will REMOVE your protection in the area, allowing others to claim it!", "yes","no");
 						if(answer.equals("no")){
 							sendSyncMessage(player,"Okay! Aborting!");
 							return;
 						} else {
 							s.scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
 								public void run(){
 									try {
 										sendMessage(player,"Releaseing the plot!");
 										removeProtection(player.getName());
 									} catch (Exception e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									}
 								}
 							});
 						}
 					}
 				}
 			});
 		}
 		return true;
 	}
 	private void removeProtection(String player) throws Exception {
 		removeBedrock(playerAreas.get(player).getRegionSelector().getRegion(),Bukkit.getWorld("equestria"));
 		config.set(player,null);
 		saveNamedConfig("wehook.yml",config);
 		loadAreas();
 	}
 	private void removeBedrock(Region r,World w){
 		for(BlockVector bv : r){
			Block b = w.getBlockAt(bv.getBlockX(),1,bv.getBlockZ());
 			if(b.getType() == Material.BEDROCK){
 				b.setType(Material.GRASS);
 			}
 		}
 	}
 	private boolean addProtection(Player player) throws Exception{
 		Location l = player.getLocation();
 		l.setY(1);
 		
 		//get selection
 		Location one, two;
 		one = new Location(l.getWorld(),l.getBlockX()-50, 1, l.getBlockZ()-50);
 		two = new Location(l.getWorld(),l.getBlockX()+50, 1, l.getBlockZ()+50);
 		Selection bedrocksel = new CuboidSelection(l.getWorld(),one,two);
 		Selection finalsel = new CuboidSelection(l.getWorld(),one,new Location(l.getWorld(),two.getBlockX(),l.getWorld().getMaxHeight()-1,two.getBlockZ()));
 		
 		//compare if it already built stuff / intersects
 		for(BlockVector bv : finalsel.getRegionSelector().getRegion()){
 			Block b = l.getWorld().getBlockAt(bv.getBlockX(),bv.getBlockY(),bv.getBlockZ());
 			Material m = b.getType();
 			if(!(m==Material.BEDROCK||m==Material.AIR||m==Material.GRASS||m==Material.DIRT)){
 				//System.out.println(m.name());
 				sendMessage(player,"This area has built stuff on it already! It is unclaimable! (hint: there needs to be a 50 block radius in unclaimed, untouched area to claim an area!)");
 				return false;
 			}
 		}
 		for (Selection othersel : playerAreas.values()){
 			if(intersects(othersel,finalsel)){
 				sendMessage(player,"This area is intersecting an already claimed area! It is unclaimable! (hint: there needs to be a 50 block radius in unclaimed, untouched area to claim an area!)");
 				return false;
 			}
 		}
 		
 		
 		//All checks out! lets lay down the bedrock, and commit the selection
 		makeBedrock(bedrocksel,l.getWorld());
 		
 		config.set(player.getName()+".x", l.getBlockX());
 		config.set(player.getName()+".z", l.getBlockZ());
 		saveNamedConfig("wehook.yml",config);
 		
 		loadAreas();
 		return true;
 	}
     private boolean intersects(Selection s1, Selection s2){
         Location s1Min, s1Max, s2Min, s2Max;
         s1Min = s1.getMinimumPoint();
         s1Max = s1.getMaximumPoint();
         s2Min = s2.getMinimumPoint();
         s2Max = s2.getMaximumPoint();
 
 
         if((s1Min.getBlockX() <= s2Max.getBlockX()) && (s2Min.getBlockX() <= s1Max.getBlockX()))
         {
                 if((s1Min.getBlockZ() <= s2Max.getBlockZ()) && (s2Min.getBlockZ() <= s1Max.getBlockZ()))
                 {
                         return true;
                 }
         }
         return false;
     }
 	private void makeBedrock(Selection sel, World w) throws Exception{
 		int maxX, minX, maxZ, minZ;
 		maxX = Math.max(sel.getMinimumPoint().getBlockX(), sel.getMaximumPoint().getBlockX());
 		minX = Math.min(sel.getMinimumPoint().getBlockX(), sel.getMaximumPoint().getBlockX());
 		maxZ = Math.max(sel.getMinimumPoint().getBlockZ(), sel.getMaximumPoint().getBlockZ());
 		minZ = Math.min(sel.getMinimumPoint().getBlockZ(), sel.getMaximumPoint().getBlockZ());
 		
 		Region bvs = sel.getRegionSelector().getRegion();
 		for(BlockVector bv : bvs){
 			Block b = w.getBlockAt(bv.getBlockX(),bv.getBlockY(),bv.getBlockZ());
 			int x, z;
 			x = b.getX();
 			z = b.getZ();
 			//if along min/max z
 			if(z == maxZ || z == minZ){
 				if(x>=minX||x<=maxX){
 					b.setType(Material.BEDROCK);
 				}
 			}
 			if(x == maxX || x == minX){
 				if(z>=minZ||z<=maxZ){
 					b.setType(Material.BEDROCK);
 				}
 			}
 		}
 	}
 	private boolean canModify(Location l, Player p){
 		if(p.hasPermission("pppopp3.creativeadmin")){
 			return true;
 		}
 		if(l.getBlock().getY()<2){
 			return false;
 		}
 		for(String p2 : playerAreas.keySet()){
 			Selection s = playerAreas.get(p2);
 			if(s.contains(l)){
 				if(p2.equals(p.getName())){
 					continue;
 				} else {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	@EventHandler
 	public void onCommand(PlayerCommandPreprocessEvent event){
 		if(event.getPlayer().hasPermission("pppopp3.creativeadmin")){
 			return;
 		}
 		String[] msgWords = event.getMessage().split(" ");
 		String cmd = msgWords[0];
 		String[] args = new String[msgWords.length-1];
 		for(int i = 1; i<msgWords.length;i++){
 			args[i-1] = msgWords[i];
 		}
 		if(cmd.equals("//;")||cmd.equals("/;")){
 			event.setCancelled(true);
 			return;
 		}
 		if(!event.getPlayer().getWorld().getName().equals("equestria")){
 			return;
 		}
 		if(commands.contains(cmd)){
 			final Player p = event.getPlayer();
 			final Selection sel = wep.getSelection(p);
 			if(cmd.equals("//sel")||cmd.equals("/;")){
 				if(args.length>0&&!(args[0].equals("cuboid"))){
 					sendMessage(p,"Please only use cuboid selections!");
 					event.setCancelled(true);
 					return;
 				}
 				return;
 			}
 			if(sel==null){
 				sendMessage(p,"Hey, you need to make a world edit selection first! pull out the wooden axe and use that, or type //pos1 and //pos2!");
 				event.setCancelled(true);
 				return;
 			}
 			if(!(sel instanceof CuboidSelection)){
 				sendMessage(p,"Please dont use anything but a cuboid selection (//sel cuboid). It makes me cry if you dont!");
 				event.setCancelled(true);
 				return;
 			}
 			Selection playersel = playerAreas.get(p.getName());
 			if(playersel == null){
 				sendMessage(p,"Hey, you dont have a registered plot! type /claimplot to get your own world edit area!");
 				event.setCancelled(true);
 				return;
 			}
 			boolean contains = (playersel.contains(sel.getMaximumPoint())&&playersel.contains(sel.getMinimumPoint()));
 			if(!contains){
 				sendMessage(p, "Hey, your world edit selection isnt contained within your area! you cant edit an area outside of your plot.");
 				event.setCancelled(true);
 				return;
 			} else {
 				if(sel.getArea()>10000){
 					
 				}
 			}
 		}
 	}
 	@EventHandler
     public void onFromTo(BlockFromToEvent event)
     {
 		if(!event.getBlock().getWorld().getName().equals("equestria")){
 			return;
 		}
         int id = event.getBlock().getTypeId();
         if(id >= 8 && id <= 11)
         {
             Block b = event.getToBlock();
             int toid = b.getTypeId();
             if(toid == 0)
             {
                 if(generatesCobble(id, b))
                 {
                     event.setCancelled(true);
                 }
             }
         }
     }
  
     private final BlockFace[] faces = new BlockFace[]
         {
             BlockFace.SELF,
             BlockFace.UP,
             BlockFace.DOWN,
             BlockFace.NORTH,
             BlockFace.EAST,
             BlockFace.SOUTH,
             BlockFace.WEST
         };
  
     public boolean generatesCobble(int id, Block b)
     {
         int mirrorID1 = (id == 8 || id == 9 ? 10 : 8);
         int mirrorID2 = (id == 8 || id == 9 ? 11 : 9);
         for(BlockFace face : faces)
         {
             Block r = b.getRelative(face, 1);
             if(r.getTypeId() == mirrorID1 || r.getTypeId() == mirrorID2)
             {
                 return true;
             }
         }
         return false;
     }
 	@EventHandler
 	public void breakBlock(BlockBreakEvent event){
 		if(!event.getBlock().getWorld().getName().equals("equestria")){
 			return;
 		}
 		Player p = event.getPlayer();
 		event.setCancelled(!canModify(event.getBlock().getLocation(),p));
 	}
 	@EventHandler
 	public void placeBlock(BlockPlaceEvent event){
 		if(!event.getBlock().getWorld().getName().equals("equestria")){
 			return;
 		}
 		Player p = event.getPlayer();
 		event.setCancelled(!canModify(event.getBlock().getLocation(),p));
 	}
 }
