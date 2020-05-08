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
 
 import com.google.common.base.Function;
 import net.joala.condition.timing.IgnorableStateQueryException;
 
 import javax.annotation.Nullable;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * <p>
  * A function to evaluate an expression. It especially wraps exceptions of type
  * {@link ExpressionEvaluationException} into {@link IgnorableStateQueryException}.
  * </p>
  *
  * @param <T> the type of the value the expression returns
  * @since 9/18/12
  */
class ExpressionFunction<T> implements Function<Expression<T>, T> {
   /**
    * <p>
    * Evaluates expression and makes any {@link ExpressionEvaluationException} ignorable.
    * </p>
    *
    * @param input the expression to evaluate
    * @return the result of the expression
    */
   @Override
   @Nullable
  public T apply(@Nullable final Expression<T> input) {
     checkNotNull(input, "Expression must not be null.");
     try {
       return input.get();
     } catch (ExpressionEvaluationException e) {
       throw new IgnorableStateQueryException(this, e);
     }
   }
 }
