 package applets.Termumformungen$in$der$Technik_02_Kondensatoren;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Set;
 import java.util.Stack;
 
 
 public class Utils {
 
 	static Iterable<String> tokenizeString(final String str, String delim) {
 		final Set<Character> delimChars = new HashSet<Character>(collFromIter(iterableString(delim)));
 		return new Iterable<String>() {
 			public Iterator<String> iterator() {
 				return new Iterator<String>() {
 					int pos = 0;
 					public boolean hasNext() { return pos < str.length(); }
 					public String next() {
 						String ret = "";
 						for(; pos < str.length(); ++pos) {
 							char c = str.charAt(pos);
 							if(delimChars.contains(c)) {
 								if(ret.isEmpty()) {
 									ret += c;
 									pos++;
 								}
 								return ret;
 							}
 							ret += c;
 						}
 						return ret;
 					}
 					public void remove() { throw new UnsupportedOperationException(); }
 				};
 			}
 		};
 	}
 	
 	static Iterable<String> tokenizeString(String str) {
 		return tokenizeString(str, " \t\n\r\f");
 	}
 	
 	static <T> T randomChoiceFrom(List<T> list, Random r) {
 		int i = r.nextInt(list.size());
 		return list.get(i);
 	}
 	
 	static interface Size {
 	    int getWidth();
 	    int getHeight();
 	}
 		
 	static abstract class LightCollection<E> implements Collection<E> {
 		public boolean isEmpty() { return !iterator().hasNext(); }
 		public int size() {
 			int c = 0;
 			for(Iterator<E> i = iterator(); i.hasNext(); ++c) i.next();
 			return c;
 		}
 		public Object[] toArray() { return toArray(new Object[] {}); }
 		public <T> T[] toArray(T[] array) { 
 		    int size = size();
 		    if (array.length < size) { 
 		        array = newArray(classOf(array), size);
 		    } else if (array.length > size) {
 		        array[size] = null;
 		    }
 
 		    int i = 0;
 		    for (E e : this) {
 		        array[i] = classOf(array).cast(e);
 		        i++;
 		    }
 		    return array;
 		} 
 		public boolean add(E e) { throw new UnsupportedOperationException(); }
 		public boolean addAll(Collection<? extends E> c) { throw new UnsupportedOperationException(); }
 		public void clear() { throw new UnsupportedOperationException(); }
 		public boolean contains(Object o) {
 			for(Iterator<E> i = iterator(); i.hasNext(); )
 				if(i.next().equals(o)) return true;
 			return false;
 		}
 		public boolean containsAll(Collection<?> c) { throw new UnsupportedOperationException(); }
 		public boolean remove(Object o) { throw new UnsupportedOperationException(); }
 		public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
 		public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
 		@Override public String toString() { return "[" + concat(this, ", ") + "]"; }
 	}
 	
 	@SuppressWarnings("unchecked") static <T> Class<? extends T> classOf(T obj) {
 		return (Class<? extends T>) obj.getClass();
 	}
 
 	@SuppressWarnings("unchecked") static <T> Class<? extends T> classOf(T[] array) {
 		return (Class<? extends T>) array.getClass().getComponentType();
 	}
 
 	@SuppressWarnings("unchecked") static <T> T[] newArray(Class<T> clazz, int size) {
 		return (T[]) Array.newInstance(clazz, size);
 	}	
 	
 	static <E> LightCollection<E> collFromIter(final Iterable<E> iter) {
 		return new LightCollection<E>() {
 			public Iterator<E> iterator() {
 				return iter.iterator();
 			}
 		};
 	}
 	
 	static abstract class LightSet<E> extends LightCollection<E> implements Set<E> {}
 	
 	static <E> Collection<E> extendedCollectionView(final Collection<E> base, final Collection<E> extension) {
 		return new Utils.LightCollection<E>() {
 			@Override public boolean add(E e) { return extension.add(e); }
 			@Override public boolean addAll(Collection<? extends E> c) { return extension.addAll(c); }
 
 			public boolean isEmpty() { return base.isEmpty() && extension.isEmpty(); }
 			public int size() { return base.size() + extension.size(); }
 
 			public Iterator<E> iterator() {
 				return new Iterator<E>() {
 					Iterator<E> baseIterator = base.iterator();
 					Iterator<E> extendedIterator = extension.iterator();
 					boolean lastElementWasFromBase = true;
 					
 					public boolean hasNext() {
 						if(baseIterator.hasNext()) return true;
 						return extendedIterator.hasNext();
 					}
 
 					public E next() {
 						if(baseIterator.hasNext()) { lastElementWasFromBase = true; return baseIterator.next(); }
 						if(extendedIterator.hasNext()) { lastElementWasFromBase = false; return extendedIterator.next(); }
 						throw new NoSuchElementException();
 					}
 
 					public void remove() {
 						if(lastElementWasFromBase) baseIterator.remove();
 						else extendedIterator.remove();
 					}
 				};
 			}
 			
 			// Note that we could implement some more of Collection<E>. I just did not because I didn't needed it for now.
 		};
 	}
 	
 	static <E> Collection<E> concatCollectionView(final Iterable<? extends Iterable<E>> colls) {
 		return new LightCollection<E>() {
 			public Iterator<E> iterator() {
 				return new Iterator<E>() {
 					Iterator<? extends Iterable<E>> curSetsIter = colls.iterator();
 					Iterable<E> curColl = null;
 					Iterator<E> curIterator = null;
 					
 					{
 						forward();
 					}
 					
 					void forward() {
 						while(curIterator == null || !curIterator.hasNext()) {
 							if(curSetsIter.hasNext()) {
 								curColl = curSetsIter.next();
 								curIterator = curColl.iterator();
 							}
 							else
 								break;
 						}
 					}
 					
 					public boolean hasNext() {
 						return curIterator != null && curIterator.hasNext();
 					}
 					public E next() {
 						if(!hasNext()) throw new NoSuchElementException();
 						E result = curIterator.next();
 						forward();
 						return result;
 					}
 					public void remove() {
 						if(curIterator == null) throw new IllegalStateException();
 						curIterator.remove();
 					}
 				};
 			}
 		};
 	}
 	
 	static <E> Iterable<E> iterableFromArray(final E[] arr) {
 		return new Iterable<E>() {
 			public Iterator<E> iterator() {
 				return new Iterator<E>() {
 					int i = 0;					
 					public boolean hasNext() { return i < arr.length; }
 					public E next() { if(!hasNext()) throw new NoSuchElementException(); return arr[i++]; } 
 					public void remove() { throw new UnsupportedOperationException(); }					
 				};
 			}
 		};
 	}
 
 	static <E> List<E> listFromArgs() {
 		return new LinkedList<E>();
 	}
 
 	static <E> List<E> listFromArgs(E arg1) {
 		List<E> list = new LinkedList<E>();
 		list.add(arg1);
 		return list;
 	}
 
 	static <E> List<E> listFromArgs(E arg1, E arg2) {
 		List<E> list = new LinkedList<E>();
 		list.add(arg1);
 		list.add(arg2);
 		return list;
 	}
 
 	static <E> List<E> listFromArgs(E arg1, E arg2, E arg3) {
 		List<E> list = new LinkedList<E>();
 		list.add(arg1);
 		list.add(arg2);
 		list.add(arg3);
 		return list;
 	}
 	
 	static <E> Collection<E> concatCollectionView(Iterable<E> c1, Iterable<E> c2) {		
 		return concatCollectionView(listFromArgs(c1, c2));
 	}
 	
 	static <E> Set<E> mergedSetView(final Iterable<? extends Set<E>> sets) {
 		return new LightSet<E>() {
 			public Iterator<E> iterator() {
 				// NOTE: Because of unique, this requires O(N) memory.
 				// It is possible to implement this with O(1) memory requirement;
 				// however, this is a bit complicated/annoying and this is enough for now.
 				return unique(concatCollectionView(sets)).iterator();
 			}
 		};
 	}
 	
 	static interface Callback<T> {
 		void run(T obj);
 	}
 	
 	static <T> Callback<T> runnableToCallback(final Runnable r) {
 		return new Callback<T>() {
 			public void run(T obj) { r.run(); }
 		};
 	}
 	
 	static interface Predicate<T> {
 		boolean apply(T obj);
 	}
 	
 	static <T> Iterable<T> filter(final Iterable<T> i, final Predicate<? super T> pred) {
 		return new Iterable<T>() {
 			public Iterator<T> iterator() {
 				return new Iterator<T>() {
 					Iterator<T> last = i.iterator(), curr = i.iterator();
 					int lastDistToCurr = 0;
 					T nextObj = null;
 					
 					public boolean hasNext() { return advance(); }
 					public void remove() { last.remove(); }
 					
 					private boolean advance() {
 						if(nextObj != null) return true;
 						while(curr.hasNext()) {
 							lastDistToCurr++;
 							nextObj = curr.next();
 							if(pred.apply(nextObj))
 								return true;
 						}
 						nextObj = null;
 						return false;
 					}
 					
 					public T next() {
 						if(advance()) {
 							T obj = nextObj;
 							nextObj = null;
 							while(lastDistToCurr > 0) { last.next(); --lastDistToCurr; }
 							return obj;
 						}
 						throw new NoSuchElementException();
 					}
 				};
 			}
 		};
 	}
 	
 	static <X, Y extends X> Iterable<Y> filterType(final Iterable<X> i, final Class<Y> clazz) {
 		Iterable<X> iter = filter(i, new Predicate<X>() {
 			public boolean apply(X obj) {
 				return clazz.isInstance(obj);
 			}
 		});
 		return map(iter, new Function<X,Y>() {
 			public Y eval(X obj) {
 				return clazz.cast(obj);
 			}
 		});
 	}
 	
 	static <T> Set<T> filter(final Set<T> i, final Predicate<? super T> pred) {
 		return new LightSet<T>() {
 			public Iterator<T> iterator() {
 				return filter((Iterable<T>) i, pred).iterator();
 			}
 		};
 	}
 	
 	static <T> Iterable<T> filterNotIn(final Iterable<T> i, final Set<T> set) {
 		Predicate<T> filterNotInPred = new Predicate<T>() {
 			public boolean apply(T obj) { return !set.contains(obj); }						
 		};
 		return filter(i, filterNotInPred);
 	}
 	
 	static <T> Set<T> substractedSet(final Set<T> base, final Set<T> substract) {
 		return new LightSet<T>() {
 			public Iterator<T> iterator() {
 				return filterNotIn(base, substract).iterator();
 			}
 		};
 	}
 	
 	static <T> Iterable<T> unique(final Iterable<T> i) {
 		return new Iterable<T>() {
 			public Iterator<T> iterator() {
 				return new Iterator<T>() {
 					Set<T> objectsSoFar = new HashSet<T>();
 					Iterator<T> filteredIter = filterNotIn(i, objectsSoFar).iterator();
 					
 					public boolean hasNext() { return filteredIter.hasNext(); }
 					public T next() {
 						T obj = filteredIter.next();
 						objectsSoFar.add(obj);
 						return obj;
 					}
 					public void remove() { filteredIter.remove(); }
 				};
 			}
 		};
 	}
 	
 	static <T> Collection<T> cuttedFromRight(final Iterable<T> coll, final Predicate<T> stopCondition) {
 		return new LightCollection<T>() {
 			public Iterator<T> iterator() {
 				return new Iterator<T>() {
 					Iterator<T> iter = coll.iterator();
 					Iterator<T> nextIter = coll.iterator();					
 					T nextObj = null;
 					boolean stop = false;					
 					{
 						advanceNextIter();
 					}
 					
 					void advanceNextIter() {
 						if(nextIter.hasNext()) {
 							nextObj = nextIter.next();
 							if(stopCondition.apply(nextObj))
 								stop = true;
 						}
 						else {
 							if(iter.hasNext()) throw new AssertionError("something is wrong");
 							nextObj = null;
 						}
 					}
 					
 					public boolean hasNext() { return iter.hasNext() && !stop; }
 					public T next() {
 						if(!hasNext()) throw new NoSuchElementException();
 						T objToReturn = nextObj;
 						if(iter.next() != objToReturn) throw new AssertionError("something is wrong");
 						advanceNextIter();
 						return objToReturn;
 					}
 					public void remove() { iter.remove(); }
 				};
 			}			
 		};
 	}
 	
 	static <T> List<T> list(Iterable<T> i) {
 		List<T> l = new LinkedList<T>();
 		for(T o : i) l.add(o);
 		return l;
 	}
 	
 	static class Pair <T1,T2> {
 		T1 first;
 		T2 second;
 		public Pair(T1 first, T2 second) {
 			this.first = first;
 			this.second = second;
 		}
 	}
 	
 	static <T> Iterable<Pair<T,T>> allPairs(Iterable<T> coll) { return allPairs(coll, coll, true, true); }
 	static <T> Iterable<Pair<T,T>> allPairs(Iterable<T> coll1, Iterable<T> coll2) { return allPairs(coll1, coll2, false, false); }
 	static <T> Iterable<Pair<T,T>> allPairs(final Iterable<T> coll1, final Iterable<T> coll2, final boolean removeIdentityAndSwappedPairs, final boolean useDeepEqualCheck) {
 		return new Iterable<Pair<T,T>>() {
 			public Iterator<Pair<T,T>> iterator() {
 				return new Iterator<Pair<T,T>>() {
 					Iterator<T> i1 = coll1.iterator();
 					T obj1 = null;
 					Iterator<T> i2 = null;
 					T obj2 = null;
 					{
 						advance();
 					}
 					
 					void advance() {
 						if(obj1 != null && i2.hasNext()) {
 							obj2 = i2.next();
 							return;
 						}
 						
 						if(!i1.hasNext()) {
 							obj1 = null;
 							obj2 = null;
 							return;
 						}
 						
 						obj1 = i1.next();
 						i2 = coll2.iterator();
 						if(removeIdentityAndSwappedPairs) {
 							while(i2.hasNext()) {
 								if(useDeepEqualCheck && i2.next().equals(obj1)) break;
 								if(!useDeepEqualCheck && i2.next() == obj1) break;
 							}
 						}
 						if(i2.hasNext())
 							obj2 = i2.next();
 						else
 							obj2 = null;
 					}
 					
 					public boolean hasNext() { return obj1 != null && obj2 != null; }
 					public Pair<T,T> next() {
 						if(!hasNext()) throw new NoSuchElementException();
 						Pair<T,T> pair = new Pair<T,T>(obj1, obj2);
 						advance();
 						return pair;
 					}
 					public void remove() { throw new UnsupportedOperationException(); }
 				};
 			}
 		};
 	}	
 		
 	static <T1,T2> Iterable<Pair<T1,T2>> zip(final Iterable<T1> i1, final Iterable<T2> i2) {
 		return new Iterable<Pair<T1,T2>>() {
 			public Iterator<Pair<T1, T2>> iterator() {
 				return new Iterator<Pair<T1,T2>>() {
 					Iterator<T1> it1 = i1.iterator();
 					Iterator<T2> it2 = i2.iterator();
 					
 					public boolean hasNext() { return it1.hasNext() && it2.hasNext(); }
 					public Pair<T1, T2> next() { return new Pair<T1,T2>(it1.next(), it2.next()); }
 					public void remove() { it1.remove(); it2.remove(); }
 				};
 			}
 		};
 	}
 	
     static <T> Set<T> minSet(Iterable<? extends T> it, Comparator<? super T> comp) {
     	Iterator<? extends T> i = it.iterator();
     	Set<T> candidates = new HashSet<T>();
 
     	while (i.hasNext()) {
     		T next = i.next();
     		if(candidates.isEmpty())
     			candidates.add(next);
     		else {
     			int c = comp.compare(next, candidates.iterator().next());
     			if(c < 0) {
     				candidates.clear();
     				candidates.add(next);
     			}
     			else if(c == 0)
     				candidates.add(next);
     		}
     	}
     	
     	return candidates;
     }
 
     static <T> int indexInArray(T[] a, T obj) {
     	for(int i = 0; i < a.length; ++i)
     		if(a[i] == obj) return i;
     	return -1;
     }
     
 	static interface Function<X,Y> {
 		Y eval(X obj);
 	}
 
 	static <X> Function<X,X> identifyFunc() {
 		return new Function<X,X>() {
 			public X eval(X obj) { return obj; }
 		};
 	}	
 	
 	static <Y, X extends Y> Function<X,Y> identifyFunc2() {
 		return new Function<X,Y>() {
 			public Y eval(X obj) { return obj; }
 		};
 	}
 	
 	static interface CopyableIterator<X> extends Iterator<X>, Cloneable {}
 	
     static <X,Y> Iterable<Y> map(final Iterable<? extends X> coll, final Function<X,Y> func) {
 		return new Iterable<Y>() {
 			public Iterator<Y> iterator() {
 				return new Iterator<Y>() {
 					Iterator<? extends X> baseIter = coll.iterator();					
 					public boolean hasNext() { return baseIter.hasNext(); }
 					public Y next() { return func.eval(baseIter.next()); }
 					public void remove() { baseIter.remove(); }
 				};
 			}
 		};
     }
 
 	static <X,Y> Collection<Y> map(final Collection<? extends X> coll, final Function<X,Y> func) {
     	return new LightCollection<Y>() {
 			public boolean isEmpty() { return coll.isEmpty(); }
 			public int size() { return coll.size(); }
 			public Iterator<Y> iterator() { return map((Iterable<? extends X>) coll, func).iterator(); }
     	};
     }
     
     static <X> String concat(Iterable<X> coll, String seperator) {
     	String s = ""; Iterator<X> i = coll.iterator();
     	if(i.hasNext()) s += i.next().toString();
     	while(i.hasNext()) {
     		s += seperator;
     		s += i.next().toString();
     	}
     	return s;
     }
 
     static <T> Iterable<T> iterableReverseList(final List<T> l) {
     	return new Iterable<T>() {
     		public Iterator<T> iterator() {
     			return new Iterator<T>() {
     				ListIterator<T> listIter = l.listIterator(l.size());    				
 					public boolean hasNext() { return listIter.hasPrevious(); }
 					public T next() { return listIter.previous(); }
 					public void remove() { listIter.remove(); }    				
 				};
     		}
 		};
     }
     
     static <T extends Comparable<T>> Comparator<Collection<T>> orderOnCollection() {
     	return new Comparator<Collection<T>>() {
     		public int compare(Collection<T> o1, Collection<T> o2) {
     	        Iterator<T> i1 = o1.iterator();
     	        Iterator<T> i2 = o2.iterator();
     	        while(i1.hasNext() && i2.hasNext()) {
     	            int c = i1.next().compareTo(i2.next());
     	            if(c != 0) return c;
     	        }
     	        if(i1.hasNext())
     	            return 1;
     	        else if(i2.hasNext())
     	            return -1;
     	        return 0;
     	    }
 		};
     }
     
 	static class StringIterator implements Iterator<Character> {
 		String str; int pos = 0;
 		StringIterator(String str) { this.str = str; }
 		public boolean hasNext() { return pos < str.length(); }
 		public Character next() { return str.charAt(pos++); }
 		public void remove() { throw new AssertionError("removing in string iterator not supported"); }
 	}
     
 	static class StringReverseIterator implements Iterator<Character> {
 		String str; int pos;
 		StringReverseIterator(String str) { this.str = str; pos = str.length() - 1; }
 		public boolean hasNext() { return pos >= 0; }
 		public Character next() { return str.charAt(pos--); }
 		public void remove() { throw new AssertionError("removing in string iterator not supported"); }
 	}
 
 	static Iterable<Character> iterableString(final String s) {
 		return new Iterable<Character>() {
 			public Iterator<Character> iterator() { return new StringIterator(s); }
 		};
 	}
 
 	static Iterable<Character> iterableStringReversed(final String s) {
 		return new Iterable<Character>() {
 			public Iterator<Character> iterator() { return new StringReverseIterator(s); }
 		};
 	}
 
 	static <T> int equalLen(Iterable<? extends T> c1, Iterable<? extends T> c2) {
 		Iterator<? extends T> i1 = c1.iterator();
 		Iterator<? extends T> i2 = c2.iterator();
 		int len = 0;
 		while(i1.hasNext() && i2.hasNext()) {
 			if(!i1.next().equals(i2.next())) break;
 			len++;
 		}
 		return len;
 	}
 	
 	static int equalStartLen(String s1, String s2) { return equalLen(iterableString(s1), iterableString(s2)); }
 	static int equalEndLen(String s1, String s2) { return equalLen(iterableStringReversed(s1), iterableStringReversed(s2)); }
 	
     static <T> T castOrNull(Object obj, Class<T> clazz) {
         if(clazz.isInstance(obj))
             return clazz.cast(obj);
         return null;
     }
 	
     static int countStackFrames(String functionName) {
     	StackTraceElement[] stack = new Exception().getStackTrace();
     	int count = 0;
     	for(StackTraceElement frame : stack) {
     		if(frame.getMethodName().contains(functionName))
     			count++;
     	}
     	return count;
     }
     
     static String multiplyString(String s, int fac) {
     	String r = "";
     	for(int i = 0; i < fac; ++i) r += s;
     	return r;
     }
     
     static class ParseTree {
     	static abstract class Entity { abstract Entity trim(); }
     	static class Subtree extends Entity {
     		String prefix, postfix;
     		ParseTree content = new ParseTree();
     		Entity trim() {
     			Subtree t = new Subtree();
     			t.prefix = prefix; t.postfix = postfix;
     			t.content = content.trim();
     			return t;
     		}
     	}
     	static class RawString extends Entity {
     		String content = "";
     		RawString() {}
     		RawString(String s) { content = s; }
     		Entity trim() { return new RawString(content.trim()); }
     	}
     	List<Entity> entities = new LinkedList<Entity>();
     	
     	void parse(Iterator<Character> i, Map<String,String> bracketTypes, String stoppingBracket) {
     		for(String s : bracketTypes.keySet()) if(s.length() != 1) throw new AssertionError("bracketsize != 1 currently not supported");
     		for(String s : bracketTypes.values()) if(s.length() != 1) throw new AssertionError("bracketsize != 1 currently not supported");
 
     		RawString lastRaw = null;
     		while(i.hasNext()) {
     			char c = i.next();
     			if(stoppingBracket.equals("" + c)) return;
     			if(bracketTypes.containsKey("" + c)) {
     				lastRaw = null;
     				Subtree subtree = new Subtree();
     				subtree.prefix = "" + c;
     				subtree.postfix = bracketTypes.get("" + c);
     				entities.add(subtree);
     				subtree.content.parse(i, bracketTypes, subtree.postfix);
     			}
     			else {
     				if(lastRaw == null) {
     					lastRaw = new RawString();
     					entities.add(lastRaw);
     				}
     				lastRaw.content += c;
     			}
     		}
     	}
 
     	void parse(Iterator<Character> i, Map<String,String> bracketTypes) { parse(i, bracketTypes, ""); }
     	void parse(String str, Map<String,String> bracketTypes) { parse(new StringIterator(str), bracketTypes); }
     	
     	void parse(String str, String bracketTypes) {
     		if(bracketTypes.length() % 2 == 1) throw new AssertionError("bracketTypes string len must be a multiple of 2");
     		Map<String,String> bracketTypesMap = new HashMap<String,String>();
     		for(int i = 0; i < bracketTypes.length(); i += 2)
     			bracketTypesMap.put("" + bracketTypes.charAt(i), "" + bracketTypes.charAt(i+1));
     		parse(str, bracketTypesMap);
 		}
     	
     	void parse(String str) { parse(str, "()[]{}\"\""); }
     	
     	ParseTree() {}
     	ParseTree(String str) { parse(str); }
     	
     	ParseTree trim() {
     		ParseTree t = new ParseTree();
     		for(Entity e : entities) t.entities.add(e.trim());
     		return t;
     	}
     	void removeEmptyRawStrings() {
     		for(Iterator<Entity> e = entities.iterator(); e.hasNext();) {
     			RawString s = castOrNull(e.next(), RawString.class);
     			if(s != null && s.content.isEmpty()) e.remove();
     		}
     	}
     	List<ParseTree> split(Set<String> ss) {
     		for(String s : ss) if(s.length() != 1) throw new AssertionError("str len != 1 not supported currently");
     		List<ParseTree> l = new LinkedList<ParseTree>();
     		{
 	    		ParseTree t = new ParseTree();
 	    		l.add(t);
 	    		for(Entity e : entities) {
 	    			if(e instanceof RawString) {
 	    				String rs = ((RawString)e).content;
 	    				RawString lastStr = new RawString();
 	    				t.entities.add(lastStr);
 	    				for(Character c : iterableString(rs)) {
 	    					if(ss.contains("" + c)) {
 	    						t.removeEmptyRawStrings();
 	            				t = new ParseTree();
 	            				l.add(t);
 	            				lastStr = new RawString();
 	            				t.entities.add(lastStr);            				
 	    					}
 	    					else
 	    						lastStr.content += c;
 	    				}
 	    			}
 	    			else
 	    				t.entities.add(e);
 	    		}
 	    		t.removeEmptyRawStrings();
     		}
     		// remove empty trees
     		for(Iterator<ParseTree> i = l.iterator(); i.hasNext();) {
     			ParseTree t = i.next();
     			if(t.entities.isEmpty()) i.remove();
     		}	
     		return l;
     	}
     	List<ParseTree> split(String s) { Set<String> ss = new HashSet<String>(); ss.add(s); return split(ss); }
         
     	static ParseTree merge(List<ParseTree> tl) {
     		ParseTree finalt = new ParseTree();
     		for(ParseTree t : tl) finalt.entities.addAll(t.entities);
     		return finalt;
     	}
     }
     
     static class OperatorTree implements Comparable<OperatorTree> {
     	String op = "";
     	abstract static class Entity implements Comparable<Entity> {
     		OperatorTree asTree() { return new OperatorTree("", this); }
     		OperatorTree prefixed(final String op) {
 				// empty subtree at the beginning is the mark for a prefix op
     			OperatorTree ot = new OperatorTree(op, new Subtree(new OperatorTree()));
     			ot.entities.add(this);
     			return ot;
     		}
     		boolean isEnclosedImplicitely() { return true; }
 			String toString(String parentOp) { return toString(); }
     		abstract Object getContent();
 			@Override public int hashCode() { return 31 + getContent().hashCode(); }
 			@Override public boolean equals(Object obj) {
 				if(!(obj instanceof Entity)) return false;
 				return compareTo((Entity) obj) == 0;
 			}
     	}
     	static class RawString extends Entity {
     		String content = "";
     		RawString() {}
     		RawString(String s) { if(s == null) throw new AssertionError("string must be non-null"); content = s; }
     		@Override public String toString() { return debugOperatorTreeDump ? ("{" + content + "}") : content; }
 			public int compareTo(Entity o) {
 				if(o instanceof RawString) return content.compareTo(((RawString) o).content);
 				return -1;
 			}
 			Object getContent() { return content; }
 			static Function<RawString,String> toStringConverter() {
 				return new Function<RawString,String>() {
 					public String eval(RawString obj) {
 						return obj.content;
 					}
 				};
 			}
     	}
     	static class Subtree extends Entity {
     		OperatorTree content;
     		boolean explicitEnclosing = false;
     		Subtree(OperatorTree t) { content = t; }
     		Subtree(OperatorTree t, boolean explicitEnclosing) { content = t; this.explicitEnclosing = explicitEnclosing; }
 			@Override boolean isEnclosedImplicitely() { return explicitEnclosing; }
     		@Override public String toString() {
     			if(debugOperatorTreeDump || explicitEnclosing)
     				return "(" + content.toString() + ")";
     			if(content.entities.size() == 1)
     				return content.toString();
     			if(content.canBeInterpretedAsUnaryPrefixed())
     				return content.toString();
 				return "(" + content.toString() + ")";
     		}
     		@Override String toString(String parentOp) {
     			if(debugOperatorTreeDump || explicitEnclosing)
     				return "(" + content.toString() + ")";
     			if(content.entities.size() == 1)
     				return content.toString();
     			if(content.canBeInterpretedAsUnaryPrefixed())
     				return content.toString();
     			final String ops = "/∙+-=";
     			int parentOpIdx = ops.indexOf(parentOp);
     			int childOpIdx = ops.indexOf(content.op);
     			if(parentOpIdx < 0 || childOpIdx < 0) return "(" + content.toString() + ")";
     			if(parentOpIdx == 3) parentOpIdx--; if(childOpIdx == 3) childOpIdx--; // take +- as equal
     			if(childOpIdx <= parentOpIdx) return content.toString();
     			return "(" + content.toString() + ")";
     		}
     		@Override OperatorTree asTree() { return content; }
 			Object getContent() { return content; }
 			public int compareTo(Entity o) {
 				if(o instanceof Subtree) return content.compareTo(((Subtree) o).content);
 				return 1; // not subtree, i.e. rawstring, i.e. greater
 			}
     	}
     	List<Entity> entities = new LinkedList<Entity>();
     	
     	OperatorTree() {}
     	OperatorTree(String op) { this.op = op; } 
     	OperatorTree(String op, Entity e) { this.op = op; entities.add(e); } 
     	OperatorTree(String op, List<Entity> entities) { this.op = op; this.entities = entities; }
     	OperatorTree copy() { return new OperatorTree(op, new LinkedList<Entity>(entities)); }
     	static OperatorTree MergedEquation(OperatorTree left, OperatorTree right) {
 			return new OperatorTree("+", listFromArgs(left.asEntity(), right.minusOne().asEntity()));
     	}    	
     	static OperatorTree Sum(List<Entity> entities) { return new OperatorTree("+", entities); }
     	static OperatorTree Product(List<Entity> entities) { return new OperatorTree("∙", entities); }
     	
         static OperatorTree Zero() { return Number(0); }        
         boolean isZero() {
         	if(op.equals("∙") && entities.isEmpty()) return false;
         	if(entities.isEmpty()) return true;
         	if(entities.size() > 1) return false;
         	Entity e = entities.get(0);
         	if(e instanceof RawString)
         		return ((RawString) e).content.equals("0");
         	return ((Subtree) e).content.isZero();
         }
 
         static OperatorTree One() { return Number(1); }        
         boolean isOne() {
         	if(op.equals("∙") && entities.isEmpty()) return true;
         	if(entities.isEmpty()) return false;
         	if(entities.size() > 1) return false;
         	Entity e = entities.get(0);
         	if(e instanceof RawString)
         		return ((RawString) e).content.equals("1");
         	return ((Subtree) e).content.isOne();
         }
         
         static OperatorTree Variable(String var) {
         	try {
         		Integer.parseInt(var);
         		throw new AssertionError("Variable '" + var + "' must not be interpretable as a number.");
         	}
         	catch(NumberFormatException e) {
             	return new RawString(var).asTree();        		
         	}
         }
     	static OperatorTree Number(int num) { return new RawString("" + num).asTree(); }
     	Integer asNumber() {
     		if(isZero()) return 0;
     		if(isOne()) return 1;
     		if(entities.size() == 1) {
     			Entity e = entities.get(0);
     			if(e instanceof Subtree)
     				return ((Subtree)e).content.asNumber();
     			String s = ((RawString)e).content;
     			try { return Integer.parseInt(s); }
     			catch(NumberFormatException exc) { return null; }
     		}
     		Integer x;
     		if(op.equals("+") || op.equals("-")) x = 0;
     		else if(op.equals("∙") || op.equals("/")) x = 1;
     		else return null;
     		for(Entity e : entities) {
     			Integer y = e.asTree().asNumber();
     			if(y == null) return null;
     			try {
 	    			if(op.equals("+")) x += y;
 	    			else if(op.equals("-")) x -= y;
 	    			else if(op.equals("∙")) x *= y;
 	    			else if(op.equals("/")) x /= y;
     			} catch(ArithmeticException exc) { // e.g. div by zero 
     				return null;
     			}
     		}
     		return x;
     	}
         boolean isNumber(int num) {
         	Integer x = asNumber();
         	if(x == null) return false;
         	return x.intValue() == num;
         }
     	
     	OperatorTree sublist(int from, int to) { return new OperatorTree(op, entities.subList(from, to)); }
     	
     	static class RawStringIterator implements Iterator<RawString> {
     		static class State {
     			int entityIndex = 0;
     			OperatorTree ot;
     			State(int i, OperatorTree t) { entityIndex = i; ot = t; }
     			State(OperatorTree t) { ot = t; }
     			State(State s) { entityIndex = s.entityIndex; ot = s.ot; }
     			boolean hasNext() { return entityIndex < ot.entities.size(); }
     			Entity current() { return ot.entities.get(entityIndex); }
     			void remove() { ot.entities.remove(entityIndex); }
     			void replace(Entity e) { ot.entities.set(entityIndex, e); }
     			boolean isAtEnd() { return entityIndex == ot.entities.size() - 1; }
     		}
     		Stack<State> stateStack = new Stack<State>();
     		State lastState;
     		RawStringIterator() {}    		
     		RawStringIterator(OperatorTree t) { stateStack.push(new State(t)); walkSubtrees(); }    		
     		@SuppressWarnings("unchecked" /* because of the clone() */) RawStringIterator(RawStringIterator i) { 
     			stateStack = (Stack<State>) i.stateStack.clone();
     			lastState = i.lastState; // this is safe because we never manipulate lastState
     		}
     		RawStringIterator copy() { return new RawStringIterator(this); }
     		static Iterable<RawString> iterable(final OperatorTree t) {
     			return new Iterable<RawString>() {
 					public Iterator<RawString> iterator() {
 						return new RawStringIterator(t);
 					}
     			};
     		}
 
     		public boolean hasNext() { return !stateStack.empty() && stateStack.peek().hasNext(); }
 
     		void walkdown() {
 				while(!stateStack.empty() && !stateStack.peek().hasNext()) {
 					stateStack.pop();
 					if(!stateStack.empty())
 						stateStack.peek().entityIndex++;						
 				}
     		}
     		
     		void walkSubtrees() {
     			walkdown();
 				while(!stateStack.empty() && stateStack.peek().current() instanceof Subtree) {
 					stateStack.push(new State( ((Subtree)stateStack.peek().current()).content ));
 					walkdown();
 				}
     		}
     		
     		public RawString peek() {
 				if(!hasNext()) throw new NoSuchElementException();
 				return (RawString) stateStack.peek().current();
     		}
     		    		
 			public RawString next() {
 				if(!hasNext()) throw new NoSuchElementException();
 				
 				lastState = new State(stateStack.peek());
 				
 				stateStack.peek().entityIndex++;
 				walkSubtrees();
 				
 				return (RawString) lastState.current();
 			}
 
 			public void remove() {
 				if(lastState == null) throw new IllegalStateException();
 				lastState.remove();
 				for(State s : stateStack) {
 					if(s.ot == lastState.ot) {
 						if(s.entityIndex == lastState.entityIndex) throw new AssertionError("should not happen that we delete currently pointed-to entry");
 						if(s.entityIndex > lastState.entityIndex)
 							s.entityIndex--;
 					}
 				}
 				lastState = null;
 			}
 			
 			// replaces last element returned by next() with e in underlying container
 			public void replace(Entity e) {
 				if(lastState == null) throw new IllegalStateException();
 				lastState.replace(e);
 			}
 			
     		public boolean wasEndOfTree() {
 				if(lastState == null) throw new IllegalStateException();
     			return lastState.isAtEnd();
     		}
     		
     		public OperatorTree lastTree() {
 				if(lastState == null) throw new IllegalStateException();
     			return lastState.ot;
     		}
     	}
     	    	
     	/* Resulting tree contains subtrees which each parts and rawstrings
     	 * with the specific op.
     	 */
     	private static OperatorTree splitByOps(OperatorTree ot, Set<String> equalPriorityOps) {
     		OperatorTree res = new OperatorTree();
     		OperatorTree lastSubtree = new OperatorTree();
     		
     		for(Entity e : ot.entities) {
     			if(e instanceof RawString) {
     	    		String lastStr = "";
 					for(Character c : iterableString(((RawString) e).content)) {
 						if(equalPriorityOps.contains("" + c)) {
 							if(!lastStr.isEmpty()) {
 								lastSubtree.entities.add(new RawString(lastStr));
 								lastStr = "";
 							}
 							
 							if(!lastSubtree.entities.isEmpty()) {
 					    		res.entities.add(new Subtree(lastSubtree));
 					    		lastSubtree = new OperatorTree();
 							}
 							
 							res.entities.add(new RawString("" + c));
 						}
 						else if(c != ' ')							
 							lastStr += c;
 						else {
 							if(!lastStr.isEmpty()) {
 								lastSubtree.entities.add(new RawString(lastStr));
 								lastStr = "";
 							}							
 						}
 					}
 					if(!lastStr.isEmpty())
 						lastSubtree.entities.add(new RawString(lastStr));
     			}
     			else { // e instanceof Subtree
     				lastSubtree.entities.add(e);
     			}
     		}
 			if(!lastSubtree.entities.isEmpty())
 	    		res.entities.add(new Subtree(lastSubtree));			
     		
     		return res;
     	}
     	
     	static Entity parseOpsInEntity(final Entity source, final Set<String> binOps, List<Set<String>> binOpList) {
     		if(source instanceof RawString) {
     			OperatorTree ot = parseOpsInTree(new OperatorTree("", source), binOps, binOpList);
         		if(ot.entities.size() == 0) throw new AssertionError("something is wrong");
         		else if(ot.entities.size() == 1) return ot.entities.get(0);
         		return new Subtree(ot);        		
     		}
     		else
     			return new Subtree(parseOpsInTree(((Subtree)source).content, binOps, binOpList), ((Subtree) source).explicitEnclosing);
     	}
 
     	static OperatorTree grabUnaryPrefixedOpFromSplitted(final String op, final Iterator<Entity> rest) {
     		Entity obj = null;
     		
     		if(rest.hasNext()) {
     			Entity e = rest.next();
     			if(e instanceof Subtree) {
     				OperatorTree eSubtree = ((Subtree) e).content;
     				if(eSubtree.entities.size() == 1)
     					obj = eSubtree.entities.get(0);
     				else
     					obj = e;
     			} else // e instanceof RawString -> another unary prefix
     				obj = new Subtree(grabUnaryPrefixedOpFromSplitted(((RawString) e).content, rest));    			
     		}
     		else
     			// we have expected another entry but there is none anymore.
     			// add a dummy empty subtree so that it looks like an unary prefixed op.
     			obj = new Subtree(new OperatorTree());
     		
     		return obj.prefixed(op);
     	}
     	
     	static OperatorTree grabUnaryPrefixedOpFromSplitted(OperatorTree splitted) {
     		OperatorTree ot = new OperatorTree();
     		boolean nextOpWanted = false;
 			for(Iterator<Entity> eit = splitted.entities.iterator(); eit.hasNext();) {
 				Entity e = eit.next();
 				if(e instanceof RawString) {
 					String op = ((RawString) e).content;
 					if(!nextOpWanted) {
 						//System.out.println("prefix: " + op + " in " + splitted);
 						ot.entities.add(new Subtree(grabUnaryPrefixedOpFromSplitted(op, eit)));
 						//System.out.println("last prefixed: " + ((Subtree) ot.entities.get(ot.entities.size() - 1)).content);
 						nextOpWanted = true;
 					} else {
 						ot.entities.add(e);
 						nextOpWanted = false;
 					}
 				}
 				else { // e instanceof Subtree
 					if(nextOpWanted) throw new AssertionError("should not happen by the way splitByOps works");
 					ot.entities.add(e);
 					nextOpWanted = true;
 				}
 			}
 			return ot;
     	}
     	
     	static OperatorTree splitSplittedByOps(final OperatorTree splitted, Set<String> ops) {
     		OperatorTree ot = new OperatorTree();
     		OperatorTree lastSubtree = new OperatorTree();
     		
     		for(Entity e : splitted.entities) {
     			if(e instanceof RawString) {
     				String op = ((RawString) e).content;
     				if(ops.contains(op)) {
     					if(!lastSubtree.entities.isEmpty())
     			    		ot.entities.add(new Subtree(lastSubtree));
     					ot.entities.add(e);
     					lastSubtree = new OperatorTree();
     					continue;
     				}
     			}
     			lastSubtree.entities.add(e);
     		}
 			if(!lastSubtree.entities.isEmpty())
 	    		ot.entities.add(new Subtree(lastSubtree));
     		return ot;
     	}
     	
     	static OperatorTree handleOpsInSplitted(OperatorTree splitted, List<Set<String>> binOps) {
     		if(binOps.isEmpty()) {
     			if(splitted.entities.size() == 1 && splitted.entities.get(0) instanceof Subtree)
     				return ((Subtree) splitted.entities.get(0)).content;
     			return splitted;
     		}
     		
     		Set<String> equallyPriorityOps = binOps.get(0);
     		List<Set<String>> restBinOps = binOps.subList(1, binOps.size());
     		    		
 			splitted = splitSplittedByOps(splitted, equallyPriorityOps);
 			if(splitted.entities.size() == 1)
 				return handleOpsInSplitted( ((Subtree) splitted.entities.get(0)).content, restBinOps );
 			
     		OperatorTree ot = new OperatorTree();
     		boolean nextOpWanted = false;
 			for(Entity e : splitted.entities) {
 				if(e instanceof RawString) {
 					String op = ((RawString) e).content;
 					if(!nextOpWanted) throw new AssertionError("we should have handled that already in grabUnaryPrefixedOpFromSplitted");
 					if(ot.op.isEmpty())
 						ot.op = op;
 					else
 						// it must be an op in equallyPriorityOps because of splitSplittedByOps
 						ot = new OperatorTree(op, new Subtree(ot));
 					nextOpWanted = false;
 				}
 				else { // e instanceof Subtree
 					if(nextOpWanted) throw new AssertionError("should not happen by the way splitByOps works; ops = " + binOps + ", splitted = " + splitted + ", current = " + ot + ", next = " + ((Subtree) e).content);
 					OperatorTree subtree = ((Subtree) e).content;
 					//if(subtree.entities.size() == 1 && subtree.entities.get(0) instanceof Subtree)
 					//	subtree = ((Subtree) subtree.entities.get(0)).content;
 					subtree = handleOpsInSplitted( subtree, restBinOps );
 					if(subtree.entities.size() == 1)
 						ot.entities.add(subtree.entities.get(0));
 					else
 						ot.entities.add(new Subtree(subtree));
 					nextOpWanted = true;
 				}				
 			}
 			
 			return ot;
     	}
     	
     	static OperatorTree parseOpsInTree(final OperatorTree source, final Set<String> binOps, List<Set<String>> binOpList) {
     		if(source.entities.isEmpty()) return source;
     		
     		OperatorTree ot = new OperatorTree();
     		if(!source.op.isEmpty()) {
     			ot.op = source.op;
     			for(Entity e : source.entities)
     				ot.entities.add(parseOpsInEntity(e, binOps, binOpList));
     			return ot;
     		}
     		
     		OperatorTree splitted = splitByOps(source, binOps);
     		if(splitted.entities.size() == 1 && splitted.entities.get(0) instanceof Subtree) {
     			// no op found -> we put the source just into one single subtree
     			for(Entity e : ((Subtree) splitted.entities.get(0)).content.entities) {
     				if(e instanceof RawString)
     					ot.entities.add(e); // we already parsed that in splitByOps
     				else
     					ot.entities.add(new Subtree(parseOpsInTree( ((Subtree) e).content, binOps, binOpList ), ((Subtree) e).explicitEnclosing));
     			}
     			return ot;
     		}
     		
     		splitted = grabUnaryPrefixedOpFromSplitted(splitted);
     		//System.out.println("after unary prefix grab: " + splitted);
     		splitted = handleOpsInSplitted(splitted, binOpList);
     		//System.out.println("after op handling: " + splitted);
     		return parseOpsInTree( splitted, binOps, binOpList );
     	}
     	    	
     	static OperatorTree parseDefaultAnonBinOpInTree(final OperatorTree source, String defaultOp) {
     		OperatorTree ot = new OperatorTree();
     		ot.op = source.op;
     		if(source.op.isEmpty() && source.entities.size() > 1)
     			ot.op = defaultOp;
     		
     		for(Entity e : source.entities) {
     			if(e instanceof RawString)
     				ot.entities.add(e);
     			else
     				ot.entities.add( new Subtree( parseDefaultAnonBinOpInTree( ((Subtree) e).content, defaultOp ), ((Subtree) e).explicitEnclosing ) );
     		}
     		return ot;
     	}
     	    	
     	static OperatorTree parse(OperatorTree t, List<Set<String>> binOpList, String defaultAnonBinOp) {
     		Set<String> binOps = new HashSet<String>();
     		for(Set<String> ops : binOpList)
     			binOps.addAll(ops);
     			
     		t = parseOpsInTree(t, binOps, binOpList);
     		//System.out.println("before defaultop: " + t);
     		if(!defaultAnonBinOp.isEmpty()) t = parseDefaultAnonBinOpInTree(t, defaultAnonBinOp);
     		return t;
     	}
     	
     	static OperatorTree undefinedOpTreeFromParseTree(ParseTree t) {
     		OperatorTree ot = new OperatorTree();
     		for(ParseTree.Entity e : t.entities) {
     			if(e instanceof ParseTree.RawString)
     				ot.entities.add( new RawString(((ParseTree.RawString)e).content) );
     			else
     				ot.entities.add( new Subtree( undefinedOpTreeFromParseTree(((ParseTree.Subtree)e).content), true ) );
     		}
     		return ot;
     	}
     	    	
     	static List<Set<String>> parseOpList(String ops) {
     		List<Set<String>> opl = new LinkedList<Set<String>>();
     		Set<String> curEquOps = new HashSet<String>();
 			opl.add(curEquOps);
     		for(Character c : iterableString(ops)) {
     			if(c == ' ') {
     				if(curEquOps.isEmpty()) throw new AssertionError("bad oplist str");
     				curEquOps = new HashSet<String>();
     				opl.add(curEquOps);
     			}
     			else
     				curEquOps.add("" + c);
     		}
 			if(curEquOps.isEmpty()) throw new AssertionError("bad oplist str");
 			if(opl.isEmpty()) throw new AssertionError("bad oplist str - no operators");
 			return opl;
     	}
     	
     	static Set<String> simpleParseOps(String ops) {
     		Set<String> opSet = new HashSet<String>();
     		for(Character c : iterableString(ops)) {
     			if(c == ' ')
     				throw new AssertionError("bad oplist str");
     			else
     				opSet.add("" + c);
     		}
 			if(opSet.isEmpty()) throw new AssertionError("bad oplist str");
 			return opSet;
     	}
     	    	
     	static OperatorTree parse(String str, String binOps, String defaultAnonBinOp) {
     		return parse( undefinedOpTreeFromParseTree(new ParseTree(str)), parseOpList(binOps), defaultAnonBinOp );
     	}
     	static OperatorTree parse(String str, String defaultAnonBinOp) { return parse(str.replace('*', '∙').replace(',', '.'), "= +- ∙/", defaultAnonBinOp); }
     	static OperatorTree parse(String str) { return parse(str, "∙"); }
     	
     	boolean canBeInterpretedAsUnaryPrefixed() {
     		return entities.size() == 2 && entities.get(0) instanceof Subtree && ((Subtree)entities.get(0)).content.entities.isEmpty();	
     	}
     	
     	Entity unaryPrefixedContent() {
     		if(!canBeInterpretedAsUnaryPrefixed()) throw new AssertionError("we expect the OT to be unary prefixed");
     		return entities.get(1);
     	}
     	
     	OperatorTree prefixed(String prefixOp) {
     		return new OperatorTree(prefixOp, listFromArgs(new Subtree(new OperatorTree()), asEntity()));
     	}
     	
     	OperatorTree replaceVar(String var, OperatorTree replacement) {
     		OperatorTree ot = new OperatorTree(op);
     		for(Entity e : entities) {
     			if(e instanceof RawString) {
     				if(((RawString)e).content.equals(var))
     					ot.entities.add(replacement.asEntity());
     				else
     					ot.entities.add(e);
     			}
     			else {
     				Subtree subtree = (Subtree) e;
     				ot.entities.add(new Subtree(subtree.content.replaceVar(var, replacement), subtree.explicitEnclosing));
     			}
     		}
     		return ot;
     	}
     	
     	boolean isNegative() {
     		if(entities.size() == 1) {
     			Entity e = entities.get(0);
     			if(e instanceof RawString) return false;
     			return ((Subtree) e).content.isNegative();
     		}
     		
     		if(op.equals("∙") || op.equals("/")) {
     			boolean neg = false;
     			for(Entity e : entities)
     				neg ^= e.asTree().isNegative();
     			return neg;
     		}
     		
     		return canBeInterpretedAsUnaryPrefixed() && op.equals("-") && !unaryPrefixedContent().asTree().isNegative();
     	}
     	
     	@Override public String toString() {
     		if(debugOperatorTreeDump)
         		return "[" + op + "] " + concat(entities, ", ");    			
     		if(canBeInterpretedAsUnaryPrefixed())
     			// this is a special case used for unary ops (or ops which look like those)
     			return op + unaryPrefixedContent().toString(); // always put brackets if it is a subtree
     		/*if(entities.isEmpty()) {
     			if(isZero()) return "0";
     			if(isOne()) return "1";
     		}*/
     		Iterable<String> entitiesStr = map(entities, new Function<Entity,String>() {
 				public String eval(Entity obj) {
 					return obj.toString(op);
 				}
     		});
     		return concat(entitiesStr, " " + op + " ");
     	}
         static boolean debugOperatorTreeDump = false;
 
         String toString(boolean debug) {
         	boolean oldDebugState = debugOperatorTreeDump;
         	debugOperatorTreeDump = debug;
         	String s = toString();
         	debugOperatorTreeDump = oldDebugState;
         	return s;
         }
         
         String debugStringDouble() { return toString(false) + " // " + toString(true); }
         
 		public int compareTo(OperatorTree o) {
 			if(entities.size() != 1) {
 				int c = op.compareTo(o.op);
 				if(c != 0) return c;
 			}
 			Comparator<Collection<Entity>> comp = orderOnCollection();
 			return comp.compare(entities, o.entities);
 		}
 		@Override public int hashCode() {
 			int result = 1;
 			result = 31 * result + op.hashCode();
 			result = 31 * result + entities.hashCode();
 			return result;
 		}
 		@Override public boolean equals(Object obj) {
 			if(!(obj instanceof OperatorTree)) return false;
 			return compareTo((OperatorTree) obj) == 0;
 		}
 
 		Iterable<RawString> leafs() { return RawStringIterator.iterable(this); }
 		Iterable<String> leafsAsString() { return map(leafs(), RawString.toStringConverter()); }
 
         Iterable<String> vars() {
         	Iterable<String> leafs = leafsAsString();
         	return filter(leafs, new Predicate<String>() {
 				public boolean apply(String s) {
 					try {
 						Integer.parseInt(s);
 						return false; // it's an integer -> it's not a var
 					}
 					catch(NumberFormatException ex) {
 						return true; // it's not an integer -> it's a var
 					}
 				}
         	});
 		}
 		
 		Iterable<String> ops() {
 			return new Iterable<String>() {
 				public Iterator<String> iterator() {
 					return new Iterator<String>() {
 						Iterator<String> childs = null;
 						public boolean hasNext() { return childs == null || childs.hasNext(); }
 						public String next() {
 							if(childs == null) {
 								Iterable<Subtree> subtrees = filterType(OperatorTree.this.entities, Subtree.class);
 								Iterable<Iterable<String>> iterables = map(subtrees, new Function<Subtree,Iterable<String>>() {
 									public Iterable<String> eval(Subtree obj) {
 										return obj.content.ops();
 									}
 								});
 								childs = concatCollectionView(iterables).iterator();
 								return OperatorTree.this.op;
 							}
 							return childs.next();
 						}
 						public void remove() { throw new UnsupportedOperationException(); }
 					};
 				}
 			};
 		}
 		
 		OperatorTree removeObsolete() {
 			if(entities.size() == 1) {
 				if(entities.get(0) instanceof Subtree)
 					return ((Subtree) entities.get(0)).content.removeObsolete();
 				return this;
 			}
 				
 			OperatorTree ot = new OperatorTree(op);
 
 			if(op.equals("+") || op.equals("-")) {
 				boolean first = true;
 				for(Entity e : entities) {
 					if(e instanceof Subtree)
 						e = ((Subtree) e).content.removeObsolete().asEntity();
 					if(!e.asTree().isZero() || (op.equals("-") && first))
 						ot.entities.add(e);
 					first = false;
 				}
 			}
 			else if(op.equals("∙") || op.equals("/")) {
 				boolean first = true;
 				for(Entity e : entities) {
 					if(e instanceof Subtree)
 						e = ((Subtree) e).content.removeObsolete().asEntity();
 					if(!e.asTree().isOne() || (op.equals("/") && first))
 						ot.entities.add(e);
 					first = false;
 				}
 			}
 			else {
 				for(Entity e : entities) {
 					if(e instanceof Subtree)
 						e = ((Subtree) e).content.removeObsolete().asEntity();
 					ot.entities.add(e);
 				}
 			}
 			
 			return ot;
 		}
 		
         OperatorTree mergeOps(Set<String> ops) {
         	if(entities.size() == 1 && entities.get(0) instanceof Subtree)
         		return ((Subtree) entities.get(0)).content.mergeOps(ops);
         	
         	OperatorTree ot = new OperatorTree(op);
         	for(Entity e : entities) {
         		if(e instanceof RawString)
         			ot.entities.add(e);
         		else {
         			OperatorTree subtree = ((Subtree) e).content.mergeOps(ops);
         			if(subtree.op.equals(ot.op) && ops.contains(op) && !subtree.canBeInterpretedAsUnaryPrefixed())
         				ot.entities.addAll(subtree.entities);
         			else
         				ot.entities.add(new Subtree(subtree));
         		}
         	}
         	return ot;
         }
         
         OperatorTree mergeOpsFromRight(Set<String> ops) {
         	if(entities.size() == 1 && entities.get(0) instanceof Subtree)
         		return ((Subtree) entities.get(0)).content.mergeOpsFromRight(ops);
         	
         	OperatorTree ot = new OperatorTree();
         	boolean first = true;
         	ot.op = op;
         	for(Entity e : entities) {
         		if(e instanceof RawString)
         			ot.entities.add(e);
         		else {
         			OperatorTree subtree = ((Subtree) e).content.mergeOpsFromRight(ops);
         			if(first && subtree.op.equals(ot.op) && ops.contains(op) && !subtree.canBeInterpretedAsUnaryPrefixed())
         				ot = subtree;
         			else
         				ot.entities.add(new Subtree(subtree));
         		}
         		first = false;
         	}
         	return ot;
         }        
         
         OperatorTree mergeOps(String ops) { return mergeOps(simpleParseOps(ops)); }        
         OperatorTree mergeOpsFromRight(String ops) { return mergeOpsFromRight(simpleParseOps(ops)); }
         OperatorTree simplify() { return mergeOps("=+∙").mergeOpsFromRight("-/").removeObsolete(); }
         
         OperatorTree transformOp(String oldOp, String newOp, Function<Entity,Entity> leftTransform, Function<Entity,Entity> rightTransform) {
         	OperatorTree ot = new OperatorTree();
         	if(!op.equals(oldOp) || canBeInterpretedAsUnaryPrefixed()) {
         		ot.op = op;
             	for(Entity e : entities) {
             		if(e instanceof Subtree)
             			ot.entities.add( new Subtree( ((Subtree) e).content.transformOp(oldOp, newOp, leftTransform, rightTransform), ((Subtree) e).explicitEnclosing ) );
             		else // e instanceof RawString
             			ot.entities.add(e);
             	}
             	return ot;
         	}
         	
         	// op == oldOp here 
         	ot.op = newOp;
         	boolean first = true;
         	for(Entity e : entities) {
         		if(e instanceof Subtree)
         			e = new Subtree( ((Subtree) e).content.transformOp(oldOp, newOp, leftTransform, rightTransform), ((Subtree) e).explicitEnclosing );
         		if(first) {
         			if(leftTransform != null) e = leftTransform.eval(e);
         		} else {
         			if(rightTransform != null) e = rightTransform.eval(e);
         		}
         		ot.entities.add(e);
         		first = false;
         	}
         	return ot;
         }
         
         static Function<Entity,Entity> DoPrefixByOp(final String op) {
         	return new Function<Entity,Entity>() {
         		public Entity eval(Entity obj) { return new Subtree(obj.prefixed(op)); }
         	};
         }
         
         OperatorTree transformMinusToPlus() {
 			return transformOp("-", "+", null, DoPrefixByOp("-"));
         }
         
         OperatorTree transformMinusPushedDown() {
         	Entity e = transformMinusPushedDown(false);
         	if(e instanceof Subtree)
         		return ((Subtree) e).content;
         	return new OperatorTree("", e);
         }
         Entity transformMinusPushedDown(boolean negate) {
         	if(canBeInterpretedAsUnaryPrefixed() && op.equals("-")) {        		
         		Entity e = unaryPrefixedContent();
         		negate = !negate;
         		if(e instanceof Subtree)
         			return ((Subtree) e).content.transformMinusPushedDown(negate);
         		if(negate)
         			return new Subtree(e.prefixed("-"));
         		return e;
         	}
         	else {
         		OperatorTree ot = new OperatorTree();
         		ot.op = op;
         		for(Entity e : entities) {
         			if(e instanceof Subtree) {
         				Subtree origSubtree = (Subtree) e;
         				e = origSubtree.content.transformMinusPushedDown(negate);
         				if(e instanceof Subtree)
         					((Subtree) e).explicitEnclosing = origSubtree.explicitEnclosing;
         			}
         			else {
         				if(negate)
         					e = new Subtree(e.prefixed("-"));
         			}
         			ot.entities.add(e);
         			// don't negate further entries if this is a multiplication/division
         			if(op.equals("∙") || op.equals("/")) negate = false;
         		}
         		return new Subtree(ot);
         	}
         }
         
 		OperatorTree sum(OperatorTree other) {
 			if(this.isZero()) return other;
 			if(other.isZero()) return this;
 
 			if((this.op.equals("+") || this.entities.size() == 1) && (other.op.equals("+") || other.entities.size() == 1))
 				return Sum(new LinkedList<Entity>(concatCollectionView(entities, other.entities)));
 						
 			if(this.op.equals("+"))
 				return Sum(new LinkedList<Entity>(concatCollectionView(this.entities, listFromArgs(other.asEntity()))));
 
 			if(other.op.equals("+"))
 				return Sum(new LinkedList<Entity>(concatCollectionView(listFromArgs(this.asEntity()), other.entities)));
 
 			return Sum(listFromArgs(this.asEntity(), other.asEntity()));
 		}
 
         OperatorTree minusOne() {
         	if(canBeInterpretedAsUnaryPrefixed() && op.equals("-"))
         		return unaryPrefixedContent().asTree();
         	
         	Entity e = transformMinusPushedDown(true);
         	return e.asTree();
 		}
 
 		Entity asEntity() {
 			if(entities.size() == 1)
 				return entities.get(0);
 			return new Subtree(this);
 		}
         
 		OperatorTree divide(OperatorTree other) {
 			if(isZero()) return this;
 			if(other.isOne()) return this;
 			if(other.isNumber(-1)) return this.minusOne();
 			{
 				Integer thisNum = this.asNumber(), otherNum = other.asNumber();
 				if(thisNum != null && otherNum != null && thisNum % otherNum != 0)
 					return Number(thisNum / otherNum);
 			}
 			if(op.equals("/")) {
 				if(entities.size() == 2)
 					return new OperatorTree("/", listFromArgs(entities.get(0), entities.get(1).asTree().multiply(other).asEntity()));
 				OperatorTree ot = new OperatorTree(op);
 				for(Entity e : entities) ot.entities.add(e);
 				ot.entities.add(other.asEntity());
 				return ot;
 			}
 			return new OperatorTree("/", listFromArgs(asEntity(), other.asEntity()));
 		}
 		
 		OperatorTree mergeDivisions() {
 			if(entities.size() == 1) {
 				Entity e = entities.get(0);
 				if(e instanceof Subtree)
 					return ((Subtree)e).content.mergeDivisions();
 				return this;
 			}
 			
 			if(op.equals("∙")) {
 				OperatorTree nom = One(), denom = One();
 				for(Entity e : entities) {
 					OperatorTree ot = e.asTree().mergeDivisions();
 					if(ot.op.equals("/")) {
 						int i = 0;
 						for(Entity e2 : ot.entities) {
 							if(i == 0) nom = nom.multiply(e2.asTree());
 							else denom = denom.multiply(e2.asTree());
 							i++;
 						}
 					}
 					else nom = nom.multiply(ot);
 				}
 				return nom.divide(denom);
 			}
 			else if(op.equals("/")) {
 				OperatorTree nom = One(), denom = One();
 				int i = 0;
 				for(Entity e : entities) {
 					OperatorTree ot = e.asTree().mergeDivisions();
 					if(ot.op.equals("/")) {
 						int j = 0;
 						for(Entity e2 : ot.entities) {
 							if(j == 0 && i == 0 || j > 0 && i > 0) nom = nom.multiply(e2.asTree());
 							else denom = denom.multiply(e2.asTree());
 							j++;
 						}
 					}
 					else {
 						if(i == 0) nom = nom.multiply(e.asTree());
 						else denom = denom.multiply(e.asTree());
 					}
 					i++;
 				}
 				return nom.divide(denom);
 			}
 			else {
 				OperatorTree ot = new OperatorTree(op);
 				for(Entity e : entities)
 					ot.entities.add(e.asTree().mergeDivisions().asEntity());
 				return ot;
 			}
 		}
 		
 		OperatorTree canRemoveFactor(OperatorTree fac) {
 			if(equals(fac)) return One();
 			if(minusOne().equals(fac)) return Number(-1);
 			if(entities.size() == 1) {
 				if(entities.get(0) instanceof Subtree)
 					return ((Subtree) entities.get(0)).content.canRemoveFactor(fac);
 				return null;
 			}
 			
 			OperatorTree ot = new OperatorTree(op);
 			boolean needOne;
 			if(op.equals("∙")) needOne = true;
 			else if(op.equals("/")) {
 				if(entities.isEmpty()) return null;
 				OperatorTree sub = entities.get(0).asTree().canRemoveFactor(fac);
 				if(sub == null) return null;
 				ot.entities.add(sub.asEntity());
 				for(int i = 1; i < entities.size(); ++i) ot.entities.add(entities.get(i));
 				return ot;
 			}
 			else needOne = false;
 			boolean haveOne = false;
 			int numfac = 1;
 			for(Entity e : entities) {
 				if(needOne && haveOne) {
 					ot.entities.add(e);
 					continue;
 				}
 				OperatorTree sub = e.asTree().canRemoveFactor(fac);
 				if(sub == null) {
 					if(!needOne) return null; // we need all but this one does not have the factor
 					ot.entities.add(e);
 					continue;
 				}
 				haveOne = true;
 				if(op.equals("∙") && sub.asNumber() != null)
 					numfac *= sub.asNumber();
 				else
 					ot.entities.add(sub.asEntity());
 			}
 			if(needOne && !haveOne && !entities.isEmpty()) return null;
 			if(op.equals("∙") && numfac != 1) {
 				if(!ot.entities.isEmpty()) ot.entities.set(0, ot.entities.get(0).asTree().multiply(Number(numfac)).asEntity());
 				else ot = Number(numfac);
 			}
 			return ot;
 		}
 		
 		static Pair<Integer,Integer> simplifyDivisionFactor(List<Entity> nomProd, List<Entity> denomProd) {
 			Pair<Integer,Integer> nomDenomFacs = new Pair<Integer,Integer>(1,1);
 			int fac = 1;
 			for(int i = 0; i < nomProd.size(); ++i) {
 				Entity e = nomProd.get(i);
 				if(e.asTree().isNegative()) {
 					nomProd.set(i, e.asTree().minusOne().asEntity());
 					fac *= -1;
 					nomDenomFacs.first *= -1;
 					e = nomProd.get(i);
 				}
 				nomProd.set(i, e.asTree().simplify().asEntity());
 			}
 			for(int i = 0; i < denomProd.size(); ++i) {
 				Entity e = denomProd.get(i);
 				if(e.asTree().isNegative()) {
 					denomProd.set(i, e.asTree().minusOne().asEntity());
 					fac *= -1;
 					nomDenomFacs.second *= -1;
 					e = denomProd.get(i);
 				}
 				denomProd.set(i, e.asTree().simplify().asEntity());
 			}
 			if(fac == -1) {
 				if(nomProd.isEmpty()) nomProd.add(Number(-1).asEntity());
 				else nomProd.set(0, nomProd.get(0).asTree().minusOne().asEntity());
 				nomDenomFacs.first *= -1;
 			}
 			return nomDenomFacs;
 		}
 		
 		OperatorTree asSum() {
 			if((op.equals("+") || op.equals("-")) && !canBeInterpretedAsUnaryPrefixed()) return this;
 			return Sum(listFromArgs(asEntity()));
 		}
 		
 		OperatorTree asProduct() {
 			if(op.equals("∙")) return this;
 			return Product(listFromArgs(asEntity()));
 		}
 		
 		OperatorTree firstProductInSum() { // expecting that we are a sum
 			if(entities.isEmpty()) return null;
 			if(op.equals("+") || op.equals("-")) return entities.get(0).asTree().asProduct();
 			return null;
 		}
 		
 		OperatorTree simplifyDivision() {
 			if(op.equals("/") && entities.size() == 2) {
 				OperatorTree nom = entities.get(0).asTree().asSum().copy(), denom = entities.get(1).asTree().asSum().copy();
 				OperatorTree nomProd = nom.firstProductInSum();
 				if(nomProd == null) return this; // somehow illformed -> cannot simplify
 				OperatorTree denomProd = denom.firstProductInSum();
 				if(denomProd == null) return this; // cannot simplify because it is undefined (division by zero)				
 				Pair<Integer,Integer> nomDenomFac = simplifyDivisionFactor(nomProd.entities, denomProd.entities);
 				nom.entities.set(0, nomProd.asEntity());
 				denom.entities.set(0, denomProd.asEntity());
 				if(nomDenomFac.first != 1) {
 					for(int i = 1; i < nom.entities.size(); ++i)
 						nom.entities.set(i, nom.entities.get(i).asTree().multiply(Number(nomDenomFac.first)).asEntity());
 				}
 				if(nomDenomFac.second != 1) {
 					for(int i = 1; i < denom.entities.size(); ++i)
 						denom.entities.set(i, denom.entities.get(i).asTree().multiply(Number(nomDenomFac.second)).asEntity());
 				}
 				
 				//System.out.println("div: " + nomProd + " / " + denomProd);
 				for(Entity e : new ArrayList<Entity>(concatCollectionView(denomProd.entities, listFromArgs(denom.asEntity())))) {
 					OperatorTree newNom = nom.canRemoveFactor(e.asTree());
 					OperatorTree newDenom = denom.canRemoveFactor(e.asTree());
 					//System.out.println("removing " + e.asTree().debugStringDouble() + " from " + nom.debugStringDouble() + ": " + newNom);
 					//System.out.println("removing " + e.asTree().debugStringDouble() + " from " + denom.debugStringDouble() + ": " + newDenom);
 					
 					if(newNom != null && newDenom != null) {
 						nom = newNom.asSum(); denom = newDenom.asSum();
 						nomProd = nom.firstProductInSum();
 						denomProd = denom.firstProductInSum();
 						if(nomProd == null || denomProd == null) break;
 					}
 				}
 				
 				return nom.divide(denom);
 			}
 			return this;
 		}
 		
 		OperatorTree nextDivision() {
 			if(entities.isEmpty()) return null;
 			if(op.equals("/")) return entities.get(entities.size()-1).asTree();
 			for(Entity e : entities) {
 				if(e instanceof Subtree) {
 					OperatorTree next = ((Subtree) e).content.nextDivision();
 					if(next != null) return next;
 				}
 			}
 			return null;
 		}
 		
         OperatorTree multiplyAllDivisions() {
         	OperatorTree ot = this;
         	OperatorTree nextDiv = null;
         	while((nextDiv = ot.nextDivision()) != null)
         		ot = ot.multiply(nextDiv);
         	return ot;
         }
         
         boolean matchDenominatorInDiv(OperatorTree denom) {
         	return op.equals("/") && entities.size() > 1 && entities.get(entities.size()-1).asTree().equals(denom);
         }
         
         boolean haveDenominatorInSubtree(OperatorTree denom) {
         	if(matchDenominatorInDiv(denom)) return true;
         	for(Entity e : entities) {
         		if(e instanceof Subtree && e.asTree().haveDenominatorInSubtree(denom))
         			return true;
         	}
         	return false;
         }
         
         OperatorTree multiply(OperatorTree other) {
         	if(isZero()) return this;
         	if(isOne()) return other;
         	if(isNumber(-1)) return other.minusOne();
         	if(other.isOne()) return this;
         	if(other.isNumber(-1)) return this.minusOne();
        	
         	OperatorTree ot = new OperatorTree(op);
 
         	if(op.equals("∙")) {
         		for(int i = 0; i < entities.size(); ++i) {
         			if(entities.get(i).asTree().haveDenominatorInSubtree(other)) {
         				for(int j = 0; j < entities.size(); ++j) {
         					if(i != j)
         						ot.entities.add(entities.get(j));
         					else
         						ot.entities.add(entities.get(j).asTree().multiply(other).asEntity());
         				}
         				return ot;
         			}
         		}
         		ot.entities.addAll(entities);
         		if(other.op.equals(op))
         			ot.entities.addAll(other.entities);
         		else
         			ot.entities.add(other.asEntity());
         		return ot;
         	}
         	
        	if(canBeInterpretedAsUnaryPrefixed()) {
         		Entity e = unaryPrefixedContent();
         		if(e instanceof Subtree)
         			return ((Subtree) e).content.multiply(other).prefixed(op);
         		ot.op = "∙";
         		ot.entities.add(asEntity());
         		ot.entities.add(other.asEntity());
         		return ot;
         	}
         	
         	if(entities.size() == 1) {
         		Entity e = entities.get(0);
         		if(e instanceof Subtree)
         			return ((Subtree) e).content.multiply(other);
         		ot.op = "∙";
         		ot.entities.add(e);
         		ot.entities.add(other.asEntity());
         		return ot;
         	}
 
         	if(op.equals("/")) {
         		if(entities.isEmpty())
         			ot.entities.add(other.asEntity());
         		else if(matchDenominatorInDiv(other)) {
         			ot.entities.addAll(entities);
         			ot.entities.remove(entities.size()-1);
         			if(ot.entities.size() == 1)
         				ot = ot.entities.get(0).asTree();
         		}
         		else {
         			boolean first = true;
                 	for(Entity e : entities) {
                 		if(first)
                 			ot.entities.add(e.asTree().multiply(other).asEntity());
                 		else
                 			ot.entities.add(e);
                 		first = false;
                 	}
         		}
         		return ot;
         	}
         	
         	// +, - or whatever else -> mult each entry
         	for(Entity e : entities)
         		ot.entities.add(e.asTree().multiply(other).asEntity());
         	return ot;
         }
         
         OperatorTree pushdownMultiplication(OperatorTree other) {
         	if(this.isOne()) return other;
 			if((op.equals("+") || op.equals("-")) && !canBeInterpretedAsUnaryPrefixed()) {
 				OperatorTree newOt = new OperatorTree(op);
 				for(Entity e : entities) {
 					OperatorTree ot = e.asTree().pushdownMultiplication(other);
 					if(ot.op.equals("+"))
 						newOt.entities.addAll(ot.entities);
 					else
 						newOt.entities.add(ot.asEntity());
 				}
 				return newOt;
 			}
 			if(other.op.equals("+") || other.op.equals("-") && !other.canBeInterpretedAsUnaryPrefixed()) {
 				OperatorTree newOt = new OperatorTree(other.op);
 				for(Entity e : other.entities) {
 					OperatorTree ot = pushdownMultiplication(e.asTree());
 					if(ot.op.equals("+"))
 						newOt.entities.addAll(ot.entities);
 					else
 						newOt.entities.add(ot.asEntity());
 				}
 				return newOt;
 			}
 			if(canBeInterpretedAsUnaryPrefixed() && op.equals("-") && other.canBeInterpretedAsUnaryPrefixed() && other.op.equals("-")) {
 				return unaryPrefixedContent().asTree().pushdownMultiplication(other.unaryPrefixedContent().asTree());
 			}
 			if(op.equals("∙")) {
 				OperatorTree newOt = new OperatorTree(op);
 				newOt.entities.addAll(entities);
 				if(other.op.equals("∙"))
 					newOt.entities.addAll(other.entities);
 				else
 					newOt.entities.add(other.asEntity());
 				return newOt;
 			}
         	return multiply(other);
         }
         
         OperatorTree pushdownAllMultiplications() {
         	if(op.equals("∙")) {
         		OperatorTree ot = One();
         		for(Entity e : entities)
         			ot = ot.pushdownMultiplication(e.asTree().pushdownAllMultiplications());
         		return ot;
         	}
         	else {
 	        	OperatorTree ot = new OperatorTree(op);
 	        	for(Entity e : entities) {
 	        		if(e instanceof Subtree) {
 	        			OperatorTree subtree = ((Subtree) e).content.pushdownAllMultiplications();
 	        			if(op.equals("+") && subtree.op.equals("+"))
 	        				ot.entities.addAll(subtree.entities);
 	        			else
 	        				ot.entities.add(subtree.asEntity());
 	        		}
 	        		else
 	        			ot.entities.add(e);
 	        	}
 	        	return ot;
         	}
         }
         
         static abstract class PositionInfo {
         	int absolutePos = 0;
         	BetweenEntities parent = null;
         	
         	abstract PositionInfo next();
         	int prefixLen() { return 0; }
         	int postfixLen() { return 0; }
         	BetweenEntities currentTreePos() { return parent; }
         	
         	PositionInfo nextInStack() {
         		if(parent == null) return null;
         		BetweenEntities pos = new BetweenEntities();
         		pos.parent = parent.parent;
         		pos.ot = parent.ot;
         		pos.index = parent.index + 1;
         		pos.absolutePos = absolutePos + postfixLen();
         		return pos;
         	}
         	
             static class StringPos extends PositionInfo {
             	RawString rs;
             	int pos;            	
 
             	@Override PositionInfo next() {
 					if(pos >= rs.content.length()) return nextInStack();
 					StringPos pos = new StringPos();
 					pos.absolutePos = absolutePos + 1;
 					pos.parent = parent;
 					pos.rs = rs;
 					pos.pos = this.pos + 1;
 					return pos;
 				}
             }
             static class BetweenEntities extends PositionInfo {
             	OperatorTree ot;
             	int index; // of following index. 0 .. ot.entities.size()
             	boolean beforeOp = true;
             	@Override int prefixLen() { return 1; /* bracket '(' */ }
             	@Override int postfixLen() { return 1; /* bracket ')' */ }
             	@Override BetweenEntities currentTreePos() { return this; }
             	@Override PositionInfo next() {
 					if(index >= ot.entities.size()) return nextInStack();
 					if(beforeOp) {
 						BetweenEntities pos = new BetweenEntities();
 						pos.ot = ot;
 						pos.index = index;
 						pos.beforeOp = false;
 						pos.parent = parent;
 						pos.absolutePos = absolutePos + 1/*space*/ + ot.op.length() + 1/*space*/;
 						return pos;
 					}
 					Entity nextEntity = ot.entities.get(index);
 					PositionInfo pos = null;
 					if(nextEntity instanceof RawString) {
 						StringPos spos = new StringPos();
 						spos.rs = (RawString) nextEntity;
 						spos.pos = 0;
 						pos = spos;
 					}
 					else {
 						BetweenEntities spos = new BetweenEntities();
 						spos.ot = ((Subtree) nextEntity).content;
 						spos.index = 0;
 						pos = spos;
 					}
 					pos.absolutePos = absolutePos + pos.prefixLen();
 					pos.parent = this;
             		return pos;
             	}
             }
             
             static PositionInfo start(OperatorTree ot) {
             	BetweenEntities pos = new BetweenEntities();
             	pos.ot = ot;
             	return pos;
             }
             
             static PositionInfo posAt(OperatorTree ot, int pos) {
             	PositionInfo p = start(ot);
             	while(p.absolutePos < pos) {
             		PositionInfo next = p.next();
             		if(next == null) return p; // guarantees that we return something != null
             		p = next;
             	}
             	return p;
             }
         }
         
         PositionInfo posAt(int pos) { return PositionInfo.posAt(this, pos); }
         
         Iterator<PositionInfo> positionIterator() {
         	return new Iterator<PositionInfo>() {
         		PositionInfo next = PositionInfo.start(OperatorTree.this);        		
 				public boolean hasNext() { return next != null; }
 				public PositionInfo next() {
 					if(!hasNext()) throw new NoSuchElementException();
 					PositionInfo res = next;
 					next = next.next();
 					return res;
 				}
 				public void remove() { throw new UnsupportedOperationException(); }        	
 			};
         }
                 
         Iterable<PositionInfo> position() {
         	return new Iterable<PositionInfo>() {
         		public Iterator<PositionInfo> iterator() { return positionIterator(); }
 			};
         }
 
     }
 
 	static void debugUtilsParsingOpTree(String s) {
 		OperatorTree.debugOperatorTreeDump = true;
 		OperatorTree ot = OperatorTree.parse(s); 
 		String debugStr = ot.toString();
 		OperatorTree.debugOperatorTreeDump = false;
 		String normalStr = ot.toString();
 		String simplifiedStr = ot.simplify().toString();
 		System.out.println("parsed " + s + " -> " + debugStr + " -> " + normalStr + " -> " + simplifiedStr);
 	}
 
 	static void debugUtilsParsingOpTree() {
 		debugUtilsParsingOpTree("a * b = c");
 		debugUtilsParsingOpTree("a + b * d");
 		debugUtilsParsingOpTree("1 + 2 - 3 - 4 + 5");
 		debugUtilsParsingOpTree("(1 + 2) + (3 + 4) - 5");
 		debugUtilsParsingOpTree("(1 + 2) - (3) - 4");
 		debugUtilsParsingOpTree("(1 + 2) + (3 + 4) (5 + 6)");
 		debugUtilsParsingOpTree("1 + 2 (3)");
 		debugUtilsParsingOpTree("1 + 2 3 / 4 * 5");
 		debugUtilsParsingOpTree("1 = (2 + 3)");
 		debugUtilsParsingOpTree("a + b = c + d");
 		debugUtilsParsingOpTree("(a + b) = c + d");
 		debugUtilsParsingOpTree("a = (b * c) + d");
 		debugUtilsParsingOpTree("a + (b * )");
 		debugUtilsParsingOpTree("a * -b");
 		debugUtilsParsingOpTree("a * --b");
 		debugUtilsParsingOpTree("a * -+-b");
 		debugUtilsParsingOpTree("1 + -2 + 3");
 		debugUtilsParsingOpTree("1 + -(2 + 3) + 4");
 		debugUtilsParsingOpTree("a - (b - c)");
 		debugUtilsParsingOpTree("a + +b");
 	}
     
     
 }
