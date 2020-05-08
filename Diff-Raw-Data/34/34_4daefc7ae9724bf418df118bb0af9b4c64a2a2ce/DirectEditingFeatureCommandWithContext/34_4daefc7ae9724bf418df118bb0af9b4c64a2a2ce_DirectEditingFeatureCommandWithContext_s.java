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
 package org.eclipse.graphiti.internal.command;
 
 import org.eclipse.graphiti.features.IDirectEditingFeature;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.features.impl.AbstractDirectEditingFeature;
 
 /**
  * The Class DirectEditingFeatureCommandWithContext.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class DirectEditingFeatureCommandWithContext extends GenericFeatureCommandWithContext {
 
 	private String newValue;
 
 	/**
 	 * Instantiates a new direct editing feature command with context.
 	 * 
 	 * @param feature
 	 *            the feature
 	 * @param context
 	 *            the context
 	 * @param valueObject
 	 *            the value object
 	 */
	public DirectEditingFeatureCommandWithContext(IDirectEditingFeature feature, IDirectEditingContext context, String valueObject) {
 		super(feature, context);
 		setNewValue(valueObject);
 	}
 
 	private String getNewValue() {
 		return newValue;
 	}
 
 	private void setNewValue(String newValue) {
 		this.newValue = newValue;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext
 	 * #execute()
 	 */
 	@Override
 	public boolean execute() {
 		boolean ret = false;
 		if (getFeature() instanceof IDirectEditingFeature && getContext() instanceof IDirectEditingContext) {
 			IDirectEditingFeature def = (IDirectEditingFeature) getFeature();
 			IDirectEditingContext dec = (IDirectEditingContext) getContext();
 			String initialValue = def.getInitialValue(dec);
 			if (initialValue == null) {
 				initialValue = ""; //$NON-NLS-1$
 			}
 			if (!initialValue.equals(getNewValue())) {
				def.setValue(getNewValue(), dec);
 				ret = true;
 				// Notify the feature that there really are changes
 				if (getFeature() instanceof AbstractDirectEditingFeature) {
 					((AbstractDirectEditingFeature) getFeature()).setValueChanged();
 				}
 			}
 		}
 		return ret;
 	}
 
 }
