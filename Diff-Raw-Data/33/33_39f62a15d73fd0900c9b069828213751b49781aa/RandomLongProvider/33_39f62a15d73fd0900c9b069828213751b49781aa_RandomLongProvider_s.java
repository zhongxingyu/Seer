 /*
  * Copyright 2012 CoreMedia AG
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
 
 package net.joala.data.random;
 
 /**
  * <p>
  * Random number provider for long values.
  * </p>
  *
  * @since 9/17/12
  */
 public class RandomLongProvider extends AbstractRandomNumberProvider<Long> {
   /**
    * <p>
    * Creates a random number provider for long values.
    * </p>
    */
   public RandomLongProvider() {
     super(new LongRandomNumberType());
   }
 
  private static class LongRandomNumberType extends AbstractRandomNumberType<Long> {
 
     private LongRandomNumberType() {
       super(Long.class);
     }
 
     @Override
     public Long min() {
       return Long.MIN_VALUE;
     }
 
     @Override
     public Long max() {
       return Long.MAX_VALUE;
     }
 
     @Override
     public Long sum(final Long value1, final Long value2) {
       return value1 + value2;
     }
 
     @Override
     public Long percentOf(final double percent, final Long value) {
       return (long) percent * value;
     }
   }
 }
