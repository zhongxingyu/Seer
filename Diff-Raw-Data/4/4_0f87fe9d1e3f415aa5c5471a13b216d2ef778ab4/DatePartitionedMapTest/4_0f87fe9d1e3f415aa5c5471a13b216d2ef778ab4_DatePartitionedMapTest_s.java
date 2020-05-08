 /*
  * Copyright (c) 2013 MercadoLibre  -- All rights reserved
  */
 package com.zauberlabs.bigdata.lambdaoa.realtime.util;
 
 import static junit.framework.Assert.*;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.testng.collections.Lists;
 
 import com.google.common.collect.ImmutableSortedMap;
 
 /**
  * {@link DatePartitionedMap} test  
  * 
  * 
  * @since 05/07/2013
  */
 public class DatePartitionedMapTest {
     
     private DatePartitionedMap<List<String>> map;
 
     @Before
     public final void setup() {
         map = new DatePartitionedMap<List<String>>(new Callable<List<String>>() {
             @Override public List<String> call() throws Exception { return Lists.newArrayList(); } });
     }
     
     @Test
     public final void should_allow_addition_of_data_after_drop() {
         
         map.get(new Date(1000)).add("c");
         
         map.get(new Date(1001)).add("f");
         
         map.dropLessThan(new Date(1001));
         
         map.get(new Date(1002)).add("f");
         
         assertEquals(
             ImmutableSortedMap.of(new Date(1001), Arrays.asList("f"), new Date(1002), Arrays.asList("f")),
             map.getTarget());
     }
     
     
     @Test
     public final void should_drop_data_older_data() {
         map.get(new Date(1000)).addAll(Lists.newArrayList("a", "b", "c"));
         
         map.get(new Date(1001)).add("f");
         
         map.get(new Date(2000)).add("ff");
         
         map.dropLessThan(new Date(1001));
         
        assertEquals(ImmutableSortedMap.of(new Date(1001), Arrays.asList("f")), map.getTarget());
     }
 
     
     
 }
