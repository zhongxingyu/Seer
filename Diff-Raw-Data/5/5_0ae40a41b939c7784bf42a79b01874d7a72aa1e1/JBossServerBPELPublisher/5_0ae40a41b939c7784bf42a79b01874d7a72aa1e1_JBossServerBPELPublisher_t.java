 package org.jboss.tools.bpel.as.integration;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.jboss.ide.eclipse.as.core.publishers.JstPublisher;
 import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.eclipse.bpel.runtimes.IBPELModuleFacetConstants;
 
 public class JBossServerBPELPublisher extends JstPublisher implements
 		IJBossServerPublisher {
 
 	public JBossServerBPELPublisher() {
 	}
 
 	public boolean accepts(String method, IServer server, IModule[] module) {
 		return "local".equals(method) && accepts(server, module);
 	}
 	
 	public boolean accepts(IServer server, IModule[] module) {
 		IProject project = module[0].getProject();
 		if(project == null) return false;
 		
 		try {
 			IFacetedProject fp = ProjectFacetsManager.create(project);
			IProjectFacet pf = ProjectFacetsManager.getProjectFacet(IBPELModuleFacetConstants.BPEL20_PROJECT_FACET);
 			return fp.hasProjectFacet(pf);
 		} catch (Exception e) {
 			return false;
 		}
 		
 	}
 }
