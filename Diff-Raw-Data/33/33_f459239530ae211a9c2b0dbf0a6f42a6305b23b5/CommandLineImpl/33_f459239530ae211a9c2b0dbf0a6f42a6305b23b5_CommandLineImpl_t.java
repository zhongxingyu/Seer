 /*
  * Copyright (c) 2010-2011, Dmitry Sidorenko. All Rights Reserved.
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
 
 package com.googlecode.commandme.impl;
 
 import com.googlecode.commandme.CliException;
 import com.googlecode.commandme.CommandLine;
 import com.googlecode.commandme.impl.interrogator.Interrogator;
 import com.googlecode.commandme.impl.interrogator.InterrogatorFactory;
 import com.googlecode.commandme.impl.introspector.ModuleIntrospector;
 import com.googlecode.commandme.impl.introspector.ModuleIntrospectorFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 /**
  * Implementation of command line.
  * Combines three phases: Definition, Parsing and Interrogation.
  *
  * @author Dmitry Sidorenko
  */
 public class CommandLineImpl<T> implements CommandLine<T> {
     @SuppressWarnings({"UnusedDeclaration"})
     private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineImpl.class);
 
 
     private ModuleIntrospector moduleIntrospector;
 
     private Class<T> clz;
     private T        instance;
 
     /**
      * Constructs an object
      *
      * @param clz class of a module
      * @throws CliException
      */
     public CommandLineImpl(Class<T> clz) {
         if (clz == null) {
             throw new NullPointerException("Can not pass null class");
         }
         this.clz = clz;
 
         moduleIntrospector = ModuleIntrospectorFactory.createIntrospector(clz);
         moduleIntrospector.inspect();
 
         instance = createInstance();
     }
 
     /**
      * Does actual work.
      */
     public T execute(String[] arguments) {
         Interrogator interrogator = InterrogatorFactory.createInterrogator(instance, moduleIntrospector, arguments);
         interrogator.torture();
 
         return instance;
     }
 
 
     /**
      * Creates an instance of given class. The class should have public no-arg constructor.
      *
      * @return created instance, never {@code null}
      * @throws CliException exception if class does not have no-arg constructor, constructor is not accessible , if class is null
      */
     private T createInstance() throws CliException {
         Constructor<T> constructor;
         try {
             constructor = clz.getConstructor();
         } catch (NoSuchMethodException e) {
             LOGGER.error("Error", e);
             throw new CliException("No public no-args constructor found", e);
         }
 
         T instance;
         try {
             instance = constructor.newInstance();
         } catch (InstantiationException e) {
             LOGGER.error("Error", e);
             throw new CliException(e);
         } catch (IllegalAccessException e) {
             LOGGER.error("Error", e);
             throw new CliException(e);
         } catch (InvocationTargetException e) {
             LOGGER.error("Error", e);
             throw new CliException(e);
         }
         return instance;
     }
 
     public T getModule() {
         return instance;
     }
 
    @Override
    public void shutdown() {
        clz = null;
        moduleIntrospector = null;
        instance = null;
    }

 }
