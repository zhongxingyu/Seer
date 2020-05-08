 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * AttributeEditor.java
  *
  * Created on 1. Juli 2004, 13:42
  */
 package de.cismet.cids.editors;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.resource.ResourceManager;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.navigator.ui.RequestsFullSizeComponent;
 import Sirius.navigator.ui.attributes.AttributeViewer;
 import Sirius.navigator.ui.attributes.editor.AttributeEditor;
 import Sirius.navigator.ui.tree.MetaCatalogueTree;
 
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.newuser.User;
 
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 
 import org.openide.util.WeakListeners;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Level;
 
 import javax.swing.AbstractButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.SwingWorker;
 import javax.swing.Timer;
 
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.DisposableCidsBeanStore;
 
 import de.cismet.tools.CismetThreadPool;
 import de.cismet.tools.StaticDebuggingTools;
 
 import de.cismet.tools.gui.ComponentWrapper;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.WrappedComponent;
 
 /**
  * DOCUMENT ME!
  *
  * @author   pascal
  * @version  $Revision$, $Date$
  */
 public class NavigatorAttributeEditorGui extends AttributeEditor {
 
     //~ Static fields/initializers ---------------------------------------------
 
     // do not remove!
     private static final ResourceManager resources = ResourceManager.getManager();
 
     //~ Instance fields --------------------------------------------------------
 
     private transient PropertyChangeListener strongReferenceOnWeakListener = null;
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private Object treeNode = null;
     private MetaObject backupObject = null;
     private MetaObject editorObject = null;
     private JComponent wrappedWaitingPanel;
     private BeanInitializer currentInitializer = null;
     private DisposableCidsBeanStore currentBeanStore = null;
     private JComponent editorComponent = null;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton cancelButton;
     private javax.swing.JButton commitButton;
     private javax.swing.JPanel controlBar;
     private javax.swing.JButton copyButton;
     private javax.swing.JScrollPane editorScrollPane;
     private javax.swing.JButton jButton1;
     private javax.swing.JLabel lblEditorCreation;
     private javax.swing.JPanel panDebug;
     private javax.swing.JButton pasteButton;
     private javax.swing.JScrollPane scpEditor;
     private javax.swing.JPanel switchPanel;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form AttributeEditor.
      */
     public NavigatorAttributeEditorGui() {
         initComponents();
         if (!StaticDebuggingTools.checkHomeForFile("cidsNavigatorGuiHiddenDebugControls")) { // NOI18N
             panDebug.setVisible(false);
         }
         final ComponentWrapper cw = CidsObjectEditorFactory.getInstance().getComponentWrapper();
         if (cw != null) {
             wrappedWaitingPanel = (JComponent)cw.wrapComponent(lblEditorCreation);
         }
 
         // Nur fuer die Uebergangsphase sollange noch von AttributeEditor geerbt wird
         ActionListener[] alr = commitButton.getActionListeners();
         for (final ActionListener al : alr) {
             commitButton.removeActionListener(al);
         }
         alr = cancelButton.getActionListeners();
         for (final ActionListener al : alr) {
             cancelButton.removeActionListener(al);
         }
 
         commitButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (((backupObject != null)
                                     && (editorObject.getBean().hasArtificialChangeFlag()
                                         || editorObject.isChanged()))
                                 || (backupObject == null)) {
                         if (editorObject.getBean().hasObjectWritePermission(SessionManager.getSession().getUser())) {
                             if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 0) {
                                 final int answer = JOptionPane.showConfirmDialog(
                                         NavigatorAttributeEditorGui.this,
                                         org.openide.util.NbBundle.getMessage(
                                             NavigatorAttributeEditorGui.class,
                                             "NavigatorAttributeEditorGui.NavigatorAttributeEditorGui().commitButton.JOptionPane.message"), // NOI18N
                                         org.openide.util.NbBundle.getMessage(
                                             NavigatorAttributeEditorGui.class,
                                             "NavigatorAttributeEditorGui.NavigatorAttributeEditorGui().commitButton.JOptionPane.title"), // NOI18N
                                         JOptionPane.YES_NO_CANCEL_OPTION);
                                 if (answer == JOptionPane.YES_OPTION) {
                                     saveIt();
                                 } else if (answer == JOptionPane.CANCEL_OPTION) {
                                 } else {
                                     reloadFromDB();
                                     clear();
                                 }
                             } else {
                                 saveIt(false);
                             }
                         } else {
                             JOptionPane.showMessageDialog(
                                 NavigatorAttributeEditorGui.this,
                                 org.openide.util.NbBundle.getMessage(
                                     NavigatorAttributeEditorGui.class,
                                     "NavigatorAttributeEditorGui.NavigatorAttributeEditorGui().commitButton.JOptionPane.noobjectpermission.message"),
                                 org.openide.util.NbBundle.getMessage(
                                     NavigatorAttributeEditorGui.class,
                                     "NavigatorAttributeEditorGui.NavigatorAttributeEditorGui().commitButton.JOptionPane.noobjectpermission.title"),
                                 JOptionPane.WARNING_MESSAGE);
                             return;
                         }
                     } else {
                         if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 0) {
                             clear();
                         }
                     }
                 }
             });
 
         cancelButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     if (((backupObject != null)
                                     && (editorObject.getBean().hasArtificialChangeFlag()
                                         || editorObject.isChanged()))
                                 || (backupObject == null)) {
                         final int answer = JOptionPane.showConfirmDialog(
                                 NavigatorAttributeEditorGui.this,
                                 org.openide.util.NbBundle.getMessage(
                                     NavigatorAttributeEditorGui.class,
                                     "NavigatorAttributeEditorGui.NavigatorAttributeEditorGui().cancelButton.JOptionPane.message"), // NOI18N
                                 org.openide.util.NbBundle.getMessage(
                                     NavigatorAttributeEditorGui.class,
                                     "NavigatorAttributeEditorGui.NavigatorAttributeEditorGui().cancelButton.JOptionPane.title"), // NOI18N
                                 JOptionPane.YES_NO_OPTION);
                         if (answer == JOptionPane.YES_OPTION) {
                             reloadFromDB();
                             clear();
                         }
                     } else {
                        if (currentBeanStore instanceof EditorSaveListener) {
                            ((EditorSaveListener)currentBeanStore).editorClosed(
                                new EditorClosedEvent(
                                    EditorSaveListener.EditorSaveStatus.CANCELED));
                        }
                         clear();
                     }
                 }
             });
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  DOCUMENT ME!
      */
     public static void main(final String[] args) {
         System.out.println(
             org.openide.util.NbBundle.getMessage(
                 NavigatorAttributeEditorGui.class,
                 "NavigatorAttributeEditorGui.NavigatorAttributeEditorGui().commitButton.JOptionPane.noobjectpermission.message"));
     }
 
     @Override
     public void setControlBarVisible(final boolean isVisible) {
         controlBar.setVisible(isVisible);
     }
 
     @Override
     public Vector<AbstractButton> getControlBarButtons() {
         final Vector<AbstractButton> buttons = new Vector<AbstractButton>();
         buttons.add(commitButton);
         buttons.add(cancelButton);
         buttons.add(copyButton);
         buttons.add(pasteButton);
         return buttons;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void refreshAttributeTable() {
         final AttributeViewer viewer = ComponentRegistry.getRegistry().getAttributeViewer();
         final Object node = ComponentRegistry.getRegistry().getActiveCatalogue().getSelectedNode();
         viewer.setTreeNode(node);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void refreshSearchTree() {
         final List<Node> oldNodes = ComponentRegistry.getRegistry().getSearchResultsTree().getResultNodes();
 
         if (oldNodes == null) {
             // The search result tree has no elements. So it should not be refreshed
             return;
         }
         final Node[] newNodes = new Node[oldNodes.size()];
 
         for (int index = 0; index < oldNodes.size(); index++) {
             final Node node = oldNodes.get(index);
             if (node instanceof MetaObjectNode) {
                 // Bei MetaObjectNodes wird der Node neu erzeugt, damit der Name gleich dem ToString Wert des
                 // veränderten Objektes ist
                 try {
                     final MetaObjectNode metaObjectNode = (MetaObjectNode)node;
                     final MetaObject metaObject = metaObjectNode.getObject();
                     final CidsBean cidsBean = metaObject.getBean();
                     newNodes[index] = new MetaObjectNode(cidsBean);
                 } catch (final Exception ex) {
                     if (log.isDebugEnabled()) {
                         log.debug("error while creating new MetaObjectNode", ex);
                     }
                     // wenn was schief läuft, dann wenigstens den alten node übernehmen
                     newNodes[index] = node;
                 }
             } else {
                 newNodes[index] = node;
             }
         }
         ComponentRegistry.getRegistry().getSearchResultsTree().setResultNodes(newNodes, false, null);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void refreshTree() {
         if (treePath != null) {
             try {
                 final MetaCatalogueTree metaCatalogueTree = ComponentRegistry.getRegistry().getCatalogueTree();
                 metaCatalogueTree.refreshTreePath(treePath);
             } catch (Exception e) {
                 log.error("Error when refreshing Tree", e); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  orig  DOCUMENT ME!
      */
     private void createBackup(final MetaObject orig) {
         try {
             final int oid = orig.getID();
             final int cid = orig.getMetaClass().getID();
             final String domain = orig.getDomain();
             final User user = SessionManager.getSession().getUser();
             backupObject = null;
             backupObject = SessionManager.getConnection().getMetaObject(user, oid, cid, domain);
         } catch (Exception e) {
             log.error("Error during Backupcreation. Cannot detect whether the object is changed.", e); // NOI18N
             JOptionPane.showMessageDialog(
                 NavigatorAttributeEditorGui.this,
                 org.openide.util.NbBundle.getMessage(
                     NavigatorAttributeEditorGui.class,
                     "NavigatorAttributeEditorGui.createBackup().exception.JOptionPane.message"),       // NOI18N
                 org.openide.util.NbBundle.getMessage(
                     NavigatorAttributeEditorGui.class,
                     "NavigatorAttributeEditorGui.createBackup().exception.JOptionPane.title"),         // NOI18N
                 JOptionPane.WARNING_MESSAGE);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void reloadFromDB() {
         try {
             final ObjectTreeNode otn = (ObjectTreeNode)treeNode;
             final int oid = otn.getMetaObject().getID();
 
             if (oid == -1) {
                 return;
             }
 
             final int cid = otn.getMetaObject().getMetaClass().getID();
             final String domain = otn.getMetaObject().getDomain();
             final User user = SessionManager.getSession().getUser();
 
             final MetaObject reloaded = SessionManager.getConnection().getMetaObject(user, oid, cid, domain);
             reloaded.setAllClasses();
             if (log.isDebugEnabled()) {
                 log.debug("Reloaded MO:" + reloaded.getDebugString()); // NOI18N
             }
             otn.setMetaObject(reloaded);
 
             refreshAttributeTable();
         } catch (Exception e) {
             log.error("Error durig reload from DB.", e);                                 // NOI18N
             final ErrorInfo ei = new ErrorInfo(org.openide.util.NbBundle.getMessage(
                         NavigatorAttributeEditorGui.class,
                         "NavigatorAttributeEditorGui.reloadFromDB().ErrorInfo.title"),   // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         NavigatorAttributeEditorGui.class,
                         "NavigatorAttributeEditorGui.reloadFromDB().ErrorInfo.message"), // NOI18N
                     null,
                     null,
                     e,
                     Level.SEVERE,
                     null);
             JXErrorPane.showDialog(this, ei);
         }
         if (currentBeanStore instanceof EditorSaveListener) {
             ((EditorSaveListener)currentBeanStore).editorClosed(new EditorClosedEvent(
                     EditorSaveListener.EditorSaveStatus.CANCELED));
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void saveIt() {
         saveIt(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  closeEditor  DOCUMENT ME!
      */
     private void saveIt(final boolean closeEditor) {
         final ObjectTreeNode otn = (ObjectTreeNode)treeNode;
         final MetaObject mo = otn.getMetaObject();
         final CidsBean oldBean = mo.getBean();
         final EditorSaveListener editorSaveListener;
         if (currentBeanStore instanceof EditorSaveListener) {
             editorSaveListener = (EditorSaveListener)currentBeanStore;
             if (!editorSaveListener.prepareForSave()) {
                 // editor is not ready for safe? then stop save procedure...
                 return;
             }
         } else {
             editorSaveListener = null;
         }
         try {
             final CidsBean savedInstance = oldBean.persist();
             ((ObjectTreeNode)treeNode).setMetaObject(savedInstance.getMetaObject());
             if (closeEditor) {
                 final JOptionPane jop = new JOptionPane(
                         org.openide.util.NbBundle.getMessage(
                             NavigatorAttributeEditorGui.class,
                             "NavigatorAttributeEditorGui.saveIt().jop.message"), // NOI18N
                         JOptionPane.INFORMATION_MESSAGE);
 
                 final JDialog dialog = jop.createDialog(
                         NavigatorAttributeEditorGui.this,
                         org.openide.util.NbBundle.getMessage(
                             NavigatorAttributeEditorGui.class,
                             "NavigatorAttributeEditorGui.saveIt().dialog.title")); // NOI18N
 
                 final Timer t = new Timer(2000, new ActionListener() {
 
                             @Override
                             public void actionPerformed(final ActionEvent e) {
                                 dialog.setVisible(false);
                                 dialog.dispose();
                             }
                         });
                 t.setRepeats(false);
                 t.start();
                 StaticSwingTools.showDialog(dialog);
                 clear();
             } else {
 //                final AttributeViewer viewer = ComponentRegistry.getRegistry().getAttributeViewer();
 //                final MetaCatalogueTree tree = ComponentRegistry.getRegistry().getActiveCatalogue();
                 editorObject = savedInstance.getMetaObject();
                 createBackup(editorObject);
                 // --- CidsBean bean = editorObject.getBean(); final PropertyChangeListener propertyChangeListener = new
                 // PropertyChangeListener() {
                 //
                 // @Override public void propertyChange(PropertyChangeEvent evt) { viewer.repaint(); tree.repaint();
                 //
                 // } }; if (currentBeanStore instanceof JComponent) { strongReferenceOnWeakListener =
                 // propertyChangeListener; } else { log.error("A CidsBeansStore must be instanceof JComponent here,
                 // but it was " + currentBeanStore + "!"); }
                 // bean.addPropertyChangeListener(WeakListeners.propertyChange(propertyChangeListener, bean)); ---
                 // editorObject.getBean().addPropertyChangeListener(new PropertyChangeListener() {
                 //
                 // @Override public void propertyChange(PropertyChangeEvent evt) { viewer.repaint(); tree.repaint();
                 //
                 // } });
                 currentBeanStore.setCidsBean(savedInstance);
                 for (final PropertyChangeListener pcl : oldBean.getPropertyChangeListeners()) {
                     savedInstance.addPropertyChangeListener(pcl);
                 }
             }
             if (editorSaveListener != null) {
                 editorSaveListener.editorClosed(new EditorClosedEvent(
                         EditorSaveListener.EditorSaveStatus.SAVE_SUCCESS,
                         savedInstance));
             }
             refreshTree();
             refreshSearchTree();
         } catch (Exception ex) {
             if (editorSaveListener != null) {
                 editorSaveListener.editorClosed(new EditorClosedEvent(EditorSaveListener.EditorSaveStatus.SAVE_ERROR));
             }
             final Throwable firstCause = getFirstCause(ex);
             log.error("Error while saving", ex); // NOI18N
 
             if ((firstCause != null) && (firstCause.getMessage() != null)
                         && firstCause.getMessage().equals("not allowed to insert meta object")) {
                 final ErrorInfo ei = new ErrorInfo(org.openide.util.NbBundle.getMessage(
                             NavigatorAttributeEditorGui.class,
                             "NavigatorAttributeEditorGui.saveIt().JOptionPane.title"),   // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             NavigatorAttributeEditorGui.class,
                             "NavigatorAttributeEditorGui.saveIt().JOptionPane.message"), // NOI18N
                         null,
                         null,
                         ex,
                         Level.SEVERE,
                         null);
                 JXErrorPane.showDialog(NavigatorAttributeEditorGui.this, ei);
             } else {
                 final ErrorInfo ei = new ErrorInfo(org.openide.util.NbBundle.getMessage(
                             NavigatorAttributeEditorGui.class,
                             "NavigatorAttributeEditorGui.saveIt().ErrorInfo.title"),     // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             NavigatorAttributeEditorGui.class,
                             "NavigatorAttributeEditorGui.saveIt().ErrorInfo.message"),   // NOI18N
                         null,
                         null,
                         ex,
                         Level.SEVERE,
                         null);
                 JXErrorPane.showDialog(NavigatorAttributeEditorGui.this, ei);
             }
             refreshTree();
             refreshSearchTree();
         }
         if (closeEditor) {
             clear();
         }
     }
 
     @Override
     public void setTreeNode(final Object node) {
         if ((treeNode != null) && (editorObject != null) && (backupObject != null)) {
             if (editorObject.getBean().hasArtificialChangeFlag() || editorObject.isChanged()) {
                 final int answer = JOptionPane.showConfirmDialog(
                         NavigatorAttributeEditorGui.this,
                         org.openide.util.NbBundle.getMessage(
                             NavigatorAttributeEditorGui.class,
                             "NavigatorAttributeEditorGui.setTreeNode().confirmDialog.message"), // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             NavigatorAttributeEditorGui.class,
                             "NavigatorAttributeEditorGui.setTreeNode().confirmDialog.title"), // NOI18N
                         JOptionPane.YES_NO_CANCEL_OPTION);
                 if (answer == JOptionPane.YES_OPTION) {
                     saveIt();
                 } else if (answer == JOptionPane.NO_OPTION) {
                     reloadFromDB();
                 } else {                                                                      // Cancel
                     return;
                 }
             }
         }
         final AttributeViewer viewer = ComponentRegistry.getRegistry().getAttributeViewer();
         final MetaCatalogueTree tree = ComponentRegistry.getRegistry().getActiveCatalogue();
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     removeAndDisposeEditor();
                     scpEditor.getViewport().setView(wrappedWaitingPanel);
                     NavigatorAttributeEditorGui.this.revalidate();
                 }
             });
         treeNode = node;
         if (treeNode instanceof ObjectTreeNode) {
 //            final DescriptionPane desc = ComponentRegistry.getRegistry().getDescriptionPane();
             cancelButton.setEnabled(false);
             commitButton.setEnabled(false);
             CismetThreadPool.execute(new SwingWorker<JComponent, Void>() {
 
                     @Override
                     protected JComponent doInBackground() throws Exception {
                         final ObjectTreeNode otn = (ObjectTreeNode)treeNode;
                         editorObject = otn.getMetaObject();
                         backupObject = null;
                         new Thread(new Runnable() {
 
                                 @Override
                                 public void run() {
                                     try {
                                         createBackup(editorObject);
                                     } finally {
                                         EventQueue.invokeLater(new Runnable() {
 
                                                 @Override
                                                 public void run() {
                                                     cancelButton.setEnabled(true);
                                                     commitButton.setEnabled(true);
                                                 }
                                             });
                                     }
                                 }
                             }).start();
 
 //                    editorObject.getBean().addPropertyChangeListener(new PropertyChangeListener() {
 //
 //                        @Override
 //                        public void propertyChange(PropertyChangeEvent evt) {
 //                            viewer.repaint();
 //                            tree.repaint();
 //                            //commitButton.setEnabled(!backupObject.propertyEquals(mo)); //vielleicht einfach zuviel des guten
 //
 //                        }
 //                    });
 //                    try {
 //                        EditorBeanInitializerStore.getInstance().initialize(editorObject.getBean());
 //                    } catch (Exception ex) {
 //                        log.error("Exception while initializing Bean with template values!", ex);
 //                    }
                         final JComponent ed = CidsObjectEditorFactory.getInstance().getEditor(editorObject);
                         return ed;
                     }
 
                     @Override
                     protected void done() {
                         try {
                             JComponent ed = get();
                             if (log.isDebugEnabled()) {
                                 log.debug("editor:" + ed); // NOI18N
                             }
                             removeAndDisposeEditor();
                             editorComponent = ed;
                             switchPanel.remove(scpEditor);
                             if (ed instanceof RequestsFullSizeComponent) {
                                 switchPanel.add(ed, BorderLayout.CENTER);
                             } else {
                                 switchPanel.add(scpEditor, BorderLayout.CENTER);
                                 scpEditor.getViewport().setView(ed);
                             }
 
                             if (ed instanceof WrappedComponent) {
                                 ed = ((WrappedComponent)ed).getOriginalComponent();
                             }
                             if (ed instanceof DisposableCidsBeanStore) {
                                 currentBeanStore = (DisposableCidsBeanStore)ed;
                                 currentInitializer = EditorBeanInitializerStore.getInstance()
                                             .getInitializer(editorObject.getMetaClass());
                                 copyButton.setEnabled(true);
                                 if (currentInitializer != null) {
                                     // enable editor attribute paste only for NEW MOs
                                     final boolean isNewBean = currentBeanStore.getCidsBean().getMetaObject().getStatus()
                                                 == MetaObject.NEW;
                                     pasteButton.setEnabled(isNewBean);
                                 }
                             }
                             final CidsBean bean = editorObject.getBean();
                             final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
 
                                     @Override
                                     public void propertyChange(final PropertyChangeEvent evt) {
                                         viewer.repaint();
                                         tree.repaint();
                                     }
                                 };
                             strongReferenceOnWeakListener = propertyChangeListener;
                             bean.addPropertyChangeListener(WeakListeners.propertyChange(propertyChangeListener, bean));
                             NavigatorAttributeEditorGui.this.revalidate();
                             if (log.isDebugEnabled()) {
                                 log.debug("editor added");                                       // NOI18N
                             }
                         } catch (Exception e) {
                             final ErrorInfo ei = new ErrorInfo(
                                     org.openide.util.NbBundle.getMessage(
                                         NavigatorAttributeEditorGui.class,
                                         "NavigatorAttributeEditorGui.done().ErrorInfo.title"),   // NOI18N
                                     org.openide.util.NbBundle.getMessage(
                                         NavigatorAttributeEditorGui.class,
                                         "NavigatorAttributeEditorGui.done().ErrorInfo.message"), // NOI18N
                                     null,
                                     null,
                                     e,
                                     Level.SEVERE,
                                     null);
                             log.error(
                                 "Error while displaying Editor"
                                         + ei.getState()
                                         + editorObject.getDebugString()
                                         + "\n",
                                 e);                                                              // NOI18N
                             JXErrorPane.showDialog(NavigatorAttributeEditorGui.this, ei);
                             clear();
                         }
                     }
                 });
         } else {
             log.warn("Given Treenode is not instance of ObjectTreeMode, but: " + node.getClass()); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   th  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Throwable getFirstCause(final Throwable th) {
         if (th == null) {
             return null;
         } else if (th.getCause() == null) {
             return th;
         } else {
             return getFirstCause(th.getCause());
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void removeAndDisposeEditor() {
         if (currentBeanStore != null) {
             currentBeanStore.dispose();
         }
         if (editorComponent instanceof RequestsFullSizeComponent) {
             switchPanel.remove(editorComponent);
 //            switchPanel.removeAll();
         } else {
             scpEditor.getViewport().removeAll();
         }
         // release the strong reference on the listener, so that the weak listener can be GCed.
         strongReferenceOnWeakListener = null;
         currentBeanStore = null;
         editorComponent = null;
     }
 
     @Override
     protected void clear() {
         currentInitializer = null;
         removeAndDisposeEditor();
         commitButton.setEnabled(false);
         cancelButton.setEnabled(false);
         copyButton.setEnabled(false);
         pasteButton.setEnabled(false);
         revalidate();
         repaint();
         ComponentRegistry.getRegistry().getAttributeViewer().repaint();
         this.treeNode = null;
         backupObject = null;
         editorObject = null;
     }
 
     @Override
     public Object getTreeNode() {
         return treeNode;
     }
 
     @Override
     public boolean isChanged() {
         return true;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         final java.awt.GridBagConstraints gridBagConstraints;
 
         final javax.swing.JToggleButton pinButton = new javax.swing.JToggleButton();
         final javax.swing.JToggleButton editButton = new javax.swing.JToggleButton();
         editorScrollPane = new javax.swing.JScrollPane();
         lblEditorCreation = new javax.swing.JLabel();
         controlBar = new javax.swing.JPanel();
         commitButton = new javax.swing.JButton();
         cancelButton = new javax.swing.JButton();
         copyButton = new javax.swing.JButton();
         pasteButton = new javax.swing.JButton();
         switchPanel = new javax.swing.JPanel();
         scpEditor = new javax.swing.JScrollPane();
         panDebug = new javax.swing.JPanel();
         jButton1 = new javax.swing.JButton();
 
         pinButton.setIcon(resources.getIcon("attr_pin_off.gif"));
         pinButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NavigatorAttributeEditorGui.class,
                 "NavigatorAttributeEditorGui.pinButton.tooltip")); // NOI18N
         pinButton.setActionCommand("pin");
         pinButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         pinButton.setContentAreaFilled(false);
         pinButton.setFocusPainted(false);
         pinButton.setMaximumSize(new java.awt.Dimension(16, 16));
         pinButton.setMinimumSize(new java.awt.Dimension(16, 16));
         pinButton.setPreferredSize(new java.awt.Dimension(16, 16));
         pinButton.setRolloverIcon(resources.getIcon("attr_pin_off.gif"));
         pinButton.setRolloverSelectedIcon(resources.getIcon("attr_pin_on.gif"));
         pinButton.setSelectedIcon(resources.getIcon("attr_pin_on.gif"));
 
         editButton.setIcon(resources.getIcon("objekt_bearbeiten.gif"));
         editButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NavigatorAttributeEditorGui.class,
                 "NavigatorAttributeEditorGui.editButton.tooltip")); // NOI18N
         editButton.setActionCommand("edit");
         editButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         editButton.setContentAreaFilled(false);
         editButton.setFocusPainted(false);
         editButton.setMaximumSize(new java.awt.Dimension(16, 16));
         editButton.setMinimumSize(new java.awt.Dimension(16, 16));
         editButton.setPreferredSize(new java.awt.Dimension(16, 16));
         editButton.setRolloverIcon(resources.getIcon("objekt_bearbeiten.gif"));
         editButton.setRolloverSelectedIcon(resources.getIcon("objekt_bearbeiten.gif"));
         editButton.setSelectedIcon(resources.getIcon("objekt_bearbeiten.gif"));
 
         editorScrollPane.setPreferredSize(new java.awt.Dimension(250, 150));
 
         lblEditorCreation.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblEditorCreation.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/Sirius/navigator/resource/img/load.png"))); // NOI18N
 
         controlBar.setLayout(new java.awt.GridBagLayout());
 
         commitButton.setIcon(resources.getIcon("save_objekt.gif"));
         commitButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NavigatorAttributeEditorGui.class,
                 "NavigatorAttributeEditorGui.commitButton.tooltip")); // NOI18N
         commitButton.setActionCommand("commit");
         commitButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         commitButton.setContentAreaFilled(false);
         commitButton.setEnabled(false);
         commitButton.setFocusPainted(false);
         commitButton.setMaximumSize(new java.awt.Dimension(16, 16));
         commitButton.setMinimumSize(new java.awt.Dimension(16, 16));
         commitButton.setPreferredSize(new java.awt.Dimension(16, 16));
         commitButton.setRolloverIcon(resources.getIcon("save_objekt.gif"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
         controlBar.add(commitButton, gridBagConstraints);
 
         cancelButton.setIcon(resources.getIcon("zurueck_objekt.gif"));
         cancelButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NavigatorAttributeEditorGui.class,
                 "NavigatorAttributeEditorGui.cancelButton.tooltip")); // NOI18N
         cancelButton.setActionCommand("cancel");
         cancelButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         cancelButton.setContentAreaFilled(false);
         cancelButton.setEnabled(false);
         cancelButton.setFocusPainted(false);
         cancelButton.setMaximumSize(new java.awt.Dimension(16, 16));
         cancelButton.setMinimumSize(new java.awt.Dimension(16, 16));
         cancelButton.setPreferredSize(new java.awt.Dimension(16, 16));
         cancelButton.setRolloverIcon(resources.getIcon("zurueck_objekt.gif"));
         controlBar.add(cancelButton, new java.awt.GridBagConstraints());
 
         copyButton.setIcon(resources.getIcon("document-copy.png"));
         copyButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NavigatorAttributeEditorGui.class,
                 "NavigatorAttributeEditorGui.copyButton.tooltip")); // NOI18N
         copyButton.setActionCommand("cancel");
         copyButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         copyButton.setContentAreaFilled(false);
         copyButton.setEnabled(false);
         copyButton.setFocusPainted(false);
         copyButton.setMaximumSize(new java.awt.Dimension(16, 16));
         copyButton.setMinimumSize(new java.awt.Dimension(16, 16));
         copyButton.setPreferredSize(new java.awt.Dimension(16, 16));
         copyButton.setRolloverIcon(resources.getIcon("document-copy.png"));
         copyButton.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     copyButtonActionPerformed(evt);
                 }
             });
         controlBar.add(copyButton, new java.awt.GridBagConstraints());
 
         pasteButton.setIcon(resources.getIcon("clipboard-paste.png"));
         pasteButton.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NavigatorAttributeEditorGui.class,
                 "NavigatorAttributeEditorGui.pasteButton.tooltip")); // NOI18N
         pasteButton.setActionCommand("paste");
         pasteButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         pasteButton.setContentAreaFilled(false);
         pasteButton.setEnabled(false);
         pasteButton.setFocusPainted(false);
         pasteButton.setMaximumSize(new java.awt.Dimension(16, 16));
         pasteButton.setMinimumSize(new java.awt.Dimension(16, 16));
         pasteButton.setPreferredSize(new java.awt.Dimension(16, 16));
         pasteButton.setRolloverIcon(resources.getIcon("clipboard-paste.png"));
         pasteButton.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     pasteButtonActionPerformed(evt);
                 }
             });
         controlBar.add(pasteButton, new java.awt.GridBagConstraints());
 
         add(controlBar, java.awt.BorderLayout.NORTH);
 
         switchPanel.setLayout(new java.awt.BorderLayout());
         switchPanel.add(scpEditor, java.awt.BorderLayout.CENTER);
 
         panDebug.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
 
         jButton1.setText(org.openide.util.NbBundle.getMessage(
                 NavigatorAttributeEditorGui.class,
                 "NavigatorAttributeEditorGui.jButton1.text")); // NOI18N
         jButton1.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     jButton1ActionPerformed(evt);
                 }
             });
         panDebug.add(jButton1);
 
         switchPanel.add(panDebug, java.awt.BorderLayout.NORTH);
 
         add(switchPanel, java.awt.BorderLayout.CENTER);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void jButton1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton1ActionPerformed
         if ((getTreeNode() != null) && (getTreeNode() instanceof ObjectTreeNode)) {
             final MetaObject mo = ((ObjectTreeNode)getTreeNode()).getMetaObject();
             log.fatal("Current MetaObject:" + mo.getDebugString());              // NOI18N
             EditorBeanInitializerStore.getInstance()
                     .registerInitializer(mo.getMetaClass(), new DefaultBeanInitializer(mo.getBean()));
         }
     }                                                                            //GEN-LAST:event_jButton1ActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void copyButtonActionPerformed(final java.awt.event.ActionEvent evt) {      //GEN-FIRST:event_copyButtonActionPerformed
         if (currentBeanStore != null) {
             final CidsBean bean = currentBeanStore.getCidsBean();
             final boolean isNewBean = bean.getMetaObject().getStatus() == MetaObject.NEW;
             if (currentBeanStore instanceof BeanInitializerProvider) {
                 final BeanInitializerProvider beanInitProvider = (BeanInitializerProvider)currentBeanStore;
                 currentInitializer = beanInitProvider.getBeanInitializer();
                 if (currentInitializer != null) {
                     EditorBeanInitializerStore.getInstance()
                             .registerInitializer(bean.getMetaObject().getMetaClass(), currentInitializer);
                 } else {
                     log.error("BeanInitializerProvider delivers null as initializer."); ////NOI18N
                 }
             } else {
                 currentInitializer = new DefaultBeanInitializer(bean);
                 EditorBeanInitializerStore.getInstance()
                         .registerInitializer(bean.getMetaObject().getMetaClass(), currentInitializer);
             }
             // enable editor attribute paste only for new MOs
             pasteButton.setEnabled((currentInitializer != null) && isNewBean);
         }
     } //GEN-LAST:event_copyButtonActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void pasteButtonActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_pasteButtonActionPerformed
         if (currentBeanStore != null) {
             final CidsBean bean = currentBeanStore.getCidsBean();
             try {
                 EditorBeanInitializerStore.getInstance().initialize(bean);
             } catch (Exception ex) {
                 log.error(ex, ex);
             }
         }
     }                                                                               //GEN-LAST:event_pasteButtonActionPerformed
 }
