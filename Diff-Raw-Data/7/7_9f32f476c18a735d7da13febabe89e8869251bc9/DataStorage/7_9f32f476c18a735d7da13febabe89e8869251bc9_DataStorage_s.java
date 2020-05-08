 /*******************************************************************************
  * Copyright (c) 2013 - 2014 Maksym Barvinskyi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  *     Maksym Barvinskyi - initial API and implementation
  ******************************************************************************/
 package org.grible.adaptor;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * Class that contains methods for retrieving data from grible Data Storages and
  * transform it to descriptors objects.
  * 
  * @author Maksym Barvinskyi
  * @author Oleksii Pasko :)
  * 
  */
 public class DataStorage {
 	private static Map<Class<?>, TreeMap<Integer, ?>> descriptors = new HashMap<Class<?>, TreeMap<Integer, ?>>();
 
 	@SuppressWarnings("unchecked")
 	public static <T> List<T> getDescriptors(Class<T> type, Integer[] indexes) {
 		List<T> result = new ArrayList<T>();
 		if (indexes[0] == 0) {
 			return result;
 		}
 		createDescriptorsEntryWithEmptyDescriptor(type);
 
 		TreeMap<Integer, T> map = (TreeMap<Integer, T>) descriptors.get(type);
 
 		TestTable dataStorage = new TestTable(type.getSimpleName());
 		Map<Integer, HashMap<String, String>> rows = dataStorage.getDataStorageValues(indexes);
 
 		for (Integer index : indexes) {
 			final T value = map.get(index);
 			if (value != null) {
 				result.add(value);
 				continue;
 			}
 
 			T descriptor = null;
 			try {
 				Constructor<T> c = type.getConstructor(new Class[] { HashMap.class });
 				descriptor = c.newInstance(new Object[] { rows.get(index) });
 			} catch (Exception e) {
 				Exception gribleException = new Exception("DataStorage exception: " + e.getMessage()
 						+ ". Happened during creating a descriptor: " + type.toString() + " # " + index);
 				gribleException.setStackTrace(e.getStackTrace());
 				GribleSettings.getErrorsHandler().onAdaptorFail(gribleException);
 			}
 			map.put(new Integer(index), descriptor);
 			result.add(descriptor);
 		}
 
 		return result;
 	}
 	
 	public static <T> List<T> getDescriptors(Class<T> type) {
 		TestTable dataStorage = new TestTable(type.getSimpleName());
 		List<HashMap<String, String>> rows = dataStorage.getDataStorageValues();
 
 		Integer[] indexes = new Integer[rows.size()];
 		for (int i = 0; i < rows.size(); i++) {
 			indexes[i] = i + 1;
 		}
 		return getDescriptors(type, indexes);
 	}
 
 	private static <T> void createDescriptorsEntryWithEmptyDescriptor(Class<T> type) {
 		if (descriptors.get(type) == null) {
 			TreeMap<Integer, T> map = new TreeMap<Integer, T>();
 			map.put(new Integer(0), createEmptyDescriptor(type));
 			descriptors.put(type, map);
 		}
 	}
 
 	/**
 	 * Retrieves data from grible Data Storage and transforms it to descriptors
 	 * objects.
 	 * 
 	 * @param type
 	 *            - class of the descriptor (i.e. UserInfo.class);
 	 * @param indexes
 	 *            - rows indexes of the descriptors to retrieve (i.e. "5",
 	 *            "1;2;2;7");
 	 * @return ArrayList of the descriptors with specified row numbers found in
 	 *         the storage.
 	 */
 	public static <T> List<T> getDescriptors(Class<T> type, String indexes) {
 		Integer[] iterationNumbers = getIntArrayFromString(indexes, ";");
 		return getDescriptors(type, iterationNumbers);
 	}
 
 	/**
 	 * Retrieves data from grible Data Storage and transforms it to the single
 	 * descriptor object.
 	 * 
 	 * @param type
 	 *            - class of the descriptor (i.e. UserInfo.class);
 	 * @param index
 	 *            - row index of the descriptor to retrieve (i.e. "1", "5"); if
 	 *            "0", returns an empty descriptor;
 	 * @return Descriptor for specified row number found in the storage or an
 	 *         empty (which all properties are null) descriptor.
 	 */
 	public static <T> T getDescriptor(Class<T> type, Integer index) {
		return getDescriptors(type, String.valueOf(index)).get(0);
 	}
 
 	public static <T> T getDescriptor(Class<T> type, String index) {
		return getDescriptors(type, index).get(0);
 	}
 
 	private static <T> T createEmptyDescriptor(Class<T> type) {
 		T descriptor = null;
 		try {
 			Constructor<T> c = type.getConstructor(new Class[] { HashMap.class });
 			descriptor = c.newInstance(new Object[] { null });
 		} catch (Exception e) {
 			Exception gribleException = new Exception("DataStorage exception: " + e.getMessage()
 					+ ". Happened during creating an empty descriptor: " + type.toString());
 			gribleException.setStackTrace(e.getStackTrace());
 			GribleSettings.getErrorsHandler().onAdaptorFail(gribleException);
 		}
 		return descriptor;
 	}
 
 	private static Integer[] getIntArrayFromString(String allElements, String delimiter) {
 		if (("").equals(allElements) || (allElements == null)) {
 			return new Integer[] { 0 };
 		}
 		String[] tempArray = allElements.split(delimiter);
 		Integer[] resultArray = new Integer[tempArray.length];
 		for (int i = 0; i < tempArray.length; i++) {
 			resultArray[i] = Integer.parseInt(tempArray[i]);
 		}
 		return resultArray;
 	}
 }
