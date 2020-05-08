 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.provider;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 import org.eclipse.compare.IEditableContent;
 import org.eclipse.compare.IStreamContentAccessor;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.compare.AttributeChange;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.provider.EcoreEditPlugin;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * A {@link ITypedElement} that can be used as input of TextMergeViewer. The returned content is the value of
  * the given {@link EAttribute} on the given {@link EObject}.
  * 
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class StringAttributeChangeAccessor implements ITypedElement, IStreamContentAccessor, IEditableContent {
 
 	/**
 	 * The EObject to get the value of the EAttribute from.
 	 */
 	private final EObject fEObject;
 
 	/**
 	 * The EAttribute to retrieve from the wrapped EObject.
 	 */
 	private final EAttribute fEAtribute;
 
 	private final AttributeChange fAttributeChange;
 
 	/**
 	 * Creates a new object for the given <code>eObject</code> and <code>eAttribute</code>.
 	 * 
 	 * @param eObject
 	 *            The EObject to get the value of the EAttribute from
 	 * @param eAtribute
 	 *            The EAttribute to retrieve from the wrapped EObject
 	 * @param attributeChange
 	 */
 	public StringAttributeChangeAccessor(EObject eObject, EAttribute eAtribute,
 			AttributeChange attributeChange) {
 		this.fEObject = eObject;
 		this.fEAtribute = eAtribute;
 		this.fAttributeChange = attributeChange;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.ITypedElement#getName()
 	 */
 	public String getName() {
 		return this.fEAtribute.getName();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.ITypedElement#getImage()
 	 */
 	public Image getImage() {
 		return ExtendedImageRegistry.getInstance().getImage(
 				EcoreEditPlugin.getPlugin().getImage("full/obj16/EAttribute")); //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.ITypedElement#getType()
 	 */
 	public String getType() {
 		return "eText"; //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.IStreamContentAccessor#getContents()
 	 */
 	public InputStream getContents() throws CoreException {
 		Object value = fEObject.eGet(fEAtribute);
 		String stringValue = EcoreUtil.convertToString(fEAtribute.getEAttributeType(), value);
		if (stringValue != null) {
			return new ByteArrayInputStream(stringValue.getBytes());
		} else {
			return new ByteArrayInputStream(new byte[0]);
		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.IEditableContent#isEditable()
 	 */
 	public boolean isEditable() {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.IEditableContent#setContent(byte[])
 	 */
 	public void setContent(byte[] newContent) {
 		throw new UnsupportedOperationException(
 				"ITypedElement StringAttributeChangeAccessor#replace(ITypedElement, ITypedElement)"); //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.IEditableContent#replace(org.eclipse.compare.ITypedElement,
 	 *      org.eclipse.compare.ITypedElement)
 	 */
 	public ITypedElement replace(ITypedElement dest, ITypedElement src) {
 		throw new UnsupportedOperationException(
 				"ITypedElement StringAttributeChangeAccessor#replace(ITypedElement, ITypedElement)"); //$NON-NLS-1$
 	}
 }
