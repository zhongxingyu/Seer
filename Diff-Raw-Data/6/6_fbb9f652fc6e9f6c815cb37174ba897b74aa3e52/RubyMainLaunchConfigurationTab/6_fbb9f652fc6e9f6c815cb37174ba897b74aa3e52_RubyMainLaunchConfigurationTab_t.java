 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.debug.ui.launchConfigurations;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.debug.ui.launchConfigurations.MainLaunchConfigurationTab;
 import org.eclipse.dltk.ruby.core.RubyLanguageToolkit;
import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ui.DLTKPluginImages;
 import org.eclipse.swt.graphics.Image;
 
 public class RubyMainLaunchConfigurationTab extends MainLaunchConfigurationTab {
 
 
 	protected boolean validateProject(IDLTKProject project) {
 		if (project == null)
 			return false;
 		// check project nature		
 		try {
 			IDLTKLanguageToolkit ltk = DLTKLanguageManager.getLanguageToolkit(project);
 			if (ltk instanceof RubyLanguageToolkit)
 				return true;
 		} catch (CoreException e) {
 		}
 		return false;
 	}
 	
 	protected String getLanguageName () {
 		return "RUBY";
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
 	 */
 	public Image getImage() {
 		return DLTKPluginImages.get(DLTKPluginImages.IMG_OBJS_CLASS);
 	}
 
	protected String getNatureID() {
		return RubyNature.NATURE_ID;
	}

 }
