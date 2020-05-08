 /*{{{ header
  * BufferList.java
  * Copyright (c) 2000-2002 Dirk Moebius
  * Copyright (c) 2004 Karsten Pilz
  *
  * :tabSize=4:indentSize=4:noTabs=false:maxLineLen=0:folding=explicit:collapseFolds=1:
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  *}}}
  */
 package bufferlist;
 
 // {{{ imports
 import java.awt.BorderLayout;
 import java.awt.Rectangle;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.ToolTipManager;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 import org.gjt.sp.jedit.Buffer;
 import org.gjt.sp.jedit.EBComponent;
 import org.gjt.sp.jedit.EBMessage;
 import org.gjt.sp.jedit.EditBus;
 import org.gjt.sp.jedit.GUIUtilities;
 import org.gjt.sp.jedit.View;
 import org.gjt.sp.jedit.jEdit;
 import org.gjt.sp.jedit.gui.DockableWindowManager;
 import org.gjt.sp.jedit.io.VFS;
 import org.gjt.sp.jedit.io.VFSManager;
 import org.gjt.sp.jedit.msg.BufferUpdate;
 import org.gjt.sp.jedit.msg.EditPaneUpdate;
 import org.gjt.sp.jedit.msg.PropertiesChanged;
 import org.gjt.sp.util.StandardUtilities;
 
 // }}}
 /**
  * A dockable panel that contains a list of open files.
  * 
  * @author Dirk Moebius
  */
 public class BufferList extends JPanel implements EBComponent
 {
 	static final String ROOT = "ROOT";
 
 	private static final long serialVersionUID = 1L;
 
 	// {{{ display mode constants
 	public static final int DISPLAY_MODE_FLAT_TREE = 1;
 
 	public static final int DISPLAY_MODE_HIERARCHICAL = 2;// }}}
 
 	// {{{ instance variables
 	private final View view;
 
 	// private final String position;
 
 	private final JTree tree;
 
 	private final JScrollPane scrTree;
 
 	private DefaultTreeModel model;
 
 	private final BufferListTreeNode rootNode;
 
 	private boolean sortIgnoreCase;
 	
 	private boolean ignoreSelectionChange;
 
 	private int displayMode;
 
 	private HashMap<String, BufferListTreeNode> distinctDirs;
 
 	private final JLabel bufferCountsLabel = new JLabel();// }}}
 
 	// {{{ +BufferList(View, String) : <init>
 	public BufferList(final View view, final String position)
 	{
 		super(new BorderLayout());
 		// <reusage of BufferListTreeNode>
 		final Object root = new Object()
 		{
 			@Override
 			public String toString()
 			{
 				return ROOT;
 			}
 		};
 		rootNode = new BufferListTreeNode(root);
 		distinctDirs = new HashMap<String, BufferListTreeNode>();
 		distinctDirs.put(ROOT, rootNode);
 		// </reusage of BufferListTreeNode>
 		this.view = view;
 		// this.position = position;
 		sortIgnoreCase = jEdit.getBooleanProperty("vfs.browser.sortIgnoreCase");
 		// tree:
 		ignoreSelectionChange = false;
 		tree = new JTree()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition,
 				boolean pressed)
 			{
 				ignoreSelectionChange = true;
 				boolean res = super.processKeyBinding(ks, e, condition, pressed);
 				ignoreSelectionChange = false;
 				return res;
 			}
 		};
 		tree.setRootVisible(false);
 		tree.setShowsRootHandles(true);
 		tree.addMouseListener(new MouseHandler());
 		tree.addKeyListener(new KeyHandler());
 		tree.addTreeSelectionListener(new TreeSelectionListener()
 		{
 			public void valueChanged(TreeSelectionEvent e)
 			{
 				if (ignoreSelectionChange || e.getNewLeadSelectionPath() == null)
 				{
 					return;
 				}
 				BufferListTreeNode node = (BufferListTreeNode) e.getNewLeadSelectionPath()
 					.getLastPathComponent();
 				if (node.isBuffer())
 				{
 					view.goToBuffer(node.getBuffer());
 					return;
 				}
 			}
 		});
 		
 		ToolTipManager.sharedInstance().registerComponent(tree);
 		// scrollpane for tree:
 		scrTree = new JScrollPane(tree);
 		// overall layout:
 		updateBufferCounts();
 		add(BorderLayout.NORTH, bufferCountsLabel);
 		add(BorderLayout.CENTER, scrTree);
 		displayMode = jEdit.getIntegerProperty("bufferlist.displayMode", DISPLAY_MODE_FLAT_TREE);
 		sortIgnoreCase = jEdit.getBooleanProperty("vfs.browser.sortIgnoreCase");
 		createModel();
 		handlePropertiesChanged();
 		if (position.equals(DockableWindowManager.FLOATING))
 		{
 			requestTreeFocus();
 		}
 		expandCurrentPath();
 		if (jEdit.getBooleanProperty("bufferlist.startExpanded"))
 		{
 			TreeTools.expandAll(tree);
 		}
 	} // }}}
 
 	// {{{ +getInstanceForView(View) : BufferList
 	/**
 	 * Helper used by various actions in "actions.xml";
 	 * 
 	 * @since BufferList 1.0.2
 	 * @see actions.xml
 	 */
 	public static BufferList getInstanceForView(View view)
 	{
 		DockableWindowManager mgr = view.getDockableWindowManager();
 		BufferList bufferlist = (BufferList) mgr.getDockable("bufferlist");
 		if (bufferlist == null)
 		{
 			mgr.addDockableWindow("bufferlist");
 			bufferlist = (BufferList) mgr.getDockable("bufferlist");
 		}
 		return bufferlist;
 	} // }}}
 
 	// {{{ +requestTreeFocus() : void
 	/**
 	 * Set the focus on the tree.
 	 * 
 	 * @since BufferList 1.2
 	 */
 	public void requestTreeFocus()
 	{
 		tree.requestFocus();
 	} // }}}
 	
 	/**
 	 * Expand path to the current buffer and make sure it is visible.
 	 */
 	public void expandCurrentPath()
 	{
 		BufferListTreeNode node = getNode(view.getBuffer());
 		if (node == null)
 		{
 			return;
 		}
 		TreePath path = new TreePath(node.getPath());
 		tree.expandPath(path.getParentPath());
 		//We don't need two emphasizers - selection and bold font.
 		tree.clearSelection();
 		// Make sure path is visible.
 		// Note: can't use tree.scrollPathToVisible(path) here, because it moves
 		// the enclosing JScrollPane horizontally; but we want vertical movement
 		// only.
 		// tree.makeVisible(path);
 		Rectangle bounds = tree.getPathBounds(path);
 		if (bounds != null)
 		{
 			bounds.width = 0;
 			bounds.x = 0;
 			tree.scrollRectToVisible(bounds);
 		}
 	}
 
 	// {{{ +nextBuffer() : void
 	/**
 	 * Go to next buffer in open files list.
 	 * 
 	 * @since BufferList 0.5
 	 */
 	public void nextBuffer()
 	{
 		Buffer buffer = view.getBuffer();
 		Enumeration<BufferListTreeNode> e = rootNode.depthFirstEnumeration();
 		BufferListTreeNode first = null, next = null;
 		while (e.hasMoreElements())
 		{
 			BufferListTreeNode node = e.nextElement();
 			if (first == null && node.isBuffer())
 			{
 				first = node;
 			}
			if (node.isBuffer() && node.getBuffer() == buffer)
 			{
 				break;
 			}
 		}
 		while (e.hasMoreElements())
 		{
 			BufferListTreeNode node = e.nextElement();
 			if (node.isBuffer())
 			{
 				next = node;
 				break;
 			}
 		}
 		if (next == null)
 		{
 			next = first;
 		}
 		if (next != null)
 		{
 			view.goToBuffer(next.getBuffer());
 		}
 	} // }}}
 
 	// {{{ +previousBuffer() : void
 	/**
 	 * Go to previous buffer in open files list.
 	 * 
 	 * @since BufferList 0.5
 	 */
 	public void previousBuffer()
 	{
 		Buffer buffer = view.getBuffer();
 		Enumeration<BufferListTreeNode> e = rootNode.depthFirstEnumeration();
 		BufferListTreeNode prev = null, node = null;
 		while (e.hasMoreElements())
 		{
 			node = e.nextElement();
			if (node.isBuffer() && node.getUserObject() == buffer)
 			{
 				break;
 			}
 			if (node.isBuffer())
 			{
 				prev = node;
 			}
 		}
 		if (prev == null)
 		{
 			while (e.hasMoreElements())
 			{
 				node = e.nextElement();
 				if (node.isBuffer())
 				{
 					prev = node;
 				}
 			}
 		}
 		if (prev != null)
 		{
 			view.goToBuffer(prev.getBuffer());
 		}
 	} // }}}
 
 	// {{{ +setDisplayMode(int) : void
 	/**
 	 * Helper function; may be called from "actions.xml"; set the display mode
 	 * for this instance.
 	 * 
 	 * @since BufferList 1.0.2
 	 */
 	public void setDisplayMode(int pDisplayMode)
 	{
 		displayMode = pDisplayMode;
 		if (displayMode == DISPLAY_MODE_FLAT_TREE)
 		{
 			tree.putClientProperty("JTree.lineStyle", "Horizontal");
 		}
 		else
 		{
 			tree.putClientProperty("JTree.lineStyle", "Angled");
 		}
 		updateModel();
 	} // }}}
 
 	// {{{ +toggleDisplayMode() : void
 	/**
 	 * Invoked by action "bufferlist-toggle-display-mode" only; toggles between
 	 * DISPLAY_MODE_FLAT_TREE/DISPLAY_MODE_HIERARCHICAL.
 	 * 
 	 * @since BufferList 1.0.2
 	 * @see actions.xml
 	 */
 	public void toggleDisplayMode()
 	{
 		if (displayMode == DISPLAY_MODE_FLAT_TREE)
 		{
 			setDisplayMode(DISPLAY_MODE_HIERARCHICAL);
 		}
 		else
 		{
 			setDisplayMode(DISPLAY_MODE_FLAT_TREE);
 		}
 	} // }}}
 
 	// {{{ +getDisplayMode(View) : int
 	/**
 	 * Used by "bufferlist-toggle-display-mode:IS_SELECTED"; returns the display
 	 * mode for the view's bufferlist or the current default display mode.
 	 * 
 	 * @since BufferList 1.0.2
 	 * @see actions.xml
 	 */
 	public static int getDisplayMode(View view)
 	{
 		DockableWindowManager mgr = view.getDockableWindowManager();
 		BufferList bufferlist = (BufferList) mgr.getDockable("bufferlist");
 		if (bufferlist == null)
 		{
 			return jEdit.getIntegerProperty("bufferlist.displayMode", DISPLAY_MODE_FLAT_TREE);
 		}
 		else
 		{
 			return bufferlist.displayMode;
 		}
 	} // }}}
 
 	// {{{ +addNotify() : void
 	/**
 	 * Invoked when the component is created; adds focus event handlers to all
 	 * EditPanes of the View associated with this BufferList.
 	 */
 	@Override
 	public void addNotify()
 	{
 		super.addNotify();
 		EditBus.addToBus(this);
 	} // }}}
 
 	// {{{ +removeNotify() : void
 	/**
 	 * Invoked when the component is removed; removes the focus event handlers
 	 * from all EditPanes.
 	 */
 	@Override
 	public void removeNotify()
 	{
 		super.removeNotify();
 		EditBus.removeFromBus(this);
 	} // }}}
 
 	// {{{ +handleMessage(EBMessage) : void
 	/** Handle jEdit EditBus messages */
 	public void handleMessage(EBMessage message)
 	{
 		if (message instanceof BufferUpdate)
 		{
 			handleBufferUpdate((BufferUpdate) message);
 		}
 		else if (message instanceof EditPaneUpdate)
 		{
 			handleEditPaneUpdate((EditPaneUpdate) message);
 		}
 		else if (message instanceof PropertiesChanged)
 		{
 			handlePropertiesChanged();
 		}
 	} // }}}
 
 	// {{{ -handleBufferUpdate(BufferUpdate) : void
 	private void handleBufferUpdate(BufferUpdate bu)
 	{
 		if (bu.getWhat() == BufferUpdate.DIRTY_CHANGED)
 		{
 			updateNode(bu.getBuffer());
 		}
 		else if (bu.getWhat() == BufferUpdate.CREATED || bu.getWhat() == BufferUpdate.CLOSED
 			|| bu.getWhat() == BufferUpdate.SAVED)
 		{
 			updateModel();
 		}
 		updateBufferCounts();
 	} // }}}
 
 	// {{{ -handleEditPaneUpdate(EditPaneUpdate) : void
 	private void handleEditPaneUpdate(EditPaneUpdate epu)
 	{
 		// View v = ((EditPane) epu.getSource()).getView();
 		View v = epu.getEditPane().getView();
 		if (v != view)
 		{
 			return; // not for this BufferList instance
 		}
 		if (epu.getWhat() == EditPaneUpdate.BUFFER_CHANGED)
 		{
 			currentBufferChanged();
 		}
 	} // }}}
 
 	// {{{ -handlePropertiesChanged() : void
 	private void handlePropertiesChanged()
 	{
 		boolean modelChanged = false;
 		if (jEdit.getIntegerProperty("bufferlist.textClipping", 1) == 0)
 		{
 			scrTree
 				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		}
 		else
 		{
 			scrTree.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		}
 		boolean newSortIgnoreCase = jEdit.getBooleanProperty("vfs.browser.sortIgnoreCase");
 		if (sortIgnoreCase != newSortIgnoreCase)
 		{
 			modelChanged = true;
 			sortIgnoreCase = newSortIgnoreCase;
 		}
 		if (modelChanged)
 		{
 			updateModel();
 		}
 		// set new cell renderer to change fonts:
 		tree.setCellRenderer(new BufferListRenderer(view));
 	} // }}}
 
 	// {{{ -updateBufferCounts() : void
 	private void updateBufferCounts()
 	{
 		int dirtyBuffers = 0;
 		Buffer buffers[] = jEdit.getBuffers();
 		for (Buffer buffer : buffers)
 		{
 			if (buffer.isDirty())
 			{
 				dirtyBuffers++;
 			}
 		}
 		bufferCountsLabel.setText(jEdit.getProperty("bufferlist.openfiles.label")
 			+ jEdit.getBufferCount() + " " + jEdit.getProperty("bufferlist.dirtyfiles.label")
 			+ dirtyBuffers);
 	} // }}}
 
 	// {{{ -getDir(Buffer) : String
 	private static String getDir(Buffer buffer)
 	{
 		return buffer.getVFS().getParentOfPath(buffer.getPath());
 	} // }}}
 
 	// {{{ -createDirectoryNodes(String) : BufferListTreeNode
 	private BufferListTreeNode createDirectoryNodes(String path)
 	{
 		VFS vfs = VFSManager.getVFSForPath(path);
 		String parent = vfs.getParentOfPath(path);
 		if (path.equals(parent))
 		{
 			return rootNode;
 		}
 		BufferListTreeNode node = distinctDirs.get(path);
 		if (node == null)
 		{
 			node = new BufferListTreeNode(path, true);
 			distinctDirs.put(path, node);
 		}
 		if (!node.isConnected())
 		{
 			BufferListTreeNode parentNode;
 			if (displayMode == DISPLAY_MODE_FLAT_TREE)
 			{
 				parentNode = rootNode;
 			}
 			else
 			{
 				parentNode = createDirectoryNodes(parent);
 			}
 			parentNode.add(node);
 			node.setConnected();
 		}
 		return node;
 	} // }}}
 
 	// {{{ -removeObsoleteDirNodes(BufferListTreeNode) : void
 	/**
 	 * Removes all intermediate directory nodes that only have one directory
 	 * node and no buffer nodes as children, i.e. removes unnecessary levels
 	 * from the tree.
 	 */
 	private void removeObsoleteDirNodes(BufferListTreeNode node)
 	{
 		Vector<BufferListTreeNode> vec = new Vector<BufferListTreeNode>(node.getChildCount());
 		Enumeration<BufferListTreeNode> children = node.children();
 		while (children.hasMoreElements())
 		{
 			vec.add(children.nextElement());
 		}
 		node.removeAllChildren();
 		children = vec.elements();
 		while (children.hasMoreElements())
 		{
 			BufferListTreeNode child = children.nextElement();
 			removeObsoleteDirNodes(child);
 			boolean keep = child.getChildCount() > 1 || child.getUserObject() instanceof Buffer;
 			if (!keep && child.getChildCount() == 1)
 			{
 				if (((BufferListTreeNode) child.getFirstChild()).getUserObject() instanceof Buffer)
 				{
 					keep = true;
 				}
 			}
 			if (keep)
 			{
 				node.add(child);
 			}
 			else
 			{
 				Enumeration<BufferListTreeNode> childChildren = child.children();
 				while (childChildren.hasMoreElements())
 				{
 					node.add(childChildren.nextElement());
 				}
 			}
 		}
 	} // }}}
 
 	// {{{ -removeDirNodesCommonPrefixes(BufferListTreeNode, String) : void
 	/**
 	 * Removes the path prefix that is present in the parent node (for each
 	 * directory node)
 	 */
 	private void removeDirNodesCommonPrefixes(BufferListTreeNode node, String prefix)
 	{
 		Enumeration<BufferListTreeNode> children = node.children();
 		while (children.hasMoreElements())
 		{
 			BufferListTreeNode child = children.nextElement();
 			if (child.getUserObject() instanceof String)
 			{
 				String child_prefix = (String) child.getUserObject();
 				if (child_prefix.startsWith(prefix))
 				{
 					child.setUserObject(child_prefix.substring(prefix.length()));
 				}
 				removeDirNodesCommonPrefixes(child, child_prefix);
 			}
 			else
 			{
 				removeDirNodesCommonPrefixes(child, prefix);
 			}
 		}
 	} // }}}
 
 	// {{{ -saveExpansionState() : void
 	/**
 	 * Saves the expansion state of all directory nodes (within each
 	 * BufferListTreeNode).
 	 */
 	private void saveExpansionState()
 	{
 		for (BufferListTreeNode node : distinctDirs.values())
 		{
 			node.reset();
 		}
 		distinctDirs.clear();
 		distinctDirs.put(ROOT, rootNode);
 		Enumeration<TreePath> e = tree.getExpandedDescendants(new TreePath(tree.getModel()
 			.getRoot()));
 		if (e != null)
 		{
 			while (e.hasMoreElements())
 			{
 				TreePath expPath = e.nextElement();
 				if (expPath.getLastPathComponent() instanceof BufferListTreeNode)
 				{
 					BufferListTreeNode node = (BufferListTreeNode) expPath.getLastPathComponent();
 					node.setExpanded(true);
 					if (node != rootNode)
 					{
 						distinctDirs.put(node.getUserPath(), node);
 					}
 				}
 				else
 				{
 					System.err.println("test");
 				}
 			}
 		}
 	} // }}}
 
 	// {{{ -restoreExpansionState() : void
 	/**
 	 * Restores the expansion state of all directory nodes.
 	 */
 	private void restoreExpansionState()
 	{
 		for (BufferListTreeNode node : distinctDirs.values())
 		{
 			if (node.isExpanded())
 			{
 				tree.expandPath(new TreePath(node.getPath()));
 			}
 		}
 	} // }}}
 
 	// {{{ -recreateModel() : void
 	/**
 	 * Updates the tree model (preserving the current expansion state).
 	 */
 	private void updateModel()
 	{
 		saveExpansionState();
 		createModel();
 		restoreExpansionState();
 	} // }}}
 
 	// {{{ -createModel() : void
 	/**
 	 * Sets a new tree model.
 	 */
 	private void createModel()
 	{
 		Buffer[] buffers = jEdit.getBuffers();
 		Arrays.sort(buffers, new Comparator<Buffer>()
 		{
 			public int compare(Buffer buf1, Buffer buf2)
 			{
 				if (buf1 == buf2)
 				{
 					return 0;
 				}
 				else
 				{
 					String dir1 = getDir(buf1);
 					String dir2 = getDir(buf2);
 					int cmpDir = StandardUtilities.compareStrings(dir1, dir2, sortIgnoreCase);
 					if (cmpDir == 0)
 					{
 						return StandardUtilities.compareStrings(buf1.getName(), buf2.getName(),
 							sortIgnoreCase);
 					}
 					else
 					{
 						return cmpDir;
 					}
 				}
 			}
 		});
 		for (BufferListTreeNode node : distinctDirs.values())
 		{
 			node.removeAllChildren();
 		}
 		for (int i = 0; i < buffers.length; ++i)
 		{
 			Buffer buffer = buffers[i];
 			BufferListTreeNode dirNode = createDirectoryNodes(buffer.getVFS().getParentOfPath(
 				buffer.getPath()));
 			dirNode.add(new BufferListTreeNode(buffer, false));
 		}
 		removeObsoleteDirNodes(rootNode); // NOTE: when ommited, the tree
 		// contains all intermediate levels
 		// i.e. gets its "full depth"
 		removeDirNodesCommonPrefixes(rootNode, "");
 		model = new DefaultTreeModel(rootNode);
 		tree.setModel(model);
 	} // }}}
 
 	// {{{ -getNode(Buffer) : BufferListTreeNode
 	/**
 	 * @return the tree node for the jEdit buffer, or null if the buffer cannot
 	 *         be found in the current tree model.
 	 */
 	private BufferListTreeNode getNode(Buffer buffer)
 	{
 		Enumeration<BufferListTreeNode> e = rootNode.depthFirstEnumeration();
 		while (e.hasMoreElements())
 		{
 			BufferListTreeNode node = e.nextElement();
 			if (node.getUserObject() == buffer)
 			{
 				return node;
 			}
 		}
 		return null;
 	} // }}}
 
 	// {{{ -updateNode(Buffer) : void
 	private void updateNode(Buffer buffer)
 	{
 		BufferListTreeNode node = getNode(buffer);
 		if (node == null)
 		{
 			return;
 		}
 		model.nodeChanged(node);
 	} // }}}
 
 	// {{{ -currentBufferChanged() : void
 	/**
 	 * Called after the current buffer has changed; notifies the cell renderer
 	 * and makes sure the current buffer is visible.
 	 */
 	private void currentBufferChanged()
 	{
 		expandCurrentPath();
 		// Set new cell renderer to draw to current buffer bold
 		tree.setCellRenderer(new BufferListRenderer(view));
 	} // }}}
 
 	// {{{ -focusEditPane() : void
 	private void focusEditPane()
 	{
 		view.getTextArea().requestFocus();
 	} // }}}
 
 	// {{{ -class MouseHandler
 	/**
 	 * A mouse listener for the buffer list.
 	 */
 	private class MouseHandler extends MouseAdapter
 	{
 		// {{{ +mouseClicked(MouseEvent) : void
 		/**
 		 * invoked when the mouse button has been clicked (pressed and released)
 		 * on the buffer list.
 		 */
 		@Override
 		public void mouseClicked(MouseEvent e)
 		{
 			// first exclude what we don't handle
 			if ((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0)
 			{
 				return;
 			}
 			if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
 			{
 				return;
 			}
 			if (e.isAltDown() || e.isAltGraphDown() || e.isMetaDown() || e.isShiftDown()
 				|| e.isControlDown())
 			{
 				return;
 			}
 			if (e.getClickCount() > 2 && e.getClickCount() % 2 != 0)
 			{
 				return;
 			}
 			e.consume();
 			TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
 			if (path == null)
 			{
 				return;
 			}
 			BufferListTreeNode node = (BufferListTreeNode) path.getLastPathComponent();
 			Object obj = node.getUserObject();
 			if (obj instanceof String)
 			{
 				return;
 			}
 			Buffer buffer = (Buffer) obj;
 			if (e.getClickCount() >= 2
 				&& jEdit.getBooleanProperty("bufferlist.closeFilesOnDoubleClick", true))
 			{
 				// left mouse double press: close buffer
 				jEdit.closeBuffer(view, buffer);
 			}
 			else
 			{
 				// left mouse single press: open buffer
 				// view.goToBuffer(buffer);
 			}
 		} // }}}
 
 		// {{{ +mousePressed(MouseEvent) : void
 		@Override
 		public void mousePressed(MouseEvent e)
 		{
 			if (e.isPopupTrigger())
 			{
 				showPopup(e);
 			}
 		} // }}}
 
 		// {{{ +mouseReleased(MouseEvent) : void
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			if (e.isPopupTrigger())
 			{
 				showPopup(e);
 			}
 		} // }}}
 
 		// {{{ -showPopup(MouseEvent) : void
 		private void showPopup(MouseEvent e)
 		{
 			e.consume();
 			// if user didn't select any buffer, or selected only one buffer,
 			// then select entry at mouse position:
 			TreePath[] paths = tree.getSelectionPaths();
 			if (paths == null || paths.length == 1)
 			{
 				TreePath locPath = tree.getClosestPathForLocation(e.getX(), e.getY());
 				if (locPath != null)
 				{
 					Rectangle nodeRect = tree.getPathBounds(locPath);
 					if (nodeRect != null && nodeRect.contains(e.getX(), e.getY()))
 					{
 						paths = new TreePath[] { locPath };
 						tree.setSelectionPath(locPath);
 					}
 				}
 			}
 			// check whether user selected a directory node:
 			if (paths != null)
 			{
 				for (int i = 0; i < paths.length; ++i)
 				{
 					BufferListTreeNode node = (BufferListTreeNode) paths[i].getLastPathComponent();
 					Object obj = node.getUserObject();
 					if (obj != null && obj instanceof String)
 					{
 						// user selected directory node; select all entries
 						// below it:
 						Enumeration<BufferListTreeNode> children = node.depthFirstEnumeration();
 						while (children.hasMoreElements())
 						{
 							BufferListTreeNode childNode = children.nextElement();
 							tree.addSelectionPath(new TreePath(childNode.getPath()));
 						}
 					}
 				}
 			}
 			// create & show popup
 			paths = tree.getSelectionPaths();
 			BufferListPopup popup = new BufferListPopup(view, tree, paths, BufferListPlugin
 				.getMenuExtensions());
 			popup.show(tree, e.getX() + 1, e.getY() + 1);
 		} // }}}
 	} // }}}
 
 	// {{{ -class KeyHandler
 	/**
 	 * A key handler for the buffer list.
 	 */
 	private class KeyHandler extends KeyAdapter
 	{
 		// {{{ +keyPressed(KeyEvent) : void
 		@Override
 		public void keyPressed(KeyEvent evt)
 		{
 			if (evt.isConsumed())
 			{
 				return;
 			}
 			int kc = evt.getKeyCode();
 			if (kc == KeyEvent.VK_ESCAPE || kc == KeyEvent.VK_CANCEL)
 			{
 				evt.consume();
 				tree.clearSelection();
 				focusEditPane();
 			}
 			else if (kc == KeyEvent.VK_ENTER || kc == KeyEvent.VK_ACCEPT)
 			{
 				evt.consume();
 				TreePath[] sel = tree.getSelectionPaths();
 				if (sel != null && sel.length > 0)
 				{
 					if (sel.length > 1)
 					{
 						GUIUtilities.error(BufferList.this, "bufferlist.error.tooMuchSelection",
 							null);
 						return;
 					}
 					else
 					{
 						BufferListTreeNode node = (BufferListTreeNode) sel[0]
 							.getLastPathComponent();
 						if (node.isBuffer())
 						{
 							view.setBuffer(node.getBuffer());
 						}
 					}
 				}
 				focusEditPane();
 			}
 		} // }}}
 	} // }}}
 }
