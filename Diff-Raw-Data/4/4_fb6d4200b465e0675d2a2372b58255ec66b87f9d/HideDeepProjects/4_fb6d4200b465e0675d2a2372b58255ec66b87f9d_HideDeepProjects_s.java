 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.customprojects.rcp.projects;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HideDeepProjects extends ViewerFilter {
 
 	private static Logger logger = LoggerFactory
 			.getLogger(HideDeepProjects.class);
 	
 	static final String SINGLE_LEVEL_NATURE = uk.ac.diamond.scisoft.customprojects.rcp.natures.SingleLevelProjectNature.NATURE_ID;
 	
 	
 	@Override
 	public boolean select(Viewer viewer, Object parentElement, Object element) {
 //		logger.debug("dealing with parentElement: " + parentElement.getClass().toString());
 //		logger.debug("parent of element " + element.getClass().toString());	
 		
 	
 		if(element instanceof IResource){
 //			logger.debug("================== parentElement: " + parentElement.getClass().getName());
 //			logger.debug("================== MET AN IResource: " + ((IResource)element).getName());
 //			logger.debug("================== my parent is: " + ((IResource)element).getParent().getName());
 //			logger.debug("================== my project is: " + ((IResource)element).getProject().getName());	
 						
 			boolean isTopProjectSingleLevel = false;
 			try {
 				if(((IResource)element).getProject().isAccessible()){
 				isTopProjectSingleLevel = ((IResource)element).getProject().hasNature(SINGLE_LEVEL_NATURE);
 				}
 			} catch (CoreException e1) {
 				e1.printStackTrace();
 			}
 			
 			
 			if(isTopProjectSingleLevel){
 
 			if (!(((IResource)element) instanceof IFile) && ((IResource)element).getParent().isLinked()){
 				
 				logger.debug(">>>>>>>>  HIDDING : " + ((IResource)element).getName());
 				
 				try {
 					((IResource)element).setHidden(true);
 				} catch (CoreException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			}
 		}
 		
 		//logger.debug("---------  REFRESHING --------------");
 		try {
 			((IResource)element).getParent().refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
 			((IResource)element).getProject().refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
 			((IResource)element).refreshLocal(IResource.DEPTH_ZERO,	new NullProgressMonitor());
 			
 		} catch (CoreException e) {
 			logger.error("error refreshing project explorer" + e.getMessage());
 		}
	
 		
 		return true;
 	}
 
 }
