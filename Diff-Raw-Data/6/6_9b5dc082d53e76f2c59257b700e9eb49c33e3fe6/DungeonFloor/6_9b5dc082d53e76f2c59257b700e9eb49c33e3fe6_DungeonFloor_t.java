 package aigilas.dungeons;
 
 import aigilas.Common;
 import aigilas.Config;
 import aigilas.creatures.impl.CreatureFactory;
 import aigilas.creatures.impl.Player;
 import aigilas.entities.*;
 import aigilas.gods.GodId;
 import aigilas.items.ItemFactory;
 import aigilas.net.Client;
 import sps.bridge.ActorTypes;
 import sps.bridge.EntityTypes;
 import sps.core.Core;
 import sps.core.Point2;
 import sps.core.RNG;
 import sps.core.Settings;
 import sps.entities.Entity;
 import sps.entities.EntityManager;
 import sps.entities.IActor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DungeonFloor {
     private static int enemyCapModifier = 0;
     private static int enemyBaseModifier = 0;
     private int playerCount = Client.get().getPlayerCount();
 
     private List<Entity> _contents = new ArrayList<Entity>();
     private final Entity[][] dungeon = new Entity[Settings.get().tileMapWidth][Settings.get().tileMapHeight];
     private final Point2 _upSpawnLocation = new Point2(0, 0);
     private final Point2 _downSpawnLocation = new Point2(0, 0);
 
     public static void reset() {
         enemyBaseModifier = 0;
         enemyCapModifier = 0;
     }
 
     public DungeonFloor() {
         generateRooms(true);
         placeAltars();
         placeStairs();
         transferDungeonState();
     }
 
     public DungeonFloor(Point2 upstairsSpawn) {
         enemyCapModifier++;
         enemyBaseModifier = Config.get().enemyBase + enemyCapModifier / 5;
         EntityManager.get().clear();
         generateRooms(false);
         _upSpawnLocation.copy(upstairsSpawn);
         placeStairs();
         int enemiesToPlace = RNG.next(Config.get().enemyBase + enemyBaseModifier, Config.get().enemyCap + enemyCapModifier);
         if (enemiesToPlace <= 0) {
             enemiesToPlace = 1;
         }
         placeCreatures(enemiesToPlace);
         placeItems(RNG.next(Config.get().itemBase, Config.get().itemCap));
         transferDungeonState();
     }
 
     private void placeAltars() {
         int startY = Settings.get().tileMapHeight / 2;
         int startX = Settings.get().tileMapWidth / 3 - 1;
         for (GodId god : GodId.values()) {
             dungeon[startX][startY] = new Altar(new Point2(startX, startY), god);
             startX += 2;
         }
 
         dungeon[Settings.get().tileMapWidth / 2][startY - 2] = CreatureFactory.create(ActorTypes.get(Common.Dummy), new Point2(Settings.get().tileMapWidth / 2, startY - 2));
     }
 
     private List<Entity> playerCache;
 
     public void loadTiles(boolean goingUp) {
         EntityManager.get().clear();
         placeFloor();
         playerCache = Dungeon.flushCache();
         Point2 spawn = goingUp ? _downSpawnLocation : _upSpawnLocation;
         List<Point2> neighbors = spawn.getNeighbors();
         for (Entity player : playerCache) {
             Player pl = ((Player) player);
             pl.setLocation(getRandomNeighbor(neighbors));
             _contents.add(pl);
         }
         for (Entity item : _contents) {
             EntityManager.get().addObject(item);
         }
        //Force an update so that tiles with dynamic sprites render correctly without any flicker
        EntityManager.get().update();
     }
 
     public void cacheContents() {
         for (IActor player : EntityManager.get().getActors(ActorTypes.get(Core.Player))) {
             Dungeon.addToCache((Entity) player);
             EntityManager.get().removeObject((Entity) player);
         }
         _contents = new ArrayList<Entity>(EntityManager.get().getEntitiesToCache());
     }
 
     private void transferDungeonState() {
         for (Entity[] row : dungeon) {
             for (Entity tile : row) {
                 if (tile != null) {
                     if (tile.getEntityType() != EntityTypes.get(Core.Floor)) {
                         _contents.add(tile);
                     }
                     EntityManager.get().addObject(tile);
                 }
             }
         }
 
         List<Entity> cache = Dungeon.flushCache();
         List<Point2> neighbors = _upSpawnLocation.getNeighbors();
 
         if (cache.size() == 0) {
             if (Config.get().debugFourPlayers) {
                 playerCount = 4;
             }
             for (int ii = 0; ii < playerCount; ii++) {
                 _contents.add(CreatureFactory.create(ActorTypes.get(Core.Player), getRandomNeighbor(neighbors)));
             }
         }
         else {
             for (Entity player : cache) {
                 player.setLocation(getRandomNeighbor(neighbors));
             }
             EntityManager.get().addObjects(cache);
             _contents.addAll(cache);
         }
     }
 
     private Point2 findRandomFreeTile() {
         return findRandomFreeTile(1);
     }
 
     private Point2 findRandomFreeTile(int buffer) {
         while (true) {
             int x = RNG.next(buffer, Settings.get().tileMapWidth - buffer);
             int y = RNG.next(buffer, Settings.get().tileMapHeight - buffer);
             if (dungeon[x][y].getEntityType() == EntityTypes.get(Core.Floor)) {
                 return new Point2(x, y);
             }
         }
     }
 
     Point2 neighborTemp = new Point2(0, 0);
 
     private Point2 getRandomNeighbor(List<Point2> neighbors) {
         while (neighbors.size() > 0) {
             int neighborIndex = RNG.next(0, neighbors.size());
             neighborTemp = neighbors.get(neighborIndex);
             neighbors.remove(neighborIndex);
             if (!dungeon[neighborTemp.GridX][neighborTemp.GridY].isBlocking()) {
                 return neighborTemp;
             }
         }
         return neighborTemp;
     }
 
     private void generateRooms(boolean altarRoom) {
         for (Tile tile : new FloorPlan(altarRoom).getTiles()) {
             dungeon[tile.X][tile.Y] = EntityFactory.create(tile.EntityType, tile.Position);
         }
     }
 
     private void placeFloor() {
         for (int ii = 1; ii < Settings.get().tileMapWidth - 1; ii++) {
             for (int jj = 1; jj < Settings.get().tileMapHeight - 1; jj++) {
                 EntityManager.get().addObject(new Floor(new Point2(ii, jj)));
             }
         }
     }
 
     private void placeCreatures(int amountOfCreatures) {
 //        $$$ Easiest way to test specific bosses
 //        Point2 random = new Point2(findRandomFreeTile());
 //        dungeon[random.GridX][random.GridY] = CreatureFactory.create(ActorTypes.get(Core.Player)Sloth, random);
 //        while(CreatureFactory.bossesRemaining() > 0){
 //                CreatureFactory.createNextBoss(Point2.Zero);
 //        }
 //
 //        return;
 
         if (Config.get().bossLevelMod <= 1 || Dungeon.getFloorCount() % Config.get().bossLevelMod == 1) {
             Point2 randomPoint = new Point2(findRandomFreeTile());
             dungeon[randomPoint.GridX][randomPoint.GridY] = CreatureFactory.createNextBoss(randomPoint);
         }
         else {
             while (amountOfCreatures > 0) {
                 amountOfCreatures--;
                 Point2 randomPoint = new Point2(findRandomFreeTile());
                 dungeon[randomPoint.GridX][randomPoint.GridY] = CreatureFactory.createRandom(randomPoint);
             }
         }
     }
 
     private void placeStairs() {
         placeUpstairs();
         placeDownstairs();
     }
 
     private void placeDownstairs() {
         _downSpawnLocation.copy(findRandomFreeTile(2));
         dungeon[_downSpawnLocation.GridX][_downSpawnLocation.GridY] = new Downstairs(_downSpawnLocation);
     }
 
     private void placeUpstairs() {
         if (_upSpawnLocation.isZero()) {
             _upSpawnLocation.copy(findRandomFreeTile(2));
         }
         dungeon[_upSpawnLocation.GridX][_upSpawnLocation.GridY] = new Upstairs(_upSpawnLocation);
     }
 
     private void placeItems(int amountToPlace) {
         while (amountToPlace > 0) {
             amountToPlace--;
             Point2 randomPoint = findRandomFreeTile();
             dungeon[randomPoint.GridX][randomPoint.GridY] = ItemFactory.createRandomPlain(randomPoint);
         }
     }
 
     public Point2 getDownstairsLocation() {
         return _downSpawnLocation;
     }
 }
