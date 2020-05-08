 /**
  * 
  */
 package org.iplantc.de.client.notifications.views;
 
 import java.util.List;
 
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.events.AnalysisPayloadEvent;
 import org.iplantc.de.client.events.AnalysisPayloadEventHandler;
 import org.iplantc.de.client.events.AnalysisUpdateEvent;
 import org.iplantc.de.client.events.DataPayloadEvent;
 import org.iplantc.de.client.events.DataPayloadEventHandler;
 import org.iplantc.de.client.events.NotificationCountUpdateEvent;
 import org.iplantc.de.client.notifications.models.NotificationAutoBeanFactory;
 import org.iplantc.de.client.notifications.models.NotificationMessage;
 import org.iplantc.de.client.notifications.models.NotificationPayload;
 import org.iplantc.de.client.utils.NotificationHelper;
 import org.iplantc.de.client.utils.NotificationHelper.Category;
 import org.iplantc.de.client.utils.NotifyInfo;
 import org.iplantc.de.client.utils.builders.context.AnalysisContextBuilder;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.sencha.gxt.cell.core.client.SimpleSafeHtmlCell;
 import com.sencha.gxt.core.client.IdentityValueProvider;
 import com.sencha.gxt.core.client.XTemplates;
 import com.sencha.gxt.core.client.resources.CommonStyles;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.widget.core.client.ListView;
 import com.sencha.gxt.widget.core.client.ListViewCustomAppearance;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
 
 /**
  * New notifications as list
  * 
  * @author sriram
  * 
  */
 public class NotificationListView implements IsWidget {
 
     private ListView<NotificationMessage, NotificationMessage> view;
     private ListStore<NotificationMessage> store;
     private int totalNotificationCount;
 
     private final NotificationAutoBeanFactory factory = GWT.create(NotificationAutoBeanFactory.class);
 
     final Resources resources = GWT.create(Resources.class);
     final Style style = resources.css();
     final Renderer r = GWT.create(Renderer.class);
 
     interface Renderer extends XTemplates {
         @XTemplate("<div class=\"{style.thumb}\"> {msg.message}</div>")
         public SafeHtml renderItem(NotificationMessage msg, Style style);
     }
 
     interface Style extends CssResource {
         String over();
 
         String select();
 
         String thumb();
 
         String thumbWrap();
     }
 
     interface Resources extends ClientBundle {
         @Source("NotificationListView.css")
         Style css();
     }
 
     ModelKeyProvider<NotificationMessage> kp = new ModelKeyProvider<NotificationMessage>() {
         @Override
         public String getKey(NotificationMessage item) {
             return item.getTimestamp() + "";
         }
     };
 
     ListViewCustomAppearance<NotificationMessage> appearance = new ListViewCustomAppearance<NotificationMessage>(
             "." + style.thumbWrap(), style.over(), style.select()) {
 
         @Override
         public void renderEnd(SafeHtmlBuilder builder) {
             String markup = new StringBuilder("<div class=\"").append(CommonStyles.get().clear())
                     .append("\"></div>").toString();
             builder.appendHtmlConstant(markup);
         }
 
         @Override
         public void renderItem(SafeHtmlBuilder builder, SafeHtml content) {
             builder.appendHtmlConstant("<div class='" + style.thumbWrap()
                     + "' style='border: 1px solid white'>");
             builder.append(content);
             builder.appendHtmlConstant("</div>");
         }
 
     };
 
     public NotificationListView() {
         registerEventHandlers();
         resources.css().ensureInjected();
     }
 
     public void highlightNewNotifications() {
         List<NotificationMessage> new_notifications = store.getAll();
         // TODO: implement higlight
     }
 
     //
     public void markAsSeen() {
         java.util.List<NotificationMessage> new_notifications = store.getAll();
         JSONArray arr = new JSONArray();
         int i = 0;
         if (new_notifications.size() > 0) {
             for (NotificationMessage n : new_notifications) {
                 arr.set(i++, new JSONString(n.getId().toString()));
                 // set seen here
             }
 
             JSONObject obj = new JSONObject();
             obj.put("uuids", arr);
 
             org.iplantc.de.client.notifications.services.MessageServiceFacade facade = new org.iplantc.de.client.notifications.services.MessageServiceFacade();
             facade.markAsSeen(obj, new AsyncCallback<String>() {
 
                 @Override
                 public void onSuccess(String result) {
                     // Do nothing intentionally
                 }
 
                 @Override
                 public void onFailure(Throwable caught) {
                     org.iplantc.core.uicommons.client.ErrorHandler.post(caught);
                 }
             });
         }
 
     }
 
     private void registerEventHandlers() {
         final EventBus eventbus = EventBus.getInstance();
 
         // handle data events
         eventbus.addHandler(DataPayloadEvent.TYPE, new DataPayloadEventHandler() {
             @Override
             public void onFire(DataPayloadEvent event) {
                 // TODO:Add data context
                 addFromEventHandler(Category.DATA, I18N.DISPLAY.fileUpload(), event.getMessage(), null,
                         null);
                 highlightNewNotifications();
             }
         });
 
         // handle analysis events
         eventbus.addHandler(AnalysisPayloadEvent.TYPE, new AnalysisPayloadEventHandler() {
             @Override
             public void onFire(AnalysisPayloadEvent event) {
                 JSONObject payload = event.getPayload();
                 AutoBean<NotificationPayload> bean = AutoBeanCodex.decode(factory,
                         NotificationPayload.class, payload.toString());
                 AnalysisContextBuilder builder = new AnalysisContextBuilder();
                 addFromEventHandler(Category.ANALYSIS, I18N.CONSTANT.analysis(), event.getMessage(),
                         builder.build(bean.as()), payload);
                 highlightNewNotifications();
             }
         });
     }
 
     private void addFromEventHandler(final Category category, final String header,
             final JSONObject objMessage, final String context, JSONObject payload) {
 
         AutoBean<NotificationMessage> bean = AutoBeanCodex.decode(factory, NotificationMessage.class,
                 objMessage.toString());
 
         NotificationMessage nm = bean.as();
         nm.setCategory(category);
         nm.setContext(context);
 
         NotificationMessage existing = view.getStore().findModelWithKey(nm.getTimestamp() + "");
         if (existing == null) {
             view.getStore().add(nm);
             totalNotificationCount = totalNotificationCount + 1;
             fireEvents(category, payload);
             NotifyInfo.display(header, nm.getMessage());

         }

     }
 
     /**
      * get total notification count
      * 
      * @return
      */
     public int getTotalNotificationCount() {
         return totalNotificationCount;
     }
 
     /**
      * reset all notification count
      * 
      */
     public void resetCount() {
         totalNotificationCount = 0;
     }
 
     private void fireEvents(final Category category, JSONObject payload) {
         NotificationCountUpdateEvent ncue = new NotificationCountUpdateEvent(getTotalNotificationCount());
         EventBus instance = EventBus.getInstance();
         instance.fireEvent(ncue);
         if (category.equals(NotificationHelper.Category.ANALYSIS)) {
             AnalysisUpdateEvent aue = new AnalysisUpdateEvent(payload);
             instance.fireEvent(aue);
         }
     }
 
     @Override
     public Widget asWidget() {
         store = new ListStore<NotificationMessage>(kp);
         view = new ListView<NotificationMessage, NotificationMessage>(store,
                 new IdentityValueProvider<NotificationMessage>(), appearance);
 
         view.getSelectionModel().addSelectionChangedHandler(
                 new SelectionChangedHandler<NotificationMessage>() {
 
                     @Override
                     public void onSelectionChanged(SelectionChangedEvent<NotificationMessage> event) {
                         final NotificationMessage msg = event.getSelection().get(0);
                         if (msg != null) {
                             NotificationHelper.getInstance().view(msg);
                         }
                     }
 
                 });
 
         view.setCell(new SimpleSafeHtmlCell<NotificationMessage>(
                 new AbstractSafeHtmlRenderer<NotificationMessage>() {
 
                     @Override
                     public SafeHtml render(NotificationMessage object) {
                         return r.renderItem(object, style);
                     }
                 }));
 
         view.setSize("250px", "200px");
         return view;
     }
 
 }
