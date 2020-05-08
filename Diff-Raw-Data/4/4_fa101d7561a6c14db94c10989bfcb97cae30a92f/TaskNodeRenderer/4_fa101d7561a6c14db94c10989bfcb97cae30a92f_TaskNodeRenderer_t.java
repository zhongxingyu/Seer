 /* TaskNodeRenderer.java - Part of Task Mistress
  * Written in 2012 by anonymous.
  * 
  * To the extent possible under law, the author(s) have dedicated all copyright and related and neighbouring rights to
  * this software to the public domain worldwide. This software is distributed without any warranty.
  * 
  * Full license at <http://creativecommons.org/publicdomain/zero/1.0/>.
  */
 
 package anonpds.TaskMistress;
 
 import java.awt.Component;
 import java.awt.MediaTracker;
 import java.net.URL;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultTreeCellRenderer;
 
 /**
  * Renders the cells (task nodes) of the treeView.
  * @author anonpds <anonpds@gmail.com>
  */
 @SuppressWarnings("serial")
 public class TaskNodeRenderer extends DefaultTreeCellRenderer {
 	/** Resource/file name of the icon that represents finished task. */
 	private static final String DONE_ICON_FILE = "res/done.gif";
 	
 	/** Resource/file name of the icon that represents unfinished task. */
 	private static final String UNDONE_ICON_FILE = "res/undone.gif";
 	
 	/** Resource/file name of the icon that represents default task. */
 	private static final String DEFAULT_ICON_FILE = "res/default.gif";
 	
 	/** Resource/file name of the icon that represents task folder with all tasks finished. */
 	private static final String DONE_FOLDER_ICON_FILE = "res/done-folder.gif";
 	
 	/** Resource/file name of the icon that represents task folder with some tasks unfinished. */
 	private static final String UNDONE_FOLDER_ICON_FILE = "res/undone-folder.gif";
 
 	/** Resource/file name of the icon that represents default task folder. */
 	private static final String DEFAULT_FOLDER_ICON_FILE = "res/folder.gif";
 	
 	/** Icon that represents done task. */
 	private Icon doneIcon;
 
 	/** Icon that represents undone task. */
 	private Icon undoneIcon;
 
 	/** Icon that represents default task without done/undone status. */
 	private Icon defaultIcon;
 
 	/** Icon that represents folder with all tasks done. */
 	private Icon doneFolderIcon;
 
 	/** Icon that represents folder with some tasks undone. */
 	private Icon undoneFolderIcon;
 
 	/** Icon that represents folder with only default tasks. */
 	private Icon defaultFolderIcon;
 	
 	/** Default constructor. */
 	public TaskNodeRenderer() {
 		this.loadIcons();
 	}
 	
 	/**
 	 * Loads an icon from file system or by using the class loader to find the specified resource.
 	 * @param name the file name or resource name
 	 * @return the loaded icon
 	 */
 	private static Icon loadIcon(String name) {
 		/* TODO add debugging */
 		/* TODO perhaps change the priority and try file first and class loader then? */
 		ImageIcon icon;
 
		URL url = TaskMistress.class.getClassLoader().getResource(name);
 		if (url != null) icon = new ImageIcon(url);
 		else icon = new ImageIcon(name);
 
 		if (icon.getImageLoadStatus() == MediaTracker.ERRORED) throw new RuntimeException("Icon " + name + " not found.");

 		return icon;
 	}
 	
 	/** Loads the icons used by the renderer. */
 	private void loadIcons() {
 		this.doneIcon = loadIcon(DONE_ICON_FILE);
 		this.undoneIcon = loadIcon(UNDONE_ICON_FILE);
 		this.defaultIcon = loadIcon(DEFAULT_ICON_FILE);
 		this.doneFolderIcon = loadIcon(DONE_FOLDER_ICON_FILE);
 		this.undoneFolderIcon = loadIcon(UNDONE_FOLDER_ICON_FILE);
 		this.defaultFolderIcon = loadIcon(DEFAULT_FOLDER_ICON_FILE);
 	}
 	
 	@Override
 	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
 						boolean leaf, int row, boolean hasFocus) {
 		/* get the default renderer */
 		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
 		
 		/* set the icon */
 		TaskNode node = (TaskNode) value;
 		Task task = node.getTask();
 		Icon icon = this.defaultIcon;
 		
 		if (task == null) return this;
 		
 		if (node.getChildCount() > 0) {
 			switch (task.getStatus()) {
 			case Task.STATUS_DONE: icon = this.doneFolderIcon;
 			case Task.STATUS_UNDONE: icon = this.undoneFolderIcon;
 			case Task.STATUS_DEFAULT: icon = this.defaultFolderIcon;
 			}
 		} else {
 			switch (task.getStatus()) {
 			case Task.STATUS_DONE: icon = this.doneIcon;
 			case Task.STATUS_UNDONE: icon = this.undoneIcon;
 			case Task.STATUS_DEFAULT: icon = this.defaultIcon;
 			}
 		}
 		
 		this.setIcon(icon);
 		
 		return this;
 	}
 }
