 /* TaskStore
  * Written in 2012 by anonymous.
  * 
  * To the extent possible under law, the author(s) have dedicated all copyright and related and neighbouring rights to
  * this software to the public domain worldwide. This software is distributed without any warranty.
  * 
  * Full license at <http://creativecommons.org/publicdomain/zero/1.0/>.
  */
 
 package anonpds.TaskMistress;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 
 /* TODO separate the TaskStore from the TaskTreeNode:
  *      this is essential to allow different methods of storing the nodes on disk (for example as XML).
  *      There should be a general TaskTreeNode structure that only contains the node data. The class can then be
  *      overwritten by different TaskStore modules to include the information that is relevant to those classes. 
  */
 
 /* TODO how to fix movement of nodes:
  *      first: the fs names of moved nodes need to be set to null, since they may no longer be valid
  *      second: the old node folders need to be removed or moved (the latter is probably more complex)
  *        - this could be implemented by first adding the exist/not-exist flag to nodes, and then actually making
  *          a copy of the moved nodes, instead of really moving them; the old nodes would be marked non-existent
  *          and would thus be removed from the file system the next time the nodes are saved
  *        - the above way is quite inefficient, but it should do for now -- it will not be a problem, unless there
  *          are huge numbers of nodes to move/save 
  */
 
