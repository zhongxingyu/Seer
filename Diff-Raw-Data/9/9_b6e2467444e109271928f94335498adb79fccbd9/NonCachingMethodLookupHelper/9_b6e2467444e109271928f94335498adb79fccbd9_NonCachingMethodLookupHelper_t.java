 /*
  * Copyright 2006 Ville Peurala
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
 package org.laughingpanda.jretrofit;
 
 import java.lang.reflect.Method;
 
 /**
 * @author Ville Peurala
  */
 class NonCachingMethodLookupHelper extends AbstractMethodLookupHelper {
     public NonCachingMethodLookupHelper(Object target) {
         super(target);
     }
 
     Method findMethodToCall(Method interfaceMethod) {
         Method[] targetMethods = getTarget().getClass().getMethods();
         for (int i = 0; i < targetMethods.length; i++) {
             Method currentMethod = targetMethods[i];
             if (areMethodsCompatible(interfaceMethod, currentMethod)) {
                 return currentMethod;
             }
         }
         throw new UnsupportedOperationException("Target object '" + getTarget()
                 + "' does not have a method which is compatible with '"
                 + interfaceMethod + "'!");
     }
 }
