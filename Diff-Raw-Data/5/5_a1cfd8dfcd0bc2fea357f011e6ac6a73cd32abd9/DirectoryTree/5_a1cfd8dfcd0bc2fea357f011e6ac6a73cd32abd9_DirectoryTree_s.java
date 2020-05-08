 package org.ksa14.webhard.ui;
 
 import java.util.*;
 import java.awt.*;
 import javax.swing.*;
 import javax.swing.tree.*;
 import javax.swing.event.*;
 
 import org.ksa14.webhard.sftp.*;
 
 /**
  * DirectoryTree represents the directory tree component that goes left of the webahrd window. 
  * It shows the hierarchy of directories in the remote webhard server, 
  * and should initiate appropriate sftp requests as it gets user inputs.
  * 
  * @author Jongwook
  */
 public class DirectoryTree extends JTree implements TreeSelectionListener, TreeWillExpandListener, SftpListener {
 	public static final long serialVersionUID = 0L;
 
 	private static DirectoryTree theInstance;
 	DefaultMutableTreeNode top, lastNode;
 	TreePath lastPath;
 
 	private class MyTreeCellRenderer extends DefaultTreeCellRenderer {
 		public static final long serialVersionUID = 0L;
 
 		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
 			if (leaf && tree.getPathForRow(row).getLastPathComponent() instanceof DefaultMutableTreeNode) { 
 				setLeafIcon(getClosedIcon());
 			}    
 			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,	row, hasFocus);
 		}
 	}
 
 	/**
 	 * Initializes the directory tree view 
 	 */
 	private DirectoryTree(DefaultMutableTreeNode tnode) {
 		super (tnode);
 		top = tnode;
 
 		Object paths[] = {"/", ""};
 		top.add(new DefaultMutableTreeNode("..."));
 		lastNode = top;
 		lastPath = new TreePath(top);
 		UpdateNode(paths, top);
 
 		this.setScrollsOnExpand(true);
 		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		this.addTreeSelectionListener(this);
 		this.addTreeWillExpandListener(this);
 		this.setRowHeight(20);
 		this.setCellRenderer(new MyTreeCellRenderer());
 		
 		SftpAdapter.AddListener(this);
 	}
 
 	public static DirectoryTree GetInstance() {
 		if(theInstance == null) 
 			theInstance = new DirectoryTree(new DefaultMutableTreeNode("KSA14 Webhard"));
 
 		return theInstance;	
 	}
 
	public void UpdateTreeDone(Vector<?> dirlist) {	
 		for (int i=0; i<dirlist.size(); i++) {
 			String dir = (String)dirlist.elementAt(i);
 
 			// skip hidden files
 			if(dir.charAt(0) == '.') continue;
 
 			DefaultMutableTreeNode child = new DefaultMutableTreeNode(dirlist.elementAt(i));
 
 			// create dummy child
 			child.add(new DefaultMutableTreeNode("..."));
 
 			lastNode.add(child);
 		}
 		collapsePath(lastPath);
 		expandPath(lastPath);
 		
 		this.setEnabled(true);
 	}
 
 	public void valueChanged(TreeSelectionEvent e) {
 		DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
 		if(node == top || node == lastNode) return;
 		lastPath = e.getPath();
 		lastNode = node;
 		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 		UpdateNode(e.getPath().getPath(), node);
 	}
 
 	public void treeWillExpand(TreeExpansionEvent e) {
 		DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
 		if(node == top || node == lastNode) return;
 		lastPath = e.getPath();
 		lastNode = node;
 		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 		UpdateNode(e.getPath().getPath(), node);
 		this.setSelectionPath(e.getPath());
 	}
 
 	public void treeWillCollapse(TreeExpansionEvent e) {}
 
 	public void UpdateNode(final Object paths[], final DefaultMutableTreeNode node) {
 		if(node == null) return;
 		
 		this.setEnabled(false);
 		FileList.GetInstance().setEnabled(false);
 		
 		new Thread() { 
 			public void run() {
 				StringBuffer path = new StringBuffer();
 				
 				for(int depth = 1; depth < paths.length; ++depth) {
 					path.append("/" + paths[depth]);
 				}
 
 				if(!node.isLeaf() && node.getChildAt(0).toString().equals("...")) {
 					node.remove(0);
 					SftpAdapter.GetDirectoryList(path.toString());
 				}
 
 				FileList.GetInstance().UpdateList(path.toString());
 			}
 		}.start();
 	}
 	
 	public void ChangeDirectory(String directory) {
 		DefaultMutableTreeNode childNode = null;
 		for(int i=0; i<lastNode.getChildCount(); ++i) {
 			if(lastNode.getChildAt(i).toString().equals(directory)) {
 				childNode = (DefaultMutableTreeNode)lastNode.getChildAt(i);
 				break;
 			}
 		}
 		if(childNode == null) return;
 		lastNode = childNode;
 		lastPath = lastPath.pathByAddingChild(lastNode);
 		WebhardFrame.GetInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 		UpdateNode(lastPath.getPath(), lastNode);
 	}
 	
 	public void UpdateStatus(final int type, final Object arg) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				if(type == SftpListener.DIRLIST_DONE) {
 					Vector<?> dirList = (Vector<?>)arg;
 					DirectoryTree.GetInstance().UpdateTreeDone(dirList);
 				}
 			}
 		});
 	}
 }
