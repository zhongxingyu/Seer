 package de.flower.rmt.ui.markup.html.form.renderer;
 
 import de.flower.rmt.model.db.entity.event.Event;
 import de.flower.rmt.model.db.entity.event.Match;
 import de.flower.rmt.model.db.type.EventType;
 import de.flower.rmt.util.Dates;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.model.ResourceModel;
 
 /**
  * @author flowerrrr
  */
 public class EventRenderer implements IChoiceRenderer<Event> {
 
     private boolean dateLong;
 
     public EventRenderer() {
         this(false);
     }
 
     public EventRenderer(final boolean dateLong) {
         this.dateLong = dateLong;
     }
 
     @Override
     public Object getDisplayValue(final Event event) {
         return getDateTeamTypeSummary(event, dateLong);
     }
 
     @Override
     public String getIdValue(final Event object, final int index) {
         if (object.getId() != null) {
             return "" + object.getId();
         } else {
             return "index-" + index;
         }
     }
 
     public static String getDateTeamTypeSummary(final Event event, boolean dateLong) {
         String team = event.getTeam().getName();
         String opponent = "";
         String eventType = new ResourceModel(EventType.from(event).getResourceKey()).getObject();
         if (EventType.from(event).isMatch()) {
             Match match = (Match) event;
            if (match.getOpponent() != null) {
                opponent = " : " + match.getOpponent().getName();
            }
         }
         String date;
         if (dateLong) {
             date = Dates.formatDateLongTimeShortWithWeekday(event.getDateTimeAsDate());
         } else {
             date = Dates.formatDateTimeShortWithWeekday(event.getDateTimeAsDate());
         }
         return date + " - " + eventType + " - " + team + opponent + " - " + event.getSummary();
     }
 }
