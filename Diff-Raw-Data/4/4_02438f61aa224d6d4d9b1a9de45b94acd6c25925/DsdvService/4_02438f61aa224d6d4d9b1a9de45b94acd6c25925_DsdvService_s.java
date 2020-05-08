 package de.htw;
 
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import de.uni_trier.jane.basetypes.Address;
 import de.uni_trier.jane.basetypes.ServiceID;
 import de.uni_trier.jane.service.EndpointClassID;
 import de.uni_trier.jane.service.RuntimeService;
 import de.uni_trier.jane.service.neighbor_discovery.NeighborDiscoveryData;
 import de.uni_trier.jane.service.neighbor_discovery.NeighborDiscoveryListener;
 import de.uni_trier.jane.service.neighbor_discovery.NeighborDiscoveryService;
 import de.uni_trier.jane.service.neighbor_discovery.NeighborDiscoveryService_sync;
 import de.uni_trier.jane.service.network.link_layer.LinkLayer_async;
 import de.uni_trier.jane.service.operatingSystem.RuntimeOperatingSystem;
 import de.uni_trier.jane.service.parameter.todo.Parameters;
 import de.uni_trier.jane.visualization.shapes.Shape;
 
 
 
 public class DsdvService implements RuntimeService, NeighborDiscoveryListener {
 
 	public static ServiceID serviceID;
 	private ServiceID linkLayerID;
 	private ServiceID neighborID;
 	private Address deviceId;
 	
 	
 	private LinkLayer_async linkLayer;
 	private NeighborDiscoveryService_sync neighborService;
 	private RuntimeOperatingSystem runtimeOperatingSystem;
 	
 	private RoutingTable table = new RoutingTable();
 	
 	public DsdvService(ServiceID linkLayerID, ServiceID neighborID) {
 		super();
 		
 		serviceID = new EndpointClassID(DsdvService.class.getName());
 		
 		this.linkLayerID = linkLayerID;
 		this.neighborID = neighborID;
 		
 		//beacon = new BeaconContent();
 	}	
 	
 	@Override
 	public ServiceID getServiceID() {
 		return serviceID;
 	}
 
 	@Override
 	public void finish() {
 
 	}
 
 	@Override
 	public Shape getShape() {
 		return null;
 	}
 
 	@Override
 	public void getParameters(Parameters parameters) {
 	}
 
 
 
 	@Override
 	public void setNeighborData(NeighborDiscoveryData neighborData) {
 //		Address neighborAddress = neighborData.getSender();
 //		table.put(neighborAddress, neighborAddress, 1, 2); //TODO seqNum really == 2 ???
 		table.incSeqNum(deviceId, 2);
 		RoutingTable tableCopy = table.copy();
		table.setAllNextHop(deviceId);
		table.incAllDistanceToDestination();
 		RouteTableMessage msg = new RouteTableMessage(tableCopy.getMap());
 		linkLayer.sendBroadcast(msg);
 	}
 
 	@Override
 	public void updateNeighborData(NeighborDiscoveryData neighborData) {
 	}
 
 	@Override
 	public void removeNeighborData(Address linkLayerAddress) { //TODO check
 		table.incSeqNum(linkLayerAddress, 1);
 		table.setDistanceToDestinationToInfinity(linkLayerAddress);
 		RoutingTable tableCopy = table.copy();
 		RouteTableMessage msg = new RouteTableMessage(tableCopy.getMap());
 		linkLayer.sendBroadcast(msg);
 	}
 
 	@Override
 	public void start(RuntimeOperatingSystem runtimeOperatingSystem) {
 		this.runtimeOperatingSystem = runtimeOperatingSystem;
 		this.deviceId = runtimeOperatingSystem.getDeviceID();
 		
 		//Am LinkLayer registrieren, um diesen aus TestService heraus nutzen zu knnen
 		linkLayer=(LinkLayer_async)runtimeOperatingSystem.getSignalListenerStub(linkLayerID, LinkLayer_async.class);
 				
 		runtimeOperatingSystem.registerAtService(linkLayerID, LinkLayer_async.class);
 				
 		//Am Nachbarschaftsservice registrieren, um diesen aus TestService heraus nutzen zu knnen
 		neighborService = (NeighborDiscoveryService_sync)runtimeOperatingSystem.getSignalListenerStub(neighborID, NeighborDiscoveryService_sync.class);	
 		runtimeOperatingSystem.registerAtService(neighborID, NeighborDiscoveryService.class);
 		
 		table.put(deviceId, deviceId, 0, 0);
 	}
 	
 
 	public void handleMessage(Address sender, HashMap<Address, DeviceRouteData> routeTable) {
 		if(sender.toString().equals(deviceId.toString())){
 			return ;
 		}
 		System.out.println("ownTable: " + table);
 		System.out.println("otherTable: " + routeTable);
 		boolean merge = table.merge(routeTable);
 		boolean isOwnReachabilityCorrect = table.isOwnReachabilityCorrect(routeTable, deviceId);
 		System.out.println("merge: " + merge + "  isOwn: " + isOwnReachabilityCorrect);
 		if (merge || !isOwnReachabilityCorrect){
 			RouteTableMessage msg = new RouteTableMessage(table.getMap());
 			linkLayer.sendBroadcast(msg);
 		}
 		System.out.println("Table " + deviceId);
 		System.out.println(table);
 	}
 }
