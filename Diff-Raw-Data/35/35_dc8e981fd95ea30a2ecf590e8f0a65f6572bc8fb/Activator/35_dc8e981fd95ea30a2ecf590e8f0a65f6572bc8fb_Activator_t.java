 package sorcer.boot.load;
 /**
  *
  * Copyright 2013 Rafał Krupiński.
  * Copyright 2013 Sorcersoft.com S.A.
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
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.net.URL;
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
 
     public void activate(ClassLoader cl, URL[] jars) throws Exception {
         for (URL jar : jars) {
             activate(cl, jar);
         }
     }
 
     public void activate(URL[] jars) throws Exception {
         activate(Thread.currentThread().getContextClassLoader(), jars);
     }
 
     public void activate(ClassLoader cl, URL jarUrl) throws Exception {
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
             String activatorClassName = mainAttributes.getValue(ServiceActivator.KEY_ACTIVATOR);
             if (activatorClassName == null) return;
 
            activate(cl, jarFile, activatorClassName);
 
         } catch (IOException e) {
             throw new IllegalArgumentException("Could not open jar file " + jarUrl, e);
         } catch (ClassNotFoundException e) {
             throw new IllegalArgumentException("Could not find Sorcer-Activator class from " + jarUrl, e);
         } catch (InstantiationException e) {
             throw new IllegalArgumentException("Could not instantiate Sorcer-Activator class from " + jarUrl, e);
         } catch (IllegalAccessException e) {
             throw new IllegalArgumentException("Could not instantiate Sorcer-Activator class from " + jarUrl, e);
         }
     }

    private void activate(ClassLoader cl, File jarFile, String activatorClassName) throws Exception {
        Class<?> activatorClass = Class.forName(activatorClassName, true, cl);
        if (!ServiceActivator.class.isAssignableFrom(activatorClass)) {
            throw new IllegalArgumentException("Activator class " + activatorClassName + " must implement ServiceActivator");
        }
        if (activatorClass.isInterface() || Modifier.isAbstract(activatorClass.getModifiers())) {
            throw new IllegalArgumentException("Activator class " + activatorClassName + " must be concrete");
        }
        log.info("Activating {} with class {}", jarFile, activatorClassName);
        ServiceActivator activator = (ServiceActivator) activatorClass.newInstance();
        activator.activate();
    }
 }
