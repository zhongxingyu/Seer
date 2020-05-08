 package org.kryomq;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.kryomq.Control.Command;
 
import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.serializers.FieldSerializer;
 import org.kryomq.kryonet.Connection;
 import org.kryomq.kryonet.KryoSerialization;
 import org.kryomq.kryonet.Listener;
 import org.kryomq.kryonet.Server;
 
 public class MqServer extends Listener {
 	private static final Logger log = LoggerFactory.getLogger(MqServer.class);
 	
 	protected int port;
 	
 	protected Server server;
 	
 	protected TopicRegistry<Connection> registry = new TopicRegistry<Connection>();
 	protected TopicRegistry<Connection> privileged = new TopicRegistry<Connection>();
 	
 	protected Map<Connection, String> origins = new ConcurrentHashMap<Connection, String>();
 	
 	public MqServer(int port) {
 		this.port = port;
 			
 	}
 	
 	public int getPort() {
 		return port;
 	}
 	
 	public void start() throws IOException {
 		log.debug("{} starting server on port {}", this, port);
 		server = new Server(1024*256, 1024*256, new KryoSerialization(new MqKryo()));
 		server.start();
 		server.bind(port, port);
 		server.addListener(this);
 		log.debug("{} started server on port {}", this, port);
 	}
 	
 	public void stop() throws IOException {
 		log.debug("{} stopping server on port {}", this, port);
 		server.close();
 		server.stop();
 	}
 	
 	@Override
 	public void connected(Connection connection) {
 		String personalTopic = Topics.PRIVILEGED + Topics.CLIENT + connection.getID();
 		String controlledTopic = Topics.CONTROLLED + Topics.CLIENT + connection.getID();
 		privileged.subscribe(personalTopic, connection);
 		privileged.subscribe(controlledTopic, connection);
 		origins.put(connection, personalTopic);
 		server.sendToTCP(connection.getID(), new Control(Command.PERSONAL_TOPIC, personalTopic));
 		server.sendToTCP(connection.getID(), new Control(Command.CONTROLLED_TOPIC, controlledTopic));
 		server.sendToTCP(connection.getID(), new Control(Command.PERSONAL_ID, "" + connection.getID()));
 		server.sendToAllTCP(new Control(Command.CONNECTED, personalTopic));
 	}
 
 	@Override
 	public void disconnected(Connection connection) {
 		String personalTopic = Topics.PRIVILEGED + Topics.CLIENT + connection.getID();
 		server.sendToAllTCP(new Control(Command.DISCONNECTED, personalTopic));
 		registry.deregister(connection);
 		privileged.deregister(connection);
 		origins.remove(connection);
 	}
 	
 	@Override
 	public void received(Connection connection, Object object) {
 		if(object instanceof Message) {
 			Message m = (Message) object;
 			m.origin = origins.get(connection);
 			boolean authorized = true;
 			if(m.topic.startsWith(Topics.CONTROLLED)) {
 				authorized = false;
 				if(connection.getRemoteAddressTCP().getAddress().isLoopbackAddress())
 					authorized = true;
 				else if(privileged.get(m.topic).contains(connection))
 					authorized = true;
 			}
 			if(!authorized) {
 				log.trace("{} dropping unauthorized message from {} to {}", this, m.origin, m.topic);
 				return;
 			}
 			log.trace("{} dispatching message from {} to {}", this, m.origin, m.topic);
 			Set<Connection> subscribers = registry.get(m.topic);
 			for(Connection c : subscribers) {
 				if(m.reliable)
 					server.sendToTCP(c.getID(), m);
 				else
 					server.sendToUDP(c.getID(), m);
 			}
 		}
 		if(object instanceof Control) {
 			Control c = (Control) object;
 			switch(c.command) {
 			case SUBSCRIBE:
 				if(!c.topic.startsWith(Topics.PRIVILEGED) || privileged.get(c.topic).contains(connection)) {
 					log.trace("{} subscribing {} to topic {}", this, connection, c.topic);
 					registry.subscribe(c.topic, connection);
 				} else {
 					log.trace("{} not subscribing {} to privileged topic {}", this, connection, c.topic);
 				}
 				break;
 			case UNSUBSCRIBE:
 				log.trace("{} unsubscribing {} from topic {}", this, connection, c.topic);
 				registry.unsubscribe(c.topic, connection);
 				break;
 			case PRIVILEGED_SUBSCRIBE:
 				if(connection.getRemoteAddressTCP().getAddress().isLoopbackAddress()) {
 					log.trace("{} subscribing privileged {} to topic {}", this, connection, c.topic);
 					registry.subscribe(c.topic, connection);
 				} else {
 					log.trace("{} not subscribing privileged {} to topic {}", this, connection, c.topic);
 				}
 				break;
 			case PRIVILEGED_SET_ORIGIN:
 				if(connection.getRemoteAddressTCP().getAddress().isLoopbackAddress()) {
 					log.trace("{} setting privileged origin of {} to topic {}", this, connection, c.topic);
 					origins.put(connection, c.topic);
 				} else {
 					log.trace("{} not setting privileged origin of {} to topic {}", this, connection, c.topic);
 				}
 				break;
 			}
 		}
 	}
 }
