 /*
  * Copyright 2011 Zouhin Ro
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
 package magicware.scm.redmine.tools;
 
 import java.io.IOException;
 
 import magicware.scm.redmine.tools.config.Config;
 import magicware.scm.redmine.tools.config.ConfigFacade;
 import magicware.scm.redmine.tools.config.SyncItem;
 
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 
 public class AppEntry {
 
     public static void main(String[] args) throws InvalidFormatException,
             IOException {

        System.getProperty(Constants.CONFIG_FILE, args[0]);
 
         Config config = ConfigFacade.getConfig();
 
         IssueSyncApp app = new IssueSyncApp();
 
         for (SyncItem syncItem : config.getSyncItems()) {
             app.execute(syncItem);
         }
     }
 
 }
