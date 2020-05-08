 package fr.imag.adele.apam.application.icasa.adaptor;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Instantiate;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 
 import fr.imag.adele.apam.Apam;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.liglab.adele.icasa.location.LocatedDevice;
 import fr.liglab.adele.icasa.location.Position;
 import fr.liglab.adele.icasa.location.Zone;
 import fr.liglab.adele.icasa.location.ZoneListener;
 import fr.liglab.adele.icasa.simulator.SimulationManager;
 
 
 /**
  * The zone manager listen to zone creation events in the iCasa simulation environment,
  * and automatically instantiates the corresponding APAM representation of a room
  * 
  * @author vega
  *
  */
 @Component
 @Provides(specifications = ZoneListener.class)
 
 @Instantiate
 public class ZoneManager implements ZoneListener {
 
 	@Requires(proxy = false)
 	private SimulationManager manager;
 	
 	@SuppressWarnings("unused")
 	@Requires(proxy = false)
 	private Apam apam;
 	
 	
     /**
      * The event executor. We use a pool of a threads to handle notification
      * to APAM of underlying platform events, without blocking the platform thread.
      */
     private final Executor dispatcher = Executors.newCachedThreadPool();
     
 	/**
 	 * The list of iCasa zones currently represented in Apam
 	 */
	private final Map<Zone,Instance> zones;
     
 	/**
 	 * A request to be processed by the dispatcher
 	 */
 	private abstract class Request implements Runnable {
 		
 		protected final Zone 	zone;
 		
 		public Request(Zone zone) {
 			this.zone = zone;
 		}
 		
 	}
 	
 	/**
 	 * A request to add a new zone
 	 */
 	private class AddRequest extends Request {
 		
 		public AddRequest(Zone zone) {
 			super(zone);
 		}
 		
 		@Override
 		public void run() {
 			
			synchronized (zones) {
 				
 				System.out.println("------- zone added");
 				
 				/*
 				 * Verify if already known in APAM, otherwise create a new instance
 				 */
				Instance apamZone = zones.get(zone);
 				
				if (apamZone != null)
 					return;
 				
 			
				Implementation zoneType	= CST.apamResolver.resolveSpecByName(null,"Zone",null,null);
				apamZone				= zoneType.createInstance(null, Collections.singletonMap("location",zone.getId()));
 			
				if (apamZone == null) {
 					System.out.println("----Impossible to create zone");
 					return;
 				}
 			
				zones.put(zone,apamZone);
 			
				System.out.println("---- zone created");
 			
 			}
 		}
 	}
 	
 	private class RemoveRequest extends Request {
 		
 		public RemoveRequest(Zone zone) {
 			super(zone);
 		}
 		
 		@Override
 		public void run() {
 			
			synchronized (zones) {
 
 				@SuppressWarnings("unused")
				Instance room = zones.remove(zone);
 				
 				/*
 				 * TODO dispose Apam instance
 				 */
 				System.out.println("------- zone removed");
 			}
 		}
 	}
 	public ZoneManager() {
		zones	= new HashMap<Zone, Instance>();
 		started	= false;
 	}
 
 	private boolean started;
 
 	@Validate
 	public void start(){
 		
 		synchronized (this) {
 			started = true;
 		}
 		
 		manager.addListener(this);
 		for (Zone zone : manager.getZones()) {
 			zoneAdded(zone);
 		}
 	}
 	
 	@Invalidate
 	public void stop(){
 		manager.removeListener(this);
 		for (Zone zone : manager.getZones()) {
 			zoneRemoved(zone);
 		}
 		
 		synchronized (this) {
 			started = false;
 		}
 		
 	}
 	
 	private void execute(Request request) {
 		
 		/*
 		 * ignore events while stopped
 		 */
 		synchronized (this) {
 			if (! started)
 				return;
 		}
 		
 		dispatcher.execute(request);
 	}
 	
 	@Override
 	public void zoneAdded(Zone zone) {
 		execute(this.new AddRequest(zone));
 	}
 	
 	@Override
 	public void zoneRemoved(Zone zone) {
 		execute(this.new RemoveRequest(zone));
 	}
 	
 	@Override
 	public void zoneVariableAdded(Zone zone, String variable) {
 	}
 
 	@Override
 	public void zoneVariableRemoved(Zone zone, String variable) {
 	}
 
 	@Override
 	public void zoneVariableModified(Zone zone, String variable, Object value, Object oldValue) {
 	}
 
 	@Override
 	public void deviceAttached(Zone zone, LocatedDevice device) {
 	}
 
 	@Override
 	public void deviceDetached(Zone zone, LocatedDevice device) {
 	}
 
 	@Override
 	public void zoneMoved(Zone zone, Position position, Position  oldPosition) {
 	}
 
 	@Override
 	public void zoneParentModified(Zone zone, Zone parent, Zone formerParent) {
 	}
 	
 	@Override
 	public void zoneResized(Zone zone) {
 	}
 
 	
 }
