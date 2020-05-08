 /*
 Server.java
 Written by George Brinzea
 
 Handles connections from the phone client and 
 sends information for direction calculations
 */
 
 
 import java.net.*;
 import java.io.*;
 //import javax.imageio.ImageIO;
 //import java.awt.Image;
 //import java.awt.image.BufferedImage;
 //import javax.swing.ImageIcon;
 //import java.awt.Graphics2D;
 
 public class Server {
 	
 	static int PORT = 4444;
 	
 	static Socket clientSocket = null;
 	
 	static class Connection extends Thread{
 		public void run(){
 			Socket threadedSocket = clientSocket;
 			
 			PrintWriter out = null;
 			ObjectOutputStream oos = null;
 			ObjectInputStream ois = null;
 			BufferedReader in = null;
 			try {
 				out = new PrintWriter(threadedSocket.getOutputStream(), true);
 				in = new BufferedReader(new InputStreamReader(threadedSocket.getInputStream()));
 				oos = new ObjectOutputStream(threadedSocket.getOutputStream());
 				ois = new ObjectInputStream(threadedSocket.getInputStream());
 			} catch (IOException e) {
 				System.err.println("I/O stream creation error.");
 				e.printStackTrace();
 				System.exit(1);
 			}
 	        	
 	        	UserInput ui = null;
 	        	
 	        	//Takes in the UserInput class from the client
 	        	try{
 	        		ui = (UserInput) ois.readObject();
 	        		System.out.println("Recieved object.");
 	        	}catch(Exception e){
 	        		System.err.println("Object input error.");
 	        		e.printStackTrace();
 				System.exit(1);
 			}
 	        	
	        	System.out.println("Transport: " + ui.getTransport() + "\nFloor: " + ui.getFloor() + "\nLatitude: " + ui.getLatitude() + "\nLongitude: " + ui.getLongitude() + "\roomNumber: " + ui.getRoomNumber() + "\nonAcademicRoom: " + ui.getNonAcademicRoom() + "\professorName: " + ui.getProfessorName());
 	        	/*ui.setTransport(Transport.ELEVATOR);
 	        	ui.setFloor(Floor.SECOND);
 	        	ui.setLatitude(100);
 	        	ui.setLongitude(200);
 	        	
 	        	try{
 	        		oos.writeObject(ui);
 	        	}catch(Exception e){
 				System.exit(1);
 			}*/
 			
 			//Process an image based on the user input//
 				
 			//Send back data to the client//
 	        
 	        	//out.println("The data has been recieved");
 			
 			try {
 				out.close();
 				in.close();
 				oos.close();
 				ois.close();
 				clientSocket.close();
 			}catch (IOException e) {
 				e.printStackTrace();
 				System.exit(1);
 			}	
 		}
 	}
 	
 	public static void main(String[] args) throws IOException {
 		ServerSocket serverSocket = null;
         	try {
             		serverSocket = new ServerSocket(PORT);
         	} catch (IOException e) {
            		System.err.println("Could not listen on port: " + PORT + ".");
            		System.exit(1);
        		}
         
         	Connection connection = null;
         	try {
         		while(true){
         			clientSocket = serverSocket.accept();
         			connection = new Connection();
         			connection.start();
 				
             			System.out.println("Made a new connetion.");
         		}
         	} catch (IOException e) {
            		System.err.println("Connection accept failed.");
             		System.exit(1);
         	}
 	}
 }
