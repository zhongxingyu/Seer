 package org.jboss.tools.livereload.internal.util;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.server.core.IServer;
 
public class WTPUtils {
 
 	public static IFolder getWebappFolder(IProject project) {
 		IVirtualComponent component = ComponentCore.createComponent(project);
 		if (component == null) {
 			return null;
 		}
 		IVirtualFolder contentFolder = component.getRootFolder();
 		return (IFolder) contentFolder.getUnderlyingFolder();
 	}
 
 	/**
 	 * Returns the HTTP port for the given {@link IServer}
 	 * @param server
 	 * @return
 	 */
 	public static int getPort(IServer server) {
 		return 8080;
 	}
 
 }
