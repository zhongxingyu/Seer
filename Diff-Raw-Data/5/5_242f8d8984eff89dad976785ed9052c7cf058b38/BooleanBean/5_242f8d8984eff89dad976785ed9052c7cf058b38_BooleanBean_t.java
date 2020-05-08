 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.beans.common;
 
 /**
  * Boolean bean provides a boolean value for simple adapter UI-Binding
  */
 public class BooleanBean extends AbstractBean {
 
	/**
	 * Key for the value property (PROP_VALUE = "value").
	 */
	public static final String PROP_VALUE = "value"; //$NON-NLS-1$

 	private boolean value;
 
 	/**
 	 * Creates a boolean bean with default value <code>false</code>
 	 */
 	public BooleanBean() {
 		value = false;
 	}
 
 	/**
 	 * Creates a boolean bean with the given value;
 	 * 
 	 * @param value
 	 *            The value.
 	 */
 	public BooleanBean(final boolean value) {
 		this.value = value;
 	}
 
 	/**
 	 * Creates a boolean bean with the given value;
 	 * 
 	 * @param value
 	 *            The value.
 	 */
 	public BooleanBean(final Boolean value) {
 		this.value = value;
 	}
 
 	/**
 	 * @return the value.
 	 */
 	public boolean isValue() {
 		return value;
 	}
 
 	/**
 	 * @param value
 	 *            The value to set.
 	 */
 	public void setValue(final boolean value) {
 		if (this.value != value) {
 			final boolean old = this.value;
 			this.value = value;
 			firePropertyChanged("value", old, this.value); //$NON-NLS-1$
 		}
 	}
 }
