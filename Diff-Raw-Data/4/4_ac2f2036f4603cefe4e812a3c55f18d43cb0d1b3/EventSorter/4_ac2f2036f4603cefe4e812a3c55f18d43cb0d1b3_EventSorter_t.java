 package hsa.awp.event.util;
 
 import hsa.awp.event.model.Category;
 import hsa.awp.event.model.Event;
 
 import java.util.Comparator;
 
 public class EventSorter {
 
   public static Comparator<Category> alphabeticalCategoryName() {
     return new Comparator<Category>() {
       @Override
       public int compare(Category category1, Category category2) {
         return category1.getName().compareTo(category2.getName());
       }
     };
   }
 
   public static Comparator<Event> alphabeticalEventName() {
     return new Comparator<Event>() {
       @Override
       public int compare(Event event1, Event event2) {
 
         String subjectName1 = event1.getSubject().getName();
         String subjectName2 = event2.getSubject().getName();
 
         int subjectNamePosition = subjectName1.compareTo(subjectName2);
         if (subjectNamePosition == 0) {

          Integer eventId1 = event1.getEventId();
          return eventId1.compareTo(event2.getEventId());
         }
         return subjectNamePosition;
       }
     };
   }
 
 }
