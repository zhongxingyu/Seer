 package nl.sense_os.commonsense.client.groups.invite;
 
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.constants.Constants;
 import nl.sense_os.commonsense.client.common.constants.Urls;
 import nl.sense_os.commonsense.client.common.models.GroupModel;
 
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestBuilder.Method;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.UrlBuilder;
 
 public class GroupInviteController extends Controller {
 
     private static final Logger LOG = Logger.getLogger(GroupInviteController.class.getName());
 
     public GroupInviteController() {
         registerEventTypes(GroupInviteEvents.ShowInviter, GroupInviteEvents.InviteRequested);
     }
 
     @Override
     public void handleEvent(AppEvent event) {
         final EventType type = event.getType();
 
         if (type.equals(GroupInviteEvents.InviteRequested)) {
            LOG.finest("InviteRequested");
             final GroupModel group = event.getData("group");
             final String email = event.getData("username");
             View source = (View) event.getSource();
             inviteUser(group, email, source);
 
         } else if (type.equals(GroupInviteEvents.ShowInviter)) {
            LOG.finest("ShowInviter");
             GroupInviteView inviter = new GroupInviteView(this);
             forwardToView(inviter, event);
 
         } else {
             LOG.warning("Unexpected event: " + event);
         }
     }
 
     private void onInviteSuccess(View source) {
         forwardToView(source, new AppEvent(GroupInviteEvents.InviteComplete));
         Dispatcher.forwardEvent(GroupInviteEvents.InviteComplete);
     }
 
     private void onInviteFailure(View source) {
         forwardToView(source, new AppEvent(GroupInviteEvents.InviteFailed));
     }
 
     private void inviteUser(GroupModel group, String username, final View source) {
 
         // prepare request properties
         final Method method = RequestBuilder.POST;
         final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
         urlBuilder.setPath(Urls.PATH_GROUPS + "/" + group.getId() + "/users.json");
         final String url = urlBuilder.buildString();
         final String sessionId = Registry.<String> get(Constants.REG_SESSION_ID);
 
         // prepare request body
         String body = "{\"user\":{\"username\":\"" + username + "\"}}";
 
         // prepare request callback
         RequestCallback reqCallback = new RequestCallback() {
 
             @Override
             public void onError(Request request, Throwable exception) {
                 LOG.warning("POST group user onError callback: " + exception.getMessage());
                 onInviteFailure(source);
             }
 
             @Override
             public void onResponseReceived(Request request, Response response) {
                 LOG.finest("POST group user response received: " + response.getStatusText());
                 int statusCode = response.getStatusCode();
                 if (Response.SC_CREATED == statusCode) {
                     onInviteSuccess(source);
                 } else {
                     LOG.warning("POST group user returned incorrect status: " + statusCode);
                     onInviteFailure(source);
                 }
             }
         };
 
         // send request
         RequestBuilder builder = new RequestBuilder(method, url);
         builder.setHeader("X-SESSION_ID", sessionId);
         builder.setHeader("Content-Type", Urls.HEADER_JSON_TYPE);
         try {
             builder.sendRequest(body, reqCallback);
         } catch (RequestException e) {
             LOG.warning("POST group user request threw exception: " + e.getMessage());
             onInviteFailure(source);
         }
     }
 
 }
