 package p2p.simulator.scenarios;
 
 import java.math.BigInteger;
 import java.util.Properties;
 
 import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;
 
 @SuppressWarnings("serial")
 public class Scenario1 extends Scenario {
 	
 	public static  int NUMBER_OF_PEERS;
 	public static  int NUMBER_OF_SUBCRIPTIONS;
 	public static  int NUMBER_OF_PUBLICATIONS;
 	public static  int NUMBER_OF_UNSUBSCRIPTIONS;
 	public static int NUMBER_OF_BITS;
 	public static String subscriptionsModel;
 	public static String publicationsModel;
 	
 	public static int NUMBER_OF_LONGLINKS;
 	
 	
 	public static Properties configFile = new Properties();
 	
 	private static SimulationScenario scenario = new SimulationScenario() {{
 		
 		try {
			configFile.load(this.getClass().getClassLoader().getResourceAsStream("simulation.properties"));
 		} catch (Exception e) {
 			System.err.println("Error: couldn't load the properties file in Scenario1.java");
 		}
 		
 		NUMBER_OF_PEERS = Integer.parseInt(configFile.getProperty("NumberOfNodes"));
 		NUMBER_OF_SUBCRIPTIONS = Integer.parseInt(configFile.getProperty("NumberOfSubscriptions"));
 		NUMBER_OF_PUBLICATIONS = Integer.parseInt(configFile.getProperty("NumberOfPublication"));
 		NUMBER_OF_UNSUBSCRIPTIONS = Integer.parseInt(configFile.getProperty("NumberOfUnsubscriptions"));
 		
 		subscriptionsModel = configFile.getProperty("SubscriptionsModel");
 		publicationsModel = configFile.getProperty("PublicationsModel");
 		
 		NUMBER_OF_BITS = Integer.parseInt(configFile.getProperty("NumberOfBits"));
 		
 		NUMBER_OF_LONGLINKS = Integer.parseInt(configFile.getProperty("NumberOfLonglinks"));
 		
 		StochasticProcess firstPeerStart = new StochasticProcess() {{
 			eventInterArrivalTime(constant(100));
 			//raise(1, Operations.serverStart, uniform(NUMBER_OF_BITS));
 			raise(1, Operations.peerJoin, uniform(NUMBER_OF_BITS));
 		}};
 
 		/*
 		StochasticProcess startOne = new StochasticProcess() {{
 			eventInterArrivalTime(constant(100));
 			raise(1, Operations.peerJoin, uniform(NUMBER_OF_BITS));
 		}};
 		*/
 		
 // ---------------------------------------------------------------------
 		// Joining
 		StochasticProcess joining = null;
 		if (subscriptionsModel.equals("twitter")) {
 			joining = new StochasticProcess() {{
 				eventInterArrivalTime(constant(100));
 				raise(1, Operations.allPeerJoin);
 			}};
 		}
 		else {
 			joining = new StochasticProcess() {{
 				eventInterArrivalTime(constant(100));
 				raise(NUMBER_OF_PEERS - 1, Operations.peerJoin, uniform(NUMBER_OF_BITS));
 			}};
 		}
 ///*
 // ---------------------------------------------------------------------
 		// Subscription
 		StochasticProcess subscribing = null;
 		if (subscriptionsModel.equals("random")) {
 			subscribing = new StochasticProcess() {{
 				eventInterArrivalTime(constant(100));
 				raise(NUMBER_OF_SUBCRIPTIONS, Operations.peerSubscribe, uniform(NUMBER_OF_BITS));
 			}};
 		} 
 		else if (subscriptionsModel.equals("correlated")) { 
 			subscribing = new StochasticProcess() {{
 				eventInterArrivalTime(constant(100));
 				raise(1, Operations.allPeerSubscribe_C);
 			}};
 		}
 		else if (subscriptionsModel.equals("twitter")) { 
 			// Subscription
 			subscribing = new StochasticProcess() {{
 				eventInterArrivalTime(constant(100));
 				raise(1, Operations.allPeerSubscribe_T);
 			}};
 		}
 		
 // ---------------------------------------------------------------------		
 		// Publication
 		StochasticProcess publishing = new StochasticProcess() {{
 			eventInterArrivalTime(constant(100));
 			raise(NUMBER_OF_PUBLICATIONS, Operations.peerPublish, uniform(NUMBER_OF_BITS));
 		}};
 		
 // ---------------------------------------------------------------------		
 		// Unsubscribe 
 		StochasticProcess unsubscribing = new StochasticProcess() {{
 			eventInterArrivalTime(constant(100));
 			raise(NUMBER_OF_UNSUBSCRIPTIONS, Operations.peerUnsubscribe, uniform(NUMBER_OF_BITS));
 		}};
 		
 		//*/
 // ---------------------------------------------------------------------
 		StochasticProcess termination = new StochasticProcess() {{
 			eventInterArrivalTime(constant(10000));
 			//raise(NUMBER_OF_PEERS/10, Operations.peerFail, uniform(NUMBER_OF_BITS));
 			raise(1, Operations.peerFail, uniform(NUMBER_OF_BITS));
 		}};
 
 		firstPeerStart.start();
 		joining.startAfterTerminationOf(5000, firstPeerStart);
 
 		subscribing.startAfterTerminationOf(500000, joining);
 		publishing.startAfterTerminationOf(80000, subscribing);
 		unsubscribing.startAfterTerminationOf(5000, publishing); 
 		// TODO: ask Amir why starting the unsubcribing process after 
 		// the subscribing process will make the execution stops without no clear reason.
 		
 		termination.startAfterTerminationOf(500, unsubscribing);
 	}};
 	
 //-------------------------------------------------------------------
 	public Scenario1() {
 		super(scenario);
 	} 
 }
