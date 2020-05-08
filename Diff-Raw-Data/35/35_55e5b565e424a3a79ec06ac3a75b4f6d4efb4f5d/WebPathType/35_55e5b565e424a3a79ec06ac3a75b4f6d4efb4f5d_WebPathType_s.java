 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 		Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.taglibprocessing.attributevalues;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.jsf.metadataprocessors.IMetaDataEnabledFeature;
 import org.eclipse.jst.jsf.metadataprocessors.features.IValidValues;
 import org.eclipse.jst.jsf.metadataprocessors.features.ValidationMessage;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualContainer;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 
 /**
  * Path relative to web root
  * <b>EXPERIMENTAL</b> - may change or dissappear
  *
  */
 public class WebPathType extends PathType implements
 	IMetaDataEnabledFeature, IValidValues{
 
 	public boolean isValidValue(String value) {
 		//is this a relative path, or absolute url?
 		try {
 			URL url = new URL(value);			
 			//if file protocol, see if it is valid?  Otherwise assume ok.
 			if (url.getProtocol().equals("file")){ //$NON-NLS-1$
 				validateFileRelativeToWebRoot(url.getPath());
 			} 
 		} catch (MalformedURLException e) {
			//is this a valid path relative to the 			
			validateFileRelativeToWebRoot(value);						
 		}
 		return getValidationMessages().size() == 0;
 
 	}
 
     private void validateFileRelativeToWebRoot(String value) {
 		IVirtualContainer webRoot = getWebRoot();
 		if (! webRoot.exists()){
 			getValidationMessages().add(new ValidationMessage( Messages.WebPathType_1));
 		} 
 		else {
 			IVirtualFile file = webRoot.getFile(new Path(value));
 			if (!file.exists()) {
 				//was this a valid file path string, or bogus url?
 				getValidationMessages().add(new ValidationMessage(Messages.WebPathType_2));
 			}									
 			//we could also validate the expected file-extensions from meta data
 		}
 		
 	}
 
 	private IVirtualContainer getWebRoot()
     {
         IVirtualContainer webRoot =
             ComponentCore.createComponent(getProject()).getRootFolder();
         	       
         return webRoot;
     }
 }
