 package watch;
 import java.net.URL;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.io.IOException;
import com.rabbitmq.client.*;
 
 public class Client {
 		
 	public static void main(String[] args) {
 		
 		//Server URL and port
 		String serverUrl = "http://localhost:8080";
		
		//Name of the directory to be watched and name of exchange
		String dirName = "/Users/tjt7a/tmp/";
		
 		XmlRpcClientConfigImpl config;
 		XmlRpcClient client;
 		
 		try{
 			config = new XmlRpcClientConfigImpl();
 			config.setServerURL(new URL(serverUrl));
 			
 			client = new XmlRpcClient();
 			client.setConfig(config);
 			
 			//Parameters to be pushed to RPC Server handler
			Object[] parameters = new Object[]{new String(dirName)};
 			//Object[] parameters = new Object[]{new Integer(2), new Integer(4), new Integer(6)};
 			
 			String i = (String)client.execute("HandlerClass.register", parameters);
 			System.out.println("Returned: "+i);
 			
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("elmer.cs.virginia.edu");
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			
			channel.exchangeDeclare(dirName, "fanout");
			String queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, dirName, "");
			
			System.out.println(" [*] Waiting for messages.");
			
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(dirName, true, consumer);
			
			while(true){
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String message = new String(delivery.getBody());
				
				System.out.println(" [x] Received: "+message);
			}
			
			
 		}catch(Exception e){
 			System.out.println("RPC_Client: "+e);
 			e.printStackTrace();
 		}
 	}
 
 }
