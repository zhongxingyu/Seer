 /*
  * Copyright 2011 Gregory P. Moyer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.syphr.mythtv.proto.impl;
 
 import junit.framework.Assert;
 
 import org.apache.commons.lang3.Pair;
 import org.junit.Test;
 
 public class Protocol63UtilsTest
 {
     @Test
     public void testCombineAndBreakNumbers()
     {
         long value = 1234567890098765432L;
 
         Pair<Integer, Integer> split = Protocol63Utils.splitLong(value);
        Assert.assertEquals(value, Protocol63Utils.combineInts(split.getLeftElement(), split.getRightElement()));
     }
 }
