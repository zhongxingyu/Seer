 package uk.ac.ebi.arrayexpress.utils.persistence;
 
 /*
  * Copyright 2009-2011 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 
 import java.io.File;
 import java.io.IOException;
 
 public class FilePersistence<T extends Persistable> extends Persistence<T>
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private File persistenceFile;
 
     private final static String PERSISTENCE_FILE_ENCODING = "UTF-8";
 
     public FilePersistence( T object, File persistenceFile )
     {
         super(object);
         this.persistenceFile = persistenceFile;
     }
 
     protected void persist( T object ) throws IOException
     {
         logger.debug("Persisting object [{}] to [{}]"
                 , object.getClass().toString()
                 , persistenceFile.getName());
 
         StringTools.stringToFile(object.toPersistence(), persistenceFile, PERSISTENCE_FILE_ENCODING);
 
     }
 
     protected void restore( T object ) throws IOException
     {
 
         logger.debug("Restoring object [{}] from [{}]"
                 , object.getClass().toString()
                 , persistenceFile.getName());
 
        if (persistenceFile.exists()) {
            String objectString = StringTools.fileToString(persistenceFile, PERSISTENCE_FILE_ENCODING);
            object.fromPersistence(objectString);
        } else {
            logger.warn("Persistance file [{}] not found", persistenceFile.getName());
        }
     }
 }
