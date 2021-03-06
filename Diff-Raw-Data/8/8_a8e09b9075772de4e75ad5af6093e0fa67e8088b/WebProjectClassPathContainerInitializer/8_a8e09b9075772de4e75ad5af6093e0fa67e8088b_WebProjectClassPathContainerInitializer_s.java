 /**
  * 
  */
 package org.eclipse.wst.jsdt.web.core.internal.project;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.wst.jsdt.core.ClasspathContainerInitializer;
 import org.eclipse.wst.jsdt.core.IJavaProject;
 import org.eclipse.wst.jsdt.web.core.internal.java.JSP2ServletNameUtil;
 
 /**
  * @author childsb
  *
  */
 public class WebProjectClassPathContainerInitializer extends ClasspathContainerInitializer {

	private static final String MANGLED_BUTT = ".htm";
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.jsdt.core.ClasspathContainerInitializer#initialize(org.eclipse.core.runtime.IPath, org.eclipse.wst.jsdt.core.IJavaProject)
 	 */
 	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
 		/* need this to activate the jsdt.web plugin */
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.jsdt.core.ClasspathContainerInitializer#canUpdateClasspathContainer(org.eclipse.core.runtime.IPath, org.eclipse.wst.jsdt.core.IJavaProject)
 	 */
 	public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
 		/* dont remove from this project */
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.jsdt.core.ClasspathContainerInitializer#getDescription(org.eclipse.core.runtime.IPath, org.eclipse.wst.jsdt.core.IJavaProject)
 	 */
 	public String getDescription(IPath containerPath, IJavaProject project) {
 		if(containerPath.equals(JsWebNature.VIRTUAL_CONTAINER_PATH)) {
 			return new String("Web Project support for JSDT");
 		}
 		
 		String containerPathString = containerPath.toString();
 			String unmangled =  getUnmangedHtmlPath(containerPathString);
 			if(unmangled!=null) {
 				IPath projectPath = project.getPath();
 				
 				/* Replace the project path with the project name */
 				if(unmangled.indexOf(projectPath.toString()) >=0) {
 					unmangled =  project.getDisplayName() + ":"  + unmangled.substring(projectPath.toString().length());
 				}
 				return unmangled;
 			}
 		return containerPathString;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.jsdt.core.ClasspathContainerInitializer#getHostPath(org.eclipse.core.runtime.IPath)
 	 */
 	public URI getHostPath(IPath path, IJavaProject project) {
 		// TODO Auto-generated method stub
 		String htmlPath = getUnmangedHtmlPath(path.toString());
 		if(htmlPath!=null) {
 			try {
 				return new URI(htmlPath);
 			} catch (URISyntaxException ex) {
 			
 				ex.printStackTrace();
 			}	
 		}
 		return null;
 	}
 	
 	private static String getUnmangedHtmlPath(String containerPathString) {
 		if(containerPathString==null) return null;
 		
		if(containerPathString.toLowerCase().indexOf(MANGLED_BUTT)==-1) {
 			return  JSP2ServletNameUtil.unmangle(containerPathString);
 		}
 		return null;
 	}
 	
 }
