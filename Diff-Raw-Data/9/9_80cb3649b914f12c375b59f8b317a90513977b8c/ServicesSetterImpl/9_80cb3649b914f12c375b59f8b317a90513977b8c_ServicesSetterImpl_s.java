 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 package org.amplafi.hivemind.factory.servicessetter;
 
 import static org.apache.hivemind.util.PropertyUtils.*;
 
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.amplafi.hivemind.annotations.InjectService;
 import org.amplafi.hivemind.annotations.NotService;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hivemind.internal.Module;
 import org.apache.hivemind.util.PropertyAdaptor;
 
 
 /**
  * Utility class that allows wiring up existing services (from a hivemind
  * registry) into a given object.
  *
  * @author andyhot
  */
 public class ServicesSetterImpl implements ServicesSetter {
 
     private Module module;
 
     public ServicesSetterImpl() {
     }
 
     public void setModule(Module module) {
         this.module = module;
     }
 
     @SuppressWarnings("unchecked")
     public void wire(Object obj) {
         wire(obj, Collections.EMPTY_LIST);
     }
 
     public void wire(Object obj, String... excludedProperties) {
         wire(obj, Arrays.asList(excludedProperties));
     }
 
     /**
      *
      * @see org.amplafi.hivemind.factory.servicessetter.ServicesSetter#wire(java.lang.Object, java.lang.Iterable)
      */
     @SuppressWarnings("unchecked")
     public void wire(Object obj, Iterable<String> excludedProperties) {
         if ( obj == null ) {
             return;
         }
         List<String> props = getWriteableProperties(obj);
         for(String exclude: excludedProperties) {
             props.remove(exclude);
         }
 
         for (String prop : props) {
             PropertyAdaptor type = getPropertyAdaptor(obj, prop);
             Class propertyType = type.getPropertyType();
             if (propertyType.isPrimitive() || propertyType.isAnnotation() || propertyType.isArray() || propertyType.isEnum()) {
                 // and primitive types of course can't be injected!
                 continue;
             }
             if ( propertyType.getPackage().getName().startsWith("java")) {
                 // if it is a standard java class then lets exclude it.
                 continue;
             }
             // if it is not readable then, then we can't verify that
             // we are not overwriting non-null property.
             if ( !type.isReadable() || !type.isWritable()) {
                 continue;
             }
             if ( propertyType.getAnnotation(NotService.class) != null) {
                 continue;
             }
 
             // check to see if we have a service to offer before bothering
             // to checking if the property can be set. This avoids triggering
             // actions caused by calling the get/setters.
             Object srv;
             if ( type.getPropertyType() == Log.class) {
                 // log is special.
                 srv = LogFactory.getLog(obj.getClass());
             } else {
                 srv = null;
                 String propertyAccessorMethodName = "set"+StringUtils.capitalize(type.getPropertyName());
                 InjectService service;
                 try {
                     service = findServiceAnnotation(obj, propertyAccessorMethodName, propertyType);
                     if ( service == null ) {
                         propertyAccessorMethodName = "get"+StringUtils.capitalize(type.getPropertyName());
                         service = findServiceAnnotation(obj, propertyAccessorMethodName);
                     }
                     if ( service == null && (propertyType == boolean.class || propertyType == Boolean.class)) {
                         propertyAccessorMethodName = "is"+StringUtils.capitalize(type.getPropertyName());
                         service = findServiceAnnotation(obj, propertyAccessorMethodName);
                     }
                 } catch(DontInjectException e) {
                     // do nothing
                     continue;
                 }
 
                 if ( service != null ) {
                     String serviceName = service.value();
                     if ( StringUtils.isNotBlank(serviceName)) {
                         for (String attempt: new String[] {
                                 serviceName,
                                 serviceName +'.' +type.getPropertyName(),
                                 serviceName +'.' +StringUtils.capitalize(type.getPropertyName())
                         }) {
                             try {
                                 srv = module.getService(attempt, propertyType);
                                 if ( srv != null ) {
                                     serviceName = attempt;
                                     break;
                                 }
                             }catch(Exception e) {
                                 // oh well... not around.
                             }
                         }
                     }
                 }
                 if ( srv == null) {
                     try {
                         srv = module.getService(propertyType);
                     } catch (Exception e) {
                     }
                 }
             }
             // Doing the read check last avoids
             // triggering problems caused by lazy initialization and read-only properties.
             if ( srv != null && type.read(obj) == null) {
                type.write(obj, srv);
             }
         }
     }
 
     /**
      * @param obj
      * @param propertyAccessorMethodName
      * @param propertyType
      * @return
      * @throws DontInjectException
      */
     private InjectService findServiceAnnotation(Object obj, String propertyAccessorMethodName, Class<?>... propertyType) throws DontInjectException {
         // look for @InjectService
         InjectService service= null;
 
         try {
             Method m = obj.getClass().getMethod(propertyAccessorMethodName, propertyType);
             if ( m.getAnnotation(NotService.class) != null ) {
                 throw new DontInjectException();
             }
             service = m.getAnnotation(InjectService.class);
             if ( service == null ) {
                 for(Class<?> cls: obj.getClass().getInterfaces()) {
                     try {
                         m = cls.getMethod(propertyAccessorMethodName, propertyType);
                         if ( m.getAnnotation(NotService.class) != null ) {
                             throw new DontInjectException();
                         }
                         service = m.getAnnotation(InjectService.class);
                         if ( service != null ) {
                             break;
                         }
                     } catch(NoSuchMethodException e) {
 
                     }
                 }
             }
         } catch(NoSuchMethodException e) {
 
         }
         return service;
     }
     private class DontInjectException extends Exception {
 
     }
 }
