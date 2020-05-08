 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.environment.IFileHandle;
 
 public interface ISourceCodeCache {
 
 	/**
 	 * @param abstractSourceModule
 	 * @return
 	 * @throws ModelException
 	 */
 	char[] get(IFile resource) throws ModelException;
 
 	/**
 	 * @param file
 	 * @return
 	 */
 	char[] get(IFileHandle file) throws ModelException;
 
 }
