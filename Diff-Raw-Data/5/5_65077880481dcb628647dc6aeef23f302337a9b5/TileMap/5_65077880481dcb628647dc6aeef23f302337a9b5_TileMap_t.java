 package vooga.rts.map;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 import java.util.Map;
 import vooga.rts.IGameLoop;
 import vooga.rts.gamedesign.sprite.map.Tile;
 import vooga.rts.util.Camera;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.TimeIt;
 
 
 /**
  * This class is responsible for the underlying tile system of the map.
  * The tiles are the images that only provide a visual representation
  * of the lowest level of the game map.
  * These are things such as grass, water, etc.
  * 
  * @author Jonathan Schmidt
  * 
  */
 public class TileMap implements IGameLoop {
 
     private int myWidth;
     private int myHeight;
 
     private Dimension myTileSize;
 
     private Dimension myMapSize;
 
     private Map<Integer, BufferedImage> myTileTypes;
 
     private Tile[][] myTiles;
 
     /**
      * Creates a new Tile Map with the specified properties.
      * 
      * @param tileSize The size of each tile in the map.
      *        All tiles are the same size.
      * @param width The number of tiles in the X direction.
      * @param height The number of tiles in the Y direction.
      */
     public TileMap (Dimension tileSize, int width, int height) {
         myWidth = width; 
         myHeight = height;
         myTileSize = tileSize;
         myMapSize =
                 new Dimension((int) (myWidth * myTileSize.getWidth()),
                               (int) (myHeight * myTileSize.getHeight()));
         myTileTypes = new HashMap<Integer, BufferedImage>();
         myTiles = new Tile[myWidth][myHeight];
     }
 
     /**
      * Adds a new type of tile to the tiles that the map
      * supports.
      * 
      * @param id The ID of the tile.
      * @param image The image of the tile that will be used to
      *        visually represent the tile.
      */
     public void addTileType (int id, BufferedImage image) {
         if (image != null) {
             myTileTypes.put(id, image);
         }
     }
 
     /**
      * Creates an actual tile for use in the map.
      * Uses the image already loaded from the addTileType method.
      * 
      * @param id The type of tile to create.
      * @param x The X index that this tile represents on the map.
      * @param y The Y index that this tile represents on the map.
      */
     public void createTile (int tiletype, int x, int y) {
         if (x < 0 || y < 0 || x >= myWidth || y >= myHeight) {
             return;
         }
         BufferedImage pic = myTileTypes.get(tiletype);
         Pixmap image = new Pixmap(pic);
 
         Location3D position =
                 new Location3D(x * myTileSize.width / 2 ,
                                y * myTileSize.height / 2, 0);
 
         Tile newTile = new Tile(image, position, myTileSize);
         setTile(x, y, newTile);
     }
 
     /**
      * Helper method to get the tile at a specific location. <br />
      * This removes the way that tiles are actually stored from
      * the rest of the classes.
      * 
      * @param x The X index of the tile
      * @param y The Y index of the tile
      * @return The Tile at the specified location.
      */
     public Tile getTile (int x, int y) {
         if (x < 0 || y < 0 || x >= myWidth || y >= myHeight) {
             return null;
         }
         return myTiles[x][y];
     }
 
     /**
      * Helper method to set the tile of a specific location. <br />
      * This removes the way that tiles are actually stored from
      * the rest of the classes.
      * 
      * @param x The X index of the tile
      * @param y The Y index of the tile
      * @param toset The tile to be placed at the location.
      */
     public void setTile (int x, int y, Tile toset) {
         if (x < 0 || y < 0 || x >= myWidth || y >= myHeight) {
             return;
         }
         myTiles[x][y] = toset;
     }
 
     @Override
     public void update (double elapsedTime) {
         for (int x = 0; x < myWidth; x++) {
             for (int y = 0; y < myHeight; y++) {
                 Tile cur = getTile(x, y);
                 if (cur != null) {
                     cur.update(elapsedTime);
                 }
             }
         }
     }
 
     @Override
     public void paint (Graphics2D pen) {
         Rectangle view = Camera.instance().getWorldVision().getBounds();
 
         // Get the start index of what is visible by the cameras.
         int startX = (int) (view.getMinX() > 0 ? view.getMinX() : 0);
         startX /= myTileSize.getWidth();
         startX /= Camera.ISO_HEIGHT;
 
         int startY = (int) (view.getMinY() > 0 ? view.getMinY() : 0);
         startY /= myTileSize.getHeight();
         startY /= Camera.ISO_HEIGHT;
 
         // Get the end index of what is visible
         int endX =
                 (int) (view.getMaxX() < myMapSize.getWidth() ? view.getMaxX() : myMapSize
                         .getWidth());
         endX /= myTileSize.getWidth();
         endX /= Camera.ISO_HEIGHT;
        endX = endX < myWidth ? endX : myWidth;
 
         int endY =
                 (int) (view.getMaxY() < myMapSize.getHeight() ? view.getMaxY() : myMapSize
                         .getHeight());
         endY /= myTileSize.getHeight();
         endY /= Camera.ISO_HEIGHT;
        endY = endY < myHeight ? endY : myHeight;
 
         for (int x = startX; x < endX; x++) {
             for (int y = startY; y < endY; y++) {
                 Tile cur = myTiles[x][y];
                 if (cur != null) {
                     cur.paint(pen);
                 }
             }
         }
     }
     
     public int getMyWidth() {
         return myWidth;
     }
     
     public int getMyHeight() {
         return myHeight;
     }
     
     public Dimension getMyTileSize() {
         return myTileSize;
     }
 }
