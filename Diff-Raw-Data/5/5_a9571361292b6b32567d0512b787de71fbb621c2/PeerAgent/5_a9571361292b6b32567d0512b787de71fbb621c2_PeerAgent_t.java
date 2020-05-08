 package kimononet.peer;
 
 import java.util.HashMap;
 
 import kimononet.geo.DefaultGeoDevice;
 import kimononet.geo.GeoDevice;
 import kimononet.geo.GeoService;
 import kimononet.net.beacon.BeaconService;
 import kimononet.net.p2p.port.PortConfiguration;
 import kimononet.net.p2p.port.PortConfigurationProvider;
 import kimononet.net.p2p.port.SimulationPortConfigurationProvider;
 import kimononet.net.transport.DataService;
 import kimononet.stat.StatMonitor;
 import kimononet.time.SystemTimeProvider;
 import kimononet.time.TimeProvider;
 
 /**
  * Peer agent represents a peer associated with a specific environment. Agent is
  * also responsible for managing services such {@link BeaconService} and
  * {@link DataService}, and storing common data exchanged by these services. 
  * 
  * @author Zorayr Khalapyan
  * @version 3/12/2012
  *
  */
 public class PeerAgent {
 
 	/**
 	 * Current peer represented by this agent.
 	 */
 	private Peer peer;
 	
 	/**
 	 * Peer discovery service associated with the current peer. Peer discovery
 	 * service is responsible for sending beacons and beacon acknowledgments.
 	 */
 	private BeaconService beaconService;
 	
 	/**
 	 * Stores current peer's environment information.
 	 */
 	private PeerEnvironment environment;
 	
 	/**
 	 * Peer's GPS device.
 	 */
 	private GeoDevice geoDevice;
 	
 	/**
 	 * Service that update's the current peer's GPS location at a certain
 	 * frequency.
 	 */
 	private GeoService geoService;
 	
 	/**
 	 * Port configuration provider for the current peer. Services that need to 
 	 * create socket connections for various services will consult the provider
 	 * for configuring the correct port numbers. 
 	 */
 	private PortConfigurationProvider portConfigurationProvider;
 	
 	/**
 	 * Neighboring peer's mapped to a peer address. This is ROUTING_TABLE_1 
 	 * described in the protocol documentation. To access the peers table, use
 	 * method {@link #getPeers()}.
 	 * 
 	 * @see #getPeers()
 	 */
 	private HashMap<PeerAddress, Peer> peers;
 	
 	/**
 	 * Stores second-hop neighbors: given a peer address, it will get the peers
 	 * map of that peer if it exists. This is ROUTING_TABLE_2 specified in the 
 	 * protocol documentation. 
 	 * 
 	 * @see #getPeers2()
 	 */
 	private HashMap<PeerAddress, HashMap<PeerAddress, Peer>> peers2;
 	
 	/**
 	 * Time provider for the current Peer. 
 	 */
 	private TimeProvider timeProvider;
 	
 	/**
 	 * Service for gathering statistical information for the peer represented
 	 * by this agent. All packets sent or received by this peer should be 
 	 * reported to this monitor.
 	 * 
 	 * @see #getStatMonitor()
 	 * @see #setStatMonitor(StatMonitor)
 	 */
 	private StatMonitor statMonitor;
 	
 	/**
 	 * Creates a peer agent with a default peer environment.
 	 * 
 	 * @param peer The peer represented by the peer agent.
 	 */
 	public PeerAgent(Peer peer){
		this(peer, new DefaultPeerEnvironment(),  new DefaultGeoDevice(peer.getLocation(), peer.getVelocity()));
 	}
 	
 	/**
 	 * Creates a peer agent associated with the specified peer environment.
 	 * 
 	 * @param peer The peer represented by the peer agent.
 	 * @param environment The peer's environment.
 	 */
 	public PeerAgent(Peer peer, PeerEnvironment environment){
		this(peer, environment, new DefaultGeoDevice(peer.getLocation(), peer.getVelocity()));
 	}
 	
 	/**
 	 * Creates a peer agent associated with the specified peer environment. The
 	 * constructor utilizes {@link SimulationPortConfigurationProvider} to set
 	 * up port configuration. 
 	 * 
 	 * @param peer The peer represented by the peer agent.
 	 * @param environment The peer's environment.
 	 * @param geoDevice The peer's GPS device.
 	 */
 	public PeerAgent(Peer peer, PeerEnvironment environment, GeoDevice geoDevice){
 		
 		this(peer, 
 		     environment, 
 		     geoDevice, 
 		     new SimulationPortConfigurationProvider());
 	}
 	
 	/**
 	 * Creates a peer agent associated with the specified peer environment.
 	 * Time provider will be set to {@link SystemTimeProvider}.
 	 * 
 	 * @param peer The peer represented by the peer agent.
 	 * @param environment The peer's environment.
 	 * @param geoDevice The peer's GPS device.
 	 * @param portConfigurationProvider Port configuration provider.
 	 */
 	public PeerAgent(Peer peer, 
 					 PeerEnvironment environment, 
 					 GeoDevice geoDevice, 
 					 PortConfigurationProvider portConfigurationProvider){
 		
 			this(peer, 
 			     environment, 
 			     geoDevice, 
 			     portConfigurationProvider,
 			     new SystemTimeProvider());
 	}
 	
 	/**
 	 * Creates a peer agent associated with the specified peer environment.
 	 * 
 	 * @param peer The peer represented by the peer agent.
 	 * @param environment The peer's environment.
 	 * @param geoDevice The peer's GPS device.
 	 * @param portConfigurationProvider Port configuration provider.
 	 * @param timeProvider Time provider for the current peer agent.
 	 */
 	public PeerAgent(Peer peer, 
 					 PeerEnvironment environment, 
 					 GeoDevice geoDevice, 
 					 PortConfigurationProvider portConfigurationProvider,
 					 TimeProvider timeProvider){
 		
 		//Save the parameters specified by the constructor.
 		this.peer                      = peer;
 		this.environment               = environment;
 		this.geoDevice                 = geoDevice;
 		this.portConfigurationProvider = portConfigurationProvider;
 		this.timeProvider 			   = timeProvider;
 		
 		//Create any data structures used by the agent.
 		this.peers = new HashMap<PeerAddress, Peer>();
 		this.peers2 = new HashMap<PeerAddress, HashMap<PeerAddress, Peer>>();
 	}
 	
 	public StatMonitor getStatMonitor(){
 		return this.statMonitor;
 	}
 	
 	public void setStatMonitor(StatMonitor statMonitor){
 		this.statMonitor = statMonitor;
 	}
 	
 	
 	/**
 	 * Starts beacon service and also geo-service. Beacon service is responsible
 	 * for sending beacons and responding to beacons, while geo-service is 
 	 * responsible for fetching the peer's current GPS location via a GPS 
 	 * enabled device and update velocity information. To shutdown the services,
 	 * use {@link #shutdownServices()}.
 	 * 
 	 * @see #shutdownServices()
 	 */
 	public void startServices(){
 		//Create the services used by the agent.
 		this.beaconService = new BeaconService(this);		
 		this.geoService    = new GeoService(this);
 		
 		this.geoService.startService();
 		this.beaconService.start();
 		
 	}
 	
 	/**
 	 * Stops services associated with this peer agent. Current implementation 
 	 * includes services for sending beacons, and service for updating peer's
 	 * GPS location.
 	 * 
 	 * @see #startServices()
 	 */
 	public void shutdownServices(){
 		this.geoService.shutdownService();
 		this.beaconService.shutdownService();
 	}
 	
 	/**
 	 * Returns the current peer's peers. These are single-hop peers that were
 	 * learned about via beacon service. To get the second-hop peers, use 
 	 * {@link #getPeers2()}.
 	 * 
 	 * @return The current peer's peers.
 	 */
 	public HashMap<PeerAddress, Peer> getPeers(){
 		// TODO add blocking for while table is updating
 		return this.peers;
 	}
 	
 	/**
 	 * Returns the second hop neighbor's table. For further documentation, read
 	 * comments on {@link #peers2} or protocol documentation.
 	 * 
 	 * @return Second hop neighbor's table.
 	 */
 	public HashMap<PeerAddress, HashMap<PeerAddress, Peer>> getPeers2(){
 		// TODO add blocking for while table is updating
 		return this.peers2;
 	}
 	
 	/**
 	 * Returns current peer's port configuration. 
 	 * @return Peer's port configuration. 
 	 */
 	public PortConfiguration getPortConfiguration(){
 		return this.portConfigurationProvider.getPortConfiguration(this);
 	}
 	
 	/**
 	 * Returns the peer's GPS device.
 	 * @return The peer's GPS device.
 	 */
 	public GeoDevice getGeoDevice(){
 		return geoDevice;
 	}
 	
 	/**
 	 * Returns the peer represented by this agent.
 	 * @return The peer represented by this agent.
 	 */
 	public Peer getPeer() {
 		return peer;
 	}
 
 	/**
 	 * Returns the peer's environment.
 	 * @return the peer's environment.
 	 */
 	public PeerEnvironment getEnvironment() {
 		return environment;
 	}
 
 	/**
 	 * Sets the peer's environment. 
 	 * @param environment Peer's new environment.
 	 */
 	public void setEnvironment(PeerEnvironment environment) {
 		this.environment = environment;
 	}
 	
 	/**
 	 * Returns {@link TimeProvider} associated with this agent. The time 
 	 * provider should be used in any situation where the peers's time is 
 	 * required. 
 	 * 
 	 * @return Time provider for this peer agent.
 	 */
 	public TimeProvider getTimeProvider(){
 		return this.timeProvider;
 	}
 	
 	/**
 	 * Sets the time provider for the current peer agent. To get the time 
 	 * provider or the peer's current time, use either 
 	 * {@link #getTimeProvider()} or {@link #getTime()} respectively. 
 	 * 
 	 * @param timeProvider The time provider to set.
 	 */
 	public void setTimeProvider(TimeProvider timeProvider){
 		this.timeProvider = timeProvider;
 	}
 	
 	/**
 	 * Shortcut method for accessing time using the {@link #getTimeProvider()}. 
 	 * 0 will be returned, if the time provider for the current agent is null. 
 	 * 
 	 * @return Current peer time as specified by the time provider.
 	 */
 	public long getTime(){
 		return (this.timeProvider == null)? 0 : this.timeProvider.getTime();
 	}
 }
