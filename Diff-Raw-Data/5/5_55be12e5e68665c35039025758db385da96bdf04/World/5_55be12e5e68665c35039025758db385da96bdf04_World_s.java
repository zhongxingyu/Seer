 package platformer;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class World {
     public Main main;
     public WorldGenerator worldgenerator;
     public ChunkHandler chunkhandler;
     public List<Entity> entities;
     public List<int[]> scheduledTicks;
     public EntityPlayerSP thePlayer;
     public final int chunkSize;
     public int chunkupdates;
     public Random random;
     public int randomTick;
     public HUD hud;
     
     public World (Main main)
     {
         scheduledTicks = new ArrayList<int[]>();
         hud = new HUD(this);
         randomTick = 0;
         chunkupdates = 0;
         chunkSize = 16;
         this.main = main;
         worldgenerator = new WorldGenerator(this);
         chunkhandler   = new ChunkHandler(this, worldgenerator);
         entities       = new ArrayList<Entity>();
         thePlayer      = new EntityPlayerSP(this);
         random         = new Random(worldgenerator.seed);
         addEntity(thePlayer);
     }
     public void addEntity (Entity entity)
     {
         entity.spawned();
         entities.add(entity);
     }
     public void removeEntity (Entity entity)
     {
         entity.removed();
         entities.remove(entity);
     }
     public void tick (double delta)
     {
         hud.tick(delta);
         randomTick += delta;
         if (randomTick > 100) randomTick -= 100;
         boolean doTick = true;
         for (int i = entities.size() - 1; i >= 0; i--) {
             Entity entity = entities.get(i);
             entity.tick(delta);
             if (entity.dead) removeEntity(entity);
         }
         for (Chunk chunk : chunkhandler.chunks) {
             if (chunk.needsupdate) {
                 chunk.chunkUpdate();
                 chunk.needsupdate = false;
                 chunkupdates++;
             }
             if (doTick) {
                 int x = random.nextInt(chunkSize);
                 int y = random.nextInt(chunkSize);
                 chunk.getTileRelative(x, y).onRandomTick(this,
                         x + chunk.x * chunkSize + 1,
                         y + chunk.y * chunkSize + 1);
             }
         }
         if (doTick) {
             Tile tile;
             List<int[]> scheduledTicks = new ArrayList<int[]>(this.scheduledTicks);
             this.scheduledTicks.clear();
             for (int[] info : scheduledTicks) {
                 if (info[2] > 0) {
                     scheduleTick(info[0], info[1], info[2] - 1);
                     continue;
                 }
                 tile = getTile(info[0], info[1]);
                 if (tile != null) tile.onScheduledTick(this, info[0], info[1]);
             }
         }
         /*chunkhandler.generateChunk(thePlayer.getChunkX(),
                                    thePlayer.getChunkY());*/
         chunkhandler.generateChunksAround(thePlayer.getChunkX(),
                                           thePlayer.getChunkY(),
                                           (int) Math.ceil(Math.max(Utilities.getWidth(), Utilities.getHeight()) / chunkSize / 16) + 1);
     }
     public void render ()
     {
         int tx = (int) -Math.floor(thePlayer.x) + Utilities.getWidth()  / 2;
         int ty = (int) -Math.floor(thePlayer.y) - Utilities.getHeight() / 2;
         Utilities.translateFromOrigin(tx, ty);
         
         glBegin(GL_QUADS);
             glColor3d(0.7, 0.7, 1.0);
             glVertex2d(-tx, -ty);
             glVertex2d(-tx + Utilities.getWidth(), -ty);
             
             glColor3d(0.8, 0.8, 1.0);
             glVertex2d(-tx + Utilities.getWidth(), -ty - Utilities.getHeight());
             glVertex2d(-tx, -ty - Utilities.getHeight());
         glEnd();
         
         for (Chunk chunk : chunkhandler.getChunks()) {
             chunk.render();
         }
         for (Entity entity : entities) {
             entity.render();
         }
         hud.render();
     }
     public Tile getTile(int x, int y)
     {
         int cx = exactToChunkCord(x * 16);
         int cy = exactToChunkCord(y * 16);
         Chunk chunk = chunkhandler.getChunk(cx, cy);
         if (chunk == null) return null;
         return chunk.getTileAbsolute(x, y);
     }
     public int exactToChunkCord(double c)
     {
         return (int) Math.ceil(c / chunkSize / 16) - 1;
     }
     public Tile setTile(int x, int y, Tile tile, boolean particles)
     {
         Tile tile2 = setTile(x, y, tile);
         if (particles && tile2 != null) {
             int amount = 10;
             if (entities.size() > 200) amount = 7;
             if (entities.size() > 500) amount = 3;
             if (entities.size() > 700) amount = 1;
             if (entities.size() > 1500) amount = 0;
             for (int i = 0; i < amount; i++) {
                 addEntity(new EntityParticleBlock(this, tile2, x, y));
             }
         }
         return tile2;
     }
     public Tile setTileWithoutNotice(int x, int y, Tile tile)
     {
         int cx = exactToChunkCord(x * 16);
         int cy = exactToChunkCord(y * 16);
         Chunk chunk = chunkhandler.getChunk(cx, cy);
         if (chunk == null) return null;
         Tile tile2 = chunk.setTileAbsolute(x, y, tile);
         if (tile2.id == tile.id) return null;
         return tile2;
     }
     public Tile setTile(int x, int y, Tile tile)
     {
         Tile tile2 = setTileWithoutNotice(x, y, tile);
         if (tile2 == null || tile2.id == tile.id) return null; 
         tile2.onBlockRemoval(this, x, y);
         Tile tile3;
         tile.onPlacement(this, x, y);
         tile3 = getTile(x+1, y);
         if (tile3 != null) tile3.onNeighbourTileChange(this, x+1, y, x, y);
         tile3 = getTile(x-1, y);
         if (tile3 != null) tile3.onNeighbourTileChange(this, x-1, y, x, y);
         tile3 = getTile(x, y+1);
         if (tile3 != null) tile3.onNeighbourTileChange(this, x, y+1, x, y);
         tile3 = getTile(x, y-1);
         if (tile3 != null) tile3.onNeighbourTileChange(this, x, y-1, x, y);
         return tile2;
     }
     public void scheduleTick (int x, int y, int ticks)
     {
         scheduledTicks.add(new int[]{x, y, ticks});
     }
     public boolean isEntityAt (int x, int y)
     {
         List<Entity> notColliding = new ArrayList<Entity>();
         for (Entity entity : entities) {
             if (entity instanceof EntityParticle) continue;
             if (!entity.isColliding()) notColliding.add(entity);
         }
         
        setTileWithoutNotice(x, y, Tile.dirt);
         
         boolean colliding = false;
         
         for (Entity entity : notColliding) if (entity.isColliding()) { colliding = true; break; }
        setTileWithoutNotice(x, y, Tile.air);
         return colliding;
     }
     public void explosion(int x, int y, int hardness)
     {
         for (int xa = -(hardness / 2); xa <= (hardness / 2); xa++) {
             for (int ya = -(hardness / 2); ya <= (hardness / 2); ya++) {
                 int distance = (int) Math.abs(Math.sqrt(xa*xa + ya*ya));
                 if (distance <= (hardness / 2)) {
                     Tile tile = setTile(x + xa, y + ya, Tile.air, true);
                     if (tile != null) tile.onExplosion(this, x + xa, y + ya, x, y);
                 }
             }
         }
     }
 }
