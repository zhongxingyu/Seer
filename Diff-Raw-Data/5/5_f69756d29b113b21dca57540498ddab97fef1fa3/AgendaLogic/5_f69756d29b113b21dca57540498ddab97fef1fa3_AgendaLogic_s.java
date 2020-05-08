 package org.es.butler.logic.impl;
 
 import android.content.Context;
 import android.text.format.DateFormat;
 
 import org.es.api.pojo.AgendaEvent;
 import org.es.butler.R;
 import org.es.butler.logic.PronunciationLogic;
 
 import java.util.List;
 
 /**
  * Created by Cyril Leroux on 24/06/13.
  */
 public class AgendaLogic implements PronunciationLogic {
 
     List<AgendaEvent> mEvents;
     boolean mToday;
 
     public AgendaLogic(List<AgendaEvent> events, boolean today) {
         mEvents = events;
         mToday = today;
     }
 
     @Override
     public String getPronunciation(Context context) {
         return getPronunciationEn(mEvents, context);
     }
 
     private String getPronunciationEn(final List<AgendaEvent> events, Context context) {
         int count = (events == null || events.isEmpty()) ? 0 : events.size();
 
         StringBuilder sb = new StringBuilder();
         if (mToday) {
            sb.append(context.getResources().getQuantityString(R.plurals.today_appointments, count, count));
         } else {
            sb.append(context.getResources().getQuantityString(R.plurals.upcoming_appointments, count, count));
         }
 
         if (count == 0) {
             return sb.toString();
         }
 
         sb.append(" ");
 
         for (AgendaEvent event : events) {
 
             // Day of week (if not today)
             if (!mToday) {
                 sb.append(getWeekDay(event.getStartDateMillis()));
                 sb.append(" ");
             }
 
             // Event title
             sb.append(event.getTitle());
 
             // Event start on (if not all day)
             if (!event.isAllDay()) {
                 TimeLogic logic = new TimeLogic(event.getStartDateMillis(), false);
                 sb.append(" at ");
                 sb.append(logic.getPronunciation(context));
                 sb.append(" ");
             }
         }
          //+ " Your Jujitsu course is at 8 30 pm.";
         //return context.getString(R.plurals.appointment_count, count) + " Your Jujitsu course is at 8 30 pm.";
         //return "You have no appointment today.";
         return sb.toString();
     }
 
     private String getWeekDay(long timeMillis) {
         return DateFormat.format("EEEE", timeMillis).toString();
     }
 }
