 package org.jboss.tools.jst.web.kb.internal.validation;
 
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IMarkerResolution;
 import org.eclipse.ui.IMarkerResolution2;
 import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.wst.validation.internal.plugin.ValidationPlugin;
 import org.jboss.tools.jst.web.kb.WebKbPlugin;
 
 public class BuilderOrderResolutionGenerator implements IMarkerResolutionGenerator2 {
 
 	public IMarkerResolution[] getResolutions(IMarker marker) {
 		try {
 			if(ValidatorManager.ORDER_PROBLEM_MARKER_TYPE.equals(marker.getType())) {
 				return new IMarkerResolution[]{new BuilderOrderResolution()};
 			}
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 		return new IMarkerResolution[0];
 	}
 
 	public boolean hasResolutions(IMarker marker) {
 		try {
 			return ValidatorManager.ORDER_PROBLEM_MARKER_TYPE.equals(marker.getType());
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 		return false;
 	}
 
 }
 
 class BuilderOrderResolution implements IMarkerResolution2 {
 
 	public String getLabel() {
 		return "Change Order of Builders."; //$NON-NLS-1$
 	}
 
 	public void run(IMarker marker) {
 		IProject project = marker.getResource().getProject();
 		
 		try {
 			IProjectDescription d = project.getDescription();
 			ICommand[] bs = d.getBuildSpec();
 			ICommand v = null;
 			for (int i = 0; i < bs.length; i++) {
				if(ValidationPlugin.VALIDATION_BUILDER_ID.equals(bs[i].getBuilderName())) {
 					v = bs[i];
 				}
 				if(v != null) {
 					bs[i] = (i + 1 < bs.length) ? bs[i + 1] : v;
 				}
 			}
 			d.setBuildSpec(bs);
 			project.setDescription(d, IProject.FORCE, new NullProgressMonitor());
 			project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 		
 	}
 
 	public String getDescription() {
 		return null;
 	}
 
 	public Image getImage() {
 		return null;
 	}
 	
 }
