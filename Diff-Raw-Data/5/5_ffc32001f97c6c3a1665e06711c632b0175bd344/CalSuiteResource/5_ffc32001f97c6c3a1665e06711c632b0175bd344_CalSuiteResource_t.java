 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.webcommon;
 
 import org.bedework.calfacade.BwResource;
 import org.bedework.calfacade.exc.CalFacadeException;
 import org.bedework.util.misc.Util;
 import org.bedework.util.servlet.ContentType;
 import org.bedework.util.servlet.ContentType.Param;
 
 import java.io.Serializable;
 import java.util.List;
 
 /** How we expose a calsuite resource
  *
 * @author Mike Douglass douglm - rpi.edu
  */
 public class CalSuiteResource implements Serializable {
   private BwResource resource;
 
   private String rclass;
 
   private String type;
 
   private List<Param> params;
   private String extra;
 
   /**
    * @param resource
    * @param rclass
    */
   public CalSuiteResource(final BwResource resource,
                           final String rclass) {
     this.resource = resource;
     this.rclass = rclass;
   }
 
   /**
    * @return gets the underlying resource's name
    */
   public String getName() {
     return this.resource.getName();
   }
 
   /**
    * @return resource
    */
   public BwResource getResource() {
     return resource;
   }
 
   /** Class of resource
    *
    * @return String calsuite resource type name
    */
  public String getRclass() {
     return rclass;
   }
 
   /** Type of resource
    *
    * @return String type
    */
   public String getType() {
     if (extra == null) {
       extra = resource.getContentTypeExtra();
 
       ContentType ct = ContentType.decode("x/x;" + extra);
 
       params = ct.getParams();
 
       for (Param p: params) {
         if ("type".equals(p.getName())) {
           type = p.getValue();
           break;
         }
       }
     }
 
     return type;
   }
 
   /**
    * @return path
    */
   public String getPath() {
     return Util.buildPath(false, resource.getColPath(), "/",
                           resource.getName());
   }
 
   /**
    * @return content type
    */
   public String getContentType() {
     return resource.getContentTypeStripped();
   }
 
   /**
    * @return content
    * @throws CalFacadeException
    */
   public String getContent() throws CalFacadeException {
     return resource.getContent().getStringContent();
   }
 }
