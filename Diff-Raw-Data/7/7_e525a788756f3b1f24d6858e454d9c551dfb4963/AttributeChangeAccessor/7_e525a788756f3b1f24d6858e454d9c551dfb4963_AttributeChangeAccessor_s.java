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
 
 import static com.google.common.collect.Iterables.concat;
 import static com.google.common.collect.Iterables.filter;
 
 import org.eclipse.emf.compare.AttributeChange;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.IMergeViewer.MergeViewerSide;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.provider.EcoreEditPlugin;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class AttributeChangeAccessor extends AbstractDiffAccessor {
 
 	public AttributeChangeAccessor(AttributeChange attributeChange, MergeViewerSide side) {
 		super(attributeChange, side);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.provider.IStructuralFeatureAccessor#getValue(org.eclipse.emf.compare.Diff)
 	 */
 	public Object getValue(Diff diff) {
		final Match match = getMatch(diff);
 		final Object value;
 		switch (getSide()) {
 			case ANCESTOR:
 				value = match.getOrigin().eGet(getEStructuralFeature());
 				break;
 			case LEFT:
 				value = match.getLeft().eGet(getEStructuralFeature());
 				break;
 			case RIGHT:
 				value = match.getRight().eGet(getEStructuralFeature());
 				break;
 			default:
 				throw new IllegalStateException();
 		}
 		String toString = EcoreUtil.convertToString(
 				((EAttribute)getEStructuralFeature()).getEAttributeType(), value);
 		return toString;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.provider.IStructuralFeatureAccessor#getDiff(java.lang.Object)
 	 */
 	public Diff getDiff(Object value, MergeViewerSide side) {
 		Diff ret = null;
 		Iterable<AttributeChange> attributeChanges = filter(concat(getDiffFromThisSide(),
				getDiffFromTheOtherSide()), AttributeChange.class);
 		for (AttributeChange attributeChange : attributeChanges) {
 			Object attributeChangeValue = attributeChange.getValue();
 			if (attributeChangeValue == value) {
 				ret = attributeChange;
 				break;
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.provider.IStructuralFeatureAccessor#getMatch()
 	 */
 	public Match getMatch() {
 		return getMatch(getDiff());
 	}
 
 	private Match getMatch(Diff diff) {
 		final Match match = diff.getMatch();
 		return match;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.ITypedElement#getName()
 	 */
 	public String getName() {
 		return AttributeChangeAccessor.class.getName();
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
 		return ContentMergeViewerConstants.REFERENCE_CHANGE_NODE_TYPE;
 	}
 
 }
