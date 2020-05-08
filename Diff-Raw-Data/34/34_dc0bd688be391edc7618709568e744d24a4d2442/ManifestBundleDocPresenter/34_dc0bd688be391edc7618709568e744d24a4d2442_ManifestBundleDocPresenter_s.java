 package kkckkc.jsourcepad.bundleeditor.manifest;
 
 import com.google.common.collect.*;
 import kkckkc.jsourcepad.bundleeditor.BasicBundleDocPresenter;
 import kkckkc.jsourcepad.bundleeditor.model.BundleDocImpl;
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.bundle.Bundle;
 import kkckkc.jsourcepad.model.bundle.BundleItemSupplier;
 import kkckkc.jsourcepad.model.bundle.BundleListener;
 import kkckkc.jsourcepad.model.bundle.BundleStructure;
 import kkckkc.jsourcepad.util.messagebus.DispatchStrategy;
 import kkckkc.jsourcepad.util.ui.JTreeUtils;
 
 import javax.swing.*;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.*;
 
 public class ManifestBundleDocPresenter extends BasicBundleDocPresenter {
 
     @Override
     protected void saveCallback() {
         super.saveCallback();
 
         Map mainMenu = Maps.newHashMap();
         save(mainMenu, (DefaultMutableTreeNode) ((ManifestBundleDocViewImpl) view).getMenu().getModel().getRoot());
 
         BundleDocImpl bDoc = (BundleDocImpl) doc;
         bDoc.getPlist().put("mainMenu", mainMenu);
     }
 
     private void save(Map dest, DefaultMutableTreeNode root) {
         List items = Lists.newArrayList();
         Map submenus = Maps.newHashMap();
         for (int i = 0; i < root.getChildCount(); i++) {
             DefaultMutableTreeNode n = (DefaultMutableTreeNode) root.getChildAt(i);
             TreeEntry v = (TreeEntry) n.getUserObject();
 
             if (v.isFolder()) {
                 Map s = Maps.newHashMap();
                 s.put("name", v.getValue());
                 submenus.put(v.getKey(), s);
                 save(s, n);
 
             }
 
             items.add(v.getKey());
         }
 
         dest.put("items", items);
         dest.put("submenus", submenus);
     }
 
     @Override
     public void init() {
         super.init();
 
         final ManifestBundleDocViewImpl mView = (ManifestBundleDocViewImpl) view;
         final BundleDocImpl bDoc = (BundleDocImpl) doc;
         final Bundle bundle = Application.get().getBundleManager().getBundle(bDoc.getName());
 
         Application.get().topic(BundleListener.class).subscribe(DispatchStrategy.ASYNC_EVENT, new BundleListener() {
 
             @Override
             public void bundleAdded(Bundle bundle) {
             }
 
             @Override
             public void bundleRemoved(Bundle bundle) {
             }
 
             @Override
             public void bundleUpdated(Bundle bundle) {
                 DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeEntry("", "Menu Structure", true));
                 DefaultTreeModel menuModel = new TreeEntryTreeModel(root);
 
                 buildMenu(root, (Map) bDoc.getPlist().get("mainMenu"), bundle.getItemsByUuid());
 
                 JTreeUtils.mergeModels((DefaultTreeModel) mView.getMenu().getModel(), menuModel, new TreeEntryMerger());
 
 
                 root = new DefaultMutableTreeNode("Available Items");
                 DefaultTreeModel availableModel = new TreeEntryTreeModel(root);
 
                 Set<String> usedUuids = Sets.newHashSet();
                 addAllUuids(usedUuids, (DefaultMutableTreeNode) menuModel.getRoot());
                 buildAvailable(root, bundle, usedUuids);
 
                 JTreeUtils.mergeModels((DefaultTreeModel) mView.getAvailable().getModel(), availableModel, new TreeEntryMerger());
             }
 
             @Override
             public void languagesUpdated() { }
         });
 
 
         DefaultTreeModel menuModel = initMenu(mView, bDoc, bundle);
         initAvailable(mView, menuModel, bundle);
 
         mView.getMenu().getModel().addTreeModelListener(new TreeModelListener() {
 
             @Override
             public void treeNodesChanged(TreeModelEvent e) {
                 BundleDocImpl bDoc = (BundleDocImpl) doc;
                 bDoc.setModified(true);
             }
 
             @Override
             public void treeNodesInserted(TreeModelEvent e) {
                 BundleDocImpl bDoc = (BundleDocImpl) doc;
                 bDoc.setModified(true);
             }
 
             @Override
             public void treeNodesRemoved(TreeModelEvent e) {
                 BundleDocImpl bDoc = (BundleDocImpl) doc;
                 bDoc.setModified(true);
             }
 
             @Override
             public void treeStructureChanged(TreeModelEvent e) {
                 BundleDocImpl bDoc = (BundleDocImpl) doc;
                 bDoc.setModified(true);
             }
         });
     }
 
     private void initAvailable(ManifestBundleDocViewImpl mView, DefaultTreeModel menuModel, Bundle bundle) {
         JTree available = mView.getAvailable();
 
         DefaultMutableTreeNode root = new DefaultMutableTreeNode("Available Items");
         DefaultTreeModel availableModel = new TreeEntryTreeModel(root);
         available.setModel(availableModel);
 
         Set<String> usedUuids = Sets.newHashSet();
         addAllUuids(usedUuids, (DefaultMutableTreeNode) menuModel.getRoot());
         buildAvailable(root, bundle, usedUuids);
 
         available.expandPath(new TreePath(new Object[] { root }));
         available.setDragEnabled(true);
         available.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         available.setTransferHandler(new ManifestAvailableTransferHandler(availableModel, bundle));
     }
 
     private DefaultTreeModel initMenu(ManifestBundleDocViewImpl mView, BundleDocImpl bDoc, Bundle bundle) {
         final JTree menu = mView.getMenu();
         DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeEntry("", "Menu Structure", true));
         final DefaultTreeModel menuModel = new TreeEntryTreeModel(root);
         menu.setDragEnabled(true);
         menu.setDropMode(DropMode.INSERT);
         menu.setTransferHandler(new ManifestMenuTransferHandler(menuModel, bundle));
 
         buildMenu(root, (Map) bDoc.getPlist().get("mainMenu"), bundle.getItemsByUuid());
         
         menu.setModel(menuModel);
         menu.expandPath(new TreePath(new Object[] { root }));
 
         menu.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() == 2 && menu.getSelectionPath() != null) {
                     Object userObject = ((DefaultMutableTreeNode) menu.getSelectionPath().getLastPathComponent()).getUserObject();
                     if (userObject instanceof TreeEntry && ((TreeEntry) userObject).isFolder()) {
                         String value = JOptionPane.showInputDialog("Enter new name:");
 
                         if (value != null) {
                             ((TreeEntry) userObject).setValue(value);
                             menuModel.nodeChanged(((DefaultMutableTreeNode) menu.getSelectionPath().getLastPathComponent()));
                         }
                     }
                 } else {
                     super.mouseClicked(e);
                 }
             }
         });
 
         return menuModel;
     }
 
 
     private void addAllUuids(Set<String> dest, DefaultMutableTreeNode parent) {
         Object o = parent.getUserObject();
         if (o instanceof TreeEntry) {
             dest.add(((TreeEntry) o).getKey());
         }
 
         for (int i = 0; i < parent.getChildCount(); i++) {
             addAllUuids(dest, (DefaultMutableTreeNode) parent.getChildAt(i));
         }
     }
 
     private void buildAvailable(DefaultMutableTreeNode root, Bundle bundle, Set<String> usedUuids) {
         root.add(new DefaultMutableTreeNode(new TreeEntry("", "New Submenu", true)));
         root.add(new DefaultMutableTreeNode(new TreeEntry("------------------------------------", "------------------------------------", false)));
 
         Multimap<BundleStructure.Type, TreeEntry> entries = HashMultimap.create();
         for (Map.Entry<String, BundleItemSupplier> entry : bundle.getItemsByUuid().entrySet()) {
             BundleItemSupplier bis = entry.getValue();
             if (! usedUuids.contains(bis.getUUID()))
                 entries.put(bis.getType(), new TreeEntry(bis.getUUID(), bis.getName(), false));
         }
 
         for (BundleStructure.Type type : Arrays.asList(BundleStructure.Type.COMMAND, BundleStructure.Type.MACRO, BundleStructure.Type.SNIPPET)) {
             if (! entries.containsKey(type)) continue;
 
             List<TreeEntry> l = new ArrayList<TreeEntry>(entries.get(type));
             Collections.sort(l, new TreeEntry.TreeEntryComparator());
 
             DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(new TreeEntry("", type.getFolder(), true));
             root.add(typeNode);
             for (TreeEntry s : l) {
                 typeNode.add(new DefaultMutableTreeNode(s));
             }
         }
     }
 
     private void buildMenu(DefaultMutableTreeNode parent, Map map, Map<String, BundleItemSupplier> itemsByUuid) {
         Map<String, Map> submenus = (Map<String, Map>) map.get("submenus");
         if (submenus == null) submenus = Maps.newHashMap();
 
         List<String> items = (List<String>) map.get("items");
        for (String s : items) {
            if (submenus.containsKey(s)) {
                Map sm = submenus.get(s);
                DefaultMutableTreeNode newParent = new DefaultMutableTreeNode(new TreeEntry(s, (String) sm.get("name"), true), true);
                parent.add(newParent);
                buildMenu(newParent, sm, itemsByUuid);
            } else if (s.startsWith("----")) {
                parent.add(new DefaultMutableTreeNode(new TreeEntry(s, s, false), false));
            } else {
                String name = itemsByUuid.get(s).getName();
                parent.add(new DefaultMutableTreeNode(new TreeEntry(s, name, false), false));
             }
         }
     }
 
 
 }
