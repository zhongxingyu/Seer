 package se.mickelus.customgen.newstuff;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.nbt.NBTBase;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.nbt.NBTTagString;
 import net.minecraftforge.common.BiomeDictionary.Type;
 
 import se.mickelus.customgen.MLogger;
 import se.mickelus.customgen.gui.GuiText;
 import se.mickelus.customgen.segment.Segment;
 
 public class Gen {
 	
 	public static final int UNDERGROUND_LEVEL = 0;
 	public static final int SURFACE_LEVEL = 1;
 	public static final int SEA_FLOOR_LEVEL = 2;
 	
 	public static final String NAME_KEY = "name";
 	public static final String RESOURCEPACK_KEY = "resourcepack";
 	public static final String LEVEL_KEY = "level";
 	public static final String VILLAGE_KEY = "village";
 	public static final String BIOME_KEY = "biome";
 	public static final String SEGMENT_KEY = "segment";
 	public static final String SEGMENT_START_KEY = "segmentstart";
 	
 	
 	//private EnumSet<Type> biomes;	
 	
 	private ArrayList<Type> biomes;
 	private int genLevel = -1;
 	
 	private String name;
 	private String resourcePack;
 	
 	private List<Segment> segmentList;
 	private List<Segment> startingSegments;
 	
 	private boolean isVillage = false;
 	
 	/**
 	 * Creates a new gen that generates in all biomes but has no segments.
 	 */
 	public Gen(String name, String resourcePack) {
 		this.name = name;
 		this.resourcePack = resourcePack;
 		
 		segmentList = new ArrayList<Segment>();
 		startingSegments = new ArrayList<Segment>();
 		
 		biomes = new ArrayList<Type>();
 
 	}
 	
 	/**
 	 * Returns the name of this gen.
 	 * @return The name of this gen.
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * Sets the name of this gen.
 	 * @param name The name of this gen.
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * Returns the name of the resource pack this gen is in.
 	 * @return The name of the resource pack this gen is in.
 	 */
 	public String getResourcePack() {
 		return resourcePack;
 	}
 	
 	/**
 	 * Sets the name of the resource pack this gen should be in.
 	 * @param resource PackThe name of the resource pack this gen should be in.
 	 */
 	public void setResourcePack(String resourcePack) {
 		this.resourcePack = resourcePack;
 	}
 
 	/**
 	 * Sets the biomes this gen can generate in. An empty array equals all biomes.
 	 * @param biomes An array of biome types.
 	 */
 	public void setBiomes(Type [] biomes) {
 		this.biomes.clear();
 		for (int i = 0; i < biomes.length; i++) {
 			this.biomes.add(biomes[i]);	
 		}
 	}
 	
 	/**
 	 * Sets the biomes this gen can generate in. An empty array equals all biomes.
 	 * @param biomes An array of integers representing indexes of values in the Type enum.
 	 */
 	public void setBiomes(int [] biomes) {
 		this.biomes.clear();
 		Type[] values = Type.values();
 		for (int i = 0; i < biomes.length; i++) {
 			this.biomes.add(values[biomes[i]]);		
 		}
 		
 	}
 	
 	/**
 	 * Sets the biomes this gen can generate in. An empty array equals all biomes.
 	 * @param biomes An array of strings representing values in the Type enum.
 	 */
 	public void setBiomes(String[] biomeNames) {
 		biomes.clear();
 		for (int i = 0; i < biomeNames.length; i++) {
 			biomes.add(Type.valueOf(biomeNames[i]));
 		}
 	}
 	
 	/**
 	 * Adds a biome Type to this gen
 	 * @param type
 	 */
 	public void addBiome(Type type) {
 		if(!biomes.contains(type)) {
 			biomes.add(type);
 		}
 	}
 	
 	/**
 	 * Checks if this gen can generate in the given biome type.
 	 * @param biome The type of biome to be checked against.
 	 * @return True if this gen can generate in the given type of biome, otherwise false.
 	 */
 	public boolean generatesInBiome(Type biome) {
 		return biomes.contains(biome);
 	}
 	
 	
 	/**
 	 * Returns the number of biome types this gen can generate in.
 	 * @return The amount of biome types this gen can generate in. A value of 0 means that
 	 *     this gen can generate in all biome types.
 	 */
 	public int getNumBiomes() {
 		return biomes.size();
 	}
 	
 	/**
 	 * Get a biome type that this gen can generate in by index.
 	 * @param index An index that is 0 or greater, and less than getNumBiomes()
 	 * @return The biome Type at given index.
 	 */
 	public Type getBiome(int index) {
 		return biomes.get(index);
 	}
 	
 	/**
 	 * Sets the biomes this gen can generate in. An empty array equals all biomes.
 	 * @param biomes An array of biome types.
 	 */
 	public Type[] getBiomes() {
 		Type[] types = new Type[biomes.size()];
 		return biomes.toArray(types);
 		
 	}
 	
 	/**
 	 * Set the level of this gen, this decides on what level this gens start segments can spawn.
 	 * The following values are accepted:
 	 * Gen.UNDERGROUND_LEVEL - will generate the start segment above y=4 and below y=52 TODO subject to change
 	 * Gen.SURFACE_LEVEL	 - will generate at the surface, either on land or sea
 	 * Gen.SEA_FLOOR_LEVEL	 - generates at the bottom at lakes, oceans and rivers. Acts as a surface gen if not in water.
 	 * @param genLevel
 	 */
 	public void setLevel(int genLevel) {
 		this.genLevel = genLevel;
 	}
 	
 	/**
 	 * Returns the level of this gen, this decides on what level this gens start segments can spawn.
 	 * @return the level of this gen.
 	 */
 	public int getLevel() {
 		return genLevel;
 	}
 	
 	/**
 	 * Sets whether or not this gen should generate as a building in a village.
 	 * @param isVillage true if it should generate in a village, otherwise false
 	 */
 	public void setVillageGen(boolean isVillage) {
 		this.isVillage = isVillage;
 	}
 	
 	/**
 	 * Returns a boolean representing whether if this gen should generate in a village or not.
 	 * @return a boolean representing whether if this gen should generate in a village or not.
 	 */
 	public boolean isVillageGen() {
 		return isVillage;
 	}
 	
 	/**
 	 * Adds a new segment to this gen. If the name of the segment matches an existing one,
 	 * that one will be replace by the new segment.
 	 * @param segment the segment that is to be added
 	 * @param isStart whether or not the gen can start a generation with this segment
 	 */
 	public void addSegment(Segment segment, boolean isStart) {
 		boolean found = false;
 
 			// iterate over starting segments
 			for (int i = 0; i < startingSegments.size(); i++) {
 				if(startingSegments.get(i).getName().equals(segment.getName())) {
 					
 					if(isStart) { // replace segment if isStart and it exists
 						startingSegments.set(i, segment);
 						return;
 					} else { // remove segment if !isStart and it exists
 						startingSegments.remove(i);
 						found = true;
 						break;
 					}
 					
 				}
 			}
 
 			// iterate over normal segments if we have not already found a match
 			if(!found) {
 				for (int i = 0; i < segmentList.size(); i++) {
 					if(segmentList.get(i).getName().equals(segment.getName())) {
 						
 						if(!isStart) { // replace segment if !isStart and it exists
 							segmentList.set(i, segment);
 							return;
 						} else { // remove segment if isStart and it exists
 							segmentList.remove(i);
 							break;
 						}
 					}
 				}
 				
 				
 			}
 		
 		if(isStart) {
 			startingSegments.add(segment);
 		} else {
 			segmentList.add(segment);
 		}
 	}
 	
 	/**
 	 * Returns a random starting segment in this gen.
 	 * @param random a properly seeded Random-object.
	 * @return a random starting segment in this gen or null if there is none.
 	 */
 	public Segment getStartingSegment(Random random) {
		if(startingSegments.size() == 0) {
			return null;
		}
 		int index = random.nextInt(startingSegments.size());
 		return startingSegments.get(index);
 	}
 	
 	/**
 	 * Attempts to return a random segment matching the given interface values based on the given Random-object.
 	 * Will match interface values above 0 (inclusive), a value of -1 is used as a wildcard.
 	 * @param interfaces an array of all the interface values. Should be of length 6, indexes and sides map like this:
 	 *     0 - top
 	 *     1 - bottom
 	 *     2 - north
 	 *     3 - east
 	 *     4 - south
 	 *     5 - west
 	 * @param random a properly seeded Random-object.
 	 * @return a random segment matching the given interfaces or null if there was no matching segments
 	 */
 	public Segment getMatchingSegment(int[] interfaces, Random random) {
 		List<Segment> tempList = new ArrayList<Segment>(); // TODO check performance on this
 		for (Segment segment : segmentList) {
 			boolean match = true;
 			for (int i = 0; i < interfaces.length; i++) {
 				if(interfaces[i] != -1 && interfaces[i] != segment.getInterface(i)) {
 					match = false;
 					break;
 				}
 			}
 			if(match) {
 				tempList.add(segment);
 			}
 		}
 		
 		if(tempList.size()>0) {
 			return tempList.get(random.nextInt(tempList.size()));
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the amount of "normal" segments in this gen.
 	 * @return the amount of "normal "segments in this gen.
 	 */
 	public int getNumSegments() {
 		return segmentList.size();
 	}
 	
 	/**
 	 * Returns the segment at the given index.
 	 * @param index Should be 0 => index < getNumSegments()
 	 * @return The segment at the given index.
 	 */
 	public Segment getSegment(int index) {
 		return segmentList.get(index);
 	}
 	
 	/**
 	 * Returns the amount of starting segments in this gen.
 	 * @return the amount of starting segments in this gen.
 	 */
 	public int getNumStartingSegments() {
 		return startingSegments.size();
 	}
 	
 	/**
 	 * Returns the starting segment at the given index.
 	 * @param index Should be 0 => index < getNumStartingSegments()
 	 * @return The starting segment at the given index.
 	 */
 	public Segment getStartingSegment(int index) {
 		return startingSegments.get(index);
 	}
 	
 	/**
 	 * Returns a segment which name matches the given name. This segment
 	 * can be either a normal segment or a starting segment.
 	 * @param name The name of the requested segment
 	 * @return A segment or null if there is no segment with a matching name
 	 */
 	public Segment getSegmentByName(String name) {
 		
 		// iterate over normal segments
 		for (Segment segment : segmentList) {
 			if(segment.getName().equals(name)) {
 				return segment;
 			}
 		}
 		
 		// iterate over starting segments
 		for (Segment segment : startingSegments) {
 			if(segment.getName().equals(name)) {
 				return segment;
 			}
 		}
 		
 		// return null if there is no matching segment
 		return null;
 	}
 	
 	/**
 	 * Converts this gen into a NBT tag, making it easy to save and send between the server and the clients.
 	 * This will also write all segments in this gen.
 	 * @return A NBT tag that can be used to rebuild this gen.
 	 */
 	public NBTTagCompound writeToNBT(boolean writeBlocks) {
 		NBTTagCompound nbt = new NBTTagCompound();
 		NBTTagList biomeTagList = new NBTTagList();
 		NBTTagList segmentTagList = new NBTTagList();
 		NBTTagList segmentStartTagList = new NBTTagList();
 		
 		nbt.setString(NAME_KEY, name);
 		nbt.setString(RESOURCEPACK_KEY, resourcePack);
 		nbt.setInteger(LEVEL_KEY, genLevel);
 		nbt.setBoolean(VILLAGE_KEY, isVillageGen());
 		
 		for (Type type : biomes) {
 			biomeTagList.appendTag(new NBTTagString(BIOME_KEY, type.toString()));
 		}
 		nbt.setTag(BIOME_KEY, biomeTagList);
 		
 		
 		for (Segment segment : segmentList) {			
 			segmentTagList.appendTag(segment.writeToNBT(writeBlocks));
 		}
 		nbt.setTag(SEGMENT_KEY, segmentTagList);
 		
 		for (Segment segment : startingSegments) {			
 			segmentStartTagList.appendTag(segment.writeToNBT(writeBlocks));
 		}
 		nbt.setTag(SEGMENT_START_KEY, segmentStartTagList);
 		
 		return nbt;
 	}
 	
 	/**
 	 * Creates a gen from the given NBT tag. This will also read all segments
 	 * contained in this NBT tag.
 	 * @param nbt a NBT tag containing gen data.
 	 * @return A Gen-object built from the NBT tag.
 	 */
 	public static Gen readFromNBT(NBTTagCompound nbt) {
 		Gen gen = new Gen(nbt.getString(NAME_KEY), nbt.getString(RESOURCEPACK_KEY));
 		NBTTagList biomeTagList = nbt.getTagList(BIOME_KEY);
 		NBTTagList segmentTagList = nbt.getTagList(SEGMENT_KEY);
 		NBTTagList segmentStartTagList = nbt.getTagList(SEGMENT_START_KEY);
 		String[] biomes = new String[biomeTagList.tagCount()];
 		
 		gen.setLevel(nbt.getInteger(LEVEL_KEY));
 		gen.setVillageGen(nbt.getBoolean(VILLAGE_KEY));
 		
 		for (int i = 0; i < biomeTagList.tagCount(); i++) {
 			
 			biomes[i] = biomeTagList.tagAt(i).toString();
 		}
 		gen.setBiomes(biomes);
 		
 		
 		for (int i = 0; i < segmentTagList.tagCount(); i++) {
 			gen.addSegment(Segment.readFromNBT((NBTTagCompound) segmentTagList.tagAt(i)), false);
 		}
 		
 		for (int i = 0; i < segmentStartTagList.tagCount(); i++) {
 			gen.addSegment(Segment.readFromNBT((NBTTagCompound) segmentStartTagList.tagAt(i)), true);
 		}
 		
 		
 		return gen;
 	}
 	
 	@Override
 	public String toString() {
 		return String.format("GEN:%s [PACK:%s B#:%d LVL:%d V:%b]", getName(), getResourcePack(), biomes.size(), getLevel(), isVillageGen());
 	}
 	
 }
