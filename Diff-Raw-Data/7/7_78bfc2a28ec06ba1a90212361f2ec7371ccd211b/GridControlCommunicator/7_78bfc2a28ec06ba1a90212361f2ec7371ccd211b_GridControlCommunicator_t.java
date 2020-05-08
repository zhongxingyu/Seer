 package pc;
 
 import java.io.*;
 import lejos.pc.comm.*;
 
 /**
  * Starter version - does everything except read and write useful data
  * 
  * @author Roger
  */
 public class GridControlCommunicator {
 
 	public GridControlCommunicator(GNC control) {
 		this.control = control; // callback path
 		System.out.println("GridControlCom1 built");
 	}
 
 	/**
 	 * establish a bluetooth connection to the robot ; needs the robot name
 	 * 
 	 * @param name
 	 */
 
 	public void connect(String name) {
 		try {
 			connector.close();
 		} catch (Exception e) {
 			System.out.println(e);
 		}
 		System.out.println(" conecting to " + name);
 		if (connector.connectTo(name, "", NXTCommFactory.BLUETOOTH)) {
			control.setMessage("Connected to " + name);
 			System.out.println(" connected !");
 			dataIn = new DataInputStream(connector.getInputStream());
 			dataOut = new DataOutputStream(connector.getOutputStream());
 			if (dataIn == null)
 				System.out.println(" no Data  ");
 			else if (!reader.isRunning)
 				reader.start();
 		} else
 			System.out.println(" no connection ");
 	}
 
 	public void send(int x, int y) {
 		System.out.println(" Comm send " + x + " " + y);
 		try {
 			dataOut.writeInt(0); // replace with useful code
 		} catch (IOException e) {
 			System.out.print(e);
 		}
 	}
 
 	/**
 	 * reads the data input stream, and calls DrawRobotPath() and DrawObstacle()
 	 * uses OffScreenDrawing, dataIn
 	 * 
 	 * @author Roger Glassey
 	 */
 	class Reader extends Thread {
 
 		int count = 0;
 		boolean isRunning = false;
 
 		// assumes robot message structure is 3 integers: header, x,y
 		public void run() {
 			System.out.println(" reader started GridControlComm1 ");
 			isRunning = true;
 			int header = 0;
 			int x = 0;
 			int y = 0;
 			String message = "";
 			while (isRunning) {
 				try {
 					// read the message sent by the robot
 					dataIn.readInt(); // replace with something useful
 
 				} catch (IOException e) {
 					System.out.println("Read Exception in GridControlComm");
 					count++;
 				}
 				message = "Received " + x + " " + y + " code " + header;
				control.setMessage(message);
				//control.incomingMessage(header, x, y);
 			}
 		}
 	}
 
 	/**
 	 * call back reference; calls setMessage, dreawRobotPositin, drasObstacle;
 	 */
 	GNC control;
 
 	/***
 	 * connects to NXT using bluetooth. Provides data input stream and data
 	 * output stream
 	 */
 	private NXTConnector connector = new NXTConnector();
 	/**
 	 * used by reader
 	 */
 	private DataInputStream dataIn;
 	/**
 	 * used by send()
 	 */
 	private DataOutputStream dataOut;
 	/**
 	 * inner class extends Thread; listens for incoming data from the NXT
 	 */
 	private Reader reader = new Reader();
 }
