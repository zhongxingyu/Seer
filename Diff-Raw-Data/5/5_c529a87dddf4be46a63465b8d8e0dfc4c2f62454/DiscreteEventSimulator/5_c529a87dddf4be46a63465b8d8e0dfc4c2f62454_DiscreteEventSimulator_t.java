 package ece358.networks.assignment2;
 
 import java.util.Random;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.lang.Math;
 
 public class DiscreteEventSimulator {
 	
 	public static boolean isMediumBusy = true;
 	public static boolean dbg = false;
 
 
 	class Packet {
 		public int generationTime;
 		public int serviceTime;
 	}
 
 	public static int MAX_TICKS = 1000000;
 
 	public static int TEST_RUN_TIMES = 5;
 	public static double TICK_DURATION = 0.0001; 	// one tick = one millisecond
 	public static int DEFAULT_TRANSMIT_TIME = 3;
 	
 	public static Random rand = new Random();
 
 	public static double unifRand() { 
 		Random random = new Random();
 		return random.nextDouble();
 
 	}
 	public static double unifExp(int packetsPerSecond) { 
 		return (double) -Math.log(1-unifRand())/(packetsPerSecond * TICK_DURATION);
 	}
 	
 	public static double calculatePropogationTime(int nodeSender, int nodeReceiver)
 	{
 		debug("CalculatePropogationTime: Sender: " + nodeSender + " Receiver: " + nodeReceiver);
 		return (10*(nodeSender - nodeReceiver))/250000000;
 	}
 	
 	public static double calculateTransferTime(int lanSpeed, int packetLength)
 	{
 		return packetLength/lanSpeed;
 	}
 	
 	class Node {
 		int waitDuration = 0;
 		boolean isTransmitting = false;
 		int transmissionRemaining = 0;
 		int pktGenerationTime = 0;
 		int id = -1;
 		int collisionCounter = 0;
 		
 		public Node() {}
 		
 		public void generateNextPacketArrival(int packetsPerSecond, int currentTick) {
 			if (pktGenerationTime == 0) {
 				pktGenerationTime = currentTick + (int)unifExp(packetsPerSecond);				
 			}
 		}
 		public boolean isTransmissionSuccessful() {
 			return true;
 		}
 		public boolean isCollisionDetected() {
 			return true;
 		}
 		public int calculateBEB() {
 			int exponentialTime = (int) (Math.random() * (Math.pow(2,collisionCounter) - 1));	
 			return exponentialTime;
 		}
 	}
 
 	//private final static int LAN_SPEED = 1000000;
 	//private static final int PACKET_LENGTH = 100;
 	private final static int NODE_DISTANCE = 10;
 	private final static double PROPAGATION_SPEED = 2.5E8;
 	
 	public static void main(String args[]) {
 		
 		if (args.length == 1 && args[0].equals("-usage")) {
 			debug("------------------------------------------------------------------------");
 			debug("Parameter description:");
 			debug("N: the number of computers connected to the LAN (variable)");
 			debug("A: Data packets arrive at the MAC layer following a Poisson process with an average arrival rate of A packets/second (variable)");
 			debug("W: the speed of the LAN (fixed)");
 			debug("L: packet length (fixed)");
 			debug("P: Persistence parameter for P-persistent CSMA protocols");
 			debug("-----------------------------------------------------------------------");
 			debug("Command Format: ");
 			debug("DiscreteEventSimulator <N> <A> <W> <L> <P>");
 			debug("");
  		}
  		else if (args.length == 5) {
  			
  			int compNum = Integer.parseInt(args[0]);
  			int pktArrivalRate = Integer.parseInt(args[1]);
  			int lanSpeed = Integer.parseInt(args[2]);
  			int pktLength = Integer.parseInt(args[3]);
  			int persistanceParam = Integer.parseInt(args[4]);
 
  			int transmitTime = (int) ((pktLength * 8) / (lanSpeed * TICK_DURATION));
  			
  			DiscreteEventSimulator des = new DiscreteEventSimulator();
  			ArrayList<Node> nodes = new ArrayList<Node>();
  			HashSet<Node> collisionsDetected = new HashSet<Node>();
  			
  			for (int i = 0; i < compNum; i++) {
  				Node n = des.new Node();
  				n.id = i;
  				n.generateNextPacketArrival(pktArrivalRate, 0);		
  				nodes.add(n);
  			}
  			
  			Node transmitter = null;
  			
  			for (int currentTick = 0; currentTick < MAX_TICKS; currentTick++) {
  				
  				if (transmitter != null && transmitter.transmissionRemaining > 0) {
  					transmitter.transmissionRemaining -= 1;
  				}
 				else if (transmitter != null && transmitter.transmissionRemaining == 0) {	
  	 				for (Node n : nodes) {
  	 					if (n.id == transmitter.id) {
  	 						n.generateNextPacketArrival(pktArrivalRate, currentTick);
  	 					}
  	 				}
  					transmitter = null;
  					isMediumBusy = false;
  				}
  				
  				if (!isMediumBusy) { //nobody else is using the medium, yay :)
 	 				collisionsDetected.clear();
 	 				for (int j = 0; j < nodes.size(); j++) {
 	 					Node currentNode = nodes.get(j);
 	 					if (currentTick == currentNode.pktGenerationTime) {
 	 						collisionsDetected.add(currentNode);		
 	 					}
 	 				}
 	 				
 	 				if (collisionsDetected.size() > 1) { //goddamnit, we got a collision :(
 	 					for (Node n : collisionsDetected) {
 							n.collisionCounter++;
 							n.waitDuration = n.calculateBEB();
 	 					}
 	 				}
 	 				else if (collisionsDetected.size() == 1) { //only 1 transmitter, life is good :)
 	 					// create packet
 	 					Packet p = des.new Packet();
 	 					Node source = collisionsDetected.iterator().next();
 	 					// send packet (ignore distance for now)
 	 					isMediumBusy = true;
 	 					source.transmissionRemaining = transmitTime;
 	 					transmitter = source;
 
 						source.pktGenerationTime = 0;
 	 				}
  				}
 
  				/*for (Node n : nodes) {
  					if (n.waitDuration > 0) {
  						n.waitDuration--;
  					}
  				}
  				
  				if (isMediumBusy && persistanceParam == 1) {
 	 				for (int j = 0; j < nodes.size(); j++) {
 	 					Node currentNode = nodes.get(j);
 	 					if (currentNode.pktGenerationTime > 0 && currentNode.pktGenerationTime > currentTick) { //never collided, hasn't detected busy before ...
 	 						currentNode.pktGenerationTime = currentTick + 1;
 	 					}
 	 				}
  				}
  				if (isMediumBusy && persistanceParam == 2) { //uh oh, the medium is busy! calculate random wait
 	 				for (int j = 0; j < nodes.size(); j++) {
 	 					Node currentNode = nodes.get(j);
 	 					if (currentNode.waitDuration == 0) { //never collided, hasn't detected busy before ...
 	 						currentNode.waitDuration = 	Math.min(2, (int) (Math.random() % 10)); //wait between 2 and 10
 	 					}
 	 				}
  				}
  				else if (isMediumBusy && persistanceParam == 3) { //uh oh, the medium is busy! calculate random wait
 	 				//????
  				}*/
  				
 				if (isMediumBusy)
  				{
  					for (int j = 0; j< nodes.size(); j++)
  					{
  						Node currentNode = nodes.get(j);
  						if(currentNode.pktGenerationTime > 0 && currentNode.pktGenerationTime > currentTick)
  						{
  							if(persistanceParam == 1)
  							{
  								currentNode.pktGenerationTime = currentTick + 1;
  							}
  							else if (persistanceParam == 2)
  							{
  								currentNode.pktGenerationTime = currentTick + Math.min(2, (int) (Math.random() % 10));
  							}
  							else if (persistanceParam == 3)
  							{
  								//Add this later.
  							}
  						}
  					}
  				}
  				
  				
  			}
  		}
  		else {
  			debug("Not enough arguments! Enter DiscreteEventSimulator -usage");
  		}
 
 
 	}
 
 	public static void debug(String msg) {
 		if(dbg)
 		{
 			System.out.println("DEBUG:" + msg);
 		}
 	}
 }
