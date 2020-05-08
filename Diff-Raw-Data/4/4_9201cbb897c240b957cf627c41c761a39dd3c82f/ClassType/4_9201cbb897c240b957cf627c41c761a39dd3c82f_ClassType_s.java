 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
 
  *******************************************************************************/
 package org.eclipse.dltk.ti.types;
 
 import org.eclipse.dltk.core.search.indexing.IIndexConstants;
 
 /**
  * Represents type as some user class Each such class should be presented inside
  * a DLTK MixinModel.
  */
 public abstract class ClassType implements IEvaluatedType {
 
 	public String getTypeName() {
		String typeName = getModelKey().replace(
				String.valueOf(IIndexConstants.SEPARATOR), "::"); //$NON-NLS-1$
 		if (typeName.endsWith("%") == true) { //$NON-NLS-1$
 			typeName = typeName.substring(0, (typeName.length() - 1));
 		}
 		return typeName;
 	}
 
 	public abstract String getModelKey();
 
 	public String toString() {
 		return getModelKey();
 	}
 
 }
