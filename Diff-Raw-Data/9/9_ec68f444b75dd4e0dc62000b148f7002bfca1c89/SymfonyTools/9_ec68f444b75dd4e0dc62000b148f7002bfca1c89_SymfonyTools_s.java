 package naholyr.eclipse.sf;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 
 public class SymfonyTools {
 
 	private static boolean hasSymfonyScript(IPath path) {
 		IPath sfPath = path.append("symfony"); //$NON-NLS-1$
 		File f = sfPath.toFile();
 		return f.exists() && f.isFile();
 	}
 
 	public static IPath getProjectPath(IResource resource) {
 		return getProjectPath(resource.getLocation());
 	}
 
 	public static IPath getProjectPath(IPath path) {
 		if (hasSymfonyScript(path)) {
 			return path;
 		}
		if (path.isEmpty()) {
 			return null;
 		}
 		return getProjectPath(path.removeLastSegments(1));
 	}
 
 	public static boolean isInProject(IResource resource) {
 		return isInProject(resource.getLocation());
 	}
 
 	public static boolean isInProject(IPath path) {
 		return getProjectPath(path) != null;
 	}
 
 	public static boolean isTemplate(IResource resource) {
 		return isTemplate(resource.getLocation());
 	}
 
 	public static boolean isTemplate(IPath path) {
 		if (!isInProject(path)) {
 			return false;
 		}
 		return path.removeLastSegments(1).lastSegment().equals("templates"); //$NON-NLS-1$
 	}
 
 	public static boolean isTemplateOverride(IResource resource) {
 		return isTemplateOverride(resource.getLocation());
 	}
 
 	public static boolean isTemplateOverride(IPath path) {
 		IPath[] templates = getTemplateOverrides(path);
 		return templates != null && templates.length != 0;
 	}
 
 	public static IPath[] getTemplateOverrides(IResource resource) {
 		return getTemplateOverrides(resource.getLocation());
 	}
 
 	public static IPath[] getTemplateOverrides(IPath path) {
 		if (!isTemplate(path)) {
 			return null;
 		}
 		String fileName = path.lastSegment();
 		IPath projectPath = getProjectPath(path);
 		IPath[] templatesFolders = getAllTemplatesFolders(projectPath);
 		ArrayList<IPath> result = new ArrayList<IPath>();
 		for (IPath templatesFolder : templatesFolders) {
 			IPath templatePath = templatesFolder.append(fileName);
 			if (!templatePath.equals(path)) {
 				File templateFile = templatePath.toFile();
 				if (templateFile.exists() && templateFile.isFile()) {
 					result.add(templatePath);
 				}
 			}
 		}
 		return result.toArray(new IPath[0]);
 	}
 
 	private static IPath[] getAllTemplatesFolders(IPath projectPath) {
 		IPath[] apps = getAllTemplatesFolders(projectPath, "apps"); //$NON-NLS-1$
 		IPath[] plugins = getAllTemplatesFolders(projectPath, "plugins"); //$NON-NLS-1$
 		IPath[] all = new IPath[apps.length + plugins.length];
 		for (int i = 0; i < apps.length; i++) {
 			all[i] = apps[i];
 		}
 		for (int i = 0; i < plugins.length; i++) {
 			all[apps.length + i] = plugins[i];
 		}
 		return all;
 	}
 
 	/**
 	 * 
 	 * @param projectPath
 	 * @param type
 	 *            "apps" or "plugins"
 	 * @return
 	 */
 	private static IPath[] getAllTemplatesFolders(IPath projectPath, String type) {
 		ArrayList<IPath> templatesFolders = new ArrayList<IPath>();
 		// Applications
 		IPath[] apps = getSubfolders(projectPath, type);
 		if (apps != null) {
 			for (IPath app : apps) {
 				// Application : "templates" folder
 				IPath appTemplatesFolder = getTemplatesFolderIfExists(app);
 				if (appTemplatesFolder != null) {
 					templatesFolders.add(appTemplatesFolder);
 				}
 				// Application : modules
 				IPath[] modules = getSubfolders(app, "modules"); //$NON-NLS-1$
 				if (modules != null) {
 					for (IPath module : modules) {
 						// Module : "templates" folder
 						IPath moduleTemplatesFolder = getTemplatesFolderIfExists(module);
 						if (moduleTemplatesFolder != null) {
 							templatesFolders.add(moduleTemplatesFolder);
 						}
 					}
 				}
 			}
 		}
 		return templatesFolders.toArray(new IPath[0]);
 	}
 
 	private static IPath getTemplatesFolderIfExists(IPath path) {
 		IPath templatesPath = path.append("templates"); //$NON-NLS-1$
 		File templatesFolder = templatesPath.toFile();
 		if (templatesFolder.exists() && templatesFolder.isDirectory()) {
 			return templatesPath;
 		}
 		return null;
 	}
 
 	private static IPath[] getSubfolders(IPath root, String folderName) {
 		if (root == null) {
 			return null;
 		}
 		root = root.append(folderName);
 		File folder = root.toFile();
 		if (folder == null || !folder.isDirectory()) {
 			return null;
 		}
 		FileFilter isDirectory = new FileFilter() {
 			public boolean accept(File pathname) {
 				return pathname.exists() && pathname.isDirectory();
 			}
 		};
 		File[] subFolders = folder.listFiles(isDirectory);
 		IPath[] result = new IPath[subFolders.length];
 		for (int i = 0; i < subFolders.length; i++) {
 			result[i] = root.append(subFolders[i].getName());
 		}
 		return result;
 	}
 }
