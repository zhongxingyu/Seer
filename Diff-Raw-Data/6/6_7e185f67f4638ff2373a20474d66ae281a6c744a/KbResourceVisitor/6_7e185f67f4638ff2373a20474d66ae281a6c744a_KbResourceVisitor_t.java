 package org.jboss.tools.jst.web.kb.internal;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.project.ProjectHome;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
import org.jboss.tools.common.web.WebUtils;
 import org.jboss.tools.jst.web.kb.WebKbPlugin;
 import org.jboss.tools.jst.web.kb.internal.scanner.IFileScanner;
 import org.jboss.tools.jst.web.kb.internal.scanner.JSF2ResourcesScanner;
 import org.jboss.tools.jst.web.kb.internal.scanner.LoadedDeclarations;
 import org.jboss.tools.jst.web.kb.internal.scanner.MyFacesScanner;
 import org.jboss.tools.jst.web.kb.internal.scanner.ScannerException;
 import org.jboss.tools.jst.web.kb.internal.scanner.XMLScanner;
 import org.jboss.tools.jst.web.model.helpers.InnerModelHelper;
 
 public class KbResourceVisitor implements IResourceVisitor {
 	static IFileScanner[] FILE_SCANNERS = {
 		new MyFacesScanner(),
 		new XMLScanner(),
 	};
 	JSF2ResourcesScanner jsf2scanner = new JSF2ResourcesScanner();
 	
 	KbProject p;
 
 	IPath[] outs = new IPath[0];
 	IPath[] srcs = new IPath[0];
 	IPath[] webinfs = new IPath[0];
 	IPath[] jsf2resources = new IPath[0];
 	Set<IPath> jsf2resourcesProcessed = new HashSet<IPath>();
 
 	public KbResourceVisitor(KbProject p) {
 		this.p = p;
 
 		if(p.getProject() != null && p.getProject().isOpen()) {
 			getJavaSourceRoots(p.getProject());
 
 			List<IPath> jsf2rs = new ArrayList<IPath>();
 			XModel model = InnerModelHelper.createXModel(p.getProject());
 			if(model != null) {
				webinfs = WebUtils.getWebInfPaths(p.getProject());
				IPath[] webContents = WebUtils.getWebContentPaths(p.getProject());
 				for (IPath webcontent: webContents) {
 					IPath jsf2r = webcontent.append("resources"); //$NON-NLS-1$
 					IResource rf = ResourcesPlugin.getWorkspace().getRoot().getFolder(jsf2r);
 					if(rf.exists() && !jsf2rs.contains(jsf2r)) {
 						jsf2rs.add(jsf2r);
 					}
 				}
 			}
 			for (IPath s: srcs) {
 				IPath jsf2r = s.append("META-INF/resources"); //$NON-NLS-1$
 				IResource rf = ResourcesPlugin.getWorkspace().getRoot().getFolder(jsf2r);
 				if(rf.exists() && !jsf2rs.contains(jsf2r)) {
 					jsf2rs.add(jsf2r);
 				}
 			}
 			jsf2resources = jsf2rs.toArray(new IPath[0]);
 		}
 	}
 
 	public IResourceVisitor getVisitor() {
 		return this;
 	}
 
 	public void init() {
 		jsf2resourcesProcessed.clear();
 	}
 
 	public boolean visit(IResource resource) {
 		if(resource instanceof IFile) {
 			IFile f = (IFile)resource;
 			for (int i = 0; i < outs.length; i++) {
 				if(outs[i].isPrefixOf(resource.getFullPath())) {
 					return false;
 				}
 			}
 			for (int i = 0; i < FILE_SCANNERS.length; i++) {
 				IFileScanner scanner = FILE_SCANNERS[i];
 				if(scanner.isRelevant(f)) {
 //					long t = System.currentTimeMillis();
 					if(!scanner.isLikelyComponentSource(f)) {
 						p.pathRemoved(f.getFullPath());
 						return false;
 					}
 					LoadedDeclarations c = null;
 					try {
 						c = scanner.parse(f, p);
 					} catch (ScannerException e) {
 						WebKbPlugin.getDefault().logError(e);
 					}
 					if(c != null) componentsLoaded(c, f);
 //					long dt = System.currentTimeMillis() - t;
 //					timeUsed += dt;
 //					System.out.println("Time=" + timeUsed);
 					break;
 				}
 			}
 			for (IPath jsf2resource: jsf2resources) {int i = 0;
 				if(jsf2resource.isPrefixOf(f.getFullPath()) && jsf2scanner.isLikelyComponentSource(f)) {
 					processJSF2Resources(jsf2resource);
 					break;
 				}
 			}
 		}
 		if(resource instanceof IFolder) {
 			IPath path = resource.getFullPath();
 			for (int i = 0; i < outs.length; i++) {
 				if(outs[i].isPrefixOf(path)) {
 					return false;
 				}
 			}
 			for (int i = 0; i < srcs.length; i++) {
 				if(srcs[i].isPrefixOf(path) || path.isPrefixOf(srcs[i])) {
 					return true;
 				}
 			}
 			for (IPath jsf2resource: jsf2resources) {
 				if (jsf2resource.isPrefixOf(path)) {
 					processJSF2Resources(jsf2resource);
 					return false;
 				}
 				if(path.isPrefixOf(jsf2resource)) {
 					return true;
 				}
 			}
 			for (IPath webinf: webinfs) {
 				if(webinf.isPrefixOf(path) || path.isPrefixOf(webinf)
 						|| webinf.removeLastSegments(1).isPrefixOf(path) //Webroot
 				) {
 					return true;
 				}
 			}
 			
 			if(resource == resource.getProject()) {
 				return true;
 			}
 			return false;
 		}
 		//return true to continue visiting children.
 		return true;
 	}
 
 	void processJSF2Resources(IPath jsf2resource) {
 		if (jsf2resourcesProcessed.contains(jsf2resource)) return;
 		jsf2resourcesProcessed.add(jsf2resource);
 		IResource jsf2resourcesFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(jsf2resource);
 		if(!jsf2resourcesFolder.exists()) {
 			return;
 		}
 		JSF2ResourcesScanner scanner = new JSF2ResourcesScanner();
 		Map<IPath,LoadedDeclarations> result = null;
 		try {
 			result = scanner.parse((IFolder) jsf2resourcesFolder, p);
 		} catch (ScannerException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 		if (result != null) {
 			for (IPath path: result.keySet()) {
 				LoadedDeclarations c = result.get(path);
 				p.registerComponents(c, path);
 			}
 			p.updateChildPaths(jsf2resourcesFolder.getFullPath(), result.keySet());
 		}
 	}
 	
 	void componentsLoaded(LoadedDeclarations c, IResource resource) {
 		if(c == null || c.getLibraries().size() == 0) return;
 		p.registerComponents(c, resource.getFullPath());
 	}
 
 	void getJavaSourceRoots(IProject project) {
 		IJavaProject javaProject = EclipseResourceUtil.getJavaProject(project);
 		if(javaProject == null) return;
 		List<IPath> ps = new ArrayList<IPath>();
 		List<IPath> os = new ArrayList<IPath>();
 		try {
 			IPath output = javaProject.getOutputLocation();
 			if(output != null) os.add(output);
 			IClasspathEntry[] es = javaProject.getResolvedClasspath(true);
 			for (int i = 0; i < es.length; i++) {
 				if(es[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 					IResource findMember = ModelPlugin.getWorkspace().getRoot().findMember(es[i].getPath());
 					if(findMember != null && findMember.exists()) {
 						ps.add(findMember.getFullPath());
 					}
 					IPath out = es[i].getOutputLocation();
 					if(out != null && !os.contains(out)) {
 						os.add(out);
 					}
 				} 
 			}
 			srcs = ps.toArray(new IPath[0]);
 			outs = os.toArray(new IPath[0]);
 		} catch(CoreException ce) {
 			ModelPlugin.getPluginLog().logError("Error while locating java source roots for " + project, ce); //$NON-NLS-1$
 		}
 	}
 
 }
