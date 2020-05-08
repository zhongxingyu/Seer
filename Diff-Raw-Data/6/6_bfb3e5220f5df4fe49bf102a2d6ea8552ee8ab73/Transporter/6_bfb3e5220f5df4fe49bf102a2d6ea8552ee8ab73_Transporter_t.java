 import lejos.pc.comm.*;
 import java.io.*;
 import java.util.*;
 
 public class Transporter implements Runnable {
 
 	private DataInputStream in;
 	private DataOutputStream out;
 	public String name;
 	public Thread thread;
 	
 	private ArrayList<Character> cmdq = new ArrayList<Character>();
 
 	public Transporter(String name, DataInputStream in, DataOutputStream out) {
 		this.name = name;
 		this.in = in;
 		this.out = out;
 		thread = new Thread(this);
 		thread.start();
 	}
 
 	public void run() {
 		while (true) {
 			try {
 				while (cmdq.get(0) != null) {
 					char cmd = cmdq.get(0);
 					
 					if (cmd == '.') {
 						this.disconnect();
 					}
 					else {
 						sendCommand(cmd);
 						System.out.println(name + ": busy(" + cmd + ")...");
 						waitForMessage('k');
 						cmdq.remove(0);
 					}
					Thread.yield();
 				}
				System.out.println(name + ": ready.");
				
 				Thread.yield();
 				Thread.sleep(500);
 				Thread.yield();
 			}
 			catch (Exception e) {
 				System.out.println(e.getMessage());
 			}
 		}
 	}
 	
 	public void enqueueCommand(char cmd) throws Exception {
 		this.cmdq.add(cmd);
 	}
 	
 	private void sendCommand(char cmd) throws Exception {
 		System.out.println(name + ": sending command(" + cmd + ")...");
 		out.writeChar(cmd);
 		out.flush();
 	}
 	
 	public void waitForMessage(char msg) throws Exception {
 		char ch = in.readChar();
 	 	while(ch != msg) {
 			ch = in.readChar();
 		}
 		return;
 	}
 	
 	public void disconnect() throws Exception {
 		System.out.println(this.name + ": sending shutdown request...");
 		sendCommand('.');
 		System.out.println(this.name + ": busy (shutting down)...");
 		waitForMessage('k');
 		Server.transporters.remove(this);
 		System.out.println(this.name + ": successfully disconnected.");
 	}
 	
 	public static Transporter getByName(String name) {
 		for (Transporter t: Server.transporters) {
 			if (t.name.equals(name)) {
 				return t;
 			}
 		}
 		return null;
 	}
 }
