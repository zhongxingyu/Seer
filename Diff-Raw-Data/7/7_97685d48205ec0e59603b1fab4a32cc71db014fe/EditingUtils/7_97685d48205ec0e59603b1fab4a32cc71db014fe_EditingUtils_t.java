 /*******************************************************************************
  * Copyright (c) 2008, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.utils;
 
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.eef.runtime.ui.UIConstants;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class EditingUtils {
 
 	/**
 	 * @param part
 	 *            the workbench part managing the editing domain
 	 * @return if provided the editingDomain of the given workbench part
 	 */
 	public static EditingDomain getResourceSetFromEditor(IWorkbenchPart part) {
 		EditingDomain editingDomain = null;
 		if (part instanceof IEditingDomainProvider) {
 			editingDomain = ((IEditingDomainProvider)part).getEditingDomain();
 		} else {
 			if (part.getAdapter(IEditingDomainProvider.class) != null)
 				editingDomain = ((IEditingDomainProvider)part
 						.getAdapter(IEditingDomainProvider.class)).getEditingDomain();
 			else if (part.getAdapter(EditingDomain.class) != null)
 				editingDomain = (EditingDomain)part.getAdapter(EditingDomain.class);
 		}
 		return editingDomain;
 	}
 
 	/**
 	 * Set an id to a given widget.
 	 * 
 	 * @param widget
 	 *            the widget where put the ID
 	 * @param value
 	 *            the ID to put
 	 */
 	public static void setID(Control widget, Object value) {
 		if (widget != null)
 			widget.setData(UIConstants.EEF_WIDGET_ID_KEY, value);
 	}
 
 	/**
 	 * Return the ID of a widget?
 	 * 
 	 * @param widget
 	 *            the widget to inspect
 	 * @return the ID of the widget
 	 */
 	public static Object getID(Control widget) {
 		if (widget != null)
 			return widget.getData(UIConstants.EEF_WIDGET_ID_KEY);
 		return null;
 	}
 
 	/**
 	 * Set the EEF type of widget.
 	 * 
 	 * @param widget
 	 *            the widget where put the ID
 	 * @param value
 	 *            the type of the widget
 	 */
 	public static void setEEFtype(Control widget, String value) {
 		if (widget != null)
 			widget.setData(UIConstants.EEF_WIDGET_TYPE_KEY, value);
 	}
 
 	/**
 	 * Return the ID of a widget?
 	 * 
 	 * @param widget
 	 *            the widget to inspect
 	 * @return the ID of the widget
 	 */
 	public static String getEEFType(Control widget) {
 		if (widget != null) {
 			Object data = widget.getData(UIConstants.EEF_WIDGET_ID_KEY);
 			if (data instanceof String)
 				return (String)data;
 		}
 		return UIConstants.UNKNOW_EEF_TYPE;
 	}
 
 	/**
 	 * @return platform shell
 	 */
 	public static Shell getShell() {
 		Shell theShell = null;
		if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
 			theShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		} else {
		    theShell = new Shell();
		}
 		return theShell;
 	}
 }
