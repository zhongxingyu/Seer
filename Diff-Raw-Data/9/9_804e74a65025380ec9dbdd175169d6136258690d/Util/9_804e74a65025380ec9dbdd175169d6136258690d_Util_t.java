 package net.shrine.webclient.client.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * 
  * @author clint
  * @date Mar 22, 2012
  * 
  */
 public final class Util {
 	private Util() {
 		super();
 	}
 
 	public static final List<String> rowCssClasses = makeRowCssClassNameList("row");
 
 	public static final int numRowColors = 10;
 
 	static List<String> makeRowCssClassNameList(final String prefix) {
 		final List<String> result = Util.makeArrayList();
 
		for (int i = 0; i < numRowColors; ++i) {
 			result.add(prefix + i);
 		}
 
 		return result;
 	}
 
 	public static <T extends Comparable<T>> List<T> sorted(final Iterable<T> stuff) {
 		final List<T> result = makeArrayList(stuff);
 
 		Collections.sort(result);
 
 		return result;
 	}
 
 	public static <T> List<T> take(final int howMany, final Iterable<T> stuff) {
 		requireNotNull(stuff);
 
 		return take(howMany, stuff.iterator());
 	}
 
 	public static <T> List<T> take(final int howMany, final Iterator<T> stuff) {
 		require(howMany >= 0);
 		requireNotNull(stuff);
 
 		if (howMany == 0) {
 			return Collections.emptyList();
 		}
 
 		final List<T> result = makeArrayList();
 
 		for (int i = 0; i < howMany && stuff.hasNext(); ++i) {
 			result.add(stuff.next());
 		}
 
 		return result;
 	}
 
 	public static int count(final Iterable<?> elems) {
 		int result = 0;
 
 		for (@SuppressWarnings("unused")
 		final Object elem : elems) {
 			++result;
 		}
 
 		return result;
 	}
 
 	public static <T> List<T> toList(final Iterable<T> elems) {
 		if (elems instanceof List) {
 			return (List<T>) elems;
 		}
 
 		final List<T> result = makeArrayList();
 
 		for (final T elem : elems) {
 			result.add(elem);
 		}
 
 		return result;
 	}
 
 	public static <T> String join(final Iterable<T> things) {
 		return join("", things);
 	}
 
 	public static <T> String join(final String separator, final Iterable<T> things) {
 		final Iterator<T> itr = things.iterator();
 
 		final StringBuilder result = new StringBuilder();
 
 		while (itr.hasNext()) {
 			result.append(String.valueOf(itr.next()));
 
 			if (itr.hasNext()) {
 				result.append(separator);
 			}
 		}
 
 		return result.toString();
 	}
 
 	public static void requireNotNull(final Object o) {
 		require(o != null);
 	}
 
 	public static void require(final boolean expr) {
 		require(expr, null);
 	}
 
 	public static void require(final boolean expr, final String message) {
 		if (!expr) {
 			throw new IllegalArgumentException("Requirement not met: " + (message == null ? "" : message));
 		}
 	}
 
 	public static <T> ArrayList<T> makeArrayList() {
 		return new ArrayList<T>();
 	}
 
 	public static <T> ArrayList<T> makeArrayList(final Iterable<T> collection) {
 		final ArrayList<T> result = makeArrayList();
 
 		for (final T t : collection) {
 			result.add(t);
 		}
 
 		return result;
 	}
 
 	public static <K, V> HashMap<K, V> makeHashMap() {
 		return new HashMap<K, V>();
 	}
 }
