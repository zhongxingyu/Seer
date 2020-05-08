 import javax.swing.*;
 import java.io.*;
 import java.net.Socket;
 import java.awt.*;
 import java.awt.image.*;
 
 public class PaintFrame extends JFrame {
 	
 	Socket clientSocket;
 	DataOutputStream out;
 	DataInputStream in;
 	
 	public PaintFrame(){
		setVisible(true);
 		setSize(500, 500);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
 		
 		try{
			clientSocket = new Socket("localhost", 12356);  
 	        out = new DataOutputStream(clientSocket.getOutputStream());
 	        in = new DataInputStream(clientSocket.getInputStream());
 		}
 		catch(IOException ioe){
 			System.out.println("IOException setting up applet in frame...");
 		}
 		
 		getContentPane().add(new SidePanel(in, out));
 		getContentPane().add(new PaintApplet(in, out));
 	}
 	
 	
 	public static void main(String[] args){
 		new PaintFrame();
 	}
 	
 }
