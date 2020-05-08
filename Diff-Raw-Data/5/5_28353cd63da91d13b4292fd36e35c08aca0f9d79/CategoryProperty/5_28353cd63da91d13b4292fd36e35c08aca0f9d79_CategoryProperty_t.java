 package org.jgum.category;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.AbstractIterator;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.Iterators;
 
 /**
  * The property of a category. A category property value depends on the location of the category in a categorization.
  * If it does not explicitly set a property, its value will be looked up in its ancestor categories. 
  * @author sergioc
  *
  * @param <T>
  */
 public class CategoryProperty<T> {
 
 	public static <U> FluentIterable<U> properties(Iterable<? extends Category> categories, Object key) {
 		return FluentIterable.<U>from(new PropertyIterable<U>(categories, key));
 	}
 	
 	private final Category category;
 	private final Object property;
 	private final PropertyIterable<T> propertyIterable;
 	
 	/**
 	 * Resolves a given property according to the default bottom-up linearization function.
 	 * @param category the category where a property is queried.
 	 * @param property the property name to query.
 	 */
 	public CategoryProperty(Category category, Object property) {
 		this(category, property, category.bottomUpLinearization());
 	}
 
 	/**
 	 * Resolves a given property according to a given bottom-up linearization function.
 	 * @param category the category where a property is queried.
 	 * @param property the property name to query.
 	 * @param linearizationFunction the bottom-up linearization function that will be used to find the property value.
 	 */
 	public CategoryProperty(Category category, Object property, Function<Category, List<Category>> linearizationFunction) {
 		this(category, property, linearizationFunction.apply(category));
 	}
 	
 	private CategoryProperty(Category category, Object property, List<? extends Category> linearization) {
 		this.category = category;
 		this.property = property;
 		this.propertyIterable = new PropertyIterable<T>(linearization, property);
 	}
 	
 	public Category getCategory() {
 		return category;
 	}
 
 	/**
 	 * 
 	 * @return the name of the property.
 	 */
 	public Object getName() {
 		return property;
 	}
 	
 	/**
 	 * 
 	 * @return true if the property is present. false otherwise.
 	 */
 	public boolean isPresent() {
 		return propertyIterable.iterator().hasNext();
 	}
 	
 	/**
 	 * 
 	 * @return the value of a property in a category. If the property is not set it will throw an exception.
 	 */
 	public T get() {
 		return propertyIterable.iterator().next();
 	}
 	
 	/**
 	 * Sets the value of the property in the wrapped category.
 	 * @param value the value of the property.
 	 */
 	public void set(T value) {
 		category.setProperty(property, value);
 	}
 	
 	
 	
 	public static class PropertyIterator<T> extends AbstractIterator<T> {
 		
 		private final Iterator<? extends Category> propertyNodes;
 		private final Object key;
 		
		public PropertyIterator(Category category, Object key) {
 			this((Iterator)category.bottomUpLinearization(), key);
 		}
 		
 		public PropertyIterator(Iterator<? extends Category> propertyNodes, final Object key) {
 			this.key = key;
 			this.propertyNodes = Iterators.filter(propertyNodes, new Predicate<Category>() {
 				@Override
 				public boolean apply(Category category) {
 					return category.containsLocalProperty(key);
 				}
 			});
 		}
 		
 		@Override
 		protected T computeNext() {
 			if(propertyNodes.hasNext()) {
 				Category nextPropertiesNode = propertyNodes.next();
 				return (T) nextPropertiesNode.getLocalProperty(key).get();
 			} else
 				return endOfData();
 		}
 	}
 
 	
 	
 	public static class PropertyIterable<T> implements Iterable<T> {
 
 		private final Iterable<? extends Category> propertyNodes;
 		private final Object key;
 		
		public PropertyIterable(Category category, Object key) {
 			this((Iterable)category.bottomUpLinearization(), key);
 		}
 		
 		public PropertyIterable(Iterable<? extends Category> propertyNodes, Object key) {
 			this.propertyNodes = propertyNodes;
 			this.key = key;
 		}
 		
 		@Override
 		public Iterator<T> iterator() {
 			return new PropertyIterator<>(propertyNodes.iterator(), key);
 		}
 
 	}
 	
 }
