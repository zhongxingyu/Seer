 package com.josh.lejos.scifair2015;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import lejos.nxt.LCD;
 import lejos.nxt.Motor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.comm.Bluetooth;
 import lejos.nxt.comm.NXTConnection;
 import lejos.util.EndianTools;
 
 import org.scifair.util.BaseLejos;
 
 import routineFunctions.RTConglamoration;
 
 public class SensorPower extends BaseLejos {
 	DataOutputStream dOut;
 	DataInputStream dIn;
 	NXTConnection conn;
 	VoltmeterSensor voltageSensor = new VoltmeterSensor(SensorPort.S2);
 	int MotorA = 0;
 	int MotorB = 0;
 	int MotorC = 0;
 
 	@Override
 	public void run() {
 
 		RTConglamoration rtc = new RTConglamoration();
 		LCD.drawString("waiting", 0, 0);
 		conn = Bluetooth.waitForConnection(0, NXTConnection.PACKET);
 
 		dOut = conn.openDataOutputStream();
 		dIn = conn.openDataInputStream();
 		LCD.drawString("connected", 2, 2);
 		while (true) {
 			int c;
 			try {
 				if (dIn.available() == 0)
 					continue;
 
 				c = readInt();
 
 				System.out.println(c);
 				// value = dIn.readDouble();
 				if (c == 1) {
 					SensorPort.S1.setPowerType(1);
 					// rtc.MotorActivate();
 				} else if (c == 2) {
 
 					Motor.A.rotate(180);
 					sendInt(c);
 					// System.out.println("writing short: "+ c);
 
 				} else if (c == 3) {
 
 					Motor.A.rotate(90);
 					sendInt(c);
 					// System.out.println("writing short: "+ c);
 				} else if (c == 4) {
 
 					Motor.B.rotate(180);
 					sendInt(c);
 					// rtc.PowerOff();
 				} else if (c == 5) {
 					Motor.B.rotate(90);
 					sendInt(c);
 				} else if (c == 6) {
 					Motor.C.rotate(180);
 					sendInt(c);
 				} else if (c == 7) {
 					Motor.C.rotate(90);
 					sendInt(c);
 				} else if (c == 8) {
 					sendInt(c);
 					for (int i = 0; i < 30; i++) {
 						try {
 							Thread.sleep(200);
 							Motor.A.rotate(3);
 							SensorPort.S1.setPowerType(1);
 							System.out.println(voltageSensor.readVoltage());
 							Thread.sleep(1000);
 							SensorPort.S1.setPowerType(0);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 					}
 
 				} else if (c == 9) {
 					for (int i = 0; i < 30; i++) {
 						sendInt(c);
 						try {
 
 							SensorPort.S1.setPowerType(1);
 							Motor.B.rotate(3);
 							System.out.println(voltageSensor.readVoltage());
 							SensorPort.S1.setPowerType(0);
 							Thread.sleep(300);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 
 				} else if (c == 10) {
 					sendInt(c);
 					try {
 
 						SensorPort.S1.setPowerType(1);
 						Motor.C.rotate(3);
 						System.out.println(voltageSensor.readVoltage());
 						SensorPort.S1.setPowerType(0);
 						Thread.sleep(300);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				} else if (c == 11) {
 					SensorPort.S1.setPowerType(0);
 				} else if(c==20)
 				{
 					
 					double voltage = voltageSensor.readVoltage();
 					System.out.println(voltage);
 					sendInt((int)voltage);
 				}
 				  
 				
 				else if (c == 30) {
 					int motor = readInt();
 					int position = readInt();
 					switch(motor)
 					{
 					case 1:
						Motor.A.rotateTo(position, true);
 						break;
 					case 2:
						Motor.B.rotateTo(position,true);
 						break;
 					case 3:	
						Motor.C.rotateTo(position,true);
 						break;
 					}
 					
 				} else if (c == 1000) {
 					break;
 				} else {
 					continue;
 				}
 			} catch (IOException e) {
 				System.out.println(e.getMessage());
 			}
 		}
 
 		close();
 	}
 
 	public int readInt() {
 		byte[] b = new byte[4];
 		try {
 			dIn.read(b, 0, 4);
 		} catch (IOException e) {
 			e.getMessage();
 			return -666;
 		}
 		return EndianTools.decodeIntLE(b, 0);
 	}
 
 	public void sendInt(int i) {
 		byte[] b = new byte[4];
 		EndianTools.encodeIntLE(i, b, 0);
 		try {
 			dOut.write(b);
 			dOut.flush();
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	public void close() {
 
 		try {
 			dOut.close();
 			dIn.close();
 			conn.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args) {
 
 		SensorPower sp = new SensorPower();
 		sp.run();
 
 	}
 }
