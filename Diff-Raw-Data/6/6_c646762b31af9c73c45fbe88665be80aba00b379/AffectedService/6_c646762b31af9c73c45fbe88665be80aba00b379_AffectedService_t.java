 /**
  * Copyright (C) 2012 Damian Sullivan <djcsdjcs@gmail.com>
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
 package com.transportation.cta.bustracker;
 
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.Root;
 
 /**
  * One or a combination of route, direction and stop
  * for which a service bulletin is valid.
  * @author dsullivan
  */
 @Root(name="srvc")
 public class AffectedService {
 
   /**
    * Alphanumeric designator of the route
   * (ex. "20" or "X20") for which this
    * service bulletin is in effect.
    */
   @Element(name="rt", required=false)
   private String route;
   public String getRoute() {
     return route;
   }
 
   /**
    * Direction of travel of the route for which
    * this service bulletin is in effect.
    */
   @Element(name="rtdir", required=false)
   private String routeDirection;
   public String routeDirection() {
     return routeDirection;
   }
 
   /**
    * ID of the stop for which this service bulletin is in effect.
    */
   @Element(name="stpid", required=false)
   private int stopId;
   public int getStopId() {
     return stopId;
   }
 
   /**
    * Name of the stop for which this service bulletin is in effect.
    */
   @Element(name="stpnm", required=false)
   private String stopName;
   public String getStopName() {
     return stopName;
   }
 
 
   @Override
   public String toString() {
     return String.format(
         "route: %s%nrouteDirection: %s%nstopId: %s%nstopName: %s",
         route, routeDirection, stopId, stopName);
   }
}
