 package net.stuffrepos.tactics16.components;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
import java.awt.Polygon;
 import net.stuffrepos.tactics16.Layout;
 import net.stuffrepos.tactics16.game.Coordinate;
 import net.stuffrepos.tactics16.game.Map;
 import net.stuffrepos.tactics16.game.Terrain;
 import net.stuffrepos.tactics16.util.cache.CacheableValue;
 import net.stuffrepos.tactics16.util.image.ColorUtil;
import net.stuffrepos.tactics16.util.image.DrawerUtil;
 import net.stuffrepos.tactics16.util.listeners.Listener;
 
 /**
  *
  * @author Eduardo H. Bogoni <eduardobogoni@gmail.com>
  */
 public class VisualMap extends AbstractObject2D {
 
     private static final int SIDE_SIZE = 5;
     private static final Color LEFT_COLOR = Color.gray;
     private static final Color FRONT_COLOR = ColorUtil.dark(LEFT_COLOR);
     private static final Color RIGHT_COLOR = FRONT_COLOR;
     private long elapsedTime = 0l;
     private final CacheableValue<Polygon> leftBorder = new CacheableValue<Polygon>() {
 
         @Override
         protected Polygon calculate() {
             Polygon p = new Polygon();
             p.addPoint(SIDE_SIZE, 0);
             p.addPoint(0, SIDE_SIZE);
             p.addPoint(0, getHeight());
             p.addPoint(SIDE_SIZE, getHeight() - SIDE_SIZE);
            p.translate(getLeft(), getTop());
            return p;
         }
     };
     private final CacheableValue<Polygon> rightBorder = new CacheableValue<Polygon>() {
 
         @Override
         protected Polygon calculate() {
             Polygon p = new Polygon();
             p.addPoint(0, 0);
             p.addPoint(SIDE_SIZE, SIDE_SIZE);
             p.addPoint(SIDE_SIZE, getHeight());
             p.addPoint(0, getHeight() - SIDE_SIZE);
            p.translate(getLeft() + SIDE_SIZE + internalMap.getWidth(), getTop());
            return p;
         }
     };
     private final Map map;
     private final InternalMap internalMap;
 
     public VisualMap(Map map) {
         this.map = map;
         this.internalMap = new InternalMap();
 
         getPosition().addListener(new Listener<Coordinate>() {
 
             public void onChange(Coordinate source) {
                 internalMap.getPosition().set(getPosition(), SIDE_SIZE, 0);
                 leftBorder.clear();
                 rightBorder.clear();
             }
         });
     }
 
     public Object2D getInternalVisualMap() {
         return internalMap;
     }
 
     public Coordinate getPersonPosition(Coordinate terrainPosition) {
         return internalMap.getPersonPosition(terrainPosition);
     }
 
     public Coordinate getTerrainPosition(Coordinate terrainPosition) {
         return internalMap.getTerrainPosition(terrainPosition);
     }
 
     public void render(final Graphics g) {
         g.setColor(FRONT_COLOR);
         g.fillRect(getLeft(), Layout.getBottom(internalMap), getWidth(), SIDE_SIZE);
         g.setColor(LEFT_COLOR);
        DrawerUtil.fillPolygon(g,leftBorder.getValue());        
         g.setColor(RIGHT_COLOR);
        DrawerUtil.fillPolygon(g,rightBorder.getValue());        
 
         internalMap.render(g);
     }
 
     public Map getMap() {
         return map;
     }
 
     public void render(final Graphics g, int[] playerColors) {
         internalMap.render(g, playerColors);
     }
 
     public void update(long elapsedTime) {
         this.elapsedTime += elapsedTime;
         internalMap.update(elapsedTime);
     }
 
     @Override
     protected int calculateWidth() {
         return internalMap.getWidth() + SIDE_SIZE * 2;
     }
 
     @Override
     protected int calculateHeight() {
         return internalMap.getHeight() + SIDE_SIZE;
     }
 
     private class InternalMap extends AbstractObject2D {
 
         public Coordinate getPersonPosition(Coordinate terrainPosition) {
             return new Coordinate(
                     getLeft() + terrainPosition.getX() * Map.TERRAIN_SIZE + (Map.TERRAIN_SIZE / 2),
                     getTop() + terrainPosition.getY() * Map.TERRAIN_SIZE + (Map.TERRAIN_SIZE / 2));
         }
 
         public Coordinate getTerrainPosition(Coordinate terrainPosition) {
             return new Coordinate(
                     getLeft() + terrainPosition.getX() * Map.TERRAIN_SIZE,
                     getTop() + terrainPosition.getY() * Map.TERRAIN_SIZE);
         }
 
         public void render(final Graphics g) {
             render(g, null);
         }
 
         public void render(final Graphics g, int[] playerColors) {
             map.iterate(new Map.Iterator() {
 
                 public void check(int x, int y, Terrain terrain) {
                     if (terrain != null) {
                         map.getTerrainImage(terrain,elapsedTime).render(
                                 g,
                                 getLeft() + x * Map.TERRAIN_SIZE,
                                 getTop() + y * Map.TERRAIN_SIZE);
                     } else {
                         g.setColor(Color.black);
                         g.drawRect(
                                 getLeft() + x * Map.TERRAIN_SIZE,
                                 getTop() + y * Map.TERRAIN_SIZE,
                                 Map.TERRAIN_SIZE - 1,
                                 Map.TERRAIN_SIZE - 1);
                     }
                 }
             });
             if (playerColors != null) {
                 for (Coordinate personPosition : map.getPersonInitialPositions().getAllPositions()) {
                     g.setColor(new Color(playerColors[map.getPersonInitialPositions().getPlayerFromPosition(personPosition)]));
                     g.fillRect(
                             getLeft() + personPosition.getX() * Map.TERRAIN_SIZE,
                             getTop() + personPosition.getY() * Map.TERRAIN_SIZE,
                             Map.TERRAIN_SIZE,
                             Map.TERRAIN_SIZE);
 
                 }
             }
 
         }
 
         public void update(long elapsedTime) {
             
         }
 
         @Override
         protected int calculateWidth() {
             return map.getWidth() * Map.TERRAIN_SIZE;
         }
 
         @Override
         protected int calculateHeight() {
             return map.getHeight() * Map.TERRAIN_SIZE;
         }
     }
 }
