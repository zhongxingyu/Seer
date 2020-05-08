 package se.gustavkarlsson.snap.gui.pages.send.choosefiles;
 
 import java.io.File;
 import java.util.List;
 
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerDropAdapter;
 import org.eclipse.swt.dnd.TransferData;
 
 import se.gustavkarlsson.snap.domain.FileNode;
 import se.gustavkarlsson.snap.domain.FolderNode;
 import se.gustavkarlsson.snap.domain.Node;
 import se.gustavkarlsson.snap.util.FileUtils;
 
 public class FileTreeDropListener extends ViewerDropAdapter {
 
	FolderNode root = null;
	
	public FileTreeDropListener(TreeViewer viewer, FolderNode root) {
 		super(viewer);
		this.root = root;
 	}
 
 	@Override
 	public boolean validateDrop(Object target, int op, TransferData type) {
 		switch (getCurrentLocation()) {
 		case LOCATION_BEFORE:
 			return true;
 		case LOCATION_ON:
 			return (target instanceof FolderNode);
 		case LOCATION_AFTER:
 			return true;
 		case LOCATION_NONE:
 			// Root
 			return true;
 		default:
 			// TODO Error message
 			return false;
 		}
 	}
 
 	@Override
 	public boolean performDrop(Object data) {
 		FolderNode targetParent;
 		switch (getCurrentLocation()) {
 		case LOCATION_BEFORE:
 			targetParent = ((Node) getCurrentTarget()).getParent();
 			break;
 		case LOCATION_ON:
 			targetParent = (FolderNode) getCurrentTarget();
 			break;
 		case LOCATION_AFTER:
 			targetParent = ((Node) getCurrentTarget()).getParent();
 			break;
 		case LOCATION_NONE:
 			// Root
 			targetParent = (FolderNode) getViewer().getInput();
 			break;
 		default:
 			// TODO Error message
 			return false;
 		}
 
 		if (data instanceof String[]) {
 			String[] filePaths = (String[]) data;
 
 			for (String filePath : filePaths) {
 				addFileTreeFromFileSystemPath(targetParent, filePath);
 			}
 		} else if (data instanceof InternalFileDndPayload) {
 			InternalFileDndPayload payload = (InternalFileDndPayload) data;
 			for (List<String> path : payload.getNodes()) {
				Node node = getInternalFileFromPath(root, path); // FIXME targetparent ska vara root?
 				if (payload.isCopyAction()) {
 					copyInternalFile(node, targetParent);
 				} else {
 					moveInternalFile(node, targetParent);
 				}
 			}
 		} else {
 			// TODO error message
 			return false;
 		}
 		
 		getViewer().refresh();
 		return true;
 	}

 	private static boolean moveInternalFile(Node file, FolderNode targetParent) {
 		return targetParent.addChild(file);
 	}
 	
 	private static boolean copyInternalFile(Node file, FolderNode targetParent) {
 		Node clone = file.clone();
 		return targetParent.addChild(clone);
 	}
 	
 	private static Node getInternalFileFromPath(FolderNode root, List<String> path) {
 		Node currentNode = root;
 		
 		while (!path.isEmpty()) {
 			String nextNodeName = path.get(0);
 			
 			if (!(currentNode instanceof FolderNode)) {
 				// TODO: handle error
 			}
 			
 			for (Node child : ((FolderNode) currentNode).getChildren()) {
 				if (child.getName().equals(nextNodeName)) {
 					currentNode = child;
 					path.remove(0);
 					break;
 				}
 			}
 		}
 		
 		return currentNode;
 	}
 
 	private static void addFileTreeFromFileSystemPath(FolderNode parent, String fileSystemPath) {
 		File file = new File(fileSystemPath);
 
 		if (file.isFile()) {
 			parent.addChild(new FileNode(fileSystemPath));
 		} else {
 			FolderNode folder = new FolderNode(
 					FileUtils.extractFileNameFromPath(fileSystemPath));
 			if (file.listFiles() == null) {
 				// TODO Error message (could not list)
 			} else {
 				for (File child : file.listFiles()) {
 					addFileTreeFromFileSystemPath(folder, child.getAbsolutePath());
 				}
 			}
 			parent.addChild(folder);
 		}
 	}
 }
