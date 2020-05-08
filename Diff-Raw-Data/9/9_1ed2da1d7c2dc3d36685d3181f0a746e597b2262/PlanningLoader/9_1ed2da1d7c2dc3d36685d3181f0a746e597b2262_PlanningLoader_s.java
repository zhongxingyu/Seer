 import com.google.common.base.Strings;
 import com.google.gson.Gson;
 import com.google.inject.Singleton;
 
 import java.util.List;
 
 @Singleton
 public class PlanningLoader {
   public void createTalks(Planning planning, String json) {
     if (Strings.isNullOrEmpty(json)) {
       return;
     }
 
     Sessions sessions = new Gson().fromJson(json, Sessions.class);
 
     for (Day day : sessions.days) {
       for (Slot slot : day.slots) {
         for (Talk talk : slot.talks) {
          planning.createTalk(talk.id, day.day + "-" + slot.slot);
         }
       }
     }
   }
 
   static class Sessions {
     List<Day> days;
   }
 
   static class Day {
     String day;
     List<Slot> slots;
   }
 
   static class Slot {
     String slot;
     List<Talk> talks;
   }
 
   static class Talk {
     String id;
   }
 }
