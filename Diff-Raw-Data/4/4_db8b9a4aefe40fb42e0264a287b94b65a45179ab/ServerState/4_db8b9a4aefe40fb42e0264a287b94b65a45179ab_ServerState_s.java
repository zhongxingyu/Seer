 package uk.ac.cam.jk510.part2project.server;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import uk.ac.cam.jk510.part2project.network.DataConnectionManager;
 import uk.ac.cam.jk510.part2project.network.DeviceConnection;
 import uk.ac.cam.jk510.part2project.network.MessageType;
 import uk.ac.cam.jk510.part2project.network.ServerMessage;
 import uk.ac.cam.jk510.part2project.session.Device;
 import uk.ac.cam.jk510.part2project.session.DeviceHandleIP;
 import uk.ac.cam.jk510.part2project.session.Session;
 import uk.ac.cam.jk510.part2project.session.SessionPackage;
 import uk.ac.cam.jk510.part2project.settings.Config;
 import uk.ac.cam.jk510.part2project.store.Coords;
 import uk.ac.cam.jk510.part2project.store.PositionStore;
 import uk.ac.cam.jk510.part2project.store.PositionStoreSubscriber;
 import uk.ac.cam.jk510.part2project.store.Response;
 
 
 public class ServerState implements PositionStoreSubscriber {
 
 	/*
 	 * This class holds the implementation state of the server, e.g which points from each device are new points.
 	 * It subscribes to PositionStore updates so can stay updated.
 	 */
 
 	protected static LinkedList<Coords>[] coordsToSend;	//one linkedlist for each device to send to. Client server so only one.
 
 	private static ArrayList<LinkedList<Integer>> globalNewPoints = new ArrayList<LinkedList<Integer>>();
 	private static boolean initialised = false;
 	private static long timeOfLastSend=0;
 	private static int numNewPoints=0;
 	private static SessionDeviceConnection[] sessionSetupConnections;
 	private static DeviceConnection[] connections;
 	private static boolean alive = true;
 
 	public static void main(String[] args) {
 		/*
 		 * Receive serialized session from some device.
 		 * Set up data storage structures
 		 * Set up datagram socket.
 		 * Spawn one listening thread.
 		 * Start periodic sending thread.
 		 * 
 		 */
 
 		//Receive serialized Session from some device
 		/*
 		 * Involves starting thread to listen for a connection
 		 */
 
 		Session session;
 
 
 
 		if(Config.singleSession()) {
 
 			int numDevices = Integer.parseInt(args[0]);
 			sessionSetupConnections = new SessionDeviceConnection[numDevices];
 
 			coordsToSend = new LinkedList[numDevices];
 			for(int i = 0; i<numDevices; i++) {
 				coordsToSend[i] = new LinkedList<Coords>();
 			}
 
 			//spawn thread that accepts connections
 			try {
 				ServerSocket serverSock = new ServerSocket(60000);
 				for(int i=0; i<numDevices; i++) {
 					System.out.println("Waiting for device "+i);
 					Socket sock = serverSock.accept();
 					sessionSetupConnections[i] = new SessionDeviceConnection(i, sock, numDevices);
 					sessionSetupConnections[i].connectAndReceive();
 					System.out.println("Connected to device "+i);
 				}
 
 
 
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 
 
 		} else {
 
 			SessionPackage pack = NetworkInterface.getSessionPackage();
 			session = Session.reconstructSession(pack);
 
 			coordsToSend = new LinkedList[session.numDevices()];
 			for(int i = 0; i<session.numDevices(); i++) {
 				coordsToSend[i] = new LinkedList<Coords>();
 			}
 			startMainProcessing(session);
 
 		}	
 
 	}
 
 	public static void startMainProcessing(Session session) {
 		//NetworkInterface net = NetworkInterface.getInstance();
 		ServerState.init();
 
 		//TODO spawn periodic sending thread
 		new Thread(new Runnable() {
 			public void run() {
 				while(alive) {
 					try {
 						Thread.sleep(Config.getServerResendPeriodMillis()+100);
 						ServerState.sendIfReady();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}).start();
 
 		System.out.println("Now listening for data");	//debug
 		initDataSockets();
 		for(final Device device: Session.getSession().getDevices()) {
 
 			new Thread(new Runnable() {
 				public void run() {
 
 					byte[] receivingData = new byte[1024];
 
 					while(alive) {
 						try {
 							ByteBuffer bb = DataConnectionManager.receive(connections[device.getDeviceID()], receivingData);
 							System.out.println("Recieved datagram");
 							ServerMessage.processData(bb);
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 					}
 				}
 			}).start();
 		}
 
 		//listen for incoming data and process it:
 		//		while(true) {
 		//			ServerMessage.processDatagram(net.receiveDatagram());
 		//		}
 	}
 
 	public static synchronized void initDataSockets() {
 		if(connections == null) {
 			connections = new DeviceConnection[Session.getSession().numDevices()];
 			//TODO make it ProtocolManager.numConnections instead or make it do it or something for server and all.
 			for(Device device: Session.getSession().getDevices()) {
 				try {
 					System.out.println("device");
 					connections[device.getDeviceID()] = DeviceConnection.newConnection(device);
 				} catch (SocketException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	//commented 23rd Jan
 	//	protected static void sendCoordsToAddress(final InetSocketAddress toSocketAddress, List<Coords> coordsList) {
 	//
 	//		System.out.println("sending to "+toSocketAddress.getAddress().getHostAddress()+":"+toSocketAddress.getPort());
 	//
 	//		byte[] data = new byte[(2 + 5*coordsList.size())*4];	//1 int for header, 1 int for fromID + 5 (int|float)s for each coord
 	//		ByteBuffer bb = ByteBuffer.wrap(data);
 	//
 	//		bb.putInt(MessageType.datapoints.ordinal());	//put header
 	//
 	//		bb.putInt(-1);	//server ID
 	//
 	//		for(Coords coords: coordsList) {
 	//
 	//			int aboutDeviceID = coords.getDevice();	//deviceID of the device whose location this point is.
 	//			int lClock = coords.getLClock();
 	//			float x = coords.getCoord(0);
 	//			float y = coords.getCoord(1);
 	//			float alt = coords.getCoord(2);
 	//
 	//			bb.putInt(aboutDeviceID);
 	//			bb.putInt(lClock);
 	//			bb.putFloat(x);
 	//			bb.putFloat(y);
 	//			bb.putFloat(alt);
 	//			System.out.println("sending. device "+aboutDeviceID+" lClock "+lClock+" x "+x+" y "+y+" alt "+alt);
 	//
 	//		}
 	//		try {
 	//			//checkInit();
 	//			DatagramPacket datagram = new DatagramPacket(data, data.length, toSocketAddress);
 	//			NetworkInterface.getInstance().sendDatagram(datagram);
 	//
 	//
 	//		} catch (SocketException e) {
 	//			// TODO Auto-generated catch block
 	//			e.printStackTrace();
 	//		} catch (IOException e) {
 	//			// TODO Auto-generated catch block
 	//			e.printStackTrace();
 	//		}
 	//	}
 
 	public synchronized static void sendIfReady() {
 		//init();	//init moved to Server.main
 		if(ready()) {
 			//TODO send points in batches, with configurable batch size
 			//sendNewPoints();
 			System.out.println("size of globalNewPoints = "+globalNewPoints.size());
 			int i = 0;
 			for(LinkedList<Integer> list: globalNewPoints) {
 				//int deviceNumber = globalNewPoints.indexOf(list);
 				int deviceNumber = i;
 				System.out.println("now doin points about device "+deviceNumber);
 				Device fromDevice = Session.getSession().getDevice(deviceNumber);
 
 				for(int index: list) {
 					Coords coords = PositionStore.getCoord(fromDevice, index);
 					for(Device toDevice: Session.getSession().getDevices()) {
 						//dont send to imaginary devices
 						if(Config.serverDuplicationTest() && toDevice.getDeviceID()!=0) {
 							//do nothing
 						} else {
 							if(Config.dontSendPointsToOwner() && (coords.getDevice() == toDevice.getDeviceID())) {
 								//don't send
 							} else {
 								//do send
 								System.out.println("adding "+index);
 								coordsToSend[toDevice.getDeviceID()].add(coords);
 							}
 						}
 
 						//byte[] data = new byte[1024];
 						//DatagramPacket datagram = new DatagramPacket(data, data.length, sockadd);
 
 
 						//net.sendCoordsToDevice(toDevice, fromDevice, coords);
 					}
 				}
 				i++;
 			}
 			for(LinkedList<Integer> list: globalNewPoints) {	//clear newPointsLists
 				list.clear();
 			}
 			sendCoordsInQueue();
 
 			numNewPoints = 0;
 		}
 	}
 
 	private static synchronized void sendCoordsInQueue() {
 		for(Device toDevice: Session.getSession().getDevices()) {
			if(toDevice.getDeviceID()!=0) {continue;}
 			if(!coordsToSend[toDevice.getDeviceID()].isEmpty()) {
 				System.out.println("Sending to device "+toDevice.getDeviceID());	//debug
 				InetSocketAddress sockadd = new InetSocketAddress(((DeviceHandleIP) toDevice.getHandle()).getIP().getHostName(), ((DeviceHandleIP) toDevice.getHandle()).getPort());
 				DataConnectionManager.sendCoordsToDevice(connections[toDevice.getDeviceID()], coordsToSend[toDevice.getDeviceID()]);
 				//sendCoordsToAddress(sockadd, coordsToSend[toDevice.getDeviceID()]);
 				coordsToSend[toDevice.getDeviceID()].clear();
 			}
 		}
 		timeOfLastSend = System.currentTimeMillis();	//reset timer
 	}
 
 	@Override
 	public synchronized void notifyOfUpdate(Device d, LinkedList<Integer> givenNewPoints) {
 		LinkedList<Integer> newPointsList = globalNewPoints.get(d.getDeviceID());
 		newPointsList.addAll(givenNewPoints);
 		numNewPoints += givenNewPoints.size();
 		System.out.println("new points: "+newPointsList.size());
 		sendIfReady();
 	}
 
 	static void init() {
 		if(!initialised) {
 			PositionStore.subscribeToUpdates(new ServerState());	//subscribe to updates
 			for(int i=0; i<Session.getSession().numDevices(); i++) {	//initialise lists
 				globalNewPoints.add(new LinkedList<Integer>());
 			}
 			initialised = true;
 		}
 	}
 	private static boolean ready() {
 		//Note ready is always false when there is just one device in session.
 		return (timeOfLastSend + Config.getServerResendPeriodMillis() <= System.currentTimeMillis()) || (numNewPoints>=Config.getServerNewPointsThreshold()) && Session.getSession().numDevices()>1;
 	}
 
 	public static void sendSessionToAllDevices(Session session) {
 		SessionPackage pack = new SessionPackage(session);
 		for(SessionDeviceConnection conn: sessionSetupConnections) {
 			try {
 				conn.sendSessionPackage(pack);
 			} catch (NullPointerException e) {
 				if(!Config.serverDuplicationTest()) {
 					e.printStackTrace();
 				}
 			}
 
 		}
 
 	}
 
 	public static void serviceRequest(int fromID, LinkedList<Integer>[] requestArray) {
 		Response[] responses = PositionStore.fulfillRequest(requestArray);
 		//LinkedList<Coords> response = PositionStore.fulfillRequest(requestArray);
 		List<Coords> coordsList = Response.getCoordsList(responses);
 		respondToNetwork(fromID, coordsList);
 
 		/*
 		 * This is server code, so since server doesnt have the "remainder" data,
 		 * only the generator of it does, so send each device a request asking
 		 * for just their contribution.
 		 */
 
 		//initialise array with empty lists
 		LinkedList<Integer>[] newRequestArray = new LinkedList[Session.getSession().numDevices()];
 		for(int i=0; i<Session.getSession().numDevices(); i++) {
 			newRequestArray[i] = new LinkedList<Integer>();
 		}
 
 		//clear all lists so only send one devices request:
 		for(int i=0; i<Session.getSession().numDevices(); i++) {
 			for(int j=0; j<Session.getSession().numDevices(); j++) {
 				newRequestArray[j].clear();
 			}
 			newRequestArray[i] = responses[i].remainingPoints;
 
 			//issue new request to culprit to get the remaining points.
 			Device toDevice = Session.getSession().getDevice(i);
 			InetSocketAddress sockAdd = new InetSocketAddress(((DeviceHandleIP) toDevice.getHandle()).getIP().getHostName(), ((DeviceHandleIP) toDevice.getHandle()).getPort());
 			try {
 				byte[] data = DataConnectionManager.createRequestMessageWithAddress(sockAdd, newRequestArray);
 				//DatagramPacket datagram = ServerMessage.createRequestMessageWithAddress(sockAdd, newRequestArray);
 				if(data != null) {
 					connections[toDevice.getDeviceID()].sendGeneric(data, data.length);
 					//NetworkInterface.getInstance().sendDatagram(datagram);
 				}
 			} catch (SocketException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	//note this doesnt wait before sending. Will respond as soon as it can.
 	private static synchronized void respondToNetwork(int fromID, List<Coords> response) {
 		coordsToSend[fromID].addAll(response);
 		sendCoordsInQueue();
 	}
 }
