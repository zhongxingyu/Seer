 package net.cloudengine.cti.utils;
 
 import java.net.InetAddress;
import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import net.cloudengine.cti.CallsMonitor;
 import net.cloudengine.cti.Connection;
 import net.cloudengine.cti.ConnectionListener;
 import net.cloudengine.cti.PhoneBootstrap;
 import net.cloudengine.cti.asterisk.AsteriskCallsMonitor;
 import net.cloudengine.cti.asterisk.SoftPhoneBootstrap;
 import net.cloudengine.pbx.PBXMonitor;
 import net.cloudengine.pbx.asterisk.AsteriskPBXMonitor;
 import net.cloudengine.rpc.controller.config.PropertyController;
 
 import org.asteriskjava.AsteriskVersion;
 import org.asteriskjava.config.ConfigFile;
 import org.asteriskjava.live.AsteriskAgent;
 import org.asteriskjava.live.AsteriskChannel;
 import org.asteriskjava.live.AsteriskQueue;
 import org.asteriskjava.live.AsteriskServer;
 import org.asteriskjava.live.AsteriskServerListener;
 import org.asteriskjava.live.CallerId;
 import org.asteriskjava.live.ManagerCommunicationException;
 import org.asteriskjava.live.MeetMeRoom;
 import org.asteriskjava.live.NoSuchChannelException;
 import org.asteriskjava.live.OriginateCallback;
 import org.asteriskjava.live.Voicemailbox;
 import org.asteriskjava.manager.ManagerConnection;
 import org.asteriskjava.manager.ManagerConnectionState;
 import org.asteriskjava.manager.ManagerEventListener;
 import org.asteriskjava.manager.ResponseEvents;
 import org.asteriskjava.manager.SendActionCallback;
 import org.asteriskjava.manager.action.EventGeneratingAction;
 import org.asteriskjava.manager.action.ManagerAction;
 import org.asteriskjava.manager.action.OriginateAction;
 import org.asteriskjava.manager.event.ManagerEvent;
 import org.asteriskjava.manager.response.ManagerResponse;
 
 import com.google.inject.AbstractModule;
 
 public class AsteriskTestModule extends AbstractModule {
 
 	private EventGenerator generator;
 	
 	public AsteriskTestModule(EventGenerator generator) {
 		this.generator = generator;
 	}
 	
 	@Override
 	protected void configure() {
 		bind(Connection.class).toInstance(new MockConnection(this.generator));
 		bind(PBXMonitor.class).to(AsteriskPBXMonitor.class);
 		bind(PropertyController.class).to(MockPropertyController.class);
 		bind(PhoneBootstrap.class).to(SoftPhoneBootstrap.class);
 		bind(CallsMonitor.class).to(AsteriskCallsMonitor.class);
 	}
 }
 
 class MockConnection implements Connection {
 
 	private AsteriskServer as;
 	
 	public MockConnection(EventGenerator generator) {
 		as = new MockAsteriskServer(generator);
 	}	
 	
 	@Override
 	public AsteriskServer getAsteriskServer() {
 		return as;
 	}
 
 	@Override
 	public void close() {}
 	
 	public void start() {
 		
 	}
 
 	@Override
 	public boolean isConnected() {
 		return true;
 	}
 
 	@Override
 	public void connect() {

 	}
 
 	@Override
 	public void register(ManagerEventListener listener) {
		as.getManagerConnection().addEventListener(listener);
 	}
 
 	@Override
 	public void register(ConnectionListener listener) {
 		// TODO Auto-generated method stub
 		
 	}
 }
 
 class MockManagerConnection implements ManagerConnection {
 
 	private EventGenerator generator;
 	
 	public MockManagerConnection(EventGenerator generator) { 
 		this.generator = generator; 
 	}
 	
 	@Override
 	public String getHostname() { return null; }
 
 	@Override
 	public int getPort() { return 0; }
 
 	@Override
 	public String getUsername() { return null; }
 
 	@Override
 	public String getPassword() { return null; }
 
 	@Override
 	public AsteriskVersion getVersion() { return null; }
 
 	@Override
 	public boolean isSsl() { return false; }
 
 	@Override
 	public InetAddress getLocalAddress() { return null; }
 
 	@Override
 	public int getLocalPort() { return 0; }
 
 	@Override
 	public InetAddress getRemoteAddress() { return null;}
 
 	@Override
 	public int getRemotePort() { return 0;}
 
 	@Override
 	public void registerUserEventClass(Class<? extends ManagerEvent> userEventClass) {}
 
 	@Override
 	public void setSocketTimeout(int socketTimeout) {}
 
 	@Override
 	public void setSocketReadTimeout(int socketReadTimeout) {}
 
 	@Override
 	public void login() {}
 
 	@Override
 	public void login(String events)  {}
 
 	@Override
 	public void logoff() throws IllegalStateException {}
 
 	@Override
 	public String getProtocolIdentifier() { return null; }
 
 	@Override
 	public ManagerConnectionState getState() { return null; }
 
 	@Override
 	public ManagerResponse sendAction(ManagerAction action) {return null; }
 
 	@Override
 	public ManagerResponse sendAction(ManagerAction action, long timeout) { return null; }
 
 	@Override
 	public void sendAction(ManagerAction action, SendActionCallback callback) { }
 
 	@Override
 	public ResponseEvents sendEventGeneratingAction(EventGeneratingAction action) { return null; }
 
 	@Override
 	public ResponseEvents sendEventGeneratingAction(EventGeneratingAction action, long timeout) {
 		return null;
 	}
 
 	@Override
 	public void addEventListener(ManagerEventListener eventListener) {
 		generator.setListener(eventListener);
 	}
 
 	@Override
 	public void removeEventListener(ManagerEventListener eventListener) { }
 	
 }
 
 class MockAsteriskServer implements AsteriskServer {
 
 	private ManagerConnection mc;
 	
 	public MockAsteriskServer(EventGenerator generator) {
 		mc = new MockManagerConnection(generator);
 	}
 	
 	@Override
 	public ManagerConnection getManagerConnection() {
 		return mc;
 	}
 
 	@Override
 	public AsteriskChannel originate(OriginateAction originateAction) {return null; }
 
 	@Override
 	public void originateAsync(OriginateAction originateAction, OriginateCallback cb) {}
 
 	@Override
 	public AsteriskChannel originateToExtension(String channel, String context, String exten, int priority, long timeout) { return null;}
 
 	@Override
 	public AsteriskChannel originateToExtension(String channel, String context,
 			String exten, int priority, long timeout, CallerId callerId,
 			Map<String, String> variables)
 			 {
 		return null;
 	}
 
 	@Override
 	public AsteriskChannel originateToApplication(String channel,
 			String application, String data, long timeout)
 			throws ManagerCommunicationException, NoSuchChannelException {
 		return null;
 	}
 
 	@Override
 	public AsteriskChannel originateToApplication(String channel,
 			String application, String data, long timeout, CallerId callerId,
 			Map<String, String> variables)
 			throws ManagerCommunicationException, NoSuchChannelException {
 		return null;
 	}
 
 	@Override
 	public void originateToExtensionAsync(String channel, String context,
 			String exten, int priority, long timeout, OriginateCallback callback)
 			throws ManagerCommunicationException {
 	}
 
 	@Override
 	public void originateToExtensionAsync(String channel, String context,
 			String exten, int priority, long timeout, CallerId callerId,
 			Map<String, String> variables, OriginateCallback callback)
 			throws ManagerCommunicationException {
 	}
 
 	@Override
 	public void originateToApplicationAsync(String channel, String application,
 			String data, long timeout, OriginateCallback callback)
 			throws ManagerCommunicationException {
 	}
 
 	@Override
 	public void originateToApplicationAsync(String channel, String application,
 			String data, long timeout, CallerId callerId,
 			Map<String, String> variables, OriginateCallback callback)
 			throws ManagerCommunicationException {
 	}
 
 	@Override
 	public Collection<AsteriskChannel> getChannels() { return null;	}
 
 	@Override
 	public AsteriskChannel getChannelByName(String name) { return null; }
 
 	@Override
 	public AsteriskChannel getChannelById(String id) { return null; }
 
 	@Override
 	public Collection<MeetMeRoom> getMeetMeRooms() { return null; }
 
 	@Override
 	public MeetMeRoom getMeetMeRoom(String roomNumber) { return null; }
 
 	@Override
 	public Collection<AsteriskQueue> getQueues() { return null; }
 
 	@Override
 	public Collection<AsteriskAgent> getAgents() { return null; }
 
 	@Override
 	public String getVersion() { return null; }
 
 	@Override
 	public int[] getVersion(String file) throws ManagerCommunicationException { return null;}
 
 	@Override
 	public String getGlobalVariable(String variable) { return null; }
 
 	@Override
 	public void setGlobalVariable(String variable, String value) {}
 
 	@Override
 	public Collection<Voicemailbox> getVoicemailboxes() {return null; }
 
 	@Override
 	public List<String> executeCliCommand(String command) {	return null; }
 
 	@Override
 	public boolean isModuleLoaded(String module) { return false; }
 
 	@Override
 	public void loadModule(String module) throws ManagerCommunicationException {}
 
 	@Override
 	public void unloadModule(String module) {}
 
 	@Override
 	public void reloadModule(String module) {}
 
 	@Override
 	public void reloadAllModules() {}
 
 	@Override
 	public ConfigFile getConfig(String filename) { return null; }
 
 	@Override
 	public void addAsteriskServerListener(AsteriskServerListener listener) {}
 
 	@Override
 	public void removeAsteriskServerListener(AsteriskServerListener listener) {}
 
 	@Override
 	public void shutdown() {}
 
 	@Override
 	public void initialize()  {}
 	
 }
