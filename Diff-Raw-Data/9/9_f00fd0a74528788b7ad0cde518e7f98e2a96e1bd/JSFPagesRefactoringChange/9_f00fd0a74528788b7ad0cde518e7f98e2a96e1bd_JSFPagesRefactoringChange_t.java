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
 package org.jboss.tools.jst.web.refactoring;
 
 import java.util.*;
 import org.eclipse.core.resources.*;
 import org.eclipse.ltk.core.refactoring.CompositeChange;
 import org.jboss.tools.common.model.*;
 import org.jboss.tools.common.model.filesystems.FileSystemsHelper;
 import org.jboss.tools.common.model.filesystems.impl.FileAnyImpl;
 import org.jboss.tools.common.model.util.*;
 import org.jboss.tools.common.util.FileUtil;
 import org.jboss.tools.common.model.refactoring.RefactoringHelper;
 import org.jboss.tools.jst.web.WebModelPlugin;
 import org.jboss.tools.jst.web.messages.xpl.WebUIMessages;
 import org.jboss.tools.jst.web.project.list.*;
 
 /**
  * Changes in jsp caused by file rename.
  * 
  * @author glory
  */
 
 public class JSFPagesRefactoringChange extends CompositeChange {
 	protected String newName;
 	protected String oldName;
 	protected XModel model;
 	protected XModelObject fileObject;
 	
 	String resourcePath;
 	String newResourcePath;
 	String jsfResourcePath;
 	String newJsfResourcePath;
 	
 	public JSFPagesRefactoringChange(XModelObject fileObject, String newName) {
 		super(WebUIMessages.JSP_REFACTORING);
 		this.model = fileObject.getModel();
 		this.fileObject = fileObject;
 		resourcePath = XModelObjectLoaderUtil.getResourcePath(fileObject);
 		int i = resourcePath == null ? -1 : resourcePath.lastIndexOf('/');
 		if(i >= 0) newResourcePath = resourcePath.substring(0, i + 1) + newName;
 		this.newName = newName;
 		oldName = fileObject.getAttributeValue("name"); //$NON-NLS-1$
 		List list = WebPromptingProvider.getInstance().getList(model, IWebPromptingProvider.JSF_GET_URL, resourcePath, new Properties());
 		if(list != null && list.size() > 0) {
 			jsfResourcePath = list.get(0).toString();
 		}
 		list = WebPromptingProvider.getInstance().getList(model, IWebPromptingProvider.JSF_GET_URL, newResourcePath, new Properties());
 		if(list != null && list.size() > 0) {
 			newJsfResourcePath = list.get(0).toString();
 		}
		addChanges();
 	}
 	
 	public XModel getModel() {
 		return model;
 	}
 	
	private void addChanges() {
 		if(model == null) return;
 		XModelObject webRoot = FileSystemsHelper.getWebRoot(model);
 		if(webRoot == null) return;
 		addChanges(webRoot.getChildren());
 	}
 	
 	private void addChanges(XModelObject[] objects) {
 		for (int i = 0; i < objects.length; i++) {
 			if(objects[i].getFileType() == XModelObject.FOLDER) {
 				addChanges(objects[i].getChildren());
 			} else {
 				String entity = objects[i].getModelEntity().getName();
 				if(".FileJSP.FileHTML.FileXHTML.".indexOf("." + entity + ".") < 0) continue; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				FileAnyImpl f = (FileAnyImpl)objects[i];
 				String text = f.getAsText();
 				if(text == null || text.indexOf(oldName) < 0) continue;
 				Properties replacements = createReplacements(f);
 				RefactoringHelper.addChanges(objects[i], replacements, this);
 			}
 		}
 	}
 	
 	Properties createReplacements(FileAnyImpl f) {
 		Properties replacements = new Properties();
 		if(resourcePath == null) return replacements;
 		char lastChar = fileObject.getFileType() == XModelObject.FILE ? '"' : '/';
 		replacements.setProperty("\"" + resourcePath + lastChar, "\"" + newResourcePath + lastChar); //$NON-NLS-1$ //$NON-NLS-2$
 		replacements.setProperty("\"." + resourcePath + lastChar, "\"." + newResourcePath + lastChar); //$NON-NLS-1$ //$NON-NLS-2$
 		if(jsfResourcePath != null) {
 			replacements.setProperty("\"" + jsfResourcePath + lastChar, "\"" + newJsfResourcePath + lastChar); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		IFile f1 = (IFile)EclipseResourceUtil.getResource(f);
 		if(f1 == null) return replacements;
 		String path1 = f1.getParent().getLocation().toString().replace('\\', '/');
 		IResource f2 = (IResource)EclipseResourceUtil.getResource(fileObject);
 		if(f2 == null) return replacements;
 		String path2 = f2.getLocation().toString().replace('\\', '/');
 		String relativePath = FileUtil.getRelativePath(path1, path2);
 		if(relativePath == null) return replacements;
 		int i = relativePath.lastIndexOf('/');
 		String newRelativePath = relativePath.substring(0, i + 1) + newName;
 		if(relativePath.startsWith("/")) { //$NON-NLS-1$
 			replacements.setProperty("\"" + relativePath.substring(1) + lastChar, "\"" + newRelativePath.substring(1) + lastChar); //$NON-NLS-1$ //$NON-NLS-2$
 			replacements.setProperty("\"." + relativePath + lastChar, "\"." + newRelativePath + lastChar); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		return replacements;
 	}
 
 }
