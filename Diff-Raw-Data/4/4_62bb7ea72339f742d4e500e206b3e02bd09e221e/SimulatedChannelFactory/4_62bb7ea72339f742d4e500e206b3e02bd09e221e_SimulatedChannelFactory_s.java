 package edu.iastate.pdlreasoner.net.simulated;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Map;
 
 import org.jgroups.Address;
 import org.jgroups.Channel;
 import org.jgroups.ChannelException;
 import org.jgroups.ChannelFactory;
 import org.w3c.dom.Element;
 
 import edu.iastate.pdlreasoner.util.CollectionUtil;
 
 public class SimulatedChannelFactory implements ChannelFactory {
 
 	private Map<Address,SimulatedChannel> m_Channels;
 	
 	public SimulatedChannelFactory() {
 		m_Channels = CollectionUtil.makeMap();
 	}
 	
 	public SimulatedChannel getChannel(Address add) {
 		return m_Channels.get(add);
 	}
 	
 	public Collection<Address> getAllChannelAddresses() {
 		return m_Channels.keySet();
 	}
 	
 	public void removeChannel(Address localAddress) {
		m_Channels.remove(localAddress);
 	}
 
 	
 	@Override
 	public Channel createChannel() throws ChannelException {
 		SimulatedChannel channel = new SimulatedChannel(this);
 		synchronized (m_Channels) {
 			m_Channels.put(channel.getLocalAddress(), channel);
 		}
 		return channel;
 	}
 
 	
 	@Override
 	public Channel createChannel(Object arg0) throws ChannelException {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Channel createChannel(String arg0) throws Exception {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Channel createMultiplexerChannel(String arg0, String arg1) throws Exception {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Channel createMultiplexerChannel(String arg0, String arg1, boolean arg2, String arg3) throws Exception {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void setMultiplexerConfig(Object arg0) throws Exception {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void setMultiplexerConfig(File arg0) throws Exception {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void setMultiplexerConfig(Element arg0) throws Exception {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void setMultiplexerConfig(URL arg0) throws Exception {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void setMultiplexerConfig(String arg0) throws Exception {
 		throw new UnsupportedOperationException();
 	}
 
 	
 }
