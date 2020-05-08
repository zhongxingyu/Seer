 package de.sofd.viskit.ui.imagelist;
 
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.backend.DrawingViewerBackend;
import de.sofd.util.BiIdentityHashMap;
import de.sofd.util.BiMap;
import de.sofd.util.IdentityHashSet;
import de.sofd.util.Misc;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellAddEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellPaintListener;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellRemoveEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ContainerAdapter;
 import java.awt.event.ContainerEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.swing.AbstractListModel;
 import javax.swing.JPanel;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  * Base class for GUI components displaying a list of elements, which are objects implementing {@link ImageListViewModelElement}.
  * <p>
  * The elements must be fed to the JImageListView via a {@link ListModel} that contains them.
  * <p>
  * A selection of elements is maintained via a {@link ListSelectionModel}.
  * <p>
  * The list automatically maintains a list of "cell" objects, implementing {@link ImageListViewCell},
  * that will always be associated 1:1 to the current elements (i.e. each element is associated
  * to exactly one cell, and vice versa). A cell is used to hold data that should be associated
  * with a model element in this list, but not elsewhere, i.e. if a model element is displayed
  * simultaneously in more than one JImageListView, each one has separate cell data associated with
  * the element.
  *
  * @author olaf
  */
 public abstract class JImageListView extends JPanel {
 
     private ListModel model;
     public static final String PROP_MODEL = "model";
     private ListSelectionModel selectionModel;
     public static final String PROP_SELECTIONMODEL = "selectionModel";
     private final List<ListSelectionListener> listSelectionListeners = new ArrayList<ListSelectionListener>();
     private String displayName = "";
     public static final String PROP_SCALEMODE = "scaleMode";
     private ScaleMode scaleMode;
     private final Map<ImageListViewCell, Integer> cellToIndexMap = new IdentityHashMap<ImageListViewCell, Integer>();
 
     public static interface ScaleMode extends Serializable {
         String getDisplayName();
     }
 
 
     // TODO: it's probably better to use a map that uses normal (equals()/hashCode()-based)
     //       mapping for the modelElement => cell direction
     // TODO: maybe this should really be list of cell objects that tracks the model
     //       (a ListModel may contain more than one identical or equal elements, in which case the
     //       cell => element mapping would fail). Or, explicitly forbid having the same element
     //       in the model more than once (we kind of do that for now; see the modelChangeListener below)
     private BiMap<ImageListViewModelElement, ImageListViewCell> cellsByElementMap
             = new BiIdentityHashMap<ImageListViewModelElement, ImageListViewCell>();
 
     public JImageListView() {
         ensureUiStateIsCopiedForAddedComponents();
     }
 
 
     /**
      * Get the value of model
      *
      * @return the value of model
      */
     public ListModel getModel() {
         return model;
     }
 
     /**
      * Set the value of model
      *
      * @param model new value of model
      */
     public void setModel(ListModel model) {
         ListModel oldModel = this.model;
         if (oldModel != null) {
             oldModel.removeListDataListener(modelChangeListener);
             clearCellsByElementMap();
         }
         this.model = model;
         if (this.model != null) {
             fillCellsByElementMap();
             this.model.addListDataListener(modelChangeListener);
         }
         firePropertyChange(PROP_MODEL, oldModel, model);
     }
 
     private ListDataListener modelChangeListener = new ListDataListener() {
         // we only keep the cellsByElementMap in sync with the model here;
         // we don't initiate any cell refreshs b/c we assume the subclass
         // will do that (probably automatically by passing the model through
         // to an internal, wrapped list component)
         @Override
         public void contentsChanged(ListDataEvent e) {
             boolean newElements = false;
             for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                 ImageListViewModelElement elt = (ImageListViewModelElement) getModel().getElementAt(i);
                 if (! cellsByElementMap.containsKey(elt)) {
                     ImageListViewCell cell = createCell(elt);
                     cellsByElementMap.put(elt, cell);
                     newElements = true;
                 }
             }
             if (newElements) {
                 garbageCollectStaleCells();
             }
             JImageListView.this.modelContentsChanged(e);
         }
         @Override
         public void intervalAdded(ListDataEvent e) {
             for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                 ImageListViewModelElement elt = (ImageListViewModelElement) getModel().getElementAt(i);
                 if (cellsByElementMap.containsKey(elt)) {
                     throw new IllegalStateException("JImageListView doesn't support adding the same model element (" + elt + ") more than once");
                 }
                 ImageListViewCell cell = createCell(elt);
                 cellsByElementMap.put(elt, cell);
             }
             JImageListView.this.modelIntervalAdded(e);
         }
         @Override
         public void intervalRemoved(ListDataEvent e) {
             JImageListView.this.modelIntervalRemoved(e);
             garbageCollectStaleCells();
         }
     };
 
     public int getLength() {
         if (null == getModel()) {
             return 0;
         } else {
             return getModel().getSize();
         }
     }
 
     public ImageListViewModelElement getElementAt(int index) {
         return (ImageListViewModelElement) (getModel().getElementAt(index));
     }
 
     public ImageListViewCell getCellForElement(ImageListViewModelElement elt) {
         return cellsByElementMap.get(elt);
     }
 
     public ImageListViewModelElement getElementForCell(ImageListViewCell cell) {
         return cellsByElementMap.reverseGet(cell);
     }
 
     public ImageListViewCell getCell(int index) {
         if (null != getModel()) {
             return cellsByElementMap.get((ImageListViewModelElement) (getModel().getElementAt(index)));
         } else {
             return null;
         }
     }
 
     /**
      * Remove cells that are no longer in use (i.e. no longer correspond to an existing model element)
      */
     protected void garbageCollectStaleCells() {
         Collection<ImageListViewModelElement> staleElts = new IdentityHashSet<ImageListViewModelElement>(cellsByElementMap.keySet());
         int size = getModel().getSize();
         for (int i = 0; i < size; i++) {
             ImageListViewModelElement elt = (ImageListViewModelElement) getModel().getElementAt(i);
             staleElts.remove(elt);
         }
         for (ImageListViewModelElement elt : staleElts) {
             ImageListViewCell cell = cellsByElementMap.get(elt);
             beforeCellRemoval(cell, elt);
             cellsByElementMap.remove(elt);
             cellToIndexMap.remove(cell);
         }
     }
 
     /**
      * Called if the contents of the {@link #getModel() } have changed in some way.
      * Default impl. does nothing, subclasses may override
      * @param e
      */
     protected void modelContentsChanged(ListDataEvent e) {
     }
 
     /**
      * Called if an interval was added to the {@link #getModel() }.
      * Default impl. does nothing, subclasses may override
      * @param e
      */
     protected void modelIntervalAdded(ListDataEvent e) {
     }
 
     /**
      * Called if an interval was removed from the {@link #getModel() }.
      * Default impl. does nothing, subclasses may override
      * @param e
      */
     protected void modelIntervalRemoved(ListDataEvent e) {
     }
 
     public String getDisplayName() {
         return displayName;
     }
 
     public void setDisplayName(String displayName) {
         if (null == displayName) {
             this.displayName = "";
         } else {
             this.displayName = displayName.trim();
         }
     }
 
     /**
      * Get the value of scaleMode
      *
      * @return the value of scaleMode
      */
     public ScaleMode getScaleMode() {
         return scaleMode;
     }
 
     /**
      * Set the scaleMode ({@link #getScaleMode()}) of this viewer to one of the {@link #getSupportedScaleModes() }
      * (calling this with an unsupported scale mode will raise an exception). Sets the bean property,
      * fires the corresponding PropertyChangeEvent, and (before firing, but after setting the property) calls
      * {@link #doSetScaleMode(de.sofd.viskit.ui.imagelist.JImageListView.ScaleMode, de.sofd.viskit.ui.imagelist.JImageListView.ScaleMode) },
      * which subclasses should override normally (rather than overriding this method).
      *
      * @param scaleMode new value of scaleMode
      */
     public void setScaleMode(ScaleMode scaleMode) {
         if (! getSupportedScaleModes().contains(scaleMode)) {
             throw new IllegalArgumentException("Unsupported scale mode: " + scaleMode);
         }
         if (Misc.equal(scaleMode, getScaleMode())) {
             return;
         }
         ScaleMode oldScaleMode = this.scaleMode;
         this.scaleMode = scaleMode;
         doSetScaleMode(oldScaleMode, this.scaleMode);
         firePropertyChange(PROP_SCALEMODE, oldScaleMode, scaleMode);
     }
 
     /**
      * Actual setter for the {@link #getScaleMode() }. oldScaleMode is the previous scaleMode,
      * newScale mode is the new one (which will already be in {@link #getScaleMode() } by the time
      * this method is called, so this parameter is just a convenience). Default impl. ist empty,
      * which is pretty useless unless the subclass doesn't support any ScaleModes or doesn't want
      * to do anything when the scaleMode is set (which would be kind of pointless). Thus, subclasses
      * should normally override this method. Alternatively, they could override
      * {@link #setScaleMode(de.sofd.viskit.ui.imagelist.JImageListView.ScaleMode) } and
      * {@link #getScaleMode() } in concert to implement some completely different way of setting the
      * scale mode; however, in that case, the general contract for the bound bean property getter/setter
      * (which is adhered to by the default implementations of those methods) must be re-implemented.
      *
      * @param oldScaleMode oldScaleMode
      * @param newScaleMode newScaleMode
      */
     protected void doSetScaleMode(ScaleMode oldScaleMode, ScaleMode newScaleMode) {
         //
     }
 
     /**
      *
      * @return list off ScaleModes supported by this JImageListView implementation. Subclasses must implement.
      */
     public abstract Collection<ScaleMode> getSupportedScaleModes();
 
     /**
      * Creates a new ImageListViewCell (using doCreateCell()), ensures the cell's
      * PropertyChangeEvents are exposed to {@link #addCellPropertyChangeListener(java.beans.PropertyChangeListener) },
      * and calls {@link #ensureCellRepaintsOnRoiDrawingViewerChanges(de.sofd.viskit.ui.imagelist.ImageListViewCell) }.
      * Not normally called by subclasses (and never called by users). Hardly ever overridden;
      * override {@link #doCreateCell(de.sofd.viskit.ui.imagelist.ImageListViewModelElement) } instead.
      *
      * @param modelElement
      * @return
      */
     protected ImageListViewCell createCell(ImageListViewModelElement modelElement) {
         ImageListViewCell cell = doCreateCell(modelElement);
         cell.addPropertyChangeListener(cellPropertyChangeEventForwarder);
         fireImageListViewEvent(new ImageListViewCellAddEvent(this, cell));
         ensureCellRepaintsOnRoiDrawingViewerChanges(cell);
         return cell;
     }
 
     /**
      * Called by {@link #createCell(de.sofd.viskit.ui.imagelist.ImageListViewModelElement) }
      * for newly created cells. Ensures that any changes to the cell's ROI drawing viewer
      * cause the cell to be repainted. Default impl. registers a {@link DrawingViewerBackend}
      * on the ROI drawing viewer that calls {@link #refreshCell(de.sofd.viskit.ui.imagelist.ImageListViewCell) }
      * whenever a repaint is requested.
      *
      * @param cell
      */
     protected void ensureCellRepaintsOnRoiDrawingViewerChanges(final ImageListViewCell cell) {
         cell.getRoiDrawingViewer().setBackend(new DrawingViewerBackend() {
             @Override
             public void connected(DrawingViewer viewer) {
             }
             @Override
             public void repaint() {
                 refreshCell(cell);
             }
             @Override
             public void repaint(double x, double y, double width, double height) {
                 refreshCell(cell);
             }
             @Override
             public void disconnecting() {
             }
         });
     }
 
     /**
      * Factory method for creating the {@link ImageListViewCell} instances
      * to associate with model elements. Called whenever a new cell must
      * be created to be associated with a model element (either during initialization,
      * or whenever a new element is added to this list later).
      * <p>
      * Default implementation instantiates
      * a {@link DefaultImageListViewCell}. Subclasses may override. Don't call
      * this method; call createCell() instead (if you really think you must).
      *
      * @param modelElement the model element to create the cell for
      * @return the newly created cell
      */
     protected ImageListViewCell doCreateCell(ImageListViewModelElement modelElement) {
         return new DefaultImageListViewCell(this, modelElement);
     }
 
     /**
      * Callback method called immediately before a cell is disassociated from its model element.
      * Override to implement special handling in this situation. Call super() in the overridden
      * version. Never call this method yourself in subclasses.
      *
      * @param cell
      * @param modelElement
      */
     protected void beforeCellRemoval(ImageListViewCell cell, ImageListViewModelElement modelElement) {
         cell.removePropertyChangeListener(cellPropertyChangeEventForwarder);
     }
 
     private PropertyChangeListener cellPropertyChangeEventForwarder = new PropertyChangeListener() {
         @Override
         public void propertyChange(PropertyChangeEvent evt) {
             JImageListView.this.fireCellPropertyChangeEvent(evt);
         }
     };
 
     public ImageListViewCell getCellFor(ImageListViewModelElement modelElement) {
         return cellsByElementMap.get(modelElement);
     }
 
     private void clearCellsByElementMap() {
         for (ImageListViewModelElement elt: cellsByElementMap.keySet()) {
             ImageListViewCell cell = cellsByElementMap.get(elt);
             fireImageListViewEvent(new ImageListViewCellRemoveEvent(this, cell));
             beforeCellRemoval(cell, elt);
         }
         cellsByElementMap.clear();
     }
 
     private void fillCellsByElementMap() {
         for (int i = 0; i < getModel().getSize(); i++) {
             ImageListViewModelElement elt = (ImageListViewModelElement) getModel().getElementAt(i);
             ImageListViewCell cell = createCell(elt);
             cellsByElementMap.put(elt, cell);
         }
     }
 
     /**
      * Get the value of selectionModel
      *
      * @return the value of selectionModel
      */
     public ListSelectionModel getSelectionModel() {
         return selectionModel;
     }
 
     /**
      * Set the value of selectionModel
      *
      * @param selectionModel new value of selectionModel
      */
     public void setSelectionModel(ListSelectionModel selectionModel) {
         ListSelectionModel oldSelectionModel = this.selectionModel;
         if (oldSelectionModel != null) {
             oldSelectionModel.removeListSelectionListener(listSelectionListener);
         }
         this.selectionModel = selectionModel;
         if (this.selectionModel != null) {
             this.selectionModel.addListSelectionListener(listSelectionListener);
         }
         firePropertyChange(PROP_SELECTIONMODEL, oldSelectionModel, selectionModel);
     }
 
     public int getSelectedIndex() {
         return getMinSelectionIndex();
     }
 
     public ImageListViewModelElement getSelectedValue() {
         int i = getMinSelectionIndex();
         return (i == -1) ? null : (ImageListViewModelElement) getModel().getElementAt(i);
     }
 
     public int getMinSelectionIndex() {
         return getSelectionModel().getMinSelectionIndex();
     }
 
     public int getMaxSelectionIndex() {
         return getSelectionModel().getMaxSelectionIndex();
     }
 
     public int getLeadSelectionIndex() {
         return getSelectionModel().getLeadSelectionIndex();
     }
 
     /**
      * Adds a listener that gets notified if this list's selection changes.
      * The source of the selection change events will be this list (rather
      * than the selection model, as would be the case for listeners registered
      * via getSelectionModel().addListSelectionListener())
      * 
      * @param listener
      */
     public void addListSelectionListener(ListSelectionListener listener) {
         listSelectionListeners.add(listener);
     }
 
     public void removeListSelectionListener(ListSelectionListener listener) {
         listSelectionListeners.remove(listener);
     }
 
     protected void fireListSelectionEvent(int firstIndex, int lastIndex, boolean isAdjusting) {
         for (ListSelectionListener l : listSelectionListeners) {
             l.valueChanged(new ListSelectionEvent(this, firstIndex, lastIndex, isAdjusting));
         }
     }
 
     private ListSelectionListener listSelectionListener = new ListSelectionListener() {
         @Override
         public void valueChanged(ListSelectionEvent e) {
             JImageListView.this.selectionChanged(e);
             fireListSelectionEvent(e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());
         }
     };
 
     /**
      * Called if the selection of this list has changed.
      * Default implementation does nothing, subclasses may override.
      * @param e
      */
     protected void selectionChanged(ListSelectionEvent e) {
     }
 
     /**
      * Called if a the cells of this viewer need to be refreshed. Normally
      * called internally by subclasses if they determine the need to refresh the cells,
      * but may also be called explicitly from the outside.
      * <p>
      * Uses the same special casing hack as {@link #refreshCellForIndex(int) }.
      */
     public void refreshCells() {
         if (getModel() instanceof AbstractListModel) {
             AbstractListModel alm = (AbstractListModel) getModel();
             Misc.callMethod(alm, "fireContentsChanged", alm, 0, alm.getSize() - 1);
         } else {
             System.err.println("JImageListView#refreshCells(): don't know how to refresh. Override me!");
         }
     }
 
     /**
      * Called if a specific cell of this viewer needs to be refreshed. Normally
      * called internally by subclasses if they determine the need to refresh a cell,
      * but may also be called explicitly from the outside.
      * <p>
      * Default implementation implements a special casing hack if the #getModel()
      * is an instance of {@link AbstractListModel}: In that case, it fires a
      * corresponding contentsChanged event through the model, which should cause
      * compliant viewers to refresh themselves accordingly. This might be terribly
      * inefficient (all viewers of the model will refresh even if only cell data
      * in this viewer has changed, and there may be more efficient ways to refresh
      * the cell). Subclasses must override (not calling the super implementation)
      * if they need/want a better strategy.
      *
      * @param idx index of the cell (corresponds to index in the getModel())
      */
     public void refreshCellForIndex(int idx) {
         if (getModel() instanceof AbstractListModel) {
             AbstractListModel alm = (AbstractListModel) getModel();
             Misc.callMethod(alm, "fireContentsChanged", alm, idx, idx);
         } else {
             System.err.println("JImageListView#refreshCellForIndex(): don't know how to refresh. Override me!");
         }
     }
 
     /**
      * Called if a specific cell of this viewer needs to be refreshed. Normally
      * called internally by subclasses if they determine the need to refresh a
      * cell, but may also be called explicitly from the outside.
      * <p>
      * Default implementation finds the cell displaying elt, then calls
      * {@link #refreshCell(ImageListViewCell))}.
      * 
      * @param elt
      *            model element corresponding to the cell
      */
     public void refreshCellForElement(ImageListViewModelElement elt) {
         if (null == getModel()) { return; }
         ImageListViewCell cell = cellsByElementMap.get(elt);
         if (null != cell) {
             refreshCell(cell);
         }
     }
 
     /**
      * Called if a specific cell of this viewer needs to be refreshed. Normally
      * called internally by subclasses if they determine the need to refresh a
      * cell, but may also be called explicitly from the outside.
      * <p>
      * Default implementation finds cell in the list of cells of this list,
      * using an internal table to look up the index in O(1) rather than O(n) in
      * most cases, then calls {@link #refreshCellForIndex(int) }.
      * 
      * @param cell
      *            the cell
      */
     public void refreshCell(ImageListViewCell cell) {
         // use cellToIndexMap to look up cell's index quickly, creating/updating
         // cellToIndexMap lazily along the way if necessary
         // in most cases, the cellToIndexMap should contain the correct index for cell.
         // we optimize for that case (O(1)).
         // cellToIndexMap will only become temporarily outdated if somebody
         // adds/removes cells from the list.
         if (null == getModel()) { return; }
         Integer index = cellToIndexMap.get(cell);
         ImageListViewCell foundCell = null;
        if (index != null) {
             foundCell = getCell(index);
         }
         if (index != null && foundCell == cell) {
             refreshCellForIndex(index);
         } else {
             int eltCount = getModel().getSize();
             for (int i = 0; i < eltCount; i++) {
                 ImageListViewCell c2 = getCell(i);
                 cellToIndexMap.put(c2, i);
                 if (c2 == cell) {
                     refreshCellForIndex(i);
                     return;
                 }
             }
         }
     }
 
     @Override
     public void setForeground(Color fg) {
         super.setForeground(fg);
         copyUiStateToSubComponents();
     }
 
 
     @Override
     public void setBackground(Color bg) {
         super.setBackground(bg);
         copyUiStateToSubComponents();
     }
 
 
     protected void copyUiStateToSubComponents() {
         for (Component c : this.getComponents()) {
             copyUiStateToSubComponent(c);
         }
     }
 
     /**
      * The base class (JImageListView) calls this whenever a UI state like
      * foreground/background colors should to be copied from this component
      * to a child component (c). This happens when (a) such a UI state
      * was changed or (b) when a new child component was added. JImageListView
      * ensures that this is done correctly ((b) is ensured by the constructor
      * calling {@link #ensureUiStateIsCopiedForAddedComponents() }). The default
      * implementation copies the foreground and background color. Subclasses
      * may override to copy additional properties.
      *
      * @param c
      */
     protected void copyUiStateToSubComponent(Component c) {
         c.setForeground(this.getForeground());
         c.setBackground(this.getBackground());
     }
 
     /**
      * Ensures {@link #copyUiStateToSubComponent(java.awt.Component) } will
      * be called for any child component added to this component in the
      * future. Called once by the default constructor of JImageListView.
      * Subclasses may (rarely) override, e.g. with an empty implementation
      * if they want to inhibit this behaviour for some (strange) reason.
      */
     protected void ensureUiStateIsCopiedForAddedComponents() {
         this.addContainerListener(new ContainerAdapter() {
             @Override
             public void componentAdded(ContainerEvent e) {
                 JImageListView.this.copyUiStateToSubComponent(e.getChild());
             }
         });
     }
 
     /*
     private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
 
     protected PropertyChangeSupport getPropertyChangeSupport() {
         //Need to get the propertyChangeSupport via this lazy intialization
         //getter because the in-place initialization
         if (null == propertyChangeSupport) {
             propertyChangeSupport = new PropertyChangeSupport(this);
         }
         return propertyChangeSupport;
     }
     */
 
     private Collection<ImageListViewListener> imageListViewListeners =
             new ArrayList<ImageListViewListener>();
 
     public void addImageListViewListener(ImageListViewListener listener) {
         imageListViewListeners.add(listener);
     }
 
     public void removeImageListViewListener(ImageListViewListener listener) {
         imageListViewListeners.remove(listener);
     }
 
     protected void fireImageListViewEvent(ImageListViewEvent e) {
         for (ImageListViewListener l : imageListViewListeners) {
             l.onImageListViewEvent(e);
         }
     }
 
 
     private Collection<PropertyChangeListener> cellPropertyChangeListeners =
             new ArrayList<PropertyChangeListener>();
 
     /**
      * Add a PropertyChangeListener that receives property change events for all
      * properties of all cells of this list. This is a convenient way to be
      * informed of all such property changes without having to manually add
      * listeners to all cells (and track elements and corresponding cells being
      * added/removed to the list, and correctly add/remove the listeners to them).
      * This method takes care of all that internally.
      *
      * @param listener
      */
     public void addCellPropertyChangeListener(PropertyChangeListener listener) {
         cellPropertyChangeListeners.add(listener);
     }
 
     public void removeCellPropertyChangeListener(PropertyChangeListener listener) {
         cellPropertyChangeListeners.remove(listener);
     }
 
     protected void fireCellPropertyChangeEvent(PropertyChangeEvent e) {
         for (PropertyChangeListener l : cellPropertyChangeListeners) {
             l.propertyChange(e);
         }
     }
 
 
 
     private Collection<MouseListener> cellMouseListeners =
             new ArrayList<MouseListener>();
 
     /**
      * Add a MouseListener that receives mouse events for all cells of this list.
      * This is a convenient way to be
      * informed of all such mouse events without having to manually add
      * listeners to all cells (and track elements and corresponding cells being
      * added/removed to the list, and correctly add/remove the listeners to them).
      * This method takes care of all that internally.
      * <p>
      * The event's source will be the cell, the mouse coordinates will be relative
      * to the cell's upper-left border.
      *
      * @param listener
      */
     public void addCellMouseListener(MouseListener listener) {
         cellMouseListeners.add(listener);
     }
 
     public void removeCellMouseListener(MouseListener listener) {
         cellMouseListeners.remove(listener);
     }
 
     protected void fireCellMouseEvent(MouseEvent e) {
         if (e.getID() == MouseEvent.MOUSE_MOVED || e.getID() == MouseEvent.MOUSE_DRAGGED) {
             fireCellMouseMotionEvent(e);
             return;
         }
         for (MouseListener l : cellMouseListeners) {
             switch (e.getID()) {
                 case MouseEvent.MOUSE_CLICKED:
                     l.mouseClicked(e);
                     break;
                 case MouseEvent.MOUSE_PRESSED:
                     l.mousePressed(e);
                     break;
                 case MouseEvent.MOUSE_RELEASED:
                     l.mouseReleased(e);
                     break;
                 case MouseEvent.MOUSE_ENTERED:
                     l.mouseEntered(e);
                     break;
                 case MouseEvent.MOUSE_EXITED:
                     l.mouseExited(e);
                     break;
             }
             if (e.isConsumed()) {
                 break;
             }
         }
     }
 
 
     private Collection<MouseMotionListener> cellMouseMotionListeners =
             new ArrayList<MouseMotionListener>();
 
     /**
      * Like {@link #addCellMouseListener(java.awt.event.MouseListener) }, but for
      * MouseMotionListeners.
      *
      * @param listener
      */
     public void addCellMouseMotionListener(MouseMotionListener listener) {
         cellMouseMotionListeners.add(listener);
     }
 
     public void removeCellMouseMotionListener(MouseMotionListener listener) {
         cellMouseMotionListeners.remove(listener);
     }
 
     protected void fireCellMouseMotionEvent(MouseEvent e) {
         for (MouseMotionListener l : cellMouseMotionListeners) {
             switch (e.getID()) {
                 case MouseEvent.MOUSE_MOVED:
                     l.mouseMoved(e);
                     break;
                 case MouseEvent.MOUSE_DRAGGED:
                     l.mouseDragged(e);
                     break;
             }
             if (e.isConsumed()) {
                 break;
             }
         }
     }
 
 
     private Collection<MouseWheelListener> cellMouseWheelListeners =
             new ArrayList<MouseWheelListener>();
 
     /**
      * Like {@link #addCellMouseListener(java.awt.event.MouseListener) }, but for
      * MouseWheelListener.
      *
      * @param listener
      */
     public void addCellMouseWheelListener(MouseWheelListener listener) {
         cellMouseWheelListeners.add(listener);
     }
 
     public void removeCellMouseWheelListener(MouseWheelListener listener) {
         cellMouseWheelListeners.remove(listener);
     }
 
     protected void fireCellMouseWheelEvent(MouseWheelEvent e) {
         for (MouseWheelListener l : cellMouseWheelListeners) {
             l.mouseWheelMoved(e);
             if (e.isConsumed()) {
                 break;
             }
         }
     }
 
 
     /**
      * Add a new listener for CellPaintEvents, with default z-order
      * (PAINT_ZORDER_DEFAULT).
      * 
      * @param listener
      */
     public void addCellPaintListener(ImageListViewCellPaintListener listener) {
         addCellPaintListener(PAINT_ZORDER_DEFAULT, listener);
     }
 
     /**
      * Add a new listener to for CellPaintEvents. The listener will be invoked
      * whenever a cell in this list needs to be (re)painted. The zOrder
      * parameter determines the order in which the listeners are invoked (if
      * more than one listener was added). Listeners with higher zOrder numbers
      * are called after ones with lower zOrder numbers (when the numbers are
      * equal, insertion order decides). A listener that is called later will end
      * up drawing on top of what the listeners called earlier have drawn.
      * <p>
      * You can pass any of the PAINT_ZORDER_* constants as the zOrder, or any
      * other number.
      * 
      * @param zOrder
      * @param listener
      */
     public void addCellPaintListener(int zOrder, ImageListViewCellPaintListener listener) {
         cellPaintListeners.add(new PaintListenerRecord(listener, zOrder));
     }
     
     public void removeCellPaintListener(ImageListViewCellPaintListener listener) {
         for (Iterator<PaintListenerRecord> it = cellPaintListeners.iterator(); it.hasNext();) {
             if (it.next().listener == listener) {
                 it.remove();
                 return;
             }
         }
     }
     
     /**
      * Paint z-order at which the image is normally drawn. Numerical value is 10, which
      * is the lowest of all the PAINT_ZORDER_* constants, so the image will normally be drawn
      * at the bottom of anything else (i.e., everything else will be drawn on top).
      * 
      * (TODO: not implemented)
      */
     public static final int PAINT_ZORDER_IMAGE = 10;
 
     /**
      * Paint z-order at which the ROIs are normally drawn. Numerical value is
      * 50, which means the ROIs are drawn on top of the image, but below any
      * labels (see PAINT_ZORDER_LABELS)
      * 
      * (TODO: not implemented)
      */
     public static final int PAINT_ZORDER_ROI = 50;
 
     /**
      * Paint z-order at which any labels are normally drawn. Numerical value is
      * 200, which is the highest of all the PAINT_ZORDER_* constants, so this
      * will be drawn on top of everything else unless you specify a z-order
      * that's not one of the PAINT_ZORDER_* constants, and is higher than
      * PAINT_ZORDER_LABELS.
      * 
      * (TODO: not implemented)
      */
     public static final int PAINT_ZORDER_LABELS = 200;
 
     /**
      * Default paint z-order. This is used if you call
      * {@link #addCellPaintListener(ImageListViewCellPaintListener)} (i.e.
      * without explicitly specifying a z-order). Numerical value is 100,
      * meaning that this would normally be drawn between the ROIs and any
      * labels.
      */
     public static final int PAINT_ZORDER_DEFAULT = 100;
     
     
     /**
      * NOT A PUBLIC API! DON'T CALL.
      * 
      * (method is defined public so internal classes in subpackages can call it)
      * @param e
      */
     public void fireCellPaintEvent(ImageListViewCellPaintEvent e) {
         for (PaintListenerRecord rec : cellPaintListeners) {
             rec.listener.onCellPaint(e);
             if (e.isConsumed()) {
                 break;
             }
         }
     }
 
     /**
      * NOT A PUBLIC API! DON'T CALL.
      * 
      * (method is defined public so internal classes in subpackages can call it)
      * @param e
      */
     public void fireCellPaintEvent(ImageListViewCellPaintEvent e, int minZ, int maxZ) {
         ImageListViewCellPaintListener dummy = new ImageListViewCellPaintListener() {
             @Override
             public void onCellPaint(ImageListViewCellPaintEvent e) {
             }
         };
         PaintListenerRecord min = new PaintListenerRecord(dummy, minZ);
         PaintListenerRecord max = new PaintListenerRecord(dummy, maxZ);
         for (PaintListenerRecord rec : cellPaintListeners.subSet(min, max)) {
             rec.listener.onCellPaint(e);
             if (e.isConsumed()) {
                 break;
             }
         }
     }
 
     private SortedSet<PaintListenerRecord> cellPaintListeners = new TreeSet<PaintListenerRecord>();
     
     private static class PaintListenerRecord implements Comparable<PaintListenerRecord> {
         ImageListViewCellPaintListener listener;
         Integer zOrder;
         Integer instanceNumber;
         private static int lastInstanceNumber;
         public PaintListenerRecord(ImageListViewCellPaintListener listener, int zOrder) {
             this.listener = listener;
             this.zOrder = zOrder;
             this.instanceNumber = lastInstanceNumber++;
         }
         @Override
         public int compareTo(PaintListenerRecord o) {
             int res = zOrder.compareTo(o.zOrder);
             if (res == 0) {
                 return instanceNumber.compareTo(o.instanceNumber);
             } else {
                 return res;
             }
         }
     }
     
 }
