 package org.dawnsci.spectrum.ui.actions;
 
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.navigator.CommonActionProvider;
 import org.eclipse.ui.navigator.ICommonActionConstants;
 import org.eclipse.ui.navigator.ICommonActionExtensionSite;
 import org.eclipse.ui.navigator.ICommonViewerSite;
 import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
 
 public class SpectrumActionProvider extends CommonActionProvider {
 	
 	OpenSpectrumAction openAction;
 	
 	public SpectrumActionProvider() {
 		
 	}
 	
 	public void init(ICommonActionExtensionSite aSite) {
 		ICommonViewerSite viewSite = aSite.getViewSite();
 		
 		if (viewSite instanceof ICommonViewerWorkbenchSite) {
 			ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;
 			openAction = new OpenSpectrumAction(workbenchSite.getPage(),workbenchSite.getSelectionProvider());
 		}
 	}
 	
 	public void fillActionBars(IActionBars actionBars) {
 		
 		if (openAction.isEnabled()) {
 			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
 		}
 		
 	}
 	
 	public void fillContextMenu(IMenuManager menu) {
 		if (openAction.isEnabled()) {
			//menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openAction);
 		}
 	}
 
 }
