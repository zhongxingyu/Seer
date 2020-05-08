 import java.awt.AWTException;
 import java.awt.Robot;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 
 import javax.swing.JLabel;
 
 import org.omg.CORBA.DataOutputStream;
 
 public class HandShake {
 
 	private Thread receiver = null;;
 	private boolean isOn = false;
 	private compute com = null;
 	private Thread remoteDesktop = null;
 	private ImageSender imgSendObj = null;
 	public ServerSocket socket;
 	public Socket serverSocket=null;
 	private boolean first=true;
 	private int port = 7895;
 	private boolean isAirPcInSession=false;
 
 	
 
 	private void gateway() {
 		
 		Thread gateway=new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				try {
 					socket = new ServerSocket(port);
					//if(com==null)
						//startMouseKeyboard();
 					while (true) {
 
 						serverSocket = socket.accept();
 						DataInputStream inStream = new DataInputStream(serverSocket.getInputStream());
 						java.io.DataOutputStream out = new java.io.DataOutputStream(serverSocket.getOutputStream());
 						String inst = inStream.readUTF();
 						String split[] = inst.split(",");
 						System.out.println(inst);
 						
 						if ((split[0].equals("airMouse") || split[0].equals("airPc"))&&isOn)
 						{
 							if(split[0].equals("airPc")&&isAirPcInSession)
 								out.writeUTF("inSession");
 							else
 							out.writeUTF("send Password");
 						}
 
 						else if (split[0].equals("airPcPassword")) {
 							Password passObj = new Password();
 
 							if (split[1].equals(passObj.getPassword()))
 							{
 								out.writeUTF("password accepted");
 								
 								
								startMouseKeyboard();
 								startRemoteDesktop();
 								isAirPcInSession=true;
 							}
 							else
 								out.writeUTF("password rejected");
 							passObj.destroy();
 
 						}
 						else if(split[0].equals("airPcClose"))
 						{
 							
 							imgSendObj.destroy();
 							com.cleanup();
 							isAirPcInSession=false;
 						}
 					}
 
 				}
 
 				catch (Exception e) {
 				
 					System.out.println("hi");
 					System.out.println(e.getMessage());
 				}
 			}
 		});
 		
 		gateway.start();
 		
 	}
 
 	private void startMouseKeyboard() {
 
 		com = new compute();
 		System.out.println("inside");
 
 		receiver = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				com.setConnection();
 
 			}
 
 		});
 		
 		receiver.start();
 	}
 
 	private void startRemoteDesktop() {
 		imgSendObj = new ImageSender();
 
 		remoteDesktop = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 
 				imgSendObj.RemoteDesktopEngine();
 
 			}
 		});
 		remoteDesktop.start();
 	}
 
 	public void stop(JLabel status) {
 
 		if(isOn)
 		{
 			try
 			{
 				status.setText("Status : OFF");
 				isOn=false;
 				
 				cleanup();
 				
 			}
 			catch(Exception e)
 			{
 				System.out.println("hi1");
 				System.out.println(e.getMessage());
 			}
 		}
 		
 		
 
 	}
 
 	public void cleanup()
 	{
 		if(com!=null)
 			com.cleanup();
 	}
 	
 
 	public void start(JLabel status) {
 
 		
 		if(isOn==false)
 		{
 			if(first)
 			gateway();
 			isOn=true;
 			first=false;
 			status.setText("Status : ON");
 		}
 	
 		
 	}
 
 }
