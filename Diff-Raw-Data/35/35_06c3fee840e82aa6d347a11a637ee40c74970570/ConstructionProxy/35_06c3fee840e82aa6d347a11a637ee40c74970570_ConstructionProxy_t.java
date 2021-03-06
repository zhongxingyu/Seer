 /**
  * Copyright (C) 2006 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.inject.internal;
 
 import com.google.inject.Parameter;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Member;
 import java.util.List;
 
 /**
  * Proxies calls to a {@link java.lang.reflect.Constructor} for a class
  * {@code T}.
  *
  * @author crazybob@google.com (Bob Lee)
  */
 public interface ConstructionProxy<T> {
 
   /**
    * Constructs an instance of {@code T} for the given arguments.
    */
   T newInstance(Object... arguments) throws InvocationTargetException;
 
   List<Parameter<?>> getParameters();
 
  /**
   * Returns the injected method or constructor. If the injected member is
   * synthetic (such as generated code for method interception), the natural
   * constructor is returned.
   */
   Member getMember();
 }
