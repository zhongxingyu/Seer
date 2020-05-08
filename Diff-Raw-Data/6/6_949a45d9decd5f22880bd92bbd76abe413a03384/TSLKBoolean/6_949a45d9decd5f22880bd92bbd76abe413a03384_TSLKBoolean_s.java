 /*******************************************************************************
  * Copyright (c) 2013 Nick Guletskii.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Nick Guletskii - initial API and implementation
  ******************************************************************************/
 package org.ng200.tslk.lang.runtime;
 
 import java.math.BigDecimal;
 
 import org.ng200.tslk.lang.runtime.exceptions.TSLKRuntimeException;
 import org.ng200.tslk.lang.runtime.exceptions.TSLKTypeMismatchException;
 
 public class TSLKBoolean extends TSLKObject {
 
 	private boolean value;
 
 	public TSLKBoolean(TSLKInstance instance, boolean value) {
 		super(instance, Type.BOOLEAN);
 		this.value = value;
 	}
 
 	@Override
 	public TSLKObject add(TSLKObject obj) {
 		throw new TSLKTypeMismatchException(
 				"Can't perform arithmetic operations on a boolean and a "
 						+ obj.getType().name().toLowerCase() + "!");
 	}
 
 	@Override
 	public TSLKObject and(TSLKObject obj) {
 		if (obj instanceof TSLKBoolean)
 			return new TSLKBoolean(getTSLKInstance(), value
 					&& ((TSLKBoolean) obj).getValue());
 		if (obj instanceof TSLKNumber)
 			return new TSLKBoolean(getTSLKInstance(), value
 					&& !((TSLKNumber) obj).getValue().equals(BigDecimal.ZERO));
 		throw new TSLKTypeMismatchException(
 				"Can't perform binary operations on a boolean and a "
 						+ obj.getType().name().toLowerCase() + "!");
 	}
 
 	@Override
 	public TSLKObject divide(TSLKObject obj) {
 		throw new TSLKTypeMismatchException(
 				"Can't perform arithmetic operations on a boolean and a "
 						+ obj.getType().name().toLowerCase() + "!");
 	}
 
 	@Override
 	public boolean equals(TSLKObject obj) {
 		if (obj == null)
 			return false;
		return obj.toString().equals(value ? "1" : "0")
				|| (obj instanceof TSLKNumber && ((TSLKNumber) obj).getValue()
						.equals(BigDecimal.ZERO) ^ this.value)
 				|| obj.toString().equals(this.toString());
 	}
 
 	@Override
 	public TSLKObject exponentiate(TSLKObject obj) {
 		throw new TSLKTypeMismatchException(
 				"Can't perform arithmetic operations on a boolean and a "
 						+ obj.getType().name().toLowerCase() + "!");
 	}
 
 	@Override
 	public TSLKObject getAtIndex(TSLKObject obj) {
 		throw new TSLKRuntimeException("Can't index a boolean!");
 	}
 
 	public boolean getValue() {
 		return value;
 	}
 
 	@Override
 	public int hashCode() {
 		return ((Boolean) value).hashCode();
 	}
 
 	@Override
 	public boolean isLess(TSLKObject obj) {
 		throw new TSLKTypeMismatchException(
 				"Can't do arithmetic comparison with a boolean!");
 	}
 
 	@Override
 	public boolean isLessOrEqual(TSLKObject obj) {
 		throw new TSLKTypeMismatchException(
 				"Can't do arithmetic comparison with a boolean!");
 	}
 
 	@Override
 	public boolean isMore(TSLKObject obj) {
 		throw new TSLKTypeMismatchException(
 				"Can't do arithmetic comparison with a boolean!");
 	}
 
 	@Override
 	public boolean isMoreOrEqual(TSLKObject obj) {
 		throw new TSLKTypeMismatchException(
 				"Can't do arithmetic comparison with a boolean!");
 	}
 
 	@Override
 	public TSLKObject length() {
 		throw new TSLKTypeMismatchException("Booleans don't have length!");
 	}
 
 	@Override
 	public TSLKObject minus() {
 		throw new TSLKTypeMismatchException("Can't invert a boolean!");
 	}
 
 	@Override
 	public TSLKObject multiply(TSLKObject obj) {
 		throw new TSLKTypeMismatchException(
 				"Can't perform arithmetic operations on a boolean and a "
 						+ obj.getType().name().toLowerCase() + "!");
 	}
 
 	@Override
 	public TSLKObject not() {
 		return new TSLKBoolean(getTSLKInstance(), !value);
 	}
 
 	@Override
 	public TSLKObject or(TSLKObject obj) {
 		if (obj instanceof TSLKBoolean)
 			return new TSLKBoolean(getTSLKInstance(), value
 					|| ((TSLKBoolean) obj).getValue());
 		if (obj instanceof TSLKNumber)
 			return new TSLKBoolean(getTSLKInstance(), value
 					|| !((TSLKNumber) obj).getValue().equals(BigDecimal.ZERO));
 		throw new TSLKTypeMismatchException(
 				"Can't perform binary operations on a boolean and a "
 						+ obj.getType().name().toLowerCase() + "!");
 	}
 
 	@Override
 	public TSLKObject remainder(TSLKObject obj) {
 		throw new TSLKTypeMismatchException(
 				"Can't perform arithmetic operations on a boolean and a "
 						+ obj.getType().name().toLowerCase() + "!");
 	}
 
 	@Override
 	public void setAtIndex(TSLKObject index, TSLKObject value) {
 		throw new TSLKRuntimeException("Can't index a boolean!");
 
 	}
 
 	@Override
 	public TSLKObject subtract(TSLKObject obj) {
 		throw new TSLKTypeMismatchException(
 				"Can't perform arithmetic operations on a boolean and a "
 						+ obj.getType().name().toLowerCase() + "!");
 	}
 
 	@Override
 	public String toString() {
 		return value ? "true" : "false";
 	}
 }
