 /*
  * Copyright 2010 CodeGist.org
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  *
  * ===================================================================
  *
  * More information at http://www.codegist.org.
  */
 
 package org.codegist.common.codec;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Laurent Gilles (laurent.gilles@codegist.org)
  */
 public class HexTest {
 
     @Test(expected = NullPointerException.class)
     public void testNull() {
         Hex.encode(null);
     }
 
     @Test
     public void testEmpty() {
        assertEquals("", Hex.encode(new byte[0]));
     }
 
     @Test
     public void test1() {
        assertEquals("000102030405060708090a0b0c0d0e0f1011", Hex.encode(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17}));
     }
 
     @Test
     public void test2() {
        assertEquals("00fffefdfcfbfaf9f8f7f6f5f4f3f2f1f0ef", Hex.encode(new byte[]{0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15, -16, -17}));
     }
 
 
 }
