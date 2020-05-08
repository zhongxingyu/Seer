 package net.virtuallyabstract.minecraft;
 
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.material.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import javax.xml.parsers.*;
 import org.w3c.dom.*;
 import java.util.*;
 import java.io.*;
 
 public class BlockInfo implements Comparable<BlockInfo>
 {
 	private Block b;
 	private Material m;
 	private Byte data;
 	private String metaStr = null;
 	private org.bukkit.entity.Entity entity = null;
 
 	private static HashMap<Material, Integer> priorityMap = new HashMap<Material, Integer>();
 	private static final Integer DEFAULT_PRIORITY = 9999;
 
 	static
 	{
 		try
 		{
 			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(CommandBuilder.class.getResourceAsStream("/BlockPriority.xml"));
 			NodeList nodes = doc.getElementsByTagName("BlockPriority");
 			for(int i = 0; i < nodes.getLength(); i++)
 			{
 				Node parent = nodes.item(i);
 				NodeList children = parent.getChildNodes();
 				for(int n = 0; n < children.getLength(); n++)
 				{
 					Node child = children.item(n);
 					NamedNodeMap atts = child.getAttributes();
 
 					String tag = child.getNodeName();
 					if(!tag.equals("Block"))
 						continue;
 
 					String materialName = atts.getNamedItem("material").getNodeValue();
 					String priority = atts.getNamedItem("priority").getNodeValue();
 
 					Material m = Material.matchMaterial(materialName);
 					Integer p = DEFAULT_PRIORITY;
 					if(m == null)
 					{
 						System.out.println("Unrecognized material: " + materialName);
 						continue;
 					}
 
 					try
 					{
 						p = Integer.parseInt(priority);
 					}
 					catch(Exception e)
 					{
 						System.out.println("Invalid priority: " + priority);
 						continue;
 					}
 
 					priorityMap.put(m, p);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public BlockInfo(Block b)
 	{
 		this.b = b;
 		this.m = b.getType();
 		this.data = b.getData();
 	}
 
 	public BlockInfo(Block b, Material m, Byte data)
 	{
 		this.b = b;
 		this.m = m;
 		this.data = data;
 	}
 
 	public void setBlock()
 	{
 		if(metaStr != null && metaStr.startsWith("ENTITY"))
 		{
 			setEntity();
 			return;
 		}
 
 		if(m == null)
 			return;
 
 		b.setType(m);
 			
 		if(data != null)
 			b.setData(data);
 		
 		if(metaStr != null)
 		{
			BlockState bs = b.getState();
 			if(bs instanceof InventoryHolder)
 			{
 				Dungeon.parseInventoryString((InventoryHolder)bs, metaStr);	
 			}
 
 			if(bs instanceof org.bukkit.block.Sign)
 			{
 				Dungeon.parseSignString((org.bukkit.block.Sign)bs, metaStr);
 			}
 		}
 	}
 
 	public org.bukkit.entity.Entity getEntity()
 	{
 		return entity;
 	}
 	
 	public void setEntity(org.bukkit.entity.Entity e)
 	{
 		this.entity = e;
 	}
 
 	private void setEntity()
 	{
 		if(b == null)
 			return;
 
 		World w = b.getWorld();
 		if(w == null)
 			return;
 
 		String clazz = metaStr.replace("ENTITY(", "");
 		clazz = clazz.replace(")", "");
 
 		if(clazz.contains("org.bukkit.craftbukkit.entity.CraftMinecart"))
 			clazz = "org.bukkit.entity.Minecart";
 		else if(clazz.contains("org.bukkit.craftbukkit.entity.CraftPoweredMinecart"))
 			clazz = "org.bukkit.entity.PoweredMinecart";
 		else if(clazz.contains("org.bukkit.craftbukkit.entity.CraftStorageMinecart"))
 			clazz = "org.bukkit.entity.StorageMinecart";
 		else
 			return;
 
 		try
 		{
 			if(entity != null)
 			{
 				entity.remove();
 				entity = null;
 			}
 
 			@SuppressWarnings("unchecked")
 			Class<? extends org.bukkit.entity.Entity> entityClass = (Class<? extends org.bukkit.entity.Entity>)Class.forName(clazz);
 			entity = w.spawn(b.getLocation(), entityClass);
 		}
 		catch(Exception e)
 		{
 		}
 	}
 
 	public boolean isEntityBlock()
 	{
 		if(metaStr != null)
 			return metaStr.startsWith("ENTITY");
 		
 		return false;
 	}
 
 	public Block getBlock()
 	{
 		return b;
 	}
 
 	public Material getType()
 	{
 		return m;
 	}
 
 	public void setMetaString(String metaStr)
 	{
 		this.metaStr = metaStr;
 	}
 
 	public String getMetaString()
 	{
 		return metaStr;
 	}
 
 	public boolean isAttachable()
 	{
 		switch(m)
 		{
 			case TORCH:
 			case LADDER:
 			case IRON_DOOR_BLOCK:
 			case LEVER:
 			case RAILS:
 			case POWERED_RAIL:
 			case REDSTONE:
 			case REDSTONE_TORCH_ON:
 			case REDSTONE_TORCH_OFF:
 			case REDSTONE_WIRE:
 			case STONE_BUTTON:
 			case TRAP_DOOR:
 			case WALL_SIGN:
 			case WOOD_DOOR:
 			case WOODEN_DOOR:
 			case SAPLING:
 			case DETECTOR_RAIL:
 			case WEB:
 			case DEAD_BUSH:
 			case LONG_GRASS:
 			case YELLOW_FLOWER:
 			case RED_ROSE:
 			case BROWN_MUSHROOM:
 			case RED_MUSHROOM:
 			case CROPS:
 			case STONE_PLATE:
 			case WOOD_PLATE:
 			case SUGAR_CANE_BLOCK:
 			case DIODE_BLOCK_OFF:
 			case DIODE_BLOCK_ON:
 			case VINE:
 			case STORAGE_MINECART:
 			case POWERED_MINECART:
 				return true;
 			default:
 				return false;
 		}
 	}
 
 	public boolean isRedstoneTorch()
 	{
 		switch(m)
 		{
 			case REDSTONE_TORCH_ON:
 			case REDSTONE_TORCH_OFF:
 				return true;
 			default:
 				return false;
 		}
 	}
 
 	public boolean isLiquid()
 	{
 		switch(m)
 		{
 			case LAVA:
 			case STATIONARY_LAVA:
 			case WATER:
 			case STATIONARY_WATER:
 				return true;
 			default:
 				return false;
 		}
 	}
 
 	@Override public int compareTo(BlockInfo bi2)
 	{
 		boolean entity1 = isEntityBlock();
 		boolean entity2 = bi2.isEntityBlock();
 
 		if(!entity1 && entity2)
 			return -1;
 		if(entity1 && !entity2)
 			return 1;
 
 		int priority1 = DEFAULT_PRIORITY;
 		int priority2 = DEFAULT_PRIORITY;
 
 		if(priorityMap.containsKey(m))
 			priority1 = priorityMap.get(m);
 		if(priorityMap.containsKey(bi2.getType()))
 			priority2 = priorityMap.get(bi2.getType());
 
 		if(priority1 > priority2)
 			return -1;
 		if(priority1 < priority2)
 			return 1;
 
 		int y1 = b.getY();
 		int y2 = bi2.getBlock().getY();
 
 		if(y1 < y2)
 			return -1;
 		if(y1 > y2)
 			return 1;
 
 		int x1 = b.getX();
 		int x2 = bi2.getBlock().getX();
 
 		if(x1 < x2)
 			return -1;
 		if(x1 > x2)
 			return 1;
 
 		int z1 = b.getZ();
 		int z2 = bi2.getBlock().getZ();
 
 		if(z1 < z2)
 			return -1;
 		if(z1 > z2)
 			return 1;
 
 		return 0;
 	}
 
 	public void writeData(DataOutputStream dos)
 		throws Exception
 	{
 		dos.writeDouble(b.getX());
 		dos.writeDouble(b.getY());
 		dos.writeDouble(b.getZ());
 		dos.writeFloat(b.getLocation().getPitch());
 		dos.writeFloat(b.getLocation().getYaw());
 		dos.writeInt(b.getType().getId());
 		dos.write(b.getData());
 	}
 
 	public static BlockInfo readData(int version, DataInputStream dis, World world)
 		throws Exception
 	{
 		try
 		{
 			double x = dis.readDouble();
 			double y = dis.readDouble();
 			double z = dis.readDouble();
 			float pitch = dis.readFloat();
 			float yaw = dis.readFloat();
 
 			Location loc = new Location(world, x, y, z, yaw, pitch);
 			Block b = loc.getBlock();
 
 			int type = dis.readInt();
 			Material m = Material.getMaterial(type);
 
 			byte data = dis.readByte();
 
 			return new BlockInfo(b, m, data);
 		}
 		catch(EOFException eofe)
 		{
 			return null;
 		}
 	}
 	
 	//@Override public int compareTo(BlockInfo bi2)
 	//{
 	//	boolean isLiquid = false, isLiquid2 = false;
 	//	isLiquid = isLiquid();
 	//	isLiquid2 = bi2.isLiquid();
 
 	//	//Sugar cane needs to be loaded last since water needs to be present first
 	//	if(m == Material.SUGAR_CANE_BLOCK && bi2.getType() != Material.SUGAR_CANE_BLOCK)
 	//		return 1;
 	//	if(m != Material.SUGAR_CANE_BLOCK && bi2.getType() == Material.SUGAR_CANE_BLOCK)
 	//		return -1;
 
 	//	//Load liquids last (but before sugar canes)
 	//	if(isLiquid && !isLiquid2)
 	//		return 1;
 	//	if(!isLiquid && isLiquid2)
 	//		return -1;
 
 	//	//Load redstone torches first (before wires and other redstone related objects)
 	//	if(isRedstoneTorch() && !bi2.isRedstoneTorch())
 	//		return -1;
 	//	if(!isRedstoneTorch() && bi2.isRedstoneTorch())
 	//		return 1;
 
 	//	//Everything else gets loaded based on if it is an attachable object or not.
 	//	if(isLiquid == isLiquid2)
 	//	{
 	//		if(isAttachable() && bi2.isAttachable())
 	//			return 0;
 	//		if(isAttachable() && !bi2.isAttachable())
 	//			return 1;
 	//		if(!isAttachable() && bi2.isAttachable())
 	//			return -1;
 	//	}
 
 	//	return 0;
 	//}
 }
