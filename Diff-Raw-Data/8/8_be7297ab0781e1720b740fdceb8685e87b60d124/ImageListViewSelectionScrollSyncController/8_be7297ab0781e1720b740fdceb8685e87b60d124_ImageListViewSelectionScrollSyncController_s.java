 package de.sofd.viskit.controllers;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import de.sofd.viskit.ui.imagelist.JImageListView;
 
 /**
  * Controller that can synchronize the selection and the scroll position of a
  * {@link JImageListView} in various ways.
  * 
  * @author Olaf Klischat
  */
 public class ImageListViewSelectionScrollSyncController {
     protected JImageListView controlledImageListView;
     public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
     private boolean enabled;
     public static final String PROP_ENABLED = "enabled";
     
     private boolean isSelectionTracksScrollPosition = false;
     public static final String PROP_isSelectionTracksScrollPosition = "isSelectionTracksScrollPosition";
     private boolean isScrollPositionTracksSelection = false;
     public static final String PROP_isScrollPositionTracksSelection = "isScrollPositionTracksSelection";
     private boolean isAllowEmptySelection = false;
     public static final String PROP_isAllowEmptySelection = "isAllowEmptySelection";
 
     public ImageListViewSelectionScrollSyncController() {
         setScrollPositionTracksSelection(true);
         setAllowEmptySelection(true);
     }
 
     public ImageListViewSelectionScrollSyncController(JImageListView controlledImageListView) {
         this();
         setControlledImageListView(controlledImageListView);
     }
 
     /**
      * Get the value of enabled
      *
      * @return the value of enabled
      */
     public boolean isEnabled() {
         return enabled;
     }
 
     /**
      * Set the value of enabled
      *
      * @param enabled new value of enabled
      */
     public void setEnabled(boolean enabled) {
         boolean oldEnabled = this.enabled;
         this.enabled = enabled;
         propertyChangeSupport.firePropertyChange(PROP_ENABLED, oldEnabled, enabled);
     }
 
     public boolean isScrollPositionTracksSelection() {
         return isScrollPositionTracksSelection;
     }
     
     public boolean isSelectionTracksScrollPosition() {
         return isSelectionTracksScrollPosition;
     }
     
     public void setScrollPositionTracksSelection(
             boolean isScrollPositionTracksSelection) {
         boolean oldValue = this.isScrollPositionTracksSelection;
         if (isScrollPositionTracksSelection != oldValue) {
             this.isScrollPositionTracksSelection = isScrollPositionTracksSelection;
             propertyChangeSupport.firePropertyChange(PROP_isScrollPositionTracksSelection, oldValue, isScrollPositionTracksSelection);
         }
     }
     
     public void setSelectionTracksScrollPosition(
             boolean isSelectionTracksScrollPosition) {
         boolean oldValue = this.isSelectionTracksScrollPosition;
         if (isSelectionTracksScrollPosition != oldValue) {
             this.isSelectionTracksScrollPosition = isSelectionTracksScrollPosition;
             propertyChangeSupport.firePropertyChange(PROP_isSelectionTracksScrollPosition, oldValue, isSelectionTracksScrollPosition);
         }
     }
 
     public boolean isAllowEmptySelection() {
         return isAllowEmptySelection;
     }
     
     public void setAllowEmptySelection(boolean isAllowEmptySelection) {
         boolean oldValue = this.isAllowEmptySelection;
         if (isAllowEmptySelection != oldValue) {
             this.isAllowEmptySelection = isAllowEmptySelection;
             ensureNonEmptySelectionIfRequired();
             propertyChangeSupport.firePropertyChange(PROP_isAllowEmptySelection, oldValue, isAllowEmptySelection);
         }
     }
 
     protected void ensureNonEmptySelectionIfRequired() {
         if (!isAllowEmptySelection() && null != getControlledImageListView()) {
             ListSelectionModel sm = getControlledImageListView().getSelectionModel();
             if (sm != null && -1 == sm.getLeadSelectionIndex()) {
                 int fvi = getControlledImageListView().getFirstVisibleIndex();
                 if (fvi != -1) {
                     sm.setSelectionInterval(fvi, fvi);
                 }
             }
         }
     }
     /**
      * Get the value of controlledImageListView
      *
      * @return the value of controlledImageListView
      */
     public JImageListView getControlledImageListView() {
         return controlledImageListView;
     }
 
     /**
      * Set the value of controlledImageListView
      *
      * @param controlledImageListView new value of controlledImageListView
      */
     public void setControlledImageListView(JImageListView controlledImageListView) {
         JImageListView oldControlledImageListView = this.controlledImageListView;
         this.controlledImageListView = controlledImageListView;
         if (null != oldControlledImageListView) {
             oldControlledImageListView.removePropertyChangeListener(syncHandler);
             oldControlledImageListView.removeListSelectionListener(syncHandler);
         }
         if (null != controlledImageListView) {
             controlledImageListView.addPropertyChangeListener(syncHandler);
             controlledImageListView.addListSelectionListener(syncHandler);
         }
         propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
         ensureNonEmptySelectionIfRequired();
     }
 
 
     private SyncHandler syncHandler = new SyncHandler();
     
     private class SyncHandler implements PropertyChangeListener, ListSelectionListener {
         private boolean inProgrammedChange = false;
         
         @Override
         public void propertyChange(PropertyChangeEvent evt) {
             if (!isEnabled()) {
                 return;
             }
             if (inProgrammedChange) {
                 return;
             }
             if (!evt.getPropertyName().equals(JImageListView.PROP_FIRSTVISIBLEINDEX) &&
                 !evt.getPropertyName().equals(JImageListView.PROP_SCALEMODE)) {
                 return;
             }
             if (!isSelectionTracksScrollPosition()) {
                 return;
             }
             inProgrammedChange = true;
             try {
                 //System.err.println("scroll posn change: " + getControlledImageListView().getFirstVisibleIndex());
                getControlledImageListView().selectSomeVisibleCell();
             } finally {
                 inProgrammedChange = false;
             }
         }
         
         @Override
         public void valueChanged(ListSelectionEvent e) {
             if (!isEnabled()) {
                 return;
             }
             if (inProgrammedChange) {
                 return;
             }
             if (!isScrollPositionTracksSelection()) {
                 return;
             }
             inProgrammedChange = true;
             try {
                 ensureNonEmptySelectionIfRequired();
                int selIndex = getControlledImageListView().getSelectedIndex();
                if (selIndex >= 0) {
                     getControlledImageListView().scrollToSelection();
                 }
             } finally {
                 inProgrammedChange = false;
             }
         }
         
     }
     
 
     private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
 
     /**
      * Add PropertyChangeListener.
      *
      * @param listener
      */
     public void addPropertyChangeListener(PropertyChangeListener listener) {
         propertyChangeSupport.addPropertyChangeListener(listener);
     }
 
     /**
      * Remove PropertyChangeListener.
      *
      * @param listener
      */
     public void removePropertyChangeListener(PropertyChangeListener listener) {
         propertyChangeSupport.removePropertyChangeListener(listener);
     }
 
 }
