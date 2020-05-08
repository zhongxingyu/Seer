 package org.pu.open.actions;
 
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.action.IAction;
 import org.pu.open.Activator;
 import org.pu.open.preferences.PreferenceConstants;
 
 /**
  * @author Shang Pu
  * @version Date: Sep 8, 2012 9:52:41 PM
  */
 public class OpenClassFolderAction extends BaseOpenAction {
 	
 	@Override
 	public void runAction(IAction action, String filePath) {
 		Activator plugin = Activator.getDefault();
 		String command = plugin.getPreferenceStore().getString(PreferenceConstants.P_OPEN_FOLDER);
 		String classFolderPath = getClassFolderPath(filePath);
 		plugin.execCommand(command, classFolderPath);
 	}
 
 	/**
 	 * @return class folder path as per filePath
 	 */
 	protected String getClassFolderPath(String filePath) {
 		IJavaProject javaProject = getIJavaProject(selectedObject);
 		Map<String, List<String>> map = getMapping(javaProject);
 		if (map == null) return filePath;
 		
 		try {
 			String defaultClassFolder = javaProject.getOutputLocation().toOSString();
 			Iterator<String> it = map.keySet().iterator();
 			while (it.hasNext()) {
 				String output = it.next();
 				List<String> srcs = map.get(output);
 				for (String src : srcs) {
 					if (filePath.contains(src)) {
 						String classpath = filePath.replace(src, output);
 						if (new File(classpath).exists()) {
 							return classpath;
 						}
 					}
 				}
				// return the default output location if didn't find in the map
				String projectPath = new File(javaProject.getProject().getLocation().toOSString()).getParent();
				return (projectPath + defaultClassFolder);
 			}
 		} catch (JavaModelException e) {
 			Activator.log(Status.INFO, "Unable to get class folder ", e);
 		}
 		return filePath;
 	}
 
 	/**
 	 * get the src and class output location mapping
 	 * @return map &ltString, List&ltString>> key is the output location, value is the src list
 	 */
 	private Map<String, List<String>> getMapping(IJavaProject project) {
 		if (project == null) return null;
 		try {
 			String defaultOutput = project.getOutputLocation().toOSString();
 			System.out.println("defaultOutput = " + defaultOutput);
 			Map<String, List<String>> map = new HashMap<String, List<String>>();
 			IClasspathEntry[] entrys = project.getRawClasspath();
 			
 			for (IClasspathEntry entry : entrys) {
 				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 					String src = entry.getPath().toOSString();
 					IPath path = entry.getOutputLocation();
 					String output = (path == null ? defaultOutput : path.toOSString());
 
 					System.out.println("src = " +src +"; output = " + output);
 					if (map.get(output) != null){
 						map.get(output).add(src);
 					} else {
 						List<String> list = new ArrayList<String>();
 						list.add(src);
 						map.put(output, list);
 					}
 				}
 			}
 			return map;
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private IJavaProject getIJavaProject(Object object) {
 		if (null != selectedProject) return selectedProject;
 		if (null == object) return null;
 		IProject project = null;
 		if (object instanceof IResource) {
 			project = ((IResource) object).getProject();
 		}
 		
 		return JavaCore.create(project);
 	}
 }
