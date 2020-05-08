 /**
  * <copyright>
  *
  * Copyright (c) 2011 E.D.Willink and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   E.D.Willink - Initial API and implementation
  *
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.ocl.examples.domain.values.impl;
 
 import org.eclipse.ocl.examples.domain.elements.DomainElement;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.values.ElementValue;
 import org.eclipse.ocl.examples.domain.values.ValueFactory;
 
 public class DomainElementValueImpl extends AbstractObjectValue<DomainElement> implements ElementValue
 {
 	protected DomainType type = null;
 	
 	public DomainElementValueImpl(ValueFactory valueFactory, DomainElement element) {
 		super(valueFactory, element);
 	}
 
 	@Override
 	public DomainElement asElement() {
 		return object;
 	}
 
 	@Override
 	public ElementValue asElementValue() {
 		return this;
 	}
 
 	public DomainElement getElement() {
 		return object;
 	}
 
 	public DomainType getType() {
		return type;	// FIXME
 	}
 }
