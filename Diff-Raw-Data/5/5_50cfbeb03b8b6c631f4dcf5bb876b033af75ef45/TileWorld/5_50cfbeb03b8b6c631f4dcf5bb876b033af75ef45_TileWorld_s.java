 package com.gravity.map;
 
 import java.util.List;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.google.common.collect.Lists;
 import com.gravity.gameplay.GravityGameController;
 import com.gravity.physics.Entity;
 import com.gravity.physics.SpikeEntity;
 import com.gravity.physics.TileWorldEntity;
 
 public class TileWorld implements GameWorld {
     public final int height;
     public final int width;
     
     public final int tileHeight;
     public final int tileWidth;
     
     private List<Entity> entityNoCalls, entityCallColls;
     
     private TiledMap map;
     
     private final int TILES_LAYER_ID;
     private final int SPIKES_LAYER_ID;
     
     public TileWorld(TiledMap map, GravityGameController controller) {
         TILES_LAYER_ID = map.getLayerIndex("collisions");
         SPIKES_LAYER_ID = map.getLayerIndex("spikes");
         
         // Get width/height
         this.tileWidth = map.getTileWidth();
         this.tileHeight = map.getTileHeight();
         this.width = map.getWidth() * tileWidth;
         this.height = map.getHeight() * tileHeight;
         
         this.map = map;
         
         // Iterate over and find all tiles
         int layerId = TILES_LAYER_ID; // Layer ID to search at
         entityNoCalls = Lists.newArrayList();
         entityCallColls = Lists.newArrayList();
         for (int i = 0; i < map.getWidth(); i++) {
             for (int j = 0; j < map.getHeight(); j++) {
                 int tileId = map.getTileId(i, j, layerId);
                 if (tileId != 0) {
                     // Tile exists at this spot
                     Rectangle r = new Rectangle(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
                     Entity e = new TileWorldEntity(r);
                     
                     entityNoCalls.add(e);
                 }
             }
         }
         
         layerId = SPIKES_LAYER_ID;
         for (int i = 0; i < map.getWidth(); i++) {
             for (int j = 0; j < map.getHeight(); j++) {
                 int tileId = map.getTileId(i, j, layerId);
                 if (tileId != 0) {
                     // Tile exists at this spot
                     Rectangle r = new Rectangle(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
                     Entity e = new SpikeEntity(controller, r);
                     
                     entityCallColls.add(e);
                 }
             }
         }
         
     }
     
     @Override
     public List<Entity> getCollisions(Shape shape) {
         List<Entity> collisions = Lists.newArrayList();
         for (Entity ent : entityNoCalls) {
             if (shape.intersects(ent.getShape(0))) {
                 collisions.add(ent);
             }
         }
         return collisions;
     }
     
     @Override
     public List<Shape> getTouching(Shape shape) {
         List<Shape> touches = Lists.newArrayList();
         for (Entity terrain : entityNoCalls) {
             if (shape.intersects(terrain.getShape(0))) {
                 touches.add(terrain.getShape(0));
             }
         }
         return touches;
     }
     
     @Override
     public int getHeight() {
         return height;
     }
     
     @Override
     public int getWidth() {
         return width;
     }
     
     @Override
     public List<Entity> getTerrainEntitiesNoCalls() {
         return entityNoCalls;
     }
     
     @Override
     public void render(Graphics g, int offsetX, int offsetY) {
         /*
          * // if we need to draw hitboxes again: g.pushTransform(); g.translate(offsetX, offsetY); g.setColor(Color.red); for (Entity e :
          * entityNoCalls) { g.draw(e.getShape(0)); } g.setColor(Color.white); g.resetTransform(); g.popTransform();
          */
         map.render(offsetX, offsetY);
     }
     
     @Override
     public List<Entity> getTerrainEntitiesCallColls() {
         return entityCallColls;
     }
 }
