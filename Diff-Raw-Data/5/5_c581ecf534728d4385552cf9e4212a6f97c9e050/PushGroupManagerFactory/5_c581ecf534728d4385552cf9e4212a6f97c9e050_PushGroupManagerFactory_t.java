 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  *
  */
 package org.icepush;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 
 import org.icepush.util.AnnotationScanner;
 
 public class PushGroupManagerFactory {
     private static final Logger LOGGER = Logger.getLogger(PushGroupManagerFactory.class.getName());
 
     public static PushGroupManager newPushGroupManager(final ServletContext servletContext, final Configuration configuration) {
        String _pushGroupManager = (String)servletContext.getAttribute("org.icepush.PushGroupManager");
        if (_pushGroupManager == null) {
            _pushGroupManager = configuration.getAttribute("pushGroupManager", null);
        }
         if (_pushGroupManager == null || _pushGroupManager.trim().length() == 0) {
             LOGGER.log(Level.FINEST, "Using annotation scanner to find @ExtendedPushGroupManager.");
             Set<String> _annotationSet = new HashSet<String>();
             _annotationSet.add("Lorg/icepush/ExtendedPushGroupManager;");
             AnnotationScanner _annotationScanner = new AnnotationScanner(_annotationSet, servletContext);
             try {
                 Class[] _classes = _annotationScanner.getClasses();
                 // throws IOException
                 for (Class _class : _classes) {
                     try {
                         return
                             (PushGroupManager)
                                 _class.
                                     // throws NoSuchMethodException, SecurityException
                                     getConstructor(ServletContext.class).
                                     // throws
                                     //     IllegalAccessException, IllegalArgumentException, InstantiationException,
                                     //     InvocationTargetException, ExceptionInInitializerError
                                     newInstance(servletContext);
                     } catch (NoSuchMethodException exception) {
                         LOGGER.log(Level.FINEST, "Can't get constructor!", exception);
                         // Do nothing.
                     } catch (SecurityException exception) {
                         LOGGER.log(Level.FINEST, "Can't get constructor!", exception);
                         // Do nothing.
                     } catch (IllegalAccessException exception) {
                         LOGGER.log(Level.FINEST, "Can't create instance!", exception);
                         // Do nothing.
                     } catch (IllegalArgumentException exception) {
                         LOGGER.log(Level.FINEST, "Can't create instance!", exception);
                         // Do nothing.
                     } catch (InstantiationException exception) {
                         LOGGER.log(Level.FINEST, "Can't create instance!", exception);
                         // Do nothing.
                     } catch (InvocationTargetException exception) {
                         LOGGER.log(Level.FINEST, "Can't create instance!", exception);
                         // Do nothing.
                     } catch (ExceptionInInitializerError error) {
                         LOGGER.log(Level.FINEST, "Can't create instance!", error);
                         // Do nothing.
                     }
                 }
             } catch (IOException exception) {
                 if (LOGGER.isLoggable(Level.FINEST)) {
                     LOGGER.log(
                         Level.FINEST,
                         "An I/O error occurred while trying to get the classes.",
                         exception);
                 }
                 // Do nothing.
             }
         } else {
             LOGGER.log(Level.FINEST, "PushGroupManager: " + _pushGroupManager);
             try {
                 return
                     (PushGroupManager)
                         // throws ClassNotFoundException, ExceptionInInitializerError
                         Class.forName(_pushGroupManager).
                             // throws NoSuchMethodException, SecurityException
                             getConstructor(ServletContext.class).
                             // throws
                             //     IllegalAccessException, IllegalArgumentException, InstantiationException,
                             //     InvocationTargetException, ExceptionInInitializerError
                             newInstance(servletContext);
             } catch (ClassNotFoundException exception) {
                 LOGGER.log(Level.FINEST, "Can't find class!", exception);
                 // Do nothing.
             } catch (ExceptionInInitializerError error) {
                 LOGGER.log(Level.FINEST, "Can't find class or can't create instance!", error);
                 // Do nothing.
             } catch (NoSuchMethodException exception) {
                 LOGGER.log(Level.FINEST, "Can't get constructor!", exception);
                 // Do nothing.
             } catch (SecurityException exception) {
                 LOGGER.log(Level.FINEST, "Can't get constructor!", exception);
                 // Do nothing.
             } catch (IllegalAccessException exception) {
                 LOGGER.log(Level.FINEST, "Can't create instance!", exception);
                 // Do nothing.
             } catch (IllegalArgumentException exception) {
                 LOGGER.log(Level.FINEST, "Can't create instance!", exception);
                 // Do nothing.
             } catch (InstantiationException exception) {
                 LOGGER.log(Level.FINEST, "Can't create instance!", exception);
                 // Do nothing.
             } catch (InvocationTargetException exception) {
                 LOGGER.log(Level.FINEST, "Can't create instance!", exception);
                 // Do nothing.
             }
         }
         LOGGER.log(Level.FINEST, "Falling back to LocalPushGroupManager.");
         return new LocalPushGroupManager(servletContext);
     }
 }
