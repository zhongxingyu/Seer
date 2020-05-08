 /*
  * Copyright 2011 Vasily Shiyan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package see.evaluation.processors;
 
 import com.google.common.base.Supplier;
 import see.evaluation.ValueProcessor;
 import see.parser.numbers.NumberFactory;
 
 import javax.annotation.Nullable;
 
 /**
  * Value processor for use with {@link see.evaluation.visitors.LazyVisitor}.
  * If value is a number, pass it through NumberFactory.
  */
 public class NumberLifter implements ValueProcessor {
 
    private final Supplier<NumberFactory> factorySupplier;
 
    public NumberLifter(Supplier<NumberFactory> factorySupplier) {
         this.factorySupplier = factorySupplier;
     }
 
     @Override
     public Object apply(@Nullable Object input) {
         if (input instanceof Number) {
             return factorySupplier.get().getNumber((Number) input);
         } else {
             return input;
         }
     }
 }
