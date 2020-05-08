 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.utils.coercion.coercers;
 
 import java.math.BigDecimal;
 
 
 public class BigDecimalCoercer extends AbstractCoercer {
 
 	public BigDecimal coerce(Integer i, Class<?> toType) {
 		return new BigDecimal(i);
 	}
 	
 	public BigDecimal coerce(Short s, Class<?> toType) {
 		return new BigDecimal(s);
 	}
 	
 	public BigDecimal coerce(Byte b, Class<?> toType) {
 		return new BigDecimal(b);
 	}
 	
 	public BigDecimal coerce(Long l, Class<?> toType) {
 		return new BigDecimal(l);
 	}
 	
 	public BigDecimal coerce(Double d, Class<?> toType) {
 		return new BigDecimal(d);
 	}
 	
 	public BigDecimal coerce(Float f, Class<?> toType) {
 		return new BigDecimal(f);
 	}
 	
 	public BigDecimal coerce(String string, Class<?> toType) {
		String value = string.trim();
		return "".equals(value) ? null : new BigDecimal(value);
 	}

 	@Override
 	public Class<?> getType() {
 		return BigDecimal.class;
 	}
 
 }
