 /*
 Copyright 2012 Portland Transport
 
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
 
 package org.transitappliance.loader;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.io.File;
 import java.io.IOException;
 import org.onebusaway.gtfs.serialization.GtfsReader;
 import org.onebusaway.csv_entities.EntityHandler;
 import org.onebusaway.gtfs.model.Stop;
 import org.onebusaway.gtfs.model.Route;
 import org.onebusaway.gtfs.model.StopTime;
 import org.onebusaway.gtfs.model.Agency;
 import org.onebusaway.gtfs.model.Trip;
 import org.onebusaway.gtfs.model.ShapePoint;
 import org.onebusaway.gtfs.model.ServiceCalendar;
 import org.onebusaway.gtfs.model.ServiceCalendarDate;
 import org.onebusaway.gtfs.model.Transfer;
 import org.onebusaway.gtfs.model.AgencyAndId;
 import org.onebusaway.gtfs.impl.GtfsDaoImpl;
 
 /**
  * This does not store StopTimes but does store an ArrayList mapping stops to routes.
  */
 public class NoStopTimeDaoImpl extends GtfsDaoImpl {
     // All the stops, routes and stop<->route mappings are saved up front, then the routes are 
     // merged into the stops
     private HashMap<AgencyAndId, TAStop> stops = new HashMap<AgencyAndId, TAStop>();
     // route ids, routes
     private HashMap<AgencyAndId, TARoute> routes = new HashMap<AgencyAndId, TARoute>();
     // stop IDs, route IDs
     private HashMap<AgencyAndId, ArrayList<AgencyAndId>> stopRouteMapping = new HashMap<AgencyAndId, ArrayList<AgencyAndId>>();
 
     int stopCount = 0;
     int routeCount = 0;
     int stopTimeCount = 0;
 
     /**
      * Do the same thing for all entities except the StopTimes; map them and discard
      */
     public void saveEntity(Object entity) {
         // parse everything out
         if (entity instanceof Stop) {
             stopCount++;
             if (stopCount % 1000 == 0)
                 System.out.println("Stops: " + stopCount);
 
             Stop stop = (Stop) entity;
                 
             TAStop dbStop = new TAStop();
 
             AgencyAndId stopId = stop.getId();
                 
             dbStop.agency = stopId.getAgencyId();
             dbStop.stop_id = stopId.getId();
             dbStop.id = dbStop.agency + ":" + dbStop.stop_id;
             dbStop.stop_lat = stop.getLat();
             dbStop.stop_lon = stop.getLon();
             dbStop.stop_desc = stop.getDesc();
             dbStop.stop_name = stop.getName();
             dbStop.stop_code = stop.getCode();
             // TODO: extra attributes in attributes HashMap
 
             stops.put(stopId, dbStop);
         }
 
         else if (entity instanceof Route) {
             routeCount++;
             if (routeCount % 10 == 0)
                 System.out.println("Routes: " + routeCount);
 
             Route route = (Route) entity;
                 
             TARoute dbRoute = new TARoute();
                 
             AgencyAndId aid = route.getId();
 
             dbRoute.route_id = aid.getId();
             dbRoute.route_url = route.getUrl();
             dbRoute.route_long_name = route.getLongName();
             dbRoute.route_short_name = route.getShortName();
             dbRoute.route_type = route.getType();
                 
             routes.put(aid, dbRoute);
         }
 
         else if (entity instanceof StopTime) {
             stopTimeCount++;
             if (stopTimeCount % 250000 == 0) {
                 System.out.println("Stop times: " + stopTimeCount);
             }
 
             StopTime st = (StopTime) entity;
                 
             // parse it down to a mapping
             AgencyAndId stopId = st.getStop().getId();
             AgencyAndId routeId = st.getTrip().getRoute().getId();
 
             if (!stopRouteMapping.containsKey(stopId)) {
                 stopRouteMapping.put(stopId, new ArrayList<AgencyAndId>());
             }
                 
             ArrayList routesForStop = stopRouteMapping.get(stopId);
             if (!routesForStop.contains(routeId)) {
                 routesForStop.add(routeId);
                 stopRouteMapping.put(stopId, routesForStop);
             }
         }
         // TODO: agencies
         // end linear if
 
         // This saves everything except stop times the normal way as well so that references are preserved
         if (!(entity instanceof StopTime || entity instanceof ShapePoint || entity instanceof ServiceCalendar 
               || entity instanceof ServiceCalendarDate || entity instanceof Transfer)) {
             super.saveEntity(entity);
         }
     }
 
     /**
      * Returns all of the loaded TAStops
      */
     public TAStop[] getStops () {
         // now, process the relations
       
         // We can't remove the stops while iterating over them, so save the ones to remove and do it later
         ArrayList<AgencyAndId> stopsToRemove = new ArrayList<AgencyAndId>();
 
         for (AgencyAndId stopId : stops.keySet()) {
             TAStop stop = stops.get(stopId);
          
             ArrayList<AgencyAndId> routesForStop = stopRouteMapping.get(stopId);
          
             if (routesForStop == null) {
                 System.out.println("Warning: stop " + stopId + " has no routes; removing");
                 stopsToRemove.add(stopId);
                 continue;
             }
          
             // there are no duplicates to worry about; that is checked above
             for (AgencyAndId routeId : routesForStop) {
                 stop.routes.add(routes.get(routeId));
             }
          
             // add it to the output list and remove it from the input to save ram
             stops.put(stopId, stop);
          
             //System.out.println("Stop " + stopId + " has " + stop.routes.size() + " routes");
         }
 
         // remove stops marked for removal
         for (AgencyAndId stopId : stopsToRemove) {
             stops.remove(stopId);
         }
 
         System.out.println("GTFS read; stops " + stops.size() + ", routes " + routes.size()); 
 
        return (TAStop[]) stops.values().toArray();
     }
 }
