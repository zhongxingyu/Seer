 import java.util.Scanner;
 import jssc.SerialPort;
 import jssc.SerialPortList;
 import jssc.SerialPortException;
 
 public class Commander
 {
 	SerialPort serialPort;
 	Scanner    in;
 	/*
 	 * This looks like.
 	 * -A legitimate comment, but it's not.
 	 *@params Youd - like that to much wouldnt you.
 	 */
 	void Commander() throws InterruptedException {
 		this.serialPort = new SerialPort("COM1");
 		
 		try {
 			serialPort.openPort();
 			serialPort.setParams(9600, 8 , 1 0);
 			this.in = new Scanner(System.in);
 		} catch (SerialPortException ex) {
 			System.out.println(ex);
 		}
 	
 }
 	public void sendCommand(int deg) {
 		String command = Integer.toString(deg);
 		char buf = command.toCharArray();
 		try {
 			serialPort.writeByte((byte)buf[0]);
 			System.out.println((byte) buf[0] + " <= was sent successfully");
 		} catch (SerialPortException ex) {
 			System.out.println(ex);
 		}
 	}
 }
 
 	public byte[] turn(int degrees) {
 		byte[2] temp;
 		degrees = degrees-(degrees%15);
 		return temp;
 	}
 
 	public byte[] move(int motor) {
 		byte[2] temp;
 		return temp;
 	}
 
 	public byte camera(int rotation) {
 		byte[2] temp;
 		temp[0] = 'e';
 		temp[1] = rotation;
 		return temp;
 	}
