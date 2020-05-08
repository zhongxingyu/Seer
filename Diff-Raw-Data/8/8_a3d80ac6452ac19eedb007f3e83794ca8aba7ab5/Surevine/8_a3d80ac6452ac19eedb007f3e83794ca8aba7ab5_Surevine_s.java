 package com.surevine.profileserver.packetprocessor.iq.namespace.surevine;
 
 import java.util.Properties;
 import java.util.concurrent.BlockingQueue;
 
 import org.xmpp.packet.IQ;
 import org.xmpp.packet.Packet;
 
 import com.surevine.profileserver.db.DataStore;
 import com.surevine.profileserver.packetprocessor.PacketProcessor;
 import com.surevine.profileserver.packetprocessor.iq.namespace.AbstractNamespace;
 
 public class Surevine extends AbstractNamespace {
 
 	public static final String NAMESPACE_URI = "http://surevine.com/xmpp/profiles/1";
 
 	public Surevine(BlockingQueue<Packet> outQueue, Properties configuration,
 			DataStore dataStore) {
 
 		super(outQueue, configuration, dataStore);
 	}
 
 	@Override
 	protected PacketProcessor<IQ> get() {
 		return null;
 	}
 
 	@Override
 	protected PacketProcessor<IQ> set() {
		return new Set(outQueue, configuration, dataStore);
 	}
 
 	@Override
 	protected PacketProcessor<IQ> result() {
 		return null;
 	}
 
 	@Override
 	protected PacketProcessor<IQ> error() {
 		return null;
 	}
 }
