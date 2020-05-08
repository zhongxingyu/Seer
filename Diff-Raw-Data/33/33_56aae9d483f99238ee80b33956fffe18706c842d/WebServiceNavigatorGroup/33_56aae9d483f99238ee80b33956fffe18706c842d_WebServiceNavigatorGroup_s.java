 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Jan 20, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.webservice;
 
import org.eclipse.core.internal.resources.WorkspaceRoot;
 
 
 /**
  * @author jlanuti
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class WebServiceNavigatorGroup {
 
 	private String label = WebServiceUIResourceHandler.getString("WebServiceNavigatorGroup_UI_0"); //$NON-NLS-1$
	private WorkspaceRoot root;
 
 	/**
 	 * Default constructor
 	 */
	public WebServiceNavigatorGroup(WorkspaceRoot wsRoot) {
 		super();
 		root = wsRoot;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return label;
 	}
 
 	/**
 	 * @return Returns the root.
 	 */
	public WorkspaceRoot getRoot() {
 		return root;
 	}
 
 	/**
 	 * @param root
 	 *            The root to set.
 	 */
	public void setRoot(WorkspaceRoot root) {
 		this.root = root;
 	}
 
 }
