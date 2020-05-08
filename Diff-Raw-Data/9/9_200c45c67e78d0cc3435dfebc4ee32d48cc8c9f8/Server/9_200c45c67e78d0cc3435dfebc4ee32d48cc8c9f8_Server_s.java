 package org.omo.cash;
 
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Queue;
 import javax.jms.Session;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.omo.core.MapModel;
 import org.omo.core.Model;
 import org.omo.core.MapModel.Adaptor;
 import org.omo.jms.RemotingFactory;
 
 public class Server {
 
 	private final static Log LOG = LogFactory.getLog(Server.class);
 
 	private static final String META_MODEL = "MetaModel";
 	private final int timeout=60000;
 	private final Connection connection;
 	private final Session session;
 	private final RemotingFactory<Model<String, String>> factory;
 	private final MapModel<String, String> metaModel;
 	private final Adaptor<String, String> adaptor = new  Adaptor<String, String>() {@Override public String getKey(String value) {return value;}};
 
 	public Server(String name, ConnectionFactory connectionFactory) throws JMSException {
 		connection = connectionFactory.createConnection();
 		connection.start();
 		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 		factory = new RemotingFactory<Model<String, String>>(session, Model.class, (Destination)null, timeout);
 		metaModel = new MapModel<String, String>(META_MODEL, adaptor);
 		Queue queue = session.createQueue(name + "." + META_MODEL);
 		factory.createServer(metaModel, queue);
 		metaModel.upsert(metaModel.getName()); // The metaModel is a Model !
 		LOG.info("Listening on: " + queue);
 		
 		// adding TradeFeed
 		String tradeFeedName = name + "." + "TradeFeed";
 //		RemotingFactory<Model<Integer, Trade>> serverFactory = new RemotingFactory<Model<Integer, Trade>>(session, Model.class, (Destination)null, timeout);
 //		Model<Integer, Trade> tradeFeed= new TradeFeed(tradeFeedName, 10,100L);
 //		serverFactory.createServer(tradeFeed, session.createQueue(tradeFeedName));
 //		tradeFeed.start();
 		metaModel.upsert(tradeFeedName);
 	}
 	
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		String name = (args.length == 0 ? "Server" : args[0]);
		String url = "peer://" + name + "/broker0?broker.persistent=false&broker.useJmx=false";
 		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
 		LOG.info("Broker URL: " +url);
 		new Server(name, connectionFactory);
 		
 		// keep going...
 		while (true)
 			try {
 				Thread.sleep(60*1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 }
