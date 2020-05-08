 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    Volker Wegert - Bug 332363 - Direct Editing: enable automatic resizing for combo boxes
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.features.impl;
 
 import org.eclipse.graphiti.features.IDirectEditingFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.func.IProposalSupport;
 import org.eclipse.graphiti.internal.Messages;
 import org.eclipse.graphiti.internal.util.T;
 
 /**
  * The Class AbstractDirectEditingFeature.
  */
 public abstract class AbstractDirectEditingFeature extends AbstractFeature implements IDirectEditingFeature {
 
 	/**
 	 * The Constant EMPTY_STRING_ARRAY.
 	 */
 	protected static final String[] EMPTY_STRING_ARRAY = new String[0];
 
 	/**
 	 * Used to track if direct editing did really change anything.
 	 */
 	private boolean valueChanged = false;
 
 	/**
 	 * Creates a new {@link AbstractDirectEditingFeature}.
 	 * 
 	 * @param fp
 	 *            the fp
 	 */
 	public AbstractDirectEditingFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	public boolean canExecute(IContext context) {
 		final String SIGNATURE = "canExecute(IContext)"; //$NON-NLS-1$
 		boolean info = T.racer().info();
 		if (info) {
 			T.racer().entering(AbstractDirectEditingFeature.class, SIGNATURE, new Object[] { context });
 		}
 		boolean ret = false;
 		if (context instanceof IDirectEditingContext) {
 			ret = canDirectEdit((IDirectEditingContext) context);
 		}
 
 		if (info) {
 			T.racer().exiting(AbstractDirectEditingFeature.class, SIGNATURE, ret);
 		}
 		return ret;
 	}
 
 	public boolean canDirectEdit(IDirectEditingContext context) {
 		return true;
 	}
 
 	public void execute(IContext context) {
 		// nop
 	}
 
 	/**
 	 * Returns true only if the direct editing feature really has changed
 	 * anything. This is indicated by the execution of the feature in the
	 * DirectEditingFeatureCommandWithContext.execute method.
 	 */
 	@Override
 	public boolean hasDoneChanges() {
 		return this.valueChanged;
 	}
 
 	/**
 	 * Called by the framework (@see DirectEditingFeatureCommandWithContext) to
 	 * indicate that this direct editing feature execution has really changed
 	 * something. Only in this case there should be an entry in the undo stack
 	 * and the editor should get dirty.
 	 */
 	public final void setValueChanged() {
 		this.valueChanged = true;
 	}
 
 	public String[] getPossibleValues(IDirectEditingContext context) {
 		return EMPTY_STRING_ARRAY;
 	}
 
 	public String[] getValueProposals(String value, int caretPos, IDirectEditingContext context) {
 		return EMPTY_STRING_ARRAY;
 	}
 
 	public String checkValueValid(String value, IDirectEditingContext context) {
 		return null;
 	}
 
 	public String completeValue(String value, int caretPos, String choosenValue, IDirectEditingContext context) {
 		return choosenValue;
 	}
 
 	@Override
 	public boolean stretchFieldToFitText() {
 		return false;
 	}
 
 	public boolean isAutoCompletionEnabled() {
 		return false;
 	}
 
 	public boolean isCompletionAvailable() {
 		return false;
 	}
 
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	/**
 	 * @since 0.8
 	 */
 	@Override
 	public IProposalSupport getProposalSupport() {
 		return null;
 	}
 
 	private static final String NAME = Messages.AbstractDirectEditingFeature_0_xfld;
 
 	/**
 	 * @since 0.8
 	 */
 	@Override
 	public void setValue(String value, IDirectEditingContext context) {
 	}
 }
