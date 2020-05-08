 /**
  * Copyright (C) 2007-2008 Elastic Grid, LLC.
  * 
  * Licensed under the GNU Lesser General Public License, Version 3.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.gnu.org/licenses/lgpl-3.0.html
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.elasticgrid.model;
 
 import com.elasticgrid.model.ec2.EC2Node;
 import com.elasticgrid.model.ec2.EC2Grid;
 import java.io.Serializable;
 import java.util.List;
 import java.util.Set;
 import java.net.InetAddress;
 
 /**
  * @author Jerome Bernard
  */
 public interface Grid<N extends Node> extends Serializable {
     String getName();
     Grid<N> name(String name);
    Grid<N> status(Status status);
     boolean isRunning();
     Set<N> getNodes();
     List<Application> getApplications();
     Application application(String name);
     Grid<N> addNodes(List<N> nodes);
 
     enum Status {
         RUNNING, STOPPED
     }
 }
