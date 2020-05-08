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
 package org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.impl;
 
 import static com.google.common.base.Predicates.instanceOf;
 import static com.google.common.base.Predicates.or;
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Iterables.getFirst;
 
 import com.google.common.collect.ImmutableList;
 
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.compare.ResourceAttachmentChange;
 import org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.impl.TypeConstants;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.IMergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.IMergeViewerItem;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.MergeViewerItem;
 import org.eclipse.emf.compare.rcp.ui.internal.util.MergeViewerUtil;
 import org.eclipse.emf.compare.utils.EMFComparePredicates;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class ContainmentReferenceChangeAccessorImpl extends AbstractStructuralFeatureAccessor {
 
 	/**
 	 * 
 	 */
 	public ContainmentReferenceChangeAccessorImpl(AdapterFactory adapterFactory,
 			ReferenceChange referenceChange, MergeViewerSide side) {
 		super(adapterFactory, referenceChange, side);
 	}
 
 	/**
 	 * @return
 	 */
 	@Override
 	protected ImmutableList<Diff> computeDifferences() {
 		List<Diff> allDifferences = getComparison().getDifferences();
 		return ImmutableList.<Diff> copyOf(filter(filter(allDifferences, ReferenceChange.class),
 				or(EMFComparePredicates.CONTAINMENT_REFERENCE_CHANGE,
 						instanceOf(ResourceAttachmentChange.class))));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.mergeviewer.accessor.ICompareAccessor#getInitialItem()
 	 */
 	@Override
 	public IMergeViewerItem getInitialItem() {
 		Diff initialDiff = getInitialDiff();
 		EObject diffValue = (EObject)MergeViewerUtil.getDiffValue(initialDiff);
 		Match match = getComparison().getMatch(diffValue);
 
		if (match != null) {
			return new MergeViewerItem.Container(getComparison(), getInitialDiff(), match, getSide(),
					getAdapterFactory());
		}
		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.mergeviewer.accessor.ICompareAccessor#getItems()
 	 */
 	public ImmutableList<? extends IMergeViewerItem> getItems() {
 		final ImmutableList.Builder<IMergeViewerItem> ret = ImmutableList.builder();
 
 		EList<Match> matches = getComparison().getMatches();
 		for (Match match : matches) {
 			ResourceAttachmentChange diff = getFirst(filter(match.getDifferences(),
 					ResourceAttachmentChange.class), null);
 			ret.add(new MergeViewerItem.Container(getComparison(), diff, match.getLeft(), match.getRight(),
 					match.getOrigin(), getSide(), getAdapterFactory()));
 		}
 
 		return ret.build();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement#getType()
 	 */
 	@Override
 	public String getType() {
 		return TypeConstants.TYPE__ETREE_DIFF;
 	}
 }
