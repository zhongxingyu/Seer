 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.nodomain.volkerk.PinkDotRemover;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import org.nodomain.volkerk.SimpleTIFFlib.ImageFileDirectory;
 import org.nodomain.volkerk.SimpleTIFFlib.TIFFhandler;
 
 /**
  * Removes the AF dots in Magic Lantern's raw video frames for the Canon 650D
  */
 public class PinkDotRemover {
     
     /**
      * the DNG file to modify
      */
     protected String srcFileName;
     
     /**
      * the image handler for the input file
      */
     protected TIFFhandler srcDng;
     
     /**
      * an image handler for the output file -- will be initialized from the input file
      */
     protected TIFFhandler dstDng;
     
     /**
      * Constructor. Checks for a valid file name and tries to open the file
      * 
      * @param fName the name / path of the DNG file
      */
     public PinkDotRemover(String fName)
     {
         File src = new File(fName);
         
         if (!(src.exists()))
         {
             throw new IllegalArgumentException("File " + fName + " does not exist!");
         }
         
         try
         {
             srcDng = new TIFFhandler(fName);
             dstDng = new TIFFhandler(fName);
         }
         catch (Exception e)
         {
             throw new IllegalArgumentException("Baaaaad file: " + e.getMessage());
         }
         
         srcFileName = fName;
     }
     
     /**
      * Removes the pink dots from the target file
      * 
      * @return true if the dots could be removed, false in case of errors
      */
     public boolean doRemovalInMemory()
     {
         // prepare access to the image data
         // we assume that the TIFF file contains exactly one RAW image...
         ImageFileDirectory ifdSrc = srcDng.getFirstIFDwithCFA();
         ImageFileDirectory ifdDst = dstDng.getFirstIFDwithCFA();
         int w = (int) ifdSrc.imgWidth();
         int h = (int) ifdSrc.imgLen();
         
         // prepare a list of x,y-values representing the distorted dots
         ArrayList<int[]> dotList;
         
         // step 1: get the empirically determined dot coordinates
         dotList = getEmpiricDotPattern(w, h);
         if (dotList == null)
         {
            System.err.println("No empiric dot pattern for images " + w + "x" + h + " available!");
             return false;
         }
         
         // step 2: get the "regular" or "grid" dot pattern
         ArrayList<int[]> gridDots = getGridDotPattern(w, h);
         if (gridDots == null)
         {
            System.err.println("No dot pattern for images " + w + "x" + h + " available!");
             return false;
         }
         
         // step 3: merge the two lists
         dotList.addAll(gridDots);
         
         // finally, loop over all coordinates and do the interpolation to fix the distortion
         for (int[] dot : dotList)
         {
             interpolPixel(ifdSrc, ifdDst, dot[0], dot[1], 1.0);
         }
         
         return true;
     }
     
     
     /**
      * Replaces a pixel intensity with an interpolation of the "X"-like neighboring pixels
      * Pixels closer than 2 pixel to the image border can't be interpolated and remain unmodified.
      * 
      * @param ifdSrc ImageFileHandler for the distorted source image data (read)
      * @param ifdDst ImageFileHandler for the improved image data (write)
      * @param x the 0-based x-coordinate of the pixel to fix
      * @param y the 0-based y-coordinate of the pixel to fix
      * @param weight a factor between 0...1 blending the current pixel value with the new, interpolated value; 1.0 replaces the pixel with the interpolated value
      */
     protected void interpolPixel(ImageFileDirectory ifdSrc, ImageFileDirectory ifdDst, int x, int y, double weight)
     {
         if ((x < 2) || (x > (ifdSrc.imgWidth() - 3)) || (y < 2) || (y > (ifdSrc.imgLen() - 3))) return;
         
         //double fac1 = 0.0;
         //double fac2 = 0.0;
         double fac3 = 0.25;
 
         // calc a new pixel value from the neighbors of the dot;
         // no range checking here; I assume there's always a neighbor...
         double newVal = 0;
         
         // direct neighbors: "+" direction
         //newVal += fac1 * ifdSrc.CFA_getPixel(x - 2, y);
         //newVal += fac1 * ifdSrc.CFA_getPixel(x + 2, y);
         //newVal += fac1 * ifdSrc.CFA_getPixel(x, y + 2);
         //newVal += fac1 * ifdSrc.CFA_getPixel(x, y - 2);
 
         // direct neighbors: "X" direction
         newVal += fac3 * ifdSrc.CFA_getPixel(x - 2, y - 2);
         newVal += fac3 * ifdSrc.CFA_getPixel(x + 2, y - 2);
         newVal += fac3 * ifdSrc.CFA_getPixel(x - 2, y + 2);
         newVal += fac3 * ifdSrc.CFA_getPixel(x + 2, y + 2);
 
         // indirect neighbors
         //newVal += fac2 * ifdSrc.CFA_getPixel(x - 4, y - 4);
         //newVal += fac2 * ifdSrc.CFA_getPixel(x + 4, y + 4);
         //newVal += fac2 * ifdSrc.CFA_getPixel(x - 4, y + 4);
         //newVal += fac2 * ifdSrc.CFA_getPixel(x + 4, y - 4);
         
         if (weight != 1.0) newVal = (1.0 - weight) * ((double) ifdSrc.CFA_getPixel(x, y)) + weight * newVal;
 
         ifdDst.CFA_setPixel(x, y, (int) newVal);
         
     }
     
     
     /**
      * Writes the contents of the destination image to a DNG file. The filename
      * is constructed from the original filename plus a leading underscore.
      * Existing files will be overwritten.
      * 
      * @return the name and (possibly) path of the destination file
      */
     public String writeResultToFile()
     {
         Path srcPath = Paths.get(srcFileName);
         
         String fName = srcPath.getFileName().toString();
         String pName = srcPath.getParent().normalize().toString();
         
         Path dstPath = Paths.get(pName, "_" + fName);
                 
         dstDng.saveAs(dstPath);
         
         return dstPath.toString();
     }
     
     /**
      * Retrieves the coordinates for the regular, grid-like dots for a specific image size
      * 
      * @param w image width
      * @param h image height
      * @return a list of (x, y)-coordinates or null if no data for the requested image size is available
      */
     protected ArrayList<int[]> getGridDotPattern(int w, int h)
     {
         int[][] gridData = null;
         
         if ((w == 1280) && (h == 720))
         {
             gridData = new int[][] {
                 {511, 213, 767, 263, 8, 10},
                 {507, 219, 763, 269, 8, 10},
                 {504, 234, 760, 304, 8, 10},
                 {504, 334, 760, 404, 8, 10},
                 {508, 338, 764, 408, 8, 10},
                 {511, 413, 767, 463, 8, 10},
                 {507, 419, 763, 469, 8, 10},
                 {504, 434, 760, 504, 8, 10},
                 {508, 238, 764, 308, 8, 10},
                 {312, 274, 496, 284, 8, 10},
                 {316, 278, 500, 288, 8, 10},
                 {768, 274, 952, 284, 8, 10},
                 {768, 284, 956, 288, 8, 10},
                 {319, 313, 359, 323, 8, 10},
                 {315, 319, 955, 329, 8, 10},
                 {511, 333, 767, 363, 8, 10},
                 {507, 339, 763, 369, 8, 10},
                 
                 {312, 354, 496, 364, 8, 10},
                 {316, 358, 500, 368, 8, 10},
                 {312, 394, 496, 404, 8, 10},
                 {316, 398, 500, 408, 8, 10},
                 {312, 434, 496, 444, 8, 10},
                 {316, 438, 500, 448, 8, 10},
                 {319, 433, 503, 443, 8, 10},
                 {315, 439, 499, 449, 8, 10},
                 
                 {312+456, 354, 496+456, 364, 8, 10},
                 {316+456, 358, 500+456, 368, 8, 10},
                 {312+456, 394, 496+456, 404, 8, 10},
                 {316+456, 398, 500+456, 408, 8, 10},
                 {312+456, 434, 496+456, 444, 8, 10},
                 {316+456, 438, 500+456, 448, 8, 10},
                 {319+456, 433, 503+456, 443, 8, 10},
                 {315+456, 439, 499+456, 449, 8, 10},
                 {316+456, 278, 500+456, 288, 8, 10},
                 
                 {508, 438, 764, 508, 8, 10},
                 {367, 313, 959, 323, 8, 10},
                 {775, 353, 959, 363, 8, 10},
                 {771, 359, 955, 369, 8, 10},
                 
                 {327, 273, 903, 283, 8, 10},
                 {315, 279, 939, 289, 8, 10},
                 
                 {511, 293, 767, 303, 8, 10},
                 {507, 299, 763, 309, 8, 10},
                 
                 {319, 393, 959, 403, 8, 10},
                 {315, 399, 955, 409, 8, 10},
                 
                 {511, 493, 767, 503, 8, 10},
                 {507, 499, 763, 509, 8, 10},
                 
                 {319, 353, 503, 363, 8, 10},
                 {315, 359, 499, 369, 8, 10},
                 
                 {512, 214, 760, 224, 8, 10},
                 {516, 218, 764, 228, 8, 10},
                 
                 {312, 314, 952, 324, 8, 10},
                 {308, 318, 948, 328, 8, 10},
                 
                 {496, 414, 744, 424, 8, 10},
                 {516, 418, 764, 428, 8, 10}
             };
         
         }
         
         // convert the grid data into single dot coordinates
         if (gridData == null) return null;
         
         ArrayList<int[]> result = new ArrayList<>();
         for (int[] gridBlock : gridData)
         {
             int minX = gridBlock[0];
             int minY = gridBlock[1];
             int maxX = gridBlock[2];
             int maxY = gridBlock[3];
             int stepX = gridBlock[4];
             int stepY = gridBlock[5];
 
             // "unwrap" the gridd
             for (int y = minY; y <= maxY; y += stepY)
             {
                 for (int x = minX; x <= maxX; x += stepX)
                 {
                     // add the regular coordinates to the empiric coordinates from before
                     result.add(new int[] {x, y});
                 }
             }
         }
             
         return result;
     }
     
     /**
      * Reads "empiric" dot coordinates stored in a text file in the CLASSPATH
      * 
      * @param w image width
      * @param h image height
      * @return a list of (x, y)-coordinates or null if no data for the requested image size is available
      */
     protected ArrayList<int[]> getEmpiricDotPattern(int w, int h)
     {
         ArrayList<int[]> result = null;
         
         if ((w == 1280) && (h == 720))
         {
             
             try
             {
                 // open the dot data file as a resource (e. g. in the JAR file)
                 InputStream in = this.getClass().getResourceAsStream("res/pixCoord_threshold2068.txt");
                 InputStreamReader ir = new InputStreamReader(in);
                 BufferedReader b  = new BufferedReader(ir);
                                 
                 result = new ArrayList<>();
                 
                 // read the file line-by-line and convert the ASCII-text into numbers
                 String line;
                 while ((line = b.readLine()) != null)
                 {
                     int x = Integer.parseInt(line.split(",")[0].trim());
                     int y = Integer.parseInt(line.split(",")[1].trim());
                     result.add(new int[] {x, y});
                 }
             }
             catch (Exception e)
             {
                 System.err.println("Something went terribly wrong while reading empiric dot pattern data: " + e.getMessage());
             }
         }
             
         return result;
     }
     
 }
