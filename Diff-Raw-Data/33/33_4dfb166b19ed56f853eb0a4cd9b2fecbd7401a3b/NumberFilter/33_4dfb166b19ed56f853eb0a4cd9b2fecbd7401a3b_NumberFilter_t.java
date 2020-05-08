 /*******************************************************************************
  * Copyright (c) 2014 Formal Mind GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Ingo Weigelt - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.search.filter;
 
 import java.math.BigInteger;
 
 import org.eclipse.rmf.reqif10.AttributeDefinition;
 import org.eclipse.rmf.reqif10.AttributeDefinitionInteger;
 import org.eclipse.rmf.reqif10.AttributeDefinitionReal;
 import org.eclipse.rmf.reqif10.AttributeValueInteger;
 import org.eclipse.rmf.reqif10.AttributeValueReal;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10Util;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Sets;
 
 public class NumberFilter implements IFilter {
 
 	public static final ImmutableSet<Operator> SUPPORTED_OPERATORS = Sets
 			.immutableEnumSet(Operator.IS, Operator.IS_NOT,
 					Operator.BETWEEN, Operator.GREATER, Operator.SMALLER);
 	
 	private AttributeDefinition attributeDefinition;
 
 	private Operator operator;
 	private Comparable<Number> value1;
 	private Comparable<Number> value2;
 	
 	
 	
 	public NumberFilter(Operator operator, BigInteger value1, BigInteger value2, 
 			AttributeDefinitionInteger attributeDefinition) {
 		init(operator, value1, value2, attributeDefinition);
 	}
 	
 	public NumberFilter(Operator operator, Double value1, Double value2, 
 			AttributeDefinitionReal attributeDefinition) {
 		init(operator, value1, value2, attributeDefinition);
 	}
 	
 	
 	/**
 	 * 
 	 * @param operator 
 	 * @param value1 the value used for all operations. For the BETWEEN operation this is the lower value
 	 * @param value2 If operation is BETWEEN, this is the upper bound. Otherwise this parameter is ignored
 	 * @param attributeDefinition
 	 */
 	@SuppressWarnings("unchecked")
 	private void init(Operator operator, Comparable<? extends Number> value1, Comparable<? extends Number> value2, 
 			AttributeDefinition attributeDefinition) {
 		
 		// ensure that operator is supported
 		if (!SUPPORTED_OPERATORS.contains(operator)){
 			if (!SUPPORTED_OPERATORS.contains(operator)){
 				throw new IllegalArgumentException(
 						"This filter does not support the " + operator.toString()
 								+ " operation");
 			}
 		};
 		
 		// ensure that value1 is not null
 		if (null == value1){
 			throw new IllegalArgumentException(
 					"value1 can not be null");
 		}
 		
 		// ensure that value2 is not null for operations that need two parameters
 		if (operator.equals(IFilter.Operator.BETWEEN)) {
 			if (null == value2) {
 				throw new IllegalArgumentException("value2 can not be null");
 			}
 		}
 		
 		
 		this.attributeDefinition = attributeDefinition;
 		this.operator = operator;
 		this.value1 =  (Comparable<Number>) value1;
 		this.value2 =  (Comparable<Number>) value2;
 	}
 	
 
 
 	@Override
 	public boolean match(SpecElementWithAttributes element) {
 		Number theValue = getTheValue(element);
 		
 		
 		if (theValue == null){
 			/* Check if there is any default value for this attribute */
 			theValue = getDefaultValue();
 		}
 		
 		
 		if (theValue == null){
 			if (operator.equals(Operator.IS_NOT)){
 				return true;
 			}
 			return false;
 		}
	
 		
 		switch (operator) {
 		case IS:
 			return value1.compareTo(theValue) == 0;
 		case IS_NOT:
 			return value1.compareTo(theValue) != 0;
 		case BETWEEN:
 			return value1.compareTo(theValue) <= 0 && value2.compareTo(theValue) >= 0;
 		case GREATER:
 			return value1.compareTo(theValue) <= 0;
 		case SMALLER:
 			return value1.compareTo(theValue) >= 0;
 			
 		default:
 			throw new IllegalArgumentException(
 					"This filter does not support the " + this.operator
 							+ " operation");
 		}
 		
 	}
 	
 	private Number getDefaultValue() {
 		if (attributeDefinition instanceof AttributeDefinitionInteger) {
 			AttributeDefinitionInteger ad = (AttributeDefinitionInteger) attributeDefinition;
			if (!ad.isSetDefaultValue()){
				return null;
			}
			if (!ad.getDefaultValue().isSetTheValue()){
				return null;
			}
			return ad.getDefaultValue().getTheValue(); 
 		}
 		if (attributeDefinition instanceof AttributeDefinitionReal) {
 			AttributeDefinitionReal ad = (AttributeDefinitionReal) attributeDefinition;
			if (!ad.isSetDefaultValue()){
				return null;
			}
			if (!ad.getDefaultValue().isSetTheValue()){
				return null;
			}
			return ad.getDefaultValue().getTheValue();
 		}
 		throw new IllegalStateException("Expected an AttributeDefinitionInteger or AttributeDefinitionReal as attribute but found " + attributeDefinition.getClass());
 	}
 
 	private Number getTheValue(SpecElementWithAttributes element){
 		if (attributeDefinition instanceof AttributeDefinitionInteger){
 			AttributeValueInteger attributeValue = (AttributeValueInteger) ReqIF10Util.getAttributeValue(element, attributeDefinition);
			if (attributeValue == null || !attributeValue.isSetTheValue()){
				return null;
			}
			return attributeValue.getTheValue();
 		}
 		if (attributeDefinition instanceof AttributeDefinitionReal){
 			AttributeValueReal attributeValue = (AttributeValueReal) ReqIF10Util.getAttributeValue(element, attributeDefinition);
			if (attributeValue == null || !attributeValue.isSetTheValue()){
				return null;
			}
			return attributeValue.getTheValue();
 		}
 		else{
 			throw new IllegalArgumentException("SpecElementType is not supported");
 		}
 	}
 
 	@Override
 	public AttributeDefinition getAttribute() {
 		return attributeDefinition;
 	}
 
 	@Override
 	public Operator getOperator() {
 		return operator;
 	}
 
 	@Override
 	public Number getFilterValue1() {
 		return (Number) value1;
 	}
 
 	@Override
 	public Number getFilterValue2() {
 		return (Number) value2;
 	}
 
 	@Override
 	public ImmutableSet<Operator> getSupportedOperators() {
 		return SUPPORTED_OPERATORS;
 	}
 	
 	
 
 }
