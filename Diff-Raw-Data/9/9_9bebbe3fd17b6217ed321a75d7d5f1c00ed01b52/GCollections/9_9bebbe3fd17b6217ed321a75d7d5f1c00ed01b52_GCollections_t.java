 /*
 
  IGO Software SL  -  info@igosoftware.es
 
  http://www.glob3.org
 
 -------------------------------------------------------------------------------
  Copyright (c) 2010, IGO Software SL
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
      * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
      * Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
      * Neither the name of the IGO Software SL nor the
        names of its contributors may be used to endorse or promote products
        derived from this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL IGO Software SL BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 -------------------------------------------------------------------------------
 
 */
 
 
 package es.igosoftware.util;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 
 import es.igosoftware.concurrent.GConcurrent;
 
 
 public final class GCollections {
 
    private GCollections() {
    }
 
 
    public static int size(final Iterable<?> iterable) {
       if (iterable instanceof Collection<?>) {
          return ((Collection<?>) iterable).size();
       }
 
       final Iterator<?> iterator = iterable.iterator();
       int count = 0;
       while (iterator.hasNext()) {
          count++;
          iterator.next();
       }
       return count;
    }
 
 
    public static CharSequence toString(final int[] array) {
       final StringBuffer result = new StringBuffer();
 
       result.append("{");
       boolean first = true;
       for (final int each : array) {
          if (first) {
             first = false;
          }
          else {
             result.append(", ");
          }
 
          result.append(each);
       }
       result.append("}");
 
       return result;
    }
 
 
    public static CharSequence toString(final double[] array) {
       final StringBuffer result = new StringBuffer();
 
       result.append("{");
       boolean first = true;
       for (final double each : array) {
          if (first) {
             first = false;
          }
          else {
             result.append(", ");
          }
 
          result.append(each);
       }
       result.append("}");
 
       return result;
    }
 
 
    public static CharSequence toString(final float[] array) {
       final StringBuffer result = new StringBuffer();
 
       result.append("{");
       boolean first = true;
       for (final double each : array) {
          if (first) {
             first = false;
          }
          else {
             result.append(", ");
          }
 
          result.append(each);
       }
       result.append("}");
 
       return result;
    }
 
 
    public static <T> CharSequence toString(final T[] array) {
       //      final StringBuffer result = new StringBuffer();
       //
       //      result.append("{");
       //      boolean first = true;
       //      for (final Object each : array) {
       //         if (first) {
       //            first = false;
       //         }
       //         else {
       //            result.append(", ");
       //         }
       //
       //         result.append(each.toString());
       //      }
       //      result.append("}");
       //
       //      return result;
       return toString(array, "{", "}", ", ");
    }
 
 
    public static <T> CharSequence toString(final T[] array,
                                            final String opening,
                                            final String closing,
                                            final String delimiter) {
       if (array == null) {
          return "null";
       }
 
       final StringBuffer result = new StringBuffer();
 
       result.append(opening);
       boolean first = true;
       for (final T each : array) {
          if (first) {
             first = false;
          }
          else {
             result.append(delimiter);
          }
 
          if (each == null) {
             result.append("null");
          }
          else {
             result.append(each.toString());
          }
       }
       result.append(closing);
 
       return result;
    }
 
 
    public static double median(final List<Double> a) {
       Collections.sort(a);
       if (a.size() == 0) {
          // no elements, just return 0
          return 0;
       }
       else if (a.size() % 2 != 0) {
          // odd-length, return middle element
          return a.get(a.size() / 2);
       }
       else {
          // even-length, return average of middle two elements
          // be sure to divide by 2.0 and not by 2!
          return (a.get(a.size() / 2) + a.get((a.size() / 2) - 1)) / 2.0;
       }
    }
 
 
    public static <T> List<T> reversed(final List<T> list) {
       final ArrayList<T> reversedList = new ArrayList<T>(list);
       Collections.reverse(reversedList);
       return Collections.unmodifiableList(reversedList);
    }
 
 
    public static <T> Iterator<T> reversedIterator(final List<T> list) {
       return reversed(list).iterator();
    }
 
 
    public static <T> List<T> getLast(final List<T> list,
                                      final int count) {
       final int listSize = list.size();
       return list.subList(listSize - count, listSize);
    }
 
 
    public static <T> List<T> getLastUpTo(final List<T> list,
                                          final int count) {
       return getLast(list, Math.min(count, list.size()));
    }
 
 
    public static <T, IT extends Iterable<T>> Iterable<T> flatten(final Iterable<IT> iterables) {
       return new Iterable<T>() {
          @Override
          public Iterator<T> iterator() {
             final ArrayList<Iterator<T>> iterators = new ArrayList<Iterator<T>>();
 
             for (final Iterable<T> iterable : iterables) {
                iterators.add(iterable.iterator());
             }
 
             return new CompositeIterator<T>(iterators);
          }
       };
    }
 
 
    public static <T> Iterable<T> unmodifiable(final Iterable<T> iterable) {
       return new Iterable<T>() {
          @Override
          public Iterator<T> iterator() {
             return new Iterator<T>() {
                final private Iterator<T> iterator = iterable.iterator();
 
 
                @Override
                public boolean hasNext() {
                   return iterator.hasNext();
                }
 
 
                @Override
                public T next() {
                   return iterator.next();
                }
 
 
                @Override
                public void remove() {
                   throw new UnsupportedOperationException();
                }
 
             };
 
          }
       };
    }
 
 
    public static <ElementT, ResultT> List<ResultT> collect(final List<ElementT> list,
                                                            final ITransformer<ElementT, ResultT> transformer) {
       if (list == null) {
          return null;
       }
 
       if (list.isEmpty()) {
          return Collections.emptyList();
       }
 
       if (list.size() == 1) {
          return Collections.singletonList(transformer.transform(list.get(0)));
       }
 
       final List<ResultT> result = new ArrayList<ResultT>(list.size());
 
       for (final ElementT element : list) {
          result.add(transformer.transform(element));
       }
 
       return result;
    }
 
 
    public static int[] collect(final int[] array,
                                final ITransformerIntInt transformer) {
       if (array == null) {
          return null;
       }
 
       final int[] result = new int[array.length];
 
       for (int i = 0; i < array.length; i++) {
          result[i] = transformer.transform(array[i]);
       }
 
       return result;
    }
 
 
    public static byte[] collect(final int[] array,
                                 final ITransformerIntByte transformer) {
       if (array == null) {
          return null;
       }
 
       final byte[] result = new byte[array.length];
 
       for (int i = 0; i < array.length; i++) {
          result[i] = transformer.transform(array[i]);
       }
 
       return result;
    }
 
 
    public static <ElementT, ResultT> Set<ResultT> collect(final Set<ElementT> set,
                                                           final ITransformer<ElementT, ResultT> transformer) {
       if (set == null) {
          return null;
       }
 
       if (set.isEmpty()) {
          return Collections.emptySet();
       }
 
       final Set<ResultT> result = new HashSet<ResultT>(set.size());
 
       for (final ElementT element : set) {
          result.add(transformer.transform(element));
       }
 
       return result;
    }
 
 
    public static <ElementK, ElementV, ResultK, ResultV> Map<ResultK, ResultV> collect(final Map<ElementK, ElementV> map,
                                                                                       final ITransformer<ElementK, ResultK> keyTransformer,
                                                                                       final ITransformer<ElementV, ResultV> valueTransformer) {
       if (map == null) {
          return null;
       }
 
       if (map.isEmpty()) {
          return Collections.emptyMap();
       }
 
       final Map<ResultK, ResultV> result = new HashMap<ResultK, ResultV>(map.size());
 
       for (final Map.Entry<ElementK, ElementV> entry : map.entrySet()) {
          result.put(keyTransformer.transform(entry.getKey()), valueTransformer.transform(entry.getValue()));
       }
 
       return result;
    }
 
 
    public static <ElementT, ResultT> Collection<ResultT> collect(final Collection<ElementT> collection,
                                                                  final ITransformer<ElementT, ResultT> transformer) {
       if (collection == null) {
          return null;
       }
 
       if (collection.isEmpty()) {
          return Collections.emptyList();
       }
 
       final Collection<ResultT> result = new ArrayList<ResultT>(collection.size());
 
       for (final ElementT element : collection) {
          result.add(transformer.transform(element));
       }
 
       return result;
    }
 
 
    public static <ElementT, ResultT> Iterable<ResultT> collect(final Iterable<ElementT> iterable,
                                                                final ITransformer<ElementT, ResultT> transformer) {
       if (iterable == null) {
          return null;
       }
 
       return new Iterable<ResultT>() {
          @Override
          public Iterator<ResultT> iterator() {
             return new TransformIterator<ElementT, ResultT>(iterable.iterator(), transformer);
          }
       };
    }
 
 
    //   public static <ElementT, ResultT> Collection<ResultT> concurrentCollect(final Collection<ElementT> collection,
    //                                                                           final ITransformer<ElementT, ResultT> transformer) {
    //      if (collection == null) {
    //         return null;
    //      }
    //
    //      final int size = collection.size();
    //      if (size == 0) {
    //         return Collections.emptyList();
    //      }
    //
    //      if (size == 1) {
    //         return Collections.singletonList(transformer.transform(collection.iterator().next()));
    //      }
    //
    //
    //      final ExecutorService executor = GConcurrent.getDefaultExecutor();
    //
    //      final List<Future<ResultT>> futuresResult = new ArrayList<Future<ResultT>>(size);
    //
    //      for (final ElementT element : collection) {
    //         final Future<ResultT> future = executor.submit(new Callable<ResultT>() {
    //            @Override
    //            public ResultT call() throws Exception {
    //               return transformer.transform(element);
    //            }
    //         });
    //
    //         futuresResult.add(future);
    //      }
    //
    //      final List<ResultT> result = new ArrayList<ResultT>(size);
    //      for (final Future<ResultT> futureResult : futuresResult) {
    //         try {
    //            result.add(futureResult.get());
    //         }
    //         catch (final InterruptedException e) {
    //            throw new RuntimeException(e);
    //         }
    //         catch (final ExecutionException e) {
    //            throw new RuntimeException(e);
    //         }
    //      }
    //
    //      return result;
    //   }
 
    @SuppressWarnings("unchecked")
    public static <ElementT, ResultT> ResultT[] concurrentCollect(final ElementT[] array,
                                                                  final ITransformer<ElementT, ResultT> transformer) {
       if (array == null) {
          return null;
       }
 
       final int size = array.length;
       if (size == 0) {
          return (ResultT[]) new Object[0];
       }
 
       final List<ElementT> list = Arrays.asList(array);
       final List<ResultT> collected = concurrentCollect(list, transformer);
       return collected.toArray((ResultT[]) new Object[0]);
 
       //      if (size == 1) {
       //         return Collections.singletonList(transformer.transform(list.get(0)));
       //      }
       //
       //      final ArrayList<ResultT> result = new ArrayList<ResultT>(size);
       //      for (int i = 0; i < size; i++) {
       //         result.add(null);
       //      }
       //
       //      concurrentEvaluate(list, new IRangeEvaluator() {
       //         @Override
       //         public void evaluate(final int from,
       //                              final int to) {
       //            //System.out.println("collecting " + from + "->" + to);
       //            for (int i = from; i <= to; i++) {
       //               final ResultT transformed = transformer.transform(list.get(i));
       //               synchronized (result) {
       //                  result.set(i, transformed);
       //               }
       //            }
       //         }
       //      });
       //
       //      return result;
    }
 
 
    public static <ElementT, ResultT> List<ResultT> concurrentCollect(final List<ElementT> list,
                                                                      final ITransformer<ElementT, ResultT> transformer) {
       if (list == null) {
          return null;
       }
 
       final int size = list.size();
       if (size == 0) {
          return Collections.emptyList();
       }
 
       if (size == 1) {
          return Collections.singletonList(transformer.transform(list.get(0)));
       }
 
       final ArrayList<ResultT> result = new ArrayList<ResultT>(size);
       for (int i = 0; i < size; i++) {
          result.add(null);
       }
 
       concurrentEvaluate(list, new IRangeEvaluator() {
          @Override
          public void evaluate(final int from,
                               final int to) {
             //System.out.println("collecting " + from + "->" + to);
             for (int i = from; i <= to; i++) {
                final ResultT transformed = transformer.transform(list.get(i));
                synchronized (result) {
                   result.set(i, transformed);
                }
             }
          }
       });
 
       return result;
    }
 
 
    public static byte[] concurrentCollect(final int[] array,
                                           final ITransformerIntByte transformer) {
       if (array == null) {
          return null;
       }
 
       final int size = array.length;
       if (size == 0) {
          return new byte[0];
       }
 
       if (size == 1) {
          return new byte[] { transformer.transform(array[0]) };
       }
 
       final byte[] result = new byte[size];
 
       concurrentEvaluate(array, new IRangeEvaluator() {
          @Override
          public void evaluate(final int from,
                               final int to) {
             //System.out.println("collecting " + from + "->" + to);
             for (int i = from; i <= to; i++) {
                result[i] = transformer.transform(array[i]);
             }
          }
       });
 
       return result;
 
       //         final ExecutorService executor = GConcurrent.getDefaultExecutor();
       //   
       //         final List<Future<Byte>> futuresResult = new ArrayList<Future<Byte>>(size);
       //   
       //         for (final int element : array) {
       //            final Future<Byte> future = executor.submit(new Callable<Byte>() {
       //               @Override
       //               public Byte call() throws Exception {
       //                  return transformer.transform(element);
       //               }
       //            });
       //   
       //            futuresResult.add(future);
       //         }
       //   
       //         final byte[] result = new byte[size];
       //         for (int i = 0; i < size; i++) {
       //            try {
       //               result[i] = futuresResult.get(i).get();
       //            }
       //            catch (final InterruptedException e) {
       //               throw new RuntimeException(e);
       //            }
       //            catch (final ExecutionException e) {
       //               throw new RuntimeException(e);
       //            }
       //         }
       //   
       //         return result;
    }
 
 
    //   public static <ElementT, ResultT> Set<ResultT> concurrentCollect(final Set<ElementT> set,
    //                                                                    final ITransformer<ElementT, ResultT> transformer) {
    //      if (set == null) {
    //         return null;
    //      }
    //
    //      final int size = set.size();
    //      if (size == 0) {
    //         return Collections.emptySet();
    //      }
    //
    //      if (size == 1) {
    //         return Collections.singleton(transformer.transform(set.iterator().next()));
    //      }
    //
    //
    //      final ExecutorService executor = GConcurrent.getDefaultExecutor();
    //
    //      final List<Future<ResultT>> futuresResult = new ArrayList<Future<ResultT>>(size);
    //
    //      for (final ElementT element : set) {
    //         final Future<ResultT> future = executor.submit(new Callable<ResultT>() {
    //            @Override
    //            public ResultT call() throws Exception {
    //               return transformer.transform(element);
    //            }
    //         });
    //
    //         futuresResult.add(future);
    //      }
    //
    //      final Set<ResultT> result = new HashSet<ResultT>(size);
    //      for (final Future<ResultT> futureResult : futuresResult) {
    //         try {
    //            result.add(futureResult.get());
    //         }
    //         catch (final InterruptedException e) {
    //            throw new RuntimeException(e);
    //         }
    //         catch (final ExecutionException e) {
    //            throw new RuntimeException(e);
    //         }
    //      }
    //
    //      return result;
    //   }
 
 
    public static <T> T[] select(final T[] array,
                                 final IPredicate<T> predicate) {
       final List<T> selected = select(Arrays.asList(array), predicate);
 
       final Class<? extends Object[]> newType = array.getClass();
       @SuppressWarnings("unchecked")
       final T[] arrayResult = (newType == Object[].class) ? //
                                                          (T[]) new Object[selected.size()] : //
                                                          (T[]) Array.newInstance(newType.getComponentType(), selected.size());
 
       return selected.toArray(arrayResult);
    }
 
 
    public static <ElementT> List<ElementT> select(final List<ElementT> list,
                                                   final IPredicate<ElementT> predicate) {
       if (list == null) {
          return null;
       }
 
       final ArrayList<ElementT> result = new ArrayList<ElementT>(list.size());
 
       for (final ElementT element : list) {
          if (predicate.evaluate(element)) {
             result.add(element);
          }
       }
       result.trimToSize();
 
       return result;
    }
 
 
    //   @SuppressWarnings("unchecked")
    //   public static <ElementT> ElementT[] select(final ElementT[] list,
    //                                              final IPredicate<ElementT> predicate) {
    //      if (list == null) {
    //         return null;
    //      }
    //
    //      final ArrayList<ElementT> result = new ArrayList<ElementT>(list.length);
    //
    //      for (final ElementT element : list) {
    //         if (predicate.evaluate(element)) {
    //            result.add(element);
    //         }
    //      }
    //      result.trimToSize();
    //
    //      return result.toArray((ElementT[]) new Object[0]);
    //   }
 
 
    public static <ElementT> Set<ElementT> select(final Set<ElementT> set,
                                                  final IPredicate<ElementT> predicate) {
       if (set == null) {
          return null;
       }
 
       final HashSet<ElementT> result = new HashSet<ElementT>();
 
       for (final ElementT element : set) {
          if (predicate.evaluate(element)) {
             result.add(element);
          }
       }
 
       return result;
    }
 
 
    public static <ElementT> Collection<ElementT> select(final Collection<ElementT> collection,
                                                         final IPredicate<ElementT> predicate) {
       if (collection == null) {
          return null;
       }
 
       final ArrayList<ElementT> result = new ArrayList<ElementT>(collection.size());
 
       for (final ElementT element : collection) {
          if (predicate.evaluate(element)) {
             result.add(element);
          }
       }
       result.trimToSize();
 
       return result;
    }
 
 
    public static <ElementT> boolean allSatisfy(final Iterable<ElementT> iterable,
                                                final IPredicate<ElementT> predicate) {
       if (iterable == null) {
          return true;
       }
 
       for (final ElementT element : iterable) {
          if (!predicate.evaluate(element)) {
             return false;
          }
       }
 
       return true;
    }
 
 
    public static <ElementT> boolean anySatisfy(final Iterable<ElementT> iterable,
                                                final IPredicate<ElementT> predicate) {
       if (iterable == null) {
          return false;
       }
 
       for (final ElementT element : iterable) {
          if (predicate.evaluate(element)) {
             return true;
          }
       }
 
       return false;
    }
 
 
    public static <ElementT> void evaluate(final Iterable<ElementT> iterable,
                                           final IEvaluator<ElementT> evaluator) {
       if (iterable == null) {
          return;
       }
 
       for (final ElementT element : iterable) {
          evaluator.evaluate(element);
       }
    }
 
 
    public static <ElementT> void evaluate(final ElementT[] array,
                                           final IEvaluator<ElementT> evaluator) {
       if (array == null) {
          return;
       }
 
       for (final ElementT element : array) {
          evaluator.evaluate(element);
       }
    }
 
 
    public static int[] rangeArray(final int from,
                                   final int to) {
       final int len = to - from + 1;
 
       final int[] result = new int[len];
 
       for (int i = 0; i < len; i++) {
          result[i] = from + i;
       }
 
       return result;
    }
 
 
    public static Integer[] rangeArrayInteger(final int from,
                                              final int to) {
       final int len = to - from + 1;
 
       final Integer[] result = new Integer[len];
 
       for (int i = 0; i < len; i++) {
          result[i] = from + i;
       }
 
       return result;
    }
 
 
    public static List<Integer> rangeList(final int from,
                                          final int to) {
       final int len = to - from + 1;
 
       final List<Integer> result = new ArrayList<Integer>(len);
 
       for (int i = 0; i < len; i++) {
          result.add(from + i);
       }
 
       return result;
    }
 
 
    public static int[] toIntArray(final List<Integer> list) {
       if (list == null) {
          return null;
       }
 
       final int[] result = new int[list.size()];
       for (int i = 0; i < result.length; i++) {
          result[i] = list.get(i);
       }
       return result;
    }
 
 
    public static float[] toFloatArray(final List<Float> list) {
       if (list == null) {
          return null;
       }
 
       final float[] result = new float[list.size()];
       for (int i = 0; i < result.length; i++) {
          result[i] = list.get(i);
       }
       return result;
    }
 
 
    public static int[] toArray(final Integer[] array) {
       final int[] result = new int[array.length];
       for (int i = 0; i < result.length; i++) {
          result[i] = array[i];
       }
       return result;
    }
 
 
    public static void addAll(final List<Integer> list,
                              final int[] ints) {
       for (final int i : ints) {
          list.add(i);
       }
    }
 
 
    public static int[] removeAll(final int[] array,
                                  final Collection<Integer> toRemove) {
       final Set<Integer> toRemoveSet = new HashSet<Integer>(toRemove);
       final List<Integer> resultL = new ArrayList<Integer>(array.length - toRemove.size());
       for (final Integer i : array) {
          if (!toRemoveSet.contains(i)) {
             resultL.add(i);
          }
       }
       return toIntArray(resultL);
    }
 
 
    public static <ElementT> void concurrentEvaluate(final List<ElementT> list,
                                                     final IEvaluator<ElementT> evaluator) {
       concurrentEvaluate(list, new IRangeEvaluator() {
          @Override
          public void evaluate(final int from,
                               final int to) {
             // System.out.println("evaluating from " + from + " to " + to);
             for (int i = from; i <= to; i++) {
                evaluator.evaluate(list.get(i));
             }
          }
       }, GConcurrent.AVAILABLE_PROCESSORS * 3, GConcurrent.DEFAULT_THREAD_PRIORITY, true);
    }
 
 
    public static <ElementT> void concurrentEvaluate(final ElementT[] array,
                                                     final IEvaluator<ElementT> evaluator) {
       concurrentEvaluate(array, new IRangeEvaluator() {
          @Override
          public void evaluate(final int from,
                               final int to) {
             // System.out.println("evaluating from " + from + " to " + to);
             for (int i = from; i <= to; i++) {
                evaluator.evaluate(array[i]);
             }
          }
       }, GConcurrent.AVAILABLE_PROCESSORS * 3, GConcurrent.DEFAULT_THREAD_PRIORITY, true);
    }
 
 
    public static void concurrentEvaluate(final IListInt array,
                                          final IEvaluatorInt evaluator) {
       concurrentEvaluate(array, new IRangeEvaluator() {
          @Override
          public void evaluate(final int from,
                               final int to) {
             // System.out.println("evaluating from " + from + " to " + to);
             for (int i = from; i <= to; i++) {
                evaluator.evaluate(array.get(i));
             }
          }
       }, GConcurrent.AVAILABLE_PROCESSORS * 3, GConcurrent.DEFAULT_THREAD_PRIORITY, true);
    }
 
 
    public static <ElementT> void asyncConcurrentEvaluate(final ElementT[] array,
                                                          final IEvaluator<ElementT> evaluator,
                                                          final int threadPriority) {
       asyncConcurrentEvaluate(array, evaluator, GConcurrent.AVAILABLE_PROCESSORS * 3, threadPriority);
    }
 
 
    public static <ElementT> void asyncConcurrentEvaluate(final ElementT[] array,
                                                          final IEvaluator<ElementT> evaluator,
                                                          final int numberThreads,
                                                          final int threadPriority) {
       concurrentEvaluate(array, new IRangeEvaluator() {
          @Override
          public void evaluate(final int from,
                               final int to) {
             // System.out.println("evaluating from " + from + " to " + to);
             for (int i = from; i <= to; i++) {
                evaluator.evaluate(array[i]);
             }
          }
       }, numberThreads, threadPriority, false);
    }
 
 
    public static <ElementT> void concurrentEvaluate(final List<ElementT> list,
                                                     final IEvaluator<ElementT> evaluator,
                                                     final int numberThreads) {
       concurrentEvaluate(list, new IRangeEvaluator() {
          @Override
          public void evaluate(final int from,
                               final int to) {
             // System.out.println("evaluating from " + from + " to " + to);
             for (int i = from; i <= to; i++) {
                evaluator.evaluate(list.get(i));
             }
          }
       }, numberThreads);
    }
 
 
    public static <ElementT> void concurrentEvaluate(final ElementT[] array,
                                                     final IEvaluator<ElementT> evaluator,
                                                     final int numberThreads) {
       concurrentEvaluate(array, new IRangeEvaluator() {
          @Override
          public void evaluate(final int from,
                               final int to) {
             // System.out.println("evaluating from " + from + " to " + to);
             for (int i = from; i <= to; i++) {
                evaluator.evaluate(array[i]);
             }
          }
       }, numberThreads);
    }
 
 
    public static <ElementT> void concurrentEvaluate(final List<ElementT> list,
                                                     final IRangeEvaluator rangeEvaluator,
                                                     final int numberThreads) {
       concurrentEvaluate(list, rangeEvaluator, numberThreads, GConcurrent.DEFAULT_THREAD_PRIORITY, true);
    }
 
 
    public static <ElementT> void concurrentEvaluate(final List<ElementT> list,
                                                     final IRangeEvaluator rangeEvaluator) {
       //      concurrentEvaluate(list, rangeEvaluator, GConcurrent.AVAILABLE_PROCESSORS * 3, GConcurrent.DEFAULT_THREAD_PRIORITY, true);
       concurrentEvaluate(list, rangeEvaluator, GConcurrent.AVAILABLE_PROCESSORS * 3);
    }
 
 
    public static <ElementT> void concurrentEvaluate(final ElementT[] array,
                                                     final IRangeEvaluator rangeEvaluator) {
       concurrentEvaluate(array, rangeEvaluator, GConcurrent.AVAILABLE_PROCESSORS * 3, GConcurrent.DEFAULT_THREAD_PRIORITY, true);
    }
 
 
    public static <ElementT> void concurrentEvaluate(final ElementT[] array,
                                                     final IRangeEvaluator rangeEvaluator,
                                                     final int numberOfThreads) {
       concurrentEvaluate(array, rangeEvaluator, numberOfThreads, GConcurrent.DEFAULT_THREAD_PRIORITY, true);
    }
 
 
    public static void concurrentEvaluate(final int[] array,
                                          final IRangeEvaluator rangeEvaluator) {
       concurrentEvaluate(array, rangeEvaluator, GConcurrent.AVAILABLE_PROCESSORS * 3, GConcurrent.DEFAULT_THREAD_PRIORITY, true);
    }
 
 
    public static void concurrentEvaluate(final IListInt list,
                                          final IRangeEvaluator rangeEvaluator) {
       concurrentEvaluate(list, rangeEvaluator, GConcurrent.AVAILABLE_PROCESSORS * 3);
    }
 
 
    public static void concurrentEvaluate(final IListInt list,
                                          final IRangeEvaluator rangeEvaluator,
                                          final int numberThreads) {
       concurrentEvaluate(list, rangeEvaluator, numberThreads, GConcurrent.DEFAULT_THREAD_PRIORITY, true);
    }
 
 
    private static <ElementT> void concurrentEvaluate(final List<ElementT> list,
                                                      final IRangeEvaluator rangeEvaluator,
                                                      final int numberThreads,
                                                      final int threadPriority,
                                                      final boolean waitFinalization) {
       if (list == null) {
          return;
       }
 
       final ExecutorService executor = GConcurrent.createExecutor(numberThreads + 1, threadPriority);
 
       final int size = list.size();
 
       int step = Math.max(Math.round((float) size / numberThreads), 1);
       if (step * numberThreads < size) {
          step++;
       }
 
       int from = 0;
       while (from < size) {
          final int to = Math.min(from + step - 1, size - 1);
          final int finalFrom = from;
          executor.execute(new Runnable() {
             @Override
             public void run() {
                rangeEvaluator.evaluate(finalFrom, to);
             }
          });
 
          from += step;
       }
 
       executor.shutdown();
       if (waitFinalization) {
          GConcurrent.awaitTermination(executor);
       }
    }
 
 
    private static <ElementT> void concurrentEvaluate(final ElementT[] array,
                                                      final IRangeEvaluator rangeEvaluator,
                                                      final int numberThreads,
                                                      final int threadPriority,
                                                      final boolean waitFinalization) {
       if (array == null) {
          return;
       }
 
       final ExecutorService executor = GConcurrent.createExecutor(numberThreads + 1, threadPriority);
 
       final int size = array.length;
 
       int step = Math.max(Math.round((float) size / numberThreads), 1);
       if (step * numberThreads < size) {
          step++;
       }
 
       int from = 0;
       while (from < size) {
          final int to = Math.min(from + step - 1, size - 1);
          final int finalFrom = from;
          executor.execute(new Runnable() {
             @Override
             public void run() {
                rangeEvaluator.evaluate(finalFrom, to);
             }
          });
 
          from += step;
       }
 
       executor.shutdown();
       if (waitFinalization) {
          GConcurrent.awaitTermination(executor);
       }
    }
 
 
    private static void concurrentEvaluate(final int[] array,
                                           final IRangeEvaluator rangeEvaluator,
                                           final int numberThreads,
                                           final int threadPriority,
                                           final boolean waitFinalization) {
       if (array == null) {
          return;
       }
 
       final ExecutorService executor = GConcurrent.createExecutor(numberThreads + 1, threadPriority);
 
       final int size = array.length;
 
       int step = Math.max(Math.round((float) size / numberThreads), 1);
       if (step * numberThreads < size) {
          step++;
       }
 
       int from = 0;
       while (from < size) {
          final int to = Math.min(from + step - 1, size - 1);
          final int finalFrom = from;
          executor.execute(new Runnable() {
             @Override
             public void run() {
                rangeEvaluator.evaluate(finalFrom, to);
             }
          });
 
          from += step;
       }
 
       executor.shutdown();
       if (waitFinalization) {
          GConcurrent.awaitTermination(executor);
       }
    }
 
 
    private static void concurrentEvaluate(final IListInt array,
                                           final IRangeEvaluator rangeEvaluator,
                                           final int numberThreads,
                                           final int threadPriority,
                                           final boolean waitFinalization) {
       if (array == null) {
          return;
       }
 
       final ExecutorService executor = GConcurrent.createExecutor(numberThreads + 1, threadPriority);
 
       final int size = array.size();
 
       int step = Math.max(Math.round((float) size / numberThreads), 1);
       if (step * numberThreads < size) {
          step++;
       }
 
       int from = 0;
       while (from < size) {
          final int to = Math.min(from + step - 1, size - 1);
          final int finalFrom = from;
          executor.execute(new Runnable() {
             @Override
             public void run() {
                rangeEvaluator.evaluate(finalFrom, to);
             }
          });
 
          from += step;
       }
 
       executor.shutdown();
       if (waitFinalization) {
          GConcurrent.awaitTermination(executor);
       }
    }
 
 
    public static int theOnlyOne(final int[] array) {
       if (array.length == 1) {
          return array[0];
       }
 
       throw new RuntimeException("array has " + array.length + " elements");
    }
 
 
    public static <T> T theOnlyOne(final T[] array) {
       if (array.length == 1) {
          return array[0];
       }
 
       throw new RuntimeException("array has " + array.length + " elements");
    }
 
 
    public static <T> T[] rtrim(final T[] array) {
       final int length = array.length;
 
       int newLenght = length;
       for (int i = length - 1; i >= 0; i--) {
          if (array[i] != null) {
             newLenght = i + 1;
             break;
          }
       }
 
       if (newLenght == length) {
          return array;
       }
       return Arrays.copyOf(array, newLenght);
    }
 
 
    public static int[] remove(final int[] array,
                               final int value) {
 
       for (int i = 0; i < array.length; i++) {
          if (array[i] == value) {
             final int[] result = new int[array.length - 1];
             System.arraycopy(array, 0, result, 0, i);
             System.arraycopy(array, i + 1, result, i, array.length - i - 1);
             return result;
          }
       }
 
       return array;
    }
 
 
    public static int[] removeFromSorted(final int[] array,
                                         final int value) {
       final int i = Arrays.binarySearch(array, value);
       if (i < 0) {
          return array;
       }
 
       final int[] result = new int[array.length - 1];
       System.arraycopy(array, 0, result, 0, i);
       System.arraycopy(array, i + 1, result, i, result.length - i);
       return result;
    }
 
 
    //   public static void main(final String[] args) {
    //      final String[] array = new String[] { "a", "b", "c" };
    //      final String[] collected = GCollections.collect(array, new ITransformer<String, String>() {
    //         @Override
    //         public String transform(final String element) {
    //            return element.toUpperCase();
    //         }
    //      });
    //      System.out.println(Arrays.toString(collected));
    //   }
 
 
    private static int partition(final int array[],
                                 final int left,
                                 final int right,
                                 final IComparatorInt comparator) {
      // convert to long, and back to int, to avoid the possibility of an int overflow while adding
      final int pivotIndex = (int) (((long) left + right) / 2);
      final int pivot = array[pivotIndex];
 
       int i = left;
       int j = right;
       while (i <= j) {
          //while (array[i] < pivot) {
          while (comparator.compare(array[i], pivot) < 0) {
             i++;
          }
 
          //while (array[j] > pivot) {
          while (comparator.compare(array[j], pivot) > 0) {
             j--;
          }
 
          if (i <= j) {
             final int tmp = array[i];
             array[i] = array[j];
             array[j] = tmp;
 
             i++;
             j--;
          }
       }
 
       return i;
    }
 
 
    private static int partition(final IListInt array,
                                 final int left,
                                 final int right,
                                 final IComparatorInt comparator) {
       final int pivot = array.get((left + right) / 2);
 
       int i = left;
       int j = right;
       while (i <= j) {
          //while (array[i] < pivot) {
          while (comparator.compare(array.get(i), pivot) < 0) {
             i++;
          }
 
          //while (array[j] > pivot) {
          while (comparator.compare(array.get(j), pivot) > 0) {
             j--;
          }
 
          if (i <= j) {
             final int tmp = array.get(i);
             array.set(i, array.get(j));
             array.set(j, tmp);
 
             i++;
             j--;
          }
       }
 
       return i;
    }
 
 
    private static int partition(final double array[],
                                 final int left,
                                 final int right,
                                 final IComparatorDouble comparator) {
      // convert to long, and back to int, to avoid the possibility of an int overflow while adding
      final int pivotIndex = (int) (((long) left + right) / 2);
      final double pivot = array[pivotIndex];
 
       int i = left;
       int j = right;
       while (i <= j) {
          //while (array[i] < pivot) {
          while (comparator.compare(array[i], pivot) < 0) {
             i++;
          }
 
          //while (array[j] > pivot) {
          while (comparator.compare(array[j], pivot) > 0) {
             j--;
          }
 
          if (i <= j) {
             final double tmp = array[i];
             array[i] = array[j];
             array[j] = tmp;
 
             i++;
             j--;
          }
       }
 
       return i;
    }
 
 
    public static void quickSort(final int array[],
                                 final int left,
                                 final int right,
                                 final IComparatorInt comparator) {
       final int index = partition(array, left, right, comparator);
 
       if (left < index - 1) {
          quickSort(array, left, index - 1, comparator);
       }
 
       if (index < right) {
          quickSort(array, index, right, comparator);
       }
 
    }
 
 
    public static void quickSort(final IListInt array,
                                 final int left,
                                 final int right,
                                 final IComparatorInt comparator) {
       final int index = partition(array, left, right, comparator);
 
       if (left < index - 1) {
          quickSort(array, left, index - 1, comparator);
       }
 
       if (index < right) {
          quickSort(array, index, right, comparator);
       }
 
    }
 
 
    public static void quickSort(final double array[],
                                 final int left,
                                 final int right,
                                 final IComparatorDouble comparator) {
       final int index = partition(array, left, right, comparator);
 
       if (left < index - 1) {
          quickSort(array, left, index - 1, comparator);
       }
 
       if (index < right) {
          quickSort(array, index, right, comparator);
       }
 
    }
 
 
    public static void quickSort(final int array[],
                                 final IComparatorInt comparator) {
 
       quickSort(array, 0, array.length - 1, comparator);
    }
 
 
    public static void quickSort(final IListInt array,
                                 final IComparatorInt comparator) {
 
       quickSort(array, 0, array.size() - 1, comparator);
    }
 
 
    public static void quickSort(final double array[],
                                 final IComparatorDouble comparator) {
 
       quickSort(array, 0, array.length - 1, comparator);
    }
 
 
    public static byte[][] split(final byte[] bytes,
                                 final int fragmentSize) {
 
       final int bytesLength = bytes.length;
 
       if (bytesLength == 0) {
          return new byte[0][0];
       }
 
       int fragmentsCount = bytesLength / fragmentSize;
       if (fragmentsCount * fragmentSize < bytesLength) {
          fragmentsCount++;
       }
 
       final byte[][] result = new byte[fragmentsCount][fragmentSize];
 
       for (int i = 0; i < fragmentsCount - 1; i++) {
          System.arraycopy(bytes, i * fragmentSize, result[i], 0, fragmentSize);
       }
 
       final int lastFragmentSize = bytesLength - ((fragmentsCount - 1) * fragmentSize);
       final byte[] lastFragment = new byte[lastFragmentSize];
       System.arraycopy(bytes, (fragmentsCount - 1) * fragmentSize, lastFragment, 0, lastFragmentSize);
       result[fragmentsCount - 1] = lastFragment;
 
       return result;
    }
 
 
    public static <T> List<List<T>> split(final List<T> list,
                                          final int fragmentSize) {
 
       final int listSize = list.size();
 
       if (listSize == 0) {
          return Collections.emptyList();
       }
 
       int fragmentsCount = listSize / fragmentSize;
       if (fragmentsCount * fragmentSize < listSize) {
          fragmentsCount++;
       }
 
       final List<List<T>> result = new ArrayList<List<T>>(fragmentsCount);
 
       for (int i = 0; i < fragmentsCount - 1; i++) {
          result.add(list.subList(i * fragmentSize, i * fragmentSize + fragmentSize));
       }
 
       final int lastFragmentSize = listSize - ((fragmentsCount - 1) * fragmentSize);
       result.add(list.subList((fragmentsCount - 1) * fragmentSize, (fragmentsCount - 1) * fragmentSize + lastFragmentSize));
 
       return result;
    }
 
 
    public static byte[] concatenate(final byte[] bytes0,
                                     final byte[] bytes1) {
       final int length0 = bytes0.length;
       final int length1 = bytes1.length;
 
       final byte[] result = new byte[length0 + length1];
 
       System.arraycopy(bytes0, 0, result, 0, length0);
       System.arraycopy(bytes1, 0, result, length0, length1);
 
       return result;
    }
 
 
    public static <T> T[] concatenate(final T[] array,
                                      final T[] anotherArray) {
 
       if ((array == null) || (array.length == 0)) {
          return anotherArray;
       }
 
       if ((anotherArray == null) || (anotherArray.length == 0)) {
          return array;
       }
 
       final T[] result = Arrays.copyOf(array, array.length + anotherArray.length);
       System.arraycopy(anotherArray, 0, result, array.length, anotherArray.length);
 
       return result;
    }
 
 
    public static <T, ET extends T> List<T> createList(final ET... elements) {
       final ArrayList<T> result = new ArrayList<T>(elements.length);
 
       for (final T element : elements) {
          result.add(element);
       }
 
       return result;
    }
 
 
    public static float[] toFloat(final double doubleList[]) {
       final float floatList[] = new float[doubleList.length];
       for (int i = 0; i < doubleList.length; i++) {
          floatList[i] = (float) doubleList[i];
       }
       return floatList;
    }
 
 
    public static void main(final String[] args) {
       System.out.println("GCollections 0.1");
       System.out.println("----------------\n");
 
       System.out.println(GCollections.rangeList(48, 49));
    }
 
 
 }
