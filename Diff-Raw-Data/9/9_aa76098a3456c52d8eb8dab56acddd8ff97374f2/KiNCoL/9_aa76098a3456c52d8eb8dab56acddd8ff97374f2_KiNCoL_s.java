 package kimononet.kincol;
 
 import kimononet.geo.DefaultGeoDevice;
 import kimononet.geo.GeoDevice;
 import kimononet.geo.GeoLocation;
 import kimononet.geo.GeoMap;
 import kimononet.geo.GeoVelocity;
 import kimononet.geo.RandomWaypointGeoDevice;
 import kimononet.log.Logger;
 import kimononet.net.routing.QualityOfService;
 import kimononet.net.transport.DataPacket;
 import kimononet.peer.DefaultPeerEnvironment;
 import kimononet.peer.Peer;
 import kimononet.peer.PeerAddress;
 import kimononet.peer.PeerAgent;
 import kimononet.peer.PeerEnvironment;
 import kimononet.stat.MasterStatMonitor;
 import kimononet.stat.StatMonitor;
 import kimononet.stat.StatResults;
 
 public class KiNCoL extends Thread{
 	
 	/**
 	 * Internal at which packets will be send from a random source to a random
 	 * sink. This value is in milliseconds.
 	 */
 	private static final int PACKET_SENDING_INTERVAL = 500;
 	
 	/**
 	 * Numbers of seconds to allow beacon service to populate peers table prior
 	 * to sending packets. This value is in milliseconds.
 	 */
 	private static final int INITIALIZATION_DELAY = 2000;
 	
 	/**
 	 * Delay in seconds to wait before exiting the simulation. The delay will 
 	 * allow all running threads to successfully shutdown. This value is in 
 	 * milliseconds.
 	 */
 	private static final int SHUTDOWN_DELAY = 1000;
 	
 	
 	/**
 	 * The simulation will timeout with this specified value if the number of 
 	 * packets to send ({@link #numberOfPackets} is zero. In other words, this
 	 * timeout lets developers test out beacon service without sending any 
 	 * data packets.
 	 */
 	private static final int BEACON_SERVICE_SIMULATION_TIMEOUT = 6000;
 	
 	/**
 	 * Probability that at each interval an agent will explode. Value should be
 	 * in the range of [0, 1].
 	 */
 	private final float hostilityFactor;
 	
 	/**
 	 * Maximum number of packets to send out before killing all the services.
 	 */
 	private final float numberOfPackets;
 	
 	/**
 	 * All the agents currently participating in the simulation.
 	 */
 	private final PeerAgent[] agents;
 	
 	/**
 	 * Statistical monitor for all the agents participating in the current 
 	 * simulation.
 	 */
 	private final StatMonitor statMonitor;
 	
 	/**
 	 * The environment for all the peers participating in the simulation.
 	 */
 	private final PeerEnvironment peerEnvironment;
 	
 	private int beaconTimeout;
 	
 	public KiNCoL(int numberOfPeers, 
 				  GeoMap map, 
 				  float peerSpeed, 
 				  int numberOfPackets, 
 				  float hostilityFactor,
 				  int beaconTimeout,
 				  boolean gpsrSimulation){
 		
 		System.out.println("########PARAMETERS RECEIVED########");
 		System.out.println("Number of Peers:   \t " + numberOfPeers);
 		System.out.println("Number of Packets: \t " + numberOfPackets);
 		System.out.println("Map Size:          \t " + map);
 		System.out.println("Peer Velocity:     \t " + peerSpeed);
 		System.out.println("Hostility Factor:  \t " + hostilityFactor);
 		
 		this.beaconTimeout = beaconTimeout;
 		
 		if(hostilityFactor > 0.5){
 			Logger.info("Warning! You have specified a very hostile environment.");
 		}
 		
 		this.hostilityFactor = hostilityFactor;
 		this.numberOfPackets = numberOfPackets;
 		
 		peerEnvironment = new DefaultPeerEnvironment();
 		
 		if(beaconTimeout > 0)
 			peerEnvironment.set("beacon-service-timeout", "" + beaconTimeout);
 		
 		statMonitor = new MasterStatMonitor();;
 		
 		agents = new PeerAgent[numberOfPeers];
 		
 		for(int i = 0; i < numberOfPeers; i++){
 			
 			Peer peer = new Peer(PeerAddress.generateRandomAddress(), "peer-" + i);
 			GeoLocation peerLocation = GeoLocation.generateRandomGeoLocation(map);
 			GeoVelocity peerVelocity = new GeoVelocity(peerSpeed, GeoLocation.generateRandomBearing());
 			
 			//Agent zero is the destination/sink and hence is stationary.
 			GeoDevice geoDevice = (i == 0)? new DefaultGeoDevice(peerLocation, new GeoVelocity()) :
 				                            new RandomWaypointGeoDevice(peerLocation, peerVelocity, map);
 			
 			PeerAgent agent = new PeerAgent(peer, peerEnvironment, geoDevice);
 			agent.setGPSRSimulation(gpsrSimulation);
 			agent.setStatMonitor(statMonitor);
 			agents[i] = agent;
 		}
 		
 	}
 
 
 	public void run(){
 		
 		Logger.info("#########STARTING SIMULATION##########");
 	
 		long startTime = System.currentTimeMillis();
 		
 		//The stat result will store the final results to be displayed to the 
 		//user.
 		StatResults results = new StatResults();
 		
 		//Start all services.
 		for(PeerAgent agent : agents){
 			agent.startServices();
 		}
 		
 		
 		try {
 			
 			//If no data packets are going to be sent, then sleep for a long 
 			//time in order to get a chance to test out beacon service.
 			if(numberOfPackets == 0){
 				sleep(BEACON_SERVICE_SIMULATION_TIMEOUT + beaconTimeout);
 				
 				results.combine(statMonitor.getStats().getStatResults(null, null));
 				
 			//If data packets are supposed to be sent, wait for just a bit so
 			//that beacon service has enough time to populate its neighbor 
 			//tables.
 			}if(numberOfPackets > 0){
 				sleep(INITIALIZATION_DELAY + beaconTimeout);
 			}
 			
 			//Get a random sender.
 			PeerAgent source = getRandomPeerAgent();
 			PeerAgent destination = agents[0];	
 			
 			for(int i = 0; i < numberOfPackets; i++){
 				
 				//Find a random sender that is not the destination.
 				while((source = getRandomPeerAgent()) == destination){}
 				
 				//Account for agents exploding in hostile environments.
 				for(PeerAgent agent : agents){
 					
 					if(agent == source || agent == destination)
 						continue;
 					
 					if(Math.random() < hostilityFactor){
 						killAgent(agent);
 					}
 				}
 				
 				byte[] payload = new byte[]{0x01, 0x02};
 				
 				source.sendDataPacket(new DataPacket(source, destination.getPeer(), QualityOfService.REGULAR, payload));			
 			
 				//Sleep until the packet is delivered so we can compute the 
 				//statistical information. If this line was not here, then after
 				//calling the send 
 				sleep(PACKET_SENDING_INTERVAL);
 				
 				results.combine(statMonitor.getStats().getStatResults(source.getPeer().getAddress(), destination.getPeer().getAddress()));
 				
 				statMonitor.getStats().reset();
 			}
 			
 			Logger.info("Shutting down...");
 			
 			//Kill all services/threads.
 			killEveryone(agents);
 			
 			long stopTime = System.currentTimeMillis();
 			
 			//Wait for all the threads to die out/shutdown.
 			sleep(SHUTDOWN_DELAY + beaconTimeout);
 			
 			//Output the results.
 			Logger.info(results.toString());
 			
 			Logger.info("Total Time: \t " + (int)((stopTime - startTime) / 1000) + " seconds.\n");
 			
 			Logger.info("#########BEACON SERVICE RESULTS#########");
 			for(PeerAgent agent : agents){
 				
 				int peers2NeighborCount = 0;
 				
 				for(PeerAddress address : agent.getPeers2().keySet()){
 					peers2NeighborCount += agent.getPeers2().get(address).size();
 				}
 				
 				Logger.info("Agent: \t " + agent.getPeer().getName() + 
 						    " # of peers: \t " + agent.getPeers().size() + 
						    " # of peers2: \t " + peers2NeighborCount);
 			}
 			
 			
 			Logger.info("#################DONE###################");
 			
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 
 
 	/**
 	 * Returns a random peer agent.
 	 * @return a random peer agent.
 	 */
 	private PeerAgent getRandomPeerAgent(){
 		return (agents.length == 0)? 
 						null: 
 						agents[(int)(Math.random() * agents.length)];
 	}
 	
 	/**
 	 * Shuts down all the services of the specified peer agent.
 	 * @param agent The agent to kill.
 	 */
 	private void killAgent(PeerAgent agent){
 		if(agent != null)
 			agent.shutdownServices();
 	}
 	
 	/**
 	 * Kills all the agents specified.
 	 * @param agents The agents to kill.
 	 * @see #killAgent(PeerAgent)
 	 */
 	private void killEveryone(PeerAgent[] agents){
 			
 		for(PeerAgent agent : agents){
 			killAgent(agent);
 		}
 	}
 }
