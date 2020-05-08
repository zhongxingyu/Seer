 package org.jtheque.resources.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 /*
  * Copyright JTheque (Baptiste Wicht)
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
 
 public class ModuleDescriptor {
     private final String id;
     private final List<ModuleVersion> moduleVersions = new ArrayList<ModuleVersion>(5);
 
     public ModuleDescriptor(String id) {
         super();
 
         this.id = id;
     }
 
     public String getId() {
         return id;
     }
 
     public Collection<ModuleVersion> getVersions() {
         return moduleVersions;
     }
 
     public void addVersion(ModuleVersion version) {
         moduleVersions.add(version);
     }
 
     /**
      * Return the most recent version of the VersionsFile.
      *
      * @return The most recent version.
      *
      * @throws NoSuchElementException If the version's file contains no version.
      */
     public ModuleVersion getMostRecentVersion() {
         if (moduleVersions.isEmpty()) {
            throw new NoSuchElementException("The descriptor contains no versions. ");
         }
 
         return moduleVersions.get(moduleVersions.size() - 1);
     }
 }
