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
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.ETypedElement;
 import org.eclipse.ocl.examples.domain.elements.DomainClassifierType;
 import org.eclipse.ocl.examples.domain.elements.DomainCollectionType;
 import org.eclipse.ocl.examples.domain.elements.DomainElement;
 import org.eclipse.ocl.examples.domain.elements.DomainStandardLibrary;
 import org.eclipse.ocl.examples.domain.elements.DomainTupleType;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.elements.DomainTypedElement;
 import org.eclipse.ocl.examples.domain.evaluation.InvalidValueException;
 import org.eclipse.ocl.examples.domain.messages.EvaluatorMessages;
 import org.eclipse.ocl.examples.domain.values.Bag;
 import org.eclipse.ocl.examples.domain.values.BagValue;
 import org.eclipse.ocl.examples.domain.values.BooleanValue;
 import org.eclipse.ocl.examples.domain.values.CollectionValue;
 import org.eclipse.ocl.examples.domain.values.ElementValue;
 import org.eclipse.ocl.examples.domain.values.IntegerRange;
 import org.eclipse.ocl.examples.domain.values.IntegerValue;
 import org.eclipse.ocl.examples.domain.values.InvalidValue;
 import org.eclipse.ocl.examples.domain.values.NullValue;
 import org.eclipse.ocl.examples.domain.values.ObjectValue;
 import org.eclipse.ocl.examples.domain.values.OrderedSet;
 import org.eclipse.ocl.examples.domain.values.OrderedSetValue;
 import org.eclipse.ocl.examples.domain.values.RealValue;
 import org.eclipse.ocl.examples.domain.values.SequenceValue;
 import org.eclipse.ocl.examples.domain.values.SetValue;
 import org.eclipse.ocl.examples.domain.values.StringValue;
 import org.eclipse.ocl.examples.domain.values.TupleValue;
 import org.eclipse.ocl.examples.domain.values.TypeValue;
 import org.eclipse.ocl.examples.domain.values.UnlimitedValue;
 import org.eclipse.ocl.examples.domain.values.Value;
 import org.eclipse.ocl.examples.domain.values.ValueFactory;
 import org.eclipse.osgi.util.NLS;
 
 public abstract class AbstractValueFactory implements ValueFactory
 {
 	public static final BigInteger INTEGER_MAX_VALUE = BigInteger.valueOf(Integer.MAX_VALUE);
 	public static final BigInteger INTEGER_MIN_VALUE = BigInteger.valueOf(Integer.MIN_VALUE);
 	public static final BigInteger LONG_MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);
 	public static final BigInteger LONG_MIN_VALUE = BigInteger.valueOf(Long.MIN_VALUE);
 	private static final String maxLongValue = Long.toString(Long.MAX_VALUE);
 	private static final int maxLongSize = maxLongValue.length();	
 
 	private BooleanValue falseValue; 
 	private InvalidValue invalidValue; 
 	private NullValue nullValue; 
 	private IntegerValue oneValue;
 	private BooleanValue trueValue; 
 	private UnlimitedValue unlimitedValue; 
 	private IntegerValue zeroValue;
 
 	protected final DomainStandardLibrary standardLibrary;
 
 	public AbstractValueFactory(DomainStandardLibrary standardLibrary) {
 		this.standardLibrary = standardLibrary;
 	}
 
 	public BooleanValue booleanValueOf(boolean value) {
 		return value ? getTrue() : getFalse();
 	}
 
     public BagValue createBagOf(Object... objects) {
     	Bag<Value> collection = new BagImpl<Value>();
     	if (objects != null) {
     		for (Object object : objects) {
  				collection.add(valueOf(object));
     		}
     	}
     	DomainType elementType = getElementType(collection);
     	return createBagValue(standardLibrary.getBagType(elementType), collection);
     }
 
     public BagValue createBagOf(Iterable<?> objects) {
     	Bag<Value> collection = new BagImpl<Value>();
     	if (objects != null) {
     		for (Object object : objects) {
  				collection.add(valueOf(object));
     		}
     	}
     	DomainType elementType = getElementType(collection);
     	return createBagValue(standardLibrary.getBagType(elementType), collection);
     }
 	
 	public BagValue createBagValue(DomainCollectionType type, Value... values) {
 		return new BagValueImpl(this, type, values);
 	}
 
 	public BagValue createBagValue(DomainCollectionType type, Bag<? extends Value> values) {
 		return new BagValueImpl(this, type, values);
 	}
 
 	public BagValue createBagValue(DomainCollectionType type, Collection<? extends Value> values) {
 		return new BagValueImpl(this, type, values);
 	}
 
 	public BagValue createBagValue(Value... values) {
 		DomainType elementType = getElementType(values);
 		DomainCollectionType collectionType = standardLibrary.getBagType(elementType);
 		return new BagValueImpl(this, collectionType, values);
 	}
 
 	public BooleanValue.Accumulator createBooleanAccumulatorValue() {
 		return new BooleanValueImpl.Accumulator(this, false);
 	}
 
 	public CollectionValue.Accumulator createCollectionAccumulatorValue(DomainCollectionType type) {
 		DomainStandardLibrary standardLibrary = getStandardLibrary();
 		if (type instanceof DomainCollectionType) {
 			DomainCollectionType collectionValueType = (DomainCollectionType)type;
 			boolean isOrdered = collectionValueType.isOrdered();
 			boolean isUnique = collectionValueType.isUnique();
 			if (isOrdered) {
 				if (isUnique) {
 					return new OrderedSetValueImpl.Accumulator(this, collectionValueType);
 				}
 				else {
 					return new SequenceValueImpl.Accumulator(this, collectionValueType);
 				}
 			}
 			else {
 				if (isUnique) {
 					return new SetValueImpl.Accumulator(this, collectionValueType);
 				}
 				else {
 					return new BagValueImpl.Accumulator(this, collectionValueType);
 				}
 			}
 		}
 		else {
 			return new SetValueImpl.Accumulator(this, standardLibrary.getSetType(type));		// WIP used by "any"
 		}
 	}	
 	
 	/**
 	 * Creates a new OCL <tt>Collection</tt> of the specified ordering and uniqueness.
      * 
 	 * @param isOrdered the required collection ordering
 	 * @param isUnique the required collection uniqueness
 	 * @param values the required collection contents
 	 * @return the new collection
 	 */
 	public CollectionValue createCollectionValue(boolean isOrdered, boolean isUnique, Value... values) {
 		return createCollectionValue(isOrdered, isUnique, getElementType(values), values);
 	}
 
 	public CollectionValue createCollectionValue(boolean isOrdered, boolean isUnique, Collection<Value> values) {
 		return createCollectionValue(isOrdered, isUnique, getElementType(values), values);
 	}
 
 	public CollectionValue createCollectionValue(boolean isOrdered, boolean isUnique, DomainType elementType, Value... values) {
 		if (isOrdered) {
 			if (isUnique) {
 				return createOrderedSetValue(standardLibrary.getOrderedSetType(elementType), values);
 			}
 			else {
 				return createSequenceValue(standardLibrary.getSequenceType(elementType), values);
 			}
 		}
 		else {
 			if (isUnique) {
 				return createSetValue(standardLibrary.getSetType(elementType), values);
 			}
 			else {
 				return createBagValue(standardLibrary.getBagType(elementType), values);
 			}
 		}
 	}
    
 	/**
 	 * Creates a new OCL <tt>Collection</tt> of the specified ordering and uniqueness.
      * 
 	 * @param isOrdered the required collection ordering
 	 * @param isUnique the required collection uniqueness
 	 * @param values the required collection contents
 	 * @return the new collection
 	 */
 	public CollectionValue createCollectionValue(boolean isOrdered, boolean isUnique, DomainType elementType, Collection<Value> values) {
 		if (isOrdered) {
 			if (isUnique) {
 				return createOrderedSetValue(standardLibrary.getOrderedSetType(elementType), values);
 			}
 			else {
 				return createSequenceValue(standardLibrary.getSequenceType(elementType), values);
 			}
 		}
 		else {
 			if (isUnique) {
 				return createSetValue(standardLibrary.getSetType(elementType), values);
 			}
 			else {
 				return createBagValue(standardLibrary.getBagType(elementType), values);
 			}
 		}
 	}
 
     public OrderedSetValue createOrderedSetOf(Object... objects) {
     	OrderedSet<Value> collection = new OrderedSetImpl<Value>();
     	if (objects != null) {
     		for (Object object : objects) {
  				collection.add(valueOf(object));
     		}
     	}
     	DomainType elementType = getElementType(collection);
     	return createOrderedSetValue(standardLibrary.getOrderedSetType(elementType), collection);
     }
 
 	public ObjectValue createEObjectValue(EObject eObject) {
 		if (eObject instanceof DomainElement) {
 			if (eObject instanceof DomainClassifierType) {
 				return createTypeValue((DomainClassifierType) eObject);
 			}
 			return createElementValue((DomainElement) eObject);
 		}
		return null;
 	}
 
 	public ElementValue createElementValue(DomainElement element) {
 		return new DomainElementValueImpl(this, element);
 	}
 
 	public ObjectValue createObjectValue(Object object) {
 		return new ObjectValueImpl(this, object);
 	}
 
     public OrderedSetValue createOrderedSetOf(Iterable<?> objects) {
     	OrderedSet<Value> collection = new OrderedSetImpl<Value>();
     	if (objects != null) {
     		for (Object object : objects) {
  				collection.add(valueOf(object));
     		}
     	}
     	DomainType elementType = getElementType(collection);
     	return createOrderedSetValue(standardLibrary.getOrderedSetType(elementType), collection);
     }
 
 	public OrderedSetValue createOrderedSetValue(DomainCollectionType type, Value... values) {
 		return new OrderedSetValueImpl(this, type, values);
 	}
 
 	public OrderedSetValue createOrderedSetValue(DomainCollectionType type, OrderedSet<? extends Value> values) {
 		return new OrderedSetValueImpl(this, type, values);
 	}
 
 	public OrderedSetValue createOrderedSetValue(DomainCollectionType type, Collection<? extends Value> values) {
 		return new OrderedSetValueImpl(this, type, values);
 	}
 
 	public OrderedSetValue createOrderedSetValue(Value... values) {
 		DomainType elementType = getElementType(values);
 		DomainCollectionType collectionType = standardLibrary.getOrderedSetType(elementType);
 		return new OrderedSetValueImpl(this, collectionType, values);
 	}
 
     public SequenceValue createSequenceOf(Object... objects) {
     	List<Value> collection = new ArrayList<Value>();
     	if (objects != null) {
     		for (Object object : objects) {
  				collection.add(valueOf(object));
     		}
     	}
     	DomainType elementType = getElementType(collection);
     	return createSequenceValue(standardLibrary.getSequenceType(elementType), collection);
     }
 
     public SequenceValue createSequenceOf(Iterable<?> objects) {
     	List<Value> collection = new ArrayList<Value>();
     	if (objects != null) {
     		for (Object object : objects) {
  				collection.add(valueOf(object));
     		}
     	}
     	DomainType elementType = getElementType(collection);
     	return createSequenceValue(standardLibrary.getSequenceType(elementType), collection);
     }
 
 	public IntegerRange createRange(IntegerValue firstInteger, IntegerValue lastInteger) throws InvalidValueException {
 		return new IntegerRangeImpl(firstInteger, lastInteger);
 	}
 
 	public SequenceValue createSequenceAccumulatorValue(DomainCollectionType type, List<Value> values) {
 		return new SequenceValueImpl.Accumulator(this, type, values);
 	}
 
 	public SequenceValue createSequenceRange(DomainCollectionType type, IntegerRange range) {
 		return new SequenceRangeImpl(this, type, range);
 	}
 
 	public SequenceValue createSequenceValue(DomainCollectionType type, Value... values) {
 		return new SequenceValueImpl(this, type, values);
 	}
 
 	public SequenceValue createSequenceValue(DomainCollectionType type, List<? extends Value> values) {
 		return new SequenceValueImpl(this, type, values);
 	}
 
 	public SequenceValue createSequenceValue(DomainCollectionType type, Collection<? extends Value> values) {
 		return new SequenceValueImpl(this, type, values);
 	}
 
 	public SequenceValue createSequenceValue(Value... values) {
 		DomainType elementType = getElementType(values);
 		DomainCollectionType collectionType = standardLibrary.getSequenceType(elementType);
 		return new SequenceValueImpl(this, collectionType, values);
 	}
 
     public SetValue createSetOf(Object... objects) {
     	Set<Value> collection = new HashSet<Value>();
     	if (objects != null) {
     		for (Object object : objects) {
  				collection.add(valueOf(object));
     		}
     	}
     	DomainType elementType = getElementType(collection);
     	return createSetValue(standardLibrary.getSetType(elementType), collection);
     }
 
     public SetValue createSetOf(Iterable<?> objects) {
     	Set<Value> collection = new HashSet<Value>();
     	if (objects != null) {
     		for (Object object : objects) {
  				collection.add(valueOf(object));
     		}
     	}
     	DomainType elementType = getElementType(collection);
     	return createSetValue(standardLibrary.getSetType(elementType), collection);
     }
 
 	public SetValue createSetValue(DomainCollectionType type, Value... values) {
 		return new SetValueImpl(this, type, values);
 	}
 
 	public SetValue createSetValue(DomainCollectionType type, Set<? extends Value> values) {
 		return new SetValueImpl(this, type, values);
 	}
 
 	public SetValue createSetValue(DomainCollectionType type, Collection<? extends Value> values) {
 		return new SetValueImpl(this, type, values);
 	}
 
 	public SetValue createSetValue(Value... values) {
 		DomainType elementType = getElementType(values);
 		DomainCollectionType collectionType = standardLibrary.getSetType(elementType);
 		return new SetValueImpl(this, collectionType, values);
 	}
 
 	public TupleValue createTupleValue(DomainTupleType type, Map<? extends DomainTypedElement, Value> values) {
 		return new TupleValueImpl(this, type, values);
 	}
 
 	public TypeValue createTypeValue(DomainClassifierType type) {
 		return new DomainTypeValueImpl(this, type);
 	}
 
 	public void dispose() {
 		falseValue = null; 
 		invalidValue = null; 
 		nullValue = null; 
 		oneValue = null;
 		trueValue = null; 
 		unlimitedValue = null; 
 		zeroValue = null;
 	}
 	
     public DomainType getElementType(Value... values) {
     	DomainType elementType = standardLibrary.getOclVoidType();
     	for (Value value : values) {
     		elementType = elementType.getCommonType(standardLibrary, value.getType());
     	}
      	return elementType;
     }
 
 	public Object getEcoreValueOf(Value value) {
 		if (value instanceof NullValue) {
 			return null;
 		}
 		else if (value instanceof CollectionValue) {
 			CollectionValue collectionValue = (CollectionValue) value;
 			List<Object> ecoreResult = new BasicEList<Object>(collectionValue.intSize());
 			for (Value elementValue : collectionValue) {
 				ecoreResult.add(getEcoreValueOf(elementValue));
 			}
 			return ecoreResult;
 		}
 		else if (value instanceof BooleanValueImpl) {
 			return ((BooleanValueImpl)value).asBoolean();
 		}
 		else if (value instanceof IntegerIntValueImpl) {
 			return ((IntegerIntValueImpl)value).intValue();
 		}
 		else if (value instanceof IntegerLongValueImpl) {
 			return ((IntegerLongValueImpl)value).longValue();
 		}
 		else if (value instanceof IntegerValueImpl) {
 			return ((IntegerValueImpl)value).bigIntegerValue();
 		}
 		else if (value instanceof RealValueImpl) {
 			return ((RealValueImpl)value).doubleValue();
 		}
 		else {
 			return value.asObject();
 		}
 	}
 
     public DomainType getElementType(Iterable<Value> values) {
     	DomainType elementType = standardLibrary.getOclVoidType();
     	for (Value value : values) {
     		elementType = elementType.getCommonType(standardLibrary, value.getType());
     	}
      	return elementType;
     }
 
 	public BooleanValue getFalse() {
 		if (falseValue == null) {
 			falseValue = new BooleanValueImpl(this, false); 
 		}
 		return falseValue;
 	}
 
 	public InvalidValue getInvalid() {
 		if (invalidValue == null) {
 			invalidValue = new InvalidValueImpl(this); 
 		}
 		return invalidValue;
 	}
 
 	public NullValue getNull() {
 		if (nullValue == null) {
 			nullValue = new NullValueImpl(this); 
 		}
 		return nullValue;
 	}
 
 	public IntegerValue getOne() {
 		if (oneValue == null) {
 			oneValue = integerValueOf(1);
 		}
 		return oneValue;
 	}
 	
 	public final DomainStandardLibrary getStandardLibrary() {
 		return standardLibrary;
 	}
 
 	public BooleanValue getTrue() {
 		if (trueValue == null) {
 			trueValue = new BooleanValueImpl(this, true); 
 		}
 		return trueValue;
 	}
 
 	public UnlimitedValue getUnlimited() {
 		if (unlimitedValue == null) {
 			unlimitedValue = new UnlimitedValueImpl(this);
 		}
 		return unlimitedValue;
 	}
 
 	public IntegerValue getZero() {
 		if (zeroValue == null) {
 			zeroValue = integerValueOf(0);
 		}
 		return zeroValue;
 	}
 
 	public IntegerValue integerValueOf(int value) {
 		return new IntegerIntValueImpl(this, value);
 	}
 
 	public IntegerValue integerValueOf(long value) {
 		if ((Integer.MIN_VALUE <= value) && (value <= Integer.MAX_VALUE)) {
 			return new IntegerIntValueImpl(this, (int) value);
 		}
 		else {
 			return new IntegerLongValueImpl(this, value);
 		}
 	}
 	
 	public IntegerValue integerValueOf(BigInteger value) {
 		if (value.signum() >= 0) {
 			if (value.compareTo(INTEGER_MAX_VALUE) <= 0) {
 				return new IntegerIntValueImpl(this, value.intValue());
 			}
 			if (value.compareTo(LONG_MAX_VALUE) <= 0) {
 				return new IntegerLongValueImpl(this, value.longValue());
 			}
 		}
 		else {
 			if (value.compareTo(INTEGER_MIN_VALUE) >= 0) {
 				return new IntegerIntValueImpl(this, value.intValue());
 			}
 			if (value.compareTo(LONG_MIN_VALUE) >= 0) {
 				return new IntegerLongValueImpl(this, value.longValue());
 			}
 		}
 		return new IntegerValueImpl(this, value);
 	}
 	
 	/**
 	 * Creates a BigInteger representation for aValue.
 	 * @param aValue the string representation of a (non-negative) integer number
 	 * @return the numeric representation
 	 * @throws InvalidValueException if representation cannot be created
 	 */
 	public IntegerValue integerValueOf(String aValue) throws InvalidValueException {
 		try {
 			int len = aValue.length();
 			if ((len < maxLongSize) || ((len == maxLongSize) && (maxLongValue.compareTo(aValue) >= 0))) {
 				return integerValueOf(BigInteger.valueOf(Long.parseLong(aValue)));
 			}
 			else {
 				return integerValueOf(new BigInteger(aValue));
 			}
 		}
 		catch (NumberFormatException e) {
 			return throwInvalidValueException(e, EvaluatorMessages.InvalidInteger, aValue);
 		}
 	}
 
 	public RealValue realValueOf(double value) {
 		return new RealValueImpl(this, value);
 	}
 
 	public RealValue realValueOf(BigDecimal value) {
 		return new RealValueImpl(this, value);
 	}
 
 //	public static RealValue realValueOf(IntegerValue value) {
 //		return new RealValueImpl(value.bigDecimalValue());
 //	}
 
 	public RealValue realValueOf(IntegerValue integerValue) {
 		return realValueOf(integerValue.bigDecimalValue());
 	}
 	
 	public RealValue realValueOf(String aValue) throws InvalidValueException {
 		try {
 			return new RealValueImpl(this, new BigDecimal(aValue.trim()));
 		}
 		catch (NumberFormatException e) {
 			return throwInvalidValueException(e, EvaluatorMessages.InvalidReal, aValue);
 		}
 	}
 	
 	public StringValue stringValueOf(String value) {
 		return new StringValueImpl(this, value);
 	}
 
 	public InvalidValue throwInvalidValueException(String message, Object... bindings) throws InvalidValueException {
 		String boundMessage = NLS.bind(message, bindings);
 		throw new InvalidValueException(boundMessage);
 	}
 
 	public InvalidValue throwInvalidValueException(Throwable e, String message, Object... bindings) throws InvalidValueException {
 		String boundMessage = NLS.bind(message, bindings);
 		throw new InvalidValueException(boundMessage, e);
 	}
 
 	public DomainType typeOf(Value value, Value... values) {
 		DomainType type = value.getType();
 //		if (values != null) {
 			for (Value anotherValue : values) {
 				type = type.getCommonType(standardLibrary, anotherValue.getType());
 			}		
 //		}
 		return type;
 	}
 
 	public Value valueOf(Object object) {
 		if (object == null) {
 			return getNull();
 		}
 		else if (object instanceof Value) {
 			return (Value) object;
 		}
 		else if (object instanceof EObject) {
			ObjectValue eObjectValue = createEObjectValue((EObject) object);
			if (eObjectValue != null) {
				return eObjectValue;
			}
			else {
				return new EObjectValueImpl(this, (EObject) object);
			}
 		}
 		else if (object instanceof Number) {
 			if ((object instanceof Integer) || (object instanceof Long) || (object instanceof Short) || (object instanceof Byte)) {
 				return integerValueOf(((Number) object).longValue());
 			}
 			if ((object instanceof Float) || (object instanceof Double)) {
 				return realValueOf(((Number) object).doubleValue());
 			}
 			if (object instanceof BigDecimal) {
 				return realValueOf((BigDecimal) object);
 			}
 			if (object instanceof BigInteger) {
 				return integerValueOf((BigInteger) object);
 			}			
 		}
 		else if (object instanceof String) {
 			return stringValueOf((String) object);
 		}
 		else if (object instanceof Character) {
 			return integerValueOf(((Character) object).charValue());
 		}			
 		else if (object instanceof Boolean) {
 			return booleanValueOf((Boolean) object);
 		}
 		else if (object.getClass().isArray()) {
 			try {
 				return createSequenceOf((Object[])object);
 			} 
 			catch (IllegalArgumentException e) {}
 		}
 		else if (object instanceof Iterable<?>) {
 			if ((object instanceof LinkedHashSet) || (object instanceof OrderedSet)) {
 				return createOrderedSetOf((Iterable<?>)object);
 			}
 			else if (object instanceof Bag) {
 				return createBagOf((Iterable<?>)object);
 			}
 			else if (object instanceof Set) {
 				return createSetOf((Iterable<?>)object);
 			}
 			else {
 				return createSequenceOf((Iterable<?>)object);
 			}
 		}
 		return createObjectValue(object);
 	}
 
 	public Value valueOf(Object eValue, ETypedElement eFeature) {
 		if (eFeature.isMany()) {
 			Collection<?> eValues = (Collection<?>) eValue;
 			ArrayList<Value> values = new ArrayList<Value>(eValues.size());
 			for (Object eVal : eValues) {
 				values.add(valueOf(eVal));
 			}
 			boolean isOrdered = eFeature.isOrdered();
 			boolean isUnique = eFeature.isUnique();
 			return createCollectionValue(isOrdered, isUnique, values);
 		}
 		else if (eValue instanceof Value) {
 			return (Value) eValue;		
 		}
 		else {
 			return valueOf(eValue);
 		}
 	}
 }
  
