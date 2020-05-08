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
 
 package net.joala.condition;
 
import org.hamcrest.Description;
import org.hamcrest.SelfDescribing; // NOSONAR
 
 import javax.annotation.Nullable;
 
 /**
  * An expression is handed over to conditions to be evaluated again and again.
  * To implement it is recommended to extend {@link AbstractExpression} which ensures
  * to stay compatible with future changes and for convenience provides an empty implementation
  * of {@link #describeTo(Description)}.
  *
  * @param <T> the result type of the expression
  * @since 2/27/12
  */
 public interface Expression<T> extends SelfDescribing {
   /**
    * Retrieve the result of the expression.
    *
    * @return expression result
    * @throws ExpressionEvaluationException when the expression cannot (yet) be computed, but this failure is
    *                                       "volatile", i.&nbsp;e. it is expected to vanish later, so it makes sense
    *                                       to re-evaluate the expression
    */
   @Nullable
   T get();
 }
