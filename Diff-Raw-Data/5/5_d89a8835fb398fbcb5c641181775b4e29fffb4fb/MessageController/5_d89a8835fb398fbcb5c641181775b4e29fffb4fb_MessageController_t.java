 package common;
 
 import java.io.IOError;
 import java.io.IOException;
 
 import com.rabbitmq.client.*;
 
 public class MessageController {
 	//RMQ Members.
 	private ConnectionFactory factory;
 	private Connection connection;
 	private Channel channel;
 	private QueueingConsumer consumer;
 	
 	private String queueName;
 	
 	public MessageController(String queueName) throws IOException{		
 		this.queueName = queueName;
 		
 		//Initialize RabbitMQ connection.
 		factory = new ConnectionFactory();
 		factory.setHost(Common.RMQServer);
 		connection = factory.newConnection();
 		channel = connection.createChannel();
 		
 		//Initialize Node queue.
 		this.channel.queueDeclare(this.queueName, true, false, false, null);
 	}
 	
 	public void DeclareExchange(String exchange, String exchangeType, Boolean bindQ) throws IOException
 	{
 		this.channel.exchangeDeclare(exchange, exchangeType);
		
 		if (bindQ)
 			this.channel.queueBind(this.queueName, exchange, this.queueName);
		else
			this.channel.queueUnbind(this.queueName, exchange, this.queueName);
		
 	}
 
 	public void InitializeConsumer() throws IOException
 	{
 		this.consumer = new QueueingConsumer(this.channel);
 		this.channel.basicConsume(this.queueName, true, this.consumer);
 	}
 	
 	//Receive a new Message from the RMQ Server queue for node
 	public MessageWrapper ReceiveMessage() throws IOException, InterruptedException
 	{
 		//nextDelivery(long milliseconds) --> timeout interval for blocking receiving from queue.
 		MessageWrapper wrapper = null;
 		
 		QueueingConsumer.Delivery delivery = consumer.nextDelivery(500);
 		if(delivery != null)
 		{
 			String msg = new String(delivery.getBody());
 			wrapper = MessageWrapper.getDeSerializedMessage(msg); 
 		}
 		
 		return wrapper;
 	}
 	
 	//Send message to the queue.
 	public void SendMessage(MessageWrapper message, String exchangeName, String routingKey) throws IOException
 	{
 		channel.basicPublish(exchangeName, routingKey, null, message.getSerializedMessage().getBytes());		
 	}
 			
 }
