 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.scannable;
 
 import gda.util.QuantityFactory;
 
 import java.lang.reflect.Array;
 import java.util.List;
 
 import org.jscience.physics.quantities.Dimensionless;
 import org.jscience.physics.quantities.Quantity;
 import org.jscience.physics.units.Unit;
 import org.python.core.PyFloat;
 import org.python.core.PyInteger;
 import org.python.core.PyList;
 import org.python.core.PyObject;
 import org.python.core.PySequence;
 import org.python.core.PyString;
 import org.python.core.PyTuple;
 
 /**
  * Some functions for converting the the objects used as positions by Scannables.
  */
 public class PositionConvertorFunctions {
 
 	/**
 	 * Converts an object to an object array. If the object is an array it is caste directly to an array, otherwise it
 	 * is put into a single element array. No length checking is performed.
 	 * 
 	 * @param object
 	 * @return definitely an object array.
 	 */
 	static public Object[] toObjectArray(Object object) {
 
 		if (object == null) {
 			return null;
 		}
 
 		// will capture any non-primitive array
 		if (object instanceof Object[]) {
 			return (Object[]) object;
 		}
 
 		if (object instanceof PyString) { // PyString is a type of PySequence, so we need a special case
 			return new Object[] { object };
 		}
 
 		if (object instanceof PySequence) {
 			int length = ((PySequence) object).__len__();
 			Object[] objectArray = new Object[length];
 			for (int i = 0; i < length; i++) {
 				Object item = ((PySequence) object).__finditem__(i);
 				if (!(item instanceof org.python.core.PyNone)){
 					objectArray[i] = item;
 				}
 			}
 			return objectArray;
 		}
 
 		if (object instanceof List<?>) {
 			List<?> list = (List<?>) object;
 			return list.toArray(new Object[] {});
 		}
 
 		if (object.getClass().isArray()) {
 			int length = Array.getLength(object);
 			Object[] objectArray = new Object[length];
 			for (int i = 0; i < length; i++) {
 				Object item = Array.get(object, i);
 				if (!(item instanceof org.python.core.PyNone)){
 					objectArray[i] = item;
 				}
 			}
 			return objectArray;
 		}
 
 		// The object is not an array of understandable form. Assume it is a single element
 		return new Object[] { object };
 
 	}
 
 	/**
 	 * Attempts to convert an object array to the targetObject's container type. Returns a PyTuple if the target is a
 	 * PyTuple. Returns a PyList if the target is any other PySequence (includes PyList, but by special exception, not
 	 * PyString). Returns an Object[] if the target is a List or array. Otherwise assumes the target was a single
 	 * element and returns the first element of objectArray.
 	 * 
 	 * @param objectArray
 	 * @param targetObject
 	 * @return object
 	 */
 	public static Object toParticularContainer(Object[] objectArray, Object targetObject) {
 
 		if (objectArray == null) {
 			return null;
 		}
 
 		if (targetObject instanceof PyString) { // PyString is a type of PySequence, so we need a special case
 			return objectArray[0];
 		}
 
 		if (targetObject instanceof PyTuple) {
 			try {
 				PyObject[] pyObjectArray = toPyObjectArray(objectArray);
 				return new PyTuple(pyObjectArray, false);
 			} catch (IllegalArgumentException e) {
 				return objectArray;
 			}
 		}
 
 		if (targetObject instanceof PySequence) {
 			try {
 				PyObject[] pyObjectArray = toPyObjectArray(objectArray);
 				return new PyList(pyObjectArray);
 			} catch (IllegalArgumentException e) {
 				return objectArray;
 			}
 		}
 
 		if (targetObject instanceof List<?>) {
 			return objectArray;
 		}
 
 		if (targetObject.getClass().isArray()) {
 			return objectArray;
 		}
 
 		// The object is not an array of understandable form. Assume it is a single element
 		// TODO at risk of breaking code, should check that the objectArray has only one element before doing this and
 		// throw exception otherwise.
 		return objectArray[0];
 
 	}
 
 	public static org.python.core.PyObject toPyObject(Object object) {
 
 		if (object == null) {
 			return null;
 		}
 
 		if (object instanceof PyObject) {
 			return (PyObject) object;
 		}
 
 		if (object instanceof String) {
 			return new PyString((String) object);
 		}
 
 		if (object instanceof Float) {
 			return new PyFloat(((Float) object).floatValue());
 		}
 
 		if (object instanceof Double) {
 			return new PyFloat(((Double) object).doubleValue());
 		}
 
 		if (object instanceof Number) {
 			return new PyInteger(((Number) object).intValue());
 		}
 
 		throw new IllegalArgumentException("Could not convert " + object.toString() + " to a PyObject.");
 
 	}
 
 	public static PyObject[] toPyObjectArray(Object[] objectArray) {
 		PyObject[] pyObjectArray = new PyObject[objectArray.length];
 		for (int i = 0; i < objectArray.length; i++) {
 			pyObjectArray[i] = toPyObject(objectArray[i]);
 		}
 		return pyObjectArray;
 	}
 
 	/**
 	 * Returns the length of the Array/List that the object would be coerced into.
 	 * 
 	 * @param object
 	 * @return length
 	 */
 	static public int length(Object object) {
 		return toObjectArray(object).length;
 	}
 
 	/**
 	 * Converts an array to an object. If the array has only one element then this element is returned otherwise the
 	 * array is simply down-casted to an object.
 	 * 
 	 * @param objectArray
 	 * @return either an object, or an object array.
 	 */
 	static public Object toObject(Object[] objectArray) {
 
 		if (objectArray == null) {
 			return null;
 		}
 
 		if (objectArray.length == 1) {
 			return objectArray[0];
 		}
 		return objectArray; // as Object!
 	}
 
 	/**
 	 * Attempts to convert and Object to a Double. The Object may be null, a String, a Number, any PyObject coercable to
 	 * Double, or a Quantity (where the Amount will be taken).
 	 * <p>
 	 * Note: May throw various Unchecked exceptions!
 	 * 
 	 * @param object
 	 * @return a Double
 	 */
 	static public Double toDouble(Object object) {
 
 		if (object == null) {
 			return null;
 		}
 		if (object instanceof String) {
 			return Double.parseDouble((String) object);
 		}
 		if (object instanceof Number) {
 			return ((Number) object).doubleValue();
 		}
 		if (object instanceof PyString) {
 			// The next case fails with PyStrings
			return ((PyString) object).atof();
 		}
 		if (object instanceof PyObject) {
 			return (Double) ((PyObject) object).__tojava__(Double.class);
 		}
 		if (object instanceof Quantity) {
 			return ((Quantity) object).getAmount();
 		}
 
 		throw new IllegalArgumentException("Could not convert " + object.toString() + " to a double.");
 	}
 
 	/**
 	 * Converts an array of Objects to an array of Doubles if possible.
 	 * 
 	 * @param objectArray
 	 */
 	static public Double[] toDoubleArray(Object[] objectArray) {
 
 		if (objectArray == null) {
 			return null;
 		}
 
 		Double[] doubleArray = new Double[objectArray.length];
 
 		for (int i = 0; i < objectArray.length; i++) {
 			doubleArray[i] = toDouble(objectArray[i]);
 		}
 		return doubleArray;
 	}
 
 	/**
 	 * Converts an Object List to a Double array if possible.
 	 * 
 	 * @param objectList
 	 * @return a Double array
 	 */
 	static public Double[] toDoubleArray(List<Object> objectList) {
 
 		return toDoubleArray(objectList.toArray());
 
 	}
 
 	// 
 
 	/**
 	 * Converts an Object to a Double array if possible. Uses toDouble() for conversion.
 	 * 
 	 * @param objectArray
 	 * @return A Double array
 	 */
 	static public Double[] toDoubleArray(Object objectArray) {
 
 		if (objectArray == null) {
 			return null;
 		}
 		return toDoubleArray(toObjectArray(objectArray));
 	}
 
 	static public Quantity[] toQuantityArray(final Object[] objectArray, final Unit<?> targetUnit) {
 		if (objectArray == null) {
 			return null;
 		}
 		Quantity[] quantityArray = new Quantity[objectArray.length];
 		for (int i = 0; i < objectArray.length; i++) {
 			quantityArray[i] = toQuantity(objectArray[i], targetUnit);
 		}
 		return quantityArray;
 	}
 
 	static public Quantity[] toQuantityArray(final Quantity[] quantityArray, final Unit<?> targetUnit) {
 		if (quantityArray == null) {
 			return null;
 		}
 		Quantity[] targetQuantityArray = new Quantity[quantityArray.length];
 		for (int i = 0; i < quantityArray.length; i++) {
 			targetQuantityArray[i] = (quantityArray[i]==null) ? null : quantityArray[i].to(targetUnit);
 		}
 		return targetQuantityArray;
 	}
 
 	static public Integer toInteger(Object object) {
 
 		if (object == null) {
 			return null;
 		}
 		if (object instanceof String) {
 			return Integer.parseInt((String) object);
 		}
 		if (object instanceof Number) {
 			return ((Number) object).intValue();
 		}
 		if (object instanceof PyString) {
 			// The next case fails with PyStrings
			return ((PyString) object).atoi();
 		}
 		if (object instanceof PyObject) {
 			return (Integer) ((PyObject) object).__tojava__(Integer.class);
 		}
 		if (object instanceof Quantity) {
 			return (int) ((Quantity) object).getAmount();
 		}
 
 		throw new IllegalArgumentException("Could not convert " + object.toString() + " to an integer.");
 	}
 	
 	/**
 	 * Converts an array of Objects to an array of Integers if possible.
 	 * 
 	 * @param objectArray
 	 */
 	static public Integer[] toIntegerArray(Object[] objectArray) {
 
 		if (objectArray == null) {
 			return null;
 		}
 		Integer[] integerArray = new Integer[objectArray.length];
 		for (int i = 0; i < objectArray.length; i++) {
 			integerArray[i] = toInteger(objectArray[i]);
 		}
 		return integerArray;
 	}
 	
 	static public Integer[] toIntegerArray(Object objectArray) {
 
 		if (objectArray == null) {
 			return null;
 		}
 		return toIntegerArray(toObjectArray(objectArray));
 	}
 	
 	
 	static public Quantity toQuantity(final Object object, final Unit<?> targetUnit) {
 
 		if (object == null) {
 			return null;
 		}
 
 		if (object instanceof Dimensionless) {
 			return Quantity.valueOf(((Dimensionless) object).getAmount(), targetUnit);
 		}
 
 		if (object instanceof Quantity) {
 			return ((Quantity) object).to(targetUnit);
 		}
 
 		if (object instanceof String) {
 			Quantity quantity = QuantityFactory.createFromString((String) object);
 			if (quantity == null) {
 				throw new IllegalArgumentException("Could not parse string '" + (String) object + "' to a quantity.");
 			}
 			return (quantity instanceof Dimensionless) ? Quantity.valueOf(((Dimensionless) quantity).getAmount(),
 					targetUnit) : quantity.to(targetUnit);
 		}
 
 		if (object instanceof PyString) {
 			return toQuantity(((PyString) object).toString(), targetUnit);
 		}
 
 		// Assume it is parseable to double. toDouble throws an IllegalArgumentException if it canot parse object
 		return Quantity.valueOf(toDouble(object), targetUnit);
 	}
 
 	static public Double[] toAmountArray(final Quantity[] quantityArray) {
 		Double[] ammountArray = new Double[quantityArray.length];
 		for (int i = 0; i < quantityArray.length; i++) {
 			ammountArray[i] = (quantityArray[i]==null) ? null : quantityArray[i].getAmount();
 		}
 		return ammountArray;
 	}
 
 }
