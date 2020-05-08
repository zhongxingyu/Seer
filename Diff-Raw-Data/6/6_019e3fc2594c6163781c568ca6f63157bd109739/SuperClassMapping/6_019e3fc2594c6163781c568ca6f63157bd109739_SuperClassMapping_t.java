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
 
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ForwardingMap;
 
 import de.cosmocode.commons.reflect.Reflection;
 
 /**
  * {@link Mapping} implementation which traverses the object hierarchy when
  * {@link Mapping#find(Class)} is called to find the most appropriate type mapping.
  *
  * @since 1.1
  * @author Willi Schoenborn
  */
 public final class SuperClassMapping extends ForwardingMap<Class<?>, ValueRenderer<?>> implements Mapping {
     
     private static final Logger LOG = LoggerFactory.getLogger(SuperClassMapping.class);
 
     private final Map<Class<?>, ValueRenderer<?>> renderers;
     
     public SuperClassMapping(Map<Class<?>, ValueRenderer<?>> renderers) {
         this.renderers = Preconditions.checkNotNull(renderers, "Renderers");
     }
 
     @Override
     protected Map<Class<?>, ValueRenderer<?>> delegate() {
         return renderers;
     }
     
     @Override
     public <T> ValueRenderer<T> find(Class<? extends T> type) {
         Preconditions.checkNotNull(type, "Type");
         
        if (type.isArray()) {
            @SuppressWarnings("unchecked")
            final ValueRenderer<T> renderer = (ValueRenderer<T>) renderers.get(Object[].class);
            return renderer;
        }
        
         for (Class<?> superType : Reflection.getAllSuperTypes(type)) {
             if (Object.class.equals(superType)) continue;
             LOG.trace("Considering {}", superType);
             final ValueRenderer<?> found = renderers.get(superType);
             if (found == null) continue;
             @SuppressWarnings("unchecked")
             final ValueRenderer<T> renderer = (ValueRenderer<T>) found;
             return renderer;
         }
 
         @SuppressWarnings("unchecked")
         final ValueRenderer<T> renderer = (ValueRenderer<T>) renderers.get(Object.class);
         return renderer;
     }
     
 }
