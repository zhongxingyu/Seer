 // @formatter:off
 /*
  * Copyright 2012 the original author or authors.
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
 // @formatter:on
 
 package ws.rocket.path.annotation;
 
 import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
 import static java.lang.annotation.ElementType.PARAMETER;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 import javax.enterprise.util.Nonbinding;
 import javax.inject.Qualifier;
 
 /**
  * CDI qualifying annotation, which triggers a tree construction where the root node value is with provided name or
  * type. When omitted, the field or parameter name is used for looking up a CDI bean with the same name. This annotation
  * must precede a variable with type of {@link ws.rocket.path.TreeNode}.
  * <p>
  * Usage: specify either the root node value bean name or type. Do not specify both as only one will be used:
  * <ol>
  * <li><code>type</code> when <code>type != Object.class</code>;
  * <li><code>value</code> when <code>type == Object.class</code>.
  * </ol>
  * <p>
  * This annotation refers to a value bean of the root node to be created. When the resolved bean is also annotated with
  * {@link ws.rocket.path.annotation.TreeNode} annotation, it will be used to create subtrees of the root node.
  * 
  * @author Martti Tamm
  */
 @Documented
 @Qualifier
 @Retention(RetentionPolicy.RUNTIME)
@Target({ PARAMETER, FIELD, METHOD })
 public @interface RootNode {
 
   /**
    * The name of the bean to be used as <code>TreeNode</code> value object. Defaults to field or parameter name where
    * the annotation is used.
    * <p>
    * To be effective, the targeted value bean must be annotated with {@link javax.inject.Named} annotation.
    * <p>
    * This annotation value is used when the {@link #type()} attribute value equals to <code>Object.class</code>.
    * 
    * @return The name of the value bean according to CDI.
    */
   @Nonbinding
   String value() default "";
 
   /**
    * The type of the bean to be used as <code>TreeNode</code> value object. Defaults to <code>Object.class</code>, which
    * means that this attribute won't be used.
    * <p>
    * When using type, the CDI needs to resolve it to exactly one bean or tree construction will fail.
    * <p>
    * This annotation value is used when the this attribute value does not equal to <code>Object.class</code>.
    * 
    * @return The type of the value bean.
    */
   @Nonbinding
   Class<?> type() default Object.class;
 
 }
