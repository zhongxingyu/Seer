 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INSFO-RI-261552.
 
  Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 package eu.stratuslab.hudson;
 
 import hudson.model.Descriptor;
 import hudson.model.Node;
 import hudson.slaves.NodeProperty;
 
 import java.io.IOException;
import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.logging.Logger;
 
 public class SlaveCreator implements Callable<Node> {
 
     private static final Logger LOGGER = Logger.getLogger(StratusLabCloud.class
             .getName());
 
     private final SlaveTemplate template;
 
     private final CloudParameters cloudParams;
 
     private final String[] names;
 
     public SlaveCreator(SlaveTemplate template, CloudParameters cloud,
             String[] names) {
 
         this.template = template;
         this.cloudParams = cloud;
        this.names = Arrays.copyOf(names, names.length);
     }
 
     public Node call() throws IOException, StratusLabException,
             Descriptor.FormException {
 
         List<? extends NodeProperty<?>> nodeProperties = new LinkedList<NodeProperty<Node>>();
 
         LOGGER.info("creating slave for " + names[2]);
 
         CloudSlave slave = new CloudSlave(cloudParams, template, names[1],
                 names[2], template.remoteFS, template.executors,
                 Node.Mode.NORMAL, names[0], nodeProperties);
 
         LOGGER.info("slave created for " + names[2]);
 
         return slave;
     }
 
 }
