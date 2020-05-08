 package com.willbat.MotherlAndroid;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.BoundingBox;
 
 /**
  * This class contains the map of tiles that is the rendered game world
  * TODO: This will also contain the code used to generate the world and distribute minerals
  * TODO: add conditional constructors for loading maps
  * TODO: Maps could be stored as JSON objects, but it might make more sense just to store arrays of numbers and use the tiletype enum
  */
 public class Map
 {
     Tile[][][] tiles;
     Chunk currentChunk;
 
     public Map(int width, int height)
     {
         tiles = new Tile[3][height][width];
         generateMap();
     }
 
     public Map(String filename)
     {
 
     }
 
     public void update(Vector3 playerPosition)
     {
 //        if (playerPosition.y )
 //        {
 //
 //        }
     }
 
     private void generateMap()
     {
         //int layerNumber = 0;
         for (Tile[][] columnRow : tiles)
         {
             int rowNumber = 0;
             for (Tile[] row : columnRow)
             {
                 int columnNumber = 0;
                 for (Tile tile : row)
                 {
                     if (rowNumber < 2)
                     {
                         row[columnNumber] = new Tile(Tiletype.AIR, columnNumber, rowNumber);
                     }
                     else if (rowNumber > 2 && rowNumber < 5)
                     {
                         row[columnNumber] = new Tile(Tiletype.DIRT, columnNumber, rowNumber);
                     }
                     else
                     {
                         row[columnNumber] = new Tile(Tiletype.COAL, columnNumber, rowNumber);
                     }
                     columnNumber++;
                 }
                 rowNumber++;
             }
             //layerNumber++;
         }
     }
 
     public void draw(SpriteBatch batch, ExtendedCamera camera)
     {
         int layerNumber = 0;
         for (Tile[][] columnRow : tiles)
         {
             int columnNumber = 0;
             for (Tile[] row : columnRow)
             {
                 int rowNumber = 0;
                 for (Tile tile : row)
                 {
                     tile.isDrawn = false;
                     if (camera.frustum.boundsInFrustum(convertRectToBoundingBox(tile.tileSprite.getBoundingRectangle())))
                     {
                         if (layerNumber == 0)
                         {
                             if (tile != null && !tiles[layerNumber+2][columnNumber][rowNumber].visible && !tiles[layerNumber+1][columnNumber][rowNumber].visible && tile.visible)
                             {
                                 tile.draw(batch);
                                 tile.isDrawn = true;
                             }
                         }
                         else if (layerNumber == 1)
                         {
                             if (tile != null && !tiles[layerNumber+1][columnNumber][rowNumber].visible && tile.visible)
                             {
                                 tile.draw(batch);
                                 tile.isDrawn = true;
                             }
                         }
                         else if (layerNumber == 2)
                         {
                             if (tile != null && tile.visible)
                             {
                                 tile.draw(batch);
                                 tile.isDrawn = true;
                             }
                         }
                     }
                     rowNumber++;
                 }
                 columnNumber++;
             }
             layerNumber++;
         }
     }
     private BoundingBox convertRectToBoundingBox(Rectangle rectangle)
     {
        BoundingBox bBox = new BoundingBox(new Vector3(rectangle.getX(), rectangle.getY(), 0), new Vector3(rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight(), 0));
         return bBox;
     }
 
     private class Chunk
     {
         protected Vector3 size;
         public Chunk()
         {
             size = new Vector3(10,10,3);
         }
     }
 }
