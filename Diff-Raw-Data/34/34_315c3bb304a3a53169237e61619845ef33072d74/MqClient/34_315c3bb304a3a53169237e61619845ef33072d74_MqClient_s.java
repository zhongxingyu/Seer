 package org.kryomq;
 
 import java.io.IOException;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.kryomq.Control.Command;
 
import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.serializers.FieldSerializer;
 import org.kryomq.kryonet.Client;
 import org.kryomq.kryonet.Connection;
 import org.kryomq.kryonet.KryoSerialization;
 import org.kryomq.kryonet.Listener;
 
 public class MqClient extends Listener {
 	private static final Logger log = LoggerFactory.getLogger(MqClient.class);
 	
 	protected String host;
 	protected int port;
 	protected Client client;
 	
 	protected TopicRegistry<MessageListener> registry = new TopicRegistry<MessageListener>();
 	
 	protected class CommandLatches {
 		private CountDownLatch[] latches;
 		public CommandLatches() {
 			latches = new CountDownLatch[Control.Command.values().length];
 			for(int i = 0; i < latches.length; i++)
 				latches[i] = new CountDownLatch(1);
 		}
 		
 		public void countDown(Control.Command command) {
 			latches[command.ordinal()].countDown();
 		}
 		
 		public void await(Control.Command command) {
 			try {
 				latches[command.ordinal()].await();
 			} catch(InterruptedException ie) {
 				Thread.currentThread().interrupt();
 			}
 		}
 	}
 	
 	protected CommandLatches latches = new CommandLatches();
 	
 	protected String personalTopic;
 	protected String controlledTopic;
 	protected int personalId;
 	
 	public MqClient(String host, int port) {
 		this.host = host;
 		this.port = port;
 	}
 	
 	public void start() throws IOException {
 		log.debug("{} connecting to {}:{}", this, host, port);
 		client = new Client(256*1024, 256*1024, new KryoSerialization(new MqKryo()));
 		client.start();
 		client.connect(10000, host, port, port);
 		client.addListener(this);
 		log.debug("{} connected and registered", this);
 	}
 	
 	public void stop() throws IOException {
 		log.debug("{} stoppping", this);
 		client.close();
 		client.stop();
 	}
 	
 	public String getPersonalTopic() {
 		latches.await(Command.PERSONAL_TOPIC);
 		return personalTopic;
 	}
 	
 	public int getPersonalId() {
 		latches.await(Command.PERSONAL_ID);
 		return personalId;
 	}
 	
 	public String getControlledTopic() {
 		latches.await(Command.CONTROLLED_TOPIC);
 		return controlledTopic;
 	}
 	
 	@Override
 	public void received(Connection connection, Object object) {
 		if(object instanceof Message) {
 			Message m = (Message) object;
 			log.trace("{} dispatching {}", this, m);
 			Set<MessageListener> subscribers = registry.get(m.topic);
 			for(MessageListener l : subscribers) {
 				l.messageReceived(m);
 			}
 		}
 		if(object instanceof Control) {
 			Control c = (Control) object;
 			switch(c.command) {
 			case PERSONAL_TOPIC:
 				personalTopic = c.topic;
 				latches.countDown(Command.PERSONAL_TOPIC);
 				log.debug("{} received personal topic:{}", this, personalTopic);
 				break;
 			case PERSONAL_ID:
 				personalId = Integer.parseInt(c.topic);
 				latches.countDown(Command.PERSONAL_ID);
 				log.debug("{} received personal id:{}", this, personalId);
 				break;
 			case CONTROLLED_TOPIC:
 				controlledTopic = c.topic;
 				latches.countDown(Command.CONTROLLED_TOPIC);
 				log.debug("{} received controlled topic:{}", this, controlledTopic);
 				break;
 			}
 		}
 	}
 	
 	public synchronized void subscribe(String topic, MessageListener subscriber) {
 		log.debug("{} subscribing {} to topic {}", this, subscriber, topic);
 		registry.subscribe(topic, subscriber);
 		client.sendTCP(new Control(Command.SUBSCRIBE, topic));
 		if(topic.startsWith(Topics.PRIVILEGED)) {
 			log.trace("{} making privileged subscription request to topic {}", this, topic);
 			client.sendTCP(new Control(Command.PRIVILEGED_SUBSCRIBE, topic));
 		}
 	}
 	
 	public synchronized void unsubscribe(String topic, MessageListener subscriber) {
 		log.debug("{} unsubscribing {} from topic {}", this, subscriber, topic);
 		if(registry.unsubscribe(topic, subscriber).size() == 0)
 			client.sendTCP(new Control(Command.UNSUBSCRIBE, topic));
 	}
 	
 	public void setOrigin(String topic) {
 		log.trace("{} making privileged set origin request to topic {}", this, topic);
 		client.sendTCP(new Control(Command.PRIVILEGED_SET_ORIGIN, topic));
 	}
 
 	public void send(Message message) {
 		if(message.reliable)
 			client.sendTCP(message);
 		else
 			client.sendUDP(message);
 	}
 }
