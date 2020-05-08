 package com.tzapps.tzpalette.data;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.graphics.Bitmap;
 
 public class PaletteData
 {
     private static final String TAG = "PaletteData";
     
     private static final int THUMB_MAX_WIDTH  = 500;
     private static final int THUMB_MAX_HEIGHT = 500;
     
     Bitmap mThumb;
     List<Integer> mColors;
     
     public PaletteData()
     {
         mThumb  = null;
         mColors = new ArrayList<Integer>();
     }
     
     public PaletteData(Bitmap thumb)
     {
         mThumb  = thumb;
         mColors = new ArrayList<Integer>();
     }
     
     public void setThumb(Bitmap thumb)
     {
         if (mThumb != null)
             mThumb.recycle();
         
        if (thumb == null)
        {
            mThumb = null;
            return;
        }
        
         int width = thumb.getWidth();
         int height = thumb.getHeight();
         
         int targetW = THUMB_MAX_WIDTH;
         int targetH = THUMB_MAX_HEIGHT;
         
         if (width <= targetW && height <= targetH)
         {
             mThumb = thumb;
         }
         else
         {   
             float scale = 0.0f;
             
             if (width > height)
             {
                 scale = ((float) targetW) / width;
                 
                 targetH = (int)Math.round(height * scale);
             }
             else
             {
                 scale = ((float) targetH) / height;
                 targetW = (int)Math.round(width * scale);
             }
             
             mThumb = Bitmap.createScaledBitmap(thumb, targetW, targetH, false);
         }
     }
     
     public Bitmap getThumb()
     {
         return mThumb;
     }
     
     public void addColor(int color)
     {
         mColors.add(color);
     }
     
     public void addColors(int[] colors, boolean reset)
     {
         if (colors == null)
             return;
         
         if (reset)
             mColors.clear();
         
         for (int color : colors)
             addColor(color);
     }
     
     public int[] getColors()
     {
         int numOfColors = mColors.size();
         int[] colors = new int[mColors.size()];
         
         for (int i = 0; i < numOfColors; i++)
         {
             colors[i] = mColors.get(i);
         }
         
         Arrays.sort(colors);
         
         return colors;
     }
     
     public void clearColors()
     {
         mColors.clear();
     }
 }
