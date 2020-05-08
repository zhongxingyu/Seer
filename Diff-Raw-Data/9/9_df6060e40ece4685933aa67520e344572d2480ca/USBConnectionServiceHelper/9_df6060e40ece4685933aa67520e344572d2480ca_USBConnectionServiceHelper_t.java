 package de.uniulm.bagception.rfidapi.connection;
 
import de.uniulm.bagception.service.USBConnectionService;
import de.uniulm.bagception.services.ServiceNames;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class USBConnectionServiceHelper {
 	
 	private final USBConnectionServiceCallback callback;
 	private final Context context;
 	public USBConnectionServiceHelper(Context c,USBConnectionServiceCallback callback) {
 		this.callback=callback;
 		this.context=c;
 	}
 	
 	
 	private final ServiceConnection usbConnectionService = new ServiceConnection() {
 	
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 		}
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
			USBConnectionService remoteStubService=USBConnectionService.Stub.asInterface(service);
 			try {
 				callback.onUSBConnected(remoteStubService.isConnected());
 			} catch (RemoteException e) {
 				callback.onUSBConnectionError(e);
 				e.printStackTrace();
 			} finally{
 				context.unbindService(usbConnectionService);
 			}
 			
 
 		}
 	};
 	
 	public void checkUSBConnection(){
 		//final String servicename = "de.uniulm.bagception.service.USBConnectionServiceRemote";
		final String servicename=ServiceNames.RFID_SERVICE;
 		if (!context.bindService(new Intent(servicename),
 				usbConnectionService, Context.BIND_AUTO_CREATE)){
 			Log.d("Service","error binding to service: "+servicename);
 			callback.onUSBConnectionError(new Exception("error binding to connection service"));
 		}
 
 		
 	}
 }
