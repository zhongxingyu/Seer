 package pl.psnc.dl.wf4ever.portal.pages.notifications;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxEventBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.PropertyListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.purl.wf4ever.rosrs.client.notifications.Notification;
 
 import pl.psnc.dl.wf4ever.portal.listeners.IAjaxLinkListener;
 
 final class NotificationsList extends Panel {
 
     /** id. */
     private static final long serialVersionUID = -2527527943968289889L;
     private transient DateTimeFormatter hourFormatter = new DateTimeFormatterBuilder().appendHourOfDay(2)
             .appendLiteral(":").appendMinuteOfHour(2).toFormatter();
     private transient DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendDayOfMonth(1)
             .appendLiteral("/").appendMonthOfYear(1).appendLiteral("/").appendYear(2, 2).toFormatter();
 
     private PropertyListView<Notification> list;
 
     private IModel<Notification> selectedNotificationModel;
 
     private List<IAjaxLinkListener> listeners = new ArrayList<>();
 
 
     public NotificationsList(String id, List<? extends Notification> notifications,
             IModel<Notification> selectedNotificationModel) {
         super(id);
         this.selectedNotificationModel = selectedNotificationModel;
         list = new NotificationsPropertyListView("list", notifications);
         list.setReuseItems(true);
         add(list);
         setOutputMarkupId(true);
     }
 
 
     public List<IAjaxLinkListener> getListeners() {
         return listeners;
     }
 
 
     private final class NotificationsPropertyListView extends PropertyListView<Notification> {
 
         /** id. */
         private static final long serialVersionUID = 3207517289844703505L;
 
 
         private NotificationsPropertyListView(String id, List<? extends Notification> list) {
             super(id, list);
         }
 
 
         @Override
         protected void populateItem(final ListItem<Notification> item) {
             item.add(new Label("title"));
             item.add(new Label("published", formatDateTime(item.getModelObject().getPublished())));
            item.add(new Label("source"));
             item.add(new AjaxEventBehavior("onclick") {
 
                 /** id. */
                 private static final long serialVersionUID = -7851716059681985652L;
 
 
                 @Override
                 protected void onEvent(AjaxRequestTarget target) {
                     selectedNotificationModel.setObject(item.getModelObject());
                     target.add(NotificationsList.this);
                     for (IAjaxLinkListener listener : listeners) {
                         listener.onAjaxLinkClicked(item, target);
                     }
                 }
             });
             item.add(new Behavior() {
 
                 /** id. */
                 private static final long serialVersionUID = -5842150372531742905L;
 
 
                 @SuppressWarnings("unchecked")
                 @Override
                 public void onComponentTag(Component component, ComponentTag tag) {
                     super.onComponentTag(component, tag);
                     if (((ListItem<Notification>) component).getModelObject().equals(
                         selectedNotificationModel.getObject())) {
                         //                        System.out.println("Selected item: " + selectedItem);
                         tag.append("class", "selected", " ");
                     }
                 }
             });
         }
     }
 
 
     private String formatDateTime(DateTime dateTime) {
         if ((new LocalDate(dateTime)).equals(new LocalDate())) {
             return hourFormatter.print(dateTime);
         } else {
             return dateFormatter.print(dateTime);
         }
     }
 
 }
