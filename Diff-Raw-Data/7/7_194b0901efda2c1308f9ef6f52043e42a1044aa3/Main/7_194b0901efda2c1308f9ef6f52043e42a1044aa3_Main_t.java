 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import MQTTLib.Message;
 
 
 
 
 
 
 public class Main {
 	
 	static SocketClient client;
 	static SocketClient senderClient;
 	static String brokerHostIp = "localhost";
 	static int brokerPort = 1883;
 	static String url = "localhost:27017";
 	static String messageDB = "gopartyon_message";
 	static int reSendTime = 10000;
 	static int awakeTime = 20000;
 	
 	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
 	
 		Start(brokerHostIp, brokerPort);
 		//Send(brokerHostIp, brokerPort);
 		
 		
 	}
 	
 	public static void Start(String broker_ip_addr, int broker_port) throws UnknownHostException, IOException, InterruptedException{
 		client = new SocketClient("FUNCUBE_MQTT_SERVER");
 		client.connect(broker_ip_addr, broker_port);
 		client.subscribe("$sys/");
 		
 		System.out.println("::.. MQTT Server ..::");
 		System.out.println("===Waiting for Connection===");
 		
 		mongodb db= new mongodb();
 		try {db.start(url, messageDB);} catch (Exception e) {  e.printStackTrace();	}
 		
 		senderClient = new SocketClient("FUNCUBE_MQTT_SENDER");
 		senderClient.connect(broker_ip_addr, broker_port);
 		System.out.println("::.. MQTT Sender ..::");
 		System.out.println("===Waiting for Connection===");
 		
 		MqttSend mqttSend = new MqttSend();
 		mqttSend.start();
 		
 		while(true){
 			System.out.println("I am awake!");
 			
 			client.publish("$hello/awake","FUNCUBE_MQTT_SERVER awake!");
 			senderClient.publish("$hello/awake","FUNCUBE_MQTT_SENDER awake!");
 			
 			try{Thread.sleep(awakeTime);}catch(Exception e){}
 		}
 	}
 	
 	public static void Send(String broker_ip_addr, int broker_port) throws IOException, InterruptedException{
 		System.out.println(" Send Loop ...");
 		
 		// Get 閮
		mongodb db= new mongodb();
  		List<MessageData> messageDateList = db.findAll(); 
 
 		// �潮�閮
 		for(MessageData currData : messageDateList)
 		{
 			try {
 				
				
 				senderClient.publish(currData.getTarget(), "0:" + "give:" + currData.getSerial() + ":" + currData.getMessage());
 
 				System.out.println("Send : " + currData.toString());
 				
 			} catch(Exception ex)
 			{
 				System.out.println("Send Exception : " + ex.getMessage());
 			}
 			
 			try{Thread.sleep((long) 0.001);}catch(Exception e){}
 		}
 		
 	}
 	
 
 	private static class MqttSend extends Thread {
 
 		@Override
 		public void run() {
 			
 			try {
 				while (true) {
 					
 					Send(brokerHostIp, brokerPort);
 					try{Thread.sleep(reSendTime);}catch(Exception e){}
 				}
 			} catch (Exception e) {
 			}
 		}
 	}
 }
