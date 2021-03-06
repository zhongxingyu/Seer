 package StevenDimDoors.mod_pocketDim.world.gateways;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import StevenDimDoors.mod_pocketDim.DDProperties;
 import StevenDimDoors.mod_pocketDim.Point3D;
 import StevenDimDoors.mod_pocketDim.mod_pocketDim;
 import StevenDimDoors.mod_pocketDim.core.DimLink;
 import StevenDimDoors.mod_pocketDim.core.LinkTypes;
 import StevenDimDoors.mod_pocketDim.core.PocketManager;
 import StevenDimDoors.mod_pocketDim.dungeon.DungeonData;
 import StevenDimDoors.mod_pocketDim.dungeon.pack.DungeonPack;
 import StevenDimDoors.mod_pocketDim.schematic.InvalidSchematicException;
 import StevenDimDoors.mod_pocketDim.schematic.Schematic;
 import StevenDimDoors.mod_pocketDim.schematic.SchematicFilter;
 import StevenDimDoors.mod_pocketDim.world.PocketBuilder;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 
 public abstract class BaseGateway 
 {
 	protected DungeonPack startingPack;
 	protected boolean isBiomeSpecific;
 	protected ArrayList<String> allowedBiomeNames;
 	protected boolean surfaceGateway;
 	protected int generationWeight;
 	protected String schematicPath;
 	protected GatewayBlockFilter filter;
 
 
 	public BaseGateway(DDProperties properties)
 	{
 		//not using DD properties because sometimes its IDS can be wrong, but require it so we dont init too early
 		filter = new GatewayBlockFilter((short) mod_pocketDim.dimensionalDoor.blockID,(short) mod_pocketDim.transientDoor.blockID);
 	}
 	
 	/**
 	 * Generates the gateway centered on the given coords
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 */
 	public boolean generate(World world, int x, int y, int z)
 	{
 		Point3D doorLocation= new Point3D(0,0,0);
 		int orientation = 0;
 		try 
 		{
 			if(this.schematicPath!=null)
 			{
 					Schematic schematic = Schematic.readFromResource(schematicPath);
 					schematic.applyFilter(filter);
 					doorLocation = filter.getEntranceDoorLocation();
 					orientation = filter.getEntranceOrientation();
 					schematic.copyToWorld(world, x-doorLocation.getX(), y-doorLocation.getY(), z-doorLocation.getZ());
 					
 			}
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 			return false;
 		}
 		this.generateRandomBits(world, x,y,z);
 		
 		DimLink link = PocketManager.getDimensionData(world).createLink(x, y + 1, z, LinkTypes.DUNGEON, orientation);
 		PocketBuilder.generateSelectedDungeonPocket(link, mod_pocketDim.properties, this.getStartingDungeon(world.rand));
 
 		return true;
 	}
 	
 	abstract void generateRandomBits(World world, int x, int y, int z);
 	
 	/**
 	 * returns a dungeon from the assigned pack to start with
 	 * @return
 	 */
 	public DungeonData getStartingDungeon(Random random)
 	{
 		return startingPack.getRandomDungeon(random);
 	}
 	
 	/**
 	 * determines if a given location is valid for the gateway to be generated, based on height, biome, and world.
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param biome
 	 * @return
 	 */
 	public boolean isLocationValid(World world, int x, int y, int z, BiomeGenBase biome)
 	{
 		return false;
 	}
 	
 	public boolean shouldGenUnderground()
 	{
 		return !surfaceGateway;
 	}
 	public boolean isBiomeValid(BiomeGenBase biome)
 	{
 		return this.isBiomeSpecific||this.allowedBiomeNames.contains(biome.biomeName.toLowerCase());
 	}
 	
 	
 }
