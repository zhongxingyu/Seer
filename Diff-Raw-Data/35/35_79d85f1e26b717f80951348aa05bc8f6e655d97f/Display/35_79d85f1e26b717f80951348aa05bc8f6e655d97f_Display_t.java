 /*
  *   Copyright 2006 Martin B. Smith
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.mbs3.jkaraoke;
 
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
 import java.awt.image.*;
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class Display extends javax.swing.JPanel {
 
     /* For serialization of the class */
     protected static final long serialVersionUID = 1;
     
     /* This is the size of the array that we operate on.  We add an
     # additional border on the right and bottom edge of 6 and 12 pixels,
     # respectively, to allow for display shifting.  (It's not clear from
     # the spec which colour should be visible when the display is shifted
     # to the right or down.  We say it should be the border colour.)
     */
     public static final int CDG_FULL_WIDTH      = 306;
     public static final int CDG_FULL_HEIGHT     = 228;
 
     /* This is the size of the screen that is actually intended to be
     # visible.  It is the center area of CDG_FULL.  In addition to hiding
     # our additional border on the right and bottom, there is an official
     # border area on the top and left that is not meant to be visible.
     */
     public static final int CDG_DISPLAY_WIDTH   = 294;
     public static final int CDG_DISPLAY_HEIGHT  = 204;
 
     /* This is the size of the display as defined by the CDG specification.
     # The pixels in this region can be painted, and scrolling operations
     # rotate through this number of pixels.
     */
     public static final int CDG_SCROLL_WIDTH      = 300;
     public static final int CDG_SCROLL_HEIGHT     = 216;
     
     /* Screen tile positions
     # The viewable area of the screen (294x204) is divided into
     # 24 tiles (6x4 of 49x51 each). This is used to only update
     # those tiles which have changed on every screen update,
     # thus reducing the CPU load of screen updates. A bitmask of
     # tiles requiring update is held in cdgPlayer.UpdatedTiles.
     # This stores each of the 4 columns in separate bytes, with 6 bits used
     # to represent the 6 rows.
     */
     public static final int TILE_WIDTH              = 6;
     public static final int TILE_HEIGHT             = 12;
     public static final int TILES_PER_ROW           = CDG_FULL_WIDTH / TILE_WIDTH;
     public static final int TILES_PER_COL           = CDG_FULL_HEIGHT / TILE_HEIGHT;
 
     public static final int COLOR_TABLE_SIZE  = 16;
     
     ColorModel colorModel = generate4096DirectColorModel();
     
     private MemoryImageSource fSource;
     private Image fImage;
     
     protected int [] fPixels = new int [CDG_FULL_WIDTH * CDG_FULL_HEIGHT];;
     protected int [] fColorValues = new int [CDG_FULL_WIDTH * CDG_FULL_HEIGHT];
     protected int[] colorTable = new int[COLOR_TABLE_SIZE];
 
     public Display() {
         super();
         
         for(int i = 0; i < colorTable.length; i++)
             colorTable[i] = 0;
         
         // The MemoryImageSource creates an image from the array.
         fSource = new MemoryImageSource (CDG_FULL_WIDTH, CDG_FULL_HEIGHT, 
                 colorModel, fPixels, 0, CDG_FULL_WIDTH);
         
         // Set the flag for animations.
         fSource.setAnimated (true);
         
         // get an image from the memory image source
         /* To scale the image way up or down:
          * 
          * Image i = createImage (fSource);
          * fImage = i.getScaledInstance(CDG_FULL_WIDTH*2, CDG_FULL_HEIGHT*2, Image.SCALE_AREA_AVERAGING);
          */
         fImage = createImage(fSource);
 
         initGUI();
     }
     
     private void initGUI() {
         try {
             setPreferredSize(new Dimension(CDG_FULL_WIDTH, CDG_FULL_HEIGHT));
			this.addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent evt) {
					rootComponentResized(evt);
				}
			});
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private ColorModel generate4096DirectColorModel() {
         
         // We're storing colors in a 32 bit integer slot, but 
         // we only use 12 of it (optimize me please!)
         
         // [--unused--][---Red---][--Blue--][-Green--]
         // [bits 12-32][bits 8-11][bits 4-7][bits 0-3]
         
         int bits = 12;
         int rmask = 0xF00; int gmask = 0x0F0; int bmask = 0x00F;
         
         DirectColorModel dcm = new DirectColorModel(bits, rmask, gmask, bmask);
         return dcm;
     }
 
     /** Draw the image on the panel. **/
     public void paintComponent (Graphics g) {
       g.drawImage ( fImage, 0,0, this );
     }
     
     public void setBackgroundColor(int colorIndex)
     {
         for(int y = 1; y < TILES_PER_COL-1; y++)
             for(int x = 1; x < TILES_PER_ROW-1; x++)
                 setEntireTileColorValue(y, x, colorIndex);
         
     }
 
     public void setBorderColor(int colorIndex)
     {
         // top bar
         for(int x = 0; x < TILES_PER_ROW; x++)
             setEntireTileColorValue(0, x, colorIndex);
         //fSource.newPixels(0,0,CDG_FULL_WIDTH,TILE_HEIGHT);
         
         // bottom bar
         for(int x = 0; x < TILES_PER_ROW; x++)
             setEntireTileColorValue(TILES_PER_COL-1, x, colorIndex);
         //fSource.newPixels(0,TILE_HEIGHT*(TILES_PER_COL-1),CDG_FULL_WIDTH,TILE_HEIGHT);
         
         // left bar
         for(int y = 0; y < TILES_PER_COL; y++)
             setEntireTileColorValue(y, 0, colorIndex);
         //fSource.newPixels(0,0,TILE_WIDTH,CDG_FULL_HEIGHT);
         
         // right bar
         for(int y = 0; y < TILES_PER_COL; y++)
             setEntireTileColorValue(y, TILES_PER_ROW-1, colorIndex);
         //fSource.newPixels(TILE_WIDTH*(TILES_PER_ROW-1),0,TILE_WIDTH,CDG_FULL_HEIGHT);
     }
     
     public void setTileColor(int tileRow, int tileColumn, int colorIndex0, int colorIndex1, byte [] tilePixels, int flag)
     {
         try {
             //System.out.println("Going to spin through a tile at " + tileRow + "," + tileColumn + " tile pixel values");
             int row = TILE_HEIGHT*(tileRow+1);
             int column = TILE_WIDTH*(tileColumn+1);
             
             int indx = row*CDG_FULL_WIDTH + column;
 
             for(int i = 0; i < tilePixels.length; i++)
             {
                 //System.out.println("Accessing tilePixels[" + i + "]");
                 byte currentLine = (byte)(tilePixels[i] & 0x3F);
                 
                 int [] eachPixel = new int[6];
                 
                 
                 // chop off the rightmost bit, then the next, then the next...
                 for(int j = 0; j < eachPixel.length; j++)
                     eachPixel[j] = (currentLine >>> (5-j)) & 0x01;
                 
                 for(int j = 0; j < 6; j++)
                 {
                     int colorChoice = (eachPixel[j] == 0 ? colorIndex0 : colorIndex1 );
                     int newValue = (flag == Packet.INSTR_TILE_BLOCK_XOR ? colorChoice ^ fColorValues[indx]: colorChoice);
                     //System.out.println("Pixel column " + j + " on row " + i + " at index " + indx + ": " + Integer.toBinaryString(eachPixel[5-j]));
                     //System.out.println("Attempting to access fPixels[" + currentIndex + "] and eachPixel[" + j + "]");
                     //System.out.println((xor == FLAG_TILE_XOR ? "XORing " : "Using ") + Integer.toBinaryString(colorChoice) + " and " + Integer.toBinaryString(fColorValues[indx]) + "=" + Integer.toBinaryString(newValue));
 
                     setPixelWithColorIndex(indx, newValue); //KaraokePlayer.getTableColorInt((int)(newValue));
                     indx++;
                 }
                 indx += (CDG_FULL_WIDTH - TILE_WIDTH);
                 //System.out.println();
                 //Thread.sleep(1);
                 
                 
             }
             
             fSource.newPixels(column,row,TILE_WIDTH,TILE_HEIGHT);
             //System.out.println("Doing new pixel square at " + row + "," + column);
             //fSource.newPixels();
         } catch (Exception ex) { System.err.println("Possible file corruption!"); ex.printStackTrace(); }
     }
     
     public void setEntireTileColorValue(int tileRow, int tileColumn, int colorIndex)
     {
         try {
             int row = TILE_HEIGHT*tileRow;
             int column = TILE_WIDTH*tileColumn;
 
             //System.out.println("Painting entire tile at " + tileRow + "," + tileColumn + " which translates to " + row + "," + column + " = ");
             int currentIndex = (row*CDG_FULL_WIDTH) + column;
             for(int i = 0; i < 12; i++)
             {
                 for(int j = 0; j < 6; j++)
                 {
                     
                     //System.out.println("Pixel column " + j + " on pixel row " + i + ": real index is " + currentIndex + " out of " + fPixels.length);
                     setPixelWithColorIndex(currentIndex, colorIndex); //KaraokePlayer.getTableColorInt((int)(newValue));
                     currentIndex++;
                 }
                 currentIndex += (CDG_FULL_WIDTH - TILE_WIDTH);
             }
             
             fSource.newPixels(column,row,TILE_WIDTH,TILE_HEIGHT);
         } catch (Exception ex) { System.err.println(ex); ex.printStackTrace(); }
     }
     
     protected void setPixelWithColorIndex(int pixel, int indexColor)
     {
         fPixels[pixel] = getTableColorInt(indexColor);
         fColorValues[pixel] = indexColor;
     }
     
     public void refreshColorTable()
     {
         for(int i = 0; i < fPixels.length; i++)
         {
             setPixelWithColorIndex(i,fColorValues[i]);
         }
         
         fSource.newPixels(0,0,CDG_FULL_WIDTH,CDG_FULL_HEIGHT);
     }
     
     public void loadColorTable(int [] colors, int flag)
     {
         for (int i = 0; i < 8; i++)
         {
             int offset = ( flag == Packet.INSTR_LOAD_COLOR_TABLE_LOWER ? 0 : 8);
             colorTable[i+offset] = colors[i];
         }
         refreshColorTable();
     }
     
     public int getTableColorInt (int colorNumber)
     {
 
         //System.out.println("Requested color number " + colorNumber);
         //colorNumber = (colorNumber % 15);
 
         int colorBytes = 0x0;
         
         try {
 
             colorBytes = colorTable[colorNumber];
         } catch (Exception ex) { System.err.println(ex); ex.printStackTrace(); System.err.flush(); }
         
         // System.out.println("color: " +
         // Integer.toBinaryString(colorEntryByteArray));
 
         return colorBytes;
     }
    
	private void rootComponentResized(ComponentEvent evt) {
		// TODO: Optimize this -- it hurts performance!
		System.out.println("this.componentResized, event=" + evt);
		if(fSource != null)
	         fImage = (createImage (fSource)).getScaledInstance(CDG_FULL_WIDTH*2, CDG_FULL_HEIGHT*2, Image.SCALE_AREA_AVERAGING);
	}
 
 }
