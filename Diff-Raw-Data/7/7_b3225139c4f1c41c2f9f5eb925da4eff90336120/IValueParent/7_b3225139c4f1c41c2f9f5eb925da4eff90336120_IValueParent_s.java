 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.typeinference;
 
 import java.util.Set;
 
 public interface IValueParent {
 
 	Set<String> getDirectChildren();
 
 	Set<String> getDeletedChildren();
 
 	boolean hasChild(String name);
 
 	/**
	 * Finds the child with the specified name.
 	 * 
 	 * @param name
	 * @return the child found or <code>null</code> if there is no child with
	 *         such name
 	 */
 	IValueReference getChild(String name);
 
 }
