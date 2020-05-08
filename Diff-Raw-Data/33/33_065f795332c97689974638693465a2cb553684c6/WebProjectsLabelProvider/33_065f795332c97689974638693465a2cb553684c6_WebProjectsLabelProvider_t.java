 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.navigator;
 
import org.jboss.tools.common.model.filesystems.impl.Libs;
 import org.jboss.tools.common.model.ui.navigator.NavigatorLabelProvider;
 import org.jboss.tools.common.model.ui.navigator.decorator.DecoratorManager;
 import org.jboss.tools.common.model.ui.navigator.decorator.XModelObjectDecorator;
 
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.util.*;
 
 public class WebProjectsLabelProvider extends NavigatorLabelProvider {
 	
 	public WebProjectsLabelProvider() {
 	}
 	
 	@SuppressWarnings("nls")
 	public String getText(Object element) {
 		if(!(element instanceof XModelObject)) return "";
 		XModelObject o = (XModelObject)element;
 		String entity = o.getModelEntity().getName();
 
 		XModelObjectDecorator d = DecoratorManager.getInstance().getDecoratorByEntity(entity);
 		if(d != null) {
 			String label = d.getLabel(o);
 			return applyModification(o, label);
 		}
 		
 		if(o.getFileType() != XModelObject.FILE) return super.getText(element);
 		
 		if("FilePROPERTIES".equals(entity)) {
 			XModelObject fs = o;
 			while(fs != null && fs.getFileType() != XModelObject.SYSTEM) fs = fs.getParent();
 			if(fs == null || !fs.getAttributeValue("name").startsWith("src")) return super.getText(element);
 			String res = XModelObjectLoaderUtil.getResourcePath(o);
 			if(res == null || !res.endsWith(".properties")) return super.getText(element);
 			res = res.substring(1, res.length() - 11).replace('/', '.');
 			return applyModification(o, res);
 		} else if("FileSystemFolder".equals(entity)) {
 			String s = o.getAttributeValue("location").replace('\\', '/');
 			return s.substring(s.lastIndexOf('/') + 1);			
 		}
 
 		//of jar
 		XModelObject p = o.getParent();
 		while(p != null && p.getFileType() != XModelObject.SYSTEM) p = p.getParent();
 		if(p != null && p.getModelEntity().getName().equals("FileSystemJar")) {
 			String n = p.getAttributeValue("name");
			if(n != null && n.startsWith(Libs.LIB_PREFIX)) n = n.substring(4);
 			String addition = (n != null) ? " - " + n : "";
 			return super.getText(element) + addition;
 		}
 		
 		return super.getText(element);
 	}
 	
 	public void dispose() {}
 
 }
