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
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.pattern;
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.features.impl.AbstractDirectEditingFeature;
 import org.eclipse.graphiti.func.IDirectEditing;
 
 /**
  * The Class DirectEditingFeatureForPattern.
  */
 public class DirectEditingFeatureForPattern extends AbstractDirectEditingFeature {
 	private IDirectEditing delegate;
 
 	/**
 	 * Creates a new {@link DirectEditingFeatureForPattern}.
 	 * 
 	 * @param featureProvider
 	 *            the feature provider
 	 * @param pattern
 	 *            the pattern
 	 */
 	public DirectEditingFeatureForPattern(IFeatureProvider featureProvider, IDirectEditing pattern) {
 		super(featureProvider);
 		delegate = pattern;
 	}
 
 	@Override
 	public boolean canDirectEdit(IDirectEditingContext context) {
 		return delegate.canDirectEdit(context);
 	}
 
 	@Override
 	public String checkValueValid(String value, IDirectEditingContext context) {
 		return delegate.checkValueValid(value, context);
 	}
 
 	@Override
 	public String completeValue(String value, int caretPos, String choosenValue, IDirectEditingContext context) {
 		return delegate.completeValue(value, caretPos, choosenValue, context);
 	}
 
 	@Override
 	public String[] getPossibleValues(IDirectEditingContext context) {
 		return delegate.getPossibleValues(context);
 	}
 
 	@Override
 	public String[] getValueProposals(String value, int caretPos, IDirectEditingContext context) {
 		return delegate.getValueProposals(value, caretPos, context);
 	}
 
 	@Override
 	public boolean isAutoCompletionEnabled() {
 		return delegate.isAutoCompletionEnabled();
 	}
 
 	@Override
 	public boolean isCompletionAvailable() {
 		return delegate.isCompletionAvailable();
 	}
 
 	@Override
	public boolean stretchTextfieldToFitText() {
		return delegate.stretchTextfieldToFitText();
 	}
 
 	public int getEditingType() {
 		return delegate.getEditingType();
 	}
 
 	public String getInitialValue(IDirectEditingContext context) {
 		return delegate.getInitialValue(context);
 	}
 
 	public void setValue(String value, IDirectEditingContext context) {
 		delegate.setValue(value, context);
 	}
 }
