 package org.jboss.tools.bpel.runtimes.ui.view.server;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
 import org.jboss.tools.bpel.runtimes.IBPELModuleFacetConstants;
 import org.jboss.tools.bpel.runtimes.module.JBTBPELPublisher;
 
 public class BPELModuleContentProvider implements ITreeContentProvider {
 	public BPELModuleContentProvider() {
 	}
 	public Object[] getChildren(Object parentElement) {
 		if( parentElement instanceof ModuleServer ) {
 			IServer s = ((ModuleServer)parentElement).server;
 			IModule[] module = ((ModuleServer)parentElement).module;
 			IModule mod = module.length > 0 ? module[module.length-1] : null;
 			String typeId = mod.getModuleType().getId();
			// https://jira.jboss.org/browse/JBIDE-7486
			// if project was closed or deleted, mod.getProject() is null - ignore
			if( mod != null && mod.getProject() != null && typeId.equals(IBPELModuleFacetConstants.BPEL_MODULE_TYPE)) {
 				// we have a bpel module deployed to a server. List the children
 				String[] versions = JBTBPELPublisher.getDeployedPathsFromDescriptor(s, mod.getProject());
 				return wrap((ModuleServer)parentElement, versions);
 			}
 		}
 		return new Object[]{};
 	}
 	
 	protected BPELVersionDeployment[] wrap(ModuleServer ms, String[] vals) {
 		BPELVersionDeployment[] versions = new BPELVersionDeployment[vals.length];
 		for( int i = 0; i < vals.length; i++ ) {
 			versions[i] = new BPELVersionDeployment(ms, vals[i]);
 		}
 		return versions;
 	}
 
 	public static class BPELVersionDeployment {
 		private String path;
 		private ModuleServer ms;
 		public BPELVersionDeployment(ModuleServer ms, String path) {
 			this.path = path;
 			this.ms = ms;
 		}
 		public String getPath() { return path; }
 		public ModuleServer getModuleServer() { return ms; }
 		public IProject getProject() {
 			if( ms.module != null && ms.module.length > 0 )
 				return ms.module[ms.module.length-1].getProject();
 			return null;
 		}
 	}
 	
 	public Object getParent(Object element) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public boolean hasChildren(Object element) {
 		return getChildren(element).length > 0;
 	}
 
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	public void dispose() {
 	}
 
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
