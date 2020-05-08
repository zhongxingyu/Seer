 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Johannes Faltermeier
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.provider;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.impl.FileURIHandlerImpl;
import org.eclipse.emf.emfstore.common.URIUtil;
 
 /**
  * Handler for projectspace file URIs. Adds functionality for successfully deleting temp folders.
  * 
  * @author jfaltermeier
  * 
  */
 public class ProjectSpaceFileURIHandler extends FileURIHandlerImpl {
 
 	@Override
 	public boolean canHandle(URI uri) {
		if (uri.segment(2).equals(URIUtil.PROJECTSPACES_SEGMENT)) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void delete(URI uri, Map<?, ?> options) throws IOException
 	{
 		File file = new File(uri.toFileString());
 		File parent = file.getParentFile();
 		file.delete();
 
 		if (parent != null && parent.listFiles().length == 1) {
 			// if there is only one child left, it's the temp folder.
 			FileUtils.deleteDirectory(parent);
 		}
 	}
 
 }
