 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package midgard.pubsubhubbub;
 
 import midgard.pubsubhubbub.events.PubSubHubBubNotificationEvent;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Vector;
 import midgard.events.IEvent;
 import midgard.network.Utils;
 import midgard.pubsubhubbub.events.PublicationEvent;
 import midgard.pubsubhubbub.events.PublicationSensorEvent;
 import midgard.pubsubhubbub.events.PublicationSensorEventData;
 import midgard.services.Service;
 import midgard.web.IHTTPServer;
 import midgard.web.IWebServer;
 import midgard.web.Request;
 import midgard.web.Response;
 import midgard.web.http.HttpConnector;
 
 /**
  *
  * @author fenrrir
  */
 public class DefaultHub extends Service implements IHub {
 
     private Vector uris;
     private Hashtable listenersByTopic;
     private IPublisher publisher;
     private IWebServer webserver;
     private String myAddress;
 
     public String[] getRequiredInterfaces() {
         return new String[]{
             IPublisher.class.getName(),
             IWebServer.class.getName()
         };
     }
 
     public void initialize() {
         super.initialize();
         listenersByTopic = new Hashtable();
         uris = new Vector();
         uris.addElement("/notify");
         uris.addElement("/subscribe");
         publisher = (IPublisher) getConnectedComponents()
                 .get(IPublisher.class.getName());
         webserver = (IWebServer) getConnectedComponents()
                 .get(IWebServer.class.getName());
 
         publisher.registerEventListener(this);
 
         webserver.addWebApplication(this);
 
         myAddress = Utils.getAddress();
     }
 
     public void destroy() {
         super.destroy();
         uris.removeAllElements();
         listenersByTopic.clear();
         listenersByTopic = null;
         uris = null;
         publisher.removeEventListener(this);
         publisher = null;
         webserver.removeWebApplication(this);
         webserver = null;
     }
 
     public Vector getURIs() {
         return uris;
     }
 
     private Response handleSubscribe(Request request) {
         Vector listeners;
         String topic = request.parms.getProperty("topic");
         String address = request.parms.getProperty("address");
  
        if (!listenersByTopic.contains(topic)) {
             listeners = new Vector();
             listeners.addElement(address);
             listenersByTopic.put(topic, listeners);
         } else {
             listeners = (Vector) listenersByTopic.get(topic);
             listeners.addElement(address);
 
         }
         return getResponse("{ \"result\" : true }");
     }
 
     private Response handleNotify(Request request) {
         fireEvent(new PubSubHubBubNotificationEvent(request));
         return getResponse("{ \"result\" : true }");
     }
 
     private Response getResponse(String body) {
         return new Response(IHTTPServer.HTTP_OK, IHTTPServer.MIME_APPLICATION_JSON,
                 new ByteArrayInputStream(body.getBytes()), body.length());
     }
 
     public Response serve(Request request) throws Exception {
 
         if (request.uri.equals("/notify")) {
             return handleNotify(request);
         }
 
         if (request.uri.equals("/subscribe")) {
             return handleSubscribe(request);
         }
 
         return null;
 
     }
 
     public void newEventArrived(IEvent event) {
 
 
         if (event instanceof PublicationEvent) {
             handlePublicationEvent(event);
         }
         if (event instanceof PublicationSensorEvent) {
             handleSensorEvent(event);
         }
 
 
     }
 
     private void handlePublicationEvent(IEvent event) {
         Response response = (Response) event.getContentObject();
 
         String topic = response.uri;
 
         byte[] buffer = new byte[response.contentLength];
         try {
             response.data.read(buffer, 0, response.contentLength);
         } catch (IOException ex) {
             ex.printStackTrace();
         }
 
         String appData = new String(buffer);
 
         Hashtable result = new Hashtable();
         result.put("topic", topic);
         result.put("result", appData);
         result.put("address", myAddress);
         sendNotify(topic, result);
 
 
 
     }
 
     private void sendNotify(String topic, Hashtable parameters) {
 
         String address;
         Vector listeners = (Vector) listenersByTopic.get(topic);
 
         if (listeners != null) {
             HttpConnector connector = new HttpConnector();
 
             for (int i = 0; i < listeners.size(); i++) {
                 address = (String) listeners.elementAt(i);
 
                 try {
                     connector.connect(address);
                     connector.post("/notify", parameters);
                     connector.closeConnection();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
 
             }
         }
 
     }
 
     private void handleSensorEvent(IEvent event) {
         Hashtable result = new Hashtable();
         PublicationSensorEventData data = (PublicationSensorEventData) event.getContentObject();
         result.put("value", data.getData());
         result.put("topic", data.getUri());
         result.put("address", myAddress);
         sendNotify(data.getUri(), result);
     }
 }
