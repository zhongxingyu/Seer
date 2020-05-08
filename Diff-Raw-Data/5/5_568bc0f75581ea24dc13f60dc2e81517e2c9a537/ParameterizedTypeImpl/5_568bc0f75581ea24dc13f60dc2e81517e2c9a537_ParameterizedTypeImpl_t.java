 /**
  * Copyright (C) 2008, 2009 Ivan S. Dubrov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.code.nanorm.internal.introspect;
 
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 
 import com.google.code.nanorm.internal.util.ToStringBuilder;
 
 /**
  * Simple implementation of {@link ParameterizedType}.
  * 
  * @see ParameterizedType
  * @author Ivan Dubrov
  */
 public class ParameterizedTypeImpl implements ParameterizedType {
 
     private final Type rawType;
 
     private final Type[] actualTypeArguments;
 
     /**
      * Constructor.
      * 
      * @param rawType raw type
      */
     public ParameterizedTypeImpl(Class<?> rawType) {
        // We don't have the substitutions, so set actual type
        // arguments to type variables itself
        this(rawType, rawType.getTypeParameters());
     }
 
     /**
      * Constructor.
      * @param rawType raw type
      * @param actualTypeArguments actual type arguments
      * 
      */
     public ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments) {
         this.rawType = rawType;
         this.actualTypeArguments = actualTypeArguments;
     }
 
     /**
      * {@inheritDoc}
      */
     public Type[] getActualTypeArguments() {
         return actualTypeArguments;
     }
 
     /**
      * {@inheritDoc}
      */
     public Type getOwnerType() {
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public Type getRawType() {
         return rawType;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return new ToStringBuilder(this).append("rawType", rawType).append("actualTypeArguments",
                 actualTypeArguments).toString();
     }
 
 }
