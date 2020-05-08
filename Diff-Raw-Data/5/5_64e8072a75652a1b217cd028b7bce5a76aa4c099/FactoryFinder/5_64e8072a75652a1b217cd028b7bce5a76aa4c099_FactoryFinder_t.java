 /**
  *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.fusesource.meshkeeper.distribution;
 
 
 import java.io.IOException;
 import java.util.concurrent.ConcurrentHashMap;
 
 
 public class FactoryFinder {
     private final String path;
     private final ConcurrentHashMap<String, Class<?>> classMap = new ConcurrentHashMap<String, Class<?>>();
 
     public FactoryFinder(String path) {
         this.path = path;
     }
 
     /**
      * Creates a new instance of the the class associated with the specified key
      * 
      * @param key
      *            is the key to add to the path to find a text file containing
      *            the factory class name
      * @return a newly created instance
      */
     public <T> T create(String key) throws IllegalAccessException, InstantiationException, IOException, ClassNotFoundException {
         Class<T> clazz = find(key);
         return clazz.newInstance();
     }
 
     /**
      * Creates a new instance of the the class associated with the specified key
      * 
      * @param key
      *            is the key to add to the path to find a text file containing
      *            the factory class name
      * @return a newly created instance
      */
     @SuppressWarnings("unchecked")
     public <T> Class<T> find(String key) throws ClassNotFoundException, IOException {
        Class<?> clazz = classMap.get(key);
         if (clazz == null) {
             clazz = loadClass(key);
             classMap.put(key, clazz);
         }
        return (Class<T>) clazz;
     }
 
     private Class<?> loadClass(String key) throws ClassNotFoundException, IOException {
         return PluginClassLoader.getContextPluginLoader().loadPlugin(path, key);
     }
 }
