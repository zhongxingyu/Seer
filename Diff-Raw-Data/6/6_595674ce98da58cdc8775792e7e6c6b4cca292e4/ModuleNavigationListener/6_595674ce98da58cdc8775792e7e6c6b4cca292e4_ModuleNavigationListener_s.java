 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.TypedEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 import org.eclipse.riena.core.util.Nop;
 import org.eclipse.riena.core.util.RAPDetector;
 import org.eclipse.riena.internal.navigation.ui.swt.handlers.SwitchModule;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.model.NavigationProcessor;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 
 /**
  * Navigation for the 'submodule' tree used in {@link ModuleView}. This includes both mouse navigation (selection) and keyboard navigation (key down / up).
  * <p>
  * This class takes care of the following cases:
  * <ul>
  * <li>node clicked / selection change: activate the node</li>
  * <li>arrow up / down: activate the node</li>
  * <li>arrow up on the first node: activate the previous module, wrapping around to the last, if there is no previous one</li>
  * <li>arrow down on the last node: activate the next module, wrapping around to the 1st, if there is no next one</li>
  * </ul>
  * <p>
  * Some submodule nodes can be flagged as non-selecteble. If such a node is activated, then the first selectable child will be selected. This is done by
  * {@link NavigationProcessor}. Moving over such a node is not problem anymore, since the activation is only triggered after a delay - not instantly.
  * 
  * @since 3.0
  */
 public class ModuleNavigationListener extends SelectionAdapter implements KeyListener, FocusListener {
 
 	/** Keycode for 'arrow down' (16777218) */
 	private static final int KC_ARROW_DOWN = 16777218;
 	/** Keycode for 'arrow up' (16777217) */
 	private static final int KC_ARROW_UP = 16777217;
 
 	private volatile boolean keyPressed;
 	private int keyCode;
 
 	private NodeSwitcher nodeSwitcher;
 	private boolean isFirst;
 	private boolean isLast;
 
 	public ModuleNavigationListener(final Tree moduleTree) {
 		moduleTree.addKeyListener(this);
 		moduleTree.addSelectionListener(this);
 		moduleTree.addFocusListener(this);
 	}
 
 	@Override
 	public void widgetSelected(final SelectionEvent event) {
 		cancelSwitch();
 		if (!keyPressed) {
 			// selected by mouse click
 			startSwitch(getSelection(event));
 		} else {
 			// key is down and we must check if the tree node is selectable
 			final Tree tree = (Tree) event.widget;
 			final TreeItem[] sel = tree.getSelection();
 			if (sel.length > 0) {
 				TreeItem item = sel[0];
 				if (!isSelectable(item)) {
 					// if item is not selectable find the next selectable item in the same direction
 					if (keyCode == SWT.ARROW_UP || keyCode == SWT.ARROW_LEFT) {
 						item = findPrevious(item);
 					} else if (keyCode == SWT.ARROW_DOWN || keyCode == SWT.ARROW_RIGHT) {
 						item = findNext(item);
 					}
 					if (item != null) {
 						item.getParent().select(item);
 					}
 					// if item == null then the code in keyRelease will expand the
 					// unselectable item and select the first child
 				}
 			}
 		}
 	}
 
 	public void keyPressed(final KeyEvent event) {
 		cancelSwitch();
 		blockLetterOrDigit(event);
 		keyPressed = true;
 		keyCode = event.keyCode;
 		final TreeItem from = getSelection(event);
 		isFirst = isFirst(from);
 		isLast = isLast(from);
 	}
 
 	public void keyReleased(final KeyEvent event) {
 		blockLetterOrDigit(event);
 		// plain arrow up
 		if (event.stateMask == 0 && KC_ARROW_UP == event.keyCode && isFirst) {
 			createSwitchModuleOp().doSwitch(false);
 			// plain arrow down
 		} else if (event.stateMask == 0 && KC_ARROW_DOWN == event.keyCode && isLast) {
 			createSwitchModuleOp().doSwitch(true);
 		} else {
 			startSwitch(getSelection(event));
 		}
 		keyPressed = false;
 	}
 
 	private void blockLetterOrDigit(final KeyEvent e) {
 		if (Character.isLetterOrDigit(e.character) && !isCharacterNavigationEnabled()) {
 			e.doit = false;
 		}
 	}
 
 	private boolean isCharacterNavigationEnabled() {
 		return LnfManager.getLnf().getBooleanSetting(LnfKeyConstants.NAVIGATION_TREE_CHARACTER_SELECTION_ENABLED, false);
 	}
 
 	public void focusGained(final FocusEvent e) {
 		// unused
 	}
 
 	public void focusLost(final FocusEvent e) {
 		// reset key pressed state when focus is removed from the tree
 		keyPressed = false;
 	}
 
 	// helping methods
 	//////////////////
 
 	private void cancelSwitch() {
 		if (nodeSwitcher != null) {
 			nodeSwitcher.cancel();
 		}
 	}
 
 	private SwitchModule createSwitchModuleOp() {
 		final SwitchModule result = new SwitchModule();
 		result.setActivateSubmodule(true);
 		return result;
 	}
 
 	private TreeItem findLast(final Tree tree) {
 		if (tree.isDisposed()) {
 			return null;
 		}
 		final TreeItem[] items = tree.getItems();
 		return items.length > 0 ? findLast(items[items.length - 1]) : null;
 	}
 
 	private TreeItem findLast(final TreeItem item) {
 		TreeItem result = item;
 		if (item.getExpanded() && item.getItemCount() > 0) {
 			final TreeItem last = item.getItem(item.getItemCount() - 1);
 			result = findLast(last);
 		}
 		return result;
 	}
 
 	private static TreeItem findItem(final TreeItem[] items, final INavigationNode<?> source) {
 		for (final TreeItem item : items) {
 			if (item.getData() == source) {
 				return item;
 			}
 			final TreeItem result = item.getItemCount() > 0 ? findItem(item.getItems(), source) : null;
 			if (result != null) {
 				return result;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Find the first selectable TreeItem below {@code item} in the tree. Will consider all available (=expanded) tree items.
 	 * 
 	 * @return a TreeItem, may be null
 	 */
 	private TreeItem findNext(final TreeItem item) {
 		final List<TreeItem> siblings = sequentialize(item.getParent().getItems());
 		final int index = siblings.indexOf(item);
 		TreeItem result = index != -1 && index < siblings.size() - 1 ? siblings.get(index + 1) : null;
 		if (result != null && !isSelectable(result)) {
 			result = findNext(result);
 		}
 		return result;
 	}
 
 	/**
 	 * Find the first selectable TreeItem above {@code item} in the tree. Will consider all available (=expanded) tree items.
 	 * 
 	 * @return a TreeItem, may be null
 	 */
 	private TreeItem findPrevious(final TreeItem item) {
 		final List<TreeItem> siblings = sequentialize(item.getParent().getItems());
 		final int index = siblings.indexOf(item);
 		TreeItem result = index > 0 ? siblings.get(index - 1) : null;
 		if (result != null && !isSelectable(result)) {
 			result = findPrevious(result);
 		}
 		return result;
 	}
 
 	private TreeItem getSelection(final TypedEvent event) {
 		final Tree tree = (Tree) event.widget;
 		TreeItem result = null;
 		if (tree.getSelectionCount() > 0) {
 			result = tree.getSelection()[0];
 		}
 		return result;
 	}
 
 	private boolean isFirst(final TreeItem item) {
 		boolean result = false;
 		if (item != null) {
 			final Tree tree = item.getParent();
 			if (tree.getItemCount() > 0) {
 				result = tree.getItem(0) == item;
 			}
 		}
 		return result;
 	}
 
 	private boolean isLast(final TreeItem item) {
 		if (null == item) {
 			return false;
 		}
 		return item == findLast(item.getParent());
 	}
 
 	private boolean isSelectable(final TreeItem item) {
 		boolean result = true;
 		final INavigationNode<?> node = (INavigationNode<?>) item.getData();
 		if (node instanceof SubModuleNode) {
 			result = isSelectableRoot((SubModuleNode) node);
 
 		}
 		return result;
 	}
 
 	/*
 	 * return true if the node or one of its child-successors is selectable
 	 */
 	private boolean isSelectableRoot(final ISubModuleNode node) {
 		if (node.isSelectable()) {
 			return true;
 		}
 		final List<ISubModuleNode> children = node.getChildren();
 		for (final ISubModuleNode child : children) {
 			if (isSelectableRoot(child)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Do a DFS traversal and return all reachable nodes.
 	 */
 	private List<TreeItem> sequentialize(final TreeItem[] siblings) {
 		final List<TreeItem> stack = new ArrayList<TreeItem>(Arrays.asList(siblings));
 		final List<TreeItem> result = new ArrayList<TreeItem>();
 		while (!stack.isEmpty()) {
 			final TreeItem item = stack.remove(0);
 			result.add(item);
 			if (item.getExpanded()) {
 				stack.addAll(0, Arrays.asList(item.getItems()));
 			}
 		}
 		return result;
 	}
 
 	private void startSwitch(final TreeItem item) {
 		cancelSwitch();
 		if (item != null) {
 			nodeSwitcher = createNodeSwitcher(item);
 			nodeSwitcher.start();
 		}
 	}
 
 	protected NodeSwitcher createNodeSwitcher(final TreeItem item) {
 		return new NodeSwitcher(item);
 	}
 
 	/**
 	 * Activates the given node.
 	 * <p>
 	 * If the activation fails (maybe the node is not selectable), update the selection inside the tree.
 	 * 
 	 * @param node
 	 *            navigation node to activated
 	 * @param tree
 	 *            tree of the module
 	 */
 	private static void activateNode(final INavigationNode<?> node, final Tree tree) {
 		node.setContext("fromUI", true); //$NON-NLS-1$
 		node.activate();
 		if (!node.isActivated()) {
 			final INavigationNode<?> selectedNode = node.getNavigationProcessor().getSelectedNode();
 			if (selectedNode != null) {
 				final TreeItem item = findItem(tree.getItems(), selectedNode);
 				if (item != null) {
 					tree.setSelection(item);
 				}
 			}
 
 		}
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * Activates a navigation node after a timeout. Can be cancelled.
 	 */
 	protected static class NodeSwitcher extends Thread {
 
 		/**
 		 * Wait this long (ms) before activating a node.
 		 */
 		private static final int TIMEOUT_MS = RAPDetector.isRAPavailable() ? 50 : 300;
 
 		protected final Display display;
 		private final Tree tree;
 		private final INavigationNode<?> node;
 
 		private volatile boolean isCancelled;
 
 		protected NodeSwitcher(final TreeItem item) {
 			this.display = item.getDisplay();
 			this.tree = item.getParent();
 			this.node = (INavigationNode<?>) item.getData();
 			if (node == null) {
 				throw new IllegalStateException("This class can't handle a null node. Currently node is null which is probably an error."); //$NON-NLS-1$
 			}
 		}
 
 		/**
 		 * @since 5.0
 		 */
 		protected INavigationNode<?> getNavigationNode() {
 			return node;
 		}
 
 		@Override
 		public void run() {
			if (!node.getParent().isActivated()) {
				return;
			}
 			try {
 				sleep(TIMEOUT_MS);
 			} catch (final InterruptedException iex) {
 				Nop.reason("ignore"); //$NON-NLS-1$
 			}
 			if (!isCancelled) {
 				// node activation must be triggered from the UI thread:
 				display.syncExec(new Runnable() {
 					public void run() {
 						activateNode(node, tree);
 					}
 				});
 			}
 		}
 
 		/**
 		 * Cancels activation of the node, if called before the timeout.
 		 */
 		public void cancel() {
 			isCancelled = true;
 		}
 	}
 }
