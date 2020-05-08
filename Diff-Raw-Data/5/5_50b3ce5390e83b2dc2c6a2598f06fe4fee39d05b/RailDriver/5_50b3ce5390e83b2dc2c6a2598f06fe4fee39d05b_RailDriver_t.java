 package ca.mcpnet.RailDriver;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.material.Diode;
 import org.bukkit.material.Dispenser;
 import org.bukkit.material.Furnace;
 import org.bukkit.material.Lever;
 import org.bukkit.material.PistonBaseMaterial;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 
 
 
 public class RailDriver extends JavaPlugin {
 
	static public final String VERSION = "0.5";
 	static Logger logger = Logger.getLogger("Minecraft");
 	static public void log(String msg) {
 		logger.info("[RailDriver] "+msg);
 	}
 	
 	PluginManager pm;
 	
 	
 
 	private final ItemStack[] devkit = {
 			new ItemStack(Material.DIAMOND_PICKAXE, 1),
 			new ItemStack(Material.DIAMOND_SWORD, 1),
 			new ItemStack(Material.DIAMOND_SPADE, 1),
 			new ItemStack(Material.DIAMOND_AXE, 1),
 			new ItemStack(Material.DIAMOND_HOE, 1),
 			new ItemStack(Material.TORCH,64)};
 
 	private final ItemStack[] devarmor = {
 			new ItemStack(Material.DIAMOND_BOOTS, 1),
 			new ItemStack(Material.DIAMOND_LEGGINGS, 1),
 			new ItemStack(Material.DIAMOND_CHESTPLATE, 1),
 			new ItemStack(Material.DIAMOND_HELMET, 1),
 	};
 
 	private final Material[] diodes = {
 			Material.DIODE,
 			Material.DIODE_BLOCK_OFF,
 			Material.DIODE_BLOCK_ON
 	};
 	private final Material[] piston_base = {
 			Material.PISTON_STICKY_BASE,
 	};
 	private final Material[] piston_diamond = {
 			Material.DIAMOND_BLOCK,
 	};
 	private final Material[] furnaces = {
 			Material.FURNACE,
 			Material.BURNING_FURNACE
 	};
 	static protected BlockTemplate[][][] raildriverblocklist;
 	
 	HashSet<RailDriverTask> taskset;
 
 	static public enum Facing {
 		FORWARD,
 		LEFT,
 		BACKWARD,
 		RIGHT,
 		UP,
 		DOWN,
 		DONTCARE;
 		
 		static private final BlockFace horizBlockFaceArray[] = {
 			BlockFace.WEST,
 			BlockFace.SOUTH,
 			BlockFace.EAST,
 			BlockFace.NORTH
 		};
 		
 		public boolean checkFace(BlockFace direction, BlockFace blockdirection) {
 			if (this == DONTCARE) {
 				return true;
 			}
 			if (this == UP) {
 				return blockdirection == BlockFace.UP;
 			}
 			if (this == DOWN) {
 				return blockdirection == BlockFace.DOWN;
 			}
 			BlockFace expectedDirection = translate(direction);			
 			return expectedDirection == blockdirection;
 		}
 		
 		public BlockFace translate(BlockFace direction) {
 			if (direction == BlockFace.WEST) {
 				return horizBlockFaceArray[this.ordinal()];
 			} else if (direction == BlockFace.SOUTH) {
 				return horizBlockFaceArray[(this.ordinal() + 1) % 4];
 			} else if (direction == BlockFace.EAST) {
 				return horizBlockFaceArray[(this.ordinal() + 2) % 4];
 			} else if (direction == BlockFace.NORTH) {
 				return horizBlockFaceArray[(this.ordinal() + 3) % 4];
 			} else {
 				return null;
 			}
 		}
 	}
 	
 	static public class BlockTemplate {
 		Material[] materials;
 		Facing facing;
 		
  		BlockTemplate(Material m, Facing f) {
 			materials = new Material[1];
 			materials[0] = m;
 			facing = f;
 		}
 		
 		BlockTemplate(Material[] ma, Facing f) {
 			materials = ma;
 			facing = f;
 		}
 		
 		public boolean checkBlock(Block block, BlockFace direction) {
 			// Does the block type match what's specified in the template?
 			boolean typematch = false;
 			for (int i = 0; i < materials.length; i++) {
 				// log.info("  Match Type "+materials[i].name())
 				// Treat air as a dontcare
 				if (materials[i] == Material.AIR) {
 					return true;
 				}
 				if (block.getType() == materials[i]) {
 					typematch = true;
 					break;
 				}
 			}
 			if (!typematch) {
 				return false;
 			}
 			// Does the block's orientation match what's in the template?
 			BlockFace f;
 			if (block.getType() == Material.DIODE || 
 					block.getType() == Material.DIODE_BLOCK_OFF ||
 					block.getType() == Material.DIODE_BLOCK_ON) {
 				Diode diode = new Diode(block.getType(),block.getData());
 				f = diode.getFacing();
 			} else if (block.getType() == Material.PISTON_STICKY_BASE) {
 				PistonBaseMaterial piston = new PistonBaseMaterial(block.getType(),block.getData());
 				f = piston.getFacing();
 			} else if (block.getType() == Material.FURNACE ||
 					block.getType() == Material.BURNING_FURNACE) {
 				Furnace furnace = new Furnace(block.getType(),block.getData());
 				f = furnace.getFacing();
 			} else if (block.getType() == Material.DISPENSER) {
 				Dispenser dispenser = new Dispenser(block.getType(),block.getData());
 				f = dispenser.getFacing();
 			} else {
 				f = direction;
 			}
 			// log.info("  Match Direction "+f.name());
 			return facing.checkFace(direction, f);
 		}
 	}
 
 	public RailDriver() {
 		taskset = new HashSet<RailDriverTask>();
 		if (raildriverblocklist == null) {
 			raildriverblocklist = new BlockTemplate[3][3][];
 		
 			raildriverblocklist[0][0] = new BlockTemplate[6];
 			raildriverblocklist[0][0][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE);
 			raildriverblocklist[0][0][1] = new BlockTemplate(furnaces, Facing.BACKWARD);
 			raildriverblocklist[0][0][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[0][0][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[0][0][4] = new BlockTemplate(piston_base, Facing.FORWARD);
 			raildriverblocklist[0][0][5] = new BlockTemplate(piston_diamond, Facing.DONTCARE);
 	
 			raildriverblocklist[0][1] = new BlockTemplate[7];
 			raildriverblocklist[0][1][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE);
 			raildriverblocklist[0][1][1] = new BlockTemplate(Material.DISPENSER, Facing.BACKWARD);
 			raildriverblocklist[0][1][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[0][1][3] = new BlockTemplate(diodes, Facing.FORWARD);
 			raildriverblocklist[0][1][4] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[0][1][5] = new BlockTemplate(piston_base, Facing.FORWARD);
 			raildriverblocklist[0][1][6] = new BlockTemplate(piston_diamond, Facing.DONTCARE);
 	
 			raildriverblocklist[0][2] = new BlockTemplate[6];
 			raildriverblocklist[0][2][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE);
 			raildriverblocklist[0][2][1] = new BlockTemplate(Material.AIR, Facing.DONTCARE);
 			raildriverblocklist[0][2][2] = new BlockTemplate(Material.REDSTONE_WIRE, Facing.FORWARD);
 			raildriverblocklist[0][2][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[0][2][4] = new BlockTemplate(piston_base, Facing.FORWARD);
 			raildriverblocklist[0][2][5] = new BlockTemplate(piston_diamond, Facing.DONTCARE);
 	
 			raildriverblocklist[1][0] = new BlockTemplate[7];
 			raildriverblocklist[1][0][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE);
 			raildriverblocklist[1][0][1] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[1][0][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[1][0][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[1][0][4] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[1][0][5] = new BlockTemplate(piston_base, Facing.FORWARD);
 			raildriverblocklist[1][0][6] = new BlockTemplate(piston_diamond, Facing.DONTCARE);
 	
 			raildriverblocklist[1][1] = new BlockTemplate[8];
 			raildriverblocklist[1][1][0] = new BlockTemplate(Material.LEVER, Facing.FORWARD);
 			raildriverblocklist[1][1][1] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[1][1][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[1][1][3] = new BlockTemplate(diodes, Facing.FORWARD);
 			raildriverblocklist[1][1][4] = new BlockTemplate(diodes, Facing.FORWARD);
 			raildriverblocklist[1][1][5] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[1][1][6] = new BlockTemplate(piston_base, Facing.FORWARD);
 			raildriverblocklist[1][1][7] = new BlockTemplate(piston_diamond, Facing.DONTCARE);
 	
 			raildriverblocklist[1][2] = new BlockTemplate[7];
 			raildriverblocklist[1][2][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE);
 			raildriverblocklist[1][2][1] = new BlockTemplate(Material.CHEST, Facing.DONTCARE);
 			raildriverblocklist[1][2][2] = new BlockTemplate(Material.LEVER, Facing.DONTCARE);
 			raildriverblocklist[1][2][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[1][2][4] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[1][2][5] = new BlockTemplate(piston_base, Facing.FORWARD);
 			raildriverblocklist[1][2][6] = new BlockTemplate(piston_diamond, Facing.DONTCARE);
 	
 			raildriverblocklist[2][0] = new BlockTemplate[6];
 			raildriverblocklist[2][0][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE);
 			raildriverblocklist[2][0][1] = new BlockTemplate(furnaces, Facing.BACKWARD);
 			raildriverblocklist[2][0][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[2][0][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[2][0][4] = new BlockTemplate(piston_base, Facing.FORWARD);
 			raildriverblocklist[2][0][5] = new BlockTemplate(piston_diamond, Facing.DONTCARE);
 	
 			raildriverblocklist[2][1] = new BlockTemplate[7];
 			raildriverblocklist[2][1][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE);
 			raildriverblocklist[2][1][1] = new BlockTemplate(Material.DISPENSER, Facing.BACKWARD);
 			raildriverblocklist[2][1][2] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[2][1][3] = new BlockTemplate(diodes, Facing.FORWARD);
 			raildriverblocklist[2][1][4] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[2][1][5] = new BlockTemplate(piston_base, Facing.FORWARD);
 			raildriverblocklist[2][1][6] = new BlockTemplate(piston_diamond, Facing.DONTCARE);
 	
 			raildriverblocklist[2][2] = new BlockTemplate[6];
 			raildriverblocklist[2][2][0] = new BlockTemplate(Material.AIR, Facing.DONTCARE);
 			raildriverblocklist[2][2][1] = new BlockTemplate(Material.AIR, Facing.DONTCARE);
 			raildriverblocklist[2][2][2] = new BlockTemplate(Material.REDSTONE_WIRE, Facing.FORWARD);
 			raildriverblocklist[2][2][3] = new BlockTemplate(Material.IRON_BLOCK, Facing.DONTCARE);
 			raildriverblocklist[2][2][4] = new BlockTemplate(piston_base, Facing.FORWARD);
 			raildriverblocklist[2][2][5] = new BlockTemplate(piston_diamond, Facing.DONTCARE);
 		}
 	}
 
 	/**
 	 * This function will determine if this block is the 
 	 * lever block in a properly constructed RailDriver
 	 * @param railDriverBlockListener TODO
 	 * @param block TODO
 	 * @return
 	 */
 	boolean isRailDriver(Block block) {
 		//#debug debug
 //@		logger.info("validation construction");
 		// First is this block a lever block
 		if (block.getType() != Material.LEVER)
 			return false;
 		// What block is the lever attached to?
 		Lever lever = new Lever(block.getType(),block.getData());
 		BlockFace direction = lever.getAttachedFace();
 		Vector directionVector;
 		// RailDriver.log.info(direction.name());
 		if (direction == BlockFace.WEST) {
 			directionVector = new Vector(-1,0,0);
 		} else if (direction == BlockFace.EAST) {
 			directionVector = new Vector(1,0,0);
 		} else if (direction == BlockFace.NORTH) {
 			directionVector = new Vector(0,0,-1);
 		} else if (direction == BlockFace.SOUTH) {
 			directionVector = new Vector(0,0,1);
 		} else {
 			//#debug debug
 //@			logger.info("validation FAILED");
 			return false;
 		}
 	
 		for (int j = 0; j < 3; j++) {
 			for (int k = 0; k < 3; k++) {
 				Vector startBlock = null;
 				if (direction == BlockFace.WEST) {
 					startBlock = new Vector(block.getX(),block.getY()-1+k,block.getZ()-1+j);
 				} else if (direction == BlockFace.NORTH) {
 					startBlock = new Vector(block.getX()-1+j,block.getY()-1+k,block.getZ());
 				} else if (direction == BlockFace.SOUTH) {
 					startBlock = new Vector(block.getX()+1-j,block.getY()-1+k,block.getZ());
 				} else if (direction == BlockFace.EAST) {
 						startBlock = new Vector(block.getX(),block.getY()-1+k,block.getZ()+1-j);
 				} else {
 					//#debug debug
 //@					logger.info("validation FAILED");
 					return false;
 				}
 				/*
 				 * Some Handy code to generate code describing the machine
 				 * 
 				int i = 0;
 				BlockIterator bitr = new BlockIterator(block.getWorld(),startBlock,directionVector,0,10);
 				Block b;
 				while (bitr.hasNext()) {
 					b = bitr.next();
 					RailDriver.log.info("raildriverblocklist["+j+"]["+k+"]["+i+"] = new BlockTemplate(Material."+b.getType().name()+
 							", Facing.FIXME, false);");
 					i++;
 					if (b.getType() == Material.DIAMOND_BLOCK) {
 						RailDriver.log.info("// raildriverblocklist["+j+"]["+k+"] = new BlockTemplate["+(i)+"];");
 						break;
 					}
 				}
 				*/
 				// RailDriver.log.info("Checking "+j+","+k);
 				BlockIterator bitr = new BlockIterator(block.getWorld(),startBlock,directionVector,0,10);// RailDriver.raildriverblocklist[1][1].length);
 				for (int i = 0; i < RailDriver.raildriverblocklist[j][k].length; i++) {
 					Block b = bitr.next();
 					// RailDriver.log.info("Checking Block "+b.getType().name()+ " against template "+i);
 					BlockTemplate bt = RailDriver.raildriverblocklist[j][k][i];
 					if (!bt.checkBlock(b, direction)) 
 					{
 						//#debug debug
 //@						logger.info("validation FAILED");
 							return false;
 					}
 				}
 			}
 		}
 		//#debug debug
 //@		logger.info("validation OK");
 		return true;
 	}
 
 	// Bukkit Callbacks
 	public void onEnable() {
 		//#debug debug
		log("RailDriver v"+VERSION+" Plugin Enabled!");
 		pm = getServer().getPluginManager();
 		
 		pm.registerEvents(new RailDriverPlayerListener(this), this);
 		
 		
 		// Due to ongoing work, theres nothing in these atm, so commenting them out for now
 		//pm.registerEvents(new RailDriverBlockListener(this), this);
 		//pm.registerEvents(new RailDriverWorldListener(this), this);
 		
 		getConfig().options().copyDefaults(true);
 		getConfig().options().copyHeader(true);
 		saveConfig();
 
 	}
 
 	public void onDisable() {
 		log("RailDriver v"+VERSION+" Plugin Disabled!");
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 
 		
 		// Stock the player with things useful for a developer of this plugin
 		if (cmd.getName().equals("rd_devkit")) {
 			if (args.length > 1) {
 				sender.sendMessage("Too many arguments!");
 				return false;
 			}
 			if (args.length == 1) {
 				player = getServer().getPlayer(args[0]);
 				if (player == null) {
 					sender.sendMessage("Player not found!");
 					return false;
 				}
 			}
 			if (player == null) {
 				sender.sendMessage("Must specify player when calling from server console!");
 				return false;
 			}
 			sender.sendMessage("DevKit applied");
 			if (sender != player) {
 				player.sendMessage("DevKit applied");
 			}
 			PlayerInventory inventory = player.getInventory();
 			for (int i = 0; i < devkit.length; i++) {
 				if (!inventory.contains(devkit[i].getType())) {
 					inventory.addItem(devkit[i]);
 				}
 			}
 			inventory.setArmorContents(devarmor);
 			return true;
 		}
 		
 		// Stock the player with materials required to build a raildriver
 		if (cmd.getName().equalsIgnoreCase("rd_stock")) {
 			if (args.length > 1) {
 				sender.sendMessage("Too many arguments!");
 				return false;
 			}
 			if (args.length == 1) {
 				player = getServer().getPlayer(args[0]);
 				if (player == null) {
 					sender.sendMessage("Player not found!");
 					return false;
 				}
 			}
 			if (player == null) {
 				sender.sendMessage("Must specify player when calling from server console!");
 				return false;
 			}
 			sender.sendMessage("Inventory stocked with RailDriver parts");
 			if (sender != player) {
 				player.sendMessage("Inventory stocked with RailDriver parts");
 			}
 			PlayerInventory pi = player.getInventory();
 			pi.addItem(new ItemStack(29,9)); // 29 sticky piston
 			pi.addItem(new ItemStack(42,19)); // 42 iron block
 			pi.addItem(new ItemStack(331,2)); // 331 redstone
 			pi.addItem(new ItemStack(69,2)); // 69 lever
 			pi.addItem(new ItemStack(61,2)); // 61 furnace
 			pi.addItem(new ItemStack(23,2)); // 23 dispencer
 			pi.addItem(new ItemStack(57,9)); // 57 block of diamond
 			pi.addItem(new ItemStack(Material.DIODE,4));
 			pi.addItem(new ItemStack(Material.CHEST,1));
 			return true;
 		}
 		
 		// Reload the RailDriver configuration
 		if(cmd.getName().equalsIgnoreCase("rd_reload")) {
 			if(player != null) {
 				player.sendMessage("Reloading RailDriver configuration");
 			} 	
 			
 			this.reloadConfig();
 			log("Configuration reloaded!");
 			return true;
 		}
 		return false;
 	}
 
 	public RailDriverTask findRailDriverTask(Block block) {
 		Iterator<RailDriverTask> itr = taskset.iterator();
 		while(itr.hasNext()) {
 			RailDriverTask task = itr.next();
 			if (task.matchBlock(block)) {
 				return task;
 			}
 			else
 			{
 				logger.info("not a railDriver");
 			}
 			
 		}
 		return null;
 	}
 	
 	public RailDriverTask findCreateRailDriverTask(Block block) {
 		Iterator<RailDriverTask> itr = taskset.iterator();
 		while(itr.hasNext()) {
 			RailDriverTask task = itr.next();
 			if (task.matchBlock(block)) {
 				return task;
 			}
 		}
 		// Create a new task
 		RailDriverTask task = new RailDriverTask(this, block);
 		taskset.add(task);
 		return task;
 	}
 	
 	public void stopPlayerRailDrivers(Player target) {
 		Iterator<RailDriverTask> itr = taskset.iterator();
 		while(itr.hasNext()) {
 			RailDriverTask task = itr.next();
 			if (task.getOwner() == target) {
 				task.deactivate();
 			}
 		}
 	}
 }
