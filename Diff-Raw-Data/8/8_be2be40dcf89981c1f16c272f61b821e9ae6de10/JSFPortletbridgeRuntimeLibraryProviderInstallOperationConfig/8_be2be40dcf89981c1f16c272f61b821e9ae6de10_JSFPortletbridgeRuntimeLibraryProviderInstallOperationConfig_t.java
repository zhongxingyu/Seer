 package org.jboss.tools.portlet.core.libprov;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
 import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
 import org.eclipse.wst.common.project.facet.core.IFacetedProjectBase;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.jboss.tools.portlet.core.IPortletConstants;
 import org.jboss.tools.portlet.core.Messages;
 import org.jboss.tools.portlet.core.PortletCoreActivator;
 import org.osgi.service.prefs.BackingStoreException;
 import org.osgi.service.prefs.Preferences;
 
 public class JSFPortletbridgeRuntimeLibraryProviderInstallOperationConfig extends
 		AbstractLibraryProviderInstallOperationConfig {
 
 	private String portletbridgeHome;
 
 	@Override
 	public synchronized IStatus validate() {
 		IStatus status = super.validate();
 		if (!status.isOK()) {
 			return status;
 		}
 		if (isEPP()) {
 			return status;
 		}
 		if (portletbridgeHome == null) {
 			return getInvalidPortletbridgeRuntime();
 		}
 		portletbridgeHome = portletbridgeHome.trim();
 		if (portletbridgeHome.length() <= 0) {
 			status = new Status( IStatus.ERROR, PortletCoreActivator.PLUGIN_ID, Messages.JSFPortletFacetInstallPage_Portletbridge_Runtime_directory_is_required );
 			return status;
 		}
 		File folder = new File(portletbridgeHome);
 		if (!folder.exists() || !folder.isDirectory()) {
 			return getInvalidPortletbridgeRuntime();
 		}
 		String[] fileList = folder.list(new FilenameFilter() {
 
 			public boolean accept(File dir, String name) {
				if (name.startsWith("portletbridge") && name.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
					return true;
				}
				if (name.equals("examples")) { //$NON-NLS-1$
 					return true;
 				}
 				return false;
 			}
 
 		});
		if (fileList.length < 3) {
 			return getInvalidPortletbridgeRuntime();
 		}
 		return Status.OK_STATUS;
 	}
 
 	private IStatus getInvalidPortletbridgeRuntime() {
 		IStatus status = new Status( IStatus.ERROR, PortletCoreActivator.PLUGIN_ID, Messages.JSFPortletFacetInstallPage_Invalid_Portletbridge_Runtime_directory );
 		return status;
 	}
 
 	public String getPortletbridgeHome() {
 		if (isEPP()) {
 			return PortletCoreActivator.getEPPDir(getFacetedProject(), PortletCoreActivator.PORTLETBRIDGE).getAbsolutePath();
 		}
 		return portletbridgeHome;
 	}
 
 	public void setPortletbridgeHome(String portletbridgeHome) {
 		String oldValue = this.portletbridgeHome;
 		this.portletbridgeHome = portletbridgeHome;
 		notifyListeners(IPortletConstants.PORTLETBRIDGE_HOME, oldValue, portletbridgeHome);
 		updatePreferences();
 	}
 	
 	@Override
 	public void init(IFacetedProjectBase fpj, IProjectFacetVersion fv,
 			ILibraryProvider provider) {
 		super.init(fpj, fv, provider);
 		reset();
 	}
 
 	@Override
 	public void reset() {
 		super.reset();
 		IProjectFacet f = getProjectFacet();
         try {
 			Preferences prefs = FacetedProjectFramework.getPreferences( f );
 			prefs = prefs.node(IPortletConstants.PORTLET_BRIDGE_HOME);
 			if( prefs.nodeExists( IPortletConstants.PREFS_PORTLETBRIDGE_HOME ) ) {
 				portletbridgeHome = prefs.get(IPortletConstants.PREFS_PORTLETBRIDGE_HOME, null);
 			}
 		} catch (BackingStoreException e) {
 			PortletCoreActivator.log(e);
 		}
 	}
 
 	private void updatePreferences() {
 		IProjectFacet f = getProjectFacet();
         try {
 			Preferences prefs = FacetedProjectFramework.getPreferences( f );
 			prefs = prefs.node(IPortletConstants.PORTLET_BRIDGE_HOME);
 			prefs.put(IPortletConstants.PREFS_PORTLETBRIDGE_HOME, portletbridgeHome);
 		} catch (BackingStoreException e) {
 			PortletCoreActivator.log(e);
 		}
 	}
 	
 	
 }
