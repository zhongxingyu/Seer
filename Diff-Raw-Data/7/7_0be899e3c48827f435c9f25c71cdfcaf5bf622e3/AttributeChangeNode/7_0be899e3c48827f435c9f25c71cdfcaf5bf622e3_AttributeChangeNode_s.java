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
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.provider;
 
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.AttributeChange;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.IMergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.provider.AttributeChangeAccessor;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.provider.StringAttributeChangeAccessor;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * Specific AbstractEDiffNode for {@link AttributeChange} objects.
  * 
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class AttributeChangeNode extends DiffNode {
 
 	/**
 	 * Creates a node with the given factory.
 	 * 
 	 * @param adapterFactory
 	 *            the factory given to the super constructor.
 	 */
 	public AttributeChangeNode(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.provider.DiffNode#getTarget()
 	 */
 	@Override
 	public AttributeChange getTarget() {
 		return (AttributeChange)super.getTarget();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.AbstractEDiffElement#getType()
 	 */
 	@Override
 	public String getType() {
 		return ITypedElement.TEXT_TYPE;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.AbstractEDiffNode#getAncestor()
 	 */
 	@Override
 	public ITypedElement getAncestor() {
 		ITypedElement ret = null;
 		EAttribute attribute = getTarget().getAttribute();
 		EObject origin = getTarget().getMatch().getOrigin();
 		if (origin != null) {
 			if (attribute.getEAttributeType().getInstanceClass() == String.class && !attribute.isMany()) {
 				ret = new StringAttributeChangeAccessor(origin, attribute, getTarget());
 			} else {
 				ret = new AttributeChangeAccessor(getTarget(), MergeViewerSide.ANCESTOR);
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.AbstractEDiffNode#getLeft()
 	 */
 	@Override
 	public ITypedElement getLeft() {
 		ITypedElement ret = null;
 		EAttribute attribute = getTarget().getAttribute();
 		EObject left = getTarget().getMatch().getLeft();
 		if (left != null) {
 			if (attribute.getEAttributeType().getInstanceClass() == String.class && !attribute.isMany()) {
 				ret = new StringAttributeChangeAccessor(left, attribute, getTarget());
 			} else {
				ret = new AttributeChangeAccessor(getTarget(), MergeViewerSide.ANCESTOR);
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.AbstractEDiffNode#getRight()
 	 */
 	@Override
 	public ITypedElement getRight() {
 		ITypedElement ret = null;
 		EAttribute attribute = getTarget().getAttribute();
 		EObject right = getTarget().getMatch().getRight();
 		if (right != null) {
 			if (attribute.getEAttributeType().getInstanceClass() == String.class && !attribute.isMany()) {
 				ret = new StringAttributeChangeAccessor(right, attribute, getTarget());
 			} else {
				ret = new AttributeChangeAccessor(getTarget(), MergeViewerSide.ANCESTOR);
 			}
 		}
 		return ret;
 	}

 }
