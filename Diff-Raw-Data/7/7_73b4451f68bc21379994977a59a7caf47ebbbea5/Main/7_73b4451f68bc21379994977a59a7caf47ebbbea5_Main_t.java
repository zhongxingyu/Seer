 
 
 package com.comze_instancelabs.skins;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import javax.imageio.ImageIO;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 //import org.bukkit.material.Sign;
 
 
 
 /**
  * 
  * @author instancelabs
  *
  */
 
 public class Main extends JavaPlugin implements Listener {
 	
 
 	//TODO:
 	// [HIGH] cover all colors (0.6 Mio to go)
 	
 	
 	private boolean skin_updating = false;
 	private volatile BukkitTask task = null;
     private static int interval = 15; // minutes
 	
 	
 	public String newline = System.getProperty("line.separator");
 	
 	
 	//public static HashMap<Player, Location> undoloc = new HashMap<Player, Location>();
 	//public static HashMap<Player, String> undoskin = new HashMap<Player, String>();
 	//public static HashMap<Player, String> undodir = new HashMap<Player, String>();
 	//public static HashMap<Player, String> undo_uuid = new HashMap<Player, String>();
 	
 	public static HashMap<Player, ArrayList<Location>> undoloc = new HashMap<Player, ArrayList<Location>>();
 	public static HashMap<Player, ArrayList<String>> undoskin = new HashMap<Player, ArrayList<String>>();
 	public static HashMap<Player, ArrayList<String>> undodir = new HashMap<Player, ArrayList<String>>();
 	public static HashMap<Player, ArrayList<String>> undo_uuid = new HashMap<Player, ArrayList<String>>();
 	
 	
 	
 	@Override
 	public void onEnable(){
 		getServer().getPluginManager().registerEvents(this, this);
 		
 		getConfig().options().header("Check out the bukkitdev page (http://dev.bukkit.org/bukkit-plugins/skin-statue-builder/) for more information on these settings.");
 		getConfig().addDefault("config.auto_updating", true);
 		getConfig().addDefault("config.auto_skin_updating", false);
 		getConfig().addDefault("config.auto_skin_updating_interval_minutes", 30);
 		getConfig().addDefault("config.use_worldguard", false);
 		getConfig().options().copyDefaults(true);
 		this.saveConfig();
 		
 		try{
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 			// Failed to submit the stats :(
 		}
 		
 		
 		// own metrics system, still in testing
 		// will replace Hidendras metrics system (above) with this one, if it works
 		/*try {
 			MyMetrics m = new MyMetrics(this);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}*/
 		
 		if(getConfig().getBoolean("config.auto_updating")){
         	Updater updater = new Updater(this, 66523, this.getFile(), Updater.UpdateType.DEFAULT, false);
         }
 		
 		skin_updating = getConfig().getBoolean("config.auto_skin_updating");
 		interval = getConfig().getInt("config.auto_skin_updating_interval_minutes");
 		
 		if(skin_updating){
 			update_skins();
 		}
 	}
 	
 	private WorldGuardPlugin getWorldGuard() {
 	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
 
 	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
 	        return null;
 	    }
 
 	    return (WorldGuardPlugin) plugin;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("mobbuild")){
 			if(sender.hasPermission("mobs.build")){
 				if(args.length > 0){
 					String action = args[0];
 					if(action.equalsIgnoreCase("chicken")){
 						Player p = null;
 						try{
 							p = (Player)sender;	
 						}catch(Exception e){
 							sender.sendMessage("4Please execute this command ingame.");
 						}
 						
						sender.sendMessage("3Building a 6chicken 3now.");
 						Chicken.buildChicken(p.getLocation(), "west");

 						//TODO: add more mobs
 					}else if(action.equalsIgnoreCase("cow")){
 						
 					}else if(action.equalsIgnoreCase("sheep")){
 						
 					}else if(action.equalsIgnoreCase("pig")){
 						
 					}else if(action.equalsIgnoreCase("ocelot")){
 						
 					}else if(action.equalsIgnoreCase("villager")){
 						
 					}else if(action.equalsIgnoreCase("mooshroom")){
 						
 					}
 				}else{
 					sender.sendMessage("3Possible Mobs: 6Chicken");
 				}
 			}
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("skin")){
 			if(sender.hasPermission("skins.build")){
 				if(args.length > 0){
 					String action = args[0];
 					if(action.equalsIgnoreCase("undo")){ // /skin undo
 						Player p = null;
 						try{
 							p = (Player)sender;	
 						}catch(Exception e){
 							sender.sendMessage("4Please execute this command ingame.");
 						}
 						
 						if(p != null){
 							if(undoloc.containsKey(p)){
 								//Location t = undoloc.get(p);
 								//String direction = undodir.get(p);
 								ArrayList<Location> t_ = undoloc.get(p);
 								Location t = null;
 								ArrayList<String> dir_ = undodir.get(p);
 								String direction = "";
 								if(!t_.isEmpty() && !dir_.isEmpty()){
 									t = t_.get(t_.size() - 1);
 									direction = dir_.get(dir_.size() - 1);
 									undo(p, t, direction);
 								}
 							}else{
 								p.sendMessage("4I don't have any skins you requested in memory!");
 							}
 						}
 						
 					}else if(action.equalsIgnoreCase("smooth")){ // /skin smooth
 						Player p = null;
 						try{
 							p = (Player)sender;
 						}catch(Exception e){
 							sender.sendMessage("4Please execute this command ingame.");
 						}
 						
 						if(p != null){
 							if(undoloc.containsKey(p)){
 								//Location t = undoloc.get(p);
 								//String skin = undoskin.get(p);
 								//String dir = undodir.get(p);
 								boolean cont = true;
 								
 								ArrayList<Location> t_ = undoloc.get(p);
 								Location t = null;
 								ArrayList<String> dir_ = undodir.get(p);
 								String dir = "";
 								ArrayList<String> skin_ = undoskin.get(p);
 								String skin = "";
 								if(!t_.isEmpty() && !dir_.isEmpty() && !skin_.isEmpty()){
 									t = t_.get(t_.size() - 1);
 									dir = dir_.get(dir_.size() - 1);
 									skin = skin_.get(skin_.size() - 1);
 									
 								}else{
 									cont = false;
 								}
 								
 								BufferedImage Image1 = null;
 								try {
 									URL url; //= new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 									if(skin.equalsIgnoreCase("steve")){
 								    	url = new URL("https://minecraft.net/images/char.png");
 								    }else{
 								    	url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + skin + ".png");
 								    }
 									Image1 = ImageIO.read(url);
 								} catch (IOException e) {
 									cont = false;
 								}
 
 								if(cont){
 									smooth(t, Image1, dir);
 								}else{
 									p.sendMessage("4Playername not found!");
 								}
 								
 							}
 						}
 					}else if(action.equalsIgnoreCase("forceupdate")){
 						if(skin_updating){
 							sender.sendMessage(ChatColor.DARK_AQUA + "Forcing Skins update..");
 							Bukkit.getScheduler().runTaskLater(this, new Runnable(){
 			            		@Override
 			            		public void run(){
 			            			update_func();	
 			            		}
 			            	}, 10L);	
 						}else{
 							sender.sendMessage(ChatColor.RED + "You need to toggle skin_auto_updating to true in the config first.");
 						}
 					}else{						
 						if(args.length > 1){ // /skin [name] [direction]
 							if(args[1].equalsIgnoreCase("clay")){ // /skin [name] clay
 								Player p = null;
 								try{
 									p = (Player)sender;	
 								}catch(Exception e){
 									sender.sendMessage("4Please execute this command ingame.");
 									return true;
 								}
 								
 								String name = args[0];
 								sender.sendMessage("3Please don't move for 3 seconds while the skin is being built.");
 								BufferedImage Image1 = null;
 								boolean cont = true;
 								try {
 									URL url; //= new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 									if(name.equalsIgnoreCase("steve")){
 								    	url = new URL("https://minecraft.net/images/char.png");
 								    }else{
 								    	url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 								    }
 								    
 								    Image1 = ImageIO.read(url);
 								} catch (IOException e) {
 									cont = false;
 								}
 								
 								if(cont){
 									String look_direction = getDirection(p.getLocation().getYaw());
 									if(look_direction != null){
 										if(canBuild(p, look_direction)){
 											buildclay(p, Image1, args[0], look_direction); // builds in direction player is facing
 										}
 									}else{
 										if(canBuild(p, "east")){
 											buildclay(p, Image1, args[0], "east");
 										}
 									}
 								}else{
 									p.sendMessage("4Playername not found!");
 								}
 							}else if(args[1].equalsIgnoreCase("glass")){ // /skin [name] glass
 								Player p = null;
 								try{
 									p = (Player)sender;	
 								}catch(Exception e){
 									sender.sendMessage("4Please execute this command ingame.");
 									return true;
 								}
 								
 								String name = args[0];
 								sender.sendMessage("3Please don't move for 3 seconds while the skin is being built.");
 								BufferedImage Image1 = null;
 								boolean cont = true;
 								try {
 									URL url; //= new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 									if(name.equalsIgnoreCase("steve")){
 								    	url = new URL("https://minecraft.net/images/char.png");
 								    }else{
 								    	url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 								    }
 								    
 								    Image1 = ImageIO.read(url);
 								} catch (IOException e) {
 									cont = false;
 								}
 								
 								if(cont){
 									String look_direction = getDirection(p.getLocation().getYaw());
 									if(look_direction != null){
 										if(canBuild(p, look_direction)){
 											buildglass(p, Image1, args[0], look_direction); // builds in direction player is facing
 										}
 									}else{
 										if(canBuild(p, "east")){
 											buildglass(p, Image1, args[0], "east");
 										}
 									}
 								}else{
 									p.sendMessage("4Playername not found!");
 								}
 							}else if(args[1].equalsIgnoreCase("all")){ // /skin [name] all
 								Player p = null;
 								try{
 									p = (Player)sender;	
 								}catch(Exception e){
 									sender.sendMessage("4Please execute this command ingame.");
 									return true;
 								}
 								
 								String name = args[0];
 								sender.sendMessage("3Please don't move for 3 seconds while the skin is being built.");
 								BufferedImage Image1 = null;
 								boolean cont = true;
 								try {
 									URL url; //= new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 									if(name.equalsIgnoreCase("steve")){
 								    	url = new URL("https://minecraft.net/images/char.png");
 								    }else{
 								    	url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 								    }
 								    
 								    Image1 = ImageIO.read(url);
 								} catch (IOException e) {
 									cont = false;
 								}
 								
 								if(cont){
 									String look_direction = getDirection(p.getLocation().getYaw());
 									if(look_direction != null){
 										if(canBuild(p, look_direction)){
 											buildall(p, Image1, args[0], look_direction); // builds in direction player is facing
 										}
 									}else{
 										if(canBuild(p, "east")){
 											buildall(p, Image1, args[0], "east");
 										}
 									}
 								}else{
 									p.sendMessage("4Playername not found!");
 								}
 							}else{  // /skin [name] [direction]
 								if(args.length > 2){
 									if(args[2].equalsIgnoreCase("clay")){ // /skin [name] [direction] clay
 										Player p = null;
 										try{
 											p = (Player)sender;	
 										}catch(Exception e){
 											sender.sendMessage("4Please execute this command ingame.");
 											return true;
 										}
 										
 										String direction = args[1];
 										String name = args[0];
 										sender.sendMessage("3Please don't move for4 3 3seconds while the skin is being built.");
 										BufferedImage Image1 = null;
 										boolean cont = true;
 										try {
 											URL url; //= new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 											if(name.equalsIgnoreCase("steve")){
 										    	url = new URL("https://minecraft.net/images/char.png");
 										    }else{
 										    	url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 										    }
 											
 										    Image1 = ImageIO.read(url);
 										} catch (IOException e) {
 											cont = false;
 										}
 										
 										if(cont){
 											List<String> places = Arrays.asList("east", "west", "south", "north", "e", "w", "s", "n");
 											if(places.contains(direction)){
 												if(canBuild(p, direction)){
 													buildclay(p, Image1, args[0], direction);
 												}
 											}else{
 												sender.sendMessage("2Usage: /skins [name] [direction: east, west, north, south]. 3Example: /skin InstanceLabs south");
 											}
 										}else{
 											p.sendMessage("4Playername not found!");
 										}
 									}else if(args[2].equalsIgnoreCase("glass")){ // /skin [name] [direction] glass
 										Player p = null;
 										try{
 											p = (Player)sender;	
 										}catch(Exception e){
 											sender.sendMessage("4Please execute this command ingame.");
 											return true;
 										}
 										
 										String direction = args[1];
 										String name = args[0];
 										sender.sendMessage("3Please don't move for4 3 3seconds while the skin is being built.");
 										BufferedImage Image1 = null;
 										boolean cont = true;
 										try {
 											URL url; //= new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 											if(name.equalsIgnoreCase("steve")){
 										    	url = new URL("https://minecraft.net/images/char.png");
 										    }else{
 										    	url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 										    }
 											
 										    Image1 = ImageIO.read(url);
 										} catch (IOException e) {
 											cont = false;
 										}
 										
 										if(cont){
 											List<String> places = Arrays.asList("east", "west", "south", "north", "e", "w", "s", "n");
 											if(places.contains(direction)){
 												buildglass(p, Image1, args[0], direction);
 											}else{
 												sender.sendMessage("2Usage: /skins [name] [direction: east, west, north, south]. 3Example: /skin InstanceLabs south");
 											}
 										}else{
 											p.sendMessage("4Playername not found!");
 										}
 									}
 								}else{ // /skin [name] [direction]
 									Player p = null;
 									try{
 										p = (Player)sender;	
 									}catch(Exception e){
 										sender.sendMessage("4Please execute this command ingame.");
 										return true;
 									}
 									
 									String direction = args[1];
 									String name = args[0];
 									sender.sendMessage("3Please don't move for4 3 3seconds while the skin is being built.");
 									BufferedImage Image1 = null;
 									boolean cont = true;
 									try {
 										URL url; //= new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 										if(name.equalsIgnoreCase("steve")){
 									    	url = new URL("https://minecraft.net/images/char.png");
 									    }else{
 									    	url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 									    }
 										
 									    Image1 = ImageIO.read(url);
 									} catch (IOException e) {
 										cont = false;
 									}
 									
 									if(cont){
 										List<String> places = Arrays.asList("east", "west", "south", "north", "e", "w", "s", "n");
 										if(places.contains(direction)){
 											if(canBuild(p, direction)){
 												build(p, Image1, args[0], direction);
 											}
 										}else{
 											sender.sendMessage("2Usage: /skins [name] [direction: east, west, north, south]. 3Example: /skin InstanceLabs south");
 										}
 									}else{
 										p.sendMessage("4Playername not found!");
 									}
 								}
 								
 							}
 							
 						}else{ // /skin [name]
 							Player p = null;
 							try{
 								p = (Player)sender;	
 							}catch(Exception e){
 								sender.sendMessage("4Please execute this command ingame.");
 								return true;
 							}
 							
 							String name = args[0];
 							sender.sendMessage("3Please don't move for 3 seconds while the skin is being built.");
 							BufferedImage Image1 = null;
 							boolean cont = true;
 							try {
 								URL url; //= new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 								if(name.equalsIgnoreCase("steve")){
 							    	url = new URL("https://minecraft.net/images/char.png");
 							    }else{
 							    	url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 							    }
 
 							    Image1 = ImageIO.read(url);
 							} catch (IOException e) {
 								cont = false;
 							}
 							
 							if(cont){
 								String look_direction = getDirection(p.getLocation().getYaw());
 								if(look_direction != null){
 									if(canBuild(p, look_direction)){
 										build(p, Image1, args[0], look_direction); // builds in direction player is facing	
 									}
 								}else{
 									if(canBuild(p, "east")){
 										build(p, Image1, args[0], "east");
 									}
 								}
 							}else{
 								p.sendMessage("4Playername not found!");
 							}
 						}
 					}
 				}else{
 					sender.sendMessage("3 -- Skins Help --");
 					sender.sendMessage("3 /skin [name] : 2Builds a skin in the direction you are looking");
 					sender.sendMessage("3 /skin [name] [direction] : 2Builds a skin in the provided direction");
 					sender.sendMessage("3 /skin smooth : 2Smoothes the skin with wood blocks");
 					sender.sendMessage("3 /skin undo : 2Undoes the last skin");
 					sender.sendMessage("3 /colortest [start/status] : 2Runs a colortest to determine all currently supported colors");
 				}
 			}else{
 				sender.sendMessage("4You don't have permission.");
 			}
 			
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("colortest")){ // /colortest
 			if(sender.hasPermission("colortest.colortest")){
 				if(args.length > 0){
 					if(args[0].equalsIgnoreCase("start")){ // /colortest start
 						poscount = 0;
 						negcount = 0;
 						Runnable r = new Runnable() {
 					        public void run() {
 					        	colorTest();
 					        }
 					    };
 					    new Thread(r).start();
 					}else if(args[0].equalsIgnoreCase("startwithlog")){ // /colortest startwithlog
 						poscount = 0;
 						negcount = 0;
 						Runnable r = new Runnable(){
 					        public void run() {
 					        	colorTestLog();
 					        }
 					    };
 					    new Thread(r).start();
 					}else if(args[0].equalsIgnoreCase("status")){ // /colortest status
 						sender.sendMessage("2Pos count: " + Integer.toString(poscount));
 						sender.sendMessage("4Neg count: " + Integer.toString(negcount));
 					}else if(args[0].equalsIgnoreCase("punchcard")){ // /colortest punchcard
 						sender.sendMessage("3Building Punchcard . . .");
 						Runnable r = new Runnable() {
 					        public void run() {
 					        	createPunchcard();
 					        }
 					    };
 					    new Thread(r).start();
 					}
 				}else{
 					sender.sendMessage("3/colortest start");
 					sender.sendMessage("3/colortest status");
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	
 	public boolean update_skins() {
         if (task != null) {
             return true;
         }
 
         final Plugin main = this;
         
         task = getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable(){
             public void run() {
                 // update skins
             	Bukkit.getScheduler().runTaskLater(main, new Runnable(){
             		
             		@Override
             		public void run(){
             			update_func();	
             		}
             	
             	}, 10L);
             	
             }
         }, 20, interval * 1200);
 
         return true;
 	}
 	
 	public void update_func(){
 		try{
 			getLogger().info("Updating Skins . . .");
 			if(getConfig().isSet("skins")){
 				for(String uuid : getConfig().getConfigurationSection("skins.").getKeys(false)){
 					if(isValidSkin(uuid)){
 						String skin_ = getConfig().getString("skins." + uuid + ".name");
 						getLogger().info("Updating " + skin_);
 						boolean cont = true;
 						String direction = getConfig().getString("skins." + uuid + ".direction");
 						String mode = getConfig().getString("skins." + uuid + ".mode");
 						Location t = new Location(Bukkit.getWorld(getConfig().getString("skins." + uuid + ".location.world")), getConfig().getInt("skins." + uuid + ".location.x"), getConfig().getInt("skins." + uuid + ".location.y"), getConfig().getInt("skins." + uuid + ".location.z"));
 						BufferedImage Image1 = null;
 						BufferedImage local = null;
 						try {
 						    URL url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + skin_ + ".png");
 						    Image1 = ImageIO.read(url);
 						} catch (IOException e) {
 							cont = false;
 						}
 						
 						try {
 							local = ImageIO.read(new File(this.getDataFolder().getPath() + "/" + uuid + ".png"));
 							//local = ImageIO.read(new File(this.getDataFolder().getPath() + "/" + skin_ + ".png"));
 						} catch (IOException e) {
 							cont = false;
 						}
 						
 						if(local != null){
 							if(!bufferedImagesEqual(local, Image1)){
 								update(t, Image1, skin_, direction, mode);
 							}
 						}else{
 							getLogger().info("An error occured while updating " + skin_ + ": Skin image in config folder was not found. Downloading latest skin.");
 							File outputfile = new File(this.getDataFolder().getPath() + "/" + uuid + ".png");
 							//File outputfile = new File(this.getDataFolder().getPath() + "/" + skin + ".png");
 							try {
 								ImageIO.write(Image1, "png", outputfile);
 							} catch (IOException e) {
 								//
 							}
 							update(t, Image1, skin_, direction, mode);
 						}
 					}
 				}
 			}
 			
 			getLogger().info("Finished updating skins.");
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	public boolean isValidSkin(String skin){
 		if(getConfig().isSet("skins." + skin + ".name") && getConfig().isSet("skins." + skin + ".location") && getConfig().isSet("skins." + skin + ".direction")){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	private boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
 		if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
 			for (int x = 0; x < img1.getWidth(); x++) {
 				for (int y = 0; y < img1.getHeight(); y++) {
 					if (img1.getRGB(x, y) != img2.getRGB(x, y)) return false;
 				}
 			}
 		} else {
 			return false;
 		}
 		return true;
 	}
 		  
 	
 	
 	public boolean isDuplicateLocation(String skin1, Location l_skin2){
 		if(getConfig().getInt("skins." + skin1 + ".location.x") == l_skin2.getBlockX() && getConfig().getInt("skins." + skin1 + ".location.y") == l_skin2.getBlockY() && getConfig().getInt("skins." + skin1 + ".location.z") == l_skin2.getBlockZ() && getConfig().getString("skins." + skin1 + ".location.world").equals(l_skin2.getWorld().getName())){
 			return true;
 		}
 		return false;
 	}
 	
 	
 	public void saveSkin(Location t, String skin, String uuid_, String direction, String mode){
 		// check if there are any skins registered on that location
 		// remove if true
 		if(getConfig().isSet("skins")){
 			for(String uuid : getConfig().getConfigurationSection("skins.").getKeys(false)){
 				if(isValidSkin(uuid)){
 					String skin_ = getConfig().getString("skins." + uuid + ".name");
 					// remove skin, if duplicate location
 					if(isDuplicateLocation(uuid, t)){
 						removeSkin(skin_, uuid);
 					}
 				}
 			}
 		}
 		
 		
 		
 		getConfig().set("skins." + uuid_ + ".name", skin);
 		getConfig().set("skins." + uuid_ + ".location.x", t.getBlockX());
 		getConfig().set("skins." + uuid_ + ".location.y", t.getBlockY());
 		getConfig().set("skins." + uuid_ + ".location.z", t.getBlockZ());
 		getConfig().set("skins." + uuid_ + ".location.world", t.getWorld().getName());
 		getConfig().set("skins." + uuid_ + ".direction", direction);
 		getConfig().set("skins." + uuid_ + ".mode", mode);
 		this.saveConfig();
 
 		
 		
 		boolean cont = true;
 		BufferedImage Image1 = null;
 		try {
 			URL url; //= new URL("http://s3.amazonaws.com/MinecraftSkins/" + args[0] + ".png");
 			if(skin.equalsIgnoreCase("steve")){
 		    	url = new URL("https://minecraft.net/images/char.png");
 		    }else{
 		    	url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + skin + ".png");
 		    }
 			
 		    Image1 = ImageIO.read(url);
 		} catch (IOException e) {
 			cont = false;
 		}
 		
 		File outputfile = new File(this.getDataFolder().getPath() + "/" + uuid_ + ".png");
 		//File outputfile = new File(this.getDataFolder().getPath() + "/" + skin + ".png");
 		try {
 			ImageIO.write(Image1, "png", outputfile);
 		} catch (IOException e) {
 			//
 		}
 	}
 	
 	
 	public void removeSkin(String skin, String uuid){
 		try{
 			getConfig().set("skins." + uuid, null);	
 		}catch(Exception e){
 			//
 		}
 		
 		this.saveConfig();
 		
 		try{
 			File outputfile = new File(this.getDataFolder().getPath() + "/" + uuid + ".png");
 			//File outputfile = new File(this.getDataFolder().getPath() + "/" + skin + ".png");
 			outputfile.delete();	
 		}catch(Exception e){
 			//
 		}
 	}
 	
 	
 	
 	private void smooth(Location t, BufferedImage Image2, String direction){
 		
 		Location c = t;
 		//this function only builds skin blocks (wood instead of orange wool)
 		//needs the location of a skin
 		
 		if(direction.equalsIgnoreCase("east")){
 			// leg1
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 0, 4, 20, 32, "leg1_left");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 4, 8, 20, 32, "leg1_front");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 12, 16, 20, 32, "leg1_behind");
 			// leg2
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 0, 4, 20, 32, "leg2_left");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 4, 8, 20, 32, "leg2_front");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 0, 4, 20, 32, "leg2_right");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 12, 16, 20, 32, "leg2_behind");
 			// body
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 16, 20, 20, 32, "body_left");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 20, 28, 20, 32, "body_front");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 28, 32, 20, 32, "body_right");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 32, 40, 20, 32, "body_behind");
 			// arm1
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 44, 48, 16, 20, "arm1_top");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 40, 44, 20, 32, "arm1_left");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 44, 48, 20, 32, "arm1_front");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 48, 52, 20, 32, "arm1_right");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 52, 56, 20, 32, "arm1_behind");
 			// arm2
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 44, 48, 16, 20, "arm2_top");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 40, 44, 20, 32, "arm2_left");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 44, 48, 20, 32, "arm2_front");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 48, 52, 20, 32, "arm2_right");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 52, 56, 20, 32, "arm2_behind");
 			// head
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 0, 8, 8, 16, "head_left");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 16, 24, 8, 16, "head_right");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 24, 32, 8, 16, "head_behind");
 			//SkinSmooth.smoothPartOfImageEast(this, t, Image2, 8, 16, 0, 8, "head_top");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 16, 24, 0, 8, "head_bottom");
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 8, 16, 8, 16, "head_front");
 			
 			SkinSmooth.smoothPartOfImageEast(this, t, Image2, 40, 48, 8, 16, "hat_front");
 		}else if(direction.equalsIgnoreCase("west")){
 			// leg1_left
 			SkinSmooth.smoothWestSide(this, c.getWorld(), Image2, 0, 4, 20, 32, new Location(c.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinSmooth.smoothWestFront(this, c.getWorld(), Image2, 4, 8, 20, 32, new Location(c.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinSmooth.smoothWestFrontInvert(this, c.getWorld(), Image2, 12, 16, 20, 32, new Location(c.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2
 			// leg2_right
 			SkinSmooth.smoothWestSide(this, c.getWorld(), Image2, 0, 4, 20, 32, new Location(c.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 7));
 			// leg2_ front
 			SkinSmooth.smoothWestFrontInvert(this, c.getWorld(), Image2, 4, 8, 20, 32, new Location(c.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ() - 3));
 			// leg2_behind
 			SkinSmooth.smoothWestFront(this, c.getWorld(), Image2, 12, 16, 20, 32, new Location(c.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() - 4));
 			// body
 			// body_front
 			SkinSmooth.smoothWestFront(this, c.getWorld(), Image2, 20, 28, 20, 32, new Location(c.getWorld(), c.getBlockX() -+ 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinSmooth.smoothWestFront(this, c.getWorld(), Image2, 32, 40, 20, 32, new Location(c.getWorld(), c.getBlockX() -+ 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinSmooth.smoothPartOfImageWest(this, t, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinSmooth.smoothPartOfImageWest(this, t, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinSmooth.smoothWestSide(this, c.getWorld(), Image2, 40, 44, 20, 32, new Location(c.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_front
 			SkinSmooth.smoothWestFront(this, c.getWorld(), Image2, 44, 48, 20, 32, new Location(c.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_right
 			SkinSmooth.smoothWestSide(this, c.getWorld(), Image2, 48, 52, 20, 32, new Location(c.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinSmooth.smoothWestFront(this, c.getWorld(), Image2, 52, 56, 20, 32, new Location(c.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm2
 			SkinSmooth.smoothPartOfImageWest(this, t, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinSmooth.smoothPartOfImageWest(this, t, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinSmooth.smoothWestSide(this, c.getWorld(), Image2, 40, 44, 20, 32, new Location(c.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 11));
 			// arm2_front
 			SkinSmooth.smoothWestFrontInvert(this, c.getWorld(), Image2, 44, 48, 20, 32, new Location(c.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// arm2_right
 			SkinSmooth.smoothWestSide(this, c.getWorld(), Image2, 48, 52, 20, 32, new Location(c.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 8));
 			// arm2_behind
 			SkinSmooth.smoothWestFrontInvert(this, c.getWorld(), Image2, 52, 56, 20, 32, new Location(c.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// head
 			// head_left
 			SkinSmooth.smoothWestSide(this, c.getWorld(), Image2, 0, 8, 8, 16, new Location(c.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinSmooth.smoothWestSideInvert(this, c.getWorld(), Image2, 16, 24, 8, 16, new Location(c.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_behind
 			SkinSmooth.smoothWestFront(this, c.getWorld(), Image2, 24, 32, 8, 16, new Location(c.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ()));
 			// head_top and head_bottom
 			SkinSmooth.smoothPartOfImageWest(this, t, Image2, 8, 16, 0, 8, "head_top");
 			SkinSmooth.smoothPartOfImageWest(this, t, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinSmooth.smoothWestFront(this, c.getWorld(), Image2, 8, 16, 8, 16, new Location(c.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 	
 			// hat_front
 			SkinSmooth.smoothWestFrontHAT(this, t, Image2, 40, 48, 8, 16, new Location(t.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 		}else if(direction.equalsIgnoreCase("south")){
 			// leg1_left
 			SkinSmooth.smoothSouthSide(this, t.getWorld(), Image2, 0, 4, 20, 32, new Location(t.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 1));
 			// leg1_front
 			SkinSmooth.smoothSouthFront(this, t.getWorld(), Image2, 4, 8, 20, 32, new Location(t.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 2));
 			// leg1_ behind
 			SkinSmooth.smoothSouthFrontInvert(this, t.getWorld(), Image2, 12, 16, 20, 32, new Location(t.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 5));
 			// leg2
 			// leg2_right
 			SkinSmooth.smoothSouthSide(this, t.getWorld(), Image2, 0, 4, 20, 32, new Location(t.getWorld(), c.getBlockX() - 7, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2_ front
 			SkinSmooth.smoothSouthFrontInvert(this, t.getWorld(), Image2, 4, 8, 20, 32, new Location(t.getWorld(), c.getBlockX() - 3, c.getBlockY(), c.getBlockZ() + 2));
 			// leg2_behind
 			SkinSmooth.smoothSouthFront(this, t.getWorld(), Image2, 12, 16, 20, 32, new Location(t.getWorld(), c.getBlockX() - 4, c.getBlockY(), c.getBlockZ() + 5));
 			// body
 			// body_front
 			SkinSmooth.smoothSouthFront(this, t.getWorld(), Image2, 20, 28, 20, 32, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 2));
 			// body_behind
 			SkinSmooth.smoothSouthFront(this, t.getWorld(), Image2, 32, 40, 20, 32, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm1
 			SkinSmooth.smoothPartOfImageSouth(this, t, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinSmooth.smoothPartOfImageSouth(this, t, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinSmooth.smoothSouthSide(this, t.getWorld(), Image2, 40, 44, 20, 32, new Location(t.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_front
 			SkinSmooth.smoothSouthFront(this, t.getWorld(), Image2, 44, 48, 20, 32, new Location(t.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm1_right
 			SkinSmooth.smoothSouthSide(this, t.getWorld(), Image2, 48, 52, 20, 32, new Location(t.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinSmooth.smoothSouthFront(this, t.getWorld(), Image2, 52, 56, 20, 32, new Location(t.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm2
 			SkinSmooth.smoothPartOfImageSouth(this, t, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinSmooth.smoothPartOfImageSouth(this, t, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinSmooth.smoothSouthSide(this, t.getWorld(), Image2, 40, 44, 20, 32, new Location(t.getWorld(), c.getBlockX() - 11, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_front
 			SkinSmooth.smoothSouthFrontInvert(this, t.getWorld(), Image2, 44, 48, 20, 32, new Location(t.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm2_right
 			SkinSmooth.smoothSouthSide(this, t.getWorld(), Image2, 48, 52, 20, 32, new Location(t.getWorld(), c.getBlockX() - 8, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_behind
 			SkinSmooth.smoothSouthFrontInvert(this, t.getWorld(), Image2, 52, 56, 20, 32, new Location(t.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// head
 			// head_left
 			SkinSmooth.smoothSouthSide(this, t.getWorld(), Image2, 0, 8, 8, 16, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 			// head_right
 			SkinSmooth.smoothSouthSideInvert(this, t.getWorld(), Image2, 16, 24, 8, 16, new Location(t.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ() + 8));
 			// head_behind
 			SkinSmooth.smoothSouthFront(this, t.getWorld(), Image2, 24, 32, 8, 16, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 7));
 			SkinSmooth.smoothPartOfImageSouth(this, t, Image2, 8, 16, 0, 8, "head_top");
 			SkinSmooth.smoothPartOfImageSouth(this, t, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinSmooth.smoothSouthFront(this, t.getWorld(), Image2, 8, 16, 8, 16, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 		
 			// hat_front
 			SkinSmooth.smoothSouthFrontHAT(this, t, Image2, 40, 48, 8, 16, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 		}else if(direction.equalsIgnoreCase("north")){
 			// leg1_left
 			SkinSmooth.smoothNorthSide(this, t.getWorld(), Image2, 0, 4, 20, 32, new Location(t.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 1));
 			// leg1_front
 			SkinSmooth.smoothNorthFront(this, t.getWorld(), Image2, 4, 8, 20, 32, new Location(t.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 2));
 			// leg1_behind
 			SkinSmooth.smoothNorthFrontInvert(this, t.getWorld(), Image2, 12, 16, 20, 32, new Location(t.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 5));
 			// leg2
 			// leg2_right
 			SkinSmooth.smoothNorthSide(this, t.getWorld(), Image2, 0, 4, 20, 32, new Location(t.getWorld(), c.getBlockX() + 7, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2_ front
 			SkinSmooth.smoothNorthFrontInvert(this, t.getWorld(), Image2, 4, 8, 20, 32, new Location(t.getWorld(), c.getBlockX() + 3, c.getBlockY(), c.getBlockZ() - 2));
 			// leg2_behind
 			SkinSmooth.smoothNorthFront(this, t.getWorld(), Image2, 12, 16, 20, 32, new Location(t.getWorld(), c.getBlockX() + 4, c.getBlockY(), c.getBlockZ() - 5));
 			// body
 			// body_front
 			SkinSmooth.smoothNorthFront(this, t.getWorld(), Image2, 20, 28, 20, 32, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 2));
 			// body_behind
 			SkinSmooth.smoothNorthFront(this, t.getWorld(), Image2, 32, 40, 20, 32, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm1
 			// arm1_bottom and arm1_top
 			SkinSmooth.smoothPartOfImageNorth(this, t, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinSmooth.smoothPartOfImageNorth(this, t, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinSmooth.smoothNorthSide(this, t.getWorld(), Image2, 40, 44, 20, 32, new Location(t.getWorld(), c.getBlockX() - 4, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_front
 			SkinSmooth.smoothNorthFront(this, t.getWorld(), Image2, 44, 48, 20, 32, new Location(t.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm1_right
 			SkinSmooth.smoothNorthSide(this, t.getWorld(), Image2, 48, 52, 20, 32, new Location(t.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinSmooth.smoothNorthFront(this, t.getWorld(), Image2, 52, 56, 20, 32, new Location(t.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm2
 			// arm2_bottom and arm2_top
 			SkinSmooth.smoothPartOfImageNorth(this, t, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinSmooth.smoothPartOfImageNorth(this, t, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinSmooth.smoothNorthSide(this, t.getWorld(), Image2, 40, 44, 20, 32, new Location(t.getWorld(), c.getBlockX() + 11, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_front
 			SkinSmooth.smoothNorthFrontInvert(this, t.getWorld(), Image2, 44, 48, 20, 32, new Location(t.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm2_right
 			SkinSmooth.smoothNorthSide(this, t.getWorld(), Image2, 48, 52, 20, 32, new Location(t.getWorld(), c.getBlockX() + 8, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_behind
 			SkinSmooth.smoothNorthFrontInvert(this, t.getWorld(), Image2, 52, 56, 20, 32, new Location(t.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// head
 			// head_left
 			SkinSmooth.smoothNorthSide(this, t.getWorld(), Image2, 0, 8, 8, 16, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 			// head_right
 			SkinSmooth.smoothNorthSideInvert(this, t.getWorld(), Image2, 16, 24, 8, 16, new Location(t.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ() - 8)); 
 			// head_behind
 			SkinSmooth.smoothNorthFront(this, t.getWorld(), Image2, 24, 32, 8, 16, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_top
 			SkinSmooth.smoothPartOfImageNorth(this, t, Image2, 8, 16, 0, 8, "head_top");
 			// head_front
 			SkinSmooth.smoothNorthFront(this, t.getWorld(), Image2, 8, 16, 8, 16, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			
 			// hat_front
 			SkinSmooth.smoothNorthFrontHAT(this, t, Image2, 40, 48, 8, 16, new Location(t.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 		}
 	}
 	
 	
 	
 	
 	
 	
 	
 	private void undo(Player p, Location t, String direction){
 		if(skin_updating){
 			removeSkin(undoskin.get(p).get(undoskin.get(p).size() - 1), undo_uuid.get(p).get(undo_uuid.get(p).size() - 1));	
 		}
 		
 		ArrayList<Location> loc_ = new ArrayList<Location>();
 		ArrayList<String> skin_ = new ArrayList<String>();
 		ArrayList<String> dir_ = new ArrayList<String>();
 		ArrayList<String> uuid_ = new ArrayList<String>();
 		if(undoloc.containsKey(p)){
 			loc_ = undoloc.get(p);
 			skin_ = undoskin.get(p);
 			dir_ = undodir.get(p);
 			uuid_ = undo_uuid.get(p);
 			
 			if(loc_.size() < 2){
 				undoloc.remove(p);
 				undoskin.remove(p);
 				undodir.remove(p);
 				undo_uuid.remove(p);
 			}else{
 				loc_.remove(loc_.size() - 1);
 				skin_.remove(skin_.size() - 1);
 				dir_.remove(dir_.size() - 1);
 				uuid_.remove(uuid_.size() - 1);
 				
 				undoloc.put(p, loc_);
 				undoskin.put(p, skin_);
 				undodir.put(p, dir_);
 				undo_uuid.put(p, uuid_);
 			}
 		}
 		
 		//undoloc.remove(p);
 		//undoskin.remove(p);
 		//undodir.remove(p);
 		//undo_uuid.remove(p);
 		
 		Location c = t;
 		
 		if(direction.equalsIgnoreCase("east")){
 			// leg1
 			SkinUndo.undoPartOfImageEast(t,  0, 4, 20, 32, "leg1_left");
 			SkinUndo.undoPartOfImageEast(t, 4, 8, 20, 32, "leg1_front");
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 8, 12, 20, 32, "leg1_right"); // no need, is IN the statue, not seen from outside
 			SkinUndo.undoPartOfImageEast(t, 12, 16, 20, 32, "leg1_behind");
 			// leg2
 			SkinUndo.undoPartOfImageEast(t, 0, 4, 20, 32, "leg2_left");
 			SkinUndo.undoPartOfImageEast(t, 4, 8, 20, 32, "leg2_front");
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 8, 12, 20, 32, "leg2_right");
 			SkinUndo.undoPartOfImageEast(t,  0, 4, 20, 32, "leg2_right");
 			SkinUndo.undoPartOfImageEast(t,  12, 16, 20, 32, "leg2_behind");
 			// body
 			SkinUndo.undoPartOfImageEast(t, 16, 20, 20, 32, "body_left");
 			SkinUndo.undoPartOfImageEast(t, 20, 28, 20, 32, "body_front");
 			SkinUndo.undoPartOfImageEast(t, 28, 32, 20, 32, "body_right");
 			SkinUndo.undoPartOfImageEast(t, 32, 40, 20, 32, "body_behind");
 			// arm1
 			SkinUndo.undoPartOfImageEast(t, 48, 52, 16, 20, "arm1_bottom");
 			SkinUndo.undoPartOfImageEast(t, 44, 48, 16, 20, "arm1_top");
 			SkinUndo.undoPartOfImageEast(t, 40, 44, 20, 32, "arm1_left");
 			SkinUndo.undoPartOfImageEast(t, 44, 48, 20, 32, "arm1_front");
 			SkinUndo.undoPartOfImageEast(t, 48, 52, 20, 32, "arm1_right");
 			SkinUndo.undoPartOfImageEast(t, 52, 56, 20, 32, "arm1_behind");
 			// arm2
 			SkinUndo.undoPartOfImageEast(t, 48, 52, 16, 20, "arm2_bottom");
 			SkinUndo.undoPartOfImageEast(t, 44, 48, 16, 20, "arm2_top");
 			SkinUndo.undoPartOfImageEast(t, 40, 44, 20, 32, "arm2_left");
 			SkinUndo.undoPartOfImageEast(t, 44, 48, 20, 32, "arm2_front");
 			SkinUndo.undoPartOfImageEast(t, 48, 52, 20, 32, "arm2_right");
 			SkinUndo.undoPartOfImageEast(t, 52, 56, 20, 32, "arm2_behind");
 			// head
 			SkinUndo.undoPartOfImageEast(t, 0, 8, 8, 16, "head_left");
 			SkinUndo.undoPartOfImageEast(t, 8, 16, 8, 16, "head_front");
 			SkinUndo.undoPartOfImageEast(t, 16, 24, 8, 16, "head_right");
 			SkinUndo.undoPartOfImageEast(t, 24, 32, 8, 16, "head_behind");
 			SkinUndo.undoPartOfImageEast(t, 8, 16, 0, 8, "head_top");
 			SkinUndo.undoPartOfImageEast(t, 16, 24, 0, 8, "head_bottom");
 			// hat layers
 			SkinUndo.undoPartOfImageEast(t, 32, 40, 8, 16, "hat_left");
 			SkinUndo.undoPartOfImageEast(t, 40, 48, 8, 16, "hat_front");
 			SkinUndo.undoPartOfImageEast(t, 48, 56, 8, 16, "hat_right");
 			SkinUndo.undoPartOfImageEast(t, 56, 64, 8, 16, "hat_behind");
 			SkinUndo.undoPartOfImageEast(t, 40, 48, 0, 8, "hat_top");
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 56, 0, 8, "hat_bottom");	// this looks like crap
 		}else if(direction.equalsIgnoreCase("west")){
 			/*// leg1_left
 			SkinUndo.undoWestSide(p, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinUndo.undoEastFront(p, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinUndo.undoWestFrontInvert(p, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 8, 12, 20, 32, "leg2_right");
 			// leg2_right
 			SkinUndo.undoWestSide(p, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 7));
 			// leg2_ front
 			SkinUndo.undoWestFrontInvert(p, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ() + 3));
 			// leg2_behind
 			SkinUndo.undoWestFront(p, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() + 4));
 			// body
 			// body_front
 			SkinUndo.undoWestFront(p, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinUndo.undoWestFront(p, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinUndo.undoPartOfImageWest(t, 48, 52, 16, 20, "arm1_bottom");
 			SkinUndo.undoPartOfImageWest(t, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinUndo.undoWestSide(p, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_front
 			SkinUndo.undoWestFront(p, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_right
 			SkinUndo.undoWestSide(p, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinUndo.undoWestFront(p, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm2
 			SkinUndo.undoPartOfImageWest(t, 48, 52, 16, 20, "arm2_bottom");
 			SkinUndo.undoPartOfImageWest(t, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinUndo.undoWestSide(p, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 8));
 			// arm2_front
 			SkinUndo.undoWestFrontInvert(p, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// arm2_right
 			SkinUndo.undoWestSide(p, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 11));
 			// arm2_behind
 			SkinUndo.undoWestFrontInvert(p, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// head
 			// head_left
 			SkinUndo.undoWestSide(p, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinUndo.undoWestSide(p, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 7));
 			// head_behind
 			SkinUndo.undoWestFront(p, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ()));
 			SkinUndo.undoPartOfImageWest(t, 8, 16, 0, 8, "head_top");
 			SkinUndo.undoPartOfImageWest(t, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinUndo.undoWestFront(p, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			SkinUndo.undoPartOfImageWest(t, 32, 40, 8, 16, "hat_left");
 			SkinUndo.undoPartOfImageWest(t, 48, 56, 8, 16, "hat_right");
 			SkinUndo.undoPartOfImageWest(t, 56, 64, 8, 16, "hat_behind");
 			SkinUndo.undoPartOfImageWest(t, 40, 48, 8, 16, "hat_front");
 			SkinUndo.undoPartOfImageWest(t, 40, 48, 0, 8, "hat_top");*/
 			
 			SkinUndo.undoFullWest(t);
 		}else if(direction.equalsIgnoreCase("north")){
 			SkinUndo.undoFullNorth(t);
 		}else if(direction.equalsIgnoreCase("south")){
 			SkinUndo.undoFullSouth(t);
 		}
 		
 		p.sendMessage("2Undo successful.");
 	}
 
 	
 	
 	//TODO: ADD CLAY AND GLASS MODE AND SMOOTHING TO UPDATE MECHANISM
 	private void update(Location p, BufferedImage Image2, String skin, String direction, String mode){
 		if(skin_updating){
 			String uuid = UUID.randomUUID().toString().replaceAll("-", "");
 			saveSkin(p, skin, uuid, direction, mode);
 		}
 		
 		Location c = p;
 		
 		if(direction.equalsIgnoreCase("east") || direction.equalsIgnoreCase("e")){
 			// leg1_left
 			SkinUpdate.buildEastSide(this, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinUpdate.buildEastFront(this, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinUpdate.buildEastFrontInvert(this, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2
 			//SkinUpdate.buildPartOfImageEast(this, p, Image2, 8, 12, 20, 32, "leg2_right");
 			// leg2_right
 			SkinUpdate.buildEastSide(this, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 7));
 			// leg2_ front
 			SkinUpdate.buildEastFrontInvert(this, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ() + 3));
 			// leg2_behind
 			SkinUpdate.buildEastFront(this, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() + 4));
 			// body
 			// body_front
 			SkinUpdate.buildEastFront(this, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinUpdate.buildEastFront(this, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinUpdate.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinUpdate.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinUpdate.buildEastSide(this, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_front
 			SkinUpdate.buildEastFront(this, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_right
 			SkinUpdate.buildEastSide(this, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinUpdate.buildEastFront(this, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm2
 			SkinUpdate.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinUpdate.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinUpdate.buildEastSide(this, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 11));
 			// arm2_front
 			SkinUpdate.buildEastFrontInvert(this, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// arm2_right
 			SkinUpdate.buildEastSide(this, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 8));
 			// arm2_behind
 			SkinUpdate.buildEastFrontInvert(this, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// head
 			// head_left
 			SkinUpdate.buildEastSide(this, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinUpdate.buildEastSide(this, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 7));
 			// head_behind
 			SkinUpdate.buildEastFront(this, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ()));
 			SkinUpdate.buildPartOfImageEast(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinUpdate.buildPartOfImageEast(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinUpdate.buildEastFront(this, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			SkinUpdate.buildPartOfImageEast(this, p, Image2, 32, 40, 8, 16, "hat_left");
 			SkinUpdate.buildPartOfImageEast(this, p, Image2, 48, 56, 8, 16, "hat_right");
 			SkinUpdate.buildPartOfImageEast(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 			SkinUpdate.buildPartOfImageEast(this, p, Image2, 40, 48, 8, 16, "hat_front");
 			SkinUpdate.buildPartOfImageEast(this, p, Image2, 40, 48, 0, 8, "hat_top");
 			//SkinUpdate.buildPartOfImageEast(this, p, Image2, 48, 56, 0, 8, "hat_bottom");	// this looks like crap
 		}else if(direction.equalsIgnoreCase("south") || direction.equalsIgnoreCase("s")){
 			// leg1_left
 			SkinUpdate.buildSouthSide(this, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 1));
 			// leg1_front
 			SkinUpdate.buildSouthFront(this, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 2));
 			// leg1_ behind
 			SkinUpdate.buildSouthFrontInvert(this, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 5));
 			// leg2
 			// leg2_right
 			SkinUpdate.buildSouthSide(this, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2_ front
 			SkinUpdate.buildSouthFrontInvert(this, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 3, c.getBlockY(), c.getBlockZ() + 2));
 			// leg2_behind
 			SkinUpdate.buildSouthFront(this, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY(), c.getBlockZ() + 5));
 			// body
 			// body_front
 			SkinUpdate.buildSouthFront(this, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 2));
 			// body_behind
 			SkinUpdate.buildSouthFront(this, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm1
 			SkinUpdate.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinUpdate.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinUpdate.buildSouthSide(this, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_front
 			SkinUpdate.buildSouthFront(this, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm1_right
 			SkinUpdate.buildSouthSide(this, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinUpdate.buildSouthFront(this, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm2
 			SkinUpdate.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinUpdate.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinUpdate.buildSouthSide(this, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 11, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_front
 			SkinUpdate.buildSouthFrontInvert(this, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm2_right
 			SkinUpdate.buildSouthSide(this, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_behind
 			SkinUpdate.buildSouthFrontInvert(this, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// head
 			// head_left
 			SkinUpdate.buildSouthSide(this, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 			// head_right
 			SkinUpdate.buildSouthSide(this, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ() - 1));
 			// head_behind
 			SkinUpdate.buildSouthFront(this, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 7));
 			SkinUpdate.buildPartOfImageSouth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinUpdate.buildPartOfImageSouth(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinUpdate.buildSouthFront(this, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			// hat_left
 			SkinUpdate.buildSouthSideHAT(this, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 1));
 			// hat_right
 			SkinUpdate.buildSouthSideHATInvert(this, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() + 8));
 			// hat_behind
 			SkinUpdate.buildSouthFrontHAT(this, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 8));
 			// hat_front
 			SkinUpdate.buildSouthFrontHAT(this, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 			// hat_top
 			SkinUpdate.buildPartOfImageSouth(this, p, Image2, 40, 48, 0, 8, "hat_top");
 		}else if(direction.equalsIgnoreCase("west") || direction.equalsIgnoreCase("w")){
 			// leg1_left
 			SkinUpdate.buildWestSide(this, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinUpdate.buildWestFront(this, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinUpdate.buildWestFrontInvert(this, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2
 			// leg2_right
 			SkinUpdate.buildWestSide(this, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 7));
 			// leg2_ front
 			SkinUpdate.buildWestFrontInvert(this, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ() - 3));
 			// leg2_behind
 			SkinUpdate.buildWestFront(this, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() - 4));
 			// body
 			// body_front
 			SkinUpdate.buildWestFront(this, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinUpdate.buildWestFront(this, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinUpdate.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinUpdate.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinUpdate.buildWestSide(this, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_front
 			SkinUpdate.buildWestFront(this, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_right
 			SkinUpdate.buildWestSide(this, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinUpdate.buildWestFront(this, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm2
 			SkinUpdate.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinUpdate.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinUpdate.buildWestSide(this, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 11));
 			// arm2_front
 			SkinUpdate.buildWestFrontInvert(this, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// arm2_right
 			SkinUpdate.buildWestSide(this, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 8));
 			// arm2_behind
 			SkinUpdate.buildWestFrontInvert(this, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// head
 			// head_left
 			SkinUpdate.buildWestSide(this, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinUpdate.buildWestSide(this, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_behind
 			SkinUpdate.buildWestFront(this, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ()));
 			// head_top and head_bottom
 			SkinUpdate.buildPartOfImageWest(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinUpdate.buildPartOfImageWest(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinUpdate.buildWestFront(this, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			// hat_left
 			SkinUpdate.buildWestSideHAT(this, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 			// hat_right
 			SkinUpdate.buildWestSideHATInvert(this, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 			// hat_behind
 			SkinUpdate.buildWestFrontHAT(this, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ()));
 			// hat_front
 			SkinUpdate.buildWestFrontHAT(this, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 			// hat_top
 			SkinUpdate.buildPartOfImageWest(this, p, Image2, 40, 48, 0, 8, "hat_top");
 		}else if(direction.equalsIgnoreCase("north") || direction.equalsIgnoreCase("n")){
 			// leg1_left
 			SkinUpdate.buildNorthSide(this, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 1));
 			// leg1_front
 			SkinUpdate.buildNorthFront(this, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 2));
 			// leg1_behind
 			SkinUpdate.buildNorthFrontInvert(this, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 5));
 			// leg2
 			// leg2_right
 			SkinUpdate.buildNorthSide(this, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2_ front
 			SkinUpdate.buildNorthFrontInvert(this, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 3, c.getBlockY(), c.getBlockZ() - 2));
 			// leg2_behind
 			SkinUpdate.buildNorthFront(this, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY(), c.getBlockZ() - 5));
 			// body
 			// body_front
 			SkinUpdate.buildNorthFront(this, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 2));
 			// body_behind
 			SkinUpdate.buildNorthFront(this, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm1
 			// arm1_bottom and arm1_top
 			SkinUpdate.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinUpdate.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinUpdate.buildNorthSide(this, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_front
 			SkinUpdate.buildNorthFront(this, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm1_right
 			SkinUpdate.buildNorthSide(this, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinUpdate.buildNorthFront(this, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm2
 			// arm2_bottom and arm2_top
 			SkinUpdate.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinUpdate.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinUpdate.buildNorthSide(this, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 11, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_front
 			SkinUpdate.buildNorthFrontInvert(this, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm2_right
 			SkinUpdate.buildNorthSide(this, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_behind
 			SkinUpdate.buildNorthFrontInvert(this, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// head
 			// head_left
 			SkinUpdate.buildNorthSide(this, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 			// head_right
 			SkinUpdate.buildNorthSide(this, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ() + 1));
 			// head_behind
 			SkinUpdate.buildNorthFront(this, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_top
 			SkinUpdate.buildPartOfImageNorth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			// head_front
 			SkinUpdate.buildNorthFront(this, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			// hat_left:
 			SkinUpdate.buildNorthSideHAT(this, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 			// hat_right:
 			SkinUpdate.buildNorthSideHATInvert(this, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 			// hat_behind:
 			SkinUpdate.buildNorthFrontHAT(this, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 8));
 			// hat_front:
 			SkinUpdate.buildNorthFrontHAT(this, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 			//SkinUpdate.buildPartOfImageNorth(this, Image2, 32, 40, 8, 16, "hat_left");
 			//SkinUpdate.buildPartOfImageNorth(this, Image2, 48, 56, 8, 16, "hat_right");
 			//SkinUpdate.buildPartOfImageNorth(this, Image2, 56, 64, 8, 16, "hat_behind");
 			//SkinUpdate.buildPartOfImageNorth(this, Image2, 40, 48, 8, 16, "hat_front");
 			SkinUpdate.buildPartOfImageNorth(this, p, Image2, 40, 48, 0, 8, "hat_top");
 		}
 	}
 	
 	
 	
 	
 	
 	private void buildclay(Player p, BufferedImage Image2, String skin, String direction){
 		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
 
 		ArrayList<Location> loc_ = new ArrayList<Location>();
 		ArrayList<String> skin_ = new ArrayList<String>();
 		ArrayList<String> dir_ = new ArrayList<String>();
 		ArrayList<String> uuid_ = new ArrayList<String>();
 		if(undoloc.containsKey(p)){
 			loc_ = undoloc.get(p);
 			skin_ = undoskin.get(p);
 			dir_ = undodir.get(p);
 			uuid_ = undo_uuid.get(p);
 		}
 		
 		loc_.add(p.getLocation());
 		skin_.add(skin);
 		dir_.add(direction);
 		uuid_.add(uuid);
 		
 		//undoloc.put(p, p.getLocation());
 		//undoskin.put(p, skin);
 		//undodir.put(p, direction);
 		//undo_uuid.put(p, uuid);
 		
 		undoloc.put(p, loc_);
 		undoskin.put(p, skin_);
 		undodir.put(p, dir_);
 		undo_uuid.put(p, uuid_);
 		
 		Location c = p.getLocation();
 		
 		if(skin_updating){
 			saveSkin(p.getLocation(), skin, uuid, direction, "clay");
 		}
 		
 		// FIRST BUILD NORMAL WOOL:
 		if(direction.equalsIgnoreCase("east") || direction.equalsIgnoreCase("e")){
 			// leg1_left
 			SkinBuild.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuild.buildEastFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 8, 12, 20, 32, "leg2_right");
 			// leg2_right
 			SkinBuild.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 7));
 			// leg2_ front
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ() + 3));
 			// leg2_behind
 			SkinBuild.buildEastFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() + 4));
 			// body
 			// body_front
 			SkinBuild.buildEastFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuild.buildEastFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_front
 			SkinBuild.buildEastFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_right
 			SkinBuild.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuild.buildEastFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm2
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 11));
 			// arm2_front
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// arm2_right
 			SkinBuild.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 8));
 			// arm2_behind
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// head
 			// head_left
 			SkinBuild.buildEastSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuild.buildEastSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 7));
 			// head_behind
 			SkinBuild.buildEastFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ()));
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildEastFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 56, 0, 8, "hat_bottom");	// this looks like crap
 		}else if(direction.equalsIgnoreCase("south") || direction.equalsIgnoreCase("s")){
 			// leg1_left
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 1));
 			// leg1_front
 			SkinBuild.buildSouthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 2));
 			// leg1_ behind
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 5));
 			// leg2
 			// leg2_right
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2_ front
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 3, c.getBlockY(), c.getBlockZ() + 2));
 			// leg2_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY(), c.getBlockZ() + 5));
 			// body
 			// body_front
 			SkinBuild.buildSouthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 2));
 			// body_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm1
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_front
 			SkinBuild.buildSouthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm1_right
 			SkinBuild.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm2
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 11, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_front
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm2_right
 			SkinBuild.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_behind
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// head
 			// head_left
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 			// head_right
 			SkinBuild.buildSouthSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ() - 1)); 
 			// head_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 7));
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildSouthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuild.buildSouthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_right
 				SkinBuild.buildSouthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_behind
 				SkinBuild.buildSouthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_front
 				SkinBuild.buildSouthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_top
 				SkinBuild.buildPartOfImageSouth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("west") || direction.equalsIgnoreCase("w")){
 			// leg1_left
 			SkinBuild.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuild.buildWestFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2
 			// leg2_right
 			SkinBuild.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 7));
 			// leg2_ front
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ() - 3));
 			// leg2_behind
 			SkinBuild.buildWestFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() - 4));
 			// body
 			// body_front
 			SkinBuild.buildWestFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuild.buildWestFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_front
 			SkinBuild.buildWestFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_right
 			SkinBuild.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuild.buildWestFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm2
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 11));
 			// arm2_front
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// arm2_right
 			SkinBuild.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 8));
 			// arm2_behind
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// head
 			// head_left
 			SkinBuild.buildWestSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuild.buildWestSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 7)); 
 			// head_behind
 			SkinBuild.buildWestFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ()));
 			// head_top and head_bottom
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildWestFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuild.buildWestSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right
 				SkinBuild.buildWestSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind
 				SkinBuild.buildWestFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_front
 				SkinBuild.buildWestFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_top
 				SkinBuild.buildPartOfImageWest(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("north") || direction.equalsIgnoreCase("n")){
 			// leg1_left
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 1));
 			// leg1_front
 			SkinBuild.buildNorthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 2));
 			// leg1_behind
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 5));
 			// leg2
 			// leg2_right
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2_ front
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 3, c.getBlockY(), c.getBlockZ() - 2));
 			// leg2_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY(), c.getBlockZ() - 5));
 			// body
 			// body_front
 			SkinBuild.buildNorthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 2));
 			// body_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm1
 			// arm1_bottom and arm1_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_front
 			SkinBuild.buildNorthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm1_right
 			SkinBuild.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm2
 			// arm2_bottom and arm2_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 11, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_front
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm2_right
 			SkinBuild.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_behind
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// head
 			// head_left
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 			// head_right
 			SkinBuild.buildNorthSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ() + 1)); 
 			// head_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			// head_front
 			SkinBuild.buildNorthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left:
 				SkinBuild.buildNorthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right:
 				SkinBuild.buildNorthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind:
 				SkinBuild.buildNorthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_front:
 				SkinBuild.buildNorthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuild.buildPartOfImageNorth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}
 		
 		
 		//THEN OVERLAY WITH CLAY
 		if(direction.equalsIgnoreCase("east") || direction.equalsIgnoreCase("e")){
 			// leg1_left
 			SkinBuildClay.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuildClay.buildEastFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuildClay.buildEastFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2
 			//SkinBuildClay.buildPartOfImageEast(this, p, Image2, 8, 12, 20, 32, "leg2_right");
 			// leg2_right
 			SkinBuildClay.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 7));
 			// leg2_ front
 			SkinBuildClay.buildEastFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ() + 3));
 			// leg2_behind
 			SkinBuildClay.buildEastFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() + 4));
 			// body
 			// body_front
 			SkinBuildClay.buildEastFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuildClay.buildEastFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuildClay.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildClay.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildClay.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_front
 			SkinBuildClay.buildEastFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_right
 			SkinBuildClay.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuildClay.buildEastFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm2
 			SkinBuildClay.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildClay.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildClay.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 11));
 			// arm2_front
 			SkinBuildClay.buildEastFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// arm2_right
 			SkinBuildClay.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 8));
 			// arm2_behind
 			SkinBuildClay.buildEastFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// head
 			// head_left
 			SkinBuildClay.buildEastSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuildClay.buildEastSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 7));
 			// head_behind
 			SkinBuildClay.buildEastFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ()));
 			SkinBuildClay.buildPartOfImageEast(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuildClay.buildPartOfImageEast(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuildClay.buildEastFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				SkinBuildClay.buildPartOfImageEast(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				SkinBuildClay.buildPartOfImageEast(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				SkinBuildClay.buildPartOfImageEast(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				SkinBuildClay.buildPartOfImageEast(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuildClay.buildPartOfImageEast(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 			//SkinBuildClay.buildPartOfImageEast(this, p, Image2, 48, 56, 0, 8, "hat_bottom");	// this looks like crap
 		}else if(direction.equalsIgnoreCase("south") || direction.equalsIgnoreCase("s")){
 			// leg1_left
 			SkinBuildClay.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 1));
 			// leg1_front
 			SkinBuildClay.buildSouthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 2));
 			// leg1_ behind
 			SkinBuildClay.buildSouthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 5));
 			// leg2
 			// leg2_right
 			SkinBuildClay.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2_ front
 			SkinBuildClay.buildSouthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 3, c.getBlockY(), c.getBlockZ() + 2));
 			// leg2_behind
 			SkinBuildClay.buildSouthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY(), c.getBlockZ() + 5));
 			// body
 			// body_front
 			SkinBuildClay.buildSouthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 2));
 			// body_behind
 			SkinBuildClay.buildSouthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm1
 			SkinBuildClay.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildClay.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildClay.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_front
 			SkinBuildClay.buildSouthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm1_right
 			SkinBuildClay.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuildClay.buildSouthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm2
 			SkinBuildClay.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildClay.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildClay.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 11, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_front
 			SkinBuildClay.buildSouthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm2_right
 			SkinBuildClay.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_behind
 			SkinBuildClay.buildSouthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// head
 			// head_left
 			SkinBuildClay.buildSouthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 			// head_right
 			SkinBuildClay.buildSouthSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ()  + 8)); 
 			// head_behind
 			SkinBuildClay.buildSouthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 7));
 			SkinBuildClay.buildPartOfImageSouth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuildClay.buildPartOfImageSouth(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuildClay.buildSouthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuildClay.buildSouthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_right
 				SkinBuildClay.buildSouthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_behind
 				SkinBuildClay.buildSouthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_front
 				SkinBuildClay.buildSouthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_top
 				SkinBuildClay.buildPartOfImageSouth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("west") || direction.equalsIgnoreCase("w")){
 			// leg1_left
 			SkinBuildClay.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuildClay.buildWestFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuildClay.buildWestFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2
 			// leg2_right
 			SkinBuildClay.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 7));
 			// leg2_ front
 			SkinBuildClay.buildWestFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ() - 3));
 			// leg2_behind
 			SkinBuildClay.buildWestFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() - 4));
 			// body
 			// body_front
 			SkinBuildClay.buildWestFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuildClay.buildWestFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuildClay.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildClay.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildClay.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_front
 			SkinBuildClay.buildWestFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_right
 			SkinBuildClay.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuildClay.buildWestFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm2
 			SkinBuildClay.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildClay.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildClay.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 11));
 			// arm2_front
 			SkinBuildClay.buildWestFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// arm2_right
 			SkinBuildClay.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 8));
 			// arm2_behind
 			SkinBuildClay.buildWestFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// head
 			// head_left
 			SkinBuildClay.buildWestSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuildClay.buildWestSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX()  - 8, c.getBlockY() + 24, c.getBlockZ() - 7)); 
 			// head_behind
 			SkinBuildClay.buildWestFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ()));
 			// head_top and head_bottom
 			SkinBuildClay.buildPartOfImageWest(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuildClay.buildPartOfImageWest(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuildClay.buildWestFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuildClay.buildWestSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right
 				SkinBuildClay.buildWestSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind
 				SkinBuildClay.buildWestFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_front
 				SkinBuildClay.buildWestFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_top
 				SkinBuildClay.buildPartOfImageWest(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("north") || direction.equalsIgnoreCase("n")){
 			// leg1_left
 			SkinBuildClay.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 1));
 			// leg1_front
 			SkinBuildClay.buildNorthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 2));
 			// leg1_behind
 			SkinBuildClay.buildNorthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 5));
 			// leg2
 			// leg2_right
 			SkinBuildClay.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2_ front
 			SkinBuildClay.buildNorthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 3, c.getBlockY(), c.getBlockZ() - 2));
 			// leg2_behind
 			SkinBuildClay.buildNorthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY(), c.getBlockZ() - 5));
 			// body
 			// body_front
 			SkinBuildClay.buildNorthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 2));
 			// body_behind
 			SkinBuildClay.buildNorthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm1
 			// arm1_bottom and arm1_top
 			SkinBuildClay.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildClay.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildClay.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_front
 			SkinBuildClay.buildNorthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm1_right
 			SkinBuildClay.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuildClay.buildNorthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm2
 			// arm2_bottom and arm2_top
 			SkinBuildClay.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildClay.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildClay.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 11, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_front
 			SkinBuildClay.buildNorthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm2_right
 			SkinBuildClay.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_behind
 			SkinBuildClay.buildNorthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// head
 			// head_left
 			SkinBuildClay.buildNorthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 			// head_right
 			SkinBuildClay.buildNorthSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ() - 8)); 
 			// head_behind
 			SkinBuildClay.buildNorthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_top
 			SkinBuildClay.buildPartOfImageNorth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			// head_front
 			SkinBuildClay.buildNorthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left:
 				SkinBuildClay.buildNorthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right:
 				SkinBuildClay.buildNorthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind:
 				SkinBuildClay.buildNorthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_front:
 				SkinBuildClay.buildNorthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 				//SkinBuildClay.buildPartOfImageNorth(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				//SkinBuildClay.buildPartOfImageNorth(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				//SkinBuildClay.buildPartOfImageNorth(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				//SkinBuildClay.buildPartOfImageNorth(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuildClay.buildPartOfImageNorth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}
 		
 		p.sendMessage("2Finished building the skin!");
 	}
 	
 	
 	
 	private void buildglass(Player p, BufferedImage Image2, String skin, String direction){
 		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
 		
 		ArrayList<Location> loc_ = new ArrayList<Location>();
 		ArrayList<String> skin_ = new ArrayList<String>();
 		ArrayList<String> dir_ = new ArrayList<String>();
 		ArrayList<String> uuid_ = new ArrayList<String>();
 		if(undoloc.containsKey(p)){
 			loc_ = undoloc.get(p);
 			skin_ = undoskin.get(p);
 			dir_ = undodir.get(p);
 			uuid_ = undo_uuid.get(p);
 		}
 		
 		loc_.add(p.getLocation());
 		skin_.add(skin);
 		dir_.add(direction);
 		uuid_.add(uuid);
 		
 		//undoloc.put(p, p.getLocation());
 		//undoskin.put(p, skin);
 		//undodir.put(p, direction);
 		//undo_uuid.put(p, uuid);
 		
 		undoloc.put(p, loc_);
 		undoskin.put(p, skin_);
 		undodir.put(p, dir_);
 		undo_uuid.put(p, uuid_);
 		
 		Location c = p.getLocation();
 		
 		if(skin_updating){
 			saveSkin(p.getLocation(), skin, uuid, direction, "glass");
 		}
 		
 		// FIRST BUILD NORMAL WOOL:
 		if(direction.equalsIgnoreCase("east") || direction.equalsIgnoreCase("e")){
 			// leg1_left
 			SkinBuild.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuild.buildEastFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 8, 12, 20, 32, "leg2_right");
 			// leg2_right
 			SkinBuild.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 7));
 			// leg2_ front
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ() + 3));
 			// leg2_behind
 			SkinBuild.buildEastFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() + 4));
 			// body
 			// body_front
 			SkinBuild.buildEastFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuild.buildEastFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_front
 			SkinBuild.buildEastFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_right
 			SkinBuild.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuild.buildEastFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm2
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 11));
 			// arm2_front
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// arm2_right
 			SkinBuild.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 8));
 			// arm2_behind
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// head
 			// head_left
 			SkinBuild.buildEastSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuild.buildEastSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 7));
 			// head_behind
 			SkinBuild.buildEastFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ()));
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildEastFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 56, 0, 8, "hat_bottom");	// this looks like crap
 		}else if(direction.equalsIgnoreCase("south") || direction.equalsIgnoreCase("s")){
 			// leg1_left
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 1));
 			// leg1_front
 			SkinBuild.buildSouthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 2));
 			// leg1_ behind
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 5));
 			// leg2
 			// leg2_right
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2_ front
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 3, c.getBlockY(), c.getBlockZ() + 2));
 			// leg2_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY(), c.getBlockZ() + 5));
 			// body
 			// body_front
 			SkinBuild.buildSouthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 2));
 			// body_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm1
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_front
 			SkinBuild.buildSouthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm1_right
 			SkinBuild.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm2
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 11, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_front
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm2_right
 			SkinBuild.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_behind
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// head
 			// head_left
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 			// head_right
 			SkinBuild.buildSouthSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ() - 1)); 
 			// head_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 7));
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildSouthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuild.buildSouthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_right
 				SkinBuild.buildSouthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_behind
 				SkinBuild.buildSouthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_front
 				SkinBuild.buildSouthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_top
 				SkinBuild.buildPartOfImageSouth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("west") || direction.equalsIgnoreCase("w")){
 			// leg1_left
 			SkinBuild.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuild.buildWestFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2
 			// leg2_right
 			SkinBuild.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 7));
 			// leg2_ front
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ() - 3));
 			// leg2_behind
 			SkinBuild.buildWestFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() - 4));
 			// body
 			// body_front
 			SkinBuild.buildWestFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuild.buildWestFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_front
 			SkinBuild.buildWestFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_right
 			SkinBuild.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuild.buildWestFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm2
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 11));
 			// arm2_front
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// arm2_right
 			SkinBuild.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 8));
 			// arm2_behind
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// head
 			// head_left
 			SkinBuild.buildWestSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuild.buildWestSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 7)); 
 			// head_behind
 			SkinBuild.buildWestFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ()));
 			// head_top and head_bottom
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildWestFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuild.buildWestSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right
 				SkinBuild.buildWestSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind
 				SkinBuild.buildWestFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_front
 				SkinBuild.buildWestFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_top
 				SkinBuild.buildPartOfImageWest(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("north") || direction.equalsIgnoreCase("n")){
 			// leg1_left
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 1));
 			// leg1_front
 			SkinBuild.buildNorthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 2));
 			// leg1_behind
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 5));
 			// leg2
 			// leg2_right
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2_ front
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 3, c.getBlockY(), c.getBlockZ() - 2));
 			// leg2_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY(), c.getBlockZ() - 5));
 			// body
 			// body_front
 			SkinBuild.buildNorthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 2));
 			// body_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm1
 			// arm1_bottom and arm1_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_front
 			SkinBuild.buildNorthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm1_right
 			SkinBuild.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm2
 			// arm2_bottom and arm2_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 11, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_front
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm2_right
 			SkinBuild.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_behind
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// head
 			// head_left
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 			// head_right
 			SkinBuild.buildNorthSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ() + 1)); 
 			// head_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			// head_front
 			SkinBuild.buildNorthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left:
 				SkinBuild.buildNorthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right:
 				SkinBuild.buildNorthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind:
 				SkinBuild.buildNorthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_front:
 				SkinBuild.buildNorthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuild.buildPartOfImageNorth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}
 		
 		
 		//THEN OVERLAY WITH GLASS
 		if(direction.equalsIgnoreCase("east") || direction.equalsIgnoreCase("e")){
 			// leg1_left
 			SkinBuildGlass.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuildGlass.buildEastFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuildGlass.buildEastFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2
 			//SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 8, 12, 20, 32, "leg2_right");
 			// leg2_right
 			SkinBuildGlass.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 7));
 			// leg2_ front
 			SkinBuildGlass.buildEastFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ() + 3));
 			// leg2_behind
 			SkinBuildGlass.buildEastFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() + 4));
 			// body
 			// body_front
 			SkinBuildGlass.buildEastFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuildGlass.buildEastFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildGlass.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_front
 			SkinBuildGlass.buildEastFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_right
 			SkinBuildGlass.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuildGlass.buildEastFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm2
 			SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildGlass.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 11));
 			// arm2_front
 			SkinBuildGlass.buildEastFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// arm2_right
 			SkinBuildGlass.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 8));
 			// arm2_behind
 			SkinBuildGlass.buildEastFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// head
 			// head_left
 			SkinBuildGlass.buildEastSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuildGlass.buildEastSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 7));
 			// head_behind
 			SkinBuildGlass.buildEastFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ()));
 			SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuildGlass.buildEastFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 			//SkinBuildGlass.buildPartOfImageEast(this, p, Image2, 48, 56, 0, 8, "hat_bottom");	// this looks like crap
 		}else if(direction.equalsIgnoreCase("south") || direction.equalsIgnoreCase("s")){
 			// leg1_left
 			SkinBuildGlass.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 1));
 			// leg1_front
 			SkinBuildGlass.buildSouthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 2));
 			// leg1_ behind
 			SkinBuildGlass.buildSouthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 5));
 			// leg2
 			// leg2_right
 			SkinBuildGlass.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2_ front
 			SkinBuildGlass.buildSouthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 3, c.getBlockY(), c.getBlockZ() + 2));
 			// leg2_behind
 			SkinBuildGlass.buildSouthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY(), c.getBlockZ() + 5));
 			// body
 			// body_front
 			SkinBuildGlass.buildSouthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 2));
 			// body_behind
 			SkinBuildGlass.buildSouthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm1
 			SkinBuildGlass.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildGlass.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildGlass.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_front
 			SkinBuildGlass.buildSouthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm1_right
 			SkinBuildGlass.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuildGlass.buildSouthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm2
 			SkinBuildGlass.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildGlass.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildGlass.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 11, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_front
 			SkinBuildGlass.buildSouthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm2_right
 			SkinBuildGlass.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_behind
 			SkinBuildGlass.buildSouthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// head
 			// head_left
 			SkinBuildGlass.buildSouthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 			// head_right
 			SkinBuildGlass.buildSouthSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ()  + 8)); 
 			// head_behind
 			SkinBuildGlass.buildSouthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 7));
 			SkinBuildGlass.buildPartOfImageSouth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuildGlass.buildPartOfImageSouth(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuildGlass.buildSouthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuildGlass.buildSouthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_right
 				SkinBuildGlass.buildSouthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_behind
 				SkinBuildGlass.buildSouthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_front
 				SkinBuildGlass.buildSouthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_top
 				SkinBuildGlass.buildPartOfImageSouth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("west") || direction.equalsIgnoreCase("w")){
 			// leg1_left
 			SkinBuildGlass.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuildGlass.buildWestFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuildGlass.buildWestFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2
 			// leg2_right
 			SkinBuildGlass.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 7));
 			// leg2_ front
 			SkinBuildGlass.buildWestFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ() - 3));
 			// leg2_behind
 			SkinBuildGlass.buildWestFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() - 4));
 			// body
 			// body_front
 			SkinBuildGlass.buildWestFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuildGlass.buildWestFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuildGlass.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildGlass.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildGlass.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_front
 			SkinBuildGlass.buildWestFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_right
 			SkinBuildGlass.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuildGlass.buildWestFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm2
 			SkinBuildGlass.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildGlass.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildGlass.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 11));
 			// arm2_front
 			SkinBuildGlass.buildWestFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// arm2_right
 			SkinBuildGlass.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 8));
 			// arm2_behind
 			SkinBuildGlass.buildWestFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// head
 			// head_left
 			SkinBuildGlass.buildWestSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuildGlass.buildWestSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX()  - 8, c.getBlockY() + 24, c.getBlockZ() - 7)); 
 			// head_behind
 			SkinBuildGlass.buildWestFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ()));
 			// head_top and head_bottom
 			SkinBuildGlass.buildPartOfImageWest(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuildGlass.buildPartOfImageWest(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuildGlass.buildWestFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuildGlass.buildWestSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right
 				SkinBuildGlass.buildWestSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind
 				SkinBuildGlass.buildWestFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_front
 				SkinBuildGlass.buildWestFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_top
 				SkinBuildGlass.buildPartOfImageWest(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("north") || direction.equalsIgnoreCase("n")){
 			// leg1_left
 			SkinBuildGlass.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 1));
 			// leg1_front
 			SkinBuildGlass.buildNorthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 2));
 			// leg1_behind
 			SkinBuildGlass.buildNorthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 5));
 			// leg2
 			// leg2_right
 			SkinBuildGlass.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2_ front
 			SkinBuildGlass.buildNorthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 3, c.getBlockY(), c.getBlockZ() - 2));
 			// leg2_behind
 			SkinBuildGlass.buildNorthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY(), c.getBlockZ() - 5));
 			// body
 			// body_front
 			SkinBuildGlass.buildNorthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 2));
 			// body_behind
 			SkinBuildGlass.buildNorthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm1
 			// arm1_bottom and arm1_top
 			SkinBuildGlass.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildGlass.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildGlass.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_front
 			SkinBuildGlass.buildNorthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm1_right
 			SkinBuildGlass.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuildGlass.buildNorthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm2
 			// arm2_bottom and arm2_top
 			SkinBuildGlass.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildGlass.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildGlass.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 11, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_front
 			SkinBuildGlass.buildNorthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm2_right
 			SkinBuildGlass.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_behind
 			SkinBuildGlass.buildNorthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// head
 			// head_left
 			SkinBuildGlass.buildNorthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 			// head_right
 			SkinBuildGlass.buildNorthSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ()  - 8)); 
 			// head_behind
 			SkinBuildGlass.buildNorthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_top
 			SkinBuildGlass.buildPartOfImageNorth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			// head_front
 			SkinBuildGlass.buildNorthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left:
 				SkinBuildGlass.buildNorthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right:
 				SkinBuildGlass.buildNorthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind:
 				SkinBuildGlass.buildNorthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_front:
 				SkinBuildGlass.buildNorthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 				//SkinBuildGlass.buildPartOfImageNorth(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				//SkinBuildGlass.buildPartOfImageNorth(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				//SkinBuildGlass.buildPartOfImageNorth(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				//SkinBuildGlass.buildPartOfImageNorth(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuildGlass.buildPartOfImageNorth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}
 		
 		p.sendMessage("2Finished building the skin!");
 	}
 	
 	
 	
 	private void buildall(Player p, BufferedImage Image2, String skin, String direction){
 		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
 		
 		ArrayList<Location> loc_ = new ArrayList<Location>();
 		ArrayList<String> skin_ = new ArrayList<String>();
 		ArrayList<String> dir_ = new ArrayList<String>();
 		ArrayList<String> uuid_ = new ArrayList<String>();
 		if(undoloc.containsKey(p)){
 			loc_ = undoloc.get(p);
 			skin_ = undoskin.get(p);
 			dir_ = undodir.get(p);
 			uuid_ = undo_uuid.get(p);
 		}
 		
 		loc_.add(p.getLocation());
 		skin_.add(skin);
 		dir_.add(direction);
 		uuid_.add(uuid);
 		
 		//undoloc.put(p, p.getLocation());
 		//undoskin.put(p, skin);
 		//undodir.put(p, direction);
 		//undo_uuid.put(p, uuid);
 		
 		undoloc.put(p, loc_);
 		undoskin.put(p, skin_);
 		undodir.put(p, dir_);
 		undo_uuid.put(p, uuid_);
 		
 		Location c = p.getLocation();
 		
 		if(skin_updating){
 			saveSkin(p.getLocation(), skin, uuid, direction, "all");
 		}
 		
 		// FIRST BUILD NORMAL WOOL:
 		if(direction.equalsIgnoreCase("east") || direction.equalsIgnoreCase("e")){
 			// leg1_left
 			SkinBuild.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuild.buildEastFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 8, 12, 20, 32, "leg2_right");
 			// leg2_right
 			SkinBuild.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 7));
 			// leg2_ front
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ() + 3));
 			// leg2_behind
 			SkinBuild.buildEastFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() + 4));
 			// body
 			// body_front
 			SkinBuild.buildEastFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuild.buildEastFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_front
 			SkinBuild.buildEastFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_right
 			SkinBuild.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuild.buildEastFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm2
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 11));
 			// arm2_front
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// arm2_right
 			SkinBuild.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 8));
 			// arm2_behind
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// head
 			// head_left
 			SkinBuild.buildEastSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuild.buildEastSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 7));
 			// head_behind
 			SkinBuild.buildEastFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ()));
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildEastFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 56, 0, 8, "hat_bottom");	// this looks like crap
 		}else if(direction.equalsIgnoreCase("south") || direction.equalsIgnoreCase("s")){
 			// leg1_left
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 1));
 			// leg1_front
 			SkinBuild.buildSouthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 2));
 			// leg1_ behind
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 5));
 			// leg2
 			// leg2_right
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2_ front
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 3, c.getBlockY(), c.getBlockZ() + 2));
 			// leg2_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY(), c.getBlockZ() + 5));
 			// body
 			// body_front
 			SkinBuild.buildSouthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 2));
 			// body_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm1
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_front
 			SkinBuild.buildSouthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm1_right
 			SkinBuild.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm2
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 11, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_front
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm2_right
 			SkinBuild.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_behind
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// head
 			// head_left
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 			// head_right
 			SkinBuild.buildSouthSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ() - 1)); 
 			// head_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 7));
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildSouthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuild.buildSouthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_right
 				SkinBuild.buildSouthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_behind
 				SkinBuild.buildSouthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_front
 				SkinBuild.buildSouthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_top
 				SkinBuild.buildPartOfImageSouth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("west") || direction.equalsIgnoreCase("w")){
 			// leg1_left
 			SkinBuild.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuild.buildWestFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2
 			// leg2_right
 			SkinBuild.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 7));
 			// leg2_ front
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ() - 3));
 			// leg2_behind
 			SkinBuild.buildWestFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() - 4));
 			// body
 			// body_front
 			SkinBuild.buildWestFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuild.buildWestFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_front
 			SkinBuild.buildWestFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_right
 			SkinBuild.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuild.buildWestFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm2
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 11));
 			// arm2_front
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// arm2_right
 			SkinBuild.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 8));
 			// arm2_behind
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// head
 			// head_left
 			SkinBuild.buildWestSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuild.buildWestSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 7)); 
 			// head_behind
 			SkinBuild.buildWestFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ()));
 			// head_top and head_bottom
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildWestFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuild.buildWestSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right
 				SkinBuild.buildWestSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind
 				SkinBuild.buildWestFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_front
 				SkinBuild.buildWestFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_top
 				SkinBuild.buildPartOfImageWest(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("north") || direction.equalsIgnoreCase("n")){
 			// leg1_left
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 1));
 			// leg1_front
 			SkinBuild.buildNorthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 2));
 			// leg1_behind
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 5));
 			// leg2
 			// leg2_right
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2_ front
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 3, c.getBlockY(), c.getBlockZ() - 2));
 			// leg2_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY(), c.getBlockZ() - 5));
 			// body
 			// body_front
 			SkinBuild.buildNorthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 2));
 			// body_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm1
 			// arm1_bottom and arm1_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_front
 			SkinBuild.buildNorthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm1_right
 			SkinBuild.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm2
 			// arm2_bottom and arm2_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 11, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_front
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm2_right
 			SkinBuild.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_behind
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// head
 			// head_left
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 			// head_right
 			SkinBuild.buildNorthSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ() + 1)); 
 			// head_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			// head_front
 			SkinBuild.buildNorthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left:
 				SkinBuild.buildNorthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right:
 				SkinBuild.buildNorthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind:
 				SkinBuild.buildNorthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_front:
 				SkinBuild.buildNorthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuild.buildPartOfImageNorth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}
 		
 		
 		//THEN OVERLAY WITH ALL BLOCKS
 		if(direction.equalsIgnoreCase("east") || direction.equalsIgnoreCase("e")){
 			// leg1_left
 			SkinBuildAll.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuildAll.buildEastFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuildAll.buildEastFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2
 			//SkinBuildAll.buildPartOfImageEast(this, p, Image2, 8, 12, 20, 32, "leg2_right");
 			// leg2_right
 			SkinBuildAll.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 7));
 			// leg2_ front
 			SkinBuildAll.buildEastFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ() + 3));
 			// leg2_behind
 			SkinBuildAll.buildEastFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() + 4));
 			// body
 			// body_front
 			SkinBuildAll.buildEastFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuildAll.buildEastFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuildAll.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildAll.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildAll.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_front
 			SkinBuildAll.buildEastFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_right
 			SkinBuildAll.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuildAll.buildEastFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm2
 			SkinBuildAll.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildAll.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildAll.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 11));
 			// arm2_front
 			SkinBuildAll.buildEastFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// arm2_right
 			SkinBuildAll.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 8));
 			// arm2_behind
 			SkinBuildAll.buildEastFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// head
 			// head_left
 			SkinBuildAll.buildEastSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuildAll.buildEastSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 7));
 			// head_behind
 			SkinBuildAll.buildEastFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ()));
 			SkinBuildAll.buildPartOfImageEast(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuildAll.buildPartOfImageEast(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuildAll.buildEastFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				SkinBuildAll.buildPartOfImageEast(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				SkinBuildAll.buildPartOfImageEast(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				SkinBuildAll.buildPartOfImageEast(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				SkinBuildAll.buildPartOfImageEast(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuildAll.buildPartOfImageEast(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 			//SkinBuildAll.buildPartOfImageEast(this, p, Image2, 48, 56, 0, 8, "hat_bottom");	// this looks like crap
 		}else if(direction.equalsIgnoreCase("south") || direction.equalsIgnoreCase("s")){
 			// leg1_left
 			SkinBuildAll.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 1));
 			// leg1_front
 			SkinBuildAll.buildSouthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 2));
 			// leg1_ behind
 			SkinBuildAll.buildSouthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 5));
 			// leg2
 			// leg2_right
 			SkinBuildAll.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2_ front
 			SkinBuildAll.buildSouthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 3, c.getBlockY(), c.getBlockZ() + 2));
 			// leg2_behind
 			SkinBuildAll.buildSouthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY(), c.getBlockZ() + 5));
 			// body
 			// body_front
 			SkinBuildAll.buildSouthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 2));
 			// body_behind
 			SkinBuildAll.buildSouthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm1
 			SkinBuildAll.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildAll.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildAll.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_front
 			SkinBuildAll.buildSouthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm1_right
 			SkinBuildAll.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuildAll.buildSouthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm2
 			SkinBuildAll.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildAll.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildAll.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 11, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_front
 			SkinBuildAll.buildSouthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm2_right
 			SkinBuildAll.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_behind
 			SkinBuildAll.buildSouthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// head
 			// head_left
 			SkinBuildAll.buildSouthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 			// head_right
 			SkinBuildAll.buildSouthSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ() + 8)); 
 			// head_behind
 			SkinBuildAll.buildSouthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 7));
 			SkinBuildAll.buildPartOfImageSouth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuildAll.buildPartOfImageSouth(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuildAll.buildSouthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuildAll.buildSouthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_right
 				SkinBuildAll.buildSouthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_behind
 				SkinBuildAll.buildSouthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_front
 				SkinBuildAll.buildSouthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_top
 				SkinBuildAll.buildPartOfImageSouth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("west") || direction.equalsIgnoreCase("w")){
 			// leg1_left
 			SkinBuildAll.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuildAll.buildWestFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuildAll.buildWestFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2
 			// leg2_right
 			SkinBuildAll.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 7));
 			// leg2_ front
 			SkinBuildAll.buildWestFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ() - 3));
 			// leg2_behind
 			SkinBuildAll.buildWestFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() - 4));
 			// body
 			// body_front
 			SkinBuildAll.buildWestFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuildAll.buildWestFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuildAll.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildAll.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildAll.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_front
 			SkinBuildAll.buildWestFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_right
 			SkinBuildAll.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuildAll.buildWestFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm2
 			SkinBuildAll.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildAll.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildAll.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 11));
 			// arm2_front
 			SkinBuildAll.buildWestFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// arm2_right
 			SkinBuildAll.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 8));
 			// arm2_behind
 			SkinBuildAll.buildWestFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// head
 			// head_left
 			SkinBuildAll.buildWestSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuildAll.buildWestSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() - 7)); 
 			// head_behind
 			SkinBuildAll.buildWestFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ()));
 			// head_top and head_bottom
 			SkinBuildAll.buildPartOfImageWest(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuildAll.buildPartOfImageWest(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuildAll.buildWestFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuildAll.buildWestSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right
 				SkinBuildAll.buildWestSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind
 				SkinBuildAll.buildWestFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_front
 				SkinBuildAll.buildWestFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_top
 				SkinBuildAll.buildPartOfImageWest(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("north") || direction.equalsIgnoreCase("n")){
 			// leg1_left
 			SkinBuildAll.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 1));
 			// leg1_front
 			SkinBuildAll.buildNorthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 2));
 			// leg1_behind
 			SkinBuildAll.buildNorthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 5));
 			// leg2
 			// leg2_right
 			SkinBuildAll.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2_ front
 			SkinBuildAll.buildNorthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 3, c.getBlockY(), c.getBlockZ() - 2));
 			// leg2_behind
 			SkinBuildAll.buildNorthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY(), c.getBlockZ() - 5));
 			// body
 			// body_front
 			SkinBuildAll.buildNorthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 2));
 			// body_behind
 			SkinBuildAll.buildNorthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm1
 			// arm1_bottom and arm1_top
 			SkinBuildAll.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuildAll.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuildAll.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_front
 			SkinBuildAll.buildNorthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm1_right
 			SkinBuildAll.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuildAll.buildNorthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm2
 			// arm2_bottom and arm2_top
 			SkinBuildAll.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuildAll.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuildAll.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 11, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_front
 			SkinBuildAll.buildNorthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm2_right
 			SkinBuildAll.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_behind
 			SkinBuildAll.buildNorthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// head
 			// head_left
 			SkinBuildAll.buildNorthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 			// head_right
 			SkinBuildAll.buildNorthSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ() - 8)); 
 			// head_behind
 			SkinBuildAll.buildNorthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_top
 			SkinBuildAll.buildPartOfImageNorth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			// head_front
 			SkinBuildAll.buildNorthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left:
 				SkinBuildAll.buildNorthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right:
 				SkinBuildAll.buildNorthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind:
 				SkinBuildAll.buildNorthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_front:
 				SkinBuildAll.buildNorthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 				//SkinBuildAll.buildPartOfImageNorth(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				//SkinBuildAll.buildPartOfImageNorth(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				//SkinBuildAll.buildPartOfImageNorth(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				//SkinBuildAll.buildPartOfImageNorth(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuildAll.buildPartOfImageNorth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}
 		
 		p.sendMessage("2Finished building the skin!");
 	}
 	
 	
 	
 	private void build(Player p, BufferedImage Image2, String skin, String direction){
 		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
 		
 		ArrayList<Location> loc_ = new ArrayList<Location>();
 		ArrayList<String> skin_ = new ArrayList<String>();
 		ArrayList<String> dir_ = new ArrayList<String>();
 		ArrayList<String> uuid_ = new ArrayList<String>();
 		if(undoloc.containsKey(p)){
 			loc_ = undoloc.get(p);
 			skin_ = undoskin.get(p);
 			dir_ = undodir.get(p);
 			uuid_ = undo_uuid.get(p);
 		}
 		
 		loc_.add(p.getLocation());
 		skin_.add(skin);
 		dir_.add(direction);
 		uuid_.add(uuid);
 		
 		//undoloc.put(p, p.getLocation());
 		//undoskin.put(p, skin);
 		//undodir.put(p, direction);
 		//undo_uuid.put(p, uuid);
 		
 		undoloc.put(p, loc_);
 		undoskin.put(p, skin_);
 		undodir.put(p, dir_);
 		undo_uuid.put(p, uuid_);
 
 		if(skin_updating){
 			saveSkin(p.getLocation(), skin, uuid, direction, "default");
 		}
 		
 		Location c = p.getLocation();
 		
 		if(direction.equalsIgnoreCase("east") || direction.equalsIgnoreCase("e")){
 			// leg1_left
 			SkinBuild.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuild.buildEastFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 8, 12, 20, 32, "leg2_right");
 			// leg2_right
 			SkinBuild.buildEastSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 7));
 			// leg2_ front
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY(), c.getBlockZ() + 3));
 			// leg2_behind
 			SkinBuild.buildEastFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY(), c.getBlockZ() + 4));
 			// body
 			// body_front
 			SkinBuild.buildEastFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuild.buildEastFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_front
 			SkinBuild.buildEastFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm1_right
 			SkinBuild.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuild.buildEastFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() - 4));
 			// arm2
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildEastSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 11));
 			// arm2_front
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 2, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// arm2_right
 			SkinBuild.buildEastSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 8));
 			// arm2_behind
 			SkinBuild.buildEastFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 5, c.getBlockY() + 12, c.getBlockZ() + 7));
 			// head
 			// head_left
 			SkinBuild.buildEastSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuild.buildEastSide(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 7));
 			// head_behind
 			SkinBuild.buildEastFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ()));
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageEast(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildEastFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuild.buildPartOfImageEast(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 			//SkinBuild.buildPartOfImageEast(this, p, Image2, 48, 56, 0, 8, "hat_bottom");	// this looks like crap
 		}else if(direction.equalsIgnoreCase("south") || direction.equalsIgnoreCase("s")){
 			// leg1_left
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 1));
 			// leg1_front
 			SkinBuild.buildSouthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() + 2));
 			// leg1_ behind
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY(), c.getBlockZ() + 5));
 			// leg2
 			// leg2_right
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2_ front
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 3, c.getBlockY(), c.getBlockZ() + 2));
 			// leg2_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY(), c.getBlockZ() + 5));
 			// body
 			// body_front
 			SkinBuild.buildSouthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 2));
 			// body_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm1
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_front
 			SkinBuild.buildSouthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm1_right
 			SkinBuild.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// arm2
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildSouthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 11, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_front
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 2));
 			// arm2_right
 			SkinBuild.buildSouthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm2_behind
 			SkinBuild.buildSouthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 12, c.getBlockZ() + 5));
 			// head
 			// head_left
 			SkinBuild.buildSouthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 			// head_right
 			SkinBuild.buildSouthSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ() + 8));
 			// head_behind
 			SkinBuild.buildSouthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 7));
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageSouth(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildSouthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuild.buildSouthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_right
 				SkinBuild.buildSouthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_behind
 				SkinBuild.buildSouthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 8));
 				// hat_front
 				SkinBuild.buildSouthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 1));
 				// hat_top
 				SkinBuild.buildPartOfImageSouth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("west") || direction.equalsIgnoreCase("w")){
 			// leg1_left
 			SkinBuild.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ()));
 			// leg1_front
 			SkinBuild.buildWestFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ()));
 			// leg1_ behind
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() + 1));
 			// leg2
 			// leg2_right
 			SkinBuild.buildWestSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 7));
 			// leg2_ front
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY(), c.getBlockZ() - 3));
 			// leg2_behind
 			SkinBuild.buildWestFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY(), c.getBlockZ() - 4));
 			// body
 			// body_front
 			SkinBuild.buildWestFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 2, c.getBlockY() + 12, c.getBlockZ()));
 			// body_behind
 			SkinBuild.buildWestFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX() -+ 5, c.getBlockY() + 12, c.getBlockZ()));
 			// arm1
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_front
 			SkinBuild.buildWestFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm1_right
 			SkinBuild.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() + 1));
 			// arm1_behind
 			SkinBuild.buildWestFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() + 4));
 			// arm2
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildWestSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 11));
 			// arm2_front
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() - 2, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// arm2_right
 			SkinBuild.buildWestSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 8));
 			// arm2_behind
 			SkinBuild.buildWestFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() - 5, c.getBlockY() + 12, c.getBlockZ() - 7));
 			// head
 			// head_left
 			SkinBuild.buildWestSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 			// head_right
 			SkinBuild.buildWestSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_behind
 			SkinBuild.buildWestFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX() - 7, c.getBlockY() + 24, c.getBlockZ()));
 			// head_top and head_bottom
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 8, 16, 0, 8, "head_top");
 			SkinBuild.buildPartOfImageWest(this, p, Image2, 16, 24, 0, 8, "head_bottom");
 			// head_front
 			SkinBuild.buildWestFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left
 				SkinBuild.buildWestSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right
 				SkinBuild.buildWestSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind
 				SkinBuild.buildWestFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX() - 8, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_front
 				SkinBuild.buildWestFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX() + 1, c.getBlockY() + 24, c.getBlockZ()));
 				// hat_top
 				SkinBuild.buildPartOfImageWest(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}else if(direction.equalsIgnoreCase("north") || direction.equalsIgnoreCase("n")){
 			// leg1_left
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 1));
 			// leg1_front
 			SkinBuild.buildNorthFront(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY(), c.getBlockZ() - 2));
 			// leg1_behind
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY(), c.getBlockZ() - 5));
 			// leg2
 			// leg2_right
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 4, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY(), c.getBlockZ() - 1));
 			// leg2_ front
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 4, 8, 20, 32, new Location(p.getWorld(), c.getBlockX() + 3, c.getBlockY(), c.getBlockZ() - 2));
 			// leg2_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 12, 16, 20, 32, new Location(p.getWorld(), c.getBlockX() + 4, c.getBlockY(), c.getBlockZ() - 5));
 			// body
 			// body_front
 			SkinBuild.buildNorthFront(this, p, Image2, 20, 28, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 2));
 			// body_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 32, 40, 20, 32, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm1
 			// arm1_bottom and arm1_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm1_bottom");
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm1_top");
 			// arm1_left
 			SkinBuild.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() - 4, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_front
 			SkinBuild.buildNorthFront(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm1_right
 			SkinBuild.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm1_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX()  - 4, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// arm2
 			// arm2_bottom and arm2_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 52, 16, 20, "arm2_bottom");
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 44, 48, 16, 20, "arm2_top");
 			// arm2_left
 			SkinBuild.buildNorthSide(this, p, Image2, 40, 44, 20, 32, new Location(p.getWorld(), c.getBlockX() + 11, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_front
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 44, 48, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 2));
 			// arm2_right
 			SkinBuild.buildNorthSide(this, p, Image2, 48, 52, 20, 32, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 12, c.getBlockZ() - 1));
 			// arm2_behind
 			SkinBuild.buildNorthFrontInvert(this, p, Image2, 52, 56, 20, 32, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 12, c.getBlockZ() - 5));
 			// head
 			// head_left
 			SkinBuild.buildNorthSide(this, p, Image2, 0, 8, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 			// head_right
 			SkinBuild.buildNorthSideInvert(this, p, Image2, 16, 24, 8, 16, new Location(p.getWorld(), c.getBlockX() + 7, c.getBlockY() + 24, c.getBlockZ() - 8));
 			// head_behind
 			SkinBuild.buildNorthFront(this, p, Image2, 24, 32, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 7));
 			// head_top
 			SkinBuild.buildPartOfImageNorth(this, p, Image2, 8, 16, 0, 8, "head_top");
 			// head_front
 			SkinBuild.buildNorthFront(this, p, Image2, 8, 16, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ()));
 			// hat layers
 			if(!skin.equalsIgnoreCase("notch")){
 				// hat_left:
 				SkinBuild.buildNorthSideHAT(this, p, Image2, 32, 40, 8, 16, new Location(p.getWorld(), c.getBlockX() - 1, c.getBlockY() + 24, c.getBlockZ() + 1));
 				// hat_right:
 				SkinBuild.buildNorthSideHATInvert(this, p, Image2, 48, 56, 8, 16, new Location(p.getWorld(), c.getBlockX() + 8, c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_behind:
 				SkinBuild.buildNorthFrontHAT(this, p, Image2, 56, 64, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() - 8));
 				// hat_front:
 				SkinBuild.buildNorthFrontHAT(this, p, Image2, 40, 48, 8, 16, new Location(p.getWorld(), c.getBlockX(), c.getBlockY() + 24, c.getBlockZ() + 1));
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 32, 40, 8, 16, "hat_left");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 48, 56, 8, 16, "hat_right");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 56, 64, 8, 16, "hat_behind");
 				//SkinBuild.buildPartOfImageNorth(this, p, Image2, 40, 48, 8, 16, "hat_front");
 				SkinBuild.buildPartOfImageNorth(this, p, Image2, 40, 48, 0, 8, "hat_top");	
 			}
 		}
 		
 		p.sendMessage("2Finished building the skin!");
 	}
 	
 
 	public String getStringFromColor(Color c){
 		String ret = "";
 
 		Integer r = c.getRed(); // RED
 		Integer g = c.getGreen(); // GREEN
 		Integer b = c.getBlue(); // BLUE
 		
 		float[] hsb = new float[3];
 		c.RGBtoHSB(r, g, b, hsb);
 		
 		float h = hsb[0]; // HUE
 		float s = hsb[1]; // SATURATION
 		float v = hsb[2]; // BRIGHTNESS
 		
 		if(s > 0.4 && v > 0.2 && h < 0.037){
 			ret = "RED";
 		}else if(s > 0.6 && v > 0.7 && h > 0.0333333333 && h < 0.1138888888){ // s > 0.4 && v > 0.5
 			ret = "ORANGE";
 		}else if(s > 0.4 && s < 0.61 && v > 0.14 && v < 0.5 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 79 && v > 0.14 && v < 0.45 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.15 && s < 0.4 && v > 0.14 && v < 0.6 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.15 && s < 0.4 && v > 0.6 && v < 0.81 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "RED";
 		}else if(s > 0.55 && v > 0.41 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "RED";
 		}else if(s > 0.15 && s < 0.4 && v > 0.8 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "PINK";
 		}else if(s > 0.4 && s < 0.61 && v > 0.14 && v < 0.75 && h > 0.038 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.6 && s < 0.8 && v > 0.09 && v < 0.7 && h > 0.038 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.6 && s < 0.8 && v > 0.7 && h > 0.038 && h < 0.12){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "ORANGE";
 		}else if(s > 0.8 && v > 0.09 && v < 0.7 && h > 0.038 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.3 && v > 0.5 && v < 0.8 && h > 0.02 && h < 0.0333333333){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "RED";
 		}else if(s > 0.3 && v < 0.5 && h > 0.02 && h < 0.0333333333){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.3 && v > 0.5 && v < 0.75 && h > 0.0333333333 && h < 0.1138888888){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.3 && v < 0.55 && h > 0.1138888888 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.3 && v > 0.55 && v < 0.7 && h > 0.1138888888 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "GREEN";
 		}else if(s < 0.41 && v < 0.2 && h > 0.01 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BLACK";
 		}else if(s > 0.4 && v < 0.35 && v > 0.2 && h > 0.969){
 			ret = "BROWN";
 		}else if(s > 0.4 && v < 0.2 && v > 0.1 && h > 0.079999999 && h < 0.1222222){
 			ret = "BROWN";
 		}else if(s > 0.8 && v < 0.15 && v > 0.05 && h > 0.079999999 && h < 0.1222222){
 			ret = "BROWN";
 		}else if(s > 0.4 && v > 0.5 && h > 0.1138888888 && h < 0.1916666666){
 			ret = "YELLOW";
 		}else if(s > 0.4 && v < 0.51 && v > 0.1 && h > 0.1138888888 && h < 0.1916666666){ // new
 			ret = "BROWN";
 		}else if(s > 0.4 && v > 0.2 && v < 0.81 && h > 0.1916666666 && h < 0.3805555555){
 			ret = "GREEN";
 		}else if(s > 0.4 && v > 0.5 && h > 0.1916666666 && h < 0.3805555555){
 			ret = "LIME";
 		}else if(s > 0.2 && v > 0.75 && h > 0.1916666666 && h < 0.3805555555){
 			ret = "LIME";
 		}else if(s > 0.2 && v > 0.8 && h > 0.3805555555 && h < 0.5194444444){ // v > 0.4 adjusted 3
 			ret = "LIGHT_BLUE";
 		}else if(s > 0.1 && s < 0.21 && v > 0.9 && h > 0.3805555555 && h < 0.5194444444){ // new 3
 			ret = "LIGHT_BLUE";
 		}else if(s > 0.4 && v < 0.81 && v > 0.2 && h > 0.3805555555 && h < 0.6027777777){ // adjusted 3
 			ret = "CYAN";
 		}else if(s > 0.4 && v > 0.2 && h > 0.5194444444 && h < 0.6027777777){
 			ret = "CYAN";
 		}else if(s > 0.4 && v > 0.4 && h > 0.6027777777 && h < 0.6944444444){
 			ret = "BLUE";
 		}else if(s > 0.2 && s < 0.41 && v > 0.7 && h > 0.6027777777 && h < 0.6944444444){ // adjusted 3
 			ret = "LIGHT_BLUE";
 		}else if(s > 0.114 && s < 0.2 && v > 0.6 && h > 0.6027777777 && h < 0.6944444444){ // new 3
 			ret = "BLUE";
 		}else if(s > 0.1 && s < 0.2 && v > 0.6 && v < 0.91 && h > 0.6027777777 && h < 0.6944444444){ // new 3
 			ret = "LIGHT_BLUE";
 		}else if(s > 0.114 && s < 0.2 && v > 0.9 && h > 0.6027777777 && h < 0.6944444444){ // new 3
 			ret = "BLUE";
 		}else if(s > 0.6 && v > 0.1 && h > 0.6027777777 && h < 0.6944444444){
 			ret = "BLUE";
 		}else if(s > 0.4 && v > 0.3 && h > 0.6944444444 && h < 0.8305555555){
 			ret = "PURPLE";
 		}else if(s > 0.4 && v > 0.4 && h > 0.8305555555 && h < 0.8777777777){
 			ret = "MAGENTA";
 		}else if(s > 0.3 && v > 0.4 && h > 0.8777777777 && h < 0.9611111111){
 			ret = "PINK";
 		}else if(s > 0.4 && v > 0.4 && h > 0.9361111111 && h < 1.0000000001){
 			ret = "RED";
 		}else if(s < 0.11 && v > 0.9){
 			ret = "WHITE";
 		}else if(s < 0.11 && v < 0.91 && v > 0.6){
 			ret = "SILVER";
 		}else if(s < 0.11 && v < 0.601 && v > 0.2){
 			ret = "GRAY";
 		}else if(s < 0.11 && v < 0.21){
 			ret = "BLACK";
 		}else if(s < 0.3 && v < 0.3 && v > 0.1){
 			ret = "GRAY";
 		}else if(s < 0.3 && v < 0.11){
 			ret = "BLACK";
 		}else if(s < 0.7 && v < 0.6){
 			ret = "BLACK";
 		}else if(v < 0.1){ // 0.05
 			ret = "BLACK";
 		}else if(s > 0.29 && s < 0.8 && v < 0.11){
 			ret = "GRAY";
 		}else if(s > 0.29 && s < 0.6 && v < 0.2){
 			ret = "GRAY";
 		//NEW COLORS
 		}else if(s > 0.6 && h > 0.5666666 && h < 0.602777 && v > 0.12 && v < 0.3){
 			ret = "BLUE";
 		}else if(h > 0.5 && h < 0.602777 && v < 0.13){
 			ret = "BLACK";	
 		}else if(h > 0.95833333 && s > 0.7 && v > 0.19 && v < 0.4){
 			ret = "RED";
 		}else if(h > 0.8 && h < 0.91666666 && s > 0.35 && v > 0.16 && v < 0.4){
 			ret = "PURPLE";
 		}else if(h > 0.3055555 && h < 0.3888888 && s < 0.35 && v > 0.6 && v < 0.8){
 			ret = "CYAN";
 		}else if(h > 0.38 && h < 0.5833333 && s < 0.35 && v > 0.7 && v < 0.95){
 			ret = "LIGHT_BLUE";
 		}else if(h > 0.38 && h < 0.5833333 && s < 0.35 && v > 0.5 && v < 0.71){
 			ret = "BLUE";
 		}else if(h > 0.5 && h < 0.61 && s > 0.2 && v > 0.7){
 			ret = "LIGHT_BLUE";
 		}else if(h > 0.5 && h < 0.61 && s > 0.2 && v < 0.71){
 			ret = "BLUE";
 		}else if(s < 0.31 && v < 0.16){
 			ret = "BLACK";
 		//NEW COLORS 2:
 		}else if(h > 0.32 && h < 0.501 && s > 0.99 && v < 0.12){
 			ret = "BLACK";
 		}else if(h > 0.53 && h < 0.7 && s > 0.5 && v < 0.3 && v > 0.15){
 			ret = "BLUE";
 		}else if(h > 0.4 && h < 0.53 && s > 0.5 && v < 0.3 && v > 0.15){
 			ret = "CYAN";
 		}else if(h < 0.4 && h > 0.2777777 && s > 0.5 && v < 0.3 && v > 0.15){
 			ret = "GREEN";
 		}else if(h < 0.25 && h > 0.2 && s > 0.6 && v < 0.25 && v > 0.15){
 			ret = "BROWN";
 		}else if(h > 833333 && h < 94 && s > 0.6 && v < 0.4 && v > 0.15){
 			ret = "PURPLE";
 		}else if(h > 0.47222222 && h < 0.541 && s < 0.4 && s > 0.2 && v > 0.8){
 			ret = "LIGHT_BLUE";
 		}else if(h > 0.541 && h < 0.64 && s < 0.4 && s > 0.2 && v > 0.3){
 			ret = "BLUE";
 		}else if(h > 0.47222222 && h < 0.541 && s < 0.4 && s > 0.2 && v < 0.5 && v > 0.2){
 			ret = "BLUE";
 		}else if(h > 0.32 && h < 0.501 && s > 0.99 && v < 0.12){
 			ret = "GRAY";
 		// NEW COLORS 3
 		}else if(h > 0.85 && s > 0.2 && s < 0.41 && v > 0.9){
 			ret = "PINK";
 		}else if(h > 0.763 && s > 0.2 && s < 0.41 && v > 0.5){
 			ret = "PURPLE";
 		}else if(h > 0.125 && h < 0.191666666 && s > 0.25 && s < 0.4 && b > 0.89){
 			ret = "YELLOW";
 		}else if(h > 0.125 && h < 0.191666666 && s > 0.25 && s < 0.4 && b < 0.81 && b > 0.3){
 			ret = "BROWN";
 		}else if(h > 0.222222 && h < 0.2777777777 && s > 0.2 && s > 0.4 && b > 0.85){
 			ret = "LIME";
 		}else if(h > 0.222222 && h < 0.2777777777 && s > 0.2 && s > 0.4 && b > 0.4 && b < 0.8){
 			ret = "GREEN";
 		}else if(s < 0.114 && b < 0.71 && b > 0.15){
 			ret = "GRAY";
 		}else{
 			ret = "WHITE"; // nothing matched
 			//getLogger().info(Float.toString(h) + " " + Float.toString(s) + " " + Float.toString(v));
 		}
 		
 		//getLogger().info(ret + " " + Float.toString(h) + " " + Float.toString(s) + " " + Float.toString(v));
 		
 		return ret;
 	}
 
 	
 	
 	int poscount;
 	int negcount;
 
 
 	public void colorTest(){
 		for(int r = 0; r <= 255; r++){
 			for(int g = 0; g <= 255; g++){
 				for(int b = 0; b <= 255; b++){
 					Color c = new Color(r, g, b);
 					if(getStringFromColorTEST(c)){
 						poscount += 1;
 					}else{
 						negcount += 1;
 					}
 				}
 			}
 		}
 		
 		getLogger().info("Colortest finished.");
 	}
 	
 	
 	
 	public void colorTestLog(){
 		File log = new File("colors.txt");
         if (!log.exists()){
         	try {
         		log.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
         }
 
 		for(int r = 0; r <= 255; r++){
 			for(int g = 0; g <= 255; g++){
 				for(int b = 0; b <= 255; b++){
 					Color c = new Color(r, g, b);
 					if(getStringFromColorTEST(c)){
 						poscount += 1;
 					}else{
 						negcount += 1;
 												
 						float[] hsb = new float[3];
 						c.RGBtoHSB(r, g, b, hsb);
 						
 						float h = hsb[0]; // HUE
 						float s = hsb[1]; // SATURATION
 						float v = hsb[2]; // BRIGHTNESS
 						
 						try {
 							java.io.PrintWriter pw = new PrintWriter(new FileWriter(log, true));
 							pw.write("[RGB]" + Integer.toString(c.getRed()) + "|" + Integer.toString(c.getGreen()) + "|" + Integer.toString(c.getBlue()) + "[HSB]" + Float.toString(h) + "|" + Float.toString(s) + "|" + Float.toString(v) + newline);
 							pw.close();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 		
 		getLogger().info("Colortest finished.");
 	}
 	
 	
 	public boolean getStringFromColorTEST(Color c){
 		boolean ret = false;
 
 		Integer r = c.getRed(); // RED
 		Integer g = c.getGreen(); // GREEN
 		Integer b = c.getBlue(); // BLUE
 		
 		float[] hsb = new float[3];
 		c.RGBtoHSB(r, g, b, hsb);
 		
 		float h = hsb[0]; // HUE
 		float s = hsb[1]; // SATURATION
 		float v = hsb[2]; // BRIGHTNESS
 		
 		if(s > 0.4 && v > 0.2 && h < 0.037){
 			ret = true;
 		}else if(s > 0.6 && v > 0.7 && h > 0.0333333333 && h < 0.1138888888){ // s > 0.4 && v > 0.5
 			ret = true;
 		}else if(s > 0.4 && s < 0.61 && v > 0.14 && v < 0.5 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 79 && v > 0.14 && v < 0.45 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.15 && s < 0.4 && v > 0.14 && v < 0.6 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.15 && s < 0.4 && v > 0.6 && v < 0.81 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.55 && v > 0.41 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.15 && s < 0.4 && v > 0.8 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.4 && s < 0.61 && v > 0.14 && v < 0.75 && h > 0.038 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.6 && s < 0.8 && v > 0.09 && v < 0.7 && h > 0.038 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.6 && s < 0.8 && v > 0.7 && h > 0.038 && h < 0.12){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.8 && v > 0.09 && v < 0.7 && h > 0.038 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.3 && v > 0.5 && v < 0.8 && h > 0.02 && h < 0.0333333333){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.3 && v < 0.5 && h > 0.02 && h < 0.0333333333){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.3 && v > 0.5 && v < 0.75 && h > 0.0333333333 && h < 0.1138888888){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.3 && v < 0.55 && h > 0.1138888888 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		}else if(s > 0.3 && v > 0.55 && v < 0.7 && h > 0.1138888888 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = true;
 		/*if(s > 0.4 && v > 0.2 && h < 0.03333333333){
 			ret = true;
 		}else if(s > 0.6 && v > 0.7 && h > 0.0333333333 && h < 0.1138888888){
 			ret = true;
 		*/	
 		}else if(s > 0.4 && v > 0.145 && h > 0.02 && h < 0.15){
 			ret = true;
 		}else if(s > 0.4 && v < 0.35 && v > 0.2 && h > 0.969){
 			ret = true;
 		}else if(s > 0.4 && v < 0.2 && v > 0.1 && h > 0.079999999 && h < 0.1222222){
 			ret = true;
 		}else if(s > 0.8 && v < 0.15 && v > 0.05 && h > 0.079999999 && h < 0.1222222){
 			ret = true;
 		}else if(s > 0.4 && v > 0.5 && h > 0.1138888888 && h < 0.1916666666){
 			ret = true;
 		}else if(s > 0.4 && v < 0.51 && v > 0.1 && h > 0.1138888888 && h < 0.1916666666){ // new
 			ret = true;
 		}else if(s > 0.4 && v > 0.2 && v < 0.81 && h > 0.1916666666 && h < 0.3805555555){
 			ret = true;
 		}else if(s > 0.4 && v > 0.5 && h > 0.1916666666 && h < 0.3805555555){
 			ret = true;
 		}else if(s > 0.2 && v > 0.75 && h > 0.1916666666 && h < 0.3805555555){
 			ret = true;
 		}else if(s > 0.2 && v > 0.8 && h > 0.3805555555 && h < 0.5194444444){ // v > 0.4 adjusted 3
 			ret = true;
 		}else if(s > 0.1 && s < 0.21 && v > 0.9 && h > 0.3805555555 && h < 0.5194444444){ // new 3
 			ret = true;
 		}else if(s > 0.4 && v < 0.81 && v > 0.2 && h > 0.3805555555 && h < 0.6027777777){ // adjusted 3
 			ret = true;
 		}else if(s > 0.4 && v > 0.2 && h > 0.5194444444 && h < 0.6027777777){
 			ret = true;
 		}else if(s > 0.4 && v > 0.4 && h > 0.6027777777 && h < 0.6944444444){
 			ret = true;
 		}else if(s > 0.2 && s < 0.41 && v > 0.7 && h > 0.6027777777 && h < 0.6944444444){ // adjusted 3
 			ret = true;
 		}else if(s < 0.2 && v > 0.6 && h > 0.6027777777 && h < 0.6944444444){ // new 3
 			ret = true;
 		}else if(s > 0.1 && s < 0.2 && v > 0.6 && v < 0.91 && h > 0.6027777777 && h < 0.6944444444){ // new 3
 			ret = true;
 		}else if(s > 0.1 && s < 0.2 && v > 0.9 && h > 0.6027777777 && h < 0.6944444444){ // new 3
 			ret = true;
 		}else if(s > 0.6 && v > 0.1 && h > 0.6027777777 && h < 0.6944444444){
 			ret = true;
 		}else if(s > 0.4 && v > 0.3 && h > 0.6944444444 && h < 0.8305555555){
 			ret = true;
 		}else if(s > 0.4 && v > 0.4 && h > 0.8305555555 && h < 0.8777777777){
 			ret = true;
 		}else if(s > 0.3 && v > 0.4 && h > 0.8777777777 && h < 0.9611111111){
 			ret = true;
 		}else if(s > 0.4 && v > 0.4 && h > 0.9361111111 && h < 1.0000000001){
 			ret = true;
 		}else if(s < 0.1 && v > 0.9){
 			ret = true;
 		}else if(s < 0.1 && v < 0.91 && v > 0.7){
 			ret = true;
 		}else if(s < 0.1 && v < 0.71 && v > 0.2){
 			ret = true;
 		}else if(s < 0.1 && v < 0.21){
 			ret = true;
 		}else if(s < 0.3 && v < 0.3 && v > 0.1){
 			ret = true;
 		}else if(s < 0.3 && v < 0.11){
 			ret = true;
 		}else if(s < 0.7 && v < 0.6){
 			ret = true;
 		}else if(v < 0.1){
 			ret = true;
 		}else if(s > 0.29 && s < 0.8 && v < 0.11){
 			ret = true;
 		}else if(s > 0.29 && s < 0.6 && v < 0.2){
 			ret = true;
 		}else if(h > 0.068 && h < 0.1194444 && s > 0.2 && s < 0.6 && v > 0.7){
 			ret = true; // HUMAN SKIN
 		}else if(h > 0.041 && h < 0.09 && s > 0.3 && s < 0.6 && v > 0.84){
 			ret = true; // HUMAN SKIN
 		}else if(h > 0.110 && h < 0.1389 && s < 0.6 && s > 0.3 && v > 0.74 && v < 0.91){
 			ret = true; // HUMAN SKIN
 		}else if(h < 0.09722 && h > 0.0333333 && s > 0.25 && s < 0.41 && v > 0.95){
 			ret = true; // HUMAN SKIN
 		//NEW COLORS [TEST]
 		}else if(s > 0.6 && h > 0.5666666 && h < 0.602777 && v > 0.12 && v < 0.3){
 			ret = true;
 		}else if(h > 0.5666666 && h < 0.602777 && v < 0.13){
 			ret = true;	
 		}else if(h > 0.95833333 && s > 0.7 && v > 0.19 && v < 0.4){
 			ret = true;
 		}else if(h > 0.8 && h < 0.91666666 && s > 0.35 && v > 0.16 && v < 0.4){
 			ret = true;
 		}else if(h > 0.3055555 && h < 0.3888888 && s < 0.35 && v > 0.6 && v < 0.8){
 			ret = true;
 		}else if(h > 0.38 && h < 0.5833333 && s < 0.35 && v > 0.7 && v < 0.95){
 			ret = true;
 		}else if(h > 0.38 && h < 0.5833333 && s < 0.35 && v > 0.5 && v < 0.71){
 			ret = true;
 		}else if(h > 0.5 && h < 0.61 && s > 0.2 && v > 0.7){
 			ret = true;
 		}else if(h > 0.5 && h < 0.61 && s > 0.2 && v < 0.71){
 			ret = true;
 		}else if(s < 0.31 && v < 0.16){
 			ret = true;
 		//NEW COLORS 2 [TEST]
 		}else if(h > 0.32 && h < 0.501 && s > 0.99 && v < 0.12){
 			ret = true;
 		}else if(h > 0.53 && h < 0.7 && s > 0.5 && v < 0.3 && v > 0.15){
 			ret = true;
 		}else if(h > 0.4 && h < 0.53 && s > 0.5 && v < 0.3 && v > 0.15){
 			ret = true;
 		}else if(h < 0.4 && h > 0.2777777 && s > 0.5 && v < 0.3 && v > 0.15){
 			ret = true;
 		}else if(h < 0.25 && h > 0.2 && s > 0.6 && v < 0.25 && v > 0.15){
 			ret = true;
 		}else if(h > 833333 && h < 94 && s > 0.6 && v < 0.4 && v > 0.15){
 			ret = true;
 		}else if(h > 0.47222222 && h < 0.541 && s < 0.4 && s > 0.2 && v > 0.8){
 			ret = true;
 		}else if(h > 0.541 && h < 0.64 && s < 0.4 && s > 0.2 && v > 0.3){
 			ret = true;
 		}else if(h > 0.47222222 && h < 0.541 && s < 0.4 && s > 0.2 && v < 0.5 && v > 0.2){
 			ret = true;
 		}else if(h > 0.32 && h < 0.501 && s > 0.99 && v < 0.12){
 			ret = true;
 		// NEW COLORS 3 [TEST]
 		}else if(h > 0.85 && s > 0.2 && s < 0.41 && v > 0.9){
 			ret = true;
 		}else if(h > 0.763 && s > 0.2 && s < 0.41 && v > 0.5){
 			ret = true;
 		}else if(h > 0.125 && h < 0.191666666 && s > 0.25 && s < 0.4 && b > 0.89){
 			ret = true;
 		}else if(h > 0.125 && h < 0.191666666 && s > 0.25 && s < 0.4 && b < 0.81 && b > 0.3){
 			ret = true;
 		}else if(h > 0.222222 && h < 0.2777777777 && s > 0.2 && s > 0.4 && b > 0.85){
 			ret = true;
 		}else if(h > 0.222222 && h < 0.2777777777 && s > 0.2 && s > 0.4 && b > 0.4 && b < 0.8){
 			ret = true;
 		}else{
 			ret = false; // nothing matched
 			//getLogger().info(Float.toString(h) + " " + Float.toString(s) + " " + Float.toString(v));
 		}
 		
 		return ret;
 	}
 	
 	
 	
 	public boolean isHumanSkin(Color c){
 		boolean ret = false;
 
 		Integer r = c.getRed(); // RED
 		Integer g = c.getGreen(); // GREEN
 		Integer b = c.getBlue(); // BLUE
 		
 		float[] hsb = new float[3];
 		c.RGBtoHSB(r, g, b, hsb);
 		
 		float h = hsb[0]; // HUE
 		float s = hsb[1]; // SATURATION
 		float v = hsb[2]; // BRIGHTNESS
 		
 		if(h > 0.068 && h < 0.1194444 && s > 0.2 && s < 0.6 && v > 0.7){ // h > 0.722222
 			ret = true;
 		}else if(h > 0.041 && h < 0.09 && s > 0.3 && s < 0.6 && v > 0.84){ // NEW
 			ret = true;
 		}else if(h > 0.110 && h < 0.1389 && s < 0.6 && s > 0.3 && v > 0.74 && v < 0.91){ // NEW 2
 			ret = true;
 		}else if(h < 0.09722 && h > 0.0333333 && s > 0.25 && s < 0.41 && v > 0.95){ // NEW 3
 			ret = true;
 		}else{
 			ret = false; // nothing matched
 			//getLogger().info(Float.toString(h) + " " + Float.toString(s) + " " + Float.toString(v));
 		}
 		
 		
 		//TODO: TEST, REMOVE LATER
 		if(h > 0.03 && h < 0.1 && s > 0.3 && s < 0.7 && v > 0.6){ // NEW
 			ret = true;
 		}	
 		
 		
 		return ret;
 	}
 	
 	
 	public void createPunchcard(){
 		for(int b = 0; b <= 255; b++){
 			// for each brightness step:
 			BufferedImage newb = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
 			for(int r = 0; r <= 255; r++){
 				for(int g = 0; g <= 255; g++){
 					Color c = new Color(r, g, b);
 					
 					if(getStringFromColorTEST(c)){
 						newb.setRGB(r, g, c.getRGB());
 					}else{
 						//newb.setRGB(r, g, Color.WHITE.getRGB());
 					}
 				}
 			}
 			
 		    File outputfile = new File("plugins/Skins/" + Integer.toString(b) + ".png");
 		    try {
 				ImageIO.write(newb, "png", outputfile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		getLogger().info("Finished creating punchcard.");
 		
 		
 		/*for(int r = 0; r <= 255; r++){
 			for(int g = 0; g <= 255; g++){
 				for(int b = 0; b <= 255; b++){
 					Color c = new Color(r, g, b);
 					
 					if(getStringFromColorTEST(c)){
 						
 					}else{
 						
 					}
 				}
 			}
 		}	
 
 		for(int h = 0; h < 360; h++){ // hue
 			for(int s = 0; s < 100; s++){ // saturation
 				for(int b = 0; b < 100; b++){ // brightness
 					Color c = Color.getHSBColor(h, s, b);
 					
 				}
 			}
 		}*/
 		
 	}
 	
 	
 	//TODO: CHANGE COLORS
 	public String getStringFromColorClay(Color c){
 		String ret = "";
 
 		Integer r = c.getRed(); // RED
 		Integer g = c.getGreen(); // GREEN
 		Integer b = c.getBlue(); // BLUE
 		
 		float[] hsb = new float[3];
 		c.RGBtoHSB(r, g, b, hsb);
 		
 		float h = hsb[0]; // HUE
 		float s = hsb[1]; // SATURATION
 		float v = hsb[2]; // BRIGHTNESS
 		
 		if(s > 0.4 && v > 0.2 && h < 0.037){
 			ret = "RED";
 		}else if(s > 0.6 && v > 0.7 && h > 0.0333333333 && h < 0.1138888888){ // s > 0.4 && v > 0.5
 			ret = "ORANGE";
 		}else if(s > 0.4 && s < 0.61 && v > 0.14 && v < 0.5 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 79 && v > 0.14 && v < 0.45 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.15 && s < 0.4 && v > 0.14 && v < 0.6 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.15 && s < 0.4 && v > 0.6 && v < 0.81 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "RED";
 		}else if(s > 0.55 && v > 0.41 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "RED";
 		}else if(s > 0.15 && s < 0.4 && v > 0.8 && h > 0.019 && h < 0.038){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "PINK";
 		}else if(s > 0.4 && s < 0.61 && v > 0.14 && v < 0.75 && h > 0.038 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.6 && s < 0.8 && v > 0.09 && v < 0.7 && h > 0.038 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.6 && s < 0.8 && v > 0.7 && h > 0.038 && h < 0.12){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "ORANGE";
 		}else if(s > 0.8 && v > 0.09 && v < 0.7 && h > 0.038 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.3 && v > 0.5 && v < 0.8 && h > 0.02 && h < 0.0333333333){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "RED";
 		}else if(s > 0.3 && v < 0.5 && h > 0.02 && h < 0.0333333333){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.3 && v > 0.5 && v < 0.75 && h > 0.0333333333 && h < 0.1138888888){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.3 && v < 0.55 && h > 0.1138888888 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.3 && v > 0.55 && v < 0.7 && h > 0.1138888888 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "GREEN";
 		}
 		/*if(s > 0.4 && v > 0.2 && h < 0.03333333333){
 			ret = "RED";
 		}else if(s > 0.6 && v > 0.7 && h > 0.0333333333 && h < 0.1138888888){ // s > 0.4 && v > 0.5
 			ret = "ORANGE";
 		}else if(s > 0.4 && v > 0.14 && h > 0.019 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.6 && v > 0.09 && h > 0.019 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}else if(s > 0.3 && v > 0.5 && h > 0.02 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BROWN";
 		}*/else if(s < 0.41 && v < 0.2 && h > 0.01 && h < 0.15){ // v < 0.5 // s < 0.801 // v > 0.2
 			ret = "BLACK";
 		}else if(s > 0.4 && v < 0.35 && v > 0.2 && h > 0.969){
 			ret = "BROWN";
 		}else if(s > 0.4 && v < 0.2 && v > 0.1 && h > 0.079999999 && h < 0.1222222){
 			ret = "BROWN";
 		}else if(s > 0.8 && v < 0.15 && v > 0.05 && h > 0.079999999 && h < 0.1222222){
 			ret = "BROWN";
 		}else if(s > 0.4 && v > 0.5 && h > 0.1138888888 && h < 0.1916666666){
 			ret = "YELLOW";
 		}else if(s > 0.4 && v < 0.51 && v > 0.1 && h > 0.1138888888 && h < 0.1916666666){ // new
 			ret = "BROWN";
 		}else if(s > 0.4 && v > 0.2 && v < 0.81 && h > 0.1916666666 && h < 0.3805555555){
 			ret = "GREEN";
 		}else if(s > 0.4 && v > 0.5 && h > 0.1916666666 && h < 0.3805555555){
 			ret = "LIME";
 		}else if(s > 0.2 && v > 0.75 && h > 0.1916666666 && h < 0.3805555555){
 			ret = "LIME";
 		}else if(s > 0.2 && v > 0.8 && h > 0.3805555555 && h < 0.5194444444){ // v > 0.4 adjusted 3
 			ret = "LIGHT_BLUE";
 		}else if(s > 0.1 && s < 0.21 && v > 0.9 && h > 0.3805555555 && h < 0.5194444444){ // new 3
 			ret = "LIGHT_BLUE";
 		}else if(s > 0.4 && v < 0.81 && v > 0.2 && h > 0.3805555555 && h < 0.6027777777){ // adjusted 3
 			ret = "CYAN";
 		}else if(s > 0.4 && v > 0.2 && h > 0.5194444444 && h < 0.6027777777){
 			ret = "CYAN";
 		}else if(s > 0.4 && v > 0.4 && h > 0.6027777777 && h < 0.6944444444){
 			ret = "BLUE";
 		}else if(s > 0.2 && s < 0.41 && v > 0.7 && h > 0.6027777777 && h < 0.6944444444){ // adjusted 3
 			ret = "LIGHT_BLUE";
 		}else if(s < 0.2 && v > 0.6 && h > 0.6027777777 && h < 0.6944444444){ // new 3
 			ret = "BLUE";
 		}else if(s > 0.1 && s < 0.2 && v > 0.6 && v < 0.91 && h > 0.6027777777 && h < 0.6944444444){ // new 3
 			ret = "LIGHT_BLUE";
 		}else if(s > 0.1 && s < 0.2 && v > 0.9 && h > 0.6027777777 && h < 0.6944444444){ // new 3
 			ret = "BLUE";
 		}else if(s > 0.6 && v > 0.1 && h > 0.6027777777 && h < 0.6944444444){
 			ret = "BLUE";
 		}else if(s > 0.4 && v > 0.3 && h > 0.6944444444 && h < 0.8305555555){
 			ret = "PURPLE";
 		}else if(s > 0.4 && v > 0.4 && h > 0.8305555555 && h < 0.8777777777){
 			ret = "MAGENTA";
 		}else if(s > 0.3 && v > 0.4 && h > 0.8777777777 && h < 0.9611111111){
 			ret = "PINK";
 		}else if(s > 0.4 && v > 0.4 && h > 0.9361111111 && h < 1.0000000001){
 			ret = "RED";
 		}else if(s < 0.1 && v > 0.9){
 			ret = "WHITE";
 		}else if(s < 0.1 && v < 0.91 && v > 0.7){
 			ret = "SILVER";
 		}else if(s < 0.1 && v < 0.71 && v > 0.2){
 			ret = "SILVER";
 		}else if(s < 0.1 && v < 0.21){
 			ret = "BLACK";
 		}else if(s < 0.3 && v < 0.3 && v > 0.1){
 			ret = "GRAY";
 		}else if(s < 0.3 && v < 0.11){
 			ret = "BLACK";
 		}else if(s < 0.7 && v < 0.6){
 			ret = "BLACK";
 		}else if(v < 0.1){ // 0.05
 			ret = "BLACK";
 		}else if(s > 0.29 && s < 0.8 && v < 0.11){
 			ret = "GRAY";
 		}else if(s > 0.29 && s < 0.6 && v < 0.2){
 			ret = "GRAY";
 		//NEW COLORS
 		}else if(s > 0.6 && h > 0.5666666 && h < 0.602777 && v > 0.12 && v < 0.3){
 			ret = "BLUE";
 		}else if(h > 0.5 && h < 0.602777 && v < 0.13){
 			ret = "BLACK";	
 		}else if(h > 0.95833333 && s > 0.7 && v > 0.19 && v < 0.4){
 			ret = "RED";
 		}else if(h > 0.8 && h < 0.91666666 && s > 0.35 && v > 0.16 && v < 0.4){
 			ret = "PURPLE";
 		}else if(h > 0.3055555 && h < 0.3888888 && s < 0.35 && v > 0.6 && v < 0.8){
 			ret = "CYAN";
 		}else if(h > 0.38 && h < 0.5833333 && s < 0.35 && v > 0.7 && v < 0.95){
 			ret = "LIGHT_BLUE";
 		}else if(h > 0.38 && h < 0.5833333 && s < 0.35 && v > 0.5 && v < 0.71){
 			ret = "BLUE";
 		}else if(h > 0.5 && h < 0.61 && s > 0.2 && v > 0.7){
 			ret = "LIGHT_BLUE";
 		}else if(h > 0.5 && h < 0.61 && s > 0.2 && v < 0.71){
 			ret = "BLUE";
 		}else if(s < 0.31 && v < 0.16){
 			ret = "BLACK";
 		//NEW COLORS 2:
 		}else if(h > 0.32 && h < 0.501 && s > 0.99 && v < 0.12){
 			ret = "BLACK";
 		}else if(h > 0.53 && h < 0.7 && s > 0.5 && v < 0.3 && v > 0.15){
 			ret = "BLUE";
 		}else if(h > 0.4 && h < 0.53 && s > 0.5 && v < 0.3 && v > 0.15){
 			ret = "CYAN";
 		}else if(h < 0.4 && h > 0.2777777 && s > 0.5 && v < 0.3 && v > 0.15){
 			ret = "GREEN";
 		}else if(h < 0.25 && h > 0.2 && s > 0.6 && v < 0.25 && v > 0.15){
 			ret = "BROWN";
 		}else if(h > 833333 && h < 94 && s > 0.6 && v < 0.4 && v > 0.15){
 			ret = "PURPLE";
 		}else if(h > 0.47222222 && h < 0.541 && s < 0.4 && s > 0.2 && v > 0.8){
 			ret = "LIGHT_BLUE";
 		}else if(h > 0.541 && h < 0.64 && s < 0.4 && s > 0.2 && v > 0.3){
 			ret = "BLUE";
 		}else if(h > 0.47222222 && h < 0.541 && s < 0.4 && s > 0.2 && v < 0.5 && v > 0.2){
 			ret = "BLUE";
 		}else if(h > 0.32 && h < 0.501 && s > 0.99 && v < 0.12){
 			ret = "GRAY";
 		// NEW COLORS 3
 		}else if(h > 0.85 && s > 0.2 && s < 0.41 && v > 0.9){
 			ret = "PINK";
 		}else if(h > 0.763 && s > 0.2 && s < 0.41 && v > 0.5){
 			ret = "PURPLE";
 		}else if(h > 0.125 && h < 0.191666666 && s > 0.25 && s < 0.4 && b > 0.89){
 			ret = "YELLOW";
 		}else if(h > 0.125 && h < 0.191666666 && s > 0.25 && s < 0.4 && b < 0.81 && b > 0.3){
 			ret = "BROWN";
 		}else if(h > 0.222222 && h < 0.2777777777 && s > 0.2 && s > 0.4 && b > 0.85){
 			ret = "LIME";
 		}else if(h > 0.222222 && h < 0.2777777777 && s > 0.2 && s > 0.4 && b > 0.4 && b < 0.8){
 			ret = "GREEN";
 		}else if(s < 0.11 && b > 0.9){
 			ret = "WHITE";
 		}else if(s < 0.11 && b < 0.91 && b > 0.7){
 			ret = "SILVER";
 		}else if(s < 0.11 && b < 0.71 && b > 0.15){
 			ret = "GRAY";
 		}else{
 			ret = "WHITE"; // nothing matched
 			//getLogger().info(Float.toString(h) + " " + Float.toString(s) + " " + Float.toString(v));
 		}
 		
 		return ret;
 	}
 
 	
 	public String getDirection(Float yaw)
 	{
 	    yaw = yaw / 90;
 	    yaw = (float)Math.round(yaw);
 	 
 	    if (yaw == -4 || yaw == 0 || yaw == 4) {return "SOUTH";}
 	    if (yaw == -1 || yaw == 3) {return "EAST";}
 	    if (yaw == -2 || yaw == 2) {return "NORTH";}
 	    if (yaw == -3 || yaw == 1) {return "WEST";}
 	    return "";
 	}
 	
 	
 	
 	
 	
 	//TODO REMOVE
 	
 	// Experimental:
 	
 	
 	private static int[] STONE = new int[] { 145, 145, 145, 0, 1, 0 };
 	private static int[] WOOL_GREY = new int[] { 99, 99, 99, 1, 35, 7 };
 	private static int[] WOOL_L_GREY = new int[] { 145, 145, 145, 2, 35, 8 };
 	private static int[] WOOL_BLACK = new int[] { 0, 0, 0, 3, 35, 15 };
 	private static int[] WOOL_ORANGE = new int[] { 255, 128, 0, 4, 35, 1 };
 	private static int[] WOOL_MAGENTA = new int[] { 255, 0, 255, 5, 35, 2 };
 	private static int[] WOOL_L_BLUE = new int[] { 0, 155, 255, 6, 35, 3};
 	private static int[] WOOL_YELLOW = new int[] { 175, 175, 0, 7, 35, 4 };
 	private static int[] WOOL_LIME = new int[] { 75, 255, 0, 8, 35, 5 };
 	private static int[] WOOL_PINK = new int[] { 255, 125, 180, 9, 35, 6 };
 	private static int[] WOOL_CYAN = new int[] { 0, 150, 255, 10, 35, 9 };
 	private static int[] WOOL_PURPLE = new int[] { 150, 0, 150, 11, 35, 10 };
 	private static int[] WOOL_BLUE = new int[] { 0, 0, 255, 12, 35, 11 };
 	private static int[] WOOL_BROWN = new int[] { 130, 75, 0, 13, 35, 12 };
 	private static int[] WOOL_GREEN = new int[] { 0, 255, 0, 14, 35, 13 };
 	private static int[] WOOL_RED = new int[] { 255, 0, 0, 15, 35, 14 };
 	private static int[] WOOL_WHITE = new int[] { 180, 180, 180, 16, 35, 0 };
 	private static int[] SANDSTONE = new int[] { 255, 175, 135, 17, 24, 0 };
 	private static int[] IRON = new int[] { 215, 215, 215, 18, 42, 0 };
 	private static int[] DIAMOND = new int[] { 0, 255, 255, 19, 57, 0 };
 	private static int[] GOLD = new int[] { 255, 255, 0, 20, 41, 0 };
 	private static int[] SNOW = new int[] { 255, 255, 255, 21, 80, 0 };
 	private static int[] GRAVEL = new int[] { 140, 135, 155, 22, 13, 0 };
 	private static int[] TWO_HALFSLABS = new int[] { 159, 159, 159, 23, 43, 0 };
 	private static int[] SNOW_BLOCK = new int[] { 239, 251, 251, 24, 80, 0 };
 	private static int[] NETHER_RACK = new int[] { 110, 53, 51, 25, 87, 0 };
 	private static int[] SOUL_SAND = new int[] { 84, 64, 51, 26, 88, 0 };
 	private static int[] OBSIDIAN = new int[] { 20, 18, 29, 27, 49, 0 };
 	
 	public static int[][] materials = {
 		 STONE, WOOL_GREY, WOOL_L_GREY, WOOL_BLACK, WOOL_ORANGE, WOOL_MAGENTA, WOOL_L_BLUE, WOOL_YELLOW, WOOL_LIME,
 		 WOOL_PINK, WOOL_CYAN, WOOL_PURPLE, WOOL_BLUE, WOOL_BROWN, WOOL_GREEN, WOOL_RED, WOOL_WHITE,
 		 SANDSTONE, IRON, DIAMOND, GOLD, SNOW, GRAVEL, TWO_HALFSLABS, SNOW_BLOCK, NETHER_RACK, SOUL_SAND, OBSIDIAN
 	};
 
 	public static double colorDistance(Color c1, Color c2)
 	{
 		long rm = (c1.getRed() + c2.getRed()) / 2L;
 	    long r = c1.getRed() - c2.getRed();
 	    long g = c1.getGreen() - c2.getGreen();
 	    long b = c1.getBlue() - c2.getBlue();
 	    return Math.sqrt(((512L + rm) * r * r >> 8) + 4L * g * g + ((767L - rm) * b * b >> 8));
 	}
 
 	public static int[] getMaterialFromColor(Color col) {
 		int closestId = 0;
 
 		int closestBlock = 35;
 		int closestData = 0;
 
 		double d = colorDistance(col, new Color(255, 255, 255));
 
 		for (int i = 0; i < materials.length; i++) {
 			double currentdist = colorDistance(col, new Color(materials[i][0], materials[i][1], materials[i][2]));
 
 			if (currentdist < d) {
 				closestId = i;
 				d = currentdist;
 			}
 		}
 
 		return new int[] { materials[closestId][4], materials[closestId][5] };
 	}
 	
 	
 	
 	public boolean canBuild(Player p, String direction){
 		if(!getConfig().getBoolean("config.use_worldguard")){
 			return true;
 		}
 		
 		if(direction.equalsIgnoreCase("east")){
 			ArrayList<Location> locs = new ArrayList<Location>(Arrays.asList(
 				p.getLocation().getBlock().getRelative(-1, 0, -4).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(-1, 0, +11).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(+8, 0, +11).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(+8, 0, -4).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(-1, +33, -4).getLocation(), // up
 				p.getLocation().getBlock().getRelative(-1, +33, +11).getLocation(), // up
 				p.getLocation().getBlock().getRelative(+8, +33, +11).getLocation(), // up
 				p.getLocation().getBlock().getRelative(+8, +33, -4).getLocation() // up
 			));
 			for(Location l : locs){
 				if(!getWorldGuard().canBuild(p, l)){
 					p.sendMessage("cYou don't have permission to build a skin here!");
 					return false;
 				}
 			}
 		}else if(direction.equalsIgnoreCase("south")){
 			ArrayList<Location> locs = new ArrayList<Location>(Arrays.asList(
 				p.getLocation().getBlock().getRelative(+4, 0, -1).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(-11, 0, -1).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(-11, 0, +8).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(+4, 0, +8).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(+4, +33, -1).getLocation(), // up
 				p.getLocation().getBlock().getRelative(-11, +33, -1).getLocation(), // up
 				p.getLocation().getBlock().getRelative(-11, +33, +8).getLocation(), // up
 				p.getLocation().getBlock().getRelative(+4, +33, +8).getLocation() // up
 			));
 			for(Location l : locs){
 				if(!getWorldGuard().canBuild(p, l)){
 					p.sendMessage("cYou don't have permission to build a skin here!");
 					return false;
 				}
 			}
 		}else if(direction.equalsIgnoreCase("north")){
 			ArrayList<Location> locs = new ArrayList<Location>(Arrays.asList(
 				p.getLocation().getBlock().getRelative(-4, 0, +1).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(+11, 0, +1).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(+11, 0, -8).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(-4, 0, -8).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(-4, +33, +1).getLocation(), // up
 				p.getLocation().getBlock().getRelative(+11, +33, +1).getLocation(), // up
 				p.getLocation().getBlock().getRelative(+11, +33, -8).getLocation(), // up
 				p.getLocation().getBlock().getRelative(-4, +33, -8).getLocation() // up
 			));
 			for(Location l : locs){
 				if(!getWorldGuard().canBuild(p, l)){
 					p.sendMessage("cYou don't have permission to build a skin here!");
 					return false;
 				}
 			}
 		}else if(direction.equalsIgnoreCase("west")){
 			ArrayList<Location> locs = new ArrayList<Location>(Arrays.asList(
 				p.getLocation().getBlock().getRelative(+1, 0, +4).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(+1, 0, -11).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(-8, 0, -11).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(-8, 0, +4).getLocation(), // bottom
 				p.getLocation().getBlock().getRelative(+1, +33, +4).getLocation(), // up
 				p.getLocation().getBlock().getRelative(+1, +33, -11).getLocation(), // up
 				p.getLocation().getBlock().getRelative(-8, +33, -11).getLocation(), // up
 				p.getLocation().getBlock().getRelative(-8, +33, +4).getLocation() // up
 			));
 			for(Location l : locs){
 				if(!getWorldGuard().canBuild(p, l)){
 					p.sendMessage("cYou don't have permission to build a skin here!");
 					return false;
 				}
 			}
 		}
 
 
 		return true;
 	}
 	
 }
