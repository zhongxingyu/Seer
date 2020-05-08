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
 
 import chibipaint.util.CPRect;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Vector;
 
 public class CPUndoManager
 {
 
 public int maxUndo = 30;
 
 private final LinkedList<CPUndo> undoList = new LinkedList<CPUndo> ();
 private final LinkedList<CPUndo> redoList = new LinkedList<CPUndo> ();
 private final CPArtwork artwork;
 private final ArrayList<CPUndo> pendingUndoList = new ArrayList<CPUndo> ();
 private boolean preservedActiveLayerVisibility;
 private final ArrayList<Boolean> preservedLayerCheckState = new ArrayList<Boolean> ();
 
 public CPSelection getPreservedSelection ()
 {
   return preservedSelection;
 }
 
 final CPSelection preservedSelection;
 final CPLayer preservedActiveLayer;
 Vector<CPLayer> preservedAllLayers;
 
 public CPLayer getPreservedActiveLayer ()
 {
   return preservedActiveLayer;
 }
 
 public CPUndoManager (CPArtwork artworkArg)
 {
   artwork = artworkArg;
   preservedActiveLayer = new CPLayer (artwork.getWidth (), artwork.getHeight ());
   preservedSelection = new CPSelection (artwork.getWidth (), artwork.getHeight ());
 }
 
 public LinkedList<CPUndo> getUndoList ()
 {
   return undoList;
 }
 
 public LinkedList<CPUndo> getRedoList ()
 {
   return redoList;
 }
 
 void restoreActiveLayerAlpha (CPRect r)
 {
   artwork.getActiveLayer ().copyAlphaFrom (preservedActiveLayer, r);
 }
 
 public void preserveActiveLayerData ()
 {
   preservedActiveLayer.copyFrom (artwork.getActiveLayer ());
 }
 
 public void preserveAllLayersState ()
 {
   preservedAllLayers = new Vector<CPLayer> (artwork.getLayersVector ().size ());
   preservedAllLayers.setSize (artwork.getLayersVector ().size ());
   for (int i = 0; i < artwork.getLayersVector ().size (); i++)
     {
       preservedAllLayers.setElementAt (new CPLayer (artwork.getWidth (), artwork.getHeight ()), i);
       preservedAllLayers.elementAt (i).copyFrom (artwork.getLayer (i));
     }
 }
 
 public int[] getActiveLayerPreservedData ()
 {
   return preservedActiveLayer.getData ();
 }
 
 public void preserveCurrentSelection ()
 {
   preservedSelection.copyFrom (artwork.getCurSelection ());
 }
 
public void currentSelectionChanged ()
 {
   CPRect rect = preservedSelection.getBoundingRect ();
   rect.union (artwork.getCurSelection ().getBoundingRect ());
   CPUndoSelection undo = new CPUndoSelection (artwork, preservedSelection, rect);
   pendingUndoList.add (undo);
 }
 
 public void finalizeUndo ()
 {
   if (pendingUndoList.size () == 0)
     return;
 
   if (pendingUndoList.size () == 1)
     {
       appendUndoToList (pendingUndoList.get (0));
       pendingUndoList.clear ();
     }
   else
     {
       appendUndoToList (new CPMultiUndo (pendingUndoList));
       pendingUndoList.clear ();
       // Create MultiUndo
     }
 }
 
 public void discardUndo ()
 {
   pendingUndoList.clear ();
 }
 
 void appendUndoToList (CPUndo undo)
 {
   if (undoList.isEmpty () || !(undoList.getFirst ()).merge (undo))
     {
       if (undoList.size () >= maxUndo)
         {
           undoList.removeLast ();
         }
       undoList.addFirst (undo);
     }
   else
     {
       // Two merged changes can mean no change at all
       // don't leave a useless undo in the list
       if ((undoList.getFirst ()).noChange ())
         {
           undoList.removeFirst ();
         }
     }
   redoList.clear ();
 }
 
 public void setMaxUndo (int maxUndo)
 {
   this.maxUndo = maxUndo;
 }
 
 public void activeLayerDataChange (CPRect rect)
 {
   CPUndo undo = new CPUndoPaint (rect);
   pendingUndoList.add (undo);
 }
 
 public void allLayersChanged (CPRect rect)
 {
   CPUndo undo = new CPUndoPaintAll (artwork, rect);
   pendingUndoList.add (undo);
 }
 
 public Vector<CPLayer> getPreservedAllLayers ()
 {
   return preservedAllLayers;
 }
 
 public void activeLayerVisibilityChanged (int layer, boolean oldVisibility)
 {
   CPUndo undo = new CPUndoLayerVisible (artwork, layer, oldVisibility, artwork.getLayer (layer).isVisible ());
   pendingUndoList.add (undo);
 }
 
 public long getUndoMemoryUsed (CPArtwork artwork)
 {
   long total = 0;
 
   CPColorBmp lastBitmap = artwork.getClipboard () != null ? artwork.getClipboard ().bmp : null;
 
   for (int i = getRedoList ().size () - 1; i >= 0; i--)
     {
       CPUndo undo = getRedoList ().get (i);
 
       total += undo.getMemoryUsed (true, lastBitmap);
     }
 
   for (CPUndo undo : getUndoList ())
     {
       total += undo.getMemoryUsed (false, lastBitmap);
     }
 
   return total;
 }
 
 public void layerWasAppended ()
 {
   CPUndo undo = new CPUndoAddLayer (artwork, artwork.getActiveLayerNum () - 1);
   pendingUndoList.add (undo);
 }
 
 public void layerWasRemoved (int previouslyActiveLayerNum)
 {
   CPUndo undo = new CPUndoRemoveLayer (artwork, previouslyActiveLayerNum, preservedActiveLayer);
   pendingUndoList.add (undo);
 }
 
 public void layerDuplicationTookPlace ()
 {
   CPUndo undo = new CPUndoDuplicateLayer (artwork, artwork.getActiveLayerNum () - 1);
   pendingUndoList.add (undo);
 }
 
 public void beforeMergingLayer ()
 {
   CPUndo undo = new CPUndoMergeDownLayer (artwork, artwork.getActiveLayerNum ());
   pendingUndoList.add (undo);
 }
 
 public void beforeMergingAllLayers ()
 {
   CPUndo undo = new CPUndoMergeAllLayers (artwork);
   pendingUndoList.add (undo);
 }
 
 public void preserveLayersCheckState ()
 {
 
   preservedLayerCheckState.clear ();
   boolean first = false;
   for (int i = 0; i < preservedLayerCheckState.size (); i++)
     {
       preservedLayerCheckState.set (i, artwork.getLayersVector ().elementAt (i).isVisible ());
     }
 }
 
 public void layersCheckStateWasToggled ()
 {
   CPUndo undo = new CPUndoToggleLayers (artwork, preservedLayerCheckState);
   pendingUndoList.add (undo);
 }
 
 public void beforeLayerMove (int from, int to)
 {
   CPUndo undo = new CPUndoMoveLayer (artwork, from, to);
   pendingUndoList.add (undo);
 }
 
 public void beforeLayerAlphaChange (CPArtwork artwork, int layer, int alpha)
 {
   CPUndo undo = new CPUndoLayerAlpha (artwork, layer, alpha);
   pendingUndoList.add (undo);
 }
 
 public void beforeChangingLayerBlendMode (int layer, int blendMode)
 {
   CPUndo undo = new CPUndoManager.CPUndoLayerMode (artwork, layer, blendMode);
   pendingUndoList.add (undo);
 }
 
 public void beforeLayerRename (int layer, String name)
 {
   CPUndo undo = new CPUndoManager.CPUndoLayerRename (artwork, layer, name);
   pendingUndoList.add (undo);
 }
 
 public void restoreActiveLayerData ()
 {
   artwork.getActiveLayer ().copyFrom (preservedActiveLayer);
 }
 
 public void restoreSelection ()
 {
   artwork.getCurSelection ().copyFrom (preservedSelection);
 }
 
 
 class CPUndoPaint extends CPUndo
 {
 
   final int layer;
   final CPRect rect;
   final int[] data;
 
 
   public CPUndoPaint (CPRect rectArg)
   {
     layer = artwork.getActiveLayerNb ();
     rect = new CPRect (rectArg);
 
     data = getPreservedActiveLayer ().copyRectXOR (artwork.getActiveLayer (), rect);
   }
 
   @Override
   public void undo ()
   {
     artwork.getLayer (layer).setRectXOR (data, rect);
     artwork.invalidateFusion (rect);
   }
 
   @Override
   public void redo ()
   {
     artwork.getLayer (layer).setRectXOR (data, rect);
     artwork.invalidateFusion (rect);
   }
 
   @Override
   public long getMemoryUsed (boolean undone, Object param)
   {
     return data.length * 4;
   }
 }
 
 class CPUndoPaintAll extends CPUndo
 {
 
   final Vector<int[]> data;
   final CPRect rect;
   private final CPArtwork artwork;
 
   public CPUndoPaintAll (CPArtwork artwork, CPRect rectArg)
   {
     this.artwork = artwork;
     data = new Vector<int[]> (artwork.getLayersVector ().size ());
     data.setSize (artwork.getLayersVector ().size ());
     rect = new CPRect (rectArg);
     for (int i = 0; i < artwork.getLayersVector ().size (); i++)
       data.setElementAt (preservedAllLayers.elementAt (i).copyRectXOR (artwork.getLayersVector ().elementAt (i), rect), i);
 
     preservedAllLayers = null;
   }
 
   @Override
   public void undo ()
   {
     for (int i = 0; i < artwork.getLayersVector ().size (); i++)
       artwork.getLayer (i).setRectXOR (data.elementAt (i), rect);
 
     artwork.invalidateFusion (rect);
   }
 
   @Override
   public void redo ()
   {
     for (int i = 0; i < artwork.getLayersVector ().size (); i++)
       artwork.getLayer (i).setRectXOR (data.elementAt (i), rect);
 
     artwork.invalidateFusion (rect);
   }
 
   @Override
   public long getMemoryUsed (boolean undone, Object param)
   {
     return (data.size () != 0) ? data.size () * data.elementAt (0).length * 4 : 0;
   }
 }
 
 static class CPUndoLayerVisible extends CPUndo
 {
 
   final int layer;
   final boolean oldVis;
   boolean newVis;
   private final CPArtwork artwork;
 
   public CPUndoLayerVisible (CPArtwork artwork, int layer, boolean oldVis, boolean newVis)
   {
     this.artwork = artwork;
     this.layer = layer;
     this.oldVis = oldVis;
     this.newVis = newVis;
   }
 
   @Override
   public void redo ()
   {
     artwork.getLayer (layer).setVisible (newVis);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public void undo ()
   {
     artwork.getLayer (layer).setVisible (oldVis);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public boolean merge (CPUndo u)
   {
     if (u instanceof CPUndoLayerVisible && layer == ((CPUndoLayerVisible) u).layer)
       {
         newVis = ((CPUndoLayerVisible) u).newVis;
         return true;
       }
     return false;
   }
 
   @Override
   public boolean noChange ()
   {
     return oldVis == newVis;
   }
 }
 
 class CPUndoMergeAllLayers extends CPUndo
 {
 
   final Vector<CPLayer> oldLayers;
   final int oldActiveLayer;
   private final CPArtwork artwork;
 
   @SuppressWarnings ("unchecked")
   public CPUndoMergeAllLayers (CPArtwork artwork)
   {
     this.artwork = artwork;
     oldLayers = (Vector<CPLayer>) artwork.getLayersVector ().clone ();
     oldActiveLayer = artwork.getActiveLayerNb ();
   }
 
   @Override
   @SuppressWarnings ("unchecked")
   public void undo ()
   {
     artwork.setLayers ((Vector<CPLayer>) oldLayers.clone ());
     artwork.setActiveLayerNumber (oldActiveLayer);
 
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public void redo ()
   {
     artwork.mergeAllLayers ();
     discardUndo ();
   }
 
   @Override
   public long getMemoryUsed (boolean undone, Object param)
   {
     return undone ? 0 : oldLayers.size () * artwork.getWidth () * artwork.getHeight () * 4;
   }
 }
 
 class CPUndoMergeDownLayer extends CPUndo
 {
 
   final int layer;
   CPLayer layerBottom, layerTop;
   private final CPArtwork artwork;
 
   public CPUndoMergeDownLayer (CPArtwork artwork, int layer)
   {
     this.artwork = artwork;
     this.layer = layer;
     layerBottom = new CPLayer (artwork.getWidth (), artwork.getHeight ());
     layerBottom.copyFrom (artwork.getLayersVector ().elementAt (layer - 1));
     layerTop = artwork.getLayersVector ().elementAt (layer);
   }
 
   @Override
   public void undo ()
   {
     artwork.getLayersVector ().elementAt (layer - 1).copyFrom (layerBottom);
     artwork.getLayersVector ().add (layer, layerTop);
     artwork.setActiveLayerNumber (layer);
 
     layerBottom = layerTop = null;
 
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public void redo ()
   {
     layerBottom = new CPLayer (artwork.getWidth (), artwork.getHeight ());
     layerBottom.copyFrom (artwork.getLayersVector ().elementAt (layer - 1));
     layerTop = artwork.getLayersVector ().elementAt (layer);
 
     artwork.setActiveLayerNumber (layer);
     artwork.mergeDown ();
     discardUndo ();
   }
 
   @Override
   public long getMemoryUsed (boolean undone, Object param)
   {
     return undone ? 0 : artwork.getWidth () * artwork.getHeight () * 4 * 2;
   }
 }
 
 static class CPUndoMoveLayer extends CPUndo
 {
 
   final int from;
   final int to;
   private final CPArtwork artwork;
 
   public CPUndoMoveLayer (CPArtwork artwork, int from, int to)
   {
     this.artwork = artwork;
     this.from = from;
     this.to = to;
   }
 
   @Override
   public void undo ()
   {
     if (to <= from)
       {
         artwork.moveLayerReal (to, from + 1);
       }
     else
       {
         artwork.moveLayerReal (to - 1, from);
       }
   }
 
   @Override
   public void redo ()
   {
     artwork.moveLayerReal (from, to);
   }
 }
 
 static class CPUndoRemoveLayer extends CPUndo
 {
 
   final int layer;
   final CPLayer layerObj;
   private final CPArtwork artwork;
 
   public CPUndoRemoveLayer (CPArtwork artwork, int layer, CPLayer layerObj)
   {
     this.artwork = artwork;
     this.layer = layer;
     this.layerObj = layerObj;
   }
 
   @Override
   public void undo ()
   {
     artwork.getLayersVector ().add (layer, layerObj);
     artwork.setActiveLayerNumber (layer);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public void redo ()
   {
     artwork.getLayersVector ().remove (layer);
     artwork.setActiveLayerNumber (layer < artwork.getLayersVector ().size () ? layer : layer - 1);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public long getMemoryUsed (boolean undone, Object param)
   {
     return undone ? 0 : artwork.getWidth () * artwork.getHeight () * 4;
   }
 
 }
 
 static class CPUndoSelection extends CPUndo
 {
 
   final CPRect rect;
   final byte[] data;
   private final CPArtwork artwork;
 
   public CPUndoSelection (CPArtwork artwork, CPSelection pastSelection, CPRect changedRect)
   {
     this.artwork = artwork;
     rect = (CPRect) changedRect.clone ();
     data = pastSelection.copyRectXOR (artwork.curSelection, changedRect);
   }
 
   @Override
   public void undo ()
   {
     artwork.curSelection.setRectXOR (data, rect);
     artwork.curSelection.precalculateSelection ();
     artwork.invalidateFusion (rect);
   }
 
   @Override
   public void redo ()
   {
     artwork.curSelection.setRectXOR (data, rect);
     artwork.curSelection.precalculateSelection ();
     artwork.invalidateFusion (rect);
   }
 
   @Override
   public boolean merge (CPUndo u)
   {
     return false;
   }
 
   @Override
   public boolean noChange ()
   {
     for (byte aData : data)
       {
         if (aData > 0)
           {
             return false;
           }
       }
 
     return true;
   }
 }
 
 static class CPUndoAddLayer extends CPUndo
 {
 
   final int layer;
   private final CPArtwork artwork;
 
   public CPUndoAddLayer (CPArtwork artwork, int layer)
   {
     this.artwork = artwork;
     this.layer = layer;
   }
 
   @Override
   public void undo ()
   {
     artwork.getLayersVector ().remove (layer + 1);
     artwork.setActiveLayerNumber (layer);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public void redo ()
   {
     CPLayer newLayer = new CPLayer (artwork.getWidth (), artwork.getHeight ());
     newLayer.setName (artwork.getDefaultLayerName ());
     artwork.getLayersVector ().add (layer + 1, newLayer);
 
     artwork.setActiveLayerNumber (layer + 1);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 }
 
 static class CPUndoDuplicateLayer extends CPUndo
 {
 
   final int layer;
   private final CPArtwork artwork;
 
   public CPUndoDuplicateLayer (CPArtwork artwork, int layer)
   {
     this.artwork = artwork;
     this.layer = layer;
   }
 
   @Override
   public void undo ()
   {
     artwork.getLayersVector ().remove (layer + 1);
     artwork.setActiveLayerNumber (layer);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public void redo ()
   {
     String copySuffix = " Copy";
 
     CPLayer newLayer = new CPLayer (artwork.getWidth (), artwork.getHeight ());
     newLayer.copyFrom (artwork.getLayersVector ().elementAt (layer));
     if (!newLayer.getName ().endsWith (copySuffix))
       {
         newLayer.setName (newLayer.getName () + copySuffix);
       }
     artwork.getLayersVector ().add (layer + 1, newLayer);
 
     artwork.setActiveLayerNumber (layer + 1);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 }
 
 static class CPUndoLayerAlpha extends CPUndo
 {
 
   final int layer;
   final int from;
   int to;
   private final CPArtwork artwork;
 
   public CPUndoLayerAlpha (CPArtwork artwork, int layer, int alpha)
   {
     this.artwork = artwork;
     this.from = artwork.getLayer (layer).getAlpha ();
     this.to = alpha;
     this.layer = layer;
   }
 
   @Override
   public void undo ()
   {
     artwork.getLayer (layer).setAlpha (from);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public void redo ()
   {
     artwork.getLayer (layer).setAlpha (to);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public boolean merge (CPUndo u)
   {
     if (u instanceof CPUndoLayerAlpha && layer == ((CPUndoLayerAlpha) u).layer)
       {
         to = ((CPUndoLayerAlpha) u).to;
         return true;
       }
     return false;
   }
 
   @Override
   public boolean noChange ()
   {
     return from == to;
   }
 }
 
 class CPMultiUndo extends CPUndo
 {
   final ArrayList<CPUndo> undoList = new ArrayList<CPUndo> ();
 
   public CPMultiUndo (ArrayList<CPUndo> undoListArg)
   {
     for (CPUndo undo : undoListArg)
       undoList.add (undo);
   }
 
   @Override
   public void undo ()
   {
     // undoing it backwards.
     for (int i = undoList.size () - 1; i >= 0; i--)
       undoList.get (i).undo ();
   }
 
   @Override
   public void redo ()
   {
     for (CPUndo undo : undoList)
       undo.redo ();
   }
 
   @Override
   public long getMemoryUsed (boolean undone, Object param)
   {
     int memoryUsed = 0;
     for (CPUndo undo : undoList)
       memoryUsed += undo.getMemoryUsed (undone, param);
     return memoryUsed;
   }
 }
 
 static class CPUndoLayerMode extends CPUndo
 {
 
   final int layer;
   final int from;
   int to;
   private final CPArtwork artwork;
 
   public CPUndoLayerMode (CPArtwork artwork, int layer, int mode)
   {
     this.artwork = artwork;
     this.from = artwork.getLayer (layer).getBlendMode ();
     this.to = mode;
     this.layer = layer;
   }
 
   @Override
   public void undo ()
   {
     artwork.getLayer (layer).setBlendMode (from);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public void redo ()
   {
     artwork.getLayer (layer).setBlendMode (to);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public boolean merge (CPUndo u)
   {
     if (u instanceof CPUndoLayerMode && layer == ((CPUndoLayerMode) u).layer)
       {
         to = ((CPUndoLayerMode) u).to;
         return true;
       }
     return false;
   }
 
   @Override
   public boolean noChange ()
   {
     return from == to;
   }
 }
 
 static class CPUndoLayerRename extends CPUndo
 {
 
   final int layer;
   final String from;
   String to;
   private final CPArtwork artwork;
 
   public CPUndoLayerRename (CPArtwork artwork, int layer, String name)
   {
     this.artwork = artwork;
     this.from = artwork.getLayer (layer).getName ();
     this.to = name;
     this.layer = layer;
   }
 
   @Override
   public void undo ()
   {
     artwork.getLayer (layer).setName (from);
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public void redo ()
   {
     artwork.getLayer (layer).setName (to);
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public boolean merge (CPUndo u)
   {
     if (u instanceof CPUndoLayerRename && layer == ((CPUndoLayerRename) u).layer)
       {
         to = ((CPUndoLayerRename) u).to;
         return true;
       }
     return false;
   }
 
   @Override
   public boolean noChange ()
   {
     return from.equals (to);
   }
 }
 
 static class CPUndoToggleLayers extends CPUndo
 {
   final ArrayList<Boolean> mask;
   boolean toggleType; // true - we checking everything, false - unchecking
   private CPArtwork artwork;
 
   public CPUndoToggleLayers (CPArtwork artwork, ArrayList<Boolean> maskArg)
   {
     this.artwork = artwork;
     mask = maskArg;
     boolean first = false;
     for (int i = 0; i < maskArg.size (); i++)
       {
         if (!first && !mask.get (i))
           {
             toggleType = true;
             first = true;
           }
       }
   }
 
   @Override
   public void undo ()
   {
     for (int i = 0; i < artwork.getLayersVector ().size (); i++)
       artwork.getLayersVector ().elementAt (i).setVisible (mask.get (i));
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 
   @Override
   public void redo ()
   {
     for (int i = 0; i < artwork.getLayersVector ().size (); i++)
       artwork.getLayersVector ().elementAt (i).setVisible (toggleType);
     artwork.invalidateFusion ();
     artwork.callListenersLayerChange ();
   }
 }
 }
