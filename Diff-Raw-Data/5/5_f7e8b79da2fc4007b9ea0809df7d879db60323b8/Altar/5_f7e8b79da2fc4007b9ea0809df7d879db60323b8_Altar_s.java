 package com.censoredsoftware.demigods.episodes.demo.structure;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.world.ChunkLoadEvent;
 
 import com.censoredsoftware.core.util.Randoms;
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.element.Structure;
 import com.censoredsoftware.demigods.engine.language.TranslationManager;
 import com.censoredsoftware.demigods.engine.location.DLocation;
 import com.censoredsoftware.demigods.engine.util.Admins;
 import com.censoredsoftware.demigods.episodes.demo.EpisodeDemo;
 
 public class Altar extends Structure
 {
 	private final static List<BlockData> enchantTable = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.ENCHANTMENT_TABLE));
 		}
 	};
 	private final static List<BlockData> stoneBrick = new ArrayList<BlockData>(3)
 	{
 		{
 			add(new BlockData(Material.SMOOTH_BRICK, 8));
 			add(new BlockData(Material.SMOOTH_BRICK, (byte) 1, 1));
 			add(new BlockData(Material.SMOOTH_BRICK, (byte) 2, 1));
 		}
 	};
 	private final static List<BlockData> quartz = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.QUARTZ_BLOCK));
 		}
 	};
 	private final static List<BlockData> pillarQuartz = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.QUARTZ_BLOCK, (byte) 2));
 		}
 	};
 	private final static List<BlockData> stoneBrickSlabBottom = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(44), (byte) 5));
 		}
 	};
 	private final static List<BlockData> stoneBrickSlabTop = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(44), (byte) 13));
 		}
 	};
 	private final static List<BlockData> quartzSlabBottom = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(44), (byte) 7));
 		}
 	};
 	private final static List<BlockData> quartzSlabTop = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(44), (byte) 15));
 		}
 	};
 	private final static List<BlockData> stoneBrickSpecial = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(98), (byte) 3));
 		}
 	};
 	private final static List<BlockData> quartzSpecial = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.QUARTZ_BLOCK, (byte) 1));
 		}
 	};
 	private final static List<BlockData> spruceWood = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(5), (byte) 1));
 		}
 	};
 	private final static List<BlockData> spruceSlab = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(126), (byte) 1));
 		}
 	};
 	private final static List<BlockData> birchWood = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(5), (byte) 2));
 		}
 	};
 	private final static List<BlockData> birchSlab = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(126), (byte) 2));
 		}
 	};
 	private final static List<BlockData> sandStairNorth = new ArrayList<BlockData>()
 	{
 		{
 			add(new BlockData(Material.getMaterial(128), (byte) 6));
 		}
 	};
 	private final static List<BlockData> sandStairSouth = new ArrayList<BlockData>()
 	{
 		{
 			add(new BlockData(Material.getMaterial(128), (byte) 7));
 		}
 	};
 	private final static List<BlockData> sandStairEast = new ArrayList<BlockData>()
 	{
 		{
 			add(new BlockData(Material.getMaterial(128), (byte) 5));
 		}
 	};
 	private final static List<BlockData> sandStairWest = new ArrayList<BlockData>()
 	{
 		{
 			add(new BlockData(Material.getMaterial(128), (byte) 4));
 		}
 	};
 	private final static List<BlockData> smoothSandStone = new ArrayList<BlockData>()
 	{
 		{
 			add(new BlockData(Material.SANDSTONE, (byte) 2));
 
 		}
 	};
 	private final static List<BlockData> sandyGrass = new ArrayList<BlockData>()
 	{
 		{
 			add(new BlockData(Material.SAND, 2));
 			add(new BlockData(Material.GRASS, 8));
 		}
 	};
 	private final static List<BlockData> grass = new ArrayList<BlockData>()
 	{
 		{
 			add(new BlockData(Material.GRASS));
 		}
 	};
 
 	private final static Schematic general = new Schematic("general", "_Alex", 3)
 	{
 		{
 			// Create roof
 			add(new Cuboid(2, 3, 2, stoneBrickSlabTop));
 			add(new Cuboid(-2, 3, -2, stoneBrickSlabTop));
 			add(new Cuboid(2, 3, -2, stoneBrickSlabTop));
 			add(new Cuboid(-2, 3, 2, stoneBrickSlabTop));
 			add(new Cuboid(2, 4, 2, stoneBrick));
 			add(new Cuboid(-2, 4, -2, stoneBrick));
 			add(new Cuboid(2, 4, -2, stoneBrick));
 			add(new Cuboid(-2, 4, 2, stoneBrick));
 			add(new Cuboid(2, 5, 2, spruceSlab));
 			add(new Cuboid(-2, 5, -2, spruceSlab));
 			add(new Cuboid(2, 5, -2, spruceSlab));
 			add(new Cuboid(-2, 5, 2, spruceSlab));
 			add(new Cuboid(0, 6, 0, spruceSlab));
 			add(new Cuboid(-1, 5, -1, 1, 5, 1, spruceWood));
 
 			// Create the enchantment table
 			add(new Cuboid(0, 2, 0, enchantTable));
 
 			// Create magical table stand
 			add(new Cuboid(0, 1, 0, stoneBrick));
 
 			// Create outer steps
 			add(new Cuboid(3, 0, 3, stoneBrickSlabBottom));
 			add(new Cuboid(-3, 0, -3, stoneBrickSlabBottom));
 			add(new Cuboid(3, 0, -3, stoneBrickSlabBottom));
 			add(new Cuboid(-3, 0, 3, stoneBrickSlabBottom));
 			add(new Cuboid(4, 0, -2, 4, 0, 2, stoneBrickSlabBottom));
 			add(new Cuboid(-4, 0, -2, -4, 0, 2, stoneBrickSlabBottom));
 			add(new Cuboid(-2, 0, -4, 2, 0, -4, stoneBrickSlabBottom));
 			add(new Cuboid(-2, 0, 4, 2, 0, 4, stoneBrickSlabBottom));
 
 			// Create inner steps
 			add(new Cuboid(3, 0, -1, 3, 0, 1, stoneBrick));
 			add(new Cuboid(-1, 0, 3, 1, 0, 3, stoneBrick));
 			add(new Cuboid(-3, 0, -1, -3, 0, 1, stoneBrick));
 			add(new Cuboid(-1, 0, -3, 1, 0, -3, stoneBrick));
 
 			// Create pillars
 			add(new Cuboid(3, 4, 2, spruceSlab));
 			add(new Cuboid(3, 4, -2, spruceSlab));
 			add(new Cuboid(2, 4, 3, spruceSlab));
 			add(new Cuboid(-2, 4, 3, spruceSlab));
 			add(new Cuboid(-3, 4, 2, spruceSlab));
 			add(new Cuboid(-3, 4, -2, spruceSlab));
 			add(new Cuboid(2, 4, -3, spruceSlab));
 			add(new Cuboid(-2, 4, -3, spruceSlab));
 			add(new Cuboid(3, 0, 2, 3, 3, 2, stoneBrick));
 			add(new Cuboid(3, 0, -2, 3, 3, -2, stoneBrick));
 			add(new Cuboid(2, 0, 3, 2, 3, 3, stoneBrick));
 			add(new Cuboid(-2, 0, 3, -2, 3, 3, stoneBrick));
 			add(new Cuboid(-3, 0, 2, -3, 3, 2, stoneBrick));
 			add(new Cuboid(-3, 0, -2, -3, 3, -2, stoneBrick));
 			add(new Cuboid(2, 0, -3, 2, 3, -3, stoneBrick));
 			add(new Cuboid(-2, 0, -3, -2, 3, -3, stoneBrick));
 
 			// Left beam
 			add(new Cuboid(1, 4, -2, -1, 4, -2, stoneBrick).exclude(0, 4, -2));
 			add(new Cuboid(0, 4, -2, stoneBrickSpecial));
 			add(new Cuboid(-1, 5, -2, 1, 5, -2, spruceSlab));
 
 			// Right beam
 			add(new Cuboid(1, 4, 2, -1, 4, 2, stoneBrick).exclude(0, 4, 2));
 			add(new Cuboid(0, 4, 2, stoneBrickSpecial));
 			add(new Cuboid(-1, 5, 2, 1, 5, 2, spruceSlab));
 
 			// Top beam
 			add(new Cuboid(2, 4, 1, 2, 4, -1, stoneBrick).exclude(2, 4, 0));
 			add(new Cuboid(2, 4, 0, stoneBrickSpecial));
 			add(new Cuboid(2, 5, -1, 2, 5, 1, spruceSlab));
 
 			// Bottom beam
 			add(new Cuboid(-2, 4, 1, -2, 4, -1, stoneBrick).exclude(-2, 4, 0));
 			add(new Cuboid(-2, 4, 0, stoneBrickSpecial));
 			add(new Cuboid(-2, 5, -1, -2, 5, 1, spruceSlab));
 
 			// Create main platform
 			add(new Cuboid(0, 1, 0, stoneBrick));
 			add(new Cuboid(-2, 1, -2, 2, 1, 2, stoneBrickSlabBottom).exclude(0, 1, 0));
 		}
 	};
 	private final static Schematic holy = new Schematic("holy", "HmmmQuestionMark", 3)
 	{
 		{
 			// Create roof
 			add(new Cuboid(2, 3, 2, quartzSlabTop));
 			add(new Cuboid(-2, 3, -2, quartzSlabTop));
 			add(new Cuboid(2, 3, -2, quartzSlabTop));
 			add(new Cuboid(-2, 3, 2, quartzSlabTop));
 			add(new Cuboid(2, 4, 2, quartz));
 			add(new Cuboid(-2, 4, -2, quartz));
 			add(new Cuboid(2, 4, -2, quartz));
 			add(new Cuboid(-2, 4, 2, quartz));
 			add(new Cuboid(2, 5, 2, birchSlab));
 			add(new Cuboid(-2, 5, -2, birchSlab));
 			add(new Cuboid(2, 5, -2, birchSlab));
 			add(new Cuboid(-2, 5, 2, birchSlab));
 			add(new Cuboid(0, 6, 0, birchSlab));
 			add(new Cuboid(-1, 5, -1, 1, 5, 1, birchWood));
 
 			// Create the enchantment table
 			add(new Cuboid(0, 2, 0, enchantTable));
 
 			// Create magical table stand
 			add(new Cuboid(0, 1, 0, quartzSpecial));
 
 			// Create outer steps
 			add(new Cuboid(3, 0, 3, quartzSlabBottom));
 			add(new Cuboid(-3, 0, -3, quartzSlabBottom));
 			add(new Cuboid(3, 0, -3, quartzSlabBottom));
 			add(new Cuboid(-3, 0, 3, quartzSlabBottom));
 			add(new Cuboid(4, 0, -2, 4, 0, 2, quartzSlabBottom));
 			add(new Cuboid(-4, 0, -2, -4, 0, 2, quartzSlabBottom));
 			add(new Cuboid(-2, 0, -4, 2, 0, -4, quartzSlabBottom));
 			add(new Cuboid(-2, 0, 4, 2, 0, 4, quartzSlabBottom));
 
 			// Create inner steps
 			add(new Cuboid(3, 0, -1, 3, 0, 1, quartz).exclude(3, 0, 0));
 			add(new Cuboid(-1, 0, 3, 1, 0, 3, quartz).exclude(0, 0, 3));
 			add(new Cuboid(-3, 0, -1, -3, 0, 1, quartz).exclude(-3, 0, 0));
 			add(new Cuboid(-1, 0, -3, 1, 0, -3, quartz).exclude(0, 0, -3));
 			add(new Cuboid(3, 0, 0, quartzSpecial));
 			add(new Cuboid(0, 0, 3, quartzSpecial));
 			add(new Cuboid(-3, 0, 0, quartzSpecial));
 			add(new Cuboid(0, 0, -3, quartzSpecial));
 
 			// Create pillars
 			add(new Cuboid(3, 4, 2, birchSlab));
 			add(new Cuboid(3, 4, -2, birchSlab));
 			add(new Cuboid(2, 4, 3, birchSlab));
 			add(new Cuboid(-2, 4, 3, birchSlab));
 			add(new Cuboid(-3, 4, 2, birchSlab));
 			add(new Cuboid(-3, 4, -2, birchSlab));
 			add(new Cuboid(2, 4, -3, birchSlab));
 			add(new Cuboid(-2, 4, -3, birchSlab));
 			add(new Cuboid(3, 0, 2, 3, 3, 2, pillarQuartz));
 			add(new Cuboid(3, 0, -2, 3, 3, -2, pillarQuartz));
 			add(new Cuboid(2, 0, 3, 2, 3, 3, pillarQuartz));
 			add(new Cuboid(-2, 0, 3, -2, 3, 3, pillarQuartz));
 			add(new Cuboid(-3, 0, 2, -3, 3, 2, pillarQuartz));
 			add(new Cuboid(-3, 0, -2, -3, 3, -2, pillarQuartz));
 			add(new Cuboid(2, 0, -3, 2, 3, -3, pillarQuartz));
 			add(new Cuboid(-2, 0, -3, -2, 3, -3, pillarQuartz));
 
 			// Left beam
 			add(new Cuboid(1, 4, -2, -1, 4, -2, quartz).exclude(0, 4, -2));
 			add(new Cuboid(0, 4, -2, quartzSpecial));
 			add(new Cuboid(-1, 5, -2, 1, 5, -2, birchSlab));
 
 			// Right beam
 			add(new Cuboid(1, 4, 2, -1, 4, 2, quartz).exclude(0, 4, 2));
 			add(new Cuboid(0, 4, 2, quartzSpecial));
 			add(new Cuboid(-1, 5, 2, 1, 5, 2, birchSlab));
 
 			// Top beam
 			add(new Cuboid(2, 4, 1, 2, 4, -1, quartz).exclude(2, 4, 0));
 			add(new Cuboid(2, 4, 0, quartzSpecial));
 			add(new Cuboid(2, 5, -1, 2, 5, 1, birchSlab));
 
 			// Bottom beam
 			add(new Cuboid(-2, 4, 1, -2, 4, -1, quartz).exclude(-2, 4, 0));
 			add(new Cuboid(-2, 4, 0, quartzSpecial));
 			add(new Cuboid(-2, 5, -1, -2, 5, 1, birchSlab));
 
 			// Create main platform
 			add(new Cuboid(0, 1, 0, quartz));
 			add(new Cuboid(-2, 1, -2, 2, 1, 2, quartzSlabBottom).exclude(0, 1, 0));
 		}
 	};
 	private final static Schematic oasis = new Schematic("oasis", "_Alex", 4)
 	{
 		{
 			// Enchantment Table
 			add(new Cuboid(0, 1, 0, smoothSandStone));
 			add(new Cuboid(0, 2, 0, enchantTable));
 
 			// Ground
 			add(new Cuboid(-3, 0, -3, 3, 0, 3, sandyGrass).exclude(-2, 0, -2, 2, 0, 2));
 			add(new Cuboid(-2, 0, -2, 2, 0, 2, grass));
 
 			add(new Cuboid(-1, 1, -1, -1, 1, 1, sandStairWest));
 			add(new Cuboid(1, 1, -1, 1, 1, 1, sandStairEast));
 			add(new Cuboid(0, 1, -1, sandStairNorth));
 			add(new Cuboid(0, 1, 1, sandStairSouth));
 
 			// TODO: Add grass/flower generation
 		}
 	};
 
 	public static enum AltarDesign implements Design
 	{
		GENERAL("general", general), HOLY("holy", holy), OASIS("oasis", holy);
 
 		private final String name;
 		private final Schematic schematic;
 
 		private AltarDesign(String name, Schematic schematic)
 		{
 			this.name = name;
 			this.schematic = schematic;
 		}
 
 		@Override
 		public String getName()
 		{
 			return name;
 		}
 
 		@Override
 		public Schematic getSchematic()
 		{
 			return schematic;
 		}
 
 		public AltarDesign getByName(String name)
 		{
 			for(AltarDesign design : AltarDesign.values())
 			{
 				if(design.getName().equalsIgnoreCase(name)) return design;
 			}
 			return null;
 		}
 	}
 
 	@Override
 	public Set<Flag> getFlags()
 	{
 		return new HashSet<Flag>()
 		{
 			{
 				add(Flag.NO_PVP);
 				add(Flag.PRAYER_LOCATION);
 				add(Flag.PROTECTED_BLOCKS);
 			}
 		};
 	}
 
 	@Override
 	public String getStructureType()
 	{
 		return "Altar";
 	}
 
 	@Override
 	public Schematic get(String name)
 	{
 		if(name.equals(general.toString())) return general;
 		if(name.equals(oasis.toString())) return oasis;
 		return holy;
 	}
 
 	@Override
 	public int getRadius()
 	{
 		return Demigods.config.getSettingInt("zones.altar_radius");
 	}
 
 	@Override
 	public Location getClickableBlock(Location reference)
 	{
 		switch(reference.getBlock().getBiome())
 		{
 			case DESERT:
 			case DESERT_HILLS:
 				return reference.clone().add(0, 1, 0);
 			default:
 				return reference.clone().add(0, 2, 0);
 		}
 	}
 
 	@Override
 	public Listener getUniqueListener()
 	{
 		return new AltarListener();
 	}
 
 	@Override
 	public Set<Save> getAll()
 	{
 		return Util.findAll("type", getStructureType());
 	}
 
 	@Override
 	public Save createNew(Location reference, boolean generate)
 	{
 		Save save = new Save();
 		save.setReferenceLocation(reference);
 		save.setType(getStructureType());
 		save.setDesign(getDesign(reference).getName());
 		save.addFlags(getFlags());
 		save.save();
 		if(generate) save.generate();
 		return save;
 	}
 
 	public Design getDesign(Location reference)
 	{
 		switch(reference.getBlock().getBiome())
 		{
 			case ICE_PLAINS:
 				return AltarDesign.HOLY;
 			case DESERT:
 			case DESERT_HILLS:
 				return AltarDesign.OASIS;
 			default:
 				return AltarDesign.GENERAL;
 		}
 	}
 
 	public static boolean altarNearby(Location location)
 	{
 		for(Save structureSave : Util.findAll("type", "Altar"))
 		{
 			if(structureSave.getReferenceLocation().distance(location) <= Demigods.config.getSettingInt("generation.min_blocks_between_altars")) return true;
 		}
 		return false;
 	}
 }
 
 class AltarListener implements Listener
 {
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onChunkLoad(final ChunkLoadEvent event)
 	{
 		if(!event.isNewChunk()) return;
 
 		// Define variables
 		final Location location = DLocation.Util.randomChunkLocation(event.getChunk());
 
 		// Check if it can generate
 		if(Structure.Util.canGenerateStrict(location, 3))
 		{
 			// Return a random boolean based on the chance of Altar generation
 			if(Randoms.randomPercentBool(Demigods.config.getSettingDouble("generation.altar_chance")))
 			{
 				// If another Altar doesn't exist nearby then make one
 				if(!Altar.altarNearby(location))
 				{
 					Admins.sendDebug(ChatColor.RED + "Altar generated by SERVER at " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 
 					EpisodeDemo.Structures.ALTAR.getStructure().createNew(location, true);
 
 					location.getWorld().strikeLightningEffect(location);
 					location.getWorld().strikeLightningEffect(location);
 
 					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							for(Entity entity : event.getWorld().getEntities())
 							{
 								if(entity instanceof Player)
 								{
 									if(entity.getLocation().distance(location) < 400)
 									{
 										((Player) entity).sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + Demigods.text.getText(TranslationManager.Text.ALTAR_SPAWNED_NEAR));
 									}
 								}
 							}
 						}
 					}, 1);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void demigodsAdminWand(PlayerInteractEvent event)
 	{
 		if(event.getClickedBlock() == null) return;
 
 		// Define variables
 		Block clickedBlock = event.getClickedBlock();
 		Location location = clickedBlock.getLocation();
 		Player player = event.getPlayer();
 
 		/**
 		 * Handle Altars
 		 */
 		String design = clickedBlock.getType().equals(Material.EMERALD_BLOCK) ? "general" : clickedBlock.getType().equals(Material.GOLD_BLOCK) ? "holy" : clickedBlock.getType().equals(Material.DIAMOND_BLOCK) ? "oasis" : "general";
 		if(Admins.useWand(player) && Altar.AltarDesign.valueOf(design) != null)
 		{
 			event.setCancelled(true);
 
 			// Remove clicked block
 			clickedBlock.setType(Material.AIR);
 
 			Admins.sendDebug(ChatColor.RED + "Altar generated by ADMIN WAND at " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 
 			player.sendMessage(ChatColor.GRAY + Demigods.text.getText(TranslationManager.Text.ADMIN_WAND_GENERATE_ALTAR));
 
 			// Generate the Altar based on the block given.
 			Structure.Save save = EpisodeDemo.Structures.ALTAR.getStructure().createNew(location, false);
 			save.setDesign(design);
 			if(!save.generate()) player.sendMessage(ChatColor.RED + "Could not generate.");
 
 			player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TranslationManager.Text.ADMIN_WAND_GENERATE_ALTAR_COMPLETE));
 			return;
 		}
 
 		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && Admins.useWand(player) && Structure.Util.partOfStructureWithType(location, "Altar", true))
 		{
 			event.setCancelled(true);
 
 			Structure.Save altar = Structure.Util.getStructureSave(location, true);
 
 			if(DataManager.hasTimed(player.getName(), "destroy_altar"))
 			{
 				Admins.sendDebug(ChatColor.RED + "Altar at " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed by " + "ADMIN WAND" + ".");
 
 				// Remove the Altar
 
 				altar.remove();
 
 				DataManager.removeTimed(player.getName(), "destroy_altar");
 
 				player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TranslationManager.Text.ADMIN_WAND_REMOVE_ALTAR_COMPLETE));
 			}
 			else
 			{
 				DataManager.saveTimed(player.getName(), "destroy_altar", true, 5);
 				player.sendMessage(ChatColor.RED + Demigods.text.getText(TranslationManager.Text.ADMIN_WAND_REMOVE_ALTAR));
 			}
 		}
 	}
 }
