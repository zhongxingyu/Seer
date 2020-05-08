 package org.nnsoft.hazelmap;
 
 /*
  *    Copyright 2011 The 99 Software Foundation
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
 import static org.nnsoft.hazelmap.Hazelmap.processInput;
 
 import org.junit.Test;
 
 import com.hazelcast.core.MultiMap;
 
 public final class MapReduceTestCase
 {
 
     @Test
     public void simpleCase()
     {
         MultiMap<String, Integer> actual = processInput( new TemperatureReader( getClass().getResourceAsStream( "temperatures.txt" ) ) )
                                             .usingMapper( new MaxTemperatureMapper() )
                                             .withReducer( new MaxTemperatureReducer() )
                                             .invoke();
 
         assertNotNull( actual );
         assertEquals( 221, actual.get( "1950" ).iterator().next().intValue() );
         assertEquals( 1111, actual.get( "1949" ).iterator().next().intValue() );
     }
 
 }
