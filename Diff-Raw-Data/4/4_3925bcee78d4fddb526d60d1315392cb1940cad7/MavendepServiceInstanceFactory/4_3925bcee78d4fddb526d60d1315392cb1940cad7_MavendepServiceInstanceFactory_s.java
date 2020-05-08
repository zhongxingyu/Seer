 /**
  * Licensed to the Austrian Association for Software Tool Integration (AASTI)
  * under one or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information regarding copyright
  * ownership. The AASTI licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.connector.mavendep.internal;
 
 import java.util.Map;
 
 import org.openengsb.core.api.Connector;
 import org.openengsb.core.common.AbstractConnectorInstanceFactory;
 import org.openengsb.domain.dependency.DependencyDomainEvents;
 
 public class MavendepServiceInstanceFactory extends AbstractConnectorInstanceFactory<MavendepServiceImpl> {
 
     private DependencyDomainEvents dependencyEvents;
 
     @Override
     public Connector createNewInstance(String id) {
         MavendepServiceImpl service = new MavendepServiceImpl(id);
         service.setDependencyEvents(dependencyEvents);
         return service;
     }
 
     public void setDependencyEvents(DependencyDomainEvents events) {
         this.dependencyEvents = events;
     }
 
     @Override
     public void doApplyAttributes(MavendepServiceImpl instance,
             Map<String, String> attributes) {
         if (attributes.containsKey("pomfile")) {
             instance.setPomFile(attributes.get("pomfile"));
         }
        if (attributes.containsKey("attribute")) {
            instance.setAttribute(attributes.get("attribute"));
         }
     }
 }
