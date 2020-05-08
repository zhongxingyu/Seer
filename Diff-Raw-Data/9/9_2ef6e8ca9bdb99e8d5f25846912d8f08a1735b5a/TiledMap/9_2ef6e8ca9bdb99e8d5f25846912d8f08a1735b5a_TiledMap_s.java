 /*
  * Copyright (C) 2013 Lasse Dissing Hansen
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  */
 
 package volpes.ldk.client;
 
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 /**
 * @auther Lasse Dissing Hansen
  */
 public class TiledMap {
 
     private int width;
     private int height;
     private int tileWidth;
     private int tileHeight;
 
     private List<TileSet> tileSets;
     private List<Layer> layers;
 
     TiledMap(int width, int height, int tileWidth, int tileHeight, List<Layer> layers , List<TileSet> tileSets) {
         this.width = width;
         this.height = height;
         this.tileWidth = tileWidth;
         this.tileHeight = tileHeight;
         this.layers = layers;
         this.tileSets = tileSets;
     }
 
 
     public int getTile(int x, int y, int layer) {
         return layers.get(layer).tiles.get(x + y*width);
     }
 
 
     public void setTile(int tile, int x, int y, int layer) {
         layers.get(layer).tiles.set(x + y*width, tile);
     }
 
     public int getWidth() {
         return width;
     }
 
     public int getHeight() {
         return height;
     }
 
     public void cacheLayer(boolean cache, int layer) {
         layers.get(layer).cache = cache;
     }
 
     public void render(GameContainer container, Render g, int xOffset, int yOffset) {
         for (int layer = 0; layer < layers.size(); layer++) {
             if (!layers.get(layer).cache) {
                 drawLayer(g,layer,xOffset,yOffset);
             } else {
                 g.drawImage(layers.get(layer).cachedImage,xOffset,yOffset);
             }
         }
     }
 
     /**
      * Draws a single layer to a Graphics
      * @param g The Graphics to draw to
      * @param layer The layer to render
      */
     private void drawLayer(Render g,int layer,int xOffset, int yOffset) {
         for (int i = 0; i < width; i++) {
             for (int j = 0; j < height; j++) {
                 int tile = getTile(i,j,layer);
                 if (tile != 0) {
                     g.drawImage(getTileImg(tile), i*tileWidth+xOffset, j*tileHeight+yOffset);
                 }
             }
         }
     }
 
     private BufferedImage getTileImg(int index) {
         int size = width*height;
         return tileSets.get(index / size).get(index % size);
     }
 
 
 }
