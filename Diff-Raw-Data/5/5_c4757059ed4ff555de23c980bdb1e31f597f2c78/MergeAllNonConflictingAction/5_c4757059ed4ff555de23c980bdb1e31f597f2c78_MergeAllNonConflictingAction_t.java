 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.actions;
 
import static com.google.common.base.Predicates.and;
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Lists.newArrayList;
 import static org.eclipse.emf.compare.utils.EMFComparePredicates.WITHOUT_CONFLICT;
 import static org.eclipse.emf.compare.utils.EMFComparePredicates.hasState;
 
 import java.util.List;
 
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.domain.ICompareEditingDomain;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareIDEUIMessages;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareIDEUIPlugin;
 import org.eclipse.emf.compare.internal.merge.MergeMode;
 import org.eclipse.emf.compare.merge.IMerger;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 /**
  * Abstract Action that manages a merge of a all non-conflicting difference in case of both sides of the
  * comparison are editable.
  * 
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  * @since 3.0
  */
 public class MergeAllNonConflictingAction extends MergeAction {
 
 	private Comparison comparison;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param configuration
 	 *            The compare configuration object.
 	 */
 	public MergeAllNonConflictingAction(ICompareEditingDomain editingDomain, Comparison comparison,
 			IMerger.Registry mergerRegistry, MergeMode mode, boolean isLeftEditable, boolean isRightEditable) {
 		super(editingDomain, mergerRegistry, mode, isLeftEditable, isRightEditable);
 		this.comparison = comparison;
 	}
 
 	@Override
 	protected void initToolTipAndImage(MergeMode mode) {
 		switch (mode) {
 			case LEFT_TO_RIGHT:
 				setToolTipText(EMFCompareIDEUIMessages.getString("merged.all.to.right.tooltip")); //$NON-NLS-1$
 				setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
 						EMFCompareIDEUIPlugin.PLUGIN_ID, "icons/full/toolb16/merge_all_to_right.gif")); //$NON-NLS-1$
 				break;
 			case RIGHT_TO_LEFT:
 				setToolTipText(EMFCompareIDEUIMessages.getString("merged.all.to.left.tooltip")); //$NON-NLS-1$
 				setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
 						EMFCompareIDEUIPlugin.PLUGIN_ID, "icons/full/toolb16/merge_all_to_left.gif")); //$NON-NLS-1$
 				break;
 			case ACCEPT:
 				setToolTipText(EMFCompareIDEUIMessages.getString("accept.all.changes.tooltip")); //$NON-NLS-1$
 				setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
 						EMFCompareIDEUIPlugin.PLUGIN_ID, "icons/full/toolb16/accept_all_changes.gif")); //$NON-NLS-1$
 				break;
 			case REJECT:
 				setToolTipText(EMFCompareIDEUIMessages.getString("reject.all.changes.tooltip")); //$NON-NLS-1$
 				setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
 						EMFCompareIDEUIPlugin.PLUGIN_ID, "icons/full/toolb16/reject_all_changes.gif")); //$NON-NLS-1$
 				break;
 			default:
 				throw new IllegalStateException();
 		}
 	}
 
 	public void setComparison(Comparison comparison) {
 		this.comparison = comparison;
 		clearCache();
 		// update the enablement of this action by simulating a selection change.
 		setEnabled(comparison != null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.actions.MergeAction#getDifferencesToMerge()
 	 */
 	@Override
 	protected List<Diff> getDifferencesToMerge() {
		Iterable<Diff> differences = filter(comparison.getDifferences(), and(WITHOUT_CONFLICT,
 				hasState(DifferenceState.UNRESOLVED)));
 		return newArrayList(differences);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.actions.MergeAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
 	 */
 	@Override
 	protected boolean updateSelection(IStructuredSelection selection) {
 		// this subclass does not care about the selection change event.
 		return true;
 	}
 
 }
