 package org.zend.php.zendserver.deployment.ui.targets;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.action.ContributionManager;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerDropAdapter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.TransferData;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.menus.IMenuService;
 import org.eclipse.ui.part.ResourceTransfer;
import org.zend.php.zendserver.deployment.core.descriptor.DescriptorContainerManager;
import org.zend.php.zendserver.deployment.core.descriptor.IDeploymentDescriptor;
import org.zend.php.zendserver.deployment.core.descriptor.IDescriptorContainer;
import org.zend.php.zendserver.deployment.core.descriptor.ProjectType;
 import org.zend.php.zendserver.deployment.ui.Activator;
import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.IZendTarget;
import org.zend.webapi.core.connection.data.values.ZendServerVersion;
 
 public class DropTransferListener extends ViewerDropAdapter {
 
 	private static final String PROJECT_NAME = "projectName"; //$NON-NLS-1$
 	private static final String TARGET_ID = "targetId"; //$NON-NLS-1$
 	
 	private static final String DROP_MENU = "dropMenu"; //$NON-NLS-1$
	private static final String APP_MENU_LOCATION_URI = "menu:org.zend.php.zendserver.deployment.ui.targets.TargetsViewer"; //$NON-NLS-1$
	private static final String LIBRARY_MENU_LOCATION_URI = "menu:org.zend.php.zendserver.deployment.ui.targets.targetsViewer.Library"; //$NON-NLS-1$
 
 	private Viewer viewer;
 	
 	protected DropTransferListener(Viewer viewer) {
 		super(viewer);
 		this.viewer = viewer;
 	}
 
 	@Override
 	public boolean performDrop(Object data) {
 		IResource[] resources = (IResource[]) data;
 		List<String> projectNames = new ArrayList<String>();
 		for (IResource res : resources) {
 			if (res instanceof IProject) {
 				projectNames.add(res.getName());
 			}
 		}
 		if (projectNames.size() == 0) {
 			return false;
 		}
 		
 		// can cast safely, after type check in #validateDrop()
 		IZendTarget target = (IZendTarget) getCurrentTarget();
 		
 		Display display = Display.getDefault();
 		final Menu menu = new Menu(viewer.getControl().getShell(), SWT.POP_UP);
 		
 		ContributionManager cm = new ContributionManager() {
 			
 			public void update(boolean force) {
 				for (IContributionItem item : getItems()) {
 					item.fill(menu, -1);
 				}
 			}
 		};
 		cm.add(new GroupMarker(DROP_MENU));
 		
 		IMenuService service = (IMenuService) PlatformUI.getWorkbench().getService(IMenuService.class);
 		
 		// pass selected objects and drop target as a context
 		Object projName = projectNames.size() == 1 ?  projectNames.get(0) : projectNames.toArray(new String[projectNames.size()]);
 		service.getCurrentState().addVariable(PROJECT_NAME, projName);
 		service.getCurrentState().addVariable(TARGET_ID, target.getId());
 		
		if (isLibrary(resources[0].getProject())) {
			if (TargetsManager.checkMinVersion(target,
					ZendServerVersion.byName("6.1.0"))) { //$NON-NLS-1$
				service.populateContributionManager(cm,
						LIBRARY_MENU_LOCATION_URI);
			} else {
				return false;
			}
		} else {
			service.populateContributionManager(cm, APP_MENU_LOCATION_URI);
		}
 		cm.update(false);
 		
         menu.setLocation(getCurrentEvent().x, getCurrentEvent().y);
         menu.setVisible(true);
         try {
 	        while (!menu.isDisposed() && menu.isVisible()) {
 	          if (!display.readAndDispatch())
 	            display.sleep();
 	        }
         } catch (RuntimeException e) {
         	// eat any exceptions to make sure that SWT event loop won't break the drop handler.  
         	Activator.log(e);
         } finally {
 	        menu.dispose();
 		}
         return true;
 	}
 	
 	@Override
 	public boolean validateDrop(Object target, int operation,
 			TransferData type) {
 		boolean isSupported = ResourceTransfer.getInstance().isSupportedType(type);
 		boolean isZendTarget = target instanceof IZendTarget;
 		overrideOperation(DND.DROP_COPY);
 		return isSupported && isZendTarget;
 	}
	
	private boolean isLibrary(IProject project) {
		IDescriptorContainer container = DescriptorContainerManager
				.getService().openDescriptorContainer(project);
		IDeploymentDescriptor desc = container.getDescriptorModel();
		return desc.getType() == ProjectType.LIBRARY;
	}
 
 }
