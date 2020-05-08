 package org.rosenvold.spring.convention.interfacemappers;
 /*
  * Copyright 2011 the original author or authors.
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
 
 import org.rosenvold.spring.convention.InterfaceToImplementationMapper;
 
 /**
  * @author Kristian Rosenvold
  */
 public class StubSuffix implements InterfaceToImplementationMapper {
     private final PackageManipulator packageManipulator;
 
     public StubSuffix() {
         this( null);
     }
 
     public StubSuffix(PackageManipulator packageManipulator) {
         this.packageManipulator = packageManipulator;
     }
 
    @Override
     public String getBeanClassName(Class aClass) {
         String name = packageManipulator != null ? packageManipulator.getRemappedPackageName( aClass.getName()) : aClass.getName();
         return name + "Stub";
     }
 }
