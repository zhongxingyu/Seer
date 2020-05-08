 /*
 	ChibiPaintMod
    Copyright (c) 2012-2013 Sergey Semushin
    Copyright (c) 2006-2008 Marc Schefer
 
     This file is part of ChibiPaintMod (previously ChibiPaint).
 
     ChibiPaintMod is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     ChibiPaintMod is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with ChibiPaintMod. If not, see <http://www.gnu.org/licenses/>.
 
  */
 
 package chibipaint.engine;
 
 import chibipaint.CPController;
 import chibipaint.engine.CPBrushManager.CPBrushDab;
 import chibipaint.util.CPColorFloat;
 import chibipaint.util.CPRect;
 
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Vector;
 
 //FIXME: BROKEN: use setForegroundColor and setBrush, controller's layerChanged replaced by the ICPArtworkListener mechanism
 
 public class CPArtwork
 {
 
 private final int width;
 private final int height;
 
 private Vector<CPLayer> layers;
 private CPLayer activeLayer;
 private int activeLayerNumber;
 
 private final CPLayer fusion; // fusion is a final view of the image, like which should be saved to png (no overlays like selection or grid here)
 private final CPLayer opacityBuffer;
 private final CPLayer transformBuffer; // temporary place to fusion active layer with preview in case if transformation is taking place.
 private final CPRect fusionArea;
 private final CPRect opacityArea;
 private final CPTransformHandler transformHandler;
 
 CPSelection curSelection;
 
 private final Random rnd = new Random ();
 
 public CPUndoManager getUndoManager ()
 {
   return undoManager;
 }
 
 public final CPUndoManager undoManager = new CPUndoManager (this);
 
 public CPClip getClipboard ()
 {
   return clipboard;
 }
 
 public void initializeTransform ()
 {
   undoManager.preserveActiveLayerData ();
   undoManager.preserveCurrentSelection ();
   transformHandler.initialize (curSelection, getActiveLayer ());
 }
 
 public CPTransformHandler getTransformHandler ()
 {
   return transformHandler;
 }
 
 public void FinishTransformUndo ()
 {
  getUndoManager ().currentSelectionChanged ();
   getUndoManager ().activeLayerDataChange (getSize ());
   getUndoManager ().finalizeUndo ();
   invalidateFusion ();
 }
 
 public void RestoreActiveLayerAndSelection ()
 {
   undoManager.restoreActiveLayerData ();
   undoManager.restoreSelection ();
 }
 
 public void copySelected (boolean limited)
 {
   CPColorBmp copy = new CPColorBmp (width, height);
   copy.copyDataFrom (activeLayer);
   copy.cutBySelection (curSelection);
   CPRect rect = curSelection.getBoundingRect ();
   CPCopyPasteImage img = new CPCopyPasteImage (rect.getWidth (), rect.getHeight (), rect.getLeft (), rect.getTop ());
   img.setData (copy.copyRectToIntArray (rect));
   CPClipboardHelper.SetClipboardImage (img, limited);
   return;
 }
 
 
 public void cutSelected (boolean limited)
 {
   undoManager.preserveActiveLayerData ();
   undoManager.preserveCurrentSelection ();
   copySelected (limited);
   activeLayer.removePartsCutBySelection (curSelection);
   CPRect rect = curSelection.getBoundingRect ();
   invalidateFusion (rect);
   curSelection.makeEmpty ();
  undoManager.currentSelectionChanged ();
   undoManager.activeLayerDataChange (rect);
   undoManager.finalizeUndo ();
   return;
 }
 
 public void pasteFromClipboard (boolean limited)
 {
   CPCopyPasteImage imageInClipboard = CPClipboardHelper.GetClipboardImage (limited);
   if (imageInClipboard == null)
     return;
   addLayer ();
   imageInClipboard.paste (getActiveLayer ());
   CPSelection selection = new CPSelection (getWidth (), getHeight ());
   selection.makeSelectionFromAlpha (getActiveLayer ().getData (), new CPRect (imageInClipboard.getPosX (), imageInClipboard.getPosY (),
                                                                               imageInClipboard.getPosX () + imageInClipboard.getWidth (),
                                                                               imageInClipboard.getPosY () + imageInClipboard.getHeight ()));
   getUndoManager ().activeLayerDataChange (selection.getBoundingRect ());
   DoSelection (SelectionTypeOfAppliance.CREATE, selection);
   finalizeUndo ();
 
   invalidateFusion ();
 }
 
 public void deselectAll ()
 {
   undoManager.preserveCurrentSelection ();
   curSelection.makeEmpty ();
  getUndoManager ().currentSelectionChanged ();
  undoManager.finalizeUndo ();
}

public void selectAll ()
{
  undoManager.preserveCurrentSelection ();
  curSelection.selectAll ();
  undoManager.currentSelectionChanged ();
   undoManager.finalizeUndo ();
 }
 
 public enum SelectionTypeOfAppliance
 {
   CREATE,
   SUBTRACT,
   ADD,
   INTERSECT
 }
 
 ;
 
 public void DoSelection (SelectionTypeOfAppliance type, CPSelection selection)
 {
   undoManager.preserveCurrentSelection ();
   switch (type)
     {
     case CREATE:
       curSelection = selection;
       break;
     case SUBTRACT:
       curSelection.SubtractFromSelection (selection);
       break;
     case ADD:
       curSelection.AddToSelection (selection);
       break;
     case INTERSECT:
       curSelection.IntersectWithSelection (selection);
       break;
     }
   CPRect rect = undoManager.getPreservedSelection ().getBoundingRect ();
   rect.union (curSelection.getBoundingRect ());
  undoManager.currentSelectionChanged ();
 }
 
 public CPSelection getCurSelection ()
 {
   return curSelection;
 }
 
 public void setCurSelection (CPSelection cpSelection)
 {
   curSelection = cpSelection;
 }
 
 public int getWidth ()
 {
   return width;
 }
 
 public int getHeight ()
 {
   return height;
 }
 
 public void finalizeUndo ()
 {
   undoManager.finalizeUndo ();
 }
 
 public interface ICPArtworkListener
 {
 
   void updateRegion (CPArtwork artwork, CPRect region);
 
   void layerChange (CPArtwork artwork);
 }
 
 private final LinkedList<ICPArtworkListener> artworkListeners = new LinkedList<ICPArtworkListener> ();
 
 // Clipboard
 
 public static class CPClip
 {
 
   final CPColorBmp bmp;
   final int x;
   final int y;
 
   CPClip (CPColorBmp bmp, int x, int y)
   {
     this.bmp = bmp;
     this.x = x;
     this.y = y;
   }
 }
 
 private final CPClip clipboard = null;
 
 private CPBrushInfo curBrush;
 
 // FIXME: shouldn't be public
 public final CPBrushManager brushManager = new CPBrushManager ();
 
 private float lastX;
 private float lastY;
 private float lastPressure;
 private int[] brushBuffer = null;
 
 //
 // Current Engine Parameters
 //
 
 private boolean sampleAllLayers = false;
 private boolean lockAlpha = false;
 
 private int curColor;
 
 private final CPBrushTool[] paintingModes = {new CPBrushToolSimpleBrush (), new CPBrushToolEraser (), new CPBrushToolDodge (),
         new CPBrushToolBurn (), new CPBrushToolWatercolor (), new CPBrushToolBlur (), new CPBrushToolSmudge (),
         new CPBrushToolOil (),};
 
 private static final int BURN_CONSTANT = 260;
 private static final int BLUR_MIN = 64;
 private static final int BLUR_MAX = 1;
 
 public CPArtwork (int width, int height)
 {
   this.width = width;
   this.height = height;
 
   transformHandler = new CPTransformHandler (width, height);
   setLayers (new Vector<CPLayer> ());
 
   CPLayer defaultLayer = new CPLayer (width, height);
   defaultLayer.setName (getDefaultLayerName ());
   defaultLayer.clear (0xffffffff);
   getLayersVector ().add (defaultLayer);
 
   activeLayer = getLayersVector ().get (0);
   fusionArea = new CPRect (0, 0, width, height);
   opacityArea = new CPRect ();
   setActiveLayerNum (0);
 
   // we reserve a double sized buffer to be used as a 16bits per channel buffer
   opacityBuffer = new CPLayer (width, height);
   transformBuffer = new CPLayer (width, height);
 
   fusion = new CPLayer (width, height);
 }
 
 public long getDocMemoryUsed ()
 {
   return (long) getWidth () * getHeight () * 4 * (3 + getLayersVector ().size ())
           + (clipboard != null ? clipboard.bmp.getWidth () * clipboard.bmp.getHeight () * 4 : 0);
 }
 
 public CPLayer getDisplayBM ()
 {
   fusionLayers ();
   return fusion;
 
   // for(int i=0; i<opacityBuffer.data.length; i++)
   // opacityBuffer.data[i] |= 0xff000000;
   // return opacityBuffer;
 }
 
 public void fusionLayers ()
 {
   if (fusionArea.isEmpty ())
     {
       return;
     }
 
   mergeOpacityBuffer (curColor, false);
 
   fusion.clear (fusionArea, 0x00ffffff);
   boolean fullAlpha = true, first = true;
   int i = 0;
   for (CPLayer l : getLayersVector ())
     {
       if (!first)
         {
           fullAlpha = fullAlpha && fusion.hasAlpha (fusionArea);
         }
 
       if (getActiveLayer () == l && transformHandler.isTransformActive ())
         {
           transformBuffer.clear ();
           // transformBuffer.copyDataFrom (l);
           transformBuffer.copyRectFrom (l, fusionArea);
           transformBuffer.setAlpha (l.getAlpha ());
           transformBuffer.setBlendMode (l.getBlendMode ());
           transformHandler.drawPreviewOn (transformBuffer);
           doFusionWith (transformBuffer, fullAlpha);
         }
       else
         doFusionWith (l, fullAlpha);
     }
 
   fusionArea.makeEmpty ();
 }
 
 private void doFusionWith (CPLayer layer, boolean fullAlpha)
 {
   if (!layer.isVisible ())
     return;
 
   if (fullAlpha)
     {
       layer.fusionWithFullAlpha (fusion, fusionArea);
     }
   else
     {
       layer.fusionWith (fusion, fusionArea);
     }
 }
 
 // ///////////////////////////////////////////////////////////////////////////////////
 // Listeners
 // ///////////////////////////////////////////////////////////////////////////////////
 
 public void addListener (ICPArtworkListener listener)
 {
   artworkListeners.addLast (listener);
 }
 
 public void removeListener (ICPArtworkListener listener)
 {
   artworkListeners.remove (listener);
 }
 
 void callListenersUpdateRegion (CPRect region)
 {
   for (ICPArtworkListener l : artworkListeners)
     {
       l.updateRegion (this, region);
     }
 }
 
 public void callListenersLayerChange ()
 {
   for (ICPArtworkListener l : artworkListeners)
     {
       l.layerChange (this);
     }
 }
 
 // ///////////////////////////////////////////////////////////////////////////////////
 // Global Parameters
 // ///////////////////////////////////////////////////////////////////////////////////
 
 public void setSampleAllLayers (boolean b)
 {
   sampleAllLayers = b;
 }
 
 public void setLockAlpha (boolean b)
 {
   lockAlpha = b;
 }
 
 public void setForegroundColor (int color)
 {
   curColor = color;
 }
 
 public void setBrush (CPBrushInfo brush)
 {
   curBrush = brush;
 }
 
 // ///////////////////////////////////////////////////////////////////////////////////
 // Paint engine
 // ///////////////////////////////////////////////////////////////////////////////////
 
 public void beginStroke (float x, float y, float pressure)
 {
   if (curBrush == null)
     {
       return;
     }
 
   paintingModes[curBrush.paintMode].beginStroke (x, y, pressure);
 }
 
 public void continueStroke (float x, float y, float pressure)
 {
   if (curBrush == null)
     {
       return;
     }
 
   paintingModes[curBrush.paintMode].continueStroke (x, y, pressure);
 }
 
 public void endStroke ()
 {
   if (curBrush == null)
     {
       return;
     }
 
   paintingModes[curBrush.paintMode].endStroke ();
   undoManager.finalizeUndo ();
 }
 
 void mergeOpacityBuffer (int color, boolean clear)
 {
   if (!opacityArea.isEmpty ())
     {
 
       for (int j = opacityArea.top; j < opacityArea.bottom; j++)
         {
           int dstOffset = opacityArea.left + j * getWidth ();
           for (int i = opacityArea.left; i < opacityArea.right; i++, dstOffset++)
             {
               opacityBuffer.getData ()[dstOffset] = curSelection.cutOpacity (opacityBuffer.getData ()[dstOffset], i, j);
             }
         }
       paintingModes[curBrush.paintMode].mergeOpacityBuf (opacityArea, color);
 
       // Allow to eraser lower alpha with 'lock alpha' because it's all more logical and comfortable (look at gimp and other stuff)
       if (isLockAlpha () && curBrush.paintMode != CPBrushInfo.M_ERASE)
         {
           undoManager.restoreActiveLayerAlpha (opacityArea);
         }
 
       if (clear)
         {
           opacityBuffer.clear (opacityArea, 0);
         }
 
       opacityArea.makeEmpty ();
     }
 }
 
 // Extend this class to create new tools and brush types
 abstract class CPBrushTool
 {
 
   abstract public void beginStroke (float x, float y, float pressure);
 
   abstract public void continueStroke (float x, float y, float pressure);
 
   abstract public void endStroke ();
 
   abstract public void mergeOpacityBuf (CPRect dstRect, int color);
 }
 
 abstract class CPBrushToolBase extends CPBrushTool
 {
 
   private final CPRect undoArea = new CPRect ();
 
   @Override
   public void beginStroke (float x, float y, float pressure)
   {
     undoManager.preserveActiveLayerData ();
 
     opacityBuffer.clear ();
     opacityArea.makeEmpty ();
 
     lastX = x;
     lastY = y;
     lastPressure = pressure;
     paintDab (x, y, pressure);
   }
 
   @Override
   public void continueStroke (float x, float y, float pressure)
   {
     float dist = (float) Math.sqrt (((lastX - x) * (lastX - x) + (lastY - y) * (lastY - y)));
     float spacing = Math.max (curBrush.minSpacing, curBrush.curSize * curBrush.spacing);
 
     if (dist > spacing)
       {
         float nx = lastX, ny = lastY, np = lastPressure;
 
         float df = (spacing - 0.001f) / dist;
         for (float f = df; f <= 1.f; f += df)
           {
             nx = f * x + (1.f - f) * lastX;
             ny = f * y + (1.f - f) * lastY;
             np = f * pressure + (1.f - f) * lastPressure;
             paintDab (nx, ny, np);
           }
         lastX = nx;
         lastY = ny;
         lastPressure = np;
       }
   }
 
   @Override
   public void endStroke ()
   {
     undoArea.clip (getSize ());
     if (!undoArea.isEmpty ())
       {
         mergeOpacityBuffer (curColor, false);
         undoManager.activeLayerDataChange (undoArea);
         undoArea.makeEmpty ();
       }
     brushBuffer = null;
   }
 
   void paintDab (float xArg, float yArg, float pressure)
   {
     float x = xArg, y = yArg;
     curBrush.applyPressure (pressure);
     if (curBrush.scattering > 0f)
       {
         x += rnd.nextGaussian () * curBrush.curScattering / 4f;
         y += rnd.nextGaussian () * curBrush.curScattering / 4f;
         // x += (rnd.nextFloat() - .5f) * tool.scattering;
         // y += (rnd.nextFloat() - .5f) * tool.scattering;
       }
     CPBrushDab dab = brushManager.getDab (x, y, curBrush);
     paintDab (dab);
   }
 
   void paintDab (CPBrushDab dab)
   {
     CPRect srcRect = new CPRect (dab.width, dab.height);
     CPRect dstRect = new CPRect (dab.width, dab.height);
     dstRect.translate (dab.x, dab.y);
 
     clipSourceDest (srcRect, dstRect);
 
     // drawing entirely outside the canvas
     if (dstRect.isEmpty ())
       {
         return;
       }
 
     undoArea.union (dstRect);
     opacityArea.union (dstRect);
     invalidateFusion (dstRect);
 
     paintDabImplementation (srcRect, dstRect, dab);
   }
 
   abstract void paintDabImplementation (CPRect srcRect, CPRect dstRect, CPBrushDab dab);
 }
 
 class CPBrushToolSimpleBrush extends CPBrushToolBase
 {
 
   @Override
   void paintDabImplementation (CPRect srcRect, CPRect dstRect, CPBrushDab dab)
   {
     // FIXME: there should be no reference to a specific tool here
     // create a new brush parameter instead
     if (curBrush.isAirbrush)
       {
         paintFlow (srcRect, dstRect, dab.brush, dab.width, Math.max (1, dab.alpha / 8));
       }
     else if (curBrush.toolNb == CPController.T_PEN)
       {
         paintFlow (srcRect, dstRect, dab.brush, dab.width, Math.max (1, dab.alpha / 2));
       }
     else
       {
         // paintOpacityFlow(srcRect, dstRect, brush, dab.stride, alpha, 255);
         // paintOpacityFlow(srcRect, dstRect, brush, dab.stride, 128, alpha);
         paintOpacity (srcRect, dstRect, dab.brush, dab.width, dab.alpha);
       }
   }
 
   @Override
   public void mergeOpacityBuf (CPRect dstRect, int color)
   {
     int[] opacityData = opacityBuffer.getData ();
     int[] undoData = undoManager.getActiveLayerPreservedData ();
 
     for (int j = dstRect.top; j < dstRect.bottom; j++)
       {
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, dstOffset++)
           {
             int opacityAlpha = opacityData[dstOffset] / 255;
             if (opacityAlpha > 0)
               {
                 int destColor = undoData[dstOffset];
 
                 int destAlpha = destColor >>> 24;
                 int newLayerAlpha = opacityAlpha + destAlpha * (255 - opacityAlpha) / 255;
                 int realAlpha = 255 * opacityAlpha / newLayerAlpha;
                 int invAlpha = 255 - realAlpha;
 
                 int newColor = (((color >>> 16 & 0xff) * realAlpha + (destColor >>> 16 & 0xff) * invAlpha) / 255) << 16
                         & 0xff0000
                         | (((color >>> 8 & 0xff) * realAlpha + (destColor >>> 8 & 0xff) * invAlpha) / 255) << 8
                         & 0xff00 | (((color & 0xff) * realAlpha + (destColor & 0xff) * invAlpha) / 255) & 0xff;
 
                 newColor |= newLayerAlpha << 24 & 0xff000000;
                 getActiveLayer ().getData ()[dstOffset] = newColor;
               }
           }
       }
   }
 
   void paintOpacity (CPRect srcRect, CPRect dstRect, byte[] brush, int w, int alpha)
   {
     int[] opacityData = opacityBuffer.getData ();
 
     int by = srcRect.top;
     for (int j = dstRect.top; j < dstRect.bottom; j++, by++)
       {
         int srcOffset = srcRect.left + by * w;
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, srcOffset++, dstOffset++)
           {
             int brushAlpha = (brush[srcOffset] & 0xff) * alpha;
             if (brushAlpha != 0)
               {
                 int opacityAlpha = opacityData[dstOffset];
                 if (brushAlpha > opacityAlpha)
                   {
                     opacityData[dstOffset] = brushAlpha;
                   }
               }
 
           }
       }
   }
 
   void paintFlow (CPRect srcRect, CPRect dstRect, byte[] brush, int w, int alpha)
   {
     int[] opacityData = opacityBuffer.getData ();
 
     int by = srcRect.top;
     for (int j = dstRect.top; j < dstRect.bottom; j++, by++)
       {
         int srcOffset = srcRect.left + by * w;
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, srcOffset++, dstOffset++)
           {
             int brushAlpha = (brush[srcOffset] & 0xff) * alpha;
             if (brushAlpha != 0)
               {
                 int opacityAlpha = Math.min (255 * 255, opacityData[dstOffset]
                         + (255 - opacityData[dstOffset] / 255) * brushAlpha / 255);
                 opacityData[dstOffset] = opacityAlpha;
               }
 
           }
       }
   }
 
 }
 
 class CPBrushToolEraser extends CPBrushToolSimpleBrush
 {
 
   @Override
   public void mergeOpacityBuf (CPRect dstRect, int color)
   {
     int[] opacityData = opacityBuffer.getData ();
     int[] undoData = undoManager.getActiveLayerPreservedData ();
 
     for (int j = dstRect.top; j < dstRect.bottom; j++)
       {
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, dstOffset++)
           {
             int opacityAlpha = opacityData[dstOffset] / 255;
             if (opacityAlpha > 0)
               {
                 int destColor = undoData[dstOffset];
                 int destAlpha = destColor >>> 24;
 
                 int realAlpha = destAlpha * (255 - opacityAlpha) / 255;
                 getActiveLayer ().getData ()[dstOffset] = destColor & 0xffffff | realAlpha << 24;
               }
           }
       }
   }
 }
 
 class CPBrushToolDodge extends CPBrushToolSimpleBrush
 {
 
   @Override
   public void mergeOpacityBuf (CPRect dstRect, int color)
   {
     int[] opacityData = opacityBuffer.getData ();
     int[] undoData = undoManager.getActiveLayerPreservedData ();
 
     for (int j = dstRect.top; j < dstRect.bottom; j++)
       {
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, dstOffset++)
           {
             int opacityAlpha = opacityData[dstOffset] / 255;
             if (opacityAlpha > 0)
               {
                 int destColor = undoData[dstOffset];
                 if ((destColor & 0xff000000) != 0)
                   {
                     opacityAlpha += 255;
                     int r = (destColor >>> 16 & 0xff) * opacityAlpha / 255;
                     int g = (destColor >>> 8 & 0xff) * opacityAlpha / 255;
                     int b = (destColor & 0xff) * opacityAlpha / 255;
 
                     if (r > 255)
                       {
                         r = 255;
                       }
                     if (g > 255)
                       {
                         g = 255;
                       }
                     if (b > 255)
                       {
                         b = 255;
                       }
 
                     int newColor = destColor & 0xff000000 | r << 16 | g << 8 | b;
                     getActiveLayer ().getData ()[dstOffset] = newColor;
                   }
               }
           }
       }
   }
 }
 
 class CPBrushToolBurn extends CPBrushToolSimpleBrush
 {
 
   @Override
   public void mergeOpacityBuf (CPRect dstRect, int color)
   {
     int[] opacityData = opacityBuffer.getData ();
     int[] undoData = undoManager.getActiveLayerPreservedData ();
 
     for (int j = dstRect.top; j < dstRect.bottom; j++)
       {
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, dstOffset++)
           {
             int opacityAlpha = opacityData[dstOffset] / 255;
             if (opacityAlpha > 0)
               {
                 int destColor = undoData[dstOffset];
                 if ((destColor & 0xff000000) != 0)
                   {
                     // opacityAlpha = 255 - opacityAlpha;
 
                     int r = destColor >>> 16 & 0xff;
                     int g = destColor >>> 8 & 0xff;
                     int b = destColor & 0xff;
 
                     r = r - (BURN_CONSTANT - r) * opacityAlpha / 255;
                     g = g - (BURN_CONSTANT - g) * opacityAlpha / 255;
                     b = b - (BURN_CONSTANT - b) * opacityAlpha / 255;
 
                     if (r < 0)
                       {
                         r = 0;
                       }
                     if (g < 0)
                       {
                         g = 0;
                       }
                     if (b < 0)
                       {
                         b = 0;
                       }
 
                     int newColor = destColor & 0xff000000 | r << 16 | g << 8 | b;
                     getActiveLayer ().getData ()[dstOffset] = newColor;
                   }
               }
           }
       }
   }
 }
 
 class CPBrushToolBlur extends CPBrushToolSimpleBrush
 {
 
   @Override
   public void mergeOpacityBuf (CPRect dstRect, int color)
   {
     int[] opacityData = opacityBuffer.getData ();
     int[] undoData = undoManager.getActiveLayerPreservedData ();
 
     for (int j = dstRect.top; j < dstRect.bottom; j++)
       {
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, dstOffset++)
           {
             int opacityAlpha = opacityData[dstOffset] / 255;
             if (opacityAlpha > 0)
               {
                 int blur = BLUR_MIN + (BLUR_MAX - BLUR_MIN) * opacityAlpha / 255;
 
                 int destColor = undoData[dstOffset];
                 int a = blur * (destColor >>> 24 & 0xff);
                 int r = blur * (destColor >>> 16 & 0xff);
                 int g = blur * (destColor >>> 8 & 0xff);
                 int b = blur * (destColor & 0xff);
                 int sum = blur + 4;
 
                 destColor = undoData[j > 0 ? dstOffset - getWidth () : dstOffset];
                 a += destColor >>> 24 & 0xff;
                 r += destColor >>> 16 & 0xff;
                 g += destColor >>> 8 & 0xff;
                 b += destColor & 0xff;
 
                 destColor = undoData[j < getHeight () - 1 ? dstOffset + getWidth () : dstOffset];
                 a += destColor >>> 24 & 0xff;
                 r += destColor >>> 16 & 0xff;
                 g += destColor >>> 8 & 0xff;
                 b += destColor & 0xff;
 
                 destColor = undoData[i > 0 ? dstOffset - 1 : dstOffset];
                 a += destColor >>> 24 & 0xff;
                 r += destColor >>> 16 & 0xff;
                 g += destColor >>> 8 & 0xff;
                 b += destColor & 0xff;
 
                 destColor = undoData[i < getWidth () - 1 ? dstOffset + 1 : dstOffset];
                 a += destColor >>> 24 & 0xff;
                 r += destColor >>> 16 & 0xff;
                 g += destColor >>> 8 & 0xff;
                 b += destColor & 0xff;
 
                 a /= sum;
                 r /= sum;
                 g /= sum;
                 b /= sum;
                 getActiveLayer ().getData ()[dstOffset] = a << 24 | r << 16 | g << 8 | b;
               }
           }
       }
   }
 }
 
 // Brushes derived from this class use the opacity buffer
 // as a simple alpha layer
 class CPBrushToolDirectBrush extends CPBrushToolSimpleBrush
 {
 
   @Override
   public void mergeOpacityBuf (CPRect dstRect, int color)
   {
     int[] opacityData = opacityBuffer.getData ();
     int[] undoData = undoManager.getActiveLayerPreservedData ();
 
     for (int j = dstRect.top; j < dstRect.bottom; j++)
       {
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, dstOffset++)
           {
             int color1 = opacityData[dstOffset];
             int alpha1 = (color1 >>> 24);
             if (alpha1 <= 0)
               {
                 continue;
               }
             int color2 = undoData[dstOffset];
             int alpha2 = (color2 >>> 24) * fusion.alpha / 100;
 
             int newAlpha = alpha1 + alpha2 - alpha1 * alpha2 / 255;
             if (newAlpha > 0)
               {
                 int realAlpha = alpha1 * 255 / newAlpha;
                 int invAlpha = 255 - realAlpha;
 
                 getActiveLayer ().getData ()[dstOffset] = newAlpha << 24
                         | (((color1 >>> 16 & 0xff) * realAlpha + (color2 >>> 16 & 0xff) * invAlpha) / 255) << 16
                         | (((color1 >>> 8 & 0xff) * realAlpha + (color2 >>> 8 & 0xff) * invAlpha) / 255) << 8
                         | (((color1 & 0xff) * realAlpha + (color2 & 0xff) * invAlpha) / 255);
               }
           }
       }
   }
 }
 
 class CPBrushToolWatercolor extends CPBrushToolDirectBrush
 {
 
   static final int wcMemory = 50;
   static final int wxMaxSampleRadius = 64;
 
   LinkedList<CPColorFloat> previousSamples;
 
   @Override
   public void beginStroke (float x, float y, float pressure)
   {
     previousSamples = null;
 
     super.beginStroke (x, y, pressure);
   }
 
   @Override
   void paintDabImplementation (CPRect srcRect, CPRect dstRect, CPBrushDab dab)
   {
     if (previousSamples == null)
       {
         CPColorFloat startColor = sampleColor ((dstRect.left + dstRect.right) / 2,
                                                (dstRect.top + dstRect.bottom) / 2, Math.max (1, Math.min (wxMaxSampleRadius,
                                                                                                           dstRect.getWidth () * 2 / 6)), Math.max (1, Math.min (wxMaxSampleRadius, dstRect
                 .getHeight () * 2 / 6)));
 
         previousSamples = new LinkedList<CPColorFloat> ();
         for (int i = 0; i < wcMemory; i++)
           {
             previousSamples.addLast (startColor);
           }
       }
     CPColorFloat wcColor = new CPColorFloat (0, 0, 0);
     for (CPColorFloat sample : previousSamples)
       {
         wcColor.r += sample.r;
         wcColor.g += sample.g;
         wcColor.b += sample.b;
       }
     wcColor.r /= previousSamples.size ();
     wcColor.g /= previousSamples.size ();
     wcColor.b /= previousSamples.size ();
 
     // resaturation
     int color = curColor & 0xffffff;
     wcColor.mixWith (new CPColorFloat (color), curBrush.resat * curBrush.resat);
 
     int newColor = wcColor.toInt ();
 
     // bleed
     wcColor.mixWith (sampleColor ((dstRect.left + dstRect.right) / 2, (dstRect.top + dstRect.bottom) / 2, Math
             .max (1, Math.min (wxMaxSampleRadius, dstRect.getWidth () * 2 / 6)), Math.max (1, Math.min (
             wxMaxSampleRadius, dstRect.getHeight () * 2 / 6))), curBrush.bleed);
 
     previousSamples.addLast (wcColor);
     previousSamples.removeFirst ();
 
     paintDirect (srcRect, dstRect, dab.brush, dab.width, Math.max (1, dab.alpha / 4), newColor);
     mergeOpacityBuffer (0, false);
     if (isSampleAllLayers ())
       {
         fusionLayers ();
       }
   }
 
   void paintDirect (CPRect srcRect, CPRect dstRect, byte[] brush, int w, int alpha, int color1)
   {
     int[] opacityData = opacityBuffer.getData ();
 
     int by = srcRect.top;
     for (int j = dstRect.top; j < dstRect.bottom; j++, by++)
       {
         int srcOffset = srcRect.left + by * w;
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, srcOffset++, dstOffset++)
           {
             int alpha1 = (brush[srcOffset] & 0xff) * alpha / 255;
             if (alpha1 <= 0)
               {
                 continue;
               }
 
             int color2 = opacityData[dstOffset];
             int alpha2 = (color2 >>> 24);
 
             int newAlpha = alpha1 + alpha2 - alpha1 * alpha2 / 255;
             if (newAlpha > 0)
               {
                 int realAlpha = alpha1 * 255 / newAlpha;
                 int invAlpha = 255 - realAlpha;
 
                 // The usual alpha blending formula C = A * alpha + B * (1 - alpha)
                 // has to rewritten in the form C = A + (1 - alpha) * B - (1 - alpha) *A
                 // that way the rounding up errors won't cause problems
 
                 int newColor = newAlpha << 24
                         | ((color1 >>> 16 & 0xff) + (((color2 >>> 16 & 0xff) * invAlpha - (color1 >>> 16 & 0xff)
                         * invAlpha) / 255)) << 16
                         | ((color1 >>> 8 & 0xff) + (((color2 >>> 8 & 0xff) * invAlpha - (color1 >>> 8 & 0xff)
                         * invAlpha) / 255)) << 8
                         | ((color1 & 0xff) + (((color2 & 0xff) * invAlpha - (color1 & 0xff) * invAlpha) / 255));
 
                 opacityData[dstOffset] = newColor;
               }
           }
       }
   }
 
   CPColorFloat sampleColor (int x, int y, int dx, int dy)
   {
     LinkedList<CPColorFloat> samples = new LinkedList<CPColorFloat> ();
 
     CPLayer layerToSample = isSampleAllLayers () ? fusion : getActiveLayer ();
 
     samples.addLast (new CPColorFloat (layerToSample.getPixel (x, y) & 0xffffff));
 
     for (float r = 0.25f; r < 1.001f; r += .25f)
       {
         samples.addLast (new CPColorFloat (layerToSample.getPixel ((int) (x + r * dx), y) & 0xffffff));
         samples.addLast (new CPColorFloat (layerToSample.getPixel ((int) (x - r * dx), y) & 0xffffff));
         samples.addLast (new CPColorFloat (layerToSample.getPixel (x, (int) (y + r * dy)) & 0xffffff));
         samples.addLast (new CPColorFloat (layerToSample.getPixel (x, (int) (y - r * dy)) & 0xffffff));
 
         samples.addLast (new CPColorFloat (layerToSample.getPixel ((int) (x + r * .7f * dx), (int) (y + r * .7f
                 * dy)) & 0xffffff));
         samples.addLast (new CPColorFloat (layerToSample.getPixel ((int) (x + r * .7f * dx), (int) (y - r * .7f
                 * dy)) & 0xffffff));
         samples.addLast (new CPColorFloat (layerToSample.getPixel ((int) (x - r * .7f * dx), (int) (y + r * .7f
                 * dy)) & 0xffffff));
         samples.addLast (new CPColorFloat (layerToSample.getPixel ((int) (x - r * .7f * dx), (int) (y - r * .7f
                 * dy)) & 0xffffff));
       }
 
     CPColorFloat average = new CPColorFloat (0, 0, 0);
     for (CPColorFloat sample : samples)
       {
         average.r += sample.r;
         average.g += sample.g;
         average.b += sample.b;
       }
     average.r /= samples.size ();
     average.g /= samples.size ();
     average.b /= samples.size ();
 
     return average;
   }
 }
 
 class CPBrushToolOil extends CPBrushToolDirectBrush
 {
 
   @Override
   void paintDabImplementation (CPRect srcRect, CPRect dstRect, CPBrushDab dab)
   {
     if (brushBuffer == null)
       {
         brushBuffer = new int[dab.width * dab.height];
         for (int i = brushBuffer.length - 1; --i >= 0; )
           {
             brushBuffer[i] = 0;
           }
         // activeLayer.copyRect(dstRect, brushBuffer);
         oilAccumBuffer (srcRect, dstRect, brushBuffer, dab.width, 255);
       }
     else
       {
         oilResatBuffer (srcRect, dstRect, brushBuffer, dab.width, (int) ((curBrush.resat <= 0f) ? 0 : Math.max (
                 1, (curBrush.resat * curBrush.resat) * 255)), curColor & 0xffffff);
         oilPasteBuffer (srcRect, dstRect, brushBuffer, dab.brush, dab.width, dab.alpha);
         oilAccumBuffer (srcRect, dstRect, brushBuffer, dab.width, (int) ((curBrush.bleed) * 255));
       }
     mergeOpacityBuffer (0, false);
     if (isSampleAllLayers ())
       {
         fusionLayers ();
       }
   }
 
   private void oilAccumBuffer (CPRect srcRect, CPRect dstRect, int[] buffer, int w, int alpha)
   {
     CPLayer layerToSample = isSampleAllLayers () ? fusion : getActiveLayer ();
 
     int by = srcRect.top;
     for (int j = dstRect.top; j < dstRect.bottom; j++, by++)
       {
         int srcOffset = srcRect.left + by * w;
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, srcOffset++, dstOffset++)
           {
             int color1 = layerToSample.getData ()[dstOffset];
             int alpha1 = (color1 >>> 24) * alpha / 255;
             if (alpha1 <= 0)
               {
                 continue;
               }
 
             int color2 = buffer[srcOffset];
             int alpha2 = (color2 >>> 24);
 
             int newAlpha = alpha1 + alpha2 - alpha1 * alpha2 / 255;
             if (newAlpha > 0)
               {
                 int realAlpha = alpha1 * 255 / newAlpha;
                 int invAlpha = 255 - realAlpha;
 
                 int newColor = newAlpha << 24
                         | ((color1 >>> 16 & 0xff) + (((color2 >>> 16 & 0xff) * invAlpha - (color1 >>> 16 & 0xff)
                         * invAlpha) / 255)) << 16
                         | ((color1 >>> 8 & 0xff) + (((color2 >>> 8 & 0xff) * invAlpha - (color1 >>> 8 & 0xff)
                         * invAlpha) / 255)) << 8
                         | ((color1 & 0xff) + (((color2 & 0xff) * invAlpha - (color1 & 0xff) * invAlpha) / 255));
 
                 buffer[srcOffset] = newColor;
               }
           }
       }
   }
 
   private void oilResatBuffer (CPRect srcRect, CPRect dstRect, int[] buffer, int w, int alpha1, int color1)
   {
     if (alpha1 <= 0)
       {
         return;
       }
 
     int by = srcRect.top;
     for (int j = dstRect.top; j < dstRect.bottom; j++, by++)
       {
         int srcOffset = srcRect.left + by * w;
         for (int i = dstRect.left; i < dstRect.right; i++, srcOffset++)
           {
             int color2 = buffer[srcOffset];
             int alpha2 = (color2 >>> 24);
 
             int newAlpha = alpha1 + alpha2 - alpha1 * alpha2 / 255;
             if (newAlpha > 0)
               {
                 int realAlpha = alpha1 * 255 / newAlpha;
                 int invAlpha = 255 - realAlpha;
 
                 int newColor = newAlpha << 24
                         | ((color1 >>> 16 & 0xff) + (((color2 >>> 16 & 0xff) * invAlpha - (color1 >>> 16 & 0xff)
                         * invAlpha) / 255)) << 16
                         | ((color1 >>> 8 & 0xff) + (((color2 >>> 8 & 0xff) * invAlpha - (color1 >>> 8 & 0xff)
                         * invAlpha) / 255)) << 8
                         | ((color1 & 0xff) + (((color2 & 0xff) * invAlpha - (color1 & 0xff) * invAlpha) / 255));
 
                 buffer[srcOffset] = newColor;
               }
           }
       }
   }
 
   private void oilPasteBuffer (CPRect srcRect, CPRect dstRect, int[] buffer, byte[] brush, int w, int alpha)
   {
     int[] opacityData = opacityBuffer.getData ();
 
     int by = srcRect.top;
     for (int j = dstRect.top; j < dstRect.bottom; j++, by++)
       {
         int srcOffset = srcRect.left + by * w;
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, srcOffset++, dstOffset++)
           {
             int color1 = buffer[srcOffset];
             int alpha1 = (color1 >>> 24) * (brush[srcOffset] & 0xff) * alpha / (255 * 255);
             if (alpha1 <= 0)
               {
                 continue;
               }
 
             int color2 = getActiveLayer ().getData ()[dstOffset];
             int alpha2 = (color2 >>> 24);
 
             int newAlpha = alpha1 + alpha2 - alpha1 * alpha2 / 255;
             if (newAlpha > 0)
               {
                 int realAlpha = alpha1 * 255 / newAlpha;
                 int invAlpha = 255 - realAlpha;
 
                 int newColor = newAlpha << 24
                         | ((color1 >>> 16 & 0xff) + (((color2 >>> 16 & 0xff) * invAlpha - (color1 >>> 16 & 0xff)
                         * invAlpha) / 255)) << 16
                         | ((color1 >>> 8 & 0xff) + (((color2 >>> 8 & 0xff) * invAlpha - (color1 >>> 8 & 0xff)
                         * invAlpha) / 255)) << 8
                         | ((color1 & 0xff) + (((color2 & 0xff) * invAlpha - (color1 & 0xff) * invAlpha) / 255));
 
                 opacityData[dstOffset] = newColor;
 
               }
           }
       }
   }
 }
 
 class CPBrushToolSmudge extends CPBrushToolDirectBrush
 {
 
   @Override
   void paintDabImplementation (CPRect srcRect, CPRect dstRect, CPBrushDab dab)
   {
     if (brushBuffer == null)
       {
         brushBuffer = new int[dab.width * dab.height];
         smudgeAccumBuffer (srcRect, dstRect, brushBuffer, dab.width, 0);
       }
     else
       {
         smudgeAccumBuffer (srcRect, dstRect, brushBuffer, dab.width, dab.alpha);
         smudgePasteBuffer (srcRect, dstRect, brushBuffer, dab.brush, dab.width);
 
         if (isLockAlpha ())
           {
             undoManager.restoreActiveLayerAlpha (dstRect);
           }
       }
     opacityArea.makeEmpty ();
     if (isSampleAllLayers ())
       {
         fusionLayers ();
       }
   }
 
   @Override
   public void mergeOpacityBuf (CPRect dstRect, int color)
   {
     // Don't know what should be there
   }
 
   private void smudgeAccumBuffer (CPRect srcRect, CPRect dstRect, int[] buffer, int w, int alpha)
   {
 
     CPLayer layerToSample = isSampleAllLayers () ? fusion : getActiveLayer ();
 
     int by = srcRect.top;
     for (int j = dstRect.top; j < dstRect.bottom; j++, by++)
       {
         int srcOffset = srcRect.left + by * w;
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, srcOffset++, dstOffset++)
           {
             int layerColor = layerToSample.getData ()[dstOffset];
             int opacityAlpha = 255 - alpha;
             if (opacityAlpha > 0)
               {
                 int destColor = buffer[srcOffset];
 
                 int destAlpha = 255;
                 int newLayerAlpha = opacityAlpha + destAlpha * (255 - opacityAlpha) / 255;
                 int realAlpha = 255 * opacityAlpha / newLayerAlpha;
                 int invAlpha = 255 - realAlpha;
 
                 int newColor = (((layerColor >>> 24 & 0xff) * realAlpha + (destColor >>> 24 & 0xff) * invAlpha) / 255) << 24
                         & 0xff000000
                         | (((layerColor >>> 16 & 0xff) * realAlpha + (destColor >>> 16 & 0xff) * invAlpha) / 255) << 16
                         & 0xff0000
                         | (((layerColor >>> 8 & 0xff) * realAlpha + (destColor >>> 8 & 0xff) * invAlpha) / 255) << 8
                         & 0xff00
                         | (((layerColor & 0xff) * realAlpha + (destColor & 0xff) * invAlpha) / 255)
                         & 0xff;
 
                 if (newColor == destColor)
                   {
                     if ((layerColor & 0xff0000) > (destColor & 0xff0000))
                       {
                         newColor += 1 << 16;
                       }
                     else if ((layerColor & 0xff0000) < (destColor & 0xff0000))
                       {
                         newColor -= 1 << 16;
                       }
 
                     if ((layerColor & 0xff00) > (destColor & 0xff00))
                       {
                         newColor += 1 << 8;
                       }
                     else if ((layerColor & 0xff00) < (destColor & 0xff00))
                       {
                         newColor -= 1 << 8;
                       }
 
                     if ((layerColor & 0xff) > (destColor & 0xff))
                       {
                         newColor += 1;
                       }
                     else if ((layerColor & 0xff) < (destColor & 0xff))
                       {
                         newColor -= 1;
                       }
                   }
 
                 buffer[srcOffset] = newColor;
               }
           }
       }
 
     if (srcRect.left > 0)
       {
         int fill = srcRect.left;
         for (int j = srcRect.top; j < srcRect.bottom; j++)
           {
             int offset = j * w;
             int fillColor = buffer[offset + srcRect.left];
             for (int i = 0; i < fill; i++)
               {
                 buffer[offset++] = fillColor;
               }
           }
       }
 
     if (srcRect.right < w)
       {
         int fill = w - srcRect.right;
         for (int j = srcRect.top; j < srcRect.bottom; j++)
           {
             int offset = j * w + srcRect.right;
             int fillColor = buffer[offset - 1];
             for (int i = 0; i < fill; i++)
               {
                 buffer[offset++] = fillColor;
               }
           }
       }
 
     for (int j = 0; j < srcRect.top; j++)
       {
         System.arraycopy (buffer, srcRect.top * w, buffer, j * w, w);
       }
 
     for (int j = srcRect.bottom; j < w; j++)
       {
         System.arraycopy (buffer, (srcRect.bottom - 1) * w, buffer, j * w, w);
       }
   }
 
   private void smudgePasteBuffer (CPRect srcRect, CPRect dstRect, int[] buffer, byte[] brush, int w)
   {
     int[] undoData = undoManager.getActiveLayerPreservedData ();
 
     int by = srcRect.top;
     for (int j = dstRect.top; j < dstRect.bottom; j++, by++)
       {
         int srcOffset = srcRect.left + by * w;
         int dstOffset = dstRect.left + j * getWidth ();
         for (int i = dstRect.left; i < dstRect.right; i++, srcOffset++, dstOffset++)
           {
             int bufferColor = buffer[srcOffset];
             int opacityAlpha = (bufferColor >>> 24) * (brush[srcOffset] & 0xff) / 255;
             if (opacityAlpha > 0)
               {
                 int destColor = undoData[dstOffset];
 
                 int realAlpha = 255;
                 int invAlpha = 255 - realAlpha;
 
                 int newColor = (((bufferColor >>> 24 & 0xff) * realAlpha + (destColor >>> 24 & 0xff) * invAlpha) / 255) << 24
                         & 0xff000000
                         | (((bufferColor >>> 16 & 0xff) * realAlpha + (destColor >>> 16 & 0xff) * invAlpha) / 255) << 16
                         & 0xff0000
                         | (((bufferColor >>> 8 & 0xff) * realAlpha + (destColor >>> 8 & 0xff) * invAlpha) / 255) << 8
                         & 0xff00
                         | (((bufferColor & 0xff) * realAlpha + (destColor & 0xff) * invAlpha) / 255)
                         & 0xff;
 
                 getActiveLayer ().getData ()[dstOffset] = newColor;
               }
           }
       }
   }
 }
 
 // ///////////////////////////////////////////////////////////////////////////////////
 // Layer methods
 // ///////////////////////////////////////////////////////////////////////////////////
 
 public void setActiveLayerNumber (int i)
 {
   if (i < 0 || i >= getLayersVector ().size ())
     {
       return;
     }
 
   setActiveLayerNum (i);
   activeLayer = getLayersVector ().get (i);
   callListenersLayerChange ();
 }
 
 public int getActiveLayerNb ()
 {
   return getActiveLayerNum ();
 }
 
 public CPLayer getActiveLayer ()
 {
   return activeLayer;
 }
 
 public CPLayer getLayer (int i)
 {
   if (i < 0 || i >= getLayersVector ().size ())
     {
       return null;
     }
 
   return getLayersVector ().get (i);
 }
 
 //
 // Undo / Redo
 //
 
 public void undo ()
 {
   if (!canUndo ())
     {
       return;
     }
 
   CPUndo undo = undoManager.getUndoList ().removeFirst ();
   undo.undo ();
   undoManager.getRedoList ().addFirst (undo);
 }
 
 public void redo ()
 {
   if (!canRedo ())
     {
       return;
     }
 
   CPUndo redo = undoManager.getRedoList ().removeFirst ();
   redo.redo ();
   undoManager.getUndoList ().addFirst (redo);
 }
 
 boolean canUndo ()
 {
   return !undoManager.getUndoList ().isEmpty ();
 }
 
 boolean canRedo ()
 {
   return !undoManager.getRedoList ().isEmpty ();
 }
 
 public void clearHistory ()
 {
   undoManager.getUndoList ().clear ();
   undoManager.getRedoList ().clear ();
 
   Runtime r = Runtime.getRuntime ();
   r.gc ();
 }
 
 //
 //
 //
 
 public int colorPicker (float x, float y)
 {
   // not really necessary and could potentially the repaint
   // of the canvas to miss that area
   // fusionLayers();
   if (isSampleAllLayers ())
     return fusion.getPixel ((int) x, (int) y) & 0xffffff;
   else
     return getActiveLayer ().getPixel ((int) x, (int) y) & 0xffffff;
 }
 
 public boolean isPointWithin (float x, float y)
 {
   return x >= 0 && y >= 0 && (int) x < getWidth () && (int) y < getHeight ();
 }
 
 // FIXME: 2007-01-13 I'm moving this to the CPRect class
 // find where this version is used and change the
 // code to use the CPRect version
 
 void clipSourceDest (CPRect srcRect, CPRect dstRect)
 {
   // FIXME:
   // /!\ dstRect bottom and right are ignored and instead we clip
   // against the width, height of the layer. :/
   //
 
   // this version would be enough in most cases (when we don't need
   // srcRect bottom and right to be clipped)
   // it's left here in case it's needed to make a faster version
   // of this function
   // dstRect.right = Math.min(width, dstRect.left + srcRect.getWidth());
   // dstRect.bottom = Math.min(height, dstRect.top + srcRect.getHeight());
 
   // new dest bottom/right
   dstRect.right = dstRect.left + srcRect.getWidth ();
   if (dstRect.right > getWidth ())
     {
       srcRect.right -= dstRect.right - getWidth ();
       dstRect.right = getWidth ();
     }
 
   dstRect.bottom = dstRect.top + srcRect.getHeight ();
   if (dstRect.bottom > getHeight ())
     {
       srcRect.bottom -= dstRect.bottom - getHeight ();
       dstRect.bottom = getHeight ();
     }
 
   // new src top/left
   if (dstRect.left < 0)
     {
       srcRect.left -= dstRect.left;
       dstRect.left = 0;
     }
 
   if (dstRect.top < 0)
     {
       srcRect.top -= dstRect.top;
       dstRect.top = 0;
     }
 }
 
 public Object[] getLayers ()
 {
   return getLayersVector ().toArray ();
 }
 
 public int getLayersNb ()
 {
   return getLayersVector ().size ();
 }
 
 public CPRect getSize ()
 {
   return new CPRect (getWidth (), getHeight ());
 }
 
 //
 // Selection methods
 //
 
 
 //
 //
 //
 
 public void invalidateFusion (CPRect r)
 {
   fusionArea.union (r);
   callListenersUpdateRegion (r);
 }
 
 public void invalidateFusion ()
 {
   invalidateFusion (new CPRect (0, 0, getWidth (), getHeight ()));
 }
 
 public void setLayerVisibility (int layer, boolean visible)
 {
 
   boolean oldVisibility = getLayer (layer).isVisible ();
   getLayer (layer).setVisible (visible);
   invalidateFusion ();
   callListenersLayerChange ();
   undoManager.activeLayerVisibilityChanged (layer, oldVisibility);
 }
 
 public void addLayer ()
 {
 
   CPLayer newLayer = new CPLayer (getWidth (), getHeight ());
   newLayer.setName (getDefaultLayerName ());
   getLayersVector ().add (getActiveLayerNum () + 1, newLayer);
   setActiveLayerNumber (getActiveLayerNum () + 1);
   undoManager.layerWasAppended ();
 
   invalidateFusion ();
   callListenersLayerChange ();
 }
 
 public void removeLayer ()
 {
   if (getLayersVector ().size () > 1)
     {
       int previouslyActiveLayerNum = getActiveLayerNum ();
       undoManager.preserveActiveLayerData ();
       getLayersVector ().remove (getActiveLayerNum ());
       setActiveLayerNumber (getActiveLayerNum () < getLayersVector ().size () ? getActiveLayerNum () : getActiveLayerNum () - 1);
       undoManager.layerWasRemoved (previouslyActiveLayerNum);
       invalidateFusion ();
       callListenersLayerChange ();
     }
 }
 
 public void toggleLayers ()
 {
   undoManager.preserveLayersCheckState ();
   int i, first_unchecked_pos = 0;
   for (i = 0; i < getLayersVector ().size (); i++)
     if (!getLayersVector ().elementAt (i).isVisible ())
       break;
   first_unchecked_pos = i;
 
   for (i = 0; i < getLayersVector ().size (); i++)
     getLayersVector ().elementAt (i).setVisible ((first_unchecked_pos < getLayersVector ().size ()));
 
   undoManager.layersCheckStateWasToggled ();
 
   invalidateFusion ();
   callListenersLayerChange ();
 }
 
 public void duplicateLayer ()
 {
   String copySuffix = " Copy";
 
   CPLayer newLayer = new CPLayer (getWidth (), getHeight ());
   newLayer.copyFrom (getLayersVector ().elementAt (getActiveLayerNum ()));
   if (!newLayer.getName ().endsWith (copySuffix))
     {
       newLayer.setName (newLayer.getName () + copySuffix);
     }
   getLayersVector ().add (getActiveLayerNum () + 1, newLayer);
 
   setActiveLayerNumber (getActiveLayerNum () + 1);
   undoManager.layerDuplicationTookPlace ();
   invalidateFusion ();
   callListenersLayerChange ();
 }
 
 public void mergeDown ()
 {
   if (getLayersVector ().size () > 0 && getActiveLayerNum () > 0)
     {
       undoManager.beforeMergingLayer ();
       getLayersVector ().elementAt (getActiveLayerNum ()).fusionWithFullAlpha (getLayersVector ().elementAt (getActiveLayerNum () - 1),
                                                                                new CPRect (getWidth (), getHeight ()));
       getLayersVector ().remove (getActiveLayerNum ());
       setActiveLayerNumber (getActiveLayerNum () - 1);
 
       invalidateFusion ();
       callListenersLayerChange ();
     }
 }
 
 public void mergeAllLayers ()
 {
   if (getLayersVector ().size () > 1)
     {
 
       undoManager.beforeMergingAllLayers ();
 
       fusionLayers ();
       getLayersVector ().clear ();
 
       CPLayer layer = new CPLayer (getWidth (), getHeight ());
       layer.setName (getDefaultLayerName ());
       layer.copyDataFrom (fusion);
       getLayersVector ().add (layer);
       setActiveLayerNumber (0);
 
       invalidateFusion ();
       callListenersLayerChange ();
     }
 }
 
 public void moveLayer (int from, int to)
 {
   if (from < 0 || from >= getLayersNb () || to < 0 || to > getLayersNb () || from == to)
     {
       return;
     }
   undoManager.beforeLayerMove (from, to);
   moveLayerReal (from, to);
 }
 
 void moveLayerReal (int from, int to)
 {
   CPLayer layer = getLayersVector ().remove (from);
   if (to <= from)
     {
       getLayersVector ().add (to, layer);
       setActiveLayerNumber (to);
     }
   else
     {
       getLayersVector ().add (to - 1, layer);
       setActiveLayerNumber (to - 1);
     }
 
   invalidateFusion ();
   callListenersLayerChange ();
 }
 
 public void setLayerAlpha (int layer, int alpha)
 {
   if (getLayer (layer).getAlpha () != alpha)
     {
       undoManager.beforeLayerAlphaChange (this, layer, alpha);
       getLayer (layer).setAlpha (alpha);
       invalidateFusion ();
       callListenersLayerChange ();
     }
 }
 
 public void setBlendMode (int layer, int blendMode)
 {
   if (getLayer (layer).getBlendMode () != blendMode)
     {
       undoManager.beforeChangingLayerBlendMode (layer, blendMode);
       getLayer (layer).setBlendMode (blendMode);
       invalidateFusion ();
       callListenersLayerChange ();
     }
 }
 
 public void setLayerName (int layer, String name)
 {
   if (!getLayer (layer).getName ().equals (name))
     {
       undoManager.beforeLayerRename (layer, name);
       getLayer (layer).setName (name);
       callListenersLayerChange ();
     }
 }
 
 public void floodFill (float x, float y, int colorDistance)
 {
   undoManager.preserveActiveLayerData ();
 
   getActiveLayer ().floodFill ((int) x, (int) y, curColor | 0xff000000, isSampleAllLayers () ? fusion : getActiveLayer (), colorDistance);
 
   undoManager.activeLayerDataChange (new CPRect (getWidth (), getHeight ()));
   invalidateFusion ();
 }
 
 public void fill (int color, boolean applyToAllLayers)
 {
   // TODO: Limit to selection
   CPRect r = getSize ();
 
   if (!applyToAllLayers)
     {
       undoManager.preserveActiveLayerData ();
 
       getActiveLayer ().clear (r, color);
       undoManager.activeLayerDataChange (getSize ());
     }
   else
     {
       undoManager.preserveAllLayersState ();
       for (int i = 0; i < getLayersVector ().size (); i++)
         {
           getLayersVector ().elementAt (i).clear (r, color);
 
         }
       undoManager.allLayersChanged (getSize ());
     }
 
   invalidateFusion ();
 }
 
 public void clear (boolean applyToAllLayers)
 {
   fill (0xffffff, applyToAllLayers);
 }
 
 public void hFlip (boolean applyToAllLayers)
 {
   if (!applyToAllLayers)
     {
       undoManager.preserveActiveLayerData ();
 
       getActiveLayer ().copyRegionHFlip (getSize (), undoManager.getActiveLayerPreservedData ());
       undoManager.activeLayerDataChange (getSize ());
     }
   else
     {
       undoManager.preserveAllLayersState ();
       for (int i = 0; i < getLayersVector ().size (); i++)
         {
           getLayersVector ().elementAt (i).copyRegionHFlip (getSize (), undoManager.getPreservedAllLayers ().elementAt (i).getData ());
 
         }
       undoManager.allLayersChanged (getSize ());
     }
 
   invalidateFusion ();
 }
 
 public void vFlip (boolean applyToAllLayers)
 {
 
   if (!applyToAllLayers)
     {
       undoManager.preserveActiveLayerData ();
 
       getActiveLayer ().copyRegionVFlip (getSize (), undoManager.getActiveLayerPreservedData ());
       undoManager.activeLayerDataChange (getSize ());
     }
   else
     {
       undoManager.preserveAllLayersState ();
       for (int i = 0; i < getLayersVector ().size (); i++)
         {
           getLayersVector ().elementAt (i).copyRegionVFlip (getSize (), undoManager.getPreservedAllLayers ().elementAt (i).getData ());
 
         }
       undoManager.allLayersChanged (getSize ());
     }
   invalidateFusion ();
 }
 
 public void monochromaticNoise (boolean applyToAllLayers)
 {
 
   if (!applyToAllLayers)
     {
       undoManager.preserveActiveLayerData ();
 
       getActiveLayer ().fillWithNoise (getSize ());
       undoManager.activeLayerDataChange (getSize ());
     }
   else
     {
       undoManager.preserveAllLayersState ();
       for (int i = 0; i < getLayersVector ().size (); i++)
         {
           getLayersVector ().elementAt (i).fillWithNoise (getSize ());
 
         }
       undoManager.allLayersChanged (getSize ());
     }
 
   invalidateFusion ();
 }
 
 public void colorNoise (boolean applyToAllLayers)
 {
 
   if (!applyToAllLayers)
     {
       undoManager.preserveActiveLayerData ();
 
       getActiveLayer ().fillWithColorNoise (getSize ());
       undoManager.activeLayerDataChange (getSize ());
     }
   else
     {
       undoManager.preserveAllLayersState ();
       for (int i = 0; i < getLayersVector ().size (); i++)
         {
           getLayersVector ().elementAt (i).fillWithColorNoise (getSize ());
 
         }
       undoManager.allLayersChanged (getSize ());
     }
 
   invalidateFusion ();
 }
 
 public void boxBlur (int radiusX, int radiusY, int iterations, boolean applyToAllLayers)
 {
 
   if (!applyToAllLayers)
     {
       undoManager.preserveActiveLayerData ();
 
       for (int c = 0; c < iterations; c++)
         {
           getActiveLayer ().boxBlur (getSize (), radiusX, radiusY);
         }
       undoManager.activeLayerDataChange (getSize ());
     }
   else
     {
       undoManager.preserveAllLayersState ();
       for (int i = 0; i < getLayersVector ().size (); i++)
         {
           for (int c = 0; c < iterations; c++)
             {
               getLayersVector ().elementAt (i).boxBlur (getSize (), radiusX, radiusY);
             }
         }
       undoManager.allLayersChanged (getSize ());
     }
 
   invalidateFusion ();
 }
 
 public void invert (boolean applyToAllLayers)
 {
 
   if (!applyToAllLayers)
     {
       undoManager.preserveActiveLayerData ();
 
       getActiveLayer ().invert (getSize ());
 
       undoManager.activeLayerDataChange (getSize ());
     }
   else
     {
       undoManager.preserveAllLayersState ();
       for (int i = 0; i < getLayersVector ().size (); i++)
         {
           getLayersVector ().elementAt (i).invert (getSize ());
         }
       undoManager.allLayersChanged (getSize ());
     }
 
   invalidateFusion ();
 }
 
 public void makeMonochrome (boolean applyToAllLayers, int type)
 {
 
   if (!applyToAllLayers)
     {
       undoManager.preserveActiveLayerData ();
 
       getActiveLayer ().makeMonochrome (getSize (), type, curColor);
 
       undoManager.activeLayerDataChange (getSize ());
     }
   else
     {
       undoManager.preserveAllLayersState ();
       for (int i = 0; i < getLayersVector ().size (); i++)
         {
           getLayersVector ().elementAt (i).makeMonochrome (getSize (), type, curColor);
         }
       undoManager.allLayersChanged (getSize ());
     }
 
   invalidateFusion ();
 }
 
 // ////
 // Copy/Paste
 
 // ////////////////////////////////////////////////////
 // Miscellaneous functions
 
 public String getDefaultLayerName ()
 {
   String prefix = "Layer ";
   int highestLayerNb = 0;
   for (CPLayer l : getLayersVector ())
     {
       if (l.getName ().matches ("^" + prefix + "[0-9]+$"))
         {
           highestLayerNb = Math.max (highestLayerNb, Integer.parseInt (l.getName ().substring (prefix.length ())));
         }
     }
   return prefix + (highestLayerNb + 1);
 }
 
 public boolean hasAlpha ()
 {
   return fusion.hasAlpha ();
 }
 
 // ////////////////////////////////////////////////////
 // Undo classes
 public Vector<CPLayer> getLayersVector ()
 {
   return layers;
 }
 
 public void setLayers (Vector<CPLayer> layers)
 {
   this.layers = layers;
 }
 
 public int getActiveLayerNum ()
 {
   return activeLayerNumber;
 }
 
 void setActiveLayerNum (int activeLayer)
 {
   this.activeLayerNumber = activeLayer;
 }
 
 public boolean isLockAlpha ()
 {
   return lockAlpha;
 }
 
 public boolean isSampleAllLayers ()
 {
   return sampleAllLayers;
 }
 
 }
