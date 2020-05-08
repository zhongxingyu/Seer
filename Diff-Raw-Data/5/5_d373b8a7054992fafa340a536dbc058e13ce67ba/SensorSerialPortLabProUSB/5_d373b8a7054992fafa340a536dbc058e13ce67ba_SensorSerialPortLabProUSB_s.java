 package org.concord.sensor.vernier.labpro;
 
 import java.io.IOException;
 
 import org.concord.sensor.impl.Vector;
 import org.concord.sensor.labprousb.jna.LabProUSB;
 import org.concord.sensor.labprousb.jna.LabProUSBException;
 import org.concord.sensor.labprousb.jna.LabProUSBLibrary;
 import org.concord.sensor.serial.SensorSerialPort;
 import org.concord.sensor.serial.SerialException;
 
 public class SensorSerialPortLabProUSB implements SensorSerialPort 
 {
 	byte [] tmpBuffer = new byte [2048];
 
 	private static LabProUSB lpusb;
 	
 	static {
 		Runtime.getRuntime().addShutdownHook(new Thread(){
 			public void run() {
 				if (lpusb != null) {
 					System.err.println("Closing LabProUSB.  Its open state is: " + lpusb.isOpen());
 					
 					// Make sure the labpro is closed
 					lpusb.close();
 				}
 			}
 		});
 	}
 	
 	public SensorSerialPortLabProUSB() {
 		
 	}
 	
 	public void close() throws SerialException 
 	{
 		lpusb.close();
 	}
 
 	public void disableReceiveTimeout() 
 	{
 		// there are no timeouts
 	}
 
 	public void enableReceiveTimeout(int time) throws SerialException 
 	{
 		// this isn't supported
 	}
 
 	public Vector getAvailablePorts() 
 	{		
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public int getBaudRate() 
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public int getDataBits() 
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public int getParity() 
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public int getStopBits() 
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public boolean isOpen() 
 	{
 		short open = lpusb.isOpen();
 		return open == 1;
 	}
 
 	public void open(String portName) throws SerialException 
 	{	
 		try {
 			LabProUSBLibrary lplib = new LabProUSBLibrary();
 	    	lplib.init();
 	    	lpusb = lplib.openDevice();
 		} catch (LabProUSBException e) {
 			e.printStackTrace();
 			throw new SerialException("Unable to open device");
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new SerialException("Unable to initialize native library");
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			throw new SerialException("Unable to initialize native library (interrupted)");
 		} catch (UnsatisfiedLinkError e){
 			e.printStackTrace();
 			throw new SerialException("Can't load labprousb library", e);
 		}
 	}
 
 	/**
 	 * This is not thread safe.
 	 */
 	public int readBytes(byte[] buf, int off, int len, long timeout)
 			throws SerialException 
 	{
 		int size = 0;	    
 	    long startTime = System.currentTimeMillis();
 	    while(size != -1 && size < len &&
 	            (System.currentTimeMillis() - startTime) < timeout){
 	    	
 	    	long availableBytes = lpusb.getAvailableBytes();
 	    	if(availableBytes > 0){
 	    		int numRead = (int) lpusb.readBytes(availableBytes, tmpBuffer);
 		        if(numRead < 0) {	      
 		            System.err.println();
 		            System.err.println("error in readBytes: " + numRead);
 		            
 		            return numRead;
 		        }
 	    		
 				System.arraycopy(tmpBuffer, 0, buf, size+off, numRead);	    		
 	    		size += numRead;
 	    	} 
 	    	
 	    	try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    }
 	    
 	    return size;	
 	}
 
 	public void setFlowControlMode(int flowcontrol) throws SerialException {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setSerialPortParams(int baud, int data, int stop, int parity)
 			throws SerialException {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void write(int value) throws SerialException 
 	{
 		tmpBuffer[0] = (byte)value;
 		write(tmpBuffer, 0, 1);
 	}
 
 	public void write(byte[] buffer) throws SerialException 
 	{
 		write(buffer, 0, buffer.length);
 	}
 
 	public void write(byte[] buffer, int start, int length)
 			throws SerialException 
 	{
 		byte [] bufToWrite = buffer;
 		if(start != 0){
 			System.arraycopy(buffer, start, tmpBuffer, 0, length);
 			bufToWrite = tmpBuffer;
 		}
 		
 		short numWritten = lpusb.writeBytes((short)length, bufToWrite);
 		if(numWritten < length){			
 			throw new SerialException("Didn't write all bytes. Wrote: " + numWritten +
 					" out of: " + length);
 		}		
 	}
 
 	/**
 	 * This port can close and open quickly.
 	 * 
 	 */
 	public boolean isOpenFast() 
 	{
 		return true;
 	}
 }
