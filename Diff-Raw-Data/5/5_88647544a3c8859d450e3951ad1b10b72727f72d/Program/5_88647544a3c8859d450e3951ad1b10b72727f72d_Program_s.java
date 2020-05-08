 /**
  * 
  */
 package AP2DX.coordinator;
 
 import java.net.*;
 import java.util.ArrayList;
 import java.io.*;
 
 import AP2DX.*;
 import AP2DX.specializedMessages.*;
 import AP2DX.usarsim.*;
 import AP2DX.usarsim.specialized.*;
 
 
 /**
  * @author Jasper Timmer
  *
  */
 public class Program extends AP2DXBase {
     PrintWriter out;
     UsarSimMessageReader in;
 
 
     /**
      * entrypoint of coordinator 
      */
     public static void main (String[] args){
         new Program();
     }
 
 
     /**
      * constructor
      */
     public Program() 
     {
         super(Module.COORDINATOR); // Explicitly calls base constructor
         System.out.println(" Running Coordinator... ");
 
     }
 
     @Override
         protected void doOverride() {
             System.out.println("Warning, security override in progress");
             config = readConfig();
             System.out.println("Config: " + config.get("sim_port"));
            UsarSimMessage message = new InitMessage();
             System.out.println("Message: " + message.toString());
 
             String address = config.get("sim_address").toString();
             int port = Integer.parseInt(config.get("sim_port").toString());
 
             try 
             {
                 Socket socket = new Socket(address, port);
                 out = new PrintWriter(socket.getOutputStream());
                 in = new UsarSimMessageReader(socket.getInputStream());
             }
             catch (Exception ex) 
             {
                 ex.printStackTrace();
             }
 
 
 
 
             try {
                 // TODO: this was once used to test drive P2DX, but now it can not be used any longer
                 //out.println("INIT {ClassName USARBot.P2DX} {Location 4.5,1.9,1.8} {Name R1}");
                 ////out.flush();
                 //Message msg = in.readMessage();
                 //System.out.println(msg.getValues().get("msgtype"));
                 //Thread.sleep(10);
                 //out.println("DRIVE {Left -1.0} {Right 1.0}");
                 //Thread.sleep(5000);
                 //out.println("DRIVE {Left 1.0} {Right -1.0}");
                 //System.out.println(in.readLine());
 
 
                 //This should be able to initialize a robot now!
                 out.print(message);
                 out.flush();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
     /**
      * ComponentLogic (for now) creates a message from an USAR_SENSOR message, to forward
      * the sonar sensor messages to our sensor module.
      * @author Maarten de Waard
      */
     @Override
         public ArrayList<AP2DXMessage> componentLogic(Message msg) {
             ArrayList<AP2DXMessage> messageList = new ArrayList<AP2DXMessage> ();  
             switch (msg.getMsgType())
             {
                 case USAR_SENSOR:
                     try
                     {
                        SonarMessage messageIn = new SonarMessage((UsarSimMessage) msg);
                         //Create a new message to the Sensor module
                         SonarSensorMessage message = new SonarSensorMessage(IAM, Module.SENSOR);
                         // Put the right values in the message
                         message.setRangeArray(messageIn.getData());
                         message.setTime(messageIn.getTime());
                         messageList.add(message);
                     }
                     catch (Exception e)
                     {
                         System.err.println("Some exception occured while making a SonarMessage");
                         System.err.println(e.getMessage());
                     }
                 default:
                     System.out.println("Unexpected message type in ap2dx.sensor.Program: " + msg.getMsgType());
             }
             return messageList;
         }
 }
