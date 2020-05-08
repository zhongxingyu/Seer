 /*******************************************************************************
  * Copyright (c) 2007 DSource.org and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Bruno Medeiros - initial implementation
  *******************************************************************************/
 package melnorme.miscutil;
 
 
 import static melnorme.miscutil.Assert.assertFail;
 
 import java.util.Arrays;
 
 /**
  * Utils for miscellaneous core language functionality. 
  */
 public class CoreUtil /* extends Assert */ {
 	
 	/** @return whether the two given objects are the same (including null) or equal. */
 	public static boolean areEqual(Object o1, Object o2) {
 		return (o1 == o2) || (o1 != null && o2 != null && o1.equals(o2));
 	}
 	
 	/** @return whether the two given arrays are the same (including null) or equal 
 	 * according to {@link Arrays#equals(Object[], Object[])}. */
 	public static boolean areArrayEqual(Object[] a1, Object[] a2) {
 		return (a1 == a2) || (a1 != null && a2 != null && Arrays.equals(a1, a2));
 	}
 	
 	/** @return whether the two given arrays are the same (including null) or equal. 
 	 * according to {@link Arrays#deepEquals(Object[], Object[])}.*/
 	public static boolean areArrayDeepEqual(Object[] a1, Object[] a2) {
 		return (a1 == a2) || (a1 != null && a2 != null && Arrays.deepEquals(a1, a2));
 	}
 	
 	/** Returns the first element of objs array that is not null.
 	 * At least one element must be not null. */
 	public static <T> T firstNonNull(T... objs) {
 		for (int i = 0; i < objs.length; i++) {
 			if(objs[i] != null)
 				return objs[i];
 		}
 		assertFail();
 		return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static <T> T blindCast(Object obj) {
 		return (T) obj;
 	}
 	
 	/** If possible casts and returns given obj as a type T, otherwise return null. */
 	public static <T> T tryCast(Object obj, Class<T> klass) {
 		if(klass.isAssignableFrom(obj.getClass())) {
			return blindCast(obj);
 		} else {
 			return null;
 		}
 	}
 	
 	/** Shortcut for creating an array of T. */
 	public static <T> T[] array(T... elems) {
 		return elems;
 	}
 	
 	public static boolean[] arrayP(boolean... elems) {
 		return elems;
 	}
 	public static byte[] arrayP(byte... elems) {
 		return elems;
 	}
 	public static short[] arrayP(short... elems) {
 		return elems;
 	}
 	public static int[] arrayP(int... elems) {
 		return elems;
 	}
 	public static long[] arrayP(long... elems) {
 		return elems;
 	}
 	public static float[] arrayP(float... elems) {
 		return elems;
 	}
 	public static double[] arrayP(double... elems) {
 		return elems;
 	}
 	public static char[] arrayP(char... elems) {
 		return elems;
 	}
 	
 	/** Marker method for signaling a feature that is not yet implemented. 
 	 * Uses the Deprecated annotation solely to cause a warning.
 	 * Causes an assertion failure. */ 
 	@Deprecated
 	public static RuntimeException assertTODO() {
 		return Assert.fail("TODO");
 	}
 	
 	/** Marker method for signaling a feature that is not yet implemented. 
 	 * Uses the Deprecated annotation solely to cause a warning. 
 	 * Returns false. */
 	@Deprecated
 	public static boolean taskTODO() {
 		return false;
 	}
 	
 }
