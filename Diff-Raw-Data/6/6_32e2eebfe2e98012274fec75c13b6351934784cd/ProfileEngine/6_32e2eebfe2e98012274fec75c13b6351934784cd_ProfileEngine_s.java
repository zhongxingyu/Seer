 package com.surevine.profileserver;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.log4j.Logger;
 import org.xmpp.component.Component;
 import org.xmpp.component.ComponentException;
 import org.xmpp.component.ComponentManager;
 import org.xmpp.packet.JID;
 import org.xmpp.packet.Packet;
 
import com.surevine.profileserver.db.DefaultNodeStoreFactoryImpl;
 import com.surevine.profileserver.db.NodeStoreFactory;
 import com.surevine.profileserver.db.exception.NodeStoreException;
 import com.surevine.profileserver.queue.InQueueConsumer;
 import com.surevine.profileserver.queue.OutQueueConsumer;
 
 public class ProfileEngine implements Component {
 
 	private static final Logger logger = Logger.getLogger(ProfileEngine.class);
 
 	private JID jid = null;
 	private ComponentManager manager = null;
 
 	private BlockingQueue<Packet> outQueue = new LinkedBlockingQueue<Packet>();
 	private BlockingQueue<Packet> inQueue = new LinkedBlockingQueue<Packet>();
 
 	private NodeStoreFactory nodeStoreFactory;
 
 	private Configuration conf;
 
 	public ProfileEngine(Configuration conf) {
 		this.conf = conf;
 	}
 
 	@Override
 	public String getDescription() {
 		return "XMPP Profile Engine";
 	}
 
 	@Override
 	public String getName() {
 		return "xmpp-profile-engine";
 	}
 
 	@Override
 	public void initialize(JID jid, ComponentManager manager)
 			throws ComponentException {
 
 		this.jid = jid;
 		this.manager = manager;
 
 		setupManagers();
 		startQueueConsumers();
 
 		logger.info("XMPP Component started. We are '" + jid.toString()
 				+ "' and ready to accept packages.");
 	}
 
 	private void startQueueConsumers() {
 		OutQueueConsumer outQueueConsumer = new OutQueueConsumer(this, outQueue, conf);
 
 		InQueueConsumer inQueueConsumer = new InQueueConsumer(outQueue, conf,
 				inQueue, nodeStoreFactory);
 
 		outQueueConsumer.start();
 		inQueueConsumer.start();
 	}
 
 	private void setupManagers() throws ComponentException {
 		try {
			nodeStoreFactory = new DefaultNodeStoreFactoryImpl(conf);
 		} catch (NodeStoreException e) {
 			throw new ComponentException(e);
 		}
 	}
 
 	@Override
 	public void processPacket(Packet p) {
 		try {
 			this.inQueue.put(p);
 		} catch (InterruptedException e) {
 			logger.error(p);
 		}
 	}
 
 	public void sendPacket(Packet p) throws ComponentException {
 		manager.sendPacket(this, p);
 	}
 
 	@Override
 	public void shutdown() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void start() {
 		/**
 		 * Notification message indicating that the component will start
 		 * receiving incoming packets. At this time the component may finish
 		 * pending initialization issues that require information obtained from
 		 * the server.
 		 * 
 		 * It is likely that most of the component will leave this method empty.
 		 */
 	}
 
 	public JID getJID() {
 		return this.jid;
 	}
 }
