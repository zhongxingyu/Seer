 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.resteasy.samples.people.api;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlID;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  */
@XmlRootElement
 public class Person {
 
   @XmlID @XmlElement public String id;
  @XmlElement public String name;
   @XmlElement public String password;
   @XmlElement public Map<String, Object> extraProperties;
   
   public Person() {
     
   }
   
   public Person(String id, Map<String, Object> properties) {
     this.id = id;
     this.name = (String) properties.get("name");
     this.extraProperties = properties;
   }
   
   public Map<String, Object> toContent() {
     Map<String, Object> properties = new HashMap<String, Object>(extraProperties);
     properties.put("id", id);
     properties.put("name", name);
     return properties;
   }
 }
