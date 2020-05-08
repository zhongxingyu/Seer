 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.ui.tree;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.plugin.PluginRegistry;
 import Sirius.navigator.plugin.interfaces.PluginSupport;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.types.treenode.RootTreeNode;
 import Sirius.navigator.types.treenode.WaitTreeNode;
 
 import Sirius.server.middleware.types.MetaNode;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 
 import org.apache.log4j.Logger;
 
 import java.awt.EventQueue;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.SwingWorker;
 import javax.swing.tree.DefaultTreeModel;
 
 import de.cismet.cids.navigator.utils.DirectedMetaObjectNodeComparator;
 import de.cismet.cids.navigator.utils.MetaTreeNodeVisualization;
 
 import de.cismet.tools.CismetThreadPool;
 
 /**
  * Der SearchTree dient zum Anzeigen von Suchergebnissen. Neben der Funktionalit\u00E4t, die er von GenericMetaTree
  * erbt, bietet er zusaetzlich noch die Moeglichkeit, die Suchergebnisse schrittweise anzuzeigen. D.h. es wird immer nur
  * ein kleiner Ausschnitt fester Groesse aus der gesamten Ergebissmenge angezeigt. Um durch die Ergebnissmenge zu
  * navigieren stellt der SearchTree spezielle Methoden bereit.
  *
  * @version  $Revision$, $Date$
  */
 public class SearchResultsTree extends MetaCatalogueTree {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(SearchResultsTree.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private boolean empty = true;
     private Node[] resultNodes = null;
     private final RootTreeNode rootNode;
     private Thread runningNameLoader = null;
     private SwingWorker<Void, Void> refreshWorker;
     private boolean syncWithMap = false;
     private boolean ascending = true;
     private final WaitTreeNode waitTreeNode = new WaitTreeNode();
     private MouseAdapter cancelRefreshingListener;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Erzeugt einen neuen, leeren, SearchTree. Es werden jeweils 50 Objekte angezeigt.
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public SearchResultsTree() throws Exception {
         this(true, 2);
     }
 
     /**
      * Creates a new SearchResultsTree object.
      *
      * @param   useThread       DOCUMENT ME!
      * @param   maxThreadCount  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public SearchResultsTree(final boolean useThread, final int maxThreadCount) throws Exception {
         super(new RootTreeNode(), false, useThread, maxThreadCount);
         this.rootNode = (RootTreeNode)this.defaultTreeModel.getRoot();
         defaultTreeModel.setAsksAllowsChildren(true);
         this.defaultTreeModel.setAsksAllowsChildren(true);
         cancelRefreshingListener = new CancelRefreshingListener();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Setzt die ResultNodes fuer den Suchbaum, d.h. die Ergebnisse der Suche.
      *
      * @param  nodes  Ergebnisse, die im SearchTree angezeigt werden sollen.
      */
     public void setResultNodes(final Node[] nodes) {
         if (LOG.isInfoEnabled()) {
             LOG.info("[SearchResultsTree] filling tree with '" + nodes.length + "' nodes"); // NOI18N
         }
 
         if ((nodes == null) || (nodes.length < 1)) {
             empty = true;
             resultNodes = new Node[0];
         } else {
             empty = false;
         }
 
         if (resultNodes.length > 0) {
             refreshTree(true);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void syncWithMap() {
         syncWithMap(isSyncWithMap());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  sync  DOCUMENT ME!
      */
     public void syncWithMap(final boolean sync) {
         if (sync) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("syncWithMap");                                                   // NOI18N
             }
             try {
                 final PluginSupport map = PluginRegistry.getRegistry().getPlugin("cismap"); // NOI18N
                 final List<DefaultMetaTreeNode> v = new ArrayList<DefaultMetaTreeNode>();
                 final DefaultTreeModel dtm = (DefaultTreeModel)getModel();
 
                 for (int i = 0; i < ((DefaultMetaTreeNode)dtm.getRoot()).getChildCount(); ++i) {
                     if (resultNodes[i] instanceof MetaObjectNode) {
                         final DefaultMetaTreeNode otn = (DefaultMetaTreeNode)((DefaultMetaTreeNode)dtm.getRoot())
                                     .getChildAt(i);
                         v.add(otn);
                     }
                 }
 
                 MetaTreeNodeVisualization.getInstance().addVisualization(v);
             } catch (Throwable t) {
                 LOG.warn("Fehler beim synchronisieren der Suchergebnisse mit der Karte", t); // NOI18N
             }
         }
     }
 
     /**
      * Setzt die ResultNodes fuer den Suchbaum, d.h. die Ergebnisse der Suche.<br>
      * Diese Ergebnisse koennen an eine bereits vorhandene Ergebnissmenge angehaengt werden
      *
      * @param  nodes   Ergebnisse, die im SearchTree angezeigt werden sollen.
      * @param  append  Ergebnisse anhaengen.
      */
     public void setResultNodes(final Node[] nodes, final boolean append) {
         if (LOG.isInfoEnabled()) {
             LOG.info("[SearchResultsTree] " + (append ? "appending" : "setting") + " '" + nodes.length + "' nodes"); // NOI18N
         }
 
         if ((append == true) && ((nodes == null) || (nodes.length < 1))) {
             return;
         } else if ((append == false) && ((nodes == null) || (nodes.length < 1))) {
             this.clear();
             return;
         } else if ((append == true) && (empty == false)) {
             final Node[] tmpNodes = new Node[resultNodes.length + nodes.length];
             int j = resultNodes.length;
 
             System.arraycopy(resultNodes, 0, tmpNodes, 0, resultNodes.length);
 
             this.clear();
 
             for (int i = 0; i < nodes.length; i++) {
                 tmpNodes[j] = nodes[i];
                 j++;
             }
             resultNodes = tmpNodes;
         } else {
             this.clear();
             resultNodes = nodes;
         }
 
         empty = false;
         refreshTree(true);
 
         if (!getModel().getRoot().equals(rootNode)) {
             ((DefaultTreeModel)getModel()).setRoot(rootNode);
             ((DefaultTreeModel)getModel()).reload();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  initialFill  sort DOCUMENT ME!
      */
     private void refreshTree(final boolean initialFill) {
         if ((refreshWorker != null) && !refreshWorker.isDone()) {
             LOG.warn("Refreshing search result tree is triggered while another refresh process is still not done.");
             refreshWorker.cancel(true);
         }
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     rootNode.removeAllChildren();
                     rootNode.add(waitTreeNode);
                     addMouseListener(cancelRefreshingListener);
                     defaultTreeModel.nodeStructureChanged(rootNode);
                     refreshWorker = new RefreshTreeWorker(initialFill);
                     CismetThreadPool.execute(refreshWorker);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      */
     private void checkForDynamicNodes() {
         final DefaultTreeModel dtm = (DefaultTreeModel)getModel();
         final DefaultMetaTreeNode node = (DefaultMetaTreeNode)dtm.getRoot();
         if (runningNameLoader != null) {
             runningNameLoader.interrupt();
         }
 
         final Thread t = new Thread() {
 
                 @Override
                 public void run() {
                     runningNameLoader = this;
                     for (int i = 0; i < dtm.getChildCount(node); ++i) {
                         if (interrupted()) {
                             break;
                         }
                         try {
                             final DefaultMetaTreeNode n = (DefaultMetaTreeNode)dtm.getChild(node, i);
 
                             if ((n != null) && (n.getNode().getName() == null) && n.isObjectNode()) {
                                 try {
                                     final ObjectTreeNode on = ((ObjectTreeNode)n);
                                     EventQueue.invokeLater(new Runnable() {
 
                                             @Override
                                             public void run() {
                                                 n.getNode()
                                                         .setName(
                                                             org.openide.util.NbBundle.getMessage(
                                                                 SearchResultsTree.class,
                                                                 "SearchResultsTree.checkForDynamicNodes().loadName")); // NOI18N
                                                 dtm.nodeChanged(on);
                                             }
                                         });
                                     if (LOG.isDebugEnabled()) {
                                         LOG.debug("caching object node");                                              // NOI18N
                                     }
                                     final MetaObject MetaObject = SessionManager.getProxy()
                                                 .getMetaObject(on.getMetaObjectNode().getObjectId(),
                                                     on.getMetaObjectNode().getClassId(),
                                                     on.getMetaObjectNode().getDomain());
                                     on.getMetaObjectNode().setObject(MetaObject);
                                     EventQueue.invokeLater(new Runnable() {
 
                                             @Override
                                             public void run() {
                                                 n.getNode().setName(MetaObject.toString());
                                                 dtm.nodeChanged(on);
                                             }
                                         });
                                 } catch (final Exception t) {
                                     LOG.error("could not retrieve meta object of node '" + this + "'", t); // NOI18N
                                 }
                             } else {
                                 if (LOG.isDebugEnabled()) {
                                     LOG.debug("n.getNode().getName()!=null: " + n.getNode().getName() + ":"); // NOI18N
                                 }
                             }
                         } catch (final Exception e) {
                             LOG.error("Error while loading the name", e);                                  // NOI18N
                         }
                     }
                     runningNameLoader = null;
                 }
             };
 
         CismetThreadPool.execute(t);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Node[] getResultNodes() {
         return resultNodes;
     }
 
     /**
      * Diese Funktion dient dazu, eine Selektion von Knoten aus dem SearchTree zu loeschen.
      *
      * @param   selectedNodes  Die Knoten, die geloescht werden sollen.
      *
      * @return  true, wenn mindestens ein Knoten geloescht wurde.
      */
     public boolean removeResultNodes(final DefaultMetaTreeNode[] selectedNodes) {
         if (LOG.isInfoEnabled()) {
             LOG.info("[SearchResultsTree] removing '" + selectedNodes + "' nodes"); // NOI18N
         }
         boolean deleted = false;
 
         if ((selectedNodes == null) || (selectedNodes.length < 1)) {
             return deleted;
         }
 
         final List tmpNodeVector = new ArrayList();
         tmpNodeVector.addAll(Arrays.asList(resultNodes));
 
         for (int i = 0; i < tmpNodeVector.size(); i++) {
             for (int j = 0; j < selectedNodes.length;) {
                 if ((i < tmpNodeVector.size()) && selectedNodes[j].equalsNode((Node)tmpNodeVector.get(i))) {
                     tmpNodeVector.remove(i);
                     deleted = true;
                 } else {
                     ++j;
                 }
             }
         }
 
         if (deleted) {
             this.setResultNodes((Node[])tmpNodeVector.toArray(new Node[tmpNodeVector.size()]), false);
         }
 
         return deleted;
     }
 
     /**
      * DOCUMENT ME!
      */
     public void removeSelectedResultNodes() {
         final Collection selectedNodes = this.getSelectedNodes();
         if (selectedNodes != null) {
             this.removeResultNodes(selectedNodes);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   selectedNodes  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean removeResultNodes(final Collection selectedNodes) {
         if (LOG.isInfoEnabled()) {
             LOG.info("[SearchResultsTree] removing '" + selectedNodes + "' nodes"); // NOI18N
         }
         boolean deleted = false;
         try {
             if ((selectedNodes != null) && (selectedNodes.size() > 0)) {
                 final ArrayList all = new ArrayList(Arrays.asList(resultNodes));
                 final ArrayList allWorkingCopy = new ArrayList(Arrays.asList(resultNodes));
                 final ArrayList selectionTreeNodes = new ArrayList(selectedNodes);
                 final ArrayList selection = new ArrayList();
                 for (final Object selO : selectionTreeNodes) {
                     if (selO instanceof DefaultMetaTreeNode) {
                         final Node n = ((DefaultMetaTreeNode)selO).getNode();
                         selection.add(n);
                     }
                 }
 
                 for (final Object allO : all) {
                     for (final Object selO : selection) {
                         if ((allO instanceof MetaObjectNode) && (selO instanceof MetaObjectNode)) {
                             final MetaObjectNode allMON = (MetaObjectNode)allO;
                             final MetaObjectNode selMON = (MetaObjectNode)selO;
                             if ((allMON.getObjectId() == selMON.getObjectId())
                                         && (allMON.getClassId() == selMON.getClassId())
                                         && (allMON.getId() == selMON.getId())
                                         && allMON.getDomain().equals(selMON.getDomain())
                                         && allMON.toString().equals(selMON.toString())) {
                                 allWorkingCopy.remove(allO);
                                 deleted = true;
                             }
                         } else if ((allO instanceof MetaNode) && (selO instanceof MetaNode)) {
                             final MetaNode allMN = (MetaNode)allO;
                             final MetaNode selMN = (MetaNode)selO;
                             if ((allMN.getId() == selMN.getId())
                                         && allMN.getDomain().equals(selMN.getDomain())
                                         && allMN.toString().equals(selMN.toString())) {
                                 allWorkingCopy.remove(allO);
                                 deleted = true;
                             }
                         }
                     }
                 }
 
                 this.setResultNodes((Node[])allWorkingCopy.toArray(new Node[allWorkingCopy.size()]), false);
             }
         } catch (Exception e) {
             LOG.error("Fehler beim Entfernen eines Objektes aus den Suchergebnissen", e);
         }
         return deleted;
     }
 
     /**
      * Setzt den SearchTree komplett zurueck und entfernt alle Knoten.
      */
     public void clear() {
         LOG.info("[SearchResultsTree] removing all nodes"); // NOI18N
         resultNodes = null;
         empty = true;
         rootNode.removeAllChildren();
         firePropertyChange("browse", 0, 1);                 // NOI18N
         defaultTreeModel.nodeStructureChanged(rootNode);
         System.gc();
     }
 
     /**
      * Returns a flag indicating whether the SearchResultsTree is empty or not.
      *
      * @return  Flag indicating whether tree is empty or not.
      */
     public boolean isEmpty() {
         return this.empty;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isSyncWithMap() {
         return syncWithMap;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  syncWithMap  DOCUMENT ME!
      */
     public void setSyncWithMap(final boolean syncWithMap) {
         this.syncWithMap = syncWithMap;
     }
 
     /**
      * Changes the sort order to ascending or descending according to the given parameter.
      *
      * @param  ascending  Whether to sort ascending (<code>true</code>) or descending (<code>false</code>).
      */
     public void sort(final boolean ascending) {
         this.ascending = ascending;
         refreshTree(false);
     }
 
     /**
      * DOCUMENT ME!
      */
     public void cancelNodeLoading() {
         if ((refreshWorker != null) && !refreshWorker.isDone()) {
             refreshWorker.cancel(true);
             rootNode.removeAllChildren();
             defaultTreeModel.nodeStructureChanged(rootNode);
         }
     }
 
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public SwingWorker getNodeLoadingWorker() {
        return refreshWorker;
    }

     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * A SwingWorker which encapsulates sorting the results and refreshing the tree. This worker is needed since it
      * could be necessary to load every object during the first sort process on a certain result set.
      *
      * @version  $Revision$, $Date$
      */
     private class RefreshTreeWorker extends SwingWorker<Void, Void> {
 
         //~ Instance fields ----------------------------------------------------
 
         private boolean initialFill = false;
         private DirectedMetaObjectNodeComparator comparator;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new RefreshTreeWorker object.
          *
          * @param  initialFill  A flag indicating whether to sort the result set or not.
          */
         public RefreshTreeWorker(final boolean initialFill) {
             this.initialFill = initialFill;
             comparator = new DirectedMetaObjectNodeComparator(ascending);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected Void doInBackground() throws Exception {
             if (!isCancelled()) {
                 Arrays.sort(resultNodes, comparator);
             }
 
             return null;
         }
 
         @Override
         protected void done() {
             SearchResultsTree.this.removeMouseListener(cancelRefreshingListener);
 
             if (isCancelled()) {
                 comparator.cancel();
             }
 
             try {
                 if (!isCancelled()) {
                     get();
                 }
             } catch (InterruptedException ex) {
                 LOG.error("Error occured while refreshing search results tree", ex);
             } catch (ExecutionException ex) {
                 LOG.error("Error occured while refreshing search results tree", ex);
             }
 
             if (!isCancelled()) {
                 rootNode.removeAllChildren();
                 rootNode.addChildren(resultNodes);
             }
 
             SearchResultsTree.this.firePropertyChange("browse", 0, 1); // NOI18N
             defaultTreeModel.nodeStructureChanged(rootNode);
 
             if (initialFill) {
                 syncWithMap();
                 checkForDynamicNodes();
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class CancelRefreshingListener extends MouseAdapter {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void mousePressed(final MouseEvent e) {
             if ((e.getButton() != MouseEvent.BUTTON1) || (e.getClickCount() != 2)) {
                 return;
             }
             cancelNodeLoading();
         }
     }
 }
