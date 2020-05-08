 package org.drools.planner.examples.ras2012.model;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Map;
 
 import org.drools.planner.api.domain.entity.PlanningEntity;
 import org.drools.planner.api.domain.variable.PlanningVariable;
 import org.drools.planner.api.domain.variable.ValueRange;
 import org.drools.planner.api.domain.variable.ValueRangeType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @PlanningEntity
 public final class ItineraryAssignment implements Cloneable {
 
     private final Train                         train;
 
     private Route                               route;
     private Itinerary                           itinerary;
     private final Collection<MaintenanceWindow> maintenances;
 
     private static final Logger                 logger = LoggerFactory
                                                                .getLogger(ItineraryAssignment.class);
 
     public ItineraryAssignment(final Train t) {
         this(t, Collections.<MaintenanceWindow> emptySet());
     }
 
     public ItineraryAssignment(final Train t, final Collection<MaintenanceWindow> maintenances) {
         if (t == null) {
             throw new IllegalArgumentException("Train may not be null!");
         }
         this.train = t;
         this.maintenances = maintenances;
     }
 
     @Override
     public ItineraryAssignment clone() {
         final ItineraryAssignment clone = new ItineraryAssignment(this.train, this.maintenances);
        clone.setRoute(this.route);
         for (final Map.Entry<Node, WaitTime> entry : this.getItinerary().getWaitTimes().entrySet()) {
            clone.getItinerary().setWaitTime(entry.getKey(), entry.getValue());
         }
         return clone;
     }
 
     public Itinerary getItinerary() {
         if (this.itinerary == null) {
             throw new IllegalStateException("No itinerary available, provide a route first.");
         }
         return this.itinerary;
     }
 
     @PlanningVariable
     @ValueRange(type = ValueRangeType.UNDEFINED)
     public synchronized Route getRoute() {
         return this.route;
     }
 
     public Train getTrain() {
         return this.train;
     }
 
     public synchronized void setRoute(final Route route) {
         if (route == null) {
             throw new IllegalArgumentException("Route may not be null.");
         }
         if (this.route != route) {
             this.route = route;
             ItineraryAssignment.logger.debug("Creating new itinerary for {}.",
                     new Object[] { this });
             this.itinerary = new Itinerary(this.route, this.train, this.maintenances);
         }
     }
 
     @Override
     public String toString() {
         if (this.route == null) {
             return "ItineraryAssignment [train=" + this.train.getName() + ", no route]";
         } else {
             return "ItineraryAssignment [train=" + this.train.getName() + ", route="
                     + this.route.getId() + "]";
         }
     }
 
 }
