 package se.gustavkarlsson.snap.gui.pages.send.choosefiles;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSourceAdapter;
 import org.eclipse.swt.dnd.DragSourceEvent;
 
 import se.gustavkarlsson.snap.domain.Node;
 import se.gustavkarlsson.snap.util.LoggerHelper;
 
 public class InternalFileDragListener extends DragSourceAdapter {
 	private static final Logger logger = LoggerHelper.getLogger();
 	private final StructuredViewer viewer;
 
 	public InternalFileDragListener(StructuredViewer viewer) {
 		this.viewer = viewer;
 	}
 
 	/**
 	 * Method declared on DragSourceListener
 	 */
 
 	@Override
 	public void dragSetData(DragSourceEvent event) {
 		IStructuredSelection fileTreeSelection = (IStructuredSelection) viewer
 				.getSelection();
 
		if (!InternalFileTransfer.getInstance().isSupportedType(event.dataType)) {
 			logger.error("Unsupported transfer type");
 			return;
 		}
 		Set<List<String>> paths = new HashSet<List<String>>();
 
 		List<String> firstParentPath = null;
 		for (Object object : fileTreeSelection.toArray()) {
 			Node node = (Node) object;
 			List<String> path = node.getNodePath(new LinkedList<String>());
 
 			List<String> parentPath = path.subList(0, path.size() - 1);
 			if (firstParentPath == null) {
 				// Set first parent path
 				firstParentPath = parentPath;
 			} else if (!parentPath.equals(firstParentPath)) {
 				// Parent path is different, so return
 				return;
 			}
 			paths.add(path);
 		}
 
 		boolean copyAction = (event.detail & DND.DROP_COPY) == DND.DROP_COPY;
 		InternalFileDndPayload payload = new InternalFileDndPayload(paths,
 				copyAction);
 
 		event.data = payload;
 	}
 }
