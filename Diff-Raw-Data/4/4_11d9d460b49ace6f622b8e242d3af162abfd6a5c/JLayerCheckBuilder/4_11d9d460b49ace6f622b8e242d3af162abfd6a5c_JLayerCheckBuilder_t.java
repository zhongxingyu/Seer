 package jlayercheckbuilder.builder;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.sf.jlayercheck.util.DependencyVisitor;
 import net.sf.jlayercheck.util.XMLConfiguration;
 import net.sf.jlayercheck.util.XMLConfigurationParser;
 import net.sf.jlayercheck.util.exceptions.ConfigurationException;
 import net.sf.jlayercheck.util.exceptions.OverlappingModulesDefinitionException;
 import net.sf.jlayercheck.util.model.ClassDependency;
 import net.sf.jlayercheck.util.model.ClassSource;
 import net.sf.jlayercheck.util.modeltree.ClassNode;
 import net.sf.jlayercheck.util.modeltree.ModelTree;
 import net.sf.jlayercheck.util.modeltree.ModuleNode;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IPackageDeclaration;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.xml.sax.SAXException;
 
 public class JLayerCheckBuilder extends IncrementalProjectBuilder {
 
 	/**
 	 * Contains the dependency informations of all classes.
 	 */
 	protected ModelTree mt;
 	
 	class JLayerCheckDeltaVisitor implements IResourceDeltaVisitor {
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 		 */
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			IResource resource = delta.getResource();
 			switch (delta.getKind()) {
 			case IResourceDelta.ADDED:
 				// handle added resource
 				check(resource);
 				break;
 			case IResourceDelta.REMOVED:
 				// handle removed resource
 				break;
 			case IResourceDelta.CHANGED:
 				// handle changed resource
 				check(resource);
 				break;
 			}
 			//return true to continue visiting children.
 			return true;
 		}
 	}
 
 	class JLayerCheckResourceVisitor implements IResourceVisitor {
 		public boolean visit(IResource resource) {
 			check(resource);
 			//return true to continue visiting children.
 			return true;
 		}
 	}
 
 	public static final String BUILDER_ID = "JLayerCheckBuilder.jlayercheckBuilderID";
 
 	private static final String MARKER_TYPE = "JLayerCheckBuilder.dependency";
 
 	private void addMarker(IFile file, String message, int lineNumber,
 			int severity) {
 		try {
 			IMarker marker = file.createMarker(MARKER_TYPE);
 			marker.setAttribute(IMarker.MESSAGE, message);
 			marker.setAttribute(IMarker.SEVERITY, severity);
 			if (lineNumber == -1) {
 				lineNumber = 1;
 			}
 			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
 	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
 	throws CoreException {
 		if (kind == FULL_BUILD) {
 			fullBuild(monitor);
 		} else {
 			IResourceDelta delta = getDelta(getProject());
 			if (delta == null) {
 				fullBuild(monitor);
 			} else {
 				incrementalBuild(delta, monitor);
 			}
 		}
 		return null;
 	}
 
 	protected void check(IResource resource) {
 		if (resource instanceof IFile && resource.getName().endsWith("jlayercheck.xml")) {
 			IFile file = (IFile) resource;
 			
 			refreshArchitecture(file);
 		}
 		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
 			IFile file = (IFile) resource;
 			deleteMarkers(file);
 
			if (mt == null) {
				refreshArchitecture(file);
			}
			
 			ICompilationUnit compilationUnit = (ICompilationUnit) JavaCore.createCompilationUnitFrom(file);
 			try {
 				IPackageDeclaration pd[] = compilationUnit.getPackageDeclarations();
 				String p = "";
 				if (pd.length>0) {
 					p = pd[0].getElementName();
 				}
 
 				String classname = p.replace(".", "/").concat("/").concat(file.getName()).replaceAll(".java$", "");
 
 				File classOutputPath = JavaCore.create(file.getProject()).getOutputLocation().toFile();
 				File basepath = file.getProject().getParent().getLocation().toFile();
 				File classfilename = new File(new File(basepath, classOutputPath.toString()), classname+".class");
 				
 				// refresh architectural model
 				File fi = file.getProject().getFile("/jlayercheck.xml").getRawLocation().toFile();
 				XMLConfiguration xcp;
 				try {
 					xcp = new XMLConfigurationParser().parse(fi);
 					xcp.updateModelTree(mt, classfilename);
 				} catch (ConfigurationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (SAXException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ParserConfigurationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (OverlappingModulesDefinitionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				ClassNode cn = mt.getClassNode(classname);
 
 				if (cn != null) {
 					String modulename = ((ModuleNode) cn.getParent().getParent()).getModuleName();
 					for(ClassDependency cd : cn.getClassDependencies()) {
 						if (cd.isUnallowedDependency()) {
 
 							ClassNode cndest = mt.getClassNode(cd.getDependency());
 							String moduledest = "";
 							if (cndest != null) {
 								moduledest = ((ModuleNode) cndest.getParent().getParent()).getModuleName();
 							}
 
 							// add markers
 							for(Integer linenumber : cd.getLineNumbers()) {
 
 								String msg = "Module " + modulename + " should not access " + cd.getDependency().replace("/", ".") + " (" + moduledest +")";
 								addMarker(file, msg, linenumber.intValue(), IMarker.SEVERITY_WARNING);
 							}						
 						}
 					}
 				}
 
 			} catch (JavaModelException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Reloads all dependencies.
 	 * 
 	 * @param file
 	 */
 	protected void refreshArchitecture(IFile file) {
 		try {
 			File fi = file.getProject().getFile("/jlayercheck.xml").getRawLocation().toFile();
 			XMLConfiguration xcp = new XMLConfigurationParser().parse(fi);
 			DependencyVisitor dv = new DependencyVisitor();
 			for(ClassSource source : xcp.getClassSources()) {
 				source.call(dv);
 			}
 			mt = xcp.getModelTree(dv);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (OverlappingModulesDefinitionException e) {
 			e.printStackTrace();
 		} catch (net.sf.jlayercheck.util.exceptions.ConfigurationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void deleteMarkers(IFile file) {
 		try {
 			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
 		} catch (CoreException ce) {
 		}
 	}
 
 	protected void fullBuild(final IProgressMonitor monitor)
 	throws CoreException {
 		try {
 			getProject().accept(new JLayerCheckResourceVisitor());
 		} catch (CoreException e) {
 		}
 	}
 
 	protected void incrementalBuild(IResourceDelta delta,
 			IProgressMonitor monitor) throws CoreException {
 		// the visitor does the work.
 		delta.accept(new JLayerCheckDeltaVisitor());
 	}
 }
