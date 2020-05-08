 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.components.reorderablelist;
 
 import com.dmdirc.addons.ui_swing.components.GenericListModel;
 
 import java.awt.Cursor;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragGestureListener;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceDragEvent;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceEvent;
 import java.awt.dnd.DragSourceListener;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JList;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
 
 /**
  * Reorderable JList.
  *
  * @param <T> Type contained in the list
  */
 public class ReorderableJList<T> extends JList<T> implements DragSourceListener,
         DropTargetListener, DragGestureListener {
 
     /**
      * A version number for this class.
      */
     private static final long serialVersionUID = 1;
     /** Drag source. */
     private final DragSource dragSource;
     /** Drag target. */
     private final DropTarget dropTarget;
     /** Drop target. */
     private Object dropTargetCell;
     /** Dragged index. */
     private int draggedIndex = -1;
     /** Data flavor. */
     private DataFlavor dataFlavor;
     /** Below drop target. */
     private boolean belowTarget;
 
     /** Instantiate new ReorderableJList. */
     public ReorderableJList() {
         this(new GenericListModel<>());
     }
 
     /**
      * Instantiate new ReorderableJList.
      *
      * @param model Model
      */
     public ReorderableJList(final GenericListModel<T> model) {
         super(model);
 
         setCellRenderer(new ReorderableJListCellRenderer<>(this));
         setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         setTransferHandler(new ArrayListTransferHandler<T>());
 
         dragSource = DragSource.getDefaultDragSource();
         dropTarget = new DropTarget(this, this);
 
         dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
         try {
 
             dataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
                     + ";class=java.util.List");
         } catch (ClassNotFoundException e) {
             //This class will always exist
         }
     }
 
     @Override
     public GenericListModel<T> getModel() {
         return (GenericListModel<T>) super.getModel();
     }
 
     @Override
     public void setModel(final ListModel<T> model) {
        if (model instanceof GenericListModel) {
             super.setModel(model);
         } else {
             throw new IllegalArgumentException("model needs to be an instance of GenericListModel");
         }
     }
 
     /**
      * Returns the target drop item.
      *
      * @return Drop target cell
      */
     public Object getTargetCell() {
         return dropTargetCell;
     }
 
     /**
      * Returns whether the target is below the drop cell.
      *
      * @return if the target is above or below the point
      */
     public boolean getBelowTarget() {
         return belowTarget;
     }
 
     @Override
     public void dragEnter(final DragSourceDragEvent dsde) {
         //Ignore
     }
 
     @Override
     public void dragOver(final DragSourceDragEvent dsde) {
         //Ignore
     }
 
     @Override
     public void dropActionChanged(final DragSourceDragEvent dsde) {
         //Ignore
     }
 
     @Override
     public void dragExit(final DragSourceEvent dse) {
         //Ignore
     }
 
     @Override
     public void dragDropEnd(final DragSourceDropEvent dsde) {
         //clear drop variables and repaint
         dropTargetCell = null;
         draggedIndex = -1;
         repaint();
     }
 
     @Override
     public void dragEnter(final DropTargetDragEvent dtde) {
         //check whether to accept drag
         if (dtde.getSource() == dropTarget) {
             dtde.acceptDrag(DnDConstants.ACTION_MOVE);
         } else {
             dtde.rejectDrag();
         }
     }
 
     @Override
     public void dragOver(final DropTargetDragEvent dtde) {
         //Reject drops on self
         if (dtde.getSource() != dropTarget) {
             dtde.rejectDrag();
         }
 
         //get location and index
         final Point dragPoint = dtde.getLocation();
         final int index = locationToIndex(dragPoint);
 
         //set drag variables and repaint
         if (index == -1) {
             dropTargetCell = null;
         } else {
             dropTargetCell = getModel().getElementAt(index);
             //check whether the drop point is after the last index
             final Rectangle bounds = getCellBounds(index, index);
             belowTarget = index == getModel().getSize() - 1
                     && dragPoint.y > bounds.y + bounds.height;
         }
 
         repaint();
     }
 
     @Override
     public void dropActionChanged(final DropTargetDragEvent dtde) {
         //Ignore
     }
 
     @Override
     public void dragExit(final DropTargetEvent dte) {
         //Ignore
     }
 
     @Override
     public void drop(final DropTargetDropEvent dtde) {
         //check source and reject
         if (dtde.getSource() != dropTarget) {
             dtde.rejectDrop();
             return;
         }
         //get object location and index
         final Point dropPoint = dtde.getLocation();
         int index = locationToIndex(dropPoint);
         if (belowTarget) {
             index++;
         }
 
         //reject invalid drops
         if (index == -1 || index == draggedIndex) {
             dtde.rejectDrop();
             return;
         }
 
         //accept drop as a move
         dtde.acceptDrop(DnDConstants.ACTION_MOVE);
 
         //get dropped item
         final Object dragged;
         try {
             dragged = dtde.getTransferable().getTransferData(dataFlavor);
         } catch (UnsupportedFlavorException | IOException e) {
             //Don't transfer if this fails
             return;
         }
 
         //move items
         final boolean sourceBeforeTarget = draggedIndex < index;
         final GenericListModel<T> mod = getModel();
         final int newIndex = sourceBeforeTarget ? index - 1 : index;
         mod.remove(draggedIndex);
         for (Object item : (ArrayList<?>) dragged) {
             @SuppressWarnings("unchecked")
             final T genericItem = (T) item;
             mod.add(newIndex, genericItem);
         }
 
         getSelectionModel().setSelectionInterval(newIndex, newIndex);
 
         //drop complete
         dtde.dropComplete(true);
     }
 
     @Override
     public void dragGestureRecognized(final DragGestureEvent dge) {
         //find the objects location and index
         final Point clickPoint = dge.getDragOrigin();
         final int index = locationToIndex(clickPoint);
 
         if (index == -1) {
             return;
         }
 
         //get the list object
         final T target = getModel().getElementAt(index);
         //create the transferable object
         final ArrayList<T> transferObject = new ArrayList<>();
         transferObject.add(target);
         final ListTransferable<T> trans = new ListTransferable<>(transferObject);
         //start drag
         draggedIndex = index;
         dragSource.startDrag(dge, Cursor.getDefaultCursor(), trans, this);
     }
 
 }
