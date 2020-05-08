 package org.codefaces.ui.internal.views;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.codefaces.core.events.WorkspaceChangeEvent;
 import org.codefaces.core.events.WorkspaceChangeListener;
 import org.codefaces.core.models.RepoFile;
 import org.codefaces.core.models.RepoFolder;
 import org.codefaces.core.models.RepoResource;
 import org.codefaces.core.models.RepoResourceType;
 import org.codefaces.core.models.Workspace;
 import org.codefaces.ui.internal.Images;
 import org.codefaces.ui.internal.StatusManager;
 import org.codefaces.ui.internal.actions.SwitchBranchAction;
 import org.codefaces.ui.internal.commands.CommandExecutor;
 import org.codefaces.ui.internal.commands.OpenFileCommandHandler;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.IOpenListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.OpenEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.part.ViewPart;
 
 public class ProjectExplorerViewPart extends ViewPart {
 	public static final String ID = "org.codefaces.ui.view.projectExplorer";
 
 	private static final String VIEWER_CONTEXT_MENU_ID = ID + "#viewer";
 
 	private TreeViewer viewer;
 
 	private Workspace workspace;
 
 	private StatusManager statusManager;
 
 	private UpdateBranchWorkspaceChangeListener updateBranchListener = new UpdateBranchWorkspaceChangeListener();
 
 	private ProjectExplorerTreeViewManager manager;
 
 	private final class UpdateBranchWorkspaceChangeListener implements
 			WorkspaceChangeListener {
 		@Override
 		public void workspaceChanged(WorkspaceChangeEvent evt) {
 			update(evt.getRepoBranch());
 		}
 	}
 
 	private class ProjectExplorerContentProvider implements
 			ITreeContentProvider {
 
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 		}
 
 		public void dispose() {
 		}
 
 		public Object[] getElements(Object parent) {
 			return getChildren(parent);
 		}
 
 		public Object getParent(Object child) {
 			if (child instanceof RepoResource) {
 				return ((RepoResource) child).getParent();
 			}
 			return null;
 		}
 
 		public Object[] getChildren(Object parent) {
 			return manager.getElement(parent);
 		}
 
 		public boolean hasChildren(Object parent) {
 			if (parent instanceof RepoResource) {
 				return ((RepoResource) parent).hasChildren();
 			}
 
 			return false;
 		}
 	}
 
 	private class ProjectExplorerLabelProvider extends LabelProvider {
 		public String getText(Object obj) {
 			return manager.getText(obj);
 		}
 
 		public Image getImage(Object obj) {
 			return manager.getImage(obj);
 		}
 	}
 
 	private class ProjectExplorerViewerComparator extends ViewerComparator {
 		@Override
 		public int compare(Viewer viewer, Object obj1, Object obj2) {
 			if (obj1 instanceof RepoFile && obj2 instanceof RepoFolder) {
 				return 1;
 			}
 
 			if (obj1 instanceof RepoFolder && obj2 instanceof RepoFile) {
 				return -1;
 			}
 
 			return super.compare(viewer, obj1, obj2);
 		}
 	}
 
 	private class FolderOpenListener implements IOpenListener {
 		@Override
 		public void open(OpenEvent event) {
 			IStructuredSelection selection = (IStructuredSelection) event
 					.getSelection();
			if (selection.isEmpty()) {
 				return;
 			}
 
 			RepoResource clickedRepoResource = (RepoResource) selection
 					.getFirstElement();
 			Viewer viewer = event.getViewer();
 			if (clickedRepoResource instanceof RepoFolder
 					&& viewer instanceof TreeViewer) {
 				TreeViewer treeViewer = (TreeViewer) viewer;
 				if (treeViewer.isExpandable(clickedRepoResource)) {
 					treeViewer.setExpandedState(clickedRepoResource,
 							!treeViewer.getExpandedState(clickedRepoResource));
 				}
 			}
 		}
 	}
 
 	private class FileOpenListener implements IOpenListener {
 		@Override
 		public void open(OpenEvent event) {
 			IStructuredSelection selection = (IStructuredSelection) event
 					.getSelection();
			if (selection.isEmpty()) {
 				return;
 			}
 
 			RepoResource clickedRepoResource = (RepoResource) selection
 					.getFirstElement();
 			if (clickedRepoResource.getType() == RepoResourceType.FILE) {
 				Map<String, String> parameterMap = new HashMap<String, String>();
 				parameterMap.put(OpenFileCommandHandler.PARAM_MODE,
 						OpenFileCommandHandler.MODE_DIRECT_FILES);
 
 				Map<String, Object> variableMap = new HashMap<String, Object>();
 				variableMap.put(OpenFileCommandHandler.VARIABLE_FILES,
 						new RepoFile[] { (RepoFile) clickedRepoResource });
 
 				CommandExecutor.execute(OpenFileCommandHandler.ID,
 						parameterMap, variableMap);
 			}
 		}
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		createViewer(parent);
 
 		hookWorkspace();
 		createToolBar(parent);
 
 		statusManager = new StatusManager(getViewSite().getActionBars()
 				.getStatusLineManager(), getViewer());
 
 		registerContextMenu(viewer);
 	}
 
 	private void hookWorkspace() {
 		workspace = Workspace.getCurrent();
 		RepoFolder cachedBranch = workspace.getWorkingBranch();
 		if (cachedBranch != null) {
 			update(cachedBranch);
 		}
 		workspace.addWorkspaceChangeListener(updateBranchListener);
 	}
 
 	@Override
 	public void dispose() {
 		if (manager != null) {
 			manager.dispose();
 		}
 
 		workspace.removeWorkspaceChangeListener(updateBranchListener);
 		super.dispose();
 	}
 
 	/**
 	 * create and register a context menu associate to the explorer tree viewer
 	 * 
 	 * @param viewer
 	 *            - the project explorer tree-viewer
 	 */
 	private void registerContextMenu(TreeViewer viewer) {
 		MenuManager contextMenuManager = new MenuManager();
 		contextMenuManager.add(new Separator(
 				IWorkbenchActionConstants.MB_ADDITIONS));
 		Menu menu = contextMenuManager.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(VIEWER_CONTEXT_MENU_ID,
 				contextMenuManager, viewer);
 		getSite().setSelectionProvider(viewer);
 	}
 
 	public StatusManager getStatusManager() {
 		return statusManager;
 	}
 
 	/**
 	 * Create and initialize the viewer
 	 */
 	private void createViewer(Composite parent) {
 		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
 				| SWT.BORDER);
 		viewer.setContentProvider(new ProjectExplorerContentProvider());
 		viewer.setLabelProvider(new ProjectExplorerLabelProvider());
 		viewer.setComparator(new ProjectExplorerViewerComparator());
 		viewer.addOpenListener(new FileOpenListener());
 		viewer.addOpenListener(new FolderOpenListener());
 		viewer.setComparator(new ProjectExplorerViewerComparator());
 
 	}
 
 	/**
 	 * Update the Explorer input to the given RepoResource.
 	 * 
 	 * @param workingBranch
 	 *            the new working branch
 	 */
 	public void update(RepoFolder workingBranch) {
 		if (manager != null) {
 			manager.dispose();
 		}
 		manager = new ProjectExplorerTreeViewManager(viewer);
 		viewer.setInput(workingBranch);
 	}
 
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	public TreeViewer getViewer() {
 		return viewer;
 	}
 
 	/**
 	 * Create and fill in the tool bar
 	 */
 	private void createToolBar(Composite parent) {
 		IToolBarManager toolbar = getViewSite().getActionBars()
 				.getToolBarManager();
 		Action switchBranchAction = new SwitchBranchAction();
 
 		switchBranchAction.setImageDescriptor(Images
 				.getImageDescriptorFromRegistry(Images.IMG_BRANCHES));
 		toolbar.add(switchBranchAction);
 	}
 
 }
