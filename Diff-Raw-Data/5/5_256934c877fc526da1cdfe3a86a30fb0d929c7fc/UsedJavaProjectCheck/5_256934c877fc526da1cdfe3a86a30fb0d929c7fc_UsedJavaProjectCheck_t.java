 package org.jboss.tools.jst.web.kb.internal.scanner;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.jboss.tools.jst.web.kb.IKbProject;
 import org.jboss.tools.jst.web.kb.KbMessages;
 import org.jboss.tools.jst.web.kb.KbProjectFactory;
 import org.jboss.tools.jst.web.kb.internal.KbBuilderMarker;
 import org.jboss.tools.jst.web.kb.internal.KbProject;
 
 public class UsedJavaProjectCheck {
 	
 	public void check(KbProject project) throws CoreException {
 		if(!project.getProject().isAccessible()) {
 			return;
 		}
 		List<IProject> list = getNonKbJavaProjects(project.getProject());
 		
 		IMarker[] ms = KbBuilderMarker.getOwnedMarkers(project.getProject(), KbBuilderMarker.KIND_DEPENDS_ON_NON_KB_POJECTS);
 		if(list.isEmpty()) {
 			if(ms != null) {
 				for (IMarker m: ms) {
 					m.delete();
 				}
 			}
 		} else {
 			IMarker m = (ms == null) ? null : ms[0];
 			String projectList = asText(list);
 			String messageId = list.size() == 1 ? KbMessages.KBPROBLEM_DEPENDS_ON_JAVA_SINGLE : KbMessages.KBPROBLEM_DEPENDS_ON_JAVA_MANY;
 			String message = MessageFormat.format(messageId, projectList);
 			m = KbBuilderMarker.createOrUpdateKbProblemMarker(m, project.getProject(), message, KbBuilderMarker.KIND_DEPENDS_ON_NON_KB_POJECTS);
			for (IProject p: list) {
				if(p.isAccessible()) {
					p.setPersistentProperty(KbProjectFactory.NATURE_MOCK, "true"); //$NON-NLS-1$
				}
			}
 		}		
 	}
 
 	public String asText(List<IProject> list) {
 		StringBuffer projectList = new StringBuffer();
 		for (IProject p: list) {
 			if(projectList.length() > 0) {
 				projectList.append(", "); //$NON-NLS-1$
 			}
 			projectList.append(p.getName());
 		}
 		return projectList.toString();
 	}
 
 	public List<IProject> getNonKbJavaProjects(IProject project) throws CoreException {
 		List<IProject> list = new ArrayList<IProject>();
 		if(!project.hasNature(IKbProject.NATURE_ID)) {
 			//do not check dependencies in projects without kb nature.
 			return list;
 		}
 		if(project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
 			IJavaProject javaProject = JavaCore.create(project);
 			IClasspathEntry[] es = javaProject.getResolvedClasspath(true);
 			for (int i = 0; i < es.length; i++) {
 				if(es[i].getEntryKind() == IClasspathEntry.CPE_PROJECT) {
 					IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(es[i].getPath().lastSegment());
 					if(p != null && p.isAccessible() 
 							&& p.hasNature(JavaCore.NATURE_ID)
 							&& !p.hasNature(IKbProject.NATURE_ID)) {
 						list.add(p);
 					}
 				}
 			}
 			
 		}
 		return list;
 	}
 	
 
 }
