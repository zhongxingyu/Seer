 /*
  * Copyright (c) 2011, Ken Gilmer
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
  * in the documentation and/or other materials provided with the distribution. Neither the name of the Ken Gilmer nor the names 
  * of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
  * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.sprinkles;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Map a function from one list to another. A way of applying a function to list
  * and tree data structures. See
  * http://en.wikipedia.org/wiki/Map_(higher-order_function)
  * 
  * @author kgilmer
  * 
  */
 public class Fn {
 
 	/**
 	 * Apply a function to an object and return an object.
 	 * 
 	 * @author kgilmer
 	 * 
 	 */
 	public interface Function<I, O> {
 		/**
 		 * @param <T>
 		 * @param element
 		 * @return result of function. If null is returned nothing is added to
 		 *         result.
 		 */
 		public O apply(I element);
 	}
 
 	/**
 	 * A function in a fold operation.
 	 * 
 	 * @author kgilmer
 	 * 
 	 */
 	public interface FoldFunction<I, O> {
 		/**
 		 * @param element
 		 * @param result
 		 *            - result from previous application of function.
 		 * @return
 		 */
 		public O apply(I element, O result);
 	}
 
 	/**
 	 * Map a function over an input. Will recurse if any element is also
 	 * Iterable.
 	 * 
 	 * @param function
 	 *            to apply to input.
 	 * @param input
 	 *            element or Collection to apply the function.
 	 *            If input is null, function is not executed and empty list is returned.
 	 *            
 	 * @return collection of results of execution of function. If null is
 	 *         returned from function, nothing is added.
 	 */
 	public static <I, O> Collection<O> map(Function<I, O> function, Object input) {
 		if (input == null)
 			return Collections.emptyList();
 		
 		Collection<O> out = new ArrayList<O>();
 		Iterable in;
 
 		// If the input is iterable, treat as such, otherwise apply function to
 		// single element.
 		if (input instanceof Iterable)
 			in = (Iterable) input;
		else if (input instanceof Object[])
			in = Arrays.asList((Object []) input);
		else 
 			in = Arrays.asList(input);
 
 		applyMap(function, in, out, false, true, true);
 
 		return Collections.unmodifiableCollection(out);
 	}
 	
 	/**
 	 * Map a function over an input, adding results to a client-provided output collection.
 	 * @param <I>
 	 * @param <O>
 	 * @param function
 	 * @param input
 	 * @param out
 	 */
 	public static <I, O> void map(Function<I, O> function, Object input, Collection<O> out) {		
 		Iterable in;
 
 		// If the input is iterable, treat as such, otherwise apply function to
 		// single element.
 		if (input instanceof Iterable)
 			in = (Iterable) input;
 		else
 			in = Arrays.asList(input);
 
 		applyMap(function, in, out, false, true, true);
 	}
 
 	/**
 	 * The map function.
 	 * 
 	 * @param function
 	 *            Function to be applied
 	 * @param input
 	 *            Collection to apply the function to.
 	 * @param collection
 	 *            Stores the result of function application.
 	 * @param stopFirstMatch
 	 *            Return (stop recursing) after first time function returns
 	 *            non-null value.
 	 * @param adaptMap
 	 *            Inspect input types, if is a Map, iterate over values of map.
 	 * @param recurse
 	 *            Call apply on any elements of collection that are iterable.
 	 */
 	private static <I, O> void applyMap(Function<I, O> function, Iterable input, Collection<O> collection, boolean stopFirstMatch, boolean adaptMap, boolean recurse) {
 		for (Object child : input) {
 			boolean isIterable = child instanceof Collection;
 
 			/*if (adaptMap)
 				throw new RuntimeException("Adapt Map is broken!");*/
 			if (!isIterable && adaptMap) {
 				if (child instanceof Map) {
 					child = (I) ((Map) child).values();
 					isIterable = true;
 				}
 			}
 
 			if (isIterable && recurse) {
 				
 				applyMap(function, (Iterable) child, collection, stopFirstMatch, adaptMap, recurse);
 			} else {
 				O result = function.apply((I) child);
 
 				if (result != null) {
 					collection.add(result);
 
 					if (stopFirstMatch) {
 						return;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * Apply a function to a list of elements, passing the result of the
 	 * previous call on to the next.
 	 * 
 	 * @param function
 	 * @param elements
 	 * @return
 	 */
 	public static <I, O> O fold(FoldFunction<I, O> function, Object input) {
 		Collection in;
 
 		// If the input is iterable, treat as such, otherwise apply function to
 		// single element.
 		if (input instanceof Collection)
 			in = (Collection) input;
 		else
 			in = Arrays.asList(input);
 
 		return applyFold(function, in, null, true, true);
 	}
 
 	/**
 	 * @param function
 	 * @param input
 	 * @param result
 	 * @param adaptMap
 	 * @param recurse
 	 * @return
 	 */
 	private static <I, O> O applyFold(FoldFunction<I, O> function, Iterable input, O result, boolean adaptMap, boolean recurse) {
 		for (Object child : input) {
 			boolean isIterable = child instanceof Collection;
 
 			if (!isIterable && adaptMap) {
 				if (child instanceof Map) {
 					child = ((Map) child).values();
 					isIterable = true;
 				}
 			}
 
 			if (isIterable && recurse) {
 				result = applyFold(function, (Iterable) child, result, adaptMap, recurse);
 			} else {
 				result = function.apply((I) child, result);
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Apply function to element until one function returns non-null value. If
 	 * no function matches, returns null, otherwise returns results of
 	 * Function.apply().
 	 * 
 	 * @param function
 	 * @param input input passed to function, if null is passed, null is returned without evaluation.
 	 * @return
 	 */
 	public static <I, O> O find(Function<I, O> function, Object input) {
 		if (input == null)
 			return null;
 		
 		Collection<O> out = new ArrayList<O>();
 		Collection in;
 		if (input instanceof Collection)
 			in = (Collection) input;
 		else
 			in = Arrays.asList(input);
 
 		Fn.applyMap(function, in, out, true, true, true);
 
 		if (out.size() > 1) {
 			throw new RuntimeException("call to apply with stopFirstMatch should never add more than one element to collection.");
 		} else if (out.size() == 1) {
 			return out.iterator().next();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Apply collection of functions in order for each element of input (depth
 	 * first). A result that is also Iterable will cause the function to
 	 * recurse.
 	 * 
 	 * @param functions
 	 * @param input input to evaluate, if null passed in empty set is returned and no evaluation occurs.
 	 * @return
 	 */
 	public static Collection map(Collection<Function> functions, Object input) {
 		if (input == null)
 			return Collections.emptyList();
 		
 		Collection results = new ArrayList();
 		Collection ic = null;
 		if (input instanceof Collection) {
 			ic = (Collection) input;
 		} else {
 			ic = new ArrayList();
 
 			ic.add(input);
 		}
 
 		mapDepthFirst(new ArrayList(functions), ic, results, 0);
 
 		return results;
 	}
 
 	/**
 	 * Internal function that does the depth-first match operation.
 	 * 
 	 * @param functions
 	 * @param input
 	 * @param results
 	 * @param fi
 	 */
 	private static void mapDepthFirst(List<Function> functions, Object input, Collection results, int fi) {
 		Function f = functions.get(fi);
 
 		if (input instanceof Collection) {
 			for (Object elem : (Collection) input) {
 				Object result = f.apply(elem);
 
 				if (result == null) {
 					continue;
 				}
 
 				if (fi < functions.size() - 1) {
 					mapDepthFirst(functions, result, results, fi + 1);
 				} else {
 					results.add(result);
 				}
 			}
 		} else {
 			Object result = f.apply(input);
 
 			if (result == null) {
 				return;
 			}
 
 			if (fi < functions.size() - 1) {
 				mapDepthFirst(functions, result, results, fi + 1);
 			} else {
 				results.add(result);
 			}
 		}
 	}
 }
