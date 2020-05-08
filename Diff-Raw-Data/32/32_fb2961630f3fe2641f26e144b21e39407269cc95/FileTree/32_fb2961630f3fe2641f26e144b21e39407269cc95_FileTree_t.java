 package gui;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JTree;
 import javax.swing.border.LineBorder;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 public class FileTree extends JPanel{
 
 	private static final long serialVersionUID = -2836582864395401650L;
 
 	private JTree fileTree;
 	private FileSystemModel fileSystemModel;
 	private JTextArea fileDetailsTextArea = new JTextArea("");
 	
 	FileTree(){
 		setBackground(Color.RED);
 		setPreferredSize(new Dimension(250, 100));
 		
 		String directory = "C:\\";
 		
 		fileDetailsTextArea.setEditable(false);
 		fileDetailsTextArea.setPreferredSize(new Dimension(250, 50));
 		fileSystemModel = new FileSystemModel(new File(directory));
 		fileTree = new JTree(fileSystemModel);
 		fileTree.setEditable(true);
 		fileTree.addTreeSelectionListener(new TreeSelectionListener() {
 			public void valueChanged(TreeSelectionEvent event) {
 			File file = (File) fileTree.getLastSelectedPathComponent();
 			fileDetailsTextArea.setText(getFileDetails(file));
 			}
 		});
 		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, new JScrollPane(fileDetailsTextArea), 
 				new JScrollPane(fileTree));
 	    
 		LineBorder border = new LineBorder(Color.GREEN, 3, true);
 		splitPane.setBorder(border);
 		
 		add(splitPane);
 	}
 
 	public void paint(Graphics g){
 		super.paint(g);
 	}
 	
 	private String getFileDetails(File file) {
 		if (file == null)
 			return "";
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("Name: " + file.getName() + "\n");
 		buffer.append("Path: " + file.getPath() + "\n");
 		buffer.append("Size: " + file.length() + "\n");
 		return buffer.toString();
 	}
 }
 
 class FileSystemModel implements TreeModel {
 	private File root;
 
 	//	  private Vector listeners = new Vector();
 	private ArrayList<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
 
 	public FileSystemModel(File rootDirectory) {
 		root = rootDirectory;
 	}
 
 	public Object getRoot() {
 		return root;
 	}
 
 	public Object getChild(Object parent, int index) {
 		File directory = (File) parent;
 		String[] children = directory.list();
 		return new TreeFile(directory, children[index]);
 	}
 
 	public int getChildCount(Object parent) {
 		File file = (File) parent;
 		if (file.isDirectory()) {
 			String[] fileList = file.list();
 			if (fileList != null)
 				return file.list().length;
 		}
 		return 0;
 	}
 
 	public boolean isLeaf(Object node) {
 		File file = (File) node;
 		return file.isFile();
 	}
 
 	public int getIndexOfChild(Object parent, Object child) {
 		File directory = (File) parent;
 		File file = (File) child;
 		String[] children = directory.list();
 		for (int i = 0; i < children.length; i++) {
 			if (file.getName().equals(children[i])) {
 				return i;
 			}
 		}
 		return -1;
 
 	}
 
 	public void valueForPathChanged(TreePath path, Object value) {
 		File oldFile = (File) path.getLastPathComponent();
 		String fileParentPath = oldFile.getParent();
 		String newFileName = (String) value;
 		File targetFile = new File(fileParentPath, newFileName);
 		oldFile.renameTo(targetFile);
 		File parent = new File(fileParentPath);
 		int[] changedChildrenIndices = { getIndexOfChild(parent, targetFile) };
 		Object[] changedChildren = { targetFile };
 		fireTreeNodesChanged(path.getParentPath(), changedChildrenIndices, changedChildren);
 
 	}
 
 	private void fireTreeNodesChanged(TreePath parentPath, int[] indices, Object[] children) {
 		TreeModelEvent event = new TreeModelEvent(this, parentPath, indices, children);
 		Iterator<TreeModelListener> iterator = listeners.iterator();
 		TreeModelListener listener = null;
 		while (iterator.hasNext()) {
 			listener = (TreeModelListener) iterator.next();
 			listener.treeNodesChanged(event);
 		}
 	}
 
 	public void addTreeModelListener(TreeModelListener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeTreeModelListener(TreeModelListener listener) {
 		listeners.remove(listener);
 	}
 
 	private class TreeFile extends File {
 		private static final long serialVersionUID = -5775093232151119831L;
 
 		public TreeFile(File parent, String child) {
 			super(parent, child);
 		}
 
 		public String toString() {
 			return getName();
 		}
 	}
 }
 
 
 
 
 
 
 
