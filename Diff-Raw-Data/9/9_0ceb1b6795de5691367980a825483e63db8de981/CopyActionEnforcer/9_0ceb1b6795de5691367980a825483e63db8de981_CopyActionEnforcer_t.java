 /*
  * Apr 9, 2006
  */
 package com.thinkparity.codebase.swing.dnd;
 
 import java.awt.Component;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetAdapter;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.util.TooManyListenersException;
 
 /**
  * A drop target adapter that monitors the
  * {@link #dragEnter(DropTargetDragEvent)} and
 * {@link #dragOver(DropTargetDragEvent)} events and checks whether or not their
  * underlying drop action is COPY. If they are not COPY actions; a new event is
  * cloned with a COPY action.
  * 
  * @author raykroeker@gmail.com
  * @version 1.1
  * @see DropTarget#addDropTargetListener(java.awt.dnd.DropTargetListener)
  * @see Component#getDropTarget()
  */
 public class CopyActionEnforcer extends DropTargetAdapter {
 
     /**
      * Apply a copy action enforcer to the component.
      * 
      * @param c
      *            An awt component.
      */
     public static void applyEnforcer(final Component c) {
         try { c.getDropTarget().addDropTargetListener(new CopyActionEnforcer()); }
         catch(final TooManyListenersException tmlx) {
             throw new RuntimeException(tmlx);
         }
     }
 
     /**
      * Create a CopyActionEnforcer.
      * 
      */
     private CopyActionEnforcer() { super(); }
 
     /**
      * @see java.awt.dnd.DropTargetAdapter#dragEnter(java.awt.dnd.DropTargetDragEvent)
      * 
      */
     public void dragEnter(final DropTargetDragEvent dtde) {
         if(!isCopyAction(dtde)) { getTarget(dtde).dragEnter(cloneAsCopy(dtde)); }
     }
 
     /**
      * @see java.awt.dnd.DropTargetAdapter#dragOver(java.awt.dnd.DropTargetDragEvent)
      * 
      */
     public void dragOver(DropTargetDragEvent dtde) {
         if(!isCopyAction(dtde)) { getTarget(dtde).dragOver(cloneAsCopy(dtde)); }
     }
 
     /**
      * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
      */
     public void drop(final DropTargetDropEvent dtde) {}
 
     /**
      * Create a new drop target drag event using a copy action.
      * 
      * @param dtde
      *            The drop target drag event to clone.
      * @return A drop target drag event.
      * @see DropTargetDragEvent#DropTargetDragEvent(java.awt.dnd.DropTargetContext,
      *      java.awt.Point, int, int)
      */
     private DropTargetDragEvent cloneAsCopy(final DropTargetDragEvent dtde) {
         return new DropTargetDragEvent(
                 dtde.getDropTargetContext(), dtde.getLocation(),
                 DnDConstants.ACTION_COPY, dtde.getSourceActions());
         
     }
 
     /**
      * Obtain the drop target from the event.
      * 
      * @param dtde
      *            The drop target drag event.
      * @return The drop target.
      */
     private DropTarget getTarget(final DropTargetDragEvent dtde) {
         return dtde.getDropTargetContext().getDropTarget();
     }
 
     /**
      * Determine whether or not the drop action is COPY.
      * 
      * @param dtde
      *            The drop target drag event.
      * @return True if the drop action is copy.
      */
     private boolean isCopyAction(final DropTargetDragEvent dtde) {
         return DnDConstants.ACTION_COPY == dtde.getDropAction();
     }
 }
