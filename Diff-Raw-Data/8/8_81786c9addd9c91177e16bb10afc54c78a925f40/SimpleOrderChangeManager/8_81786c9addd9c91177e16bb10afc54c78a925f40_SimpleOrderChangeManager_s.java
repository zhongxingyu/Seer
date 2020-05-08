 package com.exadel.borsch.managers.simple;
 
 import com.exadel.borsch.dao.OrderChange;
 import com.exadel.borsch.managers.OrderChangeManager;
 import com.exadel.borsch.util.DateTimeUtils;
 import org.joda.time.DateTime;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import java.util.*;
 
 /**
  * @author Andrew Zhilka
  */
 @Service
 @Scope(value = "singleton")
 public class SimpleOrderChangeManager implements OrderChangeManager {
     private List<OrderChange> changes = new ArrayList<>();
 
     @Override
     public List<OrderChange> getChangesForCurrentWeek() {
         List<OrderChange> latestChanges = new ArrayList<>();
         DateTime startOfWeek = DateTimeUtils.getStartOfCurrentWeek();
 
         for (OrderChange change : changes) {
             if (!change.getDateOfChange().isBefore(startOfWeek)) {
                 latestChanges.add(change);
             }
         }
 
         return Collections.unmodifiableList(latestChanges);
     }
 
     @Override
     public List<OrderChange> getActualChanges() {
         List<OrderChange> latestChanges = new ArrayList<>();
        DateTime startOfWeek = DateTimeUtils.getStartOfCurrentWeek();
 
         for (OrderChange change : changes) {
             if (!DateTimeUtils.isDateBefore(change.getDateOfChange(), DateTime.now())) {
                 latestChanges.add(change);
             }
         }
 
         return Collections.unmodifiableList(latestChanges);
     }
 
     @Override
     public void addNewChange(OrderChange newChange) {
         changes.add(newChange);
     }
 
     @Override
     public void deleteChangeById(UUID id) {
         ListIterator<OrderChange> it = changes.listIterator();
         while (it.hasNext()) {
             OrderChange curChange = it.next();
             if (curChange.getId().equals(id)) {
                 it.remove();
                 return;
             }
         }
     }
 
     @Override
     public void updateChange(OrderChange newChange) {
         ListIterator<OrderChange> it = changes.listIterator();
         while (it.hasNext()) {
             OrderChange curChange = it.next();
             if (curChange.getId().equals(newChange.getId())) {
                 it.set(newChange);
                 return;
             }
         }
     }
 
     @Override
     public void resetChanges() {
         changes.clear();
     }
 
     @Override
     public void resetOldChanges() {
         changes.retainAll(this.getChangesForCurrentWeek());
     }
 }
