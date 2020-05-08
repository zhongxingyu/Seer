 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver.project.cidsclass;
 
 import org.apache.log4j.Logger;
 
 import org.jdesktop.swingx.JXList;
 import org.jdesktop.swingx.JXTable;
 import org.jdesktop.swingx.table.TableColumnExt;
 
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.EventQueue;
 import java.awt.datatransfer.Transferable;
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
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.SortOrder;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.JTableHeader;
 
 import de.cismet.cids.abf.domainserver.project.utils.PermissionResolver;
 import de.cismet.cids.abf.domainserver.project.utils.Renderers.UnifiedCellRenderer;
 import de.cismet.cids.abf.utilities.CidsUserGroupTransferable;
 import de.cismet.cids.abf.utilities.Comparators;
 
 import de.cismet.cids.jpa.backend.service.Backend;
 import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
 import de.cismet.cids.jpa.entity.common.Domain;
 import de.cismet.cids.jpa.entity.permission.ClassPermission;
 import de.cismet.cids.jpa.entity.permission.Permission;
 import de.cismet.cids.jpa.entity.permission.Policy;
 import de.cismet.cids.jpa.entity.user.UserGroup;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public final class NewCidsClassVisualPanel2 extends JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(NewCidsClassVisualPanel2.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient NewCidsClassWizardPanel2 model;
     private final transient ItemListener policyL;
 
     private transient Permission[] validPermissions;
     private transient int differentRightCount;
     private transient List<UserGroup> allUserGroups;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final transient javax.swing.JButton btnAdd = new javax.swing.JButton();
     private final transient javax.swing.JButton btnAddAll = new javax.swing.JButton();
     private final transient javax.swing.JButton btnRemove = new javax.swing.JButton();
     private final transient javax.swing.JButton btnRemoveAll = new javax.swing.JButton();
     private final transient javax.swing.JComboBox cboPolicy = new javax.swing.JComboBox();
     private final transient javax.swing.JComboBox cboRights = new javax.swing.JComboBox();
     private final transient javax.swing.JLabel lblDefaultRight = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblGroups = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblPolicy = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblRights = new javax.swing.JLabel();
     private final transient javax.swing.JList lstGroups = new DnDJXList();
     private final transient javax.swing.JScrollPane scpGroups = new javax.swing.JScrollPane();
     private final transient javax.swing.JScrollPane scpRightsTable = new javax.swing.JScrollPane();
     private final transient javax.swing.JTable tblRights = new DnDJXTable();
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new NewCidsClassVisualPanel2 object.
      *
      * @param  m  DOCUMENT ME!
      */
     public NewCidsClassVisualPanel2(final NewCidsClassWizardPanel2 m) {
         this.model = m;
         this.policyL = new PolicyChangedListener();
 
         initComponents();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     void init() {
         if (LOG.isDebugEnabled()) {
             LOG.debug("init");                             // NOI18N
         }
         clear();
         initCboPolicy();
         final UnifiedCellRenderer unifiedRenderer = new UnifiedCellRenderer(
                 model.getProject(),
                 model.getCidsClass());
         final Backend backend = model.getProject().getCidsDataObjectBackend();
         final Comparators.UserGroups ugComp = new Comparators.UserGroups();
         final JComboBox rightComboboxTableCellRenderer = new JComboBox(new DefaultComboBoxModel());
         rightComboboxTableCellRenderer.addActionListener(new RenderListener());
         final List<Permission> perms = backend.getAllEntities(Permission.class);
         final List<Domain> domains = backend.getAllEntities(Domain.class);
         Collections.sort(perms, new Comparators.Permissions());
         Collections.sort(domains, new Comparators.Domains());
         validPermissions = new Permission[perms.size()];
         for (int i = 0; i < perms.size(); ++i) {
             final Permission perm = perms.get(i);
             validPermissions[i] = perm;
             rightComboboxTableCellRenderer.addItem(perm);
             cboRights.addItem(perm);
         }
         differentRightCount = perms.size();
         rightComboboxTableCellRenderer.setRenderer(unifiedRenderer);
         cboRights.setRenderer(unifiedRenderer);
         cboRights.setSelectedItem(perms.get(0));
         final DefaultTableModel tModel = (DefaultTableModel)tblRights.getModel();
         final String group = org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel2.class,
                 "NewCidsClassVisualPanel2.init().group");  // NOI18N
         final String right = org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel2.class,
                 "NewCidsClassVisualPanel2.init().right");  // NOI18N
         final String domain = org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel2.class,
                 "NewCidsClassVisualPanel2.init().domain"); // NOI18N
         tModel.setColumnIdentifiers(new Object[] { group, right, domain });
         tblRights.setTableHeader(new JTableHeader(tblRights.getColumnModel()));
         tblRights.getColumn(right).setCellEditor(new DefaultCellEditor(
                 rightComboboxTableCellRenderer));
         tblRights.getColumn(group).setCellRenderer(unifiedRenderer);
         tblRights.getColumn(right).setCellRenderer(unifiedRenderer);
         tblRights.getColumn(domain).setCellRenderer(unifiedRenderer);
         TableColumnExt tc = ((JXTable)tblRights).getColumnExt(group);
         tc.setComparator(ugComp);
         tc.setSortable(true);
         tc = ((JXTable)tblRights).getColumnExt(right);
         tc.setComparator(null);
         tc.setSortable(false);
         tc = ((JXTable)tblRights).getColumnExt(domain);
         tc.setComparator(null);
         tc.setSortable(false);
         ((JXTable)tblRights).setSortOrder(group, SortOrder.ASCENDING);
         final List<UserGroup> groups = backend.getAllEntities(UserGroup.class);
         allUserGroups = groups;
         ((JXList)lstGroups).setAutoCreateRowSorter(true);
         ((JXList)lstGroups).setSortable(true);
         ((JXList)lstGroups).setSortsOnUpdates(true);
         ((JXList)lstGroups).setComparator(ugComp);
         ((JXList)lstGroups).setSortOrder(SortOrder.ASCENDING);
         ((JXList)lstGroups).setCellRenderer(unifiedRenderer);
         final Set<ClassPermission> cperms = model.getCidsClass().getClassPermissions();
         if (cperms != null) {
             for (final ClassPermission cp : cperms) {
                 final UserGroup ug = cp.getUserGroup();
                groups.remove(ug);
                 tModel.addRow(
                     new Object[] { ug, cp.getPermission(), ug.getDomain() });
             }
         }
        for (final UserGroup ug : groups) {
            ((DefaultListModel)lstGroups.getModel()).addElement(ug);
        }
         lstGroups.requestFocusInWindow();
         updateGroupList();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initCboPolicy() {
         cboPolicy.removeItemListener(policyL);
         ((DefaultComboBoxModel)cboPolicy.getModel()).removeAllElements();
         cboPolicy.setRenderer(new DefaultListCellRenderer() {
 
                 @Override
                 public Component getListCellRendererComponent(
                         final JList list,
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
                     final Policy policy = (Policy)value;
                     final String s;
                     if (policy.equals(Policy.NO_POLICY)) {
                         final CidsClass clazz = model.getCidsClass();
                         final Policy p = clazz.getPolicy();
                         clazz.setPolicy(null);
                         try {
                             s = "<"
                                         + PermissionResolver.getInstance(model.getProject()).getPermString(clazz, null)
                                         .getInheritanceString() + ">"; // NOI18N
                         } finally {
                             clazz.setPolicy(p);
                         }
                     } else {
                         s = policy.getName();
                     }
                     label.setText(s);
                     label.setIcon(null);
                     return label;
                 }
             });
         cboPolicy.addItem(Policy.NO_POLICY);
         final List<Policy> policies = model.getProject().getCidsDataObjectBackend().getAllEntities(Policy.class);
         Collections.sort(policies, new Comparators.Policies());
         for (final Policy p : policies) {
             cboPolicy.addItem(p);
         }
         if (model.getCidsClass().getPolicy() == null) {
             cboPolicy.setSelectedIndex(0);
         } else {
             cboPolicy.setSelectedItem(model.getCidsClass().getPolicy());
         }
         cboPolicy.addItemListener(policyL);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void clear() {
         ((DefaultListModel)lstGroups.getModel()).clear();
         while (tblRights.getModel().getRowCount() > 0) {
             ((DefaultTableModel)tblRights.getModel()).removeRow(0);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void addAllPushed() {
         final DefaultListModel lModel = (DefaultListModel)lstGroups.getModel();
         for (int i = 0; i < lModel.size(); ++i) {
             addIndex(i);
         }
         updateGroupList();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void addPushed() {
         final JXList jxList = (JXList)lstGroups;
         final int[] selected = lstGroups.getSelectedIndices();
         Arrays.sort(selected);
         // start from behind to ensure the indices are still valid!
         for (int i = selected.length - 1; i >= 0; i--) {
             final int index = jxList.convertIndexToModel(selected[i]);
             addIndex(index);
             updateGroupList();
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void removePushed() {
         final DefaultTableModel tModel = (DefaultTableModel)tblRights.getModel();
         final JXTable jxTable = (JXTable)tblRights;
         final int[] selected = tblRights.getSelectedRows();
         Arrays.sort(selected);
         // start from behind to ensure the indices are still valid!
         for (int i = selected.length - 1; i >= 0; i--) {
             final int index = jxTable.convertRowIndexToModel(selected[i]);
             tModel.removeRow(index);
         }
         updateGroupList();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void removeAllPushed() {
         final DefaultTableModel tModel = (DefaultTableModel)tblRights.getModel();
         while (tModel.getRowCount() > 0) {
             tModel.removeRow(0);
         }
         updateGroupList();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   index  DOCUMENT ME!
      *
      * @throws  IllegalStateException  DOCUMENT ME!
      */
     private void addIndex(final int index) {
         final UserGroup ug = (UserGroup)((DefaultListModel)lstGroups.getModel()).getElementAt(index);
         Permission permittedPerm = (Permission)cboRights.getSelectedItem();
         if (!addPermitted(ug, permittedPerm)) {
             permittedPerm = null;
             for (int i = 0; i < validPermissions.length; ++i) {
                 if (addPermitted(ug, validPermissions[i])) {
                     permittedPerm = validPermissions[i];
                     break;
                 }
             }
             if (permittedPerm == null) {
                 throw new IllegalStateException(
                     "could not find a valid permission"); // NOI18N
             }
         }
         ((DefaultTableModel)tblRights.getModel()).addRow(
             new Object[] { ug, permittedPerm, ug.getDomain() });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   ug    DOCUMENT ME!
      * @param   perm  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean addPermitted(final UserGroup ug, final Permission perm) {
         final List data = ((DefaultTableModel)tblRights.getModel()).getDataVector();
         for (final Object row : data) {
             final List l = (List)row;
             if (ug.equals(l.get(0)) && perm.equals(l.get(1))) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void updateGroupList() {
         final JXList list = (JXList)lstGroups;
         final DefaultListModel lModel = (DefaultListModel)list.getModel();
         final Object[] selUgs = lstGroups.getSelectedValues();
         lModel.clear();
         final Set<UserGroup> ugs = getSelectedUGsWithMaxUsage();
         for (final UserGroup ug : allUserGroups) {
             if (!ugs.contains(ug)) {
                 lModel.addElement(ug);
             }
         }
         setSelection(selUgs);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  ugs  DOCUMENT ME!
      */
     private void setSelection(final Object[] ugs) {
         final JXList list = (JXList)lstGroups;
         final ArrayList<Integer> indices = new ArrayList<Integer>(ugs.length);
         for (final Object o : ugs) {
             for (int i = 0; i < list.getElementCount(); ++i) {
                 final Object el = list.getElementAt(i);
                 if (o.equals(el)) {
                     indices.add(i);
                     break;
                 }
             }
         }
         final int[] selection = new int[indices.size()];
         for (int i = 0; i < indices.size(); ++i) {
             selection[i] = indices.get(i);
         }
         list.setSelectedIndices(selection);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Set<UserGroup> getSelectedUGsWithMaxUsage() {
         if (LOG.isDebugEnabled()) {
             LOG.debug("searching for usergroups with max usage"); // NOI18N
         }
         final List data = ((DefaultTableModel)tblRights.getModel()).getDataVector();
         final Set<UserGroup> ugs = new HashSet(data.size());
         for (int i = 0; i < data.size(); ++i) {
             int usageCount = 1;
             final UserGroup ug = (UserGroup)((List)data.get(i)).get(0);
             for (int j = i + 1; j < data.size(); ++j) {
                 final UserGroup copy = (UserGroup)((List)data.get(j)).get(0);
                 if (copy.equals(ug) && (++usageCount == differentRightCount)) {
                     ugs.add(ug);
                     break;
                 }
             }
         }
         return ugs;
     }
 
     @Override
     public String getName() {
         return org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel2.class,
                 "NewCidsClassVisualPanel2.getName().returnvalue"); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     Set<ClassPermission> getPermissions() {
         final Set<ClassPermission> perms = new HashSet<ClassPermission>();
         final DefaultTableModel tModel = (DefaultTableModel)tblRights.getModel();
         for (int i = 0; i < tModel.getRowCount(); i++) {
             final ClassPermission cperm = new ClassPermission();
             final UserGroup ug = (UserGroup)tModel.getValueAt(i, 0);
             final Permission perm = (Permission)tModel.getValueAt(i, 1);
             // Domain domain = (Domain)tModel.getValueAt(i, 2);
             cperm.setCidsClass(model.getCidsClass());
             cperm.setUserGroup(ug);
             cperm.setPermission(perm);
             // cperm.setDomain(domain);
             perms.add(cperm);
         }
         return perms;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private JXTable getRightsTable() {
         return (JXTable)tblRights;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         tblRights.setModel(new DefaultTableModel());
         scpRightsTable.setViewportView(tblRights);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblRights,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel2.class,
                 "NewCidsClassVisualPanel2.lblRights.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(btnAddAll, "<<");
         btnAddAll.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnAddAllActionPerformed(evt);
                 }
             });
 
         org.openide.awt.Mnemonics.setLocalizedText(btnAdd, "<");
         btnAdd.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnAddActionPerformed(evt);
                 }
             });
 
         org.openide.awt.Mnemonics.setLocalizedText(btnRemove, ">");
         btnRemove.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnRemoveActionPerformed(evt);
                 }
             });
 
         org.openide.awt.Mnemonics.setLocalizedText(btnRemoveAll, ">>");
         btnRemoveAll.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnRemoveAllActionPerformed(evt);
                 }
             });
 
         lstGroups.setModel(new DefaultListModel());
         scpGroups.setViewportView(lstGroups);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblGroups,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel2.class,
                 "NewCidsClassVisualPanel2.lblGroups.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblDefaultRight,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel2.class,
                 "NewCidsClassVisualPanel2.lblDefaultRight.text")); // NOI18N
 
         cboRights.setModel(new DefaultComboBoxModel());
         cboRights.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cboRightsActionPerformed(evt);
                 }
             });
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblPolicy,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel2.class,
                 "NewCidsClassVisualPanel2.lblPolicy.text")); // NOI18N
 
         final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                 layout.createSequentialGroup().addContainerGap().add(
                     layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                         scpRightsTable,
                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                         405,
                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(lblRights)).addPreferredGap(
                     org.jdesktop.layout.LayoutStyle.RELATED).add(
                     layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(btnRemoveAll).add(
                         btnRemove).add(btnAdd).add(btnAddAll)).addPreferredGap(
                     org.jdesktop.layout.LayoutStyle.RELATED).add(
                     layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                         org.jdesktop.layout.GroupLayout.TRAILING,
                         cboRights,
                         0,
                         204,
                         Short.MAX_VALUE).add(
                         scpGroups,
                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                         204,
                         Short.MAX_VALUE).add(lblGroups).add(lblDefaultRight).add(lblPolicy).add(
                         cboPolicy,
                         0,
                         204,
                         Short.MAX_VALUE)).addContainerGap()));
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                 layout.createSequentialGroup().addContainerGap().add(
                     layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                         layout.createSequentialGroup().add(
                             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(lblRights).add(
                                 lblPolicy)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(
                                 scpRightsTable,
                                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                 374,
                                 Short.MAX_VALUE).add(
                                 org.jdesktop.layout.GroupLayout.LEADING,
                                 layout.createSequentialGroup().add(
                                     cboPolicy,
                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(5, 5, 5).add(
                                     lblDefaultRight).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                                     cboRights,
                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                                     org.jdesktop.layout.LayoutStyle.RELATED).add(lblGroups).addPreferredGap(
                                     org.jdesktop.layout.LayoutStyle.RELATED).add(
                                     scpGroups,
                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                     279,
                                     Short.MAX_VALUE))).addContainerGap()).add(
                         org.jdesktop.layout.GroupLayout.TRAILING,
                         layout.createSequentialGroup().add(btnAddAll).addPreferredGap(
                             org.jdesktop.layout.LayoutStyle.RELATED).add(btnAdd).add(35, 35, 35).add(btnRemove)
                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(btnRemoveAll).add(
                             152,
                             152,
                             152)))));
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cboRightsActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_cboRightsActionPerformed
     {                                                                           //GEN-HEADEREND:event_cboRightsActionPerformed
                                                                                 // TODO add your handling code here:
     }                                                                           //GEN-LAST:event_cboRightsActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddAllActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_btnAddAllActionPerformed
     {                                                                           //GEN-HEADEREND:event_btnAddAllActionPerformed
         addAllPushed();
     }                                                                           //GEN-LAST:event_btnAddAllActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnAddActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_btnAddActionPerformed
     {                                                                        //GEN-HEADEREND:event_btnAddActionPerformed
         addPushed();
     }                                                                        //GEN-LAST:event_btnAddActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_btnRemoveActionPerformed
     {                                                                           //GEN-HEADEREND:event_btnRemoveActionPerformed
         removePushed();
     }                                                                           //GEN-LAST:event_btnRemoveActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnRemoveAllActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_btnRemoveAllActionPerformed
     {                                                                              //GEN-HEADEREND:event_btnRemoveAllActionPerformed
         removeAllPushed();
     }                                                                              //GEN-LAST:event_btnRemoveAllActionPerformed
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class PolicyChangedListener implements ItemListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void itemStateChanged(final ItemEvent e) {
             if (e.getStateChange() == ItemEvent.SELECTED) {
                 final Policy policy = (Policy)e.getItem();
                 model.getCidsClass().setPolicy((policy.getId() == null) ? null : policy);
             }
             NewCidsClassVisualPanel2.this.repaint();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class RenderListener implements ActionListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void actionPerformed(final ActionEvent e) {
             if (tblRights.isEditing()) {
                 return;
             }
             if (LOG.isDebugEnabled()) {
                 LOG.debug("new right selected, updating"); // NOI18N
             }
             final DefaultTableModel tModel = (DefaultTableModel)tblRights.getModel();
             final JXTable tblRights = getRightsTable();
             final int[] selRows = tblRights.getSelectedRows();
             if (selRows.length != 1) {
                 return;
             }
             final int selIndex = tblRights.convertRowIndexToModel(selRows[0]);
             final UserGroup ug = (UserGroup)tModel.getValueAt(selIndex, 0);
             final Permission selPerm = (Permission)tModel.getValueAt(
                     selIndex,
                     1);
             final List rows = tModel.getDataVector();
             for (int i = 0; i < rows.size(); ++i) {
                 final List row = (List)rows.get(i);
                 if (ug.equals(row.get(0)) && selPerm.equals(row.get(1))
                             && (i != selIndex)) {
                     final int toRem = i;
                     EventQueue.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 tModel.removeRow(toRem);
                             }
                         });
                     return;
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class DnDJXTable extends JXTable implements DropTargetListener,
         DragGestureListener,
         DragSourceListener {
 
         //~ Methods ------------------------------------------------------------
 
         // <editor-fold defaultstate="collapsed" desc=" Not needed Listener impls ">
         @Override
         public void dragEnter(final DragSourceDragEvent dsde) {
             // not needed
         }
 
         @Override
         public void dragOver(final DragSourceDragEvent dsde) {
             // not needed
         }
 
         @Override
         public void dropActionChanged(final DragSourceDragEvent dsde) {
             // not needed
         }
 
         @Override
         public void dragExit(final DragSourceEvent dse) {
             // not needed
         }
 
         @Override
         public void dragDropEnd(final DragSourceDropEvent dsde) {
             // not needed
         }
 
         @Override
         public void dragEnter(final DropTargetDragEvent dtde) {
             // not needed
         }
 
         @Override
         public void dragOver(final DropTargetDragEvent dtde) {
             // not needed
         }
 
         @Override
         public void dropActionChanged(final DropTargetDragEvent dtde) {
             // not needed
         }
 
         @Override
         public void dragExit(final DropTargetEvent dte) {
             // not needed
         } // </editor-fold>
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient DragSource dragSource;
         private final transient DropTarget dropTarget;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new DnDJXTable object.
          */
         public DnDJXTable() {
             dropTarget = new DropTarget(this, DnDConstants.ACTION_MOVE, this);
             dragSource = DragSource.getDefaultDragSource();
             dragSource.createDefaultDragGestureRecognizer(
                 this,                     // component where drag originates
                 DnDConstants.ACTION_MOVE, // actions
                 this);                    // drag gesture recognizer
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void drop(final DropTargetDropEvent dtde) {
             try {
                 final Object o = dtde.getTransferable().getTransferData(
                         CidsUserGroupTransferable.CIDS_UG_FLAVOR);
                 if (o instanceof Object[]) {
                     final Object[] dropped = (Object[])o;
                     if (dropped[0].equals(this)) {
                         return;
                     }
                     final Integer[] indices = (Integer[])dropped[1];
                     for (final int index : indices) {
                         addIndex(index);
                     }
                     updateGroupList();
                 }
             } catch (final Exception ex) {
                 LOG.error("could not perform drop action", ex); // NOI18N
                 // TODO: shall I inform the user...
             } finally {
                 dtde.dropComplete(true);
             }
         }
 
         @Override
         public void dragGestureRecognized(final DragGestureEvent dge) {
             final List ugs = new ArrayList(getSelectedRowCount());
             final List indices = new ArrayList(getSelectedRowCount());
             for (final int row : getSelectedRows()) {
                 ugs.add(getValueAt(row, 0));
                 indices.add(convertRowIndexToModel(row));
             }
             final Object object = new Object[] {
                     this,
                     indices.toArray(new Integer[indices.size()]),
                     ugs.toArray(new UserGroup[ugs.size()])
                 };
             final Transferable t = new CidsUserGroupTransferable(object);
             dragSource.startDrag(dge, Cursor.getPredefinedCursor(
                     Cursor.MOVE_CURSOR), t, this);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class DnDJXList extends JXList implements DropTargetListener, DragGestureListener, DragSourceListener {
 
         //~ Methods ------------------------------------------------------------
 
         // <editor-fold defaultstate="collapsed" desc=" Not needed Listener impls ">
         @Override
         public void dragEnter(final DragSourceDragEvent dsde) {
             // not needed
         }
 
         @Override
         public void dragOver(final DragSourceDragEvent dsde) {
             // not needed
         }
 
         @Override
         public void dropActionChanged(final DragSourceDragEvent dsde) {
             // not needed
         }
 
         @Override
         public void dragExit(final DragSourceEvent dse) {
             // not needed
         }
 
         @Override
         public void dragDropEnd(final DragSourceDropEvent dsde) {
             // not needed
         }
 
         @Override
         public void dragEnter(final DropTargetDragEvent dtde) {
             // not needed
         }
 
         @Override
         public void dragOver(final DropTargetDragEvent dtde) {
             // not needed
         }
 
         @Override
         public void dropActionChanged(final DropTargetDragEvent dtde) {
             // not needed
         }
 
         @Override
         public void dragExit(final DropTargetEvent dte) {
             // not needed
         } // </editor-fold>
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient DragSource dragSource;
         private final transient DropTarget dropTarget;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new DnDJXList object.
          */
         public DnDJXList() {
             dropTarget = new DropTarget(this, DnDConstants.ACTION_MOVE, this);
             dragSource = DragSource.getDefaultDragSource();
             dragSource.createDefaultDragGestureRecognizer(
                 this,                     // component where drag originates
                 DnDConstants.ACTION_MOVE, // actions
                 this);                    // drag gesture recognizer
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void drop(final DropTargetDropEvent dtde) {
             try {
                 final Object o = dtde.getTransferable().getTransferData(
                         CidsUserGroupTransferable.CIDS_UG_FLAVOR);
                 if (o instanceof Object[]) {
                     final Object[] dropped = (Object[])o;
                     if (dropped[0].equals(this)) {
                         return;
                     }
                     final Integer[] indices = (Integer[])dropped[1];
                     final UserGroup[] ugs = (UserGroup[])dropped[2];
                     final DefaultTableModel tModel = (DefaultTableModel)tblRights.getModel();
                     for (final int index : indices) {
                         tModel.removeRow(index);
                     }
                     updateGroupList();
                 }
             } catch (final Exception ex) {
                 LOG.error("could not perform drop action", ex); // NOI18N
                 // TODO: shall I inform the user...
             } finally {
                 dtde.dropComplete(true);
             }
         }
 
         @Override
         public void dragGestureRecognized(final DragGestureEvent dge) {
             final List indices = new ArrayList(getSelectedIndices().length);
             for (final int index : getSelectedIndices()) {
                 indices.add(convertIndexToModel(index));
             }
             final Object[] o = getSelectedValues();
             final UserGroup[] ugs = new UserGroup[o.length];
             for (int i = 0; i < o.length; i++) {
                 ugs[i] = (UserGroup)o[i];
             }
             final Object object = new Object[] {
                     this,
                     indices.toArray(new Integer[indices.size()]),
                     ugs
                 };
             final Transferable t = new CidsUserGroupTransferable(object);
             dragSource.startDrag(dge, Cursor.getPredefinedCursor(
                     Cursor.MOVE_CURSOR), t, this);
         }
     }
 }
