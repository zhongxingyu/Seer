 package se.chalmers.pd.sensors;
 
 import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
 import org.eclipse.paho.client.mqttv3.MqttException;
 import org.eclipse.paho.client.mqttv3.MqttMessage;
 import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
 
 /**
  * This worker launches an MQTT client in a separate thread and subscribes to
  * the systems basic topics. When a message is received, it broadcasts the
  * message using custom intent filters.
  * 
  * When the thread is started, the first thing the worker does is to create a
  * directory which it can use for temporary storage of mqtt messages. It then
  * connects to the broker.
  * 
  */
 public class MqttWorker extends Thread {
 
 	private static final String STORAGE_DIRECTORY = "/infotainment/";
 	private static final String WORKER_NAME = "MqttWorker";
 	private static final String BROKER = "tcp://192.168.43.147:1883";
 	private static final String CLIENT_NAME = "sensors";
 	private MqttClient mqttClient;
	private MqttConnectOptions connectionOptions;
 
 	public MqttWorker() {
 
 	}
 
 	@Override
 	public void run() {
 		try {
 			this.setName(WORKER_NAME);
 			String tmpDir = System.getProperty("user.dir") + STORAGE_DIRECTORY;
 			MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
			connectionOptions = new MqttConnectOptions();
            connectionOptions.setKeepAliveInterval(10);
 			mqttClient = new MqttClient(BROKER, CLIENT_NAME, dataStore);
 			mqttClient.connect();
 		} catch (MqttException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Publishes a message on the given topic.
 	 * 
 	 * @param topic
 	 *            the topic to publish on
 	 * @param message
 	 *            should be stringified JSON
 	 */
 	public void publish(String topic, String message) {
 		log("MqttController", "publishing topic " + topic + " with message " + message);
 		try {
 			MqttMessage payload = new MqttMessage(message.getBytes());
 			mqttClient.getTopic(topic).publish(payload);
 		} catch (MqttPersistenceException e) {
 			e.printStackTrace();
 		} catch (MqttException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Helper method that logs to system out.
 	 * 
 	 * @param title
 	 *            a title reference to the output
 	 * @param message
 	 *            a message containing debug information
 	 */
 	private void log(String title, String message) {
 		System.out.println(title + ", " + message);
 	}
 }
