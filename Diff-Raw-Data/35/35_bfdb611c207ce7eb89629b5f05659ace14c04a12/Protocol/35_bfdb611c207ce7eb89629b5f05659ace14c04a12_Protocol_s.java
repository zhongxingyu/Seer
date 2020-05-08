 /*
 * Copyright 2012 Anders Eie, Henrik Goldsack, Johan Jansen, Asbj�rn 
 * Lucassen, Emanuele Di Santo, Jonas Svarvaa, Bj�rnar H�kenstad Wold
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
 
 package no.ntnu.osnap.com;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeoutException;
 
 //import android.util.Log;
 
 /**
  * This class defines a communication standard with a remote device. The actual
  * communication method is defined in the subclass that extends the Protocol class.
  * For example a BluetoothConnection sends data through bluetooth sockets but this
  * is transparent to the user because communication is done through the Protocol 
  * standard. This class can be extended to support new communication methods like
  * InfraRed, NFC or WiFi.
  */
 public abstract class Protocol implements Runnable {
 	/**
 	 * The version number of this ComLib release
 	 */
 	public final static String LIBRARY_VERSION = "1.4.1";
 	
 	/**
 	 * Private mutex flag for atomic methods
 	 */
 	private volatile boolean locked = false;
 
 	/**
 	 * Private mutex flag to determine if there is an active connection
 	 */
 	private volatile boolean activeConnection = false;
 
 	/**
 	 * The unique metadata package for this connection
 	 * This is initially set to null and need to be retrieved
 	 * in getConnectionData() that is inherited by the connection
 	 * implementation.
 	 */
 	protected ConnectionMetadata connectionMetadata;
 	
 	private final BlockingQueue<ProtocolInstruction> pendingInstructions;
 	private ProtocolInstruction currentInstruction;
     
 	/**
 	 * Number of miliseconds to wait for a response before throwing a TimeoutException
 	 */
 	protected static final int TIMEOUT = 2000;
 	protected static final int MAX_CONTENT_SIZE = 250;
 
 	/**
 	 * Package private enumeration for all Commands supported by the Protocol standard
 	 *
 	 */
     enum OpCode {
     	PING,			//0
     	TEXT,			//1
     	SENSOR,			//2
     	DATA,			//3
     	PIN_R,			//4
     	PIN_W,			//5
     	DEVICE_INFO,	//6
     	RESPONSE(0xFE),	//254
     	RESET(0xFF);	//255
     	
     	//Read only value
     	public final byte value;
     	
     	/** Implicit value constructor */
     	private OpCode(){
     		this.value = (byte) this.ordinal();
     	}
     	
     	/** Explicit value constructor */
     	private OpCode(int value) {
     		this.value = (byte) value;
     	}
     }
     
     private Command currentCommand;
     private OpCode waitingForAck;
     
     private static final byte[] ackProcessors = {
         OpCode.SENSOR.value,
 		OpCode.PIN_R.value,
 		OpCode.PING.value
     };
 	
 	private OpCode tempAckProcessor;
 
 	/**
 	 * Default constructor called by sub-classes
 	 */
     public Protocol() {
         currentCommand = new Command();
         waitingForAck = null;
 		tempAckProcessor = null;
 		pendingInstructions = new LinkedBlockingQueue<ProtocolInstruction>();
     }
 	
     /**
      * Retrieves the ConnectionMetadata associated with the remote device. This
      * is normally stored internally on the remote device. Implementation of this 
      * method should be defined by the sub-class
      */
     public abstract ConnectionMetadata getConnectionData();
     
     /**
      * Establishes connection to the remote device
      */
     public abstract void connect();
     
     /**
      * Shuts down the connection and resets every data field so that
      * connect can be called again
      */
     public void disconnect()
     {
     	activeConnection = false;
 		lock();
 		
 		//Flush any pending instructions
 		synchronized (pendingInstructions) {
 			pendingInstructions.clear();
 		}
 		
 		//Reset states
         currentCommand = new Command();
         waitingForAck = null;
 		tempAckProcessor = null;
 		release();
     }
         
     /**
      * Retrieves a single long String of raw unprocessed list of services, platforms and download links supported by the remote device
      * @return a raw String representation of the device info retrieved from the remote device
      * @throws TimeoutException if the remote device used longer than Protocol.TIMEOUT milliseconds to respond
      */
 	protected final String getDeviceInfo() throws TimeoutException {
 		ProtocolInstruction newInstruction = new ProtocolInstruction(OpCode.DEVICE_INFO, (byte)0, new byte[1]);
 		
 		// Blocking methodlock();
 		lock();
 
 		waitingForAck = OpCode.DEVICE_INFO;
 		tempAckProcessor = OpCode.DEVICE_INFO;
 
 		try {
 			sendBytes(newInstruction.getInstructionBytes());
 		} catch (IOException ex) {
 			//System.out.println("Send fail");
 			//Log.e(getClass().getName(), "Send byte failure: " + ex);	//TODO: should be this format (but only works on Android)
 		}
 		release();
 		
 		//Wait until we get a response or a timeout
 		long time = System.currentTimeMillis();
 		while (waitingForAck != null) {
 			if (System.currentTimeMillis() - time > TIMEOUT)
 				throw new TimeoutException(Thread.currentThread().getStackTrace()[2].getMethodName() + " has timed out");
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException ex) {
 			}
 		}
 		
 		tempAckProcessor = null;
 		
         String response = new String( currentCommand.getContent() );
 
         ackProcessingComplete();
 
         return response;
 	}    
 	
 	public void run() {
 		activeConnection = true;
 
 		while (activeConnection){
 			
 			synchronized (pendingInstructions) {
 				if( pendingInstructions.isEmpty() )
 				{
 					try { 
 						pendingInstructions.wait(1000); 
 					} 
 					catch (InterruptedException ex) {
 						
 					}
 					continue;
 				}
 			}
 			
 			lock();
 			
 			currentInstruction = pendingInstructions.poll();
			Log.d(getClass().getName(), "Sending instruction: " + currentInstruction.getOpcode().name());
 			
 			try {
 				sendBytes(currentInstruction.getInstructionBytes());
 				waitingForAck = currentInstruction.getOpcode();
 				
 			} catch (IOException ex) {
				Log.e("Protocol", "Send error: " + ex);
 			}
 			
 			release();
 		}
 
 	}
 	
 	private void queueInstruction(ProtocolInstruction instr){
 		synchronized (pendingInstructions) {
 			pendingInstructions.add(instr);
        	Log.v("Protocol", "Added new pending instruction of: " + pendingInstructions.size() + " length");
 			pendingInstructions.notify();
 		}
 	}
 	
 	/**
 	 * Sends a ping to the remote device and returns when it is finished. This
 	 * method is blocking.
 	 * @throws TimeoutException if the remote device used too long time to respond 
 	 * 							(defined in TIMEOUT)
 	 */
     public final void ping() throws TimeoutException {
         lock();
         
 		ProtocolInstruction newInstruction = new ProtocolInstruction(OpCode.PING, (byte)0, new byte[1]);
 		
         try {
             sendBytes(newInstruction.getInstructionBytes());
         } catch (IOException ex) {
         	//Log.e("Protocol", "Could not send data: " + ex);
         }
         
         waitingForAck = OpCode.PING;
         
         release();
 		
         //Wait up to TIMEOUT milliseconds before giving up
 		long time = System.currentTimeMillis() + TIMEOUT;
 		while (waitingForAck != null) {
 			if (System.currentTimeMillis() > time) {
 				waitingForAck = null;
 				throw new TimeoutException("Timeout (remote device used too long time to respond)");
 			}
 			try { Thread.sleep(10); } catch (InterruptedException ex) { /*nothing*/ }
 		}
 
         ackProcessingComplete();
     }
     
     /**
      * Same as calling print(text, false)
      * @see public final void print(String text, boolean blocking) throws TimeoutException
      */
 	public final void print(String text) throws TimeoutException{
 		print(text, false);
 	}
 	
     /**
      * Sends a String to the remote device. How the String is handled or what is done
      * with the String is application defined by the remote device. The usual thing
      * is to print the text on a display.
      * @param text Which String to send to the remote device
      * @param blocking TRUE if the method should block until a response or timeout happens.
      * 				   FALSE if the method should return immediately and send the String asynchronously
      * @throws TimeoutException if the remote device used too long time to receive the String
      */
 	public final void print(String text, boolean blocking) throws TimeoutException{
 		ProtocolInstruction newInstruction =
 				new ProtocolInstruction(OpCode.TEXT, (byte)0, text.getBytes());
 		if (!blocking){
 			queueInstruction(newInstruction);
 		}
 		else {
 			// Blocking methodlock();
 			lock();
 
 			waitingForAck = OpCode.TEXT;
 			tempAckProcessor = OpCode.TEXT;
 
 			try {
 				sendBytes(newInstruction.getInstructionBytes());
 			} catch (IOException ex) {
 				System.out.println("Send fail");
 				//Log.e(getClass().getName(), "Send byte failure: " + ex);	//TODO: should be this format (but only works on Android)
 			}
 			release();
 			
 			long time = System.currentTimeMillis();
 			while (waitingForAck != null) {
 				if (System.currentTimeMillis() - time > TIMEOUT){
 					waitingForAck = null;
 					throw new TimeoutException("Timeout");
 				}
 				try {
 					Thread.sleep(10);
 				} catch (InterruptedException ex) {
 				}
 			}
 			
 			tempAckProcessor = null;
 
 			ackProcessingComplete();
 		}
 	}
 
 	/**
 	 * Requests the value of the specified sensor on the remote device. This method is blocking
 	 * until a Timeout happens. What kind of Sensor the remote device supports and how they are
 	 * implemented (and what integer value each sensor represents) is defined on the remote device
 	 * firmware.
 	 * @param sensor which pin to get the value from
 	 * @return the value of the specified sensor
 	 * @throws TimeoutException if the remote device used too long time to respond
 	 * @see The ConnectionMetadata class to retrieve services such as sensor types the remote device supports
 	 */
     public final int sensor(int sensor) throws TimeoutException {
 		ProtocolInstruction newInstruction =
 				new ProtocolInstruction(OpCode.SENSOR, (byte)sensor, new byte[1]);
 		
         lock();
 
         waitingForAck = OpCode.SENSOR;
 
         try {
             sendBytes(newInstruction.getInstructionBytes());
         } catch (IOException ex) {
             System.out.println("Send fail");
         }
 
         release();
 		
 		long time = System.currentTimeMillis();
 		while (waitingForAck != null) {
 			if (System.currentTimeMillis() - time > TIMEOUT){
 				waitingForAck = null;
 				throw new TimeoutException("Timeout");
 			}
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException ex) {
 			}
 		}
 		
 
         byte content[] = currentCommand.getContent();
 
         ackProcessingComplete();
 
         int sensorValue = (content[0] << 8) + toUnsigned(content[1]);
 
         return sensorValue;
     }
 
     /**
      * Helper function to convert a signed byte to an unsigned byte (represented using a signed short in Java)
      */
     private short toUnsigned(byte value) {
         if (value < 0) {
             return (short) ((short) value & (short) 0xFF);
         }
         return (short) value;
     }
 
     /**
      * Same as data(data, false)
      * @see public final void data(int pin, byte[] data) throws TimeoutException
      */
     public final void data(byte[] data) throws TimeoutException {
 		data(data, false);
     }
 	
     /**
      * Sends raw data expressed as a byte array to the remote device to the specified pint
      * @param pin
      * @param data array of bytes to send
      * @param blocking determines if method should wait until a Timeout happens or should return
      * immediately and send data asynchronously.
      * @throws TimeoutException
      */
 	public final void data(byte[] data, boolean blocking) throws TimeoutException{
 		ArrayList<ProtocolInstruction> newInstructions = new ArrayList<ProtocolInstruction>();
 		
 		//ProtocolInstruction tempInstruction;
 		
 		if (data.length > MAX_CONTENT_SIZE){
 			byte[] tempBytes = new byte[MAX_CONTENT_SIZE];
 			
 			int restSize = 0;
 			
 			for (int i = 0; i < data.length; ++i){
 				
 				tempBytes[i % MAX_CONTENT_SIZE] = data[i];
 				if (i % MAX_CONTENT_SIZE == 0){
 					newInstructions.add(
 						new ProtocolInstruction(OpCode.DATA, (byte)1, tempBytes)
 						);
 					
 					if (data.length - i < MAX_CONTENT_SIZE){
 						restSize = data.length - i;
 					}
 				}
 			}
 			
 			byte[] restBytes = new byte[restSize];
 			for (int i = 0; i < restSize; ++i){
 				restBytes[i] = tempBytes[i];
 			}
 			newInstructions.add(
 				new ProtocolInstruction(OpCode.DATA, (byte)0, restBytes)
 				);
 		}
 		else {
 			newInstructions.add(
 				new ProtocolInstruction(OpCode.DATA, (byte)0, data)
 				);
 		}
 		
 		if (!blocking){
 			for (ProtocolInstruction instr : newInstructions){
 				queueInstruction(instr);
 			}
 		}
 		else {
 
 			lock();
 			
 			for (ProtocolInstruction newInstruction : newInstructions){
 
 				waitingForAck = OpCode.DATA;
 
 				try {
 					sendBytes(newInstruction.getInstructionBytes());
 				} catch (IOException ex) {
 					System.out.println("Send fail");
 				}
 
 				long time = System.currentTimeMillis();
 				while (waitingForAck != null) {
 					if (System.currentTimeMillis() - time > TIMEOUT){
 						waitingForAck = null;
 						throw new TimeoutException("Timeout");
 					}
 					try {
 						Thread.sleep(10);
 					} catch (InterruptedException ex) {
 					}
 				}
 
 				ackProcessingComplete();
 			}
 			
 			release();
 		}
 	}
 
 	/**
 	 * Requests the value of the specified pin on the remote device. This method is blocking
 	 * until a Timeout happens.
 	 * @param sensor which pin to get the value from
 	 * @return the value of the specified pin
 	 * @throws TimeoutException if the remote device used too long time to respond
 	 */
     public final boolean read(int pin) throws TimeoutException {
 		ProtocolInstruction newInstruction =
 				new ProtocolInstruction(OpCode.PIN_R, (byte)pin, new byte[1]);
 		
         lock();
 
         waitingForAck = OpCode.PIN_R;
 
         try {
             sendBytes(newInstruction.getInstructionBytes());
         } catch (IOException ex) {
             System.out.println("Send fail");
         }
         release();
 		
 		long time = System.currentTimeMillis();
 		while (waitingForAck != null) {
 			if (System.currentTimeMillis() - time > TIMEOUT){
 				waitingForAck = null;
 				throw new TimeoutException("Timeout");
 			}
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException ex) {
 			}
 		}
 		
 
         byte content[] = currentCommand.getContent();
 
         ackProcessingComplete();
 
         return content[0] > 0 ? true : false;
     }
 
     /**
      * Same as write(pin, value, false)
      * @see public final void write(int pin, boolean value) throws TimeoutException
      */
     public final void write(int pin, boolean value) throws TimeoutException {
 		write(pin, value, false);
     }
 	
     /**
      * Set the specified pin on the remote device to HIGH (true) or LOW (false)
      * @param pin which pin to change
      * @param value true if the pin is to be set HIGH or false if it is LOW
      * @param blocking determines if this method should wait until success or Timeout happens
      * @throws TimeoutException if the remote device used too long to respond
      */
 	public final void write(int pin, boolean value, boolean blocking) throws TimeoutException{
 		ProtocolInstruction newInstruction =
 				new ProtocolInstruction(OpCode.PIN_W, (byte)pin, new byte[] {value ? (byte)1 : (byte)0});
 
 		if (!blocking){
 			queueInstruction(newInstruction);
 		}
 		else {
 			lock();
 
 			waitingForAck = OpCode.PIN_W;
 
 			try {
 				sendBytes(newInstruction.getInstructionBytes());
 			} catch (IOException ex) {
 				System.out.println("Send fail");
 			}
 			release();
 			
 			long time = System.currentTimeMillis();
 			while (waitingForAck != null) {
 				if (System.currentTimeMillis() - time > TIMEOUT){
 					waitingForAck = null;
 					throw new TimeoutException("Timeout");
 				}
 				try {
 					Thread.sleep(10);
 				} catch (InterruptedException ex) {
 				}
 			}
 
 			ackProcessingComplete();
 		}
 	}
 	
     /**
      * Internal mutex lock to prevent multiple threads from sending or reading data
      * through the protocol at the same time.
      */
     private synchronized void lock() {
         while (locked) {
             try {
                 this.wait(1000);
             } catch (InterruptedException ex) {
             }
         }
 
         locked = true;
 		
 		while (waitingForAck != null) {
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException ex) {
 			}
 		}
 		
 		while (processingAck){
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException ex) {
 			}
 		}
     }
 
     /**
      * Releases a previously Locked protocol
      */
     private synchronized void release() {
         if (!locked) {
             throw new IllegalStateException("Already released");
         }
         locked = false;
 		this.notify();
     }
     private boolean processingAck = false;
 
     private void ackProcessing() {
         processingAck = true;
 		
 		waitingForAck = null;
 		
 		while (processingAck){
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException ex) {
 
 			}
 		}
     }
 
     private void ackProcessingComplete() {
         processingAck = false;
     }
     
     /**
      * Called by sub-classes whenever they receive new data. The data is then processed
      * internally as defined by the protocol standard.
      * @param data a single byte received from the remote device
      */
     protected final void byteReceived(byte data) {
         if (currentCommand.byteReceived(data)) {
         	
             // Process command
             if (currentCommand.isAckFor(waitingForAck)) {
             	//Log.v("Protocol", "Ack received for: " + waitingForAck.name());
                 byte tempAck = waitingForAck.value;
                 
 				boolean hadAckProcessor = false;
 
 				for (byte ack : ackProcessors){
 					if (tempAck == ack){
 						ackProcessing();
 						hadAckProcessor = true;
 						break;
 					}
 				}
 				
 				if (!hadAckProcessor){
 					if (tempAckProcessor != null && tempAck == tempAckProcessor.value){
 						ackProcessing();
 					}
 					else {
 						waitingForAck = null;
 					}
 				}
 				
                 currentCommand = new Command();
             } else {
             	//Log.e("Protocol", "Received something unexpected");
                 throw new IllegalArgumentException("Received something unexpected");
             }
         }
     }
 
     /**
      * Same as byteReceived except that it processes multiple bytes
      * @param data array of bytes received from the remote device
      */
     protected final void bytesReceived(byte[] data) {
         for (byte item : data) {
             byteReceived(item);
         }
     }
 
     /**
      * Sends data to the remote device. The specifics of this method is implemented
      * by the subclass that inherits Protocol (could be through a Socket or a 
      * ByteStream for example)
      * @param data array of bytes to send to the remote
      * @throws IOException if there was a problem sending the data
      */
     protected abstract void sendBytes(byte[] data) throws IOException;
 
     /**
      * The Protocol mechanic is handled internally by a state machine to process
      * commands and data. This enum defines the different states the protocol class
      * can enter
      */
     private enum State {
         STATE_START,
         STATE_SIZE,
         STATE_OPCODE,
         STATE_FLAG,
         STATE_CONTENT,
         STATE_DONE
     }
     
     /**
      * Private helper class to process comands
      */
     private class Command {
 
         private final byte START_BYTE = (byte) 0xFF;
 
         private State state;
         private byte size;
         private byte opcode;
         private byte flag;
         private byte[] content;
         private int contentCounter;
 
         public Command() {
             state = State.STATE_START;
             contentCounter = 0;
         }
 
         public boolean byteReceived(byte data) {
             switch (state) {
                 case STATE_START:
                     if (data == START_BYTE) {
                         state = State.STATE_SIZE;
                     }
                     break;
                 case STATE_SIZE:
                     size = data;
                     content = new byte[size];
                     state = State.STATE_OPCODE;
                     break;
                 case STATE_OPCODE:
                     opcode = data;
                     state = State.STATE_FLAG;
                     break;
                 case STATE_FLAG:
                     flag = data;
                     state = State.STATE_CONTENT;
                     break;
                 case STATE_CONTENT:
                     content[contentCounter++] = data;
                     if (contentCounter >= size - 3) {
                         state = State.STATE_DONE;
                         return true;
                     }
                     break;
                 case STATE_DONE:
                     throw new IndexOutOfBoundsException("Command already finished");
                 default:
                     break;
             }
 
             return false;
         }
 
         /*public byte getOpcode() {
             return opcode;
         }*/
 
         public byte[] getContent() {
             return content;
         }
 
         public boolean isAckFor(OpCode command) {
             return opcode == OpCode.RESPONSE.value && flag == command.value;
         }
     }
 
 }
 
