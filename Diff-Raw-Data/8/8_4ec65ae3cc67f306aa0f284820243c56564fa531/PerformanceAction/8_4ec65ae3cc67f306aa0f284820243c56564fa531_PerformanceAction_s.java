 /*
  * Copyright 2012 Canoo Engineering AG.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.canoo.dolphin.demo;
 
 import com.canoo.dolphin.core.PresentationModel;
 import com.canoo.dolphin.core.server.DTO;
 import com.canoo.dolphin.core.server.Slot;
 import com.canoo.dolphin.core.server.action.DolphinServerAction;
 import com.canoo.dolphin.core.server.comm.ActionRegistry;
 import com.canoo.dolphin.core.server.comm.CommandHandler;
 import com.canoo.dolphin.core.server.comm.SimpleCommandHandler;
 
 import java.util.LinkedList;
 
 public class PerformanceAction extends DolphinServerAction {
     static long id = 0;
 
     private final CommandHandler stressAction = new SimpleCommandHandler() {
         public void handleCommand() {
             PresentationModel pm = getServerDolphin().getAt("input");
             if (pm == null) {
                 throw new IllegalStateException("No input criteria known on the server!");
             }
             int count    = pm.getValue("count", 1);
             int attCount = pm.getValue("attCount", 1);
 
             for (int pmCount = 0; pmCount < count; pmCount++) {
                 LinkedList<Slot> slots = new LinkedList();
                 for (int attI = 0; attI < attCount; attI++) {
                     slots.add(new Slot("att" + Long.toString(id++), "val" + Long.toString(id++)));
                 }
                 presentationModel(Long.toString(id++), "all", new DTO(slots));
             }
         }
     };
 
    private final CommandHandler syncAction = new SimpleCommandHandler() {
        public void handleCommand() {
            // do nothing; this is only for synchronizing the timer after "clear"
        }
    };

     public void registerIn(final ActionRegistry registry) {
        registry.register("sync",       syncAction);
         registry.register("stressTest", stressAction);
     }
 
 }
