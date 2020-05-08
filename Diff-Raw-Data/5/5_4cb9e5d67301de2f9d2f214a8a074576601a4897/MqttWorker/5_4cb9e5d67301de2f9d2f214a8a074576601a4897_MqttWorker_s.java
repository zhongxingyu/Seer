 package se.chalmers.pd.headunit;
 
 import org.eclipse.paho.client.mqttv3.MqttCallback;
 import org.eclipse.paho.client.mqttv3.MqttClient;
 import org.eclipse.paho.client.mqttv3.MqttDefaultFilePersistence;
 import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
 import org.eclipse.paho.client.mqttv3.MqttException;
 import org.eclipse.paho.client.mqttv3.MqttMessage;
 import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
 import org.eclipse.paho.client.mqttv3.MqttSecurityException;
 import org.eclipse.paho.client.mqttv3.MqttTopic;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.os.Environment;
 import android.util.Log;
 
 /**
  * This thread launches an MQTT client and subscribes to the systems basic
  * topics. When a message is received, it broadcasts the message using custom
  * intent filters.
  */
 public class MqttWorker extends Thread {
 
     public static final String ACTION_DATA = "data";
     public static final String ACTION_INSTALL = "install";
     public static final String ACTION = "action";
     public static final String ACTION_EXIST = "exist";
     public static final String ACTION_SUCCESS = "success";
     public static final String ACTION_ERROR = "error";
     public static final String ACTION_TYPE = "type";
     public static final String ACTION_RESPONSE = "response";
     public static final String ACTION_START = "start";
     public static final String ACTION_UNINSTALL = "uninstall";
     public static final String ACTION_STOP = "stop";
     public static final String ACTION_PENDING = "pending";
     public static final String TOPIC_SYSTEM = "/system";
 
     private static final String STORAGE_DIRECTORY = "/infotainment/";
     private static final String WORKER_NAME = "MqttWorker";
     private static final String BROKER_URL = "tcp://192.168.43.147:1883";
     private static final String CLIENT_NAME = "headunit";
 
     private MqttClient mqttClient;
     private Callback callback;
     private String data;
 
     public interface Callback {
         public void onMessage(String topic, String payload);
 
         public void onConnected(boolean connected);
     }
 
     public MqttWorker(Callback callback, Context context) {
         this.callback = callback;
     }
 
     @Override
     public void run() {
         try {
             // Sets up the client and subscribes to topics
             String tmpDir = Environment.getExternalStorageDirectory() + STORAGE_DIRECTORY;
             MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
             mqttClient = new MqttClient(BROKER_URL, CLIENT_NAME, dataStore);
             mqttClient.setCallback(new CustomMqttCallback());
             if (!connect()) {
                 callback.onConnected(false);
                 return;
             }
         } catch (MqttException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Connects to the broker and automatically starts subscribing to the /system
      * topic. Also fires the callback to let the instantiator know that the
      * connection was successful.
      *
      * @return true if connected
      */
     private boolean connect() {
         boolean connected = false;
         if (mqttClient != null) {
             try {
                 mqttClient.connect();
                 mqttClient.subscribe(TOPIC_SYSTEM);
                 callback.onConnected(true);
                 connected = true;
                 Log.d(WORKER_NAME, "Connected and subscribing to system");
             } catch (MqttSecurityException e) {
                 Log.e(WORKER_NAME, "Could not connect to the broker " + e.getMessage());
             } catch (MqttException e) {
                 Log.e(WORKER_NAME, "Could not connect to the broker " + e.getMessage());
             }
         }
         return connected;
     }
 
     /**
      * Publishes a message on the given topic.
      *
      * @param topic
      * @param message should be stringified JSON
      */
     public void publish(String topic, String message) {
         if (mqttClient != null) {
             Log.d(WORKER_NAME, "Publishing topic " + topic + " with message " + message);
             try {
                 MqttMessage payload = new MqttMessage(message.getBytes());
                 if (topic != null) {
                     MqttTopic mqttTopic = mqttClient.getTopic(topic);
                     mqttTopic.publish(payload);
                 } else {
                     Log.e(WORKER_NAME, "Could not publish to topic " + topic + " with message " + message + ". Topic not initiated, call exists to set up first!");
                 }
             } catch (MqttPersistenceException e) {
                Log.e(WORKER_NAME, "Could not publish to topic " + topic + " with message " + message + ". Error: " + e.getMessage());
             } catch (MqttException e) {
                Log.e(WORKER_NAME, "Could not publish to topic " + topic + " with message " + message + ". Error: " + e.getMessage());
             }
         } else {
             Log.e(WORKER_NAME, "Could not publish to topic " + topic + " with message " + message + ". Is the client initiated?");
         }
     }
 
     /**
      * Subscribes to the given topic
      *
      * @param topic
      */
     public void subscribe(String topic) {
         try {
             mqttClient.subscribe(topic, 2);
         } catch (MqttSecurityException e) {
             e.printStackTrace();
         } catch (MqttException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Unsubscribes from the given topic
      *
      * @param topic
      */
     public void unsubscribe(String topic) {
         try {
             mqttClient.unsubscribe(topic);
         } catch (MqttException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * To prevent out of memory errors when passing around the install zip file
      * this method should be used to fetch the data when the action is 'getData'.
      *
      * @return a Base64 encoded string with the zip data
      */
     public String getApplicationRawData() {
         return data;
     }
 
     /**
      * Called when messages are received. Filters out data from installation
      * messages since it is too much to pass around as Strings.
      */
     class CustomMqttCallback implements MqttCallback {
 
         private static final String ACTION_GET_METHOD = "getData";
 
         @Override
         public void messageArrived(MqttTopic topic, MqttMessage message) {
             JSONObject json;
             String payload = "";
             String stringTopic = "";
             try {
                 json = new JSONObject(message.toString());
                 stringTopic = topic.toString();
                 // Filter install messages and their data separately
                 if (json.getString(ACTION).equals(ACTION_INSTALL) && stringTopic.equals(TOPIC_SYSTEM)) {
                     data = json.getString(ACTION_DATA);
                     json = new JSONObject();
                     json.put(ACTION, ACTION_INSTALL);
                     json.put(ACTION_DATA, ACTION_GET_METHOD);
                     payload = json.toString();
                 } else {
                     payload = message.toString();
                 }
             } catch (JSONException e) {
                 e.printStackTrace();
             }
 
             callback.onMessage(stringTopic, payload);
             Log.d(WORKER_NAME, "messageArrived" + "topic:" + stringTopic + ", message:" + payload);
         }
 
         @Override
         public void deliveryComplete(MqttDeliveryToken token) {
             Log.d(WORKER_NAME, "deliveryComplete " + "token:" + token);
         }
 
         @Override
         public void connectionLost(Throwable cause) {
             Log.d(WORKER_NAME, "connectionLost " + "cause:" + cause.toString());
         }
     }
 }
