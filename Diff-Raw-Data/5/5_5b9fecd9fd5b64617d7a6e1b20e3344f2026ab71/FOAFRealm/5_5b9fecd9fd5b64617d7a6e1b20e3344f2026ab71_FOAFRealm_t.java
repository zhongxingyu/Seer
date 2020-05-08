 /*
  * Copyright 2007 Wyona
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.wyona.org/licenses/APACHE-LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.wyona.yanel.impl.map;
 
 import org.apache.log4j.Category;
 
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 
 import org.wyona.commons.io.FileUtil;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 
 /**
  *
  */
 public class FOAFRealm extends org.wyona.yanel.core.map.Realm {
 
     private Category log = Category.getInstance(FOAFRealm.class);
 
     Repository profilesRepo;
 
     /**
      *
      */
     public FOAFRealm(String name, String id, String mountPoint, java.io.File configFile) throws Exception {
         super(name, id, mountPoint, configFile);
        log.debug("Custom FOAF Realm implementation!");
 
         DefaultConfigurationBuilder builder =  new DefaultConfigurationBuilder(true);
         Configuration config = builder.buildFromFile(configFile);
         java.io.File profilesDataRepoConfigFile = new java.io.File(config.getChild("profiles-data").getValue());
        log.debug("Profiles Data repo path: " + profilesDataRepoConfigFile);
 
         if (!profilesDataRepoConfigFile.isAbsolute()) {
             profilesDataRepoConfigFile = FileUtil.file(configFile.getParentFile().getAbsolutePath(), profilesDataRepoConfigFile.toString());
         }
         profilesRepo = new RepositoryFactory().newRepository("profiles", profilesDataRepoConfigFile);
     }
 
     /**
      *
      */
     public Repository getProfilesRepository() {
         return profilesRepo;
     }
 
     /**
      *
      */
     public void destroy() throws Exception {
         this.destroy();
         getProfilesRepository().close();
     }
 }
