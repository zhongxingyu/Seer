 package io;
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.PortInUseException;
 import gnu.io.SerialPort;
 import gnu.io.UnsupportedCommOperationException;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Properties;
 
 import javax.print.attribute.standard.OutputDeviceAssigned;
 
 import device.DeviceCode;
 import device.DeviceRadio;
 import device.LEDcolor;
 import device.OutputLED;
 import device.OutputOutlet;
 import device.Device;
 import device.DeviceLED;
 import device.OutputRadio;
 
 public class ConnectionManager {
 
 	HashMap<String,Integer> idBaudMap;
 	
 	DeviceRadio outputRadioDevice;
 
 	public DeviceRadio getOutputRadioDevice() {
 		return outputRadioDevice;
 	}
 	LinkedList<OutputLED> outputLEDList;
 	public LinkedList<OutputLED> getOutputLEDList() {
 		return outputLEDList;
 	}
 	public OutputLED getOutputLED(int id) {
 		for(OutputLED outputLED : outputLEDList)
 			if(outputLED.getID()==id)
 				return outputLED;
 		return null;
 	}
 	LinkedList<OutputOutlet> outputOutletList;
 	public LinkedList<OutputOutlet> getOutputOutletList() {
 		return outputOutletList;
 	}
 	private LinkedList<Device> allDevices;
 	
 	private static final int DEFAULT_DATA_RATE = 115200;
 	public static int getDataRate() {
 		return DEFAULT_DATA_RATE;
 	}
 
 	public ConnectionManager() {
 		outputLEDList = new LinkedList<OutputLED>();
 		outputOutletList = new LinkedList<OutputOutlet>();
 		allDevices = new LinkedList<Device>();
 		
 		idBaudMap = new HashMap<String,Integer>();
 		
 //		Properties q = new Properties();
 //		q.setProperty("/dev/blabla", ""+ 19200);
 //		try {
 //			q.store(new FileOutputStream("config.properties"), null);
 //		} catch (FileNotFoundException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		} catch (IOException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		}
 			
 		//Read property file
 		Properties p = new Properties();
 		try {
 			p.load(new FileInputStream("config.properties"));
 			for(Enumeration<?> e = p.propertyNames(); e.hasMoreElements();){
 				Object o = e.nextElement();
 				idBaudMap.put(o.toString(), Integer.parseInt(p.get(o).toString()));
 				//System.out.println("key: " + o.toString() + " value: " + idBaudMap.get(o.toString())  );
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Connects all devices and creates the socket connection
 	 * @return returns true if at least one device is connected 
 	 */
 	public boolean connectSerialDevices() {
 		Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
 		while (portEnum.hasMoreElements()) {
 			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();	
 			System.out.print(currPortId.getName()+": ");
 			if (currPortId.getName().contains("tty") && !currPortId.getName().contains("ttyS0")) {
 				System.out.println("try connect...");
 				Device sd;
 				if(idBaudMap.containsKey(currPortId.getName())){
 					sd = new Device(currPortId.getName(),idBaudMap.get(currPortId.getName()));
 				}
 //				if(currPortId.getName().equals("/dev/tty.usbserial-FTTPRBJH")){ //TODO: remove quick hack
 //					 sd = new Device(currPortId.getName(),19200);
 //				}
 				else{
 					 sd = new Device(currPortId.getName(),DEFAULT_DATA_RATE);
 				}			
 				String s;
 				try {
 					s = sd.connect();
 					if(s != null && !s.isEmpty()) {
 						if(!parseInput(s,sd)){
 							sd.close();
 						}
 					}
 					
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}
 			else{
 				System.out.println("skipped.");
 			}
 		}
 		
 		return (allDevices.size()>0);
 	}
 	
 	/**
 	 * This method matches an input string to a known device
 	 * @param str
 	 * @param sp
 	 * @throws IOException
 	 */
 	private boolean parseInput(String str, Device sp) throws IOException{
 		System.out.println("   input string: " + str );
 		if(str.equals("LED-1-DCODE")){
 			System.out.println("   found DCODE device");
 			DeviceLED led = new DeviceLED(sp,DeviceCode.D_CODE);
 			OutputLED o = new OutputLED(led, 1, LEDcolor.RGB);
 			outputLEDList.add(o);
 			outputOutletList.add(o);
 			allDevices.add(led);
 			return true;
 		}else if(str.contains("TCODE")){
 			System.out.println("   found TCODE device");
 			DeviceLED led = new DeviceLED(sp,DeviceCode.T_CODE);
 			allDevices.add(led);
 			String[] outputs=str.split("-")[1].split(";");
 			for(String output : outputs){
 				int id=Integer.parseInt(output.substring(3,4));
 				String type=output.substring(5);
 				if(type.equals("RGB")){
 					OutputLED o = new OutputLED(led, id, LEDcolor.RGB);
 					outputLEDList.add(o);	
 					outputOutletList.add(o);
 				}		
 				if(type.equals("WHITE")){
 					OutputLED o = new OutputLED(led, id, LEDcolor.WHITE);
 					outputLEDList.add(o);	
 					outputOutletList.add(o);
 				}
 					
 			}
 			return true;
 		}else if(str.equals("radio")){
 			System.out.println("   found RADIO device");
 			outputRadioDevice = new DeviceRadio(sp);
 			allDevices.add(outputRadioDevice);
 			
 			OutputRadio r = new OutputRadio(outputRadioDevice,"test","1000"); //
 			
 			outputOutletList.add( r);
 			return true;
 		}
 		return false;
 	}
 
 	public LinkedList<Device> getAllDevices() {
 		return allDevices;
 	}
 	public void closeAllDevices(){
 		for(Device sd : allDevices){
 			sd.close();
 		}
 	}
 	
 	public void printResult(){
 		System.out.println("...finished." + "\n RESULT: \n LEDOutputs: "+getOutputLEDList().size() + 
 				"\n RadioDevice: " + (getOutputRadioDevice() != null) +
				"\n TOTAL OUTPUTS: " +getOutputOutletList().size()+ 
				"\n TOTAL DEVICES: " + getAllDevices().size());
 	}
 
 
 
 	
 
 }
