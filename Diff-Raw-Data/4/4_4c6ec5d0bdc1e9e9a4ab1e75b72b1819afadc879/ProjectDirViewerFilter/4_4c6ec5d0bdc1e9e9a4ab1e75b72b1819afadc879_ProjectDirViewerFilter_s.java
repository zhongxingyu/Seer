 package org.mumps.pathstructure.generic;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 
 public class ProjectDirViewerFilter extends ViewerFilter {
 
 	private String projectName;
 
 	public ProjectDirViewerFilter(String projectName) {
 		this.projectName = projectName;
 	}
 
 	@Override
 	public boolean select(Viewer viewer, Object parent, Object element) {
 		IResource eRes = (IResource) element;
 		IResource pRes = (IResource) element;
 		
		if (!eRes.getProject().getName().equals(projectName))
 			return false;
 		
 		if (eRes.getType() == IResource.FILE || (eRes.getType() == IResource.FOLDER && eRes.getName().equals("backups")))
 			return false;
 		
 		return true;
 //		
 //		if (res.getProject().getName().equals(projectName))
 //			return true;
 //		else
 //			return false;
 	}
 }
