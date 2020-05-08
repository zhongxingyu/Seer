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
  * Random number provider for double values.
  * </p>
  *
  * @since 9/17/12
  */
 public class RandomDoubleProvider extends AbstractRandomNumberProvider<Double> {
   /**
    * <p>
    * Creates a random number provider for double values.
    * </p>
    */
   public RandomDoubleProvider() {
     super(new DoubleRandomNumberType());
   }
 
  private static class DoubleRandomNumberType extends AbstractRandomNumberType<Double> {
 
     private DoubleRandomNumberType() {
       super(Double.class);
     }
 
     @Override
     public Double min() {
       return Double.MIN_VALUE;
     }
 
     @Override
     public Double max() {
       return Double.MAX_VALUE;
     }
 
     @Override
     public Double sum(final Double value1, final Double value2) {
       return value1 + value2;
     }
 
     @Override
     public Double percentOf(final double percent, final Double value) {
       return percent * value;
     }
   }
 }
