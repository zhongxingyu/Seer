 /*
  *  Copyright 2008 the original author or authors.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package net.sf.ipsedixit.annotation;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Inherited;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import net.sf.ipsedixit.core.FieldHandler;
 import net.sf.ipsedixit.core.ObjectAnalyser;
import net.sf.ipsedixit.core.impl.NonFinalFieldObjectAnalyser;
 
 /**
  * This annotation allows some customisation of Ipsedixit in the scope of the class, or package the class is defined in.
  */
 @Target({ElementType.TYPE, ElementType.PACKAGE})
 @Retention(RetentionPolicy.RUNTIME)
 @Inherited
 public @interface Ipsedixit {
 
     /**
      * Return the {@link net.sf.ipsedixit.core.ObjectAnalyser} implementation to use.
      */
    Class<? extends ObjectAnalyser> value() default NonFinalFieldObjectAnalyser.class;
 
     /**
      * Return the types of any additional {@link net.sf.ipsedixit.core.FieldHandler}'s to use.
      */
     Class<? extends FieldHandler>[] additionalHandlers() default {};
 }
