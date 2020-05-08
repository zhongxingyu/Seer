 package org.eclipse.dltk.javascript.internal.debug.ui.launchConfigurations;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.debug.ui.launchConfigurations.MainLaunchConfigurationTab;
 import org.eclipse.dltk.javascript.core.JavaScriptLanguageToolkit;
 import org.eclipse.dltk.javascript.core.JavaScriptNature;
 import org.eclipse.dltk.ui.DLTKPluginImages;
 import org.eclipse.swt.graphics.Image;
 
 public class JavaScriptMainLaunchConfigurationTab extends MainLaunchConfigurationTab {
 
 	protected boolean validateProject(IScriptProject project) {
 		if (project == null)
 			return false;
 
 		try {
 			IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 					.getLanguageToolkit(project);
 			if (toolkit instanceof JavaScriptLanguageToolkit) {
 				return true;
 			}
 		} catch (CoreException e) {
 		}
 
 		return false;
 	}
 
 	protected String getNatureID() {
 		return JavaScriptNature.NATURE_ID;
 	}
 
 	public Image getImage() {
 		return DLTKPluginImages.get(DLTKPluginImages.IMG_OBJS_CLASS);
 	}
 
 	
 }
