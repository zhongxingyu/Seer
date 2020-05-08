 package sorcer.boot.load;
 /**
  * Copyright 2013, 2014 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sorcer.core.ServiceActivator;
 import sorcer.tools.ActivationFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 /**
  * Find, instantiate and call ServiceActivator implementation in jars
  *
  * @author Rafał Krupiński
  */
 public class Activator {
 
     private static final Logger log = LoggerFactory.getLogger(Activator.class);
 
     public abstract class AbstractClassEntryHandler {
         public void handle(String value) {
             try {
                 Class<?> klass = Class.forName(value, false, Thread.currentThread().getContextClassLoader());
                 handle(activationFactory.create(klass));
             } catch (ClassNotFoundException e) {
                 throw new IllegalArgumentException(value, e);
             } catch (InstantiationException e) {
                 throw new IllegalArgumentException(value, e);
             } catch (IllegalAccessException e) {
                 throw new IllegalArgumentException(value, e);
             }
         }
 
         public abstract void handle(Object instance);
     }
 
     public final class EntryHandlerEntry {
         public final String key;
         public final AbstractClassEntryHandler entryHandler;
 
         public EntryHandlerEntry(String key, AbstractClassEntryHandler entryHandler) {
             this.key = key;
             this.entryHandler = entryHandler;
         }
     }
 
     public final List<EntryHandlerEntry> entryHandlers;
 
     {
         entryHandlers = new LinkedList<EntryHandlerEntry>();
         entryHandlers.add(new EntryHandlerEntry(
                 ServiceActivator.KEY_ACTIVATOR,
                 new AbstractClassEntryHandler() {
                     @Override
                     public void handle(Object activator) {
                         if (activator instanceof ServiceActivator)
                             try {
                                 ((ServiceActivator) activator).activate();
                             } catch (Exception e) {
                                 log.error("Activating {}", activator, e);
                             }
                     }
                 }
         ));
     }
 
     private ActivationFactory activationFactory = new ActivationFactory() {
         @Override
         public Object create(Class c) throws IllegalAccessException, InstantiationException {
             return c.newInstance();
         }
     };
 
     public void activate(URL[] jars) throws Exception {
         for (URL jar : jars)
             activate(jar);
     }
 
     public void activate(URL jarUrl) throws Exception {
         JarFile jar;
         try {
            File jarFile = new File(jarUrl.getFile());
             if (!jarFile.exists()) {
                 log.info("Skip non-existent dir {}", jarFile);
                 return;
             }
             if (jarFile.isDirectory()) {
                 log.debug("Skip directory {}", jarFile);
                 return;
             }
             jar = new JarFile(jarFile);
 
             Manifest manifest = jar.getManifest();
             if (manifest == null) {
                 log.debug("No manifest in {}", jarFile);
                 return;
             }
             Attributes mainAttributes = manifest.getMainAttributes();
             for (EntryHandlerEntry e : entryHandlers) {
                 String activatorClassName = mainAttributes.getValue(e.key);
                 if (activatorClassName != null)
                     e.entryHandler.handle(activatorClassName);
             }
         } catch (IOException e) {
             throw new IllegalArgumentException("Could not open jar file " + jarUrl, e);
         }
     }
 
     public void setActivationFactory(ActivationFactory activationFactory) {
         this.activationFactory = activationFactory;
     }
 }
