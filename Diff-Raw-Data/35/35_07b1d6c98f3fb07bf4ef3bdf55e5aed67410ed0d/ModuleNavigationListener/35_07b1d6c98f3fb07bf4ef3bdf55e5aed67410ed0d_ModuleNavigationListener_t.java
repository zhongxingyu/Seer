 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
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
 import org.eclipse.riena.internal.navigation.ui.swt.handlers.SwitchModule;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.model.NavigationProcessor;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 
 /**
  * Navigation for the 'submodule' tree used in {@link ModuleView}. This includes
  * both mouse navigation (selection) and keyboard navigation (key down / up).
  * <p>
  * This class takes care of the following cases:
  * <ul>
  * <li>node clicked / selection change: activate the node</li>
  * <li>arrow up / down: activate the node</li>
  * <li>arrow up on the first node: activate the previous module, wrapping around
  * to the last, if there is no previous one</li>
  * <li>arrow down on the last node: activate the next module, wrapping around to
  * the 1st, if there is no next one</li>
  * </ul>
  * <p>
  * Some submodule nodes can be flagged as non-selecteble. If such a node is
  * activated, then the first selectable child will be selected. This is done by
  * {@link NavigationProcessor}. Moving over such a node is not problem anymore,
  * since the activation is only triggered after a delay - not instantly.
  */
class ModuleNavigationListener extends SelectionAdapter implements KeyListener, FocusListener {
 
 	/** Keycode for 'arrow down' (16777218) */
 	private static final int KC_ARROW_DOWN = 16777218;
 	/** Keycode for 'arrow up' (16777217) */
 	private static final int KC_ARROW_UP = 16777217;
 
 	private volatile boolean keyPressed;
 	private int keyCode;
 
 	private NodeSwitcher nodeSwitcher;
 	private boolean isFirst;
 	private boolean isLast;
 
 	ModuleNavigationListener(Tree moduleTree) {
 		moduleTree.addKeyListener(this);
 		moduleTree.addSelectionListener(this);
		moduleTree.addFocusListener(this);
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent event) {
 		// System.out.println("widgetSelected() " + getSelection(event));
 		cancelSwitch();
 		if (!keyPressed) {
 			// selected by mouse click
 			startSwitch(getSelection(event));
 		} else {
 			// key is down and we must check if the tree node is selectable
 			Tree tree = (Tree) event.widget;
 			TreeItem[] sel = tree.getSelection();
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
 
 	public void keyPressed(KeyEvent event) {
 		cancelSwitch();
 		keyPressed = true;
 		keyCode = event.keyCode;
 		TreeItem from = getSelection(event);
 		isFirst = isFirst(from);
 		isLast = isLast(from);
 	}
 
 	public void keyReleased(KeyEvent event) {
 		// System.out.println("keyReleased() " + getSelection(event));
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
 
	public void focusGained(FocusEvent e) {
		// unused
	}

	public void focusLost(FocusEvent e) {
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
 		SwitchModule result = new SwitchModule();
 		result.setActivateSubmodule(true);
 		return result;
 	}
 
 	private TreeItem findLast(Tree tree) {
 		if (tree.isDisposed()) {
 			return null;
 		}
 		TreeItem[] items = tree.getItems();
 		return items.length > 0 ? findLast(items[items.length - 1]) : null;
 	}
 
 	private TreeItem findLast(TreeItem item) {
 		TreeItem result = item;
 		if (item.getExpanded() && item.getItemCount() > 0) {
 			TreeItem last = item.getItem(item.getItemCount() - 1);
 			result = findLast(last);
 		}
 		return result;
 	}
 
 	/**
 	 * Find the first selectable TreeItem below {@code item} in the tree. Will
 	 * consider all available (=expanded) tree items.
 	 * 
 	 * @return a TreeItem, may be null
 	 */
 	private TreeItem findNext(TreeItem item) {
 		List<TreeItem> siblings = sequentialize(item.getParent().getItems());
 		int index = siblings.indexOf(item);
 		TreeItem result = index != -1 && index < siblings.size() - 1 ? siblings.get(index + 1) : null;
 		if (result != null && !isSelectable(result)) {
 			result = findNext(result);
 		}
 		return result;
 	}
 
 	/**
 	 * Find the first selectable TreeItem above {@code item} in the tree. Will
 	 * consider all available (=expanded) tree items.
 	 * 
 	 * @return a TreeItem, may be null
 	 */
 	private TreeItem findPrevious(TreeItem item) {
 		List<TreeItem> siblings = sequentialize(item.getParent().getItems());
 		int index = siblings.indexOf(item);
 		TreeItem result = index > 0 ? siblings.get(index - 1) : null;
 		if (result != null && !isSelectable(result)) {
 			result = findPrevious(result);
 		}
 		return result;
 	}
 
 	private TreeItem getSelection(TypedEvent event) {
 		Tree tree = (Tree) event.widget;
 		TreeItem result = null;
 		if (tree.getSelectionCount() > 0) {
 			result = tree.getSelection()[0];
 		}
 		return result;
 	}
 
 	private boolean isFirst(TreeItem item) {
 		boolean result = false;
 		if (item != null) {
 			Tree tree = item.getParent();
 			if (tree.getItemCount() > 0) {
 				result = tree.getItem(0) == item;
 			}
 		}
 		return result;
 	}
 
 	private boolean isLast(TreeItem item) {
 		if (null == item) {
 			return false;
 		}
 		return item == findLast(item.getParent());
 	}
 
 	private boolean isSelectable(TreeItem item) {
 		boolean result = true;
 		INavigationNode<?> node = (INavigationNode<?>) item.getData();
 		if (node instanceof SubModuleNode) {
 			result = ((SubModuleNode) node).isSelectable();
 		}
 		return result;
 	}
 
 	/**
 	 * Do a DFS traversal and return all reachable nodes.
 	 */
 	private List<TreeItem> sequentialize(TreeItem[] siblings) {
 		List<TreeItem> stack = new ArrayList<TreeItem>(Arrays.asList(siblings));
 		List<TreeItem> result = new ArrayList<TreeItem>();
 		while (!stack.isEmpty()) {
 			TreeItem item = stack.remove(0);
 			result.add(item);
 			if (item.getExpanded()) {
 				stack.addAll(0, Arrays.asList(item.getItems()));
 			}
 		}
 		return result;
 	}
 
 	private void startSwitch(TreeItem item) {
 		cancelSwitch();
 		if (item != null) {
 			nodeSwitcher = new NodeSwitcher(item.getDisplay(), (INavigationNode<?>) item.getData());
 			nodeSwitcher.start();
 		}
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * Activates a navigation node after a timeout. Can be cancelled.
 	 */
 	private final static class NodeSwitcher extends Thread {
 
 		/**
 		 * Wait this long (ms) before activating a node.
 		 */
 		private static final int TIMEOUT_MS = 300;
 
 		private final INavigationNode<?> node;
 		private final Display display;
 
 		private volatile boolean isCancelled;
 
 		NodeSwitcher(Display display, INavigationNode<?> node) {
 			this.display = display;
 			this.node = node;
 		}
 
 		@Override
 		public void run() {
 			try {
 				sleep(TIMEOUT_MS);
 			} catch (InterruptedException iex) {
 				Nop.reason("ignore"); //$NON-NLS-1$
 			}
 			if (!isCancelled) {
 				// node activation must be triggered from the UI thread:
 				display.syncExec(new Runnable() {
 					public void run() {
 						node.activate();
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
