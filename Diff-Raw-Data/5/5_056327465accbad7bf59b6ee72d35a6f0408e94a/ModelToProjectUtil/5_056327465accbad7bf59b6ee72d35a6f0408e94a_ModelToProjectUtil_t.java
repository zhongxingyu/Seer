 package org.ow2.mindEd.ide.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.gmf.runtime.common.ui.util.FileUtil;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.ow2.mindEd.ide.model.MindAdl;
 import org.ow2.mindEd.ide.model.MindFile;
 import org.ow2.mindEd.ide.model.MindItf;
 import org.ow2.mindEd.ide.model.MindPackage;
 import org.ow2.mindEd.ide.model.MindProject;
 import org.ow2.mindEd.ide.model.MindRootSrc;
 
 
 
 public class ModelToProjectUtil {
 	
 	/**
 	 * The instance singleton
 	 */
 	public static final ModelToProjectUtil INSTANCE = new ModelToProjectUtil();
 	
 	/**
 	 * The editor input associated with the editor opened.
 	 * Needed to get the current project / package / definition
 	 */
 	private IEditorInput editorInput;
 	
 	private ModelToProjectUtil () {
 		refreshEditorInput();
 	}
 	
 	/**
 	 * Seeks a component with the given name.
 	 * @param componentName the name of the component to resolve
 	 * @param imports the list of imports name in the definition
 	 * @return the URI of the resolved component or null
 	 * @see {@link MindProject#resolveAdl(String, String, EList)}
 	 */
 	public URI resolveAdl(String componentName, ArrayList<String> imports) {
 		EList<String> importsEList = new BasicEList<String>();
 		importsEList.addAll(imports);
 		return resolveAdl(componentName, importsEList);
 	}
 	
 	/**
 	 * Seeks a component with the given name.
 	 * @param componentName the name of the component to resolve
 	 * @param imports the list of imports name in the definition
 	 * @return the URI of the resolved component or null
 	 * @see {@link MindProject#resolveAdl(String, String, EList)}
 	 */
 	public URI resolveAdl(String componentName, EList<String> imports) {
 		try {
 			// This is the current project
 			MindProject project = getMindProject();
 
 			// This is the current package
 			String defaultPackage = getPackage(project).getName();
 			// Resolve and return the URI
 			MindAdl adl = project.resolveAdl(componentName, defaultPackage, imports);
			if (adl == null) return null;
			
 			return URI.createURI(adl.getFullpath());
 		} catch (NullPointerException e) {
 			return null;
 		}
 	}
 	
 	/**
 	 * 
 	 * @return a list of string containing all the definitions in the project, or an empty list
 	 */
 	public List<String> getDefinitionsInProject() {
 		try {
 			EList<MindFile> files = getMindProject().getAllFiles();
 			List<String> definitions = new ArrayList<String>();
 			for (MindFile file : files) {
 				if (file instanceof MindAdl) {
 					definitions.add(file.getQualifiedName());
 				}
 			}
 			return definitions;
 		}catch (NullPointerException e) {
 			// Project is null
 			return new ArrayList<String>();
 		}
 	}
 	
 	/**
 	 * 
 	 * @return a list of string containing all the .itf in the project, or an empty list
 	 */
 	public List<String> getInterfacesInProject() {
 		try {
 			EList<MindFile> files = getMindProject().getAllFiles();
 			List<String> definitions = new ArrayList<String>();
 			for (MindFile file : files) {
 				if (file instanceof MindItf) {
 					definitions.add(file.getQualifiedName());
 				}
 			}
 			return definitions;
 		}catch (NullPointerException e) {
 			// Project is null
 			return new ArrayList<String>();
 		}
 	}
 	
 	
 	public void setEditorInput(IEditorInput input) {
 		editorInput = input;
 	}
 	
 	private void refreshEditorInput () {
 		try {
 			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window == null)
				return;
 			editorInput = window.getActivePage().getActiveEditor().getEditorInput();
 		} catch (NullPointerException e) {
 			// The editor is probably being initialized
 			return;
 		}
 		
 	}
 	
 	/**
 	 * @return the current MindProject or null if project is not of Mind nature
 	 */
 	public MindProject getMindProject() {
 		return getMindProject(getIProject());
 	}
 	
 	/**
 	 * @param project the IProject
 	 * @return the MindProject associated with given IProject or null if the project is not of Mind nature
 	 */
 	public MindProject getMindProject(IProject project) {
 		return MindIdeCore.get(project);
 	}
 	
 	public String getCurrentDefinition() {
 		refreshEditorInput();
 		
 		if(editorInput instanceof FileEditorInput){
 			try{
 				String relativePath = FileUtil.getRelativePath(((FileEditorInput)editorInput).getPath().toString(), Platform.getLocation().toString());
 				// Convert to mind format
 				String mindPath = convertToMindPath(relativePath);
 				String pack = getPackage(getMindProject()).getName();
 				int start = mindPath.lastIndexOf(pack);
 				int end = mindPath.lastIndexOf(".");
 				String finalPath = mindPath.substring(start, end);
 				return finalPath;
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 			
 		return new String(" ");
 	}
 	
 	/**
 	 * Used to convert path  to a mind path<p>
 	 * Converts "\\" or "/" separators to "."
 	 * @param oldPath
 	 * @return the converted path
 	 */
 	static String convertToMindPath(String oldPath){	
 		String tempPath = oldPath.replace('\\', '.');
 		String convertedPath = tempPath.replace('/', '.');
 		return convertedPath;
 	}
 	
 	/**
 	 * 
 	 * @param uri the URI of the wanted file
 	 * @return the IFile or null
 	 */
 	public IFile getIFile(URI uri) {
 		if (uri != null) {
 			try {
 				IWorkspace workspace = org.eclipse.core.resources.ResourcesPlugin.getWorkspace();
 				// Get the project associated with the first segment
 				IProject project = workspace.getRoot().getProject(uri.segment(0));
 				String path = FileUtil.getRelativePath(uri.path(),project.getFullPath().toString());
 				return project.getFile(path);
 			} catch (NullPointerException e) {
 				// File could not be found
 				return null;
 			}
 			
 		}
 		return null;
 	}
 	
 	/**
 	 * @return the current IProject or null
 	 */
 	public IProject getIProject() {
 		IProject project = null;
 		refreshEditorInput();
 
 		if(editorInput instanceof FileEditorInput){
 			// URI of the file associated with the editor
 			@SuppressWarnings("unused")
 			URI resourceURI = URI.createPlatformResourceURI(((FileEditorInput)editorInput).getURI().toString(),true);
 			try{
 				String relativePath = FileUtil.getRelativePath(((FileEditorInput)editorInput).getPath().toString(), Platform.getLocation().toString());
 				//Replace \\ with /
 				String convertedRelativePath = convertToGenericPath(relativePath);
 				URI relativeURI = URI.createURI(convertedRelativePath);
 				
 				if (relativeURI.segmentCount() > 1) {
 					IWorkspace workspace = org.eclipse.core.resources.ResourcesPlugin.getWorkspace();
 					project = workspace.getRoot().getProject(relativeURI.segment(0));
 				}
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		return project;
 	}
 	
 	/**
 	 * @param resourceURI : A file URI
 	 * @return Fully qualified name from a given URI
 	 */
 	public String getFQNFromURI(URI resourceURI)
 	{
 		if(resourceURI==null)return null;
 		String pack = getPackageFromURI(resourceURI).getName();
 		if(pack==null)pack="";
 		String component = resourceURI.lastSegment().substring(0, resourceURI.lastSegment().lastIndexOf("."));
 		return pack+"."+component;
 	}
 	
 	/**
 	 * @param resourceURI : A file URI
 	 * @return package name from a resource URI
 	 */
 	public MindPackage getPackageFromURI(URI resourceURI) {
 		refreshEditorInput();
 		if(editorInput instanceof FileEditorInput){
 			try{
 				String relativePath = resourceURI.devicePath();
 				//Replace \ with #
 				String convertedRelativePath = '/' + convertToGenericPath(relativePath);
 				URI relativeURI = URI.createURI(convertedRelativePath);
 				
 				if (relativeURI.segmentCount() > 1) {
 					EList<MindRootSrc> roots = getMindProject().getRootsrcs();
 					for (MindRootSrc rootSrc : roots) {
 						String rootPath = rootSrc.getFullpath();
 						if (convertedRelativePath.contains(rootPath)) {
 							EList<MindPackage> packages = rootSrc.getPackages();
 							for (MindPackage mindPackage : packages) {
 								String packagePath = mindPackage.getFullpath();
 								if (convertedRelativePath.contains(packagePath)) {
 									return mindPackage;
 								}
 							}
 						}
 						
 					}
 				}
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		return null;	
 	}
 	
 	/**
 	 * @return the IProject associated with the editorInput
 	 */
 	public MindPackage getPackage(MindProject project) {
 		refreshEditorInput();
 		
 		if(editorInput instanceof FileEditorInput){
 			@SuppressWarnings("unused")
 			URI resourceURI = URI.createPlatformResourceURI(((FileEditorInput)editorInput).getURI().toString(),true);
 			try{
 				String relativePath = FileUtil.getRelativePath(((FileEditorInput)editorInput).getPath().toString(), Platform.getLocation().toString());
 				//Replace \ with #
 				String convertedRelativePath = '/' + convertToGenericPath(relativePath);
 				URI relativeURI = URI.createURI(convertedRelativePath);
 				
 				if (relativeURI.segmentCount() > 1) {
 					EList<MindRootSrc> roots = project.getRootsrcs();
 					for (MindRootSrc rootSrc : roots) {
 						String rootPath = rootSrc.getFullpath();
 						if (convertedRelativePath.contains(rootPath)) {
 							EList<MindPackage> packages = rootSrc.getPackages();
 							for (MindPackage mindPackage : packages) {
 								String packagePath = mindPackage.getFullpath();
 								if (convertedRelativePath.contains(packagePath)) {
 									return mindPackage;
 								}
 							}
 						}
 						
 					}
 				}
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	
 		
 	/**
 	 * Used to convert a windows path ( \\ ) to a generic path ( / )
 	 * @param oldPath
 	 * @return the converted path
 	 */
 	static String convertToGenericPath(String oldPath){	
 		String convertedPath = oldPath.replace('\\', '/');
 		return convertedPath;
 	}
 	
 	
 }
