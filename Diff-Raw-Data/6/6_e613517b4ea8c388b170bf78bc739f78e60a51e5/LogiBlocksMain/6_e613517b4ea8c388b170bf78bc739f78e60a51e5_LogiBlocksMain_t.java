 package plugin.bleachisback.LogiBlocks;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 import net.minecraft.server.v1_6_R2.EntityPlayer;
 import net.minecraft.server.v1_6_R2.Packet39AttachEntity;
 
 import org.bukkit.Art;
 import org.bukkit.Bukkit;
 import org.bukkit.Color;
 import org.bukkit.DyeColor;
 import org.bukkit.FireworkEffect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.Painting;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.entity.Villager;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import plugin.bleachisback.LogiBlocks.Commands.BaseCommandListener;
 import plugin.bleachisback.LogiBlocks.Commands.ForCommandListener;
 import plugin.bleachisback.LogiBlocks.Commands.LogiCommandListener;
 import plugin.bleachisback.LogiBlocks.Listeners.LogiBlocksCraftListener;
 import plugin.bleachisback.LogiBlocks.Listeners.LogiBlocksInteractListener;
 import plugin.bleachisback.LogiBlocks.Listeners.LogiBlocksRedstoneListener;
 import plugin.bleachisback.LogiBlocks.Listeners.LogiFlagListener;
 
 public class LogiBlocksMain extends JavaPlugin
 {
 	protected Logger log;
 	private Server server;
 	private PluginDescriptionFile desc;
 	private PluginManager pm;
 	
 	protected Configuration config;
 	
 	protected FileConfiguration flagConfig;
 	protected File flagFile;
 	protected HashMap<String, FlagListener> flags = new HashMap<String, FlagListener>();	
 	
 	private Entity lastRedstone = null;
 	
 	private LogiFlagListener flagListener;
 	
 	private FileConfiguration listenerConfig;
 	private File listenerFile;
 	@SuppressWarnings("unused")
 	private HashMap<String, Block> listeners = new HashMap<String, Block>();
 			
 	public void onEnable()
 	{
 		server=getServer();
 		log=getLogger();
 		desc=getDescription();
 		pm=server.getPluginManager();
 		
 		flagFile=new File(getDataFolder(), "flags");		
 		flagConfig=YamlConfiguration.loadConfiguration(flagFile);
 		convertOldFlags();
 		
 		saveDefaultConfig();
 		config=getConfig();
 		updateConfig();
 		
 		getCommand("command").setExecutor(new BaseCommandListener(this));
 		getCommand("logicif").setExecutor(new LogiCommandListener(this));
 		getCommand("logicfor").setExecutor(new ForCommandListener());
 		
 		pm.registerEvents(new FilterCommandListener(this),this);
 		
 		if(config.getBoolean("allow-crafting", true))
 		{
 			pm.registerEvents(new LogiBlocksCraftListener(this), this);
 			setupRecipe();
 		}
 		if(config.getBoolean("allow-command-insertion",true))
 		{
 			pm.registerEvents(new LogiBlocksInteractListener(this), this);
 			setupPermissions();
 		}
 		if(config.getBoolean("listen-for-redstone",true))
 		{
 			pm.registerEvents(new LogiBlocksRedstoneListener(this), this);
 		}
 		
 		flagListener=new LogiFlagListener(this);
 		registerFlag("getFlag",flagListener);
 		registerFlag("isFlag",flagListener);
 		registerFlag("getGlobalFlag",flagListener);
 		registerFlag("isGlobalFlag",flagListener);
 		registerFlag("hasequip",flagListener);
 		registerFlag("inventory",flagListener);
 		registerFlag("exists",flagListener);
 		registerFlag("hasPassenger",flagListener);
 		registerFlag("isPassenger",flagListener);		
 		registerFlag("random",flagListener);
 		
		//listenerFile = new File(getDataFolder(), "listeners");		
		//listenerConfig = YamlConfiguration.loadConfiguration(listenerFile);
		//loadListeners();
 		
 		
 		log.info(desc.getFullName()+" is enabled");
 	}
 	
 	public void onDisable()
 	{
 		trace(desc.getName()+" is disabled");
 	}
 	
 	private void trace(String string)
 	{
 		log.info(string);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		switch(cmd.getName())
 		{
 			case "phantom":
 				if(sender.hasPermission("c.hidden.phantom")&&args.length>1)
 				{
 					server.getPlayer(args[0]).remove();
 					return true;
 				}
 				break;				
 		}
 		return false;
 	}
 	
 	private void loadListeners()
 	{
 		@SuppressWarnings("unchecked")
 		List<String[]> locationList = (List<String[]>) listenerConfig.getList("");
 		for(String[] locArray : locationList)
 		{
 			try
 			{
 				@SuppressWarnings("unused")
 				Location location = new Location(Bukkit.getWorld(UUID.fromString(locArray[0])), Double.parseDouble(locArray[1]), Double.parseDouble(locArray[2]), Double.parseDouble(locArray[3]));
 			}
 			catch(NumberFormatException e)
 			{
 				continue;
 			}
 		}
 	}
 	
 	//Adds the configurable recipe for command blocks
 	private char[][] getchar={{'a','b','c'},{'d','e','f'},{'g','h','i'}};
 	private void setupRecipe()
 	{
 		Material[][] mats=new Material[3][3];
 		HashMap<Material,Character> chars=new HashMap<Material,Character>();
 		for(int i=0;i<3;i++)
 		{
 			String[] units=config.getStringList("crafting-recipe").get(i).replace(" ","").split(",");
 			for(int j=0;j<3;j++)
 			{
 				Material material=Material.matchMaterial(units[j]);
 				if(material==null)
 				{
 					material=Material.getMaterial(Integer.parseInt(units[j]));
 				}
 				mats[i][j]=material;
 				if(!chars.containsKey(material))
 				{
 					chars.put(material,getchar[i][j]);
 				}
 			}
 		}		
 		ItemStack result=new ItemStack(Material.COMMAND,1);
 		ShapedRecipe recipe=new ShapedRecipe(result);
 		String[] shape=new String[3];
 		for(int i=0;i<3;i++)
 		{
 			shape[i]="";
 			for(int j=0;j<3;j++)
 			{
 				Material material=mats[i][j];
 				shape[i]=shape[i]+chars.get(material).toString();
 			}
 		}
 		recipe.shape(shape);
 		for(int i=0;i<3;i++)
 		{
 			for(int j=0;j<3;j++)
 			{
 				Material material=mats[i][j];
 				if(material!=Material.AIR)
 				{
 					recipe.setIngredient(chars.get(material), material);
 				}				
 			}
 		}		
 		server.addRecipe(recipe);
 	}
 	
 	private void setupPermissions()
 	{
 		for(String perm:config.getConfigurationSection("permissions").getKeys(false))
 		{
 			pm.addPermission(new Permission("c.permission."+perm,PermissionDefault.OP));
 		}
 	}
 	
 	//For people switching from a version of LogiBlocks before a certain date, to a newer version
 	//Converts old flag files to new ones
 	//All old flags are now global flags
 	private void convertOldFlags()
 	{		
 		for(String name:flagConfig.getKeys(false))
 		{
 			if(!name.equals("global")&&!name.equals("local"))
 			{
 				flagConfig.set("global."+name, flagConfig.getBoolean(name));
 				flagConfig.set(name, null);
 				try 
 				{
 					flagConfig.save(flagFile);
 				} 
 				catch (IOException e) 
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	//For people switching from an older plugin version or trying to refresh their config file
 	//This will automatically replace any values that don't exist in the current config file with their default value
 	private void updateConfig()
 	{
 		Map<String,Object> map=config.getDefaults().getValues(true);
 		for(String key:map.keySet())
 		{
 			if(config.get(key,null)==null)
 			{
 				config.set(key, map.get(key));				
 			}
 		}		
 		saveConfig();
 	}
 	
 	//Searches for specific syntax in commands
 	//Syntax being filtered so far:
 	//&& - Used to run several commands in one line
 	//@e - Similar to @p, but for entities
 	//@lr - Similar to @p and @e, except it returns the last entity/player that activated redstone
 	public boolean filter(String[] args, CommandSender sender, Command command, Location loc)
 	{
 		for(int currentArg=0;currentArg<args.length;currentArg++)
 		{
 			String string=args[currentArg];
 			if(string.equals("&&"))
 			{	
 				String[] originalCommand=Arrays.copyOfRange(args,0,currentArg);
 				String commandLine=command.getName()+" ";
 				for(String commandPart:originalCommand)
 				{
 					commandLine=commandLine+commandPart+" ";
 				}
 				Bukkit.dispatchCommand(sender, commandLine);
 				
 				String[] newCommand=Arrays.copyOfRange(args,currentArg+1,args.length);
 				String commandLine2="";
 				for(String commandPart:newCommand)
 				{
 					commandLine2=commandLine2+commandPart+" ";
 				}
 				Bukkit.dispatchCommand(sender, commandLine2);
 				return false;
 			}
 			else if(string.equals("\\&&"))
 			{
 				args[currentArg]="&&";
 			}
 			else if(string.startsWith("@"))
 			{
 				switch(string.substring(1,string.contains("[")?string.indexOf("["):string.length()))
 				{
 					case "e":
 						World world=loc.getWorld();
 						//search properties
 						EntityType type=null;
 						double x=loc.getX();
 						double y=loc.getY();
 						double z=loc.getZ();
 						int r=0;
 						int rm=0;
 						int c=1;
 						boolean rand=false;
 						
 						if(string.contains("[")&&string.contains("]"))
 						{
 							if(!string.contains("="))
 							{
 								return true;
 							}
 							for(String args1:string.substring(string.indexOf("[")+1, string.indexOf("]")).split(","))
 							{
 								if(args1.length()<3||!args1.contains("="))
 								{
 									continue;
 								}
 								if(args1.length()<=args1.indexOf("="))
 								{
 									continue;
 								}
 								try
 								{
 									switch(args1.substring(0,args1.indexOf("=")))
 									{
 										case "t":
 										type=EntityType.fromName(args1.substring(2,args1.length()));
 										if(type==null)
 										{
 											type=entFromAlias(args1.substring(2,args1.length()));
 										}
 										break;
 										case "x":
 											x=Double.parseDouble(args1.substring(2,args1.length()));
 											break;
 										case "y":
 											y=Double.parseDouble(args1.substring(2,args1.length()));
 											break;
 										case "z":
 											z=Double.parseDouble(args1.substring(2,args1.length()));
 											break;
 										case "loc":
 											String[] locArray=args1.substring(2,args1.length()).split("\\|");
 											switch(locArray.length)
 											{
 											default:
 											case 3:
 												z=Double.parseDouble(locArray[2]);
 											case 2:
 												y=Double.parseDouble(locArray[1]);
 											case 1:
 												x=Double.parseDouble(locArray[0]);
 												break;
 											case 0:
 												break;
 											}
 											break;
 										case "r":
 											r=Integer.parseInt(args1.substring(2,args1.length()));
 											break;
 										case "rm":
 											rm=Integer.parseInt(args1.substring(3,args1.length()));
 											break;
 										case "c":
 											c=Integer.parseInt(args1.substring(2,args1.length()));
 											break;
 										case "rand":
 											rand=Boolean.parseBoolean(args1.substring(5,args1.length()));
 											break;
 									}
 								}
 								catch(NumberFormatException e)
 								{}
 							}
 						}
 
 						List<Entity> nearbyEntities=loc.getWorld().getEntities();
 						
 						for(int i=0;i<nearbyEntities.size();i++)
 						{
 							if(i<0)
 							{
 								continue;
 							}
 							Entity entity=nearbyEntities.get(i);
 							if((entity.getLocation().distance(new Location(world,x,y,z))>r&&r>0)
 									||(entity.getLocation().distance(new Location(world,x,y,z))<rm))
 							{																
 								nearbyEntities.remove(entity);
 								i--;
 							}
 							if(type!=null)
 							{
 								if(entity.getType()!=type)
 								{
 									nearbyEntities.remove(entity);
 									i--;
 								}
 							}
 						}
 						if(nearbyEntities.size()==0)
 						{
 							return false;
 						}
 						
 						if(c<1)
 						{
 							c=nearbyEntities.size();
 						}
 						
 						Entity[] entities=new Entity[c];
 
 						if(rand)
 						{
 							for(int i=0;i<c;i++)
 							{
 								Entity entity=nearbyEntities.get(new Random().nextInt(nearbyEntities.size()));
 								entities[i]=entity;
 								nearbyEntities.remove(entity);
 							}
 						}
 						else
 						{
 							for(int i=0;i<c;i++)
 							{
 								double distance=nearbyEntities.get(0).getLocation().distance(loc);
 								Entity entity=nearbyEntities.get(0);
 								for(Entity e:nearbyEntities)
 								{
 									if(loc.distance(e.getLocation())<distance)
 									{
 										distance=loc.distance(e.getLocation());
 										entity=e;
 									}
 								}
 								entities[i]=entity;
 								nearbyEntities.remove(entity);
 							}
 						}
 						String entid="";
 						if(c==1)
 						{
 							if(entities[0] instanceof Player)
 							{
 								args[currentArg]=((Player)entities[0]).getName();
 							}
 							else
 							{
 								entid=""+entities[0].getEntityId();
 								args[currentArg]="@e["+entid+"]";
 							}							
 						}
 						else
 						{
 							String[] newArgs=args;
 							for(int i=0;i<newArgs.length;i++)
 							{
 								if(newArgs[i].equals("&&"))
 								{
 									newArgs[i]="\\&&";
 								}
 							}
 							for(int i=0;i<c;i++)
 							{
 								String newArg="";
 								if(entities[i] instanceof Player)
 								{
 									newArg=((Player)entities[0]).getName();
 								}
 								else
 								{
 									newArg="@e["+entities[i].getEntityId()+"]";
 								}
 								for(int j=currentArg+1;j<args.length;j++)
 								{
 									if(args[currentArg].equals(args[j]))
 									{
 										newArgs[j]=newArg;
 									}
 								}
 								newArgs[currentArg]=newArg;								
 								command.execute(sender, command.getLabel(), newArgs);								
 							}
 							return false;
 						}
 						return true;
 						//End @e
 					case "lr":
 						if(lastRedstone==null)
 						{
 							return false;
 						}
 						else if(lastRedstone instanceof Player)
 						{
 							args[currentArg]=((Player)lastRedstone).getName();
 						}
 						else
 						{
 							args[currentArg]="@e["+lastRedstone.getEntityId()+"]";
 						}						
 						return true;
 						//end @r
 				}
 			}
 		}
 		return true;
 	}
 	
 	public void setLastRedstone(Entity ent)
 	{
 		lastRedstone=ent;
 	}
 	
 	public Entity getLastRedstone()
 	{
 		return lastRedstone;
 	}
 	
 	public FileConfiguration getFlagConfig()
 	{
 		return flagConfig;
 	}
 	
 	public File getFlagFile()
 	{
 		return flagFile;
 	}
 	
 	public HashMap<String,FlagListener> getFlagListeners()
 	{
 		return flags;
 	}
 	
 	//Controls what color the next sheep spawned with the "rainbow" color argument will be
 	private int currentRainbow=0;
 	
 	//Allows plugins to change an entity's data, based on user input
 	//Used by the spawn and setdata sub-commands
 	@SuppressWarnings("incomplete-switch")
 	public void handleData(Entity ent, String data)
 	{
 		if(data==null||ent==null)
 		{
 			return;
 		}
 		for(String dataPiece:data.split(";"))
 		{
 			if(ent instanceof LivingEntity&&(dataPiece.startsWith("n=")||dataPiece.startsWith("name=")))
 			{
 				((LivingEntity)ent).setCustomName(dataPiece.substring(dataPiece.indexOf("=")+1,dataPiece.length()));
 				continue;				
 			}
 			switch(ent.getType())
 			{
 				case CREEPER:
 					((Creeper)ent).setPowered(dataPiece.equalsIgnoreCase("powered")||dataPiece.equalsIgnoreCase("charged"));
 					break;
 				case ENDERMAN:
 					Material enderMat=null;
 					try
 					{
 						enderMat=Material.getMaterial(Integer.parseInt(dataPiece));
 					}
 					catch(NumberFormatException e)
 					{
 						enderMat=Material.matchMaterial(dataPiece);
 					}
 					if(enderMat==null)
 					{
 						return;
 					}
 					((Enderman)ent).setCarriedMaterial(enderMat.getNewData((byte)0));
 					break;
 				case EXPERIENCE_ORB:
 					try
 					{
 						((ExperienceOrb)ent).setExperience(Integer.parseInt(dataPiece));
 					}
 					catch(NumberFormatException e)
 					{}
 					break;
 				case FIREWORK:
 					Firework firework=(Firework)ent;
 					FireworkMeta meta=firework.getFireworkMeta();
 					if(dataPiece.contains("="))
 					{
 						switch(dataPiece.substring(0,dataPiece.indexOf("=")))
 						{
 							case "p":
 							case "power":
 								try
 								{
 									meta.setPower(Integer.parseInt(dataPiece.substring(dataPiece.indexOf("=")+1, dataPiece.length())));
 								}
 								catch(NumberFormatException e)
 								{}
 								break;
 							case "e":
 							case "effect":
 								String[] effect=dataPiece.substring(dataPiece.indexOf("=")+1, dataPiece.length()).split("\\|");
 								FireworkEffect.Builder builder=FireworkEffect.builder();
 								boolean color=false;
 								for(String effectPiece:effect)
 								{
 									if(effectPiece.equalsIgnoreCase("flicker"))
 									{
 										builder.withFlicker();
 									}
 									else if(effectPiece.equalsIgnoreCase("trail"))
 									{
 										builder.withTrail();
 									}
 									else if(effectPiece.contains("="))
 									{
 										switch(effectPiece.substring(0,effectPiece.indexOf("=")))
 										{
 											case "e":
 											case "effect":
 												try
 												{
 													builder.with(FireworkEffect.Type.valueOf(effectPiece.substring(effectPiece.indexOf("=")+1,effectPiece.length()).toUpperCase()));
 												}
 												catch(IllegalArgumentException _e)
 												{}
 												break;
 											case "c":
 											case "color":
 												try
 												{
 													builder.withColor(Color.fromRGB(Integer.parseInt(effectPiece.substring(effectPiece.indexOf("=")+1,effectPiece.length()), 16)));
 													color=true;
 												}
 												catch(NumberFormatException e)
 												{}
 												break;
 											case "f":
 											case "fade":
 												try
 												{
 													builder.withFade(Color.fromRGB(Integer.parseInt(effectPiece.substring(effectPiece.indexOf("=")+1,effectPiece.length()), 16)));
 												}
 												catch(NumberFormatException e)
 												{}
 												break;
 										}
 									}
 								}
 								if(!color)
 								{
 									builder.withColor(Color.WHITE);
 								}
 								meta.addEffect(builder.build());
 								break;
 						}
 					}
 					firework.setFireworkMeta(meta);
 					break;
 				case ITEM_FRAME:
 					Material frameMat=null;
 					try
 					{
 						frameMat=Material.getMaterial(Integer.parseInt(dataPiece));
 					}
 					catch(NumberFormatException e)
 					{
 						frameMat=Material.matchMaterial(dataPiece);
 					}
 					if(frameMat==null)
 					{
 						return;
 					}
 					((ItemFrame)ent).setItem(new ItemStack(frameMat));
 					break;
 				case MAGMA_CUBE:
 					try
 					{
 						((Slime)ent).setSize(Integer.parseInt(dataPiece));
 					}
 					catch(NumberFormatException e)
 					{}
 					break;
 				case OCELOT:
 					Ocelot.Type ocelotType=null;
 					try
 					{
 						ocelotType=Ocelot.Type.getType(Integer.parseInt(dataPiece));
 					}
 					catch(NumberFormatException e)
 					{
 						try
 						{
 							ocelotType=Ocelot.Type.valueOf(dataPiece.toUpperCase());
 						}
 						catch(IllegalArgumentException _e)
 						{}
 						
 					}
 					if(ocelotType==null)
 					{
 						return;
 					}
 					((Ocelot)ent).setCatType(ocelotType);
 					break;
 				case PAINTING:
 					Art paintType=null;
 					try
 					{
 						paintType=Art.getById(Integer.parseInt(dataPiece));
 					}
 					catch(NumberFormatException e)
 					{
 						paintType=Art.getByName(dataPiece);
 					}
 					if(paintType==null)
 					{
 						return;
 					}
 					((Painting)ent).setArt(paintType);
 					break;
 				case PIG:
 					((Pig)ent).setSaddle(dataPiece.equalsIgnoreCase("saddle")||dataPiece.equalsIgnoreCase("saddled"));
 					break;
 				case PRIMED_TNT:
 					try
 					{
 						((TNTPrimed)ent).setFuseTicks(Integer.parseInt(dataPiece));
 					}
 					catch(NumberFormatException e)
 					{}
 					break;
 				case SHEEP:
 					((Sheep)ent).setSheared((dataPiece.equalsIgnoreCase("sheared")||dataPiece.equalsIgnoreCase("naked"))&&!((Sheep)ent).isSheared());					
 					try
 					{
 						((Sheep)ent).setColor(DyeColor.valueOf(dataPiece.toUpperCase()));
 					}	
 					catch(IllegalArgumentException e)
 					{						
 						if(dataPiece.equalsIgnoreCase("rainbow"))
 						{
 							((Sheep)ent).setColor(DyeColor.values()[currentRainbow]);
 							currentRainbow++;
 							if(currentRainbow>=DyeColor.values().length)
 							{
 								currentRainbow=0;
 							}
 						}
 					}
 					break;
 				case SKELETON:
 					((Skeleton)ent).setSkeletonType(dataPiece.equalsIgnoreCase("wither")?Skeleton.SkeletonType.WITHER:Skeleton.SkeletonType.NORMAL);
 					break;
 				case SLIME:
 					try
 					{
 						((Slime)ent).setSize(Integer.parseInt(dataPiece));
 					}
 					catch(NumberFormatException e)
 					{}
 					break;
 				case VILLAGER:
 					Villager.Profession villagerProfession=null;
 					try
 					{
 						villagerProfession=Villager.Profession.getProfession(Integer.parseInt(dataPiece));
 					}
 					catch(NumberFormatException e)
 					{
 						try
 						{
 							villagerProfession=Villager.Profession.valueOf(dataPiece.toUpperCase());
 						}
 						catch(IllegalArgumentException _e)
 						{}
 					}
 					if(villagerProfession==null)
 					{
 						return;
 					}
 					((Villager)ent).setProfession(villagerProfession);
 					break;
 			}
 		}
 	}
 	
 	//Allows plugins to get a Bukkit ItemStack from a user through text
 	//The format is @i[arg1=arg1,arg2=arg2,...]
 	public static ItemStack parseItemStack(String itemString)
 	{
 		if(itemString.startsWith("@i[")&&itemString.endsWith("]"))
 		{
 			Material mat=Material.AIR;
 			int amount=1;
 			short damage=0;
 			String name=null;
 			String lore=null;
 			HashMap<Enchantment, Integer> enchantments= new HashMap<Enchantment, Integer>();
 			String owner=null;
 			
 			for(String arg:itemString.substring(itemString.indexOf("[")+1,itemString.indexOf("]")).split(","))
 			{
 				if(arg.length()<3||!arg.contains("="))
 				{
 					continue;
 				}
 				if(arg.length()<=arg.indexOf("="))
 				{
 					continue;
 				}				
 				switch(arg.substring(0,arg.indexOf("=")))
 				{
 					case "m":
 					case "mat":
 					case "material":
 					case "id":
 						mat=Material.matchMaterial(arg.substring(arg.indexOf("=")+1,arg.length()).toUpperCase());
 						break;
 					case "c":
 					case "count":
 					case "amount":
 						amount=Integer.parseInt(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "d":
 					case "data":
 					case "damage":
 					case "durability":
 						damage=Short.parseShort(arg.substring(arg.indexOf("=")+1,arg.length()));
 						break;
 					case "n":
 					case "name":
 						name=arg.substring(arg.indexOf("=")+1,arg.length()).replace("_", " ");
 						break;
 					case "l":
 					case "lore":
 					case "description":
 						lore=arg.substring(arg.indexOf("=")+1,arg.length());
 						break;
 					case "e":
 					case "enchant":
 					case "enchantment":
 						String enchString=arg.substring(arg.indexOf("=")+1,arg.length());
 						if(enchString.contains("|"))
 						{							
 							String[] enchStringArray=enchString.split("\\|");
 							enchantments.put(Enchantment.getByName(enchStringArray[0].toUpperCase()), Integer.valueOf(enchStringArray[1]));
 						}
 						break;
 					case "o":
 					case "owner":
 						owner=arg.substring(arg.indexOf("=")+1,arg.length());
 						break;
 				}				
 			}
 			if(mat==null)
 			{
 				mat=Material.AIR;
 			}
 			ItemStack item=new ItemStack(mat,amount,damage);
 			ItemMeta meta=item.getItemMeta();
 			if(name!=null)
 			{
 				meta.setDisplayName(name);
 			}
 
 			if(lore!=null)
 			{
 				meta.setLore(Arrays.asList(lore.replace("_"," ").split("\\|")));
 			}
 			if(item.getType()==Material.SKULL_ITEM&&owner!=null)
 			{
 				((SkullMeta) meta).setOwner(owner);
 			}
 			item.setItemMeta(meta);
 			for(Enchantment ench:enchantments.keySet())
 			{
 				if(ench==null||enchantments.get(ench)==null)
 				{
 					continue;
 				}
 				item.addUnsafeEnchantment(ench, enchantments.get(ench));
 			}			
 			return item;
 		}
 		return null;		
 	}
 	
 	//Allows plugins to get a Bukkit Entity from a user through text
 	//This is after the command has already been filtered
 	//The format is @e[entityId]
 	public static Entity parseEntity(String entity, World world)
 	{		
 		if(entity.startsWith("@e[")&&entity.endsWith("]"))
 		{
 			int id=0;
 			try
 			{
 				id=Integer.parseInt(entity.substring(3, entity.length()-1));
 			}
 			catch(Exception e)
 			{
 				return null;
 			}
 			for(Entity ent:world.getEntities())
 			{
 				if(ent.getEntityId()==id)
 				{
 					return ent;
 				}
 			}
 		}
 		return Bukkit.getPlayer(entity);
 	}
 	
 	//Allows plugins to get a Bukkit Location from a user through text
 	//The format is @l[arg1=arg1,arg2=arg2,...]
 	public static Location parseLocation(String locString, Location def)
 	{
 		if(locString.startsWith("@l[")&&locString.endsWith("]"))
 		{
 			World world=Bukkit.getWorlds().get(0);					
 			double x=0;
 			double y=0;
 			double z=0;
 			float yaw=0;
 			float pitch=0;
 			int randX=0;
 			int minX=0;
 			int randY=0;
 			int minY=0;
 			int randZ=0;
 			int minZ=0;
 			boolean floor=false;
 			
 			if(def!=null)
 			{
 				world=def.getWorld();
 				x=def.getX();
 				y=def.getY();
 				z=def.getZ();
 				yaw=def.getYaw();
 				pitch=def.getPitch();
 			}
 			
 			for(String arg:locString.substring(locString.indexOf("[")+1,locString.indexOf("]")).split(","))
 			{
 				if(arg.length()<3)
 				{
 					continue;
 				}
 				if(arg.contains("="))
 				{
 					if(arg.length()<=arg.indexOf("="))
 					{
 						continue;
 					}	
 				}
 				try
 				{
 					switch(arg.substring(0,arg.contains("=")?arg.indexOf("="):arg.length()))
 					{
 						case "w":
 						case "world":
 							world=Bukkit.getWorld(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "c":
 						case "coords":
 						case "coordinates":
 							String[] coords=arg.substring(arg.indexOf("=")+1,arg.length()).split("\\|");
 							switch(coords.length)
 							{
 								default:
 								case 3:
 									z=Double.parseDouble(coords[2]);
 								case 2:
 									y=Double.parseDouble(coords[1]);
 								case 1:
 									x=Double.parseDouble(coords[0]);
 									break;
 								case 0:
 									break;
 							}
 							break;
 						case "x":
 							x=Double.parseDouble(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "y":
 							y=Double.parseDouble(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "z":
 							z=Double.parseDouble(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "p":
 						case "pitch":
 							pitch=Float.parseFloat(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "yaw":
 							yaw=Float.parseFloat(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "randx":
 						case "rx":
 							randX=Integer.parseInt(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "minx":
 						case "mx":
 							minX=Integer.parseInt(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "randy":
 						case "ry":
 							randY=Integer.parseInt(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "miny":
 						case "my":
 							minY=Integer.parseInt(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "randz":
 						case "rz":
 							randZ=Integer.parseInt(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "minz":
 						case "mz":
 							minZ=Integer.parseInt(arg.substring(arg.indexOf("=")+1,arg.length()));
 							break;
 						case "floor":
 						case "f":
 							floor=true;
 							break;
 					}
 				}
 				catch(NumberFormatException e)
 				{}
 			}
 			
 			if(minX>randX)
 			{
 				minX=randX;
 			}
 			if(minY>randY)
 			{
 				minY=randY;
 			}
 			if(minZ>randZ)
 			{
 				minZ=randZ;
 			}
 			
 			x=x+(Math.random()*(randX-minX)+minX)*(Math.random()>0.5?1:-1);
 			y=y+(Math.random()*(randY-minY)+minY)*(Math.random()>0.5?1:-1);
 			z=z+(Math.random()*(randZ-minZ)+minZ)*(Math.random()>0.5?1:-1);
 			
 			Location location=new Location(world,x,y,z,yaw,pitch);	
 			
 			while(floor && location.getBlock().getType()==Material.AIR && location.getBlock().getRelative(BlockFace.DOWN).getType()==Material.AIR)
 			{
 				location.subtract(0,1,0);
 			}
 			
 			while(floor && (location.getBlock().getType()!=Material.AIR || location.getBlock().getRelative(BlockFace.UP).getType()!=Material.AIR))
 			{
 				location.add(0,1,0);
 			}
 			
 			return location;
 		}
 		else if(parseEntity(locString,def.getWorld())!=null)
 		{
 			return parseEntity(locString,def.getWorld()).getLocation();
 		}
 		return def;		
 	}
 	
 	//Alows plugins to register their own flags for use in certain commands, such as logicif and the setflag sub-command
 	public void registerFlag(String flag, FlagListener listener)
 	{
 		if(listener==null)
 		{			
 			return;
 		}
 		flags.put(flag.toLowerCase().replace(" ", "_"), listener);
 	}
 	
 	//Many of the names listed for entities in the EntityType Enum are odd, and not what an average person would remember them by
 	//This helps to make them more user-friendly
 	public static EntityType entFromAlias(String alias)
 	{
 		switch(alias.toLowerCase())
 		{
 			case "firework":
 				return EntityType.FIREWORK;
 			case "pearl":
 			case "enderpearl":
 				return EntityType.ENDER_PEARL;
 			case "endersignal":
 				return EntityType.ENDER_SIGNAL;
 			case "xpbottle":
 				return EntityType.THROWN_EXP_BOTTLE;
 			case "tnt":
 				return EntityType.PRIMED_TNT;
 			case "minecart":
 				return EntityType.MINECART;
 			case "pigman":
 				return EntityType.PIG_ZOMBIE;
 			case "magmaslime":
 			case "redslime":
 			case "lavacube":
 			case "magmacube":
 				return EntityType.MAGMA_CUBE;
 			case "wither":
 				return EntityType.WITHER;
 			case "ocelot":
 				return EntityType.OCELOT;
 			case "irongolem":
 			case "golem":
 				return EntityType.IRON_GOLEM;
 			default:
 				return null;
 		}
 	}
 	
 	//A special teleport function - will not only teleport the specific entity, but also anything that it is riding, and anything that is riding it
 	public void teleport(Entity tper,Location tpLocation)
 	{
 		while(tper.getVehicle()!=null)
 		{
 			tper=tper.getVehicle();
 		}		
 		Entity tpPassenger=tper.getPassenger();
 		tper.eject();
 		tper.teleport(tpLocation);
 		tper.setPassenger(tpPassenger);
 		if(tpPassenger instanceof Player)
 		{
 			final EntityPlayer entPlayer=((CraftPlayer)tpPassenger).getHandle();
 			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable()
 			{
 				public void run() 
 				{
 					entPlayer.playerConnection.sendPacket(new Packet39AttachEntity(0,entPlayer,entPlayer.vehicle));
 				}						
 			}, 1);
 		}
 	}
 }
