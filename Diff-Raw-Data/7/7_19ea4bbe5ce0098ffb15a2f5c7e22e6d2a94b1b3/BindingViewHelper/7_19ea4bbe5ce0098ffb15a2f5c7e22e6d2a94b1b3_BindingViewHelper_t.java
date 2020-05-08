 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.emf.eef.runtime.ui.parts.impl;
 
 import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.context.ExtendedPropertiesEditingContext;
 import org.eclipse.emf.eef.runtime.ui.parts.ViewHelper;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  *
  */
 public class BindingViewHelper implements ViewHelper {
 	
 	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
 
 	/**
 	 * Image registry key for help image (value <code>"dialog_help_image"</code> ).
 	 */
 	public static final String DLG_IMG_HELP = "dialog_help_image"; //$NON-NLS-1$
 
 	private ExtendedPropertiesEditingContext context;
 	private FormToolkit toolkit;
 	
 	/**
 	 * @param context
 	 */
 	public BindingViewHelper(ExtendedPropertiesEditingContext context) {
 		this.context = context;
 		this.toolkit = null;
 	}
 
 	/**
 	 * @param context
 	 * @param toolkit
 	 */
 	public BindingViewHelper(ExtendedPropertiesEditingContext context, FormToolkit toolkit) {
 		this.context = context;
 		this.toolkit = toolkit;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.ui.parts.ViewHelper#createLabel(org.eclipse.swt.widgets.Composite, java.lang.Object, java.lang.String)
 	 */
 	public Label createLabel(Composite parent, Object editor, String alternate) {
 		IPropertiesEditionComponent propertiesEditingComponent = context.getPropertiesEditingComponent();
 		String text = getDescription(editor, alternate);
 		if (!text.endsWith(": ") && !text.endsWith(":")) {
 			text += ": ";
 		}
 		Label label;
 		if (toolkit != null) {
 			label = toolkit.createLabel(parent, text);
 		} else {
 			label = new Label(parent, SWT.NONE);
 			label.setText(text);
 		}
 		// Asserting that toolkit is setted => kind == Form
 		if (propertiesEditingComponent != null && propertiesEditingComponent.isRequired(editor, toolkit == null?0:1)) {
 			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
 		}
 		return label;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.ui.parts.ViewHelper#getDescription(java.lang.Object, java.lang.String)
 	 */
 	public String getDescription(Object editor, String alternate) {
 		IPropertiesEditionComponent propertiesEditingComponent = context.getPropertiesEditingComponent();
 		String text = alternate;
 		EStructuralFeature associatedFeature = propertiesEditingComponent.associatedFeature(editor);
 		if (associatedFeature != null) {
 			IItemPropertySource labelProvider = (IItemPropertySource) context.getAdapterFactory().adapt(context.getEObject(), org.eclipse.emf.edit.provider.IItemPropertySource.class);
 			if (labelProvider != null) {
				IItemPropertyDescriptor propertyDescriptor = labelProvider.getPropertyDescriptor(context.getEObject(), associatedFeature);
				if (propertyDescriptor != null) {
					text = propertyDescriptor.getDisplayName(editor);
				}
 			}
 		}
 		return text;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.ui.parts.ViewHelper#createHelpButton(org.eclipse.swt.widgets.Composite, java.lang.Object)
 	 */
 	@SuppressWarnings("unused")
 	public Control createHelpButton(Composite parent, Object feature) {
 		//To manage in future
 		String helpID = null;
 		String alternate = context.getPropertiesEditingComponent().getHelpContent(feature, toolkit == null?0:1);
 		Image image = JFaceResources.getImage(DLG_IMG_HELP);
 		if (helpID != null && !EMPTY_STRING.equals(helpID)) { //$NON-NLS-1$
 			ToolBar result = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
 			((GridLayout)parent.getLayout()).numColumns++;
 			result.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
 			ToolItem item = new ToolItem(result, SWT.NONE);
 			item.setImage(image);
 			if (alternate != null && !EMPTY_STRING.equals(alternate)) //$NON-NLS-1$
 				item.setToolTipText(alternate);
 			return result;
 		} else {
 			Label result = null; 
 			if (toolkit != null) {
 				result = toolkit.createLabel(parent, EMPTY_STRING); //$NON-NLS-1$
 			} else {
 				result = new Label(parent, SWT.NONE);
 			}
 			if (alternate != null && !EMPTY_STRING.equals(alternate)) { //$NON-NLS-1$
 				result.setImage(image);
 				result.setToolTipText(alternate);
 			}
 			return result;
 		}
 	}
 
 }
