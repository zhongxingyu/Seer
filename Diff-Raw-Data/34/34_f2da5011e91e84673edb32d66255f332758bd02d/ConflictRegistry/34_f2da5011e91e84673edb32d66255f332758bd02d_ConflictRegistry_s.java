 package org.drools.planner.examples.ras2012.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.drools.planner.examples.ras2012.model.Train;
 import org.drools.planner.examples.ras2012.util.model.OccupationTracker;
 
 public class ConflictRegistry {
 
     private static class ConflictRegistryItem {
 
         private final Map<Train, OccupationTracker> occupiedArcsByTrain = new HashMap<Train, OccupationTracker>();
         private final List<OccupationTracker>       occupiedArcs        = new ArrayList<OccupationTracker>();
 
         public double getConflicts() {
             double conflicts = 0;
             final int size = this.occupiedArcs.size();
             for (int position = 0; position < size; position++) {
                 final OccupationTracker left = this.occupiedArcs.get(position);
                 for (int i = position + 1; i < size; i++) {
                     final OccupationTracker right = this.occupiedArcs.get(i);
                     conflicts += left.getConflictingMileage(right).doubleValue();
                 }
             }
             return conflicts;
         }
 
         public void resetOccupiedArcs(final Train t) {
             final OccupationTracker toRemove = this.occupiedArcsByTrain.get(t);
             if (toRemove != null) {
                 this.occupiedArcsByTrain.remove(t);
                 this.occupiedArcs.remove(toRemove);
             }
         }
 
         public void setOccupiedArcs(final Train t, final OccupationTracker arcs) {
             this.occupiedArcsByTrain.put(t, arcs);
             this.occupiedArcs.add(arcs);
         }
 
     }
 
     private final Map<Long, ConflictRegistryItem>  itemsByTime;
     private final Collection<ConflictRegistryItem> items;
 
     public ConflictRegistry(final int numberOfItems) {
         this.itemsByTime = new HashMap<Long, ConflictRegistryItem>(numberOfItems);
         this.items = new ArrayList<ConflictRegistryItem>(numberOfItems);
     }
 
     public int countConflicts() {
         double conflicts = 0;
         for (final ConflictRegistryItem item : this.items) {
             conflicts += item.getConflicts();
         }
         return (int) Math.round(conflicts);
     }
 
     public void resetOccupiedArcs(final Train t) {
         for (final ConflictRegistryItem item : this.items) {
             item.resetOccupiedArcs(t);
         }
     }
 
     public void setOccupiedArcs(final long time, final Train t, final OccupationTracker occupiedArcs) {
        if (!this.itemsByTime.containsKey(time)) {
            final ConflictRegistryItem item = new ConflictRegistryItem();
            this.itemsByTime.put(time, item);
            this.items.add(item);
            item.setOccupiedArcs(t, occupiedArcs);
         } else {
            this.itemsByTime.get(time).setOccupiedArcs(t, occupiedArcs);
         }
     }
 }
