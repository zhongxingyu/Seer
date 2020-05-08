 package org.iplantc.de.client.desktop.presenter;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.de.client.events.NotificationCountUpdateEvent;
 import org.iplantc.de.client.notifications.services.MessageServiceFacade;
 
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * This task requests the number of unseen notifications for the user.
  */
 final class CountUnseenNotifications implements Runnable {
 
 	@Override
 	public void run() {
 		new MessageServiceFacade().getUnSeenMessageCount(new AsyncCallback<String>() {
             @Override
             public void onFailure(final Throwable caught) {
                 // currently we do nothing on failure
             }
             @Override
             public void onSuccess(final String result) {
                 JSONObject obj = JsonUtil.getObject(result);
                NotificationCountUpdateEvent event = new NotificationCountUpdateEvent(Integer
                        .parseInt(JsonUtil.getString(obj, "total")));
                 EventBus.getInstance().fireEvent(event);
             }
         });
 	}
 	
 }
