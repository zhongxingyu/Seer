 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.slideabletree;
 
 import org.jdesktop.swingx.JXTaskPane;
 import org.jdesktop.swingx.JXTaskPaneContainer;
 import org.jdesktop.swingx.VerticalLayout;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Rectangle;
 import java.awt.dnd.DragSource;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import javax.swing.Icon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeExpansionListener;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.event.TreeWillExpandListener;
 import javax.swing.plaf.TreeUI;
 import javax.swing.text.Position.Bias;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.ExpandVetoException;
 import javax.swing.tree.TreeCellEditor;
 import javax.swing.tree.TreeCellRenderer;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * DOCUMENT ME!
  *
  * @author   dmeiers
  * @version  $Revision$, $Date$
  */
 public class SlideableTree extends JTree implements TreeExpansionListener,
     TreeSelectionListener,
     TreeWillExpandListener {
 
     //~ Instance fields --------------------------------------------------------
 
     private JXTaskPaneContainer container;
     private ArrayList<SlideableSubTree> trees;
     private ArrayList<SubTreePane> panes;
     private JScrollPane containerScrollPane;
     private DragSource dragSource;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new SlideableTree object.
      */
     public SlideableTree() {
         trees = new ArrayList<SlideableSubTree>();
         panes = new ArrayList<SubTreePane>();
         container = new JXTaskPaneContainer();
         dragSource = DragSource.getDefaultDragSource();
         this.setLayout(new BorderLayout());
 
         /*
          * Erzeuge fuer alle obersten Knoten einen eigenen SubTree dieser wird einem JXTaskpane zugeordnet
          */
         createSubTrees(this.getModel());
         addToTreeContainer(panes);
 
         final VerticalLayout verticalLayout = new VerticalLayout();
         verticalLayout.setGap(7);
         container.setLayout(verticalLayout);
         container.setBorder(new EmptyBorder(0, 0, 0, 0));
 
         // die Panes mit den SubTrees zu dem Container hinzufuegen
         addToTreeContainer(panes);
 
         containerScrollPane = new JScrollPane(container);
         // fuer niftyScrollBar
         StaticSwingTools.setNiftyScrollBars(containerScrollPane);
         this.add(containerScrollPane, BorderLayout.CENTER);
 //        this.add(container, BorderLayout.CENTER);
     }
 
     /**
      * Creates a new SlideableTree object.
      *
      * @param  model  DOCUMENT ME!
      */
     public SlideableTree(final TreeModel model) {
         this();
         this.setModel(model);
     }
 
     /**
      * Creates a new SlideableTree object.
      *
      * @param  node  DOCUMENT ME!
      */
     public SlideableTree(final TreeNode node) {
         this(new DefaultTreeModel(node));
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void addSelectionPath(final TreePath path) {
         if (trees != null) {
             final SlideableSubTree t = getSubTreeForPath(path);
             final TreePath subTreePath = getPathForSubTree(path);
             t.addSelectionPath(path);
         }
         // super.addSelectionPath(path);
     }
 
     @Override
     public void addSelectionPaths(final TreePath[] paths) {
         if (trees != null) {
             for (int i = 0; i < paths.length; i++) {
                 addSelectionPath(paths[i]);
             }
         }
         super.addSelectionPaths(paths);
     }
 
     @Override
     public void addSelectionRow(final int row) {
         final TreePath path = getPathForRow(row);
         addSelectionPath(path);
         super.addSelectionRow(row);
     }
 
     @Override
     public void addSelectionRows(final int[] rows) {
         for (int i = 0; i < rows.length; i++) {
             addSelectionRow(rows[i]);
         }
         super.addSelectionRows(rows);
     }
 
     /*
      * das Intervall wird auf die einzelnen SubTrees umgelenkt. Dabei kann durch index0 und die Anzahl der jeweils
      * sichtbaren Elemente im Subtree, der Subtree herausgefunden werden, in dem die Selektion beginnt. (index0>=anzahl
      * sichtbarer Elemente im Subtree) Das Ende der Selektion kann auf gleiche Weise hereausgefunden werden
      * Dazwischenliegenede Subtrees sind komplett selektiert.
      */
     @Override
     public void addSelectionInterval(final int index0, final int index1) {
         if (index1 < index0) {
             return;
         } else {
             for (int i = index0; i <= index1; i++) {
                 final TreePath path = getPathForRow(i);
                 addSelectionPath(path);
             }
         }
         super.addSelectionInterval(index0, index1);
     }
 
     /*
      * fuer alle subtrees, da pro subtree editiert werden kann
      */
     @Override
     public void cancelEditing() {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.cancelEditing();
             }
         }
         // super.cancelEditing();
     }
 
     /*
      * fuer alle subtrees
      */
     @Override
     public void clearSelection() {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.clearSelection();
             }
         }
         // super.clearSelection();
     }
 
     @Override
     protected void clearToggledPaths() {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.clearToggledPaths();
             }
         }
         super.clearToggledPaths();
     }
 
     @Override
     public void expandRow(final int row) {
         if ((row < 0) || (row > getRowCount())) {
             return;
         }
         if ((trees != null) && (panes != null)) {
             final SlideableSubTree t = getSubTreeForRow(row);
             final int index = trees.indexOf(t);
             final JXTaskPane pane = panes.get(index);
             pane.setCollapsed(false);
             final TreePath subTreePath = getPathForSubTree(getPathForRow(row));
             t.expandPath(subTreePath);
         }
     }
 
     /*
      * subtree fuer knoten ausfindig machen, neuen path erstellen, methode weiterleiten, Paths in der enumeration
      * anpassen
      */
     @Override
     public Enumeration<TreePath> getExpandedDescendants(final TreePath parent) {
         final Vector<TreePath> paths = new Vector<TreePath>();
         final Object lastPathElement = parent.getLastPathComponent();
         final Object origRoot = this.getModel().getRoot();
 
         if (trees != null) {
             // falls der durch parent representierte knoten der Rootnode des
             // original baum ist, alle SubTreeRoots falls das JXTaskPane aufgeklappt zurueckgeben
             if (lastPathElement.equals(origRoot)) {
                 for (final SlideableSubTree t : trees) {
                     final JXTaskPane pane = panes.get(trees.indexOf(t));
                     if (!(pane.isCollapsed())) {
                         final Object subTreeRoot = t.getModel().getRoot();
                         paths.add(getPathforOriginalTree(new TreePath(subTreeRoot)));
                     }
                 }
             } // sonst lediglich fuer den Subtree der den durch parent representierten
             // knoten enthaelt
             else {
                 final SlideableSubTree subTree = getSubTreeForPath(parent);
                 final Enumeration<TreePath> newPaths = subTree.getExpandedDescendants(getPathForSubTree(parent));
                 if (newPaths != null) {
                     while (newPaths.hasMoreElements()) {
                         paths.add(getPathforOriginalTree(newPaths.nextElement()));
                     }
                 }
             }
         }
         return paths.elements();
     }
 
     /*
      * Durch alle subtress durch, angepasste pfad zu dem knoten der editiert wird
      */
     @Override
     public TreePath getEditingPath() {
         for (final JTree t : trees) {
             final TreePath path = t.getEditingPath();
             if (path != null) {
                 return getPathforOriginalTree(path);
             }
         }
         return super.getEditingPath();
     }
 
     /*
      * laut javadoc: returns null wenn koordinaten nicht innerhalb des Closestpath
      */
     @Override
     public TreePath getPathForLocation(final int x, final int y) {
         final TreePath closestPath = getClosestPathForLocation(x, y);
         // closest Path ist null wenn koordinaten nicht innerhalb des SlideableTree liegen..
         if (closestPath == null) {
             return null;
         } else {
             final JXTaskPane pane = panes.get(trees.indexOf(getSubTreeForPath(closestPath)));
             final int paneX = pane.getX();
             final int paneY = pane.getY();
             final int titleBarHeight = (pane.getHeight() - pane.getContentPane().getHeight());
             // Sonderfall closest ist Rootnode eines subTrees
             if (closestPath.getPathCount() == 2) {
                 if ((y >= paneY) && (y <= (paneY + titleBarHeight))) {
                     return closestPath;
                 }
             }
 
             final int treeX = getSubTreeForPath(closestPath).getX();
             final int treeY = getSubTreeForPath(closestPath).getY();
             final int newX = x - paneX - treeX;
             final int newY = y - paneY - treeY - titleBarHeight;
 
             final Rectangle r = getPathBounds(closestPath);
             double recX = 0;
             double recY = 0;
             double recWidth = 0;
             double recHeight = 0;
             if (r != null) {
                 recX = r.getX();
                 recY = r.getY();
                 recWidth = r.getWidth();
                 recHeight = r.getHeight();
             }
             // liegen Koordinaten innerhalb des closestPath?
             if ((newX >= recX) && (newX <= (recX + recWidth)) && (newY >= recY) && (newY <= (recY + recHeight))) {
                 return closestPath;
             }
             return null;
         }
     }
 
     /*
      * was ist wenn X/Y nicht in einem JXTaskpane liegen?
      */
     @Override
     public TreePath getClosestPathForLocation(final int x, final int y) {
         System.out.println("VisibleRect: " + this.getVisibleRect());
         System.out.println("Tree X/Y von: " + this.getX() + "/" + this.getY());
         System.out.println("Tree X/Y bis: " + this.getX() + this.getWidth() + "/" + this.getY() + this.getHeight());
         final Component c = container.getComponentAt(x, y);
         if (c instanceof JXTaskPane) {
             final JXTaskPane pane = (JXTaskPane)c;
             final SlideableSubTree t = trees.get(panes.indexOf(pane));
             final int titleBarHeight = (pane.getHeight() - pane.getContentPane().getHeight());
             if (y <= (titleBarHeight + pane.getY())) {
                 // der rootNode des SubTrees ist der closestNode
                 return getPathforOriginalTree(new TreePath(t.getModel().getRoot()));
             } else if ((y > (titleBarHeight + pane.getY())) && (y < (pane.getY() + titleBarHeight + t.getY()))) {
                 final int distanceToTitle = Math.abs(y - (titleBarHeight + pane.getY()));
                 final int distanceToTree = Math.abs(y - (pane.getY() + titleBarHeight + t.getY()));
                 if (distanceToTitle < distanceToTree) {
                     return getPathforOriginalTree(new TreePath(t.getModel().getRoot()));
                 }
             }
             final int newY = y - pane.getY() - t.getY() - titleBarHeight;
             final int newX = x - pane.getX() - t.getX();
             TreePath subTreePath = t.getClosestPathForLocation(newX, newY);
             // ist null falls kein Knoten sichtbar ist, z.b wenn der rootNode des
             // subTree ein blatt ist
             if (subTreePath == null) {
                 subTreePath = new TreePath(t.getModel().getRoot());
             }
             return getPathforOriginalTree(subTreePath);
 
             // y liegt zwischen titlebar und tree, geringsten abstand bestimmen
         } else if (c instanceof JXTaskPaneContainer) {
             // falls berechne den nahestehendsten JXTaskpane..
             JXTaskPane closest = null;
             boolean lastComponent = false;
             for (final JXTaskPane p : panes) {
                 final int paneY = p.getY();
                 // liegt y vor p?
                 if (y < paneY) {
                     if (panes.indexOf(p) == 0) {
                         closest = p;
                         lastComponent = false;
                     } else {
                         final int distance = Math.abs(y - paneY);
                         final JXTaskPane predecessor = panes.get(panes.indexOf(p) - 1);
                         final int distanceToPredecessor = Math.abs(y - (predecessor.getY() + predecessor.getHeight()));
                         if (distance <= distanceToPredecessor) {
                             closest = p;
                             lastComponent = false;
                         } else {
                             closest = predecessor;
                             lastComponent = true;
                         }
                     }
                 }
             }
             if (closest == null) {
                 closest = panes.get(panes.size() - 1);
                 lastComponent = true;
             }
             final SlideableSubTree t = trees.get(panes.indexOf(closest));
             if (lastComponent) {
                 final int newY = closest.getY() + closest.getHeight();
                 TreePath subTreePath = t.getClosestPathForLocation(0, newY);
                 if (subTreePath == null) {
                     subTreePath = new TreePath(t.getModel().getRoot());
                 }
                 return getPathforOriginalTree(subTreePath);
             }
             return getPathforOriginalTree(new TreePath(t.getModel().getRoot()));
         } // falls x/y nicht innerhalb des container liegen return null
         else {
             return null;
         }
     }
 
     @Override
     protected Enumeration<TreePath> getDescendantToggledPaths(final TreePath parent) {
         final Vector<TreePath> toggledPaths = new Vector<TreePath>();
         final Object lastPathComponent = parent.getLastPathComponent();
         final Object parentRoot = this.getModel().getRoot();
 
         if (trees != null) {
             if (lastPathComponent.equals(parentRoot)) {
                 // falls parent gleich dem RootNode des ParentTree ist
                 // was tun ??
             } else {
                 final SlideableSubTree t = getSubTreeForPath(parent);
                 final TreePath subTreePath = getPathForSubTree(parent);
                 final Enumeration toggledSubPaths = t.getDescendantToggledPaths(subTreePath);
 
                 while (toggledSubPaths.hasMoreElements()) {
                     final TreePath originPath = getPathforOriginalTree((TreePath)toggledSubPaths.nextElement());
                     toggledPaths.add(getPathforOriginalTree(originPath));
                 }
             }
             return toggledPaths.elements();
         }
         return super.getDescendantToggledPaths(parent);
     }
 
     /*
      * fuer alle subtrees rueckwaerts durchlaufen, wenn selektion gefunden richtige Row berechnen (offset addieren)
      */
     @Override
     public int getMaxSelectionRow() {
         if (trees != null) {
             int offset = 0;
             final int maxSelection = 0;
             int indexOfTree = 0;
             /*
              * letzen Baum mit selektion herausfinden
              */
             for (int i = trees.size() - 1; i
                         >= 0; i--) {
                 final SlideableSubTree t = trees.get(i);
                 if (maxSelection != -1) {
                     indexOfTree = i;
                     break;
                 }
             }
             if (maxSelection == -1) {
                 return maxSelection;
             } else {
                 /*
                  * Offset berechnen
                  */
                 for (int i = 0; i
                             < indexOfTree; i++) {
                     offset += trees.get(i).getRowCount() + 1;
                 }
                 return maxSelection + offset;
             }
         }
         return super.getMaxSelectionRow();
     }
 
     /*
      * fuer alle subtrees durhclaufen, wenn selektion gefunden richtige Row berechnen (offset addieren)
      */
     @Override
     public int getMinSelectionRow() {
         if (trees != null) {
             int offset = 0;
             int minSelection = 0;
             final int indexOfTree = 0;
 
             for (final SlideableSubTree t : trees) {
                 minSelection = t.getMinSelectionRow();
                 if (minSelection == -1) {
                     offset += t.getRowCount() + 1;
                 } else {
                     break;
                 }
             }
             if (minSelection == -1) {
                 return minSelection;
             } else {
                 return minSelection + offset;
             }
         }
         return super.getMinSelectionRow();
     }
 
     /*
      * subtree ausfindig machen, neuen Path machen, methode weiterleiten
      */
     @Override
     public Rectangle getPathBounds(final TreePath path) {
         final SlideableSubTree t = getSubTreeForPath(path);
         // wenn durch path representierter Knoten die Wurzel des Originalbaum ist
 
         if (t == null) {
             return super.getPathBounds(path);
         } else {
             final Rectangle rec = t.getPathBounds(getPathForSubTree(path));
             final JXTaskPane pane = panes.get(trees.indexOf(t));
             rec.setLocation((int)rec.getX() + pane.getX(), (int)rec.getY() + pane.getY());
             return rec;
         }
     }
 
     /*
      * number of rows that are currently displayed durch alle subtrees durch und addieren (plus offset fuer root nodes)
      */
     @Override
     public int getRowCount() {
         int sum = 0;
 
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 // Anzahl der sichtbaren Zeilen + 1 fuer den jeweiligen Root des SubTree
                 sum += t.getRowCount();
 
                 if (!t.isRootVisible()) {
                     sum++;
                 }
             }
         }
         return sum;
     }
 
     /*
      * liefert  Path zu dem ERSTEN selektieren knoten oder null wenn nichts selektiert Die einzelnen subtrees
      * durchgehen, mehtode aufrufen Einschraenkung, RootNodes der SubTrees und des Originaltrees koennen nicht
      * selektiert werden
      */
     @Override
     public TreePath getSelectionPath() {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 /*final SubTreePane pane = panes.get(trees.indexOf(t));
                  * if (pane.isSelected()) { return getPathforOriginalTree(new TreePath(t.getModel().getRoot())); }
                  */
                 final TreePath firstSelection = t.getSelectionPath();
 
                 if (firstSelection != null) {
                     return getPathforOriginalTree(firstSelection);
                 }
             }
         }
         return null;
     }
 
     /*
      * liefert null falls keine selektion vorhanden, sonst die angepassten path objekte
      */
     @Override
     public TreePath[] getSelectionPaths() {
         final ArrayList<TreePath> paths = new ArrayList<TreePath>();
 
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 final TreePath[] selections = t.getSelectionPaths();
 
                 if (selections != null) {
                     for (int i = 0; i
                                 < selections.length; i++) {
                         paths.add(getPathforOriginalTree(selections[i]));
                     }
                 }
             }
             if (paths.isEmpty()) {
                 return null;
             }
             final TreePath[] path = new TreePath[paths.size()];
             paths.toArray(path);
 
             return path;
         }
         return null;
     }
 
     /*
      * fuer jeden Subtree aufrufen und aufaddieren
      */
     @Override
     public int getSelectionCount() {
         int sum = 0;
 
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 sum += t.getSelectionCount();
             }
             return sum;
         }
         return super.getSelectionCount();
     }
 
     /*
      * return null wenn SubTree nicht vorhanden
      *
      */
     @Override
     public TreePath getPathForRow(final int row) {
         final SlideableSubTree t = getSubTreeForRow(row);
         final int subTreeRow = getRowForSubTree(row);
 
         if (t != null) {
             if ((subTreeRow < 0) && !t.isRootVisible()) {
                 return getPathforOriginalTree(new TreePath(t.getModel().getRoot()));
             } else {
                 final TreePath tmp = t.getPathForRow(subTreeRow);
                 final TreePath path = getPathforOriginalTree(tmp);
 
                 return path;
             }
         }
         return null;
     }
 
     @Override
     public int getRowForPath(final TreePath path) {
         final SlideableSubTree subTree = getSubTreeForPath(path);
         final TreePath subPath = getPathForSubTree(path);
         int offset = 0;
         int row = -1;
 
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 if (t.equals(subTree)) {
                     break;
                 } else {
                     offset += t.getRowCount() + 1;
                 }
             }
             row = subTree.getRowForPath(subPath) + offset;
         }
         return row;
     }
 
     @Override
     public TreePath getNextMatch(final String prefix, final int startingRow, final Bias bias) {
         return super.getNextMatch(prefix, startingRow, bias);
     }
 
     @Override
     protected TreePath[] getPathBetweenRows(final int index0, final int index1) {
         final ArrayList<TreePath> list = new ArrayList<TreePath>();
 
         for (int i = index0 + 1; i
                     <= index1; i++) {
             list.add(getPathForRow(i));
         }
         final TreePath[] finalPaths = new TreePath[list.size()];
 
         for (int i = 0; i
                     < finalPaths.length; i++) {
             finalPaths[i] = list.get(i);
         }
         return finalPaths;
     }
 
     /*
      * zu knoten der durch path representiert ist zugehoerigen subtree ausfindig machen, neuen path erstellen mehtode
      * weiterleiten
      */
     @Override
     public boolean hasBeenExpanded(final TreePath path) {
         final SlideableSubTree t = getSubTreeForPath(path);
         // keinen SubTree gefunden, Knoten ist also die Wurzel oder nicht enthalten
 
         if (t == null) {
             return super.hasBeenExpanded(path);
         } else {
             return t.hasBeenExpanded(getPathForSubTree(path));
         }
     }
 
     /*
      * uberpruefen ob ein knoten in einem subtree editiert wird mehrfache editierung moeglich?
      */
     @Override
     public boolean isEditing() {
         boolean isEditing = false;
 
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 if (t.isEditing()) {
                     isEditing = true;
                 }
             }
         }
         return isEditing;
     }
 
     /*
      * herausfinden welcher knoten mit path identifiziert wird, methode weiterleiten
      */
     @Override
     public boolean isExpanded(final TreePath path) {
         final SlideableSubTree t = getSubTreeForPath(path);
 
         if (t == null) {
             return super.isExpanded(path);
         } else {
             return t.isExpanded(getPathForSubTree(path));
         }
     }
 
     /*
      * herausfinden welcher knoten mit row identifiziert wird, methode weiterleiten
      */
     @Override
     public boolean isExpanded(final int row) {
         final TreePath path = getPathForRow(row);
 
         return isExpanded(path);
     }
 
     /*
      * Vorgehensweise Was passiert falls der Knoten der Root eines Subtrees ist?? diese koennen nicht selektiert
      * werden....
      */
     @Override
     public boolean isPathSelected(final TreePath path) {
         final SlideableSubTree t = getSubTreeForPath(path);
 
         if (t == null) {
             // was passiert falls der Knoten die wurzel ist?
             return super.isPathSelected(path);
         } else {
             return t.isPathSelected(getPathForSubTree(path));
         }
     }
 
     /*
      * Vorgehensweise herausfinden welcher Knoten durch row represntiert wird, methode weiterleiten
      */
     @Override
     public boolean isRowSelected(final int row) {
         final TreePath path = getPathForRow(row);
         if (path == null) {
             return false;
         }
         final boolean isSelected = isPathSelected(path);
 
         return isSelected;
     }
 
     /*
      * Vorgehensweise fuer jeden subtree ueberpruefen ob selection emtpy
      */
     @Override
     public boolean isSelectionEmpty() {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 if (!(t.isSelectionEmpty())) {
                     return false;
                 }
             }
         }
         return true;
     }
     /*
      * Vorgehensweise wie bei makeVisible
      */
 
     @Override
     public boolean isVisible(final TreePath path) {
         final SlideableSubTree t = getSubTreeForPath(path);
 
         if (t == null) {
             return super.isVisible(path);
         } else {
             return t.isVisible(getPathForSubTree(path));
         }
     }
 
     /*
      * subtree herausfinden der den durch path beschriebenen Knoten enthaelt und und diesen sichtbar machen(methode
      * aufrufen)
      */
     @Override
     public void makeVisible(final TreePath path) {
         final JTree t = getSubTreeForPath(path);
 
         if (t == null) {
             super.makeVisible(path);
         } else {
             t.makeVisible(getPathForSubTree(path));
         }
     }
 
     @Override
     public void removeSelectionInterval(final int index0, final int index1) {
         if (trees != null) {
             if (index1 < index0) {
                 return;
             } else {
                 for (int i = index0; i
                             <= index1; i++) {
                     final TreePath path = getPathForRow(i);
                     removeSelectionPath(path);
                 }
             }
             super.removeSelectionInterval(index0, index1);
         }
     }
 
     @Override
     public void removeSelectionPath(final TreePath path) {
         if (trees != null) {
             final SlideableSubTree subTree = getSubTreeForPath(path);
             subTree.removeSelectionPath(getPathForSubTree(path));
         }
         super.removeSelectionPath(path);
     }
 
     @Override
     public void removeSelectionPaths(final TreePath[] paths) {
         for (int i = 0; i
                     < paths.length; i++) {
             removeSelectionPath(paths[i]);
         }
         super.removeSelectionPaths(paths);
     }
 
     @Override
     public void removeSelectionRow(final int row) {
         final TreePath path = getPathForRow(row);
         removeSelectionPath(
             path);
         super.removeSelectionRow(row);
     }
 
     @Override
     public void removeSelectionRows(final int[] rows) {
         for (int i = 0; i
                     < rows.length; i++) {
             final TreePath path = getPathForRow(i);
             removeSelectionPath(path);
         }
         super.removeSelectionRows(rows);
     }
 
     /*
      * subtree ausfindig machen, mehtode weiterleiten, Paths in Enuemration anpassen
      */
     @Override
     protected boolean removeDescendantSelectedPaths(final TreePath path, final boolean includePath) {
         final SlideableSubTree t = getSubTreeForPath(path);
         final TreePath subTreePath = getPathForSubTree(path);
 
         if (trees != null) {
             return t.removeDescendantSelectedPaths(subTreePath, includePath);
         } else {
             return super.removeDescendantSelectedPaths(path, includePath);
         }
     }
 
     @Override
     protected void removeDescendantToggledPaths(final Enumeration<TreePath> toRemove) {
         if (trees != null) {
             while (toRemove.hasMoreElements()) {
                 final TreePath path = toRemove.nextElement();
                 final SlideableSubTree t = getSubTreeForPath(path);
                 final Vector<TreePath> subToRemove = new Vector<TreePath>();
                 subToRemove.add(getPathForSubTree(path));
                 t.removeDescendantToggledPaths(subToRemove.elements());
             }
         } else {
             super.removeDescendantToggledPaths(toRemove);
         }
     }
 
     /*
      * prinizipiell methode an subtrees weiterreichen, evtl offsett addieren
      */
     @Override
     public int[] getSelectionRows() {
         final int[][] result = new int[trees.size()][];
         int offset = 0;
         int count = 0;
 
         if (trees != null) {
             // getSelectionRows fuer jeden Subtree
             for (final SlideableSubTree t : trees) {
                 result[trees.indexOf(t)] = t.getSelectionRows();
                 count += result[trees.indexOf(t)].length;
                 // Offset aufaddieren fuer korrekte Inidzes
                 for (int i = 0; i
                             < result[trees.indexOf(t)].length; i++) {
                     result[trees.indexOf(t)][i] += offset;
                 }
                 offset += t.getRowCount() + 1;
             }
             final int[] selectionRows = new int[count];
             // Ergebnisse zusammenfassen
 
             for (int i = 0; i
                         < selectionRows.length; i++) {
                 for (int j = 0; j
                             < result[i].length; j++) {
                     selectionRows[i] = result[i][j];
                 }
             }
             return selectionRows;
         }
         return super.getSelectionRows();
     }
 
     /*
      * Moegliche Vorgehensweise *herausfinden welches element sichtbar werden soll, ueberpruefen in welchem baum es
      * ist,und fuer diesen die methode aufrufen
      */
     @Override
     public void scrollPathToVisible(final TreePath path) {
         if (trees != null) {
             final SlideableSubTree t = getSubTreeForPath(path);
             // path ist die wurzel des baums, oder gar nicht enthalten
 
             if (t == null) {
                 super.scrollPathToVisible(path);
             } else {
                 t.scrollPathToVisible(getPathForSubTree(path));
             }
         }
     }
 
     /*
      * moegliche vorgehensweise herausfinden welches element sichtbar werden soll, ueberpruefen in welchem baum es
      * ist,und fuer diesen die methode aufrufen
      */
     @Override
     public void scrollRowToVisible(final int row) {
         final TreePath path = getPathForRow(row);
         scrollPathToVisible(
             path);
     }
 
     @Override
     public void setAnchorSelectionPath(final TreePath newPath) {
         if (trees != null) {
             final SlideableSubTree t = getSubTreeForPath(newPath);
             final TreePath subTreePath = getPathForSubTree(newPath);
 
             if (t != null) {
                 t.setAnchorSelectionPath(subTreePath);
             }
         }
         super.setAnchorSelectionPath(newPath);
     }
 
     @Override
     public void setCellEditor(final TreeCellEditor cellEditor) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setCellEditor(cellEditor);
             }
         }
         super.setCellEditor(cellEditor);
     }
 
     @Override
     public void setCellRenderer(final TreeCellRenderer x) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setCellRenderer(x);
                 final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)t.getModel().getRoot();
                 final JXTaskPane pane = panes.get(trees.indexOf(t));
                 final DefaultTreeCellRenderer renderer;
                 if (x instanceof DefaultTreeCellRenderer) {
                     renderer = (DefaultTreeCellRenderer)t.getCellRenderer();
                     final JLabel l = (JLabel)renderer.getTreeCellRendererComponent(
                             this,
                             rootNode,
                             false,
                             !(pane.isCollapsed()),
                             rootNode.isLeaf(),
                             0,
                             false);
                     pane.setIcon(l.getIcon());
 //                    if (rootNode.isLeaf()) {
 //                        pane.setIcon(renderer.getDefaultLeafIcon());
 //                    } else if (!(pane.isCollapsed())) {
 //                        pane.setIcon(renderer.getDefaultOpenIcon());
 //                    } else {
 //                        pane.setIcon(renderer.getDefaultClosedIcon());
 //                    }
                 }
             }
         }
         super.setCellRenderer(x);
     }
 
     @Override
     public void setDragEnabled(final boolean b) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setDragEnabled(b);
             }
         }
         super.setDragEnabled(b);
     }
 
     @Override
     public void setEditable(final boolean flag) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setEditable(flag);
             }
         }
         super.setEditable(flag);
     }
 
     @Override
     public void setExpandsSelectedPaths(final boolean newValue) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setExpandsSelectedPaths(newValue);
             }
         }
         super.setExpandsSelectedPaths(newValue);
     }
 
     /*
      * subtree zu knoten herausfinden, neuen path erstellen, mehtode weiterleiten ! Achtung Methode protected!!
      */
     @Override
     protected void setExpandedState(final TreePath path, final boolean state) {
         if (trees != null) {
             final SlideableSubTree t = getSubTreeForPath(path);
             final TreePath subTreePath = getPathForSubTree(path);
             t.setExpandedState(subTreePath, state);
         }
     }
 
     @Override
     public void setInvokesStopCellEditing(final boolean newValue) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setInvokesStopCellEditing(newValue);
             }
         }
         super.setInvokesStopCellEditing(newValue);
     }
 
     @Override
     public void setLargeModel(final boolean newValue) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setLargeModel(newValue);
             }
             super.setLargeModel(newValue);
         }
     }
 
     @Override
     public void setLeadSelectionPath(final TreePath newPath) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setLeadSelectionPath(newPath);
             }
         }
         super.setLeadSelectionPath(newPath);
     }
 
     @Override
     /*
      *  aendert sich das Model, muesssen neue Subtrees mit neuem DelegatingModel erzeugt werden.
      */
     public void setModel(final TreeModel newModel) {
         final TreeModel oldModel = this.getModel();
         treeModel = newModel;
         firePropertyChange(TREE_MODEL_PROPERTY, oldModel, newModel);
 
         if (trees != null) {
             createSubTrees(newModel);
             flushTreeContainer();
             addToTreeContainer(panes);
         }
     }
 
     @Override
     public void setRootVisible(final boolean rootVisible) {
         for (final SlideableSubTree t : trees) {
             t.setRootVisible(rootVisible);
         }
         super.setRootVisible(rootVisible);
     }
 
     @Override
     public void setRowHeight(final int rowHeight) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setRowHeight(rowHeight);
             }
         } else {
             super.setRowHeight(rowHeight);
         }
     }
 
     @Override
     public void setScrollsOnExpand(final boolean newValue) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setScrollsOnExpand(newValue);
             }
         }
     }
 
     @Override
     public void setSelectionInterval(final int index0, final int index1) {
         if (trees != null) {
             final ArrayList<TreePath> pathList = new ArrayList<TreePath>();
             if (index1 < index0) {
                 return;
             } else {
                 for (int i = index0; i
                             <= index1; i++) {
                     final TreePath path = getPathForRow(i);
                     pathList.add(path);
                 }
                 final TreePath[] finalPaths = new TreePath[pathList.size()];
 
                 for (int i = 0; i
                             < finalPaths.length; i++) {
                     finalPaths[i] = pathList.get(i);
                 }
                 setSelectionPaths(finalPaths);
             }
         } else {
             super.setSelectionInterval(index0, index1);
         }
     }
 
     @Override
     public void setSelectionModel(final TreeSelectionModel selectionModel) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setSelectionModel(selectionModel);
             }
         } else {
             super.setSelectionModel(selectionModel);
         }
     }
 
     @Override
     public void setSelectionPath(final TreePath path) {
         if (trees != null) {
             final SlideableSubTree t = getSubTreeForPath(path);
 
             for (final JTree tmpTree : trees) {
                 if (t.equals(tmpTree)) {
                     continue;
                 } else {
                     tmpTree.clearSelection();
                 }
             }
             if (t == null) {
                 // was wenn der Root knoten selektiert werden soll
             } else {
                 t.setSelectionPath(getPathForSubTree(path));
             }
         }
         // super.setSelectionPath(path);
     }
 
     @Override
     public void setSelectionPaths(final TreePath[] paths) {
         for (int i = 0; i
                     < paths.length; i++) {
             setSelectionPath(paths[i]);
         }
     }
 
     @Override
     public void setSelectionRow(final int row) {
         final TreePath path = getPathForRow(row);
         final SlideableSubTree subTree = getSubTreeForPath(path);
         subTree.setSelectionPath(getPathForSubTree(path));
     }
 
     @Override
     public void setSelectionRows(final int[] rows) {
         for (int i = 0; i
                     < rows.length; i++) {
             final TreePath path = getPathForRow(rows[i]);
             final SlideableSubTree subTree = getSubTreeForPath(path);
             subTree.setSelectionPath(getPathForSubTree(path));
         }
     }
 
     @Override
     public void setShowsRootHandles(final boolean newValue) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setShowsRootHandles(newValue);
             }
         } else {
             super.setShowsRootHandles(newValue);
         }
     }
 
     @Override
     public void setToggleClickCount(final int clickCount) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setToggleClickCount(clickCount);
             }
         } else {
             super.setToggleClickCount(clickCount);
         }
     }
 
     @Override
     public void setUI(final TreeUI ui) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setUI(ui);
             }
         } else {
             super.setUI(ui);
         }
     }
 
     @Override
     public void setVisibleRowCount(final int newCount) {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.setVisibleRowCount(newCount);
             }
         }
         super.setVisibleRowCount(newCount);
     }
 
     /*
      * den Subtree zu Path herusfinden und methode delegieren was ist wenn der RootNode eines SubTrees editiert werden
      * soll
      */
     @Override
     public void startEditingAtPath(final TreePath path) {
         final SlideableSubTree t = getSubTreeForPath(path);
         if (t == null) {
             super.startEditingAtPath(path);
         } else {
             t.startEditingAtPath(getPathForSubTree(path));
         }
     }
 
     /*
      * liefert false, wenn der baum nicht editiert wurde, daher nur true wenn bei einem der Subtrees true zurueck kommt
      */
     @Override
     public boolean stopEditing() {
         boolean stopped = false;
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 if (t.stopEditing()) {
                     stopped = true;
                 }
             }
         } else {
             return super.stopEditing();
         }
         return stopped;
     }
 
     @Override
     public void updateUI() {
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 t.updateUI();
             }
         }
         super.updateUI();
     }
 
     /**
      * Hilfsmethode, die den Inhalt des JXTaskPaneContainer erstellt (also die einzelnen JXTaskPanes mit SubTree).
      *
      * @param  model  DOCUMENT ME!
      */
     private void createSubTrees(final TreeModel model) {
         final TreeModelListener listener = createTreeModelListener();
         treeModel.addTreeModelListener(listener);
         flushTreeContainer();
         trees = new ArrayList<SlideableSubTree>();
         panes = new ArrayList<SubTreePane>();
         final Object root = this.getModel().getRoot();
         final int childCount = this.getModel().getChildCount(root);
 
         for (int i = 0; i
                     < childCount; i++) {
             final Object child = model.getChild(root, i);
             final TreeNode newRootNode = new DefaultMutableTreeNode(child);
             final SlideableSubTree subTree = new SlideableSubTree(newRootNode);
             final DelegatingModel modelDelegate = new DelegatingModel(child, model);
             subTree.setModel(modelDelegate);
             subTree.setRootVisible(false);
             subTree.addTreeExpansionListener(this);
             subTree.addTreeSelectionListener(this);
             subTree.addTreeWillExpandListener(this);
             // subTree.setDragEnabled(true);
             subTree.setBorder(new EmptyBorder(1, 1, 1, 1));
             // final MetaTreeNodeDNDHandler dndHandler = new MetaTreeNodeDNDHandler(subTree);
             trees.add(subTree);
             final SubTreePane tmpPane = new SubTreePane();
             final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)subTree.getModel().getRoot();
             final DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)trees.get(i).getCellRenderer();
             Icon icon = null;
             tmpPane.setCollapsed(true);
 
             // icon ändert sich nicht, falls JXTaskpane collapsed/!(collapsed)
             if (rootNode.isLeaf()) {
                 icon = renderer.getLeafIcon();
             } else if (tmpPane.isCollapsed()) {
                 icon = renderer.getClosedIcon();
             } else {
                 icon = renderer.getOpenIcon();
             }
             tmpPane.setIcon(icon);
             tmpPane.setTitle(newRootNode.toString());
             tmpPane.addMouseListener(new MouseAdapter() {
 
                     /*
                      * wird auf eine Pane geklickt(linke maustaste) wird diese abhaengig vom zustand selektiert bzw.
                      * deselektiert und ein event gefeuert das dieser Knoten expandiert wurde (pane klappt sich auf,
                      * autom. Nachladen der Kinder) sowie, das der Knoten Selektiert wurde(für beschreibung)
                      */
                     @Override
                     public void mouseClicked(final MouseEvent e) {
                         if (e.isPopupTrigger()) {
                             // System.out.println("popup menu");
                         } else {
                             final SubTreePane pane = (SubTreePane)e.getSource();
                             final SlideableSubTree t = trees.get(panes.indexOf(pane));
                             final TreePath subTreePath = new TreePath(t.getModel().getRoot());
                             final TreePath path = SlideableTree.this.getPathforOriginalTree(subTreePath);
                             if (pane.isSelected()) {
                                 SlideableTree.this.getSelectionModel().removeSelectionPath(path);
                                 SlideableTree.this.fireTreeCollapsed(path);
                                 pane.setSelected(false);
                                 final DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)t.getCellRenderer();
                                 if (renderer instanceof DefaultTreeCellRenderer) {
                                     final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)t
                                                 .getModel().getRoot();
                                     final JLabel l = (JLabel)renderer.getTreeCellRendererComponent(
                                             SlideableTree.this,
                                             rootNode,
                                             false,
                                             !(pane.isCollapsed()),
                                             rootNode.isLeaf(),
                                             0,
                                             false);
                                     pane.setIcon(l.getIcon());
                                 }
                             } else {
                                 SlideableTree.this.fireValueChanged(
                                     new TreeSelectionEvent(
                                         SlideableTree.this,
                                         path,
                                         true,
                                         SlideableTree.this.getLeadSelectionPath(),
                                         path));
 
 //                            SlideableTree.this.getSelectionModel().setSelectionPath(path);
                                 SlideableTree.this.fireTreeExpanded(path);
                                 pane.setSelected(true);
                                 final DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)t.getCellRenderer();
                                 if (renderer instanceof DefaultTreeCellRenderer) {
                                     final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)t
                                                 .getModel().getRoot();
                                     final JLabel l = (JLabel)renderer.getTreeCellRendererComponent(
                                             SlideableTree.this,
                                             rootNode,
                                             false,
                                             !(pane.isCollapsed()),
                                             rootNode.isLeaf(),
                                             0,
                                             false);
                                     pane.setIcon(l.getIcon());
                                 }
                             }
                         }
                     }
                 });
 
             ((JComponent)tmpPane.getContentPane()).setBorder(new EmptyBorder(1, 16, 1, 1));
             tmpPane.add(subTree);
             panes.add(tmpPane);
         }
     }
 
     /**
      * Hilfsmethode zum leeren des Containers der die SubTrees enthaelt.
      */
     private void flushTreeContainer() {
         if (container != null) {
             for (int i = 0; i
                         < container.getComponentCount(); i++) {
                 container.remove(container.getComponent(i));
             }
         }
     }
 
     /**
      * Hilfsmethode zum bef�llen des Containers der die Subtrees entaehlt.
      *
      * @param  list  DOCUMENT ME!
      */
     private void addToTreeContainer(final ArrayList<SubTreePane> list) {
         for (final JComponent c : list) {
             container.add(c);
         }
     }
 
     /**
      * Hilfsmethode die zu einem Knoten der durch einen TreePath representiert wird, den subtree zurueckliefert, der
      * diesen Knoten enthaelt liefert null zurueck, falls es keinen Subtree gibt.
      *
      * @param   path  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private SlideableSubTree getSubTreeForPath(final TreePath path) {
         final Object pathRoot = path.getPathComponent(0);
         // Der Pfad entaehlt nur einen Knoten, also die Wurzel
         if (path.getPathCount() <= 1) {
             return null;
         } else {
             final Object pathSubRoot = path.getPathComponent(1);
             if (trees != null) {
                 for (final JTree t : trees) {
                     final Object subTreeRoot = t.getModel().getRoot();
 
                     if (pathSubRoot.equals(subTreeRoot)) {
                         return (SlideableSubTree)t;
                     }
                 }
             }
         }
         return null;
     }
 
     /**
      * Hilfsmethode die den Pfad eines Subtrees zu einem Pfad des originalen Baums transformiert.
      *
      * @param   subTreePath  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private TreePath getPathforOriginalTree(final TreePath subTreePath) {
         final Object pathRoot = subTreePath.getPathComponent(0);
         final Object origRoot = this.getModel().getRoot();
         // falls der Methode bereits ein pfad des original Baum uebergeben wird,
         // diesen einfach zurueckgeben
         if (pathRoot.equals(origRoot)) {
             return subTreePath;
         }
         final Object[] oldPath = subTreePath.getPath();
         final Object[] newPath = new Object[oldPath.length + 1];
         newPath[0] = origRoot;
         System.arraycopy(oldPath, 0, newPath, 1, oldPath.length);
         return new TreePath(newPath);
     }
 
     /**
      * Hilfsmethode die einen Pfad des OriginalTrees zu einem Pfad des entsprechenden subtrees generiert return null,
      * wenn originPath kein Path des OriginalBaums ist.
      *
      * @param   originPath  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private TreePath getPathForSubTree(final TreePath originPath) {
         final Object pathRoot = originPath.getPathComponent(0);
         final Object originRoot = this.getModel().getRoot();
 
         // wenn path nicht zum originalbaum gehoert
         if (!pathRoot.equals(originRoot)) {
             return null;
         } // der pfad enthaelt nur den Root knoten des Originalbaums
         else if (originPath.getPathCount() <= 1) {
             return null;
         } else {
             final Object[] oldPath = originPath.getPath();
             final Object[] newPath = new Object[oldPath.length - 1];
             System.arraycopy(oldPath, 1, newPath, 0, newPath.length);
 
             return new TreePath(newPath);
         }
     }
 
     /**
      * Hilfsmethode return null fuer falls row > rowCount ist bzw. keine SubTrees vorhanden sind
      *
      * @param   row  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private SlideableSubTree getSubTreeForRow(final int row) {
         int sum = 0;
         if (trees != null) {
             for (final SlideableSubTree t : trees) {
                 sum += t.getRowCount();
                 if (!t.isRootVisible()) {
                     sum += 1;
                 }
                 if (sum > row) {
                     return t;
                 }
             }
         }
         return null;
     }
 
     /**
      * Hilfsmethode.
      *
      * @param   originRow  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private int getRowForSubTree(final int originRow) {
         final SlideableSubTree tmpTree = getSubTreeForRow(originRow);
 
         int offset = 1;
 
         if (tmpTree != null) {
             for (final SlideableSubTree t : trees) {
                 if (t.equals(tmpTree)) {
                     break;
                 } else {
                     offset += t.getRowCount();
 
                     if (!t.isRootVisible()) {
                         offset++;
                     }
                 }
             }
         }
         return originRow - offset;
     }
 
     /*
      * Returns the last path component in the first node of the current selection. ruft methode fuer path auf, der als
      * ergebnis von getSelectedpath zurueckkommt daher nicht ueberschreibe
      * */
     @Override
     public Object getLastSelectedPathComponent() {
         final TreePath path = this.getSelectionPath();
 
         if (path != null) {
             return path.getLastPathComponent();
         }
         return null;
     }
 
     @Override
     public void addTreeExpansionListener(final TreeExpansionListener tel) {
         super.addTreeExpansionListener(tel);
         /*if (trees != null) {
          *  for (final SlideableSubTree t : trees) {     t.addTreeExpansionListener(tel); } }
          * */
     }
 
     @Override
     public void fireTreeExpanded(final TreePath path) {
         final TreeExpansionListener[] listener = this.getTreeExpansionListeners();
 
         for (int i = 0; i < listener.length; i++) {
             listener[i].treeExpanded(new TreeExpansionEvent(this, path));
         }
     }
 
     /**
      * nicht zu ueberschreibende Methoden.
      *
      * @param  event  DOCUMENT ME!
      */
     /*************************************************************************/
 // <editor-fold defaultstate="collapsed" desc="comment">
     /*
      * @Override public void addTreeExpansionListener(TreeExpansionListener tel) {
      *
      * super.addTreeExpansionListener(tel); }
      *
      * @Override public void addTreeSelectionListener(TreeSelectionListener tsl) {
      *
      * super.addTreeSelectionListener(tsl); }
      *
      * @Override public void addTreeWillExpandListener(TreeWillExpandListener tel) {
      *
      * super.addTreeWillExpandListener(tel); }
      *
      * @Override public void removeTreeExpansionListener(TreeExpansionListener tel) {
      * super.removeTreeExpansionListener(tel); }
      *
      * @Override public void removeTreeSelectionListener(TreeSelectionListener tsl) {
      * super.removeTreeSelectionListener(tsl); }
      *
      * @Override public void removeTreeWillExpandListener(TreeWillExpandListener tel) {
      * super.removeTreeWillExpandListener(tel); }
      *
      *
      * laut JavaDoc wird diese Methode lediglich von der UI aufgerufen zb wenn Knoten expandiert oder hinzugef�gt wurden,
      * daher m.E.n. keine neue impl notwendig
      *
      * repaint() revalidate() @Override public void treeDidChange() { super.treeDidChange(); }
      *
      * @Override public boolean isRootVisible() { return super.isRootVisible(); }
      *
      * laut javadoc f�r debug gedacht, muss also evtl nicht �berschrieben werden @Override protected String paramString()
      * { return super.paramString(); }
      *
      * @Override public boolean isFixedRowHeight() { return super.isFixedRowHeight(); }
      *
      * @Override public boolean isLargeModel() { return super.isLargeModel(); }
      *
      * returns isEditable @Override public boolean isPathEditable(TreePath path) { return super.isPathEditable(path); }
      *
      * returns !isExpandend(path) @Override public boolean isCollapsed(TreePath path) { return super.isCollapsed(path); }
      *
      * returns !isExpandend(row) @Override public boolean isCollapsed(int row) { return super.isCollapsed(row); }
      *
      * @Override public boolean isEditable() { return super.isEditable(); }
      *
      * @Override public boolean getShowsRootHandles() { return super.getShowsRootHandles(); }
      *
      * @Override public int getToggleClickCount() { return super.getToggleClickCount(); }
      *
      * @Override public String getToolTipText(MouseEvent event) { return super.getToolTipText(event); }
      *
      * @Override public TreeExpansionListener[] getTreeExpansionListeners() { return super.getTreeExpansionListeners(); }
      *
      * @Override public TreeSelectionListener[] getTreeSelectionListeners() { return super.getTreeSelectionListeners(); }
      *
      * @Override public TreeWillExpandListener[] getTreeWillExpandListeners() { return super.getTreeWillExpandListeners();
      * }
      *
      * @Override public TreeUI getUI() { return super.getUI(); }
      *
      * @Override public String getUIClassID() { return super.getUIClassID(); }
      *
      * @Override public int getVisibleRowCount() { return super.getVisibleRowCount(); }
      *
      * @Override public TreeSelectionModel getSelectionModel() { return super.getSelectionModel(); }
      *
      * @Override public boolean getScrollsOnExpand() { return super.getScrollsOnExpand(); }
      *
      * @Override public int getRowHeight() { return super.getRowHeight(); }
      *
      * @Override public TreeModel getModel() { return super.getModel(); }
      *
      * return getPathBounds(getPathForRow(row)) daher nicht �berschreiben @Override public Rectangle getRowBounds(int row)
      * { return super.getRowBounds(row); }
      *
      * @Override public boolean getDragEnabled() { return super.getDragEnabled(); }
      *
      * @Override public TreeCellEditor getCellEditor() { return super.getCellEditor(); }
      *
      * @Override public TreeCellRenderer getCellRenderer() { return super.getCellRenderer(); }
      *
      *
      * nicht �berschreiben setExpandedState(path, false) @Override public void collapsePath(TreePath path) {
      * super.collapsePath(path); }
      *
      * nicht �berschreiben collapsePath(getPathForRow(row)) @Override public void collapseRow(int row) {
      * super.collapseRow(row); }
      *
      * nicht �berschreiben setExpandedState(path, true) subtree f�r knoten herausfinden, path umbauen, mehtode
      * weiterleiten @Override public void expandPath(TreePath path) { super.expandPath(path); }
      *
      * return getRowForPath(getPathForLocation(x, y)); @Override public int getRowForLocation(int x, int y) { return
      * super.getRowForLocation(x, y); }
      *
      * return getRowForPath(getClosestPathForLocation(x, y)); @Override public int getClosestRowForLocation(int x, int y)
      * { return super.getClosestRowForLocation(x, y); }
      *
      * @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return
      * super.getScrollableBlockIncrement(visibleRect, orientation, direction); }
      *
      * @Override public boolean getScrollableTracksViewportHeight() { return super.getScrollableTracksViewportHeight(); }
      *
      * @Override public boolean getScrollableTracksViewportWidth() { return super.getScrollableTracksViewportWidth(); }
      *
      * @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return
      * super.getScrollableUnitIncrement(visibleRect, orientation, direction); }
      *
      * @Override public Dimension getPreferredScrollableViewportSize() { return
      * super.getPreferredScrollableViewportSize(); }
      *
      * @Override public boolean getExpandsSelectedPaths() { return super.getExpandsSelectedPaths(); }
      *
      * @Override public boolean getInvokesStopCellEditing() { return super.getInvokesStopCellEditing(); }
      *
      * @Override public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row,
      * boolean hasFocus) { return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus); }
      *
      * @Override protected TreeModelListener createTreeModelListener() { return super.createTreeModelListener(); }
      *
      * @Override public TreePath getAnchorSelectionPath() { return super.getAnchorSelectionPath(); }
      *
      * Gets the AccessibleContext associated with this JTree. For JTrees, the AccessibleContext takes the form of an
      * AccessibleJTree. A new AccessibleJTree instance is created if necessary.
      *
      * @Override public AccessibleContext getAccessibleContext() { return super.getAccessibleContext(); }
      *
      * @Override public void fireTreeCollapsed(TreePath path) { super.fireTreeCollapsed(path); }
      *
      * @Override public void fireTreeExpanded(TreePath path) { super.fireTreeExpanded(path); }
      *
      * @Override public void fireTreeWillCollapse(TreePath path) throws ExpandVetoException {
      * super.fireTreeWillCollapse(path); }
      *
      * @Override public void fireTreeWillExpand(TreePath path) throws ExpandVetoException {
      * super.fireTreeWillExpand(path); }
      *
      * @Override protected void fireValueChanged(TreeSelectionEvent e) { super.fireValueChanged(e); }
      *
      *
      * nicht �berschreiben
      *
      * @Override public TreePath getLeadSelectionPath() { return super.getLeadSelectionPath(); }
      *
      *
      * returns row for getleadselectionPath methode getRowforPath wird benutzt daher nicht �berschreiben
      *
      * @Override public int getLeadSelectionRow() { return super.getLeadSelectionRow(); }
      */
     // </editor-fold>
     /**
      * Interface Methods.
      *
      * @param  event  DOCUMENT ME!
      */
     /************************************************************************/
     @Override
     public void treeExpanded(final TreeExpansionEvent event) {
         fireTreeExpanded(getPathforOriginalTree(event.getPath()));
     }
 
     @Override
     public void treeCollapsed(final TreeExpansionEvent event) {
         fireTreeCollapsed(getPathforOriginalTree(event.getPath()));
     }
 
     @Override
     public void valueChanged(final TreeSelectionEvent e) {
         if (trees != null) {
             final TreePath path = getPathforOriginalTree(e.getPath());
 
             for (final JTree tmpTree : trees) {
                 if (!(e.isAddedPath()) || tmpTree.equals((SlideableSubTree)e.getSource())) {
                     continue;
                 } else {
                     tmpTree.clearSelection();
                 }
             }
             fireValueChanged(e);
         }
     }
 
     @Override
     public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
         fireTreeWillExpand(getPathforOriginalTree(event.getPath()));
     }
 
     @Override
     public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
         fireTreeWillCollapse(getPathforOriginalTree(event.getPath()));
     }
 
     @Override
     public TreeModelListener createTreeModelListener() {
         return new MyTreeModelHandler(this);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  node    DOCUMENT ME!
      * @param  indent  DOCUMENT ME!
      */
     public void print(final TreeNode node, String indent) {
         System.out.println(indent + node.toString());
         indent += "\t";
 
         for (int i = 0; i
                     < node.getChildCount(); i++) {
             if (node.getChildCount() > 0) {
                 final TreeNode child = node.getChildAt(i);
 
                 if (child.isLeaf()) {
                     System.out.println(indent + child.toString());
                 } else {
                     indent += "\t";
                     this.print(child, indent);
                 }
             }
         }
     }
 
     @Override
     public void addMouseListener(final MouseListener l) {
         if (trees != null) {
             for (final JTree t : trees) {
                 t.addMouseListener(l);
                 panes.get(trees.indexOf(t)).addMouseListener(l);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public ArrayList<SlideableSubTree> getTrees() {
         return trees;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public ArrayList<SubTreePane> getPanes() {
         return panes;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class MyTreeModelHandler implements TreeModelListener {
 
         //~ Instance fields ----------------------------------------------------
 
         private SlideableTree tree;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new MyTreeModelHandler object.
          */
         public MyTreeModelHandler() {
         }
 
         /**
          * Creates a new MyTreeModelHandler object.
          *
          * @param  t  DOCUMENT ME!
          */
         public MyTreeModelHandler(final SlideableTree t) {
             tree = t;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void treeNodesChanged(final TreeModelEvent e) {
             final SlideableSubTree t = getSubTreeForPath(e.getTreePath());
             t.updateUI();
         }
 
         @Override
         public void treeNodesInserted(final TreeModelEvent e) {
             final SlideableSubTree t = getSubTreeForPath(e.getTreePath());
             t.updateUI();
         }
 
         @Override
         public void treeStructureChanged(final TreeModelEvent e) {
             final SlideableSubTree t = getSubTreeForPath(e.getTreePath());
             t.updateUI();
         }
 
         @Override
         public void treeNodesRemoved(final TreeModelEvent e) {
             final SlideableSubTree t = getSubTreeForPath(e.getTreePath());
             t.updateUI();
         }
     }
 }
