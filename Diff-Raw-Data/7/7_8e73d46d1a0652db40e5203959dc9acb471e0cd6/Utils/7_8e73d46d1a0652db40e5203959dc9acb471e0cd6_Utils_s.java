 package applets.Termumformungen$in$der$Technik_01_URI;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.Stack;
 
 public class Utils {
 
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
 	
     static <X,Y> List<Y> map(Iterable<? extends X> coll, Function<X,Y> func) {
     	List<Y> l = new LinkedList<Y>();
     	for(X obj : coll) l.add(func.eval(obj));
     	return l;
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
     
     static class OperatorTree {
     	String op = "";
     	abstract static class Entity {
     		OperatorTree asTree() { return new OperatorTree("", this); }
     		OperatorTree prefixed(final String op) {
 				// empty subtree at the beginning is the mark for a prefix op
     			OperatorTree ot = new OperatorTree(op, new Subtree(new OperatorTree()));
     			ot.entities.add(this);
     			return ot;
     		}
     	}
     	static class RawString extends Entity {
     		String content = "";
     		RawString() {}
     		RawString(String s) { content = s; }
     		@Override public String toString() { return debugOperatorTreeDump ? ("{" + content + "}") : content; }
     	}
     	static class Subtree extends Entity {
     		OperatorTree content;
     		Subtree(OperatorTree t) { content = t; }
     		@Override public String toString() { return (content.canBeInterpretedAsUnaryPrefixed() && !debugOperatorTreeDump) ? content.toString() : ("(" + content.toString() + ")"); }
     		@Override OperatorTree asTree() { return content; }
     	}
     	List<Entity> entities = new LinkedList<Entity>();
     	
     	OperatorTree() {}
     	OperatorTree(String op, Entity e) { this.op = op; entities.add(e); } 
     	OperatorTree(String op, List<Entity> entities) { this.op = op; this.entities = entities; }    	
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
     			return new Subtree(parseOpsInTree(((Subtree)source).content, binOps, binOpList));
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
     					ot.entities.add(new Subtree(parseOpsInTree( ((Subtree) e).content, binOps, binOpList )));
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
     				ot.entities.add( new Subtree( parseDefaultAnonBinOpInTree( ((Subtree) e).content, defaultOp ) ) );
     		}
     		return ot;
     	}
     	    	
     	static OperatorTree parse(OperatorTree t, List<Set<String>> binOpList, String defaultAnonBinOp) {
     		Set<String> binOps = new HashSet<String>();
     		for(Set<String> ops : binOpList)
     			binOps.addAll(ops);
     			
     		t = parseOpsInTree(t, binOps, binOpList);
     		//System.out.println("before defaultop: " + t);
     		t = parseDefaultAnonBinOpInTree(t, defaultAnonBinOp);
     		return t;
     	}
     	
     	static OperatorTree undefinedOpTreeFromParseTree(ParseTree t) {
     		OperatorTree ot = new OperatorTree();
     		for(ParseTree.Entity e : t.entities) {
     			if(e instanceof ParseTree.RawString)
     				ot.entities.add( new RawString(((ParseTree.RawString)e).content) );
     			else
     				ot.entities.add( new Subtree( undefinedOpTreeFromParseTree(((ParseTree.Subtree)e).content) ) );
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
     	static OperatorTree parse(String str) { return parse(str.replace('*', '∙').replace(',', '.'), "= +- ∙/", "∙"); }
     	
     	boolean canBeInterpretedAsUnaryPrefixed() {
     		return entities.size() == 2 && entities.get(0) instanceof Subtree && ((Subtree)entities.get(0)).content.entities.isEmpty();	
     	}
     	
     	Entity unaryPrefixedContent() {
     		if(!canBeInterpretedAsUnaryPrefixed()) throw new AssertionError("we expect the OT to be unary prefixed");
     		return entities.get(1);
     	}
     	
     	@Override public String toString() {
     		if(debugOperatorTreeDump)
         		return "[" + op + "] " + concat(entities, ", ");    			
     		if(canBeInterpretedAsUnaryPrefixed())
     			// this is a special case used for unary ops (or ops which look like those)
     			return op + unaryPrefixedContent();
     		return concat(entities, " " + op + " ");
     	}
         static boolean debugOperatorTreeDump = false;
         
         OperatorTree mergeOps(Set<String> ops) {
         	if(entities.size() == 1 && entities.get(0) instanceof Subtree)
         		return ((Subtree) entities.get(0)).content.mergeOps(ops);
         	
         	OperatorTree ot = new OperatorTree();
         	ot.op = op;
         	for(Entity e : entities) {
         		if(e instanceof RawString)
         			ot.entities.add(e);
         		else {
         			OperatorTree subtree = ((Subtree) e).content.mergeOps(ops);
        			if(subtree.op.equals(ot.op) && ops.contains(op))
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
        			if(first && subtree.op.equals(ot.op) && ops.contains(op))
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
         OperatorTree simplify() { return mergeOps("=+∙").mergeOpsFromRight("-/"); }
         
         OperatorTree transformOp(String oldOp, String newOp, Function<Entity,Entity> leftTransform, Function<Entity,Entity> rightTransform) {
         	OperatorTree ot = new OperatorTree();
         	if(!op.equals(oldOp) || canBeInterpretedAsUnaryPrefixed()) {
         		ot.op = op;
             	for(Entity e : entities) {
             		if(e instanceof Subtree)
             			ot.entities.add( new Subtree( ((Subtree) e).content.transformOp(oldOp, newOp, leftTransform, rightTransform) ) );
             		else // e instanceof RawString
             			ot.entities.add(e);
             	}
             	return ot;
         	}
         	
         	ot.op = newOp;
         	boolean first = true;
         	for(Entity e : entities) {
         		if(e instanceof Subtree)
         			e = new Subtree( ((Subtree) e).content.transformOp(oldOp, newOp, leftTransform, rightTransform) );
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
         
         static Function<Entity,Entity> prefixByOp(final String op) {
         	return new Function<Entity,Entity>() {
         		public Entity eval(Entity obj) { return new Subtree(obj.prefixed(op)); }
         	};
         }
         
         OperatorTree transformMinusToPlus() {
 			return transformOp("-", "+", null, prefixByOp("-"));
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
 	}
     
     
 }
