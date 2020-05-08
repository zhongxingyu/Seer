 /*******************************************************************************
  *  Copyright 2007 Ketan Padegaonkar http://ketan.padegaonkar.name
  *  
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *  
  *      http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  ******************************************************************************/
 package net.sourceforge.jcctray.ui.settings.providers;
 
 import net.sourceforge.jcctray.model.DashBoardProject;
 import net.sourceforge.jcctray.model.Host;
 import net.sourceforge.jcctray.model.JCCTraySettings;
 
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 
 public class EnabledProjectsFilter extends ViewerFilter {
 
 	private boolean	enabled;
 	
 	public EnabledProjectsFilter(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	public boolean select(Viewer viewer, Object parentElement, Object element) {
 		if (element instanceof DashBoardProject) {
 			DashBoardProject project = (DashBoardProject) element;
 			Host host = project.getHost();
 			Host hostInSettings = JCCTraySettings.getInstance().findHostByString(host.getHostString());
 			DashBoardProject projectInSettings = hostInSettings.getConfiguredProject(project.getName());
			if ((projectInSettings != null) && projectInSettings.isEnabled() == this.enabled)
 				return true;
 		}
 		return false;
 	}
 }
