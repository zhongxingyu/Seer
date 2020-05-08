 package main;
 
 
 import driver.AppManagerImpl;
 import driver.Sphero;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import listener.AliveListener;
 import se.nicklasgavelin.bluetooth.Bluetooth;
 import se.nicklasgavelin.bluetooth.Bluetooth.EVENT;
 import se.nicklasgavelin.bluetooth.BluetoothDevice;
 import se.nicklasgavelin.bluetooth.BluetoothDiscoveryListener;
 import se.nicklasgavelin.sphero.Robot;
import se.nicklasgavelin.sphero.command.SpinRightCommand;
 import utils.SpheroConfig;
 
 public class SpheroDriver extends Thread implements BluetoothDiscoveryListener {
 	private AppManagerImpl _appMgr = AppManagerImpl.getInstance();
 	private List<Sphero> _availableSpheroDevices;
 	private Bluetooth bt;
 	private SpheroConfig config;
 
 	public static void main( String[] args ) throws UnknownHostException, IOException {
 		SpheroDriver sd = new SpheroDriver();
 		sd.start();
 	}
 
 	public SpheroDriver() {
 	}
 
 	@Override
 	public void run() {
 		_availableSpheroDevices = new LinkedList<Sphero>();
 		
 		config = new SpheroConfig();
 		try {
 			String devID = config.load();
 			directConnect(devID);
 		} catch (Exception e) {
 			deviceSearch();
 		}		
 	}
 	
 	private void deviceSearch(){
 		bt = new Bluetooth( this, Bluetooth.SERIAL_COM );
 		bt.discover(); // # COMMENT THIS IF UNCOMMENTING THE BELOW AREA #		
 	}
 
 	private void addShutdownHook() {
 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				Thread k = new Thread(new Runnable() {
 
 					@Override
 					public void run() {
 						for (Sphero s : _availableSpheroDevices)
 							s.disconnect();
 					}
 				});
 
 				try {
 					k.join();
 				} catch (InterruptedException e) {
 				}
 			}
 		}));
 	}
 
 	private void registerApplication()
 	{
 		_appMgr.setupAppManager( _availableSpheroDevices, "DefaultApp" );
 	}
 
 	@Override
 	public void deviceSearchCompleted( Collection<BluetoothDevice> devices )
 	{
 		for( BluetoothDevice btd : devices )
 		{
 			if( btd.getAddress().startsWith( Robot.ROBOT_ADDRESS_PREFIX ) )
 			{
 				// Create robot
 				try
 				{
 					Sphero s = new Sphero( btd );
 					if( s.connect() )
 					{
 						// Connected
 						Logger.getLogger( this.getClass().getCanonicalName() ).log( Level.INFO, "Connected to Sphero device " + s.getId() + "(" + s.getAddress() + ")" );
 						_availableSpheroDevices.add( s );
 						config.save(s.getId());
 						
 						AliveListener aliveListener = new AliveListener(this);
 						s.addListener(aliveListener);
 						
						s.sendCommand(new SpinRightCommand(0),2000);
						
 						System.out.println( btd.getConnectionURL() );
 					} else {
 						throw new Exception("Sphero not connected");
 					}
 				}
 				catch( Exception e )
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 
 		if( _availableSpheroDevices.size() > 0 )
 			registerApplication();
 	}
 
 	@Override
 	public void deviceDiscovered( BluetoothDevice device ) {
 	}
 
 	@Override
 	public void deviceSearchFailed( EVENT error ) {
 		Logger.getLogger( this.getClass().getCanonicalName() ).log( Level.SEVERE, "Failed to perform device search " + error );
 	}
 
 	@Override
 	public void deviceSearchStarted() {
 		Logger.getLogger( this.getClass().getCanonicalName() ).log( Level.INFO, "Starting device search" );
 	}
 	
 	public void directConnect(String id) throws Exception {
 		Bluetooth bt = new Bluetooth( this, Bluetooth.SERIAL_COM );
 		BluetoothDevice btd = new BluetoothDevice( bt, "btspp://" + id + ":1;authenticate=true;encrypt=false;master=false" );
 					
 		if( btd.getAddress().startsWith( Robot.ROBOT_ADDRESS_PREFIX ) ) {
 			 //Create robot
 				Sphero s = new Sphero( btd );
 				if( s.connect() ) {
 					
 					// Connected
 					Logger.getLogger( this.getClass().getCanonicalName() ).log( Level.INFO, "Connected to Sphero device " + s.getId() + "(" + s.getAddress() + ")" );
 
 					_availableSpheroDevices.add( s );
 					
 					if( _availableSpheroDevices.size() > 0 ) {
 						registerApplication();
 					}
 					
 					AliveListener aliveListener = new AliveListener(this);
 					s.addListener(aliveListener);
 					
					s.sendCommand(new SpinRightCommand(0),2000);
					
 					addShutdownHook();
 				} else {
 					throw new Exception("Sphero not connected");
 				}
 		}
 	}
 	
 	public void restartConnection(Sphero oldSphero){
 		String devID = oldSphero.getId();
 		String msg = "Connection to sphero "+devID+" is lost and trying to restart.";
 		Logger.getLogger( this.getClass().getName() ).log( Level.WARNING, msg );
 		
 		boolean connectedSphero = false;
 		while(!connectedSphero){
 			try {
 				directConnect(devID);
 				connectedSphero = true;
 				_availableSpheroDevices.remove(oldSphero); // Prevent registerapp to run again in directconnect
 				
 				if(oldSphero.active) { // If it got eventreporting activated
 					for (Sphero sphero : _availableSpheroDevices) {
 						if(devID.equals(sphero.getId())) {
 							sphero.activateEvents(oldSphero.events); // Reactivate old events
 							break;
 						}
 					}
 				}				
 			} catch (Exception e) {			
 			}
 		}
 	}
 	
 	private static void pause(int ms) {
 		try {
 			Thread.sleep( ms );
 		} catch( Exception e )
 		{
 			// Failure in searching for devices for some reason.
 			e.printStackTrace();
 		}
 	}
 }
