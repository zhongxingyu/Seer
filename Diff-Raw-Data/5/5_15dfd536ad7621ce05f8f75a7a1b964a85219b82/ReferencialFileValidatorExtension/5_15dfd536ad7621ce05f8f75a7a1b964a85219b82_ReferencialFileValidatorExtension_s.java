 /*******************************************************************************
  * Copyright (c) 2001, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on May 3, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.wst.validation.internal;
 
import org.eclipse.core.internal.runtime.Assert;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.wst.validation.internal.operations.ReferencialFileValidator;
 
 /**
  * @author vijayb
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class ReferencialFileValidatorExtension {
 	private String id = null;
 	private ReferencialFileValidator instance;
 	private boolean errorCondition = false;
 	private IConfigurationElement element;
 	public static final String REF_FILE_VALIDATOR_EXTENSION = "referencialFileValidator"; //$NON-NLS-1$
 	public static final String RUN = "run"; //$NON-NLS-1$
 	public static final String ATT_ID = "id"; //$NON-NLS-1$
 	public static final String ATT_CLASS = "class"; //$NON-NLS-1$
 
 	/**
 	 *  
 	 */
 	public ReferencialFileValidatorExtension() {
 		super();
 	}
 
 	public ReferencialFileValidator getInstance() {
 		try {
 			if (instance == null && !errorCondition)
 				instance = (ReferencialFileValidator) element.createExecutableExtension("run"); //$NON-NLS-1$
 		} catch (Throwable e) {
 			Logger.getLogger().logError(e);
 			errorCondition = true;
 		}
 		return instance;
 	}
 
 	public ReferencialFileValidatorExtension(IConfigurationElement element) {
		Assert.isLegal(REF_FILE_VALIDATOR_EXTENSION.equals(element.getName()), "Extensions must be of the type \"" + REF_FILE_VALIDATOR_EXTENSION + "\"."); //$NON-NLS-1$ //$NON-NLS-2$
 		this.element = element;
 		init();
 	}
 
 	private void init() {
 		this.id = this.element.getAttribute(ATT_ID);
 	}
 
 	/**
 	 * @return Returns the id.
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            The id to set.
 	 */
 	public void setId(String id) {
 		this.id = id;
 	}
 }
