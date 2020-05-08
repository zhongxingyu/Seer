 /*
  * Copyright (c) 2012 Alvin R. de Leon. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.piraso.ui.base;
 
 import org.piraso.api.entry.Entry;
 import org.piraso.api.entry.RequestEntry;
 import org.piraso.api.entry.ResponseEntry;
 import org.piraso.ui.api.manager.FontProviderManager;
 import org.piraso.ui.api.manager.ModelEvent;
 import org.piraso.ui.api.manager.ModelOnChangeListener;
 import org.piraso.ui.api.manager.SingleModelManagers;
 import org.piraso.ui.api.util.SingleClassInstanceContent;
 import org.piraso.ui.api.util.WindowUtils;
 import org.piraso.ui.base.manager.EntryViewProviderManager;
 import org.piraso.io.IOEntryEvent;
 import org.piraso.io.IOEntryListener;
 import org.apache.commons.collections.CollectionUtils;
 import org.netbeans.api.settings.ConvertAsProperties;
 import org.openide.awt.ActionID;
 import org.openide.awt.ActionReference;
 import org.openide.util.*;
 import org.openide.util.NbBundle.Messages;
 import org.openide.util.lookup.AbstractLookup;
 import org.openide.util.lookup.InstanceContent;
 import org.openide.windows.TopComponent;
 
 import javax.swing.*;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.*;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.*;
 
 /**
  * Top component which displays something.
  */
 @ConvertAsProperties(dtd = "-//org.piraso.ui.base//RequestTree//EN", autostore = false)
 @TopComponent.Description(preferredID = "RequestTreeTopComponent", iconBase= "org/piraso/ui/base/icons/folders-explorer-icon.png", persistenceType = TopComponent.PERSISTENCE_ALWAYS)
 @TopComponent.Registration(mode = "explorer", openAtStartup = true)
 @ActionID(category = "Window", id = "org.piraso.ui.base.RequestTreeTopComponent")
