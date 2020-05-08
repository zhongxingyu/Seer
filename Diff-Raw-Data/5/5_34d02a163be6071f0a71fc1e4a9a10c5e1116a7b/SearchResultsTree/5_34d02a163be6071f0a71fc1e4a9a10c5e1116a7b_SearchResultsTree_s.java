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
 
 import Sirius.server.middleware.types.MetaNode;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 
 import java.awt.EventQueue;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.tree.DefaultTreeModel;
 
 import de.cismet.cids.navigator.utils.MetaObjectNodeComparator;
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
 
     //~ Instance fields --------------------------------------------------------
 
     private boolean empty = true;
     private boolean browseBack = false;
     private boolean browseForward = false;
     private Node[] resultNodes = null;
     private Node[] visibleResultNodes = null;
     private final RootTreeNode rootNode;
     private final int visibleNodes;
     // Position des LETZTEN Elements
     private int pos = 0;
     private int max = 0;
     private int rest = 0;
     private Thread runningNameLoader = null;
     private boolean syncWithMap = false;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Erzeugt einen neuen, leeren, SearchTree. Es werden jeweils 50 Objekte angezeigt.
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public SearchResultsTree() throws Exception {
         this(50, true, 2);
     }
 
     /**
      * Creates a new SearchResultsTree object.
      *
      * @param   visibleNodes    DOCUMENT ME!
      * @param   useThread       DOCUMENT ME!
      * @param   maxThreadCount  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public SearchResultsTree(final int visibleNodes, final boolean useThread, final int maxThreadCount)
             throws Exception {
         super(new RootTreeNode(), false, useThread, maxThreadCount);
         this.rootNode = (RootTreeNode)this.defaultTreeModel.getRoot();
         this.visibleNodes = visibleNodes;
         defaultTreeModel.setAsksAllowsChildren(true);
         this.defaultTreeModel.setAsksAllowsChildren(true);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Blaettert in der Ergebnissmenge einen Schritt vor. Loest einen PropertyChange Event ("browse") aus.
      *
      * @return  true, bis das Ende der Ergebnissmenge erreicht wurde.
      */
     public boolean browseForward() {
         logger.info("[SearchResultsTree] browsing forward"); // NOI18N
         if (resultNodes != null) {
             boolean full = true;
 
             if ((pos + 1) > max) {
                 browseForward = false;
                 browseBack = true;
             } else if (visibleNodes >= max) {
                 browseBack = false;
                 browseForward = false;
 
                 visibleResultNodes = new Node[max];
                 visibleResultNodes = resultNodes;
             } else if ((pos + visibleNodes) < max) {
                 int j = 0;
                 for (int i = pos; i < (pos + visibleNodes); i++) {
                     visibleResultNodes[j] = resultNodes[i];
                     j++;
                 }
                 pos += visibleNodes;
                 full = false;
 
                 if (pos > visibleNodes) {
                     browseBack = true;
                 } else {
                     browseBack = false;
                 }
                 browseForward = true;
             } else {
                 browseForward = false;
                 browseBack = true;
 
                 rest = max - pos;
                 visibleResultNodes = new Node[rest];
                 int j = 0;
                 for (int i = pos; i < max; i++) {
                     visibleResultNodes[j] = resultNodes[i];
                     j++;
                 }
                 pos = max;
                 full = true;
             }
 
             rootNode.removeChildren();
 
             try {
                 rootNode.addChildren(visibleResultNodes);
             } catch (Exception exp) {
                logger.fatal("[SearchResultsTree] could not browse forward", exp); // NOI18N
             }
 
             firePropertyChange("browse", 0, 1); // NOI18N
             defaultTreeModel.nodeStructureChanged(rootNode);
             checkForDynamicNodes();
             return full;
         }
 
         System.gc();
         return true;
     }
 
     /**
      * Blaettert in der Ergebnissmenge einen Schritt zurueck. Loest einen PropertyChange Event ("browse") aus.
      *
      * @return  true, bis der Anfang der Ergebnissmenge erreicht wurde.
      */
     public boolean browseBack() {
         logger.info("[SearchResultsTree] browsing back"); // NOI18N
         if (resultNodes != null) {
             boolean full = true;
 
             if (visibleNodes >= max) {
                 browseForward = false;
                 browseBack = true;
                 if (logger.isDebugEnabled()) {
                     logger.debug("browseForward: " + browseForward + " browseBack: " + browseBack); // NOI18N
                     logger.debug("visibleNodes: " + visibleNodes + " >= max: " + max);              // NOI18N
                 }
                 visibleResultNodes = new Node[max];
                 visibleResultNodes = resultNodes;
             } else if ((pos < max) && ((pos - visibleNodes) >= visibleNodes)) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("pos: " + pos + " - visibleNodes: " + visibleNodes + " >= 0");     // NOI18N
                 }
                 pos -= visibleNodes;
                 int j = 0;
                 visibleResultNodes = new Node[visibleNodes];
 
                 for (int i = (pos - visibleNodes); i < pos; i++) {
                     // logger.debug("i: " + i + "j: " + j);
                     visibleResultNodes[j] = resultNodes[i];
                     j++;
                 }
                 full = false;
 
                 if (pos <= visibleNodes) {
                     browseBack = false;
                 } else {
                     browseBack = true;
                 }
                 browseForward = true;
                 if (logger.isDebugEnabled()) {
                     logger.debug("browseForward: " + browseForward + " browseBack: " + browseBack); // NOI18N
                 }
             } else if (pos == max) {
                 browseForward = true;
                 browseBack = true;
                 if (logger.isDebugEnabled()) {
                     logger.debug("browseForward: " + browseForward + " browseBack: " + browseBack); // NOI18N
                     logger.debug("pos: " + pos + " == max" + max);                                  // NOI18N
                 }
                 pos -= rest;
                 int j = 0;
                 visibleResultNodes = new Node[visibleNodes];
 
                 for (int i = (pos - visibleNodes); i < pos; i++) {
                     if (logger.isDebugEnabled()) {
                         logger.debug("i: " + i + "j: " + j); // NOI18N
                     }
                     visibleResultNodes[j] = resultNodes[i];
                     j++;
                 }
                 full = false;
             }
 
             if (pos == visibleNodes) {
                 browseBack = false;
             }
 
             if (logger.isDebugEnabled()) {
                 logger.debug("pos: " + pos + " max: " + max); // NOI18N
             }
             rootNode.removeChildren();
 
             try {
                 rootNode.addChildren(visibleResultNodes);
             } catch (Exception exp) {
                logger.fatal("[SearchResultsTree] could not browse back", exp); // NOI18N
             }
 
             firePropertyChange("browse", 0, 1); // NOI18N
             defaultTreeModel.nodeStructureChanged(rootNode);
             checkForDynamicNodes();
             return full;
         }
         checkForDynamicNodes();
         System.gc();
         return true;
     }
 
     /**
      * Setzt die ResultNodes fuer den Suchbaum, d.h. die Ergebnisse der Suche.
      *
      * @param  nodes  Ergebnisse, die im SearchTree angezeigt werden sollen.
      */
     public void setResultNodes(final Node[] nodes) {
         if (logger.isInfoEnabled()) {
             logger.info("[SearchResultsTree] filling tree with '" + nodes.length + "' nodes"); // NOI18N
         }
         pos = 0;
 
         if ((nodes == null) || (nodes.length < 1)) {
             empty = true;
             browseBack = false;
             browseForward = false;
             firePropertyChange("browse", 0, 1); // NOI18N
         } else {
             resultNodes = nodes;
             max = resultNodes.length;
             visibleResultNodes = new Node[visibleNodes];
             this.browseForward();
             empty = false;
         }
 
         firePropertyChange("browse", 0, 1); // NOI18N
         syncWithMap();
         checkForDynamicNodes();
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
             if (logger.isDebugEnabled()) {
                 logger.debug("syncWithMap");                                                // NOI18N
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
                 logger.warn("Fehler beim synchronisieren der Suchergebnisse mit der Karte", t); // NOI18N
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
         if (logger.isInfoEnabled()) {
             logger.info("[SearchResultsTree] appending '" + nodes.length + "' nodes"); // NOI18N
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
 
         if (resultNodes.length <= visibleNodes) {
             Arrays.sort(resultNodes, new MetaObjectNodeComparator());
         }
         max = resultNodes.length;
         pos = 0;
         visibleResultNodes = new Node[visibleNodes];
         this.browseForward();
         empty = false;
         firePropertyChange("browse", 0, 1); // NOI18N
         syncWithMap();
         checkForDynamicNodes();
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
                     final Thread parentThread = this;
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
                                     if (logger.isDebugEnabled()) {
                                         logger.debug("caching object node");                                           // NOI18N
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
                                     logger.error("could not retrieve meta object of node '" + this + "'", t); // NOI18N
                                 }
                             } else {
                                 if (logger.isDebugEnabled()) {
                                     logger.debug("n.getNode().getName()!=null: " + n.getNode().getName() + ":"); // NOI18N
                                 }
                             }
                         } catch (final Exception e) {
                             logger.error("Error while loading the name", e);                                  // NOI18N
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
         if (logger.isInfoEnabled()) {
             logger.info("[SearchResultsTree] removing '" + selectedNodes + "' nodes"); // NOI18N
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
         if (logger.isInfoEnabled()) {
             logger.info("[SearchResultsTree] removing '" + selectedNodes + "' nodes"); // NOI18N
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
             logger.error("Fehler beim Entfernen eines Objektes aus den Suchergebnissen", e);
         }
         return deleted;
     }
 
     /**
      * Setzt den SearchTree komplett zurueck und entfernt alle Knoten.
      */
     public void clear() {
         logger.info("[SearchResultsTree] removing all nodes"); // NOI18N
         resultNodes = null;
         pos = 0;
         max = 0;
         empty = true;
         browseBack = false;
         browseForward = false;
         rootNode.removeAllChildren();
         firePropertyChange("browse", 0, 1);                    // NOI18N
         defaultTreeModel.nodeStructureChanged(rootNode);
         System.gc();
     }
 
     /**
      * Liefert true, wenn im SearchTree zurueck geblaettert werden kann.
      *
      * @return  true/false
      */
     public boolean isBrowseBack() {
         return this.browseBack;
     }
 
     /**
      * Liefert true, wenn im SearchTree vorwaerts geblaettert werden kann.
      *
      * @return  true/false
      */
     public boolean isBrowseForward() {
         return this.browseForward;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getVisibleNodes() {
         return this.visibleNodes;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
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
 }
