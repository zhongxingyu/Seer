 package org.jtheque.core;
 
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
 
 import org.jtheque.utils.SystemProperty;
 import org.jtheque.utils.annotations.ThreadSafe;
 import org.jtheque.utils.io.FileUtils;
 
 import java.io.File;
 
 /**
  * Give access to the folders of the application.
  *
  * @author Baptiste Wicht
  */
 @ThreadSafe
 public final class Folders {
     private static final File CORE_FOLDER;
     private static final File APPLICATION_FOLDER;
     private static final File MODULES_FOLDER;
 
     static {
         File userDir = new File(SystemProperty.USER_DIR.get());
 
        CORE_FOLDER = new File(userDir, "application");
         FileUtils.createIfNotExists(CORE_FOLDER);
 
        APPLICATION_FOLDER = new File(userDir, "core");
         FileUtils.createIfNotExists(APPLICATION_FOLDER);
 
         MODULES_FOLDER = new File(userDir, "modules");
         FileUtils.createIfNotExists(MODULES_FOLDER);
     }
 
     /**
      * Utility class, not instantiable. 
      */
     private Folders() {
         throw new AssertionError();
     }
 
     /**
      * Return the core folder. It seems the folder where the files of the core are located.
      *
      * @return The File object who denotes the core folder.
      */
     public static File getCoreFolder() {
         return CORE_FOLDER;
     }
 
     /**
      * Return the application folder.
      *
      * @return The File object who denotes the application folder.
      */
     public static File getApplicationFolder() {
         return APPLICATION_FOLDER;
     }
 
     /**
      * Return the modules folder. It seems the folder where the modules are located.
      *
      * @return The File object who denotes the modules folder.
      */
     public static File getModulesFolder() {
         return MODULES_FOLDER;
     }
 }
