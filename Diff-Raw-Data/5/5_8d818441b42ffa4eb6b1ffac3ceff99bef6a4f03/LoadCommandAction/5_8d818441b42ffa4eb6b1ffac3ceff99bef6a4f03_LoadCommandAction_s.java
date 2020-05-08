 /*
  * Copyright 2013 Mobile Helix, Inc.
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
 package org.helix.mobile.model;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.faces.FacesException;
 
 /**
  *
  * @author shallem
  */
 public class LoadCommandAction {
     private static final Logger LOG = Logger.getLogger(LoadCommandAction.class.getName());
     
     private Constructor ctor;
     private Method loader;
     private Method getter;
     private Method postConstruct;
     private String beanName;
     private String key;
     private Class beanClass;
     
     public LoadCommandAction(String key,
             String beanName,
             Class c,
             Constructor ctor,
             Method loader,
             Method getter) {
         this.key = key;
         this.ctor = ctor;
         this.loader = loader;
         this.getter = getter;
         this.beanName = beanName;
         this.beanClass = c;
         
         for (Method m : c.getMethods()) {
             if (m.getAnnotation(javax.annotation.PostConstruct.class) != null) {
                 this.postConstruct = m;
                 break;
             }
         }
     }
     
     public Object doLoad(Object thisObject) throws FacesException {
         /*Object thisObject = null;
         try {
             thisObject = ctor.newInstance(new Object[]{});
             if (this.postConstruct != null) {
                 this.postConstruct.invoke(thisObject, new Object[]{});
             } 
         } catch(Exception e) {
 
         }
         if (thisObject == null) {
             throw new FacesException("Failed to construct object with constructor " + ctor.toString());
         }*/
         try {
             /* NOTE: we use an explicit list of catch blocks here so that application specific
              * exceptions are not caught. This is intentional.
              */
             loader.invoke(thisObject, new Object[] {});
         } catch (IllegalAccessException ex) {
             LOG.log(Level.SEVERE, null, ex);
             throw new FacesException("Failed to invoke loader: " + ex.getMessage());
         } catch (IllegalArgumentException ex) {
             LOG.log(Level.SEVERE, null, ex);
             throw new FacesException("Failed to invoke loader: " + ex.getMessage());
         } catch (InvocationTargetException ex) {
             LOG.log(Level.SEVERE, null, ex);
            throw new FacesException("Failed to invoke loader: " + ex.getMessage());
         }
         return thisObject;
     }
     
     public String getAndSerialize(Object thisObject) throws IOException {
         Object gotten = null;
         try {
             /* NOTE: we use an explicit list of catch blocks here so that application specific
              * exceptions are not caught. This is intentional.
              */
             gotten = getter.invoke(thisObject, new Object[] {});
         } catch (IllegalAccessException ex) {
             LOG.log(Level.SEVERE, null, ex);
             throw new FacesException("Failed to invoke loader: " + ex.getMessage());
         } catch (IllegalArgumentException ex) {
             LOG.log(Level.SEVERE, null, ex);
             throw new FacesException("Failed to invoke loader: " + ex.getMessage());
         } catch (InvocationTargetException ex) {
             LOG.log(Level.SEVERE, null, ex);
             throw new FacesException("Failed to invoke loader: " + ex.getTargetException().getMessage());
         }
         if (gotten != null) {
             JSONSerializer s = new JSONSerializer();
             try {
                 /* NOTE: we use an explicit list of catch blocks here so that application specific
                  * exceptions are not caught. This is intentional.
                  */
                 return s.serializeObject(gotten);
             } catch (IllegalAccessException ex) {
                 LOG.log(Level.SEVERE, null, ex);
                 throw new FacesException("Failed to invoke getter: " + ex.getMessage());
             } catch (IllegalArgumentException ex) {
                 LOG.log(Level.SEVERE, null, ex);
                 throw new FacesException("Failed to invoke getter: " + ex.getMessage());
             } catch (InvocationTargetException ex) {
                 LOG.log(Level.SEVERE, null, ex);
                throw new FacesException("Failed to invoke getter: " + ex.getMessage());
             } catch (NoSuchMethodException ex) {
                 LOG.log(Level.SEVERE, null, ex);
                 throw new FacesException("Failed to invoke getter: " + ex.getMessage());
             }
         } else {
             return "{ 'error' : 'Failed to serialize object.' }";
         }
     }
 
     public String getBeanName() {
         return beanName;
     }
 
     public Class getBeanClass() {
         return beanClass;
     }
 }
