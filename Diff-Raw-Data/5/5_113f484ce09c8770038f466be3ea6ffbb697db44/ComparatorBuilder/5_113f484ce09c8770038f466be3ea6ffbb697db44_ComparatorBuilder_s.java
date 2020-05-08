 /*
  *  Copyright (c) 2012 Nicholas Okunew
  *  All rights reserved.
  *  
  *  This file is part of the com.atomicleopard.expressive library
  *  
  *  The com.atomicleopard.expressive library is free software: you 
  *  can redistribute it and/or modify it under the terms of the GNU
  *  Lesser General Public License as published by the Free Software Foundation, 
  *  either version 3 of the License, or (at your option) any later version.
  *  
  *  The com.atomicleopard.expressive library is distributed in the hope
  *  that it will be useful, but WITHOUT ANY WARRANTY; without even
  *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU Lesser General Public License for more details.
  *  
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with the com.atomicleopard.expressive library.  If not, see
  *  http://www.gnu.org/licenses/lgpl-3.0.html.
  */
 package com.atomicleopard.expressive.comparator;
 
 import java.beans.PropertyDescriptor;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import net.sf.cglib.core.ReflectUtils;
 
 import com.atomicleopard.expressive.ETransformer;
 import com.atomicleopard.expressive.Expressive;
 import com.atomicleopard.expressive.transform.ETransformers;
 
 /**
  * <p>
  * A {@link ComparatorBuilder} allows the creation of a comparator for a given java bean type based on the values of the properties within the bean.
  * </p>
  * <p>
  * In the resulting comparator, properties are compared using the specified rules in the order they are supplied to the builder. Evaluation continues until a non-zero value occurs on a property, or
  * all properties have been compared.
  * </p>
  * 
  * @param <T>
  * @see Expressive.Comparators#compare(Class)
  */
 public class ComparatorBuilder<T> implements Comparator<T> {
 	private Class<T> type;
 	private Map<String, PropertyDescriptor> getters;
 	protected LinkedHashMap<String, Comparator<?>> propertyComparators = new LinkedHashMap<String, Comparator<?>>();
 
 	private static Map<Class<?>, Map<String, PropertyDescriptor>> PropertyDescriptorCache = new HashMap<Class<?>, Map<String, PropertyDescriptor>>();
 	private static ETransformer<Collection<PropertyDescriptor>, Map<String, PropertyDescriptor>> PropertyDescriptorLookupTransformer = ETransformers.toKeyBeanLookup("name", PropertyDescriptor.class);
 
 	public ComparatorBuilder(Class<T> type) {
 		this(type, false);
 	}
 
 	public ComparatorBuilder(Class<T> type, boolean noCache) {
 		this.type = type;
 		this.getters = getPropertyDescriptions(type, noCache);
 	}
 
 	private ComparatorBuilder(ComparatorBuilder<T> comparatorBuilder) {
 		this.type = comparatorBuilder.type;
 		this.getters = comparatorBuilder.getters;
 		this.propertyComparators = new LinkedHashMap<String, Comparator<?>>(comparatorBuilder.propertyComparators);
 	}
 
 	protected void put(String property, Comparator<?> comparator) {
 		if (!this.getters.containsKey(property)) {
 			throw new RuntimeException(String.format("Unable to compare %s on the property %s, there is no accessible bean property with that name", type.getName(), property));
 		}
 		propertyComparators.put(property, comparator);
 	}
 
 	public <S> CompareUsing<S> on(String property) {
 		return new CompareUsing<S>(property);
 	}
 
 	/**
 	 * We cache property descriptors for types
 	 * 
 	 * @param type
 	 * @param noCache
 	 * @return
 	 */
 	private static Map<String, PropertyDescriptor> getPropertyDescriptions(Class<?> type, boolean noCache) {
 		Map<String, PropertyDescriptor> existing = noCache ? null : PropertyDescriptorCache.get(type);
 		if (existing == null) {
 			PropertyDescriptor[] getters = ReflectUtils.getBeanGetters(type);
 			existing = PropertyDescriptorLookupTransformer.from(Arrays.asList(getters));
 			if (!noCache) {
 				PropertyDescriptorCache.put(type, existing);
 			}
 		}
 		return existing;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public int compare(T o1, T o2) {
 		try {
 			for (Map.Entry<String, Comparator<?>> entry : propertyComparators.entrySet()) {
 				String property = entry.getKey();
 				Object l = getters.get(property).getReadMethod().invoke(o1);
 				Object r = getters.get(property).getReadMethod().invoke(o2);
 				Comparator comparator = entry.getValue();
 				int value = comparator.compare(l, r);
 				if (value != 0) {
 					return value;
 				}
 			}
 			return 0;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private <S> ComparatorBuilder<T> copyAndAdd(String property, Comparator<S> comparator) {
 		ComparatorBuilder<T> copy = new ComparatorBuilder<T>(this);
 		copy.put(property, comparator);
 		return copy;
 	}
 
 	public class CompareUsing<S> {
 		private String property;
 
 		public CompareUsing(String property) {
 			this.property = property;
 		}
 
 		public ComparatorBuilder<T> using(Comparator<S> propertyComparator) {
 			return copyAndAdd(property, propertyComparator);
 		}
 
		public <C extends Comparable<C>> ComparatorBuilder<T> naturally() {
			return copyAndAdd(property, new ComparableComparator<C>());
 		}
 	}
 }
