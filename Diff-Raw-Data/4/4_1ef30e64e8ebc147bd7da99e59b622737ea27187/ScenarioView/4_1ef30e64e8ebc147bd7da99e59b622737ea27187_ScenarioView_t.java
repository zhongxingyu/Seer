 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.crisma;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.ImageUtilities;
 
 import java.awt.Component;
 import java.awt.EventQueue;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  $Revision$, $Date$
  */
 public class ScenarioView extends javax.swing.JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** LOGGER. */
     private static final transient Logger LOG = Logger.getLogger(ScenarioView.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private transient boolean listSelectionUpdate;
     private transient boolean treeSelectionUpdate;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JList lstScenarios;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form ScenarioView.
      */
     private ScenarioView() {
         initComponents();
 
         ComponentRegistry.getRegistry().getCatalogueTree().addTreeSelectionListener(new TreeSelectionListener() {
 
                 @Override
                 public void valueChanged(final TreeSelectionEvent e) {
                     if (listSelectionUpdate) {
                         return;
                     }
 
                     try {
                         treeSelectionUpdate = true;
                         updateSelection();
                     } finally {
                         treeSelectionUpdate = false;
                     }
                 }
             });
 
         lstScenarios.addListSelectionListener(new ListSelectionListener() {
 
                 @Override
                 public void valueChanged(final ListSelectionEvent e) {
                    if (treeSelectionUpdate || e.getValueIsAdjusting()) {
                         return;
                     }
 
                     try {
                         listSelectionUpdate = true;
                         final Object[] selection = lstScenarios.getSelectedValues();
                         final List<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
 
                         final DefaultMutableTreeNode dntm = (DefaultMutableTreeNode)ComponentRegistry
                                     .getRegistry().getCatalogueTree().getModel().getRoot();
                         final Enumeration dfs = dntm.depthFirstEnumeration();
 
                         while (dfs.hasMoreElements()) {
                             final Object o = dfs.nextElement();
                             MetaObject mo = null;
                             if (o instanceof ObjectTreeNode) {
                                 mo = ((ObjectTreeNode)o).getMetaObject();
                             }
                             if (o instanceof MetaObjectNode) {
                                 mo = ((MetaObjectNode)o).getObject();
                             }
 
                             for (final Object obj : selection) {
                                 if (obj.equals(mo)) {
                                     nodes.add((DefaultMutableTreeNode)o);
                                 }
                             }
                         }
                         if (nodes.isEmpty()) {
                             final List mos = new ArrayList();
                             for (final Object mo : selection) {
                                 mos.add(new ObjectTreeNode(new MetaObjectNode(((MetaObject)mo).getBean())));
                             }

                             ComponentRegistry.getRegistry().getDescriptionPane().setNodesDescriptions(mos);
                         } else {
                             ComponentRegistry.getRegistry().getCatalogueTree().setSelectedNodes(nodes, true);
                         }
                     } finally {
                         listSelectionUpdate = false;
                     }
                 }
             });
 
         lstScenarios.setCellRenderer(new DefaultListCellRenderer() {
 
                 private final transient ImageIcon wsIcon = ImageUtilities.loadImageIcon(
                         ScenarioView.this.getClass().getPackage().getName().replaceAll("\\.", "/")
                                 + "/earth.gif",
                         false);
 
                 @Override
                 public Component getListCellRendererComponent(final JList list,
                         final Object value,
                         final int index,
                         final boolean isSelected,
                         final boolean cellHasFocus) {
                     final JLabel label = (JLabel)super.getListCellRendererComponent(
                             list,
                             value,
                             index,
                             isSelected,
                             cellHasFocus);
                     label.setIcon(wsIcon);
 
                     return label;
                 }
             });
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static ScenarioView getInstance() {
         return LazyInitialiser.INSTANCE;
     }
 
     /**
      * DOCUMENT ME!
      */
     public void updateLeafs() {
         final MetaClass mc = ClassCacheMultiple.getMetaClass("CRISMA", "WORLDSTATE");
 
         final String sql = "SELECT " + mc.getID() + ", " + mc.getPrimaryKey()
                     + " FROM WORLDSTATE WHERE id NOT IN (SELECT DISTINCT parent FROM WORLDSTATE WHERE parent IS NOT NULL)"; // NOI18N
 
         final MetaObject[] mos;
         try {
             mos = SessionManager.getProxy().getMetaObjectByQuery(sql, 0);
         } catch (final ConnectionException ex) {
             LOG.warn("cannot update leafs", ex);
 
             return;
         }
 
         final Runnable r = new Runnable() {
 
                 @Override
                 public void run() {
                     final DefaultListModel dlm = (DefaultListModel)lstScenarios.getModel();
                     dlm.clear();
                     for (final MetaObject mo : mos) {
                         dlm.addElement(mo);
                     }
 
                     updateSelection();
                 }
             };
         if (EventQueue.isDispatchThread()) {
             r.run();
         } else {
             EventQueue.invokeLater(r);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void updateSelection() {
         final Runnable r = new Runnable() {
 
                 @Override
                 public void run() {
                     final Collection selectedNodes = ComponentRegistry.getRegistry()
                                 .getCatalogueTree()
                                 .getSelectedNodes();
                     final ArrayList<Integer> indicesList = new ArrayList<Integer>();
                     for (final Object o : selectedNodes) {
                         if (o instanceof ObjectTreeNode) {
                             final MetaObject mo = ((ObjectTreeNode)o).getMetaObject();
                             indicesList.add(((DefaultListModel)lstScenarios.getModel()).indexOf(mo));
                         }
                         if (o instanceof MetaObjectNode) {
                             final MetaObject mo = ((MetaObjectNode)o).getObject();
                             indicesList.add(((DefaultListModel)lstScenarios.getModel()).indexOf(mo));
                         }
                     }
 
                     final int[] indices = new int[indicesList.size()];
                     for (int i = 0; i < indices.length; ++i) {
                         indices[i] = indicesList.get(i);
                     }
 
                     lstScenarios.clearSelection();
                     lstScenarios.setSelectedIndices(indices);
                 }
             };
 
         if (EventQueue.isDispatchThread()) {
             r.run();
         } else {
             EventQueue.invokeLater(r);
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         final java.awt.GridBagConstraints gridBagConstraints;
 
         jScrollPane1 = new javax.swing.JScrollPane();
         lstScenarios = new javax.swing.JList();
 
         setLayout(new java.awt.GridBagLayout());
 
         lstScenarios.setModel(new DefaultListModel());
         jScrollPane1.setViewportView(lstScenarios);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(jScrollPane1, gridBagConstraints);
     } // </editor-fold>//GEN-END:initComponents
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static final class LazyInitialiser {
 
         //~ Static fields/initializers -----------------------------------------
 
         private static final ScenarioView INSTANCE = new ScenarioView();
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new LazyInitialiser object.
          */
         private LazyInitialiser() {
         }
     }
 }
