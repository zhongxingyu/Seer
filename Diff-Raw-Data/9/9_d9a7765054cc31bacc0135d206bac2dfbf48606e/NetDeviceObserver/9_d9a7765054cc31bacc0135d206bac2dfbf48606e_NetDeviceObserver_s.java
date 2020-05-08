 package edu.wsn.phoneusage.main;
 
 import java.util.List;
 import java.util.Vector;
 
 import edu.wsn.phoneusage.db.DataElement;
 import edu.wsn.phoneusage.device.Bluetooth;
 import edu.wsn.phoneusage.device.Bluetooth.BluetoothData;
 import edu.wsn.phoneusage.device.DeviceCalculator;
 import edu.wsn.phoneusage.device.DeviceComponent;
 import edu.wsn.phoneusage.device.DeviceData;
 import edu.wsn.phoneusage.device.IterationData;
 import edu.wsn.phoneusage.device.ThreeG;
 import edu.wsn.phoneusage.device.Wifi;
 import edu.wsn.phoneusage.device.ThreeG.ThreeGData;
 import edu.wsn.phoneusage.device.Wifi.WifiData;
 
 import android.content.Context;
 import android.util.SparseArray;
 
 /**
  * content of the net device observer thread for  wifi, 3g, 
  * bluetooth observation - devices information not possible
  * to be obtained directly by the API 
  * 
  * @author Lothar Rubusch
  */
 public class NetDeviceObserver implements Runnable{
 //	private static final String TAG = "NetDeviceObserver";
 
 	public static final int ALL_COMPONENTS = -1;
 	public HiddenService context;
 	private Vector<DeviceComponent> deviceComponents;
 	private Vector<DeviceFunction> powerFunctions;
 
 	public NetDeviceObserver(HiddenService context){
 		this.context = context;
 		deviceComponents = new Vector<DeviceComponent>();
 		powerFunctions = new Vector<DeviceFunction>();
 		generateComponents(context, deviceComponents, powerFunctions);
 	}
 
 	public void generateComponents(
 			Context context
 			, List<DeviceComponent> components
 			, List<DeviceFunction> functions ){
 		final PhoneConstants constants = new PhoneConstants(context);
 		final DeviceCalculator calculator = new DeviceCalculator(context);
 
 
 		// Wifi component
 		String wifiInterface = SystemInfo.getInstance().getProperty("wifi.interface");
 		if( (null != wifiInterface) && (0 != wifiInterface.length()) ){
 			components.add( new Wifi(context, constants) );
 			functions.add( new DeviceFunction(){
 				public double calculate( DeviceData data ){
 					return calculator.getWifiPower( (WifiData) data );
 				}} );
 		}
 
 		// 3G component
 		if( 0 != constants.threegInterface().length() ){
 			components.add( new ThreeG(context, constants) );
 			functions.add( new DeviceFunction(){
 				public double calculate( DeviceData data ){
 					return calculator.getThreeGPower( (ThreeGData) data );
 				}} );
 		}
 
 		// Bluetooth component
 		String bluetoothInterface = SystemInfo.getInstance().getProperty("bluetooth.interface");
 		if( (null != bluetoothInterface) && (0 != bluetoothInterface.length()) ){
 			components.add( new Bluetooth(context, constants) );
 			functions.add( new DeviceFunction(){
 				public double calculate( DeviceData data ){
 					return calculator.getBluetoothPower( (BluetoothData) data );
 				}} );
 		}
 	}
 
 // TODO separate
 	private void probe_wifi( IterationData dev ){
 		int totalPower = 0;
 		DeviceComponent comp = deviceComponents.get(0);
 		if( null != (dev = comp.getData()) ){
 			SparseArray<DeviceData> uidPower = dev.getUidDeviceData();
 			for(int j = 0; j < uidPower.size(); j++) {
 				int uid = uidPower.keyAt(j);
 				DeviceData powerData = uidPower.valueAt(j);
 				int power = (int) powerFunctions.get(0).calculate(powerData);
 				if(uid == SystemInfo.AID_ALL) {
 					totalPower += power;
 				}
 			}
 			if( uidPower.size() > 0){
 //				DataElement_wifi element = new DataElement_wifi( totalPower / uidPower.size() );
				DataElement element = new DataElement();
 				element.setValue("KEY_VALUE", String.valueOf( totalPower / uidPower.size()));
 				for( int idx=0; idx < MainActivity.DB_list.size(); ++idx){
 // TODO refactor generalized access to DB_list...wifi
 					if( MainActivity.DB_list.get(idx).tablename() == SystemInfo.DB_TABLENAME_WIFI ){
 						MainActivity.DB_list.get(idx).data_save(element);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	private void probe_threeg( IterationData dev ){
 		int totalPower = 0;
 		DeviceComponent comp = deviceComponents.get(1);
 		if( null != (dev = comp.getData()) ){
 			SparseArray<DeviceData> uidPower = dev.getUidDeviceData();
 			for(int j = 0; j < uidPower.size(); j++) {
 				int uid = uidPower.keyAt(j);
 				DeviceData powerData = uidPower.valueAt(j);
 				int power = (int) powerFunctions.get(1).calculate(powerData);
 				if(uid == SystemInfo.AID_ALL) {
 					totalPower += power;
 				}
 			}
 			if( uidPower.size() > 0 ){
 //				DataElement_threeg element = new DataElement_threeg( totalPower / uidPower.size() );
 				DataElement element = new DataElement();
 				element.setValue( "KEY_VALUE", String.valueOf( totalPower / uidPower.size()));
 
 // TODO refactor, generic access to the tablename threeg in DB_list
 				for( int idx=0; idx < MainActivity.DB_list.size(); ++idx){
 					if( MainActivity.DB_list.get(idx).tablename() == SystemInfo.DB_TABLENAME_THREEG ){
 						MainActivity.DB_list.get(idx).data_save(element);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	private void probe_bluetooth( IterationData dev ){
 		int totalPower = 0;
 		DeviceComponent comp = deviceComponents.get(0);
 		if( null != (dev = comp.getData()) ){
 			SparseArray<DeviceData> uidPower = dev.getUidDeviceData();
 			for(int j = 0; j < uidPower.size(); j++) {
 				int uid = uidPower.keyAt(j);
 				DeviceData powerData = uidPower.valueAt(j);
 				int power = (int) powerFunctions.get(0).calculate(powerData);
 				if(uid == SystemInfo.AID_ALL) {
 					totalPower += power;
 				}
 			}
 			if( uidPower.size() > 0){
 //				DataElement_bluetooth element = new DataElement_bluetooth( totalPower / uidPower.size() );
 				DataElement element = new DataElement();
 				element.setValue( "KEY_VALUE", String.valueOf( totalPower / uidPower.size()));
 
 // TODO refactor the DB_list access for tablename bluetooth
 				for( int idx=0; idx < MainActivity.DB_list.size(); ++idx ){
 					if( MainActivity.DB_list.get(idx).tablename() == SystemInfo.DB_TABLENAME_BLUETOOTH ){
 						MainActivity.DB_list.get(idx).data_save(element);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 
 	/*
 	 * This is the loop that keeps updating the power profile
 	 */
 	public void run() {
 		int numberOfComponents = deviceComponents.size();
 		for(int i = 0; i < numberOfComponents; ++i){
 			deviceComponents.get(i).init( SystemInfo.PERIOD_NET_DEVICE );
 			deviceComponents.get(i).start();
 		}
 		IterationData[] dataTemp = new IterationData[numberOfComponents];
 		while( !Thread.interrupted() ){
 
 			try {
 				Thread.currentThread();
 				Thread.sleep( SystemInfo.PERIOD_NET_DEVICE );
 			} catch(InterruptedException e) {
 				break;
 			}
 
 			for(int i=0; i<dataTemp.length; ++i){
 /*
 				probe( dataTemp[i] );
 /*/
 // TODO refac
 				// wifi
 				probe_wifi( dataTemp[0] );
 
 				// threeg
 				probe_threeg( dataTemp[1] );
 
 				// bluetooth
 				probe_bluetooth( dataTemp[2] );
 //*/
 			}
 		}
 
 		// in case, join components thread and stop them
 		for(int i = 0; i < numberOfComponents; i++) {
 			deviceComponents.get(i).interrupt();
 		}
 
 		for(int i = 0; i < numberOfComponents; i++) {
 			try {
 				deviceComponents.get(i).join();
 			} catch(InterruptedException e) {
 			}
 		}
 	}
 
 	public String[] getComponents() {
 		int components = deviceComponents.size();
 		String[] ret = new String[components];
 		for(int i = 0; i < components; i++) {
 			ret[i] = deviceComponents.get(i).getComponentName();
 		}
 		return ret;
 	}
 
 	public int[] getComponentsMaxPower() {
 		PhoneConstants constants = new PhoneConstants(context);
 		int components = deviceComponents.size();
 		int[] ret = new int[components];
 		for(int i = 0; i < components; i++) {
 			ret[i] = (int)constants.getMaxPower(
 					deviceComponents.get(i).getComponentName());
 		}
 		return ret;
 	}
 
 	public int getNoUidMask() {
 		int components = deviceComponents.size();
 		int ret = 0;
 		for(int i = 0; i < components; i++) {
 			if(!deviceComponents.get(i).hasUidInformation()) {
 				ret |= 1 << i;
 			}
 		}
 		return ret;
 	}
 
 	public interface DeviceFunction {
 		public double calculate( DeviceData data);
 	}
 }
 
