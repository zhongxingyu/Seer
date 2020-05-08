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
 package org.eclipse.jst.j2ee.internal.archive.operations;
 
 
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.wst.common.internal.emfworkbench.validateedit.ResourceStateValidator;
 
 
 /**
  * Insert the type's description here. Creation date: (4/16/2001 3:12:41 PM)
  * 
  * @author: Administrator
  */
 public interface IOverwriteHandler {
 	public static final String NO = EJBArchiveOpsResourceHandler.getString("No_UI"); //$NON-NLS-1$
 	public static final String YES = EJBArchiveOpsResourceHandler.getString("Yes_UI"); //$NON-NLS-1$
 	public static final String ALL = EJBArchiveOpsResourceHandler.getString("Yes_To_All_UI"); //$NON-NLS-1$
 	public static final String NONE = EJBArchiveOpsResourceHandler.getString("Cancel_UI_"); //$NON-NLS-1$
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
 	org.eclipse.core.resources.IProject getProject();
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
 	boolean isOverwriteAll();
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
 	boolean isOverwriteNone();
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
 	boolean isOverwriteResources();
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
 	String queryOverwrite(String resourcePath);
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
 	void setEarSaveStrategy(EARComponentSaveStrategyImpl newEarSaveStrategy);
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
	void setEjbSaveStrategy(J2EEComponentSaveStrategyImpl newEjbSaveStrategy);
 
 	/**
 	 * Sets the RAR SaveStrategy.
 	 * 
 	 * @param RARProjectSaveStrategyImpl
 	 *            rarSaveStrategy - The rar save strategy to set.
 	 */
	void setRARSaveStrategy(J2EEComponentSaveStrategyImpl newRARSaveStrategy);
 
 	void setOverwriteAll(boolean newAllTrue);
 
 	void setOverwriteNone(boolean newNoneTrue);
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
 	void setOverwriteResources(boolean newOverwriteResources);
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
 	void setProject(org.eclipse.core.resources.IProject newProject);
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
	void setWarSaveStrategy(J2EEComponentSaveStrategyImpl newWarSaveStrategy);
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
 	boolean shouldOverwrite(String uri);
 
 	/**
 	 * Check to see if the file can be checked out.
 	 * 
 	 * @throws OverwriteHandlerException
 	 *             if the validate fails
 	 */
 	void validateEdit(IFile aFile) throws OverwriteHandlerException;
 
 	/**
 	 * Check to see if the files can be checked out.
 	 * 
 	 * @throws OverwriteHandlerException
 	 *             if the validate fails
 	 */
 	void validateState(ResourceStateValidator resourceStateValidator);
 
 	/**
 	 * Insert the method's description here. Creation date: (4/16/2001 3:13:38 PM)
 	 * 
 	 * @return boolean
 	 */
 	boolean shouldOverwriteWLProject(IProject fProject, java.lang.String uri);
 }
