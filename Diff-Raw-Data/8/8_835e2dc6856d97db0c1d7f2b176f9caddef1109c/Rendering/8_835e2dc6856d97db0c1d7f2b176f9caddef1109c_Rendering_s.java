 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.rendering;
 
 /**
  * Static utility class for rendering concers. All comparision
  * methods are <strong>not necessarily transitive nor symetric</strong>.
  *
  * @author Willi Schoenborn
  */
 public final class Rendering {
 
     private Rendering() {
         
     }
     
     /**
     * Creates a {@link RenderingLevel} which is greater than everyother
      * level but equals to itself.
      * 
      * <p>
      *   The returned level is interoperable with other implementations.
      * </p>
      * 
      * <p>
      *   <strong>Note</strong>: In contrast to the name, it's still possible
      *   to write a custom level implementation which is greater than the returned
     *   level. Beware that all compare methods are not necessarily symetric.
      * </p>
      * 
      * @return a rendering level which is greater than every other level
      */
     public static RenderingLevel maxLevel() {
         return MaxRenderingLevel.INSTANCE;
     }

     /**
      * Uses the specified int to create a rendering level.
      * 
      * <p>
      *   The returned level is <strong>not</strong> interoperable with
      *   other implementations.
      * </p>
      * 
      * @param value the backing int value
      * @return a rendering level which compares int values
      *         to provide comparision
      */
     public static RenderingLevel asLevel(int value) {
         return new IntegerRenderingLevel(value);
     }
     
     /**
      * Creates a rendering level which is backed by the specified comparable.
      * 
      * <p>
      *   The returned level is interoperable when the given comparable supports it.
      * </p>
      * 
      * @param comparable the backing comparable
      * @return a rendering level which compares levels using the semantics
      *         of the specified comparable
      */
     public static RenderingLevel asLevel(Comparable<? super RenderingLevel> comparable) {
         return new ComparableRenderingLevel(comparable);
     }
     
     /**
      * Creates a value renderer for type t which enforces the default behaviour
      * of unknown value type rendering by delegating to {@link Renderer#value(Object)}.
      * 
      * @param <T> the generic value type
      * @return a value renderer which delegates back to the renderer
      */
     // safe because the parameter will just be passed to Renderer.value(Object)
     @SuppressWarnings("unchecked")
     public static <T> ValueRenderer<T> defaultValueRenderer() {
         return (ValueRenderer<T>) DefaultValueRenderer.INSTANCE;
     }
     
 }
