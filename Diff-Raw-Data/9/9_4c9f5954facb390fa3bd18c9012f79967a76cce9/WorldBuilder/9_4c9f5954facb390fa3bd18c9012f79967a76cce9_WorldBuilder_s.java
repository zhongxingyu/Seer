 package com.macbury.procedular;
 
 import java.awt.List;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Collections;
 import javax.security.auth.callback.Callback;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.GeomUtil;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.imageout.ImageOut;
 import org.newdawn.slick.util.Log;
 
 import com.macbury.unamed.PerlinGen;
 import com.macbury.unamed.level.Bedrock;
 import com.macbury.unamed.level.Block;
 import com.macbury.unamed.level.CoalOre;
 import com.macbury.unamed.level.CopperOre;
 import com.macbury.unamed.level.DiamondOre;
 import com.macbury.unamed.level.Dirt;
 import com.macbury.unamed.level.GoldOre;
 import com.macbury.unamed.level.Lava;
 import com.macbury.unamed.level.Level;
 import com.macbury.unamed.level.Rock;
 import com.macbury.unamed.level.Sand;
 import com.macbury.unamed.level.Sidewalk;
 import com.macbury.unamed.level.Water;
 
 public class WorldBuilder implements Runnable, DungeonBSPNodeCorridorGenerateCallback, DungeonBSPNodeRoomGenerateCallback {
   private static final int SEED_THROTTLE      = 1000;
   private static final int ROOM_COUNT         = 60;
   private static final int CELL_SIZE          = 256;
   
   public static final int NORMAL              = 1000;
   public static final int BIG                 = 4000;
   public static final int CRASH_MY_COMPUTER   = 6000;
   private static final int MIN_DIGGER_COUNT   = 50;
   private static final int MAX_DIGGER_COUNT   = MIN_DIGGER_COUNT * 10;
   private static final float CAVE_COUNT_FACTOR  = 0.70f;
   
   private static final int MAX_LOOPS_WITHOUT_SPAWN = 2000;
 
 
   
   public float perlinNoise[][];
   public int size;
   private PerlinGen pg;
   private int seed;
   private WorldBuilderListener listener;
   
   private ArrayList<Room> rooms;
   private ArrayList<CaveDigger> diggers;
   public int progress;
   private Level level;
   public float subProgress;
   public String currentStatus;
   private Random random;
   private int dirtCellCount = 0;
   
   public WorldBuilder(int size, int seed) throws SlickException {
     this.seed          = seed;
     this.size          = size;
     this.pg            = new PerlinGen(0, 0);
     this.rooms         = new ArrayList<Room>();
     this.diggers       = new ArrayList<CaveDigger>();
     this.level         = new Level();
     this.level.setSize(size);
     currentStatus = "Initializing world...";
   }
   
   public void setListener(WorldBuilderListener listener) {
     this.listener = listener;
   }
   
   private void applyCopper() throws SlickException {
     this.perlinNoise   = pg.generate(size, 5, getSeed());
     applyDataFromPerlinNoise(0.0f,0.3f, Block.RESOURCE_COPPER);
   }
 
   private void applySandAndWater() throws SlickException {
     this.perlinNoise   = pg.generate(size, 6, getSeed());
     applyDataFromPerlinNoise(0.0f,0.38f, Block.RESOURCE_SAND);
     applyDataFromPerlinNoise(0.0f,0.3f, Block.RESOURCE_WATER); //water
   }
   
   private void applyStone() throws SlickException {
     this.perlinNoise   = pg.generate(size, 7, getSeed());
     applyDataFromPerlinNoise(0.0f,0.4f, Block.RESOURCE_STONE);
     applyDataFromPerlinNoise(0.1f,0.2f, Block.RESOURCE_LAVA); //lava
   }
 
   private void applyCoal() throws SlickException {
     this.perlinNoise   = pg.generate(size, 5, getSeed());
     applyDataFromPerlinNoise(0.0f,0.3f, Block.RESOURCE_COAL);
     applyDataFromPerlinNoise(0.0f,0.05f, Block.RESOURCE_DIAMOND); // diamond
   }
 
   private void applyGold() throws SlickException {
     this.perlinNoise   = pg.generate(size, 7, getSeed());
     applyDataFromPerlinNoise(0.0f,0.1f, Block.RESOURCE_GOLD);
   }
   
   private int getSeed() {
     this.seed++;
     return this.seed * SEED_THROTTLE;
   }
 
   private void fillWithGround() {
     this.subProgress = 0;
     for (int x = 0; x < this.size; x++) {
       this.subProgress = (float)x / (float)this.size;
       for (int y = 0; y < this.size; y++) {
         this.level.setBlock(x, y, new Dirt(x, y));
       }
     }
     
     this.dirtCellCount  = this.size * this.size;
     
     this.subProgress = 0;
   }
   
   public void applyDataFromPerlinNoise(float start, float end, byte resourceType) throws SlickException {
     this.subProgress = 0.0f;
     for (int x = 0; x < this.size; x++) {
       this.subProgress = (float)x / (float)this.size;
       for (int y = 0; y < this.size; y++) {
         float val = this.perlinNoise[x][y];
         if (val >= start && val <= end) {
           Block block = Block.blockByTypeId(resourceType, x, y);
           this.level.setBlock(x, y, block);
           this.dirtCellCount--;
         }
       }
     }
   }
   
   public void dumpTo(String filePath) throws SlickException {
     this.level.dumpTo(filePath);
   }
   
   @Override
   public void run() {
     this.random      = new Random(seed);
     try {
       applyResources();
       digCaves();
       buildDungeon();
       applyBedrockBorder();
     } catch (Exception e) {
       e.printStackTrace();
     }
     //applyRooms();
 
     this.level.save();
     this.progress = 100;
     Log.info("Finished...");
     this.listener.onWorldBuildingFinish();
   }
 
   public Dirt findRandomDirt() {
     Dirt dirt = null;
     while(dirt == null) {
       int x = random.nextInt(this.size);
       int y = random.nextInt(this.size);
       Block block = level.getBlockForPosition(x, y);
       if (block != null && block.isDirt()) {
         dirt = (Dirt) block;
       }
     }
     
     return dirt;
   }
   
   private void digCaves() {
     currentStatus = "Creating cave system";
     this.progress = 45;
     
     while(this.diggers.size() < MIN_DIGGER_COUNT) {
       spawnRandomDigger();
     }
     
     this.progress = 46;
     
     this.dirtCellCount = 0;
     this.subProgress = 0;
     for (int x = 0; x < this.size; x++) {
       this.subProgress = (float)x / (float)this.size;
       for (int y = 0; y < this.size; y++) {
         if (level.getBlockForPosition(x, y).isDirt()) {
           dirtCellCount++;
         }
       }
     }
     
     int loopWithoutSpawning = 0;
     int totalDirtCells = Math.round(this.dirtCellCount * CAVE_COUNT_FACTOR);
     int dirtCellsLeft = totalDirtCells;
     
     boolean spawnedDigger = false;
     while(dirtCellsLeft > 0) {
       this.subProgress = 1.0f - (float)dirtCellsLeft / (float)totalDirtCells;
       
       for (int i = 0; i < this.diggers.size(); i++) {
         CaveDigger digger = this.diggers.get(i);
         if (digger.dig()) {
          dirtCellsLeft -= 4;
           
           CaveDigger newCaveDigger = digger.tryToSpawnDigger();
           
           if (newCaveDigger != null) {
             //Log.info("Spawned digger nr. "+ this.diggers.size());
             this.diggers.add(newCaveDigger);
             spawnedDigger = true;
           }
         }
       }
       
       if (spawnedDigger) {
         spawnedDigger = false;
         loopWithoutSpawning = 0;
       } else {
         loopWithoutSpawning++;
       }
       
       if (loopWithoutSpawning > MAX_LOOPS_WITHOUT_SPAWN) {
         loopWithoutSpawning = 0;
         spawnRandomDigger();
         
         dirtCellsLeft -= dirtCellsLeft * 0.01f;
       }
     }
     
     this.progress = 50;
     currentStatus = "Cleaning caves";
     
     for (int x = 1; x < this.size-1; x++) {
       this.subProgress = (float)x / (float)this.size;
       for (int y = 1; y < this.size-1; y++) {
         if (level.getBlockForPosition(x, y).isDirt()) {
           
           if (level.getBlockForPosition(x, y-1).isAir() && level.getBlockForPosition(x, y+1).isAir() && level.getBlockForPosition(x-1, y).isAir() && level.getBlockForPosition(x+1, y).isAir()) {
             level.setBlockForPosition(new Sidewalk(x, y), x, y);
           } else {
             if (isIsland(x,y, 2) || isIsland(x,y, 3) || isIsland(x,y,4)) {
               level.setBlockForPosition(new Sidewalk(x, y), x, y);
             }
           }
           
         }
       }
     }
   }
   
   public boolean isIsland(int sx, int sy, int size) {
     boolean island = true;
     int ex = sx + size;
     int ey = sy + size;
     for (int x = 1; x <= ex; x++) {
       for (int y = 1; y <= ey; y++) {
         Block block = level.getBlockForPosition(x, y);
         if (block != null) {
           if (x == ex && y == ey) {
             
             if (!block.isAir()) {
               island = false;
               return false;
             }
             
           } else {
             if (!block.isDirt() || !block.isAir()) {
               island = false;
               return false;
             }
           }
         }
         
       }
     }
     
     return island;
   }
 
   private void spawnRandomDigger() {
     Dirt block = findRandomDirt();
     CaveDigger digger = new CaveDigger(level, random);
     digger.setX(block.x);
     digger.setY(block.y);
     Log.info("Spawning Miner at position: " + block.x + " x " + block.y);
     this.diggers.add(digger);
   }
 
   private void buildDungeon() {
     int cellCount = this.size / CELL_SIZE;
     Log.info("Cell size: " + cellCount); 
     
     currentStatus = "Creating dungeon";
     
     int minCellCount = Math.round(cellCount * cellCount * 0.5f);
     
     ArrayList<DungeonBSPNode> generatedDungeons = new ArrayList<DungeonBSPNode>();
     
     while (minCellCount > 0) {
       int x = random.nextInt(size - CELL_SIZE);
       int y = random.nextInt(size - CELL_SIZE);
       
       DungeonBSPNode dungeonBSPNode = new DungeonBSPNode(null, x, y, CELL_SIZE, CELL_SIZE, 0, random);
       
       boolean splitDungeon = true;
       
       for (DungeonBSPNode iterateNode : generatedDungeons) {
         if (iterateNode.intersects(dungeonBSPNode)) {
           splitDungeon = false;
           break;
         }
       }
       
       if (splitDungeon) {
         generatedDungeons.add(dungeonBSPNode);
         dungeonBSPNode.split();
         ArrayList<Room> rooms = dungeonBSPNode.getAllRooms();
         this.level.applyRooms(rooms);
         dungeonBSPNode.bottomsUpByLevelEnumerate(this, this);
         
         int i = 1 + this.random.nextInt(3);
         while(i-- > 0) {
           Collections.shuffle(rooms, this.random);
           
           Room previousRoom = null;
           
           for (Room currentRoom : rooms) {
             if (previousRoom != null) {
               bruteForceConnectRooms(previousRoom.getNode(), currentRoom.getNode());
             }
               
             previousRoom = currentRoom;
           }
         }
       }
       
       minCellCount--;
     }
     
     //cellCount        = minCellCount + random.nextInt(minCellCount);
     
     
     
   }
   
   @Override
   public void onGenerateRoom(DungeonBSPNode dungeonNode) {
     dungeonNode.createRoom();
   }
 
   @Override
   public void onGenerateCorridor(DungeonBSPNode currentNode) {
     defaultCorridorGenerator(currentNode);
   }
   
   public void bruteForceConnectRooms(DungeonBSPNode regionA, DungeonBSPNode regionB) {
     CorridorDigger digger     = new CorridorDigger(this.level);
     
     if (regionA.getRoom().getMaxY() < regionB.getRoom().getY() || regionB.getRoom().getMaxY() < regionA.getRoom().getY()) {
       DungeonBSPNode upperRegion = (regionA.getRoom().getMaxY() <= regionB.getRoom().getY()) ? regionA : regionB;
       DungeonBSPNode lowerRegion = (upperRegion == regionA) ? regionB : regionA;
       
       int minOverlappingX        = (int) Math.max(upperRegion.getRoom().getX(), lowerRegion.getRoom().getX());
       int maxOverlappingX        = (int) Math.min(upperRegion.getRoom().getMaxX(), lowerRegion.getRoom().getMaxX());
       
       if (maxOverlappingX - minOverlappingX >= 3) {
         int corridorX = minOverlappingX + 1 + this.random.nextInt(maxOverlappingX - minOverlappingX - 2);
         
         digger.dig(corridorX, (int) upperRegion.getRoom().getMaxY(), CorridorDigger.UP_DIRECTION, 0, true);
         digger.dig(corridorX, (int) (upperRegion.getRoom().getMaxY() + 1), CorridorDigger.DOWN_DIRECTION, (int) (lowerRegion.getRoom().getY() - (upperRegion.getRoom().getMaxY() + 1)), true);
       } else {
         connectZShapeVertical(digger, upperRegion, lowerRegion);
       }
     } else {
       DungeonBSPNode leftRegion = (regionA.getRoom().getMaxX() <= regionB.getRoom().getX()) ? regionA : regionB;
       DungeonBSPNode rightRegion = (leftRegion == regionA) ? regionB : regionA;
       
       int minOverlappingY = (int) Math.max(leftRegion.getRoom().getY(), rightRegion.getRoom().getY());
       int maxOverlappingY = (int) Math.min(leftRegion.getRoom().getMaxY(), rightRegion.getRoom().getMaxY());
       if (maxOverlappingY - minOverlappingY >= 3) {
         int corridorY = minOverlappingY + 1 + this.random.nextInt(maxOverlappingY - minOverlappingY - 2);
 
         digger.dig(leftRegion.getRoom().getMaxX(), corridorY, CorridorDigger.LEFT_DIRECTION, 0, true);
         digger.dig(leftRegion.getRoom().getMaxX() + 1, corridorY, CorridorDigger.RIGHT_DIRECTION, (int) (rightRegion.getRoom().getX() - (leftRegion.getRoom().getMaxX() + 1)), true);
       } else {
         connectZShapeHorizontal(digger, leftRegion, rightRegion);
       }
     }
   }
   
   public void connectZShapeHorizontal(CorridorDigger digger, DungeonBSPNode leftChild, DungeonBSPNode rightChild) {
     int tunnelMeetX, tunnelMeetY;
     if (leftChild.getRoom().getY() > rightChild.getRoom().getY()) {
       //        _____
       //   X____|   |
       //    |   | R |
       //    |   |___|
       //  __|__
       //  |   |
       //  | L |
       //  |___|
       tunnelMeetX = randomValueBetween(leftChild.getRoom().getX() + 1, leftChild.getRoom().getMaxX());
       tunnelMeetY = randomValueBetween(rightChild.getRoom().getY() + 1, Math.min(rightChild.getRoom().getMaxY() - 1, leftChild.getRoom().getY()));
       digger.digDownRightCorridor(tunnelMeetX, tunnelMeetY, tunnelMeetX, tunnelMeetY);
     } else {
       //    _____
       //    |   |____X
       //    | L |   |
       //    |___|   |
       //          __|__
       //          |   |
       //          | R |
       //          |___|
       tunnelMeetX = randomValueBetween(rightChild.getRoom().getX() + 1, rightChild.getRoom().getMaxX());
       tunnelMeetY = randomValueBetween(leftChild.getRoom().getY() + 1, Math.min(leftChild.getRoom().getMaxY() - 1, rightChild.getRoom().getY()));
       digger.digDownLeftLCorridor(tunnelMeetX, tunnelMeetY, tunnelMeetX, tunnelMeetY);
     }
   }
   
   public void connectZShapeVertical(CorridorDigger digger, DungeonBSPNode leftChild, DungeonBSPNode rightChild) {
     int tunnelMeetX, tunnelMeetY;
     
     if (leftChild.getRoom().getX() > rightChild.getRoom().getX()) {
       //        _____
       //        |   |
       //        | L |
       //        |___|
       //  _____   |
       //  |   |   |
       //  | R |___|X
       //  |___|    
       
       tunnelMeetX = randomValueBetween(Math.max(leftChild.getRoom().getX() + 1, rightChild.getRoom().getMaxX() + 1), leftChild.getRoom().getMaxX());
       tunnelMeetY = randomValueBetween(rightChild.getRoom().getY() + 1, rightChild.getRoom().getMaxY());
       digger.digUpLeftCorridor(tunnelMeetX, tunnelMeetY, tunnelMeetX, tunnelMeetY);
     } else {
       //    _____
       //    |   |
       //    | L |
       //    |___|
       //      |    _____
       //      |    |   |
       //     X|____| R |
       //           |___|   
       
       tunnelMeetX = randomValueBetween(leftChild.getRoom().getX(), Math.min(rightChild.getRoom().getX(), leftChild.getRoom().getMaxX() - 1));
       tunnelMeetY = randomValueBetween(rightChild.getRoom().getY() + 1, rightChild.getRoom().getMaxY());
       digger.DigUpRightLCorridor(tunnelMeetX, tunnelMeetY, tunnelMeetX, tunnelMeetY);
     }
   }
   
   public void defaultCorridorGenerator(DungeonBSPNode dungeonNode) {
     DungeonBSPNode leftChild  = dungeonNode.leftChild;
     DungeonBSPNode rightChild = dungeonNode.rightChild;
     CorridorDigger digger     = new CorridorDigger(this.level);
     
     if (leftChild == null || !leftChild.haveRoom()) {
       dungeonNode.setRoom(rightChild.getRoom());
     } else if (rightChild == null || !rightChild.haveRoom()) {
       dungeonNode.setRoom(leftChild.getRoom());
     } else {
       if (dungeonNode.isHorizontal()) {
         int minOverlappingY = (int) Math.max(leftChild.getRoom().getMinY(), rightChild.getRoom().getMinY());
         int maxOverlappingY = (int) Math.min(leftChild.getRoom().getMaxY(), rightChild.getRoom().getMaxY());
         
         if (maxOverlappingY - minOverlappingY >= 3) {
           int corridorY = minOverlappingY + 1 + this.random.nextInt(maxOverlappingY - minOverlappingY - 2);
           
           digger.dig(leftChild.getRoom().getMaxX(), corridorY, CorridorDigger.LEFT_DIRECTION, 0, true);
           digger.dig(leftChild.getRoom().getMaxX() + 1, corridorY, CorridorDigger.RIGHT_DIRECTION, 0, true);
         } else {
           connectZShapeHorizontal(digger, leftChild, rightChild);
         }
       } else {
         int minOverlappingX = (int) Math.max(leftChild.getRoom().getX(), rightChild.getRoom().getX());
         int maxOverlappingX = (int) Math.min(leftChild.getRoom().getMaxX(), rightChild.getRoom().getMaxX());
         if (maxOverlappingX - minOverlappingX >= 3) {
           int corridorX = minOverlappingX + 1 + this.random.nextInt(maxOverlappingX - minOverlappingX - 2);
 
           digger.dig(corridorX, (int) leftChild.getRoom().getMaxY(), CorridorDigger.UP_DIRECTION, 0, true);
           digger.dig(corridorX, (int) (leftChild.getRoom().getMaxY() + 1), CorridorDigger.DOWN_DIRECTION, 0, true);
         } else {
           connectZShapeVertical(digger, leftChild, rightChild);
         }
       }
       
       int sx = (int) Math.min(leftChild.getRoom().getX(), rightChild.getRoom().getX());
       int sy = (int) Math.min(leftChild.getRoom().getY(), rightChild.getRoom().getY());
       
       int ex = (int) Math.max(leftChild.getRoom().getMaxX(), rightChild.getRoom().getMaxX());
       int ey = (int) Math.max(leftChild.getRoom().getMaxY(), rightChild.getRoom().getMaxY());
       
       int width  = ex - sx;
       int height = ey - sy;
       Room room  = new Room(sx, sx, width, height);
       
       //dungeonNode.setRoom(room);
     }
   }
 
   private int randomValueBetween(float start, float end)  {
     return (int)start + random.nextInt((int)end - (int)start);
   }
   
   private void applyBedrockBorder() {
     Log.info("Adding bedrock border");
     currentStatus = "Adding bedrock";
     this.progress = 70;
     int y = this.level.mapTileHeight-1;
     int x = 0;
     for (x = 0; x < this.level.mapTileWidth; x++) {
       this.level.setBlock(x, 0, new Bedrock(x,0));
       this.level.setBlock(x, y, new Bedrock(x,y));
     }
     
     x = this.level.mapTileWidth-1;
     this.progress = 71;
     for (y = 0; y < this.level.mapTileHeight; y++) {
       this.level.setBlock(0, y, new Bedrock(0,y));
       this.level.setBlock(x, y, new Bedrock(x,y));
     }
     
     this.progress = 75;
   }
   
   private void applyResources() throws SlickException {
     Log.info("Starting building world");
     this.progress = 5;
     currentStatus = "Filling with ground";
     fillWithGround();
 
     this.progress = 10;
     currentStatus = "Adding sand and water";
     applySandAndWater();
     this.progress = 15;
     Log.info("Building stone");
     this.progress = 22;
     currentStatus = "Adding stone";
     applyStone();
     Log.info("Building copper");
     this.progress = 25;
     currentStatus = "Adding copper";
     applyCopper();
     Log.info("Building coal");
     this.progress = 30;
     currentStatus = "Adding coal";
     applyCoal();
     Log.info("Building gold");
     this.progress = 35;
     currentStatus = "Adding gold";
     applyGold();
   }
 
   public void setSeed(int i) {
     this.seed = i;
     this.random = new Random(seed);
     this.progress = 0;
   }
   
 }
