 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.util.beans;
 
 /**
  * A simple bean.
  */
 public class TestBean {
 
 	/**
 	 * The name of the 'property' property of the bean.
 	 */
	public static final String PROPERTY = "property";
 
 	private Object property;
 
 	/**
 	 * @return The property.
 	 */
 	public Object getProperty() {
 		return property;
 	}
 
 	/**
 	 * Sets the property.
 	 * 
 	 * @param property
 	 *            The new property value.
 	 */
 	public void setProperty(Object property) {
 		this.property = property;
 	}

 }
