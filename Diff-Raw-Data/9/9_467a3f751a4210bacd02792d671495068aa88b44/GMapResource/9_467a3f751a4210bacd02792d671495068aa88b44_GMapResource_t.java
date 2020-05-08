 /*******************************************************************************
  * Copyright (c) 2009 EclipseSource Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the 
  * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v1.0 
  * which accompanies this distribution. The Eclipse Public License is available at 
  * http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution License 
  * is available at http://www.eclipse.org/org/documents/edl-v10.php.
  *
  * Contributors: 
  *     EclipseSource Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.examples.toast.rap.gmaps;
 
 import org.eclipse.rwt.resources.IResource;
 import org.eclipse.rwt.resources.IResourceManager.RegisterOptions;
 
 public class GMapResource implements IResource {
 
 	public String getCharset() {
 		return "ISO-8859-1";
 	}
 
 	public ClassLoader getLoader() {
 		return this.getClass().getClassLoader();
 	}
 
 	public RegisterOptions getOptions() {
 		return RegisterOptions.VERSION_AND_COMPRESS;
 	}
 
 	public String getLocation() {
		return "org/eclipse/examples/toast/rap/gmaps/GMap.js";
 	}
 
 	public boolean isJSLibrary() {
 		return true;
 	}
 
 	public boolean isExternal() {
 		return false;
 	}
 }
