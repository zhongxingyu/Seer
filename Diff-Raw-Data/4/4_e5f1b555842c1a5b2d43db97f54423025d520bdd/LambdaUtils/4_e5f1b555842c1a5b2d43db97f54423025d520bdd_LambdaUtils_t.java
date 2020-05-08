 package bruce.common.functional;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public final class LambdaUtils {
 	
 	public static <TSource> void forEach(Iterable<TSource> iterable, Action1<TSource> act) {
 		for (TSource obj : iterable)
 			act.call(obj);
 	}
 
 	public static <TSource> void forEachWithIndex(Iterable<TSource> iterable, Action2<TSource, Integer> act) {
 		int index = 0;
 		for (TSource tSource : iterable) {
 			act.call(tSource, index++);
 		}
 	}
 	
	public static <Rtn, TSource> List<Rtn> map(Iterable<TSource> list, Func1<Rtn, TSource> func) {
		List<Rtn> listRtn = new ArrayList<Rtn>();
 		for (TSource src : list)
 			listRtn.add(func.call(src));
 		return listRtn;
 	}
 	
 	public static <Rtn, TSource> Rtn reduce(Iterable<TSource> iterable, Rtn init, Func2<Rtn, Rtn, TSource> func2) {
 		for (TSource tSource : iterable) {
 			init = func2.call(init, tSource);
 		}
 		return init;
 	}
 	
 	public static <TSource> TSource firstOrNull(Iterable<TSource> iterable, Func1<Boolean, TSource> predicate) {
 		for (TSource tSource : iterable) {
 			if (predicate.call(tSource)) return tSource;
 		}
 		return null;
 	}
 
 	public static <Rtn, TSource1, TSource2> List<Rtn> zip(final List<TSource1> list1, final List<TSource2> list2, final Func2<Rtn, TSource1, TSource2> zipFunc) {
 		final ArrayList<Rtn> results = new ArrayList<Rtn>();
 		while (list1.size() < list2.size()) list1.add(null);
 		while (list2.size() < list1.size()) list2.add(null);
 		forEachWithIndex(list1, new Action2<TSource1, Integer>() {
 			@Override
 			public void call(TSource1 t1, Integer i) {
 				results.add(zipFunc.call(t1, list2.get(i)));
 			}
 		});
 		return results;
 	}
 
 	public static <Rtn, TSource> List<Rtn> select(Iterable<TSource> iterable, final Func1<Rtn, TSource> selector) {
 		final List<Rtn> results = new ArrayList<Rtn>();
 		forEach(iterable, new Action1<TSource>() {
 			@Override
 			public void call(TSource t) {
 				results.add(selector.call(t));
 			}
 		});
 		return results;
 	}
 	
 	public static <TSource> List<TSource> where(Iterable<TSource> iterable, final Func1<Boolean, TSource> predicate) {
 		final List<TSource> results = new ArrayList<TSource>();
 		forEach(iterable, new Action1<TSource>() {
 			@Override
 			public void call(TSource t) {
 				if (predicate.call(t)) results.add(t);
 			}
 		});
 		return results;
 	}
 	
 	public static <_Comparable_, TSource> SortableList<TSource> orderBy(List<TSource> list) {
 		return orderBy(list, new Func1<TSource, TSource>() {
 			@Override
 			public TSource call(TSource t) { return t; }
 		});
 	}
 	
 	public static <_Comparable_, TSource> SortableList<TSource> orderBy(List<TSource> list, final Func1<_Comparable_, TSource> selector) {
 		return new SortableList<TSource>(list, selector, false);
 	}
 
 	public static <_Comparable_, TSource> SortableList<TSource> orderByDescending(List<TSource> list) {
 		return orderByDescending(list, new Func1<TSource, TSource>() {
 			@Override
 			public TSource call(TSource t) { return t; }
 		});
 	}
 	
 	public static <_Comparable_, TSource> SortableList<TSource> orderByDescending(List<TSource> list, final Func1<_Comparable_, TSource> selector) {
 		return new SortableList<TSource>(list, selector, true);
 	}
 	
 
 	public static <TSource> int count(Iterable<TSource> iterable, final Func1<Boolean, TSource> predicate) {
 		return reduce(iterable, 0, new Func2<Integer, Integer, TSource>() {
 			@Override
 			public Integer call(Integer init, TSource t) {
 				if (predicate == null || predicate.call(t)) return init + 1;
 				return init;
 			}
 		});
 	}
 
 	public static <TSource> int sum(Iterable<TSource> iterable, final Func1<Integer, TSource> selector) {
 		return reduce(iterable, 0, new Func2<Integer, Integer, TSource>() {
 			@Override
 			public Integer call(Integer init, TSource t) {
 				return init + selector.call(t);
 			}
 		});
 	}
 	
 	public static void times(int times, Action1<Integer> action) {
 		for (int i = 0; i < times; i++) {
 			action.call(i);
 		}
 	}
 	
 	public static <TSource> boolean all(Iterable<TSource> iterable, Func1<Boolean, TSource> predicate) {
 		for (TSource tSource : iterable) {
 			if (!predicate.call(tSource)) return false;
 		}
 		return true;
 	}
 	
 	public static <TSource> boolean any(Iterable<TSource> iterable, Func1<Boolean, TSource> predicate) {
 		for (TSource tSource : iterable) {
 			if (predicate.call(tSource)) return true;
 		}
 		return false;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static <TSource, Value> Comparable<Value> max(Iterable<TSource> iterable, Func1<Comparable<Value>, TSource> selector) {
 		Comparable<Value> maxVal = null;
 		for (TSource tSource : iterable) {
 			Comparable<Value> comparableVal = selector.call(tSource);
 			if (maxVal == null) maxVal = comparableVal;
 			else if (maxVal.compareTo((Value) comparableVal) < 0) maxVal = comparableVal;
 		}
 		return maxVal;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static <TSource, Value> Comparable<Value> min(Iterable<TSource> iterable, Func1<Comparable<Value>, TSource> selector) {
 		Comparable<Value> minVal = null;
 		for (TSource tSource : iterable) {
 			Comparable<Value> comparableVal = selector.call(tSource);
 			if (minVal == null) minVal = comparableVal;
 			else if (minVal.compareTo((Value) comparableVal) > 0) minVal = comparableVal;
 		}
 		return minVal;
 	}
 	
 	public static <TSource> int indexOf(Iterable<TSource> iterable, Func1<Boolean, TSource> predicate) {
 		int i = 0;
 		for (TSource tSource : iterable) {
 			if (predicate.call(tSource)) return i;
 			i++;
 		}
 		return -1;
 	}
 	
 }
