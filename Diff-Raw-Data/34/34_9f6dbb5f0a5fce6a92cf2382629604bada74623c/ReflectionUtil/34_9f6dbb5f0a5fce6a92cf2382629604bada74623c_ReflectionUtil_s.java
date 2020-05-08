 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.util;
 
 import java.lang.reflect.Method;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.graphiti.internal.util.T;
 import org.eclipse.swt.dnd.Transfer;
 
 /**
  * In this class, we combine all reflection code introduced to make Graphiti run
  * in a lean RCP setup, that is, for instance without org.eclipse.ui.ide
  * installed. An alternative would be to use extension points which seems to be
  * rather heavy-weight.
  */
@SuppressWarnings({ "unchecked", "rawtypes" })
 public class ReflectionUtil {
 
 	private static final String CLASSNAME_RESOURCE_TRANSFER = "org.eclipse.ui.part.ResourceTransfer"; //$NON-NLS-1$
 	private static final String METHODNAME_RESOURCE_TRANSFER = "getInstance"; //$NON-NLS-1$
 
	private static final String CLASSNAME_FILE_EDITOR_INPUT = "org.eclipse.ui.IFileEditorInput"; //$NON-NLS-1$
	private static final String METHODNAME_FILE_EDITOR_INPUT = "getFile"; //$NON-NLS-1$

 	/**
	 * IFileEditorInput resides in org.eclipse.ui.ide. We need to support an RCP
	 * scenario without having this plug-in installed. Therefore ashamed we use
	 * dynamic class-loading and reflection .
 	 */
 	public static IFile getFile(Object input) {
		Class[] genericInterfaces = input.getClass().getInterfaces();
		for (Class interfaze : genericInterfaces) {
			if (CLASSNAME_FILE_EDITOR_INPUT.equals(interfaze.getName())) {
				try {
					interfaze = Class.forName(CLASSNAME_FILE_EDITOR_INPUT);
					Method method = interfaze.getMethod(METHODNAME_FILE_EDITOR_INPUT);
					Object obj = method.invoke(input);
					if (obj != null && obj instanceof IFile) {
						return (IFile) obj;
					}
				} catch (Exception e) {
					T.racer().debug(e.getMessage());
				}
			}
 		}
 		return null;
 	}
 
 	/**
 	 * ResourceTransfer resides in org.eclipse.ui.ide. We need to support an RCP
 	 * scenario without having this plug-in installed. Therefore ashamed we use
 	 * dynamic class-loading and reflection.
 	 */
 	public static Transfer getResourceTransfer() {
 		Transfer resourceTransfer = null;
 		try {
 			Class<?> forName = Class.forName(CLASSNAME_RESOURCE_TRANSFER);
 			Method method = forName.getMethod(METHODNAME_RESOURCE_TRANSFER);
 			Object obj = method.invoke(forName);
 			if (obj != null && obj instanceof Transfer)
 				resourceTransfer = (Transfer) obj;
 		} catch (Exception e) {
 			T.racer().debug(e.getMessage());
 		}
 		return resourceTransfer;
 	}
 }
