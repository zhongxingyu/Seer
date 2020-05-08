 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import net.tinyos.message.Message;
 import net.tinyos.message.MessageListener;
 import net.tinyos.message.MoteIF;
 import net.tinyos.packet.BuildSource;
 import net.tinyos.packet.PhoenixSource;
 import net.tinyos.util.PrintStreamMessenger;
 
 /**
  * @author Andrea Crotti, Marius Grysla, Oscar Dustmann
  * 
  * TODO: connect correctly connector & GUI
  * TODO: understand how to write on the debug output window
  * 
  */
 public class BlinkConnector implements MessageListener {
 
     // A reference the the user interface
     private BlinkGUI gui;
     
     // The serial interface
     MoteIF moteInterface = null;
     
     // A sequential number
     short seqNo = 1;
     
     // The mote ID we communicate with directly
     int commID = 0;
 
     /**
      * Constructor of the BlinkConnector class.
      * 
      * @param moteInterface The interface to a node.
      */
     public BlinkConnector(MoteIF moteInterface){
     	this.moteInterface = moteInterface;
     	this.moteInterface.registerListener(new BlinkMsg(), this);
     }
     
     /**
      * Connects to a mote via serial cable.
      * 
      * @param ip The IP-Address
      * @param port The Port
      */
     public void connect(String ip, String port){
     	String source = "sf@" + ip + ":" + port;
     	
     	try{
     		connect(source);
     	}catch(Exception e){
     		gui.print(e.getMessage());
     	}
     }
     
     public void connect(String source) throws IOException{
     	PhoenixSource phoenix;
 		phoenix = BuildSource.makePhoenix(source, PrintStreamMessenger.err);
 		
 		// Set the mote Interface
 		this.moteInterface = new MoteIF(phoenix);
     }
     
     public void disconnect(){
     	this.moteInterface = null;
     }
 
     /**
      * General method to send an arbitrary LED 
      * 
      * @param destination
      * @param mask
      */
     public void sendLedMask(short destination, short mask){
     	// Create the message
     	BlinkMsg message = new BlinkMsg();
     	
     	// Set the contents
     	message.set_dests(destination);
     	message.set_type((short)1);
     	message.set_instr(mask);
     	message.set_seqno(this.seqNo++);
     	
     	gui.print("Sended message with mask " + mask + " and destination "+ destination);
     	
     	try{
             moteInterface.send(this.commID, message);
     	} catch(Exception e) {
             this.gui.print(e.getMessage());
     	}
     }
     
     public void requestLightData(short destination){
     	requestData(destination, (short)1);
     }
     
 	public void requestInfraredData(short destination){
 		requestData(destination, (short)2);
 	}
 	
 	public void requestHumidityData(short destination){
 		requestData(destination, (short)3);
 	}
 	
 	public void requestTemperatureData(short destination){
 		requestData(destination, (short)4);
 	}
 	
 	public void requestData(short destination, short type){
 		// Create the message
     	BlinkMsg message = new BlinkMsg();
     	
     	// Set the contents
     	message.set_dests(destination);
     	//message.set_sender(0);
     	//message.set_seqno(this.seqNo++);
     	message.set_type((short)2);
     	message.set_instr(type);
     	
     	this.gui.print("Requesting data type " + type + " from destination " + destination);
     	
     	try{
             moteInterface.send(this.commID, message);
     	} catch(Exception e) {
             this.gui.print(e.getMessage());
     	}
 	}
     
     /**
      * Implements the MessageListener interface.
      */
     public void messageReceived(int to, Message message) {
     	BlinkMsg msg = (BlinkMsg)message;
     	
     	int sender = getIDFromBM(msg.get_sender());
     	short instr = msg.get_instr();
     	int data = msg.get_data();
    	int converted;
     	
     	switch (instr) {
 		case 1:
 			this.gui.print("Mote " + sender + " sensed Light: " + convertLight(data));
 			break;
 		case 2:
 			this.gui.print("Mote " + sender + " sensed Infrared: " + convertInfrared(data));
 			break;
 		case 3:
 			this.gui.print("Mote " + sender + " sensed Humidity: " + convertHumidity(data) + "%");
 			break;
 		case 4:
 			this.gui.print("Mote " + sender + " sensed Temperature: " + convertTemp(data) + "°C");
 			break;
 		default:
 			break;
 		}
    	
        this.gui.print("Got sensing result of type " + instr + " from mote " + sender + " with result " + data);
     }
     
     public int convertLight(int data){
     	return data;
     }
     
     public int convertInfrared(int data){
     	return data;
     }
     
     public double convertHumidity(int data){
     	double hum_lin = -0.0000028*data*data + 0.0405*data-4;
 
     	return hum_lin;
     }
     
     public double convertTemp(int data){
     	return (-38.4 + 0.0098 * data);
     }
 
     public int getIDFromBM(int bm){
     	int local_bm = bm;
     	int counter = 0;
     	local_bm >>= 1;
     	while(local_bm != 0){
     	    local_bm >>= 1;
     	    counter++;
     	}
     	return counter;
     }
     
     /**
      * Sets the Gui to this Class.
      * 
      * @param gui A BlinkGui Instance.
      */
     public void setGui(BlinkGUI gui){
         this.gui = gui;
     }
 
     private static void usage() {
        	System.err.println("usage: BlinkConnector [-comm <source>]");
     }		
     
     /**
      * This function starts the program.
      * Both the connector class and the corresponding GUI are created and BlinkConnector.start() is called.
 	 *
      * @param args Command-line arguments.
      */
     public static void main(String[] args) {
     	// Check the command line arguments
     	String source = null;
         if (args.length == 2) {
           if (!args[0].equals("-comm")) {
     	usage();
     	System.exit(1);
           }
           source = args[1];
         }
         else if (args.length != 0) {
           usage();
           System.exit(1);
         }
     	
         PhoenixSource phoenix;
         
         if (source == null) {
           phoenix = BuildSource.makePhoenix(PrintStreamMessenger.err);
         }
         else {
           phoenix = BuildSource.makePhoenix(source, PrintStreamMessenger.err);
         }
 
         MoteIF mif = new MoteIF(phoenix);
     	
     	// Create the Blink Connector
         BlinkConnector connector = new BlinkConnector(mif);
 
         // Create the Gui
         BlinkGUI gui = new BlinkGUI(connector);
 
         // Connect the GUI to the connector
         connector.setGui(gui);
     }
 
 }
