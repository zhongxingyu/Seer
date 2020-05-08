 package mods.taverns;
 
 import static net.minecraftforge.common.ChestGenHooks.VILLAGE_BLACKSMITH;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraft.world.World;
 import net.minecraft.world.gen.structure.ComponentVillage;
 import net.minecraft.world.gen.structure.ComponentVillageStartPiece;
 import net.minecraft.world.gen.structure.StructureBoundingBox;
 import net.minecraft.world.gen.structure.StructureComponent;
 import net.minecraftforge.common.ChestGenHooks;
 
 public class ComponentVillageTavern extends ComponentVillage
 {
 	public static final String TAVERN_CHEST = "tavernChest";
     public static final WeightedRandomChestContent[] tavernChestContents = new WeightedRandomChestContent[]{
     	new WeightedRandomChestContent(Item.diamond.itemID, 0, 1, 3, 3), 
     	new WeightedRandomChestContent(Item.ingotIron.itemID, 0, 1, 5, 10), 
     	new WeightedRandomChestContent(Item.bed.itemID, 0, 1, 3, 100), 
     	new WeightedRandomChestContent(Item.bread.itemID, 0, 1, 3, 15), 
     	new WeightedRandomChestContent(Item.appleRed.itemID, 0, 1, 3, 15), 
     	new WeightedRandomChestContent(Item.pickaxeIron.itemID, 0, 1, 1, 5), 
     	new WeightedRandomChestContent(Item.swordIron.itemID, 0, 1, 1, 5), 
     	new WeightedRandomChestContent(Item.plateIron.itemID, 0, 1, 1, 5), 
     	new WeightedRandomChestContent(Item.helmetIron.itemID, 0, 1, 1, 5), 
     	new WeightedRandomChestContent(Item.legsIron.itemID, 0, 1, 1, 5), 
     	new WeightedRandomChestContent(Item.bootsIron.itemID, 0, 1, 1, 5), 
     	new WeightedRandomChestContent(Block.obsidian.blockID, 0, 3, 7, 5), 
     	new WeightedRandomChestContent(Block.sapling.blockID, 0, 3, 7, 5)
     };
 	
     private int averageGroundLevel = -1;
     private static final int HEIGHT = 10;
 	private final int SOUTH = 3;
 	private final int WEST  = 0;
 	private final int NORTH = 2;
 	private final int EAST  = 1;
 
 	protected ComponentVillageTavern(ComponentVillageStartPiece startPiece, int type, Random random, StructureBoundingBox _boundingBox, int direction){
 		super(startPiece, type);
         coordBaseMode = direction;
     	boundingBox = _boundingBox;
 	}
 
 	public static Object buildComponent(ComponentVillageStartPiece startPiece, List pieces, Random random, int x, int y, int z, int direction, int type) {
         StructureBoundingBox _boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 14, 10, 8, direction);
         if(canVillageGoDeeper(_boundingBox)){ 
         	if(StructureComponent.findIntersecting(pieces, _boundingBox) == null){
         		return new ComponentVillageTavern(startPiece, type, random, _boundingBox, direction);
         	}
         }
 		return null;
 	}
 	
 	@Override
 	public boolean addComponentParts(World world, Random random, StructureBoundingBox box) 
 	{
         if (averageGroundLevel < 0){
             averageGroundLevel = getAverageGroundLevel(world, box);
             if (averageGroundLevel < 0){
                 return true;
             }
            boundingBox.offset(0, this.averageGroundLevel - boundingBox.maxY + HEIGHT - 1, 0);
         }
 
 		
 		fillWithBlocks(world, box, 0,0,0, 14,9,7, 0, 0, false);
 		
         for (int xx = 0; xx < 14; xx++){
             for (int zz = 0; zz < 8; zz++){
                 clearCurrentPositionBlocksUpwards(world, xx,0,zz, box);
                 fillCurrentPositionBlocksDownwards(world, Block.cobblestone.blockID, 0, xx,-1,zz, box);
             }
         }
 		
 		// floors
 		fillWithBlocks(world, box, 0,0,1, 14,0,7, Block.dirt.blockID, Block.dirt.blockID, false);
 		fillWithBlocks(world, box, 0,0,0, 14,0,0, Block.gravel.blockID, Block.gravel.blockID, false);
 		fillWithBlocks(world, box, 1,0,1, 4,0,6, Block.cobblestone.blockID, Block.cobblestone.blockID, false);
 		fillWithBlocks(world, box, 5,0,1, 12,0,6, Block.planks.blockID, Block.planks.blockID, false);
 		fillWithBlocks(world, box, 1,4,1, 12,4,6, Block.planks.blockID, Block.planks.blockID, false);	
 		fillWithBlocks(world, box, 7,8,1, 12,8,6, Block.planks.blockID, Block.planks.blockID, false);
 
 		// walls
 		fillWithBlocks(world, box, 1,1,1, 1,4,1, Block.wood.blockID, Block.wood.blockID, false);
 		fillWithBlocks(world, box, 1,1,2, 1,3,5, Block.cobblestone.blockID, Block.cobblestone.blockID, false);
 		fillWithBlocks(world, box, 1,1,6, 1,4,6, Block.wood.blockID, Block.wood.blockID, false);
 		
 		fillWithBlocks(world, box, 2,3,1, 3,3,1, Block.planks.blockID, Block.planks.blockID, false);
 		fillWithBlocks(world, box, 2,1,1, 3,1,1, Block.cobblestone.blockID, Block.cobblestone.blockID, false);
 		fillWithBlocks(world, box, 2,2,1, 3,2,1, Block.thinGlass.blockID, Block.thinGlass.blockID, false);
 		fillWithBlocks(world, box, 4,1,1, 4,3,1, Block.wood.blockID, Block.wood.blockID, false);
 		fillWithBlocks(world, box, 5,3,1, 6,3,1, Block.wood.blockID, Block.wood.blockID, false);
 		fillWithBlocks(world, box, 7,1,1, 7,7,1, Block.wood.blockID, Block.wood.blockID, false);
         placeBlockAtCurrentPosition(world, Block.wood.blockID, getMetadataWithOffset(Block.wood.blockID, 4), 4, 3, 1, box);
         placeBlockAtCurrentPosition(world, Block.wood.blockID, getMetadataWithOffset(Block.wood.blockID, 4), 7, 3, 1, box);
         fillWithBlocks(world, box, 8,1,1, 11,1,1, Block.cobblestone.blockID, Block.cobblestone.blockID, false);
 		fillWithBlocks(world, box, 8,2,1, 11,7,1, Block.planks.blockID, Block.planks.blockID, false);
 		fillWithBlocks(world, box, 8,2,1, 11,2,1, Block.thinGlass.blockID, Block.thinGlass.blockID, false);
 		fillWithBlocks(world, box, 12,1,1, 12,7,1, Block.wood.blockID, Block.wood.blockID, false);
 
 		fillWithBlocks(world, box, 12,1,2, 12,1,5, Block.cobblestone.blockID, Block.cobblestone.blockID, false);
 		fillWithBlocks(world, box, 12,2,2, 12,7,5, Block.planks.blockID, Block.planks.blockID, false);
 		fillWithBlocks(world, box, 12,2,3, 12,2,4, Block.thinGlass.blockID, Block.thinGlass.blockID, false);
 		fillWithBlocks(world, box, 12,6,3, 12,6,4, Block.thinGlass.blockID, Block.thinGlass.blockID, false);
 		fillWithBlocks(world, box, 12,1,6, 12,7,6, Block.wood.blockID, Block.wood.blockID, false);
 		
 		fillWithBlocks(world, box, 7,1,6, 11,1,6, Block.cobblestone.blockID, Block.cobblestone.blockID, false);
 		fillWithBlocks(world, box, 7,2,6, 11,7,6, Block.planks.blockID, Block.planks.blockID, false);
 		placeBlockAtCurrentPosition(world, Block.thinGlass.blockID, 0, 10,6,6, box);
 		fillWithBlocks(world, box, 6,1,6, 6,8,6, Block.wood.blockID, Block.wood.blockID, false);
 		placeBlockAtCurrentPosition(world, Block.wood.blockID, 0, 5,3,6, box);
 		fillWithBlocks(world, box, 4,1,6, 4,3,6, Block.wood.blockID, Block.wood.blockID, false);
 		fillWithBlocks(world, box, 2,1,6, 3,3,6, Block.cobblestone.blockID, Block.cobblestone.blockID, false);
 
 		fillWithBlocks(world, box, 7,5,2, 7,7,5, Block.planks.blockID, Block.planks.blockID, false);
 		fillWithBlocks(world, box, 6,5,5, 6,8,5, Block.planks.blockID, Block.planks.blockID, false);
 		
 		// roofing
 		for(int xx = 0; xx <= 13; xx++){
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, SOUTH), xx, 4, 0, box);
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, NORTH), xx, 4, 7, box);
 		}
 		for(int zz = 0; zz <= 7; zz++){
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, WEST), 0, 4, zz, box);
             placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, EAST), 13, 4, zz, box);
 		}
 		
 		for(int xx = 6; xx <= 13; xx++){
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, SOUTH), xx, 7, 0, box);
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, NORTH), xx, 7, 7, box);
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, SOUTH), xx, 8, 1, box);
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, NORTH), xx, 8, 6, box);
 		}
 		for(int xx = 7; xx <= 13; xx++){
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, SOUTH), xx, 9, 2, box);
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, NORTH), xx, 9, 5, box);
 		}
 		for(int zz = 2; zz <= 5; zz++){
 			placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, WEST), 6,8,zz, box);
 		}
 		for(int zz = 3; zz <= 4; zz++){
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, EAST), 13,9,zz, box);
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, WEST), 12,9,zz, box);	        
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, WEST), 7,9,zz, box);
 	        placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, EAST), 8,9,zz, box);
 		}
 		for(int zz = 5; zz <= 7; zz++){
 			placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, WEST), 5,7,zz, box);
 		}
 		fillWithAir(world, box, 6,8,4, 6,8,4);
 				
 		// Interior
 		fillWithBlocks(world, box, 1,5,6, 5,5,6, Block.fence.blockID, Block.fence.blockID, false);
 		fillWithBlocks(world, box, 1,5,1, 1,5,5, Block.fence.blockID, Block.fence.blockID, false);
 		fillWithBlocks(world, box, 2,5,1, 6,5,1, Block.fence.blockID, Block.fence.blockID, false);
 		
         placeBlockAtCurrentPosition(world, Block.torchWood.blockID, 0, 1,6,6, box);
         placeBlockAtCurrentPosition(world, Block.torchWood.blockID, 0, 1,6,1, box);
         placeBlockAtCurrentPosition(world, Block.torchWood.blockID, 0, 9,9,4, box);
         placeBlockAtCurrentPosition(world, Block.torchWood.blockID, 0, 4,3,0, box);
         placeBlockAtCurrentPosition(world, Block.torchWood.blockID, 0, 7,3,0, box);
         placeBlockAtCurrentPosition(world, Block.torchWood.blockID, 0, 5,3,7, box);
         placeBlockAtCurrentPosition(world, Block.torchWood.blockID, 0, 2,3,5, box);
         placeBlockAtCurrentPosition(world, Block.torchWood.blockID, 0, 4,3,2, box);
         placeBlockAtCurrentPosition(world, Block.torchWood.blockID, 0, 9,3,2, box);
         
         // FIXME: ladder metadata
         for(int yy = 5; yy <= 8; yy++){
         	placeBlockAtCurrentPosition(world, Block.ladder.blockID, getMetadataWithOffset(Block.ladder.blockID, 4), 6,yy,4, box);
         }
         
         placeBlockAtCurrentPosition(world, Block.bookShelf.blockID, 0, 11,5,2, box);
         placeBlockAtCurrentPosition(world, Block.torchWood.blockID, 0, 11,6,2, box);
         // TODO: Add code to place a bed
         placeBlockAtCurrentPosition(world, Block.bed.blockID, getMetadataWithOffset(Block.bed.blockID, 2), 10,5,2, box);
         placeBlockAtCurrentPosition(world, Block.bed.blockID, getMetadataWithOffset(Block.bed.blockID, 0), 10,5,3, box);
         
 
         // TODO: Add custom TAVERN_CHEST for loot drop
         generateStructureChestContents(world, box, random, 9,5,2, ChestGenHooks.getItems(ChestGenHooks.BONUS_CHEST, random), ChestGenHooks.getCount(ChestGenHooks.BONUS_CHEST, random));
         
         placeDoorAtCurrentPosition(world, box, random, 7,5,2, getMetadataWithOffset(Block.doorWood.blockID, EAST));
         placeDoorAtCurrentPosition(world, box, random, 5,1,1, getMetadataWithOffset(Block.doorWood.blockID, SOUTH));
         placeDoorAtCurrentPosition(world, box, random, 6,1,1, getMetadataWithOffset(Block.doorWood.blockID, SOUTH));
         placeDoorAtCurrentPosition(world, box, random, 5,1,6, getMetadataWithOffset(Block.doorWood.blockID, NORTH));
 		
         fillWithBlocks(world, box, 4,1,5, 4,3,5, Block.wood.blockID, Block.wood.blockID, false);
 		fillWithBlocks(world, box, 7,1,2, 7,3,2, Block.wood.blockID, Block.wood.blockID, false);
 		
 		placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, WEST), 7,1,5, box);
 		placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, WEST), 8,2,5, box);
 		placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, WEST), 9,3,5, box);
 		placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, WEST), 10,4,5, box);
 		fillWithAir(world, box, 7,4,5, 9,4,5);
 		
 		placeBlockAtCurrentPosition(world, Block.planks.blockID, Block.planks.blockID, 8,1,5, box);
 		placeBlockAtCurrentPosition(world, Block.stairsWoodSpruce.blockID, getMetadataWithOffset(Block.stairsWoodSpruce.blockID, WEST), 9,1,5, box);
 		placeBlockAtCurrentPosition(world, Block.stairsWoodSpruce.blockID, getMetadataWithOffset(Block.stairsWoodSpruce.blockID, WEST), 10,1,5, box);
 		placeBlockAtCurrentPosition(world, Block.stairsWoodSpruce.blockID, getMetadataWithOffset(Block.stairsWoodSpruce.blockID, WEST), 11,1,5, box);
 		placeBlockAtCurrentPosition(world, Block.stairsWoodSpruce.blockID, getMetadataWithOffset(Block.stairsWoodSpruce.blockID, NORTH), 11,1,4, box);
 		placeBlockAtCurrentPosition(world, Block.stairsWoodSpruce.blockID, getMetadataWithOffset(Block.stairsWoodSpruce.blockID, NORTH), 11,1,3, box);
 		placeBlockAtCurrentPosition(world, Block.planks.blockID, Block.planks.blockID, 11,1,2, box);
 		placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, WEST), 10,1,2, box);
 		placeBlockAtCurrentPosition(world, Block.fence.blockID, Block.fence.blockID, 9,1,2, box);
 		placeBlockAtCurrentPosition(world, Block.pressurePlatePlanks.blockID, 0, 9,2,2, box);
 		placeBlockAtCurrentPosition(world, Block.stairsWoodOak.blockID, getMetadataWithOffset(Block.stairsWoodOak.blockID, EAST), 8,1,2, box);
 		
 		fillWithBlocks(world, box, 4,1,2, 4,1,3, Block.planks.blockID, Block.planks.blockID, false);
 		fillWithBlocks(world, box, 2,1,2, 2,1,3, Block.planks.blockID, Block.planks.blockID, false);
 		// TODO: trapdoor name?
 		placeBlockAtCurrentPosition(world, Block.brewingStand.blockID, 0, 2,2,3, box);
 		placeBlockAtCurrentPosition(world, Block.furnaceIdle.blockID, getMetadataWithOffset(Block.furnaceIdle.blockID, SOUTH), 2,1,5, box);
 		placeBlockAtCurrentPosition(world, Block.planks.blockID, 0, 3,1,5, box);
 		
         spawnVillagers(world, box, 2,1,2,1);
         return true;
     }
 
 	// TODO: make a bar wench
     protected int getVillagerType(int par1){
         return VillageHandlerBarWench.BAR_WENCH;
     }
 
 	// TODO: identify desert biome to return sand, possibly others
 	private int getGroundID(){
 		return Block.grass.blockID;
 	}
 	
 	public static void registerTavernChest(){
 		ChestGenHooks.getInfo(TAVERN_CHEST);
    		for(int i = 0; i < tavernChestContents.length; i++){
 			ChestGenHooks.addItem(TAVERN_CHEST, tavernChestContents[i]);
 		}
 	}
 }