/* TODO this class should probably use a TreeModelListener, instead of storing the root node itself. */

 /**
  * A class that handles the storage of task trees in Task Mistress.
  * @author anonpds <anonpds@gmail.com>
  */
 public class TaskStore {
 	/** The name of the file that contains task meta data. */
 	private static final String META_FILE = "meta.txt";
 
 	/** The name of the file that contains task text. */
 	private static final String TEXT_FILE = "task.txt";
 	
 	/** The tree model that contains the stored task tree. */
 	private DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
 
 	/** The file system path in which the file is stored. */
 	private File path;
 	
 	/**
 	 * Creates a new task store from the specified directory. The directory is created if it doesn't exist and an
 	 * empty task store is initialised.
 	 * @param path the directory that stores the task tree
 	 * @throws Exception on any IO errors
 	 */
 	public TaskStore(File path) throws Exception {
 		/* make sure the path exists or can at least be created */
 		if (!path.exists() && !path.mkdirs()) throw new Exception("cannot create '" + path.getPath() + "'");
 		this.path = path;
 		
 		/* add the directories and their sub-directories recursively as nodes */
 		/* TODO the addTaskDirectory function should be able to deal with the root node, so that there is no need
 		 * for this ugle bit of code.
 		 */
 		File[] files = this.path.listFiles();
 		for (File file : files) {
 			if (file.isDirectory()) this.addTaskDirectory((DefaultMutableTreeNode) this.treeModel.getRoot(), file);
 		}
 		
 		/* DEBUG */ print();
 	}
 
 	/**
 	 * Adds the task to the store from a directory and recursively adds all the sub-tasks in sub-directories.
 	 * @param tree the tree node to add to
 	 * @param path the directory path to add
 	 * @throws Exception on any IO or parse errors
 	 */
 	private void addTaskDirectory(DefaultMutableTreeNode tree, File path) throws Exception {
 		/* must be a directory */
 		if (!path.isDirectory()) throw new Exception("'" + path.getPath() + "' not a directory");
 
 		/* the directory must contain the meta data file; skip the directory if doesn't exist */
 		File metaFile = new File(path, META_FILE);
 		if (!metaFile.exists()) return;
 
 		/* read the meta data */
 		/* TODO implement a better way to handle the meta data */
 		String name = null, date = null;
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(metaFile));
 			name = reader.readLine();
 			date = reader.readLine();
 			reader.close();
 		} catch (Exception e) {
 			throw new Exception("can not access '" + metaFile.getPath() + "': " + e.getMessage());
 		}
 
 		/* validate and parse the meta data */
 		if (name == null) throw new Exception("no name in metadata " + metaFile.getPath());
 		if (date == null) throw new Exception("no date in metadata " + metaFile.getPath());
 		long timeStamp = Long.parseLong(date);
 		
 		/* read the task text if it exists */
 		File textFile = new File(path, TEXT_FILE);
 		String text = "";
 		if (textFile.exists()) {
 			try {
 				String line;
 				BufferedReader reader = new BufferedReader(new FileReader(textFile));
 				while ((line = reader.readLine()) != null) {
 					if (text.length() > 0) text = text + "\n";
 					text = text + line;
 				}
 				reader.close();
 			} catch (Exception e) { /* TODO error handling */ }
 		}
 
 		/* create the task from the read data */
 		Task task = new Task(name, path.getName(), text, timeStamp, false);
 		
 		/* add the node to the tree */
 		DefaultMutableTreeNode node = new DefaultMutableTreeNode(task);
 		tree.add(node);
 		
 		/* finally recurse into any potential sub-directories */
 		File[] files = path.listFiles();
 		for (File file : files) {
 			if (file.isDirectory()) this.addTaskDirectory(node, file);
 		}
 	}
 	
 	/**
 	 * Returns the root node of the tree.
 	 * @return the root node
 	 */
 	public DefaultMutableTreeNode getRoot() {
 		return (DefaultMutableTreeNode) this.treeModel.getRoot();
 	}
 
 	/**
 	 * Adds a named node as a child of the given node.
 	 * @param parent the parent node
 	 * @param name the name of the new node to add
 	 * @return the added node
 	 */
 	public DefaultMutableTreeNode addChild(DefaultMutableTreeNode parent, String name) {
 		/* TODO rename this to just add; I considered having both addChild and addSibling, but now I can't think of
 		 * a reason to have addSibling, when you can just call: addChild(node.getParent(), name)
 		 */
 		Task task = new Task(name, null, "", System.currentTimeMillis(), true);
 		DefaultMutableTreeNode node = new DefaultMutableTreeNode(task);
 		parent.add(node);
 		return(node);
 	}
 
 	/**
 	 * Returns the file system path of the node.
 	 * @param node the node to query
 	 * @return the file system path to the node
 	 */
 	private File getNodePath(DefaultMutableTreeNode node) {
 		/* start from the task tree root directory */
 		File path = this.path;
 
 		/* traverse the tree path to this node */
 		TreeNode[] treePath = node.getPath();
 		
 		/* start traversal from second path object; first is root and already accounted for */
 		for (int i = 1; i < treePath.length; i++) {
 			/* get the Task object from the tree node */
 			DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) treePath[i];
 			Task task = (Task) curNode.getUserObject();
 			
 			/* get the file system (plain) name of the task; create it, if it's not set */
 			String curPath = task.getPlainName();
 			if (curPath == null) this.setFileSystemName(path, curNode);
 			
 			/* add the task directory to path */
 			path = new File(path, task.getPlainName());
 		}
 		
 		return path;
 	}
 	
 	/**
 	 * Deletes a directory and all its contents recursively.
 	 * @param path the directory to delete
 	 */
 	private void deleteDirectory(File path) {
 		if (!path.isDirectory()) return;
 		File[] files = path.listFiles();
 		for (File file : files) {
 			if (file.isDirectory()) deleteDirectory(file); /* recurse on directories */
 			else file.delete(); /* delete normal files; TODO errors */
 		}
 		/* when the directory is finally empty, delete itself; TODO errors*/
 		path.delete();
 	}
 	
 	/**
 	 * Removes a node and all its children from the tree.
 	 * @param node the node to remove
 	 */
 	public void remove(DefaultMutableTreeNode node) {
 		/* TODO perhaps the node should just be marked non-existing, so that it could be removed from the file system
 		 * the next time the tree is saved (at the same time the node would be removed from memory) 
 		 */
 		if (node.isRoot()) return; /* never remove the root node */
 		
 		/* delete the file system path of the node and its children */
 		File path = this.getNodePath(node);
 		this.deleteDirectory(path);
 		
 		/* remove from the task tree */
 		node.removeFromParent();
 	}
 	
 	/**
 	 * Moves a node and all its children under another node.
 	 * @param dest the destination node
 	 * @param node the node to move
 	 */
	public void move(TaskTreeNode dest, TaskTreeNode node) {
 		/* CRITICAL this will mess out the file system layout; do not use it yet! */
 		
 		/* never move root node */
 		if (node.isRoot()) return;
 		
		node.getParent().remove(node);
 		dest.add(node);
 	}
 	
 	/**
 	 * Writes the tasks to disk. 
 	 * @throws Exception on any error
 	 */
 	public void writeOut() throws Exception {
 		/* TODO add a version of this that takes the root path as a parameter; useful for when there is an error
 		 * saving to the path set in the class instance, so the tree can be saved to another location
 		 */
 		this.writeOutRecurse(this.path, this.getRoot());
 	}
 
 	/**
 	 * Writes the tasks in given node and all its children to disk recursively.
 	 * @param path the path to write to
 	 * @param node the node to write out
 	 * @throws Exception on IO errors
 	 */
 	private void writeOutRecurse(File path, DefaultMutableTreeNode node) throws Exception {
 		/* get the user object from the node */
 		Task task = (Task) node.getUserObject();
 		/* DEBUG */ if (task != null) System.out.println("Writing out " + this.path.getPath() + ", " + task.getName() + " (" + task.isDirty() + ")");
 		/* create the path if it doesn't exist */
 		if (!path.exists() && !path.mkdirs()) throw new Exception("can not create " + path);
 		
 		/* write the node if not root and if dirty */
 		if (!node.isRoot() && task.isDirty()) {
 			/* write the meta data */
 			File metaFile = new File(path, META_FILE);
 			try {
 				BufferedWriter writer = new BufferedWriter(new FileWriter(metaFile));
 				writer.write(task.getName() + "\n");
 				writer.write(Long.toString(task.getCreationTime()) + "\n");
 				writer.close();
 			} catch (Exception e) {
 				throw new Exception("can not write to " + metaFile.getPath() + ": " + e.getMessage());
 			}
 			
 			/* write the task text, if any */
 			File textFile = new File(path, TEXT_FILE);
 			try {
 				BufferedWriter writer = new BufferedWriter(new FileWriter(textFile));
 				if (task.getText() != null) writer.write(task.getText());
 				writer.close();
 			} catch (Exception e) {
 				throw new Exception("can not write to " + textFile.getPath() + ": " + e.getMessage());
 			}
 		}
 		
 		/* recurse for each of the child nodes */
 		for (int i = 0; i < node.getChildCount(); i++) {
 			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
 			Task childTask = (Task) child.getUserObject();
 			
 			/* make sure the file system name has been set for the node */
 			this.setFileSystemName(path, child);
 			File newPath = new File(path, childTask.getPlainName());
 			this.writeOutRecurse(newPath, child);
 		}
 	}
 
 	/**
 	 * Sets the file system name of a tree node, if it isn't set already.
 	 * @param path the file system path where the node resides
 	 * @param curNode the node
 	 */
 	/* TODO this can be implemented without the path parameter */
 	private void setFileSystemName(File path, DefaultMutableTreeNode curNode) {
 		/* don't set for root */
 		if (curNode.isRoot()) return;
 		
 		/* get the user object from the tree node */
 		Task task = (Task) curNode.getUserObject();
 		
 		/* don't set name if one already exists */
 		if (task.getPlainName() != null) return;
 
 		/* remove all silly characters from the node name and make everything lower-case */
 		/* TODO limit the file system name to something like 20 characters */
 		String name = "";
 		for (int i = 0; i < task.getName().length(); i++) {
 			char ch = task.getName().charAt(i);
 			if (ch < 256 && Character.isLetterOrDigit(ch)) name = name + Character.toLowerCase(ch);
 		}
 		
 		/* if any characters left, try the name as is */
 		if (name.length() > 0) {
 			File newPath = new File(path, name);
 			if (!newPath.exists()) {
 				/* success */
 				task.setPlainName(name);
 				return;
 			}
 		}
 		
 		/* append numbers at the end of the name until non-existing path is found */
 		/* TODO only 100 numbers are tried at most! Do something about this. */
 		for (int i = 0; i < 100; i++) {
 			String newName = name + i;
 			File newPath = new File(path, newName);
 			if (!newPath.exists()) {
 				/* success! */
 				task.setPlainName(newName);
 				return;
 			}
 		}
 		
 		/* error, no suitable name found */
 		throw new RuntimeException("no suitable path found for " + task.getName() + " in " + path.getPath());
 	}
 
 	/* DEBUG */
 	@SuppressWarnings("javadoc")
 	public void print() {
 		print(0, this.getRoot());
 	}
 	/* DEBUG */
 	@SuppressWarnings("javadoc")
 	public void print(int depth, DefaultMutableTreeNode node) {
 		Task task = (Task) node.getUserObject();
 		for (int i = 0; i < depth; i++) System.out.print("  ");
 		if (task != null) System.out.println(task.getName() + ": " + task.getText());
 		for (int i = 0; i < node.getChildCount(); i++)
 			print(depth + 1, (DefaultMutableTreeNode) node.getChildAt(i));
 	}
 }
