 // Modified or written by Ex Machina SAGL for inclusion with lambdaj.
 // Copyright (c) 2009 Mario Fusco, Luca Marrocco.
 // Licensed under the Apache License, Version 2.0 (the "License")
 
 package ch.lambdaj;
 
 import java.lang.reflect.*;
 import java.util.*;
 
 import org.hamcrest.*;
 
 import ch.lambdaj.function.aggregate.*;
 import ch.lambdaj.function.argument.*;
 import ch.lambdaj.function.compare.*;
 import ch.lambdaj.function.convert.*;
 import ch.lambdaj.proxy.*;
 
 /**
  * @author Mario Fusco
  * @author Luca Marrocco
  */
 @SuppressWarnings("unchecked")
 public class Lambda {
 	
 	public static <T> T on(Class<T> clazz) {
 		return ArgumentsFactory.createArgument(clazz);
 	}
 
 	public static final <T> T by(T t) {
 		T result = null;
 		try {
 			result = (T) t.getClass().newInstance();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return result;
 	}
 
 	/**
 	 * Transforms a collection of Ts in a single object having the same methods of a single instance of T.
 	 * That allows to invoke a method on each T in the collection with a single strong typed method call as in the following example:
 	 * <p/>
 	 * <code>
 	 * 		List<Person> personInFamily = asList(new Person("Domenico"), new Person("Mario"), new Person("Irma"));
 	 *		forEach(personInFamily).setLastName("Fusco");
 	 * </code>
 	 * <p/>
 	 * The actual class of T is inferred from the class of the first iterable's item, but you can
 	 * specify a particular class by using the overloaded method.
 	 * @param <T> The type of the items in the iterable
 	 * @param iterable The iterable to be transformed
 	 * @return An object that proxies all the item in the iterable or null if the iterable is null or empty
 	 */
 	public static <T> T forEach(Iterable<? extends T> iterable) {
 		if (iterable == null) return null;
 		Iterator<? extends T> iterator = iterable.iterator();
 		return (iterator.hasNext()) ? forEach(iterable, iterator.next().getClass()) : null;
 	}
 
 	/**
 	 * Transforms a collection of Ts in a single object having the same methods of a single instance of T.
 	 * That allows to invoke a method on each T in the collection with a single strong typed method call as in the following example:
 	 * <p/>
 	 * <code>
 	 * 		List<Person> personInFamily = asList(new Person("Domenico"), new Person("Mario"), new Person("Irma"));
 	 *		forEach(personInFamily, Person.class).setLastName("Fusco");
 	 * </code>
 	 * <p/>
 	 * The given class represents the proxied by the returned object, so it should be a superclass of all the objects in the iterable.
 	 * @param <T> The type of the items in the iterable
 	 * @param iterable The iterable to be transformed
 	 * @param clazz The class proxied by the returned object
 	 * @return An object that proxies all the item in the iterable. If the given iterable is null or empty it returns
 	 * an instance of T that actually proxies an empty Iterable of Ts
 	 */
 	public static <T> T forEach(Iterable<? extends T> iterable, Class<?> clazz) {
 		return ProxyIterator.createProxyIterator(iterable, clazz);
 	}
 
 	public static <T> T[] toArray(Collection<T> c) {
 		if (c == null || c.isEmpty()) return null;
 		return toArray(c, c.iterator().next().getClass());
 	}
 
 	public static <T> T[] toArray(Collection<T> c, Class<?> t) {
 		return c.toArray((T[]) Array.newInstance(t, c == null ? 0 : c.size()));
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// /// Collection
 	// ////////////////////////////////////////////////////////////////////////
 
	public static <T> List<T> collect(Object iterable) {
 		List<T> collected = new LinkedList<T>();
 		for (Object item : (Iterable<?>) iterable) {
 			if (item instanceof Iterable) collected.addAll((Collection<T>) collect(item));
 			else
 				collected.add((T) item);
 		}
 		return collected;
 	}
 	
 	public static <T> List<T> collect(Object iterable, T argument) {
		return collect(convert(iterable, new ArgumentConverter<Object, Object>(argument)));
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// /// Selection
 	// ////////////////////////////////////////////////////////////////////////
 
 
 	public static <T> Collection<T> filter(Matcher<?> matcher, Iterable<T> iterable) {
 		return select(iterable, matcher);
 	}
 
 	public static <T> Collection<T> select(Iterable<T> iterable, Matcher<?> matcher) {
 		Collection<T> collected = new LinkedList<T>();
 		if (iterable == null) return collected;
 		for (T item : iterable)
 			if (matcher.matches(item)) collected.add(item);
 		return collected;
 	}
 
 	public static <T> Collection<T> to(Iterable<T> iterable, Matcher<?> matcher) {
 		return select(iterable, matcher);
 	}
 
 	public static <T> Collection<T> select(Object iterable, Matcher<?> matcher) {
 		return select((Iterable<T>) iterable, matcher);
 	}
 
 	public static <T> T selectUnique(Object iterable, Matcher<?> matcher) {
 		return selectUnique((Iterable<T>) iterable, matcher);
 	}
 
 	public static <T> T selectUnique(Iterable<T> iterable, Matcher<?> matcher) {
 		return (T) unique(select(iterable, matcher));
 	}
 
 	public static <T> T unique(Collection<T> collected) {
 		if (collected.isEmpty()) return null;
 		if (collected.size() > 1) throw new RuntimeException("Not unique item. Found: " + collected);
 		return collected.iterator().next();
 	}
 
 	public static <T> T selectFirst(Object iterable, Matcher<?> matcher) {
 		return selectFirst((Iterable<T>) iterable, matcher);
 	}
 
 	public static <T> T first(Collection<T> collected) {
 		if (collected.isEmpty()) return null;
 		return collected.iterator().next();
 	}
 
 	public static <T> T selectFirst(Iterable<T> iterable, Matcher<?> matcher) {
 		return (T) first(select(iterable, matcher));
 	}
 
 	public static <T> Collection<T> selectDistinct(Iterable<T> iterable) {
 		return selectDistinct(iterable, (Comparator<T>) null);
 	}
 
 	public static <T> Collection<T> selectDistinct(Object iterable) {
 		return selectDistinct((Iterable<T>) iterable, (Comparator<T>) null);
 	}
 
 	public static <T> Collection<T> selectDistinct(Iterable<T> iterable, String propertyName) {
 		return selectDistinct(iterable, new PropertyComparator<T>(propertyName));
 	}
 
 	public static <T> Collection<T> selectDistinct(Object iterable, String propertyName) {
 		return selectDistinct((Iterable<T>) iterable, new PropertyComparator<T>(propertyName));
 	}
 
 	public static <T> Collection<T> selectDistinct(Object iterable, Comparator<T> comparator) {
 		Set<T> collected = comparator == null ? new HashSet<T>() : new TreeSet<T>(comparator);
 		if (iterable != null) for (T item : (Iterable<T>) iterable)
 			collected.add(item);
 		return collected;
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// /// Aggregation
 	// ////////////////////////////////////////////////////////////////////////
 
 	private static final Sum Sum = new Sum();
 
 	private static final Min Min = new Min();
 
 	private static final Max Max = new Max();
 
 	private static final Concat Concat = new Concat();
 
 	public static <T> T aggregate(Object iterable, Aggregator<T> aggregator) {
 		T result = aggregator.emptyItem();
 		if (iterable != null) for (T item : (Iterable<T>) iterable)
 			result = aggregator.aggregate(result, item);
 		return result;
 	}
 
 	public static <T> T aggregate(Object iterable, Aggregator<T> aggregator, Object argument) {
 		return aggregate(convert(iterable, new ArgumentConverter<Object, Object>(argument)), aggregator);
 	}
 	
 	public static <T, A> T aggregateFrom(Iterable<T> iterable, Aggregator<A> a) {
 		if (iterable == null) return null;
 		Iterator<T> i = iterable.iterator();
 		return i.hasNext() ? aggregateFrom(iterable, i.next().getClass(), a) : null;
 	}
 
 	public static <T, A> T aggregateFrom(Iterable<T> i, Class<?> c, Aggregator<A> a) {
 		return (T) ProxyAggregator.createProxyAggregator(i, a, c);
 	}
 
 	// -- (Sum) ---------------------------------------------------------------
 
 	public static Number sum(Object iterable) {
 		return aggregate(iterable, Sum);
 	}
 
 	public static <T> T sum(Object iterable, T argument) {
 		return (T)aggregate(iterable, Sum, argument);
 	}
 	
 	public static <T> T sumFrom(Iterable<T> c) {
 		return aggregateFrom(c, Sum);
 	}
 
 	public static <T> T sumFrom(Iterable<T> c, Class<?> t) {
 		return aggregateFrom(c, t, Sum);
 	}
 
 	// -- (Min) ---------------------------------------------------------------
 
 	public static <T> T min(Object iterable) {
 		return (T) aggregate((Iterable<T>) iterable, Min);
 	}
 
 	public static <T> T minFrom(Iterable<T> c) {
 		return (T) aggregateFrom(c, Min);
 	}
 
 	public static <T> T minFrom(Iterable<T> c, Class<?> t) {
 		return (T) aggregateFrom(c, t, Min);
 	}
 
 	// -- (Max) ---------------------------------------------------------------
 
 	public static <T> T max(Object iterable) {
 		return (T) aggregate((Iterable<T>) iterable, Max);
 	}
 
 	public static <T> T maxFrom(Iterable<T> c) {
 		return (T) aggregateFrom(c, Max);
 	}
 
 	public static <T> T maxFrom(Iterable<T> c, Class<?> t) {
 		return (T) aggregateFrom(c, t, Max);
 	}
 
 	// -- (Concat) ------------------------------------------------------------
 
 	public static <T> T joinFrom(Iterable<T> c) {
 		return aggregateFrom(c, Concat);
 	}
 
 	public static <T> T joinFrom(Iterable<T> c, String separator) {
 		return aggregateFrom(c, new Concat(separator));
 	}
 
 	public static <T> T joinFrom(Iterable<T> c, Class<?> t) {
 		return aggregateFrom(c, t, Concat);
 	}
 
 	public static <T> T joinFrom(Iterable<T> c, Class<?> t, String separator) {
 		return aggregateFrom(c, t, new Concat(separator));
 	}
 
 	public static String join(Object iterable) {
 		return join(iterable, ", ");
 	}
 
 	private static void flatten(List collection, Iterable iterable) {
 		for (Object object : iterable) {
 			if (object instanceof Iterable) {
 				flatten(collection, (Iterable) object);
 			} else if (object instanceof Map) {
 				flatten(collection, ((Map) object).values());
 			} else {
 				collection.add(object);
 			}
 		}
 	}
 
 	public static List flatten(Iterable iterable) {
 		List collection = new LinkedList();
 		flatten(collection, iterable);
 		return collection;
 	}
 
 	public static String join(Object iterable, String separator) {
 		if (iterable instanceof String) return iterable.toString();
 		if (iterable instanceof Long) return iterable.toString();
 		if (iterable instanceof Double) return iterable.toString();
 		if (iterable instanceof Float) return iterable.toString();
 		if (iterable instanceof Integer) return iterable.toString();
 		return (String) aggregate((Iterable<?>) iterable, new Concat(separator));
 	}
 
 	// ////////////////////////////////////////////////////////////////////////
 	// /// Conversion
 	// ////////////////////////////////////////////////////////////////////////
 
 	public static <F, T> Collection<T> convert(Object iterable, Converter<F, T> convertor) {
 		Collection<T> collected = new ArrayList<T>();
 		if (iterable != null) for (F item : (Iterable<F>) iterable)
 			collected.add(convertor.convert(item));
 		return collected;
 	}
 
 	public static <F, T> Collection<T> extract(Object iterable, String propertyName) {
 		return convert(iterable, new PropertyExtractor<F, T>(propertyName));
 	}
 	
 	public static <F, T> Map<T, F> map(Object iterable, Converter<F, T> convertor) {
 		Map<T, F> map = new HashMap<T, F>();
 		if (iterable != null) for (F item : (Iterable<F>) iterable)
 			map.put(convertor.convert(item), item);
 		return map;
 	}
 	
 	public static <F, T> Map<T, F> index(Object iterable, String propertyName) {
 		return map(iterable, new PropertyExtractor<F, T>(propertyName));
 	}
 }
