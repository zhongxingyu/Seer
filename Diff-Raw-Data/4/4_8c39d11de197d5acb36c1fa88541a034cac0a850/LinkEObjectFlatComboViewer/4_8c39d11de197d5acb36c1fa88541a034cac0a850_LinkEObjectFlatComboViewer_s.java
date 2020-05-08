 /*******************************************************************************
  * Copyright (c) 2008, 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.widgets;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.eef.runtime.ui.utils.EditingUtils;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Link;
 
 /**
  * AdvancedEObjectFlatComboViewer with a link to set the element properties.
  * 
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lpine</a>
  */
 public class LinkEObjectFlatComboViewer extends AbstractAdvancedEObjectFlatComboViewer {
 
 	/** Associated link. */
 	protected Link valueLink;
 	
 	/**
 	 * Constructor from super class
 	 * 
 	 * @param dialogTitle
 	 * @param input Object
 	 * @param filter ViewerFilter
 	 * @param adapterFactory AdapterFactory
 	 * @param callback EObjectFlatComboViewerListener
 	 */
 	public LinkEObjectFlatComboViewer(String dialogTitle, Object input,
 			ViewerFilter filter, AdapterFactory adapterFactory,
 			EObjectFlatComboViewerListener callback) {
 		super(dialogTitle, input, filter, adapterFactory, callback);
 	}
 
 	/** (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.AbstractAdvancedEObjectFlatComboViewer#createLabels(org.eclipse.swt.widgets.Composite)
 	 */
 	protected void createLabels(Composite parent) {
 		String value = UNDEFINED_VALUE;
 		if (selection != null) {
 			value = labelProvider.getText(selection);
 		}
 		this.valueLink = createLink(parent, value, SWT.NONE);
 		FormData data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(browseButton, 0);
		data.top = new FormAttachment(0, 1);
		valueLink.setLayoutData(data);
 		valueLink.addSelectionListener(new SelectionAdapter() {
 			/** (non-Javadoc)
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				EObject editedElement = getSelection();
 				handleEdit(editedElement);
 			}
 		});
 	}
 
 	/**
 	 * @param parent Composite
 	 * @param value String
 	 * @param style
 	 * @return the created Link
 	 */
 	private Link createLink(Composite parent, String value, int style) {
 		Link link = new Link(parent, style);
 		link.setText(value);
 		EditingUtils.setEEFtype(field, "eef::LinkEObjectFlatComboViewer::link");
 		return link;
 	}
 
 	/** (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.AbstractAdvancedEObjectFlatComboViewer#setSelection(org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setSelection(ISelection selection) {
 		if (selection instanceof StructuredSelection) {
 			StructuredSelection structuredSelection = (StructuredSelection) selection;
 			if (!structuredSelection.isEmpty()
 					&& !"".equals(structuredSelection.getFirstElement())) {
 				setSelection((EObject) structuredSelection.getFirstElement());
 			} else {
 				this.valueLink.setText(UNDEFINED_VALUE);
 			}
 		}
 	}
 
 	/** (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.AbstractAdvancedEObjectFlatComboViewer#setSelection(org.eclipse.emf.ecore.EObject)
 	 */
 	public void setSelection(EObject selection) {
 		this.selection = selection;
 		String text = labelProvider.getText(selection);
 		if ("".equals(text)) //$NON-NLS-1$
 			this.valueLink.setText(UNDEFINED_VALUE);
 		else
 			this.valueLink.setText("<a>" + text + "</a>");
 	}
 	
 	/** (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.AbstractAdvancedEObjectFlatComboViewer#setID(java.lang.Object)
 	 */
 	public void setID(Object id) {
 		super.setID(id);
 		EditingUtils.setID(valueLink, id);
 	}
 
 	/** (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.AbstractAdvancedEObjectFlatComboViewer#setEnabled(boolean)
 	 */
 	public void setEnabled(boolean enabled) {
 		super.setEnabled(enabled);
 		valueLink.setEnabled(enabled);
 	}
 	
 	/** (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.AbstractAdvancedEObjectFlatComboViewer#isEnabled()
 	 */
 	public boolean isEnabled() {
 		return super.isEnabled() && valueLink.isEnabled();
 	}
 
 	/** (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.AbstractAdvancedEObjectFlatComboViewer#setToolTipText(java.lang.String)
 	 */
 	public void setToolTipText(String tooltip) {
 		super.setToolTipText(tooltip);
 		valueLink.setToolTipText(tooltip);
 	}
 
 }
