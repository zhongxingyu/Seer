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
 package org.bedework.timezones.common;
 
import org.bedework.util.misc.ToString;
import org.bedework.util.timezones.model.TimezonesType;
 
 /** Allows us to cache expansions
  *
  * @author douglm
  */
 public class ExpandedMapEntry {
   private String etag;
   private TimezonesType tzs;
 
   /**
    * @param etag
    * @param tzs
    */
   public ExpandedMapEntry(final String etag,
                           final TimezonesType tzs) {
     this.etag = etag;
     this.tzs = tzs;
   }
 
   /**
    * @return etag
    */
   public String getEtag() {
     return etag;
   }
 
   /**
    * @return tzs
    */
   public TimezonesType getTzs() {
     return tzs;
   }
 
   @Override
   public String toString() {
     ToString ts = new ToString(this);
 
     ts.append("etag=", getEtag());
     ts.newLine();
     ts.append("tzs", tzs.toString());
 
     return ts.toString();
   }
 }