@ActionReference(path = "Menu/Window")
 @TopComponent.OpenActionRegistration(displayName = "#CTL_RequestTreeAction",
 preferredID = "RequestTreeTopComponent")
 @Messages({
     "CTL_RequestTreeAction=Request Explorer",
     "CTL_RequestTreeTopComponent=Request Explorer",
     "HINT_RequestTreeTopComponent=This is a Request Explorer window"
 })
 public final class RequestTreeTopComponent extends TopComponent implements LookupListener {
 
     public static final long LATEST_TIME_THRESHOLD = 10000;
 
     public static final long SOME_HOW_LATEST_TIME_THRESHOLD = 30000;
 
 
     public static RequestTreeTopComponent get() {
         Set<TopComponent> opened = TopComponent.getRegistry().getOpened();
         for(TopComponent component : opened) {
             if(RequestTreeTopComponent.class.isInstance(component)) {
                 return (RequestTreeTopComponent) component;
             }
         }
 
         return null;
     }
 
     private final ModelOnChangeListener GENERAL_SETTINGS_LISTENER = new ModelOnChangeListener() {
         @Override
         public void onChange(ModelEvent evt) {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     repaint();
                     invalidate();
                 }
             });
         }
     };
 
 
     private DefaultMutableTreeNode root = new DefaultMutableTreeNode();
 
     private DefaultTreeModel model = new DefaultTreeModel(root);
 
     private SingleClassInstanceContent<Entry> entryContent;
 
     protected Lookup.Result result = null;
 
     protected ContextMonitorDelegate lastActiveMonitor;
 
     private final Set<Child> latestChildren = new HashSet<Child>();
     
     private final Map<ContextMonitorDelegate, ContextMonitorHandler> delegateHandlers = new HashMap<ContextMonitorDelegate, ContextMonitorHandler>();
 
     public RequestTreeTopComponent() {
         setName(Bundle.CTL_RequestTreeTopComponent());
         setToolTipText(Bundle.HINT_RequestTreeTopComponent());
 
         initComponents();
 
         InstanceContent content = new InstanceContent();
         associateLookup(new AbstractLookup(content));
 
         this.entryContent = new SingleClassInstanceContent<Entry>(content);
 
         jTree.setFont(FontProviderManager.INSTANCE.getEditorDefaultFont());
         jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         jTree.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 int selRow = jTree.getRowForLocation(e.getX(), e.getY());
                 TreePath selPath = jTree.getPathForLocation(e.getX(), e.getY());
                 if(selRow != -1 && selPath != null) {
                     DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                     Child child = null;
                     if(Child.class.isInstance(node.getUserObject())) {
                         child = (Child) node.getUserObject();
                     }
 
                     if(child != null && e.getClickCount() == 2) {
                         child.select();
                     }
                 }
             }
         });
 
         jTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
             @Override
             public void valueChanged(TreeSelectionEvent e) {
                 if(e.getPath() != null && e.getPath().getLastPathComponent() != null) {
                     DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                     if(node.getUserObject() != null && Child.class.isInstance(node.getUserObject())) {
                         Child child = (Child) node.getUserObject();
                         child.showRequestView();
                     }
                 }
             }
         });
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         jTree = new javax.swing.JTree();
         toolbar = new javax.swing.JToolBar();
         btnCollapse = new javax.swing.JButton();
         btnColorLatest = new javax.swing.JToggleButton();
         jSeparator1 = new javax.swing.JToolBar.Separator();
         btnTarget = new javax.swing.JButton();
 
         setLayout(new java.awt.BorderLayout());
 
         jTree.setModel(model);
         jTree.setCellRenderer(new TreeCellRenderer());
         jTree.setRootVisible(false);
         jTree.setShowsRootHandles(true);
         jScrollPane1.setViewportView(jTree);
 
         add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
         toolbar.setBackground(new java.awt.Color(226, 226, 226));
         toolbar.setFloatable(false);
         toolbar.setRollover(true);
         toolbar.setOpaque(false);
 
         btnCollapse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/piraso/ui/base/icons/gh-icon-collapse.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(btnCollapse, org.openide.util.NbBundle.getMessage(RequestTreeTopComponent.class, "RequestTreeTopComponent.btnCollapse.text")); // NOI18N
         btnCollapse.setToolTipText(org.openide.util.NbBundle.getMessage(RequestTreeTopComponent.class, "RequestTreeTopComponent.btnCollapse.toolTipText")); // NOI18N
         btnCollapse.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 7, 7, 7));
         btnCollapse.setFocusable(false);
         btnCollapse.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnCollapse.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnCollapse.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCollapseActionPerformed(evt);
             }
         });
         toolbar.add(btnCollapse);
 
         btnColorLatest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/piraso/ui/base/icons/time.png"))); // NOI18N
         btnColorLatest.setSelected(true);
         org.openide.awt.Mnemonics.setLocalizedText(btnColorLatest, org.openide.util.NbBundle.getMessage(RequestTreeTopComponent.class, "RequestTreeTopComponent.btnColorLatest.text")); // NOI18N
         btnColorLatest.setToolTipText(org.openide.util.NbBundle.getMessage(RequestTreeTopComponent.class, "RequestTreeTopComponent.btnColorLatest.toolTipText")); // NOI18N
         btnColorLatest.setFocusable(false);
         btnColorLatest.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnColorLatest.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnColorLatest.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnColorLatestActionPerformed(evt);
             }
         });
         toolbar.add(btnColorLatest);
         toolbar.add(jSeparator1);
 
         btnTarget.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/piraso/ui/base/icons/target_arrow.png"))); // NOI18N
         org.openide.awt.Mnemonics.setLocalizedText(btnTarget, org.openide.util.NbBundle.getMessage(RequestTreeTopComponent.class, "RequestTreeTopComponent.btnTarget.text")); // NOI18N
         btnTarget.setToolTipText(org.openide.util.NbBundle.getMessage(RequestTreeTopComponent.class, "RequestTreeTopComponent.btnTarget.toolTipText")); // NOI18N
         btnTarget.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 7, 7, 7));
         btnTarget.setFocusable(false);
         btnTarget.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnTarget.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnTarget.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnTargetActionPerformed(evt);
             }
         });
         toolbar.add(Box.createHorizontalGlue());
         toolbar.add(btnTarget);
 
         add(toolbar, java.awt.BorderLayout.NORTH);
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTargetActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 if(lastActiveMonitor == null || lastActiveMonitor.getSelectedRequest() == null) {
                     return;
                 }
 
                 if(!((TopComponent) lastActiveMonitor).isOpened()) {
                     lastActiveMonitor = null;
                     return;
                 }
 
                 RequestEntry entry = lastActiveMonitor.getSelectedRequest();
                 synchronized (delegateHandlers) {
                     ContextMonitorHandler handler = delegateHandlers.get(lastActiveMonitor);
 
                     Child child = handler.childMap.get(entry.getBaseRequestId());
                     if(child != null) {
                         TreePath path = new TreePath(child.node.getPath());
                         jTree.getSelectionModel().setSelectionPath(path);
                         jTree.expandPath(path);
                     }
                 }
             }
         });
     }//GEN-LAST:event_btnTargetActionPerformed
 
     private void btnColorLatestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnColorLatestActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 if(btnColorLatest.isSelected()) {
                     btnColorLatest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/piraso/ui/base/icons/time.png")));
                     btnColorLatest.setToolTipText(NbBundle.getMessage(ContextMonitorTopComponent.class, "RequestTreeTopComponent.btnColorLatest.toolTipText"));
                 } else {
                     btnColorLatest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/piraso/ui/base/icons/time-minus.png")));
                     btnColorLatest.setToolTipText(NbBundle.getMessage(ContextMonitorTopComponent.class, "RequestTreeTopComponent.btnColorLatest.unselected.toolTipText"));
                 }
 
                 if(!btnColorLatest.isSelected()) {
                     synchronized (latestChildren) {
                         refreshChildrenColor();
                     }
                 }
             }
         });
     }//GEN-LAST:event_btnColorLatestActionPerformed
 
     private void btnCollapseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCollapseActionPerformed
         model.nodeStructureChanged(root);
     }//GEN-LAST:event_btnCollapseActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCollapse;
     private javax.swing.JToggleButton btnColorLatest;
     private javax.swing.JButton btnTarget;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JTree jTree;
     private javax.swing.JToolBar toolbar;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public void componentOpened() {
         result = Utilities.actionsGlobalContext().lookupResult(ContextMonitorDelegate.class);
         result.addLookupListener(this);
         SingleModelManagers.GENERAL_SETTINGS.addModelOnChangeListener(GENERAL_SETTINGS_LISTENER);
     }
 
     @Override
     public void componentClosed() {
         result.removeLookupListener(this);
         result = null;
         SingleModelManagers.GENERAL_SETTINGS.removeModelOnChangeListener(GENERAL_SETTINGS_LISTENER);
     }
 
     void writeProperties(java.util.Properties p) {
         p.setProperty("version", "1.0");
     }
 
     void readProperties(java.util.Properties p) {
         String version = p.getProperty("version");
     }
 
     public ContextMonitorHandler createHandler(ContextMonitorDelegate delegate) {
         synchronized(delegateHandlers) {
             ContextMonitorHandler monitor = new ContextMonitorHandler(delegate);
 
             delegateHandlers.put(delegate, monitor);
 
             return monitor;
         }
     }
 
     public void refreshChildrenColor() {
         Iterator<Child> itr = latestChildren.iterator();
 
         while(itr.hasNext()) {
             Child child = itr.next();
 
             if(!child.isSomeHowLatest()) {
                 model.nodeChanged(child.node);
                 itr.remove();
             }
         }
     }
 
     public void refreshChildrenColorToRemove(DefaultMutableTreeNode parent) {
         Iterator<Child> itr = latestChildren.iterator();
 
         while(itr.hasNext()) {
             Child child = itr.next();
 
             if(child.node.getParent().equals(parent)) {
                 itr.remove();
             }
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void resultChanged(LookupEvent evt) {
         Lookup.Result<ContextMonitorDelegate> r = (Lookup.Result<ContextMonitorDelegate>) evt.getSource();
         Collection<? extends ContextMonitorDelegate> entries = r.allInstances();
 
         if(CollectionUtils.isNotEmpty(entries)) {
             lastActiveMonitor = entries.iterator().next();
         }
     }
 
     public class ContextMonitorHandler implements IOEntryListener {
 
         private ContextMonitorDelegate delegate;
 
         private DefaultMutableTreeNode node;
 
         private Parent parent;
 
         private Map<Long, Child> childMap = new HashMap<Long, Child>();
 
         private boolean closed = false;
 
         public ContextMonitorHandler(ContextMonitorDelegate delegate) {
             this.delegate = delegate;
             parent = new Parent(delegate);
             node = new DefaultMutableTreeNode(parent);
             node.setAllowsChildren(true);
             root.add(node);
             
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     model.nodeStructureChanged(root);
                 }
             });
         }
 
         @Override
         public void started(IOEntryEvent evt) {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     parent.start();
                     model.nodeChanged(node);
                 }
             });
         }
 
         @Override
         public void stopped(IOEntryEvent evt) {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     synchronized (this) {
                         if(!closed) {
                             parent.stop();
                             model.nodeChanged(node);
                         }
                     }
                 }
             });
         }
 
         @Override
         public void receivedEntry(IOEntryEvent evt) {
             final Entry entry = evt.getEntry().getEntry();
 
             if(RequestEntry.class.isInstance(entry)) {
                 final Child child = new Child(delegate, (RequestEntry) entry);
                 final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
                 childNode.setAllowsChildren(false);
 
                 child.setNode(childNode);
                 node.add(childNode);
 
                 SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                         child.start();
 
                         childMap.put(entry.getBaseRequestId(), child);
 
                         synchronized (this) {
                             if(!closed) {
                                 model.nodeStructureChanged(node);
                             }
                         }
                         if(node.getChildCount() > 0) {
                             jTree.expandPath(new TreePath(node.getPath()));
                         }
                     }
                 });
             } else if(ResponseEntry.class.isInstance(entry)) {
                 SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                         Child child = childMap.get(entry.getBaseRequestId());
 
                         if(child != null) {
                             child.stop();
                             synchronized (this) {
                                 if(!closed) {
                                     model.nodeChanged(child.node);
                                 }
                             }
                         }
                     }
                 });
             }
 
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     if(btnColorLatest.isSelected()) {
                         synchronized (latestChildren) {
                             Child child = childMap.get(entry.getBaseRequestId());
                             if(child != null) {
                                 child.touch();
                                 latestChildren.add(child);
                                 refreshChildrenColor();
                             }
                         }
                     }
                 }
             });
         }
 
         public void reset() {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     synchronized (this) {
                         refreshChildrenColorToRemove(node);
                         node.removeAllChildren();
                         model.nodeStructureChanged(node);
                     }
                 }
             });
         }
 
         public void close() {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     synchronized (this) {
                         refreshChildrenColorToRemove(node);
                         root.remove(node);
                         model.nodeStructureChanged(root);
                         closed = true;
                         
                         synchronized(delegateHandlers) {
                             delegateHandlers.remove(delegate);
                         }
                     }
                 }
             });
         }
     }
 
     public class Parent {
         private ContextMonitorDelegate delegate;
 
         private boolean alive;
 
         public Parent(ContextMonitorDelegate delegate) {
             this.delegate = delegate;
             alive = false;
         }
 
         public void start() {
             alive = true;
         }
 
         public void stop() {
             alive = false;
         }
 
         @Override
         public String toString() {
             return delegate.getName();
         }
     }
 
     public final class Child {
         private ContextMonitorDelegate delegate;
 
         private boolean done;
 
         private DefaultMutableTreeNode node;
 
         private RequestEntry entry;
         
         private long timestamp;
 
         public Child(ContextMonitorDelegate delegate, RequestEntry entry) {
             this.delegate = delegate;
             this.entry = entry;
             done = false;
             touch();
         }
         
         public void touch() {
             timestamp = System.currentTimeMillis();
         }
         
         public boolean isLatest() {
             return btnColorLatest.isSelected() && System.currentTimeMillis() - timestamp <= LATEST_TIME_THRESHOLD;
         }
 
         public boolean isSomeHowLatest() {
             return btnColorLatest.isSelected() && System.currentTimeMillis() - timestamp <= SOME_HOW_LATEST_TIME_THRESHOLD;
         }
 
         public void setNode(DefaultMutableTreeNode node) {
             this.node = node;
         }
 
         public void start() {
             done = false;
         }
 
         public void showRequestView() {
             Class<? extends TopComponent> viewClass = EntryViewProviderManager.INSTANCE.getViewClass(entry);
             entryContent.add(entry);
 
             if(viewClass != null) {
                 WindowUtils.selectWindow(viewClass);
             }
         }
 
         public void select() {
             delegate.selectRequest(entry);
             showRequestView();
         }
 
         public void stop() {
             done = true;
         }
 
         @Override
         public String toString() {
             return entry.toString();
         }
     }
 
     public class TreeCellRenderer extends DefaultTreeCellRenderer {
         public static final String STOPPED_ICON_PATH = "org/piraso/ui/base/icons/status-stopped.png";
 
         public static final String STARTED_ICON_PATH = "org/piraso/ui/base/icons/status-active.png";
 
         public static final String DONE_ICON_PATH = "org/piraso/ui/base/icons/tick.png";
 
         public static final String IN_PROGRESS_ICON_PATH = "org/piraso/ui/base/icons/ui-progress-bar-icon.png";
 
         @Override
         public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
             JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
 
             if(sel) {
                 label.setForeground(Color.WHITE);
             } else {
                 label.setForeground(Color.BLACK);
             }
 
             DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
             if(Parent.class.isInstance(node.getUserObject())) {
                 Parent parent = (Parent) node.getUserObject();
 
                 if(parent.alive) {
                     label.setIcon(ImageUtilities.loadImageIcon(STARTED_ICON_PATH, true));
                 } else {
                     label.setIcon(ImageUtilities.loadImageIcon(STOPPED_ICON_PATH, true));
                 }
 
                 label.setFont(FontProviderManager.INSTANCE.getBoldEditorDefaultFont());
             }
 
             if(Child.class.isInstance(node.getUserObject())) {
                 Child child = (Child) node.getUserObject();
 
                 if(child.done) {
                     label.setIcon(ImageUtilities.loadImageIcon(DONE_ICON_PATH, true));
                 } else {
                     label.setIcon(ImageUtilities.loadImageIcon(IN_PROGRESS_ICON_PATH, true));
                 }
 
                 label.setFont(FontProviderManager.INSTANCE.getEditorDefaultFont());
 
                 if(child.isLatest()) {
                     if(sel) {
                         label.setForeground(new Color(0xBAEEBA));
                     } else {
                         label.setForeground(new Color(0x008000));
                     }
                 } else if(child.isSomeHowLatest()) {
                     if(sel) {
                         label.setForeground(new Color(0xE2FAFF));
                     } else {
                         label.setForeground(new Color(0x000080));
                     }
                 }
             }
 
             return label;
         }
     }
 }
