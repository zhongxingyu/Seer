 package org.eclipse.riena.navigation.ui.swt.component;
 
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.ISubModuleNodeListener;
 import org.eclipse.riena.navigation.model.SubModuleNodeAdapter;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 import org.eclipse.riena.ui.swt.utils.ImageUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.TreeEvent;
 import org.eclipse.swt.events.TreeListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 public class SubModuleNavigationComponent extends AbstractNavigationComponent<ISubModuleNode> {
 
 	private TreeItem treeItem;
 
 	// observe model
 	private ISubModuleNodeListener subModuleObserver;
 	// observe ui
 	private Listener treeUiObserver;
 
 	private TreeItem parentTreeItem;
 
 	public SubModuleNavigationComponent(ISubModuleNode node, Tree tree) {
 		this(node, tree, null);
 	}
 
 	public SubModuleNavigationComponent(ISubModuleNode node, Tree tree, TreeItem parentItem) {
 		super(node, tree);
 		tree.addTreeListener(new ExpandTreeListener());
 		this.parentTreeItem = parentItem;
 		initUI();
 		buildInitialTree();
 		updateAcitivity();
 	}
 
 	private void updateAcitivity() {
 		if (getModelNode().isActivated()) {
 			getTree().showItem(getTreeItem());
 			getTree().setSelection(getTreeItem());
 		}
 	}
 
 	protected TreeItem getParentTreeItem() {
 		return parentTreeItem;
 	}
 
 	@Override
 	protected void buildInitialTree() {
 		for (ISubModuleNode subNode : getModelNode().getChildren()) {
 			createSubModuleComponent(subNode);
 		}
 	}
 
 	private void createSubModuleComponent(ISubModuleNode subNode) {
 		new SubModuleNavigationComponent(subNode, getTree(), getTreeItem());
 	}
 
 	protected TreeItem getTreeItem() {
 		return treeItem;
 	}
 
 	private final class SubModuleNodeObserver extends SubModuleNodeAdapter {
 
 		@Override
 		public void activated(ISubModuleNode source) {
 			updateAcitivity();
 		}
 
 		@Override
 		public void childAdded(ISubModuleNode source, ISubModuleNode child) {
 			createSubModuleComponent(child);
 		}
 	}
 
 	private final class TreeUIObserver implements Listener {
 
 		public void handleEvent(Event event) {
			Tree tree = getTree();
			/*
			 * The focus check is necessary because of a windows/swt "feature".
			 * Sometimes we get a bogus selection event on resize, which can be
			 * identified because the tree has no focus. If the user clicks on
			 * the tree it, the tree will be the focus control.
			 */
			if (tree.isFocusControl()) {
				TreeItem[] selection = tree.getSelection();
				if (selection != null && selection.length > 0) {
					Object data = selection[0].getData();
					if (data.equals(getModelNode())) {
						nodeSelected();
					}
				}
 			}
 		}
 	}
 
 	@Override
 	protected void initObserver() {
 		subModuleObserver = new SubModuleNodeObserver();
 		getModelNode().addListener(subModuleObserver);
 		treeUiObserver = new TreeUIObserver();
 		getTree().addListener(SWT.Selection, treeUiObserver);
 	}
 
 	protected Tree getTree() {
 		return (Tree) getParentComposite();
 	}
 
 	@Override
 	protected void initUI() {
 		RienaDefaultLnf lnf = LnfManager.getLnf();
 		if (getParentTreeItem() != null) {
 			treeItem = new TreeItem(getParentTreeItem(), SWT.BORDER);
 			if (getParentTreeItem().getItemCount() == 1) {
 				updateItemImage(getParentTreeItem());
 			}
 		} else {
 			treeItem = new TreeItem(getTree(), SWT.BORDER);
 		}
 		treeItem.setText(getModelNode().getLabel());
 		treeItem.setForeground(lnf.getColor(ILnfKeyConstants.SUB_MODULE_TREE_ITEM_FOREGROUND));
 		treeItem.setBackground(lnf.getColor(ILnfKeyConstants.SUB_MODULE_TREE_ITEM_BACKGROUND));
 		treeItem.setData(getModelNode());
 		updateItemImage(treeItem);
 	}
 
 	/**
 	 * Sets the image of the sub-module for the given tree item
 	 * 
	 * @param item
	 *            - tree item
 	 */
 	private void setSubModuleImage(TreeItem item) {
 
 		Image image = null;
 		if (getModelNode().getIcon() != null) {
 			image = ImageUtil.getImage(getModelNode().getIcon());
 			if (image != null) {
 				item.setImage(image);
 			}
 		}
 
 	}
 
 	public void nodeSelected() {
 		getModelNode().activate();
 	}
 
 	@Override
 	Composite getUI() {
 		return getTree();
 	}
 
 	/**
 	 * Updates the image of a folder in the tree.
 	 * 
	 * @param item
	 *            - item to be updated.
 	 */
 	public void updateItemImage(TreeItem item) {
 
 		updateItemImage(item, item.getExpanded());
 
 	}
 
 	/**
 	 * Updates the image of a folder in the tree.
 	 * 
	 * @param item
	 *            - item to be updated.
 	 */
 	public void updateItemImage(TreeItem item, boolean expanded) {
 
 		RienaDefaultLnf lnf = LnfManager.getLnf();
 		String folderIcon = ILnfKeyConstants.SUB_MODULE_TREE_FOLDER_CLOSED_ICON;
 		if (item.getItemCount() > 0) {
 			folderIcon = ILnfKeyConstants.SUB_MODULE_TREE_FOLDER_CLOSED_ICON;
 			if (expanded) {
 				folderIcon = ILnfKeyConstants.SUB_MODULE_TREE_FOLDER_OPEN_ICON;
 			}
 		} else {
 			folderIcon = ILnfKeyConstants.SUB_MODULE_TREE_DOCUMENT_LEAF_ICON;
 		}
 		Image image = lnf.getImage(folderIcon);
 		item.setImage(image);
 
 		// setSubModuleImage(item);
 
 	}
 
 	/**
 	 * After a tree branch is collapsed or expanded the images of the tree item
 	 * must be updated.
 	 */
 	private class ExpandTreeListener implements TreeListener {
 
 		/**
 		 * @see org.eclipse.swt.events.TreeListener#treeCollapsed(org.eclipse.swt.events.TreeEvent)
 		 */
 		public void treeCollapsed(TreeEvent e) {
 			updateItemImage((TreeItem) e.item, false);
 		}
 
 		/**
 		 * @see org.eclipse.swt.events.TreeListener#treeExpanded(org.eclipse.swt.events.TreeEvent)
 		 */
 		public void treeExpanded(TreeEvent e) {
 			updateItemImage((TreeItem) e.item, true);
 		}
 
 	}
 
 }
