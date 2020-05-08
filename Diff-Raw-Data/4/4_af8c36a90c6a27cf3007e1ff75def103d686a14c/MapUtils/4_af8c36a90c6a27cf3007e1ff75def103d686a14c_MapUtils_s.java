 package com.othelle.jtuples;
 
 /*
  * =============================================================================
  *
  *   Copyright 2013, JTuples team
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  *
  * =============================================================================
  */
 
 import java.util.*;
 
 import static com.othelle.jtuples.Tuples.convert;
 import static com.othelle.jtuples.Tuples.tuple;
 
 /**
  * author: v.vlasov
  */
 public class MapUtils {
 
     /**
      * Flattens map to a list of tuples
      *
      * @param map
      * @return
      */
     public static <T1, T2> List<Tuple2<T1, T2>> flatten(Map<T1, T2> map) {
         ArrayList<Tuple2<T1, T2>> result = new ArrayList<Tuple2<T1, T2>>(map.size());
 
         for (Map.Entry<T1, T2> entry : map.entrySet()) result.add(convert(entry));
 
         return result;
     }
 
 
     /**
      * Builds a map from a collection of tuples, utilizes the size() method
      *
      * @param keyValues an iterable object of tuples
      * @return a map constructed from the objects
      */
     public static <T1, T2> Map<T1, T2> map(Collection<Tuple2<T1, T2>> keyValues) {
         return map(keyValues, false);
     }
 
 
     /**
      * Pretty verbose function if some special configuration is required. Such as invoking for Collection with a known size.
      *
      * @param keyValues     iterable object of tuples
      * @param preserveOrder pass true to use LinkedList
      * @return a map constructed from the objects with a given parameters
      */
     public static <T1, T2> Map<T1, T2> map(Collection<Tuple2<T1, T2>> keyValues, boolean preserveOrder) {
         Map<T1, T2> map;
         int size = keyValues.size();
         //to avoid the rebuilds in most cases, since load factor is <= 1
         map = preserveOrder ? new LinkedHashMap<T1, T2>(size * 2) : new HashMap<T1, T2>(size * 2);
 
         return putItems(map, keyValues);
     }
 
     private static <T1, T2> Map<T1, T2> putItems(Map<T1, T2> map, Collection<Tuple2<T1, T2>> keyValues) {
         for (Tuple2<T1, T2> keyValue : keyValues) {
             map.put(keyValue._1(), keyValue._2());
         }
         return map;
     }
 
 
     //Generated methods do not edit
 
     /**
      * Constructs a map from a given list of Tuple2, considering each first element as a key and the rest as a value Tuple1
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2> Map<T1, Tuple1<T2>> map2(Collection<Tuple2<T1, T2>> keyValues) {
         Map<T1, Tuple1<T2>> map = new HashMap<T1, Tuple1<T2>>();
         for (Tuple2<T1, T2> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple3, considering each first element as a key and the rest as a value Tuple2
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3> Map<T1, Tuple2<T2, T3>> map3(Collection<Tuple3<T1, T2, T3>> keyValues) {
         Map<T1, Tuple2<T2, T3>> map = new HashMap<T1, Tuple2<T2, T3>>();
         for (Tuple3<T1, T2, T3> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple4, considering each first element as a key and the rest as a value Tuple3
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4> Map<T1, Tuple3<T2, T3, T4>> map4(Collection<Tuple4<T1, T2, T3, T4>> keyValues) {
         Map<T1, Tuple3<T2, T3, T4>> map = new HashMap<T1, Tuple3<T2, T3, T4>>();
         for (Tuple4<T1, T2, T3, T4> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple5, considering each first element as a key and the rest as a value Tuple4
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5> Map<T1, Tuple4<T2, T3, T4, T5>> map5(Collection<Tuple5<T1, T2, T3, T4, T5>> keyValues) {
         Map<T1, Tuple4<T2, T3, T4, T5>> map = new HashMap<T1, Tuple4<T2, T3, T4, T5>>();
         for (Tuple5<T1, T2, T3, T4, T5> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple6, considering each first element as a key and the rest as a value Tuple5
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5, T6> Map<T1, Tuple5<T2, T3, T4, T5, T6>> map6(Collection<Tuple6<T1, T2, T3, T4, T5, T6>> keyValues) {
         Map<T1, Tuple5<T2, T3, T4, T5, T6>> map = new HashMap<T1, Tuple5<T2, T3, T4, T5, T6>>();
         for (Tuple6<T1, T2, T3, T4, T5, T6> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5(), keyValue._6()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple7, considering each first element as a key and the rest as a value Tuple6
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5, T6, T7> Map<T1, Tuple6<T2, T3, T4, T5, T6, T7>> map7(Collection<Tuple7<T1, T2, T3, T4, T5, T6, T7>> keyValues) {
         Map<T1, Tuple6<T2, T3, T4, T5, T6, T7>> map = new HashMap<T1, Tuple6<T2, T3, T4, T5, T6, T7>>();
         for (Tuple7<T1, T2, T3, T4, T5, T6, T7> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5(), keyValue._6(), keyValue._7()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple8, considering each first element as a key and the rest as a value Tuple7
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8> Map<T1, Tuple7<T2, T3, T4, T5, T6, T7, T8>> map8(Collection<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> keyValues) {
         Map<T1, Tuple7<T2, T3, T4, T5, T6, T7, T8>> map = new HashMap<T1, Tuple7<T2, T3, T4, T5, T6, T7, T8>>();
         for (Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5(), keyValue._6(), keyValue._7(), keyValue._8()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple9, considering each first element as a key and the rest as a value Tuple8
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Map<T1, Tuple8<T2, T3, T4, T5, T6, T7, T8, T9>> map9(Collection<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> keyValues) {
         Map<T1, Tuple8<T2, T3, T4, T5, T6, T7, T8, T9>> map = new HashMap<T1, Tuple8<T2, T3, T4, T5, T6, T7, T8, T9>>();
         for (Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5(), keyValue._6(), keyValue._7(), keyValue._8(), keyValue._9()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple10, considering each first element as a key and the rest as a value Tuple9
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Map<T1, Tuple9<T2, T3, T4, T5, T6, T7, T8, T9, T10>> map10(Collection<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> keyValues) {
         Map<T1, Tuple9<T2, T3, T4, T5, T6, T7, T8, T9, T10>> map = new HashMap<T1, Tuple9<T2, T3, T4, T5, T6, T7, T8, T9, T10>>();
         for (Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5(), keyValue._6(), keyValue._7(), keyValue._8(), keyValue._9(), keyValue._10()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple11, considering each first element as a key and the rest as a value Tuple10
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Map<T1, Tuple10<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> map11(Collection<Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> keyValues) {
         Map<T1, Tuple10<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> map = new HashMap<T1, Tuple10<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>>();
         for (Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5(), keyValue._6(), keyValue._7(), keyValue._8(), keyValue._9(), keyValue._10(), keyValue._11()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple12, considering each first element as a key and the rest as a value Tuple11
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Map<T1, Tuple11<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> map12(Collection<Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> keyValues) {
         Map<T1, Tuple11<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> map = new HashMap<T1, Tuple11<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>>();
         for (Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5(), keyValue._6(), keyValue._7(), keyValue._8(), keyValue._9(), keyValue._10(), keyValue._11(), keyValue._12()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple13, considering each first element as a key and the rest as a value Tuple12
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Map<T1, Tuple12<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> map13(Collection<Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> keyValues) {
         Map<T1, Tuple12<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> map = new HashMap<T1, Tuple12<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>>();
         for (Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5(), keyValue._6(), keyValue._7(), keyValue._8(), keyValue._9(), keyValue._10(), keyValue._11(), keyValue._12(), keyValue._13()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple14, considering each first element as a key and the rest as a value Tuple13
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Map<T1, Tuple13<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> map14(Collection<Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> keyValues) {
         Map<T1, Tuple13<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> map = new HashMap<T1, Tuple13<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>>();
         for (Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5(), keyValue._6(), keyValue._7(), keyValue._8(), keyValue._9(), keyValue._10(), keyValue._11(), keyValue._12(), keyValue._13(), keyValue._14()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given list of Tuple15, considering each first element as a key and the rest as a value Tuple14
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Map<T1, Tuple14<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> map15(Collection<Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> keyValues) {
         Map<T1, Tuple14<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> map = new HashMap<T1, Tuple14<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>>();
         for (Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5(), keyValue._6(), keyValue._7(), keyValue._8(), keyValue._9(), keyValue._10(), keyValue._11(), keyValue._12(), keyValue._13(), keyValue._14(), keyValue._15()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
    public static <T1, T2> Map<T1, Tuple1<T2>> map(Tuple2<T1, T2>... keyValues) {
        return map2(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3> Map<T1, Tuple2<T2, T3>> map(Tuple3<T1, T2, T3>... keyValues) {
         return map3(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4> Map<T1, Tuple3<T2, T3, T4>> map(Tuple4<T1, T2, T3, T4>... keyValues) {
         return map4(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5> Map<T1, Tuple4<T2, T3, T4, T5>> map(Tuple5<T1, T2, T3, T4, T5>... keyValues) {
         return map5(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5, T6> Map<T1, Tuple5<T2, T3, T4, T5, T6>> map(Tuple6<T1, T2, T3, T4, T5, T6>... keyValues) {
         return map6(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5, T6, T7> Map<T1, Tuple6<T2, T3, T4, T5, T6, T7>> map(Tuple7<T1, T2, T3, T4, T5, T6, T7>... keyValues) {
         return map7(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8> Map<T1, Tuple7<T2, T3, T4, T5, T6, T7, T8>> map(Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>... keyValues) {
         return map8(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Map<T1, Tuple8<T2, T3, T4, T5, T6, T7, T8, T9>> map(Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>... keyValues) {
         return map9(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Map<T1, Tuple9<T2, T3, T4, T5, T6, T7, T8, T9, T10>> map(Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>... keyValues) {
         return map10(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Map<T1, Tuple10<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> map(Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>... keyValues) {
         return map11(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Map<T1, Tuple11<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> map(Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>... keyValues) {
         return map12(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Map<T1, Tuple12<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> map(Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>... keyValues) {
         return map13(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Map<T1, Tuple13<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> map(Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>... keyValues) {
         return map14(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Map<T1, Tuple14<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> map(Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>... keyValues) {
         return map15(Arrays.asList(keyValues));
     }
 
 
     /**
      * Constructs a map from a given list of Tuple16, considering each first element as a key and the rest as a value Tuple15
      *
      * @param keyValues
      * @return a map
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Map<T1, Tuple15<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> map16(Iterable<Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> keyValues) {
         Map<T1, Tuple15<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> map = new HashMap<T1, Tuple15<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>>();
         for (Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> keyValue : keyValues) {
             map.put(keyValue._1(), tuple(keyValue._2(), keyValue._3(), keyValue._4(), keyValue._5(), keyValue._6(), keyValue._7(), keyValue._8(), keyValue._9(), keyValue._10(), keyValue._11(), keyValue._12(), keyValue._13(), keyValue._14(), keyValue._15(), keyValue._16()));
         }
         return map;
     }
 
     /**
      * Constructs a map from a given keyValues
      *
      * @param keyValues
      * @return map build from an array of tuples
      */
     public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Map<T1, Tuple15<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> map(Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>... keyValues) {
         return map16(Arrays.asList(keyValues));
     }
 }
