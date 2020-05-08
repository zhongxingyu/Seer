 /*
  * Copyright © 2013 Volker Knollmann
  * 
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See the COPYING file or visit
  * http://www.wtfpl.net/ for more details.
  * 
  * This program comes without any warranty. Use it at your own risk or
  * don't use it at all.
  */
 
 package org.nodomain.volkerk.PinkDotRemover;
 
import java.io.BufferedReader;
 import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
 import java.nio.file.Path;
 import java.nio.file.Paths;
import java.util.ArrayList;
 import org.nodomain.volkerk.LoggingLib.LoggingClass;
 import org.nodomain.volkerk.SimpleTIFFlib.ImageFileDirectory;
 import org.nodomain.volkerk.SimpleTIFFlib.TIFFhandler;
 
 /**
  * Removes the AF dots in Magic Lantern's raw video frames for the Canon 650D
  */
 public class PinkDotRemover extends LoggingClass {
     
     /**
      * the DNG file to modify
      */
     protected String srcFileName;
     
     /**
      * the image handler for the input file
      */
     protected TIFFhandler srcDng;
     
     /**
      * A handle for the dot location database
      */
     DotLocationDB db;
     
     /**
      * The cam type to use
      */
     String camType;
     
     /**
      * an image handler for the output file -- will be initialized from the input file
      */
     protected TIFFhandler dstDng;
     protected static final String DEFAULT_CAM_TYPE = "650D";
     
     /**
      * Constructor. Checks for a valid file name and tries to open the file
      * 
      * @param fName the name / path of the DNG file
      * @param db is the database with dot locations for all cams and resolutions
      * @param camType is the name of the camera type
      */
     public PinkDotRemover(String fName, DotLocationDB _db, String _camType)
     {
         preLog(LVL_DEBUG, "Trying to instanciate File for ", fName);
         File src = new File(fName);
         
         if (!(src.exists()))
         {
             resultLog(LOG_FAIL + ": got null pointer");
             throw new IllegalArgumentException("File " + fName + " does not exist!");
         }
         resultLog(LOG_OK);
         
         logPush("Instanciating TIFF handlers for ", fName);
         try
         {
             
             logPush("Instanciating source TIFF handler with string arg");
             srcDng = new TIFFhandler(fName);
             logPop("Done");
             
             logPush("Instanciating destination TIFF handler with string arg");
             dstDng = new TIFFhandler(fName);
             logPop("Done");
         }
         catch (Exception e)
         {
             failed(e.getMessage());
             throw new IllegalArgumentException("Baaaaad file: " + e.getMessage());
         }
         logPop("Done");
         
         srcFileName = fName;
         
         db = _db;
         
         camType = DEFAULT_CAM_TYPE;
         if ((_camType != null) && (_camType.length() != 0)) camType = _camType;
     }
     
     /**
      * Removes the pink dots from the target file
      * 
      * @param doInterpolation if true, the interpolation algorithm is used; otherwise, the pixel is simply marked as "bad pixel"
      * @return true if the dots could be removed, false in case of errors
      */
     public boolean doRemovalInMemory(boolean doInterpolation)
     {
         // prepare access to the image data
         // we assume that the TIFF file contains exactly one RAW image...
         logPush("Retrieving CFA IFDs");
         
         ImageFileDirectory ifdSrc = srcDng.getFirstIFDwithCFA();
         if (ifdSrc == null) dbg("Got null for srcDng");
         
         ImageFileDirectory ifdDst = dstDng.getFirstIFDwithCFA();
         if (ifdDst == null) dbg("Got null for srcDng");
         
         logPop("Done");
         
         int w = (int) ifdSrc.imgWidth();
         int h = (int) ifdSrc.imgLen();
         
         // Let's see if we have the dot pattern for this type of image
         DotSet ds = db.getDotSetByModelAndResolution(camType, w, h);
         if (ds == null)
         {
             failed("No dot pattern for image size " + w + "x" + h + " and cam type ", camType, " available!");
             return false;
         }
         dbg("Found dot set for image: ", ds.getCombinedName());
         
         int[][] dotList = ds.getAllCoordinates();
         
         if (doInterpolation) interpolPixel(ifdSrc, ifdDst, dotList);
         else markBadPixels(ifdSrc, ifdDst, dotList);
         
         dbg("Conversion in memory completed!");
         
         return true;
     }
     
     
     /**
      * Replaces a pixel intensity with an interpolation of the "X"-like neighboring pixels
      * Pixels closer than 2 pixel to the image border can't be interpolated and remain unmodified.
      * 
      * @param ifdSrc ImageFileHandler for the distorted source image data (read)
      * @param ifdDst ImageFileHandler for the improved image data (write)
      * @param dotList a list of x,y-coordinates of the dots to fix
      */
     protected void interpolPixel(ImageFileDirectory ifdSrc, ImageFileDirectory ifdDst, int[][] dotList)
     {
         int w = (int) ifdSrc.imgWidth();
         int h = (int) ifdSrc.imgLen();
         
         for (int[] dot : dotList)
         {
             int x = dot[0];
             int y = dot[1];
             
             // don't fix pixel on image borders
             if ((x < 2) || (x > (w - 3)) || (y < 2) || (y > (h - 4))) continue;
             
             // determine intensity gradients in all four directions
             int g1 = ifdSrc.CFA_getPixel(x, y - 2) - ifdSrc.CFA_getPixel(x, y + 2); // top-down
             int g2 = ifdSrc.CFA_getPixel(x - 2, y) - ifdSrc.CFA_getPixel(x + 2, y); // left-right
             int g3 = ifdSrc.CFA_getPixel(x - 2, y - 2) - ifdSrc.CFA_getPixel(x + 2, y + 2); // top-left, down-right
             int g4 = ifdSrc.CFA_getPixel(x + 2, y - 2) - ifdSrc.CFA_getPixel(x - 2, y + 2); // top-right, down-left
             
             // find the minimum gradient
             g1 = Math.abs(g1);
             g2 = Math.abs(g2);
             g3 = Math.abs(g3);
             g4 = Math.abs(g4);
             
             int minG = Math.min(g1, g2);
             minG = Math.min(minG, g3);
             minG = Math.min(minG, g4);
             
             // use the minimum gradient for interpolation
             double newVal;
             if (minG == g1)
             {
                 newVal = (ifdSrc.CFA_getPixel(x, y - 2) + ifdSrc.CFA_getPixel(x, y + 2)) * 0.5;
             }
             else if (minG == g2)
             {
                 newVal = (ifdSrc.CFA_getPixel(x-2, y) + ifdSrc.CFA_getPixel(x+2, y)) * 0.5;
             }
             else if (minG == g3)
             {
                 newVal = (ifdSrc.CFA_getPixel(x-2, y-2) + ifdSrc.CFA_getPixel(x+2, y+2)) * 0.5;
             }
             else
             {
                 newVal = (ifdSrc.CFA_getPixel(x+2, y-2) + ifdSrc.CFA_getPixel(x-2, y+2)) * 0.5;
             }
 
 
             ifdDst.CFA_setPixel(x, y, (int) newVal);
         }
         
     }
     
     /**
      * Replaces a pixel intensity with a 0 to indicate a bad pixel
      * Leaves the actual interpolation to the RAW processor later on
      * 
      * @param ifdSrc ImageFileHandler for the distorted source image data (read)
      * @param ifdDst ImageFileHandler for the improved image data (write)
      * @param dotList a list of x,y-coordinates of the dots to fix
      */
     protected void markBadPixels(ImageFileDirectory ifdSrc, ImageFileDirectory ifdDst, int[][] dotList)
     {
         for (int[] dot : dotList)
         {
             ifdDst.CFA_setPixel(dot[0], dot[1], 0);
         }
     
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
         preLog(LVL_DEBUG, "Trying to instanciate Path for ", srcFileName);
         Path srcPath = Paths.get(srcFileName);
         if (srcPath == null) resultLog((LOG_FAIL));
         else resultLog(LOG_OK);
         
         String fName = srcPath.getFileName().toString();
         dbg("fName = ", fName);
         String pName = "";
         if (srcPath.getParent() != null) pName = srcPath.getParent().normalize().toString();
         dbg("pName = ", pName);
         
         preLog(LVL_DEBUG, "Trying to instanciate Path for destination file");
         Path dstPath = Paths.get(pName, "_" + fName);
         if (dstPath == null) resultLog((LOG_FAIL));
         else resultLog(LOG_OK);
                 
         logPush("Calling dstDng.saveAs() with Path parameter ", dstPath);
         dstDng.saveAs(dstPath);
         logPop("Done");
         
         dbg("File saved successfully");
         
         return dstPath.toString();
     }
     
 }
