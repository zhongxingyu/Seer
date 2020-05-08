 package pl.psnc.dl.wf4ever.portal.pages.notifications;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.Model;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.purl.wf4ever.rosrs.client.notifications.Notification;
 
 public class NotificationPanel extends Panel {
 
     /** id. */
     private static final long serialVersionUID = 2297121898862626801L;
 
 
     public NotificationPanel(String id, CompoundPropertyModel<Notification> model) {
         super(id, model);
         add(new Label("title"));
         add(new Label("published", new PublishedDateModel()));
        add(new Label("source"));
         add(new Label("content").setEscapeModelStrings(false));
     }
 
 
     class PublishedDateModel extends Model<String> {
 
         /** id. */
         private static final long serialVersionUID = -8334879852621791873L;
 
         private transient DateTimeFormatter fullDateFormatter = buildDateTimeFormatter();
 
 
         protected DateTimeFormatter buildDateTimeFormatter() {
             return new DateTimeFormatterBuilder().appendMonthOfYearText().appendLiteral(' ').appendDayOfMonth(1)
                     .appendLiteral(", ").appendYear(4, 4).appendLiteral(" ").appendHourOfDay(2).appendLiteral(":")
                     .appendMinuteOfHour(2).toFormatter();
         }
 
 
         protected DateTimeFormatter getDateTimeFormatter() {
             if (fullDateFormatter == null) {
                 fullDateFormatter = buildDateTimeFormatter();
             }
             return fullDateFormatter;
         }
 
 
         @Override
         public String getObject() {
             Notification notification = (Notification) getDefaultModelObject();
             return notification != null ? getDateTimeFormatter().print(notification.getPublished()) : null;
         }
 
     }
 }
