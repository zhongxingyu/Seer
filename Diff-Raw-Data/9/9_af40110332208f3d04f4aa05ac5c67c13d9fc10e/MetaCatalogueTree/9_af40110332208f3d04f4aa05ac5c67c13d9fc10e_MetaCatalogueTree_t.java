 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.ui.tree;
 
 import Sirius.navigator.NavigatorConcurrency;
 import Sirius.navigator.method.MethodManager;
 import Sirius.navigator.types.treenode.ClassTreeNode;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.types.treenode.PureTreeNode;
 import Sirius.navigator.types.treenode.RootTreeNode;
 import Sirius.navigator.types.treenode.WaitTreeNode;
 import Sirius.navigator.ui.status.DefaultStatusChangeSupport;
 import Sirius.navigator.ui.status.Status;
 import Sirius.navigator.ui.status.StatusChangeListener;
 import Sirius.navigator.ui.status.StatusChangeSupport;
 
 import Sirius.server.middleware.types.MetaClassNode;
 import Sirius.server.middleware.types.MetaNode;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.newuser.permission.PermissionHolder;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.WeakListeners;
 
 import java.awt.EventQueue;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.dnd.Autoscroll;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.swing.AbstractAction;
 import javax.swing.ActionMap;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeExpansionListener;
 import javax.swing.event.TreeModelListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import de.cismet.cids.navigator.utils.MetaTreeNodeVisualization;
 
 import de.cismet.tools.CismetThreadPool;
 
 /**
  * DefaultMetaTree ist ein Navigationsbaum.
  *
  * @version  $Revision$, $Date$
  */
 public class MetaCatalogueTree extends JTree implements StatusChangeSupport, Autoscroll {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(MetaCatalogueTree.class);
 
     //~ Instance fields --------------------------------------------------------
 
     protected final DefaultStatusChangeSupport statusChangeSupport;
     protected final DefaultTreeModel defaultTreeModel;
     protected final boolean useThread;
 
     private BufferedImage dragImage = null;
     // HELL
     private int margin = 12;
 
     private final transient MetaTreeRefreshCache refreshCache;
     private final transient ExecutorService treePool;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MetaCatalogueTree object.
      *
      * @param  rootTreeNode  DOCUMENT ME!
      * @param  editable      DOCUMENT ME!
      */
     public MetaCatalogueTree(final RootTreeNode rootTreeNode, final boolean editable) {
         this(rootTreeNode, editable, true, 3);
     }
 
     /**
      * Creates a new MetaCatalogueTree object.
      *
      * @param  rootTreeNode    DOCUMENT ME!
      * @param  editable        DOCUMENT ME!
      * @param  useThread       DOCUMENT ME!
      * @param  maxThreadCount  DOCUMENT ME!
      */
     public MetaCatalogueTree(final RootTreeNode rootTreeNode,
             final boolean editable,
             final boolean useThread,
             final int maxThreadCount) {
         this.setModel(new DefaultTreeModel(rootTreeNode, true));
         this.setEditable(editable);
         this.useThread = useThread;
 
         this.statusChangeSupport = new DefaultStatusChangeSupport(this);
         this.defaultTreeModel = (DefaultTreeModel)this.getModel();
 
         this.refreshCache = new MetaTreeRefreshCache();
         this.defaultTreeModel.addTreeModelListener(WeakListeners.create(
                 TreeModelListener.class,
                 refreshCache,
                 defaultTreeModel));
         this.treePool = Executors.newFixedThreadPool(
                 maxThreadCount,
                 NavigatorConcurrency.createThreadFactory("meta-tree")); // NOI18N
 
         this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
         this.setCellRenderer(new MetaTreeNodeRenderer());
         this.putClientProperty("JTree.lineStyle", "Angled"); // NOI18N
         this.setShowsRootHandles(true);
         this.setRootVisible(false);
         this.addTreeSelectionListener(new MetaCatalogueSelectionListener());
         this.addTreeExpansionListener(new MetaCatalogueExpansionListener());
 
         if (editable) {
             final InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
             final ActionMap actionMap = this.getActionMap();
 
             inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteNode()"); // NOI18N
             actionMap.put("deleteNode()", new AbstractAction() {                         // NOI18N
 
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("performing delete node action");                                // NOI18N
                         }
                         final DefaultMetaTreeNode selectedNode = MetaCatalogueTree.this.getSelectedNode();
                         if ((selectedNode != null) && selectedNode.isLeaf()) {
                             if (MethodManager.getManager().checkPermission(
                                             selectedNode.getNode(),
                                             PermissionHolder.WRITEPERMISSION)) {
                                 MethodManager.getManager().deleteNode(MetaCatalogueTree.this, selectedNode);
                             } else if (LOG.isDebugEnabled()) {
                                 LOG.warn("actionPerformed() deleting not possible, no node selected"); // NOI18N
                             }
                         }
                     }
                 });
         }
         addMouseListener(new MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final MouseEvent e) {
                     super.mouseClicked(e);
                     if (e.getClickCount() > 1) {
                         try {
                             final ArrayList<DefaultMetaTreeNode> v = new ArrayList<DefaultMetaTreeNode>();
                             final DefaultMetaTreeNode[] resultNodes = getSelectedNodesArray();
                             for (int i = 0; i < resultNodes.length; ++i) {
                                 if (LOG.isDebugEnabled()) {
                                     LOG.debug("resultNodes:" + resultNodes[i]); // NOI18N
                                 }
                                 if (resultNodes[i].getNode() instanceof MetaObjectNode) {
                                     final DefaultMetaTreeNode otn = resultNodes[i];
                                     v.add(otn);
                                 }
                             }
                             if (v.size() > 0) {
                                 MetaTreeNodeVisualization.getInstance().addVisualization(v);
                             }
                         } catch (Throwable t) {
                             LOG.warn("Error of displaying map", t);             // NOI18N
                         }
                     }
                 }
             });
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Expandiert alle geladenen Knoten.
      */
     public void expandAll() {
         int row = 0;
         while (row < this.getRowCount()) {
             this.expandRow(row);
             row++;
         }
     }
 
     /**
      * Schliesst alle expandierten Knoten.
      */
     public void collapseAll() {
         int row = this.getRowCount() - 1;
         while (row >= 0) {
             this.collapseRow(row);
             row--;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   artificialId  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Set<Future> requestRefreshNode(final String artificialId) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("refresh for artificial id requested: " + artificialId); // NOI18N
         }
 
         final Set<Future> futures = new HashSet<Future>();
 
         if (refreshCache.isValid()) {
             final Set<DefaultMetaTreeNode> nodes = refreshCache.get(artificialId);
 
             final Iterator<DefaultMetaTreeNode> it = nodes.iterator();
 
             while (it.hasNext()) {
                 final DefaultMetaTreeNode node = it.next();
                 if ((node == null) || !node.isExplored()) {
                     // we won't do anything, the node is not in cache or has not been explored yet, so an update would
                     // be pointless
                 } else {
                     futures.add(treePool.submit(new RefreshWorker(node)));
                 }
             }
         } else {
             LOG.warn("cannot refresh nodes, because the cache is invalid"); // NOI18N
         }
 
         return futures;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   treePath  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Future exploreSubtree(final TreePath treePath) {
         final Set<Future> futures = new HashSet<Future>();
         final Object[] nodes = treePath.getPath();
         final Object rootNode = this.getModel().getRoot();
         final ArrayList<DefaultMetaTreeNode> dmtnNodeList = new ArrayList<DefaultMetaTreeNode>();
 
         if ((rootNode != null) && (nodes != null) && (nodes.length > 1)) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("exploring subtree: " + nodes.length);                                                        // NOI18N
             }
             final List<?> nodeList = Arrays.asList(nodes);
             for (final Object o : nodeList) {
                 if (!(o instanceof DefaultMetaTreeNode)) {
                     nodeList.remove(o);
                     LOG.warn("Node " + o                                                                                // NOI18N
                                 + " is not instance of DefaultMetaTreeNode and has been removed from the Collection."); // NOI18N
                 }
             }
             final Iterator<DefaultMetaTreeNode> childrenIterator = (Iterator<DefaultMetaTreeNode>)nodeList.iterator();
 
             // Root Node entfernen
             childrenIterator.next();
             final SubTreeExploreThread subTreeExploreThread = new SubTreeExploreThread((DefaultMetaTreeNode)rootNode,
                     childrenIterator);
 
             return CismetThreadPool.submit(subTreeExploreThread);
         } else {
             LOG.warn("could not explore subtree"); // NOI18N
         }
 
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   treePath  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Future refreshTreePath(final TreePath treePath) {
         final Set<Future> futures = new HashSet<Future>();
         final Object[] nodes = treePath.getPath();
         final Object rootNode = this.getModel().getRoot();
         final ArrayList<DefaultMetaTreeNode> dmtnNodeList = new ArrayList<DefaultMetaTreeNode>();
 
         if ((rootNode != null) && (nodes != null) && (nodes.length > 1)) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("exploring subtree: " + nodes.length);                                                        // NOI18N
             }
             final List<?> nodeList = Arrays.asList(nodes);
             for (final Object o : nodeList) {
                 if (!(o instanceof DefaultMetaTreeNode)) {
                     nodeList.remove(o);
                     LOG.warn("Node " + o                                                                                // NOI18N
                                 + " is not instance of DefaultMetaTreeNode and has been removed from the Collection."); // NOI18N
                 }
             }
             final Iterator<DefaultMetaTreeNode> childrenIterator = (Iterator<DefaultMetaTreeNode>)nodeList.iterator();
 
             // Root Node entfernen
             childrenIterator.next();
 
             while (childrenIterator.hasNext()) {
                 final DefaultMetaTreeNode node = childrenIterator.next();
 //                if ((node == null) || !node.isExplored()) {
 //                    // we won't do anything, the node is not in cache or has not been explored yet, so an update would
 //                    // be pointless
 //                } else {
 //                    if (LOG.isDebugEnabled()) {
 //                        LOG.debug("refresh node: " + node);
 //                    }
                 futures.add(treePool.submit(new RefreshWorker(node)));
 //                }
             }
             return null;
         } else {
             LOG.warn("could not explore subtree"); // NOI18N
         }
 
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getSelectedNodeCount() {
         final TreePath[] selectedPaths = this.getSelectionPaths();
 
         if ((selectedPaths == null) || (selectedPaths.length == 0)) {
             return 0;
         }
 
         int j = 0;
 
         for (int i = 0; i < selectedPaths.length; i++) {
             if (!((DefaultMetaTreeNode)selectedPaths[i].getLastPathComponent()).isWaitNode()) {
                 j++;
             }
         }
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("<TREE> getSelectedNodeCount(): " + j); // NOI18N
         }
 
         return j;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public DefaultMetaTreeNode getSelectedNode() {
         final Object object = this.getLastSelectedPathComponent();
 
         if ((object != null) && !((DefaultMetaTreeNode)object).isWaitNode()
                     && !((DefaultMetaTreeNode)object).isRootNode()) {
             return (DefaultMetaTreeNode)object;
         } else {
             return null;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Collection getSelectedNodes() {
         final TreePath[] selectedPaths = this.getSelectionPaths();
 
         if ((selectedPaths != null) && (selectedPaths.length > 0)) {
             final ArrayList selectedNodes = new ArrayList(selectedPaths.length);
             for (int i = 0; i < selectedPaths.length; i++) {
                 selectedNodes.add(selectedPaths[i].getLastPathComponent());
             }
 
             return selectedNodes;
         } else {
             return new LinkedList();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  selectedNodes  DOCUMENT ME!
      * @param  expandTree     DOCUMENT ME!
      */
     public void setSelectedNodes(final Collection<DefaultMutableTreeNode> selectedNodes, final boolean expandTree) {
         if (LOG.isDebugEnabled()) {
             LOG.info("setSelectedNodes(): selecting " + selectedNodes.size() + " nodes, expanding tree: "
                         + expandTree); // NOI18N
         }
         final ArrayList treePaths = new ArrayList();
         this.setExpandsSelectedPaths(expandTree);
 
         for (final Iterator<DefaultMutableTreeNode> iterator = selectedNodes.iterator(); iterator.hasNext();) {
             final DefaultMutableTreeNode next = iterator.next();
             if (next != null) {
                 treePaths.add(new TreePath(next.getPath()));
             }
         }
 
         if (treePaths.size() > 0) {
             this.setSelectionPaths((TreePath[])treePaths.toArray(new TreePath[treePaths.size()]));
         } else if (LOG.isDebugEnabled()) {
             LOG.warn("setSelectedNodes(): collections of nodes is empty"); // NOI18N
         }
         // vor Messe
         if (selectedNodes.isEmpty()) {
             this.removeSelectionPaths(getSelectionPaths());
         }
     }
 
     @Override
     public void autoscroll(final Point p) {
         int realrow = getRowForLocation(p.x, p.y);
         final Rectangle outer = getBounds();
         realrow = (((p.y + outer.y) <= margin) ? ((realrow < 1) ? 0 : (realrow - 1))
                                                : ((realrow < (getRowCount() - 1)) ? (realrow + 1) : realrow));
         scrollRowToVisible(realrow);
     }
 
     @Override
     public Insets getAutoscrollInsets() {
         final Rectangle outer = getBounds();
         final Rectangle inner = getParent().getBounds();
         return new Insets(inner.y - outer.y + margin,
                 inner.x
                         - outer.x
                         + margin,
                 outer.height
                         - inner.height
                         - inner.y
                         + outer.y
                         + margin,
                 outer.width
                         - inner.width
                         - inner.x
                         + outer.x
                         + margin);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public DefaultMetaTreeNode[] getSelectedNodesArray() {
         final Collection selectedNodes = this.getSelectedNodes();
         return (DefaultMetaTreeNode[])selectedNodes.toArray(new DefaultMetaTreeNode[selectedNodes.size()]);
     }
 
     @Override
     public void addStatusChangeListener(final StatusChangeListener listener) {
         this.statusChangeSupport.addStatusChangeListener(listener);
     }
 
     @Override
     public void removeStatusChangeListener(final StatusChangeListener listener) {
         this.statusChangeSupport.removeStatusChangeListener(listener);
     }
 
     @Override
     public boolean isPathEditable(final TreePath treePath) {
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public BufferedImage getDragImage() {
         return dragImage;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  dragImage  DOCUMENT ME!
      */
     public void setDragImage(final BufferedImage dragImage) {
         this.dragImage = dragImage;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   node  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IllegalArgumentException  DOCUMENT ME!
      */
     public static DefaultMetaTreeNode createTreeNode(final Node node) {
         if (node instanceof MetaObjectNode) {
             return new ObjectTreeNode((MetaObjectNode)node);
         } else if (node instanceof MetaNode) {
             return new PureTreeNode((MetaNode)node);
         } else if (node instanceof MetaClassNode) {
             return new ClassTreeNode((MetaClassNode)node);
         } else {
             throw new IllegalArgumentException("unknown node type: " + node); // NOI18N
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class RefreshWorker implements Runnable {
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient DefaultMetaTreeNode node;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new RefreshWorker object.
          *
          * @param  node  DOCUMENT ME!
          */
         public RefreshWorker(final DefaultMetaTreeNode node) {
             this.node = node;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void run() {
             synchronized (node) {
                 final Node thisNode = node.getNode();
 
                 assert thisNode != null : "DefaultMetaTreeNode without backing node: " + node; // NOI18N
 
                if (thisNode.isSqlSort() && (thisNode.getDynamicChildrenStatement() != null)) {
                     if (LOG.isInfoEnabled()) {
                         LOG.info("these children are sorted via SQL, thus soft refresh is not possible: " + thisNode); // NOI18N
                     }
 
                     node.refreshChildren();
                     EventQueue.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 defaultTreeModel.reload(node);
                             }
                         });
                 } else {
                     try {
                         final Enumeration<DefaultMetaTreeNode> currentChildren = node.children();
                         final Node[] dbChildren = node.getChildren();
 
                         final List<Node> foundDbNodes = new ArrayList<Node>(dbChildren.length);
 
                         while (currentChildren.hasMoreElements()) {
                             final DefaultMetaTreeNode treeNode = currentChildren.nextElement();
                             final Node currentNode = treeNode.getNode();
 
                             assert currentNode != null : "found DefaultMetaTreeNode without backing Node"; // NOI18N
 
                             boolean found = false;
                             for (final Node dbNode : dbChildren) {
                                 if (currentNode.deepEquals(dbNode)) {
                                     foundDbNodes.add(dbNode);
                                     found = true;
 
                                     break;
                                 }
                             }
 
                             if (!found) {
                                 scheduleRemoval(treeNode);
                             }
                         }
 
                         for (final Node dbNode : dbChildren) {
                             if (!foundDbNodes.contains(dbNode)) {
                                 scheduleAddition(createTreeNode(dbNode));
                             }
                         }
                     } catch (final Exception e) {
                         LOG.error("cannot refresh node: " + node, e); // NOI18N
                     }
                 }
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   toRemove  DOCUMENT ME!
          *
          * @throws  IllegalStateException  DOCUMENT ME!
          */
         private void scheduleRemoval(final DefaultMetaTreeNode toRemove) {
             EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         final int index = node.removeNode(toRemove);
 
                         if (index == -1) {
                             throw new IllegalStateException(
                                 "trying to remove a node that is not present: [node=" // NOI18N
                                         + node
                                         + "|removalCandidate="                        // NOI18N
                                         + toRemove
                                         + "]");                                       // NOI18N
                         }
                         defaultTreeModel.nodesWereRemoved(node, new int[] { index }, new Object[] { toRemove });
                         defaultTreeModel.nodeStructureChanged(node);
                     }
                 });
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   toAdd  DOCUMENT ME!
          *
          * @throws  IllegalStateException  DOCUMENT ME!
          */
         private void scheduleAddition(final DefaultMetaTreeNode toAdd) {
             EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         final int index = node.insertNode(toAdd);
 
                         if (index == -1) {
                             throw new IllegalStateException(
                                 "trying to add a node failed: [node=" // NOI18N
                                         + node
                                         + "|additionCandidate="       // NOI18N
                                         + toAdd
                                         + "]");                       // NOI18N
                         }
                         defaultTreeModel.nodesWereInserted(node, new int[] { index });
                         defaultTreeModel.nodeStructureChanged(node);
                     }
                 });
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class MetaCatalogueSelectionListener implements TreeSelectionListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void valueChanged(final TreeSelectionEvent e) {
             statusChangeSupport.fireStatusChange(
                 org.openide.util.NbBundle.getMessage(
                     MetaCatalogueTree.class,
                     "MetaCatalogueTree.valueChanged().objectsSelected",
                     new Object[] { MetaCatalogueTree.this.getSelectedNodeCount() }), // NOI18N
                 Status.MESSAGE_POSITION_1);
         }
     }
 
     /**
      * Dieser innere Klasse sorgt dafuer, dass an den Knoten der expandiert wurde seine Children vom Server (oder aus
      * dem Cache) geladen und angehaengt werden indem die <b>explore()</b> des Knotens ausgefuehrt wird. Fuer einen
      * Knoten der bereits expandiert wurde (d.h. dessen Children bereits vom Server geladen wurden) muss die Funktion
      * <b>explore()</b> nicht mehr ausgefuehrt werden.
      *
      * <p> <b>Wenn der DefaultMetaTree Multithreading benutzen soll, wird an den expandierten Knoten eine <b>
      * DefaultMetaTreeNode</b> vom Typ <b>WaitNode</b> aengehaengt. Anschliessend wird <b>nodeStructureChanged(node)</b>
      * aufgerufen, um diesen Knoten anzuzeigen.<br>
      * Die <b>WaitNode</b> wird zwar sofort innerhalb der <b>explore</b> Funktion der selektierten <b>
      * DefaultMetaTreeNode</b> wieder entfernt (<b>removeChildren</b>) aber da der <b>TreeExploreThread</b> das GUI
      * Update asynchron zum <b>EventDispatchThread</b> ausfuehrt, verschwindet die <b>WaitNode</b> im TreeView erst,
      * wenn alle Children vom Server geladen wurden.<br>
      * </b></p>
      *
      * <p>Wenn keine Threads benutzt werden bzw. die maximale Anzahl kokurrierender Threads erreicht wurde, wird <b>
      * explore()</b> sofort ausgefuehrt und das GUI bleibt blockiert, bis alle Children geladen und visualisiert wurden.
      * </p>
      *
      * @version  $Revision$, $Date$
      */
     private class MetaCatalogueExpansionListener implements TreeExpansionListener {
 
         //~ Instance fields ----------------------------------------------------
 
         final WaitTreeNode waitNode = new WaitTreeNode();
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void treeCollapsed(final TreeExpansionEvent e) {
         }
 
         @Override
         public void treeExpanded(final TreeExpansionEvent e) {
             final DefaultMetaTreeNode selectedNode = (DefaultMetaTreeNode)(e.getPath().getLastPathComponent());
             if (LOG.isDebugEnabled()) {
                 LOG.debug("treeExpanded() Expanding Node: " + selectedNode.toString()); // NOI18N
             }
 
             if (!selectedNode.isLeaf() && !selectedNode.isExplored())                                // && selectedNode.getChildCount() == 0)
             {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("treeExpanded() Expanding Node: " + selectedNode.toString() + " ok."); // NOI18N
                 }
                 if (useThread) {
                     new SwingWorker<Void, Void>() {
 
                             @Override
                             protected Void doInBackground() throws Exception {
                                 selectedNode.add(waitNode);
 
                                 return null;
                             }
 
                             @Override
                             protected void done() {
                                 defaultTreeModel.nodeStructureChanged(selectedNode);
                                 treePool.execute(new TreeExploreThread(selectedNode, defaultTreeModel));
                             }
                         }.execute();
                 } else {
                     synchronized (selectedNode) {
                         try {
                             statusChangeSupport.fireStatusChange(
                                 org.openide.util.NbBundle.getMessage(
                                     MetaCatalogueTree.class,
                                     "MetaCatalogueTree.treeExpanded().loadingObjects"), // NOI18N
                                 Status.MESSAGE_POSITION_1,
                                 Status.ICON_IGNORE,
                                 Status.ICON_BLINKING);
                             selectedNode.explore();
                             defaultTreeModel.nodeStructureChanged(selectedNode);
                             statusChangeSupport.fireStatusChange(
                                 org.openide.util.NbBundle.getMessage(
                                     MetaCatalogueTree.class,
                                     "MetaCatalogueTree.treeExpanded().dataLoadedFromServer"), // NOI18N
                                 Status.MESSAGE_POSITION_1,
                                 Status.ICON_ACTIVATED,
                                 Status.ICON_DEACTIVATED);
                         } catch (Exception exp) {
                             statusChangeSupport.fireStatusChange(
                                 org.openide.util.NbBundle.getMessage(
                                     MetaCatalogueTree.class,
                                     "MetaCatalogueTree.treeExpanded().loadingError"), // NOI18N
                                 Status.MESSAGE_POSITION_1,
                                 Status.ICON_DEACTIVATED,
                                 Status.ICON_ACTIVATED);
                             LOG.fatal("treeExpanded() could not load nodes", exp); // NOI18N
                             selectedNode.removeChildren();
                         }
                     }
                 }
             } else {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("treeExpanded() " + selectedNode.getNode() + "'s children loaded from cache"); // NOI18N
                 }
                 statusChangeSupport.fireStatusChange(
                     org.openide.util.NbBundle.getMessage(
                         MetaCatalogueTree.class,
                         "MetaCatalogueTree.treeExpanded().dataLoadedFromCache"), // NOI18N
                     Status.MESSAGE_POSITION_1,
                     Status.ICON_ACTIVATED,
                     Status.ICON_DEACTIVATED);
             }
         }
     }
 
     /**
      * Der <b>TreeExploreThread</b> sorgt dafuer, dass die TreeNodes im Hintergrund geladen werden. Innerhalb des
      * Threads wird die <b>explore()</b> Funktion der selektierten DefaultMetaTreeNode ausgefuehrt. Waehrend des
      * Ladevorgangs der Daten vom Server wird der TreeView nicht aktualisiert, da ansonsten das GUI w\u00E4hrend dieser
      * Zeit komplett blockiert wuerde (kein repaint() meher!). Deshalb geschieht die eigentlich Aktualisierung des Views
      * asynchron zum <b>EventDispatchThread</b>.<br>
      * Das bedeutet, dass das GUI nicht innerhalb des EventDispatchThreads (in diesem Fall der <b>
      * TreeExpansionEvent</b>) oder aus einem aus ihm heraus gestarteten Thread (z.B. der TreeExploreThread)
      * aktuakisiert wird. Die Aktualisierung erfolgt durch <b>nodeStructureChanged(node)</b>. Diese Funktion wird in
      * einem weiteren Thread innerhalb des TreeExploreThreads ausgefuehrt, der durch <b>
      * SwingUtilities.invokeLater(runnable)</b> erst gestartet wird, wenn der <b>TreeExpansionEvent</b> bzw. der <b>
      * TreeExploreThread</b> beendet
      *
      * @version  $Revision$, $Date$
      */
     private class TreeExploreThread extends Thread {
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient Runnable treeSwingUpdater;
         private final transient DefaultMetaTreeNode node;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new TreeExploreThread object.
          *
          * @param  selectedNode      DOCUMENT ME!
          * @param  defaultTreeModel  DOCUMENT ME!
          */
         public TreeExploreThread(final DefaultMetaTreeNode selectedNode, final DefaultTreeModel defaultTreeModel) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("<THREAD>: TreeExploreThread"); // NOI18N
             }
             node = selectedNode;
 
             treeSwingUpdater = new Runnable() {
 
                     @Override
                     public void run() {
                         MetaCatalogueTree.this.defaultTreeModel.nodeStructureChanged(node);
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("<THREAD>: TreeExploreThread GUI update done"); // NOI18N
                         }
                     }
                 };
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void run() {
             synchronized (node) {
                 try {
                     statusChangeSupport.fireStatusChange(
                         org.openide.util.NbBundle.getMessage(
                             MetaCatalogueTree.class,
                             "MetaCatalogueTree.TreeExploreThread.loadingObjects"), // NOI18N
                         Status.MESSAGE_POSITION_1,
                         Status.ICON_BLINKING,
                         Status.ICON_IGNORE);
 
                     node.explore();
 
                     statusChangeSupport.fireStatusChange(
                         org.openide.util.NbBundle.getMessage(
                             MetaCatalogueTree.class,
                             "MetaCatalogueTree.TreeExploreThread.dataLoadedFromServer"), // NOI18N
                         Status.MESSAGE_POSITION_1,
                         Status.ICON_ACTIVATED,
                         Status.ICON_DEACTIVATED);
                 } catch (final Exception exp) {
                     LOG.error("could not load nodes", exp); // NOI18N
                     statusChangeSupport.fireStatusChange(
                         org.openide.util.NbBundle.getMessage(
                             MetaCatalogueTree.class,
                             "MetaCatalogueTree.TreeExploreThread.loadingError"), // NOI18N
                         Status.MESSAGE_POSITION_1,
                         Status.ICON_DEACTIVATED,
                         Status.ICON_ACTIVATED);
 
                     node.removeChildren();
                 } finally {
                     SwingUtilities.invokeLater(treeSwingUpdater);
                 }
             }
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("<THREAD> TreeExploreThread done"); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class SubTreeExploreThread extends Thread {
 
         //~ Instance fields ----------------------------------------------------
 
         private final DefaultMetaTreeNode node;
         private final Iterator<DefaultMetaTreeNode> childrenNodes;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new SubTreeExploreThread object.
          *
          * @param  rootNode       DOCUMENT ME!
          * @param  childrenNodes  DOCUMENT ME!
          */
         public SubTreeExploreThread(final DefaultMetaTreeNode rootNode,
                 final Iterator<DefaultMetaTreeNode> childrenNodes) {
             this.node = rootNode;
             this.childrenNodes = childrenNodes;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void run() {
             synchronized (node) {
                 try {
                     statusChangeSupport.fireStatusChange(
                         org.openide.util.NbBundle.getMessage(
                             MetaCatalogueTree.class,
                             "MetaCatalogueTree.SubTreeExploreThread.loadingObjects"), // NOI18N
                         Status.MESSAGE_POSITION_1,
                         Status.ICON_BLINKING,
                         Status.ICON_IGNORE);
 
                     final TreePath selectionPath = this.node.explore(this.childrenNodes);
 
                     SwingUtilities.invokeLater(new TreeUpdateThread(selectionPath));
 
                     statusChangeSupport.fireStatusChange(
                         org.openide.util.NbBundle.getMessage(
                             MetaCatalogueTree.class,
                             "MetaCatalogueTree.SubTreeExploreThread.dataLoadedFromServer"), // NOI18N
                         Status.MESSAGE_POSITION_1,
                         Status.ICON_ACTIVATED,
                         Status.ICON_DEACTIVATED);
                 } catch (final Exception exp) {
                     LOG.error("SubTreeExploreThread: could not load nodes", exp); // NOI18N
                     statusChangeSupport.fireStatusChange(
                         org.openide.util.NbBundle.getMessage(
                             MetaCatalogueTree.class,
                             "MetaCatalogueTree.SubTreeExploreThread.loadingError"), // NOI18N
                         Status.MESSAGE_POSITION_1,
                         Status.ICON_DEACTIVATED,
                         Status.ICON_ACTIVATED);
 
                     node.removeChildren();
 
                     SwingUtilities.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 MetaCatalogueTree.this.defaultTreeModel.nodeStructureChanged(node);
                             }
                         });
                 }
             }
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("SubTreeExploreThread: done"); // NOI18N
             }
         }
 
         //~ Inner Classes ------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @version  $Revision$, $Date$
          */
         private class TreeUpdateThread implements Runnable {
 
             //~ Instance fields ------------------------------------------------
 
             private final TreePath selectionPath;
 
             //~ Constructors ---------------------------------------------------
 
             /**
              * Creates a new TreeUpdateThread object.
              *
              * @param  selectionPath  DOCUMENT ME!
              */
             private TreeUpdateThread(final TreePath selectionPath) {
                 this.selectionPath = selectionPath;
             }
 
             //~ Methods --------------------------------------------------------
 
             @Override
             public void run() {
                 if (!EventQueue.isDispatchThread()) {
                     throw new IllegalStateException("Tree Update Thread can only be scheduled in EDT"); // NOI18N
                 }
 
                 expandPath(selectionPath);
                 setSelectionPath(this.selectionPath);
                 scrollPathToVisible(selectionPath);
                 defaultTreeModel.nodeStructureChanged((TreeNode)selectionPath.getLastPathComponent());
 
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("GUI Update done"); // NOI18N
                 }
             }
         }
     }
 }
