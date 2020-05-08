 package org.fedoraproject.eclipse.packager.tests;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.team.internal.ccvs.core.CVSTag;
 import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
 import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
 import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
 import org.eclipse.team.internal.ccvs.ui.operations.CheckoutSingleProjectOperation;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.progress.IProgressService;
 
 @SuppressWarnings("restriction")
 public class CVSTestProject {
 	public static final String SCM_URL = ":pserver;username=anonymous;hostname=cvs.fedoraproject.org:/cvs/pkgs";
 	protected IProject project;
 	
 	public CVSTestProject(String name, String tag) throws CoreException, InvocationTargetException, InterruptedException {
 		ICVSRepositoryLocation repo = CVSRepositoryLocation.fromString(SCM_URL);
		ICVSRemoteFolder remoteFolder = repo.getRemoteFolder("rpms" + Path.SEPARATOR + name, new CVSTag(tag, CVSTag.VERSION));
 		
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		project = root.getProject(name);
 		
 		IProgressService progress = PlatformUI.getWorkbench().getProgressService();
 		IRunnableWithProgress op = new CheckoutSingleProjectOperation(null, remoteFolder, project, null, false);
 		progress.busyCursorWhile(op);
 		
 		project.refreshLocal(IResource.DEPTH_INFINITE, null);
 	}
 	
 	public void dispose() throws Exception {
 		project.delete(true, true, null);
 		project.refreshLocal(IResource.DEPTH_INFINITE, null);
 	}
 	
 	public IProject getProject() {
 		return project;
 	}
 }
